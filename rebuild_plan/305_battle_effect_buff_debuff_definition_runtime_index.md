# 305 - Battle Effect / Buff / Debuff Definition Runtime Index

Date: 2026-07-14

Purpose: after the source-clean refactor that introduced `BattleEffectRow`,
this document says where every temporary battle effect is defined, where its
logic runs, and where its animation/visual rows come from.

This is the single lookup page for temporary battle effects:

- `Effect`: common metadata view over a temporary battle row.
- `Buff`: source table `aq.c[6]`, stored in `game.b.v[id][0..4]`.
- `Debuff`: source table `aq.c[7]`, stored in `game.b.w[id][0..4]`.

Do not mix these with held items/passives from `aq.c[3]`. Held items such as
Man Da La Thach, Hong Sac Hai Loa, keys, and evolution materials are not
temporary battle buff/debuff rows.

## Code Ownership After Refactor

| Layer | File | Responsibility |
| --- | --- | --- |
| Table metadata | `rebuild_game/src/main/java/VqsvBattleTables.java` | `BattleEffectRow`, `BattleBuffRow`, `BattleDebuffRow`, `effect(bank,id)` |
| Unit data model / wrappers | `rebuild_game/src/main/java/VqsvBattleUnit.java` | source-shaped slots, HP/stat/skill data, wrapper API for battle runtime |
| Buff/debuff slot logic | `rebuild_game/src/main/java/VqsvBattleEffectLogic.java` | apply, tick, clear, restore/reapply stat effects, pending debuff commit, PP hooks, damage formula hooks |
| P7/P12/P13 flow | `rebuild_game/src/main/java/VqsvBattleRuntime.java` | producer flow, post-skill q(), active queue consumer, P12/P13 visual gate |
| Visual tables | `rebuild_game/src/main/java/VqsvBattleAnimationTables.java` | `effect.mid`, `speffect.mid`, `bufDebuf.mid`, `blood.mid` lookup |
| Smoke coverage | `rebuild_game/src/main/java/VqsvSmokeHarness.java`, `FireSkill.java`, `WoodSkill.java` | focused PNG/debug checkpoints |

## Source Anchors

