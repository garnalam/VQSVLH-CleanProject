# 177 - Panel petsetting choice.ui item/equipment audit

Date: 2026-07-10

## Scope

Source audit only for the next `petsetting.ui` subflows:

- `petsetting c=0 -> choice.ui` item-use list.
- `petsetting c=2 -> choice.ui` equipment/accessory list.

No runtime code is changed in this audit.

## Primary Source

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/ui/decoded/data__ui__choice.ui.json`

## Shared UI: choice.ui

Both branches open:

- `/data/ui/choice.ui`, sprite `257`
- close `/data/ui/petsetting.ui`
- close `/data/ui/petstate.ui`

Source mutates the same widget set differently per branch:

- title headers: widget `8`, widget `9`
- normal softkeys: widget `5`, widget `6`
- world/k softkeys: widget `59`, widget `60`
- row icons: widgets `54..58`
- row names/status/count: widgets `13 + i * 5`, `14 + i * 5`
- description: widget `53`
- scrollbar thumb: widget `51`

Source uses the `game.k` owner path in panel/world:

- hides widgets `5/6`
- shows widgets `59/60`
- changes widget `59` text to the active command.

## Branch c=0: item-use list

Source open path in `game.h.X()`:

- `f = 2`
- `r = 0`
- open `/data/ui/choice.ui`
- close `petsetting.ui`
- close `petstate.ui`
- widget `8 = "Đạo cụ"`
- widget `9 = "Số lượng"`
- command text = `"Sử dụng"`
- calls `be()`

Source list helper `be()`:

- list source is `q.J`
- if `q.J.size() > 5`, list scroll mode enabled
- selected cursor from list widget `0`: `w = e`, `h = f`
- clamps selected index when it exceeds `q.J.size() - 1`
- each visible row:
  - `int[] row = q.J[w + i]`
  - `row[0]` = item id
  - `row[1]` = count
  - icon widget `54 + i` uses `aq.c[4][itemId][1]`
  - name widget `13 + i * 5` uses `an.f(aq.c[4][itemId][0])`
  - count widget `14 + i * 5` uses `row[1]`
- description widget `53` uses `an.f(aq.c[4][selectedItem][2])`
- scrollbar widget `51` y = `98 + h * 72 / q.J.size()`

Confirm path for `c=0`:

- up/down call `p.a.b(0/1)` while `f == 2`
- confirm only proceeds if `q.J.size() > 0`
- selected item: `int[] item = q.J[r]`
- item ids `13` and `14` are forbidden here:
  - opens `msgwarm.ui`
  - text `"Đạo cụ này không thể sử dụng"`
  - `f = 3`
- otherwise validates selected pet with `q.z[b].x(itemId)`
- validation result messages:
  - `0`: pet dead strict, cannot use
  - `1`: pet missing, cannot use
  - `2`: HP full
  - `3`: skill/PP full
  - `4`: no debuff/effect to clear
  - `5`: already excited
  - `7`: HP and PP full
  - `8`: pet dead
- success:
  - `q.z[b].w(itemId)`
  - `e(b)` refresh petstate
  - `f = 4`
  - open `msgwarm.ui`
  - text `"Thành công sử dụng đạo cụ"`
  - close `/data/ui/choice.ui`
- after warning `f == 3`, confirm closes `msgwarm.ui` and returns to `f = 2`.
- after success `f == 4`, confirm closes `msgwarm.ui` and returns to `f = 0`.
- back while `f == 2`:
  - calls `e(b)`
  - closes `/data/ui/choice.ui`

Downstream item logic in `game.b`:

- `b.x(itemId)` validates by `aq.c[4][itemId][5]`.
- `b.w(itemId)` applies effect and consumes one item through `game.g.o().d(itemId, 1, (byte)0)`.
- behavior kinds observed:
  - `1`: restore HP by percent plus flat
  - `2`: restore skill/PP
  - `3`: restore HP plus PP
  - `4`: revive/full style path
  - `5`: clear bad effects
  - `6`: excited/status flag

Rebuild status:

- `PORTED/PARTIAL`: battle P4/P16 already ports much of item validation/use.
- `PORTED/PARTIAL`: panel bag tab renders `bag.ui` tab 0.
- `PENDING`: panel `petsetting c=0 -> choice.ui` source path.
- `PENDING`: source `q.J` exact list split from `q.K`, because rebuild currently stores bag entries in `sourceBagItems` with `bagChannel`.

## Branch c=2: equipment/accessory list

Source open path in `game.h.X()`:

- calls `o.m()`
- `f = 2`
- `r = 0`
- open `/data/ui/choice.ui`
- close `petsetting.ui`
- close `petstate.ui`
- widget `8 = "Vật phẩm trang sức"`
- widget `9 = "Trạng thái"`
- command text initially `"Mang theo"`
- calls `bd()`

Source list helper `bd()`:

- list source is `q.L`
- if `q.L.size() > 5`, list scroll mode enabled
- selected cursor from list widget `0`: `w = e`, `h = f`
- clamps selected index when it exceeds `q.L.size() - 1`
- if selected item is already held by selected pet:
  - command text becomes `"Dỡ xuống"`
- otherwise command text becomes `"Mang theo"`
- each visible row:
  - `int[] row = q.L[w + i]`
  - `row[0]` = equipment id
  - `row[1]` = equipped flag
  - icon widget `54 + i` uses `aq.c[3][equipmentId][1]`
  - name widget `13 + i * 5` uses `an.f(aq.c[3][equipmentId][0])`
  - status widget `14 + i * 5`:
    - `"Đã mang theo"` if selected pet `c[5] == equipmentId`
    - `"Bị mang theo"` if `row[1] == 1`
    - empty otherwise
- description widget `53` uses `an.f(aq.c[3][selectedEquipment][2])`
- scrollbar widget `51` y = `98 + h * 62 / q.L.size()`

Confirm path for `c=2`:

- up/down call `p.a.b(0/1)` and then `bd()`
- confirm only proceeds when `q.L.size() > 0`
- selected equipment: `int[] equip = q.L[h]`
- if selected pet already holds this equipment:
  - `q.l(pet.c[5])`
  - `pet.c[5] = -1`
  - refresh `bd()`
  - call `E()`
  - message `"Thành công dỡ xuống"`
  - `f = 3`
- otherwise:
  - `q.f(equipId, b)`
  - refresh `bd()`
  - call `E()`
  - message `"Thành công mang theo"`
  - `f = 3`
- after message:
  - next confirm sets `f = 2`
  - calls `o.m()`
  - `e(b)` refresh petstate
  - `F()`
  - closes `/data/ui/choice.ui`
- back while `f == 2`:
  - calls `e(b)`
  - closes `/data/ui/choice.ui`

Downstream equipment state in `game.g`:

- `q.L` stores equipment rows as `[equipmentId, equippedFlag, 0]`.
- adding equipment through `game.g.c(id, qty, (byte)2)`:
  - for `id < 12`, adds a row to `L`.
  - for `id >= 12`, goes to special/material vector `M`, not this equipment list.
- `q.f(equipmentId, petIndex)`:
  - if pet already has `c[5]`, calls `q.l(oldEquipment)` and clears old held id.
  - if target equipment already equipped elsewhere, clears the previous holder.
  - marks selected equipment row flag to `1`.
  - sets `pet.c[5] = equipmentId`.
- `q.l(equipmentId)` sets matching `q.L` row equipped flag to `0`.

Rebuild status:

- `PENDING`: no dedicated `SourceEquipment` or `sourceEquipmentItems/q.L` model yet.
- `PORTED/PARTIAL`: petstate displays held item from `sourcePayload[2]`, but source equipment actually uses pet `c[5]` and `aq.c[3]`, not `aq.c[4]`.
- `PENDING`: save/load for equipment inventory and held state.
- `PENDING`: equip/unequip mutations and petstate refresh.

## Risk Comparison

`c=0` item-use is the better next code slice if we want quickest progress:

- reuse existing `BagItem`, `BattleItemRow`, and battle item-use validation.
- UI list is the same `choice.ui` row mapping.
- mutation affects pet HP/PP/status and item count, but battle already has source-shaped helpers.

`c=2` equipment is higher risk:

- needs new source-backed equipment inventory model for `q.L`.
- needs correct held item field mapping to source `pet.c[5]`.
- current petstate held-item display appears to use item table group 4 from payload slot 2, which does not match source equipment group 3.

## Recommended Next Slice

Recommended next implementation:

1. Port `petsetting c=0 -> choice.ui` render/navigate/back only.
2. Then add confirm warning/success for one or two source-proven item behaviors already present in battle item runtime.
3. Defer `c=2` until a small audit/fix maps source equipment `q.L` and pet `c[5]` into rebuild state/save/petstate.

Smoke plan after `c=0` render slice:

- `panel_petsetting_item_choice_open`
- `panel_petsetting_item_choice_navigation`
- `panel_petsetting_item_choice_back_returns_petstate`
- `panel_petsetting_item_choice_empty_or_no_items`
- panel regressions: petstate, skill, bag, save, system option

Smoke plan after `c=2` later:

- `panel_petsetting_equipment_choice_open`
- `panel_petsetting_equipment_choice_navigation`
- `panel_petsetting_equipment_choice_back_returns_petstate`
- `panel_petsetting_equipment_equip_success`
- `panel_petsetting_equipment_unequip_success`
- `world_petstate_ui_held_equipment_refresh`
