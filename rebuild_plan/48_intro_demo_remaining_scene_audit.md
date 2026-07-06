# Intro Demo Remaining Scene Audit

Date: 2026-07-06

Scope: audit what still lives inside `VqsvIntroDemo.Scene` after the recent
refactors:

- `VqsvSceneView`
- `VqsvSceneLoaders`
- `VqsvFreeWorldRuntime`
- `VqsvSourceOps`
- `VqsvSourceEffects`
- `VqsvSceneScriptSupport`
- `VqsvBattleScripts`

This document is audit-only. No Java code was changed for this audit.

## Current Truth

`VqsvIntroDemo.java` is still the demo/client shell and contains the inner
`Scene` runtime context. The heavy story scripts and many subsystem helpers
have already moved out, but `Scene` still owns the central mutable state and
the frame/event runner.

Current size observed:

| File | Lines | Note |
|---|---:|---|
| `VqsvIntroDemo.java` | 447 | still the central coordinator/context file |
| `VqsvTextRenderer.java` | 579 | text/font renderer |
| `VqsvSpriteRenderer.java` | 558 | sprite renderer |
| `VqsvSceneLoaders.java` | 473 | map/room loader tables |
| `VqsvScriptBlocks.java` | 460 | blocking/event primitives |

Important: line count alone is not the decision criterion. `Scene` is large
because it is the shared context object. Some parts should remain there until a
cleaner runtime architecture exists.

## Remaining `Scene` Responsibilities

### State Container

Observed in `VqsvIntroDemo.java` around the start of the inner class:

- `font`, `effect`
- `actors`, `player`, `tempSprites`, `worldUi`
- `mapRenderer`, `text`, `choice`
- `eventIndex`, `current`, current scene/room ids
- camera and player coordinates
- input booleans
- battle display/runtime state fields
- source-like money/items/rewards/pets/event-state trace

Classification: `KEEP_CORE` for now.

Reason: these fields are the shared runtime context used by scripts, render,
free-world, source ops, smoke harness, and battle runtime. Moving them before
extracting `Scene` as a top-level runtime object would likely create noisy
coupling without reducing risk.

### Input / Frame Runner

Observed methods:

- `press0()`
- `click(int,int)`
- `setMoveKey(int,boolean)`
- `tick()`

Classification: `DO_NOT_TOUCH_YET`.

Reason: this is the core frame/event-runner path. It controls:

- typewriter confirm and choice confirm;
- key reset timing through `key0 = false`;
- current blocking event lifecycle;
- auto-advancing the manual event list;
- actor/player ticking;
- camera follow update.

Any mistake here can regress intro, dialog, free-world, interaction triggers,
pet choice, and battle route checkpoints. Do not move this casually.

### Render / Camera Wrapper Methods

Observed methods:

- `render(Graphics2D)`
- `setCameraCenter`
- `moveCameraToward`
- `cameraCenteredOn`
- `followActor`
- `stopCameraFollow`
- `updateCameraFollow`

Classification: `KEEP_CORE` as wrappers, implementation already moved.

Reason: actual implementation is in `VqsvSceneView`. Keeping thin wrappers in
`Scene` preserves existing script call shape. Removing wrappers is possible
later, but it would touch many scripts and smoke harness call sites for small
benefit.

### Actor Bootstrap Table

Observed method:

- `makeActors()`

Classification: `MOVE_SAFE`.

Recommended target:

- `VqsvSceneActors.java`

Proposed shape:

```java
final class VqsvSceneActors {
    static Actor[] makeActors() { ... }
}
```

Then `Scene` can initialize actors through:

```java
final Actor[] actors = VqsvSceneActors.makeActors();
```

Also update `reloadBlankRoom(...)` to call `VqsvSceneActors.makeActors()`.

Risk: low-to-medium.

Why not zero risk: actor rows affect scene0, scene1 room3, room0, room1,
pet selection, and smoke setup. This is still a move-only change, but it must
be smoke-tested because actor ids and initial visibility are critical.

Required smoke if moved:

- `--smoke ... 900`
- `--smoke ... 5400`
- `--smoke-checkpoint room0_group2_first_dialog`
- `--smoke-checkpoint room0_pet_choice_ui`
- `--smoke-checkpoint route_bunny_after_battle_task`
- `--smoke-checkpoint route_elder_after_battle_reward_state`

Implementation update:

- DONE: `makeActors()` moved to `VqsvSceneActors.java`.
- DONE: initial actor creation now calls `VqsvSceneActors.makeActors()`.
- DONE: blank-room reset now calls `VqsvSceneActors.makeActors()`.
- Verification PASS:
  - build
  - `--check`
  - mojibake scan
  - `scene_actors_intro_t900.png`
  - `scene_actors_10years_t5400.png`
  - `scene_actors_group2_dialog.png`
  - `scene_actors_pet_choice.png`
  - `scene_actors_bunny_route.png`
  - `scene_actors_elder_route.png`

