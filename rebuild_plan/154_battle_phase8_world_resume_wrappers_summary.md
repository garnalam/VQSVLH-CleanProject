# 154 Battle Phase 8 World Resume Wrappers Summary

Status date: 2026-07-09

Status: PHASE 8 WRAPPER CLOSEOUT / AUDIT SUMMARY.

Purpose:

- Summarize the three trace-only `VqsvWorldResumeDescriptor` slices created
  after `150_battle_phase8_state10_freeworld_resume_audit.md`.
- Decide what should happen next before any generic decoded event VM work.

## Source Basis

From `150_battle_phase8_state10_freeworld_resume_audit.md`:

- `game.i` app state `10` clears the previous module, loads `game.k`, calls
  `k.p()`, installs it, then enters app state `11`.
- `game.k.p()` enters world state `P=0`.
- `game.k.b()` state `0` drives free-world input, UI, event visuals, and
  `game.c` event advancement.
- Rebuild still uses manual Java `Blocking` continuations, not the source
  `game.i/game.k/game.c` runtime.

## Implemented Wrappers

| Descriptor | Boundary | Wrapped blocker | Source event assert | Status |
| --- | --- | --- | --- | --- |
| `SCENE1_ROOM1_AFTER_SAVE_TO_OP13` | room1 save prompt -> Bunny op13 | `Op13FreeWorldTrigger` | `[1,1,1]` complete | `PORTED/PARTIAL` trace-only |
| `SCENE1_ROOM1_AFTER_BUNNY_TO_ROOM0` | Bunny group0 complete -> room0 transition | `ActorTransitionFreeWorldTrigger` | `[1,1,0]` complete | `PORTED/PARTIAL` trace-only |
| `SCENE1_ROOM0_AFTER_GROUP6_FREEWORLD` | Elder group6 complete -> post-group6 free-world | `Room0PostGroup6FreeWorld` | `[1,0,6]` complete | `PORTED/PARTIAL` trace-only |

All three wrappers:

- assert current scene/room before delegating;
- assert the expected source event state is complete;
- assert the next blocker type has not drifted;
- trace `game.i state10->11` and `game.k.p()->P0`;
- assert they do not mutate `Scene.eventIndex`;
- delegate to the existing blocker and keep behavior unchanged.

## Files Added Or Updated

Java:

- `rebuild_game/src/main/java/VqsvWorldResumeDescriptor.java`
- `rebuild_game/src/main/java/Scene1Room0Group0Script.java`
- `rebuild_game/src/main/java/Scene1Room1BunnyScript.java`
- `rebuild_game/src/main/java/Scene1Room0Group6ElderBattleScript.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Docs:

- `rebuild_plan/151_battle_phase8_world_resume_descriptor_closeout.md`
- `rebuild_plan/152_battle_phase8_bunny_return_world_resume_descriptor_closeout.md`
- `rebuild_plan/153_battle_phase8_group6_freeworld_resume_descriptor_closeout.md`
- `rebuild_plan/154_battle_phase8_world_resume_wrappers_summary.md`

## Smoke Coverage

Wrapper-specific smoke PNG:

- `rebuild_game/build/smoke_phase8_world_resume/room1_op13_bunny_trigger.png`
- `rebuild_game/build/smoke_phase8_bunny_return_resume/return_room0_transition.png`
- `rebuild_game/build/smoke_phase8_group6_freeworld_resume/post_group6_room2_entry_tip.png`

Regression smoke PNG:

- `route_bunny_after_battle_task`
- `actor52_interaction_group2`
- `route_elder_after_battle_reward_state`
- `route_sophie_after_battle_branch`
- `post_group6_room0_back_from_room2`

Build/check used across the slices:

- `powershell -ExecutionPolicy Bypass -File .\build.ps1`
- `java -cp .\build\classes com.vqsv.rebuild.Main --check`
- `java -cp .\build\classes VqsvBattleDamageFormulaCheck`
- Java mojibake literal scan

## Honest Classification

| Area | Status | Note |
| --- | --- | --- |
| Current route world-resume boundaries | `PORTED/PARTIAL` | Trace/assert bridge exists for the three manual boundaries currently in use. |
| Existing trigger/free-world behavior | `UNCHANGED` | Wrappers delegate to existing blockers. |
| `Scene.eventIndex` safety | `PORTED/PARTIAL` | Wrappers assert no local mutation; full source cursor model is still absent. |
| `game.i/game.k/game.c` runtime parity | `PENDING` | Rebuild still does not swap modules or run the source event queue. |
| Generic decoded event VM | `PENDING / NOT READY` | Still intentionally not attempted. |
| Free-world side quests and broad room triggers | `PENDING` | Only current route blockers are represented. |

## Decision

Phase 8 trace-wrapper objective is complete for the current manual route.

Do not continue adding wrappers blindly. The next useful work should be one of:

1. **Move to next roadmap phase** if the goal is gameplay progress.
2. **Audit one specific missing free-world trigger/side quest** if the user wants
   to continue event integration.
3. **Start decoded event VM only after a separate source-backed design audit**.

Recommended next step:

```text
Move to the next roadmap phase unless a concrete free-world trigger is chosen.
```

Reason:

- The wrapper layer now protects the known battle/save/free-world boundaries.
- More wrappers without new behavior would add little value.
- A generic event VM is still too broad without a dedicated design/audit pass.
