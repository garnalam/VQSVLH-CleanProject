# Resource/Renderer Notes

Pham vi audit lan nay:

- `ae.java`
- `aq.java`
- `am.java`
- `aa.java`
- `o.java`
- `d.java` root package, sprite animation renderer
- `j.java` root package, map renderer
- `s.java`
- `f.java`, `n.java`, `game/a.java`, `t.java` as caller chain
- `b.java` effect manager
- `ab.java`, `ao.java`, `w/al/af/ac/k/m/z/a/ak/y/l/e`: UI `.ui` renderer/runtime

Trang thai: VERIFIED/PARTIAL cho resource + renderer core. Renderer primitive da co audit sau rieng trong [15_renderer_primitive_deep_audit.md](15_renderer_primitive_deep_audit.md). Effect `b.java` va UI `.ui` renderer da co audit rieng trong [08_effect_ui_renderer_notes.md](08_effect_ui_renderer_notes.md).

## 1. Resource Boot Order

Core bootstrap happens in `game.i.d()`:

```text
am.a()    // image cache arrays
aa.a()    // sprite cache array
aq.a()    // global resource tables
game.k.a()
game.k.i()
```

`aq.a()` loads global metadata:

```text
/data/script/sprite.mid  -> aq.a
/data/mod/modInfo.mid    -> aq.b
/data/script/chs.mid     -> aq.d text db
/data/script/npcDialog.mid -> game.k.N
/data/script/db.mid      -> aq.c gameplay db, 9 groups
/data/tex/tex_0..3       -> aq.e RGB arrays
/data/tex/bk             -> aq.f background texture
```

Resource dependency shape:

```text
World/event actor record
  -> spriteIndex
  -> aq.a[spriteIndex] = [sprId, imgId0, imgId1, ...]
  -> aa.a(sprId) loads /data/spr/spr_<sprId>_all(r)
  -> am.a(imgId) loads /data/img/img_<imgId>.mid
  -> d renderer combines sprite metadata + Image[]
```

## 2. `ae.java`: Binary And Image Helper

Main roles:

- opens resource streams through `aj.a(path)`;
- reads generic packed tables;
- decodes string tables;
- converts `.mid` image data into MIDP `Image`;
- provides geometry/random/text helper functions used by renderer and gameplay.

Important table readers:

```text
ae.a(InputStream):
  read short rowCount
  for each row:
    read short len
    read len shorts
  -> short[][]

ae.b(InputStream):
  same layout but byte rows
  -> byte[][]

ae.c(InputStream):
  read short rowCount
  for each row:
    read short stringCount
    for each string:
      read unsigned byte charCount
      if charCount == 255, read short charCount
      read charCount UTF-16-ish shorts
  -> String[][]
```

Packed sprite helper readers:

```text
ae.a(byte[], offsetRef):
  read short rows
  read short rowWidth
  read rows * rowWidth shorts into flat short[]

ae.b(byte[], offsetRef):
  read short rowCount
  read short rowWidth
  for each row:
    read short len
    read len * rowWidth shorts
  -> short[][]
```

Image loading:

```text
ae.b(path, name):
  raw = ae.g(path + name + ".mid")
  return Image.createImage(raw, 0, raw.length)
```

Audit note:

- Inventory confirms most `/data/img/img_*.mid` files are PNG bytes with `.mid` extension.
- `ae.f(String)` can reconstruct PNG chunks for custom packed image payloads, but common image path in this game enters through `ae.g()` then `Image.createImage`.
- For rebuild, prefer loading decoded PNGs when available; keep a raw `.mid` image loader for parity and missing assets.

## 3. `am.java`: Image Cache

Fields:

| Field | Meaning |
|---|---|
| `Image[] a` | image cache indexed by image id |
| `byte[] b` | reference count per image id |
| `c` | max size, set to 50000 |

Flow:

```text
am.a():
  allocate cache arrays

am.a(imgId):
  if image not cached:
    load /data/img/img_<imgId>.mid through ae.b()
  increment ref count
  return image

am.b(imgId):
  decrement ref count but keep image object

am.c(imgId):
  decrement ref count
  if <= 0, null image and return true
```

Rebuild rule:

- Keep reference semantics because map/sprite loaders call `am.b()` vs `am.c()` differently.
- Java SE can use `BufferedImage`, but the cache API should stay close to original.

## 4. `aq.java`: Global Metadata Tables

Fields:

| Field | Source | Meaning |
|---|---|---|
| `aq.a` | `/data/script/sprite.mid` | sprite table rows `[sprId, imgId0, imgId1, ...]` |
| `aq.b` | `/data/mod/modInfo.mid` | map mod id -> image ids used by tileset |
| `aq.c` | `/data/script/db.mid` | 9 groups of gameplay/db short tables |
| `aq.d` | `/data/script/chs.mid` | flattened text database |
| `aq.e` | `/data/tex/tex_0..3` | RGB arrays extracted from texture images |
| `aq.f` | `/data/tex/bk` | repeated background texture |

Important correction:

- Actor scene records usually store a sprite table index.
- That index is not the same as `imgId`.
- `aq.a[index][0]` is `sprId`, while `aq.a[index][1..]` are image ids.

## 5. `aa.java` And `o.java`: Sprite Metadata Loader

`o` is a passive sprite metadata container:

| `o` field | Meaning inferred |
|---|---|
| `a` | reference count |
| `b` | frame rectangle flat table, rows x 5: `[imageSlot, sx, sy, w, h]` |
| `e` | cell composition table, rows of `[frameId, offsetX, offsetY, transform]` |
| `f` | animation table |
| `d` | hitbox records remapped per cell |
| `c` | collision records remapped per cell |
| `g` | animation format flag, set by renderer caller |

Load flow:

```text
aa.a(sprId):
  if not cached:
    read /data/spr/spr_<sprId>_all(r) into fixed 20000-byte buffer
    o.b = ae.a(bytes, off)
    if sprId in 86..185:
      o.e = ae.b(bytes, off), then synthesize 5 directional variants
      o.f = ae.b(bytes, off), then replace with hardcoded default animation
    else:
      o.e = ae.b(bytes, off)
      o.f = ae.b(bytes, off)
    o.d = remap(ae.a(bytes, off), o.e.length)
    o.c = remap(ae.a(bytes, off), o.e.length)
  increment ref count
```

Special sprite ids `86..185`:

- `aa` creates 5 cell variants by offsetting `offsetX` with `{0, 10, 3, 7, -10}`.
- animation table is replaced by:

```text
{{2,0}, {1,0,1,1,1,2,1,3,1,2}, {5,0,5,4}}
```

Rebuild rule:

- Do not parse sprite binary independently from this sequence.
- The order of reads matters: frame rect -> cell -> anim -> hitbox -> collision.
- Must preserve the `86..185` synthetic branch.

## 6. Root `d.java`: Sprite Animation Renderer

This class is the true sprite renderer. It binds:

```text
spriteIndex
  -> aq.a[spriteIndex]
  -> Image[] from am
  -> o metadata from aa
```

Binding:

```text
d.a(spriteIndex, extendedAnimFlag):
  imageCount = aq.a[spriteIndex].length - 1
  images[i] = am.a(aq.a[spriteIndex][i + 1])
  metadata = aa.a(aq.a[spriteIndex][0])
  metadata.g = extendedAnimFlag
  set animation state 0
```

Animation state:

| Field | Meaning |
|---|---|
| `b` | current animation row/state |
| `q` | current keyframe index in animation row |
| `n` | duration counter for current frame |
| `r` | number of keyframes in current animation row |
| `p` | next/loop behavior: `-1` loop, `-2` hold last, `>=0` jump to state |

Animation table format:

- If `o.g == false`: each keyframe is `[duration, cellId]`.
- If `o.g == true`: each keyframe uses 4 shorts; renderer reads duration at `q << 2` and cell id at `(q << 2) + 1`.
- Some extra fields in the 4-short format are not yet fully named, but they affect indexing/timing shape.

Frame advance:

```text
d.d():
  if duration counter n > 0: decrement
  else advance q
  if q reaches r:
    p >= 0  -> switch to state p
    p == -2 -> hold last frame
    p == -1 -> loop to first frame
```

Drawing:

```text
d.a(Graphics, x, y, direction):
  current animation row -> current cell id
  d.a(Graphics, cellId, x, y, direction)
```

Cell drawing:

- `o.e[cellId]` is a sequence of 4-short parts:
  `[frameId, offsetX, offsetY, transform]`.
- For each part, renderer looks up `o.b[frameId * 5]` to find:
  `[imageSlot, sx, sy, w, h]`.
- It draws with MIDP:

```text
Graphics.drawRegion(image[imageSlot], sx, sy, w, h, transform, x, y, TOP|LEFT)
```

Direction/transform:

- direction `0` draws normal orientation.
- direction `1`, `3`, `4` mirror/rotate offsets and transform using lookup arrays.
- Transform lookup arrays map the sprite's packed transform id to MIDP transform constants.
- Root `f.java` usually passes direction `0`, except `n == 3` where it passes direction `1`.

Collision/hitbox:

- `d.j()` returns current collision/hitbox table from `o.c`.
- `d.k()` returns current table from `o.d`.
- Exact gameplay usage still belongs to entity/battle audit, but renderer exposes them by current animation frame.

## 7. Actor Render Chain

Scene actor flow:

```text
scene actor record
  -> game.a.a(short[] record, index)
  -> root f.a(spriteIndex, false)
  -> root d.a(spriteIndex, false)
  -> root f.a(animationState, nextState, reset)
  -> t display list
```

`game.a` maps actor state/direction to animation rows. Examples:

- type `0` with `v == 1 || v == 18` derives `h = state / 3` and combines `h * 3 + n`.
- normal type `0` calls animation row equal to state byte.
- some type `1` animation rows use `p == -2` to hold on final frame.

Display list:

```text
t.b():
  update camera
  update map visible bounds
  update actors in layer vectors
  sort middle layer by actor.j (Y)

t.a(Graphics):
  map layer 1
  map layer 2
  actor layer s=2
  actor layer s=1 sorted by Y
  map layer 3
  actor layer s=0
```

This is why actor foot/Y position matters. Rendering actors without the `t` layer order will look wrong even if sprite frames are correct.

## 8. Root `j.java`: Map Renderer

Map load:

```text
j.a(mapId):
  open /data/map/map_<mapId>.mid
  read compact flag/version byte
  read mod id
  load mod images and tile rects through d()
  read map width/height in tiles
  read tile size
  read layer count
  read each layer
  create offscreen image C sized to screen
```

Mod load:

```text
j.d():
  aq.b[modId] gives image ids for this tileset
  am.a(imgId) loads each image
  /data/mod/mod_<modId>.mid gives tile rect rows:
    [imageSlot, sx, sy, w, h]
```

Layer formats:

| Layer type | Meaning |
|---|---|
| `0` | dense tile grid, tile id stores lower 12 bits |
| `1` | dense tile grid, tile id may include transform bits |
| `2/3/4` | sparse/object layer rows `[tileId, x, y, transform]` |

Transform:

- map transform bits are `(value & 0x7000) >> 12`.
- renderer maps them through `j.f = {0,5,3,6,2,4,1,7}` before `drawRegion`.

Camera/cache:

```text
j.a(centerX, centerY):
  set camera a/b = center - half screen
  clamp to map bounds

j.c():
  calculate visible tile bounds

j.a(Graphics, layer, mode):
  for dense layers:
    render into offscreen C/D
    if camera moved, use D.copyArea and redraw exposed strips
    draw C to screen
  for sparse layers:
    draw visible object rows directly
```

Rebuild rule:

