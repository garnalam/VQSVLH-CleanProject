# 111 Battle P17 UI Widget Runtime Parity Matrix

Status date: 2026-07-08

Scope:
- P17 catch animation/effect.
- Battle uses of `/data/ui/choice.ui`, `/data/ui/msgwarm.ui`, `/data/ui/openbox.ui`.

Rules:
- Source facts first. No MIDP pixel-perfect claim without original-client pixel comparison.
- `PORTED` means source path/data/side effect is represented in rebuild.
- `PARTIAL` means source-shaped but still lacks exact Java ME/MIDP pixel parity.

## Source Facts

### P17 catch

Source: `modules/source_code/decoded/decompiled_source_cfr/game/d.java`

| Source point | Fact | Rebuild status |
| --- | --- | --- |
| P17 enter | `aj = new f(); aj.a(269,false); aj.b(h.i,h.j); aj.c(); e((byte)0);` | `PORTED` as `SpriteAnim.load(269)`, positioned on target battle slot, phase q0. |
| catch decision | `n4 = b(l); ak = ae.a(100) < n4; U==0 && V==5 forces fail` | `PORTED` with deterministic smoke hook and Bunny first-catch forced fail. |
| `e(0)` | ball sprite state 0, loop/next mode 0 | `PORTED`. |
| `e(1)` | hides target, creates `ah` type 8 row `[8,x,y,speciesSprite,0,side,0,9,1,3,0,10,0,0,7,0,-10,4,0,-20]`, starts H, ball state 1 hold-last | `PORTED/PARTIAL`: exact row/timing/draw order represented; bitmap transform still source-shaped. |
| `e(2)` | ball sprite state 2 | `PORTED`. |
| `e(3)` | success ball sprite state 3 hold-last | `PORTED`. |
| `e(4)` | fail H type 8 row `[8,x,y,speciesSprite,0,side,0,8,1,4,1,4,0,-20,6,0,-12,8,0,-4,10,0,0]`, ball state 1 hold-last | `PORTED/PARTIAL`: exact row/timing/draw order represented; bitmap transform still source-shaped. |
| P17 draw | draw battle actors, then if `H != null && H.c()` draw H then `aj`; else draw `aj` | `PORTED/PARTIAL`: H-before-ball order represented in smoke. Exact MIDP `drawRGB` needs pixel compare. |
| q3 success result | `S.b("Bắt thành công #2...")`; storage bag/bank/full branches | `PORTED` for bag/bank/full release and openbox chain. |
| q4 fail cleanup | `H=null; d[0].b(true); aj.d(); h.J=true; i++; P1` | `PORTED` smoke verifies target restore and catch visual clear. |

### `ah` type 8 transform

Source: `modules/source_code/decoded/decompiled_source_cfr/ah.java`

| Source point | Fact | Rebuild status |
| --- | --- | --- |
| constructor type 8 | Copies target sprite cell to `b[0]`, clones to `b[1]`, stores tail values in `t` | `PORTED/PARTIAL`. Rebuild derives bitmap from sprite cell and keeps source tail values. |
| update type 8 | Uses `t[0] < t[1] / t[3] * t[2]`; optionally resets clone when `t[4]==1`; applies `l.a(scale)` then `l.b(...,1,50)`, adds dx/dy | `PORTED/PARTIAL`. Scale/dx/dy/tick sequence source-shaped. |
| draw type 8 | `drawRGB(b[1], i+d, j+e, true)` | `PARTIAL`. Java2D equivalent still not pixel-compared with Java ME `drawRGB`. |

## UI Widget Matrix

### `/data/ui/choice.ui`

Source: `modules/source_code/decoded/decompiled_source_cfr/game/h.java`, decoded UI `modules/ui/decoded/data__ui__choice.ui.json`

