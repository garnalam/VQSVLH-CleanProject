# 194 - Panel bag itemId 14 egg accelerator closeout

## Scope

Implemented the narrow source-backed `bag.ui` itemId `14` branch from `game.h.ac()`.

This slice only ports the top-level bag item-use warning/success loop:

- source `q.k(0)` active egg check;
- source `q.I` egg type threshold;
- source `game.k.q` hatch-progress cap;
- source `q.d(item,1,0)` item consume;
- source `msgwarm.ui` success/warning messages;
- return to `bag.ui` after confirming the warning.

This slice does not port the actual hatch action in `bag.ui b == 3`, and does not port after-battle progress increment.

## Source-backed state

Added rebuild fields:

| Rebuild | Source | Status |
| --- | --- | --- |
| `sourceEggActive` | `q.k(0)` over `q.N` row `[0,1,*]` | PORTED/PARTIAL |
| `sourceEggType` | `q.I` | PORTED/PARTIAL |
| `sourceEggProgress` | `game.k.q` | PORTED/PARTIAL |

These fields are now saved/loaded in `VqsvSaveRuntime`, matching source persistence of `q.I` and `game.k.q`.

## Item 14 behavior

Source branch:

- If no active egg: warning `Không có trứng có thể ấp trứng`, no item mutation.
- If `q.I == 0 && game.k.q < 10`: remove one item 14, set `game.k.q = 10`, show success.
- If `q.I > 0 && game.k.q < 30`: remove one item 14, set `game.k.q = 30`, show success.
- If already ready: source top-level condition fails, so it shows the same warning and does not consume item.

Rebuild maps this as:

- `sourceEggActive == false` -> warning.
- `sourceEggType == 0`, `sourceEggProgress < 10` -> success, progress `10`.
- `sourceEggType > 0`, `sourceEggProgress < 30` -> success, progress `30`.
- progress already at threshold -> warning.

## Source metadata

`sourceItem(14)` now reads `VqsvBattleTables.instance().item(14)`:

- `nameTextId=277`
- `iconId=41`
- `descriptionTextId=294`
- `bagChannel/behavior=9`

Also fixed `VqsvPanelRuntime.bagRows()` to reflect source `q.K + q.J` for top-level bag tab. This is required because item 14 lives in source `q.J` (`aq.c[4][14][5] = 9`), but source `game.h.ac()` and `bk()` operate on `q.K + q.J`.

## Files touched

- `rebuild_game/src/main/java/VqsvIntroDemo.java`
- `rebuild_game/src/main/java/VqsvSaveRuntime.java`
- `rebuild_game/src/main/java/VqsvSourceOps.java`
- `rebuild_game/src/main/java/VqsvText.java`
- `rebuild_game/src/main/java/VqsvPanelRuntime.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

## Smoke

Focused PNG checkpoints:

- `panel_bag_item14_no_egg_warning`
- `panel_bag_item14_type0_success`
- `panel_bag_item14_type1_success`
- `panel_bag_item14_already_ready_warning`
- `panel_bag_item14_success_returns_bag`

Output:

- `rebuild_game/build/smoke/panel_bag_item14/`

Regression smoke:

- bag open/navigation/back
- item `0..3` cannot-use
- item 13 success/already/forbidden
- petsetting active/evolve/release/item/equipment/skill
- save prompt
- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`

Output:

- `rebuild_game/build/smoke/panel_bag_item14_regression/`

## Verification

Passed:

- `rebuild_game/build.ps1`
- `java "-Dvqsv.modules=..\modules" -jar build\libs\vqsv-liet-hoa-rebuild.jar --check`
- `java "-Dvqsv.modules=..\modules" -cp build\classes VqsvBattleDamageFormulaCheck`
- `rg -n "Ã|Â|�" rebuild_game/src/main/java`
- `git diff --check`
- focused PNG smoke
- regression PNG smoke

Note: `git diff --check` only reported existing CRLF conversion warnings from Git.

## Current status

`bag.ui itemId 14 egg accelerator`: PORTED/PARTIAL.

Remaining pending:

- after-battle increment of `game.k.q`;
- `bag.ui b == 3` hatch action;
- `game.k.r()` full hatch-ready UI path;
- hatch species RNG and add to bag/bank/release result;
- exact `q.N` runtime UI for egg row.

## Recommended next

Audit and port the separate egg hatch action:

- `bag.ui b == 3`, `q.N case 0`;
- `game.k.r()`;
- `q.y()` space result;
- `q.j(0)` close egg state;
- species `58` path for `q.I == 0`;
- weighted random species path for `q.I > 0`;
- result openbox text and bag/bank/release mutation.
