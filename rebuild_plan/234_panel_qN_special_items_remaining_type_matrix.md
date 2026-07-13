# 234 - Panel q.N special items remaining type matrix

Date: 2026-07-13

Scope: audit-only refresh of the remaining `game.g.N` / `bag.ui` tab `b == 3`
special rows after the normal item table and item 13 world avoid timer work.
No code was changed for this document.

This document intentionally does not include top-level normal items from
`aq.c[4]` except where they create side effects into `q.N` (for example item
13 calling `q.c(1)`). It is meant to prevent the next work from looping around
ordinary heal/PP items and to split the remaining special rows by source type.

## Source anchors

- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
  - `N` is the special/egg/ride vector.
  - `i(id)` creates q.N rows; for ids `1..4` it also unlocks ride slots through
    `P[id - 1] = 1`.
  - `c(id, qty)` stacks a q.N row and caps count at `99`.
  - `e(id, petIndex)` applies ids `7/8/9` to a selected pet, consumes one
    stack, and calls `z[petIndex].i((byte)id)`.
  - `j(id)` deactivates an active row.
  - `k(id)` checks whether a row is active.
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
  - `bl()` renders `bag.ui` tab `b == 3` rows from `q.N` and `aq.c[5]`.
  - `ac()` confirms rows in `bag.ui`.
  - `ab()` handles state 19 special pet-target use for q.N ids `7/8/9`.
- `modules/script/decoded/data__script__db.mid.json`
  - `aq.c[5]` is the q.N special row metadata table.
- `modules/script/decoded/data__script__chs.mid.json`
  - text ids in `aq.c[5]` resolve row names/descriptions.

Related rebuild docs:

- `rebuild_plan/196_panel_bag_qN_remaining_rows_audit.md`
- `rebuild_plan/230_panel_bag_qN_remaining_rows_current_state_matrix.md`
- `rebuild_plan/232_panel_bag_item13_14_special_route_parity_closeout.md`
- `rebuild_plan/233_panel_item13_world_avoid_timer_closeout.md`

## Source storage model

`game.g.N` stores q.N rows as `int[]` with shape:

| Field | Meaning | Notes |
|---|---|---|
| `row[0]` | special id | indexes `aq.c[5][id]` |
| `row[1]` | active flag | `0` inactive/count row, `1` active/opened row |
| `row[2]` | stack count | used by stackable specials, especially ids `7/8/9`; source caps at `99` |

Helper behavior:

| Source helper | Behavior | Rebuild implication |
|---|---|---|
| `game.g.i(0)` | add `[0,0,0]` egg row | egg row exists but inactive until opened/active |
| `game.g.i(id != 0)` | add `[id,1,0]` | non-egg rows are active by default |
| `game.g.i(1..4)` | also set `P[id - 1] = 1` | ride unlock rows unlock ride slots |
| `game.g.c(id, qty)` | add/stack `[id,0,qty]`, cap qty at `99` | used by stackable specials; item 13 calls `q.c(1)` in source |
| `game.g.e(7/8/9, pet)` | call pet `i((byte)id)`, decrement stack/remove | state 19 special use |
| `game.g.e(other id, pet)` | if row inactive, set active flag to `1` | generic activate path |
| `game.g.j(id)` | if active row, set active flag to `0` | used by egg hatch completion |
| `game.g.k(id)` | true when row `[id,1,*]` exists | egg active/readiness checks |

Source oddity: item 13 is a normal item in `aq.c[4]`, but its successful use
calls `q.c(1)`. q.N id `1` is also a ride harness row in `aq.c[5]`. Keep this
as a source side effect, not as an invented item-family rule.

## aq.c[5] raw type matrix

| q.N id | Raw row | Inferred source type | Name/meaning | Current status |
|---:|---|---|---|---|
| 0 | `[295,55,303]` | egg lifecycle | pet egg row | PORTED/PARTIAL |
| 1 | `[296,43,304]` | ride unlock row | ride harness 1 | PORTED/PARTIAL |
| 2 | `[297,44,305]` | ride unlock row | ride harness 2 | PORTED/PARTIAL |
| 3 | `[298,45,306]` | ride unlock row | ride harness 3 | PORTED/PARTIAL |
| 4 | `[299,46,307]` | ride unlock row | ride harness 4 | PORTED/PARTIAL |
| 5 | `[300,47,308]` | ride UI opener | pet book/page / ride entry row | PORTED/PARTIAL |
| 6 | `[301,48,309]` | badge/record UI opener | badge/illustration record | PENDING |
| 7 | `[302,49,310]` | pet-target special stone | ancient rune stone agility | PORTED/PARTIAL |
| 8 | `[372,50,374]` | pet-target special stone | ancient rune stone attack | PORTED/PARTIAL |
| 9 | `[373,51,375]` | pet-target special stone | ancient rune stone life | PORTED/PARTIAL |
| 10 | `[511,54,512]` | transmit UI opener | transmission stone | PENDING |

The names above are normalized meanings from the decoded text table. Do not
copy mojibake text into Java source.

## Render matrix from `game.h.bl()`

