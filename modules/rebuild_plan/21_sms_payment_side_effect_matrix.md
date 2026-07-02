# 21. SMS / Payment Side Effect Matrix

Muc tieu: dong phan SMS/payment o muc game-side behavior: nut nao mo flow, callback thanh cong/thua tac dong gi vao player/save/event, va phan nao chi la payment provider/VM khong can chan rebuild core.

Trang thai: VERIFIED/PARTIAL.

Ket luan ngan: game-side side effects da dong du de port full gameplay offline. `an` la noi thuc su ap reward khi SMS thanh cong. Voi source/JAR hien tai, flow trong `an` goi success truc tiep qua `b(true)`; `q` la payment Canvas co VM `/data/event/scene_13.mib`, nhung khong thay caller `new q(...)` trong source decode va field `an.u` cung khong duoc dung. Vi vay khong nen mac dinh `q` la duong payment dang chay trong ban hien tai. Phan con PARTIAL la provider config (`l2.bin`/`cpid.txt`) va network billing that tren may Java ME.

## 1. Nguon da doc

- `source_code/decoded/decompiled_source_cfr/an.java`
- `source_code/decoded/decompiled_source_cfr/q.java`
- `source_code/decoded/decompiled_source_cfr/r.java`
- `source_code/decoded/decompiled_source_cfr/u.java`
- `source_code/decoded/decompiled_source_cfr/v.java`
- `source_code/decoded/decompiled_source_cfr/c.java`
- `source_code/decoded/decompiled_source_cfr/x.java`
- `source_code/decoded/decompiled_source_cfr/a/a.java`
- `source_code/decoded/bytecode_javap/ag.javap.txt`
- `source_code/decoded/decompiled_source_cfr/game/h.java`
- `source_code/decoded/decompiled_source_cfr/game/k.java`
- `source_code/decoded/decompiled_source_cfr/game/d.java`
- `event/decoded/data__event__scene_13.mib.json`
- `root_misc/original/META-INF/MANIFEST.MF`
- `root_misc/original/cpid.txt`

## 2. Architecture Thuc Te

Flow game-side dang thay trong source:

1. World/battle/UI goi cac entry trong `game.h`: `aG/aH/aI/aJ` hoac body shop branch.
2. `game.h` set product id vao `an.o` bang `an.c(byte)` va mo `/data/ui/smsInfo.ui`.
3. `game.h.aM()` doc input cua `smsInfo.ui`/`smsTip.ui`.
4. Confirm goi `an.g(1)`.
5. `an.g(1)` set state gui tin `l=4`, hien tip, roi goi private `an.a()`.
6. `an.a()` tao `ag` neu can, notify `"sms://"`, sau do goi private `an.a(int)`.
7. `an.a(int)` trong source hien tai chi goi `this.b(true)`.
8. `an.b(true)` chi ap reward neu `l == 4`; sau do chuyen state `l=2`.

Flow payment Canvas/VM ton tai nhung khong thay duong goi:

1. `q` la Canvas rieng, load `/data/event/scene_13.mib`.
2. `q.run()` tao VM `a.g`, register native callbacks qua `r`, chay script.
3. `q.d()` callback ra interface `c.a(boolean)` voi dieu kien `f.i() >= r`.
4. Neu `q` duoc gan callback la `an`, no co the goi `an.a(boolean) -> an.b(boolean)`.
5. Nhung `rg "new q"` trong source decode khong thay caller; `an` co field `private q u;` nhung khong co read/write ngoai khai bao.

Y nghia port: rebuild core nen port `an` reward path truoc. `q/scene_13.mib/package a/*` chi can khi muon tai hien man payment carrier legacy, khong phai dieu kien de game reward/save dung.

## 3. Class Responsibility Matrix

