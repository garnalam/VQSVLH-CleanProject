# 207 - Battle P11 shopbuy.ui/msgyn.ui Polish Closeout

Date: 2026-07-10

Scope: polish battle `P11` shop runtime after `206` proved source item reachability.

## Source chain

Source files:

- `game.d case 11` enters/updates battle shop by calling `game.h.a(4, (byte)0)` and `game.h.a((byte)4, (byte)0)`.
- `game.h.a(int, byte)` loads `/data/ui/shopbuy.ui`, sets list length to `aq.c[4].length`, and shows battle softkey widgets `39/40`.
- `game.h.b(int, byte)` renders:
  - item icon from `aq.c[4][id][1]`;
  - item name from text id `aq.c[4][id][0]`;
  - price from `aq.c[4][id][3]`, with battle list display special-casing item `0`;
  - row currency icon from `aq.c[4][id][4]`;
  - description widget `56`;
  - wallet widgets `43=q.G()` and `44=q.E()`.
- `game.h.a(byte, byte)` opens `/data/ui/msgyn.ui` for normal money/badge purchase, stores `f=1`, starts `c=1`, and left/right adjusts quantity.
- PC rebuild policy: mobile purchase behavior is removed. All battle shop rows are treated as PC-free rows with displayed price/confirm total `0`.
- Original source price basis is retained in code comments/helpers:
  - list display: `itemId == 0 ? aq.c[4][0][3] : aq.c[4][itemId][3] << 1`;
  - confirm total: `qty * aq.c[4][itemId][3] << 1`;
  - trace: `sourceOriginalDisplayPrices=[id=price:currencyX, ...]`.
- `game.h.a(int qty, int baseTotal, int currency, int group)` writes `msgyn.ui` widgets:
  - `9`: quantity;
  - `11`: battle total price. Source uses the original price formula; PC rebuild overrides battle shop total to `0`;
  - `12`: money/badge icon.
- `game.h.b(byte, byte)` confirms purchase, calls `q.c(item, qty, 0)`, subtracts `q.s` for money or `q.u` for badges, then opens `/data/ui/msgwarm.ui`.

## Implemented

- `P11` now renders through a `shopbuy.ui` overlay instead of the generic `choice.ui` overlay.
- `shopbuy.ui` now displays:
  - source table rows from `aq.c[4]`;
  - item icons, names, PC-free prices, row currency icons;
  - selected item description;
  - `q.G`/badge and `q.E`/money wallet values.
- Selecting a normal money/badge row now opens source-shaped `msgyn.ui` instead of buying immediately.
- `msgyn.ui` supports:
  - quantity `c`;
  - left/right wrap using `99 - current item count`;
  - PC-free battle total price `0`;
  - money/badge icon;
  - confirm/back/cancel loop.
- Confirm purchase now:
  - checks money or badges by `aq.c[4][id][4]`;
  - treats every battle shop row as free (`0` units);
  - does not subtract money/badges while the PC-free battle shop policy is active;
  - adds the selected quantity to inventory;
  - opens `msgwarm.ui` success;
  - returns to `shopbuy.ui`.
- 2026-07-10 pixel polish pass:
  - draws `shopbuy.ui` description frame widget `36` before widget `56` text;
  - keeps battle shop softkey text clipped inside safe source-backed zones so it does not cover wallet widgets `41..44`;
  - keeps `msgyn.ui` to one active pointer widget and draws option text in a left option lane so the pointer does not cut through `Xac nhan/Khong`;
  - no logic mutation; renderer-only change.

## Not implemented

- Mobile purchase behavior is intentionally removed for PC rebuild, not pending.
- `shopbuy.ui` is still `PORTED/PARTIAL`, not full source UI VM.
- `msgyn.ui` is source-shaped for this shop path only, not a global reusable runtime for every source caller.
- No pixel-perfect claim yet. This pass has rebuild PNG smoke only, not original-vs-rebuild pixel compare.

## Smoke coverage

Focused PNG checkpoints:

- `battle_p11_shop_full_source_item_rows`
- `battle_p11_shopbuy_wallet_source_widgets`
- `battle_p11_shop_msgyn_open`
- `battle_p11_shop_msgyn_quantity_right`
- `battle_p11_shop_buy_currency2_item0_free`
- `battle_p11_shop_buy_qty2_money`
- `battle_p11_shop_buy_badge_item3`
- `battle_p11_shop_buy_item5_then_p16_use`
- `battle_p11_shop_buy_item6_then_p16_use`
- `battle_p11_shop_buy_item7_then_p16_use`
- `battle_p11_shop_buy_item8_then_p16_use`
- `battle_p11_shop_buy_item9_then_p16_use`
- `battle_p11_shop_buy_item10_then_p16_use`
- `battle_p11_shop_buy_item11_then_p16_use`
- `battle_p11_shop_buy_item12_then_p16_use`

2026-07-10 pixel polish output directory:

- `rebuild_game/build/smoke/p11_pixel_polish/`

Regression PNG checkpoints:

- `battle_elder_shop_p11`
- `battle_p16_item_pp_restore`
- `battle_p16_item_hp_pp`
- `battle_p16_item_revive`
- `battle_p16_item_clear_debuff`
- `panel_bag_item5_12_metadata_source_backed`
- `panel_bag_default_item_state17_revival_item11_success_msg`
- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`

## Status

- P11 source row reachability: PORTED/PARTIAL.
- P11 shopbuy renderer: PORTED/PARTIAL.
- P11 msgyn quantity confirm: PORTED/PARTIAL for PC-free battle shop rows.
- P11 currency type 2/mobile purchase: REMOVED; all P11 rows are PC-free for current PC rebuild.
- P11 item use after purchase: covered for item `5..12` through P16 smoke.

## Next recommended step

Next small source-backed slice: generic `msgyn.ui` reuse or shop visual pixel compare.
