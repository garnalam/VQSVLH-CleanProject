# 183 Battle Phase 9-Z Cleanse Protection Producer Smoke

Status date: 2026-07-09

Status: PHASE 9-Z / IMPLEMENTED-PARTIAL / SMOKE-COVERED.

Purpose:

- Smoke producer behavior for no-damage/default `effectMode == 1` skills `24` and `25`.
- Prove skill `24` heals and clears debuffs through buff13.
- Prove skill `25` clears debuffs and applies buff14 protection.
- Prove buff14 blocks a later debuff attempt.

## Source Facts

Sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/script/decoded/data__script__db.mid.json`
- `rebuild_plan/181_battle_phase9x_no_damage_effectmode1_audit.md`

Rows:

```text
skill24 = [2,141,553,0,1,10,1,13,-1,1]
skill25 = [2,142,554,0,1,10,1,14,-1,1]
buff13  = [346,361,3,20,-1]
buff14  = [347,362,3,-1,-1]
```

Source route:

```text
skill[3] == 0
skill[6] == 1
skill[9] == 1
game.d.q() default -> ((b)h.p).a((byte)skill[7], -1, skillId)
```

Buff behavior:

| Buff | Source behavior |
| ---: | --- |
| `13` | Heal by max HP percent and call `C()` to clear debuffs. |
| `14` | Call `C()` to clear debuffs; source debuff apply path checks `target.m(14)` and blocks new debuff. |

## Implementation

Touched code:

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Added smoke-only `debugPlayerHasDebuffForSmoke()` and `debugEnemyTryDebuffPlayerForSmoke()` to verify buff14 blocks later debuff application through `BattleUnit.computeDamage()`. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added checkpoint prefix `battle_phase9z_cleanse_protect_skill_`. |
| `rebuild_plan/156_battle_phase9_skill_coverage_matrix.md` | Updated skills `24/25`. |

Checkpoint setup:

- Enter elder battle P7 with skill `24` or `25`.
- Lower player HP to half.
- Seed player debuff `5`.
- Advance to P7 phase 3.

Checkpoint asserts:

- P7 no-damage route is used.
- No `battle P7 damage frame` trace for skill `24/25`.
- No P7 hitroll trace for skill `24/25`.
- Target vector is same-side: `targetSide=1`, `targetSlot=1`.
- Player debuff `5` is cleared.
- Skill `24` applies buff13 and heals player.
- Skill `25` applies buff14.
- After skill `25`, a forced enemy debuff attempt with skill `2` returns `appliedDebuff=-1` and player does not receive debuff `1`.

## Smoke

Outputs:

```text
rebuild_game/build/smoke/phase9z/battle_phase9z_cleanse_protect_skill_24.png
rebuild_game/build/smoke/phase9z/battle_phase9z_cleanse_protect_skill_25.png
```

| Checkpoint | Result |
| --- | --- |
| `battle_phase9z_cleanse_protect_skill_24` | PASS |
| `battle_phase9z_cleanse_protect_skill_25` | PASS |

## Regression

| Check | Result |
| --- | --- |
| `build.ps1` | PASS |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| Java mojibake scan | PASS, no matches |
| `route_sophie_after_battle_branch` | PASS |
| `route_bunny_after_battle_task` | PASS |
| `route_elder_after_battle_reward_state` | PASS |

Regression PNG folder:

```text
rebuild_game/build/smoke/phase9z_regression/
```

## Status Decision

| Skill | Status | Notes |
| ---: | --- | --- |
| `24` | PORTED/PARTIAL, smoke-covered | Producer buff13 no-damage route, heal, and debuff clear covered. Tick heal still covered by active queue lifecycle, not re-smoked here. |
| `25` | PORTED/PARTIAL, smoke-covered | Producer buff14 no-damage route, debuff clear, and later debuff block covered. |

Visual status:

- Skill `24` uses actor action plus AH type `1`; source-shaped/PARTIAL.
- Skill `25` includes speffect4 AH type `7`, which remains visual PENDING for normal P7 special renderer.
- No pixel-perfect visual parity is claimed here.

## Next Roadmap Step

Recommended:

```text
Phase 9-AA: defensive hook producer smoke for skills 34/35.
```

Scope:

- Skill `34` -> buff5 stored-damage hook.
- Skill `35` -> buff6 source-odd formula hook.
- Keep deterministic RNG and separate producer smoke from visual AH type7 parity.