| q.N id/type | Source render path | Current rebuild mapping | Status |
|---|---|---|---|
| id `0` egg | row icon/name from `aq.c[5][0]`; count text is `Hoan thanh`, `1 cai`, or `0 cai`; description widget `163`; progress widgets `164/165` when active | egg row render, warnings, hatch result/openbox flow | PORTED/PARTIAL |
| ids `1..4` ride unlock | row icon/name/description from `aq.c[5]`; action label path is decompiled oddly and may show ride summon/recall style text | ride unlocks represented in `sourceSpecialRewards` and used by ride UI smoke seeding | PORTED/PARTIAL |
| id `5` ride UI opener | row icon/name/description from `aq.c[5][5]`; confirm opens state 11 | ride UI entry, render/navigation/warnings/speed mutation already covered | PORTED/PARTIAL |
| id `6` badge/record opener | row icon/name/description from `aq.c[5][6]`; confirm opens state 12 | no badge/record runtime | PENDING |
| ids `7/8/9` pet-target stones | row icon/name/description from `aq.c[5]`; action is `Su dung`; confirm opens state 19 | petstate target, level gate, consume, warning/success message | PORTED/PARTIAL |
| id `10` transmit opener | row icon/name/description from `aq.c[5][10]`; confirm opens state 24 | no transmit runtime | PENDING |

Decompiler note: the `bl()` action-label ternary has a suspicious condition
`i1 > 0 || i1 <= 4`, which is always true for real q.N ids. Earlier switch
logic and confirm behavior are more reliable anchors than that label expression
alone.

## Confirm matrix from `game.h.ac()`

| q.N id | Source confirm behavior | Source UI/state | Side effect | Rebuild status |
|---:|---|---|---|---|
| 0 | if active and `game.k.a().r()` ready, hatch; if no space, warn; if not ready, warn | `msgwarm.ui`, then openbox/result text | `game.k.q = 0`, map actor slot update, `q.j(0)` | PORTED/PARTIAL |
| 1 | no explicit confirm case in q.N switch | q.N row / ride unlock data | unlock is created by `game.g.i(1)` via `P[0] = 1` | PORTED/PARTIAL |
| 2 | no explicit confirm case in q.N switch | q.N row / ride unlock data | unlock is created by `game.g.i(2)` via `P[1] = 1` | PORTED/PARTIAL |
| 3 | no explicit confirm case in q.N switch | q.N row / ride unlock data | unlock is created by `game.g.i(3)` via `P[2] = 1` | PORTED/PARTIAL |
| 4 | no explicit confirm case in q.N switch | q.N row / ride unlock data | unlock is created by `game.g.i(4)` via `P[3] = 1` | PORTED/PARTIAL |
| 5 | `o.a((byte)11)`, close `/data/ui/bag.ui` | `ride.ui` / state 11 | ride runtime handles `q.h(i)` summon/recall behavior | PORTED/PARTIAL |
| 6 | `o.a((byte)12)`, close `/data/ui/bag.ui` | badge/record UI / state 12 | unknown until state 12 audit | PENDING |
| 7 | set `s = 7`, `o.a((byte)19)`, close `/data/ui/bag.ui` | `petstate.ui` / state 19 | `q.e(7, petIndex)`, consume stack, apply pet special marker | PORTED/PARTIAL |
| 8 | set `s = 8`, `o.a((byte)19)`, close `/data/ui/bag.ui` | `petstate.ui` / state 19 | `q.e(8, petIndex)`, consume stack, apply pet special marker | PORTED/PARTIAL |
| 9 | set `s = 9`, `o.a((byte)19)`, close `/data/ui/bag.ui` | `petstate.ui` / state 19 | `q.e(9, petIndex)`, consume stack, apply pet special marker | PORTED/PARTIAL |
| 10 | `o.a((byte)24)`, close `/data/ui/bag.ui` | transmit/teleport UI / state 24 | unknown until state 24 audit | PENDING |

## Current rebuild status by type

| Type | Rows | Current rebuild equivalent | Status | Remaining gap |
|---|---|---|---|---|
| Egg lifecycle | `0` | egg active/not-ready/space-full/success messages, hatch mutation, openbox/result flow | PORTED/PARTIAL | after-battle egg progress increment remains separate/deferred; full `bag.ui` widget runtime remains partial |
| Ride unlock data | `1..4` | `sourceSpecialRewards`, ride slot unlock smoke, save/load support | PORTED/PARTIAL | exact event acquisition for every ride row and full player visual ride swap remain partial |
| Ride UI opener | `5` | state 11 ride UI entry, navigation, warnings, speed mutation | PORTED/PARTIAL | exact ride UI widget runtime and map visual parity remain partial |
| Badge/record UI opener | `6` | trace/source row only | PENDING | needs dedicated state 12 audit before code |
| Pet-target special stones | `7/8/9` | state 19 petstate target, level gate, `game.g.e(id, pet)`, consume, success/warning `msgwarm.ui` | PORTED/PARTIAL | exact state-8 return stack and exact pet stat mutation parity should be rechecked if these stones become gameplay-critical |
| Transmit UI opener | `10` | trace/source row only | PENDING | needs dedicated state 24 audit before code |

## Remaining rows decision

The only fully unported q.N row families are:

1. q.N id `6`: state 12 badge/record.
2. q.N id `10`: state 24 transmit.

Do not port both together. They are different UI states and likely different
side-effect domains.

Recommended next slice:

1. Create a dedicated audit for q.N id `6` / state 12 badge-record.
   - Read `game.h` state 12 methods.
   - Identify all `.ui` files loaded.
   - Identify what save/global data it reads/writes.
   - Produce a state/input/side-effect matrix.
   - No code in the audit slice.
2. Then choose either a tiny state 12 render-only slice or, if state 12 is not
   currently useful, defer it and audit q.N id `10` / state 24 transmit.

Keep deferred:

- item `14` after-battle egg progress increment from battle/post-battle source;
- full random encounter generator;
- generic decoded event VM;
- full `bag.ui` widget runtime beyond the rows currently needed.

## Verification status

Audit-only document. No build, check, or smoke was required because no runtime
code changed.
