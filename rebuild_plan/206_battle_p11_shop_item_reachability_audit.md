# 206 - Battle P11 Shop Item Reachability Audit

Date: 2026-07-10

Scope: audit whether battle shop `P11` is a real source path for item `5..12`, then decide smoke coverage for using those items. No panel/system branch changes.

## Source proof

Source battle state is `game.d`, not `game.k`:

- `game.d case 11` entry calls `this.S.a(4, (byte)0)`.
- `game.d case 11` update calls `this.S.a((byte)4, (byte)0)`.

`game.h.a(int i1, byte i2)`:

- loads `/data/ui/shopbuy.ui`;
- sets list row count to `aq.c[i1].length`;
- for battle owner (`this.o instanceof d`) shows widgets `39/40` and hides `57/58`;
- calls `b(i1, i2)` to render visible rows.

Important: source renders 5 rows per viewport, but total shop length is `aq.c[4].length`. The old rebuild treated the 5 visible rows as the total item list.

`game.h.b(int i1, byte i2)` render path:

- each visible row uses `aq.c[i1][this.w + i3]`;
- icon comes from column `1`;
- name text id from column `0`;
- description from column `2`;
- battle price is `aq.c[4][0][3]` only for item `0`; otherwise `aq.c[4][id][3] << 1`;
- currency icon uses column `4`.

`game.h.b(byte i1, byte i2)` purchase path for battle owner:

- checks money with `this.q.b(this.h, this.c * aq.c[i1][this.h][3] << 1, i1)`;
- adds item with `this.q.c(this.h, this.c, i2)`;
- subtracts money with `q.s(...)` for money currency or `q.u(...)` for badge currency;
- opens `/data/ui/msgwarm.ui` success text.

`game.g.c(itemId, count, (byte)0)` stores item by source table:

- `aq.c[4][itemId][5] == 0` goes into `K`;
- otherwise goes into `J`.

## Source item table rows

`aq.c[4]` contains item rows `0..14`. Rows `5..12` are real table rows:

- `5`: behavior `1`, HP restore.
- `6`: behavior `2`, PP restore.
- `7`: behavior `2`, stronger PP restore.
- `8`: behavior `3`, HP + PP restore.
- `9`: behavior `3`, stronger HP + PP restore.
- `10`: behavior `5`, clear debuff.
- `11`: behavior `4`, revive.
- `12`: behavior `4`, stronger revive.

Therefore battle `P11` is a source-backed path to obtain item `5..12`.

## Rebuild finding

Before this slice, `VqsvBattleRuntime.prepareShopMenu()` used:

```java
int limit = Math.min(5, VqsvBattleTables.instance().rowCount(4));
```

That was `PORTED/PARTIAL`: it used source metadata and the original source price formula, but exposed only item ids `0..4`. This confused the source UI viewport size with the source table length.

## Fix scope

Implemented:

- use `VqsvBattleTables.instance().rowCount(4)` as the P11 menu length;
- keep source metadata/name/icon/currency ownership from `aq.c[4]`;
- PC rebuild policy after mobile purchase removal: battle shop price display and confirm total are `0` for every row, even though the original source price formula was audited;
- runtime keeps the original source price basis in code as `sourceBattleShopDisplayPrice()` and `sourceBattleShopConfirmTotal()`, and the P11 trace logs `sourceOriginalDisplayPrices` so the free policy can be reversed without rediscovery;
- add smoke coverage that buys items through P11 first, then uses the purchased item through P4/P16.

Not changed:

- no generic `shopbuy.ui` full runtime beyond current battle choice renderer;
- no quantity selector `msgyn.ui`;
- no badge-currency purchase mutation beyond existing money path coverage;
- no panel shop branches.

## Status

- P11 source item reachability: PORTED/PARTIAL -> widened to source table rows.
- P11 UI renderer: PORTED/PARTIAL, still not full `shopbuy.ui`.
- P11 purchase quantity: APPROX, still buys one item.
- P11 item use smoke: route-backed through battle P11 purchase for item behavior groups.

## Next recommended step

After this passes, continue with `shopbuy.ui` visual/runtime debt: quantity confirmation (`msgyn.ui`) and currency/badge display parity, or move back to the next panel branch if UI polish is lower priority.
