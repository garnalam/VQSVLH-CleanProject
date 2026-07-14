# 292 - Battle Debuff6 Nhut Chi Closeout

Scope: dedicated closeout for target-side debuff id `6` / `Nhut Chi`.

## Plain Gameplay Explanation

`Nhut Chi` weakens the affected pet's next attacks.

It does not tick HP damage and does not show a body effect during P12/P13. While
the status is active, the debuffed pet deals less outgoing damage. Current
producer skills `33` and `39` both store `10`, so the damage reduction is `10%`.

## Source Facts

```text
debuff6 = [317,328,3]
skill33 = [3,150,562,100,1,30,2,6,10,0]
skill39 = [3,156,568,150,3,15,2,6,10,0]
effect.mid[33] = [0,0,23,0,-1,-1,0]
effect.mid[39] = [0,0,23,0,-1,-1,0]
bufDebuf ar[1][6] = [1,12,0,-1]
game.d.ai[1] = [0,1,2,3,8,9,10]
```

## Implemented / Tightened

Added dedicated smoke checkpoints:

```text
battle_status_debuff6_before_no_effect
battle_status_debuff6_skill33_producer_damage_down10
battle_status_debuff6_skill39_producer_damage_down10
battle_status_debuff6_miss_queue_no_text
battle_status_debuff6_p12_no_body_visual_noop
battle_status_debuff6_expiry_clears_icon
battle_status_debuff6_outgoing_damage_down
battle_status_debuff6_buff14_blocks_skill33
```

Runtime patch:

- Added trace-only formula proof for debuff6 damage reduction in
  `VqsvBattleUnit.computeDamage`.
- Fixed P7 miss behavior for source-immediate side effects:
  - debuff/clear-buff mutations from `game.b.b(target)` now commit even if the
    later P7 hit roll misses;
  - HP damage and debuff text still remain hidden on miss;
  - buff5 reflect storage remains separate and is not forced by this miss path.

## Numeric Smoke Results

| Slice | Before | After |
| --- | --- | --- |
| before/no effect | no debuff6 | no enemy icon |
| skill33 producer | direct power `100%` | value `10`, duration `3`, icon `7/137` |
| skill39 producer | direct power `150%` | value `10`, duration `3`, icon `7/137` |
| miss queue | forced miss | debuff text hidden, debuff6 committed/queued, HP unchanged |
| P12 no visual | duration `3`, value `10`, HP `50` | no body visual, duration `2`, HP `50` |
| expiry | duration `3`, value `10` | duration `0`, icon cleared, HP unchanged |
| outgoing damage | baseline skill10 damage `80` | debuff6 value `10` reduces to `72` |
| buff14 block | target has buff14 | damage hits, debuff6 not applied, buff14 icon remains |

## PNG Output

```text
rebuild_game/build_intro_demo/debuff6_closeout/
```

Key focused PNGs:

```text
build_intro_demo/debuff6_closeout/battle_status_debuff6_before_no_effect.png
build_intro_demo/debuff6_closeout/battle_status_debuff6_skill33_producer_damage_down10.png
build_intro_demo/debuff6_closeout/battle_status_debuff6_skill39_producer_damage_down10.png
build_intro_demo/debuff6_closeout/battle_status_debuff6_miss_queue_no_text.png
build_intro_demo/debuff6_closeout/battle_status_debuff6_p12_no_body_visual_noop.png
build_intro_demo/debuff6_closeout/battle_status_debuff6_expiry_clears_icon.png
build_intro_demo/debuff6_closeout/battle_status_debuff6_outgoing_damage_down.png
build_intro_demo/debuff6_closeout/battle_status_debuff6_buff14_blocks_skill33.png
```

## Verification

```text
build.ps1 PASS
focused debuff6 closeout PNG smoke 8/8 PASS
com.vqsv.rebuild.Main --check PASS
VqsvBattleDamageFormulaCheck PASS
Java mojibake scan PASS
battle_quick PASS 201/201
```

Regression/check commands:

```text
com.vqsv.rebuild.Main --check
VqsvBattleDamageFormulaCheck
Java mojibake scan
battle_quick
```

## Classification

```text
Debuff6 producer skills 33/39: PORTED
Stored damage-down value 10: PORTED
Damage formula consumer damage-=damage*w[6][1]/100: PORTED
P7 miss source-immediate debuff mutation: PORTED
P12/P13 no-body-visual skip via game.d.ai[1]: PORTED
Tick no-op + expiry icon clear: PORTED
Buff14 block: PORTED
Pixel-perfect original comparison: PENDING
```

## Next Step

Move to debuff7 dedicated closeout only if we want to re-tighten an already
ported row. Debuff7 is defense down: skills `51/57` store `20%` of base defense,
lower current defense, and damage formula should increase incoming damage while
the debuff is active.
