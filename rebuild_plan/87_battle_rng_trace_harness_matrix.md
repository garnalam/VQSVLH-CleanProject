# 87 Battle RNG Trace Harness Matrix

## Scope

Current slice:
- Document the trace-only `VqsvSourceRandom` harness.
- List source RNG callsites that must be mapped before claiming exact P17 catch
  RNG stream parity.
- Record which rebuild callsites are currently traced, approximate, missing, or
  intentionally not wired yet.
- Define focused PNG-only smoke coverage after the trace hook.

Out of scope:
- Do not claim exact global RNG parity.
- Do not claim P17 pixel-perfect animation/UI.
- Do not wire `VqsvSourceRandom` broadly into unrelated runtime paths until their
  source call order is proven.

## Source RNG Contract

Source owner: `modules/source_code/decoded/decompiled_source_cfr/ae.java`.

Confirmed helpers:

| Source | Formula | Status |
|---|---|---|
| `ae.a(int n)` | `(f.nextInt() >>> 1) % n` | PROVED |
| `ae.a()` | `-2 + (f.nextInt() >>> 1) % 4` | PROVED |
| `ae.b(int min, int max)` | `(f.nextInt() >>> 1) % (max - min + 1) + min` | PROVED |

Seed/lifecycle:
- `ae.f` is `private static Random f`.
- First helper call lazy-initializes `f = new Random(System.currentTimeMillis())`.
- The stream is process-wide, not battle-local or catch-local.
- No reset/seed setter was found in source.

Status: PROVED in audit 86.

## Rebuild Harness

File: `rebuild_game/src/main/java/VqsvSourceRandom.java`.

Implemented:
- `a(label, n, trace)` mirrors `ae.a(int)`.
- `a(label, trace)` mirrors `ae.a()`.
- `b(label, min, max, trace)` mirrors `ae.b(int,int)`.
- Runtime lazy seed uses `System.currentTimeMillis()`.
- Smoke/test can inject seed via `setSeed(long)`.
- Trace logs:
  - label;
  - helper;
  - bound/range;
  - raw `Random.nextInt()`;
  - return value;
  - seed source/value.

Status: PORTED for formula and trace harness.

Limit:
- Java `Random(System.currentTimeMillis())` live seed cannot match original unless
  original seed is captured/injected and all preceding consumers match exactly.

## Current Rebuild Hook Matrix

| Rebuild area | File / line | Current RNG | Source equivalent | Status | Notes |
|---|---:|---|---|---|---|
| P17 catch roll | `VqsvBattleRuntime.java:sourceCatchRollPercent` | `VqsvSourceRandom.a("battle.P17.catch", 100, trace)` | `game.d.java:892 ae.a(100)` | PORTED/PARTIAL | Formula and trace are source-shaped. Global stream order is not proven. |
| P17 forced smoke roll | `VqsvBattleRuntime.java:sourceCatchRollPercent` | debug forced value | smoke-only override | PORTED/TEST-ONLY | Does not consume RNG; logs `helper=debug-forced`. |
| P17 seeded smoke | `VqsvSmokeHarness.java:battle_rng_trace_p17_catch` | injected seed | deterministic test harness | PORTED/TEST-ONLY | Proves raw/return trace for one seeded P17 roll. |
| Battle damage/status random | `VqsvBattleUnit.java:randomPercent` | `BATTLE_RANDOM.nextInt(100)` | mostly `game.b.java ae.a(100)` callsites | APPROX/MISSING TRACE | Separate RNG and Java `nextInt(100)` formula do not mirror `ae.a(100)`. Must not be used for exact parity claims. |
| Boot/logo/menu RNG | `BootFlowState.java` | local `Random(System.currentTimeMillis())`, source-shaped formulas | `game.i`, `game.f` | APPROX/MISSING TRACE | Separate stream, no `VqsvSourceRandom` trace yet. |
| Visual effect RNG | `VqsvEffect.java` | local `Random(7)` | source effect/menu/world varies | APPROX/MISSING TRACE | Visual deterministic helper, not mapped to `ae.f`. |

## Source Callsites To Map Before P17 Catch

This is a route-order matrix, not a full call graph. Each source callsite below
can advance the same `ae.f` stream before `game.d.java:892` catches.

