# 226 - New Dev Chat Handoff: Battle Lab, Item Completion, Skill Completion

Status: NEW CHAT HANDOFF / CURRENT BATTLE ROADMAP.

Purpose:

- Give a new dev chat enough context to continue without guessing.
- Explain where source truth lives, how to develop safely, and how to use Battle Lab.
- Set the next concrete roadmap: finish game item logic, then finish deeper skill behavior/parity.
- Require an entry exercise before coding.

This document must stay path-portable. Do not hardcode machine-specific absolute
paths in code, docs, or scripts. Use project-relative paths such as
`rebuild_game/`, `modules/`, and `rebuild_plan/`.

## Copy-Paste Prompt For New Dev Chat

```text
You are taking over VQSV/Liet Hoa battle-engine rebuild work.

Supreme rules:
- Source first. Do not guess logic, UI, assets, opcode behavior, RNG, or state flow.
- Compare against original source files under modules/source_code before coding.
- Use rebuild_plan docs as audit history, not as a substitute for source.
- Classify everything honestly: PORTED / PORTED-PARTIAL / APPROX / STUB / PENDING / UNKNOWN.
- Do not open the live client unless the user explicitly asks. Use PNG smoke by default.
- After each code slice: build/check/formula/mojibake/smoke regression.
- Never hardcode absolute local paths.
- Battle Lab is a test harness over the same runtime, not a separate game logic fork.

Before coding, read this handoff and answer the Compulsory Entry Exercise inside it.

Current target:
1. Finish item logic across battle, panel/bag, shop, rewards, equipment, save persistence.
2. Then finish deeper skill behavior/parity beyond broad smoke coverage.
3. Use Battle Lab for fast NPC/catch testing, and only merge behavior into the official route through shared runtime code.
```

## Required Reading Order

Read these first:

1. `rebuild_plan/226_new_dev_chat_handoff_battle_lab_items_skills.md`
2. `rebuild_plan/battle_engine_master_roadmap_progress.md`
3. `rebuild_plan/220_battle_lab_hub_npc_and_catch_plan.md`
4. `rebuild_plan/219_petstate_lab_build_workflow.md`
5. `rebuild_plan/221_battle_choice_ui_wheel_hover_click_mapping_audit.md`
6. `rebuild_plan/222_battle_exp_normal_vs_forced_levelup_audit.md`
7. `rebuild_plan/223_battle_exp_formula_and_source_flow_audit.md`
8. `rebuild_plan/224_battle_p8_exp_pos_mid_marker_placement_audit.md`
9. `rebuild_plan/225_battle_p8_exp_initial_frame_timing_audit.md`
10. `rebuild_plan/187_battle_phase9ad_skill_coverage_closeout.md`
11. `rebuild_plan/79_battle_item_pet_catch_state_matrix.md`
12. `rebuild_plan/114_battle_p16_source_reaudit_matrix.md`
13. `rebuild_plan/200_panel_bag_default_state17_audit.md`
14. `rebuild_plan/204_panel_bag_default_state17_item_behavior_audit.md`
15. `rebuild_plan/205_panel_item_5_12_source_metadata_closeout.md`
16. `rebuild_plan/206_battle_p11_shop_item_reachability_audit.md`
17. `rebuild_plan/207_battle_p11_shopbuy_msgyn_polish_closeout.md`

Then read source files for the slice you are touching:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- `modules/source_code/decoded/decompiled_source_cfr/aq.java`
- `modules/source_code/decoded/decompiled_source_cfr/ae.java`
- `modules/source_code/decoded/decompiled_source_cfr/ah.java`
- `modules/source_code/decoded/decompiled_source_cfr/f.java`
- `modules/source_code/decoded/decompiled_source_cfr/l.java`

Then inspect rebuild files:

- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleRenderer.java`
- `rebuild_game/src/main/java/VqsvBattleUnit.java`
- `rebuild_game/src/main/java/VqsvBattleTables.java`
- `rebuild_game/src/main/java/VqsvSourceOps.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`
- `rebuild_game/BattleLabScenarios.ps1`
- `rebuild_game/run_battle_lab.ps1`
- `rebuild_game/run_battle_lab_smoke.ps1`
- `rebuild_game/run_battle_lab_suite_smoke.ps1`

## Current Project Shape

The project has three important layers:

| Layer | Path | Role |
| --- | --- | --- |
| Original decoded source | `modules/source_code/decoded/decompiled_source_cfr/` | Source truth for Java ME logic. |
| Decoded UI/assets | `modules/ui/decoded/`, `modules/source_code/.../data` | Source truth for widget geometry, sprites, MID/script data. |
| Rebuild runtime | `rebuild_game/src/main/java/` | PC rebuild implementation. |

Do not treat Battle Lab as a fourth runtime. It is a module/workflow for
seeding scenarios and taking PNG/live checkpoints against the same rebuild
runtime.

## How To Develop

Every slice should follow this order:

1. Pick one source-backed behavior.
2. Read source call chain first: state entry -> UI method -> input method -> side effect -> next state.
3. Read asset/UI dependency: `.ui`, sprite id/cell, `.mid`, table row, text source.
4. Write or update an audit doc before risky code.
5. Patch the shared runtime, not Battle Lab-only behavior.
6. Add or update a focused Battle Lab scenario/checkpoint if it helps manual/PNG testing.
7. Run build/check/formula/mojibake/smoke.
8. Report what is done, what remains PARTIAL/PENDING, and what should be next.

Do not implement a UI because it "looks right". Implement it because source
calls that UI and source data says where each part belongs.

## Current Status

### Battle Core

| Area | Status | Notes |
| --- | --- | --- |
| State machine `game.d` main route | PORTED-PARTIAL | Current Sophie/Bunny/Elder routes are smoke-covered, but not a generic decoded VM. |
| Battle entry/exit op bridge | PORTED-PARTIAL | `op37/op32/op47` descriptors exist for current routes. |
| P7 damage/result flow | PORTED-PARTIAL | Closed for current routes; do not reopen without source-route mismatch or original capture. |
| KO/result/EXP flow | PORTED-PARTIAL | Player KO opens P5 when party has living pets; all-dead goes lose/revive path. EXP is awarded from enemy KO, not player pet death. |
| P8 EXP animation | PORTED-PARTIAL | Source formula and first-frame timing now match current audit: frame 0 can show `0/760`, then `+8` ticks. Exact original-client pixel compare remains PENDING. |
| P22/P23 level-up/learn skill | PORTED-PARTIAL | Level-up UI and learn-skill route exist. Evolution queue remains separate/PARTIAL. |
| Catch P21/P17 | PORTED-PARTIAL | Bunny tutorial, item list, storage, capture anchor, and openbox are current-route smoke-covered. Pixel-perfect capture remains PENDING. |
| Phase 9 skill coverage | CLOSED AS PORTED-PARTIAL | Every skill row `aq.c[1][0..69]` has coverage/classification, but this is not full exact parity for every animation/RNG/passive edge. |
| Phase 11 UI runtime | ACTIVE/PARTIAL | `choice.ui`, `msgwarm.ui`, `openbox.ui` improved; `petstate.ui` and generic `game.h` widget runtime still partial. |

### Items And Inventory

| Area | Status | Notes |
| --- | --- | --- |
| Battle P4 item list | PORTED-PARTIAL | Uses `choice.ui` source-shaped rows, hover/click/wheel mapping verified. |
| Battle P16 item target/use | PORTED-PARTIAL | Uses `petstate.ui` target; `game.b.x/w` behaviors `1..6` exist at current granularity. |
| Catch ball list/use | PORTED-PARTIAL | P21/P17 current Bunny/generic catch path smoke-covered. |
| Panel bag default state17 | PORTED-PARTIAL | Open/navigation/warning/success mutation slices exist. More item behaviors need full source audit. |
| Item metadata 5..12 | PORTED-PARTIAL | Names/icons/descriptions source-backed from `VqsvBattleTables.item(id)`. |
| Shop P11 | PORTED-PARTIAL | Reachability, quantity confirm, free-PC currency policy, and msgyn polish exist. SMS/network is intentionally removed for PC. |
| Equipment/petsetting item paths | PORTED-PARTIAL | Equip/unequip and ownership transfer slices exist; verify before extending. |
| Rewards/op17 inventory | PORTED-PARTIAL | Source-shaped reward popup/inventory logic exists, but global item completion needs a matrix. |
| Save/load persistence | PORTED-PARTIAL | Key battle/pet/item state persists where audited. New item state must explicitly add save/load tests. |

## Battle Lab As Project Module

Battle Lab is the standard testing module for battle work.

It consists of:

| File | Role |
| --- | --- |
| `rebuild_game/BattleLabScenarios.ps1` | Single scenario/suite map. Keep it mapping-only. |
| `rebuild_game/run_battle_lab.ps1` | Manual/live scenario launcher. |
| `rebuild_game/run_battle_lab_smoke.ps1` | One-scenario PNG smoke launcher. |
| `rebuild_game/run_battle_lab_suite_smoke.ps1` | Suite PNG smoke launcher. |
| `rebuild_game/run_battle_lab.cmd` | CMD wrapper. |
| `rebuild_game/run_battle_lab_smoke.cmd` | CMD wrapper. |
| `rebuild_game/run_battle_lab_suite_smoke.cmd` | CMD wrapper. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Checkpoint setup/assertions. |

Battle Lab rules:

- Lab can seed party, inventory, money, scenario state, and selected skills for testing speed.
- Lab must call the same runtime and UI code as the official game.
- Do not add behavior that only works in lab.
- If a lab scenario needs a new source behavior, implement the behavior in shared runtime first.
- Lab output PNGs are evidence, not proof of pixel-perfect parity unless compared against original frames.

### CMD Usage

From `rebuild_game/`:

```cmd
run_battle_lab_smoke.cmd -List
run_battle_lab_suite_smoke.cmd -List
run_battle_lab.cmd -Lane npc -Scenario exp_frame0 -NoBuild
run_battle_lab.cmd -Lane npc -Scenario command -NoBuild
run_battle_lab.cmd -Lane catch -Scenario p21_list -NoBuild
run_battle_lab_suite_smoke.cmd -Lane npc -Suite exp_animation
run_battle_lab_suite_smoke.cmd -Lane npc -Suite items_shop_exp
run_battle_lab_suite_smoke.cmd -Lane catch -Suite core
run_battle_lab_suite_smoke.cmd -Lane catch -Suite inventory_storage
```

If working from another drive in `cmd.exe`, use `cd /d` to enter the repo.
Do not write an absolute path into docs or scripts.

### PowerShell Usage

From `rebuild_game/`:

```powershell
.\run_battle_lab_smoke.ps1 -List
.\run_battle_lab_suite_smoke.ps1 -List
.\run_battle_lab.ps1 -Lane npc -Scenario exp_frame0 -NoBuild
.\run_battle_lab.ps1 -Lane catch -Scenario p21_list -NoBuild
.\run_battle_lab_suite_smoke.ps1 -Lane npc -Suite exp_animation
```

### Current Important Suites

| Lane | Suite | Use |
| --- | --- | --- |
| `npc` | `core` | NPC entry, command, skill, target, P7 basics. |
| `npc` | `petstate` | P5 switch, forced switch, warnings, all-dead boundaries. |
| `npc` | `items_shop_exp` | P16 items, P11 shop/payment, P8/P22/P23. |
| `npc` | `exp_animation` | P8 EXP frame0/frame1/mid/hold/level-up/learn-skill. |
| `npc` | `loss` | P9/P24 lose/revive. |
| `catch` | `core` | Bunny/catch command, weak prompt, P21/P17 basics. |
| `catch` | `tutorial` | Forced fail/retry tutorial route. |
| `catch` | `capture_visual` | P17 visual checkpoints. |
| `catch` | `inventory_storage` | Ball count, catch forbidden, storage bag/bank/full. |
| `catch` | `world_petstate` | Caught Bunny persistence into petstate. |
| `panel` | `panel_wheel` | Panel list wheel/hover/click row mapping. |

## Required Regression Gates

For any battle runtime change:

```cmd
powershell -ExecutionPolicy Bypass -File build.ps1
java -cp build/classes com.vqsv.rebuild.Main --check
java -cp build/classes VqsvBattleDamageFormulaCheck
java -Dvqsv.modules=../modules -cp build/classes VqsvIntroDemo --smoke-suite battle_quick build_intro_demo/battle_quick
```

For panel/list input changes:

```cmd
java -Dvqsv.modules=../modules -cp build/classes VqsvIntroDemo --smoke-suite panel_wheel build_intro_demo/panel_wheel
```

For Battle Lab item/skill work, also run the focused suite:

```cmd
run_battle_lab_suite_smoke.cmd -Lane npc -Suite items_shop_exp
run_battle_lab_suite_smoke.cmd -Lane npc -Suite exp_animation
run_battle_lab_suite_smoke.cmd -Lane catch -Suite inventory_storage
```

Run route regressions after any state/route/event change:

- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`

