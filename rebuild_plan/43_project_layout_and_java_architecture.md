# Project Layout And Java Architecture

Date: 2026-07-03

Scope: current rebuild project structure after the split of text, sprite,
world actors, script blocks, event state, script files, and battle runtime.

This document is for a new dev chat. It describes what exists now; it does not
claim the rebuild is a complete original-engine port.

## Top-Level Layout

Path convention:

- `<PROJECT_ROOT>` means the folder that contains `modules`, `rebuild_game`,
  and `rebuild_plan`.
- Do not hardcode a machine-specific path in docs or code.

```text
<PROJECT_ROOT>
  modules\
  rebuild_game\
  rebuild_plan\
```

## `modules`

`modules` is the source-of-truth data area. The rebuild must trace behavior
back here before claiming parity.

Important areas:

```text
modules\event\decoded\
  data__event__scene_0.mid.json
  data__event__scene_1.mid.json
```

Decoded event JSON. Use these to prove scene, room, group, record, opcode,
arguments, and string-pool order.

```text
modules\source_code\decoded\decompiled_source_cfr\
  game\c.java
  game\d.java
  game\g.java
  game\h.java
  game\j.java
  game\k.java
```

Decompiled original logic. Some methods are CFR-damaged, so any statement based
on those methods must be marked `PARTIAL`, `DAMAGED`, or `APPROX` unless
verified another way.

```text
modules\script\decoded\
modules\script\original\
modules\img\decoded\
modules\spr\original\
modules\map\original\
modules\mod\original\
modules\ui\original\
```

Resource sources for sprite tables, images, maps, mods, UI definitions, battle
database, and source-like data tables.

## `rebuild_game`

Java rebuild project. It currently contains two layers:

1. packaged reusable skeleton under `com.vqsv.rebuild.*`
2. current manual playable demo in default-package files such as
   `VqsvIntroDemo.java` and `Scene*Script.java`

Build entry:

```text
rebuild_game\build.ps1
```

Current resources copied into the build:

```text
rebuild_game\src\main\resources\
  font.bin
  img\*.png
  spr_*_all(r)
  tex\*.png
```

## Packaged Skeleton

These files are the more general rebuild framework.

```text
com\vqsv\rebuild\resource\
```

Resource locator, binary readers, asset paths, image loader, and resource smoke
checks.

```text
com\vqsv\rebuild\render\
```

Map renderer, tile sets, sprite metadata, sprite animator, world renderer, and
renderer smoke checks.

```text
com\vqsv\rebuild\cutscene\
```

Early text/cutscene renderer smoke layer.

```text
com\vqsv\rebuild\state\
```

Boot flow, intro state, and legacy intro state.

```text
com\vqsv\rebuild\runtime\
com\vqsv\rebuild\core\
com\vqsv\rebuild\input\
```

Swing panel/runtime shell, app config, screen abstraction, and input snapshots.

Important note: the packaged skeleton is useful infrastructure, but the current
story route being tested by the user mostly lives in `VqsvIntroDemo.java` and
the default-package helper files.

## Current Manual Demo Files

### `VqsvIntroDemo.java`

Status: large coordinator, still a hotspot.

Responsibilities still inside:

- runnable demo and smoke-test entrypoint
- main `Scene` state object
- event list assembly call site
- input/tick/event-runner shell
- thin wrappers around extracted source helper files

Known size after refactor: still large.

Do not treat it as fully cleaned up. New code should avoid making it larger
unless the task specifically requires working inside the central scene object.

### `VqsvText.java`

Central visible text registry.

Rule: add Vietnamese display text here, not scattered inside scripts. This is
to avoid mojibake regressions and to make source text review easier.

### `VqsvTextRenderer.java`

Text drawing and UI text primitives:

- `FontBitmap`
- `TextBox`
- cursor/prompt logic

Current truth: readable and smoke-tested for the current route, but not
pixel-perfect MIDP `s.java` parity.

### `VqsvSpriteRenderer.java`

Sprite table/animation/draw helpers for copied `spr_*_all(r)` and image
assets.

Current truth: enough for current actor/sprite rendering; not a final full
original renderer for every effect and edge case.

### `VqsvWorldActors.java`

World actor and small UI containers:

- `Actor`
- `TempSprite`
- `WorldUi`
- `ChoiceBox`

Current truth: current route actors/dialog/choice are covered by smoke, but
full `game.h` UI runtime is still pending.

### `VqsvSceneActors.java`

Actor bootstrap table extracted from `VqsvIntroDemo.Scene`.

Current truth: move-only extraction of the source-guided actor rows used by the
current manual route. `VqsvIntroDemo.Scene` now calls
`VqsvSceneActors.makeActors()` for initial actor creation and blank-room reset.

This file does not change actor behavior or source fidelity; it only owns the
current actor table construction.

### `VqsvSceneScriptSupport.java`

Shared actor/dialog script support extracted from `VqsvIntroDemo.Scene`.

Current truth: move-only extraction of these implementation bodies:

- `setActive(Scene,int[],int[])`
- `hide(Scene,int[])`
- `dialog(String,String)`
- `dialog(String,String,int)`
- `taskNotice(String)`
- `waitForText()`

Scripts use this file for common actor visibility, dialog creation, task-tip
creation, and text-confirm blocking. It still accepts `VqsvIntroDemo.Scene`
because `Scene` remains the current runtime context.

This file does not change dialog timing, typewriter behavior, prompt behavior,
or source fidelity; it only removes these utility bodies from
`VqsvIntroDemo.Scene`.

### `VqsvScriptBlocks.java`

Reusable blocking/event primitives:

- `Event`
- `Blocking`
- `Delay`
- `Move`
- `ActionSet`
- `TimedAction`
- `Path`
- `CameraPan`
- `Op13FreeWorldTrigger`
- `ActorTransitionFreeWorldTrigger`
- `ActorInteractionFreeWorldTrigger`

This file should contain generic blocks only. Scene-specific story behavior
should move into `Scene*Script.java` files when safe.

### `VqsvEventState.java`

Source-like event-state wrapper:

- `op15CheckEventState`
- `op86CheckEventState`
- `op23MarkEventComplete`
- `op14CompleteEvent`
- source event-state trace

This is the preferred place for event-state helper behavior.

### `VqsvBattleRuntime.java`

Contains `SourceBattleRuntime`.

Current truth: battle setup/stats/result branches are source-backed enough for
the three current story battles, but this is still `PORTED/APPROX`, not full
`game.d`.

Not yet complete:

- turn state machine
- command menu runtime
- skills
- buffs/debuffs
- status
- EXP/result flow
- capture probability and full inventory semantics
- battle animations/effects

### `VqsvBattleTables.java`

Source-backed wrappers for battle database tables loaded from
`modules/script/original/db.mid`.

Current truth: reads all nine `aq.c[0..8]` groups and exposes named row wrappers
for species, skills, status/form rows, items, buffs, and debuffs. This is the
new foundation for replacing the remaining `PORTED/APPROX` battle runtime with
real `game.d/game.b/game.h` logic.

Important source-backed correction: battle relation now uses species
relation/catch class `[22]`, matching the battle audit docs, rather than
accidentally using sprite/visual id.

This file does not by itself complete battle. It only makes the source data
named and testable for the next phases: `BattleUnit`, damage formula, command
UI, catch, EXP, and animation scripts.

### `VqsvBattleUnit.java`

Source-shaped battle unit model based on `game.b`.

Current truth: mirrors the important `game.b` storage shape:

- `baseStats c[23]`;
- `currentStats d[23]`;
- `skillPp y[5]`;
- `skillIds z[5]`;
- `buffSlots v[16][5]`;
- `debuffSlots w[11][5]`;
- `activeEffectQueue x[2][3]`;
- `activeEffectCount N[2]`;
- `effectScratch K[16]`.

Current story battles instantiate this model, then pass through the temporary
`SourceBattleUnit` bridge for the existing renderer/runtime fields.

This file does not yet make battle fully original. The bytecode-equivalent
`game.b.b(target)` damage formula, full buff/debuff tick/apply behavior, item
validation/use, catch, EXP, and save payload still need later slices.

### `VqsvBattleScripts.java`

Small story-battle factory helpers extracted from `VqsvIntroDemo.Scene`.

