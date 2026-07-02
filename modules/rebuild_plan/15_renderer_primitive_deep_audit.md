# 15. Renderer Primitive Deep Audit

Pham vi:

- `source_code/decoded/decompiled_source_cfr/d.java`
- `source_code/decoded/decompiled_source_cfr/aa.java`
- `source_code/decoded/decompiled_source_cfr/am.java`
- `source_code/decoded/decompiled_source_cfr/j.java`
- `source_code/decoded/decompiled_source_cfr/l.java`
- `source_code/decoded/decompiled_source_cfr/s.java`
- `source_code/decoded/decompiled_source_cfr/ae.java`
- `source_code/decoded/decompiled_source_cfr/aq.java`

Trang thai: VERIFIED/PARTIAL.

Ket luan ngan: da du thong tin de port renderer primitive trong project rebuild. Cac format/resource path, sprite frame/cell/animation, map layer, font bitmap, image cache va DB bootstrap da duoc xac nhan tu code va mot phan binary goc. Phan con PARTIAL la pixel-validation: mapping transform MIDP tren Java SE, timing/copyArea cache cua map, va mot so field data gameplay trong `aq.c`.

## 1. Boot Order Va Quan He Module

Nguon: `game.i.d()`, `aq.a()`, `am.a()`, `aa.a()`.

Thu tu khoi tao renderer/resource:

```text
am.a()  -> tao Image[50000] va refcount byte[50000]
aa.a()  -> tao sprite metadata cache o[1000]
aq.a()  -> load bang resource/global DB
game.k.a(), game.k.i() -> world/save init
```

Bang quan he resource:

```text
actor/ui sprite index
  -> aq.a[spriteIndex]
     [0]    = sprId
     [1..]  = imgId list
  -> aa.a(sprId)
     /data/spr/spr_<sprId>_all(r)
  -> am.a(imgId)
     /data/img/img_<imgId>.mid
  -> d.java
     drawRegion(frame rect + cell part + transform)
```

Voi map:

```text
map_<id>.mid -> modId
modInfo.mid  -> aq.b[modId] = image id list
mod_<id>.mid -> tile rect table [imageSlot, sx, sy, w, h]
j.java       -> draw dense/sparse map layers
```

## 2. `aq.java`: Global Resource Tables

`aq.a()` load cac bang sau:

| Field | Source | Format | Y nghia |
| --- | --- | --- | --- |
| `aq.a` | `/data/script/sprite.mid` | `short[][]` | Sprite index -> `[sprId, imgId0, imgId1, ...]`. |
| `aq.b` | `/data/mod/modInfo.mid` | custom byte count + short image ids | Map mod id -> image ids tileset. |
| `aq.c` | `/data/script/db.mid` | 9 consecutive `short[][]` tables | Gameplay DB. Renderer dung icon/name refs qua UI/gameplay. |
| `aq.d` | `/data/script/chs.mid` | `String[][]` flattened | Text DB, moi row ghep thanh mot string. |
| `game.k.N` | `/data/script/npcDialog.mid` | `String[][]` flattened by row | NPC/dialog text bank. |
| `aq.e` | `/data/tex/tex_0..3` | RGB arrays | Texture RGB buffers for effects/UI. |
| `aq.f` | `/data/tex/bk` | Image | Background/repeated texture. |

Binary validation:

```text
sprite.mid:
  rows = 345
  example row 84 = [84,162]
  example row 85 = [85,163]
  max row length = 13
  rows with >1 image id = 19

modInfo.mid:
  mod count = 7
  rows:
    0 -> [0,1,2,17,5,6]
    1 -> [0,3,4,12,5,214,209]
    2 -> [0,5,6,3,10,1,2]
    3 -> [0,7,17,18,16]
    4 -> [0,10,11,8,9,6,5,16]
    5 -> [0,12,14,6,5]
    6 -> [0,16]

db.mid:
  group count = 9
  group 0 rows=100 len=23   likely creature/pet base table
  group 1 rows=70  len=10   skill table
  group 2 rows=8   len=7
  group 3 rows=18  len=5..7 item/effect table
  group 4 rows=15  len=6..9 item table
  group 5 rows=11  len=3
  group 6 rows=15  len=5
  group 7 rows=11  len=3
  group 8 rows=4   len=5
```

Status:

