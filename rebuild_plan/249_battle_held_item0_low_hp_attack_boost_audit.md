# 249 - Battle Held Item 0 Low HP Attack Boost Audit

Date: 2026-07-13

Scope: re-audit `battle_status_form0_low_hp_attack_boost` after the `aq.c[3]` taxonomy fix.

## What It Is

`Mạn Đà La Thạch` is not a Pokemon, not a skill, and not a temporary battle status.

It is:

```text
aq.c[3][0] pet-held property/passive
```

Best user-facing name:

```text
held item / pet-held passive
```

Source storage:

```text
q.L inventory property list -> game.b.c[5] on the pet
```

Source check:

```java
game.b.f((byte)0)
```

## Source Row

```text
aq.c[3][0] = [213,1,237,5,1,30,100]
```

| Field | Meaning for this audit |
| --- | --- |
| `[0] = 213` | source name text id |
| `[1] = 1` | held item icon cell |
| `[2] = 237` | source description text id |
| `[5] = 30` | HP threshold percent |
| `[6] = 100` | attack increase percent |

## Source Logic

From `modules/source_code/decoded/decompiled_source_cfr/game/b.java`, method `B()`:

```text
raw = attackerAttack - targetDefense

if attacker.f(0):
    if attackerCurrentHp <= aq.c[3][0][5] * attackerMaxHp / 100:
        raw = attackerAttack * (100 + aq.c[3][0][6]) / 100 - targetDefense
```

So with source row `[5]=30`, `[6]=100`:

```text
if HP <= 30% max HP:
    raw = attack * 200 / 100 - targetDefense
```

This is not final damage x2. The attack stat is doubled before subtracting defense.

## Rebuild Definition

Current rebuild field names are legacy from earlier docs:

| Source concept | Current rebuild |
| --- | --- |
| held item/passive id | `BattleUnit.baseStats[STAT_FORM]` |
| source `f(byte)` check | `BattleUnit.hasFormStatus(byte)` |
| source `B()` raw attack | `BattleUnit.baseAttack()` |
| damage entry | `BattleUnit.computeDamage()` |

The naming is imperfect, but behavior maps to source.

## Smoke Demo Requirement

The checkpoint must show/verify all of these:

1. The active player pet has held item/passive id `0`.
2. The held item is represented by source row `aq.c[3][0]`, name `Mạn Đà La Thạch`, icon cell `1`.
3. At high HP, the held item is present but not triggered.
4. At low HP (`<= 30%`), the held item triggers.
5. The damage formula changes from:

```text
120 - 40 = 80
```

to:

```text
120 * 200 / 100 - 40 = 200
```

6. No HUD buff/debuff status icon should appear, because this is a held item/passive, not active queue status.

## Current Smoke

Checkpoint:

```text
battle_status_form0_low_hp_attack_boost
```

Output PNG:

```text
rebuild_game/build_intro_demo/status_effectiveness_battle_status_form0_low_hp_attack_boost.png
```

The smoke now overlays a small visual audit panel on top of the battle frame:

- held item name and icon;
- source row;
- application path `q.L -> c[5]=0`;
- trigger threshold;
- baseline damage;
- active damage.

Latest verified PNG:

```text
rebuild_game/build_intro_demo/status_effectiveness_battle_status_form0_low_hp_attack_boost.png
```

Latest verified console result:

```text
battleState=P7
battleHp=30/134:109/109
battleLog=player deals 200 damage
```

## Status

| Area | Status | Note |
| --- | --- | --- |
| Source row | PORTED | `aq.c[3][0]` params used |
| Formula | PORTED | `attack*200/100-defense` under low HP |
| Damage smoke | PORTED | high HP vs low HP deterministic comparison |
| Visual smoke explanation | PORTED | overlay added to existing PNG |
| HUD status behavior | PORTED | no buff/debuff queue icon expected |
| Full user equip flow | PARTIAL/SEPARATE | `q.L -> Mang theo -> c[5]` belongs to panel/equipment flow, not this battle formula checkpoint |

## Verification 2026-07-13

Passed:

- `build.ps1`
- `java -cp build/classes VqsvIntroDemo --smoke-checkpoint battle_status_form0_low_hp_attack_boost build_intro_demo/status_effectiveness_battle_status_form0_low_hp_attack_boost.png`
- `java -cp build/classes com.vqsv.rebuild.Main --check`
- `java -cp build/classes VqsvBattleDamageFormulaCheck`
- Java mojibake scan. Hits were existing valid Vietnamese intro text only.

## Next Step

After this checkpoint is accepted visually, continue with held item/passive id `4`
`Viễn Cổ Long Cốt`:

```text
crit chance += 10 percentage points
```
