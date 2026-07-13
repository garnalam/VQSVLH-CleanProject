# 169 Panel Task Slice Closeout

Date: 2026-07-09

## Scope

Implemented the first source-backed slice for original panel task path:

- `gamemenu.ui` row `b=4`
- source target `P=10`
- `game.h.R()` loads `/data/ui/task.ui`
- render main/branch task tabs
- navigate rows
- switch tabs with left/right
- back to `gamemenu.ui` selected row `4`

Task option/details/claim behavior is intentionally not implemented in this slice.

## Source Chain

Source files read:

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/c.java`
- `modules/ui/decoded/data__ui__task.ui.json`
- `modules/script/decoded/data__script__mTask.mid.json`
- `modules/script/decoded/data__script__bTask.mid.json`

Confirmed source path:

- `game.h.l()` on `gamemenu b=4`
- sets `b=0`
- calls `o.a((byte)10)`
- closes `/data/ui/gamemenu.ui`
- `game.k.a(byte 10)` calls `game.h.R()`
- `game.h.R()` loads `/data/ui/task.ui`, sets cursor state, calls `ba()` then `bb()`
- `game.h.S()` handles:
  - up `4100`
  - down `8448`
  - left `16400`
  - right `32832`
  - back `983072` to `P=6` when previous state is panel menu

## Widget Mapping

Task UI source widgets used:

- frame/title: `1`, `2`, `3`, `4`, `5`
- tabs: `6`, `7`, labels `8`, `9`
- five rows:
  - row background: `11`, `16`, `21`, `26`, `31`
  - number: `12`, `17`, `22`, `27`, `32`
  - title: `13`, `18`, `23`, `28`, `33`
  - status: `14`, `19`, `24`, `29`, `34`
- detail box: `35`, `36`
- progress label/value: `37`, `38`
- scrollbar: `39`, `40`
- softkeys: `41`, `42`

## Rebuild Changes

Files changed:

- `rebuild_game/src/main/java/VqsvPanelRuntime.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Behavior:

- `gamemenu b=4` opens `TASK` mode.
- Task title/detail text is loaded from decoded source scripts:
  - `mTask.mid.json`
  - `bTask.mid.json`
- Main task cursor is source-shaped from current rebuild event state.
- Branch task completion is source-shaped for the currently ported Bunny path.
- Up/down navigates task rows.
- Left/right switches main/branch tabs.
- Back returns to `gamemenu selected=4`.
- Confirm logs `PENDING`; no mutation, reward, or `taskOption.ui` logic.

## Status

`PORTING STATUS: PORTED/PARTIAL`

Ported:

- source route `gamemenu b=4 -> P=10 -> game.h.R()`
- `task.ui` frame/tabs/list/detail/progress render from source widget ids
- main/branch task text loaded from source decoded scripts
- up/down row navigation
- left/right tab navigation
- back to gamemenu row 4

Partial:

- full `game.c.t`, `game.c.u`, `game.c.s` task state is approximated from rebuild event state.
- status text `Hoàn thành` is clipped by the original narrow widget, because source layout width is built for shorter original text.
- scrollbar is minimum source-shaped render, not full `al` runtime.

Pending:

- `taskOption.ui`
- task confirm/details flow
- full branch task state `game.c.s`
- exact `game.c.r/q` task progress parity
- original-vs-rebuild pixel compare

## Smoke

Focused PNG smoke:

- `panel_task_open_from_gamemenu`
- `panel_task_tab_branch`
- `panel_task_navigation`
- `panel_task_back_returns_gamemenu`

Output:

- `rebuild_game/build/smoke/panel_task_open_from_gamemenu.png`
- `rebuild_game/build/smoke/panel_task_tab_branch.png`
- `rebuild_game/build/smoke/panel_task_navigation.png`
- `rebuild_game/build/smoke/panel_task_back_returns_gamemenu.png`

Regression smoke run:

- `panel_gamemenu_open_from_world`
- `panel_gamesystem_navigation`
- `panel_save_prompt_from_gamemenu`
- `panel_save_success_status`
- `panel_petstate_open_from_gamemenu`
- `panel_bag_open_from_gamemenu`
- `panel_bag_back_returns_gamemenu`
- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`

Validation:

- `build.ps1` passed
- `com.vqsv.rebuild.Main --check` passed
- `VqsvBattleDamageFormulaCheck` passed
- `git diff --check` passed
- Java mojibake scan passed

## Recommended Next Step

Next source panel slice:

1. `gamemenu b=3 -> P=9 -> petmap.ui`, render/navigate/back only.

Alternative if staying in task:

2. Audit `taskOption.ui` and implement task confirm/details as a separate slice.

Do not add speed toggle until original panel branches are stable.
