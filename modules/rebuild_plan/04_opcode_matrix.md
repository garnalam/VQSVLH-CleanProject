# Event Opcode Matrix

Update:

- File nay la matrix cu tap trung intro scene 0 group 0.
- Matrix World/Event day du hon nam o [10_world_event_opcode_matrix.md](10_world_event_opcode_matrix.md).

Nguồn chính: `source_code/decoded/decompiled_source_cfr/game/c.java`.

Mục tiêu: port `game.c` thành `EventVm` mới, không hardcode từng cutscene.

## Trạng Thái

Legend:

- VERIFIED: đã đối chiếu code + chạy demo.
- PARTIAL: hiểu hướng chính, cần audit thêm.
- UNVERIFIED: chưa đọc đủ.

## Opcode Đã Dùng Trong Intro Scene 0 Group 0

| Opcode | Ý nghĩa hiện biết | Trạng thái |
|---|---|---|
| `1` | Text full/cutscene, gọi `game.j`, bật wait key `0` | VERIFIED |
| `2` | Set actor state/animation + active actor `.c()` | VERIFIED sơ bộ |
| `3` | Hide/deactivate actor `.d()` | VERIFIED sơ bộ |
| `6` | Chuyển scene/map target | PARTIAL |
| `9` | Effect gateway vào `b.java` | PARTIAL |
| `11` | Camera/effect camera target qua `ai` | PARTIAL |
| `12` | Delay frame/tick | VERIFIED |
| `14` | End/mark event branch state | PARTIAL |
| `21` | Set next world transition params | PARTIAL |
| `29` | Move actor/player bằng delta mỗi tick | VERIFIED |
| `30` | Path absolute theo chuỗi X/Y từng tick | VERIFIED |
| `48` | Text box tại x/y/w/h, optional wait nếu arg[5] == 1 | VERIFIED |

## Opcode 9 Subtypes Hiện Biết

| Type | Ý nghĩa hiện biết | Trạng thái |
|---|---|---|
| `10` | Flash trắng/đen ngắn (`b.d(limit, mode)`) | VERIFIED sơ bộ |
| `12` | Letterbox/mask mở dạng ngược `13` | PARTIAL |
| `13` | Letterbox/mask đen trên/dưới | VERIFIED sơ bộ |
| `14` | Image fade out | PARTIAL |
| `15` | Image fade in | PARTIAL |
| `16` | Particle texture: star/fire | PARTIAL |
| `17` | Circle fill expand/shrink tại điểm | VERIFIED sơ bộ |
| khác | Fade/color/effect khác theo `b.java` | UNVERIFIED |

## Cần Audit Tiếp

- Tất cả `case` trong `game.c.n()`.
- Branch/condition opcodes: `13,15,16,38,41,42,47...`.
- Inventory/task/reward opcodes.
- UI choice/dialog opcodes.
- World transition opcodes `6,21,22,23`.
- Battle-trigger opcodes.
