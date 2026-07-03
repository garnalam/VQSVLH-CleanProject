# Scene 1 Room0 Player And World UI Correction

Reason: user caught two mismatches against original screenshots:

1. Neil/player disappeared after the "10 years later" transition.
2. World UI icons at the lower screen corners were missing.

## Source Facts

- `scene_1 room0 group0 record 3` is `op8 [199,218]`.
- `game.c.java:554-560` handles `op8` by setting player `game.g` position,
  resetting selected actor `game.k.u`, and setting player action/direction.
- `game.g.java:119-145` initializes the player with sprite index `0` when
  `t == -1`; this is Neil's world sprite path.
- `game.h.java:90` loads `/data/ui/world.ui`.
- `modules/ui/decoded/data__ui__world.ui.json` confirms lower-corner visual
  widgets, including id `7` at `(1,303)` and id `5` at `(222,303)`.

## Current Rebuild Status

- `PORTED/APPROX`: Neil/player is now rendered from source sprite index `0`
  and positioned by `setPlayerPositionApprox(...)` for `op8`.
- `PORTED/APPROX`: lower-corner world UI widgets `7` and `5` are now rendered
  from source UI sprite resource `257` (`spr_257_all(r)`) using cells `167`
  and `68`, matching `world.ui` positions `(1,303)` and `(222,303)`.
- `PORTED/APPROX`: room0 player direction now preserves the transition
  direction from room3 `op22` (`w=2`) when room0 `op8 [199,218]` places Neil.
- `PORTED/APPROX`: actor/NPC constructors now preserve source row direction,
  and `op2`/`setActive` direction overrides refresh the displayed animation.

## Verification After Fix

- Build passed:
  `powershell -ExecutionPolicy Bypass -File .\build.ps1`
- Smoke images:
  - `build_intro_demo/scene1_fix_ui_dir_room0_5200.png`
  - `build_intro_demo/scene1_fix_ui_dir_room0_5850.png`
  - `build_intro_demo/scene1_fix_ui_dir_room0_6200.png`
- Final jar check passed:
  `java "-Dvqsv.modules=..\modules" -jar ".\build\libs\vqsv-rebuild-skeleton.jar" --check`

Remaining honesty note:

- This is still a minimal source-backed UI cell renderer, not the full
  `ao/af/k/m` UI runtime for all widgets and modes.

## Rule Going Forward

Do not re-add world UI by eye. The next fix must parse `world.ui` original bytes
using `ao.java` field order and render `m.java` payloads with the correct
alignment/mode. Until then, world UI remains `MISSING`, not `APPROX`.
