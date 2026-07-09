# 131 Battle P17 AH Type 8 Effect Checkpoint

## Scope

Tighten P17 catch visual parity for the remaining `ah` type `8` target-copy effect:

- q1 capture shrink.
- q4 escape/release effect.

This slice does not change catch decision, catch storage, P21 list behavior, sprite 269 timing, Bunny tutorial gating, or RNG.

## Source Chain

| Source | Finding | Status |
| --- | --- | --- |
| `game.d.e(byte)` case `17` q1 | Builds `short[]{8, enemy.i, enemy.j, speciesSprite, 0, enemy.n, 0, 9, 1, 3, 0, 10, 0, 0, 7, 0, -10, 4, 0, -20}`. | PORTED/PARTIAL |
| `game.d.e(byte)` case `17` q4 | Builds `short[]{8, enemy.i, enemy.j, speciesSprite, 0, enemy.n, 0, 8, 1, 4, 1, 4, 0, -20, 6, 0, -12, 8, 0, -4, 10, 0, 0}`. | PORTED/PARTIAL |
| `ah.a(short[])` case `8` | Copies `sArray[6..]` to `t`, stores actor position from `sArray[1..2]`, renders target sprite cell `0` through `l.a(d, cell, bounds, direction, e)`. | PORTED/PARTIAL |
| `ah.e()` case `8` | Step timing is `t[0] < t[1] / t[3] * t[2]`; per step does `b[1] = l.b(l.a(b[1], scale10), 1, 50)`, then adds dx/dy to `b[1].d/e`; q4 has `t[4] == 1` so it resets from `b[0]` each tick. | PORTED |
| `ah.a(Graphics)` case `8` | Draws only `b[1]` using Java ME `drawRGB(..., this.i + b[1].d, this.j + b[1].e, ..., true)`. | PORTED/PARTIAL |
| `l.a(e,int)` | Scales bitmap nearest-neighbor by tenths and scales origin offsets `d/e` by the same tenths. | PORTED/PARTIAL |
| `l.b(e,1,50)` | Brightens each RGB channel by `+50`, clamps to `0..255`. | PORTED |

## Rebuild Change

`VqsvBattleRenderer.drawCatchEffectType8` now follows the source draw shape:

1. Load enemy species sprite.
2. Render cell `0` into an offscreen bitmap using source cell bounds.
3. Brighten visible pixels by `+50`.
4. Scale nearest-neighbor by `battleCatchEffectScale10`.
5. Draw at actor origin + scaled source origin + q1/q4 dx/dy.

Before this slice, rebuild centered the scaled bitmap inside the enemy battle rect. That was visually plausible but not source-shaped because source `ah` uses `this.i + b[1].d`, not rect-centered placement.

## Step Tables

| Phase | Source copied `t` | Runtime checkpoints |
| --- | --- | --- |
| q1 | `[0,9,1,3,0,10,0,0,7,0,-10,4,0,-20]` | shrink checkpoints include `[7,0,-10]`; final shrink reaches `[4,0,-20]`. |
| q4 | `[0,8,1,4,1,4,0,-20,6,0,-12,8,0,-4,10,0,0]` | escape checkpoints include `[6,0,-12]`; source reset flag `t[4]=1` is represented by `resetEachTick=true`. |

## Verification

- Focused smoke checkpoint added: `battle_p17_ah_type8_q1_capture_shrink`.
- Focused smoke checkpoint added: `battle_p17_ah_type8_q4_escape_effect`.
- Both assert the source table scale/dx/dy and verify visible rendered pixels in the enemy effect region.

## Status

| Area | Status | Note |
| --- | --- | --- |
| q1 timing table | PORTED | Uses source q1 table and integer step division. |
| q4 timing table | PORTED | Uses source q4 table and reset-each-tick behavior. |
| `l.b(...,1,50)` brighten | PORTED | Rebuild brightens opaque pixels by `+50`. |
| `drawRGB` alpha/transparency | PORTED/PARTIAL | Rebuild uses ARGB image transparency; not a MIDP `drawRGB` pixel compare. |
| actor-origin draw placement | PORTED/PARTIAL | Now source-shaped from battle actor origin plus scaled origin/dx/dy; absolute actor anchor remains rebuild battle-slot mapping, not original-vs-rebuild pixel compared. |
| pixel-perfect claim | PENDING | No original MIDP frame capture/pixel compare in this slice. |

## Next

Run build/check/mojibake and the two new smoke PNG checkpoints. If they pass, the next battle-engine slice should move to the next documented visual debt, not widen this P17 slice further unless original-vs-rebuild captures are available.
