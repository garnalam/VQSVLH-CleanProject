# Runtime/Core Notes

Pham vi audit lan nay:

- `game/GameMIDLet.java`
- `game/e.java`
- `game/i.java`
- `an.java`
- `ap.java`
- `as.java`
- lien quan truc tiep: cac state con ke thua `an` nhu `game.k`, `game.f`, `game.d`, `game.c`

Trang thai: VERIFIED/PARTIAL cho runtime core. Cac he world/UI/battle chi duoc nhac toi o muc tac nhan,
chua coi la da audit sau.

## 1. Y Nghia Tung File

### `game/GameMIDLet.java`

Vai tro:

- Entry point MIDP that extends `MIDlet`.
- Lay `Display`.
- Tao singleton Canvas `game.e`.
- Set Canvas thanh man hinh hien tai.

Co che:

```text
new GameMIDLet()
  -> Display.getDisplay(this)
  -> e.a(this)
  -> display.setCurrent(canvas)
```

Dieu tac dong den no:

- MIDP container goi constructor/startApp/pauseApp/destroyApp.
- `game.e.run()` se goi `GameMIDLet.destroyApp(true)` khi state manager tra state <= 1.

Ghi chu rebuild:

- Java SE debug target co the thay bang `DebugLauncher`.
- MIDP target can giu `MIDlet` entrypoint tuong tu.

### `game/e.java`

Vai tro:

- Canvas that extends MIDP `Canvas` and implements `Runnable`.
- La game loop chinh.
- Chuyen input key/pointer vao state manager `game.i`.
- Quan ly fullscreen va kich thuoc man.

Co che init:

```text
e constructor
  -> setFullScreenMode(true)
  -> an.A()                    // set frame delay = 66ms
  -> an.a(width, height)       // luu kich thuoc man vao an
  -> i.a()                     // singleton state manager
  -> i.c()                     // enable/init state manager, set state 3
  -> i.c(true) via ap          // enable input
  -> new Thread(this).start()
```

Game loop:

```text
while (i.e() > 1):
  start = now
  i.b()              // update current state
  repaint()
  serviceRepaints()  // force paint sync in MIDP style
  elapsed = now - start
  sleep(an.B() - min(elapsed, an.B()))
after loop:
  MIDlet.destroyApp(true)
```

Render:

```text
paint(Graphics g):
  if i.e() > 1:
    i.b(g)
```

Input:

- `keyPressed(code)` -> `i.i(code)`.
- `keyReleased(code)` -> `i.j(code)`.
- Pointer press maps softkey-ish areas to key codes:
  - left soft area -> `-6` or `-21`
  - right soft area -> `-7` or `-22`
  - then calls `keyPressed(mappedCode)`.
- Pointer release calls `keyReleased(lastPointerKey)` and forwards coordinates via `i.d(x,y)`.

Dieu tac dong den no:

- `game.i` state value controls loop lifetime and paint.
- `an.T` controls pause behavior in `hideNotify`.
- Device screen size affects all global coordinate helpers.

Ghi chu rebuild:

- Must keep fixed-step-ish loop of 66ms for behavior/timing.
- Need force render after update, especially for old MIDP timing.
- Pointer mapping matters for UI soft keys.

### `ap.java`

Vai tro:

- Base input bitmask class.
- Maintains current/pressed/released key masks.
- Supports chained active input receiver (`private ap a`).

Fields meaning inferred:

| Field | Meaning |
|---|---|
| `b` | held keys/current down mask |
| `c` | pressed-since-last-frame mask |
| `d` | released-since-last-frame mask |
| `e` | latched held mask for current update |
| `f` | latched pressed mask for current update |
| `g` | latched released mask for current update |
| `h`, `i` | last pointer x/y |
| `Y` | enabled flag, used by `an` states |
| `a` | child input receiver |

Key mapping:

| Key code | Bit |
|---|---|
| `48` (`0`) | `1` |
| `49`..`57` (`1`..`9`) | `2`..`512` |
| `42` (`*`) | `1024` |
| `35` (`#`) | `2048` |
| `-1,-2,-3,-4,-5` | d-pad/fire bits |
| `-6`/`-21` | `131072` left soft |
| `-7`/`-22` | `262144` right soft |

Core methods:

```text
i(code): key down, set c and b, forward to child
j(code): key up, set d and clear b, forward to child
S(): latch b/c/d into e/f/g, then clear c/d
k(mask): pressed this frame
l(mask): held this frame
R(): released mask intersects 0xF154
P(): reset all input masks, also child
a(ap child): replace active child input receiver
```

