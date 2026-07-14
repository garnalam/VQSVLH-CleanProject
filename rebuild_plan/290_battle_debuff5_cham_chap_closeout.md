# 290 - Battle Debuff5 Cham Chap Closeout

Scope: dedicated closeout for target-side debuff id `5` / `Cham Chap`.

## Plain Gameplay Explanation

`Cham Chap` lowers speed for a few turns.

It has no HP damage tick. Its important gameplay effect is that speed is lower
while the status is active, and battle hit/miss logic later reads that lowered
current speed.

## Source Facts

```text
debuff5 = [316,327,3]
skill32 = [3,149,561,60,0,45,2,5,10,0]
skill38 = [3,155,567,150,3,15,2,5,10,0]
skill61 = [6,178,590,80,0,45,2,5,5,0]
skill67 = [6,184,596,110,2,30,2,5,5,0]
bufDebuf ar[1][5] = [1,0,0,-1, 0,25,0,-1]
game.d.ai[1] = [0,1,2,3,8,9,10]
```

Important skill67 conclusion:

Skill67 has `skill[7] == 5` in the table, but source bytecode routes it to
default raw damage and does not apply debuff5. It is covered as a regression in
this closeout.

## Implemented / Tightened

Added dedicated smoke checkpoints:

```text
battle_status_debuff5_before_no_effect
battle_status_debuff5_skill32_producer_speed10
battle_status_debuff5_skill38_producer_speed10
battle_status_debuff5_skill61_producer_speed5
battle_status_debuff5_skill67_raw_no_debuff
battle_status_debuff5_p12_no_body_visual_reassert_speed
battle_status_debuff5_expiry_restores_speed
battle_status_debuff5_miss_chance_attacker_speed_down
battle_status_debuff5_buff14_blocks_skill32
```

These checkpoints are now in fixed `battle_quick` regression.

Runtime gameplay did not require a logic patch. One smoke-only debug helper was
added for direct player-side debuff ticking so the miss-chance consumer could be
proven without opening the client.

## Numeric Smoke Results

| Slice | Before | After |
| --- | --- | --- |
| before/no effect | base/current speed `100/100` | no debuff5 icon |
| skill32 producer | base speed `100` | value `10`, current speed `90`, icon `6/137` |
| skill38 producer | base speed `100` | value `10`, current speed `90`, icon `6/137` |
| skill61 producer | base speed `100` | value `5`, current speed `95`, icon `6/137` |
| skill67 regression | base speed `100` | raw damage only, no debuff5, speed `100` |
| P12 no visual | debuff5 duration `3`, value `10` | no visual start, speed `90`, duration `2` |
| expiry | speed `90`, duration `3` | speed `100`, duration `0`, icon cleared |
| miss chance consumer | player base speed `55`, debuff value `10` | current speed `45`, target speed `50`, miss chance `10` |
| buff14 block | target speed `100`, target has buff14 | damage hits, debuff5 not applied, speed remains `100` |

## PNG Output

```text
rebuild_game/build_intro_demo/debuff5_closeout/
rebuild_game/build_intro_demo/suite_battle_quick/
```

Key focused PNGs:

```text
build_intro_demo/debuff5_closeout/battle_status_debuff5_before_no_effect.png
build_intro_demo/debuff5_closeout/battle_status_debuff5_skill32_producer_speed10.png
build_intro_demo/debuff5_closeout/battle_status_debuff5_skill61_producer_speed5.png
build_intro_demo/debuff5_closeout/battle_status_debuff5_skill67_raw_no_debuff.png
build_intro_demo/debuff5_closeout/battle_status_debuff5_p12_no_body_visual_reassert_speed.png
build_intro_demo/debuff5_closeout/battle_status_debuff5_expiry_restores_speed.png
build_intro_demo/debuff5_closeout/battle_status_debuff5_miss_chance_attacker_speed_down.png
```

## Verification

```text
build.ps1 PASS
focused debuff5 closeout PNG smoke 9/9 PASS
```

Full regression/check commands are run after roadmap updates:

```text
com.vqsv.rebuild.Main --check
VqsvBattleDamageFormulaCheck
Java mojibake scan
battle_quick
```

## Classification

```text
Debuff5 producer skills 32/38/61: PORTED
Skill67 debuff5 path: NOT_REACHED / REGRESSION-LOCKED
Stored speed-down values 10/10/5: PORTED
Immediate current speed reduction: PORTED
P12/P13 no-body-visual skip via game.d.ai[1]: PORTED
Tick reassert + expiry speed restore: PORTED
Miss chance consumer through lowered current speed: PORTED
Buff14 block: PORTED
Pixel-perfect original comparison: PENDING
```

## Next Step

Move to debuff6 dedicated closeout. Audit first because debuff6 is outgoing
damage down: skills `33/39` store a flat percent value and the damage formula
subtracts `damage * w[6][1] / 100`.