Run a Java mojibake scan after Java edits. Existing scripts/commands may vary,
but the result must be reported.

## Roadmap From Here

### Phase A - Item Completion Matrix

Item completion audit now exists:

```text
227_battle_item_full_completion_matrix.md
```

It maps every source item behavior touched by:

- `aq.c[4]` item rows and behavior columns.
- `game.b.x(itemId)` validation.
- `game.b.w(itemId)` mutation.
- `game.h` P4/P16/P21/state17 input handlers.
- `game.g` bag/storage methods.
- P11 shop purchase and payment policy.
- Panel bag item use.
- Battle item use.
- Catch item use.
- Equipment owner transfer.
- Rewards/op17 add/remove.
- Save/load persistence.

The matrix must say, for each item or behavior:

```text
source row -> UI path -> validation -> mutation -> inventory consume -> save/load -> smoke checkpoint -> status
```

Do not implement all items at once. Pick the smallest source-proven missing
behavior after the matrix.

Current next source-backed slice from `227`:

```text
228_panel_state17_petsetting_item_behavior_smoke_matrix.md
```

This should lock focused PNG coverage for already-ported normal item behaviors
before changing logic.

### Phase B - Item Logic Slices

Recommended order:

1. Verify every item row has source metadata: name, icon, description, behavior, value.
2. Finish battle P16 behavior gaps item by item.
3. Finish panel bag state17 behavior gaps item by item.
4. Verify shop purchase -> inventory -> use loop for reachable items.
5. Verify reward/op17 add/remove -> inventory -> use loop.
6. Verify save/load persists all state changed by each item.
7. Add Battle Lab scenario/suite coverage only after shared runtime works.

