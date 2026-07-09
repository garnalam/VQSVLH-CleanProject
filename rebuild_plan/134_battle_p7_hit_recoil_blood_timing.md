# 134 Battle P7 Hit / Recoil / Blood Timing

## Scope

Focused visual parity slice for battle P7 direct damage:

- source damage application timing;
- target hit/recoil state timing;
- `blood.mid` floating damage/debuff text timing and placement.

This is not a pixel-perfect claim. Original-vs-rebuild frame compare is still pending.

## Source Chain

Source files/resources:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `/data/script/effect.mid`
- `/data/script/blood.mid`
- `/data/tex/blood_0..2`

`game.d.b()` case `7` enters P7:

- prepares attacker and target UI;
- calls `n()` to load current `effect.mid` row into `O`;
- runs actor `u` animation and optional `H` special effect;
- only after the source flags `z/A/B` allow it, applies damage.

Damage source path:

```text
((b)h.p).k(Z[0])
if crit: a("-" + Z[0], byte 0, image 1, target.r(), target.l(), target.m(), 15, 19)
else:    a("-" + Z[0], byte 0, image 0, target.r(), target.l(), target.m(), 9, 12)
if debuff: a(debuffName, byte 1, image 0, target.r(), target.l(), target.m(), 9, 12)
S.k = 0
S.a(target) / S.b(target)
V() advances floating text rows and removes them when the matching `blood.mid` row ends
```

`game.d.c(Graphics)` placement:

| Floating type | Target side | Source formula | Status |
| --- | --- | --- | --- |
| Damage text type `0` | player side `r == 0` | `as + blood[0][x] + 30`, `at + blood[0][y] - 30` | PORTED/PARTIAL |
| Damage text type `0` | enemy side `r != 0` | `as - blood[0][x] - 30`, `at + blood[0][y] - 30` | PORTED/PARTIAL |
| Debuff text type `1` | player side `r == 0` | `as - 10`, `at + blood[1][y] - 30` | PORTED/PARTIAL |
| Debuff text type `1` | enemy side `r != 0` | `as + 10`, `at + blood[1][y] - 30` | PORTED/PARTIAL |

Decoded `blood.mid` rows used by this slice:

- row `0`: damage text path, 11 frames.
- row `1`: debuff/miss text path, 6 frames.

## Rebuild Fix

`VqsvBattleRenderer.drawP7Damage()` now:

- loads `blood.mid` via `VqsvBattleAnimationTables.instance().bloodRow(...)`;
- anchors text at `sourceBattleActorX/Y(targetSide)`, matching the source `target.l()/target.m()` queue point rather than moving with rebuild recoil offsets;
- uses the source side formula:
  - player target: `baseX + dx + 30`;
  - enemy target: `baseX - dx - 30`;
- draws debuff text with row `1` and hides it after row `1` frame count expires.

## Current Status

| Area | Status | Note |
| --- | --- | --- |
| P7 damage HP mutation | PORTED | Existing damage formula/state path unchanged. |
| P7 damage apply timing | PORTED/PARTIAL | Applies after effect sequence, matching current supported source chunks. Multi-chunk/special edge cases remain partial. |
| Target base hit/dead state | PORTED/PARTIAL | Rebuild sets state `2` or `3`; audit 135 proved `S.a/S.b` is HP tween and removed non-source synthetic actor offsets. |
| Blood row data | PORTED | Uses decoded `/data/script/blood.mid`. |
| Damage text placement formula | PORTED/PARTIAL | Source formula and source actor anchor ported; MIDP `blood_0..2` texture rendering still partial. |
| Debuff text timing | PORTED/PARTIAL | Row `1` frame lifetime is respected visually; full floating-text queue runtime is not fully generalized. |
| Exact recoil timing | PORTED/PARTIAL | Synthetic lunge/recoil arrays removed in audit 135; remaining timing depends on sprite state/cursor parity. |
| Pixel-perfect P7 | PENDING | Requires original-vs-rebuild frame capture compare. |

## Smoke Plan

Focused PNG checkpoints:

- `battle_elder_p7_anim_start`
- `battle_elder_p7_lunge_peak`
- `battle_elder_p7_actor_u21_trigger_hit`
- `battle_elder_p7_damage_frame`
- `battle_elder_p7_recoil_peak`
- `battle_elder_p7_actor_u21_recover`

Regression PNG/routes:

- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`

Required checks after slice:

- `build.ps1`
- `com.vqsv.rebuild.Main --check`
- `VqsvBattleDamageFormulaCheck`
- Java mojibake scan
- `git diff --check`

## Next Recommended Slice

Audit/port default-package `d.d()/d.e()` sprite frame cursor timing against rebuild `SpriteAnim` to tighten state `1`/`2` frame parity.
