# 220 - Battle Lab Hub NPC And Catch Plan

Status: LAB TOOLING ADDED / NO BATTLE RUNTIME CODE CHANGE.

Purpose:

- Build a dedicated battle lab layer for fast manual and PNG testing.
- Split battle testing into two source-shaped lanes:
  - NPC Battle Lab, anchored on the Elder battle.
  - Catch Battle Lab, anchored on the Bunny/catch battle.
- Keep lab shortcuts separate from the official game route.

## Core Rule

The lab can seed battle state, inventory, pets, money, and selected skills for
test speed, but the tested runtime must still use the real rebuild battle logic,
real source-backed UI widgets, and real source-shaped pet/inventory models.

Do not use lab-only data inside normal player flow.

## Lane A - NPC Battle Lab

Anchor:

```text
Elder battle route / NPC enemy battle.
```

Why Elder:

- It is already part of the current story route.
- It exercises NPC battle entry UI (`npcEnemy.ui`), P0/P20/P3/P6/P7/P8/P9/P15,
  EXP/reward, P5 switch, P16 item target, and P11 shop.
- It is safer than inventing a fake NPC battle.

### NPC Lab Entry Requirements

When opening an NPC lab scenario, it must initialize:

| Requirement | Source-backed reason |
| --- | --- |
| NPC battle entry UI | Source NPC battles show `npcEnemy.ui` before battle. |
| Player party | Pet bag/source party must match source-shaped model, not renderer mock data. |
| Enemy party | Elder enemy party should be loaded through the battle descriptor/runtime path. |
| Inventory | Items must come from `SourceItem`/`sourceBagItems` and source item table. |
| Money/badges | Needed for P11 shop/payment tests. |
| Battle result bridge | P8/P9 must still go through current route/result logic when scenario asks for it. |

### NPC Lab Scenario Groups

| Group | Scenarios | Existing likely checkpoint family |
| --- | --- | --- |
| Entry UI | NPC VS intro, pet throw-in, command UI. | `battle_npc_enemy_*`, `battle_elder_command_ui`. |
| Skills/formula | All Phase 9 skill families, crit/miss, debuff, buff, follow-up. | `battle_phase9*`, P7 checkpoints. |
| Effects/animation | P7 actor/effect chunks, AH type7/8/9/12/16, death effect. | `battle_phase10b*`, P7 death checkpoints. |
| Pet switch/P5 | Voluntary switch, forced switch after KO, current/dead warnings. | `battle_p5*`, `battle_elder_switched_bunny_ko_forced_p5_no_exp`. |
| Item/P16 | Heal, PP restore, HP+PP, revive, clear debuff, warnings, back to P4. | `battle_p16*`, P11 buy/use checkpoints. |
| Shop/P11 | Shop rows, quantity confirm, payment, money/badge/free PC policy, buy then use. | `battle_p11_shop*`. |
| EXP/level/learn | P8 EXP, multi-participant EXP, level up UI, learn skill. | `battle_exp*`, `battle_levelup*`. |
| Lose/revive | P9 all-dead, P24 revive, insufficient money warning. | `battle_p9*`, `battle_p24*`. |

## Lane B - Catch Battle Lab

Anchor:

```text
Bunny battle / catch tutorial and generic catch battle.
```

Why Bunny:

- It is the current source-backed catch route.
- It exercises P21 catch list, P17 capture animation/result, inventory
  consumption, catch storage, task progression, and caught pet persistence.

### Catch Lab Entry Requirements

When opening a catch lab scenario, it must initialize:

| Requirement | Source-backed reason |
| --- | --- |
| Bunny battle state | Catch must happen through P21/P17, not direct storage mutation. |
| Ball inventory | P21 reads the current bag/catch item list and counts. |
| Non-ball item inventory | P4/P16 should still use the same source bag and item logic. |
| Pet bag/storage capacity | Catch result must route to bag/bank/full-storage behavior. |
| Tutorial flags | Bunny tutorial forced fail/retry must be testable separately from generic catch. |
| Openbox/msgwarm UI | Success/warning UI must use source-backed runtime. |

