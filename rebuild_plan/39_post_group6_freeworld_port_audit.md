# Post Group6 Free-World Port Audit

Scope: runtime code added after `scene_1 room0 group6` so the player can
move freely and trigger source-backed transitions instead of auto-running the
next event.

## Source Evidence

- `modules/event/decoded/data__event__scene_1.mid.json`
- `modules/source_code/decoded/decompiled_source_cfr/game/a.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/c.java`
- Prior audit: `rebuild_plan/38_scene1_post_group6_flow_audit.md`

## Implemented

### Room0 Free-World After Tutorial

`Room0PostGroup6FreeWorld` now stays active as a free-world loop after
`state[1,0,6]=3`.

Implemented source-backed type-1 transitions:

| From | Actor | Source row | Target | Status |
|---|---:|---|---|---|
| scene1 room0 | 31 | `[1,223,1,200,318,1,1,2,1,2,2]` | scene1 room2 actor2 | PORTED/APPROX |
| scene1 room0 | 30 | `[1,223,3,408,273,1,1,3,1,1,37]` | scene1 room1 actor37 | PORTED/APPROX |

Direction gate uses source `game.a.R = {2,3,0,1}`, so actor31 uses
required direction `0`, actor30 uses required direction `1`.

### Scene1 Room2 Loader

Added `loadScene1Room2()`:

- map id from room2 `unknown_ab = 6`
- actor table copied from event JSON, 30 actors
- current room state tracked as `[1,2]`

### Room2 Entry Tutorial

Room2 group3 is source-backed:

- `op15 [1,0,6]`
- `op40 "Nhấn nút 0 tra xét tiểu địa đồ, nhấn nút 1 tra xét nhiệm vụ."`
- `op14`

This runs only after the player manually transitions into room2, not directly
after group6.

### Room2 Back Transition

Implemented source-backed transition:

| From | Actor | Source row | Target | Status |
|---|---:|---|---|---|
| scene1 room2 | 2 | `[1,223,0,120,23,1,1,0,1,0,31]` | scene1 room0 actor31 | PORTED/APPROX |

## Pending, Not Faked

- Room0 building doors actors 3/4/5 to scene11 rooms 5/6/4: PENDING.
- Room0 Dodo side quest actor35, groups 7/8/9: PENDING.
- Room2 actor3 transition to scene1 room3 actor24: PENDING until room3
  free-world loader is audited.
- Exact `game.k` transition state/camera placement remains APPROX; current
  placement uses target actor tile alignment.

## Smoke Results

Commands were run from `rebuild_game` with `-Dvqsv.modules=..\modules`.

- `--check`: PASS.
- `post_group6_room2_entry_tip.png`: PASS, room `[1,2]`, `state106=3`,
  `state123=0`, source tip visible.
- `post_group6_room2_after_tip.png`: PASS, room `[1,2]`, `state123=3`,
  text closed.
- `post_group6_room0_back_from_room2.png`: PASS, room `[1,0]` after returning
  through room2 actor2.

## Font Note

The room2 tip originally exposed a rebuild text bug: the code had copied a
mojibake literal instead of the Unicode text from event JSON. That was fixed.
The bitmap font still lacks some Vietnamese glyphs such as `ú`, `ồ`, and `ụ`.
The renderer now falls back to the nearest base glyph only when the exact glyph
is absent. This is readable but still `APPROX`, not pixel-perfect MIDP text.

## Next Recommended Slice

Audit and port `scene_1 room2` main-route flow:

- group0 zone `[47,144,144,16]`, gated by `op15 [1,2,3]`
- Lanni dialog/reward `op36`
- optional battles group1/group2
- actor3 transition toward `scene_1 room3`
