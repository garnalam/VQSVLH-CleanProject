# 222 Battle EXP Normal Gain vs Forced Level-Up Audit

Status: SOURCE-BACKED CHECKPOINT ADDED.

Scope:

- Re-check battle EXP calculation and visible EXP animation after an enemy KO.
- Separate normal gameplay EXP from forced smoke/lab level-up setups.
- No client launch; PNG smoke only.

## Source facts

Source chain:

```text
enemy-side KO
-> game.d.h(defeatedEnemy)
   -> compute pending EXP into game.b.B for participants game.d.x
   -> add display pets into game.d.j
-> game.d.X()
   -> commit B -> S
   -> clear pending B / active marker
-> state 8
   -> game.h.am() animates EXP display by J += 8
   -> if display reaches threshold, enter state 22
-> state 22
   -> game.h.an() opens levelUp.ui and calls game.b.v()
```

Therefore:

- Defeating an enemy should award EXP only on the enemy-KO -> P8 path.
- Level-up should happen only if committed EXP reaches `game.b.A(T + 1)`.
- A smoke that seeds EXP at `threshold - 10` is a forced level-up test, not a
  default battle scenario.

## Rebuild status after this slice

| Concern | Status | Evidence |
| --- | --- | --- |
| Normal enemy KO EXP formula | PORTED/PARTIAL, VERIFIED | `battle_exp_normal_gain_no_levelup_anim` uses source formula helper and asserts no P22 level-up trace. |
| P8 EXP animation without level-up | PORTED/PARTIAL, VERIFIED | Same checkpoint stops during non-leveled EXP overlay with `expValue > 0`; renderer no longer draws `levelUp.ui` for this state. |
| P22 level-up UI separation | PORTED/PARTIAL, VERIFIED | `levelUp.ui` is drawn only when `VqsvBattleLevelUpView.leveled == true`, matching source `game.h.an()`. |
| Forced threshold level-up smoke | INTENTIONAL TEST SETUP | Existing `battle_exp_levelup_ui` sets `sourcePayload[7] = threshold - 10`. |
| Battle Lab scenario naming | FIXED | Added `npc.exp_normal_gain`; kept `npc.exp_levelup` as explicit forced-threshold scenario. |

## New checkpoint

`battle_exp_normal_gain_no_levelup_anim`:

- Player pet: species `17`, level `7`, EXP `0`.
- Enemy: species `68`, level `5`, quality `1`.
- Expected award uses source formula:

```text
sourceExpectedExpAward(enemyLevel=5, enemyQuality=1, participantLevel=7, participantCount=1)
```

- Assert:
  - current state is P8 with EXP overlay;
  - EXP display is animating (`expValue > 0`);
  - committed pet EXP equals expected award;
  - pet level remains `7`;
  - expected award is below next threshold;
  - no `P22 game.h.an/ao levelUp` trace appears.

Renderer correction:

- Before this audit, rebuild reused the full `levelUp.ui` overlay for normal
  P8 EXP gain, so a non-level-up frame still showed the title `Thăng cấp` and
  duplicate old/new stat rows.
- Source `game.h.am()` does not open `/data/ui/levelUp.ui`; it updates EXP text
  and progress widgets for the selected `game.d.j` pet.
- The renderer now uses a compact source-shaped EXP panel for `leveled=false`,
  and keeps full `levelUp.ui` only for real P22 level-up.

## Battle Lab

New NPC scenario:

```text
run_battle_lab_smoke.cmd -Lane npc -Scenario exp_normal_gain
```

Existing scenarios remain:

```text
exp_levelup     -> forced threshold level-up UI
exp_learn_skill -> forced threshold + learn-skill UI
```

## Remaining

- `levelUp.ui` is still source-shaped, not pixel-perfect.
- Exact original-client frame comparison for EXP bar movement and the source
  `pos.mid` marker placement is still pending.
- If a real route still levels up unexpectedly, capture the route state/EXP
  before battle; the likely cause will be saved pet EXP already near threshold,
  not the normal formula path.

## Next

If continuing EXP work, audit the real route save/load EXP state before Elder
and Battle Lab entry, then add route-specific smoke that proves the pet enters
battle with the expected EXP value before KO.
