# Rebuild Plan Learning Index

Date: 2026-07-03

This is the recommended reading path through `rebuild_plan` for a new dev chat.
It groups the existing documents by purpose so the new chat does not read them
randomly and miss the current truth.

## Start Here

1. `42_new_dev_chat_handoff_after_refactor.md`
   - current handoff after source split/refactor
   - prime directive, current layout, commands, known remaining work

2. `43_project_layout_and_java_architecture.md`
   - current Java source structure and project layout
   - explains which files are framework, script, runtime, or hotspot

3. `44_new_dev_chat_training_and_exercises.md`
   - copy-paste prompt, required exercises, answer rubric, first safe tasks

## Closeout Truth For Current Playable Route

4. `40_intro_to_elder_battle_closeout_audit.md`
   - current closed scope from intro to elder battle reward
   - must read before claiming old sections are done

5. `41_battle_engine_three_stub_replacement_audit.md`
   - explains current battle slice
   - important because battle is improved, but still not full `game.d`

## Scene 1 Return / Elder / Pet / Battle Chain

6. `35_scene1_return_to_room0_group2_elder_audit.md`
   - post-Bunny return to room0 group2
   - actor52 interaction gate

7. `36_scene1_room0_group3_pet_selection_audit.md`
   - pet selection source chain and implemented behavior

8. `37_scene1_room0_group6_elder_battle_audit.md`
   - elder battle and reward chain

9. `38_scene1_post_group6_flow_audit.md`
   - what source seems to do after group6

10. `39_post_group6_freeworld_port_audit.md`
    - current post-group6 free-world slice

## Earlier Scene 1 Audits

11. `31_scene1_post_intro_original_init_trace.md`
    - original initialization after intro

12. `28_scene1_room0_group0_manual_script_audit.md`
    - ten-years-later room0 group0 manual script audit

13. `32_scene1_room1_freeworld_op13_audit.md`
    - transition/free-world trigger into Bunny map

14. `33_scene1_room1_group0_post_op13_audit.md`
    - room1 group0 after op13

15. `29_scene1_event_transition_chain_audit.md`
    - broader scene1 event transition notes

16. `30_scene1_room0_player_world_ui_correction.md`
    - correction notes around player/world/UI behavior

17. `22_scene1_room3_group0_manual_script_audit.md`
    - room3 entry/Sophie/Neil cutscene audit

## Resource / Runtime Study Notes

18. `24_scene1_room0_sprite_mapping_audit.md`
    - room0 sprite mapping and resource readiness

19. `25_module_source_learning_notes.md`
    - learning notes from module/source audit

20. `26_source_code_file_role_map.md`
    - role map for decompiled source files

21. `27_deep_runtime_resource_world_trace.md`
    - deeper runtime/resource/world trace

## Old Handoff Docs

22. `23_new_chat_handoff_training.md`
    - older handoff/training doc

23. `34_new_chat_handoff_current_state_and_exercises.md`
    - previous new-chat exercise set, still useful for historical context

These are not the latest entrypoint anymore. Prefer `42`, `43`, and `44` first.

## How To Use The Index

For a new dev chat:

1. Read `42`, `43`, `44`.
2. Answer the exercises in `44`.
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
| Scene1 room3 entry | PORTED/APPROX + battle pending | `22`, `40`, `41` |
| Scene1 room0 group0 | PORTED/APPROX | `28`, `31`, `40` |
| Room1 Bunny path | PORTED/APPROX + battle pending | `32`, `33`, `41` |
| Return room0 actor52/group2 | PORTED/APPROX | `35`, `40` |
| Pet selection group3 | PORTED/APPROX | `36`, `40` |
| Elder battle/reward group6 | PORTED/APPROX + battle pending | `37`, `41` |
| Post-group6 free world | PORTED/APPROX slice | `38`, `39` |
| Full battle engine | PENDING | `41` |
| Full UI runtime | PENDING | `40`, `41` |
| Generic event runner | PENDING | `42`, `43` |

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

