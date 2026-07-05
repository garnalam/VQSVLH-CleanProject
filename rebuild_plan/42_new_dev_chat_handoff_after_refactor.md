# New Dev Chat Handoff After Refactor

Updated after the rebuild project split pass that extracted script blocks,
script files, event state, and battle runtime.

## Prime Directive

- Follow the original game. Do not invent images, sprite mappings, timings,
  event chains, battle results, UI behavior, or state transitions.
- Every meaningful behavior must be backed by one of:
  - decoded event JSON under `modules/event/decoded`
  - decoded/source assets under `modules`
  - decompiled source under `modules/source_code`
  - an existing rebuild audit document under `rebuild_plan`
- Mark behavior honestly as `PORTED`, `APPROX`, `STUB`, `PENDING`, or
  `UNKNOWN`.
- Do not touch user-approved sections unless the current task explicitly
  requires it.
- Do not run the interactive client unless the user explicitly asks. Prefer
  PNG-only smoke tests.
- Do not run `java -jar` for `VqsvIntroDemo` smoke. Use classpath execution.
- If editing code, build/check/smoke after each risky split.

## Required Read Order

Read these before coding:

1. `rebuild_plan/43_project_layout_and_java_architecture.md`
2. `rebuild_plan/44_new_dev_chat_training_and_exercises.md`
3. `rebuild_plan/45_rebuild_plan_learning_index.md`
4. `rebuild_plan/40_intro_to_elder_battle_closeout_audit.md`
5. `rebuild_plan/41_battle_engine_three_stub_replacement_audit.md`
6. `rebuild_plan/35_scene1_return_to_room0_group2_elder_audit.md`
7. `rebuild_plan/36_scene1_room0_group3_pet_selection_audit.md`
8. `rebuild_plan/37_scene1_room0_group6_elder_battle_audit.md`
9. `rebuild_plan/38_scene1_post_group6_flow_audit.md`
10. `rebuild_plan/39_post_group6_freeworld_port_audit.md`
11. `rebuild_game/src/main/java/VqsvIntroDemo.java`
12. `rebuild_game/src/main/java/VqsvScriptBlocks.java`
13. `rebuild_game/src/main/java/VqsvEventState.java`
14. `rebuild_game/src/main/java/VqsvBattleRuntime.java`
15. All `Scene*Script.java` files in `rebuild_game/src/main/java`

Reference source files when needed:

- `modules/event/decoded/data__event__scene_0.mid.json`
- `modules/event/decoded/data__event__scene_1.mid.json`
- `modules/source_code/decoded/decompiled_source_cfr/game/c.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/script/decoded/data__script__sprite.mid.json`

## Current Project Layout

Project root:

```text
<PROJECT_ROOT>
```

Path convention:

- `<PROJECT_ROOT>` is the folder that contains `modules`, `rebuild_game`, and
  `rebuild_plan`.
- Do not hardcode a machine-specific path. In commands, set
  `$env:PROJECT_ROOT`, `$env:MODULES_ROOT`, and `$env:REBUILD_GAME` first.

Main rebuild project:

```text
rebuild_game/
  build.ps1
  run.ps1
  src/main/java/
```

Asset/source modules:

```text
modules/
  event/decoded/
  event/original/
  img/decoded/
  map/original/
  mod/original/
  spr/original/
  script/decoded/
  source_code/decoded/decompiled_source_cfr/
  ui/original/
```

Audit docs:

```text
rebuild_plan/
```

## Current Java Source Structure

`VqsvIntroDemo.java`

- Still the main runnable demo/client and smoke entrypoint.
- Still contains the large `Scene` class.
- Still contains the coordinator shell for input/tick/event-runner state and
  thin wrappers around extracted source/free-world/view helpers.
- It is no longer the place where the main event list is handwritten inline.
  `makeEvents()` now delegates to script files.

`Scene0IntroScript.java`

- Manual source-backed scene 0 intro chain.
- Status: `PORTED/APPROX`.
- User-approved baseline. Do not alter casually.

`Scene1Room3EntryScript.java`

