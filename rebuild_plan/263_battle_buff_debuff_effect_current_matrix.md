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
| 4 | Phòng ngự | Tăng phòng ngự trong nhiều hiệp. | `[337,352,2,-1,-1]` | Duration `2`. Row param is sentinel `-1`; source uses producer `skill[8]`. Skills `21/27` use `skill[8] = 10`, so defense `+10% baseDefense`. Source tick `game.b.o(4)` adds the stored defense value to current defense once more before duration decrement. | Self defense up from q()/post-skill buff; target-side defense reduces incoming hit damage. | Icon `16`; no P12/P13 body visual by source gate. | PORTED | Dedicated before, producer, hit, miss, crit, and expiry smokes now pass. |
| 5 | Vô hình | Đối thủ có tỉ lệ bị phản lại chính thương tổn. | `[338,353,3,30,-1]` | Duration `3`. `v[5][1] = 30`. If holder is hit, roll `<= 30`; on pass, source stores incoming damage in attacker scratch `K[5]`, then `game.d.q()` applies it back to attacker. | Reflect/store damage chance. | Icon `17`; P12/P13 body visual exists as type0 actor row `[0,23,0,-1]`. | PORTED | Dedicated producer, reflect success/fail, P12/P13 body visual, and expiry smokes now pass. |
| 6 | Kiên nhẫn | Có cơ hội làm tỉ lệ công kích của kẻ thù chỉ còn một nửa theo mô tả. | `[339,354,3,50,-1]` | Duration `3`. `v[6][1] = 50`, `v[6][2] = -1`. Source branch is odd, but user-approved rebuild gameplay intentionally uses target-side buff6: roll `<= 50` then incoming damage is halved. | INTENTIONAL GAMEPLAY FIX: target with buff6 has 50% proc chance; proc reduces incoming damage by 50%. | Icon `18`; producer visual is `effect.mid[35]` speffect `4 -> 17`; no P12/P13 body visual because source gate excludes id `6`. | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED | Dedicated before, producer, animation chunk0 type7, animation chunk1 type1, reduction success/fail, P12 no-body-visual, and expiry smokes pass. Pixel-perfect original comparison remains pending. |
| 7 | Linh Xảo | Tăng giá trị linh xảo/tốc độ trong nhiều hiệp. | `[340,355,2,-1,-1]` | Duration `2`. Row param is sentinel `-1`; source uses producer `skill[8]`. Skills `42/48` use `skill[8] = 5`, so speed `+5% baseSpeed`. | Raw damage skill plus self speed up. | Icon `19`; no P12/P13 body visual by source gate. | PORTED | Dedicated before, skill42/skill48 producer, P7 animation timeline, P12 no-body-visual, and expiry smokes now pass. |
| 8 | Điện áp | Tăng tiêu hao kỹ năng và tăng thương tổn. | `[341,356,4,30,-1]` | Duration `4`. `v[8][1] = 30`. Outgoing damage `+30%`; skill PP consumption subtracts one extra PP while active. | Damage up plus PP cost increase. | Icon `20`; no P12/P13 body visual by source gate. Producer skill `44` uses normal P7 `effect.mid[44]` speffect `19 -> 15`. | PORTED | Dedicated producer visual, PP/damage, and expiry smokes now pass. |
| 9 | Hóa Thạch | Tăng linh xảo/tốc độ, giảm phòng ngự. | `[342,357,3,50,50]` | Duration `3`. `v[9][1] = baseSpeed * 50 / 100`, speed `+50%`. `v[9][2] = baseDefense * 50 / 100`, defense `-50%`. | Speed up and defense down. | Icon `21`; no body visual by source gate. Producer skill `45` uses normal P7 `effect.mid[45]` speffect `19 -> 15`. | PORTED | Dedicated before, producer visual/stat, P7 animation timeline, P12 no-body-visual, and expiry smokes now pass. |
| 10 | Man Lực | Tăng lực lượng/công kích trong nhiều hiệp. | `[343,358,2,-1,-1]` | Source oddity remains audited: row param `-1` would make `attack 100 -> 99`. User-approved gameplay fix: duration `3`, attack bonus decays by remaining turn: `+15%`, `+10%`, `+5%`, then clears. Smoke locks `attack 100 -> 115 -> 110 -> 105 -> 100` and sample damage `80 -> 98` on turn 1. | Attack stat hook with intentional gameplay fix. | Producer skills `62/68` use identical source `effect.mid` row: actor action `26`, `speffect 0`, then `speffect 15`; icon `22`; no P12/P13 body visual. | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED | See `274_battle_buff10_man_luc_gameplay_fix_closeout.md`. Historical source-oddity proof stays in `273_battle_buff10_man_luc_source_oddity_closeout.md`. |
| 11 | Thâu Thủ | Chuyển trạng thái có lợi của đối thủ sang bản thân. | `[344,359,3,-1,-1]` | Duration `3`. `v[11][1] = selected donor index`. Source copies active buffs from selected donor, clears donor buffs, and repeats copy/clear during active tick `game.b.o(11)`. | Buff steal/copy/clear. | Icon `23`; no body visual. | PORTED/PARTIAL | See `275_battle_buff11_thau_thu_donor_vector_audit.md` and `276_battle_buff11_thau_thu_closeout.md`. Cast-time copy/clear, active tick re-steal, nonzero donor slot, donor switch cleanup, and stale status icon clear have focused smoke coverage. Multi-enemy/full source slot and broader KO/replacement cleanup remain partial. |
| 12 | Gia Tốc | Mỗi hiệp công kích hai lần; lần thứ hai lặp lại kỹ năng trước và không tốn kỹ năng. | `[345,360,2,-1,-1]` | Duration `2`. Apply sets `K[12] = 1`; tick sets `K[12] = 2`. No direct stat value. | Follow-up attack/PP conservation state. | Icon `24`; no body visual. | PORTED/PARTIAL | See `277_battle_buff12_gia_toc_audit_closeout.md`. Producer speffect16/15, K12 apply/tick/follow-up, PP conservation, icon/duration, no-body-visual, and expiry smokes pass. Full global turn-vector/multi-actor parity remains partial. |
| 13 | Thạch Hóa | Giải phóng trạng thái dị thường, mỗi hiệp hồi `20%` HP tối đa trong 3 hiệp. | `[346,361,3,20,-1]` | Duration `3`. `v[13][1] = maxHP * 20 / 100`. Clear all debuffs, heal `20% maxHP` on apply and tick. | Cleanse plus heal over time. | Icon `25`; P12/P13 body visual exists via active queue row `ap id=13 row=[1,17,0,-1]`. | PORTED | See `278_battle_buff13_thach_hoa_audit_closeout.md`. Dedicated before, skill24 actor22, speffect17, apply cleanse/heal, P13 body visual, P13 heal tick, and expiry smokes pass. Pixel-perfect original comparison remains pending. |
| 14 | Thạch Phu | Giải phóng trạng thái dị thường và miễn dịch trạng thái dị thường trong 3 hiệp. | `[347,362,3,-1,-1]` | Duration `3`. No numeric stat delta. Clears all debuffs on apply; target debuff apply path blocks while buff14 is active. | Cleanse plus debuff immunity/protection. | Icon `26`; no P12/P13 body visual by source gate. | PORTED | See `279_battle_buff14_thach_phu_audit_closeout.md`. Dedicated before, skill25 speffect4/17, apply cleanse/protect, debuff-family immunity, P13 no-body-visual/pre-clear, and expiry smokes pass. |

