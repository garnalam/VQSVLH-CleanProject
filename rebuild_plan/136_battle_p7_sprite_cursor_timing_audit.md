# 136 Battle P7 Sprite Cursor Timing Audit

## Scope

Audit source `d.d()` / `d.e()` sprite cursor timing before changing P7 state `1` / `2` visual frame behavior.

This is source-first and limited to P7 actor sprite cursor parity. It does not claim pixel-perfect P7.

## Source Files Checked

- `modules/source_code/decoded/decompiled_source_cfr/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/f.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`

## Source Animator Facts

Default-package `d` is the source sprite animator used by default-package `f`, the superclass of `game.b`.

| Source path | Meaning | Status |
| --- | --- | --- |
| `d.a(byte state, byte nextState, boolean reset)` | Selects animation state. With `reset=true`, cursor `q` resets to frame `0`. | PORTED/PARTIAL |
| `d.c(int frame)` | Loads frame delay from sprite anim data. If delay is positive, it immediately stores `delay - 1`. | PORTED |
| `d.d()` | Advances the cursor only after delay reaches zero. On animation end, `p >= 0` switches to next state, `p == -2` holds last, `p == -1` loops. | PORTED/PARTIAL |
| `d.e()` | Returns `q >= r - 1`, meaning current cursor is on the last frame. | PORTED |
| `f.a()` | Calls `d.d()` only when the actor wrapper is active/visible. | PORTED/PARTIAL |
| `f.b()` | Returns `d.e()`. In P7, `game.b.b()` therefore means "sprite is on last frame". | PORTED |

The source per-frame display duration is equivalent to summing `max(1, frameDelay)` and using elapsed ticks since state reset. Frame `0` must be visible immediately after `game.b.d(state)` because `d.a(..., reset=true)` calls `c(0)`.

## P7 Source State Calls

`game.b.d(byte)` maps battle actor states:

| State | Source call | Meaning | Status |
| --- | --- | --- | --- |
| `0` | `this.a.a(0, -1, true)` | idle/base loop | PORTED/PARTIAL |
| `1` | `this.a.a(1, 0, true)` plus optional species `L` speffect | attack/action one-shot then visual state `0` | PORTED/PARTIAL |
| `2` | `this.a.a(2, 0, true)` | hit/recoil one-shot then visual state `0` | PORTED/PARTIAL |
| `3` | death effect path | death | PARTIAL |
| `4` | `this.a.a(4, -1, true)` | alternate/base loop | PARTIAL |

In `game.d` P7 case `7`, source checks `u.a.e()` / `u.a(frame)` before ticking the actor effect forward. Target hit state is also source-driven by `game.b.d((byte)2)`, and completion checks use `game.b.b()` / `d.e()`.

## Rebuild Finding

`SpriteAnim.tick()` and `SpriteAnim.tickHoldLast()` already use the same important delay shape as source:

- cursor starts at `0`;
- delay is initialized to `frameDelay - 1`;
- cursor advances only after delay reaches zero.

The gap was not the low-level `SpriteAnim` delay. The gap was P7 base pet rendering:

- `VqsvBattleRuntime.setP7BaseState()` stored only state number;
- `VqsvBattleRenderer.baseCursor()` fell back to `idleCursor(..., s.battleAnimationTick)`;
- therefore a state change to `1` or `2` could render at an arbitrary frame based on global battle UI tick, not frame `0` from the source `game.b.d(state)` reset.

## Code Decision

Smallest proven patch:

- make `setP7BaseState()` scene-aware;
- record per-side P7 base state start tick from `Scene.battleAnimationTick`;
- compute forced base cursors from elapsed ticks since that exact state reset;
- feed `battleP7BaseCursorPlayerSide` / `battleP7BaseCursorEnemySide` into the existing renderer.

This ports the timing reset semantics for P7 base state `1` / `2` without adding synthetic movement or changing damage/RNG logic.

Smoke note: state `1` is reset to frame `0` when the source-equivalent `game.b.d(1)` call is made, but the first captured P7 phase-entry frame in the current smoke harness occurs one runtime tick later. For the Elder route that means the local cursor checkpoint observes cursor `1`, not cursor `0`. The important parity point is that this cursor is now derived from the state reset tick, not from global battle UI uptime.

## Status

| Area | Status | Note |
| --- | --- | --- |
| `d.d()` delay shape | PORTED | Rebuild cursor formula matches `frameDelay - 1` source semantics. |
| `d.e()` last-frame meaning | PORTED | Used by actor-effect completion logic. |
| P7 actor-effect cursor | PORTED/PARTIAL | Uses `SpriteAnim.tickHoldLast()`, still not pixel-compared with original. |
| P7 base state `1/2` cursor reset | PORTED/PARTIAL | Patched to start from frame `0` at `game.b.d(1/2)` equivalent. |
| Full visual parity | PENDING | Needs original-vs-rebuild frame compare for exact sprite/cell output. |

## Next

After this patch and smoke pass, the next visual debt is original-vs-rebuild P7 frame comparison for state `1` attack and state `2` hit/recoil on the exact species used by Elder/Bunny routes.
