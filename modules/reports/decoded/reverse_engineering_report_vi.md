# Báo Cáo Giải Mã JAR Game

## Kết Luận Nhanh
- Đây là game Java ME MIDP 2.0/CLDC 1.0; entrypoint là `game.GameMIDLet`.
- Các class đã bị obfuscate tên ngắn (`a`, `b`, `game.k`, hàm `a()`, `b()`...), nhưng decompile được đầy đủ bằng CFR và bytecode `javap` đã lưu kèm.
- Chỉ `data/sound/0.mid` là MIDI âm thanh thật. Hầu hết `.mid` còn lại là dữ liệu nhị phân riêng của game: map, scene, script table, ảnh PNG trá hình, hoặc container PNG.
- `data/event/scene_*.mid` điều khiển scene/room: string pool, tên room, actor records, event groups/opcodes. Code gọi trực tiếp nằm ở `game.k`, phần thực thi điều kiện/sự kiện nằm nhiều ở `game.c`, actor/world state ở `game.g`/`game.a`.
- `data/ui/*.ui` là layout nhị phân, được parser `ao` dựng thành container/control/style. `data/event/scene_13.mib` mới là bytecode VM của package `a.*`.

## Deliverables
- `reverse_engineering_report_vi.md`: báo cáo này.
- `file_inventory.csv`: inventory 814 file, SHA-256, magic bytes, loại file.
- `decoded_assets/png/`: 268 ảnh PNG giải mã, tất cả đã verify mở được.
- `decoded_assets/json/`: 170 JSON đã decode: map, scene, script, UI, VM bytecode.
- `scene_room_overview.csv`: danh sách scene/room, `world_key`, actor count, event group count.
- `resource_call_graph.md`: file resource nào được class/line nào gọi.
- `decompiled_source_cfr/`: source Java decompiled cho 68 class.
- `bytecode_javap/`: disassembly verbose cho 68 class để đối chiếu khi CFR chưa rõ.

## Thống Kê File
- `event_scene_binary`: 12
- `java_class`: 68
- `map_binary`: 94
- `midi_audio`: 1
- `module_tileset_binary`: 8
- `png_chunk_container_or_raw_png`: 22
- `png_image`: 246
- `script_table_binary`: 17
- `sprite_binary`: 299
- `ui_layout_binary`: 40
- `unknown_or_misc`: 6
- `vm_bytecode_script`: 1

## Luồng Chạy Chính
1. `game.GameMIDLet` tạo `game.e` và set làm Canvas hiện tại.
2. `game.e` bật fullscreen, khởi tạo `game.i`, rồi start thread chính.
3. Vòng lặp `game.e.run()` gọi `game.i.b()` để update logic, `repaint/serviceRepaints`, sau đó sleep theo `an.B()`. Đây là nhịp nhanh/chậm frame của game.
4. `game.i` quản lý trạng thái màn hình, logo/audio/loading, rồi chuyển sang world/battle/menu.
5. `game.k` là world/scene controller: giữ `f=scene_id`, `g=room_index`, load `/data/event/scene_<f>.mid`, chọn block room `g`, tạo actor, nạp event groups, map và asset liên quan.
6. `j` nạp `/data/map/map_<id>.mid`; map chỉ chứa tile/object entries, còn tileset rectangle lấy từ `/data/mod/mod_<id>.mid` và danh sách ảnh tileset từ `/data/mod/modInfo.mid`.

