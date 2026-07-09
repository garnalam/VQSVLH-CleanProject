# 159 Battle Phase 9-C Formula Variant Smoke Coverage

Status date: 2026-07-09

Status: PHASE 9-C / DETERMINISTIC SMOKE / IMPLEMENTED.

Purpose:

- Cover direct formula variants that need deterministic setup.
- Do not alter formula/runtime behavior unless a mismatch appears.
- Keep `SOURCE_SWITCH_GAP` and broad debuff lifecycle work out of this slice.

## Source Families Covered

Primary source family mapping from `game.b.b(target)`:

| Family | Skills | Source behavior | Phase 9-C setup |
| --- | --- | --- | --- |
| `DIRECT_PLUS_RAW_DIVISOR` | `1,7` | Damage = direct percent + raw attack / `skill[8]`; debuff `0`. | Forced hit; assert debuff0 text/slot. |
| `CONDITIONAL_IF_TARGET_DEBUFF0` | `3,9` | Alternate damage percent when target has debuff `0`. | Preload enemy debuff0. |
| `CONDITIONAL_IF_TARGET_DEBUFF1` | `23,29` | Alternate damage percent when target has debuff `1`. | Preload enemy debuff1. |
| `DIRECT_AND_CLEAR_TARGET_BUFFS` | `43,49` | Direct damage then `target.D()` clears target buffs. | Preload enemy buff2; assert cleared. |
| `HP_PERCENT_SCALING` | `53,59` | Damage = raw * (`skill[8]` - attacker HP%) / 100. | Set attacker HP to roughly 25 percent. |

## Implementation

Added source-only smoke helpers in `VqsvBattleRuntime`:

| Helper | Purpose |
| --- | --- |
| `debugSetPlayerHpForSmoke(...)` | Deterministic HP-percent setup for skills `53/59`. |
| `debugEnemyDebuffForFormulaSmoke(...)` | Preload enemy debuff slots for conditional formula skills. |
| `debugEnemyHasDebuffForSmoke(...)` | Assert conditional setup/result state. |
| `debugEnemyBuffForFormulaSmoke(...)` | Preload enemy buff slot before clear-buff skills. |
| `debugEnemyHasBuffForSmoke(...)` | Assert `target.clearBuffs()` effect. |

Added smoke checkpoint groups:

```text
battle_phase9c_plus_divisor_skill_<id>
battle_phase9c_cond_debuff0_skill_<id>
battle_phase9c_cond_debuff1_skill_<id>
battle_phase9c_clear_buff_skill_<id>
battle_phase9c_hp_scaling_skill_<id>
```

All Phase 9-C checkpoints force P7 hit roll to `99` so the formula branch is
not masked by miss/dodge.

## Smoke Results

Output folder:

```text
rebuild_game/build/smoke/phase9c/
```

| Skill | Checkpoint | Asserted behavior | Result |
| --- | --- | --- | --- |
| `1` | `battle_phase9c_plus_divisor_skill_1` | Hit, damage text, debuff0 text/slot. | PASS |
| `7` | `battle_phase9c_plus_divisor_skill_7` | Hit, damage text, debuff0 text/slot. | PASS |
| `3` | `battle_phase9c_cond_debuff0_skill_3` | Preloaded debuff0 remains, hit path. | PASS |
| `9` | `battle_phase9c_cond_debuff0_skill_9` | Preloaded debuff0 remains, hit path. | PASS |
| `23` | `battle_phase9c_cond_debuff1_skill_23` | Preloaded debuff1 remains, hit path. | PASS |
| `29` | `battle_phase9c_cond_debuff1_skill_29` | Preloaded debuff1 remains, hit path. | PASS |
| `43` | `battle_phase9c_clear_buff_skill_43` | Preloaded enemy buff2 is cleared, hit path. | PASS |
| `49` | `battle_phase9c_clear_buff_skill_49` | Preloaded enemy buff2 is cleared, hit path. | PASS |
| `53` | `battle_phase9c_hp_scaling_skill_53` | Attacker low HP setup, hit path. | PASS |
| `59` | `battle_phase9c_hp_scaling_skill_59` | Attacker low HP setup, hit path. | PASS |

Route regression:

| Checkpoint | Result |
| --- | --- |
| `route_sophie_after_battle_branch` | PASS |
| `route_bunny_after_battle_task` | PASS |
| `route_elder_after_battle_reward_state` | PASS |

Verification:

| Check | Result |
| --- | --- |
| `build.ps1` | PASS |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| Java mojibake literal scan | PASS: no matches |

## Current Status

| Area | Status |
| --- | --- |
| Plus-divisor `1/7` | PORTED/PARTIAL, smoke-covered |
| Conditional debuff0 `3/9` | PORTED/PARTIAL, smoke-covered |
| Conditional debuff1 `23/29` | PORTED/PARTIAL, smoke-covered |
| Clear target buffs `43/49` | PORTED/PARTIAL, smoke-covered |
| HP-percent scaling `53/59` | PORTED/PARTIAL, smoke-covered |
| Exact numeric damage parity per skill | Covered by formula code/check shape, not pixel/numeric-per-skill asserted in PNG. |
| Full debuff lifecycle for all ids | PENDING dedicated debuff family slices. |
| `SOURCE_SWITCH_GAP` | PENDING source/bytecode audit. |

## Next Roadmap Step

Next Phase 9 slice should cover result/lifecycle families that are already
partly implemented but need dedicated smoke:

```text
Phase 9-D: post-hit siblings and follow-up/miss interactions.
```

Recommended order:

1. hardcoded heal sibling `17` after existing `11`;
2. leech sibling `52` after existing `58`;
3. follow-up sibling `69` after existing `63`;
4. miss interaction for `11/17/52/58/63/69` where relevant.

After that, move to debuff-family coverage by debuff id:

```text
2/8/22/28 -> debuff1
12/18 -> debuff2
13/19 -> debuff3
31/37 -> debuff4
32/38/61 -> debuff5
33/39 -> debuff6
51/57 -> debuff7
41/47 -> debuff10
```

Do not start `SOURCE_SWITCH_GAP` until a dedicated audit proves the damaged
switch/control-flow.
