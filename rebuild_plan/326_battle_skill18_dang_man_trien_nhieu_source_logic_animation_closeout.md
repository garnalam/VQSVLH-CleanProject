# 326 - Battle Skill 18 / Dang man trien nhieu Source Logic Animation Closeout

Date: 2026-07-14

Status: CODED / SMOKE-COVERED / PORTED-PARTIAL.

This closes Wood Lane skill `18` after skill `17` in `325`. Scope is only
skill `18`. The Java change adds a focused smoke timeline; shared battle
runtime behavior was not changed.

## Source Identity

| Field | Value |
| --- | --- |
| Skill id | `18` |
| Vietnamese name | `Dang man trien nhieu` / `Đằng mạn triền nhiễu` source text id `135` |
| Source description | `Thuong ton tuong doi cao, cung dem Quan Quanh 3 hiep.` |
| `aq.c[1][18]` | `[1,135,547,150,3,15,2,2,-1,0]` |
| Element lane | `1`, Wood Lane |
| Power | `150` |
| PP max | `15` |
| Effect mode | `2`, target debuff producer |
| Effect id | `2`, `Quan Quanh` |
| Param | `-1`, implicit apply path |
| Target side | `0`, target enemy |

Meaning:

```text
Dang man trien nhieu is the stronger sibling of skill 12 / Dang Phuoc.
It spends 1 PP, plays one target-side actor action 21/state0, applies direct
damage at 150% power, and applies debuff id 2 / Quan Quanh for 3 turns.
It has no P7 special-effect chunk. The visible speffect6/AH8 belongs to the
later P12/P13 active queue visual for the Quan Quanh status.
```

## Source Logic

Skill row:

```text
aq.c[1][18] = [1,135,547,150,3,15,2,2,-1,0]
```

Debuff row:

```text
aq.c[7][2] = [313,324,3]
```

The important behavior is:

- direct damage uses `powerPercent = 150`;
- PP goes `15 -> 14`;
- target gets debuff id `2`;
- debuff duration comes from `aq.c[7][2][2] == 3`;
- debuff value is `0`;
- source skill recorded in the target debuff slot is `18`;
- `game.b.q(2)` is a no-op HP/stat tick, but shared duration ticking still decrements and expires it.

Gameplay meaning of `Quan Quanh` is already closed in `284`:

- blocks item, pet switch, and run while active;
- does not block skill/catch/shop commands;
- no HP damage tick;
- improves catch chance through its source multiplier path;
- changes normal target-defense formula downstream.

## Source Animation Path

`effect.mid[18]`:

```text
[0,0,21,0,-1,-1,0]
```

P7 chunks:

| Chunk | Raw | Meaning |
| ---: | --- | --- |
| `0` | `[0,0,21,0,-1,-1,0]` | target/enemy-side actor action `u`, effect id `21`, state `0`, sprite `263` |

No P7 special-effect chunk exists for skill `18`.

The status active-queue visual is from `bufDebuf.mid` for debuff id `2`:

```text
[0,21,0,0,1,6,0,0]
```

That means:

- first segment: actor action `21/state0`;
- second segment: `speffect6`;
- `speffect.mid[6]` resolves to AH type `8`.

## Rebuild Mapping

| Source behavior | Rebuild equivalent | Status |
| --- | --- | --- |
| P3 skill list contains skill `18` with PP `15/15` | `SourceBattleRuntime.prepareSkillMenu` from player skill ids | PORTED |
| Confirm skill `18` targets enemy slot `0` | P3 confirm -> P6 auto target -> P2/P7 | PORTED |
| P7 chunk 0 actor action | actor effect `21`, sprite `263`, state `0`, enemy side | PORTED |
| No P7 special chunk | smoke asserts no `battle P7 speffect skill=18` before damage | PORTED |
| Direct damage | `powerPercent=150`, source-shaped damage jitter | PORTED |
| PP consumes once | `15 -> 14` | PORTED |
| Debuff producer | enemy debuff id `2`, duration `3`, source skill `18` | PORTED |
| Status icon/duration | icon cell `3`, duration cell `137 -> 136` after tick | PORTED |
| P12 active queue visual | actor segment then `speffect6/AH8` | PORTED/PARTIAL |
| `game.b.q(2)` no-op tick | HP/stat unchanged, duration decrements | PORTED |
| Expiry | debuff clears after three ticks, icon removed | PORTED |
| Exact original MIDP frame/pixel parity | no original-vs-rebuild frame compare in this slice | PENDING |

