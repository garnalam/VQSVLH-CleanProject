# 261 - Battle Held Item 10 Cảm Lãm Chi Diệp HP Floor Audit

Date: 2026-07-13

Scope: audit plus P7 HP-floor port closeout.

## What It Is

`Cảm Lãm Chi Diệp` is a pet-held passive from `aq.c[3]`.

It is not a buff/debuff/status icon. It is a held-item flag checked during P7 damage resolution to stop the attacker's target from dropping below a floor HP value.

Important correction:

```text
The source checks the attacker's held item, not the target's held item.
```

So this item behaves like a capture/safety passive: if the attacker carries it, attacks should not kill the target when the source HP-floor condition is reached.

## Source Row

```text
aq.c[3][10] = [223,11,247,5,1,10]
```

| Field | Meaning |
| --- | --- |
| `[0] = 223` | source name text id |
| `[1] = 11` | held item icon cell |
| `[2] = 247` | source description text id |
| `[5] = 10` | HP floor value |

Source description meaning:

```text
No matter how injured the opponent is, HP will not go below 10 points.
The wording is capture-related.
```

## Source Evidence

Source file:

```text
modules/source_code/decoded/decompiled_source_cfr/game/d.java
```

### P7 Direct Damage Path

Source shape:

```text
if (ae.a(100) >= missChance) {
    target.k(Z[0]); // apply damage

    if (attacker.f((byte)10) && target.a((byte)1) <= aq.c[3][10][5]) {
        target.a((byte)1, aq.c[3][10][5]); // set target HP to 10
    }

    show damage text "-Z[0]"
}
```

Facts:

- `h` is the current actor/attacker.
- `h.p` is the selected target.
- `h.f((byte)10)` checks held item id `10` on the attacker.
- `((b)h.p).a((byte)1)` reads target HP/stat index `1`.
- `((b)h.p).a((byte)1, value)` writes target HP/stat index `1`.
- damage text still uses the original `Z[0]`, not the actual HP lost after floor correction.

### U() Post-Animation Path

Source shape:

```text
private void U() {
    if (this.h.f((byte)10) && ((b)this.h.p).d[1] <= aq.c[3][10][5]) {
        ((b)this.h.p).d[1] = aq.c[3][10][5];
    }
    if (((b)this.h.p).d[1] <= 0) {
        ((b)this.h.p).d((byte)3);
        return;
    }
    ((b)this.h.p).d((byte)0);
}
```

Facts:

- same owner rule: attacker `h` must carry held item id `10`;
- target HP is floored again before death state check;
- if floor applies, target state returns to normal state `0`, not dead state `3`.

## Source Matrix

| Source location | Condition | Target mutation | Text/visual | Next-state implication | Status |
| --- | --- | --- | --- | --- | --- |
| `game.d` P7 damage apply block around direct hit | `attacker.f(10)` and target HP `<= 10` after damage | set target HP to `10` | still shows `-Z[0]` damage text | target remains alive for following checks | SOURCE_PROVEN |
| `game.d.U()` | `attacker.f(10)` and target HP `<= 10` | set target HP to `10` | no extra text proven here | target state `0` instead of death state `3` | SOURCE_PROVEN |
| Catch/P21/P17 path | no direct `f(10)` consumer found in catch state search | none proven | none proven | wording is capture-related, but battle consumer is P7/U | NOT_DIRECTLY_PROVEN |

## Rebuild Current State

Current search result in `rebuild_game/src/main/java`:

```text
no active held item id 10 hook found
```

Relevant current code:

| Rebuild file/function | Current behavior | Gap |
| --- | --- | --- |
| `SourceBattleRuntime.applyP7Damage(...)` | applies `p7Target.damage(p7Damage)` directly | does not floor target HP to held item 10 param |
| `SourceBattleRuntime.finishP7(...)` | if `!p7Target.alive()`, immediately enters KO handling | should see floored HP as alive before KO |
| `BattleUnit.hasSourceHeldItem(...)` | exists after held item 9 cleanup | can be reused for id 10 |
| `BattleUnit.sourceHeldItemParam(...)` | exists | can read `[5] = 10` |
| Smoke checkpoint | none | needs new checkpoint |

