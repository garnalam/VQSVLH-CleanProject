# 362 - Battle Wind Skill 60..69 S60 Reaudit And Battle Lab

Date: 2026-07-15

## Scope

This audit covers the seventh skill group, family `6` skills `60..69`, after
the S60 resource/table merge.

Rules used:
- Source first: source rows are read from `aq.c[1][skillId]` through
  `VqsvBattleTables.skill(id)`.
- Visual chain is read from `effect.mid[skillId]` through
  `VqsvBattleAnimationTables.effectRow(id)`.
- Runtime behavior is checked against current bytecode-backed rebuild logic,
  not only the surface table row.
- No pixel-perfect claim: current checks verify source row, effect row,
  runtime HP/PP/status/copy/follow-up data, and PNG smoke. Original-vs-rebuild
  frame compare is still pending.

## Easy Mechanism Summary

Wind skills `60..69` mix direct wind damage, speed-down, self buffing,
beneficial-status steal, double-action setup, and follow-up attacks:

- `60 Phong nhan`: direct Wind damage using actor effect `26 -> sprite268`.
- `61 Phong ap`: damage plus `debuff5` Cham Chap, reducing target speed.
- `62 Thuan phong`: damage plus self `buff10` Man Luc.
- `63 Long quyen`: direct damage with a source follow-up roll. If the q()
  follow-up roll passes, battle routes back to `P2` for another action.
- `64 Nghich Phong Doat`: zero-power `buff11` Thau Thu path. It copies a
  selected beneficial buff from the target, clears donor ownership, applies
  buff11 to the attacker, and skips damage.
- `65 Vo Liet Thuat`: zero-power `buff12` Gia Toc producer. It applies K12=1;
  active queue later promotes K12 for follow-up behavior.
- `66 Yen Hoi Thiem`: stronger direct Wind damage.
- `67 Phong Chi Tuyen Qua`: source oddity. The S60 row advertises
  `effectMode=2,effectId=5,chance=5`, but current bytecode path treats skill67
  as `BYTECODE_DEFAULT_RAW_DAMAGE`, ignores effectId, and applies no debuff5.
- `68 Phong Chi Tu Hau`: damage plus self `buff10` Man Luc.
- `69 Phi Yen Hoan Sao`: stronger direct damage with source follow-up roll,
  like skill63 but chance param is `8`.

## Source Matrix

