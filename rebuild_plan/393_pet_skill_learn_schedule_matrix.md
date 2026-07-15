# 393 Pet Skill Learn Schedule Matrix

Status: SOURCE-AUDITED / DATA-MATRIX.

Purpose:

- Explain exactly when a pet can learn a new skill after level-up.
- Map every battle species to its element lane and `aq.c[8]` learn group.
- Keep this as a lookup table before changing P23 learn-skill behavior.

## Source Rule

Source files:

- `modules/source_code/decoded/decompiled_source_cfr/game/k.java` -> `aq()/ar()/as()/at()` level-up and P23 UI flow.
- `modules/source_code/decoded/decompiled_source_cfr/game/i.java` -> `G()`, `X()`, `g(byte)`.
- `modules/script/original/db.mid` -> source `aq.c` tables.

Level-up opens learn-skill UI only when:

```java
skillCount < 5 && skillCount < level / 10 + 1 && candidates != null
```

Candidate generation mirrors source `game.i.G()`:

```java
learnGroup = aq.c[0][species][18];
element = aq.c[0][species][1];
tier = X(level); // thresholds {5,10,20,30,40}
maxLearnTier = aq.c[8][learnGroup][tier];
candidate skill ids = element * 10 .. element * 10 + 9
include skill when aq.c[1][skill][4] <= maxLearnTier and the pet does not already know it
```

Important interpretation:

- A pet normally starts with its lane starter skill `element * 10`.
- New skill slots effectively unlock at levels `10`, `20`, `30`, and `40`.
- The UI candidate list is not a fixed one-skill-per-level schedule. It is the eligible pool minus current known skills.
- If a pet skips a candidate at level 10, that old candidate can still appear at later level-ups while a slot is open.
- Full 5-slot pets do not get replacement UI in source parity. See `392_battle_p23_learn_skill_full_slot_replace_audit.md`.

## Learn Tier Bands

| Source tier index | Level band | Slot gate meaning |
| --- | --- | --- |
| 0 | 5-9 | Starter/default context; no second slot by normal `level / 10 + 1` gate. |
| 1 | 10-19 | Slot 2 can learn if candidates exist. |
| 2 | 20-29 | Slot 3 can learn if candidates exist. |
| 3 | 30-39 | Slot 4 can learn if candidates exist. |
| 4 | 40-50 | Slot 5 can learn if candidates exist. |

## Skill Lane / Learn-Tier Table

