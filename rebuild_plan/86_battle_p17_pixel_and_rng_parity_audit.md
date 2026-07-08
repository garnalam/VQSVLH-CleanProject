# 86 Battle P17 Pixel And RNG Parity Audit

## Scope

User request:
- Prove source RNG class/seed lifecycle `ae.f` across the whole game, not only catch.
- Start the audit needed before any claim of P17 pixel-perfect animation/UI or exact RNG seed stream parity.

This file is audit-only. No runtime claim is made here.

## Sources Read

- `modules/source_code/decoded/decompiled_source_cfr/ae.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/a.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/f.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/i.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- Current rebuild catch/runtime docs and smokes.

## Source RNG Lifecycle

### RNG field

Source: `ae.java`.

Relevant source:

```java
private static Random f;
```

Status: PROVED.

`ae.f` is a private static field owned by utility class `ae`. It is not per-battle,
not per-scene, and not per-catch. It is one process-wide RNG stream for all call
sites that use the random helpers below.

### Lazy initialization

Source: `ae.java`.

Random helper methods:

```java
public static int a(int n2) {
    if (f == null) {
        f = new Random(System.currentTimeMillis());
    }
    return (f.nextInt() >>> 1) % n2;
}

public static int a() {
    if (f == null) {
        f = new Random(System.currentTimeMillis());
    }
    return -2 + (f.nextInt() >>> 1) % 4;
}

