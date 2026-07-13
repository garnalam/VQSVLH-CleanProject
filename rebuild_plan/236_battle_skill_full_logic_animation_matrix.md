# 236 - Battle skill full logic/animation matrix

Date: 2026-07-13

Scope: source-first catalog for every pet skill in `aq.c[1][0..69]`.

Goal: before coding more skills one-by-one, keep one master sheet that lists:

- source skill name and description;
- raw `aq.c[1]` row;
- battle logic family;
- post-skill `game.d.q()` behavior;
- `effect.mid` chunks and `speffect.mid` / AH renderer type where visible;
- honest current status and the next coding strategy.

No runtime code was changed for this document.

## Source anchors

- `modules/script/decoded/data__script__db.mid.json`
  - group `1` is `aq.c[1]`, 70 skill rows.
- `modules/script/decoded/data__script__chs.mid.json`
  - skill name text id is `aq.c[1][skill][1]`;
  - skill description text id is `aq.c[1][skill][2]`.
- `modules/script/decoded/data__script__effect.mid.json`
  - one row per skill, split into 7-value effect chunks.
- `modules/script/decoded/data__script__speffect.mid.json`
  - special effect rows referenced when an effect chunk has `chunk[1] == 1`;
  - `speffect[row][0]` is the AH renderer type.
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
  - `game.b.b(target)` produces damage/debuff/crit result.
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
  - P7 animation/resolve and `game.d.q()` post-skill behavior.
- Existing phase docs:
  - `rebuild_plan/72_battle_full_skill_status_behavior_classification.md`
  - `rebuild_plan/156_battle_phase9_skill_coverage_matrix.md`
  - `rebuild_plan/187_battle_phase9ad_skill_coverage_closeout.md`

## Skill row schema

`aq.c[1][skill]` is:

```text
[element, nameText, descText, power, learnTier, pp, effectMode, effectId, param, targetMode]
```

Current interpretation:

| Column | Meaning |
|---:|---|
| `0` | element/family id |
| `1` | name text id |
| `2` | description text id |
| `3` | power/percent or `0` for no-damage skills |
| `4` | learn tier / unlock tier |
| `5` | max PP / skill value |
| `6` | effect mode: `0` none/direct, `1` self/buff/no-damage path, `2` target debuff/effect chance path |
| `7` | effect/debuff/buff id, or `-1` |
| `8` | extra parameter: chance, divisor, percent, follow-up rate, etc. |
| `9` | target mode; source P6/P7 decides real target vector from this |

## Logic family legend

| Family | Source meaning |
|---|---|
| `DIRECT_SIMPLE` | damage = attack base * `power / 100`; optional target debuff when `effectId >= 0` |
| `DIRECT_PLUS_RAW_DIVISOR` | direct damage plus `attackBase / param`; skills `1/7` |
| `DIRECT_EXPLICIT_DEBUFF_CHANCE` | direct damage, debuff chance from `param`; skills `2/8/22/28/41/47` |
| `CONDITIONAL_TARGET_DEBUFF0/1` | alternate damage percent if target already has debuff `0` or `1` |
| `DIRECT_CLEAR_TARGET_BUFFS` | direct damage, then clear target self-buffs |
| `HP_PERCENT_SCALING` | damage depends on attacker current HP percent |
| `RAW_DAMAGE_SELF_BUFF_Q` | bytecode-default raw damage, then `game.d.q()` self-buff |
| `NO_DAMAGE_BUFF_EFFECTMODE1` | no direct damage; produces P13 self/buff queue |
| `NO_DAMAGE_SELECTED_BUFF_COPY` | no direct damage; selected-index buff copy/clear behavior |
| `RAW_DAMAGE_VISUAL_ONLY` | raw damage; visual effect row is used, but source post-effect/debuff is not consumed |

## Current high-level status

Phase 9 closed skill result coverage as:

```text
PORTED/PARTIAL + smoke-covered at producer/branch level
```

That means every skill row has source classification and at least one coverage
anchor or source-backed `NOT_REACHED` classification. It does **not** mean all
skill animations are pixel-perfect or every multi-target/per-passive edge is
complete.

Remaining broad gaps:

- exact Java ME RNG stream parity;
- pixel-perfect P7 animation/effect timing;
- full AH renderer parity for every referenced `speffect` row;
- full multi-target/formation parity;
- passive/equipment/global hook parity;
- full UI runtime parity around battle choices.

