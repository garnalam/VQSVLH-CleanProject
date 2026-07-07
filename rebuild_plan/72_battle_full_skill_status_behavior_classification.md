# 72 Battle Full Skill / Status Behavior Classification

Status: SOURCE AUDIT ONLY.

Scope: phase 1, part 2 follow-up. This document classifies every battle-facing
row currently known from `db.mid`: all `aq.c[1]` skills, `aq.c[2]` passive
hooks, `aq.c[3]` status/form rows, `aq.c[6]` self-side buff rows, and `aq.c[7]`
target-side debuff rows.

No rebuild code was changed in this step.

Primary sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- `modules/script/decoded/data__script__db.mid.json`
- `rebuild_game/src/main/java/VqsvBattleUnit.java`
- `rebuild_game/src/main/java/VqsvBattleRuntime.java`

Important honesty rule: this file classifies behavior only when the source
shows how the row is consumed. Several rows are now classified as
`SOURCE_SWITCH_GAP` or `PENDING` rather than guessed from table numbers alone.

## Classification Legend

| Label | Meaning |
| --- | --- |
| `SOURCE_CLASSIFIED` | Source shows the behavior class and table fields used. |
| `PORTED` | Rebuild has corresponding logic in the current battle runtime/unit. |
| `PARTIAL` | Behavior is known but rebuild or source audit is incomplete. |
| `PENDING` | Row exists, but complete behavior is not yet proven/ported. |
| `SOURCE_SWITCH_GAP` | Row values imply battle behavior, but the damaged/decompiled switch does not clearly include the id. Needs bytecode or deeper control-flow validation before porting. |
| `NON_BATTLE_OR_UI` | Not a direct battle formula/status behavior in this audit. |

## Source Behavior Families

`game.b.b(target)` is the main damage/debuff classifier. Its visible switch
groups are:

| Family | Skill ids | Source behavior |
| --- | --- | --- |
| `DIRECT_SIMPLE` | `0,6,10,11,12,13,16,17,18,19,20,26,30,31,32,33,36,37,38,39,40,46,50,51,52,54,55,56,57,58,60,61,63,66,68,69` | Damage = attack base * `skill[3] / 100`; if `skill[7] != -1`, target debuff may be applied. |
| `DIRECT_PLUS_RAW_DIVISOR` | `1,7` | Damage = base * `skill[3] / 100` + base / `skill[8]`; can apply debuff id `0`. |
| `DIRECT_EXPLICIT_DEBUFF_CHANCE` | `2,8,22,28,41,47` | Damage = base * `skill[3] / 100`; debuff chance comes from `skill[8]`. |
| `CONDITIONAL_IF_TARGET_DEBUFF0` | `3,9` | Uses `skill[8]` as alternate damage percent if target has debuff `0`; otherwise uses `skill[3]`. |
| `CONDITIONAL_IF_TARGET_DEBUFF1` | `23,29` | Uses `skill[8]` as alternate damage percent if target has debuff `1`; otherwise uses `skill[3]`. |
| `DIRECT_AND_CLEAR_TARGET_BUFFS` | `43,49` | Direct damage, then `target.D()` clears target self-side buffs. |
| `HP_PERCENT_SCALING` | `53,59` | Damage = base * (`skill[8]` - attacker HP%) / 100. |

`game.d.q()` then applies hardcoded post-animation skill effects:

| Hardcoded path | Skill ids | Source behavior |
| --- | --- | --- |
| `DQ_HEAL_ATTACKER` | `11,17` | Heal attacker by `attacker.B() * skill[8] / 100`. |
| `DQ_SELF_BUFF` | `21,27,42,48,62,68` | Apply self-side buff id `skill[7]`. |
| `DQ_DAMAGE_LEECH_IF_AA` | `52,58` | If battle flag `aa` is true, heal attacker by damage * `skill[8] / 100`. |
| `DQ_BUFF_WITH_SELECTED_INDEX` | `64` | Apply buff id `skill[7]` using selected index/target `h.I`. |
| `DQ_DEFAULT_EFFECTMODE1` | other skills with `skill[6] == 1` | Apply buff/effect id `skill[7]` to `h.p` in the default branch. Target meaning depends on target selection. |

## Full Skill Classification `aq.c[1][0..69]`

