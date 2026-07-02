# 20. Text / Cutscene Renderer Matrix - `game.j`

Muc tieu: dong co che renderer text/cutscene `game.j` de port intro/cutscene dung logic goc nhat co the, nhat la:

- typewriter;
- wrap theo bitmap font `s`;
- prompt "Nhan nut 0 de tiep tuc";
- paging / auto hide;
- layout text trong scene;
- lien ket voi opcode event `1/48/51/84`.

Trang thai: VERIFIED/PARTIAL.

Ket luan ngan: du de port intro/cutscene `mode 0` theo data that. Mode 0 co counter logic rat ky: no phu thuoc text bat dau bang color tag `#RRGGBB`; thong ke event data cho thay 22/22 text mode 0 deu bat dau bang `#`, nen nhanh nay duoc xem la VERIFIED theo data goc. Mode 3 da audit them bang `javap` tu class goc va Vineflower: day la branch broken/dead trong JAR hien co, khong co caller/data nao dung. Chua du de goi pixel-perfect 100% chi vi font MIDP/prompt/timing/layer van can screenshot validation khi implement, khong phai vi con mo logic intro.

## 1. Nguon da doc

- `source_code/decoded/decompiled_source_cfr/game/j.java`
- `source_code/decoded/bytecode_javap/game__j.javap.txt`
- `source_code/decoded/decompiled_source_cfr/game/c.java`
- `source_code/decoded/decompiled_source_cfr/s.java`
- `event/decoded/data__event__scene_0.mid.json`
- `event/decoded/data__event__scene_*.mid.json` thong ke opcode `1/48/51/84`
- `javap -c -p -v` chay lai truc tiep tren `game/j.class` trong JAR goc
- Vineflower 1.12.0 decompile lai `game/j.class`

Luu y quan trong: file nay noi ve `game.j` trong package `game`, la text/cutscene renderer. No khac voi `source_code/decoded/decompiled_source_cfr/j.java`, la map/mod renderer.

## 2. Vai tro tong quan

`game.j` la singleton text renderer duoc `game.c` giu trong field `D`. Moi tick event:

1. `game.c.b()` goi `D.d()` de cap nhat typewriter/scroll/paging.
2. `game.c.a(Graphics)` goi `D.a(Graphics)` de ve text len man hinh.
3. Opcode event setup text bang `D.a(x,y)`, `D.a(mode,text,layout)`, tuy truong hop goi them `D.b(width,height)` va `D.a(true)`.
4. Khi text blocking da hoan tat va nguoi choi bam confirm, `game.c` goi `D.b()` roi `D.c()`.

Vi vay `game.j` khong tu doc event. No chi render text/cutscene theo tham so ma event VM dua vao.

## 3. Field Matrix

Ten field bi obfuscate, nen bang nay ghi theo hanh vi thay duoc, khong dat ten qua chac khi dump bi hong.

| Field | Vai tro suy ra | Status |
| --- | --- | --- |
| static `c` | Render mode `0..3`. Setup tu tham so `by` trong `a(byte,String,int)`. | VERIFIED |
| static `a` | Co "text da hoan tat / co the confirm". `game.c` cho bam confirm khi flag nay true. | VERIFIED |
| static `b` | Renderer active. Neu false thi `a(Graphics)` khong ve text. | VERIFIED |
| `d` | Buffer char cua text hien tai. | VERIFIED |
| `o,p` | Vi tri start do opcode truyen vao. | VERIFIED |
| `E` | Do rong wrap = `an.w() - 2 * o` sau `a(x,y)`. | VERIFIED |
| `t` | Layout variant = `short_args[0] % 10`. Data hien tai dung `0`. Code co nhanh `1`. | VERIFIED/PARTIAL |
| `r` | So dong toi da/trang hoac tong so dong, tuy setup. Opcode `48/51` overwrite bang `height / an.G()`. | VERIFIED/PARTIAL |
| `v` | Wait-for-user flag. `a(boolean)` bo qua tham so va luon set `v=true`. `e()` tra ve `v`. | VERIFIED |
| `D` | Timestamp delay 2500 ms sau khi text ket thuc. | VERIFIED |
| `j[10]`, `k`, `l` | Vong dem mau parsed tu tag `#RRGGBB`; `l` giu mau cuoi de carry qua page. | VERIFIED/PARTIAL |
| `m,n` | Mode 2 scroll speed/offset. `m=2`, `n += m`. | VERIFIED |
| `s` | Counter da dung cho blink prompt, mode 1/2 done counter, mode 3 visible-line count. | VERIFIED/PARTIAL |
| `g,h,w,C` | Counter char/line/typewriter. Mode 0 dung duoc vi text data luon bat dau bang `#RRGGBB`: tick doc `d[w]`, gap `#` thi tang visible end `h += 7`, draw ve den `h`. | VERIFIED cho data goc / PARTIAL cho text khong co color tag |
| `x[][]`, `y` | Mode 3 line table va page index. `x` chi bi set `null` trong constructor; khong co `putfield x` nao khac trong bytecode goc. | BROKEN/DEAD in current JAR |

