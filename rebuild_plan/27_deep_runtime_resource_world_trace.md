# Deep Runtime, Resource, World Trace

Purpose: ghi lai vong doc truc tiep `modules/source_code` sau khi da co ban do
tong quan. File nay tap trung vao duong chay that: boot resource, sprite,
room load, actor init, event VM/opcode. Day la tai lieu hieu runtime, chua phai
implementation plan.

Status terms:

- `VERIFIED`: co source line / data path ro rang.
- `PARTIAL`: da thay flow chinh nhung CFR damage hoac chua doc het nhanh phu.
- `OPEN`: can trace tiep truoc khi port full.

## 1. Boot Resource Pipeline

Source anchors:

- `modules/source_code/decoded/decompiled_source_cfr/game/i.java:188`
- `modules/source_code/decoded/decompiled_source_cfr/aq.java:21`
- `modules/source_code/decoded/decompiled_source_cfr/am.java:14`
- `modules/source_code/decoded/decompiled_source_cfr/aa.java:9`

`game.i.d()` la boot/init resource core. Thu tu da xac nhan:

```text
game.i.d()
  -> this.s()
  -> game.h.a() and ab.a()
  -> S.a(this)
  -> am.a()
  -> aa.a()
  -> aq.a()
  -> game.i.e(0)
  -> game.i.D()
  -> game.k.a()
  -> game.k.i()
  -> game.i.t()
```

Meaning:

- `am.a()` tao image cache arrays `Image[50000]` va refcount byte array
  (`am.java:14-18`).
- `aa.a()` tao sprite metadata cache `o[1000]` (`aa.java:9-11`).
- `aq.a()` nap global script/resource metadata (`aq.java:21-55`).
- `game.k.a()` dam bao world singleton ton tai.
- `game.k.i()` doc RMS/world flag slot `PK6_RMS_RMS`, not a room bootstrap
  (`game.k.java:1144-1163`). Earlier notes that called it "world bootstrap"
  should be corrected.

`aq.a()` load cac bang:

```text
/data/script/sprite.mid      -> aq.a short[][]
/data/mod/modInfo.mid        -> aq.b short[][]
/data/script/chs.mid         -> aq.d String[]
/data/script/npcDialog.mid   -> game.k.N String[]
/data/script/db.mid          -> aq.c short[][][]
/data/tex/tex_0..3           -> aq.e int[][]
/data/tex/bk                 -> aq.f Image
```

Important correction: `aq.a` la bang sprite index. Row format da duoc source
xac nhan qua `d.java:32-40`:

```text
aq.a[spriteIndex][0]    = sprId
aq.a[spriteIndex][1..]  = imgIds
```

## 2. Binary Table Readers

Source anchors:

- `modules/source_code/decoded/decompiled_source_cfr/ae.java:47`
- `modules/source_code/decoded/decompiled_source_cfr/ae.java:68`
- `modules/source_code/decoded/decompiled_source_cfr/ae.java:85`
- `modules/source_code/decoded/decompiled_source_cfr/ae.java:124`
- `modules/source_code/decoded/decompiled_source_cfr/ae.java:170`

`ae` la helper dung khap runtime:

- `ae.a(byte[], path)` doc resource vao buffer raw (`ae.java:47-66`).
- `ae.a(byte[], offsetRef)` doc flat short table co header row/width
  (`ae.java:68-83`).
- `ae.b(byte[], offsetRef)` doc jagged short table co row count + width
  (`ae.java:85-103`).
- `ae.a(InputStream)` doc `short[][]` tu stream, dung cho `sprite.mid`
  (`ae.java:124-145`).
- `ae.c(InputStream)` doc `String[][]`, dung cho `chs`, `npcDialog`, task text
  (`ae.java:170-208`).

Implication: khi rebuild loader, uu tien parser cau truc nay. Khong nen cat
chuoi/byte ad hoc neu da co format doc duoc tu source.

## 3. Sprite Binding And Rendering

Source anchors:

- `modules/source_code/decoded/decompiled_source_cfr/f.java:15`
- `modules/source_code/decoded/decompiled_source_cfr/d.java:32`
- `modules/source_code/decoded/decompiled_source_cfr/aa.java:13`
- `modules/source_code/decoded/decompiled_source_cfr/am.java:20`
- `modules/source_code/decoded/decompiled_source_cfr/d.java:327`
- `modules/source_code/decoded/decompiled_source_cfr/d.java:379`

Binding flow:

```text
game actor / temp effect / UI sprite
  -> root f.a(spriteIndex, extendedFlag)
  -> root d.a(spriteIndex, extendedFlag)
       m[] = aq.a[spriteIndex][1..]      // img ids
       k[] = am.a(imgId)                 // load /data/img/img_<id>
       l   = aa.a(aq.a[spriteIndex][0])  // load /data/spr/spr_<sprId>_all(r)
```

`am.a(imgId)` lazy-loads `/data/img/img_<id>` and increments refcount
(`am.java:20-27`). `am.b(imgId)` decrements but keeps image reference;
`am.c(imgId)` can null image when refcount reaches zero (`am.java:29-49`).

`aa.a(sprId)` lazy-loads `/data/spr/spr_<sprId>_all(r)` into container `o`:

- `o.b`: frame rectangles / source rect table.
- `o.e`: cell composition rows.
- `o.f`: animation rows.
- `o.d` and `o.c`: remapped collision/hit related tables.

Special case `sprId 86..185` is source-backed and must be preserved:
`aa.java:21-31` synthesizes five cell variants using offset table
`{0,10,3,7,-10}` and replaces animation rows with hardcoded `aa.c`.

Draw flow:

```text
root f.a(Graphics, camX, camY)
  -> if visible, root d.a(Graphics, x-camX, y-camY, dir)
  -> choose current animation frame/cell
  -> for each cell part:
       frameId, dx, dy, transform
       frame rect = o.b[frameId * 5 .. frameId * 5 + 4]
       Graphics.drawRegion(imageSlot, sx, sy, w, h, transform, x, y, anchor)
```

Transform direction tables live in `d.java:12-19`; draw direction branches are
in `d.java:335-376`; final `drawRegion` call is `d.java:379-380`.

Port rule: actor record sprite value and effect sprite value are sprite table
indexes. They are not direct image ids and not necessarily spr ids.

## 4. Room Load And Actor Record Parse

Source anchors:

- `modules/source_code/decoded/decompiled_source_cfr/game/k.java:312`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java:324`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java:337`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java:377`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java:526`

Room load opens `/data/event/scene_<world>.mid` (`game.k.java:312-323`).

High-level parse:

```text
read room size table
skip bytes for previous rooms
read room string pool
read room display name
read map/room metadata
read actor count
for each actor:
  read actor record by type
  game.a.a(record, actorIndex)
read actor name/dialog name table into game.k.aj
read event count
game.c.a(stream, world, room, eventCount, stringPool)
```

Actor common fields in `game.k.java:382-391`:

```text
record[0] = actor type byte
record[1] = sprite table index
record[2] = direction/state byte-ish value
record[3] = world x
record[4] = world y
record[5] = visible flag
record[6] = actor behavior/subtype byte
```

Then extra fields depend on `record[0]`:

- `type 0`: active/NPC actor, reads `7..12` (`game.k.java:429-435`).
- `type 1`: transition/door-like actor, reads `7..10`
  (`game.k.java:393-397`).
- `type 2`: static/flag actor, reads `7` and maybe `8..12`
  (`game.k.java:417-424`).
- `type 3`: special actor, reads `7..11` (`game.k.java:489-494`).

Persistence:

- `game.k.ag[linearRoom][actor][0..2]` persists actor direction/visible/current
  animation-ish state.
- `game.k.ah[linearRoom][actor][0..1]` persists actor coordinates.
- Linear room index is `game.k.l[scene] + room`.

Implication: if rebuild reloads a room but ignores `ag/ah`, actor state after
events will not match source.

## 5. Actor Init

Source anchors:

- `modules/source_code/decoded/decompiled_source_cfr/game/a.java:45`
- `modules/source_code/decoded/decompiled_source_cfr/game/a.java:64`
- `modules/source_code/decoded/decompiled_source_cfr/game/a.java:166`
- `modules/source_code/decoded/decompiled_source_cfr/game/a.java:672`

`game.a` extends root `f`, so every room actor owns root sprite animator `d`
through inherited `f.a`.

Init core in `game.a.a(short[], int)`:

```text
I = actorIndex
t = record[0]                 // actor type
this.a.a(record[1], false)    // bind sprite index through root d/aq/aa/am
this.a.c()                    // reset animation cursor
v = record[6]                 // behavior/subtype
d(record[2])                  // apply direction/action mapping
i = record[3]
j = record[4]
visible = record[5] == 1
```

For `type 0`, additional source-backed behavior:

- `s = record[7]`
- `w = record[8]`
- if `w != 0`, create attached `f` using sprite index `259` and animation `w`
  above actor (`game.a.java:69-76`).
