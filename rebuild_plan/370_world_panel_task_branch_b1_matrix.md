# 370 - task.ui Branch Tab b=1 Matrix

Date: 2026-07-15

Scope: `task.ui` tab `b=1` (`Nhiệm vụ phụ`). No live client. Smoke PNG only.

## Source Facts

| Source data | Meaning | Source proof | Rebuild status |
| --- | --- | --- | --- |
| `game.e.D` | Branch task text table from `/data/script/bTask.mid`. First half is title, second half is detail. | `game.e` constructor loads `bTask.mid`; `game.k.bd()` reads `D[F[index][0]]` and `D[D.length / 2 + F[h][0]]`. | `PORTED/PARTIAL` |
| `game.e.F` | Branch task vector, source shape `short[200][2]`; each row is `[taskId, status]`. | Static/init allocates `F`; save/load writes only first `H` rows. | `PORTED/PARTIAL` |
| `game.e.H` | Count of unlocked branch tasks. | Save/load writes `H`; `game.k.bc()` sets list length to `H`. | `PORTED/PARTIAL` |
| `F[row][1] == 3` | Completed branch task. | `game.k.bd()` shows `Hoàn thành`; `game.e.m(taskId)` sets status `3`. | `PORTED/PARTIAL` |
| `F[row][1] == 1` | Active/uncompleted branch task. | Opcode `49` can set `F[H][1] = 1; H++`; `game.e.J()` checks active status for branch markers. | `PORTED/PARTIAL` |
| `bqTask.mid` `ap/aq` | Branch quest marker trigger tables, two matrices of `[scene/room/event]`. | `game.e` loads `/data/script/bqTask.mid`; `game.e.J()` uses `ap/aq` to decide NPC marker/effect. | `AUDITED/PENDING` |

## UI Behavior

| `game.k` method | Behavior | Rebuild status |
| --- | --- | --- |
| `U()` | Opens `/data/ui/task.ui`, resets cursor/scroll, then calls `bc()` and `bd()`. | `PORTED/PARTIAL` |
| `bc()` when `b=1` | Sets list size `a.a = H`, cursor/scroll to `0`, clears detail, sets progress label `Chi nhánh hoàn thành độ:`, counts completed `F[*][1] == 3`, divides by `D.length / 2`. | `PORTED/PARTIAL` |
| `bd()` when `b=1` | For visible rows `v+i < H`, row number is `i+v+1`, title is `D[F[v+i][0]]`, status is `Hoàn thành` only when `F[v+i][1] == 3`; detail is `D[D.length/2 + F[h][0]]`. | `PORTED/PARTIAL` |
| `V()` left/right | `p.a.b(2/3)` then `bc()` switches main/branch tab. | `PORTED/PARTIAL` |

## Rebuild Changes

- Added `SourceBranchTask(taskId,status)` as a source-shaped proxy for `game.e.F/H`.
- Added `Scene.sourceBranchTasks`, `sourceUnlockBranchTask(taskId,status)`, and `sourceCompleteBranchTask(taskId)`.
- `VqsvPanelRuntime.taskRowsForRender(..., tab=1)` now reads `sourceBranchTasks` instead of hardcoding one Bunny row.
- Branch progress now counts all `status == 3` rows and divides by `bTask.mid` half length, matching `game.k.bc()`.
- `selectedLabel()` for task mode now uses a cache updated from the current `Scene`, so branch rows can expose the actual selected title.

## Status

| Area | Status | Note |
| --- | --- | --- |
| Branch row title/detail/status render | `PORTED/PARTIAL` | Uses decoded `bTask.mid` text and `SourceBranchTask`. |
| Branch unlock/complete model | `PORTED/PARTIAL` | Source-shaped helper exists; full decoded event VM wiring is pending. |
| Branch progress math | `PORTED/PARTIAL` | Counts completed branch vector rows over `D.length / 2`. |
| `bqTask.mid` NPC marker/effect lifecycle | `AUDITED/PENDING` | Not part of this UI slice. |
| Exact `c.b` list-controller runtime | `PARTIAL` | Renderer/input remain branch-specific. |

## Smoke

New checkpoints:

- `panel_task_branch_open`
- `panel_task_branch_completed`
- `panel_task_branch_detail`

Expected PNGs:

- `rebuild_game/build_intro_demo/world_panel_full_task_branch/panel_task_branch_open.png`
- `rebuild_game/build_intro_demo/world_panel_full_task_branch/panel_task_branch_completed.png`
- `rebuild_game/build_intro_demo/world_panel_full_task_branch/panel_task_branch_detail.png`

## Next

Recommended next slice after visual review:

1. Audit `bqTask.mid` marker/effect draw path in `game.e.J()` if branch quest world markers matter now.
2. Otherwise continue to the next concrete panel route still `PENDING` in `368`.