## Full skill catalog

| Skill | Name | Source description | Raw aq.c[1] row | Logic family | Post-skill behavior | effect.mid / speffect |
|---:|---|---|---|---|---|---|
| 0 | Hỏa trảo | Thương tổn thấp. | `[0,117,529,100,0,45,0,-1,-1,0]` | DIRECT_SIMPLE | - | `[0,0,20,0,-1,-1,0]` |
| 1 | Dương viêm | Thương tổn thấp tăng thêm hiệu quả đốt cháy, mỗi hiệp sẽ giảm bớt trị số sinh mạng nhất định, duy trì liên tục 3 hiệp. | `[0,118,530,50,0,45,2,0,4,0]` | DIRECT_PLUS_RAW_DIVISOR | - | `[0,0,20,0,-1,-1,0]` |
| 2 | Diễm kích | Thương tổn thấp, cũng có 10% xác suất lâm vào trạng thái Mê Muội 2 hiệp. | `[0,119,531,100,0,45,2,1,10,0]` | DIRECT_EXPLICIT_DEBUFF_CHANCE | - | `[0,0,20,0,-1,-1,0]` / `[0,1,14,0,0,0,-1] spe:14/AH12` |
| 3 | Hỏa Vân triệu | Thương tổn thấp, nếu đối phương đang cháy thì sẽ gia tăng được thương tổn trên người đối phương. | `[0,120,532,100,1,30,0,-1,120,0]` | CONDITIONAL_TARGET_DEBUFF0 | - | `[0,0,20,0,-1,-1,0]` |
| 4 | Thiên Hỏa tế | Tăng khả năng phòng ngự 30%, vòng chiến đấu tiếp theo sẽ gia tăng thương tổn. | `[0,121,533,0,1,10,1,0,-1,1]` | NO_DAMAGE_BUFF_EFFECTMODE1 | P13 buff/effect `0` | `[0,1,16,0,-1,-1,0] spe:16/AH9` / `[0,1,15,0,-1,-1,0] spe:15/AH1` |
| 5 | Viêm lôi phá | Khả năng phòng ngự giảm xuống 50%, Gia tăng tỷ lệ thương tổn 50%, duy trì liên tục 3 hiệp. | `[0,122,534,0,1,10,1,1,-1,1]` | NO_DAMAGE_BUFF_EFFECTMODE1 | P13 buff/effect `1` | `[0,1,16,0,-1,-1,0] spe:16/AH9` / `[0,1,15,0,-1,-1,0] spe:15/AH1` |
| 6 | Hỏa diễm đao | Tỷ lệ thương tổn gia tăng khá cao. | `[0,123,535,150,2,30,0,-1,-1,0]` | DIRECT_SIMPLE | - | `[0,0,20,0,-1,-1,0]` |
| 7 | Chước nhiệt chi xúc | Thương tổn thấp làm tăng hiệu quả đốt cháy, mỗi hiệp giảm bớt được trị số sinh mạng nhất định, duy trì liên tục 3 hiệp. | `[0,124,536,75,2,30,2,0,3,0]` | DIRECT_PLUS_RAW_DIVISOR | - | `[0,0,20,0,-1,-1,0]` |
| 8 | Liệt diễm phong bạo | Thương tổn cao, cũng có 20% xác suất lâm vào trạng thái Mê Muội 2 hiệp. | `[0,125,537,200,3,15,2,1,20,0]` | DIRECT_EXPLICIT_DEBUFF_CHANCE | - | `[0,0,20,0,-1,-1,0]` / `[0,1,14,0,0,-1,1] spe:14/AH12` |
| 9 | Vĩnh hằng hỏa ảnh | Thương tổn cao, nếu kẻ địch đang cháy thì tỷ lệ thương tổn trên người sẽ gia tăng. | `[0,126,538,200,3,15,0,-1,250,0]` | CONDITIONAL_TARGET_DEBUFF0 | - | `[0,0,20,0,-1,-1,0]` |
| 10 | Diệp Toàn | Thương tổn thấp. | `[1,127,539,100,0,45,0,-1,-1,0]` | DIRECT_SIMPLE | - | `[0,0,21,1,-1,-1,0]` |
| 11 | Quang phản | Thương tổn thấp, có thể khôi phục trị số sinh mạng nhất định. | `[1,128,540,90,0,45,0,-1,10,0]` | DIRECT_SIMPLE | `q()` heal attacker param `10` | `[0,0,21,1,-1,-1,0]` / `[1,1,10,0,-1,-1,0] spe:10/AH9` |
| 12 | Đằng Phược | Thương tổn thấp, cũng đem Quấn Quanh 3 hiệp. | `[1,129,541,50,0,45,2,2,-1,0]` | DIRECT_SIMPLE_DEBUFF2 | - | `[0,0,21,0,-1,-1,0]` / `[0,1,6,0,-1,-1,0] spe:6/AH8` |
| 13 | Thảo Chủng | Thương tổn thấp, kẻ địch rơi vào trạng thái Thực Loại, sau 2 hiệp tạo thành thương tổn tương đối cao. | `[1,130,542,50,1,30,2,3,150,0]` | DIRECT_SIMPLE_DEBUFF3 | - | `[0,0,21,0,-1,-1,0]` |
| 14 | Đằng chi bích lũy | Gia tăng khả năng phòng ngự 30% thương tổn cũng dội ngược trở lại, duy trì liên tục 3 hiệp. | `[1,131,543,0,1,10,1,2,-1,1]` | NO_DAMAGE_BUFF_EFFECTMODE1 | P13 buff/effect `2` | `[0,0,21,1,-1,-1,0]` |
| 15 | Thảo nguyện thuật | Mỗi hiệp khôi phục trị số sinh mạng nhất định, duy trì liên tục 3 hiệp. | `[1,132,544,0,1,10,1,3,-1,1]` | NO_DAMAGE_BUFF_EFFECTMODE1 | P13 buff/effect `3` | `[0,0,33,0,0,-1,0]` / `[0,1,7,0,-1,-1,0] spe:7/AH9` |
| 16 | Châm Diệp Trảm | Tỷ lệ thương tổn gia tăng khá cao. | `[1,133,545,150,2,30,0,-1,-1,0]` | DIRECT_SIMPLE | - | `[0,0,21,1,-1,-1,0]` |
| 17 | Diệp chi ân huệ | Thương tổn ở mức độ trung bình, cũng có thể khôi phục trị số sinh mạng nhất định. | `[1,134,546,130,2,30,0,-1,40,0]` | DIRECT_SIMPLE | `q()` heal attacker param `40` | `[0,0,21,1,-1,-1,0]` / `[1,1,10,0,-1,-1,0] spe:10/AH9` |
| 18 | Đằng mạn triền nhiễu | Thương tổn tương đối cao, cũng đem Quấn Quanh 3 hiệp. | `[1,135,547,150,3,15,2,2,-1,0]` | DIRECT_SIMPLE_DEBUFF2 | - | `[0,0,21,0,-1,-1,0]` |
| 19 | Quang hợp hiệu ứng | Thương tổn tương đối cao, kẻ địch rơi vào trạng thái Thực Loại, sau 2 hiệp tạo thành Thương tổn cao. | `[1,136,548,150,3,15,2,3,200,0]` | DIRECT_SIMPLE_DEBUFF3 | - | `[0,0,21,0,-1,-1,0]` |
| 20 | Hất bụi | Thương tổn thấp. | `[2,137,549,100,0,45,0,-1,-1,0]` | DIRECT_SIMPLE | - | `[0,0,22,0,-1,-1,0]` |
| 21 | Thổ thuẫn | Thương tổn thấp, có khả năng tự gia tăng phòng ngự 10%(có thể chất chồng). | `[2,138,550,80,0,45,1,4,10,0]` | RAW_DAMAGE_SELF_BUFF_Q | `q()` self buff `4` | `[0,0,22,0,-1,-1,0]` / `[1,1,5,0,-1,-1,0] spe:5/AH9` |
| 22 | Bão cát | Thương tổn thấp cũng có 25% xác suất lâm vào trạng thái Mê Muội 2 hiệp. | `[2,139,551,50,0,45,2,1,25,0]` | DIRECT_EXPLICIT_DEBUFF_CHANCE | - | `[0,0,22,0,-1,-1,0]` |
| 23 | Nham băng | Thương tổn thấp, kẻ địch rơi vào trạng thái Mê Muội, tạo thành thương tổn cực lớn. | `[2,140,552,100,1,30,0,-1,250,0]` | CONDITIONAL_TARGET_DEBUFF1 | - | `[0,0,22,0,-1,-1,0]` / `[0,1,6,0,-1,-1,0] spe:6/AH8` |
| 24 | Người bảo vệ Địa Giới | Giải phóng những vùng có trạng thái dị thường, mỗi hiệp khôi phục trị số sinh mạng nhất định, duy trì liên tục 3 hiệp. | `[2,141,553,0,1,10,1,13,-1,1]` | NO_DAMAGE_BUFF_EFFECTMODE1 | P13 buff/effect `13` | `[0,0,22,0,-1,-1,0]` / `[0,1,17,0,-1,-1,0] spe:17/AH1` |
| 25 | Thạch phu thuật | Giải phóng những vùng có trạng thái dị thường, 3 hiệp có khả năng miễn dịch ở những nơi có trạng thái dị thường. | `[2,142,554,0,1,10,1,14,-1,1]` | NO_DAMAGE_BUFF_EFFECTMODE1 | P13 buff/effect `14` | `[0,1,4,0,-1,-1,0] spe:4/AH7` / `[0,1,17,0,-1,-1,0] spe:17/AH1` |
| 26 | Nham bạo | Thương tổn ở mức độ trung bình. | `[2,143,555,150,2,30,0,-1,-1,0]` | DIRECT_SIMPLE | - | `[0,0,22,0,-1,-1,0]` / `[0,1,6,0,-1,-1,0] spe:6/AH8` |
| 27 | Hàng rào cát đá | Thương tổn thấp, khả năng tự gia tăng phòng ngự 10%(có thể chất chồng). | `[2,144,556,100,2,30,1,4,10,0]` | RAW_DAMAGE_SELF_BUFF_Q | `q()` self buff `4` | `[0,0,22,0,-1,-1,0]` / `[1,1,7,0,-1,-1,0] spe:7/AH9` |
| 28 | Bão cát | Thương tổn ở mức độ trung bình cũng có 25% xác suất lâm vào trạng thái Mê Muội 2 hiệp. | `[2,145,557,150,3,15,2,1,25,0]` | DIRECT_EXPLICIT_DEBUFF_CHANCE | - | `[0,0,22,0,-1,-1,0]` |
| 29 | Thổ Chi Loạn Vũ | Thương tổn tương đối cao, kẻ địch rơi vào trạng thái Mê Muội, Gia tăng tỷ lệ thương tổn. | `[2,146,558,180,3,15,0,-1,300,0]` | CONDITIONAL_TARGET_DEBUFF1 | - | `[0,0,22,0,-1,-1,0]` |
| 30 | Bong bóng | Thương tổn thấp. | `[3,147,559,100,0,45,0,-1,-1,0]` | DIRECT_SIMPLE | - | `[0,0,23,0,-1,-1,0]` |
| 31 | Băng lao | Thương tổn thấp cũng làm giảm tỉ lệ đánh trúng mục tiêu, duy trì liên tục 3 hiệp. | `[3,148,560,60,0,45,2,4,1,0]` | DIRECT_SIMPLE_DEBUFF4 | - | `[0,0,23,0,-1,-1,0]` |
| 32 | Tuyết ảnh | Thương tổn thấp cũng làm giảm hiệu quả công kích kẻ địch 10% Linh Xảo, duy trì liên tục 3 hiệp. | `[3,149,561,60,0,45,2,5,10,0]` | DIRECT_SIMPLE_DEBUFF5 | - | `[0,0,23,0,-1,-1,0]` / `[0,1,1,0,-1,-1,0] spe:1/AH9` |
| 33 | Thủy trụ | Thương tổn thấp cũng làm giảm hiệu quả công kích kẻ địch 10% thương tổn, duy trì liên tục 3 hiệp. | `[3,150,562,100,1,30,2,6,10,0]` | DIRECT_SIMPLE_DEBUFF6 | - | `[0,0,23,0,-1,-1,0]` |
| 34 | Thuật cầu nguyện | 30% xác suất bắn ngược thương tổn, duy trì liên tục 3 hiệp. | `[3,151,563,0,1,10,1,5,-1,1]` | NO_DAMAGE_BUFF_EFFECTMODE1 | P13 buff/effect `5` | `[0,1,4,0,-1,-1,0] spe:4/AH7` |
| 35 | Thủy bích | 50% xác suất gây thương tổn cho kẻ địch bị giảm phân nửa, duy trì liên tục 3 hiệp. | `[3,152,564,0,1,10,1,6,-1,1]` | NO_DAMAGE_BUFF_EFFECTMODE1 | P13 buff/effect `6` | `[0,1,4,0,-1,-1,0] spe:4/AH7` / `[0,1,17,0,-1,-1,0] spe:17/AH1` |
| 36 | Bạo Phong Tuyết | Tỷ lệ thương tổn gia tăng khá cao. | `[3,153,565,150,2,30,0,-1,-1,0]` | DIRECT_SIMPLE | - | `[0,0,23,0,-1,-1,0]` |
| 37 | Lá chắn gió tuyết | Thương tổn thấp cũng làm giảm tỉ lệ đánh trúng mục tiêu, duy trì liên tục 3 hiệp. | `[3,154,566,100,2,30,2,4,2,0]` | DIRECT_SIMPLE_DEBUFF4 | - | `[0,0,23,0,-1,-1,0]` / `[0,1,7,0,-1,0,0] spe:7/AH9` / `[0,1,6,0,-1,-1,0] spe:6/AH8` |
| 38 | Băng Phong Hãm Tịnh | Thương tổn tương đối cao cũng làm giảm hiệu quả công kích kẻ địch 10% Linh Xảo, duy trì liên tục 3 hiệp. | `[3,155,567,150,3,15,2,5,10,0]` | DIRECT_SIMPLE_DEBUFF5 | - | `[0,0,23,0,-1,-1,0]` / `[0,1,7,0,-1,-1,0] spe:7/AH9` |
| 39 | Ray lạnh | Thương tổn tương đối cao cũng làm giảm hiệu quả công kích kẻ địch 10% thương tổn, duy trì liên tục 3 hiệp. | `[3,156,568,150,3,15,2,6,10,0]` | DIRECT_SIMPLE_DEBUFF6 | - | `[0,0,23,0,-1,-1,0]` |
| 40 | Điện giật | Thương tổn thấp. | `[4,157,569,100,0,45,0,-1,-1,0]` | DIRECT_SIMPLE | - | `[0,0,24,0,-1,-1,0]` |
| 41 | Lôi thiểm | Thương tổn thấp cũng làm giảm 10% xác suất làm kẻ địch Tê Liệt 3 hiệp. | `[4,158,570,90,0,45,2,10,10,0]` | DIRECT_EXPLICIT_DEBUFF_CHANCE | - | `[0,0,24,0,-1,-1,0]` / `[0,1,4,0,-1,-1,1] spe:4/AH7` |
| 42 | Nạp điện | Thương tổn thấp, khả năng tự Linh Xảo gia tăng 5%. | `[4,159,571,90,0,45,1,7,5,0]` | RAW_DAMAGE_SELF_BUFF_Q | `q()` self buff `7` | `[0,0,24,0,-1,-1,0]` / `[1,1,1,0,-1,-1,0] spe:1/AH9` |
| 43 | Sóng điện từ | Thương tổn thấp cũng có thể làm giảm mặt lợi ích của bản thân đối với kẻ địch. | `[4,160,572,100,1,30,0,-1,-1,0]` | DIRECT_CLEAR_TARGET_BUFFS | - | `[0,0,24,0,-1,-1,0]` / `[0,1,4,0,-1,-1,0] spe:4/AH7` |
| 44 | Đoạt mệnh cao áp | Số lần tiêu hao kỹ năng tăng gấp bội, Gia tăng tỷ lệ thương tổn 30%, duy trì liên tục 3 hiệp. | `[4,161,573,0,1,10,1,8,-1,1]` | NO_DAMAGE_BUFF_EFFECTMODE1 | P13 buff/effect `8` | `[0,1,19,0,-1,-1,0] spe:19/AH9` / `[0,1,15,0,-1,-1,0] spe:15/AH1` |
| 45 | Điện năng chuyển đổi | Linh Xảo gia tăng 50%, phòng ngự giảm xuống 50%, duy trì liên tục 3 hiệp. | `[4,162,574,0,1,10,1,9,-1,1]` | NO_DAMAGE_BUFF_EFFECTMODE1 | P13 buff/effect `9` | `[0,1,19,0,-1,-1,0] spe:19/AH9` / `[0,1,15,0,-1,-1,0] spe:15/AH1` |
| 46 | Tia lửa điện | Tỷ lệ thương tổn gia tăng khá cao. | `[4,163,575,150,2,30,0,-1,-1,0]` | DIRECT_SIMPLE | - | `[0,0,24,0,-1,-1,0]` |
| 47 | Chùm sấm sét | Thương tổn ở mức độ trung bình cũng giảm 10% xác suất làm kẻ địch Tê Liệt 3 hiệp. | `[4,164,576,130,2,30,2,10,10,0]` | DIRECT_EXPLICIT_DEBUFF_CHANCE | - | `[0,0,24,0,-1,-1,0]` / `[0,1,4,0,-1,-1,1] spe:4/AH7` |
| 48 | Điện quang thạch hỏa | Thương tổn ở mức độ trung bình, Linh Xảo gia tăng 5%. | `[4,165,577,130,3,15,1,7,5,0]` | RAW_DAMAGE_SELF_BUFF_Q | `q()` self buff `7` | `[0,0,24,0,-1,-1,0]` / `[1,1,9,0,-1,-1,0] spe:9/AH9` |
| 49 | Cảm ứng điện từ | Thương tổn cao cũng có thể làm giảm mặt lợi ích của bản thân đối với kẻ địch. | `[4,166,578,180,3,15,0,-1,-1,0]` | DIRECT_CLEAR_TARGET_BUFFS | - | `[0,0,24,0,-1,-1,0]` |
| 50 | Ảnh thứ | Thương tổn thấp. | `[5,167,579,100,0,45,0,-1,-1,0]` | DIRECT_SIMPLE | - | `[0,0,25,0,-1,-1,0]` / `[0,1,9,0,-1,-1,0] spe:9/AH9` |
| 51 | Chú oán | Thương tổn thấp cũng làm giảm hiệu quả công kích kẻ địch 20% phòng ngự, duy trì liên tục 3 hiệp. | `[5,168,580,80,0,45,2,7,20,0]` | DIRECT_SIMPLE_DEBUFF7 | - | `[0,0,25,0,-1,-1,0]` / `[0,1,8,0,-1,-1,0] spe:8/AH9` / `[0,1,11,0,-1,-1,0] spe:11/AH1` |
| 52 | Quỷ độc | Thương tổn thấp cũng có một ít xác suất khôi phục giá trị sinh mệnh. | `[5,169,581,80,0,45,0,-1,5,0]` | DIRECT_SIMPLE | `q()` leech param `5` | `[0,0,25,1,-1,-1,0]` / `[0,1,8,0,-1,-1,0] spe:8/AH9` / `[1,1,10,0,-1,-1,1] spe:10/AH9` |
| 53 | Cơn ác mộng | Thương tổn nhất định, giá trị sinh mệnh kẻ địch càng thấp càng thương tổn cao. | `[5,170,582,200,1,30,0,-1,200,0]` | HP_PERCENT_SCALING | - | `[0,0,25,2,-1,-1,0]` / `[0,1,9,0,-1,-1,0] spe:9/AH9` |
| 54 | Mị ảnh | 40% xác suất lực công kích của kẻ địch bị dội ngược trở lại, duy trì liên tục 4 hiệp. | `[5,171,583,0,1,10,2,8,40,0]` | ZERO_POWER_NO_DAMAGE_GUARD | table debuff `8` is NOT_REACHED from current P7 guard | `[0,1,0,0,-1,-1,0] spe:0/AH9` |
| 55 | Hỗn loạn | Không bị khống chế, có thể tự do hành động. | `[5,172,584,0,1,10,2,9,-1,0]` | ZERO_POWER_NO_DAMAGE_GUARD | table debuff `9` is NOT_REACHED from current P7 guard | `[0,1,12,0,-1,-1,0] spe:12/AH12` |
| 56 | Độc ảnh thứ | Tỷ lệ thương tổn gia tăng khá cao. | `[5,173,585,150,2,30,0,-1,-1,0]` | DIRECT_SIMPLE | - | `[0,0,25,0,-1,-1,0]` / `[0,1,8,0,-1,-1,0] spe:8/AH9` / `[0,1,9,0,-1,-1,0] spe:9/AH9` |
| 57 | Chú Phược Quỷ Lao | Thương tổn ở mức độ trung bình cũng làm giảm hiệu quả công kích kẻ địch 20% phòng ngự, duy trì liên tục 3 hiệp. | `[5,174,586,120,2,30,2,7,20,0]` | DIRECT_SIMPLE_DEBUFF7 | - | `[0,0,25,1,-1,-1,0]` / `[0,1,14,0,-1,-1,0] spe:14/AH12` / `[0,1,11,0,-1,-1,0] spe:11/AH1` |
| 58 | Quỷ độc tín ngưỡng | Thương tổn thấp cũng có một ít xác suất khôi phục giá trị sinh mệnh. | `[5,175,587,100,3,15,0,-1,8,0]` | DIRECT_SIMPLE | `q()` leech param `8` | `[0,0,25,1,-1,-1,0]` / `[0,1,13,0,-1,-1,0] spe:13/AH1` / `[1,1,10,0,-1,-1,1] spe:10/AH9` |
| 59 | Lời nguyền cuối cùng | Xuất hiện thương tổn, giá trị sinh mạng của kẻ địch càng thấp, thương tổn càng cao. | `[5,176,588,250,3,15,0,-1,250,0]` | HP_PERCENT_SCALING | - | `[0,0,25,2,-1,-1,0]` |
| 60 | Phong nhận | Thương tổn thấp. | `[6,177,589,100,0,45,0,-1,-1,0]` | DIRECT_SIMPLE | - | `[0,0,26,0,-1,-1,0]` |
| 61 | Phong áp | Thương tổn thấp cũng làm giảm hiệu quả công kích kẻ địch 5% Linh Xảo, duy trì liên tục 3 hiệp. | `[6,178,590,80,0,45,2,5,5,0]` | DIRECT_SIMPLE_DEBUFF5 | - | `[0,0,26,0,-1,-1,0]` / `[0,1,11,0,-1,-1,0] spe:11/AH1` |
| 62 | Thuận phong | Thương tổn thấp, lực lượng tự gia tăng 5%, duy trì liên tục 2 hiệp. | `[6,179,591,80,0,45,1,10,5,0]` | RAW_DAMAGE_SELF_BUFF_Q | `q()` self buff `10` | `[0,0,26,0,-1,-1,0]` / `[0,1,0,0,-1,-1,0] spe:0/AH9` / `[1,1,15,0,-1,-1,0] spe:15/AH1` |
| 63 | Long quyển | Thương tổn thấp, có 5% xác suất tiếp tục công kích lần nữa. | `[6,180,592,100,1,30,0,-1,5,0]` | DIRECT_SIMPLE | `q()` follow-up chance `5` | `[0,0,26,0,-1,-1,0]` |
| 64 | Nghịch Phong Đoạt | Sử dụng Thâu Thủ đối với kẻ địch để lấy tất cả những trạng thái có lợi sang mình, duy trì liên tục 3 hiệp. | `[6,181,593,0,1,10,1,11,-1,0]` | NO_DAMAGE_SELECTED_BUFF_COPY | `q()` selected buff/copy `11` | `[1,1,18,0,-1,-1,0] spe:18/AH9` / `[1,1,15,0,-1,-1,0] spe:15/AH1` |
| 65 | Vũ Liệt Thuật | Mỗi hiệp công kích hai lần, duy trì liên tục 2 hiệp. | `[6,182,594,0,1,10,1,12,-1,1]` | NO_DAMAGE_BUFF_EFFECTMODE1 | P13 buff/effect `12` | `[1,1,16,0,-1,-1,0] spe:16/AH9` / `[1,1,15,0,-1,-1,0] spe:15/AH1` |
| 66 | Yến Hồi Thiểm | Tỷ lệ thương tổn gia tăng khá cao. | `[6,183,595,150,2,30,0,-1,-1,0]` | DIRECT_SIMPLE | - | `[0,0,26,0,-1,-1,0]` |
| 67 | Phong Chi Tuyền Qua | Thương tổn ở mức độ trung bình cũng làm giảm hiệu quả công kích kẻ địch 5% nhanh nhẹn, duy trì liên tục 3 hiệp. | `[6,184,596,110,2,30,2,5,5,0]` | RAW_DAMAGE_VISUAL_ONLY | no q()/no debuff side effect from Phase 9 source audit | `[0,0,26,0,-1,-1,0]` / `[0,1,11,0,-1,-1,0] spe:11/AH1` |
| 68 | Phong Chi Tí Hữu | Thương tổn ở mức độ trung bình, lực lượng tự gia tăng 55, duy trì liên tục 2 hiệp. | `[6,185,597,110,3,15,1,10,5,0]` | DIRECT_SIMPLE_DEBUFF10 + SELF_BUFF_Q | `q()` self buff `10` | `[0,0,26,0,-1,-1,0]` / `[0,1,0,0,-1,-1,0] spe:0/AH9` / `[1,1,15,0,-1,-1,0] spe:15/AH1` |
| 69 | Phi Yến Hoàn Sào | Thương tổn tương đối cao, có 8% xác suất tiếp tục công kích lần nữa. | `[6,186,598,150,3,15,0,-1,8,0]` | DIRECT_SIMPLE | `q()` follow-up chance `8` | `[0,0,26,0,-1,-1,0]` |

