# 238 - Battle Status Effectiveness Smoke Matrix

Date: 2026-07-13

Scope: source-first audit for battle status/effect effectiveness before doing
more per-skill polish.

No runtime code was changed for this document.

## Goal

Close the status/effect layer before returning to individual skill work.

A status is not considered good enough just because a slot or HUD icon appears.
For each source-called status we need proof that:

1. the status is applied to the correct owner: player pet or enemy pet;
2. the source slot/queue is correct;
3. the HUD icon/duration cell follows `game.h`;
4. the P12/P13 body visual appears only when `game.d` source gate calls it;
5. the gameplay logic actually changes HP, stat, damage, catch, miss, EXP, or
   turn behavior;
6. expiry/clear resets and reapplies stats without leaving stale values.

## Source Facts

### Three status systems

| System | Source | Unit storage | Queue | HUD icon | Body/P12 visual |
| --- | --- | --- | --- | --- | --- |
| Form/status | `aq.c[3]` | `game.b.c[5]` / `f(byte)` | none | no `x[][]` icon | no P12/P13 queue |
| Self buff | `aq.c[6]` | `game.b.v[16][5]` | `x[0][0..2]` | cell `buffId + 12` | only gated ids |
| Target debuff | `aq.c[7]` | `game.b.w[11][5]` | `x[1][0..2]` | cell `debuffId + 1` | only gated ids |

### HUD icon/duration source

Source: `game.h.a(b)` and `game.h.b(b)`.

| Condition | Icon cell | Duration cell |
| --- | ---: | ---: |
| empty | sprite `325`, cell `0` | `145` |
| buff id `id`, `v[id][0] > 0` | `id + 12` | `134 + v[id][0]` |
| debuff id `id`, `w[id][0] > 0` | `id + 1` | `134 + w[id][0]` |

Visible order:

```text
buff slot0, debuff slot0, buff slot1, debuff slot1, buff slot2, debuff slot2
```

### P12/P13 active visual gate

Source: `game.d` builds `af/ag/ah` from `x[0]` then `x[1]`, using:

```text
ai/ar = {{3,5,13}, {0,1,2,3,8,9,10}}
```

Therefore:

| Bank | P12/P13 visual ids | Logic-only ids |
| --- | --- | --- |
| buff bank `0` | `3,5,13` | `0,1,2,4,6,7,8,9,10,11,12,14` |
| debuff bank `1` | `0,1,2,3,8,9,10` | `4,5,6,7` |

Absence of P12/P13 visual does not mean absence of logic or HUD icon.

## Vietnamese Source Text

Source UI does not invent these labels. It calls `an.f(...)` with ids from
`aq.c` rows:

- form/status `aq.c[3]`: name `row[0]`, description `row[2]`;
- self buff `aq.c[6]`: name `row[0]`, description `row[1]`;
- target debuff `aq.c[7]`: name `row[0]`, description `row[1]`.

### Form/status `aq.c[3]` Vietnamese text

