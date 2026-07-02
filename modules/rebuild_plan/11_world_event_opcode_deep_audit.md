# World/Event Opcode Deep Audit

Nguon doc trong pass nay:

- `source_code/decoded/decompiled_source_cfr/game/c.java`
- `source_code/decoded/decompiled_source_cfr/game/g.java`
- `source_code/decoded/decompiled_source_cfr/game/h.java`
- `source_code/decoded/decompiled_source_cfr/game/d.java`
- `source_code/decoded/decompiled_source_cfr/game/i.java`
- `source_code/decoded/decompiled_source_cfr/p.java`
- `event/decoded/*.json`

Trang thai: ACTIONABLE, nhung van tach ro `VERIFIED`, `PARTIAL`, `UNKNOWN`.

Tai lieu nay la pass audit sau cua `10_world_event_opcode_matrix.md`. Matrix `0..88` van nam o file 10; file nay ghi sau hon cac nhom opcode can port ky: branch/condition, inventory/task/reward, UI dialog/choice, world transition, battle trigger.

## 1. Rule Quan Trong Cua Event VM

`p.java` xac nhan mot event record co:

```text
p.a()  -> event state e
p.a(x) -> set event state
p.b()  -> event id trong room
p.b(x) -> set command pointer d
p.c()  -> current command = c[d]
p.d()  -> first command, dung cho trigger
p.e()  -> d++, wrap ve 0 neu qua cuoi
```

Trong `game.c.n()`, sau khi chay switch:

```text
if state != 5 && state != 6:
  p.e()

if state == 3 || state == 4:
  remove khoi active list
  persist vao game.c.b[roomLinearIndex][eventId]
```

He qua port:

- `state 5` = command dang wait, khong auto advance.
- `state 6` = blocked/paused, khong auto advance.
- Branch opcodes hay goi `p.b(target - 2)` vi sau switch VM se auto `p.e()`.
- Complete state `3/4` phai persist vao `game.c.b`, khong chi remove runtime.

## 2. Branch / Condition Opcodes

| Opcode | Phase | Verified behavior | Status |
|---|---|---|---|
| `13` | trigger + executor | Trigger: player collision with rectangle. Executor: neu player nam trong rectangle thi set state `1` va stop player; nguoc lai set state `6`. | VERIFIED |
| `15` | trigger only | Kiem tra event khac o world/room khac da state `3` hoac `4`. Khong co executor case. | VERIFIED |
| `16` | trigger + executor | Kiem tra selected actor `game.k.u`. Trigger doi `game.c.h`; executor neu actor dung thi bat interaction flag `g`, doi `h`, roi set state `2`; sai actor thi state `6`. | VERIFIED |
| `38` | executor | Duyet list actor id trong `c()[0]`; neu selected actor match va da press/interact `h`, branch theo list target trong `c()[1]`, clear `u/g/h`. Cuoi case luon set state `6`, nen branch chi co hieu luc khi matched/confirmed truoc do. | VERIFIED/PARTIAL |
| `41` | executor | Unconditional branch: set command pointer `b()[0] - 2`. | VERIFIED |
| `42` | executor | Mark event state `4`; loop end se remove + persist. | VERIFIED |
| `47` | executor | Branch theo field `this.l`: neu `l != -1`, target = `b()[l] - 2`. `this.l` duoc set ngoai case nay, can trace caller khi port quest branch. | PARTIAL |
| `62` | executor | Duyet actor list `c()[0]`, tim actor co `h()==2`; neu actor tim duoc bang `b()[0]` thi branch `b()[1]`, nguoc lai branch `b()[2]`. Ban dau set state `6`. | PARTIAL |
| `65` | executor | Lan dau goi runtime/module state `y.a(100)` va wait state `5`; sau do branch theo static flag `game.c.X`: true -> `b()[0]`, false -> `b()[1]`. | PARTIAL |
| `71` | executor | Branch theo `x.F >= threshold`: true -> `b()[1]`, false -> `b()[2]`. Domain cua `x.F` chua dat ten chac. | PARTIAL |
| `74` | executor | Branch theo first item cua vector `x.K`: `((int[])x.K[0])[1] > 0`. Vi `K` khoi tao co `{0,0,1}`, day co ve la check inventory/slot dac biet. | PARTIAL |
| `81` | executor | Neu `b()[0]==0`, check money-like `x.t(amount)`; neu `b()[0]==1`, check badge/currency-like `x.v(amount)`. True -> `b()[2]`, false -> `b()[3]`. | VERIFIED |
| `83` | executor | Lan dau goi `y.a(30)` va wait; sau do branch theo static index `game.c.m`: target = `b()[m] - 2`. | PARTIAL |
| `85` | executor | Branch theo counter `this.p`: neu `0 <= p < 5` -> `b()[0]`, nguoc lai -> `b()[1]`. | PARTIAL |
| `88` | executor | Branch theo capacity status `x.y() == 2`: full/no-space -> `b()[0]`, otherwise -> `b()[1]`. | VERIFIED |