- For first playable rebuild, a simpler redraw-each-frame map renderer is acceptable.
- For pixel/performance parity, reproduce offscreen cache + `copyArea` scrolling.

## 9. `s.java`: Bitmap Font Renderer

`s` loads `/font.bin` statically.

Format:

```text
readUTF() -> glyph charset string
readByte() -> font height
for each glyph:
  readByte() -> glyph width
bitmap bitstream:
  low bit first
  rows = height
  columns = sum glyph widths
```

Render:

- `s.a(Graphics, char, x, y)` draws each set pixel with `drawLine(x,y,x,y)`.
- `s.a(Graphics, text, x, y, anchor)` implements MIDP-like anchor adjustment.
- `s.a(String, width)` wraps text using glyph widths.
- `s.b = width("nhung1")` is used as wrap threshold padding.

Rebuild rule:

- Use this bitmap font for cutscene/text parity.
- Do not replace with Java desktop fonts for final game UI unless intentionally debugging.

## 10. `b.java` Va UI `.ui`: Effect/UI Renderer

Status: VERIFIED/PARTIAL. Chi tiet xem [08_effect_ui_renderer_notes.md](08_effect_ui_renderer_notes.md).

`b.java` known role:

- singleton transition/effect manager;
- state fields `a/g/h` select active effect channel/type;
- boolean `b` marks effect complete;
- uses `drawRGB`, `fillRect`, `drawImage`, map snapshot and `game.d.a().c` for transitions.

Known effect relations from Runtime/Core:

- boot state starts effect id `19`;
- battle entry uses ids `6/7/8` depending `game.d.b`;
- event opcode 9 also routes into this manager.

Renderer observations:

- ids `0/1/2` are full-screen alpha/color fills using RGB buffers.
- id `3` uses quadrant radial buffers and `drawRGB`.
- ids `4/5/6` and later cases include snapshot/map-based screen transitions.

UI `.ui` known role:

- `ab.java` manages UI cache/stack/render list.
- `ao.java` parses binary `.ui` into a widget tree.
- `al/af/ac` are container/visual/grid widgets.
- `k/m/z` draw text/icon/sprite/style/list slots.
- input/focus flows through `ao.b(keyAction)`.

Rebuild rule:

- Keep `EffectManager` as a separate renderer subsystem.
- Keep UI as a widget runtime, not hardcoded per screen.
- First rebuild can implement simple effect ids plus `dialog.ui`/cutscene UI, then expand to menu/battle UI.
- Full parity requires screenshot comparison for each effect and runtime validation for `z.java` dynamic list binding.

## 11. Resource/Renderer Port Order

Recommended order:

1. `BinaryReader` equivalents for `ae.a/b/c` table formats.
2. `ImageStore` equivalent of `am`.
3. `SpriteTable` equivalent of `aq.a`.
4. `SpriteMetadataStore` equivalent of `aa/o`.
5. `SpriteAnimator` equivalent of root `d`.
6. `BitmapFont` equivalent of `s`.
7. `TileSet/MapRenderer` equivalent of root `j`.
8. `DisplayList` equivalent of `t`.
9. `EffectManager` subset, then full `b.java`.
10. UI `.ui` renderer from `ab/ao/a/*`.

## 12. Residual Risks

- `ae.g(String)` da duoc expand trong [15_renderer_primitive_deep_audit.md](15_renderer_primitive_deep_audit.md): `img_*/menu.mid` doc raw bytes, cac texture khac co the di qua packed PNG reconstruction.
- The 4-short extended animation format uses fields beyond duration/cell id; `d.java` itself only reads duration and cell id, but extra fields still need validation if another system consumes them indirectly.
- Exact transform parity depends on MIDP `drawRegion` constants and Java SE mapping.
- `b.java` exact pixel timing remains screenshot-compare work, but architecture/effect ids/API are mapped.
- UI `.ui` parser/runtime is mapped; `z.java` dynamic data-binding has CFR damage and must be validated through gameplay callers.
