# 231 - Battle/panel item full table audit

Date: 2026-07-13

Scope: source-first audit for normal item rows `aq.c[4]`. This is a planning
document only; no code changes are made here. Goal is to stop treating item
logic/descriptions as generic mock behavior and instead port each row from the
source table.

## Source anchors

- `modules/script/decoded/data__script__db.mid.json`
  - group `4` is the item table `aq.c[4]`.
- `modules/script/decoded/data__script__chs.mid.json`
  - item name text id is `aq.c[4][id][0]`.
  - item description text id is `aq.c[4][id][2]`.
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
  - bag/petsetting item lists render description from
    `an.f(aq.c[4][itemId][2])`.
  - `bo()` validates with `game.b.x(itemId)`, consumes with
    `game.g.b/d(...)`, then applies `game.b.w(itemId)`.
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
  - `w(int itemId)` applies behavior using row params.
  - `x(int itemId)` validates target and returns warning code.

## Source item table

Columns:

- `row`: raw `aq.c[4][id]`.
- `ch`: source bucket/channel from `row[4]`.
- `beh`: behavior from `row[5]`.
- `p6/p7/p8`: behavior parameters from `row[6..8]`.

| id | name | desc | row | ch | beh | params | Source meaning |
|---:|---|---|---|---:|---:|---|---|
| 0 | Tất Trung Cầu | Bất luận là sủng vật hoang dã nào thì xác suất bắt được là 100%. | `[261,25,278,2,2,0,9999]` | 2 | 0 | catch `9999` | Ball/catch item. Not usable through `game.b.w`. |
| 1 | Phong ấn cầu | Cầu thông thường có được nhiều sủng vật; | `[262,26,279,50,0,0,100]` | 0 | 0 | catch `100` | Ball/catch item. |
| 2 | Cao cấp cầu | Cầu may mắn bắt được sủng vật hiếm; | `[263,27,280,200,0,0,140]` | 0 | 0 | catch `140` | Ball/catch item. |
| 3 | Đại sư cầu | Chỉ một số ít sủng vật cần phải có cầu cao cấp mới bắt được; | `[264,28,281,1,1,0,200]` | 1 | 0 | catch `200` | Ball/catch item / premium source type. |
| 4 | Bánh Sandwich | Khôi phục một phần trị số sinh mạng sủng vật; | `[265,29,282,100,0,1,50,50]` | 0 | 1 | HP `50%, +50` | Heal HP by `maxHp * 50 / 100 + 50`, then clamp. Elder/group0 reward item. |
| 5 | Chocolate | Khôi phục toàn bộ trị số sinh mạng sủng vật; | `[266,30,283,250,0,1,100,0]` | 0 | 1 | HP `100%, +0` | Heal HP by `maxHp`. |
| 6 | Năng lượng Thạch | Khôi phục một phần giá trị kỹ năng của sủng vật; | `[267,31,284,100,0,2,25]` | 0 | 2 | PP `25` | Restore each skill PP/value by 25. |
| 7 | Tụ Năng Thạch | Khôi phục toàn bộ giá trị kỹ năng của sủng vật; | `[268,32,285,250,0,2,45]` | 0 | 2 | PP `45` | Restore each skill PP/value by 45. |
| 8 | Khôi phục tề | Khôi phục một phần trị số sinh mạng cùng giá trị kỹ năng; | `[269,33,286,250,0,3,50,50,20]` | 0 | 3 | HP `50%, +50`; PP `20` | Restore HP and PP partially. |
| 9 | Siêu năng tề | Khôi phục toàn bộ trị số sinh mạng cùng giá trị kỹ năng; | `[270,34,287,500,0,3,100,0,45]` | 0 | 3 | HP `100%, +0`; PP `45` | Restore HP and PP strongly/full-ish. |
| 10 | Vạn năng dược | Loại bỏ bớt sủng vật; | `[271,35,288,50,0,5]` | 0 | 5 | none | Clear bad effects/debuffs with `game.b.C()`. |
| 11 | Sinh mệnh thạch | Khi sủng vật sống lại, trị số sinh mạng cùng kỹ năng sẽ được khôi phục; | `[272,36,289,300,0,4,50,50,20]` | 0 | 4 | revive HP `50%, +50`; PP `20` | Revive dead pet, set HP, restore PP. Elder reward item. |
| 12 | Linh Hồn Thạch | Sủng vật sống lại, khôi phục toàn bộ trị số sinh mạng cùng kỹ năng; | `[273,37,290,750,0,4,100,0,45]` | 0 | 4 | revive HP `100%, +0`; PP `45` | Strong revive. |
| 13 | Tị quái hàn | Trong thời gian ngắn không gặp kẻ địch | `[509,53,510,300,0,10,500]` | 0 | 10 | avoid `500` | Top-level bag special branch, not `game.b.w`. |
| 14 | Gia tốc dược | Lập tức cho ấp trứng, không cần chờ đợi, sẽ nhanh chóng được thấy sủng vật mới. | `[277,41,294,1,1,9]` | 1 | 9 | none | Top-level egg accelerator branch, not `game.b.w`. |

