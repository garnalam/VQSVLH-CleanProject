# Battle Current Status And Next Plan

Date: 2026-07-06

Scope: audit trung thực phần battle hiện tại trước khi port tiếp. Battle phải đi theo mạch gốc:

```text
event opcode -> game.d battle state -> game.b unit/stat/skill/status
-> game.h UI call -> ui/sprite/effect asset -> result branch/save/world return
```

Không được dựng UI/animation độc lập nếu chưa chứng minh source state gọi tới nó.

## Source Facts Đã Xác Minh

### Battle entry

- `game.c` event opcodes `37/32/47/52/67` là cầu nối từ event sang battle.
- Ba battle đang dùng trong rebuild:
  - Sophie/kidnapping: story branch về record 78.
  - Bunny tutorial: encounter `[34,5,1]`, `op32 [0,0]`, `op47 [12,0,0]`, result success hiện giữ `-1`.
  - Elder battle: actor 52, encounter `[68,5,1]`, `op32 [0,2]`, `op47 [10,10,0]`.
- Các entry này hiện là source-backed ở mức record/event, nhưng chưa phải runner bytecode-equivalent của `game.c + game.d`.

### Battle asset load

Trong `modules/source_code/decoded/decompiled_source_cfr/game/d.java`, khi battle init (`d()`), bản gốc load:

- `/data/script/pos.mid`
- `/data/script/cpos.mid`
- `/data/script/effect.mid`
- `/data/script/speffect.mid`
- `/data/script/blood.mid`
- `/data/script/bufDebuf.mid`
- `/data/tex/blood_0..2`
- battle actor/effect sprites qua `f`, `ah`, sprite id từ `aq.c[0][species][17]`.

Kết luận: animation battle không thể làm đúng chỉ bằng vẽ sprite đứng yên. Phải port các bảng script này sau khi state/target/skill đã đúng.

### Battle UI source calls

Trong `game.h`:

- Main HUD: mở `/data/ui/battle.ui`.
- Skill list: `e(b) / h(b) / f(b)` mở `/data/ui/choiceskill.ui`, fill widget rows `13 + i*5`, PP row `14 + i*5`, mô tả skill id `53`.
- Catch: `ah() / ai()` mở `/data/ui/choice.ui`, title `Pokemon ball`, subtitle `Tỉ lệ bắt`, action `Sử dụng`; icon từ `aq.c[4][ball][1]`; chance từ `game.d.b(ball)`.
- Item: `aj() / ak() / bo()` mở `/data/ui/choice.ui` và warning `/data/ui/msgwarm.ui`; behavior item đọc `aq.c[4][item][5]`.
- Level-up/learn skill: `an/ao/ap/aq` mở `/data/ui/levelUp.ui` và `/data/ui/choiceskill.ui`.

Kết luận: UI phải được gọi theo state/method trên, không port rời rạc theo file `.ui`.

### Battle unit/source data

Trong `game.b`:

- `c[23]`: base stats.
- `d[23]`: current stats.
- `z[5]`: skill ids.
- `y[5]`: PP/current skill value.
- `v[16][5]`: buff slots.
- `w[11][5]`: debuff slots.
- `x[2][3]`, `N[2]`: active effect queues.
- `P()`: serialized caught pet payload.

Trong `db.mid`:

- `aq.c[0]`: species/stat/sprite/learn/relation.
- `aq.c[1]`: skill name/desc/power/PP/effect/target.
- `aq.c[4]`: item/ball icon/behavior/params.
- `aq.c[6]`: buff.
- `aq.c[7]`: debuff.
- `aq.c[8]`: learn thresholds.

## Đã Làm Được

### PORTED

- `VqsvBattleTables.java` đọc `db.mid` và wrap các group chính: species, skill, status, item, buff, debuff.
- `VqsvBattleUnit.java` dựng lại cấu trúc gần `game.b`: `c[]/d[]`, `z[]/y[]`, `v/w`, `x/N`.
- Damage direct path đã port source-shaped từ `game.b.b(target)` và có regression `VqsvBattleDamageFormulaCheck`.
- Battle runtime không còn overlay stub trắng trợn; đã có state enum theo source label: `P0/P20/P3/P21/P17/P4/P16/P5/P11/P10/P2/P7/P1/P8/P9`.
- Command bar battle có 6 lệnh, một con trỏ, key/click điều hướng được.
- P21/P17 catch flow là phần chắc nhất hiện tại:
  - list ball từ bag item có `aq.c[4][id][5] == 0`;
  - icon/name/chance từ source table;
  - confirm consume ball;
  - catch chance theo shape `game.d.b(itemId)`;
  - sprite capture `269`, image row `309`;
  - q0..q4 phase và type-8 effect q1/q4 đã port source-shaped;
  - storage bag/bank/full theo shape `game.g.y()`.
- Ba route story battle smoke hiện giữ nhánh:
  - Sophie -> branch 78;
  - Bunny -> task quay về trưởng thôn;
  - Elder -> reward/state/free-world.

### PARTIAL

- Main battle HUD đang là renderer source-shaped, chưa phải generic `game.h` widget runtime.
- HP/name/level/status slots có dựng theo tọa độ decoded `battle.ui`, nhưng chưa pixel-perfect và chưa đầy đủ disabled/status icon parity.
- P4 item, P5 pet switch, P10 run, P11 shop có shell logic và UI tối thiểu, nhưng chưa full source behavior.
- P17 capture effect đã sát hơn, nhưng chưa byte-for-byte MIDP `drawRGB`/`l/e/ah` pipeline.
- Catch payload có shape `game.b.P()`, nhưng save/global inventory parity của `game.g` chưa xong.
- Turn order/AI hiện source-shaped đơn giản, chưa đủ `game.d` queue/priority/status behavior.