- Scene 1 room3 entry and Sophie/Neil kidnapping cutscene.
- Contains the Sophie kidnapping battle trigger.
- Status: `PORTED/APPROX + STUB battle`.

`Scene1Room0Group0Script.java`

- Ten-years-later village tutorial start.
- Leads into Bunny map through op10 movement and room1 transition.
- Status: `PORTED/APPROX`.

`Scene1Room1BunnyScript.java`

- Bunny capture path after op13.
- Writes `op23 [1,0,1]` and `op14 [1,1,0]`.
- Returns toward room0 through actor transition.
- Status: `PORTED/APPROX + STUB battle`.

`Scene1Room0Group2ElderScript.java`

- Elder interaction after Bunny.
- Must not auto-run. It starts after actor52 interaction through op86/op16.
- Status: `PORTED/APPROX`.

`Scene1Room0Group3PetScript.java`

- Owns `Room0Group3PetOffer`.
- Pet selection blocking behavior was moved out of `VqsvIntroDemo` during the
  cleanup pass.
- Status: `PORTED/APPROX`; op9 and pet inventory semantics still approximate.

`Scene1Room0Group6ElderBattleScript.java`

- Elder battle/reward/free-world unlock script.
- Owns `Room0Group6Start` and `Room0PostGroup6FreeWorld`.
- Calls `SourceBattleRuntime` for the elder battle slice.
- Status: `PORTED/APPROX + STUB battle`.

`VqsvScriptBlocks.java`

- Generic blocking primitives and free-world triggers:
  - `Delay`
  - `Opcode34Counter`
  - `Move`
  - `ActionSet`
  - `TimedAction`
  - `Op10PlayerTimedAction`
  - `Path`
  - `CameraPan`
  - `CameraPanPoint`
  - `Op13FreeWorldTrigger`
  - `ActorTransitionFreeWorldTrigger`
  - `ActorInteractionFreeWorldTrigger`
- Also contains top-level `Event` and `Blocking`.

`VqsvEventState.java`

- Owns event state map and trace list.
- Provides:
  - `sourceEventState`
  - `sourceEventStateComplete`
  - `op15CheckEventState`
  - `op86CheckEventState`
  - `op23MarkEventComplete`
  - `op14CompleteEvent`
- `Scene` keeps thin wrappers for existing call sites.

`VqsvBattleRuntime.java`

- Contains `SourceBattleRuntime`.
- This is still a controlled `STUB/APPROX`, not full `game.d`.
- It preserves branch/result behavior for:
  - Sophie kidnapping battle
  - Bunny capture
  - Elder battle

`VqsvBattleRenderer.java`

- Owns current battle overlay/HUD drawing helpers.
- Status: `PORTED/APPROX`; this is a renderer extraction only, not full
  `game.h/game.d` battle UI parity.

`VqsvSceneLoaders.java`

- Owns current map/room loader tables moved out of `VqsvIntroDemo.Scene`.
- Status: move-only extraction, no intended runtime behavior change.
- Contains `loadMapRenderer` and loaders for:
  - `loadScene7Room2`
  - `loadRoom1`
  - `loadScene5Room3`
  - `loadScene1Room3Entry`
  - `loadScene1Room0`
  - `loadScene1Room1`
  - `loadScene1Room2`
- `VqsvIntroDemo.Scene` keeps thin wrapper methods so existing scripts still
  call `s.loadScene...(...)` unchanged.

`VqsvText.java`

- Centralized Vietnamese text/constants.
- Prefer adding text here instead of putting Vietnamese literals in logic files.

`VqsvTextRenderer.java`

- `FontBitmap` and `TextBox`.
- Critical for mojibake, dialog, task tip, open box, prompt behavior.

`VqsvSpriteRenderer.java`

- Sprite data/animation loading and drawing.

`VqsvWorldActors.java`

- `Actor`, `TempSprite`, `WorldUi`, `ChoiceBox`.

`VqsvSourceModels.java`

- Source-like item/reward/battle unit/pet data models.

`VqsvSourceOps.java`

