# New Chat Handoff - Current State And Entry Exercises

Date: 2026-07-03

Use this file together with `23_new_chat_handoff_training.md`. That older file
teaches the general rules; this file updates the current game-scene progress and
gives the next chat a compulsory exercise before it is allowed to implement.

## Copy-Paste Prompt For A New Chat

```text
Bạn đang tiếp quản dự án rebuild VQSV/Liệt Hỏa tại:
- Root: E:\VQSVLH-CleanProject
- Game project: E:\VQSVLH-CleanProject\rebuild_game
- Modules/source/assets: E:\VQSVLH-CleanProject\modules
- Plans/audits: E:\VQSVLH-CleanProject\rebuild_plan

Luật tối cao:
1. Bám bản gốc từ logic đến assets/UI/timing/event chain. Không đoán bừa.
2. Mọi thứ liên quan phải truy ra file gốc: event JSON, decompiled source, asset/module.
3. Phân loại rõ: PORTED, APPROX, STUB, MISSING, PENDING, UNKNOWN.
4. Không nối event theo cảm tính. Phải đi theo trigger/source state:
   event trước kết thúc ra state nào, event sau consume state nào, opcode nào chạy.
5. Làm xong một phần phải tự check lại với bản gốc, build/check/smoke nếu sửa code.
6. Không được che giấu stub/approx. Battle/capture hiện vẫn là stub, chưa phải game.d thật.
7. Không đụng lại đoạn user đã xác nhận ổn nếu task không yêu cầu.

Trước khi làm code, đọc theo thứ tự:
1. E:\VQSVLH-CleanProject\rebuild_plan\23_new_chat_handoff_training.md
2. E:\VQSVLH-CleanProject\rebuild_plan\34_new_chat_handoff_current_state_and_exercises.md
3. E:\VQSVLH-CleanProject\rebuild_plan\31_scene1_post_intro_original_init_trace.md
4. E:\VQSVLH-CleanProject\rebuild_plan\28_scene1_room0_group0_manual_script_audit.md
5. E:\VQSVLH-CleanProject\rebuild_plan\32_scene1_room1_freeworld_op13_audit.md
6. E:\VQSVLH-CleanProject\rebuild_plan\33_scene1_room1_group0_post_op13_audit.md
7. E:\VQSVLH-CleanProject\modules\event\decoded\data__event__scene_1.mid.json
8. E:\VQSVLH-CleanProject\modules\source_code\decoded\decompiled_source_cfr\game\c.java
9. E:\VQSVLH-CleanProject\rebuild_game\src\main\java\VqsvIntroDemo.java

Sau khi đọc xong, làm "Bài tập đầu vào" trong file 34 và trả lời ngay trong chat.
Chỉ khi bài tập đạt thì mới bắt đầu sửa code.
```

## Mission

Mục tiêu tối thượng của dự án là rebuild lại game Java/MIDP VQSV/Liệt Hỏa bằng
Java hiện đại, càng sát bản gốc càng tốt. Không phải làm một bản "trông giống",
mà là dựng lại dựa trên source/event/assets thật:

- event flow đúng opcode và state gốc
- map/actor/UI/sprite/effect dùng đúng asset hoặc ghi rõ chưa đủ bằng chứng
- gameplay/battle/item/pet/task không được tự chế
- smoke test bằng ảnh để user đối chiếu trực tiếp

## Current Confirmed Chain

The current implemented chain in `VqsvIntroDemo.java` is:

1. `scene_1 room3 group0`
   - intro "Sáu năm sau..."
   - ends with `op22 [1,2,199,218,240,320]`, then `op6 [1,0,0,0]`, then `op14`
   - loads `scene_1 room0`

2. `scene_1 room0 group0`
   - source-valid because first record is `op15 [1,3,0]`
   - starts after room3 group0 is complete
   - elder gives Bunny capture task
   - item/pet/task UI side effects are partly implemented/source-backed, some APPROX
   - ends with:
     - `op22 [1,1,55,279,240,320]`
     - `op25 [1]`
     - `op6 [1,1,37,1]`
     - `op14`
   - loads `scene_1 room1`, places Neil near actor 37

