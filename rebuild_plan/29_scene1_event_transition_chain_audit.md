# Scene 1 Event Transition Chain Audit

Scope: source-backed chain from `scene_1 room3 group0` ending records to the
next valid event trigger. This file exists to prevent continuing by visual guess
or by appending the next event list manually without source transition proof.

Status terms:

- `VERIFIED`: source line and/or decoded event data directly supports it.
- `APPROX`: current rebuild behavior follows the main intent but does not yet
  port the original subsystem.
- `OPEN`: must be audited or implemented before claiming parity.

## 1. Current Rule

Do not decide "next event" from the order of manual Java code. The original game
does this chain:

```text
active event opcode executes
  -> event state persists in game.c.b[linearRoom][eventId]
  -> game.i state changes if opcode requests room/world transition
  -> game.k.d() reloads target room resources
  -> game.c.a(...) parses target room event groups
  -> game.c.b() scans idle events by first opcode trigger
  -> only triggered event enters active vector
```

Therefore the next port target must be proven by trigger scan, not by assuming
the next room group automatically runs.

## 2. Source Chain: Room3 Group0 -> Room0 Group0

Source anchors:

- `modules/source_code/decoded/decompiled_source_cfr/game/c.java:840`
- `modules/source_code/decoded/decompiled_source_cfr/game/c.java:514`
- `modules/source_code/decoded/decompiled_source_cfr/game/i.java:267`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java:312`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java:529`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java:628`
- `modules/source_code/decoded/decompiled_source_cfr/game/c.java:188`

Decoded event data:

- `modules/event/decoded/data__event__scene_1.mid.json`
- `scene_1 room3 group0 record 101`: `op22 [1,2,199,218,240,320]`
- `scene_1 room3 group0 record 102`: `op6 [1,0,0,0]`
- `scene_1 room3 group0 record 103`: `op14 []`
- `scene_1 room0 group0 record 0`: `op15 [1,3,0]`

Verified flow:

1. `op22` sets transition metadata on `game.k`: transition flag, `w`,
   target/camera coordinates `h/i`, viewport `B/C`, and clears `j`.
2. `op6` persists current event state as complete (`3`) for current
   linear room/event id, sets target scene/room to `[1,0]`, leaves event actor
   target as `-1` because arg3 is `0`, then sets `game.i` state `22`.
3. `game.i` state `22` calls `game.k.a().d()`, then returns to world state `11`.
4. `game.k.d()` opens `/data/event/scene_1.mid`, skips to room0, reads room
   strings, room name, map id, actors, event groups, loads map/display/UI, then
   calls `M.i()` and `M.b()` where `M = game.c`.
5. `game.c.b()` scans event groups with state `0` or `4`. Room0 group0 starts
   with `op15 [1,3,0]`, which checks event state table for scene1 room3 event0.
   Because room3 event0 was persisted as `3`, room0 group0 is legitimately
   triggered.

Conclusion: porting `scene_1 room0 group0` after room3 group0 is source-valid,
but it must be marked as a triggered event, not just a manual append.

## 3. Room0 Group0 Resource And Opcode Owners

Decoded data:

- Room name: `Thuy Moc Thon`
- Map id / `unknown_ab`: `2`
- Actor count: `58`
- Event groups: `12`
- Current target group: room0 group0 records `0..29`

Resources directly used or implied:

| Resource | Source/data reason | Current rebuild status |
|---|---|---|
| `/data/event/scene_1.mid` | room/event data parsed by `game.k.d()` | `APPROX` via decoded JSON/manual rows |
| `/data/map/map_2.mid` | room0 `unknown_ab = 2` | `APPROX/PORTED map render` via `MapRenderer` |
| `/data/ui/dialog.ui` | op4 and op51 call `game.h` dialog paths | `APPROX` custom `TextBox` |
| `/data/ui/taskTip.ui` + sprite `257` anim `10` | op45 calls `game.h.c(text)`, which loads taskTip, attaches sprite `257` to visual id `1`, then writes text to visual id `2` | `APPROX`: rebuild now uses source taskTip visual id `1/2` positions, text slot `(16,154,208,*)`, and horizontal over-width text scroll; full close animation state `13` not ported |
| `/data/ui/openbox.ui` + sprite `257` anim `9` | op17 inventory popup path in `game.h.a(String,int)` / `game.h.b(String)`; `game.h.e(String)` attaches `m` sprite `257` to visual id `1`, text goes to visual id `2` | `APPROX`: openbox popup now uses source visual id `1/2` positions, source sprite `257` anim `9`, and source text slot `(47,154,146,*)`; full `game.h.S` UI runtime not ported |
| sprite index `259` | op5 temp effect sprite | `APPROX` effect shortcut |
| room0 actor sprite table indexes | actor records from event room actor table | `PARTIAL`; see sprite mapping audit |

Important opcode owners for room0 group0:

| Opcode | Source owner | Current rebuild status |
|---:|---|---|
| `15` | `game.c.b()` trigger scan | `APPROX`, source-valid trigger chain now documented |
| `1/51` | `game.j` text renderer | `APPROX` custom text box |
| `2/5/8/10` | `game.a`, `game.g`, root sprite/movement | `APPROX`; no full player/world model |
| `4/45` | `game.h` UI workflow | `APPROX`; not real `.ui` runtime |
| `17/39/25` | `game.g/game.b/game.c` gameplay state | `APPROX/PORTED`: `op17` inventory state + popup, `op39` represented pet loop, `op25` exact flag |
| `22/6/14` | `game.k/game.i/game.c` transition/persist | `APPROX`; manual world load |

## 4. Source Chain: Room0 Group0 -> Room1 Free World

Decoded event data:

- `scene_1 room0 group0 record 26`: `op22 [1,1,55,279,240,320]`
- `scene_1 room0 group0 record 27`: `op25 [1]`
- `scene_1 room0 group0 record 28`: `op6 [1,1,37,1]`
- `scene_1 room0 group0 record 29`: `op14 []`

Source anchors:

- `game.c.java:840`: `op22` transition metadata.
- `game.c.java:869`: `op25` sets static `game.c.f = args[0] == 0`.
- `game.c.java:514`: `op6` persists current event state and sets target
  scene/room plus actor id when arg3 is `1`.
- `game.k.java:551`: transition placement logic.
- `game.k.java:561`: if player already exists and `j >= 0`, place player at
  target actor tile, not simply at `op22 h/i`.

Decoded room1 data:

- Room name: `Bat duoc Thuy Moc Thon`
- Map id / `unknown_ab`: `5`
- Actor count: `51`
- Actor 37 values: `[1,223,2,19,273,1,1,1,1,0,30]`
- Map5 tile size: `16`

Verified source implication:

- At this point the player model has already been created/positioned earlier.
- Because `op6 [1,1,37,1]` sets `j = 37`, `game.k.d()` uses actor 37 placement.
- For map5 tile size `16`, actor37 approximate tile-aligned placement is
  `x = 19 - 19 % 16 = 16`, `y = 273 - 273 % 16 = 272`.
- Current rebuild now stores this as player approximation after loading room1.

Conclusion: after room0 group0, the game enters room1/free-world near actor37.
It does not automatically run room1 group0.

## 5. Why Room1 Group0 Is Not The Immediate Script To Append

Decoded room1 group0 first command:

```text
record 0: op13 [370,176,80,32]
```

Source trigger:

- `game.c.java:183` and `game.c.java:727`
- `op13` checks rectangle overlap through `ae.a(...)`.
- `ae.java:337` is the rectangle-vs-player-shape helper used by this trigger.

Room1 placement versus trigger:

```text
player approx after transition actor37: x=16, y=272
room1 group0 trigger rect: x=370, y=176, w=80, h=32
```

These are far apart. The original game expects free movement / player control
or world simulation before this trigger can fire.

Conclusion: the next correct engineering task is not "append room1 group0".
The next task is to port enough world/free movement and trigger scanning to let
the player reach room1 group0, or explicitly make a debug-only manual trigger
and label it as such.

## 6. Current Code Status After Correction

Implementation file:

- `rebuild_game/src/main/java/VqsvIntroDemo.java`

Current manual code facts:

- Room3 group0 -> room0 group0 remains source-valid by `op15 [1,3,0]`.
- Sprite rendering in the demo now resolves event actor `spriteIndex` through
  source `sprite.mid` instead of treating it as direct `sprId`; this fixes the
  room0 crowd actors that were previously blank/wrong.
- Neil/player is now rendered from source player sprite index `0` after
  room0 `op8 [199,218]`; earlier smoke images that lacked Neil were wrong.
- World UI from `/data/ui/world.ui` is represented by the source-backed lower
  corner widgets; see audit `30`.
- Room3 record 96 `op9 [15,0,120,100,10,0]` now uses real `ikon_1` tex asset
  instead of the earlier circle placeholder.
- Room0 group0 records `0..29` are represented. Several are still `APPROX`,
  but audit `28` now has `STUB: 0` for this group.
- End of room0 group0 loads room1 map id `5` and actor table.
- Code now records player approximation at actor37 tile-aligned position
  `(16,272)` after room1 load.
- Room1 group0 is not appended.

## 7. Next Work That Is Source-Correct

Priority order:

1. Implement a minimal source-backed world/free-movement state for the current
   demo path: player position, direction/action state, camera follow, map
   collision query, and `op13` trigger check.
2. Improve `opcode 45` and `opcode 51` from generic `TextBox` into real
   `game.h`/`.ui`-backed approximations, using `dialog.ui` and `taskTip.ui`.
3. Only after player reaches `[370,176,80,32]`, port room1 group0 records
   `0..13`; its battle opcodes are currently expected to remain `STUB` until
   `game.d` is ported.

Do not mark room1 group0 as `PORTED` until the trigger path and its battle/UI
side effects are source-backed.
