# 337 - FireSkill Live Animation Re-audit Follow-up

## Status

`REOPENED / PARTIAL FIX APPLIED`

The user provided live screenshots showing Fire battle visuals that are clearly not
represented well by the old Fire smoke contact sheet. The old statement that Fire
skills can be treated as one visual family is not acceptable for visual parity work.

## Source Matrix

| Skill | Name | Skill row | Effect row | Source visual path | Rebuild status |
|---:|---|---|---|---|---|
| 0 | Hỏa trảo | `[0,117,529,100,0,45,0,-1,-1,0]` | `[0,0,20,0,-1,-1,0]` | target `u20`, sprite 262, state 0 | logic PORTED, visual PARTIAL |
| 1 | Dương viêm | `[0,118,530,50,0,45,2,0,4,0]` | `[0,0,20,0,-1,-1,0]` | target `u20`; debuff0 after hit | logic PORTED, visual PARTIAL |
| 2 | Diễm kích | `[0,119,531,100,0,45,2,1,10,0]` | `[0,0,20,0,-1,-1,0, 0,1,14,0,0,0,-1]` | target `u20` then `speffect14/AH12` | logic PORTED, visual PARTIAL |
| 3 | Hỏa vân triệu | `[0,120,532,100,1,30,0,-1,120,0]` | `[0,0,20,0,-1,-1,0]` | target `u20`; conditional damage | logic PORTED, visual PARTIAL |
| 4 | Thiên hỏa tế | `[0,121,533,0,1,10,1,0,-1,1]` | `[0,1,16,0,-1,-1,0, 0,1,15,0,-1,-1,0]` | self `speffect16/AH9` then `speffect15/AH1`, no damage | logic PORTED, visual PARTIAL |
| 5 | Viêm lôi phá | `[0,122,534,0,1,10,1,1,-1,1]` | `[0,1,16,0,-1,-1,0, 0,1,15,0,-1,-1,0]` | same source producer visuals as skill4, different buff | logic PORTED, visual PARTIAL |
| 6 | Hỏa diễm đao | `[0,123,535,150,2,30,0,-1,-1,0]` | `[0,0,20,0,-1,-1,0]` | target `u20`; stronger damage | logic PORTED, visual PARTIAL |
| 7 | Chước nhiệt chi xúc | `[0,124,536,75,2,30,2,0,3,0]` | `[0,0,20,0,-1,-1,0]` | target `u20`; debuff0 after hit | logic PORTED, visual PARTIAL |
| 8 | Liệt diễm phong bạo | `[0,125,537,200,3,15,2,1,20,0]` | `[0,0,20,0,-1,-1,0, 0,1,14,0,0,-1,1]` | target `u20` then `speffect14/AH12`, with state trigger | logic PORTED, visual PARTIAL |
| 9 | Vĩnh hằng hỏa ảnh | `[0,126,538,200,3,15,0,-1,250,0]` | `[0,0,20,0,-1,-1,0]` | target `u20`; conditional damage | logic PORTED, visual PARTIAL |

## What Was Wrong In The Rebuild Smoke

| Problem | Fix / current status |
|---|---|
| Fire source-stage sheet captured only one `u/H` frame, making multiple skills look identical. | Still needs a fuller per-skill frame strip; current sheet is not enough for final visual parity. |
| Skill 4/5 have two special chunks, but the sheet stopped at `speffect16/AH9` and missed `speffect15/AH1`. | `FireSkill` smoke now advances to `speffect15/AH1` for skills 4/5 and writes `2a_self_H16_type9` sidecar PNG. |
| AH type9 renderer drew only the tinted copy, not the base plus tinted overlay that `ah.java` draws. | `VqsvBattleRenderer` now draws the base actor copy first and then blinks the tinted copy on top. |
| Live screenshots show Fire visuals still richer/different than the source-stage sheet. | Visual parity remains REOPENED; next step is AH type1/texture and exact live skill-id mapping. |

## Source Facts For The Fix

`ah.java` type9 draw logic:

```text
draw b[0] base copy;
if t[0] / t[2] % 2 == 0 draw b[1] transformed/tinted copy;
```

Rebuild previously only drew the transformed overlay, so self-special Fire skills
could become an unreadable color silhouette.

`effect.mid[4]` and `effect.mid[5]` both contain:

```text
[0,1,16,0,-1,-1,0, 0,1,15,0,-1,-1,0]
```

So source requires two H chunks, not one.

## Smoke Outputs

```text
rebuild_game/build/smoke/fire_source_stage_animation/battle_fire_source_stage_animation.png
rebuild_game/build/smoke/fire_source_stage_animation/battle_fire_source_stage_animation_zoom.png
rebuild_game/build/smoke/fire_source_stage_animation/fire_source_stage_frames/
```

## Next Work

Do not move to new skills until this visual layer is tighter:

1. Add a richer Fire frame-strip smoke for all 10 skills:
   - actor `u` start/mid/late;
   - H chunk 1 start/mid/late;
   - H chunk 2 start/mid/late where present;
   - hit/recover and settled idle.
2. Audit `AH type1` against `ah.java` and `l.a/l.b` because live screenshots show beam/texture-like Fire visuals not represented well enough yet.
3. Identify the exact skill id/name from each live screenshot before claiming a mismatch for a specific source row.
4. Only after that, patch shared `VqsvBattleRenderer`/`VqsvBattleRuntime`; do not hardcode Fire per-skill visuals.