### Event List Assembly

Observed method:

- `makeEvents()`

Current body delegates to:

- `Scene0IntroScript.appendTo(e)`
- `Scene1Room3EntryScript.appendTo(e)`
- `Scene1Room0Group0Script.appendTo(e)`
- `Scene1Room1BunnyScript.appendTo(e)`
- `Scene1Room0Group2ElderScript.appendTo(e)`
- `Scene1Room0Group3PetScript.appendTo(e)`
- `Scene1Room0Group6ElderBattleScript.appendTo(e)`

Classification: `KEEP_CORE`.

Reason: this is now small and clear. It is useful to keep the route order in
one visible place while the rebuild remains a manual route, not a generic
`game.c` event runner.

### Transition / Free-World Wrapper Methods

Observed methods:

- `prepareTransition(...)`
- `markWorldTransition(...)`
- `sourceTransitionRequiredDirection(...)`
- `trySourceTransition(...)`
- `setPlayerPositionApprox(...)`
- `placePlayerAtTransitionActorApprox(...)`
- `tickFreeWorldPlayer()`
- `playerIntersectsSourceRect(...)`
- `playerIntersectsActorSourceMask(...)`
- `playerInteractsActorSourceMask(...)`
- `stopPlayerForSourceEvent()`

Classification: `KEEP_CORE` as wrappers, implementation already moved.

Reason: actual implementation is in `VqsvFreeWorldRuntime`. Wrapper methods
preserve source-shaped script call sites. Removing them would force many script
and smoke changes while not improving source fidelity.

### Room Loader Wrapper Methods

Observed methods:

- `loadScene7Room2`
- `loadRoom1`
- `loadScene5Room3`
- `loadScene1Room3Entry`
- `loadScene1Room0`
- `loadScene1Room1`
- `loadScene1Room2`
- private `reloadBlankRoom(...)`
- `reloadBlankRoomCenteredOnActor(...)`

Classification:

| Method group | Classification | Reason |
|---|---|---|
| public-ish loader wrappers | `KEEP_CORE` | implementation already in `VqsvSceneLoaders`; wrappers keep scripts stable |
| `reloadBlankRoom(...)` | `MOVE_LATER` | can move only if scene loader/reset ownership is redesigned |
| `reloadBlankRoomCenteredOnActor(...)` | `MOVE_LATER` | tightly tied to reset/camera/actor bootstrap |

Recommended: do not touch loaders in the next cleanup slice. They were already
recently extracted and are easy to regress visually.

### Source Ops / Effects / Event State Wrappers

Observed methods:

- `op5ActorEffect`
- `op17Item`
- `op39RefreshPets`
- `op25SetGameFlag`
- `op9SourceEffect`
- `op67SetBattleActor`
- `op31CurrencyReward`
- `op19SpecialReward`
- `op56ActorVisibility`
- `sourceEventState`
- `sourceEventStateComplete`
- `op15CheckEventState`
- `op86CheckEventState`
- `op23MarkEventComplete`
- `op14CompleteEvent`

Classification: `KEEP_CORE` as wrappers.

Reason: implementations already live in:

- `VqsvSourceEffects`
- `VqsvSourceOps`
- `VqsvEventState`

The wrappers are still useful because scripts read like source opcodes. Do not
inline these calls into scripts unless a broader event-runner design is chosen.

### Script Support Implementation

Observed methods:

- `setActive(Scene,int[],int[])`
- `hide(Scene,int[])`
- `dialog(String,String)`
- `dialog(String,String,int)`
- `taskNotice(String)`
- `waitForText()`

Classification: `MOVED`.

Current target:

- `VqsvSceneScriptSupport.java`

Current truth:

`VqsvSceneScriptSupport.java` now owns these implementation bodies directly.
The matching static helper methods were removed from `VqsvIntroDemo.Scene`.
Scripts, smoke checkpoints, and source-op helper code call the shared support
class directly.

This remains a move-only cleanup. It did not alter text rendering, typewriter
behavior, prompt logic, input/tick flow, battle behavior, source data, or
assets.

Risk: low.

Verification after move:

- `--smoke ... 900`
- `--smoke-checkpoint room0_group2_first_dialog`
- `--smoke-checkpoint room0_pet_choice_ui`
- `--smoke-checkpoint route_bunny_after_battle_task`
- `--smoke-checkpoint route_elder_after_battle_reward_state`

Observed result: PASS in the `script_support_impl_*` smoke set documented in
`46_scene_script_support_refactor_audit.md`.

### Actor Effect Convenience

Observed method:

- `spawnActorEffect(int actorId, int animation)`

Classification: `MOVE_LATER`.

