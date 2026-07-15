# 372 - World bqTask Marker Predicate `game.e.a(b2)` / `game.e.b(b2)` Audit

Date: 2026-07-15

Scope: source audit only for branch quest world marker predicates used by `game.e.G()`. No runtime behavior was changed in this slice.

## Source Facts

| Source | Fact | Status |
| --- | --- | --- |
| `game.e.G()` | First loops `aq` rows, then `ap` rows from `/data/script/bqTask.mid`. | `AUDITED` |
| `game.e.G()` + `aq` | If `game.e.b(b2)` is true, source creates sprite `259`, animation/state `1`. | `AUDITED/PENDING` |
| `game.e.G()` + `aq` | If active branch task exists in `F` with status `1`, source creates sprite `259`, animation/state `15`. | `AUDITED/PORTED_ROW0_PARTIAL` |
| `game.e.G()` + `ap` | If `game.e.a(b2)` is true, source creates sprite `259`, animation/state `7`. | `AUDITED/PORTED_ROW0_PARTIAL` |
| Event record | `b2.a()` is opcode. `b2.b()` is the event record `short_args` array. | `AUDITED` |
| Opcode `43` | Branch quest accept/available side. It is tested with `game.e.a(b2)`. | `AUDITED` |
| Opcode `44` | Branch quest active/ready/complete side. It is tested with `game.e.b(b2)`. | `AUDITED` |

## `short_args` Index Map For Opcode 43/44

The predicates read these indexes directly:

| Index | Source use | Meaning inferred from source/events | Status |
| --- | --- | --- | --- |
| `0` | `game.e.n(b2.b()[0])`, `F[][0]` | branch task id | `AUDITED` |
| `1` | checked in `G()` before marker state `15` | branch task flag/type; `1` rows participate in `F` active task flow | `AUDITED/PARTIAL` |
| `2` | compare with current scene `J.p` | interaction scene id | `AUDITED` |
| `3` | compare with current room `J.q` | interaction room index | `AUDITED` |
| `4` | `J.n[actorId]` | actor id to attach marker to | `AUDITED` |
| `5` | `n[worldKey(scene,room)]` prerequisite scene | prerequisite scene id | `AUDITED` |
| `6` | `n[worldKey(scene,room)]` prerequisite room | prerequisite room index | `AUDITED` |
| `7` | prerequisite event-group status | prerequisite event group id, or `-1` for none | `AUDITED` |
| `8` | predicate switch selector | condition type | `AUDITED` |
| `9` | condition parameter | meaning depends on condition type | `AUDITED/PARTIAL` |
| `10` | condition parameter | meaning depends on condition type | `AUDITED/PARTIAL` |
| `11` | present in some opcode `44` rows, not read by these predicates | extra event argument, outside this predicate audit | `PENDING` |

## Predicate `game.e.a(b2)` - Available/Accept Marker State 7

Source precondition:

- If `args[7] != -1`, `n[worldKey(args[5], args[6])][args[7]]` must equal `3`.
- If prerequisite event table is missing, predicate returns false.

Condition matrix:

| `args[8]` | Source condition | Rebuild implication | Status |
| --- | --- | --- | --- |
| `-1` | Not handled by switch, returns true after prerequisite gate. | No extra requirement. | `AUDITED` |
| `0` | `K.U[args[9]]` must be true. | Requires source world/story flag array parity. | `PENDING` |
| `1` | Returns true immediately. | Always available after prerequisite gate. | `AUDITED` |
| `2` | `K.P.size() + K.B >= args[9]`, then party or bank must contain species `args[10]`. | Pet count + party/bank species requirement. | `PENDING` |
| `3` | `K.G >= args[9]`. | Currency/progress value requirement. | `PENDING/PARTIAL` |
| `4` | `K.a((byte)args[9], args[10]) == 2`. | Inventory/ownership requirement. | `PENDING` |
| `5` | static `game.e.G > args[9]`. | Global counter strictly greater than threshold. | `PENDING` |
| `6` | static `game.e.G == args[9]`. | Global counter equals threshold. | `PENDING` |

