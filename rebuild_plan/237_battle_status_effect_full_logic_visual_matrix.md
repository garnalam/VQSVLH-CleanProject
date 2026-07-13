# 237 - Battle status/effect full logic visual matrix

Date: 2026-07-13

Scope: source-first audit for battle statuses before doing more per-skill work.

Goal: every status must be understood as gameplay logic plus visible feedback:

- which source table owns it;
- whether it applies to player pet, enemy pet, or both;
- whether it is a form/status, self-side buff, or target-side debuff;
- whether it has HUD icon/duration;
- whether it has body-attached `H/u/AH` effect;
- whether rebuild logic is already effective or still partial.

No runtime code was changed for this document.

## Source anchors

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
  - `v[16][5]`: self-side buff slots.
  - `w[11][5]`: target-side debuff slots.
  - `x[2][3]`: active visible status queue, bank `0` buffs and bank `1` debuffs.
  - `a(byte,int,int)`: apply buff.
  - `b(target)`: compute damage and apply debuff.
  - `o(id)`: per-turn buff tick.
  - `q(id)`: per-turn debuff tick.
  - `d(id,slot)` / `c(id,slot)`: duration decrement and clear.
  - `C()` / `D()`: clear all debuffs / buffs.
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
  - states `12/13`: active queue consumer.
  - state `7`: P7 damage/effect resolve.
  - visual gate `ai = {{3,5,13},{0,1,2,3,8,9,10}}`.
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
  - battle HUD status icon methods `a(b)` and `b(b)`.
- `modules/script/decoded/data__script__db.mid.json`
  - `aq.c[3]`: form/status/material rows.
  - `aq.c[6]`: self-side buff rows.
  - `aq.c[7]`: target-side debuff rows.
- `modules/script/decoded/data__script__bufDebuf.mid.json`
  - active queue visual rows for P12/P13.
- `modules/script/decoded/data__script__speffect.mid.json`
  - AH type rows for active/body effects.
- `modules/img/decoded/data__img__img_804.mid.png`
  - HUD status icon sheet through sprite `325`.

Related docs:

- `rebuild_plan/71_battle_skill_status_table_matrix.md`
- `rebuild_plan/72_battle_full_skill_status_behavior_classification.md`
- `rebuild_plan/77_battle_p12_p13_h_speffect_matrix.md`
- `rebuild_plan/78_battle_active_effect_lifecycle_full_matrix.md`
- `rebuild_plan/163_battle_status_icon_sprite325_img804_audit.md`
- `rebuild_plan/188_battle_phase10a_status_icon_effect_overlay_audit.md`
- `rebuild_plan/189_battle_phase10b_body_attached_effect_overlay_audit.md`
- `rebuild_plan/190_battle_phase10b_normal_p7_ah_closeout_coverage.md`

## Three different status systems

Do not mix these systems.

| System | Source table / field | Applies to | Has HUD icon? | Main purpose | Current status |
|---|---|---|---|---|---|
| Form/status | `aq.c[3]`, unit `c[5]`, checked by `f(id)` | the pet that owns the form/status | not through `x[2][3]` HUD queue | passive stat/catch/EXP hooks or material/key rows | PORTED/PARTIAL for ids with source consumers |
| Self-side buff | `aq.c[6]`, unit `v[id]`, queue `x[0]` | the pet that receives the buff; player or enemy both possible | yes, cell `buffId + 12` | positive or self-applied temporary effects | PORTED/PARTIAL |
| Target-side debuff | `aq.c[7]`, unit `w[id]`, queue `x[1]` | the pet that is hit by the debuff; player or enemy both possible | yes, cell `debuffId + 1` | harmful/abnormal temporary effects | PORTED/PARTIAL |

## HUD icon rules

Source `game.h.a(b)` / `game.h.b(b)` proves:

| Condition | Sprite/icon | Duration cell | Applies to |
|---|---:|---:|---|
| empty slot | sprite `325` cell `0` | UI cell `145` | both sides |
| active buff id `id` with `v[id][0] > 0` | sprite `325` cell `id + 12` | `134 + v[id][0]` | owner of buff |
| active debuff id `id` with `w[id][0] > 0` | sprite `325` cell `id + 1` | `134 + w[id][0]` | owner of debuff |

Visible order is source queue order:

```text
buff slot0, debuff slot0, buff slot1, debuff slot1, buff slot2, debuff slot2
```

