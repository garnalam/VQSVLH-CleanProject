# New Chat Handoff, Training, And Audit Pack

Purpose: document này dùng để mở một đoạn chat mới và giúp nó tiếp nhận đúng cách dự án rebuild VQSV/Liệt Hỏa. Đoạn chat mới phải đọc tài liệu này trước, sau đó đọc các file được chỉ định, tự kiểm tra bằng bộ câu hỏi, rồi mới sửa code.

## 1. Prompt Khởi Động Cho Chat Mới

Copy đoạn này vào đầu chat mới:

```text
Bạn đang tiếp quản dự án rebuild VQSV/Liệt Hỏa bằng Java tại:
- Root chung: <ResourcesVQSV>
- Project: <ResourcesVQSV>\rebuild_game
- Assets/modules/source: <ResourcesVQSV>\modules
- Plan/docs: <ResourcesVQSV>\rebuild_plan

Nguyên tắc quan trọng nhất:
1. Không đoán bừa. Cái nào có source/event/assets chứng minh thì ghi PORTED. Cái nào chỉ gần đúng thì ghi APPROX. Cái nào là placeholder thì ghi STUB. Cái nào chưa làm thì ghi MISSING.
2. Không tự chế logic, hình ảnh, timing, effect, actor, map. Mỗi thay đổi phải chỉ ra nó bám vào file nào, opcode nào, record nào, hoặc source class nào.
3. Không đụng lại các đoạn đã được user xác nhận ổn nếu task không yêu cầu, đặc biệt early scene_0, Bảo Châu, room1 destruction, panel/logo/menu.
4. Khi sửa xong phải build/test/smoke, và báo thật phần nào còn APPROX/STUB.
5. Manual scripted porting đang được chấp nhận. Chưa bắt buộc event runner tổng quát nếu chưa handle đủ opcode.

Trước khi làm task mới, hãy đọc:
- <ResourcesVQSV>\rebuild_plan\23_new_chat_handoff_training.md
- <ResourcesVQSV>\rebuild_plan\22_scene1_room3_group0_manual_script_audit.md
- <ResourcesVQSV>\rebuild_game\src\main\java\VqsvIntroDemo.java

Sau đó tự trả lời quiz trong handoff, so với đáp án, rồi mới bắt đầu task.
```

## 2. Dự Án Đang Ở Đâu

Mục tiêu dài hạn: tạo một project Java có thể build lại một bản `.jar` tái dựng game VQSV/Liệt Hỏa càng sát bản gốc càng tốt, đi từ core/runtime/resource/renderer đến world/event/gameplay.

Hướng đi hiện tại: chưa dựng full engine tổng quát ngay. Đang rebuild theo luồng game thật, manual script từng đoạn, vừa làm vừa audit logic gốc. Khi đủ opcode và behavior thì mới tách dần thành event runner.

Project chính:

- `<ResourcesVQSV>\rebuild_game`
- Main demo/cutscene manual runner: `<ResourcesVQSV>\rebuild_game\src\main\java\VqsvIntroDemo.java`
- Build jar: `<ResourcesVQSV>\rebuild_game\build\libs\vqsv-rebuild-skeleton.jar`

Nguồn gốc dữ liệu:

- Decoded event JSON: `<ResourcesVQSV>\modules\event\decoded`
- Decompiled source CFR: `<ResourcesVQSV>\modules\source_code\decoded\decompiled_source_cfr`
- Original assets: `<ResourcesVQSV>\modules`
- Important source classes: `game.c`, `game.k`, `game.a`, `game.f`, `game.j`, `game.d`, `game.b`, `game.g`, `ae`, `s`.

## 3. Nguyên Tắc Làm Việc Của Chúng Ta

Không trả lời fake. Nếu chưa chắc thì nói chưa chắc, rồi chỉ ra cần đọc file nào để chắc.

Luôn phân biệt:

- `PORTED`: đã bám được source/event/assets, giữ đúng main args/flow.
- `APPROX`: đã có hành vi tương đương hoặc gần đúng, nhưng chưa mô phỏng engine gốc đầy đủ.
- `STUB`: cố tình thay bằng placeholder để flow chạy tiếp.
- `MISSING`: chưa implement.

Khi user bảo “bản gốc như nào thì phải như thế”, nghĩa là:

- Không tự đổi timing cho đẹp.
- Không lấy ảnh/effect khác thay nếu chưa có mapping.
- Không sửa những scene user đã xác nhận đúng.
- Không bỏ sót record/event vì thấy nó nhỏ.
- Không ghép map/actor bằng cảm tính.

Khi làm code:

- Dùng `rg` để tìm.
- Dùng `apply_patch` để sửa file.
- Build bằng `powershell -ExecutionPolicy Bypass -File .\build.ps1`.
- Smoke bằng `java -Dvqsv.modules=... -cp build\classes VqsvIntroDemo --smoke <png> <ticks>`.
- Check skeleton bằng `java -Dvqsv.modules=... -jar build\libs\vqsv-rebuild-skeleton.jar --check`.

## 4. Tiến Độ Theo Giai Đoạn

### Giai Đoạn A: Audit Core/Runtime

Đã audit khá rộng:

- Runtime/core: state machine `game.i`, `game.k`, timer/repaint, state transitions.
- Resource: path locator, binary reader, image loader, font, sprite, map.
- UI: dialog/menu/shop/task ở mức audit; chưa port full UI engine.
- Battle: đã audit state/formula/status nhiều, nhưng trong demo hiện tại vẫn dùng stub.
- SMS/payment: đã audit side effect mức đủ biết không chặn rebuild core; chưa port full behavior.

Điểm còn phải giữ trong đầu:

- Không phải tất cả audit đều đã thành code.
- Một số phần từng được ghi PARTIAL/UNVERIFIED vì CFR damage hoặc chưa pixel-compare.
- Tài liệu hiện tại trong `rebuild_plan` không còn đầy đủ các file audit cũ; file quan trọng còn lại là audit group0 mới nhất.

### Giai Đoạn B: Skeleton Project

Đã có skeleton tại `rebuild_game`.

Đã có:

- Resource locator + binary readers.
- Image loader/cache.
- Bitmap font parser/renderer nền.
- Sprite renderer đọc `spr_*_all(r)` ở mức đủ render nhiều actor/effect.
- Map renderer đọc map/mod/layer, camera 240x320.
- Boot/menu flow: logo, CWA logo, music prompt, title menu.
- Click UI cơ bản: click tương đương confirm/0 ở demo.

### Giai Đoạn C: Intro / Scene 0

Đoạn legacy scene_0 đã được user xác nhận tốt ở nhiều phần:

- Text “Nghe đồn...”
- Prompt ấn 0.
- Bạch Long/Hắc Long.
- Bảo Châu sau khi đã sửa lại.
- Room “Phá hủy thôn trang” đã được chỉnh đến mức user nói ổn.

Không tự ý sửa lại các phần này nếu task mới không yêu cầu.

### Giai Đoạn D: Scene_7 Room2 Event0

Đã port thử một phần scene_7 room2 event0 trước khi chuyển sang scene_1. Có smoke. Không phải trọng tâm hiện tại.

### Giai Đoạn E: Scene_1 Room3 Group0

Đây là trọng tâm hiện tại.

Scope:

- File event: `modules\event\decoded\data__event__scene_1.mid.json`
- `room_index=3`
- `event_group[0]`
- Records `0..103`

Audit mới nhất:

- `<ResourcesVQSV>\rebuild_plan\22_scene1_room3_group0_manual_script_audit.md`
- Totals hiện tại: `PORTED 40`, `APPROX 59`, `STUB 5`, `MISSING 0`

Nội dung đã có trong manual script:

- “Sáu năm sau ...”
- Neil/Sophie dialog.
- Actor show/hide.
- Actor movement/action/effect gần đúng.
- Battle trigger visible stub.
- Special textbox opcode 48 đã sửa `x=20,y=220,w=200,h=40`.
- Record 90 opcode 34 đã port đúng source behavior gần như instant complete.
- Record 101..102 đã load sang `scene_1 room0`, map id `2`, camera `199,218`.

Điểm còn APPROX/STUB:

- `opcode 10` movement chưa mô phỏng đầy đủ `actor.h()`, collision/path/animation finished.
- `opcode 9` effect chưa pixel-perfect vì chưa port full `b.a()` effect engine.
- `opcode 5` actor effect còn approximate offset/layer/lifetime.
- Battle vẫn là `ScriptedBattleStub`, chưa phải `game.d`.
- World transition room0 chưa phải full `game.k` state 22/free movement loader.
- Một số actor room0 có thể blank vì sprite mapping/resource chưa đủ. Không được tự đoán ảnh.

## 5. Kiến Thức Bắt Buộc

### Event Opcode Trong Đoạn Hiện Tại

Các opcode đã gặp trong scene_1 room3 group0:

