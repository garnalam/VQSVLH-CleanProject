# 282 - Battle Debuff0 Gieo Hat Closeout

Scope: debuff table `aq.c[7][0]`, skills `1` and `7`, active queue
P12/P13 tick, body visual, expiry, and buff14 block regression.

## Source Facts

```text
debuff0 = [311,322,3]
skill1  = [0,118,530,50,0,45,2,0,4,0]
skill7  = [0,124,536,75,2,30,2,0,3,0]
effect.mid[1] = [0,0,20,0,-1,-1,0]
effect.mid[7] = [0,0,20,0,-1,-1,0]
bufDebuf aq[0] = [1,18,0,-1]
```

Source behavior:

- Producer path stores `w[0][1] = preSkillRawDamage`.
- Producer path stores source skill in `w[0][3]`.
- Duration starts at `3`.
- Tick damage is `max(1, w[0][1] / aq.c[1][sourceSkill][8])`.
- Skill `1` uses divisor `4`; skill `7` uses divisor `3`.
- HUD icon is debuff id + 1, so debuff0 icon cell is `1`.
- Duration cell is `134 + duration`, so `137 -> 136 -> 135 -> clear`.
- Body visual is source active queue row `aq[0]`, speffect `18`, AH type `9`.
- Buff14 blocks incoming debuff application at the target debuff planning gate.

## Implemented / Tightened

Added focused smoke checkpoints:

```text
battle_status_debuff0_before_no_effect
battle_status_debuff0_skill1_producer_apply
battle_status_debuff0_skill7_producer_apply
battle_status_debuff0_tick_skill1_div4
battle_status_debuff0_tick_skill7_div3
battle_status_debuff0_body_visual_speffect18_anchor
battle_status_debuff0_expiry_clears_icon
battle_status_debuff0_buff14_blocks_skill1
```

Also added these to fixed `battle_quick` regression.

Runtime changes are smoke/debug support only:

- `debugEnemyDebuffValueForSmoke`
- `debugEnemyDebuffSourceSkillForSmoke`
- `debugEnemyDebuffDurationForSmoke`
- `debugTickEnemySourceDebuffForSmoke`

No gameplay path was rewritten for debuff0 in this closeout. Existing runtime
already used `BattleUnit.tickSourceDebuff()` for active queue P12/P13.

## Measured Smoke Results

Focused PNG smoke:

```text
build_intro_demo/debuff0_closeout/battle_status_debuff0_before_no_effect.png
build_intro_demo/debuff0_closeout/battle_status_debuff0_skill1_producer_apply.png
build_intro_demo/debuff0_closeout/battle_status_debuff0_skill7_producer_apply.png
build_intro_demo/debuff0_closeout/battle_status_debuff0_tick_skill1_div4.png
build_intro_demo/debuff0_closeout/battle_status_debuff0_tick_skill7_div3.png
build_intro_demo/debuff0_closeout/battle_status_debuff0_body_visual_speffect18_anchor.png
build_intro_demo/debuff0_closeout/battle_status_debuff0_expiry_clears_icon.png
build_intro_demo/debuff0_closeout/battle_status_debuff0_buff14_blocks_skill1.png
```

Numeric before / during / after:

| Slice | Before | During | After |
| --- | --- | --- | --- |
| no effect | enemy icon count `0`, no debuff0 | no active queue | unchanged |
| skill1 producer | no debuff0 | P7 uses `effect.mid[1]`, actor anim id `20`, hit commits `appliedDebuffId=0` | enemy icon `1/137`, source skill `1`, stored raw `>0` |
| skill7 producer | no debuff0 | P7 uses `effect.mid[7]`, actor anim id `20`, hit commits `appliedDebuffId=0` | enemy icon `1/137`, source skill `7`, stored raw `>0` |
| skill1 tick | HP `50`, stored raw `40`, duration `3` | `40 / 4 = 10` damage, text `-10` | HP `40`, icon `1/136` |
| skill7 tick | HP `60`, stored raw `45`, duration `3` | `45 / 3 = 15` damage, text `-15` | HP `45`, icon `1/136` |
| body visual | enemy has debuff0 icon `1/137` | P12 active queue visual `aq[0]`, speffect `18`, AH type `9` | visual anchored on source battle actor position |
| expiry | HP `80`, stored raw `48`, duration `3` | ticks are `-12`, `-12`, `-12` | HP `44`, duration `0`, icon cleared |
| buff14 block | enemy has buff14 icon `26/137` | skill1 still hits for damage, but `appliedDebuffId=-1` | no debuff0 icon, buff14 remains |

## Verification

```text
build.ps1 PASS
com.vqsv.rebuild.Main --check PASS
VqsvBattleDamageFormulaCheck PASS
focused debuff0 closeout PNG smoke 8/8 PASS
battle_quick PASS 142/142
git diff --check PASS
diff mojibake scan PASS (no new mojibake in Java diff)
```

## Classification

```text
Debuff0 core logic: PORTED
Producer skills 1/7: PORTED
P12/P13 body visual speffect18: PORTED
Expiry/icon clear: PORTED
Buff14 block regression: PORTED
Pixel-perfect: NOT CLAIMED
```

Pixel-perfect is not claimed because there is no original-vs-rebuild frame
compare for speffect18. This closeout proves source row, effect id, timing hook,
icon/duration state, numeric tick formula, and no off-platform actor regression.

## Next Step

Debuff1 `Me Muoi` is now closed in
`283_battle_debuff1_me_muoi_closeout.md`. Move to debuff2 dedicated closeout in
table order.
