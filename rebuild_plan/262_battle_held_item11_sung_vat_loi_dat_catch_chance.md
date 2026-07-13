# 262 - Battle Held Item 11 Sủng vật lôi đạt Catch Chance Closeout

Date: 2026-07-13

Scope: held item / pet-held passive id `11`.

## What It Is

`Sủng vật lôi đạt` is a pet-held passive from `aq.c[3]`.

It affects catch chance. It is not a temporary buff/debuff/status icon and must not create HUD status slots.

Source storage/check path:

```text
q.L inventory property list -> game.b.c[5] on the pet -> game.b.f((byte)11)
```

## Source Row

```text
aq.c[3][11] = [224,12,248,5,1,20]
```

| Field | Meaning |
| --- | --- |
| `[0] = 224` | source name text id |
| `[1] = 12` | held item icon cell |
| `[2] = 248` | source description text id |
| `[5] = 20` | catch chance bonus percent |

## Source Evidence

Source file:

```text
modules/source_code/decoded/decompiled_source_cfr/game/d.java
```

Method:

```text
game.d.b(int itemId)
```

Source shape:

```text
if (target.m(1)) statusIndex = 1;
if (target.m(2)) statusIndex = 2;
if (target.m(10)) statusIndex = 3;
if (attacker.f((byte)11)) statusIndex = 4;

chance = hpBucket * ballCatchParam / 100;
chance = chance * qualityMultiplier / 100;
chance = chance * [10, 11, 12, 12, 12][statusIndex] / 10;

if (attacker.f((byte)11)) {
    chance = chance * (100 + aq.c[3][11][5]) / 100;
}
```

Important:

```text
Held item 11 applies twice in the source-shaped formula:
1. statusIndex becomes 4, so multiplier is 12/10.
2. then chance is multiplied by (100 + 20) / 100.
```

So it is not only a simple final `+20%` in the bytecode shape.

## Rebuild Mapping

| Source concept | Rebuild equivalent |
| --- | --- |
| `game.b.f((byte)11)` | `BattleUnit.hasSourceHeldItem(11)` |
| `aq.c[3][11][5]` | `BattleUnit.sourceHeldItemParam(11, 5, 0)` |
| `game.d.b(int itemId)` | `SourceBattleRuntime.catchChance(int itemId)` |
| P21 chance display | `VqsvChoiceUiView` value column in catch list |

## Code Changes

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Reclassified catch chance hook from legacy form/status 11 to held item 11 and reads `sourceHeldItemParam(11,5,0)`. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added checkpoint `battle_held_item11_catch_chance`, included it in `battle_quick`, and updated smoke trace naming to `attackerHeldItem11`. |

## Smoke

Checkpoint:

```text
battle_held_item11_catch_chance
```

Output:

```text
rebuild_game/build_intro_demo/battle_held_item11_catch_chance.png
```

Deterministic setup:

```text
enemy: species 68, level 5, full HP
catch item: item 1 Phong ấn cầu
attacker held item: id 11 in equipped branch
```

Expected source-shaped values:

```text
base chance:
    22%

with held item 11:
    22 * 12 / 10 = 26
    26 * (100 + 20) / 100 = 31
```

The checkpoint asserts:

- base chance for item 1 is `22%`;
- held item 11 chance is `31%`;
- no player status icon is created;
- trace contains `attackerHeldItem11=true`;
- P21 catch list renders the held-item chance.

Smoke image rule:

```text
No audit overlay is drawn. The PNG is a clean P21 catch list frame.
```

## Verification

Passed:

- `build.ps1`
- `java -cp build/classes VqsvIntroDemo --smoke-checkpoint battle_held_item11_catch_chance build_intro_demo/battle_held_item11_catch_chance.png`
- `java -cp build/classes VqsvIntroDemo --smoke-suite battle_quick build_intro_demo/suite_battle_quick`
- `java -cp build/classes com.vqsv.rebuild.Main --check`
- `java -cp build/classes VqsvBattleDamageFormulaCheck`
- Java mojibake scan with strict pattern `Ã|Â|�|\?i\?|Th\?`

`battle_quick` now contains `39` checkpoints and passed `39/39`.

## Status

| Area | Status | Note |
| --- | --- | --- |
| Source row | PORTED | `aq.c[3][11] = [224,12,248,5,1,20]` |
| Source consumer | PORTED | `game.d.b(int itemId)` catch chance |
| Catch chance formula | PORTED | `statusIndex=4` multiplier plus `[5]=20` bonus |
| P21 UI value | PORTED | catch list shows updated chance |
| HUD status behavior | PORTED | no buff/debuff/status icon |
| Full equip/save flow | PARTIAL/SEPARATE | outside this battle runtime checkpoint |

## Next Roadmap Step

The battle-relevant held items `0..11` now have dedicated source-backed coverage.

Recommended next slice:

```text
Held item closeout matrix for ids 0..11
```

Goal:

- mark each id `PORTED/PARTIAL/PENDING`;
- list checkpoint name for each id;
- separate battle-held passives from non-battle materials `12..17`;
- decide whether to rename the remaining legacy checkpoint `battle_status_form0_low_hp_attack_boost` to `battle_held_item0_low_hp_attack_boost`.
