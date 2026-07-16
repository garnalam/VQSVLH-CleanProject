# 407 - Panel Save / msgtip.ui Source Flow Closeout

Date: 2026-07-15

Scope: audit and closeout target for `gamemenu.ui` row `5` / `Luu du lieu`.

## Source Basis

Decoded source `game.k`:

- `K()` opens `/data/ui/msgtip.ui` with sprite `257`.
- `N()` drives the save prompt:
  - `f == 0` + confirm: set `f = 1`, text `Dang luu...`, hide widgets `3/4`.
  - `f == 0` + back: set selected menu row to save row, close `msgtip.ui`, return to `gamemenu.ui`.
  - `f == 1`: call `((l)this.o).I()`, then show `Luu thanh cong`, set `f = 2`.
  - `f == 2`: close both `msgtip.ui` and `gamemenu.ui`, return to world state `P=0`.

Decoded UI `msgtip.ui`:

- widget `1`: message box frame.
- widget `2`: message text.
- widget `3`: right/confirm icon.
- widget `4`: left/back icon.

## Current Runtime State

| Area | Runtime mapping | Status |
| --- | --- | --- |
| `gamemenu.ui` row `5` | opens `Mode.SAVE`, `savePhase=0`, prompt text `Co luu du lieu khong?` | `PORTED/PARTIAL` |
| confirm from prompt | `savePhase=1`, message `Dang luu...`, hides widgets `3/4` by render condition | `PORTED/PARTIAL` |
| save execution | calls `VqsvSaveRuntime.save(s)` and writes `build/save/vqsv_autosave.properties` | `PORTED/PARTIAL` |
| success state | `savePhase=2`, message `Luu thanh cong` | `PORTED/PARTIAL` |
| close after success | closes panel and returns to free-world movement | `PORTED/PARTIAL` |
| cancel/back from prompt | runtime exists, but lacked a dedicated smoke checkpoint before this slice | `PORTED/PARTIAL` |

## Data Saved

`VqsvSaveRuntime` currently persists a source-shaped route snapshot:

- scene/room/event index/camera/player.
- money, badges, source flags.
- event states, branch tasks, actors.
- bag items, equipment, special rewards.
- party pets and bank pets.
- egg/ride/speed-related panel state.

Status: `PORTED/PARTIAL`. It is not a byte-identical original save format.

## Missing Coverage Before Code Slice

- `panel_save_back_cancel_returns_gamemenu`: `PENDING`.
- visual asserts for `msgtip.ui` prompt/status: `PENDING`.
- proof that a save made through panel can drive title-menu `Choi tiep`: `PENDING`.

## Implemented In This Slice

| Area | Change | Status |
| --- | --- | --- |
| Save cancel smoke | Added `panel_save_back_cancel_returns_gamemenu`: prompt back closes `msgtip.ui` and returns to `gamemenu.ui` row `5` without entering save-success trace. | `PORTED/PARTIAL` |
| Save visual smoke | Added source-widget visual assertions for prompt frame, confirm/back icons, saving text, and success text. | `PORTED/PARTIAL` |
| Continue-load smoke | Added `boot_title_continue_after_panel_save`: performs save through panel, then verifies title `Choi tiep` routes to `LegacyIntroDemoState` loaded from the saved scene snapshot. | `PORTED/PARTIAL` |
| Regression suite | Added cancel and continue-load checkpoints to `world_panel_full`. | `FIXED` |

## Verification

Build/check:

- `.\build.ps1`: PASS
- `java "-Dvqsv.modules=..\modules" -cp .\build\classes com.vqsv.rebuild.Main --check`: PASS

Focused PNG smoke:

- `panel_save_prompt_from_gamemenu`: PASS
- `panel_save_back_cancel_returns_gamemenu`: PASS
- `panel_save_saving_status`: PASS
- `panel_save_success_status`: PASS
- `panel_save_success_closes_world`: PASS
- `boot_title_continue_after_panel_save`: PASS

Suite smoke:

- `world_panel_full`: PASS `102/102`
- Output folder: `rebuild_game/build_intro_demo/panel_save_407/world_panel_full`

Visual checked:

- `panel_save_prompt_from_gamemenu.png`: `msgtip.ui` box, prompt text, check icon, and X icon are visible.
- `boot_title_continue_after_panel_save.png`: title menu shows `Choi tiep` after saving through panel.

Mojibake scan:

- No new mojibake in touched Java files.

## Closeout Status

`gamemenu.ui` row `5` / `Luu du lieu`: `PORTED/PARTIAL`.

Now covered:

- save prompt open from menu.
- prompt cancel/back to menu.
- prompt confirm -> saving status.
- save success status.
- success close -> free-world.
- panel-created save exposes `Choi tiep` at title and loads into the saved legacy scene snapshot.

Still not full parity:

- Save format is rebuild properties snapshot, not original byte-identical game save.
- Full original `((l)this.o).I()` internals remain `PORTED/PARTIAL`.
- No original-vs-rebuild pixel compare for `msgtip.ui`.

## Recommended Next Step

Next slice should be a top-level panel closeout summary for right-softkey `gamemenu.ui` rows `0..5`, then choose the next visible polish target from actual screenshots rather than adding new panel branches blindly.
