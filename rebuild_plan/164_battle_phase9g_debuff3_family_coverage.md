# 164 Battle Phase 9-G Debuff Id 3 Family Coverage

Status date: 2026-07-09

Status: PHASE 9-G / IMPLEMENTED-PARTIAL / SMOKE-COVERED.

Purpose:

- Continue Phase 9 debuff-family coverage by debuff id.
- Cover implicit delayed-damage debuff id `3` skills: `13/19`.
- Prove source producer behavior, anti-debuff gates, miss interaction, and the
  existing P12 active queue delayed-damage consumer.

## Source Facts

Primary sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/script/decoded/data__script__db.mid.json`
- `modules/script/decoded/data__script__bufDebuf.mid.json`

Skill rows from `db.mid` group 1:

| Skill | Row | Meaning |
| --- | --- | --- |
| `13` | `[1,130,542,50,1,30,2,3,150,0]` | Direct damage, effectMode `2`, debuff id `3`, implicit chance `-1`; `skill[8]` = `150` for delayed HP-delta. |
| `19` | `[1,136,548,150,3,15,2,3,200,0]` | Direct damage, effectMode `2`, debuff id `3`, implicit chance `-1`; `skill[8]` = `200` for delayed HP-delta. |

Debuff row:

| Debuff id | Row | Meaning |
| --- | --- | --- |
| `3` | `[314,325,3]` | Delayed damage-over-time percent branch. |

Visual row from `bufDebuf.mid`:

| Bank | Id | Row | Meaning |
| --- | --- | --- | --- |
| Debuff bank `1` | `3` | `[0,21,0,-1]` | P12/P13 active queue uses type0 actor action row. Existing smoke expects sprite `263`, state `0`. |

Source behavior from `game.b.b(target)`:

| Step | Source behavior | Rebuild status |
| --- | --- | --- |
| Damage formula | Skills `13/19` are in the direct-simple switch: `raw * skill[3] / 100`. | PORTED/PARTIAL. |
| Debuff producer | `var8_10` stays `-1`, so normal non-status3 targets apply debuff id `3` unless buff14 blocks. | PORTED/PARTIAL. |
| Stored value | `case 3` stores `w[3][1] = var6_8`, the pre-skill raw/base damage value. | PORTED/PARTIAL. |
| Buff 14 block | If target `m(14)`, debuff is blocked. | PORTED/PARTIAL. |
| Status 3 anti-debuff | If target `f(3)`, source uses `chance * (100 - aq.c[3][3][5]) / 100`. With implicit chance `-1`, Java integer math yields threshold `0`; roll `1` blocks. | PORTED/PARTIAL. |
| Slot write | Applies target `w[3]`, duration from `aq.c[7][3][2]`, source skill id, active flag. | PORTED/PARTIAL. |
| Active queue | Adds active effect bank `1`, id `3`. | PORTED/PARTIAL. |

Source behavior from `game.b.q(3)`:

| Step | Source behavior | Rebuild status |
| --- | --- | --- |
| Delayed HP delta | Only when `w[3][0] <= 1`, damage by `w[3][1] * aq.c[1][sourceSkill][8] / 100`. | PORTED/PARTIAL. |
| Death state | If unit dies after HP delta, actor state becomes `3`. | PARTIAL; broader death/replacement parity remains separate. |

Source behavior from `game.d` P7:

- `game.b.b(target)` applies debuff result before P7 miss/dodge HP damage.
- Therefore a successful debuff producer can enqueue debuff id `3` even when
  the later P7 hit roll misses.
- On miss, P7 hides damage/debuff text and shows dodge text.

## Implementation

Touched code:

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added Phase 9-G debuff id `3` checkpoints and `isPhase9GDebuff3Skill(...)`. |

No runtime gameplay code was changed in this slice. Existing `BattleUnit` and
`VqsvBattleRuntime` already had the source-shaped producer/consumer path needed
for this family.

## Smoke Matrix

Output folder:

```text
rebuild_game/build/smoke/phase9g/
```

| Checkpoint | Asserted behavior | Result |
| --- | --- | --- |
| `battle_phase9g_debuff3_success_skill_13` | Forced hit, debuff3 text visible, enemy has debuff3. | PASS |
| `battle_phase9g_debuff3_success_skill_19` | Same success path for skill `19`. | PASS |
| `battle_phase9g_debuff3_buff14_block_skill_13` | Target buff14 blocks debuff3 while damage still hits. | PASS |
| `battle_phase9g_debuff3_status3_block_skill_13` | Target status3 plus roll `1` blocks implicit debuff3. | PASS |
| `battle_phase9g_debuff3_miss_queue_skill_13` | Forced miss hides debuff text but source-applied debuff3 reaches P12 queue. | PASS |
| `battle_p12_debuff3_queue_start` | Existing consumer visual starts type0 actor action row `[0,21,0,-1]`. | PASS |
| `battle_p12_debuff3_after_apply` | Existing consumer applies and exits active queue. | PASS |

## Verification

Commands run:

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-checkpoint <phase9g checkpoint> build\smoke\phase9g\<checkpoint>.png
java -cp build\classes com.vqsv.rebuild.Main --check
java -cp build\classes VqsvBattleDamageFormulaCheck
rg -n "Ã|Â|Æ|Ð|ð|□|�|mojibake" src\main\java
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_sophie_after_battle_branch build\smoke\phase9g_regression\route_sophie_after_battle_branch.png
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_bunny_after_battle_task build\smoke\phase9g_regression\route_bunny_after_battle_task.png
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state build\smoke\phase9g_regression\route_elder_after_battle_reward_state.png
```

Result:

| Check | Result |
| --- | --- |
| Build | PASS |
| Phase 9-G smoke group | PASS |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| Java mojibake literal scan | PASS: no matches |
| Route Sophie/Bunny/Elder | PASS |

Note: PowerShell console output can show Vietnamese as mojibake because of
console encoding. The Java source mojibake scan passed.

## Current Status

| Area | Status |
| --- | --- |
| Skills `13/19` formula family | PORTED/PARTIAL, smoke-covered. |
| Debuff id `3` producer | PORTED/PARTIAL, smoke-covered. |
| Stored raw value for delayed damage | PORTED/PARTIAL. |
| Buff14 block | PORTED/PARTIAL, smoke-covered. |
| Status3 block for implicit chance | PORTED/PARTIAL, smoke-covered. |
| Miss interaction | PORTED/PARTIAL: P7 text suppressed, queue can still occur. |
| Debuff3 P12 delayed HP-delta consumer | PORTED/PARTIAL; existing consumer smoke re-run in this slice. |
| Exact RNG stream parity | PARTIAL. Smoke uses deterministic roll hook. |
| Pixel-perfect active queue visual | PENDING outside this slice. |
| HUD buff/debuff icon sprite 325 | PENDING UI parity later; see `163_battle_status_icon_sprite325_img804_audit.md`. |

## Next Roadmap Step

Continue Phase 9 by debuff id.

Recommended next slice:

```text
Phase 9-H: debuff id 4 family, skills 31/37.
```

Why:

- Debuff id `4` is the next direct-simple implicit debuff family.
- It is behavior-light in `game.b.q(4)` but still participates in formula
  through source speed/miss-related logic.
- It should be audited separately before moving to stat-changing ids `5/7` or
  high-risk debuff id `10`.

Do not start `SOURCE_SWITCH_GAP` skills yet.
