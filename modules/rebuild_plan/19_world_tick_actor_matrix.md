# 19. World Tick + Actor Behavior Matrix

Muc tieu: dong dan cum World/Actor truoc khi rebuild full game:

- `game.k.b()` world tick va state `P`.
- Random encounter / battle transition tu world.
- Room/world transition fields.
- `game.a`, `f`, `n`, `ai`, `ah` actor/motion/camera/effect behavior.

Nguon da doc:

- `source_code/decoded/decompiled_source_cfr/game/k.java`
- `source_code/decoded/decompiled_source_cfr/game/a.java`
- `source_code/decoded/decompiled_source_cfr/f.java`
- `source_code/decoded/decompiled_source_cfr/n.java`
- `source_code/decoded/decompiled_source_cfr/ai.java`
- `source_code/decoded/decompiled_source_cfr/ah.java`
- `source_code/decoded/bytecode_javap/game__k.javap.txt`
- `source_code/decoded/bytecode_javap/game__a.javap.txt`
- `source_code/decoded/bytecode_javap/ai.javap.txt`

Trang thai: VERIFIED/PARTIAL, implementation-ready cho world/actor skeleton. Khung world tick, state `P`, random encounter, transition fields, actor follow/move/collision, va actor subtype used-by-data da du ro de port. Chua du de noi pixel/gameplay-perfect vi `ah` can visual validation va `game.h` UI delegate states van nam o file UI.

## 1. Core World Objects

| Original | Rebuild name | Meaning | Status |
| --- | --- | --- | --- |
| `game.k` | `WorldController` | World/map room controller, owns map renderer, player, actors, event VM, UI facade, save. | VERIFIED/PARTIAL |
| `game.k.P` | `worldUiState` | In-world substate: free roam, menus, dialogs, map view, shops, SMS, transitions. | VERIFIED/PARTIAL |
| `game.k.f/g` | `worldId/roomId` | Current world/room indexes. Also target world/room during transition. | VERIFIED |
| `game.k.h/i` | `spawnX/spawnY` | Target spawn coordinate for transition variants. | VERIFIED/PARTIAL |
| `game.k.j` | `targetActorId` | Target actor id after transition, `-1` when no actor target. | VERIFIED |
| `game.k.w/x/y/z/A/B/C` | `transitionGlobals` | Prepared by event opcodes `21/22`; used on room reload/transition. | VERIFIED/PARTIAL |
| `game.k.c` | `player` | `game.g` player/player-data object. It is also a moving actor in world. | VERIFIED |
| `game.k.d[]` | `actors` | Room actor array, each `game.a`. | VERIFIED |
| `game.k.b` | `mapRenderer` | Map renderer `j` (not `game.j` text renderer). | VERIFIED |
| `game.k.M` | `eventVm` | `game.c` event executor for current scene. | VERIFIED |
| `game.k.S` | `ui` | `game.h` UI facade. Most non-free-roam states delegate here. | VERIFIED |
| `game.k.p` | `helperSprite` | Temporary attached sprite, usually created by `a(x,y,parent)` and removed by `e()`. | VERIFIED |
| `game.k.o` | `worldAttachedEffect` | One attached effect sprite created by `a(spriteId)`/`a(f parent)` and removed by `g()`. | VERIFIED |

## 2. Resource Loaders Around World

| Method | Resource | Behavior | Status |
| --- | --- | --- | --- |
| `T()` | `/data/script/petArea.mid` | Loads encounter rows for current room. Splits rows by encounter type field into vectors `al/am/an/ao`. | VERIFIED |
| `U()` | `/data/script/petRide.mid` | Loads room ride/transport config into player `c.Q`, updates `aA/aB/aC` lookup indexes. | VERIFIED/PARTIAL |
| `V()` | `/data/script/backPic.mid` | Loads room background/color. If type `0`, loads `/data/img/img_<id>`; if type `1`, sets background color. Also loads gold and `img_10023`. | VERIFIED |
| `W()` | actor list | Calls each actor `f()` visibility/collision update and adds it to map renderer. | VERIFIED |
| `ak()` | actor type `v==14` | Precomputes max extension distance `B` by probing map collision in actor facing direction. | VERIFIED |

## 3. World State `P` Setup Matrix

