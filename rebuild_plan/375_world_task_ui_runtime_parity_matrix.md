# 375 - World `task.ui` Runtime Parity Matrix

Date: 2026-07-15

Scope: focused audit/port of `/data/ui/task.ui` UI runtime parity. This document covers visual/layout/input parity only. Branch quest lifecycle is tracked in `374_world_branch_quest_port_inventory_matrix.md`.

## Source Facts

| Source | Fact | Status |
| --- | --- | --- |
| `game.k.U()` | Opens `/data/ui/task.ui` with sprite `257`, closes `/data/ui/gamemenu.ui`, sets tab `b=0`, then calls `bc()` and `bd()`. | `AUDITED` |
| `game.k.bc()` main tab | Main tab uses `game.e.G`; selected index `h = G`, scroll `v = G - 4` clamped; widget `37` label becomes main completion text; widget `38` becomes progress percent. | `PORTED` |
| `game.k.bc()` branch tab | Branch tab uses `game.e.H/F`; selected index starts `0`; widget `37` label becomes branch completion text; widget `38` becomes branch completion percent. | `PORTED/PARTIAL` |
| `game.k.bd()` rows | Five visible row groups are filled through widget ids `12/13/14`, `17/18/19`, `22/23/24`, `27/28/29`, `32/33/34`; row background widgets are `11/16/21/26/31`. | `PORTED` |
| `game.k.bd()` detail | Widget `36` displays selected task detail from second half of `mTask.mid` or `bTask.mid`; decoded widget `36` has `d=true`. | `PORTED` |
| `game.k.bd()` scrollbar | Widget `40` y is moved by `104 + (h << 6) / totalCount` when list is scrollable. | `PORTED/PARTIAL` |
| `game.k.bc()` main percent | Widget `38` has two source formulas: normal `n2 / 50 + "." + decimal + "%"`, premium `n2 / 10 + "." + n2 % 10 + "%"`, where `n2 = G * 1000 / (E.length / 2)`. | `PORTED/PARTIAL` |
| `game.k.V()` input | Up/down navigate list; left/right switch main/branch tabs; back returns to gamemenu; confirm closes to world. | `PORTED` |
| `task.ui` widgets `41/42` | Softkeys use cell `28` and text alignment `b=4`. | `PORTED/PARTIAL` |

## Widget Matrix

| Widget(s) | Source role | Rebuild equivalent | Status | Notes |
| --- | --- | --- | --- | --- |
| `1/3/4/5` | Main frame/background bands. | `drawTaskFrame()` draws source cells/bands. | `PORTED/PARTIAL` | Uses decoded widget bounds and source sprite cells, but not full widget VM. |
| `2` | Title text. | `renderTask()` draws decoded title. | `PORTED/PARTIAL` | Existing renderer still uses manual wide offset because source title widget width is narrow. |
| `6/7` | Main/branch tab cell state, image `108`, alt `107`. | `drawCellState()` uses selected tab state. | `PORTED` | Smoke checks main and branch tab visible. |
| `8/9` | Main/branch tab labels. | Selected tab uses source mutation color `11290624`; inactive uses widget inactive palette. | `PORTED/PARTIAL` | Exact Java ME text baseline still partial. |
| `11/16/21/26/31` | Five row backgrounds, image `26`, alt `25`. | Renderer selects alt cell for selected row. | `PORTED` | Source-shaped row selection covered. |
| `12/17/22/27/32` | Row numbers. | Drawn from row number, selected row uses active palette. | `PORTED` | Uses widget bounds and alignment. |
| `13/18/23/28/33` | Row task title. | Drawn from `mTask.mid` / `bTask.mid`, clipped before status column; selected overlong title uses one-line marquee. | `PORTED/PARTIAL` | Source-shaped for `task.ui`; not a generic `game.h` text widget VM yet. |
| `14/19/24/29/34` | Row completed status. | Draws `Hoàn thành` for completed rows. | `PORTED` | Selected/inactive colors now use widget active/inactive palettes. |
| `35/36` | Detail frame/text. | Detail text uses widget `36` wrapped/clipped and source-shaped vertical scroll when content exceeds the box. | `PORTED/PARTIAL` | Exact Java ME inner scroll timing is still not pixel-compared. |
| `37/38` | Completion label/value. | Main/branch labels and percent values drawn from source-shaped `G/H/F`; main tab normal/premium formulas are represented. | `PORTED/PARTIAL` | Premium flag is modeled as `Scene.sourcePremiumUiPercent`, not a full global `a.a.i` runtime. |
| `39/40` | Scrollbar track/thumb. | Thumb y follows source formula for task list; track uses widget palette. | `PORTED/PARTIAL` | Exact source widget move/cell render is source-shaped, not full runtime mutation. |
| `41/42` | Confirm/back softkeys. | Rendered through `drawSoftkey()` with decoded cell and centered text. | `PORTED/PARTIAL` | Exact Java ME baseline is still partial. |