## Predicate `game.e.b(b2)` - Ready/Objective Marker State 1

Source precondition:

- Passes if `args[7] == -1`.
- Otherwise requires `n[worldKey(args[5], args[6])][args[7]] == 3`.

Condition matrix:

| `args[8]` | Source condition | Rebuild implication | Status |
| --- | --- | --- | --- |
| `0` | `K.a((byte)args[9], args[10]) == 2`. | Inventory/ownership objective satisfied. | `PENDING` |
| `1` | `K.U[args[9]]` must be true. | Source flag objective satisfied. | `PENDING` |
| `2` | Same event completion check as precondition. | Completion-driven objective. | `AUDITED/PARTIAL` |
| `3` | `K.b(args[9], args[10], (byte)0)` must be true. | Item/objective ownership check with source mode `0`. | `PENDING` |
| `4` | Same event completion check as precondition. | Completion-driven objective. | `AUDITED/PARTIAL` |
| `5` | `K.G >= args[9]`. | Currency/progress value requirement. | `PENDING/PARTIAL` |
| `6` | For all element ids `0,1,2,3`, player party must contain at least one pet whose `a.b.c.a(0, pet.r(), 1)` equals that element. | Four-element party collection requirement. | `PENDING` |

## Marker Priority In `game.e.G()`

For each `aq` row, source prevents duplicate markers on the same actor with `vector.contains(actorId)`:

1. `aq` + `game.e.b(b2)` true -> marker state `1`.
2. Otherwise `aq` active branch logic -> marker state `15`.
3. Then `ap` rows are scanned; if actor has no marker yet and `game.e.a(b2)` true -> marker state `7`.

This means marker state `1` has priority over active state `15`, and both have priority over available state `7` on the same actor.

## Full `bqTask.mid` Predicate Snapshot

`first` is `ap`, `second` is `aq`.

