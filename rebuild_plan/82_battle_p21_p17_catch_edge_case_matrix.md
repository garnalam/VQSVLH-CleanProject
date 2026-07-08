# 82 Battle P21/P17 Catch Edge Case Matrix

Status: SOURCE AUDIT / CURRENT DELTA MATRIX. No rebuild code changed in this
step.

Scope: current Phase 5 target from `rebuild_plan/81_new_chat_hand_off_battle_engine_current.md`:
P21/P17 catch edge cases. This document does not port code. It proves the
source flow, identifies current rebuild deltas, and chooses safe next slices.

Rules for this audit:

- Source first, then logic, then UI/effect.
- Do not guess behavior, sprites, UI, effects, timing, or event links.
- Do not touch intro/world/panel/scene scripts.
- Do not open the live client. Use smoke PNG after code only.
- Mark every piece as `PORTED`, `PARTIAL`, `APPROX`, `STUB`, `PENDING`,
  `UNKNOWN`, or `DAMAGED`.
- New docs/code use project-relative paths only.

## Read Inputs

Controlling roadmap/docs:

- `rebuild_plan/battle_engine_master_roadmap_progress.md`
- `rebuild_plan/81_new_chat_hand_off_battle_engine_current.md`
- `rebuild_plan/79_battle_item_pet_catch_state_matrix.md`
- `rebuild_plan/51_battle_p21_p17_catch_flow_audit.md`
- `rebuild_plan/74_battle_game_d_state_full_matrix.md`

Source/rebuild files checked for this matrix:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
- `modules/script/decoded/data__script__db.mid.json`
- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleTables.java`
- `rebuild_game/src/main/java/VqsvText.java`

## Source Flow Summary

P21 and P17 must stay separate:

```text
P20 command Catch
-> P21 catch list
   -> game.d.a(21): target enemy d[0], call game.h.ah()
   -> game.h.ah(): open choice.ui and list ball rows from q.K
   -> game.h.ai(): navigate, confirm, no-count warning, back
-> P17 catch result
   -> game.d.a(17): sprite 269, q=0, chance, U/V force-fail gate
   -> game.d.b() case 17: q0..q4 animation/result/storage/fail
```

Source evidence:

- `game.d.a(byte)` case `21`: sets `h.p = d[0]`, calls `S.ah()`.
- `game.h.ah()` opens `/data/ui/choice.ui`, writes title/action widgets, fills
  rows from `q.K`.
- `game.h.ai()` confirms count, sets `game.d.l`, calls `o.m()`, consumes a
  ball, then enters state `17`.
- `game.d.a(byte)` case `17`: creates/uses sprite `269`, positions it at
  `h.i/h.j`, calls `e(0)`, computes chance with `b(itemId)`, and applies
  `U == 0 && V == 5` force-fail.
- `game.g.y()` returns storage target: `0` bag, `1` bank, `2` full/release.

## P21 State/UI Matrix

| Step | Source method | UI/resource | Input | Source side effect | Source next | Current rebuild | Status |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Enter P21 | `game.d.a(21)` | battle scene + UI facade | none | `h.p = d[0]`, then `S.ah()` | P21 wait | `prepareCatchMenu()` then `P21_CATCH_LIST` | PORTED/PARTIAL |
| Build list | `game.h.ah()` | `/data/ui/choice.ui`, sprite `258` icons | none | title widget `8`, chance header `9`, action `5`; rows from `q.K`; icon `aq.c[4][id][1]`; name `aq.c[4][id][0]`; chance `game.d.b(id)`; count text widget `53` | P21 wait | Source-shaped `setMenu(...)`, rows from `sourceBagItems` with behavior `0` | PORTED/PARTIAL |
| Move list | `game.h.ai()` | `/data/ui/choice.ui` | up/down | list cursor moves, `bn()` refreshes count text | P21 wait | `handleMenuInput(...)` | PORTED/PARTIAL |
| Confirm with count | `game.h.ai()` | `/data/ui/choice.ui` | confirm | `game.d.l = itemId`; `o.m()` tutorial hook; `q.d(item,1,0)` consume; enter `P17`; close `choice.ui` | P17 | `tickCatchList()` removes item and calls `initCatchResult()` | PORTED/PARTIAL |
| Confirm missing count | `game.h.ai()` | `/data/ui/msgwarm.ui` | confirm | warning text, local `f = 1`, no consume | P21 warning loop | `tickCatchList()` warns with `NO_BALLS` | PARTIAL |
| Confirm warning for item `0` | `game.h.ai()` | `/data/ui/msgwarm.ui` -> P101 | confirm after warning | if selected row item id is `0`, close `choice.ui`, enter state `101` | P101 | no P101 state; warning returns P21 | PENDING |
| Back | `game.h.ai()` | close `/data/ui/choice.ui` | back | no consume | P20 | back enters command state | PORTED/PARTIAL |

## Ball Item Matrix

Decoded from `modules/script/decoded/data__script__db.mid.json`, group `4`.
Only rows with `aq.c[4][id][5] == 0` are catch balls in the current source path.

| Item id | Raw row | Icon `[1]` | Behavior `[5]` | Chance param `[6]` | Source meaning in catch |
| ---: | --- | ---: | ---: | ---: | --- |
| `0` | `[261,25,278,2,2,0,9999]` | `25` | `0` | `9999` | Special/perfect tutorial ball; `game.d.b(0)` returns `100` before row math. |
| `1` | `[262,26,279,50,0,0,100]` | `26` | `0` | `100` | Normal catch ball candidate. |
| `2` | `[263,27,280,200,0,0,140]` | `27` | `0` | `140` | Stronger catch ball candidate. |
| `3` | `[264,28,281,1,1,0,200]` | `28` | `0` | `200` | Strongest catch ball candidate in current table. |

Current rebuild uses `BattleItemRow.behavior` and `BattleItemRow.paramA`, which
map to source columns `[5]` and `[6]`.

## Source Catch Chance

Source `game.d.b(int itemId)`:

```text
if itemId == 0:
    return 100

