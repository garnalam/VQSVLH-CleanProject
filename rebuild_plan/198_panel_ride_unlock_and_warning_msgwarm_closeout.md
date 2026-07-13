# 198 - Panel ride.ui q.N ids 1..4 unlock smoke and warning msgwarm closeout

Date: 2026-07-10

Target slice:

- Seed source q.N ids `1..4` in smoke to prove unlocked ride slot rendering.
- Port `ride.ui` confirm warnings through visible `msgwarm.ui`.
- Do not port `q.h()` ride mutation.
- Do not touch q.N cases `6/10/7/8/9`.
- Do not touch top-level `bag.ui` default item-use/state 17.

## Source anchors

`modules/source_code/decoded/decompiled_source_cfr/game/g.java`:

- `i(id)`:
  - id `0` creates egg row `[0,0,0]`;
  - nonzero id creates row `[id,1,0]`;
  - ids `1..4` also set `P[id - 1] = 1`.
- `f(i)` returns `P[i] != 0`, used by `ride.ui` to decide whether a ride is
  unlocked.
- `g(i)` returns `Q[i] != 1`, used by `ride.ui` to decide whether an unlocked
  ride can be used at the current location.
- `h(i)` mutates active ride state. This remains pending.

`modules/source_code/decoded/decompiled_source_cfr/game/h.java`:

- `ad()` loads `/data/ui/ride.ui`, sets selected ride `b = 0`, then calls
  `bm()`.
- `bm()` renders 4 ride slots from `P[]`/`Q[]`.
- `ae()` confirm behavior:
  - if `!q.f(b)`: warning `Chua co sung vat cuoi nay`;
  - else if `!q.g(b)`: warning `Noi nay khong the su dung sung vat cuoi`;
  - else call `q.h(b)`, close ride UI, return to world.

## Rebuild changes

- Added `sourceRideBlocked[4]` to `VqsvIntroDemo.Scene` as a small source-shaped
  mirror of `game.g.Q[]` for warning/smoke only.
- Persisted `sourceRideBlocked` in `VqsvSaveRuntime`.
- `VqsvPanelRuntime.tickRide()` now opens `TextBox.msgWarm(...)` for:
  - locked ride: `q.f(i)=false`;
  - unusable ride: `q.f(i)=true`, `q.g(i)=false`.
- Closing the warning returns to `ride.ui`.
- `sourceRideUsable()` now checks `sourceRideBlocked[i] != 1`.
- Added smoke helper that opens ride through the real UI path:
  `gamemenu.ui -> bag.ui b=3 -> q.N case5 -> ride.ui`.
- Smoke seeding for q.N ids `1..4` maps source `game.g.i(id)` to ride unlocks.

## Status

- q.N ids `1..4` smoke seed -> ride unlock state: PORTED/PARTIAL.
- `ride.ui` unlocked slot render: PORTED/PARTIAL.
- locked ride warning `msgwarm.ui`: PORTED/PARTIAL.
- unusable ride warning `msgwarm.ui`: PORTED/PARTIAL.
- warning close back to `ride.ui`: PORTED/PARTIAL.
- exact full `msgwarm.ui` text marquee/crop runtime: PORTED/PARTIAL from prior
  generic msgwarm work, not pixel-perfect claimed here.
- `q.h(i)` ride mutation: PENDING.
- q.N cases `6/10/7/8/9`: PENDING.
- top-level `bag.ui` default item-use/state 17: PENDING.

## Smoke PNG

Focused:

- `panel_bag_ride_unlocked_slots`
- `panel_bag_ride_locked_warning_msgwarm`
- `panel_bag_ride_unusable_warning_msgwarm`
- `panel_bag_ride_warning_close_returns_ride`

Regression:

- `panel_bag_special_reward5_render`
- `panel_bag_special_reward5_confirm_pending`
- `panel_bag_special_reward5_ride_navigation`
- `panel_bag_special_reward5_ride_back_returns_world`
- `panel_bag_egg_hatch_result_to_bag`
- `route_elder_after_battle_reward_state`
- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`

Next recommended decision:

1. If keeping ride path: audit/port `q.h(i)` mutation and its map/movement
   side effects from source `game.g.h`, `a(int, boolean)`, speed `d[0]`, active
   ride `P[i]=2`.
2. If deferring ride mutation: move to top-level `bag.ui` default item-use /
   state 17 branches.
