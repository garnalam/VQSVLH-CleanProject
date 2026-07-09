# 148 Battle Phase 8 Room1 Group1 Save Prompt Wrapper Audit

Status date: 2026-07-09

Status: SOURCE-FIRST AUDIT ONLY / NO CODE CHANGE YET.

Purpose:

- Audit whether scene1 room1 group1 save prompt can be represented as its own
  source-shaped event group wrapper.
- Keep current save payload and route behavior unchanged.
- Do not mutate `Scene.eventIndex`.
- Do not rewrite RMS save parity.
- Do not reopen P7.
- Do not build a generic decoded event VM.

This follows:

- `143_battle_phase8_entry_exit_event_integration_audit.md`
- `144_battle_phase8_op47_consumer_wrapper_audit.md`
- `145_battle_phase8_sophie_descriptor_op47_audit.md`
- `146_battle_phase8_post_op47_downstream_consumer_audit.md`
- `147_new_dev_chat_handoff_battle_phase8_event_integration.md`

## Source Files Read

Primary source/data:

- `modules/event/decoded/data__event__scene_1.mid.json`
- `modules/source_code/decoded/decompiled_source_cfr/game/c.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java` through
  prior save audit `98_save_resume_bunny_task_source_audit.md`

Current rebuild:

- `rebuild_game/src/main/java/Scene1Room0Group0Script.java`
- `rebuild_game/src/main/java/VqsvSavePromptBlocking.java`
- `rebuild_game/src/main/java/VqsvSaveRuntime.java`
- `rebuild_game/src/main/java/VqsvEventState.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`
- `rebuild_game/src/main/java/VqsvText.java`

## Source Room1 Group1 Records

Decoded source room:

```text
scene = 1
room = 1
room_name = "Bắt được Thủy Mộc Thôn"
group = 1
record_count = 4
```

Records:

| Record | Opcode | Args | String args | Source meaning |
| ---: | ---: | --- | --- | --- |
| 0 | `15` | `[1,0,0]` | none | Gate on event state `[1,0,0]` being `3` or `4`. |
| 1 | `56` | `[0,1]` | `"50"`, `"0"` | Show/update actor `50` with state `0`. |
| 2 | `46` | none | `"Bạn có muốn lưu trữ không?"` | Open save prompt. |
| 3 | `14` | none | none | Complete the current group after save prompt flow. |

## Source Opcode Facts

### `op15`

Source `game.c case 15` checks:

```text
b[game.k.a(args[0], args[1])][args[2]] == 3 or 4
```

Meaning:

- room1 group1 is gated by source event state `[1,0,0]`.
- The group should not run if that state is not complete.

Current rebuild mapping:

| Source | Rebuild | Status |
| --- | --- | --- |
| `b[game.k.a(scene,room)][event] == 3/4` | `VqsvEventState.op15CheckEventState(scene,room,group)` currently checks state `3` only. | `PORTED/PARTIAL` |

Audit note:

- Source accepts `3` or `4`.
- Current helper only treats `3` as complete. This is acceptable for the current
  Bunny route if `[1,0,0]` is written as `3`, but a wrapper should trace this
  as `PORTED/PARTIAL`, not full parity.

### `op56`

Source `game.c case 56`:

- `args[1]` is the actor count.
- If `args[0] == 0`:
  - parse actor ids from string arg 0,
  - parse state values from string arg 1,
  - set actor state/visibility/update fields,
  - refresh actor render/action state.
- If `args[0] == 1`:
  - hide/remove actor ids from string arg 0.

For room1 group1:

```text
op56 [0,1], strings "50", "0"
```

Meaning:

- Show/update actor `50` to state `0`.

Current rebuild mapping:

| Source | Rebuild | Status |
| --- | --- | --- |
| `op56 [0,1] actor 50 state 0` | Existing `Scene.op56ActorVisibility(...)` can represent actor/state visibility; Bunny downstream already uses `op56 [1,1]` hide actor 50. | `PORTABLE / NOT WIRED FOR GROUP1` |

Audit note:

- A wrapper can safely assert/trace the source op56 row.
- Whether to execute `op56 [0,1]` depends on current room1 actor state after
  transition. This needs a focused smoke before enabling mutation.

### `op46`

Source `game.c case 46`:

| Source state | Behavior |
| --- | --- |
| first entry | `S.H(); S.a(prompt); record.state = 5` |
| `S.f == 0` + confirm | `S.f = 1`; text `"Đang lưu..."`; `S.J()` |
| `S.f == 1` | mark current event cell state `3`; call `((k)y).k()` source save; on success text `"Lưu thành công"`; `S.f = 2` |
| `S.f == 2` | close UI with `S.I()`, clear `S.f`, continue event |
| back/cancel | close UI and continue without saving |

Current rebuild mapping:

