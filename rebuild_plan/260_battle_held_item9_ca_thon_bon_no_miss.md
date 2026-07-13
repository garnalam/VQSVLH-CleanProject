# 260 - Battle Held Item 9 Cá Thờn Bơn No-Miss Closeout

Date: 2026-07-13

Scope: held item / pet-held passive id `9`.

## What It Is

`Cá Thờn Bơn` is a pet-held passive from `aq.c[3]`.

It is not a temporary battle status, not a buff/debuff, and must not create a HUD status icon. Older docs/checkpoints called it `form9` or `status_form9`; that is legacy naming only.

Source storage/check path:

```text
q.L inventory property list -> game.b.c[5] on the pet -> game.b.f((byte)9)
```

## Source Row

```text
aq.c[3][9] = [222,10,246,5,1]
```

| Field | Meaning |
| --- | --- |
| `[0] = 222` | source name text id |
| `[1] = 10` | held item icon cell |
| `[2] = 246` | source description text id |
| no `[5]` param | behavior is flag-based |

Source description meaning: the pet's attacks always hit.

## Source Evidence

Source file:

```text
modules/source_code/decoded/decompiled_source_cfr/game/d.java
```

Relevant source shape in P7 hit/miss flow:

```text
missChance = (targetSpeed - attackerSpeed) << 1

if (attacker.f((byte)9)) {
    missChance = 0
}

if (missChance <= 0) {
    missChance = 0
} else if (missChance >= 20) {
    missChance = 20
}

if (ae.a(100) >= missChance) {
    apply damage
} else {
    show "Né tránh"
}
```

Meaning:

- normal miss chance is based on target speed minus attacker speed, doubled;
- miss chance is clamped to `0..20`;
- held item id `9` forces `missChance = 0`;
- because source hit condition is `ae.a(100) >= missChance`, a miss chance of `0` always hits;
- this path does not create any status queue/icon.

## Rebuild Mapping

| Source concept | Rebuild equivalent |
| --- | --- |
| `game.b.f((byte)9)` | `BattleUnit.hasSourceHeldItem(9)` |
| P7 miss chance calculation | `SourceBattleRuntime.sourceP7MissChance(...)` |
| source hit condition | `SourceBattleRuntime.applyP7Damage(...)` using `hitRoll >= missChance` |
| legacy smoke name | `battle_status_form9_no_miss` kept as alias only |
| new smoke name | `battle_held_item9_no_miss` |

## Code Changes

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleUnit.java` | Added `hasSourceHeldItem(...)` alias for held-item classification. |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Reclassified P7 no-miss hook from `form9` trace to `heldItem9`. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added primary checkpoint `battle_held_item9_no_miss`; kept old `battle_status_form9_no_miss` as compatibility alias; updated `battle_quick` to use the held-item name; made exact-damage held item 0/1 smoke deterministic by forcing non-crit roll. |

## Smoke

Primary checkpoint:

```text
battle_held_item9_no_miss
```

Output:

```text
rebuild_game/build_intro_demo/battle_held_item9_no_miss.png
```

Compatibility alias:

```text
battle_status_form9_no_miss
```

Deterministic setup:

```text
attacker speed: 0
target speed: 200
forced P7 hit roll: 0
held item: id 9 on attacker
```

Expected without id 9:

```text
missChance would clamp to 20
roll 0 would miss
```

Expected with id 9:

```text
missChance = 0
roll 0 hits
damage text is visible
no miss text
no player status icon
```

The checkpoint asserts:

- baseline without held item id `9` misses at the same setup (`missChance = 20`, roll `0`);
- trace contains `heldItem9=true`;
- trace contains `missChance=0`;
- trace contains `hit=true`;
- no miss text is visible;
- no HUD status icon is created.

Smoke image rule:

```text
No audit overlay is drawn. The PNG is a clean battle frame.
```

## Verification

Passed:

- `build.ps1`
- `java -cp build/classes VqsvIntroDemo --smoke-checkpoint battle_held_item9_no_miss build_intro_demo/battle_held_item9_no_miss.png`
- `java -cp build/classes VqsvIntroDemo --smoke-checkpoint battle_status_form9_no_miss build_intro_demo/battle_status_form9_no_miss_alias.png`
- `java -cp build/classes VqsvIntroDemo --smoke-suite battle_quick build_intro_demo/suite_battle_quick`
- `java -cp build/classes com.vqsv.rebuild.Main --check`
- `java -cp build/classes VqsvBattleDamageFormulaCheck`
- Java mojibake scan with strict pattern `Ã|Â|�|\?i\?|Th\?`

`battle_quick` passed `37/37`; it now uses `battle_held_item9_no_miss` as the primary checkpoint.

## Status

| Area | Status | Note |
| --- | --- | --- |
| Source row | PORTED | `aq.c[3][9] = [222,10,246,5,1]` |
| Source consumer | PORTED | `game.d` P7 miss path |
| No-miss formula | PORTED | `missChance = 0`, so `ae.a(100) >= 0` always hits |
| Checkpoint naming | PORTED | new primary name is `battle_held_item9_no_miss` |
| Old checkpoint alias | PORTED/COMPAT | `battle_status_form9_no_miss` still works, but should not be used in new roadmap docs |
| HUD status behavior | PORTED | no buff/debuff/status icon |
| Full equip/save flow | PARTIAL/SEPARATE | outside this battle runtime checkpoint |

## Next Roadmap Step

Next held item by order:

```text
id 10 - Cảm Lãm Chi Diệp
HP floor / capture-related source path
```

This one should be audited before coding because source ties it to the P7 damage path and capture safety behavior, not a simple stat modifier.
