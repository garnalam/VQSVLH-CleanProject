# 199 - Panel ride.ui q.h mutation closeout

Date: 2026-07-10

Target slice: audit and port the safe source-backed part of `game.g.h(int)`
called from `game.h.ae()` ride confirm.

This slice does not touch top-level `bag.ui` default item-use/state 17.

## Source proof

`modules/source_code/decoded/decompiled_source_cfr/game/h.java`:

- `ae()` confirm:
  - if `q.f(b)` and `q.g(b)` pass, call `q.h(b)`;
  - close `/data/ui/ride.ui`;
  - `o.a((byte)0)` returns to world.

`modules/source_code/decoded/decompiled_source_cfr/game/g.java`:

```java
public final void h(int n2) {
    if (n2 == -1) {
        return;
    }
    this.P[n2] = 2;
    this.a.b();
    this.a(n2 + 1, false);
    if (this.u == 1) {
        this.a(1, 107);
    }
    this.b((byte)0, this.n);
    this.d[0] = this.P[n2] == 2 && n2 == 0 || this.P[n2] == 2 && n2 == 1 ? 8 : 4;
    if (this.P[2] == 2 && game.k.a().o != null) {
        game.k.a().o.d();
    }
    this.Z = this.d[0];
    this.t = n2;
}
```

Important downstream source effects:

- `P[n2] = 2`: active ride state.
- `a(n2 + 1, false)`: swap player visual/sprite mode through `game.f/an`.
- `b((byte)0, n)`: reset player movement animation toward current direction.
- `d[0]`: movement step. Ride `0/1` => speed `8`; ride `2/3` => speed `4`.
- `Z = d[0]`: collision/movement probe speed.
- `t = n2`: active ride index.
- `P[2] == 2 && game.k.a().o != null`: extra map/object side effect.

## Rebuild changes

- Added source-shaped scene state:
  - `sourceRideActiveIndex`, default `-1`;
  - `sourcePlayerMoveSpeed`, default `4`.
- Persisted both fields in `VqsvSaveRuntime`.
- `VqsvPanelRuntime.tickRide()` confirm usable ride now:
  - sets `sourceRideActiveIndex = selectedRide`;
  - sets `sourcePlayerMoveSpeed = 8` for ride `0/1`, `4` for ride `2/3`;
  - calls `player.applyMode(0)` as a small animation reset approximation;
  - closes `ride.ui`;
  - traces pending visual sprite swap.
- `VqsvFreeWorldRuntime.tickFreeWorldPlayer()` now uses
  `sourcePlayerMoveSpeed` instead of hard-coded speed `4`.

## Status

- `game.h.ae()` confirm success path: PORTED/PARTIAL.
- `game.g.h()` active ride index `t`: PORTED/PARTIAL as
  `sourceRideActiveIndex`.
- `game.g.h()` movement speed `d[0]` / `Z`: PORTED/PARTIAL as
  `sourcePlayerMoveSpeed` used by free-world movement.
- close `ride.ui -> P=0`: PORTED/PARTIAL.
- player visual sprite swap `a(n2 + 1, false)`: PENDING.
- `u == 1` visual variant side effect: PENDING.
- `P[2] == 2 && game.k.a().o.d()` map/object side effect: PENDING.
- exact source `P[]` multi-active semantics: PORTED/PARTIAL. Rebuild keeps a
  single `sourceRideActiveIndex` for current gameplay.
- dismount `game.g.s()`: PENDING.
- q.N cases `6/10/7/8/9`: PENDING.
- top-level `bag.ui` default item-use/state 17: PENDING.

## Smoke PNG

Focused:

- `panel_bag_ride_confirm_success_mutation`
- `panel_bag_ride_confirm_speed_world_movement`

Regression:

- `panel_bag_ride_unlocked_slots`
- `panel_bag_ride_locked_warning_msgwarm`
- `panel_bag_ride_unusable_warning_msgwarm`
- `panel_bag_ride_warning_close_returns_ride`
- `panel_bag_special_reward5_render`
- `panel_bag_special_reward5_confirm_pending`
- `panel_bag_special_reward5_ride_navigation`
- `panel_bag_special_reward5_ride_back_returns_world`
- `panel_bag_egg_hatch_result_to_bag`
- `route_elder_after_battle_reward_state`
- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`

Next recommended slice:

Move to top-level `bag.ui` default item-use/state 17 branches, because the
remaining ride work is mostly visual/player-sprite and map-object parity rather
than blocking the current panel route.
