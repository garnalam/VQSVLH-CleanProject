# New Dev Chat Training And Exercises

Date: 2026-07-03

Use this document to onboard a new dev chat before it touches code.

## Copy-Paste Prompt For The New Dev Chat

```text
You are taking over the VQSV/Liet Hoa rebuild project.

Prime directive:
- Follow the original game from source logic, event JSON, and assets.
- Do not invent sprite/image/effect/UI/timing/event-chain behavior.
- Every meaningful claim must point to one of:
  modules/event/decoded/*.json
  modules/source_code/decoded/decompiled_source_cfr/**/*.java
  modules/script/decoded or original data
  modules/img, modules/spr, modules/map, modules/mod, modules/ui
  rebuild_plan/*.md audit docs
- Mark every unfinished area honestly as PORTED, APPROX, STUB, PENDING,
  UNKNOWN, or DAMAGED.
- Do not touch user-approved intro/scene0/ten-years route behavior unless the
  current task requires it.
- Prefer PNG-only smoke tests. Do not open the interactive client unless the
  user explicitly asks.
- Do not run java -jar for VqsvIntroDemo smoke. Use classpath execution.
- Do not hardcode local machine paths. Use PROJECT_ROOT/MODULES_ROOT/
  REBUILD_GAME placeholders or environment variables.
- After code edits: build, --check, mojibake scan, and relevant PNG smoke.

Before coding, read:
1. rebuild_plan/42_new_dev_chat_handoff_after_refactor.md
2. rebuild_plan/43_project_layout_and_java_architecture.md
3. rebuild_plan/44_new_dev_chat_training_and_exercises.md
4. rebuild_plan/45_rebuild_plan_learning_index.md
5. rebuild_plan/40_intro_to_elder_battle_closeout_audit.md
6. rebuild_plan/41_battle_engine_three_stub_replacement_audit.md
7. rebuild_plan/35_scene1_return_to_room0_group2_elder_audit.md
8. rebuild_plan/36_scene1_room0_group3_pet_selection_audit.md
9. rebuild_plan/37_scene1_room0_group6_elder_battle_audit.md
10. modules/event/decoded/data__event__scene_1.mid.json
11. modules/source_code/decoded/decompiled_source_cfr/game/c.java
12. rebuild_game/src/main/java/VqsvIntroDemo.java
13. all Scene*Script.java files

After reading, do not code yet. Answer the compulsory entry exercises in
rebuild_plan/44_new_dev_chat_training_and_exercises.md.
```

## Compulsory Entry Exercise

The new dev chat must answer these in chat before implementing anything.

### Exercise 1: Architecture Proof

Explain the responsibility and current risk level of these files:

- `VqsvIntroDemo.java`
- `VqsvText.java`
- `VqsvTextRenderer.java`
- `VqsvSpriteRenderer.java`
- `VqsvWorldActors.java`
- `VqsvScriptBlocks.java`
- `VqsvEventState.java`
- `VqsvBattleRuntime.java`
- `Scene1Room0Group2ElderScript.java`
- `Scene1Room0Group3PetScript.java`
- `Scene1Room0Group6ElderBattleScript.java`

Minimum expected points:

- `VqsvIntroDemo.Scene` is still a large coordinator/hotspot.
- `Scene*Script.appendTo(e)` owns current manual route order.
- `VqsvText.java` is the visible text registry and protects against mojibake.
- `VqsvBattleRuntime.java` is still `PORTED/APPROX`, not full `game.d`.
- `VqsvScriptBlocks.java` should stay generic, not scene-specific.

### Exercise 2: Source Chain Proof

Prove the post-Bunny return-to-elder chain:

- room1 group0 writes `op23 [1,0,1]`
- room1 group0 writes `op14 [1,1,0]`
- room0 group2 opens with `op86 [1,1,0]`
- room0 group2 record 1 is `op16 [52]`
- actor52 must be interacted with; group2 must not auto-run

Required sources:

- `modules/event/decoded/data__event__scene_1.mid.json`
- `rebuild_game/src/main/java/Scene1Room1BunnyScript.java`
- `rebuild_game/src/main/java/Scene1Room0Group2ElderScript.java`
- `rebuild_game/src/main/java/VqsvEventState.java`
- `rebuild_plan/35_scene1_return_to_room0_group2_elder_audit.md`

Expected conclusion:

```text
This chain is source-backed as an interaction-gated return path, not a timed
auto-continue path.
```

### Exercise 3: Status Classification

Classify each item:

| Item | Expected status |
|---|---|
| Scene0 intro timing/logic | PORTED/APPROX, user-approved baseline |
| actor52 interaction trigger | PORTED/APPROX |
| Pet choice UI | PORTED/APPROX |
| Bunny battle | PORTED/APPROX battle slice, not full game.d |
| Elder battle | PORTED/APPROX battle slice, not full game.d |
| Generic `game.c` event runner | PENDING |
| Full `game.h` UI runtime | PENDING |
| Full opcode 9 effects | APPROX/PENDING |
| MIDP pixel-perfect font | APPROX/PENDING |
| Post-group6 broader world progression | PENDING |

The answer is wrong if it claims Bunny/elder battle are fully done.

