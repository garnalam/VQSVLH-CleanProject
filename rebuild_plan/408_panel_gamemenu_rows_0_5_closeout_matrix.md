# 408 - Panel gamemenu.ui Rows 0..5 Closeout Matrix

Date: 2026-07-15

Scope: top-level closeout for the right-softkey `gamemenu.ui` branch after
the record/petmap gate and save/msgtip slices. This document does not add new
behavior. It freezes the current verified baseline so future panel work has a
clear map.

## Rules For This Closeout

- Source first: status is based on decoded `game.k`, decoded UI files, and the
  current rebuild runtime.
- No live client was opened for this closeout. Verification is PNG smoke only.
- Do not claim pixel-perfect parity without original-vs-rebuild pixel compare.
- Classify every row honestly as `PORTED`, `PORTED/PARTIAL`, `APPROX`,
  `PENDING`, or `PC_QOL`.
- This is a panel/world UI closeout only. It does not reopen battle P7, skill
  animation, or generic event VM work.

## Source Chain

World source route:

- `game.k.c()` opens `/data/ui/world.ui`.
- `world.ui` widget `5`, cell `68`, is the right softkey.
- Right softkey calls the game menu route, source-shaped in rebuild as
  `gamemenu.ui`.
- `gamemenu.ui` is the gameplay panel menu containing rows `0..5`.

Runtime/files:

- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- `modules/ui/decoded/data__ui__world.ui.json`
- `modules/ui/decoded/data__ui__gamemenu.ui.json`
- `rebuild_game/src/main/java/VqsvPanelRuntime.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Supporting closeouts:

- `365_world_softkey_panel_source_audit.md`
- `366_new_dev_chat_handoff_world_panel_ui_routes.md`
- `395_panel_petsetting_all_rows_closeout.md`
- `403_panel_bag_qo_case10_transmit_route_render_closeout.md`
- `405_panel_record_dogiam_route_matrix.md`
- `406_panel_record_petmap_item_gate_closeout.md`
- `407_panel_save_msgtip_source_flow_closeout.md`

## Row Matrix

| Row | Source label | Primary UI/state | Current status | What is verified |
| ---: | --- | --- | --- | --- |
| `0` | `Tuy than cua hang` | `bodyShop.ui` / shop buy/payment | `PORTED/PARTIAL` | Shop rows, hover preview, quantity/payment flow, msgyn confirmation, zero-price PC rebuild policy for SMS/currency-type-2 avoidance. |
| `1` | `Sung vat` | `petstate.ui` / `petsetting.ui` | `PORTED/PARTIAL` | Pet list open/navigation/back, hover preview, held item display, petsetting rows `0..5`, item use, active switch, equipment, release, skill view, evolve route. |
| `2` | `Lung bao` | `bag.ui` plus child UIs | `PORTED/PARTIAL` | Bag tabs, hover/wheel list input, item metadata, state17 pet-target items, item13/item14, egg hatch, q.N ride, q.O badge/special/transmit descriptor route. |
| `3` | `Do giam` | `record.ui`, `petmap.ui`, `badge.ui` | `PORTED/PARTIAL` | Record open/back, petmap item gate warning, petmap open/navigation/tab/wheel/back, badge open/navigation/back. |
| `4` | `Nhiem vu` | `task.ui`, task option/detail | `PORTED/PARTIAL` | Main/branch task tabs, detail layout, task option open/navigation/back/confirm close, source-shaped task traces and save/load branch fields. |
| `5` | `Luu du lieu` | `msgtip.ui`, save runtime | `PORTED/PARTIAL` | Save prompt, back/cancel to gamemenu, saving status, success status, close to world, panel-created save exposing title `Choi tiep`. |

## Row 0 - Shop / bodyShop

Status: `PORTED/PARTIAL`.

Closed enough for current panel baseline:

- Source-shaped shop row display and mouse hover preview are covered.
- Quantity confirmation and `msgyn.ui` flow were polished enough for current
  route testing.
- SMS / currency type `2` was intentionally removed/neutralized for PC rebuild
  usage; source original pricing was documented elsewhere for future reversal.

Remaining:

- Full original SMS purchase path is intentionally not ported.
- Exact original shop economy/state lifecycle is not fully claimed.
- No original-vs-rebuild pixel compare for `shopbuy.ui` / `msgyn.ui`.

Representative smoke:

- `panel_shopbuy_hover_preview_no_confirm`

## Row 1 - Sung vat / petstate and petsetting

Status: `PORTED/PARTIAL`.

Closed enough for current panel baseline:

- `petstate.ui` opens from `gamemenu.ui`.
- Navigation/back/hover preview and header/back arrow are covered.
- `petsetting.ui` rows `0..5` have route/action coverage:
  - row `0`: item choice/use.
  - row `1`: active pet switch.
  - row `2`: equipment choice/equip/unequip/transfer.
  - row `3`: release confirm/cancel/success/warnings.
  - row `4`: skill read-only view.
  - row `5`: evolve route.

Remaining:

- Full generic pet storage/bank runtime is wider than this closeout.
- Some stat/source lifecycle fields are still source-shaped rather than exact
  original object parity.
- No pixel-perfect original compare for `petstate.ui`, `petsetting.ui`,
  `choice.ui`, `skill.ui`, or `evolve.ui`.

Representative smoke:

- `panel_petstate_open_from_gamemenu`
- `panel_petstate_navigation`
- `panel_petstate_hover_preview_no_confirm`
- `panel_petstate_petsetting_open`
- `panel_petstate_petsetting_active_switch_success`
- `panel_petstate_petsetting_release_success_removes_pet`
- `panel_petstate_petsetting_item_choice_success_msg`
- `panel_petstate_petsetting_equipment_choice_transfer_success_msg`
- `panel_petstate_petsetting_skill_open`
- `panel_petstate_petsetting_evolve_success_mutate`

## Row 2 - Lung bao / bag

Status: `PORTED/PARTIAL`.

Closed enough for current panel baseline:

- Main item tab opens/navigates/backs.
- Mouse hover and wheel/scrollbar behavior are locked for visible-row mapping.
- Tab `1` equipment inventory renders.
- Tab `2` material/key rows render.
- Tab `3` q.O special rows render.
- Default item use state17 opens pet target view and covers warning/success
  mutation paths.
- q.O routes currently covered include egg, ride, badge route, state19 pet
  target specials, and transmit descriptor route.

Remaining:

- q.O transmit confirms only write the source target tuple; actual map load and
  world resume are deferred.
- Some special item rows are route-rendered but not all original lifecycle
  side effects are fully ported.
- No pixel-perfect original compare for `bag.ui`, `ride.ui`, `badge.ui`,
  `transmit.ui`, or related warning boxes.

Representative smoke:

- `panel_bag_open_from_gamemenu`
- `panel_bag_hover_preview_no_confirm`
- `panel_bag_mouse_wheel_scrollbar_no_confirm`
- `panel_bag_mouse_wheel_hover_click_viewport`
- `panel_bag_default_item_state17_success_msg`
- `panel_bag_item13_success_msg`
- `panel_bag_item14_type0_success`
- `panel_bag_egg_hatch_success_msg`
- `panel_bag_ride_confirm_success_mutation`
- `panel_bag_qo_case6_badge_open`
- `panel_bag_qo_case10_transmit_confirm_world_target`

## Row 3 - Do giam / record, petmap, badge

Status: `PORTED/PARTIAL`.

Closed enough for current panel baseline:

- `gamemenu.ui` row `3` opens `record.ui`.
- Record option `0` checks the petmap item gate:
  - present: opens `petmap.ui`.
  - missing: shows source-shaped warning loop.
- `petmap.ui` navigation/tab/wheel/back is covered.
- Record option `1` opens `badge.ui`; badge navigation/back is covered.

Remaining:

- Exact `game.j.p().l(5)` acquisition lifecycle is pending.
- `record.ui` counters/playtime remain approximate in places.
- Full `q.X/q.Y/q.a()` pet encyclopedia lifecycle is not fully mirrored.
- Exact `q.C[8][2]` badge state parity is still partial.
- Petmap confirm/detail behavior remains pending until source route is proven.

Representative smoke:

- `panel_petmap_record_open_from_gamemenu`
- `panel_petmap_open_from_record`
- `panel_petmap_missing_item_warning_from_record`
- `panel_petmap_navigation`
- `panel_petmap_tab_navigation`
- `panel_petmap_mouse_wheel_hover_click_viewport`
- `panel_badge_open_from_record`
- `panel_badge_navigation`
- `panel_badge_back_returns_record`

## Row 4 - Nhiem vu / task

Status: `PORTED/PARTIAL`.

Closed enough for current panel baseline:

- Task opens from `gamemenu.ui`.
- Main/branch tabs and task row navigation are covered.
- Task option/detail screens open, navigate, back, and close through current
  source-shaped paths.
- Several source traces assert `game.e` / branch field usage.

Remaining:

- Full source marquee/crop behavior is still not a generic Java ME widget VM.
- Full task lifecycle beyond the currently routed early-game tasks remains
  partial.
- No pixel-perfect original compare for `task.ui`.

Representative smoke:

- `panel_task_open_from_gamemenu`
- `panel_task_main_open`
- `panel_task_branch_open`
- `panel_task_branch_completed`
- `panel_task_ui_main_source_layout`
- `panel_task_ui_branch_source_layout`
- `panel_task_navigation`
- `panel_task_hover_preview_no_confirm`
- `panel_task_option_open`
- `panel_task_option_navigation`
- `panel_task_option_back_returns_task`
- `panel_task_back_returns_gamemenu`

## Row 5 - Luu du lieu / msgtip save flow

Status: `PORTED/PARTIAL`.

Closed enough for current panel baseline:

- Save prompt opens from `gamemenu.ui` row `5`.
- Back/cancel returns to `gamemenu.ui` row `5`.
- Confirm shows saving status, calls `VqsvSaveRuntime.save(s)`, then shows
  success status.
- Success closes the panel and returns to free-world movement.
- A panel-created save exposes title-menu `Choi tiep` and loads the saved scene
  snapshot.

Remaining:

- Save format is rebuild properties snapshot, not original byte-identical
  source save.
- Full original `((l)this.o).I()` internals remain partial.
- No pixel-perfect original compare for `msgtip.ui`.

Representative smoke:

- `panel_save_prompt_from_gamemenu`
- `panel_save_back_cancel_returns_gamemenu`
- `panel_save_saving_status`
- `panel_save_success_status`
- `panel_save_success_closes_world`
- `boot_title_continue_after_panel_save`

## Cross-Cutting Input Status

| Input area | Status | Notes |
| --- | --- | --- |
| Mouse hover preview | `PC_QOL/PORTED/PARTIAL` | Applied to panel lists where source-shaped selection allows preview without confirm. |
| Mouse wheel / scrollbar | `PC_QOL/PORTED/PARTIAL` | Covered for bag/petmap/gamesystem and non-scrollable task/skill cases. |
| Visible row click mapping after wheel | `FIXED` | Locked by bag and petmap hover/click viewport smoke. |
| Bottom softkeys | `PORTED/PARTIAL` | World corner buttons open correct source panels; PC-friendly hitboxes are intentional. |
| Generic UI VM | `PENDING` | Rebuild uses concrete source-shaped renderers per route, not a full Java ME widget playback VM. |

## Verification Baseline

Commands to verify this panel baseline from `rebuild_game`:

```powershell
.\build.ps1
java "-Dvqsv.modules=..\modules" -cp .\build\classes com.vqsv.rebuild.Main --check
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-suite world_panel_full .\build_intro_demo\panel_closeout_408\world_panel_full
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-suite panel_wheel .\build_intro_demo\panel_closeout_408\panel_wheel
```

Expected current suite status after `407`:

- `world_panel_full`: `102/102`
- `panel_wheel`: `16/16`, including fixed wheel/hover mapping checkpoints for
  panel list input.

Verified for this closeout:

- `.\build.ps1`: PASS.
- `java "-Dvqsv.modules=..\modules" -cp .\build\classes com.vqsv.rebuild.Main --check`: PASS.
- `world_panel_full`: PASS `102/102`, output
  `rebuild_game/build_intro_demo/panel_closeout_408/world_panel_full`.
- `panel_wheel`: PASS `16/16`, output
  `rebuild_game/build_intro_demo/panel_closeout_408/panel_wheel`.

Visual spot-checks:

- `panel_gamemenu_open_from_world.png`: menu frame, row highlight, and bottom
  softkeys are visible.
- `panel_save_prompt_from_gamemenu.png`: `msgtip.ui` prompt box and corner
  check/X buttons are visible.
- `panel_badge_open_from_record.png`: badge grid/detail screen is visible.
- `panel_petmap_missing_item_warning_from_record.png`: runtime verifies the
  missing-item warning text before closing it, but the final saved PNG is the
  post-close `record.ui` state. If we need visual regression for this warning,
  add a dedicated checkpoint that captures before the close.

## Closeout Status

Right-softkey `gamemenu.ui` rows `0..5`: `PORTED/PARTIAL`.

This means:

- Every visible top-level `gamemenu.ui` row has a routed, smoke-covered rebuild
  path.
- The early-game practical panel loop can be tested without known hard blocks
  in pet, bag, record, task, or save.
- Remaining debt is now mostly exact source-data parity, pixel/timing polish,
  and deeper lifecycle coverage rather than missing top-level routes.

## Remaining Debt To Track

- `shopbuy.ui`: original SMS/currency-type-2 flow is intentionally removed for
  PC rebuild; full original economy parity is not claimed.
- `petstate.ui`: full storage/bank/stat lifecycle parity remains wider than
  the panel route closeout.
- `bag.ui`: q.O transmit actual map load/resume is deferred; several special
  item side effects remain route-specific partials.
- `record.ui`: exact counters/playtime are partial/approx.
- `petmap.ui`: exact encyclopedia arrays/lifecycle are partial.
- `badge.ui`: exact `q.C[8][2]` state parity is partial.
- `task.ui`: exact marquee/crop/widget VM is partial.
- `msgtip.ui`: save format is rebuild-native, not byte-identical original.
- No panel screen in this closeout is claimed pixel-perfect without original
  source-client pixel comparison.

## Recommended Next Step

Next concrete step: inspect the latest `world_panel_full` PNG output and choose
the worst visible panel mismatch for one focused polish slice.

Recommended first candidates:

1. `badge q.C exact state parity` if the user wants logic/data correctness.
2. `record.ui counter/playtime parity` if the visible record screen looks wrong.
3. `task.ui marquee/crop polish` if text rendering is the biggest visible debt.

Do not add new generic panel architecture until a concrete route proves the
current source-shaped renderer cannot support it.
