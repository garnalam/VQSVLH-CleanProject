# 18. Battle Formula + Status Matrix

Muc tieu: tach rieng cong thuc battle khoi state machine de khi port co the viet code/test theo tung bang. File nay dua tren:

- `source_code/decoded/decompiled_source_cfr/game/b.java`
- `source_code/decoded/decompiled_source_cfr/game/d.java`
- `source_code/decoded/bytecode_javap/game__b.javap.txt`
- `script/decoded/data__script__db.mid.json`
- doi chieu voi [17_battle_state_machine.md](17_battle_state_machine.md)

Trang thai: VERIFIED cho battle core formula/status/effect runtime. Nhung phan con can test runtime la regression gia tri cu the va animation/pixel timing, khong phai do chua hieu source.

## 1. Naming Conventions

| Original | Rebuild name | Meaning |
| --- | --- | --- |
| `game.b` | `BattleUnit` | Pet/monster instance trong battle. |
| `c[]` | `baseStats` | `c[1]` max HP, `c[2]` attack, `c[3]` defense, `c[4]` speed, `c[5]` status/form id. |
| `d[]` | `currentStats` | `d[1]` current HP, `d[2..4]` mutable attack/defense/speed. |
| `z[]` | `skillIds` | Skill id list cua unit. |
| `y[]` | `skillPP` | PP/current uses cua skill. |
| `D` | `selectedSkillId` | Skill dang thi trien. |
| `p` | `target` | Target unit cua action hien tai. |
| `v[16][5]` | `buffSlots` | Self/buff effects. |
| `w[11][5]` | `debuffSlots` | Target/debuff effects. |
| `x[2][3]` | `activeEffectQueue` | `x[0]` active buff ids, `x[1]` active debuff ids. |
| `N[2]` | `activeEffectCount` | Count cua queue tuong ung. |
| `K[]` | `effectScratch` | Scratch vars cho mot so effect nhu skill source/counter. |

## 2. Damage Pipeline

Entry point: `game.b.b(game.b target)` returns `int[]{damage, critFlag, appliedDebuffId}`.

| Step | Formula / branch | Source | Status |
| --- | --- | --- | --- |
| Base | `raw = attacker.B()` | `game.b.B()` | VERIFIED |
| Critical chance | starts `5`, can become `30` for a special owner/form condition, then `+ currentSpeed / 2`, then `+ aq.c[3][4][5]` if attacker `f(4)`. | `game.b.b(target)` | VERIFIED |
| Critical damage | if `ae.a(100) <= critChance`, `raw = raw * 3 / 2`, `critFlag = 1`. | `game.b.b(target)` | VERIFIED |
| Skill family modifier | Applies selected skill group table below. | `game.b.b(target)` | VERIFIED |
| Secondary debuff apply | Uses skill `[7]` as debuff id, `[8]` as chance/param when relevant. | `game.b.b(target)` | VERIFIED |
| Passive/status modifiers | Applies buff/debuff/passive modifiers after secondary effect decision. | `game.b.b(target)` | VERIFIED |
| Element relation | strong -> `damage *= 3`; weak -> `damage = damage * 60 / 100`. | `game.b.a(target)` | VERIFIED |
| Clamp/jitter | if `damage <= 0`, set `1`; then tiny random +/- branch only changes damage when `damage*2/100 <= 0`; clamp again to `>= 1`. | `game__b.javap.txt` | VERIFIED |
| Counter storage | if target has buff `m(5)` and random succeeds, attacker stores final damage in `K[5]`. Return value is unchanged. | `game.b.b(target)` | VERIFIED |

## 3. Base Attack Formula `B()`

`B()` computes attack minus target defense before skill power.

| Branch | Formula | Meaning |
| --- | --- | --- |
| Player passive defense adjust | If target is player-side and `game.g.c(4,0)==2`, target defense becomes `target.c[3] * (100 + aq.c[2][4][5]) / 100`. |
| Target status `f(2)` | `attacker.d[2] - target.d[3] * (100 + aq.c[3][2][5]) / 100`. |
| Attacker status `f(0)` low HP | If `attacker.d[1] <= aq.c[3][0][5] * attacker.c[1] / 100`, use `attacker.d[2] * (100 + aq.c[3][0][6]) / 100 - target.d[3]`. |
| Attacker status `f(1)` | `attacker.d[2] * (100 + aq.c[3][1][5]) / 100 - target.d[3]`. |
| Default | `attacker.d[2] - target.d[3]`. |

