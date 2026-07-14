# 297 - Battle Debuff9 Hon Loan Closeout

Scope: dedicated audit and implementation closeout for debuff id `9` / `Hon Loan`.

## Plain Gameplay Explanation

`Hon Loan` is not a poison/DOT/stat-down effect.

When a pet already has this status and starts an attack, the source throws away
the normal target route and rebuilds the target list from every living battle
unit except the attacker. Then it chooses one target by source RNG.

In the current PC rebuild's verified 1v1 battle route this still visually points
to the enemy, because the only living unit except the player pet is the enemy.
The important locked behavior is the source route and RNG call:

```text
attacker has debuff9
-> game.d.f(attacker)
-> living units except attacker
-> ae.a(G.size())
-> P7 resolves against the chosen target
```

User rule after audit: debuff9 does not block pet switching. The only battle
status in this closeout lane that blocks switching is debuff2 `Quan Quanh`.
Source audit also found no direct `game.h.X()` P5 confirm branch that rejects
pet switching because of debuff9.

The proven debuff9 source route is in `game.d`: after active queue, if the
player unit still has debuff9, battle returns to P2 auto action instead of P20
command menu.

## Source Facts

```text
skill55 = [5,172,584,0,1,10,2,9,-1,0]
debuff9 = [320,331,1]
effect.mid[55] = [0,1,12,0,-1,-1,0]
bufDebuf ar[1][9] = [1,12,0,-1]
game.d.ai[1] includes debuff ids [0,1,2,3,8,9,10]
```

Important source branches:

```text
game.d case 2:
if attacker.p(9):
    f(attacker)
    index = ae.a(attacker.G.size())
    target = attacker.G[index]
    attacker.I = attacker.H[index]
    S.b(attacker, target)
    attacker.a(skill, target)
    a((byte)7)
```

```text
game.d.f(attacker):
attacker.G.clear()
attacker.H.clear()
for each active battle unit:
    if unit.alive && unit != attacker:
        attacker.G.add(unit)
        attacker.H.add(unitIndex)
```

```text
game.d.b(b2, true), player active-queue return:
if b2.p(9):
    a((byte)2)
else:
    a((byte)20)
```

No source-backed direct P5 confirm lock was found in `game.h.X()`. That method
delegates battle P5 confirm to `game.d.a(selectedRow)` and handles dead/current/
valid pet selection, but it does not check debuff9 directly. Keep P5 switching
allowed for debuff9 unless a later source capture proves otherwise.

## Implemented

Runtime:

- `SourceBattleRuntime.prepareTargetList()` now handles active debuff9:
  - rebuilds target vector as living units except attacker;
  - calls source-shaped RNG through `VqsvSourceRandom.a(bound)`;
  - sets the selected target index from that RNG result;
  - traces normal route, reroute route, random index, and source classification.
- `SourceBattleRuntime.prepareP7()` now commits the random debuff9 target before
  resolving P7, so auto-action P2 and manual skill confirm both follow the same
  source consumer.
- Skill55 ordinary debuff producer remains `NOT_REACHED`; no fake damage/debuff
  application was added.

Focused checkpoints:

```text
battle_status_debuff9_before_no_effect
battle_status_debuff9_skill55_zero_power_no_apply
battle_status_debuff9_random_target_seeded_active
battle_status_debuff9_p12_body_visual_type12
battle_status_debuff9_expiry_clears_icon
```

## Numeric Smoke Results

| Slice | Before | During / after |
| --- | --- | --- |
| before/no effect | enemy HP `109/109`, no debuff9 | enemy status icon count `0` |
| skill55 producer | enemy HP `109/109` | zero-power guard, no hitroll, no damage text, no `appliedDebuffId=9` |
| seeded active target route | player debuff9 duration `1`, icon `10`, duration cell `135` | target vector rebuilt through `game.d.f(attacker)`, forced random index `0`, slot `0`, ordinary skill55 producer still `NOT_REACHED` |
| P12/P13 body visual | enemy HP `50`, debuff9 duration `1` | visual row `[1,12,0,-1]` starts, AH/speffect type `12` visible |
| expiry | enemy HP `50`, debuff9 duration `1` | tick gives duration `1 -> 0`, HP unchanged, icon cleared |

## PNG Output

Focused PNGs:

```text
rebuild_game/build_intro_demo/debuff9_closeout/battle_status_debuff9_before_no_effect.png
rebuild_game/build_intro_demo/debuff9_closeout/battle_status_debuff9_skill55_zero_power_no_apply.png
rebuild_game/build_intro_demo/debuff9_closeout/battle_status_debuff9_random_target_seeded_active.png
rebuild_game/build_intro_demo/debuff9_closeout/battle_status_debuff9_p12_body_visual_type12.png
rebuild_game/build_intro_demo/debuff9_closeout/battle_status_debuff9_expiry_clears_icon.png
```

## Verification

```text
build.ps1 PASS
focused debuff9 closeout PNG smoke 5/5 PASS
com.vqsv.rebuild.Main --check PASS
VqsvBattleDamageFormulaCheck PASS
battle_quick PASS 221/221
```

## Classification

```text
Skill55 visual / zero-power route: PORTED/PARTIAL
Skill55 ordinary debuff9 producer: NOT_REACHED
Debuff9 active random-target consumer: PORTED/PARTIAL
RNG target index ae.a(G.size): PORTED
P12/P13 body visual from bufDebuf row: PORTED
Tick no-op + duration expiry icon clear: PORTED
Direct P5 switch behavior: ALLOWED / USER_CONFIRMED
Full multi-active target divergence: PENDING
Pixel-perfect original comparison: PENDING
```

`Debuff9 active random-target consumer` remains `PORTED/PARTIAL` only because
the current verified battle route is still 1v1. The route trace and RNG index are
source-shaped and smoke-locked, but there is not yet a multi-active source route
where the chosen target visibly diverges.

## Next Step

Move to debuff10 `Te Liet` action-delay parity. Do not add a debuff9 P5
switch-lock; `Quan Quanh` is the status that blocks pet switching.