Source note: decompiled `game.b.a(byte,int,int)` has a `case 15`, but decoded `aq.c[6]` currently has rows `0..14` only. Treat buff id `15` as source-control residue / UNKNOWN, not a usable gameplay row.

## Target Debuff Matrix - `aq.c[7]`

| Id | Name | Source description | Raw row | Exact numbers / formula | Main runtime effect | Visual/icon | Current status | Smoke/coverage |
| ---: | --- | --- | --- | --- | --- | --- | --- | --- |
| 0 | Gieo Hạt | Mỗi hiệp giảm bớt `X` điểm HP trong `Y` hiệp. | `[311,322,3]` | Duration `3`. Apply stores `w[0][1] = preSkillRawDamage`. Tick damage is `max(1, w[0][1] / skill[8])`. Skill `1` uses divisor `4`; skill `7` uses divisor `3`. | Damage over time; also enables conditional damage for skills `3/9`. | Icon `1`; P12/P13 body visual exists. | PORTED | `battle_status_debuff0_damage_tick`, P12 queue smokes, battle quick coverage. |
| 1 | Mê Muội | Source text describes a confusion-like abnormal state lasting `Y` turns. | `[312,323,2]` | Duration `2`. No stored numeric value in source apply switch. Conditional skills `23/29` use stronger branch if target has debuff1. Catch status multiplier index `1` uses `11/10`. | Flag/status hook; no per-turn HP/stat tick. | Icon `2`; P12/P13 body visual exists. | PORTED/PARTIAL | Phase 9-E covers success/block/miss; conditional skill smokes exist. |
| 2 | Quấn Quanh | Target cannot switch; player also cannot run/use items in source text. | `[313,324,3]` | Duration `3`. No stored numeric value in source apply switch. Catch status multiplier index `2` uses `12/10`. `game.b.B()` raises bound target effective defense by `aq.c[3][2][5]=15%`. | Bind/command-lock flag, catch modifier, and normal-damage target-defense modifier. | Icon `3`; P12/P13 body visual exists. | PORTED | Closeout `284`: producers, speffect6/type8, no-op tick/expiry, catch multiplier, defense formula, command locks item/pet/run, non-lock skill/catch/shop, buff14 block. |
| 3 | Thực Loại | After `Y` turns, receives `X` damage; if death occurs the effect disappears. | `[314,325,3]` | Duration `3`. Apply stores `w[3][1] = preSkillRawDamage`. On last tick, damage is `max(1, w[3][1] * skill[8] / 100)`. Skill `13` uses `150%`; skill `19` uses `200%`. | Delayed HP damage near expiry. | Icon `4`; P12/P13 body visual exists via actor effect `21`. | PORTED | Closeout `286`: producers, actor body visual, tick1/tick2 no damage, final `150%/200%` damage, KO transition, and buff14 block pass. |
| 4 | Mục | Accuracy/reduction effect lasting `Y` turns. | `[315,326,3]` | Duration `3`. Apply stores `w[4][1] = skill[8]`. Skill `31` stores `1`; skill `37` stores `2`. Miss/evasion path subtracts this value from affected unit speed when it later attacks. | Accuracy/miss chance hook via effective speed. | Icon `5`; source `bufDebuf` has `[1,1,0,0,1,11,0,-1]` but `game.d.ai[1]` excludes id `4`, so P12/P13 skips body visual. | PORTED | Closeout `288`: producers, stored value `1/2`, P12 no-body-visual skip, no-op tick/expiry, miss chance values `12/14`, and buff14 block pass. |
| 5 | Chậm Chạp | Temporary value decreases for `Y` turns. | `[316,327,3]` | Duration `3`. Apply stores `w[5][1] = baseSpeed * skill[8] / 100`. Skills `32/38` use `10%`; skill `61` uses `5%`. Skill `67` has `skill[7]==5` but source bytecode routes it to default raw damage, so it is `NOT_REACHED` for debuff5. | Speed down immediately, reasserted on active queue tick, restored on expiry; lowered current speed feeds miss chance. | Icon `6`; source `bufDebuf` has `[1,0,0,-1,0,25,0,-1]` but `game.d.ai[1]` excludes id `5`, so P12/P13 skips body visual. | PORTED | Closeout `290`: producers `32/38/61`, skill67 no-debuff regression, P12 no-body-visual speed reassert, expiry speed restore, miss chance consumer, and buff14 block pass. |
| 6 | Nhụt Chí | Damage ratio decreases for `Y` turns. | `[317,328,3]` | Duration `3`. Apply stores `w[6][1] = skill[8]`. Skills `33/39` use `10`, so affected unit outgoing damage is reduced by `damage * 10 / 100`. | Damage output down; source-immediate debuff mutation still commits on P7 miss, while HP/debuff text remain gated by hit. | Icon `7`; source `bufDebuf` has `[1,12,0,-1]` but `game.d.ai[1]` excludes id `6`, so P12/P13 skips body visual. | PORTED | Closeout `292`: producers `33/39`, stored value `10`, miss source-immediate mutation, P12 no-body-visual no-op, expiry clear, outgoing damage `80 -> 72`, and buff14 block pass. |
| 7 | Phòng Ngự | Defense value decreases for `Y` turns. | `[318,329,3]` | Duration `3`. Apply stores `w[7][1] = baseDefense * skill[8] / 100`. Skills `51/57` use `20%`, so defense `-20%`. | Defense down. | Icon `8`; no body visual. | PORTED | `battle_status_debuff7_defense_down`; Phase 9-K. |
| 8 | Quỷ Mị | Text says only by exploiting opponent attack can self attack opponent; exact source route is special. | `[319,330,4]` | Duration `4`. Skill `54` has table effect id `8`; rebuild now intentionally applies it through a no-damage post-effect producer using source chance `40`, stores value `10`, and remains blocked by buff14. User-approved gameplay fix: attacker damage `+10%`, target roll `55%` self / `45%` opponent. | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED: unstable damage-up status. | Icon `9` if active; P12/P13 body visual exists if queued. | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED | Closeout `296`: skill54 producer apply, buff14 block, self-hit smoke `101 -> 111`, enemy-hit smoke `80 -> 88`, body visual, expiry. |
| 9 | Hỗn Loạn | Confusion-like random target status; user confirmed it does not block pet switching. | `[320,331,1]` | Duration `1`. Skill `55` has table effect id `9`; rebuild now intentionally applies it through a no-damage post-effect producer. Active consumer rebuilds target list through `game.d.f(attacker)` and chooses by `ae.a(G.size())`. | Random target route when attacking; P5 pet switch remains allowed. | Icon `10` if active; P12/P13 body visual exists if queued. | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED | Closeout `297`: skill55 producer apply, seeded random target consumer, body visual, expiry. Direct P5 lock is `NOT_APPLICABLE / USER_CONFIRMED_ALLOWED`; only debuff2 Quan Quanh blocks pet switching. |
| 10 | Tê Liệt | Each action costs extra wait time. | `[321,332,4]` | Duration `4`. No stored numeric value in source apply switch. Skills `41/47` apply this family with chance param `10`. Catch status multiplier index `3` uses `12/10`. Source search found no proven action-delay consumer for debuff slot `w[10]`; `game.d h.f((byte)10)` is held item/passive id `10`, not debuff10. | Catch modifier/status flag; action-delay scheduling remains source-unproven. | Icon `11`; P12/P13 body visual row `[1,19,0,-1,1,6,0,-1]` exists. | PORTED/PARTIAL | Closeout `299`: before, skill41/47 producer, P12/P13 body visual, catch multiplier, expiry clear. Action-delay is `NOT_FOUND_IN_PC_SOURCE / PENDING_SOURCE_PROOF`. |

