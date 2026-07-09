# 138 Battle P7 Damage Text And HP Tween Audit

## Scope

Audit and tighten P7 damage text timing/placement and HP HUD tween behavior.

This slice covers:

- `game.d` P7 damage section;
- source floating text queue using `blood.mid`;
- `game.h.a(b, boolean)` player-side HP HUD tween;
- `game.h.b(b, boolean)` enemy-side HP HUD tween.

No original client frame capture was used, so this remains source-shaped and smoke-verified, not full pixel-perfect runtime parity.

## Source Facts

In `game.d` case `7`, after actor/effect completion:

1. Damage is applied to the target.
2. Floating text is queued via `a(String, byte, int, side, x, y, colorA, colorB)`.
3. `S.k = 0`.
4. `S.a(target)` or `S.b(target)` prepares the HUD effect icons.
5. Each tick waits for both:
   - `V()` floating text queue to complete;
   - `S.a(target,false)` or `S.b(target,false)` HP tween to complete.

`V()` increments each floating text frame cursor and removes that text once the source row from `blood.mid` has ended.

`game.h.a/b` HP tween:

| Source behavior | Status |
| --- | --- |
| Display HP is `v1.N()`, real HP is `v1.d[1]`. | PORTED/PARTIAL |
| Step is computed once while `F == 0`: `abs(display-real)/11`, minimum `1`. | PORTED |
| First three calls increment `G` and keep old percent widgets visible. | PORTED |
| Then `F += step` every call and display HP moves toward real HP. | PORTED |
| Completion resets `F`, `G`, and `k`. | PORTED/PARTIAL |

## Rebuild Gap Found

Before this slice, rebuild pushed `enemy.hp` / `player.hp` directly into HUD fields during P7 phase 2. That made the HP bar/text jump immediately when damage was applied.

`drawP7Damage()` also clamped to the final `blood.mid` frame for the whole fixed phase-2 duration.

## Code Decision

Smallest source-backed patch:

- add runtime display HP fields separate from real HP;
- on P7 damage, keep display HP at the old value and start a source-shaped tween;
- delay HP movement for three ticks;
- move HP by source `F += step` behavior;
- keep P7 phase 2 until both `blood.mid` text and HP tween are complete;
- stop drawing damage/debuff text after their `blood.mid` rows end instead of clamping forever.

## Smoke Checkpoints

| Checkpoint | Purpose |
| --- | --- |
| `battle_elder_p7_damage_hp_delay` | P7 phase 2 entered, enemy HUD HP still shows pre-damage HP through the initial delay. |
| `battle_elder_p7_damage_hp_tween_step` | After the source delay, enemy HUD HP begins decreasing but P7 damage text remains visible. |
| `battle_elder_p7_damage_text_lifecycle` | P7 phase 2/3 can progress after damage text and HP tween complete. |

## Status

| Area | Status | Note |
| --- | --- | --- |
| HP tween delay/step | PORTED/PARTIAL | Source-shaped `G/F` logic ported for P7 damage. |
| Damage text lifetime | PORTED/PARTIAL | Uses `blood.mid` row length instead of fixed clamp. |
| Damage text placement | PORTED/PARTIAL | Existing source `game.d.c(Graphics)` side formula retained. |
| Full MIDP pixel parity | PENDING | Needs original client frame captures. |

## Next

After smoke pass, next P7 visual debt is multi-frame original-client comparison for the combined text/HUD tween frames, or move to the next roadmap visual area if original captures are unavailable.
