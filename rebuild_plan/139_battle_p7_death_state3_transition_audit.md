# 139 Battle P7 Death State3 And Post-Damage Transition Audit

## Scope

Audit and tighten P7 KO/death visual state `3` and the post-damage transition after HP tween/text completion.

This slice focuses on:

- source `game.d.U()` post-damage helper;
- source `game.b.d((byte)3)` death/down state;
- transition from P7 to P15/P8/P9 after KO.

## Source Facts

After P7 damage text and HP HUD tween complete, source calls `U()`:

```text
if target.d[1] <= 0:
    target.d((byte)3)
else:
    target.d((byte)0)
```

`game.b.d(3)`:

- only runs the death effect path when `game.d.a().b == 0`;
- hides/disposes the base actor wrapper via `this.d()`;
- creates `ah` effect row `[16, actorX, actorY, spriteId, 0, direction, 0, 0, 4]`;
- starts that effect with `L.a()`;
- records actor state `U = 3`.

Then P7 waits:

```text
if v3 && (target.S() || c(target, true)):
    B = true
    z = false
```

`target.S()` is alive check (`d[1] > 0`). For KO targets, source waits for `c(target,true)`, which waits for the death `L` effect to complete before the battle routes to replacement/win/lose.

## Rebuild Gap Found

Before this slice, rebuild set base state `3` immediately in `applyP7Damage()` when the target HP became zero. That was too early:

- source keeps target in hit state `2` while damage text and HP tween finish;
- source only enters death state `3` from `U()` after those complete;
- source hides the base actor and runs AH type `16` before final transition.

## Code Decision

Smallest source-backed patch:

- P7 damage always sets target base state `2` first;
- after damage text and HP tween complete, KO target enters source-equivalent state `3`;
- state `3` hides the base actor;
- a source-shaped AH type `16` death effect is drawn from the target sprite at the source actor anchor;
- P7 does not route to P15/P8/P9 until the death effect completes.
- after the death effect, the KO actor remains hidden for P8/P9, matching source `game.b.d()`/`super.d()` lifetime.
  Enemy replacement P15 resets the hidden flag when the next enemy becomes active.

The AH type `16` draw is still `PORTED/PARTIAL`: it uses source sprite/anchor and source row shape `[16,x,y,sprite,0,dir,0,0,4]`, but does not claim exact MIDP `l.a/drawRGB` pixel parity.

## Smoke Checkpoints

| Checkpoint | Purpose |
| --- | --- |
| `battle_elder_p7_death_state3_effect_start` | lethal P7 waits through damage/HUD, then starts state3 death effect and hides enemy base. |
| `battle_elder_p7_death_to_p8_after_effect` | single enemy death routes P8 only after death effect completes. |
| `battle_p12_queue_death_to_p15` | existing enemy reserve replacement remains valid. |
| `battle_p15_enemy_replaced` | existing P15 replacement swap remains valid. |

## Status

| Area | Status | Note |
| --- | --- | --- |
| Death state timing | PORTED/PARTIAL | State `3` now starts after text/HP tween, matching `U()`. |
| Base actor hide on death | PORTED/PARTIAL | Base actor hidden while death effect runs. |
| KO actor hidden after effect | PORTED/PARTIAL | Dead side stays hidden through P8/P9; P15 replacement resets for the next actor. |
| AH type16 death visual | PORTED/PARTIAL | Source-shaped sprite/anchor/duration; exact `drawRGB` parity pending. |
| Post-death transition wait | PORTED/PARTIAL | P7 waits death effect before P15/P8/P9. |
| Pixel-perfect death effect | PENDING | Requires original-client frame comparison. |

## Next

After this passes, the next P7 visual debt is original-client frame comparison for AH type16 death effect or, if original captures are unavailable, the next documented P7 queue/result transition gap.
