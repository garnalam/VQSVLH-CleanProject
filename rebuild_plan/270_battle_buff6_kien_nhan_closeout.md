# Battle Buff6 Kien Nhan Closeout

Scope: dedicated closeout for buff6 `Kien nhan` after source audit
`269_battle_buff6_kien_nhan_source_oddity_audit.md` and the user-approved
gameplay override.

## Source Facts

- Buff row: `aq.c[6][6] = [339,354,3,50,-1]`.
- Producer skill: `35`.
- Producer visual row:
  `effect.mid[35] = [0,1,4,0,-1,-1,0, 0,1,17,0,-1,-1,0]`.
- Chunk0 uses speffect `4`, AH type `7`.
- Chunk1 uses speffect `17`, AH type `1`.
- Apply values are source-shaped: duration `3`, value/chance `50`,
  secondary `-1`.
- HUD icon cell is `18`; duration cells are `137 -> 136 -> 135 -> clear`.
- P12/P13 body visual is not rendered for buff6 because source gate
  `game.d.ai[0]` excludes id `6`.

## Source Oddity And Final Decision

Source damage hook checks whether the defender/target has buff6, but reads the
attacker-side `v[6]` params. That source oddity is documented in audit 269, but
the active rebuild intentionally does not keep it.

Final user-approved gameplay rule:

- target has buff6;
- roll `<= 50`;
- incoming damage is reduced by `50%`;
- attacker-side buff6 params are not required.

Deterministic probe results after the gameplay fix:

| Checkpoint | Setup | Result |
| --- | --- | --- |
| `battle_status_buff6_damage_reduction_success` | target has buff6, forced roll `0` | baseline `80`, result `41` after half-damage plus normal jitter |
| `battle_status_buff6_damage_reduction_fail` | target has buff6, forced roll `99` | baseline `80`, result `80` |

## Smoke Coverage

Passing focused checkpoints:

- `battle_status_buff6_before_no_effect`
- `battle_status_buff6_producer_visual`
- `battle_status_buff6_visual_chunk0_type7`
- `battle_status_buff6_visual_chunk1_type1`
- `battle_status_buff6_damage_reduction_success`
- `battle_status_buff6_damage_reduction_fail`
- `battle_status_buff6_p12_no_body_visual`
- `battle_status_buff6_expiry_clears_icon`

PNG outputs:

- `rebuild_game/build_intro_demo/battle_status_buff6_before_no_effect.png`
- `rebuild_game/build_intro_demo/battle_status_buff6_producer_visual.png`
- `rebuild_game/build_intro_demo/battle_status_buff6_visual_chunk0_type7.png`
- `rebuild_game/build_intro_demo/battle_status_buff6_visual_chunk1_type1.png`
- `rebuild_game/build_intro_demo/battle_status_buff6_damage_reduction_success.png`
- `rebuild_game/build_intro_demo/battle_status_buff6_damage_reduction_fail.png`
- `rebuild_game/build_intro_demo/battle_status_buff6_p12_no_body_visual.png`
- `rebuild_game/build_intro_demo/battle_status_buff6_expiry_clears_icon.png`

Latest focused timeline outputs:

- `rebuild_game/build_intro_demo/buff6_timeline/battle_status_buff6_before_no_effect.png`
- `rebuild_game/build_intro_demo/buff6_timeline/battle_status_buff6_producer_visual.png`
- `rebuild_game/build_intro_demo/buff6_timeline/battle_status_buff6_visual_chunk0_type7.png`
- `rebuild_game/build_intro_demo/buff6_timeline/battle_status_buff6_visual_chunk1_type1.png`
- `rebuild_game/build_intro_demo/buff6_timeline/battle_status_buff6_damage_reduction_success.png`
- `rebuild_game/build_intro_demo/buff6_timeline/battle_status_buff6_damage_reduction_fail.png`
- `rebuild_game/build_intro_demo/buff6_timeline/battle_status_buff6_p12_no_body_visual.png`
- `rebuild_game/build_intro_demo/buff6_timeline/battle_status_buff6_expiry_clears_icon.png`

Timeline notes:

- Start/before: no buff6 icon, no stat/damage hook active.
- Producer: text `Kien nhan`, icon `18/137`, value `50`, secondary `-1`.
- Chunk0: source `speffect=4`, AH type `7`, player side. Current rebuild frame is source-backed but visually subtle without original pixel compare.
- Chunk1: source `speffect=17`, AH type `1`, player side; this is the visually obvious striped overlay frame.
- Damage hook: INTENTIONAL_DEVIATION / GAMEPLAY_FIXED, target-side buff6 roll `<= 50` halves incoming damage.
- Expiry: duration `3 -> 2 -> 1 -> 0`, icon clears.

## Classification

| Area | Status |
| --- | --- |
| Source rows | PROVED |
| Producer route | PROVED |
| Producer visual rows | PROVED |
| Buff apply values | PORTED |
| Source oddity damage hook | PROVED / SUPERSEDED |
| Gameplay damage hook | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED |
| P12/P13 body visual absence | PROVED |
| Dedicated smoke closeout | PASS |
| Original-vs-rebuild pixel parity | PENDING |

## Next

Follow table order with buff7 `Linh Xao` dedicated closeout, source-first.