## Vai Trò Class Quan Trọng
- `game.GameMIDLet`: MIDlet entrypoint.
- `game.e`: Canvas + main loop + key/touch dispatch; điều tiết frame bằng `an.B()`.
- `game.i`: Game state manager, logo/loading/audio; chỉ load MIDI thật ở `/data/sound/0.mid`.
- `game.k`: World/scene controller, scene loader, map transition, actor creation, RMS save/load.
- `game.c`: Event/quest trigger engine; đọc event group `p/ad`, kiểm tra opcode điều kiện và chạy phản ứng.
- `game.d`: Battle scene/controller; nạp script combat như `pos`, `effect`, `speffect`, `blood`, `bufDebuf`.
- `game.g`: Player/pet/world entity state lớn; được event và battle logic thao tác.
- `j`: Map renderer/loader cho `data/map` và `data/mod`.
- `aq`: Global data table loader: sprite, modInfo, chs, npcDialog, db, tex.
- `ae`: Utility giải mã resource: bảng short/byte/string, PNG container, image loader, text helpers.
- `aj`: Resource stream wrapper `getResourceAsStream`.
- `ao/ab/al/af/ac/z`: UI layout manager/parser và control tree cho `.ui`.
- `a.*`: VM bytecode runtime dùng bởi `scene_13.mib` SMS/payment script.

## Định Dạng Resource Đã Giải
- `byte_matrix`: 1 JSON
- `event_scene`: 12 JSON
- `map`: 94 JSON
- `mod_info`: 1 JSON
- `mod_tile_rects`: 7 JSON
- `nine_short_matrices`: 1 JSON
- `short_matrix`: 6 JSON
- `string_matrix`: 4 JSON
- `three_byte_matrices`: 1 JSON
- `three_short_matrices`: 1 JSON
- `two_byte_matrices`: 1 JSON
- `ui_layout`: 40 JSON
- `vm_bytecode_tree`: 1 JSON

- `string_matrix`: short count -> rows -> string length -> UTF-16BE chars, có repair mojibake tiếng Việt.
- `short_matrix` / `byte_matrix`: cấu trúc count/row length/values theo `ae.a` và `ae.b`.
- `event_scene`: block table theo room, string pool, room name, actors, post actor strings, event groups.
- `ui_layout`: root container, navigation map, styles, visual controls, list/grid controls theo parser `ao`.
- `vm_bytecode_tree`: function tree/constant/instruction sample cho VM `a.*`.

## Scene Và Room
- `scene_0.mid`: 2 room. Ví dụ: Bản đồ trống, Phá hủy thôn trang
- `scene_1.mid`: 7 room. Ví dụ: Thủy Mộc Thôn, Bắt được Thủy Mộc Thôn, Theo chỉ dẫn địa đồ tiếp theo là Thủy Mộc Thôn, Thủy Mộc Thôn hạ 2, Đi thông bến tàu Thủy Mộc Thôn
- `scene_2.mid`: 8 room. Ví dụ: Bích Thủy Thành Thượng, Bích Thủy Thành, Một khu Bích Thủy Thành Hạ, Bích Thủy Thành Hữu Nhất, Một khu Bích Thủy Thành Hữu Thượng
- `scene_3.mid`: 8 room. Ví dụ: Đi Vãng Sơn Động, Thụ Thung Mê Cung, Nguyên Mộc Thành Thượng, Nguyên Mộc Thành Nhất, Nguyên Mộc Thành Tả Nhất
- `scene_4.mid`: 13 room. Ví dụ: Niêm Thổ Mã Đầu, Xuy Phong Phế Khư, Niêm Thổ Sơn Động 1, Niêm Thổ Sơn Động 2, Niêm Thổ Sơn Động 3
- `scene_5.mid`: 7 room. Ví dụ: Hắc Thạch đi qua Sơn Động, Vào cửa thành Hắc Thạch, Hắc Thạch Thành Mã Đầu, Hắc Thạch Thành Nhất, Vào cửa Địa lao
- `scene_6.mid`: 2 room. Ví dụ: Hắc Long Thần Điện Mã Đầu, Hắc Long Thần Điện
- `scene_7.mid`: 13 room. Ví dụ: Bạch Long Thần Điện, Đi qua Bạch Long Thành, Bạch Long Thành, Bạch Long Thành Tả Nhất, Bạch Long Thành Tả đi qua Lôi Kỳ Lân
- `scene_9.mid`: 8 room. Ví dụ: Bích Thủy Đạo Quán, Nguyên Mộc Đạo Quán, Niêm Thổ Đạo Quán Thượng, Niêm Thổ Đạo Quán Hạ, Hắc Thạch Đạo Quán Thượng
- `scene_10.mid`: 15 room. Ví dụ: Tương Quân Giải, Lôi Kỳ Lân 1, Lôi Kỳ Lân 2, Lôi Kỳ Lân 3, Lôi Kỳ Lân 4
- `scene_11.mid`: 29 room. Ví dụ: Ngục giam, Đồ Thư Quán, Khí cầu, Bạch Long Thành, Nhân vật chính
- `scene_12.mid`: 1 room. Ví dụ: Gian phòng 0

