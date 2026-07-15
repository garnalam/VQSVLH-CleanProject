# 400 - Panel Bag q.O Case6 Badge/Record Audit

Date: 2026-07-15

## Scope

Audit only. No behavior port in this slice.

Goal: identify what `bag.ui` tab 3 `Dac thu` q.O case `6` really opens, what data it reads, and what code slice should follow.

## Result Summary

`q.O case 6` is the **Badge / Huy hieu** route.

It is not a material `q.N` row and not a generic record row. Source route:

`bag.ui` tab `b=3` -> selected `q.O` row id `6` -> `o.a((byte)12)` -> close `/data/ui/bag.ui` -> state `12` opens `/data/ui/badge.ui`.

## Source Anchors

| Source | Lines / method | Finding |
| --- | --- | --- |
| `game.k.bu()` | tab 3 renderer | Reads `q.O`, table `aq.c[5]`, widgets `137/138/139`, desc `163`. |
| `game.k.br()` case `3` | bag refresh | For q.O ids `5/6/10`, softkey widget `7` label is `Mo ra`. |
| `game.k.ac()` case `b=3` | confirm action | `case 6: this.o.a((byte)12); this.p.a("/data/ui/bag.ui");` |
| `game.k.Q()` | record open | Opens `/data/ui/record.ui`, sets record counters, selected record row. |
| `game.k.R()` | record input | If record selected row `1`, calls `o.a((byte)12)`. |
| `game.k.W()` | badge open | Opens `/data/ui/badge.ui`, closes `/data/ui/record.ui`, initializes badge icon cells. |
| `game.k.be()` | badge detail refresh | Renders selected badge title/description/status from `aq.c[2]` and `q.C`. |
| `game.k.X()` | badge input/back | Navigates badge grid; back goes to previous state `8` or to `record.ui` depending on state stack. |

## q.O Row Data

Source special table row:

`aq.c[5][6] = [301,48,309]`

| Column | Meaning in `game.k.bu()` | Value | Decoded text |
| --- | --- | ---: | --- |
| `[0]` | name text id | `301` | `Huy hieu cua cac hinh minh hoa` |
| `[1]` | icon cell | `48` | sprite `257/258` cell used by bag row |
| `[2]` | description text id | `309` | `Nhan nut 5 tra xet nhung huy hieu da dat duoc;` |

Vietnamese decoded text is mojibake in some JSON console output, but the intended strings are:

- Name: `Huy hiệu của các hình minh họa`
- Description: `Nhấn nút 5 tra xét những huy hiệu đã đạt được;`

## Badge UI Source Data

`badge.ui` shows 8 badge slots. Source data comes from:

- `q.C[8][2]`: player badge state matrix.
  - `q.C[id][0]`: achieved status. Source checks `== 0` as `Chua dat`, non-zero as achieved; other source branches use `== 2` for fully achieved.
  - `q.C[id][1]`: badge level/variant used to choose description text.
- `aq.c[2][badgeId]`: badge metadata.

`aq.c[2]` rows:

| id | Raw row | Name | Base effect / locked desc | Advanced desc |
| ---: | --- | --- | --- | --- |
| `0` | `[187,0,195,203,211,220,10]` | `Liet Hoa Chuong` | increases dark vision | post-battle HP recovery |
| `1` | `[188,0,196,204,211,-1,5]` | `Moc Quy Chuong` | remove tree obstacles | forest attribute boost |
| `2` | `[189,0,197,205,211,-1,5]` | `Dai Dia Chuong` | break rock obstacles | rock-zone attribute boost |
| `3` | `[190,0,198,206,211,-1,2]` | `Thuy Lam Chuong` | enter water areas | water-zone attribute boost |
| `4` | `[191,0,199,207,211,5,2]` | `Hoa Phong Chuong` | defense +5% | self battle speed improvement |
| `5` | `[192,0,200,208,211,1,20]` | `U Linh Chuong` | seed growth / spell utility | each turn attribute +1%, max 20% |
| `6` | `[193,0,201,209,211,5,50]` | `Loi Dien Chuong` | damage rate +5% | adverse effect duration halved |
| `7` | `[194,0,202,210,212,100,-1]` | `Hoang Kim Chuong` | reserve pets get EXP | cannot strengthen |

