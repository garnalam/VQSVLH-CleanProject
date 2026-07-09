# 149 Battle Phase 8 Room1 Group1 Save Prompt Wrapper Closeout

Status date: 2026-07-09

Status: CLOSEOUT / VERIFIED.

Scope:

- Close the source-shaped room1 group1 save prompt wrapper slice.
- Keep save payload/RMS parity unchanged.
- Keep battle/P7/event VM behavior unchanged.

Source basis:

- `modules/event/decoded/data__event__scene_1.mid.json`
  - scene1 room1 group1:
    - `op15 [1,0,0]`
    - `op56 [0,1]`, strings `"50"`, `"0"`
    - `op46 "Bạn có muốn lưu trữ không?"`
    - `op14`
- `modules/source_code/decoded/decompiled_source_cfr/game/c.java`
  - `case 15`: gate on source event state `3` or `4`
  - `case 56`: actor show/hide/state update
  - `case 46`: save prompt, save-in-progress, event-cell complete, `game.k.k()`, success text
  - `case 14`: complete current event group
- Prior audit:
  - `148_battle_phase8_room1_group1_save_prompt_wrapper_audit.md`

## Implementation

| File | Change | Status |
| --- | --- | --- |
| `rebuild_game/src/main/java/VqsvRoom1Group1SavePromptWrapper.java` | Added a small `Blocking` wrapper around existing save prompt behavior. | `PORTED/PARTIAL` |
| `rebuild_game/src/main/java/Scene1Room0Group0Script.java` | Replaced direct `VqsvSavePromptBlocking` with `VqsvRoom1Group1SavePromptWrapper` at the existing source-shaped call point before room1 group0 `Op13FreeWorldTrigger`. | `PORTED/PARTIAL` |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Save prompt/success smokes now use the wrapper and assert wrapper trace + `[1,1,1]` event-state completion. | `PORTED/PARTIAL` |

## Behavior

Implemented:

- Wrapper traces `op15 [1,0,0]` and current gate state.
- Wrapper traces `op56 [0,1] actor=50 state=0`.
- Wrapper delegates `op46` UI/save behavior to `VqsvSavePromptBlocking`.
- On confirm/save, wrapper marks `[1,1,1]` complete before `VqsvSaveRuntime.save(s)`, matching source `op46` shape where the current event cell is marked before `game.k.k()`.
- If the prompt is skipped, wrapper marks `[1,1,1]` complete when prompt closes, matching the following source `op14`.

Intentionally unchanged:

- `VqsvSaveRuntime` save format remains rebuild route snapshot.
- Full source RMS records remain `PENDING`.
- `Scene.eventIndex` is not mutated.
- `Op13FreeWorldTrigger` remains the next route step.
- `battleBranchTarget`/op47 behavior is unchanged.
- P7 is untouched.
- No generic decoded event VM was added.

## Verification

Passed:

- `build.ps1`
- `com.vqsv.rebuild.Main --check`
- `VqsvBattleDamageFormulaCheck`
- Java mojibake scan

Smoke PNG checkpoints passed:

- `room1_bunny_save_prompt`
- `room1_bunny_save_success`
- `room1_bunny_save_resume_state`
- `boot_title_continue_with_save`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`
- `route_sophie_after_battle_branch`

Smoke output folder:

- `rebuild_game/build/smoke_phase8_save_wrapper/`

`git diff --check` note:

- Not available in the current working folder because this workspace is not a git repository from `rebuild_game`.
- Existing whitespace warnings from earlier plan files were not part of this slice and were not changed.

## Status Matrix

| Area | Status | Note |
| --- | --- | --- |
| room1 group1 source group wrapper | `PORTED/PARTIAL` | Source opcode sequence is represented around current save prompt call point. |
| `op15 [1,0,0]` | `PORTED/PARTIAL` | Traced; current helper checks state `3`, while source accepts `3/4`. |
| `op56 [0,1] actor 50 state 0` | `PORTED/PARTIAL` | Trace-only in this slice; no actor mutation yet. |
| `op46` save prompt | `PORTED/PARTIAL` | Existing prompt/save flow reused. |
| `op14 [1,1,1]` | `PORTED/PARTIAL` | Wrapper marks group1 complete. |
| full source RMS save | `PENDING` | Out of scope. |
| generic decoded event VM | `PENDING` | Out of scope. |

## Next Phase 8 Step

Next roadmap-consistent work:

```text
Audit source state 10 / free-world event resume mechanics before attempting a
generic decoded event VM.
```

Suggested next doc:

```text
150_battle_phase8_state10_freeworld_resume_audit.md
```

Scope for `150`:

- `game.i` state `10` return to `game.k`.
- How event script resumes after battle/save/task UI closes.
- How current rebuild `Blocking` continuation and `Op13FreeWorldTrigger` differ
  from source.
- Whether a tiny trace/assert wrapper can reduce drift without creating a full
  decoded event VM.
