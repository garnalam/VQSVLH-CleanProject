# 329 - Battle Skill 20 / Hat Bui Source Logic Animation Closeout

Date: 2026-07-14

Status: CODED / SMOKE-COVERED / PORTED-PARTIAL.

This starts Earth Lane `20..29` after the Wood Lane closeout in `328`.
Timeline code for this lane is now isolated in:

```text
rebuild_game/src/main/java/EarthSkill.java
```

`VqsvSmokeHarness.java` was not expanded for this skill timeline.

## Source Identity

| Field | Value |
| --- | --- |
| Skill id | `20` |
| Vietnamese name | `Hat Bui` / source text id `137` |
| Source description | low damage |
| `aq.c[1][20]` | `[2,137,549,100,0,45,0,-1,-1,0]` |
| Element lane | `2`, Earth Lane |
| Power | `100` |
| PP max | `45` |
| Effect mode | `0`, direct/simple |
| Effect id | `-1`, no buff/debuff |
| Param | `-1` |
| Target side | `0`, enemy target |

## Numeric Logic

Skill `20` is the basic Earth direct hit.

- spends one PP: `45 -> 44`;
- uses direct damage with `powerPercent = 100`;
- applies no buff;
- applies no debuff;
- has no `game.d.q()` post-effect.

Smoke run result:

```text
Before:       player HP 134/134, enemy HP 109/109, PP 45
Actor frame:  player HP 134/134, enemy HP 109/109, PP 44
Damage frame: damage 23, enemy HP display still 109/109
Settled:      enemy HP 86/109, PP 44
Crit:         false
Miss:         none
Debuff:       none
```

Trace also records:

```text
raw=24
powerPercent=100
damageBeforeModifiers=24
damageFrame=23
appliedDebuffId=-1
```

## Animation / Effect

`effect.mid[20]`:

```text
[0,0,22,0,-1,-1,0]
```

P7 chunk:

| Chunk | Raw | Meaning |
| ---: | --- | --- |
| `0` | `[0,0,22,0,-1,-1,0]` | target/enemy-side actor action `22`, state `0`, sprite `264` |

There is no P7 special-effect chunk and no P12/P13 active status visual for
skill `20`.

## Rebuild Mapping

| Source behavior | Rebuild equivalent | Status |
| --- | --- | --- |
| Earth Lane isolated skill class | `EarthSkill.java` | PORTED |
| Smoke dispatcher lane registration | `VqsvSkillSmokeSupport` includes `EarthSkill.INSTANCE` | PORTED |
| P3 skill list contains skill `20` with PP `45/45` | `SourceBattleRuntime.prepareSkillMenu` | PORTED |
| Confirm skill `20` selects enemy slot `0` | P3 confirm -> P6 target -> P7 | PORTED |
| P7 actor chunk | actor effect `22`, sprite `264`, state `0`, enemy side | PORTED |
| Direct damage | `powerPercent = 100`, same-run damage verified | PORTED |
| PP consumption | `45 -> 44` | PORTED |
| No buff/debuff | smoke asserts empty debuff text and `appliedDebuffId=-1` | PORTED |
| Exact original MIDP frame/pixel parity | no original frame compare in this slice | PENDING |

## Smoke Output

Command:

```powershell
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill20_hat_bui_timeline build\smoke\battle_skill20_hat_bui_timeline
```

PNG checkpoints:

```text
rebuild_game/build/smoke/battle_skill20_hat_bui_timeline/battle_skill20_hat_bui_timeline_before.png
rebuild_game/build/smoke/battle_skill20_hat_bui_timeline/battle_skill20_hat_bui_timeline_actor_u22_start.png
rebuild_game/build/smoke/battle_skill20_hat_bui_timeline/battle_skill20_hat_bui_timeline_damage_frame.png
rebuild_game/build/smoke/battle_skill20_hat_bui_timeline/battle_skill20_hat_bui_timeline_hp_settled.png
```

Debug file:

```text
rebuild_game/build/smoke/battle_skill20_hat_bui_timeline/battle_skill20_hat_bui_timeline_debug.txt
```

## Battle Lab

Interactive battle lab remains table-driven through:

```text
SourceBattleRuntime.enableSkillLabAllSkills
```

So skill `20` is available in `battle_lab_skill_test_all`; the focused
automated timeline lives in `EarthSkill.java`.

## Verification

Passed:

```text
rebuild_game/build.ps1
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill20_hat_bui_timeline build\smoke\battle_skill20_hat_bui_timeline
```

## Next Roadmap Step

Continue Earth Lane in order with skill `21` / `Tho thuan`.

Required format remains:

```text
audit source -> numeric logic -> animation/effect -> before/during/after smoke -> battle lab
```

Skill `21` is not just direct-simple: it is raw damage plus a self-defense
buff through `game.d.q()`, so audit the source row, `effect.mid[21]`, and the
post-skill buff path before coding.
