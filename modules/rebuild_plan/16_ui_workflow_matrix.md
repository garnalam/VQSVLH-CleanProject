# 16. UI Workflow Matrix

Pham vi chinh:

- `source_code/decoded/decompiled_source_cfr/game/h.java`
- `ui/decoded/*.ui.json`
- `ui/original/*.ui`

Muc tieu: lap bang workflow theo dang:

```text
.ui file -> widget id quan trong -> input mask -> side effect / state change
```

Trang thai: PARTIAL but actionable.

Ly do chua danh dau full `VERIFIED`: `game.h` rat lon, co nhieu workflow long nhau, mot so doan CFR decompiled kho doc. Matrix nay du de dung khung UI rebuild va port theo thu tu, nhung tung nhanh edge case van can test lai bang ban goc khi implement.

## 1. Input Mask Legend

Day la mask quan sat trong `game.h`, khong nen gan cung voi ten phim vat ly neu chua doi chieu MIDP key map.

| Mask | Ten dung trong rebuild | Vai tro quan sat | Status |
| --- | --- | --- | --- |
| `4100` | `NAV_UP` | Di len trong menu/list. | VERIFIED |
| `8448` | `NAV_DOWN` | Di xuong trong menu/list. | VERIFIED |
| `16400` | `NAV_LEFT` | Doi tab/trang/cot trai. | VERIFIED |
| `32832` | `NAV_RIGHT` | Doi tab/trang/cot phai. | VERIFIED |
| `196640` | `ACTION` | Xac nhan/chon/vao. | VERIFIED |
| `262144` | `BACK` | Thoat/quay lai/huy. | VERIFIED |
| `131072` | `SOFT_ACTION_A` | Nut action phu, thuong thay trong help/bodyShop/SMS. | PARTIAL |
| `131104` | `SOFT_ACTION_A_PLUS` | Bien the action phu trong pet/confirm flow. | PARTIAL |
| `786432` | `SOFT_BACK_B` | Back/huy phu, thuong dong popup/confirm. | PARTIAL |
| `983072` | `SOFT_ACTION_B` | Action dac biet trong task. | PARTIAL |
| `10` | `KEY_10_OR_ENTER` | Xuat hien trong task flow; can doi chieu key map. | PARTIAL |
| `65568` | `SOFT_ACTION_C` | Xuat hien trong bodyShop flow. | PARTIAL |

Port rule: o layer Java moi nen dat ten theo role workflow truoc (`ACTION`, `BACK`, `NAV_*`), sau do moi map sang MIDP key thuc.

## 2. Layout Inventory

