# Scene 1 Room 0 Group 6 Elder Battle Audit

Scope: `scene_1` room0 group6, records 0..21, manual rebuild path in
`rebuild_game/src/main/java/VqsvIntroDemo.java`.

## Source Evidence

- Event JSON: `modules/event/decoded/data__event__scene_1.mid.json`
- Source VM: `modules/source_code/decoded/decompiled_source_cfr/game/c.java`
- Item/reward DB: `modules/script/decoded/data__script__db.mid.json`
- Text DB: `modules/script/decoded/data__script__chs.mid.json`

## Record Matrix

| Rec | Opcode | Args | Rebuild status |
|---:|---|---|---|
| 0 | 15 | `[1,0,3]` | PORTED: waits for group3 complete. |
| 1 | 8 | `[199,218]` | PORTED/APPROX: places player at source coordinates and centers camera. |
| 2 | 7 | actor `-1`, state `0`, action `2` | PORTED/APPROX: player animation/action wait approximated. |
| 3 | 9 | `[1,0,0,0,0,0]` | PORTED/APPROX: routed through shared source `op9` helper; id 1 maps to rebuild fade-in. |
| 4 | 4 | elder dialog | PORTED/APPROX: current dialog renderer. |
| 5 | 67 | `[52]` | PORTED: sets battle actor/event actor. |
| 6 | 37 | `[68,5,1]` | PORTED/APPROX: source battle setup captured as species 68, level 5, nature 1; full `game.d` battle engine pending. |
| 7 | 32 | `[0,2]` | STUB/APPROX: source-shaped battle slice/transition, not state 12 `game.d` runtime. |
| 8 | 47 | `[10,10,0]` | STUB/APPROX: forced success branch to reward path; branch table preserved. |
| 9 | 4 | elder reward dialog | PORTED/APPROX. |
| 10 | 31 | `[0,0,500]` | PORTED/APPROX: money +500 tracked in rebuild state. |
| 11 | 17 | `[0,4,10]` | PORTED/APPROX: adds item 4 `Banh Sandwich` x10. |
| 12 | 17 | `[0,11,2]` | PORTED/APPROX: adds item 11 `Sinh menh thach` x2. |
| 13 | 19 | `[5,1]` | PORTED/APPROX: stores structured reward id 5 from `aq.c[5][5]=[300,47,308]` and mirrors `game.g.i(id)` unlock path; full inventory UI pending. |
| 14 | 4 | elder record-book dialog | PORTED/APPROX. |
| 15 | 4 | elder Abra/Bich Thuy dialog | PORTED/APPROX. |
| 16 | 4 | Neil confirm dialog | PORTED/APPROX. |
| 17 | 23 | `[1,0,4]` | PORTED: marks event state complete. |
| 18 | 23 | `[1,0,5]` | PORTED: marks event state complete. |
| 19 | 45 | `[2]` task text | PORTED/APPROX: task tip shown. |
| 20 | 40 | free-world notice | PORTED/APPROX: openbox shown. |
| 21 | 14 | complete group6 | PORTED: sets state `[1,0,6]=3`. |

## Smoke Results

Commands were run from `rebuild_game` with `-Dvqsv.modules=..\modules`.

- `--check`: PASS.
- `room0_group6_start.png`: PASS state chain, but visual is black because it captures the battle/fade stub moment.
- `room0_group6_after_rewards.png`: PASS, `state103=3`, `state106=3`, `sourcePets=1`, `money=500`.
- `room0_group6_after_rewards_after_patch2.png`: PASS, `state103=3`, `state106=3`, `sourcePets=1`, `money=500`.
- `post_group6_room2_entry_tip_after_patch.png`: PASS, room `[1,2]`, `state106=3`, room2 tip visible.

## Current Truth

- `op35` choice UI is no longer a plain TextBox. It uses source `option.ui`
  coordinates/cells because `game.c` calls `game.h.a(aa,Z,...)`, and for
  group3 `[2,0]` means `aa=0` -> `/data/ui/option.ui`. It supports keyboard/click selection. It is still
  PARTIAL because the full `game.h` + `ao/al` UI object stack is not ported.
- `op9` now goes through a shared helper matching the source branch ids used
  here and in group3. Ids 1/2/10/12/13/16 are handled at rebuild level; ids
  14/15/17 remain documented as pending paths.
- `op19` now stores reward metadata and source side-effect class. For id 5,
  the source path is `game.c case 19 -> game.g.d(5,1) == -1 -> game.g.i(5)`,
  not a normal stackable bag item.
- Elder battle is not full `game.d`. The rebuild shows a source-backed battle
  slice with species/level/nature and keeps the original branch result, but
  command menu, turn order, damage, AI, animation scripts, EXP, and return
  state are still pending.

## Remaining Work

- PENDING: real `game.d` elder battle engine for op37/op32/op47.
- PENDING: exact source animation semantics for op7.
- PARTIAL: exact source effect renderer for op9.
- PARTIAL: full `game.g` special inventory UI/use semantics for op19.