Rebuild status:

- `Scene` has status icon/duration arrays for both sides.
- `SourceBattleRuntime.syncRenderState()` populates the arrays from
  `BattleUnit.activeEffectQueue`, `buffSlots`, and `debuffSlots`.
- `VqsvBattleRenderer.drawStatusSlots()` draws sprite `325` cells and duration
  cells.
- Dedicated smoke exists for enemy debuff, enemy buff, player debuff, and mixed
  order.

Status: `PORTED/PARTIAL`. Exact MIDP widget-mode and pixel parity are still not
claimed.

## Body/effect visual rules

There are two visual routes besides HUD icons:

| Route | Source | Meaning | Current status |
|---|---|---|---|
| Normal P7 skill visual | `effect.mid` -> `speffect.mid` / actor action `u` | effect while a skill resolves | PORTED/PARTIAL for normal P7 AH types `1/7/8/9/12` |
| P12/P13 active queue visual | `bufDebuf.mid` with gate `ai` | visual shown when buff/debuff queue ticks | PORTED/PARTIAL for source-reachable AH types `1/8/9/12` |

Source active-queue visual gate:

| Bank | Ids that get P12/P13 visual playback | Ids that tick/apply without active visual |
|---|---|---|
| buff bank `0` | `3,5,13` | `0,1,2,4,6,7,8,9,10,11,12,14` |
| debuff bank `1` | `0,1,2,3,8,9,10` | `4,5,6,7` |

Important: absence of P12/P13 body visual does not mean absence of logic or HUD
icon. Many effects are logic+icon only.

## Form/status table `aq.c[3]`

These are not active queue buffs. They are pet form/status/material rows.

| Id | Name | Source meaning | Affects player/enemy? | Rebuild status | Next proof needed |
|---:|---|---|---|---|---|
| 0 | Mạn Đà La Thạch | low HP attack boost: when HP <= 30%, attack boost 100% | owner pet, both sides if present | PORTED/PARTIAL | deterministic formula smoke with owner at low/high HP |
| 1 | Hồng Sắc Hải Loa | attack boost 10% | owner pet, both sides | PORTED/PARTIAL | formula smoke for player and enemy owner |
| 2 | Quy Xác Toái Phiến | defense interaction / target defense boost 15% | target/owner depending formula path | PORTED/PARTIAL | formula smoke proving target defense change |
| 3 | Ô Nha Uế | anti-debuff/resistance 20% | target pet being debuffed | PORTED/PARTIAL | forced debuff block/reduce smoke already exists by family; add broad status smoke later |
| 4 | Viễn Cổ Long Cốt | crit chance bonus 10% | attacker pet | PORTED/PARTIAL | forced/seeded crit chance smoke |
| 5 | Mật Phong Sào | EXP gain +20% | participant pet | PORTED/PARTIAL | EXP consumer smoke exists; keep regression |
| 6 | Ký Cư Giải Xác | reserve/off-party EXP share | reserve pet | PORTED/PARTIAL | reserve share smoke already exists or must be kept in EXP suite |
| 7 | Linh Trùng Thi Hài | source text says battle begins; direct battle consumer not proven | UNKNOWN/PENDING | PENDING | source search before coding |
| 8 | Hấp Huyết Đằng Mạn | post-attack self-heal chance/percent | attacker pet | PORTED/PARTIAL | q() status8 hit/miss smoke needed if not already dedicated |
| 9 | Cá Thờn Bơn | cannot miss / certain hit style source text | owner pet | PARTIAL/PENDING | dodge/miss formula smoke with status9 |
| 10 | Cảm Lãm Chi Diệp | HP floor: HP should not fall below 10 in source path | owner/target pet | PORTED/PARTIAL | lethal damage HP-floor smoke |
| 11 | Sủng vật lôi đạt | catch chance +20% | catcher/player-side context | PORTED/PARTIAL | catch chance status multiplier smoke exists; keep regression |
| 12 | Tinh Nguyên Thạch | evolution material | not a battle status | NON_BATTLE / q.M material | do not port in status system |
| 13 | Thiên Giới Tinh Thạch | advanced evolution material | not a battle status | NON_BATTLE / q.M material | do not port in status system |
| 14 | Thiên Địa Thần Thạch | rare advanced evolution material | not a battle status | NON_BATTLE / q.M material | do not port in status system |
| 15 | Hồn Tinh Thạch | mutation material | not a battle status | NON_BATTLE / q.M material | do not port in status system |
| 16 | Quỷ Thần Tinh Thạch | rare mutation material | not a battle status | NON_BATTLE / q.M material | do not port in status system |
| 17 | Chìa khóa | golden chest key | not a battle status | NON_BATTLE / q.M key | do not port in status system |

