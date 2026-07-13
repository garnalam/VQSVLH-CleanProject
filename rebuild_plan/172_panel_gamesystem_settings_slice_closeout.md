# 172 Panel Gamesystem Settings Slice Closeout

Date: 2026-07-10

## Scope

Implemented/verified the original gamesystem settings branch:

- `gamemenu.ui b=0 -> P=14 -> game.h.m() gamesystem.ui`
- `gamesystem.ui b=2 -> P=21 -> game.h.w() help.ui`
- `game.h.x()` left/right adjust/back only

No speed toggle. No `option.ui` main-menu reset. No full audio engine mutation.

## Source Chain

Source files read:

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/i.java`
- `modules/ui/decoded/data__ui__help.ui.json`

Confirmed source behavior:

- `game.h.n()` confirm `b=2` calls `o.a((byte)21)` and closes `/data/ui/gamesystem.ui`.
- `game.h.w()` calls `game.h.s()` then closes `/data/ui/gamesystem.ui`.
- `game.h.s()` loads `/data/ui/help.ui`.
- `game.h.s()` sets widget `5` text to `Tùy chọn`.
- `game.h.s()` hides widget `6`, shows widget `7`, shows widgets `9..12`.
- `game.h.aU()` colors widgets `10..12` by `game.i.a().g`.
- `game.h.x()`:
  - key `16400`: calls `game.i.a().i()`, decrements `g` to minimum `0`.
  - key `32832`: calls `game.i.a().h()`, increments `g` to maximum `3`.
  - key `131072`: returns to `P=13`, closes `/data/ui/help.ui`.
- `game.i.a().g` source default is `0`.

## Widget Mapping

`help.ui` widgets used:

- frame/background: `1`, `2`, `3`, `4`
- title: `5`
- hidden in source settings: `6`
- visible softkey: `7`
- content area: `8`
- label: `9`
- volume bars: `10`, `11`, `12`

Source colors mirrored for the bars:

- active: `-2148 & 0xffffff = 0xfff79c`
- inactive: `-8540732 & 0xffffff = 0x7da884`

## Rebuild Behavior

Implemented:

- selecting gamesystem row `2` opens `SETTINGS` mode.
- renders source-shaped `/data/ui/help.ui`.
- setting level starts at source default `0`.
- right increments level up to `3`.
- left decrements level down to `0`.
- bar colors update according to source `aU()` rule.
- back returns to `gamesystem.ui selected=2`.

## Status

`PORTING STATUS: PORTED/PARTIAL`

Ported:

- source route `gamesystem b=2 -> P=21 -> help.ui`
- source route settings back `P=13 -> gamesystem.ui`
- source setting level range `0..3`
- source bar color rule for widgets `10..12`
- source-shaped `help.ui` render

Partial:

- setting level is rebuild panel state, not full global `game.i.a().g` audio object parity.
- no actual J2ME `Player` / `VolumeControl` behavior.
- no full `ao/al` UI runtime.
- no original-vs-rebuild pixel compare.

Pending:

- persistence/boot linkage for sound level if needed later.
- exact UI runtime for hidden/active widget focus cursor.
- `gamesystem b=3 -> option.ui` main-menu confirm/reset path.

## Smoke

Focused PNG smoke:

- `panel_gamesystem_settings_open`
- `panel_gamesystem_settings_adjust_right`
- `panel_gamesystem_settings_back_returns_gamesystem`

Regression PNG smoke:

- `panel_gamemenu_open_from_world`
- `panel_gamesystem_navigation`
- `panel_gamesystem_help_open`
- `panel_gamesystem_help_page_right`
- `panel_gamesystem_help_back_returns_gamesystem`
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

Next smallest panel branch:

1. Audit/implement `gamesystem b=3 -> option.ui` main-menu confirmation.
2. First slice should only open/render/navigate/back the `option.ui` confirm box.
3. Do not reset progress yet until the source reset path is documented and smoke-covered.

After that, implement the confirm-yes reset mutation with a dedicated save/progress regression.