### Phase C - Skill Completion Matrix

After item completion is stable, create:

```text
228_battle_skill_full_completion_matrix.md
```

Phase 9 closed broad row coverage, but this next matrix is stricter. For each
skill family or individual skill, map:

- `aq.c[1]` row.
- command selection path.
- PP cost.
- target rules.
- damage formula branch.
- miss/crit/dodge.
- buff/debuff/status producer.
- `game.d.q()` follow-up.
- active queue P12/P13 consumer.
- P7 effect chain and sprite/MID assets.
- death/result transition.
- RNG calls and labels.
- AI use if source has it.
- smoke/lab scenario.
- status: `PORTED`, `PORTED-PARTIAL`, `APPROX`, `PENDING`, `UNKNOWN`.

Do not reopen random P7 visual work unless a skill source path proves it needs
that effect or an original-vs-rebuild capture shows a mismatch.

### Phase D - Skill Logic Slices

Recommended order:

1. Fix formula/status/passive behavior before visual polish.
2. Use NPC Battle Lab to test selected skill ids quickly.
3. Use catch lane only when catch-specific battle state matters.
4. Add synthetic Battle Lab scenarios for rare skill families, but keep runtime shared.
5. Keep every skill slice tied to a source row and source call path.

## Compulsory Entry Exercise

Before coding, answer these in chat:

1. Prove the P8 EXP source chain:
   `enemy KO -> game.d.h()/X -> state 8 -> game.h.a(...) -> game.h.am()`.
   Explain why frame0 can show `0/760` before `+8`.
2. Explain Battle Lab architecture:
   which file maps scenarios, which file sets checkpoints, and why lab must not
   contain runtime-only behavior.
3. Build a mini item matrix for three real item ids:
   source metadata, UI path, validation, mutation, inventory consume, save/load,
   current status.
4. Build a mini skill matrix for three skill ids:
   source row, formula/status/effect, runtime file, smoke/lab scenario, current
   status.
5. Write a PNG-only smoke plan for the first item slice you recommend.
6. Safety statement:
   files you expect to edit, files you will not touch, regressions you will run,
   and remaining `PARTIAL/PENDING` you will not overclaim.

Only after this exercise is accepted should the new chat code.

## Immediate Recommendation

Next concrete work:

```text
Create 228_panel_state17_petsetting_item_behavior_smoke_matrix.md.
```

Do not start by coding a random item. First add focused PNG checkpoints for
panel `state17` and `petsetting c=0` item behaviors that already have
source-proven logic:

- item `6` PP restore;
- item `8` HP+PP restore;
- item `10` clear debuff success/warning;
- item `12` stronger revive;
- item `8/9` both-full warning `7`.

If any checkpoint fails, fix only the failing source-proven branch.
