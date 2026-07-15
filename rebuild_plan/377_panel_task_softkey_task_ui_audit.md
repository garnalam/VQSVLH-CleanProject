# 377 Panel Task Softkey Task UI Audit

Scope: `task.ui` opened from the softkey/gamemenu task entry only. No world quest trigger, map, battle, or generic quest VM work belongs to this slice.

## Source Facts

| Source | Fact | Rebuild mapping | Status |
|---|---|---|---|
| `game.k.U()` / old `game.h` panel runtime | Opens `/data/ui/task.ui`, returns from `/data/ui/gamemenu.ui`, sets tab `b=0`, cursor state, then calls `bc()` and `bd()`. | `VqsvPanelRuntime` opens `Mode.TASK` from gamemenu selected task row. | `PORTED/PARTIAL` |
| `game.k.bc()` main tab | Main task tab uses `game.e.G` and `game.e.E`; if `G` is not past end, list count is `G+1`, selected row is `G`; widget `37` label is main completion text; widget `38` is percent. | `sourceMainTaskProgress` is rebuild `game.e.G`; `mTask.mid` rows are rebuild `game.e.E`. | `PORTED` |
| `game.k.bd()` main rows | Widgets `12/13/14`, `17/18/19`, etc. draw number/title/status. Rows before `G` show `Hoàn thành`; row `G` is active without status. Widget `36` shows detail text from `E[half + selected]`. | `TaskRow` renders numbers/title/status/detail from `mTask.mid`. | `PORTED/PARTIAL` |
| `game.k.bc()` branch tab | Branch tab uses `game.e.H`, `game.e.F`, and `game.e.D`. Widget `37` label becomes branch completion text. Percent is completed branch count over `D.length/2`. | `VqsvBranchQuestRuntime.tasks()` is rebuild `F/H`; `bTask.mid` rows are rebuild `D`. | `PORTED/PARTIAL` |
| `game.k.bd()` branch rows | Row title is `D[F[row][0]]`; completed status appears only when `F[row][1] == 3`; detail is `D[D.length/2 + F[selected][0]]`. | `taskRowsForRender(..., tab=1)` binds to actual source branch task list, not mock rows. | `PORTED/PARTIAL` |
| `task.ui` decoded widgets | Frame/title/tab rows/detail/progress/scrollbar/softkeys use widgets `1/2`, `6/7/8/9`, `11..34`, `35/36`, `37/38`, `39/40`, `41/42`. | Renderer draws those widget regions and uses source colors where decoded. | `PORTED/PARTIAL` |
| `game.k.V()` input | Up/down move list, left/right switch tab, back/right softkey closes task.ui back to gamemenu, key `10` closes to world. | Keyboard + mouse tab/row/softkey handling implemented. | `PORTED/PARTIAL` |

## Current Softkey Smoke

Dedicated suite: `panel_task_softkey`.

| PNG | Assertion focus |
|---|---|
| `panel_task_main_open.png` | Main tab opens from softkey/gamemenu, selected row follows `G`, rows bind to `E`. |
| `panel_task_main_detail.png` | Widget `36` detail and widgets `37/38` progress are visible. |
| `panel_task_branch_open.png` | Branch tab binds to `F/H` and `D`, no seeded mock row in render path. |
| `panel_task_branch_completed.png` | `F[row][1] == 3` renders `Hoàn thành` and detail. |
| `panel_task_softkey_back.png` | Back softkey closes task.ui to gamemenu selected task row. |

## Remaining Partial/Pending

- `PORTED/PARTIAL`: generic `game.h/game.k` widget runtime is still not 100%; renderer is source-shaped for `task.ui`.
- `PENDING`: full quest VM is out of scope here.
- `PENDING`: full branch quest lifecycle for all `bqTask.mid` rows is out of scope here.
- `UNKNOWN`: exact Java ME font baseline/clip parity can still need pixel compare, but current widget bounds and colors are source-backed.
