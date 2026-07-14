# 310 - Battle Skill 3 Hoa Van trieu Source Logic / Animation Closeout

Status: `PORTED/PARTIAL`

Scope: one focused fire-lane skill closeout for skill `3`. No live client was opened; verification used smoke PNG.

## Source Facts

| Source | Row / method | Meaning |
| --- | --- | --- |
| `aq.c[1][3]` | `[0,120,532,100,1,30,0,-1,120,0]` | Fire skill, baseline power `100`, learn tier `1`, PP `30`, no produced buff/debuff, conditional param `120`, target enemy side `0`. |
| `effect.mid[3]` | `[0,0,20,0,-1,-1,0]` | Single actor action effect id `20`, state `0`, target/enemy side. |
| `aq.c[7][0]` | `[311,322,3]` | Existing debuff0 (`Gieo Hat`) is the condition checked by this skill. Skill `3` does not apply it. |
| `game.b.b(target)` | cases `3` / `9` | If target has debuff0, damage is `raw * skill[8] / 100`; otherwise `raw * skill[3] / 100`. |

## Rebuild Mapping

| Area | Rebuild equivalent | Result |
| --- | --- | --- |
| Skill row parse | `BattleSkillRow` via `VqsvBattleTables.skill(3)` | `PORTED` |
| P7 actor animation | `effect.mid[3]` -> actor effect `20`, sprite `262`, state `0` | `PORTED/PARTIAL`; source-shaped, no MIDP pixel compare claim |
| Baseline formula | target has no debuff0 -> `raw * 100 / 100` | `PORTED` |
| Conditional formula | target has debuff0 -> `raw * 120 / 100` | `PORTED` |
| Debuff producer | none | `PORTED`; `appliedDebuffId=-1` |

## Smoke Result

Suite:

```text
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill3_hoa_van_trieu_timeline build\smoke\battle_skill3_hoa_van_trieu_timeline
```

Output summary:

```text
Baseline:
  HP 134/134 : 109/109
  PP 30 -> 29
  damage=25
  enemy HP 109/109 -> 84/109
  debuffText empty

Conditional target has debuff0:
  HP 134/134 : 109/109
  PP 30 -> 29
  damage=29
  enemy HP 109/109 -> 80/109
  debuffText empty
  visible status: enemy debuff0 icon cell 1, duration cell 137
```

Smoke PNGs:

- `rebuild_game/build/smoke/battle_skill3_hoa_van_trieu_timeline/battle_skill3_hoa_van_trieu_timeline_baseline_before.png`
- `rebuild_game/build/smoke/battle_skill3_hoa_van_trieu_timeline/battle_skill3_hoa_van_trieu_timeline_baseline_actor_u20_start.png`
- `rebuild_game/build/smoke/battle_skill3_hoa_van_trieu_timeline/battle_skill3_hoa_van_trieu_timeline_baseline_damage_frame.png`
- `rebuild_game/build/smoke/battle_skill3_hoa_van_trieu_timeline/battle_skill3_hoa_van_trieu_timeline_baseline_hp_settled.png`
- `rebuild_game/build/smoke/battle_skill3_hoa_van_trieu_timeline/battle_skill3_hoa_van_trieu_timeline_conditional_debuff0_before.png`
- `rebuild_game/build/smoke/battle_skill3_hoa_van_trieu_timeline/battle_skill3_hoa_van_trieu_timeline_conditional_debuff0_actor_u20_start.png`
- `rebuild_game/build/smoke/battle_skill3_hoa_van_trieu_timeline/battle_skill3_hoa_van_trieu_timeline_conditional_debuff0_damage_frame.png`
- `rebuild_game/build/smoke/battle_skill3_hoa_van_trieu_timeline/battle_skill3_hoa_van_trieu_timeline_conditional_debuff0_hp_settled.png`
- `rebuild_game/build/smoke/battle_skill3_hoa_van_trieu_timeline/battle_skill3_hoa_van_trieu_timeline_visual_compare_zoom.png`
- Debug text: `rebuild_game/build/smoke/battle_skill3_hoa_van_trieu_timeline/battle_skill3_hoa_van_trieu_timeline_debug.txt`

## Notes

- Skill `3` is not a debuff producer. It only reads target debuff0 and chooses the stronger formula branch.
- The conditional setup in smoke preloads the real enemy debuff0 status slot so the Gieo Hat icon is visible before and during the hit. This is not claiming that skill `3` itself displays or applies debuff0.
- Source does not call a different skill animation for the conditional branch: both baseline and conditional use `effect.mid[3] -> actor effect 20`; the visible difference is the existing debuff icon plus higher damage.
- No pixel-perfect MIDP parity is claimed for actor/effect timing.

## Next Roadmap Step

Continue sequential fire lane with skill `4` (`Thien Hoa te`): no-damage buff0 producer. It needs visual, buff icon/duration, defense +30%, duration-edge damage hook, and expiry checks.
