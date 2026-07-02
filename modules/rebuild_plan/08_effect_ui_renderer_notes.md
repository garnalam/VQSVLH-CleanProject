# Effect And UI Renderer Notes

Pham vi audit:

- `b.java`: effect/transition manager.
- `ab.java`: UI screen manager/cache/stack.
- `ao.java`: `.ui` parser + UI runtime.
- `w.java`, `al.java`, `af.java`, `ac.java`: widget model.
- `k.java`, `m.java`, `z.java`, `a.java`, `ak.java`, `y.java`, `l.java`, `e.java`: UI render helpers.

Trang thai: VERIFIED/PARTIAL.

- `b.java`: lifecycle, channels, effect ids, caller-facing API da nam du de port khung effect. Mot so cong thuc pixel trong tung effect can compare bang anh goc khi can pixel-perfect.
- UI `.ui`: parser, manager, widget tree, input/focus, draw chain da nam du de port renderer. Mot so doan data-binding trong `z.java` bi CFR pha thanh `null`, can validate them bang caller/gameplay data khi port menu dong.

## 1. Tong Quan

Hai phan nay nam o cuoi render pipeline:

```text
game.e.paint()
  -> game.i.b(Graphics)
    -> world/map/actor renderer
    -> event/cutscene renderer
    -> ab UI manager
    -> b effect manager
```

`b.java` khong phai renderer sprite/map chinh. No la lop phu len man hinh: fade, shutter, block reveal, flash, shake, texture overlay, weather/particle.

`ab/ao/a/*` la mot UI runtime nho. File `.ui` khong chi la toa do anh; no mo ta cay widget, style, text, sprite/icon, navigation va focus.

## 2. `b.java` La Gi

`b` la singleton effect compositor:

```text
b.a() -> instance
```

Vai tro:

- giu effect dang chay;
- ve overlay len `Graphics` sau scene/UI;
- update counter frame/timing;
- bao caller biet effect da xong qua flag;
- quan ly mot so overlay rieng nhu weather particle.

Ba kenh effect:

| Field | Vai tro |
|---|---|
| `a` | main effect id |
| `g` | secondary effect id, thay trong case `12/13` |
| `h` | special UI-overlay effect id, thay trong case `18` |
| `b` | main effect complete flag |
| `c` | secondary effect complete flag |

Quy tac start effect:

```text
c(color, effectId):
  if effectId == 12 or 13:
    g = effectId
    c = false
  else if effectId == 18:
    h = effectId
  else:
    a = effectId
    b = false
  reset counters / init buffers theo effect
```

Dieu nay quan trong khi port: khong nen gom tat ca effect vao mot state duy nhat, vi ban goc co the chay main effect va secondary effect doc lap.

## 3. Bang Effect Id Trong `b.java`

| Id | Co che da xac dinh | Muc port |
|---|---|---|
| `0/1/2` | full-screen alpha/color fill bang `drawRGB`, dung color `n`, counter `k`, speed `m`, stripe height `w` | port duoc |
| `3` | radial/quadrant alpha mask bang `drawRGB` | port duoc, can compare pixel |
| `4/5` | radial/tile dissolve, private `d(Graphics)` | port duoc, can compare pixel |
| `6` | battle/world transition dang shutter/stripe, dung snapshot/map state va random variant `x` | port can map snapshot |
| `7` | chained `ah` particle/effect sequence, tao tu private `a(int)` | port sau khi audit `ah` ky hon |
| `8` | UI/scripted reveal sequence voi `game.h`, `ab`, static table `A`; ket thuc mo `/data/ui/npcEnemy.ui` | port can UI runtime |
| `9` | solid fillRect bang color `n`; event opcode 9 co route vao day | port duoc |
| `10` | screen flash trong `L` frame, color/param `M` | port duoc |
| `11` | camera shake/motion, goi `ai.a().d/e()` va table `B` | can port cung camera |
| `12/13` | secondary black-bar/camera-ish effect, dung params `G..K`, flag `c` | port duoc |
| `14/15` | fade texture/image overlay, load `/data/tex/<name>`, ve qua RGB wrapper `l/e` | can port texture |
| `17` | circle fillArc grow/shrink, params `S/T/U/R` | port duoc |
| `18` | special UI overlay channel, ve truoc main switch trong `a(Graphics)` | can inspect caller khi dung |
| `19/20` | block grid reveal/cover, static `X`, `W`, direction `c(-1)` / `c(1)` | port duoc |

Static tables quan trong:

- `A`: cac nhom id UI/event cho effect `8`, dang `{{0},{1},{2},{3},{4,5,6,7,8},...}`.
- `B`: motion/shake step table cho effect `11`.
- `X`: block-grid controller cho effect `19/20`.
- `W`: block-grid cell data/random order.

## 4. API Khoi Tao Effect

Cac overload trong `b.java` khong phai trung lap vo nghia; moi ham setup mot kieu effect:

