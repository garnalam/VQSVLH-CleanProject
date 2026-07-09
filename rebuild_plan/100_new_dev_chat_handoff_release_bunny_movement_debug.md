# 100 New Dev Chat Handoff - Release Test + Bunny Movement Debug

Status: HANDOFF / REQUIRED READING / DEBUG TARGET.

Audience: a new dev chat taking over the current rebuild project.

Immediate target: debug and fix why the official release-test path can become
stuck after "Choi tiep" / transition to the Bunny map, where the player should
be able to move freely and trigger the Bunny capture route.

Do not treat this as a generic battle task. The current blocker is release
runtime + save/continue + room1 free-world resume.

## Supreme Rules

- Do not guess logic, assets, UI, timing, or event continuation.
- Every behavior claim must trace to one of:
  - decoded event JSON,
  - decompiled source,
  - decoded/original UI,
  - decoded/original asset/module,
  - current rebuild implementation.
- Classify every claim as `PORTED`, `PORTED/PARTIAL`, `PARTIAL`, `APPROX`,
  `STUB`, `PENDING`, `UNKNOWN`, or `REBUILD_POLICY`.
- Do not hide partial/stub behavior.
- Do not modify unrelated intro/world/panel/battle code.
- Do not open the live game/client unless the user explicitly asks.
- For automated visual checks, use headless smoke PNG only.
- Never run `com.vqsv.rebuild.Main --smoke*`; that path is intentionally not
  the smoke runner and may open the app in older code.
- Use `VqsvIntroDemo --smoke-checkpoint` or `VqsvIntroDemo --smoke-suite` for
  smoke PNG checks.

## Required Reading Order

Read these before coding:

1. `rebuild_game/RELEASE_TEST_BUILD.md`
2. `rebuild_game/README.md`
3. `rebuild_plan/98_save_resume_bunny_task_source_audit.md`
4. `rebuild_plan/battle_engine_master_roadmap_progress.md`
5. `rebuild_game/src/main/java/com/vqsv/rebuild/Main.java`
6. `rebuild_game/src/main/java/com/vqsv/rebuild/state/BootFlowState.java`
7. `rebuild_game/src/main/java/com/vqsv/rebuild/state/LegacyIntroDemoState.java`
8. `rebuild_game/src/main/java/VqsvSaveRuntime.java`
9. `rebuild_game/src/main/java/VqsvRoom1Group1SavePromptWrapper.java`
10. `rebuild_game/src/main/java/VqsvSavePromptBlocking.java`
11. `rebuild_game/src/main/java/Scene1Room0Group0Script.java`
12. `rebuild_game/src/main/java/Scene1Room1BunnyScript.java`
13. `rebuild_game/src/main/java/VqsvScriptBlocks.java`
14. `rebuild_game/src/main/java/VqsvFreeWorldRuntime.java`
15. `modules/event/decoded/data__event__scene_1.mid.json`
16. `modules/source_code/decoded/decompiled_source_cfr/game/c.java`
17. `modules/source_code/decoded/decompiled_source_cfr/game/k.java`

If any numbered file is missing, report that first and continue with the closest
available source.

## Project Structure Snapshot

Repository layout used by this project:

```text
<ResourcesVQSV>/
  modules/
    event/decoded/
    source_code/decoded/decompiled_source_cfr/game/
    script/original/
    script/decoded/
    ui/original/
    ui/decoded/
    img/
    spr/
  rebuild_game/
    src/main/java/
    build.ps1
    run.ps1
    scripts/reset_save.ps1
    RELEASE_TEST_BUILD.md
    README.md
  rebuild_plan/
```

Do not hardcode a user-specific absolute path in docs or code. Use project
relative paths or placeholders like `<ResourcesVQSV>`.

## Official Test Build

Official play-test command:

```powershell
cd <ResourcesVQSV>/rebuild_game
powershell -ExecutionPolicy Bypass -File ./run.ps1
```

This builds and launches:

```text
rebuild_game/build/libs/vqsv-liet-hoa-rebuild.jar
```

Official runtime path:

```text
com.vqsv.rebuild.Main
  -> GameApp
  -> BootFlowState
  -> LegacyIntroDemoState
  -> VqsvIntroDemo.Scene
```

`VqsvIntroDemo --play-*` is dev-only. It is not the official player-facing test
build.

## Dev Smoke Rules

Smoke PNG checks must use the dev runner directly:

```powershell
cd <ResourcesVQSV>/rebuild_game
powershell -ExecutionPolicy Bypass -File ./build.ps1
java -Dvqsv.modules=../modules -cp build/classes VqsvIntroDemo --smoke-checkpoint <checkpoint> <out.png>
```

