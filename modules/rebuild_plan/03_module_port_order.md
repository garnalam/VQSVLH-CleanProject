# Module Port Order

Thứ tự port phải đi từ nền thấp lên gameplay cao, giống xây nhà từ móng.

## Phase 0 - Audit Source

- Đọc toàn bộ `source_code/decoded/decompiled_source_cfr`.
- Lập bảng class -> vai trò -> resource phụ thuộc.
- Đánh dấu class decompile lỗi hoặc có `null` giả do CFR.

## Phase 1 - Build Và Platform

1. Tạo project build ra JAR.
2. Thêm game loop 66ms.
3. Thêm input bitmask tương thích `ap.java`.
4. Thêm graphics abstraction có `drawRegion`.

Kết quả: app mở cửa sổ/canvas đen, nhận phím.

## Phase 2 - Resource Core

1. `BinaryReader` tương đương phần cần dùng của `ae.java`.
2. `ImageStore` tương đương `am.java`.
3. `ScriptTables` tương đương `aq.java`.
4. Parser `sprite.mid`, `modInfo.mid`, `backPic.mid`.

Kết quả: load được ảnh, sprite table, font.

## Phase 3 - Renderer Core

1. `FontBitmap` từ `s.java`.
2. `SpriteStore` từ `aa.java` + `o.java`.
3. `SpriteRenderer` từ `d.java`.
4. `MapRenderer` từ `j.java`.
5. `EffectRenderer` từ `b.java`.
6. `DisplayList` từ `t.java`.

Ghi chu audit moi:

- `EffectRenderer` phai tach rieng thanh compositor co main/secondary/overlay channels nhu `b.java`.
- Nen port truoc ids `0/1/2/9/10/17/19/20`, sau do moi den `6/7/8/11/12/13/14/15/18`.

Kết quả: vẽ đúng actor/map/text/effect độc lập.

## Phase 4 - Event VM

1. Parser event scene.
2. Port opcode trong `game.c` theo nhóm:
   - text/dialog,
   - actor visibility/state,
   - movement/path,
   - camera/effect,
   - branch/condition,
   - world transition.
3. Chạy scene 0 group 0 đầy đủ.

Kết quả: intro scene chạy bằng VM thay vì hardcode.

## Phase 5 - World State

1. Port room loader từ `game.k`.
2. Port actor init từ `game.a`.
3. Port camera/player/world update.
4. Port map transitions.

Kết quả: vào world room, đi lại cơ bản.

## Phase 6 - UI Framework

1. Parser `.ui`.
2. Port `ab`, `ao`, component tree.
3. Port widget runtime `w/al/af/ac` va render helpers `k/m/z`.
4. Port dialog, menu, choice, bag, task UI.

Ghi chu audit moi:

- UI `.ui` khong nen hardcode tung man; can dung parser va widget tree.
- `z.java` dynamic list binding can validate bang menu/bag/shop runtime vi CFR pha mot so doan thanh `null`.

Kết quả: menu/dialog hoạt động.

## Phase 7 - Gameplay Systems

1. Player/pet state từ `game.g`.
2. Task/shop/item/bag.
3. Battle state.
4. Save/RMS.
5. Sound.

Kết quả: game loop đầy đủ.

## Phase 8 - Verification

- So sánh screenshot từng scene/màn.
- So sánh event timing.
- So sánh input/menu flow.
- So sánh save/load.
