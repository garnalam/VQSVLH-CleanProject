# 388 - Petsetting Chiến Đấu Row Deep Closeout

Ngày: 2026-07-15

Phạm vi: tab **Sủng vật** -> `petsetting.ui` -> row `1 Chiến đấu`.

Luật làm việc: audit source trước, chỉ smoke/headless PNG. Không mở live client.

## Source Path

| Source | Vai trò | Kết luận |
| --- | --- | --- |
| `game.k` panel handler, `f == 1`, `c == 1` | Confirm row `Chiến đấu` trong `petsetting.ui` | PORTED |
| `game.i.T()` | Check pet còn sống, source là `e[1] > 0` | PORTED via `sourcePayload[6] > 0` |
| `game.g.p(this.b)` | Đưa pet được chọn lên slot 0, các pet trước đó dời xuống | PORTED |
| `/data/ui/msgwarm.ui` | Warning khi pet chết hoặc đã xuất chiến | PORTED/PARTIAL |
| `/data/ui/petstate.ui` + `/data/ui/petsetting.ui` | Refresh UI sau switch/warning | PORTED/PARTIAL |

## Source Behavior Matrix

| Case | Source behavior | Rebuild behavior | Status |
| --- | --- | --- | --- |
| Chọn pet chết | `!q.A[b].T()` -> `msgwarm.ui`, text `Sủng vật này không thể tham chiến`, reset `b=0` | Close `petsetting.ui`, show same warning, reset `battleMenuIndex=0` | PORTED |
| Chọn pet đang ở slot 0 | `b == 0` -> `msgwarm.ui`, text `Sủng vật này đã xuất chiến`, reset `b=0` | Close `petsetting.ui`, show same warning, keep active slot 0 | PORTED |
| Chọn pet reserve còn sống | `q.p(b)`, `f=0`, `b=0`, refresh `petstate.ui`, reset list `e/f=0` | Move selected `sourcePets[b]` to index 0, update slots, close petsetting, refresh petstate | PORTED |
| Back từ `petsetting.ui` | close `petsetting.ui`, return `petstate.ui` | Already covered by petsetting shell smoke | PORTED |

## UI Notes

`Chiến đấu` không mở một UI con mới. Đây là một action row trực tiếp trong `petsetting.ui`:

1. Player đang ở `petstate.ui`.
2. Confirm mở `petsetting.ui`.
3. Move cursor xuống row `Chiến đấu`.
4. Confirm sẽ mutate party order hoặc hiện `msgwarm.ui`.

Sau warning, bấm 5 đóng `msgwarm.ui` và quay lại `petstate.ui` theo source-shaped flow.

## Smoke Coverage

Suite mới: `panel_petsetting_battle`.

| Checkpoint | Chứng minh |
| --- | --- |
| `panel_petstate_petsetting_open` | mở được `petsetting.ui` trên `petstate.ui` |
| `panel_petstate_petsetting_navigation` | cursor xuống đúng row `Chiến đấu` |
| `panel_petstate_petsetting_active_switch_success` | reserve pet sống được đưa lên slot 0, pet cũ dời xuống |
| `panel_petstate_petsetting_active_dead_warning` | pet chết không được chọn chiến đấu, warning đúng text |
| `panel_petstate_petsetting_active_already_warning` | pet slot 0 báo đã xuất chiến |

## Current Status

Row `1 Chiến đấu` hiện đạt **PORTED** cho logic source chính:

- Validate pet chết/current đúng source.
- Party reorder theo `game.g.p(b)` đã có.
- UI warning source-shaped qua `msgwarm.ui`.
- Smoke riêng đã tách khỏi suite lớn để tránh lặp vòng khi regression.

## Remaining Debt

| Debt | Ghi chú |
| --- | --- |
| Full `game.h/game.k` widget runtime | Rebuild vẫn source-shaped, chưa phải VM widget Java ME 100%. |
| Exact msgwarm pixel parity | Warning flow đúng, nhưng baseline/clip tuyệt đối vẫn thuộc debt UI engine chung. |
| Save/load party order after switch | Logic memory đã port; nếu muốn khóa sâu hơn có thể thêm smoke save/load riêng. |

## Next Roadmap Step

Tiếp tục theo thứ tự `petsetting.ui`:

1. **Row 2 - Vật phẩm trang sức**: audit/port sâu equipment choice, held item transfer/equip/unequip, save/load `q.L`.
2. Nếu row 2 đã đủ theo tài liệu cũ, tạo closeout hiện trạng mới giống 387/388.
3. Sau đó mới qua row 3 `Phóng sinh`.