statusIndex = 0
if target.m(1): statusIndex = 1
if target.m(2): statusIndex = 2
if target.m(10): statusIndex = 3
if attacker.f(11): statusIndex = 4

base = 1
if targetHP <= maxHP * 15 / 100: base = 85
else if targetHP <= maxHP * 50 / 100: base = 45
else if targetHP <= maxHP: base = 20

base *= aq.c[4][itemId][6] / 100
base *= qualityMultiplier[target.c[0] - 1] / 100
base *= statusMultiplier[statusIndex] / 10
if attacker.f(11):
    base *= (100 + aq.c[3][11][5]) / 100
base *= relationMultiplier[aq.c[0][targetSpecies][22]] / 1000
if target.level >= 20 and base >= cap[itemId]:
    base = cap[itemId]
clamp 1..100
```

Rebuild `catchChance(itemId)` currently mirrors the HP threshold, item param,
quality/nature, relation class, level cap, and clamp. It does not fully prove
all status/global context parity.

Classification:

| Concern | Status | Note |
| --- | --- | --- |
| Item `0` returns 100 | PORTED | Rebuild returns `100`. |
| HP threshold math | PORTED/PARTIAL | Same visible thresholds; requires tests per boundary. |
| Ball param `[6]` | PORTED | Rebuild uses `BattleItemRow.paramA`. |
| Target quality/nature multiplier | PORTED/PARTIAL | Rebuild uses `enemy.nature`; source uses `target.c[0] - 1`. Need confirm field parity for every source payload. |
| Status multiplier | PARTIAL | Rebuild relation/status context is not fully source-global. |
| Attacker status `11` bonus | PARTIAL/PENDING | Needs direct audit of current runtime field availability. |
| Relation/catch class `[22]` | PORTED/PARTIAL | Rebuild has relation class, but broad save/global parity remains partial. |
| Level cap | PORTED/PARTIAL | Rebuild applies caps by item id; needs focused tests for item ids 1..3. |

## P17 Lifecycle Matrix

| Phase | Source | Current rebuild | Status | Gap |
| --- | --- | --- | --- | --- |
| Enter | `game.d.a(17)` creates/uses `f aj`, loads sprite `269`, positions at enemy, calls `e(0)`, computes chance and `ak`. | `initCatchResult()` loads `SpriteAnim 269`, phase `0`, chance and `catchCaught`. | PORTED/PARTIAL | Success decision differs from source random/force-fail. |
| q0 | If `q == 0 && aj.b()`, call `e(1)`. | `catchPhase == 0 && animEnded` -> phase `1`. | PORTED/PARTIAL | Depends on rebuild sprite cursor equivalence. |
| q1 | If `q == 1 && aj.b()` and `!H.e()`, call `e(2)`. | phase `1`, q1 type8 effect steps, waits effect source-like. | PORTED/PARTIAL | Java2D effect is source-shaped, not MIDP pixel-perfect. |
| q2 | If `q == 2 && aj.b()`, branch to q3 on caught or q4 on fail. | phase `2` -> phase `3/4` from `catchCaught`. | PORTED/PARTIAL | `catchCaught` is deterministic/shortcut. |
| q3 success | If `q == 3 && aj.b()`, call `game.g.y()`: bag, bank, or full release messages and storage. | phase `3` -> `applyCatchStorage()` then `P8_WIN`. | PORTED/PARTIAL | Payload parity with `game.b.P()` still partial. |
| q4 fail | If `q == 4 && aj.b() && !H.e()`, clear `H`, restore enemy, stop `aj`; if not caught, advance actor and `P1`. | phase `4` waits q4 type8 effect, clears visuals, marks player action, enters P1. | PORTED/PARTIAL | Exact `H.e()` / draw ordering / enemy restore timing pending. |
| Draw | Source P17 draws battle scene, then if `H.c()` draws `H`, then catch sprite; otherwise just catch sprite. | `VqsvBattleRenderer.drawCatchAnimation(...)` draws type8 effect and sprite. | PORTED/PARTIAL | Exact `H` ordering and MIDP `l/e` pipeline not pixel-perfect. |

## Tutorial Gating Matrix

Source tutorial methods are `game.d.l()` and `game.d.m()`.

Known source facts:

| Source state | Source behavior | Rebuild concern |
| --- | --- | --- |
| `U == 0, V == 0` in `l()` | If enemy HP is at or below 50%, set tutorial flags, increment `V`, show wounded Bunny/catch prompt. | Rebuild has Bunny weak gate after P7, source-shaped but not exact full `U/V`. |
| `U == 0, V == 1` in `l()` | Wait UI, set prompt state, guide pressing key 5. | Rebuild does not model full prompt state sequence. |
| `U == 0, V == 3` in `l()` | Guide selecting ball. | Rebuild does not model full source UI guide flags. |
| `U == 0, V == 5` in `l()` | Shows first catch failed prompt. | Source P17 also force-fails when `U == 0 && V == 5`. |
| `U == 0, V == 6` in `l()` | After UI wait, enters P21 again. | Rebuild currently uses simpler Bunny catch route. |
| `U == 0, V == 8` in `l()` | Clears tutorial flags and resets `U/V`. | Rebuild route hooks are approximate. |
| `U == 2` in `l()` | Guides all-hit ball under a separate tutorial mode and can reset when a task flag is set. | Not audited into rebuild. |
| `U == 5` in `l()/m()` | Another all-hit-ball prompt/advance path. | Not audited into rebuild. |
| `m()` | Advances tutorial `V` on confirmed actions; for `U==0`, advances at `V==2/4/7`; for `U==5`, advances and clears flags. | Rebuild confirm path does not fully own `U/V`. |

Current rebuild delta:

- `prepareCatchMenu()` auto-seeds item `0` for Bunny when no catch ball exists.
- `initCatchResult()` forces catch success when `isBunnyCaptureBattle()` is true.
- Source instead has guided stages, a first forced failure at `U==0 && V==5`,
  and a later route back to P21.

Classification: tutorial gating is `APPROX/PENDING`.

## Storage Matrix

Source `game.g.y()`:

```text
if active bag count A < 6: return 0
if bank O.size() < 100: return 1
return 2
```

Source P17 q3:

| `game.g.y()` | Source side effect | Current rebuild | Status |
| ---: | --- | --- | --- |
| `0` | message success; `game.d.p.a(((b)h.p).P())` add to active bag | add `SourcePetState` to `sourcePets` | PORTED/PARTIAL |
| `1` | message success; `game.d.p.b(((b)h.p).P())` add to bank | add `SourcePetState` to `sourcePetBank` | PORTED/PARTIAL |
| `2` | message no space/release | no add, release message | PORTED/PARTIAL |

Remaining gap: `SourcePetState.sourcePayload` is `game.b.P()`-shaped, but full
`game.g` save/global parity remains `PARTIAL`.

## Current Delta Summary

| Area | Current rebuild status | Delta to source | Suggested action |
| --- | --- | --- | --- |
| P21 entry/list | PORTED/PARTIAL | Source-shaped, not generic `game.h` widget runtime. | Keep; smoke only. |
| P21 no-ball warning | PARTIAL | Warning exists, but source local warning flag and P101 item `0` path are not ported. | Audit/port warning return first; keep P101 pending unless fully audited. |
| Bunny tutorial auto-seed | APPROX | Rebuild auto-seeds item `0` whenever Bunny has no ball; source has `U/V` guided states and first fail path. | Gate or remove only after source route proof. |
| Catch success decision | PORTED/PARTIAL | Rebuild uses source-shaped `roll 0..99 < chance`; Bunny tutorial force-fail keeps the source `U==0,V==5` edge. | Exact RNG seed stream parity remains partial. |
| Chance formula | PORTED/PARTIAL | HP, ball param, quality, target statuses `1/2/10`, attacker form/status `11`, relation class, level cap, clamp are ported. | P101/SMS purchase context and pixel parity remain outside this formula slice. |
| P17 q0..q4 | PORTED/PARTIAL | Source-shaped; pixel/timing parity pending. | Do not polish before logic gaps. |
| Storage routing | PORTED/PARTIAL | Bag/bank/full route exists; payload parity partial. | Keep; maybe add payload audit later. |
| P101 purchase/SMS | PENDING | Source state exists, but side effects not audited enough for battle slice. | Document as pending, do not invent behavior. |

## Recommended Slice Order From This Matrix

### Slice 1: no-ball / missing count path audit-safe fix

Goal:

- Match source warning text/return shape as closely as possible.
- Keep P101 purchase/SMS `PENDING` unless fully audited.
- Add focused smoke for no-ball warning and return to P21.

Why first:

- Smallest user-visible edge case.
- Does not require changing random/success behavior.
- Does not require touching P17 effect renderer.

### Slice 2: tutorial gating audit

Goal:

- Prove whether current Bunny auto-seed item `0` is too broad.
- Map current story event flags to source `U/V` enough to gate the behavior.
- Preserve route smoke before changing behavior.

Do not code this until source event/runtime link is clear.

### Slice 3: catch success decision parity

Goal:

- Replace deterministic shortcut only when deterministic smoke strategy exists.
- Preserve special item `0`.
- Represent `U==0,V==5` first forced fail if current runtime has enough tutorial state.

Risk:

- Affects Bunny route result and P17 fail/success branches.
- Must run route regressions.

### Slice 4: chance/storage boundary checks

Goal:

- Add tests/smokes for item ids `1..3`, HP thresholds, level cap, and bag/bank/full.
- Do not invent payload fields.

### Slice 5: P17 lifecycle tightening

Goal:

- Only after logic gaps are stable, tighten q phase timing and H/ah ordering.
- No pixel-perfect claim without original-vs-rebuild compare.

## Required Smoke/Regression Plan After Code Slices

Focused catch smokes from `81`:

```text
battle_bunny_catch_p21
battle_bunny_catch_p17_anim_or_result
battle_bunny_after_catch_route
battle_catch_fail_or_warning
battle_catch_storage_bank
battle_catch_storage_full_release
```

If touching shared menu/input code, also run P5 regressions:

```text
battle_elder_pet_p5
battle_p5_click_reserve_success
battle_p5_current_warning
battle_p5_dead_warning
battle_p5_forced_replacement_success
battle_p5_switch_transition
battle_p5_switch_transition_mid
```

If touching shared item/menu handling, run relevant P16 item smoke too.

Route regressions if catch result/branch can be affected:

```text
route_sophie_after_battle_branch
route_bunny_after_battle_task
route_elder_after_battle_reward_state
```

Required checks after Java code:

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java "-Dvqsv.modules=..\modules" -jar .\build\libs\vqsv-rebuild-skeleton.jar --check
java -cp .\build\classes VqsvBattleDamageFormulaCheck
mojibake scan for Java source
focused smoke PNG
```

## Safety Statement

- Files likely to edit after this audit:
  - `rebuild_game/src/main/java/VqsvBattleRuntime.java`
  - `rebuild_game/src/main/java/VqsvSmokeHarness.java`
  - `rebuild_game/src/main/java/VqsvText.java` only if a source warning string
    is missing and must be added Unicode-safe.
- Files not to touch unless P21/P17 source proof requires shared helpers:
  - P5/P16/P7 logic.
  - intro/world/panel/scene scripts.
  - unrelated refactor files.
- Purchase/SMS/P101 remains `PENDING` unless side effects are fully audited.
- Missing source behavior must not be replaced with guessed visual behavior.
- P17 effect renderer remains `PORTED/PARTIAL`; no pixel-perfect claim.

## Conclusion

Current catch flow is a good source-shaped base, but the edge cases are not
closed. The safest next code slice is the no-ball/missing-count warning path,
while documenting P101 as pending. Tutorial gating and success decision are
higher-risk because they affect the Bunny route and require exact `U/V` state
mapping or an explicit deterministic test strategy.
