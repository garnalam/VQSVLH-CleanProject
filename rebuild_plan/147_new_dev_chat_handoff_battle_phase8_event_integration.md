# 147 New Dev Chat Handoff - Battle Phase 8 Event Integration

Status date: 2026-07-09

Status: CURRENT NEW DEV CHAT HANDOFF / TRAINING / ENTRY EXERCISE.

This supersedes older battle handoff docs such as:

- `81_new_chat_hand_off_battle_engine_current.md`
- `102_new_dev_chat_handoff_battle_engine_after_levelup.md`
- `128_new_dev_chat_handoff_battle_phase5_ui_runtime.md`

Keep older handoffs as historical context only. Their "next target" sections
are not current unless confirmed by this file and
`battle_engine_master_roadmap_progress.md`.

## Copy-Paste Prompt For A New Dev Chat

You are taking over the VQSV/Liet Hoa rebuild during Battle Phase 8:
battle entry/exit and event integration.

Hard rules:

- Source first. Do not guess logic, assets, UI, text, timings, animation, event
  links, or save behavior.
- Do not open the live client unless the user explicitly asks. Use headless
  smoke PNG checkpoints.
- Do not touch P7 or visual timing unless there is a concrete source-route
  mismatch or an original-client capture proving a regression.
- Do not genericize the full decoded event VM unless the task explicitly asks
  for a source-backed slice.
- Do not mutate `Scene.eventIndex` in Phase 8 bridge slices unless a source
  audit proves that exact mutation is safe.
- Keep `battleBranchTarget` as the current rebuild raw logical target. Source
  cursor is traced separately as `rawTarget - 2`.
- Classify every claim as `PORTED`, `PORTED/PARTIAL`, `APPROX`, `STUB`,
  `PENDING`, `UNKNOWN`, or `REBUILD_POLICY`.
- Do not claim pixel-perfect without original-vs-rebuild pixel compare.
- Logic before UI. Only port UI/effects when source proves the runtime calls it.
- After any code slice: run `build.ps1`, `Main --check`,
  `VqsvBattleDamageFormulaCheck` when battle/formula/runtime was touched, Java
  mojibake scan, `git diff --check`, focused smoke PNG, and route regressions
  for Bunny/Elder/Sophie/save if branch/task/save/free-world can be affected.

Before coding, read the required files below and answer the Compulsory Entry
Exercise in chat. You may not code until the exercise is answered with
source-backed, specific claims.

## Required Reading Order

Read in this order:

1. `rebuild_plan/battle_engine_master_roadmap_progress.md`
2. `rebuild_plan/147_new_dev_chat_handoff_battle_phase8_event_integration.md`
3. `rebuild_plan/146_battle_phase8_post_op47_downstream_consumer_audit.md`
4. `rebuild_plan/145_battle_phase8_sophie_descriptor_op47_audit.md`
5. `rebuild_plan/144_battle_phase8_op47_consumer_wrapper_audit.md`
6. `rebuild_plan/143_battle_phase8_entry_exit_event_integration_audit.md`
7. `rebuild_plan/142_battle_p7_phase6_closeout_and_next_phase.md`
8. `rebuild_plan/98_save_resume_bunny_task_source_audit.md`
9. `rebuild_plan/99_battle_petstate_item_exp_original_compare_audit.md`
10. `rebuild_plan/101_battle_levelup_learn_skill_evolution_audit.md`
11. `rebuild_plan/103_battle_levelup_evolution_queue_matrix.md`
12. `rebuild_plan/79_battle_item_pet_catch_state_matrix.md`
13. `rebuild_plan/74_battle_game_d_state_full_matrix.md`
14. `rebuild_plan/70_battle_unit_full_field_matrix.md`
15. `rebuild_plan/43_project_layout_and_java_architecture.md`

Source files to keep open:

- `modules/source_code/decoded/decompiled_source_cfr/game/c.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- `modules/source_code/decoded/decompiled_source_cfr/ae.java`
- `modules/source_code/decoded/decompiled_source_cfr/ah.java`
- `modules/source_code/decoded/decompiled_source_cfr/f.java`
- `modules/source_code/decoded/decompiled_source_cfr/l.java`

Event/resource files commonly needed:

- `modules/event/decoded/data__event__scene_1.mid.json`
- `modules/script/original/db.mid`
- `modules/script/original/effect.mid`
- `modules/script/original/speffect.mid`
- `modules/script/original/blood.mid`
- `modules/script/original/bufDebuf.mid`
- `modules/script/original/pos.mid`
- `modules/script/original/cpos.mid`
- `modules/ui/original/battle.ui`
- `modules/ui/original/world.ui`
- `modules/ui/original/openbox.ui`
- `modules/ui/original/msgwarm.ui`
- `modules/ui/original/msgconfirm.ui`
- `modules/ui/original/choice.ui`
- `modules/ui/original/petstate.ui`
- `modules/ui/original/choiceskill.ui`
- `modules/ui/original/levelUp.ui`

Current rebuild files to inspect before Phase 8 code:

- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleRenderer.java`
- `rebuild_game/src/main/java/VqsvBattleEventDescriptor.java`
- `rebuild_game/src/main/java/VqsvPostBattleDownstreamDescriptor.java`
- `rebuild_game/src/main/java/VqsvBattleScripts.java`
- `rebuild_game/src/main/java/Scene1Room1BunnyScript.java`
- `rebuild_game/src/main/java/Scene1Room0Group6ElderBattleScript.java`
- `rebuild_game/src/main/java/Scene1Room3EntryScript.java`
- `rebuild_game/src/main/java/Scene1Room0Group0Script.java`
- `rebuild_game/src/main/java/VqsvEventState.java`
- `rebuild_game/src/main/java/VqsvSavePromptBlocking.java`
- `rebuild_game/src/main/java/VqsvSaveRuntime.java`
- `rebuild_game/src/main/java/VqsvSourceOps.java`
- `rebuild_game/src/main/java/VqsvSourceEffects.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`
- `rebuild_game/src/main/java/VqsvText.java`

## Project Structure Snapshot

Workspace roots:

- `modules/source_code/decoded/decompiled_source_cfr/`: decompiled source.
  Treat this as the highest authority for logic.
- `modules/event/decoded/`: decoded event scripts, especially scene records and
  opcode args.
- `modules/ui/original/` and `modules/ui/decoded/`: UI binary and decoded UI
  data. Prefer binary-backed runtime where current code supports it.
- `modules/script/original/`: source tables for battle, effects, positions, and
  animation timing.
- `modules/spr`, `modules/img`, `modules/map`, `modules/mod`: source assets.
- `rebuild_game/src/main/java/`: Java rebuild runtime.
- `rebuild_plan/`: audits, handoffs, roadmap, and proof notes. New work must
  leave a source-backed audit trail here.

Core Java areas:

- Scene shell:
  - `VqsvIntroDemo.java`
  - `VqsvSceneView.java`
  - `VqsvSceneLoaders.java`
  - `VqsvSceneScriptSupport.java`
  - `VqsvScriptBlocks.java`
  - `VqsvFreeWorldRuntime.java`
- Manual scene scripts:
  - `Scene1Room3EntryScript.java`
  - `Scene1Room0Group0Script.java`
  - `Scene1Room1BunnyScript.java`
  - `Scene1Room0Group2ElderScript.java`
  - `Scene1Room0Group3PetScript.java`
  - `Scene1Room0Group6ElderBattleScript.java`
- Battle:
  - `VqsvBattleRuntime.java`
  - `VqsvBattleRenderer.java`
  - `VqsvBattleUnit.java`
  - `VqsvBattleTables.java`
  - `VqsvBattleAnimationTables.java`
  - `VqsvBattleScripts.java`
  - `VqsvBattleEventDescriptor.java`
  - `VqsvPostBattleDownstreamDescriptor.java`
