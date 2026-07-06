# Battle Entry Snapshot Matrix

Date: 2026-07-06

Scope: khóa dữ liệu vào trận trước khi port tiếp battle. Mục tiêu là biết chắc mỗi battle hiện tại lấy gì từ event/source/table, UI đầu tiên là gì, và rebuild đã dùng dữ liệu đó ra sao.

Rule: không tự nối battle bằng cảm tính. Battle entry phải đi theo:

```text
scene event opcode -> SourceBattleRuntime/game.d setup -> BattleUnit/game.b data
-> game.h UI -> result branch
```

## Source References

- Event JSON: `modules/event/decoded/data__event__scene_1.mid.json`
- Battle source: `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- UI source: `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- Unit source: `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- Species/skill rows: `modules/script/decoded/data__script__db.mid.json`
- Current rebuild runtime: `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- Current renderer: `rebuild_game/src/main/java/VqsvBattleRenderer.java`

## Source Entry Records

| Battle | Event record source | Opcode chain | Meaning | Rebuild status |
|---|---|---|---|---|
| Sophie/kidnapping | scene 1 room 3 group 0 records 72..76 | `op67 [56]`, `op37 [5,20,4]`, `op52 [1,1]`, `op32 [0,2]`, `op47 [78,78,0]` | actor 56 starts battle; enemy species 5 level 20 nature 4; result branch 78. | PORTED/PARTIAL |
| Bunny tutorial | scene 1 room 1 group 0 records 1..5 | `op37 [34,5,1]`, `op52 [0,1]`, `op32 [0,0]`, `op47 [12,0,0]` | enemy Bunny species 34 level 5 nature 1; tutorial capture path; current success result kept as `-1`. | PORTED/PARTIAL |
| Elder battle | scene 1 room 0 group 6 records 5..8 | `op67 [52]`, `op37 [68,5,1]`, `op32 [0,2]`, `op47 [10,10,0]` | actor 52 elder battle; enemy species 68 level 5 nature 1; win branch 10 then reward/free-world. | PORTED/PARTIAL |

Other battle entries exist later in scene 1, but are not part of the current intro-to-elder closeout slice yet.

## Species Snapshot From `aq.c[0]`

Raw rows below are from `data__script__db.mid.json`, group 0.

| Entity | Species id | Encounter level/nature | Raw row | Important fields used now |
|---|---:|---|---|---|
| Sophie/kidnapping enemy | 5 | `[5,20,4]` | `[15,0,0,3,4,25,20,5,4,5,3,3,20,2,3,2,0,91,3,-1,-1,-1,1]` | element 0, quality row field 3, sprite 91, learn group 3, relation class 1 |
| Player smoke pet | 17 | smoke pet level 7 nature/quality shape from `SourcePetState(0,17,7,3,2,10,45)` | `[27,1,1,2,4,20,16,2,4,5,-1,1,20,4,5,4,0,103,2,18,1,1,0]` | element 1, sprite 103, learn group 2, relation class 0 |
| Bunny enemy | 34 | `[34,5,1]` | `[44,2,0,1,4,24,18,2,4,6,2,3,20,0,4,4,0,120,2,-1,-1,-1,0]` | element 2, sprite 120, learn group 2, relation class 0 |
| Elder enemy | 68 | `[68,5,1]` | `[78,4,0,2,2,26,18,6,4,4,4,3,20,3,4,4,0,154,2,69,0,1,0]` | element 4, sprite 154, learn group 2, relation class 0 |

Smoke-calculated HP snapshots from current `BattleUnit`:

| Battle smoke | Player HP | Enemy HP | Notes |
|---|---:|---:|---|
| Bunny P3 | 134/134 | 104/104 | player pet species 17 seeded for P3 smoke |
| Elder P3 | 134/134 | 109/109 | player pet species 17 seeded |
| Sophie route | 120/120 at entry fallback | 473/473 | current rebuild uses Neil fallback because this slice predates pet ownership |

## Skill Snapshot For Current Player Smoke Pet

