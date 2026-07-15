# 374 - World Branch Quest Port Inventory Matrix

Date: 2026-07-15

Scope: inventory of the current world/task/branch-quest ports around `task.ui`, source task state `G/H/F`, `bqTask.mid` markers, and Dodo branch quest row 0.

This document is not a deep audit. It is the status board for what has actually been ported, what is only partial, and what must not be claimed complete yet.

## Status Legend

| Status | Meaning |
| --- | --- |
| `PORTED` | Source behavior is implemented in rebuild for this slice and covered by smoke/check. |
| `PORTED/PARTIAL` | Source-shaped behavior is implemented and useful, but not full source parity yet. Usually missing full row coverage, exact widget VM, objective predicate, or generic event VM. |
| `AUDITED/PENDING` | Source behavior is known, but code has not been ported yet. |
| `PENDING` | Required work is identified but not audited/ported enough to rely on. |
| `UNKNOWN` | Source behavior is not proven yet. Do not code from guesses. |

## Current Port Matrix

| Port area | Source proof | Rebuild implementation | What was ported | Status | Smoke / proof | Remaining gap |
| --- | --- | --- | --- | --- | --- | --- |
| Main task progress `G` | `game.e` opcode `45`; `game.k` task UI reads `game.e.G`. | `Scene.sourceMainTaskProgress`; `Scene.sourceSetMainTaskProgress`; `VqsvSceneScriptSupport.taskNoticeOp45`; `VqsvPanelRuntime.taskRowsForRender`. | `taskTip.ui` op45 now updates the source-shaped main task cursor. `task.ui` main tab reads this cursor instead of guessing from event flags. | `PORTED` | `panel_task_open_from_gamemenu`, op45 task tip route smokes; `world_panel_full` PASS `63/63`. | Full decoded event VM still not generic; only wrappers that call `taskNoticeOp45` update `G`. |
| Main task text table | `/data/script/mTask.mid`; `game.e.E`. | `VqsvPanelRuntime.loadMainTasks()`. | Main task title/detail text is loaded from decoded table. | `PORTED/PARTIAL` | Task panel smoke renders real rows. | Some decoded text may still carry decode/mojibake issues from source extraction; full text normalization not done here. |
| Branch task text table | `/data/script/bTask.mid`; `game.e.D`. | `VqsvPanelRuntime.loadBranchTasks()`. | Branch task title/detail text comes from decoded branch task table. | `PORTED/PARTIAL` | `panel_task_branch_open`, `panel_task_branch_detail`. | Only state rows that exist in runtime `F/H` are visible; full task lifecycle for all rows is not ported. |
| Branch quest runtime owner | Source `game.e.F/H` is the branch task runtime list. | `VqsvBranchQuestRuntime`. | New dedicated manager owns branch task rows and `bqTask` marker list. Provides accept/update/complete/status/refresh methods. `Scene` methods are now compatibility delegates. | `PORTED` | Build/check pass; `world_panel_full` after manager refactor PASS `63/63`. | Future branch quest code must go through this class; do not write raw task state in scripts. |
| Opcode `49` branch accept state | `game.e` case `49`: result `0` and `b()[1] == 1` writes `F[H][1]=1`, increments `H`. | `VqsvPanelRuntime.openBranchTaskAcceptOption`; `VqsvBranchQuestRuntime.accept`; `Scene.sourceAcceptBranchTask`. | Confirm option `0` accepts task and writes source-shaped `F/H`; option `1` or back does not add task. | `PORTED/PARTIAL` | `panel_task_branch_opcode49_reject_no_add`; `panel_task_option_*`; Dodo accept/reject smoke. | Popup UI is source-shaped, not full `taskOption.ui` widget VM for every possible opcode49 payload. |
| Branch task completion `m(taskId)` | `game.e.m(taskId)` sets matching `F[i][1]=3`; source calls it when opcode44 completes with `b()[1] == 1`. | `VqsvBranchQuestRuntime.complete`; `Scene.sourceCompleteBranchTask`; `DodoGroup8CompletionFlow`. | Runtime can mark branch task complete and Dodo group8 now completes task0 through the real source-shaped path. | `PORTED/PARTIAL` | `panel_task_branch_completed`; `panel_task_branch_save_load_g_h_f`; `world_dodo_group8_complete_task0`; `panel_task_branch_completed_after_dodo_group8`. | Generic opcode44 completion for all branch rows is still pending. |
| Save/load `G/H/F` | Source save/load persists `game.e.G`, `game.e.H`, and `game.e.F` rows. | `VqsvSaveRuntime.writeBranchTasks`; `restoreBranchTasks`; `sourceMainTaskProgress` property. | Rebuild properties save persists main task progress and branch task rows/status. | `PORTED/PARTIAL` | `panel_task_branch_save_load_g_h_f`; `world_panel_full` PASS. | Rebuild save format is not original binary save format parity. |
| `bqTask.mid` marker table load/audit | `game.e` constructor loads `/data/script/bqTask.mid` into `ap` and `aq`. | Docs `371`, `372`; runtime row0 hard mapping in `VqsvBranchQuestRuntime.refreshBqTaskMarkers`. | Row0 mapping is source-backed: `ap[0]=[1,0,7]`, `aq[0]=[1,0,8]`. | `PORTED/PARTIAL` | `world_bqtask_marker_available`, `world_bqtask_marker_active`, `world_bqtask_marker_completed_clear`. | Full 30-row table is not ported. |
| `bqTask` available marker | Source `ap` loop creates sprite `259` marker anim `7` when available predicate passes. | `VqsvBranchQuestRuntime.refreshBqTaskMarkers`; `SourceQuestMarker`. | For row0, actor35 gets sprite `259` anim `7` after early event gate, before task is accepted. | `PORTED/PARTIAL` | `world_bqtask_marker_available`; `world_dodo_group7_reject_no_task`. | Exact generic predicate `game.e.a(b2)` is not fully implemented. Current row0 predicate is source-shaped by known early gates. |
| `bqTask` active marker | Source `aq` branch marker uses task state `F[n][1] == 1`, sprite `259`, anim `15`. | `VqsvBranchQuestRuntime.refreshBqTaskMarkers`; `SourceQuestMarker`. | For row0, after Dodo accept, marker switches from available anim `7` to active anim `15`; missing group8 objective keeps it active. | `PORTED/PARTIAL` | `world_bqtask_marker_active`; `world_dodo_group7_accept_task0`; `world_dodo_group8_missing_objective`. | Full generic active objective predicate is still pending. |
| `bqTask` completed marker clear / next marker | Source branch task complete clears row0 active marker; if group8 event is complete, row1 `ap[1]=[1,0,9]` can become the next available marker; after accepting task1, active target is `aq[1]=[2,1,6]`; when objective category `4` species `68` is obtained, marker can become ready anim `1`. | `VqsvBranchQuestRuntime.refreshBqTaskMarkers`. | Task0 complete advances to task1 availability; task1 accept clears room0 marker; scene2 room1 actor73 shows task1 active/ready marker; Eliza completion clears it. | `PORTED/PARTIAL` | `world_bqtask_marker_completed_clear`; `world_dodo_group8_complete_task0`; `world_dodo_group9_accept_task1`; `world_dodo_group9_reject_no_task1`; `world_eliza_group6_missing_dien_mieu`; `world_eliza_group6_complete_task1`. | Full 30-row bqTask mapping remains pending. |
| Quest marker rendering | Source marker uses sprite `259`, anchor `actor.j, actor.k - 40`. | `SourceQuestMarker`; `VqsvSceneView` render loop; `Scene.sourceQuestMarkers` alias from runtime. | Marker is ticked/rendered above actor at source-shaped `y - 40` anchor. | `PORTED/PARTIAL` | Pixel-visible smoke asserts marker region. | Actor flag `actor.f((byte)1)` and exact MIDP sprite timing/pixel compare remain pending. |
| Dodo branch accept producer | `scene_1 room0 group7`; opcode `43`; actor `35`; opcode `49`; accept path op40 tips + op14 complete. | `Scene1Room0Group7DodoScript`; `Room0PostGroup6FreeWorld`. | Interacting with Dodo after group6 opens dialogs, opens opcode49 option, accepts task0, completes event `[1,0,7]`, refreshes marker to active. | `PORTED/PARTIAL` | `world_dodo_group7_accept_task0`. | Reward payload in `taskOption.ui` is source-shaped, not full opcode49 reward VM. |
| Dodo branch task0 completion | `scene_1 room0 group8`; opcode `44 short=[0,1,1,0,35,1,0,7,0,1,23,0]`; dialog; `opcode17 [0,1,3]`; `op14`; source calls `game.e.m(0)` after opcode44 completes. | `DodoGroup8CompletionFlow`; `Scene.sourcePetRecordObtained(1,23)`; `VqsvSourceOps.op17Item`; `sourceCompleteBranchTask(0)`. | If task0 is active and source pet-record predicate is met, Dodo thanks player, awards item id `1` x3, completes task0, completes event `[1,0,8]`, and marker advances to row1 availability. Missing objective leaves task active. | `PORTED/PARTIAL` | `world_dodo_group8_missing_objective`; `world_dodo_group8_complete_task0`; `panel_task_branch_completed_after_dodo_group8`. | Pet-record predicate is narrow and based on party/bank ownership, not full `game.j.D`; task1 group9 is pending. |
| Dodo branch task1 accept | `scene_1 room0 group9`; opcode `43 short=[1,1,1,0,35,1,0,8,1,0,0]`; opcode `49 short=[0,1,-1,-1]`; `op40`; `op14`; reject branch `op42`. | `Scene1Room0Group9DodoScript`; `TaskOptionData.branchTask(1)`; `sourceAcceptBranchTask(1)`. | If task0 is complete and event `[1,0,8]` is complete, Dodo offers task1. Accept adds/updates task1 status `1`, completes event `[1,0,9]`, and clears room0 marker. Reject/back leaves task1 unavailable and keeps available marker. | `PORTED/PARTIAL` | `world_dodo_group9_accept_task1`; `world_dodo_group9_reject_no_task1`. | Completion target `aq[1]=[2,1,6]` is now ported through Eliza group6; no generic opcode49 VM yet. |
| Eliza branch task1 completion | `scene_2 room1 group6`; opcode `44 short=[1,1,2,1,73,1,0,9,0,4,68,0]`; dialogs; opcode `17 short=[0,1,3]`; `op14`. | `Scene2Room1Group6ElizaScript`; `Scene2Room1FreeWorld`; `sourcePetRecordObtained(4,68)`; `sourceCompleteBranchTask(1)`. | If task1 is active and Điện Miêu species `68` is obtained, Eliza thanks player, awards item id `1` x3, completes task1, completes event `[2,1,6]`, and clears marker. Missing objective keeps task1 active. | `PORTED/PARTIAL` | `world_eliza_group6_missing_dien_mieu`; `world_eliza_group6_complete_task1`; `panel_task_branch_task1_completed_after_eliza_group6`. | Scene2 room1 loader now uses map49 plus actor73; full Bích Thủy actor parity is pending. |
| Dodo branch reject/back | Same opcode `49`; non-zero result branches away and does not add `F/H`. | `Scene1Room0Group7DodoScript`; `VqsvPanelRuntime.closeTaskOption`. | Back/reject closes the option without accepting, leaves task0 status `-1`, marker remains available. | `PORTED/PARTIAL` | `world_dodo_group7_reject_no_task`; `panel_task_branch_opcode49_reject_no_add`. | Exact reject dialog text/path is source-shaped; full op42/event VM path not generic. |
| `taskOption.ui` Dodo row0/row1 payload | `scene_1 room0 group7/group9` string args include reward/options: row0 `x3`, `6,10`; row1 `x3`, `7,11`; both `Tiếp nhận, từ chối`. | `TaskOptionData.branchTask(0/1)`. | Dodo task0/task1 options use source-shaped accept/reject labels and `x3` reward label. | `PORTED/PARTIAL` | Dodo accept/reject smoke. | Generic parsing of opcode49 reward arrays/options is pending. |
| `task.ui` branch render from runtime | `game.k` branch tab uses `H` count and `F[i][0/1]`. | `VqsvPanelRuntime.sourceBranchTasksForRender`. | Branch tab now reads branch task rows from `VqsvBranchQuestRuntime`, not direct smoke-only state. | `PORTED` | Branch tab smokes and save/load smoke. | Full widget runtime for `task.ui` still partial. |

