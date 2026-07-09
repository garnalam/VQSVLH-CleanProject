# 128 New Dev Chat Handoff - Battle Phase 5 After UI Runtime

Status date: 2026-07-08

Status: CURRENT NEW DEV CHAT HANDOFF / TRAINING / ENTRY EXERCISE.

This supersedes `102_new_dev_chat_handoff_battle_engine_after_levelup.md` as
the recommended handoff for a new battle-engine dev chat. Keep `102` and older
handoffs as historical context, but do not treat their "next target" sections
as current unless they are confirmed by this file and the latest roadmap docs.

## Copy-Paste Prompt For A New Dev Chat

You are taking over the VQSV/Liet Hoa rebuild during Battle Phase 5 after the
P4/P16 item flow and the `choice.ui` / `petstate.ui` / `msgwarm.ui` battle UI
runtime slice.

Hard rules:

- Source first. Do not guess logic, sprites, UI, text, timings, animation, or
  event links.
- Do not open the live client/JAR unless the user explicitly asks. Use smoke
  PNG/headless checkpoints only.
- Do not touch intro/world/panel/scene scripts unless the battle source path
  truly requires it.
- Classify every claim as `PORTED`, `PORTED/PARTIAL`, `APPROX`, `STUB`,
  `PENDING`, `UNKNOWN`, `DAMAGED`, or `REBUILD_POLICY`.
- Do not claim pixel-perfect without original-vs-rebuild pixel comparison.
- Logic before UI: only port a UI/effect when `game.d`, `game.h`, `game.b`,
  `game.g`, or `game.k` source proves that the runtime calls it.
- After any code slice: run build, `--check`, `VqsvBattleDamageFormulaCheck`
  if battle runtime/formula was touched, Java mojibake scan, focused smoke PNG,
  and route regressions when state/branch/inventory/pet/catch/EXP can be
  affected.
- Never hide approximations. If a renderer is source-shaped but not full
  MIDP parity, say `PORTED/PARTIAL`.

Before coding, read this file and answer the Compulsory Entry Exercise below in
chat. You may not code until the exercise is answered with source-backed,
specific claims.

## Required Reading Order

Read in this order:

1. `rebuild_plan/battle_engine_master_roadmap_progress.md`
2. `rebuild_plan/127_battle_choice_petstate_msgwarm_ui_runtime_closeout.md`
3. `rebuild_plan/126_battle_item_inventory_ownership_audit.md`
4. `rebuild_plan/125_battle_p4_p16_a_flow_closeout.md`
5. `rebuild_plan/124_battle_p4_p16_item_source_audit.md`
6. `rebuild_plan/112_battle_choice_ui_p21_p4_p5_parity_matrix.md`
7. `rebuild_plan/111_battle_p17_ui_widget_runtime_parity_matrix.md`
8. `rebuild_plan/110_battle_p21_p17_catch_edge_closeout.md`
9. `rebuild_plan/109_battle_p5_p15_pet_switch_port_smoke.md`
10. `rebuild_plan/108_battle_p5_p15_pet_switch_source_audit.md`
11. `rebuild_plan/107_evolution_msgwarm_tutorial_evolve_slice_matrix.md`
12. `rebuild_plan/103_battle_levelup_evolution_queue_matrix.md`
13. `rebuild_plan/102_new_dev_chat_handoff_battle_engine_after_levelup.md`
14. `rebuild_plan/99_battle_petstate_item_exp_original_compare_audit.md`
15. `rebuild_plan/97_battle_p5_petstate_ui_logic_full_audit.md`
16. `rebuild_plan/82_battle_p21_p17_catch_edge_case_matrix.md`
17. `rebuild_plan/79_battle_item_pet_catch_state_matrix.md`
18. `rebuild_plan/78_battle_active_effect_lifecycle_full_matrix.md`
19. `rebuild_plan/76_battle_p12_p13_active_queue_lifecycle_matrix.md`
20. `rebuild_plan/74_battle_game_d_state_full_matrix.md`
21. `rebuild_plan/73_battle_rebuild_mapping_next_code_tasks.md`
22. `rebuild_plan/70_battle_unit_full_field_matrix.md`
23. `rebuild_game/src/main/java/VqsvBattleRuntime.java`
24. `rebuild_game/src/main/java/VqsvBattleRenderer.java`
25. `rebuild_game/src/main/java/VqsvUiLayout.java`
26. `rebuild_game/src/main/java/VqsvTextRenderer.java`
27. `rebuild_game/src/main/java/VqsvBattleUnit.java`
28. `rebuild_game/src/main/java/VqsvBattlePetStateView.java`
29. `rebuild_game/src/main/java/VqsvBattleLevelUpView.java`
30. `rebuild_game/src/main/java/VqsvSourceModels.java`
31. `rebuild_game/src/main/java/VqsvSmokeHarness.java`
32. `rebuild_game/src/main/java/VqsvText.java`

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
- `modules/ui/original/choice.ui`
- `modules/ui/original/petstate.ui`
- `modules/ui/original/msgwarm.ui`
- `modules/ui/original/openbox.ui`
- `modules/ui/original/choiceskill.ui`
- `modules/ui/original/levelUp.ui`
- `modules/ui/decoded/data__ui__choice.ui.json`
- `modules/ui/decoded/data__ui__petstate.ui.json`
- `modules/ui/decoded/data__ui__msgwarm.ui.json`
- `modules/ui/decoded/data__ui__openbox.ui.json`
- `modules/ui/decoded/data__ui__choiceskill.ui.json`
- `modules/ui/decoded/data__ui__levelUp.ui.json`
- `modules/ui/decoded/data__ui__battle.ui.json`
- `modules/ui/decoded/data__ui__world.ui.json`

