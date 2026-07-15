# 352 - Battle Actor Anchor And P17 Capture Anchor Fix

Date: 2026-07-14

Status: FIXED / SOURCE-BACKED / PNG-VERIFIED / NO PIXEL-PERFECT CLAIM.

## Scope

User issue after S60 resource merge:

- battle pets must stand centered on their ground marker
- P17 catch ball must fly into the enemy pet/body, not outside the screen
- P17 catch ball should land on top of the enemy pet/head area for every species
- failed catch must open/escape from that enemy head/top anchor
- successful catch must keep the ball at that enemy head/top anchor and must not
  redraw the caught enemy

This slice does not change catch chance, P17 state timing, or sprite 269 frame
matrix. It only fixes visual anchoring.

## Sources / Prior Audits Used

- `rebuild_plan/86_battle_p17_pixel_and_rng_parity_audit.md`
- `rebuild_plan/130_battle_p17_sprite269_timing_checkpoint.md`
- `rebuild_plan/351_s60_pet_sprite_composition_fix.md`
- `modules/script/decoded/data__script__pos.mid.json`
- runtime PNG audits under `build/visual_audit/`

Important source position row from `pos.mid` row 0:

```text
enemy actor: 177,103
enemy marker: 144,85
player actor: 70,223
player marker: 36,206
```

Classification:

- battle position table: PORTED
- pet sprite source cell composition after 351: PORTED
- broad actor placement patch: NOT NEEDED for audited species
- exact original-vs-rebuild pixel parity: PENDING

## Actor Ground Audit

Audit tool:

```text
build/visual_audit_tools/BattleAnchorAudit.java
```

Outputs:

```text
build/visual_audit/battle_anchor_before/battle_actor_anchor_sheet.png
build/visual_audit/battle_anchor_before/battle_actor_anchor_metrics.csv
```

Measured species visuals:

| visual | side | centerDelta | bottomDelta |
| ---: | --- | ---: | ---: |
| 17 | enemy | 0 | -12 |
| 17 | player | 1 | -13 |
| 34 | enemy | 0 | -12 |
| 34 | player | 1 | -13 |
| 68 | enemy | 0 | -12 |
| 68 | player | 1 | -13 |
| 92 | enemy | 0 | -12 |
| 92 | player | 1 | -13 |

Conclusion:

- horizontal placement is already centered after the S60 pet sprite composition
  fix
- the visual feet sit slightly above the marker center, which matches the marker
  being a floor shadow/platform rather than a literal bottom line
- no global pet actor offset patch was made, because it would risk breaking
  species that are already centered

Status: VERIFIED/PARTIAL. Verified for the audited representative species and
current Elder/Bunny Battle Lab routes. Wider species coverage is still useful
when new species-specific placement bugs appear.

## P17 Capture Ball Bug

Old rebuild behavior:

```text
For q0/q1/q2/q4, draw sprite 269 aligned against the enemy visible sprite rect.
For q3 success, bias by marker but still based on cell bounds.
```

Why this failed after S60 merge:

- source sprite 269 cells include transparent padding
- centering by the full cell/target rect can place the visible ball core at the
  screen edge or platform edge
- source P17 creates the ball at the enemy actor path, but rebuild needs to
  account for source cell padding when drawing the decoded sprite bitmap

Fixed behavior in `rebuild_game/src/main/java/VqsvBattleRenderer.java`:

```text
q0 throw:
  interpolate visible ball center from player pet body to enemy head/top point

q1 capture / q2 rumble / q4 fail escape:
  anchor visible ball center to enemy head/top point

q3 success flash/drop:
  anchor visible ball center to enemy head/top point
```

The renderer now renders the current sprite 269 cell to a temporary image,
computes opaque-pixel bounds, and centers the visible ball core rather than the
whole transparent cell. The enemy head/top point is computed from the current
enemy sprite bounds, so the rule is species-generic and is not hard-coded to
Bunny.

Classification:

- sprite 269 timing/state matrix: PORTED by audit 130
- P17 ball visible-core anchor: PORTED/PARTIAL
- q0 throw path: PORTED/PARTIAL, source-shaped visual interpolation
- q1/q2/q4 enemy head/top anchor: PORTED/PARTIAL
- q3 success enemy head/top anchor: PORTED/PARTIAL
- exact original P17 pixel compare: PENDING

## New Checkpoint

Added checkpoint:

```text
battle_p17_throw_hits_enemy_anchor
```

Added Battle Lab scenario:

```text
Lane catch, scenario throw_hits_enemy
```

Included in catch suites:

```text
core
capture_visual
all
```

Checkpoint assertion:

- enter Bunny catch P17
- stop during q0 throw at cursor >= 7 before enemy hide
- assert rendered pixels near the enemy body/head area

PNG proof:

```text
rebuild_game/build_intro_demo/battle_lab_suites/catch/all/throw_hits_enemy.png
rebuild_game/build_intro_demo/battle_lab_suites/catch/all/first_fail_escape.png
rebuild_game/build_intro_demo/battle_lab_suites/catch/all/success_flash_mid.png
rebuild_game/build_intro_demo/battle_lab_suites/catch/all/q1_capture_shrink.png
```

Visual result:

- throw frame: ball lands on top of Bunny, not off-screen
- fail escape: ball opens from the enemy top/head anchor and enemy returns there
- success q3: ball remains at the enemy top/head anchor and the captured enemy
  is not redrawn outside the ball
- q1 shrink remains coherent with type8 capture effect

## Verification

Commands run from `rebuild_game`:

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp .\build\classes com.vqsv.rebuild.Main --check
java -cp .\build\classes VqsvBattleDamageFormulaCheck
mojibake scan Java source
.\run_battle_lab_suite_smoke.cmd -Lane catch -Suite all -NoBuild
.\run_battle_lab_suite_smoke.cmd -Lane npc -Suite all -NoBuild
```

Results:

```text
build.ps1 PASS
Main --check PASS
VqsvBattleDamageFormulaCheck PASS
mojibake scan PASS
Battle Lab catch all PASS 31/31
Battle Lab NPC all PASS 33/33
```

Also previously verified in this slice:

```text
panel_wheel PASS 8/8
battle_quick PASS 227/227
```

## Remaining Debt

- P17 exact original-vs-rebuild pixel parity is still PENDING.
- Exact source drawRGB/JavaME bitmap blending for AH type8 is still not claimed
  pixel-perfect.
- Actor placement is verified for representative species and active lab routes,
  not every species in the game.
- No broad actor offset table was added. Add one only if a species/route-specific
  PNG proves a real source mismatch.

## Next

Recommended next step:

```text
Continue battle skill/effect roadmap, but keep this anchor checkpoint in fixed
regression whenever P17, sprite 269, battle actor placement, or S60 sprite loading
is touched again.
```
