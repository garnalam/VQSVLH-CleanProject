# 166 - Panel Save Flow Slice Closeout

Date: 2026-07-09

Scope completed: original panel save flow from `gamemenu b=5 -> P=22 -> game.h.K()`.

## Source Chain

- `game.h.l()`
  - `gamemenu selected b=5`
  - hides widgets `11/12`
  - switches world runtime to `P=22`
- `game.k.a(byte 22)`
  - calls `S.H()`
  - loads `/data/ui/msgtip.ui`
  - writes prompt text: `Co luu du lieu khong?`
- `game.h.K()`
  - `f=0`, confirm: writes `Dang luu...`, hides widgets `3/4` with `J()`
  - `f=1`: waits for `game.k.k()`
  - save success: writes `Luu thanh cong`, sets `f=2`
  - `f=2`: closes `msgtip.ui` and `gamemenu.ui`, returns `P=0`

## Implemented

- Extended `VqsvPanelRuntime` with `SAVE` mode.
- `gamemenu selected=5` confirm now opens source-shaped `msgtip.ui` prompt.
- `SAVE` mode renders:
  - frame widget `1`
  - text widget `2`
  - confirm/back icon widgets `3/4` only while `savePhase == 0`
- Confirm save:
  - phase `0 -> 1`: shows `Dang luu...`
  - phase `1 -> 2`: calls `VqsvSaveRuntime.save(scene)` and shows `Luu thanh cong`
  - phase `2`: closes panel and returns to world/free movement
- Back during prompt returns to `gamemenu selected=5`, matching source `game.h.K() f=0 back -> P=6`.
- Refactored panel smoke logic out of `runSmokeCheckpoint()` into `handlePanelCheckpoint()` to avoid Java bytecode `code too large`.

## Status

`PORTED/PARTIAL`

The panel save UI/control flow is source-shaped and wired to the existing rebuild save runtime. Save data contents remain the existing `VqsvSaveRuntime` route snapshot, not a full original RMS binary save.

Remaining partials:

- Full original persistent data format: `PENDING`.
- Exact async timing of `game.k.k()`: `PORTED/PARTIAL`; rebuild completes on next save tick.
- Pixel-perfect compare against original client: `PENDING`.
- Other panel branches still pending:
  - pet panel from `gamemenu b=1`
  - bag from `b=2`
  - record from `b=3`
  - task from `b=4`
  - gamesystem help/settings/main menu rows
- Speed toggle remains `REBUILD_POLICY/PENDING`.

## Verification

Build/check:

- `powershell -ExecutionPolicy Bypass -File .\build.ps1`
- `java "-Dvqsv.modules=..\modules" -jar build\libs\vqsv-liet-hoa-rebuild.jar --check`
- `java "-Dvqsv.modules=..\modules" -cp build\classes VqsvBattleDamageFormulaCheck`
- `git diff --check`
- mojibake scan on Java source and panel docs

Focused PNG smoke:

- `rebuild_game/build/smoke/panel_save_prompt_from_gamemenu.png`
- `rebuild_game/build/smoke/panel_save_saving_status.png`
- `rebuild_game/build/smoke/panel_save_success_status.png`
- `rebuild_game/build/smoke/panel_save_success_closes_world.png`

Regression PNG smoke:

- `rebuild_game/build/smoke/save_regression_panel_gamemenu_navigation.png`
- `rebuild_game/build/smoke/save_regression_panel_gamesystem_navigation.png`
- `rebuild_game/build/smoke/save_regression_boot_title_continue_with_save.png`
- `rebuild_game/build/smoke/save_regression_route_bunny_after_battle_task.png`
- `rebuild_game/build/smoke/save_regression_route_elder_after_battle_reward_state.png`
- `rebuild_game/build/smoke/save_regression_route_sophie_after_battle_branch.png`

## Next Step

Recommended next slice: panel pet branch from `gamemenu b=1 -> P=7`.

Reason:

- It connects to already existing world/battle `petstate.ui` partial renderer.
- It is more gameplay-critical than help/settings.
- It should replace the old direct world-petstate shortcut with a source path.

Keep it narrow:

- Open carried pet `petstate.ui` through `gamemenu b=1`.
- Render source rows from current `sourcePets`.
- Back returns to `gamemenu`/world as source-proven.
- Do not implement petsetting/equip/evolve/release in the first pet-panel slice.
- Do not add speed until original panel branches are stable.
