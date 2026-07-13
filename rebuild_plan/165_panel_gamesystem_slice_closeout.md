# 165 - Panel Gamesystem Slice Closeout

Date: 2026-07-09

Scope completed: narrow source-backed `gamesystem.ui` slice from `gamemenu b=0 -> P=14`.

## Implemented

- Extended `VqsvPanelRuntime` with two modes:
  - `GAMEMENU`
  - `GAMESYSTEM`
- Added source branch:
  - source: `game.h.l()` confirm with `b=0`
  - close `/data/ui/gamemenu.ui`
  - switch world state `P=14`
  - source calls `game.h.m()` and renders `/data/ui/gamesystem.ui`
- Added `gamesystem.ui` renderer:
  - UI layout: `modules/ui/original/gamesystem.ui` via `VqsvUiLayout.load("gamesystem.ui")`
  - sprite bank: `257`
  - frame widgets: `1,3,4,5`
  - row widgets: `6,7,8,9`
  - bottom softkey widgets: `10,11`
- Added source-shaped input:
  - up `4100`
  - down `8448`
  - confirm `196640`
  - back `262144`
- Implemented only safe branch:
  - `gamesystem selected=0` confirm closes `gamesystem.ui` and returns world `P=0`.
  - back closes `gamesystem.ui` and returns world `P=0`.
- Other `gamesystem.ui` rows are trace-only `PENDING`:
  - help
  - settings/help page
  - return main menu confirm/option flow

## Status

`PORTED/PARTIAL`

This slice ports `gamemenu -> gamesystem`, gamesystem navigation, continue, and back. It does not implement help/settings/main-menu option flow and does not add speed.

Remaining partials:

- `game.h.n()` case `1 -> P=20` help: `PENDING`
- `game.h.n()` case `2 -> P=21` settings/help page: `PENDING`
- `game.h.n()` case `3 -> option.ui/main menu`: `PENDING`
- Text scrolling/long-label behavior remains `PORTED/PARTIAL`.
- No original-vs-rebuild pixel compare, so no pixel-perfect claim.
- Speed toggle remains `REBUILD_POLICY/PENDING`.

## Verification

Build/check:

- `powershell -ExecutionPolicy Bypass -File .\build.ps1`
- `java "-Dvqsv.modules=..\modules" -jar build\libs\vqsv-liet-hoa-rebuild.jar --check`
- `java "-Dvqsv.modules=..\modules" -cp build\classes VqsvBattleDamageFormulaCheck`
- `git diff --check`
- mojibake scan on Java source and panel docs

Focused PNG smoke:

- `rebuild_game/build/smoke/panel_gamesystem_open_from_gamemenu.png`
- `rebuild_game/build/smoke/panel_gamesystem_navigation.png`
- `rebuild_game/build/smoke/panel_gamesystem_continue_returns_world.png`
- `rebuild_game/build/smoke/panel_gamesystem_back_returns_world.png`

Gamemenu regression PNG smoke:

- `rebuild_game/build/smoke/gamesystem_regression_panel_gamemenu_open_from_world.png`
- `rebuild_game/build/smoke/gamesystem_regression_panel_gamemenu_navigation.png`
- `rebuild_game/build/smoke/gamesystem_regression_panel_gamemenu_back_returns_world.png`

Route regression PNG smoke:

- `rebuild_game/build/smoke/gamesystem_regression_route_bunny_after_battle_task.png`
- `rebuild_game/build/smoke/gamesystem_regression_route_elder_after_battle_reward_state.png`
- `rebuild_game/build/smoke/gamesystem_regression_route_sophie_after_battle_branch.png`

## Next Step

Next concrete slice: original panel save flow.

Source path:

- `gamemenu selected b=5`
- `game.h.l()` hides widgets `11/12`
- switch world state `P=22`
- `game.h.K()`:
  - confirm -> `Dang luu...`
  - wait for `game.k.k()`
  - show `Luu thanh cong`
  - close `msgtip.ui` and `gamemenu.ui`
  - return `P=0`

Keep it narrow: wire this to existing `VqsvSaveRuntime` through a source-shaped `msgtip.ui` status. Do not add speed toggle until panel save flow passes PNG smoke.