Entry: `game.k.a(byte state)` stores previous state in `Q`, does setup, sets `P=state`, calls `P()`.

| P | Setup in `a(byte)` | Tick in `b()` | Meaning | Status |
| --- | --- | --- | --- | --- |
| `0` | Reset input/ui flags, close or reset UI depending event state, set player idle. | Free-roam input, actor tick, map tick, random encounter, event VM tick, evolution tips. | FREE_ROAM | VERIFIED/PARTIAL |
| `1` | `S.j=1`, `S.C()` | `S.D()` | Menu/dialog helper state. Exact UI screen belongs to `game.h`. | VERIFIED/PARTIAL |
| `2` | If selected actor type `24` or `20`, opens specific UI `S.a(...)`. | Same branching, depends actor type or `M.c`. | NPC/special actor interaction branch. | VERIFIED/PARTIAL |
| `32` | `S.j=3`, open UI `S.a(3,2)`. | `S.a(3,2)`. | Variant of state `2`. | VERIFIED/PARTIAL |
| `26` | `S.j=2`, open UI `S.a(4,0)`. | `S.a(4,0)`. | Variant of state `2`. | VERIFIED/PARTIAL |
| `3` | `S.L()` | `S.M()` | Shop/bag-style UI delegate. | PARTIAL |
| `4` | No UI setup. | Full-screen map/minimap view, scrollable with nav keys, exits on action/back mask `262145`. | VERIFIED |
| `5` | `S.ad()` | `S.ae()` | UI delegate. | PARTIAL |
| `6` | `S.k()` | `S.l()` | UI delegate, also tutorial can enter here. | PARTIAL |
| `7` | `S.W()` pet selection setup. | `S.X()` plus `l()` tutorial tick. | Pet selection / party view branch. | VERIFIED/PARTIAL |
| `8` | `S.Y()` | `S.ac()` | Bag/inventory branch. | VERIFIED/PARTIAL |
| `9` | `S.N()` | `S.O()` | UI delegate. | PARTIAL |
| `10` | `S.R()` | `S.S()` | Task/menu branch. | VERIFIED/PARTIAL |
| `11` | `S.P()` | `S.Q()` | UI delegate. | PARTIAL |
| `12` | `S.T()` | `S.U()` | UI delegate. | PARTIAL |
| `13` | `S.m()` | `S.n()` | UI delegate. | PARTIAL |
| `14` | `S.az()` | `S.aA()` | UI delegate. | PARTIAL |
| `15` | `S.y()` | `S.z()` | UI delegate. | PARTIAL |
| `16` | `S.A()` | `S.B()` | UI delegate. | PARTIAL |
| `17` | Sets `S.l=false`, falls through to `18/19` setup: `S.W()`. | `S.Z()` | Pet/selection variant. | PARTIAL |
| `18` | `S.W()` | `S.aa()` | Pet/selection variant. | PARTIAL |
| `19` | `S.W()` | `S.ab()` | Pet/selection variant. | PARTIAL |
| `20` | `S.u()` | `S.v()` | UI delegate. | PARTIAL |
| `21` | `S.w()` | `S.x()` | UI delegate. | PARTIAL |
| `22` | Save confirm prompt `S.H()` + text. | `S.K()` | Save prompt state. | VERIFIED/PARTIAL |
| `23` | NPC dialog text/choice via `S.a(...)`, depends previous `Q`, actor type and text index. | Waits `S.c(t,s)` and key `196640`; advances page via `ae.c()`; returns to `0` or special states. | NPC_DIALOG | VERIFIED/PARTIAL |
| `24` | `S.h()` | `S.i()` | UI delegate. | PARTIAL |
| `25` | `S.ar()` | `S.as()` | Evolution/tip sequence branch from static `H/I/K/L`. | PARTIAL |
| `27` | `S.aQ()` | `S.aR()` | Special actor/shop/evolution branch. | PARTIAL |
| `28` | `S.a(byte,index,x,y)` using transition room table. | `S.aN()` | Wharf/transition destination UI. | VERIFIED/PARTIAL |
| `29` | Starts effect `b.a().c(0,2)`. | Waits effect manager `b.a().b`; then `game.i` state `23`. | World reload/transition effect. | VERIFIED |
| `30` | `S.aO()` | `S.aP()` | UI delegate. | PARTIAL |
| `31` | Badge/evolution condition dialog setup, computes `aU/aV/aW`. | Waits dialog paging/action, grants badge/progression then returns to `27`. | BADGE_PROGRESS_DIALOG | VERIFIED/PARTIAL |
| `100` | `S.aG()` | `S.aM()` | SMS activation state. | VERIFIED/PARTIAL |
| `101` | `S.aH()` | `S.aM()` | SMS buy master ball. | VERIFIED/PARTIAL |
| `102` | `S.aJ()` | `S.aM()` | SMS buy money. | VERIFIED/PARTIAL |
| `104` | `S.aI()` | `S.aM()` | SMS buy badge. | VERIFIED/PARTIAL |

