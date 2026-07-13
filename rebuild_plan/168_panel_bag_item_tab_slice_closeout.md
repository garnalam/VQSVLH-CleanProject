# 168 Panel Bag Item Tab Slice Closeout

Date: 2026-07-09

## Scope

Implemented the first source-backed slice for the original panel bag path:

- `gamemenu.ui` row `b=2`
- source target `P=8`
- `game.h.Y()` load `/data/ui/bag.ui`
- `b=0` item tab only
- render/navigate/back only

Item use is intentionally not implemented in this slice.

## Source Chain

Source files read:

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/ui/decoded/data__ui__bag.ui.json`

Confirmed source path:

- `game.h.l()` on gamemenu confirm `b=2`
- `o.m()`
- `o.a((byte)8)`
- close `gamemenu.ui`
- `game.k.a(byte 8)` enters `game.h.Y()`
- `game.h.Y()` loads `/data/ui/bag.ui`, sets `b=0`, calls `bi()`
- `bi()` resets container scroll/cursor for `8 + b * 39`, then calls `bj()`
- `bj()` with `b=0` calls `bk()`
- `bk()` renders item tab from `q.K + q.J`

Source widget mapping used:

- root/top frame: `1`, `2`, `3`, `4`, `5`, `6`, `7`
- item tab container: `8`
- tabs: `9`, `10`, `11`, `12`
- headers: `14`, `15`
- five rows:
  - row background: `17`, `22`, `27`, `32`, `37`
  - icon: `18`, `23`, `28`, `33`, `38`
  - name: `19`, `24`, `29`, `34`, `39`
  - count: `20`, `25`, `30`, `35`, `40`
- description: `46`
- scrollbar: `42`, `43`

## Rebuild Changes

Files changed:

- `rebuild_game/src/main/java/VqsvPanelRuntime.java`
- `rebuild_game/src/main/java/VqsvSourceModels.java`
- `rebuild_game/src/main/java/VqsvSourceOps.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Behavior:

- `gamemenu b=2` now opens `BAG` mode.
- Bag shows only source runtime items with `bagChannel == 0` and `count > 0`.
- The placeholder `item0 count0 keepAtZero=true` is not shown as a real row.
- Up/down moves the row cursor.
- Back returns to `gamemenu.ui` with selected row `2`.
- Confirm/use logs `PENDING` and does not mutate inventory.

## Status

`PORTING STATUS: PORTED/PARTIAL`

Ported:

- source route `gamemenu b=2 -> P=8 -> game.h.Y()`
- `bag.ui` item-tab render using source widget ids
- row cursor movement
- back to gamemenu row 2
- state-backed item counts

Partial:

- source `q.K + q.J` split is approximated by current `sourceBagItems` plus `bagChannel == 0`.
- source `aq.c[4]` item DB is only mapped for the currently used rebuild items.
- icon/description are source-shaped fields, but not a full original item database dump.
- scrollbar is minimum source-shaped render, not full `al` runtime.

Pending:

- tabs `b=1`, `b=2`, `b=3`
- item use branches in `game.h.ac()`
- `msgwarm.ui` warnings from bag item use
- key/special/equipment bag pages
- exact `q.K`, `q.J`, `q.L`, `q.M`, `q.N` storage parity
- original-vs-rebuild pixel compare

## Smoke

Focused PNG smoke:

- `panel_bag_open_from_gamemenu`
- `panel_bag_navigation`
- `panel_bag_back_returns_gamemenu`

Output:

- `rebuild_game/build/smoke/panel_bag_open_from_gamemenu.png`
- `rebuild_game/build/smoke/panel_bag_navigation.png`
- `rebuild_game/build/smoke/panel_bag_back_returns_gamemenu.png`

Regression smoke run:

- `panel_gamemenu_open_from_world`
- `panel_gamesystem_navigation`
- `panel_save_prompt_from_gamemenu`
- `panel_save_success_status`
- `panel_petstate_open_from_gamemenu`
- `panel_petstate_navigation`
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

Next should be one of these, in order:

1. `gamemenu b=4 -> P=10 -> task.ui`, render/navigate/back only.
2. Or, if staying inside bag, audit `game.h.ac()` item use for item tab `b=0` and implement only one proven warning/use branch.

Do not add speed toggle yet. Finish original panel branches first.
