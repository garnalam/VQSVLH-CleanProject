# Scene 1 Room 1 Free-World And Op13 Trigger Audit

Scope: bridge after `scene_1 room0 group0` into `scene_1 room1 group0`.

## Source Trace

- Room0 group0 ends with `opcode 6 [1,1,37,1]`.
- Source `game.c case 6` marks the current event complete, sets
  `game.k.a().f/g` to target scene/room, stores actor target `j=37`, then
  switches `game.i` to state `22`.
- Source room loader uses actor target `37` in room1. Actor37 is at
  `[19,273]`; with map5 tile size `16`, rebuild places Neil at `[16,272]`.
- Room1 group0 first record is `opcode 13 [370,176,80,32]`.
- Source trigger scan checks this with
  `ae.a(rectX, rectY, rectW, rectH, player.i, player.j, player.a.k())`.
- If the rectangle overlaps the player collision box, source stops the player
  with `x.b((byte)0, x.n)` and activates the event.

## Rebuild Implementation

- `VqsvIntroDemo` now keeps the room1 transition in a blocking
  `Op13FreeWorldTrigger` instead of ending the script immediately.
- While this blocker is active:
  - arrow keys, WASD, and numpad `8/2/4/6` move Neil;
  - `0` / numpad `0` still confirms text as before;
  - Neil uses source direction mapping `0=down`, `1=right`, `2=up`, `3=left`;
  - speed is `4`, matching the default source movement used in this path;
  - camera centers on Neil each tick;
  - player animation switches between walk state `3` and idle state `0`.
- Trigger check uses a source-shaped player footprint `[-8,-8,16,16]`, matching
  the `game.g` movement collision footprint seen around `i +/- 8`, `j +/- 8`.

## Status

- `PORTED/APPROX`: room1 free-world wait state and op13 area trigger.
- `APPROX`: full `game.g.q()` tile collision and actor collision are not ported
  yet. Current collision prevents leaving map bounds only.
- Room1 group0 records after op13 are now audited in
  [33_scene1_room1_group0_post_op13_audit.md](33_scene1_room1_group0_post_op13_audit.md).
  The success path is represented with a source-backed battle/capture stub;
  retry branch records `11..13` remain pending until battle result handling is
  real.

## Verification

- Build:
  `powershell -ExecutionPolicy Bypass -File .\build.ps1`
- Check:
  `java "-Dvqsv.modules=..\modules" -jar ".\build\libs\vqsv-rebuild-skeleton.jar" --check`
- Smoke:
  - `build_intro_demo/room1_freeworld_op13_sheet.png`
  - `build_intro_demo/room1_freeworld_start.png`
  - `build_intro_demo/room1_freeworld_right_mid.png`
  - `build_intro_demo/room1_op13_trigger_reached.png`
- Probe:
  - after preload tick `5920`: `eventIndex=248`, `current=Op13FreeWorldTrigger`,
    player `[16,272]`
  - after route `R90,U20`: `current=null`, player `[376,216]`
  - trace contains `op13 wait scene=1 room=1 group=0 rect=[370,176,80,32]`
    followed by `op13 trigger scene=1 room=1 group=0 player=[376,216]`
