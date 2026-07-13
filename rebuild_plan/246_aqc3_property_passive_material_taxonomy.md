# 246 - aq.c[3] Property / Passive / Material Taxonomy

Date: 2026-07-13

Scope: clarify what `aq.c[3]` really is. It must not be treated as a pure
"ability table".

No runtime code was changed by this audit.

## Short Answer

`aq.c[3]` is a shared **property / held passive / special material table**.

It contains three different kinds of rows:

1. `0..11`: pet-held property/passive rows. Some have battle consumers.
2. `12..16`: evolution/mutation material rows.
3. `17`: key/special item row.

The original game reuses the same table for icons/text in multiple UI screens,
which is why material rows like `Hồn Tinh Thạch` and `Chìa khóa` appear beside
battle-passive rows like `Mạn Đà La Thạch`.

## Source Facts

### Pet carries one `aq.c[3]` property

Source: `game.b.f(byte)`.

```java
public final boolean f(byte by) {
    return this.c[5] == by;
}
```

Source UI shows the pet's current property:

- `game.h.java:619-621`: decoded pet data `v1[2]` uses `aq.c[3][v1[2]]`.
- `game.h.java:1862-1864`: runtime pet `v1[i].c[5]` uses `aq.c[3][c[5]]`.

So `c[5]` is the pet's carried property/passive id.

### `q.L` is the list for ids `< 12`

Source: `game.g.a/c(..., by=2)`.

```java
case 2:
    if (n2 >= 12) {
        return ... this.M;
    }
    // n2 < 12 goes into this.L
```

Source UI:

- `game.h.java:2436-2462`: `q.L` list renders `aq.c[3][id]`.
- It marks rows as `"Đã mang theo"` / `"Bị mang theo"`.

This means ids `0..11` are inventory/equipment-like pet properties that can be
carried by a pet. They are not ordinary temporary battle buffs.

### `q.M` is the stack list for ids `>= 12`

Source: `game.g.a/c(..., by=2)`.

```java
case 2:
    if (n2 >= 12) {
        return ... this.M;
    }
```

Source UI:

- `game.h.java:2731-2769`: `q.M` list renders `aq.c[3][id]`.
- `id == 17` is shown as `"Chìa khóa vàng"`.

This means ids `12..17` are inventory material/key rows, not battle passives.

### Event/chest rewards can award `aq.c[3]` rows

Source: `game.g.v()`.

```java
if (((a)this.p).E == 0) {
    string = an.f(aq.c[4][F][0]); // normal item table
} else if (((a)this.p).E == 2) {
    string = an.f(aq.c[3][F][0]); // aq.c[3] property/material/key table
}
```

So events/chests can award either normal item rows (`aq.c[4]`) or this shared
`aq.c[3]` table.

## Taxonomy

| Id range | Meaning | Storage/list | Battle meaning |
| --- | --- | --- | --- |
| `0..11` | Pet-held property/passive rows | `q.L`; equipped on pet as `game.b.c[5]` | Only ids with source consumers affect battle/catch/EXP |
| `12..16` | Evolution/mutation materials | `q.M` stack list | No direct battle effect |
| `17` | Key/special row | `q.M` stack list; special key UI text | No direct battle effect |

## Why Catch Chance Can Be In This Table

`id 11` is not the wild pet having an ability. It is the player's active pet
carrying a source property checked during catch.

Source: `game.d.b(itemId)` catch chance path:

```java
if (this.h.f((byte)11)) {
    n4 = n4 * (100 + aq.c[3][11][5]) / 100;
}
```

Meaning:

```text
active/player pet has property id 11
catch chance *= 120 / 100
```

So this is better described as a **held/carry passive that boosts capture**,
not as a wild-pet ability.

## Row Classification

| Id | Name | Real category | Source consumer / meaning | Battle status |
| ---: | --- | --- | --- | --- |
| 0 | Mạn Đà La Thạch | held passive/property | `game.b.B()` low-HP attack hook | battle consumer |
| 1 | Hồng Sắc Hải Loa | held passive/property | `game.b.B()` attack hook | battle consumer |
| 2 | Quy Xác Toái Phiến | held passive/property | `game.b.B()` / defense hook | battle consumer |
| 3 | Ô Nha Uế | held passive/property | `game.b.b(target)` debuff resistance hook | battle consumer |
| 4 | Viễn Cổ Long Cốt | held passive/property | `game.b.b(target)` crit chance hook | battle consumer |
| 5 | Mật Phong Sào | held passive/property | `game.d` EXP award multiplier | battle/EXP consumer |
| 6 | Ký Cư Giải Xác | held passive/property | reserve EXP/share path | EXP consumer |
| 7 | Linh Trùng Thi Hài | held passive/property | no direct battle consumer proven yet | UNKNOWN/PENDING |
| 8 | Hấp Huyết Đằng Mạn | held passive/property | `game.d.q()` post-hit self-heal | battle consumer |
| 9 | Cá Thờn Bơn | held passive/property | P7 miss path; no-miss | battle consumer |
| 10 | Cảm Lãm Chi Diệp | held passive/property | P7/U() HP floor path | battle consumer |
| 11 | Sủng vật lôi đạt | held passive/property | catch chance multiplier if active pet has this property | catch consumer |
| 12 | Tinh Nguyên Thạch | material | evolution/material inventory in `q.M` | NON_BATTLE |
| 13 | Thiên Giới Tinh Thạch | material | evolution/material inventory in `q.M` | NON_BATTLE |
| 14 | Thiên Địa Thần Thạch | material | evolution/material inventory in `q.M` | NON_BATTLE |
| 15 | Hồn Tinh Thạch | material | mutation/material inventory in `q.M` | NON_BATTLE |
| 16 | Quỷ Thần Tinh Thạch | material | mutation/material inventory in `q.M` | NON_BATTLE |
| 17 | Chìa khóa | key/special item | special inventory/key row in `q.M`; UI displays "Chìa khóa vàng" | NON_BATTLE |

## Naming Rule Going Forward

Do not call the whole table `ability`.

Use:

```text
aq.c[3] property/passive/material table
```

For ids `0..11`, use:

```text
pet-held passive/property
```

For ids `12..17`, use:

```text
material/key inventory rows
```

When coding battle, only implement rows that have a proven `game.b` or `game.d`
consumer. Do not give material/key rows battle behavior.