## Current Java Source Structure

Core entry / scene:

- `VqsvIntroDemo.java`: main demo runtime, still the central shell, but much
  lighter than before.
- `VqsvSceneView.java`: scene rendering view helper.
- `VqsvSceneLoaders.java`: scene/map/resource loading helpers.
- `VqsvSceneActors.java`: actor creation/helpers.
- `VqsvSceneScriptSupport.java`: scene script support helpers.
- `VqsvScripts.java`, `Scene0IntroScript.java`,
  `Scene1Room3EntryScript.java`, `Scene1Room0Group0Script.java`,
  `Scene1Room1BunnyScript.java`, `Scene1Room0Group2ElderScript.java`,
  `Scene1Room0Group3PetScript.java`,
  `Scene1Room0Group6ElderBattleScript.java`: manual source-backed script
  slices.
- `VqsvScriptBlocks.java`: blocking/script primitives.
- `VqsvEventState.java`: event-state helpers.
- `VqsvFreeWorldRuntime.java`: current free-world runtime helpers.

Battle:

- `VqsvBattleRuntime.java`: battle state machine and source-shaped behavior.
  It is still large and is the main file for P4/P16/P5/P21/P17/P8/P22/P23.
- `VqsvBattleUnit.java`: source-shaped `game.b` battle unit fields, damage,
  item behavior, buff/debuff/stat hooks.
- `VqsvBattleRenderer.java`: battle UI, HUD, P7 effects, P5/P16 petstate,
  choice overlays, level/evolution visuals.
- `VqsvBattleTables.java`: source data table loaders.
- `VqsvBattleAnimationTables.java`: animation/effect table helpers.
- `VqsvBattlePetStateView.java`: petstate view model.
- `VqsvBattleLevelUpView.java`: level-up view model.
- `VqsvBattleScripts.java`: battle runtime factories for scripted battles.
- `VqsvBattleDamageFormulaCheck.java`: formula regression check.

Text / UI / assets:

- `VqsvText.java`: canonical Vietnamese text constants; do not hardcode
  mojibake strings in logic.
- `VqsvTextRenderer.java`: font, text boxes, openbox/taskTip/msgwarm rendering.
- `VqsvUiLayout.java`: binary-first `.ui` widget parser/runtime helper.
- `VqsvSpriteRenderer.java`: sprite frame/cell/anim helpers.
- `VqsvEffect.java`, `VqsvSourceEffects.java`: source-shaped effects.

Source models / persistence:

- `VqsvSourceModels.java`: source-like data structs and payloads.
- `VqsvSourceOps.java`: source-like operations such as inventory removal.
- `VqsvSourceRandom.java`: source random stream helpers.
- `VqsvSourceStoryState.java`: story state bridge.
- `VqsvSourceEvolutionRuntime.java`: evolution world/tutorial/runtime slices.
- `VqsvSaveRuntime.java`, `VqsvSavePromptBlocking.java`: save prompt/runtime.
- `VqsvWorldActors.java`: actor/temp sprite/world UI/choice box helpers.

