# 14. Source Code Remaining Audit Checklist

Pham vi: `C:/Users/Dell/Downloads/ResourcesVQSV/modules/source_code/decoded/decompiled_source_cfr`

Muc tieu file nay: liet ke thuc te nhung gi trong `source_code` van can audit tiep truoc khi rebuild full game. Khong danh dau `VERIFIED` neu moi suy luan theo ten bien/decompile.

## 1. Ket luan nhanh

Chua the noi la da audit het `source_code`.

Nhung phan da du lam mong rebuild skeleton:

- Runtime/Core: lifecycle MIDlet, Canvas, input, timer, state manager.
- Resource/Renderer core: loader, cache, sprite/map/font pipeline o muc API va luong goi.
- World/Event architecture: room, actor, camera, event VM, opcode matrix `0..88`.
- UI architecture: `ab/ao/af`, `.ui` parser/runtime, `game.h` o muc workflow lon.
- Gameplay/Battle/Save: player/inventory/pet/save/battle o muc khung van hanh.

Nhung phan con can audit sau de rebuild full va gan pixel/gameplay voi ban goc:

- Renderer primitive da audit sau; con validation pixel/draw order khi implement.
- Battle engine state machine, cac state phu `18/19/24/101/102/104`, skill/effect table semantics, va damage formula da dong trong [17_battle_state_machine.md](17_battle_state_machine.md). Con lai la runtime regression/pixel timing khi implement.
- UI workflow chi tiet theo widget id da co ban do dau tien trong [16_ui_workflow_matrix.md](16_ui_workflow_matrix.md); van PARTIAL cho edge cases trong `game.h`.
- Game world tick `game.k.b()` va actor behavior da co pass chi tiet trong [19_world_tick_actor_matrix.md](19_world_tick_actor_matrix.md). Con lai la UI delegate side effects, tutorial/evolution tail domain names, `ai` mode `2` neu variant khac dung, va pixel validation cho `ah`.
- Text/cutscene renderer `game.j` da co matrix rieng trong [20_text_cutscene_renderer_matrix.md](20_text_cutscene_renderer_matrix.md). Mode 0 du de port intro/cutscene theo opcode `1/48/51`; counter mode 0 da duoc giai thich la phu thuoc leading color tag va VERIFIED voi data goc. Mode 3 da dong la broken/dead branch trong JAR hien co, khong can port. Con PARTIAL o MIDP prompt font va pixel/timing validation.
- SMS/payment side effects game-side da co matrix trong [21_sms_payment_side_effect_matrix.md](21_sms_payment_side_effect_matrix.md): reward/save da dong; provider/VM/carrier behavior van PARTIAL/EXTERNAL.

## 2. Can audit bat buoc truoc khi rebuild full

