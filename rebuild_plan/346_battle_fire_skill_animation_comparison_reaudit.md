# 346 - Battle Fire Skill Animation Comparison Re-audit

Date: 2026-07-14

Status: SOURCE COMPARED / PNG SMOKE RUN / FIRE VISUAL STILL PARTIAL / NO CODE PATCH.

This audit responds to the user request to compare several Fire skills instead
of assuming that one element/class means one animation.

No live client was opened. Only source rows and headless PNG smoke were used.

## Why This Matters

The user is right that we must not say "all Fire skills are the same" just
because they are in the same element class. The original battle runtime does not
pick animation from the Java skill class. It reads:

```text
effect.mid[skillId]
```

and then `game.d.n()` dispatches each 7-byte chunk as either:

```text
actor u effect, e.g. u20/state0/sprite262
special H effect, e.g. speffect14/AH12
```

Therefore every skill must be checked from its own `effect.mid` row.

## Fire Source Rows

Source rows re-read from decoded original tables:

| Skill | Source `effect.mid` row | Source interpretation |
|---:|---|---|
| 0 | `[0,0,20,0,-1,-1,0]` | target actor `u20/state0/sprite262` only |
| 1 | `[0,0,20,0,-1,-1,0]` | same first actor chunk; later debuff0 logic/status differs |
| 2 | `[0,0,20,0,-1,-1,0, 0,1,14,0,0,0,-1]` | actor `u20`, then `speffect14/AH12` |
| 3 | `[0,0,20,0,-1,-1,0]` | same first actor chunk; conditional damage logic differs |
| 4 | `[0,1,16,0,-1,-1,0, 0,1,15,0,-1,-1,0]` | self-side `speffect16/AH9`, then `speffect15/AH1` |
| 5 | `[0,1,16,0,-1,-1,0, 0,1,15,0,-1,-1,0]` | same visual producer as skill4; buff logic differs |
| 6 | `[0,0,20,0,-1,-1,0]` | same first actor chunk; higher power |
| 7 | `[0,0,20,0,-1,-1,0]` | same first actor chunk; later debuff0 logic/status differs |
| 8 | `[0,0,20,0,-1,-1,0, 0,1,14,0,0,-1,1]` | actor `u20`, then `speffect14/AH12`; trigger differs from skill2 |
| 9 | `[0,0,20,0,-1,-1,0]` | same first actor chunk; conditional damage logic differs |

## Important Conclusion

Source proves two things at the same time:

1. Fire skills are not all one generic Java-class animation.
2. Some Fire skills intentionally share the first actor chunk.

Classification:

| Group | Skills | Status | Meaning |
|---|---|---|---|
| Actor-only first chunk | 0,1,3,6,7,9 | SOURCE-BACKED SAME FIRST CHUNK / VISUAL PARTIAL | These skills start with the same `u20/state0/sprite262` actor animation. Differences come from damage, conditional branches, debuff/status text, active queue, and later gameplay. |
| Actor + H special | 2,8 | SOURCE-BACKED DIFFERENT FULL PRESENTATION / PARTIAL | These start with `u20` but add `speffect14/AH12`. Rebuild reaches the H chunk, but the visual is weak/subtle. |
| Self buff special | 4,5 | SOURCE-BACKED DIFFERENT FULL PRESENTATION / PARTIAL | These do not use target `u20` as the main producer. They play self-side `speffect16/AH9 -> speffect15/AH1`. |

So the correct wording is:

```text
Several Fire skills share the first actor chunk in source, but their full battle
presentation is not necessarily the same.
```

The wrong wording is:

```text
All Fire skills are the same animation.
```

## PNG Smoke Run