## 4. Skill Damage Families

Skill table `aq.c[1][skill]` columns:

`[0] family/element`, `[1] nameText`, `[2] descText`, `[3] powerPercent`, `[4] learnTier`, `[5] ppMax`, `[6] effectMode`, `[7] effectId`, `[8] chanceOrParam`, `[9] targetSide`.

| Skill ids | Damage/effect formula | Status |
| --- | --- | --- |
| `0,6,10,11,12,13,16,17,18,19,20,26,30,31,32,33,36,37,38,39,40,46,50,51,52,54,55,56,57,58,60,61,63,66,68,69` | `damage = raw * skill[3] / 100`. | VERIFIED |
| `1,7` | `damage = raw * skill[3] / 100 + raw / skill[8]`. | VERIFIED |
| `2,8,22,28,41,47` | `damage = raw * skill[3] / 100`; set secondary proc chance `skill[8]`. | VERIFIED |
| `3,9` | If target has debuff `p(0)`, `damage = raw * skill[8] / 100`; else `raw * skill[3] / 100`. | VERIFIED |
| `23,29` | If target has debuff `p(1)`, `damage = raw * skill[8] / 100`; else `raw * skill[3] / 100`. | VERIFIED |
| `43,49` | `damage = raw * skill[3] / 100`; target clears all self/buffs via `target.D()`. | VERIFIED |
| `53,59` | `damage = raw * (skill[8] - attackerHpPercent) / 100`. | VERIFIED |
| Other ids | No direct damage in `game.b.b(target)`; `appliedDebuffId = -1`. Often handled by `game.d.q()` self/buff/heal branch. | VERIFIED |

## 5. Skill Effect Columns Matrix

| Column | Values observed | Runtime meaning | Status |
| --- | --- | --- | --- |
| `[6] effectMode` | `0` count 28 | Direct/no extra self-buff display mode. Damage may still be special-cased by skill id. | VERIFIED |
| `[6] effectMode` | `1` count 18 | Buff/self or same-side effect display/apply mode in `game.d.q()`. | VERIFIED |
| `[6] effectMode` | `2` count 24 | Target/debuff apply mode through `game.b.b(target)` and skill `[7]`. | VERIFIED |
| `[7] effectId` | `-1` count 28 | No secondary effect. | VERIFIED |
| `[7] effectId` | `0..14` | Buff/debuff id. If self/same-side branch, indexes `aq.c[6]`; if target/debuff branch, indexes `aq.c[7]` when id is `0..10`. Ids `11..14` are self/buff-only in current data. | VERIFIED |
| `[8] chanceOrParam` | `-1`, small chance, percent, or divisor | Multipurpose: proc chance, damage percent, heal percent, stat percent, divisor for DOT, or special branch param depending skill id/effect id. | VERIFIED |
| `[9] targetSide` | `0` count 59 | Opponent-side target list / target HP display basis. | VERIFIED |
| `[9] targetSide` | `1` count 11 | Same-side/self target list / heal/buff display basis. | VERIFIED |

## 6. Skill Id -> Effect Id References

| Effect id | Referencing skill ids |
| --- | --- |
| `-1` | `0,3,6,9,10,11,16,17,20,23,26,29,30,36,40,43,46,49,50,52,53,56,58,59,60,63,66,69` |
| `0` | `1,4,7` |
| `1` | `2,5,8,22,28` |
| `2` | `12,14,18` |
| `3` | `13,15,19` |
| `4` | `21,27,31,37` |
| `5` | `32,34,38,61,67` |
| `6` | `33,35,39` |
| `7` | `42,48,51,57` |
| `8` | `44,54` |
| `9` | `45,55` |
| `10` | `41,47,62,68` |
| `11` | `64` |
| `12` | `65` |
| `13` | `24` |
| `14` | `25` |

## 7. Element Relation Matrix

Relation helper: `game.b.a(game.b target)`.

Return values:

- `0`: attacker is strong -> damage `*3`.
- `1`: attacker is weak/resisted -> damage `*60/100`.
- `-1`: neutral.

Strong pairs:

| Attacker element | Target element |
| --- | --- |
| `0` | `1` |
| `1` | `2` |
| `2` | `3` |
| `3` | `0` |
| `5` | `6` |
| `6` | `4` |
| `4` | `5` |

Species relation/catch class: `aq.c[0][species][22]`.