| UI file | Role | Widget id quan trong | Status |
| --- | --- | --- | --- |
| `world.ui` | HUD world. | `1`, `5`, `6`, `7` duoc `game.h` update; `6` la text world tip/zone. | VERIFIED/PARTIAL |
| `dialog.ui` | Hop thoai event/cutscene. | `12`, `13` button text; `14` dialog text/paged text. | VERIFIED/PARTIAL |
| `option.ui` | Lua chon event. | Dung qua mang `N[0]`; id chi tiet can map tiep. | PARTIAL |
| `answer.ui` | Tra loi/event answer. | Dung qua `N[1]`; id chi tiet can map tiep. | PARTIAL |
| `openbox.ui` | Popup reward/message. | `1` animation/icon, `2` text. | VERIFIED |
| `taskTip.ui` | Popup task tip. | `1` animation/icon, `2` text. | VERIFIED |
| `gamemenu.ui` | Menu trong game. | `5..11` entries; `12` confirm; `15` shop label; selected container/root state. | VERIFIED/PARTIAL |
| `gamesystem.ui` | System menu. | `6..11` entries. | VERIFIED/PARTIAL |
| `help.ui` | Help/about simple page. | Label/page widgets; back by input. | PARTIAL |
| `help1.ui` | Help paged. | Many page text ids; left/right page by input. | PARTIAL |
| `shop.ui` | Shop main/recover/pet bank. | `5` title/currency; `6` buy/deposit; `7` sell/withdraw; `8` leave; `9` recover; `10` confirm; `11` back. | VERIFIED/PARTIAL |
| `shopbuy.ui` | Buy/sell/egg shop list. | `5` action; `9` category; `10` price; rows `14+5*i`/`15+5*i`; icons `45+i`, `51+i`; `56` desc; `43/44` money/medal; `38` scroll; `39/40/57/58` buttons. | VERIFIED/PARTIAL |
| `msgyn.ui` | Yes/no + quantity confirm. | `7` no; `8` quantity; `10` category/count label; other ids for text/buttons. | VERIFIED/PARTIAL |
| `msgwarm.ui` | Warning/error popup. | `6` prompt text observed; common close/action popup. | VERIFIED/PARTIAL |
| `msgtip.ui` | Tip popup. | `0..4`; closes by action/back. | PARTIAL |
| `msgconfirm.ui` | Confirm/cancel popup. | `2` confirm text; `3` back text. | VERIFIED/PARTIAL |
| `msgRecover.ui` | Pet recover confirm. | Confirm/back around pet recover. | PARTIAL |
| `bag.ui` | Bag/inventory. | Normal rows `18+5*i`,`19+5*i`,`20+5*i`; equip rows `59+5*i`; special rows `98+5*i`; task/temp rows `137+5*i`; desc ids `46`,`85`,`124`,`163`; scroll ids `43`,`84`,`123`; progress `164/165`. | VERIFIED/PARTIAL |
| `petstate.ui` | Pet status/detail. | HP/MP bars `16/17 + 6*i`; sprite `48`; name `51`; type `52`; equip `59..61`; action `64/75`; stats `65..68`; stars `70..74`. | VERIFIED/PARTIAL |
| `petsetting.ui` | Pet action menu. | `5` item; `6` battle; `7` equip; `8` release; `9` evolve; `10` skill. | VERIFIED/PARTIAL |
| `choice.ui` | Generic item/equip/ball choice. | `5/59` action; `6/60` back; `8` header; `9` column label; rows `13+5*i`, `14+5*i`; icons `54+i`; desc `53`; scroll `51`. | VERIFIED/PARTIAL |
| `choiceskill.ui` | Skill choice. | `5` action; `6` back; `8` header; `9` PP/count label; rows `13+5*i`,`14+5*i`; desc `53`; scroll `51`. | VERIFIED/PARTIAL |
| `skill.ui` | Pet skill detail/list. | `0..22`; used from petsetting. | PARTIAL |
| `evolve.ui` | Pet evolution. | `10` sprite; `38` name; `40` level; `45` material; `46` material count; old/new stats `19..22`, `31..34`. | VERIFIED/PARTIAL |
| `battle.ui` | Battle HUD/commands. | `3` fight, `4` catch, `5` item, `6` pet, `7` points, `8` run; selection overlay around `20+selected`; HP/name/level/status ids around `9..19`, `38..42`, `55..59`. | VERIFIED/PARTIAL |
| `levelUp.ui` | Level up/new skill flow. | `0..51`; skill learning branches. | PARTIAL |
| `npcEnemy.ui` | Enemy info/list. | `0..36`; setup enemy data. | PARTIAL |
| `task.ui` | Task/quest list. | `8` main tab; `9` sub tab; row ids `12/13/14 + 5*i`; desc `36`; progress `37/38`; scroll `40`; confirm/back `41/42`. | VERIFIED/PARTIAL |
| `taskOption.ui` | Event task/reward choice. | Reward icons/text `13/14`, `15/16`, ...; options `17+`; text `21`. | VERIFIED/PARTIAL |
| `record.ui` | Player record. | `14` total pets; `17` capture count; `20` rare; `26` badge; `29` achieved badges; `31` time. | VERIFIED/PARTIAL |
| `petmap.ui` | Pet map/record detail. | ids from layout need mapping tiep. | PARTIAL |
| `badge.ui` | Badge record/detail. | icons `25+i`; detail ids `13`, `14`, `16`, `33`. | VERIFIED/PARTIAL |
| `smsTip.ui` | SMS/payment prompt. | `6` icon; `7/8` title/detail. | PARTIAL |
| `smsInfo.ui` | SMS info/payment detail. | `5/8` labels; `10/11` text in `bw()`. | PARTIAL |
| `transmit.ui` | Fast transmit/teleport. | entries `5..9`; scroll `13`; buttons `14/15`; list/root id `0`. | VERIFIED/PARTIAL |
| `wharf1.ui` | Wharf destination list variant 1. | destination text `5..`; title/id `8`; action/back `9/10`. | VERIFIED/PARTIAL |
| `wharf2.ui` | Wharf destination/menu variant 2. | destination text `5..`; title `10`; back `11`; action `12`. | VERIFIED/PARTIAL |
| `ride.ui` | Mount/ride choice. | mount texts `8..11`. | PARTIAL |
| `bodyShop.ui` | Body/point shop. | `0..16`; body shop point purchase flow. | PARTIAL |
| `shopsale.ui` | Sale layout. | Present in resources, but `game.h` mainly uses `shopbuy.ui` for sale flow. | PARTIAL |

