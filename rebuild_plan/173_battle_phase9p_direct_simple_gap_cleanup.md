# 173 Battle Phase 9-P Direct-Simple Gap Cleanup

Status date: 2026-07-09

Status: PHASE 9-P / SMOKE-ONLY / SMOKE-COVERED.

Purpose:

- Close the stale `MISSING` coverage gap for direct-simple skills that already
  use the ported `game.b.b(target)` direct damage path.
- Do not change battle logic, formulas, UI, effects, intro/world, or route
  behavior.
- Keep `SOURCE_SWITCH_GAP` skills out of this slice.

## Source Facts

Primary sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/script/decoded/data__script__db.mid.json`
- `rebuild_plan/156_battle_phase9_skill_coverage_matrix.md`
- `rebuild_plan/158_battle_phase9b_direct_smoke_coverage.md`

The direct-simple path is the already-ported family where source
`game.b.b(target)` computes direct damage from the current raw attack/defense
base and `aq.c[1][skill][3]`, with no dedicated post-hit q() behavior, no
debuff queue, no target buff clear, and no HP-percent scaling.

## Scope

Covered skills:

```text
0, 6, 10, 16, 20, 26, 30, 36, 40, 46, 50, 56, 60, 66
```

Excluded on purpose:

| Family | Skills | Reason |
| --- | --- | --- |
| Debuff families | `2,8,12,13,18,19,22,28,31,32,33,37,38,39,41,47,51,57,61` | Already covered by debuff-id slices or needs those family assertions. |
| Post-hit q() | `11,17,52,58,63,69` | Needs heal/leech/follow-up assertions. |
| Target buff clear | `43,49` | Covered by Phase 9-N. |
| HP-percent scaling | `53,59` | Covered by Phase 9-O. |
| Zero-power guard | `54,55` | Covered by Phase 9-M as no-damage guard, not direct damage. |
| Self-buff/source switch gap | `21,27,42,48,62,67` | Requires bytecode/control-flow audit before code. |
| Selected-index buff | `64` | Needs selected-index parity audit. |
| Default no-damage buff route | `4,5,14,24,25,34,35,44,65` | Needs P13/routing coverage, not direct damage. |

## Implementation

No code changes were required in this slice. Existing smoke checkpoint support:

```text
battle_phase9b_direct_skill_<skillId>
```

was reused and written to a Phase 9-P output folder.

Each checkpoint:

1. enters Elder battle with the selected skill in slot 0;
2. forces P7 hit roll to `99`;
3. waits for P7 damage phase;
4. asserts damage popup is visible;
5. asserts miss text is empty;
6. asserts trace contains `battle P7 hitroll skill=<id>` and `hit=true`.

## Smoke Matrix

Output folder:

```text
rebuild_game/build/smoke/phase9p/
```

| Skill | Checkpoint used | PNG | Result |
| --- | --- | --- | --- |
| `0` | `battle_phase9b_direct_skill_0` | `battle_phase9p_direct_skill_0.png` | PASS |
| `6` | `battle_phase9b_direct_skill_6` | `battle_phase9p_direct_skill_6.png` | PASS |
| `10` | `battle_phase9b_direct_skill_10` | `battle_phase9p_direct_skill_10.png` | PASS |
| `16` | `battle_phase9b_direct_skill_16` | `battle_phase9p_direct_skill_16.png` | PASS |
| `20` | `battle_phase9b_direct_skill_20` | `battle_phase9p_direct_skill_20.png` | PASS |
| `26` | `battle_phase9b_direct_skill_26` | `battle_phase9p_direct_skill_26.png` | PASS |
| `30` | `battle_phase9b_direct_skill_30` | `battle_phase9p_direct_skill_30.png` | PASS |
| `36` | `battle_phase9b_direct_skill_36` | `battle_phase9p_direct_skill_36.png` | PASS |
| `40` | `battle_phase9b_direct_skill_40` | `battle_phase9p_direct_skill_40.png` | PASS |
| `46` | `battle_phase9b_direct_skill_46` | `battle_phase9p_direct_skill_46.png` | PASS |
| `50` | `battle_phase9b_direct_skill_50` | `battle_phase9p_direct_skill_50.png` | PASS |
| `56` | `battle_phase9b_direct_skill_56` | `battle_phase9p_direct_skill_56.png` | PASS |
| `60` | `battle_phase9b_direct_skill_60` | `battle_phase9p_direct_skill_60.png` | PASS |
| `66` | `battle_phase9b_direct_skill_66` | `battle_phase9p_direct_skill_66.png` | PASS |

## Regression

| Check | Result |
| --- | --- |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| Java mojibake scan | PASS, no matches |
| `route_sophie_after_battle_branch` | PASS |
| `route_bunny_after_battle_task` | PASS |
| `route_elder_after_battle_reward_state` | PASS |

Regression PNG folder:

```text
rebuild_game/build/smoke/phase9p_regression/
```

## Status Decision

| Area | Status |
| --- | --- |
| Direct-simple no-extra-effect smoke coverage | PORTED/PARTIAL, smoke-covered. |
| Formula value exactness per skill | PARTIAL: this slice checks forced-hit result flow and visible damage, not per-skill numeric parity. |
| Critical/miss/jitter exact RNG stream parity | PARTIAL: deterministic smoke hooks are used. |
| `SOURCE_SWITCH_GAP` skills | PENDING/UNKNOWN. |

## Next Roadmap Step

Recommended next slice:

```text
Phase 9-Q: SOURCE_SWITCH_GAP audit for 21/27/42/48/62/67.
```

Do this as audit-first, not code-first. The goal is to prove source control
flow around the decompiled switch gaps before deciding whether any of these
skills should enter the direct damage path, default effect path, or q()
self-buff path.
