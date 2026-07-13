# 170 Panel Petmap Slice Closeout

Date: 2026-07-09

## Scope

Implemented/verified the first source-backed slice for the original illustrated pet record path:

- `gamemenu.ui` row `b=3`
- source target `P=9`
- `game.h.N()` opens `/data/ui/record.ui`
- confirm record option `c=0`
- source target `P=11`
- `game.h.P()` opens `/data/ui/petmap.ui`
- render/navigate/back only

No speed toggle. No petmap detail page. No record subpage beyond the first source option.

## Source Chain

Source files read:

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- `modules/ui/decoded/data__ui__record.ui.json`
- `modules/ui/decoded/data__ui__petmap.ui.json`

Confirmed source path:

- `game.h.l()` on `gamemenu b=3`
- sets `c=0`
- calls `o.a((byte)9)`
- closes `/data/ui/gamemenu.ui`
- `game.k.a(byte 9)` calls `game.h.N()`
- `game.h.N()` loads `/data/ui/record.ui`, writes record counters, sets previous cursor
- `game.h.O()` handles left/right, confirm/back
- confirm `c=0` calls `o.a((byte)11)`
- `game.k.a(byte 11)` calls `game.h.P()`
- `game.h.P()` loads `/data/ui/petmap.ui`, resets `b/c/f`, calls `aZ()`
- `game.h.Q()` handles petmap list navigation/tab/back

Important correction:

- This is not a direct `gamemenu -> petmap.ui` jump in source.
- The real source path is `gamemenu b=3 -> P=9 record.ui -> P=11 petmap.ui`.

## Widget Mapping

Petmap UI source widgets used:

- frame/title: `1`, `2`, `3`, `4`, `5`
- tabs: `6..12`
- tab labels: `13..19`
- selected summary: `20`
- sprite preview: `21`
- five rows:
  - row background: `25`, `29`, `33`, `37`, `41`
  - marker: `44`, `45`, `46`, `47`, `48`
  - name: `27`, `31`, `35`, `39`, `43`
- scrollbar: source-shaped minimal render
- softkeys: `49`, `50`

## Rebuild Behavior

Implemented/verified:

- `gamemenu b=3` opens `RECORD` mode.
- `record.ui` renders source-shaped counters from current rebuild state.
- record back returns to `gamemenu selected=3`.
- record confirm on first option opens `PETMAP` mode.
- petmap renders tabs by source element category.
- petmap rows are built from source battle species table.
- owned markers are derived from current source party/bank species.
- up/down navigates rows.
- left/right switches tabs.
- back from petmap returns to `record.ui`.

## Status

`PORTING STATUS: PORTED/PARTIAL`

Ported:

- source route `gamemenu b=3 -> P=9 -> record.ui`
- source route `record c=0 -> P=11 -> petmap.ui`
- record render/back
- petmap render/navigate/tab/back
- source-backed species rows from battle species table
- owned marker from rebuild source pet storage

Partial:

- record counters are source-shaped from rebuild state, not full `game.g` storage parity.
- petmap category grouping uses rebuilt battle species table fields.
- scrollbar is minimum source-shaped render, not full `al` runtime.
- no original-vs-rebuild pixel compare.
- record bottom option layout is functional but not claimed pixel-perfect.

Pending:

- petmap details/confirm branch
- `record.ui` second option branch
- full `game.g.o().k(5)` item gate for pet book access
- full `aq.c`/pet book storage parity
- pixel-perfect compare against original client

## Smoke

Focused PNG smoke:

- `panel_petmap_record_open_from_gamemenu`
- `panel_petmap_open_from_record`
- `panel_petmap_navigation`
- `panel_petmap_tab_navigation`
- `panel_petmap_back_returns_record`
- `panel_petmap_record_back_returns_gamemenu`

Output examples:

- `rebuild_game/build/smoke/panel_petmap_record_open_from_gamemenu.png`
- `rebuild_game/build/smoke/panel_petmap_open_from_record.png`
- `rebuild_game/build/smoke/panel_petmap_tab_navigation.png`
- `rebuild_game/build/smoke/panel_petmap_record_back_returns_gamemenu.png`

Regression smoke run:

- `panel_gamemenu_open_from_world`
- `panel_gamesystem_navigation`
- `panel_save_prompt_from_gamemenu`
- `panel_save_success_status`
- `panel_petstate_open_from_gamemenu`
- `panel_bag_open_from_gamemenu`
- `panel_task_open_from_gamemenu`
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

Next original panel slice should be:

1. Audit current panel runtime as a whole and close remaining top-level gamemenu branches.

Concrete next candidate:

- `gamemenu b=0 -> gamesystem.ui` sub-options help/settings/main-menu, only one small branch after source audit.

Alternative:

- audit `petmap.ui` confirm/details branch, but only after source proves which UI it opens and what state it mutates.

Do not add speed toggle until original panel branches are stable.