## 4. Caller / Opcode Matrix

### Opcode 1 - blocking text

Source pattern trong `game.c.n()`:

- setup neu event record chua vao state 5:
  - `D.a(short_args[1], short_args[2])`
  - `D.a((byte)(short_args[0] / 10 - 1), text, short_args[0] % 10)`
  - `D.a(true)`
  - record state = 5
- wait:
  - chi khi `game.j.a == true` va input `k(1)` moi goi `D.b()`
  - neu `game.j.b == false` thi `D.c()` va event advance

Y nghia: day la text blocking, dung cho cau chuyen/caption can bam `0` sau khi noi xong.

Data intro:

| Scene | Opcode | short_args | Meaning |
| --- | --- | --- | --- |
| scene 0 room 0 | `1` | `10,30,90` | mode 0, layout 0, x=30, y=90, wait confirm |

### Opcode 48 - positioned text box

Source pattern:

- setup:
  - `D.a(short_args[1], short_args[2])`
  - `D.a(mode,text,layout)`
  - if `short_args[5] == 1`, goi `D.a(true)`
  - `D.b(short_args[3], short_args[4])`
  - record state = 5
- tick:
  - neu `D.e() == true`, no se doi `game.j.a && input k(1)` giong opcode 1
  - neu `D.e() == false`, record quay ve state 1 va event tiep tuc; text renderer van active va tu type/auto-hide

Y nghia: day la text dat toa do, co the blocking hoac non-blocking. Intro dung no de ve text phia duoi trong khi scene phia tren van chay.

Data intro:

| Scene | Opcode | short_args | Meaning |
| --- | --- | --- | --- |
| scene 0 room 0 | `48` | `10,10,270,220,50,0` | mode 0, layout 0, x=10, y=270, box 220x50, non-blocking |

Voi man hinh 240x320, `y=270` va height `50` dat text o day man hinh. Phan scene con lai nam tren khoang `0..270`. Ty le nay khong phai dung 7:3 tuyet doi; theo so data la vung text cao khoang 50 px, con scene tren khoang 270 px. Neu co khung/nen UI rieng thi can verify bang screenshot.

### Opcode 51 - non-blocking setup after closing UI

Source pattern:

- `S.aB()`
- `D.a(x,y)`
- `D.a(mode,text,layout)`
- `D.b(width,height)`
- khong goi `D.a(true)`

Y nghia: text overlay non-blocking. Trong data decode chi thay 1 lan voi `short_args[0]=10`.

### Opcode 84 - UI formatted dialog, not direct `game.j`

Opcode 84 goi `game.h`:

- `S.a(title/body, resolvedText, style)`
- wait UI confirm qua `S.c(...)` va input mask `196640`

Y nghia: day la UI/dialog workflow, khong phai `game.j` renderer truc tiep. Khi port text/cutscene `game.j`, opcode 84 chi can link vao UI system rieng.

## 5. Event Data Usage

Thong ke tren `event/decoded/data__event__scene_*.mid.json`:

| Opcode | `short_args[0]` | Count | Mode |
| --- | --- | --- | --- |
| `1` | `10` | 9 | mode 0 |
| `48` | `10` | 12 | mode 0 |
| `51` | `10` | 1 | mode 0 |
| `84` | `-1` | 2 | UI dialog, khong vao `game.j.a(byte,...)` |

Cong thuc mode/layout:

- `mode = short_args[0] / 10 - 1`
- `layout = short_args[0] % 10`

Voi data hien tai, `10 -> mode 0, layout 0`. Chua thay event data goi mode `1/2/3`.

Call-site audit:

- `game.c` la noi duy nhat goi `D.a((byte)(short_args[0] / 10 - 1), text, short_args[0] % 10)`.
- `game.k` chi khoi tao singleton `game.j.a()`.
- Khong thay caller khac truyen mode `1/2/3` vao `game.j`.
- Toan bo opcode `1/48/51` trong decoded event data co `short_args[0] = 10`.

Kiem tra them rieng cho mode 0:

- Tong text mode 0 qua opcode `1/48/51`: 22.
- So text mode 0 khong bat dau bang `#`: 0.
- Cac prefix thay duoc: `#FFFFFF`, `#ffffff`, `#1c6c91`.

Day la bang chung quan trong vi bytecode mode 0 phu thuoc leading color tag. Neu dua text khong bat dau bang `#` vao renderer nay, counter `w/h` co the khong tien dung cach va khong nen coi do la use case goc.

## 6. Lifecycle Mode 0

Mode 0 la renderer quan trong nhat cho intro.

### Setup

Call order:

1. `D.a(x,y)`
   - set `o=x`, `p=y`
   - set `E=screenWidth - 2*x`
   - set `f=0`
2. `D.a((byte)0, text, layout)`
   - `d = text.toCharArray()`
   - `c = 0`
   - tinh line count theo width `E - 10` va reserve `s.b`
   - reset counter
   - `b=true`, `a=false`, `v=false`, `t=layout`
3. Neu blocking, caller goi `D.a(true)`:
   - `v=true`
4. Neu opcode `48/51`, caller goi `D.b(width,height)`:
   - `e = width / an.F()`
   - `f = (width - e * an.F()) >> 1`
   - `r = height / an.G()`

Luu y: voi opcode `48/51`, `r` bi set lai thanh so dong co the hien trong box. Voi opcode `1`, `r` giu line count tinh trong setup.

### Tick / Typewriter

`D.d()` duoc goi moi event tick.

Bytecode cho thay mode 0 chay loop 2 lan moi tick. Hanh vi thuc te voi data goc:

- text luon bat dau bang color tag;
- moi loop doc `d[w]`;
- neu `d[w] == '#'`, no tang visible end `h += 7` va khong tang `w`;
- vi `w` van o `0` va `d[0] == '#'`, moi tick tang `h` them 14 raw-char index;
- draw se parse tag mau va ve cac glyph co index `< h`.

Vay toc do dung theo bytecode/data khong phai 2 glyph/tick. No la 2 loop/tick, moi loop day visible end them 7 raw chars khi text bat dau bang `#RRGGBB`. Ky tu tag khong ve ra man hinh; phan glyph hien thi moi tick xap xi 14 raw chars tru di tag/color-control nam trong khoang do.

Ket thuc text:

1. Khi da di het buffer, neu `D == 0`, set `D = now + 2500`.
2. Sau 2500 ms:
   - neu `v == false`, clear text, set `game.j.b=false`, va neu `/data/ui/dialog.ui` dang mo thi dong qua `S.aC()`;
   - set `game.j.a=true`;
   - tang `s` de prompt blink / state counter.
3. Neu `v == true`, text khong auto clear; no doi caller nhan confirm.

Luu y audit: luc dau counter `h/w` nhin nhu damaged vi normal char branch tang `w` nhung draw dung `h`. Doi chieu data cho thay day la logic phu thuoc leading color tag: tat ca text mode 0 goc deu bat dau bang `#`, nen code luon di vao branch tag va tang `h`. Khi port, nen giu behavior nay cho data goc hoac viet renderer tuong duong theo visible-end index; khong nen ho tro text khong co leading color tag roi coi do la hanh vi goc.

### Draw

Mode 0 `a(Graphics)` goi private draw:

- layout `t=0`: draw tai `(o,p)`;
- layout `t=1`: draw tai `(o,p - an.G()/2)`.

Private draw dung bitmap font `s`:

- start x = `o`;
- start y = `p` hoac adjusted y;
- right limit = `E + startX - 10`;
- word reserve = `rightLimit - s.b`;
- moi glyph dung `s.a(char)` lay width;
- neu `x + glyphWidth > rightLimit`, wrap line;
- neu char la space va `x + glyphWidth > wordReserve`, wrap theo word reserve;
- khi wrap, y tang `s.a + 1`;
- draw glyph bang `s.a(Graphics,char,x,y)`.

### Confirm / Advance

Caller chi cho confirm khi `game.j.a == true`. Khi bam confirm:

- `D.b()` duoc goi;
- neu `game.j.b == false`, caller clear `D.c()` va advance event.

Trong flow opcode `1`, bam `0` truoc khi text xong khong co tac dung vi `game.c` check `game.j.a` truoc.

## 7. Prompt Matrix

Prompt duoc ve trong `a(Graphics)` sau phan text:

| Dieu kien | Gia tri |
| --- | --- |
| Wait flag | `v == true` |
| Text complete | `game.j.a == true` |
| Blink phase | `s % 10 < 5` |
| Mau | white `0xFFFFFF` |
| Text | `Nhan nut 0 de tiep tuc` trong source goc co dau Vietnamese |
| Position | `x = an.w() >> 1`, `y = an.x() - 8` |
| Anchor | `33` |
| Font | MIDP `Graphics.drawString`, khong phai bitmap font `s` |

