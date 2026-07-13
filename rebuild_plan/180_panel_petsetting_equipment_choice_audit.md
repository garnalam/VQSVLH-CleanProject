# 180 - Panel petsetting c=2 equipment choice.ui audit

Date: 2026-07-10

## Target

Audit and prepare a small source-backed slice for:

- `petsetting c=2 -> /data/ui/choice.ui`
- source `q.L` equipment/accessory list
- selected pet equipment slot mapping
- render/navigate/back first

No equip/unequip mutation should be implemented until this render slice is verified.

## Source Proof

Primary source:

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`

### Open Path

In `game.h.X()`, while `f == 1`, `o.Q == 6 || o.Q == 0`, confirm on `c == 2`:

- calls `o.m()`
- sets `f = 2`
- sets `r = 0`
- opens `/data/ui/choice.ui`
- closes `/data/ui/petsetting.ui`
- closes `/data/ui/petstate.ui`
- widget `8 = "Vat pham trang suc"`
- widget `9 = "Trang thai"`
- world owner `game.k` uses alternate softkeys:
  - hide widget `5/6`
  - show widget `59/60`
  - widget `59 = "Mang theo"`
- calls `bd()`

### q.L Vector

In `game.g`:

- `c(itemId, qty, (byte)2)` handles equipment acquire.
- If `itemId >= 12`, it adds/stacks in `M`.
- If `itemId < 12`, it appends an entry to `L`:
  - `int[] { itemId, 0, 0 }`
- `a(itemId, qty, (byte)2)` checks uniqueness:
  - for `itemId < 12`, returns false if `L` already contains that item id.
- `l(itemId)` marks an equipment row as not worn:
  - finds `L` row by id
  - sets row `[1] = 0`
- `f(itemId, petIndex)` equips an item:
  - if selected pet already has `c[5] >= 0`, calls `l(oldItem)` and clears old slot
  - if target item is already worn by another pet, calls `l(itemId)` and clears that other pet's `c[5]`
  - sets matching `L` row `[1] = 1`
  - sets `z[petIndex].c[5] = itemId`

So `q.L` row shape for this slice is:

- `[0]`: equipment id
- `[1]`: worn flag, `1` means worn by some pet
- `[2]`: present but not used by the visible choice render path

### Pet Slot

In `game.b`:

- constructor initializes source payload through `a(...)`
- `c[5]` is assigned from the third constructor/source payload argument.
- `P()` persists the pet and stores `nArray[2] = c[5]`.

So selected pet equipment slot is:

- `q.z[b].c[5]`
- rebuild source payload index `2`

### choice.ui Render

In `game.h.bd()`:

- scroll/list count comes from `q.L.size()`
- selected row id comes from `((int[]) q.L.elementAt(h))[0]`
- row icon from `aq.c[3][id][1]`
- row name from `aq.c[3][id][0]`
- description widget `53` from `aq.c[3][selectedId][2]`
- row status:
  - if selected pet `c[5] == id`: `"Da mang theo"`
  - else if row `[1] == 1`: `"Bi mang theo"`
  - else: empty
- softkey text changes:
  - if selected pet `c[5] == selectedId`: `"Do xuong"`
  - else: `"Mang theo"`

### Back Path

In `game.h.X()` case `c == 2`:

- back while `f == 2`:
  - calls `e(b)`
  - closes `/data/ui/choice.ui`

## Rebuild Mapping Plan

Add a source-shaped equipment list separate from `sourceBagItems`:

- `List<SourceEquipmentItem> sourceEquipmentItems`
- `SourceEquipmentItem.id`
- `SourceEquipmentItem.equippedFlag`

Use selected pet source payload index `2` as the rebuild mirror of source `c[5]`.

Render through the existing source-backed `choice.ui` renderer and `VqsvChoiceUiView`:

- title: `"Vat pham trang suc"`
- subtitle: `"Trang thai"`
- action: `"Mang theo"` or `"Do xuong"` based on selected row and pet payload `[2]`
- row names/icons/descriptions from `VqsvBattleTables.row(3, id)`
- row values from status rules above
- alternate softkeys `59/60` because this is world/panel owner like `game.k`

## Slice Boundary

Allowed now:

- open/render `choice.ui`
- navigate up/down
- back to `petstate.ui`
- smoke seeded `q.L` state:
  - empty list
  - current pet already wearing selected equipment
  - another equipment row with `equippedFlag == true`

Not allowed in this slice:

- mutate equip/unequip
- update another pet's slot
- persist equipment through save
- apply equipment battle stat/passive effects

## Status

- `PORTED/PARTIAL`: source path and data shape are proven.
- `PENDING`: actual equip/unequip mutation `game.g.f/l`.
- `PENDING`: source save/load of `q.L`.
- `PENDING`: battle effects of `aq.c[3]` equipment rows.
- `PENDING`: original-vs-rebuild pixel compare.

## Next

Implement render/navigate/back only, with smoke PNG:

- `panel_petstate_petsetting_equipment_choice_open`
- `panel_petstate_petsetting_equipment_choice_navigation`
- `panel_petstate_petsetting_equipment_choice_back_returns_petstate`
- `panel_petstate_petsetting_equipment_choice_statuses`

Then run panel regressions and route regressions.