## Code Changes

| File | Change | Status |
| --- | --- | --- |
| `VqsvPanelRuntime.java` | Main task opening now selects `mainTaskCursor(s)` and keeps it visible, matching source `h=game.e.G`, `v=G-4`. | `PORTED` |
| `VqsvPanelRuntime.java` | Switching back to main tab also restores selection from `G`, not row `0`. | `PORTED` |
| `VqsvPanelRuntime.java` | Task tab selected text uses source mutation color `11290624`. | `PORTED/PARTIAL` |
| `VqsvPanelRuntime.java` | Row number/title/status selected/inactive colors now use active/inactive widget palettes instead of hardcoded orange. | `PORTED/PARTIAL` |
| `VqsvPanelRuntime.java` | Detail widget `36` now wraps/clips text using source-shaped `d=true` behavior. | `PORTED/PARTIAL` |
| `VqsvPanelRuntime.java` | Task softkeys now render with decoded softkey cell `28` and centered text. | `PORTED/PARTIAL` |
| `VqsvPanelRuntime.java` | Task scrollbar thumb y now follows source formula `104 + (selected << 6) / total`. | `PORTED/PARTIAL` |
| `VqsvPanelRuntime.java` | Selected task row title now uses clipped one-line marquee for overlong source strings. | `PORTED/PARTIAL` |
| `VqsvPanelRuntime.java` | Task detail widget now scrolls vertically when wrapped text exceeds widget `36` height. | `PORTED/PARTIAL` |
| `VqsvPanelRuntime.java` / `VqsvIntroDemo.java` | Main completion percent now supports source normal and premium `a.a.i` formulas through `sourcePremiumUiPercent`. | `PORTED/PARTIAL` |
| `VqsvSmokeHarness.java` | Added focused smoke checkpoints for main layout, scrollbar, branch layout, softkey/back, marquee/detail/percent. | `PORTED` |

## Smoke Matrix

| Checkpoint | What it proves | Status |
| --- | --- | --- |
| `panel_task_ui_main_source_layout` | Frame, main tab, selected row from `G`, wrapped detail, and softkeys render. | `PASS` |
| `panel_task_ui_main_scrollbar` | Main tab opens at source `G`, list scrolls to keep source selected row visible, scrollbar/thumb visible. | `PASS` |
| `panel_task_ui_branch_source_layout` | Branch tab selected state, branch row from `VqsvBranchQuestRuntime`, and detail box render. | `PASS` |
| `panel_task_ui_softkey_back` | Softkey render exists and back returns to gamemenu selected row `4`. | `PASS` |
| `panel_task_ui_marquee_detail_percent` | Long selected row title is marquee-clipped, detail remains clipped/scrollable, and normal/premium percent branches differ. | `PASS` |
| `panel_task_open_from_gamemenu` | Opening task.ui from gamemenu follows source path and now selects row `G`. | `PASS` |
| `panel_task_navigation` | Up/down source list navigation updates selected row and trace. | `PASS` |
| `panel_task_hover_preview_no_confirm` | Mouse hover can update selection without confirming/opening dialogs. | `PASS` |
| `panel_task_confirm_closes_world` | Confirm key closes `task.ui` back to world. | `PASS` |

Latest run:

- `world_panel_full`: PASS `68/68`, including `panel_task_ui_marquee_detail_percent`.
- `com.vqsv.rebuild.Main --check`: PASS.
- Mojibake scan on edited Java files: no matches.

Key PNGs:

- `rebuild_game/build_intro_demo/world_panel_full_task_ui_parity/panel_task_ui_main_source_layout.png`
- `rebuild_game/build_intro_demo/world_panel_full_task_ui_parity/panel_task_ui_main_scrollbar.png`
- `rebuild_game/build_intro_demo/world_panel_full_task_ui_parity/panel_task_ui_branch_source_layout.png`
- `rebuild_game/build_intro_demo/world_panel_full_task_ui_parity/panel_task_ui_softkey_back.png`

## Remaining Partial / Pending

| Area | Status | Reason |
| --- | --- | --- |
| Full `game.h/game.k` widget runtime | `PARTIAL` | Renderer still implements task.ui explicitly instead of executing the full Java ME widget tree. |
| Exact Java ME text baseline/clip | `PARTIAL` | Smoke verifies visible layout, not pixel-perfect MIDP baseline. |
| Row-title marquee for overlong strings | `PORTED/PARTIAL` | Selected row title now scrolls inside the decoded task title width; not generalized to every text widget. |
| Detail text exact scroll/marquee | `PARTIAL` | Widget `36` wraps and scrolls when too tall; exact Java ME timing remains not pixel-compared. |
| Source premium percent branch `a.a.i` | `PORTED/PARTIAL` | Formula is ported through a Scene flag; full global premium/runtime ownership is still outside this slice. |
| Full task data lifecycle | `PARTIAL` | UI parity is improved, but task production/completion for all rows is tracked separately. |
