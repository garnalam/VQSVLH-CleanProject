# 244 - Battle Status Effectiveness Numeric Full VI

Ngày: 2026-07-13

Mục tiêu: bản gộp từ `240_battle_status_effectiveness_full_vi.md` và
`243_battle_status_numeric_effect_audit.md`. Từ giờ khi code/smoke status,
buff, debuff phải dùng bảng này để biết **tên, mô tả source, raw row, logic,
con số/công thức cụ thể, icon/body visual, rebuild status, smoke cần chốt**.

Tài liệu này là audit/doc only, chưa đổi runtime.

## Source facts

| Source | Vai trò |
| --- | --- |
| `modules/script/decoded/data__script__db.mid.json` | `aq.c[1]` skill rows, `aq.c[3]` property/passive/material rows, `aq.c[6]` buff, `aq.c[7]` debuff |
| `modules/source_code/decoded/decompiled_source_cfr/game/b.java` | `B()`, `b(target)`, `a(buff)`, `o(buff tick)`, debuff apply/tick |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java` | P7 hit/miss, `q()` post-skill, catch chance `b(itemId)`, EXP hooks |
| `modules/source_code/decoded/bytecode_javap/game__b.javap.txt` | xác nhận bytecode cho các source oddity như buff10 |

## Pet-held property/passive aq.c[3][0..11]

Các row này là property/passive dạng mang theo trên pet, check bằng `game.b.f(byte)` qua
`game.b.c[5]`. Đây không phải buff/debuff tạm thời, không dùng HUD queue icon và không có
P12/P13 body visual.

Tên gần đúng trong rebuild: **pet-held passive/property**. Nếu cần ví dụ dễ hiểu thì nó giống
"held item/passive ability" hơn là status. Nó không phải Pokemon/species, không phải skill, và
không phải active battle effect queue.

| Id | Tên | Mô tả source | Raw row | Logic chính | Con số / công thức cụ thể | Visual/icon | Rebuild status | Smoke cần chốt |
| ---: | --- | --- | --- | --- | --- | --- | --- | --- |
| 0 | Mạn Đà La Thạch | Sủng vật hiện tại trị số sinh mạng thấp hơn 30%, gia tăng lực công kích 100%; | `[213,1,237,5,1,30,100]` | Owner pet HP thấp thì tăng công trong damage formula | Nếu `HP <= maxHP * 30 / 100`, raw attack part đổi thành `attack * 200 / 100 - targetDefense`. Đây là +100% attack trước khi trừ defense, không phải final damage x2. | không HUD queue | PORTED | `battle_status_form0_low_hp_attack_boost` |
| 1 | Hồng Sắc Hải Loa | Sủng vật gia tăng lực công kích 10%; | `[214,2,238,5,1,10]` | owner tăng attack formula | Raw attack part là `attack * 110 / 100 - targetDefense`. | không HUD queue | PORTED/PARTIAL | damage tăng đúng 10% attack |
| 2 | Quy Xác Toái Phiến | Sủng vật Gia tăng khả năng phòng ngự 15%; | `[215,3,239,5,1,15]` | target/owner tăng defense trong raw damage path | Target defense dùng trong formula là `defense * 115 / 100`. | không HUD queue | PORTED/PARTIAL | target nhận damage thấp hơn |
| 3 | Ô Nha Uế | Gia tăng sức đối kháng 20%; | `[216,4,240,5,1,20]` | giảm tỉ lệ dính debuff | Debuff apply chance đổi thành `chance * (100 - 20) / 100`. Ví dụ chance 25% còn 20%. | không HUD queue | PORTED/PARTIAL | forced debuff roll bị giảm/block |
| 4 | Viễn Cổ Long Cốt | Sủng vật gia tăng tỉ số bạo kích 10%; | `[217,5,241,5,1,10]` | tăng crit chance | Crit chance cộng thêm 10 điểm %. Source base là `5 + speed / 2`, hoặc 30 trước speed nếu final-visual element condition đúng. | không HUD queue | PORTED/PARTIAL | crit chance smoke |
| 5 | Mật Phong Sào | Sau khi chiến đấu sủng vật lấy được giá trị kinh nghiệm 20%; | `[218,6,242,5,1,20]` | EXP multiplier cho participant | EXP award đổi thành `award * 120 / 100`. | không HUD queue | PORTED/PARTIAL | EXP participant smoke |
| 6 | Ký Cư Giải Xác | Mang theo đạo cụ sủng vật tuy không tham chiến cũng có thể đạt được giá trị kinh nghiệm tương ứng; | `[219,7,243,5,1,100]` | reserve EXP share | Row param là 100%, reserve/share path cho pet không tham chiến. Exact lifecycle vẫn thuộc cụm EXP/passive. | không HUD queue | PORTED/PARTIAL | reserve EXP smoke |
| 7 | Linh Trùng Thi Hài | Giao tranh bắt đầu; | `[220,8,244,5,1]` | chưa chứng minh consumer battle trực tiếp | Chưa có số combat được chứng minh. | UNKNOWN/PENDING | UNKNOWN/PENDING | source search trước khi code |
| 8 | Hấp Huyết Đằng Mạn | Sủng vật công kích là cách tốt nhất để khôi phục lại một phần trị số sinh mạng; | `[221,9,245,5,1,10,20]` | attacker có chance tự heal sau hit | Sau hit roll `<= 10`; nếu pass heal `damage * 20 / 100`. | không HUD queue | PORTED/PARTIAL | hit smoke thấy attacker heal |
| 9 | Cá Thờn Bơn | Sủng vật công kích nhất định trúng mục tiêu; | `[222,10,246,5,1]` | không hụt | Miss chance bị ép về `0`. | không HUD queue | PORTED | high-miss setup vẫn hit |
| 10 | Cảm Lãm Chi Diệp | Bất luận đối thủ đã bị thương thế nào, trị số sinh mạng cũng sẽ không thấp hơn 10 điểm (bắt được dùng); | `[223,11,247,5,1,10]` | HP floor/capture-related source path | Trong P7/U() path, nếu điều kiện source đúng và HP target `<= 10`, set HP lại `10`. | không HUD queue | PORTED/PARTIAL | lethal hit còn HP floor 10 |
| 11 | Sủng vật lôi đạt | Gia tăng tỷ lệ bắt được sủng vật 20%; | `[224,12,248,5,1,20]` | tăng catch chance | Source dùng active/player pet property: chọn multiplier `12/10`, rồi nhân thêm `(100 + 20) / 100`; trước cap là khoảng `1.44x`. | không HUD queue | PORTED/PARTIAL | catch multiplier smoke |

## Non-battle material/key aq.c[3][12..17]

Các row này dùng chung text/icon table `aq.c[3]`, nhưng nằm ở inventory stack `q.M`, không gắn
lên pet qua `c[5]`, và không phải battle status/passive.

| Id | Tên | Mô tả source | Raw row | Phân loại thật | Battle behavior | Rebuild status |
| ---: | --- | --- | --- | --- | --- | --- |
| 12 | Tinh Nguyên Thạch | Tài liệu liên quan đến tiến hóa; | `[225,13,249,1000,0]` | material tiến hóa | không có | NON_BATTLE |
| 13 | Thiên Giới Tinh Thạch | Tài liệu liên quan tiến hóa bậc cao; | `[226,14,250,2,1]` | material tiến hóa bậc cao | không có | NON_BATTLE |
| 14 | Thiên Địa Thần Thạch | Tài liệu liên quan đến tiến hóa bậc cao hiếm có; | `[227,15,251,10,1]` | material tiến hóa hiếm | không có | NON_BATTLE |
| 15 | Hồn Tinh Thạch | Tài liệu liên quan đến dị hoá; | `[228,16,252,2,1]` | material dị hóa | không có | NON_BATTLE |
| 16 | Quỷ Thần Tinh Thạch | Tài liệu liên quan đến dị hóa bậc cao hiếm có; | `[229,17,253,10,1]` | material dị hóa hiếm | không có | NON_BATTLE |
| 17 | Chìa khóa | Có thể mở hoàng kim bảo rương; | `[363,52,364,1,1]` | key/special item | không có | NON_BATTLE |

## Buff bên bản thân aq.c[6]

Buff nằm trong `game.b.v[id][0..4]`.

- Duration: `v[id][0] = aq.c[6][id][2]`.
- HUD icon: `buffId + 12`.
- Duration cell: `134 + v[id][0]`.
- Body visual P12/P13 chỉ có id `3,5,13`.

| Id | Tên | Mô tả source | Raw row | Logic chính | Con số / công thức cụ thể | Icon cell | Body visual | Rebuild status | Smoke cần chốt |
| ---: | --- | --- | --- | --- | --- | ---: | --- | --- | --- |
| 0 | Súc Lực | Sự chuyển hướng không công kích, nhưng cải thiện khả năng phòng ngự, những đợt công kích tiếp theo sẽ làm gia tăng thương tổn. | `[333,348,2,30,190]` | tăng defense, lưu extra damage | Duration 2. Khi apply: `v[0][1] = baseDefense * 30 / 100`, nên defense +30%. Đồng thời `v[0][2] = 190 * B() / 100`. Khi source condition `v[0][0] == 0`, outgoing damage cộng thêm `v[0][2]`, tức 190% raw `B()` đã lưu lúc apply. | `12` | không | PORTED/PARTIAL | formula/counter smoke |
| 1 | Phá Phủ | Gia tăng thương tổn, khả năng phòng ngự giảm, duy trì liên tục Y hiệp. | `[334,349,3,50,50]` | damage tăng, defense bản thân giảm | Duration 3. `v[1][1] = baseDefense * 50 / 100`, defense bản thân -50%. `v[1][2] = 50`, outgoing damage cộng `damage * 50 / 100`, tức +50% damage. | `13` | không | PORTED | damage/defense smoke |
| 2 | Kinh Cức | Khả năng phòng ngự tự gia tăng, công kích ngược lại đối thủ tạo thành thương tổn, duy trì liên tục Y hiệp. | `[335,350,3,30,10]` | defense up, phản damage/hook liên quan | Duration 3. `v[2][1] = baseDefense * 30 / 100`, defense +30%. `v[2][2] = 10`, khi bị đánh thì attacker nhận reflect/counter `hitDamage * 10 / 100`. | `14` | không | PORTED/PARTIAL | reflect/defense smoke |
| 3 | Khôi phục | Mỗi hiệp khôi phục trị số sinh mạng nhất định, duy trì liên tục Y hiệp. | `[336,351,3,5,-1]` | heal khi apply và tick mỗi hiệp | Duration 3. `v[3][1] = maxHP * 5 / 100`. Heal 5% max HP khi apply và mỗi P13 tick. | `15` | có | PORTED | checkpoint bắt buộc |
| 4 | Phòng ngự | Gia tăng giá trị phòng ngự, duy trì liên tục Y hiệp. | `[337,352,2,-1,-1]` | defense up theo skill param | Duration 2. Không dùng row `-1`; source dùng producer `skill[8]`. Skill 21/27 có `skill[8] = 10`, nên `v[4][1] = baseDefense * 10 / 100`, defense +10%. | `16` | không | PORTED/PARTIAL | stat reassert/expiry |
| 5 | Vô hình | Công kích của đối thủ sẽ có một phần tỷ lệ dội ngược lại chính mình, duy trì liên tục Y hiệp. | `[338,353,3,30,-1]` | reflect/store damage chance | Duration 3. `v[5][1] = 30`. Khi unit này bị hit, roll `<= 30`; nếu pass lưu incoming damage vào `K[5]`, post-hit gây lại từng đó damage cho attacker. | `17` | có | PORTED/PARTIAL | visual + reflect |
| 6 | Kiên nhẫn | Cơ hội tiếp xúc để công kích kẻ thù tỷ lệ chỉ là một nửa, duy trì liên tục Y hiệp. | `[339,354,3,50,-1]` | giảm/chuyển damage theo source oddity | Duration 3. `v[6][1] = 50`, `v[6][2] = -1`. Source branch: nếu target có buff6 và roll `<= 50`, damage thành `damage * (-1) / 100`. Đây là source oddity, chưa được design-fix. | `18` | không | SOURCE_ODDITY/PARTIAL | owner-side formula smoke |
| 7 | Linh Xảo | Gia tăng giá trị Linh Xảo, duy trì liên tục Y hiệp. | `[340,355,2,-1,-1]` | speed up theo skill param | Duration 2. Không dùng row `-1`; source dùng producer `skill[8]`. Skill 42/48 có `skill[8] = 5`, nên `v[7][1] = baseSpeed * 5 / 100`, speed +5%. | `19` | không | PORTED | speed/order smoke |
| 8 | Điện áp | Gia tăng tiêu hao giá trị kỹ năng, làm gia tăng thương tổn, duy trì liên tục Y hiệp. | `[341,356,4,30,-1]` | tăng PP cost và damage | Duration 4. `v[8][1] = 30`. Outgoing damage cộng `damage * 30 / 100`, tức +30%. Phần tăng PP cost còn phải kiểm tra riêng. | `20` | không | PORTED/PARTIAL | PP drain + damage |
| 9 | Hỏa Thạch | Gia tăng giá trị Linh Xảo, phòng ngự giá trị giảm xuống, duy trì liên tục Y hiệp. | `[342,357,3,50,50]` | speed up, defense down | Duration 3. `v[9][1] = baseSpeed * 50 / 100`, speed +50%. `v[9][2] = baseDefense * 50 / 100`, defense -50%. | `21` | không | PORTED/PARTIAL | stat delta + turn hook |
| 10 | Man Lực | Lực lượng giá trị Gia tăng, duy trì liên tục Y hiệp. | `[343,358,2,-1,-1]` | attack up theo text, nhưng source oddity | Duration 2. Bytecode dùng `v[10][1] = baseAttack * (-1) / 100`, attack thành `baseAttack + v[10][1]`, tức xấp xỉ -1% attack. Audit `242` chứng minh không phải lỗi decode. | `22` | không | PORTED-AS-SOURCE / SOURCE_ODDITY | checkpoint đã chứng minh source oddity |
| 11 | Thâu Thủ | Trạng thái thuận lợi của đối thủ sẽ được chuyển qua cho bạn, duy trì liên tục Y hiệp. | `[344,359,3,-1,-1]` | copy buff từ donor rồi clear donor | Duration 3. `v[11][1] = selected donor index`; copy toàn bộ buff active từ donor theo `game.d.f[]/selectedTargetSlot`, sau đó clear buff donor. Numeric value giữ nguyên từ donor buff được copy. | `23` | không | PARTIAL | donor vector smoke |
| 12 | Gia Tốc | Mỗi hiệp công kích 2 lần, duy trì liên tục Y hiệp, lần thứ hai không cần lựa chọn kỹ năng, lặp lại kỹ năng lần trước đó, hơn nữa không cần thiết hao tổn kỹ năng. | `[345,360,2,-1,-1]` | K12 follow-up/PP conservation | Duration 2. Apply set `K[12] = 1`; tick set `K[12] = 2`; không trực tiếp đổi HP/stat. | `24` | không | PORTED/PARTIAL | producer->consumer |
| 13 | Thạch Hóa | Giải phóng các trạng thái dị thường, mỗi hiệp khôi phục 20% hạn mức cao nhất của trị số sinh mạng, duy trì liên tục 3 hiệp. | `[346,361,3,20,-1]` | clear debuff + heal tick | Duration 3. `v[13][1] = maxHP * 20 / 100`. Clear debuff và heal 20% max HP khi apply/tick. | `25` | có | PORTED | cleanse+heal+visual |
| 14 | Thạch Phu | Giải phóng các trạng thái dị thường, sẽ có 3 hiệp miễn dịch ở những nơi có trạng thái dị thường. | `[347,362,3,-1,-1]` | clear debuff + block abnormal/debuff | Duration 3. Không có stat number. Clear debuffs khi apply; block debuff/abnormal apply trong debuff chance path. | `26` | không | PORTED/PARTIAL | checkpoint bắt buộc |

## Debuff bên target aq.c[7]

Debuff nằm trong `game.b.w[id][0..4]`.

- Duration: `w[id][0] = aq.c[7][id][2]`.
- HUD icon: `debuffId + 1`.
- Duration cell: `134 + w[id][0]`.
- Body visual P12/P13 chỉ có id `0,1,2,3,8,9,10`.

| Id | Tên | Mô tả source | Raw row | Logic chính | Con số / công thức cụ thể | Icon cell | Body visual | Rebuild status | Smoke cần chốt |
| ---: | --- | --- | --- | --- | --- | ---: | --- | --- | --- |
| 0 | Gieo Hạt | Mỗi hiệp giảm bớt X điểm trị số sinh mạng, duy trì liên tục Y hiệp. | `[311,322,3]` | tick HP damage; skill 3/9 conditional damage | Duration 3. Khi apply: `w[0][1] = preSkillRawDamage`. Mỗi P12 tick gây `max(1, w[0][1] / skill[8])`. Skill 1 divisor 4; skill 7 divisor 3. | `1` | có | PORTED | checkpoint bắt buộc |
| 1 | Mê Muội | Gia tăng tốc độ công kích đối thủ có thể sử dụng đạo cụ, duy trì liên tục Y hiệp. | `[312,323,2]` | flag; skill 23/29 conditional; catch multiplier | Duration 2. Không lưu stat value trong apply switch. Nếu target có debuff1, skill 23/29 dùng nhánh `raw * skill[8] / 100`. Catch multiplier index 1 = `11 / 10`, tức 1.1x. | `2` | có | PORTED/PARTIAL | flag/conditional/catch smoke |
| 2 | Quấn Quanh | Gia tăng tốc độ, sủng vật không thể thay thế, bạn cũng không thể trốn chạy, không thể sử dụng đạo cụ, ...duy trì liên tục Y hiệp. | `[313,324,3]` | block switch/item/run; catch multiplier | Duration 3. Không lưu stat value trong apply switch. Command disable/lock flag; catch multiplier index 2 = `12 / 10`, tức 1.2x. | `3` | có | PORTED/PARTIAL | command disable smoke |
| 3 | Thực Loại | Sau Y hiệp đã bị X đả thương, nếu tử vong thì hiệu quả Thực Loại biến mất. | `[314,325,3]` | delayed HP damage gần hết duration | Duration 3. Khi apply: `w[3][1] = preSkillRawDamage`. Gần expiry gây `max(1, w[3][1] * skill[8] / 100)`. Skill 13 dùng 150%; skill 19 dùng 200%. | `4` | có | PORTED | delayed tick smoke |
| 4 | Mục | Hiệu quả giảm chỉ duy trì liên tục Y hiệp. | `[315,326,3]` | accuracy/miss chance hook | Duration 3. `w[4][1] = skill[8]` flat value. Miss/evasion path giảm effective attacker speed bởi value này. Skill 31 lưu 1; skill 37 lưu 2. | `5` | không | PORTED/PARTIAL | miss chance smoke |
| 5 | Chậm Chạp | Giá trị tạm thời giảm xuống, duy trì liên tục Y hiệp. | `[316,327,3]` | speed down | Duration 3. `w[5][1] = baseSpeed * skill[8] / 100`. Skill 32/38 giảm 10% base speed; skill 61/67 giảm 5% base speed. | `6` | không | PORTED | checkpoint bắt buộc |
| 6 | Nhụt Chí | Tỉ suất thương tổn giảm xuống, duy trì liên tục Y hiệp. | `[317,328,3]` | damage output down | Duration 3. `w[6][1] = skill[8]`. Outgoing damage giảm `damage * w[6][1] / 100`. Skill 33/39 dùng 10%, tức damage -10%. | `7` | không | PORTED/PARTIAL | damage reduction smoke |
| 7 | Phòng Ngự | Giá trị phòng ngự giảm xuống, duy trì liên tục Y hiệp. | `[318,329,3]` | defense down | Duration 3. `w[7][1] = baseDefense * skill[8] / 100`. Skill 51/57 dùng 20%, tức target defense -20%. | `8` | không | PORTED | checkpoint bắt buộc |
| 8 | Quỷ Mị | Lợi dụng sự công kích của đối thủ mới có thể chính mình công kích được đối phương. | `[319,330,4]` | source route còn đặc biệt/zero-power | Duration 4. Producer skill 54 có `skill[8] = 40`, nhưng full consumer vẫn chưa chứng minh đủ; chưa được phép gán damage/stat bừa. | `9` nếu active | có nếu active | FLAG/PENDING | chứng minh route trước |
| 9 | Hỗn Loạn | Không thể thay đổi sủng vật của mình, duy trì liên tục Y hiệp. | `[320,331,1]` | không thể đổi pet / route đặc biệt | Duration 1. Producer skill 55 có `skill[8] = -1`. Full command-disable/P5 behavior chưa generalized hoàn toàn. | `10` nếu active | có nếu active | FLAG/PENDING | route + P5 disabled smoke |
| 10 | Tê Liệt | Mỗi lần hành động lại mất không ít thời gian chờ đợi. | `[321,332,4]` | catch/status/action multiplier family | Duration 4. Không lưu stat value trong apply switch. Catch multiplier index 3 = `12 / 10`, tức 1.2x. Producer skill 41/47 có `skill[8] = 10` trong apply chance family. | `11` | có | PORTED/PARTIAL | checkpoint bắt buộc |

## Producer mapping cần nhớ

| Nhóm | Id | Producer skills | Param số cụ thể |
| --- | ---: | --- | --- |
| buff | 4 | 21, 27 | `skill[8] = 10`, defense +10% |
| buff | 7 | 42, 48 | `skill[8] = 5`, speed +5% |
| buff | 10 | 62, 68 | `skill[8] = 5` nhưng source bỏ qua, dùng row `-1` |
| debuff | 0 | 1, 7 | divisor 4 hoặc 3 cho HP tick |
| debuff | 3 | 13, 19 | delayed damage 150% hoặc 200% stored raw |
| debuff | 5 | 32, 38, 61, 67 | speed down 10%, 10%, 5%, 5% |
| debuff | 6 | 33, 39 | damage output -10% |
| debuff | 7 | 51, 57 | defense -20% |

## Next smoke/code order

Trước khi quay lại từng skill phức tạp, dùng bảng này làm expected value:

1. form0 low HP: HP threshold 30%, attack +100%.
2. form4 crit: +10 điểm % crit chance.
3. form10 HP floor: floor 10 HP.
4. buff13: clear debuff + heal 20% max HP.
5. debuff3: delayed damage 150%/200% stored raw.
6. debuff2: command disable duration 3.

## Correction 2026-07-13: aq.c[3] naming

Do not treat the whole `aq.c[3]` table as battle status.

Correct taxonomy is documented in `247_aqc3_not_battle_status_clarification_vi.md`:

- `aq.c[3][0..11]`: pet-held property/passive rows. These are equipped/stored on pet as `game.b.c[5]` and checked by `game.b.f(byte)`. They are not P12/P13 buff/debuff queue rows.
- `aq.c[3][12..16]`: evolution/mutation material inventory rows.
- `aq.c[3][17]`: key/special inventory row.

So rows like `Hồn Tinh Thạch`, `Quỷ Thần Tinh Thạch`, and `Chìa khóa` must not be given battle behavior.
They appear here only because the original game reuses `aq.c[3]` for both pet properties and material/key text/icon rows.

Catch chance note: `aq.c[3][11]` is a pet-held property on the active/player pet, not an ability on the wild pet.
In `game.d.b(itemId)`, `h.f((byte)11)` selects catch multiplier index `4` (`12/10`) and then applies row param `[5] = 20` again as `(100 + 20) / 100`.
Before later cap/clamp, this is approximately `1.2 * 1.2 = 1.44x`.
