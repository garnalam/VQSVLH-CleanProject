# 89 Battle Bunny Pre-P17 Damage RNG Matrix

## Scope

Audit source and rebuild RNG consumers before Bunny reaches the weak/catch
tutorial gate.

Goal:
- Identify source `ae.f` consumers in battle damage/status logic.
- Compare rebuild `BattleUnit.randomPercent()` and related P7 runtime calls.
- Decide whether the next change should be trace-only labels or behavior port.

This is audit-only. No runtime code changed in this step.

## Files Read

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `rebuild_game/src/main/java/VqsvBattleUnit.java`
- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

## Source Damage RNG Contract

Source damage method: `game.b.b(b target)`.

Confirmed source RNG consumers inside one damage application:

| Source line | Purpose | Source helper | Notes |
|---:|---|---|---|
| `game/b.java:1267` | critical hit check | `ae.a(100) <= critChance` | Crit chance starts at 5, can become 30 for final visual, plus speed/2 and status 4 bonus. |
| `game/b.java:1375` | debuff apply when target has form/status 3 | `ae.a(100) <= adjustedChance` | Status 3 reduces debuff chance. Decompiled branch is awkward but still an RNG gate. |
| `game/b.java:1381` | normal debuff apply | `ae.a(100) > chance` | If roll exceeds chance, no debuff. |
| `game/b.java:1436` | target buff 6 damage reduction | `ae.a(100) <= this.v[6][1]` | Applies defensive damage scale. |
| `game/b.java:1456` | damage jitter | `ae.a(100)` | Used to +/-1 when 2% delta rounds to 0. |
| `game/b.java:1469` | target buff 5 reflect/store check | `ae.a(100) <= target.v[5][1]` | Stores damage for later effect. |

Status: PROVED.

Important: every source call above consumes the same process-wide `ae.f` stream.
Exact catch RNG parity after Bunny weak gate depends on whether any of these
calls happen before P17 in the tested route.

## Source P7 Runtime RNG Outside `game.b`

Source P7 enter method: `game.d.a(byte)` case `7`.

Special skill gate:

| Source line | Purpose | Source helper | Notes |
|---:|---|---|---|
| `game/d.java:837` | skill 52/58 chance gate | `ae.a(100) > 30` | If roll > 30, `aa=false`; otherwise `aa=true`. |

Source post-damage effect:
- `game/d.java:2000-2002` applies skill 52/58 leech/heal only if `aa` is true.
- `game/d.java:2380` shows `W()` can also use `!aa` in completion/flow logic.

Status: PROVED.

This RNG is not inside `game.b.b(target)`, but it can happen before Bunny catch if
Bunny route uses skill 52 or 58 before the weak gate.

## Rebuild Damage RNG Matrix

Rebuild owner: `rebuild_game/src/main/java/VqsvBattleUnit.java`.

Current random source:
- `private static final Random BATTLE_RANDOM = new Random(0x56515356L);`
- `randomPercent()` returns `BATTLE_RANDOM.nextInt(100)`.

This is not source-shaped `ae.a(100)`, because source uses
`(nextInt() >>> 1) % 100` on the global `ae.f` stream.

| Rebuild line | Purpose | Current RNG | Source equivalent | Status |
|---:|---|---|---|---|
| `VqsvBattleUnit.java:308` | critical hit check | `randomPercent() <= critChance` | `game/b.java:1267` | PORTED/PARTIAL |
| `VqsvBattleUnit.java:352` | target buff 6 damage reduction | `randomPercent() <= buffSlots[6][1]` | `game/b.java:1436` | PORTED/PARTIAL |
| `VqsvBattleUnit.java:377` | damage jitter | `randomPercent()` | `game/b.java:1456` | PORTED/PARTIAL |
| `VqsvBattleUnit.java:391` | target buff 5 reflect/store | `randomPercent() <= target.buffSlots[5][1]` | `game/b.java:1469` | PORTED/PARTIAL |
| `VqsvBattleUnit.java:962` | debuff apply with target form/status 3 | `randomPercent() > adjustedChance` | `game/b.java:1375` | PORTED/PARTIAL |
| `VqsvBattleUnit.java:967` | normal debuff apply | `randomPercent() > chance` | `game/b.java:1381` | PORTED/PARTIAL |

Status detail:
- Logic shape is mostly source-mapped.
- RNG formula and stream are not source-global.
- There is no trace label per callsite yet.

Classification: PORTED/PARTIAL for logic, APPROX/MISSING TRACE for exact RNG.

## Rebuild P7 Skill 52/58 Gate

