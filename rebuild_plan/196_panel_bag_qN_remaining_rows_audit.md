# 196 - Panel bag.ui b==3 q.N remaining rows audit

Date: 2026-07-10

Scope: audit remaining source rows in `bag.ui` tab `b == 3` / `game.g.N`
after `q.N case 0` egg hatch was ported. This audit answers whether the
current rebuilt route touches cases `5/6/10/7/8/9`, and whether we should
continue q.N or switch to top-level `bag.ui` default item-use / state 17.

## Source anchors

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
  - `bl()` renders `q.N` rows into `bag.ui` widgets `137/138/139...`,
    description widget `163`, scrollbar widget `162`.
  - `ac()` handles confirm in `bag.ui`.
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
  - `N` is the special/egg/ride vector.
  - `i(id)` unlocks/creates special q.N rows.
  - `c(id, qty)` stacks special rows, especially ids `7/8/9`.
  - `e(id, n3)` consumes/activates special rows.
  - `j(0)` removes/closes egg row.
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
  - save/load persists q.N.
- `modules/script/decoded/data__script__db.mid.json`
  - table group `5` backs q.N names/icons/descriptions.

## Source behavior matrix

| q.N id | Source confirm path in `game.h.ac()` | Source UI/action label | Current route reachability | Rebuild status |
|---:|---|---|---|---|
| 0 | hatch action in-place; `game.k.r()`, `q.y()`, `q.j(0)`, `msgwarm.ui`, then result text | `Ap trung` when active | Reachable via panel egg flow smoke/state seeding | PORTED/PARTIAL: hatch mutation + warning/result UI ported; exact full `game.h` runtime still partial |
| 5 | `o.a((byte)11)`, close `/data/ui/bag.ui`, enter `game.h.ad()` / `/data/ui/ride.ui` | `Mo ra` | REACHABLE: Elder post-battle script calls `op19SpecialReward(5, 1)` | PORTED/PARTIAL: source-backed row render + confirm trace/assert only; `ride.ui` runtime PENDING |
| 6 | `o.a((byte)12)`, close `/data/ui/bag.ui` | `Mo ra` | Not proven reachable in current route | PENDING: source row supported by DB metadata, no route consumer port |
| 10 | `o.a((byte)24)`, close `/data/ui/bag.ui` | `Mo ra` / `Gia toc` label in source render path | Not proven reachable in current route | PENDING: source row supported by DB metadata, no route consumer port |
| 7 | `s = id`, `o.a((byte)19)`, close `/data/ui/bag.ui` | `Su dung` | Not proven reachable in current route | PENDING: special-use state 19 not ported |
| 8 | `s = id`, `o.a((byte)19)`, close `/data/ui/bag.ui` | `Su dung` | Not proven reachable in current route | PENDING: special-use state 19 not ported |
| 9 | `s = id`, `o.a((byte)19)`, close `/data/ui/bag.ui` | `Su dung` | Not proven reachable in current route | PENDING: special-use state 19 not ported |

## Current rebuild checks

- `Scene1Room0Group6ElderBattleScript.java` calls `op19SpecialReward(5, 1)`
  after Elder battle.
- `VqsvSourceOps.op19SpecialReward()` creates/updates a `SourceSpecialReward`
  with source table group `5` metadata and source-shaped `game.g.i(id)` /
  `game.g.c(id, qty)` semantics.
- `VqsvSaveRuntime` persists `sourceSpecialRewards`, so q.N special row state
  survives save/load.
- `VqsvPanelRuntime.bagRows(..., bagTab == 3)` renders:
  - egg row `0`;
  - visible `sourceSpecialRewards`, sorted by special id.
- `VqsvPanelRuntime.useSpecialBagRow()`:
  - ports case `0` hatch;
  - traces/asserts case `5` as pending `ride.ui`;
  - traces cases `6/10/7/8/9` as pending.
- `VqsvSmokeHarness` has focused PNG checkpoints:
  - `panel_bag_special_reward5_render`;
  - `panel_bag_special_reward5_confirm_pending`.

## Decision

Do not skip q.N entirely and do not switch blindly to top-level default
`bag.ui` item-use/state 17 yet. The condition "if q.N remaining rows are not
reachable" is false because row `5` is reachable from the Elder route.

Recommended next implementation slice:

1. Port `q.N case 5` entry path: `game.h.ac()` confirm -> `o.a((byte)11)`
   -> `game.h.ad()` / `game.h.ae()` `/data/ui/ride.ui`, render/navigate/back
   first.
2. Keep cases `6/10/7/8/9` as PENDING until a source route or save state proves
   they are reachable in the current rebuild path.
3. After row `5` has render/navigation smoke, then return to top-level
   `bag.ui` default item-use/state 17 branches.

## Verification target

Minimum smoke set for this audit/slice:

- `panel_bag_special_reward5_render`
- `panel_bag_special_reward5_confirm_pending`
- `panel_bag_egg_hatch_result_to_bag`
- `route_elder_after_battle_reward_state`
- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`

Status summary:

- `q.N case 0`: PORTED/PARTIAL.
- `q.N case 5`: PORTED/PARTIAL render + trace-only confirm; `ride.ui` PENDING.
- `q.N cases 6/10/7/8/9`: PENDING, source-supported but not route-proven.
- Top-level `bag.ui` default item-use/state 17: PENDING; next after case 5
  unless user explicitly chooses to defer `ride.ui`.