## 3. Workflow Matrix: World, Dialog, Event Popup

| UI | Open/setup methods | Input methods | Input -> side effect | Status |
| --- | --- | --- | --- | --- |
| `world.ui` | `c()`, `d()`, `aS()`, `e()` | world update loop in `e()` | No direct list input here; UI is updated from world state. `((game.k)o).k` writes world/status text, ids around `1/5/6/7` hidden/shown by `ab.a(...)`. | VERIFIED/PARTIAL |
| `openbox.ui` | `au()`, `av()`, `a(String,int)`, `b(String)` | `f()`, `aw()`, `ax()` | `ACTION(196640)` closes popup after animation/timer; side effect is only UI close unless caller queued reward state before opening. | VERIFIED |
| `taskTip.ui` | `c(String)` | `g()`, `br()`, `ay()` | `ACTION(196640)` closes task tip. Used by event/task updates. | VERIFIED |
| `dialog.ui` | `aB()`, `a(String,String,int)`, `b(int)` | `aC()`, `c(int,int)` | `ACTION(196640)` advances text/page; close when `ae.d` paging done. Side effects include `game.k.t/s` dialog actor flags and event VM resume. | VERIFIED/PARTIAL |
| `option.ui` | `a(int,int,String[],String)` via `N[0]` | `c(int)` | `NAV_UP/DOWN` changes selected option; `ACTION` writes selected option/result to event flow; `BACK` can close depending mode. | PARTIAL |
| `answer.ui` | `a(int,int,String[],String)` via `N[1]` | `c(int)` | Similar to `option.ui`; exact widget ids and scoring/answer side effect need branch-level audit. | PARTIAL |
| `taskOption.ui` | `a(int[],int[],String[],String[])` | `aD()` | `NAV_UP/DOWN` changes choice; `ACTION` selects reward/option and resumes event VM; `BACK` closes/cancels if allowed. | VERIFIED/PARTIAL |

## 4. Workflow Matrix: Game Menu, System, Help

| UI | Open/setup methods | Input methods | Input -> side effect | Status |
| --- | --- | --- | --- | --- |
| `gamemenu.ui` | `k()` | `l()` | `NAV_UP/DOWN` changes `b` selection. `ACTION` routes selected entry to owner states: pet/status, bag, task, record, system/save/shop-like states observed around bytes `7/8/9/10/14/22`. `BACK` closes to world state. | VERIFIED/PARTIAL |
| `gamesystem.ui` | `m()` | `n()` | `NAV_UP/DOWN` changes selection. `ACTION` routes to continue/help/options/main menu/save related branches. `BACK` returns to `gamemenu.ui`. | VERIFIED/PARTIAL |
| `help1.ui` | `o()`, `u()` | `p()`, `v()` | `NAV_LEFT/RIGHT` changes help page; `BACK` closes to caller (`gamesystem.ui` or previous help flow). | PARTIAL |
| `help.ui` | `q()`, `s()`, `w()` | `r()`, `t()`, `x()` | `NAV_LEFT/RIGHT` changes subpage where available. `BACK` or soft action closes. | PARTIAL |
| `msgtip.ui` | `aV()`, `a(String)`-style helpers | `aW()`/caller checks | `ACTION/BACK` closes informational tip; some callers return to menu or shop after close. | PARTIAL |

## 5. Workflow Matrix: Shop, Buy, Sale, Recover