| Lane | Skill id | Name | learnTier `aq.c[1][id][4]` | PP | Raw row |
| --- | ---: | --- | ---: | ---: | --- |
| 0 Hỏa | 0 | Hỏa trảo | 0 | 45 | `[0, 117, 529, 100, 0, 45, 0, -1, -1, 0]` |
| 0 Hỏa | 1 | Dương viêm | 0 | 45 | `[0, 118, 530, 50, 0, 45, 2, 0, 4, 0]` |
| 0 Hỏa | 2 | Diễm kích | 0 | 45 | `[0, 119, 531, 100, 0, 45, 2, 1, 10, 0]` |
| 0 Hỏa | 3 | Hỏa Vân triệu | 1 | 30 | `[0, 120, 532, 100, 1, 30, 0, -1, 120, 0]` |
| 0 Hỏa | 4 | Thiên Hỏa tế | 1 | 10 | `[0, 121, 533, 0, 1, 10, 1, 0, -1, 1]` |
| 0 Hỏa | 5 | Viêm lôi phá | 1 | 10 | `[0, 122, 534, 0, 1, 10, 1, 1, -1, 1]` |
| 0 Hỏa | 6 | Hỏa diễm đao | 2 | 30 | `[0, 123, 535, 150, 2, 30, 0, -1, -1, 0]` |
| 0 Hỏa | 7 | Chước nhiệt chi xúc | 2 | 30 | `[0, 124, 536, 75, 2, 30, 2, 0, 3, 0]` |
| 0 Hỏa | 8 | Liệt diễm phong bạo | 3 | 15 | `[0, 125, 537, 200, 3, 15, 2, 1, 20, 0]` |
| 0 Hỏa | 9 | Vĩnh hằng hỏa ảnh | 3 | 15 | `[0, 126, 538, 200, 3, 15, 0, -1, 250, 0]` |
| 1 Mộc | 10 | Diệp Toàn | 0 | 45 | `[1, 127, 539, 100, 0, 45, 0, -1, -1, 0]` |
| 1 Mộc | 11 | Quang phản | 0 | 45 | `[1, 128, 540, 90, 0, 45, 0, -1, 10, 0]` |
| 1 Mộc | 12 | Đằng Phược | 0 | 45 | `[1, 129, 541, 50, 0, 45, 2, 2, -1, 0]` |
| 1 Mộc | 13 | Thảo Chủng | 1 | 30 | `[1, 130, 542, 50, 1, 30, 2, 3, 150, 0]` |
| 1 Mộc | 14 | Đằng chi bích lũy | 1 | 10 | `[1, 131, 543, 0, 1, 10, 1, 2, -1, 1]` |
| 1 Mộc | 15 | Thảo nguyện thuật | 1 | 10 | `[1, 132, 544, 0, 1, 10, 1, 3, -1, 1]` |
| 1 Mộc | 16 | Châm Diệp Trảm | 2 | 30 | `[1, 133, 545, 150, 2, 30, 0, -1, -1, 0]` |
| 1 Mộc | 17 | Diệp chi ân huệ | 2 | 30 | `[1, 134, 546, 130, 2, 30, 0, -1, 40, 0]` |
| 1 Mộc | 18 | Đằng mạn triền nhiễu | 3 | 15 | `[1, 135, 547, 150, 3, 15, 2, 2, -1, 0]` |
| 1 Mộc | 19 | Quang hợp hiệu ứng | 3 | 15 | `[1, 136, 548, 150, 3, 15, 2, 3, 200, 0]` |
| 2 Thổ | 20 | Hất bụi | 0 | 45 | `[2, 137, 549, 100, 0, 45, 0, -1, -1, 0]` |
| 2 Thổ | 21 | Thổ thuẫn | 0 | 45 | `[2, 138, 550, 80, 0, 45, 1, 4, 10, 0]` |
| 2 Thổ | 22 | Bão cát | 0 | 45 | `[2, 139, 551, 50, 0, 45, 2, 1, 25, 0]` |
| 2 Thổ | 23 | Nham băng | 1 | 30 | `[2, 140, 552, 100, 1, 30, 0, -1, 250, 0]` |
| 2 Thổ | 24 | Người bảo vệ Địa Giới | 1 | 10 | `[2, 141, 553, 0, 1, 10, 1, 13, -1, 1]` |
| 2 Thổ | 25 | Thạch phu thuật | 1 | 10 | `[2, 142, 554, 0, 1, 10, 1, 14, -1, 1]` |
| 2 Thổ | 26 | Nham bạo | 2 | 30 | `[2, 143, 555, 150, 2, 30, 0, -1, -1, 0]` |
| 2 Thổ | 27 | Hàng rào cát đá | 2 | 30 | `[2, 144, 556, 100, 2, 30, 1, 4, 10, 0]` |
| 2 Thổ | 28 | Bão cát | 3 | 15 | `[2, 145, 557, 150, 3, 15, 2, 1, 25, 0]` |
| 2 Thổ | 29 | Thổ Chi Loạn Vũ | 3 | 15 | `[2, 146, 558, 180, 3, 15, 0, -1, 300, 0]` |
| 3 Thủy | 30 | Bong bóng | 0 | 45 | `[3, 147, 559, 100, 0, 45, 0, -1, -1, 0]` |
| 3 Thủy | 31 | Băng lao | 0 | 45 | `[3, 148, 560, 60, 0, 45, 2, 4, 1, 0]` |
| 3 Thủy | 32 | Tuyết ảnh | 0 | 45 | `[3, 149, 561, 60, 0, 45, 2, 5, 10, 0]` |
| 3 Thủy | 33 | Thủy trụ | 1 | 30 | `[3, 150, 562, 100, 1, 30, 2, 6, 10, 0]` |
| 3 Thủy | 34 | Thuật cầu nguyện | 1 | 10 | `[3, 151, 563, 0, 1, 10, 1, 5, -1, 1]` |
| 3 Thủy | 35 | Thủy bích | 1 | 10 | `[3, 152, 564, 0, 1, 10, 1, 6, -1, 1]` |
| 3 Thủy | 36 | Bạo Phong Tuyết | 2 | 30 | `[3, 153, 565, 150, 2, 30, 0, -1, -1, 0]` |
| 3 Thủy | 37 | Lá chắn gió tuyết | 2 | 30 | `[3, 154, 566, 100, 2, 30, 2, 4, 2, 0]` |
| 3 Thủy | 38 | Băng Phong Hãm Tịnh | 3 | 15 | `[3, 155, 567, 150, 3, 15, 2, 5, 10, 0]` |
| 3 Thủy | 39 | Ray lạnh | 3 | 15 | `[3, 156, 568, 150, 3, 15, 2, 6, 10, 0]` |
| 4 Điện | 40 | Điện giật | 0 | 45 | `[4, 157, 569, 100, 0, 45, 0, -1, -1, 0]` |
| 4 Điện | 41 | Lôi thiểm | 0 | 45 | `[4, 158, 570, 90, 0, 45, 2, 10, 10, 0]` |
| 4 Điện | 42 | Nạp điện | 0 | 45 | `[4, 159, 571, 90, 0, 45, 1, 7, 5, 0]` |
| 4 Điện | 43 | Sóng điện từ | 1 | 30 | `[4, 160, 572, 100, 1, 30, 0, -1, -1, 0]` |
| 4 Điện | 44 | Đoạt mệnh cao áp | 1 | 10 | `[4, 161, 573, 0, 1, 10, 1, 8, -1, 1]` |
| 4 Điện | 45 | Điện năng chuyển đổi | 1 | 10 | `[4, 162, 574, 0, 1, 10, 1, 9, -1, 1]` |
| 4 Điện | 46 | Tia lửa điện | 2 | 30 | `[4, 163, 575, 150, 2, 30, 0, -1, -1, 0]` |
| 4 Điện | 47 | Chùm sấm sét | 2 | 30 | `[4, 164, 576, 130, 2, 30, 2, 10, 10, 0]` |
| 4 Điện | 48 | Điện quang thạch hỏa | 3 | 15 | `[4, 165, 577, 130, 3, 15, 1, 7, 5, 0]` |
| 4 Điện | 49 | Cảm ứng điện từ | 3 | 15 | `[4, 166, 578, 180, 3, 15, 0, -1, -1, 0]` |
| 5 Ảnh | 50 | Ảnh thứ | 0 | 45 | `[5, 167, 579, 100, 0, 45, 0, -1, -1, 0]` |
| 5 Ảnh | 51 | Chú oán | 0 | 45 | `[5, 168, 580, 80, 0, 45, 2, 7, 20, 0]` |
| 5 Ảnh | 52 | Quỷ độc | 0 | 45 | `[5, 169, 581, 80, 0, 45, 0, -1, 5, 0]` |
| 5 Ảnh | 53 | Cơn ác mộng | 1 | 30 | `[5, 170, 582, 200, 1, 30, 0, -1, 200, 0]` |
| 5 Ảnh | 54 | Mị ảnh | 1 | 10 | `[5, 171, 583, 0, 1, 10, 2, 8, 40, 0]` |
| 5 Ảnh | 55 | Hỗn loạn | 1 | 10 | `[5, 172, 584, 0, 1, 10, 2, 9, -1, 0]` |
| 5 Ảnh | 56 | Độc ảnh thứ | 2 | 30 | `[5, 173, 585, 150, 2, 30, 0, -1, -1, 0]` |
| 5 Ảnh | 57 | Chú Phược Quỷ Lao | 2 | 30 | `[5, 174, 586, 120, 2, 30, 2, 7, 20, 0]` |
| 5 Ảnh | 58 | Quỷ độc tín ngưỡng | 3 | 15 | `[5, 175, 587, 100, 3, 15, 0, -1, 8, 0]` |
| 5 Ảnh | 59 | Lời nguyền cuối cùng | 3 | 15 | `[5, 176, 588, 250, 3, 15, 0, -1, 250, 0]` |
| 6 Phong | 60 | Phong nhận | 0 | 45 | `[6, 177, 589, 100, 0, 45, 0, -1, -1, 0]` |
| 6 Phong | 61 | Phong áp | 0 | 45 | `[6, 178, 590, 80, 0, 45, 2, 5, 5, 0]` |
| 6 Phong | 62 | Thuận phong | 0 | 45 | `[6, 179, 591, 80, 0, 45, 1, 10, 5, 0]` |
| 6 Phong | 63 | Long quyển | 1 | 30 | `[6, 180, 592, 100, 1, 30, 0, -1, 5, 0]` |
| 6 Phong | 64 | Nghịch Phong Đoạt | 1 | 10 | `[6, 181, 593, 0, 1, 10, 1, 11, -1, 0]` |
| 6 Phong | 65 | Vũ Liệt Thuật | 1 | 10 | `[6, 182, 594, 0, 1, 10, 1, 12, -1, 1]` |
| 6 Phong | 66 | Yến Hồi Thiểm | 2 | 30 | `[6, 183, 595, 150, 2, 30, 0, -1, -1, 0]` |
| 6 Phong | 67 | Phong Chi Tuyền Qua | 2 | 30 | `[6, 184, 596, 110, 2, 30, 2, 5, 5, 0]` |
| 6 Phong | 68 | Phong Chi Tí Hữu | 3 | 15 | `[6, 185, 597, 110, 3, 15, 1, 10, 5, 0]` |
| 6 Phong | 69 | Phi Yến Hoàn Sào | 3 | 15 | `[6, 186, 598, 150, 3, 15, 0, -1, 8, 0]` |

