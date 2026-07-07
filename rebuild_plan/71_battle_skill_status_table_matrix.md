# 71 Battle Skill / Status Table Matrix

Status: SOURCE AUDIT ONLY.

Scope: phase 1, part 2. This document audits the `aq.c[...]` data tables that
feed `game.b` battle-unit behavior and the related `game.d/game.g/game.k` hooks.
No rebuild code was changed in this step.

Primary sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/aq.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- `modules/script/decoded/data__script__db.mid.json`
- `rebuild_game/src/main/java/VqsvBattleTables.java`

Important scope note: the user named `aq.c[2]`, `aq.c[3]`, and `aq.c[7]` as
main status-related sources. The source also proves that `aq.c[6]` is mandatory
for self-side buffs and `aq.c[4]` is mandatory for item/catch/heal behavior.
They are included here so the battle backbone is not missing table dependencies.

## `db.mid` Group Inventory

`game.aq` loads `/data/script/db.mid` into nine short matrices. The decoded JSON
confirms this shape:

| Group | Rows | First row length | Current meaning | Status |
| --- | ---: | ---: | --- | --- |
| `aq.c[0]` | 100 | 23 | Species/base stat/evolution metadata. | PORTED/PARTIAL |
| `aq.c[1]` | 70 | 10 | Skill table. | PORTED/PARTIAL |
| `aq.c[2]` | 8 | 7 | Passive/badge/global battle hooks. | PARTIAL |
| `aq.c[3]` | 18 | variable 5..7 | Status/form condition table. | PARTIAL |
| `aq.c[4]` | 15 | variable 6..9 | Item table, including balls/heal/revive/status-clear. | PARTIAL |
| `aq.c[5]` | 11 | 3 | Not directly used by `game.b` battle core in audited slices. | PENDING |
| `aq.c[6]` | 15 | 5 | Self-side buff table for `v[16][5]`. | PARTIAL |
| `aq.c[7]` | 11 | 3 | Target-side debuff table for `w[11][5]`. | PARTIAL |
| `aq.c[8]` | 4 | 5 | Learn-threshold table for species learn groups. | PARTIAL |

## `aq.c[0]` Species / Base Stat Table

Source-backed index map:

| Index | Meaning from source use | Evidence | Rebuild equivalent | Status |
| --- | --- | --- | --- | --- |
| `[0]` | Name text id. | `game.b.P()`, battle/catch strings use `aq.c[0][species][0]`. | `BattleSpeciesRow.nameTextId` | PORTED |
| `[1]` | Element family `0..6`; drives default skill range and element relation. | `game.b.F()`, `G()`, `T()`, `a(b target)`. | `BattleSpeciesRow.element` | PORTED |
| `[2]` | Evolution category for target species in `R()` / tutorial/evolution checks. | `game.b.R()`, `game.k` evolution branch. | evolution metadata | PENDING/NON_BATTLE |
| `[3]` | Default/random quality when pet quality argument is `-1`. | `game.b.a(...)` sets `c[0]`. | `BattleSpeciesRow.quality` | PORTED |
| `[5][6][7]` | HP stat formula: base + level scale + add. | `game.b.a(...)`, `V()`, static `b(...)`. | `BattleSpeciesRow.statHp` | PORTED |
| `[8][9][10]` | Attack stat formula. | `game.b.a(...)`, `V()`, `a(... stat index 2)`. | `BattleSpeciesRow.statAttack` | PORTED |
| `[11][12][13]` | Defense stat formula. Level scale uses `/10` in source. | `game.b.a(...)`, `V()`, `a(... stat index 3)`. | `BattleSpeciesRow.statDefense` | PORTED |
| `[14][15][16]` | Speed stat formula. Level scale uses `/10` in source. | `game.b.a(...)`, `V()`, `a(... stat index 4)`. | `BattleSpeciesRow.statSpeed` | PORTED |
| `[17]` | Battle sprite/visual id. | `game.b.a(...)` sets `C`; AH/L effects pass this id. | `BattleSpeciesRow.spriteId` | PORTED |
| `[18]` | Learn group id into `aq.c[8]`. | `game.b.F()`. | `BattleSpeciesRow.learnGroup` | PORTED/PARTIAL |
| `[19][20][21]` | Evolution target/item/material fields found through `aq.a(..., byte 19/20/21)`. | `game.b.J()`, `R()`. | evolution metadata | PENDING/NON_BATTLE |
| `[22]` | Relation/capture class; modifies element relation and catch chance. | `game.b.a(b)`, `game.d.b(itemId)`, `game.g` collection counters. | `BattleSpeciesRow.relationClass` | PORTED/PARTIAL |

