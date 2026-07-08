# 102 New Dev Chat Handoff - Battle Engine After LevelUp

Status: CURRENT NEW DEV CHAT HANDOFF / TRAINING / ENTRY EXERCISE.

This supersedes `81_new_chat_hand_off_battle_engine_current.md`. Keep `81` as
historical context only; its P21/P17-only next target and P22/P23 pending
statements are outdated.

## Copy-Paste Prompt For A New Dev Chat

You are taking over the VQSV/Liet Hoa battle engine rebuild after the Bunny
catch, petstate, save prompt, EXP/levelUp, and learn-skill slices.

Hard rules:

- Source first. Do not guess logic, sprites, UI, text, timings, or event links.
- Do not open the live client unless the user explicitly asks. Use smoke PNG.
- Do not touch intro/world/panel/scene scripts unless the battle source path
  truly requires it.
- Classify every claim as `PORTED`, `PORTED/PARTIAL`, `APPROX`, `STUB`,
  `PENDING`, `UNKNOWN`, or `DAMAGED`.
- Do not claim pixel-perfect without original-vs-rebuild pixel compare.
- After code: run build, `--check`, `VqsvBattleDamageFormulaCheck` if battle
  runtime/formula was touched, mojibake scan, focused smoke PNG, and route
  regressions.

Before coding, read this file and answer the Compulsory Entry Exercise below in
chat. You may not code until the exercise is answered with source-backed,
specific claims.

## Required Reading Order

Read in this order:

1. `rebuild_plan/battle_engine_master_roadmap_progress.md`
2. `rebuild_plan/99_battle_petstate_item_exp_original_compare_audit.md`
3. `rebuild_plan/100_battle_exp_levelup_source_audit.md`
4. `rebuild_plan/101_battle_levelup_learn_skill_evolution_audit.md`
5. `rebuild_plan/98_save_resume_bunny_task_source_audit.md`
6. `rebuild_plan/97_battle_p5_petstate_ui_logic_full_audit.md`
7. `rebuild_plan/96_battle_pet_state_persistence_matrix.md`
8. `rebuild_plan/95_battle_visual_parity_marker_hud_p7_audit.md`
9. `rebuild_plan/94_battle_p0_entry_cpos_pet_release_audit.md`
10. `rebuild_plan/93_battle_p17_catch_success_openbox_fix.md`
11. `rebuild_plan/92_battle_bunny_completion_closure.md`
12. `rebuild_plan/91_battle_rng_formula_stream_slice_result.md`
13. `rebuild_plan/90_battle_bunny_pre_p17_trace_order_compare.md`
14. `rebuild_plan/87_battle_rng_trace_harness_matrix.md`
15. `rebuild_plan/82_battle_p21_p17_catch_edge_case_matrix.md`
16. `rebuild_plan/79_battle_item_pet_catch_state_matrix.md`
17. `rebuild_plan/78_battle_active_effect_lifecycle_full_matrix.md`
18. `rebuild_plan/76_battle_p12_p13_active_queue_lifecycle_matrix.md`
19. `rebuild_plan/74_battle_game_d_state_full_matrix.md`
20. `rebuild_game/src/main/java/VqsvBattleRuntime.java`
21. `rebuild_game/src/main/java/VqsvBattleUnit.java`
22. `rebuild_game/src/main/java/VqsvBattleRenderer.java`
23. `rebuild_game/src/main/java/VqsvBattlePetStateView.java`
24. `rebuild_game/src/main/java/VqsvBattleLevelUpView.java`
25. `rebuild_game/src/main/java/VqsvSourceModels.java`
26. `rebuild_game/src/main/java/VqsvSmokeHarness.java`
27. `rebuild_game/src/main/java/VqsvText.java`

Source files to keep open:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- `modules/source_code/decoded/decompiled_source_cfr/ae.java`
- `modules/source_code/decoded/decompiled_source_cfr/ah.java`
- `modules/source_code/decoded/decompiled_source_cfr/f.java`
- `modules/source_code/decoded/decompiled_source_cfr/l.java`

