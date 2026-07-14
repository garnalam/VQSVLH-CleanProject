# 296 - Battle Debuff8 Quy Mi Closeout

Scope: dedicated closeout for debuff id `8` / `Quy Mi`.

## Plain Gameplay Explanation

`Quy Mi` is now an intentional gameplay-fixed status, approved by the user.
It is not being claimed as exact source parity.

When a pet has `Quy Mi`:

- its outgoing damage is increased by `10%`;
- each attack target is rerouted by a simple 1v1 rule:
  - roll `< 55`: hit itself;
  - roll `>= 55`: hit the opponent.

In plain words: the affected pet becomes more dangerous, but unstable. It hits
harder, while having a higher chance to hurt itself than to hit the enemy.

## Source Facts Kept

```text
skill54 = [5,171,583,0,1,10,2,8,40,0]
debuff8 = [319,330,4]
effect.mid[54] = [0,1,0,0,-1,-1,0]
bufDebuf ar[1][8] = [1,0,0,-1,0,25,0,-1]
game.d.ai[1] includes debuff ids [0,1,2,3,8,9,10]
```

Historical source audit found that skill `54` is a zero-power special route.
It must not be treated as an ordinary direct-hit debuff producer.

The old source-shaped active target route was:

```text
if skill[9] == 0
and attacker.hasDebuff8
and ae.a(100) > skill[8]:
    game.d.f(attacker)
```

That source path is documented, but the current rebuild gameplay intentionally
supersedes it with the user-approved 55/45 1v1 rule above.

## Implemented

Runtime patch:

- `BattleUnit.computeDamage()` applies the user-approved outgoing damage hook:
  `damage += damage * 10 / 100` when the attacker has debuff8.
- `SourceBattleRuntime.prepareTargetList()` applies the user-approved target
  route for active debuff8:
  - target vector becomes `[self, opponent]`;
  - roll `< 55` selects self;
  - roll `>= 55` selects opponent.
- Skill54 ordinary debuff producer remains `NOT_REACHED`.
- P12/P13 body visual and expiry remain source-shaped from the debuff table.

## Smoke Checkpoints

```text
battle_status_debuff8_before_no_effect
battle_status_debuff8_skill54_zero_power_no_apply
battle_status_debuff8_skill54_zero_power_buff14_no_apply
battle_status_debuff8_gameplay_fixed_self_hit_damage_up
battle_status_debuff8_gameplay_fixed_enemy_hit_damage_up
battle_status_debuff8_p12_body_visual_type1_actor25
battle_status_debuff8_expiry_clears_icon
```

## Numeric Smoke Results

| Slice | Before | During / after |
| --- | --- | --- |
| before/no effect | no debuff8 | enemy status icon count `0` |
| skill54 producer | enemy HP `109/109` | zero-power guard, no HP damage, no hitroll, no `appliedDebuffId=8` |
| buff14 regression | target has buff14 icon `26/137` | skill54 still no ordinary debuff8 apply; buff14 is not the deciding gate |
| gameplay self-hit | forced roll `0`, attacker has icon `9/138` | target is self; damage `101 -> 111` after +10% |
| gameplay enemy-hit | forced roll `99`, attacker has icon `9/138` | target is opponent; damage `80 -> 88` after +10% |
| P12/P13 body visual | debuff8 duration `4`, HP `50` | visual row `[1,0,0,-1,0,25,0,-1]` starts; body visual visible |
| expiry | duration `4`, HP `50` | duration `4 -> 3 -> 2 -> 1 -> 0`, HP unchanged, icon cleared |

## PNG Output

Focused gameplay-fix PNGs:

```text
rebuild_game/build_intro_demo/debuff8_gameplay_fixed/battle_status_debuff8_gameplay_fixed_self_hit_damage_up.png
rebuild_game/build_intro_demo/debuff8_gameplay_fixed/battle_status_debuff8_gameplay_fixed_enemy_hit_damage_up.png
```

Closeout PNGs are also part of `battle_quick` after this update.

## Classification

```text
Skill54 visual / zero-power route: PORTED/PARTIAL
Skill54 ordinary debuff8 producer: NOT_REACHED
Debuff8 active gameplay consumer: INTENTIONAL_DEVIATION / GAMEPLAY_FIXED
Outgoing damage +10%: INTENTIONAL_DEVIATION / GAMEPLAY_FIXED
Target route 55% self / 45% opponent: INTENTIONAL_DEVIATION / GAMEPLAY_FIXED
P12/P13 body visual from bufDebuf row: PORTED
Tick no-op + duration expiry icon clear: PORTED
Full multi-active source target divergence: SUPERSEDED_BY_GAMEPLAY_FIX
Pixel-perfect original comparison: PENDING
```

## Verification

Current focused verification:

```text
build.ps1 PASS
focused debuff8 gameplay-fix PNG smoke 2/2 PASS
```

Full regression for this slice must include:

```text
com.vqsv.rebuild.Main --check
VqsvBattleDamageFormulaCheck
battle_quick
Java/rebuild_plan mojibake scan for touched files
```

## Next Step

After this gameplay-fix closeout, continue the battle-effect roadmap in table
order. The next recommended lane remains debuff10 `Te Liet` action-delay parity,
unless the user asks to tighten Hỗn Loạn / Quỷ Mị multi-active Battle Lab
fixtures first.
