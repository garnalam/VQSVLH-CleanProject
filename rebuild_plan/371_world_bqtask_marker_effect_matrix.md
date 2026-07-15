# 371 - World bqTask Marker / Effect Matrix

Date: 2026-07-15

Scope: branch quest marker/effect outside panel, driven by `/data/script/bqTask.mid` and `game.e.G()`.

## Source Facts

| Source | Fact | Status |
| --- | --- | --- |
| `game.e` constructor | Loads `/data/script/bqTask.mid` into two byte matrices `ap` and `aq`. | `AUDITED` |
| `bqTask.mid` | Contains two 30-row matrices. Each row shape is `[sceneId, roomIndex, eventGroupIndex]`. | `AUDITED` |
| `game.e.G()` first loop | Iterates `aq`; if row matches current scene/room and event is active, it creates `new g()`, loads sprite `259`, and attaches marker to event actor `b()[4]`. | `AUDITED` |
| `game.e.G()` second loop | Iterates `ap`; if row matches current scene/room and event is active and `a(b2)` passes, it creates sprite `259` marker animation `7`. | `AUDITED` |
| Marker position | Source calls `g.b(actor.j, actor.k - 40)`, so marker is anchored above actor at `y - 40`. | `PORTED/PARTIAL` |
| Actor flag | Source calls `actor.f((byte)1)` when marker is attached. | `PENDING/PARTIAL` |

## Marker Type Matrix

| Source path | Source condition | Sprite/state | Rebuild equivalent | Status |
| --- | --- | --- | --- | --- |
| `aq` direct-ready marker | `this.b(b2)` true | `sprite 259`, anim/state `1` | Not ported yet; exact `this.b(b2)` condition needs event VM/quest objective parity. | `PENDING` |
| `aq` active branch marker | `b()[1] == 1`, task exists in `F`, and `F[n][1] == 1` | `sprite 259`, anim/state `15` | `SourceQuestMarker(actorId=35, animation=15)` for branch task 0 active. | `PORTED/PARTIAL` |
| `ap` available branch marker | `this.a(b2)` true | `sprite 259`, anim/state `7` | `SourceQuestMarker(actorId=35, animation=7)` for task 0 available after early main/event gate. | `PORTED/PARTIAL` |
| Completed branch task | `F[n][1] == 3` or event complete | No active marker from this slice. | Marker list clears when task 0 status is `3`. | `PORTED/PARTIAL` |

## Concrete Row 0 Slice

Decoded source rows:

| Table | Row | Event group | Source event | Meaning |
| --- | --- | --- | --- | --- |
| `ap[0]` | `[1,0,7]` | `scene_1 room0 event_group 7` | opcode `43`, `short_args=[0,1,1,0,35,...]` | Branch task 0, actor 35, available/accept side. |
| `aq[0]` | `[1,0,8]` | `scene_1 room0 event_group 8` | opcode `44`, `short_args=[0,1,1,0,35,...]` | Branch task 0, actor 35, active/complete side. |

Actor 35 in rebuild `loadScene1Room0` is visible at source position `(294,175)`, matching the source marker attachment target for this first branch quest.

## Rebuild Changes

- Added persistent `SourceQuestMarker`, using `SpriteAnim.load(259)` and source `actor.y - 40` anchor.
- Added `Scene.sourceQuestMarkers` and ticking/rendering.
- Added `Scene.sourceRefreshBqTaskMarkers()` with first-row source-backed mapping:
  - available branch task 0 -> actor 35, animation 7.
  - active branch task 0 -> actor 35, animation 15.
  - completed branch task 0 -> marker cleared.
- Rendered quest markers in world render after actors/temp sprites and before the upper map layer.
- Refactored branch quest runtime ownership into `VqsvBranchQuestRuntime`:
  - owns source-shaped `F/H` rows.
  - owns `bqTask` marker list.
  - `Scene.sourceRefreshBqTaskMarkers()` is now a delegate, not the implementation owner.
- Wired real `scene_1 room0 group7` Dodo interaction:
  - actor 35 starts branch task row 0 through source-shaped opcode `43`.
  - opcode `49` opens `taskOption.ui` with task id `0`.
  - confirm result `0` writes `F/H` through `Scene.sourceAcceptBranchTask(0)`.
  - back/reject result does not write `F/H`.
  - accept path runs source-shaped op40 task tips, completes event `[1,0,7]`, then refreshes `bqTask` marker to active animation `15`.

## Smoke

New checkpoints:

- `world_bqtask_marker_available`
- `world_bqtask_marker_active`
- `world_bqtask_marker_completed_clear`
- `world_dodo_group7_accept_task0`
- `world_dodo_group7_reject_no_task`

Verified PNGs:

- `rebuild_game/build_intro_demo/world_panel_full_bqtask_marker/world_bqtask_marker_available.png`
- `rebuild_game/build_intro_demo/world_panel_full_bqtask_marker/world_bqtask_marker_active.png`
- `rebuild_game/build_intro_demo/world_panel_full_bqtask_marker/world_bqtask_marker_completed_clear.png`

Result:

- `world_panel_full`: PASS `63/63`.
- `world_bqtask_marker_available`: marker visible above actor 35 at the source `actor.y - 40` anchor.
- `world_bqtask_marker_active`: marker visible for active branch task 0.
- `world_bqtask_marker_completed_clear`: marker cleared when branch task 0 status is `3`.
- `world_dodo_group7_accept_task0`: real actor35 interaction accepts row0, `F/H` becomes active, event `[1,0,7]` completes, marker becomes anim `15`.
- `world_dodo_group7_reject_no_task`: actor35 interaction closes without accepting, `F/H` remains empty, marker remains available anim `7`.

## Remaining Partial/Pending

| Area | Status | Note |
| --- | --- | --- |
| Full 30-row `bqTask.mid` mapping | `PENDING` | Only row 0 is ported. |
| Marker animation `1` condition | `AUDITED/PENDING` | Predicate source audited in `372`; code still needs backing objective fields before porting. |
| Dodo completion groups `8/9` | `PENDING` | Accept group7 is wired; active/complete-side objective logic is not ported yet. |
| Actor `f((byte)1)` side effect | `PENDING` | Rebuild has no exact actor flag parity yet. |
| Generic event VM integration | `PENDING` | Current slice is source-shaped, not full decoded VM. |
| Exact sprite 259 timing/pixel compare | `PARTIAL` | Uses source sprite/state but no MIDP pixel compare. |

## Next

Recommended next slice:

1. Either expand `bqTask` mapping row-by-row for concrete side quests as they become reachable.
2. Predicate audit is now in `372_world_bqtask_marker_predicate_a_b_audit.md`; next code slice should be trace-only generic evaluator before porting more rows.
