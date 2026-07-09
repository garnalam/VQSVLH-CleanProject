# 186 Battle Phase 9-AC Missing Formula Smoke Closeout

Status date: 2026-07-09

Status: PHASE 9-AC / NO RUNTIME CODE CHANGE / SMOKE-COVERED.

Purpose:

- Close the rows still marked `MISSING` in `156_battle_phase9_skill_coverage_matrix.md`.
- Re-run the existing Phase 9-C formula-variant checkpoints for skills `3/7/9/23/29`.
- Update the matrix so it reflects the real smoke coverage already present in harness/doc `159`.

## Scope

Skills closed:

| Skill | Family | Checkpoint |
| ---: | --- | --- |
| `3` | Conditional damage if target has debuff0 | `battle_phase9c_cond_debuff0_skill_3` |
| `7` | Plus-divisor sibling with debuff0 | `battle_phase9c_plus_divisor_skill_7` |
| `9` | Conditional damage if target has debuff0 | `battle_phase9c_cond_debuff0_skill_9` |
| `23` | Conditional damage if target has debuff1 | `battle_phase9c_cond_debuff1_skill_23` |
| `29` | Conditional damage if target has debuff1 | `battle_phase9c_cond_debuff1_skill_29` |

No Java runtime behavior was changed for this slice.

## Source Facts

Primary reference:

- `rebuild_plan/159_battle_phase9c_formula_variant_smoke_coverage.md`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/script/decoded/data__script__db.mid.json`

Source behavior:

| Family | Source behavior |
| --- | --- |
| `DIRECT_PLUS_RAW_DIVISOR` | Damage = direct percent + raw attack / `skill[8]`; can apply debuff0. |
| `CONDITIONAL_IF_TARGET_DEBUFF0` | Uses alternate percent `skill[8]` if target has debuff0. |
| `CONDITIONAL_IF_TARGET_DEBUFF1` | Uses alternate percent `skill[8]` if target has debuff1. |

## Smoke

Output folder:

```text
rebuild_game/build/smoke/phase9ac/
```

Results:

| Checkpoint | Result |
| --- | --- |
| `battle_phase9c_cond_debuff0_skill_3` | PASS |
| `battle_phase9c_plus_divisor_skill_7` | PASS |
| `battle_phase9c_cond_debuff0_skill_9` | PASS |
| `battle_phase9c_cond_debuff1_skill_23` | PASS |
| `battle_phase9c_cond_debuff1_skill_29` | PASS |

PNG outputs:

```text
rebuild_game/build/smoke/phase9ac/battle_phase9c_cond_debuff0_skill_3.png
rebuild_game/build/smoke/phase9ac/battle_phase9c_plus_divisor_skill_7.png
rebuild_game/build/smoke/phase9ac/battle_phase9c_cond_debuff0_skill_9.png
rebuild_game/build/smoke/phase9ac/battle_phase9c_cond_debuff1_skill_23.png
rebuild_game/build/smoke/phase9ac/battle_phase9c_cond_debuff1_skill_29.png
```

## Matrix Update

Updated:

- `rebuild_plan/156_battle_phase9_skill_coverage_matrix.md`

Rows changed from `MISSING` to the existing Phase 9-C checkpoint names:

```text
3, 7, 9, 23, 29
```

## Status Decision

| Skill | Status | Notes |
| ---: | --- | --- |
| `3` | PORTED/PARTIAL, smoke-covered | Conditional debuff0 branch covered. |
| `7` | PORTED/PARTIAL, smoke-covered | Plus-divisor sibling covered. |
| `9` | PORTED/PARTIAL, smoke-covered | Conditional debuff0 sibling covered. |
| `23` | PORTED/PARTIAL, smoke-covered | Conditional debuff1 branch covered. |
| `29` | PORTED/PARTIAL, smoke-covered | Conditional debuff1 sibling covered. |

Still not claimed:

- Exact RNG stream parity.
- Pixel-perfect P7 animation/effect parity.
- Broader negative cases beyond the deterministic Phase 9-C setup.

## Next Roadmap Step

Recommended:

```text
Phase 9-AD: scan the skill matrix for any remaining MISSING/AUDITED/PENDING rows and either close them by smoke or explicitly mark NOT_REACHED/PENDING with source reason.
```

Reason:

- The obvious `MISSING` formula rows are now closed.
- Before moving to a new battle phase, the matrix should be checked once more for stale status labels.