- VERIFIED: resource paths, load order, table format, table counts.
- PARTIAL: exact domain name of every `aq.c[group][col]`; that belongs to gameplay/battle audit, not primitive renderer.

## 3. `ae.java`: Binary/Image/Text Helper

Important binary readers:

```text
ae.a(InputStream) -> short[][]
  short rowCount
  repeat rowCount:
    short len
    short[len] row

ae.b(InputStream) -> byte[][]
  short rowCount
  repeat rowCount:
    short len
    byte[len] row

ae.c(InputStream) -> String[][]
  short rowCount
  repeat rowCount:
    short stringCount
    repeat stringCount:
      unsigned byte charCount
      if charCount == 255: short charCount
      charCount UTF-16 code units as short
```

Sprite packed readers:

```text
ae.a(byte[], offRef) -> short[]
  short rows
  short width
  rows * width shorts in flat array

ae.b(byte[], offRef) -> short[][]
  short rowCount
  short width
  repeat rowCount:
    short len
    len * width shorts in one row array
```

Image loading:

```text
ae.b(path, name):
  raw = ae.g(path + name + ".mid")
  Image.createImage(raw, 0, raw.length)
```

`ae.g(path)` rule:

- If path contains `img_` or ends with `menu.mid`: read raw bytes and pass to `Image.createImage`.
- Else: call private `ae.f(path)`, which reconstructs a PNG-like byte stream from a custom packed payload and writes PNG chunks/CRC.

Implication:

- `/data/img/img_*.mid` are treated as raw image bytes.
- `/data/tex/*.mid` usually go through PNG reconstruction unless they are `menu.mid`.
- Rebuild should support both: raw decoded PNG path first, custom MID-to-PNG fallback for texture assets.

Other renderer helpers:

- `ae.a(Image)` returns ARGB array.
- `ae.a(int[],w,h)` creates alpha-enabled image.
- `ae.a(rect...)` overloads are collision/visibility helpers.
- `ae.a(Graphics,String,...)` is the UI text layout bridge and uses `s/y` bitmap font when `y2 != null`.

Status:

- VERIFIED: table readers, raw-vs-packed image branch, geometry helpers used by renderer.
- PARTIAL: exact PNG reconstruction parity should be tested on every `/data/tex/*.mid` during implementation.

## 4. `am.java`: Image Cache

Fields:

| Field | Meaning |
| --- | --- |
| `Image[] a` | Image cache by numeric img id. |
| `byte[] b` | Refcount by img id. |
| `int c` | Max image id capacity, set to `50000`. |

Methods:

```text
am.a():
  c = 50000
  a = new Image[50000]
  b = new byte[50000]

am.a(imgId):
  if a[imgId] == null:
    a[imgId] = ae.b("/data/img/", "img_" + imgId)
  b[imgId]++
  return a[imgId]

am.b(imgId):
  b[imgId]--
  if b <= 0: b = 0
  // keeps image in cache

am.c(imgId):
  if imgId == -1: return true
  b[imgId]--
  if b <= 0:
    b = 0
    a[imgId] = null
    return true
  return false
```

Important distinction:

- `am.b()` releases a reference but preserves image object.
- `am.c()` releases and can unload image.
- `j.d()` uses both when switching map tilesets: shared old/new tiles call `am.b`, non-shared tiles call `am.c`.

Status: VERIFIED.

## 5. `aa.java` + `o.java`: Sprite Metadata Cache

`o` is a passive data container:

| Field | Meaning |
| --- | --- |
| `a` | Metadata refcount. |
| `b` | Frame rect flat table, rows x 5: `[imageSlot, sx, sy, w, h]`. |
| `e` | Cell composition rows. Each cell is `[frameId, offsetX, offsetY, transform] * n`. |
| `f` | Animation rows. Normal rows are `[duration, cellId] * n`; extended rows are 4 shorts per key. |
| `d` | Per-cell hitbox/action boxes remapped from packed rows. |
| `c` | Per-cell collision boxes remapped from packed rows. |
| `g` | Animation format flag set by `d.a(spriteIndex, extendedFlag)`. |

Load sequence in `aa.a(sprId)`:

```text
read /data/spr/spr_<sprId>_all(r) into byte[20000]
o.b = ae.a(bytes, off)      // flat frame rect table
o.e = ae.b(bytes, off)      // cell composition
o.f = ae.b(bytes, off)      // animation
o.d = remap(ae.a(bytes,off), o.e.length)
o.c = remap(ae.a(bytes,off), o.e.length)
```

