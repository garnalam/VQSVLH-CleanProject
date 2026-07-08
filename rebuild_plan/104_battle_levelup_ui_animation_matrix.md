# 104 Battle LevelUp UI Animation Matrix

Status: SOURCE AUDIT + SMALL REBUILD FIX.

Scope: battle win EXP / level-up overlay only. This does not cover the later
evolution notice/UI path from `game.k.H/L/I`.

## Source Facts

Files read:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/m.java`
- `modules/source_code/decoded/decompiled_source_cfr/af.java`
- `modules/source_code/decoded/decompiled_source_cfr/k.java`
- `modules/ui/decoded/data__ui__levelUp.ui.json`

## State / UI Matrix

| Source | Behavior | Rebuild status |
| --- | --- | --- |
| `game.d` state `22` entry | Calls `S.an()`. | `PORTED/PARTIAL` in `SourceBattleRuntime.tickWinExpLevelUp()`. |
| `game.h.an()` | Captures old stats `c[1..4]`, calls `b.v()` level-up, opens `/data/ui/levelUp.ui` with sprite `257`. | `PORTED/PARTIAL`. |
| `game.h.an()` widgets `19..22` | Old stat values. | `PORTED/PARTIAL`. |
| `game.h.an()` widgets `31..34` | New stat values after `b.v()`. | `PORTED/PARTIAL`. |
| `game.h.an()` widget `51` | Text `"Có thể học tập kỹ năng mới"` when a learn-skill candidate exists. | `PORTED/PARTIAL`, text is Unicode-safe via `VqsvText`. |
| `game.h.an()` widget `38` | Pet/species name from `aq.c[0][species][0]`. | `PORTED/PARTIAL`. |
| `game.h.an()` widget `40` | Level text after level-up. | `PORTED/PARTIAL`. |
| `game.h.an()` widget `10` | Creates `new m()`, sets `m.a = 3`, state/cell `0`, sprite `v1.C`. | `PORTED/PARTIAL`; rebuild now animates sprite state `0` cursor instead of fixed frame `0`. |
| `k.java` text widget draw | Uses `ae.a(...)` with per-widget offset `n[0]/n[1]`; horizontal text wider than widget scrolls inside clip rect. | `PORTED/PARTIAL` for level-up name/EXP/stat-label widgets. |
| `game.d` state `22` update/draw | Calls `S.ao()`. | `PORTED/PARTIAL`. |
| `game.h.ao()` | Waits `K > 40` ticks or confirm key, then branches to state `23`, state `8`, or exits battle. | `PORTED/PARTIAL`; rebuild uses `expHoldTicks=40` and confirm. |
| `game.d` state `23` entry | Calls `S.ap()` to open `/data/ui/choiceskill.ui` and close `/data/ui/levelUp.ui`. | `PORTED/PARTIAL`. |

## Animation / Effect Classification

| Concern | Source-backed conclusion | Status |
| --- | --- | --- |
| Special `ah` level-up effect | No `ah` effect is created in `game.h.an()` or `game.h.ao()`. | `NOT PRESENT IN SOURCE SLICE`. |
| Pet sprite animation | Widget `10` uses `m` with `a=3`; `af.a(boolean...)` calls `k2.m.c()`, and `m.c()` calls source sprite `d.d()`. | `PORTED/PARTIAL`; rebuild animates using source sprite frame durations. |
| Text overflow marquee | `ae.a(...)` increments horizontal offset while `font.stringWidth(text) > widgetWidth`, then resets to `-widgetWidth`. | `PORTED/PARTIAL`; rebuild uses source-shaped horizontal marquee for level-up text widgets. |
| Panel/widget animation | Full generic `.ui` widget runtime is not ported yet. | `PARTIAL`. |
| Pixel-perfect timing | Source waits 40 ticks, but exact MIDP UI tick/render cadence is not pixel-compared. | `PENDING USER COMPARE`. |

## Rebuild Fix 2026-07-08

Changed:

- `rebuild_game/src/main/java/VqsvBattleRenderer.java`

Fix:

- `drawLevelUpOverlay()` now renders the level-up pet sprite with
  `idleCursor(view.visualId, 0, s.battleAnimationTick)` instead of fixed cursor
  `0`.
- Level-up stat labels now use their real `levelUp.ui` widget width `12`, not
  the old rebuild width `24`, so labels no longer overwrite the stat numbers.
- Level-up name, EXP text, and stat labels now use source-shaped horizontal
  marquee clipping when text is wider than its widget.

This is source-backed by `game.h.an()` widget `10 -> m.a=3`, `m.c()`, and
`af.a(boolean...)` ticking `m.c()`, plus `k.java -> ae.a(...)` maintaining
horizontal text offsets.

Still pending:

- Full generic `.ui` widget renderer.
- Original MIDP pixel/timing comparison.
- Evolution notice/evolve UI/effect path, which is a separate `game.k` /
  `game.h.bg()/bh()` flow.