## Source apply formula

From `game.b.w(int itemId)`:

| behavior | Source formula |
|---:|---|
| 0 | No battle/pet use; `game.b.x` returns warning code `6`. Catch logic uses `game.d.b(itemId)` and item param `[6]`. |
| 1 | `heal = maxHp * row[6] / 100 + row[7]`; add to current HP; display heal amount with `l(heal)`. |
| 2 | `restorePp = row[6]`; call `B(restorePp)`. |
| 3 | `heal = maxHp * row[6] / 100 + row[7]`; `restorePp = row[8]`; apply both. |
| 4 | revive/reset with `c()`; set HP to `maxHp * row[6] / 100 + row[7]`; restore PP `row[8]`. |
| 5 | clear debuffs/bad effects with `C()`. |
| 6 | set excitement/state flag `d[6] = 2`. No current item row uses behavior 6. |
| 9 | egg accelerator, handled by top-level bag branch. |
| 10 | avoid-monster item, handled by top-level bag branch. |

Important example: item `4` Bánh Sandwich is **not** full heal. It heals
`maxHp * 50 / 100 + 50`. It only becomes full if the missing HP is less than or
equal to that amount.

## Source validation / warning code

From `game.b.x(int itemId)` and `game.h.bo()`:

| return | Meaning / source warning |
|---:|---|
| `-1` | OK to use. |
| `1` | Revive item selected but pet is alive: `Sủng vật này không có, không thể sử dụng`. |
| `2` | HP already full: `Máu đầy, không cần sử dụng`. |
| `3` | PP/skill value already full: `Kỹ năng giá trị đã đầy, không cần sử dụng`. |
| `4` | No debuff/bad effect to clear: `Trên người đều bị lợi hiệu quả`. |
| `5` | Already excited/state flag: `Trong hưng phấn, không thể dùng`. |
| `6` | Behavior 0/catch ball is not usable here. |
| `7` | Both HP and PP are full. |
| `8` | Pet is dead and item is not revive behavior. |

## Current rebuild mapping / gaps

| Area | Current state | Gap |
|---|---|---|
| `BattleUnit.applyBattleItem()` | Uses behavior formulas for `1/2/3/4/5/6`, including `maxHp * paramA / 100 + paramB`. | Needs wider smoke coverage per item and per route to prove it never falls back to full-heal behavior. |
| `BattleUnit.validateBattleItem()` | Mirrors the major source warning codes. | Needs source-message parity review for code `1/4/5/6/8`; some text exists but should be tied to source strings. |
| `VqsvSourceOps.sourceItem()` | PORTED for `0..14`: table-first through `BattleItemRow`, including name text id, icon id, description text id, description text, and behavior. | Fallback only applies if a source table row is missing. |
| `bag.ui` description render | `VqsvPanelRuntime` renders `SourceItem.description`; item `0..4` metadata is now source-backed. | Formula/usage behavior still needs row-by-row smoke. |
| `petsetting choice.ui` descriptions | Uses `SourceItem.description`; item metadata now comes from the same `BattleItemRow` path. | Needs later UI smoke on concrete item-use routes. |
| battle P4/P16 item descriptions | Battle renderer uses `BattleItemRow.description`, which is source-backed. | Needs smoke/pixel assert for selected description text, especially item 4/8/11. |
| item reward routes | group0 gives item `4 x5`; elder gives item `4 x10` and `11 x2`. | Logic for using those rewards must be tested with exact HP values, not merely success/fail. |

## Recommended code slices

Do not port every item ad hoc. Use source behavior groups and smoke concrete
rows.