```text
c(color, effectId)
  start effect id tong quat.

a(int n2, int n3, int n4)
  setup camera shake params C/D/E, goi ai.a().d((byte)3).

a(int G, int H, int I, int J, int K)
  setup secondary effect 12/13.

d(int L, int M)
  setup flash effect 10.

a(int S, int T, int U, int R)
  setup circle effect 17.

a(String texture, int x, int y, int speed)
  load /data/tex/<texture> cho effect 14/15.

a(int mode, byte count, byte dir, String[] texNames)
  setup weather/particle overlay tu texture.
```

Port rule:

- Tao `EffectManager` rieng, co `startMain`, `startSecondary`, `startOverlay`.
- Giu frame counter theo tick goc, khong dua vao wall-clock neu muon timing giong.
- Renderer can co abstraction `drawRgb`, `fillRect`, `fillArc`, `drawImage`, `setClip` tuong duong MIDP.

## 5. Cac Thu Tac Dong Den `b.java`

| Tac dong tu | Anh huong |
|---|---|
| `game.i` state machine | start loading/world/battle transitions |
| `game.c` event VM | opcode effect, dac biet opcode 9 |
| `game.d` battle engine | battle entry effects `6/7/8` |
| `game.k` world/map | cung cap map snapshot/camera state cho transition |
| `ai` camera helper | effect `11` va mot so shake/motion |
| `ah` effect actor | effect `7` particle/sequence |
| `ab/game.h` UI | effect `8` mo/dieu khien UI |
| `/data/tex/*` | texture overlay/weather |

## 6. UI Manager `ab.java`

`ab` la singleton quan ly cac man UI:

```text
ab.a() -> instance
```

Fields chinh:

| Field | Meaning |
|---|---|
| `Hashtable d` | cache path -> `ao` UI instance |
| `Vector e` | render stack/list cac `ao` dang mo |
| `Vector f` | path stack |
| `ao a` | active/current UI |
| `y b` | shared bitmap font wrapper |

Luot mo UI:

```text
ab.a(path, mode, handler):
  if path chua cached:
    tao ao
    gan shared y
    parse .ui
    cache theo path
  dua ao vao stack/render list
  gan active ao
  gan callback handler
```

Special case:

- `/data/ui/dialog.ui` duoc xu ly rieng: no co the giu cached/stack theo cach khac de phuc vu dialog/cutscene lien tuc.

Luot ve/update:

```text
ab.a(Graphics):
  draw all ao trong render list e
  reset clip

ab.c():
  tick active ao animation

ab.a(path):
  close/remove UI
  active = UI cuoi stack
```

## 7. `.ui` Parser `ao.java`

`ao` doc file binary `.ui` vao cay widget.

Header doc duoc trong decoded JSON:

```text
magic_or_flags = -1
version = 1
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

Thu tu parse container:

```text
read navigation count
  each entry: 4 bytes [key/action/step/wrap-or-target]

read style count
  each z style:
    style id 0/1
    enabled/depth/text/range data
    target widget ids
    slot rectangles/groups

read child count
  for each child:
    read type byte
    parse child fields
    recurse if container
```

Child type:

| Type | Class | Meaning |
|---|---|---|
| `0` | `al` | container, co child, style, navigation |
| `1` | `af` | visual/text/image control |
| `2` | `ac` | grid/list/selectable control |

Note:

- Scan nhanh decoded JSON thay nhieu `visual/container`, chua thay mau `grid` truc tiep. Nhung code parser co type `2`, nen rebuild van can implement `ac`.

## 8. Widget Model

### `w.java`

Interface/common base cho widget:

- id;
- position/size;
- parent/anchor;
- selected/focused draw;
- tick/update;
- cleanup.

### `al.java`

Container:

- giu children `w[]`;
- co optional style normal/selected `z`;
- co optional navigation map `byte[][]`;
- draw children theo thu tu;
- ho tro anchor/align theo parent hoac widget khac.

### `af.java`

Visual/text/image:

- chua renderer/content `k`;
- khong co children;
- draw text, border, fill, sprite/icon.

### `ac.java`

Grid/list/selectable:

- cell size, row/col, visible row/col;
- selected index;
- scroll offset;
- background visual `m`;
- cursor/selection visual `m`;
- item/cell navigation array `a[]`;
- direction 0..3 di chuyen selection.

### `a.java`

Cell/navigation item cho `ac`:

- optional visual `m`;
- 4 huong neighbor/index: `b/c/d/e`;
- flag `f` cho cell valid/selectable.

### `ak.java`

Rectangle plain data:

```text
x, y, w, h
```

Duoc dung de canh icon/sprite trong o UI.

## 9. UI Render Helpers

### `k.java`

Renderer noi dung cua `af`:

- text string;
- padding/align/scroll;
- colors normal/selected;
- fill/border;
- optional icon/sprite qua `m`;
- text draw qua `ae.a(...)` va `y/s` font chain;
- co special text `#P<number>` de ve progress bar.