| UI | Open/setup methods | Input methods | Input -> side effect | Status |
| --- | --- | --- | --- | --- |
| `shop.ui` | `C()` normal shop, `A()` pet bank variant | `D()`, `B()` | `NAV_UP/DOWN` changes selected row. `ACTION` routes to buy/sell/recover/leave or deposit/withdraw variants. `BACK` exits to owner state/world. | VERIFIED/PARTIAL |
| `shopbuy.ui` | `a(int,byte)`, `b(int,byte)`, `a(byte,byte)` | `b(byte,byte)` | `NAV_UP/DOWN` moves row, `NAV_LEFT/RIGHT` changes category/page. `ACTION` opens `msgyn.ui` quantity confirm or buys direct. Data source is `aq.c[group]`, player money/medal from `game.g`. | VERIFIED/PARTIAL |
| `msgyn.ui` | opened from shopbuy/sale | shopbuy/sale confirm branch | `NAV_LEFT/RIGHT/UP/DOWN` can change quantity/category depending branch. `ACTION` confirms purchase/sale; `BACK/SOFT_BACK_B` cancels and returns to shopbuy. Side effects: currency changes, inventory vector update, warning popup on insufficient money/bag full. | VERIFIED/PARTIAL |
| sale via `shopbuy.ui` | `L()`, `aX()` | `M()` | Uses sale vector `q.S` and inventory operations `q.d`, `q.s` style methods. `ACTION` opens quantity confirm; confirm removes/sells item. | PARTIAL |
| `msgRecover.ui` | from `shop.ui` recover branch | `D()` recover branch | Confirm recovers/heals pets if enough currency; back/cancel returns to shop. | PARTIAL |
| `msgwarm.ui` | common warning helper `a(String)`/shop branches | caller waits/close | Warns insufficient money, full bag, invalid action. Close returns to previous UI. | VERIFIED/PARTIAL |

## 6. Workflow Matrix: Pet, Skill, Evolve

| UI | Open/setup methods | Input methods | Input -> side effect | Status |
| --- | --- | --- | --- | --- |
| `petstate.ui` | `W()`, `e(int)`, `a(b[],int)`, `f(int)` | `X()` and petstate branches | `NAV_UP/DOWN` changes pet; `ACTION` opens `petsetting.ui` or performs context action; `BACK` returns to previous UI/world. Updates pet sprite/name/type/stats/equipment. | VERIFIED/PARTIAL |
| `petsetting.ui` | opened in `X()` | `X()` | `NAV_UP/DOWN` selects action ids `5..10`. `ACTION` routes: item use, set battle pet, equip, release confirm, evolve, skill UI. `BACK` returns to petstate. | VERIFIED/PARTIAL |
| `choice.ui` pet equip/item | `bd()`, `be()` style fill methods | choice branches inside `X()` | `NAV_UP/DOWN` moves list. `ACTION` equips/uses item on pet or detaches equipment. Side effects touch pet equipment, inventory vectors, warnings. | PARTIAL |
| `skill.ui` | `bf()` and petsetting skill branch | petsetting/skill branch | Shows pet skill detail; `ACTION/BACK` returns. Exact widget id write map still needs full pass. | PARTIAL |
| `evolve.ui` | `bg()`, `bh()` | evolve branch | `ACTION` attempts evolution: checks level/material from `aq.c[0]/[3]`, consumes item, updates pet form/stats, may show `msgwarm.ui`. `BACK` returns to petsetting/petstate. | VERIFIED/PARTIAL |
| `levelUp.ui` | `an()`, `ao()`, `ap()` | `aq()`, `as()` | Shows stat gains/new skill choices. `ACTION` advances/learns skill; may open `choiceskill.ui` if skill list full. | PARTIAL |

## 7. Workflow Matrix: Bag / Inventory

