# Scene Script Support Refactor Audit

Date: 2026-07-06

Scope: move actor/dialog script utility implementations out of
`VqsvIntroDemo.Scene` and route all scene scripts through
`VqsvSceneScriptSupport.java`.

This is a move-only/refactor slice. It does not change source event order,
runtime behavior, battle behavior, input/tick handling, or scene data.

## Why This Slice

The project is still reducing the size and responsibility of
`VqsvIntroDemo.java` and the surrounding script files. The selected low-risk
slice was:

- `setActive`
- `hide`
- `dialog`
- `taskNotice`
- `waitForText`

These helpers are used by many manual scene scripts but are not themselves
gameplay rules. Moving their bodies gives scripts a shared support surface
without touching the frame runner or source-backed event chain.

## Files Changed

Shared helper:

- `rebuild_game/src/main/java/VqsvSceneScriptSupport.java`

Updated call sites:

- `rebuild_game/src/main/java/Scene0IntroScript.java`
- `rebuild_game/src/main/java/Scene1Room3EntryScript.java`
- `rebuild_game/src/main/java/Scene1Room0Group0Script.java`
- `rebuild_game/src/main/java/Scene1Room0Group2ElderScript.java`
- `rebuild_game/src/main/java/Scene1Room0Group3PetScript.java`
- `rebuild_game/src/main/java/Scene1Room1BunnyScript.java`
- `rebuild_game/src/main/java/Scene1Room0Group6ElderBattleScript.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`
- `rebuild_game/src/main/java/VqsvSourceOps.java`

Not changed:

- `press0`, `click`, `setMoveKey`, `tick`
- battle runtime behavior
- event-state behavior
- source data or assets

## Implementation Truth

`VqsvSceneScriptSupport` now owns the implementation bodies for:

- `setActive(Scene,int[],int[])`
- `hide(Scene,int[])`
- `dialog(String,String)`
- `dialog(String,String,int)`
- `taskNotice(String)`
- `waitForText()`

The matching static helper bodies were removed from `VqsvIntroDemo.Scene`.
Scripts, smoke setup, and source-op helper code now call
`VqsvSceneScriptSupport` directly when they need these actor/dialog utilities.

The methods still accept `VqsvIntroDemo.Scene` because `Scene` remains the
central runtime context. This slice did not create a new top-level scene
runtime object.

Status:

| Area | Status | Note |
|---|---|---|
| Shared script helper class | PORTED | Owns implementation bodies now. |
| Per-script wrapper cleanup | PORTED | Scene scripts call the shared support class. |
| Runtime implementation move out of `VqsvIntroDemo.Scene` | PORTED | `setActive/hide/dialog/taskNotice/waitForText` bodies moved. |
| Input/tick/event runner split | PENDING | Intentionally not touched. |
| Battle factory split | PORTED | Already handled in `VqsvBattleScripts.java`, separate slice. |

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
rg -n "Ã|Â|Ä|Æ|áº|á»|Å|€|œ|™|š|Ÿ" "$env:REBUILD_GAME\src\main\java" -g "*.java"
```

Result: no matches.

Wrapper cleanup scan:

```powershell
rg -n "private static (Event|Blocking|void) (dialog|waitForText|setActive|hide|taskNotice)|VqsvIntroDemo\.Scene\.(dialog|waitForText|setActive|hide|taskNotice)" "$env:REBUILD_GAME\src\main\java" -g "Scene*.java"
```

Result: no old per-script wrappers remain.

Implementation-location scan:

```powershell
rg -n "static .*\\b(setActive|hide|dialog|taskNotice|waitForText)\\b|VqsvIntroDemo\\.Scene\\.(setActive|hide|dialog|taskNotice|waitForText)" "$env:REBUILD_GAME\src\main\java" -g "*.java" -g "!_backup*"
```

Expected result after this audit update:

- stale doc language should not claim support is only a delegation layer back
  to `Scene`;
- implementation symbols should be in `VqsvSceneScriptSupport.java`;
- `TextBox.dialog(...)` in the renderer is unrelated and expected.

Smoke PNGs generated:

- `rebuild_game/build_intro_demo/script_support_impl_intro_t900.png`
- `rebuild_game/build_intro_demo/script_support_impl_group2_dialog.png`
- `rebuild_game/build_intro_demo/script_support_impl_pet_choice.png`
- `rebuild_game/build_intro_demo/script_support_impl_bunny_route.png`
- `rebuild_game/build_intro_demo/script_support_impl_elder_route.png`

Observed smoke results:

- `room0_group2_first_dialog`: PASS, text present.
- `room0_pet_choice_ui`: PASS, option UI visible.
- `route_bunny_after_battle_task`: PASS, `state101=3`, `state110=3`.
- `route_elder_after_battle_reward_state`: PASS, `state106=3`,
  `money=500`, `pets=1`.
- `--smoke 900`: PASS, covers early scene0 helper use.

Console note: battle log text in terminal can still display mojibake because of
Windows console encoding. The source scan had no mojibake matches.

## Current Next Slice Options

Already completed before/alongside this source state:

- `OldRoom0Group3PetOffer` is gone.
- live `Room0Group3PetOffer` lives in
  `Scene1Room0Group3PetScript.java`.
- `Room0Group6Start` and `Room0PostGroup6FreeWorld` live in
  `Scene1Room0Group6ElderBattleScript.java`.
- battle factory wrappers live in `VqsvBattleScripts.java`:
  - `room1BunnyBattleCaptureRuntime`
  - `room0Group6ElderBattleRuntime`

Recommended next low-risk slice:

1. Pause refactor and continue source route work, or
2. do a dedicated dead-code cleanup for `sourceStateApprox` only after a fresh
   call-site scan.

Still not recommended yet:

- splitting `press0`, `click`, `setMoveKey`, or `tick`, because those are core
  frame/input/event-runner paths and can regress every route if moved wrongly.
