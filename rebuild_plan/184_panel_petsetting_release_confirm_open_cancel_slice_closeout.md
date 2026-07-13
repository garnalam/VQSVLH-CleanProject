# 184 - Panel petsetting c=3 release confirm open/cancel slice

Date: 2026-07-10

## Scope

Implemented the first code slice for:

- `petsetting c=3`
- open `/data/ui/msgconfirm.ui`
- render source-backed confirm UI
- cancel/back to `petstate.ui`

This slice intentionally does **not** release/remove the pet yet.

## Source Chain

Primary audit:

- `rebuild_plan/183_panel_petsetting_release_pet_audit.md`

Source files:

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/ui/decoded/data__ui__msgconfirm.ui.json`

Confirmed source path:

- In `game.h.X()`, while `f == 1`, `o.Q == 6 || o.Q == 0`, confirm on `c == 3`:
  - normal pet opens `/data/ui/msgconfirm.ui`
  - closes `/data/ui/petsetting.ui`
  - message: `Ban muon phong sinh sung vat nay?`
  - action: `Xac nhan`
  - sets `f = 2`
- Cancel/back key while `f <= 2`:
  - closes `/data/ui/msgconfirm.ui`
  - returns `f = 0`

## Implemented

Files:

- `rebuild_game/src/main/java/VqsvIntroDemo.java`
- `rebuild_game/src/main/java/VqsvSceneView.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Runtime behavior:

- Added `sourceReleaseConfirmVisible`.
- Added source message/action strings.
- Added `openSourceReleaseConfirmFromPetSetting()`.
- Added `tickSourceReleaseConfirm()`.
- Added render path for decoded `/data/ui/msgconfirm.ui`:
  - frame widget `1`
  - message widget `4`
  - left softkey widget `2`
  - right softkey widget `3`
- Confirm key currently logs `PENDING` and does not mutate.
- Back/cancel closes confirm and returns to `petstate.ui`.

## Status

- `PORTED/PARTIAL`: open/render/cancel path for release confirm.
- `PENDING`: protected/mythic pet warning branch.
- `PENDING`: release success mutation.
- `PENDING`: last-alive warning.
- `PENDING`: save/load side effects after release.
- `PENDING`: exact pixel compare and text clipping polish.

## Verification

Build/check:

- `build.ps1` pass
- `com.vqsv.rebuild.Main --check` pass
- `VqsvBattleDamageFormulaCheck` pass
- `git diff --check` pass with existing CRLF warnings only
- mojibake scan Java source: no output

Focused PNG smoke:

- `panel_petstate_petsetting_release_confirm_open`
- `panel_petstate_petsetting_release_cancel_returns_petstate`

Regression PNG smoke:

- `panel_petstate_petsetting_equipment_choice_unequip_success_msg`
- `panel_petstate_petsetting_equipment_choice_equip_success_msg`
- `panel_petstate_petsetting_equipment_choice_transfer_success_msg`
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

1. Implement release success mutation:
   - require at least one other living party pet
   - clear selected pet equipment flag
   - remove selected pet from `sourcePets`
   - clamp selected index
   - refresh `petstate.ui`
2. Implement last-alive warning loop:
   - `Ba lo phai luu it nhat 1 sung vat`
   - confirm closes warning and returns to `petstate.ui`
3. Keep protected/mythic pet branch as a separate source-row proof unless a concrete species row is selected for smoke.