Current truth: move-only extraction for Bunny and elder battle setup wrappers.
It adds the same source trace lines and returns the same `SourceBattleRuntime`
instances as before the split.

Contains:

- `room1BunnyBattleCaptureRuntime`
- `room0Group6ElderBattleRuntime`

This file does not make battle more complete. Full original `game.d/game.h`
battle behavior remains `PENDING`.

### `VqsvBattleRenderer.java`

Current battle overlay/HUD drawing helpers.

Current truth: extracted from `VqsvIntroDemo.Scene` without intended runtime
behavior changes. It is still `PORTED/APPROX`, because full original
`game.h/game.d` battle UI parity remains pending.

### `VqsvSceneLoaders.java`

Current map/room loader tables and map renderer construction.

Current truth: extracted from `VqsvIntroDemo.Scene` without intended runtime
behavior changes. `Scene` intentionally keeps wrapper methods such as
`loadScene1Room0(...)` so existing script files do not change call shape.

Contains loaders for scene 7 room 2, legacy `loadRoom1`, scene 5 room 3,
scene 1 room 3 entry, and scene 1 rooms 0/1/2.

### `VqsvSourceModels.java`

Small source-like models for items, special rewards, pet state, and battle
units. Keep this as data model code, not story logic.

### `VqsvSourceOps.java`

Current source-backed inventory/reward opcode helpers.

Current truth: extracted from `VqsvIntroDemo.Scene` without intended runtime
behavior changes. `Scene` intentionally keeps wrapper methods for
`op17Item(...)`, `op31CurrencyReward(...)`, and `op19SpecialReward(...)` so
existing script files do not change call shape.

This file owns current source bag helpers, item id mapping, reward popup
construction, currency mutation, and special reward mutation for the current
manual route.

### `VqsvSourceEffects.java`

Current source event side-effect opcode helpers.

Current truth: extracted from `VqsvIntroDemo.Scene` without intended runtime
behavior changes. Keep this separate from `VqsvSourceOps.java`; inventory and
reward mutation belong in `VqsvSourceOps.java`, while event side effects belong
here.

Contains current wrappers/helpers for:

- `op5ActorEffect`
- `op9SourceEffect`
- `op25SetGameFlag`
- `op39RefreshPets`
- `op56ActorVisibility`
- `op67SetBattleActor`

Important: extraction does not mean full original effect parity. Opcode 9
effect rendering remains `APPROX/PENDING` against the original engine.

### `VqsvFreeWorldRuntime.java`

Current free-world movement, transition, and collision/interaction helpers.

Current truth: extracted from `VqsvIntroDemo.Scene` without intended runtime
behavior changes. `Scene` intentionally keeps wrapper methods such as
`tickFreeWorldPlayer()`, `trySourceTransition(...)`,
`playerIntersectsActorSourceMask(...)`, and `playerInteractsActorSourceMask(...)`
so existing scripts and smoke harness code do not change call shape.

Contains current helpers for:

- transition preparation/state and source direction mapping
- type-1 actor transition checks/loads for current scene1 room0/room1/room2
- source-shaped player placement at target transition actors
- free-world player movement from held direction keys
- source rectangle trigger check for `op13`
- actor mask overlap checks for type-1 transitions and `op16`/`op38`
- stop-player behavior when a source event fires

Important: this does not make movement/collision fully original. Full
`game.g.q()` tile/actor collision remains pending; current movement collision
is still `APPROX` and mostly map-boundary plus source mask trigger checks.

### `VqsvSceneView.java`

Current scene camera and render helpers.

Current truth: extracted from `VqsvIntroDemo.Scene` without intended runtime
behavior changes. `Scene` intentionally keeps wrapper methods such as
`render(...)`, `setCameraCenter(...)`, `moveCameraToward(...)`,
`cameraCenteredOn(...)`, `followActor(...)`, and `stopCameraFollow()` so
existing scripts, loaders, free-world runtime, and smoke harness code do not
change call shape.

Contains current helpers for:

- scene render ordering
- map layer rendering
- actor layer rendering and Y-sort for layer 1
- player rendering
- camera center/pan/follow helpers

Important: extraction does not mean full original renderer parity. The current
route rendering remains `PORTED/APPROX`; full original `game.k/game.j/game.h`
rendering is still broader pending work.