Important honesty note: the table above closes setup/tick dispatch, not all `game.h` UI side effects. Many states are verified only as delegates because the actual branch is in `game.h` and file 16.

## 4. Free-Roam Tick `P=0`

Observed order in `game.k.b()`:

1. Guard `Y`; return if not active.
2. `S()` top-level input/update helper.
3. If event VM not blocking, player state `<5`, UI ready:
   - movement keys: `4100` up, `8448` down, `16400` left, `32832` right call `player.b(1,dir)`;
   - action `65568` interacts with selected actor `u`, parent actor, or player special action;
   - menu/help/action masks enter UI states `6/13/10/5` etc.
4. Player tick: `c.q()`.
5. Actor tick: each `d[i].o()`.
6. Attached effect follows parent if active.
7. Update player previous facing, map renderer tick `b.b()`.
8. Random encounter branch when player movement condition `c.D()` passes.
9. Tutorial/evolution tip branch.
10. Event VM `M.b()` and world helper `l()` if UI not blocking.

Status: VERIFIED/PARTIAL. Main order and random encounter branch are source/bytecode checked enough to port. Remaining PARTIAL is the tutorial/evolution tail domain naming and some UI-delegate side effects after the encounter branch.

## 5. Random Encounter Matrix

Data load:

- `T()` reads `/data/script/petArea.mid`.
- Current room row is `ak[l[f] + g]`.
- Row after first 5 shorts is split into 4-short encounter entries.
- Entries are grouped by field `[1]`:
  - `0` -> `al`
  - `1` -> `am`
  - `2` -> `an`
  - `4` -> `ao`
  - type `3` has no vector use in source branch.

Encounter in free roam:

| Step | Behavior | Status |
| --- | --- | --- |
| Tile type | `j.a().b(player.i, player.j)` sets static `O`. | VERIFIED |
| Candidate vector | Tile type `0/1/2/4` chooses one random row from `al/am/an/ao`. Tile type `3` returns false/no encounter. | VERIFIED |
| Extra field | If candidate `[2] != -1`, compute `extra = ae.b([2],[3])`, else `-1`. | VERIFIED |
| Room gate | `this.c()` must pass before battle setup; source verifies this is `ak[l[f]+g][2] != -1`, i.e. current room has encounter config. | VERIFIED |
| Level | `level = ae.b(ak[l[f]+g][3], ak[l[f]+g][4])`. | VERIFIED |
| Player display | `player.a(element, species, 1)` using species element `aq.c[0][species][1]`. | VERIFIED |
| Battle setup | `game.d.a().a(new int[][]{{species, level, extra}})`. | VERIFIED |
| Battle flags | `game.d.a().a = 0`, `game.d.a().b = 0`, capture world snapshot into `game.d.c`. | VERIFIED |
| Transition | `game.c.j = !(f==3 && g==7)` then `game.i.a().a((byte)12)`. | VERIFIED |

Current status: VERIFIED. Data flow is closed enough to port: tile type selects encounter vector, `c()` is the encounter-enabled room predicate, battle setup writes `game.d`, and top state switches to `game.i` state `12`. Remaining non-random-battle item is only the special room `3,7` flag `game.c.j=false`, already visible in source but domain-named as special challenge room rather than fully gameplay-named.

## 6. World / Room Transition Fields

Event opcode docs already cover `6/21/22/23`; this file records how world actors write fields too.

