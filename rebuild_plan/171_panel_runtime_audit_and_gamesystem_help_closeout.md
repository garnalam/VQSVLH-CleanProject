# 171 Panel Runtime Audit And Gamesystem Help Closeout

Date: 2026-07-09

## Scope

Audited the current panel runtime as a whole, then implemented the smallest remaining `gamesystem.ui` sub-option slice:

- `gamemenu.ui b=0 -> P=14 -> game.h.m() /data/ui/gamesystem.ui`
- `gamesystem.ui b=1 -> P=20 -> game.h.u() /data/ui/help1.ui`
- help page left/right/back only

No speed toggle. No settings mutation. No main-menu reset path.

## Source Chain

Source files read:

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/aq.java`
- `modules/source_code/decoded/decompiled_source_cfr/an.java`
- `modules/ui/decoded/data__ui__gamesystem.ui.json`
- `modules/ui/decoded/data__ui__help1.ui.json`
- `modules/ui/decoded/data__ui__help.ui.json`
- `modules/ui/decoded/data__ui__option.ui.json`
- `modules/script/decoded/data__script__chs.mid.json`

Confirmed source path:

- `game.h.m()` loads `/data/ui/gamesystem.ui`.
- `game.h.n()` handles `gamesystem.ui`.
- confirm `b=0`: close `gamesystem.ui`, return `P=0`.
- confirm `b=1`: `o.a((byte)20)`, close `gamesystem.ui`.
- `game.h.u()` loads `/data/ui/help1.ui`, closes `/data/ui/gamesystem.ui`, sets `r=0`, calls `d(r)`.
- `game.h.v()` handles left/right page index `r=0..2`.
- `game.h.v()` back closes `help1.ui` and returns to `P=13` / `gamesystem.ui`.

Text/icon source for help pages:

- page 0 uses the literal help text from `game.h.d(0)`.
- page 1/2 entries use sprite `325`, cell `entry + 1`.
- entry text uses `an.f(...)`, backed by `aq.d[]` loaded from `/data/script/chs.mid`.
- source formula:
  - entries `0..10`: `an.f(311 + entry)`
  - entries `11..25`: `an.f(333 + entry - 11)`

## Panel Runtime Audit

Current top-level panel coverage:

- `gamemenu.ui`: `PORTED/PARTIAL`
- `gamesystem.ui`: `PORTED/PARTIAL`
- `gamesystem -> help1.ui`: `PORTED/PARTIAL`
- `gamemenu b=5 -> msgtip.ui save`: `PORTED/PARTIAL`
- `gamemenu b=1 -> petstate.ui`: `PORTED/PARTIAL`
- `gamemenu b=2 -> bag.ui item tab`: `PORTED/PARTIAL`
- `gamemenu b=4 -> task.ui`: `PORTED/PARTIAL`
- `gamemenu b=3 -> record.ui -> petmap.ui`: `PORTED/PARTIAL`

Still pending in panel runtime:

- `gamesystem b=2 -> P=21 -> game.h.w()/x()` settings on `/data/ui/help.ui`
- `gamesystem b=3 -> option.ui` main-menu confirm/reset path
- `bag.ui` item use paths
- `taskOption.ui` details/confirm
- `record.ui` second option branch
- `petmap.ui` confirm/details branch
- full `al/ao` UI runtime parity
- original-vs-rebuild pixel compare

## Rebuild Behavior

Implemented:

- selecting gamesystem help opens `HELP` mode.
- help renders from `/data/ui/help1.ui`.
- page 0 renders source text from `game.h.d(0)`.
- page 1/2 render source-backed sprite `325` status icons and `chs.mid` labels.
- right/left changes page `0..2`.
- back returns to `gamesystem.ui` with `selected=1`, matching source `P=13`.

## Status

`PORTING STATUS: PORTED/PARTIAL`

Ported:

- source route `gamesystem b=1 -> P=20 -> help1.ui`
- source route `help1.ui back -> P=13 -> gamesystem.ui`
- source page index behavior `r=0..2`
- source help page 0 text
- source help page 1/2 icon id/cell formula and text token formula

Partial:

- help page 1/2 render is source-shaped, not full `m` animated widget runtime.
- font/clip parity is not pixel-perfect; some help-list labels are tight because rebuild font metrics differ from original UI runtime.
- no original-client pixel compare.

Pending:

- settings branch `P=21`
- main-menu confirm/reset branch
- full `help.ui` option color mutation via `game.i.a().g`
- exact UI runtime for help list text clipping/animation

## Smoke

Focused PNG smoke:

- `panel_gamesystem_help_open`
- `panel_gamesystem_help_page_right`
- `panel_gamesystem_help_back_returns_gamesystem`

Regression PNG smoke:

- `panel_gamemenu_open_from_world`
- `panel_gamesystem_navigation`
- `panel_save_prompt_from_gamemenu`
- `panel_save_success_status`
- `panel_petstate_open_from_gamemenu`
- `panel_bag_open_from_gamemenu`
- `panel_task_open_from_gamemenu`
- `panel_petmap_record_open_from_gamemenu`
- `panel_petmap_open_from_record`
- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`

Validation:

- `build.ps1` passed
- `com.vqsv.rebuild.Main --check` passed
- `VqsvBattleDamageFormulaCheck` passed
- `git diff --check` passed
- Java mojibake scan passed

## Recommended Next Step

Next smallest source-backed panel branch:

1. Audit and implement `gamesystem b=2 -> P=21 -> game.h.w()/x()`.
2. Render `/data/ui/help.ui` settings view.
3. Implement only navigation/back and source option level mutation `game.i.a().g` equivalent in rebuild state.
4. Add PNG smoke:
   - `panel_gamesystem_settings_open`
   - `panel_gamesystem_settings_adjust_right`
   - `panel_gamesystem_settings_back_returns_gamesystem`

Do `gamesystem b=3 -> option.ui` main-menu reset after settings, because it mutates save/progress state and is riskier.