| Skill | Name | Source row | effect.mid row | Runtime status |
| --- | --- | --- | --- | --- |
| 60 | Phong nhan | `[6,177,589,100,0,45,0,-1,-1,0]` | `[0,0,26,0,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 61 | Phong ap | `[6,178,590,80,0,45,2,5,5,0]` | `[0,0,26,1,-1,-1,0, 0,1,11,0,-1,-1,0]` | PORTED/PARTIAL debuff5 producer pass |
| 62 | Thuan phong | `[6,179,591,80,0,45,1,10,5,0]` | `[0,0,26,2,0,-1,0, 0,1,0,0,-1,-1,0, 1,1,15,0,-1,-1,0]` | PORTED/PARTIAL buff10 producer pass |
| 63 | Long quyen | `[6,180,592,100,1,30,0,-1,5,0]` | `[0,0,26,3,-1,-1,0]` | PORTED/PARTIAL follow-up pass/fail asserted |
| 64 | Nghich Phong Doat | `[6,181,593,0,1,10,1,11,-1,0]` | `[0,0,34,0,0,-1,0, 1,1,18,0,-1,-1,0, 1,1,15,0,-1,-1,0]` | PORTED/PARTIAL buff-copy closeout pass |
| 65 | Vo Liet Thuat | `[6,182,594,0,1,10,1,12,-1,1]` | `[0,0,32,0,0,-1,0, 1,1,16,0,-1,-1,0, 1,1,15,0,-1,-1,0]` | PORTED/PARTIAL buff12 K12=1 producer pass |
| 66 | Yen Hoi Thiem | `[6,183,595,150,2,30,0,-1,-1,0]` | `[0,0,26,4,-1,-1,0]` | PORTED/PARTIAL closeout smoke pass |
| 67 | Phong Chi Tuyen Qua | `[6,184,596,110,2,30,2,5,5,0]` | `[0,0,26,5,-1,-1,0, 0,1,11,0,-1,-1,0]` | PORTED/PARTIAL source oddity: raw/no debuff |
| 68 | Phong Chi Tu Hau | `[6,185,597,110,3,15,1,10,5,0]` | `[0,0,26,6,-1,-1,0, 0,1,0,0,-1,-1,0, 1,1,15,0,-1,-1,0]` | PORTED/PARTIAL buff10 producer pass |
| 69 | Phi Yen Hoan Sao | `[6,186,598,150,3,15,0,-1,8,0]` | `[0,0,26,7,-1,-1,0]` | PORTED/PARTIAL follow-up pass/fail asserted |

## Smoke Results

Focused closeout smoke:
- `battle_wind_skills_60_69_closeout`: pass.
- skill60: damage `23`, enemy HP `109->86`, PP `45->44`.
- skill61: damage `20`, enemy HP `109->89`, PP `45->44`, debuff5 active.
- skill62: damage `23`, enemy HP `109->86`, PP `45->44`, buff10 active.
- skill63: damage `25`, forced follow-up fail in main timeline; separate
  forced follow-up pass reaches `P2`.
- skill64: no damage, PP `10->9`, copied target buff2 to player, cleared donor,
  applied buff11.
- skill65: no damage, PP `10->9`, buff12 active with K12=1.
- skill66: damage `37`, enemy HP `109->72`, PP `30->29`.
- skill67: damage `25`, no debuff5 by bytecode source path.
- skill68: damage `25`, enemy HP `109->84`, PP `15->14`, buff10 active.
- skill69: damage `37`, forced follow-up fail in main timeline; separate
  forced follow-up pass reaches `P2`.

PNG output roots:
- `rebuild_game/build_intro_demo/wind_verify/skill60_69`
- `rebuild_game/build_intro_demo/battle_lab_suites/npc/wind_skills_60_69`

Each main skill has:
- `battle_skillXX_<slug>_timeline_before.png`
- `battle_skillXX_<slug>_timeline_effect_start.png`
- `battle_skillXX_<slug>_timeline_result.png`

Follow-up pass snapshots:
- `battle_skill63_long_quyen_followup_pass.png`
- `battle_skill69_phi_yen_hoan_sao_followup_pass.png`

## Battle Lab

New manual scenarios:
- `wind_skill60_phong_nhan`
- `wind_skill61_phong_ap`
- `wind_skill62_thuan_phong`
- `wind_skill63_long_quyen`
- `wind_skill64_nghich_phong_doat`
- `wind_skill65_vo_liet_thuat`
- `wind_skill66_yen_hoi_thiem`
- `wind_skill67_phong_chi_tuyen_qua`
- `wind_skill68_phong_chi_tu_hau`
- `wind_skill69_phi_yen_hoan_sao`

Suite:
- `run_battle_lab_suite_smoke.cmd -Lane npc -Suite wind_skills_60_69`

Manual examples:
- `run_battle_lab.cmd -Lane npc -Scenario wind_skill60_phong_nhan`
- `run_battle_lab.cmd -Lane npc -Scenario wind_skill69_phi_yen_hoan_sao`

## Honest Remaining Work

- PORTED/PARTIAL: source rows, effect rows, runtime HP/PP/status/copy/follow-up,
  and PNG checkpoints are verified.
- SOURCE_ODDITY: skill67 row advertises debuff5, but current bytecode-backed
  damage path ignores effectId and applies no debuff. Do not "fix" this without
  a separate source proof that S60 changed executable logic, not only data.
- PENDING: original-vs-rebuild pixel compare for Wind skill frame timing and
  exact MIDP draw order.
- PENDING: deeper manual feel test for buff12 follow-up chaining after active
  queue promotion in Battle Lab.

## Next Recommended Step

With visible skill groups `0..69` now Battle Lab integrated, the next step is a
closeout audit across all skill groups:

1. Verify every group suite exists in Battle Lab and appears in `npc/all`.
2. Run all skill group suites once after a fresh build.
3. Create a compact matrix for remaining non-pixel-perfect items:
   skill67 source oddity, zero-power status producers, original frame compare,
   and any S60 asset-specific visual mismatch found by manual testing.
