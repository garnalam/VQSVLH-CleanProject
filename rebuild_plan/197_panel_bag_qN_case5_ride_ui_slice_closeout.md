# 197 - Panel bag.ui q.N case 5 ride.ui render/navigation closeout

Date: 2026-07-10

Target slice: port the smallest source-backed path for `bag.ui b == 3`,
`q.N case 5`:

`game.h.ac()` confirm -> `o.a((byte)11)` -> close `/data/ui/bag.ui`
-> `game.h.ad()` loads `/data/ui/ride.ui` -> `game.h.ae()` handles
left/right/back.

## Source proof

Source `modules/source_code/decoded/decompiled_source_cfr/game/h.java`:

- `ad()`:
  - calls `aS()`;
  - loads `/data/ui/ride.ui` with sprite table `257`;
  - sets `b = 0`;
  - calls `bm()`.
- `bm()`:
  - iterates 4 ride slots;
  - uses sprite `260` for ride icons;
  - uses sprite `257`, cell `131`, for unavailable overlay;
  - `q.f(i)` means ride slot is owned/unlocked (`P[i] != 0`);
  - selected unlocked slot uses icon cell `i`;
  - unselected unlocked slot uses icon cell `i + 8`;
  - locked slot uses icon cell `i + 4`;
  - selected unlocked slot shows name:
    - `0`: `Luc di dieu`
    - `1`: `Hu khong hanh gia`
    - `2`: `Hai au`
    - `3`: `Nham son long`
- `ae()`:
  - left/right move selection;
  - back closes `/data/ui/ride.ui` and returns to state `P=0`;
  - confirm calls `q.h(b)` only if `q.f(b)` and `q.g(b)` pass, otherwise
    shows warning.

Source `modules/source_code/decoded/decompiled_source_cfr/game/g.java`:

- `i(id)` with ids `1..4` sets `P[id - 1] = 1`.
- `f(i)` is `P[i] != 0`.
- `g(i)` is `Q[i] != 1`.
- `h(i)` mutates active ride state and movement speed.

## Rebuild changes

- `VqsvPanelRuntime`:
  - added `Mode.RIDE`;
  - `q.N case 5` now opens `ride.ui` instead of remaining render-only;
  - added `tickRide()` for left/right/back trace;
  - added `renderRide()` using `/data/ui/ride.ui`, sprite `260`, and sprite
    `257` overlay cell `131`;
  - derives source ride ownership from `SourceSpecialReward` ids `1..4`
    because source `game.g.i(1..4)` maps to `P[0..3]`.
- `VqsvSmokeHarness`:
  - updated `panel_bag_special_reward5_confirm_pending` to assert that case 5
    now opens `RIDE`;
  - added `panel_bag_special_reward5_ride_navigation`;
  - added `panel_bag_special_reward5_ride_back_returns_world`.

## Status

- `q.N case 5 -> ride.ui open`: PORTED/PARTIAL.
- `ride.ui render`: PORTED/PARTIAL source-backed layout/icons; exact full
  `ao/af/k/m` runtime remains partial.
- `ride.ui navigation left/right`: PORTED/PARTIAL.
- `ride.ui back close to world/P=0`: PORTED/PARTIAL.
- `ride.ui confirm q.h()` mutation: PENDING.
- `ride.ui` warnings as visible `msgwarm.ui`: PENDING; this slice only traces
  warning branches.
- `q.N cases 6/10/7/8/9`: PENDING, untouched.
- top-level `bag.ui` default item-use/state 17: PENDING, untouched.

Important route note: current Elder route rewards q.N id `5`, which opens
ride.ui. It does not unlock any ride slot by itself. Source ride slot unlocks
come from q.N ids `1..4`, so current route correctly displays locked ride
icons unless those rewards are present.

## Smoke PNG

Focused checkpoints:

- `panel_bag_special_reward5_confirm_pending`
- `panel_bag_special_reward5_ride_navigation`
- `panel_bag_special_reward5_ride_back_returns_world`

Regression checkpoints:

- `panel_bag_special_reward5_render`
- `panel_bag_egg_hatch_result_to_bag`
- `route_elder_after_battle_reward_state`
- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`

Next recommended slice:

1. Either polish `ride.ui` unlocked-slot smoke by seeding q.N ids `1..4`, still
   without `q.h()` mutation.
2. Then port `ride.ui` confirm warning UI (`msgwarm.ui`) for locked/unusable
   ride.
3. Only after that decide whether to port `q.h()` ride mutation or move to
   top-level `bag.ui` default item-use/state 17.
