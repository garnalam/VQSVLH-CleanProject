# 178 - Panel petsetting c=0 item choice.ui render slice

Date: 2026-07-10

## Scope

Implemented the first code slice for:

- `petsetting c=0 -> /data/ui/choice.ui`
- render/navigate/back only
- confirm logs pending and does not mutate item/pet state

No item-use behavior, item count decrement, HP/PP/status mutation, or msgwarm success/warning loop is included.

## Source Chain

Primary source:

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`

Confirmed source path:

- In `game.h.X()`, `petsetting c=0`:
  - `f = 2`
  - `r = 0`
  - open `/data/ui/choice.ui`
  - close `/data/ui/petsetting.ui`
  - close `/data/ui/petstate.ui`
  - widget `8 = "Đạo cụ"`
  - widget `9 = "Số lượng"`
  - world/k softkey widget `59 = "Sử dụng"`
  - calls `be()`
- `be()` renders source `q.J` rows:
  - row id/count from `int[] {itemId, count, ...}`
  - icon from `aq.c[4][itemId][1]`
  - name from `aq.c[4][itemId][0]`
  - count from row `[1]`
  - description from `aq.c[4][selectedItem][2]`
- back while `f == 2`:
  - calls `e(b)`
  - closes `/data/ui/choice.ui`

## Rebuild Mapping

Rebuild maps source `q.J` as:

- `sourceBagItems` rows with `bagChannel != 0`
- sorted by item id
- `bagChannel == 0` rows, including capture balls, are excluded from this panel item-use list

This is source-shaped but not a full `q.K/q.J` vector runtime yet.

## Implemented

- Added `sourceItemChoiceVisible`, index, scroll state in `VqsvIntroDemo.Scene`.
- Added `openSourceItemChoiceFromPetSetting()`.
- Added navigation/back/pending confirm handler.
- Added `VqsvChoiceUiView` projection for panel item choice.
- Added `VqsvSceneView.renderSourceItemChoiceUi()` using decoded `choice.ui` widgets:
  - frame/static bands
  - headers `8/9`
  - rows `11/16/21/26/31`
  - icons `54..58` using sprite `258`
  - names/counts `13/14 + row * 5`
  - description `52/53`
  - softkeys `59/60`
- Added focused smoke checkpoints.

## Status

- `PORTED/PARTIAL`: source route, widget mapping, navigation, back.
- `PORTED/PARTIAL`: `q.J` is approximated via `sourceBagItems bagChannel != 0`; exact source vectors are not separate yet.
- `PENDING`: confirm item-use mutation.
- `PENDING`: source `msgwarm.ui` warning/success loop for validation results.
- `PENDING`: original-vs-rebuild pixel compare.

## Verification

Focused PNG smoke:

- `panel_petstate_petsetting_item_choice_open`
- `panel_petstate_petsetting_item_choice_navigation`
- `panel_petstate_petsetting_item_choice_back_returns_petstate`
- `panel_petstate_petsetting_item_choice_confirm_pending`

Regression to keep:

- `panel_petstate_petsetting_skill_open`
- `panel_petstate_petsetting_skill_back_returns_petstate`
- `panel_bag_open_from_gamemenu`
- `panel_save_success_status`
- `panel_gamesystem_option_confirm_no_returns_gamesystem`
- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`

## Next

Next recommended slice:

1. Add item-use confirm warning/success loop for the source-proven validation path.
2. Start with one low-risk item behavior already mirrored in battle item runtime, then expand behavior-by-behavior.
3. Defer `petsetting c=2` equipment until `q.L` and pet `c[5]` are modeled source-backed in rebuild state/save/petstate.