| Id | Name text id | Name | Description text id | Source description | Raw row |
| ---: | ---: | --- | ---: | --- | --- |
| 0 | 213 | Mạn Đà La Thạch | 237 | Sủng vật hiện tại trị số sinh mạng thấp hơn 30%, gia tăng lực công kích 100%; | `[213,1,237,5,1,30,100]` |
| 1 | 214 | Hồng Sắc Hải Loa | 238 | Sủng vật gia tăng lực công kích 10%; | `[214,2,238,5,1,10]` |
| 2 | 215 | Quy Xác Toái Phiến | 239 | Sủng vật Gia tăng khả năng phòng ngự 15%; | `[215,3,239,5,1,15]` |
| 3 | 216 | Ô Nha Uế | 240 | Gia tăng sức đối kháng 20%; | `[216,4,240,5,1,20]` |
| 4 | 217 | Viễn Cổ Long Cốt | 241 | Sủng vật gia tăng tỉ số bạo kích 10%; | `[217,5,241,5,1,10]` |
| 5 | 218 | Mật Phong Sào | 242 | Sau khi chiến đấu sủng vật lấy được giá trị kinh nghiệm 20%; | `[218,6,242,5,1,20]` |
| 6 | 219 | Ký Cư Giải Xác | 243 | Mang theo đạo cụ sủng vật tuy không tham chiến cũng có thể đạt được giá trị kinh nghiệm tương ứng; | `[219,7,243,5,1,100]` |
| 7 | 220 | Linh Trùng Thi Hài | 244 | Giao tranh bắt đầu; | `[220,8,244,5,1]` |
| 8 | 221 | Hấp Huyết Đằng Mạn | 245 | Sủng vật công kích là cách tốt nhất để khôi phục lại một phần trị số sinh mạng; | `[221,9,245,5,1,10,20]` |
| 9 | 222 | Cá Thờn Bơn | 246 | Sủng vật công kích nhất định trúng mục tiêu; | `[222,10,246,5,1]` |
| 10 | 223 | Cảm Lãm Chi Diệp | 247 | Bất luận đối thủ đã bị thương thế nào, trị số sinh mạng cũng sẽ không thấp hơn 10 điểm (bắt được dùng); | `[223,11,247,5,1,10]` |
| 11 | 224 | Sủng vật lôi đạt | 248 | Gia tăng tỷ lệ bắt được sủng vật 20%; | `[224,12,248,5,1,20]` |
| 12 | 225 | Tinh Nguyên Thạch | 249 | Tài liệu liên quan đến tiến hóa; | `[225,13,249,1000,0]` |
| 13 | 226 | Thiên Giới Tinh Thạch | 250 | Tài liệu liên quan tiến hóa bậc cao; | `[226,14,250,2,1]` |
| 14 | 227 | Thiên Địa Thần Thạch | 251 | Tài liệu liên quan đến tiến hóa bậc cao hiếm có; | `[227,15,251,10,1]` |
| 15 | 228 | Hồn Tinh Thạch | 252 | Tài liệu liên quan đến dị hoá; | `[228,16,252,2,1]` |
| 16 | 229 | Quỷ Thần Tinh Thạch | 253 | Tài liệu liên quan đến dị hóa bậc cao hiếm có; | `[229,17,253,10,1]` |
| 17 | 363 | Chìa khóa | 364 | Có thể mở hoàng kim bảo rương; | `[363,52,364,1,1]` |

### Self buff `aq.c[6]` Vietnamese text

