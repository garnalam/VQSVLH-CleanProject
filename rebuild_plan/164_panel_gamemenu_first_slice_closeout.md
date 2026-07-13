# 164 - Panel Gamemenu First Slice Closeout

Date: 2026-07-09

Scope completed: first source-backed panel slice for original world `gamemenu.ui`.

## Implemented

- Added `VqsvPanelRuntime`.
- Added source-shaped panel open state from free world:
  - Source anchor: `game.k.P == 0` back/menu key `262144` -> `game.h.k()` -> `P=6`.
  - Rebuild: `Scene.canOpenSourcePanel()` opens `panelRuntime` on `keyBack`.
- Added source-backed `gamemenu.ui` rendering:
  - UI layout: `modules/ui/original/gamemenu.ui` via `VqsvUiLayout.load("gamemenu.ui")`.
  - Sprite bank: `257`.
  - Menu rows: widgets `15,5,6,7,8,9`.
  - Title/icon token approximation: widgets `14`, token `#P605..#P610`.
  - Money/badge widgets: `18/19`.
- Added source-shaped input:
  - up `4100` -> selected row -1.
  - down `8448` -> selected row +1.
  - back `262144` -> close panel and return world.
  - confirm `196640` is trace-only/PENDING for this first slice.
- Changed bottom-left world softkey click from direct `openWorldPetstate()` to source panel open.
- Added focused PNG smoke checkpoints:
  - `panel_gamemenu_open_from_world`
  - `panel_gamemenu_click_softkey_open`
  - `panel_gamemenu_navigation`
  - `panel_gamemenu_back_returns_world`

## Status

`PORTED/PARTIAL`

The source entry/render/navigation/back loop is now present. Subpage branches are intentionally not implemented in this slice.

Remaining partials:

- `game.h.l()` confirm branches are `PENDING`:
  - `b=0 -> P=14 gamesystem.ui`
  - `b=1 -> P=7 petstate.ui`
  - `b=2 -> P=8 bag.ui`
  - `b=3 -> P=9 record.ui`
  - `b=4 -> P=10 task.ui`
  - `b=5 -> P=22 save`
- Text engine/list scrolling is `PORTED/PARTIAL`; long row labels stay clipped to source widget bounds in this slice.
- No original-vs-rebuild pixel compare has been done, so no pixel-perfect claim.
- Speed toggle is not implemented and remains `REBUILD_POLICY/PENDING`.

## Verification

Build/check:

- `powershell -ExecutionPolicy Bypass -File .\build.ps1`
- `java "-Dvqsv.modules=..\modules" -jar build\libs\vqsv-liet-hoa-rebuild.jar --check`
- `java "-Dvqsv.modules=..\modules" -cp build\classes VqsvBattleDamageFormulaCheck`
- `git diff --check`
- mojibake scan on Java source and panel audit doc

Focused PNG smoke:

- `rebuild_game/build/smoke/panel_gamemenu_open_from_world.png`
- `rebuild_game/build/smoke/panel_gamemenu_click_softkey_open.png`
- `rebuild_game/build/smoke/panel_gamemenu_navigation.png`
- `rebuild_game/build/smoke/panel_gamemenu_back_returns_world.png`

Regression PNG smoke:

- `rebuild_game/build/smoke/panel_regression_route_bunny_after_battle_task.png`
- `rebuild_game/build/smoke/panel_regression_route_elder_after_battle_reward_state.png`
- `rebuild_game/build/smoke/panel_regression_route_sophie_after_battle_branch.png`

## Next Step

Next concrete slice: implement `gamesystem.ui` branch only.

Source path:

- `gamemenu selected b=0`
- `game.h.l()` confirm
- close `gamemenu.ui`
- switch world state `P=14`
- call `game.h.m()/n()`
- render `/data/ui/gamesystem.ui`

Keep it narrow: continue/back/navigation first. Do not add speed toggle until original `gamesystem.ui` and panel save flow pass smoke.