Tests/smoke:

- `VqsvSmokeHarness.java`: headless smoke checkpoints. Add small checkpoints
  here rather than asking the user to replay manually.

## Current Battle Engine Status

### PORTED / PORTED-PARTIAL

- Core source-shaped state runtime exists for P0/P20/P3/P6/P2/P7/P12/P13/P15/P1/P8/P9/P4/P16/P5/P21/P17/P10/P11 shell.
- P7 damage result is preserved through damage frame enough for crit/debuff
  text/effect consumers at current granularity.
- `game.d.q()` post-skill behavior has source-shaped heal/buff/leech/reflect
  slices.
- P12/P13 active queue supports current buff/debuff lifecycle, HP-delta text,
  stat reset/reassert, death transitions, enemy replacement slice, and H/AH
  kind slices for current routes.
- P16 item validation/use ports `game.b.x/w` behavior `1..6` at current rebuild
  granularity.
- P4/P16-A flow is source-shaped:
  - P16 back returns to P4.
  - Success refreshes `petstate.ui`, opens `msgwarm.ui`, then confirm returns
    to P1.
  - Warnings return to P16 and preserve cursor.
- P16 inventory ownership is source-backed:
  - Source `q.b(item,1,0)` checks count only.
  - Source `game.b.w(item)` consumes once through `game.g.o().d(item,1,0)`.
  - Rebuild consumes once in runtime after apply; do not also consume inside
    `BattleUnit.applyBattleItem()` unless runtime consume is removed.
- P5 pet switch supports dead/current validation, forced replacement, pet
  reorder, HP/PP payload persistence, and source-shaped `petstate.ui`.
- P15 player switch reads original `cpos.mid` for current elder route.
- P21/P17 catch supports current Bunny tutorial, no-ball warning, chance/status
  multiplier, fail/success, storage/openbox, and RNG trace at current
  granularity.
- P8/P22/P23 EXP/level-up/learn-skill active participant slices exist:
  - EXP formula and threshold/stat refresh.
  - `levelUp.ui` renderer.
  - `choiceskill.ui` learn-skill flow.
- Evolution world notice/evolve UI slices exist for current direct smoke paths:
  - queue producer/consumer slices,
  - `msgwarm.ui` tutorial notice,
  - `evolve.ui` success/fail branches,
  - but full source queue parity is not complete.
- `choice.ui`, `petstate.ui`, and `msgwarm.ui` battle call sites are now
  fuller source-widget driven:
  - `VqsvUiLayout.load()` reads original binary UI first, decoded JSON fallback.
  - `bandHeight(id,fallback)` reduces hardcoded fill heights.
  - `text(id,fallback)` lets renderers use source labels where safe.
  - Current status remains `PORTED/PARTIAL`, not full generic `game.h`.

### Still PARTIAL / PENDING

- Full generic `game.h` UI framework (`ab/af` object graph, all widget modes,
  exact Java ME draw behavior) is not 100% ported.
- Exact MIDP pixel parity for UI/effects requires original-client pixel compare.
- Full P17 catch animation/rattle/flash timing and drawRGB parity is still
  partial.
- Full `game.d.x` multi-participant EXP/share vector and passive EXP share
  remain partial.
- Full `game.k` world pet menu modes are not complete.
- Full `game.b.J()` evolution queue through `game.k.H/L/I` and every consumer
  remains partial; direct smoke slices exist, but source-wide parity is not
  complete.
- P11 shop/SMS purchase is partial. SMS-only purchases may be treated as free
  by rebuild policy, but source side effects still need audit before claiming
  parity.
- Full broad skill coverage is partial.
- Full battle background/base marker/HUD/P7 actor motion/hit/recover/dead
  animation parity is partial.

## Current Verification Evidence

Recent closeout docs:

- `rebuild_plan/125_battle_p4_p16_a_flow_closeout.md`
- `rebuild_plan/126_battle_item_inventory_ownership_audit.md`
- `rebuild_plan/127_battle_choice_petstate_msgwarm_ui_runtime_closeout.md`

