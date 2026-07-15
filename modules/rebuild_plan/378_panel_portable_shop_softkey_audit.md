# 378 - Panel Portable Shop Softkey Audit

Scope: softkey game menu row `Tùy thân cửa hàng` only. No quest, battle shop, bag item use, or live-client flow.

## Source Flow

| Step | Source | UI | Data / Binding | Rebuild status |
|---|---|---|---|---|
| Open game menu | `game.k.k()` | `/data/ui/gamemenu.ui` | If `a.a.i` is true, row 0 is `Tùy thân cửa hàng`; rows 1..5 shift down. If false, row 0 is `Sủng vật` and portable shop is hidden. | PORTED/PARTIAL: rebuild treats premium/SMS as unlocked per project rule, so row 0 opens portable shop. |
| Confirm row 0 | `game.k.l()` -> `this.o.a((byte)14)` | closes `gamemenu.ui` | State 14. | PORTED |
| Portable shop hub | `game.l` case 14 -> `game.k.aC()` | `/data/ui/bodyShop.ui` | `c=0`, `f=0`, description widget 11 via `bA()`. | PORTED/PARTIAL |
| Hub confirm row 0 | `game.k.aD()` c=0 confirm -> `this.o.a((byte)26)` | closes `bodyShop.ui` | State 26. | PORTED |
| Buy list | `game.l` case 26 -> `game.k.a(4,(byte)0)` | `/data/ui/shopbuy.ui` | `j=2`, list length `aq.c[4].length`, title widget 5 `Mua`. | PORTED |
| Buy list render | `game.k.b(4,0)` | `/data/ui/shopbuy.ui` | row icon widgets 51..55 use sprite 258, cell `aq.c[4][id][1]`; row name widgets 14/19/24/29/34 use text `aq.c[4][id][0]`; price widgets 15/20/25/30/35. | PORTED/PARTIAL |
| Description | `game.k.b(4,0)` | widget 56 | `aq.c[4][selected][2]`. | PORTED |
| Wallet | `game.k.b(4,0)` | widgets 43/44 | source `q.G()` badges and `q.F()` money. | PORTED |
| Quantity confirm | `game.k.a(byte,byte)` buy action | `/data/ui/msgyn.ui` | Opens quantity prompt; left/right changes qty; confirm commits; back/no returns to `shopbuy.ui`. | PORTED |
| Back from buy list | `game.k.a(byte,byte)` key back -> state 14 | back to `bodyShop.ui` | Source closes `shopbuy.ui`. | PORTED |

## Price / Currency

For state 26, source sets `j=2`. In `game.k.b(4,0)`:

| Currency `aq.c[4][id][4]` | Source display / cost | Rebuild rule |
|---|---|---|
| `0` money | `price * 3 / 2` | PORTED |
| `1` badge | `price` | PORTED |
| `2` premium/SMS | source opens premium/SMS branch; project rule says SMS-free | PORTED/PARTIAL as free purchase with no SMS send |

Success text source:
`Đã thành công mua sắm #2{name} * {qty}` + `Nhấn nút 5 tiếp tục`.

Warnings source:
`Kim tiền chưa đủ`, `Số lượng Huy hiệu chưa đủ`, `Đạo cụ này đã đủ`, and premium locked text if `a.a.i` false. Rebuild keeps `a.a.i` effectively unlocked and handles money/badge/full warnings.

## Pending

| Area | Status | Note |
|---|---|---|
| `bodyShop.ui` premium rows 1/2/3 | PARTIAL | These enter deeper paid/premium flows. Not in this slice. |
| Quantity confirm `msgyn.ui` | PORTED | Panel shop now opens `msgyn.ui`, supports qty left/right, confirm, back/no, qty total, money/badge/free-SMS commit, and smoke PNG coverage. |
| Exact generic `game.h/game.k` widget VM | PARTIAL | Renderer uses decoded bounds and existing source-shaped helpers, not full Java ME widget runtime. |

## Smoke Coverage

| Checkpoint | Status | What it proves |
|---|---|---|
| `panel_portable_shop_open.png` | PASS | `bodyShop.ui` opens from softkey row. |
| `panel_portable_shop_item_detail.png` | PASS | `shopbuy.ui` item rows, price, currency, description and wallet render. |
| `panel_portable_shop_msgyn_open.png` | PASS | Buy action opens `msgyn.ui` qty=1, total uses portable shop price. |
| `panel_portable_shop_msgyn_qty2_buy.png` | PASS | Right key qty=2, confirm adds 2 items and subtracts total money. |
| `panel_portable_shop_msgyn_back.png` | PASS | Back closes confirm and does not mutate money/inventory. |
| `panel_portable_shop_buy_success.png` | PASS | Success warning after confirmed qty=1 purchase. |
| `panel_portable_shop_buy_not_enough_money.png` | PASS | Not-enough-money warning after confirm, no item added. |
| `panel_portable_shop_back.png` | PASS | Back from `shopbuy.ui` returns to `bodyShop.ui`. |
