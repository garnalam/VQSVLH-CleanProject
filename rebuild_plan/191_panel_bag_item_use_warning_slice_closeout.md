# 191 - Panel bag item-use warning slice closeout

## Scope

Started top-level `bag.ui` item-use from panel.

This slice only ports the first source-proven world bag item-use branch:

`gamemenu b=2 -> P=8 -> bag.ui tab b=0 -> itemId 0..3 -> msgwarm.ui cannot-use warning`

No inventory mutation is performed in this slice.

## Source chain

- `game.h.Y()` opens `/data/ui/bag.ui`, sets `b=0`, calls `bi()`, selects widget `5`, and sets widget `14` to `Vat pham`.
- `game.h.ac()` handles bag input.
- In `b == 0`, source selects from `q.K + q.J`.
- For item ids `0`, `1`, `2`, `3`, source opens `/data/ui/msgwarm.ui` and shows:
  - `Khong the su dung`
  - `Nhan nut 5 de tiep tuc`
- Source sets `f = 1`; confirming the warning closes `msgwarm.ui` and remains in `bag.ui`.

## Implemented behavior

| Case | Rebuild status |
| --- | --- |
| Open top-level `bag.ui` from gamemenu row `b=2` | `PORTED/PARTIAL`, existing slice |
| Confirm item id `0..3` in bag tab 0 | `PORTED` warning-only branch |
| Warning closes and returns to `bag.ui` | `PORTED` |
| Inventory count unchanged | `PORTED` |

## Rebuild files changed

- `rebuild_game/src/main/java/VqsvPanelRuntime.java`
  - Added bag warning mode for source `f=1`.
  - Added `itemId 0..3` warning path.
- `rebuild_game/src/main/java/VqsvText.java`
  - Added exact top-level panel bag text `PANEL_BAG_CANNOT_USE`.
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`
  - Added focused PNG checkpoints:
    - `panel_bag_item_cannot_use_warning`
    - `panel_bag_item_cannot_use_returns_bag`
- `rebuild_game/src/main/java/VqsvSceneView.java`
  - Fixed panel/text stacking: `panelRuntime` now renders before `TextBox`, so `msgwarm.ui` appears above `bag.ui`.

## Verification

Build/check:

- `.\build.ps1`: PASS
- `java "-Dvqsv.modules=..\modules" -jar build\libs\vqsv-liet-hoa-rebuild.jar --check`: PASS
- `java "-Dvqsv.modules=..\modules" -cp build\classes VqsvBattleDamageFormulaCheck`: PASS
- Java source mojibake scan: PASS, no hits
- `git diff --check`: PASS, no whitespace errors

Focused PNG smoke:

- `build/smoke/panel_bag_item_use/panel_bag_item_cannot_use_warning.png`: PASS
- `build/smoke/panel_bag_item_use/panel_bag_item_cannot_use_returns_bag.png`: PASS

Visual note:

- Initial smoke exposed a stacking bug where `bag.ui` rendered over the warning text box.
- The final focused PNGs verify `msgwarm.ui` renders on top of `bag.ui`.

Regression PNG smoke:

- `panel_bag_open_from_gamemenu`: PASS
- `panel_bag_navigation`: PASS
- `panel_bag_back_returns_gamemenu`: PASS
- `panel_petstate_petsetting_active_switch_success`: PASS
- `panel_petstate_petsetting_evolve_open`: PASS
- `panel_petstate_petsetting_release_success_removes_pet`: PASS
- `panel_petstate_petsetting_item_choice_success_msg`: PASS
- `panel_petstate_petsetting_equipment_choice_equip_success_msg`: PASS
- `panel_petstate_petsetting_skill_open`: PASS
- `panel_save_success_status`: PASS
- `route_sophie_after_battle_branch`: PASS
- `route_bunny_after_battle_task`: PASS
- `route_elder_after_battle_reward_state`: PASS

## Current status

- Top-level `bag.ui` open/render/navigation/back: `PORTED/PARTIAL`.
- Top-level `bag.ui` item ids `0..3` cannot-use warning: `PORTED`.
- Top-level `bag.ui` item id `13` avoid-monster pill: `PENDING`.
- Top-level `bag.ui` item id `14` egg-hatch timer item: `PENDING`.
- Top-level `bag.ui` default item path to state `17` target selection: `PENDING`.
- Other bag tabs `b=1/2/3`: `PENDING/PARTIAL`.

## Recommended next step

Audit and port the next smallest top-level bag item-use branch:

1. `itemId 13` avoid-monster pill warning/success loop, because it has clear source messages and simple count/timer state.
2. Defer `itemId 14` egg-hatch and default `state 17` until their downstream state mutations are audited.
