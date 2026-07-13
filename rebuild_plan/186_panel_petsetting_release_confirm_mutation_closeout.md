# 186 - Panel petsetting release confirm mutation closeout

Date: 2026-07-10

## Scope

Implemented the source-backed `petsetting c=3` release-pet confirm mutation from
`game.h.X()`.

This follows the audited source chain:

- normal release confirm: `/data/ui/msgconfirm.ui`
- confirm key: `131072`
- back key: `786432`
- warning confirm key: `131104`
- release mutation: `game.g.l(equipmentId)` then `game.g.m(index)`

## Source Chain

Primary source:

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`

Confirmed source behavior:

- Protected/mythic pet:
  - `aq.c[0][speciesId][22] == 2`
  - opens `msgwarm.ui`
  - message: `Than thu khong the phong sinh`
  - does not open release confirm
- Normal pet:
  - opens `msgconfirm.ui`
  - message: `Ban muon phong sinh sung vat nay?`
  - action: `Xac nhan`
- Confirm normal pet:
  - if `game.g.o(index)` is false, opens last-alive warning
  - if true:
    - `game.g.l(pet.c[5])`
    - `pet.c[5] = -1`
    - `game.g.m(index)` shifts party left
    - clamps selected index
    - refreshes `petstate.ui`
    - closes `msgconfirm.ui`

## Implemented

Files:

- `rebuild_game/src/main/java/VqsvIntroDemo.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`
- `rebuild_game/src/main/java/VqsvText.java`

Runtime:

- Added `sourceReleaseWarningMode` to model source `f=3` warning state.
- Added protected-pet check via `VqsvBattleTables.row(0, speciesId)[22] == 2`.
- Added last-alive check equivalent to source `game.g.o(index)`.
- Added release success mutation:
  - clear selected pet equipment owner through existing equipment helper
  - remove selected pet from `sourcePets`
  - shift/reindex slots
  - clamp `battleMenuIndex`
  - refresh `petstate.ui`
- Warning input is intercepted so closing `msgwarm.ui` does not accidentally open
  the underlying `petstate.ui` row.

## Status

- `PORTED`: release success mutation path for `petsetting c=3`.
- `PORTED`: last-alive warning state and return-to-petstate loop.
- `PORTED/PARTIAL`: protected/mythic warning path; source condition is real, but
  smoke uses the first valid protected source species row found in `db.mid`.
- `PORTED/PARTIAL`: `msgconfirm.ui` visual/runtime from previous slice.
- `PORTED/PARTIAL`: `msgwarm.ui` renderer is still the existing minimal
  source-backed renderer with marquee/cropping behavior, not full `ao/af/k/m`.
- `PENDING`: save/load persistence side effect audit after release.
- `PENDING`: original-vs-rebuild pixel compare for `msgwarm.ui`.

## Verification

Build/check:

- `build.ps1` pass
- `git diff --check` pass with existing CRLF warnings only
- `com.vqsv.rebuild.Main --check` pass
- `VqsvBattleDamageFormulaCheck` pass
- Java mojibake scan: no output

Focused smoke PNG:

- `panel_petstate_petsetting_release_confirm_open`
- `panel_petstate_petsetting_release_cancel_returns_petstate`
- `panel_petstate_petsetting_release_success_removes_pet`
- `panel_petstate_petsetting_release_last_alive_warning`
- `panel_petstate_petsetting_release_warning_returns_petstate`
- `panel_petstate_petsetting_release_protected_warning`

Regression smoke PNG:

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

Audit and polish the generic `msgwarm.ui` runtime, because release warning and
many other source panel warnings currently share the same minimal renderer. The
next patch should focus on message/prompt layout and marquee behavior only, not
pet release gameplay.
