# 171 Battle Phase 9-N Target Buff Clear Coverage

Status date: 2026-07-09

Status: PHASE 9-N / AUDITED / IMPLEMENTED-PARTIAL / SMOKE-COVERED.

Purpose:

- Cover target-buff-clear skills `43/49`.
- Prove `game.b.b(target)` clears target buffs via `target.D()` while preserving
  normal damage result flow.
- Prove the clear happens before P7 hit/miss display, so a forced miss still
  clears target buffs but does not reduce HP.

## Source Facts

Primary sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/script/decoded/data__script__db.mid.json`

Skill rows from `db.mid` group 1:

| Skill | Row | Meaning |
| --- | --- | --- |
| `43` | `[4,160,572,100,1,30,0,-1,-1,0]` | Direct damage, no debuff, target buff clear. |
| `49` | `[4,166,578,180,3,15,0,-1,-1,0]` | Direct damage, no debuff, target buff clear. |

Source behavior from `game.b.b(target)`:

| Step | Source behavior | Rebuild status |
| --- | --- | --- |
| Damage formula | Skills `43/49` use `raw * aq.c[1][skill][3] / 100`. | PORTED/PARTIAL, smoke added. |
| Buff clear | `case 43/49` calls `var1_1.D()` on the target. | PORTED/PARTIAL, smoke added. |
| Debuff result | `effectId` is `-1`, so no target debuff should be produced. | PORTED/PARTIAL, smoke added. |
| Hit/miss timing | P7 calls `game.b.b(target)` before hitroll display, so target buffs can be cleared even if the later P7 hitroll misses. | PORTED/PARTIAL, smoke added. |

Source behavior from `game.b.D()`:

| Step | Source behavior | Rebuild status |
| --- | --- | --- |
| Buff slot clear | Loops all 16 buff slots and calls `n(id)` for active ones. | PORTED/PARTIAL. |
| Active queue clear | Loops active effect bank `0` slots and clears them via `e(0, slot)`. | PORTED/PARTIAL, smoke added. |

## Implementation

Touched code:

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Added smoke hooks to prepare source-like active enemy buffs and inspect active buff slots. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added Phase 9-N checkpoints and `isPhase9NClearBuffSkill(...)`. |

No gameplay runtime formula was changed in this slice. Existing `BattleUnit`
already calls `target.clearBuffs()` for skills `43/49`.

## Smoke Matrix

Planned output folder:

```text
rebuild_game/build/smoke/phase9n/
```

| Checkpoint | Asserted behavior | Result |
| --- | --- | --- |
| `battle_phase9n_clear_buff_success_skill_43` | Target has active buff2 and buff14; forced hit damages target, clears both buff slots and active queue entries. | PASS |
| `battle_phase9n_clear_buff_success_skill_49` | Same clear behavior for skill49. | PASS |
| `battle_phase9n_clear_buff_miss_skill_43` | Forced miss shows dodge and keeps HP unchanged, but target buffs are still cleared because producer ran first. | PASS |
| `battle_phase9n_clear_buff_no_target_buff_skill_43` | No target buff present; skill43 still damages normally and produces no debuff. | PASS |

## Verification

Commands run:

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-checkpoint <phase9n checkpoint> build\smoke\phase9n\<checkpoint>.png
java -cp build\classes com.vqsv.rebuild.Main --check
java -cp build\classes VqsvBattleDamageFormulaCheck
rg -n "Ã|Â|Æ|Ð|ð|□|�|mojibake" src\main\java
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_sophie_after_battle_branch build\smoke\phase9n_regression\route_sophie_after_battle_branch.png
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_bunny_after_battle_task build\smoke\phase9n_regression\route_bunny_after_battle_task.png
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state build\smoke\phase9n_regression\route_elder_after_battle_reward_state.png
```

Result:

| Check | Result |
| --- | --- |
| Build | PASS |
| Phase 9-N smoke group | PASS |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| Java mojibake literal scan | PASS: no matches |
| Route Sophie/Bunny/Elder | PASS |

Note: a parallel check run raced with `build.ps1` while classes were being
rebuilt and produced a transient classpath error. The same checks passed when
run after build completion.

## Current Status

| Area | Status |
| --- | --- |
| Skills `43/49` damage formula | PORTED/PARTIAL, smoke-covered. |
| Target `D()` buff clear | PORTED/PARTIAL, smoke-covered. |
| Active buff queue clear | PORTED/PARTIAL, smoke-covered. |
| Miss interaction | PORTED/PARTIAL, smoke-covered. |
| Debuff result preservation | PORTED/PARTIAL, smoke-covered: `appliedDebuffId=-1`. |
| Exact RNG stream parity | PARTIAL. Smoke uses deterministic hitroll hook. |

## Next Roadmap Step

After Phase 9-N passes, continue Phase 9 by closing remaining direct formula
holes or choosing the next non-gap special family.

Recommended next slice:

```text
Phase 9-O: HP percent scaling skills 53/59.
```

Reason:

- They are not `SOURCE_SWITCH_GAP`.
- Runtime already has `raw * (skill[8] - hpPercent) / 100`, but needs low/high
  HP smoke and min-clamp coverage.