| UI | Open/setup methods | Input methods | Input -> side effect | Status |
| --- | --- | --- | --- | --- |
| `bag.ui` normal item tab | `Y()`, `bi()` | `ac()`/bag branch | `NAV_UP/DOWN` moves row; `NAV_LEFT/RIGHT` changes tab/category. `ACTION` uses selected item if valid, often opens pet selection or warning. Data mostly from `q.K/q.J`. | VERIFIED/PARTIAL |
| `bag.ui` equipment tab | `bj()` | bag branch | Shows equipment list from `q.L`; `ACTION` can open petstate/pet choice for equip. | PARTIAL |
| `bag.ui` special stack tab | `bk()` | bag branch | Shows special/ball/consumable stacks from `q.M`; `ACTION` performs item-specific action or warning. | PARTIAL |
| `bag.ui` task/temp/egg tab | `bl()` | bag branch | Shows task/temp/egg items from `q.N`; `ACTION` can hatch/use/inspect. Some branches update avoid-monster timer `q.x` or close to petstate. | PARTIAL |
| `petstate.ui` from bag | `Z()`, `aa()`, `ab()` | pet selection branches | Used when item/equipment needs target pet. `ACTION` applies item/equip to pet; `BACK` returns to bag. | PARTIAL |
| `msgconfirm.ui` from bag | bag confirm branch | bag confirm branch | Confirms dangerous actions such as discard/release-like flow. | PARTIAL |

## 8. Workflow Matrix: Task, Record, Badge

| UI | Open/setup methods | Input methods | Input -> side effect | Status |
| --- | --- | --- | --- | --- |
| `task.ui` | `R()`, `ba()`, `bb()` | `S()` | `NAV_UP/DOWN` moves task row; `NAV_LEFT/RIGHT` changes task tab/category. `ACTION` or special mask opens/handles task detail; `BACK` returns to `gamemenu.ui`. | VERIFIED/PARTIAL |
| `record.ui` | `N()` | `O()` | `NAV_LEFT/RIGHT/ACTION` routes among record/pet map/badge; `BACK` returns to game menu. | VERIFIED/PARTIAL |
| `petmap.ui` | `P()` | `Q()` | Grid/page navigation over pet map entries; `ACTION/BACK` returns to record flow. | PARTIAL |
| `badge.ui` | `T()`, `bc()` | `U()` | `NAV_UP/DOWN/LEFT/RIGHT` changes badge selection; action/back returns to record. | VERIFIED/PARTIAL |

## 9. Workflow Matrix: Battle UI

| UI | Open/setup methods | Input methods | Input -> side effect | Status |
| --- | --- | --- | --- | --- |
| `battle.ui` | `a(b,b)` | `d(b)` | `NAV_LEFT/RIGHT` changes command selection. `ACTION` routes command: fight -> skill select/state, catch -> ball choice/state, item -> item choice/state, pet -> pet switch/state, points/info -> state, run -> run branch. Updates battle owner `game.d`/`game.i` states. | VERIFIED/PARTIAL |
| `battle.ui` HUD update | `b(b,b)`, `a(b)`, `b(b)`, HP/status helpers | battle tick/update | Writes HP bars, name, level, status icons, type advantage percent. No direct input. | VERIFIED/PARTIAL |
| `choiceskill.ui` | `e(b)`, `h(b)` | `f(b)` | `NAV_UP/DOWN` selects skill. `ACTION` chooses skill, checks PP/target, calls battle engine (`game.d.b(skill)` style), moves battle state to execute/target branch. `BACK` returns to battle commands. | VERIFIED/PARTIAL |
| `choice.ui` capture ball | `ah()` | `ai()` | Selects capture ball/item stack. `ACTION` consumes/uses ball if valid; warning if none/invalid. `BACK` returns battle command. | PARTIAL |
| `choice.ui` battle item | `aj()`, `ak()` | `al()`, `bo()` | Selects battle usable item; `ACTION` applies item to battle pet or opens pet target, then returns battle state. | PARTIAL |
| `msgwarm.ui` battle warning | battle item/skill/catch branches | caller branch | Shows no PP, cannot catch, no item, invalid target, etc. Close returns to previous battle UI. | VERIFIED/PARTIAL |

## 10. Workflow Matrix: SMS / Payment

