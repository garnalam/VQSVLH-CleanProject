# Source Code File Role Map And Execution Flow

Purpose: lap ban do tung file trong `modules/source_code/decoded/decompiled_source_cfr`
va noi ro cac luong quan trong nhu sprite, actor, event opcode, UI, battle.

This is a working map. `VERIFIED` means role is source/data-backed enough to
use as navigation. It does not mean every branch is implemented or pixel-perfect.

## Package Groups

| Group | Files | Meaning |
|---|---:|---|
| root/default package | 44 | Runtime base, resource, renderer, UI primitives, effect manager, SMS helpers, event holders. |
| `game` package | 12 | Main game states: MIDlet/canvas/state manager/world/event/battle/player/UI/text. |
| `a` package | 10 | Lua-like bytecode VM used by legacy SMS/payment path. |
| `lavax.wireless.messaging` | 1 | Local/stub MessageConnection compatibility class. |

## `game` Package Roles

| File | Role | Main dependencies / callers | Status |
|---|---|---|---|
| `game/GameMIDLet.java` | MIDP entrypoint, creates Canvas and sets display. | `game.e` | VERIFIED |
| `game/e.java` | MIDP Canvas and main loop; forwards key/pointer input; calls update/render on `game.i`. | `game.i`, `an`, `ap` | VERIFIED |
| `game/i.java` | Top-level state manager: boot/logo/menu/world/battle/loading/music. | `game.k`, `game.d`, `game.f`, `b`, `am/aa/aq` | VERIFIED/PARTIAL |
| `game/f.java` | Menu/title state child under top-level manager. | `game.h`, `game.g`, `game.k` | PARTIAL |
| `game/k.java` | World controller: room load, map, actors, player, event VM, UI state, save/load, random battle. | `game.c`, `game.a`, `game.g`, root `j/t/ai`, `ar` | VERIFIED/PARTIAL |
| `game/c.java` | Event VM: trigger scan, active event vector, opcode executor, event text/effects, transition and battle entry. | `p`, `ad`, `game.k`, `game.j`, `b`, `game.d`, `game.h` | VERIFIED/PARTIAL |
| `game/a.java` | Room actor wrapper over root `f`: actor record init, actor subtype behavior, transition actor logic. | root `f/n/d`, `game.k`, map root `j` | VERIFIED/PARTIAL |
| `game/g.java` | Player/world gameplay model: movement actor plus inventory, pets, money, flags, item helpers. | `game.b`, `game.k`, `game.h` | VERIFIED/PARTIAL |
| `game/b.java` | Pet/battle unit model: stats, skills, buff/debuff, damage formula, catch/item validation, serialization. | `game.d`, `game.g`, `aq.c` | VERIFIED for battle formula; PARTIAL wider domain names |
| `game/d.java` | Battle engine state machine: battle init, turn order, command states, catch, EXP, level up, return world. | `game.b`, `game.h`, `game.i`, battle scripts | VERIFIED/PARTIAL |
| `game/h.java` | UI workflow controller: opens `.ui`, writes widgets, handles menu/bag/shop/task/pet/battle/SMS inputs and side effects. | `ab/ao`, `game.g`, `game.d`, `game.k`, `an` | PARTIAL but central |
| `game/j.java` | Text/cutscene renderer for event opcodes `1/48/51`, not map renderer. | `game.c`, bitmap font `s` | VERIFIED/PARTIAL |

## Root Package Roles

