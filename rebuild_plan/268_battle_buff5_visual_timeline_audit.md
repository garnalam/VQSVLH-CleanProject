# Battle Buff5 Vo Hinh Visual Timeline Audit

Date: 2026-07-13

Scope: source-backed visual audit for buff5 `Vo hinh`, producer skill `34`,
covering both cast-time `effect.mid[34]` and P12/P13 active buff body visual.

Status: PORTED/PARTIAL.

- PORTED: source rows, cast AH type7 trigger, buff apply, active queue row, sprite id,
  duration/icon transition.
- PORTED/PARTIAL: rebuild timeline is source-row backed, but there is no original-client
  frame capture, so this is not pixel-perfect claimed.

## Source Chain

### Producer Skill

| Source | Evidence |
| --- | --- |
| `modules/script/decoded/data__script__db.mid.json` | `aq.c[1][34] = [3,151,563,0,1,10,1,5,-1,1]`. Skill34 is no-damage/effect-mode route applying buff id `5`. |
| `modules/script/decoded/data__script__db.mid.json` | `aq.c[6][5] = [338,353,3,30,-1]`. Buff5 duration `3`, reflect chance `30`. |
| `modules/script/decoded/data__script__effect.mid.json` | `effect.mid[34] = [0,1,4,0,-1,-1,0]`. Skill34 cast visual uses speffect id `4`. |
| `modules/script/decoded/data__script__speffect.mid.json` | `speffect[4] = [7,0,4,2,9,10,11,10]`. AH type `7`. |
| `modules/source_code/decoded/decompiled_source_cfr/ah.java` | AH type7 creates a clone of the actor sprite, scales it to width `9/10` and height `11/10`, then alternates scaled clone/original by `t[0] / t[2] % 2`. |

Interpretation: cast-time `Vo hinh` does have actor-image change. It is not a full
hide/alpha-zero in source; it is an AH type7 actor clone scale/toggle effect around
the actor.

### Active Buff Visual

| Source | Evidence |
| --- | --- |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java` | `ai = {{3,5,13}, {0,1,2,3,8,9,10}}`; buff bank id `5` is visual-gated. |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java` | Active queue skips non-visual ids; if bank/id matches `ai`, it starts visual instead of applying immediately. |
| `modules/script/original/bufDebuf.mid` | Parsed rows: `ar[0][5] = 6`, `ap[6] = [0,23,0,-1]`. |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Source effect id `23` maps to actor sprite `265`; state `0`. |

Interpretation: P12/P13 active buff5 visual is a type0 actor action using sprite
`265`, not a speffect row and not an alpha/hidden-base sprite effect.

## Smoke Timeline

Suite:

```cmd
java "-Dvqsv.modules=..\modules" -cp build/classes VqsvIntroDemo --smoke-suite battle_buff5_visual_timeline build_intro_demo\battle_buff5_visual_timeline
```

Result: PASS, `6/6`.

| Step | Checkpoint | PNG | What it locks |
| --- | --- | --- | --- |
| Cast start | `battle_phase10b_p7_type7_skill34_start` | `rebuild_game/build_intro_demo/battle_buff5_visual_timeline/battle_phase10b_p7_type7_skill34_start.png` | Skill34 enters P7 cast timeline from `effect.mid[34]`. |
| Cast overlay | `battle_phase10b_p7_type7_skill34_overlay` | `rebuild_game/build_intro_demo/battle_buff5_visual_timeline/battle_phase10b_p7_type7_skill34_overlay.png` | AH type7 visible, `speffect=4`, row length valid. |
| Cast after | `battle_phase10b_p7_type7_skill34_after` | `rebuild_game/build_intro_demo/battle_buff5_visual_timeline/battle_phase10b_p7_type7_skill34_after.png` | P7 skill34 visual resolves. |
| Active start | `battle_status_buff5_p12_body_visual_start` | `rebuild_game/build_intro_demo/battle_buff5_visual_timeline/battle_status_buff5_p12_body_visual_start.png` | P13 starts active visual, `row=[0,23,0,-1]`, sprite `265`, state `0`, duration still `3`. |
| Active mid | `battle_status_buff5_p12_body_visual_mid` | `rebuild_game/build_intro_demo/battle_buff5_visual_timeline/battle_status_buff5_p12_body_visual_mid.png` | Actor effect remains visible in early body-visual window, duration still `3`. |
| Active after | `battle_status_buff5_p12_body_visual_after` | `rebuild_game/build_intro_demo/battle_buff5_visual_timeline/battle_status_buff5_p12_body_visual_after.png` | Actor effect cleared; active queue applies buff tick, duration `3 -> 2`, icon duration cell `137 -> 136`. |

## Numeric / Trace Results

- Cast visual:
  - skill id `34`
  - `effect.mid[34] = [0,1,4,0,-1,-1,0]`
  - `speffect = 4`
  - `speffect[4] = [7,0,4,2,9,10,11,10]`
  - AH type `7` actor clone scale/toggle.
- Active visual:
  - visual gate includes buff id `5`.
  - `bufDebuf`: `ar[0][5] = 6`, `ap[6] = [0,23,0,-1]`.
  - actor source effect id `23`, sprite `265`, state `0`.
  - start/mid duration remains `3`.
  - after active visual, source tick applies duration `3 -> 2`; HUD icon remains `17`, duration cell becomes `136`.

## Re-Audit Notes

| Question | Answer |
| --- | --- |
| Does source hide the base pet sprite fully? | UNKNOWN/PENDING. No alpha-zero or full hide path was found for buff5 itself. AH type7 toggles a transformed actor clone/original during cast. |
| Does source require body visual after buff is active? | Yes. `game.d.ai[0]` includes id `5`, and `bufDebuf.mid` maps it to `[0,23,0,-1]`. |
| Is P12/P13 body visual a speffect? | No. It is a type0 actor action row. |
| Is the rebuild pixel-perfect? | No claim. Without original-client capture, this is source-row/timeline backed only. |
| Did this change release behavior? | No gameplay behavior change; only smoke coverage and audit documentation were added. |

Next recommended step: continue roadmap with buff6 `Kien nhan` source audit before code.