- `u = record[9] != 0`
- `x = record[11]`, `y = record[12]`
- some behavior-specific facing/action rewrites for `v == 1/2/3/12/13`.
- if `v` is interactive-ish, create child marker sprite `337`
  (`game.a.java:101-112`).

For `type 1`, transition actors can be added to `game.k.e` if visible and type
range matches (`game.a.java:115-127`).

Actor tick calls inherited `super.d()` plus attached effect updates:
`game.a.java:672-687`.

## 6. Map Renderer Is Root `j`, Text Renderer Is `game.j`

Source anchors:

- `modules/source_code/decoded/decompiled_source_cfr/j.java:79`
- `modules/source_code/decoded/decompiled_source_cfr/game/j.java:75`

Root `j.java`:

- map/camera/tile renderer.
- `j.a(mapId)` opens `/data/map/map_<id>.mid` (`j.java:79-89`).
- Loads map dimension, mod id, layer count, tile arrays (`j.java:90-136`).
- Uses `aq.b[modId]` image ids and `am` for tiles later.

`game.j.java`:

- text/cutscene renderer for event opcodes `1`, `48`, `51`.
- `game.c` owns `private j D = game.j.a()` (`game.c.java:39`).
- Its renderer/tick remains `PARTIAL` because CFR damage exists around render
  methods. Do not claim pixel-perfect until validated.

## 7. Event Parse: `p` Timeline And `ad` Command

Source anchors:

- `modules/source_code/decoded/decompiled_source_cfr/game/c.java:337`
- `modules/source_code/decoded/decompiled_source_cfr/p.java:15`
- `modules/source_code/decoded/decompiled_source_cfr/ad.java:11`

`game.k` passes event section to `game.c.a(stream, scene, room, count, stringPool)`
at `game.k.java:526-529`.

`game.c.a(...)`:

```text
this.a = new p[eventCount]
this.z = new Vector()         // active events
C = new Vector()              // temp effect sprites
if state table for room missing, allocate byte[eventCount]
for each event id:
  p.a(stream, eventId, packedSceneRoom, stringPool)
  p.state = persisted state byte
this.i()                      // quest/interaction marker refresh
```

`p` structure:

- `b`: event id.
- `f`: packed scene-room.
- `a[]`: all `ad` commands.
- `c`: Vector of commands.
- `d`: current command pointer.
- `e`: event state.

`ad` command format:

```text
short opcode
byte totalParamCount
byte numericParamCount
short[numericParamCount]
String[totalParamCount - numericParamCount] via string pool indexes
```

This is why decoded JSON must preserve both numeric args and string args.

## 8. Event Tick And Trigger Scan

Source anchors:

- `modules/source_code/decoded/decompiled_source_cfr/game/c.java:140`
- `modules/source_code/decoded/decompiled_source_cfr/game/c.java:276`
- `modules/source_code/decoded/decompiled_source_cfr/game/c.java:282`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java:1714`

World tick in `game.k.b()` delegates into `M` where `M = game.c` in free-world
state; source around `game.k.java:1714+` is CFR-damaged but it clearly handles
input, player/actor interaction, UI state, and event handoff.

`game.c.b()`:

```text
if no events, return
b.a().b()        // full-screen/effect manager tick
D.d()            // game.j text tick
for each event p:
  if state is idle-ish (0 or 4):
    inspect first command p.d()
    if trigger condition true:
      A = event id
      p.commandPointer = 0
      activeVector z.add(p)
      p.state = 1
