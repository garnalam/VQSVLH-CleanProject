# 145 Battle Phase 8 Sophie Descriptor Op47 Audit

Status: IMPLEMENTED / VERIFIED.

Purpose:

- Audit whether scene1 room3 group0 Sophie/kidnapping battle should join the
  `BattleEventDescriptor` + `consumeOp47` bridge used by Bunny/Elder.
- Decide the next smallest safe code slice.
- Keep route behavior unchanged.
- Do not reopen P7.
- Do not mutate `Scene.eventIndex`.
- Do not create a full decoded event VM.

This follows:

- `143_battle_phase8_entry_exit_event_integration_audit.md`
- `144_battle_phase8_op47_consumer_wrapper_audit.md`

## Source Files Read

Primary source and data:

- `modules/event/decoded/data__event__scene_1.mid.json`
- `modules/source_code/decoded/decompiled_source_cfr/game/c.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`

Current rebuild:

- `rebuild_game/src/main/java/Scene1Room3EntryScript.java`
- `rebuild_game/src/main/java/VqsvBattleEventDescriptor.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Related docs:

- `41_battle_engine_three_stub_replacement_audit.md`
- `54_battle_entry_snapshot_matrix.md`
- `144_battle_phase8_op47_consumer_wrapper_audit.md`

## Source Record Slice

Decoded scene1 room3 group0 relevant records:

| Record | Opcode | Args | Meaning |
| ---: | ---: | --- | --- |
| 68 | `4` | `-1,0` | Neil dialog before battle. |
| 69 | `7` | `3` | Actor/action setup. |
| 70 | `4` | `-1,0` | Enemy dialog. |
| 71 | `4` | `-1,0` | Enemy dialog. |
| 72 | `67` | `56` | `game.k.v = 56`; battle actor/enemy hint. |
| 73 | `37` | `5,20,4` | Encounter: species 5, level 20, nature 4. |
| 74 | `52` | `1,1` | Battle/capture flags: `this.i=true`, `game.c.j=false` by current interpretation. |
| 75 | `32` | `0,2` | Battle entry mode; source captures world and enters `game.i` state 12. |
| 76 | `47` | `78,78,0` | Result consumer: source uses `l` to select target. |
| 77 | `3` | `1` | Hide/visibility action for actor 50 in current manual port. |
| 78 | `4` | `-1,0` | Post-battle enemy dialog. |
| 79 | `4` | `-1,0` | Sophie dialog. |
| 80 | `4` | `-1,0` | Neil dialog. |
| 81 | `5` | `1,49,6,0,0` | Actor effect. |
| 82 | `12` | `15` | Delay. |

Source battle result facts:

- Sophie route is a forced/expected loss in current rebuild.
- Source P9 lose writes `game.k.a().M.l = 1`, but current route uses
  `forcedResultIndex = 0` and `op47 [78,78,0]`, so result 0 and result 1 both
  select raw target `78`.
- Existing route smoke proves current behavior:
  - `battleResultIndex = 0`
  - `battleBranchTarget = 78`

Because `op47` args are `[78,78,0]`, adding descriptor/consume wrapper can
preserve current behavior while also tracing source cursor:

```text
rawTarget = 78
sourceCursor = 78 - 2 = 76
```

## Current Rebuild State

`Scene1Room3EntryScript` currently hardcodes the battle:

```text
new SourceBattleRuntime(
    56,
    [5,20,4],
    [1,1],
    [0,2],
    [78,78,0])
