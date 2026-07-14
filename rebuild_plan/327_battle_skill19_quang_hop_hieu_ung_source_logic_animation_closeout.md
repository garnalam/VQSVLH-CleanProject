# 327 - Battle Skill 19 / Quang Hop Hieu Ung Source Logic Animation Closeout

Date: 2026-07-14

Status: CODED / SMOKE-COVERED / PORTED-PARTIAL.

Scope is Wood Lane skill `19` only. The implementation adds the focused
timeline smoke to `WoodSkill.java`; no new timeline logic was added to
`VqsvSmokeHarness.java`.

## Source Identity

| Field | Value |
| --- | --- |
| Skill id | `19` |
| Vietnamese name | `Quang hop hieu ung` / source text id `136` |
| Source description | high damage, target enters `Thuc Loai`, after two turns takes high damage |
| `aq.c[1][19]` | `[1,136,548,150,3,15,2,3,200,0]` |
| Element lane | `1`, Wood Lane |
| Power | `150` |
| PP max | `15` |
| Effect mode | `2`, target debuff producer |
| Effect id | `3`, `Thuc Loai` |
| Delayed damage param | `200` |
| Target side | `0`, enemy target |

## Numeric Logic

Skill `19` is the stronger sibling of skill `13`.

- spends one PP: `15 -> 14`;
- applies direct damage using `powerPercent = 150`;
- applies debuff id `3`;
- debuff duration comes from `aq.c[7][3][2] == 3`;
- debuff stores the pre-skill raw damage in the target slot;
- tick 1 and tick 2 do not change HP;
- final tick applies delayed damage:

```text
max(1, storedRaw * aq.c[1][sourceSkill][8] / 100)
```

For skill `19`, `aq.c[1][19][8] == 200`, so final tick damage is:

```text
max(1, storedRaw * 200 / 100)
```

Smoke run result:

```text
Before:        player HP 134/134, enemy HP 109/109, PP 15
Actor frame:   player HP 134/134, enemy HP 109/109, PP 14
Damage frame:  direct damage 37, debuff text Thuc Loai, enemy HP display still 109/109
HP settled:    enemy HP 72/109, debuff3 duration 3, storedRaw 24
Tick 1:        HP 72 -> 72, duration 3 -> 2
Tick 2:        HP 72 -> 72, duration 2 -> 1
Final tick:    HP 72 -> 24, delayed damage 48 = max(1, 24 * 200 / 100)
```

## Animation / Effect

`effect.mid[19]`:

```text
[0,0,21,0,-1,-1,0]
```

P7 chunk:

| Chunk | Raw | Meaning |
| ---: | --- | --- |
| `0` | `[0,0,21,0,-1,-1,0]` | target/enemy-side actor action `21`, state `0`, sprite `263` |

There is no P7 special-effect chunk for skill `19`.

Active queue visual for debuff `3` uses:

```text
bufDebuf ar[1][3] = [0,21,0,-1]
```

So P12/P13 shows actor action `21/state0` on the affected body before the tick
logic applies.

## Rebuild Mapping

| Source behavior | Rebuild equivalent | Status |
| --- | --- | --- |
| P3 skill list contains skill `19` with PP `15/15` | `SourceBattleRuntime.prepareSkillMenu` | PORTED |
| Confirm skill `19` selects enemy slot `0` | P3 confirm -> P6 target -> P7 | PORTED |
| P7 actor chunk | actor effect `21`, sprite `263`, state `0`, enemy side | PORTED |
| Direct damage | `powerPercent = 150`, same-run damage verified | PORTED |
| PP consumption | `15 -> 14` | PORTED |
| Debuff producer | enemy debuff id `3`, duration `3`, source skill `19` | PORTED |
| HUD icon/duration | icon cell `4`, duration cells `137 -> 136 -> 135` | PORTED |
| P12/P13 visual | active queue actor effect `21/state0` | PORTED/PARTIAL |
| Final delayed damage | `storedRaw * 200 / 100` on final tick | PORTED |
| Exact original MIDP frame/pixel parity | no original frame compare in this slice | PENDING |

## Smoke Output

Command:

```powershell
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill19_quang_hop_hieu_ung_timeline build\smoke\battle_skill19_quang_hop_hieu_ung_timeline
```

PNG checkpoints:

```text
rebuild_game/build/smoke/battle_skill19_quang_hop_hieu_ung_timeline/battle_skill19_quang_hop_hieu_ung_timeline_before.png
rebuild_game/build/smoke/battle_skill19_quang_hop_hieu_ung_timeline/battle_skill19_quang_hop_hieu_ung_timeline_actor_u21_start.png
rebuild_game/build/smoke/battle_skill19_quang_hop_hieu_ung_timeline/battle_skill19_quang_hop_hieu_ung_timeline_damage_debuff_frame.png
rebuild_game/build/smoke/battle_skill19_quang_hop_hieu_ung_timeline/battle_skill19_quang_hop_hieu_ung_timeline_hp_settled_debuff_active.png
rebuild_game/build/smoke/battle_skill19_quang_hop_hieu_ung_timeline/battle_skill19_quang_hop_hieu_ung_timeline_p12_body_visual_actor21.png
rebuild_game/build/smoke/battle_skill19_quang_hop_hieu_ung_timeline/battle_skill19_quang_hop_hieu_ung_timeline_tick1_no_damage_duration2.png
rebuild_game/build/smoke/battle_skill19_quang_hop_hieu_ung_timeline/battle_skill19_quang_hop_hieu_ung_timeline_tick2_no_damage_duration1.png
rebuild_game/build/smoke/battle_skill19_quang_hop_hieu_ung_timeline/battle_skill19_quang_hop_hieu_ung_timeline_final_delayed_damage_expired.png
rebuild_game/build/smoke/battle_skill19_quang_hop_hieu_ung_timeline/battle_skill19_quang_hop_hieu_ung_timeline_controlled_final_tick_visual.png
```

Debug file:

```text
rebuild_game/build/smoke/battle_skill19_quang_hop_hieu_ung_timeline/battle_skill19_quang_hop_hieu_ung_timeline_debug.txt
```

## Battle Lab

Interactive battle lab already uses the all-skill source table through
`SourceBattleRuntime.enableSkillLabAllSkills`, so skill `19` is available there
without adding per-skill code to `VqsvSmokeHarness`.

The focused automated timeline is now owned by `WoodSkill.java`:

```text
battle_skill19_quang_hop_hieu_ung_timeline
```

## Verification

Passed:

```text
rebuild_game/build.ps1
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill19_quang_hop_hieu_ung_timeline build\smoke\battle_skill19_quang_hop_hieu_ung_timeline
```

## Next Roadmap Step

Continue Wood Lane after skill `19`.

The next slice should start the next lane/group according to `303`, unless the
user wants to revisit exact original frame timing or pixel comparison for Wood
Lane skill visuals first.