| Widget/source | Decoded/source fact | Rebuild status |
| --- | --- | --- |
| frame id 1 | x=41, y=68, w=158, sprite 257 cell 91 mode 2 | `PORTED/PARTIAL` in battle renderer. |
| header id 8 | x=60, y=75; P21 sets `Pokemon ball`; P4/P5/P11 set context title | `PORTED`. |
| subtitle id 9 | x=143, y=75; P21 sets catch chance/count column | `PORTED`. |
| rows id 13/14 plus icons 54+i | five visible rows at y=95,110,125,140,155; row h=15 | `PORTED`: render and click mapping use 5-row source layout. |
| footer id 5/6 | id 5 action x=50,y=235; id 6 back x=164,y=235 | `PORTED/PARTIAL`: action/back labels and click-back region represented. |
| desc/count id 53 | P21 writes `Số lượng: X cái`; other lists can use description | `PORTED/PARTIAL`: count and marquee description represented. |
| scroll id 50/51 | x around 183,y=98; source widget runtime scroll asset not fully reproduced | `PARTIAL`: source-shaped scrollbar, not full `ab/af` widget runtime. |

### `/data/ui/msgwarm.ui`

Source: `game.h.E()/F()/G()/a(String,String)`, decoded UI `modules/ui/decoded/data__ui__msgwarm.ui.json`

| Widget/source | Decoded/source fact | Rebuild status |
| --- | --- | --- |
| open | `S.E()` opens `/data/ui/msgwarm.ui`, sprite 257 | `PORTED`: battle warnings now create `TextBox.msgWarm(...)`. |
| close | `S.F()` closes; `S.G()` checks closed | `PORTED/PARTIAL`: rebuild clears `TextBox` on confirm and returns saved battle state. |
| frame id 8 | x=76,y=106,w=89, cell 128 mode 2 | `PORTED/PARTIAL`: shared TextBox renderer draws decoded frame/fills. |
| text id 7 | x=85,y=119,w=70 | `PORTED`: one-line marquee used for overflow; smoke has pixel assertion in the text region. |
| prompt id 6 | x=89,y=170,w=60 | `PORTED`: prompt uses same one-line marquee. |

### `/data/ui/openbox.ui`

Source: `game.h.b(String)`, `game.h.a(String,int)`, `game.h.ax()`, decoded UI `modules/ui/decoded/data__ui__openbox.ui.json`

| Widget/source | Decoded/source fact | Rebuild status |
| --- | --- | --- |
| open | `S.b(text)` opens `/data/ui/openbox.ui`, sprite 257 state 9 | `PORTED`: catch success/bank/full messages use `TextBox.openBox(...)`. |
| close | `S.ax()` true after UI closed; P17 uses `f=1/2/3/4` branch | `PORTED/PARTIAL`: bag/bank/full release chain represented. |
| frame id 1 | x=45,y=147,w=150,h=-1; `game.h.e(String)` attaches sprite 257, wrapper mode `m.a=3`, animation state 9 | `PORTED/PARTIAL`: shared TextBox openbox frame now draws sprite 257 state 9 instead of a hand-made frame; smoke asserts source sprite colors. |
| text readiness | `game.h.f()` writes text only when widget 1 animation `g()==9` and `b(3)` | `PORTED`: TextBox source UI text now waits for cursor >= 3 before rendering openbox text. |
| text id 2 | x=47,y=154,w=146, centered/marquee if overflow | `PORTED/PARTIAL`: centered/marquee text represented and smoke asserts text pixels; full generic `ab/af/k/ae` text runtime still not ported. |

## Smoke Checkpoints

New/required checkpoints:
- `battle_choice_ui_scroll_source_rows`
- `battle_msgwarm_source_widget_warning`
- `battle_openbox_source_widget_catch_success`
- `battle_p17_q1_h_effect_order`
- `battle_p17_q4_fail_restore_enemy`

Regression checkpoints:
- `battle_catch_missing_count_warning_return_p21`
- `battle_catch_storage_bag`
- `battle_catch_storage_bank`
- `battle_bunny_first_catch_fail_escape_effect`
- `battle_catch_success_q3_flash_mid`
- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`

## Remaining Partial

Still not allowed to claim complete pixel parity:
- MIDP original pixel comparison for P17 q1/q4.
- Exact Java ME `drawRGB` alpha/color behavior after `l.a(...)` and `l.b(...)`.
- Full generic `ab/af` widget runtime for every UI file. The current slice ports the battle call sites of `choice.ui`, `msgwarm.ui`, and `openbox.ui`, not the entire UI framework.