## Producer Skill Quick Map

| Effect | Producer skills | Key source parameter |
| --- | --- | --- |
| buff0 | `4` | row fixed: defense `+30%`, stored extra damage `190% B()` |
| buff1 | `5` | row fixed: defense `-50%`, damage `+50%` |
| buff2 | `14` | row fixed: defense `+30%`, reflect `10%` |
| buff3 | `15` | row fixed: heal `5% maxHP` |
| buff4 | `21, 27` | `skill[8] = 10`, defense `+10%` |
| buff5 | `34` | row fixed: reflect chance `30%` |
| buff6 | `35` | user-approved gameplay fix: 50% proc, 50% incoming damage reduction |
| buff7 | `42, 48` | `skill[8] = 5`, speed `+5%` |
| buff8 | `44` | row fixed: damage `+30%`, extra PP cost |
| buff9 | `45` | row fixed: speed `+50%`, defense `-50%` |
| buff10 | `62, 68` | gameplay-fixed attack value: 3 turns, `+15% -> +10% -> +5% -> clear`; source sentinel `-1` documented as oddity |
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
| debuff8 | `54` | zero-power/special route; gameplay-fixed `+10%` damage and 55/45 target roll |
| debuff9 | `55` | zero-power/special route; random target when attacking, switch allowed |
| debuff10 | `41, 47` | chance param `10`, catch/action flag |

