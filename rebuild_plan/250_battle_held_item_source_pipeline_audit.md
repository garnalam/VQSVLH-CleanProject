# 250 - Battle Held Item Source Pipeline Audit

Date: 2026-07-13

Scope: source-first audit for `aq.c[3][0..11]` held items. This is the shared
pipeline we should lock before coding the remaining held-item/passive effects.

No runtime code was changed by this audit.

## Terminology

Use:

```text
held item / pet-held passive
```

Do not use:

```text
form/status
normal item
temporary buff/debuff
```

Source UI itself calls this tab:

```text
Vật phẩm trang sức
```

So these rows behave like accessory/held-item passives: owned in inventory, equipped
onto one pet, then battle checks the pet slot.

## Source Data Model

| Source | Meaning |
| --- | --- |
| `aq.c[3][0..11]` | held item rows |
| `aq.c[3][12..17]` | material/key rows, not held battle items |
| `game.g.L` / `q.L` | inventory list for held items id `< 12` |
| `game.g.M` / `q.M` | stack list for material/key id `>= 12` |
| `game.b.c[5]` | equipped held item id on a pet, `-1` means none |
| `game.b.f(byte)` | source check: `return this.c[5] == by;` |

Important source facts:

- `game.g.a(id, qty, by=2)` checks capacity/ownership.
- `game.g.c(id, qty, by=2)` adds held item/material.
- If `id < 12`, source adds `int[]{id, 0, 0}` to `q.L`.
- If `id >= 12`, source adds stack rows to `q.M`.
- `game.b.P()` serializes `nArray[2] = this.c[5]`.
- `game.b.a(int[] payload)` restores payload index `2` into `c[5]`.

## Acquisition / Reward Flow

### Event opcode 18

Source: `game.c` case `18`.

```text
if add:
    x.a(itemId, qty, by=2)
    show "Đạt được: " + aq.c[3][itemId][0]
    x.c(itemId, qty, by=2)
if remove:
    show "Mất: " + aq.c[3][itemId][0]
    x.d(itemId, qty, by=2)
```

Meaning: event opcode `18` is the world/event route that awards or removes rows from
the shared `aq.c[3]` table.

### Chest / reward object

Source: `game.g.v()`.

If reward category `E == 2`, source displays name from `aq.c[3][F][0]` and stores
through the same `by=2` inventory route.

## Equip / Unequip Flow

### Opening the held-item tab

Source: `game.h`, petsetting case `2`.

The source opens:

```text
/data/ui/choice.ui
/data/ui/petsetting.ui
/data/ui/petstate.ui
```

Then sets:

```text
widget 8 = "Vật phẩm trang sức"
widget 9 = "Trạng thái"
action softkey = "Mang theo"
```

This is the strongest source proof that `aq.c[3][0..11]` should be treated as
held/equipment-like items.

### List rendering

Source helper: `game.h.bd()`.

| UI part | Source behavior |
| --- | --- |
| list size | `q.L.size()` |
| row icon | sprite `258`, cell `aq.c[3][id][1]` |
| row name | `an.f(aq.c[3][id][0])` |
| row status | `"Đã mang theo"` if selected pet `c[5] == id`; `"Bị mang theo"` if `q.L` row worn flag is `1`; otherwise empty |
| description | widget `53` = `an.f(aq.c[3][selectedId][2])` |
| action key | `"Dỡ xuống"` if selected pet already wears it, else `"Mang theo"` |

### Confirm behavior

Source: `game.h`, action key `196640` while `f == 2`.

If selected item is already equipped on selected pet:

```text
q.l(pet.c[5])
pet.c[5] = -1
show "Thành công dỡ xuống"
```

If selected item is not equipped on selected pet:

```text
q.f(itemId, selectedPetIndex)
show "Thành công mang theo"
```

### Equip implementation

Source: `game.g.f(itemId, petIndex)`.

Behavior:

1. If selected pet already has `c[5] >= 0`, mark old held item as not worn via `l(oldId)` and clear `c[5]`.
2. If the target held item is already worn somewhere else, source clears that old owner:
   - party pet: clear that pet's `c[5]`;
   - bank payload: clear payload index `2`.
3. Mark the `q.L` row as worn by setting row `[1] = 1`.
4. Set selected pet `c[5] = itemId`.

So one held item can only be equipped by one pet at a time.

## Petstate Display

Source petstate detail uses widgets `59/60`.

### Runtime party pet

Source: `game.h` detail fill for `game.b[]`.

```text
if pet.c[5] != -1:
    widget 59 sprite 258 cell aq.c[3][pet.c[5]][1]
    widget 60 text an.f(aq.c[3][pet.c[5]][0])
else:
    widget 59 cell 0
    widget 60 empty
```

### Bank/serialized pet

