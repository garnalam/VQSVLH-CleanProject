# 90 Battle Bunny Pre-P17 Trace Order Compare

## Scope

Audit-only compare for the Bunny tutorial route before the first P17 catch roll.

Goal:
- Compare source RNG call order against the current rebuild trace order.
- Decide whether the next slice can change RNG formula/stream, or whether more
  source mapping is still needed.
- Keep this route-specific: Bunny first forced-fail tutorial catch, not generic
  full-battle RNG parity.

No runtime code was changed for this audit.

## Files Read

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/ae.java`
- `modules/script/decoded/data__script__db.mid.json`
- `rebuild_game/src/main/java/VqsvBattleUnit.java`
- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`
- `rebuild_plan/88_battle_bunny_p17_rng_route_order_matrix.md`
- `rebuild_plan/89_battle_bunny_pre_p17_damage_rng_matrix.md`

## Source RNG Helper Contract

Source `ae` owns one private static global `Random f`.

Confirmed helpers:

| Helper | Source formula | Status |
|---|---|---|
| `ae.a(int n)` | `(f.nextInt() >>> 1) % n` | PROVED |
| `ae.a()` | `-2 + (f.nextInt() >>> 1) % 4` | PROVED |
| `ae.b(int min, int max)` | `(f.nextInt() >>> 1) % (max - min + 1) + min` | PROVED |

This audit focuses on call order. Exact seed lifecycle and full stream parity
remain outside this slice.

## Source Damage Order

Source method: `game.b.b(b target)`.

For one damaging skill, source RNG consumers appear in this order:

| Order | Source line | Purpose | Conditional |
|---:|---:|---|---|
| 1 | `game/b.java:1267` | critical hit check | Always for damage calculation |
| 2 | `game/b.java:1375` / `1381` | debuff apply gate | Only if skill maps to a target debuff |
| 3 | `game/b.java:1436` | target buff 6 damage reduction | Only if target has buff 6 |
| 4 | `game/b.java:1456` | damage jitter | If damage is positive |
| 5 | `game/b.java:1469` | target buff 5 reflect/store | Only if target has buff 5 |

Classification: PROVED for generic source damage order.

## Bunny Route Active Skill

Focused smoke `battle_bunny_pre_p17_rng_trace` reports:

```text
RNG TRACE battle.P7.skill10.damage.crit ...
RNG TRACE battle.P7.skill10.damage.jitter ...
RNG TRACE battle.P17.catch ...
```

Decoded `db.mid` group 1 row 10:

```text
[1,127,539,100,0,45,0,-1,-1,0]
```

Using rebuild `BattleSkillRow` mapping:

| Field | Value | Meaning |
|---|---:|---|
| `id` | `10` | Row index used by battle runtime |
| `powerPercent` | `100` | Normal damage scale |
| `effectMode` | `0` | No special effect mode for this compare |
| `effectId` | `-1` | No target debuff effect |
| `chanceOrParam` | `-1` | No debuff chance/param |

Therefore for the current Bunny first-P17 route, the active source path should
not consume the debuff RNG slot between crit and jitter.

Classification: PROVED for the current rebuild-smoked route, not for every
possible Bunny skill sequence.

## Source Expected Route Order

For skill 10 with no target buff 6 and no target buff 5:

```text
P7 damage enter
-> game.b.b(target)
   -> ae.a(100) crit
   -> no debuff RNG because effectId == -1
   -> no buff6 RNG unless target has buff 6
   -> ae.a(100) jitter
   -> no buff5 RNG unless target has buff 5
-> Bunny weak/tutorial P20/P21 flow
   -> no RNG in P20 entry
   -> no RNG in P21 list build/confirm
   -> no RNG in tutorial l()/m() gate
-> P17 entry
   -> ae.a(100) catch roll
   -> force fail if U == 0 && V == 5
```

Route-specific expected order:

```text
damage.crit -> damage.jitter -> P17.catch
```

Classification: PROVED/PARTIAL.

Why partial:
- The order is source-proved for skill 10 with no active buff5/buff6 branch.
- The full source stream before this point is still not proven from boot to
  battle entry.

## Rebuild Observed Trace

PNG-only smoke command:

```text
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-checkpoint battle_bunny_pre_p17_rng_trace
```

Observed output:

```text
smoke-rng-trace-order [
  RNG TRACE battle.P7.skill10.damage.crit helper=BattleUnit.randomPercent formula=Random.nextInt(100) raw=94 return=94,
  RNG TRACE battle.P7.skill10.damage.jitter helper=BattleUnit.randomPercent formula=Random.nextInt(100) raw=68 return=68,
  RNG TRACE battle.P17.catch seed=1783443595724 source=System.currentTimeMillis,
  RNG TRACE battle.P17.catch helper=ae.a(int) bound=100 raw=781455514 return=57 seed=runtime
]
```

Smoke result:

```text
smoke-checkpoint-ok battle_bunny_pre_p17_rng_trace
```

PNG:

```text
rebuild_game/build_intro_demo/smoke_checkpoint.png
```

Classification: VERIFIED by focused PNG-only smoke.

## Compare Matrix

| Step | Source expected | Rebuild observed | Status |
|---:|---|---|---|
| 1 | `ae.a(100)` crit | `battle.P7.skill10.damage.crit` | ORDER MATCH / FORMULA APPROX |
| 2 | no debuff RNG for skill 10 `effectId=-1` | no `damage.debuff` trace | MATCH |
| 3 | no buff6 RNG if target has no buff6 | no `damage.buff6` trace | MATCH for current state |
| 4 | `ae.a(100)` jitter | `battle.P7.skill10.damage.jitter` | ORDER MATCH / FORMULA APPROX |
| 5 | no buff5 RNG if target has no buff5 | no `damage.buff5` trace | MATCH for current state |
| 6 | P20/P21/tutorial no RNG | no trace between jitter and P17 seed | MATCH for local route |
| 7 | P17 entry `ae.a(100)` catch | `battle.P17.catch helper=ae.a(int)` | ORDER MATCH / STREAM PARTIAL |

## Remaining Deltas

| Area | Current state | Why it matters |
|---|---|---|
| Damage RNG formula | APPROX | Rebuild uses `Random.nextInt(100)`, source uses `(nextInt() >>> 1) % 100`. |
| Damage RNG stream | APPROX | Rebuild damage uses `BattleUnit.BATTLE_RANDOM`; source damage and catch share global `ae.f`. |
| P17 RNG stream | PORTED/PARTIAL | P17 uses `VqsvSourceRandom.a(...)`, source-shaped helper, but not yet unified with all prior source consumers. |
| Seed lifecycle | PENDING | Source seed is `System.currentTimeMillis()` lazy global; rebuild has separate deterministic battle RNG plus source-random harness. |
| Full route pre-battle consumers | PENDING | World/boot/encounter RNG before this battle is not fully traced. |
| P17 animation/UI pixel parity | PENDING | This audit only compares RNG trace order. |

## Conclusion

For the currently smoked Bunny tutorial route, trace order is source-consistent:

```text
damage.crit -> damage.jitter -> P17.catch
```

The big remaining mismatch is not order for this route. It is formula and stream:

- `damage.crit` and `damage.jitter` are still `BattleUnit.randomPercent`
  using Java `Random.nextInt(100)`.
- Source expects those rolls to be `ae.a(100)` on the same global stream as
  P17 catch.

## Recommended Next Slice

Smallest safe next code slice:

1. Route-local behavior change only:
   - replace `BattleUnit.randomPercent()` formula with a source-shaped helper
     for battle damage RNG, or route it through `VqsvSourceRandom`;
   - preserve trace labels and raw/return logging.
2. Keep scope narrow:
   - do not change P20/P21 tutorial gating;
   - do not change P17 animation/UI;
   - do not claim exact seed parity.
3. After code, run:
   - `build.ps1`
   - `com.vqsv.rebuild.Main --check`
   - `VqsvBattleDamageFormulaCheck`
   - mojibake scan Java source
   - `git diff --check`
   - focused PNG checkpoint `battle_bunny_pre_p17_rng_trace`
   - catch fail/success PNG checkpoints
   - Bunny/Sophie/Elder route regressions

## Safety Statement

Do not claim:
- exact full-game RNG parity;
- exact source seed parity;
- pixel-perfect P17 UI/animation.

Current honest status:

| Concern | Status |
|---|---|
| Bunny pre-P17 active trace order | VERIFIED / PORTED-PARTIAL |
| Source generic damage RNG order | PROVED |
| Skill 10 no-debuff branch | PROVED |
| Damage RNG formula parity | APPROX |
| Damage/P17 shared global stream parity | PENDING |
| Full boot-to-catch RNG stream parity | PENDING |