## Explicit Pending / Not Ported Yet

| Area | Status | What must happen next |
| --- | --- | --- |
| Dodo task0 complete group `8` | `PORTED/PARTIAL` | Group8 is ported for task0 only: active task + obtained pet id `23` -> thanks dialog, item `1 x3`, `sourceCompleteBranchTask(0)`, event `[1,0,8]`, and marker advances to row1 availability. Remaining gap: narrow pet-record predicate, not full `game.j.D`. |
| Dodo task1 accept group `9` | `PORTED/PARTIAL` | Group9 accept/reject is ported for task1 only: after task0 complete + event `[1,0,8]`, Dodo opens opcode49 option; accept writes task1 status `1` and event `[1,0,9]`, reject/back does not add. Completion target `aq[1]=[2,1,6]` is now ported through Eliza group6. |
| Full `bqTask.mid` 30-row mapping | `PENDING` | Add row-by-row only when a concrete side quest is being ported. Do not bulk-guess rows. |
| `aq` direct-ready marker anim `1` | `PORTED/PARTIAL` | Ported narrowly for `aq[1]=[2,1,6]`: when task1 active and pet-record category `4`, species `68` is obtained, Eliza marker uses anim `1`; generic predicates for other rows remain pending. |
| Generic opcode `49` payload parser | `PENDING` | Parse reward arrays/options from event rows instead of special-casing task0 data. |
| Generic decoded event VM | `PENDING` | Current work is hand-port/source-shaped scripts, not full VM execution. |
| Full `task.ui` / `taskOption.ui` widget runtime | `PARTIAL` | UI renders source-shaped layout but is not a complete `game.h/game.k` widget VM. |
| Marker actor flag `actor.f((byte)1)` | `PENDING` | Need source-backed actor flag semantics before implementing. |
| Exact MIDP pixel compare | `PENDING` | Smoke PNG verifies visible state, not pixel-perfect original MIDP parity. |