1. **Description parity slice**
   - Replace `VqsvSourceOps.sourceItem()` special hardcoding for `0/1/4` with
     `sourceTableItem(...)`.
   - Add explicit support for item `2/3`.
   - Smoke bag/petsetting descriptions for item `0/1/2/3/4`.

   2026-07-13 update: PORTED for `sourceItem()` and bag metadata smoke.
   Checkpoint:
   `build_intro_demo/panel_bag_item0_4_metadata_source_backed.png`.

2. **Item 4 formula slice**
   - Add focused smoke for Bánh Sandwich in P16, state17, and petsetting item
     choice.
   - Use at least two HP setups:
     - low enough to not fully heal, proving formula does not auto-full.
     - high enough that clamp to max is expected.
   - Expected formula: `hpAfter = min(maxHp, hpBefore + maxHp * 50 / 100 + 50)`.

   2026-07-13 update: PORTED/SMOKE-LOCKED for item `4` on the checked
   routes. Source row `aq.c[4][4] = [265,29,282,100,0,1,50,50]` is used by
   `BattleUnit.applyBattleItem()`, so HP heal is `maxHp * 50 / 100 + 50`.
   Smoke checkpoints:
   - `build_intro_demo/item4_formula_battle_p16_low.png`:
     battle P16 low HP, `1 -> 118/134`, not full.
   - `build_intro_demo/item4_formula_battle_p16_clamp.png`:
     battle P16 near-full HP clamps to max.
   - `build_intro_demo/item4_formula_state17_low.png`:
     panel bag state17 low HP.
   - `build_intro_demo/item4_formula_state17_clamp.png`:
     panel bag state17 near-full clamp.
   - `build_intro_demo/item4_formula_petsetting_low.png`:
     petsetting item choice low HP.
   - `build_intro_demo/item4_formula_petsetting_clamp.png`:
     petsetting item choice near-full clamp.

3. **Behavior-group formula slice**
   - Behavior 1: item `4`, `5`.
   - Behavior 2: item `6`, `7`.
   - Behavior 3: item `8`, `9`.
   - Behavior 4: item `11`, `12`.
   - Behavior 5: item `10`.
   - Top-level special: item `13`, `14`.

   2026-07-13 update: PORTED/SMOKE-LOCKED for behavior groups `1..5`
   on the checked runtime paths.
   - Unit formula check covers item `4..12` normal behavior rows.
   - Battle P16 smoke covers item `5/6/7/8/9/10/11/12`.
   - Panel bag state17 smoke covers item `5/6/7/8/9/10/11/12`.
   - Petsetting item-choice smoke covers item `5/6/7/8/9/10/11/12`.
   Representative PNGs:
   - `build_intro_demo/item_behavior_formula_battle_p16_item5_heal_full.png`
   - `build_intro_demo/item_behavior_formula_battle_p16_item7_pp_restore_full.png`
   - `build_intro_demo/item_behavior_formula_battle_p16_item9_hp_pp_full.png`
   - `build_intro_demo/item_behavior_formula_battle_p16_item12_revive_full.png`

4. **Route ownership slice**
   - Verify reward -> inventory -> use -> consume -> save/load for item `4`
     and item `11`, because both are story rewards.

   2026-07-13 update: PORTED/SMOKE-LOCKED for the elder story reward
   ownership path.
   - Source-backed route uses room0 group6 `op17 [0,4,10]` and
     `op17 [0,11,2]`.
   - Reward count smoke confirms item `4 = 10`, item `11 = 2`.
   - Use/consume smoke confirms item `4 -> 9` and item `11 -> 1`, with
     one consume per successful state17 use.
   - Save/load smoke confirms counts remain `9/1`; the smoke backs up and
     restores the existing autosave file around the checkpoint.
   Smoke PNGs:
   - `build_intro_demo/story_reward_item4_11_inventory_counts.png`
   - `build_intro_demo/story_reward_item4_11_use_consume.png`
   - `build_intro_demo/story_reward_item4_11_save_load_counts.png`