- `1`: full text/cutscene text.
- `2`: show/set active actors.
- `3`: hide actors.
- `4`: dialog UI có speaker name.
- `5`: actor-attached effect, dùng sprite/effect 259 với animation id.
- `6`: world transition target, set scene/room rồi chuyển state 22.
- `7`: actor action/animation mode/dir, chờ animation xong.
- `9`: screen/global effect qua `b.a()`.
- `10`: scripted actor movement theo dir/speed/duration.
- `11`: camera pan/snap.
- `12`: delay.
- `14`: end event.
- `15`: gate/start condition.
- `22`: prepare world transition/camera.
- `32`, `37`, `47`, `52`, `67`: battle trigger cluster; hiện đang STUB.
- `34`: counter/timed value. Record 90 args `[1,0,70,0,0]` set `N=70,O=0,B=0`, nên hoàn tất ngay/near-immediate.
- `48`: special text box. Source dùng `b[1],b[2]` làm vị trí, `b[3],b[4]` làm size.

### Source Facts Quan Trọng

`game.c case 48`:

- `this.D.a(b[1], b[2])`
- `this.D.a((byte)(b[0] / 10 - 1), text, b[0] % 10)`
- nếu `b[5] == 1` thì `this.D.a(true)`
- `this.D.b(b[3], b[4])`
- Với record 91: `[10,20,220,200,40,1]`, textbox phải đặt tại `x=20,y=220,w=200,h=40`.

`game.c case 34`:

- Initial state: `N=b[2]`, `O=b[3]`, `B=b[4]`, set state 5.
- Tick: `--B; N -= O; if B > 0 wait; else complete`.
- Với record 90: `N=70,O=0,B=0`, không có visual effect chắc chắn. Không được bịa effect.

`game.c case 22`:

- `game.k.x = true`
- `game.k.w = b[1]`
- camera/target: `h=b[2], i=b[3]`
- viewport/effect dims: `B=b[4], C=b[5]`
- record 101: `[1,2,199,218,240,320]`

`game.c case 6`:

- set complete current event.
- `game.k.a().f = b[0]`
- `game.k.a().g = b[1]`
- optional event actor id via `b[2],b[3]`
- `game.i.a().a((byte)22)`
- record 102: `[1,0,0,0]`

## 6. Cách Build/Test

Từ project root:

```powershell
cd <ResourcesVQSV>\rebuild_game
powershell -ExecutionPolicy Bypass -File .\build.ps1
```

Check skeleton:

```powershell
java "-Dvqsv.modules=..\modules" -jar ".\build\libs\vqsv-rebuild-skeleton.jar" --check
```

Run interactive:

```powershell
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

Smoke examples:

```powershell
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke ".\build_intro_demo\scene1_op48_4550.png" 4550

