# 112 Battle choice.ui P21/P4/P5 Parity Matrix

## Scope

Task: audit and port `/data/ui/choice.ui` parity for battle P21/P4/P5.

Rules for this slice:

- Source first, no invented widgets.
- Smoke PNG only, do not open client/game.
- P21/P4 are `choice.ui`.
- P5 is audited here because the task named it, but source P5 uses `petstate.ui`, not `choice.ui`.

Status key: PORTED / PARTIAL / PENDING / N/A / UNKNOWN.

## Source Call Chain

### P21 catch list

Source:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
  - battle state entry `case 21`: sets current target then calls `S.ah()`.
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
  - `ah()` opens `/data/ui/choice.ui`, sprite bank `257`.
  - widget 8 title = `Pokemon ball`.
  - widget 9 subtitle = `Tỉ lệ bắt`.
  - widget 5 command = `Sử dụng`.
  - rows use bag list `q.K`.
  - row icon widgets `54+i` use sprite `258`, cell `aq.c[4][item][1]`.
  - row name widgets `13+i*5` use `an.f(aq.c[4][item][0])`.
  - row value widgets `14+i*5` use `game.d.b(itemId) + "%"`.
  - widgets 59/60 are hidden.
  - `bn()` writes widget 53 = `Số lượng: X cái`.
  - `ai()` handles up/down, confirm consume ball, back closes `choice.ui` and returns P20.

Rebuild mapping:

- `VqsvBattleRuntime.prepareCatchMenu(...)`
- `VqsvBattleRuntime.tickCatchList(...)`
- `VqsvBattleRenderer.drawChoiceOverlay(...)`

Status: PORTED/PARTIAL.

Remaining gaps:

- Full `game.h` widget runtime is still not ported.
- Text clipping/marquee is source-shaped, not pixel-compared against MIDP.

### P4 item list

Source:

- `game.d.java` state entry `case 4`: calls `S.aj()`.
- `game.h.aj()` opens `/data/ui/choice.ui`, sprite bank `257`.
- widget 8 title = `Đạo cụ`.
- widget 9 subtitle = `Số lượng`.
- widget 5 command = `Sử dụng`.
- widgets 59/60 are hidden.
- `be()` fills visible rows from `q.J`:
  - icon widgets `54+i` use sprite `258`, cell `aq.c[4][item][1]`.
  - names use `an.f(aq.c[4][item][0])`.
  - values use item count.
  - widget 53 uses item description `an.f(aq.c[4][item][2])`.
  - scrollbar widget 51 y = `98 + h * 72 / q.J.size()`.
- `ak()` handles up/down, confirm into P16, back closes `choice.ui` and returns P20.

Rebuild mapping:

- `VqsvBattleRuntime.prepareItemMenu(...)`
- `VqsvBattleRuntime.tickItemList(...)`
- `VqsvBattleRenderer.drawChoiceOverlay(...)`

Status: PORTED/PARTIAL.

This slice changed rebuild parity:

- Header title/subtitle now render in source widget bounds:
  - id 8: `x=60,y=75,w=46`
  - id 9: `x=143,y=75,w=36`
- Footer command/back now render as one-line source widget text:
  - id 5: `x=50,y=235,w=24`
  - id 6: `x=164,y=235,w=24`
- Scrollbar thumb now follows source `h * 72 / itemCount`, not rebuild's old `scroll/maxScroll` approximation.

Remaining gaps:

- P16 target selection is not `choice.ui` in source; source routes through pet state target flow. Rebuild still has a choice-style P16 target list, so P16 remains outside this slice.

### P5 pet switch

Source:

- `game.d.java` state entry `case 5`: `this.S.c = 0; this.S.W();`
- `game.d.java` update `case 5`: `this.S.X();`
- `game.h.W()` / `game.h.e(int)` load `/data/ui/petstate.ui`, not `choice.ui`.
- Existing detailed audit: `rebuild_plan/97_battle_p5_petstate_ui_logic_full_audit.md`.

Rebuild mapping:

- `VqsvBattleRuntime.preparePetMenu(...)`
- `VqsvBattleRuntime.tickPetSwitch(...)`
- `VqsvBattleRenderer.renderPetStateOverlay(...)`

Status for `choice.ui`: N/A.

Status for P5 overall: PARTIAL, tracked by petstate audit, not by this choice.ui slice.

## Decoded choice.ui Widget Matrix

Source layout:

`modules/ui/decoded/data__ui__choice.ui.json`

| Widget | Meaning | Source geometry / asset | Rebuild status |
|---:|---|---|---|
| 1 | main frame | x=41,y=68,w=158, sprite 257 cell 91 | PORTED |
| 4 | top strip | x=44,y=70,w=151 | PORTED |
| 2 | body fill | x=44,y=78,w=151 | PORTED |
| 3 | footer strip | x=44,y=238,w=151 | PORTED |
| 7 | list body | x=48,y=90,w=143 | PORTED |
| 8 | title | x=60,y=75,w=46 | PORTED |
| 9 | value header | x=143,y=75,w=36 | PORTED |
| 11/16/21/26/31 | row backgrounds | x=54,y=95/110/125/140/155,w=126, selected cell 25 | PORTED/PARTIAL |
| 13/18/23/28/33 | row names | x=77,y=97/112/127/142/157,w=72 | PORTED |
| 14/19/24/29/34 | row values | x=141,y=97/112/127/142/157,w=36 | PORTED |
| 54..58 | row item icons | x=54,y=95/110/125/140/155,w=14, sprite 258 | PORTED/PARTIAL |
| 50/51 | scrollbar | x=183,y=98, thumb y source-computed | PORTED/PARTIAL |
| 52 | description/count frame | x=52,y=174,w=135, cell 24 | PORTED |
| 53 | description/count text | x=57,y=180,w=125 | PORTED/PARTIAL |
| 5/6 | central action/back text | x=50 and x=164,y=235,w=24 | PORTED/PARTIAL |
| 59/60 | bottom softkeys | hidden by P21/P4 source | N/A for P21/P4 |

## Smoke Coverage

Smoke-only, no client:

- `battle_bunny_catch_p21`
  - asserts P21 state, ball list ids `[0,1]`.
  - pixel asserts choice body fill, footer strip, count text.
- `battle_elder_item_p4`
  - asserts P4 state, non-empty widget 53 description.
  - pixel asserts choice body fill, footer strip, description text.
- `battle_choice_ui_scroll_source_rows`
  - asserts P4 long list scroll.
  - pixel asserts body fill, scrollbar track, scrollbar thumb at source-derived y for selected index 6 in an 8-row list.
- `battle_elder_pet_p5`
  - smoke still useful for regression, but validates `petstate.ui`, not `choice.ui`.

## Current Result

PORTED:

- P21/P4 choice panel frame/list/header/row/icon/description/count layout is source-shaped.
- P21/P4 central command/back widgets now use source positions.
- P4 long-list scrollbar thumb now uses source cursor-index formula.
- Smoke checkpoints now include pixel assertions for the source UI regions.

PARTIAL:

- Full `game.h` widget runtime is not ported.
- Exact MIDP clipping/font pixel parity is not claimed.
- Row selected/unselected state is source-shaped from decoded cells, not yet pixel-compared.

N/A:

- P5 is not a `choice.ui` state in source. It remains under `petstate.ui` work.

PENDING:

- P16 target UI should be audited separately against source `petstate.ui`/target flow.
- Full petstate P5 parity remains tracked in audit 97.
