# 360 - Battle Electric Skill 40..49 S60 Reaudit And Battle Lab

Date: 2026-07-15

## Scope

This audit covers the fifth skill group, Electric skills `40..49`, after the S60 resource/table merge.

Rules used:
- Source first: source rows are read from `aq.c[1][skillId]` through `VqsvBattleTables.skill(id)`.
- Visual chain is read from `effect.mid[skillId]` through `VqsvBattleAnimationTables.effectRow(id)`.
- No pixel-perfect claim: current checks verify source row, runtime state, PNG smoke, HP/PP/status data. Original-vs-rebuild frame compare is still pending.
- Battle Lab is the manual lane for testing this group.

## Easy Mechanism Summary

Electric skills `40..49` mix direct electric damage, paralysis, agility, and two self electric charge buffs:

- `40 Dien giat`: direct Electric damage with actor effect `24/state0`.
- `41 Loi thiem`: damage plus `debuff10` Te Liet.
- `42 Nap dien`: damage plus self `buff7` Linh Xao.
- `43 Song dien tu`: direct damage with a two-chunk visual.
- `44 Doat menh cao ap`: no damage; self `buff8` Dien ap.
- `45 Dien nang chuyen doi`: no damage; self `buff9` Hoa Thach.
- `46 Tia lua dien`: stronger direct damage.
- `47 Cham sam sat`: damage plus `debuff10` Te Liet.
- `48 Dien quang thach hoa`: damage plus self `buff7` Linh Xao.
- `49 Cam ung dien tu`: high direct damage.

## Source Matrix

| Skill | Name | Source row | effect.mid row | Runtime status |
| --- | --- | --- | --- | --- |
| 40 | Dien giat | `[4,157,569,100,0,45,0,-1,-1,0]` | `[0,0,24,0,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 41 | Loi thiem | `[4,158,570,90,0,45,2,10,10,0]` | `[0,0,24,1,8,-1,0, 0,1,4,0,-1,-1,1]` | PORTED/PARTIAL closeout smoke pass |
| 42 | Nap dien | `[4,159,571,90,0,45,1,7,5,0]` | `[0,0,24,0,3,-1,0, 1,0,32,0,1,-1,0, 1,1,1,0,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 43 | Song dien tu | `[4,160,572,100,1,30,0,-1,-1,0]` | `[0,0,24,3,4,-1,0, 0,1,4,0,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 44 | Doat menh cao ap | `[4,161,573,0,1,10,1,8,-1,1]` | `[0,0,27,0,0,-1,0, 0,1,19,0,-1,-1,0, 0,1,15,0,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 45 | Dien nang chuyen doi | `[4,162,574,0,1,10,1,9,-1,1]` | `[0,0,27,0,0,-1,0, 0,1,19,0,-1,-1,0, 0,1,15,0,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 46 | Tia lua dien | `[4,163,575,150,2,30,0,-1,-1,0]` | `[0,0,24,4,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 47 | Cham sam sat | `[4,164,576,130,2,30,2,10,10,0]` | `[0,0,24,5,10,-1,0, 0,1,4,0,-1,-1,1]` | PORTED/PARTIAL closeout smoke pass |
| 48 | Dien quang thach hoa | `[4,165,577,130,3,15,1,7,5,0]` | `[0,0,24,4,-1,-1,0, 1,0,32,0,1,-1,0, 1,1,9,0,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 49 | Cam ung dien tu | `[4,166,578,180,3,15,0,-1,-1,0]` | `[0,0,24,6,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |

## Smoke Results

Focused closeout smoke:
- `battle_electric_skills_40_49_closeout`: pass.
- skill40: damage `23`, enemy HP `109->86`, PP `45->44`.
- skill41: damage `20`, enemy HP `109->89`, PP `45->44`, debuff10 active.
- skill42: damage `25`, enemy HP `109->84`, PP `45->44`, buff7 active.
- skill43: damage `23`, enemy HP `109->86`, PP `30->29`.
- skill44: no damage, PP `10->9`, buff8 active.
- skill45: no damage, PP `10->9`, buff9 active.
- skill46: damage `37`, enemy HP `109->72`, PP `30->29`.
- skill47: damage `32`, enemy HP `109->77`, PP `30->29`, debuff10 active.
- skill48: damage `25`, enemy HP `109->84`, PP `15->14`, buff7 active.
- skill49: damage `42`, enemy HP `109->67`, PP `15->14`.

PNG output roots:
- `rebuild_game/build_intro_demo/electric_verify/skill40_49`
- `rebuild_game/build_intro_demo/battle_lab_suites/npc/electric_skills_40_49`

Each skill has:
- `battle_skillXX_<slug>_timeline_before.png`
- `battle_skillXX_<slug>_timeline_effect_start.png`
- `battle_skillXX_<slug>_timeline_result.png`

## Battle Lab

New manual scenarios:
- `electric_skill40_dien_giat`
- `electric_skill41_loi_thiem`
- `electric_skill42_nap_dien`
- `electric_skill43_song_dien_tu`
- `electric_skill44_doat_menh_cao_ap`
- `electric_skill45_dien_nang_chuyen_doi`
- `electric_skill46_tia_lua_dien`
- `electric_skill47_cham_sam_sat`
- `electric_skill48_dien_quang_thach_hoa`
- `electric_skill49_cam_ung_dien_tu`

Suite:
- `run_battle_lab_suite_smoke.cmd -Lane npc -Suite electric_skills_40_49`

Manual examples:
- `run_battle_lab.cmd -Lane npc -Scenario electric_skill40_dien_giat`
- `run_battle_lab.cmd -Lane npc -Scenario electric_skill49_cam_ung_dien_tu`

## Honest Remaining Work

- PORTED/PARTIAL: source rows, effect rows, runtime HP/PP/status, and PNG checkpoints are verified.
- PENDING: original-vs-rebuild pixel compare for Electric skill frame timing and exact MIDP draw order.
- PENDING: full manual feel test in Battle Lab for every Electric skill after user review.
- PENDING: next element skill group audit/integration after Electric, likely skill `50..59`.

## Next Recommended Step

Move to the sixth skill group `50..59`: source reaudit first, then Battle Lab integration/smoke, then focused timeline closeout for skills whose effect.mid rows or runtime behavior differ after S60.