## Current Smoke Evidence

Latest relevant smoke output:

- `world_panel_full`: PASS `76/76` after Eliza group6 task1 completion port.
- `world_panel_full` after `VqsvBranchQuestRuntime` refactor: PASS `63/63`.
- `world_dodo_group7_accept_task0`: PASS.
- `world_dodo_group7_reject_no_task`: PASS.
- `world_dodo_group8_missing_objective`: PASS.
- `world_dodo_group8_complete_task0`: PASS.
- `panel_task_branch_completed_after_dodo_group8`: PASS.
- `world_dodo_group9_accept_task1`: PASS.
- `world_dodo_group9_reject_no_task1`: PASS.
- `world_eliza_group6_missing_dien_mieu`: PASS.
- `world_eliza_group6_complete_task1`: PASS.
- `panel_task_branch_task1_completed_after_eliza_group6`: PASS.
- `com.vqsv.rebuild.Main --check`: PASS.
- Mojibake scan on edited Java files: no matches.

Key PNGs:

- `rebuild_game/build_intro_demo/world_panel_full_branch_manager/world_dodo_group7_accept_task0.png`
- `rebuild_game/build_intro_demo/world_panel_full_branch_manager/world_dodo_group7_reject_no_task.png`
- `rebuild_game/build_intro_demo/world_panel_full_branch_manager/world_bqtask_marker_available.png`
- `rebuild_game/build_intro_demo/world_panel_full_branch_manager/world_bqtask_marker_active.png`
- `rebuild_game/build_intro_demo/world_panel_full_branch_manager/world_bqtask_marker_completed_clear.png`
- `rebuild_game/build_intro_demo/dodo_group8_slice/world_dodo_group8_missing_objective.png`
- `rebuild_game/build_intro_demo/dodo_group8_slice/world_dodo_group8_complete_task0.png`
- `rebuild_game/build_intro_demo/dodo_group8_slice/panel_task_branch_completed_after_dodo_group8.png`
- `rebuild_game/build_intro_demo/dodo_group9_slice/world_dodo_group9_accept_task1.png`
- `rebuild_game/build_intro_demo/dodo_group9_slice/world_dodo_group9_reject_no_task1.png`
- `rebuild_game/build_intro_demo/eliza_group6_slice/world_eliza_group6_missing_dien_mieu.png`
- `rebuild_game/build_intro_demo/eliza_group6_slice/world_eliza_group6_complete_task1.png`
- `rebuild_game/build_intro_demo/eliza_group6_slice/panel_task_branch_task1_completed_after_eliza_group6.png`

## Rule Going Forward

All branch quest state changes must go through `VqsvBranchQuestRuntime`.

Allowed paths:

- Accept: `s.sourceAcceptBranchTask(taskId)` or `s.sourceBranchQuests.accept(s, taskId)`.
- Update/debug seed: `s.sourceUnlockBranchTask(taskId, status)` or `s.sourceBranchQuests.unlockOrUpdate(s, taskId, status)`.
- Complete: `s.sourceCompleteBranchTask(taskId)` or `s.sourceBranchQuests.complete(s, taskId)`.
- Marker refresh: `s.sourceRefreshBqTaskMarkers()` or `s.sourceBranchQuests.refreshBqTaskMarkers(s)`.

Do not add new direct `F/H` list mutation in script, panel, save, or smoke code.