Trigger-only condition opcodes da verify trong `game.c.b()`:

| Opcode | Trigger condition | Status |
|---|---|---|
| `43` | Actor interaction + helper `a(ad)`. Helper co precondition event khac complete va modes: flag `T`, always true, pet/count/id, `x.F`, item state, static `t`. | PARTIAL |
| `44` | Actor interaction + helper `b(ad)`. Modes: item state, flag `T`, event complete, has item qty, `x.F`, all four element/types present among active pets. | PARTIAL |
| `57` | Special parent actor condition: player parent actor must match type/state/id; then either reset parent actor or activate event. | PARTIAL |
| `59` | Wait until listed actors have `h()==0`. | VERIFIED |
| `61` | Same wait style as `59`, plus stops player direction during trigger handling. | VERIFIED |
| `69` | Selected actor equals param. | VERIFIED |
| `73` | Inventory/item quantity condition through `game.g.a(itemId, category?) >= 2`; exact category semantics still needs sample validation. | PARTIAL |
| `75` | `x.L` non-empty. `L` is unique/special low-id category-2 list, not normal stack inventory. | PARTIAL |
| `78` | Multiple referenced events complete state `3/4`. | VERIFIED |
| `79` | Referenced event complete + `!x.k(0)` + selected actor equals param. `x.k(0)` checks vector `N` active marker. | PARTIAL |
| `86` | Referenced event complete state `3`. | VERIFIED |

## 3. Inventory / Task / Reward Opcodes

`game.g` storage fields observed:

| Field | Observed role | Status |
|---|---|---|
| `J` | Item vector for category 0 items where `aq.c[4][id][5] != 0`. | PARTIAL |
| `K` | Item vector for category 0 items where `aq.c[4][id][5] == 0`; initialized with `{0,0,1}`. | PARTIAL |
| `L` | Unique/special list for category 2 id `< 12`; entries are `[id, marker, aux]`. | PARTIAL |
| `M` | Stack vector for category 2 id `>= 12`; id `17` adds `qty * 5`. | PARTIAL |
| `N` | Task/temporary progression list. `i/c/d/e/j/k` mutate `[id, marker, qty]`; ids `7/8/9` have special qty behavior. | PARTIAL |
| `O` | Pet/creature storage/bank; capacity 100. | VERIFIED |
| `z/A` | Active pet slots, max 6. | VERIFIED |
| `T` | 21 boolean flags, toggled by opcode `20`, checked by trigger helpers. | VERIFIED |
| `af` | Money/gold-like currency. Message text says `kim tien/kim`. | VERIFIED |
| `ag` | Badge/secondary currency. Message text says `huy hieu`. | VERIFIED |

Item helper behavior:

```text
a(id, qty, vector) -> can add/check capacity, stack cap 99
b(id, qty, vector) -> has enough
c(id, qty, vector) -> add/create, cap 99
d(id, qty, vector) -> subtract/remove if qty <= 0 and marker == 0
```

Opcode details:

| Opcode | Behavior | Wait | Status |
|---|---|---|---|
| `17` | Add/remove category 0 item using `x.a/b/c/d(id, qty, 0)`. Shows `Dat duoc` or `Mat` openbox. | Wait `S.ax()`. | VERIFIED |
| `18` | Add/remove category 2 item using `x.a/c/d(id, qty, 2)` and display name from `aq.c[3]`. | Wait `S.ax()`. | VERIFIED |
| `19` | Reward for group `aq.c[5]`; uses `x.d(id, qty)`, `x.c(id, qty)`, `x.e/i`. Exact game-domain of `N` still partial. | Wait `S.ax()`. | PARTIAL |
| `20` | Set/clear flag `x.T[index]`, show openbox string from command text. | Wait `S.ax()`. | VERIFIED |
| `31` | Add/remove `af` or `ag`: `b()[1]==0` money, `b()[1]==1` badge. | Wait `S.ax()`. | VERIFIED |
| `36` | Add/remove pet: `x.y()` decides active slot, bank, or release; add uses `game.b.b(...)` for generated data when banked. Remove uses `x.n(id)`. | Wait `S.ax()`. | VERIFIED/PARTIAL |
| `39` | Calls `I()` on all active pets `x.z[0..A)`. | Immediate. | PARTIAL |
| `50` | Calls `x.u()` when param `0`, else `x.t()`. No usage found in decoded event JSON, so domain remains unknown. | Immediate. | UNKNOWN/PARTIAL |
| `53` | Sets player/pet mode through `x.a(byte, byte, mode)`, can refresh actors with `v==1`, then waits input/UI. | Wait `w.k(1)`. | PARTIAL |
| `63` | Calls `x.h(value)` or `x.s()`, then sets VM flag `this.k`. No decoded usage found. | Immediate. | PARTIAL |
| `80` | Timed challenge/reward: param 0 starts countdown/time; param 1 ends, rewards pet/item/money based on elapsed seconds. | Wait countdown or openbox. | PARTIAL |
| `87` | Add/remove pet variant with extra first/id param passed to `x.a(extraId, species, level, ...)`. | Wait `S.ax()`. | PARTIAL |
| `88` | Capacity branch using `x.y()`: `0` active slot, `1` bank space, `2` no space. | Immediate branch. | VERIFIED |

