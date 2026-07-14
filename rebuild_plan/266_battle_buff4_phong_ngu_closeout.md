# 266 - Battle Buff4 Phong Ngu Closeout

Date: 2026-07-13

Scope: close out `aq.c[6][4]` temporary self buff, producer skills `21` and `27`.

Status: PORTED.

## Source Audit

| Source | Proven fact |
| --- | --- |
| `modules/script/decoded/data__script__db.mid.json` | Buff row `aq.c[6][4] = [337,352,2,-1,-1]`. |
| `modules/script/decoded/data__script__db.mid.json` | Skill `21 = [2,138,550,80,0,45,1,4,10,0]`; skill `27 = [2,144,556,100,2,30,1,4,10,0]`. |
| `modules/source_code/decoded/decompiled_source_cfr/game/b.java` | `game.b.a(byte,int,int)` case `4` stores `sourceSkill` and computes `v[4][1] = baseDefense * aq.c[1][sourceSkill][8] / 100`. |
| `modules/source_code/decoded/decompiled_source_cfr/game/b.java` | `game.b.o(4)` applies `d[3] = d[3] + v[4][1]` before duration decrement/clear. |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java` | `game.d.q()` cases `21/27/42/48/62/68` apply self buff `aq.c[1][skill][7]` to the attacker. |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java` | Buff body visual gate `ai[0] = {3,5,13}` excludes buff4, so no P12/P13 body visual should be invented. |
| `modules/script/decoded/data__script__effect.mid.json` | `effect.mid[21] = [0,0,22,0,-1,-1,0, 1,1,5,0,-1,-1,0]`; `effect.mid[27] = [0,0,22,0,-1,-1,0, 1,1,7,0,-1,-1,0]`. |

## Formula

Buff4 does not use the raw buff row params `-1,-1` for numeric value.

For current producer skills:

```text
storedDefense = baseDefense * skill[8] / 100
skill21[8] = 10
skill27[8] = 10
baseDefense 100 -> storedDefense 10 -> currentDefense 110
```

HUD:

```text
iconCell = buffId + 12 = 16
durationCell(2) = 134 + 2 = 136
durationCell(1) = 134 + 1 = 135
duration 0 -> clear icon
```

Source tick/expiry:

```text
apply:      currentDefense 100 -> 110, duration 2
tick #1:    game.b.o(4) adds storedDefense again, 110 -> 120, duration 1
tick #2:    adds then clears through d(4,slot); rebuild clear restores base defense 100
```

The `110 -> 120` intermediate frame is source-shaped, not a design choice.

## Implemented Smoke Checkpoints

| Checkpoint | What it locks |
| --- | --- |
| `battle_status_buff4_before_no_effect` | Before snapshot: no buff4, no status icon, defense `100 -> 100`. |
| `battle_status_buff4_producer_visual_defense` | Skill21 producer visual from `effect.mid[21]`, `sourceEffectId=22`, `speffect=5`, applies buff4 and icon `16/136`. |
| `battle_status_buff4_forced_hit_target_defense` | Target buff4 reduces deterministic direct skill10 damage `100 -> 90`. |
| `battle_status_buff4_forced_miss_no_extra_side_effect` | Forced miss keeps HP unchanged and leaves buff4 active. |
| `battle_status_buff4_forced_crit_no_wrong_multiplier` | Crit uses defense-up damage first: `90 -> 135`; no extra buff multiplier. |
| `battle_status_buff4_expiry_clears_defense` | Expiry path: `100 -> 110 -> 120 -> 100`, icon clears. |

## Focused Smoke Results

| Stage | PNG | Numeric result |
| --- | --- | --- |
| Before | `rebuild_game/build_intro_demo/battle_status_buff4_before_no_effect.png` | defense `100 -> 100`, status count `0`. |
| During producer | `rebuild_game/build_intro_demo/battle_status_buff4_producer_visual_defense.png` | skill21 damage `100`; defense `100 -> 110`; buff value `10`; duration `2`; icon/duration `16/136`. |
| During hit | `rebuild_game/build_intro_demo/battle_status_buff4_forced_hit_target_defense.png` | baseline damage `100`; target buff4 damage `90`; target defense `100 -> 110`. |
| During miss | `rebuild_game/build_intro_demo/battle_status_buff4_forced_miss_no_extra_side_effect.png` | HP unchanged, buff value `10`, duration `2`, icon/duration `16/136`. |
| During crit | `rebuild_game/build_intro_demo/battle_status_buff4_forced_crit_no_wrong_multiplier.png` | non-crit `90`; crit `135`. |
| After expiry | `rebuild_game/build_intro_demo/battle_status_buff4_expiry_clears_defense.png` | apply `100 -> 110`; first tick `120`; expired defense `100`; status count `0`. |

Focused checkpoint command pattern:

```powershell
java "-Dvqsv.modules=..\modules" -cp build/classes VqsvIntroDemo --smoke-checkpoint battle_status_buff4_producer_visual_defense build_intro_demo/battle_status_buff4_producer_visual_defense.png
```

## Re-Audit

| Question | Result |
| --- | --- |
| Did we copy buff0/buff1 producer visual? | No. Skill21/27 producer visuals are asserted from their own `effect.mid` rows. |
| Did we use row params `-1,-1` as numeric values? | No. Runtime and smoke use producer `skill[8]`, matching `game.b.a(... case 4)`. |
| Did we invent P12/P13 body visual? | No. Source visual gate excludes buff4. |
| Did miss incorrectly commit side effects? | No. Forced miss keeps target HP unchanged and status unchanged. |
| Did crit add a fake buff multiplier? | No. Crit is normal `nonCrit * 3 / 2` after defense-up damage. |
| Does expiry hide icon and restore mutable defense? | Yes. The intermediate `120` state is kept because source tick adds the stored value before clear. |

Remaining status:

- `PORTED`: buff4 formula, producer skill21 visual, numeric hit/miss/crit/expiry, HUD icon/duration.
- `PORTED/PARTIAL`: skill27 producer visual is source-audited and shares the same formula, but this closeout's dedicated producer PNG uses skill21. Skill27 remains covered by Phase9R broad smoke and the source row is documented here.
- `PENDING`: no original-client pixel compare for exact frame-by-frame P7 producer animation.

Next roadmap step: buff5 `Vo hinh`, source chance reflect + P12/P13 body visual, with dedicated source audit before code.
