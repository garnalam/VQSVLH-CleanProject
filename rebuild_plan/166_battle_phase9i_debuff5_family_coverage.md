# 166 Battle Phase 9-I Debuff Id 5 Family Coverage

Status date: 2026-07-09

Status: PHASE 9-I / IMPLEMENTED-PARTIAL / SMOKE-COVERED.

Purpose:

- Continue Phase 9 debuff-family coverage by debuff id.
- Cover implicit speed debuff id `5` skills: `32/38/61`.
- Prove source producer behavior, anti-debuff gates, miss interaction, and the
  source-backed stat consumer that lowers speed.

## Source Facts

Primary sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/script/decoded/data__script__db.mid.json`
- `modules/script/decoded/data__script__bufDebuf.mid.json`

Skill rows from `db.mid` group 1:

| Skill | Row | Meaning |
| --- | --- | --- |
| `32` | `[3,149,561,60,0,45,2,5,10,0]` | Direct damage, effectMode `2`, debuff id `5`, implicit chance `-1`; speed reduction percent `10`. |
| `38` | `[3,155,567,150,3,15,2,5,10,0]` | Direct damage, effectMode `2`, debuff id `5`, implicit chance `-1`; speed reduction percent `10`. |
| `61` | `[6,178,590,80,0,45,2,5,5,0]` | Direct damage, effectMode `2`, debuff id `5`, implicit chance `-1`; speed reduction percent `5`. |

Debuff row:

| Debuff id | Row | Meaning |
| --- | --- | --- |
| `5` | `[316,327,3]` | Speed-down debuff. |

Visual row from `bufDebuf.mid`:

| Bank | Id | Row | Meaning |
| --- | --- | --- | --- |
| Debuff bank `1` | `5` | `[1,0,0,-1,0,25,0,-1]` | Data exists, but source visual gate does not include debuff id `5`, so active queue applies immediately without P12/P13 visual. |

Source behavior from `game.b.b(target)`:

| Step | Source behavior | Rebuild status |
| --- | --- | --- |
| Damage formula | Skills `32/38/61` are in the direct-simple switch: `raw * skill[3] / 100`. | PORTED/PARTIAL. |
| Debuff producer | `var8_10` stays `-1`, so normal non-status3 targets apply debuff id `5` unless buff14 blocks. | PORTED/PARTIAL. |
| Stored value | `case 5` stores `w[5][1] = target.baseSpeed * aq.c[1][sourceSkill][8] / 100`. | PORTED/PARTIAL. |
| Immediate stat update | Source sets `target.currentSpeed = target.baseSpeed - w[5][1]` when the debuff is applied. | PORTED/PARTIAL. |
| Buff 14 block | If target `m(14)`, debuff is blocked. | PORTED/PARTIAL. |
| Status 3 anti-debuff | If target `f(3)`, source uses `chance * (100 - aq.c[3][3][5]) / 100`. With implicit chance `-1`, Java integer math yields threshold `0`; roll `1` blocks. | PORTED/PARTIAL. |
| Slot write | Applies target `w[5]`, duration from `aq.c[7][5][2]`, source skill id, active flag. | PORTED/PARTIAL. |
| Active queue | Adds active effect bank `1`, id `5`. Source visual gate skips visual for id `5`. | PORTED/PARTIAL. |

Source behavior from `game.b.q(5)`:

| Step | Source behavior | Rebuild status |
| --- | --- | --- |
| Active queue tick | Reasserts `currentSpeed = baseSpeed - w[5][1]`. | PORTED/PARTIAL. |
| Expire/clear | Duration ticks through caller and clear restores mutable stats before reapplying remaining effects. | PORTED/PARTIAL. |

Source behavior from `game.d` P7:

- `game.b.b(target)` applies debuff result before P7 miss/dodge HP damage.
- Therefore a successful debuff producer can enqueue debuff id `5` even when
  the later P7 hit roll misses.
- On miss, P7 hides damage/debuff text and shows dodge text.

## Implementation

Touched code:

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added Phase 9-I debuff id `5` checkpoints and `isPhase9IDebuff5Skill(...)`. |

No gameplay runtime code was changed in this slice. Existing `BattleUnit` and
`VqsvBattleRuntime` already had the source-shaped producer and stat consumer
path.

## Smoke Matrix

Output folder:

```text
rebuild_game/build/smoke/phase9i/
```

| Checkpoint | Asserted behavior | Result |
| --- | --- | --- |
| `battle_phase9i_debuff5_success_skill_32` | Forced hit, debuff5 text visible, enemy has debuff5. | PASS |
| `battle_phase9i_debuff5_success_skill_38` | Same success path for skill `38`. | PASS |
| `battle_phase9i_debuff5_success_skill_61` | Same success path for skill `61`. | PASS |
| `battle_phase9i_debuff5_buff14_block_skill_32` | Target buff14 blocks debuff5 while damage still hits. | PASS |
| `battle_phase9i_debuff5_status3_block_skill_32` | Target status3 plus roll `1` blocks implicit debuff5. | PASS |
| `battle_phase9i_debuff5_miss_queue_skill_32` | Forced miss hides debuff text but source-applied debuff5 reaches immediate active queue apply. | PASS |
| `battle_phase9i_debuff5_stat_consumer_skill_32` | Debuff5 active queue tick applies without visual and logs speed transition. | PASS |
| `battle_p12_debuff5_stat_skip_visual` | Existing source-gate regression for debuff5 no-visual tick. | PASS |

## Verification

Commands run:

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-checkpoint <phase9i checkpoint> build\smoke\phase9i\<checkpoint>.png
java -cp build\classes com.vqsv.rebuild.Main --check
java -cp build\classes VqsvBattleDamageFormulaCheck
rg -n "Ã|Â|Æ|Ð|ð|□|�|mojibake" src\main\java
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_sophie_after_battle_branch build\smoke\phase9i_regression\route_sophie_after_battle_branch.png
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_bunny_after_battle_task build\smoke\phase9i_regression\route_bunny_after_battle_task.png
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state build\smoke\phase9i_regression\route_elder_after_battle_reward_state.png
```

