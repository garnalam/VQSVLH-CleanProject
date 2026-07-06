# Battle Scripts Refactor Audit

Date: 2026-07-06

Scope: move the two story battle factory wrappers out of
`VqsvIntroDemo.Scene` into `VqsvBattleScripts.java`.

This is a move-only/refactor slice. It does not change `SourceBattleRuntime`,
encounter records, branch targets, battle result logic, reward logic, UI
rendering, or source event order.

## Source / Rebuild Facts

Source-backed battle records remain the same as documented in
`41_battle_engine_three_stub_replacement_audit.md`:

| Story point | Source records | Current rebuild behavior |
|---|---|---|
| Bunny capture | room1 group0 `op37 [34,5,1]`, `op52 [0,1]`, `op32 [0,0]`, `op47 [12,0,0]` | `SourceBattleRuntime`, result `-1`, manual success path continues. |
| Elder battle | room0 group6 `op67 [52]`, `op37 [68,5,1]`, `op32 [0,2]`, `op47 [10,10,0]` | `SourceBattleRuntime`, result `0 -> branch 10`. |

This refactor only moved the small factory code that adds source trace lines
and constructs the matching `SourceBattleRuntime` instance.

## Files Changed

Added:

- `rebuild_game/src/main/java/VqsvBattleScripts.java`

Updated:

- `rebuild_game/src/main/java/VqsvIntroDemo.java`
- `rebuild_game/src/main/java/Scene1Room1BunnyScript.java`
- `rebuild_game/src/main/java/Scene1Room0Group6ElderBattleScript.java`

## Implementation Truth

Before:

```text
Scene1Room1BunnyScript -> VqsvIntroDemo.Scene.room1BunnyBattleCaptureRuntime()
Scene1Room0Group6ElderBattleScript -> VqsvIntroDemo.Scene.room0Group6ElderBattleRuntime()
```

After:

```text
Scene1Room1BunnyScript -> VqsvBattleScripts.room1BunnyBattleCaptureRuntime(scene)
Scene1Room0Group6ElderBattleScript -> VqsvBattleScripts.room0Group6ElderBattleRuntime(scene)
```

`VqsvIntroDemo.Scene` no longer owns those two factory methods.

Status:

| Area | Status | Note |
|---|---|---|
| Battle factory extraction | PORTED | Move-only into `VqsvBattleScripts.java`. |
| Bunny branch/result | PORTED/APPROX | Smoke still proves result `-1`, state writes happen after battle. |
| Elder branch/result | PORTED/APPROX | Smoke still proves result `0 -> branch 10`. |
| Full `game.d` battle engine | PENDING | Not part of this refactor. |
| Full `game.h` battle command UI | PENDING | Not part of this refactor. |

## Verification

Run setup:

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

Result: PASS.

Check:

```powershell
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" com.vqsv.rebuild.Main --check
```

Result: PASS.

Mojibake scan:

```powershell
rg -n "Ãƒ|Ã‚|Ã„|Ã†|Ã¡Âº|Ã¡Â»|Ã…|â‚¬|Å“|â„¢|Å¡|Å¸" "$env:REBUILD_GAME\src\main\java" -g "*.java"
```

Result: no matches.

Factory ownership scan:

```powershell
rg -n "room1BunnyBattleCaptureRuntime|room0Group6ElderBattleRuntime|VqsvBattleScripts" "$env:REBUILD_GAME\src\main\java" -g "*.java" -g "!_backup*"
```

Result:

- the two method definitions exist only in `VqsvBattleScripts.java`;
- `Scene1Room1BunnyScript.java` calls `VqsvBattleScripts::room1BunnyBattleCaptureRuntime`;
- `Scene1Room0Group6ElderBattleScript.java` calls `VqsvBattleScripts::room0Group6ElderBattleRuntime`.

Smoke PNGs generated:

- `rebuild_game/build_intro_demo/battle_scripts_bunny_capture.png`
- `rebuild_game/build_intro_demo/battle_scripts_elder.png`
- `rebuild_game/build_intro_demo/battle_scripts_route_bunny_after_battle_task.png`
- `rebuild_game/build_intro_demo/battle_scripts_route_elder_after_battle_reward_state.png`
- `rebuild_game/build_intro_demo/battle_scripts_room0_pet_choice_ui.png`

Observed smoke results:

- `battle_bunny_capture`: PASS, battle overlay reachable.
- `battle_elder`: PASS, battle overlay reachable with one source pet.
- `route_bunny_after_battle_task`: PASS, `battleResult=-1`,
  `battleBranch=-1`, `state101=3`, `state110=3`.
- `route_elder_after_battle_reward_state`: PASS, `battleResult=0`,
  `battleBranch=10`, `state106=3`, `money=500`, `pets=1`.
- `room0_pet_choice_ui`: PASS, pet choice still renders after nearby script
  refactors.

Console note: battle log text printed in PowerShell can display mojibake due to
console encoding. The Java source scan had no mojibake matches.

## Current Next Slice Options

Recommended next low-risk audit:

1. Re-audit remaining `VqsvIntroDemo.Scene` responsibilities after
   `VqsvSceneScriptSupport`, `VqsvSceneView`, and `VqsvBattleScripts`.
2. Choose whether the next slice should be:
   - input/tick/event-runner shell audit only, no move yet; or
   - a small state/data grouping move if a safer boundary appears.

Still not recommended as a casual move:

- splitting `press0`, `click`, `setMoveKey`, or `tick`, because those are core
  frame/input/event-runner paths and can regress every route if moved wrongly.
