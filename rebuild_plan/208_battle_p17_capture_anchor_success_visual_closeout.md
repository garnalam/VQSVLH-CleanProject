# 208 - Battle P17 Capture Anchor / Success Visual Closeout

Date: 2026-07-10

Scope: fix current-route P17 capture visual mismatch reported in rebuild:

- thrown ball must target the enemy pet area, not drift off-screen/outside the battle target;
- on catch success, the enemy pet must stay hidden inside the ball;
- success ball must rest around the center of the enemy ground marker/platform;
- failure path must still restore the enemy pet.

## Source facts

Source: `modules/source_code/decoded/decompiled_source_cfr/game/d.java`

- P17 enter creates catch sprite `aj` with `aj.a(269, false)`, positions it on the current target with `aj.b(h.i, h.j)`, then starts q0.
- q1 hides the target and creates `H` type 8 shrink effect.
- q3 success opens the success openbox and stores the captured pet.
- q4 failure explicitly restores target visibility with `d[0].b(true)`.

Important implication: success q3 does **not** restore the enemy actor before the success openbox; restore is a q4 failure behavior.

## Rebuild changes

Files:

- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleRenderer.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Changes:

- P17 success now keeps `battleEnemyHiddenByCatch=true` while the openbox is shown.
- P17 success keeps catch sprite 269 visible in phase q3 instead of clearing all catch visuals before the openbox.
- P17 failure still clears catch visuals and restores the enemy.
- Catch ball render anchor:
  - q0/q1/q2/q4 target the visible enemy sprite bounds;
  - q3 success is centered on the enemy ground marker/platform using the current sprite 269 cell, not the whole q3 animation bounds.
- q3 success ball core centering was tightened after PNG review:
  - old whole-animation/current-cell placement could leave the visible red/white ball core near the platform rim;
  - new placement applies the measured sprite 269 q3 core bias so the visible ball core aligns with the enemy platform center.
- Type 8 shrink/escape effect now uses the source actor anchor currently used by the enemy sprite renderer, not the older fixed `ENEMY_RECT` fallback.
- Added focused Bunny success visual smoke checkpoint:
  - `battle_bunny_catch_success_openbox_visual`

## Status

- P17 success enemy hidden state: PORTED.
- P17 q3 ball-on-enemy-platform placement: PORTED/PARTIAL, current PNG smoke center error under 1px for Bunny success checkpoint.
- P17 q1/q4 type 8 bitmap transform: PORTED/PARTIAL.
- Exact Java ME/MIDP pixel parity: PENDING until original-vs-rebuild frame compare.

## Verified PNGs

Output directory:

- `rebuild_game/build/smoke/p17_capture_anchor_fix/`

Focused:

- `battle_catch_success_q3_flash_mid`
- `battle_openbox_source_widget_catch_success`
- `battle_bunny_catch_success_openbox_visual`
- `battle_p17_q4_fail_restore_enemy`
- `battle_bunny_first_catch_fail_escape_effect`

## Next recommended step

If more battle visual debt is needed, compare original-vs-rebuild P17 q0/q1/q3/q4 frames for sprite 269 and type 8 effect. Otherwise move back to the broader panel/battle roadmap.
