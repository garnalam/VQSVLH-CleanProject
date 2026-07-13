# 257 - Battle Held Item 6 Ky Cu Giai Xac Reserve EXP Share Closeout

Date: 2026-07-13

Scope: held item / pet-held passive id `6`.

## What It Is

`Ký Cư Giải Xác` is:

```text
aq.c[3][6] pet-held property/passive
```

It is not a temporary battle buff/debuff and must not create a HUD status icon.

Source storage/check path:

```text
q.L inventory property list -> game.b.c[5] on the pet -> game.b.f((byte)6)
```

## Source Row

```text
aq.c[3][6] = [219,7,243,5,1,100]
```

| Field | Meaning |
| --- | --- |
| `[0] = 219` | source name text id |
| `[1] = 7` | held item icon cell |
| `[2] = 243` | source description text id |
| `[5] = 100` | description/table param; not directly read by the reserve branch in `game.d.h` |

Important: the source reserve EXP branch checks `f((byte)6)` but does not multiply by `aq.c[3][6][5]`.

## Source Logic

From `modules/source_code/decoded/decompiled_source_cfr/game/d.java`, method `h(b defeated)`:

```text
for each party pet c(n3):
    if !c(n3).S() or game.d.x.contains(c(n3)):
        continue

    if game.g.B[7][0] == 2:
        exp = base / participantCount * aH[participantCount - 1] * levelFactor / 3000
        c(n3).B += exp
        c(n3).c()
        add to game.d.j
        continue

    if !c(n3).f((byte)6):
        continue

    exp = base / participantCount * aH[participantCount - 1] * levelFactor / 1000
    c(n3).B += exp
    c(n3).c()
    add to game.d.j
```

Meaning:

- the reserve pet must be alive;
- it must not already be a direct participant in `game.d.x`;
- if it has held item id `6`, it receives reserve EXP;
- the divisor is `1000`, same as a direct participant formula branch, but using the last direct participant level factor;
- it is added to `game.d.j` so the EXP UI/consumer can process it.

## Rebuild Mapping

| Source concept | Rebuild equivalent |
| --- | --- |
| direct participant vector `game.d.x` | `SourceBattleRuntime.sourceExpParticipants` |
| display/consumer vector `game.d.j` | `SourceBattleRuntime.sourceExpDisplay` |
| pending EXP `game.b.B` | `SourcePetState.sourcePendingExp` |
| held item/passive id | `BattleUnit.baseStats[STAT_FORM]` legacy field |
| source `game.b.f(byte)` | `BattleUnit.hasSourceFormStatus(byte)` |
| source row wrapper | `VqsvBattleTables.heldItem(6)` / `BattleHeldItemRow` available; reserve branch itself only checks id |
| producer path | `SourceBattleRuntime.prepareSourceExpAwards()` |

Naming note: `STAT_FORM` and `hasSourceFormStatus` are legacy names. Behavior is held item/passive.

## Code Changes

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Reserve EXP trace now names the branch `heldItem6ReserveShare f(6)`. Behavior remains source-equivalent. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added checkpoint alias `battle_held_item6_reserve_exp_share`; old `battle_exp_vector_reserve_form6_share` remains valid. |

## Smoke

Checkpoint:

```text
battle_held_item6_reserve_exp_share
```

Output:

```text
rebuild_game/build_intro_demo/battle_held_item6_reserve_exp_share.png
```

Smoke image rule:

```text
No audit overlay is drawn on the frame. The PNG is a clean battle/EXP frame.
```

Deterministic setup:

```text
active/direct participant:
    species 17, level 7, no held item 6

reserve pet:
    species 92, level 5, sourcePayload[2] = 6

enemy:
    species 68, level 5, quality 1

participantCount = 1
reserve divisor = 1000
levelFactor comes from last direct participant level 7, matching source b3.s()
```

Expected:

```text
direct participant receives normal direct EXP
reserve pet receives sourceExpectedExpAward(5, 1, 7, 1, 1000)
both pets enter game.d.j equivalent
two game.d.X commits happen
trace contains reason=heldItem6ReserveShare f(6)
```

## Verification

Passed:

- `build.ps1`
- `java -cp build/classes VqsvIntroDemo --smoke-checkpoint battle_held_item6_reserve_exp_share build_intro_demo/battle_held_item6_reserve_exp_share.png`
- legacy alias:
  - `java -cp build/classes VqsvIntroDemo --smoke-checkpoint battle_exp_vector_reserve_form6_share build_intro_demo/battle_exp_vector_reserve_form6_share.png`
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
| Source row | PORTED | `aq.c[3][6]` classified as held item |
| Reserve EXP branch | PORTED | `f(6)` reserve pet joins EXP queue |
| Direct participant exclusion | PORTED | reserve branch skips pets already in `game.d.x` |
| Display/consumer queue | PORTED | reserve pet enters `game.d.j` equivalent |
| Visual smoke | PORTED | Clean P8 frame, no audit overlay |
| HUD status behavior | PORTED | no buff/debuff queue icon |
| Petstate display | PORTED/PREVIOUS | widget 59/60 reads `aq.c[3]`; `Mang` row is text-only by user preference |
| Full equip flow from panel | PARTIAL/SEPARATE | outside this battle producer checkpoint |

## Next Roadmap Step

Next id by order:

```text
id 7 - Linh Trùng Thi Hài
```

This one is still `UNKNOWN/PENDING` and should be source-searched before coding. Do not infer behavior from the short description alone.