Columns are `[elementFamily,nameText,descText,power,learnTier,pp,effectMode,effectId,param,targetMode]`.

| Skill | Raw row | Source behavior class | Rebuild coverage | Notes |
| --- | --- | --- | --- | --- |
| `0` | `[0,117,529,100,0,45,0,-1,-1,0]` | `DIRECT_SIMPLE` | PORTED | Basic direct damage. |
| `1` | `[0,118,530,50,0,45,2,0,4,0]` | `DIRECT_PLUS_RAW_DIVISOR`, debuff `0` | PARTIAL | Damage formula ported; debuff tick/display still partial. |
| `2` | `[0,119,531,100,0,45,2,1,10,0]` | `DIRECT_EXPLICIT_DEBUFF_CHANCE`, debuff `1` chance `10` | PARTIAL | Debuff application source-shaped. |
| `3` | `[0,120,532,100,1,30,0,-1,120,0]` | `CONDITIONAL_IF_TARGET_DEBUFF0` | PORTED/PARTIAL | Alternate damage percent when target has debuff `0`. |
| `4` | `[0,121,533,0,1,10,1,0,-1,1]` | `DQ_DEFAULT_EFFECTMODE1`, buff/effect `0` | PARTIAL | No direct damage; target/self routing needs P6 validation. |
| `5` | `[0,122,534,0,1,10,1,1,-1,1]` | `DQ_DEFAULT_EFFECTMODE1`, buff/effect `1` | PARTIAL | No direct damage. |
| `6` | `[0,123,535,150,2,30,0,-1,-1,0]` | `DIRECT_SIMPLE` | PORTED | High-power direct damage. |
| `7` | `[0,124,536,75,2,30,2,0,3,0]` | `DIRECT_PLUS_RAW_DIVISOR`, debuff `0` | PARTIAL | Uses divisor `3`. |
| `8` | `[0,125,537,200,3,15,2,1,20,0]` | `DIRECT_EXPLICIT_DEBUFF_CHANCE`, debuff `1` chance `20` | PARTIAL | Direct damage plus debuff. |
| `9` | `[0,126,538,200,3,15,0,-1,250,0]` | `CONDITIONAL_IF_TARGET_DEBUFF0` | PORTED/PARTIAL | Stronger when target has debuff `0`. |
| `10` | `[1,127,539,100,0,45,0,-1,-1,0]` | `DIRECT_SIMPLE` | PORTED | Basic direct damage. |
| `11` | `[1,128,540,90,0,45,0,-1,10,0]` | `DIRECT_SIMPLE` + `DQ_HEAL_ATTACKER` | PARTIAL | Post-hit heal not fully parity-checked in rebuild. |
| `12` | `[1,129,541,50,0,45,2,2,-1,0]` | `DIRECT_SIMPLE`, debuff/effect `2` | PARTIAL | Effect chance appears implicit. |
| `13` | `[1,130,542,50,1,30,2,3,150,0]` | `DIRECT_SIMPLE`, debuff/effect `3` | PARTIAL | Debuff `3` uses skill param in tick. |
| `14` | `[1,131,543,0,1,10,1,2,-1,1]` | `DQ_DEFAULT_EFFECTMODE1`, buff/effect `2` | PARTIAL | No direct damage. |
| `15` | `[1,132,544,0,1,10,1,3,-1,1]` | `DQ_DEFAULT_EFFECTMODE1`, buff/effect `3`; P7 actor/speffect audited | PARTIAL | Skill 15 actor `u33` and speffect `7` have a dedicated audit. |
| `16` | `[1,133,545,150,2,30,0,-1,-1,0]` | `DIRECT_SIMPLE` | PORTED | Direct damage. |
| `17` | `[1,134,546,130,2,30,0,-1,40,0]` | `DIRECT_SIMPLE` + `DQ_HEAL_ATTACKER` | PARTIAL | Heal uses param `40`. |
| `18` | `[1,135,547,150,3,15,2,2,-1,0]` | `DIRECT_SIMPLE`, debuff/effect `2` | PARTIAL | Direct damage plus effect. |
| `19` | `[1,136,548,150,3,15,2,3,200,0]` | `DIRECT_SIMPLE`, debuff/effect `3` | PARTIAL | Direct damage plus effect. |
| `20` | `[2,137,549,100,0,45,0,-1,-1,0]` | `DIRECT_SIMPLE` | PORTED | Basic direct damage. |
| `21` | `[2,138,550,80,0,45,1,4,10,0]` | `DQ_SELF_BUFF`; `SOURCE_SWITCH_GAP` for damage side | PENDING | Has `power=80` but not visible in `game.b.b` switch; do not guess direct damage. |
| `22` | `[2,139,551,50,0,45,2,1,25,0]` | `DIRECT_EXPLICIT_DEBUFF_CHANCE`, debuff `1` chance `25` | PARTIAL | Direct damage plus debuff. |
| `23` | `[2,140,552,100,1,30,0,-1,250,0]` | `CONDITIONAL_IF_TARGET_DEBUFF1` | PORTED/PARTIAL | Stronger when target has debuff `1`. |
| `24` | `[2,141,553,0,1,10,1,13,-1,1]` | `DQ_DEFAULT_EFFECTMODE1`, buff/effect `13` | PARTIAL | Heal/clear debuff type via buff `13`. |
| `25` | `[2,142,554,0,1,10,1,14,-1,1]` | `DQ_DEFAULT_EFFECTMODE1`, buff/effect `14` | PARTIAL | Clear debuff / debuff immunity path. |
| `26` | `[2,143,555,150,2,30,0,-1,-1,0]` | `DIRECT_SIMPLE` | PORTED | Direct damage. |
| `27` | `[2,144,556,100,2,30,1,4,10,0]` | `DQ_SELF_BUFF`; `SOURCE_SWITCH_GAP` for damage side | PENDING | Has `power=100`, hardcoded self buff id `4`; direct damage control flow needs validation. |
| `28` | `[2,145,557,150,3,15,2,1,25,0]` | `DIRECT_EXPLICIT_DEBUFF_CHANCE`, debuff `1` chance `25` | PARTIAL | Direct damage plus debuff. |
| `29` | `[2,146,558,180,3,15,0,-1,300,0]` | `CONDITIONAL_IF_TARGET_DEBUFF1` | PORTED/PARTIAL | Stronger when target has debuff `1`. |
| `30` | `[3,147,559,100,0,45,0,-1,-1,0]` | `DIRECT_SIMPLE` | PORTED | Basic direct damage. |
| `31` | `[3,148,560,60,0,45,2,4,1,0]` | `DIRECT_SIMPLE`, debuff/effect `4` | PARTIAL | Debuff id `4` behavior incomplete. |
| `32` | `[3,149,561,60,0,45,2,5,10,0]` | `DIRECT_SIMPLE`, debuff `5` | PARTIAL | Speed-down behavior source-shaped. |
| `33` | `[3,150,562,100,1,30,2,6,10,0]` | `DIRECT_SIMPLE`, debuff `6` | PARTIAL | Damage reduction debuff. |
| `34` | `[3,151,563,0,1,10,1,5,-1,1]` | `DQ_DEFAULT_EFFECTMODE1`, buff/effect `5` | PARTIAL | Buff id `5` still partial. |
| `35` | `[3,152,564,0,1,10,1,6,-1,1]` | `DQ_DEFAULT_EFFECTMODE1`, buff/effect `6` | PARTIAL | Buff id `6` source quirk must be preserved. |
| `36` | `[3,153,565,150,2,30,0,-1,-1,0]` | `DIRECT_SIMPLE` | PORTED | Direct damage. |
| `37` | `[3,154,566,100,2,30,2,4,2,0]` | `DIRECT_SIMPLE`, debuff/effect `4` | PARTIAL | Debuff id `4` incomplete. |
| `38` | `[3,155,567,150,3,15,2,5,10,0]` | `DIRECT_SIMPLE`, debuff `5` | PARTIAL | Speed-down behavior source-shaped. |
| `39` | `[3,156,568,150,3,15,2,6,10,0]` | `DIRECT_SIMPLE`, debuff `6` | PARTIAL | Damage reduction debuff. |
| `40` | `[4,157,569,100,0,45,0,-1,-1,0]` | `DIRECT_SIMPLE` | PORTED | Basic direct damage. |
| `41` | `[4,158,570,90,0,45,2,10,10,0]` | `DIRECT_EXPLICIT_DEBUFF_CHANCE`, debuff `10` chance `10` | PARTIAL | Debuff `10` full tick/meaning pending. |
| `42` | `[4,159,571,90,0,45,1,7,5,0]` | `DQ_SELF_BUFF`; `SOURCE_SWITCH_GAP` for damage side | PENDING | Hardcoded self buff id `7`; damage side unclear. |
| `43` | `[4,160,572,100,1,30,0,-1,-1,0]` | `DIRECT_AND_CLEAR_TARGET_BUFFS` | PARTIAL | `target.D()` clear-buffs path. |
| `44` | `[4,161,573,0,1,10,1,8,-1,1]` | `DQ_DEFAULT_EFFECTMODE1`, buff/effect `8` | PARTIAL | PP/damage modifier buff. |
| `45` | `[4,162,574,0,1,10,1,9,-1,1]` | `DQ_DEFAULT_EFFECTMODE1`, buff/effect `9`; P7 speffect audited | PARTIAL | Special effect slice ported, full status behavior still partial. |
| `46` | `[4,163,575,150,2,30,0,-1,-1,0]` | `DIRECT_SIMPLE` | PORTED | Direct damage. |
| `47` | `[4,164,576,130,2,30,2,10,10,0]` | `DIRECT_EXPLICIT_DEBUFF_CHANCE`, debuff `10` chance `10` | PARTIAL | Debuff `10` pending. |
| `48` | `[4,165,577,130,3,15,1,7,5,0]` | `DQ_SELF_BUFF`; `SOURCE_SWITCH_GAP` for damage side | PENDING | Hardcoded self buff id `7`; damage side unclear. |
| `49` | `[4,166,578,180,3,15,0,-1,-1,0]` | `DIRECT_AND_CLEAR_TARGET_BUFFS` | PARTIAL | Direct damage plus target buff clear. |
| `50` | `[5,167,579,100,0,45,0,-1,-1,0]` | `DIRECT_SIMPLE` | PORTED | Basic direct damage. |
| `51` | `[5,168,580,80,0,45,2,7,20,0]` | `DIRECT_SIMPLE`, debuff `7` | PARTIAL | Defense-down behavior source-shaped. |
| `52` | `[5,169,581,80,0,45,0,-1,5,0]` | `DIRECT_SIMPLE` + `DQ_DAMAGE_LEECH_IF_AA` | PARTIAL | Conditional heal depends on battle flag `aa`. |
| `53` | `[5,170,582,200,1,30,0,-1,200,0]` | `HP_PERCENT_SCALING` | PARTIAL | Damage depends on attacker HP percent. |
| `54` | `[5,171,583,0,1,10,2,8,40,0]` | `DIRECT_SIMPLE`, debuff/effect `8`; low/min damage risk | PENDING | `power=0` but skill is in direct switch; exact clamp/effect behavior needs validation. |
| `55` | `[5,172,584,0,1,10,2,9,-1,0]` | `DIRECT_SIMPLE`, debuff/effect `9`; low/min damage risk | PENDING | `power=0` but skill is in direct switch. |
| `56` | `[5,173,585,150,2,30,0,-1,-1,0]` | `DIRECT_SIMPLE` | PORTED | Direct damage. |
| `57` | `[5,174,586,120,2,30,2,7,20,0]` | `DIRECT_SIMPLE`, debuff `7` | PARTIAL | Defense-down behavior source-shaped. |
| `58` | `[5,175,587,100,3,15,0,-1,8,0]` | `DIRECT_SIMPLE` + `DQ_DAMAGE_LEECH_IF_AA` | PARTIAL | Conditional heal depends on battle flag `aa`. |
| `59` | `[5,176,588,250,3,15,0,-1,250,0]` | `HP_PERCENT_SCALING` | PARTIAL | Damage depends on attacker HP percent. |
| `60` | `[6,177,589,100,0,45,0,-1,-1,0]` | `DIRECT_SIMPLE` | PORTED | Basic direct damage. |
| `61` | `[6,178,590,80,0,45,2,5,5,0]` | `DIRECT_SIMPLE`, debuff `5` | PARTIAL | Speed-down behavior source-shaped. |
| `62` | `[6,179,591,80,0,45,1,10,5,0]` | `DQ_SELF_BUFF`; `SOURCE_SWITCH_GAP` for damage side | PENDING | Hardcoded self buff id `10`; damage side unclear. |
| `63` | `[6,180,592,100,1,30,0,-1,5,0]` | `DIRECT_SIMPLE`, possible extra-turn/retry hook | PARTIAL | `game.d` checks skill `63/69` with `skill[8]` after resolve. |
| `64` | `[6,181,593,0,1,10,1,11,-1,0]` | `DQ_BUFF_WITH_SELECTED_INDEX`, buff `11` | PENDING | Copies other unit buffs; requires target-index parity. |
| `65` | `[6,182,594,0,1,10,1,12,-1,1]` | `DQ_DEFAULT_EFFECTMODE1`, buff/effect `12` | PARTIAL | PP conservation/extra-turn state. |
| `66` | `[6,183,595,150,2,30,0,-1,-1,0]` | `DIRECT_SIMPLE` | PORTED | Direct damage. |
| `67` | `[6,184,596,110,2,30,2,5,5,0]` | `SOURCE_SWITCH_GAP` | PENDING | Has power/effect but not visible in damaged switch list; do not port by row guess alone. |
| `68` | `[6,185,597,110,3,15,1,10,5,0]` | `DIRECT_SIMPLE` + `DQ_SELF_BUFF` | PARTIAL | Direct damage plus hardcoded self buff id `10`. |
| `69` | `[6,186,598,150,3,15,0,-1,8,0]` | `DIRECT_SIMPLE`, possible extra-turn/retry hook | PARTIAL | `game.d` checks skill `63/69` with `skill[8]` after resolve. |

