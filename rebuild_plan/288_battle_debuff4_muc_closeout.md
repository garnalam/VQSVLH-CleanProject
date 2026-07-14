# 288 - Battle Debuff4 Muc Closeout

Scope: dedicated closeout for target-side debuff id `4` / `Muc`.

## Plain Gameplay Explanation

`Muc` lowers the attacker's chance to land later attacks by feeding the miss
chance formula. It does not damage HP over time and does not play a body visual
each turn.

When a pet with debuff4 attacks:

```text
effective attacker speed = attacker speed - stored debuff4 value
miss chance = (target speed - effective attacker speed) * 2
clamp to 0..20
```

Skill `31` stores value `1`; skill `37` stores value `2`.

## Source Facts

```text
debuff4 = [315,326,3]
skill31 = [3,148,560,60,0,45,2,4,1,0]
skill37 = [3,154,566,100,2,30,2,4,2,0]
effect.mid[31] = [0,0,23,0,-1,-1,0]
effect.mid[37] = [0,0,23,0,-1,-1,0, 0,1,7,0,-1,0,0, 0,1,6,0,-1,-1,0]
bufDebuf ar[1][4] = [1,1,0,0, 1,11,0,-1]
game.d.ai[1] = [0,1,2,3,8,9,10]
```

Important visual conclusion:

`bufDebuf` has a row for debuff4, but source `game.d.ai[1]` excludes id `4`.
Therefore P12/P13 skips the visual row and directly applies the no-op tick.

## Implemented / Tightened

Added dedicated smoke checkpoints:

```text
battle_status_debuff4_before_no_effect
battle_status_debuff4_skill31_producer_apply
battle_status_debuff4_skill37_producer_apply
battle_status_debuff4_p12_no_body_visual_skip
battle_status_debuff4_tick_noop_duration2
battle_status_debuff4_expiry_clears_icon
battle_status_debuff4_miss_chance_value1
battle_status_debuff4_miss_chance_value2
battle_status_debuff4_buff14_blocks_skill31
```

These checkpoints are now in fixed `battle_quick` regression.

No runtime gameplay patch was required. The dedicated smoke proved the current
runtime matches the source-shaped behavior for this slice.

## Numeric Smoke Results

| Slice | Before | After |
| --- | --- | --- |
| before/no effect | no debuff4 | no enemy status icon |
| skill31 producer | no debuff4 | enemy icon `5/137`, value `1`, source skill `31` |
| skill37 producer | no debuff4 | enemy icon `5/137`, value `2`, source skill `37` |
| P12 no body visual | enemy debuff4 active, duration `3` | no visual start, no actor/speffect, HP unchanged, duration `2` |
| tick no-op | HP `50`, duration `3`, value `1` | HP `50`, duration `2`, icon `5/136` |
| expiry | HP `50`, duration `3`, value `2` | HP `50`, duration `0`, icon cleared |
| miss value1 | attacker speed `55`, target speed `60`, value `1` | miss chance `12` |
| miss value2 | attacker speed `55`, target speed `60`, value `2` | miss chance `14` |
| buff14 block | target has buff14 | skill31 damage hits, debuff4 not applied |

## PNG Output

```text
rebuild_game/build_intro_demo/debuff4_closeout/
rebuild_game/build_intro_demo/suite_battle_quick/
```

Key focused PNGs:

```text
build_intro_demo/debuff4_closeout/battle_status_debuff4_before_no_effect.png
build_intro_demo/debuff4_closeout/battle_status_debuff4_skill31_producer_apply.png
build_intro_demo/debuff4_closeout/battle_status_debuff4_p12_no_body_visual_skip.png
build_intro_demo/debuff4_closeout/battle_status_debuff4_miss_chance_value1.png
build_intro_demo/debuff4_closeout/battle_status_debuff4_buff14_blocks_skill31.png
```

## Verification

```text
build.ps1 PASS
focused debuff4 closeout PNG smoke 9/9 PASS
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
Debuff4 producer skills 31/37: PORTED
Stored flat miss values 1/2: PORTED
P12/P13 no-body-visual skip via game.d.ai[1]: PORTED
Tick/expiry HP no-op: PORTED
Miss chance consumer: PORTED
Buff14 block: PORTED
Pixel-perfect original comparison: PENDING
```

## Next Step

Move to debuff5 dedicated closeout. Audit first because debuff5 is speed-down:
skills `32/38/61` store a percentage of base speed into `w[5][1]`, update
current speed, and P12/P13 source visual gate also skips body visual for id `5`.