| Class relation | Behavior |
| --- | --- |
| attacker `0/1/3`, target `0/1/3` | Both strong and weak direction checks enabled. |
| attacker `2`, target not `2` | Only attacker strong-direction check enabled. |
| attacker not `2`, target `2` | Only target strong-direction check enabled, so attacker can be resisted but cannot be super-effective via normal pair table. |
| both `2` | Both directions enabled. |

The same class is used by catch chance with multiplier `{1000, 500, 1, 1000}` for classes `{0,1,2,3}`.

## 8. `aq.c[3]` Status/Form Matrix

Raw row format is variable width. Observed rows from `db.mid`: `[nameText, iconOrType, descText, durationOrWeight, flag, paramA?, paramB?]`.

| Status id | Raw row | Observed battle usage | Status |
| --- | --- | --- | --- |
| `0` | `213,1,237,5,1,30,100` | Low-HP attack boost in `B()`/`e(2)`: threshold `[5]=30`, attack bonus `[6]=100`. | VERIFIED |
| `1` | `214,2,238,5,1,10` | Attack boost in `B()`/`e(2)`: attack bonus `[5]=10`. | VERIFIED |
| `2` | `215,3,239,5,1,15` | Defense modifier when target has `f(2)`: target defense bonus `[5]=15`. | VERIFIED |
| `3` | `216,4,240,5,1,20` | Debuff resistance/immunity branch: proc chance multiplied by `(100 - [5]) / 100`. | VERIFIED |
| `4` | `217,5,241,5,1,10` | Critical chance bonus `[5]=10`. | VERIFIED |
| `5` | `218,6,242,5,1,20` | EXP gain bonus `[5]=20`. | VERIFIED |
| `6` | `219,7,243,5,1,100` | Participates in post-battle EXP share branch for non-active alive pets. | VERIFIED |
| `7` | `220,8,244,5,1` | Turn priority: unit with `f(7)` is moved to first slot in turn order. | VERIFIED |
| `8` | `221,9,245,5,1,10,20` | Lifesteal/heal chance after outgoing damage: chance `[5]=10`, heal percent `[6]=20`. | VERIFIED |
| `9` | `222,10,246,5,1` | Accuracy/evasion branch: sets hit/evasion delta to `0` in skill resolve. | VERIFIED |
| `10` | `223,11,247,5,1,10` | Target HP floor/survive branch: if attacker has `f(10)`, target HP can be clamped to `[5]=10`. | VERIFIED |
| `11` | `224,12,248,5,1,20` | Catch chance boost branch: status index for catch multiplier and extra `[5]=20` bonus. | VERIFIED |
| `12` | `225,13,249,1000,0` | No direct battle formula reference found in audited source. | VERIFIED as data, no observed formula use |
| `13` | `226,14,250,2,1` | No direct battle formula reference found in audited source. | VERIFIED as data, no observed formula use |
| `14` | `227,15,251,10,1` | No direct battle formula reference found in audited source. | VERIFIED as data, no observed formula use |
| `15` | `228,16,252,2,1` | No direct battle formula reference found in audited source. | VERIFIED as data, no observed formula use |
| `16` | `229,17,253,10,1` | No direct battle formula reference found in audited source. | VERIFIED as data, no observed formula use |
| `17` | `363,52,364,1,1` | No direct battle formula reference found in audited source. | VERIFIED as data, no observed formula use |

## 9. Buff Slot Matrix `v[16][5]`

`aq.c[6]` rows are `[nameText, descText, duration, param1, param2]`. Slot layout:

- `[0]`: remaining duration.
- `[1]`: primary computed value.
- `[2]`: secondary computed value.
- `[3]`: source/skill param when used.
- `[4]`: active flag.

