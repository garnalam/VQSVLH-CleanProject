# 146 Battle Phase 8 Post-Op47 Downstream Consumer Audit

Status: IMPLEMENTED / VERIFIED.

Purpose:

- Audit what happens after battle result `op47` on current Phase 8 routes.
- Identify which task/save/free-world return consumers are still manual route
  script behavior.
- Pick the next smallest safe code slice.
- Do not reopen P7.
- Do not mutate `Scene.eventIndex`.
- Do not genericize the full decoded event VM yet.

This follows:

- `143_battle_phase8_entry_exit_event_integration_audit.md`
- `144_battle_phase8_op47_consumer_wrapper_audit.md`
- `145_battle_phase8_sophie_descriptor_op47_audit.md`

## Source Files Read

Primary source and data:

- `modules/source_code/decoded/decompiled_source_cfr/game/c.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/event/decoded/data__event__scene_1.mid.json`

Current rebuild:

- `rebuild_game/src/main/java/VqsvBattleEventDescriptor.java`
- `rebuild_game/src/main/java/Scene1Room1BunnyScript.java`
- `rebuild_game/src/main/java/Scene1Room0Group6ElderBattleScript.java`
- `rebuild_game/src/main/java/Scene1Room3EntryScript.java`
- `rebuild_game/src/main/java/Scene1Room0Group0Script.java`
- `rebuild_game/src/main/java/VqsvEventState.java`
- `rebuild_game/src/main/java/VqsvSavePromptBlocking.java`
- `rebuild_game/src/main/java/VqsvSaveRuntime.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Related docs:

- `98_save_resume_bunny_task_source_audit.md`
- `145_battle_phase8_sophie_descriptor_op47_audit.md`

## Source Opcode Facts

These are the downstream consumers relevant after battle exit:

| Opcode | Source behavior | Current relevance |
| ---: | --- | --- |
| `4` | Dialog through `game.h` text UI. | Bunny/Elder/Sophie post-battle dialog. |
| `14` | Ends/completes the current event record group in the event runner. | Bunny and Elder manually call `op14CompleteEvent`; Sophie still reaches later transition manually. |
| `17` | Add/remove item, then show reward/warning UI and wait for close. | Elder rewards and earlier Bunny-ball task rewards. |
| `19` | Add special/material reward, then show reward/warning UI and wait. | Elder reward `op19 [5,1]`. |
| `23` | Sets event state byte `3` for a target world event cell and hides/removes active actor when in current room. | Bunny marks `[1,0,1]`; Elder marks `[1,0,4]` and `[1,0,5]`. |
| `31` | Add/remove money or badge and show reward/loss UI. | Elder money reward `500`. |
| `40` | Shows notice text through `S.c(...)` and waits for UI close. | Bunny return-Elder task notice; Elder free-world notice. |
| `45` | Shows task notice through `S.c(...)`, sets `game.c.t` task flag. | Elder task target `2`, text "Đến Bích Thủy Thành." |
| `46` | Save prompt: opens save UI, on confirm calls source save path and marks current event cell complete on success. | Room1 group1 save prompt after entering Bunny map; rebuild is partial route snapshot. |
| `47` | If battle result `l == -1`, skip branch. Otherwise set source cursor to `op47Args[l] - 2`. | Current descriptor wrapper traces/asserts raw target and source cursor, without event-index mutation. |
| `56` | Actor visibility/state update for string-listed actor ids. | Bunny hides actor `50`; room1 group1 can show actor `50`. |

Important boundary:

```text
op47 is only the battle-result branch consumer.
The actual task/reward/save/free-world effects are normal event opcodes after
the selected cursor or after the skip path.
```

Current rebuild has `consumeOp47` only as a trace/assert bridge. The following
downstream opcodes are still executed by manual route scripts, not by a decoded
event runner.

## Current Route Matrix

### Bunny Room1 Group0

Source room1 group0 records after trigger:

| Source order | Opcode | Args | Meaning |
| ---: | ---: | --- | --- |
| 0 | `13` | `370,176,80,32` | Trigger region. |
| 1 | `37` | `34,5,1` | Bunny encounter. |
| 2 | `52` | `0,1` | Battle/catch flags. |
| 3 | `66` | `0` | `an.U`, source visual mode setup. |
| 4 | `32` | `0,0` | Enter battle. |
| 5 | `47` | `12,0,0` | Result consumer. Catch success uses result `-1`, so source skips branch and continues. |
| 6 | `4` | dialog | Neil report after catching Bunny. |
| 7 | `56` | `1,1`, strings `50`, `0` | Hide/update Bunny actor `50`. |
| 8 | `23` | `1,0,1` | Mark room0 group1/task-state complete. |
| 9 | `40` | text | Notice: return to elder. |
| 10 | `14` | none | Complete room1 group0. |
| 11-13 | `4/10/42` | later branch path | Non-current branch/re-entry path; not part of Bunny catch success smoke. |

Current rebuild:

| Source consumer | Rebuild code | Status |
| --- | --- | --- |
| `op37/op52/op32/op47` | `SCENE1_ROOM1_GROUP0_BUNNY.runtime(...)` and `consumeOp47` | PORTED/PARTIAL |
| `op47 result=-1 skip` | Wrapper asserts branch `-1`, no event-index mutation | PORTED/PARTIAL |
| `op4` | `VqsvSceneScriptSupport.dialog(...)` | PORTED/PARTIAL |
| `op56` | `s.op56ActorVisibility(1, new int[]{50}, new int[]{0})` | PORTED/PARTIAL |
| `op23` | `s.op23MarkEventComplete(1,0,1)` | PORTED/PARTIAL |
| `op40` | `taskNotice(TASK_RETURN_ELDER)` | PORTED/PARTIAL; source `game.h` runtime still partial |
| `op14` | `s.op14CompleteEvent(1,1,0)` inside manual script | PORTED/PARTIAL |
| free-world return/transition | `ActorTransitionFreeWorldTrigger(1,1,37,3,1,0,30)` | APPROX/PARTIAL; source event VM/world state not generic |

Gap:

- The script knows the post-`op47` opcode sequence manually.
- There is no source-shaped downstream descriptor for `op56/op23/op40/op14`.
- The non-current branch path after `op47 [12,0,0]` remains PENDING.

### Room1 Group1 Save Prompt

Source room1 group1 records:

| Source order | Opcode | Args | Meaning |
| ---: | ---: | --- | --- |
| 0 | `15` | `1,0,0` | Gate on an earlier source event state. |
| 1 | `56` | `0,1`, strings `50`, `0` | Show/update actor `50`. |
| 2 | `46` | prompt text | Ask whether to save. |
| 3 | `14` | none | Complete room1 group1. |

Current rebuild:

| Source consumer | Rebuild code | Status |
| --- | --- | --- |
| save prompt UI | `VqsvSavePromptBlocking` | PORTED/PARTIAL |
| save payload | `VqsvSaveRuntime` route snapshot | PORTED/PARTIAL; full RMS records PENDING |
| source call point | Inserted in `Scene1Room0Group0Script` after transition to room1 and before `Op13FreeWorldTrigger` | APPROX/PARTIAL |
| room1 group1 `op15/op56/op46/op14` group | Not represented as its own event group | PENDING |

Gap:

- Existing save implementation is useful and smoke-backed, but it is not
  attached to decoded room1 group1 as source does.
- Full `game.k.k()` RMS parity remains out of scope and PENDING.
- A small future slice can source-shape only the room1 group1 group wrapper
  without changing save payload format.

### Elder Room0 Group6

Source room0 group6 relevant records:

| Source order | Opcode | Args | Meaning |
| ---: | ---: | --- | --- |
| battle setup | `67/37/32/47` | `52`, `68,5,1`, `0,2`, `10,10,0` | Elder battle and result branch. |
| after op47 | `4` | dialog | Elder reward dialog. |
| reward | `31` | `0,0,500` | Gain 500 money. |
| reward | `17` | `0,4,10` | Gain item 4 x10. |
| reward | `17` | `0,11,2` | Gain item 11 x2. |
| reward | `19` | `5,1` | Gain special/material 5 x1. |
| dialog | `4/4/4` | text | Book/Abra/Neil dialog. |
| state | `23` | `1,0,4` | Mark event state complete. |
| state | `23` | `1,0,5` | Mark event state complete. |
| notice | `45` | `2`, task text | Task flag/text for Bích Thủy. |
| notice | `40` | free-world text | Free movement notice. |
| end | `14` | none | Complete room0 group6. |

Current rebuild:

| Source consumer | Rebuild code | Status |
| --- | --- | --- |
| `op67/op37/op32/op47` | `SCENE1_ROOM0_GROUP6_ELDER.runtime(...)` and `consumeOp47` | PORTED/PARTIAL |
| rewards `31/17/19` | `op31CurrencyReward`, `op17Item`, `op19SpecialReward` | PORTED/PARTIAL; UI source-backed partial |
| `op23` | `op23MarkEventComplete(1,0,4/5)` | PORTED/PARTIAL |
| `op45` | manual trace + `TextBox.taskTip(TASK_BICH_THUY)` | PORTED/PARTIAL; `game.c.t` task flag is approximated |
| `op40` | manual trace + `TextBox.openBox(FREE_WORLD)` | PORTED/PARTIAL |
| `op14` | `op14CompleteEvent(1,0,6)` | PORTED/PARTIAL |
| free-world after op14 | `Room0PostGroup6FreeWorld` | APPROX/PARTIAL |

Gap:

- Elder downstream behavior is source-aligned but route-script driven.
- `op45` still lacks a shared source-shaped wrapper that records `game.c.t`.
- `Room0PostGroup6FreeWorld` mixes current-route free movement, transitions,
  and pending side quests; it is not a generic source state 10/world event loop.

### Sophie Room3 Group0

Source records around battle:

| Source order | Opcode | Args | Meaning |
| ---: | ---: | --- | --- |
| 72 | `67` | `56` | Battle actor/enemy hint. |
| 73 | `37` | `5,20,4` | Encounter. |
| 74 | `52` | `1,1` | Battle flags. |
| 75 | `32` | `0,2` | Enter battle. |
| 76 | `47` | `78,78,0` | Branch to raw target 78 for result 0 or 1. |
| 77 | `3` | actor/state strings | Hide/update actor 50 in current manual port. |
| 78+ | `4/5/12/...` | text/effects | Post-battle kidnapping sequence. |

Current rebuild:

| Source consumer | Rebuild code | Status |
| --- | --- | --- |
| `op67/op37/op52/op32/op47` | `SCENE1_ROOM3_GROUP0_SOPHIE.runtime(...)` and `consumeOp47` | PORTED/PARTIAL |
| `op47` trace | Asserts `rawTarget=78`, `sourceCursor=76`, raw branch remains `78` | PORTED/PARTIAL |
| post-battle actor hide/dialog/effects | Manual `hide`, dialog, effects in `Scene1Room3EntryScript` | PORTED/PARTIAL/APPROX depending opcode |
| final transition to room0 | Manual `prepareTransition`, `markWorldTransition`, `loadScene1Room0` | APPROX/PARTIAL |

Gap:

- Sophie now has descriptor/op47 coverage, but downstream sequence remains a
  manual script.
- Event-state completion/save-state for the long room3 group0 sequence remains
  broader scene-script work, not battle-specific.

## Cross-Route Classification

| Area | Status | Notes |
| --- | --- | --- |
| Current-route battle result descriptor data | PORTED/PARTIAL | Bunny, Elder, Sophie covered. |
| `op47` source cursor trace/assert | PORTED/PARTIAL | Does not mutate `eventIndex`. |
| Route post-battle dialog/reward/task ops | PORTED/PARTIAL | Source-shaped helpers exist, but scripts call them manually. |
| Source event-state byte `3` for `op23/op14` | PORTED/PARTIAL | `VqsvEventState` stores current key triples; not full original event table. |
| Save prompt UI/runtime | PORTED/PARTIAL | Current Bunny save route works; full RMS/source call point partial. |
| Free-world return after battle | APPROX/PARTIAL | Current routes work, but not generic source state 10/world VM. |
| Generic decoded-event consumer after op47 | PENDING | No VM runner slice yet. |
| P7 visual/timing | CLOSED FOR CURRENT ROUTES | Do not reopen without source-route mismatch or original capture. |

## Implementation Result

Implemented the descriptor/trace-only slice:

| File | Change | Status |
| --- | --- | --- |
| `rebuild_game/src/main/java/VqsvPostBattleDownstreamDescriptor.java` | Added data-only downstream descriptors for Bunny and Elder. They trace/assert source opcode clusters after the existing manual route helpers have already run. | PORTED/PARTIAL |
| `rebuild_game/src/main/java/Scene1Room1BunnyScript.java` | Calls Bunny downstream descriptor after manual `op14CompleteEvent(1,1,0)` and before free-world return trigger. | PORTED/PARTIAL |
| `rebuild_game/src/main/java/Scene1Room0Group6ElderBattleScript.java` | Calls Elder downstream descriptor after manual `op14CompleteEvent(1,0,6)` and before `Room0PostGroup6FreeWorld`. | PORTED/PARTIAL |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Focused Bunny/Elder route smokes now assert downstream descriptor trace in addition to op47 trace. | PORTED/PARTIAL |

Behavior intentionally unchanged:

- The descriptor does not execute reward/task/save/free-world helpers.
- It only asserts state that manual helpers already produced and appends trace.
- `Scene.eventIndex` is checked and must remain unchanged.
- `battleBranchTarget` semantics are unchanged.
- Save payload/RMS format is unchanged.
- P7 is untouched.
- No generic decoded event VM was added.

## Implemented Slice Definition

Implemented Phase 8 code slice:

```text
Create a `PostBattleDownstreamDescriptor` trace/assert wrapper for Bunny and
Elder only.
```

Scope:

1. Add data-only downstream descriptors:
   - Bunny: `op56 [1,50,0]`, `op23 [1,0,1]`, `op40 TASK_RETURN_ELDER`,
     `op14 [1,1,0]`.
   - Elder: `op31 [0,0,500]`, `op17 [0,4,10]`, `op17 [0,11,2]`,
     `op19 [5,1]`, `op23 [1,0,4]`, `op23 [1,0,5]`,
     `op45 taskFlag=2`, `op40 FREE_WORLD`, `op14 [1,0,6]`.
2. Do not execute/mutate through the new descriptor yet unless the existing
   script already executed the corresponding helper.
3. Add trace/assert after the manual script helper cluster to prove route code
   still matches source downstream records.
4. Keep Sophie as regression only for this downstream slice because its
   post-battle scene sequence is long and less battle-exit-specific.

Why this slice:

- It reduces drift without introducing a fragile generic event VM.
- It makes the manual post-battle clusters auditable like `consumeOp47`.
- It directly targets the current risk: route scripts silently diverging from
  decoded source records after battle exit.

Do not do in that slice:

- Do not mutate `Scene.eventIndex`.
- Do not replace the manual scripts wholesale.
- Do not alter reward amounts/items/text.
- Do not change save payload/RMS format.
- Do not touch P7.

## Smoke Plan For This Slice

Focused PNG/headless smoke:

| Checkpoint | Required proof |
| --- | --- |
| `route_bunny_after_battle_task` | Bunny op47 skip still passes; downstream trace confirms `op56/op23/op40/op14`; task text visible. |
| `route_elder_after_battle_reward_state` | Elder op47 still `rawTarget=10 sourceCursor=8`; downstream trace confirms rewards, event states, task/free-world notices; reward/free-world text visible. |
| `route_sophie_after_battle_branch` | Regression only; descriptor/op47 trace still present; branch remains `78`. |
| `room1_bunny_save_prompt` | Save prompt still opens at current route save point. |
| `room1_bunny_save_success` | Save success still writes route snapshot. |
| `boot_title_continue_with_save` | Continue still appears after save. |

Standard verification:

- `build.ps1`
- `com.vqsv.rebuild.Main --check`
- `VqsvBattleDamageFormulaCheck`
- Java mojibake scan
- `git diff --check`

## Current Recommendation

```text
Descriptor/trace-only downstream coverage for Bunny/Elder is complete.
Next should audit whether room1 group1 save prompt can be source-shaped as its
own op15/op56/op46/op14 group wrapper without changing save payload/RMS parity.
```

## Verification After Code

| Check | Status |
| --- | --- |
| `build.ps1` | PASS |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| Java mojibake scan | PASS, no matches |
| `git diff --check` targeted files | PASS, CRLF warnings only |
| `route_bunny_after_battle_task` | PASS, downstream descriptor trace asserted |
| `route_elder_after_battle_reward_state` | PASS, downstream descriptor trace asserted |
| `route_sophie_after_battle_branch` | PASS regression |
| `room1_bunny_save_prompt` | PASS regression |
| `room1_bunny_save_success` | PASS regression |
| `boot_title_continue_with_save` | PASS regression |

Smoke PNG output:

- `rebuild_game/build/smoke_phase8_downstream/route_bunny_after_battle_task.png`
- `rebuild_game/build/smoke_phase8_downstream/route_elder_after_battle_reward_state.png`
- `rebuild_game/build/smoke_phase8_downstream/route_sophie_after_battle_branch.png`
- `rebuild_game/build/smoke_phase8_downstream/room1_bunny_save_prompt.png`
- `rebuild_game/build/smoke_phase8_downstream/room1_bunny_save_success.png`
- `rebuild_game/build/smoke_phase8_downstream/boot_title_continue_with_save.png`
