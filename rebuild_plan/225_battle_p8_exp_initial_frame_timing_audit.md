# 225 Battle P8 EXP Initial Frame Timing Audit

Status: SOURCE AUDIT + SMALL PORT.

Scope:

- Audit why rebuild captured `8/760` on the first normal P8 EXP PNG.
- Map the source order around `game.d` state 8 entry and `game.h.am()`.
- Patch rebuild so the first rendered P8 EXP frame shows the pre-increment
  value, e.g. `0/760`, then increments by `8` on the following tick.

Out of scope:

- Original-client pixel-perfect frame compare.
- Changing the EXP formula or level-up threshold logic.

## Source Timing

`game.d.a(byte)` state 8 entry:

```text
this.al[0].b(am[0][6], am[0][7]);
this.S.a((int)am[0][4], (int)am[0][5]);
```

`game.h.a(int,int)`:

```text
selects game.d.j[this.i]
stores H/I
pet.c()
pet.b(i1, i2)
this.x = 0
```

It does not add EXP. The visual increment happens later in `game.h.am()`:

```text
if (this.x <= 0) {
    this.J += 8;
}
i2 = pet.A() + this.J;
widget 40 = i2 + "/" + threshold
widget 9 = "#P" + pet.v(i2)
```

Therefore source has a state-entry moment after `game.h.a(...)` and before the
first `game.h.am()` increment. The visible HUD can show the start value
(`0/760` in the normal Elder-style smoke) before the first animated step
(`8/760`).

## Rebuild Bug

Before this slice, `SourceBattleRuntime.tickWinExpLevelUp()` selected the
current EXP display pet and then immediately continued into:

```text
expDisplayValue = min(target, expDisplayValue + 8)
```

That merged source state-entry and first `game.h.am()` tick into one rebuild
tick. As a result, the focused PNG captured `8/760` instead of the start frame
`0/760`.

## Port

Added `expInitialFramePending`:

- set to `true` in `selectCurrentExpDisplayPet(...)`, the rebuild equivalent of
  `game.h.a(...)`;
- on the next `tickWinExpLevelUp(...)` pass, render the HUD with the current
  `expDisplayValue` and return without adding `8`;
- clear the flag, so the following tick resumes the source `game.h.am()`
  animation increment.

The smoke checkpoint `battle_exp_normal_gain_no_levelup_anim` now stops on this
initial frame and asserts:

```text
battleState == P8
battleUiMode == levelup
levelUpView.leveled == false
levelUpView.expValue == 0
trace contains "game.h.a initial render before game.h.am"
```

Battle Lab now exposes this as a focused NPC suite:

```text
run_battle_lab_suite_smoke.cmd -Lane npc -Suite exp_animation
```

Scenarios:

| Battle Lab scenario | Checkpoint | Expected frame |
| --- | --- | --- |
| `exp_frame0` | `battle_exp_p8_frame0` | `0/760` |
| `exp_frame1` | `battle_exp_p8_frame1` | `8/760` |
| `exp_mid` | `battle_exp_p8_mid` | `80/760` |
| `exp_target_hold` | `battle_exp_p8_target_hold` | `400/760` hold before exit |
| `exp_levelup` | `battle_exp_levelup_ui` | state 22 `levelUp.ui` |
| `exp_learn_skill` | `battle_exp_levelup_learn_skill_done` | P23 learn-skill path |

## Status

| Item | Status |
| --- | --- |
| Source state-entry timing audited | PORTED |
| First P8 HUD frame `0/760` | PORTED/PARTIAL |
| Subsequent `+8` animation | PORTED/PARTIAL, existing P8 loop |
| Exact original-client frame compare | PENDING |

## Next

If this visual path needs more polish, capture an original-client frame sequence
for P8 normal EXP and compare:

```text
frame 0: 0/760
frame 1: 8/760
...
target: 400/760, then hold 10 ticks
```
