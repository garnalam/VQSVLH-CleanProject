# 396 - Panel Bag Lung Bao Current Route Matrix

Ngay: 2026-07-15

Pham vi: right softkey `Menu tro choi` -> row `Lung bao` -> `/data/ui/bag.ui`.

Luat lam viec: source-first, smoke/headless PNG only. Khong mo live client.

## Source Entry

| Source | Vai tro | Ket luan |
| --- | --- | --- |
| `game.k.ab()` | Open `/data/ui/bag.ui`, set `b=0`, title `Vat pham`, call `bq()` | PORTED/PARTIAL |
| `game.k.bq()` | Reset list controller for current tab `8 + b * 39`, then refresh | PORTED/PARTIAL |
| `game.k.br()` | Dispatch render by tab `b=0..3` | PORTED/PARTIAL |
| `game.k.bs()` | Tab `0` item/ball rows from `q.L + q.K` | PORTED/PARTIAL |
| `game.k.br()` case `1` | Tab `1` equipment/held rows from `q.M` | PORTED/PARTIAL render parity |
| `game.k.bt()` | Tab `2` material/key rows from `q.N` source table `aq.c[3]` | PORTED/PARTIAL render parity |
| `game.k.bu()` | Tab `3` special rows from `q.O` source table `aq.c[5]` | PORTED/PARTIAL |
| `game.k.ac()` | Main bag input: tab switch, row nav, confirm/back, special branches | PORTED/PARTIAL |

## Source Tab Matrix

| Tab `b` | UI label | Source vector/table | Source widgets | Confirm behavior | Rebuild status |
| ---: | --- | --- | --- | --- | --- |
| `0` | `Tieu hao` / `Vat pham` | `q.L + q.K`, table `aq.c[4]` | row ids `18/19/20...`, desc `46`, scrollbar `43` | balls blocked in panel; items route to pet target state 17; item 13/14 special direct | PORTED/PARTIAL |
| `1` | `Trang suc` | `q.M`, table `aq.c[3]` | row ids `59/60/61...`, desc `85`, scrollbar `84` | equip/unequip style route; same family as held item/equipment | PORTED/PARTIAL render-only |
| `2` | `Tai lieu` | `q.N`, table `aq.c[3]` | row ids `98/99/100...`, desc `124`, scrollbar `123` | mostly display/material/key; exact confirms depend on source branch | PORTED/PARTIAL render-only |
| `3` | `Dac thu` | `q.O`, table `aq.c[5]` | row ids `137/138/139...`, desc `163`, scrollbar `162` | egg hatch, ride, badge/record, transmit, special pet-use ids `7/8/9` | PORTED/PARTIAL |

## Current Rebuild Mapping

| Area | Rebuild file/behavior | Status |
| --- | --- | --- |
| Open from `gamemenu.ui` row `Lung bao` | `VqsvPanelRuntime` mode `BAG`, selected row `2` | PORTED/PARTIAL |
| Source-shaped `bag.ui` frame/tabs/list | `renderBag(...)` uses decoded `bag.ui` and sprite `257/258` | PORTED/PARTIAL |
| Keyboard up/down, left/right tab, back | `tickBag(...)` | PORTED/PARTIAL |
| Mouse hover/wheel/click | `hover/click` bag branch | PC_QOL PORTED/PARTIAL |
| Tab 0 normal item rows | `sourceBagItems` -> `BagRow` from `VqsvSourceOps.sourceItem()` | PORTED/PARTIAL |
| Tab 0 ball rows `0..3` warning | `PANEL_BAG_CANNOT_USE` msgwarm, no mutation | PORTED |
| Tab 0 item target flow | `openPanelBagState17Petstate(...)` and state17 warnings/success | PORTED/PARTIAL |
| Tab 0 item 13 avoid monster | q.x/q.w-style timer and consume | PORTED |
| Tab 0 item 14 egg accelerator | egg progress success/warnings | PORTED |
| Tab 1 `Trang suc` top-level bag | `sourceEquipmentItems` -> `q.M` proxy, `aq.c[3]` metadata | PORTED/PARTIAL |
| Tab 2 `Tai lieu` top-level bag | `sourceMaterialItems` -> `q.N` proxy, `aq.c[3]` metadata | PORTED/PARTIAL |
| Tab 3 egg row | `sourceEggSpecialRow(...)`, hatch flow | PORTED/PARTIAL |
| Tab 3 q.O/special ids `7/8/9` | state19 pet target + level gate + consume stack | PORTED/PARTIAL |
| Tab 3 special id `5` | opens `ride.ui`; ride navigation/warnings/speed mutation | PORTED/PARTIAL |
| Tab 3 special ids `6`, `10` | trace-only/pending | PENDING |

## Existing Smoke Recheck

Output directory:

`rebuild_game/build_intro_demo/panel_bag_lung_bao_396_current`

| Checkpoint | Result | Coverage |
| --- | --- | --- |
| `panel_bag_open_from_gamemenu` | PASS | open route |
| `panel_bag_navigation` | PASS | row navigation |
| `panel_bag_hover_preview_no_confirm` | PASS | hover preview no mutation |
| `panel_bag_back_returns_gamemenu` | PASS | back route |
| `panel_bag_item_cannot_use_warning` | PASS | ball warning |
| `panel_bag_default_item_state17_open_petstate` | PASS | normal item target petstate |
| `panel_bag_default_item_state17_success_msg` | PASS | normal item apply success |
| `panel_bag_item13_success_msg` | PASS | avoid monster item |
| `panel_bag_item14_type0_success` | PASS | egg accelerator |
| `panel_bag_egg_tab_ready_render` | PASS | tab 3 egg row |
| `panel_bag_special_reward7_state19_petstate` | PASS | tab 3 special use route |
| `panel_bag_special_reward5_ride_navigation` | PASS | ride route |

## Important Difference From Petsetting

`petsetting.ui` row `2 Vat pham trang suc` already has a held-item choice flow
for a selected pet.

Top-level `bag.ui` tab `1 Trang suc` is a different screen: source renders
the equipment inventory list directly inside `bag.ui`, from `q.M`, with status
text `Da mang theo` and description widget `85`. Current rebuild does not show
that tab yet, so manual testing will look incomplete even though petsetting
equipment itself works.

## Current Status

`Lung bao / bag.ui` is **PORTED/PARTIAL**:

- tab `0`, tab `1`, tab `2`, and tab `3` now have source-backed render coverage;
- route/open/back/navigation/mouse preview are covered;
- tab `1` and tab `2` are render-only; their deeper action/lifecycle parity is not generalized;
- q.O case `6` badge/record and case `10` transmit remain pending by design.

## Recommended Next Slice

Completed follow-up slices:

- `397 - Panel Bag Tab1 Trang Suc Render Parity`
- `398 - Panel Bag Tab2 Tai Lieu Render Parity`
- `399 - Panel Bag Tab3 Dac Thu Closeout / q.N-q.O Split Audit`
- `400 - Panel Bag q.O Case6 Badge/Record Audit`
- `401 - Panel Bag q.O Case6 Badge Route Port`
- `402 - Panel Bag q.O Case10 Transmit Audit`

Recommended next slice is now `403 - Panel Bag q.O Case10 Transmit Route + Render`.
Port route/render first, then smoke confirm world-target mutation separately inside the same narrow transmit slice.