n()              // execute active events
```

Trigger-only or trigger-like first opcodes include:

- `13`: player in rectangle.
- `15`: previous event complete/persisted.
- `16/43/44/69/79`: interaction with selected actor.
- `57/59/61/73/75/78/86`: actor/path/inventory/task conditions.

Do not treat every opcode as command-only. Some opcodes are primarily triggers
when they appear as first command.

## 9. Event Executor And Wait Rule

Source anchors:

- `modules/source_code/decoded/decompiled_source_cfr/game/c.java:421`
- `modules/source_code/decoded/decompiled_source_cfr/game/c.java:1617`
- `modules/source_code/decoded/decompiled_source_cfr/game/c.java:1621`
- `modules/source_code/decoded/decompiled_source_cfr/p.java:56`

`game.c.n()` loops active event vector `z`, reads current `ad`, and switches on
opcode. CFR labels are damaged, but tail behavior is visible:

- when a command finishes normally, `p.e()` advances command pointer.
- when state is `5`, command is waiting/running and must be revisited next tick.
- state `6` behaves like blocked/paused.
- when event state becomes `3` or `4`, active event is removed and persisted
  into `game.c.b[linearRoom][eventId]`.

Branch opcodes often set command pointer to `target - 2`; this only makes sense
because the common tail advances once afterward.

Manual script implication: a manual port is not faithful if it ignores waits.
For any opcode that sets state `5`, source expects repeated ticks until its
completion condition flips.

## 10. Selected Opcode Destinations

Source anchors:

- `modules/source_code/decoded/decompiled_source_cfr/game/c.java:430`
- `modules/source_code/decoded/decompiled_source_cfr/game/c.java:495`
- `modules/source_code/decoded/decompiled_source_cfr/game/c.java:562`
- `modules/source_code/decoded/decompiled_source_cfr/game/c.java:607`
- `modules/source_code/decoded/decompiled_source_cfr/game/c.java:840`
- `modules/source_code/decoded/decompiled_source_cfr/game/c.java:994`
- `modules/source_code/decoded/decompiled_source_cfr/game/c.java:1142`

`opcode 1`: text/cutscene text via `game.j`.

- Starts `b.a().c(0,9)` effect/channel.
- Calls `D.a(x,y)`, `D.a(mode,text,style)`, `D.a(true)`.
- Waits until `game.j` completes and input allows continue.

`opcode 5`: attached effect sprite.

- Creates new root `f`.
- Loads sprite index `259`.
- Applies animation id from args.
- Places at player or actor, sometimes absolute coordinate.
- Adds to `game.c.C`.

`opcode 9`: full-screen/global effect via root `b.java`.

- Dispatches many effect shapes by `args[0]`: `10/12/13/14/15/16/17/...`.
- Must not be approximated as one generic fade if pixel parity matters.

`opcode 10`: timed actor/player action/movement.

- On first tick, parses actor list strings, sets direction/action/duration.
- Sets event state `5`.
- On later ticks, decrements per-actor timers and restores idle/action state.
- Uses actor/player helpers (`game.a`, `game.g`, root `n/f/d`).

`opcode 22`: prepare transition/camera metadata.

```text
game.k.x = true
game.k.w = args[1]
game.k.a().h = args[2]
game.k.a().i = args[3]
game.k.B = args[4]
game.k.C = args[5]
game.k.a().j = -1
```

`opcode 34`: counter/timed value.

```text
first tick: N=args[2], O=args[3], B=args[4], state=5
wait tick: --B; N -= O; complete when B <= 0
```

No visual effect is proven by this case alone.

`opcode 48`: special text box.

```text
D.a(args[1], args[2])          // x,y
D.a(args[0]/10 - 1, text, args[0]%10)
if args[5] == 1: D.a(true)
D.b(args[3], args[4])          // w,h
state = 5
```

Thus for record args `[10,20,220,200,40,1]`, source-backed box is
`x=20, y=220, w=200, h=40`.

## 11. Render Order In World

Source anchors:

- `modules/source_code/decoded/decompiled_source_cfr/game/k.java:636`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java:2346`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java:2361`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java:2374`

Room init adds all actors into display list `t` through `W()`:

```text
for each actor:
  actor.f()       // advance/check sprite state/culling
  displayList.a(actor)
```

Render order in `game.k` world paint:

```text
displayList/map/actors
root b.java screen effect channel
battle/world special overlays if active
game.c.a(graphics)     // temp effect markers
UI manager ab
optional overlay n
floating text/icons
event/menu UI game.c/game.h layer
```

Exact map layer ordering is inside root display list `t` and root `j`; this is
still `PARTIAL` for pixel parity because copyArea/camera optimizations need
separate validation.

## 12. Movement Base `n.java`

Source anchors:

- `modules/source_code/decoded/decompiled_source_cfr/n.java:26`
- `modules/source_code/decoded/decompiled_source_cfr/n.java:71`
- `modules/source_code/decoded/decompiled_source_cfr/n.java:101`
- `modules/source_code/decoded/decompiled_source_cfr/n.java:116`
- `modules/source_code/decoded/decompiled_source_cfr/n.java:144`

`n` is the movement/entity base used under root `f`, `game.a`, and `game.g`.
It is not a renderer by itself.

Important fields:

```text
c[] / d[]  = small state/action arrays; g() copies c -> d
e/f/g      = visibility / active / drawable-ish flags via a/b/c(boolean)
h          = movement/action state
i,j        = world x,y
m,n,o      = direction/action bytes
p          = parent/follow target
q,r,s      = extra display/order fields; s is display bucket for t.java
```