- Owns current source-backed inventory/reward opcode helpers moved out of
  `VqsvIntroDemo.Scene`.
- Status: move-only extraction, no intended runtime behavior change.
- Contains:
  - `op17Item`
  - `op31CurrencyReward`
  - `op19SpecialReward`
  - source bag add/remove/check/count helpers
  - source item id mapping for currently used items
  - source inventory/reward popup helper
- `VqsvIntroDemo.Scene` keeps thin wrappers so script files still call
  `s.op17Item(...)`, `s.op31CurrencyReward(...)`, and
  `s.op19SpecialReward(...)` unchanged.

`VqsvSourceEffects.java`

- Owns current source event side-effect helpers moved out of
  `VqsvIntroDemo.Scene`.
- Status: move-only extraction, no intended runtime behavior change.
- Contains:
  - `op5ActorEffect`
  - `op9SourceEffect`
  - `op25SetGameFlag`
  - `op39RefreshPets`
  - `op56ActorVisibility`
  - `op67SetBattleActor`
  - local helpers for op9 argument/default/color handling
- Keep this separate from `VqsvSourceOps.java`; `VqsvSourceOps.java` is for
  inventory/reward/source gameplay ops, while this file is for event
  side-effects. `op9` visual parity is still `APPROX/PENDING`, even though the
  current helper has been extracted.
- `VqsvIntroDemo.Scene` keeps thin wrappers so script files still call
  `s.op5ActorEffect(...)`, `s.op9SourceEffect(...)`, `s.op25SetGameFlag(...)`,
  `s.op39RefreshPets()`, `s.op56ActorVisibility(...)`, and
  `s.op67SetBattleActor(...)` unchanged.

`VqsvSmokeHarness.java`

- Owns smoke-only CLI harness code moved out of `VqsvIntroDemo.java`.
- Contains `--smoke`, `--smoke-drive`, `--smoke-checkpoint`, route driving,
  checkpoint setup, smoke image rendering, and smoke placement helpers.
- `VqsvIntroDemo.main()` now only dispatches smoke commands to this harness.
- Current movement/transition checkpoints include:
  - `room1_op13_bunny_trigger`
  - `return_room0_transition`
  - `actor52_interaction_group2`
  - `post_group6_room2_entry_tip`
  - `post_group6_room0_back_from_room2`

`VqsvFreeWorldRuntime.java`

- Owns current free-world movement, source transition, and source-shaped
  collision/interaction helpers moved out of `VqsvIntroDemo.Scene`.
- Status: move-only extraction, no intended runtime behavior change.
- Contains:
  - transition setup/state helpers
  - `sourceTransitionRequiredDirection`
  - `trySourceTransition`
  - implemented target loaders for current scene1 room0/room1/room2 slice
  - player placement helpers
  - `tickFreeWorldPlayer`
  - source rectangle trigger check for `op13`
  - actor mask overlap checks for type-1 transitions and `op16`/`op38`
  - stop-player helper used when source events fire
- `VqsvIntroDemo.Scene` keeps thin wrappers so scripts and smoke harness still
  call `s.tickFreeWorldPlayer()`, `s.trySourceTransition(...)`,
  `s.playerIntersects...(...)`, and related methods unchanged.
- Important: this is not full `game.g.q()` movement/collision. Map-boundary
  movement remains `APPROX`; full tile/actor collision remains pending.

`VqsvSceneView.java`

- Owns current scene camera and render helpers moved out of
  `VqsvIntroDemo.Scene`.
- Status: move-only extraction, no intended runtime behavior change.
- Contains:
  - `render`
  - map layer / actor layer / player rendering helpers
  - camera center/pan/follow helpers
  - local camera clamp helper
- `VqsvIntroDemo.Scene` keeps thin wrappers so scripts/loaders/smoke still
  call `s.render(...)`, `s.setCameraCenter(...)`, `s.moveCameraToward(...)`,
  `s.cameraCenteredOn(...)`, `s.followActor(...)`, and
  `s.stopCameraFollow()` unchanged.