public static int b(int n2, int n3) {
    if (f == null) {
        f = new Random(System.currentTimeMillis());
    }
    return (f.nextInt() >>> 1) % (n3 - n2 + 1) + n2;
}
```

Status: PROVED.

Properties:
- First call to any of these three helpers seeds `ae.f` from `System.currentTimeMillis()`.
- After first initialization, all three helpers consume the same `Random f` stream.
- No source assignment to `ae.f` was found outside these lazy-init branches.
- No source reset or seed setter for `ae.f` was found.
- `ae.a(100)` returns `0..99`.
- `ae.a()` returns `-2..1`.
- `ae.b(min,max)` returns inclusive `min..max`.

### Other `new Random`

Repo scan found other direct `new Random()` usage outside `ae`:
- `modules/source_code/decoded/decompiled_source_cfr/a/a.java`
- `modules/source_code/decoded/decompiled_source_cfr/a/g.java`

Status: PROVED/SEPARATE.

These are not `ae.f`. They may affect their own subsystem, but they do not consume
the shared `ae.f` stream unless they call back into `ae`.

## Whole-Game `ae.f` Consumers

The following source areas consume the shared `ae.f` stream. This matters because
exact catch RNG parity cannot be proved by looking only at P17.

### Battle Runtime Consumers

Source examples:

- `game.d case 17`: catch result uses `this.ak = ae.a(100) < chance`.
- `game.d`: battle/status probability checks use `ae.a(100)` in multiple phases.
- `game.d`: target/queue random selection uses `ae.a(this.h.G.size())`.
- `game.d`: skill/action selection uses `ae.a(100)` and `ae.a(...)`.
- `game.b`: damage/status/debuff/passive checks use `ae.a(100)`.
- `game.b`: species quality fallback uses `ae.b(aq.c[0][species][3], aq.c[0][species][3])`.

Status: PROVED.

Implication: P17 catch roll is downstream of every previous battle random call in
the same process.

### World / Actor / Encounter Consumers

Source examples:

- `game.a`: NPC/world actor behavior uses `ae.a(5)`, `ae.a(4)`, `ae.a(2)`,
  and `ae.b(20, 40)`.
- `game.f`: menu/background particle setup uses `ae.a(...)`, `ae.b(1,5)`,
  and `ae.b(3,5)`.
- `game.g`: world random range helpers use `ae.b(4,8)` and `ae.b(this.ad,this.ae)`.
- `game.k`: random encounter vectors use `ae.a(vector.size())` and coordinate
  ranges use `ae.b(...)`.
- `game.i`: logo/title random selection uses `ae.a(this.q.length)` and
  `ae.a(this.p.length)`.
- `game.h`: reward/egg/purchase and chance checks use `ae.a(100)`, `ae.b(...)`,
  and weighted choice helpers fed by `ae.a(100)`.

Status: PROVED.

Implication: exact `ae.f` stream parity is a full-game problem. Intro, menu,
world movement, NPC behavior, random encounters, battle AI, status checks, and
catch all share the same RNG stream.

### Grep Counts

These are audit hints, not exact overload resolution proof:

- `ae.a(100)`: 21 matches.
- `ae.a(2)`: 5 matches.
- `ae.a(4)`: 1 match.
- `ae.a(5)`: 1 match.
- `ae.a()`: 1 match.
- `ae.b(...,...)`: 37 textual matches, including both random range calls and
  non-random overloads such as resource/table helpers. Each call must be checked
  by argument type before treating it as `ae.b(int,int)`.

Status: PROVED/PARTIAL. The lifecycle is proved; exhaustive overload-classified
call graph is still PENDING.

### Line-Backed Consumer Matrix

This matrix records confirmed `ae.f` consumers by source line. It is not a full
control-flow graph yet, but each row below calls one of the three RNG helpers
defined in `ae.java`.

| Area | Source line(s) | RNG helper | Meaning / impact |
|---|---:|---|---|
| RNG owner | `ae.java:23` | `private static Random f` | Single private static stream. |
| RNG helper | `ae.java:292-295` | `ae.a(int)` | Lazy init, then `(nextInt() >>> 1) % n`. |
| RNG helper | `ae.java:299-302` | `ae.a()` | Lazy init, then `-2 + ... % 4`. |
| RNG helper | `ae.java:306-309` | `ae.b(int,int)` | Lazy init, inclusive range. |
| Battle P17 catch | `game/d.java:892` | `ae.a(100)` | Catch success roll. |
| Battle phase/chance | `game/d.java:837`, `1081`, `1127`, `1308`, `1336`, `1519`, `2019`, `2134`, `2174` | `ae.a(100)` | Pre-catch battle checks can advance the same stream. |
| Battle target queue | `game/d.java:1316`, `1326` | `ae.a(this.h.G.size())` | Random selection from battle queue/list. |
| Battle unit logic | `game/b.java:1267`, `1375`, `1381`, `1436`, `1456`, `1469` | `ae.a(100)` | Damage/status/passive probability checks. |
| Battle unit generation | `game/b.java:69` | `ae.b(aq.c[0][...][3], ...)` | Random range helper, even when current args are equal. |
| Root battle/visual helper | `b.java:854`, `953`, `958`, `1028` | `ae.a(2)`, `ae.a(100)`, `ae.a()`, `ae.b(0,7)` | Non-`game` battle/visual setup also consumes `ae.f`. |
| Actor/world behavior | `game/a.java:83`, `246`, `247`, `270`, `271`, `311`, `558` | `ae.b(20,40)`, `ae.a(5/4/2)` | NPC timing/direction/random actor behavior. |
| Menu background | `game/f.java:62-66` | `ae.a(this.j)`, `ae.a(this.k)`, `ae.a(2)`, `ae.b(1,5)`, `ae.b(3,5)` | Menu/particle initialization can consume stream before gameplay. |
| Logo/title | `game/i.java:139`, `146` | `ae.a(this.q.length)`, `ae.a(this.p.length)` | Startup/logo selection can consume stream before world/battle. |
| Encounter selection | `game/k.java:1868`, `1876`, `1884`, `1892`, `1906`, `1912` | `ae.a(vector.size())`, `ae.b(...)` | Random encounter vector/coordinate selection. |
| World range helper | `game/g.java:1731`, `1733` | `ae.b(4,8)`, `ae.b(this.ad,this.ae)` | World random range helper. |
| Reward/pet/shop systems | `game/h.java:3251`, `3311`, `3315`, `3794`, `3802` | `ae.a(100)`, `ae.b(...)` | Weighted choice, pet quality/stat, purchase/chance checks. |
| Script/pet creation path | `game/c.java:1479` | `ae.b(aq.c[0][54][3], ...)` | Scripted pet value generation path. |

### Overload Guardrails

`ae` overloads the same method names heavily. Only the following three helper
signatures consume `ae.f`:

- `ae.a(int)`
- `ae.a()`
- `ae.b(int,int)`

The following commonly matched overloads do not consume `ae.f` by themselves:

- `ae.b(InputStream)` at `ae.java:147`: table loader.
- `ae.b(String,String)` at `ae.java:357`: image/resource loader.
- `ae.b(int)` at `ae.java:539`: allocates/returns an `int[]`.
- `ae.b(String)` at `ae.java:921`: parser/string-to-int helper.

Status: PROVED for the overload definitions above. Any future grep matrix must
separate these overloads before counting RNG consumers.

## Current Rebuild RNG Status

Current rebuild catch decision:

- Uses source-shaped comparison `roll 0..99 < catchChance`.
- Has deterministic smoke hook for roll 99 fail and roll 0 success.
- Uses rebuild-side RNG for normal runtime catch roll.

Status:
- Catch decision shape: PORTED.
- Exact source RNG seed stream: PENDING.

Reason:
- Source `ae.f` is process-wide and seeded by first random helper call.
- Rebuild catch RNG is not the same object as full-game `ae.f`.
- Rebuild does not yet model every earlier `ae.f` consumer in exact order.
- Deterministic smoke hooks prove decision behavior, not exact seed-stream parity.

## P17 Source Animation/UI Chain

### P17 entry

Source: `game.d.a(byte)` case `17`.

Observed:
- `b b3 = this.d[0]`
- `this.h.p = b3`
- lazily creates `this.aj = new f()`
- loads `this.aj.a(269, false)`
- positions `this.aj.b(this.h.i, this.h.j)`
- starts `this.aj.c()`
- enters q0 via `this.e((byte)0)`
- computes `int chance = this.b((int)l)`
- decides `this.ak = ae.a(100) < chance`
- tutorial edge `if (U == 0 && V == 5) this.ak = false`
- clears UI flag `this.S.f = 0`

Status: PORTED/PARTIAL in rebuild.

### P17 tick phases

Source: `game.d` state tick case `17`.

Observed:
- q0: if `aj.b()` then `e(1)`.
- q1: if `aj.b()` and `!H.e()` then `e(2)`.
- q2: if `aj.b()` then branch to q3 when `ak == true`, else q4.
- q3 success: if `aj.b()` then `game.d.p.y()` decides storage:
  - `0`: openbox success, add to bag via `game.d.p.a(...)`.
  - `1`: openbox success, add to bank via `game.d.p.b(...)`, then follow-up
    text "Sủng vật ba lô đã đủ, đã để vào ngân hàng".
  - else: release text "Không còn không gian...".
- q4 fail: if `aj.b()` and `!H.e()`, clears H, shows enemy, disposes `aj`, then
  continues battle dispatch.
- Every active tick calls `this.aj.a()`.
- If `this.S.f != 0`, waits for text/UI confirm via `S.ax()` then continues.

Status: PORTED/PARTIAL in rebuild.

### P17 render order

Source: `game.d` render case `17`.

Observed:
- Draws battle background/unit layers with `this.a(graphics, false)` and
  `this.a(graphics)`.
- If `H != null && H.c()`:
  - draw `H`
  - draw `aj`
- Else:
  - draw `aj`
- Then shared UI renderer `R.a(graphics)` is called after state render.

Status: PORTED/PARTIAL in rebuild.

Pixel-perfect status:
- No original-vs-rebuild capture pair exists yet for the same P17 frame/tick.
- No per-frame pixel diff has been run for q0/q1/q2/q3/q4.
- Therefore pixel-perfect is PENDING.

## What Would Be Required To Claim Exact RNG Stream

Minimum requirements:

1. Implement/source-map a rebuild equivalent of `ae.f` with the exact three random
   helpers:
   - `a(int n) => (nextInt() >>> 1) % n`
   - `a() => -2 + (nextInt() >>> 1) % 4`
   - `b(min,max) => (nextInt() >>> 1) % (max-min+1) + min`
2. Decide how to handle `System.currentTimeMillis()`:
   - exact live parity is impossible unless the original and rebuild are seeded
     at the same millisecond and consume the same call sequence;
   - test parity requires injecting/capturing the original seed.
3. Audit and port every `ae.f` consumer before P17 in the tested route:
   - logo/title RNG;
   - world/actor RNG;
   - encounter RNG;
   - pet generation RNG;
   - battle AI/status/damage RNG;
   - catch RNG.
4. Instrument RNG call trace:
   - call site label;
   - method (`a(int)`, `a()`, `b(int,int)`);
   - bound/range;
   - raw `nextInt()`;
   - returned value;
   - frame/state.
5. Compare original trace vs rebuild trace before comparing catch result.

Until this exists, exact RNG stream parity remains PENDING.

## What Would Be Required To Claim P17 Pixel-Perfect

Minimum requirements:

1. Original capture:
   - same battle setup;
   - same selected ball;
   - same catch success/fail decision;
   - same q0/q1/q2/q3/q4 frame ticks.
2. Rebuild capture:
   - same logical resolution;
   - same frame timing;
   - same sprite 269 state/cursor;
   - same H/ah type8 state/cursor/offset/scale.
3. Pixel compare:
   - per-frame PNG diff;
   - mask only if original has platform-specific font/alpha differences and the
     mask is explicitly documented;
   - report max diff/count/regions.
4. Verify UI text sequence:
   - success add bag;
   - success add bank + follow-up bank text;
   - storage full release;
   - fail return to battle.

Until original-vs-rebuild frame compare exists, P17 pixel-perfect remains PENDING.

## Current Classification

| Area | Status | Notes |
|---|---|---|
| `ae.f` field lifecycle | PROVED | Private static, lazy initialized by random helpers only. |
| `ae.a(int)` formula | PROVED | `(nextInt() >>> 1) % n`. |
| `ae.a()` formula | PROVED | `-2 + (nextInt() >>> 1) % 4`. |
| `ae.b(int,int)` formula | PROVED | Inclusive random range. |
| Whole-game RNG sharing | PROVED | Battle, world, menu/logo, actor, reward systems consume `ae.f`. |
| Exhaustive overload-classified RNG call graph | PENDING | Grep over-approximates overloaded `ae.b`. |
| Catch decision shape | PORTED | `roll < chance`, force-fail tutorial edge handled. |
| Exact catch RNG stream | PENDING | Full `ae.f` stream and seed lifecycle not rebuilt globally. |
| P17 animation source chain | PORTED/PARTIAL | q0..q4 source chain known; rebuild smoke exists. |
| P17 pixel-perfect | PENDING | No original-vs-rebuild frame diff yet. |

## Recommended Next Step

Do not try to claim exact RNG or pixel-perfect yet.

Recommended next concrete slice:
- Create `VqsvSourceRandom` audit/test harness only, not wire it broadly yet.
- Add a trace-only comparison mode for rebuild RNG calls.
- Start with a narrow route from battle entry to P17, logging current rebuild RNG
  consumers and identifying every missing source `ae.f` consumer before catch.
