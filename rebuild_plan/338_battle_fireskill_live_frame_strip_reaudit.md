# 338 - Battle FireSkill Live Frame Strip Re-audit

Status: VISUAL PARITY REOPENED / SHARED P7 RENDERER PARTIAL FIXED.

Reason: live gameplay screenshots show Fire visuals are richer than the current
rebuild contact sheets. The previous Fire smoke proved source rows and gameplay
numbers, but it was too weak for visual parity because it captured only one
representative P7 frame per skill.

## User Live Notes

The screenshots provided in this chat show:

- a target-side fire/body effect that is visibly attached around the enemy pet;
- a vertical red/white beam-like effect over the enemy pet;
- battle state visuals that cannot be validated by a single `u20_start` frame.

Therefore Fire animation status must remain `PARTIAL`, even where logic is
already ported.

## Source Facts Reconfirmed

`game.d.n()` reads `effect.mid[skill]` in 7-value chunks:

- `chunk[1] == 0`: actor-attached `u = new ah([effectId, state, dir])`.
- `chunk[1] == 1`: special `H = new ah(speffect.mid[row])`.
- `chunk[4]`, `chunk[5]`, `chunk[6]` are frame/state trigger hooks used by P7.

Current Fire `effect.mid` rows still prove the first producer path:

| Skill | Source effect path | Current visual risk |
|---:|---|---|
| 0 | `u20/state0` | Rebuild shows the same base actor effect as 1/3/6/7/9. Needs deeper source/live mapping before calling wrong or done. |
| 1 | `u20/state0` + debuff0 logic | Same `u20`; visible difference should come from debuff/status layer after hit. |
| 2 | `u20/state0 -> speffect14/AH12` | `AH12` timing and cell-origin render are source-shaped, but the visual is still subtle and remains PARTIAL. |
| 3 | `u20/state0` + conditional damage | Same `u20`; logic differs. |
| 4 | `speffect16/AH9 -> speffect15/AH1` | Visible on player/self crop; AH1 now uses cropped local texture coordinates, still not pixel-parity. |
| 5 | `speffect16/AH9 -> speffect15/AH1` | Same producer visuals as skill4 in source, buff differs. |
| 6 | `u20/state0` + higher power | Same `u20`; logic differs. |
| 7 | `u20/state0` + debuff0 logic | Same `u20`; visible difference should come from debuff/status layer after hit. |
| 8 | `u20/state0 -> speffect14/AH12` | `AH12` timing and cell-origin render are source-shaped, but the visual is still subtle; source chunk trigger differs from skill2. |
| 9 | `u20/state0` + conditional damage | Same `u20`; logic differs. |

Important interpretation: identical `effect.mid` rows prove only the first P7
producer chunk, not the whole live battle presentation. The rebuild must not
claim "all Fire animations are the same".

## New Smoke Suite

Added suite:

```text
battle_fire_live_frame_strip
```

Implementation:

```text
rebuild_game/src/main/java/FireSkill.java
```

Output:

```text
rebuild_game/build/smoke/fire_live_frame_strip_clean/battle_fire_live_frame_strip.png
rebuild_game/build/smoke/fire_live_frame_strip_clean/battle_fire_live_frame_strip_debug.txt
rebuild_game/build/smoke/fire_live_frame_strip_clean/fire_live_frame_strip_frames/
```

Per-skill examples:

```text
rebuild_game/build/smoke/fire_live_frame_strip_clean/fire_live_frame_strip_frames/skill4/battle_fire_live_frame_strip_skill4.png
rebuild_game/build/smoke/fire_live_frame_strip_clean/fire_live_frame_strip_frames/skill8/battle_fire_live_frame_strip_skill8.png
```

The master PNG now shows the source effect-side body crop over time:

- skills `4/5` crop player body because they are self-buff skills;
- the other Fire skills crop enemy body.

Per-skill PNGs still show:

- full frame;
- player body crop;
- enemy body crop.

## Findings From The New Smoke

| Finding | Status |
|---|---|
| Skills 0/1/3/6/7/9 visibly share `u20/state0` in the current rebuild. | SOURCE ROW BACKED for first chunk, but visual parity still PARTIAL. |
| Skill 2/8 reach `speffect14/AH12`, but the rendered effect is weak/subtle. | PORTED/PARTIAL; duration uses `frameCount * (interval + 1)` and renderer uses cropped actor cell origin, but color/alpha parity still needs deeper audit. |
| Skill 4/5 reach `speffect16/AH9` then `speffect15/AH1`. | PORTED/PARTIAL; visible in player crop. AH1 now scrolls/blends texture on the cropped cell instead of screen-space, not pixel-perfect. |
| Earlier master contact sheet hid skill 4/5 self-buff because it cropped enemy body only. | FIXED in `battle_fire_live_frame_strip`: master crop side is now source-effect side. |
| Shared actor `u20` renderer was double-applying battle motion offsets. | FIXED; see `339_battle_p7_actor_u20_shared_renderer_audit.md`. |
| Live screenshots show effects not represented strongly enough by the rebuild frame strip. | GAP CONFIRMED. |

## Next Required Work

Do not continue new skill lanes until this is tighter.

1. Identify exact live skill id from the user's screenshot, because visible pet
   name/skill name can be confused in the HUD.
2. Continue shared P7/AH rendering audit, not per-skill hardcoded visuals:
   - `AH type12` color/alpha/drawRGB parity for `speffect14`;
   - `AH type1` texture source/Java ME blend parity for `speffect15`;
   - actor `u20` frame placement/anchor over target;
   - P7 `chunk[4]/[5]/[6]` timing and target hit state.
3. After renderer fixes, rerun:
   - `battle_fire_live_frame_strip`;
   - `battle_fire_source_stage_animation`;
   - `battle_quick`;
   - build/check/formula/mojibake scan.

## Verification

Passed:

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-suite battle_fire_live_frame_strip build\smoke\fire_live_frame_strip_clean
```

Latest partial-fix smoke:

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-suite battle_fire_live_frame_strip build\smoke\fire_live_frame_strip_after_shared_p7_contact_side
java -cp build\classes VqsvIntroDemo --smoke-suite battle_fire_source_stage_animation build\smoke\fire_source_stage_after_shared_p7_cell
```

Latest actor `u20` offset fix smoke:

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-suite battle_fire_live_frame_strip build\smoke\fire_live_frame_strip_after_u20_offset_fix
java -cp build\classes VqsvIntroDemo --smoke-suite battle_fire_source_stage_animation build\smoke\fire_source_stage_after_u20_offset_fix
```
