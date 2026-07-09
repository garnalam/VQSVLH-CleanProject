# 162 Battle Phase 9-F Debuff Id 2 Family Coverage

Status date: 2026-07-09

Status: PHASE 9-F / IMPLEMENTED-PARTIAL / SMOKE-COVERED.

Purpose:

- Continue Phase 9 debuff-family coverage by debuff id.
- Cover implicit debuff id `2` skills: `12/18`.
- Prove source producer behavior, anti-debuff gates, miss interaction, and P12
  active queue lifecycle.

## Source Facts

Primary sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/script/decoded/data__script__db.mid.json`
- `modules/script/decoded/data__script__bufDebuf.mid.json`

Skill rows from `db.mid` group 1:

| Skill | Row | Meaning |
| --- | --- | --- |
| `12` | `[1,129,541,50,0,45,2,2,-1,0]` | Direct damage, effectMode `2`, debuff id `2`, implicit chance `-1`. |
| `18` | `[1,135,547,150,3,15,2,2,-1,0]` | Direct damage, effectMode `2`, debuff id `2`, implicit chance `-1`. |

Debuff row:

| Debuff id | Row | Meaning |
| --- | --- | --- |
| `2` | `[313,324,3]` | Flag/duration debuff. `game.b.q(2)` has no HP/stat tick body. |

Visual row from `bufDebuf.mid`:

| Bank | Id | Row | Meaning |
| --- | --- | --- | --- |
| Debuff bank `1` | `2` | `[0,21,0,0,1,6,0,0]` | P12/P13 active queue has actor action segment then speffect `6` AH type `8`. |

Source behavior from `game.b.b(target)`:

| Step | Source behavior | Rebuild status |
| --- | --- | --- |
| Damage formula | Skills `12/18` are in the direct-simple switch: `raw * skill[3] / 100`. | PORTED/PARTIAL. |
| Debuff chance | `var8_10` stays `-1`, so normal non-status3 targets apply debuff id `2` unless buff14 blocks. | PORTED/PARTIAL. |
| Buff 14 block | If target `m(14)`, debuff is blocked. | PORTED/PARTIAL. |
| Status 3 anti-debuff | If target `f(3)`, source uses `chance * (100 - aq.c[3][3][5]) / 100`. With implicit chance `-1`, Java integer math yields threshold `0`; roll `1` blocks. | PORTED/PARTIAL. |
| Slot write | Applies target `w[2]`, duration from `aq.c[7][2][2]`, source skill id, active flag. | PORTED/PARTIAL. |
| Active queue | Adds active effect bank `1`, id `2`. | PORTED/PARTIAL. |

Source behavior from `game.d` P7:

- `game.b.b(target)` applies debuff result before P7 miss/dodge HP damage.
- Therefore a successful debuff producer can still enqueue debuff id `2` even
  when the later P7 hit roll misses.
- On miss, P7 hides damage/debuff text and shows dodge text.

## Implementation

Touched code:

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added Phase 9-F debuff id `2` checkpoints. |

This slice reused smoke-only helpers added in Phase 9-E:

| Helper | Purpose |
| --- | --- |
| `debugSetNextDamageDebuffRollForSmoke(int)` | Deterministic status3 anti-debuff roll. |
| `debugEnemyFormStatusForSmoke(...)` | Sets target source form/status `3`. |

No runtime behavior change was required for normal gameplay in this slice.

## Smoke Matrix

Output folder:

```text
rebuild_game/build/smoke/phase9f/
```

| Checkpoint | Asserted behavior | Result |
| --- | --- | --- |
| `battle_phase9f_debuff2_success_skill_12` | Forced hit, debuff2 text visible, enemy has debuff2. | PASS |
| `battle_phase9f_debuff2_success_skill_18` | Same success path for skill `18`. | PASS |
| `battle_phase9f_debuff2_buff14_block_skill_12` | Target buff14 blocks debuff2 while damage still hits. | PASS |
| `battle_phase9f_debuff2_status3_block_skill_12` | Target status3 plus roll `1` blocks implicit debuff2. | PASS |
| `battle_phase9f_debuff2_miss_queue_skill_12` | Forced miss hides debuff text but source-applied debuff2 reaches P12 queue. | PASS |

## Verification

Commands run:

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-checkpoint <phase9f checkpoint> build\smoke\phase9f\<checkpoint>.png
java -cp build\classes com.vqsv.rebuild.Main --check
java -cp build\classes VqsvBattleDamageFormulaCheck
rg -n "Ã|Â|Æ|Ð|ð|□|�|mojibake" src\main\java
```

Result:

| Check | Result |
| --- | --- |
| Build | PASS |
| Phase 9-F smoke group | PASS |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| Java mojibake literal scan | PASS: no matches |
| `battle_p12_debuff2_type8_special` | PASS |
| `battle_phase9e_debuff1_miss_queue_skill_2` | PASS |
| `battle_phase9d_miss_followup_skill_69` | PASS |
| `route_sophie_after_battle_branch` | PASS |
| `route_bunny_after_battle_task` | PASS |
| `route_elder_after_battle_reward_state` | PASS |

## Current Status

| Area | Status |
| --- | --- |
| Skills `12/18` formula family | PORTED/PARTIAL, smoke-covered. |
| Debuff id `2` producer | PORTED/PARTIAL, smoke-covered. |
| Buff14 block | PORTED/PARTIAL, smoke-covered. |
| Status3 block for implicit chance | PORTED/PARTIAL, smoke-covered. |
| Miss interaction | PORTED/PARTIAL: P7 text suppressed, queue can still occur. |
| Debuff2 P12 visual | PORTED/PARTIAL; existing `battle_p12_debuff2_type8_special` plus Phase 9-F queue smoke. |
| Exact RNG stream parity | PARTIAL. Smoke uses deterministic roll hook. |
| Pixel-perfect active queue visual | PENDING outside this slice. |

## Next Roadmap Step

Continue Phase 9 by debuff id.

Recommended next slice:

```text
Phase 9-G: debuff id 3 family, skills 13/19.
```

Why:

- Debuff id `3` is the next small direct-simple debuff family.
- It has a different lifecycle: `game.b.q(3)` only applies HP damage when
  duration is about to expire.
- There are already active queue anchors for debuff3, but source-specific
  skill-family success/block/miss coverage is still needed.

Do not start `SOURCE_SWITCH_GAP` skills yet.