| Row | Table | Scene/Room/Group | Opcode | Task | Actor | Prereq `[5,6,7]` | Cond `[8,9,10]` | Predicate role |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| 0 | `ap` | `1/0/7` | `43` | `0` | `35` | `0,0,-1` | `-1,0,0` | available state `7` |
| 0 | `aq` | `1/0/8` | `44` | `0` | `35` | `1,0,7` | `0,1,23` | ready state `1` or active state `15` |
| 1 | `ap` | `1/0/9` | `43` | `1` | `35` | `1,0,8` | `1,0,0` | available state `7` |
| 1 | `aq` | `2/1/6` | `44` | `1` | `73` | `1,0,9` | `0,4,68` | ready state `1` or active state `15` |
| 2 | `ap` | `2/1/7` | `43` | `2` | `73` | `2,1,6` | `1,0,0` | available state `7` |
| 2 | `aq` | `3/3/1` | `44` | `2` | `51` | `2,1,7` | `0,1,24` | ready state `1` or active state `15` |
| 3 | `ap` | `3/3/2` | `43` | `3` | `51` | `3,3,1` | `1,0,0` | available state `7` |
| 3 | `aq` | `4/2/0` | `44` | `3` | `10` | `3,3,2` | `1,6,0` | ready state `1` or active state `15` |
| 4 | `ap` | `4/2/1` | `43` | `4` | `10` | `4,2,0` | `1,0,0` | available state `7` |
| 4 | `aq` | `4/5/0` | `44` | `4` | `27` | `4,2,1` | `0,2,38` | ready state `1` or active state `15` |
| 5 | `ap` | `4/5/1` | `43` | `5` | `27` | `4,5,0` | `1,0,0` | available state `7` |
| 5 | `aq` | `4/5/2` | `44` | `5` | `43` | `4,5,1` | `4,0,0` | ready state `1` or active state `15` |
| 6 | `ap` | `4/5/3` | `43` | `6` | `43` | `4,5,2` | `1,0,0` | available state `7` |
| 6 | `aq` | `4/5/4` | `44` | `6` | `43` | `4,5,3` | `0,3,58` | ready state `1` or active state `15` |
| 7 | `ap` | `4/5/5` | `43` | `7` | `43` | `4,5,4` | `1,0,0` | available state `7` |
| 7 | `aq` | `5/3/1` | `44` | `7` | `53` | `4,5,5` | `1,2,0` | ready state `1` or active state `15` |
| 8 | `ap` | `5/3/2` | `43` | `8` | `53` | `5,3,1` | `1,0,0` | available state `7` |
| 8 | `aq` | `3/3/3` | `44` | `8` | `51` | `5,3,2` | `0,5,83` | ready state `1` or active state `15` |
| 9 | `ap` | `3/3/4` | `43` | `9` | `51` | `3,3,3` | `1,0,0` | available state `7` |
| 9 | `aq` | `4/2/2` | `44` | `9` | `10` | `3,3,4` | `4,0,0` | ready state `1` or active state `15` |
| 10 | `ap` | `11/19/0` | `43` | `10` | `8` | `0,0,-1` | `-1,0,0` | available state `7` |
| 10 | `aq` | `4/12/0` | `44` | `10` | `13` | `11,19,0` | `1,7,0` | ready state `1` or active state `15` |
| 11 | `ap` | `11/11/0` | `43` | `11` | `9` | `0,0,-1` | `-1,0,0` | available state `7` |
| 11 | `aq` | `11/11/1` | `44` | `11` | `9` | `11,11,0` | `0,6,88` | ready state `1` or active state `15` |
| 12 | `ap` | `11/21/0` | `43` | `12` | `9` | `0,0,-1` | `-1,0,0` | available state `7` |
| 12 | `aq` | `11/21/1` | `44` | `12` | `9` | `4,5,6` | `1,13,0` | ready state `1` or active state `15` |
| 13 | `ap` | `11/10/0` | `43` | `13` | `7` | `0,0,-1` | `-1,0,0` | available state `7` |
| 13 | `aq` | `3/3/8` | `44` | `13` | `52` | `11,10,0` | `1,0,0` | ready state `1` or active state `15` |
| 14 | `ap` | `3/6/0` | `43` | `14` | `25` | `0,0,0` | `-1,0,0` | available state `7` |
| 14 | `aq` | `3/6/1` | `44` | `14` | `25` | `3,6,0` | `0,2,39` | ready state `1` or active state `15` |
| 15 | `ap` | `3/3/6` | `43` | `15` | `51` | `3,6,1` | `1,0,0` | available state `7` |
| 15 | `aq` | `3/6/2` | `44` | `15` | `25` | `4,7,0` | `4,0,0` | ready state `1` or active state `15` |
| 16 | `ap` | `2/2/1` | `43` | `16` | `13` | `0,0,-1` | `-1,0,0` | available state `7` |
| 16 | `aq` | `2/2/2` | `44` | `16` | `13` | `2,2,1` | `3,8,3` | ready state `1` or active state `15` |
| 17 | `ap` | `4/5/7` | `43` | `17` | `30` | `0,0,-1` | `-1,0,0` | available state `7` |
| 17 | `aq` | `4/5/9` | `44` | `17` | `30` | `4,5,8` | `1,14,0` | ready state `1` or active state `15` |
| 18 | `ap` | `3/3/7` | `43` | `18` | `52` | `9,1,2` | `5,0,0` | available state `7` |
| 18 | `aq` | `4/5/10` | `44` | `18` | `32` | `3,3,7` | `1,11,0` | ready state `1` or active state `15` |
| 19 | `ap` | `4/5/11` | `43` | `19` | `32` | `4,5,10` | `1,0,0` | available state `7` |
| 19 | `aq` | `11/22/0` | `44` | `19` | `9` | `4,5,11` | `1,12,0` | ready state `1` or active state `15` |
| 20 | `ap` | `11/22/1` | `43` | `20` | `9` | `11,22,0` | `1,0,0` | available state `7` |
| 20 | `aq` | `5/5/0` | `44` | `20` | `13` | `11,22,1` | `1,1,0` | ready state `1` or active state `15` |
| 21 | `ap` | `5/5/1` | `43` | `21` | `13` | `5,5,0` | `1,0,0` | available state `7` |
| 21 | `aq` | `5/5/2` | `44` | `21` | `13` | `5,6,0` | `1,3,0` | ready state `1` or active state `15` |
| 22 | `ap` | `7/3/0` | `43` | `22` | `22` | `0,0,-1` | `-1,0,0` | available state `7` |
| 22 | `aq` | `7/3/1` | `44` | `22` | `22` | `7,3,0` | `6,4,0` | ready state `1` or active state `15` |
| 23 | `ap` | `7/10/0` | `43` | `23` | `13` | `0,0,-1` | `-1,0,0` | available state `7` |
| 23 | `aq` | `7/10/1` | `44` | `23` | `13` | `7,10,0` | `4,0,0` | ready state `1` or active state `15` |
| 24 | `ap` | `7/6/0` | `43` | `24` | `22` | `0,0,-1` | `-1,0,0` | available state `7` |
| 24 | `aq` | `11/3/0` | `44` | `24` | `16` | `7,6,0` | `1,20,0` | ready state `1` or active state `15` |
| 25 | `ap` | `7/6/1` | `43` | `25` | `22` | `11,3,0` | `1,0,0` | available state `7` |
| 25 | `aq` | `11/3/1` | `44` | `25` | `17` | `7,6,1` | `1,20,0` | ready state `1` or active state `15` |
| 26 | `ap` | `7/6/2` | `43` | `26` | `22` | `11,3,1` | `1,0,0` | available state `7` |
| 26 | `aq` | `7/8/0` | `44` | `26` | `13` | `7,6,2` | `1,20,0` | ready state `1` or active state `15` |
| 27 | `ap` | `7/6/3` | `43` | `27` | `22` | `7,8,0` | `1,0,0` | available state `7` |
| 27 | `aq` | `11/3/6` | `44` | `27` | `23` | `7,6,3` | `1,20,0` | ready state `1` or active state `15` |
| 28 | `ap` | `8/0/0` | `43` | `28` | `26` | `0,0,-1` | `-1,0,0` | available state `7` |
| 28 | `aq` | `8/0/1` | `44` | `28` | `26` | `8,4,0` | `2,8,4` | ready state `1` or active state `15` |
| 29 | `ap` | `8/0/2` | `43` | `29` | `27` | `8,0,1` | `1,0,0` | available state `7` |
| 29 | `aq` | `8/0/3` | `44` | `29` | `27` | `8,0,2` | `3,5,20` | ready state `1` or active state `15` |

## Rebuild Gap From This Audit

| Area | Status | Note |
| --- | --- | --- |
| Row 0 available/active marker | `PORTED/PARTIAL` | Already smoke-pass in `371`. |
| Generic `ap` state `7` predicate | `PENDING` | Need source-backed event completion table + condition evaluator. |
| Generic `aq` state `1` predicate | `PENDING` | Need condition evaluator for inventory/species/flags/global counters. |
| Generic `aq` state `15` active marker | `PARTIAL` | Current row 0 active is source-shaped via branch task status. Need full `F/H` lifecycle parity. |
| Duplicate actor marker priority | `PENDING` | Rebuild row 0 does not yet exercise same-actor conflict across multiple rows. |

## Next

Recommended next code slice:

1. Add a trace-only `SourceBqTaskPredicate` evaluator for row 0..29 that logs/exports condition type, prerequisite status, and selected marker state without changing gameplay.
2. Then port a tiny generic marker evaluator for the safe subset:
   - `ap` condition `-1` and `1`.
   - `aq` active state `15` from `F[taskId].status == 1`.
   - Keep `aq` state `1` condition types `0/2/3/4/5/6` as `PENDING` until each backing source field is mapped.
