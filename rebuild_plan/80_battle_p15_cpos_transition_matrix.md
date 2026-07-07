# 80 Battle P15 Cpos Transition Matrix

Status: AUDIT + CODE SLICE. Scope is only source state 15 transition after a
valid P5 pet switch. It does not claim full `game.d.an` parity for all battle
entry/replacement states.

## Source Facts

| Source | Fact |
| --- | --- |
| `modules/source_code/decoded/decompiled_source_cfr/ae.java:124..141` | `ae.a(InputStream)` reads a big-endian `short[][]`: row count short, then per-row length short, then row shorts. |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java:211..215` | Battle loads `/data/script/cpos.mid` once and calls `ae.a(inputStream)` three times into `game.d.an[0..2]`. |
| `game/d.java:230` | `r = a == 0 ? (b == 1 ? 2 : 0) : 1`; `r` selects the `an` group. |
| `game/d.java:248`, `344`, `411`, `420` | Each `an[r][slot]` row stores frame quads `[actorX, actorY, effectX, effectY]`. |
| `game/d.java:708..716` | Entering source state 15 sets `G = g`, resets `E[G] = 0`, marks the active battle unit, and hides the side marker effect with `b(g,false)`. |
| `game/d.java:1011..1027` | State 15 advances `E[G]`, moves `d[G]` by `an[r][G][E*4]`, `an[r][G][E*4+1]`, and exits after the last frame. |
| `game/h.java:1903..1936` | P5 confirm calls `game.d.a(slot)`. If return is `-1`, UI calls `game.d.a(byte 15)` and reloads `petsetting.ui`/`petstate.ui`. |

## Decoded `cpos.mid`

Decoded source: `modules/script/decoded/data__script__cpos.mid.json`.

| Group | Source selector | Rows | Meaning in current audit |
| ---: | --- | ---: | --- |
| `0` | `a == 0 && b != 1` | 2 | Normal one-enemy/current tutorial battle layout. Elder battle `op32 [0,2]` uses this group. |
| `1` | `a != 0` | 4 | Multi-side/alternate layout. Not used by current P5 elder smoke. |
| `2` | `a == 0 && b == 1` | 2 | Alternate single-enemy layout. Not used by current P5 elder smoke. |

Important rows:

| Group/row | Frame count | First frame | Last frame | Rebuild use |
| --- | ---: | --- | --- | --- |
| `an[0][0]` | 5 | `(400,33,240,81)` | `(177,103,144,85)` | Enemy-side entry row. Not used by P5 player switch. |
| `an[0][1]` | 5 | `(-400,175,-80,200)` | `(70,223,36,206)` | Player-side entry row. Used for P5 switch in current rebuild. |
| `an[2][1]` | 5 | `(-400,175,-80,200)` | `(70,223,36,206)` | Same player row for alternate group 2. |

## Rebuild Mapping

| Source item | Rebuild equivalent | Status |
| --- | --- | --- |
| `ae.a(inputStream)` for `cpos.mid` | `VqsvBattleAnimationTables.cposRow(group,row)` reads three `short[][]` groups from original `script/original/cpos.mid` with `BinaryTables.readShortRows`. | PORTED |
| `r` selector | `SourceBattleRuntime.playerSwitchCposGroup()` mirrors `a == 0 ? (b == 1 ? 2 : 0) : 1` using battle mode array. | PORTED/PARTIAL: current code assumes rebuild `battleMode[0] == source a`, `battleMode[1] == source b`, which matches audited elder battle `op32 [0,2]`. |
| `G = g` for P5 player switch | Rebuild uses player row `1` for group `0/2`; group `1` fallback row `2`. | PORTED/PARTIAL: correct for current elder P5 path; broader multi-unit player slot mapping still needs audit before claiming full parity. |
| `E[G]` frame cursor | `playerSwitchTicks`, clamped to row frame count. | PORTED |
| Actor move by `an[r][G][E*4..+1]` | Rebuild applies `playerOffset = currentXY - finalXY` to existing renderer base position. | PORTED/PARTIAL: motion shape is source-backed; absolute MIDP anchor differs because rebuild renderer has its own battle sprite anchor. |
| Exit after final frame | Rebuild stays in P15 until `playerSwitchTicks >= frameCount`, then enters P1. | PORTED |

## Smoke

| PNG | Expected |
| --- | --- |
| `rebuild_game/build_intro_demo/battle_p5_switch_transition_cpos_start.png` | P15 start; switched pet may be offscreen left because `an[0][1]` frame 0 starts at x `-400`. |
| `rebuild_game/build_intro_demo/battle_p5_switch_transition_cpos_mid.png` | P15 with switched pet entering/reached battle position using `cpos.mid`. |
| `rebuild_game/build_intro_demo/battle_p5_voluntary_switch_success_cpos_after.png` | Flow exits P15 to P1 with switched active pet. |
| `rebuild_game/build_intro_demo/battle_p5_forced_replacement_success_cpos_after.png` | Forced replacement path still exits correctly. |

## Remaining Gaps

- Full `game.d.an` parity for P0 entry, enemy P15 replacement, group `1`
  multi-unit layout, and side marker `al[]` effect movement is still PARTIAL.
- Absolute pixel anchoring is not guaranteed MIDP-perfect because the rebuild
  renderer draws battle sprites from its own anchor boxes. The movement curve
  is source-backed by `cpos.mid`; exact MIDP sprite anchor compare remains a
  visual QA task.
- P15 source branch after completion has additional turn-vector behavior
  (`k`, `v`, `i`, `d(b)`) beyond the current P5 switch route. Current rebuild
  routes P15 -> P1 at the same practical point for the tested elder path.
