# 263 - Battle Buff/Debuff Effect Current Matrix

Date: 2026-07-13

Scope: current source-backed matrix for **temporary battle effects only**:

- `aq.c[6]`: self-side buff rows, stored in `game.b.v[id][0..4]`.
- `aq.c[7]`: target-side debuff rows, stored in `game.b.w[id][0..4]`.

This document intentionally does **not** cover `aq.c[3]` held items/passives such as `Mạn Đà La Thạch`, `Hồng Sắc Hải Loa`, materials, or keys. Those are a different system.

## Source Anchors

| Source | What it proves |
| --- | --- |
| `modules/script/decoded/data__script__db.mid.json` | Raw rows: `groups[6]` buff table and `groups[7]` debuff table. |
| `modules/script/decoded/data__script__chs.mid.json` | Vietnamese name/description text ids used by the raw rows. |
| `modules/source_code/decoded/decompiled_source_cfr/game/b.java` | `a(byte,int,int)` buff apply, `o(int)` buff tick, `b(target)` damage/debuff apply, `q(int)` debuff tick, `C()/D()` clear. |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java` | P7/P12/P13 state flow and active queue text/visual calls. |
| `modules/script/decoded/data__script__bufDebuf.mid.json` | P12/P13 active queue visual rows. |
| `modules/img/decoded/data__img__img_804.mid.png` | Status icon sheet used by battle HUD status slots. |
| `rebuild_game/src/main/java/VqsvBattleUnit.java` | Current rebuild equivalent for buff/debuff apply, tick, clear, formula hooks. |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Current P7/P12/P13 runtime and smoke-only setup helpers. |

## Runtime Layout

| Field | Meaning |
| --- | --- |
| `v[id][0]`, `w[id][0]` | remaining duration/counter |
| `v[id][1]`, `w[id][1]` | primary stored numeric value |
| `v[id][2]`, `w[id][2]` | secondary stored numeric value when source uses it |
| `v[id][3]`, `w[id][3]` | source skill id or selected donor index |
| `v[id][4]`, `w[id][4]` | active flag, `1` means active |

HUD status icon rule from `game.h`:

| Effect kind | Icon cell | Duration cell |
| --- | ---: | ---: |
| buff `id` | `id + 12` | `134 + v[id][0]` |
| debuff `id` | `id + 1` | `134 + w[id][0]` |

P12/P13 body visual gate from `game.d`:

| Bank | Has active body visual | No P12/P13 body visual, but logic/icon can still exist |
| --- | --- | --- |
| buff bank `0` | `3, 5, 13` | `0,1,2,4,6,7,8,9,10,11,12,14` |
| debuff bank `1` | `0,1,2,3,8,9,10` | `4,5,6,7` |

## Self Buff Matrix - `aq.c[6]`

| Id | Name | Source description | Raw row | Exact numbers / formula | Main runtime effect | Visual/icon | Current status | Smoke/coverage |
| ---: | --- | --- | --- | --- | --- | --- | --- | --- |
| 0 | Súc Lực | Không công kích ngay, tăng phòng ngự, các đợt công kích tiếp theo tăng thương tổn. | `[333,348,2,30,190]` | Duration `2`. Apply stores `v[0][1] = baseDefense * 30 / 100`, so defense `+30%`. Also stores `v[0][2] = 190 * B() / 100`, so extra damage is `190%` of source raw attack snapshot. | Defense up immediately. Source damage hook adds `v[0][2]` when `v[0][0] == 0`; normal source duration tick clears when counter reaches `0`, so this hook is covered as a source-edge forced state, not as ordinary expiry behavior. | Icon `12`; no P12/P13 body visual. Producer skill `4` uses normal P7 `effect.mid[4]` speffect `16 -> 15`. | PORTED | Dedicated producer visual, duration-0 hook, and expiry clear smokes now pass. |
| 1 | Phá Phủ | Tăng thương tổn, phòng ngự giảm, duy trì nhiều hiệp. | `[334,349,3,50,50]` | Duration `3`. `v[1][1] = baseDefense * 50 / 100`, defense `-50%`. `v[1][2] = 50`, outgoing damage `+50%`. | Self defense down, outgoing damage up. | Icon `13`; no body visual. Producer skill `5` uses normal P7 `effect.mid[5]` speffect `16 -> 15`. | PORTED | Dedicated producer visual, forced hit, forced miss, forced crit, and expiry clear smokes now pass. |
| 2 | Kinh Cức | Tăng phòng ngự, phản lại một phần thương tổn cho đối thủ. | `[335,350,3,30,10]` | Duration `3`. `v[2][1] = baseDefense * 30 / 100`, defense `+30%`. `v[2][2] = 10`, reflected damage `incomingDamage * 10 / 100`. | Defense up and post-hit counter/reflect damage. | Icon `14`; no body visual. Producer skill `14` uses normal P7 actor action row `effect.mid[14] = [0,0,21,1,-1,-1,0]`, source effect id `21`. | PORTED | Dedicated producer visual, forced hit reflect, forced miss no-reflect, forced crit reflect, and expiry clear smokes now pass. |
| 3 | Khôi phục | Mỗi hiệp khôi phục một lượng sinh mệnh. | `[336,351,3,5,-1]` | Duration `3`. `v[3][1] = maxHP * 5 / 100`. Heal `5% maxHP` on apply and each P12/P13 tick. | HP heal over time. | Icon `15`; P12/P13 body visual exists. Producer skill `15` uses normal P7 actor action + speffect sequence from `effect.mid[15]`. | PORTED | Dedicated producer/apply heal, P12/P13 body visual, P12/P13 heal tick, and expiry clear smokes now pass. |
| 4 | Phòng ngự | Tăng phòng ngự trong nhiều hiệp. | `[337,352,2,-1,-1]` | Duration `2`. Row param is sentinel `-1`; source uses producer `skill[8]`. Skills `21/27` use `skill[8] = 10`, so defense `+10% baseDefense`. | Self defense up from q()/post-skill buff. | Icon `16`; no body visual. | PORTED/PARTIAL | Phase 9-R raw damage + self-buff smokes cover producer. Needs expiry/reapply smoke if source-cleaning stat stack. |
| 5 | Vô hình | Đối thủ có tỉ lệ bị phản lại chính thương tổn. | `[338,353,3,30,-1]` | Duration `3`. `v[5][1] = 30`. If holder is hit, roll `<= 30`; on pass, source stores incoming damage and later applies it back to attacker. | Reflect/store damage chance. | Icon `17`; P12/P13 body visual exists. | PORTED/PARTIAL | Phase 9-AA covers defensive hook. Keep visual + reflect regression. |
| 6 | Kiên nhẫn | Có cơ hội làm tỉ lệ công kích của kẻ thù chỉ còn một nửa theo mô tả. | `[339,354,3,50,-1]` | Duration `3`. `v[6][1] = 50`, `v[6][2] = -1`. Source branch is odd: if target has buff6 and roll `<= 50`, formula reads attacker-side `v[6]` and applies `damage * v[6][2] / 100`. | Source oddity retained; do not redesign without original proof. | Icon `18`; no body visual. | SOURCE_ODDITY / PARTIAL | Phase 9-AA has controlled source-odd smoke. Needs careful handling only if source comparison proves intended behavior. |
| 7 | Linh Xảo | Tăng giá trị linh xảo/tốc độ trong nhiều hiệp. | `[340,355,2,-1,-1]` | Duration `2`. Row param is sentinel `-1`; source uses producer `skill[8]`. Skills `42/48` use `skill[8] = 5`, so speed `+5% baseSpeed`. | Self speed up. | Icon `19`; no body visual. | PORTED | Phase 9-R smokes cover producer. |
| 8 | Điện áp | Tăng tiêu hao kỹ năng và tăng thương tổn. | `[341,356,4,30,-1]` | Duration `4`. `v[8][1] = 30`. Outgoing damage `+30%`; skill PP consumption subtracts one extra PP while active. | Damage up plus PP cost increase. | Icon `20`; no P12/P13 body visual by source gate. Producer skill `44` uses normal P7 `effect.mid[44]` speffect `19 -> 15`. | PORTED | Dedicated producer visual, PP/damage, and expiry smokes now pass. |
| 9 | Hóa Thạch | Tăng linh xảo/tốc độ, giảm phòng ngự. | `[342,357,3,50,50]` | Duration `3`. `v[9][1] = baseSpeed * 50 / 100`, speed `+50%`. `v[9][2] = baseDefense * 50 / 100`, defense `-50%`. | Speed up and defense down. | Icon `21`; no body visual. | PORTED/PARTIAL | Covered by skill45/P13 order and stat assertions. |
| 10 | Man Lực | Tăng lực lượng/công kích trong nhiều hiệp. | `[343,358,2,-1,-1]` | Duration `2`. Source row param is `-1`; bytecode/rebuild use `baseAttack * (-1) / 100`, so current source-shaped value is about `-1% attack`, despite text saying attack up. | Attack stat hook with source oddity. | Icon `22`; no body visual. | PORTED-AS-SOURCE / SOURCE_ODDITY | Existing buff10 audit/smoke proves source row behavior. Do not "fix" to positive without source proof. |
| 11 | Thâu Thủ | Chuyển trạng thái có lợi của đối thủ sang bản thân. | `[344,359,3,-1,-1]` | Duration `3`. `v[11][1] = selected donor index`. Source copies active buffs from selected donor, then clears donor buffs. | Buff steal/copy/clear. | Icon `23`; no body visual. | PARTIAL | Phase 9-W covers selected-index copy/clear in simplified battle vectors. Full multi-target donor parity remains later. |
| 12 | Gia Tốc | Mỗi hiệp công kích hai lần; lần thứ hai lặp lại kỹ năng trước và không tốn kỹ năng. | `[345,360,2,-1,-1]` | Duration `2`. Apply sets `K[12] = 1`; tick sets `K[12] = 2`. No direct stat value. | Follow-up attack/PP conservation state. | Icon `24`; no body visual. | PORTED/PARTIAL | Phase 9-AB producer-to-consumer smoke exists. Full turn interleaving remains sensitive. |
| 13 | Thạch Hóa | Giải phóng trạng thái dị thường, mỗi hiệp hồi `20%` HP tối đa trong 3 hiệp. | `[346,361,3,20,-1]` | Duration `3`. `v[13][1] = maxHP * 20 / 100`. Clear all debuffs, heal `20% maxHP` on apply and tick. | Cleanse plus heal over time. | Icon `25`; P12/P13 body visual exists. | PORTED | Phase 9-Z covers cleanse/heal. |
| 14 | Thạch Phu | Giải phóng trạng thái dị thường và miễn dịch trạng thái dị thường trong 3 hiệp. | `[347,362,3,-1,-1]` | Duration `3`. No numeric stat delta. Clears all debuffs on apply; target debuff apply path blocks while buff14 is active. | Cleanse plus debuff immunity/protection. | Icon `26`; no body visual. | PORTED/PARTIAL | `battle_status_buff14_blocks_debuff`; battle quick covers core logic. |

Source note: decompiled `game.b.a(byte,int,int)` has a `case 15`, but decoded `aq.c[6]` currently has rows `0..14` only. Treat buff id `15` as source-control residue / UNKNOWN, not a usable gameplay row.

## Target Debuff Matrix - `aq.c[7]`

| Id | Name | Source description | Raw row | Exact numbers / formula | Main runtime effect | Visual/icon | Current status | Smoke/coverage |
| ---: | --- | --- | --- | --- | --- | --- | --- | --- |
| 0 | Gieo Hạt | Mỗi hiệp giảm bớt `X` điểm HP trong `Y` hiệp. | `[311,322,3]` | Duration `3`. Apply stores `w[0][1] = preSkillRawDamage`. Tick damage is `max(1, w[0][1] / skill[8])`. Skill `1` uses divisor `4`; skill `7` uses divisor `3`. | Damage over time; also enables conditional damage for skills `3/9`. | Icon `1`; P12/P13 body visual exists. | PORTED | `battle_status_debuff0_damage_tick`, P12 queue smokes, battle quick coverage. |
| 1 | Mê Muội | Source text describes a confusion-like abnormal state lasting `Y` turns. | `[312,323,2]` | Duration `2`. No stored numeric value in source apply switch. Conditional skills `23/29` use stronger branch if target has debuff1. Catch status multiplier index `1` uses `11/10`. | Flag/status hook; no per-turn HP/stat tick. | Icon `2`; P12/P13 body visual exists. | PORTED/PARTIAL | Phase 9-E covers success/block/miss; conditional skill smokes exist. |
| 2 | Quấn Quanh | Target cannot switch; player also cannot run/use items in source text. | `[313,324,3]` | Duration `3`. No stored numeric value in source apply switch. Catch status multiplier index `2` uses `12/10`. Runtime command-disable parity is still not fully closed. | Bind/command-lock flag and catch modifier. | Icon `3`; P12/P13 body visual exists. | PORTED/PARTIAL | Phase 9-F covers producer and visuals; command disable still needs dedicated P4/P5/P10 smoke. |
| 3 | Thực Loại | After `Y` turns, receives `X` damage; if death occurs the effect disappears. | `[314,325,3]` | Duration `3`. Apply stores `w[3][1] = preSkillRawDamage`. On last tick, damage is `max(1, w[3][1] * skill[8] / 100)`. Skill `13` uses `150%`; skill `19` uses `200%`. | Delayed HP damage near expiry. | Icon `4`; P12/P13 body visual exists. | PORTED | Phase 9-G plus P12 delayed damage smokes. |
| 4 | Mục | Accuracy/reduction effect lasting `Y` turns. | `[315,326,3]` | Duration `3`. Apply stores `w[4][1] = skill[8]`. Skill `31` stores `1`; skill `37` stores `2`. Miss/evasion path subtracts this value from affected unit speed when it later attacks. | Accuracy/miss chance hook via effective speed. | Icon `5`; no body visual. | PORTED/PARTIAL | Phase 9-H covers producer and miss chance. Needs keep in battle quick if miss math changes. |
| 5 | Chậm Chạp | Temporary value decreases for `Y` turns. | `[316,327,3]` | Duration `3`. Apply stores `w[5][1] = baseSpeed * skill[8] / 100`. Skills `32/38` use `10%`; skill `61` uses `5%`; skill `67` source gap was smoke-covered separately. | Speed down. | Icon `6`; no body visual. | PORTED | `battle_status_debuff5_speed_down`; Phase 9-I. |
| 6 | Nhụt Chí | Damage ratio decreases for `Y` turns. | `[317,328,3]` | Duration `3`. Apply stores `w[6][1] = skill[8]`. Skills `33/39` use `10`, so affected unit outgoing damage is reduced by `damage * 10 / 100`. | Damage output down. | Icon `7`; no body visual. | PORTED/PARTIAL | Phase 9-J covers producer and damage reduction. |
| 7 | Phòng Ngự | Defense value decreases for `Y` turns. | `[318,329,3]` | Duration `3`. Apply stores `w[7][1] = baseDefense * skill[8] / 100`. Skills `51/57` use `20%`, so defense `-20%`. | Defense down. | Icon `8`; no body visual. | PORTED | `battle_status_debuff7_defense_down`; Phase 9-K. |
| 8 | Quỷ Mị | Text says only by exploiting opponent attack can self attack opponent; exact route is special. | `[319,330,4]` | Duration `4`. Skill `54` has table effect id `8`, but source P7 zero-power guard means normal damage/debuff producer does not behave like a regular direct hit. Do not assign stat/damage semantics without more source proof. | Special flag/route, currently no proven gameplay consumer beyond active flag/visual possibility. | Icon `9` if active; P12/P13 body visual exists if queued. | FLAG/PENDING | Phase 9-M smoke proves no fake damage/debuff path for zero-power row. Needs focused source consumer audit before porting behavior. |
| 9 | Hỗn Loạn | Cannot switch own pet, lasts `Y` turns. | `[320,331,1]` | Duration `1`. Skill `55` has table effect id `9`, but zero-power guard prevents treating it as ordinary damage/debuff. | Switch-lock/confusion-like flag, full P5 command parity not closed. | Icon `10` if active; P12/P13 body visual exists if queued. | FLAG/PENDING | Phase 9-M smoke exists. Needs P5 disabled/switch-lock source route audit before code. |
| 10 | Tê Liệt | Each action costs extra wait time. | `[321,332,4]` | Duration `4`. No stored numeric value in source apply switch. Skills `41/47` apply this family with chance param `10`. Catch status multiplier index `3` uses `12/10`. | Paralysis/action-delay/catch modifier flag. | Icon `11`; P12/P13 body visual exists. | PORTED/PARTIAL | Phase 9-L covers producer, visual, and catch multiplier; exact action-delay timing is still pending. |

## Producer Skill Quick Map

| Effect | Producer skills | Key source parameter |
| --- | --- | --- |
| buff0 | `4` | row fixed: defense `+30%`, stored extra damage `190% B()` |
| buff1 | `5` | row fixed: defense `-50%`, damage `+50%` |
| buff2 | `14` | row fixed: defense `+30%`, reflect `10%` |
| buff3 | `15` | row fixed: heal `5% maxHP` |
| buff4 | `21, 27` | `skill[8] = 10`, defense `+10%` |
| buff5 | `34` | row fixed: reflect chance `30%` |
| buff6 | `35` | row fixed: source oddity `50`, `-1` |
| buff7 | `42, 48` | `skill[8] = 5`, speed `+5%` |
| buff8 | `44` | row fixed: damage `+30%`, extra PP cost |
| buff9 | `45` | row fixed: speed `+50%`, defense `-50%` |
| buff10 | `62, 68` | row fixed sentinel `-1`, source-shaped attack value |
| buff11 | `64` | selected donor index copy/clear |
| buff12 | `65` | follow-up state `K12` |
| buff13 | `24` | heal `20% maxHP`, cleanse |
| buff14 | `25` | cleanse + debuff immunity |
| debuff0 | `1, 7` | tick divisor `skill[8] = 4` or `3` |
| debuff1 | `2, 8, 22, 28` | explicit debuff chance family; flag/conditional |
| debuff2 | `12, 18` | bind/command-lock flag |
| debuff3 | `13, 19` | delayed damage `150%` or `200%` stored raw |
| debuff4 | `31, 37` | miss/speed value `1` or `2` |
| debuff5 | `32, 38, 61, 67` | speed down `10%`, `10%`, `5%`, `5%` |
| debuff6 | `33, 39` | outgoing damage `-10%` |
| debuff7 | `51, 57` | defense `-20%` |
| debuff8 | `54` | zero-power/special route; behavior pending |
| debuff9 | `55` | zero-power/special route; behavior pending |
| debuff10 | `41, 47` | chance param `10`, catch/action flag |

## Current High-Value Gaps

| Gap | Why it matters | Recommended next slice |
| --- | --- | --- |
| debuff2 command-lock parity | Text/source says cannot switch, run, or use item; battle command UI must respect it. | Audit P4/P5/P10 disabled checks with debuff2 active, then smoke warning/disabled state. |
| debuff9 switch-lock parity | Text says cannot change pet; current state is flag/pending. | Audit P5 disabled branch with debuff9 active, then smoke valid/current/dead/back interactions. |
| buff11 donor vector | Rebuild has simplified selected-target copy/clear; full source has party/vector assumptions. | Only revisit after P5/source party vectors are stable. |
| debuff10 action-delay timing | Catch multiplier is covered, but "lost wait time" turn scheduling is still partial. | Audit source state delay/action scheduling before changing runtime. |

## Next Recommended Port/Smoke

Completed slice: **buff0 Súc Lực visual + logic + expiry smoke**.

Smoke PNGs:

- `rebuild_game/build_intro_demo/battle_status_buff0_producer_visual.png`
- `rebuild_game/build_intro_demo/battle_status_buff0_duration0_damage_hook.png`
- `rebuild_game/build_intro_demo/battle_status_buff0_expiry_clears_defense.png`

Verified:

- Producer skill `4` plays normal P7 speffect chunks `16 -> 15`, applies buff0, shows post-effect text and HUD status icon `12`.
- Active logic stores defense `+30%`: debug setup `100 -> 130`.
- Active logic stores extra damage from `190% * B()`.
- Forced source-edge `v[0][0] == 0` damage hook works: baseline direct skill10 damage `80`, hooked damage `308`, formula `80 + 228`.
- Normal source expiry clears after 2 ticks via `game.b.d(buffId,slot)`: icon disappears and defense returns to `100`.

Previous completed slice: **buff8 Điện áp PP-cost + damage + expiry smoke**.

Smoke PNGs:

- `rebuild_game/build_intro_demo/battle_status_buff8_producer_visual.png`
- `rebuild_game/build_intro_demo/battle_status_buff8_pp_cost_damage_active.png`
- `rebuild_game/build_intro_demo/battle_status_buff8_expiry_clears_pp_damage.png`

Verified:

- Producer skill `44` plays normal P7 speffect chunks `19 -> 15`, applies buff8, shows post-effect text and HUD status icon `20`.
- While active, direct skill10 damage changes from baseline `80` to `104`, matching `damage + damage * 30 / 100`.
- While active, skill PP delta is `-2`: normal `-1` plus buff8 extra `-1`.
- After 4 source ticks via `game.b.o(8)+d(8,slot)`, buff8 clears, status icon disappears, damage returns to `80`, and PP delta returns to `-1`.

Next slice should follow table order: **buff1 Phá Phủ visual + logic + expiry smoke**.

Expected source checks:

- Producer skill `5`, raw skill row `[0,122,534,0,1,10,1,1,-1,1]`.
- Buff row `[334,349,3,50,50]`.
- Visual should use `effect.mid[5]`, same normal P7 shape as skill4 if source row confirms it.
- Logic: defense `-50%`, outgoing damage `+50%`.
- Expiry after 3 ticks clears icon and restores defense.

## 2026-07-13 Buff1 Closeout

Completed slice: **buff1 Pha Phu visual + logic + hit/miss/crit + expiry smoke**.

Smoke PNGs:

- `rebuild_game/build_intro_demo/battle_status_buff1_producer_visual.png`
- `rebuild_game/build_intro_demo/battle_status_buff1_forced_hit_damage_defense.png`
- `rebuild_game/build_intro_demo/battle_status_buff1_forced_miss_no_damage.png`
- `rebuild_game/build_intro_demo/battle_status_buff1_forced_crit_damage.png`
- `rebuild_game/build_intro_demo/battle_status_buff1_expiry_clears_damage_defense.png`

Verified:

- Producer skill `5` plays normal P7 speffect chunks `16 -> 15`, applies buff1, shows post-effect text and HUD status icon `13`.
- Active logic stores defense `-50%`: debug setup `100 -> 50`.
- Active logic stores outgoing damage bonus `+50%`: baseline direct skill10 damage `80`, buff1 hit damage `120`.
- Forced miss shows dodge, no damage text, no HP damage, while buff1 remains active.
- Forced crit with buff1 raises hit damage further: non-crit `120`, crit `180`, and critical visual flag is true only on hit.
- Normal source expiry clears after 3 ticks via `game.b.o(1)+d(1,slot)`: icon disappears, defense returns to `100`, damage returns to baseline `80`.

Next slice should follow table order: **buff2 Kinh Cuc visual + logic + expiry/counter smoke**.

Expected source checks:

- Producer skill `14`, raw skill row from `aq.c[1][14]`.
- Buff row `[335,350,3,30,10]`.
- Logic: defense `+30%`, reflect/counter damage `incomingDamage * 10 / 100`.
- Smoke should include visual producer, forced hit incoming damage/reflection, forced miss no reflection, and expiry after 3 ticks.

## 2026-07-13 Buff2 Closeout

Completed slice: **buff2 Kinh Cuc visual + logic + hit/miss/crit + expiry smoke**.

Smoke PNGs:

- `rebuild_game/build_intro_demo/battle_status_buff2_producer_visual.png`
- `rebuild_game/build_intro_demo/battle_status_buff2_forced_hit_reflect_defense.png`
- `rebuild_game/build_intro_demo/battle_status_buff2_forced_miss_no_reflect.png`
- `rebuild_game/build_intro_demo/battle_status_buff2_forced_crit_reflect.png`
- `rebuild_game/build_intro_demo/battle_status_buff2_expiry_clears_defense_reflect.png`

Verified:

- Producer skill `14` uses `effect.mid[14] = [0,0,21,1,-1,-1,0]`, starts actor action `u.a()` with `sourceEffectId=21`, applies buff2, shows post-effect text, and HUD status icon `14`.
- Active logic stores defense `+30%`: producer setup `100 -> 130`; reflect setup `40 -> 52`.
- Active logic stores reflect percent `10`: after a hit, source-shaped `game.d.q` hook applies `Z[0] * target.v[2][2] / 100` back to attacker.
- Forced hit smoke: direct skill10 damage `68`, reflect `6`, attacker HP `134 -> 128`.
- Forced miss smoke: dodge text is shown, enemy HP does not change, attacker HP stays `134`, and no buff2 reflect trace is emitted.
- Forced crit smoke: crit damage `102`, reflect `10`, proving reflect scales from final hit damage.
- Normal source expiry clears after 3 ticks via `game.b.o(2)+d(2,slot)`: icon disappears and defense returns to `100`.

Next slice should follow table order: **buff3 Khoi phuc visual + logic + expiry smoke**.

Expected source checks:

- Producer skill `15`, raw skill row from `aq.c[1][15]`.
- Buff row `[336,351,3,5,-1]`.
- Logic: heal `maxHP * 5 / 100` on apply and each P12/P13 tick.
- Smoke should include producer visual, heal amount before/after, active queue tick, and expiry after 3 ticks.

## 2026-07-13 Buff3 Closeout

Completed slice: **buff3 Khoi phuc visual + apply heal + P12/P13 tick + expiry smoke**.

Smoke PNGs:

- `rebuild_game/build_intro_demo/battle_status_buff3_producer_visual_apply_heal.png`
- `rebuild_game/build_intro_demo/battle_status_buff3_p12_body_visual_start.png`
- `rebuild_game/build_intro_demo/battle_status_buff3_p12_heal_tick.png`
- `rebuild_game/build_intro_demo/battle_status_buff3_expiry_clears_icon.png`

Verified:

- Producer skill `15` plays P7 actor action/speffect visual, applies buff3, shows post-effect text, and HUD status icon `15`.
- Apply heal uses `maxHP * 5 / 100`: max HP `134`, heal `6`, HP `67 -> 73`.
- P12/P13 body visual starts for buff bank `0`, effect id `3`.
- P12/P13 tick heals the same `6`: HP `68 -> 74` in the queue smoke after producer/setup timing, duration `3 -> 2`, duration cell `136`.
- Normal source expiry clears after 3 ticks via `game.b.o(3)+d(3,slot)`: icon disappears and duration reaches `0`.

Next slice should follow table order: **buff4 Phong ngu visual + logic + expiry smoke**.

Expected source checks:

- Producer skills `21` and `27`, raw skill rows from `aq.c[1]`.
- Buff row `[337,352,2,-1,-1]`.
- Logic uses producer `skill[8]`, not row param: known skills use defense `+10% baseDefense`.
- Smoke should include producer visual, defense increase, hit/miss/crit unaffected except via defense when targeted, and expiry after 2 ticks.