Source uses payload index `2` similarly:

```text
if payload[2] != -1:
    widget 59 sprite 258 cell aq.c[3][payload[2]][1]
    widget 60 text an.f(aq.c[3][payload[2]][0])
```

Therefore any rebuild petstate renderer must read `aq.c[3]`, not `aq.c[4]`.

## Battle Activation

Battle does not activate held items through a generic event queue. Each held item is
checked by explicit source hooks, mostly `game.b.f((byte)id)`.

| Id | Name | Source consumer | Battle meaning | Status |
| ---: | --- | --- | --- | --- |
| 0 | Mạn Đà La Thạch | `game.b.B()`, `game.b.e(2)` | if HP <= 30%, attack part +100% before defense | PORTED |
| 1 | Hồng Sắc Hải Loa | `game.b.B()`, `game.b.e(2)` | attack +10% before defense | PORTED/PARTIAL |
| 2 | Quy Xác Toái Phiến | `game.b.B()`, `game.b.e(3)` | target defense +15% in damage/stat display path | PORTED/PARTIAL |
| 3 | Ô Nha Uế | `game.b.b(target)` | reduces incoming debuff apply chance by 20% | PORTED/PARTIAL |
| 4 | Viễn Cổ Long Cốt | `game.b.b(target)` | crit chance +10 percentage points | NEXT |
| 5 | Mật Phong Sào | `game.d` EXP award | participant EXP x120% | PORTED/PARTIAL |
| 6 | Ký Cư Giải Xác | `game.d` reserve EXP branch | reserve/nonparticipant EXP share | PORTED/PARTIAL |
| 7 | Linh Trùng Thi Hài | `game.d` turn-order setup | source moves this unit into special order slot; exact design meaning still needs focused smoke | PENDING |
| 8 | Hấp Huyết Đằng Mạn | `game.d.q()` post-hit | after hit, roll <= 10, heal damage * 20% | PORTED/PARTIAL |
| 9 | Cá Thờn Bơn | `game.d` P7 miss path | miss chance forced to 0 | PORTED |
| 10 | Cảm Lãm Chi Diệp | `game.d` P7/U path | if source condition hits, target HP floor 10 | PORTED/PARTIAL |
| 11 | Sủng vật lôi đạt | `game.d.b(itemId)` catch chance | active/player pet boosts catch chance: `12/10`, then `120/100`, about `1.44x` before cap/clamp | PORTED/PARTIAL |

## Rebuild Mapping

Current rebuild has a legacy naming mismatch:

| Source | Current rebuild |
| --- | --- |
| `game.b.c[5]` held item slot | `BattleUnit.STAT_FORM` / `baseStats[STAT_FORM]` |
| `game.b.f(byte)` | `BattleUnit.hasFormStatus(byte)` |
| `aq.c[3]` held item table | `VqsvBattleTables.status(id)` wrapper, because group 3 was historically named status |

This naming should be cleaned later, but behavior can remain stable if documented:

```text
STAT_FORM actually mirrors source c[5] held item/passive id.
BattleStatusRow for group 3 should be renamed or wrapped as BattleHeldItemRow.
```

## Rebuild Gap Found And Fixed

`VqsvBattlePetStateView.fromPet()` currently reads the held item using:

```java
BattleItemRow heldItem = heldItemId >= 0 ? VqsvBattleTables.instance().item(heldItemId) : null;
```

That is wrong for source parity because `item()` reads `aq.c[4]`, while held items use
`aq.c[3]`.

Fixed in slice `251`:

```text
Added BattleHeldItemRow / VqsvBattleTables.heldItem(id).
VqsvBattlePetStateView widget 59/60 now reads aq.c[3].
VqsvSourceOps sourceEquipmentName/Icon/Description now use the same wrapper.
```

## Standard Workflow For Each Held Item

For every remaining held item, do this order:

1. Audit row from `aq.c[3][id]`: name, icon cell, description, params.
2. Audit source consumer: exact `game.b` / `game.d` hook.
3. Port logic only where source hook exists.
4. Render UI using `aq.c[3]` icon/name in petstate/list.
5. Smoke:
   - equip/list or debug-applied `c[5]=id`;
   - petstate widget `59/60` if UI is in scope;
   - battle frame showing the effect;
   - no HUD status icon unless source also creates buff/debuff queue.

## Immediate Next Code Slice Recommendation

Before held item id `4` crit smoke, fix the shared UI/data wrapper:

```text
Create held item table wrapper for aq.c[3].
Fix VqsvBattlePetStateView held item display to use aq.c[3].
Add one petstate smoke showing Mạn Đà La Thạch icon/name in widget 59/60.
```

Then proceed to:

```text
held item id 4 Viễn Cổ Long Cốt crit +10 percentage points
```
