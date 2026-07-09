# 151 Battle Phase 8 World Resume Descriptor Closeout

Status date: 2026-07-09

Status: CODE SLICE COMPLETE / TRACE-ONLY / NO BEHAVIOR CHANGE INTENDED.

Purpose:

- Close the first safe code slice recommended by
  `150_battle_phase8_state10_freeworld_resume_audit.md`.
- Add a source-shaped trace bridge for source `game.i` state `10` ->
  `game.k.p()` -> world `P=0` resume before room1 group0 `op13`.
- Keep current manual script behavior unchanged.

## Files Changed

Java:

- `rebuild_game/src/main/java/VqsvWorldResumeDescriptor.java`
- `rebuild_game/src/main/java/Scene1Room0Group0Script.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Docs:

- `rebuild_plan/151_battle_phase8_world_resume_descriptor_closeout.md`

## Implemented

`VqsvWorldResumeDescriptor.SCENE1_ROOM1_AFTER_SAVE_TO_OP13`:

- asserts current room is `scene=1, room=1`;
- asserts room1 group1 save wrapper has completed source event `[1,1,1]`;
- asserts the next manual blocker remains `Op13FreeWorldTrigger`;
- traces source resume concept:
  - `game.i state10->11`
  - `game.k.p()->P0`
- checks the descriptor itself does not mutate `Scene.eventIndex`;
- delegates to the original `Op13FreeWorldTrigger`.

The script keeps the same event count at this boundary:

```text
VqsvRoom1Group1SavePromptWrapper
-> WorldResumeDescriptor wrapper
-> Op13FreeWorldTrigger(1,1,0,370,176,80,32)
```

## Verification

Passed:

- `powershell -ExecutionPolicy Bypass -File .\build.ps1`
- `java -cp .\build\classes com.vqsv.rebuild.Main --check`
- `java -cp .\build\classes VqsvBattleDamageFormulaCheck`

Focused smoke PNG:

- `rebuild_game/build/smoke_phase8_world_resume/room1_bunny_save_success.png`
- `rebuild_game/build/smoke_phase8_world_resume/room1_op13_bunny_trigger.png`
- `rebuild_game/build/smoke_phase8_world_resume/route_bunny_after_battle_task.png`
- `rebuild_game/build/smoke_phase8_world_resume/route_elder_after_battle_reward_state.png`
- `rebuild_game/build/smoke_phase8_world_resume/route_sophie_after_battle_branch.png`

Mojibake scan:

- No new literal text was added by this slice.
- Existing hits are helper/method names such as `decodeMojibake`.

## Classification

| Area | Status | Note |
| --- | --- | --- |
| room1 after-save world resume trace | PORTED/PARTIAL | Source-shaped trace/assert bridge only. |
| room1 group0 op13 behavior | UNCHANGED / PORTED/APPROX | Original trigger still handles free-world rectangle trigger. |
| `Scene.eventIndex` mutation by descriptor | NOT MUTATED | Descriptor asserts this locally. |
| generic decoded event VM | PENDING | Not attempted in this slice. |
| full `game.i/game.k/game.c` runtime | PENDING | Still manual rebuild `Blocking` architecture. |

## Next Roadmap Step

Next safe Phase 8 slice:

```text
Add the same trace-only WorldResumeDescriptor coverage for the next current
manual resume point: room1 group0 Bunny complete -> actor transition back to
room0.
```

Scope:

- wrap `ActorTransitionFreeWorldTrigger(1,1,37,3,1,0,30)`;
- assert room1 group0 `[1,1,0]` complete;
- assert next blocker type remains `ActorTransitionFreeWorldTrigger`;
- no behavior change, no `eventIndex` mutation, no generic event VM.
