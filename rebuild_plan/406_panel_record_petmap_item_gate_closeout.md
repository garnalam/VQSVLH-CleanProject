# 406 - Panel Record Petmap Item Gate Closeout

Date: 2026-07-15

Scope: code slice after `405_panel_record_dogiam_route_matrix.md`.

## Source Basis

Source `game.k.R()` handles `record.ui` option `0` as:

- if `game.j.p().l(5)` is true, route to petmap with `o.a((byte)11)`.
- otherwise show warning text: `Không đạt được sủng vật sách tranh đạo cụ`, set `f=1`, and stay in `record.ui`.
- pressing confirm while `f != 0` closes the warning and returns to normal `record.ui` input.

## Implemented

| Area | Change | Status |
| --- | --- | --- |
| `VqsvPanelRuntime.tickRecord()` | Added source-shaped item gate for record option `0` using rebuild inventory item id `5`. | `PORTED/PARTIAL` |
| `VqsvPanelRuntime.tickRecord()` | Added `recordMessageMode` warning loop for `msgwarm.ui`, close on key `5`, stay in `record.ui`. | `PORTED/PARTIAL` |
| Record entry/back paths | Clear `recordMessageMode` when opening `record.ui` from gamemenu, returning from badge, or returning from petmap. | `FIXED` |
| `VqsvSmokeHarness` | Existing petmap-open smoke now seeds item id `5` before confirming record option `0`. | `FIXED` |
| `VqsvSmokeHarness` | Added `panel_petmap_missing_item_warning_from_record` checkpoint and inserted it into `world_panel_full`. | `PORTED/PARTIAL` |

## Verification

Build/check:

- `.\build.ps1`: PASS
- `java "-Dvqsv.modules=..\modules" -cp .\build\classes com.vqsv.rebuild.Main --check`: PASS

Focused PNG smoke:

- `panel_petmap_missing_item_warning_from_record`: PASS
- `panel_petmap_open_from_record`: PASS
- `panel_badge_open_from_record`: PASS

Suite smoke:

- `world_panel_full`: PASS `100/100`
- Output folder: `rebuild_game/build_intro_demo/panel_record_406/world_panel_full`

Mojibake scan:

- No new mojibake from this patch.
- Existing `"??"` unknown-speaker strings remain in old scene/script files and backups.

## Remaining Status

`record.ui / Do giam`: `PORTED/PARTIAL`.

Now covered:

- gamemenu row `3` opens record.
- record option `0` opens petmap when item id `5` is present.
- record option `0` shows source warning when item id `5` is missing.
- record option `1` opens badge.
- petmap/badge back routes remain green.

Still not full parity:

- exact `game.j.p().l(5)` lifecycle/source acquisition of the pet encyclopedia item is `PENDING`.
- exact `record.ui` counters/playtime are still `PORTED/PARTIAL` / `APPROX`.
- exact petmap `q.X/q.Y/q.a()` lifecycle is still `PORTED/PARTIAL`.
- exact badge `q.C[8][2]` lifecycle is still `PORTED/PARTIAL`.
- no original-vs-rebuild pixel compare has been done for record/petmap/badge.

## Recommended Next Step

Next slice should be `407_badge_qc_exact_state_parity_audit`.

Reason: badge state is smaller and source-shaped around `q.C[8][2]`, so it is safer to close before attempting the broader petmap encyclopedia lifecycle.