Sample rows used in current smoke routes:

| Species | Source role | Row facts |
| --- | --- | --- |
| `34` | Bunny tutorial target. | Resolved through `BattleSpeciesRow` in rebuild; capture logic uses `[22]`. |
| `68` | Elder tutorial battle target. | Skill set in `game.g.a(68,7,...)` seed includes skills `1,40,45`. |
| `5` | Sophie/kidnapper early battle route. | Used by route smoke and current battle entry snapshots. |

## `aq.c[1]` Skill Table

Decoded row shape is 10 columns. Examples:

| Skill | Raw row | Source-relevant note |
| --- | --- | --- |
| `0` | `[0,117,529,100,0,45,0,-1,-1,0]` | Basic direct-damage skill. |
| `15` | `[1,132,544,0,1,10,1,3,-1,1]` | No-damage self/other skill; P7 actor `u33` plus speffect chunk already audited. |
| `45` | `[4,162,574,0,1,10,1,9,-1,1]` | No-damage skill with special effects; current smoke target. |
| `52` | `[5,169,581,80,0,45,0,-1,5,0]` | Direct-damage skill with chance/param column `5`. |
| `68` | `[6,185,597,110,3,15,1,10,5,0]` | Direct + buff/status mode path. |

Source-backed column map:

| Index | Meaning from source use | Evidence | Status |
| --- | --- | --- | --- |
| `[0]` | Element/skill family. Default learned skills are selected from `speciesElement * 10 .. +9`. | `game.b.F()`, `G()`. | PORTED |
| `[1]` | Skill name text id. | `VqsvBattleTables`, battle UI text via script text table. | PORTED |
| `[2]` | Skill description text id. | `VqsvBattleTables`, skill UI description. | PORTED |
| `[3]` | Power percent / direct-damage gate. If `0`, P7 uses no-damage path. Otherwise damage is applied after animation/timing. | `game.b.b(target)`, `game.d` P7 check around `aq.c[1][D][3] != 0`. | PORTED/PARTIAL |
| `[4]` | Learn tier/threshold. Candidate skill is learnable if `skill[4] <= aq.c[8][learnGroup][levelTier]`. | `game.b.F()`. | PARTIAL |
| `[5]` | Max PP. Used when loading/resetting learned skills and item PP restore clamp. | `game.b.v()`, `B(int)`, `g(byte)`. | PORTED |
| `[6]` | Effect mode. `1` means apply self-side buff/status text through `aq.c[6]`; other values usually leave direct damage/debuff path. | `game.d.q()` checks `by = aq.c[1][skill][6]`; if `by == 1`, displays `aq.c[6][effectId][0]`. `game.b.b(target)` uses `effectId` as debuff id when applicable. | PARTIAL |
| `[7]` | Effect id. For `effectMode == 1`, this indexes `aq.c[6]` buff. For damage/debuff skills it can index `aq.c[7]`. Special skills are also hardcoded by id. | `game.d.q()`, `game.b.b(target)`, skill-specific switch in `game.d.q()`. | PARTIAL |
| `[8]` | Chance/parameter column. Used as hit/status chance, percent, divisor, heal percent, or skill-specific parameter depending on skill id. | `game.b.b(target)`, `game.d` P6/P7 evade and target checks, `game.d.q()`. | PARTIAL |
| `[9]` | Target routing. Source uses `0` as target/opponent route for HP latch/damage; non-zero routes to selected/owner/self path in `game.d.q()` and P6 target select. | `game.d.q()` sets `aF` from `h.N()` when `[9]==0`, otherwise from `h.p`; P6 target builder also uses target mode. | PORTED/PARTIAL |

Skill behavior families from source:

| Family | Skills seen in source | Behavior |
| --- | --- | --- |
| Direct damage percent | many skills including `0,10,52,58,63,68,69` | `game.b.b(target)` computes damage from attack-defense, skill `[3]`, element relation, crit, passives, status. |
| No-damage special/buff | `15`, `45` and other `[3] == 0` rows | P7 animation/effect runs, then no direct HP damage. `effectMode/effectId` can still apply buff text/effect. |
| Skill-id hardcoded post-effect | `11,17,21,27,42,48,52,58,62,64,68` | `game.d.q()` has explicit cases for heal/self-buff/status handling. |
| Debuff chance skills | skills with effect id into `aq.c[7]` | `game.b.b(target)` may write `target.w[effectId]` when chance passes. |

