# 241 - Battle Status Effectiveness Full EN Working Doc

Date: 2026-07-13

Purpose: English working document for the battle status/effect layer. Use this
as the primary dev-chat reference to avoid spending context translating the
Vietnamese source tables on every turn.

This document is audit/doc only. No runtime code was changed.

## Source References

| Source | What it proves |
| --- | --- |
| `modules/source_code/decoded/decompiled_source_cfr/game/b.java` | unit fields, buff/debuff apply/tick, damage formula hooks |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java` | P7/P12/P13/P17/P21 battle state flow, active queue consumer, catch hooks |
| `modules/source_code/decoded/decompiled_source_cfr/game/h.java` | HUD status icons, duration cells, UI text binding |
| `modules/script/decoded/data__script__db.mid.json` | raw `aq.c[3]`, `aq.c[6]`, `aq.c[7]` rows |
| `modules/script/decoded/data__script__chs.mid.json` | source Vietnamese text used by `an.f(...)` |
| `modules/script/decoded/data__script__bufDebuf.mid.json` | P12/P13 active queue visual rows |
| `modules/script/decoded/data__script__speffect.mid.json` | AH/special effect rows |
| `modules/img/decoded/data__img__img_804.mid.png` | status icon sheet through sprite `325` |

## Core Rule: Three Separate Status Systems

Do not mix these systems.

| System | Source table | `game.b` storage | Queue | HUD icon | P12/P13 body visual |
| --- | --- | --- | --- | --- | --- |
| Form/status | `aq.c[3]` | `c[5]`, checked by `f(byte)` | none | no queue icon | no P12/P13 queue |
| Self-side buff | `aq.c[6]` | `v[16][5]` | `x[0][0..2]` | `buffId + 12` | gated ids only |
| Target-side debuff | `aq.c[7]` | `w[11][5]` | `x[1][0..2]` | `debuffId + 1` | gated ids only |

## HUD Icon Rules

Source: `game.h.a(b)` and `game.h.b(b)`.

| Condition | sprite `325` icon cell | duration cell |
| --- | ---: | ---: |
| empty slot | `0` | `145` |
| active buff `id`, `v[id][0] > 0` | `id + 12` | `134 + v[id][0]` |
| active debuff `id`, `w[id][0] > 0` | `id + 1` | `134 + w[id][0]` |

Display order:

```text
buff slot0, debuff slot0, buff slot1, debuff slot1, buff slot2, debuff slot2
```

## P12/P13 Body Visual Rules

Source `game.d` only creates active queue body visuals for:

| Bank | Has P12/P13 body visual | Logic/icon only |
| --- | --- | --- |
| buff bank `0` | `3,5,13` | `0,1,2,4,6,7,8,9,10,11,12,14` |
| debuff bank `1` | `0,1,2,3,8,9,10` | `4,5,6,7` |

No P12/P13 body visual does not mean no gameplay effect. For example debuff 7
lowers defense but has no active queue body visual.

## Form/status `aq.c[3]`

These rows are pet form/status/material rows. Battle uses only some ids as
passive hooks. Ids 12..17 are materials/keys, not battle statuses.

| Id | Source name | Source description | Raw row | Gameplay logic | Visual/icon | Rebuild status | Required proof |
| ---: | --- | --- | --- | --- | --- | --- | --- |
| 0 | Man Da La Thach | Current pet HP below 30% increases attack by 100%. | `[213,1,237,5,1,30,100]` | owner low-HP attack boost in damage formula | no HUD queue | PORTED/PARTIAL | high HP vs low HP damage smoke |
| 1 | Hong Sac Hai Loa | Pet attack increases by 10%. | `[214,2,238,5,1,10]` | owner attack boost | no HUD queue | PORTED/PARTIAL | damage increases by source param |
| 2 | Quy Xac Toai Phien | Pet defense increases by 15%. | `[215,3,239,5,1,15]` | target/owner defense boost path | no HUD queue | PORTED/PARTIAL | target takes less damage |
| 3 | O Nha Ue | Resistance increases by 20%. | `[216,4,240,5,1,20]` | reduced/block debuff chance | no HUD queue | PORTED/PARTIAL | forced debuff roll is blocked/reduced |
| 4 | Vien Co Long Cot | Pet critical rate increases by 10%. | `[217,5,241,5,1,10]` | crit chance bonus | no HUD queue | PORTED/PARTIAL | crit chance smoke |
| 5 | Mat Phong Sao | Pet gains 20% more EXP after battle. | `[218,6,242,5,1,20]` | participant EXP multiplier | no HUD queue | PORTED/PARTIAL | EXP participant smoke |
| 6 | Ky Cu Giai Xac | Reserve pet can gain corresponding EXP even without joining battle. | `[219,7,243,5,1,100]` | reserve EXP share | no HUD queue | PORTED/PARTIAL | reserve EXP smoke |
| 7 | Linh Trung Thi Hai | Battle begins. | `[220,8,244,5,1]` | direct battle consumer not proven | UNKNOWN/PENDING | UNKNOWN/PENDING | source search before code |
| 8 | Hap Huyet Dang Man | Attacking restores part of HP. | `[221,9,245,5,1,10,20]` | post-hit self-heal chance/percent | no HUD queue | PORTED/PARTIAL | hit smoke shows attacker heal |
| 9 | Ca Thon Bon | Pet attacks always hit. | `[222,10,246,5,1]` | miss chance becomes 0 | no HUD queue | PORTED/PARTIAL | high-miss setup still hits |
| 10 | Cam Lam Chi Diep | No matter target damage, HP should not drop below 10 points (capture-related text). | `[223,11,247,5,1,10]` | HP floor / capture-related source path | no HUD queue | PORTED/PARTIAL | lethal hit leaves floor HP |
| 11 | Sung vat loi dat | Capture chance increases by 20%. | `[224,12,248,5,1,20]` | catch chance boost | no HUD queue | PORTED/PARTIAL | catch multiplier smoke |
| 12 | Tinh Nguyen Thach | Evolution material. | `[225,13,249,1000,0]` | non-battle material | none | NON_BATTLE | no battle smoke |
| 13 | Thien Gioi Tinh Thach | Advanced evolution material. | `[226,14,250,2,1]` | non-battle material | none | NON_BATTLE | no battle smoke |
| 14 | Thien Dia Than Thach | Rare advanced evolution material. | `[227,15,251,10,1]` | non-battle material | none | NON_BATTLE | no battle smoke |
| 15 | Hon Tinh Thach | Mutation material. | `[228,16,252,2,1]` | non-battle material | none | NON_BATTLE | no battle smoke |
| 16 | Quy Than Tinh Thach | Rare advanced mutation material. | `[229,17,253,10,1]` | non-battle material | none | NON_BATTLE | no battle smoke |
| 17 | Chia khoa | Opens golden chest. | `[363,52,364,1,1]` | non-battle key | none | NON_BATTLE | no battle smoke |

## Self-side buffs `aq.c[6]`

Every active buff must show a HUD icon while `v[id][0] > 0`. Only ids `3,5,13`
should have P12/P13 body visuals.

| Id | Source name | Source description | Raw row | Gameplay logic | Icon cell | Body visual | Rebuild status | Required proof |
| ---: | --- | --- | --- | --- | ---: | --- | --- | --- |
| 0 | Suc Luc | Does not attack this turn, improves defense, next attacks deal increased damage. | `[333,348,2,30,190]` | defense up, stored extra damage | `12` | no | PORTED/PARTIAL | formula/counter smoke |
| 1 | Pha Phu | Damage increases, defense decreases, lasts Y turns. | `[334,349,3,50,50]` | outgoing damage up, self defense down | `13` | no | PORTED | damage/defense smoke |
| 2 | Kinh Cuc | Defense increases, counterattacks opponent for damage, lasts Y turns. | `[335,350,3,30,10]` | defense up, reflect/counter hook | `14` | no | PORTED/PARTIAL | reflect/defense smoke |
| 3 | Khoi phuc | Restores a fixed amount of HP each turn, lasts Y turns. | `[336,351,3,5,-1]` | heal on apply and tick | `15` | yes | PORTED | required checkpoint |
| 4 | Phong ngu | Increases defense value, lasts Y turns. | `[337,352,2,-1,-1]` | skill-param defense up | `16` | no | PORTED/PARTIAL | stat reassert/expiry |
| 5 | Vo hinh | Part of opponent attack is reflected back to itself, lasts Y turns. | `[338,353,3,30,-1]` | reflect/store damage chance | `17` | yes | PORTED/PARTIAL | visual + reflect |
| 6 | Kien nhan | Contact attack chance/ratio becomes half, lasts Y turns. | `[339,354,3,50,-1]` | damage conversion/reduction source oddity | `18` | no | PORTED/PARTIAL | owner-side formula smoke |
| 7 | Linh Xao | Increases agility/speed, lasts Y turns. | `[340,355,2,-1,-1]` | skill-param speed up | `19` | no | PORTED | speed/order smoke |
| 8 | Dien ap | Increases skill PP cost and increases damage, lasts Y turns. | `[341,356,4,30,-1]` | extra PP cost and damage boost | `20` | no | PORTED/PARTIAL | PP drain + damage |
| 9 | Hoa Thach | Increases agility/speed and lowers defense, lasts Y turns. | `[342,357,3,50,50]` | speed up, defense down | `21` | no | PORTED/PARTIAL | stat delta + turn hook |
| 10 | Man Luc | Strength/attack increases, lasts Y turns. | `[343,358,2,-1,-1]` | attack up | `22` | no | PORTED | required checkpoint |
| 11 | Thau Thu | Opponent beneficial statuses transfer to you, lasts Y turns. | `[344,359,3,-1,-1]` | copy buffs from donor and clear donor | `23` | no | PARTIAL | donor vector smoke |
| 12 | Gia Toc | Attacks twice each turn; second hit repeats previous skill without PP cost. | `[345,360,2,-1,-1]` | K12 follow-up/PP conservation | `24` | no | PORTED/PARTIAL | producer-to-consumer smoke |
| 13 | Thach Hoa | Clears abnormal statuses and restores 20% max HP each turn for 3 turns. | `[346,361,3,20,-1]` | clear debuffs + heal tick | `25` | yes | PORTED | cleanse+heal+visual |
| 14 | Thach Phu | Clears abnormal statuses and grants 3 turns of immunity to abnormal statuses. | `[347,362,3,-1,-1]` | clear debuffs + block new debuffs | `26` | no | PORTED/PARTIAL | required checkpoint |

## Target-side debuffs `aq.c[7]`

Every active debuff must show a HUD icon while `w[id][0] > 0`. Only ids
`0,1,2,3,8,9,10` should have P12/P13 body visuals.

| Id | Source name | Source description | Raw row | Gameplay logic | Icon cell | Body visual | Rebuild status | Required proof |
| ---: | --- | --- | --- | --- | ---: | --- | --- | --- |
| 0 | Gieo Hat | Reduces X HP each turn, lasts Y turns. | `[311,322,3]` | per-turn HP damage; skills 3/9 conditional damage | `1` | yes | PORTED | required checkpoint |
| 1 | Me Muoi | Source text is unclear; used as a flag status for Y turns. | `[312,323,2]` | flag; skills 23/29 conditional damage; catch multiplier in rebuild | `2` | yes | PORTED/PARTIAL | flag/conditional/catch smoke |
| 2 | Quan Quanh | Cannot switch pet, cannot run, cannot use item, lasts Y turns. | `[313,324,3]` | blocks switch/item/run; catch multiplier | `3` | yes | PORTED/PARTIAL | command disabled smoke |
| 3 | Thuc Loai | After Y turns, takes X damage; if death occurs, the effect disappears. | `[314,325,3]` | delayed HP damage near expiry | `4` | yes | PORTED | delayed tick smoke |
| 4 | Muc | Reduced effect lasts Y turns. | `[315,326,3]` | accuracy/miss chance hook | `5` | no | PORTED/PARTIAL | miss chance smoke |
| 5 | Cham Chap | Temporarily lowers value, lasts Y turns. | `[316,327,3]` | speed down | `6` | no | PORTED | required checkpoint |
| 6 | Nhut Chi | Damage rate decreases, lasts Y turns. | `[317,328,3]` | incoming damage reduction percent | `7` | no | PORTED/PARTIAL | damage reduction smoke |
| 7 | Phong Ngu | Defense value decreases, lasts Y turns. | `[318,329,3]` | defense down | `8` | no | PORTED | required checkpoint |
| 8 | Quy Mi | Uses opponent attack to allow self to attack the opponent. | `[319,330,4]` | special/zero-power route still not fully proven | `9` if active | yes if active | FLAG/PENDING | prove route before code claim |
| 9 | Hon Loan | Cannot switch own pet, lasts Y turns. | `[320,331,1]` | switch lock text; special route still not fully proven | `10` if active | yes if active | FLAG/PENDING | route + P5 disabled smoke |
| 10 | Te Liet | Each action loses a notable amount of waiting time. | `[321,332,4]` | catch/status/action multiplier family | `11` | yes | PORTED/PARTIAL | required checkpoint |

## Required Smoke Checkpoints Before Skill Work

| Checkpoint | Setup | Visual proof | Logic proof |
| --- | --- | --- | --- |
| `battle_status_buff3_heal_tick` | damaged unit has buff3 | icon `15`, duration cell, P12 body visual | HP increases, duration decreases |
| `battle_status_buff10_attack_up_damage` | attacker has buff10 | icon `22` | next direct damage is higher than baseline |
| `battle_status_buff14_blocks_debuff` | target has buff14, incoming debuff skill | icon `26` | `appliedDebuffId=-1`, no target debuff slot |
| `battle_status_debuff0_damage_tick` | target has debuff0 with stored raw/source skill | icon `1`, P12 body visual | HP decreases by source divisor |
| `battle_status_debuff5_speed_down` | target has debuff5 | icon `6` | speed decreases; miss/order trace changes |
| `battle_status_debuff7_defense_down` | target has debuff7 | icon `8` | next direct damage increases |
| `battle_status_debuff10_catch_multiplier` | wild target has debuff10 | icon `11`, P12 body visual | catch chance path uses debuff10 multiplier |
| `battle_status_form9_no_miss` | attacker has form/status9, target speed is high | no HUD queue icon | miss chance becomes 0; forced high-miss setup still hits |

## Done Criteria

A status/effect is good enough for skill work only when:

1. source row, source name, and source description are documented;
2. apply target is correct: player pet or enemy pet;
3. `v/w` slot and `x[0]/x[1]` queue are correct;
4. HUD icon and duration match `game.h`;
5. P12/P13 body visual follows the source gate, with no invented visuals;
6. gameplay effect really changes HP/stat/damage/catch/miss/EXP/turn behavior;
7. expiry/clear does not leave stale stats;
8. headless PNG smoke and trace/assert exist.

## Next Step

Implement smoke-only/test-support for the eight required checkpoints. Run them
headlessly and write results to `239_battle_status_effectiveness_closeout.md`.

Only fix runtime paths that fail those checkpoints. Do not open the game client
or game jar automatically.
