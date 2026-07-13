# 190 - Panel petsetting c=5 direct evolve entry closeout

## Scope

Implemented direct `petsetting.ui` row `c=5` entry to `evolve.ui`.

This slice only wires the source panel route:

`petstate.ui -> petsetting.ui c=5 -> game.h.bg() -> evolve.ui`

It does not change battle P5/P15/P7 and does not add new evolution formulas beyond the existing `VqsvSourceEvolutionRuntime`.

## Source chain

- `game.h.X()`, owner `game.k`, `o.Q == 6 || o.Q == 0`, `f == 1`, `c == 5`.
- Source does:
  - `this.o.m();`
  - `this.bg();`
- `game.h.bg()`:
  - sets `f = 2`, `r = 0`
  - opens `/data/ui/evolve.ui`
  - closes `/data/ui/petsetting.ui`
  - closes `/data/ui/petstate.ui`
  - fills widgets `10`, `38`, `40`, `45`, `46`, `19..22`, `31..34`

## Implemented behavior

| Case | Rebuild status |
| --- | --- |
| `petsetting c=5` visible only when source `R()`/notice has evolution or mutation | `PORTED/PARTIAL`, existing row count logic |
| confirm `c=5` closes `petsetting.ui` and `petstate.ui` | `PORTED` |
| confirm `c=5` opens existing source-backed `evolve.ui` runtime | `PORTED` |
| no material warning through existing `game.h.bh()` path | `PORTED/PARTIAL` |
| success material consume and pet species mutate through existing runtime | `PORTED/PARTIAL` |

## Rebuild files changed

- `rebuild_game/src/main/java/VqsvIntroDemo.java`
  - Added `sourcePetSettingIndex == 5 && sourcePetSettingCount == 6` dispatch.
  - Added `openSourceEvolveUiFromPetSetting()`.
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`
  - Added direct panel c=5 smoke helper and checkpoints:
    - `panel_petstate_petsetting_evolve_open`
    - `panel_petstate_petsetting_evolve_no_material_warning`
    - `panel_petstate_petsetting_evolve_success_mutate`

## Verification

Build/check:

- `.\build.ps1`: PASS
- `java "-Dvqsv.modules=..\modules" -jar build\libs\vqsv-liet-hoa-rebuild.jar --check`: PASS
- `java "-Dvqsv.modules=..\modules" -cp build\classes VqsvBattleDamageFormulaCheck`: PASS
- Java source mojibake scan: PASS, no hits
- `git diff --check`: PASS, no whitespace errors

Focused PNG smoke:

- `build/smoke/panel_c5/panel_petstate_petsetting_evolve_open.png`: PASS
- `build/smoke/panel_c5/panel_petstate_petsetting_evolve_no_material_warning.png`: PASS
- `build/smoke/panel_c5/panel_petstate_petsetting_evolve_success_mutate.png`: PASS

Regression PNG smoke:

- `panel_petstate_petsetting_active_switch_success`: PASS
- `panel_petstate_petsetting_active_dead_warning`: PASS
- `panel_petstate_petsetting_active_already_warning`: PASS
- `panel_petstate_petsetting_release_success_removes_pet`: PASS
- `panel_petstate_petsetting_item_choice_success_msg`: PASS
- `panel_petstate_petsetting_equipment_choice_equip_success_msg`: PASS
- `panel_petstate_petsetting_skill_open`: PASS
- `panel_save_success_status`: PASS
- `world_evolution_evolve_ui_open`: PASS
- `world_evolution_confirm_no_material`: PASS
- `world_evolution_confirm_success_mutate`: PASS
- `route_sophie_after_battle_branch`: PASS
- `route_bunny_after_battle_task`: PASS
- `route_elder_after_battle_reward_state`: PASS

## Current status

- `petsetting c=0`: `PORTED/PARTIAL`
- `petsetting c=1`: `PORTED`
- `petsetting c=2`: `PORTED/PARTIAL`
- `petsetting c=3`: `PORTED`
- `petsetting c=4`: `PORTED/PARTIAL`
- `petsetting c=5`: `PORTED/PARTIAL`

`petsetting.ui` source branches are now all represented in rebuild. Remaining limits are mostly depth/polish in sub-runtimes, not missing row routes.

## Recommended next step

Move to top-level `bag.ui` item-use from panel:

- audit source path from `gamemenu b=2 -> P=8 -> bag.ui`;
- identify item tabs and confirm behavior outside battle;
- port the first small item-use behavior with PNG smoke.

