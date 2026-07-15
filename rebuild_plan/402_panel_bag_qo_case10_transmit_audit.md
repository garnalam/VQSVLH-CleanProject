# 402 - Panel Bag q.O Case10 Transmit Audit

Date: 2026-07-15

## Scope

Audit only. No behavior port in this slice.

Goal: identify what `bag.ui` tab 3 `Dac thu` q.O case `10` does, how `/data/ui/transmit.ui` works, and what the next safe code slice should be.

## Result Summary

`q.O case 10` is the **Transmit / Teleport** route.

Source route:

`bag.ui` tab `b=3` -> selected `q.O` row id `10` -> `o.a((byte)24)` -> close `/data/ui/bag.ui` -> `game.k.h()` opens `/data/ui/transmit.ui`.

Confirm in `transmit.ui` does not consume the q.O row in the source code slice audited here. It sets the world resume target from the static destination table and switches to world state:

`game.l.B().p/q/r/s`, `game.l.G`, `game.l.B().t = -1`, then `game.f.B().a((byte)9)`.

## Source Anchors

| Source | Method / lines | Finding |
| --- | --- | --- |
| `game.k.bu()` | tab 3 renderer | Reads `q.O`, table `aq.c[5]`, widgets `137/138/139`, desc `163`. |
| `game.k.br()` case `3` | bag refresh | For q.O ids `5/6/10`, softkey widget `7` label is `Mo ra`. |
| `game.k.ac()` case `b=3` | confirm action | `case 10: this.o.a((byte)24); this.p.a("/data/ui/bag.ui");` |
| `game.k.h()` | transmit open | Opens `/data/ui/transmit.ui`, sets list size to `z.length`, initializes controller and calls `aV()`. |
| `game.k.aV()` | transmit render refresh | Renders 5 visible rows from `z[v+i]`, updates scrollbar widget `13`. |
| `game.k.i()` | transmit input | Up/down refresh rows; confirm sets world target; back closes transmit and returns to previous state `8`. |
| resource call graph | `/data/ui/transmit.ui` | Only two refs: open at `game.k.h()` and close at `game.k.i()`. |

## q.O Row Data

Source special table row:

`aq.c[5][10] = [511,54,512]`

| Column | Meaning in `game.k.bu()` | Value | Decoded text |
| --- | --- | ---: | --- |
| `[0]` | name text id | `511` | `Truyen Tong Thach` |
| `[1]` | icon cell | `54` | sprite `257/258` cell used by bag row |
| `[2]` | description text id | `512` | `Co the tai Dai Dia Do gian truyen tong` |

Intended Vietnamese:

- Name: `Truyền Tống Thạch`
- Description: `Có thể tại Đại Địa Đồ gian truyền tống`

Important distinction:

- This is `aq.c[5]` q.O special row id `10`.
- It is not a normal `aq.c[4]` item behavior `10`.
- Normal battle item behavior `10` is blocked in battle item flow, but q.O case `10` is a top-level bag special route.

## transmit.ui Widget Matrix

From `data__ui__transmit.ui.json` and `game.k.h()/aV()`:

| Purpose | Widget ids / behavior |
| --- | --- |
| Frame/background | widgets `1..4` |
| Title | widget `11`, text `Gui di` |
| Visible destination rows | widgets `5..9` |
| Row background/selected art | row widgets use image cells `7` and alt cell `6` |
| Scrollbar track/thumb | widgets `12/13`; thumb set to `109 + h * 88 / z.length` |
| Left softkey | widget `14`, text `Xac dinh` |
| Right softkey | widget `15`, text `Quay lai` |

## Destination Table

Source static arrays in `game.k`:

`z = ["Thuy Kimura", "Bich Thuy thanh", "Nguyen Moc Thanh", "Niem Tho Thanh", "Hac Thach thanh", "Thien khong", "Xa co"]`

`A` is grouped by 5 shorts per destination:

`[scene, room, x, y, G]`

| index | Destination | scene `p` | room `q` | x `r` | y `s` | `game.l.G` |
| ---: | --- | ---: | ---: | ---: | ---: | ---: |
| `0` | `Thuy Kimura` | `1` | `0` | `196` | `208` | `0` |
| `1` | `Bich Thuy thanh` | `2` | `1` | `196` | `208` | `0` |
| `2` | `Nguyen Moc Thanh` | `3` | `3` | `196` | `208` | `0` |
| `3` | `Niem Tho Thanh` | `4` | `5` | `320` | `352` | `0` |
| `4` | `Hac Thach thanh` | `5` | `3` | `320` | `196` | `0` |
| `5` | `Thien khong` | `7` | `2` | `288` | `112` | `0` |
| `6` | `Xa co` | `8` | `0` | `160` | `144` | `0` |

## Input / Transition Matrix

| Current UI | Input | Source action | Side effect | Next UI/state |
| --- | --- | --- | --- | --- |
| `bag.ui`, tab `3`, q.O id `10` | Confirm / key 5 | `o.a((byte)24)`, close `bag.ui` | No consume observed | `transmit.ui` |
| `transmit.ui` | Up | `p.a.b(0)`, `aV()` | selected row changes | `transmit.ui` |
| `transmit.ui` | Down | `p.a.b(1)`, `aV()` | selected row changes | `transmit.ui` |
| `transmit.ui` | Confirm / key 5 | assign world target from `A[h*5..h*5+4]`, set `t=-1`, call `game.f.B().a((byte)9)` | world resume target changes | world/resume state |
| `transmit.ui` | Back | `o.a((byte)8)`, close `transmit.ui` | no target mutation | previous `bag.ui` state |

## Current Rebuild State

Current rebuild status:

- `VqsvPanelRuntime.useSpecialBagRow(...)` case `10` only logs:
  - `PENDING panel game.h.ac bagTab=3 q.O case10 confirm -> o.a(24) bag.ui subruntime not ported`
- There is no `Mode.TRANSMIT` yet.
- There is no `transmit.ui` render path yet.
- There is no source world-target field group for transmit result yet.

Already available pieces that can be reused:

- `VqsvUiLayout.load("transmit.ui")`
- panel row navigation patterns from bag/ride/shop
- decoded `transmit.ui` widgets and source static arrays above

## Recommended Code Slice

Next small slice:

`403 - Panel Bag q.O Case10 Transmit Route + Render`

Implement only:

1. Add `Mode.TRANSMIT`.
2. In `useSpecialBagRow(...)` case `10`, open `Mode.TRANSMIT`.
3. Render `/data/ui/transmit.ui` with the 7 source destinations and 5 visible rows.
4. Back returns to `bag.ui` tab `3`.
5. Confirm sets rebuild source world target fields equivalent to:
   - `p = A[h*5]`
   - `q = A[h*5+1]`
   - `r = A[h*5+2]`
   - `s = A[h*5+3]`
   - `G = A[h*5+4]`
   - `t = -1`
6. Do not consume q.O row `10`.

Smoke PNG set:

- `panel_bag_qo_case10_transmit_open.png`
- `panel_bag_qo_case10_transmit_navigation.png`
- `panel_bag_qo_case10_transmit_back.png`
- `panel_bag_qo_case10_transmit_confirm_world_target.png`

Assert:

- q.O row id `10` uses `aq.c[5][10] = [511,54,512]`.
- open route trace contains `o.a(24)`.
- destination list uses exact source `z`.
- confirm writes exact target tuple for selected destination.
- no q.O special row consume/mutation.
- q.O case `6` badge route still regresses green.
