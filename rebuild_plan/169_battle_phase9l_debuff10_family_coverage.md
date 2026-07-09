# 169 Battle Phase 9-L Debuff Id 10 Family Coverage

Status date: 2026-07-09

Status: PHASE 9-L / AUDITED / IMPLEMENTED-PARTIAL / SMOKE-COVERED.

Purpose:

- Continue Phase 9 debuff-family coverage by debuff id.
- Cover explicit-chance debuff id `10` skills: `41/47`.
- Prove the extra consumer: catch chance reads target debuff `10`.
- Keep `SOURCE_SWITCH_GAP` skills out of scope.

## Source Facts

Primary sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/script/decoded/data__script__db.mid.json`
- `modules/script/original/bufDebuf.mid`

Skill rows from `db.mid` group 1:

| Skill | Row | Meaning |
| --- | --- | --- |
| `41` | `[4,158,570,90,0,45,2,10,10,0]` | Direct damage, effectMode `2`, debuff id `10`, explicit debuff chance `10`. |
| `47` | `[4,164,576,130,2,30,2,10,10,0]` | Direct damage, effectMode `2`, debuff id `10`, explicit debuff chance `10`. |

Debuff row from `db.mid` group 7:

| Debuff id | Row | Meaning |
| --- | --- | --- |
| `10` | `[321,332,4]` | Catch/status debuff. Source `game.d.b(itemId)` checks target `m(10)`. |

Visual row from original `bufDebuf.mid`:

| Bank | Id | Row map | Visual row | Meaning |
| --- | --- | --- | --- | --- |
| Debuff bank `1` | `10` | row id `7` | `[1,19,0,-1,1,6,0,-1]` | Source visual gate includes debuff id `10`, so P12/P13 should render an active-queue special effect. |

Source behavior from `game.b.b(target)`:

| Step | Source behavior | Rebuild status |
| --- | --- | --- |
| Damage formula | Skills `41/47` are in the explicit debuff chance family: `raw * skill[3] / 100`. | PORTED/PARTIAL. |
| Debuff producer | `var8_10 = aq.c[1][skill][8]`, so skill `41/47` use explicit chance `10`. | PORTED/PARTIAL. |
| Buff 14 block | If target `m(14)`, debuff is blocked. | PORTED/PARTIAL. |
| Status 3 anti-debuff | If target `f(3)`, source reduces the chance by `aq.c[3][3][5]`. | PORTED/PARTIAL. |
| Slot write | Applies target `w[10]`, duration from `aq.c[7][10][2]`, source skill id, active flag. | PORTED/PARTIAL. |
| Active queue | Adds active effect bank `1`, id `10`; source visual gate includes id `10`. | PORTED/PARTIAL. |

Source behavior from `game.d.b(itemId)` catch chance:

| Step | Source behavior | Rebuild status |
| --- | --- | --- |
| Status index | `target.m(1)` -> index 1, `target.m(2)` -> index 2, `target.m(10)` -> index 3, attacker `f(11)` -> index 4. | PORTED/PARTIAL. |
| Multiplier | Uses status multiplier array `{10,11,12,12,12}`. Debuff `10` therefore shares the same multiplier as debuff `2`. | PORTED/PARTIAL. |

## Implementation

Touched code:

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added Phase 9-L checkpoints and `isPhase9LDebuff10Skill(...)`. |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Added smoke-only `debugCatchChanceForSmoke(itemId)` read hook. |

No source gameplay formula was changed in this slice before smoke. Existing
`BattleUnit` already routes skills `41/47` through explicit debuff chance.

## Smoke Matrix

Planned output folder:

```text
rebuild_game/build/smoke/phase9l/
```

| Checkpoint | Asserted behavior | Result |
| --- | --- | --- |
| `battle_phase9l_debuff10_success_skill_41` | Forced hit, forced debuff roll `0`, debuff10 text visible, enemy has debuff10. | PASS |
| `battle_phase9l_debuff10_success_skill_47` | Same success path for skill `47`. | PASS |
| `battle_phase9l_debuff10_buff14_block_skill_41` | Target buff14 blocks debuff10 while damage still hits. | PASS |
| `battle_phase9l_debuff10_status3_block_skill_41` | Target status3 reduces explicit chance; roll `9` blocks. | PASS |
| `battle_phase9l_debuff10_miss_queue_skill_41` | Forced miss hides debuff text but source-applied debuff10 still reaches active queue. | PASS |
| `battle_phase9l_debuff10_visual_consumer_skill_41` | P12/P13 active queue starts visual row `[1,19,0,-1,1,6,0,-1]`. | PASS |
| `battle_phase9l_debuff10_catch_chance_after_skill41` | Debuff10 applied by skill41 increases catch chance versus base. | PASS |

## Verification

Commands run:

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-checkpoint <phase9l checkpoint> build\smoke\phase9l\<checkpoint>.png
java -cp build\classes com.vqsv.rebuild.Main --check
java -cp build\classes VqsvBattleDamageFormulaCheck
rg -n "Ã|Â|Æ|Ð|ð|□|�|mojibake" src\main\java
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_sophie_after_battle_branch build\smoke\phase9l_regression\route_sophie_after_battle_branch.png
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_bunny_after_battle_task build\smoke\phase9l_regression\route_bunny_after_battle_task.png
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state build\smoke\phase9l_regression\route_elder_after_battle_reward_state.png
```

Result:

| Check | Result |
| --- | --- |
| Build | PASS |
| Phase 9-L smoke group | PASS |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| Java mojibake literal scan | PASS: no matches |
| Route Sophie/Bunny/Elder | PASS |

Note: one parallel regression run raced with `build.ps1` while classes were
being rebuilt and produced a transient `NoClassDefFoundError`. The same route
regressions passed when run after build completion.

## Current Status

| Area | Status |
| --- | --- |
| Skills `41/47` formula family | PORTED/PARTIAL, smoke-covered. |
| Debuff id `10` producer | PORTED/PARTIAL, smoke-covered. |
| Explicit chance gate | PORTED/PARTIAL, smoke-covered with forced roll. |
| Buff14 block | PORTED/PARTIAL, smoke-covered. |
| Status3 chance reduction | PORTED/PARTIAL, smoke-covered. |
| Miss interaction | PORTED/PARTIAL, smoke-covered. |
| P12/P13 visual consumer | PORTED/PARTIAL, smoke-covered for source row. |
| Catch chance consumer | PORTED/PARTIAL, smoke-covered. |
| Exact RNG stream parity | PARTIAL. Smoke uses deterministic roll hook. |
| Pixel-perfect AH visual | PARTIAL. Row and AH type are source-backed; MIDP pixel compare remains outside this slice. |

## Next Roadmap Step

After Phase 9-L passes, continue Phase 9 skill/status coverage with the next
explicit missing family. Current recommended next slice:

```text
Phase 9-M: zero-power debuff ids 8/9, skills 54/55.
```

Reason:

- They are still marked PENDING in the main matrix.
- Their power is `0`, so they need a careful source audit before copying the
  normal direct-damage debuff pattern.