Movement helpers:

- `b(x,y)` sets absolute world position (`n.java:71-74`).
- `d(dx)` and `e(dy)` apply direct deltas (`n.java:88-94`).
- `a(speed,targetX,targetY)` moves toward target by normalized distance and
  snaps when remaining distance is below speed (`n.java:101-114`).
- `c(byte)` enables follower/trail mode based on parent `p`, storing the last
  10 positions/actions (`n.java:144-180`).
- `a(d,d)` updates follower position/action from the parent when trail mode is
  active (`n.java:116-142`).

Port implication for opcode `10/29/30`: current speed-duration shortcuts are
only `APPROX` until they use these movement helpers plus actor/game.g action
state and collision logic.

## 13. Display List `t.java`

Source anchors:

- `modules/source_code/decoded/decompiled_source_cfr/t.java:29`
- `modules/source_code/decoded/decompiled_source_cfr/t.java:78`
- `modules/source_code/decoded/decompiled_source_cfr/t.java:111`

`t` owns world render/update ordering. It has three vectors:

```text
n.s == 0 -> b vector
n.s == 1 -> c vector
n.s == 2 -> d vector
```

Per tick (`t.java:78-109`):

```text
camera.e.d()
map.a(camera.i, camera.j)
map.c()
tick b vector
sort c vector by world y (j)
tick c vector, including attached G/H effect sprites
tick d vector
```

World draw (`t.java:111-196`):

```text
map layer 1
map layer 2
d vector actors/effects
c vector actors sorted by Y
special player/child ordering depending on tile P[2]
map layer 3
b vector actors/effects
```

This confirms actor visual parity depends on:

- correct `n.s` bucket per actor/effect,
- correct camera from `ai`,
- Y-sort in bucket `c`,
- attached sprites `game.a.G/H` and `root f.b` drawn in the right branch.

`t.b(Graphics)` is a separate simplified/filtered draw path for actors with
`v == 0`, then fills background texture `aq.f` (`t.java:198-222`).

## 14. Corrected World Init Note

Source anchors:

- `modules/source_code/decoded/decompiled_source_cfr/game/k.java:122`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java:129`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java:285`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java:1144`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java:2744`

`game.k.a()` creates singleton world controller. Constructor initializes static
helpers and render roots:

```text
game.j.a()       // text renderer singleton exists
this.e = Vector
this.f/this.g = 0
this.h/this.i = 128/256
this.a = root j.a()      // map renderer
this.aa = ai.a()         // camera
this.b = new t()         // display list
```

The instance method `game.k.d()` is the heavy world/room load path, not
`game.k.i()`. It creates RMS wrappers if needed, binds `game.c` to world,
gets player model `game.g.o()`, then opens room event/map data
(`game.k.java:285-299` onward).

`game.k.i()` specifically reads booleans/byte from RMS slot `au[3]`, where
`au[3] == "PK6_RMS_RMS"` (`game.k.java:1144-1163`, `game.k.java:2744`).

## 15. Current Rebuild Consequences

Immediate conclusions for current Java rebuild:

- `SpriteAnim.SPRITE_TO_IMGS` should be treated as compatibility shortcut only.
  The correct long-term direction is a real loader for `sprite.mid` + `spr_*`
  metadata + image ids.
- Room actor tables in manual code should name `spriteIndex`, not `spriteId`,
  unless the row has already been converted to true `sprId`.
- Event script status must distinguish:
  - opcode dispatch implemented from source (`PORTED`);
  - source opcode known but engine shortcut used (`APPROX`);
  - visible placeholder (`STUB`);
  - not implemented (`MISSING`).
- Opcode wait behavior is central. A one-shot translation of commands will
  diverge on movement, text, effects, battle, UI, and transitions.
- Do not alter user-approved early intro/scene_0 flows while doing this
  structural understanding pass.

## 16. OPEN Trace Items

Need deeper source pass before full port:

- `game.k.d()` remaining room-load tail and map/resource globals.
- `game.k.b()` full world tick after CFR-damaged areas, including collision,
  random encounters, and interaction selection `game.k.u`.
- `game.g` player movement/collision over root `n` helpers.
- `root b.java` effect manager cases for opcode `9/24`.
- `game.h` UI workflow side effects for opcode `4/35/40/45/46/49/84`.
- `game.d/game.b/game.g` battle path before replacing battle stubs.

Rule for next pass: each OPEN item should become either a source-backed port
task or a smaller audit doc with line refs and unresolved CFR/bytecode risks.