5. **Validation/message parity slice**
   - Close the sensitive warning codes called out after formula/ownership work:
     `1/4/5/6/8`.
   - Source anchors:
     - `game.b.x(int itemId)` returns the validation code.
     - `game.h.bo()` maps the validation code to the warning message.
   - Rebuild anchors:
     - battle P16: `VqsvBattleRuntime.itemWarning(...)`.
     - panel state17/petsetting: `VqsvIntroDemo.sourceItemChoiceWarning(...)`.

   2026-07-13 update: PORTED/SMOKE-LOCKED where source UI routes can reach
   the validation code.

   | code | status | checked routes | representative smoke |
   |---:|---|---|---|
   | `1` | PORTED/SMOKE-LOCKED. Revive item on alive pet warns with the source `NO_PET_TARGET` message. | battle P16, panel state17, petsetting item choice | `build_intro_demo/item_warning_code1_battle_revive_alive.png` |
   | `4` | PORTED/SMOKE-LOCKED. Clear-debuff item on a pet with no bad effect warns with source no-debuff text. | battle P16, panel state17, petsetting item choice | `build_intro_demo/item_warning_code4_battle_no_debuff.png` |
   | `5` | SOURCE-KNOWN / UNREACHABLE_BY_SOURCE_LIST. `game.b.x` and `game.h.bo` define the warning, but current `aq.c[4]` has no item row with behavior `6`, so no real item route can trigger it yet. | source table assertion only | `build_intro_demo/item_warning_code5_source_unreachable.png` |
   | `6` | PORTED for forced state17 behavior-0 validation. Battle P4/P16 and petsetting normal item lists do not expose ball/behavior-0 rows to `game.b.x`, so those routes are source-unreachable from the current UI list. | forced panel state17 | `build_intro_demo/item_warning_code6_state17_behavior0.png` |
   | `8` | PORTED/SMOKE-LOCKED. Non-revive item on dead pet warns with source dead-pet text. | battle P16, panel state17, petsetting item choice | `build_intro_demo/item_warning_code8_battle_dead_target.png` |

   Additional PNGs generated:
   - `build_intro_demo/item_warning_code1_state17_revive_alive.png`
   - `build_intro_demo/item_warning_code1_petsetting_revive_alive.png`
   - `build_intro_demo/item_warning_code4_state17_no_debuff.png`
   - `build_intro_demo/item_warning_code4_petsetting_no_debuff.png`
   - `build_intro_demo/item_warning_code8_state17_dead_target.png`
   - `build_intro_demo/item_warning_code8_petsetting_dead_target.png`

6. **Top-level special item 13/14 route parity slice**
   - Item `13` and `14` do not go through `game.b.w`; their source branches
     live directly in `game.h.ac()` under `/data/ui/bag.ui` tab `b == 0`.
   - Detailed closeout:
     `232_panel_bag_item13_14_special_route_parity_closeout.md`.

   2026-07-13 update: PORTED/SMOKE-LOCKED for the source-proven top-level
   bag routes.

   | item | route status | representative smoke |
   |---:|---|---|
   | `13` | PORTED/SMOKE-LOCKED for success, already-active warning, forbidden-room warning, item consume, `q.x`, `q.w`, `q.c(1)` q.N stack side effect, and world movement decrement/encounter block gate. Full random encounter generator remains separate. | `build_intro_demo/panel_bag_item13_success_msg.png` |
   | `14` | PORTED/SMOKE-LOCKED for no-egg warning, type0 success to `game.k.q=10`, type>0 success to `game.k.q=30`, already-ready warning, item consume, and return to bag. Hatch action in `bag.ui b=3 q.N case0` remains separate. | `build_intro_demo/panel_bag_item14_type0_success.png` |

## Status

- Source table and formulas: PROVED.
- Rebuild item descriptions for `0/1/2/3/4`: PORTED for `sourceItem()` /
  bag metadata path.
- Rebuild exact formula smoke for item `4` across P16, state17, and
  petsetting item routes: PORTED/SMOKE-LOCKED.
- Rebuild generic behavior formulas: PORTED/SMOKE-LOCKED for normal
  behavior groups `1..5` on P16, state17, and petsetting item routes.
- Rebuild item validation/message parity for warning codes `1/4/8`:
  PORTED/SMOKE-LOCKED on battle P16, panel state17, and petsetting item
  choice.
- Warning code `6`: PORTED for forced state17 behavior-0 validation;
  source-unreachable through current battle/petsetting item lists.
- Warning code `5`: SOURCE-KNOWN but UNREACHABLE_BY_SOURCE_LIST because no
  current `aq.c[4]` row uses behavior `6`.
- Top-level item `13/14` special routes in `game.h.ac()`:
  PORTED/SMOKE-LOCKED for bag tab `b == 0` item-use branches.
