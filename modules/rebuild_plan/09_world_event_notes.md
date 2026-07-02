# World/Event Notes

Pham vi audit vong nay:

- `game/k.java`: world controller, room loader, world state, UI-state bridge.
- `game/c.java`: event/cutscene VM, trigger scan, opcode executor.
- `game/a.java`: actor wrapper for scene actors.
- `f.java`, `n.java`: entity/sprite base.
- `ai.java`: camera target/motion helper.
- `ah.java`: RGB/sprite effect actor.
- `p.java`, `ad.java`: event object and opcode command record. Hai class nay bat buoc phai tinh vao World/Event vi `game.c` chay tren chung.

Trang thai: PARTIAL, nhung khong con mo ve kien truc chinh.

- VERIFIED: room load flow, actor record read flow, event object format, trigger scan -> active event list -> opcode execute loop, basic camera helper, entity base.
- VERIFIED/PARTIAL: actor type behavior, world state `P`, event opcode groups.
- UNKNOWN/PENDING: ma tran day du opcode `0..88` voi y nghia tung param; mot so doan decompile trong `game.c`, `game.k`, `ai` bi CFR lam hong control flow hoac thanh `null`.

## 1. Tong Quan Van Hanh

World/Event nam tren resource/renderer:

```text
game.i state world
  -> game.k.d() load room
  -> game.k.b() tick world
  -> game.k.b(Graphics) render world
  -> game.c.b() scan trigger + execute event VM
```

Quan he module:

```text
game.k
  owns map renderer j
  owns display list t
  owns player game.g
  owns actors game.a[]
  owns event VM game.c
  owns camera ai
  owns UI handler game.h + ab

game.c
  owns p[] event records
  owns active event Vector z
  executes ad opcode commands
```

## 2. `game.k`: World Controller

`game.k` la singleton:

```text
game.k.a()
```

Fields chinh:

| Field | Meaning |
|---|---|
| `j a` | map renderer root `j.java` |
| `ai aa` | camera helper/follow target |
| `t b` | display list/render order |
| `g c` | player/gameplay entity |
| `a[] d` | actors cua room |
| `c M` | event VM |
| `game.h S` | UI/game handler |
| `ab R` | UI manager |
| `f/g` | current world/room id |
| `j` | target actor id for transition spawn, `-1` if none |
| `P/Q` | world UI/state mode current/previous |

Static world-room index:

```text
l = {0, 2, 9, 17, 25, 38, 45, 47, 60, 67, 75, 90}

roomLinearIndex = l[worldId] + roomId
```

This index is used for cached actor/event state arrays. Rebuild must preserve it.

RMS/save slots seen in static init:

```text
PK6_RMS_ACTOR
PK6_RMS_WORLD
PK6_RMS_EVENT
PK6_RMS_RMS
PK6_RMS_SMS
PK6_RMS_CNTSMS
PK6_RMS_GOLD
PK6_RMS_POKPET
PK6_RMS_CONITEM
PK6_RMS_PETBALL
```

## 3. Room Load Flow `game.k.d()`

Verified flow:

```text
game.k.d():
  init save wrappers ar[]
  M = game.c.a()
  M.a(this)
  c = game.g.o()
  ensure cached actor/event arrays ag/ah/ai
  open /data/event/scene_<f>.mid
  read room offset table
  skip to room g
  read scene string pool
  read room name
  read map id into ab
  read actor count
  read actor records into game.a[]
  read local npc/dialog string array aj
  read event count
  M.a(stream, f, g, eventCount, stringPool)
  init player/camera/display list/UI
  M.i()
  M.b()
  set world state P = 0
```

Scene file room selection:

- file path: `/data/event/scene_<worldId>.mid`
- first block is a short offset/length table;
- room id `g` selects how many bytes to skip.

Room actor cache:

| Cache | Meaning inferred |
|---|---|
| `ag[roomIndex][actorId][...]` | persistent actor direction/state/visible-ish bytes |
| `ah[roomIndex][actorId][...]` | persistent actor x/y position |
| `ai[roomIndex][2]` | room boolean flags |

