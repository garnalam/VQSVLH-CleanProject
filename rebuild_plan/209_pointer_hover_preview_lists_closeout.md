# 209 - Pointer Hover Preview For Lists Closeout

Date: 2026-07-10

Scope: fix PC rebuild pointer behavior where list rows could only be previewed by clicking/confirming them first.

## Problem

Before this slice, Swing only forwarded `mousePressed` to the game scene. List row clicks changed the selected row and immediately queued confirm/use/buy. That made these menus awkward:

- battle skill list: user could not hover another skill to read its description before selecting it;
- battle shop/item lists: user could not preview row text/description before opening buy/use confirm;
- panel bag/task/pet/petmap lists: user could not preview/select a row with pointer movement.

## Changes

Files:

- `rebuild_game/src/main/java/VqsvIntroDemo.java`
- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvPanelRuntime.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Implemented:

- Added Swing `mouseMoved` / `mouseDragged` forwarding to `Scene.hover`.
- Added battle hover coordinates separate from click coordinates:
  - hover updates command/menu/skill/target selection only;
  - click still confirms.
- Added panel hover runtime:
  - gamemenu/gamesystem rows;
  - bag rows;
  - task rows;
  - petmap rows;
  - ride slots.
- Added world/source overlay hover:
  - petstate row;
  - petsetting row;
  - skill.ui row;
  - item/equipment choice.ui rows.
- Fixed battle `shopbuy.ui` row hit testing:
  - source-backed UI rows are spaced by 18 px (`y=100,118,136,154,172`), not the generic `choice.ui` 15 px spacing.

## Status

- Pointer hover row preview: PORTED/PARTIAL for current rebuilt PC UI runtime.
- Click/confirm behavior: preserved.
- Full Java ME pointer parity: PENDING; original mobile source mostly assumes key/game action paths, while PC pointer behavior is rebuild-specific ergonomics layered on source-backed UI coordinates.

## Verified PNG Smoke

Output directory:

- `rebuild_game/build/smoke/hover_preview_fix/`

Focused:

- `battle_skill_hover_preview_no_confirm`
- `battle_p11_shop_hover_preview_no_confirm`
- `panel_bag_hover_preview_no_confirm`
- `panel_task_hover_preview_no_confirm`
- `panel_petstate_hover_preview_no_confirm`

Regression:

- `battle_p11_shop_msgyn_open`
- `panel_bag_default_item_state17_open_petstate`
- `panel_petstate_petsetting_open`
- `panel_task_navigation`
- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`

Core:

- `build.ps1`
- `com.vqsv.rebuild.Main --check`
- `VqsvBattleDamageFormulaCheck`
- Java mojibake/SMS scan

## Next recommended step

Continue UI pointer polish where it is still keyboard-only: tab hover/click on `bag.ui`, `task.ui`, and `petmap.ui`, then quantity left/right pointer controls for `msgyn.ui` if the user wants mouse-first PC behavior.
