# 395 - Petsetting All Rows Closeout

Ngay: 2026-07-15

Pham vi: closeout compact cho `petsetting.ui` trong tab **Sung vat** sau khi da di het row `0..5`.

## Row Matrix

| Row | Label | Main source path | Deep doc | Current status |
| --- | --- | --- | --- | --- |
| `0` | `Dao cu` | `game.k.bn()` item choice/use path | `387_panel_petsetting_item_choice_deep_closeout.md` | PORTED/PARTIAL |
| `1` | `Chien dau` | active pet switch validation/action | `388_panel_petsetting_battle_row_deep_closeout.md` | PORTED |
| `2` | `Vat pham trang suc` | held item/equipment `choice.ui` path | `389_panel_petsetting_equipment_row_deep_closeout.md` | PORTED/PARTIAL |
| `3` | `Phong sinh` | release confirm/warning/mutation guards | `390_panel_petsetting_release_row_deep_closeout.md` | PORTED |
| `4` | `Ky nang` | `game.k.bj()/bk()/bo()` read-only `skill.ui` | `391_panel_petsetting_skill_row_deep_closeout.md` | PORTED/PARTIAL |
| `5` | `Tien hoa / Di hoa` | `game.k.bl()/bp()` `evolve.ui` lifecycle | `394_panel_petsetting_evolve_row_deep_closeout.md` | PORTED/PARTIAL |

## What Is Closed

- `petstate.ui -> petsetting.ui` route exists.
- Every visible `petsetting.ui` row has a source-backed route or action.
- Every row has dedicated smoke coverage.
- PC mouse hover/wheel preview rules are applied where the screen has selection behavior.
- Rows that mutate data have at least one positive and one guarded/warning smoke where source has such a branch.

## What Is Not Claimed

| Area | Reason |
| --- | --- |
| Full generic Java ME widget VM | Current implementation uses source-shaped renderers per screen. |
| Pixel-perfect original compare | Smoke PNG asserts runtime state and visual presence, but not full original-vs-rebuild pixel diff. |
| Every storage/bank variant | This closeout covers normal carried-pet tab flow, not all pet bank/storage screens. |
| Exact evolution `ah type10` animation | Lifecycle/payload is covered; alpha/color transform parity remains pending. |
| Complete save parity for all pet fields | Focused save/load exists for equipment and key flows; full save-object audit is wider roadmap work. |

## Verification Snapshot

Commands run for the final row closeout:

```powershell
cd C:\Users\Dell\Downloads\ResourcesVQSV\rebuild_game
.\build.ps1
java "-Dvqsv.modules=..\modules" -jar .\build\libs\vqsv-liet-hoa-rebuild.jar --check
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-suite panel_pet_tab .\build_intro_demo\panel_pet_tab_394
```

Results:

- Build: PASS.
- Release check: PASS.
- `panel_pet_tab`: PASS `39/39`.
- Evolution world edge checkpoints: PASS `10/10`, output in `build_intro_demo/evolve_row_394_world`.
- Evolution payload/stat hardening recheck: PASS, output in `build_intro_demo/evolve_logic_recheck_394`.

## Roadmap Status

Tab **Sung vat** / `petsetting.ui` is now closed for current route-functionality purposes at **PORTED/PARTIAL**.

Recommended next step:

1. Do not keep looping inside `petsetting.ui` unless manual testing finds a concrete visible bug.
2. Move to the next panel/menu branch that is still visibly incomplete, or return to battle/skill animation comparison only for a specific skill the user flags as wrong.