Open point: column `[6]` is named `effectMode` in rebuild, but source meaning is broader than one label. It gates the self-side `aq.c[6]` display path in `game.d.q()` and interacts with hardcoded skill ids. Keep as PARTIAL until every skill id 0..69 is classified.

## `aq.c[8]` Learn Threshold Table

Decoded rows:

| Learn group | Raw row |
| --- | --- |
| `0` | `[0,0,0,1,1]` |
| `1` | `[0,0,1,1,2]` |
| `2` | `[0,0,1,2,3]` |
| `3` | `[0,1,2,3,3]` |

Source use:

- `game.b.F()` reads species learn group from `aq.c[0][species][18]`.
- It computes a level tier through `W()`: level thresholds `[5,10,20,30,40]`.
- It scans the 10 skills for the species element family and accepts a skill when
  `aq.c[1][skill][4] <= aq.c[8][learnGroup][tier]`.
- `game.b.G()` auto-adds the first element skill at low level and later adds
  eligible skills until skill count reaches the source limit.

Status: PARTIAL. The table semantics are source-backed, but the full rebuild
learn/replace UI and save parity are later work.

## `aq.c[6]` Self-Side Buff Table

Decoded row shape: `[nameTextId, descriptionTextId, duration, paramA, paramB]`.

Examples:

| Buff id | Raw row | Source behavior summary |
| --- | --- | --- |
| `0` | `[333,348,2,30,190]` | Defense increase plus delayed damage bonus value. |
| `3` | `[336,351,3,5,-1]` | Heal by percent of max HP. |
| `6` | `[339,354,3,50,-1]` | Chance/value slot used by damage formula; source has unusual attacker/target read pattern. |
| `9` | `[342,357,3,50,50]` | Speed up and defense down. |
| `10` | `[343,358,2,-1,-1]` | Attack increase. |
| `13` | `[346,361,3,20,-1]` | Heal and clear debuffs. |

`game.b.a(byte buffId, int value, int sourceSkill)` proves common layout:

| Column | Meaning |
| --- | --- |
| `[0]` | Buff name text id, used by `game.d.q()` floating text. |
| `[1]` | Description text id. |
| `[2]` | Duration/counter copied into `v[buffId][0]`. |
| `[3]` | Primary percentage/value. |
| `[4]` | Secondary percentage/value. |

Buff slot layout in unit:

| Slot | Meaning |
| --- | --- |
| `v[id][0]` | Remaining duration/counter. |
| `v[id][1]` | Primary computed value. |
| `v[id][2]` | Secondary computed value. |
| `v[id][3]` | Source skill/id for selected buff types. |
| `v[id][4]` | Active flag. |

Source-backed buff id matrix:

| Buff id | Behavior from `game.b` | Status |
| --- | --- | --- |
| `0` | Increases defense; stores secondary damage bonus from `paramB * B() / 100`; later damage adds `v[0][2]` when duration hits 0. | PARTIAL |
| `1` | Lowers defense and stores `paramB`; damage adds `damage * v[1][2] / 100`. | PARTIAL |
| `2` | Increases defense. | PARTIAL |
| `3` | Heals by max HP percent. | PARTIAL |
| `4` | Skill-derived defense increase using skill `[8]`; stores skill id in `K[4]`. | PARTIAL |
| `5` | Chance/value holder used by target reflect/check path after damage. | PARTIAL |
| `6` | Chance/value damage modification. Source checks `target.m(6)` but reads attacker `v[6][1]/v[6][2]`; keep bytecode behavior. | PORTED/PARTIAL |
| `7` | Skill-derived speed increase using skill `[8]`; stores skill id in `K[7]`. | PARTIAL |
| `8` | PP cost modifier and damage boost. | PARTIAL |
| `9` | Speed increase plus defense decrease. | PARTIAL |
| `10` | Attack increase. | PARTIAL |
| `11` | Copies active buffs from another unit index stored in `v[11][1]`. | PENDING |
| `12` | PP conservation/turn flag through `K[12]`. | PARTIAL |
| `13` | Heal and clears debuffs. | PARTIAL |
| `14` | Clears debuffs and blocks debuff application while active. | PARTIAL |

