# 324 - Battle Skill 16 / Châm Diệp Trảm Source Logic Animation Closeout

Date: 2026-07-14

Status: CODED / SMOKE-COVERED / PORTED-PARTIAL.

This closes Wood Lane skill `16` after skill `15` in `323`. Scope is only
skill `16`. The Java change registers a focused direct-damage smoke timeline;
shared battle gameplay logic was not changed.

## Source Identity

| Field | Value |
| --- | --- |
| Skill id | `16` |
| Vietnamese name | `Châm Diệp Trảm` source text id `133` |
| Source description | `Tỷ lệ thương tổn gia tăng khá cao.` |
| `aq.c[1][16]` | `[1,133,545,150,2,30,0,-1,-1,0]` |
| Element lane | `1`, Wood Lane |
| Power | `150` |
| PP max | `30` |
| Effect mode | `0`, direct damage only |
| Effect id | `-1`, none |
| Target side | `0`, enemy side |

Meaning:

```text
Châm Diệp Trảm is the higher-damage Wood direct attack.
It spends 1 PP, plays enemy-side actor action 21/state1, runs the normal
hit/crit/direct damage path, applies no buff/debuff, and has no q() post-effect.
```

## Source Animation Path

`effect.mid[16]`:

```text
[0,0,21,1,-1,-1,0]
```

Chunks:

| Chunk | Raw | Meaning |
| ---: | --- | --- |
| `0` | `[0,0,21,1,-1,-1,0]` | enemy-side actor action `u`, effect id `21`, state `1`, sprite `263` |

There is no normal P7 `speffect` chunk and no active queue body visual for this
skill.

## Rebuild Mapping

| Source behavior | Rebuild equivalent | Status |
| --- | --- | --- |
| P3 skill list contains skill `16` with PP `30/30` | `SourceBattleRuntime.prepareSkillMenu` from player skill ids | PORTED |
| Confirm skill `16` targets enemy slot `0` | P3 confirm -> P6 auto target -> P2/P7 | PORTED |
| P7 actor action | actor effect `21`, sprite `263`, state `1`, enemy side | PORTED |
| Direct damage | normal direct formula with `powerPercent=150` | PORTED |
| PP consumes once | `30 -> 29` | PORTED |
| Buff/debuff | none | PORTED |
| q() post-effect | none | PORTED |
| HP settle | enemy HP updates from same-run damage | PORTED |
| Exact original MIDP frame/pixel parity | no original-vs-rebuild frame compare in this slice | PENDING |

## One-Run Debug Result

Smoke suite:

```powershell
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill16_direct_timeline build\smoke\battle_skill16_direct_timeline
```

Result:

```text
PASS
Before:      HP 134/134 : 109/109, PP 30
Actor frame: HP 134/134 : 109/109, PP 29, actor 21/sprite263/state1
Damage:      -37, enemy HP display still 109/109
Settled:     enemy HP 72/109, PP 29
Crit:        false
Miss:        none
Debuff:      none
```

Trace facts from the same run:

```text
raw=24
powerPercent=150
damageBeforeModifiers=36
damageFrame=37
hit=true
critFlag=0
appliedDebuffId=-1
```

The exact first-hit damage can vary slightly because source-shaped damage jitter
still uses runtime RNG. The invariant is:

```text
damage is positive
PP consumes once
enemyHpSettled == enemyHpBefore - sameRunDamage
no buff/debuff/post-effect is applied
```

Frame checkpoints from the same run:

| Frame | PNG | Debug |
| --- | --- | --- |
| Before confirm | `rebuild_game/build/smoke/battle_skill16_direct_timeline/battle_skill16_direct_timeline_before.png` | P3, HP `134/134:109/109`, PP `30` |
| Actor action | `rebuild_game/build/smoke/battle_skill16_direct_timeline/battle_skill16_direct_timeline_actor_u21_start.png` | P7 actor visible, sprite `263`, state `1`, HP unchanged |
| Damage frame | `rebuild_game/build/smoke/battle_skill16_direct_timeline/battle_skill16_direct_timeline_damage_frame.png` | damage text `-37`, enemy HP display still `109/109` |
| HP settled | `rebuild_game/build/smoke/battle_skill16_direct_timeline/battle_skill16_direct_timeline_hp_settled.png` | enemy HP `72/109`, PP `29` |

Debug text:

```text
rebuild_game/build/smoke/battle_skill16_direct_timeline/battle_skill16_direct_timeline_debug.txt
```

## Verification

Passed in this slice:

```text
rebuild_game/build.ps1
--smoke-suite battle_skill16_direct_timeline
```

Full regression is recorded with the final status of this work item.

## Honest Status

Skill `16` is now source-shaped and smoke-covered for:

- source row and text ids;
- P7 effect row;
- enemy-side actor effect id/state;
- direct damage with `powerPercent=150`;
- PP consumption;
- same-run damage and HP settle;
- no buff/debuff/post-effect behavior.

Still `PORTED-PARTIAL` because exact original MIDP pixel/frame comparison is
not part of this slice.

## Next Roadmap Step

Continue Wood Lane in order with skill `17` / `Diệp chi ân huệ`.

That slice is not plain direct damage: it is medium Wood direct damage plus a
stronger `game.d.q()` heal using param `40`, so it should mirror skill `11`
but with skill row `17`, PP `30`, power `130`, and heal percent `40`.