### `VqsvSmokeHarness.java`

Smoke-only command-line harness extracted from `VqsvIntroDemo.java`.

It owns smoke image rendering, checkpoint setup, route driving, fast-forward
ticks, actor-placement helpers used by checkpoints, and output logging for
`--smoke`, `--smoke-drive`, and `--smoke-checkpoint`.

Movement/transition smoke checkpoints include `room1_op13_bunny_trigger`,
`return_room0_transition`, `actor52_interaction_group2`,
`post_group6_room2_entry_tip`, and `post_group6_room0_back_from_room2`.

### `VqsvEffect.java`

Current manual visual effects. Source parity for opcode 9 and `b.a()` remains
`APPROX/PENDING`.

## Script Files

`makeEvents()` in `VqsvIntroDemo.Scene` delegates in this order:

```java
Scene0IntroScript.appendTo(e);
Scene1Room3EntryScript.appendTo(e);
Scene1Room0Group0Script.appendTo(e);
Scene1Room1BunnyScript.appendTo(e);
Scene1Room0Group2ElderScript.appendTo(e);
Scene1Room0Group3PetScript.appendTo(e);
Scene1Room0Group6ElderBattleScript.appendTo(e);
```

Current route:

```text
scene0 intro
-> scene1 room3 entry
-> room0 group0 ten-years-later village
-> room1 Bunny path
-> return room0
-> room0 group2 elder interaction
-> room0 group3 pet selection
-> room0 group6 elder battle/reward
-> post-group6 free-world slice
```

Script status:

| File | Status | Notes |
|---|---|---|
| `Scene0IntroScript.java` | PORTED/APPROX | User-approved baseline. Do not edit casually. |
| `Scene1Room3EntryScript.java` | PORTED/APPROX + battle pending | Sophie kidnapping path uses source-backed battle slice, not full `game.d`. |
| `Scene1Room0Group0Script.java` | PORTED/APPROX | Ten-years-later start and Bunny transition. |
| `Scene1Room1BunnyScript.java` | PORTED/APPROX + battle pending | Writes source event states after Bunny. |
| `Scene1Room0Group2ElderScript.java` | PORTED/APPROX | Must be actor52 interaction gated, not auto-run. |
| `Scene1Room0Group3PetScript.java` | PORTED/APPROX | Owns `Room0Group3PetOffer`; full `game.g` pet inventory remains partial. |
| `Scene1Room0Group6ElderBattleScript.java` | PORTED/APPROX + battle pending | Owns `Room0Group6Start` and `Room0PostGroup6FreeWorld`; full `game.d` remains pending. |

## Current Trust Boundary

Safe to build on:

- source-backed event order through elder battle
- room1 Bunny return chain
- actor52 interaction gate
- current pet choice UI
- source event-state wrappers
- branch/result smoke for current battle slice

Not safe to call complete:

- generic `game.c` event VM
- full `game.d` battle engine
- full `game.h` UI runtime
- pixel-perfect `game.j/s.java` text renderer
- full opcode 9 effect semantics
- post-group6 broader world progression

## Required Commands

Set path variables first:

```powershell
$env:PROJECT_ROOT = "<path-to-project-root>"
$env:MODULES_ROOT = Join-Path $env:PROJECT_ROOT "modules"
$env:REBUILD_GAME = Join-Path $env:PROJECT_ROOT "rebuild_game"
```

Build:

```powershell
cd $env:REBUILD_GAME
powershell -ExecutionPolicy Bypass -File .\build.ps1
```

Check:

```powershell
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" com.vqsv.rebuild.Main --check
```

Mojibake scan:

```powershell
$pattern = @'
Ãƒ|Ã‚|Ã„|Ã†|Ã¡Âº|Ã¡Â»|Ã…|â‚¬|Å“|â„¢|Å¡|Å¸
'@.Trim()
rg -n $pattern "$env:REBUILD_GAME\src\main\java" -g "*.java" -g "!_backup*"
```

PNG smoke only:

```powershell
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint room0_pet_choice_ui "$env:REBUILD_GAME\build_intro_demo\dev_pet_choice.png"
```

```powershell
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state "$env:REBUILD_GAME\build_intro_demo\dev_elder_reward.png"
```


