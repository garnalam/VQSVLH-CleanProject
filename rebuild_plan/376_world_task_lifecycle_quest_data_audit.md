# 376 - World Task Lifecycle / Quest Data Audit

Date: 2026-07-15

Scope: audit current task lifecycle/data after `task.ui` runtime polish. This is source/read-only audit; no gameplay code change in this slice.

## Source Facts

| Area | Source fact | Rebuild equivalent | Status |
| --- | --- | --- | --- |
| Main task table | `game.e` constructor loads `/data/script/mTask.mid` into `game.e.E`. | `VqsvPanelRuntime.loadMainTasks()`. | `PORTED/PARTIAL` |
| Branch task table | `game.e` constructor loads `/data/script/bTask.mid` into `game.e.D`. First half = names, second half = details. | `VqsvPanelRuntime.loadBranchTasks()`. | `PORTED/PARTIAL` |
| Branch marker table | `game.e` constructor loads `/data/script/bqTask.mid` twice into `ap` and `aq`. | `VqsvBranchQuestRuntime.refreshBqTaskMarkers()` currently hard-ports row0. | `PORTED/PARTIAL` |
| Main task progress | Opcode `45` calls task tip UI and sets `game.e.G = b()[0]`. | `Scene.sourceMainTaskProgress`, `sourceSetMainTaskProgress`, task UI main tab. | `PORTED` |
| Branch accept | Opcode `49`, if selected option is `0` and `b()[1] == 1`, writes `F[H][1] = 1`, increments `H`. `F[H][0]` was set by the matching opcode43 gate. | `VqsvPanelRuntime.openBranchTaskAcceptOption`, `VqsvBranchQuestRuntime.accept`. | `PORTED/PARTIAL` |
| Branch completion | When an event record ends with state `3`, if source opcode is `44` and `b()[1] == 1`, source calls `game.e.m(b()[0])`, which sets matching `F[i][1] = 3`. | `DodoGroup8CompletionFlow` calls `sourceCompleteBranchTask(0)` after reward, then `op14CompleteEvent(1,0,8)`. | `PORTED/PARTIAL` |
| Marker refresh | `game.e.G()` renders sprite `259`: anim `7` for available `ap`, anim `15` for active `aq`, anim `1` for directly ready `aq` when predicate `b(...)` passes. | Row0 available/active/clear marker smoke exists; after group8 complete, row1 available marker is source-shaped as `ap[1]` anim `7`. | `PORTED/PARTIAL` |
| Objective ownership predicate | `game.e.b(...)` case `0` checks `game.j.a((byte)b()[9], b()[10]) == 2`. `game.j.a(byte,int)` returns pet-record status `D[category][species-X[category]]`; source status `2` means obtained/caught, not merely seen. | `Scene.sourcePetRecordObtained(category,speciesId)` maps the group8 predicate to `sourcePets`/`sourcePetBank` ownership for category `1`, id `23`. | `PORTED/PARTIAL` |

## Dodo Row0 / Row1 Source Matrix

| Source group | Raw source rows | Meaning | Rebuild status |
| --- | --- | --- | --- |
| `scene_1 room0 group7` | `opcode43 short=[0,1,1,0,35,0,0,-1,-1,0,0]`; `opcode49 short=[0,1,-1,-1] strings=[x3, , 6,10, Tiếp nhận/từ chối]`; `op40` task tips; `op14`. | Available branch task0 at actor35. Accept creates `F/H` row for task0 status `1`; then group7 event completes. | `PORTED/PARTIAL` |
| `bqTask row0` | `ap[0]=[1,0,7]`, `aq[0]=[1,0,8]`. | Available marker points to group7; active/ready marker points to group8. | `PORTED/PARTIAL` |
| `scene_1 room0 group8` | `opcode44 short=[0,1,1,0,35,1,0,7,0,1,23,0]`; dialog Dodo thanks; `opcode17 short=[0,1,3]`; `op14`. | Active task0 completion: objective predicate checks pet-record category `1`, species/id `23`, status `2`; on event state `3`, source calls `game.e.m(0)` because opcode44 `b()[1] == 1`. Reward is item id `1` quantity `3`. | `PORTED/PARTIAL` |
| `bqTask row1` | `ap[1]=[1,0,9]`, `aq[1]=[2,1,6]`. | After row0, actor35 group9 offers branch task1. After task1 accept, room0 marker clears; scene2 room1 actor73 Eliza shows active marker anim `15`, or ready marker anim `1` when species68 Điện Miêu is obtained. | `PORTED/PARTIAL` |
| `scene_1 room0 group9` | `opcode43 short=[1,1,1,0,35,1,0,8,1,0,0]`; `opcode49 short=[0,1,-1,-1] strings=[x3, , 7,11, Tiếp nhận/từ chối]`; `op40`; `op14`; reject branch `op42`. | Available branch task1 at actor35, gated after group8. Accept creates `F/H` row task1 status `1` and completes event `[1,0,9]`; reject/back does not add task1. | `PORTED/PARTIAL` |
| `scene_2 room1 group6` | `opcode44 short=[1,1,2,1,73,1,0,9,0,4,68,0]`; dialogs speaker `Eliza`; `opcode17 short=[0,1,3]`; `op14`. | Active task1 completion: objective predicate checks pet-record category `4`, species/id `68` Điện Miêu; reward is item id `1` quantity `3`; then source equivalent `game.e.m(1)` marks task1 complete. | `PORTED/PARTIAL` |