Text ids used by source:

- `aq.c[2][id][0]` -> badge name.
- `aq.c[2][id][2 + q.b(id, 1)]` -> selected detail text.
- Source status text:
  - `q.b(id,0) == 0` -> `Chua dat`
  - otherwise -> `Da dat duoc`

## badge.ui Widget Matrix

From `data__ui__badge.ui.json` and `game.k.W()/be()`:

| Purpose | Widget ids / behavior |
| --- | --- |
| Frame/title | widget `5` text `Huy hieu` |
| Back softkey | widget `6`, label `Quay lai` |
| Badge icon cells | widgets `25..32`, source sets sprite cell `46 + id` when `q.C[id][0] != 0` |
| Badge slots / cursor | widgets `17..24` in rebuild; source UI navigation has 8 depth slots |
| Selected badge name | widget `13`, text `aq.c[2][b][0]` |
| Selected badge description | widget `14`, text `aq.c[2][b][2 + q.C[b][1]]` |
| Status label/value | widgets `15/16`, value `Chua dat` or `Da dat duoc` |
| Locked/extra name | widget `33`, used by rebuild to show locked selected badge name |

## Input / Transition Matrix

| Current UI | Input | Source action | Side effect | Next UI/state |
| --- | --- | --- | --- | --- |
| `bag.ui`, tab `3`, q.O id `6` | Confirm / key 5 | `o.a((byte)12)` and close `bag.ui` | No consume, no item mutation found in this branch | `badge.ui` |
| `record.ui`, selected row `1` | Confirm / key 5 | `o.a((byte)12)` | No mutation | `badge.ui` |
| `badge.ui` | D-pad | `p.a.b(direction)` then `be()` | selected badge changes | `badge.ui` |
| `badge.ui` | Back | `game.k.X()` | if previous state is `8`, `o.a(8)`; else set `c=1`, `o.a(9)` | previous state or `record.ui` |

Important pending point:

- For the **bag-origin route**, exact back destination must be tested when porting. Source has a previous-state branch in `game.k.X()`, so do not hardcode "always back to record" without smoke evidence.

## Current Rebuild State

Already present:

- `VqsvPanelRuntime` has `Mode.RECORD`.
- `VqsvPanelRuntime` has `Mode.BADGE`.
- Record -> Badge route is already source-shaped:
  - trace `game.h.O confirm c=1 -> o.a(12) game.h.R badge.ui open`
- Badge render already reads `aq.c[2]` through `VqsvBattleTables`.
- Existing smoke includes:
  - `panel_badge_open_from_record`
  - `panel_badge_navigation`
  - `panel_badge_back_returns_record`
  - `panel_badge_record_back_returns_gamemenu`

Missing for q.O case `6`:

- `VqsvPanelRuntime.useSpecialBagRow(...)` currently only logs:
  - `PENDING panel game.h.ac bagTab=3 q.O case6 confirm -> o.a(12) bag.ui subruntime not ported`
- There is no direct bag tab 3 -> badge smoke yet.
- Back flow from badge when opened from bag is not proven yet.

## Recommended Code Slice

Next small slice:

`401 - Panel Bag q.O Case6 Badge Route Port`

Status: completed in `401_panel_bag_qo_case6_badge_route_port_closeout.md`.

Implement only:

1. In `useSpecialBagRow(...)` case `6`, close bag and enter existing `Mode.BADGE`.
2. Preserve enough origin state to make Back match source:
   - likely return to `bag.ui` tab `3` or source previous state `8`, but this must be verified during smoke.
3. Add smoke PNG:
   - `panel_bag_qo_case6_badge_open.png`
   - `panel_bag_qo_case6_badge_navigation.png`
   - `panel_bag_qo_case6_badge_back.png`
4. Assert:
   - row id `6` uses `aq.c[5][6] = [301,48,309]`
   - confirm opens `badge.ui`
   - no q.O stack/item is consumed
   - `sourceBadges` / badge state is unchanged

Do not port q.O case `10` in the same slice. q.O case `10` remains the next separate audit target.
