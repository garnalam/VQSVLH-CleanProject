# 83 Battle Bunny Tutorial Gating Audit

Status: SOURCE AUDIT + FOLLOW-UP SLICE TRACKING.

Scope: Bunny capture tutorial gating for Phase 5 P21/P17 catch edge cases.
This audit proves the source `U/V` flow before any code slice touches tutorial
success/fail behavior.

Rules:

- Source first; no guessed Bunny tutorial behavior.
- Do not change P17 success/gating until the source chain is proven.
- Keep P101/SMS purchase path `PENDING`.
- Keep P17 effect renderer `PORTED/PARTIAL`; no pixel-perfect claim.

## Read Inputs

- `modules/source_code/decoded/decompiled_source_cfr/an.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
- `rebuild_plan/51_battle_p21_p17_catch_flow_audit.md`
- `rebuild_plan/82_battle_p21_p17_catch_edge_case_matrix.md`
- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

## Source Global Tutorial State

Source tutorial globals live in `an`:

| Field/method | Meaning | Status |
| --- | --- | --- |
| `an.U` | active tutorial id; default `-1` means no guide gating | PORTED/PENDING in rebuild only as trace/approx |
| `an.V` | stage inside active tutorial; default `0` | PENDING in rebuild |
| `an.I()` | input allowed if no tutorial or `h[U][0] == 1` | PENDING |
| `an.J()` | back allowed if no tutorial or `h[U][0] == 2` | PENDING |
| `an.b(index, mode)` | tutorial selection gate; can block wrong UI index | PENDING |
| `an.c(slot, value)` | updates guide gate row `h[U]` | PENDING |

For Bunny battle setup, rebuild already traces source op66 as `an.U=0`, but it
does not own real `U/V` progression yet.

## Source Bunny `U == 0` Flow

Source method: `game.d.l()`.

| Stage | Source condition | Source action | Next state | Rebuild status |
| --- | --- | --- | --- | --- |
| `U=0,V=0` | enemy exists and enemy HP <= 50% | `game.d.c(0,1)`, `V++`, show wounded Bunny prompt telling player to use `phong an cau` | `V=1` | APPROX: rebuild shows `BUNNY_WEAK` after HP <= 50%, but not real gate row |
| `U=0,V=1` | prompt/UI finished: `S.ay()` | `V++`, set `S.a=1`, `c(2,1)`, `c(1,1)`, `S.ag()`, prompt press key 5 | `V=2` | PENDING |
| `U=0,V=2` | player confirms command/list action | `game.d.m()` increments `V` | `V=3` | PENDING/PARTIAL |
| `U=0,V=3` | next `l()` tick | `V++`, `c(2,0)`, `c(1,1)`, prompt select `phong an cau` | `V=4` | PENDING |
| `U=0,V=4` | player confirms ball in P21 | `game.d.m()` increments `V` before P17 | `V=5` | PENDING/PARTIAL |
| `U=0,V=5` | P17 enter and later `l()` | P17 force-fails catch; after fail prompt says try better `Tat trung cau` | `V=6` | NOT PORTED: rebuild currently forces Bunny catch success |
| `U=0,V=6` | prompt/UI finished: `S.ay()` | `c(1,0)`, `S.a=1`, `S.ag()`, set `S.b=0`, `V++`, enter P21 again | `V=7` | NOT PORTED |
| `U=0,V=7` | player confirms second ball action | `game.d.m()` increments `V` | `V=8` | PENDING |
| `U=0,V=8` | next `l()` tick after success path | `c(1,-1)`, `c(0,0)`, reset `U=-1,V=0` | tutorial done | PENDING |

## Source P21/P17 Links

Source `game.h.ai()`:

- P21 confirm checks selected row from `q.K`.
- If count is missing, opens `/data/ui/msgwarm.ui` and sets local warning flag
  `S.f=1`.
- If count exists:
  - sets `game.d.l = itemId`
  - calls `o.m()` tutorial hook
  - consumes one ball with `q.d(item,1,0)`
  - enters P17

Source `game.d.a(17)`:

- computes chance via `b(game.d.l)`
- sets `ak = ae.a(100) < chance`
- then source override:

```text
if U == 0 && V == 5:
    ak = false
