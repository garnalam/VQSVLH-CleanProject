# 320 - Battle Skill 12 / Dang Phuoc Source Logic Animation Closeout

Date: 2026-07-14

Status: CODED / SMOKE-COVERED / PORTED-PARTIAL.

This closes Wood Lane skill `12` after skill `11` in `319`. Scope is only
skill `12`. The Java change adds a focused smoke timeline; shared battle
gameplay logic was not changed.

## Source Identity

| Field | Value |
| --- | --- |
| Skill id | `12` |
| Vietnamese name | `Đằng Phược` source text id `129` |
| Source meaning | Low Wood damage and applies `Quan Quanh` for 3 turns. |
| `aq.c[1][12]` | `[1,129,541,50,0,45,2,2,-1,0]` |
| Element lane | `1`, Wood Lane |
| Power | `50` |
| PP max | `45` |
| Effect mode | `2`, direct damage plus implicit debuff |
| Effect id | `2`, debuff id `2` / `Quan Quanh` |
| Param | `-1`, source treats this family as implicit/apply path |
| Target side | `0`, target enemy |

Meaning:

```text
Đằng Phược is a low-damage Wood Lane bind attack.
It spends 1 PP, plays target-side actor action 21/state0, then target-side
speffect6/AH8, applies ordinary direct damage, and applies debuff2 Quan Quanh.
Quan Quanh lasts 3 turns, has no HP tick, and blocks item/pet switch/run while
active. Skill/catch/shop remain allowed by earlier debuff2 closeout coverage.
```

## Source Logic

Debuff row:

```text
aq.c[7][2] = [313,324,3]
```

Interpreted behavior in the current rebuild, based on `284` and this skill
timeline:

```text
duration = 3
stored value = 0
source skill = 12
icon cell = 3
duration cell at apply = 137
tick behavior = no HP/stat delta, duration decrements
expiry = duration 0, icon cleared
```

The command-lock behavior for `Quan Quanh` is already source-smoked in
`284_battle_debuff2_quan_quanh_closeout.md`:

```text
item / pet switch / run -> locked while player-side debuff2 is active
skill / catch / shop -> allowed
```

## Source Animation Path

`effect.mid[12]`:

```text
[0,0,21,0,-1,-1,0, 0,1,6,0,-1,-1,0]
```

Chunks:

| Chunk | Raw | Meaning |
| ---: | --- | --- |
| `0` | `[0,0,21,0,-1,-1,0]` | target/enemy-side actor action `u`, effect id `21`, state `0`, sprite `263` |
| `1` | `[0,1,6,0,-1,-1,0]` | target/enemy-side special effect, `speffect6`, AH type `8` |

`speffect.mid[6]`:

```text
[8,0,10,1,5,1,10,0,0, 8,0,-5,10,0,0, 8,0,-5,10,0,0]
```

Interpretation:

- AH type `8`.
- Target-side overlay.
- Three type-8 segments in the decoded row.
- Exact original Java ME pixel/frame parity is still not claimed; this is
  source-shaped renderer parity.

## Rebuild Mapping

| Source behavior | Rebuild equivalent | Status |
| --- | --- | --- |
| P3 skill list contains skill `12` with PP `45/45` | `SourceBattleRuntime.prepareSkillMenu` from player skill ids | PORTED |
| Confirm skill `12` targets enemy slot `0` | P3 confirm -> P6 auto target -> P2/P7 | PORTED |
| P7 chunk 0 actor action | actor effect `21`, sprite `263`, state `0`, enemy side | PORTED |
| P7 chunk 1 special effect | `speffect6`, AH type `8`, enemy/target side | PORTED/PARTIAL |
| Direct damage | `powerPercent=50`, source-shaped damage jitter | PORTED |
| PP consumes once | `45 -> 44` | PORTED |
| Debuff apply | `debuff2`, duration `3`, value `0`, source skill `12` | PORTED |
| Status icon/duration | enemy icon `3`, duration cell `137`; after first tick duration cell `136` | PORTED |
| P12/P13 active tick | no HP/stat delta, duration `3 -> 2 -> 1 -> 0` | PORTED |
| Exact original MIDP frame/pixel parity | no original-vs-rebuild frame compare in this slice | PENDING |

