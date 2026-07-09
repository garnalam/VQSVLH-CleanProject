# 144 Battle Phase 8 Op47 Consumer Wrapper Audit

Status: SOURCE-FIRST AUDIT + WRAPPER SLICE IMPLEMENTED.

Purpose:

- Audit the smallest safe slice for a decoded-event `op47` consumer wrapper.
- Keep existing battle runtime behavior unchanged.
- Do not reopen P7.
- Do not genericize the whole event VM.

This follows `143_battle_phase8_entry_exit_event_integration_audit.md`, where
Bunny and Elder were moved to a source-backed `BattleEventDescriptor` for
`op37/op32/op47` data.

## Source Files Read

Primary source:

- `modules/source_code/decoded/decompiled_source_cfr/game/c.java`
  - `public byte l = 0`
  - `case 47`: `if (this.l == -1) break; record.jump = args[this.l] - 2`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
  - P8 win writes `game.k.a().M.l = 0`
  - P9 lose writes `game.k.a().M.l = 1`
  - P17 catch success writes `game.k.a().M.l = -1`
- `modules/event/decoded/data__event__scene_1.mid.json`
  - scene 1 room0 group6, room1 group0, room3 group0 battle records.

Current rebuild:

- `rebuild_game/src/main/java/VqsvBattleEventDescriptor.java`
- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleScripts.java`
- `rebuild_game/src/main/java/Scene1Room1BunnyScript.java`
- `rebuild_game/src/main/java/Scene1Room0Group6ElderBattleScript.java`
- `rebuild_game/src/main/java/Scene1Room3EntryScript.java`
- `rebuild_game/src/main/java/VqsvIntroDemo.java`
- `rebuild_game/src/main/java/VqsvScriptBlocks.java`

## Source Op47 Facts

Source `game.c case 47`:

```text
if (this.l == -1) break;
eventCursor = op47Args[this.l] - 2;
```

Battle result owner:

| Source path | Source write | Meaning |
| --- | --- | --- |
| P8 win | `game.k.a().M.l = 0` | Result index 0. |
| P9 lose | `game.k.a().M.l = 1` | Result index 1. |
| P17 catch success | `game.k.a().M.l = -1` | Skip op47 branch and continue success path. |

Important naming distinction:

| Term | Meaning | Example Elder |
| --- | --- | --- |
| `rawTarget` / logical target | The short arg stored in the event record. Existing rebuild `battleBranchTarget` uses this. | `10` |
| `sourceCursor` | The value passed to source event cursor setter: `rawTarget - 2`. | `8` |

Do not change existing `battleBranchTarget` from raw target to `rawTarget - 2`
without a larger event-runner refactor. Current route smoke expects raw target:

- Bunny catch success: result `-1`, branch `-1`.
- Elder win: result `0`, branch `10`.
- Sophie loss: result `0`, branch `78`.

## Decoded Event Records

Current target routes:

| Route | op47 record index | op47 args | Source result | Source op47 effect | Current rebuild |
| --- | ---: | --- | ---: | --- | --- |
| scene1 room1 group0 Bunny | 5 | `[12,0,0]` | `-1` | Skip branch, continue to record 6. | `battleResultIndex=-1`, `battleBranchTarget=-1`, manual success path continues. |
| scene1 room0 group6 Elder | 8 | `[10,10,0]` | `0` | Cursor set to `10 - 2 = 8`; event-loop advances to logical target 10 reward path. | `battleResultIndex=0`, `battleBranchTarget=10`, manual reward path continues. |
| scene1 room3 group0 Sophie | 76 | `[78,78,0]` | `0` | Cursor set to `78 - 2 = 76`; event-loop advances to logical target 78 post-battle dialog. | `battleResultIndex=0`, `battleBranchTarget=78`, manual script continues. |

Pending later routes also use the same shape, but they are not part of this
slice.

## Current Rebuild Gap

Current runtime already computes:

```text
s.battleResultIndex
s.battleBranchTarget = descriptor/op47Targets[resultIndex]
```

But route scripts continue manually:

- Bunny script calls battle, then directly appends dialog/op56/op23/task/op14.
- Elder script calls battle, then directly appends reward/free-world records.
- Sophie script calls battle, then directly appends post-loss records.

So the gap is not battle result computation. The gap is that no code named as an
event opcode consumer executes the source `op47` rule after battle returns.

Classification:

```text
Result value l: PORTED/PARTIAL.
Raw op47 branch target: PORTED/PARTIAL.
Source event cursor target rawTarget - 2: PENDING.
Generic event VM branch execution: PENDING.
Small op47 wrapper trace/assert: NEXT SAFE SLICE.
```

## Smallest Safe Wrapper

Recommended next code slice:

```text
Add a tiny SourceOp47BattleResult wrapper that consumes the last battle result
and descriptor op47 args, computes both rawTarget and sourceCursor, traces them,
and asserts current rebuild branch target remains consistent.
```

Proposed behavior:

| Result | Wrapper behavior |
| --- | --- |
| `battleResultIndex == -1` | Trace `op47 skip`, require/leave `battleBranchTarget == -1`, return immediately. |
| `0 <= battleResultIndex < op47Args.length` | Compute `rawTarget = op47Args[result]`, `sourceCursor = rawTarget - 2`, trace both, assert `battleBranchTarget == rawTarget`, return immediately. |
| `battleResultIndex >= op47Args.length` | Match current runtime clamp policy only if source evidence requires it; otherwise mark `UNKNOWN/PENDING` and fail smoke for current descriptor misuse. |

Where to put it:

- Best current home: `VqsvBattleEventDescriptor`.
- Add a method like `consumeOp47(VqsvIntroDemo.Scene s)`.
- It should not change `s.eventIndex` in the first slice.
- It should not mutate `battleBranchTarget` in the first slice.

Where to call it:

| Route | Call position |
| --- | --- |
| Bunny room1 group0 | Immediately after `VqsvBattleScripts::room1BunnyBattleCaptureRuntime`, before post-battle dialog. |
| Elder room0 group6 | Immediately after `room0Group6ElderBattleRuntime`, before reward dialog. |
| Sophie room3 group0 | Regression only for now unless descriptor is extended first. |

Why no `eventIndex` jump yet:

- Rebuild `Scene` event runner is a linear `List<Event>` with
  `events.get(eventIndex++).start(...)`.
- Source event cursor writes `args[l] - 2`, then the original VM advances under
  its own loop rules.
- Rebuild Java scripts are already manually ordered on the success route.
- Mutating `eventIndex` now would require a full mapping from decoded record
  indices to Java list indices for each script. That is a larger event VM slice,
  not this wrapper.

## Smoke Plan

Focused PNG/headless smoke after code:

| Checkpoint | Required assertion |
| --- | --- |
| `route_bunny_after_battle_task` | Trace includes `op47 skip result=-1`; branch remains `-1`; task text still visible. |
| `route_elder_after_battle_reward_state` | Trace includes `op47 result=0 rawTarget=10 sourceCursor=8`; reward/free-world text still visible. |
| `route_sophie_after_battle_branch` | Regression only; branch remains `78`. If Sophie descriptor is added in same slice, trace includes `rawTarget=78 sourceCursor=76`. |

Verification after code:

- `build.ps1`
- `com.vqsv.rebuild.Main --check`
- `VqsvBattleDamageFormulaCheck`
- Java mojibake scan
- `git diff --check`
- focused PNG smoke above

## Do Not Do In This Slice

- Do not change `SourceBattleRuntime.resolveBranch()` semantics.
- Do not change `battleBranchTarget` to `rawTarget - 2`.
- Do not mutate `Scene.eventIndex`.
- Do not port pending battle groups.
- Do not reopen P7.
- Do not create a full decoded event VM.

## Next Code Target

```text
Implement SourceOp47BattleResult trace/assert wrapper for Bunny and Elder only.
Keep route behavior unchanged.
Sophie remains regression unless descriptor coverage is explicitly extended.
```

## Wrapper Slice Result

Implemented:

| File | Change | Status |
| --- | --- | --- |
| `rebuild_game/src/main/java/VqsvBattleEventDescriptor.java` | Added `consumeOp47(Scene)` wrapper. It consumes `battleResultIndex`, computes `rawTarget` and `sourceCursor = rawTarget - 2`, asserts `battleBranchTarget == rawTarget`, and traces source-shaped behavior. `result=-1` traces skip and requires branch `-1`. | PORTED/PARTIAL |
| `rebuild_game/src/main/java/Scene1Room1BunnyScript.java` | Calls Bunny descriptor `consumeOp47` immediately after Bunny battle, before post-battle dialog. | PORTED/PARTIAL |
| `rebuild_game/src/main/java/Scene1Room0Group6ElderBattleScript.java` | Calls Elder descriptor `consumeOp47` immediately after Elder battle, before reward dialog. | PORTED/PARTIAL |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Focused Bunny/Elder route smokes now call/assert op47 wrapper trace. Sophie remains regression only. | PORTED/PARTIAL |

Behavior intentionally unchanged:

- `Scene.eventIndex` is not mutated.
- `battleBranchTarget` remains raw logical target (`10`, `78`) and is not
  changed to source cursor (`8`, `76`).
- `SourceBattleRuntime.resolveBranch()` is unchanged.
- No P7 code changed.
- No full decoded event VM created.

Verification after wrapper slice:

| Check | Status |
| --- | --- |
| `build.ps1` | PASS |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| Java mojibake scan | PASS, no matches |
| `git diff --check` targeted files | PASS, CRLF warnings only |
| `route_bunny_after_battle_task` | PASS, op47 skip wrapper asserted |
| `route_elder_after_battle_reward_state` | PASS, `rawTarget=10`, `sourceCursor=8` wrapper asserted |
| `route_sophie_after_battle_branch` | PASS regression |

Smoke PNG output:

- `rebuild_game/build/smoke_phase8_op47/route_bunny_after_battle_task.png`
- `rebuild_game/build/smoke_phase8_op47/route_elder_after_battle_reward_state.png`
- `rebuild_game/build/smoke_phase8_op47/route_sophie_after_battle_branch.png`

## Next Phase 8 Candidate

```text
Audit extending descriptor/op47 coverage to Sophie room3 group0, then decide
whether to call consumeOp47 there as a source-backed route instead of regression
only.
```