## LearnGroup Threshold Matrix

Each learnGroup row stores max allowed skill `learnTier` for source tier indexes `{5,10,20,30,40}`.

| learnGroup | Raw `aq.c[8][group]` | L10 cap | L20 cap | L30 cap | L40 cap | Meaning |
| ---: | --- | ---: | ---: | ---: | ---: | --- |
| 0 | `[0, 0, 0, 1, 1]` | 0 | 0 | 1 | 1 | Caps skill learnTier by milestone; candidate list still subtracts known skills. |
| 1 | `[0, 0, 1, 1, 2]` | 0 | 1 | 1 | 2 | Caps skill learnTier by milestone; candidate list still subtracts known skills. |
| 2 | `[0, 0, 1, 2, 3]` | 0 | 1 | 2 | 3 | Caps skill learnTier by milestone; candidate list still subtracts known skills. |
| 3 | `[0, 1, 2, 3, 3]` | 1 | 2 | 3 | 3 | Caps skill learnTier by milestone; candidate list still subtracts known skills. |

## LearnGroup Candidate Pools By Lane

These are the source pools before subtracting already-known skills. `New at Lx` means newly allowed compared with the previous slot milestone.

| learnGroup | Lane | Starter | L10 pool | New at L20 | New at L30 | New at L40 |
| ---: | --- | --- | --- | --- | --- | --- |
| 0 | 0 Hỏa | `0` Hỏa trảo | `1` Dương viêm, `2` Diễm kích | - | `3` Hỏa Vân triệu, `4` Thiên Hỏa tế, `5` Viêm lôi phá | - |
| 0 | 1 Mộc | `10` Diệp Toàn | `11` Quang phản, `12` Đằng Phược | - | `13` Thảo Chủng, `14` Đằng chi bích lũy, `15` Thảo nguyện thuật | - |
| 0 | 2 Thổ | `20` Hất bụi | `21` Thổ thuẫn, `22` Bão cát | - | `23` Nham băng, `24` Người bảo vệ Địa Giới, `25` Thạch phu thuật | - |
| 0 | 3 Thủy | `30` Bong bóng | `31` Băng lao, `32` Tuyết ảnh | - | `33` Thủy trụ, `34` Thuật cầu nguyện, `35` Thủy bích | - |
| 0 | 4 Điện | `40` Điện giật | `41` Lôi thiểm, `42` Nạp điện | - | `43` Sóng điện từ, `44` Đoạt mệnh cao áp, `45` Điện năng chuyển đổi | - |
| 0 | 5 Ảnh | `50` Ảnh thứ | `51` Chú oán, `52` Quỷ độc | - | `53` Cơn ác mộng, `54` Mị ảnh, `55` Hỗn loạn | - |
| 0 | 6 Phong | `60` Phong nhận | `61` Phong áp, `62` Thuận phong | - | `63` Long quyển, `64` Nghịch Phong Đoạt, `65` Vũ Liệt Thuật | - |
| 1 | 0 Hỏa | `0` Hỏa trảo | `1` Dương viêm, `2` Diễm kích | `3` Hỏa Vân triệu, `4` Thiên Hỏa tế, `5` Viêm lôi phá | - | `6` Hỏa diễm đao, `7` Chước nhiệt chi xúc |
| 1 | 1 Mộc | `10` Diệp Toàn | `11` Quang phản, `12` Đằng Phược | `13` Thảo Chủng, `14` Đằng chi bích lũy, `15` Thảo nguyện thuật | - | `16` Châm Diệp Trảm, `17` Diệp chi ân huệ |
| 1 | 2 Thổ | `20` Hất bụi | `21` Thổ thuẫn, `22` Bão cát | `23` Nham băng, `24` Người bảo vệ Địa Giới, `25` Thạch phu thuật | - | `26` Nham bạo, `27` Hàng rào cát đá |
| 1 | 3 Thủy | `30` Bong bóng | `31` Băng lao, `32` Tuyết ảnh | `33` Thủy trụ, `34` Thuật cầu nguyện, `35` Thủy bích | - | `36` Bạo Phong Tuyết, `37` Lá chắn gió tuyết |
| 1 | 4 Điện | `40` Điện giật | `41` Lôi thiểm, `42` Nạp điện | `43` Sóng điện từ, `44` Đoạt mệnh cao áp, `45` Điện năng chuyển đổi | - | `46` Tia lửa điện, `47` Chùm sấm sét |
| 1 | 5 Ảnh | `50` Ảnh thứ | `51` Chú oán, `52` Quỷ độc | `53` Cơn ác mộng, `54` Mị ảnh, `55` Hỗn loạn | - | `56` Độc ảnh thứ, `57` Chú Phược Quỷ Lao |
| 1 | 6 Phong | `60` Phong nhận | `61` Phong áp, `62` Thuận phong | `63` Long quyển, `64` Nghịch Phong Đoạt, `65` Vũ Liệt Thuật | - | `66` Yến Hồi Thiểm, `67` Phong Chi Tuyền Qua |
| 2 | 0 Hỏa | `0` Hỏa trảo | `1` Dương viêm, `2` Diễm kích | `3` Hỏa Vân triệu, `4` Thiên Hỏa tế, `5` Viêm lôi phá | `6` Hỏa diễm đao, `7` Chước nhiệt chi xúc | `8` Liệt diễm phong bạo, `9` Vĩnh hằng hỏa ảnh |
| 2 | 1 Mộc | `10` Diệp Toàn | `11` Quang phản, `12` Đằng Phược | `13` Thảo Chủng, `14` Đằng chi bích lũy, `15` Thảo nguyện thuật | `16` Châm Diệp Trảm, `17` Diệp chi ân huệ | `18` Đằng mạn triền nhiễu, `19` Quang hợp hiệu ứng |
| 2 | 2 Thổ | `20` Hất bụi | `21` Thổ thuẫn, `22` Bão cát | `23` Nham băng, `24` Người bảo vệ Địa Giới, `25` Thạch phu thuật | `26` Nham bạo, `27` Hàng rào cát đá | `28` Bão cát, `29` Thổ Chi Loạn Vũ |
| 2 | 3 Thủy | `30` Bong bóng | `31` Băng lao, `32` Tuyết ảnh | `33` Thủy trụ, `34` Thuật cầu nguyện, `35` Thủy bích | `36` Bạo Phong Tuyết, `37` Lá chắn gió tuyết | `38` Băng Phong Hãm Tịnh, `39` Ray lạnh |
| 2 | 4 Điện | `40` Điện giật | `41` Lôi thiểm, `42` Nạp điện | `43` Sóng điện từ, `44` Đoạt mệnh cao áp, `45` Điện năng chuyển đổi | `46` Tia lửa điện, `47` Chùm sấm sét | `48` Điện quang thạch hỏa, `49` Cảm ứng điện từ |
| 2 | 5 Ảnh | `50` Ảnh thứ | `51` Chú oán, `52` Quỷ độc | `53` Cơn ác mộng, `54` Mị ảnh, `55` Hỗn loạn | `56` Độc ảnh thứ, `57` Chú Phược Quỷ Lao | `58` Quỷ độc tín ngưỡng, `59` Lời nguyền cuối cùng |
| 2 | 6 Phong | `60` Phong nhận | `61` Phong áp, `62` Thuận phong | `63` Long quyển, `64` Nghịch Phong Đoạt, `65` Vũ Liệt Thuật | `66` Yến Hồi Thiểm, `67` Phong Chi Tuyền Qua | `68` Phong Chi Tí Hữu, `69` Phi Yến Hoàn Sào |
| 3 | 0 Hỏa | `0` Hỏa trảo | `1` Dương viêm, `2` Diễm kích, `3` Hỏa Vân triệu, `4` Thiên Hỏa tế, `5` Viêm lôi phá | `6` Hỏa diễm đao, `7` Chước nhiệt chi xúc | `8` Liệt diễm phong bạo, `9` Vĩnh hằng hỏa ảnh | - |
| 3 | 1 Mộc | `10` Diệp Toàn | `11` Quang phản, `12` Đằng Phược, `13` Thảo Chủng, `14` Đằng chi bích lũy, `15` Thảo nguyện thuật | `16` Châm Diệp Trảm, `17` Diệp chi ân huệ | `18` Đằng mạn triền nhiễu, `19` Quang hợp hiệu ứng | - |
| 3 | 2 Thổ | `20` Hất bụi | `21` Thổ thuẫn, `22` Bão cát, `23` Nham băng, `24` Người bảo vệ Địa Giới, `25` Thạch phu thuật | `26` Nham bạo, `27` Hàng rào cát đá | `28` Bão cát, `29` Thổ Chi Loạn Vũ | - |
| 3 | 3 Thủy | `30` Bong bóng | `31` Băng lao, `32` Tuyết ảnh, `33` Thủy trụ, `34` Thuật cầu nguyện, `35` Thủy bích | `36` Bạo Phong Tuyết, `37` Lá chắn gió tuyết | `38` Băng Phong Hãm Tịnh, `39` Ray lạnh | - |
| 3 | 4 Điện | `40` Điện giật | `41` Lôi thiểm, `42` Nạp điện, `43` Sóng điện từ, `44` Đoạt mệnh cao áp, `45` Điện năng chuyển đổi | `46` Tia lửa điện, `47` Chùm sấm sét | `48` Điện quang thạch hỏa, `49` Cảm ứng điện từ | - |
| 3 | 5 Ảnh | `50` Ảnh thứ | `51` Chú oán, `52` Quỷ độc, `53` Cơn ác mộng, `54` Mị ảnh, `55` Hỗn loạn | `56` Độc ảnh thứ, `57` Chú Phược Quỷ Lao | `58` Quỷ độc tín ngưỡng, `59` Lời nguyền cuối cùng | - |
| 3 | 6 Phong | `60` Phong nhận | `61` Phong áp, `62` Thuận phong, `63` Long quyển, `64` Nghịch Phong Đoạt, `65` Vũ Liệt Thuật | `66` Yến Hồi Thiểm, `67` Phong Chi Tuyền Qua | `68` Phong Chi Tí Hữu, `69` Phi Yến Hoàn Sào | - |

