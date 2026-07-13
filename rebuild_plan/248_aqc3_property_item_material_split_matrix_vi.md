# 248 - aq.c[3] Property / Item / Material Split Matrix VI

Ngày: 2026-07-13

Mục tiêu: trả lời rõ `aq.c[3]` row nào là ability/passive của pet, row nào là item/material/key.
Không gọi chung tất cả là `form/status`.

## Kết luận quan trọng

`aq.c[3]` không phải một bảng "form/status" thuần.

Trong source, `aq.c[3]` là bảng text/icon dùng chung cho 2 nhóm:

1. `0..11`: đồ/property dạng **pet mang theo**. Khi gắn lên pet, id được lưu ở `game.b.c[5]`.
2. `12..17`: material/key nằm trong inventory stack, không gắn lên pet, không có battle passive.

Vì vậy:

- `Mạn Đà La Thạch`, `Hồng Sắc Hải Loa`, `Sủng vật lôi đạt` là **pet-held property/passive**.
- `Tinh Nguyên Thạch`, `Hồn Tinh Thạch` là **nguyên liệu tiến hóa/dị hóa**.
- `Chìa khóa` là **key/special item**.

## Cách hiểu đúng theo source

### Pet-held property/passive là gì?

Đây không phải Pokemon/species, không phải skill, không phải buff/debuff tạm thời.

Nó giống một loại **held item / passive property**:

- người chơi sở hữu trong list `q.L`;
- có thể chọn `Mang theo`;
- source gắn vào pet qua `game.b.c[5]`;
- battle chỉ có hiệu lực nếu `game.b` hoặc `game.d` check `pet.f((byte)id)`.

Source chứng minh:

```java
public final boolean f(byte by) {
    return this.c[5] == by;
}
```

Equip source:

```java
this.z[n3].c[5] = (short)n2;
```

UI source hiển thị:

```text
Đã mang theo
Bị mang theo
Mang theo
Dỡ xuống
```

### Material/key là gì?

Các row `12..17` nằm trong list `q.M`, render số lượng stack, dùng cho tiến hóa/dị hóa/key.
Chúng không được equip lên pet bằng `c[5]`, nên không được coi là battle passive.

## Split matrix

| Id | Tên | Nhóm thật | Có phải ability/passive pet không | Có phải item/material không | Storage/source | Battle consumer đã chứng minh | Ghi chú |
| ---: | --- | --- | --- | --- | --- | --- | --- |
| 0 | Mạn Đà La Thạch | pet-held property/passive | Có | Inventory property có thể mang | `q.L` -> `game.b.c[5]` | Có: low HP attack boost trong `game.b.B()` | Không phải Pokemon; là passive/held property khi pet mang |
| 1 | Hồng Sắc Hải Loa | pet-held property/passive | Có | Inventory property có thể mang | `q.L` -> `game.b.c[5]` | Có: attack +10% trong `game.b.B()` | Không phải Pokemon; là passive/held property |
| 2 | Quy Xác Toái Phiến | pet-held property/passive | Có | Inventory property có thể mang | `q.L` -> `game.b.c[5]` | Có: defense +15% khi bị tính damage | Passive phòng ngự khi pet mang |
| 3 | Ô Nha Uế | pet-held property/passive | Có | Inventory property có thể mang | `q.L` -> `game.b.c[5]` | Có: giảm chance dính debuff 20% | Passive kháng debuff |
| 4 | Viễn Cổ Long Cốt | pet-held property/passive | Có | Inventory property có thể mang | `q.L` -> `game.b.c[5]` | Có: crit chance +10 điểm % | Passive tăng chí mạng |
| 5 | Mật Phong Sào | pet-held property/passive | Có | Inventory property có thể mang | `q.L` -> `game.b.c[5]` | Có: EXP participant x120% | Passive EXP |
| 6 | Ký Cư Giải Xác | pet-held property/passive | Có | Inventory property có thể mang | `q.L` -> `game.b.c[5]` | Có/PARTIAL: reserve EXP share | Passive EXP cho pet không tham chiến |
| 7 | Linh Trùng Thi Hài | pet-held property/passive | Có | Inventory property có thể mang | `q.L` -> `game.b.c[5]` | Chưa chứng minh consumer trực tiếp | PENDING, không code bừa |
| 8 | Hấp Huyết Đằng Mạn | pet-held property/passive | Có | Inventory property có thể mang | `q.L` -> `game.b.c[5]` | Có: sau hit có chance tự hồi HP | Passive hút máu |
| 9 | Cá Thờn Bơn | pet-held property/passive | Có | Inventory property có thể mang | `q.L` -> `game.b.c[5]` | Có: ép miss chance về 0 | Passive đánh không hụt |
| 10 | Cảm Lãm Chi Diệp | pet-held property/passive | Có | Inventory property có thể mang | `q.L` -> `game.b.c[5]` | Có: HP floor 10 trong P7/catch-related path | Passive giữ HP target còn tối thiểu 10 khi điều kiện source đúng |
| 11 | Sủng vật lôi đạt | pet-held property/passive | Có | Inventory property có thể mang | `q.L` -> `game.b.c[5]` | Có: active/player pet tăng catch chance | Đây là passive của pet người chơi, không phải ability của pet hoang dã |
| 12 | Tinh Nguyên Thạch | material tiến hóa | Không | Có | `q.M` stack | Không | Nguyên liệu, không phải battle status |
| 13 | Thiên Giới Tinh Thạch | material tiến hóa bậc cao | Không | Có | `q.M` stack | Không | Nguyên liệu, không phải battle status |
| 14 | Thiên Địa Thần Thạch | material tiến hóa hiếm | Không | Có | `q.M` stack | Không | Nguyên liệu, không phải battle status |
| 15 | Hồn Tinh Thạch | material dị hóa | Không | Có | `q.M` stack | Không | Nguyên liệu dị hóa, không phải ability |
| 16 | Quỷ Thần Tinh Thạch | material dị hóa hiếm | Không | Có | `q.M` stack | Không | Nguyên liệu dị hóa, không phải ability |
| 17 | Chìa khóa | key/special item | Không | Có | `q.M` stack | Không | UI source còn đổi tên thành `Chìa khóa vàng` |

## Cách gọi từ giờ

Không dùng:

```text
form/status aq.c[3]
ability table aq.c[3]
```

Dùng:

```text
aq.c[3] shared property/passive/material table
aq.c[3][0..11] pet-held property/passive
aq.c[3][12..17] material/key rows
```

## Battle coding rule

Chỉ code battle behavior cho row `0..11` khi có source consumer rõ:

- `game.b.B()`
- `game.b.b(target)`
- `game.d.q()`
- `game.d.b(itemId)`
- EXP/catch/post-hit path trong `game.d`

Không code battle behavior cho row `12..17`.

## Catch chance clarification

`Sủng vật lôi đạt` id `11` không có nghĩa pet hoang dã tự tăng tỷ lệ bị bắt.

Source kiểm tra active/player pet:

```java
if (this.h.f((byte)11)) {
    n3 = 4;
}
...
if (this.h.f((byte)11)) {
    n4 = n4 * (100 + aq.c[3][11][5]) / 100;
}
```

Nên nghĩa đúng là:

```text
pet người chơi đang active mang property id 11
=> tăng tỷ lệ bắt target
```

Theo source order, hệ số riêng của property id 11 là khoảng `1.44x` trước khi cap/clamp:

```text
12 / 10 * 120 / 100
```
