# Rebuild Plan Learning Index

Date: 2026-07-08

This is the recommended reading path through `rebuild_plan` for a new dev chat.
It groups the existing documents by purpose so the new chat does not read them
randomly and miss the current truth.

## Start Here

1. `102_new_dev_chat_handoff_battle_engine_after_levelup.md`
   - latest battle-engine handoff after catch, petstate, save, EXP/levelUp,
     and learn-skill slices
   - includes current rules, required reading, current status, next target, and
     compulsory entry exercise

2. `42_new_dev_chat_handoff_after_refactor.md`
   - current handoff after source split/refactor
   - prime directive, current layout, commands, known remaining work

3. `43_project_layout_and_java_architecture.md`
   - current Java source structure and project layout
   - explains which files are framework, script, runtime, or hotspot

4. `44_new_dev_chat_training_and_exercises.md`
   - copy-paste prompt, required exercises, answer rubric, first safe tasks

5. `47_battle_scripts_refactor_audit.md`
   - latest small refactor audit for moving Bunny/elder battle factory wrappers
     into `VqsvBattleScripts.java`

6. `48_intro_demo_remaining_scene_audit.md`
   - latest audit-only map of what remains in `VqsvIntroDemo.Scene`
   - classifies remaining areas as `KEEP_CORE`, `MOVE_SAFE`, `MOVE_LATER`,
     and `DO_NOT_TOUCH_YET`

7. `49_scene_tick_input_runner_audit.md`
   - audit-only map of `press0`, `click`, `setMoveKey`, and `tick`
   - documents why input/tick runner has not been moved yet

## Closeout Truth For Current Playable Route

7. `40_intro_to_elder_battle_closeout_audit.md`
   - current closed scope from intro to elder battle reward
   - must read before claiming old sections are done

8. `41_battle_engine_three_stub_replacement_audit.md`
   - explains current battle slice
   - important because battle is improved, but still not full `game.d`

9. `50_battle_full_engine_port_plan.md`
   - current forward plan for replacing the remaining `PORTED/APPROX` battle
     slice with a source-backed `game.d/game.b/game.h` runtime
   - includes the immediate fix that removes the visible `Scripted stub`
     overlay and the staged implementation order

## Scene 1 Return / Elder / Pet / Battle Chain

10. `35_scene1_return_to_room0_group2_elder_audit.md`
   - post-Bunny return to room0 group2
   - actor52 interaction gate

11. `36_scene1_room0_group3_pet_selection_audit.md`
   - pet selection source chain and implemented behavior

12. `37_scene1_room0_group6_elder_battle_audit.md`
   - elder battle and reward chain

13. `38_scene1_post_group6_flow_audit.md`
   - what source seems to do after group6

14. `39_post_group6_freeworld_port_audit.md`
    - current post-group6 free-world slice

## Earlier Scene 1 Audits

15. `31_scene1_post_intro_original_init_trace.md`
    - original initialization after intro

16. `28_scene1_room0_group0_manual_script_audit.md`
    - ten-years-later room0 group0 manual script audit

17. `32_scene1_room1_freeworld_op13_audit.md`
    - transition/free-world trigger into Bunny map

18. `33_scene1_room1_group0_post_op13_audit.md`
    - room1 group0 after op13

19. `29_scene1_event_transition_chain_audit.md`
    - broader scene1 event transition notes

20. `30_scene1_room0_player_world_ui_correction.md`
    - correction notes around player/world/UI behavior

21. `22_scene1_room3_group0_manual_script_audit.md`
    - room3 entry/Sophie/Neil cutscene audit

## Resource / Runtime Study Notes

22. `24_scene1_room0_sprite_mapping_audit.md`
    - room0 sprite mapping and resource readiness

23. `25_module_source_learning_notes.md`
    - learning notes from module/source audit

24. `26_source_code_file_role_map.md`
    - role map for decompiled source files

25. `27_deep_runtime_resource_world_trace.md`
    - deeper runtime/resource/world trace

## Old Handoff Docs

22. `23_new_chat_handoff_training.md`
    - older handoff/training doc

23. `34_new_chat_handoff_current_state_and_exercises.md`
    - previous new-chat exercise set, still useful for historical context

These are not the latest entrypoint anymore. Prefer `42`, `43`, and `44` first.

