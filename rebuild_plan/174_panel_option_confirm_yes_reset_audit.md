# 174 - Panel gamesystem option.ui confirm c=0 reset audit

## Target

Slice: `gamesystem.ui` row `b=3` opens `option.ui`; confirming `c=0` should follow source reset route.

Status before this slice:
- `c=1` / `Khong`: PORTED/PARTIAL, closes `option.ui` and returns to `gamesystem.ui`.
- `c=0`: PENDING, trace-only in rebuild.

## Source chain

Primary source: `modules/source_code/decoded/decompiled_source_cfr/game/h.java`, method `game.h.n()`.

When `b=3`, confirm key `196640`, and `f==0`:
- load `/data/ui/option.ui`
- set list selection `((al)this.p.a.a(0)).a.f = this.c = 1`
- widget `12` text `""`
- widget `13` text `"Khong"`
- set `f = 1`

When `f==1` and confirm with `c==1`:
- close `/data/ui/option.ui`
- set `f = 0`
- set `g = true`
- no reset

When `f==1` and confirm with `c==0`:
- `game.i.a().b = 0L`
- `game.i.a().a = 0L`
- `game.g.o().y = false`
- `game.i.a().a((byte)7)`
- close `/data/ui/gamesystem.ui`

State-machine source: `modules/source_code/decoded/decompiled_source_cfr/game/i.java`, method `game.i.a(byte)`.

For target state `7`:
- call `this.f()`
- set `this.m = game.f.a()`
- call `this.m.d()`
- attach `this.a(this.m)`
- immediately switch to state `8`

Title/menu source: `modules/source_code/decoded/decompiled_source_cfr/game/f.java`.

`game.f.d()` builds the title/main-menu runtime. It chooses menu rows from `W`:
- `W == true`: menu includes continue/new/options/help/about/exit.
- `W == false`: menu starts at new/options/help/about/exit.

`game.f.e()` is the new-game branch and separately resets runtime fields before `game.i.a().a((byte)9)`.

Save flag source: `modules/source_code/decoded/decompiled_source_cfr/game/k.java`.

`game.k.h()` writes save availability/state booleans including `W` into record storage. `game.k.i()` reads them back. The `option.ui c=0` branch above does not call `game.k.h()`, does not clear record storage, and does not directly mutate `W`.

## Rebuild mapping

Current rebuild title/menu is `com.vqsv.rebuild.state.BootFlowState`.

Current rebuild save availability is `VqsvSaveRuntime.hasSave()`, backed by:

`rebuild_game/build/save/vqsv_autosave.properties`

Therefore:
- PORTED/PARTIAL: `game.i.a().a/b = 0`, `game.g.o().y=false` are represented by a reset-title request trace and by leaving the legacy scene.
- PORTED/PARTIAL: `game.i.a((byte)7) -> game.f.d() -> state 8` maps to replacing `LegacyIntroDemoState` with `BootFlowState`.
- PENDING: exact source `game.i` timer fields and `game.g.y` object state are not modeled as standalone rebuild globals yet.
- PENDING: exact `game.f` state-8 entry bypassing earlier logo/music phases. Rebuild currently enters `BootFlowState`; smoke advances it to title.
- DO NOT DELETE SAVE: source branch does not prove persistent save deletion. Rebuild must preserve `VqsvSaveRuntime.hasSave()` in this slice.

## Implementation decision

Small safe slice:
1. `VqsvPanelRuntime` confirm `OPTION_CONFIRM selected=0` closes panel and calls a narrow scene reset-title request.
2. `VqsvIntroDemo.Scene` stores `panelTitleResetRequested`.
3. `LegacyIntroDemoState.tick()` checks that request after scene tick and replaces the state machine with `BootFlowState`.
4. Smoke verifies route to title and that save/continue remains available when a save existed before confirm.

## Smoke

Focused:
- `panel_gamesystem_option_confirm_yes_routes_title_preserves_save`

Regression:
- `panel_gamesystem_option_confirm_no_returns_gamesystem`
- `panel_gamesystem_option_back_returns_gamesystem`
- `panel_save_success_status`
- `boot_title_continue_with_save`
- route regressions: Sophie/Bunny/Elder

## Next

After this slice passes, next recommended panel slice is main-menu reset/new-game nuance only if source proves additional persistent-state mutation. Otherwise continue panel source sub-options or speed toggle after panel source paths are stable.