| Source | Meaning |
| --- | --- |
| `modules/script/decoded/data__script__db.mid.json` group `6` | buff rows |
| `modules/script/decoded/data__script__db.mid.json` group `7` | debuff rows |
| `modules/script/decoded/data__script__chs.mid.json` | source text ids for names/descriptions |
| `modules/source_code/decoded/decompiled_source_cfr/game/b.java` | buff/debuff apply/tick/damage hooks |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java` | P7/P12/P13 state flow and visual gate |
| `modules/script/original/effect.mid` | skill producer animation chunks |
| `modules/script/original/bufDebuf.mid` | active queue body visual rows |
| `modules/script/original/speffect.mid` | special effect renderer rows |
| `modules/img/decoded/data__img__img_804.mid.png` | HUD status icon sheet |

## Slot Layout

| Slot | Buff `v[id][slot]` | Debuff `w[id][slot]` |
| ---: | --- | --- |
| 0 | remaining duration | remaining duration |
| 1 | primary stored number | primary stored number |
| 2 | secondary stored number | secondary stored number when used |
| 3 | source skill id / donor index | source skill id |
| 4 | active flag | active flag |

HUD:

- buff icon cell = `id + 12`
- debuff icon cell = `id + 1`
- duration cell = `134 + remainingDuration`

P12/P13 body visual gate from source `game.d.ai`:

- buff ids with body visual: `3, 5, 13`
- debuff ids with body visual: `0, 1, 2, 3, 8, 9, 10`
- every other id can still have logic/icon, but no active queue body visual

## Buff Definitions - aq.c[6]

| Id | Name | Raw row | Producer skills | Logic definition | Animation / visual | Status |
| ---: | --- | --- | --- | --- | --- | --- |
| 0 | Suc Luc | `[333,348,2,30,190]` | `4` | Duration 2. Defense `+30%`. Stores extra damage `190% * source raw attack snapshot`; source-edge hook adds it when `v[0][0] == 0`. | HUD icon `12`. No P12/P13 body visual. Producer skill4 uses `effect.mid[4]` and speffect `16 -> 15`. | PORTED |
| 1 | Pha Phu | `[334,349,3,50,50]` | `5` | Duration 3. Defense `-50%`. Outgoing damage `+50%`. | HUD icon `13`. No body visual. Producer skill5 uses `effect.mid[5]` and speffect `16 -> 15`. | PORTED |
| 2 | Kinh Cuc | `[335,350,3,30,10]` | `14` | Duration 3. Defense `+30%`. Reflects `10%` of final incoming hit damage; miss does not reflect. | HUD icon `14`. No body visual. Producer skill14 uses actor action `effect.mid[14] = [0,0,21,1,-1,-1,0]`. | PORTED |
| 3 | Khoi Phuc | `[336,351,3,5,-1]` | `15` | Duration 3. Heal `maxHP * 5 / 100` on apply and every active tick. | HUD icon `15`. Has P12/P13 body visual. Producer skill15 uses actor action plus speffect `7`. | PORTED |
| 4 | Phong Ngu | `[337,352,2,-1,-1]` | `21,27` | Duration 2. Row param is sentinel; producer `skill[8]` supplies value. Known producer value `10`, so defense `+10%`. | HUD icon `16`. No body visual. Producer animation depends on skill row. | PORTED |
| 5 | Vo Hinh | `[338,353,3,30,-1]` | `34` | Duration 3. On incoming hit, `30%` chance to store/reflect incoming damage back to attacker through post-hit q() path. | HUD icon `17`. Has P12/P13 body visual row `[0,23,0,-1]`. Producer skill34 uses speffect `4`. | PORTED |
| 6 | Kien Nhan | `[339,354,3,50,-1]` | `35` | Source branch is odd. User-approved gameplay fix: `50%` proc, incoming damage reduced by `50%`. | HUD icon `18`. No body visual. Producer skill35 uses speffect `4 -> 17`. | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED |
| 7 | Linh Xao | `[340,355,2,-1,-1]` | `42,48` | Duration 2. Producer `skill[8]` supplies value. Known value `5`, so speed `+5%`. | HUD icon `19`. No body visual. Producer animation depends on skill row. | PORTED |
| 8 | Dien Ap | `[341,356,4,30,-1]` | `44` | Duration 4. Outgoing damage `+30%`. Skill PP cost consumes one extra PP while active. | HUD icon `20`. No body visual. Producer skill44 uses speffect `19 -> 15`. | PORTED |
| 9 | Hoa Thach | `[342,357,3,50,50]` | `45` | Duration 3. Speed `+50%`; defense `-50%`. | HUD icon `21`. No body visual. Producer skill45 uses speffect `19 -> 15`. | PORTED |
| 10 | Man Luc | `[343,358,2,-1,-1]` | `62,68` | Source sentinel would reduce attack. User-approved gameplay fix: attack boost decays `+15% -> +10% -> +5% -> clear`. | HUD icon `22`. No body visual. Producer uses actor `26`, speffect `0 -> 15`. | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED |
| 11 | Thau Thu | `[344,359,3,-1,-1]` | `64` | Duration 3. Copies selected donor active buffs, clears donor buffs, repeats copy/clear during active tick. | HUD icon `23`. No body visual. | PORTED/PARTIAL |
| 12 | Gia Toc | `[345,360,2,-1,-1]` | `65` | Duration 2. Sets follow-up state `K[12]`; next repeated skill can conserve PP and re-enter P2/P7 flow. | HUD icon `24`. No body visual. Producer speffect `16 -> 15`. | PORTED/PARTIAL |
| 13 | Thach Hoa | `[346,361,3,20,-1]` | `24` | Duration 3. Clears all debuffs, heals `maxHP * 20 / 100` on apply and each tick. | HUD icon `25`. Has P12/P13 body visual row `ap id=13 [1,17,0,-1]`. | PORTED |
| 14 | Thach Phu | `[347,362,3,-1,-1]` | `25` | Duration 3. Clears debuffs and blocks incoming ordinary debuff producers while active. | HUD icon `26`. No body visual. Producer speffect `4 -> 17`. | PORTED |

Note: decompiled `game.b.a(byte,int,int)` has a `case 15`, but decoded
`aq.c[6]` has only rows `0..14`. Treat buff id `15` as source residue until a
real row/callsite is proven.

## Debuff Definitions - aq.c[7]

| Id | Name | Raw row | Producer skills | Logic definition | Animation / visual | Status |
| ---: | --- | --- | --- | --- | --- | --- |
| 0 | Gieo Hat | `[311,322,3]` | `1,7` | Duration 3. Stores pre-skill raw damage. Tick damage is `storedRaw / skill[8]`: skill1 divisor `4`, skill7 divisor `3`. Enables conditional damage for skills `3/9`. | HUD icon `1`. Has P12/P13 body visual, speffect18 anchor. | PORTED |
| 1 | Me Muoi | `[312,323,2]` | `2,8,22,28` | Duration 2. Flag/status. No HP/stat tick. Enables conditional stronger damage for skills `23/29`. Catch multiplier index 1 uses `11/10`. | HUD icon `2`. Has P12/P13 body visual, speffect14/AH type12. | PORTED |
| 2 | Quan Quanh | `[313,324,3]` | `12,18` | Duration 3. Bind/command-lock status. Blocks item/pet/run; skill/catch/shop remain allowed. Catch multiplier index 2 uses `12/10`. Also affects defense formula by `15%` source param. | HUD icon `3`. Has P12/P13 body visual, speffect6/AH type8. | PORTED |
| 3 | Thuc Loai | `[314,325,3]` | `13,19` | Duration 3. Stores pre-skill raw damage. Final tick applies `storedRaw * skill[8] / 100`: skill13 `150%`, skill19 `200%`. | HUD icon `4`. Has P12/P13 body visual through actor effect `21`. | PORTED |
| 4 | Muc | `[315,326,3]` | `31,37` | Duration 3. Stores `skill[8]`: skill31 `1`, skill37 `2`. P7 miss/evasion path subtracts this value from affected unit speed when it attacks. | HUD icon `5`. Source bufDebuf row exists, but source visual gate excludes id4, so no P12/P13 body visual. | PORTED |
| 5 | Cham Chap | `[316,327,3]` | `32,38,61`; skill67 table points here but is NOT_REACHED for debuff | Duration 3. Speed down: stores `baseSpeed * skill[8] / 100`. Skills32/38 `10%`, skill61 `5%`. | HUD icon `6`. Source visual gate excludes id5, so no body visual. | PORTED |
| 6 | Nhut Chi | `[317,328,3]` | `33,39` | Duration 3. Outgoing damage down: stores `skill[8]=10`; damage becomes `damage - damage * 10 / 100`. Source-immediate mutation can exist after miss, but HP/text are hit-gated. | HUD icon `7`. Source visual gate excludes id6, so no body visual. | PORTED |
| 7 | Phong Ngu | `[318,329,3]` | `51,57` | Duration 3. Defense down: stores `baseDefense * 20 / 100`; incoming damage rises while active. | HUD icon `8`. No body visual. | PORTED |
| 8 | Quy Mi | `[319,330,4]` | table skill `54` is NOT_REACHED as ordinary producer | Source route special/unclear. User-approved gameplay fix: active attacker gets outgoing damage `+10%` and target roll `55%` self-hit / `45%` opponent-hit. | HUD icon `9`. Has P12/P13 body visual row `[1,0,0,-1,0,25,0,-1]`. | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED |
| 9 | Hon Loan | `[320,331,1]` | table skill `55` is NOT_REACHED as ordinary producer | Duration 1. Active consumer chooses random target from source-shaped target vector. User confirmed pet switch remains allowed; only debuff2 locks switch. | HUD icon `10`. Has P12/P13 body visual, speffect12/AH type12. | PORTED/PARTIAL |
| 10 | Te Liet | `[321,332,4]` | `41,47`; skill68 also applies direct damage + debuff10 + self buff10 route | Duration 4. Explicit chance family with chance param `10`. Catch multiplier index 3 uses `12/10`. No proven action-delay consumer found for `w[10]`; do not invent delay. | HUD icon `11`. Has P12/P13 body visual row `[1,19,0,-1,1,6,0,-1]`. | PORTED/PARTIAL |

## Current Implementation Boundaries

| Item | Decision |
| --- | --- |
| `BattleEffectRow` | Shared metadata only: bank, ids, name/description, duration, params, raw row. |
| `BattleBuffRow` | Concrete row for source `aq.c[6]`; apply/tick logic lives in `VqsvBattleEffectLogic`. |
| `BattleDebuffRow` | Concrete row for source `aq.c[7]`; commit/tick logic lives in `VqsvBattleEffectLogic`. |
| `BattleUnit` wrappers | `applySourceBuff`, `tickSourceBuff`, `tickSourceDebuff`, `clearBuffs`, `clearDebuffs`, and restore helpers delegate to `VqsvBattleEffectLogic` so old runtime call sites stay stable. Buff/debuff PP hooks and damage formula hooks also route through `VqsvBattleEffectLogic`. |
| Animation definition | Not stored in `BattleEffectRow` yet because producer animation comes from skill `effect.mid`, while active-queue body animation comes from `bufDebuf.mid`; those are two different source routes. |
| Full per-effect polymorphic classes | Not created yet on purpose. The switch logic has moved out of the unit model, but each source switch is still kept source-shaped inside `VqsvBattleEffectLogic` instead of being split into 26 tiny classes too early. |

## Existing Detail Documents

| Topic | Document |
| --- | --- |
| Full current matrix | `263_battle_buff_debuff_effect_current_matrix.md` |
| Buff table closeout | `280_battle_buff_table_0_14_closeout.md` |
| Debuff table closeout | `300_battle_debuff_table_0_10_closeout.md` |
| Per-buff closeouts | `266..279_battle_buff*_*.md` |
| Per-debuff closeouts | `281..299_battle_debuff*_*.md` |
| Skill/effect roadmap | `303_battle_all_skill_source_logic_animation_audit.md` |
| New chat handoff | `304_new_dev_chat_handoff_battle_skill_effect_current.md` |

## Next Source-Clean Step

The next clean code slice should not change gameplay:

1. Create a small `BattleEffectDefinition` or `BattleEffectDescriptor` data view
   only if it reads from `BattleEffectRow` plus `VqsvBattleAnimationTables`.
2. Use it for debug/smoke/reporting labels first.
3. Do not move apply/tick/damage switches into polymorphic effect classes until
   the next gameplay lane needs it and the smoke suite remains green.
