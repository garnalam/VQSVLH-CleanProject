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
- map/world render glue
- source inventory/reward slices
- some room loaders and actor setup
- current battle HUD drawing glue
- pet-selection and post-group6 blocking classes that still need extraction

Known size after refactor: about 2368 lines.

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

Contains `SourceBattleRuntime`, `SourceBattleDb`, and `SourceBattleUnit`.

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

### `VqsvSourceModels.java`

Small source-like models for items, special rewards, pet state, and battle
units. Keep this as data model code, not story logic.

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
| `Scene1Room0Group3PetScript.java` | PORTED/APPROX | Still delegates to `Room0Group3PetOffer` inside `VqsvIntroDemo`. |
| `Scene1Room0Group6ElderBattleScript.java` | PORTED/APPROX + battle pending | Still uses `Room0Group6Start` and post-group6 block inside `VqsvIntroDemo`. |

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
rg -n "Ãƒ|Ã‚|Ã„|Ã†|Ã¡Âº|Ã¡Â»|Ã…|â‚¬|Å“|â„¢|Å¡|Å¸" "$env:REBUILD_GAME\src\main\java" -g "*.java"
```

PNG smoke only:

```powershell
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint room0_pet_choice_ui "$env:REBUILD_GAME\build_intro_demo\dev_pet_choice.png"
```

```powershell
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state "$env:REBUILD_GAME\build_intro_demo\dev_elder_reward.png"
```


