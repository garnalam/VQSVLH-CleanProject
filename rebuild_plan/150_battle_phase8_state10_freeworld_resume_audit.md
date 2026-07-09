# 150 Battle Phase 8 State10 Free-World Resume Audit

Status date: 2026-07-09

Status: SOURCE-FIRST AUDIT ONLY / NO CODE CHANGE.

Purpose:

- Audit how source returns from battle/save/task UI to free-world and resumes
  event processing.
- Compare source `game.i` state `10` + `game.k` state `0` + `game.c` event
  runner against current rebuild `Blocking` continuation.
- Decide the next safe Phase 8 slice before any generic decoded event VM.

Rules:

- Do not reopen P7.
- Do not change battle branch/op47 semantics.
- Do not mutate `Scene.eventIndex`.
- Do not create a generic decoded event VM in this audit.
- Do not open the live client.

This follows:

- `147_new_dev_chat_handoff_battle_phase8_event_integration.md`
- `148_battle_phase8_room1_group1_save_prompt_wrapper_audit.md`
- `149_battle_phase8_room1_group1_save_prompt_wrapper_closeout.md`

## Source Files Read

Primary source:

- `modules/source_code/decoded/decompiled_source_cfr/game/i.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/c.java`

Current rebuild:

- `rebuild_game/src/main/java/VqsvScriptBlocks.java`
- `rebuild_game/src/main/java/Scene1Room0Group0Script.java`
- `rebuild_game/src/main/java/Scene1Room1BunnyScript.java`
- `rebuild_game/src/main/java/Scene1Room0Group2ElderScript.java`
- `rebuild_game/src/main/java/Scene1Room0Group6ElderBattleScript.java`
- `rebuild_game/src/main/java/VqsvBattleEventDescriptor.java`
- `rebuild_game/src/main/java/VqsvPostBattleDownstreamDescriptor.java`
- `rebuild_game/src/main/java/VqsvRoom1Group1SavePromptWrapper.java`

## Source App-State Chain

### `game.i` state 10

Source `game.i.b()` case `10`:

```text
this.f();
this.m = game.k.a();
((k)this.m).p();
this.a(this.m);
this.a((byte)11);
```

Meaning:

| Step | Source meaning | Rebuild equivalent |
| --- | --- | --- |
| `this.f()` | clears previous active sub-state/module resources. | Battle `EXIT_FADE` clears battle overlay/runtime. `PORTED/PARTIAL`. |
| `this.m = game.k.a()` | selects singleton world runtime. | Rebuild keeps same `Scene` object and current room/map. `APPROX/PARTIAL`. |
| `((k)m).p()` | rebinds event runner/UI to world and enters world state `0`. | Rebuild continues Java `Blocking` list. No real `game.k.p()` shell. `APPROX`. |
| `this.a(m)` | installs world module into app shell. | Rebuild has no separate app shell module swap. `APPROX`. |
| `this.a((byte)11)` | state 11 delegates updates/draw to current module. | Rebuild render/tick remains inside `VqsvIntroDemo.Scene`. `APPROX`. |

Source `game.i.b()` cases `11/13/20`:

```text
if (this.m != null) this.m.b();
```

Meaning:

- After app state 10 installs world, state 11 calls `game.k.b()` every frame.
- Battle exit itself does not execute all post-battle logic; it returns control
  to world/event runtime.

### `game.k.p()`

Source `game.k.p()`:

```text
this.M.a(this);
this.S.a(this);
game.c.f = false;
J = true;
this.a((byte)0);
this.R.a("/data/ui/battle.ui");
return true;
```

Meaning:

| Step | Source meaning | Rebuild equivalent |
| --- | --- | --- |
| `M.a(this)` | event runner `game.c` is bound to current world runtime. | Manual scripts already operate on `Scene`; no dynamic binding. `APPROX`. |
| `S.a(this)` | UI facade bound to current world runtime. | Text/UI objects write directly into `Scene`. `APPROX/PARTIAL`. |
| `game.c.f = false` | event-runner/global flag reset on world resume. | Some source flags exist, but not full `game.c` flag lifecycle. `PARTIAL/PENDING`. |
| `J = true` | world-start flag used by `game.k.a(0)` to choose UI/world setup path. | Not directly represented. `PENDING`. |
| `this.a((byte)0)` | enters world state `P=0`, normal free-world. | Current rebuild free-world blockers call `tickFreeWorldPlayer()`. `APPROX/PARTIAL`. |
| `R.a("/data/ui/battle.ui")` | source world loads/reuses UI resource after battle. | Battle/world UI resources are handled separately in rebuild. `PARTIAL`. |

