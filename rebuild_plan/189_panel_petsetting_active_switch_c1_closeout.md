# 189 - Panel petsetting c=1 active pet switch closeout

## Scope

Implemented the source-backed `petsetting.ui` row `c=1` from `game.h.X()`.

This slice is panel/world petstate only. It does not change battle P5, P15, P7, or evolve row `c=5`.

## Source chain

- `game.h.X()`, owner `game.k`, `o.Q == 6 || o.Q == 0`, `f == 1`, `c == 1`.
- Dead check uses `game.b.S()`, which is `d[1] > 0`.
- Valid switch calls `game.g.p(this.b)`.
- `game.g.p(int)` moves the selected pet to party slot `0` and shifts earlier pets down.

## Implemented behavior

| Case | Source behavior | Rebuild status |
| --- | --- | --- |
| selected pet dead | close `petsetting.ui`, open `msgwarm.ui`, text `Sung vat nay khong the tham chien`, reset `b=0` | `PORTED` |
| selected pet already slot 0 | close `petsetting.ui`, open `msgwarm.ui`, text `Sung vat nay da xuat chien`, reset `b=0` | `PORTED` |
| selected pet valid reserve | `game.g.p(b)`, refresh `petstate.ui`, close `petsetting.ui`, reset selection to `0` | `PORTED` |

## Rebuild files changed

- `rebuild_game/src/main/java/VqsvIntroDemo.java`
  - Added `confirmSourcePetSettingActivePet()`.
  - Added `tickSourcePetSettingActiveWarningMessage()`.
  - Added `sourcePetSettingActiveWarningMode`.
- `rebuild_game/src/main/java/VqsvText.java`
  - Added source-specific panel text `PET_ALREADY_DEPLOYED`.
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`
  - Added focused PNG smoke checkpoints:
    - `panel_petstate_petsetting_active_switch_success`
    - `panel_petstate_petsetting_active_dead_warning`
    - `panel_petstate_petsetting_active_already_warning`
  - Updated legacy `panel_petstate_petsetting_confirm_pending` to assert the new source-backed warning instead of the old pending placeholder.

## Verification

Build/check:

- `.\build.ps1`: PASS
- `java "-Dvqsv.modules=..\modules" -jar build\libs\vqsv-liet-hoa-rebuild.jar --check`: PASS
- `java "-Dvqsv.modules=..\modules" -cp build\classes VqsvBattleDamageFormulaCheck`: PASS
- Java source mojibake scan: PASS, no hits
- `git diff --check`: PASS, no whitespace errors

Focused PNG smoke:

- `build/smoke/panel_c1/panel_petstate_petsetting_active_switch_success.png`: PASS
- `build/smoke/panel_c1/panel_petstate_petsetting_active_dead_warning.png`: PASS
- `build/smoke/panel_c1/panel_petstate_petsetting_active_already_warning.png`: PASS

Regression PNG smoke:

- `build/smoke/panel_c1_regression/panel_petstate_petsetting_release_success_removes_pet.png`: PASS
- `build/smoke/panel_c1_regression/panel_petstate_petsetting_item_choice_success_msg.png`: PASS
- `build/smoke/panel_c1_regression/panel_petstate_petsetting_equipment_choice_equip_success_msg.png`: PASS
- `build/smoke/panel_c1_regression/panel_petstate_petsetting_skill_open.png`: PASS
- `build/smoke/panel_c1_regression/panel_save_success_status.png`: PASS
- `build/smoke/panel_c1_regression/route_sophie_after_battle_branch.png`: PASS
- `build/smoke/panel_c1_regression/route_bunny_after_battle_task.png`: PASS
- `build/smoke/panel_c1_regression/route_elder_after_battle_reward_state.png`: PASS

## Current status

- `petsetting c=1`: `PORTED`.
- `petsetting c=5`: still `PENDING/PARTIAL`; direct route from `petsetting.ui` to `evolve.ui` is not wired yet.
- Top-level `bag.ui` item-use from panel: still later, after deciding whether to close `c=5` first.

## Recommended next step

Port `petsetting c=5` direct evolve/mutate entry:

- audit direct `game.h.bg()` open state against existing `openSourceEvolveUi()`;
- wire `sourcePetSettingIndex == 5` to existing evolve runtime only when `sourcePetSettingCount == 6`;
- add direct-panel evolve PNG smoke before moving to top-level `bag.ui` item-use.