| Id | Name text id | Name | Description text id | Source description | Raw row |
| ---: | ---: | --- | ---: | --- | --- |
| 0 | 333 | Súc Lực | 348 | Sự chuyển hướng không công kích, nhưng cải thiện khả năng phòng ngự, những đợt công kích tiếp theo sẽ làm gia tăng thương tổn. | `[333,348,2,30,190]` |
| 1 | 334 | Phá Phủ | 349 | Gia tăng thương tổn, khả năng phòng ngự giảm, duy trì liên tục Y hiệp. | `[334,349,3,50,50]` |
| 2 | 335 | Kinh Cức | 350 | Khả năng phòng ngự tự gia tăng, công kích ngược lại đối thủ tạo thành thương tổn, duy trì liên tục Y hiệp. | `[335,350,3,30,10]` |
| 3 | 336 | Khôi phục | 351 | Mỗi hiệp khôi phục trị số sinh mạng nhất định, duy trì liên tục Y hiệp. | `[336,351,3,5,-1]` |
| 4 | 337 | Phòng ngự | 352 | Gia tăng giá trị phòng ngự, duy trì liên tục Y hiệp. | `[337,352,2,-1,-1]` |
| 5 | 338 | Vô hình | 353 | Công kích của đối thủ sẽ có một phần tỷ lệ dội ngược lại chính mình, duy trì liên tục Y hiệp. | `[338,353,3,30,-1]` |
| 6 | 339 | Kiên nhẫn | 354 | Cơ hội tiếp xúc để công kích kẻ thù tỷ lệ chỉ là một nửa, duy trì liên tục Y hiệp. | `[339,354,3,50,-1]` |
| 7 | 340 | Linh Xảo | 355 | Gia tăng giá trị Linh Xảo, duy trì liên tục Y hiệp. | `[340,355,2,-1,-1]` |
| 8 | 341 | Điện áp | 356 | Gia tăng tiêu hao giá trị kỹ năng, làm gia tăng thương tổn, duy trì liên tục Y hiệp. | `[341,356,4,30,-1]` |
| 9 | 342 | Hỏa Thạch | 357 | Gia tăng giá trị Linh Xảo, phòng ngự giá trị giảm xuống, duy trì liên tục Y hiệp. | `[342,357,3,50,50]` |
| 10 | 343 | Man Lực | 358 | Lực lượng giá trị Gia tăng, duy trì liên tục Y hiệp. | `[343,358,2,-1,-1]` |
| 11 | 344 | Thâu Thủ | 359 | Trạng thái thuận lợi của đối thủ sẽ được chuyển qua cho bạn, duy trì liên tục Y hiệp. | `[344,359,3,-1,-1]` |
| 12 | 345 | Gia Tốc | 360 | Mỗi hiệp công kích 2 lần, duy trì liên tục Y hiệp, lần thứ hai không cần lựa chọn kỹ năng, lặp lại kỹ năng lần trước đó, hơn nữa không cần thiết hao tổn kỹ năng. | `[345,360,2,-1,-1]` |
| 13 | 346 | Thạch Hóa | 361 | Giải phóng các trạng thái dị thường, mỗi hiệp khôi phục 20% hạn mức cao nhất của trị số sinh mạng, duy trì liên tục 3 hiệp. | `[346,361,3,20,-1]` |
| 14 | 347 | Thạch Phu | 362 | Giải phóng các trạng thái dị thường, sẽ có 3 hiệp miễn dịch ở những nơi có trạng thái dị thường. | `[347,362,3,-1,-1]` |

### Target debuff `aq.c[7]` Vietnamese text

| Id | Name text id | Name | Description text id | Source description | Raw row |
| ---: | ---: | --- | ---: | --- | --- |
| 0 | 311 | Gieo Hạt | 322 | Mỗi hiệp giảm bớt X điểm trị số sinh mạng, duy trì liên tục Y hiệp. | `[311,322,3]` |
| 1 | 312 | Mê Muội | 323 | Gia tăng tốc độ công kích đối thủ có thể sử dụng đạo cụ, duy trì liên tục Y hiệp. | `[312,323,2]` |
| 2 | 313 | Quấn Quanh | 324 | Gia tăng tốc độ, sủng vật không thể thay thế, bạn cũng không thể trốn chạy, không thể sử dụng đạo cụ, ...duy trì liên tục Y hiệp. | `[313,324,3]` |
| 3 | 314 | Thực Loại | 325 | Sau Y hiệp đã bị X đả thương, nếu tử vong thì hiệu quả Thực Loại biến mất. | `[314,325,3]` |
| 4 | 315 | Mục | 326 | Hiệu quả giảm chỉ duy trì liên tục Y hiệp. | `[315,326,3]` |
| 5 | 316 | Chậm Chạp | 327 | Giá trị tạm thời giảm xuống, duy trì liên tục Y hiệp. | `[316,327,3]` |
| 6 | 317 | Nhụt Chí | 328 | Tỉ suất thương tổn giảm xuống, duy trì liên tục Y hiệp. | `[317,328,3]` |
| 7 | 318 | Phòng Ngự | 329 | Giá trị phòng ngự giảm xuống, duy trì liên tục Y hiệp. | `[318,329,3]` |
| 8 | 319 | Quỷ Mị | 330 | Lợi dụng sự công kích của đối thủ mới có thể chính mình công kích được đối phương. | `[319,330,4]` |
| 9 | 320 | Hỗn Loạn | 331 | Không thể thay đổi sủng vật của mình, duy trì liên tục Y hiệp. | `[320,331,1]` |
| 10 | 321 | Tê Liệt | 332 | Mỗi lần hành động lại mất không ít thời gian chờ đợi. | `[321,332,4]` |