3. `scene_1 room1 free movement + op13`
   - room1 group0 must not auto-run
   - first record is `op13 [370,176,80,32]`
   - current rebuild has source-shaped free movement and area trigger
   - full `game.g.q()` collision is not ported yet; current collision is map-bounds only

4. `scene_1 room1 group0 after op13`
   - records after trigger:
     - `op37 [34,5,1]`
     - `op52 [0,1]`
     - `op66 [0]`
     - `op32 [0,0]`
     - `op47 [12,0,0]`
     - `op4 Neil "Chính là con thỏ..."`
     - `op56 [1,1] strings "50","0"`
     - `op23 [1,0,1]`
     - `op40 "Trở về tìm trưởng thôn!"`
     - `op14`
   - battle/capture is still a visible STUB/APPROX, forced to `l=-1` for success path
   - retry records `11..13` are PENDING and must not be appended linearly
   - actor index 50 is hidden by `op56`; if another Bunny-looking sprite remains, verify actor id before calling it a bug

## Current Next Source-Correct Step

The next work should be the return-to-village path and `scene_1 room0 group2`.

Evidence already found:

- `room1 group0` marks `op23 [1,0,1]`.
- `room0 group2` starts with `op86 [1,1,0]`.
- In `game.c.b()`, `op86` checks an event-state condition:
  it is eligible when the referenced event state is complete.
- `room0 group2 rec1` is `op16 [52]`.
- In `game.c.b()`, `op16` is an actor interaction trigger using `game.k.u`.
- Therefore the likely intended flow is:
  1. after catching Bunny, return from room1 to room0
  2. free movement in room0
  3. interact with actor 52, the village elder / Trưởng thôn
  4. run room0 group2

Do not implement this from memory. Re-read the records and source before code.

## Room0 Group2 Records To Audit Before Porting

Source: `modules\event\decoded\data__event__scene_1.mid.json`,
`room_index=0`, `group_index=2`.

Known record outline:

| Rec | Opcode | Args/Text | Initial understanding |
|---:|---:|---|---|
| 0 | 86 | `[1,1,0]` | gated by room1 group0 complete |
| 1 | 16 | `[52]` | interact with actor 52 |
| 2 | 4 | Neil: `Bị bắt` | dialog |
| 3 | 4 | Trưởng thôn: `Những con thỏ trông dễ thương làm sao.` | dialog |
| 4 | 5 | `[0,0,9,0,0]` | actor/player attached effect; audit source case 5 |
| 5 | 12 | `[15]` | delay |
| 6 | 4 | Neil: `Trưởng thôn ... có vẻ mục tiêu sai ...` | dialog |
| 7 | 4 | Trưởng thôn long explanation | dialog |
| 8 | 5 | `[0,0,14,0,0]` | actor/player attached effect |
| 9 | 12 | `[15]` | delay |
| 10 | 4 | Neil: `Tốt quá! Ta đi xem!` | dialog |
| 11 | 4 | Trưởng thôn: only choose one pet | dialog |
| 12 | 4 | Neil: `Thế này chả bằng cho à?` | dialog |
| 13 | 5 | `[1,52,3,0,0]` | elder-attached effect |
| 14 | 45 | `[1]` text: `Lựa chọn sủng vật cùng trưởng thôn tỷ thí.` | task/notice; audit source case 45 |
| 15 | 14 | `[]` | complete group |

## Source Places To Read For This Step

Mandatory:

- `modules\event\decoded\data__event__scene_1.mid.json`
  - room0 groups 1, 2, 3, 4, 5
  - room1 groups 0, 1, 2
- `modules\source_code\decoded\decompiled_source_cfr\game\c.java`
  - trigger scan `public final void b()`
  - `case 16`
  - `case 86`
  - `case 5`
  - `case 12`
  - `case 14`
  - `case 45`
  - `case 6`, `case 22`, `case 23`, `case 40`, `case 42` if transition/state is involved