| File | Role | Main dependencies / callers | Status |
|---|---|---|---|
| `an.java` | Abstract base state; extends input `ap`; screen/timing/font/loading helpers; SMS reward state. | `game.i/k/d/f`, `game.h`, SMS callbacks | VERIFIED/PARTIAL |
| `ap.java` | Input bitmask base: held/pressed/released masks, child input receiver, key mapping. | `an`, all states | VERIFIED |
| `as.java` | TimerTask repaint pulse during loading. | `an.s()` | VERIFIED |
| `aq.java` | Global metadata loader: `sprite.mid`, `modInfo.mid`, `db.mid`, `chs.mid`, textures. | `game.i.d()`, sprite/map/battle/UI | VERIFIED |
| `ae.java` | Binary/image/text/geometry helper: table readers, stream load, image create, collision helpers. | Almost all resource/render/gameplay code | VERIFIED/PARTIAL |
| `aj.java` | DataInputStream wrapper/resource helper used by stream loading. | `ae`, resource load paths | PARTIAL |
| `am.java` | Image cache/refcount for `/data/img/img_<id>.mid`. | root `d`, map `j`, UI/effects | VERIFIED |
| `aa.java` | Sprite metadata cache/refcount for `/data/spr/spr_<sprId>_all(r)`. | root `d` | VERIFIED |
| `o.java` | Passive sprite metadata container: frame rect, cells, anim, hit/collision, extended flag. | `aa`, root `d` | VERIFIED |
| `d.java` | Root sprite animator/renderer: binds sprite index through `aq.a`, advances anim, draws cells via MIDP `drawRegion`. | root `f`, UI `m`, effects `l/ah` | VERIFIED/PARTIAL pixel |
| `f.java` | Sprite entity base over `n`: binds root `d`, draw/update/culling, child sprite. | `game.a`, `game.g`, temp effects | VERIFIED |
| `n.java` | Movement/entity base: position, facing, parent/follower trail, move-to-target helpers. | `f`, `ai`, `ah` | VERIFIED |
| `ai.java` | Camera target/helper singleton, modes fixed/follow; mode 2 unused/damaged. | `game.k`, event opcode 11, effect id 11 | VERIFIED/PARTIAL |
| `j.java` | Root map renderer, not `game.j`: loads map/mod/tile layers, camera/copyArea, collision tile query. | `game.k`, display list `t` | VERIFIED/PARTIAL pixel |
| `t.java` | Display list/draw order: map layers and actor Y-sort. | `game.k` | VERIFIED |
| `s.java` | Bitmap font loader/renderer for `/font.bin`. | `game.j`, UI text `y/k` | VERIFIED |
| `y.java` | Thin UI text render wrapper around `s`. | `ab/ao/k` | VERIFIED |
| `b.java` | Full-screen effect/compositor manager: fades, flash, circle, texture, battle transitions, UI overlay. | `game.i`, `game.c`, `game.d`, `game.k` | VERIFIED/PARTIAL pixel |
| `ah.java` | Effect actor/RGB sprite effect, separate from full-screen manager `b.java`. | `b`, world/battle effects | VERIFIED/PARTIAL pixel |
| `e.java` | RGB buffer struct for effects. `a()` clone has CFR bug (`new e().a`) to fix by intent when porting. | `l`, `ah`, `b` | VERIFIED/PARTIAL |
| `l.java` | RGB/image effect helper: image-to-buffer, scale, alpha, tint, blend. | `b`, `ah`, UI/effects | VERIFIED/PARTIAL |
| `g.java` | Small int table/counter buffer, likely effect helper state. | `b/ah` context | LOW/PARTIAL |
| `ab.java` | UI manager/cache/stack for `.ui` screens. | `game.h`, `game.c`, `b` effect 8 | VERIFIED/PARTIAL |
| `ao.java` | `.ui` parser/runtime/focus controller for one UI instance. | `ab`, widgets `al/af/ac` | VERIFIED/PARTIAL |
| `w.java` | Common UI widget interface. | `al`, `af`, `ac` | VERIFIED |
| `al.java` | UI container widget, children, anchoring, styles/navigation. | `ao` | VERIFIED/PARTIAL |
| `af.java` | UI visual/text/image widget. | `ao`, renderer payload `k` | VERIFIED/PARTIAL |
| `ac.java` | UI grid/list/selectable widget. | `ao`, cell item `a` | VERIFIED/PARTIAL |
| `a.java` | UI cell/navigation item for `ac`, optional sprite/image `m`, rect/neighbor metadata. | `ac` | VERIFIED |
| `ak.java` | Plain rectangle data for UI placement. | `m/k/z` | VERIFIED |
| `k.java` | UI visual payload renderer: text, fill/border, icon/sprite, progress text. | `af`, `z`, `m`, `y` | VERIFIED/PARTIAL |
| `m.java` | UI sprite/image wrapper around root sprite renderer `d`. | `k`, root `a` cell item | VERIFIED |
| `z.java` | UI style/list repeater and dynamic slot binding. CFR-risky dynamic binding. | `al/af/ac` | PARTIAL |
| `p.java` | Event timeline holder: event id, state, command vector, command pointer. | `game.c` | VERIFIED |
| `ad.java` | Event command holder: opcode, numeric params, string params resolved from scene string pool. | `p`, `game.c` | VERIFIED |
| `ar.java` | RMS record wrapper with primary/extra payload handling. | `game.k` save/load | VERIFIED/PARTIAL |
| `c.java` | Payment callback interface `a(boolean)`. | `q/an` path | VERIFIED |
| `x.java` | Payment callback interface `b(boolean)`. | `ag/an` path | VERIFIED |
| `q.java` | Legacy/payment Canvas running VM script `/data/event/scene_13.mib`; no visible caller in current source. | `a/*`, SMS provider helpers | PARTIAL/OPTIONAL |
| `r.java` | Adapter from VM native callback into `q`. | `q`, `a.b` | VERIFIED |
| `u.java` | SMS provider/config decoder from app property or `/l2.bin`; builds destination/body. | `q`, payment VM | PARTIAL/OPTIONAL |
| `v.java` | Payment/SMS mutable state model for `q`: counts, destination/body config. | `q/u` | VERIFIED/PARTIAL |
| `h.java` | Simple logger storing throwable/string pairs. | Misc exception paths | VERIFIED/LOW |
| `i.java` | UI callback interface `a(int[])`. | `ao`, `game.h` | VERIFIED |

