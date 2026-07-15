# 382 - Panel bodyShop.ui Full Closeout

Date: 2026-07-15

Scope: close out the full `bodyShop.ui` portable shop hub after row 0 item
shop and rows 1/2/3 premium service branches. This document is only for the
world right-softkey portable shop path. It does not cover bag item use, battle
shop, petsetting, or generic UI VM work.

## Source Entry

Source files/data read:

- `source_code/decoded/decompiled_source_cfr/game/k.java`
- `ui/decoded/data__ui__bodyShop.ui.json`
- `ui/decoded/data__ui__shopbuy.ui.json`
- `ui/decoded/data__ui__msgyn.ui.json`
- `ui/decoded/data__ui__smsInfo.ui.json`
- `ui/decoded/data__ui__msgwarm.ui.json`

Source route:

```text
world.ui right softkey
  -> game.k.k() / gamemenu.ui
  -> gamemenu row "Tuy than cua hang"
  -> game.k.aC()
  -> /data/ui/bodyShop.ui
```

`game.k.aC()` sets `c=0`, `f=0`, opens `bodyShop.ui`, then calls `bA()`.
`game.k.bA()` updates description widget `11`. For rows `c > 0`, it also calls
`o.c(0)` and `bB()` to prepare the premium product id.

Project policy: the PC rebuild treats SMS/payment products as free confirms.
No SMS/network behavior is implemented or required.

## Row Matrix

| Row | Source row | Label | Source route | Rebuild status |
| --- | --- | --- | --- | --- |
| 0 | `c=0` | `Thuong diem binh dan` | `game.k.aD()` confirm closes `bodyShop.ui`, enters state `26`, then opens `shopbuy.ui` item shop. | `PORTED/PARTIAL` |
| 1 | `c=1` | `Thang cap sung vat` | `bB()` maps to `o.b((byte)3)`, product `3`; if all pets level 50, opens `msgwarm.ui`; otherwise enters `smsInfo.ui`. | `PORTED/PARTIAL` |
| 2 | `c=2` | `Mua sam huy hieu` | `bB()` maps to `o.b((byte)4)`, product `4`; enters `smsInfo.ui`. | `PORTED/PARTIAL` |
| 3 | `c=3` | `Mua sam kim tien` | `bB()` maps to `o.b((byte)2)`, product `2`; enters `smsInfo.ui`. | `PORTED/PARTIAL` |

## Row 0 Item Shop

Row 0 is the ordinary item shop route:

```text
bodyShop.ui c=0
  -> P=26
  -> shopbuy.ui
  -> msgyn.ui quantity confirm
  -> msgwarm.ui success/warning
```

Source-backed behavior currently covered:

- item rows bind to `aq.c[4]`;
- name/icon/description/price/wallet render from source tables;
- money price uses source state `j=2`, money cost `price * 3 / 2`;
- badge price uses source badge cost;
- quantity confirm supports left/right, confirm, back/no;
- successful purchase mutates inventory and currency once;
- not-enough-money/badge and full-item warnings are handled;
- back from `shopbuy.ui` returns to `bodyShop.ui`.

Reference doc: `378_panel_portable_shop_softkey_audit.md`.

## Row 1 Product 3: Pet Level-Up

Source proof:

- `game.k.bB()` row `c=1` calls `o.b((byte)3)`.
- `game.k.aD()` checks whether all bag pets are already level `>= 50`.
- Product `3` commit loops bag pets, adds `+5` levels, caps at `50`, refreshes
  pet state, and queues evolution candidates.

Rebuild mapping:

- bag pets: `Scene.sourcePets`;
- level mutation: `SourcePetState.level`;
- cap: `min(level + 5, 50)`;
- evolution queue: `Scene.sourceEvolutionQueue`, `sourceEvolutionL`,
  `sourceEvolutionI`.

Status: `PORTED/PARTIAL`.

Partial means:

- free PC `smsInfo.ui` confirm is source-shaped, not full SMS runtime;
- evolution notice consumer exists, but exact full tutorial/evolve bridge remains
  broader roadmap work.

Reference doc: `379_panel_bodyshop_row1_pet_levelup_service_matrix.md`.

## Row 2 Product 4: Badge Purchase

Source proof:

- `game.k.bB()` row `c=2` calls `o.b((byte)4)`.
- Product `4` commit calls `game.g.o().u(10)`.

Rebuild mapping:

- `game.g.o().u(10)` -> `Scene.sourceBadges += 10`.

Status: `PORTED/PARTIAL`.

Reference doc: `380_panel_bodyshop_row2_badge_purchase_service_matrix.md`.

## Row 3 Product 2: Money Purchase

Source proof:

- `game.k.bB()` row `c=3` calls `o.b((byte)2)`.
- Product `2` commit calls `game.g.o().s(10000)`.

Rebuild mapping:

- `game.g.o().s(10000)` -> `Scene.sourceMoney += 10000`.

Status: `PORTED/PARTIAL`.

Reference doc: `381_panel_bodyshop_row3_money_purchase_service_matrix.md`.