Important:

- `k(1)` means key `0` pressed this frame.
- `game.c` text wait checks `this.y.k(1)`.
- Parent state can forward input to child state. This is central to `game.i` -> current `an` state.

Ghi chu rebuild:

- Do not use raw Java key event directly in gameplay. Rebuild must use this bitmask model.
- Need preserve one-frame pressed/released semantics.

### `an.java`

Vai tro:

- Abstract base for all major game states.
- Extends `ap`, so every state is also an input receiver.
- Stores global screen size, frame delay, default MIDP fonts, global background color.
- Owns shared UI refs `R` (`ab`) and `S` (`game.h`).
- Provides timer helper and SMS/payment-related runtime callbacks.

Abstract state contract:

```text
public abstract void b();             // update
public abstract void b(Graphics g);   // render
public abstract boolean d();          // init/load
public abstract void f();             // cleanup
public abstract void a(byte state);   // set local substate
```

Screen/timing globals:

```text
an.A()       -> c = 66
an.B()       -> frame delay
an.a(w,h)    -> set global screen size
an.w()/x()   -> width/height
an.y()/z()   -> half width/height
an.e(color)  -> set global background color
an.C()       -> background color
```

Font helpers:

- `D()` returns MIDP small/system font.
- `E()` returns larger MIDP font.
- `F()` returns width of "Sung" fallback if font not ready.
- `G()` returns default font height.

Timer/loading helper:

```text
an.s():
  if no timer is active:
    schedule new as() after 10ms, then every 200ms

an.t():
  cancel timer, set k = true

an.u():
  k = false

an.v():
  return k
```

`as.java` only calls:

```text
e.a().repaint()
```

This timer is separate from the main 66ms loop. It has two jobs:

- keep MIDP repaint alive while synchronous loading is running;
- act as the loading gate through `k`: `u()` means loading not complete, `t()` means loading complete, `v()` reads that flag.

Important callers:

- `game.i.d()` starts `s()` before global bootstrap and calls `t()` when done.
- `game.k.d()` starts `s()` before room/world load and calls `game.k.t()` when done.
- `game.d.d()` starts `s()` before battle resource load and calls `game.d.t()` when done.

Rebuild note:

- Java SE/MIDP rebuild should model this as `beginLoading()`/`endLoading()` plus optional repaint pulse.
- Do not treat it as cosmetic only, because `game.i` state `3` and `12` gate transition behavior on `an.v()`.

Text/database helper:

- `an.f(id)` returns `aq.d[id]`.
- `an.a(id, int[])`, `an.a(id, String[])`, `an.a(String,int[])` perform `%s` substitutions.

SMS/payment block:

- `an` also contains SMS activation/purchase flow with callbacks through interfaces `c` and `x`.
- It can mutate gameplay state after payment success:
  - grant money/items,
  - update task/event state,
  - adjust pets/levels,
  - set global flags.

Payment package ids:

| `o` | Meaning inferred from text/effect | Success side effect |
|---|---|---|
| `0` | Activate/unlock full game | `X=true`; grant 2000 money; grant items `(1,5)`, `(4,5)`, `(11,2)`; grant 5 badges; set event/task state `game.c.a().b[game.k.a(9,0)][5]=3` and `game.c.a().a[5].a(3)` |
| `1` | Catch item / "Tat trung cau" | grant item `(0,1)` |
| `2` | Buy money | grant 10000 money |
| `3` | Buy levels | level up every owned pet by up to 5, capped at 50; rebuild level-up queue in `game.k.E/F`; set `game.k.G` to drive post-level UI |
| `4` | Buy badges | grant 10 badges |

Payment flow fields:

| Field | Meaning |
|---|---|
| `l` | payment UI/status state (`1` confirm, `4` sending/waiting, `2` success, `3` failure, `5` close/reset) |
| `o` | selected package id |
| `p` | current success count |
| `q` | required success count, currently 1 for all packages |
| static `m[]` | per-package purchase count |
| static `T` | payment flow active flag |

Rebuild note:

- For a full rebuild, SMS must become an adapter/service with the same callback surface.
- Even if real SMS is disabled, a debug purchase must execute exactly these side effects or later quests, inventory, pet-level UI and save state will diverge.

Ghi chu rebuild:

- Runtime skeleton should keep `GameState` base like `an`.
- Payment/SMS can initially be stubbed, but side effects must still run through a deterministic debug adapter.
- Timer repaint behavior should be represented, even if simplified in Java SE.

