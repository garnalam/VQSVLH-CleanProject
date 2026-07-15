# 383 - Panel gamesystem.ui Settings Re-entry Audit

Date: 2026-07-15

Scope: return to the second world softkey branch, the bottom-left system
softkey. This audit focuses on `gamesystem.ui` and especially the settings row
that opens `help.ui`. It does not cover the right-softkey `gamemenu.ui` routes
or `bodyShop.ui`.

## Source Entry

Source files/data read:

- `source_code/decoded/decompiled_source_cfr/game/k.java`
- `ui/decoded/data__ui__world.ui.json`
- `ui/decoded/data__ui__gamesystem.ui.json`
- `ui/decoded/data__ui__help.ui.json`
- `ui/decoded/data__ui__help1.ui.json`
- `ui/decoded/data__ui__option.ui.json`

Source route:

```text
world.ui widget 7 / left softkey
  -> game.k.m()
  -> /data/ui/gamesystem.ui
  -> game.k.n()
```

`gamesystem.ui` source rows:

| Row | Source confirm | Target | Current status |
| --- | --- | --- | --- |
| 0 | `game.k.n()` case `b=0` | close `gamesystem.ui`, return world | `PORTED/PARTIAL` |
| 1 | `game.k.n()` case `b=1` | `P=20`, `help1.ui` | `PORTED/PARTIAL` |
| 2 | `game.k.n()` case `b=2` | `P=21`, `help.ui` settings | `PORTED/PARTIAL` |
| 3 | `game.k.n()` case `b=3` | `option.ui` main-menu confirm | `PORTED/PARTIAL`; confirm-yes reset remains guarded |

## Settings Source Behavior

Source chain:

```text
game.k.n() b=2
  -> o.a((byte)21)
  -> close /data/ui/gamesystem.ui
  -> game.k.s()
  -> open /data/ui/help.ui
  -> game.k.t()
```

`game.k.s()`:

- opens `/data/ui/help.ui`;
- sets widget `5` to `Tuy chon`;
- clears widget `8`;
- hides widget `6`;
- shows widget `7`;
- shows widgets `9..12`;
- calls `aW()`.

`game.k.aW()`:

- loops `i=1..3`;
- writes widget `10..12` color by current volume level `game.f.B().r`;
- active color: `-2148 & 0xffffff = 0xfff79c`;
- inactive color: `-8540732 & 0xffffff = 0x7da884`.

`game.k.t()`:

- left key `16400` calls `game.f.B().G()` then `aW()`;
- right key `32832` calls `game.f.B().F()` then `aW()`;
- confirm key `131072` saves `game.g.B().k = game.f.B().r`, returns to system
  menu, and closes `help.ui`.

## UI Widget Matrix

`gamesystem.ui`:

| Widget | Role | Current rebuild handling |
| --- | --- | --- |
| `1..5` | frame/background bands | drawn through decoded layout helpers |
| `2` | title `He thong menu` | drawn centered/source-shaped |
| `6..9` | four rows | selection and navigation covered |
| `10` | left softkey `Xac dinh` | white centered text, source cell `28` |
| `11` | right softkey `Quay lai` | white centered text, source cell `28` |

`help.ui` settings view:

| Widget | Role | Current rebuild handling |
| --- | --- | --- |
| `1..4` | frame/background | drawn through decoded layout helpers |
| `5` | title `Tuy chon` | drawn source-shaped |
| `6` | right/back softkey | hidden for settings by source; rebuild does not draw it in settings |
| `7` | left/confirm softkey | drawn source-shaped |
| `8` | help text area | blank in settings |
| `9` | label `Am luong` | drawn source-shaped |
| `10..12` | three volume bars | colors update by `settingsLevel` |

## Current Rebuild Mapping

Runtime:

- `VqsvPanelRuntime.openGameSystemFromWorld()` opens the left softkey branch.
- `renderGameSystem()` renders `gamesystem.ui`.
- `tickGameSystem()` routes row 0/1/2/3.
- `renderSettings()` renders the source-shaped `help.ui` settings screen.
- `tickSettings()` handles left/right/back/confirm.

Smoke checkpoints already present:

- `panel_gamesystem_click_softkey_open`
- `panel_gamesystem_open_from_gamemenu`
- `panel_gamesystem_navigation`
- `panel_gamesystem_continue_returns_world`
- `panel_gamesystem_back_returns_world`
- `panel_gamesystem_help_open`
- `panel_gamesystem_help_page_right`
- `panel_gamesystem_help_back_returns_gamesystem`
- `panel_gamesystem_settings_open`
- `panel_gamesystem_settings_adjust_right`
- `panel_gamesystem_settings_confirm_returns_gamesystem`
- `panel_gamesystem_settings_back_returns_gamesystem`
- `panel_gamesystem_option_open`
- `panel_gamesystem_option_navigate_up`
- `panel_gamesystem_option_back_returns_gamesystem`
- `panel_gamesystem_option_confirm_no_returns_gamesystem`

## Status

| Area | Status | Note |
| --- | --- | --- |
| left world softkey route | `FIXED/PORTED/PARTIAL` | Opens `gamesystem.ui` from world UI and PC-friendly hitbox. |
| `gamesystem.ui` frame/rows/softkeys | `PORTED/PARTIAL` | Source widgets used; generic UI VM not full. |
| row 0 continue/back | `PORTED/PARTIAL` | Returns world. |
| row 1 help | `PORTED/PARTIAL` | `help1.ui` page navigation/back exists. |
| row 2 settings | `PORTED/PARTIAL` | Volume level 0..3 source-shaped; no real audio backend parity. |
| settings confirm/save-return | `PORTED/PARTIAL` | Confirm key saves the selected level in source-shaped trace and returns to `gamesystem.ui selected=2`. |
| row 3 option confirm-no/back | `PORTED/PARTIAL` | Confirm-yes destructive reset remains guarded by separate audit. |
| full audio persistence | `PARTIAL/PENDING` | Current level is runtime panel state; real source save object/audio backend is not fully wired. |
| original pixel compare | `PENDING` | Not claimed. |

## Risks To Check Before Coding

- `help.ui` settings uses widget `7` as the visible softkey while widget `6` is
  hidden. If a visual shows the wrong softkey, fix against `game.k.s()`.
- Settings confirm now returns to `gamesystem.ui`; real audio playback is still
  intentionally separate from this UI route slice.
- The actual audio engine is not necessary for UI parity, but persisted setting
  state should not be lost if later wired into save/load.
- Softkey text must stay white and centered inside the decoded bottom cells.
  Earlier regressions made this red or vertically off-center.

## Verification And Closeout

Focused smoke was rerun after this audit:

```powershell
cd ..\rebuild_game
.\build.ps1
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-suite world_panel_full build_intro_demo\gamesystem_settings_reentry_383
```

Result: `PASS`, `98/98`.

Output directory:

```text
rebuild_game/build_intro_demo/gamesystem_settings_reentry_383
```

After the small confirm parity patch, the suite was rerun:

```powershell
cd ..\rebuild_game
.\build.ps1
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-suite world_panel_full build_intro_demo\gamesystem_settings_confirm_383
```

Result: `PASS`, `99/99`.

Output directory:

```text
rebuild_game/build_intro_demo/gamesystem_settings_confirm_383
```

Important PNGs:

- `panel_gamesystem_click_softkey_open.png`
- `panel_gamesystem_navigation.png`
- `panel_gamesystem_settings_open.png`
- `panel_gamesystem_settings_adjust_right.png`
- `panel_gamesystem_settings_confirm_returns_gamesystem.png`
- `panel_gamesystem_settings_back_returns_gamesystem.png`

Implemented in this slice:

1. `VqsvPanelRuntime.tickSettings()` confirm now returns to
   `gamesystem.ui selected=2`.
2. The trace records source-shaped `game.g.B().k=<level>` save behavior.
3. `VqsvSmokeHarness` now includes
   `panel_gamesystem_settings_confirm_returns_gamesystem`.

## Next Concrete Step

The settings row is now closed at `PORTED/PARTIAL` for current panel-route
purposes. Next choices, in roadmap order:

1. inspect `gamesystem.ui` row 3 `option.ui` confirm-yes reset with a guarded
   source audit before enabling destructive reset;
2. or return to the right-softkey `gamemenu.ui` functions still deeper than UI
   shell, such as petmap details or remaining bag special rows.