## One-Run Debug Result

Smoke suite:

```powershell
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill12_dang_phuoc_timeline build\smoke\battle_skill12_dang_phuoc_timeline
```

Result:

```text
PASS
Before:      HP 134/134 : 109/109, PP 45
Actor frame: HP 134/134 : 109/109, PP 44, actor 21/sprite263/state0
Special:     speffect6/AH8 on enemy/target side
Damage:      -13, enemy HP display still 109/109, debuff text Quan Quanh
Settled:     enemy HP 96/109, debuff2 active, icon 3, duration cell 137
Tick 1:      HP 96 -> 96, duration 3 -> 2, duration cell 136
Expiry:      duration 1 -> 0, enemy status count 0
Crit:        false
Miss:        none
```

The exact damage number can vary slightly across runs because source-shaped
damage jitter still uses runtime RNG. The smoke invariant is:

```text
enemyHpSettled == enemyHpBefore - sameRunDamage
debuff2 duration starts at 3, does not change HP on tick, and clears at 0
```

Frame checkpoints from the same run:

| Frame | PNG | Debug |
| --- | --- | --- |
| Before confirm | `rebuild_game/build/smoke/battle_skill12_dang_phuoc_timeline/battle_skill12_dang_phuoc_timeline_before.png` | P3, HP `134/134:109/109`, PP `45` |
| Actor action | `rebuild_game/build/smoke/battle_skill12_dang_phuoc_timeline/battle_skill12_dang_phuoc_timeline_actor_u21_start.png` | P7 actor visible, sprite `263`, state `0`, HP unchanged |
| Target special | `rebuild_game/build/smoke/battle_skill12_dang_phuoc_timeline/battle_skill12_dang_phuoc_timeline_speffect6_type8.png` | `speffect6/AH8` visible on enemy side |
| Damage/debuff frame | `rebuild_game/build/smoke/battle_skill12_dang_phuoc_timeline/battle_skill12_dang_phuoc_timeline_damage_debuff_frame.png` | `-13`, debuff text `Quan Quanh`, enemy HP display still `109/109` |
| HP settled | `rebuild_game/build/smoke/battle_skill12_dang_phuoc_timeline/battle_skill12_dang_phuoc_timeline_hp_settled_debuff_active.png` | enemy HP `96/109`, icon `3/137` |
| P12 body visual | `rebuild_game/build/smoke/battle_skill12_dang_phuoc_timeline/battle_skill12_dang_phuoc_timeline_p12_body_visual_speffect6_type8.png` | active queue body visual uses `speffect6/AH8` |
| Tick no-op | `rebuild_game/build/smoke/battle_skill12_dang_phuoc_timeline/battle_skill12_dang_phuoc_timeline_tick_noop_duration2.png` | HP unchanged, duration cell `136` |
| Expired | `rebuild_game/build/smoke/battle_skill12_dang_phuoc_timeline/battle_skill12_dang_phuoc_timeline_expired.png` | debuff cleared, status count `0` |

Debug text:

```text
rebuild_game/build/smoke/battle_skill12_dang_phuoc_timeline/battle_skill12_dang_phuoc_timeline_debug.txt
```

## Verification

Passed in this slice:

```text
rebuild_game/build.ps1
--smoke-suite battle_skill12_dang_phuoc_timeline
```

Full regression is recorded with the final status of this work item.

## Honest Status

Skill `12` is now source-shaped and smoke-covered for:

- source row and text ids;
- debuff row `aq.c[7][2]`;
- two-chunk P7 effect row;
- actor effect id/state/side;
- AH type `8` target-side special effect;
- direct damage plus debuff2 apply;
- PP consumption;
- same-run HP settle;
- status icon/duration;
- P12/P13 no-op tick and expiry.

Still `PORTED-PARTIAL` because exact original MIDP pixel/frame comparison is
not part of this slice.

## Next Roadmap Step

Continue Wood Lane in order with skill `13`.

That slice should be audited source-first. Do not assume it is the same as
skill `12`; prove its `aq.c[1]` row, effect row, produced debuff/buff/status,
numeric parameters, visual chunks, and before/during/after smoke result.