## Species Learn Schedule Matrix

Use this table to pick a pet, then cross-reference its `learnGroup` and lane in the candidate-pool table above.

| Species id | Name | Lane | learnGroup | Group caps `{L5,L10,L20,L30,L40}` | Starter skill | L10+ source pool summary | Notes |
| ---: | --- | --- | ---: | --- | --- | --- | --- |
| 0 | Nhiên Dực Bức | 0 Hỏa | 0 | `[0, 0, 0, 1, 1]` | `0` Hỏa trảo | L10<=tier0: `1` Dương viêm, `2` Diễm kích; L20 cap 0; L30 cap 1; L40 cap 1 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 1 | Bom tinh nghịch | 0 Hỏa | 0 | `[0, 0, 0, 1, 1]` | `0` Hỏa trảo | L10<=tier0: `1` Dương viêm, `2` Diễm kích; L20 cap 0; L30 cap 1; L40 cap 1 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 2 | Nhiên Liệp Sư | 0 Hỏa | 1 | `[0, 0, 1, 1, 2]` | `0` Hỏa trảo | L10<=tier0: `1` Dương viêm, `2` Diễm kích; L20 cap 1; L30 cap 1; L40 cap 2 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 3 | Hắc Thán Báo | 0 Hỏa | 1 | `[0, 0, 1, 1, 2]` | `0` Hỏa trảo | L10<=tier0: `1` Dương viêm, `2` Diễm kích; L20 cap 1; L30 cap 1; L40 cap 2 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 4 | Hỏa Diễm Hồ | 0 Hỏa | 1 | `[0, 0, 1, 1, 2]` | `0` Hỏa trảo | L10<=tier0: `1` Dương viêm, `2` Diễm kích; L20 cap 1; L30 cap 1; L40 cap 2 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 5 | Viêm Ma | 0 Hỏa | 3 | `[0, 1, 2, 3, 3]` | `0` Hỏa trảo | L10<=tier1: `1` Dương viêm, `2` Diễm kích, `3` Hỏa Vân triệu, `4` Thiên Hỏa tế, `5` Viêm lôi phá; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 6 | Long bảo bối | 0 Hỏa | 2 | `[0, 0, 1, 2, 3]` | `0` Hỏa trảo | L10<=tier0: `1` Dương viêm, `2` Diễm kích; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 7 | Phệ Hỏa Thú | 0 Hỏa | 2 | `[0, 0, 1, 2, 3]` | `0` Hỏa trảo | L10<=tier0: `1` Dương viêm, `2` Diễm kích; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 8 | Bạo Long Thú | 0 Hỏa | 3 | `[0, 1, 2, 3, 3]` | `0` Hỏa trảo | L10<=tier1: `1` Dương viêm, `2` Diễm kích, `3` Hỏa Vân triệu, `4` Thiên Hỏa tế, `5` Viêm lôi phá; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 9 | Ly Ngưu Ngưu | 0 Hỏa | 2 | `[0, 0, 1, 2, 3]` | `0` Hỏa trảo | L10<=tier0: `1` Dương viêm, `2` Diễm kích; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 10 | Ly Ngưu Thản Khắc | 0 Hỏa | 1 | `[0, 0, 1, 1, 2]` | `0` Hỏa trảo | L10<=tier0: `1` Dương viêm, `2` Diễm kích; L20 cap 1; L30 cap 1; L40 cap 2 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 11 | Nhiệt Bạo Phong Tốc Khuyển | 0 Hỏa | 2 | `[0, 0, 1, 2, 3]` | `0` Hỏa trảo | L10<=tier0: `1` Dương viêm, `2` Diễm kích; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 12 | Tuyệt Đối Linh Độ Khuyển | 0 Hỏa | 3 | `[0, 1, 2, 3, 3]` | `0` Hỏa trảo | L10<=tier1: `1` Dương viêm, `2` Diễm kích, `3` Hỏa Vân triệu, `4` Thiên Hỏa tế, `5` Viêm lôi phá; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 13 | Hỏa Diễm Tường Vân Khuyển | 0 Hỏa | 3 | `[0, 1, 2, 3, 3]` | `0` Hỏa trảo | L10<=tier1: `1` Dương viêm, `2` Diễm kích, `3` Hỏa Vân triệu, `4` Thiên Hỏa tế, `5` Viêm lôi phá; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 14 | Tà Vân Khuyển Thần | 0 Hỏa | 2 | `[0, 0, 1, 2, 3]` | `0` Hỏa trảo | L10<=tier0: `1` Dương viêm, `2` Diễm kích; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 15 | Hỏa Phượng Hoàng | 0 Hỏa | 3 | `[0, 1, 2, 3, 3]` | `0` Hỏa trảo | L10<=tier1: `1` Dương viêm, `2` Diễm kích, `3` Hỏa Vân triệu, `4` Thiên Hỏa tế, `5` Viêm lôi phá; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 16 | Diệp Tán Oa | 1 Mộc | 2 | `[0, 0, 1, 2, 3]` | `10` Diệp Toàn | L10<=tier0: `11` Quang phản, `12` Đằng Phược; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 17 | Thụ Tán Oa | 1 Mộc | 2 | `[0, 0, 1, 2, 3]` | `10` Diệp Toàn | L10<=tier0: `11` Quang phản, `12` Đằng Phược; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 18 | Long Oa | 1 Mộc | 3 | `[0, 1, 2, 3, 3]` | `10` Diệp Toàn | L10<=tier1: `11` Quang phản, `12` Đằng Phược, `13` Thảo Chủng, `14` Đằng chi bích lũy, `15` Thảo nguyện thuật; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 19 | Tiên Nhân Cầu Bảo Bảo | 1 Mộc | 3 | `[0, 1, 2, 3, 3]` | `10` Diệp Toàn | L10<=tier1: `11` Quang phản, `12` Đằng Phược, `13` Thảo Chủng, `14` Đằng chi bích lũy, `15` Thảo nguyện thuật; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 20 | Tiên Nhân Chưởng Thú | 1 Mộc | 1 | `[0, 0, 1, 1, 2]` | `10` Diệp Toàn | L10<=tier0: `11` Quang phản, `12` Đằng Phược; L20 cap 1; L30 cap 1; L40 cap 2 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 21 | Thiết Tý Phách Vương Thụ | 1 Mộc | 1 | `[0, 0, 1, 1, 2]` | `10` Diệp Toàn | L10<=tier0: `11` Quang phản, `12` Đằng Phược; L20 cap 1; L30 cap 1; L40 cap 2 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 22 | Khẩu Đại Thảo | 1 Mộc | 0 | `[0, 0, 0, 1, 1]` | `10` Diệp Toàn | L10<=tier0: `11` Quang phản, `12` Đằng Phược; L20 cap 0; L30 cap 1; L40 cap 1 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 23 | Khốc Tị Mộc Linh | 1 Mộc | 1 | `[0, 0, 1, 1, 2]` | `10` Diệp Toàn | L10<=tier0: `11` Quang phản, `12` Đằng Phược; L20 cap 1; L30 cap 1; L40 cap 2 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 24 | La Phục Thảo | 1 Mộc | 0 | `[0, 0, 0, 1, 1]` | `10` Diệp Toàn | L10<=tier0: `11` Quang phản, `12` Đằng Phược; L20 cap 0; L30 cap 1; L40 cap 1 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 25 | La phục Oa Oa | 1 Mộc | 1 | `[0, 0, 1, 1, 2]` | `10` Diệp Toàn | L10<=tier0: `11` Quang phản, `12` Đằng Phược; L20 cap 1; L30 cap 1; L40 cap 2 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 26 | Tây Quan Tiểu Hương Trư | 1 Mộc | 1 | `[0, 0, 1, 1, 2]` | `10` Diệp Toàn | L10<=tier0: `11` Quang phản, `12` Đằng Phược; L20 cap 1; L30 cap 1; L40 cap 2 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 27 | Trư Lộc Điệp | 1 Mộc | 2 | `[0, 0, 1, 2, 3]` | `10` Diệp Toàn | L10<=tier0: `11` Quang phản, `12` Đằng Phược; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 28 | Cương Tán Ma Cô | 1 Mộc | 2 | `[0, 0, 1, 2, 3]` | `10` Diệp Toàn | L10<=tier0: `11` Quang phản, `12` Đằng Phược; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 29 | Uy Qua Đệ Đệ | 1 Mộc | 1 | `[0, 0, 1, 1, 2]` | `10` Diệp Toàn | L10<=tier0: `11` Quang phản, `12` Đằng Phược; L20 cap 1; L30 cap 1; L40 cap 2 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 30 | Thảo Diệp Cô | 1 Mộc | 3 | `[0, 1, 2, 3, 3]` | `10` Diệp Toàn | L10<=tier1: `11` Quang phản, `12` Đằng Phược, `13` Thảo Chủng, `14` Đằng chi bích lũy, `15` Thảo nguyện thuật; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 31 | Linh Quang Lộc | 1 Mộc | 3 | `[0, 1, 2, 3, 3]` | `10` Diệp Toàn | L10<=tier1: `11` Quang phản, `12` Đằng Phược, `13` Thảo Chủng, `14` Đằng chi bích lũy, `15` Thảo nguyện thuật; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 32 | Kim Chỉ Ốc | 2 Thổ | 2 | `[0, 0, 1, 2, 3]` | `20` Hất bụi | L10<=tier0: `21` Thổ thuẫn, `22` Bão cát; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 33 | Mèo YeDoon | 2 Thổ | 2 | `[0, 0, 1, 2, 3]` | `20` Hất bụi | L10<=tier0: `21` Thổ thuẫn, `22` Bão cát; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 34 | Thỏ Di Lặc Bunny | 2 Thổ | 2 | `[0, 0, 1, 2, 3]` | `20` Hất bụi | L10<=tier0: `21` Thổ thuẫn, `22` Bão cát; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 35 | Toản Địa Khâu Dẫn | 2 Thổ | 0 | `[0, 0, 0, 1, 1]` | `20` Hất bụi | L10<=tier0: `21` Thổ thuẫn, `22` Bão cát; L20 cap 0; L30 cap 1; L40 cap 1 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 36 | Toái nham Khâu Dẫn | 2 Thổ | 1 | `[0, 0, 1, 1, 2]` | `20` Hất bụi | L10<=tier0: `21` Thổ thuẫn, `22` Bão cát; L20 cap 1; L30 cap 1; L40 cap 2 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 37 | Nham Nham Quy | 2 Thổ | 0 | `[0, 0, 0, 1, 1]` | `20` Hất bụi | L10<=tier0: `21` Thổ thuẫn, `22` Bão cát; L20 cap 0; L30 cap 1; L40 cap 1 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 38 | Kiếm Giáp Hạn Quy | 2 Thổ | 2 | `[0, 0, 1, 2, 3]` | `20` Hất bụi | L10<=tier0: `21` Thổ thuẫn, `22` Bão cát; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 39 | Bạch Châm Bảo Bảo | 2 Thổ | 3 | `[0, 1, 2, 3, 3]` | `20` Hất bụi | L10<=tier1: `21` Thổ thuẫn, `22` Bão cát, `23` Nham băng, `24` Người bảo vệ Địa Giới, `25` Thạch phu thuật; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 40 | Hắc Châm Yển Bảo Bảo | 2 Thổ | 3 | `[0, 1, 2, 3, 3]` | `20` Hất bụi | L10<=tier1: `21` Thổ thuẫn, `22` Bão cát, `23` Nham băng, `24` Người bảo vệ Địa Giới, `25` Thạch phu thuật; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 41 | Nham Sơn Long | 2 Thổ | 2 | `[0, 0, 1, 2, 3]` | `20` Hất bụi | L10<=tier0: `21` Thổ thuẫn, `22` Bão cát; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 42 | Zombie Nham Sơn Long | 2 Thổ | 2 | `[0, 0, 1, 2, 3]` | `20` Hất bụi | L10<=tier0: `21` Thổ thuẫn, `22` Bão cát; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 43 | Thổ Lang Chu Chu | 2 Thổ | 1 | `[0, 0, 1, 1, 2]` | `20` Hất bụi | L10<=tier0: `21` Thổ thuẫn, `22` Bão cát; L20 cap 1; L30 cap 1; L40 cap 2 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 44 | Độc Lang Chu | 2 Thổ | 1 | `[0, 0, 1, 1, 2]` | `20` Hất bụi | L10<=tier0: `21` Thổ thuẫn, `22` Bão cát; L20 cap 1; L30 cap 1; L40 cap 2 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 45 | Sừng tê giác bạo long | 2 Thổ | 2 | `[0, 0, 1, 2, 3]` | `20` Hất bụi | L10<=tier0: `21` Thổ thuẫn, `22` Bão cát; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 46 | Khủng giác bạo long | 2 Thổ | 3 | `[0, 1, 2, 3, 3]` | `20` Hất bụi | L10<=tier1: `21` Thổ thuẫn, `22` Bão cát, `23` Nham băng, `24` Người bảo vệ Địa Giới, `25` Thạch phu thuật; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 47 | Chiến Thần Đà | 2 Thổ | 3 | `[0, 1, 2, 3, 3]` | `20` Hất bụi | L10<=tier1: `21` Thổ thuẫn, `22` Bão cát, `23` Nham băng, `24` Người bảo vệ Địa Giới, `25` Thạch phu thuật; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 48 | Tuyết Cầu Bảo Bảo | 3 Thủy | 2 | `[0, 0, 1, 2, 3]` | `30` Bong bóng | L10<=tier0: `31` Băng lao, `32` Tuyết ảnh; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 49 | Người tuyết | 3 Thủy | 2 | `[0, 0, 1, 2, 3]` | `30` Bong bóng | L10<=tier0: `31` Băng lao, `32` Tuyết ảnh; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 50 | Tuyết Sơn Cự Linh | 3 Thủy | 2 | `[0, 0, 1, 2, 3]` | `30` Bong bóng | L10<=tier0: `31` Băng lao, `32` Tuyết ảnh; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 51 | Thủy thủ chim cánh cụt | 3 Thủy | 2 | `[0, 0, 1, 2, 3]` | `30` Bong bóng | L10<=tier0: `31` Băng lao, `32` Tuyết ảnh; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 52 | Bá tước chim cánh cụt | 3 Thủy | 2 | `[0, 0, 1, 2, 3]` | `30` Bong bóng | L10<=tier0: `31` Băng lao, `32` Tuyết ảnh; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 53 | Võ thần chim cánh cụt | 3 Thủy | 3 | `[0, 1, 2, 3, 3]` | `30` Bong bóng | L10<=tier1: `31` Băng lao, `32` Tuyết ảnh, `33` Thủy trụ, `34` Thuật cầu nguyện, `35` Thủy bích; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 54 | Lục Hành Điểu | 3 Thủy | 0 | `[0, 0, 0, 1, 1]` | `30` Bong bóng | L10<=tier0: `31` Băng lao, `32` Tuyết ảnh; L20 cap 0; L30 cap 1; L40 cap 1 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 55 | Cá Miệng Rộng | 3 Thủy | 1 | `[0, 0, 1, 1, 2]` | `30` Bong bóng | L10<=tier0: `31` Băng lao, `32` Tuyết ảnh; L20 cap 1; L30 cap 1; L40 cap 2 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 56 | Mực Ma | 3 Thủy | 0 | `[0, 0, 0, 1, 1]` | `30` Bong bóng | L10<=tier0: `31` Băng lao, `32` Tuyết ảnh; L20 cap 0; L30 cap 1; L40 cap 1 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 57 | Cá Đèn Lồng | 3 Thủy | 1 | `[0, 0, 1, 1, 2]` | `30` Bong bóng | L10<=tier0: `31` Băng lao, `32` Tuyết ảnh; L20 cap 1; L30 cap 1; L40 cap 2 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 58 | Thủy Thượng Phiêu | 3 Thủy | 3 | `[0, 1, 2, 3, 3]` | `30` Bong bóng | L10<=tier1: `31` Băng lao, `32` Tuyết ảnh, `33` Thủy trụ, `34` Thuật cầu nguyện, `35` Thủy bích; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 59 | Phi Thứ Hải Mã | 3 Thủy | 1 | `[0, 0, 1, 1, 2]` | `30` Bong bóng | L10<=tier0: `31` Băng lao, `32` Tuyết ảnh; L20 cap 1; L30 cap 1; L40 cap 2 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 60 | Tấn Cá Kiếm | 3 Thủy | 3 | `[0, 1, 2, 3, 3]` | `30` Bong bóng | L10<=tier1: `31` Băng lao, `32` Tuyết ảnh, `33` Thủy trụ, `34` Thuật cầu nguyện, `35` Thủy bích; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 61 | Cốt Cá Kiếm | 3 Thủy | 2 | `[0, 0, 1, 2, 3]` | `30` Bong bóng | L10<=tier0: `31` Băng lao, `32` Tuyết ảnh; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 62 | Độc Giác Kim Ngư | 3 Thủy | 1 | `[0, 0, 1, 1, 2]` | `30` Bong bóng | L10<=tier0: `31` Băng lao, `32` Tuyết ảnh; L20 cap 1; L30 cap 1; L40 cap 2 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 63 | Tương Quân Giải | 3 Thủy | 3 | `[0, 1, 2, 3, 3]` | `30` Bong bóng | L10<=tier1: `31` Băng lao, `32` Tuyết ảnh, `33` Thủy trụ, `34` Thuật cầu nguyện, `35` Thủy bích; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 64 | Lôi Quang Hầu | 4 Điện | 3 | `[0, 1, 2, 3, 3]` | `40` Điện giật | L10<=tier1: `41` Lôi thiểm, `42` Nạp điện, `43` Sóng điện từ, `44` Đoạt mệnh cao áp, `45` Điện năng chuyển đổi; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 65 | Lôi Vân Miêu | 4 Điện | 1 | `[0, 0, 1, 1, 2]` | `40` Điện giật | L10<=tier0: `41` Lôi thiểm, `42` Nạp điện; L20 cap 1; L30 cap 1; L40 cap 2 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 66 | Điện Nhãn Miêu | 4 Điện | 2 | `[0, 0, 1, 2, 3]` | `40` Điện giật | L10<=tier0: `41` Lôi thiểm, `42` Nạp điện; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 67 | Thân Sĩ Miêu | 4 Điện | 1 | `[0, 0, 1, 1, 2]` | `40` Điện giật | L10<=tier0: `41` Lôi thiểm, `42` Nạp điện; L20 cap 1; L30 cap 1; L40 cap 2 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 68 | Điện Miêu | 4 Điện | 2 | `[0, 0, 1, 2, 3]` | `40` Điện giật | L10<=tier0: `41` Lôi thiểm, `42` Nạp điện; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 69 | Thiểm Điện Miêu | 4 Điện | 3 | `[0, 1, 2, 3, 3]` | `40` Điện giật | L10<=tier1: `41` Lôi thiểm, `42` Nạp điện, `43` Sóng điện từ, `44` Đoạt mệnh cao áp, `45` Điện năng chuyển đổi; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 70 | Phù Du Điện Long | 4 Điện | 1 | `[0, 0, 1, 1, 2]` | `40` Điện giật | L10<=tier0: `41` Lôi thiểm, `42` Nạp điện; L20 cap 1; L30 cap 1; L40 cap 2 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 71 | Phù Du Quỷ Long | 4 Điện | 2 | `[0, 0, 1, 2, 3]` | `40` Điện giật | L10<=tier0: `41` Lôi thiểm, `42` Nạp điện; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 72 | Chuột Điện | 4 Điện | 0 | `[0, 0, 0, 1, 1]` | `40` Điện giật | L10<=tier0: `41` Lôi thiểm, `42` Nạp điện; L20 cap 0; L30 cap 1; L40 cap 1 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 73 | Chuột Lôi Điện | 4 Điện | 2 | `[0, 0, 1, 2, 3]` | `40` Điện giật | L10<=tier0: `41` Lôi thiểm, `42` Nạp điện; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 74 | Dị Tiểu Hiệp | 4 Điện | 0 | `[0, 0, 0, 1, 1]` | `40` Điện giật | L10<=tier0: `41` Lôi thiểm, `42` Nạp điện; L20 cap 0; L30 cap 1; L40 cap 1 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 75 | Lôi Kỳ Lân | 4 Điện | 3 | `[0, 1, 2, 3, 3]` | `40` Điện giật | L10<=tier1: `41` Lôi thiểm, `42` Nạp điện, `43` Sóng điện từ, `44` Đoạt mệnh cao áp, `45` Điện năng chuyển đổi; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 76 | Độc Ba Lợi | 5 Ảnh | 2 | `[0, 0, 1, 2, 3]` | `50` Ảnh thứ | L10<=tier0: `51` Chú oán, `52` Quỷ độc; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 77 | Kịch Độc Quái | 5 Ảnh | 2 | `[0, 0, 1, 2, 3]` | `50` Ảnh thứ | L10<=tier0: `51` Chú oán, `52` Quỷ độc; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 78 | Kịch Độc Khủng Thú | 5 Ảnh | 2 | `[0, 0, 1, 2, 3]` | `50` Ảnh thứ | L10<=tier0: `51` Chú oán, `52` Quỷ độc; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 79 | Đan nhãn thú | 5 Ảnh | 2 | `[0, 0, 1, 2, 3]` | `50` Ảnh thứ | L10<=tier0: `51` Chú oán, `52` Quỷ độc; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 80 | Độc nhãn quái | 5 Ảnh | 2 | `[0, 0, 1, 2, 3]` | `50` Ảnh thứ | L10<=tier0: `51` Chú oán, `52` Quỷ độc; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 81 | Độc nhãn cự thần | 5 Ảnh | 2 | `[0, 0, 1, 2, 3]` | `50` Ảnh thứ | L10<=tier0: `51` Chú oán, `52` Quỷ độc; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 82 | Túi quỷ | 5 Ảnh | 0 | `[0, 0, 0, 1, 1]` | `50` Ảnh thứ | L10<=tier0: `51` Chú oán, `52` Quỷ độc; L20 cap 0; L30 cap 1; L40 cap 1 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 83 | Thi Đại Quỷ | 5 Ảnh | 1 | `[0, 0, 1, 1, 2]` | `50` Ảnh thứ | L10<=tier0: `51` Chú oán, `52` Quỷ độc; L20 cap 1; L30 cap 1; L40 cap 2 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 84 | Kính ma | 5 Ảnh | 3 | `[0, 1, 2, 3, 3]` | `50` Ảnh thứ | L10<=tier1: `51` Chú oán, `52` Quỷ độc, `53` Cơn ác mộng, `54` Mị ảnh, `55` Hỗn loạn; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 85 | Phá kính tà linh | 5 Ảnh | 3 | `[0, 1, 2, 3, 3]` | `50` Ảnh thứ | L10<=tier1: `51` Chú oán, `52` Quỷ độc, `53` Cơn ác mộng, `54` Mị ảnh, `55` Hỗn loạn; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 86 | Hư không hành giả | 5 Ảnh | 1 | `[0, 0, 1, 1, 2]` | `50` Ảnh thứ | L10<=tier0: `51` Chú oán, `52` Quỷ độc; L20 cap 1; L30 cap 1; L40 cap 2 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 87 | Minh vương Long | 5 Ảnh | 3 | `[0, 1, 2, 3, 3]` | `50` Ảnh thứ | L10<=tier1: `51` Chú oán, `52` Quỷ độc, `53` Cơn ác mộng, `54` Mị ảnh, `55` Hỗn loạn; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 88 | Lắc lắc | 6 Phong | 0 | `[0, 0, 0, 1, 1]` | `60` Phong nhận | L10<=tier0: `61` Phong áp, `62` Thuận phong; L20 cap 0; L30 cap 1; L40 cap 1 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 89 | Chim xanh | 6 Phong | 0 | `[0, 0, 0, 1, 1]` | `60` Phong nhận | L10<=tier0: `61` Phong áp, `62` Thuận phong; L20 cap 0; L30 cap 1; L40 cap 1 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 90 | Tai tước | 6 Phong | 2 | `[0, 0, 1, 2, 3]` | `60` Phong nhận | L10<=tier0: `61` Phong áp, `62` Thuận phong; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 91 | Hải âu | 6 Phong | 0 | `[0, 0, 0, 1, 1]` | `60` Phong nhận | L10<=tier0: `61` Phong áp, `62` Thuận phong; L20 cap 0; L30 cap 1; L40 cap 1 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 92 | Dực thần | 6 Phong | 1 | `[0, 0, 1, 1, 2]` | `60` Phong nhận | L10<=tier0: `61` Phong áp, `62` Thuận phong; L20 cap 1; L30 cap 1; L40 cap 2 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 93 | Hồng nhạn | 6 Phong | 1 | `[0, 0, 1, 1, 2]` | `60` Phong nhận | L10<=tier0: `61` Phong áp, `62` Thuận phong; L20 cap 1; L30 cap 1; L40 cap 2 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 94 | Phi Dực Thú | 6 Phong | 3 | `[0, 1, 2, 3, 3]` | `60` Phong nhận | L10<=tier1: `61` Phong áp, `62` Thuận phong, `63` Long quyển, `64` Nghịch Phong Đoạt, `65` Vũ Liệt Thuật; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 95 | Đậu ưng | 6 Phong | 0 | `[0, 0, 0, 1, 1]` | `60` Phong nhận | L10<=tier0: `61` Phong áp, `62` Thuận phong; L20 cap 0; L30 cap 1; L40 cap 1 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 96 | Liệt ưng | 6 Phong | 2 | `[0, 0, 1, 2, 3]` | `60` Phong nhận | L10<=tier0: `61` Phong áp, `62` Thuận phong; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 97 | Quáng thạch dực long | 6 Phong | 3 | `[0, 1, 2, 3, 3]` | `60` Phong nhận | L10<=tier1: `61` Phong áp, `62` Thuận phong, `63` Long quyển, `64` Nghịch Phong Đoạt, `65` Vũ Liệt Thuật; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 98 | Tà ma dực long | 6 Phong | 2 | `[0, 0, 1, 2, 3]` | `60` Phong nhận | L10<=tier0: `61` Phong áp, `62` Thuận phong; L20 cap 1; L30 cap 2; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |
| 99 | Tinh Vân Hạc | 6 Phong | 3 | `[0, 1, 2, 3, 3]` | `60` Phong nhận | L10<=tier1: `61` Phong áp, `62` Thuận phong, `63` Long quyển, `64` Nghịch Phong Đoạt, `65` Vũ Liệt Thuật; L20 cap 2; L30 cap 3; L40 cap 3 | P23 candidates = this pool minus known skills; no replace at 5 slots. |

