# 411 - New Dev Chat Handoff: Evolution Material Shop Bridge

Date: 2026-07-16

Purpose: hand off the current panel/evolution-material work after closing the
`aq.c[3]` material bucket and `game.k.a(3,(byte)2)` material-shop runtime slice.
This document is the required starting point for the next dev chat before it
touches panel, shop, bag, or evolution code.

## Prime Rules

- Source first. Read CFR source, decoded event data, decoded `.ui` files, then
  inspect rebuild runtime before coding.
- Do not open the live client unless the user explicitly asks. Prefer headless
  smoke PNG.
- Do not hardcode local absolute paths in docs, code, or commands. Use repo
  relative paths and run commands from the correct working directory.
- Work in small slices: audit -> patch one route -> smoke PNG -> update docs ->
  state the next recommended step.
- Every status claim must be explicit: `PORTED`, `PORTED/PARTIAL`, `FIXED`,
  `APPROX`, `PENDING`, or `UNKNOWN`.
- Do not repurpose one source table as another. Numeric ids overlap across
  `aq.c` tables.
- Respect the dirty worktree. Do not revert unrelated changes.
- If a smoke fails, fix the root cause before moving on.
- At the end of every response, tell the user what should be done next.

## Project Layout

Repository root:

```text
.
+-- modules/       source assets, decoded data, CFR decompile
+-- rebuild_game/  Java rebuild runtime, renderer, smoke harness
+-- rebuild_plan/  audits, closeouts, handoffs, roadmap docs
```

Run build/smoke from `rebuild_game`.

Preferred PowerShell/CMD compatible command shape:

```bat
cd /d <repo-root>\rebuild_game
.\build.ps1
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke-checkpoint <checkpoint> ".\build_intro_demo\<dir>\<checkpoint>.png"
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke-suite <suite> ".\build_intro_demo\<dir>\<suite>"
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" com.vqsv.rebuild.Main --check
```

Important: use `.\build\classes` for smoke/check while actively developing.
Some older docs used jar or machine-specific absolute paths; treat those as
historical examples, not current required command style.

## Must Read First

Read these docs before coding:

1. `rebuild_plan/404_new_dev_chat_handoff_panel_softkey_after_bag.md`
2. `rebuild_plan/405_panel_record_dogiam_route_matrix.md`
3. `rebuild_plan/406_panel_record_petmap_item_gate_closeout.md`
4. `rebuild_plan/407_panel_save_msgtip_source_flow_closeout.md`
5. `rebuild_plan/408_panel_gamemenu_rows_0_5_closeout_matrix.md`
6. `rebuild_plan/409_panel_evolution_material_source_matrix.md`
7. `rebuild_plan/410_panel_evolution_material_acquisition_route_audit.md`
8. `rebuild_plan/394_panel_petsetting_evolve_row_deep_closeout.md`
9. `rebuild_plan/battle_engine_master_roadmap_progress.md`

Read these source/runtime files as needed for the next slice:

1. `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
2. `modules/source_code/decoded/decompiled_source_cfr/game/l.java`
3. `modules/source_code/decoded/decompiled_source_cfr/game/e.java`
4. `modules/event/decoded/data__event__scene_5.mid.json`
5. `modules/event/decoded/data__event__scene_11.mid.json`
6. `modules/ui/decoded/data__ui__shopbuy.ui.json`
7. `modules/ui/decoded/data__ui__bodyShop.ui.json`
8. `modules/ui/decoded/data__ui__bag.ui.json`
9. `modules/ui/decoded/data__ui__evolve.ui.json`
10. `rebuild_game/src/main/java/VqsvPanelRuntime.java`
11. `rebuild_game/src/main/java/VqsvSmokeHarness.java`
12. `rebuild_game/src/main/java/VqsvSourceOps.java`
13. `rebuild_game/src/main/java/VqsvSourceEvolutionRuntime.java`
14. `rebuild_game/src/main/java/VqsvIntroDemo.java`

Only load extra files when the current slice proves they are relevant.

## Current Roadmap State

High-level status:

| Area | Status | Notes |
| --- | --- | --- |
| Battle engine core Elder/Bunny route | `PORTED/PARTIAL` | Good enough for current route testing; do not reopen P7/P17 unless source-route mismatch or user asks. |
| Skill/effect groups 0..69 | `PORTED/PARTIAL` | S60 assets integrated; fire/wood/etc skill groups smoke-tested. Full closeout can wait. |
| World panel softkeys | `PORTED/PARTIAL` | Left/right softkey open panels; mouse click/wheel/hover improved for many lists. |
| `gamemenu.ui` rows 0..5 | `PORTED/PARTIAL` | Shop, pet, bag, record, task, save all have working source-shaped routes. |
| `bag.ui` tabs | `PORTED/PARTIAL` | Normal items, equipment, material, special tabs are split by source buckets. |
| `petstate.ui` / `petsetting.ui` | `PORTED/PARTIAL` | Active switch, item, equipment, release, skill, evolve rows are implemented enough for smoke. |
| `evolve.ui` material count/consume | `PORTED` for bucket logic | Uses `sourceMaterialItems` / `(byte)2` / `aq.c[3]`, not bag or reward buckets. |
| Opcode 18 material reward/remove | `PORTED` | `op18Material()` writes/removes `sourceMaterialItems`; id 17 stores `qty * 5`. |
| Material shop `game.k.a(3,(byte)2)` runtime | `PORTED/PARTIAL` | `shopbuy.ui` context exists and buy writes to `sourceMaterialItems`. |
| World actor/state bridge to material shop | `PENDING` | Next concrete slice. |

## Critical Source Truth: Evolution Materials

Do not break this.

```text
Evolution requirement:
  materialId   = aq.c[0][species][20] + 12
  materialNeed = aq.c[0][species][21]

Material inventory:
  source table = aq.c[3]
  source bucket = game.j / (byte)2 material/key vector
  rebuild bucket = Scene.sourceMaterialItems

Not valid:
  sourceBagItems id 12
  sourceSpecialRewards id 12
```

Example locked by smoke:

```text
68 Dien Mieu -> 69 Thiem Dien Mieu
required level = 12
materialId = 12
material = Tinh Nguyen Thach
need = 1
```

If the player has item id `12` in normal bag but `sourceMaterialItems[12] == 0`,
evolution must still show missing material. That is correct.

## Material Acquisition Routes

Source routes audited:

| Route | Source | Status | Rebuild behavior |
| --- | --- | --- | --- |
| Event opcode 18 mode 0 add | `game.e case 18` | `PORTED` | `Scene.op18Material(0,id,qty)` adds to `sourceMaterialItems`. |
| Event opcode 18 mode 1 remove | `game.e case 18` | `PORTED` | `Scene.op18Material(1,id,qty)` removes from `sourceMaterialItems`. |
| Decoded scene 5 | `scene_5.mid.json [0,15,1]` | `AUDITED` | Awards Hon Tinh Thach x1 when route is consumed. |
| Decoded scene 11 | `scene_11.mid.json [0,12,2]` | `AUDITED` | Awards Tinh Nguyen Thach x2 when route is consumed. |
| Material shop | `game.l actor type20/state32 -> game.k.a(3,(byte)2)` | `PORTED/PARTIAL` runtime | Opens `shopbuy.ui` over `aq.c[3]`, confirm-buy writes to `sourceMaterialItems`. |
| Actual world actor bridge | `game.l` interaction path | `PENDING` | Need to connect map actor/type/state route to existing material shop runtime. |

Important distinction:

```text
PC bodyShop normal item shop:
  game.k.a(byte4,0)
  table aq.c[4]
  bucket sourceBagItems

Material stone shop:
  game.k.a(3,(byte)2)
  table aq.c[3]
  bucket sourceMaterialItems