This is why actor init may use saved values instead of raw scene values after first visit.

## 4. Actor Record Load

`game.k.d()` reads each actor as a short array, then calls:

```text
game.a.a(short[] record, int actorIndex)
```

Common record fields seen before type-specific tail:

```text
record[0] = actor type t
record[1] = sprite index
record[2] = initial state/direction/anim
record[3] = x
record[4] = y
record[5] = visible flag
record[6] = actor behavior/type v
```

Type-specific tails verified from loader:

```text
type 0:
  + byte record[7]
  + byte record[8]
  + byte record[9]
  + byte record[10]
  + short record[11]
  + short record[12]

type 1:
  + byte record[7]
  + short record[8]
  + short record[9]
  + short record[10]

type 2:
  + short record[7]
  if record[7] == 1:
    + byte record[8..12]

type 3:
  + byte record[7]
  + byte record[8]
  + byte record[9]
  + short record[10]
  + short record[11]
```

Important correction:

- `record[1]` is sprite index, not image id.
- Actual render resource path remains `spriteIndex -> aq.a -> spr_* + img_*`.

## 5. `game.a`: Actor Wrapper

`game.a extends f`, so actor = entity + sprite renderer + scene-specific behavior.

Important fields:

| Field | Meaning |
|---|---|
| `t` | actor record type |
| `v` | behavior/subtype |
| `w` | optional overhead effect/icon animation id |
| `x/y` | dialog/text ids or references depending actor type |
| `I` | actor index in room |
| `G/H` | attached overhead/helper `f` effects |
| `A/B` | interaction range counters for some actor types |

Key methods:

```text
a(short[] record, int actorIndex)
  bind sprite, position, visible, behavior, optional helpers.

d(byte state)
  set animation/direction/state according actor type/behavior.

o()
  per-frame actor behavior tick.

p()/q()
  interaction/path helpers for special actors.

r()/s()
  persist actor x/y/state back to game.k caches.
```

Verified behavior examples:

- `v == 1/18`: direction state maps through `state / 3` and `state % 3`.
- `v == 2/3`: random wandering/patrol-like movement, uses map collision `j.a().b(...)`.
- `v == 4/5/6/7/15`: interaction/animation behavior, can trigger reward/drop logic.
- `v == 8/9/10/12`: special movement/collision/player proximity behavior.

These are not fully named yet. For port, keep numeric behavior ids first; name them only after caller/opcode confirms them.

## 6. `n.java` And `f.java`: Entity Base

`n` stores base entity state:

| Field | Meaning |
|---|---|
| `i/j` | world x/y |
| `n` | facing/direction-like byte |
| `p` | parent/follow target |
| `e/f/g` | active/visible/renderable flags |
| `c/d` | short arrays used by higher entities |

Movement helpers:

```text
b(x,y)          set position
d(dx), e(dy)   move x/y
a(speed,x,y)   move toward target
c(byte)        enable follow trail buffer
a(d,d)         update follower trail from parent
```

`f extends n` adds sprite renderer:

```text
d a     // root sprite animator
f b     // attached child sprite/effect
```

Render/update:

```text
f.a(Graphics, camX, camY)
  if visible:
    draw sprite at i-camX, j-camY
    transform byte 1 if n == 3

f.a()
  advance animation frame

f.f()
  visible culling against camera/screen
```

## 7. Camera `ai.java`

`ai extends n`, singleton:

```text
ai.a()
```

Modes:

| Mode `a` | Meaning |
|---|---|
| `0` | move to fixed x/y target |
| `1` | follow/move to entity target `p` |
| `2` | path/table motion, decompile damaged |

API:

```text
a(speed)
  set move speed `w`

a(x, y, immediate)
  mode 0; if immediate, set i/j directly; else move toward q/r.

a(n target, immediate)
  mode 1; follow entity; if immediate, set i/j to target.

d()
  tick camera motion.

c()
  returns true when motion complete/immediate flag x true.
```

Risk:

- mode `2` in `ai.d()` has CFR `null` placeholders. Any event/caller using camera path table must be audited from original bytecode or another decompiler.

## 8. Event Format: `p.java` And `ad.java`

`p` is one event/timeline:

```text
p:
  ad[] commands
  byte b = event id
  Vector c = command list
  byte d = current command index
  byte e = event state
  int f = packed world/room id
```

Event state `p.e` seen in code:

| State | Meaning inferred from usage |
|---|---|
| `0` | idle/not started |
| `1` | active/running |
| `2` | conditional/blocked-ish active state |
| `3` | completed, removed and persisted |
| `4` | special completed/alternate persisted |
| `5` | command waiting/running |
| `6` | paused/blocked state |

`ad` is one opcode command:

```text
short opcode
byte totalParamCount
byte shortParamCount
short[] numericParams
String[] stringParams // indexes into scene string pool
```

This is the concrete event binary command format. Rebuild parser should mirror this.

## 9. `game.c`: Event VM Lifecycle

Construction:

```text
game.c():
  bind game.k and game.g
  allocate event state cache byte[127][]
  load task strings:
    /data/script/bTask.mid
    /data/script/mTask.mid
    /data/script/bqTask.mid
```

Room event parse:

```text
game.c.a(stream, worldId, roomId, eventCount, stringPool):
  create p[eventCount]
  ensure b[roomIndex] event-state cache
  for each event:
    p.a(stream, eventId, packedWorldRoom, stringPool)
    p.a(savedState)
  i() rebuild overhead trigger icons
```

Per tick:

```text
game.c.b():
  if no events return
  b.a().b()       // tick effect manager
  D.d()           // tick text/dialog renderer game.j
  scan idle events for trigger command
  add triggered p to active Vector z
  n() execute active events
```

Render/update helpers:

```text
game.c.a(Graphics)
  draw event overhead icons/effects in C and d.

game.c.c()
  tick temporary effect sprites.

game.c.b(Graphics)
  draw effect manager, attached actor effect, text/dialog renderer.
```

## 10. Event Trigger Scan

Before executing command sequences, `game.c.b()` checks the first command of idle events.

Trigger opcodes seen:

| Opcode | Trigger condition observed |
|---|---|
| `13` | player inside rectangle |
| `15` | referenced event state completed `3/4` |
| `16` | selected/interacting actor `game.k.u` |
| `43/44` | actor interaction plus condition helpers `a(ad)` / `b(ad)` |
| `57` | player parent/special actor condition |
| `59/61` | actors animation/state completion |
| `69` | selected actor id |
| `73` | inventory/item quantity condition |
| `75` | player list/state non-empty |
| `78/79/86` | event-state/actor/inventory compound conditions |

When triggered:

```text
p.b(0)       // current command index
z.add(p)     // active event list
p.a(1)       // event state active
```

## 11. Opcode Executor `game.c.n()`

`n()` executes active events by current `ad` opcode. It advances commands unless event state is waiting (`5`) or blocked (`6`).

Verified groups:

| Opcode group | Meaning |
|---|---|
| `1/48/51/84` | text/dialog through `game.j` and UI handler |
| `2/3/7/8/10/29/30` | actor/player sprite state, movement, animation, position path |
| `4/35/40/45/46/49` | UI prompt/choice/save/message interaction |
| `5/72` | temporary effect sprite(s) attached to player/actor |
| `6/22/76` | room/world transition |
| `9/24` | effect manager `b.java` effects |
| `11` | camera target/move through `ai` |
| `12` | timed wait |
| `13` | area condition |
| `17/18/19/20/31/36/50/53/87` | inventory/item/pet/reward mutation |
| `21/22/25/52/67` | global world/event flags |
| `23/38/41/42/47/65/71/74/77/80/81/83/85/88` | branch/event state/control flow |
| `32/37` | battle entry/setup through `game.d` and `game.i state 12` |
| `64` | spawn/attach world helper effect |
| `70` | switch world/UI state and wait for `game.c.e` |

Important verified opcodes:

```text
opcode 6:
  mark current event state 3
  set game.k.f/g to target world/room
  optional game.k.j target actor id
  game.i.a().a((byte)22)

opcode 9:
  route to b.java effect manager
  handles effect ids 10/12/13/14/15/16/17 specially
  waits on b.a().b or b.a().c depending effect

opcode 11:
  config ai camera target/follow
  waits until ai.a().c()

opcode 32:
  prepare battle snapshot
  set game.d.a().a/b/c
  game.i.a().a((byte)12)
```

## 12. `ah.java`: Effect Actor

`ah extends n`, not the same as `b.java` full-screen effect manager.

Role:

- builds RGB buffers from sprites/textures via `d`, `l`, `e`;
- draws transient effects with `drawRGB`;
- supports multiple effect types `u`;
- used by `b.java` effect sequence and World/Event temp visuals.

Verified:

```text
a(short[] params)
  selects effect type u
  loads sprite frames into RGB buffers
  may tint/scale/blend buffers

e()
  tick effect by type, returns false when finished

a(Graphics)
  draw effect buffer(s)
```

Risk:

- Several effect types are empty in visible decompiled branches or require parameter-specific interpretation. Port only types observed by event resources first.

## 13. World Tick `game.k.b()`

Main state `P == 0`:

```text
handle input movement/action/menu
player q()
for each actor: actor.o()
attached helper effects update
displayList.b()
random encounter / map condition checks
M.c()          // event temp effects tick
S.e()          // UI/game handler tick
M.b()          // event trigger scan + opcode execute
l()            // likely render/camera/helper update, still needs naming
```

Other `P` values are UI/menu submodes. They mostly delegate to `game.h S`.

`game.k.a(byte)` changes `P` and calls the corresponding UI setup method.

## 14. What Is Clear Enough To Port Now

Can port now:

1. `WorldController` shell matching `game.k`.
2. Room load from `/data/event/scene_<id>.mid`.
3. Actor record parser for types `0..3`.
4. Entity base `n/f`.
5. Event object parser `p/ad`.
6. Event state cache indexed by `game.k.l[world]+room`.
7. Camera helper `ai` modes `0/1`.
8. First opcode subset needed for intro/cutscene/world transition:
   - text/dialog group,
   - actor visibility/state,
   - actor movement/wait,
   - camera follow/move,
   - effect opcode 9,
   - world transition opcode 6.

Do not port yet as "complete":

- all opcodes `0..88`;
- `ai` mode 2;
- every actor behavior `v`;
- random encounter/petArea logic;
- save/RMS side effects for event persistence.

## 15. Required Next Audit For Full Rebuild

Before coding full World/Event, create a dedicated opcode matrix:

```text
10_world_event_opcode_matrix.md
  for each opcode 0..88:
    trigger or command?
    short params count/meaning
    string params count/meaning
    blocking behavior
    mutates actor/player/world/event/save?
    calls UI/effect/battle/world transition?
    port priority
```

Audit update:

- Matrix da duoc tao tai [10_world_event_opcode_matrix.md](10_world_event_opcode_matrix.md).
- Trang thai van la PARTIAL vi nhieu opcode can doi chieu them `game.g`, `game.h`, `game.d` va decoded event resources truoc khi dat ten domain cuoi cung.

Also required:

- map actor behavior id `v` by scanning scene JSON/resources;
- decode real event resources for common opcodes and compare params;
- inspect `game.h` callbacks because many opcodes wait on UI handler state;
- inspect `game.g` for player/inventory mutation opcodes;
- inspect `game.d` for battle-entry opcodes `32/37`.

## 16. Honest Status

This module is not "done" yet.

What is done:

- architecture and runtime flow are clear;
- room/actor/event command data structures are clear;
- the route from event opcode to effect/camera/world transition is clear.

What remains:

- full opcode-by-opcode table;
- exact naming of all actor behavior `v`;
- exact side effects for gameplay/inventory/battle/save opcodes.

So this is safe as foundation for the next audit step, but not safe yet for a full game port without the opcode matrix.