### `game.k.b()` state 0

Source `game.k.b()` case `0` is the normal free-world update loop.

Important calls in the order seen in source:

| Source call / block | Meaning | Rebuild equivalent |
| --- | --- | --- |
| `!M.h()` checks around input | `M.h()` means event runner has active queued event(s); free-world input is limited while events are active. | Blocking events naturally block input until complete. `PORTED/PARTIAL` by architecture. |
| movement / confirm input checks | player movement, actor interaction, menu/map/panel input | `tickFreeWorldPlayer()`, `ActorInteractionFreeWorldTrigger`, `ActorTransitionFreeWorldTrigger`. `APPROX/PARTIAL`. |
| `M.c()` | event runner per-frame update/animations. | No generic event runner; individual blockers update themselves. `PENDING/APPROX`. |
| `S.e()` | UI facade per-frame update. | Text/choice/save prompt tick in current blockers/render path. `PORTED/PARTIAL`. |
| `if (!S.j()) M.b()` | if no blocking UI, execute/advance event runner. | Rebuild event list advances when current `Blocking` returns true. `APPROX/PARTIAL`. |
| `l()` | world trailing update after event runner. | Not generically mapped. `PENDING`. |

Key source fact:

```text
Source free-world resume is not "continue Java list only".
It re-enters game.k, then game.k.P=0 drives input, UI, and game.c event runner
every frame.
```

## Source Event Runner Mechanics

### `game.c.b()`

Source `game.c.b()` has two relevant responsibilities:

1. Scan event records that are idle/eligible.
2. When a record condition passes, put the record/group into the active queue
   `z` and mark the record state active.

Examples of gates in the scan:

| Opcode / condition | Source behavior | Current rebuild mapping |
| --- | --- | --- |
| `op13` | trigger when player rectangle intersects source rect. | `Op13FreeWorldTrigger`. `PORTED/APPROX`. |
| `op15` | gate on event state `3` or `4`. | `op15CheckEventState()` checks complete state `3`. `PORTED/PARTIAL`. |
| `op16` | actor interaction when `game.k.u` matches and confirm occurs. | `ActorInteractionFreeWorldTrigger` uses key0 + source mask. `PORTED/PARTIAL`. |
| `op43/op44` | actor transition/interact with extra state conditions. | Some current routes use `ActorTransitionFreeWorldTrigger`; broad support `PENDING`. |
| `op86` | gate on source event state `3`. | `ActorInteractionFreeWorldTrigger` uses op86 gate for room0 group2. `PORTED/PARTIAL`. |

### `game.c.h()`

Source `game.c.h()` returns true if the event runner has active queued events
whose state is not done/paused.

Current rebuild equivalent:

- A Java `Blocking` object is active in `Scene.current`.
- This prevents the main script list from advancing and usually blocks
  free-world input except where the blocker explicitly calls
  `tickFreeWorldPlayer()`.

Classification:

```text
Active event blocking: PORTED/PARTIAL by architecture.
Exact source queue z/p state semantics: PENDING.
```

### `game.c.c()`

Source `game.c.c()` updates event-runner visuals/effects each frame, such as
floating indicators and active helper sprites.

Current rebuild:

- Some visual/effect blockers update directly.
- No generic event-runner visual queue exists.

Classification:

```text
PENDING for generic source event-runner effects.
Not a blocker for current battle route continuation.
```

## Current Rebuild Free-World Resume Mapping

### Bunny path

Current flow:

```text
Scene1Room0Group0Script
  -> loadScene1Room1(...)
  -> VqsvRoom1Group1SavePromptWrapper
  -> Op13FreeWorldTrigger(1,1,0,370,176,80,32)

Scene1Room1BunnyScript
  -> battle runtime
  -> consumeOp47
  -> manual op4/op56/op23/op40/op14
  -> ActorTransitionFreeWorldTrigger(1,1,37,3,1,0,30)

Scene1Room0Group2ElderScript
  -> ActorInteractionFreeWorldTrigger(1,0,2, gate [1,1,0], actor 52)
```

Mapping:

| Source concept | Rebuild behavior | Status |
| --- | --- | --- |
| app returns to world state 10/11 | Same `Scene` continues; no app-state swap. | `APPROX` |
| room1 group1 save source group | `VqsvRoom1Group1SavePromptWrapper`. | `PORTED/PARTIAL` |
| room1 group0 op13 free-world trigger | `Op13FreeWorldTrigger`. | `PORTED/APPROX` |
| room1 group0 post-battle downstream | Manual script + `VqsvPostBattleDownstreamDescriptor`. | `PORTED/PARTIAL` |
| return transition to room0 | `ActorTransitionFreeWorldTrigger`. | `PORTED/APPROX` |
| room0 group2 elder interaction | `ActorInteractionFreeWorldTrigger`. | `PORTED/PARTIAL` |

### Elder path

Current flow:

```text
Scene1Room0Group6ElderBattleScript
  -> battle runtime
  -> consumeOp47
  -> manual reward/task/free-world op cluster
  -> VqsvPostBattleDownstreamDescriptor trace
  -> Room0PostGroup6FreeWorld
```

`Room0PostGroup6FreeWorld`:

- starts free-world after group6 op40/op14;
- handles current room0 transitions to room2/room1;
- logs pending side quests/door transitions;
- calls `tickFreeWorldPlayer()`.

Mapping:

| Source concept | Rebuild behavior | Status |
| --- | --- | --- |
| app returns to world state 10/11 | Continues Java script then enters `Room0PostGroup6FreeWorld`. | `APPROX/PARTIAL` |
| event runner resumes all room0 groups | Only selected current-route triggers are represented. | `PARTIAL/PENDING` |
| source state 10 free-world input | `tickFreeWorldPlayer()` inside blockers. | `APPROX/PARTIAL` |
| pending room0 side quests/doors | logged as pending. | `PENDING` |

### Sophie path

Current flow:

```text
Scene1Room3EntryScript
  -> battle runtime
  -> consumeOp47
  -> manual post-battle actor/dialog/effect/transition sequence
```

Mapping:

| Source concept | Rebuild behavior | Status |
| --- | --- | --- |
| op47 branch to raw target 78 | descriptor + `consumeOp47`. | `PORTED/PARTIAL` |
| post-battle downstream sequence | manual script. | `PORTED/PARTIAL/APPROX` |
| app/world free-world resume | later manual room0 load/transition. | `APPROX/PARTIAL` |

## Gap Matrix

| Area | Status | Gap |
| --- | --- | --- |
| `game.i` state 10 shell | `APPROX` | Rebuild does not swap app modules through `game.i`; it continues `Scene` script. |
| `game.k.p()` world rebind | `APPROX/PENDING` | No generic rebinding of `game.c`/`game.h`; manual `Scene` owns state. |
| `game.k.P=0` loop | `APPROX/PARTIAL` | `tickFreeWorldPlayer()` covers movement; not full input/menu/world loop. |
| `game.c.b()` scan/queue | `PENDING/PARTIAL` | Current route gates are individual blockers, not a generic record scanner. |
| `game.c.c()` event visual update | `PENDING` | No generic event-runner visual queue. |
| `game.c.h()` active event blocking | `PORTED/PARTIAL` | Java `Blocking` approximates source active event queue. |
| op13/op16/op86 current route gates | `PORTED/PARTIAL/APPROX` | Current routes smoke-covered; broad opcode parity pending. |
| `Room0PostGroup6FreeWorld` | `APPROX/PARTIAL` | Useful current-route free-world loop, but not full source state 10/event VM. |
| generic decoded event VM | `PENDING` | Still intentionally not built. |

## Safe Next Slice Options

Option A: Trace-only world resume descriptor for current routes.

```text
VqsvWorldResumeDescriptor
```

Possible descriptors:

- `SCENE1_ROOM1_AFTER_SAVE_TO_OP13`
  - source app state 10/11 resume to room1 world
  - next source event group: room1 group0 op13
- `SCENE1_ROOM1_AFTER_BUNNY_TO_ROOM0`
  - after room1 group0 op14
  - actor transition to room0 group2 gate
- `SCENE1_ROOM0_AFTER_GROUP6_FREEWORLD`
  - after room0 group6 op14
  - free-world state with room0/room2 transitions

Behavior:

- trace/assert current scene/room;
- trace whether UI is blocking;
- trace next manual blocker type;
- assert `Scene.eventIndex` unchanged;
- do not execute or mutate source event VM.

Status target:

```text
PORTED/PARTIAL trace bridge, behavior unchanged.
```

