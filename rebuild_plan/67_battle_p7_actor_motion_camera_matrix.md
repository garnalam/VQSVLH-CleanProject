# 67 - Battle P7 Actor Motion / Camera Matrix

Status: AUDITED/PARTIAL. This is a read-only audit; no runtime code was changed.

Scope requested:

- Read `game.d` P7 update.
- Read `game.b.d(byte)` actor animation states.
- Read `game.b.a(Graphics)` draw / effect timing.
- Close what each actor state means: attack / hit / recover / dead.

## Source Files Read

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/ah.java`
- Rebuild comparison only:
  - `rebuild_game/src/main/java/VqsvBattleRuntime.java`
  - `rebuild_game/src/main/java/VqsvBattleRenderer.java`

## High-Level Source Flow

`game.d` state `7` is the original battle resolve / action-animation state.

Entry into P7:

- `game.d` case `7` entry setup is around `d.java:812..851`.
- It positions/shows attacker and target through `S.a(...)` / `S.b(...)`.
- It clears P7 flags:
  - `z = false`
  - `A = false`
- It calls `n()` to start the first effect chunk.
- It computes damage vector `Z = h.b(target)` when attacker and target are on different sides, or when skill/status requires it.
- If current effect chunk owner `O[J*7] == 0`, attacker `h` enters actor state `1`.
- Otherwise attacker stays state `0`.

P7 update:

- Main source block: `d.java:1355..1568`.
- It processes either:
  - attacker/target actor action object `u`; or
  - special AH object `H`; or
  - damage/avoid/text + HP mutation once action/effect gates allow it.
- The P7 logic is not a simple fixed-tick lunge. It advances by:
  - `u.i()` active/running checks,
  - `u.d()` completion checks,
  - frame trigger checks `u.a(frame)`,
  - special effect checks `H.i()` / `H.e()`,
  - source effect row trigger fields `O[J*7+4]`, `O[J*7+5]`, `O[J*7+6]`.

## `effect.mid` / `ao[skill]` Chunk Shape

`game.d.n()` at `d.java:429..467` selects the current skill effect row:

```text
O = ao[h.D]
J = I
```

Each chunk is 7 values:

```text
O[J*7 + 0] owner side
O[J*7 + 1] special flag
O[J*7 + 2] effect id / speffect id
O[J*7 + 3] actor state/param
O[J*7 + 4] frame trigger to advance next chunk
O[J*7 + 5] frame trigger to set actor state
O[J*7 + 6] actor state to set at trigger
```

Source behavior:

- If `O[J*7+1] == 1`, P7 builds `H = new ah(...)` from `speffect.mid`.
- Else, P7 calls `b.a(short effectId, byte state)` on either attacker or target.
- It then increments `I`.

Owner side interpretation from `n()`:

| `O[J*7]` | Source target of effect chunk | Source lines | Notes |
| --- | --- | --- | --- |
| `0` | `h.p`, the current target | `d.java:439..445`, `d.java:461..462` | For AH `H`, coordinates/sprite are target. For actor `u`, calls target `a(effectId,state)`. |
| `1` | `h`, the current attacker | `d.java:446..452`, `d.java:463..464` | For AH `H`, coordinates/sprite are attacker. For actor `u`, calls attacker `a(effectId,state)`. |

## `game.b.d(byte)` Actor State Matrix

Source: `game.b.d(byte)` at `b.java:143..211`.

This method sets the base battle sprite animation state `U`.

| State byte | Source action | Meaning for rebuild | Status |
| ---: | --- | --- | --- |
| `0` | `this.a.a(0, -1, true)` | Idle / normal visible battle sprite. Used to recover after attack/effect. | PORTED |
| `1` | `this.a.a(1, 0, true)` plus species-specific `L` effect setup | Attack/action state. Starts state-1 species `L` effect for species `0,10,91,92,97,98,62,75,87`. | PORTED/PARTIAL |
| `2` | `this.a.a(2, 0, true)` | Hit / hurt / recoil state. Source sets target to state 2 after some actor `u` completion/branch. | PARTIAL |
| `3` | If `game.d.a().b == 0`, clears sprite via `d()`, creates AH type `16` effect and starts it | Dead/down/disappear effect state when HP reaches zero. Base sprite is removed; AH type 16 plays. | AUDITED/PENDING |
| `4` | `this.a.a(4, -1, true)` | Extra animation state. Not enough P7 evidence in this audit to name precisely. | UNKNOWN |

Important: this matrix is only for the battle actor state method `d(byte)`. `game.b` has many other methods and status states; those are outside this P7 actor-motion slice.

## `game.b.a(Graphics)` Draw / Timing

Source: `b.java:224..281`.

Draw order:

1. If `L != null && U == 1`, species-specific `L` effect is started when base sprite animation cursor reaches frame `1` via `this.a.b(1)`.
2. If `L != null && Z == 0`, draw `L` before actor.
3. If battle actor visibility flag `f` is true, draw base actor sprite at `(i,j,n)`.
4. If `L != null && Z == 1`, draw `L` after actor. Only species `10` sets `Z=1` in `d(1)`.
5. If actor action object `u != null`, draw `u` after base actor/L.

Tick/update:

- `game.b.o()` at `b.java:214..221` advances:
  - base actor animation via `a()`;
  - actor action object `u.e()`;
  - species `L.e()`.

## Actor Action Object `u`

Source creation:

- `game.b.a(short effectId, byte state)` at `b.java:119..130`.
- It creates `u = new ah()` and calls `u.a(new short[]{effectId, state, direction})`.
- It anchors `u` at actor `(i,j)`.
- Special coordinate adjustment:
  - if `effectId == 20 && state == 3`, or `effectId == 22 && state == 4`, anchor is moved to `j - spriteBoundsHeight`.

Source update/branch usage in P7:

- `d.java:1356..1391`: attacker-side `h.u` branch.
- `d.java:1392..1441`: target-side `h.p.u` branch.
- `u.a(frame)` checks trigger transitions:
  - `O[J*7+4]`: advance next chunk.
  - `O[J*7+5]`: set base actor state to `O[J*7+6]`.
- `u.d()` is treated as completed.
- `u.i()` is treated as active/running.

Current rebuild status:

- Rebuild has `P7ActorAnimation` and renders a separate actor-effect sprite.
- Rebuild has chunk `[4]` and `[5]/[6]` source-shaped triggers.
- Exact `ah` object behavior for every `effectId/state` combination is not fully ported.
- Therefore actor motion is source-shaped but still PARTIAL.

## Special AH Object `H`

Source creation:

- `game.d.n()` creates `H` for chunks where `O[J*7+1] == 1`.
- Source lines: `d.java:438..460`.

Source update:

- `d.java:1443..1482`.
- `H.a()` starts when:
  - attacker is back/recovered enough (`h.p() == 1 && h.b()`), or
  - source flag `M`, or
  - attacker state is idle `h.p() == 0`.
- While `H` runs:
  - base actor for the owner side is hidden via `.b(false)`.
- When `H` completes (`H.i() && !H.e()`):
  - hidden actor is restored via `.b(true)`.
  - if no actor `u` remains, source either advances next chunk or goes to damage/finish flags.

Current rebuild status:

- AH type `9` and `1` slices are ported for current smoke skills.
- AH `H` start/complete logic is source-shaped.
- Full AH type coverage and exact timing are still PARTIAL.

## Damage / Hit / Recover Flow In P7

The damage part begins after action/effect gates set `z`.

Source block:

- `d.java:1483..1568`.

Behavior:

1. If skill has no direct damage formula (`aq.c[1][h.D][3] == 0`), P7 can finish immediately after repositioning and setting target state `0`.
2. If target is already in state `3`, it can finish.
3. Otherwise, if damage has not yet been applied (`!aE`):
   - computes avoid chance;
   - applies HP damage with `target.k(Z[0])`;
   - creates floating damage text via `a("-" + Z[0], ...)`, or `"Né tránh"` on dodge;
   - updates UI side through `S.a(...)` or `S.b(...)`.
4. It then waits for UI/HP transition helpers `S.a(target,false)` / `S.b(target,false)` plus `V()`.
5. Once finished, calls `U()`, clears `aE`, and lets P7 continue/finish.

Hit/recover states:

- Target state `2` is set from actor `u` branch at `d.java:1397..1399` and later used to gate `z` at `d.java:1430..1434`.
- Target state `0` is used as recovery/idle at `d.java:1389..1390`, `d.java:1426..1428`, `d.java:1493`.
- Dead/down state `3` is set by `game.b.q(...)` / damage/status paths when unit is no longer alive, and by P7 finish logic outside this narrow block.

## Camera / Screen Motion

No direct camera object or camera shake call was found in the audited source slice.

What exists in source:

- Actor base positions `(i,j)`.
- Actor `u` AH overlays/actions.
- UI helper `S.a(...)` / `S.b(...)` for battle-side display / HP transition.
- Base actor visibility toggles during `H`.

What rebuild currently has:

- `VqsvBattleRuntime.syncP7MotionOffsets(...)` applies a source-shaped but not source-proven lunge/recoil:
  - attacker lunge during P7 phase 1;
  - target recoil during P7 phase 2.
- This is APPROX, because it is not yet derived from a specific original `ah u` row or MIDP frame capture.

Conclusion: do not claim source camera parity yet. Next port should replace/justify the approximate offsets by decoding/rendering the relevant `ah u` action rows, or mark each offset explicitly as rebuild feel.

## Rebuild Comparison Matrix

| Feature | Source evidence | Current rebuild | Status |
| --- | --- | --- | --- |
| P7 chunk sequencing | `game.d.n()`, `d.java:429..467`; P7 case `d.java:1355..1482` | `enterP7SourceChunk`, `tickP7SourceEffectSequence` | PORTED/PARTIAL |
| Base actor state 0 idle/recover | `game.b.d(0)`, `b.java:145..147`; P7 sets `d(0)` in several recover branches | `battleP7BaseState* = 0` | PORTED |
| Base actor state 1 attack/action | `game.b.d(1)`, `b.java:149..185`; P7 entry sets attacker state 1 when chunk owner is target-side | `setP7BaseState(attacker, 1)` | PORTED/PARTIAL |
| Species `L` start timing | `game.b.a(Graphics)`, `b.java:224..267`, frame cursor `a.b(1)` | `tickP7LEffect` uses sprite cursor frame 1 | PORTED/PARTIAL |
| Base actor state 2 hit/recoil | `game.b.d(2)`, `b.java:188..190`; P7 target branch sets `d(2)` | Rebuild has recoil offsets but state-2 timing is not fully source-mapped | PARTIAL |
| Base actor state 3 dead/down | `game.b.d(3)`, `b.java:192..205`, AH type 16 | Not fully ported as original down effect | PENDING |
| Base actor state 4 | `game.b.d(4)`, `b.java:207..209` | Not named/used confidently | UNKNOWN |
| Actor action object `u` | `game.b.a(short,byte)`, `b.java:119..130`; P7 checks `u.i/u.d/u.a(frame)` | `P7ActorAnimation` source-shaped wrapper | PARTIAL |
| Special object `H` hide/restore | `d.java:1443..1482` | ported for current AH type slices, not full AH runtime | PARTIAL |
| Damage text timing | `d.java:1505..1535` | rebuild P7 phase 2 fixed duration | PARTIAL |
| Camera/shake | no direct source call found in audited slice | rebuild lunge/recoil arrays are source-shaped approximation | APPROX |

## Next Safe Implementation Slice

Recommended next slice:

1. Pick one direct-damage smoke skill already covered, preferably Elder skill `10`.
2. Decode its `effect.mid` row into chunks and write a tiny per-chunk matrix.
3. For that one skill only, port:
   - source-backed state-2 trigger from `[5]/[6]` if present;
   - target state recovery to state `0`;
   - damage text timing tied to the end of `u`/`H` gates instead of only fixed phase ticks.
4. Keep existing lunge/recoil offsets labeled APPROX until a concrete `ah u` row is decoded and matched.

Do not change intro/world/panel for this work.
