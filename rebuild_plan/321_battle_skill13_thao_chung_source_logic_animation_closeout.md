# 321 - Battle Skill 13 / Thảo Chủng Source Logic Animation Closeout

Date: 2026-07-14

Status: CODED / SMOKE-COVERED / PORTED-PARTIAL.

This closes Wood Lane skill `13` after skill `12` in `320`. Scope is only
skill `13`. The Java change adds a focused smoke timeline; shared battle
gameplay logic was not changed.

## Source Identity

| Field | Value |
| --- | --- |
| Skill id | `13` |
| Vietnamese name | `Thảo Chủng` source text id `130` |
| Source description | `Thương tổn thấp, kẻ địch rơi vào trạng thái Thực Loại, sau 2 hiệp tạo thành thương tổn tương đối cao.` |
| `aq.c[1][13]` | `[1,130,542,50,1,30,2,3,150,0]` |
| Element lane | `1`, Wood Lane |
| Power | `50` |
| PP max | `30` |
| Effect mode | `2`, direct damage plus implicit debuff |
| Effect id | `3`, debuff id `3` / `Thực Loại` |
| Delayed-damage param | `150`, final delayed damage is `150%` of stored raw |
| Target side | `0`, target enemy |

Meaning:

```text
Thảo Chủng is a low-damage Wood Lane seed/bomb attack.
It spends 1 PP, plays target-side actor action 21/state0, applies immediate
direct damage, and applies debuff3 Thực Loại.

Thực Loại does not damage on its first two ticks. On the final tick, it deals:

max(1, storedRaw * 150 / 100)

where storedRaw is the pre-skill raw damage saved when the status is applied.
```

## Source Logic

Debuff row:

```text
aq.c[7][3] = [314,325,3]
```

Producer source facts from earlier debuff3 audit/closeout:

```text
game.b.b(target) applies debuff id 3.
w[3][0] = duration = 3
w[3][1] = stored pre-skill raw damage
w[3][3] = source skill id, here 13
game.b.q(3) skips HP damage while duration > 1
final tick uses aq.c[1][sourceSkill][8]
skill13 final percent = 150
```

Numerically, in the focused smoke run:

```text
storedRaw = 24
finalDelayedDamage = max(1, 24 * 150 / 100) = 36
```

## Source Animation Path

`effect.mid[13]`:

```text
[0,0,21,0,-1,-1,0]
```

Chunks:

| Chunk | Raw | Meaning |
| ---: | --- | --- |
| `0` | `[0,0,21,0,-1,-1,0]` | target/enemy-side actor action `u`, effect id `21`, state `0`, sprite `263` |

Active queue body visual for `Thực Loại`:

```text
bufDebuf ar[1][3] = [0,21,0,-1]
```

That means the later P12/P13 body visual is actor/source effect `21` only. It
does not use a follow-up `speffect` row like `Đằng Phược` does.

## Rebuild Mapping

| Source behavior | Rebuild equivalent | Status |
| --- | --- | --- |
| P3 skill list contains skill `13` with PP `30/30` | `SourceBattleRuntime.prepareSkillMenu` from player skill ids | PORTED |
| Confirm skill `13` targets enemy slot `0` | P3 confirm -> P6 auto target -> P2/P7 | PORTED |
| P7 actor action | actor effect `21`, sprite `263`, state `0`, enemy side | PORTED |
| Direct damage | `powerPercent=50`, source-shaped damage jitter | PORTED |
| PP consumes once | `30 -> 29` | PORTED |
| Debuff apply | `debuff3`, duration `3`, source skill `13`, stored raw positive | PORTED |
| Status icon/duration | enemy icon `4`, duration cell `137`; then `136`, `135`, clear | PORTED |
| Tick 1 | HP unchanged, duration `3 -> 2` | PORTED |
| Tick 2 | HP unchanged, duration `2 -> 1` | PORTED |
| Final tick | damage `storedRaw * 150 / 100`, clears status | PORTED |
| Controlled final tick visual | floating text `-36` from active queue final tick | PORTED |
| Exact original MIDP frame/pixel parity | no original-vs-rebuild frame compare in this slice | PENDING |

## One-Run Debug Result

Smoke suite:

```powershell
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill13_thao_chung_timeline build\smoke\battle_skill13_thao_chung_timeline
```

Result:

```text
PASS
Before:      HP 134/134 : 109/109, PP 30
Actor frame: HP 134/134 : 109/109, PP 29, actor 21/sprite263/state0
Damage:      -13, enemy HP display still 109/109, debuff text Thực Loại
Settled:     enemy HP 96/109, debuff3 active, icon 4, duration cell 137
Tick 1:      HP 96 -> 96, duration 3 -> 2, duration cell 136
Tick 2:      HP 96 -> 96, duration 2 -> 1, duration cell 135
Final tick:  HP 96 -> 60, delayed damage 36, status clears
Controlled visual: HP 80 -> 44, floating text -36
Crit:        false
Miss:        none
```

The exact first-hit damage can vary slightly because source-shaped damage jitter
still uses runtime RNG. The invariant is:

```text
enemyHpSettled == enemyHpBefore - sameRunDamage
storedRaw is preserved from application
finalDelayedDamage == max(1, storedRaw * 150 / 100)
debuff3 clears after final delayed damage
```

Frame checkpoints from the same run:

| Frame | PNG | Debug |
| --- | --- | --- |
| Before confirm | `rebuild_game/build/smoke/battle_skill13_thao_chung_timeline/battle_skill13_thao_chung_timeline_before.png` | P3, HP `134/134:109/109`, PP `30` |
| Actor action | `rebuild_game/build/smoke/battle_skill13_thao_chung_timeline/battle_skill13_thao_chung_timeline_actor_u21_start.png` | P7 actor visible, sprite `263`, state `0`, HP unchanged |
| Damage/debuff frame | `rebuild_game/build/smoke/battle_skill13_thao_chung_timeline/battle_skill13_thao_chung_timeline_damage_debuff_frame.png` | `-13`, debuff text `Thực Loại`, enemy HP display still `109/109` |
| HP settled | `rebuild_game/build/smoke/battle_skill13_thao_chung_timeline/battle_skill13_thao_chung_timeline_hp_settled_debuff_active.png` | enemy HP `96/109`, icon `4/137` |
| P12 body visual | `rebuild_game/build/smoke/battle_skill13_thao_chung_timeline/battle_skill13_thao_chung_timeline_p12_body_visual_actor21.png` | active queue actor visual `21`, no speffect segment |
| Tick 1 no damage | `rebuild_game/build/smoke/battle_skill13_thao_chung_timeline/battle_skill13_thao_chung_timeline_tick1_no_damage_duration2.png` | HP unchanged, duration cell `136` |
| Tick 2 no damage | `rebuild_game/build/smoke/battle_skill13_thao_chung_timeline/battle_skill13_thao_chung_timeline_tick2_no_damage_duration1.png` | HP unchanged, duration cell `135` |
| Same-run final delayed damage | `rebuild_game/build/smoke/battle_skill13_thao_chung_timeline/battle_skill13_thao_chung_timeline_final_delayed_damage_expired.png` | HP `96 -> 60`, icon cleared |
| Controlled final tick visual | `rebuild_game/build/smoke/battle_skill13_thao_chung_timeline/battle_skill13_thao_chung_timeline_controlled_final_tick_visual.png` | floating text `-36`, HP `80 -> 44` |

Debug text:

```text
rebuild_game/build/smoke/battle_skill13_thao_chung_timeline/battle_skill13_thao_chung_timeline_debug.txt
```

## Verification

Passed in this slice:

```text
rebuild_game/build.ps1
--smoke-suite battle_skill13_thao_chung_timeline
```

Full regression is recorded with the final status of this work item.

## Honest Status

Skill `13` is now source-shaped and smoke-covered for:

- source row and text ids;
- debuff row `aq.c[7][3]`;
- P7 effect row;
- actor effect id/state/side;
- direct damage plus debuff3 apply;
- PP consumption;
- same-run HP settle;
- stored raw delayed-damage value;
- status icon/duration;
- first two no-damage ticks;
- final `150%` delayed damage and status clear;
- controlled final tick visual text.

Still `PORTED-PARTIAL` because exact original MIDP pixel/frame comparison is
not part of this slice.

## Next Roadmap Step

Continue Wood Lane in order with skill `14`.

That slice is not a normal direct-damage skill. It should be audited
source-first as the `Đằng chi bích lũy` / buff2 producer path: no direct damage,
defense increase, reflect hook, icon/duration, P12/P13 lifecycle, and expiry.