For battle-engine continuation, prefer `102` first. `81` is superseded and only
historical.

## How To Use The Index

For a new dev chat:

1. If the task is battle-engine work, read `102` first and answer its exercise.
2. If the task is general refactor/scene work, read `42`, `43`, `44`.
3. Read only the audit docs relevant to the chosen task.
4. Before coding, state:
   - exact source files used
   - exact rebuild files to edit
   - expected smoke commands
   - expected `PORTED/APPROX/STUB/PENDING` status after the task

## Current Project Status In One Table

| Area | Status | Primary docs |
|---|---|---|
| Intro/scene0 | PORTED/APPROX, user-approved baseline | `40`, `42`, `43` |
| Scene1 room3 entry | PORTED/APPROX + battle partial | `22`, `40`, `102` |
| Scene1 room0 group0 | PORTED/APPROX | `28`, `31`, `40` |
| Room1 Bunny path | PORTED/APPROX + battle PORTED/PARTIAL | `32`, `33`, `92`, `102` |
| Return room0 actor52/group2 | PORTED/APPROX | `35`, `40` |
| Pet selection group3 | PORTED/APPROX | `36`, `40` |
| Elder battle/reward group6 | PORTED/APPROX + battle PORTED/PARTIAL | `37`, `41`, `102` |
| Post-group6 free world | PORTED/APPROX slice | `38`, `39` |
| Full battle engine | PARTIAL, active route smoke-covered in many slices | `74..102` |
| Full UI runtime | PENDING/PARTIAL, source-shaped battle/world widgets only | `84`, `97`, `99..102` |
| Generic event runner | PENDING | `42`, `43` |

## Recent Cleanup Status

- DONE: `OldRoom0Group3PetOffer` removed after proving it was unused.
- DONE: `Room0Group3PetOffer` moved into
  `Scene1Room0Group3PetScript.java` without behavior changes.
- DONE: `Room0Group6Start` and `Room0PostGroup6FreeWorld` moved into
  `Scene1Room0Group6ElderBattleScript.java` without behavior changes.
- DONE: battle UI drawing extracted into `VqsvBattleRenderer.java` without
  intended runtime behavior changes.
- DONE: map/room loaders extracted into `VqsvSceneLoaders.java` without
  intended runtime behavior changes.
- DONE: inventory/reward/source gameplay ops extracted into
  `VqsvSourceOps.java` without intended runtime behavior changes.
- DONE: smoke harness extracted into `VqsvSmokeHarness.java` without intended
  smoke behavior changes.
- DONE: source event side-effect helpers extracted into
  `VqsvSourceEffects.java` without intended runtime behavior changes.
  `op9` effect parity remains `APPROX/PENDING`; this was a move-only cleanup.
- DONE: free-world movement/transition/collision helpers extracted into
  `VqsvFreeWorldRuntime.java` without intended runtime behavior changes.
  Full `game.g.q()` movement/collision remains pending; this was a move-only
  cleanup.
- DONE: scene camera/render helpers extracted into `VqsvSceneView.java`
  without intended runtime behavior changes. Full original renderer/UI parity
  remains pending; this was a move-only cleanup.
- DONE: battle factory wrappers extracted into `VqsvBattleScripts.java`
  without intended runtime behavior changes.
- DONE: actor bootstrap table extracted into `VqsvSceneActors.java` without
  intended runtime behavior changes.
- DONE: actor/dialog script support implementation moved into
  `VqsvSceneScriptSupport.java` without intended runtime behavior changes.
  This includes `setActive`, `hide`, `dialog`, `taskNotice`, and
  `waitForText`.
- DONE: unused `sourceStateApprox` helper removed after a dedicated call-site
  scan found no Java callers.
- DONE: input/tick runner audited in `49_scene_tick_input_runner_audit.md`;
  no runner code was moved.
- Next recommended cleanup:
  pause refactor and continue source route porting, or audit the next tiny
  source-clean candidate before touching input/tick.

## Documentation Rules For Future Audits

When adding a new audit doc:

- use the next numeric prefix
- state date and scope
- list exact source references
- include a record/opcode matrix when event logic is involved
- separate source facts from rebuild implementation
- mark every uncertain item as `APPROX`, `PENDING`, `UNKNOWN`, or `DAMAGED`
- include smoke commands and what each smoke proves
- explicitly list what is still not complete

