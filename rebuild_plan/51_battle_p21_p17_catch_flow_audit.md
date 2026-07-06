# Battle P21/P17 Catch Flow Audit

Date: 2026-07-06

Scope: source-backed audit for the capture flow before porting more battle UI.
This document only covers `P21` ball selection and `P17` capture result.

## Source Facts

Primary source files:

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/f.java`
- `modules/script/decoded/data__script__sprite.mid.json`
- `modules/spr/original/spr_269_all(r)`
- `modules/img/decoded/data__img__img_309.mid.png`

Verified facts:

| Source | Fact | Status |
|---|---|---|
| `game.d` state entry `case 21` | sets target `h.p = d[0]`, then calls `S.ah()` | PORTED |
| `game.h.ah()` | opens `/data/ui/choice.ui` | PORTED/PARTIAL |
| `game.h.ah()` | widget 8 = `Pokemon ball`, 9 = `Tỉ lệ bắt`, 5 = `Sử dụng` | PORTED |
| `game.h.ah()` | ball rows come from `q.K`, icon from `aq.c[4][ballId][1]`, name from `aq.c[4][ballId][0]`, chance from `game.d.b(ballId)` | PORTED |
| `game.h.ai()` | confirm checks item count, sets `game.d.l`, calls tutorial hook `o.m()`, consumes item, enters `P17`, closes `choice.ui` | PORTED/PARTIAL |
| `game.h.ai()` | missing count opens `/data/ui/msgwarm.ui` with `Số lượng Pokemon ball không đủ` | PORTED/PARTIAL |
| `game.d` state entry `case 17` | creates/uses root `f aj`, loads sprite `269`, positions at enemy `h.i/h.j`, activates it, sets phase `q=0` | PORTED |
| `game.d.e(byte)` | q1/q4 create `ah` type 8 secondary effect using source short arrays and enemy sprite id | PORTED/PARTIAL |
| `game.d` P17 tick | runs phase `q=0..4`; success is q=3, fail is q=4; phase timing is now driven by sprite-frame end plus `ah` duration | PORTED/PARTIAL |
| `game.g.y()` | storage result: active bag `<6` => 0, bank `<100` => 1, otherwise 2 | PORTED |
| `game.d.b(int itemId)` | catch chance formula: HP threshold, ball param `[6]`, target quality, status multiplier, attacker status 11, relation class `[22]`, level cap, clamp `1..100`; item 0 returns 100 | PORTED/PARTIAL |
| `sprite.mid` | row `269,309` | PORTED |
| Asset files | `spr_269_all(r)` and decoded `img_309` exist | PORTED |

## State Matrix

| State | Source method | UI/resource | Input | Side effect | Next state |
|---|---|---|---|---|---|
| P20 command | `game.h` battle command handling | `battle.ui` | choose Catch | source enters battle state 21 if catch allowed | P21 |
| P21 enter | `game.d case 21` -> `game.h.ah()` | `/data/ui/choice.ui` | none | target is enemy slot `d[0]`; list is populated from capture-ball bag rows | P21 wait |
| P21 wait | `game.h.ai()` | `/data/ui/choice.ui` | up/down | selected row changes; quantity label refreshed | P21 |
| P21 confirm | `game.h.ai()` | `/data/ui/choice.ui` | confirm | if count exists: set selected ball id, tutorial `m()`, consume 1 item | P17 |
| P21 missing item | `game.h.ai()` | `/data/ui/msgwarm.ui` | confirm | warning message, no consume | P21 or P101 special source branch |
| P21 back | `game.h.ai()` | closes choice UI | back | no consume | P20 |
| P17 enter | `game.d case 17` | sprite `269` (`spr_269_all(r)`, image `309`) | none | start capture animation at enemy, compute `ak` catch success | P17 q0 |
| P17 q0/q1/q2 | `game.d case 17` tick | `aj` animation plus secondary `H` effect | none | waits for animation/effect end | q3 if caught, q4 if failed |
| P17 q3 success | `game.d case 17` tick | openbox text | text confirm | add caught pet to bag/bank or release if full | world return |
| P17 q4 fail | `game.d case 17` tick | openbox or battle dispatch | text/none | enemy visible again, player action consumed | P1 dispatch |

## Item Matrix

Current rebuild row source is `aq.c[4]` via `VqsvBattleTables`.

| Item id | Source meaning in current rebuild | UI name source | Icon source | Consume | Catch param |
|---|---|---|---|---|---|
| 0 | perfect/tutorial ball, `game.d.b(0) == 100` | `aq.d[aq.c[4][0][0]]` | `aq.c[4][0][1]` | consume 1 on confirm | special 100 |
| 1..n with `aq.c[4][id][5] == 0` | capture balls | `aq.d[aq.c[4][id][0]]` | `aq.c[4][id][1]` | consume 1 on confirm | `aq.c[4][id][6]` |

## Rebuild Status After This Slice

PORTED:

- P21 source-shaped entry, list, confirm/back, item consume.
- P21 list icons now use `aq.c[4][itemId][1]` with sprite 258, matching the source widget setup.
- P17 now has explicit source-timed phases `0..4`, not instant resolve.
- P17 uses source sprite id `269` and source asset row `269,309`.
- P17 q1/q4 hide the enemy and render a source-shaped `ah` type 8 enemy-copy effect using the original offsets/durations from `game.d.e(byte)`.
- Catch success storage follows `game.g.y()`: add to bag, add to bank, or release when both are full.
- Bunny route still returns result `-1` and reaches the existing "return elder" task route.

PARTIAL:

- P21 uses a source-shaped choice renderer, not the full original `game.h` widget runtime.
- P17 phase timing uses actual sprite frame cursor/end checks and `ah` type 8 duration, but bitmap transforms inside root `l/e` are still approximated by drawing the enemy sprite copy at source offsets.
- Bag/bank capacity behavior is ported, but full captured-pet serialized payload `((b)h.p).P()` is still represented by `SourcePetState`, not the complete original save array.
- `game.d.m()/l()` tutorial prompt sequence is represented by route hooks; the full tutorial state machine `U/V` is not fully ported.

PENDING:

- Full generic `game.h` widget runtime for all `choice.ui`/`battle.ui` widgets.
- Exact P17 secondary bitmap transforms from root `l/e` helpers.
- Full caught-pet save payload parity with `((b)h.p).P()`.

## Update 2026-07-06: P17 Type 8 And Storage Tightening

Source-backed changes now ported:

- `game.d.e(byte)` q1 short array is represented as type 8 steps:
  scale/offset `(10,0,0) -> (7,0,-10) -> (4,0,-20)`, duration `9`,
  group count `3`.
- `game.d.e(byte)` q4 short array is represented as type 8 steps:
  scale/offset `(4,0,-20) -> (6,0,-12) -> (8,0,-4) -> (10,0,0)`,
  duration `8`, group count `4`, reset-each-step flag matching `t[4] == 1`.
- The runtime now keeps `ah`-like `t0/t1/t2/t3/t4` counters for P17 secondary
  effect timing instead of a flat phase duration.
- The renderer no longer draws a full enemy sprite copy for `H`; it renders
  source cell `0` of the target sprite as a cloned RGB effect, applies nearest
  scale, source offsets, and a `+50` RGB brighten pass corresponding to
  `l.b(..., 1, 50)`.
- Captured pet storage now records a `sourcePayload` with the same outer shape
  as `game.b.P()`: base 10 fields plus skill id/PP pairs. Bag/bank/full routing
  still follows `game.g.y()`.

Still not claimed as 100%:

- The type 8 renderer is source-backed but not byte-for-byte MIDP `drawRGB`
  parity. The original `l.a(d, cell, bounds, orientation, e)` renders through
  the MIDP image pipeline; the rebuild uses Java2D ARGB and nearest scaling.
- `sourcePayload` has `game.b.P()` shape and uses the current `BattleUnit`
  fields, but full save/global parity is still pending until the whole
  `game.g` pet inventory/save model is ported.
- `choice.ui` and `battle.ui` are still rendered by a battle-specific
  source-shaped renderer. A generic `game.h` widget runtime remains pending.

Smoke after this update:

- `rebuild_game/build_intro_demo/battle_choice_ui_after_type8.png`
- `rebuild_game/build_intro_demo/battle_p17_type8_effect.png`
- `rebuild_game/build_intro_demo/battle_catch_fail_type8.png`
- `rebuild_game/build_intro_demo/battle_storage_bank_payload.png`
- `rebuild_game/build_intro_demo/battle_storage_full_payload_release.png`
- `rebuild_game/build_intro_demo/route_sophie_after_type8.png`
- `rebuild_game/build_intro_demo/route_bunny_after_type8.png`
- `rebuild_game/build_intro_demo/route_elder_after_type8.png`