## 4. UI Choice / Dialog Opcodes

Verified `game.h` UI endpoints:

| Method | UI file/effect | Used by |
|---|---|---|
| `S.a(String,int)` | `/data/ui/openbox.ui`, text + quantity | reward item |
| `S.b(String)` | `/data/ui/openbox.ui`, text only | reward/lost/currency |
| `S.ax()` | true when openbox closed | wait reward messages |
| `S.c(String)` | `/data/ui/taskTip.ui` | task tip/message |
| `S.ay()` | true when task tip closed | wait task tip |
| `S.aB()` | open `/data/ui/dialog.ui`, hide option widgets | setup dialog |
| `S.a(speaker,text,mode)` | `/data/ui/dialog.ui`, text widget 14, widgets 12/13 by mode | dialog opcode |
| `S.aC()` | close dialog | dialog complete |
| `S.c(mode,-1)` | dialog ready check; `-1` bypasses animation wait | dialog wait |
| `S.a(uiIndex, visibleCount, options, title)` | `/data/ui/option.ui`, `answer.ui`, `wharf1.ui` | choice opcode `35` |
| `S.c(uiIndex)` | returns selected option or `-1` | choice opcode `35` |
| `S.a(types,ids,labels,branches)` | `/data/ui/taskOption.ui` with item icons/text | choice opcode `49` |
| `S.aD()` | returns selected task option or `-1`; cancel returns `1` | choice opcode `49` |
| `S.H/I/J` | `/data/ui/msgtip.ui` save prompt stages | opcode `46` |

Opcode details:

| Opcode | Behavior | Status |
|---|---|---|
| `1` | Text/caption through `game.j D`, with effect manager id 9; wait text complete and key `1`. | VERIFIED |
| `4` | Main dialog via `S.a(speaker,text,mode)`; waits `S.c(mode,-1)` and key confirm; handles paging through `ae.b`. | VERIFIED |
| `35` | Choice/list UI. Opens one of `option/answer/wharf1`, returns selected index, then branch target comes from `ab[selected] - 2`. | VERIFIED |
| `40` | Task tip message `S.c(text)`, wait closed. | VERIFIED |
| `45` | Same as `40`, plus set static `game.c.t = param0`; trigger helpers can read `t`. | VERIFIED |
| `46` | Save confirm flow: open msgtip, confirm key -> `game.k.k()` save, event marked complete, show success, close. Cancel exits event step. | VERIFIED/PARTIAL |
| `48` | Positioned text box through `game.j D`; optional wait flag `b()[5]`. If text renderer `D.e()` says non-wait/finished, event can advance. | VERIFIED |
| `49` | Task option UI with two high-level columns and branch labels; selected option branches through `ab[selected] - 2`. Can append progress into static `game.c.s/u` when first option and first command param says so. | VERIFIED/PARTIAL |
| `51` | Non-blocking dialog setup: open dialog, then draw `game.j D` positioned text. No wait state. | PARTIAL |
| `84` | Dialog text with placeholder substitution from `game.c.a(template, values)`. Values come from `this.p` challenge progress or `x.I/R.length`. Then behaves like opcode `4`. | VERIFIED/PARTIAL |

## 5. World Transition Opcodes

`game.i` state confirmation:

- State `12`: after loading delay, current module becomes `game.d.a()` battle module.
- State `22`: after loading delay, current module becomes `game.k.a()` world module and calls `game.k.d()`.
- State `23`: similar world load state, but shows random loading text centered.

World/event transition opcodes:

| Opcode | Behavior | Status |
|---|---|---|
| `6` | Persist current event as state `3`; set `game.k.f/g` to target world/room; set `game.k.j` to target actor if flag param says so, else `-1`; call `game.i.a().a((byte)22)`. This is execute-transition. | VERIFIED |
| `21` | Prepare transition globals: `game.k.x=false`, `game.k.y=b()[2]`; if `b()[1]==1`, also set `z/A/B/C`. Does not switch state by itself. | VERIFIED/PARTIAL |
| `22` | Prepare alternate transition globals: `game.k.x=true`, `game.k.w`, spawn coords `h/i`, `B/C`, and clear `j=-1`. Does not switch state by itself. | VERIFIED/PARTIAL |
| `23` | Mark arbitrary event in target world/room state `3`; if same room, update live `p` and remove from active list. | VERIFIED |
| `76` | Persist current event state `3`; set target `f/g`, clear `j=-1`, call `game.k.a((byte)29)`. This changes world mode instead of direct `game.i` state `22`. | VERIFIED/PARTIAL |
| `77` | Mark arbitrary event state `4`; if same room, update live event state. | VERIFIED |

## 6. Battle Trigger Opcodes

Luang da verified:

```text
opcode 37/54 -> game.d.a().a(int[][] setup)
opcode 32    -> capture world screen + set game.d public fields + game.i state 12
game.i state 12 -> game.d.a().d() -> battle module
```

Details:

| Opcode | Behavior | Status |
|---|---|---|
| `32` | Calls `world.e()`, sets `game.d.a().a = b()[0]`, `game.d.a().b = (byte)b()[1]`, captures current world image into `game.d.a().c`, stops player, sets event state `1`, then `game.i` state `12`. | VERIFIED |
| `37` | Creates one battle setup tuple `{{b0,b1,b2}}` and stores into `game.d.s` via `a(int[][])`. | VERIFIED/PARTIAL |
| `54` | Creates `int[count][3]` from three comma-separated string lists and stores into `game.d.s`. | VERIFIED/PARTIAL |

`game.d` confirms:

```text
game.d.a(int[][]) -> this.s = setup
game.d.j()        -> this.s.length
game.d.k()        -> this.s[0][0]
```

What is still PARTIAL:

- Exact meaning of the 3 columns in battle setup rows.
- Exact battle formation/enemy mapping inside `game.d.d()` and later state machine.
- How `game.d.a/b/c` public fields affect battle intro effect/background.

## 7. Decoded Event Resource Scan

PowerShell scan of `event/decoded/*.json` over `event_groups[].records[].opcode` found these opcode counts:

```text
1:9, 2:102, 3:84, 4:1307, 5:128, 6:26, 7:94, 8:33, 9:173, 10:151,
11:70, 12:107, 13:116, 14:289, 15:120, 16:54, 17:75, 18:2, 19:7,
20:28, 21:5, 22:22, 23:33, 24:14, 25:2, 29:26, 30:8, 31:12,
32:108, 33:6, 34:2, 35:14, 36:5, 37:41, 38:1, 39:3, 40:143,
41:7, 42:123, 43:32, 44:29, 45:55, 46:9, 47:108, 48:12, 49:28,
51:1, 52:3, 53:7, 54:67, 56:54, 57:13, 58:13, 59:4, 60:14,
61:4, 65:1, 66:5, 67:59, 72:10, 76:1, 77:1, 78:2, 79:1,
80:3, 81:1, 82:3, 83:1, 84:2, 85:1, 86:9, 87:3, 88:1
```

Opcodes in `0..88` not observed in decoded event JSON:

```text
0, 26, 27, 28, 50, 55, 62, 63, 64, 68, 69, 70, 71, 73, 74, 75
```

Important: "not observed in decoded JSON" khong co nghia la dead code 100%. Co the decoder thieu scene/resource, hoac opcode duoc generated/runtime. Nhung voi rebuild phase dau, cac opcode co count cao nen uu tien truoc: `4`, `14`, `9`, `10`, `40`, `5`, `42`, `15`, `13`, `32`, `47`, `12`, `2`, `7`, `3`.

## 8. Chot Muc Do San Sang Port

San sang port logic VM:

- Event state machine `p/ad`: VERIFIED.
- Auto advance/wait/branch rule: VERIFIED.
- Trigger scan core: VERIFIED/PARTIAL, du cho port baseline.
- UI dialog/reward wait loops: VERIFIED.
- World transition `6/21/22/23/76/77`: VERIFIED/PARTIAL, du cho port intro/world flow.
- Battle entry `32/37/54`: VERIFIED ve luong kich hoat, PARTIAL ve noi dung battle setup.

Can audit tiep truoc khi port full game:

- `game.d.d()` battle init va battle turn state machine.
- Domain exact cua `game.g.N/L/K/J/M` theo save/load va shop/menu.
- Sample validation param layout cho opcode hiem: `38,62,65,80,83,85,88`.
- Decompile cross-check cho cac doan CFR co `GOTO`/type clash trong `game.c`.