## Status/Form Classification `aq.c[3][0..17]`

Rows are variable length. Known battle usage is mostly through `game.b.f(byte)`
checking `c[5]` form/status and through catch/EXP helper paths.

| Status id | Raw row | Source behavior class | Rebuild coverage | Notes |
| --- | --- | --- | --- | --- |
| `0` | `[213,1,237,5,1,30,100]` | Low-HP attack boost | PORTED/PARTIAL | If HP <= `[5]%` max HP, attack uses `[6]%` boost. |
| `1` | `[214,2,238,5,1,10]` | Attack boost | PORTED/PARTIAL | Attack uses `100 + [5]` percent. |
| `2` | `[215,3,239,5,1,15]` | Defense interaction | PORTED/PARTIAL | Target defense can be multiplied by `100 + [5]`. |
| `3` | `[216,4,240,5,1,20]` | Anti-debuff chance | PORTED/PARTIAL | Reduces debuff application chance. |
| `4` | `[217,5,241,5,1,10]` | Crit chance boost | PORTED/PARTIAL | Adds `[5]` to crit chance. |
| `5` | `[218,6,242,5,1,20]` | EXP boost | PARTIAL | `game.d.h(deadTarget)` multiplies EXP by `100 + [5]`. |
| `6` | `[219,7,243,5,1,100]` | Formula hook suspected by rebuild; source proof incomplete in current audit | PENDING | Rebuild has a hook, but source path needs re-check before claiming. |
| `7` | `[220,8,244,5,1]` | Unknown battle behavior | PENDING | No direct proven formula/tick path in this slice. |
| `8` | `[221,9,245,5,1,10,20]` | Post-hit self heal chance | PARTIAL | `game.d.q()` heals attacker by damage percent `[6]` with chance `[5]`. |
| `9` | `[222,10,246,5,1]` | Target routing/AI flag suspected | PENDING | Seen through skill target checks, not fully classified. |
| `10` | `[223,11,247,5,1,10]` | HP floor / survive threshold | PARTIAL | `game.d` prevents target HP below `[5]` in specific path. |
| `11` | `[224,12,248,5,1,20]` | Catch chance boost | PARTIAL | `game.d.b(itemId)` multiplies catch chance by `100 + [5]`. |
| `12` | `[225,13,249,1000,0]` | Unknown / likely non-core status UI or field effect | PENDING | No direct battle formula proof in current slice. |
| `13` | `[226,14,250,2,1]` | Unknown / likely non-core status UI or field effect | PENDING | Needs dedicated search. |
| `14` | `[227,15,251,10,1]` | Unknown / likely non-core status UI or field effect | PENDING | Needs dedicated search. |
| `15` | `[228,16,252,2,1]` | Unknown / likely non-core status UI or field effect | PENDING | Needs dedicated search. |
| `16` | `[229,17,253,10,1]` | Unknown / likely non-core status UI or field effect | PENDING | Needs dedicated search. |
| `17` | `[363,52,364,1,1]` | Unknown / late-added status row | PENDING | Needs dedicated search. |

