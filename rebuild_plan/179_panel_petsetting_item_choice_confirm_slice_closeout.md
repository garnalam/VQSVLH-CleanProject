# 179 - Panel petsetting c=0 item-use confirm slice

Date: 2026-07-10

## Scope

Implemented the next slice after `178`:

- `petsetting c=0 -> choice.ui`
- confirm selected item
- source-shaped validation warning loop
- source-shaped success loop
- apply/consume low-risk item behavior through existing battle item runtime

This is still panel pet item-use only. It does not port equipment `petsetting c=2`.

## Source Chain

Primary source:

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`

Confirmed source path:

- In `game.h.X()` case `c=0`, while `f == 2` and confirm:
  - selected row comes from `q.J.elementAt(r)`
  - item `13/14` opens `/data/ui/msgwarm.ui` with `Dao cu nay khong the su dung`
  - validation calls `q.z[b].x(itemId)`
  - validation `0/1/2/3/4/5/7/8` each opens `/data/ui/msgwarm.ui`
  - success calls `q.z[b].w(itemId)`, refreshes `e(b)`, sets `f = 4`, opens `msgwarm.ui`, closes `choice.ui`
- While warning `f == 3`, confirm closes `msgwarm.ui` and returns to `f = 2`.
- While success `f == 4`, confirm closes `msgwarm.ui` and returns to `f = 0`.
- In `game.b.x(itemId)`, validation is driven by `aq.c[4][itemId][5]`.
- In `game.b.w(itemId)`, behavior `1..6` mutates HP/PP/status/excited state and consumes one item through `game.g.o().d(itemId, 1, (byte)0)`.

## Rebuild Mapping

Implemented in:

- `rebuild_game/src/main/java/VqsvIntroDemo.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`
- `rebuild_game/src/main/java/VqsvText.java`

Runtime behavior:

- Confirm on an unusable/full state opens `TextBox.msgWarm(...)`, keeps `choice.ui` active, and stores `sourceItemChoiceMessageMode = 3`.
- Confirm on warning closes the message and returns to `choice.ui`.
- Confirm on success:
  - validates through `BattleUnit.validateBattleItem(itemId)`
  - applies through `BattleUnit.applyBattleItem(itemId)`
  - consumes through `VqsvSourceOps.sourceRemoveItem(...)`
  - persists back to `SourcePetState`
  - refreshes `petstate.ui`
  - opens success `msgwarm`
  - stores `sourceItemChoiceMessageMode = 4`
- Confirm on success message closes `msgwarm` and stays at `petstate.ui`.

## Status

- `PORTED/PARTIAL`: source `f=2/f=3/f=4` item confirm loop.
- `PORTED/PARTIAL`: validation result mapping from `game.b.x(itemId)` via `BattleUnit.validateBattleItem`.
- `PORTED/PARTIAL`: item behavior `1..6` via existing `BattleUnit.applyBattleItem`.
- `PORTED/PARTIAL`: item consume through rebuild source bag state.
- `PENDING`: exact source `q.J` vector runtime; rebuild still projects from `sourceBagItems bagChannel != 0`.
- `PENDING`: exact source item table text/icon parity for every item id.
- `PENDING`: `msgwarm.ui` text layout is still source-backed partial; smoke proves loop/state, not pixel-perfect long-text wrapping.
- `PENDING`: original-vs-rebuild pixel compare.
- `PENDING`: `petsetting c=2` equipment/q.L.

## Verification

Build/check:

- `build.ps1` pass
- `com.vqsv.rebuild.Main --check` pass
- `VqsvBattleDamageFormulaCheck` pass
- `git diff --check` pass with existing CRLF warnings only
- mojibake scan Java source: no output

Focused PNG smoke:

- `panel_petstate_petsetting_item_choice_open`
- `panel_petstate_petsetting_item_choice_navigation`
- `panel_petstate_petsetting_item_choice_back_returns_petstate`
- `panel_petstate_petsetting_item_choice_warning_hp_full`
- `panel_petstate_petsetting_item_choice_warning_returns_choice`
- `panel_petstate_petsetting_item_choice_success_msg`
- `panel_petstate_petsetting_item_choice_success_returns_petstate`

Regression PNG smoke:

- `panel_petstate_petsetting_skill_open`
- `panel_petstate_petsetting_skill_back_returns_petstate`
- `panel_bag_open_from_gamemenu`
- `panel_save_success_status`
- `panel_gamesystem_option_confirm_no_returns_gamesystem`
- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`

## Next

Recommended next slice:

1. Audit and port `petsetting c=2 -> equipment choice.ui` source path.
2. Prove `q.L` equipment vector source shape, selected pet equipment slot mapping, and validation/warning loop before coding.
3. Keep `msgwarm.ui` layout polish as a separate visual-debt slice, because it affects multiple panel/battle warnings.
