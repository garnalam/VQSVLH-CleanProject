# 308 - Battle Skill 1 Duong viem Source Logic / Animation Closeout

Status: `PORTED/PARTIAL`

Scope: one focused fire-lane skill closeout for skill `1` only. No live client was opened; verification used smoke PNG.

## Source Facts

| Source | Row / method | Meaning |
| --- | --- | --- |
| `aq.c[1][1]` | `[0,118,530,50,0,45,2,0,4,0]` | Fire skill, power `50`, PP `45`, effect mode `2`, debuff id `0`, tick divisor/param `4`, target enemy side `0`. |
| `effect.mid[1]` | `[0,0,20,0,-1,-1,0]` | P7 actor action effect id `20`, state `0`, target/enemy side. This is the same actor route as skill `0` and skill `6` in source data. |
| `aq.c[7][0]` | `[311,322,3]` | Debuff0 duration `3`. Runtime name text is `Gieo Hat` in this doc's ASCII form. |
| `game.b.b(target)` | cases `1` / `7` | Damage formula is `raw * skill[3] / 100 + raw / skill[8]`. For skill `1`: `raw * 50 / 100 + raw / 4`. |
| `game.b.b(target)` | debuff apply block | Debuff0 stores `preSkillRaw` into `w[0][1]`, stores source skill id into `w[0][3]`, then uses duration from `aq.c[7][0][2]`. |
| `game.b.q(0)` | active debuff tick | Tick damage is `w[0][1] / aq.c[1][sourceSkill][8]`. For skill `1`: `storedRaw / 4`. |

## Rebuild Mapping

| Area | Rebuild equivalent | Result |
| --- | --- | --- |
| Skill row parse | `BattleSkillRow` via `VqsvBattleTables.skill(1)` | `PORTED` |
| P7 actor animation | `effect.mid[1]` -> actor effect `20`, sprite `262`, state `0` | `PORTED/PARTIAL`; source-shaped, no MIDP pixel compare claim |
| Damage formula | `BattleUnit.computeDamage()` special case for skill `1` / `7` | `PORTED` |
| Debuff producer | `BattlePendingDebuff` commit stores raw/source skill/duration | `PORTED` |
| Active queue tick | `BattleUnit.tickSourceDebuff(0)` | `PORTED` |
| Expiry visible state | duration reaches `0`, status icon/count clears | `PORTED` |
| Slot cleanup | old slot value remains after inactive flag clears | `PARTIAL`; harmless for visible status, consider lifecycle cleanup later if source proves full zeroing |

## Smoke Result

Suite:

```text
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill1_duong_viem_timeline build\smoke\battle_skill1_duong_viem_timeline
```

Output summary:

```text
damage=19
storedRaw=24
tickDamage=6
PP 45 -> 44
enemy HP 109/109 -> 90/109 after P7
debuff duration 3 -> 2 -> 1 -> 0
```

Smoke PNGs:

- `rebuild_game/build/smoke/battle_skill1_duong_viem_timeline/battle_skill1_duong_viem_timeline_before.png`
- `rebuild_game/build/smoke/battle_skill1_duong_viem_timeline/battle_skill1_duong_viem_timeline_actor_u20_start.png`
- `rebuild_game/build/smoke/battle_skill1_duong_viem_timeline/battle_skill1_duong_viem_timeline_damage_debuff_frame.png`
- `rebuild_game/build/smoke/battle_skill1_duong_viem_timeline/battle_skill1_duong_viem_timeline_hp_settled_debuff_active.png`
- `rebuild_game/build/smoke/battle_skill1_duong_viem_timeline/battle_skill1_duong_viem_timeline_tick_damage_duration2.png`
- `rebuild_game/build/smoke/battle_skill1_duong_viem_timeline/battle_skill1_duong_viem_timeline_expired.png`
- Debug text: `rebuild_game/build/smoke/battle_skill1_duong_viem_timeline/battle_skill1_duong_viem_timeline_debug.txt`

## Notes

- Skill `1` intentionally shares the same `effect.mid` actor row as skill `0` and skill `6`: `[0,0,20,0,-1,-1,0]`.
- The distinguishing behavior is not the actor animation; it is the formula plus debuff0 producer/tick lifecycle.
- No pixel-perfect MIDP parity is claimed for actor/effect timing.

## Next Roadmap Step

Continue the fire lane with skill `7` (`Chuoc nhiet chi xuc`): same debuff0 family, but stronger damage and tick divisor `3`.