| Field | Writer | Meaning | Status |
| --- | --- | --- | --- |
| `f/g` | event opcode `6`, actor `game.a` type `t=1`, UI transmit/wharf | Target/current world and room. | VERIFIED |
| `h/i` | event opcode `22`, actor transition table `T` | Spawn coordinates after room change. | VERIFIED |
| `j` | event opcode `6`, actor `game.a` type `t=1` | Target actor id to spawn near/interact with, or `-1`. | VERIFIED |
| `w` | event opcode `22`, actor transition table `T` | Transition variant/direction. | VERIFIED |
| `x/y/z/A/B/C` | event opcodes `21/22` | Prepared transition behavior and extra coords/flags. | VERIFIED/PARTIAL |

Actor transition behavior from `game.a.o()`:

| Actor `t/v` | Trigger | Writes | Top-level state | Status |
| --- | --- | --- | --- | --- |
| `t=1, v=0/1/3` | Player collision and direction/room condition. | `game.k.f=N`, `g=O`, `j=P`. | `game.i` state `9`. | VERIFIED |
| `t=1, v=2` | Player collision; finds matching entry in actor `T` table. | `h/i/w` from `T`, then `f=N`, `g=O`, `j=-1`. | `game.i` state `9`. | VERIFIED |
| `t=1, v=4` | Player collision unless player state `9/10`. | Moves player to actor position, state `9`, `j=P`. | local state change, not direct top-level reload. | VERIFIED |

## 7. `n` Motion Base

| Method | Behavior | Status |
| --- | --- | --- |
| `b(x,y)` | Set position. | VERIFIED |
| `d(dx)`, `e(dy)` | Move x/y by delta. | VERIFIED |
| `a(speed,targetX,targetY)` | Move toward target by vector step; snaps if distance smaller than speed. Returns true if already at target before move. | VERIFIED |
| `c(byte anim)` | Enable follower trail buffer of length `a+1` where `a=10`, seeded from parent `p`. | VERIFIED |
| `a(d,d)` | If trail active and parent has nonzero state, shift trail buffer and set follower position/animation from delayed slot. | VERIFIED |
| `a(n parent)` | Set parent pointer. | VERIFIED |

Direction convention inherited through `f.a(int)`:

| Direction `n` | Movement |
| --- | --- |
| `3` | x -= speed |
| `1` | x += speed |
| `2` | y -= speed |
| `0` | y += speed |

## 8. `f` Sprite Actor Base

| Method | Behavior | Status |
| --- | --- | --- |
| `a(spriteId, mirrored?)` | Load sprite through `d.a`. | VERIFIED |
| `a(anim, variant, loop)` | Set animation on sprite. | VERIFIED |
| `a(Graphics,cameraX,cameraY)` | Draw sprite if visible flag `f` is true; flips/render variant when facing `n==3`. | VERIFIED |
| `c()` | Activate visible/update/collision flags. | VERIFIED |
| `d()` | Deactivate visible/update/collision flags. | VERIFIED |
| `a(speed)` | Move by current facing and move child `b` if attached. | VERIFIED |
| `f()` | Visibility/culling: active if sprite is forced visible or intersects viewport. | VERIFIED |

## 9. `game.a` Actor Init Matrix

Actor init: `a(short[] row, int actorIndex)`.

| Actor field | Meaning | Evidence | Status |
| --- | --- | --- | --- |
| `I` | actor index in room. | set from method arg. | VERIFIED |
| `t` | actor record type / behavior group. | `sArray[0]`; switch in init and update. | VERIFIED |
| sprite id | `sArray[1]` loaded into `this.a`. | `this.a.a(sArray[1],false)`. | VERIFIED |
| initial anim/facing | `sArray[2]`, then `d(byte)`. | direction/anim mapping depends `t/v`. | VERIFIED/PARTIAL |
| `i/j` | world pixel position. | `sArray[3/4]`. | VERIFIED |
| active flag | `sArray[5] == 1` -> active visible. | `b(true/false)`. | VERIFIED |
| `v` | subtype/behavior. | `sArray[6]`; drives most actor behavior. | VERIFIED |
| `s/w/u/x/y` | type-0 extra params. | rows `[7..12]`; exact domain varies by subtype. | PARTIAL |
| `N/O/P` | type-1 transition target world/room/actor. | rows `[8..10]`. | VERIFIED |
| `C` | type-1 direction/condition index. | row `[7]`, maps through `R={2,3,0,1}` against player facing `game.g.o().o`. | VERIFIED |
| `D/E/F` | type-3 effect/trigger params. | rows `[7..9]`. | PARTIAL |