## Self-Side Buff Classification `aq.c[6][0..14]`

Rows are `[nameText,descText,duration,paramA,paramB]`. Runtime slots are
`v[id][0..4]`.

| Buff id | Raw row | Source behavior class | Rebuild coverage | Notes |
| --- | --- | --- | --- | --- |
| `0` | `[333,348,2,30,190]` | Defense up plus delayed damage bonus | PARTIAL | `v[1]` from defense percent, `v[2]` from `paramB * B()/100`. |
| `1` | `[334,349,3,50,50]` | Defense down / outgoing damage boost | PARTIAL | Damage adds `damage * v[1][2] / 100`. |
| `2` | `[335,350,3,30,10]` | Defense up | PARTIAL | Mutates `d[3]`. |
| `3` | `[336,351,3,5,-1]` | Heal by max HP percent | PARTIAL | Immediate heal. |
| `4` | `[337,352,2,-1,-1]` | Skill-derived defense up | PARTIAL | Uses source skill `[8]`, stores skill id in `K[4]`. |
| `5` | `[338,353,3,30,-1]` | Damage reflect/store chance | PARTIAL | Target buff `5` stores damage into attacker's `K[5]` path. |
| `6` | `[339,354,3,50,-1]` | Damage conversion/reduction chance | PARTIAL | Source quirk: checks target active buff but reads attacker `v[6]`. |
| `7` | `[340,355,2,-1,-1]` | Skill-derived speed up | PARTIAL | Uses source skill `[8]`, stores skill id in `K[7]`. |
| `8` | `[341,356,4,30,-1]` | PP cost + outgoing damage boost | PARTIAL | Consumes extra PP and boosts final damage. |
| `9` | `[342,357,3,50,50]` | Speed up and defense down | PARTIAL | Mutates `d[4]` and `d[3]`. |
| `10` | `[343,358,2,-1,-1]` | Attack up | PARTIAL | Mutates `d[2]`. |
| `11` | `[344,359,3,-1,-1]` | Copy buffs from another unit | PENDING | Needs selected-index parity for skill `64`. |
| `12` | `[345,360,2,-1,-1]` | PP conservation / turn state | PARTIAL | Uses `K[12]`; affects skill PP and follow-up turn state. |
| `13` | `[346,361,3,20,-1]` | Heal and clear debuffs | PARTIAL | Calls debuff clear. |
| `14` | `[347,362,3,-1,-1]` | Clear debuffs / debuff immunity | PARTIAL | Blocks target debuff application while active. |

