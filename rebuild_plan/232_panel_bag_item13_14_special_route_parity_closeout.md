# 232 - Panel bag item 13/14 special route parity closeout

Date: 2026-07-13

Scope: close the top-level `/data/ui/bag.ui` item-use branches for source
item `13` and item `14` from `game.h.ac()`.

This document is intentionally narrow. It covers the item rows in `aq.c[4]`
when selected from bag tab `b == 0`. It does not claim full parity for the
separate `q.N` special tab runtime, ride UI, or egg hatch action.

## Source anchors

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
  - `game.h.ac()` handles `/data/ui/bag.ui` input.
  - `b == 0` selects normal/special item rows from `q.K + q.J`.
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
  - `q.b(item,1,0)` checks ownership.
  - `q.d(item,1,0)` consumes one item.
  - `q.c(id,qty)` stacks `q.N` special row third field.
- `modules/script/decoded/data__script__db.mid.json`
  - `aq.c[4][13] = [509,53,510,300,0,10,500]`.
  - `aq.c[4][14] = [277,41,294,1,1,9]`.

## Item 13 - avoid monster

Source branch:

- if `q.x > 0`: show already-active `msgwarm.ui`, no mutation.
- else if current source room is `f == 3 && g == 7`: show forbidden
  `msgwarm.ui`, no mutation.
- else if `q.b(13,1,0)` passes:
  - `q.d(13,1,0)`;
  - `q.x = aq.c[4][13][6]`;
  - `q.w = 0`;
  - `bk()` refreshes bag rows/cursor;
  - `E()` opens `msgwarm.ui`;
  - `q.c(1)`;
  - success text, `f = 1`.

Rebuild mapping:

| Source | Rebuild | Status |
|---|---|---|
| `q.x` | `Scene.sourceAvoidMonsterTicks` | PORTED |
| `q.w` | `Scene.sourceAvoidMonsterElapsed` | PORTED |
| `aq.c[4][13][6] = 500` | `BattleItemRow.paramA` | PORTED |
| `q.b(13,1,0)` | `VqsvSourceOps.sourceCanRemoveItem(s,13,1)` | PORTED |
| `q.d(13,1,0)` | `VqsvSourceOps.sourceRemoveItem(s,13,1)` | PORTED |
| forbidden `f/g == 3/7` | `currentSceneId == 3 && currentRoomIndex == 7` | PORTED for current rebuild scene ids |
| `bk()` cursor/list refresh | `bagRows(...)` refresh plus selected clamp | PORTED |
| `E(); a(...); f = 1` | `TextBox.msgWarm(...)`, `bagMessageMode=15` | PORTED |
| `q.c(1)` | `sourceStackSpecialReward(s,1,1)` -> `q.N` stack field | PORTED |

Status: `bag.ui b=0 itemId=13` is PORTED/SMOKE-LOCKED for the checked source
branches.

Remaining outside this route:

- global proof that every source `game.k.a().f/g` map id maps exactly to the
  rebuild scene/room ids.

2026-07-13 follow-up:

- Runtime decrement/expiry of `q.x/q.w` and the encounter gate are closed in
  `233_panel_item13_world_avoid_timer_closeout.md`.

## Item 14 - egg accelerator

Source branch:

- condition for success:
  - `q.k(0)` is true; and
  - `q.I == 0 && game.k.q < 10`, or `q.I > 0 && game.k.q < 30`.
- on success:
  - `game.k.q = 10` for `q.I == 0`;
  - `game.k.q = 30` for `q.I > 0`;
  - `q.d(14,1,0)`;
  - `bk()` refreshes bag rows/cursor;
  - open `msgwarm.ui`, success text, `f = 1`.
- otherwise:
  - open `msgwarm.ui`, no-egg/already-ready warning, no mutation.

Rebuild mapping:

| Source | Rebuild | Status |
|---|---|---|
| `q.k(0)` | `Scene.sourceEggActive` | PORTED |
| `q.I` | `Scene.sourceEggType` | PORTED |
| `game.k.q` | `Scene.sourceEggProgress` | PORTED |
| type `0` threshold `10` | `sourceEggType == 0 ? 10 : 30` | PORTED |
| type `>0` threshold `30` | `sourceEggType == 0 ? 10 : 30` | PORTED |
| `q.b(14,1,0)` | `VqsvSourceOps.sourceCanRemoveItem(s,14,1)` | PORTED |
| `q.d(14,1,0)` | `VqsvSourceOps.sourceRemoveItem(s,14,1)` | PORTED |
| `bk()` cursor/list refresh | `bagRows(...)` refresh plus selected clamp | PORTED |
| `E(); a(...); f = 1` | `TextBox.msgWarm(...)`, `bagMessageMode=17/16` | PORTED |

Status: `bag.ui b=0 itemId=14` egg accelerator is PORTED/SMOKE-LOCKED for the
checked source branches.

Remaining outside this route:

- after-battle egg progress increment in `game.d`;
- separate `bag.ui b=3 q.N case0` hatch action details beyond existing
  source-shaped hatch smoke;
- full `q.N` special tab widget runtime.

## Smoke checkpoints

Focused item 13:

- `panel_bag_item13_success_msg`
- `panel_bag_item13_success_returns_bag`
- `panel_bag_item13_already_warning`
- `panel_bag_item13_forbidden_warning`

Focused item 14:

- `panel_bag_item14_no_egg_warning`
- `panel_bag_item14_type0_success`
- `panel_bag_item14_type1_success`
- `panel_bag_item14_already_ready_warning`
- `panel_bag_item14_success_returns_bag`

Regression should include:

- `panel_wheel`
- `battle_quick`
- route smoke for Sophie/Bunny/Elder

## Closeout

Top-level item `13/14` special route parity is closed for the source-proven
bag tab `b == 0` branches.

Do not reopen item `13/14` normal-use logic unless a new source branch is found.
Future work should target the explicitly separate systems:

1. after-battle egg progress increment for item 14;
2. full source random encounter generator from `game.k`;
3. `q.N` special tab rows/runtime.
