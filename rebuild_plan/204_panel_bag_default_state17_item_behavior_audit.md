# 204 - Panel Bag Default State17 Item Behavior Audit

Date: 2026-07-10

Scope: audit remaining `game.b.x/w` item behaviors for top-level `bag.ui b=0 -> state17`, then add route-backed smoke coverage where current source inventory actually reaches the item.

## Source state17 mechanics recap

Source path:

- `game.h.ac()` default `bag.ui b=0` item row:
  - `this.s = itemId`;
  - `this.o.a((byte)17)`;
  - close `/data/ui/bag.ui`.
- `game.k` state 17 entry:
  - `S.l = false`;
  - `S.c = 0`;
  - `S.W()` opens `/data/ui/petstate.ui`.
- `game.h.Z()` confirm calls `bo()`.
- `bo()`:
  - validates selected pet via `game.b.x(this.s)`;
  - checks inventory via `q.b(this.s, 1, (byte)0)`;
  - applies item via `game.b.w(this.s)`;
  - refreshes `petstate.ui`;
  - opens `msgwarm.ui`.

Status in rebuild:

- PORTED/PARTIAL: state17 open/navigation/back.
- PORTED/PARTIAL: validation warnings.
- PORTED/PARTIAL: success/mutation/missing-count for modeled item behaviors.

## Source item table `aq.c[4]`

Dump source: `modules/root_misc/original/db.mid`, group 4 via `VqsvBattleTables`.

| itemId | behavior | params | source meaning in `game.b.x/w` | current route/source inventory |
|---:|---:|---|---|---|
| 0 | 0 | 9999,0,0 | ball/catch item, top-level bag says cannot use | route-reachable room0 group0, battle P21 |
| 1 | 0 | 100,0,0 | ball/catch item, top-level bag says cannot use | route-reachable room0 group0, battle P21 |
| 2 | 0 | 140,0,0 | ball/catch item, top-level bag says cannot use | source-supported, not current route-proven |
| 3 | 0 | 200,0,0 | ball/catch item, top-level bag says cannot use | source-supported, not current route-proven |
| 4 | 1 | 50,50,0 | HP heal | route-reachable room0 group0 and Elder reward |
| 5 | 1 | 100,0,0 | HP heal full | source-supported, not current route-proven |
| 6 | 2 | 25,0,0 | PP/skill value restore | source-supported, not current route-proven |
| 7 | 2 | 45,0,0 | PP/skill value restore full | source-supported, not current route-proven |
| 8 | 3 | 50,50,20 | HP + PP restore | source-supported, not current route-proven |
| 9 | 3 | 100,0,45 | HP + PP restore full | source-supported, not current route-proven |
| 10 | 5 | 0,0,0 | clear bad effects/debuffs | source-supported, not current route-proven |
| 11 | 4 | 50,50,20 | revive HP + PP | route-reachable Elder reward |
| 12 | 4 | 100,0,45 | revive full HP + PP | source-supported, not current route-proven |
| 13 | 10 | 500,0,0 | avoid-monster special branch in `game.h.ac()`, not state17 | route-supported special top-level branch |
| 14 | 9 | 0,0,0 | egg accelerator special branch in `game.h.ac()`, not state17 | route-supported special top-level branch |

No source row currently uses behavior `6` even though `game.b.x/w` has code for behavior 6 excitement/buff item.

## Current route inventory proof

Implemented scripts that add items:

- `Scene1Room0Group0Script`:
  - `op17Item(0, 0, 1)`;
  - `op17Item(0, 1, 2)`;
  - `op17Item(0, 4, 5)`.
- `Scene1Room0Group6ElderBattleScript`:
  - `op17Item(0, 4, 10)`;
  - `op17Item(0, 11, 2)`.

Current route-backed state17 coverage:

- item 4 behavior 1 HP heal: covered by state17 success/missing/warning smoke.
- item 11 behavior 4 revive: newly covered by state17 revive smoke.

Current route-backed non-state17 coverage:

- item 0/1 behavior 0 ball: top-level cannot-use and battle P21/P17 catch paths already covered elsewhere.
- item 13/14: special `game.h.ac()` branches already covered elsewhere.

## Smoke added in this slice

Route-backed item 11 revive behavior:

- `panel_bag_default_item_state17_revival_item11_success_msg`
- `panel_bag_default_item_state17_revival_item11_returns_petstate`

These prove:

- selected itemId stays `11`;
- dead pet HP `0` validates successfully because behavior is revive;
- `game.b.w` equivalent behavior `4` revives HP above zero;
- inventory count decrements;
- `msgwarm.ui` success appears;
- close returns to `petstate.ui`.

## Deferred behavior coverage

Not added yet because current route/source inventory does not prove these items are acquired in the current rebuilt route:

- behavior 2: item 6/7 PP restore.
- behavior 3: item 8/9 HP+PP restore.
- behavior 5: item 10 debuff clear.
- behavior 6: source code supports it, but source table has no row in current `aq.c[4]`.

Also note: `VqsvSourceOps.sourceItem()` metadata is currently source-backed only for items used by current routes/smoke (`0/1/4/11/13/14`). Before exposing items `5..10/12` through real panel inventory or shop, add source-backed metadata from `VqsvBattleTables.item(id)` so `bag.ui` does not render fallback `Item N`.

## Verification

Build/check:

- `build.ps1`: pass.
- `java -Dvqsv.modules=..\modules -jar build\libs\vqsv-liet-hoa-rebuild.jar --check`: pass.
- `java -Dvqsv.modules=..\modules -cp build\classes VqsvBattleDamageFormulaCheck`: pass.
- Java mojibake scan: no matches.
- `git diff --check`: no whitespace errors; CRLF warnings only.

Focused PNG smoke:

- `panel_bag_default_item_state17_revival_item11_success_msg`: pass.
- `panel_bag_default_item_state17_revival_item11_returns_petstate`: pass.

Regression PNG smoke:

- `panel_bag_default_item_state17_success_msg`: pass.
- `panel_bag_default_item_state17_success_returns_petstate`: pass.
- `panel_bag_default_item_state17_missing_count_warning`: pass.
- `panel_bag_default_item_state17_missing_count_returns_bag`: pass.
- `panel_bag_default_item_state17_warning_hp_full`: pass.
- `panel_bag_default_item_state17_warning_dead`: pass.
- `panel_bag_item13_success_msg`: pass.
- `panel_bag_item14_type0_success`: pass.
- `panel_petstate_petsetting_item_choice_success_msg`: pass.
- `panel_petstate_petsetting_item_choice_success_returns_petstate`: pass.
- `route_sophie_after_battle_branch`: pass.
- `route_bunny_after_battle_task`: pass.
- `route_elder_after_battle_reward_state`: pass.

## Next recommended step

Do not add synthetic PP/debuff/excitement behavior smoke as “done” yet.

Next small source-backed step should be one of:

1. Audit/port source metadata for items `5..12` from `VqsvBattleTables.item(id)` into `VqsvSourceOps.sourceItem()`, so future shop/panel inventory rows are not fallback text.
2. Audit battle shop `P11` item menu/source reachability, then add route-backed smoke for item 6/7/8/9/10/12 only if the shop/list source actually exposes them in the rebuilt route.
