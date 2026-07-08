# 98 Save Resume Bunny Task Source Audit

## Scope

User request: after receiving the elder Bunny-catching task and entering the next map, the original asks whether to save progress. Rebuild should show the save prompt, write current progress, and title menu should allow continuing from that state.

Status key: PORTED / PORTED-PARTIAL / APPROX / STUB / PENDING / UNKNOWN.

## Source Findings

### Save prompt entry

Source file: `modules/source_code/decoded/decompiled_source_cfr/game/k.java`

World state entry case `22`:

- `this.S.H();`
- `this.S.a("Có lưu dữ liệu không?");`

Conclusion: source uses `game.h` UI facade to open a yes/no save prompt in world state 22.

Rebuild status: PORTED-PARTIAL.

### Event opcode save flow

Source file: `modules/source_code/decoded/decompiled_source_cfr/game/c.java`

Opcode `46`:

- first opens prompt text from event string through `S.H()` and `S.a(...)`
- if UI local flag `S.f == 0` and confirm key:
  - `S.f = 1`
  - `S.a("Đang lưu...")`
  - `S.J()`
- when `S.f == 1`:
  - marks event cell state `b[game.k.a(w.f,w.g)][event] = 3`
  - calls `((k)y).k()`
  - on success displays `"Lưu thành công"`
  - `S.f = 2`
- when `S.f == 2`:
  - closes UI with `S.I()`
  - continues event
- back/cancel skips and continues without saving.

Conclusion: save is an event opcode, not only system menu behavior. It both writes RMS and can mark the event cell complete.

Rebuild status: PORTED-PARTIAL.

### Source save payload

Source file: `modules/source_code/decoded/decompiled_source_cfr/game/k.java`

`game.k.k()` calls:

- `c(this.c)`
- `X()`
- `Z()`
- `game.k.h()`
- `game.k.ab()`

Static RMS record names:

- `PK6_RMS_ACTOR`
- `PK6_RMS_WORLD`
- `PK6_RMS_EVENT`
- `PK6_RMS_RMS`
- `PK6_RMS_SMS`
- `PK6_RMS_CNTSMS`
- `PK6_RMS_GOLD`
- `PK6_RMS_POKPET`
- `PK6_RMS_CONITEM`
- `PK6_RMS_PETBALL`

Nearby source methods show pet party and bank serialization using `game.b.P()` payload arrays. Event state and world/actor state are written into separate RMS records.

Conclusion: full original save is multi-record RMS persistence. Rebuild must not claim exact RMS parity until all record formats are ported.

Rebuild status: PORTED-PARTIAL route snapshot.

## Rebuild Implementation 2026-07-08

Files:

- `rebuild_game/src/main/java/VqsvSaveRuntime.java`
- `rebuild_game/src/main/java/VqsvSavePromptBlocking.java`
- `rebuild_game/src/main/java/Scene1Room0Group0Script.java`
- `rebuild_game/src/main/java/VqsvSceneView.java`
- `rebuild_game/src/main/java/VqsvIntroDemo.java`
- `rebuild_game/src/main/java/VqsvEventState.java`
- `rebuild_game/src/main/java/com/vqsv/rebuild/state/BootFlowState.java`
- `rebuild_game/src/main/java/com/vqsv/rebuild/state/GameStateMachine.java`
- `rebuild_game/src/main/java/com/vqsv/rebuild/state/LegacyIntroDemoState.java`

Implemented:

- inserted save prompt after elder Bunny task transition into scene 1 room 1, before `Op13FreeWorldTrigger`
- prompt text: `"Bạn có muốn lưu trữ không?"`
- confirm saves and shows `"Lưu thành công"`
- skip continues without saving
- save file: `rebuild_game/build/save/vqsv_autosave.properties`
- title menu detects the save and shows `Chơi tiếp` before `Chơi mới`
- `Chơi tiếp` loads the saved `VqsvIntroDemo.Scene` snapshot through `LegacyIntroDemoState(true)`
- smoke-only accessors verify that the selected title label is `Chơi tiếp`
  and that confirm routes to `LegacyIntroDemoState`
- prompt softkey positions are backed by `msgconfirm.ui` widget 2/3:
  left confirm rect starts at `(1,296)`, right back rect starts at `(196,296)`.
  The visible rebuild touch glyphs are drawn as bottom-corner blue check/X
  controls matching the captured PC client surface; this is still not a full
  `game.h`/`ao` widget runtime.

Saved fields:

- event index
- current scene/room
- camera and player position/direction/visibility
- actor x/y/direction/visibility for loaded room actors
- source event state map
- source bag items
- source party pets and bank pets, including `sourcePayload`, skill ids, and cooldowns
- source money/badges and a few source flags

Current classification:

- prompt call point: PORTED-PARTIAL
- prompt UI: PORTED-PARTIAL source-backed `msgconfirm.ui` shape plus visible
  check/X softkeys, not pixel-perfect
- save payload: PORTED-PARTIAL rebuild route snapshot
- full source RMS records: PENDING
- title menu continue: PORTED-PARTIAL
- resume into Bunny task map: PORTED for current route slice

## Smoke PNG

- `rebuild_game/build/smoke/room1_bunny_save_prompt.png`
- `rebuild_game/build/smoke/room1_bunny_save_success.png`
- `rebuild_game/build/smoke/room1_bunny_save_resume_state.png`
- `rebuild_game/build/smoke/boot_title_continue_with_save.png`
- `rebuild_game/build/smoke/route_bunny_after_battle_task_save_regression.png`
- `rebuild_game/build/smoke/route_elder_after_battle_reward_state_save_regression.png`
- `rebuild_game/build/smoke/route_sophie_after_battle_branch_save_regression.png`

## Remaining Work

- Full `game.h` yes/no prompt state and softkey behavior is still partial.
- Full RMS parity for all ten `PK6_RMS_*` records is pending.
- Continue/load currently targets the rebuild route snapshot, not arbitrary original save slots.
- No pixel-perfect claim for the prompt UI until original-vs-rebuild compare exists.
