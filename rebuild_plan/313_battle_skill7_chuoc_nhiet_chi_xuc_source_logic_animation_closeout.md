# 313 - Battle Skill 7 Chuoc nhiet chi xuc Source Logic / Animation Closeout

Status: `PORTED/PARTIAL`

Scope: one focused fire-lane skill closeout for skill `7`. No live client was opened; verification used smoke PNG only.

## Source Facts

| Source | Row / method | Meaning |
| --- | --- | --- |
| `aq.c[1][7]` | `[0,124,536,75,2,30,2,0,3,0]` | Fire skill, power `75`, learn tier `2`, PP `30`, effectMode `2`, debuff id `0`, divisor param `3`, enemy target side `0`. |
| `effect.mid[7]` | `[0,0,20,0,-1,-1,0]` | P7 actor action effect id `20`, state `0`, target/enemy side. |
| `aq.c[7][0]` | `[311,322,3]` | Debuff0 `Gieo Hat`, duration `3`. |
| `bufDebuf aq[0]` | `[1,18,0,-1]` | Active queue body visual uses `speffect18`. |
| `speffect[18]` | `[9,150,181,37,84,0,9,9]` | AH type `9` body-attached/source-shaped overlay. |
| `game.b.b(target)` | skill cases `1` / `7` | Damage base is `raw * skill[3] / 100 + raw / skill[8]`, then the common source jitter can move final damage by `-1/0/+1`. |
| `game.b.b(target)` | debuff0 apply | Stores `w[0][1] = preSkillRawDamage`, stores source skill id `7`, duration from `aq.c[7][0][2]`. |
| `game.b.q(0)` | active debuff tick | Tick damage is `max(1, storedRaw / aq.c[1][sourceSkill][8])`; for skill `7`, `storedRaw / 3`. |

## Rebuild Mapping

| Area | Rebuild equivalent | Result |
| --- | --- | --- |
| Skill row parse | `BattleSkillRow` via `VqsvBattleTables.skill(7)` | `PORTED` |
| P7 actor animation | `effect.mid[7]` -> actor effect `20`, sprite `262`, state `0` | `PORTED/PARTIAL`; no MIDP pixel compare claim |
| Damage formula | direct-plus-raw-divisor path, source jitter allowed | `PORTED` |
| Debuff producer | enemy debuff0 active, source skill `7`, duration `3`, icon `1/137` | `PORTED` |
| Active queue visual | `bufDebuf aq[0] -> speffect18 -> AH type9` | `PORTED/PARTIAL`; source-shaped visual |
| Active queue tick | tick damage `storedRaw / 3`, duration `3 -> 2 -> 1 -> 0` | `PORTED` |
| Expiry | final duration `0`, status icon/count clears | `PORTED` |

## Smoke Result

Suite:

```text
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill7_chuoc_nhiet_chi_xuc_timeline build\smoke\battle_skill7_chuoc_nhiet_chi_xuc_timeline
```

Output summary from the checked run:

```text
Before:
  HP 134/134 : 109/109
  PP 30

Actor frame:
  actor effect 20
  sprite 262
  HP still 109/109
  PP 30 -> 29

Damage/debuff frame:
  storedRaw 24
  damage base 24*75/100 + 24/3 = 26
  source jitter made visible damage 25 in this run
  enemy HP display still 109/109 during frame
  debuff text Gieo Hat
  debuff duration 3
  source skill 7

HP settled:
  enemy HP 109/109 -> 84/109
  icon cell 1
  duration cell 137

Active queue:
  P12 body visual uses speffect18, AH type 9
  tick damage 24/3 = 8
  HP 84 -> 76
  duration 3 -> 2

Expiry:
  HP 76 -> 68 -> 60
  duration 2 -> 1 -> 0
  status icon clears
```

Smoke PNGs:

- `rebuild_game/build/smoke/battle_skill7_chuoc_nhiet_chi_xuc_timeline/battle_skill7_chuoc_nhiet_chi_xuc_timeline_before.png`
- `rebuild_game/build/smoke/battle_skill7_chuoc_nhiet_chi_xuc_timeline/battle_skill7_chuoc_nhiet_chi_xuc_timeline_actor_u20_start.png`
- `rebuild_game/build/smoke/battle_skill7_chuoc_nhiet_chi_xuc_timeline/battle_skill7_chuoc_nhiet_chi_xuc_timeline_damage_debuff_frame.png`
- `rebuild_game/build/smoke/battle_skill7_chuoc_nhiet_chi_xuc_timeline/battle_skill7_chuoc_nhiet_chi_xuc_timeline_hp_settled_debuff_active.png`
- `rebuild_game/build/smoke/battle_skill7_chuoc_nhiet_chi_xuc_timeline/battle_skill7_chuoc_nhiet_chi_xuc_timeline_p12_body_visual_speffect18.png`
- `rebuild_game/build/smoke/battle_skill7_chuoc_nhiet_chi_xuc_timeline/battle_skill7_chuoc_nhiet_chi_xuc_timeline_tick_damage_duration2.png`
- `rebuild_game/build/smoke/battle_skill7_chuoc_nhiet_chi_xuc_timeline/battle_skill7_chuoc_nhiet_chi_xuc_timeline_expired.png`
- Debug text: `rebuild_game/build/smoke/battle_skill7_chuoc_nhiet_chi_xuc_timeline/battle_skill7_chuoc_nhiet_chi_xuc_timeline_debug.txt`

## Notes

- Skill `7` intentionally shares the same actor effect row as fire skills `0`, `1`, and `6`: `[0,0,20,0,-1,-1,0]`.
- The distinguishing behavior is not a unique P7 actor sprite; it is the stronger direct-plus-DoT formula and debuff0 divisor `3`.
- Direct damage can vary by the common source jitter after the base formula. The smoke asserts final damage is within source jitter range, not a fixed damage number.
- The debuff tick itself is deterministic in this smoke: `storedRaw / 3`.
- No pixel-perfect MIDP parity is claimed for actor/effect timing or AH type `9` alpha.

## Next Roadmap Step

Continue sequential fire lane with skill `8` (`Liet diem phong bao`): high fire damage plus debuff1 `Me Muoi` chance `20`. It should reuse the skill2 structure but with skill8 source row, forced debuff roll, actor effect `20`, `speffect14` AH type `12`, hit/miss/crit sanity, and debuff1 tick/expiry.
