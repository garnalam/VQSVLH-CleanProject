# 189 Battle Phase 10-B Body-Attached Effect Overlay Audit

Status: AUDIT COMPLETE, CODE CHANGE NOT DONE IN THIS SLICE

Closeout note: normal P7 AH renderer coverage for Phase 10-B is closed out in
`190_battle_phase10b_normal_p7_ah_closeout_coverage.md`.

## Scope

Phase 10-B audits body-attached battle visual systems after Phase 10-A HUD status icons.

This is not the HUD status icon path:

- HUD status icons use sprite `325` / image `804`.
- That was handled in Phase 10-A.

This document covers the actor/body overlay paths:

- `game.b.u` actor action/effect object
- `game.b.L` species/state overlay object
- `game.d.H` battle special overlay object
- root `ah.java`
- `effect.mid`
- `speffect.mid`
- `bufDebuf.mid` where active queues call `H`

## Source Channels

| Channel | Source owner | Creation path | Draw path | Meaning | Rebuild status |
|---|---|---|---|---|---|
| Actor action `u` | `game.b.u` | `game.b.a(short s2, byte by)` creates `new ah()` with row `[s2, by, direction]` | `game.b.a(Graphics)` draws `u` after actor and L | Actor action/effect segment from `effect.mid` or `bufDebuf.mid` kind `0` | PORTED/PARTIAL through `P7ActorAnimation` and active queue actor-action slices |
| Species overlay `L` | `game.b.L` | `game.b.d(1)` calls private `z(speffectId)` for species `0,10,62,75,87,91,92,97,98` | `game.b.a(Graphics)` draws before/after actor depending `Z` | State 1 attack visual attached to specific species | PORTED/PARTIAL with smoke species coverage; exact MIDP `drawRGB` parity pending |
| Battle special `H` | `game.d.H` | `game.d.n()` normal P7 special chunks or `game.d.o()` P12/P13 active queue kind `1` | `game.d` draw switch draws `H.a(Graphics)` before/around actors depending state | Body overlay built from `speffect.mid` with runtime actor snapshot | PORTED/PARTIAL by AH type, see matrices below |
| Catch `H` | `game.d.H` + `aj` | `game.d.e(byte)` P17 catch phases create type 8 rows manually | `game.d` P17 draw path checks `H.c()` and `aj` | Capture shrink/escape effect | PORTED/PARTIAL; not main Phase 10-B target unless catch visuals are selected |
| Death visual | `game.b.d(3)` / P7 KO | state 3 creates AH type 16 row in `game.b.d(byte)` | actor hidden while source-shaped death effect runs | KO/death strip/fade visual | PORTED/PARTIAL from earlier P7 death slices |

## Source Facts

### `game.b.u`

Source: `modules/source_code/decoded/decompiled_source_cfr/game/b.java`

`game.b.a(short s2, byte by)`:

```text
u = new ah()
u.a([s2, by, direction])
u.b(actorX, actorY)
if (s2 == 20 && by == 3 || s2 == 22 && by == 4) adjust Y by base sprite bounds
u.c(true)
```

`game.b.a(Graphics)` draws `u` after base actor and `L`.

Meaning: this is not a status icon. It is an actor/effect action object used by `effect.mid` chunks where `chunk[1] == 0`, and by `bufDebuf.mid` active queue segments where segment kind is `0`.

### `game.b.L`

Source: `game.b.d(byte by)` and private `z(int speffectId)`.

State `1` creates `L` only for these species:

| Species | Speffect id | AH type | Draw after actor? | Rebuild status |
|---:|---:|---:|---|---|
| `0` | `27` | `11` | no | PORTED/PARTIAL smoke exists |
| `10` | `28` | `15` | yes, `Z = 1` | PORTED/PARTIAL smoke exists |
| `91` | `26` | `14` | no | PORTED/PARTIAL smoke exists |
| `92` | `25` | `11` | no | PORTED/PARTIAL smoke exists |
| `97` | `23` | `13` | no | PORTED/PARTIAL smoke exists |
| `98` | `23` | `13` | no | PORTED/PARTIAL smoke exists |
| `62` | `24` | `13` | no | PORTED/PARTIAL smoke exists |
| `75` | `20` | `11` | no | PORTED/PARTIAL smoke exists |
| `87` | `21` | `12` | no | PORTED/PARTIAL smoke exists |

Start condition:

```text
if L != null && U == 1 && base animation reports frame 1 via actorSprite.b(1):
    L.a()
```

Draw order:

```text
if L != null && Z == 0: draw L
draw base actor if visible
if L != null && Z == 1: draw L
if u != null: draw u
```

### `game.d.H` Normal P7

Source: `game.d case 7`, `game.d.n()`, and update logic around P7.

`effect.mid` rows are 7-byte chunks:

| Chunk offset | Meaning | Status |
|---:|---|---|
| `0` | `0` target side, non-zero attacker side | VERIFIED |
| `1` | `1` means create `H = new ah()` from `speffect.mid`; `0` means actor action `u` | VERIFIED |
| `2` | speffect id or actor action id | VERIFIED |
| `3` | actor action param/state | VERIFIED |
| `4` | frame trigger to advance next chunk | PORTED/PARTIAL |
| `5` | frame trigger to change base actor state | PORTED/PARTIAL |
| `6` | base actor state for offset `5` trigger | PORTED/PARTIAL |

Runtime row passed to `ah` is:

```text
[speffectType, actorX, actorY, actorSpriteId, actorAnimState, actorDirection, ...speffectParams]
```

P7 `H` lifecycle:

1. `n()` prepares the chunk.
2. If special `H` exists, source waits until actor branch or trigger condition allows start.
3. `H.a()` starts.
4. Source hides the owner actor with `b(false)`.
5. `H.e()` ticks until complete.
6. Source restores the actor with `b(true)`.
7. It advances the next chunk or resolves damage/result.

### `game.d.H` P12/P13 Active Queue

Source: `game.d.o()` creates active queue visual segments from `bufDebuf.mid`.

Segment format is 4 bytes:

```text
[kind, id, param, trigger]
```

- `kind == 1`: create `H = new ah()` from `speffect.mid[id]`
- `kind == 0`: call actor action `b.a(id, param)` into `u`

Source visual gate:

```text
ai[0] = {3,5,13}
ai[1] = {0,1,2,3,8,9,10}
```

Only those buff/debuff ids play active queue visuals; other ids tick immediately.

## Speffect Type Distribution

From `modules/script/decoded/data__script__speffect.mid.json`:

| AH type | Speffect rows |
|---:|---|
| `1` | `11,13,15,17` |
| `7` | `4` |
| `8` | `2,3,6` |
| `9` | `0,1,5,7,8,9,10,16,18,19` |
| `11` | `20,22,25,27` |
| `12` | `12,14,21` |
| `13` | `23,24` |
| `14` | `26` |
| `15` | `28,29,30,31,32,33` |

Normal P7 `effect.mid` special chunks use only:

| AH type | Skills using it through `effect.mid` special chunks |
|---:|---|
| `1` | `4,5,24,25,35,44,45,51,57,58,61,62,64,65,67,68` |
| `7` | `25,34,35,41,43,47` |
| `8` | `12,23,26,37` |
| `9` | `4,5,11,15,17,21,27,32,37,38,42,44,45,48,50,51,52,53,54,56,58,62,64,65,68` |
| `12` | `2,8,55,57` |

No normal P7 `effect.mid` special chunk uses AH types `11,13,14,15`; those are currently state1 `L` species overlays.

## Current Rebuild Mapping

| System | Rebuild files | Current status | Notes |
|---|---|---|---|
| P7 actor action `u` | `VqsvBattleRuntime.P7ActorAnimation`, `VqsvBattleRenderer.drawP7ActorEffect` | PORTED/PARTIAL | Frame triggers `[4]/[5]/[6]` exist; not a byte-perfect `ah` object. |
| P7 `H` AH type `9` | `prepareP7SpecialEffect`, `tickP7SpecialEffect`, `drawP7SpecialEffect` | PORTED/PARTIAL | Skill 45 chunk0, skill15 chunk, and many Phase 9 smokes use this. |
| P7 `H` AH type `1` | same | PORTED/PARTIAL | Skill 45 chunk1 / texture overlay path exists. |
| P7 `H` AH type `7` | normal P7 | MISSING | Renderer does not support it for P7; skills `25,34,35,41,43,47`. |
| P7 `H` AH type `8` | normal P7 | MISSING | Renderer supports type8 in active queue path, but normal P7 `showSpecial` gate only allows `1/9`. |
| P7 `H` AH type `12` | normal P7 | MISSING | Renderer supports type12 in active queue path, but normal P7 `showSpecial` gate only allows `1/9`. |
| P12/P13 `H` type `1` | active queue runtime + renderer | PORTED/PARTIAL | Reachable active queue type. |
| P12/P13 `H` type `8` | active queue runtime + renderer | PORTED/PARTIAL | Debuff id 2 smoke exists. |
| P12/P13 `H` type `9` | active queue runtime + renderer | PORTED/PARTIAL | Debuff id 0 / buff id 3 etc. |
| P12/P13 `H` type `12` | active queue runtime + renderer | PORTED/PARTIAL | Debuff id 1 smoke exists. |
| State1 `L` type `11` | `syncP7LEffectRenderState`, `drawState1LEffect` | PORTED/PARTIAL | Species 0/75/92 smokes exist. |
| State1 `L` type `12` | same | PORTED/PARTIAL | Species 87 smoke exists. |
| State1 `L` type `13` | same | PORTED/PARTIAL | Species 62/97/98 smokes exist. |
| State1 `L` type `14` | same | PORTED/PARTIAL | Species 91 smoke exists. |
| State1 `L` type `15` | same | PORTED/PARTIAL | Species 10 smoke exists. |
| Death AH type `16` | P7 death renderer | PORTED/PARTIAL | Source-shaped; pixel compare pending. |
| Generic AH renderer | none | PENDING | Rebuild has targeted renderers, not a full `ah.java` interpreter. |

