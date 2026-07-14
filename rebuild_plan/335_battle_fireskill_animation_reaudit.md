# 335 Battle FireSkill Animation Re-Audit

Status: AUDIT / FIRE ANIMATION PARITY REOPENED.

Reason: the user verified live gameplay and reported that skill animations and pet movement/hit animation are wrong. Previous FireSkill closeouts proved source rows and gameplay numbers, but they did not prove full live animation parity.

This document supersedes the animation conclusion in `315_battle_fire_animation_comparison_contact_sheet.md`. The old statement that similar fire animations are "source-backed" is too broad. Only the `effect.mid` chunk identity is source-backed; base actor motion, hit/recover, AH placement, chunk timing, hide/restore, and live feel remain open.

## Source Rules To Use

Source P7 uses two layers:

- `game.b.d(byte)` changes the real pet actor state:
  - `0`: idle
  - `1`: action/attack
  - `2`: hit/recover
  - `3`: death/special
  - `4`: alternate state path
- `game.b.a(short, byte)` creates actor-attached `u = new ah([effectId,state,dir])`.
- `game.b.a(Graphics)` draws: `L before -> base actor -> L after -> u`.
- `game.d.n()` reads `effect.mid[skill]` in 7-value chunks.
- `chunk[1] == 0`: actor-attached `u`.
- `chunk[1] == 1`: special `H` from `speffect.mid`.
- `game.d case 7` can:
  - start attacker base state `1` when first chunk targets enemy/target side;
  - reset attacker to `0`;
  - set target to state `2` on hit/recover;
  - hide/restore actor during `H`.

## Fire Skill Source Matrix

Raw `aq.c[1][skill]` rows:

| Skill | Name | Raw skill row | Meaning |
| ---: | --- | --- | --- |
| 0 | Hoa trao | `[0,117,529,100,0,45,0,-1,-1,0]` | direct damage, no status |
| 1 | Duong viem | `[0,118,530,50,0,45,2,0,4,0]` | damage + debuff0 HP drain divisor 4 |
| 2 | Diem kich | `[0,119,531,100,0,45,2,1,10,0]` | damage + debuff1 chance 10 |
| 3 | Hoa Van trieu | `[0,120,532,100,1,30,0,-1,120,0]` | conditional damage if target has debuff0 |
| 4 | Thien Hoa te | `[0,121,533,0,1,10,1,0,-1,1]` | no damage, self buff0 |
| 5 | Viem loi pha | `[0,122,534,0,1,10,1,1,-1,1]` | no damage, self buff1 |
| 6 | Hoa diem dao | `[0,123,535,150,2,30,0,-1,-1,0]` | direct damage, no status |
| 7 | Chuoc nhiet chi xuc | `[0,124,536,75,2,30,2,0,3,0]` | damage + debuff0 HP drain divisor 3 |
| 8 | Liet diem phong bao | `[0,125,537,200,3,15,2,1,20,0]` | high damage + debuff1 chance 20 |
| 9 | Vinh hang hoa anh | `[0,126,538,200,3,15,0,-1,250,0]` | conditional damage if target has debuff0 |

## Fire P7 Effect Matrix

Raw `effect.mid[0..9]` rows:

