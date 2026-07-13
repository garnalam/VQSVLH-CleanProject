# 233 - Panel item 13 world avoid timer closeout

Date: 2026-07-13

Scope: close the world-side effect of item `13` after the top-level bag route
has already consumed the item and set `q.x/q.w`.

This slice does not port the full random encounter generator. It ports the
source timer primitive and encounter gate that the generator uses.

## Source anchors

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
  - `game.h.ac()` item `13` success sets:
    - `q.x = aq.c[4][13][6]`;
    - `q.w = 0`;
    - `q.c(1)`.
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
  - `game.g.O()` runs during free-world movement state.
  - `--q.w`; if `q.w <= 0`, source calls `game.k.q()` and resets `q.w = 0`.
  - `--q.x`; if `q.x == 0`, source calls `this.a.a(0)` and sets `q.x = -1`.
  - while `q.x > 0`, source returns before decreasing encounter countdown.
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
  - random encounter check runs only after `game.g.D()` says the encounter
    countdown is ready.
  - `game.k.q()` is an empty method in the decompiled source.

## Rebuild mapping

| Source | Rebuild | Status |
|---|---|---|
| `q.x` active avoid duration | `Scene.sourceAvoidMonsterTicks` | PORTED |
| `q.w` raw helper counter | `Scene.sourceAvoidMonsterElapsed` | PORTED/PARTIAL name is legacy; behavior now follows source normalization |
| `game.g.O()` free-world movement timer tick | `VqsvFreeWorldRuntime.tickSourceWorldTimers()` called from `tickFreeWorldPlayer()` after a successful move | PORTED |
| `q.x > 0` blocks encounter countdown | `sourceAvoidMonsterBlocksEncounter()` | PORTED |
| `q.x == 0 -> q.x = -1` expire | `tickSourceWorldTimers()` | PORTED |
| `game.k.q()` | no-op by source; rebuild normalizes `q.w` to `0` | PORTED |

## Smoke checkpoints

- `item13_world_avoid_blocks_encounter`
  - seeds `q.x = aq.c[4][13][6]`;
  - asserts `sourceAvoidMonsterBlocksEncounter() == true`.
- `item13_world_avoid_decrements_on_move`
  - seeds `q.x = 500`;
  - performs one free-world movement tick;
  - asserts `q.x == 499` and encounter remains blocked.
- `item13_world_avoid_expires_unblocks`
  - seeds `q.x = 1`;
  - performs one free-world movement tick;
  - asserts `q.x == -1` and encounter no longer blocks.

## Status

Item `13` world timer/decrement and encounter gate are PORTED/SMOKE-LOCKED for
the currently implemented free-world movement runtime.

Remaining outside this slice:

- full source random encounter generation vectors in `game.k`;
- exact encounter countdown field `game.g.v` and its map/tile-dependent seed;
- exact global map parity for every source map id.

## Next

The item `13` item-use route and world effect are now closed enough to leave the
item domain. Next reasonable work is either:

1. item `14` after-battle egg progress increment from `game.d`; or
2. full random encounter generator audit/port from `game.k` if free-world wild
   battles are the target.
