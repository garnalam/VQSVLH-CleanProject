# 283 - Battle Debuff1 Me Muoi Closeout

Scope: debuff table `aq.c[7][1]`, producer skills `2/8/22/28`,
active queue P12/P13 visual/tick, catch multiplier, conditional skills `23/29`,
and buff14 block regression.

## Source Facts

```text
debuff1 = [312,323,2]
skill2  = [0,119,531,100,0,45,2,1,10,0]
skill8  = [0,125,537,200,3,15,2,1,20,0]
skill22 = [2,139,551,50,0,45,2,1,25,0]
skill28 = [2,145,557,150,3,15,2,1,25,0]
skill23 = [2,140,552,100,1,30,0,-1,250,0]
skill29 = [2,146,558,180,3,15,0,-1,300,0]
bufDebuf ar[1][1] = 1
bufDebuf aq[1] = [1,14,0,-1]
```

Source behavior:

- Producer skills `2/8/22/28` are direct damage skills with effect id `1`.
- Producer chance uses skill column `[8]`: `10`, `20`, `25`, `25`.
- On apply, debuff1 stores no numeric tick value; it is a flag/status.
- Duration starts at `2`.
- `game.b.q(1)` has no HP/stat tick body; it only decrements duration and clears when expired.
- HUD icon is debuff id + 1, so debuff1 icon cell is `2`.
- Duration cell is `134 + duration`, so `136 -> 135 -> clear`.
- P12/P13 body visual uses `aq[1]`, speffect `14`, AH type `12`.
- Catch chance status multiplier for debuff1 is `11/10`.
- Skills `23/29` use their stronger `[8]` damage branch when target has debuff1.
- Buff14 blocks incoming debuff1 before debuff state is committed.

## Implemented / Tightened

Added focused smoke checkpoints:

```text
battle_status_debuff1_before_no_effect
battle_status_debuff1_skill2_producer_apply
battle_status_debuff1_skill8_producer_apply
battle_status_debuff1_skill22_producer_apply
battle_status_debuff1_skill28_producer_apply
battle_status_debuff1_body_visual_speffect14_type12
battle_status_debuff1_tick_noop_duration
battle_status_debuff1_expiry_clears_icon
battle_status_debuff1_catch_multiplier
battle_status_debuff1_conditional_skill23_damage
battle_status_debuff1_conditional_skill29_damage
battle_status_debuff1_buff14_blocks_skill2
```

These are now part of fixed `battle_quick` regression.

## Measured Smoke Results

Focused PNG smoke:

```text
build_intro_demo/debuff1_closeout/battle_status_debuff1_before_no_effect.png
build_intro_demo/debuff1_closeout/battle_status_debuff1_skill2_producer_apply.png
build_intro_demo/debuff1_closeout/battle_status_debuff1_skill8_producer_apply.png
build_intro_demo/debuff1_closeout/battle_status_debuff1_skill22_producer_apply.png
build_intro_demo/debuff1_closeout/battle_status_debuff1_skill28_producer_apply.png
build_intro_demo/debuff1_closeout/battle_status_debuff1_body_visual_speffect14_type12.png
build_intro_demo/debuff1_closeout/battle_status_debuff1_tick_noop_duration.png
build_intro_demo/debuff1_closeout/battle_status_debuff1_expiry_clears_icon.png
build_intro_demo/debuff1_closeout/battle_status_debuff1_catch_multiplier.png
build_intro_demo/debuff1_closeout/battle_status_debuff1_conditional_skill23_damage.png
build_intro_demo/debuff1_closeout/battle_status_debuff1_conditional_skill29_damage.png
build_intro_demo/debuff1_closeout/battle_status_debuff1_buff14_blocks_skill2.png
```

Numeric before / during / after:

| Slice | Before | During | After |
| --- | --- | --- | --- |
| no effect | enemy icon count `0`, no debuff1 | no active queue | unchanged |
| skill2 producer | no debuff1 | forced debuff roll `0`, hit commits `appliedDebuffId=1` | enemy icon `2/136`, value `0`, source skill `2` |
| skill8 producer | no debuff1 | forced debuff roll `0`, hit commits `appliedDebuffId=1` | enemy icon `2/136`, value `0`, source skill `8` |
| skill22 producer | no debuff1 | forced debuff roll `0`, hit commits `appliedDebuffId=1` | enemy icon `2/136`, value `0`, source skill `22` |
| skill28 producer | no debuff1 | forced debuff roll `0`, hit commits `appliedDebuffId=1` | enemy icon `2/136`, value `0`, source skill `28` |
| body visual | enemy has debuff1 icon `2/136` | P12 active queue visual `aq[1]`, speffect `14`, AH type `12` | actor remains anchored on source platform |
| tick no-op | HP `50`, duration `2` | no HP/stat change, text hidden | HP `50`, icon `2/135` |
| expiry | HP `50`, duration `2` | two source ticks, no HP/stat change | HP `50`, duration `0`, icon cleared |
| catch multiplier | item1 base chance from P21 | debuff1 active | chance = `base * 11 / 10` |
| skill23 conditional | same seed, attack `120`, defense `100` | target has debuff1, uses `[8]=250` branch | damage `50`, greater than baseline |
| skill29 conditional | same seed, attack `120`, defense `100` | target has debuff1, uses `[8]=300` branch | damage `60`, greater than baseline |
| buff14 block | enemy has buff14 icon `26/137` | skill2 still hits for damage, but `appliedDebuffId=-1` | no debuff1 icon, buff14 remains |

## Verification

```text
build.ps1 PASS
com.vqsv.rebuild.Main --check PASS
VqsvBattleDamageFormulaCheck PASS
focused debuff1 closeout PNG smoke 12/12 PASS
battle_quick PASS 154/154
git diff --check PASS
```

## Classification

```text
Debuff1 core flag/duration logic: PORTED
Producer skills 2/8/22/28: PORTED
P12/P13 body visual speffect14 type12: PORTED
No-op tick and expiry clear: PORTED
Catch multiplier 11/10: PORTED
Conditional skills 23/29: PORTED
Buff14 block regression: PORTED
Pixel-perfect: NOT CLAIMED
```

Pixel-perfect is not claimed because there is no original-vs-rebuild frame
comparison for speffect14/type12. This closeout proves source row, effect id,
status icon/duration state, no-op tick semantics, catch formula hook,
conditional damage hook, and buff14 block behavior.

## Next Step

Move to debuff2 dedicated closeout in table order. Recommended slice: audit
debuff2 `Quan Quanh` producer skills `12/18`, icon/duration, P12/P13 body
visual, catch multiplier `12/10`, run/item/switch command-lock consumers if
source proves them in current battle UI, and buff14 block regression.
