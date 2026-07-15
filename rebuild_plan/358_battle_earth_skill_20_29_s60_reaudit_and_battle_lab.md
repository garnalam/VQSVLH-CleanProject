# 358 - Battle Earth Skill 20..29 S60 Reaudit And Battle Lab

Date: 2026-07-14

## Scope

This audit covers the third skill group, Earth skills `20..29`, after the S60 resource/table merge.

Rules used:
- Source first: source rows are read from `aq.c[1][skillId]` through `VqsvBattleTables.skill(id)`.
- Visual chain is read from `effect.mid[skillId]` through `VqsvBattleAnimationTables.effectRow(id)`.
- No pixel-perfect claim: current checks verify source row, runtime state, PNG smoke, HP/PP/status data. Original-vs-rebuild frame compare is still pending.
- Battle Lab is now the manual lane for testing this group.

## Easy Mechanism Summary

Earth skills in this group are mostly rock/sand attacks plus defensive shields:

- `20 Hat bui`: direct Earth damage with actor effect `22/state0`.
- `21 Tho thuan`: damage plus self `buff4` defense shield; S60 row now has three visual chunks.
- `22 Bao cat`: damage plus chance to apply `debuff1` Me Muoi; S60 row now uses actor `22/state3` then `22/state4`.
- `23 Nham bang`: direct damage; if target already has `debuff1`, damage uses conditional param `250`.
- `24 Nguoi bao ve Dia Gioi`: no damage; self heal/cleanse plus `buff13`; producer visual uses actor `22/state6` then `speffect17`.
- `25 Thach phu thuat`: no damage; self `buff14` immunity/protection visual uses `speffect4` then `speffect17`.
- `26 Nham bao`: stronger direct damage with actor `22/state6` then `speffect6`.
- `27 Hang rao cat da`: damage plus self `buff4`; visual uses actor `22/state7`, actor `32/state0`, then `speffect7`.
- `28 Bao cat`: stronger `Bao cat`; damage plus chance to apply `debuff1`.
- `29 Tho Chi Loan Vu`: high damage; conditional route is verified with target preloaded with `debuff1`, param `300`.

## Source Matrix

| Skill | Name | Source row | effect.mid row | Runtime status |
| --- | --- | --- | --- | --- |
| 20 | Hat bui | `[2,137,549,100,0,45,0,-1,-1,0]` | `[0,0,22,0,-1,-1,0]` | PORTED/PARTIAL timeline smoke pass |
| 21 | Tho thuan | `[2,138,550,80,0,45,1,4,10,0]` | `[0,0,22,1,-1,-1,0, 1,1,5,0,-1,-1,0, 1,0,22,2,0,-1,0]` | PORTED/PARTIAL timeline smoke pass |
| 22 | Bao cat | `[2,139,551,50,0,45,2,1,25,0]` | `[0,0,22,3,-1,-1,0, 0,0,22,4,-1,-1,1]` | PORTED/PARTIAL timeline smoke pass |
| 23 | Nham bang | `[2,140,552,100,1,30,0,-1,250,0]` | `[0,0,22,5,-1,-1,0, 0,1,6,0,-1,-1,0]` | PORTED/PARTIAL conditional debuff1 smoke pass |
| 24 | Nguoi bao ve Dia Gioi | `[2,141,553,0,1,10,1,13,-1,1]` | `[0,0,22,6,-1,-1,0, 0,1,17,0,-1,-1,0]` | PORTED/PARTIAL heal/cleanse/buff13 smoke pass |
| 25 | Thach phu thuat | `[2,142,554,0,1,10,1,14,-1,1]` | `[0,1,4,0,-1,-1,0, 0,1,17,0,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 26 | Nham bao | `[2,143,555,150,2,30,0,-1,-1,0]` | `[0,0,22,6,-1,-1,0, 0,1,6,0,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 27 | Hang rao cat da | `[2,144,556,100,2,30,1,4,10,0]` | `[0,0,22,7,-1,-1,0, 1,0,32,0,0,-1,0, 1,1,7,0,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 28 | Bao cat | `[2,145,557,150,3,15,2,1,25,0]` | `[0,0,22,5,-1,-1,0, 0,0,22,4,-1,-1,1]` | PORTED/PARTIAL closeout smoke pass |
| 29 | Tho Chi Loan Vu | `[2,146,558,180,3,15,0,-1,300,0]` | `[0,0,22,8,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass with debuff1 preloaded |

## Smoke Results

Focused timeline smoke:
- `battle_skill20_hat_bui_timeline`: pass, damage `25`, PP `45->44`, enemy HP `109->84`.
- `battle_skill21_tho_thuan_timeline`: pass, damage `23`, PP `45->44`, defense `100->110->120->100`.
- `battle_skill22_bao_cat_timeline`: pass, damage `13`, debuff1 forced roll `0`, enemy HP `109->96`.
- `battle_skill23_nham_bang_timeline`: pass, baseline damage `25`, conditional debuff1 damage `60`.
- `battle_skill24_nguoi_bao_ve_dia_gioi_timeline`: pass, player HP `67->93->114`, PP `10->9`, debuff5 cleared, buff13 expired after ticks.
- `battle_earth_skills_25_29_closeout`: pass:
  - skill25: buff14 active true, damage `0`, PP `10->9`.
  - skill26: damage `35`, enemy HP `109->74`, PP `30->29`.
  - skill27: damage `23`, buff4 active true, PP `30->29`.
  - skill28: damage `35`, debuff1 active true, PP `15->14`.
  - skill29: damage `72` with debuff1 preloaded, PP `15->14`.

PNG output roots:
- `rebuild_game/build_intro_demo/earth_existing/skill20`
- `rebuild_game/build_intro_demo/earth_existing/skill21`
- `rebuild_game/build_intro_demo/earth_existing/skill22`
- `rebuild_game/build_intro_demo/earth_existing/skill23`
- `rebuild_game/build_intro_demo/earth_existing/skill24`
- `rebuild_game/build_intro_demo/earth_existing/skill25_29`
- `rebuild_game/build_intro_demo/battle_lab_suites/npc/earth_skills_20_29`

## Battle Lab

New manual scenarios:
- `earth_skill20_hat_bui`
- `earth_skill21_tho_thuan`
- `earth_skill22_bao_cat`
- `earth_skill23_nham_bang`
- `earth_skill24_nguoi_bao_ve_dia_gioi`
- `earth_skill25_thach_phu_thuat`
- `earth_skill26_nham_bao`
- `earth_skill27_hang_rao_cat_da`
- `earth_skill28_bao_cat`
- `earth_skill29_tho_chi_loan_vu`

Suite:
- `run_battle_lab_suite_smoke.cmd -Lane npc -Suite earth_skills_20_29`

Manual example:
- `run_battle_lab.cmd -Lane npc -Scenario earth_skill20_hat_bui`
- `run_battle_lab.cmd -Lane npc -Scenario earth_skill29_tho_chi_loan_vu`

## Honest Remaining Work

- PORTED/PARTIAL: source rows, effect rows, runtime HP/PP/status, and PNG checkpoints are verified.
- PENDING: original-vs-rebuild pixel compare for Earth skill frame timing and exact MIDP draw order.
- PENDING: full manual feel test in Battle Lab for every Earth skill after user review.
- PENDING: next element skill group audit/integration after Earth, likely skill `30..39`.

## Next Recommended Step

Move to the fourth skill group `30..39`: source reaudit first, then Battle Lab integration/smoke, then focused timeline closeout for skills whose effect.mid rows changed after S60.
