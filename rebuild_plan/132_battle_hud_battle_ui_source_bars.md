# 132 Battle HUD Battle UI Source Bars

## Scope

Next visual-debt slice after P17 catch effect: tighten the battle HUD HP/energy
bars using source `game.h` and `/data/ui/battle.ui`.

This slice does not change battle state, damage formula, P7 actor motion, catch,
item, EXP, or pet-switch logic.

## Source Chain

| Source | Finding | Status |
| --- | --- | --- |
| `game.h.a(b,b)` | Loads `/data/ui/battle.ui`, initializes both side HUD data, sets power percent widgets `58/59`, and removes `world.ui`. | PORTED/PARTIAL |
| `game.h.a(b, boolean)` | Player-side HP update writes widgets `41`, `55`, `11` with `#P` percent strings; writes HP text `38`, energy bar `9`, energy text `40`, name `12`, level `13`, element icon `17`. | PORTED/PARTIAL |
| `game.h.b(b, boolean)` | Enemy-side HP update writes widgets `42`, `56`, `14` with `#P`; writes HP text `39`, name `15`, level `16`, element icon `18`; widget `19` is driven by `game.g.a(element, species) == 2` to show the owned-species ball marker. | PORTED/PARTIAL |
| `game.g.a(byte element, int species, byte 2)` / `game.g.a(int, byte)` | Source records known/owned pet species when pets enter party/storage and later queries that collection for the enemy HUD marker. | PORTED/PARTIAL |
| `k.a(Graphics, ak, String, ...)` | If text starts with `#P`, fills `ak.x + 1, ak.y + 1, width * percent / 100 - 1, ak.h - 1`; it is a progress fill, not normal text. | PORTED |
| `/data/ui/battle.ui` | Widget ids/positions are loaded from binary source via `VqsvUiLayout.load("battle.ui")`. | PORTED |

## Rebuild Change

`VqsvBattleRenderer` now draws battle HUD bars through `battle.ui` widget ids:

- Enemy HP layers: `42`, `56`, `14`.
- Player HP layers: `41`, `55`, `11`.
- Player energy layer: `9`.
- Text/icon placement uses battle.ui widgets for name, level, HP text, energy
  text, element icons, power percent, and status slots.
- Enemy owned-species ball marker uses rebuild party/bank state:
  `sourcePets/sourcePetBank` species match -> draw widget `19` cell `101`;
  otherwise the marker region is cleared back to the current battle background
  so unowned species do not show a ball.

The previous renderer had a single hardcoded green/gray HP bar. That was not
source-shaped because source battle UI uses stacked `#P` widgets and widget
colors from `battle.ui`.

## Smoke

Focused checkpoint:

```powershell
java -cp .\build\classes VqsvIntroDemo --smoke-checkpoint battle_hud_battle_ui_source_bars build_intro_demo\battle_hud_battle_ui_source_bars.png
```

Smoke asserts:

- `battle.ui` is loaded from binary and contains required HUD widget ids.
- enemy HP source `#P` layer appears at the battle.ui enemy HP region.
- player HP source `#P` layer appears at the battle.ui player HP region.
- player energy source `#P` renderer path appears at the battle.ui energy
  region using a renderer-only full-energy stress value. Real battle energy/PP
  population remains governed by current battle runtime state and is not claimed
  complete here.
- `battle_bunny_command_ui`: Bunny is not yet owned, so `enemyOwned=false`
  and the ball marker is absent.
- `battle_bunny_owned_marker`: Bunny is seeded in party, so
  `enemyOwned=true` and the ball marker is present.

## Status

| Area | Status | Note |
| --- | --- | --- |
| battle.ui binary widget map | PORTED | Required HUD ids smoke-asserted. |
| HP/energy source `#P` fill semantics | PORTED | Uses source-style x+1/y+1/fill width and widget colors. |
| Energy runtime population | PORTED/PARTIAL | Renderer supports `battle.ui` id `9`; exact source `game.b.O()/S/u()` lifecycle is not audited in this slice. |
| HP animation layer lifecycle | PORTED/PARTIAL | Rebuild exposes current HP percent; exact source animated `Q/d[1]` split over frames remains partial. |
| HUD text/icon placement | PORTED/PARTIAL | Coordinates come from battle.ui; full generic `game.h/ao/af/k` runtime is not ported. |
| Enemy owned-species ball marker | PORTED/PARTIAL | Logical source condition is ported from party/bank state. Visual uses a targeted background clear for unowned because full battle.ui widget state/masking is still partial. |
| Pixel-perfect HUD | PENDING | No original-vs-rebuild pixel comparison. |

## Next

After verification, the next visual debt in `95_battle_visual_parity_marker_hud_p7_audit.md` is battle background source `game.d.c`, then P7 hit/recoil/blood timing.
