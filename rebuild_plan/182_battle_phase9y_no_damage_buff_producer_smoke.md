# 182 Battle Phase 9-Y No-Damage Buff Producer Smoke

Status date: 2026-07-09

Status: PHASE 9-Y / IMPLEMENTED-PARTIAL / SMOKE-COVERED.

Purpose:

- Smoke producer behavior for easy no-damage/default `effectMode == 1` skills:
  `4/5/14/44`.
- Prove each skill applies its same-side buff through P7 no-damage q() route.
- Prove no fake damage/hitroll path is created.

## Source Facts

Sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/script/decoded/data__script__db.mid.json`
- `rebuild_plan/181_battle_phase9x_no_damage_effectmode1_audit.md`

Rows:

```text
skill4  = [0,121,533,0,1,10,1,0,-1,1]
skill5  = [0,122,534,0,1,10,1,1,-1,1]
skill14 = [1,131,543,0,1,10,1,2,-1,1]
skill44 = [4,161,573,0,1,10,1,8,-1,1]
```

Common source route:

```text
skill[3] == 0
skill[6] == 1
skill[9] == 1
game.d.q() default -> ((b)h.p).a((byte)skill[7], -1, skillId)
```

## Implementation

Touched code:

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Added smoke-only stat accessors for player base/current stats. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added checkpoint prefix `battle_phase9y_no_damage_buff_skill_`. |
| `rebuild_plan/156_battle_phase9_skill_coverage_matrix.md` | Updated skills `4/5/14/44`. |

Checkpoint asserts:

- P7 reaches post-effect/no-damage route.
- `battle P7 no-damage skill=<id>` trace exists.
- No `battle P7 damage frame skill=<id>` trace exists.
- No `battle P7 hitroll skill=<id>` trace exists.
- Target vector is same-side: `targetSide=1`, `targetSlot=1`.
- Player receives the expected buff:
  - skill `4` -> buff `0`
  - skill `5` -> buff `1`
  - skill `14` -> buff `2`
  - skill `44` -> buff `8`
- Stat hook smoke:
  - skill `4`: player defense increases.
  - skill `5`: player defense decreases.
  - skill `14`: player defense increases.
  - skill `44`: buff8 producer value is active; deeper PP/damage behavior is left for a later hook-specific smoke.

## Smoke

Outputs:

```text
rebuild_game/build/smoke/phase9y/battle_phase9y_no_damage_buff_skill_4.png
rebuild_game/build/smoke/phase9y/battle_phase9y_no_damage_buff_skill_5.png
rebuild_game/build/smoke/phase9y/battle_phase9y_no_damage_buff_skill_14.png
rebuild_game/build/smoke/phase9y/battle_phase9y_no_damage_buff_skill_44.png
```

| Checkpoint | Result |
| --- | --- |
| `battle_phase9y_no_damage_buff_skill_4` | PASS |
| `battle_phase9y_no_damage_buff_skill_5` | PASS |
| `battle_phase9y_no_damage_buff_skill_14` | PASS |
| `battle_phase9y_no_damage_buff_skill_44` | PASS |

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
rebuild_game/build/smoke/phase9y_regression/
```

## Status Decision

| Skill | Status | Notes |
| ---: | --- | --- |
| `4` | PORTED/PARTIAL, smoke-covered | Producer buff0 no-damage route and defense-up hook covered. Later formula hook at duration counter `0` remains broader coverage. |
| `5` | PORTED/PARTIAL, smoke-covered | Producer buff1 no-damage route, defense-down, active damage-boost input covered. |
| `14` | PORTED/PARTIAL, smoke-covered | Producer buff2 no-damage route and defense-up covered. |
| `44` | PORTED/PARTIAL, smoke-covered | Producer buff8 no-damage route and active buff value covered. PP/damage hook behavior remains PARTIAL. |

Visual status:

- Skills `4/5/44` use AH type `9` + type `1`, currently PORTED/PARTIAL.
- Skill `14` uses actor action `u21`, currently source-shaped/PORTED-PARTIAL.
- No pixel-perfect visual parity is claimed here.

## Next Roadmap Step

Recommended:

```text
Phase 9-Z: cleanse/protection producer smoke for skills 24/25.
```

Scope:

- Seed player debuff.
- Use skill `24` and assert heal/clearDebuffs.
- Use skill `25` and assert clearDebuffs + buff14 active.
- Then assert buff14 blocks a subsequent target debuff path.
