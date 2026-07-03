# VQSV Module Rebuild Plan

ThÆ° má»¥c nÃ y lÃ  bá»™ tÃ i liá»‡u ná»n mÃ³ng Ä‘Æ°á»£c viáº¿t trong giai Ä‘oáº¡n Ä‘á»c hiá»ƒu
`modules`: source gá»‘c, resource, renderer, runtime, UI, world/event, battle,
text/cutscene vÃ  SMS/payment.

Vai trÃ² cá»§a thÆ° má»¥c nÃ y:

- ghi láº¡i cÆ¡ cháº¿ váº­n hÃ nh cá»§a source gá»‘c;
- giáº£i thÃ­ch Ã½ nghÄ©a tá»«ng cá»¥m file/module;
- chá»‰ ra dá»¯ liá»‡u nÃ o tÃ¡c Ä‘á»™ng Ä‘áº¿n module Ä‘Ã³;
- Ä‘Ã¡nh dáº¥u cÃ¡c pháº§n cÃ²n `PARTIAL`, `UNVERIFIED`, hoáº·c cáº§n audit sÃ¢u;
- lÃ m nguá»“n tham chiáº¿u khi port tá»«ng pháº§n sang project Java má»›i.

KhÃ´ng dÃ¹ng thÆ° má»¥c nÃ y nhÆ° tráº¡ng thÃ¡i tiáº¿n Ä‘á»™ má»›i nháº¥t cá»§a demo rebuild. Tiáº¿n
Ä‘á»™ hiá»‡n táº¡i, cÃ¡c closeout smoke, kiáº¿n trÃºc Java sau refactor, vÃ  bÃ i táº­p cho
dev chat má»›i náº±m á»Ÿ:

```text
<PROJECT_ROOT>\rebuild_plan
```

`<PROJECT_ROOT>` là thư mục chứa `modules`, `rebuild_game`, và `rebuild_plan`.
Không ghi cố định đường dẫn theo máy cá nhân trong tài liệu hoặc code.

Äá»c bridge nÃ y trÆ°á»›c náº¿u báº¡n lÃ  dev chat má»›i:

```text
modules\rebuild_plan\22_current_rebuild_handoff_bridge.md
```

## NhÃ³m TÃ i Liá»‡u Ná»n

1. `01_source_code_architecture.md`
   - báº£n Ä‘á»“ kiáº¿n trÃºc source gá»‘c.

2. `02_runtime_skeleton.md`
   - khung Java rebuild ban Ä‘áº§u.

3. `03_module_port_order.md`
   - thá»© tá»± port module tá»« tháº¥p lÃªn cao.

4. `04_opcode_matrix.md`
   - ma tráº­n opcode event/cutscene trong `game.c`.

5. `05_resource_format_specs.md`
   - spec tÃ i nguyÃªn `img/spr/map/mod/script/event/ui`.

6. `06_runtime_core_notes.md`
   - lifecycle, input, state manager, runtime/core.

7. `07_resource_renderer_notes.md`
   - loader, cache, sprite, map, font.

8. `08_effect_ui_renderer_notes.md`
   - effect manager `b.java` vÃ  UI `.ui` runtime.

9. `09_world_event_notes.md`
   - room, actor, event VM, camera, opcode groups.

10. `10_world_event_opcode_matrix.md`
    - opcode World/Event `0..88` tá»« `game.c`.

11. `11_world_event_opcode_deep_audit.md`
    - branch/condition, inventory/task/reward, UI, transition, battle trigger.

12. `12_ui_system_notes.md`
    - UI runtime: `ab/ao/af/al/ac`, `.ui`, `game.h`, package `a/*`.

13. `13_gameplay_battle_save_notes.md`
    - gameplay/battle/save: `game.g`, `game.b`, `game.d`, `ar`, save slots.

14. `14_source_code_remaining_audit.md`
    - cÃ¡c pháº§n source_code cÃ²n cáº§n audit trÆ°á»›c rebuild full game.

15. `15_renderer_primitive_deep_audit.md`
    - sprite frame/cell/animation, map layer, bitmap font, DB bootstrap.

16. `16_ui_workflow_matrix.md`
    - `.ui` file, widget id, input mask, side effect/state change trong `game.h`.

17. `17_battle_state_machine.md`
    - battle state machine `game.d.P`, UI battle flow, unit model `game.b`.

18. `18_battle_formula_status_matrix.md`
    - cÃ´ng thá»©c battle, skill family, status/form, buff/debuff, element relation.

19. `19_world_tick_actor_matrix.md`
    - world tick, actor behavior, random encounter, camera/effect actor.

20. `20_text_cutscene_renderer_matrix.md`
    - `game.j`: text/cutscene renderer, typewriter, prompt, paging, layout.

21. `21_sms_payment_side_effect_matrix.md`
    - SMS/payment side effects: `an`, `q/u/v`, VM `scene_13.mib`, save slots.

## CÃ¡ch DÃ¹ng ÄÃºng

Khi lÃ m má»™t task má»›i:

1. Äá»c tÃ i liá»‡u hiá»‡n táº¡i trong `..\rebuild_plan` trÆ°á»›c.
2. DÃ¹ng tÃ i liá»‡u trong `modules\rebuild_plan` Ä‘á»ƒ hiá»ƒu cÆ¡ cháº¿ gá»‘c.
3. Má»Ÿ láº¡i source/resource tháº­t Ä‘á»ƒ kiá»ƒm chá»©ng.
4. KhÃ´ng láº¥y ghi chÃº cÅ© lÃ m báº±ng chá»©ng cuá»‘i cÃ¹ng náº¿u chÆ°a Ä‘á»‘i chiáº¿u láº¡i file gá»‘c.


