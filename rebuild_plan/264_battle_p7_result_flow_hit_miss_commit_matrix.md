# 264 - Battle P7 Result Flow Hit/Miss Commit Matrix

Date: 2026-07-13

Scope: refactor the rebuild P7 direct-damage flow so `computeDamage` calculates a potential result first, then P7 commits mutating side effects only after the source hit check succeeds.

## Source Facts

| Source | Fact |
| --- | --- |
| `game.b.b(b target)` | Builds damage result array `Z = [damage, critFlag, debuffId]`; source bytecode also prepares target debuff / clear-buff / reflect-like side effects during this calculation path. |
| `game.d case 7` | Runs hit check `ae.a(100) >= missChance` after result calculation. HP damage text and debuff text are inside the hit branch. Miss branch shows `Ne tranh` only. |
| `game.d case 7` | Crit text/damage number is only visible in hit branch. Miss does not show damage/crit/debuff text. |

## Rebuild Mapping

| Piece | Before | Now | Status |
| --- | --- | --- | --- |
| Damage calculation | `BattleUnit.computeDamage(target)` returned damage and immediately mutated target debuff state. | `computeDamage(target)` returns `BattleDamageResult` with pending side effects. | PORTED |
| Debuff apply | Could happen before P7 hitroll, so forced miss could silently leave a debuff. | `BattleDamageResult.commitPendingSideEffects()` applies debuff only when P7 hit succeeds. | PORTED |
| Skill 43/49 clear target buffs | Could mutate during calculation. | Clear is pending and committed only on hit. | PORTED/PARTIAL: direct smoke coverage still needed when returning to those skills. |
| Buff5 reflect scratch | Could be stored during calculation. | Reflect scratch is pending and committed only on hit. | PORTED/PARTIAL: existing reflect smoke still passes. |
| Trace fields | `appliedDebuffId` meant result id and could be confusing on miss. | Trace logs `appliedDebuffId` as committed id, plus `pendingDebuffId` for calculated source result. | PORTED |

## Smoke Matrix

| Checkpoint | Setup | Expected |
| --- | --- | --- |
| `battle_p7_miss_forced_debuff_no_commit` | Skill `2`, forced debuff roll `0`, forced miss hitroll `0`. | HP unchanged, miss text visible, no damage text, no debuff text, enemy debuff1 inactive, `sideEffectsCommitted=false`. |
| `battle_p7_hit_forced_debuff_commit` | Skill `2`, forced debuff roll `0`, forced hitroll `99`. | Damage text visible, debuff text visible, enemy debuff1 active, `sideEffectsCommitted=true`. |
| `battle_p7_crit_forced_skill10` | Skill `10`, forced crit roll `0`, forced hitroll `99`. | Crit visual flag only appears on hit damage. |

## Verification

- `build.ps1`: PASS.
- `com.vqsv.rebuild.Main --check`: PASS.
- `VqsvBattleDamageFormulaCheck`: PASS.
- Mojibake scan Java/doc: PASS, no hits.
- `--smoke-suite battle_quick`: PASS, `47/47`.

## Remaining Notes

- This closes the shared P7 result-flow bug before continuing buff/debuff table order.
- Next roadmap slice should return to `263` order: buff1 `Pha Phu` visual + logic + expiry smoke, with forced hit/miss/crit assertions included.
