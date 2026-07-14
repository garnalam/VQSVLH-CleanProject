# 301 - Battle Skill Grouped Logic / Effect / Animation Roadmap

Date: 2026-07-14

Status: CURRENT NEXT PHASE ROADMAP / AUDIT MATRIX / NO RUNTIME CODE CHANGE.

Scope: all source pet skills in `aq.c[1][0..69]`.

Purpose:

- stop treating skills as 70 unrelated rows;
- group skills by how source code actually runs them;
- list source logic, downstream consumers, animation/effect path, current status,
  and next work;
- make future skill work testable in Battle Lab and `battle_quick`.

This document builds on:

- `156_battle_phase9_skill_coverage_matrix.md`
- `187_battle_phase9ad_skill_coverage_closeout.md`
- `236_battle_skill_full_logic_animation_matrix.md`
- `263_battle_buff_debuff_effect_current_matrix.md`
- `300_battle_debuff_table_0_10_closeout.md`

## Shared Skill Runtime Flow

Most damaging skills pass through this shape:

```text
P3/P4 skill menu
  -> P6 target select when needed
  -> P7 animation/effect/damage frame
  -> game.b.b(target) computes formula, crit, debuff result
  -> game.d P7 hit/miss frame applies visible HP/text
  -> game.d.q() optional post-skill effect
  -> P12/P13 active buff/debuff queue
  -> P8/P9/P15 result/EXP/replacement/lose paths when battle state changes
```

Animation source:

```text
effect.mid row by skill id
  chunk[0] = side/owner
  chunk[1] = 0 actor animation, 1 speffect
  chunk[2] = actor/effect/speffect id
  chunk[3..6] = timing/anchor/flags

speffect.mid row -> AH renderer type
```

Important rule:

```text
Logic parity and animation parity are separate.
```

A skill may be source-correct in formula but still `PORT/PARTIAL` visually
until its exact P7 chunk timing, hit/recoil/blood timing, and AH placement are
frame-compared or smoke-locked.

## Group A - Direct Simple Damage Baseline

Skills:

```text
0, 6, 10, 16, 20, 26, 30, 36, 40, 46, 50, 56, 60, 66
```

Source logic:

- `game.b.b(target)` direct damage.
- Formula family: `attack base * skill[3] / 100`, then relation/passive/jitter
  hooks.
- P7 handles miss, crit, damage text, HP tween, blood/hit visual.

Animation:

| Element lane | Representative skills | Base actor/effect id |
| --- | --- | --- |
| Fire | `0`, `6` | `effect.mid` actor/effect `20` |
| Wood | `10`, `16` | `21` |
| Earth | `20`, `26` | `22` |
| Water | `30`, `36` | `23` |
| Electric | `40`, `46` | `24` |
| Ghost | `50`, `56` | `25` plus extra speffects on some rows |
| Wind | `60`, `66` | `26` |

Current status:

```text
PORTED/PARTIAL
```

Smoke coverage:

- Phase 9-B / 9-P direct forced-hit smokes.
- Battle route anchors use skill10 heavily.

Remaining work:

- direct base animation pass per element lane;
- exact damage text + HP tween placement;
- original-vs-rebuild pixel compare if captures exist.

Recommended next slice:

```text
direct base animation representative: skill10 first, then one per element lane
```

## Group B - Direct Damage Plus Stored Debuff Damage

Skills:

```text
1, 7
```

Source logic:

- direct damage plus extra raw attack divided by `skill[8]`;
- applies debuff0 `Gieo Hat`;
- debuff0 later ticks using the stored raw damage:
  - skill1 divisor `4`;
  - skill7 divisor `3`.

Animation:

- same fire direct base actor/effect row `20`;
- debuff0 body visual is handled later by P12/P13 active queue.

Current status:

```text
PORTED
```

Smoke coverage:

- `battle_status_debuff0_skill1_producer_apply`
- `battle_status_debuff0_skill7_producer_apply`
- `battle_status_debuff0_tick_skill1_div4`
- `battle_status_debuff0_tick_skill7_div3`
- P12 body visual and expiry smokes.

Remaining work:

- exact direct fire hit animation timing, shared with Group A;
- full RNG stream parity for debuff chance if source route needs it.

## Group C - Conditional Damage By Existing Debuff

Skills:

```text
3, 9 -> stronger if target has debuff0
23, 29 -> stronger if target has debuff1
```

Source logic:

- normal direct damage if target lacks the required debuff;
- alternate percent from `skill[8]` if the target has the required debuff.

Runtime impact:

- these skills depend on previous status state;
- switching/persistence matters because the target's debuff slots must survive
  normal battle state transitions.

Animation:

- mostly direct element base rows:
  - fire lane for `3/9`;
  - earth lane for `23/29`, with speffect6 on skill23.

Current status:

```text
PORTED/PARTIAL
```

Smoke coverage:

- Phase 9-C/AC deterministic conditional damage smokes.

Remaining work:

- UI text/skill description preview should display correct conditional nature;
- exact animation of skill23 speffect6 remains visual parity work.

## Group D - Explicit Debuff Chance

Skills:

```text
2, 8, 22, 28 -> debuff1 Me Muoi
41, 47 -> debuff10 Te Liet
```

Source logic:

- direct damage;
- debuff chance comes from `skill[8]`;
- target buff14 blocks;
- held/passive status3 can reduce chance;
- P7 miss hides visible debuff text but source-immediate debuff state can still
  be queued.

Animation:

| Skills | Element/effect path | Debuff body visual |
| --- | --- | --- |
| `2`, `8` | fire base actor/effect `20`, plus speffect14/AH12 | debuff1 P12/P13 speffect14/AH12 |
| `22`, `28` | earth base actor/effect `22` | debuff1 P12/P13 speffect14/AH12 |
| `41`, `47` | electric base actor/effect `24`, plus speffect4/AH7 | debuff10 row `[1,19,0,-1,1,6,0,-1]` |

Current status:

```text
PORTED/PARTIAL
```

Reason for partial:

- exact RNG seed stream and pixel-perfect AH timing remain partial;
- debuff10 action-delay scheduling is `NOT_FOUND_IN_PC_SOURCE`.

Smoke coverage:

- debuff1 closeout `283`;
- debuff10 closeout `299`;
- older Phase 9-E and 9-L family smokes.

Next visual work:

- tighten skill41/47 electric speffect4 placement only if visual mismatch is
  observed.

## Group E - Implicit Debuff Direct Skills

Skills:

```text
12, 18 -> debuff2 Quan Quanh
13, 19 -> debuff3 Thuc Loai
31, 37 -> debuff4 Muc
32, 38, 61 -> debuff5 Cham Chap
33, 39 -> debuff6 Nhut Chi
51, 57 -> debuff7 Phong Ngu
68 -> direct damage + debuff10 + self buff10
```

Source logic:

- direct damage first;
- associated debuff id comes from skill row `effectId`;
- stored debuff value differs per id:
  - debuff3 stores raw damage;
  - debuff4 stores `skill[8]`;
  - debuff5 stores speed percent;
  - debuff6 stores outgoing damage reduction percent;
  - debuff7 stores defense-down percent;
  - debuff10 stores no stat value.

Animation:

| Debuff | Skills | P7 animation notes | P12/P13 body visual |
| --- | --- | --- | --- |
| debuff2 | `12`, `18` | wood/vine base, speffect6/AH8 on skill12 | yes, speffect6/type8 |
| debuff3 | `13`, `19` | wood base actor/effect | yes, actor21 |
| debuff4 | `31`, `37` | water base; skill37 has speffect7 then speffect6 | no by source gate |
| debuff5 | `32`, `38`, `61` | water/wind base, several speffects | no by source gate |
| debuff6 | `33`, `39` | water base | no by source gate |
| debuff7 | `51`, `57` | ghost base, speffect8/11 or 14/11 | no by source gate |
| debuff10 | `68` | wind base, speffect0 then speffect15 self side | debuff10 yes if queued |

Current status:

```text
PORTED/PARTIAL
```

Smoke coverage:

- debuff2 through debuff7 closeouts;
- debuff10 closeout;
- skill68 Phase 9-U direct/debuff/self-buff smoke.

Remaining work:

- exact P7 animation chunk timing for skill37/51/57/68;
- multi-target/formation parity if source routes these into non-1v1 combat.

## Group F - No-Damage Buff Producers

Skills:

```text
4, 5, 14, 15, 24, 25, 34, 35, 44, 45, 65
```

Source logic:

- `skill[3] == 0` no direct damage;
- effect mode `1` creates self-side buff/post-effect behavior;
- visible P7 should not fake damage;
- P13 active queue consumes buff logic and icon/visual as applicable.

Buff mapping:

| Skills | Buff | Runtime effect |
| --- | --- | --- |
| `4` | buff0 Suc Luc | defense up + duration-edge extra damage |
| `5` | buff1 Pha Phu | defense down + outgoing damage up |
| `14` | buff2 Kinh Cuc | defense up + reflect |
| `15` | buff3 Khoi Phuc | heal over time |
| `24` | buff13 Thach Hoa | cleanse + 20% maxHP heal |
| `25` | buff14 Thach Phu | cleanse + debuff immunity |
| `34` | buff5 Vo Hinh | chance reflect/store damage |
| `35` | buff6 Kien Nhan | intentional gameplay fix: 50% chance half incoming damage |
| `44` | buff8 Dien Ap | outgoing damage +30%, extra PP cost |
| `45` | buff9 Hoa Thach | speed +50%, defense -50% |
| `65` | buff12 Gia Toc | follow-up state / PP conservation |

Animation:

- many rows use speffect/AH type9 then type1 as second chunk;
- buff3, buff5, buff13 have P12/P13 body visuals;
- most other buff ids are icon-only after producer.

Current status:

```text
PORTED / PORTED-PARTIAL / INTENTIONAL_DEVIATION where noted
```

Remaining work:

- pixel-perfect original comparison for producer visuals;
- frame-by-frame P7 chunk timing for visually complex rows:
  - skill15,
  - skill34/35,
  - skill45,
  - skill65.

## Group G - Raw Damage Then Self-Buff In game.d.q()

Skills:

```text
21, 27 -> buff4
42, 48 -> buff7
62 -> buff10
68 -> direct damage + debuff10 + buff10
```

Source logic:

- source bytecode routes several effectMode rows through default raw damage;
- after P7, `game.d.q()` applies the self buff;
- this differs from pure no-damage buff producers.

Animation:

| Skills | Base lane | Extra chunks |
| --- | --- | --- |
| `21`, `27` | earth `22` | speffect5 or speffect7 |
| `42`, `48` | electric `24` | speffect1 or speffect9 |
| `62`, `68` | wind `26` | speffect0 then speffect15 |

Current status:

```text
PORTED/PARTIAL
```

Smoke coverage:

- raw damage + self-buff smokes Phase 9-R;
- buff4/buff7/buff10 dedicated closeouts;
- skill68 Phase 9-U.

Remaining work:

- exact q() timing relative to damage text and P12/P13 transition;
- buff10 remains intentional gameplay fix rather than source oddity.

## Group H - Heal / Leech / Follow-Up Post-Skill Consumers

Skills:

```text
11, 17 -> heal attacker
52, 58 -> leech-style heal chance
63, 69 -> chance to attack again
```

Source logic:

- direct damage path runs first;
- `game.d.q()` then performs post-skill behavior;
- miss interactions are important because source q() branches do not always
  line up intuitively with visible hit text.

Animation:

- skill11/17 use wood base actor/effect plus speffect10/AH9 for heal.
- skill52/58 use ghost base rows with speffect10/AH9 heal segment.
- skill63/69 are wind direct rows and rely more on state transition than new
  body visual.

Current status:

```text
PORTED/PARTIAL
```

Smoke coverage:

- Phase 9-D hit/miss smokes for heal, leech, follow-up;
- buff12 follow-up state smokes.

Remaining work:

- no-follow-up negative case for skill63/69 if not already separately locked;
- exact heal text/effect placement.

## Group I - Target Buff Clear

Skills:

```text
43, 49
```

Source logic:

- direct damage;
- on successful source path, target `D()` clears self buffs and active buff
  queue.

Animation:

- electric base actor/effect `24`;
- skill43 includes speffect4/AH7;
- skill49 is mostly base row.

Current status:

```text
PORTED/PARTIAL
```

Smoke coverage:

- Phase 9-N hit/miss/no-buff smokes.

Remaining work:

- exact visual cue for buff clear, if source has any visible marker beyond
  ordinary P7 chunks.

## Group J - HP Percent Scaling

Skills:

```text
53, 59
```

Source logic:

- damage scales from attacker/target HP percent source formula;
- includes min clamp and miss interaction.

Animation:

- ghost lane `25`;
- skill53 includes speffect9/AH9.

Current status:

```text
PORTED/PARTIAL
```

Smoke coverage:

- Phase 9-O low/high HP, min clamp, and miss smokes.

Remaining work:

- exact animation and relation/passive interaction after full passive audit.

## Group K - Selected Buff Copy / Steal

Skill:

```text
64
```

Source logic:

- no direct damage;
- selected target/index matters;
- copies active beneficial statuses from selected donor to attacker;
- clears donor buffs;
- buff11 active tick can re-steal.

Animation:

- speffect18/AH9 then speffect15/AH1.

Current status:

```text
PORTED/PARTIAL
```

Reason for partial:

- one selected enemy target is smoke-covered;
- full multi-enemy/source `d[]` selected slot parity remains partial.

Smoke coverage:

- buff11 closeout `276`;
- `battle_status_buff11_skill64_selected_buff_copy`;
- active tick/donor switch cleanup smokes.

Remaining work:

- multi-target/formation slot proof before widening.

## Group L - Zero-Power Special Rows

Skills:

```text
54, 55
```

Source logic:

- table says debuff8/debuff9, but `skill[3] == 0`;
- ordinary direct damage/debuff producer is `NOT_REACHED` from P7 zero-power
  guard;
- user-approved gameplay for active debuff8 is implemented separately;
- debuff9 active random-target consumer is implemented separately.

Animation:

- skill54 uses speffect0/AH9.
- skill55 uses speffect12/AH12.

Current status:

```text
54: INTENTIONAL_DEVIATION / GAMEPLAY_FIXED for active debuff8
55: PORTED/PARTIAL for active debuff9
ordinary producers: NOT_REACHED
```

Smoke coverage:

- debuff8 closeout `296`;
- debuff9 closeout `297`;
- type12/type9 visual anchors.

Remaining work:

- full source multi-active target divergence if a real source route appears;
- do not re-enable ordinary debuff producer for skill54/55.

## Skill Status Summary

| Group | Skills | Current status | Next work |
| --- | --- | --- | --- |
| A Direct simple | `0,6,10,16,20,26,30,36,40,46,50,56,60,66` | PORTED/PARTIAL | direct base animation polish |
| B Plus divisor + debuff0 | `1,7` | PORTED | shared fire/direct animation polish |
| C Conditional damage | `3,9,23,29` | PORTED/PARTIAL | UI description + visual parity |
| D Explicit debuff chance | `2,8,22,28,41,47` | PORTED/PARTIAL | exact RNG/pixel parity later |
| E Implicit debuff direct | `12,13,18,19,31,32,33,37,38,39,51,57,61,68` | PORTED/PARTIAL | complex P7 chunk timing |
| F No-damage buffs | `4,5,14,15,24,25,34,35,44,45,65` | PORTED/PARTIAL | producer visual polish |
| G Raw damage + q() self buff | `21,27,42,48,62,68` | PORTED/PARTIAL | q() timing + animation |
| H Heal/leech/follow-up | `11,17,52,58,63,69` | PORTED/PARTIAL | post-skill text/effect placement |
| I Clear target buffs | `43,49` | PORTED/PARTIAL | clear visual proof |
| J HP percent scaling | `53,59` | PORTED/PARTIAL | passive/relation edge cases |
| K Selected copy | `64` | PORTED/PARTIAL | multi-target selected slot |
| L Zero-power special | `54,55` | PORTED/PARTIAL / GAMEPLAY_FIXED | do not ordinary-produce debuff8/9 |

## Next Recommended Phase

Start with:

```text
Phase Skill-A: direct base animation pass
```

Why this first:

- direct base rows are shared by many skills;
- fixing them improves most future skill smokes;
- it avoids reopening debuff logic that is now table-closed.

Suggested first audit:

```text
302_battle_skill_direct_base_animation_audit.md
```

Scope:

- source `effect.mid` rows for representatives `0,10,20,30,40,50,60`;
- `game.d` P7 animation cursor and hit timing;
- current renderer placement of actor/action/speffect chunks;
- choose one representative, likely skill10, for a small visual polish slice.

Do not code the next slice until the audit proves the exact source animation
path and defines before/during/after PNG checkpoints.
