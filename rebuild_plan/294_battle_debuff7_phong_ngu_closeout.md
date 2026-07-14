# 294 - Battle Debuff7 Phong Ngu Closeout

Scope: dedicated closeout for target-side debuff id `7` / `Phong Ngu`.

## Plain Gameplay Explanation

`Phong Ngu` is a defense-down debuff. It makes the affected pet take more damage
because its current defense is lowered for several turns.

It does not tick HP damage and does not show a body effect during P12/P13. The
visible battle feedback is the producer skill animation, the debuff text on hit,
and the HUD status icon.

## Source Facts

```text
debuff7 = [318,329,3]
skill51 = [5,168,580,80,0,45,2,7,20,0]
skill57 = [5,174,586,120,2,30,2,7,20,0]
effect.mid[51] = actorEffect 25, speffect 8, speffect 11
effect.mid[57] = actorEffect 25, speffect 14, speffect 11
bufDebuf ar[1][7] = [1,19,0,-1,1,6,0,-1]
game.d.ai[1] = [0,1,2,3,8,9,10]
```

Source formula:

```text
w[7][1] = target.baseDefense * skill[8] / 100
currentDefense = target.baseDefense - w[7][1]
```

For skills `51/57`, `skill[8] = 20`, so base defense `100` becomes current
defense `80`.

## Implemented / Tightened

Added dedicated smoke checkpoints:

```text
battle_status_debuff7_before_no_effect
battle_status_debuff7_skill51_producer_defense_down20
battle_status_debuff7_skill57_producer_defense_down20
battle_status_debuff7_miss_queue_no_text
battle_status_debuff7_p12_no_body_visual_reassert_defense
battle_status_debuff7_expiry_restores_defense
battle_status_debuff7_incoming_damage_up
battle_status_debuff7_buff14_blocks_skill51
```

No gameplay runtime patch was needed in this closeout beyond the existing
source-immediate P7 side-effect behavior from the debuff6 slice. The only
correction during this slice was the smoke expectation for the incoming-damage
probe: that helper uses target base defense `40`, so the source-shaped stored
value is `40 * 20% = 8`.

## Numeric Smoke Results

| Slice | Before | During / after |
| --- | --- | --- |
| before/no effect | base defense `100`, current defense `100` | no debuff7, no enemy status icon |
| skill51 producer | base defense `100` | stored value `20`, duration `3`, current defense `80`, icon `8/137` |
| skill57 producer | base defense `100` | stored value `20`, duration `3`, current defense `80`, icon `8/137` |
| miss queue | forced P7 miss, enemy HP unchanged | debuff text hidden, debuff7 committed, defense `100 -> 80`, active queue icon `8/136` |
| P12/P13 active queue | enemy HP `50`, duration `3` | no body visual, HP stays `50`, duration `2`, defense stays `80` |
| expiry | duration `3`, defense `80` | duration `3 -> 2 -> 1 -> 0`, HP unchanged, defense restored to `100`, icon cleared |
| incoming damage | baseline skill10 damage sample | with base defense `40`, stored value `8`, damage sample increases to `88` |
| buff14 block | target has buff14 icon `26/137` | skill51 damage can hit, debuff text hidden, debuff7 not applied, defense remains `100` |

## PNG Output

```text
rebuild_game/build_intro_demo/debuff7_closeout/
```

Focused PNGs:

```text
build_intro_demo/debuff7_closeout/battle_status_debuff7_before_no_effect.png
build_intro_demo/debuff7_closeout/battle_status_debuff7_skill51_producer_defense_down20.png
build_intro_demo/debuff7_closeout/battle_status_debuff7_skill57_producer_defense_down20.png
build_intro_demo/debuff7_closeout/battle_status_debuff7_miss_queue_no_text.png
build_intro_demo/debuff7_closeout/battle_status_debuff7_p12_no_body_visual_reassert_defense.png
build_intro_demo/debuff7_closeout/battle_status_debuff7_expiry_restores_defense.png
build_intro_demo/debuff7_closeout/battle_status_debuff7_incoming_damage_up.png
build_intro_demo/debuff7_closeout/battle_status_debuff7_buff14_blocks_skill51.png
```

## Verification

```text
build.ps1 PASS
focused debuff7 closeout PNG smoke 8/8 PASS
```

Full regression commands to run after this doc update:

```text
com.vqsv.rebuild.Main --check
VqsvBattleDamageFormulaCheck
Java/rebuild_plan mojibake scan for touched files
battle_quick
```

## Classification

```text
Debuff7 producer skills 51/57: PORTED
Stored defense-down value 20% base defense: PORTED
Defense formula consumer increases incoming damage: PORTED
P7 miss source-immediate debuff mutation: PORTED
P12/P13 no-body-visual skip via game.d.ai[1]: PORTED
Tick/reassert/expiry restore: PORTED
Buff14 block: PORTED
Pixel-perfect original comparison: PENDING
```

## Next Step

Move to debuff8/debuff9 special-route audit. They are not ordinary direct
stat debuffs; both sit on zero-power/special route behavior, so they need source
proof before any code patch.
