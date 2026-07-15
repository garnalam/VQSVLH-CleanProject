# 387 - Petsetting Đạo Cụ Deep Closeout

Ngày: 2026-07-15

Phạm vi: tab **Sủng vật** -> `petsetting.ui` -> row `0 Đạo cụ`.

Luật làm việc: chỉ audit source + smoke/headless PNG. Không mở live client.

## Source Path

| Source | Vai trò | Kết luận |
| --- | --- | --- |
| `game.k.bh()` | Mở `/data/ui/choice.ui` cho nhánh Đạo cụ | PORTED/PARTIAL |
| `game.k.bi()` | Bind list item từ `q.K` vào `choice.ui` | PORTED/PARTIAL |
| `game.i.x(item)` | Validate item dùng trên pet | PORTED |
| `game.i.w(item)` | Apply item lên pet và consume inventory | PORTED |
| `aq.c[4]` | Item name/icon/description/behavior/params | PORTED cho item pet-use 4..12 |

## UI Runtime Matrix

| Chi tiết source | Behavior source | Rebuild status |
| --- | --- | --- |
| `choice.ui` title widget 8 | `Đạo cụ` | PORTED |
| Widget 9 subtitle | `Số lượng` | PORTED |
| Softkey | `Sử dụng` | PORTED/PARTIAL, source-shaped |
| Rows | lấy từ `q.K`, mỗi row `[itemId,count]` | PORTED |
| Icon | `aq.c[4][itemId][1]` | PORTED/PARTIAL |
| Name | `aq.c[4][itemId][0]` | PORTED |
| Count | inventory count từ `q.K` | PORTED |
| Description | widget 53 = `aq.c[4][selected][2]` | PORTED |
| Scrollbar | thumb source `98 + h * 80 / q.K.size()` | PORTED/PARTIAL |
| Mouse hover | chỉ preview selection/detail, không confirm | PC_QOL PORTED |
| Mouse wheel long list | scroll list, không confirm | PC_QOL PORTED |
| Mouse wheel short list | đổi selection, không confirm | PC_QOL PORTED |
| Back | đóng `choice.ui`, quay lại `petstate.ui` | PORTED |

## Item Behavior Matrix

`Đạo cụ` trong petsetting chỉ xử lý item dùng trực tiếp lên pet qua `game.i.x/w`.
Ball/catch item và top-level special item không thuộc nhánh này.

| Item | Tên | Source row nghĩa chính | Validate | Apply | Status |
| --- | --- | --- | --- | --- | --- |
| 0 | Tất Trung Cầu | catch ball, behavior 0 | không dùng qua petsetting | none | EXCLUDED |
| 1 | Phong ấn cầu | catch ball, behavior 0 | không dùng qua petsetting | none | EXCLUDED |
| 2 | Cao cấp cầu | catch ball, behavior 0 | không dùng qua petsetting | none | EXCLUDED |
| 3 | Đại sư cầu | catch ball, behavior 0 | không dùng qua petsetting | none | EXCLUDED |
| 4 | Bánh Sandwich | behavior 1, HP `50%, +50` | full HP -> warning 2 | `hp += maxHp * 50 / 100 + 50`, clamp | PORTED |
| 5 | Chocolate | behavior 1, HP `100%, +0` | full HP -> warning 2 | full heal by formula, clamp | PORTED |
| 6 | Năng lượng Thạch | behavior 2, PP `25` | full PP -> warning 3 | restore each skill PP by 25 | PORTED |
| 7 | Tụ Năng Thạch | behavior 2, PP `45` | full PP -> warning 3 | restore each skill PP by 45 | PORTED by formula, no dedicated PNG in this slice |
| 8 | Khôi phục tề | behavior 3, HP `50%, +50`, PP `20` | HP+PP full -> warning 7 | heal + PP restore | PORTED |
| 9 | Siêu năng tề | behavior 3, HP `100%, +0`, PP `45` | HP+PP full -> warning 7 | heal + PP restore | PORTED by formula + warning smoke |
| 10 | Vạn năng dược | behavior 5 | no debuff -> warning 4 | clear bad effects/debuffs via `D()`/`clearDebuffs()` | PORTED |
| 11 | Sinh mệnh thạch | behavior 4, revive HP `50%, +50`, PP `20` | alive -> warning 1 | revive, set HP formula, restore PP | PORTED |
| 12 | Linh Hồn Thạch | behavior 4, revive HP `100%, +0`, PP `45` | alive -> warning 1 | revive, full-ish HP formula, restore PP | PORTED |
| 13 | Tị quái hàn | top-level avoid encounter item | not this branch | handled elsewhere | EXCLUDED |
| 14 | Gia tốc dược | egg accelerator/special route | not this branch | pending special route | EXCLUDED/PENDING |