## UI Runtime Matrix

| UI | Used by | Current status | Notes |
| --- | --- | --- | --- |
| `bodyShop.ui` | portable shop hub rows 0..3 | `PORTED/PARTIAL` | Uses decoded row widgets and source-shaped description widget `11`. |
| `shopbuy.ui` | ordinary item shop | `PORTED/PARTIAL` | Item rows, wallet, description, buy/back flow covered. |
| `msgyn.ui` | quantity confirm | `PORTED/PARTIAL` | Quantity, total, confirm and back covered for portable shop. |
| `smsInfo.ui` | service confirm rows 1..3 | `PORTED/PARTIAL` | Reusable renderer; readable source-shaped text color fixed for PC smoke. |
| `msgwarm.ui` | success/warning | `PORTED/PARTIAL` | Warning/success text and continue prompt covered. |

## Smoke Matrix

Dedicated suite: `panel_portable_shop`.

Latest verification:

```powershell
cd ..\rebuild_game
.\build.ps1
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-suite panel_portable_shop build_intro_demo\bodyshop_closeout_382
```

Result: `PASS`, `22/22`.

Output directory:

```text
rebuild_game/build_intro_demo/bodyshop_closeout_382
```

| Checkpoint | Status | What it proves |
| --- | --- | --- |
| `panel_portable_shop_open` | `PASS` | Opens `bodyShop.ui` from softkey path. |
| `panel_portable_shop_item_detail` | `PASS` | Row 0 item shop binds item list, price, wallet and description. |
| `panel_portable_shop_msgyn_open` | `PASS` | Buy opens quantity confirm. |
| `panel_portable_shop_msgyn_qty2_buy` | `PASS` | Quantity change and purchase mutation work. |
| `panel_portable_shop_msgyn_back` | `PASS` | Back/no from quantity confirm does not mutate inventory. |
| `panel_portable_shop_buy_success` | `PASS` | Successful item purchase opens success warning. |
| `panel_portable_shop_buy_not_enough_money` | `PASS` | Money warning blocks mutation. |
| `panel_portable_shop_back` | `PASS` | Back from shop list returns to `bodyShop.ui`. |
| `panel_bodyshop_row1_open_description` | `PASS` | Row 1 description/selection render. |
| `panel_bodyshop_row1_smsinfo_confirm` | `PASS` | Row 1 opens free source-shaped `smsInfo.ui`. |
| `panel_bodyshop_row1_all_max_warning` | `PASS` | All pets at level 50 blocks product 3. |
| `panel_bodyshop_row1_success_level_plus5` | `PASS` | Product 3 adds exactly 5 levels. |
| `panel_bodyshop_row1_success_cap50` | `PASS` | Product 3 caps at level 50. |
| `panel_bodyshop_row1_evolution_queue` | `PASS` | Product 3 can queue evolution notice. |
| `panel_bodyshop_row2_open_description` | `PASS` | Row 2 description/selection render. |
| `panel_bodyshop_row2_smsinfo_confirm` | `PASS` | Row 2 opens free source-shaped `smsInfo.ui`. |
| `panel_bodyshop_row2_success_badges_plus10` | `PASS` | Product 4 adds exactly 10 badges. |
| `panel_bodyshop_row2_smsinfo_back` | `PASS` | Back from product 4 confirm does not mutate badges. |
| `panel_bodyshop_row3_open_description` | `PASS` | Row 3 description/selection render. |
| `panel_bodyshop_row3_smsinfo_confirm` | `PASS` | Row 3 opens free source-shaped `smsInfo.ui`. |
| `panel_bodyshop_row3_success_money_plus10000` | `PASS` | Product 2 adds exactly 10000 money. |
| `panel_bodyshop_row3_smsinfo_back` | `PASS` | Back from product 2 confirm does not mutate money. |

## Remaining Debt

These are intentionally not claimed as complete:

- full generic `game.h/game.k` decoded widget VM;
- exact Java ME text baseline, clip, and marquee behavior for all UI widgets;
- exact `a.a.c()` / `a.a.a()` string runtime for every premium message;
- real SMS/payment transport;
- full `smsTip.ui` lifecycle;
- original-vs-rebuild pixel compare against the live MIDP/S60 client.

## Closeout Status

`bodyShop.ui` as a four-row portable shop hub is closed for current roadmap
purposes at `PORTED/PARTIAL`.

This means all four visible rows have source-backed route behavior, data
binding, reward/mutation logic, and smoke coverage. Remaining work belongs to
generic UI runtime/pixel parity, not missing row functionality.

## Next Roadmap Step

Return to the other world softkey branch: bottom-left system menu
`gamesystem.ui`, especially the settings row:

```text
world.ui left softkey
  -> game.k.m()
  -> gamesystem.ui
  -> row 2 "Thiet lap tro choi"
  -> help.ui settings view
```

The next slice should audit the current `gamesystem.ui -> help.ui` settings
runtime, rerun focused smoke PNGs, then fix any visual or input mismatch found.
