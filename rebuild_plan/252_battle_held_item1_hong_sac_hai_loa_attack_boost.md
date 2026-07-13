# 252 - Battle Held Item 1 Hong Sac Hai Loa Attack Boost Closeout

Date: 2026-07-13

Scope: held item / pet-held passive id `1`, after the `aq.c[3]` taxonomy fix.

## What It Is

`Hồng Sắc Hải Loa` is:

```text
aq.c[3][1] pet-held property/passive
```

It is not a temporary battle buff/debuff and must not create a HUD status icon.

Source storage/check path:

```text
q.L inventory property list -> game.b.c[5] on the pet -> game.b.f((byte)1)
```

## Source Row

```text
aq.c[3][1] = [214,2,238,5,1,10]
```

| Field | Meaning |
| --- | --- |
| `[0] = 214` | source name text id |
| `[1] = 2` | held item icon cell |
| `[2] = 238` | source description text id |
| `[5] = 10` | attack increase percent |

## Source Logic

From `modules/source_code/decoded/decompiled_source_cfr/game/b.java`, method `B()`:

```text
raw = attackerAttack - targetDefense

else if attacker.f(1):
    raw = attackerAttack * (100 + aq.c[3][1][5]) / 100 - targetDefense
```

With row param `[5] = 10`:

```text
raw = attack * 110 / 100 - defense
```

This is not final damage +10%. The attack stat is boosted before subtracting defense.

## Rebuild Mapping

| Source concept | Rebuild equivalent |
| --- | --- |
| held item/passive id | `BattleUnit.baseStats[STAT_FORM]` legacy field |
| source `game.b.f(byte)` | `BattleUnit.hasFormStatus(byte)` legacy method |
| source row wrapper | `VqsvBattleTables.heldItem(1)` / `BattleHeldItemRow` |
| raw attack path | `BattleUnit.baseAttack()` |
| smoke setup | `SourceBattleRuntime.debugPlayerFormStatusForSmoke(s, 1)` |

Naming note: `STAT_FORM` and `hasFormStatus` are legacy names. Behavior now treats these ids as held item/passive ids.

## Code Changes

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleUnit.java` | Held item branches `0/1/2/3/4` in damage/crit/debuff source paths now read params through `BattleHeldItemRow` instead of `BattleStatusRow`. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added checkpoint `battle_held_item1_attack_boost` and visual audit overlay. |

## Smoke

Checkpoint:

```text
battle_held_item1_attack_boost
```

Output:

```text
rebuild_game/build_intro_demo/battle_held_item1_attack_boost.png
```

Deterministic setup:

```text
attack = 120
defense = 40
baseline = 120 - 40 = 80
with held item #1 = 120 * 110 / 100 - 40 = 92
```

The checkpoint asserts:

- baseline damage is exactly `80`;
- held item #1 damage is exactly `92`;
- no player HUD status icon is created;
- battle trace contains `player held item/passive=1`;
- battle reaches the P7 damage frame.

## Verification

Passed:

- `build.ps1`
- `java -cp build/classes VqsvIntroDemo --smoke-checkpoint battle_held_item1_attack_boost build_intro_demo/battle_held_item1_attack_boost.png`
- `java -cp build/classes com.vqsv.rebuild.Main --check`
- `java -cp build/classes VqsvBattleDamageFormulaCheck`
- Java mojibake scan. Hits were existing valid Vietnamese intro text only.
- route smoke regression:
  - `route_sophie_after_battle_branch`
  - `route_bunny_after_battle_task`
  - `route_elder_after_battle_reward_state`

## Status

| Area | Status | Note |
| --- | --- | --- |
| Source row | PORTED | `aq.c[3][1]` params used |
| Formula | PORTED | `attack*110/100-defense` |
| Damage smoke | PORTED | deterministic 80 -> 92 |
| HUD status behavior | PORTED | no buff/debuff queue icon |
| Petstate display | PORTED/PREVIOUS | widget 59/60 already reads `aq.c[3]`; `Mang` row is text-only by user preference |
| Full equip flow from panel | PARTIAL/SEPARATE | kept outside this formula slice |

## Next Roadmap Step

Next held item/passive slice:

```text
id 4 - Viễn Cổ Long Cốt
crit chance += 10 percentage points
```

Required work:

1. Audit source `game.b` crit chance branch for `game.b.f((byte)4)`.
2. Add deterministic smoke that proves the +10 percentage-point crit window.
3. Keep it as held item/passive, not HUD buff/debuff.
