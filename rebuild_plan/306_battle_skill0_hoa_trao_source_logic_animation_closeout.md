# 306 - Battle Skill 0 / Hỏa Trảo Source Logic Animation Closeout

Date: 2026-07-14

Status: CODED / SMOKE-COVERED / PORTED-PARTIAL.

This closes the first per-skill audit slice requested from
`303_battle_all_skill_source_logic_animation_audit.md`.

Scope is only skill `0`. No battle runtime gameplay logic was changed in this
slice; the code change adds a focused one-run smoke timeline so the before,
actor, damage, and HP-settled frames all come from the same skill use.

## Source Identity

| Field | Value |
| --- | --- |
| Skill id | `0` |
| Vietnamese name | `Hỏa trảo` |
| Source description | `Thương tổn thấp.` |
| `aq.c[1][0]` | `[0,117,529,100,0,45,0,-1,-1,0]` |
| Element lane | `0`, fire lane |
| Power | `100` |
| PP max | `45` |
| Effect mode | `0`, ordinary direct damage |
| Effect id | `-1`, no ordinary buff/debuff |
| Param | `-1`, no chance/extra param |
| Target side | `0`, target enemy |

Meaning:

```text
Hỏa trảo is a basic low-damage fire attack.
It spends 1 PP, plays one target-side actor action, then applies ordinary direct damage if the hit check succeeds.
It does not apply buff, debuff, heal, catch modifier, or post-skill q() side effect.
```

## Source Animation Path

`effect.mid[0]`:

```text
[0,0,20,0,-1,-1,0]
```

Chunk interpretation:

| Chunk field | Value | Meaning |
| --- | ---: | --- |
| `ownerSide` | `0` | effect is on target side |
| `specialFlag` | `0` | actor action `u`, not AH special |
| `effectId` | `20` | actor effect id |
| `state` | `0` | actor animation state param |
| `nextFrameTrigger` | `-1` | no extra chunk trigger |
| `stateFrameTrigger` | `-1` | no actor state switch trigger |
| `stateToSet` | `0` | unused for this row |

Source anchors:

- `game.d.n()` loads `ao[h.D]`, and for `specialFlag == 0` calls the target actor action when `ownerSide == 0`.
- `ah.java` actor effect mapping proves `20 -> sprite 262`.
- `game.d case 7` waits for actor/special gates before showing damage/miss text and committing HP display flow.
- `game.b.b(target)` includes skill `0` in the ordinary direct damage switch and applies `powerPercent = 100`.

## Rebuild Mapping

| Source behavior | Rebuild equivalent | Status |
| --- | --- | --- |
| P3 skill list contains skill `0` with PP `45/45` | `SourceBattleRuntime.prepareSkillMenu` from `BattleUnit.skillIds` | PORTED |
| Confirm skill `0` targets enemy slot `0` | P3 confirm -> target vector -> P2/P7 | PORTED |
| P7 loads `effect.mid[0]` | `VqsvBattleAnimationTables.effectRow(0)` | PORTED |
| Actor action `effectId=20,state=0,target side` | P7 actor animation, sprite `262`, state `0`, enemy side | PORTED |
| No AH special | `battleP7SpecialVisible=false` for skill 0 | PORTED |
| Damage waits until after actor gate | one-run smoke asserts HP unchanged at actor frame and damage frame | PORTED |
| PP consumes once | `45 -> 44` | PORTED |
| No buff/debuff text | `debuffText=""`, `appliedDebuffId=-1` | PORTED |
| Exact original MIDP frame/pixel parity | no original-vs-rebuild frame compare yet | PENDING |

## One-Run Debug Result

Smoke suite:

```powershell
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-suite battle_skill0_direct_timeline .\build_intro_demo\battle_skill0_direct_timeline
```

Result:

```text
PASS
HP: player 134/134, enemy 109/109 -> 86/109
PP: 45 -> 44
Damage: 23
Actor: effect 20, sprite 262, state 0, side enemy
Crit: false
Miss text: empty
Debuff text: empty
```

Frame checkpoints from the same run:

| Frame | PNG | Debug |
| --- | --- | --- |
| Before confirm | `rebuild_game/build_intro_demo/battle_skill0_direct_timeline/battle_skill0_direct_timeline_before.png` | P3, HP `134/134:109/109`, PP `45` |
| Actor action | `rebuild_game/build_intro_demo/battle_skill0_direct_timeline/battle_skill0_direct_timeline_actor_u20_start.png` | P7 actor visible, sprite `262`, HP still `109/109`, PP `44` |
| Damage frame | `rebuild_game/build_intro_demo/battle_skill0_direct_timeline/battle_skill0_direct_timeline_damage_frame.png` | damage text `-23`, HP display still `109/109` |
| HP settled | `rebuild_game/build_intro_demo/battle_skill0_direct_timeline/battle_skill0_direct_timeline_hp_settled.png` | enemy HP display `86/109`, PP still `44` |

Debug text:

```text
rebuild_game/build_intro_demo/battle_skill0_direct_timeline/battle_skill0_direct_timeline_debug.txt
```

## Verification

Passed:

```text
rebuild_game/build.ps1
com.vqsv.rebuild.Main --check
VqsvBattleDamageFormulaCheck
--smoke-suite battle_skill0_direct_timeline
--smoke-suite battle_direct_base_one_chunk
--smoke-suite battle_quick
```

`battle_quick` result:

```text
PASS, 227/227 checkpoints
```

New Java additions were ASCII-only; a focused mojibake pattern scan over the
new code ranges returned no matches.

## Honest Status

Skill `0` is now source-shaped and smoke-covered for:

- source row and text ids;
- direct damage metadata;
- P3 confirm path;
- P7 actor effect id/state/side;
- PP consumption;
- damage text before HP display settles;
- final HP after the same run's damage;
- no buff/debuff side effect.

Still `PORTED-PARTIAL` because exact original MIDP pixel/frame comparison is
not performed in this slice.

## Next Roadmap Step

Continue `303` by closing the remaining direct-simple family in small slices.
The next best slice is skill `6` if we want to stay in the fire lane, or skill
`50` if we want to handle the known two-chunk direct-base exception:

```text
skill50 effect.mid = [0,0,25,0,-1,-1,0, 0,1,9,0,-1,-1,0]
```

Do not merge skill `50` with one-chunk direct skills because it has actor `25`
plus `speffect9/AH type9`.