Note: `aq.c[6]` is not optional for battle. It is the source of names, duration,
and params for many skill effects.

## `aq.c[7]` Target-Side Debuff Table

Decoded row shape: `[nameTextId, descriptionTextId, duration]`.

Examples:

| Debuff id | Raw row | Source behavior summary |
| --- | --- | --- |
| `0` | `[311,322,3]` | Damage-over-time style, source value stored from skill damage. |
| `3` | `[314,325,3]` | Damage-over-time percent branch. |
| `5` | `[316,327,3]` | Speed decrease. |
| `6` | `[317,328,3]` | Incoming damage reduction/modifier. |
| `7` | `[318,329,3]` | Defense decrease. |
| `10` | `[321,332,4]` | Used in damage/catch/status conditions. |

`game.b.b(target)` proves:

- Debuff id comes from `aq.c[1][selectedSkill][7]` when the skill family allows it.
- Chance and parameters come mainly from skill `[8]`.
- Duration is `aq.c[7][debuffId][2]`, but passive `aq.c[2][6]` can halve duration for player-side conditions.
- Text uses `aq.c[7][debuffId][0]` in `game.d` damage display when a debuff lands.

Debuff slot layout in unit:

| Slot | Meaning |
| --- | --- |
| `w[id][0]` | Remaining duration/counter. |
| `w[id][1]` | Primary computed value. |
| `w[id][2]` | Secondary value, rarely used in visible source. |
| `w[id][3]` | Source skill id. |
| `w[id][4]` | Active flag. |

Source-backed debuff id matrix:

| Debuff id | Behavior from `game.b` | Status |
| --- | --- | --- |
| `0` | Damage over time: `q(0)` damages by `w[0][1] / skill[8]`. | PARTIAL |
| `1` | Presence changes damage for skill ids `23/29`. | PARTIAL |
| `2` | Interacts with form/status defense calculation in `B()`. | PARTIAL |
| `3` | Damage over time: `q(3)` damages by `w[3][1] * skill[8] / 100` when duration threshold is met. | PARTIAL |
| `4` | Applied as a debuff id from skill table; exact per-turn behavior not fully proven in audited body. | PENDING |
| `5` | Speed decrease: stores `target.c[4] * skill[8] / 100`, lowers `target.d[4]`. | PARTIAL |
| `6` | Damage reduction percent: final damage subtracts `damage * w[6][1] / 100`. | PARTIAL |
| `7` | Defense decrease: stores `target.c[3] * skill[8] / 100`, lowers `target.d[3]`. | PARTIAL |
| `8..10` | Rows exist and source checks some ids through status/form helpers, but full per-turn effects are not closed. | PENDING |

## `aq.c[3]` Status/Form Condition Table

Decoded rows have variable length, commonly:
`[nameTextId, iconOrType, descriptionTextId, durationOrRate, flag, paramA, paramB]`.

Examples:

| Status id | Raw row | Source-backed behavior |
| --- | --- | --- |
| `0` | `[213,1,237,5,1,30,100]` | Low-HP attack boost: if unit has form/status `0` and HP <= `paramA%`, attack uses `paramB%` boost. |
| `1` | `[214,2,238,5,1,10]` | Attack boost by `paramA%`. |
| `2` | `[215,3,239,5,1,15]` | Defense interaction: target defense can be multiplied by `100 + paramA`. |
| `3` | `[216,4,240,5,1,20]` | Reduces chance for debuff/status application. |
| `4` | `[217,5,241,5,1,10]` | Crit chance bonus via `paramA`. |
| `5` | `[218,6,242,5,1,20]` | EXP gain boost by `100 + paramA`. |
| `8` | `[221,9,245,5,1,10,20]` | Self-heal chance/percent after action if skill targets opponent. |
| `10` | `[223,11,247,5,1,10]` | Prevents target HP dropping below `paramA` in some damage paths. |
| `11` | `[224,12,248,5,1,20]` | Catch chance bonus by `100 + paramA`. |

Usage categories:

| Source site | Meaning |
| --- | --- |
| `game.b.B()` and `e(byte)` | Status/form ids `0..4` modify attack/defense/crit related stats. |
| `game.b.b(target)` | Status id `3` can reduce debuff chance; status id `4` adds crit chance; status ids `0/1/2` influence damage. |
| `game.d.q()` | Status id `8` can heal after attack; status id `10` can enforce HP floor. |
| `game.d.h(deadTarget)` | Status id `5` boosts EXP awarded. |
| `game.d.b(itemId)` catch formula | Status id `11` boosts capture chance. |

Status: PARTIAL. Rows `6,7,9,12..17` exist, but their complete battle meaning
was not proven in this slice. Do not assign names/logic to them without a
dedicated status audit.

## `aq.c[2]` Passive / Badge / Global Hook Table

Decoded row shape: 7 columns. Examples:

| Hook id | Raw row | Source-backed behavior |
| --- | --- | --- |
| `0` | `[187,0,195,203,211,220,10]` | Post-battle heal: if owned/active, living player pets heal by species base HP percent from `[6]`. |
| `1` | `[188,0,196,204,211,-1,5]` | Max HP boost in `game.b.U()` when `game.g.o().c(1,0)==2 && c(1,1)==1`. |
| `2` | `[189,0,197,205,211,-1,5]` | Defense boost in `game.b.U()` under same owned/equipped gate shape. |
| `3` | `[190,0,198,206,211,-1,2]` | Damage boost when player side, owned/equipped, and `game.k.O == 2`. |
| `4` | `[191,0,199,207,211,5,2]` | Defense/evasion related: target defense boost in `B()` and hit/evasion math in `game.d`. |
| `5` | `[192,0,200,208,211,1,20]` | Battle branch hook seen in `game.d`; exact full behavior remains partial. |
| `6` | `[193,0,201,209,211,5,50]` | Damage boost and debuff duration reduction hook. |
| `7` | `[194,0,202,210,212,100,-1]` | EXP sharing/off-party reward hook in `game.d.h(deadTarget)`. |

Gate semantics:

- `game.g.o().c(byte id, byte slot)` returns state from save/global table `B`.
- Most battle hooks check slot `0 == 2` and sometimes slot `1 == 1`.
- `game.g.a(byte element, int species, byte state)` writes ownership/collection
  state and updates counters for species relation class.

Status: PARTIAL. Hook ids and the battle sites above are source-backed, but the
full unlock/equip UI and save lifecycle are outside this phase.

## `aq.c[4]` Item Table

Decoded row shape varies. Rebuild already uses it for catch balls, but `game.b`
also uses it for item behavior:

| Column | Meaning from source | Evidence |
| --- | --- | --- |
| `[0]` | Item name text id. | UI/inventory text. |
| `[1]` | Icon id. | `BattleItemRow.iconId`. |
| `[2]` | Description text id. | UI text. |
| `[3]` | Price/value. | shop/inventory paths. |
| `[4]` | Currency/type/class. | `game.g` inventory filters. |
| `[5]` | Behavior id. | `game.b.w(itemId)`, `x(itemId)`, catch list uses behavior `0` for balls. |
| `[6]` | Primary param. | heal percent, PP amount, catch chance base. |
| `[7]` | Secondary param. | flat heal or revive PP depending behavior. |
| `[8]` | Tertiary param. | PP restore for combo/revive behaviors. |

Behavior ids from `game.b.w/x`:

| Behavior | Source effect | Status |
| --- | --- | --- |
| `0` | Ball/capture item. Not used by `game.b.w`; used by `game.d` catch flow. | PARTIAL |
| `1` | HP heal: `maxHp * [6] / 100 + [7]`. | PORTED/PARTIAL |
| `2` | PP restore to all skills by `[6]`. | PARTIAL |
| `3` | HP heal plus PP restore. | PARTIAL |
| `4` | Revive/reset actor plus HP/PP restore. | PARTIAL |
| `5` | Clear debuffs. | PARTIAL |
| `6` | Set `d[6]=2`; exact item-state meaning pending. | PENDING |

Catch chance uses `aq.c[4][itemId][6]` in `game.d.b(itemId)`, then multiplies
by target HP bucket, quality modifier, status modifier, relation class, and
level caps. This is battle-critical for P21/P17.

## Passive / Hook Impact on Formula and Turn

Source-backed formula hooks:

| Hook source | Impact | Evidence | Status |
| --- | --- | --- | --- |
| `aq.c[2][1][6]` | Player passive increases max HP during stat recalculation. | `game.b.U()`. | PARTIAL |
| `aq.c[2][2][6]` | Player passive increases defense during stat recalculation. | `game.b.U()`. | PARTIAL |
| `aq.c[2][3][5]` | Player-side damage boost gated by `game.k.O == 2`. | `game.b.b(target)`. | PARTIAL |
| `aq.c[2][4][5]` | Defense boost for target in attack calculation. | `game.b.B()`. | PARTIAL |
| `aq.c[2][4][6]` | Hit/evasion threshold bonus in P7 damage application. | `game.d` P7. | PARTIAL |
| `aq.c[2][6][5]` | Player-side damage boost. | `game.b.b(target)`. | PARTIAL |
| `aq.c[2][6]` active | Debuff duration can be halved for target-side debuffs. | `game.b.b(target)`. | PARTIAL |
| `aq.c[2][0][6]` | Post-battle living pet HP restore percent. | `game.d.T()`/post-battle cleanup. | PARTIAL |
| `aq.c[2][7]` active | Off-party/extra EXP handling after kill. | `game.d.h(deadTarget)`. | PARTIAL |

Turn/status hooks:

| Source | Impact | Status |
| --- | --- | --- |
| `game.b.a(byte skill,b target)` | Consumes PP, except buff `12` can refund and buff `8` can cost extra. | PORTED/PARTIAL |
| `game.b.q(debuffId)` | Per-turn debuff tick damage/stat changes. | PARTIAL |
| `game.b.d(buffId,queueIndex)` / `c(debuffId,queueIndex)` | Decrement durations and clear when expired. | PARTIAL |
| `game.d.h()` / `p()` | Advances turn order and skips dead/unusable units. | PORTED/PARTIAL |

## Current Rebuild Coverage

| Area | Current status | Notes |
| --- | --- | --- |
| Species stat formula from `aq.c[0]` | PORTED | Already in `BattleSpeciesRow` / `BattleUnit`. |
| Skill row parsing from `aq.c[1]` | PORTED/PARTIAL | Columns are named, but full behavior classification for all 70 skills is not complete. |
| Direct damage formula | PORTED/PARTIAL | Core bytecode shape is in place; all status/passive edge cases still need matrix-driven validation. |
| Learn table `aq.c[8]` | PARTIAL | Source meaning known; learn/replace UX not complete. |
| Buff table `aq.c[6]` | PARTIAL | Core slots known; not every id behavior is fully ported. |
| Debuff table `aq.c[7]` | PARTIAL | Duration/name and key ids known; several ids need dedicated behavior audit. |
| Status table `aq.c[3]` | PARTIAL | Important ids are source-backed; full row semantics pending. |
| Passive hooks `aq.c[2]` + `game.g/game.k` | PARTIAL | Formula hooks known; unlock/equip/save parity pending. |
| Item table `aq.c[4]` | PARTIAL | Catch/heal/revive behavior shape known; full item UI/runtime later. |

## Unknowns / Follow-Up

1. Full skill classification for all `aq.c[1][0..69]`: direct damage,
   no-damage, self-buff, target-debuff, heal, special hardcoded behavior.
2. Full `aq.c[3]` status id semantics for ids not directly proven in this pass.
3. Exact `aq.c[5]` purpose. It was loaded from `db.mid` but not proven as a
   `game.b` battle-core dependency in this audit.
4. Full passive unlock/equip lifecycle behind `game.g.o().c(id,slot)`.
5. Full item behavior parity for inventory, warnings, and battle item UI.

## Phase 1 Part 2 Conclusion

The battle data dependencies are now mapped enough to continue phase 1 without
guessing field meanings. The minimum table set for a correct battle backbone is:

- `aq.c[0]` species/stat/evolution metadata.
- `aq.c[1]` skill power/PP/effect/target rows.
- `aq.c[2]` passive/global battle hooks.
- `aq.c[3]` status/form modifiers.
- `aq.c[4]` battle item and catch parameters.
- `aq.c[6]` self-side buff rows.
- `aq.c[7]` target-side debuff rows.
- `aq.c[8]` learn thresholds.

The next phase-1 slice should be a skill/status behavior classification table:
all skill ids `0..69`, all buff ids `0..14`, all debuff ids `0..10`, and all
status ids `0..17`, marked `PORTED / PARTIAL / PENDING` against rebuild runtime.