## `a` Package VM Roles

This package is not the normal UI/runtime. It is a Lua-like VM used by the
legacy SMS/payment Canvas.

| File | Role | Status |
|---|---|---|
| `a/a.java` | Built-in/native function table: SMS send/open, properties, random/assert-like helpers. | VERIFIED/PARTIAL |
| `a/b.java` | Native callback interface. | VERIFIED |
| `a/c.java` | VM call frame / register window. | VERIFIED/PARTIAL |
| `a/d.java` | Closure/function wrapper. | VERIFIED/PARTIAL |
| `a/e.java` | VM RuntimeException. | VERIFIED |
| `a/f.java` | Bytecode/function loader, reads `scene_13.mib` function constants and instructions. | VERIFIED |
| `a/g.java` | VM interpreter/executor. | VERIFIED/PARTIAL |
| `a/h.java` | Table/global environment. CFR-damaged method remains risky. | VERIFIED/PARTIAL |
| `a/i.java` | VM thread/stack/call owner. | VERIFIED |
| `a/j.java` | Upvalue/reference cell. | PARTIAL |

## Sprite Execution Flow

### 1. Static data boot

```text
game.i.d()
  -> am.a()   // image cache arrays
  -> aa.a()   // sprite metadata cache array
  -> aq.a()   // loads /data/script/sprite.mid into aq.a
```

### 2. Actor sprite binding

```text
game.k.d() room load
  -> read actor record short[]
  -> game.a.a(record, actorIndex)
       record[1] = sprite table index
       this.a.a(record[1], false)        // inherited root f -> root d

root f.a(spriteIndex, extendedFlag)
  -> root d.a(spriteIndex, extendedFlag)
       imgIds = aq.a[spriteIndex][1..]
       images = am.a(imgId)
       sprId  = aq.a[spriteIndex][0]
       metadata = aa.a(sprId)
```

### 3. Sprite binary metadata load

```text
aa.a(sprId)
  -> /data/spr/spr_<sprId>_all(r)
  -> frame rect flat table o.b
  -> cell composition o.e
  -> animation rows o.f
  -> hit/collision remapped tables o.d / o.c

special sprId 86..185:
  -> synthesize 5 cell variants with offset [0,10,3,7,-10]
  -> replace animation with hardcoded default rows
```

### 4. Per tick / per draw

```text
actor/game.g/temp effect
  -> root f.a()              // advance root d animation
  -> root f.a(Graphics, camX, camY)
       root d.a(Graphics, x, y, dir)
          current anim row -> current cell id
          cell parts [frameId, offsetX, offsetY, transform]
          frame rect [imageSlot, sx, sy, w, h]
          Graphics.drawRegion(image[imageSlot], sx, sy, w, h, transform, ...)
```

### Important sprite rules

- Actor records and UI sprites use sprite table index, not image id.
- `aq.a[index][0]` is `sprId`; `aq.a[index][1..]` are image ids.
- Missing mapping/resource should render blank/skip safely and be marked clearly.
- Do not invent `SPRITE_TO_IMGS`; use `sprite.mid` evidence.

## Event Opcode Execution Flow

### 1. Room event parse

```text
game.k.d()
  -> open /data/event/scene_<world>.mid
  -> skip to room
  -> read string pool, room name, map id, actors
  -> game.c.a(stream, world, room, eventCount, stringPool)
       p.a(stream, eventId, packedWorldRoom, stringPool)
          ad.a(stream, stringPool)
```

`ad` command format:

```text
short opcode
byte totalParamCount
byte shortParamCount
short[] numericParams
String[] stringParams
```

### 2. Event tick

```text
game.k.b() free/world tick
  -> M.b() where M = game.c

game.c.b()
  -> b.a().b()       // effect manager tick
  -> D.d()           // game.j text tick
  -> scan idle events by first command trigger
  -> add triggered p to active Vector z
  -> n()             // opcode executor
```

### 3. VM advance/persist rule

```text
game.c.n()
  -> switch current ad.opcode
  -> if event state != 5 and != 6:
       p.e()     // command pointer advances and wraps
  -> if event state == 3 or 4:
       remove active event
       persist game.c.b[roomLinearIndex][eventId] = state
```

Branch opcodes often write `target - 2`, because the common tail will advance once.

### 4. Opcode groups and where they run

