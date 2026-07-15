# 369 - World Panel Task UI / Task Option Matrix

Date: 2026-07-15

Scope: `task.ui` and `taskOption.ui` only. No live client. Smoke PNG only.

## Source Facts

| Area | Source | Behavior | Rebuild status |
| --- | --- | --- | --- |
| Open task list | `game.k.U()` | Opens `/data/ui/task.ui`, closes `/data/ui/gamemenu.ui`, sets tab `b`, resets cursor/scroll, then calls `bc()` and `bd()`. | `PORTED/PARTIAL` |
| Task tab/progress | `game.k.bc()` | Tab `0` uses `game.e.E` main task rows/progress; tab `1` uses `game.e.D/F/H` branch tasks/progress. | `PORTED/PARTIAL` |
| Task list/details | `game.k.bd()` | Fills row widgets `12/13/14`, `17/18/19`, `22/23/24`, `27/28/29`, `32/33/34`; detail widget `36`; scrollbar thumb `40`. | `PORTED/PARTIAL` |
| Task input | `game.k.V()` | Up/down navigate list; left/right switch tab; back mask `983072` closes task and returns to previous panel/world state; key `10` closes `/data/ui/task.ui` and returns state `0`. | `PORTED/PARTIAL` |
| Task option open | `game.e` opcode `49` -> `game.k.a(...)` | Builds two option strings plus reward arrays, then opens `/data/ui/taskOption.ui`. This is event-script popup, not the task-list confirm path. | `PORTED/PARTIAL` |
| Task option input | `game.k.aG()` | Up/down updates selected `b` from list controller; confirm mask `196640` closes popup and returns selected option; back mask `262144` closes popup and returns `1`. | `PORTED/PARTIAL` |

## UI Layout Facts

### `task.ui`

| Widget ids | Role | Status |
| --- | --- | --- |
| `1/3/4/5/35/39/40/41/42` | Main frame, detail box, scrollbar, softkeys. | `PORTED/PARTIAL` |
| `6/7`, `8/9` | Main/branch tabs and labels. | `PORTED/PARTIAL` |
| `11/16/21/26/31` | Row backgrounds. | `PORTED/PARTIAL` |
| `12/17/22/27/32` | Row numbers. | `PORTED/PARTIAL` |
| `13/18/23/28/33` | Row names. | `PORTED/PARTIAL` |
| `14/19/24/29/34` | Row completion status. | `PORTED/PARTIAL` |
| `36` | Selected task detail text. | `PORTED/PARTIAL` |
| `37/38` | Progress label/value. | `PORTED/PARTIAL` |

### `taskOption.ui`

| Widget ids | Role | Status |
| --- | --- | --- |
| `1/2/3/4/9` | Popup frame and reward panel frame. | `PORTED/PARTIAL` |
| `10/11` | Two option row backgrounds. | `PORTED/PARTIAL` |
| `7/8` | Source cursor/arrow icons for selected row. | `PORTED/PARTIAL` |
| `17/18` | Option text. | `PORTED/PARTIAL` |
| `12` | Reward title text `Thưởng`. | `PORTED/PARTIAL` |
| `13/15` | Reward icons, source creates `c.g` and uses sprite `258` for item/equipment-like icons or sprite `257` for money/badge cells. | `PORTED/PARTIAL` |
| `14/16/21` | Reward labels/summary. | `PORTED/PARTIAL` |
| `19/20` | Confirm/back softkeys. | `PORTED/PARTIAL` |

## Rebuild Changes

- `task.ui` confirm now follows `game.k.V()` key `10`: close `task.ui` and return to world state, not a guessed task detail popup.
- Added `VqsvPanelRuntime.Mode.TASK_OPTION`.
- Added source-shaped `taskOption.ui` renderer and input lifecycle for event opcode `49` style popups.
- Added smoke-only entrypoint `openTaskOptionForSmoke(...)` for event popup verification until the decoded event VM calls opcode `49` directly.
- Kept task mutation/event branching trace-only: downstream `var2_2.b((byte)(af[result] - 2))` is not implemented in this slice.

## Status

| Feature | Status | Note |
| --- | --- | --- |
| `task.ui` list/tab/detail/progress | `PORTED/PARTIAL` | Uses decoded widget bounds and source task scripts. |
| `task.ui` key `10` close behavior | `PORTED` | Smoke verifies close-to-world trace. |
| `taskOption.ui` popup frame/options/reward render | `PORTED/PARTIAL` | Source-shaped renderer, not generic UI VM. |
| `taskOption.ui` aG navigation/back/confirm | `PORTED/PARTIAL` | Smoke verifies selected result trace and close path. |
| Event opcode `49` full script branch mutation | `PENDING` | Needs event VM integration; current popup entry is smoke/event-wrapper ready. |
| Exact Java ME font baseline and full `c.b` list-controller runtime | `PARTIAL` | Current implementation is branch-specific. |

## Smoke

New checkpoints:

- `panel_task_confirm_closes_world`
- `panel_task_option_open`
- `panel_task_option_navigation`
- `panel_task_option_back_returns_task`
- `panel_task_option_confirm_closes`

Expected output directory for focused run:

`rebuild_game/build_intro_demo/world_panel_full_task_option`

## Next

Recommended next route after this slice:

1. Run `world_panel_full` and inspect the new PNGs.
2. If `taskOption.ui` looks acceptable, audit another concrete panel route from `368_world_panel_function_route_matrix.md`.
3. Do not wire generic decoded event VM yet unless a concrete opcode route requires it.
