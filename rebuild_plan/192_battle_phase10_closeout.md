# 192 Battle Phase 10 Closeout

Status: PHASE 10 CLOSED / PARTIAL.

Scope:

- Phase 10-A status HUD icon regression.
- Phase 10-B normal P7 body-attached AH visual regression.
- Phase 10-C regression suite matrix and Quick Gate runner.

No client/JAR was opened. Verification used headless smoke PNG only.

## Code Changes

| File | Change |
|---|---|
| `rebuild_game/src/main/java/VqsvIntroDemo.java` | Added CLI entry `--smoke-suite <suite> <outDir>`. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added `battle_quick` suite with Tier 0 checkpoints from doc 191. |
| `rebuild_plan/191_battle_phase10c_regression_suite_matrix.md` | Updated status after runner implementation and suite pass. |
| `rebuild_plan/battle_engine_master_roadmap_progress.md` | Phase 10 row points at doc 191 and Quick Gate runner work. |

## Preflight Commands

```text
powershell -ExecutionPolicy Bypass -File ./build.ps1
java -cp build/classes com.vqsv.rebuild.Main --check
java -cp build/classes VqsvBattleDamageFormulaCheck
powershell -NoProfile -Command "$pattern = [string]::Join('|', @([char]0x00C3,[char]0x00C2,[char]0x00C6,[char]0x00D0,[char]0x00F0,[char]0x25A1,[char]0xFFFD,'mojibake')); rg -n $pattern src/main/java"
```

Result:

| Check | Result |
|---|---|
| Build | PASS |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| Java source mojibake scan | PASS, no matches |

Note: some smoke stdout battle-log text is still printed through the current
console encoding and may look mojibake in terminal output. This closeout only
asserts Java source text scan and PNG/headless behavior.

## Quick Gate Command

```text
java -cp build/classes VqsvIntroDemo --smoke-suite battle_quick build/smoke/suites/battle_quick
```

Result:

```text
smoke-suite-ok battle_quick checkpoints=14 outDir=build/smoke/suites/battle_quick
```

## Quick Gate Checkpoints

| # | Checkpoint | Output PNG | Result |
|---:|---|---|---|
| 1 | `route_sophie_after_battle_branch` | `build/smoke/suites/battle_quick/route_sophie_after_battle_branch.png` | PASS |
| 2 | `route_bunny_after_battle_task` | `build/smoke/suites/battle_quick/route_bunny_after_battle_task.png` | PASS |
| 3 | `route_elder_after_battle_reward_state` | `build/smoke/suites/battle_quick/route_elder_after_battle_reward_state.png` | PASS |
| 4 | `battle_elder_command_ui` | `build/smoke/suites/battle_quick/battle_elder_command_ui.png` | PASS |
| 5 | `battle_elder_p3_skill_list` | `build/smoke/suites/battle_quick/battle_elder_p3_skill_list.png` | PASS |
| 6 | `battle_elder_p6_target_select` | `build/smoke/suites/battle_quick/battle_elder_p6_target_select.png` | PASS |
| 7 | `battle_elder_p7_damage_frame` | `build/smoke/suites/battle_quick/battle_elder_p7_damage_frame.png` | PASS |
| 8 | `battle_p7_hit_forced_direct_skill10` | `build/smoke/suites/battle_quick/battle_p7_hit_forced_direct_skill10.png` | PASS |
| 9 | `battle_p7_miss_forced_skill10` | `build/smoke/suites/battle_quick/battle_p7_miss_forced_skill10.png` | PASS |
| 10 | `battle_p7_crit_forced_skill10` | `build/smoke/suites/battle_quick/battle_p7_crit_forced_skill10.png` | PASS |
| 11 | `battle_phase10a_status_icons_mixed_order` | `build/smoke/suites/battle_quick/battle_phase10a_status_icons_mixed_order.png` | PASS |
| 12 | `battle_phase10b_p7_type7_skill34_overlay` | `build/smoke/suites/battle_quick/battle_phase10b_p7_type7_skill34_overlay.png` | PASS |
| 13 | `battle_phase10b_p7_type8_skill12_overlay` | `build/smoke/suites/battle_quick/battle_phase10b_p7_type8_skill12_overlay.png` | PASS |
| 14 | `battle_phase10b_p7_type12_skill55_overlay` | `build/smoke/suites/battle_quick/battle_phase10b_p7_type12_skill55_overlay.png` | PASS |

## Phase 10 Status

| Area | Status | Evidence |
|---|---|---|
| Phase 10-A status HUD icons | CLOSED / PARTIAL | `battle_phase10a_status_icons_mixed_order` in Quick Gate. |
| Phase 10-B normal P7 AH renderer coverage | CLOSED / PARTIAL | `battle_phase10b_p7_type7/type8/type12` plus older type1/type9 smokes. |
| Phase 10-C matrix | DONE | `191_battle_phase10c_regression_suite_matrix.md`. |
| Quick Gate suite runner | DONE | `--smoke-suite battle_quick`. |
| Quick Gate run | PASS | 14/14 checkpoints pass. |

## Remaining Gaps

PENDING / NOT CLAIMED:

- Full exhaustive suite runner for every tier in doc 191.
- Pixel-perfect MIDP comparison.
- Generic `ah.java` interpreter.
- Exact Java ME `l.a(...)`, `l.b(...)`, `drawRGB(...)` color/alpha parity.
- Dedicated PNG for every `speffect` row and every attacker/target side variant.
- Full UI widget-runtime parity for every battle UI remains broader roadmap debt,
  especially where earlier docs mark PORTED/PARTIAL rather than PORTED.

These are not blockers for closing Phase 10 as a regression-suite foundation.

## Next Roadmap Step

Use `battle_quick` as the required baseline after every battle code change:

```text
java -cp build/classes VqsvIntroDemo --smoke-suite battle_quick build/smoke/suites/battle_quick
```

Next work should return to the main battle roadmap instead of expanding Phase 10
unless a specific regression or visual mismatch is reported.
