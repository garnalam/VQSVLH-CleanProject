# 243 - Battle Status Numeric Effect Audit

Date: 2026-07-13

Scope: source-first numeric audit for battle form/status, self buffs, and target
debuffs. This document answers the concrete question: "how much does it change
damage, HP, stats, chance, duration, or catch?"

No runtime code was changed by this audit.

## Sources

| Source | What was used |
| --- | --- |
| `modules/script/decoded/data__script__db.mid.json` | `aq.c[1]` skill rows, `aq.c[3]` form/status rows, `aq.c[6]` buff rows, `aq.c[7]` debuff rows |
| `modules/source_code/decoded/decompiled_source_cfr/game/b.java` | damage formula, form hooks, buff apply/tick, debuff apply/tick |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java` | P7 hit/miss, post-skill effects `q()`, catch chance `b(itemId)`, EXP hooks |
| `modules/source_code/decoded/bytecode_javap/game__b.javap.txt` | bytecode confirmation for source oddities such as buff10 |

## Important Rule

The numeric value does not always come from the same table.

| System | Storage | Duration source | Numeric param source |
| --- | --- | --- | --- |
| Form/status | `game.b.c[5]`, checked by `f(byte)` | row is not a battle turn counter | usually `aq.c[3][id][5+]` |
| Self buff | `game.b.v[id][0..4]` | `aq.c[6][id][2]` | `aq.c[6][id][3..4]`, except ids 4/7 use skill row param |
| Target debuff | `game.b.w[id][0..4]` | `aq.c[7][id][2]` | mostly skill row `aq.c[1][skillId][8]` or pre-skill raw damage |

## Form / Status `aq.c[3]`

These are passive/status rows on the pet itself. They do not use HUD queue icon
slots and do not use P12/P13 active queue visuals.

| Id | Raw row | Concrete source effect | Numeric meaning | Status |
| ---: | --- | --- | --- | --- |
| 0 | `[213,1,237,5,1,30,100]` | Low-HP attack hook in `game.b.B()` | If current HP `<= maxHP * 30 / 100`, raw attack part becomes `attack * 200 / 100 - targetDefense`. This is +100% attack before defense subtraction, not simply final damage +100%. | PORTED/PARTIAL |
| 1 | `[214,2,238,5,1,10]` | Attack hook in `game.b.B()` | Raw attack part becomes `attack * 110 / 100 - targetDefense`. | PORTED/PARTIAL |
| 2 | `[215,3,239,5,1,15]` | Defense hook on target in `game.b.B()` / `e(byte)` | Target defense becomes `defense * 115 / 100` for raw damage calculation. | PORTED/PARTIAL |
| 3 | `[216,4,240,5,1,20]` | Debuff resistance in `game.b.b(target)` | Debuff apply chance becomes `chance * (100 - 20) / 100`, so chance is reduced by 20%. Example: 25% becomes 20%. | PORTED/PARTIAL |
| 4 | `[217,5,241,5,1,10]` | Critical chance hook | Crit chance adds +10 percentage points. Source base is `5 + speed/2`, or 30 before speed if final visual element condition is true. | PORTED/PARTIAL |
| 5 | `[218,6,242,5,1,20]` | EXP hook in `game.d` EXP award | EXP award becomes `award * 120 / 100`. | PORTED/PARTIAL |
| 6 | `[219,7,243,5,1,100]` | Reserve EXP/share hook | Source text/row says 100% corresponding EXP for reserve item/status path. Exact producer/save lifecycle is separate EXP roadmap work. | PORTED/PARTIAL |
| 7 | `[220,8,244,5,1]` | Battle-start text row | No direct numeric battle consumer proven in the source slice audited. | UNKNOWN/PENDING |
| 8 | `[221,9,245,5,1,10,20]` | Post-hit self heal in `game.d.q()` | After a hit, roll `<= 10`; if passed, attacker heals `damage * 20 / 100`. | PORTED/PARTIAL |
| 9 | `[222,10,246,5,1]` | No-miss hook in P7 miss flow | Miss chance is forced to `0`. | PORTED |
| 10 | `[223,11,247,5,1,10]` | HP floor hook around P7/U() | If source condition is active and target HP drops to `<= 10`, HP is set back to `10`. Source wording is capture-related; exact owner semantics remain source-shaped. | PORTED/PARTIAL |
| 11 | `[224,12,248,5,1,20]` | Catch chance hook in `game.d.b(itemId)` | Catch chance is multiplied by `(100 + 20) / 100`, so +20%. | PORTED/PARTIAL |
| 12 | `[225,13,249,1000,0]` | Evolution/material row | Not a battle status effect. | NON_BATTLE |
| 13 | `[226,14,250,2,1]` | Evolution/material row | Not a battle status effect. | NON_BATTLE |
| 14 | `[227,15,251,10,1]` | Evolution/material row | Not a battle status effect. | NON_BATTLE |
| 15 | `[228,16,252,2,1]` | Mutation/material row | Not a battle status effect. | NON_BATTLE |
| 16 | `[229,17,253,10,1]` | Mutation/material row | Not a battle status effect. | NON_BATTLE |
| 17 | `[363,52,364,1,1]` | Key/material row | Not a battle status effect. | NON_BATTLE |

## Self Buff `aq.c[6]`

All active buff ids use HUD icon cell `buffId + 12`; duration cell is
`134 + v[id][0]`. Only buff ids `3,5,13` get source P12/P13 body visuals.

| Id | Raw row | Duration | Stored values / formula | Concrete source effect | Status |
| ---: | --- | ---: | --- | --- | --- |
| 0 | `[333,348,2,30,190]` | 2 turns | `v[0][1] = baseDefense * 30 / 100`; `v[0][2] = 190 * B() / 100` | Defense +30%. When source delayed condition `v[0][0] == 0` is reached, outgoing damage adds stored `v[0][2]` equal to 190% of source `B()` at apply time. | PORTED/PARTIAL |
| 1 | `[334,349,3,50,50]` | 3 turns | `v[1][1] = baseDefense * 50 / 100`; `v[1][2] = 50` | Own defense -50%. Outgoing damage then adds `damage * 50 / 100`, so final damage +50% before later relation/jitter. | PORTED |
| 2 | `[335,350,3,30,10]` | 3 turns | `v[2][1] = baseDefense * 30 / 100`; `v[2][2] = 10` | Own defense +30%. When target has buff2, attacker receives reflect/counter damage `hitDamage * 10 / 100` in post-hit flow. | PORTED/PARTIAL |
| 3 | `[336,351,3,5,-1]` | 3 turns | `v[3][1] = maxHP * 5 / 100` | Heal 5% max HP on apply and each P13 tick while active. | PORTED |
| 4 | `[337,352,2,-1,-1]` | 2 turns | `v[4][1] = baseDefense * skill[8] / 100` | Defense up uses producer skill param, not row `-1`. Current producers: skill 21/27 use `skill[8] = 10`, so defense +10%. | PORTED/PARTIAL |
| 5 | `[338,353,3,30,-1]` | 3 turns | `v[5][1] = 30` | When this unit is hit, 30% chance stores incoming damage into `K[5]`; post-hit flow damages attacker back by that stored damage. | PORTED/PARTIAL |
| 6 | `[339,354,3,50,-1]` | 3 turns | `v[6][1] = 50`; `v[6][2] = -1` | Source code has an odd target/owner check: if target has buff6 and roll `<= 50`, damage becomes `damage * (-1) / 100` through stored param. This is source-odd and should not be "fixed" without deeper proof. | SOURCE_ODDITY/PARTIAL |
| 7 | `[340,355,2,-1,-1]` | 2 turns | `v[7][1] = baseSpeed * skill[8] / 100` | Speed up uses producer skill param. Current producers skill 42/48 use `skill[8] = 5`, so speed +5%. | PORTED |
| 8 | `[341,356,4,30,-1]` | 4 turns | `v[8][1] = 30` | Outgoing damage adds `damage * 30 / 100`, so +30%. Source text also mentions extra skill cost; PP cost parity is separate. | PORTED/PARTIAL |
| 9 | `[342,357,3,50,50]` | 3 turns | `v[9][1] = baseSpeed * 50 / 100`; `v[9][2] = baseDefense * 50 / 100` | Speed +50%, defense -50%. | PORTED/PARTIAL |
| 10 | `[343,358,2,-1,-1]` | 2 turns | `v[10][1] = baseAttack * (-1) / 100` | Source row and bytecode make this roughly attack -1%, despite text saying strength up. See audit `242`; keep as source oddity. | PORTED-AS-SOURCE / SOURCE_ODDITY |
| 11 | `[344,359,3,-1,-1]` | 3 turns | `v[11][1] = selected donor index` | Copies active buffs from selected donor, then clears donor buffs. Numeric values are copied from donor `v[id][1]` and related slots. | PORTED/PARTIAL |
| 12 | `[345,360,2,-1,-1]` | 2 turns | `K[12] = 1` on apply, `K[12] = 2` on tick | Follow-up/second action hook. It does not directly change HP/stat. | PORTED/PARTIAL |
| 13 | `[346,361,3,20,-1]` | 3 turns | `v[13][1] = maxHP * 20 / 100` | Clears debuffs and heals 20% max HP on apply/tick. | PORTED |
| 14 | `[347,362,3,-1,-1]` | 3 turns | no numeric value | Clears debuffs and blocks incoming abnormal/debuff application while active. | PORTED/PARTIAL |
| 15 | no `aq.c[6][15]` row | n/a | source `game.b` has a case 15, but decoded buff table only has ids 0..14 | Do not implement as real buff until a producer/source row is found. | UNKNOWN/PENDING |

## Buff Producers From Skill Table `aq.c[1]`

For buff ids 4 and 7, this table is especially important because their numeric
effect comes from `skill[8]`, not from `aq.c[6][buffId][3]`.

| Buff id | Producer skill ids | Producer params |
| ---: | --- | --- |
| 0 | 4 | `skill[8] = -1`; row buff supplies 30/190 |
| 1 | 5 | `skill[8] = -1`; row buff supplies 50/50 |
| 2 | 14 | `skill[8] = -1`; row buff supplies 30/10 |
| 3 | 15 | `skill[8] = -1`; row buff supplies 5% HP |
| 4 | 21, 27 | `skill[8] = 10`; defense +10% |
| 5 | 34 | `skill[8] = -1`; row buff supplies 30% chance |
| 6 | 35 | `skill[8] = -1`; row buff supplies 50/-1 source oddity |
| 7 | 42, 48 | `skill[8] = 5`; speed +5% |
| 8 | 44 | `skill[8] = -1`; row buff supplies +30% damage |
| 9 | 45 | `skill[8] = -1`; row buff supplies speed +50%, defense -50% |
| 10 | 62, 68 | `skill[8] = 5`, but source ignores it for buff10; row `-1` is used |
| 11 | 64 | selected index / donor slot |
| 12 | 65 | no numeric stat; follow-up flag |
| 13 | 24 | row buff supplies 20% max HP heal |
| 14 | 25 | no numeric stat; clear/block abnormal |

## Target Debuff `aq.c[7]`

All active debuff ids use HUD icon cell `debuffId + 1`; duration cell is
`134 + w[id][0]`. Only debuff ids `0,1,2,3,8,9,10` get source P12/P13 body
visuals.

| Id | Raw row | Duration | Stored values / formula | Concrete source effect | Status |
| ---: | --- | ---: | --- | --- | --- |
| 0 | `[311,322,3]` | 3 turns | `w[0][1] = preSkillRawDamage`; source skill param is divisor | P12 tick damage is `max(1, storedRaw / skill[8])`. Producers: skill 1 divisor 4, skill 7 divisor 3. | PORTED |
| 1 | `[312,323,2]` | 2 turns | no stored numeric value in apply switch | Flag used by conditional skill family. If target has debuff1, skills 23/29 use `raw * skill[8] / 100`; catch formula also gives status multiplier index 1 = `11/10`. | PORTED/PARTIAL |
| 2 | `[313,324,3]` | 3 turns | no stored numeric value in apply switch | Command-disable/lock style flag. Catch formula gives status multiplier index 2 = `12/10`. | PORTED/PARTIAL |
| 3 | `[314,325,3]` | 3 turns | `w[3][1] = preSkillRawDamage`; source skill param is percent | Near expiry, damage is `max(1, storedRaw * skill[8] / 100)`. Producers: skill 13 uses 150%, skill 19 uses 200%. | PORTED |
| 4 | `[315,326,3]` | 3 turns | `w[4][1] = skill[8]` | Miss/evasion formula lowers effective attacker speed by this flat value. Producers: skill 31 stores 1, skill 37 stores 2. | PORTED/PARTIAL |
| 5 | `[316,327,3]` | 3 turns | `w[5][1] = baseSpeed * skill[8] / 100` | Speed down. Producers: skill 32/38 store 10% base speed; skill 61/67 store 5% base speed. | PORTED |
| 6 | `[317,328,3]` | 3 turns | `w[6][1] = skill[8]` | Outgoing damage is reduced by `damage * w[6][1] / 100`. Producers skill 33/39 store 10%, so damage -10%. | PORTED/PARTIAL |
| 7 | `[318,329,3]` | 3 turns | `w[7][1] = baseDefense * skill[8] / 100` | Defense down. Producers skill 51/57 store 20% base defense, so target defense -20%. | PORTED |
| 8 | `[319,330,4]` | 4 turns | no numeric value proven | Flag/status family from zero-power skill 54 with `skill[8] = 40`; full behavior still source-route sensitive. | FLAG/PENDING |
| 9 | `[320,331,1]` | 1 turn | no numeric value proven | Switch-lock/confusion-style flag from skill 55. Full command-disable behavior is only partially covered. | FLAG/PENDING |
| 10 | `[321,332,4]` | 4 turns | no stored numeric value in apply switch | Paralysis/catch/action-family flag. Catch formula gives status multiplier index 3 = `12/10`; producers skill 41/47 have `skill[8] = 10` for apply chance family. | PORTED/PARTIAL |

## Debuff Producers From Skill Table `aq.c[1]`

| Debuff id | Producer skill ids | Producer params |
| ---: | --- | --- |
| 0 | 1, 7 | skill 1 divisor `4`; skill 7 divisor `3` |
| 1 | 2, 8, 22, 28 | params `10`, `20`, `25`, `25`; for conditional skills 23/29 this flag changes damage branch |
| 2 | 12, 18 | param `-1`; flag behavior |
| 3 | 13, 19 | delayed damage percent `150%`, `200%` |
| 4 | 31, 37 | flat miss-speed penalty `1`, `2` |
| 5 | 32, 38, 61, 67 | speed down `10%`, `10%`, `5%`, `5%` of base speed |
| 6 | 33, 39 | outgoing damage down `10%` |
| 7 | 51, 57 | defense down `20%` of base defense |
| 8 | 54 | param `40`; full consumer still pending |
| 9 | 55 | param `-1`; full consumer still pending |
| 10 | 41, 47 | param `10`; catch/status multiplier flag |

## Catch Numeric Status Multipliers

Source: `game.d.b(itemId)`.

Status index is chosen in this order:

1. target has debuff1 -> index 1;
2. target has debuff2 -> index 2;
3. target has debuff10 -> index 3;
4. player has form/status11 -> index 4.

Then catch chance is multiplied by:

```text
index 0: 10/10 = 1.0
index 1: 11/10 = 1.1
index 2: 12/10 = 1.2
index 3: 12/10 = 1.2
index 4: 12/10 = 1.2
```

Low enemy HP also changes base catch:

```text
HP <= 15% max: base 85
HP <= 50% max: base 45
HP <= 100% max: base 20
```

Ball item multiplier then applies through `aq.c[4][itemId][6] / 100`.

## Source Oddities To Keep Honest

| Item | Why it is odd |
| --- | --- |
| buff6 | Row says `[339,354,3,50,-1]`; source damage branch can multiply damage by `-1 / 100` through stored param. Needs deeper bytecode/pixel/gameplay observation before any design fix. |
| buff10 | Row says `[343,358,2,-1,-1]`; source bytecode uses `-1` as attack percent. Audit `242` proves this is not a decode error. |
| form10 | Source sets HP floor to 10 in specific P7/U() paths; owner semantics are source-shaped but still worth smoke coverage. |
| debuff8/debuff9 | Rows and producers are known, but full command/behavior consumers are not completely generalized yet. |

## Next Work

Before adding more status smoke, use this numeric table as the expectation
source. The next safe slice is still:

1. form0 low-HP attack boost smoke using exact `30% HP` and `+100% attack`;
2. form4 crit chance smoke using exact `+10` crit chance;
3. form10 HP floor smoke using exact floor `10 HP`;
4. buff13 cleanse/heal using exact `20% max HP`;
5. debuff3 delayed damage using exact `150%/200% stored raw`;
6. debuff2 command-disable using exact duration `3`.
