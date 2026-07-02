# World/Event Opcode Matrix

Nguon code chinh:

- `source_code/decoded/decompiled_source_cfr/game/c.java`
- `source_code/decoded/decompiled_source_cfr/p.java`
- `source_code/decoded/decompiled_source_cfr/ad.java`
- cross refs: `game/k.java`, `game/a.java`, `game/g.java`, `game/h.java`, `game/d.java`, `b.java`, `ai.java`

Trang thai: PARTIAL but actionable.

This file maps the opcode switch in `game.c.n()` plus trigger scan in `game.c.b()`. It is not a final gameplay design document. Some meanings are still named by observed side effect, not by final game-domain name.

Deep audit pass:

- See `11_world_event_opcode_deep_audit.md` for the follow-up audit of branch/condition opcodes, inventory/task/reward opcodes, UI choice/dialog opcodes, world transition opcodes, battle-trigger opcodes, and decoded event resource usage counts.

## Legend

| Status | Meaning |
|---|---|
| `VERIFIED` | Directly read from code and side effect is clear enough to port. |
| `PARTIAL` | Directly read from code, but exact domain name or some params require caller/resource validation. |
| `TRIGGER_ONLY` | Used by trigger scan before VM execution; no executor case in `game.c.n()` observed. |
| `NO_CASE` | Opcode id in `0..88` but no switch case observed in trigger scan or executor. |
| `CFR_RISK` | Code has decompiler damage or ambiguous control flow. |

Event command format:

```text
ad:
  short opcode
  byte totalParamCount
  byte shortParamCount
  short[] b = numeric params
  String[] c = string params resolved from scene string pool
```

Event state:

```text
0 idle
1 active/running
2 conditional/interact state
3 complete/persist complete
4 alternate complete/persist complete
5 command waiting/running
6 blocked/paused
```

Execution rule:

```text
if event state != 5 and != 6:
  p.e() advances command index

if event state == 3 or 4:
  remove from active list
  persist state into game.c.b[roomIndex][eventId]
```

## Trigger Scan Opcodes

These opcodes can appear as the first command of an idle event and decide whether the event enters active Vector `z`.

| Opcode | Trigger condition observed | Status |
|---|---|---|
| `13` | Player rect/collision check using `ae.a(rect..., player position, player hitbox)`. | VERIFIED |
| `15` | Referenced event state in another room is `3` or `4`. | VERIFIED |
| `16` | Current selected actor `game.k.u` equals param actor id; waits for interaction flag `game.c.h`. | VERIFIED |
| `43` | Actor interaction trigger with condition helper `a(ad)`; uses world/room, actor id, selected actor, quest/inventory conditions. | PARTIAL |
| `44` | Actor interaction trigger with condition helper `b(ad)`; similar to `43` but different condition modes. | PARTIAL |
| `57` | Player parent/special actor condition, checks actor position and `game.c.h`. | PARTIAL |
| `59` | Waits until listed actors have state `h()==0`. | VERIFIED |
| `61` | Same actor-state completion style as `59`, also stops player direction. | VERIFIED |
| `69` | Current selected actor equals param actor id. | VERIFIED |
| `73` | Inventory/item quantity condition through `game.g.a(itemId, qty) >= 2`. | PARTIAL |
| `75` | Player/game list `x.L` is non-empty. | PARTIAL |
| `78` | Multiple referenced events across rooms are complete state `3/4`. | VERIFIED |
| `79` | Referenced event is complete, player condition `!x.k(0)`, and selected actor equals param. | PARTIAL |
| `86` | Referenced event in another room is complete state `3`. | VERIFIED |
| default | Any other first opcode falls through as true trigger. | VERIFIED |

## Condition Helper `a(ad)` For Trigger 43

`a(ad)` checks optional prerequisite event state and then condition mode `b()[8]`.