Recent UI runtime smoke PNG prefix:

- `rebuild_game/build_intro_demo/ui_runtime_fuller_*.png`

Representative checkpoints:

- `battle_elder_item_p4`
- `battle_choice_ui_scroll_source_rows`
- `battle_elder_item_target_p16`
- `battle_elder_pet_p5`
- `battle_msgwarm_source_widget_warning`
- `battle_p16_item_success_msgwarm`
- `battle_p4_blocked_item_warning`
- `battle_p16_item_hp_pp_full_warning`
- `battle_p16_success_confirm_to_p1`
- `battle_p16_warning_return_petstate_preserve_cursor`
- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`

Required commands after any battle/UI code slice:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp .\build\classes com.vqsv.rebuild.Main --check
java -cp .\build\classes VqsvBattleDamageFormulaCheck
$bad = Select-String -Path .\src\main\java\*.java -Pattern '�|Ã|Â|Ă|Ä|Æ' -AllMatches
if ($bad) { $bad | Select-Object -First 20; exit 1 }
java -cp .\build\classes VqsvIntroDemo --smoke-checkpoint <focused_checkpoint> build_intro_demo\<name>.png
```

Route regressions when battle runtime, UI flow, inventory, pet state, catch,
EXP, save, or event branch state was touched:

```powershell
java -cp .\build\classes VqsvIntroDemo --smoke-checkpoint route_sophie_after_battle_branch build_intro_demo\<name>_sophie.png
java -cp .\build\classes VqsvIntroDemo --smoke-checkpoint route_bunny_after_battle_task build_intro_demo\<name>_bunny.png
java -cp .\build\classes VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state build_intro_demo\<name>_elder.png
```

Do not use commands that open the live client/JAR unless the user explicitly
asks to play directly.

## Current Roadmap Position

Controlling roadmap:

`data model -> state machine -> UI -> formula/status -> item/catch/pet -> effect -> actor motion -> event integration -> skill coverage -> regression`

Current practical position:

- We are still in Battle Phase 5 cleanup: item/catch/pet switch behavior and
  their UI call sites.
- P4/P16 item flow + inventory ownership + UI runtime have just been closed at
  `PORTED/PARTIAL`.
- The next task should not be broad polish. Pick one source consumer, audit it,
  then port a small proven slice.

## Phase Gate Roadmap

Purpose:

- Prevent looping forever inside Phase 5.
- Make every phase have an entry condition, exit condition, and small task
  order.
- Keep later phases from starting until their source caller is wired.

Important rule:

- A phase can exit with `PORTED/PARTIAL` if every user-visible/current-route
  consumer is handled and remaining gaps are documented as future parity work.
  Do not block forever on MIDP pixel-perfect comparison unless the user makes
  that the active target.

### Phase 1 - Battle Data Model

Status: `AUDIT DONE / CODE PARTIAL`.

Source scope:

- `game.b`
- `game.g`
- `game.k`
- `aq.c` species/skill/item/status tables

Docs:

- `70_battle_unit_full_field_matrix.md`
- `71_battle_skill_status_table_matrix.md`
- `72_battle_full_skill_status_behavior_classification.md`
- `73_battle_rebuild_mapping_next_code_tasks.md`

Exit gate:

- Every important `game.b` field is mapped to rebuild, classified, or marked
  `UNKNOWN` with reason.
- No code slice may add random battle fields without updating the mapping.

Remaining follow-up:

- Only return here when a later phase proves a missing field is needed.

### Phase 2 - Turn/State Machine

Status: `PORTED/PARTIAL`.

Source scope:

- `game.d` battle state switch.
- `game.h` state UI calls.

Docs:

- `74_battle_game_d_state_full_matrix.md`
- `75_battle_game_d_q_post_skill_matrix.md`

Exit gate:

- Every main state P0/P20/P3/P6/P7/P12/P13/P15/P1/P8/P9/P4/P16/P5/P21/P17/P10/P11
  has a source matrix row.
- Current rebuild state transitions are not silent stubs.
- Remaining shell states are documented as `PARTIAL/PENDING`.

Do not loop here:

- If a later Phase 5 task needs a state edge, patch that state edge and return
  to Phase 5 rather than re-auditing the whole state machine.

### Phase 3 - UI Runtime

Status: `PORTED/PARTIAL`.