```

This is the first Bunny forced-fail edge.

## Ball List Proof

Source `game.g` initializes catch-ball vector `K` with `[0,0,1]`.

Room0 group0 reward path adds:

| Event | Source/rebuild op | Result |
| --- | --- | --- |
| 17 | `op17Item(0,0,1)` | `Tat Trung Cau x1` |
| 18 | `op17Item(0,1,2)` | `Phong an cau x2` |
| 19 | `op17Item(0,4,5)` | sandwich, non-ball item |

Therefore, after the story reward path and before Bunny P21, the catch list is
expected to contain ball ids `[0,1]`.

This is now smoke-locked in `VqsvSmokeHarness`:

- `battle_bunny_catch_p21` seeds room0 group0 reward state.
- It asserts `battleMenuIds == [0,1]`.
- It does not infer from visible text alone.

## Current Rebuild Delta

| Area | Current rebuild behavior | Source behavior | Status |
| --- | --- | --- | --- |
| Bunny battle setup | traces op66 `an.U=0` | actual `an.U=0`, `V=0` tutorial state | APPROX/PENDING |
| Weak Bunny prompt | after player damage leaves HP <= 50%, sets command index to Catch and shows `BUNNY_WEAK` | `U=0,V=0->1`, guide flags, prompt text | PORTED/PARTIAL |
| P21 ball list | source-shaped rows from `sourceBagItems`; Bunny route smoke now verifies `[0,1]` | `q.K` rows `[0,1]` after rewards | PORTED/PARTIAL |
| First P21 confirm | rebuild confirms current selected row and consumes it | source calls `m()` and should advance `V=4->5` before P17 | PARTIAL |
| First Bunny catch result | rebuild currently `itemId == 0 || isBunnyCaptureBattle() || chance>=50` | source random chance, but force-fail when `U=0,V=5` | APPROX/WRONG FOR TUTORIAL |
| Retry prompt | not modeled | source `U=0,V=5->6` says try `Tat Trung Cau` | PENDING |
| Re-enter P21 | not modeled as tutorial stage | source `U=0,V=6`, after prompt done, sets cursor `S.b=0` and enters P21 | PENDING |
| Second confirm | rebuild route can catch immediately | source `m()` advances `V=7->8`; item 0 chance is 100 unless other source branch blocks | PARTIAL |
| Tutorial cleanup | not modeled | source `U=0,V=8` resets `U=-1,V=0` | PENDING |

## What Not To Code Yet

Do not claim or port these until a small state model is chosen:

- full `an.h[U]` guide-row behavior
- full prompt/UI wait equivalence for `S.ay()`, `S.ag()`, `S.c(...)`
- exact click/input blocking from `an.b(index, mode)`
- random catch parity with `ae.a(100)`
- P101/SMS purchase path

## Safe Candidate Slice

Smallest source-backed next code slice:

1. Add Bunny tutorial state fields inside `SourceBattleRuntime`, only for
   `isBunnyCaptureBattle()`.
2. Model only the proven P21/P17 edge:
   - first P21 confirm with item `1` advances tutorial to force-fail stage
   - P17 first catch fails when tutorial force-fail is active
   - after q4 fail, route back to P21 with selected menu index `0`
3. Keep guide UI text/locks `APPROX`; do not implement full `an.h` yet.
4. Add focused PNG smokes:
   - Bunny first P17 forced fail
   - Bunny retry P21 selected item id `0`
   - Bunny final catch route still result `-1`

Risk:

- This changes Bunny route behavior from one catch to two catch attempts.
- Must run all required catch and route regressions.

## Required Verification After Code

- `powershell -ExecutionPolicy Bypass -File .\build.ps1`
- `java "-Dvqsv.modules=..\modules" -jar .\build\libs\vqsv-rebuild-skeleton.jar --check`
- `java -cp .\build\classes VqsvBattleDamageFormulaCheck`
- mojibake scan
- focused Bunny tutorial PNG smoke
- catch regressions:
  - `battle_bunny_catch_p21`
  - `battle_bunny_catch_p17_anim_or_result`
  - `battle_bunny_after_catch_route`
  - `battle_catch_fail_or_warning`
  - `battle_catch_missing_count_warning`
  - `battle_catch_storage_bank`
  - `battle_catch_storage_full_release`
- route regressions:
  - `route_sophie_after_battle_branch`
  - `route_bunny_after_battle_task`
  - `route_elder_after_battle_reward_state`

## Conclusion

The source Bunny tutorial is not a simple guaranteed capture. It is a guided
two-attempt flow:

1. wound Bunny
2. guide the player to choose `Phong an cau`
3. force the first P17 catch to fail at `U=0,V=5`
4. tell the player to use `Tat Trung Cau`
5. re-enter P21 with cursor on item `0`
6. second capture path succeeds and tutorial state resets

Rebuild currently skips this by forcing Bunny capture success. The next code
slice should be small and should only model the proven first-fail/retry-P21
edge, not the full generic tutorial UI runtime.

## Implemented Slice 2026-07-07

Runtime files changed after this audit:

- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Implemented:

- Bunny weak prompt now marks a local tutorial first-catch pending state.
- First guided Bunny P21 selects item `1` (`Phong an cau`) when the source route
  reward list contains `[0,1]`.
- Confirming item `1` marks source-shaped `U=0,V=4->5`.
- First Bunny P17 catch is force-failed while that marker is active.
- After q4 fail, runtime shows a source-shaped `taskTip.ui` prompt telling the
  player to use `Tat Trung Cau`.
- After the prompt is confirmed, runtime re-enters P21 and selects item `0`
  (`Tat Trung Cau`), matching source `U=0,V=6` cursor reset.
- Confirming item `0` marks source-shaped retry `U=0,V=7->8`; final capture
  route remains source-shaped but still not a full generic `an.h` tutorial UI
  runtime.

Still pending:

- Full `an.h[U]` input/click gating.
- Exact generic `S.ay()`, `S.ag()`, `S.c(...)` prompt lifecycle beyond the
  Bunny retry taskTip slice.
- Generic random catch parity with `ae.a(100)`.
- P101/SMS purchase path.