### `game/i.java`

Vai tro:

- Top-level state manager.
- Extends `an`, so it participates in update/render/input.
- Owns the active child state `m`.
- Manages state transitions: boot, logo, menu, world loading, world, battle, pause.
- Loads core resources in `d()`.
- Manages music playback through J2ME `Player`.

Singleton:

```text
i.a():
  if null new i()
  v = "0" // current sound id
```

Activation:

```text
i.c(true):
  i.c()
    -> set timestamp
    -> a((byte)3)
    -> super.c(true)
```

Initial state:

- `c()` sets top-level state to `3`.
- State `3` is boot/loading transition.
- In update, state `3` calls `d()` once resources need loading, then waits for effect complete, then goes to logo state `15`.

Resource bootstrap in `d()`:

```text
this.s()        // start repaint timer
S = game.h.a()
R = ab.a()
S.a(this)
am.a()          // image cache init
aa.a()          // sprite cache init
aq.a()          // script tables/modInfo/etc
an.e(0)         // bg color
an.D()          // init font
game.k.a()      // world singleton
game.k.i()      // static/world init
an.t()          // stop repaint timer, mark loaded
```

Top-level state ids observed:

| State | Meaning inferred | Evidence |
|---|---|---|
| `1` | inactive/exit-ish | `game.e` loop runs while `i.e() > 1` |
| `2` | pause screen | `i.b(Graphics)` draws "Tro choi tam dung" |
| `3` | boot/loading transition | `a(3)` starts effect type 19; update calls `d()` |
| `4` | small modal/confirm-ish | input left soft exits state, right soft resumes |
| `6` | main menu choice screen | render draws menu texts; input left/right soft |
| `7` | pre-menu/loading menu state | creates `game.f` then goes `8` |
| `8` | menu state running | delegates to `m.b()`/`m.b(g)` |
| `9` | world loading screen | creates `game.k`, calls `m.d()`, then state `11` |
| `10` | battle loading/init | creates `game.k`? calls `k.p()` then state `11` |
| `11` | world running | delegates to active `m` |
| `12` | battle/special transition load | creates `game.d`, starts effect, waits before battle state |
| `13` | battle running | delegates to active `m` |
| `15` | first logo `/data/logo/0` | after delay goes to `16` |
| `16` | second logo `cwalogo` | after delay goes to `6` |
| `20` | reserved delegated top-level slot | update/render delegate like `8/11/13`, but no direct top-level caller found |
| `22` | world reload/map transition variant | event opcode 6 sets target room then reloads `game.k` |
| `23` | world reload with centered loading/travel text | selected by world/menu flow, then reloads `game.k` |

Update flow in `i.b()`:

```text
if not enabled return
S() // latch input
switch topState:
  3: load core if needed, then go logo
  2: wait right soft to resume
  6: handle main menu softkeys
  7: create game.f menu and delegate
  9/22/23: create/load game.k world, set child, go state 11
  10: prepare world/battle transition, go state 11
  12: create game.d battle/special state, handle transition effect
  8/11/13/20: if child exists, child.b()
```

Render flow in `i.b(Graphics)`:

```text
if not enabled return
set font
switch topState:
  3: white loading/effect screen
  2: pause screen
  15/16: logo screens
  6: main menu screen
  9/23: loading screen/progress
  12: transition render
  8/11/13/20: child.b(Graphics)
```

Child state input:

- `i.a(this.m)` calls inherited `ap.a(ap2)` and makes active child receive input too.
- When state changes, old child is disabled via `child.c(false)`.
- New child gets `child.c(true)`.

Music:

- `a(String soundId)` loads `/data/sound/<id>.mid`, creates `Player`, gets `VolumeControl`, loops forever.
- Volume `g` is 0..3, actual level `g * 30`.
- `n()` stops/deallocates player.
- `h()` volume up, `i()` volume down.

Dieu tac dong den `game.i`:

- Input from `game.e`.
- Loading status `an.v()`.
- Effect state `b.a().b`.
- World flags `game.k`, `game.g.U`.
- Active child `an m`.
- Sound id `v`.

Ghi chu rebuild:

- Rebuild needs a top-level `StateManager` equivalent, not a direct jump into world.
- State ids can be preserved internally during port to reduce mistakes.
- Java SE can stub music initially, but state transitions should still call sound manager.

## 2. Quy Trinh Hoat Dong Tong The