Result:

| Check | Result |
| --- | --- |
| Build | PASS |
| Phase 9-I smoke group | PASS |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| Java mojibake literal scan | PASS: no matches |
| Route Sophie/Bunny/Elder | PASS |

Note: PowerShell console output can show Vietnamese as mojibake because of
console encoding. The Java source mojibake scan passed.

## Current Status

| Area | Status |
| --- | --- |
| Skills `32/38/61` formula family | PORTED/PARTIAL, smoke-covered. |
| Debuff id `5` producer | PORTED/PARTIAL, smoke-covered. |
| Stored speed reduction value `w[5][1]` | PORTED/PARTIAL. |
| Immediate speed-down stat update | PORTED/PARTIAL. |
| Buff14 block | PORTED/PARTIAL, smoke-covered. |
| Status3 block for implicit chance | PORTED/PARTIAL, smoke-covered. |
| Miss interaction | PORTED/PARTIAL: P7 text suppressed, queue can still occur. |
| Debuff5 active queue tick | PORTED/PARTIAL: source no-visual stat reassert, smoke-covered. |
| Exact RNG stream parity | PARTIAL. Smoke uses deterministic roll hook. |
| Pixel-perfect active queue visual | Not applicable for id `5` in current source gate; broad visual parity remains PENDING. |
| HUD buff/debuff icon sprite 325 | PENDING UI parity later; see `163_battle_status_icon_sprite325_img804_audit.md`. |

## Next Roadmap Step

Continue Phase 9 by debuff id.

Recommended next slice:

```text
Phase 9-J: debuff id 6 family, skills 33/39.
```

Why:

- Debuff id `6` is the next family and has gameplay impact in the damage
  formula: attacker damage is reduced by `w[6][1]`.
- It should include producer coverage plus a follow-up formula assertion, not
  only visible P7 text.

Do not start `SOURCE_SWITCH_GAP` skills yet.
