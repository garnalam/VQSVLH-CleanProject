# 173 Panel Gamesystem Option Confirm Slice Closeout

Date: 2026-07-10

## Scope

Implemented/verified the first source-backed slice for the original gamesystem main-menu confirmation overlay:

- `gamesystem.ui b=3`
- `game.h.n()` opens `/data/ui/option.ui`
- render/navigate/back
- confirm `c=1` / `Không` closes without reset

No progress reset. No save deletion. No `game.i/game.g` mutation for confirm `c=0`.

## Source Chain

Source files read:

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/ui/decoded/data__ui__option.ui.json`

Confirmed source behavior in `game.h.n()`:

- confirm key with `b=3`, `f=0`:
  - opens `/data/ui/option.ui`
  - sets root selected field to `c = 1`
  - sets widget `12` text to empty string
  - sets widget `13` text to `Không`
  - sets `f = 1`
- confirm key with `f=1`, `c=1`:
  - closes `/data/ui/option.ui`
  - sets `f = 0`
  - sets `g = true`
- confirm key with `f=1`, `c=0`:
  - resets `game.i.a().b = 0`
  - resets `game.i.a().a = 0`
  - sets `game.g.o().y = false`
  - calls `game.i.a().a((byte)7)`
  - closes `/data/ui/gamesystem.ui`
- back key with `f=1`:
  - closes `/data/ui/option.ui`
  - sets `f = 0`

## Widget Mapping

`option.ui` widgets used:

- row backgrounds: `10`, `11`
- row text: `12`, `13`
- row icons: `8`, `9`

Important source detail:

- This path deliberately sets widget `12` to `""`.
- Rebuild does not invent a `Có` label for the top row in this slice.

## Rebuild Behavior

Implemented:

- selecting gamesystem row `3` opens `OPTION_CONFIRM` mode.
- default selection is `c=1` / `Không`.
- up selects `c=0`.
- down selects `c=1`.
- back closes `option.ui` and returns to `gamesystem.ui selected=3`.
- confirm on `c=1` closes and returns to `gamesystem.ui selected=3`.
- confirm on `c=0` only traces `PENDING`; it does not reset game progress yet.

## Status

`PORTING STATUS: PORTED/PARTIAL`

Ported:

- source route `gamesystem b=3 -> option.ui`
- source default selection `c=1`
- source widget text mutations `12=""`, `13="Không"`
- up/down navigation between `c=0` and `c=1`
- back close
- confirm-no close

Partial:

- confirm-yes reset path is intentionally not ported yet.
- render is source-shaped, not full `ao/al` overlay runtime.
- no original-vs-rebuild pixel compare.

Pending:

- confirm `c=0` reset mutation.
- route from reset into correct boot/main menu state.
- save/progress safety smoke around reset.
- original-client capture compare if visual parity is required.

## Smoke

Focused PNG smoke:

- `panel_gamesystem_option_open`
- `panel_gamesystem_option_navigate_up`
- `panel_gamesystem_option_back_returns_gamesystem`
- `panel_gamesystem_option_confirm_no_returns_gamesystem`

Regression PNG smoke:

- `panel_gamemenu_open_from_world`
- `panel_gamesystem_navigation`
- `panel_gamesystem_help_open`
- `panel_gamesystem_help_page_right`
- `panel_gamesystem_help_back_returns_gamesystem`
- `panel_gamesystem_settings_open`
- `panel_gamesystem_settings_adjust_right`
- `panel_gamesystem_settings_back_returns_gamesystem`
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

Next slice should be confirm-yes reset, but only after an explicit source audit:

1. Audit how `game.i.a().a((byte)7)` routes from panel to boot/main menu state.
2. Audit how `game.i.a().a/b` and `game.g.o().y` map to rebuild save/progress state.
3. Implement confirm `c=0` mutation behind focused smoke.
4. Required smoke:
   - option confirm yes routes to title/main menu state.
   - existing save/continue state is not accidentally corrupted outside this reset path.
   - panel and route regressions still pass.
