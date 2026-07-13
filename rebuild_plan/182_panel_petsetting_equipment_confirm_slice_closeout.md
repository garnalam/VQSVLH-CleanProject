# 182 - Panel petsetting c=2 equipment confirm slice

Date: 2026-07-10

## Scope

Implemented confirm mutation for:

- `petsetting c=2 -> choice.ui`
- equip selected accessory
- unequip selected accessory
- transfer accessory from another pet to selected pet
- success `msgwarm.ui` loop, source-shaped `f=3 -> f=2`

This builds on:

- `180_panel_petsetting_equipment_choice_audit.md`
- `181_panel_petsetting_equipment_choice_render_slice_closeout.md`

## Source Chain

Primary source:

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`

Confirmed source path in `game.h.X()` case `c == 2`:

- while `f == 2`, confirm selected `q.L[h]`
- if selected pet already wears item:
  - `q.l(q.z[b].c[5])`
  - `q.z[b].c[5] = -1`
  - `bd()`
  - `E()`
  - message `"Thanh cong do xuong"`
  - `f = 3`
- otherwise:
  - `q.f(itemId, b)`
  - `bd()`
  - `E()`
  - message `"Thanh cong mang theo"`
  - `f = 3`
- next confirm while `f != 2`:
  - `f = 2`
  - `o.m()`
  - `e(b)`
  - `F()`
  - close `/data/ui/choice.ui`

Confirmed source mutation in `game.g`:

- `l(itemId)` marks `q.L` row `[1] = 0`.
- `f(itemId, petIndex)`:
  - clears selected pet old `c[5]` and old equipment flag
  - if target item is worn elsewhere, clears the other pet/storage slot
  - marks selected `q.L` row `[1] = 1`
  - sets selected pet `c[5] = itemId`

## Implemented

Files:

- `rebuild_game/src/main/java/VqsvIntroDemo.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`
- `rebuild_game/src/main/java/VqsvText.java`

Runtime behavior:

- Added equipment success constants:
  - `Thanh cong mang theo`
  - `Thanh cong do xuong`
- Added `sourceEquipmentChoiceMessageMode`.
- Added `confirmSourceEquipmentChoice()`.
- Added source-shaped helpers:
  - `sourceUnequipEquipment(...)`
  - `sourceEquipEquipment(...)`
  - `sourceEquipmentRow(...)`
  - `sourcePetIndexWearingEquipment(...)`
  - `setSourcePetEquipmentId(...)`
- Rebuild selected pet equipment id continues to mirror source `c[5]` through `SourcePetState.sourcePayload[2]`.
- Success message keeps `choice.ui` visible underneath, then confirm closes `msgwarm.ui`, closes `choice.ui`, refreshes `petstate.ui`, and clears mode.

## Status

- `PORTED/PARTIAL`: `game.h.X()` equipment confirm loop.
- `PORTED/PARTIAL`: `game.g.l(itemId)` unequip semantics for party pet.
- `PORTED/PARTIAL`: `game.g.f(itemId, petIndex)` equip and transfer semantics for party pets.
- `PENDING`: transfer from/to source bank `O` storage slots; rebuild slice currently covers party pets.
- `PENDING`: save/load of `q.L`.
- `PENDING`: equipment stat/passive effects in battle.
- `PENDING`: original-vs-rebuild pixel compare.
- `PENDING`: `choice.ui/msgwarm.ui` long text clipping polish.

## Verification

Build/check:

- `build.ps1` pass
- `com.vqsv.rebuild.Main --check` pass
- `VqsvBattleDamageFormulaCheck` pass
- `git diff --check` pass with existing CRLF warnings only
- mojibake scan Java source: no output

Focused PNG smoke:

- `panel_petstate_petsetting_equipment_choice_unequip_success_msg`
- `panel_petstate_petsetting_equipment_choice_equip_success_msg`
- `panel_petstate_petsetting_equipment_choice_transfer_success_msg`
- `panel_petstate_petsetting_equipment_choice_success_returns_petstate`

Regression PNG smoke:

- `panel_petstate_petsetting_equipment_choice_open`
- `panel_petstate_petsetting_equipment_choice_navigation`
- `panel_petstate_petsetting_equipment_choice_statuses`
- `panel_petstate_petsetting_equipment_choice_back_returns_petstate`
- `panel_petstate_petsetting_item_choice_warning_hp_full`
- `panel_petstate_petsetting_item_choice_success_msg`
- `panel_petstate_petsetting_skill_open`
- `panel_petstate_petsetting_skill_back_returns_petstate`
- `panel_bag_open_from_gamemenu`
- `panel_save_success_status`
- `panel_gamesystem_option_confirm_no_returns_gamesystem`
- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`

## Next

Recommended next slice:

1. Audit `petsetting c=3` release pet source path, because item/equipment/skill subflows are now covered enough for normal panel use.
2. Keep `q.L` save/load and equipment battle passive effects as later runtime-state slices.
3. Keep `choice.ui/msgwarm.ui` clipping as a separate visual polish task, not mixed with gameplay mutation.