## 10. `game.a` Actor Type `t` Matrix

| `t` | Meaning observed | Update behavior | Status |
| --- | --- | --- | --- |
| `0` | Normal visible/interactable actor/NPC/object. | Subtype `v` controls wander, attached marker, proximity, parent behavior. | VERIFIED |
| `1` | Transition/portal trigger actor. | On player collision, writes `game.k.f/g/h/i/j/w` and enters top-level world transition or local player state `9`. | VERIFIED |
| `2` | Conditional/invisible-ish actor record type. | Init only marks `Q`; no autonomous update branch in `o()`. No decoded event actor uses `t=2` in current data. | VERIFIED from code/data |
| `3` | Effect/trigger/static actor record type. | Init stores `D/E/F`, remaps direction codes, marks active. `o()` has no autonomous `t=3` update branch; event opcodes mutate/display these actors. | VERIFIED/PARTIAL |

## 11. `game.a` Subtype `v` Behavior Matrix

This table is source-grounded but still not fully domain-named.

| `v` | Behavior | Status |
| --- | --- | --- |
| `0` | Basic actor, direction/animation set by `d(byte)`, can participate in transition trigger when `t=1`. | VERIFIED |
| `1` | NPC-like actor with optional marker sprite `b`; attached marker and parent helper logic. | VERIFIED/PARTIAL |
| `2` | Random vertical wander/patrol. Chooses from directions `{0,1,2,3,5}`, moves `J` between `0..64` when direction `3/5`. | VERIFIED/PARTIAL |
| `3` | Random patrol variant. Chooses from `{0,1,2,4}`, moves `J` between `0..64` depending `n`. | VERIFIED/PARTIAL |
| `4/5/6/7/15` | Interactive actor group. If helper `H` is active but relation/collision is no longer valid, hides it. When actor state `h==1` animation completes, switches to state `2`, writes room actor state through `game.k.a(I,0,h,...)`, clears `game.c.h`, and `v==6/7` can grant a random `1` value through `game.g.s(n)` plus floating text entry `player.V`. | VERIFIED |
| `8` | Directional push/move actor: tries collision in facing direction, moves by `8`, increments `J` to max `2`, then resets. | VERIFIED/PARTIAL |
| `9/10` | Parent/linked actor movement; moves both self and parent `p` by `4` if path/collision checks pass, else resets. | VERIFIED/PARTIAL |
| `11` | No-op in update switch. | VERIFIED |
| `12` | Proximity actor: if player overlaps, forces player state `8`; else alternates vertical movement/checks. | VERIFIED/PARTIAL |
| `13` | Proximity actor variant: same as `12` but horizontal/facing pair `1/3`. | VERIFIED/PARTIAL |
| `14` | Gauge/extension/link actor. `game.k.ak()` precomputes max open distance `B` by probing map tiles; runtime `q()` scans outward in facing direction, stops on blocked tile, and links the first colliding actor with different `v` by setting `other.p=this`. `M` ticks `0..4` for draw width/animation. | VERIFIED |
| `16` | Proximity actor: if player overlaps and player state not `5`, sets player state `5`. | VERIFIED/PARTIAL |
| `17` | Init creates marker sprite `b` like special NPC group, but `o()` has no case `17`; runtime only falls through to `f()` visibility/culling unless event opcodes mutate it. Present in resource data. | VERIFIED |
| `18` | Init/direction mapping follows `v==1` style (`sArray[2] % 3`, grouped anim frames), but `o()` has no case `18`; runtime only falls through to `f()` visibility/culling unless event opcodes mutate it. Present in resource data. | VERIFIED |

Known bytecode-confirmed oddity: in `v=4/5/6/7/15`, bytecode checks `v == 6` then immediately checks the same field `v == 7`. That branch is unreachable in normal execution. Port should preserve behavior by always taking the reachable `game.k.a(I,0,h,true)` path, not "fix" it into `v == 6 || v == 7`.

## 11.1 Actor Usage From Decoded Event Data

Source of counts: all `event/decoded/data__event__scene_*.json`, parsed from original `data/event/scene_*`.

Total actor records counted: `2718`.