## Rebuild Mapping

| Rebuild item | Status | Note |
| --- | --- | --- |
| `BattleUnit.sourceLearnCandidateSkillIds()` | PORTED | Uses species element, learnGroup, `aq.c[8]`, skill learnTier, excludes known skills. |
| `BattleUnit.sourceCanLearnAfterLevelUp()` | PORTED | Gates `skillCount < 5` and `skillCount < level / 10 + 1`. |
| `BattleUnit.learnSourceSkill()` | PORTED | Adds into empty slot; no source replace branch. |
| P23 `choiceskill.ui` after P22 | PORTED/PARTIAL | Opens only when candidate array exists; full widget VM still not pixel-perfect. |
| Full-slot replacement | SOURCE-NOOP | Source does not replace skills at 5 slots. |

## Verification

Generated from `VqsvBattleTables` backed by `modules/script/original/db.mid`.

Counts:

- Species rows: `100`
- Valid battle species rows: `100`
- Skill rows: `70`
- LearnGroup rows: `4`

Related smoke checkpoints already passing from prior slice:

- `battle_exp_levelup_choiceskill_ui`
- `battle_exp_levelup_learn_skill_done`
- `battle_exp_levelup_full_skill_slots_no_replace`

## Next Step

If we want to make this testable from the UI, add a Battle Lab / panel debug screen that selects a species + level and shows the exact P23 candidate list computed by `sourceLearnCandidateSkillIds()`. That would be a helper view only; core source parity is already defined here.
