# Resource Format Specs

Doc lien quan:

- [07_resource_renderer_notes.md](07_resource_renderer_notes.md): co che loader/cache/renderer va quy trinh ve that.
- [08_effect_ui_renderer_notes.md](08_effect_ui_renderer_notes.md): effect manager `b.java` va UI `.ui` runtime.

Tài liệu này ghi lại format tài nguyên cần port.

## Resource Paths

Theo code gốc:

```text
/data/event/scene_<id>.mid
/data/map/map_<id>.mid
/data/mod/mod_<id>.mid
/data/mod/modInfo.mid
/data/script/sprite.mid
/data/script/backPic.mid
/data/script/db.mid
/data/script/chs.mid
/data/spr/spr_<id>_all(r)
/data/img/img_<id>.mid
/data/tex/<name>.mid
/data/ui/<name>.ui
/font.bin
```

## Font `font.bin`

Nguồn: `s.java`.

Layout:

1. `DataInputStream.readUTF()` danh sách glyph.
2. `byte height`.
3. `byte width` cho từng glyph.
4. Bitmap bitstream, bit thấp trước.

Render:

- Mỗi pixel bật được vẽ bằng `Graphics.drawLine(x,y,x,y)`.
- Text width lấy từ tổng width glyph.
- Wrap dùng width và `s.b = width("nhung1")`.

## Sprite Table `script/sprite.mid`

Nguồn: `aq.java`.

Format:

- `short[][]`.
- Mỗi row: `[spr_id, img_id_0, img_id_1, ...]`.
- Actor record dùng **sprite index** là chỉ số row, không phải image id.

Ví dụ scene 0:

```text
actor 0: spriteIndex 84 -> [84,162] -> spr_84_all(r) + img_162
actor 1: spriteIndex 85 -> [85,163] -> spr_85_all(r) + img_163
```

## Sprite Binary `spr_*_all(r)`

Nguồn: `aa.java`, `d.java`, `o.java`.

Blocks:

Ghi chu audit:

- Thu tu doc that trong `aa.a(sprId)`: frame rect -> cell composition -> animation -> hitbox -> collision.
- Animation normal format la pairs `[duration, cellId]`.
- Animation extended format dung 4 short/keyframe; root `d.java` doc duration o `q*4`, cell id o `q*4+1`.

1. Flat short table: frame rect, rows x 5:
   `[imageSlot, sx, sy, w, h]`
2. Matrix: cell composition, rows x variable len x 4:
   `[frameId, offsetX, offsetY, transform]`
3. Matrix: animation, rows x variable len x 2 hoặc x4:
   `[duration, cellId]` khi normal.
4. Flat hitbox records, packed rows x 5, remapped per cell.
5. Flat collision records, packed rows x 5, remapped per cell.

Special case:

- `spr_id 86..185`: `aa.java` sinh cell variants bằng offset `{0,10,3,7,-10}` và animation mặc định:
  `{{2,0}, {1,0,1,1,1,2,1,3,1,2}, {5,0,5,4}}`.

## Image `img_<id>.mid`

Nguồn: `am.java`, `ae.java`.

Hiện decoded PNG đã có trong `img/decoded`. Cần audit thêm format `.mid` gốc nếu muốn loader đọc trực tiếp.

## Map `map_<id>.mid`

Nguồn: `j.java`.

Fields chính:

- compact flag/version byte.
- `mod_id`.
- width/height tile.
- tile size.
- layer count.
- layer type:
  - `0/1`: tile grid.
  - `2/3/4`: object/sparse layer với transform.

Renderer:

- Cache offscreen image kích thước màn.
- Scroll dùng `copyArea`.
- Tile background fill dùng `game.k.C()`.

## Mod `mod_<id>.mid` Và `modInfo.mid`

Nguồn: `aq.java`, `j.java`.

- `modInfo.mid`: map mod id -> danh sách image id tileset.
- `mod_<id>.mid`: tile rect rows:
  `[imageSlot, sx, sy, w, h]`.

## Event `scene_<id>.mid`

Nguồn: `game.k`, `game.c`.

Room block chứa:

- string pool.
- room name.
- map id (`unknown_ab` trong decoded JSON hiện tại).
- actor records.
- event groups.

Actor type 0 record scene 0:

```text
[type, spriteIndex, state, x, y, visibleFlag, v, s, w, u, ?, xRef, yRef]
```

Cần audit thêm cho actor type `1/2/3`.

## UI `.ui`

Nguồn cần audit: `ab.java`, `ao.java`, package `a/*`.

Trang thai cu da duoc thay the boi muc `UI .ui Audit Update` ben duoi.
## UI `.ui` Audit Update

Nguon: `ab.java`, `ao.java`, `w/al/af/ac/k/m/z/a/ak/y/l/e`.

Trang thai: VERIFIED/PARTIAL. Parser/runtime da nam du de port khung UI; `z.java` dynamic list data-binding bi CFR pha mot phan.

Header decoded:

```text
short magic_or_flags = -1
short version = 1
```

Root:

```text
root al container
id = 0
x/y/w/h
navigation[]
styles[]
children[]
```

Container block:

```text
navigation count
  entry: 4 bytes [key/action/step/wrap-or-target]

style count
  style id 0/1
  target widget ids
  visible slot count / total / selected / scroll mode
  slot rectangles/groups

child count
  child type byte
  child payload
```

Child types:

| Type | Class | Meaning |
|---|---|---|
| `0` | `al` | container, recursive children |
| `1` | `af` | visual/text/image widget |
| `2` | `ac` | selectable grid/list widget |

Render refs:

- `image_ref` / `alt_image_ref` in decoded JSON map to UI sprite wrapper `m`.
- `m` uses root sprite renderer `d`, so UI images still depend on `aq/am/aa/o`.
- Text draws through `y -> s -> /font.bin`.

Manager/runtime:

- `ab` caches `path -> ao` and keeps a render stack.
- `ao.b(keyAction)` handles focus/navigation/action.
- `/data/ui/dialog.ui` has special cache/stack handling for dialog/cutscene.

Effect refs:

- `b.java` effect `8` interacts with UI and can open `/data/ui/npcEnemy.ui`.
- Effect texture overlays use `/data/tex/*` and RGB helper `l/e`.
