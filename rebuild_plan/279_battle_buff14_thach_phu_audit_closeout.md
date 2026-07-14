# 279 - Battle Buff14 Thach Phu Audit And Closeout

Scope: buff14 `Thach Phu` / skill25 source audit plus focused runtime smoke.

## Source Rows

```text
skill25 = [2,142,554,0,1,10,1,14,-1,1]
buff14  = [347,362,3,-1,-1]
effect.mid[25] = [0,1,4,0,-1,-1,0, 0,1,17,0,-1,-1,0]
```

## Source Chain

| Source | Fact |
| --- | --- |
| `game.d.q()` | For skill25/default effect mode, applies buff id `14` to selected same-side target. |
| `game.b.a(14, value, skill25)` | Calls `C()` to clear all debuffs, queues buff14, duration `3`. No heal and no stat number. |
| `game.b` debuff apply path | If target `m(14)`, incoming debuff application is blocked and applied debuff id becomes `-1`. |
| `game.d` P12/P13 setup | If buff13 or buff14 is active, source clears debuffs with `C()` before building active queue. |
| `game.d.ai[0]` active visual gate | Buff ids `3`, `5`, and `13` use body/active queue visual. Buff14 is excluded, so no P12/P13 body visual. |

## Visual/UI

| Surface | Source-backed result |
| --- | --- |
| Producer skill25 | P7 effect row plays speffect `4` / AH type7, then speffect `17` / AH type1. No normal damage frame/hitroll because power is `0`. |
| Status icon | Buff icon cell `26`; duration cells `137 -> 136 -> 135 -> clear`. |
| P12/P13 active tick | No body visual. Runtime still ticks duration and P13 pre-clears debuffs while buff14 is active. |

## Focused Smoke Checkpoints

```text
battle_status_buff14_before_no_effect
battle_status_buff14_skill25_speffect4
battle_status_buff14_skill25_speffect17
battle_status_buff14_after_apply_cleanse_protect
battle_status_buff14_blocks_debuff_families
battle_status_buff14_p13_no_body_visual_preclear
battle_status_buff14_expiry_clears_icon
```

## Current Classification

```text
PORTED
```

Reason: skill25 producer visual, no-damage route, debuff cleanse, immunity/block against debuff families `0/1/2/3/4/5/6/7/10`, P13 no-body-visual/pre-clear, icon/duration, and expiry are smoke-covered. Original-client pixel-perfect comparison is still not claimed for visual frames.

## Smoke Results

Focused PNG smoke passed:

```text
build_intro_demo/buff14_closeout/battle_status_buff14_before_no_effect.png
build_intro_demo/buff14_closeout/battle_status_buff14_skill25_speffect4.png
build_intro_demo/buff14_closeout/battle_status_buff14_skill25_speffect17.png
build_intro_demo/buff14_closeout/battle_status_buff14_after_apply_cleanse_protect.png
build_intro_demo/buff14_closeout/battle_status_buff14_blocks_debuff_families.png
build_intro_demo/buff14_closeout/battle_status_buff14_p13_no_body_visual_preclear.png
build_intro_demo/buff14_closeout/battle_status_buff14_expiry_clears_icon.png
```

Measured source-shaped behavior from smoke:

```text
apply path: debuff5 cleared, buff14 active, icon 26 duration cell 137
immunity path: skills [1,2,12,13,31,32,33,51,41] all return appliedDebuff=-1
P13 path: injected debuff5 is pre-cleared, duration 3->2, no active queue visual start for id14
expiry path: duration 3->2->1->0, icon clears
```

Regression passed:

```text
build.ps1
focused buff14 PNG smoke
com.vqsv.rebuild.Main --check
VqsvBattleDamageFormulaCheck
VqsvIntroDemo --smoke-suite battle_quick build_intro_demo/suite_battle_quick_buff14
```

## Next

Buff table `aq.c[6]` rows 0..14 are now closed in table order, with buff6 and buff10 intentionally marked as user-approved gameplay deviations. Next recommended lane: create a concise buff/debuff phase closeout audit, then start debuff table `aq.c[7]` in order from debuff0 unless the user redirects to a concrete skill route.
