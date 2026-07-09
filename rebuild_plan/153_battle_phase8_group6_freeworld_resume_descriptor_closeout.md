# 153 Battle Phase 8 Group6 Free-World Resume Descriptor Closeout

Status date: 2026-07-09

Status: CODE SLICE COMPLETE / TRACE-ONLY / NO BEHAVIOR CHANGE INTENDED.

Purpose:

- Add source-shaped world resume trace bridge for room0 group6 Elder complete
  -> post-group6 free-world loop.
- Keep the existing `Room0PostGroup6FreeWorld` behavior intact.
- Avoid generic decoded event VM work.

## Files Changed

Java:

- `rebuild_game/src/main/java/VqsvWorldResumeDescriptor.java`
- `rebuild_game/src/main/java/Scene1Room0Group6ElderBattleScript.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Docs:

- `rebuild_plan/153_battle_phase8_group6_freeworld_resume_descriptor_closeout.md`

## Implemented

`VqsvWorldResumeDescriptor.SCENE1_ROOM0_AFTER_GROUP6_FREEWORLD`:

- asserts current room is `scene=1, room=0`;
- asserts room0 group6 source event `[1,0,6]` is complete;
- asserts the next manual blocker is still `Room0PostGroup6FreeWorld`;
- traces source resume concept:
  - `game.i state10->11`
  - `game.k.p()->P0`
- checks descriptor itself does not mutate `Scene.eventIndex`;
- delegates to the original free-world blocker.

Route shape is unchanged:

```text
room0 group6 op14 complete
-> WorldResumeDescriptor wrapper
-> Room0PostGroup6FreeWorld
```

`post_group6_room0_back_from_room2` intentionally keeps using
`Room0PostGroup6FreeWorld` directly because that smoke starts in room2, already
inside the free-world loop, not at the room0 group6 resume boundary.

## Verification

Passed:

- `powershell -ExecutionPolicy Bypass -File .\build.ps1`
- `java -cp .\build\classes com.vqsv.rebuild.Main --check`
- `java -cp .\build\classes VqsvBattleDamageFormulaCheck`
- Java mojibake literal scan

Focused smoke PNG:

- `rebuild_game/build/smoke_phase8_group6_freeworld_resume/post_group6_room2_entry_tip.png`
- `rebuild_game/build/smoke_phase8_group6_freeworld_resume/post_group6_room0_back_from_room2.png`
- `rebuild_game/build/smoke_phase8_group6_freeworld_resume/route_elder_after_battle_reward_state.png`
- `rebuild_game/build/smoke_phase8_group6_freeworld_resume/route_bunny_after_battle_task.png`
- `rebuild_game/build/smoke_phase8_group6_freeworld_resume/route_sophie_after_battle_branch.png`

## Classification

| Area | Status | Note |
| --- | --- | --- |
| room0 group6 -> post-group6 free-world trace | PORTED/PARTIAL | Source-shaped trace/assert bridge only. |
| `Room0PostGroup6FreeWorld` behavior | UNCHANGED / APPROX/PARTIAL | Existing free-world blocker still owns movement/transitions. |
| `Scene.eventIndex` mutation by descriptor | NOT MUTATED | Descriptor asserts this locally. |
| room2 entry tip and return to room0 | REGRESSION PASS | Focused smoke passes. |
| generic decoded event VM | PENDING | Still intentionally not attempted. |

## Next Roadmap Step

Phase 8 trace wrappers now cover the three current manual world-resume
boundaries:

1. room1 save -> op13 Bunny;
2. room1 Bunny complete -> room0 transition;
3. room0 group6 complete -> post-group6 free-world.

Next safe step:

```text
Write a short Phase 8 wrapper closeout/audit summary, then decide whether to
audit source state 10/free-world event resume further or move to the next
roadmap phase.
```

Do not start a generic decoded event VM without a new source-backed task.