- Source state and persistence:
  - `VqsvEventState.java`
  - `VqsvSourceModels.java`
  - `VqsvSourceOps.java`
  - `VqsvSourceEffects.java`
  - `VqsvSourceStoryState.java`
  - `VqsvSourceEvolutionRuntime.java`
  - `VqsvSavePromptBlocking.java`
  - `VqsvSaveRuntime.java`
- Tests/smoke:
  - `VqsvSmokeHarness.java`
  - `VqsvBattleDamageFormulaCheck.java`

## Current Battle Roadmap Position

Current phase:

```text
Phase 8: battle entry/exit + event integration.
```

Closed for current routes:

- P7/Phase 6 visual/effect timing is closed for current routes.
- Do not reopen P7 unless there is original-client capture or a real
  source-route mismatch.

Implemented Phase 8 bridge slices:

1. `143_battle_phase8_entry_exit_event_integration_audit.md`
   - Source chain mapped:
     `op67/op37 -> op32 -> game.i state12/13 -> game.d P8/P9/P22/P23
     -> world state10 -> op47`.
2. `144_battle_phase8_op47_consumer_wrapper_audit.md`
   - `consumeOp47(Scene)` traces/asserts source result consumption.
   - Source cursor is `op47Args[l] - 2`.
   - Rebuild keeps raw `battleBranchTarget`.
3. `145_battle_phase8_sophie_descriptor_op47_audit.md`
   - Sophie descriptor implemented:
     `op67 [56]`, `op37 [5,20,4]`, `op52 [1,1]`, `op32 [0,2]`,
     `op47 [78,78,0]`.
4. `146_battle_phase8_post_op47_downstream_consumer_audit.md`
   - Bunny/Elder downstream descriptor/trace-only coverage implemented.
   - It asserts source opcode clusters after manual route helpers run.
   - It does not execute helpers, mutate `eventIndex`, change save payload, or
     create a generic event VM.

Current route classification:

| Route / system | Status | Notes |
| --- | --- | --- |
| Bunny battle entry/op47 | PORTED/PARTIAL | Descriptor + op47 skip trace. |
| Bunny downstream task return | PORTED/PARTIAL | Downstream descriptor asserts `op56/op23/op40/op14`. |
| Elder battle entry/op47 | PORTED/PARTIAL | Descriptor + raw target `10`, source cursor `8`. |
| Elder reward/task/free-world | PORTED/PARTIAL | Downstream descriptor asserts reward/event-state/task/free-world cluster. |
| Sophie battle entry/op47 | PORTED/PARTIAL | Descriptor + raw target `78`, source cursor `76`. |
| Sophie post-battle sequence | PORTED/PARTIAL/APPROX | Still manual script; not part of downstream descriptor slice. |
| Save prompt after Bunny-task transition | PORTED/PARTIAL | Works through route snapshot; full RMS parity pending. |
| Generic decoded event VM | PENDING | Do not build broadly yet. |
| Full source RMS save records | PENDING | Do not claim exact original save parity. |
| P7 exact original pixel parity | PENDING | Closed for current route work unless new proof appears. |

## Immediate Next Target

Next roadmap-consistent work:

```text
Audit whether room1 group1 save prompt can be source-shaped as its own
op15/op56/op46/op14 group wrapper, without changing save payload/RMS parity.
```

Important source facts:

- Source room1 group1 is a separate event group:
  - `op15 [1,0,0]`
  - `op56 [0,1]` with actor string `50`, state string `0`
  - `op46` prompt text
  - `op14`
- Current rebuild inserts `VqsvSavePromptBlocking` in
  `Scene1Room0Group0Script` after loading room1 and before
  `Op13FreeWorldTrigger`.
- `VqsvSaveRuntime` is a rebuild route snapshot, not exact original RMS.
- A safe next slice should first audit only, then possibly add a trace/wrapper
  around the current save prompt call point.

Do not do next:

- Do not rewrite save payload to full RMS.
- Do not move the whole event runner to decoded VM.
- Do not change Bunny/Elder/Sophie battle branch semantics.
- Do not touch P7.
- Do not open the client; use PNG smoke.

