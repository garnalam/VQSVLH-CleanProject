# 167 - Panel Petstate Source Path Slice Closeout

Date: 2026-07-09

Scope completed: original panel pet branch from `gamemenu b=1 -> P=7 -> petstate.ui`.

## Source Chain

- `game.h.l()`
  - `gamemenu selected b=1`
  - sets `c=0`
  - calls `o.m()`
  - switches world runtime to `P=7`
  - closes `/data/ui/gamemenu.ui`
- `game.k.a(byte 7)`
  - sets `S.c = 0`
  - calls `S.W()`
- `game.h.W()/e(int)`
  - sets `b=0`
  - loads `/data/ui/petstate.ui`
  - fills carried pet rows from `game.g.z`
  - hides widgets `75/76` for world owner `game.k`
- `game.h.X()` back from `P=7`
  - returns to `P=6`
  - reopens `gamemenu.ui` with `b=1`

## Implemented

- `VqsvPanelRuntime` confirm on `gamemenu selected=1` now:
  - closes the gamemenu panel
  - calls `Scene.openWorldPetstate()`
  - traces source path `game.h.l -> P=7 -> game.h.W petstate.ui`
- `Scene.tickWorldPetstate()` now handles back:
  - closes `petstate.ui`
  - reopens `gamemenu.ui` selected row `1`
  - traces `game.h.X back close petstate.ui`
- Focused smoke uses source catch runtime to create Bunny before opening panel petstate:
  - initial Dien Mieu via story state
  - Bunny captured through `SourceBattleRuntime`
  - no mock pet inserted for the pet panel smoke

## Status

`PORTED/PARTIAL`

The source path into carried-pet `petstate.ui` is wired and smoke-verified. Existing petstate rendering remains the current source-backed partial renderer from battle/world petstate work.

Still pending:

- Confirm in petstate should open `petsetting.ui`: `PENDING`
- Pet action menu: item use, battle position, equipment, release, skill, evolve/mutate: `PENDING`
- Exact source list/text scroll behavior: `PORTED/PARTIAL`
- Original-vs-rebuild pixel compare: `PENDING`
- Full `game.g.z` parity and all pet storage details: `PORTED/PARTIAL`
- Speed toggle remains `REBUILD_POLICY/PENDING`

## Verification

Build/check:

- `powershell -ExecutionPolicy Bypass -File .\build.ps1`
- `java "-Dvqsv.modules=..\modules" -jar build\libs\vqsv-liet-hoa-rebuild.jar --check`
- `java "-Dvqsv.modules=..\modules" -cp build\classes VqsvBattleDamageFormulaCheck`
- `git diff --check`
- mojibake scan on Java source and panel docs

Focused PNG smoke:

- `rebuild_game/build/smoke/panel_petstate_open_from_gamemenu.png`
- `rebuild_game/build/smoke/panel_petstate_navigation.png`
- `rebuild_game/build/smoke/panel_petstate_back_returns_gamemenu.png`

Regression PNG smoke:

- `rebuild_game/build/smoke/pet_regression_panel_gamemenu_navigation.png`
- `rebuild_game/build/smoke/pet_regression_panel_gamesystem_navigation.png`
- `rebuild_game/build/smoke/pet_regression_panel_save_success_closes_world.png`
- `rebuild_game/build/smoke/pet_regression_boot_title_continue_with_save.png`
- `rebuild_game/build/smoke/pet_regression_route_bunny_after_battle_task.png`
- `rebuild_game/build/smoke/pet_regression_route_elder_after_battle_reward_state.png`
- `rebuild_game/build/smoke/pet_regression_route_sophie_after_battle_branch.png`

## Next Step

Recommended next slice: original bag branch `gamemenu b=2 -> P=8 -> bag.ui`.

Reason:

- It completes another top-level panel branch without diving into petsetting complexity.
- It is needed before any speed/settings mod, because the panel hub should be source-stable first.

Keep it narrow:

- Open `bag.ui` through `gamemenu b=2`.
- Render tab `b=0` item list using existing `sourceBagItems`.
- Back returns to `gamemenu selected=2`.
- Do not implement item use subflows in the first bag slice.