## Validation Warning Matrix

Source `game.i.x(item)` returns numeric warning codes. Rebuild maps these to `msgwarm.ui`.

| Code | Source condition | Rebuild message status |
| --- | --- | --- |
| 1 | revive item used on alive pet | PORTED |
| 2 | HP already full | PORTED |
| 3 | PP already full | PORTED |
| 4 | no debuff to clear | PORTED |
| 5 | already excited, behavior 6 | PORTED path, no common item row yet |
| 6 | behavior 0 cannot use here | PORTED |
| 7 | HP and PP already full | PORTED |
| 8 | target pet dead but item is not revive | PORTED |

## Smoke Coverage

Suite: `panel_petsetting_items`.

| Checkpoint | Chứng minh |
| --- | --- |
| `panel_petstate_petsetting_item_choice_open` | mở đúng `choice.ui`, title/subtitle/list/description |
| `panel_petstate_petsetting_item_choice_navigation` | selection thay đổi, description đổi theo |
| `panel_petstate_petsetting_item_choice_hover_preview_no_confirm` | hover preview, không consume item |
| `panel_petstate_petsetting_item_choice_mouse_wheel_selection_no_confirm` | wheel đổi selection khi list ngắn, không consume |
| `panel_petstate_petsetting_item_choice_back_returns_petstate` | back quay lại `petstate.ui` |
| `panel_petsetting_item4_hp_heal_source_success_msg` | item 4 dùng công thức source, không heal full bừa |
| `panel_petsetting_item5_hp_heal_clamp_success_msg` | item 5 full heal/clamp |
| `panel_petsetting_item6_pp_restore_success_msg` | restore PP |
| `panel_petsetting_item8_hp_pp_success_msg` | heal + PP restore |
| `panel_petsetting_item8_both_full_warning7` | warning 7 khi HP+PP đều full |
| `panel_petsetting_item9_both_full_warning7` | warning 7 cho item 9 |
| `panel_petsetting_item10_clear_debuff_success_msg` | clear debuff thật |
| `panel_petsetting_item10_no_debuff_warning` | warning khi không có debuff |
| `panel_petsetting_item11_revive_success_msg` | revive item 11 |
| `panel_petsetting_item12_revive_success_msg` | revive item 12 |

Smoke output folder:

`rebuild_game/build_intro_demo/panel_petsetting_items_387`

## Current Status

`Đạo cụ` trong `petsetting.ui` hiện đạt mức **PORTED/PARTIAL**:

- Logic item-on-pet theo `game.i.x/w` đã port và có smoke.
- Data binding lấy từ source bag `q.K`/item table, không dùng mock item description.
- Hover/wheel behavior theo PC QoL đã thêm nhưng không làm thay đổi source confirm flow.
- Không lẫn ball/catch item, item 13 avoid encounter, item 14 egg accelerator vào nhánh dùng đạo cụ trên pet.

## Remaining Debt

| Debt | Vì sao chưa gọi là full parity |
| --- | --- |
| Full binary widget VM cho `choice.ui` | Rebuild vẫn source-shaped, chưa chạy toàn bộ `game.h/game.k` widget runtime 100%. |
| Exact Java ME text baseline/clip | Đủ nhìn và smoke được, nhưng chưa pixel-perfect từng font/clip. |
| Item visual effect `l(amount)` | Logic heal/PP/revive đã đúng; visual số hồi máu/hiệu ứng Java ME chưa đóng riêng. |
| Behavior 6 | Source có nhánh excited flag, nhưng chưa có item early/common rõ để smoke riêng. |

## Next Roadmap Step

Tiếp tục đào sâu `petsetting.ui` theo đúng thứ tự row:

1. **Chiến đấu** row `1`: audit source select active pet, dead/current validation, party order, warning/back flow.
2. Smoke PNG tối thiểu: open, current-pet warning, dead-pet warning, valid switch, back.
3. Sau đó mới sang **Vật phẩm trang sức** row `2`.