```

Unlike Bunny/Elder:

- It does not use `VqsvBattleEventDescriptor`.
- It does not call `consumeOp47`.
- Its smoke route remains regression only after `144`.

Current classification:

| Area | Status | Reason |
| --- | --- | --- |
| Sophie op67/op37/op32/op47 data | PORTED/PARTIAL | Correct source arrays are now stored in `SCENE1_ROOM3_GROUP0_SOPHIE`. |
| Sophie descriptor coverage | PORTED/PARTIAL | `VqsvBattleEventDescriptor` now covers scene1 room3 group0 Sophie. |
| Sophie op47 wrapper trace | PORTED/PARTIAL | Script and focused smoke now call/assert `consumeOp47`. |
| Sophie route behavior | PORTED/PARTIAL | Behavior unchanged: result `0`, raw branch target `78`, source cursor trace `76`. |

## Decision

It is safe and roadmap-consistent to extend descriptor/op47 coverage to Sophie
as the next small Phase 8 slice.

Recommended code slice:

1. Add `SCENE1_ROOM3_GROUP0_SOPHIE` to `VqsvBattleEventDescriptor`:
   - label: `scene1 room3 group0 Sophie`
   - scene/room/group: `1,3,0`
   - `op67ActorId = 56`
   - `op37Encounter = [5,20,4]`
   - `battleFlags = [1,1]`
   - `op32Mode = [0,2]`
   - `op47BranchTargets = [78,78,0]`
   - `sourceBattleSlice = false` unless source audit proves it should use the
     Elder source-slice flag.
2. Replace direct `new SourceBattleRuntime(...)` in
   `Scene1Room3EntryScript` with descriptor runtime.
3. Add `consumeOp47` immediately after the Sophie battle in the script.
4. Update focused smoke `route_sophie_after_battle_branch` to assert descriptor
   trace and `op47 result=0 rawTarget=78 sourceCursor=76`.
5. Optionally update Sophie-specific persistence smoke to use descriptor only
   if it is part of the same route proof; otherwise leave internal smoke direct
   to avoid widening the slice.

## Implementation Result

Implemented exactly this slice:

| File | Change | Status |
| --- | --- | --- |
| `rebuild_game/src/main/java/VqsvBattleEventDescriptor.java` | Added `SCENE1_ROOM3_GROUP0_SOPHIE` with source event data: op67 actor `56`, op37 `[5,20,4]`, op52 flags `[1,1]`, op32 `[0,2]`, op47 `[78,78,0]`, `sourceBattleSlice=false`. | PORTED/PARTIAL |
| `rebuild_game/src/main/java/Scene1Room3EntryScript.java` | Replaced direct `new SourceBattleRuntime(...)` with descriptor runtime and calls `consumeOp47` immediately after the Sophie battle. | PORTED/PARTIAL |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | `route_sophie_after_battle_branch` now uses the Sophie descriptor and asserts descriptor/op47 trace: `rawTarget=78`, `sourceCursor=76`. | PORTED/PARTIAL |

Behavior intentionally unchanged:

- `battleBranchTarget` remains raw logical target `78`.
- `Scene.eventIndex` is not mutated.
- `SourceBattleRuntime.resolveBranch()` is unchanged.
- P7 is untouched.
- No full decoded event VM was created.

## Do Not Do

- Do not change `battleBranchTarget` from `78` to `76`.
- Do not mutate `Scene.eventIndex`.
- Do not alter `SourceBattleRuntime.resolveBranch()`.
- Do not touch P7.
- Do not port pending battle groups.
- Do not genericize the event VM.

## Smoke Plan After Code

Focused PNG/headless smoke:

| Checkpoint | Required proof |
| --- | --- |
| `route_sophie_after_battle_branch` | Descriptor trace present; `op47 result=0 rawTarget=78 sourceCursor=76`; branch remains `78`; post-battle dialog visible. |
| `route_bunny_after_battle_task` | Regression: Bunny op47 skip still passes. |
| `route_elder_after_battle_reward_state` | Regression: Elder raw target/source cursor still passes. |

Standard verification:

- `build.ps1`
- `com.vqsv.rebuild.Main --check`
- `VqsvBattleDamageFormulaCheck`
- Java mojibake scan
- `git diff --check`

## Current Recommendation

```text
Sophie descriptor + consumeOp47 slice is complete.
Next Phase 8 work should audit a tiny battle exit/downstream consumer gap
instead of reopening P7 or genericizing the full event VM.
```

## Verification After Code

| Check | Status |
| --- | --- |
| `build.ps1` | PASS |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| Java mojibake scan | PASS, no matches |
| `git diff --check` targeted files | PASS, CRLF warnings only |
| `route_sophie_after_battle_branch` | PASS, descriptor/op47 trace asserted |
| `route_bunny_after_battle_task` | PASS regression |
| `route_elder_after_battle_reward_state` | PASS regression |

Smoke PNG output:

- `rebuild_game/build/smoke_phase8_sophie/route_sophie_after_battle_branch.png`
- `rebuild_game/build/smoke_phase8_sophie/route_bunny_after_battle_task.png`
- `rebuild_game/build/smoke_phase8_sophie/route_elder_after_battle_reward_state.png`
