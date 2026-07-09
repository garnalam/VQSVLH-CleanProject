# 177 Battle Phase 9-T Skill 67 Raw Visual Smoke

Status date: 2026-07-09

Status: PHASE 9-T / IMPLEMENTED-PARTIAL / SMOKE-COVERED.

Purpose:

- Smoke skill `67` after Phase 9-S classified it as raw/default damage plus
  P7 visual effect only.
- Prove it does not apply buff/debuff id `5`.
- Prove it does not run a `game.d.q()` post-effect.

## Source Basis

Primary audit:

- `rebuild_plan/176_battle_phase9s_skill67_effectmode2_audit.md`

Source facts used:

| Area | Source fact |
| --- | --- |
| Formula | `game.b.b(target)` bytecode routes skill `67` to default raw-damage branch. |
| Debuff/effect id | Default branch sets applied effect/debuff to `-1`; `skill[7] == 5` is ignored for damage result. |
| q() post-effect | `game.d.q()` has no case `67`; default q() only applies effects for `skill[6] == 1`, but skill `67` has `skill[6] == 2`. |
| Visual | `effect.mid[67] = [0,0,26,0,-1,-1,0, 0,1,11,0,-1,-1,0]`. |

## Implementation

Touched code:

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added checkpoint `battle_phase9t_raw_visual_skill_67`. |

No battle runtime/formula logic was changed in this slice.

The checkpoint asserts:

- P7 damage frame appears.
- Trace contains `BYTECODE_DEFAULT_RAW_DAMAGE skill=67`.
- Trace contains `powerPercentIgnored=110`.
- Trace contains `effectIdIgnored=5`.
- Damage result trace contains `appliedDebuffId=-1`.
- Enemy does not have debuff `5`.
- No `game.d.q postEffect skill=67` trace appears.
- P7 visual trace includes source chunk `0` with id `26`.
- P7 visual trace includes speffect chunk `11`.

## Smoke

Output:

```text
rebuild_game/build/smoke/phase9t/battle_phase9t_raw_visual_skill_67.png
```

| Checkpoint | Result |
| --- | --- |
| `battle_phase9t_raw_visual_skill_67` | PASS |

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
rebuild_game/build/smoke/phase9t_regression/
```

## Status Decision

| Area | Status |
| --- | --- |
| Skill `67` formula branch | PORTED/PARTIAL, smoke-covered as bytecode-default raw damage. |
| Skill `67` target debuff/buff id `5` | NOT_REACHED, smoke-covered. |
| Skill `67` q() post-effect | NOT_REACHED, smoke-covered. |
| Skill `67` P7 visual row | PORTED/PARTIAL trace-covered for chunks `26/11`; pixel-perfect visual parity not claimed. |

## Next Roadmap Step

Recommended:

```text
Phase 9-U: cover remaining known row gaps outside SOURCE_SWITCH_GAP.
```

Candidates visible in the current matrix:

- `68`: direct damage plus q() self-buff `10`.
- `64`: selected-index buff copy behavior.
- no-damage/default effectMode 1 rows such as `4/5/14/24/25/34/35/44/65`.

Pick one family and keep the same audit-first pattern.
