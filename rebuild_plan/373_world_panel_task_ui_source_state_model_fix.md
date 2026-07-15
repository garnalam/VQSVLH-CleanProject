# 373 - World Panel `task.ui` Source State Model Fix

Date: 2026-07-15

Scope: fix the `task.ui` data source problem where the rebuild rendered source text tables but still used guessed/mock task state.

## Problem

`task.ui` has two separate layers:

| Layer | Source | Previous rebuild issue | Status after this slice |
| --- | --- | --- | --- |
| Text table | `game.e.E` from `/data/script/mTask.mid`; `game.e.D` from `/data/script/bTask.mid` | Text was already loaded from decoded tables. | `PORTED/PARTIAL` |
| Runtime task state | `game.e.G` for main task progress; `game.e.H/F` for branch task list/status | Main tab guessed progress from a few event-complete flags. Branch tab was manually seeded in smoke. | `PORTED/PARTIAL`; `G/H/F` backbone now source-shaped and save/load covered |

## Source Facts

| Source | Fact | Status |
| --- | --- | --- |
| `game.e` constructor | Loads `bTask.mid` into static `D`, `mTask.mid` into static `E`, and initializes `F` if needed. | `AUDITED` |
| `game.k.U()/bc()/bd()` | Main tab uses `game.e.G` as current main task index/progress. | `AUDITED` |
| `game.k.U()/bc()/bd()` | Branch tab uses `game.e.H` as visible branch count and `game.e.F[i][0/1]` as task id/status. | `AUDITED` |
| `game.e` opcode `45` | Shows `taskTip.ui` and sets `game.e.G = args[0]`. | `PORTED` |
| `game.e` opcode `49` | Accepting a branch choice adds `F[H][0] = taskId`, `F[H][1] = 1`, then increments `H`. | `PORTED/PARTIAL` |
| `game.e.m(taskId)` | Completes branch task by setting `F[i][1] = 3`. | `PORTED/PARTIAL` |
| `game.l` save/load | Persists `game.e.G`, `game.e.H`, and all active `F` rows. | `PORTED/PARTIAL` |
| `scene_1 room0 group7` | Dodo actor35 branch task uses opcode `49`, `short_args=[0,1,-1,-1]`, option text `Tiếp nhận, từ chối`; accept completes event `[1,0,7]`. | `PORTED/PARTIAL` |

## Rebuild Changes

- Added `Scene.sourceMainTaskProgress`, equivalent to source `game.e.G`.
- Added `Scene.sourceSetMainTaskProgress(progress, source)`.
- Wired `VqsvSceneScriptSupport.taskNoticeOp45(...)` to set `sourceMainTaskProgress`, matching opcode `45`.
- Changed `VqsvPanelRuntime.taskRowsForRender()` / progress calculations to read `sourceMainTaskProgress` instead of guessing from event-complete flags.
- Updated task smoke setup to seed `sourceMainTaskProgress` explicitly.
- Added `Scene.sourceAcceptBranchTask(taskId)`, equivalent to opcode `49` accept result `0`: `F[H]=[taskId,1]`.
- Added `VqsvPanelRuntime.openBranchTaskAcceptOption(...)`; confirm option `0` accepts, back/option `1` does not add `F/H`.
- Wired `Room0PostGroup6FreeWorld` actor35 to `Scene1Room0Group7DodoScript` for real branch task row 0 acceptance instead of smoke-only setup.
- Added source-shaped task0 `taskOption.ui` data for Dodo row0 (`Tiếp nhận`, `Từ chối`, reward label `x3`).
- Persisted `sourceMainTaskProgress` and `sourceBranchTasks` in `VqsvSaveRuntime`, matching source `game.l` save/load of `G/H/F`.
- Added `VqsvBranchQuestRuntime` as the owner for branch quest state and markers:
  - owns source-shaped `F/H` branch task rows.
  - owns `bqTask` quest marker list.
  - provides accept/update/complete/status/refresh methods.
  - `Scene` wrappers now delegate into this runtime so future branch quest rows do not scatter logic across scripts/panels/save.
- Updated `task.ui` branch render and save/load to read/write through `VqsvBranchQuestRuntime`.

## Current Status

| Area | Status | Note |
| --- | --- | --- |
| Main task title/detail text | `PORTED/PARTIAL` | Uses decoded `mTask.mid`. Existing decoded JSON text may still show mojibake if source decode is not normalized. |
| Main task progress state `G` | `PORTED` | Opcode 45 wrapper now updates it, and panel uses it. |
| Branch task text | `PORTED/PARTIAL` | Uses decoded `bTask.mid`. |
| Branch task active/completed state `F/H` | `PORTED/PARTIAL` | Opcode 49 accept path and `m(taskId)` completion model are covered; generic event VM integration still pending. |
| Branch quest runtime ownership | `PORTED` | `VqsvBranchQuestRuntime` is now the single runtime owner for F/H and row0 bqTask marker state; old Scene methods are compatibility delegates. |
| Branch smoke seeding | `PORTED/PARTIAL` | Main branch smoke now goes through opcode 49 accept helper; focused setup helpers may still seed state for unrelated scenarios. |
| Dodo branch task row 0 producer | `PORTED/PARTIAL` | Real actor35 free-world interaction accepts task0 and drives marker from `F/H`; completion group8/group9 still pending. |
| Save/load for task state | `PORTED/PARTIAL` | `G/H/F` save/load smoke passes; full original binary save format is not targeted by rebuild properties save. |

## Smoke

Latest suite:

- `world_panel_full`: PASS `63/63`.
- `world_panel_full` after branch manager refactor: PASS `63/63`.

New/updated task-state checkpoints:

- `panel_task_branch_open`: now accepts branch task 0 through opcode 49 path before rendering.
- `panel_task_branch_completed`: accepts task 0 through opcode 49, then completes it via `sourceCompleteBranchTask(0)`.
- `panel_task_branch_detail`: accepts task 0 and task 1 through opcode 49 path.
- `panel_task_branch_opcode49_reject_no_add`: selecting option `1` leaves `F/H` empty.
- `panel_task_branch_save_load_g_h_f`: saves and loads `G=2`, `H=2`, `F0=[0,3]`, `F1=[1,1]`.
- `world_dodo_group7_accept_task0`: actor35 Dodo interaction accepts task0, writes `F/H`, completes `[1,0,7]`, and marker switches to active anim `15`.
- `world_dodo_group7_reject_no_task`: actor35 Dodo interaction backs/rejects without adding `F/H`, marker remains available anim `7`.

## Next

Recommended next slice:

1. Port Dodo branch completion side `scene_1 room0 group8/group9`, including objective predicate and `game.e.m(0)` completion.
2. Replace remaining smoke-only direct `sourceUnlockBranchTask(...)` calls when they are meant to represent real opcode `49` acceptance.
3. Continue `bqTask.mid` marker expansion using real `F/H` state instead of manual setup.
