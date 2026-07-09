# 170 Battle Phase 9-M Zero-Power Debuff 8/9 Coverage

Status date: 2026-07-09

Status: PHASE 9-M / AUDITED / IMPLEMENTED-PARTIAL / SMOKE-COVERED.

Purpose:

- Cover zero-power debuff skills `54/55`.
- Prove source behavior before treating them like ordinary direct damage.
- Confirm that source P7 skips the damage/debuff apply path when
  `aq.c[1][skill][3] == 0`, even though the skill table contains debuff ids.

## Source Facts

Primary sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/script/decoded/data__script__db.mid.json`
- `modules/script/original/bufDebuf.mid`

Skill rows from `db.mid` group 1:

| Skill | Row | Meaning |
| --- | --- | --- |
| `54` | `[5,171,583,0,1,10,2,8,40,0]` | Direct-simple table family, power `0`, effectMode `2`, debuff id `8`; P7 skips damage/debuff apply because power is `0`. |
| `55` | `[5,172,584,0,1,10,2,9,-1,0]` | Direct-simple table family, power `0`, effectMode `2`, debuff id `9`; P7 skips damage/debuff apply because power is `0`. |

Debuff rows from `db.mid` group 7:

| Debuff id | Row | Meaning |
| --- | --- | --- |
| `8` | `[319,330,4]` | Active debuff row exists, but skills `54` do not apply it through P7 because power is `0`. |
| `9` | `[320,331,1]` | Active debuff row exists, but skill `55` does not apply it through P7 because power is `0`. |

Visual rows from original `bufDebuf.mid`:

| Bank | Id | Row map | Visual row | Meaning |
| --- | --- | --- | --- | --- |
| Debuff bank `1` | `8` | row id `5` | `[1,0,0,-1,0,25,0,-1]` | Visual row exists, but should not start from skill `54` because no debuff is applied. |
| Debuff bank `1` | `9` | row id `6` | `[1,12,0,-1]` | Visual row exists, but should not start from skill `55` because no debuff is applied. |

Source behavior from `game.b.b(target)`:

| Step | Source behavior | Rebuild status |
| --- | --- | --- |
| P7 guard | `game.d` P7 checks `aq.c[1][h.D][3] == 0`; when true, it resets UI/actor state and skips the damage text/apply path. | PORTED/PARTIAL, smoke added. |
| Damage formula | `game.b.b(target)` would direct-simple clamp non-positive damage if called, but P7 does not call the damage/apply path for skills `54/55`. | NOT REACHED for this battle flow. |
| Debuff producer | Table `effectId` is `8/9`, but producer is not reached from P7 for skills `54/55`. | NOT REACHED, smoke added. |
| Buff14/status3 gates | These gates belong to the skipped producer path, so they should not roll/apply for skills `54/55`. | NOT REACHED, smoke added. |
| Active queue | Since no debuff slot is written, P12/P13 visual rows for ids `8/9` should not start from skills `54/55`. | NOT REACHED, smoke added. |

Source behavior from `game.b.q(id)`:

| Debuff id | Source behavior | Rebuild status |
| --- | --- | --- |
| `8` | No `case 8`; if some other source applied it, active queue tick would be no-op except duration. | PENDING broader source caller. |
| `9` | No `case 9`; if some other source applied it, active queue tick would be no-op except duration. | PENDING broader source caller. |

## Implementation

Touched code:

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added Phase 9-M checkpoints and `isPhase9MZeroPowerDebuffSkill(...)`. |

No runtime gameplay logic was changed in this slice before smoke. Existing
`VqsvBattleRuntime` already has the source-shaped P7 no-damage guard for
`aq.c[1][skill][3] == 0`.

## Smoke Matrix

Planned output folder:

```text
rebuild_game/build/smoke/phase9m/
```

| Checkpoint | Asserted behavior | Result |
| --- | --- | --- |
| `battle_phase9m_zero_power_success_skill_54` | Skill54 reaches P7 phase 3 with no damage text, no hitroll, no debuff8, enemy HP unchanged. | PASS |
| `battle_phase9m_zero_power_success_skill_55` | Skill55 reaches P7 phase 3 with no damage text, no hitroll, no debuff9, enemy HP unchanged. | PASS |
| `battle_phase9m_zero_power_buff14_block_skill_54` | Buff14 is irrelevant because skill54 skips producer before debuff gates. | PASS |
| `battle_phase9m_zero_power_status3_block_skill_54` | Status3/debuff roll is irrelevant because skill54 skips producer before chance gates. | PASS |
| `battle_phase9m_zero_power_miss_queue_skill_54` | Miss/hitroll is irrelevant because skill54 skips P7 damage/hitroll path. | PASS |
| `battle_phase9m_debuff8_visual_consumer_skill54` | Debuff8 visual row does not start because skill54 applies no debuff. | PASS |
| `battle_phase9m_debuff9_visual_consumer_skill55` | Debuff9 visual row does not start because skill55 applies no debuff. | PASS |

## Verification

Commands run:

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-checkpoint <phase9m checkpoint> build\smoke\phase9m\<checkpoint>.png
java -cp build\classes com.vqsv.rebuild.Main --check
java -cp build\classes VqsvBattleDamageFormulaCheck
rg -n "Ã|Â|Æ|Ð|ð|□|�|mojibake" src\main\java
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_sophie_after_battle_branch build\smoke\phase9m_regression\route_sophie_after_battle_branch.png
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_bunny_after_battle_task build\smoke\phase9m_regression\route_bunny_after_battle_task.png
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state build\smoke\phase9m_regression\route_elder_after_battle_reward_state.png
```

Result:

| Check | Result |
| --- | --- |
| Build | PASS |
| Phase 9-M smoke group | PASS |
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
| Skills `54/55` P7 no-damage guard | PORTED/PARTIAL, smoke-covered. |
| Minimum damage clamp | NOT REACHED for skills `54/55` in P7. |
| Debuff ids `8/9` producer | NOT REACHED for skills `54/55` in P7. |
| Buff14 block | NOT REACHED for skills `54/55` in P7. |
| Status3 implicit-chance block | NOT REACHED for skills `54/55` in P7. |
| Miss interaction | NOT REACHED for skills `54/55` in P7. |
| P12/P13 visual consumer | NOT REACHED from skills `54/55`; row existence audited. |
| Exact RNG stream parity | PARTIAL. Smoke uses deterministic hooks. |
| Pixel-perfect AH visual | PARTIAL. Rows are source-backed; MIDP pixel compare remains outside this slice. |

## Next Roadmap Step

After Phase 9-M passes, continue Phase 9 coverage by closing remaining direct
formula holes or moving to source-switch gaps.

Recommended next slice:

```text
Phase 9-N: target buff clear skills 43/49.
```

Reason:

- They are not `SOURCE_SWITCH_GAP`.
- Runtime already has `target.clearBuffs()`, but needs smoke coverage for
  active buff removal and damage result preservation.