Smoke suite example:

```powershell
java -Dvqsv.modules=../modules -cp build/classes VqsvIntroDemo --smoke-suite battle_quick build/smoke/suites/battle_quick
```

Do not use:

```powershell
java -jar build/libs/vqsv-liet-hoa-rebuild.jar --smoke-checkpoint ...
```

Current `Main` prints an error for `--smoke*`, but the safe habit is to never
use the release entrypoint for smoke.

## Recent Fixes To Preserve

These are current implementation facts to verify, not rewrite blindly:

| Area | Current implementation | Status |
| --- | --- | --- |
| Official launcher | `run.ps1` rebuilds then launches release jar | PORTED |
| Release path | `Main -> GameApp -> BootFlowState -> LegacyIntroDemoState -> VqsvIntroDemo.Scene` | PORTED |
| Smoke guard | `Main --smoke*` prints an error instead of launching smoke | PORTED |
| Legacy input bridge | `LegacyIntroDemoState` forwards arrows/WASD/numpad movement, back keys, click, key 5/numpad 5 | PORTED/PARTIAL |
| Battle debug text | Hidden unless `-Dvqsv.battle.debugLog.visible=true` | PORTED |
| Battle key carry-over | Battle runtime consumes `key0` in command/skill/action states | PORTED/PARTIAL |
| Save prompt UI | Save prompt uses source-backed `msgtip.ui` geometry in scene view | PORTED/PARTIAL |
| Room1 Bunny save repair | `VqsvSaveRuntime.repairKnownRouteSave` repositions invalid old room1 save to op13 trigger area | PORTED/PARTIAL |
| Save reset helper | `rebuild_game/scripts/reset_save.ps1` supports `Room1Bunny` and `Delete` | REBUILD_POLICY |

## Known Current Bug

User-reported bug:

- After choosing "Choi tiep" or after the Bunny-map transition, the player can
  get stuck and cannot move freely.
- The user expects the room1 Bunny map to be a free-world segment until the
  op13 trigger starts the Bunny capture route.
- Do not assume the smoke route proves the release path is fixed. The live
  release test still needs investigation.

Observed old invalid save shape:

```text
build/save/vqsv_autosave.properties
scene=1
room=1
eventIndex=250
player=16,272,2,1
eventState.0=1:1:1=3
```

Interpretation:

- `scene=1 room=1` means the Bunny map.
- `1:1:1=3` means the save wrapper group is complete.
- Bunny group0 is not complete.
- Player position `(16,272)` is far from the source op13 trigger rectangle
  `[370,176,80,32]`.
- This can strand the player in a room1 snapshot that cannot naturally hit the
  trigger.

Current repair in `VqsvSaveRuntime.repairKnownRouteSave`:

```text
if scene=1 room=1
and event state [1,1,1] complete
and event state [1,1,0] not complete
and player does not intersect op13 rect [370,176,80,32]
then set player to (374,180) and camera center to (374,180)
```

Status: PORTED/PARTIAL. It is a known-route repair, not full original RMS
resume parity.

## Source Facts To Prove Before Coding

The next dev chat must answer these in chat before editing code:

1. Official release proof:
   - Show how `run.ps1` launches the release jar.
   - Show how `Main` enters `BootFlowState`.
   - Show how `BootFlowState` routes "Choi tiep" to `LegacyIntroDemoState(true)`.
   - Show how `LegacyIntroDemoState(true)` calls `VqsvSaveRuntime.loadInto`.

2. Input bridge proof:
   - Show that `LegacyIntroDemoState.tick` forwards movement keys to
     `VqsvIntroDemo.Scene.setMoveKey`.
   - Show that pointer click and key 5/numpad 5 reach `click`/`press0`.
   - Identify any missing keys or scale mismatch if found.

3. Bunny event chain proof:
   - From event JSON and/or scripts, prove the chain:
     - room0 group0 sends player to room1 Bunny map,
     - room1 save wrapper can complete,
     - room1 group0 starts from op13 rect `[370,176,80,32]`,
     - Bunny complete uses actor transition back to room0.
   - Prove whether op13 should auto-run or require free-world collision.

4. Save/resume proof:
   - Show exact saved fields read/written by `VqsvSaveRuntime`.
   - Explain why `eventIndex=250` is or is not valid after room load.
   - Explain whether `current` should be null after load or should resume a
     specific blocking event.
   - Explain whether `repairKnownRouteSave` should adjust only player/camera or
     also event index/current blocking state.