| Buff id | Raw row | Apply effect | Tick/ongoing effect | Status |
| --- | --- | --- | --- | --- |
| `0` | `333,348,2,30,190` | Defense up: `+ c[3]*30/100`; stores delayed damage value `190*B()/100`. | If active and duration reaches `0`, outgoing damage adds `v[0][2]`. | VERIFIED |
| `1` | `334,349,3,50,50` | Defense down/self modifier: `d[3] = c[3] - c[3]*50/100`; stores `50`. | Outgoing damage adds `damage*50/100`. | VERIFIED |
| `2` | `335,350,3,30,10` | Defense up: `d[3] = c[3] + c[3]*30/100`; stores `10`. | When this unit is target, attacker takes extra damage `Z[0]*10/100`. | VERIFIED |
| `3` | `336,351,3,5,-1` | Heal `5%` max HP immediately. | Tick can heal same stored amount. | VERIFIED |
| `4` | `337,352,2,-1,-1` | Defense up based on source skill `[8]`; saves source skill in `K[4]`. | Restores/maintains defense branch in `o(4)`. | VERIFIED |
| `5` | `338,353,3,30,-1` | Stores chance `30`. | Target can store incoming damage into attacker `K[5]`; later attacker takes that stored damage in resolve. | VERIFIED |
| `6` | `339,354,3,50,-1` | Stores guard chance `50` and param2. | Exact bytecode behavior is odd but verified: call-site is `attacker.b(target)`, condition checks `target.m(6)`, while chance/param are read from attacker `v[6][1]/v[6][2]`; if it passes, `damage = damage * attacker.v[6][2] / 100`. If attacker lacks `v[6]`, default chance/value are `0/0`, so only `ae.a(100)==0` enters and later clamp keeps damage at least `1`. Current data param2 is `-1`; port bytecode-equivalent unless deliberately fixing original behavior. | VERIFIED |
| `7` | `340,355,2,-1,-1` | Speed up based on source skill `[8]`; saves source skill in `K[7]`. | Maintains speed modifier. | VERIFIED |
| `8` | `341,356,4,30,-1` | Stores damage boost `30`. | Outgoing damage adds `damage*30/100`; also costs extra PP in `game.b.a(skill,target)`. | VERIFIED |
| `9` | `342,357,3,50,50` | Speed up `+ c[4]*50/100`, defense down `- c[3]*50/100`. | Maintains speed/defense modifier. | VERIFIED |
| `10` | `343,358,2,-1,-1` | Attack up based on table/param. | Maintains attack modifier. | VERIFIED |
| `11` | `344,359,3,-1,-1` | Copies another slot's active buffs, then clears source slot buffs. | Copy behavior repeats in `o(11)`. | VERIFIED |
| `12` | `345,360,2,-1,-1` | Sets `K[12]=1`. | On tick sets `K[12]=2`; if `K[12]==1`, skill PP cost is refunded. | VERIFIED |
| `13` | `346,361,3,20,-1` | Heal `20%` max HP and clear all debuffs via `C()`. | Tick can heal stored amount. | VERIFIED |
| `14` | `347,362,3,-1,-1` | Clear all debuffs via `C()`. | Blocks future debuff apply while active (`target.m(14)`). | VERIFIED |
| `15` | no row in `aq.c[6]` | Code has switch case `15`, but current data has no row `15` and no skill references it. | Reserved/dead path; do not generate from data. | VERIFIED |

## 10. Debuff Slot Matrix `w[11][5]`

`aq.c[7]` rows are `[nameText, descText, duration]`. Slot layout:

- `[0]`: remaining duration.
- `[1]`: computed param.
- `[2]`: mostly unused/reserved in audited formula.
- `[3]`: source skill id.
- `[4]`: active flag.

| Debuff id | Raw row | Apply effect | Tick/ongoing effect | Status |
| --- | --- | --- | --- | --- |
| `0` | `311,322,3` | Stores pre-skill raw/base damage as `[1]`. | DOT: `k(w[0][1] / sourceSkill[8])`. | VERIFIED |
| `1` | `312,323,2` | Active marker. | No direct `q()` mutation; used by skill ids `23/29` damage condition and catch condition. | VERIFIED |
| `2` | `313,324,3` | Active marker. | Blocks player-side pet switch/turn if no PP; used in battle UI/state. | VERIFIED |
| `3` | `314,325,3` | Stores pre-skill raw/base damage as `[1]`. | On final/late tick: `k(w[3][1] * sourceSkill[8] / 100)`. | VERIFIED |
| `4` | `315,326,3` | Stores source skill `[8]`. | Used in hit/evasion calculation around target speed difference. | VERIFIED |
| `5` | `316,327,3` | Speed down: `[1]=target.c[4]*sourceSkill[8]/100`, `d[4]=c[4]-[1]`. | Maintains speed reduction. | VERIFIED |
| `6` | `317,328,3` | Stores source skill `[8]`. | Incoming damage reduction branch on attacker side: `damage -= damage*w[6][1]/100`. | VERIFIED |
| `7` | `318,329,3` | Defense down: `[1]=target.c[3]*sourceSkill[8]/100`, `d[3]=c[3]-[1]`. | Maintains defense reduction. | VERIFIED |
| `8` | `319,330,4` | Active marker. | Used in hit/evasion branch. | VERIFIED |
| `9` | `320,331,1` | Active marker. | Used for target redirection/side targeting and render/damage placement branches. | VERIFIED |
| `10` | `321,332,4` | Active marker. | Used in catch chance condition. | VERIFIED |