Note: `game.b.a(byte,int,int)` has a `case 15`, but `aq.c[6]` has rows
`0..14` only in the decoded table. Treat buff id `15` as decompile/control-flow
artifact or inaccessible until proven otherwise.

## Target-Side Debuff Classification `aq.c[7][0..10]`

Rows are `[nameText,descText,duration]`. Runtime slots are `w[id][0..4]`.

| Debuff id | Raw row | Source behavior class | Rebuild coverage | Notes |
| --- | --- | --- | --- | --- |
| `0` | `[311,322,3]` | Damage-over-time source value | PARTIAL | `q(0)` damages by `w[0][1] / sourceSkill[8]`; skills `1/7` apply it. |
| `1` | `[312,323,2]` | Conditional-damage marker | PARTIAL | Skills `23/29` do stronger damage when this is active. |
| `2` | `[313,324,3]` | Defense/form interaction | PARTIAL | Used in `B()`/status path, complete tick effect unclear. |
| `3` | `[314,325,3]` | Damage-over-time percent | PARTIAL | `q(3)` damages by `w[3][1] * sourceSkill[8] / 100` when duration condition hits. |
| `4` | `[315,326,3]` | Debuff id applied by skills `31/37`; behavior incomplete | PENDING | `w[4][1] = skill[8]`, but tick effect not proven. |
| `5` | `[316,327,3]` | Speed down | PARTIAL | Lowers target `d[4]` by base speed percent. |
| `6` | `[317,328,3]` | Incoming damage reduction/modifier | PARTIAL | Final damage subtracts `damage * w[6][1] / 100`. |
| `7` | `[318,329,3]` | Defense down | PARTIAL | Lowers target `d[3]` by base defense percent. |
| `8` | `[319,330,4]` | Debuff id applied by skill `54`; behavior incomplete | PENDING | Skill is in direct switch despite `power=0`; needs validation. |
| `9` | `[320,331,1]` | Debuff id applied by skill `55`; behavior incomplete | PENDING | Same caution as skill `55`. |
| `10` | `[321,332,4]` | Debuff id applied by skills `41/47`; behavior incomplete | PENDING | Also appears in status-related battle checks. |