### Catch Lab Scenario Groups

| Group | Scenarios | Existing likely checkpoint family |
| --- | --- | --- |
| P21 list/inventory | Two ball ids, count display, missing-count warning, row hover. | `battle_bunny_catch_p21`, `battle_catch_*`, `battle_choice_ui_*`. |
| Tutorial flow | Weak prompt, first forced fail, retry with Tat Trung Cau. | `battle_bunny_*`, tutorial checkpoints. |
| P17 animation | Throw anchor, shrink, fail escape, success ball settle. | `battle_p17*`, capture visual checkpoints. |
| Inventory consumption | Ball count decreases, zero count removed/disabled, no fake item rows. | catch inventory/checkpoints to add if missing. |
| Storage result | Add to bag, add to bank, full-slot warning/handling. | `battle_openbox*`, catch storage checkpoints. |
| Non-ball items in catch battle | P4/P16 item use while in catch battle, warnings for forbidden catch in NPC battle. | `battle_p16*`, catch-forbidden checkpoints. |
| Post-catch persistence | Bunny remains low HP in party after catch, visible in P5/petstate later. | `battle_bunny_caught_pet_p5_low_hp`, world/panel petstate. |

## Script Design

Recommended scripts:

```text
rebuild_game/run_battle_lab.ps1
rebuild_game/run_battle_lab_smoke.ps1
```

Implemented helper:

```text
rebuild_game/BattleLabScenarios.ps1
```

`BattleLabScenarios.ps1` is the single scenario map used by both scripts. It
must stay mapping-only: scenario name -> existing smoke/play checkpoint.

Implemented suite runner:

```text
rebuild_game/run_battle_lab_suite_smoke.ps1
```

This runs named PNG smoke suites over the scenario map. It builds once, then
exports a folder of checkpoint images.

For `cmd.exe`, use wrapper scripts:

```text
rebuild_game/run_battle_lab.cmd
rebuild_game/run_battle_lab_smoke.cmd
rebuild_game/run_battle_lab_suite_smoke.cmd
```

Important: in `cmd.exe`, use `cd /d E:\VQSVLH-CleanProject\rebuild_game`.
Plain `cd E:\...` does not switch from `C:` to `E:`.

Manual test:

```powershell
.\run_battle_lab.ps1 -Lane npc -Scenario elder_command
.\run_battle_lab.ps1 -Lane npc -Scenario p5_forced_switch
.\run_battle_lab.ps1 -Lane catch -Scenario bunny_capture_success
```

PNG smoke:

```powershell
.\run_battle_lab_smoke.ps1 -Lane npc -Scenario p16_item_revive
.\run_battle_lab_smoke.ps1 -Lane catch -Scenario p21_missing_count
```

Implementation should initially be only a scenario-name map to existing
`VqsvIntroDemo --play-checkpoint` and `--smoke-checkpoint`.

Do not build a custom in-game lab menu until scenario checkpoints are stable.

## Implemented Scenario Entry Examples

NPC/Elder lane:

```powershell
.\run_battle_lab_smoke.ps1 -Lane npc -Scenario vs_entry
.\run_battle_lab_smoke.ps1 -Lane npc -Scenario entry_power_percent
.\run_battle_lab_smoke.ps1 -Lane npc -Scenario p5_forced_switch
.\run_battle_lab_smoke.ps1 -Lane npc -Scenario p16_revive
.\run_battle_lab_smoke.ps1 -Lane npc -Scenario shop_buy_qty2
.\run_battle_lab_smoke.ps1 -Lane npc -Scenario exp_learn_skill
```

Catch/Bunny lane:

```powershell
.\run_battle_lab_smoke.ps1 -Lane catch -Scenario entry_no_npc_ui
.\run_battle_lab_smoke.ps1 -Lane catch -Scenario p21_list
.\run_battle_lab_smoke.ps1 -Lane catch -Scenario first_forced_fail
.\run_battle_lab_smoke.ps1 -Lane catch -Scenario success_openbox
.\run_battle_lab_smoke.ps1 -Lane catch -Scenario storage_bag
.\run_battle_lab_smoke.ps1 -Lane catch -Scenario caught_p5_low_hp
```

