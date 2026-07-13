# 253 - Battle Held Item 4 Vien Co Long Cot Crit Chance Closeout

Date: 2026-07-13

Scope: held item / pet-held passive id `4`.

## What It Is

`Viễn Cổ Long Cốt` is:

```text
aq.c[3][4] pet-held property/passive
```

It is not a temporary battle buff/debuff and must not create a HUD status icon.

Source storage/check path:

```text
q.L inventory property list -> game.b.c[5] on the pet -> game.b.f((byte)4)
```

## Source Row

```text
aq.c[3][4] = [217,5,241,5,1,10]
```

| Field | Meaning |
| --- | --- |
| `[0] = 217` | source name text id |
| `[1] = 5` | held item icon cell |
| `[2] = 241` | source description text id |
| `[5] = 10` | crit chance bonus in percentage points |

## Source Logic

From `modules/source_code/decoded/decompiled_source_cfr/game/b.java`, method `b(b target)`:

```text
critChance = 5
if final visual element condition:
    critChance = 30
critChance += speed / 2

if attacker.f((byte)4):
    critChance += aq.c[3][4][5]

if ae.a(100) <= critChance:
    raw = raw * 3 / 2
    critFlag = 1
```

With row param `[5] = 10`, this is:

```text
crit chance += 10 percentage points
```

It is not `critChance * 10%`, and it is not final damage +10%.

## Rebuild Mapping

| Source concept | Rebuild equivalent |
| --- | --- |
| held item/passive id | `BattleUnit.baseStats[STAT_FORM]` legacy field |
| source `game.b.f(byte)` | `BattleUnit.hasFormStatus(byte)` legacy method |
| source row wrapper | `VqsvBattleTables.heldItem(4)` / `BattleHeldItemRow` |
| crit chance path | `BattleUnit.computeDamage()` |
| smoke setup | `SourceBattleRuntime.debugPlayerFormStatusForSmoke(s, 4)` |

Naming note: `STAT_FORM` and `hasFormStatus` are legacy names. Behavior is held item/passive.

## Code Changes

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleUnit.java` | Added smoke-only forced `damage.crit` roll hook; held item 4 crit branch reads `BattleHeldItemRow`. |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Added `debugSetNextDamageCritRollForSmoke`. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added checkpoint `battle_held_item4_crit_window` to prove the +10 percentage-point window. |

## Smoke

Checkpoint:

```text
battle_held_item4_crit_window
```

Output:

```text
rebuild_game/build_intro_demo/battle_held_item4_crit_window.png
```

Smoke image rule:

```text
No audit overlay is drawn on the frame. The PNG is a clean battle frame.
```

Deterministic setup:

```text
speed = 0
forced crit roll = 15

without held item:
    critChance = 5
    15 <= 5 is false
    critFlag = 0

with held item #4:
    critChance = 5 + 10 = 15
    15 <= 15 is true
    critFlag = 1
```

The checkpoint asserts:

- forced `damage.crit` roll is exactly `15`;
- without held item #4, crit does not happen;
- with held item #4, crit happens;
- no player HUD status icon is created;
- battle reaches P7 damage frame and hit is true.

## Verification

Passed:

- `build.ps1`
- `java -cp build/classes VqsvIntroDemo --smoke-checkpoint battle_held_item4_crit_window build_intro_demo/battle_held_item4_crit_window.png`
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
| Source row | PORTED | `aq.c[3][4]` params used |
| Crit formula | PORTED | `critChance += 10` points |
| Deterministic smoke | PORTED | roll `15` fails without item, passes with item |
| HUD status behavior | PORTED | no buff/debuff queue icon |
| Petstate display | PORTED/PREVIOUS | widget 59/60 reads `aq.c[3]`; `Mang` row is text-only by user preference |
| Full equip flow from panel | PARTIAL/SEPARATE | outside this battle formula checkpoint |

## Next Roadmap Step

Recommended next held item/passive slice:

```text
id 2 - Quy Xác Toái Phiến
target defense uses defense * 115 / 100
```

This is the next low-risk numeric formula smoke because it is a direct source formula in `game.b.B()` / `game.b.e(byte)`.
