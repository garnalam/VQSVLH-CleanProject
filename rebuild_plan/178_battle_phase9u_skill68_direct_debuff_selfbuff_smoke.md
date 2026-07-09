# 178 Battle Phase 9-U Skill 68 Direct Debuff Self-Buff Smoke

Status date: 2026-07-09

Status: PHASE 9-U / IMPLEMENTED-PARTIAL / SMOKE-COVERED.

Purpose:

- Cover skill `68`, which is outside `SOURCE_SWITCH_GAP`.
- Prove it is direct power-percent damage.
- Prove it applies target debuff `10` from `game.b.b(target)`.
- Prove it also applies attacker self-buff `10` from `game.d.q()`.

## Source Facts

Sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/script/decoded/data__script__db.mid.json`
- `modules/script/decoded/data__script__effect.mid.json`

Skill row:

```text
skill 68 = [6,185,597,110,3,15,1,10,5,0]
```

Classification:

| Area | Source-backed behavior |
| --- | --- |
| Formula | `game.b.b(target)` direct switch includes `case 68`, so damage uses `raw * skill[3] / 100`. |
| Target debuff | Because direct branch keeps `skill[7] == 10` and chance is `-1`, target debuff `10` is applied. |
| Post-skill q() | `game.d.q()` explicit case `21/27/42/48/62/68` applies self-buff id `10` to the attacker. |
| Visual | `effect.mid[68]` uses same visual row shape as `62`: chunks `26`, `0`, `15`. |

Important correction discovered during smoke:

```text
Skill 68 is not only direct damage + self-buff.
It also applies target debuff 10 through game.b.b(target).
```

## Implementation

Touched code:

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleUnit.java` | Added trace-only marker for direct power-percent formula branch. No formula change. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added checkpoint `battle_phase9u_direct_self_buff_skill_68`. |

Checkpoint asserts:

- P7 damage frame appears.
- Trace contains `POWER_PERCENT skill=68`.
- Trace contains `powerPercent=110`.
- Trace does not contain `BYTECODE_DEFAULT_RAW_DAMAGE skill=68`.
- Damage result trace contains `appliedDebuffId=10`.
- Enemy has debuff `10`.
- `game.d.q()` post-effect trace appears.
- Player has active buff `10`.

## Smoke

Output:

```text
rebuild_game/build/smoke/phase9u/battle_phase9u_direct_self_buff_skill_68.png
```

| Checkpoint | Result |
| --- | --- |
| `battle_phase9u_direct_self_buff_skill_68` | PASS |

## Regression

| Check | Result |
| --- | --- |
| `build.ps1` | PASS |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| Java mojibake scan | PASS, no matches |
| `route_sophie_after_battle_branch` | PASS |
| `route_bunny_after_battle_task` | PASS |
| `route_elder_after_battle_reward_state` | PASS |

Regression PNG folder:

```text
rebuild_game/build/smoke/phase9u_regression/
```

## Status Decision

| Area | Status |
| --- | --- |
| Skill `68` direct formula | PORTED/PARTIAL, smoke-covered. |
| Skill `68` target debuff `10` | PORTED/PARTIAL, smoke-covered. |
| Skill `68` q() self-buff `10` | PORTED/PARTIAL, smoke-covered. |
| Exact RNG stream parity | PARTIAL: deterministic smoke hooks are used. |
| Visual pixel parity | PARTIAL/PENDING, not claimed by this slice. |

## Next Roadmap Step

Recommended:

```text
Phase 9-V: audit selected-index skill 64.
```

Reason:

- `64` is still a high-risk unique q() path: selected-index buff copy behavior.
- It should be audit-first before code/smoke.

Alternative lower-risk route:

```text
Audit no-damage/default effectMode 1 rows: 4/5/14/24/25/34/35/44/65.
```