| Source | Rebuild | Status |
| --- | --- | --- |
| Save prompt text | `VqsvText.Common.SAVE_PROMPT` in `VqsvSavePromptBlocking` | `PORTED/PARTIAL` |
| Confirm yes -> saving text | `SAVE_IN_PROGRESS`, then `VqsvSaveRuntime.save(s)` | `PORTED/PARTIAL` |
| Success text | `SAVE_SUCCESS` | `PORTED/PARTIAL` |
| Save payload | `build/save/vqsv_autosave.properties` route snapshot | `PORTED/PARTIAL`, full RMS `PENDING` |
| Source marks current event cell `3` during `op46` | Current `VqsvSavePromptBlocking` does not mark room1 group1 complete itself. | `PENDING` |
| Prompt UI | Rebuild msgconfirm-style prompt with check/X controls | `PORTED/PARTIAL` |

Audit note:

- Current save prompt is useful and smoke-backed, but not yet represented as
  decoded room1 group1 `op46`.
- Full original `game.k.k()` RMS multi-record save remains out of scope.

### `op14`

Source `game.c case 14` sets current record/group state to complete.

Current rebuild mapping:

| Source | Rebuild | Status |
| --- | --- | --- |
| `op14` current group complete | `Scene.op14CompleteEvent(scene,room,group)` via `VqsvEventState` | `PORTED/PARTIAL` |

For room1 group1, a source-shaped wrapper should eventually mark:

```text
op14CompleteEvent(1,1,1)
```

## Current Rebuild Call Point

Current manual flow in `Scene1Room0Group0Script`:

```text
loadScene1Room1(...)
placePlayerAtTransitionActorApprox(37, 16)
new VqsvSavePromptBlocking()
new Op13FreeWorldTrigger(1, 1, 0, 370, 176, 80, 32)
```

Current classification:

| Source group behavior | Current rebuild | Status |
| --- | --- | --- |
| room1 group1 exists as separate source event group | Not represented as its own wrapper. Save prompt is inserted manually in room0 group0 script. | `APPROX/PARTIAL` |
| group1 before room1 group0 op13 free-world trigger | Current call point is before `Op13FreeWorldTrigger`, matching practical route order. | `PORTED/PARTIAL` |
| op15 gate `[1,0,0]` | Current call point does not visibly check `op15` before save prompt. | `PENDING` |
| op56 actor 50 state 0 | Current call point does not execute group1 op56 wrapper. | `PENDING` |
| op46 save prompt | `VqsvSavePromptBlocking` | `PORTED/PARTIAL` |
| op14 group1 complete | Current save prompt does not mark group1 complete. | `PENDING` |

## Safety Decision

A trace-only or small wrapper slice is safe if it obeys these limits:

1. It may create a data wrapper for room1 group1:

```text
scene1 room1 group1 save prompt
op15 [1,0,0]
op56 [0,1] actors "50" states "0"
op46 "Bạn có muốn lưu trữ không?"
op14 [1,1,1]
```

2. It may be called at the existing `Scene1Room0Group0Script` save point,
   after `loadScene1Room1(...)` and before `Op13FreeWorldTrigger(...)`.

3. First code slice should be trace/assert-oriented:

- assert/trace `op15 [1,0,0]` current state;
- trace source `op56 [0,1] actor 50 state 0`;
- run existing `VqsvSavePromptBlocking`;
- after prompt completes, trace/optionally perform `op14CompleteEvent(1,1,1)`
  only if source-gate and route smoke prove it does not break save/resume.

4. It must not:

- change `VqsvSaveRuntime` payload format;
- claim full RMS parity;
- mutate `Scene.eventIndex`;
- replace `Op13FreeWorldTrigger`;
- change Bunny/Elder/Sophie battle branch semantics;
- touch P7;
- open the client.

## Proposed Next Code Slice

Create a small wrapper class, name flexible:

```text
VqsvRoom1Group1SavePromptWrapper
```

or a descriptor-style helper:

```text
VqsvSavePromptGroupDescriptor.SCENE1_ROOM1_GROUP1
```

Minimum behavior:

| Step | Wrapper behavior | Status target |
| --- | --- | --- |
| source metadata | stores op15/op56/op46/op14 facts | `PORTED` |
| op15 | traces `op15 [1,0,0]`, requires current route state complete or records `PENDING` trace | `PORTED/PARTIAL` |
| op56 | traces actor `50` state `0`; execute only if focused smoke proves actor exists and call is safe | `PORTED/PARTIAL` |
| op46 | delegates to existing `VqsvSavePromptBlocking` | `PORTED/PARTIAL` |
| op14 | after prompt closes, mark `op14CompleteEvent(1,1,1)` if safe | `PORTED/PARTIAL` |
| event index | leave unchanged | `PORTED` |

Recommended first implementation:

- Implement wrapper and call it from `Scene1Room0Group0Script` in place of
  direct `new VqsvSavePromptBlocking()`.
- Keep `VqsvSavePromptBlocking` behavior unchanged.
- Add trace strings that identify:
  - `Room1Group1SavePromptWrapper`,
  - `op15 [1,0,0]`,
  - `op56 [0,1] actor=50 state=0`,
  - `op46 save prompt`,
  - `op14 [1,1,1]`.

## Smoke Plan

