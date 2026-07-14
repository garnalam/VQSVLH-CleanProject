# 307 - Battle Skill 6 / Hỏa Diễm Đao Source Logic Animation Closeout

Date: 2026-07-14

Status: CODED / SMOKE-COVERED / PORTED-PARTIAL.

This continues the fire-lane skill work from
`303_battle_all_skill_source_logic_animation_audit.md`, after skill `0`
was closed in `306`.

Scope is only skill `6`. Battle runtime gameplay logic was not changed; the
code change generalizes the focused direct-skill timeline smoke helper so it
can assert both low-power and high-power direct rows from their own source
parameters.

## Source Identity

| Field | Value |
| --- | --- |
| Skill id | `6` |
| Vietnamese name | `Hỏa diễm đao` |
| Source description | `Tỷ lệ thương tổn gia tăng khá cao.` |
| `aq.c[1][6]` | `[0,123,535,150,2,30,0,-1,-1,0]` |
| Element lane | `0`, fire lane |
| Power | `150` |
| PP max | `30` |
| Effect mode | `0`, ordinary direct damage |
| Effect id | `-1`, no ordinary buff/debuff |
| Param | `-1`, no chance/extra param |
| Target side | `0`, target enemy |

Meaning:

```text
Hỏa diễm đao is the stronger direct fire attack.
It spends 1 PP, plays the same target-side fire actor action as Hỏa trảo,
then applies ordinary direct damage with powerPercent 150 if the hit check succeeds.
It does not apply buff, debuff, heal, catch modifier, or post-skill q() side effect.
```

## Source Animation Path

`effect.mid[6]`:

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
- `game.b.b(target)` includes skill `6` in the ordinary direct damage switch and applies `powerPercent = 150`.

## Rebuild Mapping

| Source behavior | Rebuild equivalent | Status |
| --- | --- | --- |
| P3 skill list contains skill `6` with PP `30/30` | `SourceBattleRuntime.prepareSkillMenu` from `BattleUnit.skillIds` | PORTED |
| Confirm skill `6` targets enemy slot `0` | P3 confirm -> target vector -> P2/P7 | PORTED |
| P7 loads `effect.mid[6]` | `VqsvBattleAnimationTables.effectRow(6)` | PORTED |
| Actor action `effectId=20,state=0,target side` | P7 actor animation, sprite `262`, state `0`, enemy side | PORTED |
| No AH special | `battleP7SpecialVisible=false` for skill 6 | PORTED |
| Direct formula uses power `150` | trace shows `raw=24 powerPercent=150 damageBeforeModifiers=36` | PORTED |
| Damage waits until after actor gate | one-run smoke asserts HP unchanged at actor frame and damage frame | PORTED |
| PP consumes once | `30 -> 29` | PORTED |
| No buff/debuff text | `debuffText=""`, `appliedDebuffId=-1` | PORTED |
| Exact original MIDP frame/pixel parity | no original-vs-rebuild frame compare yet | PENDING |

## One-Run Debug Result

Smoke suite:

```powershell
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-suite battle_skill6_direct_timeline .\build_intro_demo\battle_skill6_direct_timeline
```

Result:

```text
PASS
HP: player 134/134, enemy 109/109 -> 72/109
PP: 30 -> 29
Damage: 37
Actor: effect 20, sprite 262, state 0, side enemy
Crit: false
Miss text: empty
Debuff text: empty
```

Frame checkpoints from the same run:

| Frame | PNG | Debug |
| --- | --- | --- |
| Before confirm | `rebuild_game/build_intro_demo/battle_skill6_direct_timeline/battle_skill6_direct_timeline_before.png` | P3, HP `134/134:109/109`, PP `30` |
| Actor action | `rebuild_game/build_intro_demo/battle_skill6_direct_timeline/battle_skill6_direct_timeline_actor_u20_start.png` | P7 actor visible, sprite `262`, HP still `109/109`, PP `29` |
| Damage frame | `rebuild_game/build_intro_demo/battle_skill6_direct_timeline/battle_skill6_direct_timeline_damage_frame.png` | damage text `-37`, HP display still `109/109` |
| HP settled | `rebuild_game/build_intro_demo/battle_skill6_direct_timeline/battle_skill6_direct_timeline_hp_settled.png` | enemy HP display `72/109`, PP still `29` |

Debug text:

```text
rebuild_game/build_intro_demo/battle_skill6_direct_timeline/battle_skill6_direct_timeline_debug.txt
```

## Verification

Passed:

```text
rebuild_game/build.ps1
com.vqsv.rebuild.Main --check
VqsvBattleDamageFormulaCheck
--smoke-suite battle_skill6_direct_timeline
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

Skill `6` is now source-shaped and smoke-covered for:

- source row and text ids;
- high-power direct damage metadata;
- P3 confirm path;
- P7 actor effect id/state/side;
- PP consumption from `30` to `29`;
- damage text before HP display settles;
- final HP after the same run's damage;
- no buff/debuff side effect.

Still `PORTED-PARTIAL` because exact original MIDP pixel/frame comparison is
not performed in this slice.

## Next Roadmap Step

Continue the fire lane. The next practical slice is skill `1` / `Dương viêm`:

```text
Direct fire damage plus debuff0 burn/drain setup.
```

That slice must prove both direct actor `20->262` and the debuff0 side effect:
stored raw damage, duration, tick divisor `4`, icon/body visual, and expiry.
