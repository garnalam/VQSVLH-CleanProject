# 256 - Battle Held Item 5 Mat Phong Sao EXP Multiplier Closeout

Date: 2026-07-13

Scope: held item / pet-held passive id `5`.

## What It Is

`Mật Phong Sào` is:

```text
aq.c[3][5] pet-held property/passive
```

It is not a temporary battle buff/debuff and must not create a HUD status icon.

Source storage/check path:

```text
q.L inventory property list -> game.b.c[5] on the pet -> game.b.f((byte)5)
```

## Source Row

```text
aq.c[3][5] = [218,6,242,5,1,20]
```

| Field | Meaning |
| --- | --- |
| `[0] = 218` | source name text id |
| `[1] = 6` | held item icon cell |
| `[2] = 242` | source description text id |
| `[5] = 20` | EXP bonus percent |

## Source Logic

From `modules/source_code/decoded/decompiled_source_cfr/game/d.java`, method `h(b defeated)`:

```text
for each direct participant b3 in game.d.x:
    exp = baseExp / x.size() * aH[x.size() - 1] * levelFactor / 1000

    if b3.f((byte)5):
        exp = exp * (aq.c[3][5][5] + 100) / 100

    b3.B += exp
    if !game.d.j.contains(b3):
        game.d.j.addElement(b3)
```

With row param `[5] = 20`:

```text
participantExp = baseParticipantExp * 120 / 100
```

This is only for direct participants in `game.d.x`. Reserve/global EXP share belongs to held item id `6` or global state `game.g.B[7][0]`.

## Rebuild Mapping

| Source concept | Rebuild equivalent |
| --- | --- |
| direct participant vector `game.d.x` | `SourceBattleRuntime.sourceExpParticipants` |
| display/consumer vector `game.d.j` | `SourceBattleRuntime.sourceExpDisplay` |
| pending EXP `game.b.B` | `SourcePetState.sourcePendingExp` |
| held item/passive id | `BattleUnit.baseStats[STAT_FORM]` legacy field |
| source `game.b.f(byte)` | `BattleUnit.hasSourceFormStatus(byte)` |
| source row wrapper | `VqsvBattleTables.heldItem(5)` / `BattleHeldItemRow` |
| producer path | `SourceBattleRuntime.prepareSourceExpAwards()` |

Naming note: `STAT_FORM` and `hasSourceFormStatus` are legacy names. Behavior is held item/passive.

## Code Changes

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Direct participant EXP multiplier now reads `BattleHeldItemRow` through `sourceHeldItemParam(5,5,0)` and trace says `heldItem5Multiplier`. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added checkpoint alias `battle_held_item5_exp_multiplier`; old `battle_exp_vector_participant_form5_multiplier` remains valid but uses held item terminology internally. |

## Smoke

Checkpoint:

```text
battle_held_item5_exp_multiplier
```

Output:

```text
rebuild_game/build_intro_demo/battle_held_item5_exp_multiplier.png
```

Smoke image rule:

```text
No audit overlay is drawn on the frame. The PNG is a clean battle/EXP frame.
```

Deterministic setup:

```text
enemy species = 68
enemy level = 5
enemy quality = 1
participant species = 17
participant level = 7
participant count = 1
held item id = 5

base participant EXP = sourceExpectedExpAward(5, 1, 7, 1)
with Mật Phong Sào = base * (100 + 20) / 100
```

The checkpoint asserts:

- the participant has `sourcePayload[2] = 5`;
- producer trace contains `heldItem5Multiplier=true`;
- final pet EXP equals `base * 120 / 100`;
- battle reaches P8 EXP display path;
- no battle HUD buff/debuff status icon is involved.

## Verification

Passed:

- `build.ps1`
- `java -cp build/classes VqsvIntroDemo --smoke-checkpoint battle_held_item5_exp_multiplier build_intro_demo/battle_held_item5_exp_multiplier.png`
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
| Source row | PORTED | `aq.c[3][5]` params used |
| Direct participant EXP multiplier | PORTED | `exp*120/100` |
| EXP vector integration | PORTED | Uses `game.d.x` / `game.d.j` equivalent |
| Visual smoke | PORTED | Clean P8 frame, no audit overlay |
| HUD status behavior | PORTED | no buff/debuff queue icon |
| Petstate display | PORTED/PREVIOUS | widget 59/60 reads `aq.c[3]`; `Mang` row is text-only by user preference |
| Full equip flow from panel | PARTIAL/SEPARATE | outside this battle producer checkpoint |

## Next Roadmap Step

Next held item/passive by id order:

```text
id 6 - Ký Cư Giải Xác
reserve pet EXP share
```

Source already audited in `120_battle_exp_passive_share_slice_b_matrix.md`; next slice should rename/close out it as held item id `6`, then smoke reserve pet receives EXP without entering `game.d.x`.
