# 245 - Held Item 0 Low HP Attack Boost Closeout

Date: 2026-07-13

Scope: smoke/code slice for held item/passive id `0` - `Mạn Đà La Thạch`.

The checkpoint name remains `battle_status_form0_low_hp_attack_boost` for regression
compatibility, but the source concept is no longer called `form/status`. It is
`aq.c[3][0]` pet-held property/passive, equipped through `game.b.c[5]` and checked by
`game.b.f((byte)0)`.

## Source Facts

From `aq.c[3][0]`:

```text
[213,1,237,5,1,30,100]
```

Meaning:

```text
name text id = 213
icon cell = 1
description text id = 237
trigger threshold = 30% max HP
attack bonus = 100%
```

From `game.b.B()`:

```text
if pet.c[5] == 0
and current HP <= maxHP * 30 / 100:
    raw = attack * (100 + 100) / 100 - targetDefense
else:
    raw = attack - targetDefense
```

This is `+100% attack before defense subtraction`, not final damage x2.

## Rebuild Mapping

| Source | Rebuild |
| --- | --- |
| `game.b.c[5]` | `BattleUnit.baseStats[STAT_FORM]` legacy field, now interpreted as held item/passive id |
| `game.b.f((byte)0)` | `BattleUnit.hasFormStatus((byte)0)` legacy method name |
| `game.b.B()` | `BattleUnit.baseAttack()` inside `computeDamage()` |
| `aq.c[3][0][5]` | threshold `30` |
| `aq.c[3][0][6]` | attack bonus `100` |

## Smoke Checkpoint

```text
battle_status_form0_low_hp_attack_boost
```

The checkpoint compares two deterministic battle setups:

| Setup | HP | Attack | Enemy defense | Expected raw damage |
| --- | ---: | ---: | ---: | ---: |
| high HP with held item id 0 | 80/134 | 120 | 40 | 80 |
| low HP with held item id 0 | 30/134 | 120 | 40 | 200 |

It also verifies this held item/passive does not create HUD status queue icons.

The smoke PNG overlays a visual audit panel on the battle frame:

- held item name/icon cell from `aq.c[3][0]`;
- application path `q.L -> game.b.c[5] = 0`;
- trigger `HP <= 30% max HP`;
- baseline damage `80`;
- active low-HP damage `200`.

## Result

Status: `PORTED` for battle damage formula.

Flow note: this checkpoint applies the held item directly through smoke/debug state
(`c[5]=0`). The separate equip UI flow is the `q.L` / `Mang theo` / `Dỡ xuống` path
audited in the panel petsetting equipment docs.

Smoke PNG:

```text
rebuild_game/build_intro_demo/status_effectiveness_battle_status_form0_low_hp_attack_boost.png
```

Suite PNG:

```text
rebuild_game/build_intro_demo/suite_battle_quick_after_form0/battle_status_form0_low_hp_attack_boost.png
```

## Regression

Passed before this visual overlay update:

- `build.ps1`
- `java -cp build/classes com.vqsv.rebuild.Main --check`
- `java -cp build/classes VqsvBattleDamageFormulaCheck`
- Java mojibake scan: no new issue; only existing valid Vietnamese intro text hits
- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`
- `--smoke-suite battle_quick`

## Next Roadmap Step

Next held-item/passive slice: id `4` - `Viễn Cổ Long Cốt`.

Expected source numeric behavior:

```text
crit chance += 10 percentage points
```

Smoke should force/compare crit threshold around the +10 window without changing runtime
battle logic unless the source-backed expectation fails.
