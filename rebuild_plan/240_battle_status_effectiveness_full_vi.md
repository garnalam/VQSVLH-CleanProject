# 240 - Battle Status Effectiveness Full VI

Ngày: 2026-07-13

Mục tiêu: tài liệu tiếng Việt đầy đủ cho lớp trạng thái/effect trong battle.
Tài liệu này dùng để đối chiếu source gốc, text tiếng Việt trong game, logic tác
động vào pet ta/pet địch, icon HUD, body visual P12/P13, và smoke cần có.

Tài liệu này là audit/doc only, chưa đổi runtime.

Ghi chú cập nhật: bảng này giữ mô tả source tổng quan. Khi cần con số cụ thể
cho từng buff/debuff/form/status, dùng bản gộp
`244_battle_status_effectiveness_numeric_full_vi.md`.

## Source đang dùng

| Source | Vai trò |
| --- | --- |
| `modules/source_code/decoded/decompiled_source_cfr/game/b.java` | field `c/d/v/w/x/N/K`, apply/tick buff/debuff, damage formula |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java` | state P7/P12/P13/P17/P21, active queue consumer, catch hooks |
| `modules/source_code/decoded/decompiled_source_cfr/game/h.java` | HUD icon/duration, UI list text |
| `modules/script/decoded/data__script__db.mid.json` | `aq.c[3]`, `aq.c[6]`, `aq.c[7]` raw rows |
| `modules/script/decoded/data__script__chs.mid.json` | text `an.f(...)`: tên + mô tả tiếng Việt |
| `modules/script/decoded/data__script__bufDebuf.mid.json` | visual P12/P13 cho active queue |
| `modules/script/decoded/data__script__speffect.mid.json` | AH/special effect rows |
| `modules/img/decoded/data__img__img_804.mid.png` | sprite/status icon sheet qua sprite `325` |

## Ba hệ trạng thái khác nhau

Không được trộn 3 hệ này với nhau.

| Hệ | Table source | Field trong `game.b` | Queue | Icon HUD | Body visual P12/P13 |
| --- | --- | --- | --- | --- | --- |
| Form/status | `aq.c[3]` | `c[5]`, check bằng `f(byte)` | không có | không qua status slot | không qua P12/P13 queue |
| Buff bên bản thân | `aq.c[6]` | `v[16][5]` | `x[0][0..2]` | `buffId + 12` | chỉ id source gate |
| Debuff bên target | `aq.c[7]` | `w[11][5]` | `x[1][0..2]` | `debuffId + 1` | chỉ id source gate |

## Rule icon HUD từ game.h

Source: `game.h.a(b)` và `game.h.b(b)`.

| Điều kiện | Icon cell sprite `325` | Duration cell |
| --- | ---: | ---: |
| slot trống | `0` | `145` |
| buff id `id`, `v[id][0] > 0` | `id + 12` | `134 + v[id][0]` |
| debuff id `id`, `w[id][0] > 0` | `id + 1` | `134 + w[id][0]` |

Thứ tự hiển thị:

```text
buff slot0, debuff slot0, buff slot1, debuff slot1, buff slot2, debuff slot2
```

## Rule body visual P12/P13

Source `game.d` chỉ tạo active visual cho các id nằm trong gate:

| Bank | Có P12/P13 body visual | Không có P12/P13 visual nhưng vẫn có logic/icon |
| --- | --- | --- |
| buff bank `0` | `3,5,13` | `0,1,2,4,6,7,8,9,10,11,12,14` |
| debuff bank `1` | `0,1,2,3,8,9,10` | `4,5,6,7` |

Không có P12/P13 visual không có nghĩa là không có hiệu lực. Ví dụ debuff 7
giảm phòng ngự nhưng không có body visual active queue.

## Form/status aq.c[3]

Các row này là trạng thái/form/material của pet. Battle chỉ dùng một số id làm
passive/hook. Id 12..17 là material/key, không phải battle status.

| Id | Tên | Mô tả source | Raw row | Tác động logic | Visual/icon | Rebuild status | Smoke cần chốt |
| ---: | --- | --- | --- | --- | --- | --- | --- |
| 0 | Mạn Đà La Thạch | Sủng vật hiện tại trị số sinh mạng thấp hơn 30%, gia tăng lực công kích 100%; | `[213,1,237,5,1,30,100]` | Owner pet HP thấp thì tăng công trong damage formula | không HUD queue | PORTED/PARTIAL | so damage HP cao/thấp |
| 1 | Hồng Sắc Hải Loa | Sủng vật gia tăng lực công kích 10%; | `[214,2,238,5,1,10]` | owner tăng attack formula | không HUD queue | PORTED/PARTIAL | damage tăng đúng % |
| 2 | Quy Xác Toái Phiến | Sủng vật Gia tăng khả năng phòng ngự 15%; | `[215,3,239,5,1,15]` | target/owner tăng defense trong raw damage path | không HUD queue | PORTED/PARTIAL | target nhận damage thấp hơn |
| 3 | Ô Nha Uế | Gia tăng sức đối kháng 20%; | `[216,4,240,5,1,20]` | giảm/block tỉ lệ dính debuff | không HUD queue | PORTED/PARTIAL | forced debuff roll bị block |
| 4 | Viễn Cổ Long Cốt | Sủng vật gia tăng tỉ số bạo kích 10%; | `[217,5,241,5,1,10]` | tăng crit chance | không HUD queue | PORTED/PARTIAL | crit chance smoke |
| 5 | Mật Phong Sào | Sau khi chiến đấu sủng vật lấy được giá trị kinh nghiệm 20%; | `[218,6,242,5,1,20]` | EXP multiplier cho participant | không HUD queue | PORTED/PARTIAL | EXP participant smoke |
| 6 | Ký Cư Giải Xác | Mang theo đạo cụ sủng vật tuy không tham chiến cũng có thể đạt được giá trị kinh nghiệm tương ứng; | `[219,7,243,5,1,100]` | reserve EXP share | không HUD queue | PORTED/PARTIAL | reserve EXP smoke |
| 7 | Linh Trùng Thi Hài | Giao tranh bắt đầu; | `[220,8,244,5,1]` | chưa chứng minh consumer battle trực tiếp | UNKNOWN/PENDING | UNKNOWN/PENDING | source search trước khi code |
| 8 | Hấp Huyết Đằng Mạn | Sủng vật công kích là cách tốt nhất để khôi phục lại một phần trị số sinh mạng; | `[221,9,245,5,1,10,20]` | attacker có chance tự heal sau hit | không HUD queue | PORTED/PARTIAL | hit smoke thấy attacker heal |
| 9 | Cá Thờn Bơn | Sủng vật công kích nhất định trúng mục tiêu; | `[222,10,246,5,1]` | missChance về 0 | không HUD queue | PORTED/PARTIAL | high-miss setup vẫn hit |
| 10 | Cảm Lãm Chi Diệp | Bất luận đối thủ đã bị thương thế nào, trị số sinh mạng cũng sẽ không thấp hơn 10 điểm (bắt được dùng); | `[223,11,247,5,1,10]` | HP floor / capture-related source path | không HUD queue | PORTED/PARTIAL | lethal hit còn HP floor |
| 11 | Sủng vật lôi đạt | Gia tăng tỷ lệ bắt được sủng vật 20%; | `[224,12,248,5,1,20]` | tăng catch chance | không HUD queue | PORTED/PARTIAL | catch multiplier smoke |
| 12 | Tinh Nguyên Thạch | Tài liệu liên quan đến tiến hóa; | `[225,13,249,1000,0]` | non-battle material | không battle | NON_BATTLE | không cần battle smoke |
| 13 | Thiên Giới Tinh Thạch | Tài liệu liên quan tiến hóa bậc cao; | `[226,14,250,2,1]` | non-battle material | không battle | NON_BATTLE | không cần battle smoke |
| 14 | Thiên Địa Thần Thạch | Tài liệu liên quan đến tiến hóa bậc cao hiếm có; | `[227,15,251,10,1]` | non-battle material | không battle | NON_BATTLE | không cần battle smoke |
| 15 | Hồn Tinh Thạch | Tài liệu liên quan đến dị hoá; | `[228,16,252,2,1]` | non-battle material | không battle | NON_BATTLE | không cần battle smoke |
| 16 | Quỷ Thần Tinh Thạch | Tài liệu liên quan đến dị hóa bậc cao hiếm có; | `[229,17,253,10,1]` | non-battle material | không battle | NON_BATTLE | không cần battle smoke |
| 17 | Chìa khóa | Có thể mở hoàng kim bảo rương; | `[363,52,364,1,1]` | non-battle key | không battle | NON_BATTLE | không cần battle smoke |

## Buff bên bản thân aq.c[6]

Tất cả buff active phải có HUD icon khi `v[id][0] > 0`. Chỉ id `3,5,13`
có P12/P13 body visual theo source gate.

| Id | Tên | Mô tả source | Raw row | Logic chính | Icon cell | Body visual | Rebuild status | Smoke cần chốt |
| ---: | --- | --- | --- | --- | ---: | --- | --- | --- |
| 0 | Súc Lực | Sự chuyển hướng không công kích, nhưng cải thiện khả năng phòng ngự, những đợt công kích tiếp theo sẽ làm gia tăng thương tổn. | `[333,348,2,30,190]` | tăng defense, lưu extra damage | `12` | không | PORTED/PARTIAL | formula/counter smoke |
| 1 | Phá Phủ | Gia tăng thương tổn, khả năng phòng ngự giảm, duy trì liên tục Y hiệp. | `[334,349,3,50,50]` | damage tăng, defense bản thân giảm | `13` | không | PORTED | damage/defense smoke |
| 2 | Kinh Cức | Khả năng phòng ngự tự gia tăng, công kích ngược lại đối thủ tạo thành thương tổn, duy trì liên tục Y hiệp. | `[335,350,3,30,10]` | defense up, phản damage/hook liên quan | `14` | không | PORTED/PARTIAL | reflect/defense smoke |
| 3 | Khôi phục | Mỗi hiệp khôi phục trị số sinh mạng nhất định, duy trì liên tục Y hiệp. | `[336,351,3,5,-1]` | heal khi apply và tick mỗi hiệp | `15` | có | PORTED | checkpoint bắt buộc |
| 4 | Phòng ngự | Gia tăng giá trị phòng ngự, duy trì liên tục Y hiệp. | `[337,352,2,-1,-1]` | defense up theo skill param | `16` | không | PORTED/PARTIAL | stat reassert/expiry |
| 5 | Vô hình | Công kích của đối thủ sẽ có một phần tỷ lệ dội ngược lại chính mình, duy trì liên tục Y hiệp. | `[338,353,3,30,-1]` | reflect/store damage chance | `17` | có | PORTED/PARTIAL | visual + reflect |
| 6 | Kiên nhẫn | Cơ hội tiếp xúc để công kích kẻ thù tỷ lệ chỉ là một nửa, duy trì liên tục Y hiệp. | `[339,354,3,50,-1]` | giảm/chuyển damage theo source oddity | `18` | không | PORTED/PARTIAL | owner-side formula smoke |
| 7 | Linh Xảo | Gia tăng giá trị Linh Xảo, duy trì liên tục Y hiệp. | `[340,355,2,-1,-1]` | speed up theo skill param | `19` | không | PORTED | speed/order smoke |
| 8 | Điện áp | Gia tăng tiêu hao giá trị kỹ năng, làm gia tăng thương tổn, duy trì liên tục Y hiệp. | `[341,356,4,30,-1]` | tăng PP cost và damage | `20` | không | PORTED/PARTIAL | PP drain + damage |
| 9 | Hỏa Thạch | Gia tăng giá trị Linh Xảo, phòng ngự giá trị giảm xuống, duy trì liên tục Y hiệp. | `[342,357,3,50,50]` | speed up, defense down | `21` | không | PORTED/PARTIAL | stat delta + turn hook |
| 10 | Man Lực | Lực lượng giá trị Gia tăng, duy trì liên tục Y hiệp. | `[343,358,2,-1,-1]` | attack up | `22` | không | PORTED | checkpoint bắt buộc |
| 11 | Thâu Thủ | Trạng thái thuận lợi của đối thủ sẽ được chuyển qua cho bạn, duy trì liên tục Y hiệp. | `[344,359,3,-1,-1]` | copy buff từ donor rồi clear donor | `23` | không | PARTIAL | donor vector smoke |
| 12 | Gia Tốc | Mỗi hiệp công kích 2 lần, duy trì liên tục Y hiệp, lần thứ hai không cần lựa chọn kỹ năng, lặp lại kỹ năng lần trước đó, hơn nữa không cần thiết hao tổn kỹ năng. | `[345,360,2,-1,-1]` | K12 follow-up/PP conservation | `24` | không | PORTED/PARTIAL | producer->consumer |
| 13 | Thạch Hóa | Giải phóng các trạng thái dị thường, mỗi hiệp khôi phục 20% hạn mức cao nhất của trị số sinh mạng, duy trì liên tục 3 hiệp. | `[346,361,3,20,-1]` | clear debuff + heal tick | `25` | có | PORTED | cleanse+heal+visual |
| 14 | Thạch Phu | Giải phóng các trạng thái dị thường, sẽ có 3 hiệp miễn dịch ở những nơi có trạng thái dị thường. | `[347,362,3,-1,-1]` | clear debuff + block abnormal/debuff | `26` | không | PORTED/PARTIAL | checkpoint bắt buộc |

## Debuff bên target aq.c[7]

Tất cả debuff active phải có HUD icon khi `w[id][0] > 0`. Chỉ id
`0,1,2,3,8,9,10` có P12/P13 body visual theo source gate.

| Id | Tên | Mô tả source | Raw row | Logic chính | Icon cell | Body visual | Rebuild status | Smoke cần chốt |
| ---: | --- | --- | --- | --- | ---: | --- | --- | --- |
| 0 | Gieo Hạt | Mỗi hiệp giảm bớt X điểm trị số sinh mạng, duy trì liên tục Y hiệp. | `[311,322,3]` | tick HP damage; skill 3/9 conditional damage | `1` | có | PORTED | checkpoint bắt buộc |
| 1 | Mê Muội | Gia tăng tốc độ công kích đối thủ có thể sử dụng đạo cụ, duy trì liên tục Y hiệp. | `[312,323,2]` | flag; skill 23/29 conditional; catch multiplier trong rebuild | `2` | có | PORTED/PARTIAL | flag/conditional/catch smoke |
| 2 | Quấn Quanh | Gia tăng tốc độ, sủng vật không thể thay thế, bạn cũng không thể trốn chạy, không thể sử dụng đạo cụ, ...duy trì liên tục Y hiệp. | `[313,324,3]` | block switch/item/run; catch multiplier | `3` | có | PORTED/PARTIAL | command disable smoke |
| 3 | Thực Loại | Sau Y hiệp đã bị X đả thương, nếu tử vong thì hiệu quả Thực Loại biến mất. | `[314,325,3]` | delayed HP damage gần hết duration | `4` | có | PORTED | delayed tick smoke |
| 4 | Mục | Hiệu quả giảm chỉ duy trì liên tục Y hiệp. | `[315,326,3]` | accuracy/miss chance hook | `5` | không | PORTED/PARTIAL | miss chance smoke |
| 5 | Chậm Chạp | Giá trị tạm thời giảm xuống, duy trì liên tục Y hiệp. | `[316,327,3]` | speed down | `6` | không | PORTED | checkpoint bắt buộc |
| 6 | Nhụt Chí | Tỉ suất thương tổn giảm xuống, duy trì liên tục Y hiệp. | `[317,328,3]` | giảm incoming damage percent | `7` | không | PORTED/PARTIAL | damage reduction smoke |
| 7 | Phòng Ngự | Giá trị phòng ngự giảm xuống, duy trì liên tục Y hiệp. | `[318,329,3]` | defense down | `8` | không | PORTED | checkpoint bắt buộc |
| 8 | Quỷ Mị | Lợi dụng sự công kích của đối thủ mới có thể chính mình công kích được đối phương. | `[319,330,4]` | source route còn đặc biệt/zero-power, chưa claim gameplay | `9` nếu active | có nếu active | FLAG/PENDING | chứng minh route trước |
| 9 | Hỗn Loạn | Không thể thay đổi sủng vật của mình, duy trì liên tục Y hiệp. | `[320,331,1]` | text nói không thể đổi pet; source route còn đặc biệt | `10` nếu active | có nếu active | FLAG/PENDING | route + P5 disabled smoke |
| 10 | Tê Liệt | Mỗi lần hành động lại mất không ít thời gian chờ đợi. | `[321,332,4]` | catch/status/action multiplier family | `11` | có | PORTED/PARTIAL | checkpoint bắt buộc |

## Smoke bắt buộc trước khi quay lại skill

| Checkpoint | Setup | Cần thấy bằng hình | Cần chứng minh bằng logic/trace |
| --- | --- | --- | --- |
| `battle_status_buff3_heal_tick` | buff3 trên pet bị mất HP | icon `15`, duration, P12 visual | HP tăng, duration giảm |
| `battle_status_buff10_attack_up_damage` | attacker có buff10 | icon `22` | damage tăng so baseline |
| `battle_status_buff14_blocks_debuff` | target có buff14, skill gây debuff | icon `26` | `appliedDebuffId=-1`, không có debuff slot |
| `battle_status_debuff0_damage_tick` | target có debuff0 | icon `1`, P12 visual | HP giảm theo divisor source |
| `battle_status_debuff5_speed_down` | target có debuff5 | icon `6` | speed giảm, miss/order trace đổi |
| `battle_status_debuff7_defense_down` | target có debuff7 | icon `8` | damage tiếp theo tăng |
| `battle_status_debuff10_catch_multiplier` | wild target có debuff10 | icon `11`, P12 visual | catch chance dùng multiplier |
| `battle_status_form9_no_miss` | attacker có form9, target speed cao | không HUD queue | miss chance về 0, vẫn hit |

## Done criteria

Một status chỉ coi là đủ chắc khi có:

1. source row + tên/mô tả tiếng Việt;
2. apply đúng pet ta/pet địch;
3. slot `v/w` và queue `x[0]/x[1]` đúng;
4. icon/duration đúng công thức `game.h`;
5. visual P12/P13 đúng gate, không invent visual;
6. logic thật sự đổi HP/stat/damage/catch/miss/EXP/turn;
7. hết hạn/clear không để stale stat;
8. có smoke PNG/headless và trace/assert.

## Bước tiếp theo

Tạo smoke-only/test-support cho 8 checkpoint bắt buộc. Chỉ sau khi smoke fail
mới sửa runtime đúng nhánh fail. Không mở client/game jar tự động.