| Mode | Condition observed | Status |
|---|---|---|
| `0` | Player boolean flag `x.T[index]` true. | PARTIAL |
| `1` | Always true. | VERIFIED |
| `2` | Has enough pet/item list capacity and owns item/pet id in active or stored list. | PARTIAL |
| `3` | Player currency/points `x.F >= value`. | PARTIAL |
| `4` | `x.a(byte, int) == 2`, likely has/flag check. | PARTIAL |
| `5` | Static `game.c.t > value`. | PARTIAL |
| `6` | Static `game.c.t == value`. | PARTIAL |

## Condition Helper `b(ad)` For Trigger 44

`b(ad)` also checks optional prerequisite event state and then condition mode `b()[8]`.

| Mode | Condition observed | Status |
|---|---|---|
| `0` | `x.a(byte, int) == 2`. | PARTIAL |
| `1` | Player boolean flag `x.T[index]` true. | PARTIAL |
| `2/4` | Referenced event complete state `3`. | VERIFIED |
| `3` | Player has item in category 0 via `x.b(id, qty, 0)`. | PARTIAL |
| `5` | `x.F >= value`. | PARTIAL |
| `6` | Checks ownership of four required elemental/types across active pets. | PARTIAL |

## Executor Opcode Matrix

| Op | Executor behavior observed | Blocking / advance behavior | Status |
|---|---|---|---|
| `0` | No-op. | Advances normally. | VERIFIED |
| `1` | Start cutscene/dialog text through `game.j D`; starts effect id `9`; waits key `0`/confirm bit. | Sets state `5`, completes after text done and input. | VERIFIED |
| `2` | Set player or actor sprite/animation/state active. `-1` target means player. Actor ids from string list. | Immediate. | VERIFIED |
| `3` | Hide/deactivate player or listed actors. `-1` target means player. | Immediate. | VERIFIED |
| `4` | Show UI/dialog through `game.h S.a(text/title, ..., id)`; waits confirm and text paging. | State `5`, waits `S.c(id,-1)` and confirm. | PARTIAL |
| `5` | Spawn temporary attached sprite effect `f` using sprite 259 and anim param; attach to player or actor/coords; add to `game.c.C`. | Immediate. | VERIFIED |
| `6` | World/room transition: mark current event complete, set `game.k.f/g`, optional target actor `j`, switch `game.i` to state `22`. | Leaves current VM via world reload. | VERIFIED |
| `7` | Move/animate player or actors using target state/position strings; waits until animation completion. | State `5`, waits all targets done. | PARTIAL |
| `8` | Set player position and child sprite position; reset selected actor. | Immediate. | VERIFIED |
| `9` | Gateway to full-screen/overlay `b.java` effect manager. Handles subtypes specially. | State `5`, waits `b.b` or `b.c`, except subtype `16` stop case. | VERIFIED |
| `10` | Temporarily set player/actor animation/action state and duration, then restore default state. | State `5`, waits per target timer. | PARTIAL |
| `11` | Camera move/follow via `ai`: fixed point, player, or actor. Speed from param 7; immediate flag from param 6. | State `5`, waits `ai.a().c()`. | VERIFIED |
| `12` | Delay/wait ticks. | State `5`, waits `b()[0]` ticks. | VERIFIED |
| `13` | Runtime area condition; if player inside rect event continues, else state `6`. | Branches state to `1` or `6`. | VERIFIED |
| `14` | Mark event state `3` complete. | Removed and persisted at loop end. | VERIFIED |
| `15` | No executor case observed; used as trigger opcode. | Trigger-only. | TRIGGER_ONLY |
| `16` | Interaction/selected actor conditional; if selected actor matches, wait for `game.c.h`, then state `2`; else state `6`. | Conditional state change. | VERIFIED |
| `17` | Add/remove item category 0 using `game.g`; shows obtained/lost message. | State `5`, waits message close. | PARTIAL |
| `18` | Add/remove item/category 2 using `game.g` and `aq.c[3]`; shows message. | State `5`, waits message close. | PARTIAL |
| `19` | Add item/reward using `aq.c[5]` and `game.g.d/c/e/i`; handles full bag cases. | State `5`, waits message close. | PARTIAL |
| `20` | Set boolean flag `x.T[index]` and show obtained/lost string. | State `5`, waits message close. | PARTIAL |
| `21` | Set world transition globals `game.k.x=false`, `y/z/A/B/C`. | Immediate. | PARTIAL |
| `22` | Set world transition globals `game.k.x=true`, spawn coords `h/i`, transition params `B/C`, `j=-1`. | Immediate. | PARTIAL |
| `23` | Mark arbitrary event in world/room as state `3`; if same room, update live event and active list. | Immediate. | VERIFIED |
| `24` | Start `b.java` camera/screen shake effect id `11`. | State `5`, waits effect complete. | VERIFIED |
| `25` | Set static flag `game.c.f = (param0 == 0)`. | Immediate. | VERIFIED |
| `26` | No executor/trigger case observed. | None observed. | NO_CASE |
| `27` | No executor/trigger case observed. | None observed. | NO_CASE |
| `28` | No executor/trigger case observed. | None observed. | NO_CASE |
| `29` | Move player or actors by per-tick delta offsets for durations/counters. | State `5`, waits all counters finish. | VERIFIED |
| `30` | Absolute path movement for actors through arrays of x/y positions split by `#` and `,`. | State `5`, advances one path index per tick. | VERIFIED |
| `31` | Add/remove money or badge/medal-like value through `x.s()` / `x.u()`; show message. | State `5`, waits message close. | PARTIAL |
| `32` | Battle entry: capture screen, set `game.d.a().a/b/c`, stop player, switch `game.i` state `12`. | Leaves world VM into battle transition. | VERIFIED |
| `33` | Empty executor case. | No effect. | VERIFIED |
| `34` | Initializes counters `N/O/B`, decrements `B` while changing `N`. Exact consumer of `N` unclear. | State `5`, waits counter. | PARTIAL |
| `35` | UI choice/list prompt through `S.a(...)`; selected choice maps to branch labels in string params. | State `5`, waits UI selection. | VERIFIED/PARTIAL |
| `36` | Add/remove creature/pet-like entity via `game.g.a(...)` / `x.n(...)`; handles full bag/bank/release. | State `5`, waits message close. | PARTIAL |
| `37` | Pass battle/setup tuple into `game.d.a().a(int[][])`. | Immediate. | PARTIAL |
| `38` | Branch on selected actor among string list; clears interaction flags and branches using paired target list. | Sets state `6` while waiting if not matched/confirmed. | VERIFIED/PARTIAL |
| `39` | Calls `I()` on all active player pets/entities `x.z`. | Immediate. | PARTIAL |
| `40` | Show message/string through `S.c(text)` and wait. | State `5`, waits `S.ay()`. | VERIFIED |
| `41` | Unconditional branch to command index `param0 - 2`. | Immediate branch. | VERIFIED |
| `42` | Mark event state `4`. | Removed and persisted at loop end. | VERIFIED |
| `43` | No executor case observed; trigger-only actor condition. | Trigger-only. | TRIGGER_ONLY |
| `44` | No executor case observed; trigger-only actor condition. | Trigger-only. | TRIGGER_ONLY |
| `45` | Show message through `S.c(text)`, set static `game.c.t`, wait. | State `5`, waits `S.ay()`. | VERIFIED |
| `46` | Save prompt flow: show confirm, on confirm call `game.k.k()` save, mark event complete, show save success. | State `5`, multi-stage UI state `S.f`. | VERIFIED/PARTIAL |
| `47` | Branch by `this.l` index into numeric params. | Immediate branch if `l != -1`. | PARTIAL |
| `48` | Text/dialog at explicit x/y/w/h; optional wait flag `b()[5]`. | State `5`, waits text complete/input or returns if non-wait. | VERIFIED |
| `49` | Two-option choice UI with text/options and branch targets; can record quest progress in `game.c.s/u`. | State `5`, waits UI choice. | VERIFIED/PARTIAL |
| `50` | Toggle player/game state via `x.u()` or `x.t()`. Exact domain name pending `game.g`. | Immediate. | PARTIAL |
| `51` | Non-blocking/dialog setup: `S.aB()` then `game.j D` text at position. | Immediate. | PARTIAL |
| `52` | Set VM flag `this.i` and static `game.c.j`. | Immediate. | PARTIAL |
| `53` | Set player/pet mode/flag via `x.a(byte,byte,...)`, update actors of behavior `v==1`, show UI `S.a`, wait `w.k(1)`. | State `5`, waits world/input condition. | PARTIAL |
| `54` | Build `int[count][3]` from three string lists and pass to `game.d.a().a(...)`. | Immediate. | PARTIAL |
| `55` | Create/remove attached world effect sprite `ag` using sprite 340, tied to room index `ah`. | Immediate. | VERIFIED |
| `56` | Show/activate or hide/deactivate listed actors; persist visible/state through `game.k.a(actor, field, value, true)`. | Immediate. | VERIFIED |
| `57` | No executor case observed; trigger-only special parent actor condition. | Trigger-only. | TRIGGER_ONLY |
| `58` | If player parent actor is in state and animation finished, reset it and move another actor to coords; clear parent relation. | Immediate. | PARTIAL |
| `59` | No executor case observed; trigger-only wait-all-actors-idle. | Trigger-only. | TRIGGER_ONLY |
| `60` | Set listed actors to state/anim, possibly remove from display list for subtype; wait until animations finish. | State `5`, waits all done. | VERIFIED/PARTIAL |
| `61` | No executor case observed; trigger-only wait-all-actors-idle plus player stop. | Trigger-only. | TRIGGER_ONLY |
| `62` | Branch depending which actor from list has `h()==2`; compares to target actor id. | May set state `6`; branches if found. | PARTIAL |
| `63` | Player state/action: `x.h(value)` or `x.s()`, and set VM flag `k`. | Immediate. | PARTIAL |
| `64` | Spawn/remove helper sprite through `game.k.a(sprite)` and attach to player or actor; or `game.k.g()` remove. | Immediate. | VERIFIED |
| `65` | Switch outer runtime/UI state `y.a(100)` and branch based on static `game.c.X`. | State `5`, waits external flag. | PARTIAL |
| `66` | Set `an.U`, call `an.c(0,3)`. Platform/global state side effect. | Immediate. | PARTIAL |
| `67` | Set static `game.k.v`. | Immediate. | PARTIAL |
| `68` | No executor/trigger case observed. | None observed. | NO_CASE |
| `69` | No executor case observed; trigger-only selected actor condition. | Trigger-only. | TRIGGER_ONLY |
| `70` | Set world/UI mode through `game.k.a(byte)` for modes 0/1/2; wait static `game.c.e`. | State `5`, waits external completion. | VERIFIED/PARTIAL |
| `71` | Branch based on player value `x.F >= threshold`. | Immediate branch. | PARTIAL |
| `72` | Spawn one or more temporary sprite effects attached to player or actors. | Immediate. | VERIFIED |
| `73` | No executor case observed; trigger-only inventory quantity condition. | Trigger-only. | TRIGGER_ONLY |
| `74` | Branch based on first entry of `x.K` vector, element `[1] > 0`. | Immediate branch. | PARTIAL |
| `75` | No executor case observed; trigger-only `x.L` non-empty. | Trigger-only. | TRIGGER_ONLY |
| `76` | Mark current event complete, set target world/room, `j=-1`, enter world mode/state `29` via `game.k.a(byte)`. | Leaves/changes world UI mode. | VERIFIED/PARTIAL |
| `77` | Mark arbitrary event as state `4`; if same room update live event. | Immediate. | VERIFIED |
| `78` | No executor case observed; trigger-only multi-event-complete condition. | Trigger-only. | TRIGGER_ONLY |
| `79` | No executor case observed; trigger-only event complete + selected actor condition. | Trigger-only. | TRIGGER_ONLY |
| `80` | Timed challenge/reward flow. Param 0 starts countdown or ends reward; gives item/money based on elapsed time. | State `5`, waits countdown/message. | PARTIAL |
| `81` | Branch based on `x.t(id)` or `x.v(id)` boolean checks. | Immediate branch. | PARTIAL |
| `82` | Persist/reset listed actor positions and state through `actor.r()` and `actor.s()`. | Immediate. | VERIFIED |
| `83` | Switch runtime state `y.a(30)` and branch using static `game.c.m`. | State `5`, waits external selection. | PARTIAL |
| `84` | Formatted text/dialog: substitutes progress values into string, displays via `S.a`, waits confirm/paging. | State `5`, waits text confirm. | VERIFIED/PARTIAL |
| `85` | Branch based on counter/progress `p` in range `0..4`. | Immediate branch. | PARTIAL |
| `86` | No executor case observed; trigger-only referenced event complete. | Trigger-only. | TRIGGER_ONLY |
| `87` | Add/remove creature/pet-like entity with extra id param; uses `game.g.a(...)` / `x.n(...)`. | State `5`, waits message close. | PARTIAL |
| `88` | Branch based on `x.y() == 2` (capacity/status check). | Immediate branch. | PARTIAL |