| Source area | Source lines | Helper | Rebuild status | Required before exact parity |
|---|---:|---|---|---|
| Logo/title selection | `game/i.java:139`, `146` | `ae.a(length)` | MISSING TRACE / APPROX | Decide if tested route enters these source states; if yes, trace/order them. |
| Menu background particles | `game/f.java:62-66` | `ae.a(...)`, `ae.b(1,5)`, `ae.b(3,5)` | MISSING TRACE / APPROX | Map startup/menu consumers or isolate route from them with captured seed boundary. |
| World actor behavior | `game/a.java:83`, `246`, `247`, `270`, `271`, `311`, `558` | `ae.b(20,40)`, `ae.a(5/4/2)` | MISSING TRACE | Needed if world ticks before catch route are included. |
| World range helper | `game/g.java:1731`, `1733` | `ae.b(4,8)`, `ae.b(this.ad,this.ae)` | MISSING TRACE | Needed for source world/random area parity. |
| Encounter selection | `game/k.java:1868`, `1876`, `1884`, `1892`, `1906`, `1912` | `ae.a(vector.size())`, `ae.b(...)` | MISSING TRACE | Needed if random encounter generation is part of route. Current smoke starts battle directly. |
| Scripted pet creation | `game/c.java:1479` | `ae.b(aq.c[0][54][3], ...)` | MISSING TRACE | Needed for scripted pet generation parity. |
| Battle unit generation | `game/b.java:69` | `ae.b(aq.c[0][species][3], ...)` | MISSING TRACE | Rebuild uses fixed fixture stats in many smokes; exact generation pending. |
| Root battle/visual helper | `b.java:854`, `953`, `958`, `1028` | `ae.a(2)`, `ae.a(100)`, `ae.a()`, `ae.b(0,7)` | UNKNOWN/MISSING TRACE | Need classify whether this root `b.java` path is active for current battle route. |
| Battle pre-catch phase checks | `game/d.java:837`, `1081`, `1127`, `1308`, `1336`, `1519`, `2019`, `2134`, `2174` | `ae.a(100)` | MISSING TRACE / PARTIAL LOGIC | Must map each state branch that can run before P17. |
| Battle target queue | `game/d.java:1316`, `1326` | `ae.a(this.h.G.size())` | MISSING TRACE | Needed for enemy/active queue parity before catch. |
| Battle damage/status/passive | `game/b.java:1267`, `1375`, `1381`, `1436`, `1456`, `1469` | `ae.a(100)` | APPROX/MISSING TRACE | Current rebuild `BattleUnit.BATTLE_RANDOM.nextInt(100)` is separate. |
| Reward/pet/shop systems | `game/h.java:3251`, `3311`, `3315`, `3794`, `3802` | `ae.a(100)`, `ae.b(...)` | MISSING TRACE | Not on direct P17 smoke path unless route enters these systems first. |
| P17 catch decision | `game/d.java:892` | `ae.a(100)` | PORTED/PARTIAL | Current smallest traced hook. Exact parity still depends on all prior consumers. |

## Current Direct P17 Smoke Route

The focused `battle_rng_trace_p17_catch` smoke deliberately starts a direct
runtime battle fixture and injects seed at the rebuild P17 RNG boundary.

What it proves:
- `VqsvSourceRandom.a(label, 100, trace)` uses the same formula as source
  `ae.a(100)`.
- Trace records label, helper, bound, raw value, return value, and seed.
- P17 catch consumes this traced roll.

What it does not prove:
- Original route seed parity.
- Full process-wide `ae.f` consumer order.
- Any pre-P17 menu/world/battle random consumer parity.
- P17 animation pixel-perfect parity.

Status:
- Trace harness: PORTED.
- P17 catch trace hook: PORTED/PARTIAL.
- Exact RNG seed stream: PENDING.

## Focused PNG Smoke Plan

All commands must use PNG-only checkpoint entrypoint:

```powershell
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-checkpoint <checkpoint> <png>
```

Focused catch checks:
- `battle_rng_trace_p17_catch`
- `battle_catch_fail_or_warning`
- `battle_catch_generic_roll_success`

Bunny catch tutorial checks:
- `battle_bunny_first_catch_forced_fail`
- `battle_bunny_retry_p21_item0`
- `battle_bunny_retry_prompt_tasktip`
- `route_bunny_after_battle_task`

Route regressions:
- `route_sophie_after_battle_branch`
- `route_elder_after_battle_reward_state`

Optional wider Phase 5 catch regression:
- `battle_catch_missing_count_warning`
- `battle_catch_storage_bag`
- `battle_catch_storage_bank`
- `battle_catch_storage_full_release`
- `battle_catch_chance_status_multipliers`

## Recommended Next Slice

Do not wire global RNG broadly yet.

Recommended next small slice:
1. Add trace labels for current rebuild `BattleUnit.randomPercent()` callsites
   without changing behavior, or create a parallel audit-only log for these
   APPROX consumers.
2. Build a route-specific source order table from battle entry to P17 for Bunny
   tutorial:
   - P20 command;
   - P21 catch list;
   - first P17 forced-fail;
   - retry P21;
   - second P17 item0.
3. Only after source order is proven, decide whether to replace specific
   `BATTLE_RANDOM` calls with `VqsvSourceRandom`.

Safety statement:
- Files touched for current harness: `VqsvSourceRandom.java`,
  `VqsvBattleRuntime.java`, `VqsvSmokeHarness.java`.
- Do not touch intro/world/panel/runtime unless a source callsite proves it is on
  the battle route being mapped.
- Remaining exact RNG parity: PENDING.
- Remaining P17 pixel-perfect parity: PENDING.
