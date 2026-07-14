# 309 - Battle Skill 2 Diem kich Source Logic / Animation Closeout

Status: `PORTED/PARTIAL`

Scope: one focused fire-lane skill closeout for skill `2` only. No live client was opened; verification used smoke PNG.

## Source Facts

| Source | Row / method | Meaning |
| --- | --- | --- |
| `aq.c[1][2]` | `[0,119,531,100,0,45,2,1,10,0]` | Fire skill, power `100`, PP `45`, effect mode `2`, debuff id `1`, explicit debuff chance `10`, target enemy side `0`. |
| `effect.mid[2]` | `[0,0,20,0,-1,-1,0,0,1,14,0,0,0,-1]` | Two chunks: actor action effect id `20`, then special effect id `14`. |
| `speffect.mid[14]` | starts with `12` | AH type `12` special renderer. |
| `aq.c[7][1]` | `[312,323,2]` | Debuff1 duration `2`; text name is `Me Muoi` in this doc's ASCII form. |
| `game.b.b(target)` | cases `2/8/22/28/41/47` | Damage is `raw * skill[3] / 100`; explicit debuff roll uses `skill[8]`. |
| `game.b.q(1)` | active debuff tick | No HP/stat delta; duration decrements and then clears. |

## Rebuild Mapping

| Area | Rebuild equivalent | Result |
| --- | --- | --- |
| Skill row parse | `BattleSkillRow` via `VqsvBattleTables.skill(2)` | `PORTED` |
| P7 actor animation | `effect.mid[2]` chunk0 -> actor effect `20`, sprite `262`, state `0` | `PORTED/PARTIAL`; source-shaped, no MIDP pixel compare claim |
| P7 special animation | `effect.mid[2]` chunk1 -> `speffect14`, AH type `12` | `PORTED/PARTIAL` |
| Damage formula | `BattleUnit.computeDamage()` explicit chance family | `PORTED` |
| Debuff producer | forced roll `0` proves 10% apply path; stores source skill/duration | `PORTED` |
| Active queue tick | `BattleUnit.tickSourceDebuff(1)` no-op tick, duration decrement | `PORTED` |
| Expiry visible state | duration reaches `0`, status icon/count clears | `PORTED` |

## Smoke Result

Suite:

```text
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill2_diem_kich_timeline build\smoke\battle_skill2_diem_kich_timeline
```

Output summary:

```text
damage=25
debuffChance=10
forcedDebuffRoll=0
PP 45 -> 44
enemy HP 109/109 -> 84/109 after P7
debuff duration 2 -> 1 -> 0
tick HP 84 -> 84
```

Smoke PNGs:

- `rebuild_game/build/smoke/battle_skill2_diem_kich_timeline/battle_skill2_diem_kich_timeline_before.png`
- `rebuild_game/build/smoke/battle_skill2_diem_kich_timeline/battle_skill2_diem_kich_timeline_actor_u20_start.png`
- `rebuild_game/build/smoke/battle_skill2_diem_kich_timeline/battle_skill2_diem_kich_timeline_speffect14_type12.png`
- `rebuild_game/build/smoke/battle_skill2_diem_kich_timeline/battle_skill2_diem_kich_timeline_damage_debuff_frame.png`
- `rebuild_game/build/smoke/battle_skill2_diem_kich_timeline/battle_skill2_diem_kich_timeline_hp_settled_debuff_active.png`
- `rebuild_game/build/smoke/battle_skill2_diem_kich_timeline/battle_skill2_diem_kich_timeline_p12_body_visual_type12.png`
- `rebuild_game/build/smoke/battle_skill2_diem_kich_timeline/battle_skill2_diem_kich_timeline_tick_noop_duration1.png`
- `rebuild_game/build/smoke/battle_skill2_diem_kich_timeline/battle_skill2_diem_kich_timeline_expired.png`
- Debug text: `rebuild_game/build/smoke/battle_skill2_diem_kich_timeline/battle_skill2_diem_kich_timeline_debug.txt`

## Notes

- Skill `2` is the first closed fire-lane skill here with a two-chunk P7 effect row.
- Debuff1 `Me Muoi` is a flag/status: it does not tick HP down, but it enables conditional stronger damage for skills `23/29` and has catch/status hooks covered in earlier debuff closeouts.
- No pixel-perfect MIDP parity is claimed for actor/effect timing.

## Next Roadmap Step

Continue sequential fire lane with skill `3` (`Hoa Van trieu`): direct fire damage whose stronger branch requires target debuff0 to already be active.