| UI | Open/setup methods | Input methods | Input -> side effect | Status |
| --- | --- | --- | --- | --- |
| `smsTip.ui` | `a(int)`, `V()`, `aK()` | `V()`, `aL()`, `aM()` | Shows payment/SMS tip. `ACTION` continues into SMS info/payment script; `BACK/SOFT_BACK_B` closes. Side effects touch SMS counters/flags, but exact reward flags are not fully audited. | PARTIAL |
| `smsInfo.ui` | `bw()` | `bx()` | Displays SMS detail text and confirm/cancel labels. `ACTION` likely starts `q.java`/SMS canvas branch; `BACK` closes. | PARTIAL |
| `q.java` + `package a/*` | external SMS canvas | `q.keyPressed(int)` | Loads `/data/event/scene_13.mib` into Lua-like VM and stubs message send. Exact game reward/save side effects remain separate audit. | PARTIAL |

## 11. Workflow Matrix: Wharf, Transmit, Ride, Body Shop, Enemy Info

| UI | Open/setup methods | Input methods | Input -> side effect | Status |
| --- | --- | --- | --- | --- |
| `transmit.ui` | `h()`, `aT()` | `i()` | `NAV_UP/DOWN` selects destination; `ACTION` sets world transition fields in `game.k` (`f/g/h/i/w` observed) and asks owner/state manager to transition; `BACK` closes. | VERIFIED/PARTIAL |
| `wharf1.ui` / `wharf2.ui` | `a(byte,int,int)` | `aN()` | `NAV_UP/DOWN` selects destination. `ACTION` checks open flag like `game.k.M.b`, sets wharf transition globals and owner state around `29`; `BACK` closes. | VERIFIED/PARTIAL |
| `wharf2.ui` convenience menu | `aQ()` | `aR()` | Options route to shop/bag/service states around `31/7/32/0`; exact label-to-state mapping needs validation. | PARTIAL |
| `shopbuy.ui` egg/wharf shop | `aO()` | `aP()` | Uses shopbuy layout for egg purchase, observed fixed money check around `5000`; `ACTION` buys if enough money, otherwise warning. | PARTIAL |
| `ride.ui` | `ad()`, `bm()` | `ae()` | Selects ride/mount. `NAV_LEFT/RIGHT` changes mount; `ACTION` applies/uses ride; `BACK` closes. Exact inventory/flag side effect needs audit. | PARTIAL |
| `bodyShop.ui` | `az()`, `bs()` | `aA()` | Point/body shop. `NAV_UP/DOWN` selects row; soft/action confirms purchase/use; warning on invalid/insufficient point. Exact stat/currency target needs audit. | PARTIAL |
| `npcEnemy.ui` | `at()`, `b(int,int)`, `e(String)` | mostly display | Shows enemy/NPC encounter info. Input side effect not fully traced. | PARTIAL |

## 12. Rebuild Port Notes

Implementation order nen theo thu tu nay:

1. Port `ab/ao/af/al/ac/k/m/y/z` de open/close/render/focus duoc UI.
2. Tao enum path cho 40 `.ui` layout va central widget-id constants cho cac id da VERIFIED/PARTIAL o tren.
3. Port `game.h` theo workflow nho: world popup/dialog -> menu/system -> shopbuy/msgyn/msgwarm -> petstate/bag -> task/record -> battle.
4. Moi workflow port xong can viet test/smoke: open UI, feed input mask, assert selected index/state/UI stack/player data.
5. Nhung hang `PARTIAL` trong file nay khong duoc hardcode theo phan doan; khi den module do phai quay lai audit branch trong `game.h`.

## 13. Remaining Audit For UI

- Map chi tiet id cua `option.ui`, `answer.ui`, `help.ui`, `help1.ui`, `petmap.ui`, `skill.ui`, `levelUp.ui`, `npcEnemy.ui`, `ride.ui`, `bodyShop.ui`.
- Tach `game.h` thanh workflow state machine moi voi ten ro nghia thay cho fields `a/b/c/f/h/i`.
- Doi chieu input mask voi MIDP key code vat ly.
- Runtime-test UI stack behavior: popup tren `shopbuy`, `petstate`, `battle` co quay lai dung UI cu khong.
- Validate dynamic binding trong `z`/`ac`: list scroll, highlight, hidden/visible state.

Ket luan: file nay la ban do workflow UI dau tien du de bat dau port co kiem soat. No chua thay the viec audit branch cuoi khi implement tung man hinh, nhung da khoanh dung cac UI, widget ids, input masks va side effects chinh.
