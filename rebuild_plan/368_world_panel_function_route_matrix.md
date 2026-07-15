# 368 - World Panel Function Route Matrix

Date: 2026-07-15

Scope: the two world softkey panels opened from `world.ui`, their source
function routes, current rebuild status, and the first completed follow-up
slice: `record.ui` c=1 -> `badge.ui`.

## Source Entry Points

- `game.k.c()` opens `/data/ui/world.ui`.
- `world.ui` widget `7`, cell `175`: left softkey -> `game.k.m()` ->
  `/data/ui/gamesystem.ui`.
- `world.ui` widget `5`, cell `68`: right softkey -> `game.k.k()` ->
  `/data/ui/gamemenu.ui`.

## Function Matrix

| Panel | Row | Source method/state | UI opened | Rebuild status | Notes |
| --- | --- | --- | --- | --- | --- |
| `gamesystem.ui` | Continue | `game.k.n()` case `b=0`, `o.a(0)` | world | `PORTED/PARTIAL` | Route verified by smoke. |
| `gamesystem.ui` | Help | `game.k.n()` case `b=1`, `o.a(20)` | `help1.ui` | `PORTED/PARTIAL` | Pages/back verified; generic widget VM pending. |
| `gamesystem.ui` | Settings | `game.k.n()` case `b=2`, `o.a(21)` | `help.ui` | `PORTED/PARTIAL` | Volume-level UI source-shaped. |
| `gamesystem.ui` | Main menu confirm | `game.k.n()` case `b=3` | `option.ui` | `PORTED/PARTIAL` | Confirm-no verified; destructive reset path remains guarded. |
| `gamemenu.ui` | Shop | `game.k.l()` case `b=0` when `a.a.i` | shop flow | `PENDING/PARTIAL` | Premium/shop top row only exists when source flag true. |
| `gamemenu.ui` | Pet | `game.k.l()` -> `o.a(7)` | `petstate.ui` | `PORTED/PARTIAL` | Pet list/setting slices exist, not full generic runtime. |
| `gamemenu.ui` | Bag | `game.k.l()` -> `o.a(8)` | `bag.ui` | `PORTED/PARTIAL` | Item behavior coverage is incremental by source rows. |
| `gamemenu.ui` | Record | `game.k.l()` -> `o.a(9)` | `record.ui` | `PORTED/PARTIAL` | Record page + petmap + badge route now open. |
| `gamemenu.ui` | Task | `game.k.l()` -> `o.a(10)` | `task.ui` | `PORTED/PARTIAL` | Task list/tab/back/key10 verified; `taskOption.ui` event popup slice documented in `369`. |
| `gamemenu.ui` | Save | `game.k.l()` -> `o.a(22)` | `msgtip.ui` | `PORTED/PARTIAL` | Save prompt/status/success verified. |

## Badge Slice Source Proof

Source methods:

- `game.k.Q()` opens `/data/ui/record.ui`.
- `game.k.R()` handles record page:
  - `c=0` -> `o.a(11)` -> petmap.
  - `c=1` -> `o.a(12)` -> badge.
- `game.k.W()` opens `/data/ui/badge.ui` and `/data/ui/record.ui`, then
  marks badge icon widgets `25..32` when `q.C[i][0] != 0`.
- `game.k.be()` fills:
  - widget `13`: `aq.c[2][b][0]` text id, badge name.
  - widget `14`: `aq.c[2][b][2 + q.b(b,1)]`, badge description by rank.
  - widget `16`: `Chưa đạt` or `Đã đạt được`.
- `game.k.X()` navigates the badge UI and back closes `/data/ui/badge.ui`.

`badge.ui` widgets used:

| Widget | Role |
| --- | --- |
| `4/5` | title frame + `Huy hiệu`. |
| `6` | back softkey `Quay lại`, cell `28`. |
| `17..24` | 8 badge slot backgrounds, cell `36/37`. |
| `25..32` | badge icon positions; achieved source icons map to cells `46..53`. |
| `13/14/15/16/33` | selected badge name/description/status/detail text. |

Badge text/layout facts:

| Widget | Source role | Bounds | Color | Text behavior | Rebuild status |
| --- | --- | --- | --- | --- | --- |
| `13` | selected badge name | `x=55,y=174,w=48` | `-14717551` / `#1c6c91` | single line, centered (`b=4`) | `PORTED/PARTIAL` |
| `14` | selected badge description | `x=54,y=194,w=135` | active text `g=-16777216` / `#000000`; inactive `l=-3672069` / `#c7f0eb` | `d=true`, source `c.i` has active/inactive palettes and `a.e.a(...)` wraps/clips vertically | `PORTED/PARTIAL` |
| `15` | status label | `x=109,y=242,w=24` | `-14651500` / `#204954` | `d=false`, source marquee if text is wider than widget | `PORTED/PARTIAL` |
| `16` | status value | `x=148,y=242,w=40` | `-14651500` / `#204954` | `d=false`, source marquee if text is wider than widget | `PORTED/PARTIAL` |

Source text renderer notes:

- `a.e.a(...)` uses `bl=true` for wrapped vertical text. This is the path for
  badge description widget `14`.
- `c.i` uses active palette `e/f/g` when the visual is rendered active, and
  inactive palette `j/k/l` otherwise. Badge description must use the active
  text color `g=#000000`; using inactive `l=#c7f0eb` makes the text almost
  invisible on the source blue panel.
- `bl=false` uses horizontal scroll when `stringWidth > widgetWidth`. This is
  the path for status widgets `15/16`, whose source widths are intentionally
  narrow.
- Rebuild now resets badge text scroll on selection change and starts marquee
  from the beginning of the text, matching the source-shaped scroll offset more
  closely.
- `PARTIAL`: exact Java ME font baseline/clip behavior is still not a generic
  UI runtime. The rebuild applies a local baseline correction for the status
  row so text stays inside the `badge.ui` panel with the current font renderer.

## Rebuild Changes

- Added `VqsvPanelRuntime.Mode.BADGE`.
- `recordSelected == 1` now opens badge mode instead of tracing pending.
- Badge renderer uses `badge.ui` widget positions and `aq.c[2]` rows through
  `VqsvBattleTables.row(2, id)`.
- Badge navigation is source-shaped for the 2x4 grid.
- Back from badge returns to record with `recordSelected=1`.
- Badge description/status text now follows source widget behavior:
  description wraps/clips in widget `14` and uses the source active text
  palette; status widgets `15/16` marquee only because their source widths are
  narrow.

## Status

| Area | Status | Note |
| --- | --- | --- |
| `record.ui` c=1 -> `badge.ui` route | `PORTED` | Source route and back path smoke verified. |
| `badge.ui` frame/list/icon render | `PORTED/PARTIAL` | Uses decoded widget bounds/cells. |
| Badge name/description | `PORTED/PARTIAL` | Source table `aq.c[2]` via `db.mid` group 2. |
| Badge achieved state | `PARTIAL` | Source uses `q.C[i][0/1]`; rebuild currently only has `sourceBadges`, so renderer proxies first N badges as achieved. |
| Badge reward/mutation logic | `PENDING` | Not part of this route slice. |
| Full generic panel UI VM | `PENDING` | Still branch-by-branch, not full widget runtime. |

## Smoke

Latest suite output:

`rebuild_game/build_intro_demo/world_panel_full_bqtask_marker`

New checkpoints:

- `panel_badge_open_from_record`
- `panel_badge_navigation`
- `panel_badge_back_returns_record`
- `panel_badge_record_back_returns_gamemenu`
- `world_bqtask_marker_available`
- `world_bqtask_marker_active`
- `world_bqtask_marker_completed_clear`
- `world_dodo_group7_accept_task0`
- `world_dodo_group7_reject_no_task`
- `panel_task_branch_opcode49_reject_no_add`
- `panel_task_branch_save_load_g_h_f`

Latest result:

- `world_panel_full`: PASS `63/63`.

Latest focused text-layout smoke:

- `rebuild_game/build_intro_demo/badge_text_active_palette_fix/panel_badge_open_from_record.png`
- `rebuild_game/build_intro_demo/badge_text_active_palette_fix/panel_badge_navigation.png`

## Next

Continue function-by-function from the same matrix. Recommended next slice:

1. Port Dodo active/complete-side branch quest groups `8/9` so row0 can finish through source `game.e.m(0)`.
2. Pick the next concrete panel route still `PENDING` in this matrix.
3. Then return to bag/petsetting special branches that still need source-backed parity.

Do not jump to generic UI VM until a concrete route requires it.