`VqsvEffect.java`

- Visual effects used by current manual scripts.

## Known Remaining Large Areas

`VqsvIntroDemo.java` is still large. Do not assume the refactor is complete.

Most obvious next splits:

1. DONE: remove `OldRoom0Group3PetOffer`.
2. DONE: move `Room0Group3PetOffer` into `Scene1Room0Group3PetScript.java`.
3. DONE: move `Room0Group6Start` and `Room0PostGroup6FreeWorld` into
   `Scene1Room0Group6ElderBattleScript.java`.
4. DONE: extract battle UI drawing into `VqsvBattleRenderer.java`.
5. DONE: extract map/room loaders into `VqsvSceneLoaders.java`.
6. DONE: extract inventory/reward/source gameplay ops into `VqsvSourceOps.java`.
7. DONE: extract smoke harness into `VqsvSmokeHarness.java`.
8. DONE: extract source event side-effect helpers into
   `VqsvSourceEffects.java`.
9. DONE: extract free-world movement/transition/collision helpers into
   `VqsvFreeWorldRuntime.java`.
10. DONE: extract scene camera/render helpers into `VqsvSceneView.java`.

Next cleanup is not chosen yet. Before coding, audit the remaining
`VqsvIntroDemo.Scene` responsibilities and recommend the smallest move-only
split, likely one of:

- input/tick/event-runner shell separation
- actor/dialog utility wrappers
- battle runtime factory wrappers

## Current Behavior Scope

The rebuild currently covers a manual path from boot/new game through:

```text
scene0 intro
-> scene1 room3 entry
-> ten-years-later room0 group0
-> room1 Bunny capture
-> return room0
-> actor52 elder interaction / room0 group2
-> pet selection / room0 group3
-> elder battle/reward / room0 group6
-> post-group6 free-world slice
```

This path is not a generic `game.c` event runner yet.

## Important Source-Proven Chain

After Bunny:

- room1 group0 writes `op23 [1,0,1]`
- room1 group0 writes `op14 [1,1,0]`
- room0 group2 is gated by `op86 [1,1,0]`
- room0 group2 record 1 is `op16 [52]`
- actor 52 is elder/truong thon interaction
- group2 must not auto-run

Pet selection:

- room0 group3 gated by `op15 [1,0,2]`
- shows actors `53,54,55`
- `op38` waits pet interaction
- `op35` shows Yes/No choice
- `op87` grants pet approximation
- `op14` completes `state[1,0,3]`

Elder battle:

- room0 group6 gated by `op15 [1,0,3]`
- battle setup uses actor 52 and encounter species 68 level 5 nature 1
- current result path resolves `0 -> branch 10`
- rewards and state write set `state[1,0,6]=3`

## Commands

Set path variables:

```powershell
$env:PROJECT_ROOT = "<path-to-project-root>"
$env:MODULES_ROOT = Join-Path $env:PROJECT_ROOT "modules"
$env:REBUILD_GAME = Join-Path $env:PROJECT_ROOT "rebuild_game"
cd $env:REBUILD_GAME
```