## Current High-Value Gaps

| Gap | Why it matters | Recommended next slice |
| --- | --- | --- |
| debuff2 switch-lock parity | Quan Quanh is the switch-lock status. | Keep command-lock smoke as the guard: item/pet/run warn, skill/catch/shop allowed. Do not add this lock to debuff9. |
| buff11 donor vector | Audit `275` plus closeout `276` cover source cast-time copy/clear, active tick re-steal, nonzero donor slot, and donor switch cleanup in focused smoke. | Keep as PORTED/PARTIAL for multi-enemy/full source `d[]` slot and broader KO/replacement cleanup; move to buff12 unless a route mismatch appears. |
| debuff10 action-delay timing | Catch multiplier, icon, visual, and expiry are covered, but no PC source consumer for debuff slot `w[10]` action-delay scheduling has been found. | Keep as `NOT_FOUND_IN_PC_SOURCE / PENDING_SOURCE_PROOF`; do not implement delay unless a real source callsite is found. |

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

Historical next at that time was **buff1 Phá Phủ visual + logic + expiry smoke**. Current next is buff11 donor-vector copy/clear audit.

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

Historical next at that time was **buff2 Kinh Cuc visual + logic + expiry/counter smoke**. Current next is buff11 donor-vector copy/clear audit.

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

