# 58 Battle Speffect Type 9 Port

Status: PORTED/PARTIAL for the first `ah` special-effect slices.

Scope implemented in this slice:

- `effect.mid` chunks now start from chunk `0` for P7, matching `game.d.n()`
  source shape where `J = I` at P7 effect entry.
- `VqsvBattleAnimationTables` now loads:
  - `effect.mid`
  - `speffect.mid`
  - `blood.mid`
- P7 detects `effect.mid chunk[1] == 1`.
- If the referenced `speffect.mid` row is AH type `9`, rebuild renders a
  source-shaped blinking color overlay on the runtime target/attacker side.
- If the referenced `speffect.mid` row is AH type `1`, rebuild renders a
  source-shaped scrolling texture composite using `/data/tex/tex_*`.
- P7 now advances through multiple chunks in the current `effect.mid` row before
  applying the simplified damage frame.
- P7 now follows the source no-damage branch for skill rows whose
  `aq.c[1][skill][3] == 0`; skill `45` does not apply damage text/damage after
  its effect chunks.
- Smoke path selects skill `45`, which uses:

```text
effect.mid[45] =
[0,1,19,0,-1,-1,0, 0,1,15,0,-1,-1,0]

speffect.mid[19] =
[9,120,255,255,255,0,6,2]

speffect.mid[15] =
[1,0,5,3,0,0]
```

Runtime interpretation for skill `45` chunk `0`:

```text
[9, targetX, targetY, targetSpriteId, targetAnimState, targetOrientation,
 120,255,255,255,0,6,2]

[1, targetX, targetY, targetSpriteId, targetAnimState, targetOrientation,
 0,5,3,0,0]
```

## Smoke

Generated PNG checkpoints:

- `rebuild_game/build_intro_demo/battle_elder_p7_speffect45_start.png`
- `rebuild_game/build_intro_demo/battle_elder_p7_speffect45_overlay.png`
- `rebuild_game/build_intro_demo/battle_elder_p7_speffect45_type1.png`
- `rebuild_game/build_intro_demo/battle_elder_p7_speffect45_after.png`

Regression checkpoints:

- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`

## Verification

Passed:

- `build.ps1`
- `com.vqsv.rebuild.Main --check`
- `VqsvBattleDamageFormulaCheck`
- Java source mojibake scan
- required smoke checkpoints above

## Honest Limits

Still PARTIAL:

- The type `9` pixel transform is source-shaped from `l.a(e, alpha,r,g,b)`
  followed by `l.b(e,1,50)`, but it is not yet pixel-compared against original
  MIDP frames.
- The type `1` texture/composite path is source-shaped from `ah.e()` and
  `l.a(e2,e3,mode)`, but it is not yet pixel-compared against original MIDP
  frames.
- Multi-chunk effect sequencing now advances through effect chunks, but exact
  `H.i()` start conditions and `effect.mid chunk[4]/[5]/[6]` trigger hooks
  remain PARTIAL.
- Skill `45` no-damage behavior is now source-backed for `aq.c[1][45][3] == 0`,
  but broader buff/debuff turn ticking and every other skill family remain
  outside this slice.
