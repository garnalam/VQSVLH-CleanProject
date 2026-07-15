# 361 - Battle Shadow Skill 50..59 S60 Reaudit And Battle Lab

Date: 2026-07-15

## Scope

This audit covers the sixth skill group, family `5` skills `50..59`, after the
S60 resource/table merge.

Rules used:
- Source first: source rows are read from `aq.c[1][skillId]` through
  `VqsvBattleTables.skill(id)`.
- Visual chain is read from `effect.mid[skillId]` through
  `VqsvBattleAnimationTables.effectRow(id)`.
- No pixel-perfect claim: current checks verify source row, effect row,
  runtime state, PNG smoke, HP/PP/debuff/heal data. Original-vs-rebuild frame
  compare is still pending.
- Battle Lab is the manual lane for testing this group.

## Easy Mechanism Summary

Shadow skills `50..59` mix direct damage, defense-down curse, leech-style
post-hit healing, and two zero-power special/status visuals:

- `50 Anh thu`: direct Shadow damage. First target actor effect is source
  effect `25`, sprite `267`, state `0`.
- `51 Chu oan`: damage plus `debuff7` Phong Ngu. This lowers the target's
  defensive effectiveness in the existing source-backed status runtime.
- `52 Quy doc`: damage plus a leech gate. If source RNG gate passes
  (`roll <= 30`), the attacker heals by `damage * chanceOrParam / 100`.
- `53 Con ac mong`: higher direct damage with source effect `25/state4`.
- `54 Mi anh`: zero-power visual/status-style skill. Source row points at
  `debuff8`, but current rebuild producer path intentionally does not apply
  debuff8 from this zero-power producer; debuff8 consumer gameplay is handled
  separately as `INTENTIONAL_DEVIATION/GAMEPLAY_FIXED` from the earlier status
  phase.
- `55 Hon loan`: zero-power special visual. Source row points at `debuff9`,
  but current rebuild producer path intentionally does not apply debuff9 from
  this zero-power producer. The debuff9 consumer allows switching and random
  target behavior is handled separately from the earlier status phase.
- `56 Doc anh thu`: stronger direct Shadow damage with multi-chunk visual.
- `57 Chu Phuoc Quy Lao`: damage plus `debuff7` Phong Ngu.
- `58 Quy doc tin nguong`: damage plus source leech gate, like skill `52`,
  but with stronger damage and `chanceOrParam=8`.
- `59 Loi nguyen cuoi cung`: high direct Shadow damage with source
  effect `25/state10`.

## Source Matrix

