# 230 - Panel bag.ui q.N remaining rows current-state matrix

Date: 2026-07-13

Scope: refresh `196_panel_bag_qN_remaining_rows_audit.md` after the later
ride/egg/item slices. This document is the current source-backed map for
`bag.ui` tab `b == 3`, vector `game.g.N`, before continuing the remaining
special rows.

## Source anchors

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
  - `bl()` renders `q.N` rows from `aq.c[5][id]`.
  - `ac()` confirms q.N rows:
    - `0`: egg hatch in place.
    - `5`: `o.a((byte)11)`, close `bag.ui`, open `ride.ui`.
    - `10`: `o.a((byte)24)`, close `bag.ui`, open `transmit.ui`.
    - `6`: `o.a((byte)12)`, close `bag.ui`, open `badge.ui` + `record.ui`.
    - `7/8/9`: `s = id`, `o.a((byte)19)`, close `bag.ui`, open `petstate.ui`.
  - `ab()` handles state 19:
    - up/down moves pet cursor.
    - confirm requires selected pet level `>= 50`.
    - level warning text: `Chi co the cho 50 cap sung vat su dung`.
    - success calls `q.e(s, b)`, then `msgwarm.ui` text `Su dung thanh cong`.
    - success confirm closes `msgwarm.ui` and `petstate.ui`, then returns to
      state `8`.
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
  - `i(id)` creates q.N row and unlocks ride ids `1..4` via `P[id - 1] = 1`.
  - `c(id, qty)` stacks q.N ids `7/8/9`, max 99.
  - `e(id, petIndex)` for ids `7/8/9` calls `z[petIndex].i((byte)id)` and
    consumes one stack, or toggles inactive row to active.
- `modules/script/decoded/data__script__db.mid.json`
  - `aq.c[5]` rows:
    - `0 [295,55,303]`
    - `1 [296,43,304]`
    - `2 [297,44,305]`
    - `3 [298,45,306]`
    - `4 [299,46,307]`
    - `5 [300,47,308]`
    - `6 [301,48,309]`
    - `7 [302,49,310]`
    - `8 [372,50,374]`
    - `9 [373,51,375]`
    - `10 [511,54,512]`

## Current rebuild matrix

| q.N id | Source path | Rebuild equivalent | Status | Next action |
|---:|---|---|---|---|
| 0 | egg hatch in `game.h.ac()` | egg progress/hatch mutation, msgwarm/openbox flow | PORTED/PARTIAL | Keep; exact full `game.h` widget runtime remains partial |
| 1..4 | ride unlock rows via `game.g.i(id)` | `sourceSpecialRewards` unlock ride slots in `ride.ui` smoke | PORTED/PARTIAL | Keep; route reward reachability still event-dependent |
| 5 | `o.a(11)` -> `ride.ui` -> `q.h(i)` | ride render/navigation/warnings/speed mutation | PORTED/PARTIAL | Keep; player visual ride swap still pending |
| 6 | `o.a(12)` -> badge/record UI | not ported | PENDING | Audit state 12 separately before code |
| 7 | `s=id`, `o.a(19)` -> petstate special use | state19 petstate target + level warning + success consume | PORTED/PARTIAL | Keep; ids 8/9 need smoke before claiming covered |
| 8 | same as id 7 | same state19 runtime, smoke success consume | PORTED/PARTIAL | Keep; exact state-8 UI stack still partial |
| 9 | same as id 7 | same state19 runtime, smoke success consume | PORTED/PARTIAL | Keep; exact state-8 UI stack still partial |
| 10 | `o.a(24)` -> transmit UI | not ported | PENDING | Audit transmit/map-jump separately before code |

## Decision

Do not port the whole q.N family at once. The smallest source-backed remaining
slice is state 19 for q.N ids `7/8/9`, starting with id `7`:

1. Open `bag.ui` tab 3 and confirm q.N row 7.
2. Close `bag.ui`, open `petstate.ui`.
3. If selected pet level is below 50, show the source warning and return to
   `petstate.ui` after confirm.
4. If selected pet level is at least 50, call source-shaped `q.e(7, petIndex)`:
   attach special id 7 to the pet and consume one stack.
5. Show success `msgwarm.ui`.

Rows `6` and `10` are larger UI states (`badge/record` and `transmit`) and
must remain pending until audited in their own slices.

## Implementation result - q.N case 7

Files:

- `rebuild_game/src/main/java/VqsvPanelRuntime.java`
- `rebuild_game/src/main/java/VqsvIntroDemo.java`
- `rebuild_game/src/main/java/VqsvSourceModels.java`
- `rebuild_game/src/main/java/VqsvBattleUnit.java`
- `rebuild_game/src/main/java/VqsvSaveRuntime.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`
- `rebuild_game/src/main/java/VqsvTextRenderer.java`

Status:

- `game.h.ac()` q.N case 7 confirm -> `s=id`, `o.a(19)`, close `bag.ui`,
  open `petstate.ui`: PORTED/PARTIAL.
- `game.h.ab()` level `< 50` warning: PORTED/PARTIAL via `msgwarm.ui`.
- `game.g.e(7, petIndex)` consume stack and call `game.b.i(7)`:
  PORTED/PARTIAL.
- `game.b.i(7)` stat behavior is source-shaped through the existing
  `BattleUnit.applyNatureType(7)` path and persisted as `sourceSpecialUseId`.
- Success `msgwarm.ui`: PORTED/PARTIAL.
- Return after success/warning: PORTED/PARTIAL; exact source state-8 UI stack is
  approximated by returning to `bag.ui` tab 3.
- `msgwarm.ui` message text now renders as a single clipped/marquee line when
  long, matching the current project rule for warning text instead of wrapping
  into multiple crowded lines.

Smoke PNG:

- `rebuild_game/build_intro_demo/panel_bag_special_reward7_render.png`
- `rebuild_game/build_intro_demo/panel_bag_special_reward7_state19_petstate.png`
- `rebuild_game/build_intro_demo/panel_bag_special_reward7_level_warning.png`
- `rebuild_game/build_intro_demo/panel_bag_special_reward7_success_consume.png`

Verification:

- `build.ps1`: pass.
- `com.vqsv.rebuild.Main --check`: pass.
- `VqsvBattleDamageFormulaCheck`: pass.
- mojibake scan for Java source + this doc: no matches.
- `panel_wheel` smoke suite: 8/8 pass.
- `battle_quick` smoke suite: 20/20 pass.

Next roadmap step:

1. q.N ids `8` and `9` now have smoke coverage through the same state19
   runtime:
   - `rebuild_game/build_intro_demo/panel_bag_special_reward8_success_consume.png`
   - `rebuild_game/build_intro_demo/panel_bag_special_reward9_success_consume.png`
2. q.N family `7/8/9` is now PORTED/PARTIAL as a family:
   - petstate target selection,
   - level gate,
   - `game.g.e(id, pet)` stack consume,
   - `game.b.i(id)` source-shaped stat marker via `sourceSpecialUseId`,
   - success/warning `msgwarm.ui`.
3. Then choose either q.N case `6` state 12 badge/record audit or q.N case
   `10` state 24 transmit audit. Do not port both together.
