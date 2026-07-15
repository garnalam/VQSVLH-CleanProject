# 359 - Battle Water Skill 30..39 S60 Reaudit And Battle Lab

Date: 2026-07-15

## Scope

This audit covers the fourth skill group, Water/Ice skills `30..39`, after the S60 resource/table merge.

Rules used:
- Source first: source rows are read from `aq.c[1][skillId]` through `VqsvBattleTables.skill(id)`.
- Visual chain is read from `effect.mid[skillId]` through `VqsvBattleAnimationTables.effectRow(id)`.
- No pixel-perfect claim: current checks verify source row, runtime state, PNG smoke, HP/PP/status data. Original-vs-rebuild frame compare is still pending.
- Battle Lab is now the manual lane for testing this group.

## Easy Mechanism Summary

Water/Ice skills `30..39` are a mix of direct ice/water damage, speed/accuracy/output debuffs, and two defensive buffs:

- `30 Bong bang`: direct Water/Ice damage with actor effect `23/state0`.
- `31 Bang lao`: damage plus `debuff4` Moc, stored source param `1`.
- `32 Tuyet anh`: damage plus `debuff5` Cham Chap, source param `10`.
- `33 Thuy tri`: damage plus `debuff6` Nhat Chi, source param `10`.
- `34 Thuat cau nguyen`: no damage; self `buff5` Vo hinh.
- `35 Thuy bich`: no damage; self `buff6` Kien nhan. Runtime follows the current PC rebuild INTENTIONAL_DEVIATION decided earlier.
- `36 Bao Phong Tuyet`: stronger direct damage.
- `37 La chan gia tuyet`: damage plus `debuff4`; long multi-chunk effect row.
- `38 Bang Phong Ham Tinh`: stronger damage plus `debuff5`.
- `39 Ray lanh`: stronger damage plus `debuff6`.

## Source Matrix

| Skill | Name | Source row | effect.mid row | Runtime status |
| --- | --- | --- | --- | --- |
| 30 | Bong bang | `[3,147,559,100,0,45,0,-1,-1,0]` | `[0,0,23,0,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 31 | Bang lao | `[3,148,560,60,0,45,2,4,1,0]` | `[0,0,23,1,-1,-1,0, 0,0,31,0,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 32 | Tuyet anh | `[3,149,561,60,0,45,2,5,10,0]` | `[0,0,23,2,9,-1,0, 0,1,1,0,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 33 | Thuy tri | `[3,150,562,100,1,30,2,6,10,0]` | `[0,0,23,3,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 34 | Thuat cau nguyen | `[3,151,563,0,1,10,1,5,-1,1]` | `[0,0,23,4,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 35 | Thuy bich | `[3,152,564,0,1,10,1,6,-1,1]` | `[0,1,4,0,-1,-1,0, 0,1,17,0,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass; buff6 INTENTIONAL_DEVIATION mechanics |
| 36 | Bao Phong Tuyet | `[3,153,565,150,2,30,0,-1,-1,0]` | `[0,0,23,5,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 37 | La chan gia tuyet | `[3,154,566,100,2,30,2,4,2,0]` | `[0,0,23,6,4,-1,0, 0,1,7,0,-1,0,0, 0,0,31,0,0,-1,0, 0,1,6,0,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 38 | Bang Phong Ham Tinh | `[3,155,567,150,3,15,2,5,10,0]` | `[0,0,23,7,4,-1,0, 0,1,7,0,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 39 | Ray lanh | `[3,156,568,150,3,15,2,6,10,0]` | `[0,0,23,8,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |

## Smoke Results

Focused closeout smoke:
- `battle_water_skills_30_39_closeout`: pass.
- skill30: damage `25`, enemy HP `109->84`, PP `45->44`.
- skill31: damage `13`, enemy HP `109->96`, PP `45->44`, debuff4 active.
- skill32: damage `13`, enemy HP `109->96`, PP `45->44`, debuff5 active.
- skill33: damage `25`, enemy HP `109->84`, PP `30->29`, debuff6 active.
- skill34: no damage, PP `10->9`, buff5 active.
- skill35: no damage, PP `10->9`, buff6 active.
- skill36: damage `37`, enemy HP `109->72`, PP `30->29`.
- skill37: damage `23`, enemy HP `109->86`, PP `30->29`, debuff4 active.
- skill38: damage `37`, enemy HP `109->72`, PP `15->14`, debuff5 active.
- skill39: damage `37`, enemy HP `109->72`, PP `15->14`, debuff6 active.

PNG output roots:
- `rebuild_game/build_intro_demo/water_verify/skill30_39`
- `rebuild_game/build_intro_demo/battle_lab_suites/npc/water_skills_30_39`

Each skill has:
- `battle_skillXX_<slug>_timeline_before.png`
- `battle_skillXX_<slug>_timeline_effect_start.png`
- `battle_skillXX_<slug>_timeline_result.png`

## Battle Lab

New manual scenarios:
- `water_skill30_bong_bang`
- `water_skill31_bang_lao`
- `water_skill32_tuyet_anh`
- `water_skill33_thuy_tri`
- `water_skill34_thuat_cau_nguyen`
- `water_skill35_thuy_bich`
- `water_skill36_bao_phong_tuyet`
- `water_skill37_la_chan_gia_tuyet`
- `water_skill38_bang_phong_ham_tinh`
- `water_skill39_ray_lanh`

Suite:
- `run_battle_lab_suite_smoke.cmd -Lane npc -Suite water_skills_30_39`

Manual examples:
- `run_battle_lab.cmd -Lane npc -Scenario water_skill30_bong_bang`
- `run_battle_lab.cmd -Lane npc -Scenario water_skill39_ray_lanh`

## Honest Remaining Work

- PORTED/PARTIAL: source rows, effect rows, runtime HP/PP/status, and PNG checkpoints are verified.
- INTENTIONAL_DEVIATION: buff6 `Kien nhan` mechanics follow the PC rebuild gameplay decision made earlier, not exact original source semantics.
- PENDING: original-vs-rebuild pixel compare for Water/Ice skill frame timing and exact MIDP draw order.
- PENDING: full manual feel test in Battle Lab for every Water/Ice skill after user review.
- PENDING: next element skill group audit/integration after Water/Ice, likely skill `40..49`.

## Next Recommended Step

Move to the fifth skill group `40..49`: source reaudit first, then Battle Lab integration/smoke, then focused timeline closeout for skills whose effect.mid rows or runtime behavior differ after S60.
