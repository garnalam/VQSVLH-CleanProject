# 319 - Battle Skill 11 / Quang Phan Source Logic Animation Closeout

Date: 2026-07-14

Status: CODED / SMOKE-COVERED / PORTED-PARTIAL.

This closes Wood Lane skill `11` after skill `10` in `318`. Scope is only
skill `11`. The Java change adds a focused smoke timeline; normal battle
gameplay logic was not changed.

## Source Identity

| Field | Value |
| --- | --- |
| Skill id | `11` |
| Vietnamese name | `Quang phản` |
| Source description | `Thương tổn thấp, có thể khôi phục trị số sinh mạng nhất định.` |
| `aq.c[1][11]` | `[1,128,540,90,0,45,0,-1,10,0]` |
| Element lane | `1`, Wood Lane |
| Power | `90` |
| PP max | `45` |
| Effect mode | `0`, ordinary direct damage |
| Effect id | `-1`, no produced buff/debuff |
| Param | `10`, post-skill heal percent |
| Target side | `0`, target enemy |

Meaning:

```text
Quang Phan is a low-damage Wood Lane attack.
It spends 1 PP, plays target-side actor action 21/state1, plays an attacker-side
heal special effect, applies ordinary direct damage, then game.d.q() heals the
attacker by max(1, h.B() * 10 / 100).
It does not apply buff/debuff.
```

## Source Logic

`game.d.q()` source branch:

```text
case 11:
case 17:
    n3 = h.B() * aq.c[1][skill][8] / 100
    if n3 <= 0: n3 = 1
    h.l(n3)
    show "+n3" over attacker
```

For skill `11`, `aq.c[1][11][8] == 10`, so the heal formula is:

```text
heal = max(1, h.B() * 10 / 100)
```

In the current smoke setup, this resolves to `+2`.

## Source Animation Path

`effect.mid[11]`:

```text
[0,0,21,1,-1,-1,0, 1,1,10,0,-1,-1,0]
```

Chunks:

| Chunk | Raw | Meaning |
| ---: | --- | --- |
| `0` | `[0,0,21,1,-1,-1,0]` | target/enemy-side actor action `u`, effect id `21`, state `1`, sprite `263` |
| `1` | `[1,1,10,0,-1,-1,0]` | attacker/player-side special effect, `speffect10`, AH type `9` |

`speffect.mid[10]`:

```text
[9,120,218,217,169,0,4,2]
```

Interpretation:

- AH type `9`.
- Attacker-side overlay.
- Alpha/color transform row `[120,218,217,169]`.
- Duration/interval source-shaped from row tail `[4,2]`.

## Rebuild Mapping

| Source behavior | Rebuild equivalent | Status |
| --- | --- | --- |
| P3 skill list contains skill `11` with PP `45/45` | `SourceBattleRuntime.prepareSkillMenu` from player skill ids | PORTED |
| Confirm skill `11` targets enemy slot `0` | P3 confirm -> P6 auto target -> P2/P7 | PORTED |
| P7 chunk 0 actor action | actor effect `21`, sprite `263`, state `1`, enemy side | PORTED |
| P7 chunk 1 special effect | `speffect10`, AH type `9`, player/attacker side | PORTED/PARTIAL |
| Direct damage | `powerPercent=90`, source-shaped damage jitter | PORTED |
| PP consumes once | `45 -> 44` | PORTED |
| `game.d.q()` heal | `+2` in smoke, HP `67 -> 69` | PORTED/PARTIAL |
| No buff/debuff | `appliedDebuffId=-1`, `buffId=-1` | PORTED |
| Exact original MIDP frame/pixel parity | no original-vs-rebuild frame compare in this slice | PENDING |

## One-Run Debug Result

Smoke suite:

```powershell
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill11_quang_phan_timeline build\smoke\battle_skill11_quang_phan_timeline
```

Result:

```text
PASS
Before:      HP 67/134 : 109/109, PP 45
Actor frame: HP 67/134 : 109/109, PP 44, actor 21/sprite263/state1
Special:     speffect10/AH9 on attacker/player side
Damage:      -20, enemy HP display still 109/109
Settled:     enemy HP 89/109
Post-heal:   +2, player HP 67 -> 69
Crit:        false
Miss:        none
Debuff:      none
```

The exact damage number can vary slightly across runs because source-shaped
damage jitter still uses runtime RNG. The smoke invariant is:

```text
enemyHpSettled == enemyMaxHp - sameRunDamage
playerHpAfterHeal == min(maxHp, playerHpBefore + displayedHeal)
```

Frame checkpoints from the same run:

| Frame | PNG | Debug |
| --- | --- | --- |
| Before confirm | `rebuild_game/build/smoke/battle_skill11_quang_phan_timeline/battle_skill11_quang_phan_timeline_before.png` | P3, HP `67/134:109/109`, PP `45` |
| Actor action | `rebuild_game/build/smoke/battle_skill11_quang_phan_timeline/battle_skill11_quang_phan_timeline_actor_u21_start.png` | P7 actor visible, sprite `263`, state `1`, HP unchanged |
| Heal special | `rebuild_game/build/smoke/battle_skill11_quang_phan_timeline/battle_skill11_quang_phan_timeline_speffect10_type9.png` | `speffect10/AH9` visible on player side |
| Damage frame | `rebuild_game/build/smoke/battle_skill11_quang_phan_timeline/battle_skill11_quang_phan_timeline_damage_frame.png` | latest run damage text `-20`, enemy HP display still `109/109` |
| HP settled | `rebuild_game/build/smoke/battle_skill11_quang_phan_timeline/battle_skill11_quang_phan_timeline_hp_settled.png` | latest run enemy HP display `89/109` |
| Post-heal | `rebuild_game/build/smoke/battle_skill11_quang_phan_timeline/battle_skill11_quang_phan_timeline_post_heal.png` | `+2`, player HP `69/134` |

Debug text:

```text
rebuild_game/build/smoke/battle_skill11_quang_phan_timeline/battle_skill11_quang_phan_timeline_debug.txt
```

## Verification

Passed in this slice:

```text
rebuild_game/build.ps1
--smoke-suite battle_skill11_quang_phan_timeline
```

Full regression is recorded with the final status of this work item.

## Honest Status

Skill `11` is now source-shaped and smoke-covered for:

- source row and text ids;
- two-chunk P7 effect row;
- actor effect id/state/side;
- AH type `9` heal-special side/row;
- direct damage with no buff/debuff;
- PP consumption;
- same-run HP settle;
- `game.d.q()` heal text and HP increase.

Still `PORTED-PARTIAL` because exact original MIDP pixel/frame comparison and
full original multi-floating-text concurrency are not part of this slice.

## Next Roadmap Step

Continue Wood Lane in order with skill `12` / `Đằng Phược`.

That slice is direct damage plus debuff id `2` (`Quấn Quanh`) and `speffect6/AH8`.
It must prove animation, damage, debuff apply, icon/duration, and the source
lock behavior already ported earlier; do not treat it as a plain damage skill.