## Body Overlay Gap Matrix

| Gap | Source-backed candidate | Why first / later | Proposed next slice |
|---|---|---|---|
| Normal P7 AH type `12` | skill `55`, `speffect 12`; skill `2/8/57` also use type12 | Renderer type12 already exists for active queue, so risk is lower | PORTED/PARTIAL in Phase 10-B1 for skill55 |
| Normal P7 AH type `8` | skill `12` chunk1, `speffect 6`; skills `23,26,37` also reach type8-shaped rows through later chunks/candidates | Renderer type8 already exists for active queue, but P7 side/timing needed normal P7 verification | PORTED/PARTIAL in Phase 10-B2 for skill12 |
| Normal P7 AH type `7` | skill `34`, `speffect 4`; skills `25/35` also use `speffect 4` | Source type scales/toggles an actor clone | PORTED/PARTIAL in Phase 10-B3 for skill34 |
| P7 type `1/9` broader coverage | many skills | Renderer exists, but not every skill visual has dedicated smoke | Add smoke-only coverage after missing types are handled |
| Full Java ME color/alpha parity | `l.a`, `l.b`, `drawRGB` | Requires pixel compare or exact transform port | Later, not this slice |
| Generic `ah` interpreter | all AH branches | Big refactor; current targeted renderers are safer | Later, only if repeated targeted ports become messy |

## Recommended Next Code Slice

Phase 10-B1: normal P7 AH type `12` for skill `55`.

Reason:

- Source proves normal P7 can call AH type `12`.
- Rebuild renderer already has type12 logic for P12/P13 active queue.
- Current normal P7 gate only allows type `1/9`, so type12 is hidden even when source calls it.
- Skill `55` is a clean candidate:
  - `effect.mid[55] = [0,1,12,0,-1,-1,0]`
  - `speffect[12]` is AH type `12`
  - target side (`chunk[0] == 0`)

Code scope should be small:

1. Update normal P7 special support gate to include type `12`.
2. Ensure `p7CurrentEffectDuration()` and scene fields use the same type12 duration shape already used by active queue.
3. Add smoke:
   - `battle_phase10b_p7_type12_skill55_start.png`
   - `battle_phase10b_p7_type12_skill55_overlay.png`
   - `battle_phase10b_p7_type12_skill55_after.png`
4. Regression:
   - build
   - `com.vqsv.rebuild.Main --check`
   - `VqsvBattleDamageFormulaCheck`
   - mojibake scan
   - Phase 10-A status icon smokes
   - route Sophie/Bunny/Elder

## Honest Classification

| Item | Status | Reason |
|---|---|---|
| Source channels `u/L/H` | VERIFIED | Direct source read from `game.b`, `game.d`, `ah.java` |
| `effect.mid` special AH type scan | VERIFIED | Decoded tables scanned |
| P7 type `1/9` | PORTED/PARTIAL | Existing runtime/renderer/smoke, no MIDP pixel parity |
| P7 type `12` | PORTED/PARTIAL | Normal P7 now exposes AH type12 for skill55; targeted renderer only, no MIDP pixel compare |
| P7 type `8` | PORTED/PARTIAL | Normal P7 now exposes AH type8 for skill12 chunk1; targeted renderer only, no MIDP pixel compare |
| P7 type `7` | PORTED/PARTIAL | Normal P7 now exposes AH type7 for skill34; targeted renderer only, no MIDP pixel compare |
| P12/P13 type `1/8/9/12` | PORTED/PARTIAL | Source-reachable active queue visual gate covered |
| State1 L type `11/12/13/14/15` | PORTED/PARTIAL | Species smoke coverage exists |
| Generic AH renderer | PENDING | Targeted renderers only |
| Exact `l.a/l.b/drawRGB` Java ME parity | PENDING | Not pixel-compared against MIDP |

