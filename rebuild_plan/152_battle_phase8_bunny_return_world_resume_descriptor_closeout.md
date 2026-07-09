# 152 Battle Phase 8 Bunny Return World Resume Descriptor Closeout

Status date: 2026-07-09

Status: CODE SLICE COMPLETE / TRACE-ONLY / NO BEHAVIOR CHANGE INTENDED.

Purpose:

- Add the second source-shaped world resume trace bridge from Phase 8:
  room1 group0 Bunny complete -> actor transition back to room0.
- Keep the existing manual `ActorTransitionFreeWorldTrigger` behavior intact.
- Avoid generic decoded event VM work.

## Files Changed

Java:

- `rebuild_game/src/main/java/VqsvWorldResumeDescriptor.java`
- `rebuild_game/src/main/java/Scene1Room1BunnyScript.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Docs:

- `rebuild_plan/152_battle_phase8_bunny_return_world_resume_descriptor_closeout.md`

## Implemented

`VqsvWorldResumeDescriptor.SCENE1_ROOM1_AFTER_BUNNY_TO_ROOM0`:

- asserts current room is `scene=1, room=1`;
- asserts room1 group0 source event `[1,1,0]` is complete;
- asserts the next manual blocker is still `ActorTransitionFreeWorldTrigger`;
- traces source resume concept:
  - `game.i state10->11`
  - `game.k.p()->P0`
- checks descriptor itself does not mutate `Scene.eventIndex`;
- delegates to the original transition trigger:

```text
ActorTransitionFreeWorldTrigger(1,1,37,3,1,0,30)
```

Route shape is unchanged:

```text
room1 group0 op14 complete
-> WorldResumeDescriptor wrapper
-> actor 37 transition trigger
-> room0 actor 30
```

## Verification

Passed:

- `powershell -ExecutionPolicy Bypass -File .\build.ps1`
- `java -cp .\build\classes com.vqsv.rebuild.Main --check`
- `java -cp .\build\classes VqsvBattleDamageFormulaCheck`
- Java mojibake literal scan

Focused smoke PNG:

- `rebuild_game/build/smoke_phase8_bunny_return_resume/return_room0_transition.png`
- `rebuild_game/build/smoke_phase8_bunny_return_resume/route_bunny_after_battle_task.png`
- `rebuild_game/build/smoke_phase8_bunny_return_resume/actor52_interaction_group2.png`
- `rebuild_game/build/smoke_phase8_bunny_return_resume/route_elder_after_battle_reward_state.png`
- `rebuild_game/build/smoke_phase8_bunny_return_resume/route_sophie_after_battle_branch.png`

## Classification

| Area | Status | Note |
| --- | --- | --- |
| room1 group0 Bunny -> room0 transition trace | PORTED/PARTIAL | Source-shaped trace/assert bridge only. |
| actor transition behavior | UNCHANGED / PORTED/APPROX | Existing trigger still performs transition. |
| `Scene.eventIndex` mutation by descriptor | NOT MUTATED | Descriptor asserts this locally. |
| room0 group2 actor 52 downstream | REGRESSION PASS | `actor52_interaction_group2` smoke passes. |
| generic decoded event VM | PENDING | Still intentionally not attempted. |

## Next Roadmap Step

Next safe Phase 8 slice:

```text
Add trace-only WorldResumeDescriptor coverage for room0 group6 Elder complete
-> post-group6 free-world loop.
```

Scope:

- wrap `Room0PostGroup6FreeWorld`;
- assert room0 group6 `[1,0,6]` complete;
- assert next blocker type remains `Room0PostGroup6FreeWorld`;
- no behavior change, no `eventIndex` mutation, no generic event VM.