Some older docs listed id `10` as `PORTED/PARTIAL`. That classification is stale for current runtime code; based on current source search, the code status should be treated as:

```text
MISSING
```

## Expected Rebuild Behavior To Port

Minimal source-shaped port:

```text
after P7 hit applies damage:
    if attacker has held item 10:
        floor = aq.c[3][10][5]
        if target HP <= floor:
            target HP = floor
            sync render target HP
            do not enter KO flow
```

Important:

- Do not apply this when the attack misses.
- Do not apply this when target carries id 10 but attacker does not.
- Do not create a status icon.
- Do not alter displayed damage text unless source evidence proves otherwise.
- Do not treat it as catch chance or ball logic.

## Smoke Plan

Primary checkpoint to add:

```text
battle_held_item10_hp_floor
```

Deterministic setup:

```text
skill: 10 direct damage
enemy HP before attack: low enough to die from damage
attacker held item: id 10
forced hit: yes
forced crit: no
```

Assertions:

```text
baseline without held item 10:
    enemy HP becomes 0
    P7/finish enters KO/win path or target alive is false

with held item 10:
    enemy HP becomes 10
    target remains alive
    no status icon
    trace contains heldItem10 HP floor
```

Potential PNG:

```text
rebuild_game/build_intro_demo/battle_held_item10_hp_floor.png
```

Clean frame only, no audit overlay.

## Risk Notes

| Risk | Handling |
| --- | --- |
| Damage text may look larger than actual HP loss | Keep source behavior: source displays `-Z[0]` after floor |
| Existing death/EXP route might trigger too early | Apply floor before `finishP7()` dead-target checks |
| Bunny capture tutorial HP thresholds | Smoke Bunny route after port; held item 10 should not be active unless explicitly equipped |
| Enemy/player side symmetry | Implement against `p7Attacker`/`p7Target`, not hardcoded player/enemy |

## Port Closeout

Implemented:

```text
SourceBattleRuntime.applyP7Damage(...)
```

Behavior now matches the proven source slice:

```text
after a successful P7 hit:
    if attacker has held item id 10:
        floor = aq.c[3][10][5]
        if target HP <= floor:
            target HP = floor
```

Important preserved source behavior:

- the HP floor uses the attacker's held item, not the target's held item;
- the target remains alive at `10` HP;
- damage text still shows the original damage value;
- no HUD status icon is created.

Code files:

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Added `applyP7HeldItem10HpFloor(...)` and call after `p7Target.damage(...)`. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added `battle_held_item10_hp_floor`, baseline KO branch, equipped HP-floor branch, and included it in `battle_quick`. |

Smoke output:

```text
rebuild_game/build_intro_demo/battle_held_item10_hp_floor.png
```

Smoke result:

```text
baseline without held item 10:
    enemy HP becomes 0

with held item 10:
    enemy HP becomes 10
    battle log still shows damage 80
```

Verification passed:

```text
build.ps1
java -cp build/classes VqsvIntroDemo --smoke-checkpoint battle_held_item10_hp_floor build_intro_demo/battle_held_item10_hp_floor.png
java -cp build/classes VqsvIntroDemo --smoke-suite battle_quick build_intro_demo/suite_battle_quick
java -cp build/classes com.vqsv.rebuild.Main --check
java -cp build/classes VqsvBattleDamageFormulaCheck
strict mojibake scan
```

`battle_quick` now contains `38` checkpoints and passed `38/38`.

## Status

| Area | Status | Note |
| --- | --- | --- |
| Source row | SOURCE_PROVEN | `aq.c[3][10] = [223,11,247,5,1,10]` |
| Owner semantics | SOURCE_PROVEN | attacker/current actor carries id 10 |
| HP floor value | SOURCE_PROVEN | `[5] = 10` |
| P7 damage hook | PORTED | target HP floored after successful hit |
| U() post-animation/death-state hook | PORTED/PARTIAL | P7 hook happens before current death handling; exact Java ME actor-state timing remains broader P7 parity |
| Catch-specific consumer | NOT_PROVEN | wording is capture-related, but no direct P21/P17 consumer found |
| HUD icon | PORTED-BY-ABSENCE | should remain no icon |

Next held item after that:

```text
id 11 - Sủng vật lôi đạt / catch chance +20%
```