Source scope:

- `game.h`
- `.ui` files loaded by active battle states.

Current done slice:

- `choice.ui`, `petstate.ui`, `msgwarm.ui` battle call sites are fuller
  source-widget driven. See `127`.

Exit gate:

- UI files currently called by Phase 5 routes have source-widget smoke coverage.
- Full generic Java ME `game.h` runtime is explicitly marked `PENDING`.

Do not loop here:

- Do not polish a UI file unless a source battle state calls it and a smoke
  checkpoint exists.
- If a visual issue is only pixel-perfect MIDP comparison, document it and move
  on unless user explicitly selects pixel parity.

### Phase 4 - Damage / Status / Active Effects

Status: `PORTED/PARTIAL`.

Source scope:

- `game.b` formula/status methods.
- `game.d.q()`.
- P12/P13 active queue.
- `bufDebuf.mid`, `speffect.mid`.

Docs:

- `76_battle_p12_p13_active_queue_lifecycle_matrix.md`
- `77_battle_p12_p13_h_speffect_matrix.md`
- `78_battle_active_effect_lifecycle_full_matrix.md`

Exit gate:

- Current-route buff/debuff/status consumers are source-shaped and smoke
  covered.
- Remaining broad status/skill rows are listed for Phase 9 skill coverage.

Do not loop here:

- Do not port every status row just because it exists. Port rows when a skill,
  item, or event source caller reaches them.

### Phase 5 - Item / Catch / Pet Switch

Status: `ACTIVE / NEAR CLOSEOUT FOR CURRENT ROUTES`.

Source scope:

- P4/P16 item.
- P5/P15 pet switch.
- P21/P17 catch.
- `choice.ui`, `petstate.ui`, `msgwarm.ui`, `openbox.ui`.

Docs:

- `79_battle_item_pet_catch_state_matrix.md`
- `80_battle_p15_cpos_transition_matrix.md`
- `82_battle_p21_p17_catch_edge_case_matrix.md`
- `97_battle_p5_petstate_ui_logic_full_audit.md`
- `108_battle_p5_p15_pet_switch_source_audit.md`
- `109_battle_p5_p15_pet_switch_port_smoke.md`
- `110_battle_p21_p17_catch_edge_closeout.md`
- `111_battle_p17_ui_widget_runtime_parity_matrix.md`
- `112_battle_choice_ui_p21_p4_p5_parity_matrix.md`
- `124_battle_p4_p16_item_source_audit.md`
- `125_battle_p4_p16_a_flow_closeout.md`
- `126_battle_item_inventory_ownership_audit.md`
- `127_battle_choice_petstate_msgwarm_ui_runtime_closeout.md`

Task order to close Phase 5:

1. P4/P16 behavior-6 real item smoke if a real source item row/use case is
   reachable.
2. P21/P17 catch remaining edge cases and warning/openbox/message parity.
3. P5 residual data semantics only if source pet payload fields are needed by
   current UI or switch logic.
4. Write `129_battle_phase5_closeout_matrix.md` summarizing what is done,
   what is future pixel parity, and why Phase 6 can start.

Phase 5 exit gate:

- P4/P16 current route behavior is source-shaped and smoke covered.
- P5/P15 current route behavior is source-shaped and smoke covered.
- P21/P17 Bunny/catch current route behavior is source-shaped and smoke
  covered.
- Remaining gaps are either:
  - broad uncalled item/catch/pet modes,
  - exact MIDP pixel parity,
  - full generic `game.h` runtime,
  - or future `game.k` world menu work.
- A closeout doc exists and names the next Phase 6 target.

Do not loop here:

- Do not keep reworking `petstate.ui` unless a source fact or smoke failure
  proves a specific issue.
- Do not keep changing `choice.ui/msgwarm.ui` after `127` unless tied to
  P21/P17/P4/P16 source behavior.
- Do not start new inventory architecture unless `126` is disproven.

### Phase 6 - P7 Effect Animation Engine

Status: `PARTIAL / NEXT AFTER PHASE 5 CLOSEOUT`.

Source scope:

- `game.d case 7`
- `effect.mid`
- `speffect.mid`
- `blood.mid`
- `ah.java`
- `f.java`
- `l.java`

Existing docs:

- `56_battle_p7_animation_effect_matrix.md`
- `57_battle_speffect_ah_matrix.md`
- `58_battle_speffect_type9_port.md`
- `60_battle_p7_chunk_trigger_hooks_matrix.md`
- `61_battle_skill15_chunk4_audit.md`
- `62_battle_p7_source_parity_runtime_update.md`

Entry gate:

- Phase 5 closeout exists.
- The chosen effect has a real source skill/state caller.

Suggested task order:

1. Create/update a full P7 effect closeout matrix for currently smoked skills.
2. Pick one source-called skill/effect row.
3. Port only the next missing chunk/trigger/renderer type for that row.
4. Add three PNG checkpoints: start, active frame, after resolve.

Exit gate:

- Current story-route battle skills no longer look frozen or fake at the
  source-shaped level.
- Remaining AH/drawRGB parity is documented for pixel compare.

### Phase 7 - Actor Motion / Hit / Recover / Dead

Status: `PARTIAL`.

Source scope:

- `game.b.d(byte/state)`.
- `game.b.a(Graphics)`.
- actor `u` action rows.
- `cpos.mid`/battle placement when relevant.

Existing docs:

- `63_battle_actor_action_states_matrix.md`
- `64_battle_state1_l_effects_and_motion_matrix.md`
- `65_battle_state1_l_effect_full_audit.md`
- `67_battle_p7_actor_motion_camera_matrix.md`
- `68_battle_skill10_actor_u21_port.md`
- `69_battle_skill15_actor_u33_frame_trigger_port.md`

Entry gate:

- Phase 6 has enough effect timing that actor motion can attach to real frames.

Exit gate:

- Attack/hit/recover/dead states have source-shaped timing for currently
  routed battles.

### Phase 8 - Event Integration / Entry / Exit

Status: `PARTIAL`.

Source scope:

- Event opcodes that trigger battles.
- Battle result branches.
- Save/resume/task state.
- Free-world return state.

Entry gate:

- Phase 5/6/7 current battle behavior is stable enough that route transitions
  are not masking battle bugs.

Exit gate:

- Sophie/Bunny/Elder battle route regressions pass.
- Battle result state writes match source for current event paths.

### Phase 9 - Broad Skill Coverage

Status: `PARTIAL / LATER`.

Source scope:

- All `aq.c[1]` skill rows.
- `effect.mid`, `speffect.mid`, `bufDebuf.mid`, passive hooks.

Entry gate:

- Formula/status/effect/actor infrastructure exists enough to avoid one-off
  hacks.

Exit gate:

- Skill coverage matrix maps every skill to `PORTED / PENDING / UNKNOWN`.
- Important story/battle route skills have smoke coverage.

### Phase 10 - Regression Suite

Status: `PARTIAL / CONTINUOUS`.

Scope:

- Build/check/formula/mojibake.
- Headless smoke PNG.
- Route regressions.
- Pixel/sample assertions for UI/effects.

Exit gate:

- A single documented smoke command set can validate all completed phases.

## Phase Exercises For New Chat

Use these exercises to prove the new chat understands the roadmap instead of
wandering.

### Exercise A - Phase Gate Understanding

Answer:

1. Which phase are we currently in?
2. What exact docs prove that?
3. What are the exit gates for this phase?
4. Which tasks are explicitly forbidden because they cause looping?

Passing answer:

- Must name Phase 5.
- Must cite `125`, `126`, `127`, and at least one P5/P17/P21/P16/P15 doc.
- Must say Phase 5 can exit with `PORTED/PARTIAL` if current route consumers
  are source-shaped and remaining gaps are documented.

### Exercise B - Pick One Next Slice

Choose exactly one:

- P4/P16 behavior-6 smoke.
- P21/P17 catch remaining edge.
- P5 residual data semantics.
- Phase 5 closeout doc.

For the chosen slice, provide:

- source files,
- resource files,
- rebuild files,
- smoke checkpoint names,
- what is `UNKNOWN`,
- what would be out of scope.

Passing answer:

- Must not propose broad UI polish.
- Must not propose opening the client.
- Must not touch intro/world/panel.

### Exercise C - Phase 6 Readiness

Before starting Phase 6, answer:

1. Has `129_battle_phase5_closeout_matrix.md` been written?
2. Which current-route Phase 5 gaps remain?
3. Are those gaps blockers or future parity work?
4. Which P7 effect/skill row is the first Phase 6 target, and what source
   caller reaches it?

Passing answer:

- If `129` does not exist, write it first.
- Must choose a source-called effect row, not a decorative animation.

### Exercise D - Report Format After Every Slice

Every final report must include:

- `Done`: files/docs changed.
- `Verification`: build/check/formula/mojibake/smoke PNG.
- `Status`: `PORTED`, `PORTED/PARTIAL`, `PENDING`.
- `Next`: one roadmap-consistent next step.

If the report does not include `Next`, the slice is not closed.

Recommended next source-backed options:

1. P21/P17 catch edge/animation/message parity.
   - Audit exact remaining `game.h.ai()`, `game.d case 17`, `f/ah/H/aj/e(byte)`,
     storage full/bank/party payload, and warning return flow.
   - Best if user wants to close catch/Bunny path more.
2. P4/P16 behavior-6 real item smoke.
   - Choose a real source item row with behavior 6.
   - Prove validation/apply/count/clear state.
   - Add one focused smoke.
3. P5 residual parity.
   - Audit remaining `petstate.ui` relation/equipment semantics and `game.k`
     pet payload fields.
   - Do not invent relation text.
4. Evolution queue full parity.
   - Continue from `103`/`107`.
   - Audit `game.b.J()` -> `game.k.H/L/I` source-wide queue and consumers
     before adding animation/effects.

Avoid right now:

- Reopening intro/world/panel unless a battle source consumer requires it.
- Broad visual refactors in `VqsvBattleRenderer` without a smoke checkpoint.
- Moving inventory consume into `BattleUnit.applyBattleItem()` without removing
  the existing runtime consume.
- Claiming `choice.ui/petstate.ui/msgwarm.ui` are 100% complete; they are
  fuller battle call-site runtimes, not full Java ME UI runtime.

## Working Skill / Discipline Checklist

Use this as the daily loop:

1. Read the source method and table rows first.
2. Write or update an audit doc before code.
3. Identify the exact UI file/resource only after source calls it.
4. Make a tiny code slice.
5. Add or run smoke PNG checkpoints.
6. Run build/check/formula/mojibake.
7. Run route regressions if state can be affected.
8. Report:
   - what is `PORTED`,
   - what is `PORTED/PARTIAL`,
   - what remains `PENDING`,
   - what the next roadmap step is.

## Compulsory Entry Exercise

Answer in chat before coding. A passing answer must cite source/rebuild files
by relative path and classify every statement.

1. Prove P4/P16 item flow current state.
   - Source: `game.d case 4/16`, `game.h.aj()/ak()/W()/al()/bo()`,
     `game.b.x(item)`, `game.b.w(item)`, `game.g.b/d`.
   - Rebuild: `VqsvBattleRuntime.tickItemList()`,
     `VqsvBattleRuntime.tickItemTarget()`,
     `BattleUnit.applyBattleItem()`,
     `VqsvSourceOps.sourceRemoveItem()`.
   - Explain why consume-once in runtime is source-correct.

2. Prove the latest UI runtime slice.
   - Source/resources: `choice.ui`, `petstate.ui`, `msgwarm.ui` original and
     decoded files.
   - Rebuild: `VqsvUiLayout`, `VqsvBattleRenderer`, `VqsvTextRenderer`.
   - State exactly what `bandHeight()` and `text()` improve.
   - State why this is still `PORTED/PARTIAL`, not full `game.h`.

3. Build a mini matrix for one next target.
   - Choose exactly one: P21/P17 catch, P4/P16 behavior 6, P5 residual, or
     evolution queue.
   - Matrix columns must be:
     `source state/method -> UI/resource -> input -> side effect -> next state -> rebuild status`.
   - Include unknowns before implementation.

4. Write a PNG-only smoke plan.
   - Focused checkpoint(s).
   - Route regressions.
   - Pixel/sample assertion if UI is involved.
   - No live client/JAR.

5. Safety statement.
   - Files you may edit.
   - Files you must not touch.
   - Current `PENDING`/`PARTIAL` risks.

If the answer hand-waves, says "probably", claims pixel-perfect, proposes UI
without source caller, or proposes coding before source audit, stop and redo
the exercise.
