# 165 Battle Phase 9-H Debuff Id 4 Family Coverage

Status date: 2026-07-09

Status: PHASE 9-H / IMPLEMENTED-PARTIAL / SMOKE-COVERED.

Purpose:

- Continue Phase 9 debuff-family coverage by debuff id.
- Cover implicit debuff id `4` skills: `31/37`.
- Prove source producer behavior, anti-debuff gates, miss interaction, and the
  source-backed P7 miss chance hook that consumes debuff id `4`.

## Source Facts

Primary sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/script/decoded/data__script__db.mid.json`
- `modules/script/decoded/data__script__bufDebuf.mid.json`

Skill rows from `db.mid` group 1:

| Skill | Row | Meaning |
| --- | --- | --- |
| `31` | `[3,148,560,60,0,45,2,4,1,0]` | Direct damage, effectMode `2`, debuff id `4`, implicit chance `-1`; stores `skill[8] = 1`. |
| `37` | `[3,154,566,100,2,30,2,4,2,0]` | Direct damage, effectMode `2`, debuff id `4`, implicit chance `-1`; stores `skill[8] = 2`. |

Debuff row:

| Debuff id | Row | Meaning |
| --- | --- | --- |
| `4` | `[315,326,3]` | No-op in `game.b.q(4)`, but stored value is consumed by P7 miss chance when the attacker has debuff4. |

Visual row from `bufDebuf.mid`:

| Bank | Id | Row | Meaning |
| --- | --- | --- | --- |
| Debuff bank `1` | `4` | `[1,1,0,0,1,11,0,-1]` | Data exists, but source visual gate does not include debuff id `4`, so active queue applies immediately without P12/P13 visual. |

Source behavior from `game.b.b(target)`:

| Step | Source behavior | Rebuild status |
| --- | --- | --- |
| Damage formula | Skills `31/37` are in the direct-simple switch: `raw * skill[3] / 100`. | PORTED/PARTIAL. |
| Debuff producer | `var8_10` stays `-1`, so normal non-status3 targets apply debuff id `4` unless buff14 blocks. | PORTED/PARTIAL. |
| Stored value | `case 4` stores `w[4][1] = aq.c[1][sourceSkill][8]`. | PORTED/PARTIAL. |
| Buff 14 block | If target `m(14)`, debuff is blocked. | PORTED/PARTIAL. |
| Status 3 anti-debuff | If target `f(3)`, source uses `chance * (100 - aq.c[3][3][5]) / 100`. With implicit chance `-1`, Java integer math yields threshold `0`; roll `1` blocks. | PORTED/PARTIAL. |
| Slot write | Applies target `w[4]`, duration from `aq.c[7][4][2]`, source skill id, active flag. | PORTED/PARTIAL. |
| Active queue | Adds active effect bank `1`, id `4`. Source visual gate skips visual for id `4`. | PORTED/PARTIAL. |

Source behavior from `game.b.q(4)`:

| Step | Source behavior | Rebuild status |
| --- | --- | --- |
| Active queue tick | `case 4` returns immediately; duration still ticks/clears through caller. | PORTED/PARTIAL. |

Source behavior from `game.d` P7 miss chance:

| Step | Source behavior | Rebuild status |
| --- | --- | --- |
| Debuff4 consume | If attacker has debuff id `4`, source subtracts `w[4][1]` from attacker speed before miss chance calculation. | PORTED/PARTIAL, smoke-covered via trace `debuff4Value=1`. |

Source behavior from `game.d` P7:

- `game.b.b(target)` applies debuff result before P7 miss/dodge HP damage.
- Therefore a successful debuff producer can enqueue debuff id `4` even when
  the later P7 hit roll misses.
- On miss, P7 hides damage/debuff text and shows dodge text.

## Implementation

Touched code:

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added Phase 9-H debuff id `4` checkpoints and `isPhase9HDebuff4Skill(...)`. |

No gameplay runtime code was changed in this slice. Existing `BattleUnit` and
`VqsvBattleRuntime` already had the source-shaped producer, no-op active queue,
and P7 miss chance consumer path.

## Smoke Matrix

Output folder:

```text
rebuild_game/build/smoke/phase9h/
```

| Checkpoint | Asserted behavior | Result |
| --- | --- | --- |
| `battle_phase9h_debuff4_success_skill_31` | Forced hit, debuff4 text visible, enemy has debuff4. | PASS |
| `battle_phase9h_debuff4_success_skill_37` | Same success path for skill `37`. | PASS |
| `battle_phase9h_debuff4_buff14_block_skill_31` | Target buff14 blocks debuff4 while damage still hits. | PASS |
| `battle_phase9h_debuff4_status3_block_skill_31` | Target status3 plus roll `1` blocks implicit debuff4. | PASS |
| `battle_phase9h_debuff4_miss_queue_skill_31` | Forced miss hides debuff text but source-applied debuff4 reaches immediate active queue apply. | PASS |
| `battle_phase9h_debuff4_miss_chance_skill_31` | Preloaded player debuff4 feeds P7 miss chance trace with `debuff4Value=1`. | PASS |

## Verification

Commands run:

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-checkpoint <phase9h checkpoint> build\smoke\phase9h\<checkpoint>.png
java -cp build\classes com.vqsv.rebuild.Main --check
java -cp build\classes VqsvBattleDamageFormulaCheck
rg -n "Ã|Â|Æ|Ð|ð|□|�|mojibake" src\main\java
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_sophie_after_battle_branch build\smoke\phase9h_regression\route_sophie_after_battle_branch.png
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_bunny_after_battle_task build\smoke\phase9h_regression\route_bunny_after_battle_task.png
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state build\smoke\phase9h_regression\route_elder_after_battle_reward_state.png
```

Result:

| Check | Result |
| --- | --- |
| Build | PASS |
| Phase 9-H smoke group | PASS |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| Java mojibake literal scan | PASS: no matches |
| Route Sophie/Bunny/Elder | PASS |

Note: PowerShell console output can show Vietnamese as mojibake because of
console encoding. The Java source mojibake scan passed.

## Current Status

| Area | Status |
| --- | --- |
| Skills `31/37` formula family | PORTED/PARTIAL, smoke-covered. |
| Debuff id `4` producer | PORTED/PARTIAL, smoke-covered. |
| Stored skill param `w[4][1]` | PORTED/PARTIAL. |
| Buff14 block | PORTED/PARTIAL, smoke-covered. |
| Status3 block for implicit chance | PORTED/PARTIAL, smoke-covered. |
| Miss interaction | PORTED/PARTIAL: P7 text suppressed, queue can still occur. |
| Debuff4 active queue tick | PORTED/PARTIAL: source no-op visual-skip apply, smoke-covered. |
| Debuff4 P7 miss chance consumer | PORTED/PARTIAL, smoke-covered by trace. |
| Exact RNG stream parity | PARTIAL. Smoke uses deterministic roll hook. |
| Pixel-perfect active queue visual | Not applicable for id `4` in current source gate; broad visual parity remains PENDING. |
| HUD buff/debuff icon sprite 325 | PENDING UI parity later; see `163_battle_status_icon_sprite325_img804_audit.md`. |

## Next Roadmap Step

Continue Phase 9 by debuff id.

Recommended next slice:

```text
Phase 9-I: debuff id 5 family, skills 32/38/61.
```

Why:

- Debuff id `5` is the next family and has gameplay impact: it lowers target
  speed by a percent-derived value.
- It is stat-changing, so it should be handled separately from debuff id `4`.
- It should include producer coverage plus a consumer/stat assertion, not only
  a visible P7 text smoke.

Do not start `SOURCE_SWITCH_GAP` skills yet.
