# Source Code Architecture

Tài liệu này ghi lại kiến trúc thật của code gốc trong
`source_code/decoded/decompiled_source_cfr`.

## Mục Tiêu

- Hiểu entrypoint, game loop, state machine, input, render pipeline.
- Xác định module nào là resource loader, renderer, event VM, UI, battle, save.
- Tạo bản đồ phụ thuộc để project rebuild không bị dựng sai móng.

## Entry Và Vòng Đời

| Thành phần | Vai trò | Trạng thái |
|---|---|---|
| `game/GameMIDLet.java` | MIDlet entrypoint, tao Canvas | VERIFIED |
| `game/e.java` | Canvas MIDP, loop repaint/serviceRepaints, input key/pointer | VERIFIED |
| `game/i.java` | State manager cao cap, dieu phoi man/game state/audio | VERIFIED Runtime/Core |
| `an.java` | Base runtime/state abstraction, kich thuoc man, font MIDP, shared helpers | VERIFIED Runtime/Core |
| `ap.java` | Input bitmask, map phim `0` -> bit `1` | VERIFIED |

Chi tiet xem [06_runtime_core_notes.md](06_runtime_core_notes.md).

Runtime/Core audit note:

- 4 diem con treo da chot trong `06_runtime_core_notes.md`: `game.i` states `20/22/23`, `game.i` state `12` voi `game.d`, SMS/payment side effects trong `an`, va timer repaint/loading gate 200ms.
- `game.i`/`an` duoc xem la du cho khung runtime rebuild. Battle/world gameplay chi tiet van la audit rieng o cac module `game.d`, `game.k`, `game.c`.

## World Và Scene

| Thành phần | Vai trò | Trạng thái |
|---|---|---|
| `game/k.java` | World controller: load room, actor, map, render world, chuyển scene | PARTIAL |
| `game/c.java` | Event/cutscene VM, đọc opcode event và chạy timeline | PARTIAL |
| `game/a.java` | Actor wrapper trong world, init từ actor record room | PARTIAL |
| `f.java`, `n.java` | Entity base: vị trí, visible flags, animation bridge | PARTIAL |
| `t.java` | Display list/render order: map layer, actor sort theo Y, overlay | VERIFIED sơ bộ |

## Renderer Và Resource

| Thành phần | Vai trò | Trạng thái |
|---|---|---|
| `ae.java` | Binary helpers, text helpers, image conversion/loading helpers | PARTIAL |
| `aq.java` | Load `sprite.mid`, `modInfo.mid`, script db, texture info | VERIFIED Resource/Core |
| `aa.java` | Load/cache `spr_*_all(r)` thành `o` | VERIFIED sprite path |
| `o.java` | Sprite metadata container | VERIFIED |
| `d.java` | Sprite animation renderer, MIDP `drawRegion` transform | VERIFIED sơ bộ |
| `am.java` | Image cache/loader từ `/data/img` | PARTIAL |
| `j.java` | Map renderer/cache/tile layer, không phải `game.j` | PARTIAL |
| `s.java` | Bitmap font renderer từ `/font.bin` | VERIFIED sơ bộ |
| `b.java` | Effect/transition manager cho opcode 9 và một số effect khác | VERIFIED/PARTIAL |

Resource/Renderer audit note:

- Chi tiet xem [07_resource_renderer_notes.md](07_resource_renderer_notes.md).
- Da nam du core flow cho `aq/am/aa/o/root d/root j/s`: global tables, image cache, sprite metadata, sprite animator, map renderer, bitmap font.
- `b.java` effect manager va UI `.ui` renderer da duoc audit rieng trong [08_effect_ui_renderer_notes.md](08_effect_ui_renderer_notes.md). Trang thai moi: VERIFIED/PARTIAL; architecture/API/flow da nam, con can screenshot/runtime validation cho pixel timing va `z.java` dynamic binding.

## Text/UI

| Thành phần | Vai trò | Trạng thái |
|---|---|---|
| `game/j.java` | Text/cutscene text renderer, typewriter, prompt `Nhấn nút 0...` | PARTIAL |
| `ab.java` | UI manager, stack/cache UI screens | VERIFIED/PARTIAL |
| `ao.java` và nhóm `a/*` | UI components/layout từ `.ui` | VERIFIED/PARTIAL |

## Gameplay/Battle/Save

| Thành phần | Vai trò | Trạng thái |
|---|---|---|
| `game/g.java` | Player/pet/world gameplay state | UNVERIFIED |
| `game/b.java` | Battle/item-related logic | UNVERIFIED |
| `ar.java` | RMS/save wrapper | UNVERIFIED |
| `ai.java`, `ah.java` | Camera/effect/helper actors | PARTIAL |

## Render Pipeline Hiện Biết

`game.e.paint()` gọi `game.i.b(Graphics)`.

Trong world state, `game.k` gọi display list `t`:

1. Update camera theo `ai`.
2. Map layer 1 và 2.
3. Actor list `s=2`.
4. Actor list `s=1`, sort theo `j`/Y.
5. Map layer 3.
6. Actor/list `s=0`.
7. Event overlay/text/effect qua `game.c`.
8. UI overlay qua `ab`/`game.h`.

Chi tiết cần audit tiếp:

- Chính xác state IDs trong `game.i`.
- Khi nào `game.c.b()` update trước/sau actor tick.
- Quan hệ giữa `game.k.P`, map mode, battle mode, UI mode.
