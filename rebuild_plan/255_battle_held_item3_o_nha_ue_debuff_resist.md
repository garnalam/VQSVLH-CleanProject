# 255 - Battle Held Item 3 O Nha Ue Debuff Resist Closeout

Date: 2026-07-13

Scope: held item / pet-held passive id `3`.

## What It Is

`Ô Nha Uế` is:

```text
aq.c[3][3] pet-held property/passive
```

It is not a temporary battle buff/debuff and must not create a HUD status icon.

Source storage/check path:

```text
q.L inventory property list -> game.b.c[5] on the pet -> game.b.f((byte)3)
```

## Source Row

```text
aq.c[3][3] = [216,4,240,5,1,20]
```

| Field | Meaning |
| --- | --- |
| `[0] = 216` | source name text id |
| `[1] = 4` | held item icon cell |
| `[2] = 240` | source description text id |
| `[5] = 20` | debuff chance resistance percent |

## Source Logic

From `modules/source_code/decoded/decompiled_source_cfr/game/b.java`, method `b(b target)`:

```text
if target.f((byte)3):
    if ae.a(100) <= chance * (100 - aq.c[3][3][5]) / 100:
        apply debuff
    else:
        appliedDebuffId = -1
else:
    if chance != -1 && ae.a(100) > chance:
        appliedDebuffId = -1
    else:
        apply debuff
```

With row param `[5] = 20`:

```text
reducedChance = chance * 80 / 100
```

This changes only the chance to receive a debuff. It is not raw damage reduction and not a HUD status.

## Smoke Skill

The checkpoint uses source skill id `2`:

```text
aq.c[1][2] = [0,119,531,100,0,45,2,1,10,0]
```

Relevant fields:

| Field | Meaning |
| --- | --- |
| `[3] = 100` | damage power percent |
| `[7] = 1` | debuff id `1` |
| `[8] = 10` | debuff apply chance |

So:

```text
normal chance = 10
with Ô Nha Uế = 10 * 80 / 100 = 8
```

## Rebuild Mapping

| Source concept | Rebuild equivalent |
| --- | --- |
| held item/passive id | `BattleUnit.baseStats[STAT_FORM]` legacy field |
| source `game.b.f(byte)` | `BattleUnit.hasFormStatus(byte)` legacy method |
| source row wrapper | `VqsvBattleTables.heldItem(3)` / `BattleHeldItemRow` |
| debuff apply path | `BattleUnit.maybeApplyTargetDebuff()` |
| smoke setup | `SourceBattleRuntime.debugEnemyFormStatusForSmoke(s, 3)` |

Naming note: `STAT_FORM` and `hasFormStatus` are legacy names. Behavior is held item/passive.

## Smoke

Checkpoint:

```text
battle_held_item3_debuff_resist
```

Output:

```text
rebuild_game/build_intro_demo/battle_held_item3_debuff_resist.png
```

Smoke image rule:

```text
No audit overlay is drawn on the frame. The PNG is a clean battle frame.
```

Deterministic setup:

```text
skill = 2
debuff id = 1
normal chance = 10
forced debuff roll = 9

without held item:
    9 <= 10, debuff applies

with target held item #3:
    reducedChance = 10 * (100 - 20) / 100 = 8
    9 <= 8 is false, debuff is blocked
```

The checkpoint asserts:

- baseline target receives debuff id `1`;
- target with held item #3 does not receive debuff id `1`;
- forced debuff roll is exactly `9`;
- no enemy HUD status icon is created in the protected branch;
- trace contains `enemy held item/passive=3`;
- P7 damage frame still resolves normally.

## Verification

Passed:

- `build.ps1`
- `java -cp build/classes VqsvIntroDemo --smoke-checkpoint battle_held_item3_debuff_resist build_intro_demo/battle_held_item3_debuff_resist.png`
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
| Source row | PORTED | `aq.c[3][3]` params used |
| Formula | PORTED | `chance*(100-20)/100` |
| Deterministic smoke | PORTED | roll `9` applies without item, blocks with item |
| HUD status behavior | PORTED | no buff/debuff queue icon for held item itself |
| Petstate display | PORTED/PREVIOUS | widget 59/60 reads `aq.c[3]`; `Mang` row is text-only by user preference |
| Full equip flow from panel | PARTIAL/SEPARATE | outside this battle formula checkpoint |

## Next Roadmap Step

Recommended next held item/passive slice:

```text
id 8 - Hấp Huyết Đằng Mạn
after-hit heal chance + heal amount
```

Suggested deterministic smoke:

```text
force hit
force post-hit passive roll inside chance
assert attacker HP increases by damage * aq.c[3][8][6] / 100
```