Special case `sprId 86..185`:

```text
o.e is read, then replaced by 5 synthetic variants:
  offsetX += [0, 10, 3, 7, -10]

o.f is read, then replaced by hardcoded animation:
  row0 = [2,0]
  row1 = [1,0, 1,1, 1,2, 1,3, 1,2]
  row2 = [5,0, 5,4]
```

Binary validation:

```text
spr_0_all(r):
  frame=(44,5), cell=(33,4), anim=(12,2), hit=(34,5), col=(0,5)

spr_84_all(r):
  frame=(1,5), cell=(1,4), anim=(1,2), hit=(1,5), col=(1,5)

spr_86_all(r):
  frame=(1,5), cell=(1,4), anim=(1,2), hit=(0,5), col=(0,5)
  then aa.java synthesizes cell/anim because id in 86..185
```

Status:

- VERIFIED: block order, field roles, special id branch, refcount behavior.
- PARTIAL: names of `o.d` vs `o.c` as "hitbox/collision" are based on caller usage; exact gameplay terms should be finalized in actor/collision audit.

## 6. `d.java`: Sprite Animator And Renderer

Binding:

```text
d.a(spriteIndex, extendedAnimFlag):
  for each img id in aq.a[spriteIndex][1..]:
    m[i] = imgId
    k[i] = am.a(imgId)
  a = spriteIndex
  l = aa.a(aq.a[spriteIndex][0])
  l.g = extendedAnimFlag
  c(0) // start anim row 0
```

Release:

```text
d.a():
  release image refs with am.b()
  release sprite metadata with aa.b(spriteIndex), except spriteIndex 257

d.b():
  release image refs with am.c()
  force aa.c(spriteIndex)
```

Animation fields:

| Field | Meaning |
| --- | --- |
| `b` | Current animation row/state. |
| `q` | Current keyframe index. |
| `n` | Duration counter. |
| `r` | Keyframe count in current row. |
| `p` | End behavior/next state: `-1` loop, `-2` hold final, `>=0` jump to state. |

Animation decoding:

```text
if o.g == false:
  row stride = 2
  duration = o.f[b][q*2]
  cellId   = o.f[b][q*2 + 1]

if o.g == true:
  row stride = 4
  duration = o.f[b][q*4]
  cellId   = o.f[b][q*4 + 1]
```

Fields `q*4+2` and `q*4+3` are not used by `d.java` drawing/timing. They may be authoring metadata or used by other systems through raw table access, but no direct renderer usage was found in this pass.

Frame advance:

```text
d.d():
  if n > 0: n--
  else if o > 0: o--     // `o` is reset to 0 in c(), no observed active use
  else:
    q++
    if q >= r:
      p >= 0  -> switch animation row to p
      p == -2 -> clamp at last keyframe
      p == -1 -> loop to q=0
    else c(q)
```

Current frame queries:

```text
d.e() -> q >= r - 1
d.f() -> n == 0
d.b(frameIndex) -> q == frameIndex
d.g() -> current anim row
d.h() -> current keyframe index
d.j() -> current o.c box table for current cell
d.k() -> current o.d box table for current cell
```

Drawing:

```text
d.a(Graphics, x, y, dir):
  current cell id from animation row
  d.a(Graphics, cellId, x, y, dir)

d.a(Graphics, cellId, x, y, dir):
  for each part in o.e[cellId]:
    frameId, offsetX, offsetY, transformId
    rect = o.b[frameId * 5 .. +4]
    drawRegion(image[rect.imageSlot], rect.sx, rect.sy, rect.w, rect.h,
               mappedTransform, adjustedX, adjustedY, TOP|LEFT)
```

MIDP transform mapping arrays:

```text
packed id order -> MIDP transform constants

normal dir 0:
  [0,5,3,6,2,4,1,7]

horizontal mirror dir 1:
  [2,4,1,7,0,5,3,6]

both axes / 180-ish dir 3:
  [3,6,0,5,1,7,2,4]

vertical mirror dir 4:
  [1,7,2,4,3,6,0,5]
```

The alternate arrays `g/h/i/j` contain raw angle/flag-like values:

```text
[0,270,180,90,8192,8462,8372,8282] ...
```

But `d.s` is initialized `true` and no setter was found in this pass, so the active renderer path uses MIDP transform constants arrays `c/d/e/f`.

Callers:

- `f.a(Graphics, cameraX, cameraY)` draws actor sprite.
- If actor direction `n == 3`, it calls dir `1` for horizontal mirror.
- UI `m.java` can draw a sprite animation (`a == 3`) or a fixed cell (`a == 2`).

Status:

- VERIFIED: sprite frame composition, animation stepping, transform arrays, actor/UI callers.
- PARTIAL: exact visual parity of transforms must be validated against screenshots because Java SE needs its own MIDP `drawRegion` transform emulation.

## 7. `j.java`: Map Renderer

Map load `j.a(mapId)`:

```text
byte compactFlag
byte modId
if compactFlag == 1:
  byte widthTiles
  byte heightTiles
else:
  short widthTiles
  short heightTiles
byte tileSize
tileWidth = tileHeight = tileSize
byte layerCount
repeat layers:
  byte layerIndex
  byte layerType
  short recordCount
  records...
```

Layer storage:

| Type | Storage in `A[layer]` | Meaning |
| --- | --- | --- |
| `0` | dense `[width][height]`, initialized `-1`, stores `tile & 0xFFF` | Base/collision-ish dense layer. |
| `1` | dense `[width][height]`, initialized `-1`, stores raw tile short including transform bits | Dense visual tile layer with transform. |
| `2/3/4` | sparse `[recordCount][4]`: `[tileId, x, y, transform]` | Object/overlay sparse visual layers. |

Important code detail:

- For type `0`, code checks `B[by2] == 0`, not `B[i2] == 0`, in one branch. In decoded maps `by2` equals layer index in samples, so behavior matches normal expectation.
- For dense type `1`, transform bits are preserved in grid value and decoded at draw time.
- Sparse layers always store `tileId = raw & 0xFFF`, `transform = (raw & 0x7000) >> 12`.

Tile rect load `j.d()`:

```text
aq.b[modId] -> image ids for this mod
am.a(imgId) -> load each tileset image
/data/mod/mod_<modId>.mid:
  short rowCount
  repeat rowCount:
    byte imageSlot
    short sx, sy, w, h
```

Binary validation:

```text
mod_0 rows=215
mod_1 rows=164
mod_2 rows=260
mod_3 rows=214
mod_4 rows=347
mod_5 rows=133
mod_6 rows=101

sample map_0:
  compact=1, mod=1, size=23x23 tiles, tile=16, layers=5
  layer types/counts:
    0 type=0 count=529
    1 type=1 count=529
    2 type=2 count=134
    3 type=4 count=0
    4 type=3 count=0
```

Draw paths:

```text
types 0/1:
  draw dense grid into offscreen screen-sized Image C via Graphics D
  first render clears C with game.k.a(D,0,0,w,h)
  if camera changed:
    D.copyArea(...)
    redraw exposed strips only
  graphics.drawImage(C,0,0,TOP|LEFT)

types 2/3/4:
  iterate sparse rows
  visibility test via ae.a(...)
  drawRegion directly to target Graphics
```

Transform mapping:

```text
map transform bits = (tile & 0x7000) >> 12
draw transform = j.f[bits]
j.f = [0,5,3,6,2,4,1,7]
```

Collision/tile query:

```text
j.b(worldX, worldY):
  if outside map -> 1
  if A[0] missing -> -1
  if outside bounds -> 1
  return (byte)A[0][worldX/tileW][worldY/tileH]
```

This is used by `game.g` movement and `game.a` actor movement checks.

World draw order through `t.java`:

```text
t.a(Graphics):
  map layer 1
  map layer 2
  actor layer s=2
  actor layer s=1 sorted by Y
  map layer 3
  actor layer s=0
```

Special branch:

- When `k.a().c.P[2] == 2`, player/pet draw order is customized around tile value under player.
- This belongs to world/actor audit, but renderer port must preserve hook points.

Status:

- VERIFIED: map binary layout, mod layout, layer storage, transform bits, cache renderer, collision query.
- PARTIAL: exact `copyArea` dirty-strip parity should be screenshot-tested. A rebuild can first redraw full visible map every frame and later optimize.