| `t/v` | Count | Rebuild implication | Status |
| --- | ---: | --- | --- |
| `0/0` | 1782 | Static/basic visible actor dominates data. | VERIFIED |
| `0/1` | 409 | NPC/helper marker behavior is heavily used. | VERIFIED |
| `0/2` | 1 | Random vertical wander exists but rare. | VERIFIED |
| `0/3` | 4 | Random patrol exists but rare. | VERIFIED |
| `0/4` | 9 | Interactive group used. | VERIFIED |
| `0/5` | 11 | Interactive group used. | VERIFIED |
| `0/6` | 63 | Reward/random-value interactive group used. | VERIFIED |
| `0/7` | 39 | Reward/random-value interactive group used. | VERIFIED |
| `0/8` | 13 | Directional push/move actor used. | VERIFIED |
| `0/9` | 8 | Parent/linked movement used. | VERIFIED |
| `0/10` | 13 | Parent/linked movement used. | VERIFIED |
| `0/11` | 13 | No-op update branch used. | VERIFIED |
| `0/12` | 4 | Vertical proximity mover used. | VERIFIED |
| `0/13` | 5 | Horizontal proximity mover used. | VERIFIED |
| `0/14` | 8 | Gauge/extension/link actor used. | VERIFIED |
| `0/15` | 0 | Code supports it; no decoded event actor currently uses `0/15`. | VERIFIED from data |
| `0/16` | 4 | Proximity state-5 actor used. | VERIFIED |
| `0/17` | 24 | Data uses marker-only subtype; update has no dedicated case. | VERIFIED |
| `0/18` | 8 | Data uses v1-style init subtype; update has no dedicated case. | VERIFIED |
| `1/0` | 32 | Transition trigger type used. | VERIFIED |
| `1/1` | 190 | Transition trigger type used heavily. | VERIFIED |
| `1/2` | 1 | Special transition table `T` used once in current data. | VERIFIED |
| `1/3` | 5 | Direction/event-mutating transition type used. | VERIFIED |
| `1/4` | 9 | Local teleport/player state-9 transition used. | VERIFIED |
| `2/*` | 0 | `t=2` init code exists, but no decoded event actor uses it in current data. | VERIFIED from data |
| `3/0` | 53 | Effect/trigger/static actor group used; no autonomous update in `o()`. | VERIFIED |
| `3/1` | 10 | Effect/trigger/static actor group used; no autonomous update in `o()`. | VERIFIED |

Honesty note: "VERIFIED from data" only means in this decoded resource set. If another JAR variant has different event data, counts must be regenerated.

## 12. Actor Parent / Helper Sprites

| Field/helper | Behavior | Status |
| --- | --- | --- |
| `f.b` | Child sprite tied to parent movement. `f.a(speed)` and `f.b(x,y)` move child with parent. | VERIFIED |
| `n.p` | Generic parent pointer. Used by follower trail and helper sprites. | VERIFIED |
| `game.a.G` | Marker/effect sprite created for `w != 0` and active actor; sprite `259`, animation `w`, parent set to actor. | VERIFIED |
| `game.a.H` | Helper sprite created by `f(int offset)`, sprite `259`, anim `7`, parent set to actor. | VERIFIED |
| `game.a.b` | Marker sprite `337` created for `v==1/2/3/17`, positioned at actor. | VERIFIED/PARTIAL |
| `game.k.p` | World helper sprite created by `game.k.a(x,y,parent)`, sprite anim `13`, parent set to passed actor. | VERIFIED |
| `game.k.o` | Attached world effect sprite loaded by sprite id and attached to parent via `game.k.a(f parent)`. | VERIFIED |

## 13. Collision / Movement Helpers

| Helper | Behavior | Status |
| --- | --- | --- |
| `game.a.a(direction, distance, expectedTile)` | Probes map collision `j.a().b(...)` at actor edge based on sprite collision box and direction. | VERIFIED |
| `game.a.p()` | For `v==9`, sets actor direction to `1`. | VERIFIED |
| `game.a.r()` | Persists actor current x/y into room actor data `ah`. | VERIFIED |
| `game.a.s()` | Persists active/facing/state fields into room actor data `ag`. | VERIFIED |
| `game.a.x()/w()/z()` | Hide/remove helper sprites `H/G`. | VERIFIED |
| `game.a.b(Graphics,camX,camY)` | Debug/progress bar draw around actor direction; likely not normal gameplay render. | VERIFIED/PARTIAL |

