# 278 - Battle Buff13 Thach Hoa Audit And Closeout

Scope: buff13 `Thach Hoa` / skill24 source audit plus focused runtime smoke.

## Source Rows

```text
skill24 = [2,141,553,0,1,10,1,13,-1,1]
buff13  = [346,361,3,20,-1]
effect.mid[24] = [0,0,22,0,-1,-1,0, 0,1,17,0,-1,-1,0]
```

## Source Chain

| Source | Fact |
| --- | --- |
| `game.d.q()` | For skill24/default effect mode, applies buff id `13` to selected same-side target. |
| `game.b.a(13, value, skill24)` | Stores `v[13][1] = maxHP * 20 / 100`, heals immediately, calls `C()` to clear all debuffs, queues buff, duration `3`. |
| `game.b.o(13)` | Heals the same stored amount every active tick, then duration lifecycle ticks. |
| `game.d` P12/P13 setup | If buff13 or buff14 is active, source clears debuffs with `C()` before building active queue. |
| `game.d.ai[0]` active visual gate | Buff ids `3`, `5`, and `13` use body/active queue visual; buff13 is included. |

## Visual/UI

| Surface | Source-backed result |
| --- | --- |
| Producer skill24 | P7 effect row plays actor action/source effect `22`, then speffect `17` / AH type1. No normal damage frame/hitroll because power is `0`. |
| Status icon | Buff icon cell `25`; duration cells `137 -> 136 -> 135 -> clear`. |
| P12/P13 body visual | Required for buff13. Runtime uses the source active queue visual row for bank `0`, id `13`: `ap id=13 row=[1,17,0,-1]`, so it plays speffect `17` / AH type1 during P13. |

## Focused Smoke Checkpoints

```text
battle_status_buff13_before_no_effect
battle_status_buff13_skill24_actor22
battle_status_buff13_skill24_speffect17
battle_status_buff13_after_apply_cleanse_heal
battle_status_buff13_p13_body_visual_start
battle_status_buff13_p13_heal_tick
battle_status_buff13_expiry_clears_icon
```

## Current Classification

```text
PORTED
```

Reason: skill24 producer visual, no-damage route, heal amount `20% maxHP`, debuff cleanse, P12/P13 body visual, tick heal, icon/duration, and expiry are smoke-covered. Original-client pixel-perfect comparison is still not claimed for visual frames.

## Smoke Results

Focused PNG smoke passed:

```text
build_intro_demo/buff13_closeout/battle_status_buff13_before_no_effect.png
build_intro_demo/buff13_closeout/battle_status_buff13_skill24_actor22.png
build_intro_demo/buff13_closeout/battle_status_buff13_skill24_speffect17.png
build_intro_demo/buff13_closeout/battle_status_buff13_after_apply_cleanse_heal.png
build_intro_demo/buff13_closeout/battle_status_buff13_p13_body_visual_start.png
build_intro_demo/buff13_closeout/battle_status_buff13_p13_heal_tick.png
build_intro_demo/buff13_closeout/battle_status_buff13_expiry_clears_icon.png
```

Measured source-shaped numbers from smoke:

```text
maxHP=134
heal=maxHP*20/100=26
apply path: HP 67/134 -> 93/134, debuff5 cleared, icon 25 duration cell 137
P13 tick path: body visual starts for bank=0 id=13, then heals +26 and duration 3->2
expiry path: duration 3->2->1->0, icon clears
```

Regression passed:

```text
build.ps1
com.vqsv.rebuild.Main --check
VqsvBattleDamageFormulaCheck
VqsvIntroDemo --smoke-suite battle_quick build_intro_demo/suite_battle_quick_buff13
```

## Next

Next table-order slice after regression: buff14 `Thach Phu`.