### Exercise 4: Smoke Plan

Write exact commands for a code change touching:

- `Scene1Room0Group3PetScript.java`, or
- `VqsvBattleRuntime.java`, or
- `VqsvEventState.java`

Minimum required commands:

```powershell
$env:PROJECT_ROOT = "<path-to-project-root>"
$env:MODULES_ROOT = Join-Path $env:PROJECT_ROOT "modules"
$env:REBUILD_GAME = Join-Path $env:PROJECT_ROOT "rebuild_game"
cd $env:REBUILD_GAME
powershell -ExecutionPolicy Bypass -File .\build.ps1
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" com.vqsv.rebuild.Main --check
$pattern = @'
Ãƒ|Ã‚|Ã„|Ã†|Ã¡Âº|Ã¡Â»|Ã…|â‚¬|Å“|â„¢|Å¡|Å¸
'@.Trim()
rg -n $pattern "$env:REBUILD_GAME\src\main\java" -g "*.java" -g "!_backup*"
```

Relevant PNG smoke examples:

```powershell
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint room0_pet_choice_ui "$env:REBUILD_GAME\build_intro_demo\dev_pet_choice.png"
```

```powershell
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint actor52_interaction_group2 "$env:REBUILD_GAME\build_intro_demo\dev_actor52_group2.png"
```

```powershell
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state "$env:REBUILD_GAME\build_intro_demo\dev_elder_reward.png"
```

For battle changes, also include:

```powershell
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint battle_bunny_capture "$env:REBUILD_GAME\build_intro_demo\dev_bunny_battle.png"
```

### Exercise 5: No-Guess Audit

Pick one current class or behavior and write:

- source evidence used
- what is implemented
- what is approximate
- what smoke proves
- what smoke does not prove

Good candidate topics:

- `Room0Group3PetOffer`
- `SourceBattleRuntime`
- `ActorInteractionFreeWorldTrigger`
- `ChoiceBox`

## Answer Quality Rubric

A new dev answer is acceptable only if it:

- cites concrete files and opcodes/states
- separates source facts from rebuild implementation
- uses `PORTED/APPROX/STUB/PENDING/UNKNOWN`
- refuses to call battle or UI 100% done
- includes PNG smoke commands, not only interactive test instructions

Reject or correct the answer if it:

- says "looks right" without source evidence
- invents a sprite/image/effect mapping
- treats fixed tick counts as source proof
- opens the game client when asked for smoke only
- edits user-approved intro/scene0 while working on unrelated tasks

## Completed Small Tasks

- DONE: proved `OldRoom0Group3PetOffer` was unused and removed only that old
  class.
- DONE: moved live `Room0Group3PetOffer` into
  `Scene1Room0Group3PetScript.java` without changing behavior.
- DONE: moved `Room0Group6Start` and `Room0PostGroup6FreeWorld` into
  `Scene1Room0Group6ElderBattleScript.java` without changing behavior.
- DONE: extracted battle UI drawing into `VqsvBattleRenderer.java` without
  changing intended runtime behavior.
- DONE: extracted map/room loaders into `VqsvSceneLoaders.java` without
  changing intended runtime behavior.
- DONE: extracted inventory/reward/source gameplay ops into
  `VqsvSourceOps.java` without changing intended runtime behavior.
- DONE: extracted smoke harness into `VqsvSmokeHarness.java` without changing
  intended smoke behavior.
- DONE: extracted source event side-effect helpers into
  `VqsvSourceEffects.java` without changing intended runtime behavior.
  Keep it separate from `VqsvSourceOps.java`.
- DONE: extracted free-world movement/transition/collision helpers into
  `VqsvFreeWorldRuntime.java` without changing intended runtime behavior.
  Full `game.g.q()` movement/collision remains pending.
- DONE: extracted scene camera/render helpers into `VqsvSceneView.java`
  without changing intended runtime behavior.

## First Small Task For A New Dev

Recommended starter task:

```text
Audit remaining `VqsvIntroDemo.Scene` responsibilities and recommend the next
smallest move-only split.
```

Required procedure:

1. Run `rg -n "press0|click|setMoveKey|void tick|makeEvents|setActive|hide|dialog\\(|taskNotice|waitForText|room1BunnyBattleCaptureRuntime|room0Group6ElderBattleRuntime" rebuild_game/src/main/java/VqsvIntroDemo.java`.
2. Show the references in chat.
3. List remaining responsibilities in `VqsvIntroDemo.Scene`.
4. Recommend one smallest move-only split, but do not code until the user
   chooses it.
5. If the user chooses a split, keep behavior identical, build, run `--check`,
   run mojibake scan, and smoke relevant checkpoints.

Do not touch battle runtime, route scripts, scene/map loader tables,
inventory/reward/source ops, source side-effect helpers, free-world runtime
helpers, or scene view helpers in the same task.

## Larger Future Work

Do not start these until the user explicitly chooses the slice:

- full battle engine from `game.d/game.b/game.g`
- original `game.h` UI runtime
- generic opcode runner for `game.c`
- full opcode 9 effect system
- post-group6 side quest/world progression
- pixel-perfect text renderer audit against `game.j/s.java`