## 14. Camera `ai`

| Mode | Setup | Tick behavior | Status |
| --- | --- | --- | --- |
| `0` fixed point | `a(x,y,instant)` | If instant, set camera immediately; else move toward `q/r` by speed `w`, then mark complete. | VERIFIED |
| `1` follow actor | `a(n target,instant)` | If instant, camera copies target every tick; else moves toward target until complete. | VERIFIED |
| `2` path/table | No source call found that sets mode `2`; public setup methods used by opcode `11` only select modes `0/1`. | `ai.d()` bytecode contains literal `aconst_null` array accesses, so if mode `2` were entered as-is it would not have valid path data. Treat as unused/dead or obfuscated-damaged path until another source proves otherwise. | VERIFIED as unused in audited calls / UNKNOWN semantics |
| `3` effect override | `b.a(int,int,int)` calls `ai.d((byte)3)` during effect id `11`. | `ai.d()` switch has default return for mode `3`; effect code restores previous mode via `ai.a().a = ai.a().b`. | VERIFIED |

Camera completion: `ai.c()` returns `x`, i.e. movement/follow is complete/instant.

## 15. Effect Actor `ah`

`ah` is a standalone effect sprite, not the same as effect manager `b.java`.

| Mode `u` | Init/render/update behavior | Status |
| --- | --- | --- |
| `0` | Two source sprites -> blends/alternates into `b[2]` over duration `t[2]`. | VERIFIED |
| `1` | One sprite plus icon/texture, scrolls/masks in four directions based `t[4]`, then blends into `b[2]`. | VERIFIED/PARTIAL |
| `2..6` | Empty init/update/render branches in source. | VERIFIED as no-op branch |
| `7` | Scaled second image blink/toggle over base. | VERIFIED |
| `8` | Multi-step transformed image, draws `b[1]`. | VERIFIED/PARTIAL |
| `9/10` | Base plus transformed/alpha image toggled by timer. | VERIFIED |
| `11/14` | Multiple child images with offset table; mode `14` uses alpha conversion. | VERIFIED/PARTIAL |
| `12/13` | Two-layer movement based offset table. | VERIFIED/PARTIAL |
| `15` | Multi-frame selection from table `[spriteIndex,dx,dy]`. | VERIFIED/PARTIAL |
| `16` | Sprite strip/alpha cleanup render. | VERIFIED/PARTIAL |
| `17` | Similar to `9/10` with extra vertical offset and image alpha transform. | VERIFIED |
| `20+` | Uses hardcoded sprite id table `x[u-20]` and normal `d` sprite renderer. | VERIFIED |

Status: enough to port effect actor scaffold. Pixel-perfect effect behavior should be validated visually.

## 16. What Is Closed vs Not Closed

Closed enough for rebuild skeleton:

- World state dispatch `P` setup/tick delegation.
- Free-roam tick order.
- Random encounter data flow into `game.d` and `game.i` state `12`, including room encounter gate `ak[l[f]+g][2] != -1`.
- Transition fields written by actors/event paths.
- Base motion/follower trail in `n`.
- Sprite actor movement/draw in `f`.
- `game.a` subtype behavior used by current decoded data at code-path level, including `v=17/18` fall-through and bytecode-confirmed unreachable `v==6 && v==7` branch.
- Camera fixed/follow modes.
- `ah` mode scaffold.

Not closed yet:

- Exact domain names and all side effects for many `game.k.P` UI delegate states.
- `game.k.b()` tutorial/evolution tip tail domain names and UI delegate side effects.
- `ai` mode `2` semantics if a different game variant or hidden script can set it; current audited code/data do not.
- Pixel validation for `ah` effect actor modes.

## 17. Recommended Next Audit

Before implementing world actor fully:

1. Add small Java rebuild tests for `n.a(speed,x,y)`, follower trail, and `game.a` collision probes against map `j.a().b`.
2. During implementation, create regression scenes for used actor groups `0/4..0/14`, `0/17`, `0/18`, and transition types `1/0..1/4`.
3. Keep `ai` mode `2` behind an explicit unsupported/dead-path guard unless later resource evidence shows a valid setup path.
4. Pixel-validate `ah` effect actor modes against screenshots after renderer is implemented.