Dieu nay giai thich dung hanh vi ban goc: cau dau ke xong moi hien prompt, nguoi choi bam `0` thi moi sang phan tiep.

## 8. Bitmap Font / Wrap Rules

`s.java` la bitmap font renderer:

- doc `/font.bin`;
- `s.a` la glyph height;
- `s.a(char)` tra ve glyph width;
- `s.a(Graphics,char,x,y)` ve tung pixel bang `drawLine(x,y,x,y)`;
- `s.b = s.a("nhung1")`, duoc dung lam word-wrap reserve;
- `s.a(String,width)` co helper wrap doc lap, nhung `game.j` tu wrap rieng.

`game.j` color tag:

- tag hop le theo pattern thuc te la 7 char, vi du `#FFFFFF`;
- parser lay du 7 char, ky tu `#` map thanh nibble `0000`, nen `#FFFFFF` thanh `0x0FFFFFF = 0xFFFFFF`;
- mau duoc dua vao ring buffer `j[10]`;
- draw se giu mau hien tai cho cac glyph sau tag.

## 9. Mode Matrix

| Mode | Setup / Draw | Tick | Data use | Status |
| --- | --- | --- | --- | --- |
| `0` | Bitmap font `s`, typewriter, wrap theo width, color tag. | 2 loop/tick; voi text goc bat dau bang `#`, visible end tang 14 raw chars/tick; 2500 ms delay, wait/auto-hide. | `opcode 1/48/51` deu dung mode 0. | VERIFIED cho data goc |
| `1` | Ve `ae.a[]` bang `Graphics.drawString`, khong typewriter. Layout `t=0/1`. | `a=true`, `s++` ngay. | Chua thay trong event data decode. | VERIFIED/PARTIAL |
| `2` | Ve `ae.a[]` bang `drawString` voi vertical offset `n`. | `n += 2`; ket thuc khi `n > ae.a.length * (screenH + screenH/2)`, roi `b=false`, `a=true`. | Chua thay trong event data decode. | VERIFIED/PARTIAL |
| `3` | Paged bitmap-font renderer dang le dung line table `x[][]`, nhung branch trong JAR hien co dung `null` truc tiep. | Neu vao nhanh nay se NPE/crash. | Khong co caller/data dung. | BROKEN/DEAD |

## 10. Mode 1 / 2 Details

Mode 1:

- `t=0`: draw tung dong `ae.a[i]` tai `(o, p + i*(G+G/2))`, anchor `20`;
- `t=1`: draw block center quanh `p`, anchor `17`;
- tick khong typewriter, set `a=true` va tang `s`.

Mode 2:

- giong mode 1 nhung cong them offset `n`;
- `n += m`, voi `m=2`;
- khi scroll qua nguong `ae.a.length * (an.x() + an.x()/2)` thi reset `n=0`, `a=true`, `b=false`, `s++`.

PARTIAL: `game.j.a(byte,String,int)` khong tu split text vao `ae.a[]` cho mode 1/2 trong decompile. Co the `ae.a` duoc set tu noi khac, hoac cac mode nay khong dung trong data hien co.

## 11. Mode 3 Dead/Broken Branch Audit

Ket luan sau audit lai: mode 3 khong phai mot nhanh dang duoc game data hien tai dung. No la nhanh broken/dead trong JAR dang audit.

- Constructor set `this.x = null`.
- Bytecode fresh tu class goc chi co mot `putfield #70 // Field x:[[I`, nam trong constructor, gia tri la `aconst_null`.
- Setup mode 3 co tao local `int[50][2]`, nhung khong bao gio gan local nay vao `this.x`.
- Draw/tick/manual advance mode 3 trong bytecode goc dung `aconst_null` truc tiep tai cac noi dang le phai doc line table:
  - draw: `aconst_null[0]`, `aconst_null[1]`;
  - tick: `aconst_null arraylength`;
  - manual advance: `aconst_null arraylength`.
- CFR va Vineflower deu decompile thanh `null`/`((Object[])null)`, nen day khong phai rieng loi CFR.
- Neu co caller thuc su vao mode 3, nhanh nay se NPE/crash; decoded event data khong co caller nhu vay.

Y do bi bo lai trong code:

- setup tao local line table `int[50][2]`;
- khi wrap, ghi vao local:
  - `lineTable[line][0] = lineStart`;
  - `lineTable[line][1] = currentIndex`;
  - `lineStart = currentIndex`;
  - `line++`.