```

Do not merge these.

## Current Verification Baseline

Known-good commands:

```bat
cd /d <repo-root>\rebuild_game
.\build.ps1
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke-checkpoint source_op18_material_add_remove_bucket ".\build_intro_demo\material_acquisition_410\source_op18_material_add_remove_bucket.png"
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke-checkpoint panel_material_shop_buy_tinh_nguyen_evolve_bucket ".\build_intro_demo\material_acquisition_410\panel_material_shop_buy_tinh_nguyen_evolve_bucket.png"
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke-checkpoint panel_petstate_petsetting_evolve_success_mutate ".\build_intro_demo\material_acquisition_410\panel_petstate_petsetting_evolve_success_mutate.png"
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke-checkpoint panel_petstate_petsetting_evolve_no_material_warning ".\build_intro_demo\material_acquisition_410\panel_petstate_petsetting_evolve_no_material_warning.png"
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke-checkpoint world_evolution_confirm_success_mutate ".\build_intro_demo\material_acquisition_410\world_evolution_confirm_success_mutate.png"
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke-suite panel_portable_shop ".\build_intro_demo\material_acquisition_410\panel_portable_shop_suite"
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" com.vqsv.rebuild.Main --check
```

Latest verified result:

```text
build.ps1: PASS
source_op18_material_add_remove_bucket: PASS
panel_material_shop_buy_tinh_nguyen_evolve_bucket: PASS
panel_petstate_petsetting_evolve_success_mutate: PASS
panel_petstate_petsetting_evolve_no_material_warning: PASS
world_evolution_confirm_success_mutate: PASS
panel_portable_shop suite: PASS 24/24
release check: PASS
```

## Next Concrete Slice

Create audit first:

```text
412_world_material_shop_actor_bridge_audit.md
```

Scope:

- Find the exact source world interaction path that reaches actor type `20` and
  state `32`.
- Confirm which maps/scenes/actors expose material shop in decoded event/map data.
- Confirm whether current rebuild has an actor interaction dispatcher that can
  route type/state-specific shops.
- Decide the smallest bridge:

```text
world actor/type route -> game.k.a(3,(byte)2) -> VqsvPanelRuntime material shop context
```

Do not implement a generic event VM or generic actor system in this slice.

## After The Audit

If audit proves a real reachable actor/state route:

1. Add a tiny bridge method in the current world interaction path.
2. Call the existing material shop runtime context.
3. Add smoke PNG:
   - route actor opens material `shopbuy.ui`
   - hover/wheel/select material row id 12
   - confirm-buy id 12
   - assert `sourceMaterialItems[12] += 1`
   - assert `sourceBagItems[12] == 0`
   - assert Dien Mieu evolve notice sees `1/1`
   - regression: `panel_portable_shop` suite still passes

Suggested checkpoint names:

```text
world_material_shop_actor20_open
world_material_shop_actor20_buy_id12_evolve_bucket
world_material_shop_actor20_back_returns_world
```

If no reachable route exists in current rebuild world:

- Do not fake it into the main route.
- Keep runtime-only checkpoint `panel_material_shop_buy_tinh_nguyen_evolve_bucket`
  as coverage.
- Write `PENDING world bridge` with exact source evidence.

## Current Debt To Preserve

- `bodyShop.ui` still contains PC free-all policy after SMS removal. Do not
  reintroduce SMS.
- Some panel UI renderers are source-shaped, not exact Java ME widget playback.
- Material shop runtime is source-shaped but world bridge is still pending.
- Exact evolution `ah type10` visual animation remains separate visual debt.
- Many generated PNG directories are untracked; do not delete them unless asked.
- The worktree is dirty from prior slices. Only edit files needed for the current
  slice.

## New Dev Exercise

Before coding, the new chat must report:

1. What is the difference between `aq.c[4]` shop and `aq.c[3]` material shop?
2. Why does normal bag item id `12` not satisfy Dien Mieu evolution?
3. Which smoke proves material shop buy feeds `sourceMaterialItems`?
4. What is still pending after `410`?
5. Which files would be touched for the next bridge slice, and which files
   should not be touched?

Expected answer:

```text
Next work is audit-only first: create 412_world_material_shop_actor_bridge_audit.md.
The code target, if source proves route, is a tiny world actor/type20/state32
bridge to existing VqsvPanelRuntime shopTable=3/shopBucket=2.
Do not alter bodyShop aq.c[4], do not reopen battle P7/P17, and do not
genericize the event VM.
```