Build:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
```

Check:

```powershell
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" com.vqsv.rebuild.Main --check
```

Mojibake scan:

```powershell
rg -n "Ãƒ|Ã‚|Ã„|Ã†|Ã¡Âº|Ã¡Â»|Ã…|â‚¬|Å“|â„¢|Å¡|Å¸" "$env:REBUILD_GAME\src\main\java" -g "*.java"
```

Open playable demo from ten-years-later checkpoint:

```powershell
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo 5400
```

PNG-only smoke examples:

```powershell
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke "$env:REBUILD_GAME\build_intro_demo\dev_10years_t5400.png" 5400
```

```powershell
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint actor52_interaction_group2 "$env:REBUILD_GAME\build_intro_demo\dev_actor52_group2.png"
```

```powershell
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint room0_pet_choice_ui "$env:REBUILD_GAME\build_intro_demo\dev_pet_choice.png"
```

```powershell
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state "$env:REBUILD_GAME\build_intro_demo\dev_elder_reward.png"
```

## Smoke Caveats

- `route_*` checkpoints often verify branch/state and then render the post-route
  dialog/reward, not the battle overlay itself.
- Battle overlay checkpoints show the current stub/debug battle UI. They do
  not prove full original battle UI correctness.
- PowerShell console can display Vietnamese incorrectly. Trust PNG and source
  scan more than raw console mojibake.

## Compulsory Entry Exercise

Before editing code, answer these in chat.

### Exercise 1: Architecture

List the responsibility of each file:

- `VqsvIntroDemo.java`
- `VqsvScriptBlocks.java`
- `VqsvEventState.java`
- `VqsvBattleRuntime.java`
- `Scene1Room0Group2ElderScript.java`
- `Scene1Room0Group3PetScript.java`
- `Scene1Room0Group6ElderBattleScript.java`

Expected answer must mention:

- `VqsvIntroDemo.Scene` is still too large.
- `SourceBattleRuntime` is still `STUB/APPROX`.
- event script ordering is now delegated through `Scene*Script.appendTo(e)`.

### Exercise 2: Source Chain Proof

Prove the return-to-elder chain:

- room1 group0 writes `op23 [1,0,1]`
- room1 group0 writes `op14 [1,1,0]`
- room0 group2 is gated by `op86 [1,1,0]`
- room0 group2 starts only after `op16 [52]`

Use:

- `Scene1Room1BunnyScript.java`
- `Scene1Room0Group2ElderScript.java`
- `VqsvScriptBlocks.java`
- `VqsvEventState.java`
- `rebuild_plan/35_scene1_return_to_room0_group2_elder_audit.md`

### Exercise 3: Status Honesty

Classify these as `PORTED`, `APPROX`, `STUB`, `PENDING`, or `UNKNOWN`:

- `ChoiceBox` pet selection UI
- actor52 interaction trigger
- Bunny battle
- elder battle
- `op9` effect rendering
- generic event runner for `game.c`
- post-group6 Dodo side quest

Expected:

- ChoiceBox: rendered/smoked, but original `game.h` UI runtime is not fully
  ported.
- actor52 trigger: source-backed `PORTED/APPROX`.
- Bunny/elder battles: `STUB/APPROX`, not full `game.d`.
- `op9`: `APPROX/PENDING`.
- generic event runner: `PENDING`.
- Dodo side quest: `PENDING`.

### Exercise 4: Smoke Plan

Write the exact commands you would run after changing one of:

- `Scene1Room0Group3PetScript.java`
- `VqsvEventState.java`
- `VqsvBattleRuntime.java`

Must include:

- build
- `--check`
- mojibake scan
- at least one relevant PNG smoke checkpoint

## First Safe Dev Task

Recommended first task for a new dev:

```text
Audit remaining `VqsvIntroDemo.Scene` responsibilities after the completed
refactors, then recommend the next smallest move-only split.
```

Rules:

- This is a move-only refactor.
- Keep map ids, actor rows, camera placement, source transition targets, and
  rendered output identical.
- Build.
- Run `--check`.
- Run mojibake scan.
- Smoke:
  - choose checkpoints relevant to the chosen split
  - for shell/input work, include at least `room1_op13_bunny_trigger`,
    `room0_pet_choice_ui`, and a `--smoke-drive` route

Do not touch battle runtime, rewards, source inventory ops, or
`VqsvFreeWorldRuntime.java` in the same task.
Do not merge source side-effect helpers back into `VqsvSourceOps.java`.

## Current Trust Boundary

Trusted enough to continue building on:

- source-backed manual event order through elder battle
- room0/room1 transition chain
- actor52 interaction
- pet choice UI render
- source event-state wrappers
- branch/result smoke for three battle stubs

Not trusted as final/original-perfect:

- full `game.c` opcode runner
- full `game.d` battle engine
- battle command UI/status/effects/EXP/result flow
- full `game.h` UI runtime
- pixel-perfect `game.j` text renderer
- full opcode 9 `b.a()` effect semantics
- post-group6 side quests and broader world progression