Build:

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
```

Smoke:

```text
java -cp build\classes VqsvIntroDemo --smoke-suite battle_fire_animation_contact_sheet build\smoke\fire_compare_20260714\battle_fire_animation_contact_sheet.png
java -cp build\classes VqsvIntroDemo --smoke-suite battle_fire_live_frame_strip build\smoke\fire_compare_20260714_live\battle_fire_live_frame_strip.png
java -cp build\classes VqsvIntroDemo --smoke-suite battle_fire_source_stage_animation build\smoke\fire_compare_20260714_stage\battle_fire_source_stage_animation.png
```

Outputs:

```text
rebuild_game/build/smoke/fire_compare_20260714/battle_fire_animation_contact_sheet.png/battle_fire_animation_contact_sheet.png
rebuild_game/build/smoke/fire_compare_20260714/battle_fire_animation_contact_sheet.png/battle_fire_animation_contact_sheet_notes.md
rebuild_game/build/smoke/fire_compare_20260714_live/battle_fire_live_frame_strip.png/battle_fire_live_frame_strip.png
rebuild_game/build/smoke/fire_compare_20260714_live/battle_fire_live_frame_strip.png/fire_live_frame_strip_frames/
rebuild_game/build/smoke/fire_compare_20260714_stage/battle_fire_source_stage_animation.png/battle_fire_source_stage_animation.png
rebuild_game/build/smoke/fire_compare_20260714_stage/battle_fire_source_stage_animation.png/battle_fire_source_stage_animation_zoom.png
```

Representative frame strips inspected:

```text
skill0: build/smoke/fire_compare_20260714_live/battle_fire_live_frame_strip.png/fire_live_frame_strip_frames/skill0/battle_fire_live_frame_strip_skill0.png
skill2: build/smoke/fire_compare_20260714_live/battle_fire_live_frame_strip.png/fire_live_frame_strip_frames/skill2/battle_fire_live_frame_strip_skill2.png
skill4: build/smoke/fire_compare_20260714_live/battle_fire_live_frame_strip.png/fire_live_frame_strip_frames/skill4/battle_fire_live_frame_strip_skill4.png
skill8: build/smoke/fire_compare_20260714_live/battle_fire_live_frame_strip.png/fire_live_frame_strip_frames/skill8/battle_fire_live_frame_strip_skill8.png
```

## Smoke Observations

### Skill 0

Smoke frames:

```text
P3 before -> P1 base -> u20/st0/c0 -> u20/st0/c1 -> u20/st0/c2 -> u20/st0/c3 -> damage -> settled
```

This is the direct Fire baseline.

Status: PORTED/PARTIAL.

### Skill 2

Smoke frames:

```text
P3 before -> P1 base -> u20/st0/c0..c3 -> H12/sp14/t0..t8 -> damage/debuff -> settled
```

This is not identical to skill0 in source or runtime, because it has the extra
`speffect14/AH12` segment. However, the current rebuild visual for AH12 is still
too subtle, so players may still perceive it as too similar.

Status: PORTED/PARTIAL; visual strength/parity still debt.

### Skill 4

Smoke frames:

```text
P3 before -> self H9/sp16 frames -> P1 base -> self H1/sp15 frames -> settled
```

This is visibly different from target actor-only Fire skills. It is a self buff
producer and uses player-side/self-side visuals.

Status: PORTED/PARTIAL.

### Skill 8

Smoke frames:

```text
P3 before -> P1 base -> u20/st0/c0..c3 -> H12/sp14/t0..t8 -> stronger damage/debuff -> settled
```

It has the same broad source presentation family as skill2, but differs in skill
row power/chance and chunk trigger data:

```text
skill2 chunk1 trigger tail = [0,0,-1]
skill8 chunk1 trigger tail = [0,-1,1]
```

Status: PORTED/PARTIAL.

## Numeric Smoke Notes

Representative smoke values from the run:

| Skill | PP | HP result | Damage/effect note |
|---:|---|---|---|
| 0 | `45 -> 44` | enemy `109 -> 84` in timeline smoke | direct baseline, actor sprite 262 |
| 1 | `45 -> 44` | enemy `109 -> 90` | applies debuff0 with tick damage |
| 2 | `45 -> 44` | enemy `109 -> 84` | reaches `speffect14/AH12`, applies debuff1 in forced smoke |
| 4 | `10 -> 9` | no direct enemy damage | applies buff0, self-side H visuals |
| 5 | `10 -> 9` | no direct enemy damage | applies buff1, same producer visuals as skill4 |
| 6 | `30 -> 29` | enemy `109 -> 74` | same actor chunk, higher power |
| 8 | `15 -> 14` | enemy `109 -> 60` | reaches `speffect14/AH12`, higher power/chance |
| 9 | `15 -> 14` | enemy `109 -> 49/60 depending timeline branch` | same actor chunk, conditional damage logic |

## Current Gap

The source route is represented, but visual parity is not done.

Known visual debts:

| Area | Status | Why |
|---|---|---|
| `u20/state0/sprite262` actor frame/cell parity | PARTIAL | It is source-backed, but not original-frame compared. |
| `speffect14/AH12` | PARTIAL | Rebuild reaches it, but visual is weak/subtle versus expected live richness. |
| `speffect15/AH1` | PARTIAL | Rebuild has a source-shaped texture/blend approximation, not pixel-perfect. |
| Fire original-vs-rebuild compare | PENDING | No original frame strip capture matched to these smoke frames. |

## Next Recommended Slice

Do not fake per-skill visuals. The next useful patch should target the shared
renderer piece that makes source-distinct skills look too similar:

```text
Audit/fix AH type12 rendering for speffect14, using skill2 and skill8 as the
focused fire comparison pair.
```

Reason:

```text
skill2/skill8 are source-proven to include an extra H12/sp14 segment, but current
PNG smoke makes that segment too subtle. Fixing AH12 is source-backed and less
dangerous than inventing new animations for every Fire skill.
```