## Passive / Global Hook Classification `aq.c[2][0..7]`

Rows are `[nameText,?,descA,descB,?,paramA,paramB]`; source uses mostly
`[5]` and `[6]`.

| Hook id | Raw row | Source behavior class | Rebuild coverage | Notes |
| --- | --- | --- | --- | --- |
| `0` | `[187,0,195,203,211,220,10]` | Post-battle HP restore | PARTIAL | If active, living player pets heal by species base HP * `[6]` / 100. |
| `1` | `[188,0,196,204,211,-1,5]` | Max HP passive | PARTIAL | `game.b.U()` increases max HP by `[6]%` when active/equipped. |
| `2` | `[189,0,197,205,211,-1,5]` | Defense passive | PARTIAL | `game.b.U()` increases defense by `[6]%` when active/equipped. |
| `3` | `[190,0,198,206,211,-1,2]` | Damage boost under `game.k.O == 2` | PARTIAL | Adds final damage percent `[5]`; row has `[5] == -1` in decoded data, source still references it. Needs validation. |
| `4` | `[191,0,199,207,211,5,2]` | Target defense/hit-rate hook | PARTIAL | `game.b.B()` and `game.d` P7 use `[5]`/`[6]`. |
| `5` | `[192,0,200,208,211,1,20]` | Post-turn stat growth hook | PENDING | `game.d` checks active hook and calls unit `y()` for player-side living units; indexing in decompile is suspicious and needs validation. |
| `6` | `[193,0,201,209,211,5,50]` | Damage boost and debuff duration halve | PARTIAL | `game.b.b(target)` uses `[5]`; also halves debuff duration when active/equipped. |
| `7` | `[194,0,202,210,212,100,-1]` | Off-party/backup EXP hook | PARTIAL | `game.d.h(deadTarget)` grants reduced EXP to inactive pets when active. |