Current rebuild location:
- `VqsvBattleRuntime.applyP7SourcePostDamageModifiers()`
- cases `52` and `58` use `p7Attacker.battleUnit.rollSourceChance(30)`.
- `rollSourceChance(30)` delegates to `BattleUnit.randomPercent() <= chance`.

Comparison:

| Concern | Source | Rebuild | Status |
|---|---|---|---|
| Formula | `ae.a(100) > 30` fails, so pass is roll `0..30` | `randomPercent() <= 30` | PORTED/PARTIAL |
| Stream | source global `ae.f` | `BATTLE_RANDOM` local stream | APPROX/MISSING TRACE |
| Timing | P7 entry `game.d.a(7)` before damage/effect flow | post-damage modifier after `p7DamageApplied` | ORDER MISMATCH/PENDING |
| Effect use | source checks `aa` at post effect | rebuild rolls at post effect | PARTIAL |

This matters for exact RNG stream because moving the same roll later changes the
global consumer order if other RNG calls occur between entry and post effect.

## Bunny Route Risk Matrix

Current Bunny smoke driver:
- starts `SourceBattleRuntime(50, new int[]{34, 5, 1}, ...)`;
- seeds player pet from source room0 group0 reward;
- auto-selects fight before catch until Bunny HP <= 50%;
- after weak gate, it follows P20/P21/P17 tutorial route.

The exact skill used before the weak gate is selected by rebuild from the
current active unit's first available skill unless smoke input chose another
skill. Therefore the RNG risk depends on the active skill and current statuses.

| Pre-P17 condition | Source RNG possible? | Rebuild current | Risk |
|---|---:|---|---|
| Any damaging skill before Bunny weak gate | Yes, crit + jitter at minimum | `BattleUnit.randomPercent()` | HIGH for exact stream |
| Skill has target debuff chance | Yes, debuff roll | `maybeApplyTargetDebuff()` | HIGH when skill effect id maps to debuff |
| Target has buff 6 | Yes | `target.hasBuff(6)` branch | CONDITIONAL |
| Target has buff 5 | Yes | `target.hasBuff(5)` branch | CONDITIONAL |
| Skill 52/58 used before weak gate | Yes, P7 `aa` gate | post-effect `rollSourceChance(30)` | HIGH/order mismatch |
| No-damage skill before weak gate | Maybe no damage RNG, but P7/source effect may still matter | partial P7 no-damage path | NEED ROUTE-SPECIFIC PROOF |

Conclusion:
- Audit 88 proved local P20/P21/P17 does not add extra RNG before P17 except
  the P17 catch roll.
- This audit proves the remaining pre-P17 uncertainty is mainly the damage/P7
  phase that gets Bunny to <=50% HP.

## Current Classification

| Area | Status | Notes |
|---|---|---|
| Source `game.b.b(target)` RNG callsites | PROVED | Six `ae.a(100)` consumers identified. |
| Source skill 52/58 P7 gate | PROVED | One `ae.a(100)` in `game.d.a(7)`. |
| Rebuild damage RNG logic shape | PORTED/PARTIAL | Main source branches exist. |
| Rebuild damage RNG stream | APPROX | Uses `BATTLE_RANDOM.nextInt(100)`, not global `ae.f`. |
| Rebuild damage RNG trace labels | MISSING | No per-callsite label/raw/return trace. |
| Rebuild skill 52/58 gate timing | PARTIAL/PENDING | Formula similar, roll occurs later than source. |
| Bunny exact RNG before catch | PENDING | Need route-specific trace through the actual skill sequence before weak gate. |

## Recommended Next Slice

Do not replace `BATTLE_RANDOM` with `VqsvSourceRandom` yet.

Implemented after this audit:
1. Added trace-only support to `BattleUnit.randomPercent(label)`:
   - label;
   - raw/return value;
   - callsite category such as `damage.crit`, `damage.debuff`, `damage.jitter`,
     `damage.buff6`, `damage.buff5`, `skill52_58.leechGate`;
   - no behavior change: it still uses `BATTLE_RANDOM.nextInt(100)`.
2. Threaded an optional trace sink from `SourceBattleRuntime.applyP7Damage()` and
   `applyP7PostSkillEffects()` into the active `BattleUnit` calls.
3. Added focused PNG checkpoint `battle_bunny_pre_p17_rng_trace`, which drives
   Bunny to first P17 and asserts the trace contains:
   - `damage.crit`;
   - `damage.jitter`;
   - `battle.P17.catch`.

Next after trace:
- Compare the route call order against source before deciding whether to change
  formulas/stream ownership.

Safety statement:
- Do not touch P20/P21/P17 behavior for this slice.
- Do not claim exact RNG parity.
- Do not claim pixel-perfect P17 animation/UI.
- Keep changes trace-only until source order and rebuild order are both visible.
