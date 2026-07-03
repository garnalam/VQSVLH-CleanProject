# Current Rebuild Handoff Bridge

Date: 2026-07-03

This file connects the old module audit docs in `modules\rebuild_plan` with the
current Java rebuild status in `..\rebuild_plan`.

## Two Documentation Areas

Path convention: `<PROJECT_ROOT>` means the folder that contains `modules`,
`rebuild_game`, and `rebuild_plan`. Do not hardcode local machine paths.

```text
<PROJECT_ROOT>\modules\rebuild_plan
```

Purpose: foundation/audit notes for original modules, source behavior,
resource formats, renderer, UI, world/event, battle, text/cutscene, and
SMS/payment.

```text
<PROJECT_ROOT>\rebuild_plan
```

Purpose: current rebuild progress, scene-specific audits, smoke results,
handoff docs, Java refactor state, and new-dev exercises.

## Read Order For A New Dev Chat

Start in the current progress folder:

1. `..\rebuild_plan\42_new_dev_chat_handoff_after_refactor.md`
2. `..\rebuild_plan\43_project_layout_and_java_architecture.md`
3. `..\rebuild_plan\44_new_dev_chat_training_and_exercises.md`
4. `..\rebuild_plan\45_rebuild_plan_learning_index.md`

Then read module foundation docs only as needed:

- resource/sprite/map/font task:
  - `05_resource_format_specs.md`
  - `07_resource_renderer_notes.md`
  - `15_renderer_primitive_deep_audit.md`

- text/cutscene task:
  - `20_text_cutscene_renderer_matrix.md`
  - `07_resource_renderer_notes.md`

- event/opcode task:
  - `04_opcode_matrix.md`
  - `09_world_event_notes.md`
  - `10_world_event_opcode_matrix.md`
  - `11_world_event_opcode_deep_audit.md`

- UI task:
  - `08_effect_ui_renderer_notes.md`
  - `12_ui_system_notes.md`
  - `16_ui_workflow_matrix.md`

- battle task:
  - `13_gameplay_battle_save_notes.md`
  - `17_battle_state_machine.md`
  - `18_battle_formula_status_matrix.md`

- world actor/free-world task:
  - `09_world_event_notes.md`
  - `19_world_tick_actor_matrix.md`

## Current Java Rebuild Truth

Current manual route:

```text
scene0 intro
-> scene1 room3 entry
-> room0 group0 ten-years-later village
-> room1 Bunny path
-> return room0
-> actor52 elder interaction / room0 group2
-> room0 group3 pet selection
-> room0 group6 elder battle/reward
-> post-group6 free-world slice
```

Important current Java files:

```text
..\rebuild_game\src\main\java\VqsvIntroDemo.java
..\rebuild_game\src\main\java\VqsvText.java
..\rebuild_game\src\main\java\VqsvTextRenderer.java
..\rebuild_game\src\main\java\VqsvSpriteRenderer.java
..\rebuild_game\src\main\java\VqsvWorldActors.java
..\rebuild_game\src\main\java\VqsvScriptBlocks.java
..\rebuild_game\src\main\java\VqsvEventState.java
..\rebuild_game\src\main\java\VqsvBattleRuntime.java
..\rebuild_game\src\main\java\Scene*Script.java
```

Current biggest unfinished areas:

- full `game.d` battle engine;
- full `game.c` event runner;
- full `game.h` UI runtime;
- pixel-perfect `game.j/s.java` text renderer;
- full opcode 9 effect semantics;
- post-group6 broader world progression.

## Rule For Using Old Notes

Old module notes are useful, but they are not enough by themselves. For every
implementation task, the dev chat must still cite the exact original source or
resource file it used, then verify the rebuild with build/check/smoke.