| Class/file | Vai tro | Status | Ghi chu port |
| --- | --- | --- | --- |
| `an` | Chu so huu SMS state trong game, product id, callback success/fail, reward, text tip. | VERIFIED | Day la source-of-truth cho side effect gameplay. |
| `game.h` | UI workflow: mo `smsInfo.ui`, `smsTip.ui`, dispatch input confirm/back. | VERIFIED/PARTIAL | Entry points ro; exact widget drawing/focus da nam trong UI doc rieng. |
| `game.k` | World entry dispatch va save slot RMS. | VERIFIED | Save `an.X` vao `PK6_RMS_SMS`; money/badge/item/pet save nam o slot gameplay khac. |
| `game.d` | Battle shop dispatch toi purchase UI. | VERIFIED | Chi goi `game.h.aJ/aI/aH`. |
| `ag` | Async callback helper `x.b(true)` sau `notify`. | VERIFIED | Bytecode cho thay callback luon truyen `true`; khong doc noi dung SMS. |
| `q` | Legacy/payment Canvas, softkey UI, chay VM `scene_13.mib`. | VERIFIED/PARTIAL | Logic class ro, nhung khong thay caller trong source hien tai. |
| `r` | Adapter VM native -> `q.a(int,c,int)`. | VERIFIED | Thin wrapper. |
| `u` | Decode SMS provider config tu app property `sr` hoac `/l2.bin`; sinh destination/body. | VERIFIED/PARTIAL | Algorithm ro, gia tri provider chua can de core reward. |
| `v` | State model cho `q`: paid count, sent count, destination/body, unit/count. | VERIFIED | Nhieu setter ignore param (`g=2`, `f=1`, `h=false`) theo source. |
| `a/a.java` | VM builtin/native lib, gom `_s`, `_ss`, `_sp`, `_c`, `_t`, `_a`, `nc`. | VERIFIED/PARTIAL | Du de hieu payment VM; chua can port full VM cho gameplay. |
| `scene_13.mib` | Script VM cho payment UI. | PARTIAL | Constants/callback names ro; instruction semantics full cua VM khong can cho reward path hien tai. |
| `MessageConnection` | Java ME SMS API / stub trong moi truong decode. | PARTIAL/EXTERNAL | Real carrier billing khong the verify chi bang source decode. |

## 4. Product Side Effect Matrix

`an.c(byte)` set product id `o` va required count `q = 1` cho ca 5 product `0..4`, reset paid count `p = 0`.

Reward chi duoc ap trong `an.b(true)` khi `this.l == 4`. Neu callback lap lai sau khi da doi state sang `2`, no bi bo qua.

| Product | UI ten/source | Required SMS | Success side effect | Save path |
| --- | --- | --- | --- | --- |
| `0` | `Kich hoat` / `game.h.aG()` | 1 | `an.X=true`; cong money `+2000`; add item table `0`: id `1` qty `5`, id `4` qty `5`, id `11` qty `2`; badge `+5`; set event room `9,0` actor/event index `5` sang state `3`. | `an.X` -> `PK6_RMS_SMS`; money/badge -> `PK6_RMS_GOLD`; items -> item save slot; actor/event mutation -> world/event save. |
| `1` | `Tat trung cau` / `game.h.aH()` | 1 | Add item table `0`: id `0` qty `1`. | Item save slot. |
| `2` | `Mua sam kim tien` / `game.h.aJ()` va body shop branch `c=3` | 1 | Money `+10000`. | `PK6_RMS_GOLD`. |
| `3` | `Mua dang cap` / body shop branch `c=1` | 1 | Moi pet trong bag: neu level 50 thi goi `J()`; neu duoi 50 thi goi `x()`, cong toi da `+5` level nhung cap 50, goi `I()`, neu dat dieu kien evolution thi day pet vao `game.k.E` va index vao `game.k.F`; set `game.k.G=1` neu co evolution, `2` neu khong. | Pet save slot `PK6_RMS_POKPET`; evolution transient state trong world flow. |
| `4` | `Mua sam huy hieu` / `game.h.aI()` va body shop branch `c=2` | 1 | Badge `+10`. | `PK6_RMS_GOLD`. |

Mo ta text trong `an.t` khop voi side effect:

- product `0`: kich hoat game, 1 SMS 15000d.
- product `1`: tat trung cau, 1 SMS 10000d.
- product `2`: +10000 kim tien, 1 SMS 10000d.
- product `3`: tat ca pet trong ba lo +5 level, 1 SMS 10000d.
- product `4`: +10 huy hieu, 1 SMS 10000d.

## 5. UI Entry Matrix

| Caller | Case/branch | Product | UI action |
| --- | --- | --- | --- |
| `game.k` world action case `100` | `S.aG()` | `0` | Open activation SMS info. |
| `game.k` world action case `101` | `S.aH()` | `1` | Open catch-ball purchase SMS info. |
| `game.k` world action case `102` | `S.aJ()` | `2` | Open money purchase SMS info. |
| `game.k` world action case `104` | `S.aI()` | `4` | Open badge purchase SMS info. |
| `game.d` battle menu case `102` | `S.aJ()` | `2` | Open money purchase from battle context. |
| `game.d` battle menu case `104` | `S.aI()` | `4` | Open badge purchase from battle context. |
| `game.d` battle menu case `101` | `S.aH()` | `1` | Open catch-ball purchase from battle context. |
| `game.h.bt()` body shop branch `c=1` | `o.c((byte)3)` | `3` | Level purchase. |
| `game.h.bt()` body shop branch `c=2` | `o.c((byte)4)` | `4` | Badge purchase. |
| `game.h.bt()` body shop branch `c=3` | `o.c((byte)2)` | `2` | Money purchase. |

`game.h.aM()` input state:

| `an.N()` state | Input | Side effect |
| --- | --- | --- |
| `0` | confirm mask `131072` | Neu required count > 1 thi `an.d(1)`, nguoc lai `an.g(1)` start send. Trong data product hien tai required count luon 1. |
| `0` | back mask `786432` | Close `smsInfo.ui`, `an.d(5)`, restore previous game state. |
| `1` | confirm `131072` | `an.h(1)` -> start send. |
| `1` | soft/back `262144` | `an.h(2)` -> cancel/close branch. |
| `2` | UI animation/confirm mask `917504` sau khi done | Neu `an.M()` success thi close `smsInfo.ui` + `smsTip.ui` va restore state; neu chua success thi `an.d(5)`. |
| `3` | mask `393216` | `an.h(1)` -> close failure tip. |

## 6. `an` State Matrix

| State `l` | Setter | Meaning | UI text/action |
| --- | --- | --- | --- |
| `0` | `d(0)` / after `d(5)` loop | Idle/info state. | No `smsTip` forced. |
| `1` | `d(1)` | Progress/count state. | Shows message id `513` with `{q,p}`. |
| `2` | `d(2)` | Success/result state. | If `M()` true, shows message id `515`; product `0` appends id `633`; else id `516`. |
| `3` | `d(3)` | Failure state. | Shows message id `516`. |
| `4` | `d(4)` | Sending state. | Shows message id `514`; only this state allows `b(true/false)` to apply. |
| `5` | `d(5)` | Close/cancel. | Sets `T=false`, closes `smsTip.ui`, then loops to `0`. |

## 7. Callback / SMS Execution Matrix

### `an` path actually used by current source

| Method | Behavior | Status |
| --- | --- | --- |
| `an.g(1)` | Set global `T=true`, `d(4)`, call private `a()`. | VERIFIED |
| `an.a()` | Create `ag((x)this)` if null; call `ag.a("sms://")`; switch product `0..4` all call private `a(int)`. | VERIFIED |
| `an.a(int)` | Calls `this.b(true)`. | VERIFIED |
| `ag.run()` | After `notify`, if callback exists, calls `x.b(true)`. | VERIFIED from bytecode |
| `an.a(boolean)` | Delegates to `b(boolean)`. | VERIFIED |
| `an.b(boolean)` | If `l==4`, apply success/fail; success increments `p` and `m[o]`, applies reward when `p>=q`, then `d(2)`; fail -> `d(3)`. | VERIFIED |

Note: `ag` and direct `a(int)->b(true)` can both be success sources, but reward is protected by `l==4`. Sau first success, `d(2)` makes `l=2`, so later duplicate callback does not apply reward again.

### `q` payment Canvas path

`q` exists and is internally coherent, but current source decode has no instantiation. If a rebuild wants to support it as optional legacy payment screen, matrix is:

| Native name | `q` case | Behavior |
| --- | --- | --- |
| `ca` | `0` | Returns `0`. |
| `gi` | `1` | Get/set paid count `v.i`. |
| `gs` | `2` | Return required count `q.r`. |
| `gg` | `3` | Return unit/price-ish `v.g`. |
| `gj` | `4` | Get/set sent count `v.j`. |
| `yc` | `5` | Calls `ao.a(v,n,o,p,q,r)` to configure SMS/provider fields. |
| `m` | `6` | Set left softkey text. |
| `n` | `7` | Set right softkey text. |
| `as` | `8` | Set display message text. |
| `_fc` | `9` | Return `u.a` destination. |
| `_fb` | `10` | Return `u.b` body. |
| `ts` | `11` | Return `q.s`. |
| `aa` | `12` | Consume boolean, call `q.c()` to refresh prompt/status. |

`q.keyPressed(int)` is CFR damaged but structurally readable:

- left softkey / confirm variants call private `g()`;
- right softkey / back variants call private `h()`;
- key `50` / game action `1` scroll up;
- key `56` / game action `6` scroll down;
- Motorola branch remaps softkey codes.

`q.g()` starts a thread to run VM only when left text is `"Gui tin"` and required count `f.a() > 0`. `q.h()` calls callback `d()` when user exits and passes `f.i() >= r`.

## 8. VM / Provider Config Matrix

