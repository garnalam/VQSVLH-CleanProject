# 167 Battle Phase 9-J Debuff Id 6 Family Coverage

Status date: 2026-07-09

Status: PHASE 9-J / IMPLEMENTED-PARTIAL / SMOKE-COVERED.

Purpose:

- Continue Phase 9 debuff-family coverage by debuff id.
- Cover implicit damage-reduction debuff id `6` skills: `33/39`.
- Prove source producer behavior, anti-debuff gates, miss interaction, and the
  source-backed damage formula consumer.

## Source Facts

Primary sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/script/decoded/data__script__db.mid.json`
- `modules/script/decoded/data__script__bufDebuf.mid.json`

Skill rows from `db.mid` group 1:

| Skill | Row | Meaning |
| --- | --- | --- |
| `33` | `[3,150,562,100,1,30,2,6,10,0]` | Direct damage, effectMode `2`, debuff id `6`, implicit chance `-1`; damage reduction percent `10`. |
| `39` | `[3,156,568,150,3,15,2,6,10,0]` | Direct damage, effectMode `2`, debuff id `6`, implicit chance `-1`; damage reduction percent `10`. |

Debuff row:

| Debuff id | Row | Meaning |
| --- | --- | --- |
| `6` | `[317,328,3]` | Outgoing damage reduction debuff. |

Visual row from `bufDebuf.mid`:

| Bank | Id | Row | Meaning |
| --- | --- | --- | --- |
| Debuff bank `1` | `6` | `[1,12,0,-1]` | Data exists, but source visual gate does not include debuff id `6`, so active queue applies immediately without P12/P13 visual. |

Source behavior from `game.b.b(target)`:

| Step | Source behavior | Rebuild status |
| --- | --- | --- |
| Damage formula | Skills `33/39` are in the direct-simple switch: `raw * skill[3] / 100`. | PORTED/PARTIAL. |
| Debuff producer | `var8_10` stays `-1`, so normal non-status3 targets apply debuff id `6` unless buff14 blocks. | PORTED/PARTIAL. |
| Stored value | `case 6` stores `w[6][1] = aq.c[1][sourceSkill][8]`. | PORTED/PARTIAL. |
| Formula consumer | If attacker has debuff6, source does `damage -= damage * w[6][1] / 100`. | PORTED/PARTIAL, smoke-covered with preloaded value. |
| Buff 14 block | If target `m(14)`, debuff is blocked. | PORTED/PARTIAL. |
| Status 3 anti-debuff | If target `f(3)`, source uses `chance * (100 - aq.c[3][3][5]) / 100`. With implicit chance `-1`, Java integer math yields threshold `0`; roll `1` blocks. | PORTED/PARTIAL. |
| Slot write | Applies target `w[6]`, duration from `aq.c[7][6][2]`, source skill id, active flag. | PORTED/PARTIAL. |
| Active queue | Adds active effect bank `1`, id `6`. Source visual gate skips visual for id `6`. | PORTED/PARTIAL. |

Source behavior from `game.b.q(6)`:

| Step | Source behavior | Rebuild status |
| --- | --- | --- |
| Active queue tick | `case 6` returns immediately; duration still ticks/clears through caller. | PORTED/PARTIAL. |

Source behavior from `game.d` P7:

- `game.b.b(target)` applies debuff result before P7 miss/dodge HP damage.
- Therefore a successful debuff producer can enqueue debuff id `6` even when
  the later P7 hit roll misses.
- On miss, P7 hides damage/debuff text and shows dodge text.

## Implementation

Touched code:

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added Phase 9-J debuff id `6` checkpoints, `isPhase9JDebuff6Skill(...)`, and `latestTraceDamage(...)` smoke helper. |

No gameplay runtime code was changed in this slice. Existing `BattleUnit` and
`VqsvBattleRuntime` already had the source-shaped producer, no-op active queue,
and formula consumer path.

## Smoke Matrix

Output folder:

```text
rebuild_game/build/smoke/phase9j/
```

| Checkpoint | Asserted behavior | Result |
| --- | --- | --- |
| `battle_phase9j_debuff6_success_skill_33` | Forced hit, debuff6 text visible, enemy has debuff6. | PASS |
| `battle_phase9j_debuff6_success_skill_39` | Same success path for skill `39`. | PASS |
| `battle_phase9j_debuff6_buff14_block_skill_33` | Target buff14 blocks debuff6 while damage still hits. | PASS |
| `battle_phase9j_debuff6_status3_block_skill_33` | Target status3 plus roll `1` blocks implicit debuff6. | PASS |
| `battle_phase9j_debuff6_miss_queue_skill_33` | Forced miss hides debuff text but source-applied debuff6 reaches immediate active queue apply. | PASS |
| `battle_phase9j_debuff6_damage_reduction_skill_33` | Preloaded player debuff6 reduces outgoing skill10 damage through formula consumer. | PASS |

## Verification

Commands run:

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-checkpoint <phase9j checkpoint> build\smoke\phase9j\<checkpoint>.png
java -cp build\classes com.vqsv.rebuild.Main --check
java -cp build\classes VqsvBattleDamageFormulaCheck
rg -n "Ã|Â|Æ|Ð|ð|□|�|mojibake" src\main\java
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_sophie_after_battle_branch build\smoke\phase9j_regression\route_sophie_after_battle_branch.png
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_bunny_after_battle_task build\smoke\phase9j_regression\route_bunny_after_battle_task.png
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state build\smoke\phase9j_regression\route_elder_after_battle_reward_state.png
```

Result:

| Check | Result |
| --- | --- |
| Build | PASS |
| Phase 9-J smoke group | PASS |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| Java mojibake literal scan | PASS: no matches |
| Route Sophie/Bunny/Elder | PASS |

Note: PowerShell console output can show Vietnamese as mojibake because of
console encoding. The Java source mojibake scan passed.

## Current Status

| Area | Status |
| --- | --- |
| Skills `33/39` formula family | PORTED/PARTIAL, smoke-covered. |
| Debuff id `6` producer | PORTED/PARTIAL, smoke-covered. |
| Stored damage reduction value `w[6][1]` | PORTED/PARTIAL. |
| Formula consumer | PORTED/PARTIAL, smoke-covered with deterministic preloaded debuff6 value. |
| Buff14 block | PORTED/PARTIAL, smoke-covered. |
| Status3 block for implicit chance | PORTED/PARTIAL, smoke-covered. |
| Miss interaction | PORTED/PARTIAL: P7 text suppressed, queue can still occur. |
| Debuff6 active queue tick | PORTED/PARTIAL: source no-op visual-skip apply. |
| Exact RNG stream parity | PARTIAL. Smoke uses deterministic roll hook. |
| Pixel-perfect active queue visual | Not applicable for id `6` in current source gate; broad visual parity remains PENDING. |
| HUD buff/debuff icon sprite 325 | PENDING UI parity later; see `163_battle_status_icon_sprite325_img804_audit.md`. |

## Next Roadmap Step

Continue Phase 9 by debuff id.

Recommended next slice:

```text
Phase 9-K: debuff id 7 family, skills 51/57.
```

Why:

- Debuff id `7` is the next family and has gameplay impact: it lowers target
  defense by a percent-derived value.
- It should mirror the Phase 9-I pattern: producer coverage plus stat consumer
  assertion.

Do not start `SOURCE_SWITCH_GAP` skills yet.
