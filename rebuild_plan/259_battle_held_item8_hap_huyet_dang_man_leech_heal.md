# 259 - Battle Held Item 8 Hấp Huyết Đằng Mạn Leech Heal Closeout

Date: 2026-07-13

Scope: held item / pet-held passive id `8`.

## What It Is

`Hấp Huyết Đằng Mạn` is a pet-held passive from `aq.c[3]`.

It is not a temporary buff/debuff, does not create a HUD status icon, and is checked through the same legacy pet field currently named `STAT_FORM` / `hasSourceFormStatus`.

Source storage/check path:

```text
q.L inventory property list -> game.b.c[5] on the pet -> game.b.f((byte)8)
```

## Source Row

```text
aq.c[3][8] = [221,9,245,5,1,10,20]
```

| Field | Meaning |
| --- | --- |
| `[0] = 221` | source name text id |
| `[1] = 9` | held item icon cell |
| `[2] = 245` | source description text id |
| `[5] = 10` | leech chance percent |
| `[6] = 20` | heal percent from dealt damage |

Description meaning: after attacking, the pet has a chance to restore part of its HP.

## Source Evidence

Source file:

```text
modules/source_code/decoded/decompiled_source_cfr/game/d.java
```

Relevant source shape in `game.d.q()`:

```text
if (aq.c[1][d2.h.D][9] == 0) {
    if (d2.h.f((byte)8) && ae.a(100) <= aq.c[3][8][5]) {
        d2.h.u(d2.h.d[1]);
        d2.h.l((short)(d2.Z[0] * aq.c[3][8][6] / 100));
    }
}
```

Meaning:

- only runs after a hit/damage skill that targets the opponent (`skill targetSide == 0`);
- held item id `8` must be equipped on the attacker;
- roll must pass `<= 10`;
- heal amount is `damage * 20 / 100`;
- heal is applied to the attacker;
- this path does not prove a separate buff/debuff icon.

## Rebuild Mapping

| Source concept | Rebuild equivalent |
| --- | --- |
| `game.b.f((byte)8)` | `BattleUnit.hasSourceFormStatus(8)` legacy held-item check |
| `aq.c[3][8][5]` | `BattleUnit.sourceHeldItemParam(8, 5, 0)` |
| `aq.c[3][8][6]` | `BattleUnit.sourceHeldItemParam(8, 6, 0)` |
| `ae.a(100)` | `SourceBattleRuntime.sourceHeldItem8LeechRollPassed(...)` |
| `d2.Z[0]` | current P7 damage result `p7Damage` |
| `d2.h.l(...)` | `BattleUnit.heal(...)` on attacker |

Naming note: `STAT_FORM` / `hasSourceFormStatus` remain legacy names until the held-item field is renamed globally.

## Code Changes

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleUnit.java` | Added `sourceHeldItemParam(...)` so held item rows no longer need status-row helpers. |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Ported held item 8 P7 post-damage leech using `aq.c[3][8][5]/[6]`, with deterministic smoke roll trace. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added checkpoint `battle_held_item8_leech_heal` and included it in `battle_quick`. |

## Smoke

Checkpoint:

```text
battle_held_item8_leech_heal
```

Output:

```text
rebuild_game/build_intro_demo/battle_held_item8_leech_heal.png
```

Deterministic setup:

```text
skill: 10 direct damage
attacker held item: id 8
attacker HP before pass branch: 40
damage: 80
fail roll: 11
pass roll: 0
```

Expected:

```text
fail roll 11:
    chance 10 fails
    HP remains 40

pass roll 0:
    chance 10 passes
    heal = 80 * 20 / 100 = 16
    HP changes 40 -> 56
```

The checkpoint asserts:

- fail branch does not heal;
- pass branch heals exactly `damage * 20 / 100`;
- no player status icon is created;
- trace contains source mapping to `game.d.q d2.h.l(...)`.

Smoke image rule:

```text
No audit overlay is drawn. The PNG is a clean battle frame.
```

## Verification

Passed:

- `build.ps1`
- `java -cp build/classes VqsvIntroDemo --smoke-checkpoint battle_held_item8_leech_heal build_intro_demo/battle_held_item8_leech_heal.png`
- `java -cp build/classes VqsvIntroDemo --smoke-suite battle_quick build_intro_demo/suite_battle_quick`
- `java -cp build/classes com.vqsv.rebuild.Main --check`
- `java -cp build/classes VqsvBattleDamageFormulaCheck`
- Java mojibake scan with strict pattern `Ã|Â|�|\?i\?|Th\?`
- route smoke regression:
  - `route_sophie_after_battle_branch`
  - `route_bunny_after_battle_task`
  - `route_elder_after_battle_reward_state`

## Status

| Area | Status | Note |
| --- | --- | --- |
| Source row | PORTED | `aq.c[3][8] = [221,9,245,5,1,10,20]` |
| Chance gate | PORTED | roll `<= 10` |
| Heal formula | PORTED | `damage * 20 / 100`, clamped by normal HP setter |
| P7 source timing | PORTED/PARTIAL | runs in P7 post-damage source hook; exact MIDP visual timing is still broader P7 parity work |
| HUD status icon | PORTED | none created |
| Petstate display | PORTED/PREVIOUS | held item name display comes from `aq.c[3]` |
| Full equip/save flow | PARTIAL/SEPARATE | outside this battle runtime checkpoint |

## Next Roadmap Step

Next held item by order:

```text
id 9 - Cá Thờn Bơn
no-miss behavior
```

There is already older coverage named `battle_status_form9_no_miss`; next cleanup should rename/reclassify it as held item id 9, verify source row `aq.c[3][9]`, and keep the no-status-icon rule.