## Self-side buff table `aq.c[6]`

Buffs are active queue bank `0`, usually beneficial or self-applied. They can
belong to player or enemy units.

| Id | Name | Apply/tick logic | Formula/turn hook | HUD icon | Body visual | Current status |
|---:|---|---|---|---:|---|---|
| 0 | Súc Lực | raise defense; store extra damage value | damage adds stored value when duration/counter condition hits | `12` | no P12/P13 visual by source gate | PORTED/PARTIAL |
| 1 | Phá Phủ | lower own defense; store damage boost percent | outgoing damage boost | `13` | no P12/P13 visual | PORTED |
| 2 | Kinh Cức | raise defense | stat reassert on tick | `14` | no P12/P13 visual | PORTED |
| 3 | Khôi phục | heal by max HP percent on apply and tick | HP delta text | `15` | yes: `bufDebuf` -> actor action + AH type9 | PORTED |
| 4 | Phòng ngự | skill-derived defense up | stat reassert | `16` | no P12/P13 visual | PORTED/PARTIAL |
| 5 | Vô hình | reflect/store damage chance | post-damage `K[5]` style hook | `17` | yes: actor action only | PORTED/PARTIAL |
| 6 | Kiên nhẫn | chance/value damage reduction/conversion | source oddity: target has buff6 but formula reads attacker values | `18` | no P12/P13 visual | PORTED/PARTIAL |
| 7 | Linh Xảo | skill-derived speed up | stat reassert | `19` | no P12/P13 visual | PORTED |
| 8 | Điện áp | extra PP cost and damage boost | PP/damage hook | `20` | no P12/P13 visual | PORTED/PARTIAL |
| 9 | Hỏa Thạch | speed up and defense down | stat reassert and turn behavior hooks | `21` | no P12/P13 visual | PORTED/PARTIAL |
| 10 | Man Lực | attack up | stat reassert | `22` | no P12/P13 visual | PORTED |
| 11 | Thâu Thủ | copy buffs from selected/donor unit and clear donor | selected-index/multi-unit | `23` | no P12/P13 visual | PARTIAL |
| 12 | Gia Tốc | set `K[12]`, later `K[12]=2`; repeated attack/PP conservation | follow-up/turn state | `24` | no P12/P13 visual | PORTED/PARTIAL |
| 13 | Thạch Hóa | cleanse debuffs and heal 20% max HP per tick | HP delta + debuff clear | `25` | yes: AH type1 | PORTED |
| 14 | Thạch Phu | cleanse debuffs and block abnormal statuses | debuff immunity | `26` | no P12/P13 visual | PORTED/PARTIAL |

Source has a `case 15` in decompiled buff apply, but `aq.c[6]` only has rows
`0..14`. Do not invent buff id 15 unless another source path proves it.

## Target-side debuff table `aq.c[7]`

Debuffs are active queue bank `1`, applied to the target that was hit. They can
belong to enemy or player units depending who was targeted.

