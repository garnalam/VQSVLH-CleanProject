# 389 - Petsetting Vật Phẩm Trang Sức Row Deep Closeout

Ngày: 2026-07-15

Phạm vi: tab **Sủng vật** -> `petsetting.ui` -> row `2 Vật phẩm trang sức`.

Luật làm việc: audit source trước, chỉ smoke/headless PNG. Không mở live client.

## Source Path

| Source | Vai trò | Kết luận |
| --- | --- | --- |
| `game.k` panel handler, `f == 1`, `c == 2` | Confirm row `Vật phẩm trang sức` | PORTED/PARTIAL |
| `game.k.bm()` | Handle `choice.ui` navigation/back/confirm cho trang sức | PORTED/PARTIAL |
| `game.g.L` / source equipment vector | Danh sách trang sức, row shape `[id, wornFlag, 0]` | PORTED/PARTIAL |
| `game.g.f(itemId, petIndex)` | Mang trang sức, clear đồ cũ và clear holder cũ | PORTED |
| `game.g.l(itemId)` | Dỡ trang sức, clear worn flag | PORTED |
| Pet payload `c[5]` | Trang sức đang mang trên pet | PORTED qua `sourcePayload[2]` |
| `aq.c[3]` | Held item/trang sức name/icon/description/params | PORTED/PARTIAL |

## Source Behavior Matrix

| Case | Source behavior | Rebuild behavior | Status |
| --- | --- | --- | --- |
| Open row 2 | Open `choice.ui`, title `Vật phẩm trang sức`, subtitle `Trạng thái`, softkey `Mang theo` | `sourceEquipmentChoiceVisible=true`, closes petstate/petsetting overlays | PORTED/PARTIAL |
| Row current pet is wearing | Status `Đã mang theo`, action `Dỡ xuống` | `sourceEquipmentStatusText()` + `sourceEquipmentActionText()` | PORTED |
| Row worn by another pet | Status `Bị mang theo`, action `Mang theo` | Uses equipment row `equippedFlag` and selected pet payload | PORTED |
| Free row | Blank status, action `Mang theo` | PORTED |
| Confirm current item | `game.g.l(itemId)`, pet `c[5] = -1`, success msg `Thành công dỡ xuống` | `sourceUnequipEquipment()` + msgwarm | PORTED |
| Confirm free item | `game.g.f(itemId,b)`, selected pet wears item, old item cleared | `sourceEquipEquipment()` | PORTED |
| Confirm item worn by other pet | `game.g.f(itemId,b)`, previous holder cleared | `previousPet` cleared and selected pet receives item | PORTED |
| Back | Refresh `petstate.ui`, close `choice.ui` | PORTED |
| Save/load | Source `q.L` and pet `c[5]` should persist | Rebuild persists equipment rows + payload index `2` | PORTED/PARTIAL |

## Data Binding

Trang sức ở nhánh này là cùng lớp dữ liệu với held item/passive battle:

| Data | Rebuild mapping |
| --- | --- |
| `aq.c[3][id][0]` | `BattleHeldItemRow.name()` |
| `aq.c[3][id][1]` | icon cell qua `sourceEquipmentIconCell(id)` |
| `aq.c[3][id][2]` | description qua `sourceEquipmentDescription(id)` |
| `game.g.L` row `[id,wornFlag,0]` | `SourceEquipmentItem(id,equippedFlag)` |
| pet `c[5]` | `SourcePetState.sourcePayload[2]` |

Quan trọng: UI equip/unequip và battle effect là hai lớp khác nhau, nhưng dùng chung held item id từ `aq.c[3]`.

## Smoke Coverage

Suite mới: `panel_petsetting_equipment`.

| Checkpoint | Chứng minh |
| --- | --- |
| `panel_petstate_petsetting_equipment_choice_open` | open đúng `choice.ui` row 2 |
| `panel_petstate_petsetting_equipment_choice_navigation` | cursor/list navigation |
| `panel_petstate_petsetting_equipment_choice_statuses` | `Đã mang theo`, `Bị mang theo`, blank, softkey `Dỡ xuống/Mang theo` |
| `panel_petstate_petsetting_equipment_choice_back_returns_petstate` | back refresh về `petstate.ui` |
| `panel_petstate_petsetting_equipment_choice_unequip_success_msg` | dỡ đồ hiện tại, clear pet payload và flag |
| `panel_petstate_petsetting_equipment_choice_equip_success_msg` | mang đồ free, clear đồ cũ |
| `panel_petstate_petsetting_equipment_choice_transfer_success_msg` | chuyển đồ đang do pet khác mang, clear previous holder |
| `panel_petstate_petsetting_equipment_choice_success_returns_petstate` | success msgwarm confirm quay về `petstate.ui` |
| `panel_petstate_petsetting_equipment_save_load_qL` | save/load `q.L` + pet equipment payload |

## Current Status

Row `2 Vật phẩm trang sức` hiện đạt **PORTED/PARTIAL**:

- UI list/status/action source-shaped đã có.
- Equip/unequip/transfer logic chính theo `game.g.f/l` đã port.
- Save/load `q.L` và pet equipment slot đã có smoke.
- Names/descriptions/icons lấy từ held item table, không còn mock chung chung.
- Battle effects của nhiều held item đã được port ở battle roadmap, nhưng không thuộc riêng UI row này.

## Remaining Debt

| Debt | Ghi chú |
| --- | --- |
| Full `choice.ui` Java ME widget VM | Renderer hiện source-shaped, chưa phải VM widget 100%. |
| Exact text baseline/clip/pixel compare | UI nhìn được và có asserts, nhưng chưa pixel-perfect bản gốc. |
| Empty equipment list edge case | Source `q.L.size()==0` cần smoke riêng nếu muốn khóa. |
| Bank/storage equipment ownership parity | Save/load runtime chính đã có, nhưng storage-side ownership chưa audit sâu. |
| Full held item battle coverage | Một số held item material/non-battle đã tách, battle effect coverage nằm ở battle docs riêng. |

## Next Roadmap Step

Tiếp tục `petsetting.ui` theo thứ tự:

1. Row `3 Phóng sinh`: audit/khóa release confirm, protected pet, last-alive validation, mutation inventory/pet list.
2. Sau đó row `4 Kỹ năng`.
3. Cuối cùng row `5 Tiến hóa` nếu pet có evolution notice.

