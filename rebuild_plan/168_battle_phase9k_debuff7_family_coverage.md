# 168 Battle Phase 9-K Debuff Id 7 Family Coverage

Status date: 2026-07-09

Status: PHASE 9-K / IMPLEMENTED-PARTIAL / SMOKE-COVERED.

Purpose:

- Continue Phase 9 debuff-family coverage by debuff id.
- Cover implicit defense debuff id `7` skills: `51/57`.
- Prove source producer behavior, anti-debuff gates, miss interaction, and the
  source-backed stat consumer that lowers defense.

## Source Facts

Primary sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/script/decoded/data__script__db.mid.json`
- `modules/script/decoded/data__script__bufDebuf.mid.json`

Skill rows from `db.mid` group 1:

| Skill | Row | Meaning |
| --- | --- | --- |
| `51` | `[5,168,580,80,0,45,2,7,20,0]` | Direct damage, effectMode `2`, debuff id `7`, implicit chance `-1`; defense reduction percent `20`. |
| `57` | `[5,174,586,120,2,30,2,7,20,0]` | Direct damage, effectMode `2`, debuff id `7`, implicit chance `-1`; defense reduction percent `20`. |

Debuff row:

| Debuff id | Row | Meaning |
| --- | --- | --- |
| `7` | `[318,329,3]` | Defense-down debuff. |

Visual row from `bufDebuf.mid`:

| Bank | Id | Row | Meaning |
| --- | --- | --- | --- |
| Debuff bank `1` | `7` | `[1,19,0,-1,1,6,0,-1]` | Data exists, but source visual gate does not include debuff id `7`, so active queue applies immediately without P12/P13 visual. |

Source behavior from `game.b.b(target)`:

| Step | Source behavior | Rebuild status |
| --- | --- | --- |
| Damage formula | Skills `51/57` are in the direct-simple switch: `raw * skill[3] / 100`. | PORTED/PARTIAL. |
| Debuff producer | `var8_10` stays `-1`, so normal non-status3 targets apply debuff id `7` unless buff14 blocks. | PORTED/PARTIAL. |
| Stored value | `case 7` stores `w[7][1] = target.baseDefense * aq.c[1][sourceSkill][8] / 100`. | PORTED/PARTIAL. |
| Immediate stat update | Source sets `target.currentDefense = target.baseDefense - w[7][1]` when the debuff is applied. | PORTED/PARTIAL. |
| Buff 14 block | If target `m(14)`, debuff is blocked. | PORTED/PARTIAL. |
| Status 3 anti-debuff | If target `f(3)`, source uses `chance * (100 - aq.c[3][3][5]) / 100`. With implicit chance `-1`, Java integer math yields threshold `0`; roll `1` blocks. | PORTED/PARTIAL. |
| Slot write | Applies target `w[7]`, duration from `aq.c[7][7][2]`, source skill id, active flag. | PORTED/PARTIAL. |
| Active queue | Adds active effect bank `1`, id `7`. Source visual gate skips visual for id `7`. | PORTED/PARTIAL. |

Source behavior from `game.b.q(7)`:

| Step | Source behavior | Rebuild status |
| --- | --- | --- |
| Active queue tick | Reasserts `currentDefense = baseDefense - w[7][1]`. | PORTED/PARTIAL. |
| Expire/clear | Duration ticks through caller and clear restores mutable stats before reapplying remaining effects. | PORTED/PARTIAL. |

Source behavior from `game.d` P7:

- `game.b.b(target)` applies debuff result before P7 miss/dodge HP damage.
- Therefore a successful debuff producer can enqueue debuff id `7` even when
  the later P7 hit roll misses.
- On miss, P7 hides damage/debuff text and shows dodge text.

## Implementation

Touched code:

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added Phase 9-K debuff id `7` checkpoints and `isPhase9KDebuff7Skill(...)`. |

No gameplay runtime code was changed in this slice. Existing `BattleUnit` and
`VqsvBattleRuntime` already had the source-shaped producer and stat consumer
path.

## Smoke Matrix

Output folder:

```text
rebuild_game/build/smoke/phase9k/
```

| Checkpoint | Asserted behavior | Result |
| --- | --- | --- |
| `battle_phase9k_debuff7_success_skill_51` | Forced hit, debuff7 text visible, enemy has debuff7. | PASS |
| `battle_phase9k_debuff7_success_skill_57` | Same success path for skill `57`. | PASS |
| `battle_phase9k_debuff7_buff14_block_skill_51` | Target buff14 blocks debuff7 while damage still hits. | PASS |
| `battle_phase9k_debuff7_status3_block_skill_51` | Target status3 plus roll `1` blocks implicit debuff7. | PASS |
| `battle_phase9k_debuff7_miss_queue_skill_51` | Forced miss hides debuff text but source-applied debuff7 reaches immediate active queue apply. | PASS |
| `battle_phase9k_debuff7_stat_consumer_skill_51` | Debuff7 active queue tick applies without visual and logs defense transition. | PASS |

## Verification

Commands run:

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-checkpoint <phase9k checkpoint> build\smoke\phase9k\<checkpoint>.png
java -cp build\classes com.vqsv.rebuild.Main --check
java -cp build\classes VqsvBattleDamageFormulaCheck
rg -n "Ã|Â|Æ|Ð|ð|□|�|mojibake" src\main\java
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_sophie_after_battle_branch build\smoke\phase9k_regression\route_sophie_after_battle_branch.png
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_bunny_after_battle_task build\smoke\phase9k_regression\route_bunny_after_battle_task.png
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state build\smoke\phase9k_regression\route_elder_after_battle_reward_state.png
```

Result:

| Check | Result |
| --- | --- |
| Build | PASS |
| Phase 9-K smoke group | PASS |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| Java mojibake literal scan | PASS: no matches |
| Route Sophie/Bunny/Elder | PASS |

Note: PowerShell console output can show Vietnamese as mojibake because of
console encoding. The Java source mojibake scan passed.

## Current Status

| Area | Status |
| --- | --- |
| Skills `51/57` formula family | PORTED/PARTIAL, smoke-covered. |
| Debuff id `7` producer | PORTED/PARTIAL, smoke-covered. |
| Stored defense reduction value `w[7][1]` | PORTED/PARTIAL. |
| Immediate defense-down stat update | PORTED/PARTIAL. |
| Buff14 block | PORTED/PARTIAL, smoke-covered. |
| Status3 block for implicit chance | PORTED/PARTIAL, smoke-covered. |
| Miss interaction | PORTED/PARTIAL: P7 text suppressed, queue can still occur. |
| Debuff7 active queue tick | PORTED/PARTIAL: source no-visual stat reassert, smoke-covered. |
| Exact RNG stream parity | PARTIAL. Smoke uses deterministic roll hook. |
| Pixel-perfect active queue visual | Not applicable for id `7` in current source gate; broad visual parity remains PENDING. |
| HUD buff/debuff icon sprite 325 | PENDING UI parity later; see `163_battle_status_icon_sprite325_img804_audit.md`. |

## Next Roadmap Step

Continue Phase 9 by debuff id.

Recommended next slice:

```text
Phase 9-L: debuff id 10 family, skills 41/47.
```

Why:

- Debuff ids `1..7` are now covered by family smoke except id `0`, which had
  earlier P12 anchors and Phase 9-C plus-divisor coverage.
- Debuff id `10` is the remaining explicit-chance debuff family in the current
  roadmap bucket.
- It needs audit first because it also appears in catch chance/status logic.

Do not start `SOURCE_SWITCH_GAP` skills yet.
