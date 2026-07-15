# 401 - Panel Bag q.O Case6 Badge Route Port

Date: 2026-07-15

## Scope

Port the direct route from `bag.ui` tab 3 `Dac thu` q.O case `6` into `badge.ui`.

This slice does not port q.O case `10`.

## Source Route

Source path from `game.k.ac()`:

`bag.ui` tab `b=3` -> selected `q.O` row id `6` -> `o.a((byte)12)` -> close `/data/ui/bag.ui` -> state `12` -> `game.k.W()` -> `/data/ui/badge.ui`.

Back path:

`game.k.X()` checks previous state. For bag-origin route, previous state is `8`, so back returns to `bag.ui`, not `record.ui`.

## Rebuild Changes

- `VqsvPanelRuntime.useSpecialBagRow(...)` case `6` now opens existing `Mode.BADGE`.
- Added badge return tracking for bag-origin routes:
  - record-origin badge still backs to `record.ui`
  - bag-origin q.O case `6` backs to `bag.ui` tab `3`
- Preserves selected q.O row and scroll when returning to bag.
- Does not consume q.O special row.
- Does not mutate `sourceBadges` or badge state.
- Added `panel_bag` smoke checkpoints:
  - `panel_bag_qo_case6_badge_open`
  - `panel_bag_qo_case6_badge_navigation`
  - `panel_bag_qo_case6_badge_back`

## Asserted Data

q.O case `6` source row:

`aq.c[5][6] = [301,48,309]`

Meaning:

- name text id `301`: `Huy hiệu của các hình minh họa`
- icon cell `48`
- description text id `309`: `Nhấn nút 5 tra xét những huy hiệu đã đạt được;`

Smoke asserts:

- confirm opens `badge.ui`
- trace contains `q.O case6 confirm -> o.a(12) game.h.W badge.ui`
- trace contains `sourceRow=[301,48,309]`
- q.O reward stack is unchanged
- `sourceBadges` is unchanged
- badge navigation works
- back returns to `bag.ui` tab `3`, selected row `1`

## Smoke Evidence

Output directory:

`C:\Users\Dell\Downloads\ResourcesVQSV\rebuild_game\build_intro_demo\panel_bag_qo_case6_401`

Passed:

- `.\build.ps1`
- `--smoke-suite panel_bag`: `16/16`
- `--check`

Reference PNGs:

- `C:\Users\Dell\Downloads\ResourcesVQSV\rebuild_game\build_intro_demo\panel_bag_qo_case6_401\panel_bag_qo_case6_badge_open.png`
- `C:\Users\Dell\Downloads\ResourcesVQSV\rebuild_game\build_intro_demo\panel_bag_qo_case6_401\panel_bag_qo_case6_badge_navigation.png`
- `C:\Users\Dell\Downloads\ResourcesVQSV\rebuild_game\build_intro_demo\panel_bag_qo_case6_401\panel_bag_qo_case6_badge_back.png`

## Status

q.O case `6` badge route: PORTED/PARTIAL.

Complete for:

- bag tab 3 confirm route
- source row assertion
- existing badge render reuse
- badge grid navigation
- bag-origin back route
- no accidental consume/mutation

Remaining:

- `badge.ui` itself is still source-shaped, not a full generic widget VM.
- Badge achievement state currently maps through rebuild `sourceBadges` count approximation, while source uses exact `q.C[8][2]`.
- q.O case `10` transmit remains pending.

## Next Roadmap Step

Next recommended slice:

`402 - Panel Bag q.O Case10 Transmit Audit`

Status: completed in `402_panel_bag_qo_case10_transmit_audit.md`.

Audit first, no code initially:

- identify q.O row `10`
- confirm source state `24`
- identify `/data/ui/transmit.ui` behavior and data binding
- decide exact smoke PNG set before porting