| Skill | Name | Source row | effect.mid row | Runtime status |
| --- | --- | --- | --- | --- |
| 50 | Anh thu | `[5,167,579,100,0,45,0,-1,-1,0]` | `[0,0,25,0,5,-1,0, 0,1,9,0,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 51 | Chu oan | `[5,168,580,80,0,45,2,7,20,0]` | `[0,0,25,1,4,-1,0, 0,1,8,0,-1,-1,0, 0,1,11,0,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 52 | Quy doc | `[5,169,581,80,0,45,0,-1,5,0]` | `[0,0,25,2,7,-1,0, 0,1,8,0,-1,-1,0, 1,0,25,3,4,-1,0, 1,1,10,0,-1,-1,1]` | PORTED/PARTIAL closeout smoke pass |
| 53 | Con ac mong | `[5,170,582,200,1,30,0,-1,200,0]` | `[0,0,25,4,2,-1,0, 0,1,9,0,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 54 | Mi anh | `[5,171,583,0,1,10,2,8,40,0]` | `[0,1,0,0,-1,-1,0, 0,0,25,5,-1,-1,0]` | PORTED/PARTIAL visual; producer debuff8 not applied |
| 55 | Hon loan | `[5,172,584,0,1,10,2,9,-1,0]` | `[0,1,12,0,-1,-1,0]` | PORTED/PARTIAL visual; producer debuff9 not applied |
| 56 | Doc anh thu | `[5,173,585,150,2,30,0,-1,-1,0]` | `[0,0,25,6,3,-1,0, 0,1,8,0,-1,-1,0, 0,1,9,0,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 57 | Chu Phuoc Quy Lao | `[5,174,586,120,2,30,2,7,20,0]` | `[0,0,25,7,3,-1,0, 0,1,14,0,-1,-1,0, 0,1,11,0,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 58 | Quy doc tin nguong | `[5,175,587,100,3,15,0,-1,8,0]` | `[0,0,25,8,-1,-1,0, 0,1,13,0,-1,-1,0, 1,0,25,9,5,-1,0, 1,1,10,0,-1,-1,1]` | PORTED/PARTIAL closeout smoke pass |
| 59 | Loi nguyen cuoi cung | `[5,176,588,250,3,15,0,-1,250,0]` | `[0,0,25,10,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |

## Smoke Results

Focused closeout smoke:
- `battle_shadow_skills_50_59_closeout`: pass.
- skill50: damage `25`, enemy HP `109->84`, PP `45->44`.
- skill51: damage `18`, enemy HP `109->91`, PP `45->44`, debuff7 active.
- skill52: damage `20`, forced leech pass, player HP `94->95`, PP `45->44`.
- skill53: damage `25`, enemy HP `109->84`, PP `30->29`.
- skill54: no damage, PP `10->9`, debuff8 producer not applied.
- skill55: no damage, PP `10->9`, debuff9 producer not applied.
- skill56: damage `37`, enemy HP `109->72`, PP `30->29`.
- skill57: damage `29`, enemy HP `109->80`, PP `30->29`, debuff7 active.
- skill58: damage `23`, forced leech pass, player HP `94->95`, PP `15->14`.
- skill59: damage `35`, enemy HP `109->74`, PP `15->14`.

PNG output roots:
- `rebuild_game/build_intro_demo/shadow_verify/skill50_59`
- `rebuild_game/build_intro_demo/battle_lab_suites/npc/shadow_skills_50_59`

Each skill has:
- `battle_skillXX_<slug>_timeline_before.png`
- `battle_skillXX_<slug>_timeline_effect_start.png`
- `battle_skillXX_<slug>_timeline_result.png`

## Battle Lab

New manual scenarios:
- `shadow_skill50_anh_thu`
- `shadow_skill51_chu_oan`
- `shadow_skill52_quy_doc`
- `shadow_skill53_con_ac_mong`
- `shadow_skill54_mi_anh`
- `shadow_skill55_hon_loan`
- `shadow_skill56_doc_anh_thu`
- `shadow_skill57_chu_phuoc_quy_lao`
- `shadow_skill58_quy_doc_tin_nguong`
- `shadow_skill59_loi_nguyen_cuoi_cung`

Suite:
- `run_battle_lab_suite_smoke.cmd -Lane npc -Suite shadow_skills_50_59`

Manual examples:
- `run_battle_lab.cmd -Lane npc -Scenario shadow_skill50_anh_thu`
- `run_battle_lab.cmd -Lane npc -Scenario shadow_skill59_loi_nguyen_cuoi_cung`

## Honest Remaining Work

- PORTED/PARTIAL: source rows, effect rows, runtime HP/PP/debuff/heal, and PNG
  checkpoints are verified.
- INTENTIONAL_DEVIATION/GAMEPLAY_FIXED: debuff8/debuff9 consumer behavior
  follows earlier user-approved gameplay decisions, not strict source parity.
- PENDING: decide whether zero-power skill `54` and `55` should become real
  debuff producers in the PC rebuild, or remain visual/status test skills.
- PENDING: original-vs-rebuild pixel compare for Shadow skill frame timing and
  exact MIDP draw order.
- PENDING: full manual feel test in Battle Lab for every Shadow skill after
  user review.

## Next Recommended Step

Move to the seventh skill group `60..69`: source reaudit first, then Battle Lab
integration/smoke, then focused timeline closeout. This group should be treated
carefully because it includes higher-tier follow-up/buff interaction skills
that may touch already-sensitive P7 result flow.
