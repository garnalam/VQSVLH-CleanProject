# 390 - Petsetting Phóng Sinh Row Deep Closeout

Ngày: 2026-07-15

Phạm vi: tab **Sủng vật** -> `petsetting.ui` -> row `3 Phóng sinh`.

Luật làm việc: audit source trước, chỉ smoke/headless PNG. Không mở live client.

## Source Path

| Source | Vai trò | Kết luận |
| --- | --- | --- |
| `game.k` panel handler, `f == 1`, `c == 3` | Confirm row `Phóng sinh` | PORTED |
| `aq.c[0][species][22] == 2` | Protected/mythic pet cannot be released | PORTED |
| `/data/ui/msgconfirm.ui` | Confirm release dialog | PORTED/PARTIAL |
| `game.g.o(index)` | Release allowed only if another living pet exists | PORTED |
| `game.g.l(itemId)` | Clear worn equipment flag before removing pet | PORTED |
| pet `c[5]` | Clear selected pet equipment slot before removal | PORTED |
| `game.g.m(index)` | Remove party pet and shift remaining pets left | PORTED |
| `/data/ui/msgwarm.ui` | Protected/last-alive warning | PORTED/PARTIAL |

## Source Behavior Matrix

| Case | Source behavior | Rebuild behavior | Status |
| --- | --- | --- | --- |
| Protected pet | `aq.c[0][species][22] == 2` -> `msgwarm.ui`, text `Thần thú không thể phóng sinh` | Same source condition, warning mode 2 | PORTED |
| Normal pet confirm open | Open `msgconfirm.ui`, message `Bạn muốn phóng sinh sủng vật này?`, action `Xác nhận` | `sourceReleaseConfirmVisible=true`, closes petsetting overlay | PORTED/PARTIAL |
| Cancel/back | key back closes `msgconfirm.ui`, return `petstate.ui` | PORTED |
| Confirm but no other living pet | `game.g.o(b)==false` -> `msgwarm.ui`, text `Ba lô phải lưu ít nhất 1 sủng vật` | Same alive-other check, warning mode 1 | PORTED |
| Confirm valid release | clear equipment, remove pet, shift party, clamp selected index, refresh `petstate.ui` | `sourceUnequipEquipment`, `sourcePets.remove`, slot reindex, index clamp | PORTED |
| Warning confirm | close warning, return `petstate.ui` | PORTED |

## Mutation Details

Source success path:

```text
game.g.l(q.A[b].c[5])
q.A[b].c[5] = -1
game.g.m(b)
if b >= partySize: b--
refresh petstate.ui
close msgconfirm.ui
```

Rebuild mirror:

```text
equipmentId = sourcePayload[2]
sourceUnequipEquipment(equipmentId, battleMenuIndex)
sourcePets.remove(battleMenuIndex)
reindex slot fields
clamp battleMenuIndex
openWorldPetstate()
```

This means a released pet does not leave a held item marked as `Bị mang theo`.

## Smoke Coverage

Suite mới: `panel_petsetting_release`.

| Checkpoint | Chứng minh |
| --- | --- |
| `panel_petstate_petsetting_release_confirm_open` | open đúng `msgconfirm.ui` với text/action |
| `panel_petstate_petsetting_release_cancel_returns_petstate` | cancel/back quay về `petstate.ui`, không remove pet |
| `panel_petstate_petsetting_release_success_removes_pet` | valid release removes pet, shifts party, clears equipment |
| `panel_petstate_petsetting_release_last_alive_warning` | không cho phóng sinh pet sống cuối cùng |
| `panel_petstate_petsetting_release_warning_returns_petstate` | bấm 5 warning quay về `petstate.ui` |
| `panel_petstate_petsetting_release_protected_warning` | protected/mythic pet bị chặn bằng source species flag |

## Current Status

Row `3 Phóng sinh` hiện đạt **PORTED** cho logic chính:

- Protected pet check bám `aq.c[0][species][22]`.
- Confirm/cancel/warning loop đã port.
- Release mutation remove party pet và shift slot đã port.
- Trang sức đang mang được clear khỏi `q.L` mirror trước khi pet bị xóa.
- Smoke riêng đã tách để chạy gọn row này.

## Remaining Debt

| Debt | Ghi chú |
| --- | --- |
| Full `msgconfirm.ui` Java ME widget VM | Runtime vẫn source-shaped, chưa phải widget VM 100%. |
| Exact `msgwarm.ui`/`msgconfirm.ui` pixel parity | Text/action đúng, nhưng baseline/clip tuyệt đối vẫn thuộc UI engine debt. |
| Save/load after release | Mutation memory đã smoke; có thể thêm riêng nếu muốn khóa persistence sau release. |
| Bank/storage release variant | Source có nhánh khác cho owner state 16, không thuộc row 3 normal petsetting. |

## Next Roadmap Step

Tiếp tục `petsetting.ui`:

1. Row `4 Kỹ năng`: audit/khóa `skill.ui` list, PP/value, description, navigation/back, mouse wheel.
2. Sau đó row `5 Tiến hóa` nếu pet có evolution notice.

