# 157 Battle Phase 9 Crit / Dodge / Miss Result Flow Audit

Status date: 2026-07-09

Status: PHASE 9 FIRST SLICE / IMPLEMENTED-PARTIAL.

Purpose:

- Audit critical-hit and dodge/miss result flow before broad skill coverage.
- Decide the first safe Phase 9 code slice.
- Avoid adding more skill-specific smoke while P7 still applies every damage
  result as a guaranteed hit.

## Source Files Read

Primary source:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`

Current rebuild:

- `rebuild_game/src/main/java/VqsvBattleUnit.java`
- `rebuild_game/src/main/java/VqsvBattleRuntime.java`

Related docs:

- `rebuild_plan/72_battle_full_skill_status_behavior_classification.md`
- `rebuild_plan/74_battle_game_d_state_full_matrix.md`
- `rebuild_plan/134_battle_p7_hit_recoil_blood_timing.md`
- `rebuild_plan/142_battle_p7_phase6_closeout_and_next_phase.md`
- `rebuild_plan/156_battle_phase9_skill_coverage_matrix.md`

## Source Result Producer: `game.b.b(target)`

Source line range audited: `game/b.java:1252..1472`.

`game.b.b(target)` produces a result array shape:

```text
Z[0] = damage
Z[1] = crit flag, 1 when critical
Z[2] = applied debuff id, or -1
```

Critical-hit source facts:

| Source step | Evidence | Meaning |
| --- | --- | --- |
| Base crit chance | `var3_3 = 5` | Normal base chance is 5 percent. |
| Final visual/form boost | `var3_3 = 30` if current visual equals final element visual | Mature/final visual has higher base chance. |
| Speed contribution | `var3_3 += this.d[4] / 2` | Faster attacker increases crit chance. |
| Status/form 4 | `if (this.f((byte)4)) var3_3 += aq.c[3][4][5]` | Status 4 adds crit chance. |
| Crit roll | `if (ae.a(100) <= var3_3)` | Source RNG gate. |
| Crit effect | raw attack `* 3 / 2`, `var2_2 = 1` | Crit modifies raw attack before family formula and sets flag. |

Other result gates in the same method:

| Gate | Evidence | Result impact |
| --- | --- | --- |
| Debuff chance | effect id/chance branch around `var5_7`, `var8_10` | Decides `Z[2]`. |
| Anti-debuff status 3 | `target.f((byte)3)` branch | Can block debuff. |
| Buff 14 | `target.m(14)` | Blocks debuff. |
| Debuff duration passive | target side passive 6 branch | Shortens debuff duration. |
| Damage modifiers | buff/debuff/passive/relation blocks after switch | Modify `Z[0]`. |
| Damage jitter | final `ae.a(100)` | Small +/- path with min clamp. |
| Buff5 store | final `target.m(5)` roll | Stores scratch damage hook but returns same result array. |

## Source Result Consumer: `game.d` P7

Source line range audited: `game/d.java:1486..1558`.

P7 does not apply `Z[0]` unconditionally. It has a separate hit/miss gate after
the effect/animation reaches the damage frame.

Miss/dodge source facts:

| Source step | Evidence | Meaning |
| --- | --- | --- |
| No-damage skill path | `if (aq.c[1][h.D][3] == 0)` | No direct damage; target state returns to 0 and HP bar helper runs. |
| Skip if target dead state | `if (((b)h.p).p() == 3)` | Already-dead target bypasses damage. |
| Attacker avoid base | `var2_14 = h.s()` | Source uses attacker speed/avoid helper. |
| Passive hook 4 | player-side passive branch adds `aq.c[2][4][6]` | Passive can reduce target dodge chance. |
| Target speed comparison | target speed minus attacker value, shifted left one bit | Core miss chance is speed delta * 2. |
| Debuff 4 correction | if attacker has debuff/form `m(4)`, uses `h.w[4][1]` | Debuff 4 changes the miss calculation. |
| Status 9 | `if (h.f((byte)9)) var2_14 = 0` | Status 9 forces hit chance by disabling miss. |
| Clamp | `0..20` | Miss chance cannot exceed 20 percent. |
| Hit roll | `if (ae.a(100) >= var2_14)` | Hit when RNG is greater or equal to miss chance. |
| Hit result | `target.k(Z[0])`, damage text, crit style, debuff text | Applies damage and all result text. |
| Miss result | floating text `"Ne tranh"` in source text, type `1`, image `0` | No HP damage, no crit damage text, no debuff text. |
| HP bar helper | runs after hit or miss | Display still updates/waits through same UI helper. |

Important consequence:

```text
Crit is produced by game.b.b(target), but dodge/miss is consumed in game.d P7.
```

So `BattleDamageResult` alone is not source-complete unless P7 also records
whether the result actually hit.

## Current Rebuild Mapping

| Area | Current rebuild evidence | Status |
| --- | --- | --- |
| Crit chance | `VqsvBattleUnit.computeDamage()` has base chance 5, final visual 30, speed/2, status 4, RNG label `damage.crit`. | PORTED/PARTIAL |
| Crit flag result | `BattleDamageResult` has `critFlag`. | PORTED/PARTIAL |
| Crit display | `VqsvBattleRuntime.updateSceneBattleFields()` sets `battleP7DamageCritical` from `p7DamageResult.critFlag`. | PORTED/PARTIAL |
| Debuff id result | `BattleDamageResult` has `appliedDebuffId`; P7 trace and `p7DebuffText()` consume it. | PORTED/PARTIAL |
| Damage jitter | `VqsvBattleUnit.computeDamage()` has RNG label `damage.jitter`. | PORTED/PARTIAL |
| Dodge/miss roll | No equivalent field/path in `BattleDamageResult` or `VqsvBattleRuntime.applyP7Damage()`. | MISSING/PARTIAL |
| P7 damage application | `applyP7Damage()` computes result, clamps damage, then always calls `p7Target.damage(p7Damage)`. | WRONG FOR MISS CASE |
| Miss floating text | No dedicated `battleP7MissText` or miss-mode use of type 1 text in current P7 fields. | MISSING |
| Miss interaction with q() | Current post-skill flow assumes damage was applied. Source miss should not show damage/debuff text; post-skill behavior must be audited per branch before claiming. | PENDING |

## Gap Matrix

| Source behavior | Current rebuild | Gap | Priority |
| --- | --- | --- | --- |
| Crit formula gate | Implemented in `computeDamage()` | Needs deterministic forced-crit smoke. | Medium |
| Crit display style | Uses `battleP7DamageCritical` | Needs smoke where crit is guaranteed/seeded. | Medium |
| Debuff id display | Existing result field and text helper | Must be suppressed on miss. | High |
| Dodge/miss chance | Missing | Add source-shaped P7 hit roll before applying HP damage. | High |
| Miss text | Missing | Add P7 floating text using source row/type equivalent, text `Ne tranh` through text table strategy if localized. | High |
| HP bar wait after miss | Missing/implicit | P7 should still run HP helper/tween wait path, but with unchanged HP. | Medium |
| q() after miss | Not closed | Audit/choose conservative behavior before code; likely no damage-dependent leech/debuff text on miss. | High |
| Deterministic smoke | Missing | Add forced hit, forced miss, forced crit checkpoints. | High |

## Proposed First Code Slice

Name:

```text
Phase 9-A: Preserve P7 Hit Result, Add Dodge/Miss Result Path
```

Scope:

- Only P7 result flow.
- No broad skill behavior changes.
- No intro/world/panel changes.
- No live client/JAR; smoke PNG only.

Implementation outline:

1. Add a P7 hit-result field.
   - Either extend `BattleDamageResult` with `dodged/missed`, or keep
     `BattleDamageResult` as formula output and add a separate P7 field such as
     `p7AttackHit`.
   - Source shape favors separate P7 field because dodge is in `game.d`, not
     `game.b`.
2. Port source-shaped miss chance in `VqsvBattleRuntime.applyP7Damage()`.
   - Use attacker speed helper/current speed.
   - Use target speed.
   - Clamp `0..20`.
   - Respect status 9 as forced hit.
   - Gate passive/debuff corrections only if source-backed fields already exist;
     otherwise mark that subpart PARTIAL and trace it.
3. If hit:
   - current damage path continues;
   - crit/debuff text remains as today.
4. If miss:
   - do not mutate target HP;
   - do not show `-damage`;
   - do not show debuff text;
   - show miss text with type 1/blood row 1 equivalent;
   - still run HP helper/wait with unchanged HP, matching P7 source shape.
5. Add trace labels:
   - `battle.P7.skillX.hitroll.missChance`
   - `battle.P7.skillX.hitroll`
   - trace final `hit=true/false`.

Suggested smoke checkpoints:

| Checkpoint | Purpose |
| --- | --- |
| `battle_p7_hit_forced_direct_skill10` | Baseline: skill10 still damages and route is unchanged. |
| `battle_p7_miss_forced_skill10` | Miss text visible, HP unchanged, no debuff text. |
| `battle_p7_crit_forced_skill10` | Crit flag visible and damage style uses critical path. |
| `battle_p7_miss_suppresses_debuff_skill1` | If skill1 misses, debuff text/queue is not applied. |
| `route_sophie_after_battle_branch` | Regression. |
| `route_bunny_after_battle_task` | Regression. |
| `route_elder_after_battle_reward_state` | Regression. |

Regression bundle after code:

- `powershell -ExecutionPolicy Bypass -File .\build.ps1`
- `java -cp .\build\classes com.vqsv.rebuild.Main --check`
- `java -cp .\build\classes VqsvBattleDamageFormulaCheck`
- Java source mojibake literal scan
- Smoke PNG only, no live client.

## Status Decision

| Item | Status |
| --- | --- |
| Crit formula/result | PORTED/PARTIAL |
| Crit deterministic smoke | PENDING |
| Dodge/miss source mapping | SOURCE-MAPPED |
| Dodge/miss rebuild runtime | MISSING/PARTIAL |
| Miss text/UI | MISSING |
| First Phase 9 code slice | IMPLEMENTED-PARTIAL |

## Next Roadmap Step

Implementation result:

| Item | Result |
| --- | --- |
| P7 hit/miss field | PORTED/PARTIAL: rebuild keeps `BattleDamageResult` as `game.b.b(target)` output and stores P7 hit state separately, matching source ownership. |
| Miss chance | PORTED/PARTIAL: speed delta, debuff4 correction, status9 forced-hit, clamp `0..20`, and hit roll are source-shaped. Passive hook 4 remains traced as `PENDING`. |
| Hit path | PORTED: hit still applies HP damage, hit state, damage text, crit style, debuff text. |
| Miss path | PORTED/PARTIAL: miss does not apply HP damage, suppresses damage/debuff text, and shows `Né tránh` through the secondary `blood.mid[1]` text path. |
| Post-damage effects | PORTED/PARTIAL: leech/form8/reflect-style damage-dependent effects are skipped when the hit roll misses. |
| Smoke | PASS: `battle_p7_hit_forced_direct_skill10`, `battle_p7_miss_forced_skill10`, `battle_p7_crit_forced_skill10`. |
| Route regression | PASS: Sophie, Bunny, Elder route smoke after the slice. |

Smoke PNGs:

- `rebuild_game/build/smoke/battle_p7_hit_forced_direct_skill10.png`
- `rebuild_game/build/smoke/battle_p7_miss_forced_skill10.png`
- `rebuild_game/build/smoke/battle_p7_crit_forced_skill10.png`

Next step should now be:

```text
Phase 9-B: add broad smoke-only coverage for already-ported direct formula families.
```

Recommended first targets:

- direct simple sibling smoke: `0/6/16/20/26/30/36/40/46/50/56/60/66`;
- plus-divisor sibling smoke: `7` after existing `1`;
- heal/leech sibling smoke: `17/52/69` after existing `11/58/63`;
- keep `SOURCE_SWITCH_GAP` skills out until bytecode/control-flow audit.
