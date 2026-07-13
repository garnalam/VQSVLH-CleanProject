# 258 - Battle Held Item 7 Linh Trung Thi Hai Turn Priority Closeout

Date: 2026-07-13

Scope: held item / pet-held passive id `7`.

## What It Is

`Linh Trùng Thi Hài` is:

```text
aq.c[3][7] pet-held property/passive
```

It is not a temporary battle buff/debuff and must not create a HUD status icon.

Source storage/check path:

```text
q.L inventory property list -> game.b.c[5] on the pet -> game.b.f((byte)7)
```

## Source Row

```text
aq.c[3][7] = [220,8,244,5,1]
```

| Field | Meaning |
| --- | --- |
| `[0] = 220` | source name text id |
| `[1] = 8` | held item icon cell |
| `[2] = 244` | source description text id |
| no `[5]` param | behavior is identity/flag-based, not numeric-param-based |

Description text id `244` is `Giao tranh bắt đầu;`.

## Source Search Result

Direct source consumer found:

```text
modules/source_code/decoded/decompiled_source_cfr/game/d.java
method T()
```

Relevant source shape:

```text
sort this.t by this.d[n].c[4] speed descending

for each battle slot n:
    if this.d[n].f((byte)7):
        n3 = n
        this.t[n] = 0
        break

if n3 != -1:
    assign all other slots rank 1..N
    sort the remaining slots by speed

for each rank:
    this.e[this.t[n]] = n
    this.v.addElement(this.d[this.e[n]])
```

Meaning:

- held item id `7` overrides normal speed order;
- the first battle unit found with `f(7)` gets rank `0`;
- all other units keep speed ordering after it;
- in current rebuild 1v1 runtime, this means the side with held item id `7` acts before the other side at round start.

## Rebuild Mapping

| Source concept | Rebuild equivalent |
| --- | --- |
| source `game.d.T()` turn order | `SourceBattleRuntime.tickDispatch()` |
| order vectors `t/e/v` | currently simplified 1v1 dispatch |
| source `game.b.f(byte)` | `BattleUnit.hasSourceFormStatus(byte)` |
| held item/passive id | `BattleUnit.baseStats[STAT_FORM]` legacy field |
| ported helper | `SourceBattleRuntime.heldItem7PriorityPlayerFirst()` |

Naming note: `STAT_FORM` and `hasSourceFormStatus` are legacy names. Behavior is held item/passive.

## Code Changes

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Added held item 7 priority branch before speed comparison in `tickDispatch()`. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added checkpoint `battle_held_item7_turn_priority`. |

## Smoke

Checkpoint:

```text
battle_held_item7_turn_priority
```

Output:

```text
rebuild_game/build_intro_demo/battle_held_item7_turn_priority.png
```

Smoke image rule:

```text
No audit overlay is drawn on the frame. The PNG is a clean battle command frame.
```

Deterministic setup:

```text
player:
    species 17
    level 7
    held item id 7 only in enabled branch

enemy:
    species 68
    level 30
    faster than player by source stat table
```

Expected:

```text
baseline without held item 7:
    faster enemy dispatches first -> P2

with held item 7:
    player gets rank 0 before speed order -> P20 command
```

The checkpoint asserts:

- baseline reaches `P2`;
- equipped branch reaches `P20`;
- trace contains `held item7 turn priority`;
- trace contains `playerHeld=true` and `playerFirst=true`.

## Verification

Passed:

- `build.ps1`
- `java -cp build/classes VqsvIntroDemo --smoke-checkpoint battle_held_item7_turn_priority build_intro_demo/battle_held_item7_turn_priority.png`
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
| Source row | PORTED | `aq.c[3][7]` classified as held item flag |
| Source consumer | PORTED/PARTIAL | `game.d.T()` equivalent for current 1v1 dispatch |
| 1v1 turn priority | PORTED | held item 7 acts before faster enemy |
| Multi-slot order arrays `t/e/v` | PARTIAL | full multi-unit parity waits until battle formation expands beyond current 1v1 runtime |
| Visual smoke | PORTED | Clean command frame, no audit overlay |
| HUD status behavior | PORTED | no buff/debuff queue icon |
| Petstate display | PORTED/PREVIOUS | widget 59/60 reads `aq.c[3]`; `Mang` row is text-only by user preference |
| Full equip flow from panel | PARTIAL/SEPARATE | outside this battle dispatch checkpoint |

## Next Roadmap Step

Next id by order:

```text
id 8 - Hấp Huyết Đằng Mạn
after-hit leech heal
```

Source search already indicates:

```text
game.d.java:
if attacker.f((byte)8) && ae.a(100) <= aq.c[3][8][5]:
    attacker heals damage * aq.c[3][8][6] / 100
```

Next slice should audit that block, confirm exact timing/text, then smoke with forced roll.
