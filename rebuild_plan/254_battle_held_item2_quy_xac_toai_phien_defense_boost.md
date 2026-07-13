# 254 - Battle Held Item 2 Quy Xac Toai Phien Defense Boost Closeout

Date: 2026-07-13

Scope: held item / pet-held passive id `2`.

## What It Is

`Quy Xác Toái Phiến` is:

```text
aq.c[3][2] pet-held property/passive
```

It is not a temporary battle buff/debuff and must not create a HUD status icon.

Source storage/check path:

```text
q.L inventory property list -> game.b.c[5] on the pet -> game.b.f((byte)2)
```

## Source Row

```text
aq.c[3][2] = [215,3,239,5,1,15]
```

| Field | Meaning |
| --- | --- |
| `[0] = 215` | source name text id |
| `[1] = 3` | held item icon cell |
| `[2] = 239` | source description text id |
| `[5] = 15` | defense increase percent |

## Source Logic

From `modules/source_code/decoded/decompiled_source_cfr/game/b.java`, method `B()`:

```text
if target.f((byte)2):
    raw = attackerAttack - targetDefense * (100 + aq.c[3][2][5]) / 100
else:
    raw = attackerAttack - targetDefense
```

From `game.b.e(byte)`, display/stat query for defense:

```text
if self.f((byte)2):
    shownDefense = currentDefense * (100 + aq.c[3][2][5]) / 100
```

With row param `[5] = 15`:

```text
effectiveDefense = defense * 115 / 100
```

## Rebuild Mapping

| Source concept | Rebuild equivalent |
| --- | --- |
| held item/passive id | `BattleUnit.baseStats[STAT_FORM]` legacy field |
| source `game.b.f(byte)` | `BattleUnit.hasFormStatus(byte)` legacy method |
| source row wrapper | `VqsvBattleTables.heldItem(2)` / `BattleHeldItemRow` |
| raw attack path | `BattleUnit.baseAttack()` |
| smoke setup | `SourceBattleRuntime.debugEnemyFormStatusForSmoke(s, 2)` |

Naming note: `STAT_FORM` and `hasFormStatus` are legacy names. Behavior is held item/passive.

## Smoke

Checkpoint:

```text
battle_held_item2_defense_boost
```

Output:

```text
rebuild_game/build_intro_demo/battle_held_item2_defense_boost.png
```

Smoke image rule:

```text
No audit overlay is drawn on the frame. The PNG is a clean battle frame.
```

Deterministic setup:

```text
attacker attack = 120
target defense = 40

without held item:
    raw = 120 - 40 = 80

with target held item #2:
    effectiveDefense = 40 * 115 / 100 = 46
    raw = 120 - 46 = 74
```

The checkpoint asserts:

- baseline damage is exactly `80`;
- target held item #2 damage is exactly `74`;
- no enemy HUD status icon is created;
- battle trace contains `enemy held item/passive=2`;
- battle reaches P7 damage frame and hit is true.

## Verification

Passed:

- `build.ps1`
- `java -cp build/classes VqsvIntroDemo --smoke-checkpoint battle_held_item2_defense_boost build_intro_demo/battle_held_item2_defense_boost.png`
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
| Source row | PORTED | `aq.c[3][2]` params used |
| Formula | PORTED | `defense*115/100` before subtracting from attack |
| Damage smoke | PORTED | deterministic `80 -> 74` |
| HUD status behavior | PORTED | no buff/debuff queue icon |
| Petstate display | PORTED/PREVIOUS | widget 59/60 reads `aq.c[3]`; `Mang` row is text-only by user preference |
| Full equip flow from panel | PARTIAL/SEPARATE | outside this battle formula checkpoint |

## Next Roadmap Step

Recommended next held item/passive slice:

```text
id 3 - Ô Nha Uế
debuff apply chance = chance * (100 - 20) / 100
```

This one should be audited with deterministic debuff rolls because it affects status application probability, not raw damage.
