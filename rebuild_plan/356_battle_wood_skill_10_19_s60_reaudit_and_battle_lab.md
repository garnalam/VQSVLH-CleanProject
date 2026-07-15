# 356 - Battle Wood Skill 10..19 S60 Reaudit And Battle Lab

Date: 2026-07-14

Status: PORTED/PARTIAL / S60 SOURCE REBASELINED / BATTLE LAB INTEGRATED.

Scope:

- Re-read current S60-merged source tables for Wood lane skills `10..19`.
- Update smoke expectations that were still based on older `effect.mid` rows.
- Add focused Battle Lab shortcuts for manual testing each Wood skill.

No gameplay formula was changed in this slice. Runtime already reads the live
tables; this patch updates smoke/audit expectations and Battle Lab entry points.

## Source Rows

Rows are from current:

- `modules/script/decoded/data__script__db.mid.json`
- `modules/script/decoded/data__script__effect.mid.json`
- `modules/script/decoded/data__script__speffect.mid.json`

| Skill | Source role | `aq.c[1]` | Current `effect.mid` path | Status |
| ---: | --- | --- | --- | --- |
| 10 | Low Wood damage | `[1,127,539,100,0,45,0,-1,-1,0]` | `actor21/state0` | PORTED/PARTIAL |
| 11 | Damage + attacker heal 10% | `[1,128,540,90,0,45,0,-1,10,0]` | `actor21/state1 -> actor25/state9 -> speffect10/AH9` | PORTED/PARTIAL |
| 12 | Damage + debuff2 bind | `[1,129,541,50,0,45,2,2,-1,0]` | `actor21/state3 frame-trigger -> speffect6/AH8` | PORTED/PARTIAL |
| 13 | Damage + debuff3 delayed 150% | `[1,130,542,50,1,30,2,3,150,0]` | `actor21/state4`; active body visual also `actor21/state4` | PORTED/PARTIAL |
| 14 | Buff2 reflect/defense | `[1,131,543,0,1,10,1,2,-1,1]` | `actor21/state5` self-side | PORTED/PARTIAL |
| 15 | Buff3 heal over time | `[1,132,544,0,1,10,1,3,-1,1]` | `actor33/state0 -> speffect7/AH9` | PORTED/PARTIAL |
| 16 | Higher Wood damage | `[1,133,545,150,2,30,0,-1,-1,0]` | `actor21/state7` | PORTED/PARTIAL |
| 17 | Damage + attacker heal 40% | `[1,134,546,130,2,30,0,-1,40,0]` | `actor21/state8 -> actor25/state9 -> speffect10/AH9` | PORTED/PARTIAL |
| 18 | High damage + debuff2 bind | `[1,135,547,150,3,15,2,2,-1,0]` | `actor21/state10`; active queue uses `speffect6/AH8` | PORTED/PARTIAL |
| 19 | High damage + debuff3 delayed 200% | `[1,136,548,150,3,15,2,3,200,0]` | `actor21/state11`; active body visual `actor21/state4` | PORTED/PARTIAL |

Important S60 deltas versus older Wood closeout:

- Skill `10`: actor state is now `0`, not `1`.
- Skill `11`: now has an extra chunk `actor25/state9` before `speffect10`.
- Skill `12`: first actor state is now `3`, with frame trigger into
  `speffect6`.
- Skill `13`: actor state is now `4`; debuff3 body visual row is also
  `[0,21,4,-1]`.
- Skill `14`: actor state is now `5`.
- Skill `16`: actor state is now `7`.
- Skill `17`: actor state is now `8` and has the extra `actor25/state9`
  chunk before `speffect10`.
- Skill `18`: actor state is now `10`.
- Skill `19`: actor state is now `11`.

## Smoke Results

Focused timeline suites were re-run to generate before/during/after PNGs and
numeric debug output:

```text
rebuild_game/build/smoke/wood_reaudit_10_19/
```

Latest focused pass:

| Skill | Smoke summary |
| ---: | --- |
| 10 | HP `109 -> 84`, PP `45 -> 44`, damage `25` |
| 11 | player HP `67 -> 69`, enemy HP `109 -> 89`, PP `45 -> 44`, damage `20`, heal `2` |
| 12 | enemy HP `109 -> 96`, PP `45 -> 44`, damage `13`, debuff2 expired after ticks |
| 13 | enemy HP `109 -> 60`, PP `30 -> 29`, damage `13`, storedRaw `24`, delayedDamage `36` |
| 14 | PP `10 -> 9`, defense `100 -> 130`, reflect hit `6`, reflect crit `10`, expired |
| 15 | player HP `67 -> 74`, PP `10 -> 9`, apply heal `6`, tick heal active, expired |
| 16 | enemy HP `109 -> 72`, PP `30 -> 29`, damage `37` |
| 17 | player HP `67 -> 76`, enemy HP `109 -> 77`, PP `30 -> 29`, damage `32`, heal `9` |
| 18 | enemy HP `109 -> 72`, PP `15 -> 14`, damage `37`, debuff2 expired after ticks |
| 19 | enemy HP `109 -> 26`, PP `15 -> 14`, damage `35`, storedRaw `24`, delayedDamage `48` |

## Battle Lab

Focused manual scenarios were added:

```text
run_battle_lab.cmd -Lane npc -Scenario wood_skill10_diep_toan
run_battle_lab.cmd -Lane npc -Scenario wood_skill11_quang_phan
run_battle_lab.cmd -Lane npc -Scenario wood_skill12_dang_phuoc
run_battle_lab.cmd -Lane npc -Scenario wood_skill13_thao_chung
run_battle_lab.cmd -Lane npc -Scenario wood_skill14_dang_chi_bich_luy
run_battle_lab.cmd -Lane npc -Scenario wood_skill15_thao_nguyen_thuat
run_battle_lab.cmd -Lane npc -Scenario wood_skill16_cham_diep_tram
run_battle_lab.cmd -Lane npc -Scenario wood_skill17_diep_chi_an_hue
run_battle_lab.cmd -Lane npc -Scenario wood_skill18_dang_man_trien_nhieu
run_battle_lab.cmd -Lane npc -Scenario wood_skill19_quang_hop_hieu_ung
```

Focused PNG suite:

```text
run_battle_lab_suite_smoke.cmd -Lane npc -Suite wood_skills_10_19
```

## Honest Status

PORTED:

- Current S60 source rows are locked in smoke expectations.
- Core P3/P7 damage/buff/debuff/post-effect logic passes focused timelines.
- Battle Lab can open each Wood skill with the selected row pre-highlighted.

PORTED/PARTIAL:

- Exact original Java ME pixel parity is not claimed.
- The extra `actor25/state9` chunks in skills `11` and `17` are source-proven,
  but the focused timeline still captures the main actor, AH heal, damage, HP
  settle, and post-heal frames rather than a dedicated extra-chunk contact
  sheet.

Next:

- Let manual Battle Lab testing confirm Wood `10..19` visually.
- If accepted, move to Earth lane `20..29` with the same S60 source-first flow.
