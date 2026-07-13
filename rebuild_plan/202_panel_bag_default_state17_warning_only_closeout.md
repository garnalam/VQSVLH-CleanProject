# 202 - Panel Bag Default State17 Warning-Only Closeout

Date: 2026-07-10

Scope: second implementation slice for top-level `bag.ui b=0` default item-use state 17.

## Implemented

- PORTED/PARTIAL: `game.h.Z` confirm for state17 now calls source-shaped validation only.
- PORTED/PARTIAL: selected `itemId` comes from stored state17 `this.s` equivalent.
- PORTED/PARTIAL: selected pet comes from `petstate.ui` cursor `c` equivalent.
- PORTED/PARTIAL: validation uses existing source-shaped `BattleUnit.validateBattleItem(itemId)`, matching `game.b.x(itemId)` behavior.
- PORTED/PARTIAL: validation failures open `msgwarm.ui` and close back to `petstate.ui`.

Validated warning examples:

- `validation=2`: `Mau day, khong can su dung`.
- `validation=8`: `Sung vat da chet, khong the su dung`.

## Explicitly not implemented

- PENDING: successful validation path does not mutate pet yet.
- PENDING: `game.b.w(itemId)` application.
- PENDING: inventory check/remove through `q.b(this.s,1,0)` / `game.g.o().d`.
- PENDING: missing-count `f=2` path closes both `msgwarm.ui` and `petstate.ui`.

## Verification

Build/check:

- `build.ps1`: pass.
- `java -Dvqsv.modules=..\modules -jar build\libs\vqsv-liet-hoa-rebuild.jar --check`: pass.
- `java -Dvqsv.modules=..\modules -cp build\classes VqsvBattleDamageFormulaCheck`: pass.
- Java mojibake scan: no matches.
- `git diff --check`: no whitespace errors; CRLF warnings only.

Focused PNG smoke:

- `panel_bag_default_item_state17_warning_hp_full`: pass.
- `panel_bag_default_item_state17_warning_dead`: pass.
- `panel_bag_default_item_state17_warning_returns_petstate`: pass.

Regression PNG smoke:

- `panel_bag_default_item_state17_open_petstate`: pass.
- `panel_bag_default_item_state17_navigation`: pass.
- `panel_bag_default_item_state17_back_returns_bag`: pass.
- `panel_bag_item_cannot_use_warning`: pass.
- `panel_bag_item13_success_msg`: pass.
- `panel_bag_item14_type0_success`: pass.
- `panel_petstate_petsetting_item_choice_warning_hp_full`: pass.
- `panel_petstate_petsetting_item_choice_warning_returns_choice`: pass.
- `panel_petstate_petsetting_skill_open`: pass.
- `panel_save_success_status`: pass.
- `route_sophie_after_battle_branch`: pass.
- `route_bunny_after_battle_task`: pass.
- `route_elder_after_battle_reward_state`: pass.

## Next recommended slice

Implement state17 success/mutation path:

1. after `game.b.x(itemId)` returns `-1`, check source inventory count;
2. if missing, port `f=2` missing-count warning and return behavior;
3. if present, apply `game.b.w(itemId)` equivalent;
4. decrement inventory;
5. refresh `petstate.ui`;
6. show success `msgwarm.ui`;
7. smoke HP-heal success, count decrement, success close stays in `petstate.ui`, missing-count return behavior.