## Required Verification Commands

Run from `E:\VQSVLH-CleanProject\rebuild_game` after code:

```powershell
.\build.ps1
java -cp build\libs\vqsv-rebuild-skeleton.jar com.vqsv.rebuild.Main --check
java -cp build\libs\vqsv-rebuild-skeleton.jar VqsvBattleDamageFormulaCheck
rg -n "Ãƒ|Ã‚|ï¿½" src\main\java
```

Focused smoke pattern:

```powershell
New-Item -ItemType Directory -Force -Path build\smoke_phase8_next | Out-Null
java -cp build\libs\vqsv-rebuild-skeleton.jar VqsvIntroDemo --smoke-checkpoint route_bunny_after_battle_task build\smoke_phase8_next\route_bunny_after_battle_task.png
java -cp build\libs\vqsv-rebuild-skeleton.jar VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state build\smoke_phase8_next\route_elder_after_battle_reward_state.png
java -cp build\libs\vqsv-rebuild-skeleton.jar VqsvIntroDemo --smoke-checkpoint route_sophie_after_battle_branch build\smoke_phase8_next\route_sophie_after_battle_branch.png
java -cp build\libs\vqsv-rebuild-skeleton.jar VqsvIntroDemo --smoke-checkpoint room1_bunny_save_prompt build\smoke_phase8_next\room1_bunny_save_prompt.png
java -cp build\libs\vqsv-rebuild-skeleton.jar VqsvIntroDemo --smoke-checkpoint room1_bunny_save_success build\smoke_phase8_next\room1_bunny_save_success.png
java -cp build\libs\vqsv-rebuild-skeleton.jar VqsvIntroDemo --smoke-checkpoint boot_title_continue_with_save build\smoke_phase8_next\boot_title_continue_with_save.png
```

Run from repo root after edits:

```powershell
git diff --check -- rebuild_game\src\main\java rebuild_plan
```

## Compulsory Entry Exercise

Before coding, answer these in chat:

1. Prove the current roadmap position.
   - Which phase are we in?
   - Which docs prove P7 is closed for current routes?
   - Which docs prove Bunny/Elder/Sophie descriptor/op47 coverage exists?

2. Explain `op47` accurately.
   - What does source `game.c case 47` do when result `l == -1`?
   - What does it do when `l != -1`?
   - Why must rebuild keep `battleBranchTarget` raw while tracing
     `sourceCursor = rawTarget - 2`?

3. Map current downstream consumers.
   - List Bunny downstream opcodes after successful catch.
   - List Elder downstream opcodes after battle.
   - Which file asserts them now?
   - Which parts are still manual route script behavior?

4. Explain the save prompt gap.
   - Where does source room1 group1 call `op15/op56/op46/op14`?
   - Where does rebuild currently insert `VqsvSavePromptBlocking`?
   - Why is full RMS parity still `PENDING`?

5. Write a safe next-slice plan.
   - Files to read.
   - Files that may be edited.
   - Files that must not be touched.
   - Expected statuses after the slice.
   - Required PNG smoke checkpoints.

6. Safety statement.
   - Confirm no P7 work.
   - Confirm no client launch.
   - Confirm no generic event VM.
   - Confirm no `eventIndex` mutation unless source audit proves it.

The new dev chat should stop after this exercise if any answer is vague,
guessed, or not tied to source/docs.

## Good First Audit Task

Create:

```text
148_battle_phase8_room1_group1_save_prompt_wrapper_audit.md
```

Audit scope:

- Source room1 group1 records in
  `modules/event/decoded/data__event__scene_1.mid.json`.
- Source `game.c case 15`, `case 46`, `case 56`, `case 14`.
- Rebuild `Scene1Room0Group0Script`, `VqsvSavePromptBlocking`,
  `VqsvSaveRuntime`, `VqsvEventState`, and `VqsvSmokeHarness`.
- Decide whether a trace-only save-prompt group wrapper is safe.

Do not code until the audit proves the slice.