## Animation/effect renderer coverage

Known AH types referenced by the skill table/effect rows:

| AH type | Seen in skills | Current known status |
|---:|---|---|
| `1` | many second chunks such as `4/5/24/25/35/44/45/51/57/58/62/64/65/67/68` | PORTED/PARTIAL from earlier P7 special-effect work |
| `7` | `25/34/35/41/43/47` | PORTED/PARTIAL from Phase 10-B3 coverage |
| `8` | `12/23/26/37` | PORTED/PARTIAL from Phase 10-B2 coverage |
| `9` | many overlay rows, including `4/5/11/15/17/21/27/32/37/38/42/44/45/48/50/51/52/53/56/62/64/65/68` | PORTED/PARTIAL; several visual slices exist, not pixel-perfect |
| `12` | `2/8/55/57` | PORTED/PARTIAL from Phase 10-B1 and species/effect work |

Important: "renderer type exists" is not the same as "skill animation is
pixel-perfect". For each skill family we still need smoke that confirms:

- source `effect.mid` row chosen;
- each chunk starts at the right source-shaped phase;
- AH overlay attaches to attacker/target correctly;
- damage/blood/HP update happens at the right point;
- no fake damage occurs for zero-power/no-damage rows.

## Coding strategy from here

Do not code skills by numeric order. Code by family, because most skills share
the same formula/effect path.