## 8. `s.java` + `y.java`: Bitmap Font

`s` loads `/font.bin` statically.

Format:

```text
DataInputStream.readUTF() -> glyph charset string
byte height
for each glyph:
  byte glyphWidth
bitmap bits:
  rows = height
  columns = sum(glyphWidth)
  bit order = low bit first from each byte
```

Binary validation:

```text
font.bin:
  glyph count = 227
  readUTF byte length = 453
  font height = 10
  sum glyph widths = 1264
  bitmap payload bytes = 1581
```

Render:

```text
s.a(Graphics,char,x,y):
  lookup char -> glyph index
  for each set bit:
    graphics.drawLine(x+dx, y+dy, x+dx, y+dy)
  return glyph width

s.a(Graphics,String,x,y,anchor):
  MIDP-like anchor:
    bit 1 center horizontal
    bit 8 right
    bit 2 center vertical
    bit 0x20 bottom
  draw each glyph using bitmap pixels

y.a(String,...):
  thin wrapper around s.a()
```

Wrap:

```text
s.a(String,width):
  split into lines by glyph width
  if char is space and line width > width - s.b, wrap before/at space
  s.b = width("nhung1")
```

Status: VERIFIED.

## 9. `l.java` + `e.java`: RGB/Image Effects Helper

`e` is a mutable RGB buffer:

```text
int[] a pixels
int b width
int c height
int d/e anchor offsets
int f pixel count
```

`l` operations:

| Method | Meaning |
| --- | --- |
| `l.a(Image,e)` | Convert image to RGB buffer; white and black become transparent marker `0xFFFFFF`. |
| `l.a(d,cellId,bounds,dir,e)` | Render sprite cell into temporary image, then convert to RGB buffer. |
| `l.a(e,w,h)` / `l.a(e,scale)` | Nearest-neighbor scale. |
| `l.b(e,mul,add)` | Brightness/color multiply-add with clamp. |
| `l.b(e,alpha)` | Apply alpha to non-white/non-transparent pixels. |
| `l.a(Image)` | Creates a tinted/alpha version of an image. |
| `l.a(e,a,r,g,b)` | Recolor non-white pixels. |
| `l.a(e,texture,mode)` | Blend/fill with another RGB buffer; modes AND/OR/replace-like. |

Status:

- VERIFIED: RGB buffer mechanics and effect helper purpose.
- PARTIAL: exact visual naming of blend modes should be validated in `b.java` effect screenshots.

## 10. Port Checklist For New Java Project

Implement in this order:

1. `ResourceStream`: load from decoded/original path with exact byte order.
2. `BinaryTables`: implement `short[][]`, `byte[][]`, `String[][]`, sprite packed flat/matrix readers.
3. `ImageStore`: `am` equivalent with `load`, `releaseKeep`, `releaseUnload`.
4. `GlobalTables`: `aq` equivalent.
5. `SpriteMetadataStore`: `aa/o` equivalent including special `sprId 86..185`.
6. `SpriteAnimator`: `d` equivalent, with MIDP transform emulation.
7. `BitmapFont`: `s/y` equivalent from `/font.bin`.
8. `MapRenderer`: `j` equivalent; first full redraw, then optional `copyArea`.
9. `DisplayList`: `t` draw order and Y-sort.
10. `RgbEffectBuffer`: `l/e` equivalent for effects.

Minimum needed for intro/cutscene:

- `aq`, `ae` table readers.
- `am`, `aa/o`, `d`.
- `s/y` bitmap font.
- enough `j` if scene uses map background.
- display order equivalent for actor/map layering.

## 11. Remaining Validation

Not blocked for implementation:

- MIDP `drawRegion` transform mapping must be tested by rendering a known sprite with all transform ids.
- `j.copyArea` optimization can be skipped first, but final parity needs camera-scroll screenshot checks.
- `/data/tex/*.mid` PNG reconstruction should be verified against decoded texture outputs.
- `aq.c` domain names still need gameplay/battle audit; renderer only needs table access and icon/text refs.

Closed from previous checklist:

- `ae.g(String)` is no longer unknown: raw image vs packed PNG reconstruction branch is identified.
- Sprite binary block order is verified against original `spr_*_all(r)` files.
- Map layer format and transform storage are verified against original `map_*.mid` samples.
- Font bitmap format is verified against `font.bin`.
