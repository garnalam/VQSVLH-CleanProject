# 397 - Panel Bag Tab1 Trang Suc Render Parity

Date: 2026-07-15

## Scope

Port/render the top-level `bag.ui` tab 1 `Trang suc` only.

This slice is render/navigation/back parity. It intentionally does not port equip/unequip action here, because the source-shaped equip flow already lives under `petsetting.ui` row 2.

## Source Anchors

- Source runtime: `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- Open bag: `game.k.ab()` loads `/data/ui/bag.ui`, sets `b=0`, then `bq()/br()`.
- Tab refresh: `game.k.bq()` resets the list controller at `8 + b * 39`.
- Tab dispatch: `game.k.br()`.
- Tab 1 source data: `q.M`.
- Tab 1 source table: `aq.c[3]` held/equipment rows.

## Source Widget Mapping

For `b == 1`, source uses the second list container:

| Purpose | Source widget ids |
|---|---|
| List controller/container | `47` |
| Row backgrounds | `58, 63, 68, 73, 78` |
| Row icon | `59, 64, 69, 74, 79` |
| Row name | `60, 65, 70, 75, 80` |
| Row status | `61, 66, 71, 76, 81` |
| Description text | `85` |
| Scrollbar track/thumb | `83 / 84` |

Source status text:

- `q.M[row][1] == 1` -> `Da mang theo`
- otherwise empty

Source description:

- `aq.c[3][equipmentId][2]`

## Rebuild Changes

- `VqsvPanelRuntime` now renders bag tab 1 from `Scene.sourceEquipmentItems`, treated as source `q.M`.
- Equipment metadata uses `VqsvBattleTables.heldItem(id)` / `aq.c[3]` through `VqsvSourceOps.sourceEquipment*`.
- Tab 1 row icon/name/status use widgets `59/60/61 + i*5`.
- Tab 1 description uses widget `85`.
- Tab 1 scrollbar uses widgets `83/84`, with thumb position based on selected row like source.
- Long equipment names are clipped/marquee-rendered inside the name widget, so they do not overwrite `Da mang theo`.
- Mouse hover/click row hitboxes now use the row widget set for the current bag tab.
- Confirm on tab 1 is explicitly render-only/no-op and traces the selected equipment; it does not open item state17.
- Added smoke suite alias: `--smoke-suite panel_bag`.

## Smoke Evidence

Output directory:

`C:\Users\Dell\Downloads\ResourcesVQSV\rebuild_game\build_intro_demo\panel_bag_tab1_397`

Passed checkpoints:

- `panel_bag_open_from_gamemenu`
- `panel_bag_navigation`
- `panel_bag_hover_preview_no_confirm`
- `panel_bag_back_returns_gamemenu`
- `panel_bag_tab1_trang_suc_open`
- `panel_bag_tab1_trang_suc_navigation`
- `panel_bag_tab1_trang_suc_confirm_noop`
- `panel_bag_tab1_trang_suc_back`

Regression:

- `--check`
- `--smoke-suite panel_petsetting_equipment` passed `9/9`

Reference PNG:

`C:\Users\Dell\Downloads\ResourcesVQSV\rebuild_game\build_intro_demo\panel_bag_tab1_397\panel_bag_tab1_trang_suc_open.png`

## Status

`bag.ui` tab 1 `Trang suc`: PORTED/PARTIAL.

Complete for:

- source-backed data binding from `q.M` proxy
- icon/name/status/description render
- row navigation
- hover/click hitboxes
- back flow
- no accidental item-use/equip mutation from top-level bag

Remaining:

- Full generic `bag.ui` multi-container widget runtime is still not 100%.
- Tab 2 `Tai lieu` remains the next bag rendering gap.
- Equip/unequip remains owned by `petsetting.ui` row 2 and was regression-checked, not duplicated in top-level bag.

## Next Roadmap Step

Next recommended slice:

`398 - Panel Bag Tab2 Tai Lieu Render Parity`

Scope should mirror 397:

- Audit `game.k.bt()` source branch.
- Render `q.N` material/key rows from `aq.c[3]`.
- Use tab 2 widget family `96/101/106/111/116` etc. and its description/scrollbar widgets.
- Only render/navigate/back first; do not port special q.N actions in the same slice.