## Current Rebuild Foundation

| Area | Current status | Evidence |
| --- | --- | --- |
| Storage `v/w/x/N` | PORTED/PARTIAL | `VqsvBattleUnit.buffSlots`, `debuffSlots`, `activeEffectQueue` |
| Apply/tick logic | PORTED/PARTIAL | `applySourceBuff`, `maybeApplyTargetDebuff`, `tickSourceBuff`, `tickSourceDebuff` |
| HUD icon bridge | PORTED/PARTIAL | `SourceBattleRuntime.syncStatusSlots`, `VqsvBattleRenderer.drawStatusSlots` |
| P12/P13 visuals | PORTED/PARTIAL | active queue visual rows from `bufDebuf.mid`; targeted AH renderer |
| Normal P7 visuals | PORTED/PARTIAL | Phase 10 normal P7 AH type coverage |
| Full effectiveness coverage | PARTIAL | many ids have formula tests, but not one status-first smoke suite |

## Status Effectiveness Matrix

### Form/status `aq.c[3]`

These are not queue effects and do not use sprite `325` HUD slots.

| Id | Source row | Effect owner | Gameplay logic | Current proof | Status-first smoke needed |
| ---: | --- | --- | --- | --- | --- |
| 0 | `[213,1,237,5,1,30,100]` | owner pet, either side | low HP attack boost in `game.b.B()` | PORTED/PARTIAL | high HP vs low HP damage comparison |
| 1 | `[214,2,238,5,1,10]` | owner pet, either side | attack boost in `game.b.B()` | PORTED/PARTIAL | owner damage increases by status param |
| 2 | `[215,3,239,5,1,15]` | target/owner defense path | target defense boost in raw damage source path | PORTED/PARTIAL | target with form2 takes less damage |
| 3 | `[216,4,240,5,1,20]` | target being debuffed | reduces/block debuff chance | PORTED/PARTIAL | forced debuff roll blocked/reduced |
| 4 | `[217,5,241,5,1,10]` | attacker | crit chance bonus | PORTED/PARTIAL | forced/seeded crit chance comparison |
| 5 | `[218,6,242,5,1,20]` | EXP participant | EXP multiplier | PORTED/PARTIAL | participant EXP gain comparison |
| 6 | `[219,7,243,5,1,100]` | reserve pet | reserve EXP share | PORTED/PARTIAL | reserve EXP smoke remains in EXP suite |
| 7 | `[220,8,244,5,1]` | unknown | no direct consumer proven in current docs | UNKNOWN/PENDING | source search before code |
| 8 | `[221,9,245,5,1,10,20]` | attacker after hit | post-hit self heal chance/percent in `game.d.q()` | PORTED/PARTIAL | hit smoke showing attacker heals |
| 9 | `[222,10,246,5,1]` | attacker | no-miss path sets missChance to `0` | PORTED/PARTIAL | forced high miss setup, status9 still hits |
| 10 | `[223,11,247,5,1,10]` | target/owner HP floor path | HP floor around lethal damage | PORTED/PARTIAL | lethal hit leaves floor HP |
| 11 | `[224,12,248,5,1,20]` | catcher/player context | catch chance boost | PORTED/PARTIAL | catch chance multiplier trace/smoke |
| 12..17 | material/key rows | non-battle | evolution/material/key inventory | NON_BATTLE | no battle smoke |

### Self buff `aq.c[6]`

All active buffs should show HUD icon while active. Only ids `3,5,13` should get
P12/P13 body visual.