### Launch to first playable/menu state

```text
MIDlet constructor
  -> Canvas e created
  -> screen size stored globally
  -> StateManager i created and enabled
  -> main thread starts

Loop frame:
  e.run()
    -> i.b() update
    -> repaint/serviceRepaints
    -> i.b(Graphics) render

StateManager:
  state 3 boot
    -> load resources
    -> logo 0
    -> logo cwalogo
    -> menu state 6
```

### Loading world

```text
menu/input selects start/continue
  -> i state 9/22/23
  -> stop current child
  -> m = game.k.a()
  -> m.d() loads world room/resources
  -> state 11
  -> i delegates update/render/input to game.k
```

### Running world/event

```text
i state 11
  -> m = game.k
  -> game.k.b() update world
  -> game.k.b(Graphics) render world
  -> game.k owns game.c event VM, actors, display list, UI
```

### Pause/resume

```text
Canvas hideNotify()
  if !an.T and i.e() > 1:
    i.g()

i.g()
  if in loading states 9/22/23:
    stop music, mark paused, reset input
  else if not already pause:
    stop music, mark paused, switch state 2

state 2:
  right soft key 262144 resumes previous state via k()
```

## 3. Cac Tac Nhan Anh Huong Runtime/Core

| Tac nhan | Anh huong |
|---|---|
| Device screen size | Stored in `an`, affects all render/layout/camera |
| `an.B()` frame delay | Controls update pacing; default 66ms |
| `ap` input masks | Controls all gameplay decisions; one-frame press semantics |
| Child state chain | Top state and active child both receive input if chained |
| `b.a()` effect manager | Blocks some state transitions/loading screens |
| `an.v()/u()/t()` loaded flag | Used during loading/transition states |
| `game.k` static flags | Affect world loading, menu continuation, loading screens |
| `game.g.U` | Alters loading render behavior with player sprite |
| Sound `Player` state | Music start/stop around menu/world/pause |
| SMS/payment callbacks | Can mutate game state and event state |

## 4. Nhung Dieu Can Port Chinh Xac

1. `GameLoop`: update -> repaint -> sync paint -> sleep 66ms.
2. `Input`: bitmask mapping and `S()` latch behavior.
3. `StateManager`: keep top-level state transitions.
4. `Child input delegation`: parent forwards input to current child.
5. `Global screen helpers`: width/height/half/bg color/font.
6. `Loading flags`: `an.u/t/v`.
7. `Pause behavior`: `hideNotify` and state `2`.
8. `Music hooks`: can stub first, but calls must exist.
9. `Payment side effects`: debug/mock SMS must mutate gameplay exactly like `an.b(true)`.

## 5. Closed Audit: Runtime/Core Remaining Points

### 5.1 `game.i` states `20`, `22`, `23`

Top-level `game.i`:

- State `20` is implemented as a delegated state in both update and render:
  - update: same branch as `8/11/13`, calls `m.b()`;
  - render: same branch as `8/11/13`, calls `m.b(Graphics)`.
- Audit result: no direct source caller of `game.i.a().a((byte)20)` was found.
- Therefore, for Runtime/Core it should be treated as a reserved/suspended delegated slot, not as a required active transition in the normal flow.
- Important distinction: many `a((byte)20)` calls in `game.d`, `game.k`, `game.h` are child substates, not top-level `game.i` state 20.

State `22`:

- Direct top-level caller found in `game.c` opcode 6.
- Opcode 6:
  - marks current event entry complete: `this.b[game.k.l[this.w.f] + this.w.g][eventId] = 3`;
  - writes next room/map coordinates into `game.k.a().f` and `game.k.a().g`;
  - optionally stores `this.w.j` from opcode parameters;
  - calls `game.i.a().a((byte)22)`.
- In `game.i.a(22)`: resets loading flag via `u()` and progress `n=0`.
- In `game.i.b()` state `22`: same loader path as `9/23`:
  - cleanup previous child;
  - `m = game.k.a()`;
  - `m.d()` loads world/room;
  - `game.g.U=false`;
  - switch to state `11`;
  - attach child input/update/render to `game.k`.
- Render has no special case for `22`, so this is effectively a silent/synchronous reload variant.

State `23`:

- Direct top-level caller found in `game.k` action case 29, after effect manager reports done.
- In `game.i.a(23)`: resets loading flag/progress and picks random text from `q[]`.
- In update it uses the same world reload path as `9/22`.
- Render differs from `22`: black background, centered white text `s`.
- Meaning: travel/boat/loading message variant before re-entering world state `11`.
- Important distinction: `game.h` and `game.d` also use local substate `23` for pet/battle UI flows; those are not top-level state `23`.