Trang thai port: khong port mode 3 trong rebuild giai doan nay. Neu gap JAR/data khac co `short_args[0] = 40` hoac caller tuong duong mode 3, phai quay lai reconstruct rieng va can screenshot/reference moi. Voi JAR/data hien tai, bo qua mode 3 la dung hanh vi thuc te hon la co gang "sua" branch chet.

## 12. Layout Cho Intro Scene

Hai loai text trong intro scene 0:

| Doan | Opcode | short_args | Layout thuc te |
| --- | --- | --- | --- |
| Cau mo dau "Nghe don..." | `1` | `10,30,90` | mode 0, x=30, y=90, blocking, prompt sau khi type xong |
| "Vi Bach Long..." | `48` | `10,10,270,220,50,0` | mode 0, x=10, y=270, box 220x50, non-blocking |

Doan "Vi Bach Long" dung `y=270`, `height=50` tren man 240x320. Nhu vay text nam o day man hinh, scene/image/effect nam phia tren. Ty le theo so data gan `270:50`, khong phai 7:3 tuyet doi. Cam giac 7:3 co the den tu viec text box + prompt/nen UI chiem phan duoi, nhung can screenshot goc de pixel-validate.

## 13. Closed / Partial / Unknown

### CLOSED for port intro mode 0

- Opcode `1` wait flow va bam `0`.
- Opcode `48` positioned text, wait flag `short_args[5]`.
- Opcode `51` non-blocking overlay.
- Prompt condition, blink, position.
- Bitmap font `s` glyph width/draw.
- Color tag `#RRGGBB`.
- Wrap right limit va word reserve.
- Data intro mapping x/y/width/height.
- Counter/typewriter mode 0 cho data goc co leading color tag.
- Mode 3: CLOSED AS DEAD/BROKEN for current JAR/data, khong can port.

### PARTIAL

- Mode 0 voi text khong bat dau bang `#`: khong co trong decoded event data; khong nen suy ra la supported.
- Pixel-perfect timing theo ms: biet 2 loop/tick, visible end tang 14 raw chars/tick voi leading `#`, va delay 2500 ms; nhung event tick rate/repaint phai validate runtime.
- Prompt dung MIDP system font, nen Java SE rebuild can match font bang bitmap/MIDP emulation hoac chap nhan sai pixel.
- Mode 1/2 co draw/tick ro nhung khong thay data use.

### UNKNOWN

- Co version JAR khac co event goi mode `1/2/3` hay khong.
- Co version JAR khac co mode 3 da duoc compiler/obfuscator giu nguyen `x[][]` hay khong.

## 14. Port Notes

Khi implement rebuild:

1. Port `TextRendererMode0` truoc:
   - input: text, x, y, layout, optional box width/height, wait flag;
   - state: `active`, `complete`, `waitForInput`, `visibleIndex`, `lineStart`, `lineCapacity`, `finishDelayDeadline`;
   - tick: reproduce mode 0 data behavior: 2 loops/tick; with leading color tag, advance visible end by 14 raw chars/tick; skip color tags during draw;
   - draw: bitmap font + color tag + wrap.
2. Implement opcode `1/48/51` theo matrix tren.
3. Prompt:
   - chi ve khi wait flag true va complete true;
   - blink theo counter tuong duong `s % 10 < 5`;
   - bam `0` chi hop le sau complete.
4. Dung data intro scene 0 lam regression:
   - `opcode 1` cau dau phai doi bam `0`;
   - `opcode 48` "Vi Bach Long" phai ve o day man hinh va scene tren tiep tuc chay;
   - flag `short_args[5]=0` khong duoc hien prompt.
5. Khong implement mode 3 cho JAR/data hien tai. Neu muon de san interface, nem loi ro rang hoac log `unsupported dead branch mode 3` thay vi am tham sua logic.
6. Sau khi co screenshot goc, pixel-validate:
   - font width;
   - y baseline;
   - text color;
   - prompt font/anchor;
   - auto-hide sau 2500 ms;
   - overlay voi sprite/effect layer.

## 15. Ket luan

Da nam du de dung dung logic text/cutscene cho intro theo mode 0. Mode 0 khong con bi xem la counter-damaged cho data goc; no la logic phu thuoc leading color tag va da duoc doi chieu voi 22/22 text mode 0 trong event data. Mode 3 da duoc dong trang thai la branch broken/dead trong JAR hien co, khong can port cho rebuild intro/full data hien tai. Phan con can validate khi implement la pixel/timing/font/layer bang screenshot/video goc.