| Cum | File/class | Trang thai | Ly do can audit tiep |
| --- | --- | --- | --- |
| Renderer primitive | `d.java`, `aa.java`, `am.java`, `j.java`, `l.java`, `s.java`, `ae.java`, `aq.java` | VERIFIED/PARTIAL | Da audit sau trong [15_renderer_primitive_deep_audit.md](15_renderer_primitive_deep_audit.md). Du de port; con can pixel-validation cho MIDP transform, map `copyArea`, va texture packed PNG. |
| Effect draw | `b.java` | VERIFIED/PARTIAL | Opcode 9 va manager da nam, nhung `b.a(Graphics)` bi CFR damaged. Can runtime/screenshot validation neu can timing/pixel chuan. |
| UI primitives | `ao.java`, `af.java`, `ac.java`, `al.java`, `k.java`, `m.java`, `z.java`, `y.java`, `w.java`, `ak.java` | PARTIAL | Parser va widget base da ro, nhung draw/focus/dynamic binding cua mot so control chua dong het. `ao.a(Graphics)` bi CFR damaged. |
| UI workflow | `game.h` | PARTIAL | Da lap matrix UI file -> widget id -> input mask -> side effect trong [16_ui_workflow_matrix.md](16_ui_workflow_matrix.md). Con can branch-level validation khi port tung man hinh, nhat la bag/pet/battle/SMS/bodyShop/ride. |
| Battle engine | `game.d` | VERIFIED/PARTIAL | Logic state machine da dong trong [17_battle_state_machine.md](17_battle_state_machine.md), bao gom state phu `18/19/24/101/102/104`. Con PARTIAL chi la animation script timing va end-to-end UI switch-pet validation. |
| Battle unit/damage | `game.b` | VERIFIED | Unit model va damage formula da doi chieu bytecode trong [17_battle_state_machine.md](17_battle_state_machine.md). Runtime regression van nen co de bat loi port, khong phai vi formula con mo. |
| Battle/player data | `game.g`, `aq.c[0..6]` | VERIFIED/PARTIAL | Inventory/pet/currency/save da ro o muc khung, nhung can dat ten domain chuan cho cac vector `J/K/L/M/N/O` va cac table `aq.c`. |
| World tick | `game.k.b()` | VERIFIED/PARTIAL | Pass chi tiet tai [19_world_tick_actor_matrix.md](19_world_tick_actor_matrix.md): state `P`, free-roam order, random encounter gate, battle entry, transition dispatch da ro. Con PARTIAL o tutorial/evolution tail domain names va UI delegate side effects. |
| Actor behavior | `game.a`, `f.java`, `n.java`, `ai.java`, `ah.java` | VERIFIED/PARTIAL | Actor `t/v` behavior va usage count tu decoded event data da audit trong [19_world_tick_actor_matrix.md](19_world_tick_actor_matrix.md). Con PARTIAL o pixel validation `ah` va `ai` mode `2` semantics neu co variant/source khac set mode nay. |
| Text/cutscene renderer | `game.j` | VERIFIED/PARTIAL | Da audit trong [20_text_cutscene_renderer_matrix.md](20_text_cutscene_renderer_matrix.md). Mode 0 dung cho decoded event data (`opcode 1/48/51`, `short_args[0]=10`) da du de port intro: typewriter, wrap bitmap font, prompt, wait/non-wait flow. Counter mode 0 khong con xem la damaged cho data goc: 22/22 text mode 0 bat dau bang color tag `#`, dung voi cach bytecode tang visible end. Mode 3 da CLOSED AS DEAD/BROKEN trong JAR hien co: `x` chi set null, khong co caller/data dung. Con PARTIAL o MIDP prompt font va pixel/timing validation. |

## 3. Can audit neu muon full game dung hanh vi goc

| Cum | File/class | Trang thai | Ghi chu |
| --- | --- | --- | --- |
| SMS/payment | `an.java`, `q.java`, `u.java`, `r.java`, `v.java`, `ag`, `package a/*`, `scene_13.mib` | VERIFIED/PARTIAL | Da audit trong [21_sms_payment_side_effect_matrix.md](21_sms_payment_side_effect_matrix.md). Game-side reward/save da dong: `an` ap product `0..4`, `PK6_RMS_SMS` luu activation `an.X`, money/badge/item/pet di qua save gameplay. `q` Canvas/VM ro nhung khong thay caller `new q(...)`; provider/carrier va `l2.bin/cpid.txt` van PARTIAL/EXTERNAL. |
| Lua-like VM | `a/a.java`, `a/g.java`, `a/h.java`, `a/i.java`, `a/c.java`, `a/d.java`, `a/f.java`, `a/j.java` | VERIFIED/PARTIAL | Kien truc VM da ro, nhung native/builtin table va ham `a.h.b(Object,int)` bi damaged. Chu yeu phuc vu SMS/payment script. |
| Save slot edge cases | `game.k`, `ar.java` | VERIFIED/PARTIAL | Slot 0/1/2/3/4/6/7/8/9 da ro hon. Slot 5 `PK6_RMS_CNTSMS` chi thay trong slot list/constant pool, khong thay `af[5]` read/write trong source/bytecode search; mark UNUSED/UNVERIFIED. |
| Title/aux states | `game.e`, `game.f`, `game.i`, `an.java` | PARTIAL | Da nam Runtime/Core, nhung cac mode phu/title/loading/battle transition can map tiep vao rebuild state enum. |

