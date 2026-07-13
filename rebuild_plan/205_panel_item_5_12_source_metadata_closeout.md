# 205 - Panel Item 5..12 Source Metadata Closeout

Date: 2026-07-10

Scope: source-back item metadata for `bag.ui`/state17 item rows. No item-use logic changes in this slice.

## Implemented

- `VqsvSourceOps.sourceItem(5..12)` now reads metadata from `VqsvBattleTables.item(id)` / source `aq.c[4]`.
- Metadata now uses source:
  - name text id;
  - icon cell;
  - description text id;
  - behavior field.
- Item `11` no longer uses the old hardcoded placeholder metadata; it now uses the source row like the rest of `5..12`.

## Not changed

- No new item-use logic.
- No new shop behavior.
- No synthetic PP/debuff item reachability claim.

## Verification

Build/check:

- `build.ps1`: pass.
- `java -Dvqsv.modules=..\modules -jar build\libs\vqsv-liet-hoa-rebuild.jar --check`: pass.
- `java -Dvqsv.modules=..\modules -cp build\classes VqsvBattleDamageFormulaCheck`: pass.
- Java mojibake scan: no matches.
- `git diff --check`: no whitespace errors; CRLF warnings only.

Focused PNG smoke:

- `panel_bag_item5_12_metadata_source_backed`: pass.

Regression PNG smoke:

- `panel_bag_open_from_gamemenu`: pass.
- `panel_bag_default_item_state17_revival_item11_success_msg`: pass.
- `panel_bag_default_item_state17_revival_item11_returns_petstate`: pass.
- `panel_bag_default_item_state17_success_msg`: pass.
- `panel_bag_default_item_state17_warning_hp_full`: pass.
- `panel_bag_item13_success_msg`: pass.
- `panel_bag_item14_type0_success`: pass.
- `route_sophie_after_battle_branch`: pass.
- `route_bunny_after_battle_task`: pass.
- `route_elder_after_battle_reward_state`: pass.

## Next recommended step

Audit battle shop `P11` item menu/source reachability:

- identify which item ids shop exposes;
- check whether item `6/7/8/9/10/12` become obtainable through a source route;
- only then add route-backed state17 smoke for PP restore, HP+PP restore, debuff clear, or full revive.