Reason: this creates `TempSprite` instances and touches `actors` and
`tempSprites` directly. It could eventually belong in a small effect helper,
but it is intertwined with the current manual effect model. Do not move before
the safer script-support and actor-bootstrap slices.

### Dead / Suspicious Helper

Observed method:

- `sourceStateApprox(String ignoredSourceNote)`

Classification: `REMOVED_AFTER_SCAN`.

Dedicated cleanup result:

- scan found no call sites;
- only the method definition remained in `VqsvIntroDemo.java`;
- method was removed as a standalone dead-code cleanup slice.

```powershell
rg -n "sourceStateApprox" "$env:REBUILD_GAME\src\main\java" -g "*.java" -g "!_backup*"
```

Expected result after cleanup: no Java matches.

## Grouped Decision Matrix

| Area | Classification | Recommended next action |
|---|---|---|
| central state fields | `KEEP_CORE` | leave until `Scene` becomes top-level |
| `press0/click/setMoveKey/tick` | `DO_NOT_TOUCH_YET` | audit only, no move |
| render/camera wrappers | `KEEP_CORE` | leave wrappers |
| `makeActors()` | `MOVE_SAFE` | next good code slice |
| `makeEvents()` | `KEEP_CORE` | leave as route order map |
| free-world wrappers | `KEEP_CORE` | leave wrappers |
| loader wrappers | `KEEP_CORE` | leave wrappers |
| `reloadBlankRoom*` | `MOVE_LATER` | revisit with loader/reset design |
| source op/effect/event-state wrappers | `KEEP_CORE` | leave wrappers |
| script support static implementation | `MOVED` | implementation now lives in `VqsvSceneScriptSupport.java` |
| `spawnActorEffect` | `MOVE_LATER` | revisit after effect model audit |
| `sourceStateApprox` | `REMOVED_AFTER_SCAN` | no call sites; removed in dedicated cleanup |

## Recommended Cleanup Order

1. `VqsvSceneActors.java`
   - DONE: move `makeActors()`;
   - DONE: update actor reset path;
   - smoke broad route.

2. `VqsvSceneScriptSupport.java` implementation move
   - DONE: moved `setActive/hide/dialog/taskNotice/waitForText`
     implementation out of `Scene`;
   - DONE: removed static helper methods from `Scene`;
   - DONE: smoked intro/dialog/choice/battle routes.

3. Dedicated dead-code cleanup
   - DONE: scanned `sourceStateApprox`;
   - DONE: no call sites remained;
   - DONE: removed the unused helper;
   - DONE: build/check/smoke one route.

4. Audit-only `tick/input` runner
   - DONE: write `49_scene_tick_input_runner_audit.md`;
   - do not move `tick` yet.

5. Only later: `VqsvIntroPanel` / top-level `VqsvScene`
   - these are larger architecture moves and should wait until source route
     work has a stable reason for them.

## Smoke Policy For Any Future `Scene` Cleanup

Minimum checks after code changes touching `VqsvIntroDemo.Scene`:

```powershell
$env:PROJECT_ROOT = "<path-to-project-root>"
$env:MODULES_ROOT = Join-Path $env:PROJECT_ROOT "modules"
$env:REBUILD_GAME = Join-Path $env:PROJECT_ROOT "rebuild_game"
cd $env:REBUILD_GAME
powershell -ExecutionPolicy Bypass -File .\build.ps1
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" com.vqsv.rebuild.Main --check
# Run the standard project mojibake scan from `43_project_layout_and_java_architecture.md`.
# Keep the regex pattern there as the single source to avoid encoding drift in copied docs.
```

Minimum PNG smoke set:

```powershell
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke "$env:REBUILD_GAME\build_intro_demo\scene_cleanup_intro_t900.png" 900
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke "$env:REBUILD_GAME\build_intro_demo\scene_cleanup_10years_t5400.png" 5400
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint room0_group2_first_dialog "$env:REBUILD_GAME\build_intro_demo\scene_cleanup_group2_dialog.png"
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint room0_pet_choice_ui "$env:REBUILD_GAME\build_intro_demo\scene_cleanup_pet_choice.png"
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint route_bunny_after_battle_task "$env:REBUILD_GAME\build_intro_demo\scene_cleanup_bunny_route.png"
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state "$env:REBUILD_GAME\build_intro_demo\scene_cleanup_elder_route.png"
```

## Final Recommendation

Do not split input/tick yet.

Best next code slice:

```text
Pause refactor and continue source route work, or do a tiny dedicated
dead-code cleanup after scanning `sourceStateApprox`.
```

Do not choose this as a next slice anymore because it is already complete:

```text
Move `setActive/hide/dialog/taskNotice/waitForText` implementation into
`VqsvSceneScriptSupport.java`.
```

Both are source-cleaning tasks. Neither should alter gameplay behavior or claim
new original-game parity.