## Opcode 9 Effect Subtypes

Opcode `9` delegates to `b.java`.

| Effect id in `b()[0]` | Handling in opcode 9 | Wait condition | Status |
|---|---|---|---|
| `10` | `b.c(0,10)` then `b.d(limit, mode)`. | `b.b` complete. | VERIFIED |
| `12/13` | `b.c(0,id)` then `b.a(G,H,I,J,K)` secondary channel. | `b.c` complete. | VERIFIED |
| `14/15` | `b.c(0,id)` then load texture name `M[index]` with x/y/speed. | `b.b` complete. | VERIFIED/PARTIAL |
| `16` | weather/particle texture: star/fire variants, or stop when subtype else. | Does not wait same way; stop path sets event state `1`. | PARTIAL |
| `17` | `b.c(color,17)` then circle params `S/T/U/R`. | `b.b` complete. | VERIFIED |
| other | Build ARGB color from params `[1..4]`, call `b.c(color,effectId)`. | `b.b` complete. | VERIFIED/PARTIAL |

## Port Priority

Priority A, needed for intro/world flow:

```text
0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,16,21,22,23,24,29,30,32,35,38,40,41,42,48,49,56,60,64,70,76,77,84
```

Priority B, gameplay/reward/inventory:

```text
17,18,19,20,31,36,39,45,46,50,52,53,54,55,58,62,63,65,66,67,71,72,74,80,81,82,83,85,87,88
```

Trigger-only support:

```text
15,43,44,57,59,61,69,73,75,78,79,86
```

Currently no observed case:

```text
26,27,28,68
```

## Remaining Validation Checklist

- DONE in pass 2: decoded event resources were scanned and opcode counts were recorded in `11_world_event_opcode_deep_audit.md`.
- DONE in pass 2: `game.g` inventory/pet/currency methods used by opcodes were audited enough for port names, but some domain names remain PARTIAL.
- DONE in pass 2: `game.h` UI endpoints used by event opcodes were audited enough for dialog/openbox/taskTip/choice wait loops.
- DONE in pass 2: `game.d` battle trigger entry path `32/37/54` was audited enough for transition wiring.
- Still needed: for rare opcodes, sample at least one decoded event command and confirm param indexes manually: `38,62,65,80,83,85,88`.
- Still needed: audit `game.d.d()` and battle state machine before claiming full battle setup semantics.
- Re-run this matrix against another decompiler if possible for CFR damaged labels and gotos.