Option B: Focused source gate descriptor for `Op13FreeWorldTrigger`.

- Data-only descriptor for room1 group0:
  - `op13 [370,176,80,32]`.
- Wrapper can assert current room1, source rect, and trigger trace.
- This is narrower than state10, but directly reduces drift for Bunny route.

Option C: Full generic event VM.

- Not recommended yet.
- Too broad: must map decoded record indices to Java event list, `game.c.z`
  queue, `p.a()` states, UI blocking, source cursor writes, and all gate ops.

## Recommendation

Next code slice should be trace-only and small:

```text
Create a WorldResumeDescriptor trace/assert wrapper for the current three
Phase 8 resume points, starting with room1 after save -> op13.
```

Why:

- It documents source state10/free-world resume without pretending to have a
  full `game.i/game.k/game.c` VM.
- It can be smoke-asserted without changing behavior.
- It creates a safe bridge before any future decoded event VM work.

Do not do next:

- Do not mutate `Scene.eventIndex`.
- Do not replace `Op13FreeWorldTrigger` with a generic scanner yet.
- Do not port pending room battle groups.
- Do not reopen P7.
- Do not rewrite save/RMS.

## Verification Addendum

This addendum records the source line anchors checked after the first draft of
this audit. It does not change the recommendation.

| Source file | Line anchor | Fact |
| --- | --- | --- |
| `modules/source_code/decoded/decompiled_source_cfr/game/i.java` | `284..290` | App state `10` clears the current module, gets `game.k.a()`, calls `k.p()`, installs the module, then switches to app state `11`. |
| `modules/source_code/decoded/decompiled_source_cfr/game/i.java` | `443..451` | App state `10` draws nothing; states `11/13/20` delegate draw to current module. |
| `modules/source_code/decoded/decompiled_source_cfr/game/k.java` | `625..633` | `k.p()` binds UI/runtime pieces, calls `M.i()`, `M.b()`, sets `J = true`, enters world state `0`, then calls `game.k.t()`. |
| `modules/source_code/decoded/decompiled_source_cfr/game/k.java` | `1951..1964` | World state `0` ticks event visuals through `M.c()`, ticks UI through `S.e()`, and only calls `M.b()` when `S.j()` is false. |
| `modules/source_code/decoded/decompiled_source_cfr/game/k.java` | `2214..2216` | Extra queued world notifications are gated by `P == 0` and `!M.h()`, reinforcing that active events block normal free-world consumers. |
| `modules/source_code/decoded/decompiled_source_cfr/game/c.java` | `140..148` | `game.c.b()` starts by scanning eligible event records, not by continuing a single hard-coded Java list. |
| `modules/source_code/decoded/decompiled_source_cfr/game/c.java` | `301..309` | `game.c.c()` updates event helper visuals/effects each frame. |
| `modules/source_code/decoded/decompiled_source_cfr/game/c.java` | `1640..1648` | `game.c.h()` returns true when queued event records are active and not paused/done. |

Important correction from line anchors:

```text
The source `k.p()` body observed in the decompiled file uses `M.i()` and
`M.b()` before entering `P=0`; the earlier high-level wording "M.a(this)" is
only the conceptual binding/readiness role and should not be treated as the
exact line-level call sequence for this source file.
```

## Smoke Plan For A Future Trace Slice

Focused PNG/headless smoke:

| Checkpoint | Required proof |
| --- | --- |
| `room1_bunny_save_success` | trace world resume descriptor after save; save still succeeds. |
| `room1_op13_bunny_trigger` | op13 trigger still fires. |
| `route_bunny_after_battle_task` | Bunny post-battle task still visible; room1 group0 downstream trace still passes. |
| `route_elder_after_battle_reward_state` | Elder free-world resume trace after group6 still passes. |
| `route_sophie_after_battle_branch` | Sophie regression; no branch behavior change. |

Verification:

- `build.ps1`
- `com.vqsv.rebuild.Main --check`
- `VqsvBattleDamageFormulaCheck`
- Java mojibake scan
- targeted whitespace/diff check if a git repo is available

## Current Status

This audit changes documentation only.

Classification:

```text
Source state10/free-world resume understanding: SOURCE-MAPPED.
Current rebuild mapping: APPROX/PARTIAL for route-smoked behavior.
Generic event VM readiness: NOT READY / PENDING.
Recommended next code: trace-only WorldResumeDescriptor, no behavior change.
```