## One-Run Debug Result

Smoke suite:

```powershell
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill18_dang_man_trien_nhieu_timeline build\smoke\battle_skill18_dang_man_trien_nhieu_timeline
```

Result:

```text
PASS
Before:      HP 134/134 : 109/109, PP 15
Actor frame: HP 134/134 : 109/109, PP 14, actor 21/sprite263/state0
P7 special:  none
Damage:      -35, enemy HP display still 109/109, debuff text Quan Quanh
Settled:     enemy HP 74/109, debuff2 duration 3
P12 visual:  active queue speffect6/AH8
Tick 1:      HP 74 -> 74, duration 3 -> 2
Expiry:      duration 0, status icon cleared
Crit:        false
Miss:        none
```

Trace facts from the same run:

```text
raw=24
powerPercent=150
damageBeforeModifiers=36
damageFrame=35
hit=true
critFlag=0
appliedDebuffId=2
sourceSkill=18
```

The exact damage number can vary slightly across runs because source-shaped
damage jitter still uses runtime RNG. The smoke invariant is:

```text
enemyHpSettled == enemyMaxHp - sameRunDamage
enemyDebuff2Duration == 3 after P7
enemyDebuff2Duration == 2 after one P12 tick
enemyDebuff2 clears after three total ticks
```

Frame checkpoints from the same run:

| Frame | PNG | Debug |
| --- | --- | --- |
| Before confirm | `rebuild_game/build/smoke/battle_skill18_dang_man_trien_nhieu_timeline/battle_skill18_dang_man_trien_nhieu_timeline_before.png` | P3, HP `134/134:109/109`, PP `15` |
| Actor action | `rebuild_game/build/smoke/battle_skill18_dang_man_trien_nhieu_timeline/battle_skill18_dang_man_trien_nhieu_timeline_actor_u21_start.png` | P7 actor visible, sprite `263`, state `0`, HP unchanged |
| Damage + debuff | `rebuild_game/build/smoke/battle_skill18_dang_man_trien_nhieu_timeline/battle_skill18_dang_man_trien_nhieu_timeline_damage_debuff_frame.png` | damage text `-35`, debuff text `Quan Quanh`, HP display still `109/109` |
| HP settled | `rebuild_game/build/smoke/battle_skill18_dang_man_trien_nhieu_timeline/battle_skill18_dang_man_trien_nhieu_timeline_hp_settled_debuff_active.png` | enemy HP `74/109`, icon `3`, duration cell `137` |
| Active queue visual | `rebuild_game/build/smoke/battle_skill18_dang_man_trien_nhieu_timeline/battle_skill18_dang_man_trien_nhieu_timeline_p12_body_visual_speffect6_type8.png` | P12 body visual uses `speffect6/AH8` |
| Tick no-op | `rebuild_game/build/smoke/battle_skill18_dang_man_trien_nhieu_timeline/battle_skill18_dang_man_trien_nhieu_timeline_tick_noop_duration2.png` | HP `74 -> 74`, duration cell `136` |
| Expired | `rebuild_game/build/smoke/battle_skill18_dang_man_trien_nhieu_timeline/battle_skill18_dang_man_trien_nhieu_timeline_expired.png` | debuff cleared, icon removed |

Debug text:

```text
rebuild_game/build/smoke/battle_skill18_dang_man_trien_nhieu_timeline/battle_skill18_dang_man_trien_nhieu_timeline_debug.txt
```

## Verification

Passed in this slice:

```text
rebuild_game/build.ps1
--smoke-suite battle_skill18_dang_man_trien_nhieu_timeline
```

Full regression is recorded with the final status of this work item.

## Honest Status

Skill `18` is now source-shaped and smoke-covered for:

- source row and text ids;
- P7 one-chunk effect row;
- actor effect id/state/side;
- direct damage with debuff id `2`;
- PP consumption;
- same-run HP settle;
- debuff icon/duration/sourceSkill;
- P12 active queue speffect6/AH8;
- no-op tick and expiry.

Still `PORTED-PARTIAL` because exact original MIDP pixel/frame comparison is
not part of this slice.

## Next Roadmap Step

Continue Wood Lane in order with skill `19` / `Quang hop hieu ung`.

That slice is the stronger sibling of skill `13`: direct damage plus debuff id
`3` (`Thuc Loai`) with delayed final damage. It should mirror skill `13`, but
must prove row `19`, PP `15`, power `150`, delayed parameter `200`, and its own
damage/debuff/active queue/final tick timeline.