## Current Rebuild Reality

| Item | Status | Note |
| --- | --- | --- |
| `task.ui` main/branch visual runtime | `PORTED/PARTIAL` | Source-shaped and smoke-covered, but not full generic widget VM. |
| Branch task owner | `PORTED` | `VqsvBranchQuestRuntime` must own all `F/H` mutation. |
| Dodo task0 accept/reject | `PORTED/PARTIAL` | Accept/reject and marker switch are smoke-covered. |
| Dodo task0 completion group8 | `PORTED/PARTIAL` | Missing-objective and complete-objective paths are smoke-covered; pet-record predicate is narrow, not full `game.j.D`. |
| Dodo task1 group9 accept/reject | `PORTED/PARTIAL` | Accept/reject and room0 marker clear are smoke-covered; completion target `aq[1]=[2,1,6]` is now ported through Eliza group6. |
| Eliza task1 completion group6 | `PORTED/PARTIAL` | Missing-objective and complete-objective paths are smoke-covered; scene2 room1 loader currently contains only actor73 Eliza and no full map/actor parity. |
| Full 30 `bqTask` rows | `PENDING` | Do not bulk-port without choosing a concrete quest row. |
| Generic opcode49 payload parser | `PENDING` | Current Dodo task0 payload is special-cased. |
| Generic objective predicates `game.e.a(...)` / `game.e.b(...)` | `PARTIAL` | Row0 available/active is source-shaped; full predicate family is not implemented. |

## Recommended Next Slice

Next slice: choose the next concrete branch quest row from `bqTask.mid`, or audit full scene2 room1 map/actor parity before expanding Bích Thủy routes.

Checklist:

1. Keep branch quest state changes inside `VqsvBranchQuestRuntime`.
2. Do not generic-port opcode43/44/49 VM until a concrete row requires it.
3. If continuing Dodo quest chain, audit the next `bqTask` row first.
4. Full scene2 room1 map/actor parity remains a separate route/UI task.

## Smoke Evidence

| Checkpoint | What it proves | Status |
| --- | --- | --- |
| `world_dodo_group8_missing_objective` | Task0 active but no pet-record `category=1,id=23,status=2`: no reward, no completion, active marker remains. | `PASS` |
| `world_dodo_group8_complete_task0` | Pet-record objective satisfied: Dodo thanks, item id `1` x3 awarded, `F[task0][1]=3`, event `[1,0,8]` complete, marker advances to row1 availability. | `PASS` |
| `panel_task_branch_completed_after_dodo_group8` | After group8 completion, `task.ui` branch tab renders row0 as `Hoàn thành`. | `PASS` |
| `world_dodo_group9_accept_task1` | After group8, Dodo group9 accept creates task1 status `1`, completes event `[1,0,9]`, and clears room0 marker because `aq[1]` points to scene2 room1 group6. | `PASS` |
| `world_dodo_group9_reject_no_task1` | Reject/back does not add task1, does not complete event `[1,0,9]`, and keeps Dodo available marker anim `7`. | `PASS` |
| `world_eliza_group6_missing_dien_mieu` | Task1 active but no pet-record `category=4,id=68,status=2`: no reward, no completion, scene2 room1 marker remains active anim `15`. | `PASS` |
| `world_eliza_group6_complete_task1` | Pet-record objective satisfied: Eliza thanks player, item id `1` x3 awarded, `F[task1][1]=3`, event `[2,1,6]` complete, marker clears. | `PASS` |
| `panel_task_branch_task1_completed_after_eliza_group6` | After Eliza group6 completion, `task.ui` branch tab can render completed branch state for task1. | `PASS` |

Latest regression:

- `world_panel_full`: PASS `76/76` after Eliza group6 task1 completion port.

Keep full scene2 room1 map/actor parity and generic bqTask rows as later slices.