5. Smoke/debug plan:
   - Propose headless smoke(s) for room1 continue/free-move/op13 trigger.
   - Confirm no client will be opened by the commands.

If the new dev chat cannot answer these, it must not code yet.

## Compulsory Entry Exercise

Before coding, write a short response with this matrix:

| Question | Evidence file/method | Answer | Status |
| --- | --- | --- | --- |
| What is the official launch path? | `run.ps1`, `Main`, `BootFlowState` | ... | ... |
| How does "Choi tiep" load a save? | `BootFlowState`, `LegacyIntroDemoState`, `VqsvSaveRuntime` | ... | ... |
| Which input paths control movement after continue? | `LegacyIntroDemoState.tick`, `VqsvIntroDemo.Scene` | ... | ... |
| Which source event opens room1 Bunny op13? | event JSON/script/source | ... | ... |
| What is the op13 rectangle? | `Scene1Room0Group0Script`, `VqsvScriptBlocks`, event JSON | ... | ... |
| Why can an old save get stuck? | save file shape + op13/player rect | ... | ... |
| What exact smoke will you add/run? | `VqsvSmokeHarness` or new checkpoint | ... | ... |

Also list unknowns before implementation.

## Recommended First Code Slice

Only after the entry exercise is correct:

1. Add a headless smoke checkpoint for "continue from room1 Bunny save can move
   or trigger op13".
2. The checkpoint should construct/load the known problematic save state without
   opening the client.
3. Assert at least:
   - current scene/room is `1/1`,
   - player is near or intersects op13 after repair,
   - free-world movement key changes player position when no blocking UI/event
     is active, or op13 trigger completes when expected,
   - no save prompt/dialog/battle overlay is unexpectedly blocking movement.
4. If it fails, debug the exact blocker:
   - `Scene.current` not null,
   - `text`/`choice`/save prompt visible,
   - movement keys not reaching Scene,
   - player collision/map bounds,
   - camera/player mismatch,
   - event index/current blocking state mismatch.

Do not fix by blindly teleporting the player unless the source route or a
clearly documented rebuild-save repair justifies it.

## Manual User Recovery Commands

These are for the user or for explicit user-approved manual recovery, not for
automated tests that silently mutate state.

Reset an existing save to the known room1 Bunny trigger checkpoint:

```powershell
cd <ResourcesVQSV>/rebuild_game
powershell -ExecutionPolicy Bypass -File ./scripts/reset_save.ps1 -Mode Room1Bunny
```

Delete the rebuild autosave:

```powershell
powershell -ExecutionPolicy Bypass -File ./scripts/reset_save.ps1 -Mode Delete
```

Then official play-test:

```powershell
powershell -ExecutionPolicy Bypass -File ./run.ps1
```

## Validation Checklist After Any Fix

Required:

```powershell
cd <ResourcesVQSV>/rebuild_game
powershell -ExecutionPolicy Bypass -File ./build.ps1
java -Dvqsv.modules=../modules -jar build/libs/vqsv-liet-hoa-rebuild.jar --check
```

Mojibake scan must be run on Java/docs touched by the change. If a local script
exists, use it; otherwise use `rg` for suspicious replacement characters and
known broken sequences in touched files.

Suggested smoke PNGs:

```text
room1_bunny_continue_free_move.png
room1_bunny_continue_op13_trigger.png
route_bunny_after_battle_task.png
route_elder_after_battle_reward_state.png
```

Only use `VqsvIntroDemo --smoke-checkpoint` or `--smoke-suite` for these.

## Current Broader Roadmap Context

Battle engine work has advanced far, but this handoff is intentionally not the
next battle phase. Current priority is stabilizing the official release-test
runtime and save/continue route before continuing phase work.

After Bunny movement/continue is fixed and smoke-covered, return to the battle
roadmap in `battle_engine_master_roadmap_progress.md`.

Recommended next order after this blocker:

1. Close the release/continue Bunny movement bug with source-backed smoke.
2. Re-run route regressions for Sophie/Bunny/Elder.
3. Only then continue the current battle/UI roadmap phase.

## Honest Status

- Official release build path: PORTED.
- Dev smoke path: PORTED/PARTIAL.
- Save prompt and autosave route snapshot: PORTED/PARTIAL.
- Full original RMS save/load parity: PENDING.
- Bunny room1 continue/free-move from old invalid saves: ACTIVE DEBUG TARGET.
- Battle engine: many slices PORTED/PARTIAL, but not the current blocker.

