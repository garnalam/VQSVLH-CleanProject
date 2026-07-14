# 318 - Battle Skill 10 / Diep Toan Source Logic Animation Closeout

Date: 2026-07-14

Status: CODED / SMOKE-COVERED / PORTED-PARTIAL.

This starts the Wood Lane per-skill closeout pass after the Fire Lane
`0..9` closeouts. Scope is only skill `10`. No normal battle gameplay behavior
was changed in this slice; the Java change only opens a one-run smoke timeline
for the already-ported skill10 direct actor path.

## Source Identity

| Field | Value |
| --- | --- |
| Skill id | `10` |
| Vietnamese name | `Diep Toan` / `Diệp Toàn` |
| Source description | `Thuong ton thap.` / `Thương tổn thấp.` |
| `aq.c[1][10]` | `[1,127,539,100,0,45,0,-1,-1,0]` |
| Element lane | `1`, Wood Lane |
| Power | `100` |
| PP max | `45` |
| Effect mode | `0`, ordinary direct damage |
| Effect id | `-1`, no ordinary buff/debuff |
| Param | `-1`, no chance/extra param |
| Target side | `0`, target enemy |

Meaning:

```text
Diep Toan is the basic low-damage Wood Lane attack.
It spends 1 PP, plays one target-side actor action, then applies ordinary
direct damage if the hit check succeeds.
It does not apply buff, debuff, heal, catch modifier, or q() post-effect.
```

## Source Animation Path

`effect.mid[10]`:

```text
[0,0,21,1,-1,-1,0]
```

Chunk interpretation:

| Chunk field | Value | Meaning |
| --- | ---: | --- |
| `ownerSide` | `0` | effect is on target side |
| `specialFlag` | `0` | actor action `u`, not AH special |
| `effectId` | `21` | actor effect id |
| `state` | `1` | actor animation state param |
| `nextFrameTrigger` | `-1` | no extra chunk trigger |
| `stateFrameTrigger` | `-1` | no actor state switch trigger |
| `stateToSet` | `0` | unused for this row |

Source anchors:

- `game.d.n()` loads `effect.mid[skill]`.
- For `specialFlag == 0`, it calls the target actor action route.
- `ah.java` actor effect mapping proves `21 -> sprite 263`.
- `game.b.b(target)` includes skill `10` in the ordinary direct damage path.

## Rebuild Mapping

| Source behavior | Rebuild equivalent | Status |
| --- | --- | --- |
| P3 skill list contains skill `10` with PP `45/45` | `SourceBattleRuntime.prepareSkillMenu` from `BattleUnit.skillIds` | PORTED |
| Confirm skill `10` targets enemy slot `0` | P3 confirm -> P6 auto target -> P2/P7 | PORTED |
| P7 loads `effect.mid[10]` | `VqsvBattleAnimationTables.effectRow(10)` | PORTED |
| Actor action `effectId=21,state=1,target side` | P7 actor animation, sprite `263`, state `1`, enemy side | PORTED |
| No AH special | no `battleP7SpecialVisible` requirement for this skill | PORTED |
| Damage waits until after actor gate | one-run smoke asserts HP unchanged at actor frame and damage frame | PORTED |
| PP consumes once | `45 -> 44` | PORTED |
| No buff/debuff text | `debuffText=""`, `appliedDebuffId=-1` | PORTED |
| Exact original MIDP frame/pixel parity | no original-vs-rebuild frame compare in this slice | PENDING |

## One-Run Debug Result

Smoke suite:

```powershell
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill10_direct_timeline build\smoke\battle_skill10_direct_timeline
```

Result:

```text
PASS
HP: player 134/134, enemy 109/109 -> 84/109 in the latest verification run
PP: 45 -> 44
Damage: 25 in the latest verification run
Actor: effect 21, sprite 263, state 1, side enemy
Crit: false
Miss text: empty
Debuff text: empty
```

The exact damage number can vary slightly across runs because the source-shaped
damage jitter still uses runtime RNG. The smoke invariant is stronger than the
sample number: `hpSettledEnemyHp == enemyMaxHp - damage` within the same run.

Frame checkpoints from the same run:

| Frame | PNG | Debug |
| --- | --- | --- |
| Before confirm | `rebuild_game/build/smoke/battle_skill10_direct_timeline/battle_skill10_direct_timeline_before.png` | P3, HP `134/134:109/109`, PP `45` |
| Actor action | `rebuild_game/build/smoke/battle_skill10_direct_timeline/battle_skill10_direct_timeline_actor_u21_start.png` | P7 actor visible, sprite `263`, HP still `109/109`, PP `44` |
| Damage frame | `rebuild_game/build/smoke/battle_skill10_direct_timeline/battle_skill10_direct_timeline_damage_frame.png` | latest run damage text `-25`, HP display still `109/109` |
| HP settled | `rebuild_game/build/smoke/battle_skill10_direct_timeline/battle_skill10_direct_timeline_hp_settled.png` | latest run enemy HP display `84/109`, PP still `44` |

Debug text:

```text
rebuild_game/build/smoke/battle_skill10_direct_timeline/battle_skill10_direct_timeline_debug.txt
```

## Verification

Passed in this slice:

```text
rebuild_game/build.ps1
--smoke-suite battle_skill10_direct_timeline
com.vqsv.rebuild.Main --check
```

The older `battle_skill10_direct_animation` suite also passed, but that suite
uses separate checkpoint runs. The new `battle_skill10_direct_timeline` suite is
the current per-skill closeout source because all frame PNGs and debug numbers
come from one run.

## Honest Status

Skill `10` is source-shaped and smoke-covered for:

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

Continue Wood Lane in order with skill `11` / `Quang phan`.

That slice is not direct-simple only: it is direct damage plus `game.d.q()`
post-skill heal with param `10`, and has an extra `speffect10/AH9` visual.
Do not treat it as another plain direct base skill.