Current smoke pet is created with skill ids 10 and 45. Rows from `aq.c[1]`:

| Skill id | Raw row | Current P3 use |
|---:|---|---|
| 10 | `[1,127,539,100,0,45,0,-1,-1,0]` | name/description via chs text ids 127/539; max PP 45; target side 0 |
| 45 | `[4,162,574,0,1,10,1,9,-1,1]` | name/description via chs text ids 162/574; max PP 10; target side 1 |

Current rebuild now initializes default `SourcePetState` skill PP from `aq.c[1][skill][5]`, instead of treating PP 0 as missing forever. This matters because P3 no-PP warning needs PP 0 to remain a real state.

## UI Entry Snapshot

| Source state | Source method | UI file | Current rebuild |
|---|---|---|---|
| Battle entry HUD | `game.h.a(b,b)` | `/data/ui/battle.ui` | Source-shaped renderer, PARTIAL widget parity |
| Command | `game.h` battle command handling | `/data/ui/battle.ui` | PORTED/PARTIAL: one cursor, key/click |
| Skill list P3 | `game.d case 3 -> game.h.e(b)`; tick `game.h.f(b)` | `/data/ui/choiceskill.ui` | PORTED in this slice: list rows, PP labels, desc, confirm/back/no-PP warning |
| Catch P21 | `game.d case 21 -> game.h.ah/ai` | `/data/ui/choice.ui` | PORTED/PARTIAL |
| Warning | multiple `game.h` branches | `/data/ui/msgwarm.ui` | PARTIAL |

## Current Result Branch Smoke

| Smoke checkpoint | Expected | Current result |
|---|---|---|
| `route_sophie_after_battle_branch` | result 0 -> branch 78 | PASS |
| `route_bunny_after_battle_task` | result -1 -> return task; state `[1,0,1]` and `[1,1,0]` marked | PASS |
| `route_elder_after_battle_reward_state` | result 0 -> branch 10; reward/state `[1,0,6]=3` | PASS |

## P3 Port Status

PORTED:

- P20 command "Chiến đấu" now opens P3 instead of auto-skipping.
- P3 skill list is built from active player `BattleUnit.skillIds`/`skillPp`, equivalent to source `game.b.z[]/y[]`.
- UI uses `choiceskill.ui` coordinates:
  - panel cell 91 at `(41,68)`;
  - title text ids/source labels `Kỹ năng`, `Số lần`;
  - rows at y `96,111,126,141,156`;
  - skill name width around 72 and PP width around 36;
  - desc area id 53 around `(57,180)`;
  - action/back labels at bottom.
- Up/down changes selected skill.
- Click row confirms selected skill.
- Back returns to P20.
- Confirm with PP > 0 calls `BattleUnit.selectSkill`, consumes PP, then bridges to P2/P7.
- Confirm with PP 0 opens `msgwarm.ui` warning `Kỹ năng giá trị chưa đủ`.

PARTIAL:

- P6/target select is not ported yet. P3 records targetSide in trace, but current battle still bridges to existing P2/P7.
- P7 animation/effect is still source-shaped damage resolve, not script-driven `pos/cpos/effect/speffect/blood/bufDebuf`.
- `choiceskill.ui` is rendered by battle-specific renderer, not full generic `game.h` widget runtime.

## Required Smoke Images For This Slice

- `rebuild_game/build_intro_demo/battle_bunny_p3_skill_list.png`
- `rebuild_game/build_intro_demo/battle_elder_p3_skill_list.png`
- `rebuild_game/build_intro_demo/battle_skill_no_pp_warning.png`
- `rebuild_game/build_intro_demo/route_bunny_after_p3.png`
- `rebuild_game/build_intro_demo/route_elder_after_p3.png`
- `rebuild_game/build_intro_demo/route_sophie_after_p3.png`

## Next Step

Next safe target: port target select/P6 bridge for player skills.

Do not start P7 animation table work before target selection is source-backed, because animation depends on exact attacker, target slot, target side, and chosen skill.
