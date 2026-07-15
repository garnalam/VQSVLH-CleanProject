# 404 - New Dev Chat Handoff: Panel Softkey After Bag

Date: 2026-07-15

Purpose: hand off the current world/panel softkey work to a new dev chat after closing the `Lung bao / bag.ui` q.O transmit slice. This document is the required starting context for the next chat before it touches panel UI code.

## Prime Rules

- Source first. Read source code and decoded `.ui` data before changing behavior.
- Do not open the live game/client unless the user explicitly asks. Use headless smoke PNG only.
- Do not make large generic UI VM leaps unless the current slice proves it is necessary.
- Work in narrow slices: audit -> one route/feature port -> PNG smoke -> closeout doc -> report next step.
- Every claim must be classified: `PORTED`, `FIXED`, `PORTED/PARTIAL`, `APPROX`, `STUB`, `PENDING`, or `UNKNOWN`.
- Do not call anything pixel-perfect unless original-vs-rebuild pixel compare has been done.
- Do not revisit battle/skill animation unless the user explicitly moves back there.
- Respect dirty worktree. Do not revert unrelated changes.
- After every user prompt, answer what the next roadmap step should be.

## Project Roots

Current working area:

- Modules/assets/source: `C:\Users\Dell\Downloads\ResourcesVQSV\modules`
- Rebuild code: `C:\Users\Dell\Downloads\ResourcesVQSV\rebuild_game`
- Docs/roadmap: `C:\Users\Dell\Downloads\ResourcesVQSV\rebuild_plan`

Run smoke/build from `rebuild_game`.

Preferred commands:

```powershell
.\build.ps1
java "-Dvqsv.modules=C:\Users\Dell\Downloads\ResourcesVQSV\modules" -cp build\libs\vqsv-liet-hoa-rebuild.jar VqsvIntroDemo --smoke-suite panel_bag build_intro_demo\panel_bag_qo_case10_403
java "-Dvqsv.modules=C:\Users\Dell\Downloads\ResourcesVQSV\modules" -cp build\libs\vqsv-liet-hoa-rebuild.jar VqsvIntroDemo --check
```

Do not start `run.ps1` or open the playable client for this handoff exercise.

## Must Read First

Before coding, read these documents:

1. `rebuild_plan/366_new_dev_chat_handoff_world_panel_ui_routes.md`
2. `rebuild_plan/396_panel_bag_lung_bao_current_route_matrix.md`
3. `rebuild_plan/397_panel_bag_tab1_trang_suc_render_closeout.md`
4. `rebuild_plan/398_panel_bag_tab2_tai_lieu_render_closeout.md`
5. `rebuild_plan/399_panel_bag_tab3_dac_thu_qn_qo_split_closeout.md`
6. `rebuild_plan/400_panel_bag_qo_case6_badge_record_audit.md`
7. `rebuild_plan/401_panel_bag_qo_case6_badge_route_port_closeout.md`
8. `rebuild_plan/402_panel_bag_qo_case10_transmit_audit.md`
9. `rebuild_plan/403_panel_bag_qo_case10_transmit_route_render_closeout.md`

Then read these source/runtime files:

1. `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
2. `modules/ui/decoded/data__ui__gamemenu.ui.json`
3. `modules/ui/decoded/data__ui__bag.ui.json`
4. `modules/ui/decoded/data__ui__record.ui.json`
5. `modules/ui/decoded/data__ui__petmap.ui.json`
6. `modules/ui/decoded/data__ui__badge.ui.json`
7. `rebuild_game/src/main/java/VqsvPanelRuntime.java`
8. `rebuild_game/src/main/java/VqsvSmokeHarness.java`
9. `rebuild_game/src/main/java/VqsvIntroDemo.java`

Only read extra files when the next slice needs them.

## Current Panel Roadmap State

World softkeys:

| Entry | Source UI/method | Rebuild status | Notes |
| --- | --- | --- | --- |
| Left softkey | `world.ui` -> `gamesystem.ui` | `PORTED/PARTIAL` | continue/help/settings/main menu route exists. |
| Right softkey | `world.ui` -> `gamemenu.ui` | `PORTED/PARTIAL` | main gameplay panel route. |

Right softkey `Menu tro choi` rows:

| Row | Label | Main UI | Status | Notes |
| ---: | --- | --- | --- | --- |
| `0` | `Tuy than cua hang` | `bodyShop.ui` | `PORTED/PARTIAL` | shop rows, payment/confirm, service products have been worked. |
| `1` | `Sung vat` | `petstate.ui` / `petsetting.ui` | `PORTED/PARTIAL` | pet tab, petsetting rows, item/equipment/release/skill/evolve have substantial work. |
| `2` | `Lung bao` | `bag.ui` | `PORTED/PARTIAL` | current branch just closed q.O case10 transmit route/render. |
| `3` | `Do giam` | `record.ui` | `PORTED/PARTIAL` | next recommended panel softkey branch. Needs deeper audit/closeout. |
| `4` | `Nhiem vu` | `task.ui` | `PORTED/PARTIAL` | UI/data binding improved; task lifecycle should stay separate. |
| `5` | `Luu du lieu` | `msgtip.ui` / save | `PORTED/PARTIAL` | save prompt/runtime exists. |

## Bag / q.O State To Remember

Do not lose this context:

- `bag.ui` tab `1` = `q.M` equipment/held item inventory from `aq.c[3]`.
- `bag.ui` tab `2` = `q.N` material/key inventory from `aq.c[3]`.
- `bag.ui` tab `3` = `q.O` special inventory from `aq.c[5]`.
- Avoid old wrong wording like `q.N case10` for tab 3. Case10 transmit is `q.O case10`.

Bag q.O route status:

| q.O id | Function | Status |
| ---: | --- | --- |
| `0` | egg row / hatch/openbox flow | `PORTED/PARTIAL` |
| `5` | ride route | `PORTED/PARTIAL` |
| `6` | badge route | `PORTED/PARTIAL` |
| `7/8/9` | state19 pet-target special use | `PORTED/PARTIAL` |
| `10` | transmit/teleport route | `PORTED/PARTIAL descriptor-level` |

Important for q.O case10:

- `403` added `Mode.TRANSMIT`.
- `transmit.ui` renders 7 source destinations.
- Back returns to `bag.ui` tab `3`.
- Confirm writes source tuple fields:
  - `sourceTransmitScene`
  - `sourceTransmitRoom`
  - `sourceTransmitX`
  - `sourceTransmitY`
  - `sourceTransmitG`
  - `sourceTransmitT = -1`
  - `sourceTransmitConfirmed = true`
- It does **not** yet perform actual map load/resume.
- The user chose to move to the next panel softkey instead of deepening actual transmit world load. Do not reopen this unless asked.

Latest q.O transmit smoke output:

`rebuild_game/build_intro_demo/panel_bag_qo_case10_403`

Verification:

- `.\build.ps1`: PASS
- `--smoke-suite panel_bag`: PASS `20/20`
- `--check`: PASS

## Next Recommended Branch

Move to the next right-softkey panel branch:

`405 - Panel Record / Do Giam Source Route Audit`

Scope: audit only first.

Why this is next:

- `Lung bao` branch is closed enough for now.
- The next menu row after bag is `Do giam`.
- `record.ui` already has source-shaped open/render/navigation/back, plus child routes into `petmap.ui` and `badge.ui`.
- It has not yet received a full current-state closeout after the newer bag/panel work.

Do not start by coding. First create an audit matrix.

## 405 Audit Requirements

Read source:

- `game.k.Q()` / record open
- `game.k.R()` / record input
- `game.k.P()` or nearby petmap open/render/input methods
- `game.k.W()` / badge open
- `game.k.X()` / badge input/back
- any helper used for record counters or petmap/badge data

Read UI:

- `record.ui`
- `petmap.ui`
- `badge.ui`

Create doc:

`405_panel_record_dogiam_route_matrix.md`

Matrix must include:

| Source state/UI | Input | Source method | Data source | Side effect | Next UI/state | Rebuild status |
| --- | --- | --- | --- | --- | --- | --- |

At minimum cover:

- right softkey `gamemenu.ui` row `3` -> `record.ui`
- `record.ui` option `0` -> `petmap.ui`
- `record.ui` option `1` -> `badge.ui`
- `petmap.ui` tab navigation
- `petmap.ui` row/detail render
- `petmap.ui` back to `record.ui`
- `badge.ui` grid navigation
- `badge.ui` back to `record.ui`
- bag-origin badge back remains separate and already covered by `401`

Data binding to classify:

- petmap data source: species ownership, tabs, sprites, names, counts
- badge data source: source exact `q.C[8][2]` vs current rebuild approximation
- record counters/text source

## Likely Code Slice After 405

Only after audit, choose one:

1. `406 - record.ui closeout smoke matrix`
   - add focused PNG smoke for open/navigation/back and route transitions.
   - no new behavior if audit says current runtime is already enough.

2. `406 - badge q.C exact state parity`
   - only if audit proves current `sourceBadges` approximation is too weak.
   - add exact source badge matrix, not just count approximation.

3. `406 - petmap ownership/detail parity`
   - only if petmap row data or sprite/status is visibly wrong.

Do not combine these.

## Known Risks / Debt

- Generic `game.h/game.k` widget runtime is still not 100%.
- Some UI renderers are source-shaped rather than exact Java ME widget playback.
- Long text often uses one-line marquee/clip in rebuild. Do not wrap unless source wraps.
- `transmit.ui` long destination names are source-widget-width clipped/marquee. Do not widen layout unless original source proves it.
- `badge.ui` exact state source is `q.C[8][2]`; rebuild still has count/approx pieces in some routes.
- Actual world teleport resume from transmit confirm is deferred.

## Compulsory New Chat Exercise

Before coding, the new dev chat must answer in Vietnamese without guessing:

1. Tom tat chinh xac split `q.M / q.N / q.O`.
2. Noi ro `q.O case10` hien da port den muc nao va phan nao co y chua lam.
3. Chung minh tu source route `gamemenu.ui` row `3` mo `record.ui` bang method nao.
4. Liet ke `record.ui` co may lua chon chinh va tung lua chon di den UI nao.
5. De xuat PNG smoke set cho `Do giam` ma khong mo live client.
6. Noi ro khong duoc dong vao battle/skill/transmit-world-load trong slice nay.

If the new chat cannot answer these, it must read more source/docs before coding.

## Reporting Format For Next Chat

After each slice, report:

- `Changed:` files/functions touched.
- `Source proof:` source methods, table rows, widget ids.
- `Status:` `PORTED`, `PORTED/PARTIAL`, `FIXED`, or `PENDING`.
- `Smoke PNG:` output folder and checkpoint list.
- `Regression:` exact pass/fail commands.
- `Remaining:` honest debt.
- `Next:` one concrete roadmap step.