| Skill | Raw effect row | Source chunk interpretation | Current concern |
| ---: | --- | --- | --- |
| 0 | `[0,0,20,0,-1,-1,0]` | target-side actor `u20/state0`; attacker base should enter state `1`; target hit state `2` after damage | actor `u` row is source-backed, but live actor movement/hit-recover parity not closed |
| 1 | `[0,0,20,0,-1,-1,0]` | same P7 row as skill0; gameplay differs by debuff0 after hit | same as skill0; previous smoke over-focused on debuff text/icon |
| 2 | `[0,0,20,0,-1,-1,0, 0,1,14,0,0,0,-1]` | chunk0 target-side `u20`; chunk1 target-side `H speffect14/AH12`; chunk1 can be triggered by source chunk flow | AH12 visibility/placement is suspect; current contact sheet makes it barely readable |
| 3 | `[0,0,20,0,-1,-1,0]` | same P7 row as skill0; gameplay differs by conditional damage | same as skill0; conditional smoke does not prove animation |
| 4 | `[0,1,16,0,-1,-1,0, 0,1,15,0,-1,-1,0]` | two target/self-side `H` effects: speffect16/AH9 then speffect15/AH1; no actor `u`; no damage | needs H hide/restore visual parity; old smoke proves buff numbers, not full visual |
| 5 | `[0,1,16,0,-1,-1,0, 0,1,15,0,-1,-1,0]` | same visual producer path as skill4; buff id differs | source row says same producer animation as skill4, but still needs H parity |
| 6 | `[0,0,20,0,-1,-1,0]` | same P7 row as skill0; higher damage | same as skill0 |
| 7 | `[0,0,20,0,-1,-1,0]` | same P7 row as skill0; gameplay differs by debuff0 divisor 3 | same as skill0 |
| 8 | `[0,0,20,0,-1,-1,0, 0,1,14,0,0,-1,1]` | chunk0 target-side `u20`; chunk1 target-side `H speffect14/AH12`; source differs from skill2 at chunk[5]/[6] | AH12 timing/state trigger is not closed; current smoke likely misses important motion/state transition |
| 9 | `[0,0,20,0,-1,-1,0]` | same P7 row as skill0; gameplay differs by conditional damage | same as skill0 |

Referenced `speffect.mid` rows:

| Speffect | Raw row | AH type | Used by | Current renderer status |
| ---: | --- | ---: | --- | --- |
| 14 | `[12,2,255,120,0,9,0,0,2,0,4,0,-4,0,-6,0,-4,0,0,0,2,0,4,0,2,0,0,0,0,0,3,0,8,0,3,0,0,0,-4,0,-9,0,-4,0]` | 12 | skills 2, 8 and active debuff1 visuals | PORTED/PARTIAL, but placement/timing/visibility needs fire-specific visual proof |
| 15 | `[1,0,5,3,0,0]` | 1 | skills 4, 5 second chunk | PORTED/PARTIAL, needs H lifecycle visual proof |
| 16 | `[9,150,181,37,84,0,5,5]` | 9 | skills 4, 5 first chunk | PORTED/PARTIAL, needs H lifecycle visual proof |

## Current FireSkill Smoke Problem

The current `FireSkill.java` timeline smokes are useful for numbers, but insufficient for animation parity:

| Current smoke behavior | Problem |
| --- | --- |
| Captures `actor_u20_start` or `speffect*_type*` single frames | Does not prove attacker base state `1`, target hit state `2`, or return to idle |
| Contact sheet groups by broad source path | Hides timing/state differences, especially skill8 chunk `[5]/[6]` |
| Skill 2/8 show `speffect14/AH12` but visual is too subtle in contact sheet | Needs focused zoom/frame sequence and pixel assertion in target body region |
| Skill 4/5 prove buff logic | Do not prove `H` actor hide/restore or source draw order |
| Previous closeouts say `PORTED/PARTIAL` | Must be reinterpreted as logic ported, animation parity reopened |

## Re-Audit Classification

| Skill | Logic status | Animation status after this audit | Must fix/prove before calling done |
| ---: | --- | --- | --- |
| 0 | PORTED | PARTIAL | prove attacker state1 motion, target `u20`, target hit state2, return idle |
| 1 | PORTED | PARTIAL | same as skill0 plus debuff0 text/icon after hit |
| 2 | PORTED | PARTIAL/HIGH RISK | prove `u20 -> H speffect14/AH12`, H placement over target, hit state2 after damage |
| 3 | PORTED | PARTIAL | same as skill0 plus conditional damage branch |
| 4 | PORTED | PARTIAL/HIGH RISK | prove `H speffect16 -> H speffect15`, actor hide/restore, no damage path |
| 5 | PORTED | PARTIAL/HIGH RISK | same as skill4 plus buff1 icon/logic |
| 6 | PORTED | PARTIAL | same as skill0 |
| 7 | PORTED | PARTIAL | same as skill0 plus debuff0 body/active tick later |
| 8 | PORTED | PARTIAL/HIGH RISK | prove different chunk trigger from skill2, especially chunk `[5]/[6]` state set |
| 9 | PORTED | PARTIAL | same as skill0 plus conditional damage branch |

## Required Fire Fix Plan

Do this before moving to Wood/Earth again:

1. Replace the Fire contact sheet with a source-stage contact sheet:
   - before;
   - attacker base state `1` frame;
   - target actor `u` or `H` frame;
   - target hit/recover state `2` frame;
   - settled idle state.
2. Add smoke assertions for:
   - `battleP7BaseStatePlayerSide == 1` during target-side actor `u20`;
   - `battleP7BaseStateEnemySide == 2` on hit/recover for hit skills;
   - both sides return to state `0`;
   - `battleP7BaseHidden*` toggles during `H` for skills 4/5 and 2/8 if source owner side hides target.
3. Focus fixes in shared P7 runtime/renderer first, not per-skill damage code.
4. Re-run:
   - fire source-stage smoke;
   - `battle_fire_animation_contact_sheet`;
   - `battle_quick`;
   - build/check/formula/mojibake scan.

## Source-Stage Smoke Added

Implemented in:

```text
rebuild_game/src/main/java/FireSkill.java
```

New suite:

```text
battle_fire_source_stage_animation
```

This is stricter than the old contact sheet. For each Fire skill `0..9`, it captures:

```text
0_before
1_attacker_state1
2_target_u_or_h
3_target_hit_state2
4_settled_idle
```

For no-damage self buff skills `4/5`, stage `3_target_hit_state2` is intentionally a no-hit/no-damage checkpoint and asserts no target hit state/damage frame occurs.

Important assert coverage:

- damage skills must show attacker base state `1`;
- actor skills must show target-side `u20/sprite262`;
- actor+H skills `2/8` must show `u20` and then `speffect14/AH12`;
- self-buff H skills `4/5` must show `speffect16/AH9`, player-side special, and player base hidden during `H`;
- damage skills must show enemy base state `2` on damage/hit frame;
- all skills must settle with no actor effect, no special, no hidden base, and base states `0/0`.

Run:

```text
java -cp build\classes VqsvIntroDemo --smoke-suite battle_fire_source_stage_animation build\smoke\fire_source_stage_animation
```

Result:

```text
PASS
```

Output:

```text
rebuild_game/build/smoke/fire_source_stage_animation/battle_fire_source_stage_animation.png
rebuild_game/build/smoke/fire_source_stage_animation/battle_fire_source_stage_animation_zoom.png
rebuild_game/build/smoke/fire_source_stage_animation/battle_fire_source_stage_animation_debug.txt
rebuild_game/build/smoke/fire_source_stage_animation/fire_source_stage_frames/
```

The zoom sheet crops and scales the player-body region and enemy-body region
for each skill across the source-stage timeline. The `before` column is still
partly covered by the P3 skill UI because that is the actual before-confirm
state; the useful visual review columns are `atk1`, `u/H`, `hit`, and `idle`.

Fix made during this slice:

- removed a bad Fire smoke assumption that treated skill7 PP as `45`; the new source-stage smoke now reads `ppMax` from `aq.c[1][skill]`.
- no P7 runtime animation patch was needed in this slice because the new state assertions passed after the shared actor-anchor patch from `334`.

## Old Contact Sheet Smoke

Ran after the shared actor-anchor patch:

```text
java -cp build\classes VqsvIntroDemo --smoke-suite battle_fire_animation_contact_sheet build\smoke\fire_animation_reaudit
```

Result: PASS for the old contact sheet, but this is not sufficient to close animation parity.

Main PNG:

```text
rebuild_game/build/smoke/fire_animation_reaudit/battle_fire_animation_contact_sheet.png
```

Important note: this PNG proves current grouping and rough visibility only. It does not prove live motion parity.

## Verification

Passed after adding the source-stage smoke:

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-suite battle_fire_source_stage_animation build\smoke\fire_source_stage_animation
java -cp build\classes com.vqsv.rebuild.Main --check
java -cp build\classes VqsvBattleDamageFormulaCheck
rg -n "Ã|Â|Há»|Ä" src\main\java
java -cp build\classes VqsvIntroDemo --smoke-suite battle_quick build\smoke\fire_source_stage_battle_quick
```

`rg` returned no matches.

## Next

The next practical visual step is to add a zoomed source-stage contact sheet or per-stage crop for the actor body region. The current source-stage sheet is correct for state assertions, but the thumbnails are too small for comfortable human visual review of hit/recover and AH12 placement.
