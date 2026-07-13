# 203 - Panel Bag Default State17 Success/Mutation Closeout

Date: 2026-07-10

Scope: complete source-shaped mutation slice for top-level `bag.ui b=0` default item-use state 17.

## Implemented

- PORTED/PARTIAL: after state17 `game.b.x(itemId)` validation returns `-1`, runtime checks source inventory with `q.b(this.s,1,0)` equivalent.
- PORTED/PARTIAL: missing-count path opens `msgwarm.ui` with `Da khong co dao nay cu, thinh mua sam`, tracks source `f=2`.
- PORTED/PARTIAL: closing missing-count warning closes both `msgwarm.ui` and `petstate.ui`, then returns to `bag.ui/state8`.
- PORTED/PARTIAL: successful use applies `game.b.w(itemId)` equivalent via `BattleUnit.applyBattleItem(itemId)`.
- PORTED/PARTIAL: successful use decrements inventory via source bag model.
- PORTED/PARTIAL: successful use persists updated pet state and refreshes `petstate.ui` via state17 `e(c)` equivalent.
- PORTED/PARTIAL: successful use opens `msgwarm.ui` with `Thanh cong su dung dao cu`, tracks source `f=1`.
- PORTED/PARTIAL: closing success warning stays in `petstate.ui`.

## Remaining caveat

- PORTED/PARTIAL, not pixel-perfect/full-runtime: this uses the rebuild `msgwarm.ui` renderer and source-shaped `BattleUnit` item application. It is not a full port of every internal `game.b` field side effect beyond the fields currently modeled in rebuild pet state.

## Verification

Build/check:

- `build.ps1`: pass.
- `java -Dvqsv.modules=..\modules -jar build\libs\vqsv-liet-hoa-rebuild.jar --check`: pass.
- `java -Dvqsv.modules=..\modules -cp build\classes VqsvBattleDamageFormulaCheck`: pass.
- Java mojibake scan: no matches.
- `git diff --check`: no whitespace errors; CRLF warnings only.

Focused PNG smoke:

- `panel_bag_default_item_state17_success_msg`: pass.
- `panel_bag_default_item_state17_success_returns_petstate`: pass.
- `panel_bag_default_item_state17_missing_count_warning`: pass.
- `panel_bag_default_item_state17_missing_count_returns_bag`: pass.

Regression PNG smoke:

- `panel_bag_default_item_state17_open_petstate`: pass.
- `panel_bag_default_item_state17_navigation`: pass.
- `panel_bag_default_item_state17_back_returns_bag`: pass.
- `panel_bag_default_item_state17_warning_hp_full`: pass.
- `panel_bag_default_item_state17_warning_dead`: pass.
- `panel_bag_default_item_state17_warning_returns_petstate`: pass.
- `panel_bag_item_cannot_use_warning`: pass.
- `panel_bag_item13_success_msg`: pass.
- `panel_bag_item14_type0_success`: pass.
- `panel_petstate_petsetting_item_choice_warning_hp_full`: pass.
- `panel_petstate_petsetting_item_choice_warning_returns_choice`: pass.
- `panel_petstate_petsetting_item_choice_success_msg`: pass.
- `panel_petstate_petsetting_item_choice_success_returns_petstate`: pass.
- `panel_petstate_petsetting_skill_open`: pass.
- `panel_save_success_status`: pass.
- `route_sophie_after_battle_branch`: pass.
- `route_bunny_after_battle_task`: pass.
- `route_elder_after_battle_reward_state`: pass.

## Next recommended step

Audit remaining top-level `bag.ui` default/state branches after state17:

- identify which default item behaviors are route-reachable from source table;
- verify whether items with behavior `2/3/4/5/6` need route-specific smoke;
- then choose the next smallest slice, likely state17 revive/PP/debuff/excitement item behavior smoke coverage rather than new UI code.
