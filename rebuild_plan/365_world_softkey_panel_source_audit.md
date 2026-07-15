# 365 - World Softkey Panel Source Audit

Date: 2026-07-15

Scope: free-world/map bottom-left and bottom-right UI buttons, their source
state routing, rebuild mouse-click parity, and the concrete panel branches
opened from those buttons.

## Source Chain

Source files read:

- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- `modules/ui/decoded/data__ui__world.ui.json`
- `modules/ui/decoded/data__ui__gamemenu.ui.json`
- `modules/ui/decoded/data__ui__gamesystem.ui.json`
- `modules/ui/decoded/data__ui__record.ui.json`
- `modules/ui/decoded/data__ui__petmap.ui.json`

`game.k.c()` opens `/data/ui/world.ui`.

`world.ui` relevant widgets:

| Widget | Position | Cell | Meaning in rebuild |
| --- | --- | --- | --- |
| `7` | `x=0 y=303 w=18` | `175` | left softkey icon |
| `5` | `x=222 y=303 w=16` | `68` | right softkey icon |

Source panel open methods:

- `game.k.k()` opens `/data/ui/gamemenu.ui`.
- `game.k.m()` opens `/data/ui/gamesystem.ui`.

`gamemenu.ui` rows (`game.k.k/l`, source state `P=6`):

- optional shop row when `a.a.i` is true.
- `Sung vat` / pet state.
- `Lung bao` / bag.
- `Do giam` / record and pet map.
- `Nhiem vu` / task.
- `Luu du lieu` / save.

`gamesystem.ui` rows (`game.k.m/n`, source state `P=13/14`):

- `Tiep tuc tro choi` / continue.
- `Tro giup choi` / help.
- `Thiet lap tro choi` / settings.
- `Tro lai menu chinh` / main-menu confirmation.

Important clarification: the map's two corner buttons are not both inventory
buttons. The right softkey opens `gamemenu.ui`, which then contains pet, bag,
record, task, and save. The left softkey opens `gamesystem.ui`, which contains
continue/help/settings/main-menu.

## Rebuild Mapping

Files:

