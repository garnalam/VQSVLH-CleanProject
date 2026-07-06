# Scene Tick Input Runner Audit

Date: 2026-07-06

Scope: audit only for `VqsvIntroDemo.Scene` input and frame/event runner
methods:

- `press0()`
- `click(int,int)`
- `setMoveKey(int,boolean)`
- `tick()`

This document intentionally does not move these methods.

## Current Truth

These methods remain inside `VqsvIntroDemo.Scene` and are the central frame
runner for the current manual route.

Current route still depends on this runner for:

- scene0 intro text prompt and timing;
- scene1 room3 dialogs and battle transition;
- ten-years-later room0 group0;
- room1 Bunny battle and return task;
- actor52 interaction and group2 dialogs;
- pet selection choice UI;
- elder battle/reward/free-world unlock;
- smoke harness fast-forward and route-driving helpers.

## Method Matrix

| Method | Current responsibility | Classification |
|---|---|---|
| `press0()` | Sets one-frame confirm/action flag `key0 = true`. | `DO_NOT_TOUCH_YET` |
| `click(int,int)` | Converts scaled screen coordinates, lets `ChoiceBox` consume clicks, otherwise maps click to `key0`. | `DO_NOT_TOUCH_YET` |
| `setMoveKey(int,boolean)` | Maps arrows/WASD/numpad to held direction booleans. | `DO_NOT_TOUCH_YET` |
| `tick()` | Advances effects, text, temp sprites, current blocking event, event list, actor/player ticks, camera follow, and clears `key0`. | `DO_NOT_TOUCH_YET` |

## Tick Ordering

Current `tick()` order:

1. `effect.tick()`
2. `text.tick(font)` and dispose-to-null if needed
3. tick/remove `tempSprites`
4. if `current != null`, tick the current blocking event
5. if the blocking event is still active:
   - clear `key0`
   - tick actors/player
   - update camera follow
   - return
6. if current finished, clear `current`
7. auto-start events while there is no current blocking event
8. clear `key0`
9. tick actors/player
10. update camera follow

This order is behavioral. It affects prompt acceptance, choice click handling,
free-world interaction, event auto-advance, and route smoke timing.

## Key Risk: `key0` Lifetime

`key0` is intentionally short-lived. It is set by `press0()` and `click()`, then
cleared inside `tick()`.

Risk if moved incorrectly:

- text boxes may skip too fast;
- text boxes may never confirm;
- choice clicks may double-submit or fail;
- actor interaction op16/op38 may trigger at the wrong time;
- smoke fast-forward may no longer advance dialogs.

## Key Risk: Blocking Event Lifecycle

The `current` field stores the active `Blocking`.

Risk if moved incorrectly:

- a blocking movement/effect/dialog may be ticked twice in one frame;
- a completed blocking event may fail to clear;
- event list can auto-run too far because the guard loop semantics change;
- actor/player ticks can happen in a different phase than before.

## Key Risk: Actor / Player / Camera Tick Order

Actors and player are ticked after current-event handling, then camera follow is
updated.

Risk if moved incorrectly:

- scene0 actor movements can drift;
- room3 chase/dialog staging can visually regress;
- free-world position/camera can differ from smoke expectations;
- transition trigger placement can change.

## Current Consumers

Observed consumers include:

- Swing key listener in `VqsvIntroDemo`;
- Swing mouse listener in `VqsvIntroDemo`;
- `VqsvSmokeHarness.tickSceneFastForward`;
- `VqsvSmokeHarness.driveRoute`;
- scripts that rely on `key0`, `keyUp`, `keyDown`, `keyLeft`, `keyRight`;
- `ChoiceBox` through `click` and `key0`;
- `TextBox` confirmation through `waitForText`.

## Classification

| Area | Status | Reason |
|---|---|---|
| Input mapping | `KEEP_CORE` | Small, but coupled to Swing and smoke harness. |
| Click-to-choice behavior | `KEEP_CORE` | Choice click semantics are part of current UI behavior. |
| `key0` one-frame lifetime | `DO_NOT_TOUCH_YET` | High regression risk. |
| `current` blocking lifecycle | `DO_NOT_TOUCH_YET` | Core event-runner behavior. |
| event auto-start guard loop | `DO_NOT_TOUCH_YET` | Affects manual script chain. |
| actor/player/camera tick order | `DO_NOT_TOUCH_YET` | Affects visual route timing. |

## Future Move Preconditions

Before moving this runner into a file such as `VqsvSceneRunner.java`, first:

1. Add a route/checkpoint smoke specifically for click choice confirm.
2. Add a smoke/trace that confirms `key0` clears after one tick.
3. Add a smoke/trace that confirms one blocking event cannot be advanced twice
   in a single frame.
4. Add a smoke route around actor52 interaction and pet choice.
5. Keep `tick()` move separate from any UI/battle/text changes.

## Required Smoke If Ever Moved

```powershell
$env:PROJECT_ROOT = "<path-to-project-root>"
$env:MODULES_ROOT = Join-Path $env:PROJECT_ROOT "modules"
$env:REBUILD_GAME = Join-Path $env:PROJECT_ROOT "rebuild_game"
cd $env:REBUILD_GAME
powershell -ExecutionPolicy Bypass -File .\build.ps1
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" com.vqsv.rebuild.Main --check
```

PNG smoke set:

```powershell
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke "$env:REBUILD_GAME\build_intro_demo\tick_runner_intro_t900.png" 900
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke "$env:REBUILD_GAME\build_intro_demo\tick_runner_10years_t5400.png" 5400
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint actor52_interaction_group2 "$env:REBUILD_GAME\build_intro_demo\tick_runner_actor52.png"
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint room0_pet_choice_ui "$env:REBUILD_GAME\build_intro_demo\tick_runner_pet_choice.png"
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint route_bunny_after_battle_task "$env:REBUILD_GAME\build_intro_demo\tick_runner_bunny_route.png"
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state "$env:REBUILD_GAME\build_intro_demo\tick_runner_elder_route.png"
```

## Final Recommendation

Do not move `press0`, `click`, `setMoveKey`, or `tick` yet.

The runner should stay in `VqsvIntroDemo.Scene` until there is either:

- a proper top-level `VqsvScene` context object; or
- a generic/source-backed event runner that makes the current manual frame loop
  less central.
