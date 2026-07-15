# 399 - Panel Bag Tab3 Dac Thu Closeout / q.N-q.O Split Audit

Date: 2026-07-15

## Scope

Close out the `bag.ui` tab split after tab 1 `Trang suc` and tab 2 `Tai lieu`.

This slice does not port a new special action. It only locks naming/data ownership so future action slices do not mix `q.N` material rows with `q.O` special rows.

## Source Anchors

- Source runtime: `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- Bag open: `game.k.ab()`
- Bag refresh dispatch: `game.k.br()`
- Tab 2 material branch: `game.k.bt()`
- Tab 3 special branch: `game.k.bu()`
- Confirm/back/input branch: `game.k.ac()`

## Final Source Split

| Tab `b` | UI label | Source method | Source vector | Source table | Source role |
| ---: | --- | --- | --- | --- | --- |
| `0` | `Vat pham` / `Tieu hao` | `bs()` | `q.L + q.K` | `aq.c[4]` | normal items, balls, item 13/14 direct routes |
| `1` | `Trang suc` | `br()` case `1` | `q.M` | `aq.c[3]` | equipment/held item inventory render |
| `2` | `Tai lieu` | `bt()` | `q.N` | `aq.c[3]` | material/key inventory render |
| `3` | `Dac thu` | `bu()` | `q.O` | `aq.c[5]` | egg, ride/special rewards, badge/record, transmit |

Important correction:

- Previous wording like `q.N case6`, `q.N case10`, or `source q.N special` was wrong for tab 3.
- Source `game.k.bu()` proves tab 3 is `q.O`, not `q.N`.
- Rebuild traces/smoke wording now use `q.O case...` for tab 3 special actions.

## Tab 3 Source Behavior

`game.k.bu()`:

- uses list controller/container widget `125`
- reads rows from `q.O`
- row icon is `aq.c[5][id][1]`
- row name is `aq.c[5][id][0]`
- description is `aq.c[5][selectedId][2]` in widget `163`
- scrollbar thumb is widget `162`, positioned with `127 + h * 72 / q.O.size()`
- row widgets are `137/138/139 + i * 5`

Special source behavior summary:

| q.O id/case | Source role | Rebuild status |
| ---: | --- | --- |
| `0` | egg row / hatch openbox flow | PORTED/PARTIAL |
| `5` | ride/open route | PORTED/PARTIAL |
| `6` | badge/record-style special branch | PENDING |
| `7/8/9` | pet-target special use via state 19 | PORTED/PARTIAL |
| `10` | transmit/accelerate-style special branch | PENDING |

## Rebuild Changes

- Added `panel_bag_tab3_dac_thu_open` to the `panel_bag` smoke suite.
- The new checkpoint opens tab 3 without confirming any action.
- It asserts the source trace `SMOKE seed source bag q.O tab3 row0`.
- It asserts tab switch trace `b=2->3 title=Dac thu`.
- It pixel-checks:
  - tab 3 rows `137/138/139`
  - description widget `163`
  - scrollbar widget `162`
- Existing tab 3 traces now consistently say `q.O case...`.
- Search confirmed no remaining Java smoke/runtime wording like:
  - `q.N case`
  - `q.N tab3`
  - `source q.N special`
  - `qN1Stack`
  - `Expected q.N`

## Smoke Evidence

Output directory:

`C:\Users\Dell\Downloads\ResourcesVQSV\rebuild_game\build_intro_demo\panel_bag_tab3_399`

Passed suite:

- `--smoke-suite panel_bag`
- `13/13` checkpoints passed

New 399 checkpoint:

- `panel_bag_tab3_dac_thu_open`

Reference PNG:

`C:\Users\Dell\Downloads\ResourcesVQSV\rebuild_game\build_intro_demo\panel_bag_tab3_399\panel_bag_tab3_dac_thu_open.png`

Regression:

- `.\build.ps1` passed
- `VqsvIntroDemo --check` passed

## Status

`bag.ui` tab ownership is now clean:

- tab 1 `Trang suc` = `q.M`
- tab 2 `Tai lieu` = `q.N`
- tab 3 `Dac thu` = `q.O`

`bag.ui` tab 3 `Dac thu`: PORTED/PARTIAL.

Complete for:

- source-backed q.O render path
- tab 3 row/icon/name/count/description render shape
- tab 3 scrollbar render shape
- q.N/q.O naming split in runtime traces and smoke asserts
- no accidental action mutation in the 399 tab-open checkpoint

Remaining:

- q.O case `6` badge/record action is still pending.
- q.O case `10` transmit action is still pending.
- Full generic `bag.ui` widget VM is still not 100%; current implementation remains source-shaped.

## Next Roadmap Step

Choose one action slice, not both:

1. `400 - Panel Bag q.O Case6 Badge/Record Audit`
2. `402 - Panel Bag q.O Case10 Transmit Audit`

Follow-up completed:

- `400 - Panel Bag q.O Case6 Badge/Record Audit`

Next if continuing this branch:

- `401 - Panel Bag q.O Case6 Badge Route Port`

Do not implement case `6` and `10` together.
