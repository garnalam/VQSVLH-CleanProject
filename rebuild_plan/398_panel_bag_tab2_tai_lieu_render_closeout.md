# 398 - Panel Bag Tab2 Tai Lieu Render Parity

Date: 2026-07-15

## Scope

Port/render the top-level `bag.ui` tab 2 `Tai lieu`.

This slice is render/navigation/back parity only. It intentionally does not port special `q.N` action branches yet.

## Source Anchors

- Source runtime: `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- Bag refresh dispatch: `game.k.br()`
- Tab 2 branch: `game.k.bt()`
- Source list: `q.N`
- Source table: `aq.c[3]`

## Source Behavior

`game.k.bt()`:

- uses list controller/container `86`
- hides left action softkey widget `7`
- iterates `q.N`
- row icon: `aq.c[3][id][1]`
- row name: `aq.c[3][id][0]`
- special case `id == 17`: display name is `Chia khoa vang`
- row count: `q.N[row][1]`
- description: `aq.c[3][selectedId][2]`
- scrollbar thumb: widget `123`, position `127 + h * 72 / q.N.size()`

## Source Widget Mapping

| Purpose | Source widget ids |
|---|---|
| List controller/container | `86` |
| Row backgrounds | `97, 102, 107, 112, 117` |
| Row icon | `98, 103, 108, 113, 118` |
| Row name | `99, 104, 109, 114, 119` |
| Row count | `100, 105, 110, 115, 120` |
| Description text | `124` |
| Scrollbar track/thumb | `122 / 123` |

## Rebuild Changes

- Added `SourceMaterialItem` as a dedicated proxy for source `q.N`.
- Added `Scene.sourceMaterialItems`.
- Added `VqsvSourceOps.sourceMaterialIconCell/name/description`, backed by `aq.c[3]`.
- Implemented source special display name for material id `17`: `Chia khoa vang`.
- `VqsvPanelRuntime` now renders bag tab 2 from `sourceMaterialItems`.
- Tab 2 row icon/name/count use widgets `98/99/100 + i*5`.
- Tab 2 description uses widget `124`.
- Tab 2 scrollbar uses widgets `122/123`, with selected-row thumb positioning like source.
- Tab 2 left action label is empty, matching source `p.a.a(7).a(false)`.
- Confirm on tab 2 is explicitly render-only/no-op and traces selected `q.N` row.
- `panel_bag` smoke suite now includes tab 2 checkpoints.

## Smoke Evidence

Output directory:

`C:\Users\Dell\Downloads\ResourcesVQSV\rebuild_game\build_intro_demo\panel_bag_tab2_398`

Passed suite:

- `--smoke-suite panel_bag`
- `12/12` checkpoints passed

New tab 2 checkpoints:

- `panel_bag_tab2_tai_lieu_open`
- `panel_bag_tab2_tai_lieu_navigation`
- `panel_bag_tab2_tai_lieu_confirm_noop`
- `panel_bag_tab2_tai_lieu_back`

Regression:

- `--check` passed

Reference PNG:

`C:\Users\Dell\Downloads\ResourcesVQSV\rebuild_game\build_intro_demo\panel_bag_tab2_398\panel_bag_tab2_tai_lieu_open.png`

## Status

`bag.ui` tab 2 `Tai lieu`: PORTED/PARTIAL.

Complete for:

- source-backed data binding from `q.N` proxy
- icon/name/count/description render
- special display name for id `17`
- row navigation
- hover/click row hitboxes through current-tab row widgets
- back flow
- no accidental action mutation from top-level bag

Remaining:

- Full source `q.N` lifecycle/save-load parity for material rows is not generalized yet.
- Special action branches for q.N are still separate/pending:
  - case `6` badge/record
  - case `10` transmit
  - other special branches already belong to tab 3/q.O-style work and should not be mixed into this render slice.
- Generic multi-container `bag.ui` runtime is still source-shaped, not a full widget VM.

## Next Roadmap Step

Next recommended slice:

`399 - Panel Bag Tab3 Dac Thu Closeout / Remaining q.N-q.O Split Audit`

Goal:

- Re-audit whether rebuild naming around q.N/q.O is now clean enough.
- Make sure tab 3 `Dac thu` remains stable after tab 1/2 split.
- Do not port q.N case `6/10` yet unless we choose that as the next explicit function slice.