Mỗi room có `world_key = scene_id << 8 | room_index`; đây là key mà `game.c`/`p` giữ để biết event thuộc scene-room nào. Chi tiết từng room nằm trong `scene_room_overview.csv` và JSON `decoded_assets/json/data__event__scene_*.mid.json`.

## Top Resource Được Code Gọi
- `/data/ui/msgwarm.ui`: 83 refs
- `/data/ui/petstate.ui`: 19 refs
- `/data/ui/petsetting.ui`: 14 refs
- `/data/ui/shop.ui`: 13 refs
- `/data/ui/choice.ui`: 13 refs
- `/data/ui/dialog.ui`: 10 refs
- `/data/ui/gamemenu.ui`: 10 refs
- `/data/ui/msgyn.ui`: 10 refs
- `/data/ui/wharf2.ui`: 10 refs
- `/data/ui/openbox.ui`: 9 refs
- `/data/tex/`: 8 refs
- `/data/ui/msgtip.ui`: 8 refs
- `/data/ui/world.ui`: 8 refs
- `/data/ui/gamesystem.ui`: 8 refs
- `/data/ui/choiceskill.ui`: 8 refs
- `/data/ui/levelUp.ui`: 8 refs
- `/data/ui/msgconfirm.ui`: 7 refs
- `/data/ui/shopbuy.ui`: 7 refs
- `/data/ui/bag.ui`: 7 refs
- `/data/ui/taskTip.ui`: 6 refs

## Cách Scene Truy Asset
- Scene file không nhúng ảnh trực tiếp; actor records chứa sprite/id/type/position/state. Khi actor được dựng, code đi qua `game.a`, `game.g`, `am`, `aq.a`, và `data/spr/spr_<id>_all(r)` để lấy animation/sprite metadata.
- Map room được chọn bởi `game.k.f`/`game.k.g`; `j.a(mapId)` load `data/map/map_<id>.mid`, trong map có `mod_id`; `j.d()` dùng `data/mod/mod_<mod_id>.mid` và `aq.b[mod_id]` từ `modInfo.mid` để biết ảnh `data/img/img_<id>.mid` nào cần render.
- UI được gọi bằng `ab/ao` từ `game.h`, `game.k`, `game.f`, ví dụ `msgwarm.ui`, `dialog.ui`, `battle.ui`, `bag.ui`; call sites cụ thể nằm trong `resource_call_graph.md`.
- Script tables như `bTask`, `mTask`, `npcDialog`, `chs`, `db` cung cấp text/quest/NPC database; event opcode trong `game.c` trỏ tới các bảng này qua string pool hoặc id.

## Giới Hạn Còn Lại
- Tên biến/hàm gốc không thể phục hồi 100% vì obfuscation đã xóa metadata. Báo cáo dùng vai trò suy ra từ bytecode, loader, và call graph.
- Opcode event đã được tách thành `opcode`, `short_args`, `string_args`; đặt tên ngữ nghĩa cho mọi opcode cần thêm một vòng annotation thủ công dựa trên switch lớn trong `game.c`.
- Sprite binary `data/spr/spr_*_all(r)` đã inventory đầy đủ; để render từng animation frame cần reverse thêm class sprite/entity (`f`, `am`, `aa`, `game.a`) hoặc chạy emulator/instrumentation.

Ảnh decoded: 268 PNG. JSON decoded: 170. Code-resource refs: 360.