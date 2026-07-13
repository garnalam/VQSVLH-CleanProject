# 162 Release Room1 After-Save Freeworld Recovery Closeout

Status date: 2026-07-09

Status: RELEASE BUGFIX / ROOM1 BUNNY AFTER-SAVE MOVEMENT / PNG SMOKE COVERED.

Purpose:

- Fix a reported release bug where the player could become unable to move after
  receiving the Elder Bunny task, entering room1, accepting the save prompt, and
  closing the prompt.
- This is separate from the older `Chơi tiếp` stale-save repair. The reported
  bug happens immediately after saving in the same runtime.

## Source/Rebuild Boundary

Expected rebuild route:

```text
scene1 room0 group0 Elder Bunny task
-> transition to scene1 room1 actor37
-> room1 group1 save prompt wrapper completes [1,1,1]
-> room1 group0 op13 free-world trigger waits at rect [370,176,80,32]
-> player can move freely until intersecting op13
```

Observed saved state at this boundary can legitimately look like:

```text
scene=1
room=1
eventIndex=<room1 Bunny op13 index>
player=16,272,...
eventState includes 1:1:1=3
eventState does not include 1:1:0=3
```

`player=16,272` is the room1 transition actor area, not the Bunny op13 rect.
Therefore runtime must keep or recover the op13 free-world blocker; otherwise
the player appears stranded.

## Live Log Root Cause

After adding live logging to `build/debug/vqsv_live_debug.log`, the release run
showed that Swing focus and raw key input were working:

```text
input keyPressed code=68 text=D focusOwner=true
legacy tick input= U:true ... R:true ...
```

But the scene received:

```text
scene tick ... keys={0:false,U:false,D:false,L:false,R:false,B:false}
```

The real movement bug was in `LegacyIntroDemoState`: it forwarded aliases one by
one (`UP`, `W`, `NUMPAD8`, etc.). A pressed alias could be overwritten by a later
unpressed alias in the same direction group. Example:

```text
W=true -> keyUp=true
NUMPAD8=false -> keyUp=false
```

This made WASD/arrow movement appear dead in the official release path even
though the panel was receiving keys.

## Fix

Fixes:

1. `LegacyIntroDemoState` now ORs each movement direction group before calling
   `Scene.setMoveKey`:
   - up: `UP || W || NUMPAD8`
   - down: `DOWN || S || NUMPAD2`
   - left: `LEFT || A || NUMPAD4`
   - right: `RIGHT || D || NUMPAD6`
   - back: `ESCAPE || BACK_SPACE`
2. Added a narrow recovery guard in `VqsvIntroDemo.Scene.tick()`:

- only when `current == null`;
- only in `scene=1, room=1`;
- only after room1 group1 save prompt is complete;
- only before room1 group0 Bunny event is complete;
- only when event index is exactly at/just after the registered room1 Bunny
  op13 index;
- only when no dialog/choice/save prompt/battle/world petstate/evolve UI is
  open.

If those conditions are met, rebuild reinstalls:

```text
VqsvWorldResumeDescriptor.SCENE1_ROOM1_AFTER_SAVE_TO_OP13
-> Op13FreeWorldTrigger(1,1,0,370,176,80,32)
```

This is `PORTED/PARTIAL`: it protects the current manual route boundary. It is
not a generic decoded event VM or full source cursor model.

## Files Changed

- `rebuild_game/src/main/java/com/vqsv/rebuild/state/LegacyIntroDemoState.java`
- `rebuild_game/src/main/java/com/vqsv/rebuild/debug/VqsvDebugLog.java`
- `rebuild_game/src/main/java/com/vqsv/rebuild/runtime/GamePanel.java`
- `rebuild_game/src/main/java/com/vqsv/rebuild/core/GameApp.java`
- `rebuild_game/src/main/java/VqsvIntroDemo.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`
- `rebuild_plan/162_release_room1_after_save_freeworld_recovery_closeout.md`

## Smoke PNG

- `rebuild_game/build/smoke_release_bunny_debug/room1_bunny_after_save_immediate_free_move.png`
- `rebuild_game/build/smoke_release_bunny_debug/room1_bunny_after_save_stranded_recover.png`
- `rebuild_game/build/smoke_release_bunny_debug/legacy_room1_wasd_bridge_free_move.png`
- `rebuild_game/build/smoke_release_bunny_debug/route_bunny_after_battle_task.png`
- `rebuild_game/build/smoke_release_bunny_debug/route_elder_after_battle_reward_state.png`
- `rebuild_game/build/smoke_release_bunny_debug/route_sophie_after_battle_branch.png`

## Verification

Passed:

```powershell
cd rebuild_game
powershell -ExecutionPolicy Bypass -File .\build.ps1
java "-Dvqsv.modules=..\modules" -jar build\libs\vqsv-liet-hoa-rebuild.jar --check
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvBattleDamageFormulaCheck
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint room1_bunny_after_save_immediate_free_move build\smoke_release_bunny_debug\room1_bunny_after_save_immediate_free_move.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint room1_bunny_after_save_stranded_recover build\smoke_release_bunny_debug\room1_bunny_after_save_stranded_recover.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint legacy_room1_wasd_bridge_free_move build\smoke_release_bunny_debug\legacy_room1_wasd_bridge_free_move.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint route_bunny_after_battle_task build\smoke_release_bunny_debug\route_bunny_after_battle_task.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state build\smoke_release_bunny_debug\route_elder_after_battle_reward_state.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint route_sophie_after_battle_branch build\smoke_release_bunny_debug\route_sophie_after_battle_branch.png
```

## Next Step

Recommended next:

```text
User should retest the release route: skip/new game -> Elder Bunny task -> save
yes -> move in room1. If movement is still blocked, capture the exact key/input
path and inspect live trace/current state rather than widening the recovery.
```
