# 312 - Battle Skill 5 Viem loi pha Source Logic / Animation Closeout

Status: `PORTED/PARTIAL`

Scope: one focused fire-lane skill closeout for skill `5`. No live client was opened; verification used smoke PNG only.

## Source Facts

| Source | Row / method | Meaning |
| --- | --- | --- |
| `aq.c[1][5]` | `[0,122,534,0,1,10,1,1,-1,1]` | Fire skill, no direct damage, learn tier `1`, PP `10`, effectMode `1`, buff/effect id `1`, self/ally target side `1`. |
| `effect.mid[5]` | `[0,1,16,0,-1,-1,0, 0,1,15,0,-1,-1,0]` | Two special-effect chunks: `speffect16`, then `speffect15`. |
| `speffect[16]` | `[9,150,181,37,84,0,5,5]` | AH type `9` overlay, currently source-shaped. |
| `speffect[15]` | `[1,0,5,3,0,0]` | AH type `1` overlay, currently source-shaped. |
| `aq.c[6][1]` | `[334,349,3,50,50]` | Buff1 `Pha Phu`: duration `3`, defense `-50%`, outgoing damage `+50%`. |
| `game.b.a(byte,int,int)` | buff case `1` | Applies `v[1][1] = baseDefense * 50 / 100`, `v[1][2] = 50`, then lowers current defense. |
| `game.b.b(target)` | `m(1)` | Adds `damage * v[1][2] / 100` to outgoing damage. |

## Rebuild Mapping

| Area | Rebuild equivalent | Result |
| --- | --- | --- |
| Skill row parse | `BattleSkillRow` via `VqsvBattleTables.skill(5)` | `PORTED` |
| Buff row parse | `BattleBuffRow` via `VqsvBattleTables.buff(1)` | `PORTED` |
| P7 no-damage path | `game.d.q postEffect skill=5`, no hitroll, no damage frame | `PORTED` |
| Animation chunk 0 | `speffect16` / AH type `9` | `PORTED/PARTIAL`; no MIDP pixel compare claim |
| Animation chunk 1 | `speffect15` / AH type `1` | `PORTED/PARTIAL`; no MIDP pixel compare claim |
| Buff active logic | defense `100 -> 50`, icon `13`, duration cell `137` | `PORTED` |
| Damage hook | baseline `80`, active buff1 `120`, forced crit `180`, forced miss no HP change | `PORTED` |
| Expiry | duration `3 -> 2 -> 1 -> 0`, defense returns to base, icon clears | `PORTED` |

## Smoke Result

Suite:

```text
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill5_viem_loi_pha_timeline build\smoke\battle_skill5_viem_loi_pha_timeline
```

Output summary:

```text
Before:
  HP 134/134 : 109/109
  PP 10
  defense 100

During:
  speffect16 AH type 9 visible
  speffect15 AH type 1 visible

After apply:
  HP unchanged 134/134 : 109/109
  PP 10 -> 9
  defense 100 -> 50
  buff1 value 50
  damage bonus percent 50
  duration 3
  icon cell 13, duration cell 137

Damage probes:
  baseline damage 80
  forced hit with buff1 damage 120
  forced miss with buff1 keeps enemy HP 109/109
  forced crit with buff1 damage 180

Expiry:
  duration 3 -> 2 -> 1 -> 0
  defense 50 -> 50 -> 50 -> 100
  buff icon clears
```

Smoke PNGs:

- `rebuild_game/build/smoke/battle_skill5_viem_loi_pha_timeline/battle_skill5_viem_loi_pha_timeline_before.png`
- `rebuild_game/build/smoke/battle_skill5_viem_loi_pha_timeline/battle_skill5_viem_loi_pha_timeline_speffect16_type9.png`
- `rebuild_game/build/smoke/battle_skill5_viem_loi_pha_timeline/battle_skill5_viem_loi_pha_timeline_speffect15_type1.png`
- `rebuild_game/build/smoke/battle_skill5_viem_loi_pha_timeline/battle_skill5_viem_loi_pha_timeline_after_apply_icon.png`
- `rebuild_game/build/smoke/battle_skill5_viem_loi_pha_timeline/battle_skill5_viem_loi_pha_timeline_baseline_no_buff1_damage_frame.png`
- `rebuild_game/build/smoke/battle_skill5_viem_loi_pha_timeline/battle_skill5_viem_loi_pha_timeline_forced_hit_buff1_damage_frame.png`
- `rebuild_game/build/smoke/battle_skill5_viem_loi_pha_timeline/battle_skill5_viem_loi_pha_timeline_forced_miss_buff1_damage_frame.png`
- `rebuild_game/build/smoke/battle_skill5_viem_loi_pha_timeline/battle_skill5_viem_loi_pha_timeline_forced_crit_buff1_damage_frame.png`
- `rebuild_game/build/smoke/battle_skill5_viem_loi_pha_timeline/battle_skill5_viem_loi_pha_timeline_expiry_before_tick.png`
- `rebuild_game/build/smoke/battle_skill5_viem_loi_pha_timeline/battle_skill5_viem_loi_pha_timeline_expiry_after_first_tick.png`
- `rebuild_game/build/smoke/battle_skill5_viem_loi_pha_timeline/battle_skill5_viem_loi_pha_timeline_expiry_after_second_tick.png`
- `rebuild_game/build/smoke/battle_skill5_viem_loi_pha_timeline/battle_skill5_viem_loi_pha_timeline_expiry_after_clear.png`
- Debug text: `rebuild_game/build/smoke/battle_skill5_viem_loi_pha_timeline/battle_skill5_viem_loi_pha_timeline_debug.txt`

## Notes

- Skill `5` does not damage and does not run hit/miss/crit during its own producer P7 path.
- Hit/miss/crit coverage is checked through a controlled next attack after buff1 is active.
- Forced miss still computes potential damage in trace, but P7 hit gate does not commit HP damage.
- No pixel-perfect MIDP parity is claimed for AH type `9` / type `1` timing or alpha.

## Next Roadmap Step

Skill `6` already has a direct timeline closeout. Continue sequential fire lane with skill `7` (`Chuoc nhiet chi xuc`): direct damage plus debuff0/Gieo Hat with stronger tick divisor. It needs the same before/actor/damage/status/tick/expiry smoke structure used for skill `1`, but with skill7 source parameters.
