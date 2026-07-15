# 355 - Battle Fire Skill `effect.mid` S60 Reaudit

Date: 2026-07-14

Status: FIRE SKILLS 0..9 REAUDITED / SMOKE HARNESS FIXED / RUNTIME PATCH NOT REQUIRED.

Scope:

- Fire element skills `0..9`.
- Producer animation rows from `effect.mid`.
- Skill logic rows from `aq.c[1]` in `data__script__db.mid.json`.
- Rebuild smoke support in `rebuild_game/src/main/java/FireSkill.java`.

No live client was opened. This audit used decoded source/data plus PNG smoke.

## Why This Was Reopened

After the S60 resource/source merge, Fire skill producer rows are richer than
the older Fire audit docs. The old conclusion that many Fire skills shared
`u20/state0` is no longer correct for current modules.

Current source proves:

- Fire skills are not selected by one generic "Fire class" animation.
- Each skill reads its own `effect.mid[skillId]`.
- Several skills still share actor sprite source id `20`, but they now use
  different animation states.
- Skills `2/8` have multi-chunk producer sequences.
- Skills `4/5` now have self-side actor chunks before the self buff speffects.

## Current Fire Skill Matrix

| Skill | Name | Logic row summary | `effect.mid` producer path | Status |
| ---: | --- | --- | --- | --- |
| 0 | Hoa trao | Direct damage, power `100`, PP `45`, no status. | `u20/state0` | PORTED/PARTIAL |
| 1 | Duong viem | Direct damage `50%` plus debuff0 burn/seed DOT, divisor `4`, duration `3`. | `u20/state1` | PORTED/PARTIAL |
| 2 | Diem kich | Direct damage `100%`, `10%` chance debuff1 Me Muoi. | `u20/state2 -> u20/state3 -> speffect0/AH9` | PORTED/PARTIAL |
| 3 | Hoa Van trieu | Direct damage `100%`; if target has debuff0, uses conditional power `120%`. | `u20/state4` | PORTED/PARTIAL |
| 4 | Thien Hoa te | Self buff0 Suc Luc: defense `+30%`, stored extra damage. No direct damage. | `u30/state0 -> speffect16/AH9 -> speffect15/AH1` | PORTED/PARTIAL |
| 5 | Viem loi pha | Self buff1 Pha Phu: defense `-50%`, outgoing damage `+50%`. No direct damage. | `u31/state0 -> speffect16/AH9 -> speffect15/AH1` | PORTED/PARTIAL |
| 6 | Hoa diem dao | Direct damage, power `150`, PP `30`, no status. | `u20/state5` | PORTED/PARTIAL |
| 7 | Chuoc nhiet chi xuc | Direct damage `75%` plus debuff0 DOT, divisor `3`, duration `3`. | `u20/state6` | PORTED/PARTIAL |
| 8 | Liet diem phong bao | Direct damage `200%`, `20%` chance debuff1 Me Muoi. | `u20/state7 -> u20/state3 -> speffect0/AH9` | PORTED/PARTIAL |
| 9 | Vinh hang hoa anh | Direct damage `200%`; if target has debuff0, uses conditional power `250%`. | `u20/state8` | PORTED/PARTIAL |

## Exact Current `effect.mid` Rows

```text
skill0 = [0,0,20,0,-1,-1,0]
skill1 = [0,0,20,1,-1,-1,0]
skill2 = [0,0,20,2,-1,-1,0, 0,0,20,3,1,-1,0, 0,1,0,0,0,-1,1]
skill3 = [0,0,20,4,-1,-1,0]
skill4 = [0,0,30,0,0,-1,0, 0,1,16,0,-1,-1,0, 0,1,15,0,-1,-1,0]
skill5 = [0,0,31,0,0,-1,0, 0,1,16,0,-1,-1,0, 0,1,15,0,-1,-1,0]
skill6 = [0,0,20,5,-1,-1,0]
skill7 = [0,0,20,6,-1,-1,0]
skill8 = [0,0,20,7,-1,-1,0, 0,0,20,3,1,-1,0, 0,1,0,0,0,-1,1]
skill9 = [0,0,20,8,-1,-1,0]
```

Interpretation:

- Chunk layout is 7 bytes.
- Chunk type `special=0` means actor source effect `u<id>` with animation state.
- Chunk type `special=1` means `speffect.mid[id]` rendered through AH.
- `skill2/8` now use `speffect0/AH9` during P7. Their active debuff1 body
  tick still uses `speffect14/AH12` later in P12/P13.
- `skill4/5` are not H-only anymore. They first play self actor `u30/u31`.

## Rebuild Finding

The main P7 runtime was already reading `effect.mid[skillId]` dynamically:

```text
VqsvBattleAnimationTables.instance().effectRow(p7SkillId)
```

So no battle runtime patch was required.

However, `FireSkill` smoke support still had old expected rows in one source
stage helper:

- it grouped skills `1/3/6/7/9` as `u20/state0`;
- it treated skill `2/8` as `u20/state0 -> speffect14/AH12`;
- it treated skill `4/5` as H-only;
- it asserted skill0 actor state using skill7's state by mistake.

That smoke/audit harness was fixed so future Fire checks use the current S60
source rows.

## PNG Smoke Outputs

Source-stage sheet:

```text
rebuild_game/build/smoke/fire_reaudit_current/battle_fire_source_stage_animation.png/battle_fire_source_stage_animation.png
rebuild_game/build/smoke/fire_reaudit_current/battle_fire_source_stage_animation.png/battle_fire_source_stage_animation_zoom.png
rebuild_game/build/smoke/fire_reaudit_current/battle_fire_source_stage_animation.png/fire_source_stage_frames/
```

Live frame-strip sheet:

```text
rebuild_game/build/smoke/fire_reaudit_current_live/battle_fire_live_frame_strip.png/battle_fire_live_frame_strip.png
rebuild_game/build/smoke/fire_reaudit_current_live/battle_fire_live_frame_strip.png/fire_live_frame_strip_frames/
```

Contact sheet and per-skill timelines:

```text
rebuild_game/build/smoke/fire_reaudit_current_contact/battle_fire_animation_contact_sheet.png/battle_fire_animation_contact_sheet.png
rebuild_game/build/smoke/fire_reaudit_current_contact/battle_fire_animation_contact_sheet.png/fire_skill_timelines/
```

## Numeric Smoke Results

Representative numbers from `battle_fire_animation_contact_sheet`:

| Skill | PP | HP/result | Logic proof |
| ---: | --- | --- | --- |
| 0 | `45 -> 44` | enemy `109 -> 84`, damage `25` | direct baseline |
| 1 | `45 -> 44` | enemy `109 -> 90`, damage `19`, tick damage `6` | debuff0 stores raw and ticks `/4` |
| 2 | `45 -> 44` | enemy `109 -> 84`, damage `25` | forced debuff1 roll `0`, debuff1 active |
| 3 | `30 -> 29` | baseline `25`, conditional `29` | debuff0 conditional branch |
| 4 | `10 -> 9` | no enemy damage | buff0 defense `100 -> 130`, duration-edge hook smoke `80 -> 308` |
| 5 | `10 -> 9` | no enemy damage | buff1 defense `100 -> 50`, damage `80 -> 120`, crit `180` |
| 6 | `30 -> 29` | enemy `109 -> 74`, damage `35` | direct stronger damage |
| 7 | `30 -> 29` | enemy `109 -> 84`, damage `25`, tick damage `8` | debuff0 stores raw and ticks `/3` |
| 8 | `15 -> 14` | enemy `109 -> 60`, damage `49` | forced debuff1 roll `0`, higher power/chance |
| 9 | `15 -> 14` | baseline `49`, conditional `60` | debuff0 conditional branch |

## Verification

Commands run from `rebuild_game/`:

```text
build.ps1
java -cp build/classes VqsvIntroDemo --smoke-suite battle_fire_source_stage_animation build/smoke/fire_reaudit_current/battle_fire_source_stage_animation.png
java -cp build/classes VqsvIntroDemo --smoke-suite battle_fire_live_frame_strip build/smoke/fire_reaudit_current_live/battle_fire_live_frame_strip.png
java -cp build/classes VqsvIntroDemo --smoke-suite battle_fire_animation_contact_sheet build/smoke/fire_reaudit_current_contact/battle_fire_animation_contact_sheet.png
java -cp build/classes com.vqsv.rebuild.Main --check
java -cp build/classes VqsvBattleDamageFormulaCheck
```

Results:

| Check | Result |
| --- | --- |
| Build | PASS |
| Fire source-stage PNG | PASS |
| Fire live frame strip PNG | PASS |
| Fire contact sheet + per-skill timelines | PASS |
| Release check | PASS |
| Damage formula check | PASS |

## Remaining Visual Risk

This audit proves source routing and smoke-visible stages. It does not claim
pixel-perfect original parity.

Remaining `PORTED/PARTIAL` debts:

- actor sprite frame timing/cell parity for `u20` states `0/1/2/3/4/5/6/7/8`;
- self actor `u30/u31` frame parity for skills `4/5`;
- AH type9 exact pixel parity for `speffect0` and `speffect16`;
- AH type1 exact pixel parity for `speffect15`;
- original-vs-rebuild frame comparison is still pending.

## Next Step

The next source-backed slice should be one of:

1. Patch/polish Fire visual renderer if PNG inspection shows a concrete mismatch,
   starting with the broadest shared piece: `u20` actor frame/timing/placement.
2. Or continue to the next element family and repeat this exact audit shape:
   skill rows, exact `effect.mid` rows, logic differences, PNG source-stage
   sheet, frame strip, then only patch proven mismatches.