### STUB / PENDING

- P3 skill list thật: chưa mở/render `choiceskill.ui`, chưa có scroll, PP row, desc id 53, no-PP warning chuẩn.
- Target select/P6/P2 thật: chưa dùng `aq.c[1][skill][9]` để build target side/mask/cursor.
- P7 skill animation thật: chưa chạy `pos/cpos/effect/speffect/blood/bufDebuf`.
- Buff/debuff turn tick P12/P13: chưa port runtime queue + visual.
- EXP/win flow P8/P22/P23: chưa có EXP distribution, `levelUp.ui`, learn skill flow.
- Lose/revive P9/P24: mới basic branch, chưa đủ revive/world return variants.
- Full item behavior table: chưa port toàn bộ `game.b.x(item)`/`w(item)` và warning cases.
- Full pet switch restrictions: dead/status/active pet/special source flags chưa đầy đủ.
- Full shop/battle-point flow: hiện chỉ shell, chưa `shopbuy.ui` quantity/category parity.
- Pixel/timing compare với bản gốc battle: chưa có capture video/frame-by-frame.

## Rủi Ro Nếu Làm Sai Thứ Tự

- Nếu port UI trước P3/P6 thật, skill list nhìn có vẻ đúng nhưng confirm sẽ sai branch/state.
- Nếu port animation trước target/skill thật, effect sẽ gắn sai actor hoặc sai side.
- Nếu port EXP trước full win participant list, level-up/save pet sẽ lệch.
- Nếu sửa global inventory/save trước khi chốt battle item/catch, rất dễ làm hỏng route Bunny và pet selection cũ.

## Kế Hoạch Làm Tiếp

### Step 1: Battle Entry Snapshot Matrix

Tạo snapshot cho Sophie/Bunny/Elder:

- event opcodes và args;
- battle mode/flags/branch;
- player unit source pet payload;
- enemy species/level/nature/stat/sprite;
- UI đầu tiên source mở;
- smoke command UI từng battle.

Output đề xuất: `54_battle_entry_snapshot_matrix.md`.

Mục tiêu: trước khi sửa runtime, biết chắc mỗi trận đang vào với dữ liệu gì.

### Step 2: Port P3 Skill List Thật

Source cần bám:

- `game.d case 3`;
- `game.h.e(b)`, `game.h.h(b)`, `game.h.f(b)`;
- `/data/ui/choiceskill.ui`;
- `aq.c[1]` skill name/desc/PP/target side.

Yêu cầu:

- list skill từ active unit `z[]/y[]`;
- PP hiển thị đúng `current/max`;
- desc widget id `53`;
- back về P20;
- no PP mở `msgwarm.ui`;
- confirm chưa resolve ngay nếu source cần target select.

Smoke:

- `battle_bunny_p3_skill_list.png`
- `battle_elder_p3_skill_list.png`
- `battle_skill_no_pp_warning.png`

### Step 3: Port Target Select / P2 Bridge

Source cần bám:

- `aq.c[1][skill][9]`;
- target vectors trong `game.d/game.h`;
- battle cursor/target indicator từ `battle.ui`.

Yêu cầu:

- skill target enemy/self/team phải đúng;
- target cursor không đoán bằng tọa độ tay;
- AI enemy có thể PARTIAL nhưng player path phải chắc.

### Step 4: Port P7 One-Skill Animation Slice

Chỉ chọn một skill direct-damage đơn giản trước.

Source/assets cần đọc:

- `pos.mid`
- `cpos.mid`
- `effect.mid`
- `speffect.mid`
- `blood.mid`
- `bufDebuf.mid`
- `game.d.n()/o()/P7`
- `f`, `ah`, root renderer helpers.

Yêu cầu:

- attacker/target position đúng;
- effect gắn đúng actor;
- damage number/blood effect xuất hiện đúng mạch;
- HP bar update sau damage, không che số HP/EXP.

### Step 5: Status/Buff/Debuff Tick

Port:

- `game.b.a(byte,int,int)` buff apply;
- `game.b.q/c/d` debuff/buff tick;
- active queues `x/N`;
- `game.d` P12/P13 visual queues;
- status icons/widgets.

### Step 6: Full Item/Pet/Run/Shop

Sau khi skill/target/resolve ổn mới mở rộng:

- item behavior table đầy đủ;
- P16 target validation;
- pet switch restrictions;
- run chance/world return;
- shopbuy UI/quantity/money.

### Step 7: EXP / Level-Up / Learn Skill

Port:

- P8 win/EXP participant list;
- P22 `levelUp.ui`;
- P23 learn skill `choiceskill.ui`;
- write-back pet payload/save.

## Smoke/Check Bắt Buộc Cho Mỗi Slice Battle

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" com.vqsv.rebuild.Main --check
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvBattleDamageFormulaCheck
```

Route regression bắt buộc:

```powershell
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke-checkpoint route_sophie_after_battle_branch ".\build_intro_demo\route_sophie_after_battle.png"
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke-checkpoint route_bunny_after_battle_task ".\build_intro_demo\route_bunny_after_battle.png"
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state ".\build_intro_demo\route_elder_after_battle.png"
```

UI smoke tùy slice phải xuất PNG, không mở client nếu user không yêu cầu.

## Current Truth

Battle hiện tại đã có nền source-backed: table, unit model, damage formula, command shell và catch flow. Nhưng chưa thể gọi là hoàn thiện hoặc giống bản gốc 100%.

Phần nên làm tiếp ngay là Step 1 snapshot, sau đó Step 2 P3 skill list. Không nên nhảy vào animation tổng hoặc EXP ngay, vì chúng phụ thuộc vào skill/target/state chính xác.