Use `-List` on either script to print the full scenario map.

Manual catch note:

- Use `catch command` or `catch p21_list` when the user needs direct control.
- Use PNG smoke for capture/tutorial animation states such as
  `first_forced_fail`, `first_fail_escape`, and `p17_anim_or_result`.
- `catch manual_p21_idle_guard` proves the lab P21 state does not auto-confirm
  without user input.

Suite smoke:

```powershell
.\run_battle_lab_suite_smoke.ps1 -Lane all -Suite core
.\run_battle_lab_suite_smoke.ps1 -Lane npc -Suite petstate
.\run_battle_lab_suite_smoke.ps1 -Lane npc -Suite items_shop_exp
.\run_battle_lab_suite_smoke.ps1 -Lane catch -Suite tutorial
.\run_battle_lab_suite_smoke.ps1 -Lane catch -Suite capture_visual
.\run_battle_lab_suite_smoke.ps1 -Lane catch -Suite inventory_storage
.\run_battle_lab_suite_smoke.ps1 -List
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-suite panel_wheel .\build_intro_demo\panel_wheel
```

Current suites:

| Lane | Suite | Purpose |
| --- | --- | --- |
| `npc` | `core` | NPC VS entry, command, skill list, target, P7 damage. |
| `npc` | `petstate` | P5 open/warnings/forced switch/all-dead. |
| `npc` | `items_shop_exp` | P16 items, P11 shop/payment, P8/learn skill. |
| `npc` | `loss` | P9/P24 lose and revive path. |
| `catch` | `core` | Bunny entry/no NPC UI, command, weak prompt, P21, P17. |
| `catch` | `tutorial` | First forced fail, escape, counterattack, retry prompt. |
| `catch` | `capture_visual` | P17 success/fail visual checkpoints. |
| `catch` | `inventory_storage` | Ball count, forbidden catch, storage bag/bank/full. |
| `catch` | `world_petstate` | Post-catch Bunny/petstate visibility. |
| `panel` | `panel_wheel` | Fixed `--smoke-suite` for mouse-wheel viewport and scroll-then-hover/click row mapping in panel lists. |

## Scenario Naming

Use stable names that describe the state, not the implementation:

```text
npc.elder_entry
npc.command
npc.skill.skill64_copy_buff
npc.item.p16_revive
npc.shop.buy_qty2
npc.pet.p5_forced_switch
npc.exp.levelup_learn
npc.lose.p24_revive_prompt

catch.bunny_p21_list
catch.bunny_first_fail
catch.bunny_retry_success
catch.inventory_missing_ball
catch.storage_bag_success
catch.storage_bank_success
catch.petstate_low_hp_after_catch
```

## Verification Policy

Every lab expansion must include:

- one manual `--play-checkpoint` mapping if useful;
- one PNG smoke mapping;
- documentation of what source state it seeds;
- no mutation of normal new-game/continue route.

Every promoted runtime change must still run:

```powershell
.\build.ps1
java "-Dvqsv.modules=..\modules" -cp .\build\classes com.vqsv.rebuild.Main --check
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvBattleDamageFormulaCheck
rg -n "�|Ã|á»|áº|Ä|Æ" src/main/java
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-suite battle_quick .\build_intro_demo\battle_quick_lab
```

If the promoted change touches input, mouse handling, list navigation, or
panel UI, also run:

```powershell
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-suite panel_wheel .\build_intro_demo\panel_wheel
```

## Immediate Next Step

1. Keep expanding the map only when a source-backed checkpoint exists.
2. Use the lab for Phase 11 `petstate.ui` runtime parity.
3. For petstate work, start with NPC lane P5/P16 and Catch lane caught-low-HP
   scenarios before changing renderer/runtime.
