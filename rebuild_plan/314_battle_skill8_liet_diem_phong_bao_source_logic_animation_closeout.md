# 314 - Battle Skill 8 Liet diem phong bao Source Logic / Animation Closeout

Status: `PORTED/PARTIAL`

Scope: one focused fire-lane skill closeout for skill `8`. No live client was opened; verification used smoke PNG only.

## Source Facts

| Source | Row / method | Meaning |
| --- | --- | --- |
| `aq.c[1][8]` | `[0,125,537,200,3,15,2,1,20,0]` | Fire skill, power `200`, learn tier `3`, PP `15`, effectMode `2`, debuff id `1`, debuff chance/param `20`, enemy target side `0`. |
| `effect.mid[8]` | `[0,0,20,0,-1,-1,0, 0,1,14,0,0,-1,1]` | P7 actor action effect id `20`, then special `speffect14`. |
| `speffect[14]` | `[12,2,255,120,0,9,...]` | AH type `12` overlay, currently source-shaped. |
| `aq.c[7][1]` | `[312,323,2]` | Debuff1 `Me Muoi`, duration `2`. |
| `bufDebuf aq[1]` | `[1,14,0,-1]` | Active queue body visual uses `speffect14`. |
| `game.b.b(target)` | skill cases `2` / `8` / `22` / `28` | Direct damage uses `raw * skill[3] / 100`; debuff apply chance reads `skill[8]`. |

## Rebuild Mapping

| Area | Rebuild equivalent | Result |
| --- | --- | --- |
| Skill row parse | `BattleSkillRow` via `VqsvBattleTables.skill(8)` | `PORTED` |
| P7 actor animation | `effect.mid[8]` chunk0 -> actor effect `20`, sprite `262`, state `0` | `PORTED/PARTIAL` |
| P7 special animation | `effect.mid[8]` chunk1 -> `speffect14`, AH type `12` | `PORTED/PARTIAL` |
| Damage formula | power `200` plus common source jitter | `PORTED` |
| Debuff producer | forced roll `0` applies enemy debuff1, source skill `8`, duration `2`, icon `2/136` | `PORTED` |
| Active queue visual | `bufDebuf aq[1] -> speffect14 -> AH type12` | `PORTED/PARTIAL` |
| Active queue tick | no HP/stat delta, duration `2 -> 1 -> 0` | `PORTED` |
| Expiry | final duration `0`, status icon/count clears | `PORTED` |

## Smoke Result

Suite:

```text
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill8_liet_diem_phong_bao_timeline build\smoke\battle_skill8_liet_diem_phong_bao_timeline
```

Output summary from the checked run:

```text
Before:
  HP 134/134 : 109/109
  PP 15

Actor frame:
  actor effect 20
  sprite 262
  HP still 109/109
  PP 15 -> 14

Special frame:
  speffect14
  AH type 12

Damage/debuff frame:
  damage 49
  debuff chance 20
  forced debuff roll 0
  debuff text Me Muoi
  enemy HP display still 109/109
  debuff duration 2
  source skill 8

HP settled:
  enemy HP 109/109 -> 60/109
  icon cell 2
  duration cell 136

Active queue:
  P12 body visual uses speffect14, AH type 12
  tick is no-op for HP/stat
  HP 60 -> 60
  duration 2 -> 1

Expiry:
  duration 1 -> 0
  status icon clears
```

Smoke PNGs:

- `rebuild_game/build/smoke/battle_skill8_liet_diem_phong_bao_timeline/battle_skill8_liet_diem_phong_bao_timeline_before.png`
- `rebuild_game/build/smoke/battle_skill8_liet_diem_phong_bao_timeline/battle_skill8_liet_diem_phong_bao_timeline_actor_u20_start.png`
- `rebuild_game/build/smoke/battle_skill8_liet_diem_phong_bao_timeline/battle_skill8_liet_diem_phong_bao_timeline_speffect14_type12.png`
- `rebuild_game/build/smoke/battle_skill8_liet_diem_phong_bao_timeline/battle_skill8_liet_diem_phong_bao_timeline_damage_debuff_frame.png`
- `rebuild_game/build/smoke/battle_skill8_liet_diem_phong_bao_timeline/battle_skill8_liet_diem_phong_bao_timeline_hp_settled_debuff_active.png`
- `rebuild_game/build/smoke/battle_skill8_liet_diem_phong_bao_timeline/battle_skill8_liet_diem_phong_bao_timeline_p12_body_visual_type12.png`
- `rebuild_game/build/smoke/battle_skill8_liet_diem_phong_bao_timeline/battle_skill8_liet_diem_phong_bao_timeline_tick_noop_duration1.png`
- `rebuild_game/build/smoke/battle_skill8_liet_diem_phong_bao_timeline/battle_skill8_liet_diem_phong_bao_timeline_expired.png`
- Debug text: `rebuild_game/build/smoke/battle_skill8_liet_diem_phong_bao_timeline/battle_skill8_liet_diem_phong_bao_timeline_debug.txt`

## Notes

- Skill `8` shares the fire actor effect `20 -> sprite 262`, but unlike direct-only skill `6`, it has a second P7 special chunk: `speffect14` AH type `12`.
- Debuff1 `Me Muoi` is a flag/status. It does not tick HP down, but it enables conditional stronger damage for skills `23/29` and catch/status hooks covered in earlier debuff closeouts.
- The smoke forces the debuff roll to `0` so the producer path is deterministic.
- No pixel-perfect MIDP parity is claimed for actor/effect timing or AH type `12` alpha.

## Next Roadmap Step

Continue sequential fire lane with skill `9` (`Vinh hang hoa anh`): high damage conditional branch when target already has debuff0 `Gieo Hat`. It should reuse the skill3 visual-compare structure, but with skill9 source row and higher conditional parameter.
