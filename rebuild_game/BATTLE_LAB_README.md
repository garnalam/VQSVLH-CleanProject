# VQSV Battle Lab

Battle Lab is a test-only launcher layer for battle work. It does not add
shortcuts to the normal player route.

## Manual Scenario

```powershell
cd E:\VQSVLH-CleanProject\rebuild_game
.\run_battle_lab.ps1 -Lane npc -Scenario command
.\run_battle_lab.ps1 -Lane catch -Scenario p21_list
```

For catch manual testing, prefer stable hand-control stops:

```powershell
.\run_battle_lab.ps1 -Lane catch -Scenario command
.\run_battle_lab.ps1 -Lane catch -Scenario p21_list
```

Capture animation/tutorial checkpoints such as `first_forced_fail` and
`p17_anim_or_result` intentionally advance through battle animation and should
usually be inspected as PNG smoke.

From `cmd.exe`, use `/d` when changing drive and call the `.cmd` wrappers:

```bat
cd /d E:\VQSVLH-CleanProject\rebuild_game
run_battle_lab.cmd -Lane npc -Scenario command
run_battle_lab.cmd -Lane catch -Scenario p21_list
```

## PNG Scenario

```powershell
.\run_battle_lab_smoke.ps1 -Lane npc -Scenario vs_entry
.\run_battle_lab_smoke.ps1 -Lane catch -Scenario storage_bag
```

```bat
run_battle_lab_smoke.cmd -Lane npc -Scenario vs_entry
run_battle_lab_smoke.cmd -Lane catch -Scenario storage_bag
```

## PNG Suite

```powershell
.\run_battle_lab_suite_smoke.ps1 -Lane all -Suite core
.\run_battle_lab_suite_smoke.ps1 -Lane npc -Suite petstate
.\run_battle_lab_suite_smoke.ps1 -Lane catch -Suite inventory_storage
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-suite panel_wheel .\build_intro_demo\panel_wheel
```

```bat
run_battle_lab_suite_smoke.cmd -Lane all -Suite core
run_battle_lab_suite_smoke.cmd -Lane npc -Suite petstate
run_battle_lab_suite_smoke.cmd -Lane catch -Suite inventory_storage
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-suite panel_wheel .\build_intro_demo\panel_wheel
```

Use `-List` on any lab script to print available names.

## Fixed Regression Gates

For battle runtime changes, keep `battle_quick` as the baseline PNG suite:

```powershell
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-suite battle_quick .\build_intro_demo\battle_quick
```

For input/list/panel UI changes, `panel_wheel` is also required. It guards
mouse-wheel viewport scrolling, non-scrollable task behavior, and
scroll-then-hover/click row mapping for `bag.ui` and `petmap.ui`.

```powershell
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-suite panel_wheel .\build_intro_demo\panel_wheel
```

## Rule

Lab setup may seed state for fast testing, but rendered UI and battle behavior
must use the same source-shaped runtime models as the main route. Promote only
runtime fixes back to the main route; never promote lab-only shortcuts.

## Mouse Wheel

Mouse wheel is a PC convenience layer for list viewport scrolling only. It moves
the visible list scrollbar/viewport when a scrollable list-like UI is open, such
as battle skill/item/pet/catch/shop lists, panel lists, petsetting, skill,
equipment, and item-choice lists. It must not confirm selections, it must not
fake up/down key presses, and it is ignored during free-world movement or
non-scrollable lists.
