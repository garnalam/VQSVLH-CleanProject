# 322 - Battle Skill 14 / Đằng chi bích lũy Source Logic Animation Closeout

Date: 2026-07-14

Status: CODED / SMOKE-COVERED / PORTED-PARTIAL.

This closes Wood Lane skill `14` after skill `13` in `321`. Scope is only
skill `14`. The Java change adds a focused smoke timeline; shared gameplay
logic was not changed.

## Source Identity

| Field | Value |
| --- | --- |
| Skill id | `14` |
| Vietnamese name | `Đằng chi bích lũy` source text id `131` |
| Source description | `Gia tăng khả năng phòng ngự 30% thương tổn cũng dội ngược trở lại, duy trì liên tục 3 hiệp.` |
| `aq.c[1][14]` | `[1,131,543,0,1,10,1,2,-1,1]` |
| Element lane | `1`, Wood Lane |
| Power | `0` |
| PP max | `10` |
| Effect mode | `1`, no-damage explicit buff/effect |
| Effect id | `2`, buff id `2` / `Kinh Cức` |
| Target side | `1`, self/player side |

Meaning:

```text
Đằng chi bích lũy is a self-buff skill.
It spends 1 PP, plays player-side actor action 21/state1, does not run hitroll,
does not deal direct damage, then applies buff2 Kinh Cức.

Kinh Cức lasts 3 turns, raises defense by 30%, and reflects 10% of an incoming
hit's final damage back to the attacker. Missed hits do not reflect.
```

## Source Logic

Buff row:

```text
aq.c[6][2] = [335,350,3,30,10]
```

Source-shaped interpretation already proved in the buff2 status closeout:

```text
duration = 3
defense delta = baseDefense * 30 / 100
reflect percent = 10
reflect damage = incomingDamage * 10 / 100
```

The focused skill smoke proves that skill `14` is the producer path for this
buff, not a direct damage attack.

## Source Animation Path

`effect.mid[14]`:

```text
[0,0,21,1,-1,-1,0]
```

Chunks:

| Chunk | Raw | Meaning |
| ---: | --- | --- |
| `0` | `[0,0,21,1,-1,-1,0]` | player-side actor action `u`, effect id `21`, state `1`, sprite `263` |

There is no normal P7 `speffect` chunk for this skill, and no damage/blood
frame because `aq.c[1][14][3] == 0`.

## Rebuild Mapping

| Source behavior | Rebuild equivalent | Status |
| --- | --- | --- |
| P3 skill list contains skill `14` with PP `10/10` | `SourceBattleRuntime.prepareSkillMenu` from player skill ids | PORTED |
| Confirm skill `14` targets self/player slot | P3 confirm -> P6 auto target -> P2/P7 | PORTED |
| P7 actor action | actor effect `21`, sprite `263`, state `1`, player side | PORTED |
| Direct damage | skipped; no hitroll, no damage frame, no blood | PORTED |
| PP consumes once | `10 -> 9` | PORTED |
| Buff apply | player buff2, value `30`, secondary `10`, duration `3` | PORTED |
| Defense stat | base `100`, current `130` while active | PORTED |
| Status icon/duration | player icon `14`, duration cell `137`; then `136`, `135`, clear | PORTED |
| Forced hit interaction | target buff2 reflects `incomingDamage * 10 / 100` | PORTED |
| Forced miss interaction | no HP damage, no reflect | PORTED |
| Forced crit interaction | reflect is based on crit damage, not baseline damage | PORTED |
| Expiry | after third tick, defense returns to base and icon clears | PORTED |
| Exact original MIDP frame/pixel parity | no original-vs-rebuild frame compare in this slice | PENDING |

## One-Run Debug Result

Smoke suite:

```powershell
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill14_dang_chi_bich_luy_timeline build\smoke\battle_skill14_dang_chi_bich_luy_timeline
```

Result:

```text
PASS
Before:       HP 134/134 : 109/109, PP 10, defense 100
Actor frame:  HP unchanged, PP 9, actor 21/sprite263/state1, player side
After apply:  HP unchanged, no damage frame, Kinh Cức text, buff2 active
Buff stat:    defense 100 -> 130, value 30, reflect percent 10, duration 3
Forced hit:   incoming damage 68, reflected damage 6, player HP 134 -> 128
Forced miss:  Né tránh, no HP damage, no reflect
Forced crit:  crit damage 102, reflected damage 10, player HP 134 -> 124
Expiry:       duration 3 -> 2 -> 1 -> 0, defense 130 -> 100, icon clears
```

Frame checkpoints from the same run:

| Frame | PNG | Debug |
| --- | --- | --- |
| Before confirm | `rebuild_game/build/smoke/battle_skill14_dang_chi_bich_luy_timeline/battle_skill14_dang_chi_bich_luy_timeline_before.png` | P3, HP `134/134:109/109`, PP `10`, defense `100` |
| Actor action | `rebuild_game/build/smoke/battle_skill14_dang_chi_bich_luy_timeline/battle_skill14_dang_chi_bich_luy_timeline_actor_u21_state1.png` | P7 actor visible, sprite `263`, state `1`, player side |
| Buff applied | `rebuild_game/build/smoke/battle_skill14_dang_chi_bich_luy_timeline/battle_skill14_dang_chi_bich_luy_timeline_after_apply_icon.png` | post text `Kinh Cức`, icon `14/137`, defense `130` |
| Forced hit reflect | `rebuild_game/build/smoke/battle_skill14_dang_chi_bich_luy_timeline/battle_skill14_dang_chi_bich_luy_timeline_forced_hit_reflect.png` | damage `68`, reflect `6` |
| Forced miss no reflect | `rebuild_game/build/smoke/battle_skill14_dang_chi_bich_luy_timeline/battle_skill14_dang_chi_bich_luy_timeline_forced_miss_no_reflect.png` | miss text, no reflect |
| Forced crit reflect | `rebuild_game/build/smoke/battle_skill14_dang_chi_bich_luy_timeline/battle_skill14_dang_chi_bich_luy_timeline_forced_crit_reflect.png` | crit damage `102`, reflect `10` |
| Expiry duration 2 | `rebuild_game/build/smoke/battle_skill14_dang_chi_bich_luy_timeline/battle_skill14_dang_chi_bich_luy_timeline_expiry_duration2.png` | icon `14/136`, defense still `130` |
| Expiry duration 1 | `rebuild_game/build/smoke/battle_skill14_dang_chi_bich_luy_timeline/battle_skill14_dang_chi_bich_luy_timeline_expiry_duration1.png` | icon `14/135`, defense still `130` |
| Expired | `rebuild_game/build/smoke/battle_skill14_dang_chi_bich_luy_timeline/battle_skill14_dang_chi_bich_luy_timeline_expired.png` | icon cleared, defense `100` |

Debug text:

```text
rebuild_game/build/smoke/battle_skill14_dang_chi_bich_luy_timeline/battle_skill14_dang_chi_bich_luy_timeline_debug.txt
```

## Verification

Passed in this slice:

```text
rebuild_game/build.ps1
--smoke-suite battle_skill14_dang_chi_bich_luy_timeline
```

Full regression is recorded with the final status of this work item.

## Honest Status

Skill `14` is now source-shaped and smoke-covered for:

- source row and text ids;
- buff row `aq.c[6][2]`;
- P7 effect row;
- player-side actor effect id/state;
- no-damage/no-hitroll behavior;
- PP consumption;
- buff2 apply;
- defense `+30%`;
- reflect `10%`;
- forced hit/miss/crit interaction;
- status icon/duration lifecycle;
- expiry stat restore.

Still `PORTED-PARTIAL` because exact original MIDP pixel/frame comparison is
not part of this slice.

## Next Roadmap Step

Continue Wood Lane in order with skill `15` / `Thảo nguyên thuật`.

That slice is another no-damage self-buff, but it is not the same as skill `14`:
it applies buff3 `Khôi phục`, heals `maxHP * 5 / 100` on apply and each tick,
and uses the more complex actor/effect path already seen in the buff3 status
coverage. It must still get its own per-skill closeout.
