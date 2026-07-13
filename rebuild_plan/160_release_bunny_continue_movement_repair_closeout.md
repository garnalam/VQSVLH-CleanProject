# 160 Release Bunny Continue Movement Repair Closeout

Status date: 2026-07-09

Status: RELEASE/CONTINUE BLOCKER FIXED / SOURCE-BACKED ROUTE REPAIR / PNG SMOKE COVERED.

Purpose:

- Close the debug target from
  `100_new_dev_chat_handoff_release_bunny_movement_debug.md`.
- Fix the official release-test path where "Choi tiep" could load an old
  room1 Bunny save and strand the player away from the op13 trigger/free-world
  route.
- Preserve the current battle roadmap scope: this is release runtime +
  save/continue + room1 free-world resume, not a generic battle/P7 slice.

## Entry Exercise Result

| Question | Evidence file/method | Answer | Status |
| --- | --- | --- | --- |
| What is the official launch path? | `rebuild_game/run.ps1`, `com.vqsv.rebuild.Main`, `GameApp`, `BootFlowState` | `run.ps1` builds and launches `build/libs/vqsv-liet-hoa-rebuild.jar`; `Main` creates `GameApp`, which starts at `BootFlowState`. | `PORTED` |
| How does "Choi tiep" load a save? | `BootFlowState.updateTitleMenu`, `LegacyIntroDemoState(boolean)`, `VqsvSaveRuntime.loadInto` | When save exists and selected menu is index `0`, `BootFlowState` replaces state with `new LegacyIntroDemoState(true)`, which calls `VqsvSaveRuntime.loadInto(scene)`. | `PORTED/PARTIAL` |
| Which input paths control movement after continue? | `LegacyIntroDemoState.tick`, `VqsvIntroDemo.Scene.setMoveKey`, `click`, `press0` | Arrows/WASD/numpad movement keys are forwarded to `setMoveKey`; pointer is scaled by 2 into `click`; confirm/5/numpad5 calls `press0`. | `PORTED/PARTIAL` |
| Which source event opens room1 Bunny op13? | `Scene1Room0Group0Script`, `VqsvWorldResumeDescriptor`, `Op13FreeWorldTrigger` | Room0 group0 transitions to room1, runs room1 group1 save prompt wrapper, then resumes into room1 group0 op13. | `PORTED/PARTIAL` |
| What is the op13 rectangle? | `Scene1Room0Group0Script`, decoded event JSON | `Op13FreeWorldTrigger(1, 1, 0, 370, 176, 80, 32)`. | `PORTED/APPROX` |
| Why can an old save get stuck? | `VqsvSaveRuntime`, old save shape from handoff 100 | Old saves can have room1 group1 complete, room1 group0 incomplete, player far from op13, and stale `eventIndex` such as `250`; after load, Scene may not be positioned at the current op13 boundary. | `PORTED/PARTIAL` repair target |
| What exact smoke was added/run? | `VqsvSmokeHarness` | `room1_bunny_continue_free_move` and `room1_bunny_continue_op13_trigger`, plus Bunny/Elder/Sophie route regressions. | `PORTED/PARTIAL` coverage |

Unknowns remaining:

- Full original RMS save/load parity is still `PENDING`.
- Generic decoded event VM cursor restoration is still `PENDING`.
- This slice repairs a known route save shape only; it is not a complete save
  compatibility layer for every possible old autosave.

## Root Cause

The old invalid save shape:

```text
scene=1
room=1
eventIndex=250
player=16,272,2,1
eventState.0=1:1:1=3
```

means:

- current map is room1 Bunny map;
- room1 group1 save wrapper is complete;
- room1 group0 Bunny route is not complete;
- player is far from the source op13 rectangle `[370,176,80,32]`;
- `eventIndex=250` is not the current room1 Bunny op13 boundary in the
  rebuild script list.

The previous repair only moved player/camera broadly. It did not reliably reset
the rebuild event cursor to the room1 Bunny op13 boundary, so a continued game
could load into room1 without the correct free-world trigger installed.

## Fix Implemented

Java:

- `rebuild_game/src/main/java/VqsvIntroDemo.java`
  - added `Scene.room1BunnyOp13EventIndex`.
- `rebuild_game/src/main/java/Scene1Room0Group0Script.java`
  - registers the current dynamic event index immediately before adding
    `VqsvWorldResumeDescriptor.SCENE1_ROOM1_AFTER_SAVE_TO_OP13.wrap(new Op13FreeWorldTrigger(...))`.