## Rebuild Coverage Summary

| Area | Current status | Reason |
| --- | --- | --- |
| Direct damage families | PORTED/PARTIAL | `VqsvBattleUnit.computeDamage()` implements the main source switch shape. |
| Debuff slot write shape | PARTIAL | `maybeApplyTargetDebuff()` covers ids `0,3,4,5,6,7`; ids `8,9,10` still need behavior validation. |
| Buff slot behavior | PARTIAL | Structures exist, but not all `game.b.a(byte,int,int)` cases are fully wired into P7/turn flow. |
| Status/form ids `0..5,8,10,11` | PARTIAL | Important battle paths are identified; full row set not closed. |
| Status ids `6,7,9,12..17` | PENDING | Not enough source proof in this pass. |
| Skill ids with `SOURCE_SWITCH_GAP` | PENDING | `21,27,42,48,62,67` require bytecode/control-flow validation before full port. |
| Hardcoded `game.d.q()` post-effects | PARTIAL | Some are source-shaped; exact target routing and UI/effect timing still pending. |
| Passive hooks | PARTIAL | Formula hooks are identified; unlock/equip/save lifecycle remains outside this slice. |

## Must-Do Before Claiming Full Battle Data Model

1. Bytecode/control-flow validate `SOURCE_SWITCH_GAP` skills:
   `21,27,42,48,62,67`.
2. Close debuff ids `4,8,9,10` with exact tick/end behavior.
3. Close status ids `6,7,9,12..17` or prove they are non-battle.
4. Wire and test all buff ids `0..14` through real turn timing.
5. Build a deterministic smoke set covering at least one skill per behavior
   family listed above.

## Phase 1 Part 2 Follow-Up Conclusion

All rows in the battle-facing skill/status/buff/debuff/passive tables are now
classified at least to a source-backed behavior family or an explicit pending
bucket. The biggest remaining risk is not table parsing; it is damaged
control-flow around several skill ids and incomplete runtime coverage for
status/debuff tick behavior.
