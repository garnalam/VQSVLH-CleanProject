# 325 - Battle Skill 17 / Diệp chi ân huệ Source Logic Animation Closeout

Date: 2026-07-14

Status: CODED / SMOKE-COVERED / PORTED-PARTIAL.

This closes Wood Lane skill `17` after skill `16` in `324`. Scope is only
skill `17`. The Java change adds a focused smoke timeline; shared battle
gameplay logic was not changed.

## Source Identity

| Field | Value |
| --- | --- |
| Skill id | `17` |
| Vietnamese name | `Diệp chi ân huệ` source text id `134` |
| Source description | `Thương tổn ở mức độ trung bình, cũng có thể khôi phục trị số sinh mạng nhất định.` |
| `aq.c[1][17]` | `[1,134,546,130,2,30,0,-1,40,0]` |
| Element lane | `1`, Wood Lane |
| Power | `130` |
| PP max | `30` |
| Effect mode | `0`, ordinary direct damage |
| Effect id | `-1`, no produced buff/debuff |
| Param | `40`, post-skill heal percent |
| Target side | `0`, target enemy |

Meaning:

```text
Diệp chi ân huệ is the stronger sibling of skill 11 / Quang Phản.
It spends 1 PP, plays target-side actor action 21/state1, plays an
attacker-side heal special effect, applies ordinary direct damage, then
game.d.q() heals the attacker by max(1, h.B() * 40 / 100).
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

For skill `17`, `aq.c[1][17][8] == 40`, so the heal formula is:

```text
heal = max(1, h.B() * 40 / 100)
```

In the current smoke setup, this resolves to `+9`.

## Source Animation Path

`effect.mid[17]`:

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
| P3 skill list contains skill `17` with PP `30/30` | `SourceBattleRuntime.prepareSkillMenu` from player skill ids | PORTED |
| Confirm skill `17` targets enemy slot `0` | P3 confirm -> P6 auto target -> P2/P7 | PORTED |
| P7 chunk 0 actor action | actor effect `21`, sprite `263`, state `1`, enemy side | PORTED |
| P7 chunk 1 special effect | `speffect10`, AH type `9`, player/attacker side | PORTED/PARTIAL |
| Direct damage | `powerPercent=130`, source-shaped damage jitter | PORTED |
| PP consumes once | `30 -> 29` | PORTED |
| `game.d.q()` heal | `+9` in smoke, HP `67 -> 76` | PORTED/PARTIAL |
| No buff/debuff | `appliedDebuffId=-1`, `buffId=-1` | PORTED |
| Exact original MIDP frame/pixel parity | no original-vs-rebuild frame compare in this slice | PENDING |

## One-Run Debug Result

Smoke suite:

```powershell
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill17_diep_chi_an_hue_timeline build\smoke\battle_skill17_diep_chi_an_hue_timeline
```

Result:

```text
PASS
Before:      HP 67/134 : 109/109, PP 30
Actor frame: HP 67/134 : 109/109, PP 29, actor 21/sprite263/state1
Special:     speffect10/AH9 on attacker/player side
Damage:      -30, enemy HP display still 109/109
Settled:     enemy HP 79/109
Post-heal:   +9, player HP 67 -> 76
Crit:        false
Miss:        none
Debuff:      none
```

Trace facts from the same run:

```text
raw=24
powerPercent=130
damageBeforeModifiers=31
damageFrame=30
hit=true
critFlag=0
appliedDebuffId=-1
q() heal=9
```

The exact damage number can vary slightly across runs because source-shaped
damage jitter still uses runtime RNG. The smoke invariant is:

```text
enemyHpSettled == enemyMaxHp - sameRunDamage
playerHpAfterHeal == min(maxHp, playerHpBefore + displayedHeal)
displayedHeal is produced by q() skill17 param 40
```

Frame checkpoints from the same run:

| Frame | PNG | Debug |
| --- | --- | --- |
| Before confirm | `rebuild_game/build/smoke/battle_skill17_diep_chi_an_hue_timeline/battle_skill17_diep_chi_an_hue_timeline_before.png` | P3, HP `67/134:109/109`, PP `30` |
| Actor action | `rebuild_game/build/smoke/battle_skill17_diep_chi_an_hue_timeline/battle_skill17_diep_chi_an_hue_timeline_actor_u21_start.png` | P7 actor visible, sprite `263`, state `1`, HP unchanged |
| Heal special | `rebuild_game/build/smoke/battle_skill17_diep_chi_an_hue_timeline/battle_skill17_diep_chi_an_hue_timeline_speffect10_type9.png` | `speffect10/AH9` visible on player side |
| Damage frame | `rebuild_game/build/smoke/battle_skill17_diep_chi_an_hue_timeline/battle_skill17_diep_chi_an_hue_timeline_damage_frame.png` | damage text `-30`, enemy HP display still `109/109` |
| HP settled | `rebuild_game/build/smoke/battle_skill17_diep_chi_an_hue_timeline/battle_skill17_diep_chi_an_hue_timeline_hp_settled.png` | enemy HP display `79/109` |
| Post-heal | `rebuild_game/build/smoke/battle_skill17_diep_chi_an_hue_timeline/battle_skill17_diep_chi_an_hue_timeline_post_heal.png` | `+9`, player HP `76/134` |

Debug text:

```text
rebuild_game/build/smoke/battle_skill17_diep_chi_an_hue_timeline/battle_skill17_diep_chi_an_hue_timeline_debug.txt
```

## Verification

Passed in this slice:

```text
rebuild_game/build.ps1
--smoke-suite battle_skill17_diep_chi_an_hue_timeline
```

Full regression is recorded with the final status of this work item.

## Honest Status

Skill `17` is now source-shaped and smoke-covered for:

- source row and text ids;
- two-chunk P7 effect row;
- actor effect id/state/side;
- AH type `9` heal-special side/row;
- direct damage with no buff/debuff;
- PP consumption;
- same-run HP settle;
- `game.d.q()` heal text and HP increase using param `40`.

Still `PORTED-PARTIAL` because exact original MIDP pixel/frame comparison and
full original multi-floating-text concurrency are not part of this slice.

## Next Roadmap Step

Continue Wood Lane in order with skill `18` / `Đằng mạn triền nhiễu`.

That slice is the stronger direct damage plus debuff id `2` (`Quấn Quanh`) path,
so it should mirror skill `12` but with skill row `18`, PP `15`, power `200`,
and its own damage/debuff/active queue smoke timeline.
