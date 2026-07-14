# Battle Buff7 Linh Xao Closeout

Scope: dedicated closeout for buff7 `Linh Xao`.

## Source Facts

- Buff row: `aq.c[6][7] = [340,355,2,-1,-1]`.
- Producer skills: `42`, `48`.
- Skill42 row: `[4,159,571,90,0,45,1,7,5,0]`.
- Skill48 row: `[4,165,577,130,3,15,1,7,5,0]`.
- Source `game.b.a case 7` stores `K[7] = sourceSkill`, reads producer
  `skill[8]`, and stores `v[7][1] = baseSpeed * skill[8] / 100`.
- Both known producers use `skill[8] = 5`, so the stat effect is
  `+5% baseSpeed`.
- Duration is `2`.
- HUD icon cell is `19`; duration cells are `136 -> 135 -> clear`.
- P12/P13 body visual is skipped because `game.d.ai[0]` includes only buff ids
  `3,5,13`.

## Producer Visual

Decoded `effect.mid`:

- skill42: `[0,0,24,0,-1,-1,0, 1,1,1,0,-1,-1,0]`
- skill48: `[0,0,24,0,-1,-1,0, 1,1,9,0,-1,-1,0]`

Therefore both are raw-damage/self-buff skills with source effect id `24`, but
their second chunks differ:

- skill42 uses speffect `1`.
- skill48 uses speffect `9`.

## Smoke Coverage

Passing focused checkpoints:

- `battle_status_buff7_before_no_effect`
- `battle_status_buff7_producer_visual_speed_skill42`
- `battle_status_buff7_skill42_start`
- `battle_status_buff7_skill42_actor24`
- `battle_status_buff7_skill42_speffect1`
- `battle_status_buff7_skill42_after`
- `battle_status_buff7_producer_visual_speed_skill48`
- `battle_status_buff7_skill48_speffect9`
- `battle_status_buff7_skill48_after`
- `battle_status_buff7_p12_no_body_visual`
- `battle_status_buff7_expiry_clears_speed`

Deterministic smoke setup uses base/current speed `100` to avoid low-stat
integer truncation. Verified stat sequence:

- before: `100`
- producer apply: `100 -> 105`
- first tick: stays `105`, duration `2 -> 1`
- expiry: restores `105 -> 100`, icon clears

PNG outputs:

- `rebuild_game/build_intro_demo/battle_status_buff7_before_no_effect.png`
- `rebuild_game/build_intro_demo/battle_status_buff7_producer_visual_speed_skill42.png`
- `rebuild_game/build_intro_demo/battle_status_buff7_producer_visual_speed_skill48.png`
- `rebuild_game/build_intro_demo/battle_status_buff7_p12_no_body_visual.png`
- `rebuild_game/build_intro_demo/battle_status_buff7_expiry_clears_speed.png`

Latest focused timeline outputs:

- `rebuild_game/build_intro_demo/buff7_timeline/battle_status_buff7_before_no_effect.png`
- `rebuild_game/build_intro_demo/buff7_timeline/battle_status_buff7_skill42_start.png`
- `rebuild_game/build_intro_demo/buff7_timeline/battle_status_buff7_skill42_actor24.png`
- `rebuild_game/build_intro_demo/buff7_timeline/battle_status_buff7_skill42_speffect1.png`
- `rebuild_game/build_intro_demo/buff7_timeline/battle_status_buff7_skill42_after.png`
- `rebuild_game/build_intro_demo/buff7_timeline/battle_status_buff7_skill48_speffect9.png`
- `rebuild_game/build_intro_demo/buff7_timeline/battle_status_buff7_skill48_after.png`
- `rebuild_game/build_intro_demo/buff7_timeline/battle_status_buff7_p12_no_body_visual.png`
- `rebuild_game/build_intro_demo/buff7_timeline/battle_status_buff7_expiry_clears_speed.png`

Timeline notes:

- Start: P7 entered, buff7 not active yet.
- Skill42 chunk0: actor action from `sourceEffectId=24`; visible hit spark on the enemy side.
- Skill42 chunk1: source `speffect=1`, AH special overlay.
- Skill48 chunk1: source `speffect=9`, AH special overlay; same buff formula, different visual color/row.
- After: damage lands, `game.d.q postEffect` applies buff7 to the player-side pet, speed `100 -> 105`, icon `19/136`.
- P12/P13 tick: no body visual because `game.d.ai[0]` excludes id `7`; duration `2 -> 1`.
- Expiry: duration `1 -> 0`, icon clears, speed restores to `100`.

## Classification

| Area | Status |
| --- | --- |
| Source rows | PROVED |
| Producer routes | PROVED |
| Producer visual rows | PROVED |
| Speed formula | PORTED |
| P12/P13 body visual absence | PROVED |
| Dedicated smoke closeout | PASS |
| Original-vs-rebuild pixel parity | PENDING |

## Next

Buff8 `Dien ap` already has dedicated producer/PP-damage/expiry coverage. If it
remains green in regression, the next new closeout target is buff9 `Hoa Thach`.
