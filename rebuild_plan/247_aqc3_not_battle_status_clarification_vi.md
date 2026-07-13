# 247 - Làm rõ aq.c[3] không phải bảng battle status thuần

Ngày: 2026-07-13

Mục tiêu: chốt lại cách hiểu đúng cho bảng `aq.c[3]`, vì bảng này đang dễ bị gọi nhầm là
`status/effect`. Nếu gọi nhầm, các row như `Hồn Tinh Thạch` hoặc `Chìa khóa` sẽ bị lẫn vào
battle engine, dẫn tới port sai logic.

## Kết luận ngắn

`aq.c[3]` là bảng dùng chung cho **pet-held property/passive + material/key inventory**.

Nó không phải một bảng battle status thuần.

Phân loại đúng:

| Id range | Phân loại đúng | Nơi lưu / UI source | Có phải battle status không |
| --- | --- | --- | --- |
| `0..11` | Thuộc tính / passive mang trên pet | inventory `q.L`, gắn vào pet qua `game.b.c[5]` | Không phải buff/debuff tạm thời; chỉ row nào có source consumer mới ảnh hưởng battle |
| `12..16` | Nguyên liệu tiến hóa / dị hóa | inventory `q.M` | Không |
| `17` | Key / special item | inventory `q.M`, UI hiển thị riêng `"Chìa khóa vàng"` | Không |

Vì vậy `Hồn Tinh Thạch`, `Quỷ Thần Tinh Thạch`, `Chìa khóa` xuất hiện trong cùng `aq.c[3]`
là do source reuse chung bảng text/icon, không phải vì chúng là status chiến đấu.

## Source facts

### Pet có một property/passive đang mang

`game.b.f(byte)` kiểm tra trực tiếp `c[5]`:

```java
public final boolean f(byte by) {
    return this.c[5] == by;
}
```

UI pet cũng render `aq.c[3][c[5]]`, ví dụ:

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`: pet data dùng `v1[2]` hoặc `v1[i].c[5]` để lấy icon/name từ `aq.c[3]`.
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`: khi equip/remove property, source trả item về list và set `z[n].c[5]`.

Nói cách khác: `c[5]` không phải buff/debuff queue. Nó là một property/passive đang được pet mang.

### q.L và q.M chia row theo id

Source inventory group `by == 2` dùng chung `aq.c[3]`, nhưng chia list:

```text
id < 12  -> q.L
id >= 12 -> q.M
```

Ý nghĩa:

- `q.L`: các property/passive có thể gắn lên pet.
- `q.M`: nguyên liệu/key dạng stack, không gắn lên pet như passive.

Đây là lý do row `12..17` nằm cạnh row `0..11` trong cùng table.

## Vậy các "status tăng bắt pet" là gì?

Có 2 lớp khác nhau, cần tách rõ:

### 1. Debuff trên pet hoang dã làm bắt dễ hơn

Trong catch chance `game.d.b(itemId)`, target wild pet được kiểm tra:

```java
if (((b)this.h.p).m(1)) n3 = 1;
if (((b)this.h.p).m(2)) n3 = 2;
if (((b)this.h.p).m(10)) n3 = 3;
```

Sau đó source dùng:

```java
int[] nArray = new int[]{10, 11, 12, 12, 12};
n4 = n4 * nArray[n3] / 10;
```

Nên:

| Target debuff | Ý nghĩa |
| --- | --- |
| debuff `1` | catch multiplier `11/10` = x1.1 |
| debuff `2` | catch multiplier `12/10` = x1.2 |
| debuff `10` | catch multiplier `12/10` = x1.2 |

Cái này hợp logic: pet hoang dã bị mê/khống chế/tê liệt thì dễ bắt hơn.

### 2. Property id 11 trên pet của người chơi tăng catch chance

Source cũng kiểm tra active/player pet:

```java
if (this.h.f((byte)11)) {
    n3 = 4;
}
...
n4 = n4 * nArray[n3] / 10;
if (this.h.f((byte)11)) {
    n4 = n4 * (100 + aq.c[3][11][5]) / 100;
}
```

Row source:

```text
aq.c[3][11] = [224,12,248,5,1,20]
```

Vì vậy property id 11 không phải "wild pet có ability bắt pet". Nó là pet đang active của
người chơi mang property/passive `Sủng vật lôi đạt`.

Theo source order, nếu active pet có id 11:

```text
catch chance *= 12 / 10
catch chance *= 120 / 100
```

Tức là trước khi bị cap/clamp, hệ số riêng của property này là khoảng `1.44x`.
Sau đó source còn nhân theo hệ số loài/cấp và clamp về `1..100`.

## Quy tắc đặt tên từ giờ

Không gọi cả `aq.c[3]` là `status`.

Dùng tên:

```text
aq.c[3] property/passive/material table
```

Với row `0..11`, dùng:

```text
pet-held property/passive
```

Với row `12..17`, dùng:

```text
material/key inventory row
```

Battle status/effect thật sự nên tách sang:

| Table | Ý nghĩa battle |
| --- | --- |
| `aq.c[6]` | buff/self active queue |
| `aq.c[7]` | debuff/target active queue |
| `aq.c[3][0..11]` | held passive/property, chỉ có hiệu lực nếu source `game.b/game.d` check |

## Hậu quả với roadmap

Khi code battle:

1. Chỉ port `aq.c[3][0..11]` nếu có consumer rõ trong `game.b` hoặc `game.d`.
2. Không gán bất kỳ battle behavior nào cho `aq.c[3][12..17]`.
3. Catch smoke phải tách 2 case:
   - target debuff tăng catch: debuff `1/2/10`.
   - active pet property id `11` tăng catch: source multiplier khoảng `1.44x` trước cap.
4. Các tài liệu cũ ghi `Form/status aq.c[3]` cần hiểu lại là `Pet-held property/passive aq.c[3][0..11]`.