### `m.java`

Wrapper sprite/image trong UI:

```text
m.a(spriteIndex, extendedAnim, animDir)
  -> tao root d sprite animator

mode a == 3
  -> draw animation state

mode a == 2
  -> draw fixed cell/frame
```

No can bounding box tu root `d` de canh trong `ak`:

- anchor `0/1/2`: top left/center/right;
- `3/4/5`: middle left/center/right;
- `6/7/8`: bottom left/center/right.

### `z.java`

Style/list repeater:

- `b[]`: target widget ids;
- `d`: visible slot count;
- `a`: total item count;
- `e`: first visible index;
- `f`: selected index;
- `g`: wrap allowed;
- `h/i`: scroll mode/offset.

No co the tam thoi dat lai toa do/kich thuoc widget vao slot style, draw selected/unselected, roi restore lai.

Risk:

- Mot so doan data-binding trong `z.a(w)` bi CFR render thanh `(null)`. Co kha nang day la logic nap text/k renderer tu data list. Khi port UI dong nhu bag/shop/task can validate bang caller gameplay.

### `y.java` Va `s.java`

`y` la wrapper cuc mong:

```text
y.a(text, x, y, anchor, color, Graphics)
  -> setColor
  -> s.a(...)
```

Text parity that su nam o `s.java` va `/font.bin`.

### `l.java` Va `e.java`

`e` la RGB image buffer:

```text
int[] pixels
width
height
offsetX
offsetY
length
```

`l` bien `Image` hoac sprite render thanh RGB buffer, scale, alpha, tint, blend. No duoc dung boi effect texture va mot so renderer can pixel buffer.

Note:

- `e.a()` co dau hieu decompile bug (`new e().a = ...` thay vi `e2.a = ...`). Khi port phai sua theo y nghia clone buffer, khong copy bug nay.

## 10. Input/Focus Flow UI

Input vao UI di qua active `ao`:

```text
ao.b(keyAction)
```

Flow:

- `ao` giu focus path bang 2 mang:
  - `e`: child-index path;
  - `f`: widget-id path.
- `c(widgetId)` build path tu widget id len root.
- `f()` rebuild id path.
- `e()` leo len ancestor focusable.
- `a(w, int, boolean)` tim focusable/selectable target.

Voi `ac`:

- directions `0..3` move grid selection;
- action/back-ish thay trong nhom `5/7`;
- movement goi `ac.a(byte dir)` va update neighbor/scroll.

Voi `al`:

- uu tien navigation map trong file `.ui`;
- neu khong co thi dung default traversal.

Callback:

- `ao` giu handler `i h`.
- Khi action/back/select, UI co the bao nguoc ve gameplay/caller qua handler.

## 11. UI Render Flow

```text
ab.a(Graphics)
  -> for each ao in render stack:
       ao.a(Graphics)
         -> root al draw
           -> style z neu co
           -> child draw
             -> af -> k -> text/icon/sprite
             -> ac -> background/items/cursor
```

Clip:

- `ab.a(Graphics)` reset clip sau khi draw UI.
- Widget draw co the set clip trong renderer con; port can co clip stack hoac reset dung diem.

## 12. Cac Thu Tac Dong Den UI

| Tac dong tu | Anh huong |
|---|---|
| `game.h` / UI handler | nhan callback select/back/action |
| `game.i` state machine | mo/dong UI theo state |
| `game.c` event/cutscene | dialog UI/text flow |
| `b.java` effect `8` | mo `/data/ui/npcEnemy.ui` trong sequence |
| sprite renderer `d` | UI icon/animated sprite |
| bitmap font `s` | text width/wrap/render |
| resource cache `am/aa/aq` | image/sprite refs trong UI |
| decoded `.ui` files | layout/id/navigation/style |

## 13. Port Order Cho Hai Phan Nay

1. Implement `EffectManager` shell: channels, flags, counters, start APIs.
2. Implement simple effects `0/1/2/9/10/17/19/20`.
3. Implement texture/RGB helper `e/l` subset for `14/15`.
4. Implement map snapshot transition `6`.
5. Implement `ab` UI manager and `ao` parser using decoded/binary `.ui`.
6. Implement widgets `w/al/af/ac`.
7. Implement content renderer `k/m/z`.
8. Wire input focus `ao.b(keyAction)`.
9. Add effect `8` after UI works.
10. Add effect `7/11/12/13/18` with camera/particle callers.

## 14. Ket Luan

Hai phan phuc tap nay da du thong tin de bat dau port khung renderer:

- `b.java` can duoc port nhu mot compositor rieng, khong tron vao map/sprite renderer.
- `.ui` can duoc port nhu mot runtime widget tree, khong dung cach hardcode toa do tung man.

Con can validate bang chay thu:

- pixel timing tung effect trong `b`;
- `z.java` data-binding cho list/menu dong;
- UI action callback mapping voi gameplay;
- cac UI type `2` neu gap trong file goc hoac caller runtime.
