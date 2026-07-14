# 303 Battle All Skill Source Logic / Animation Audit

Status: AUDIT_ONLY / NO_CODE_PATCH.

This is the current all-skill control audit for `aq.c[1][0..69]`. It supersedes
the planning role of `236_battle_skill_full_logic_animation_matrix.md` and
`301_battle_skill_grouped_logic_animation_roadmap.md`, while keeping them as
historical source matrices.

Purpose:

- list every battle skill in the game;
- explain what the skill does in plain language;
- map the source table logic (`aq.c[1]`) to runtime behavior;
- map the P7 animation/effect path (`effect.mid` / `speffect.mid`);
- state what is currently ported, partial, intentionally changed, or pending.

No runtime code is changed by this document.

## Source Anchors

| Source | What it proves |
| --- | --- |
| `modules/script/decoded/data__script__db.mid.json` | `groups[1]` is `aq.c[1]`, 70 skill rows. |
| `modules/script/decoded/data__script__chs.mid.json` | Skill name/description text ids from `aq.c[1][skill][1..2]`. |
| `modules/script/decoded/data__script__effect.mid.json` | P7 effect rows, split into 7-value chunks. |
| `modules/script/decoded/data__script__speffect.mid.json` | AH special rows referenced by effect chunks with `chunk[1] == 1`. |
| `modules/source_code/decoded/decompiled_source_cfr/game/b.java` | Damage formula, buff/debuff apply, active tick/clear, PP/stat hooks. |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java` | P7 animation/resolve, `game.d.q()` post-skill behavior, P12/P13 active effect flow. |
| `rebuild_plan/236_battle_skill_full_logic_animation_matrix.md` | Historical full raw skill catalog. |
| `rebuild_plan/301_battle_skill_grouped_logic_animation_roadmap.md` | Current family grouping and next visual-roadmap shape. |
| `rebuild_plan/300_battle_debuff_table_0_10_closeout.md` | Debuff table 0..10 closeout status. |
| `rebuild_plan/280_battle_buff_table_0_14_closeout.md` | Buff table 0..14 closeout status. |

## Row Schema

`aq.c[1][skill]`:

```text
[element, nameText, descText, power, learnTier, pp, effectMode, effectId, param, targetMode]
```

Important rules:

- `power > 0`: P7 can apply normal damage after animation gates finish.
- `power == 0`: no fake damage; visual/effect route may still apply buff/special logic.
- `effectMode == 0`: usually direct damage or special post-hit `game.d.q()` case.
- `effectMode == 1`: self-side buff / beneficial state family, but several source-switch rows still do raw damage first.
- `effectMode == 2`: target-side debuff/status family or special zero-power route.
- `effectId == -1`: no ordinary buff/debuff id from the table.
- `param`: chance, percent, divisor, follow-up rate, or family-specific value.

## Animation Rule

Each `effect.mid[skill]` is read as 7-value chunks:

```text
[ownerSide, specialFlag, effectIdOrSpeffectId, stateOrParam, nextFrameTrigger, stateFrameTrigger, stateToSet]
```

- `specialFlag == 0`: source creates actor action `u`.
- `specialFlag == 1`: source creates AH special object `H`.
- Actor effect ids `20..34` map to sprite ids:

```text
20->262, 21->263, 22->264, 23->265, 24->266, 25->267, 26->268,
27->299, 28->300, 29->301, 30->304, 31->306, 32->307, 33->308, 34->309
```

Current rebuild follows the source-shaped P7 chunk flow, but exact
frame-by-frame MIDP cursor/pixel parity is still `PENDING` unless a dedicated
slice says otherwise.

## Status Legend

| Status | Meaning |
| --- | --- |
| `PORTED` | Logic is implemented and has focused smoke/closeout coverage. |
| `PORTED/PARTIAL` | Core logic/route is implemented, but exact animation, RNG, multi-target, passive, or pixel parity remains open. |
| `INTENTIONAL_DEVIATION / GAMEPLAY_FIXED` | User-approved gameplay differs from odd/unclear source behavior. Must stay documented. |
| `NOT_REACHED` | Source path proves the table field is not consumed from the current runtime branch. |
| `PENDING_AUDIT` | Needs a dedicated source/visual audit before code changes. |

## Full Skill Audit Table

Columns:

- `Skill`: source skill id.
- `Source role`: player-facing source-backed meaning.
- `Logic`: source runtime behavior.
- `Animation`: P7 actor/special path summary.
- `Status / next`: honest current state and next work.

### Fire Lane - Skills 0..9

| Skill | Name | Source role | Logic | Animation | Status / next |
| ---: | --- | --- | --- | --- | --- |
| 0 | Hỏa trảo | Low-damage fire basic. | Direct simple: power `100`, PP `45`, no status. | Actor `20->262`, state `0`. | `PORTED/PARTIAL`; direct-base visual slice pending. |
| 1 | Dương viêm | Low damage plus burn/seed-like HP drain for 3 turns. | Direct plus debuff0; stores raw damage, tick divisor `4`. | Actor `20->262`. | `PORTED`; shares fire direct animation polish. |
| 2 | Diễm kích | Low damage, 10% chance Mê Muội. | Explicit debuff1 chance `10`; buff14 can block. | Actor `20->262`, then `speffect14/AH12`. | `PORTED/PARTIAL`; RNG/pixel parity pending. |
| 3 | Hỏa Vân triệu | Low damage; stronger if target already has debuff0. | Conditional damage by debuff0, param `120`. | Actor `20->262`. | `PORTED/PARTIAL`; conditional UI/visual parity pending. |
| 4 | Thiên Hỏa tế | No damage; raises defense and later damage edge. | Buff0 Sức Lực: defense `+30%`, duration-edge extra damage. | `speffect16/AH9`, then `speffect15/AH1`. | `PORTED`; pixel-perfect producer visual pending. |
| 5 | Viêm lôi phá | No damage; more outgoing damage, less defense. | Buff1 Phá Phủ: defense `-50%`, damage `+50%`. | `speffect16/AH9`, then `speffect15/AH1`. | `PORTED`; pixel-perfect producer visual pending. |
| 6 | Hỏa diễm đao | Higher damage fire direct. | Direct simple: power `150`, PP `30`. | Actor `20->262`. | `PORTED/PARTIAL`; direct-base visual slice pending. |
| 7 | Chước nhiệt chi xúc | Damage plus stronger burn/HP drain setup. | Direct plus debuff0; tick divisor `3`. | Actor `20->262`. | `PORTED`; shares fire direct animation polish. |
| 8 | Liệt diễm phong bạo | High damage, 20% chance Mê Muội. | Explicit debuff1 chance `20`. | Actor `20->262`, then `speffect14/AH12`. | `PORTED/PARTIAL`; RNG/pixel parity pending. |
| 9 | Vĩnh hằng hỏa ảnh | High damage; stronger if target already has debuff0. | Conditional damage by debuff0, param `250`. | Actor `20->262`. | `PORTED/PARTIAL`; conditional visual parity pending. |

### Wood Lane - Skills 10..19

| Skill | Name | Source role | Logic | Animation | Status / next |
| ---: | --- | --- | --- | --- | --- |
| 10 | Diệp Toàn | Low-damage wood basic. | Direct simple: power `100`, PP `45`. | Actor `21->263`, state `1`. | `PORTED/PARTIAL`; first recommended direct-base polish slice. |
| 11 | Quang phản | Low damage and heals attacker. | Direct damage, then `game.d.q()` heal param `10`. | Actor `21->263`, plus `speffect10/AH9` heal. | `PORTED/PARTIAL`; heal text/effect placement pending. |
| 12 | Đằng Phược | Low damage and Quấn Quanh bind. | Direct debuff2; switch/item/run lock behavior is closed. | Actor `21->263`, plus `speffect6/AH8`. | `PORTED`; P7 visual timing still partial. |
| 13 | Thảo Chủng | Low damage, delayed Thực Loại damage. | Direct debuff3; final tick `150%` stored raw damage. | Actor `21->263`. | `PORTED`; body visual/tick logic closed, P7 polish pending. |
| 14 | Đằng chi bích lũy | No damage; defense up and reflects damage. | Buff2 Kinh Cức: defense `+30%`, reflect `10%`. | Actor `21->263`, state `1`. | `PORTED`; producer visual uses actor route, exact cursor pending. |
| 15 | Thảo nguyện thuật | No damage; heal over time. | Buff3 Khôi phục: `5% maxHP` apply/tick heal. | Actor `33->308`, frame trigger, then `speffect7/AH9`. | `PORTED/PARTIAL`; complex trigger timing pending. |
| 16 | Châm Diệp Trảm | Higher damage wood direct. | Direct simple: power `150`, PP `30`. | Actor `21->263`, state `1`. | `PORTED/PARTIAL`; direct-base visual slice pending. |
| 17 | Diệp chi ân huệ | Medium damage and stronger heal. | Direct damage, then `game.d.q()` heal param `40`. | Actor `21->263`, plus `speffect10/AH9`. | `PORTED/PARTIAL`; heal placement pending. |
| 18 | Đằng mạn triền nhiễu | High damage and Quấn Quanh bind. | Direct debuff2. | Actor `21->263`. | `PORTED`; P7 visual timing pending. |
| 19 | Quang hợp hiệu ứng | High damage, delayed Thực Loại damage. | Direct debuff3; final tick `200%` stored raw damage. | Actor `21->263`. | `PORTED`; P7 visual timing pending. |

### Earth Lane - Skills 20..29

| Skill | Name | Source role | Logic | Animation | Status / next |
| ---: | --- | --- | --- | --- | --- |
| 20 | Hất bụi | Low-damage earth basic. | Direct simple: power `100`, PP `45`. | Actor `22->264`, state `0`. | `PORTED/PARTIAL`; direct-base visual slice pending. |
| 21 | Thổ thuẫn | Damage and self defense up. | Raw damage then `game.d.q()` buff4, defense `+10%`. | Actor `22->264`, plus `speffect5/AH9`. | `PORTED`; q() timing/pixel parity pending. |
| 22 | Bão cát | Low damage, 25% chance Mê Muội. | Explicit debuff1 chance `25`. | Actor `22->264`. | `PORTED/PARTIAL`; RNG/pixel parity pending. |
| 23 | Nham băng | Damage boosted if target is Mê Muội. | Conditional damage by debuff1, param `250`. | Actor `22->264`, plus `speffect6/AH8`. | `PORTED/PARTIAL`; visual parity pending. |
| 24 | Người bảo vệ Địa Giới | No damage; cleanse and heal over time. | Buff13 Thạch Hóa: clear debuffs, heal `20% maxHP`. | Actor `22->264`, plus `speffect17/AH1`. | `PORTED`; pixel-perfect visual pending. |
| 25 | Thạch phu thuật | No damage; cleanse and debuff immunity. | Buff14 Thạch Phu: clear debuffs, block debuff family. | `speffect4/AH7`, then `speffect17/AH1`. | `PORTED`; pixel-perfect visual pending. |
| 26 | Nham bạo | Medium earth damage. | Direct simple: power `150`, PP `30`. | Actor `22->264`, plus `speffect6/AH8`. | `PORTED/PARTIAL`; direct/special timing pending. |
| 27 | Hàng rào cát đá | Damage and self defense up. | Raw damage then `game.d.q()` buff4, defense `+10%`. | Actor `22->264`, plus `speffect7/AH9`. | `PORTED`; q() timing/pixel parity pending. |
| 28 | Bão cát | Medium damage, 25% chance Mê Muội. | Explicit debuff1 chance `25`. | Actor `22->264`. | `PORTED/PARTIAL`; RNG/pixel parity pending. |
| 29 | Thổ Chi Loạn Vũ | High damage boosted if target is Mê Muội. | Conditional damage by debuff1, param `300`. | Actor `22->264`. | `PORTED/PARTIAL`; visual parity pending. |

### Water Lane - Skills 30..39

| Skill | Name | Source role | Logic | Animation | Status / next |
| ---: | --- | --- | --- | --- | --- |
| 30 | Bong bóng | Low-damage water basic. | Direct simple: power `100`, PP `45`. | Actor `23->265`, state `0`. | `PORTED/PARTIAL`; direct-base visual slice pending. |
| 31 | Băng lao | Low damage and accuracy/miss penalty. | Direct debuff4, stored value `1`. | Actor `23->265`. | `PORTED`; P7 visual timing pending. |
| 32 | Tuyết ảnh | Low damage and speed down. | Direct debuff5, speed down `10%`. | Actor `23->265`, plus `speffect1/AH9`. | `PORTED`; P7 visual timing pending. |
| 33 | Thủy trụ | Low damage and outgoing damage down. | Direct debuff6, damage output down `10%`. | Actor `23->265`. | `PORTED`; P7 visual timing pending. |
| 34 | Thuật cầu nguyện | No damage; chance reflect. | Buff5 Vô hình: reflect chance `30%`. | `speffect4/AH7`. | `PORTED`; dedicated visual closeout exists, pixel-perfect pending. |
| 35 | Thủy bích | No damage; chance halve incoming damage. | Buff6 Kiên nhẫn: `GAMEPLAY_FIXED`, 50% proc, 50% reduction. | `speffect4/AH7`, then `speffect17/AH1`. | `INTENTIONAL_DEVIATION / GAMEPLAY_FIXED`; closed by user-approved logic. |
| 36 | Bạo Phong Tuyết | Higher damage water direct. | Direct simple: power `150`, PP `30`. | Actor `23->265`. | `PORTED/PARTIAL`; direct-base visual slice pending. |
| 37 | Lá chắn gió tuyết | Damage and stronger accuracy/miss penalty. | Direct debuff4, stored value `2`. | Actor `23->265`, `speffect7/AH9`, `speffect6/AH8`. | `PORTED`; complex chunk timing pending. |
| 38 | Băng Phong Hãm Tĩnh | Medium/high damage and speed down. | Direct debuff5, speed down `10%`. | Actor `23->265`, plus `speffect7/AH9`. | `PORTED`; P7 visual timing pending. |
| 39 | Ray lạnh | Medium/high damage and outgoing damage down. | Direct debuff6, damage output down `10%`. | Actor `23->265`. | `PORTED`; P7 visual timing pending. |

### Electric Lane - Skills 40..49

| Skill | Name | Source role | Logic | Animation | Status / next |
| ---: | --- | --- | --- | --- | --- |
| 40 | Điện giật | Low-damage electric basic. | Direct simple: power `100`, PP `45`. | Actor `24->266`, state `0`. | `PORTED/PARTIAL`; direct-base visual slice pending. |
| 41 | Lôi thiểm | Low damage, chance Tê Liệt. | Explicit debuff10 chance `10`; catch multiplier covered; source action-delay not found. | Actor `24->266`, plus `speffect4/AH7`. | `PORTED/PARTIAL`; action-delay remains `NOT_FOUND_IN_PC_SOURCE`. |
| 42 | Nạp điện | Damage and self speed up. | Raw damage then `game.d.q()` buff7, speed `+5%`. | Actor `24->266`, plus `speffect1/AH9`. | `PORTED`; q() timing/pixel parity pending. |
| 43 | Sóng điện từ | Damage and clears target buffs. | Direct buff-clear route: target beneficial states cleared. | Actor `24->266`, plus `speffect4/AH7`. | `PORTED/PARTIAL`; clear visual proof pending. |
| 44 | Đoạt mệnh cao áp | No damage; PP cost up and damage up. | Buff8 Điện áp: outgoing damage `+30%`, extra PP cost. | `speffect19/AH9`, then `speffect15/AH1`. | `PORTED`; pixel-perfect producer visual pending. |
| 45 | Điện năng chuyển đổi | No damage; speed up, defense down. | Buff9 Hóa Thạch: speed `+50%`, defense `-50%`. | `speffect19/AH9`, then `speffect15/AH1`. | `PORTED`; pixel-perfect producer visual pending. |
| 46 | Tia lửa điện | Higher damage electric direct. | Direct simple: power `150`, PP `30`. | Actor `24->266`. | `PORTED/PARTIAL`; direct-base visual slice pending. |
| 47 | Chùm sấm sét | Medium damage, chance Tê Liệt. | Explicit debuff10 chance `10`. | Actor `24->266`, plus `speffect4/AH7`. | `PORTED/PARTIAL`; action-delay remains source-unproven. |
| 48 | Điện quang thạch hỏa | Medium damage and self speed up. | Raw damage then `game.d.q()` buff7, speed `+5%`. | Actor `24->266`, plus `speffect9/AH9`. | `PORTED`; q() timing/pixel parity pending. |
| 49 | Cảm ứng điện từ | High damage and clears target buffs. | Direct buff-clear route. | Actor `24->266`. | `PORTED/PARTIAL`; clear visual proof pending. |

### Ghost / Shadow Lane - Skills 50..59

| Skill | Name | Source role | Logic | Animation | Status / next |
| ---: | --- | --- | --- | --- | --- |
| 50 | Ảnh thứ | Low-damage shadow basic with extra visual. | Direct simple: power `100`, PP `45`. | Actor `25->267`, then `speffect9/AH9`. | `PENDING_AUDIT` for two-chunk direct-base polish. |
| 51 | Chú oán | Low damage and defense down. | Direct debuff7, defense down `20%`. | Actor `25->267`, `speffect8/AH9`, `speffect11/AH1`. | `PORTED`; complex visual timing pending. |
| 52 | Quỷ độc | Low damage and small leech/heal chance. | Direct damage, then `game.d.q()` leech param `5`. | Actor `25->267`, `speffect8/AH9`, `speffect10/AH9`. | `PORTED/PARTIAL`; leech effect placement pending. |
| 53 | Cơn ác mộng | Damage scales as target HP gets lower. | HP percent scaling, param `200`. | Actor `25->267`, plus `speffect9/AH9`. | `PORTED/PARTIAL`; passive/relation edge pending. |
| 54 | Mị ảnh | Table suggests Quỷ Mị, but zero-power route is special. | `power == 0`; ordinary debuff8 producer is `NOT_REACHED`. Active debuff8 is user-fixed: +10% damage and 55/45 target roll. | `speffect0/AH9`. | `INTENTIONAL_DEVIATION / GAMEPLAY_FIXED`; do not restore ordinary producer. |
| 55 | Hỗn loạn | Confusion-like special row; switch is allowed. | `power == 0`; ordinary debuff9 producer is `NOT_REACHED`; active random-target consumer is ported. | `speffect12/AH12`. | `PORTED/PARTIAL`; multi-target divergence pending. |
| 56 | Độc ảnh thứ | Higher damage shadow direct with extra visual. | Direct simple: power `150`, PP `30`. | Actor `25->267`, `speffect8/AH9`, `speffect9/AH9`. | `PORTED/PARTIAL`; direct/special timing pending. |
| 57 | Chú Phược Quỷ Lao | Medium damage and defense down. | Direct debuff7, defense down `20%`. | Actor `25->267`, `speffect14/AH12`, `speffect11/AH1`. | `PORTED`; complex visual timing pending. |
| 58 | Quỷ độc tín ngưỡng | Damage and stronger leech/heal chance. | Direct damage, then `game.d.q()` leech param `8`. | Actor `25->267`, `speffect13/AH1`, `speffect10/AH9`. | `PORTED/PARTIAL`; leech placement pending. |
| 59 | Lời nguyền cuối cùng | Damage scales as target HP gets lower. | HP percent scaling, param `250`. | Actor `25->267`. | `PORTED/PARTIAL`; passive/relation edge pending. |

### Wind Lane - Skills 60..69

| Skill | Name | Source role | Logic | Animation | Status / next |
| ---: | --- | --- | --- | --- | --- |
| 60 | Phong nhận | Low-damage wind basic. | Direct simple: power `100`, PP `45`. | Actor `26->268`, state `0`. | `PORTED/PARTIAL`; direct-base visual slice pending. |
| 61 | Phong áp | Low damage and speed down. | Direct debuff5, speed down `5%`. | Actor `26->268`, plus `speffect11/AH1`. | `PORTED`; visual timing pending. |
| 62 | Thuận phong | Damage and self attack-up effect. | Raw damage then `game.d.q()` buff10; current buff10 is user-fixed 15/10/5 decay. | Actor `26->268`, `speffect0/AH9`, `speffect15/AH1`. | `INTENTIONAL_DEVIATION / GAMEPLAY_FIXED` for buff10 value; visual timing pending. |
| 63 | Long quyển | Low damage, small follow-up chance. | Direct damage, then `game.d.q()` follow-up chance `5%`. | Actor `26->268`. | `PORTED/PARTIAL`; follow-up negative/turn timing parity pending. |
| 64 | Nghịch Phong Đoạt | No damage; steals beneficial effects. | Selected-index buff copy/clear, buff11 active re-steal. | `speffect18/AH9`, then `speffect15/AH1`. | `PORTED/PARTIAL`; multi-target selected slot pending. |
| 65 | Vũ Liệt Thuật | No damage; attacks twice per turn. | Buff12 Gia Tốc: follow-up state / PP conservation. | `speffect16/AH9`, then `speffect15/AH1`. | `PORTED/PARTIAL`; global turn-vector parity pending. |
| 66 | Yến Hồi Thiểm | Higher damage wind direct. | Direct simple: power `150`, PP `30`. | Actor `26->268`. | `PORTED/PARTIAL`; direct-base visual slice pending. |
| 67 | Phong Chi Tuyền Qua | Medium damage with source-switch oddity. | Table resembles debuff5, but Phase 9 proved no q()/no debuff side effect from current source branch. | Actor `26->268`, plus `speffect11/AH1`. | `PORTED/PARTIAL`; keep `RAW_DAMAGE_VISUAL_ONLY` unless source route mismatch appears. |
| 68 | Phong Chi Tí Hữu | Medium damage plus Tê Liệt/self attack-up route. | Direct/debuff10 plus `game.d.q()` buff10; buff10 uses user-fixed decay. | Actor `26->268`, `speffect0/AH9`, `speffect15/AH1`. | `INTENTIONAL_DEVIATION / GAMEPLAY_FIXED` for buff10 value; complex visual timing pending. |
| 69 | Phi Yến Hoàn Sào | High damage, follow-up chance. | Direct damage, then `game.d.q()` follow-up chance `8%`. | Actor `26->268`. | `PORTED/PARTIAL`; follow-up timing parity pending. |

## Family Summary And Development Order

Do not implement by numeric order. Implement by shared source family:

| Family | Skills | Current state | Next practical work |
| --- | --- | --- | --- |
| Direct simple base | `0,6,10,16,20,26,30,36,40,46,50,56,60,66` | `PORTED/PARTIAL` | Start with skill10 direct-base P7 timing, then one representative per element. |
| Debuff0 burn/drain | `1,7` | `PORTED` | Only shared fire animation polish remains. |
| Conditional damage | `3,9,23,29` | `PORTED/PARTIAL` | UI description plus visual parity. |
| Explicit debuff chance | `2,8,22,28,41,47` | `PORTED/PARTIAL` | RNG stream/pixel parity later; debuff10 delay remains source-unproven. |
| Direct implicit debuffs | `12,13,18,19,31,32,33,37,38,39,51,57,61` | `PORTED` logic, `PARTIAL` visual | Complex P7 chunk timing per visually distinct row. |
| No-damage buff producers | `4,5,14,15,24,25,34,35,44,45,65` | `PORTED/PARTIAL` | Producer visual pixel/timing polish. |
| Raw damage then self buff | `21,27,42,48,62,68` | `PORTED/PARTIAL` | `game.d.q()` timing relative to damage frame. |
| Heal/leech/follow-up | `11,17,52,58,63,69` | `PORTED/PARTIAL` | Post-skill text/effect placement and follow-up negative case. |
| Buff clear | `43,49` | `PORTED/PARTIAL` | Confirm exact visible clear cue, if source has one. |
| HP percent scaling | `53,59` | `PORTED/PARTIAL` | Passive/relation edge audit. |
| Selected copy | `64` | `PORTED/PARTIAL` | Multi-target selected slot parity. |
| Zero-power special | `54,55` | `NOT_REACHED` ordinary producer; active effects handled separately | Do not re-enable ordinary producer; keep debuff8 gameplay fix documented. |

## Current Next Slice

The immediate next code slice should still be:

```text
skill10 / Diệp Toàn direct-base animation checkpoint tightening
```

Required outputs after that slice:

- PNG before / actor active / damage frame / finish;
- numeric HP and PP before/during/after;
- P7 trace proving `effect.mid[10] = [0,0,21,1,-1,-1,0]`;
- status note: what became `PORTED`, what remains `PORTED/PARTIAL`, and no
  pixel-perfect claim unless original-vs-rebuild frame compare exists.

