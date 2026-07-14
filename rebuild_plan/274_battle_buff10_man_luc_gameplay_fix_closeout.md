# 274 - Battle Buff10 Man Luc Gameplay Fix Closeout

Scope: buff10 `Man Luc` after user-approved gameplay correction.

Status: `INTENTIONAL_DEVIATION / GAMEPLAY_FIXED`.

## Source Facts Kept

Historical source audit remains in `273_battle_buff10_man_luc_source_oddity_closeout.md`.

- Source buff row: `aq.c[6][10] = [343,358,2,-1,-1]`.
- Producer skills: `62`, `68`.
- Producer visual remains source-backed:
  - actor action `26`;
  - `speffect 0`;
  - `speffect 15`.
- Source P12/P13 active body visual is not expected for buff10 because the source active-visual gate excludes id `10`.
- Source oddity: original data/code would make `attack 100 -> 99`, despite the text meaning attack-up.

## Gameplay Fix

The user explicitly approved replacing the source oddity with an attack-up decay:

| Moment | Remaining turns | Attack bonus | Example attack |
| --- | ---: | ---: | ---: |
| Apply / turn 1 | 3 | `+15%` | `100 -> 115` |
| Active tick 1 / turn 2 | 2 | `+10%` | `100 -> 110` |
| Active tick 2 / turn 3 | 1 | `+5%` | `100 -> 105` |
| Active tick 3 | 0 | cleared | `100` |

Damage smoke with attack `120` versus defense `40`:

- baseline: `80`;
- buff10 turn 1: `98`.

## Runtime Changes

`rebuild_game/src/main/java/VqsvBattleUnit.java`

- `applySourceBuff(10, ...)` now overrides duration to `3`.
- Stored value `buffSlots[10][1]` is the current attack delta, not the source `-1` value.
- `tickSourceBuff(10, ...)` recalculates the next delta before duration decrement, so render/debug state after the tick shows the correct next turn:
  - `15 / duration 3`;
  - `10 / duration 2`;
  - `5 / duration 1`;
  - clear.

`rebuild_game/src/main/java/VqsvSmokeHarness.java`

- Replaced the main suite checkpoint with `battle_status_buff10_gameplay_decay_attack_up`.
- Kept legacy `battle_status_buff10_attack_up_damage`, but it now asserts the gameplay-fixed damage `80 -> 98`.
- Kept old `battle_status_buff10_source_oddity_attack_down` as a compatibility alias inside the closeout runner, but it no longer asserts oddity behavior.

## Smoke PNGs

Focused output:

- `rebuild_game/build_intro_demo/buff10_gameplay_fix/battle_status_buff10_before_no_effect.png`
- `rebuild_game/build_intro_demo/buff10_gameplay_fix/battle_status_buff10_skill62_start.png`
- `rebuild_game/build_intro_demo/buff10_gameplay_fix/battle_status_buff10_skill62_actor26.png`
- `rebuild_game/build_intro_demo/buff10_gameplay_fix/battle_status_buff10_skill62_speffect0.png`
- `rebuild_game/build_intro_demo/buff10_gameplay_fix/battle_status_buff10_skill62_speffect15.png`
- `rebuild_game/build_intro_demo/buff10_gameplay_fix/battle_status_buff10_skill62_after_apply.png`
- `rebuild_game/build_intro_demo/buff10_gameplay_fix/battle_status_buff10_gameplay_decay_attack_up.png`
- `rebuild_game/build_intro_demo/buff10_gameplay_fix/battle_status_buff10_p12_no_body_visual.png`
- `rebuild_game/build_intro_demo/buff10_gameplay_fix/battle_status_buff10_expiry_clears_attack.png`
- `rebuild_game/build_intro_demo/buff10_gameplay_fix/battle_status_buff10_attack_up_damage.png`

Focused checkpoints passed before final regression:

- before/no effect: no icon, attack `100`;
- producer visual: actor26 -> speffect0 -> speffect15;
- after apply: icon `22`, duration cell `137`, attack `115`;
- gameplay damage: baseline `80`, buffed `98`;
- P12/P13 active queue: no body visual, duration `3 -> 2`, attack `110`;
- expiry: attack `115 -> 110 -> 105 -> 100`, icon clears.

## Classification

Do not relabel this as `PORTED`.

Correct classification:

`INTENTIONAL_DEVIATION / GAMEPLAY_FIXED`

Reason:

- source-backed visual remains preserved;
- source oddity is documented;
- numeric gameplay differs from source by explicit user approval.

## Next Recommended Step

Continue table order with buff11 `Thau Thu`: audit selected donor vector copy/clear before coding.