Debuff duration is halved for player-side target when `game.g.c(6,0)==2 && game.g.c(6,1)==1`.

## 11. Secondary Debuff Apply Rules

| Rule | Behavior |
| --- | --- |
| No effect | If `skill[7] == -1`, returned `appliedDebuffId = -1`. |
| Target status `f(3)` | Proc chance is reduced: `chance * (100 - aq.c[3][3][5]) / 100`. |
| Target buff `m(14)` | Blocks debuff apply. |
| Explicit chance | If skill branch sets chance from `[8]`, random must pass `ae.a(100) <= chance`. |
| Duration | `w[id][0] = aq.c[7][id][2]`, or half duration by passive rule above. |
| Source skill | `w[id][3] = selectedSkillId`. |
| Return | Result slot `2` is applied debuff id or `-1`. |

## 12. Catch Formula Matrix

Entry point: `game.d.b(int itemId)`.

| Step | Formula |
| --- | --- |
| Master/zero ball | If `itemId == 0`, return `100`. |
| Target status index | target `m(1)` -> index `1`; target `m(2)` -> `2`; target `m(10)` -> `3`; attacker `f(11)` -> `4`. Later branches overwrite earlier ones. |
| HP factor | target HP `<=15%` -> `85`; `<=50%` -> `45`; `<=100%` -> `20`; else starts `1`. |
| Ball modifier | `chance *= aq.c[4][itemId][6] / 100`. |
| Quality/star modifier | `chance *= {110,100,95,80,70}[target.c[0]-1] / 100`. |
| Status modifier | `chance *= {10,11,12,12,12}[statusIndex] / 10`. |
| Attacker status `f(11)` | Extra `chance *= (100 + aq.c[3][11][5]) / 100`. |
| Species class modifier | `chance *= {1000,500,1,1000}[targetSpecies[22]] / 1000`. |
| Level cap | If target level `>=20` and chance exceeds `{0,15,35,65}[itemId]`, clamp to that cap. |
| Final clamp | `1 <= chance <= 100`. |

## 13. EXP / Battle-Adjacent Statuses

These are not core damage, but they affect battle result or turn/catch behavior.

| Status/effect | Use |
| --- | --- |
| `f(5)` | EXP bonus: `exp = exp * (100 + aq.c[3][5][5]) / 100`. |
| `f(6)` | Non-active alive pet can receive EXP share under a branch when global passive is absent. |
| `f(7)` | Turn order priority: the first unit with `f(7)` is moved to first turn slot before speed sort finalization. |
| `f(9)` | Accuracy/evasion branch sets dodge delta to `0`. |
| `f(10)` | Target HP floor/survive branch after damage. |
| `f(11)` | Catch chance status boost. |
| `m(12)` + `K[12]` | Skill PP interaction: `K[12]==1` refunds PP cost, tick changes to `2`, later battle flow checks `K[12]==2`. |
| `m(13)` or `m(14)` | Pre-turn state clears debuffs via `C()`. |

## 14. Port Checklist

1. Implement table wrappers for `aq.c[0..8]` before writing formulas.
2. Implement `BattleUnit.B()` and `BattleUnit.computeDamage(target)` bytecode-equivalent.
3. Keep integer division order exactly as original Java ME code.
4. Keep `ae.a(100)` random comparison direction exactly: most branches use `<=`, a few use `>`.
5. Treat `aq.c[6][15]` as non-data/dead path in current resources.
6. Write regression tests for:
   - neutral/strong/weak relation;
   - class `[22]` relation behavior;
   - crit and low-HP attack boost;
   - skill families `1/7`, `3/9`, `23/29`, `43/49`, `53/59`;
   - debuff apply blocked by `f(3)` and `m(14)`;
   - buff `m(5)`, `m(6)`, `m(8)`;
   - catch chance with HP/status/species class.

## 15. Remaining Risk

No remaining PARTIAL in formula semantics. Remaining risk is implementation validation:

- reproduce original integer overflow/short cast behavior where source casts to `short`;
- verify random sequence compatibility if rebuild wants deterministic parity;
- verify visual timing of `effect.mid`, `speffect.mid`, `bufDebuf.mid`, and `blood.mid`;
- verify UI edge cases around pet switch and item return, documented separately in file 17.
