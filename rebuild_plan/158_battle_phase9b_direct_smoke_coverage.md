# 158 Battle Phase 9-B Direct Smoke Coverage

Status date: 2026-07-09

Status: PHASE 9-B / SMOKE-ONLY / IMPLEMENTED.

Purpose:

- Add deterministic smoke coverage for already-ported direct-simple skill
  formula rows.
- Do not change battle formula, P7 effect timing, UI widgets, intro/world, or
  route logic.
- Keep `SOURCE_SWITCH_GAP`, debuff, heal/leech, clear-buff, and HP-scaling
  skills out of this batch because those need dedicated setup and asserts.

## Scope

This batch covers direct-simple skills with no unique post-effect/debuff logic
selected from `156_battle_phase9_skill_coverage_matrix.md`:

```text
0, 6, 10, 16, 20, 26, 30, 36, 40, 46, 50, 56, 60, 66
```

Excluded from this smoke-only batch:

| Excluded family | Reason |
| --- | --- |
| `11/17` | Direct damage plus hardcoded heal in `game.d.q()`. |
| `12/13/18/19/31/32/33/37/38/39/51/57/61` | Direct damage plus debuff/result queue. |
| `52/58` | Direct damage plus leech gate. |
| `63/69` | Direct damage plus follow-up chance. |
| `43/49` | Direct damage plus target buff clear. |
| `53/59` | HP-percent scaling formula. |
| `54/55` | Power zero plus debuff ids `8/9`; exact behavior needs audit. |
| `21/27/42/48/62/67` | `SOURCE_SWITCH_GAP`; do not smoke as if formula is proven. |

## Implementation

Added generic smoke checkpoint support:

```text
battle_phase9b_direct_skill_<skillId>
```

Current whitelist:

```text
0,6,10,16,20,26,30,36,40,46,50,56,60,66
```

Each checkpoint:

1. builds an Elder battle with the selected skill as pet skill slot 0;
2. forces P7 hit roll to `99`;
3. waits for P7 phase 2;
4. asserts:
   - damage text is visible;
   - miss text is empty;
   - trace contains `battle P7 hitroll skill=<id>`;
   - trace contains `hit=true`.

No formula values were changed in this slice.

## Smoke Results

Output folder:

```text
rebuild_game/build/smoke/phase9b/
```

| Skill | Checkpoint | Result |
| --- | --- | --- |
| `0` | `battle_phase9b_direct_skill_0` | PASS |
| `6` | `battle_phase9b_direct_skill_6` | PASS |
| `10` | `battle_phase9b_direct_skill_10` | PASS |
| `16` | `battle_phase9b_direct_skill_16` | PASS |
| `20` | `battle_phase9b_direct_skill_20` | PASS |
| `26` | `battle_phase9b_direct_skill_26` | PASS |
| `30` | `battle_phase9b_direct_skill_30` | PASS |
| `36` | `battle_phase9b_direct_skill_36` | PASS |
| `40` | `battle_phase9b_direct_skill_40` | PASS |
| `46` | `battle_phase9b_direct_skill_46` | PASS |
| `50` | `battle_phase9b_direct_skill_50` | PASS |
| `56` | `battle_phase9b_direct_skill_56` | PASS |
| `60` | `battle_phase9b_direct_skill_60` | PASS |
| `66` | `battle_phase9b_direct_skill_66` | PASS |

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

## Status Decision

| Area | Status |
| --- | --- |
| Direct-simple formula smoke coverage for selected no-extra-effect ids | PORTED/PARTIAL, smoke-covered |
| Generic Phase 9-B skill smoke harness | PORTED |
| Exact formula value parity per skill | Still covered by formula code/check, not individually asserted in PNG. |
| Skills with debuff/heal/leech/follow-up/clear/HP-scaling | PENDING dedicated smoke slices |
| `SOURCE_SWITCH_GAP` skills | PENDING bytecode/control-flow audit |

## Next Roadmap Step

Next should stay inside Phase 9 and continue coverage by behavior family:

```text
Phase 9-C: direct formula variants with deterministic setup.
```

Recommended order:

1. plus-divisor `1/7`;
2. conditional debuff formula `3/9/23/29`;
3. target buff clear `43/49`;
4. HP-percent scaling `53/59`;
5. only then post-hit siblings `17/52/69` and debuff families.

Do not start `SOURCE_SWITCH_GAP` skills until a dedicated source/bytecode audit
proves their control flow.