| Id | Source row | Applies to | Gameplay logic | HUD | P12 visual | Current proof | Status-first smoke needed |
| ---: | --- | --- | --- | --- | --- | --- | --- |
| 0 | `[333,348,2,30,190]` | self, either side | defense up; delayed extra damage when counter reaches source condition | yes | no | PORTED/PARTIAL | formula/counter smoke if source skill uses it |
| 1 | `[334,349,3,50,50]` | self | lower own defense, outgoing damage boost | yes | no | PORTED | damage up + defense down smoke |
| 2 | `[335,350,3,30,10]` | self | defense up; target reflect hook currently uses slot2 | yes | no | PORTED/PARTIAL | reflect/defense smoke |
| 3 | `[336,351,3,5,-1]` | self | heal on apply and per tick | yes | yes | PORTED | required checkpoint A |
| 4 | `[337,352,2,-1,-1]` | self | skill-derived defense up | yes | no | PORTED/PARTIAL | stat reassert/expiry smoke |
| 5 | `[338,353,3,30,-1]` | self | reflect/store damage chance | yes | yes | PORTED/PARTIAL | visual + reflected damage smoke |
| 6 | `[339,354,3,50,-1]` | self/target oddity | damage reduction/conversion source oddity | yes | no | PORTED/PARTIAL | formula smoke both owner sides |
| 7 | `[340,355,2,-1,-1]` | self | skill-derived speed up | yes | no | PORTED | speed/order-relevant smoke |
| 8 | `[341,356,4,30,-1]` | self | extra PP cost and damage boost | yes | no | PORTED/PARTIAL | PP drain + damage boost smoke |
| 9 | `[342,357,3,50,50]` | self | speed up, defense down | yes | no | PORTED/PARTIAL | stat delta and turn hook smoke |
| 10 | `[343,358,2,-1,-1]` | self | attack up | yes | no | PORTED | required checkpoint B |
| 11 | `[344,359,3,-1,-1]` | self from selected donor | copy buffs then clear donor | yes | no | PARTIAL | donor vector smoke, multi-unit parity |
| 12 | `[345,360,2,-1,-1]` | self | K12 follow-up/PP conservation path | yes | no | PORTED/PARTIAL | producer-to-consumer smoke exists, keep in suite |
| 13 | `[346,361,3,20,-1]` | self | heal and clear debuffs on apply/tick | yes | yes | PORTED | visual + cleanse + heal smoke |
| 14 | `[347,362,3,-1,-1]` | self | clear debuffs and block new abnormal status | yes | no | PORTED/PARTIAL | required checkpoint C |

### Target debuff `aq.c[7]`

All active debuffs should show HUD icon while active. Only ids
`0,1,2,3,8,9,10` should get P12/P13 body visual.

| Id | Source row | Applies to | Gameplay logic | HUD | P12 visual | Current proof | Status-first smoke needed |
| ---: | --- | --- | --- | --- | --- | --- | --- |
| 0 | `[311,322,3]` | hit target | per-turn HP damage from stored raw/divisor; conditional skills 3/9 | yes | yes | PORTED | required checkpoint D |
| 1 | `[312,323,2]` | hit target | flag; conditional skills 23/29; catch multiplier in rebuild | yes | yes | PORTED/PARTIAL | flag visual + conditional/catch smoke |
| 2 | `[313,324,3]` | hit target | flag; blocks item/switch/run paths; catch multiplier | yes | yes | PORTED/PARTIAL | command disabled/catch smoke |
| 3 | `[314,325,3]` | hit target | delayed HP damage near expiry | yes | yes | PORTED | delayed tick smoke |
| 4 | `[315,326,3]` | hit target/attacker accuracy path | miss chance modifier through `sourceP7MissChance` | yes | no | PORTED/PARTIAL | miss chance comparison smoke |
| 5 | `[316,327,3]` | hit target | speed down | yes | no | PORTED | required checkpoint E |
| 6 | `[317,328,3]` | hit target | reduces incoming damage percent | yes | no | PORTED/PARTIAL | damage reduction smoke |
| 7 | `[318,329,3]` | hit target | defense down | yes | no | PORTED | required checkpoint F |
| 8 | `[319,330,4]` | hit target if reachable | zero-power skill table points here, but source reach still special | yes if active | yes | FLAG/PENDING | source route proof before logic claim |
| 9 | `[320,331,1]` | hit target if reachable | zero-power skill table points here; text says cannot switch | yes if active | yes | FLAG/PENDING | source route proof + P5 disabled smoke |
| 10 | `[321,332,4]` | hit target | catch/status/action multiplier family | yes | yes | PORTED/PARTIAL | required checkpoint G |