- `rebuild_game/src/main/java/VqsvWorldActors.java`
- `rebuild_game/src/main/java/VqsvIntroDemo.java`
- `rebuild_game/src/main/java/VqsvPanelRuntime.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Implemented/verified behavior:

- `WorldUi.render()` draws source `world.ui` widgets `7` and `5`.
- Clicking left softkey opens `VqsvPanelRuntime.openGameSystemFromWorld()`.
- Clicking right softkey opens `VqsvPanelRuntime.open(...)` / `gamemenu.ui`.
- Rebuild keeps the source widget hitboxes and also accepts PC-friendly
  bottom-corner hitboxes:
  - left: `x <= 48 && y >= 288`
  - right: `x >= 188 && y >= 288`
- This is a `PC_QOL` hitbox expansion only; it does not change source route
  state or menu content.

## Status Matrix

| Area | Status | Note |
| --- | --- | --- |
| `world.ui` icons | `PORTED` | source widgets `7/5`, cells `175/68`. |
| Left softkey -> system menu | `FIXED/PORTED/PARTIAL` | Opens `gamesystem.ui`; generic source input engine is not fully ported. |
| Right softkey -> game menu | `FIXED/PORTED/PARTIAL` | Opens `gamemenu.ui`; source row behavior remains per implemented panel slices. |
| Gamemenu save row -> prompt | `PORTED/PARTIAL` | Routes to `P=22` style save prompt through rebuild save runtime. |
| Gamemenu pet row -> `petstate.ui` | `PORTED/PARTIAL` | Opens source-backed petstate view from current source pet state; deeper petsetting slices are handled elsewhere. |
| Gamemenu bag row -> `bag.ui` | `PORTED/PARTIAL` | Opens item list, hover preview, wheel scroll, and back path; item mutations are per existing item slices. |
| Gamemenu task row -> `task.ui` | `PORTED/PARTIAL` | Opens task tabs and navigation. Fixed title/status overlap; exact source marquee/crop is still not fully ported. |
| Gamemenu record row -> `record.ui` | `FIXED/PORTED/PARTIAL` | Opens record stats, c=0 petmap branch, back path. Rebuilt lower option buttons from source widget positions so only one cursor is visible. |
| `record.ui` c=0 -> `petmap.ui` | `PORTED/PARTIAL` | Opens petmap list, tab navigation, wheel/hover/click viewport mapping. Entry details remain pending. |
| `record.ui` c=1 badge branch | `PENDING` | Source maps this to badge UI; rebuild currently traces pending and does not open badge page yet. |
| `gamesystem.ui` help/settings/option | `PORTED/PARTIAL` | Open/navigate/back/confirm-no verified. Confirm-yes reset remains guarded by separate source audit. |
| Keyboard softkey parity | `PARTIAL` | ESC/back remains PC convenience for gamemenu; exact phone key mapping is not fully claimed. |

## PNG Audit Notes

Representative screenshots inspected from
`rebuild_game/build_intro_demo/world_panel_full_final`:

- `world_softkey_icons_render.png`: source `world.ui` corner icons render on map.
- `panel_gamemenu_open_from_world.png`: right softkey opens `gamemenu.ui`.
- `panel_gamesystem_click_softkey_open.png`: left softkey opens `gamesystem.ui`.
- `panel_petstate_open_from_gamemenu.png`: petstate opens with source pet state.
- `panel_bag_open_from_gamemenu.png`: bag opens with item list, tabs, and source-style softkey labels.
- `panel_task_open_from_gamemenu.png`: task list opens; fixed row-title overlap, but source-width status text still crops.
- `panel_petmap_record_open_from_gamemenu.png`: record page opens; fixed double-cursor visual issue in lower option area.
- `panel_petmap_open_from_record.png`: petmap list opens and routes back to record.
- `panel_save_prompt_from_gamemenu.png`: save prompt opens through panel save flow with bottom confirm/cancel affordances.

Visual caveats:

- `record.ui` lower option buttons are source-positioned and logic-shaped, but
  not pixel-perfect source-client cell playback because original source
  selected cells embed cursor art in a way that made both cursors visible in the
  rebuild renderer.
- `task.ui` status text is clipped instead of fully marquee-scrolled like the
  source runtime.
- Generic source UI VM/input engine is not fully ported; this slice verifies
  concrete route branches only.

## Verification

Focused commands:

```powershell
cd rebuild_game
.\build.ps1
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-suite world_panel_full build_intro_demo\world_panel_full_final
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-suite panel_wheel build_intro_demo\panel_wheel_final
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-suite battle_quick build_intro_demo\battle_quick_after_world_panel_final
java "-Dvqsv.modules=..\modules" -cp build\classes com.vqsv.rebuild.Main --check
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvBattleDamageFormulaCheck
```

Latest verification:

- `.\build.ps1`: PASS.
- `world_panel_full`: PASS, `42/42`, output
  `rebuild_game/build_intro_demo/world_panel_full_final`.
- `panel_wheel`: PASS, `8/8`, output
  `rebuild_game/build_intro_demo/panel_wheel_final`.
- `com.vqsv.rebuild.Main --check`: PASS.
- `VqsvBattleDamageFormulaCheck`: PASS.
- `battle_quick`: PASS, `235/235`, output
  `rebuild_game/build_intro_demo/battle_quick_after_world_panel_final`.
- Mojibake scan, source-specific pattern for replacement characters and common
  Java-source mojibake sequences: PASS, no hits.

## Next Recommended Step

After this slice, the next small panel route is `record.ui` c=1 -> `badge.ui`
open/render/back, because the source branch is known and the current rebuild
still marks it `PENDING`. If staying in route-order story polish instead,
continue with `op35 option.ui` starter confirm, then `npcEnemy.ui` Elder battle
entry timeline. Do not broaden the panel VM unless a concrete source route
requires it.
