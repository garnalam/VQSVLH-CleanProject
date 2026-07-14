# 323 - Battle Skill 15 / Thảo nguyên thuật Source Logic Animation Closeout

Date: 2026-07-14

Status: CODED / SMOKE-COVERED / PORTED-PARTIAL.

This closes Wood Lane skill `15` after skill `14` in `322`. Scope is only
skill `15`. The Java change adds a focused smoke timeline; shared gameplay
logic was not changed.

## Source Identity

| Field | Value |
| --- | --- |
| Skill id | `15` |
| Vietnamese name | `Thảo nguyên thuật` source text id `132` |
| Source description | `Mỗi hiệp khôi phục trị số sinh mạng nhất định, duy trì liên tục 3 hiệp.` |
| `aq.c[1][15]` | `[1,132,544,0,1,10,1,3,-1,1]` |
| Element lane | `1`, Wood Lane |
| Power | `0` |
| PP max | `10` |
| Effect mode | `1`, no-damage explicit buff/effect |
| Effect id | `3`, buff id `3` / `Khôi phục` |
| Target side | `1`, self/player side |

Meaning:

```text
Thảo nguyên thuật is a self-buff heal-over-time skill.
It spends 1 PP, plays player-side actor action 33/state0, advances through the
source frame-trigger into speffect7/AH9, does not run hitroll, does not deal
direct damage, then applies buff3 Khôi phục.

Khôi phục heals maxHP * 5 / 100 when applied and again on each active queue tick.
The buff lasts 3 turns and uses status icon cell 15.
```

## Source Logic

Buff row:

```text
aq.c[6][3] = [336,351,3,5,-1]
```

Source-shaped interpretation already proved in the buff3 status closeout:

```text
duration = 3
heal amount = maxHP * 5 / 100
apply heal happens through game.d.q()
active tick heal happens through game.b.o(3) / P12-P13 active queue
```

In the focused smoke, player max HP is `134`, so:

```text
heal = 134 * 5 / 100 = 6
```

## Source Animation Path

`effect.mid[15]`:

```text
[0,0,33,0,0,-1,0, 0,1,7,0,-1,-1,0]
```

Chunks:

| Chunk | Raw | Meaning |
| ---: | --- | --- |
| `0` | `[0,0,33,0,0,-1,0]` | player-side actor action `u`, effect id `33`, state `0`, sprite `308`; chunk `[4] == 0` is the frame trigger |
| `1` | `[0,1,7,0,-1,-1,0]` | player-side special effect `H`, speffect id `7` |

`speffect.mid[7]`:

```text
[9,120,218,217,169,0,9,9]
```

This is an AH type `9` overlay. The runtime already had the source-shaped
frame-trigger slice for this skill; this closeout ties it to the real skill
producer, heal logic, icon lifecycle, and active tick.

## Rebuild Mapping

| Source behavior | Rebuild equivalent | Status |
| --- | --- | --- |
| P3 skill list contains skill `15` with PP `10/10` | `SourceBattleRuntime.prepareSkillMenu` from player skill ids | PORTED |
| Confirm skill `15` targets self/player slot | P3 confirm -> P6 auto target -> P2/P7 | PORTED |
| P7 actor action | actor effect `33`, sprite `308`, state `0`, player side | PORTED |
| Chunk `[4] == 0` frame trigger | actor cursor `0` advances to chunk1 | PORTED/PARTIAL |
| P7 special effect | `speffect7`, AH type `9`, player side | PORTED/PARTIAL |
| Direct damage | skipped; no hitroll, no damage frame, no blood | PORTED |
| PP consumes once | `10 -> 9` | PORTED |
| Buff apply | player buff3, value `6`, duration `3` | PORTED |
| Apply heal | HP `67 -> 73`, text `+6` | PORTED |
| Status icon/duration | player icon `15`, duration cell `137`; then `136`, `135`, clear | PORTED |
| Active queue visual | P13 active queue for buff3 starts before tick heal | PORTED/PARTIAL |
| Active tick heal | after enemy turn HP `66 -> 72`, text `+6`, duration `3 -> 2` | PORTED |
| Expiry | controlled ticks heal `67->73->79->85->91`, then clear status | PORTED |
| Exact original MIDP frame/pixel parity | no original-vs-rebuild frame compare in this slice | PENDING |

## One-Run Debug Result

Smoke suite:

```powershell
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill15_thao_nguyen_thuat_timeline build\smoke\battle_skill15_thao_nguyen_thuat_timeline
```

Result:

```text
PASS
Before:       HP 67/134 : 109/109, PP 10
Actor frame:  HP unchanged, PP 9, actor 33/sprite308/state0, player side
Speffect:     speffect7/AH9, HP still unchanged
After apply:  HP 67 -> 73, text +6, buff3 active, icon 15/137
Enemy turn:   enemy skill40 hits player before active queue tick, HP 73 -> 66
Tick 1:       active queue heals HP 66 -> 72, text +6, duration 3 -> 2
Expiry probe: HP 67 -> 73 -> 79 -> 85 -> 91, duration clears to 0
Crit:         none for skill15 because no hitroll/damage
Miss:         none for skill15 because no hitroll/damage
```

Frame checkpoints from the same run:

| Frame | PNG | Debug |
| --- | --- | --- |
| Before confirm | `rebuild_game/build/smoke/battle_skill15_thao_nguyen_thuat_timeline/battle_skill15_thao_nguyen_thuat_timeline_before.png` | P3, HP `67/134:109/109`, PP `10` |
| Actor action | `rebuild_game/build/smoke/battle_skill15_thao_nguyen_thuat_timeline/battle_skill15_thao_nguyen_thuat_timeline_actor_u33_start.png` | P7 actor visible, sprite `308`, state `0`, player side |
| Speffect | `rebuild_game/build/smoke/battle_skill15_thao_nguyen_thuat_timeline/battle_skill15_thao_nguyen_thuat_timeline_speffect7_ah9.png` | AH type `9`, source speffect row `7` |
| Buff applied | `rebuild_game/build/smoke/battle_skill15_thao_nguyen_thuat_timeline/battle_skill15_thao_nguyen_thuat_timeline_after_apply_heal_icon.png` | HP `67 -> 73`, post text `+6`, icon `15/137` |
| P13 visual start | `rebuild_game/build/smoke/battle_skill15_thao_nguyen_thuat_timeline/battle_skill15_thao_nguyen_thuat_timeline_p12_body_visual_start.png` | active queue start for player buff3 |
| Tick heal | `rebuild_game/build/smoke/battle_skill15_thao_nguyen_thuat_timeline/battle_skill15_thao_nguyen_thuat_timeline_tick_heal_duration2.png` | HP `66 -> 72`, icon `15/136` |
| Expiry duration 2 | `rebuild_game/build/smoke/battle_skill15_thao_nguyen_thuat_timeline/battle_skill15_thao_nguyen_thuat_timeline_expiry_duration2.png` | controlled expiry, icon `15/136` |
| Expiry duration 1 | `rebuild_game/build/smoke/battle_skill15_thao_nguyen_thuat_timeline/battle_skill15_thao_nguyen_thuat_timeline_expiry_duration1.png` | controlled expiry, icon `15/135` |
| Expired | `rebuild_game/build/smoke/battle_skill15_thao_nguyen_thuat_timeline/battle_skill15_thao_nguyen_thuat_timeline_expired.png` | icon cleared, status count `0` |

Debug text:

```text
rebuild_game/build/smoke/battle_skill15_thao_nguyen_thuat_timeline/battle_skill15_thao_nguyen_thuat_timeline_debug.txt
```

## Verification

Passed in this slice:

```text
rebuild_game/build.ps1
--smoke-suite battle_skill15_thao_nguyen_thuat_timeline
```

Full regression is recorded with the final status of this work item.

## Honest Status

Skill `15` is now source-shaped and smoke-covered for:

- source row and text ids;
- buff row `aq.c[6][3]`;
- P7 effect row and speffect row;
- player-side actor effect id/state;
- chunk `[4] == 0` frame-trigger into speffect7/AH9;
- no-damage/no-hitroll behavior;
- PP consumption;
- buff3 apply;
- `maxHP * 5 / 100` apply heal;
- active queue visual and tick heal;
- status icon/duration lifecycle;
- expiry clear.

Still `PORTED-PARTIAL` because exact original MIDP pixel/frame comparison is
not part of this slice.

## Next Roadmap Step

Continue Wood Lane in order with skill `16` / `Châm Diệp Trảm`.

That slice should be a higher-damage Wood direct skill: source row/effect row,
actor effect `21`, direct damage formula, before/actor/damage/settled PNGs, and
debug numbers. It should not be treated as another buff skill.
