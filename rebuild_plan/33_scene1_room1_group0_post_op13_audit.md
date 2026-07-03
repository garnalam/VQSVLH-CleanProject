# Scene 1 Room 1 Group 0 Post-Op13 Audit

Scope: `modules/event/decoded/data__event__scene_1.mid.json`,
`room_index=1`, `event_group[0]`, records `0..13`.

Implementation: `rebuild_game/src/main/java/VqsvIntroDemo.java`, appended after
the source-backed `Op13FreeWorldTrigger` from audit 32.

## Source Flow

- Record `0` `op13 [370,176,80,32]` is a trigger, not ordinary script.
- After op13 fires, source runs battle setup:
  - `op37 [34,5,1]` -> `game.d.a().a(new int[][]{{34,5,1}})`
  - `op52 [0,1]` -> VM/battle flags: `this.i=true`, `game.c.j=false`
  - `op66 [0]` -> `an.U=0`, platform/global input state side effect
  - `op32 [0,0]` -> capture world screen, set battle mode fields, stop player,
    switch runtime to battle state `12`
  - battle returns `game.c.l`; `op47 [12,0,0]` branches by that result
- Source result handling observed in `game.d`:
  - `game.k.a().M.l = 0` for one battle end path
  - `game.k.a().M.l = 1` for another battle end path
  - `game.k.a().M.l = -1` in the capture/full-bag/release path
- For this rebuild slice, battle/capture is still a visible stub. It forces
  `l=-1` to represent successful Bunny capture and continue through records
  `6..10`. This keeps the quest progression source-valid without pretending the
  battle engine is implemented.

## Record Matrix

| Rec | Opcode | Source intent / args | Current implementation | Status | Notes |
|---:|---:|---|---|---|---|
| 0 | 13 | Area trigger `[370,176,80,32]` | `Op13FreeWorldTrigger` | APPROX | Source rect/footprint represented; full tile/actor collision remains approximate. |
| 1 | 37 | Battle setup `[34,5,1]` | `room1BunnyBattleCaptureStub()` | STUB/APPROX | Params captured and shown in battle overlay; no `game.d` engine. |
| 2 | 52 | Flags `[0,1]` | source trace + battle stub flags | APPROX | Represents `this.i=true`, `game.c.j=false`. |
| 3 | 66 | `an.U=0`, global input/platform state | source trace | APPROX | Side effect recorded, no platform input subsystem. |
| 4 | 32 | Enter battle mode `[0,0]` | `ScriptedBattleStub(mode=[0,0])` | STUB | Visible battle placeholder, not real battle. |
| 5 | 47 | Branch `[12,0,0]` by battle result `l` | forced `l=-1`, continue success path | STUB/APPROX | Source branch table retained in trace; retry branch not exercised until real battle/capture result exists. |
| 6 | 4 | Neil success dialog | `dialog("Neil", ...)` | PORTED | Text/speaker kept; dialog UI still workflow-level. |
| 7 | 56 | Hide/deactivate actor `50` | `op56ActorVisibility(1,[50],[0])` | APPROX | Actor hidden and trace recorded; persistence is trace-only. |
| 8 | 23 | Mark event `[1,0,1]` complete | `op23MarkEventComplete(1,0,1)` | APPROX | Event-state mutation recorded; no generic event-state table yet. |
| 9 | 40 | Message `Trở về tìm trưởng thôn!` | `taskNotice(...)` | APPROX | Uses existing source-backed taskTip/open message renderer. |
| 10 | 14 | Complete current event | `op14CompleteEvent(1,1,0)` | APPROX | Completion trace recorded; active-event VM not generic yet. |
| 11 | 4 | Retry dialog `Chỉ có thể đến một lần nữa!` | Not exercised | PENDING | Should run only from battle branch, not in success path. |
| 12 | 10 | Player action `dir=0 speed=4 duration=8` | Not exercised | PENDING | Branch path after retry dialog. |
| 13 | 42 | Mark event state `4` | Not exercised | PENDING | Branch path state behavior. |

## Verification

- Build:
  `powershell -ExecutionPolicy Bypass -File .\build.ps1`
- Check:
  `java "-Dvqsv.modules=..\modules" -jar ".\build\libs\vqsv-rebuild-skeleton.jar" --check`
- Smoke route:
  `--smoke-drive <out> 5920 "R90,U20" <postTicks>`
- Smoke images:
  - `build_intro_demo/room1_group0_post_op13_sheet.png`
  - `build_intro_demo/room1_group0_battle_overlay.png`
  - `build_intro_demo/room1_group0_neil_dialog.png`
  - `build_intro_demo/room1_group0_probe_210.png`
  - `build_intro_demo/room1_group0_complete_bunny_hidden.png`
- Reflection probe confirmed:
  - after trigger: `current=ScriptedBattleStub`, actor50 visible
  - post battle/dialog/task: `eventIndex=254`, `current=null`, actor50 hidden
  - trace contains op37/op52/op66/op32/op47, op56, op23, and op14 entries

## Next Safe Step

Next source-correct slice is the quest return path from
`Trở về tìm trưởng thôn!`: inspect which room/group trigger consumes the
`op23 [1,0,1]` state and task message, then port that next event. Do not port
the retry branch records `11..13` as normal linear flow.

