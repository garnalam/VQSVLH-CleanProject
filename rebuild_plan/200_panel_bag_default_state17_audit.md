# 200 - Panel Bag Default Item Use State 17 Audit

Date: 2026-07-10

Scope: source audit only. No runtime patch in this step.

## Target question

Audit top-level `bag.ui` default item-use path:

- where state 17 enters from `game.k`;
- what UI it opens;
- how `game.h.s` carries `itemId`;
- confirm/back behavior.

## Source entry chain

### `game.h.ac()` top-level bag confirm

Source: `modules/source_code/decoded/decompiled_source_cfr/game/h.java`

In `game.h.ac()`, bag tab `b == 0` chooses from:

- `q.K` if cursor `h < q.K.size()`;
- otherwise `q.J` at `h - q.K.size()`.

Special source branches already split before state 17:

- item ids `0,1,2,3`: open `/data/ui/msgwarm.ui`, text `Khong the su dung`, no mutation.
- item id `14`: egg accelerator path.
- item id `13`: avoid-monster path.
- default: this is state 17.

Default branch:

```java
this.s = v1[0];
this.o.a((byte)17);
this.p.a("/data/ui/bag.ui");
```

Status: PORTED/PARTIAL in rebuild for ids `0..3`, `13`, `14`; PENDING for this default state 17 path.

## `this.s = itemId`

`this.s` is the selected item id from the source bag row `v1[0]`.

It is not a pet index and not a UI cursor. It is later consumed by state 17:

- validation: selected pet calls `pet.x(this.s)`;
- mutation: selected pet calls `pet.w(this.s)`;
- inventory check/remove: `q.b(this.s, 1, (byte)0)` and `game.g.o().d(this.s, 1, (byte)0)` through `pet.w`.

Status: source-proven.

## `game.k` dispatcher entry/update

Source: `modules/source_code/decoded/decompiled_source_cfr/game/k.java`

Entry dispatcher:

```java
case 17: {
    this.S.l = false;
}
case 18:
case 19: {
    this.S.c = 0;
    this.S.W();
    break;
}
```

Important: case 17 intentionally falls through into 18/19 entry.

State 17 entry effects:

- `S.l = false`;
- `S.c = 0`;
- call `S.W()`.

Update dispatcher:

```java
case 17: {
    this.S.Z();
    break;
}
case 18: {
    this.S.aa();
    break;
}
case 19: {
    this.S.ab();
    break;
}
```

Status: source-proven.

## `game.h.W()` UI entry

Source: `game.h.W() -> game.h.e(int)`.

`W()`:

```java
this.b = 0;
this.e(this.c);
```

`e(int)` opens:

```java
this.p.a("/data/ui/petstate.ui", 257, this);
```

Then it refreshes pet rows and action labels. For `game.k` owner:

- fills six pet rows from `q.z[i]`;
- empty rows become `#P0`;
- if owner state `Q == 16`, widget 64 text becomes `Goi lai`;
- widgets 75 and 76 are hidden;
- selected pet index is applied to widget 0 scroll state;
- `g = true`.

Conclusion: state 17 uses `petstate.ui` as the target-pet picker. It does not open `choice.ui` on entry.

Status: source-proven.

## State 17 update: `game.h.Z()`

Source:

```java
public final void Z() {
    if (this.f == 0 && this.o.k(4100)) {
        this.p.a.b(0);
        this.f(this.c);
        return;
    }
    if (this.f == 0 && this.o.k(8448)) {
        this.p.a.b(1);
        this.f(this.c);
        return;
    }
    if (this.o.k(196640)) {
        this.bo();
        return;
    }
    if (this.f == 0 && this.o.k(262144)) {
        this.o.a((byte)8);
        this.p.a("/data/ui/petstate.ui");
    }
}
```

Key meaning in current rebuild naming:

- `4100`: up/navigation.
- `8448`: down/navigation.
- `196640`: confirm.
- `262144`: back.

Back behavior:

- only works when `f == 0`;
- switches owner to state 8;
- closes `/data/ui/petstate.ui`.

Confirm behavior:

- always calls `bo()`;
- when a warning/success `msgwarm.ui` is visible, confirm is also the close/continue action.

Status: source-proven.

## State 17 confirm core: `game.h.bo()`

### First confirm, `f == 0`

`bo()` sets `f = 1`, then validates selected pet against item `s`:

```java
int i1 = this.o instanceof k
    ? this.q.z[this.c].x(this.s)
    : this.q.z[((d)this.o).f[this.c]].x(this.s);
```

For panel/world owner, selected pet is `q.z[this.c]`.

Validation result warnings:

- `0`: `Sung vat nay da tu vong, khong the su dung`
- `1`: `Sung vat nay khong co, khong the su dung`
- `2`: `Mau day, khong can su dung`
- `3`: `Ky nang gia tri da day, khong can su dung`
- `4`: `Tren nguoi deu bi loi hieu qua`
- `5`: `Trong hung phan, khong the dung`
- `7`: `Mau va ky nang deu da day, khong can su dung`
- `8`: `Sung vat da chet, khong the su dung`

Each warning opens:

```java
this.p.a("/data/ui/msgwarm.ui", 257, this);
```

and keeps `f == 1`.

### Success

If validation returns `-1`, source checks inventory:

```java
if (this.q.b(this.s, 1, (byte)0)) {
    this.q.z[this.c].w(this.s);
    this.e(this.c);
    this.f = 1;
    this.l = true;
    this.p.a("/data/ui/msgwarm.ui", 257, this);
    this.a("Thanh cong su dung dao cu", "Nhan nut 5 de tiep tuc");
    return;
}
```

Effects:

- `game.b.w(itemId)` applies item to selected pet;
- inventory is reduced inside `game.b.w` via `game.g.o().d(itemId, 1, (byte)0)`;
- `petstate.ui` is refreshed via `e(this.c)`;
- `l = true`;
- `msgwarm.ui` success message opens.

### Missing item count

If inventory check fails:

```java
this.f = 2;
this.E();
this.a("Da khong co dao nay cu, thinh mua sam", "Nhan nut 5 de tiep tuc");
```

This uses the warning/message runtime from `E()`/`a(...)`.

### Confirm after message

If `f == 1`:

- close `/data/ui/msgwarm.ui`;
- set `f = 0`;
- stay in `petstate.ui`.

If `f == 2`:

- close `/data/ui/msgwarm.ui`;
- close `/data/ui/petstate.ui`;
- for `game.k` owner, switch to state 8.

Status: source-proven.

## `game.b.x(itemId)` validation

Source: `modules/source_code/decoded/decompiled_source_cfr/game/b.java`.

Validation is based on item behavior `aq.c[4][itemId][5]`.

Global first check:

- if pet is not alive and behavior is not revive behavior `4`, return `8`.

Behavior mapping:

- `0`: return `6` (not handled explicitly by `bo()` warning switch; should not normally be state 17 default-use behavior).
- `1`: HP item; if HP already full return `2`, else OK `-1`.
- `2`: skill/PP item; if all skill values full return `3`, else OK `-1`.
- `3`: HP + skill item; if both full return `7`, if either needs refill OK `-1`.
- `4`: revive item; if pet alive return `1`, else OK `-1`.
- `5`: clear bad effects; if no debuff/effect present return `4`, else OK `-1`.
- `6`: excitement/buff item; if already active return `5`, else OK `-1`.

Status: source-proven.

## `game.b.w(itemId)` mutation

Source applies by item behavior:

- `1`: heal HP by `% maxHP + flat`, record heal amount.
- `2`: restore skill/PP by source value.
- `3`: heal HP and restore skill/PP.
- `4`: clear death/down state, revive with HP/skill values.
- `5`: clear bad effects.
- `6`: set source state flag `d[6] = 2`.

Then:

```java
game.g.o().d(itemId, 1, (byte)0);
```

Status: source-proven.

## UI summary

State 17 source UI lifecycle:

1. `bag.ui` default item confirm closes `bag.ui`.
2. `petstate.ui` opens as target pet picker.
3. navigation changes selected pet row and refreshes pet details.
4. confirm opens `msgwarm.ui` for warning/success, or mutates pet + inventory and refreshes `petstate.ui`.
5. back from clean `petstate.ui` returns to state 8.

State 17 does not use `choice.ui`.

`choice.ui` is used by other item-use paths:

- battle item list;
- petsetting `c=0` item-use choice path already implemented separately in rebuild.

## Rebuild current gap

Current rebuild has:

- PORTED/PARTIAL: top-level bag ids `0..3` cannot-use warning.
- PORTED/PARTIAL: top-level bag item 13 avoid-monster.
- PORTED/PARTIAL: top-level bag item 14 egg accelerator.
- PORTED/PARTIAL: petsetting `c=0 -> choice.ui` item-use validation/success loop using source-shaped `game.b.x/w`.
- PENDING: top-level `bag.ui` default item-use `this.s=itemId -> P=17 -> petstate.ui`.

Current missing slice:

- opening state 17 target `petstate.ui` from top-level bag default item row;
- storing `this.s` equivalent as selected bag item id;
- state 17 navigation/back path;
- state 17 confirm warning/success loop;
- state 17 inventory count failure `f=2` return-to-bag behavior.

## Recommended next implementation slice

Smallest safe slice:

1. Add panel mode for bag default state 17 target selection.
2. From `bag.ui b=0` default item row, store source-selected item id and open source-backed `petstate.ui`.
3. Implement navigation and back only.
4. Add PNG smoke:
   - `panel_bag_default_item_state17_open_petstate`
   - `panel_bag_default_item_state17_navigation`
   - `panel_bag_default_item_state17_back_returns_bag`

Next slice after that:

1. Confirm validation warnings using existing `BattleUnit.validateBattleItem()` / source-shaped `game.b.x`.
2. Confirm success mutation using existing `applyBattleItem()` / `VqsvSourceOps.sourceRemoveItem`.
3. Add PNG smoke:
   - dead pet warning;
   - HP full warning;
   - item missing count warning returns bag;
   - heal success refreshes petstate and decrements count.

Do not mix this with `choice.ui`, ride, egg, or panel speed toggle.