Focused PNG/headless smoke:

| Checkpoint | Required proof |
| --- | --- |
| `room1_bunny_save_prompt` | Prompt opens; trace includes room1 group1 wrapper and op46. |
| `room1_bunny_save_success` | Save succeeds; trace includes op14 `[1,1,1]` if code slice marks it. |
| `room1_bunny_save_resume_state` | Route snapshot still restores room1 Bunny map. |
| `boot_title_continue_with_save` | Title continue still appears/routes after save. |
| `route_bunny_after_battle_task` | Regression: Bunny task route still reaches return-to-elder task. |
| `route_elder_after_battle_reward_state` | Regression: Elder reward route unaffected. |
| `route_sophie_after_battle_branch` | Regression: Sophie op47 route unaffected. |

Verification commands:

```powershell
.\build.ps1
java -cp build\classes com.vqsv.rebuild.Main --check
java -cp build\classes VqsvBattleDamageFormulaCheck
rg -n "�|Ã|Â|Ă|Ä|Æ" src\main\java
git diff --check -- rebuild_game\src\main\java rebuild_plan
```

## Current Exercise Answers Snapshot

This audit supports these entry-exercise answers:

| Question | Answer |
| --- | --- |
| Current roadmap phase | Phase 8: battle entry/exit + event integration. |
| P7 status | Closed for current routes by `142_battle_p7_phase6_closeout_and_next_phase.md`; do not reopen without source-route mismatch/original capture. |
| Bunny/Elder/Sophie op47 coverage | `143`, `144`, `145`, `146` prove descriptors/op47/downstream trace coverage for current battle routes. |
| op47 behavior | `result=-1` skips branch; otherwise raw target is `op47Args[result]`, source cursor is `rawTarget - 2`; rebuild keeps raw `battleBranchTarget`. |
| Save gap | Source room1 group1 has `op15/op56/op46/op14`; rebuild currently inserts `VqsvSavePromptBlocking` manually before room1 group0 `Op13FreeWorldTrigger`. Full RMS is still pending. |

## Current Recommendation

Next code slice can be:

```text
Implement a room1 group1 save prompt source-wrapper around the existing
VqsvSavePromptBlocking, with trace/assert coverage and no save payload changes.
```

## Implementation Result

Implemented the safe wrapper slice:

| File | Change | Status |
| --- | --- | --- |
| `rebuild_game/src/main/java/VqsvRoom1Group1SavePromptWrapper.java` | Added a small `Blocking` wrapper for source room1 group1. It traces `op15 [1,0,0]`, `op56 [0,1] actor=50 state=0`, delegates `op46` to the existing save prompt, and marks `[1,1,1]` complete. | `PORTED/PARTIAL` |
| `rebuild_game/src/main/java/Scene1Room0Group0Script.java` | Replaced direct `new VqsvSavePromptBlocking()` with `new VqsvRoom1Group1SavePromptWrapper()` at the existing room1 save call point, before `Op13FreeWorldTrigger`. | `PORTED/PARTIAL` |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Updated room1 save prompt/success checkpoints to use the wrapper and assert wrapper trace / event-state completion. | `PORTED/PARTIAL` |

Behavior intentionally unchanged:

- `VqsvSavePromptBlocking` UI behavior is unchanged.
- `VqsvSaveRuntime` payload format is unchanged.
- `Scene.eventIndex` is not mutated.
- `Op13FreeWorldTrigger` remains after the save prompt.
- Bunny/Elder/Sophie battle branch semantics are unchanged.
- P7 is untouched.
- No generic decoded event VM was added.

Important parity note:

- Source `op46` marks the current event cell before `game.k.k()` save on the
  confirm/save path. The wrapper mirrors this for current route by marking
  `[1,1,1]` before `VqsvSaveRuntime.save(s)` when confirm-yes is detected.
- If the prompt is skipped, the wrapper marks `[1,1,1]` when the prompt closes,
  matching the following source `op14` at the group level.
- `op56 [0,1] actor=50 state=0` remains trace-only in this slice; executing
  actor mutation is left for a focused actor-state smoke if needed.

## Verification After Code

Required after this implementation:

- `build.ps1`
- `com.vqsv.rebuild.Main --check`
- `VqsvBattleDamageFormulaCheck`
- Java mojibake scan
- `git diff --check`
- Focused smoke PNG:
  - `room1_bunny_save_prompt`
  - `room1_bunny_save_success`
  - `room1_bunny_save_resume_state`
  - `boot_title_continue_with_save`
  - `route_bunny_after_battle_task`
  - `route_elder_after_battle_reward_state`
  - `route_sophie_after_battle_branch`

## Next Phase 8 Candidate

After this wrapper is verified, the next Phase 8 step should be one of:

1. Close room1 group1 save wrapper with a short `149` closeout if no smoke
   regression appears.
2. Audit the next pending battle event group from scene1 only if the user wants
   to expand Phase 8 beyond current routes.
3. Audit source state 10/free-world event resume mechanics before attempting
   any generic decoded event VM.
