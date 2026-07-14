# 286 - Battle Debuff3 Thuc Loai Closeout

Scope: dedicated closeout for target-side debuff id `3` / `Thuc Loai`.

## Plain Gameplay Explanation

`Thuc Loai` is a delayed-damage status.

It is applied by skills `13` and `19`. The applying hit deals normal damage
immediately, then the status sits on the target. It does not hurt on the first
two active ticks. On the final tick, when duration is already `1`, it deals a
stored delayed damage burst and clears. If that burst kills the target, the
normal battle KO/result path runs.

## Source Facts

```text
debuff3 = [314,325,3]
skill13 = [1,130,542,50,1,30,2,3,150,0]
skill19 = [1,136,548,150,3,15,2,3,200,0]
effect.mid[13] = [0,0,21,0,-1,-1,0]
effect.mid[19] = [0,0,21,0,-1,-1,0]
bufDebuf ar[1][3] -> [0,21,0,-1]
```

Source chain:

- `game.b.b(target)` applies debuff id `3`, stores `w[3][1] = preSkillRawDamage`,
  stores source skill in `w[3][3]`, duration `3`, active flag `1`.
- `game.b.q(3)` skips HP damage while `w[3][0] > 1`.
- On final tick, it deals `w[3][1] * aq.c[1][sourceSkill][8] / 100`.
- Skill `13` uses `150%`; skill `19` uses `200%`.
- P12/P13 active visual is actor/source effect `21` only, no speffect segment.

## Implemented / Tightened

Added dedicated smoke checkpoints:

```text
battle_status_debuff3_before_no_effect
battle_status_debuff3_skill13_producer_apply
battle_status_debuff3_skill19_producer_apply
battle_status_debuff3_body_visual_actor21
battle_status_debuff3_tick1_no_damage_duration2
battle_status_debuff3_tick2_no_damage_duration1
battle_status_debuff3_final_tick_damage_skill13
battle_status_debuff3_final_tick_damage_skill19
battle_status_debuff3_final_tick_ko_transition
battle_status_debuff3_buff14_blocks_skill13
```

These checkpoints are now in fixed `battle_quick` regression.

No runtime gameplay patch was required; the dedicated smoke proved the current
runtime matches the source-shaped behavior for this slice.

## Numeric Smoke Results

| Slice | Before | After |
| --- | --- | --- |
| before/no effect | no debuff3 | no enemy status icon |
| skill13 producer | no debuff3 | enemy icon `4/137`, source skill `13`, stored raw positive |
| skill19 producer | no debuff3 | enemy icon `4/137`, source skill `19`, stored raw positive |
| body visual | enemy debuff3 active | P12 actor/source effect `21`, sprite `263`, no speffect segment |
| tick1 | HP `80`, duration `3` | HP `80`, duration `2`, icon `4/136` |
| tick2 | HP `80`, duration `2` | HP `80`, duration `1`, icon `4/135` |
| final skill13 | HP `80`, stored raw `20`, duration `1` | HP `50`, damage `30`, icon cleared |
| final skill19 | HP `80`, stored raw `20`, duration `1` | HP `40`, damage `40`, icon cleared |
| final KO | HP `20`, stored raw `20`, skill `19` | HP `0`, route `P8` |
| buff14 block | target has buff14 | skill13 damage hits, debuff3 not applied |

## PNG Output

```text
rebuild_game/build_intro_demo/debuff3_closeout/
rebuild_game/build_intro_demo/suite_battle_quick/
```

Key focused PNGs:

```text
build_intro_demo/debuff3_closeout/battle_status_debuff3_before_no_effect.png
build_intro_demo/debuff3_closeout/battle_status_debuff3_skill13_producer_apply.png
build_intro_demo/debuff3_closeout/battle_status_debuff3_body_visual_actor21.png
build_intro_demo/debuff3_closeout/battle_status_debuff3_tick1_no_damage_duration2.png
build_intro_demo/debuff3_closeout/battle_status_debuff3_final_tick_damage_skill13.png
build_intro_demo/debuff3_closeout/battle_status_debuff3_final_tick_ko_transition.png
```

## Verification

```text
build.ps1 PASS
com.vqsv.rebuild.Main --check PASS
VqsvBattleDamageFormulaCheck PASS
Java mojibake scan PASS, no matches
focused debuff3 closeout PNG smoke 10/10 PASS
battle_quick PASS 175/175
```

## Classification

```text
Debuff3 producer skills 13/19: PORTED
Stored raw delayed damage value: PORTED
P12/P13 actor body visual effect 21: PORTED
No-damage tick timing 3->2 and 2->1: PORTED
Final delayed damage 150%/200%: PORTED
Final delayed-damage KO transition: PORTED/PARTIAL for current single-enemy route
Buff14 block: PORTED
Pixel-perfect original comparison: PENDING
```

## Next Step

Move to debuff4 dedicated closeout. Audit first because debuff4 is not a
damage-over-time effect; it stores skill param values from skills `31/37` and
feeds accuracy/miss-speed logic rather than P12/P13 HP damage.