java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke ".\build_intro_demo\scene1_room0_after_6200.png" 6200
```

Expected current behavior:

- `scene1_op48_4550.png`: black background, special text box around `x=20,y=220,w=200,h=40`.
- `scene1_room0_after_6200.png`: visible `Thủy Mộc Thôn` map around camera center `199,218`; some actors may render, some may be blank until sprite mapping is filled.

## 7. Bài Kiểm Tra Hiểu Dự Án

Đoạn chat mới phải tự trả lời trước khi làm việc. Sau đó so với đáp án ở mục 8.

1. Vì sao không được hứa pixel-perfect cho `game.j` text/cutscene renderer?
2. Vì sao record 90 opcode 34 không nên tự thêm visual effect?
3. Record 91 opcode 48 có tọa độ/size đúng là gì, và lấy từ source nào?
4. Sau record 101..102, game chuyển đến scene/room/map nào?
5. Vì sao battle hiện tại là STUB?
6. `opcode 10` movement hiện còn APPROX ở điểm nào?
7. Khi một sprite room0 thiếu mapping/resource, cách xử lý đúng hiện tại là gì?
8. Khi user báo một đoạn đã đúng, nguyên tắc sửa tiếp là gì?
9. Lệnh build và lệnh check hiện tại là gì?
10. Nếu muốn port tiếp scene_1 room0, phải đọc file nào trước?

## 8. Đáp Án Chuẩn

1. Vì `game.j.a(Graphics)` và `game.j.a(Graphics,int,int)` từng bị CFR damaged/partial; renderer skeleton đã có typewriter/wrap/prompt cơ bản nhưng chưa pixel-compare đầy đủ font baseline/layout/mode.
2. Vì source `case 34` chỉ set `N/O/B` rồi trừ counter; record `[1,0,70,0,0]` có `B=0`, nên hoàn tất gần như ngay. Không có bằng chứng effect hình ảnh.
3. `x=20,y=220,w=200,h=40`; lấy từ `game.c case 48`: `D.a(b[1],b[2])` và `D.b(b[3],b[4])`.
4. `scene_1 room0`, room name `Thủy Mộc Thôn`, map id/`unknown_ab=2`, camera center `199,218`.
5. Vì full battle engine `game.d/game.b/game.g` chưa được port vào demo; hiện chỉ cần flow cutscene chạy tiếp nên dùng `ScriptedBattleStub` có hiển thị rõ.
6. Nó mới đi theo dir/speed/duration và start tick gần source hơn, nhưng chưa mô phỏng đầy đủ `actor.h()`, collision/path completion, animation finished, và state persistence của actor engine gốc.
7. Render blank hoặc skip an toàn, ghi rõ chưa đủ mapping. Không lấy ảnh khác để thay.
8. Không đụng lại nếu task không yêu cầu. Nếu bắt buộc đụng, phải có source evidence và smoke lại đoạn đó.
9. Build: `powershell -ExecutionPolicy Bypass -File .\build.ps1`. Check from `<ResourcesVQSV>\rebuild_game`: `java "-Dvqsv.modules=..\modules" -jar ".\build\libs\vqsv-rebuild-skeleton.jar" --check`.
10. Đọc `modules\event\decoded\data__event__scene_1.mid.json`, room_index 0; đọc `game.c` cho opcode; đọc `game.k/game.a/game.f` nếu liên quan world/actor.

## 9. Rubric Đánh Giá Chat Mới

Chấm theo 100 điểm.

- 20 điểm: đọc đúng file, dẫn chứng đúng path/source/record/opcode.
- 20 điểm: không bịa; phân loại đúng PORTED/APPROX/STUB/MISSING.
- 15 điểm: không phá đoạn đã user xác nhận đúng.
- 15 điểm: build/test/smoke đầy đủ, báo kết quả thật.
- 10 điểm: cập nhật tài liệu audit khi thay đổi behavior.
- 10 điểm: code nhỏ, scoped, theo pattern hiện có.
- 10 điểm: giao tiếp rõ ràng, biết nói “chưa chắc” đúng lúc.

Fail ngay nếu:

- Tự chế hình/effect/timing rồi nói giống bản gốc.
- Sửa scene_0/early intro không được yêu cầu.
- Không build/test sau khi sửa code.
- Che giấu `APPROX/STUB`.
- Dùng asset không tồn tại hoặc bịa mapping.

## 10. Task Nhỏ Để Thử Chat Mới

Task đề xuất: “Audit sprite mapping cho `scene_1 room0` actors đang blank, không sửa visual nếu chưa chắc.”

Yêu cầu cụ thể:

1. Đọc `loadScene1Room0()` trong `VqsvIntroDemo.java`.
2. Liệt kê sprite ids của room0 actor table.
3. Đối chiếu với `SpriteAnim.SPRITE_TO_IMGS` hiện có.
4. Đối chiếu file thật trong:
   - `<ResourcesVQSV>\modules\spr\original`
   - `<ResourcesVQSV>\modules\img\decoded`
   - `<ResourcesVQSV>\rebuild_game\src\main\resources`
5. Tạo một markdown nhỏ, ví dụ `24_scene1_room0_sprite_mapping_audit.md`, gồm:
   - sprite id
   - có spr file không
   - có mapping trong code không
   - image ids cần thiết nếu biết chắc
   - trạng thái `READY / NEED_MAPPING / NEED_RESOURCE / UNKNOWN`
6. Không thêm mapping đoán bừa.
7. Build/check không bắt buộc nếu chỉ viết audit, nhưng nếu sửa code thì bắt buộc build/check.

Kết quả tốt của task này:

- Chat mới chỉ audit, không tự vẽ actor.
- Có bảng rõ ràng.
- Có bằng chứng path/file.
- Có danh sách việc tiếp theo an toàn.

Kết quả kém:

- Tự thêm `SPRITE_TO_IMGS` bằng số đoán.
- Copy ảnh không rõ nguồn.
- Nói room0 pixel-perfect khi chưa render đủ actor.
- Không cập nhật trạng thái thiếu/chưa chắc.

## 11. Việc Nên Làm Tiếp Sau Task Nhỏ

Nếu task nhỏ đạt chất lượng, bước tiếp hợp lý:

1. Fill sprite mappings/resources cho room0 theo bằng chứng thật.
2. Port phần entry/free movement/event đầu của `scene_1 room0`.
3. Dần thay manual transition bằng `game.k` world state loader thật hơn.
4. Sau đó mới quay lại giảm APPROX cho `opcode 10` actor movement.
5. Sau nữa mới đụng `opcode 9` effect engine và battle thật.

## 12. Cảnh Báo Cho Người Tiếp Quản

Đây không phải project “làm cho nhìn hay”. Đây là project “đọc bản gốc rồi dựng lại”. Nếu không biết, hãy audit. Nếu audit chưa ra, hãy ghi `UNKNOWN`. Nếu chỉ làm gần đúng để flow chạy, hãy ghi `APPROX` hoặc `STUB`.

Điều user kỳ vọng nhất không phải tốc độ, mà là sự trung thực và bám sát bản gốc.