Resource files commonly needed:

- `modules/script/original/db.mid`
- `modules/script/original/effect.mid`
- `modules/script/original/speffect.mid`
- `modules/script/original/blood.mid`
- `modules/script/original/bufDebuf.mid`
- `modules/script/original/pos.mid`
- `modules/script/original/cpos.mid`
- `modules/ui/decoded/data__ui__battle.ui.json`
- `modules/ui/decoded/data__ui__choice.ui.json`
- `modules/ui/decoded/data__ui__choiceskill.ui.json`
- `modules/ui/decoded/data__ui__levelUp.ui.json`
- `modules/ui/decoded/data__ui__msgwarm.ui.json`
- `modules/ui/decoded/data__ui__petstate.ui.json`
- `modules/ui/decoded/data__ui__world.ui.json`

## Current Battle Engine Status

### PORTED / PORTED-PARTIAL

- Core source-shaped state runtime exists for P0/P20/P3/P6/P2/P7/P12/P13/P15/P1/P8/P9/P4/P16/P5/P21/P17/P10/P11 shell.
- P7 damage and `game.d.q()` post-skill slices are source-shaped.
- P12/P13 active effect queue has current route coverage for known buff/debuff consumers.
- P16 item validation/use ports `game.b.x/w` behavior `1..6` at current granularity.
- P5 pet switch supports dead/current validation, forced replacement, pet reorder, HP/PP payload persistence, and source-shaped `petstate.ui`.
- P15 player switch cpos uses original `cpos.mid` for the current elder path.
- P21/P17 catch supports current Bunny tutorial, no-ball warning, generic chance/status multiplier, catch fail/success, storage/openbox, and RNG trace harness at current granularity.
- Bunny tutorial route closes: first normal ball fail, Bunny counterattack, retry with better ball, catch success, openbox/storage, route task state.
- Battle entry pet release, ground markers, battle HUD/base placement have source-shaped slices but are not pixel-perfect.
- Save prompt after Bunny task and boot title continue state are source-shaped for current route.
- World petstate overlay exists using real `sourcePets`, including low-HP Bunny payload.
- P8/P22 EXP/levelUp active participant slice is implemented:
  - `game.d.h(b)` formula for one active participant;
  - `game.b.A()/g()/u()/v()` threshold/stat refresh;
  - `/data/ui/levelUp.ui` renderer;
  - smoke `battle_exp_levelup_ui_v2`.
- P23 learn-skill after level-up active-pet slice is implemented:
  - candidates from `game.b.F()`;
  - `choiceskill.ui` opens after `levelUp.ui`;
  - confirm adds skill to pet payload;
  - smokes `battle_exp_levelup_choiceskill_ui` and
    `battle_exp_levelup_learn_skill_done`.

### Still PARTIAL / PENDING

- Full `game.d.x` participant vector and multi-participant EXP distribution.
- Passive EXP share paths from source event/state and item/status effects.
- Evolution queue from `game.b.J()` into `game.k.H`, `game.k.L`, and `game.k.I`.
- Exact `msgwarm.ui`, `choiceskill.ui`, `levelUp.ui`, `petstate.ui`, and `choice.ui` widget runtime/pixel parity.
- Full battle background, base marker, HP HUD, P7 hit/recover/dead animations are still source-shaped partial, not original pixel-perfect.
- Full RNG parity from boot/world into battle is not exact; trace harness exists.
- Full P17 catch animation pixel/rattle/flash parity remains partial.
- Full broad skill coverage is partial.
- P11 shop/purchase/SMS is partial/pending; PC client may not need SMS, but source side effects must still be audited before claiming parity.
- Full `game.k` world pet menu modes: skill/equip/release/bank/send back are pending.

## Current Smoke Evidence

Recent focused PNG smokes:

- `rebuild_game/build/smoke/battle_exp_levelup_ui_v2.png`
- `rebuild_game/build/smoke/battle_exp_levelup_choiceskill_ui.png`
- `rebuild_game/build/smoke/battle_exp_levelup_learn_skill_done.png`
- `rebuild_game/build/smoke/world_petstate_dien_mieu_selected.png`
- `rebuild_game/build/smoke/world_petstate_bunny_selected.png`
- `rebuild_game/build/smoke/petstate_polish_bunny_low_hp_p5_titlefit.png`
- `rebuild_game/build/smoke/choice_catch_not_allowed_warning.png`
- `rebuild_game/build/smoke/choice_item_p4_description.png`

Recent route regressions:

- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`

Required commands after any battle code slice:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
java "-Dvqsv.modules=..\modules" -jar build\libs\vqsv-rebuild-skeleton.jar --check
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvBattleDamageFormulaCheck
rg -n "Ãƒ|Ã‚|ï¿½" src/main/java -g "*.java"
git diff --check
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint <focused_checkpoint> build\smoke\<name>.png
```

Then run route regressions if battle runtime, pet state, inventory, catch,
EXP, save, or event branch state was touched.

## Recommended Next Work

Do not code first. Create an audit first.

Best next concrete target:

`103_battle_levelup_evolution_queue_matrix.md`

Scope:

- Audit `game.b.J()` after level-up.
- Trace how it fills `game.k.H`, `game.k.L`, `game.k.I`.
- Find the `game.k` consumer that displays evolution/mutation UI/effects.
- Identify which UI/resource files are called.
- Decide whether current story routes can reach evolution, or whether a direct
  smoke fixture is needed.
- Only then port one small source-proved slice.

Alternative valid targets:

- Full EXP participant/share vector: `game.d.x`, `game.d.j`, `game.d.X()`,
  passive EXP share.
- Exact `msgwarm.ui` renderer for learn-skill/catch/P5 warnings.
- `choiceskill.ui` pixel/layout parity.
- P17 catch pixel/rattle/flash parity with original comparison.
- Remaining battle HUD/base/P7 animation parity, but only with source call path
  and smoke checkpoints.

Avoid:

- Broad UI polish without source consumer.
- Random skill effects without a route/state caller.
- Evolution animation guesses.
- Rewriting route/world scripts while working on battle engine.

## Compulsory Entry Exercise

Answer in chat before coding. A passing answer must cite the source/rebuild
files by path and classify every statement.

1. Prove the current P8/P22/P23 chain.
   - Source methods: `game.d.h(b)`, `game.d.X()`, `game.h.am()`,
     `game.h.an()`, `game.h.ao()`, `game.h.ap()`, `game.h.aq()`,
     `game.b.v()`, `game.b.F()`, `game.b.g(byte)`.
   - Rebuild methods/classes: `SourceBattleRuntime.tickWinExpLevelUp()`,
     `BattleUnit.sourceLearnCandidateSkillIds()`,
     `VqsvBattleRenderer.drawLevelUpOverlay()`,
     `VqsvSmokeHarness` checkpoints.
   - State what is PORTED/PARTIAL and what remains PENDING.

2. Prove why evolution is not done yet.
   - Explain `game.b.J()` and the `game.k.H/L/I` queue.
   - Identify what source consumer still needs audit.
   - State why adding an evolution animation now would be guessing.

3. Build the next audit matrix outline.
   - Proposed doc name.
   - Source files/functions to read.
   - UI/resource files to inspect.
   - Smoke checkpoints to add.
   - Risk to Bunny/Elder/Sophie routes.

4. Write a PNG-only smoke plan.
   - One focused checkpoint for the new slice.
   - Route regressions.
   - Build/check/formula/mojibake/diff commands.

5. Safety statement.
   - Files you may edit.
   - Files you must not touch.
   - Current remaining `PENDING`/`PARTIAL` items.

If the answer hand-waves, says "probably", claims pixel-perfect, or proposes
coding before source audit, stop and redo the exercise.

