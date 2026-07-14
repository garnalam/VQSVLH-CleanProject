# 330 - Battle Skill 21 / Tho Thuan Source Logic Animation Closeout

Date: 2026-07-14

Status: CODED / SMOKE-COVERED / PORTED-PARTIAL.

Scope is Earth Lane skill `21` only. Timeline code lives in:

```text
rebuild_game/src/main/java/EarthSkill.java
```

No new skill timeline logic was added to `VqsvSmokeHarness.java`.

## Source Identity

| Field | Value |
| --- | --- |
| Skill id | `21` |
| Vietnamese name | `Tho thuan` / source text id `138` |
| Source description | damage and self defense up |
| `aq.c[1][21]` | `[2,138,550,80,0,45,1,4,10,0]` |
| Element lane | `2`, Earth Lane |
| PP max | `45` |
| Effect mode | `1`, self buff producer through `game.d.q()` |
| Effect id | `4`, buff4 / `Phong ngu` |
| Buff parameter | `10` |
| Target side | `0`, enemy target for damage |

Important source nuance:

```text
skill21 is SOURCE_SWITCH_GAP resolved as bytecode-default raw damage.
The source-shaped damage path ignores powerPercent=80 and effectId=4 for the
direct damage calculation, then game.d.q() applies self buff4.
```

## Numeric Logic

Skill `21` does two things:

1. Hit the enemy with bytecode-default raw damage.
2. Apply buff4 to the attacker through `game.d.q()`.

Buff4 source facts from `266`:

```text
aq.c[6][4] = [337,352,2,-1,-1]
storedDefense = baseDefense * aq.c[1][sourceSkill][8] / 100
skill21[8] = 10
duration = 2
iconCell = 16
```

The focused skill timeline forces player base defense to `100` so the formula
is visible:

```text
storedDefense = 100 * 10 / 100 = 10
apply: defense 100 -> 110, duration 2
tick1: defense 110 -> 120, duration 1
expiry: defense 120 -> 100, duration 0, icon cleared
```

Smoke run result:

```text
Before:       player HP 134/134, enemy HP 109/109, PP 45, player defense 100
Actor frame:  actor 22/sprite264/state0, enemy HP unchanged, PP 44
Special:      speffect5 / AH9 on player side
Damage frame: damage 25, enemy HP display still 109/109
After apply:  enemy HP 84/109, post text Phong ngu, defense 100 -> 110, buff4 duration 2
Tick1:        defense 120, duration 1
Expired:      defense 100, duration 0, icon cleared
```

## Animation / Effect

`effect.mid[21]`:

```text
[0,0,22,0,-1,-1,0, 1,1,5,0,-1,-1,0]
```

P7 chunks:

| Chunk | Raw | Meaning |
| ---: | --- | --- |
| `0` | `[0,0,22,0,-1,-1,0]` | target/enemy-side actor action `22`, state `0`, sprite `264` |
| `1` | `[1,1,5,0,-1,-1,0]` | attacker/player-side `speffect5` |

`speffect.mid[5]`:

```text
[9,120,208,172,208,0,8,2]
```

Renderer:

```text
AH type 9
```

## Rebuild Mapping

| Source behavior | Rebuild equivalent | Status |
| --- | --- | --- |
| Earth Lane isolated skill class | `EarthSkill.java` | PORTED |
| P3 skill list contains skill `21` with PP `45/45` | `SourceBattleRuntime.prepareSkillMenu` | PORTED |
| Confirm skill `21` selects enemy slot `0` | P3 confirm -> P6 target -> P7 | PORTED |
| P7 chunk 0 actor action | actor effect `22`, sprite `264`, state `0`, enemy side | PORTED |
| P7 chunk 1 special | `speffect5`, AH type `9`, player side | PORTED/PARTIAL |
| Damage path | bytecode-default raw damage; smoke damage `25` | PORTED |
| PP consumption | `45 -> 44` | PORTED |
| `game.d.q()` post effect | self buff4, post text `Phong ngu` | PORTED |
| Buff4 numeric value | `baseDefense * skill[8] / 100` | PORTED |
| Buff4 HUD | icon `16`, duration `136 -> 135 -> cleared` | PORTED |
| Buff4 expiry | defense restores to base | PORTED |
| Exact original MIDP frame/pixel parity | no original frame compare in this slice | PENDING |

## Smoke Output

Command:

```powershell
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill21_tho_thuan_timeline build\smoke\battle_skill21_tho_thuan_timeline
```

PNG checkpoints:

```text
rebuild_game/build/smoke/battle_skill21_tho_thuan_timeline/battle_skill21_tho_thuan_timeline_before.png
rebuild_game/build/smoke/battle_skill21_tho_thuan_timeline/battle_skill21_tho_thuan_timeline_actor_u22_start.png
rebuild_game/build/smoke/battle_skill21_tho_thuan_timeline/battle_skill21_tho_thuan_timeline_speffect5.png
rebuild_game/build/smoke/battle_skill21_tho_thuan_timeline/battle_skill21_tho_thuan_timeline_damage_frame.png
rebuild_game/build/smoke/battle_skill21_tho_thuan_timeline/battle_skill21_tho_thuan_timeline_after_apply_buff_icon.png
rebuild_game/build/smoke/battle_skill21_tho_thuan_timeline/battle_skill21_tho_thuan_timeline_tick1_duration1.png
rebuild_game/build/smoke/battle_skill21_tho_thuan_timeline/battle_skill21_tho_thuan_timeline_expired.png
```

Debug file:

```text
rebuild_game/build/smoke/battle_skill21_tho_thuan_timeline/battle_skill21_tho_thuan_timeline_debug.txt
```

## Battle Lab

Interactive battle lab remains table-driven through:

```text
SourceBattleRuntime.enableSkillLabAllSkills
```

So skill `21` is available in `battle_lab_skill_test_all`; the focused
automated timeline is owned by `EarthSkill.java`.

## Verification

Passed:

```text
rebuild_game/build.ps1
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill21_tho_thuan_timeline build\smoke\battle_skill21_tho_thuan_timeline
```

## Next Roadmap Step

Continue Earth Lane in order with skill `22` / `Bao cat`.

Required format remains:

```text
audit source -> numeric logic -> animation/effect -> before/during/after smoke -> battle lab
```

Skill `22` should audit debuff1 (`Me Muoi`) chance flow before coding because
it is a direct-damage debuff producer, not a plain direct hit.
