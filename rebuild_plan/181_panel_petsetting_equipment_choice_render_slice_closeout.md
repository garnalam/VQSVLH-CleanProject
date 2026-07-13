# 181 - Panel petsetting c=2 equipment choice.ui render slice

Date: 2026-07-10

## Scope

Implemented the first code slice for:

- `petsetting c=2 -> /data/ui/choice.ui`
- source-shaped `q.L` equipment list
- selected pet equipment slot display
- render/navigate/back only

Confirm is intentionally still `PENDING`; this slice does not mutate equip/unequip state.

## Source Chain

Primary source:

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`

See audit:

- `rebuild_plan/180_panel_petsetting_equipment_choice_audit.md`

Confirmed source mapping:

- `game.h.X()` `petsetting c=2` opens `choice.ui`, closes `petsetting.ui` and `petstate.ui`.
- Header widgets:
  - `8 = "Vat pham trang suc"`
  - `9 = "Trang thai"`
- World owner uses alternate softkeys `59/60`, with action text `Mang theo` or `Do xuong`.
- Rows are from `q.L`.
- `q.L` row shape for visible render:
  - `[0] = equipment id`
  - `[1] = equipped flag`
- Selected pet equipment slot is `q.z[b].c[5]`, persisted in pet payload index `2`.
- Row metadata comes from `aq.c[3]`:
  - `[0] = name text id`
  - `[1] = icon cell`
  - `[2] = description text id`

## Implemented

Files:

- `rebuild_game/src/main/java/VqsvSourceModels.java`
- `rebuild_game/src/main/java/VqsvSourceOps.java`
- `rebuild_game/src/main/java/VqsvIntroDemo.java`
- `rebuild_game/src/main/java/VqsvSceneView.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Runtime behavior:

- Added `SourceEquipmentItem`.
- Added `sourceEquipmentItems` as a separate source-shaped `q.L` list, not mixed into `sourceBagItems`.
- Added source equipment metadata helpers from `VqsvBattleTables.row(3, id)`.
- Added `sourceEquipmentChoiceVisible/index/scroll`.
- Added `openSourceEquipmentChoiceFromPetSetting()`.
- Added `tickSourceEquipmentChoice()`:
  - up/down navigation
  - back returns to `petstate.ui`
  - confirm logs `PENDING` only
- Added `sourceEquipmentChoiceView()` using decoded `choice.ui` renderer.
- Reused generic `choice.ui` render path in `VqsvSceneView`.

## Status

- `PORTED/PARTIAL`: source route `petsetting c=2 -> choice.ui`.
- `PORTED/PARTIAL`: `q.L` row shape for visible render.
- `PORTED/PARTIAL`: selected pet equipment slot displayed from source payload index `2`.
- `PORTED/PARTIAL`: status strings:
  - selected pet has item: `Da mang theo`
  - another pet has item: `Bi mang theo`
  - free item: blank
- `PENDING`: confirm equip/unequip mutation through `game.g.f/l`.
- `PENDING`: source save/load of `q.L`.
- `PENDING`: equipment stat/passive effects in battle.
- `PENDING`: original-vs-rebuild pixel compare.
- `PENDING`: long title/status text layout polish; current renderer follows widget map but clips long strings in the narrow source widgets.

## Verification

Build/check:

- `build.ps1` pass
- `com.vqsv.rebuild.Main --check` pass
- `VqsvBattleDamageFormulaCheck` pass
- `git diff --check` pass with existing CRLF warnings only
- mojibake scan Java source: no output

Focused PNG smoke:

- `panel_petstate_petsetting_equipment_choice_open`
- `panel_petstate_petsetting_equipment_choice_navigation`
- `panel_petstate_petsetting_equipment_choice_statuses`
- `panel_petstate_petsetting_equipment_choice_back_returns_petstate`

Regression PNG smoke:

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

1. Audit and port confirm `petsetting c=2` mutation:
   - if selected pet already wears selected item: `game.g.l(itemId)` and `pet.c[5] = -1`
   - else `game.g.f(itemId, petIndex)`
   - success msgwarm `Thanh cong do xuong` / `Thanh cong mang theo`
   - `f=3 -> f=2` confirmation loop
2. Keep save/load of `q.L` and battle passive/stat effects for later slices.
3. If user prioritizes visual polish, audit original `choice.ui` long text clipping before changing widths.
