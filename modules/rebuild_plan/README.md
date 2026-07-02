# VQSV Rebuild Plan

Mục tiêu của thư mục này là làm "móng" cho project rebuild: đọc hiểu code gốc trong
`source_code`, ghi lại kiến trúc thật, rồi dùng nó để dựng một project Java mới có thể
build ra JAR chạy giống bản gốc.

Nguyên tắc làm việc:

- Không viết lại game bằng cảm tính; mọi module mới phải có đối chiếu từ source/data gốc.
- Tách rõ ba lớp: resource format, runtime renderer, gameplay/event logic.
- Ưu tiên dựng khung xương build được trước, sau đó lắp từng hệ đã hiểu chắc.
- Mọi chỗ chưa chắc phải ghi là `UNVERIFIED`, không biến suy đoán thành sự thật.

Các tài liệu chính:

- [01_source_code_architecture.md](01_source_code_architecture.md): bản đồ kiến trúc source gốc.
- [02_runtime_skeleton.md](02_runtime_skeleton.md): khung project Java mới cần dựng.
- [03_module_port_order.md](03_module_port_order.md): thứ tự port module từ thấp lên cao.
- [04_opcode_matrix.md](04_opcode_matrix.md): ma trận opcode event/cutscene trong `game.c`.
- [05_resource_format_specs.md](05_resource_format_specs.md): spec tài nguyên `img/spr/map/mod/script/event/ui`.
- [06_runtime_core_notes.md](06_runtime_core_notes.md): thu hoạch Runtime/Core: lifecycle, input, state manager.
- [07_resource_renderer_notes.md](07_resource_renderer_notes.md): thu hoach Resource/Renderer core: loader, cache, sprite, map, font.
- [08_effect_ui_renderer_notes.md](08_effect_ui_renderer_notes.md): thu hoach effect manager `b.java` va UI `.ui` runtime.
- [09_world_event_notes.md](09_world_event_notes.md): thu hoach World/Event: room, actor, event VM, camera, opcode groups.
- [10_world_event_opcode_matrix.md](10_world_event_opcode_matrix.md): ma tran opcode World/Event `0..88` tu `game.c`.
- [11_world_event_opcode_deep_audit.md](11_world_event_opcode_deep_audit.md): audit sau opcode World/Event: branch/condition, inventory/task/reward, UI, transition, battle trigger, resource usage counts.
- [12_ui_system_notes.md](12_ui_system_notes.md): thu hoach UI runtime: `ab/ao/af/al/ac`, `.ui`, `game.h`, va `package a/*`.
- [13_gameplay_battle_save_notes.md](13_gameplay_battle_save_notes.md): thu hoach Gameplay/Battle/Save: `game.g`, `game.b`, `game.d`, `ar`, save slots trong `game.k`.
- [14_source_code_remaining_audit.md](14_source_code_remaining_audit.md): checklist nhung phan trong `source_code` con can audit tiep truoc rebuild full game.
- [15_renderer_primitive_deep_audit.md](15_renderer_primitive_deep_audit.md): audit sau renderer primitive: sprite frame/cell/animation, map layer, bitmap font, image/cache, DB bootstrap.
- [16_ui_workflow_matrix.md](16_ui_workflow_matrix.md): ma tran UI workflow: `.ui` file, widget id, input mask, side effect/state change trong `game.h`.
- [17_battle_state_machine.md](17_battle_state_machine.md): battle state machine `game.d.P`, UI battle flow, unit model `game.b`, damage/catch/EXP notes.
- [18_battle_formula_status_matrix.md](18_battle_formula_status_matrix.md): ma tran cong thuc battle, skill family, status/form, buff/debuff, element relation, catch formula.
- [19_world_tick_actor_matrix.md](19_world_tick_actor_matrix.md): world tick + actor behavior: `game.k.b()`, random encounter, actor `t/v`, camera `ai`, effect actor `ah`.
- [20_text_cutscene_renderer_matrix.md](20_text_cutscene_renderer_matrix.md): text/cutscene renderer `game.j`: opcode `1/48/51/84`, typewriter, wrap bitmap font, prompt, paging, intro layout.
- [21_sms_payment_side_effect_matrix.md](21_sms_payment_side_effect_matrix.md): SMS/payment side effects: `an`, `q/u/v`, VM `scene_13.mib`, reward/save matrix, `PK6_RMS_SMS/CNTSMS`.

Proof-of-concept hiện có:

- `build_intro_demo/`: demo Java SE dựng lại intro scene từ resource thật.
- `dist/vqsv_intro_scene_demo.jar`: JAR demo scene đã build.