## Next Roadmap Step

Phase 10-B1 result: normal P7 AH type `12` for skill `55` is PORTED/PARTIAL.

Source-backed facts:

- `effect.mid[55] = [0,1,12,0,-1,-1,0]`.
- `speffect.mid[12]` starts with AH type `12`.
- The effect is target-side in normal P7.

Rebuild changes:

- Normal P7 special support gate includes type `12`.
- `p7CurrentEffectDuration()` uses the type12 row duration shape (`row[5]`).
- Scene render state exposes the type12 row to `VqsvBattleRenderer.drawP7SpecialType12(...)`.

Smoke evidence:

- `battle_phase10b_p7_type12_skill55_start.png`
- `battle_phase10b_p7_type12_skill55_overlay.png`
- `battle_phase10b_p7_type12_skill55_after.png`

Phase 10-B2 result: normal P7 AH type `8` for skill `12` is PORTED/PARTIAL.

Source-backed facts:

- `effect.mid[12] = [0,0,21,0,-1,-1,0, 0,1,6,0,-1,-1,0]`.
- Chunk0 is actor action/effect id `21`.
- Chunk1 is target-side special AH: `special=1`, `speffect=6`.
- `speffect.mid[6] = [8,0,10,1,5,1,10,0,0,8,0,-5,10,0,0,8,0,-5,10,0,0]`.
- `ah.java case 8` clones the actor image, optionally transforms `b[1]`, and draws `b[1]` attached to the actor.

Rebuild changes:

- Normal P7 special support gate includes type `8`.
- `p7CurrentEffectDuration()` uses the same type8 duration shape already used by active queue (`row[2]`).
- Scene render state exposes the type8 row to `VqsvBattleRenderer.drawP7SpecialType8(...)`.

Smoke evidence:

- `battle_phase10b_p7_type8_skill12_start.png`
- `battle_phase10b_p7_type8_skill12_overlay.png`
- `battle_phase10b_p7_type8_skill12_after.png`

Next roadmap step after verification: Phase 10-B3, audit/port normal P7 AH type `7` with one source-backed skill such as `25` or `34`.

Note: skills `23`, `26`, and `37` should still receive broader coverage later. This slice intentionally proves one normal P7 type8 route first instead of broadening many skills at once.

Phase 10-B3 result: normal P7 AH type `7` for skill `34` is PORTED/PARTIAL.

Source-backed facts:

- `effect.mid[34] = [0,1,4,0,-1,-1,0]`.
- The chunk is target-side special AH: `special=1`, `speffect=4`.
- `speffect.mid[4] = [7,0,4,2,9,10,11,10]`.
- `game.d.n()` prepends actor snapshot before calling `ah.a(short[])`, so the runtime row becomes:
  `[7, actorX, actorY, actorSpriteId, actorAnimState, actorOrientation, 0,4,2,9,10,11,10]`.
- `ah.java case 7` loads the actor sprite, creates a scaled clone, and draw toggles between scaled and original frame.
- For raw row `speffect[4]`, source-shaped scale is X `9/10`, Y `11/10`, with interval `2`.

Rebuild changes:

- Normal P7 special support gate includes type `7`.
- `p7CurrentEffectDuration()` uses the type7 source-shaped duration (`row[2]`).
- `VqsvBattleRenderer.drawP7SpecialType7(...)` renders a cell-image actor clone scaled by the source row ratios and toggled by the source interval.

Smoke evidence:

- `battle_phase10b_p7_type7_skill34_start.png`
- `battle_phase10b_p7_type7_skill34_overlay.png`
- `battle_phase10b_p7_type7_skill34_after.png`

Next roadmap step after verification: Phase 10-B closeout / broader coverage for remaining normal P7 AH rows, then decide whether to port more AH types (`11/13/14/15` in normal P7 coverage) or move to the next battle roadmap phase.

Do not touch:

- intro/world/panel
- battle command/input flow
- HUD status icons from Phase 10-A
- unrelated AH types in the same slice