- `rebuild_game\src\main\java\VqsvIntroDemo.java`
  - current manual event list
  - `Op13FreeWorldTrigger`
  - free movement
  - actor table loaders `loadScene1Room0()` and `loadScene1Room1()`
  - task notice/openbox renderers

Optional but useful:

- `modules\source_code\decoded\decompiled_source_cfr\game\k.java`
- `modules\source_code\decoded\decompiled_source_cfr\game\a.java`
- `modules\source_code\decoded\decompiled_source_cfr\game\f.java`
- `modules\source_code\decoded\decompiled_source_cfr\game\j.java`
- `modules\source_code\decoded\decompiled_source_cfr\game\d.java`

## Compulsory Entry Exercise

The next chat must answer this exercise before coding. It should write the
answers in the chat, with file/path/record/source references.

### Exercise A - Prove The Event Chain

Answer:

1. Which record in room1 group0 marks Bunny capture progress?
2. Which next room0 group consumes that state?
3. Why is room0 group2 not triggered immediately anywhere on the map?
4. What user/player action is needed for `op16 [52]`?
5. Which actor id must be found in `loadScene1Room0()` for the elder?

Expected evidence:

- event JSON room1 group0 record `op23 [1,0,1]`
- event JSON room0 group2 record 0 `op86 [1,1,0]`
- event JSON room0 group2 record 1 `op16 [52]`
- `game.c.b()` trigger scan for `op16` and `op86`

### Exercise B - Build A Record Matrix

Create a matrix for room0 group2 records `0..15` with:

- record index
- opcode
- args/text
- source meaning
- current rebuild plan
- status: PORTED / APPROX / STUB / MISSING / PENDING

No code yet. This matrix must be reviewed mentally against source before edits.

### Exercise C - Identify Unknowns Before Coding

List every unknown that must be checked before implementation, including:

- exact room1-to-room0 return trigger and transition target
- actor 52 position/direction/mode in room0
- how `op16` is represented in rebuild input model
- source behavior of `op45`
- whether `op5` effects should attach to player or actor 52
- whether current taskTip/openBox renderer is enough for `op45`, or only APPROX

### Exercise D - Smoke Plan

Write the smoke plan before coding:

- image after room1 group0 complete, Bunny actor 50 hidden
- image after returning to room0
- image standing near actor 52 before interaction
- image at first room0 group2 dialog
- image at task notice `Lựa chọn sủng vật cùng trưởng thôn tỷ thí.`
- image after group2 complete

## Acceptance Criteria For The Next Implementation

Do not call the implementation done unless:

1. The chain room1 group0 -> return room0 -> room0 group2 is source-backed.
2. The event does not auto-run before interacting with actor 52.
3. Actor 52 position/direction is verified from room0 actor table.
4. Every room0 group2 record `0..15` is represented, or explicitly marked
   PENDING/STUB with reason.
5. Build passes.
6. `--check` passes.
7. Smoke images are created and visually inspected.
8. A new audit file is written, recommended name:
   `35_scene1_return_to_room0_group2_elder_audit.md`.

## Current Known Stub/Approx Debt

Keep these visible:

- `game.d` battle/capture is not ported; Bunny capture is still a stub.
- Generic event-state table is not fully implemented; `op23/op14` mostly trace state now.
- Full tile/actor collision for free movement is not ported.
- `op16` interaction runner may need a minimal source-shaped implementation.
- `op45` task UI must be audited; do not assume it is the same as `op40`.
- `op5` source effect engine is still approximate in existing demo.
- Full ao/af/k/m UI runtime is not ported.

## Good First Response In A New Chat

A strong new chat should start by saying something like:

```text
Tôi sẽ đọc handoff 23 + 34, rồi đối chiếu event JSON/source trước.
Trước khi code tôi sẽ làm bài tập đầu vào: prove chain, record matrix,
unknowns, smoke plan. Tôi sẽ không tự nối room0 group2 nếu chưa chứng minh
op86/op16 đúng theo source.
```

Then it should actually read the files and answer the exercise.