Historical note: buff3 closeout below is complete. Current table-order next slice is buff11 donor-vector copy/clear audit; do not return to buff3 unless regression fails.

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

Historical next at that time was **buff4 Phong ngu visual + logic + expiry smoke**. Current next is buff11 donor-vector copy/clear audit.

Expected source checks:

- Producer skills `21` and `27`, raw skill rows from `aq.c[1]`.
- Buff row `[337,352,2,-1,-1]`.
- Logic uses producer `skill[8]`, not row param: known skills use defense `+10% baseDefense`.
- Smoke should include producer visual, defense increase, hit/miss/crit unaffected except via defense when targeted, and expiry after 2 ticks.

## 2026-07-13 Buff4 Closeout

Completed slice: **buff4 Phong ngu visual + logic + hit/miss/crit + expiry smoke**.

Smoke PNGs:

- `rebuild_game/build_intro_demo/battle_status_buff4_before_no_effect.png`
- `rebuild_game/build_intro_demo/battle_status_buff4_producer_visual_defense.png`
- `rebuild_game/build_intro_demo/battle_status_buff4_forced_hit_target_defense.png`
- `rebuild_game/build_intro_demo/battle_status_buff4_forced_miss_no_extra_side_effect.png`
- `rebuild_game/build_intro_demo/battle_status_buff4_forced_crit_no_wrong_multiplier.png`
- `rebuild_game/build_intro_demo/battle_status_buff4_expiry_clears_defense.png`

Verified:

- Source buff row is `[337,352,2,-1,-1]`; source ignores row params for the numeric value and uses producer `skill[8]`.
- Producer skill `21` row is `[2,138,550,80,0,45,1,4,10,0]`; producer skill `27` row is `[2,144,556,100,2,30,1,4,10,0]`. Both point to buff id `4` and `skill[8] = 10`.
- Producer visual is source-backed from `effect.mid`: skill `21` uses `[0,0,22,0,-1,-1,0, 1,1,5,0,-1,-1,0]`, so the smoke asserts `sourceEffectId=22` plus `speffect=5`. Skill `27` differs by using `speffect=7`; no code copied from buff0/1.
- Apply logic matches `game.b.a(... case 4)`: base defense `100`, stored value `10`, current defense `110`, duration `2`.
- HUD status uses icon cell `16`; duration cell is `136` at duration `2`, then `135` at duration `1`, then clears.
- Target-side defense impact is numeric: direct skill10 baseline damage `100`; with target buff4 defense `100 -> 110`, damage becomes `90`.
- Forced miss keeps HP unchanged and does not commit extra damage side effects; buff icon/duration remain active.
- Forced crit uses the defense-up result first: non-crit `90`, crit `135` (`90 * 3 / 2`), with no extra buff multiplier.
- Expiry follows source `game.b.o(4)`: apply `100 -> 110`, first source tick `110 -> 120` with duration `1`, second tick clears and restores defense to `100`.
- P12/P13 body visual is not required: `game.d.ai[0]` gate contains only buff ids `3,5,13`; buff4 is skipped even though `bufDebuf.mid` has reusable rows.

Regression status for this closeout is tracked in `rebuild_plan/266_battle_buff4_phong_ngu_closeout.md`.

Historical next at that time was **buff5 Vo hinh visual + logic + chance reflect smoke**. Current next is buff11 donor-vector copy/clear audit.

## 2026-07-13 Buff5 Closeout

Completed slice: **buff5 Vo hinh visual + logic + chance reflect + P12/P13 body visual + expiry smoke**.

Smoke PNGs:

- `rebuild_game/build_intro_demo/battle_status_buff5_producer_visual.png`
- `rebuild_game/build_intro_demo/battle_status_buff5_forced_reflect_success.png`
- `rebuild_game/build_intro_demo/battle_status_buff5_forced_reflect_fail.png`
- `rebuild_game/build_intro_demo/battle_status_buff5_p12_body_visual_start.png`
- `rebuild_game/build_intro_demo/battle_status_buff5_expiry_clears_icon.png`

Verified:

- Source buff row is `[338,353,3,30,-1]`; apply stores `v[5][1] = 30`.
- Producer skill `34` row is `[3,151,563,0,1,10,1,5,-1,1]`, so it applies self buff id `5` without direct damage.
- Producer visual is source-backed from `effect.mid[34] = [0,1,4,0,-1,-1,0]`, so the smoke asserts `speffect=4` and no damage/hitroll for skill34.
- HUD status uses icon cell `17`; duration cells are `137`, `136`, `135`, then clear.
- Reflect success path is deterministic in smoke: incoming skill10 damage `80`, forced `damage.buff5 roll=0 <= 30`, attacker HP `134 -> 54`.
- Reflect fail path is deterministic in smoke: incoming skill10 damage `80`, forced `damage.buff5 roll=99 > 30`, attacker HP stays `134`.
- P12/P13 body visual is required by source gate `game.d.ai[0] = {3,5,13}`. For buff5 the source table map is `ar[0][5] -> ap[6] = [0,23,0,-1]`, a type0 actor action, not an H/speffect row.
- Expiry follows duration `3`: icon `17/137 -> 17/136 -> 17/135 -> cleared`.

Regression status for this closeout is tracked in `rebuild_plan/267_battle_buff5_vo_hinh_closeout.md`.

Buff6 Kien nhan closeout:

- `effect.mid[35] = [0,1,4,0,-1,-1,0, 0,1,17,0,-1,-1,0]` is now smoke-locked as chunk0 AH type7 then chunk1 AH type1.
- Runtime apply is source-shaped: value `50`, secondary `-1`, duration `3`, icon `18`, duration cells `137 -> 136 -> 135 -> clear`.
- Damage hook is an intentional gameplay fix approved by the user: target-side buff6 rolls `<= 50`, then incoming damage is reduced by `50%`.
- Deterministic smoke proves baseline `80`, success roll `0` result `41` after half-damage plus normal jitter, and fail roll `99` result `80`.
- P12/P13 body visual is explicitly absent for buff6 because `game.d.ai[0]` does not include id `6`.

Historical next at that time was **buff7 Linh Xao dedicated closeout**. Current next is buff11 donor-vector copy/clear audit unless the user redirects.

## 2026-07-13 Buff7 Closeout

Completed slice: **buff7 Linh Xao dedicated closeout**.

Smoke PNGs:

- `rebuild_game/build_intro_demo/battle_status_buff7_before_no_effect.png`
- `rebuild_game/build_intro_demo/battle_status_buff7_producer_visual_speed_skill42.png`
- `rebuild_game/build_intro_demo/battle_status_buff7_producer_visual_speed_skill48.png`
- `rebuild_game/build_intro_demo/battle_status_buff7_p12_no_body_visual.png`
- `rebuild_game/build_intro_demo/battle_status_buff7_expiry_clears_speed.png`

Verified:

- Source buff row is `[340,355,2,-1,-1]`; duration `2`.
- Producer skill `42` row is `[4,159,571,90,0,45,1,7,5,0]`.
- Producer skill `48` row is `[4,165,577,130,3,15,1,7,5,0]`.
- Source `game.b.a case 7` stores `K[7] = sourceSkill`, reads producer `skill[8]`, and sets current speed to `baseSpeed + baseSpeed * skill[8] / 100`.
- Smoke fixes base speed to `100` to avoid low-stat integer truncation; both producers store value `5` and set speed `100 -> 105`.
- Producer visual is source-backed: both skills start with source effect id `24`; skill42 then uses speffect `1`, skill48 uses speffect `9`.
- HUD status uses icon cell `19`; duration cells are `136`, then `135`, then clear.
- P12/P13 body visual is not required: `game.d.ai[0]` gate contains only buff ids `3,5,13`; buff7 is skipped.
- Expiry follows duration `2`: speed `100 -> 105 -> 105 -> 100`, icon cleared.

## 2026-07-13 Buff9 Hoa Thach Closeout

Source facts now locked:

- Buff row: `aq.c[6][9] = [342,357,3,50,50]`.
- Producer skill: `45`.
- `game.b.a(byte,int,int)` case `9` stores `v[9][1] = baseSpeed * 50 / 100` and `v[9][2] = baseDefense * 50 / 100`.
- Source applies both stat deltas immediately: `d[4] = c[4] + v[9][1]`, `d[3] = c[3] - v[9][2]`.
- Producer visual is `effect.mid[45] = [0,1,19,0,-1,-1,0, 0,1,15,0,-1,-1,0]`, so P7 speffect order is `19 -> 15`.
- P12/P13 body visual is not expected because source gate `game.d.ai[0]` excludes buff id `9`.
- Icon cell is `21`; duration cells are `137 -> 136 -> 135 -> clear`.

Passing buff9 smoke checkpoints:

- `battle_status_buff9_before_no_effect`
- `battle_status_buff9_producer_visual_stats`\n- `battle_elder_p7_speffect45_start`\n- `battle_elder_p7_speffect45_overlay`\n- `battle_elder_p7_speffect45_type1`\n- `battle_elder_p7_speffect45_after`\n- `battle_status_buff9_p12_no_body_visual`
- `battle_status_buff9_expiry_clears_stats`

Verified smoke data:

- Before: base/current speed `100 -> 100`, base/current defense `100 -> 100`, no status icon.
- Producer skill45: no damage/hitroll; P7 speffects `19 -> 15`; buff9 active; speed `100 -> 150`; defense `100 -> 50`; icon `21/137`.
- P12/P13 tick: no body visual; active queue apply reduces duration `3 -> 2`; speed remains `150`; defense remains `50`; icon `21/136`.
- Expiry: after three source ticks, buff clears, icon disappears, speed restores to `100`, defense restores to `100`.

Smoke PNGs:

- `rebuild_game/build_intro_demo/buff9_closeout/battle_status_buff9_before_no_effect.png`
- `rebuild_game/build_intro_demo/buff9_closeout/battle_status_buff9_producer_visual_stats.png`\n- `rebuild_game/build_intro_demo/buff9_animation_timeline/battle_elder_p7_speffect45_start.png`\n- `rebuild_game/build_intro_demo/buff9_animation_timeline/battle_elder_p7_speffect45_overlay.png`\n- `rebuild_game/build_intro_demo/buff9_animation_timeline/battle_elder_p7_speffect45_type1.png`\n- `rebuild_game/build_intro_demo/buff9_animation_timeline/battle_elder_p7_speffect45_after.png`\n- `rebuild_game/build_intro_demo/buff9_closeout/battle_status_buff9_p12_no_body_visual.png`
- `rebuild_game/build_intro_demo/buff9_closeout/battle_status_buff9_expiry_clears_stats.png`

Next table-order note: buff14 `Thach Phu` is closed in `279_battle_buff14_thach_phu_audit_closeout.md` as PORTED. Buff table `aq.c[6]` rows 0..14 are closed in `280_battle_buff_table_0_14_closeout.md`, with buff6 and buff10 intentionally marked as user-approved gameplay deviations. Debuff0 `Gieo Hat` is closed in `282_battle_debuff0_gieo_hat_closeout.md`; debuff1 `Me Muoi` is closed in `283_battle_debuff1_me_muoi_closeout.md`; debuff2 `Quan Quanh` is closed in `284_battle_debuff2_quan_quanh_closeout.md`; debuff3 `Thuc Loai` is closed in `286_battle_debuff3_thuc_loai_closeout.md`; debuff4 `Muc` is closed in `288_battle_debuff4_muc_closeout.md`; debuff5 `Cham Chap` is closed in `290_battle_debuff5_cham_chap_closeout.md`; debuff6 `Nhut Chi` is closed in `292_battle_debuff6_nhut_chi_closeout.md`; debuff7 `Phong Ngu` is closed in `294_battle_debuff7_phong_ngu_closeout.md`. Next recommended lane is debuff8/debuff9 special-route audit because those rows are not ordinary direct stat debuffs.

## Latest Update - Debuff7 Closeout

`293_battle_debuff7_phong_ngu_audit.md` and
`294_battle_debuff7_phong_ngu_closeout.md` supersede the older Phase 9-K-only
coverage note for debuff7.

Debuff7 is now classified as `PORTED` with dedicated closeout smoke:

- producers `51/57`;
- stored value `baseDefense * skill[8] / 100`, so `20%` for current producers;
- current defense `100 -> 80` in the main producer probes;
- P7 miss keeps source-immediate debuff mutation but hides HP/debuff text;
- P12/P13 skips body visual because `game.d.ai[1]` excludes id `7`;
- expiry restores defense and clears icon;
- incoming damage probe proves the formula consumer;
- buff14 block regression passes.

## Latest Update - Debuff8/9 Special Route Audit

`295_battle_debuff8_9_special_route_audit.md` confirms that debuff8 and debuff9
must not be patched as ordinary direct-hit debuffs from skills `54/55`. Rebuild
now applies them through a no-damage post-effect producer for playable feedback.

Current status:

- skill54 visual: `PORTED/PARTIAL`;
- skill54 no-damage debuff8 producer: `INTENTIONAL_DEVIATION / GAMEPLAY_FIXED`;
- skill55 visual: `PORTED/PARTIAL`;
- skill55 no-damage debuff9 producer: `INTENTIONAL_DEVIATION / GAMEPLAY_FIXED`;
- debuff8 active gameplay consumer: `INTENTIONAL_DEVIATION / GAMEPLAY_FIXED`;
- debuff9 active target-routing consumer: `PORTED/PARTIAL`;
- debuff9 P5 switch-lock: `NOT_APPLICABLE / USER_CONFIRMED_ALLOWED`.

Debuff8 dedicated closeout is now present:

- `296_battle_debuff8_quy_mi_closeout.md`

Closeout checkpoints:

- `battle_status_debuff8_before_no_effect`
- `battle_status_debuff8_skill54_producer_apply`
- `battle_status_debuff8_skill54_buff14_blocks`
- `battle_status_debuff8_gameplay_fixed_self_hit_damage_up`
- `battle_status_debuff8_gameplay_fixed_enemy_hit_damage_up`
- `battle_status_debuff8_p12_body_visual_type1_actor25`
- `battle_status_debuff8_expiry_clears_icon`

It now proves skill54 is a zero-power visual/special route whose rebuild
gameplay applies debuff8 through a no-damage post-effect producer. Active
debuff8 gives outgoing damage `+10%` and routes 1v1 attacks by `55%` self-hit /
`45%` opponent-hit. This is intentionally marked `INTENTIONAL_DEVIATION /
GAMEPLAY_FIXED`, not source-parity.

Debuff9 dedicated closeout is now present:

- `297_battle_debuff9_hon_loan_closeout.md`

Closeout checkpoints:

- `battle_status_debuff9_before_no_effect`
- `battle_status_debuff9_skill55_producer_apply`
- `battle_status_debuff9_random_target_seeded_active`
- `battle_status_debuff9_p12_body_visual_type12`
- `battle_status_debuff9_expiry_clears_icon`

It now proves skill55 is a zero-power visual/special route whose rebuild
gameplay applies debuff9 through a no-damage post-effect producer, then proves
seeded active debuff9 random target routing, body visual, and expiry. The
current status is `INTENTIONAL_DEVIATION / GAMEPLAY_FIXED`: source route and
RNG index are ported for the consumer, but the producer was enabled for playable
feedback. Direct P5 switch-lock is `NOT_APPLICABLE / USER_CONFIRMED_ALLOWED`;
only debuff2 `Quan Quanh` blocks pet switching.

Debuff10 dedicated closeout is now present:

- `299_battle_debuff10_te_liet_closeout.md`

Closeout checkpoints:

- `battle_status_debuff10_before_no_effect`
- `battle_status_debuff10_skill41_producer_apply`
- `battle_status_debuff10_skill47_producer_apply`
- `battle_status_debuff10_p12_body_visual_type9`
- `battle_status_debuff10_catch_multiplier`
- `battle_status_debuff10_expiry_clears_icon`

It proves producer skills `41/47`, icon/duration, P12/P13 body visual, catch
multiplier, and expiry. The current status is `PORTED/PARTIAL`: action-delay
scheduling remains `NOT_FOUND_IN_PC_SOURCE / PENDING_SOURCE_PROOF` because the
only suspicious `game.d h.f((byte)10)` callsites are held/passive item id `10`,
not debuff10.

Next recommended step: close out debuff table `aq.c[7]` rows `0..10`, then move
to the next battle skill/effect roadmap phase.