### 5.2 `game.i` state `12` and `game.d`

State `12` is the top-level battle/special-entry transition. It is not the battle loop itself.

Known top-level callers:

- `game.c` opcode 32:
  - calls `this.w.e()`;
  - writes `game.d.a().a` and `game.d.a().b` from opcode params;
  - creates a screen-sized image snapshot in `game.d.a().c`;
  - renders current world/map into that snapshot;
  - syncs player/pet state via `this.x.b((byte)0, this.x.n)`;
  - marks opcode done and calls `game.i.a().a((byte)12)`.
- `game.k` world encounter branch:
  - sets `game.d.a().a=0`, `game.d.a().b=0`;
  - creates snapshot;
  - renders world;
  - syncs player state;
  - calls `game.i.a().a((byte)12)`.

State `12` update sequence:

```text
if !an.v():
  m = game.d.a()
  m.d()                 // battle resource/init load, starts/stops timer internally
  attach child m
  start transition effect:
    game.d.b == 0 -> effect 6
    game.d.b == 1 -> effect 7
    game.d.b == 2 -> effect 8

if an.v():
  b.a().b()             // tick/wait effect
  game.g.U = false

if effect done:
  ((game.d)m).g()       // attach battle UI/controller and choose initial actors
  state = 13
```

`game.d` role:

- `game.d.d()` loads battle resources and initializes actor arrays/positions.
- `game.d.g()` attaches `game.h` UI/controller, gets `ab`, and selects initial target pairing.
- `game.d.b()` is the actual battle update loop once top-level state is `13`.
- `game.d.b(Graphics)` is the battle renderer.
- `game.d.a(byte)` is the battle-local substate machine; its `20/22/23` values are internal battle/UI states.

Conclusion:

- Port `game.i` state `12` as a `BattleEntryTransition`.
- Port `game.d` as the battle engine child that starts running only after state `13`.
- Keep field `game.d.b` because it chooses the entry effect.

### 5.3 SMS/payment side effects in `an`

Audit status: Runtime/Core side effects are now identified enough to define a rebuild adapter.

Flow:

```text
c(byte packageId):
  select payment package
  set required count q = 1
  reset current count p = 0

g(1):
  set T=true
  d(4)                  // sending/waiting UI
  a()                   // opens ag sms adapter, then sends/starts package

callback a(boolean) / b(boolean):
  if l == 4 and success:
    increment p and m[o]
    if p >= q: execute package side effect
    d(2)                // success UI
  if l == 4 and failure:
    d(3)                // failure UI
```

Exact success side effects are documented in the payment package table above.

Rebuild decision:

- MIDP build can later wire a real SMS/payment provider if needed.
- Java SE/debug build should expose a deterministic `PaymentService` with `confirm(packageId)` and `complete(success)` so tests and gameplay can trigger the same mutations.

### 5.4 Timer repaint 200ms (`an.s()` + `as`)

Audit status: closed for Runtime/Core.

Mechanism:

- `an.s()` creates a `Timer` and schedules `as.run()` every 200ms after an initial 10ms delay.
- `as.run()` calls only `game.e.a().repaint()`.
- `an.t()` cancels the timer and sets static loading flag `k=true`.
- `an.u()` sets `k=false`.
- `an.v()` returns `k`.

Why it matters:

- The main loop already repaints every 66ms, but synchronous resource loads can block normal update/render.
- The extra timer tries to keep the loading screen repainting during those loads.
- More importantly, the same helper carries the loading-complete flag used by `game.i`.

Port rule:

- In a rebuild, preserve the semantic flag exactly.
- The 200ms repaint pulse can be optional in a Java SE renderer if loading happens on the render thread and screens are manually repainted, but the MIDP-compatible target should keep the pulse behavior.

## 6. Runtime/Core Residual Risks

These are no longer blockers for the Runtime/Core skeleton, but still matter for full-game parity:

- Exact visual pixel parity of effect ids `6/7/8/19` belongs to effect renderer `b.a()`.
- Full battle behavior belongs to the `game.d` gameplay/battle audit.
- Full world travel meaning of top-level state `23` depends on `game.k` action/event audit.
- Real carrier SMS behavior is not needed for offline rebuild, but save/event mutation after payment must be preserved.