## Required Deterministic Smoke Checkpoints

These checkpoints should be added/run before returning to per-skill work. They
are intentionally status-first, not skill-polish-first.

| Checkpoint | Setup | Expected visual | Expected logic |
| --- | --- | --- | --- |
| `battle_status_buff3_heal_tick` | player or enemy has buff3, damaged HP, active queue slot | HUD cell `15`, duration `134+n`, P12 visual | HP increases by stored heal; duration decrements |
| `battle_status_buff10_attack_up_damage` | attacker has buff10 | HUD cell `22` | next direct damage increases vs baseline |
| `battle_status_buff14_blocks_debuff` | target has buff14, incoming debuff skill | HUD cell `26` | `appliedDebuffId=-1`, no target debuff slot |
| `battle_status_debuff0_damage_tick` | target has debuff0 with stored raw/source skill | HUD cell `1`, P12 visual | HP decreases by source divisor; duration decrements |
| `battle_status_debuff5_speed_down` | target has debuff5 | HUD cell `6` | speed stat lower; miss/order-relevant trace changes |
| `battle_status_debuff7_defense_down` | target has debuff7 | HUD cell `8` | next direct damage increases vs baseline |
| `battle_status_debuff10_catch_multiplier` | wild target has debuff10 | HUD cell `11`, P12 visual | catch chance path uses debuff10 multiplier |
| `battle_status_form9_no_miss` | attacker has form/status9, target speed high | no HUD queue icon | miss chance becomes 0, forced high miss setup still hits |

Optional but important after the first eight:

| Checkpoint | Why |
| --- | --- |
| `battle_status_form0_low_hp_attack` | proves passive owner HP threshold logic |
| `battle_status_form4_crit_bonus` | proves crit status path separately from generic crit |
| `battle_status_form10_hp_floor` | proves lethal damage floor |
| `battle_status_buff13_cleanse_heal_visual` | proves heal + clear debuff + P12 visual together |
| `battle_status_debuff3_delayed_damage` | proves late damage timing |
| `battle_status_debuff2_command_disable` | proves status affects command availability, not just icon |

## Current Gaps To Fix Only If Smoke Fails

| Gap | Expected next action |
| --- | --- |
| A status has slot/icon but no behavior change | fix `VqsvBattleUnit` logic for that id only |
| Behavior works but icon/duration wrong | fix `syncStatusSlots` or renderer only |
| P12 visual appears for non-gated id | remove visual, keep logic/icon |
| P12 visual missing for gated id | fix `VqsvBattleRuntime` active queue visual mapping |
| Player/enemy side swapped | fix owner-side setup/sync, not formula |
| Expiry leaves stale stat | fix clear/reapply lifecycle only |
| Form/status id lacks source consumer | keep `UNKNOWN/PENDING`, do not invent behavior |

## Recommended Next Slice

Slice A code should be smoke-only/test-support first:

1. add the eight required checkpoints above to `VqsvSmokeHarness`;
2. run only those checkpoints headlessly;
3. document pass/fail in `239_battle_status_effectiveness_closeout.md`;
4. implement only the failed status paths, one group at a time.

Do not open the game client. Do not polish individual skill animations until the
status effectiveness suite is green enough.