## 4. Thap hon hoac da gan nhu stub

| File/class | Trang thai | Ghi chu |
| --- | --- | --- |
| `c.java`, `i.java`, `x.java`, `g.java`, `as.java`, `o.java`, `h.java`, `r.java` | LOW/PARTIAL | Nho, interface/stub/helper. Audit khi mot module chinh goi den. |
| `ad.java`, `p.java` | VERIFIED/PARTIAL | Event record/command holder da ro o muc VM. Can quay lai neu opcode param naming can chinh xac hon. |
| `aj.java`, `ap.java`, `t.java` | PARTIAL | Helper/resource/input-ish classes. Chua phai nut that, nhung can doc khi port renderer/UI. |

## 5. CFR damaged methods can uu tien doi chieu

Theo `summary.txt`, cac method sau decompiler CFR khong structure duoc:

| Class | Method | Muc do rui ro |
| --- | --- | --- |
| `game.d` | `b()` | Cao: battle main tick/state. |
| `game.k` | `b()` | Cao: world main tick/state. |
| `game.c` | `n()` | Cao: event opcode executor, da audit nhung van can doi chieu neu port full. |
| `game.c` | `b()` | Cao: event trigger scan/state, da audit mot phan. |
| `game.b` | `a(game.b)`, `b(game.b)` | CLOSED in [17_battle_state_machine.md](17_battle_state_machine.md): CFR source damaged, but bytecode audit closed relation helper and damage/effect formula. |
| `game.h` | Khong bi liet ke damaged trong summary, nhung file rat lon | Trung/cao: workflow chi tiet can map bang tay. |
| `game.j` | `a(Graphics)`, `a(Graphics,int,int)` | Trung/cao: dialog/cutscene text render. |
| `ao` | `a(Graphics)` | Trung/cao: UI render. |
| `b` | `a(Graphics)` | Trung: effect draw/timing. |
| `q` | `keyPressed(int)` | Trung: SMS/payment UI side effect. |
| `a.h` | `b(Object,int)` | Trung: VM table/index behavior. |

## 6. Audit order de tiep tuc

Thu tu nen lam tiep:

1. `game.d` + `game.b`: state machine, state `18/19/24/101/102/104`, skill/effect table semantics, buff/debuff runtime, relation class `[22]`, va damage formula da dong trong [17_battle_state_machine.md](17_battle_state_machine.md); buoc tiep la implement + regression/pixel validation.
2. `game.h`: matrix workflow da co trong [16_ui_workflow_matrix.md](16_ui_workflow_matrix.md); buoc tiep la branch-level audit khi implement tung UI.
3. `game.j`: da co matrix mode 0 va audit mode 3 trong [20_text_cutscene_renderer_matrix.md](20_text_cutscene_renderer_matrix.md); buoc tiep la implement + screenshot/timing validation. Chi quay lai mode 3 neu thay data/event/JAR khac dung mode nay.
4. SMS/payment: game-side side effects da co trong [21_sms_payment_side_effect_matrix.md](21_sms_payment_side_effect_matrix.md). Chi quay lai `q/u/v/package a/*` neu muon port optional legacy payment Canvas/provider config that.
5. Khi implement world actor: viet regression tests cho `n`, follower trail, collision probe, actor groups `0/4..0/14`, `0/17`, `0/18`, va transition `1/0..1/4`.

Renderer primitive `d/aa/am/j/s/l/ae/aq` da co audit sau rieng. Buoc tiep cua cum nay khong phai doc source nua ma la implement + render validation.

## 7. Trang thai rebuild hien tai

Du de tiep tuc dung khung project va port module theo thu tu.

Chua du de cam ket JAR full game pixel-perfect/gameplay-perfect. Nhung danh sach con thieu da khoanh lai ro: khong phai con mo toan bo source nua, ma tap trung vao cac cum damaged/lon o tren.
