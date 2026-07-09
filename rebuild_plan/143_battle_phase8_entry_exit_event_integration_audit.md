# 143 Battle Phase 8 Entry Exit Event Integration Audit

Status: SOURCE-FIRST AUDIT + FIRST BRIDGE SLICE IMPLEMENTED.

Purpose:

- Start Phase 8 after P7 closeout.
- Trace how source battle starts from event opcodes.
- Trace how source battle exits through result states.
- Trace which event opcode consumes battle result.
- Compare current rebuild integration gaps.
- Select the smallest safe patch candidate after audit.

Do not reopen P7 from this document. P7 remains closed for current routes at
`PORTED/PARTIAL` per `142_battle_p7_phase6_closeout_and_next_phase.md`.

## Source Files Read

Primary source:

- `modules/source_code/decoded/decompiled_source_cfr/game/c.java`
  - opcode runtime around `case 32`, `case 37`, `case 47`, `case 67`.
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
  - battle init `d()`;
  - result state entry/update for P8/P9/P22/P23;
  - EXP commit helper `X()`;
  - win/loss transitions to world state `10`.
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
  - P8/P22/P23 UI consumers `am/an/ao/ap/aq`;
  - `openbox.ui` and `taskTip.ui` helper consumers.
- `modules/source_code/decoded/decompiled_source_cfr/game/i.java`
  - app state switch `case 12` battle shell and return to state `10`.
- `modules/event/decoded/data__event__scene_1.mid.json`
  - concrete scene 1 battle event records.

Current rebuild files read:

- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleScripts.java`
- `rebuild_game/src/main/java/Scene1Room1BunnyScript.java`
- `rebuild_game/src/main/java/Scene1Room0Group6ElderBattleScript.java`
- `rebuild_game/src/main/java/Scene1Room3EntryScript.java`

Related prior docs:

- `74_battle_game_d_state_full_matrix.md`
- `122_battle_p8_p22_p23_exp_ui_closeout_matrix.md`
- `142_battle_p7_phase6_closeout_and_next_phase.md`

## Source Battle Entry Chain

Source event-side chain:

| Step | Source evidence | Meaning | Status in rebuild |
| --- | --- | --- | --- |
| Enemy actor hint | `game.c case 67`: `game.k.v = args[0]` | Stores battle actor/enemy id for later world/battle use. It does not start battle by itself. | `op67SetBattleActor` exists. PORTED/PARTIAL. |
| Enemy setup | `game.c case 37`: `game.d.a().a(new int[][]{{species, level, nature}})` | Adds source encounter row(s) before battle. | Rebuild passes `encounter` array into `SourceBattleRuntime`. PORTED/PARTIAL. |
| Battle entry | `game.c case 32`: sets `game.d.a().a`, `game.d.a().b`, creates screenshot `game.d.a().c`, draws current world, sets player mode, then `game.i.a().a((byte)12)`. | This is the real event opcode that enters battle. It captures world background and transfers app state to battle state 12. | Rebuild has `VqsvBattleScripts` and `BattleEntryTransitionThenRuntime`, but still mostly script-wrapped. PORTED/PARTIAL/APPROX by route. |
| App state shell | `game.i case 12`: creates `game.d.a()`, calls `d()`, then starts transition `b.a().c(...)`; after transition calls `game.d.g()` and app state 13. | Source battle is not just an event blocking; it becomes app state 12/13 until battle exits. | Rebuild keeps battle as `Blocking` inside Scene event runner. APPROX. |
| Battle init | `game.d.d()` loads `pos.mid`, `cpos.mid`, `effect.mid`, `speffect.mid`, `blood.mid`, `bufDebuf.mid`; creates actors/markers; copies player pets; starts P0. | This loads battle resources and initializes battle actors. | Runtime loads/uses many tables, but not through exact global app state. PORTED/PARTIAL. |

Important correction for future work:

- `op67` is not a battle runner.
- `op32` is the battle runner entry opcode.
- `op47` is the battle result consumer after battle returns to event script.

## Source Battle Exit Chain

Source internal battle states:

| State | Source behavior | Return path |
| --- | --- | --- |
| P8 win/EXP | `game.d case 8` enters result/EXP; `game.h.am()` animates EXP for `game.d.j`. May enter P22/P23. | When EXP queue is complete, source calls `game.i.a().a((byte)10)`. |
| P22 level up | `game.d case 22` -> `game.h.an()` opens `levelUp.ui`. | `game.h.ao()` waits/confirm, then P23 or P8/world. |
| P23 learn skill | `game.d case 23` -> `game.h.ap()` opens `choiceskill.ui`. | `game.h.aq()` learns skill, then P8 or world state 10. |
| P9 lose | `game.d case 9` either goes revive/P24 or restores pets and calls `game.i.a().a((byte)10)`. | Returns to world state 10 after lose/revive logic. |
| P17 catch success | `game.d P17` storage result eventually sets `game.k.a().M.l = -1`, calls `l()`, then `game.i.a().a((byte)10)`. | Returns to world state 10 after catch success message chain. |

Source world return:

- `game.i.a((byte)10)` re-enters `game.k` world.
- Event script then resumes after the battle blocking opcode.
- The next event record can consume result through `op47`.

Current rebuild:

- `SourceBattleRuntime.tickWin()` sets `s.battleResultIndex`, persists active pet,
  runs current P8/P22/P23 slices, then enters `EXIT_FADE`.
- `SourceBattleRuntime.tickLose()` sets `s.battleResultIndex`, persists active pet,
  then enters `EXIT_FADE`.
- `SourceBattleRuntime.tickExit()` clears battle overlay/snapshot and returns `DONE`.
- The surrounding Java script continues to the next scripted event.

Classification:

```text
Battle-internal P8/P22/P23/P9: PORTED/PARTIAL for current routes.
World/app state return game.i state 12/13/10: APPROX.
Generic event consumer op47: APPROX/PENDING.
```

## Source Event Result Consumer

Source `game.c case 47`:

```text
if (this.l == -1) break;
record.jump = args[this.l] - 2;
```

Meaning:

- Source stores battle/event result in `this.l`.
- `op47` uses that result as an index into its short args.
- If `this.l == -1`, the branch is skipped.
- The args are event record targets, offset by `-2`.

Current rebuild:

- `SourceBattleRuntime` stores:
  - `s.battleResultIndex`
  - `s.battleBranchTarget = resolveBranch(index)`
- `branchTargets` are passed manually per battle constructor.
- Route scripts continue manually after battle; there is no generic opcode 47 VM
  resuming from decoded event record data.

Status:

```text
op47 result mapping: PORTED/PARTIAL at runtime field level.
op47 decoded-record consumer: APPROX/PENDING.
```

## Scene 1 Battle Event Records

Extracted from `modules/event/decoded/data__event__scene_1.mid.json`:

| Room | Group | Source records | Current route status |
| --- | --- | --- | --- |
| room0 group6 | `op67 52`, `op37 68,5,1`, `op32 0,2`, `op47 10,10,0` | Elder battle. Rebuild has manual script + runtime. PORTED/PARTIAL. |
| room1 group0 | `op37 34,5,1`, `op32 0,0`, `op47 12,0,0` | Bunny capture route. Rebuild has manual script + runtime. PORTED/PARTIAL. |
| room3 group0 | `op67 56`, `op37 5,20,4`, `op32 0,2`, `op47 78,78,0` | Sophie kidnapping battle. Rebuild has direct `SourceBattleRuntime`; op32/op47 wrapper weaker than Bunny/Elder. APPROX/PARTIAL. |
| room1 group2 | `op37 54,6,1`, `op32 0,2`, `op47 6,6,0` | Not current route-smoked. PENDING. |
| room2 group1 | `op37 54,5,1`, `op32 0,2`, `op47 6,6,0` | Not current route-smoked. PENDING. |
| room2 group2 | `op37 0,6,1`, `op32 0,2`, `op47 6,6,0` | Not current route-smoked. PENDING. |
| room3 group5 | `op37 62,6,1`, `op32 0,2`, `op47 6,6,0` | Not current route-smoked. PENDING. |
| room4 group0 | `op67 53`, `op37 68,6,2`, `op32 0,2`, `op47 9,9,0` | Not current route-smoked. PENDING. |
| room6 group0 | `op37 16,7,1`, `op32 0,2`, `op47 6,6,0` | Not current route-smoked. PENDING. |
| room6 group1 | `op37 26,7,1`, `op32 0,2`, `op47 6,6,0` | Not current route-smoked. PENDING. |

Phase 8 implication:

- We should not add new battle routes by copying Java script shape.
- First create a small source-backed battle event descriptor/consumer that
  records `op37/op32/op47` exactly from decoded event data for the current
  route, then expand to pending groups.

## Task / Save / Free-World Return Consumers

Source event/UI consumers adjacent to battles:

| Consumer | Source evidence | Meaning | Current rebuild status |
| --- | --- | --- | --- |
| Task notice | `game.c case 40/45` uses `game.h.c(...)` and `/data/ui/taskTip.ui`. | Shows task notice after battle/event. | Rebuild has `TextBox.taskTip` and source-shaped renderer. PORTED/PARTIAL. |
| Reward/openbox | `game.c case 31/36/40` and `game.h` openbox helpers use `/data/ui/openbox.ui`. | Currency/item/pet/reward notices after battle. | Rebuild has source-backed partial openbox/taskTip. PORTED/PARTIAL. |
| Save prompt | `game.c case 46` shows save option, calls save path, writes event-state byte on success. | Source save is an event opcode consumer, not battle-internal. | Rebuild has save prompt/runtime for Bunny path. PORTED/PARTIAL. |
| Event completion | Source op14/op23-like event state writes around route scripts. | Marks event group complete and gates later free-world triggers. | Rebuild has manual event-state helpers. PORTED/PARTIAL/APPROX. |
| Free-world return | `game.i state 10` returns to `game.k`; subsequent event records continue or free-world blockers run. | Battle does not itself decide all world movement; event script does. | Rebuild uses `Blocking` continuation in Scene. APPROX. |

## Gap Matrix

| Area | Status | Gap |
| --- | --- | --- |
| `op37` encounter setup | PORTED/PARTIAL | Runtime accepts encounter arrays, but no generic decoded-event bridge yet. |
| `op67` actor id | PORTED/PARTIAL | Wrapper exists. Need tie to decoded event descriptor instead of route hardcoding. |
| `op32` app-state entry | PORTED/PARTIAL/APPROX | Battle starts, background snapshot exists, but source `game.i` state 12/13 shell is approximated by Scene `Blocking`. |
| `op47` branch consumer | APPROX/PENDING | Runtime computes result/branch, but route scripts do not execute a decoded opcode 47 consumer. |
| Battle result index `l` parity | PARTIAL | `s.battleResultIndex` exists, but source owner/semantics of `this.l` should be traced per event VM before genericizing. |
| P8/P22/P23 result UI | PORTED/PARTIAL | Current EXP/level-up/learn-skill route behavior smoke-covered; exact generic widget runtime still partial. |
| P9 lose/revive | PARTIAL/PENDING | Basic lose result exists; P24 revive/payment flow is not fully ported. |
| Catch success exit | PORTED/PARTIAL | P17 storage/openbox path exists for current Bunny route; exact route result/index semantics need op47 bridge. |
| Task/openbox/save after battle | PORTED/PARTIAL | Implemented by manual scripts, not fully decoded-event-driven. |
| Pending battle event groups | PENDING | Need source route audit before enabling. |

## Smallest Safe Patch Candidate

Recommended next patch:

```text
Create a source-backed BattleEventDescriptor / result bridge for current
Scene 1 battle scripts, starting with room1 group0 Bunny and room0 group6 Elder.
```

Minimum scope:

1. Add a tiny helper that records source op37/op32/op47 data:
   - actor id from op67 when present;
   - encounter from op37;
   - battle mode from op32;
   - branch targets from op47.
2. Replace only the duplicated manual trace/constructor data in
   `VqsvBattleScripts.room1BunnyBattleCaptureRuntime` and
   `VqsvBattleScripts.room0Group6ElderBattleRuntime`.
3. Keep `SourceBattleRuntime` behavior unchanged.
4. Add/keep PNG smoke:
   - `room1_bunny_op32_flash_transition`
   - `route_bunny_after_battle_task`
   - `route_elder_after_battle_reward_state`
   - `route_sophie_after_battle_branch` as regression only.

Why this slice first:

- It directly addresses Phase 8 entry/exit/event integration.
- It does not touch P7.
- It reduces hardcoded drift between decoded event records and runtime.
- It creates a safe bridge before enabling pending battle groups.

Do not do yet:

- Do not genericize the whole event VM.
- Do not port pending battle groups.
- Do not reopen P7 visual timing.
- Do not claim exact `game.i` app-state parity.

## Done Criteria For Next Slice

The next code slice is acceptable only if:

- Bunny and Elder battle constructor data are sourced from one descriptor table
  that matches decoded event records above.
- `s.battleResultIndex` and `s.battleBranchTarget` remain visible in trace.
- Route smoke still passes:
  - Bunny after battle task;
  - Elder after battle reward/free-world state;
  - Sophie after battle branch regression.
- Build/check/formula/mojibake smoke pass after code.

## Bridge Slice Result

Implemented first Phase 8 bridge slice:

| File | Change | Status |
| --- | --- | --- |
| `rebuild_game/src/main/java/VqsvBattleEventDescriptor.java` | Added source-backed descriptors for scene1 room1 group0 Bunny and scene1 room0 group6 Elder. Descriptor stores op67 actor id, op37 encounter, op32 mode, op47 branch targets, battle flags, and source-slice marker. | PORTED/PARTIAL |
| `rebuild_game/src/main/java/VqsvBattleScripts.java` | Bunny and Elder route battle creation now reads constructor data from descriptor instead of local hardcoded op37/op32/op47 arrays. Bunny still keeps its pre-entry flash wrapper. Elder behavior remains direct runtime as before. | PORTED/PARTIAL |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Focused Bunny/Elder route smokes now assert descriptor trace is actually used. Sophie remains direct and is regression only. | PORTED/PARTIAL |

Behavior intentionally unchanged:

- `SourceBattleRuntime` state machine was not changed.
- P7 was not touched.
- The whole event VM was not genericized.
- Pending battle groups remain PENDING.

Verification after bridge slice:

| Check | Status |
| --- | --- |
| `build.ps1` | PASS |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| Java mojibake scan | PASS, no matches |
| `git diff --check` targeted files | PASS, CRLF warnings only |
| `room1_bunny_op32_flash_transition` | PASS |
| `route_bunny_after_battle_task` | PASS |
| `route_elder_after_battle_reward_state` | PASS |
| `route_sophie_after_battle_branch` | PASS |

Smoke PNG output:

- `rebuild_game/build/smoke_phase8_bridge/room1_bunny_op32_flash_transition.png`
- `rebuild_game/build/smoke_phase8_bridge/route_bunny_after_battle_task.png`
- `rebuild_game/build/smoke_phase8_bridge/route_elder_after_battle_reward_state.png`
- `rebuild_game/build/smoke_phase8_bridge/route_sophie_after_battle_branch.png`

## Phase 8 Current Position

```text
Phase 8 status: FIRST BRIDGE SLICE IMPLEMENTED.
Battle entry/exit current routes: PORTED/PARTIAL.
Generic event result consumer: APPROX/PENDING.
Next code target: decide whether to extend descriptor coverage to Sophie
room3 group0 or implement a tiny decoded-event op47 consumer wrapper.
```