| Piece | What is known | Status |
| --- | --- | --- |
| `/data/event/scene_13.mib` | VM tree constants include `gi`, `gs`, `as`, `gj`, `_ss`, `_s`, `_sp`, `yc`, `aa`, `_fc`, `_fb`; strings show send-progress/failure UI. | VERIFIED/PARTIAL |
| `a/a.java` case `12` `_s` | With 3 args: create `TextMessage`, set address/body, call `lavax.wireless.messaging.MessageConnection.send`, close connection, return message or null on exception. | VERIFIED |
| `a/a.java` case `13` `_ss` | Open `Connector.open(string)` and return `MessageConnection` or null. | VERIFIED |
| `a/a.java` case `21` `_sp` | Return lowercased `System.getProperty(name)`. | VERIFIED |
| `u.a(MIDlet)` | Reads app property `sr`; if missing reads `/l2.bin`; verifies checksum-like 5-byte tail; splits config into destinations, prices/counts, body templates, display strings. | VERIFIED/PARTIAL |
| `u.a(int)` | Builds `u.a` destination and `u.b` body, replacing `%1` with `uid`, `%2` with `Term`, `%cp` with `RefCode`. | VERIFIED |
| `cpid.txt` | Contains encoded/payment config-ish rows. Not needed by current reward path. | PARTIAL |
| Real carrier billing | Not reproducible from source alone; current local stubs do not prove real device/payment outcome. | UNKNOWN/EXTERNAL |

## 9. Save / RMS Side Effect Matrix

Slot names in `game.k.au`:

| Index | Name | SMS relation |
| --- | --- | --- |
| `3` | `PK6_RMS_RMS` | Misc world/global flags, not SMS count. |
| `4` | `PK6_RMS_SMS` | Stores only `an.X` boolean activation flag in `game.k.ab()/ac()`. |
| `5` | `PK6_RMS_CNTSMS` | Name exists, but no `af[5]` read/write found in decompiled source or javap search. Mark UNUSED/UNVERIFIED. |
| `6` | `PK6_RMS_GOLD` | Stores money and badge count; used by product `0/2/4`. |
| `7` | `PK6_RMS_POKPET` | Stores pets; used by product `3`. |
| `8` | `PK6_RMS_CONITEM` | One inventory vector slot; exact product relation depends item table. |
| `9` | `PK6_RMS_PETBALL` | One inventory vector slot; product `1`/activation items likely end up through inventory save path. |

Static `an.m[5]` increments per successful product, but no save/load path was found. Therefore SMS count persistence is not verified; do not map it to `PK6_RMS_CNTSMS` unless future bytecode/resource evidence appears.

## 10. Closed / Partial / Unknown

### CLOSED

- Game-side reward effects for product `0..4`.
- Product required count is `1` for all current products.
- Reward only applies in sending state `l==4`.
- Success path in current `an` source calls `b(true)` directly.
- `ag` bytecode callback always invokes `x.b(true)`.
- `PK6_RMS_SMS` stores activation boolean `an.X`.
- Money/badge/pet/item rewards use existing gameplay save paths, not special SMS-only save path.
- `PK6_RMS_CNTSMS` has no read/write found in current decoded source/bytecode search.
- `q` internal VM callback table and key flow are understandable enough if needed.

### PARTIAL

- `q` payment Canvas is not wired by visible source; it may be dead/legacy/library code or invoked only in a variant not present here.
- `scene_13.mib` full instruction-level semantics are not decoded into pseudocode, only constants/callback interface and role are known.
- Provider config values from `l2.bin`/`cpid.txt` have not been fully materialized into human-readable carrier table because rebuild core does not need them.
- Exact UI focus/draw behavior of `smsInfo.ui`/`smsTip.ui` should be validated when implementing UI, though input side effects are clear.

### UNKNOWN / EXTERNAL

- Real Java ME carrier billing success/failure semantics on original device/network.
- Whether another VQSV JAR variant instantiates `q` or uses `PK6_RMS_CNTSMS`.
- Whether production server/operator response was expected beyond local `MessageConnection.send`.

## 11. Port Notes

Recommended rebuild approach:

1. Implement `PaymentService.completePayment(productId, success)` around `an` side effects.
2. Preserve state gate: only apply callback while payment state is `SENDING` (`l==4` equivalent).
3. Save activation flag separately as `smsActivated` (`PK6_RMS_SMS` equivalent).
4. Save money/badge/items/pets through normal gameplay save modules.
5. Treat `smsCountByProduct` (`an.m`) as volatile unless a future evidence shows `PK6_RMS_CNTSMS` read/write.
6. Stub carrier send in core rebuild:
   - default success for offline/testing builds;
   - optional fail path to test `d(3)` UI.
7. Delay porting `q` + VM `scene_13.mib` until after core game works. If ported, mark it as optional legacy payment screen, not mandatory main path for this source.

## 12. Ket Luan

Phan SMS/payment khong con la blocker cho rebuild core. Thong tin quan trong nhat da dong: thanh cong SMS tao reward nao, save vao dau, va flow input nao kich hoat no. Phan chua dong la payment provider thuc va legacy `q` Canvas/VM, nhung nhung phan do khong tac dong them vao gameplay ngoai viec cuoi cung goi callback success/fail.