| Id | Name | Apply/tick logic | Formula/turn hook | HUD icon | Body visual | Current status |
|---:|---|---|---|---:|---|---|
| 0 | Gieo Hạt | stores raw damage; per-turn damage `w[0][1] / skillParam` | skills `3/9` do more damage if active | `1` | yes: AH type9 | PORTED |
| 1 | Mê Muội | flag/duration; tick no-op | skills `23/29` do more damage if active | `2` | yes: AH type12 | PORTED |
| 2 | Quấn Quanh | flag/duration; tick no-op | source blocks item/switch/run in battle command paths | `3` | yes: actor action then AH type8 | PORTED/PARTIAL |
| 3 | Thực Loại | delayed damage when duration threshold hits | death clears/transition path | `4` | yes: actor action only | PORTED |
| 4 | Mục | hit-rate/down marker from skills `31/37` | miss chance consumer | `5` | no P12/P13 visual | PORTED/PARTIAL |
| 5 | Chậm Chạp | lowers speed immediately and reasserts | affects crit/miss/order via speed | `6` | no P12/P13 visual | PORTED |
| 6 | Nhụt Chí | stores damage reduction percent | reduces incoming damage | `7` | no P12/P13 visual | PORTED/PARTIAL |
| 7 | Phòng Ngự | lowers defense immediately and reasserts | affects damage taken | `8` | no P12/P13 visual | PORTED |
| 8 | Quỷ Mị | source row exists; zero-power skills `54` table points here but P7 guard says NOT_REACHED for debuff apply | no confirmed gameplay consumer from skill 54 | `9` if active | yes if active queue somehow present | FLAG/PENDING |
| 9 | Hỗn Loạn | source row exists; zero-power skill `55` table points here but P7 guard says NOT_REACHED for debuff apply | no confirmed gameplay consumer from skill 55; text says cannot switch pet | `10` if active | yes if active queue somehow present | FLAG/PENDING |
| 10 | Tê Liệt | applied by skills `41/47/68`; affects catch/status logic and likely action wait | catch/status multiplier and action delay-ish behavior | `11` | yes: AH type9 + AH type8 | PORTED/PARTIAL |

## Rebuild status summary

| Area | Status | Evidence |
|---|---|---|
| Buff/debuff storage `v/w/x/N` | PORTED/PARTIAL | `BattleUnit` has slots, queues, counts |
| Buff/debuff apply and tick | PORTED/PARTIAL | docs 76/78, unit formula checks, Phase 9 family smoke |
| HUD icons and duration cells | PORTED/PARTIAL | docs 163/188, Phase 10-A smokes |
| P12/P13 active queue visuals | PORTED/PARTIAL | doc 77, reachable AH type smokes |
| Normal P7 AH visuals | PORTED/PARTIAL | docs 189/190, AH types `1/7/8/9/12` smokes |
| Form/status `aq.c[3]` battle hooks | PORTED/PARTIAL | core formula/catch/EXP hooks exist, but ids `7/9/10` need stronger dedicated status smoke |
| Pixel-perfect MIDP status/effect parity | PENDING | exact Java ME drawRGB/color/alpha not claimed |
| Generic `ah.java` interpreter | PENDING | targeted renderers only |

## Done criteria before per-skill polish

A status/effect family should be considered "done enough for skill work" only
when all source-called pieces below are proven:

1. **Apply**
   - skill/item/source method writes the correct `v` or `w` slot;
   - active queue `x[bank]` receives the id if source does so.
2. **Logic**
   - stat/HP/damage/catch/EXP/turn effect actually changes behavior;
   - player-side and enemy-side owner cases are not accidentally swapped.
3. **HUD**
   - sprite `325` icon cell and duration cell match source formula.
4. **Visual**
   - if source active queue gate calls `bufDebuf` visual, P12/P13 shows it;
   - if only normal P7 calls `effect.mid`, do not invent P12/P13 visual.
5. **Expiry/Clear**
   - duration decrements;
   - stat reset/reapply is correct when one effect expires while another remains.
6. **Smoke**
   - PNG checkpoint exists for at least one player-owned and one enemy-owned
     case for the family when practical.

## Recommended next code/audit slices

Do not go back to per-skill animation yet. Close status effectiveness first.

### Slice A - Status Effectiveness Smoke Matrix

Create:

```text
238_battle_status_effectiveness_smoke_matrix.md
```

No code first. Define deterministic checkpoints for:

- buff3 heal tick + icon + P12 visual;
- buff10 attack up affects next damage + icon;
- buff14 blocks a debuff + icon;
- debuff0 damage tick + icon + P12 visual;
- debuff5 speed down affects miss/crit/order-relevant stat + icon;
- debuff7 defense down affects next damage + icon;
- debuff10 catch/action/status multiplier + icon + P12 visual;
- form/status9 no-miss or prove exact source route if not currently wired.

### Slice B - Implement only failed/missing status checkpoints

After Slice A, run smoke and fix only the statuses that fail. Do not alter
skills or generic P7 visuals unless the failing checkpoint proves that status
visual/logic needs it.

### Slice C - Closeout

Create closeout after smoke:

```text
239_battle_status_effectiveness_closeout.md
```

Only then return to per-skill polish.

## Verification status

Audit-only document. No build, check, or smoke was required because no runtime
code changed.