| Opcode group | Goes into | Notes |
|---|---|---|
| `1/48/51` | `game.j` text/cutscene renderer | Mode 0 for current data. Opcode `48` uses x/y/w/h from args. |
| `4/35/40/45/46/49/84` | `game.h` UI workflow | Dialog, choice, save, task tips, formatted dialog. |
| `2/3/7/8/10/29/30/56/60/82` | `game.a`, `game.g`, root `f/n/d` | Actor/player visibility, animation, movement, persisted state. |
| `5/72` | root `f` temp sprite using sprite 259 or params | Actor/player attached visual effects. |
| `9/24` | root `b.java` effect manager | Full-screen effects, flash, circle, texture, shake. |
| `11` | `ai` camera helper | Fixed target or actor/player follow. |
| `6/21/22/23/76/77` | `game.k` + `game.i` world transition/event persistence | `6` switches top state to `game.i` state `22`. |
| `17/18/19/20/31/36/50/53/63/80/87/88` | `game.g`, `game.b`, UI popups | Inventory, pet, currency, task/progression mutations. |
| `32/37/54/67/52/47` | `game.d` battle setup/branch flags | `32` captures screen and switches `game.i` to battle load state `12`. |
| Trigger-only `15/43/44/57/59/61/69/73/75/78/79/86` | `game.c.b()` trigger scan | Decide whether idle event becomes active. |

## Sprite-Related Event Opcodes

These are the opcodes most relevant to "sprite/opcode" behavior:

| Opcode | Sprite/actor effect | Source path |
|---:|---|---|
| `2` | Show/set active actor or player sprite state. | `game.c.n()` -> `game.a.b/d/c()` or `game.g` |
| `3` | Hide/deactivate actor or player. | `game.c.n()` -> `game.a.d()` / `game.g.d()` |
| `5` | Spawn attached effect sprite `259`, animation from params. | `game.c.n()` -> new root `f` -> root `d` |
| `7` | Set actor/player animation/action, wait animation completion. | `game.c.n()` -> `game.a.d()` / root `d.e/f` |
| `10` | Timed actor/player action/movement, then restore state. | `game.c.n()` -> actor/player movement helpers |
| `29` | Per-tick delta movement. | `game.c.n()` -> `n.d/e` movement |
| `30` | Absolute path movement from string arrays. | `game.c.n()` -> actor/player position path |
| `56` | Show/hide listed actors and persist visible/state. | `game.c.n()` -> `game.k.a(actor, field, value, true)` |
| `60` | Set listed actor state/animation, wait until finished. | `game.c.n()` -> `game.a` / root `d` |
| `64` | Spawn/remove world helper sprite attached to player/actor. | `game.c.n()` -> `game.k.a(sprite)` / `game.k.g()` |
| `72` | Spawn one or more temp sprite effects. | `game.c.n()` -> temp visual list |

Do not confuse these with sprite binary animation rows. Event opcodes manipulate
actors/effects; actual frame/cell drawing still goes through root `d.java`.

## Map Render Flow

```text
game.k.d()
  -> root j.a(mapId)
       map_<id>.mid -> modId/layers/tile data
       modInfo.mid  -> aq.b[modId] image ids
       mod_<id>.mid -> tile rects
       am.a(imgId)  -> tileset images

game.k render
  -> t.a(Graphics)
       map layer 1
       map layer 2
       actor layer s=2
       actor layer s=1 sorted by Y
       map layer 3
       actor layer s=0
       event/UI/effect overlays
```

Collision query:

```text
root j.b(worldX, worldY) -> tile value from layer 0
```

Used by player movement, actor collision, random encounter tile logic.

## Battle Flow Entry

```text
event opcode 37/54
  -> game.d.a().a(int[][] setup)

event opcode 32 or random encounter in game.k
  -> capture current world image into game.d.a().c
  -> set game.d.a().a/b
  -> game.i.a().a((byte)12)

game.i state 12
  -> game.d.d() load/init battle resources
  -> effect transition
  -> game.d.g()
  -> top-level state 13 delegates to game.d.b()
```

Battle logic is source/bytecode-audited, but animation script timing and some UI
edge cases still need implementation regression.

## Practical Development Rule

When adding a feature:

1. Identify data source: event JSON, script table, sprite table, UI file, DB table.
2. Identify source owner class from this map.
3. Read the source owner and caller chain.
4. Implement minimal behavior.
5. Mark status:
   - `PORTED`: source/data-backed behavior implemented.
   - `APPROX`: behavior close but engine detail missing.
   - `STUB`: visible placeholder.
   - `MISSING`: not implemented.
6. Build/check/smoke if code/resources changed.

Never substitute sprites/effects/timing by eye when a source/data table should exist.
