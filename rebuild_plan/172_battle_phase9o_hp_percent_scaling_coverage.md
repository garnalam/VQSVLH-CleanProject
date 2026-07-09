# 172 Battle Phase 9-O HP Percent Scaling Coverage

Status date: 2026-07-09

Status: PHASE 9-O / AUDITED / IMPLEMENTED-PARTIAL / SMOKE-COVERED.

Purpose:

- Cover HP-percent scaling skills `53/59`.
- Prove formula shape from source: `raw * (aq.c[1][skill][8] - hpPercent) / 100`.
- Prove high/low attacker HP changes damage and raw non-positive damage clamps
  to `1`.

## Source Facts

Primary sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/script/decoded/data__script__db.mid.json`

Skill rows from `db.mid` group 1:

| Skill | Row | Meaning |
| --- | --- | --- |
| `53` | `[5,170,582,200,1,30,0,-1,200,0]` | HP-percent scaling, factor `200 - attackerHpPercent`. |
| `59` | `[5,176,588,250,3,15,0,-1,250,0]` | HP-percent scaling, factor `250 - attackerHpPercent`. |

Source behavior from `game.b.b(target)`:

| Step | Source behavior | Rebuild status |
| --- | --- | --- |
| HP percent | Source computes `var7_9 = this.d[1] * 100 / this.c[1]`. | PORTED/PARTIAL, smoke added. |
| Damage formula | `var4_6 = raw * (aq.c[1][this.D][8] - var7_9) / 100`. | PORTED/PARTIAL, smoke added. |
| Debuff result | `effectId` is `-1`; no debuff should be produced. | PORTED/PARTIAL. |
| Min clamp | Final damage is clamped to `1` if non-positive after formula/modifiers. | PORTED/PARTIAL, smoke added with raw <= 0 setup. |

Important nuance:

- Because skill `53` uses factor `200 - hpPercent` and skill `59` uses
  `250 - hpPercent`, normal HP range `0..100` cannot make the factor negative.
- The min-clamp smoke therefore uses source-shaped raw attack-defense <= 0,
  not `hpPercent > factor`.

## Implementation

Touched code:

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Added smoke hooks to set player attack and enemy defense. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added Phase 9-O checkpoints, helper damage setup, and `isPhase9OHpScalingSkill(...)`. |

No gameplay formula was changed in this slice. Existing `BattleUnit` already
implements `raw * (skill.chanceOrParam - hpPercent()) / 100` for `53/59`.

## Smoke Matrix

Planned output folder:

```text
rebuild_game/build/smoke/phase9o/
```

| Checkpoint | Asserted behavior | Result |
| --- | --- | --- |
| `battle_phase9o_hp_scaling_low_high_skill_53` | With same attack/defense, 25% HP damage is higher than 100% HP damage for skill53. | PASS, PNG written. |
| `battle_phase9o_hp_scaling_low_high_skill_59` | Same low/high HP relationship for skill59. | PASS, PNG written. |
| `battle_phase9o_hp_scaling_min_clamp_skill_53` | Raw <= 0 setup clamps visible damage to `1`; damage popup renders as `-1`. | PASS, PNG written. |
| `battle_phase9o_hp_scaling_miss_skill_53` | Forced miss computes result path but shows dodge and leaves enemy HP unchanged. | PASS, PNG written. |

## Current Status

| Area | Status |
| --- | --- |
| Skills `53/59` HP scaling formula | PORTED/PARTIAL, smoke-covered. |
| Low/high HP relationship | PORTED/PARTIAL, smoke-covered. |
| Min clamp | PORTED/PARTIAL, smoke-covered. |
| Miss interaction | PORTED/PARTIAL, smoke-covered. |
| Debuff result preservation | PORTED/PARTIAL: `effectId=-1`. |
| Exact RNG stream parity | PARTIAL. Smoke uses deterministic hooks. |

Smoke PNGs:

```text
rebuild_game/build/smoke/phase9o/battle_phase9o_hp_scaling_low_high_skill_53.png
rebuild_game/build/smoke/phase9o/battle_phase9o_hp_scaling_low_high_skill_59.png
rebuild_game/build/smoke/phase9o/battle_phase9o_hp_scaling_min_clamp_skill_53.png
rebuild_game/build/smoke/phase9o/battle_phase9o_hp_scaling_miss_skill_53.png
```

Regression run:

| Check | Result |
| --- | --- |
| `build.ps1` | PASS |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| Java mojibake scan | PASS, no matches |
| `route_sophie_after_battle_branch` | PASS |
| `route_bunny_after_battle_task` | PASS |
| `route_elder_after_battle_reward_state` | PASS |

## Next Roadmap Step

After Phase 9-O passes, remaining Phase 9 choices are:

```text
Phase 9-P: direct-simple smoke-only gap cleanup for unanchored low-risk ids.
```

or:

```text
SOURCE_SWITCH_GAP audit for 21/27/42/48/62/67.
```

Recommended next slice is Phase 9-P first if the goal is coverage closure
before bytecode-control-flow work.
