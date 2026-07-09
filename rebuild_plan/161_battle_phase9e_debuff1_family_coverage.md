# 161 Battle Phase 9-E Debuff Id 1 Family Coverage

Status date: 2026-07-09

Status: PHASE 9-E / IMPLEMENTED-PARTIAL / SMOKE-COVERED.

Purpose:

- Start debuff-family coverage by debuff id.
- Cover explicit debuff id `1` skills: `2/8/22/28`.
- Prove source producer behavior, P7 hit/miss display behavior, block gates,
  and P12 active queue lifecycle.

## Source Facts

Primary sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/script/decoded/data__script__db.mid.json`
- `modules/script/decoded/data__script__bufDebuf.mid.json`

Skill rows from `db.mid` group 1:

| Skill | Row | Meaning |
| --- | --- | --- |
| `2` | `[0,119,531,100,0,45,2,1,10,0]` | Direct damage, effectMode `2`, debuff id `1`, chance `10`. |
| `8` | `[0,125,537,200,3,15,2,1,20,0]` | Direct damage, effectMode `2`, debuff id `1`, chance `20`. |
| `22` | `[2,139,551,50,0,45,2,1,25,0]` | Direct damage, effectMode `2`, debuff id `1`, chance `25`. |
| `28` | `[2,145,557,150,3,15,2,1,25,0]` | Direct damage, effectMode `2`, debuff id `1`, chance `25`. |

Debuff row:

| Debuff id | Row | Meaning |
| --- | --- | --- |
| `1` | `[312,323,2]` | Flag/duration debuff. No HP/stat tick body in `game.b.q(int)`; used by conditional skills `23/29`. |

Visual row from `bufDebuf.mid`:

| Bank | Id | Row | Meaning |
| --- | --- | --- | --- |
| Debuff bank `1` | `1` | `[1,14,0,-1]` | P12/P13 active queue uses speffect `14`, AH type `12` in rebuild smoke. |

Source behavior from `game.b.b(target)`:

| Step | Source behavior | Rebuild status |
| --- | --- | --- |
| Damage formula | Skills `2/8/22/28` use `raw * skill[3] / 100`. | PORTED/PARTIAL. |
| Explicit debuff chance | `var3_3 = aq.c[1][skill][8]`, then `ae.a(100) > chance` blocks. | PORTED/PARTIAL with deterministic smoke hook. |
| Status 3 anti-debuff | If target `f(3)`, apply chance becomes `chance * (100 - aq.c[3][3][5]) / 100`. | PORTED/PARTIAL. Skill2 chance 10 with status3 param 20 becomes 8; roll 9 blocks. |
| Buff 14 block | If target `m(14)`, debuff is blocked. | PORTED/PARTIAL. |
| Slot write | Applies target `w[1]`, duration from `aq.c[7][1][2]`, source skill id, active flag. | PORTED/PARTIAL. |
| Active queue | Adds active effect bank `1`, id `1`. | PORTED/PARTIAL. |

Source behavior from `game.d` P7:

- `game.b.b(target)` is called before P7 miss/dodge applies HP damage.
- Therefore a successful debuff producer can set target debuff state even if
  the later P7 hit roll misses.
- On miss, P7 hides damage/debuff text and shows `Ne tranh` / localized dodge
  text, but the source-applied debuff queue can still exist.

This corrects the earlier Phase 9-A planning wording that suggested a miss
would suppress debuff queue application. The source-backed behavior is:

```text
miss suppresses P7 debuff text, not necessarily the debuff producer state.
```

## Implementation

Touched code:

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleUnit.java` | Added smoke-only deterministic `damage.debuff` roll hook. |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Added smoke wrappers for forced debuff roll and enemy form-status setup. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added Phase 9-E debuff id 1 checkpoints. |

New smoke-only helpers:

| Helper | Purpose |
| --- | --- |
| `BattleUnit.setNextDebuffRollForChecks(int)` | Forces the next `damage.debuff` roll only. |
| `SourceBattleRuntime.debugSetNextDamageDebuffRollForSmoke(int)` | Runtime wrapper for the forced debuff roll. |
| `SourceBattleRuntime.debugEnemyFormStatusForSmoke(...)` | Sets enemy source form/status for anti-debuff smoke. |

## Smoke Matrix

Output folder:

```text
rebuild_game/build/smoke/phase9e/
```

| Checkpoint | Asserted behavior | Result |
| --- | --- | --- |
| `battle_phase9e_debuff1_success_skill_2` | Forced debuff roll `0`, forced hit, debuff text visible, enemy has debuff1. | PASS |
| `battle_phase9e_debuff1_success_skill_8` | Same success path for skill `8`. | PASS |
| `battle_phase9e_debuff1_success_skill_22` | Same success path for skill `22`. | PASS |
| `battle_phase9e_debuff1_success_skill_28` | Same success path for skill `28`. | PASS |
| `battle_phase9e_debuff1_buff14_block_skill_2` | Target buff14 blocks debuff1 while damage still hits. | PASS |
| `battle_phase9e_debuff1_status3_reduced_block_skill_2` | Target status3 reduces chance; roll `9` blocks skill2 debuff after reduced chance `8`. | PASS |
| `battle_phase9e_debuff1_miss_queue_skill_2` | Forced debuff success plus forced miss: P7 dodge text/no debuff text, debuff1 remains and reaches P12 queue. | PASS |

## Verification

Commands run:

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-checkpoint <phase9e checkpoint> build\smoke\phase9e\<checkpoint>.png
java -cp build\classes com.vqsv.rebuild.Main --check
java -cp build\classes VqsvBattleDamageFormulaCheck
rg -n "Ã|Â|Æ|Ð|ð|□|�|mojibake" src\main\java
```

Result:

| Check | Result |
| --- | --- |
| Build | PASS |
| Phase 9-E smoke group | PASS |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| Java mojibake literal scan | PASS: no matches |
| `battle_p12_debuff1_type12_special` | PASS |
| `battle_phase9d_miss_leech_skill_58` | PASS |
| `battle_phase9d_miss_followup_skill_69` | PASS |
| `route_sophie_after_battle_branch` | PASS |
| `route_bunny_after_battle_task` | PASS |
| `route_elder_after_battle_reward_state` | PASS |

## Current Status

| Area | Status |
| --- | --- |
| Skills `2/8/22/28` formula family | PORTED/PARTIAL, smoke-covered. |
| Debuff id `1` producer chance | PORTED/PARTIAL, deterministic success smoke-covered. |
| Buff14 block | PORTED/PARTIAL, smoke-covered. |
| Status3 reduced chance | PORTED/PARTIAL, smoke-covered. |
| Miss interaction | SOURCE-CORRECTED: P7 text suppressed, queue still possible; smoke-covered. |
| Debuff1 P12 visual | PORTED/PARTIAL; pre-existing `battle_p12_debuff1_type12_special` plus Phase 9-E queue smoke. |
| Exact RNG stream parity | PARTIAL. Smoke uses deterministic roll hook. |
| Pixel-perfect active queue visual | PENDING outside this slice. |

## Next Roadmap Step

Continue Phase 9-E by debuff id.

Recommended next slice:

```text
Phase 9-F: debuff id 2 family, skills 12/18.
```

Why:

- Debuff id `2` already has a P12 visual smoke anchor.
- It is an implicit direct-simple debuff family, smaller than broad debuff id
  `4/5/7/10` stat/damage-impact families.
- It continues the same source-backed matrix pattern before touching
  `SOURCE_SWITCH_GAP`.

Do not start `SOURCE_SWITCH_GAP` skills yet.
