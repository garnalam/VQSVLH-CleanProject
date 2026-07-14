# 316 - Battle Skill 9 Vinh hang hoa anh Source Logic / Animation Closeout

Status: `PORTED/PARTIAL`

Scope: one focused fire-lane skill closeout for skill `9`. No live client was opened; verification used smoke PNG only.

## Source Facts

| Source | Row / method | Meaning |
| --- | --- | --- |
| `aq.c[1][9]` | `[0,126,538,200,3,15,0,-1,250,0]` | Fire skill, baseline power `200`, learn tier `3`, PP `15`, no produced buff/debuff, conditional param `250`, enemy target side `0`. |
| `effect.mid[9]` | `[0,0,20,0,-1,-1,0]` | P7 actor action effect id `20`, state `0`, target/enemy side. |
| `aq.c[7][0]` | `[311,322,3]` | Debuff0 `Gieo Hat`, duration `3`; used here only as pre-existing target condition. |

## Rebuild Mapping

| Area | Rebuild equivalent | Result |
| --- | --- | --- |
| Skill row parse | `BattleSkillRow` via `VqsvBattleTables.skill(9)` | `PORTED` |
| P7 actor animation | `effect.mid[9]` -> actor effect `20`, sprite `262`, state `0` | `PORTED/PARTIAL` |
| Baseline damage branch | target has no debuff0 -> `raw * 200 / 100` plus source jitter | `PORTED` |
| Conditional damage branch | target has debuff0 -> `raw * 250 / 100` plus source jitter | `PORTED` |
| Debuff producer | none; skill9 must not apply debuff0 by itself | `PORTED` |
| PP | `15 -> 14` | `PORTED` |

## Smoke Result

Suite:

```text
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill9_vinh_hang_hoa_anh_timeline build\smoke\battle_skill9_vinh_hang_hoa_anh_timeline
```

Output summary from the checked run:

```text
Baseline, no Gieo Hat:
  Before:       HP 134/134 : 109/109, PP 15
  Actor frame:  actor effect 20, sprite 262, HP enemy still 109/109, PP 14
  Damage:       49
  Settled:      enemy HP 109/109 -> 60/109
  Debuff apply: none

Conditional, target already has Gieo Hat:
  Before:       HP 134/134 : 109/109, PP 15, debuff0 visible
  Actor frame:  actor effect 20, sprite 262, HP enemy still 109/109, PP 14
  Damage:       60
  Settled:      enemy HP 109/109 -> 49/109
  Debuff apply: none
```

Smoke PNGs:

- `rebuild_game/build/smoke/battle_skill9_vinh_hang_hoa_anh_timeline/battle_skill9_vinh_hang_hoa_anh_timeline_baseline_before.png`
- `rebuild_game/build/smoke/battle_skill9_vinh_hang_hoa_anh_timeline/battle_skill9_vinh_hang_hoa_anh_timeline_baseline_actor_u20_start.png`
- `rebuild_game/build/smoke/battle_skill9_vinh_hang_hoa_anh_timeline/battle_skill9_vinh_hang_hoa_anh_timeline_baseline_damage_frame.png`
- `rebuild_game/build/smoke/battle_skill9_vinh_hang_hoa_anh_timeline/battle_skill9_vinh_hang_hoa_anh_timeline_baseline_hp_settled.png`
- `rebuild_game/build/smoke/battle_skill9_vinh_hang_hoa_anh_timeline/battle_skill9_vinh_hang_hoa_anh_timeline_conditional_debuff0_before.png`
- `rebuild_game/build/smoke/battle_skill9_vinh_hang_hoa_anh_timeline/battle_skill9_vinh_hang_hoa_anh_timeline_conditional_debuff0_actor_u20_start.png`
- `rebuild_game/build/smoke/battle_skill9_vinh_hang_hoa_anh_timeline/battle_skill9_vinh_hang_hoa_anh_timeline_conditional_debuff0_damage_frame.png`
- `rebuild_game/build/smoke/battle_skill9_vinh_hang_hoa_anh_timeline/battle_skill9_vinh_hang_hoa_anh_timeline_conditional_debuff0_hp_settled.png`
- Debug text: `rebuild_game/build/smoke/battle_skill9_vinh_hang_hoa_anh_timeline/battle_skill9_vinh_hang_hoa_anh_timeline_debug.txt`

## Notes

- Skill `9` intentionally shares the same actor-only fire animation as skills `0/1/3/6/7`: `effect.mid -> u20 -> sprite262`.
- The visible distinction is the target having pre-existing `Gieo Hat` status and the higher damage number, not a separate special effect chunk.
- The smoke confirms `appliedDebuffId=-1`; skill9 does not create `Gieo Hat`.
- Exact MIDP pixel parity for actor timing remains pending; this closeout covers source-shaped P7 timing and formula behavior.

## Verification

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill9_vinh_hang_hoa_anh_timeline build\smoke\battle_skill9_vinh_hang_hoa_anh_timeline
java -cp build\classes VqsvIntroDemo --smoke-suite battle_fire_animation_contact_sheet build\smoke\battle_fire_animation_contact_sheet
java -cp build\classes com.vqsv.rebuild.Main --check
java -cp build\classes VqsvBattleDamageFormulaCheck
java -cp build\classes VqsvIntroDemo --smoke-suite battle_quick build\smoke\battle_quick_after_skill9
rg -n "Ã|Â|�|Há»|DÆ|Ä" src\main\java
```

Result: build/check/formula/focused smoke/contact sheet/battle quick passed. Mojibake scan returned no matches.

## Next Roadmap Step

Fire lane skill `0..9` now has source-backed closeout/smoke coverage. The next clean step is to move to the next lane in order, starting skill `10` (`Diep Toan`) if we continue skill-by-skill, or first refresh the skill-lane roadmap index to mark fire lane closed.