- `rebuild_game/src/main/java/VqsvSaveRuntime.java`
  - `repairKnownRouteSave` now restores `eventIndex` to the registered room1
    Bunny op13 event index for clearly stranded room1 Bunny saves.
  - repair is scoped to saves where:
    - `scene=1`, `room=1`;
    - `[1,1,1]` is complete;
    - `[1,1,0]` is not complete;
    - player is not intersecting source op13 rect `[370,176,80,32]`;
    - player is not already near the broader op13 area `[290,96,240,192]`.
  - this avoids teleporting valid near-op13 free-world saves.
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`
  - added `room1_bunny_continue_free_move`;
  - added `room1_bunny_continue_op13_trigger`;
  - replaced stale hardcoded `123` assumptions with the dynamic registered
    room1 Bunny op13 event index.

## Smoke Coverage

Focused release Bunny movement/debug PNGs:

- `rebuild_game/build/smoke_release_bunny_debug/room1_bunny_continue_free_move.png`
- `rebuild_game/build/smoke_release_bunny_debug/room1_bunny_continue_op13_trigger.png`
- `rebuild_game/build/smoke_release_bunny_debug/room1_bunny_save_resume_state.png`

Route regression PNGs:

- `rebuild_game/build/smoke_release_bunny_debug/route_bunny_after_battle_task.png`
- `rebuild_game/build/smoke_release_bunny_debug/route_elder_after_battle_reward_state.png`
- `rebuild_game/build/smoke_release_bunny_debug/route_sophie_after_battle_branch.png`

## Verification Commands

Required:

```powershell
cd rebuild_game
powershell -ExecutionPolicy Bypass -File .\build.ps1
java "-Dvqsv.modules=..\modules" -jar build\libs\vqsv-liet-hoa-rebuild.jar --check
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvBattleDamageFormulaCheck
```

Focused smoke:

```powershell
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint room1_bunny_continue_free_move build\smoke_release_bunny_debug\room1_bunny_continue_free_move.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint room1_bunny_continue_op13_trigger build\smoke_release_bunny_debug\room1_bunny_continue_op13_trigger.png
```

Regression smoke:

```powershell
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint room1_bunny_save_resume_state build\smoke_release_bunny_debug\room1_bunny_save_resume_state.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint route_bunny_after_battle_task build\smoke_release_bunny_debug\route_bunny_after_battle_task.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state build\smoke_release_bunny_debug\route_elder_after_battle_reward_state.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint route_sophie_after_battle_branch build\smoke_release_bunny_debug\route_sophie_after_battle_branch.png
```

Mojibake/diff hygiene:

```powershell
rg -n "<standard mojibake/replacement-character pattern>" rebuild_game/src/main/java/VqsvIntroDemo.java rebuild_game/src/main/java/Scene1Room0Group0Script.java rebuild_game/src/main/java/VqsvSaveRuntime.java rebuild_game/src/main/java/VqsvSmokeHarness.java rebuild_plan/160_release_bunny_continue_movement_repair_closeout.md
git diff --check -- rebuild_game/src/main/java/VqsvIntroDemo.java rebuild_game/src/main/java/Scene1Room0Group0Script.java rebuild_game/src/main/java/VqsvSaveRuntime.java rebuild_game/src/main/java/VqsvSmokeHarness.java rebuild_plan/160_release_bunny_continue_movement_repair_closeout.md
```

## Classification

| Area | Status | Note |
| --- | --- | --- |
| Official release path | `PORTED` | `run.ps1` launches the release jar through `Main`. |
| "Choi tiep" save load | `PORTED/PARTIAL` | Loads rebuild route snapshot into `VqsvIntroDemo.Scene`. |
| Room1 Bunny old invalid save repair | `PORTED/PARTIAL` | Known-route repair for stranded saves only. |
| Room1 Bunny op13 event index | `PORTED/PARTIAL` | Dynamic rebuild event index registration replaces stale hardcoded value in smoke/repair. |
| Existing near-op13 free-world saves | `UNCHANGED` | Broader near-op13 guard prevents unnecessary teleport repair. |
| Full original RMS persistence | `PENDING` | Not implemented by this slice. |
| Generic event VM cursor parity | `PENDING` | Manual rebuild event list remains in use. |
| Battle/P7 visual/runtime behavior | `UNCHANGED` | Not touched by this release blocker fix. |

## Next Step

Recommended next:

```text
Return to the battle engine roadmap after the user confirms the release
"Choi tiep" Bunny movement test is acceptable.
```

If the user still sees a release-only movement issue, debug only this route:

```text
BootFlowState -> LegacyIntroDemoState(true) -> VqsvSaveRuntime.loadInto ->
room1 Bunny op13/free-world state.
```

Do not resume P7/battle visual work until this release continue path is stable.