Recommended order:

1. **Animation coverage pass for direct element base rows**
   - one representative per element base actor/effect row:
     - fire `0/6`
     - wood `10/16`
     - earth `20/26`
     - water `30/36`
     - electric `40/46`
     - ghost `50/56`
     - wind `60/66`
   - Goal: prove `effect.mid` base chunks draw and hit timing feels alive.
2. **Status/debuff visual pass**
   - group by debuff id:
     - burn/debuff0: `1/7`
     - confusion/debuff1: `2/8/22/28`
     - bind/debuff2: `12/18`
     - seed/debuff3: `13/19`
     - hit-rate down/debuff4: `31/37`
     - speed down/debuff5: `32/38/61`
     - damage reduction/debuff6: `33/39`
     - defense down/debuff7: `51/57`
     - paralysis/catch debuff10: `41/47/68`
   - Goal: visual icon/body overlay and P12 queue must match the logic.
3. **No-damage buff visual pass**
   - `4/5/14/15/24/25/34/35/44/45/64/65`
   - Goal: no fake damage; P13 queue + AH overlay + status slots align.
4. **Special post-skill pass**
   - heal/leech/follow-up/self-buff/copy:
     - `11/17`, `52/58`, `63/69`, `21/27/42/48/62/68`, `64`.
   - Goal: post-skill text/effect and turn-state transitions.
5. **Per-skill polish**
   - after each family is functional, run one smoke per actual skill id with:
     - forced hit;
     - forced miss where relevant;
     - forced crit where relevant;
     - source row/effect trace assert;
     - PNG output.

## Recommended next slice

Create a focused audit for direct base animation first:

```text
237_battle_skill_direct_base_animation_matrix.md
```

Scope:

- read `game.d` P7 animation update/draw;
- read `effect.mid` rows for `0,10,20,30,40,50,60`;
- identify shared base chunks `20..26`;
- compare current renderer behavior;
- choose one skill, likely skill `0` or `10`, for the first animation polish slice.

No code until the direct-base animation audit is written.

## Verification status

Audit-only document. No build, check, or smoke was required because no runtime
code changed.
