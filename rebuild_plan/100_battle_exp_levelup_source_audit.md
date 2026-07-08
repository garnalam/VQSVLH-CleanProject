# 100 Battle EXP / levelUp Source Audit

## Scope

Current slice: port the final visible win EXP / levelUp path for the active battle pet.

Status after this slice:

- `PORTED/PARTIAL`: `game.d.h(b)` EXP award formula for one active participant.
- `PORTED/PARTIAL`: `game.b.g(int)`, `game.b.u()`, `game.b.v()` level threshold/subtract behavior.
- `PORTED/PARTIAL`: battle state `P8` EXP fill and `P22` level-up confirmation timing as a rebuild overlay.
- `PORTED/PARTIAL`: `/data/ui/levelUp.ui` renderer using source widget positions for title, sprite, EXP, old stats, new stats, and message.
- `PENDING`: full `game.d.x` participant list across switches/multiple active pets.
- `PENDING`: passive EXP share paths from event state `(7,0)` and buff/state `6`.
- `PORTED/PARTIAL`: learn-skill UI `choiceskill.ui` after `game.h.an()/ao()` when `v1.F()` is non-null; implemented and audited in `101_battle_levelup_learn_skill_evolution_audit.md`.
- `PENDING`: exact original-vs-rebuild pixel comparison for `levelUp.ui`.

## Source Chain

`game.d.a(b, boolean)`:

- On defeated enemy `b2`, source calls `b2.C()`, `b2.D()`, `this.S.b(b2)`, then `this.h(b2)`.
- `this.h(b2)` computes EXP from defeated enemy level/quality, participant count, party level factor, and adds to `b3.B`.
- When all enemies are defeated, source calls static `game.d.X()` and switches to state `8`.

`game.d.h(b)`:

- `n4 = defeated.s()`
- `n5 = ((n4 << 1) * n4 + 50) * aG[defeated.c[0] - 1] / 10 + 400`
- active participant award: `n5 / x.size() * aH[x.size() - 1] * aI[levelDiff] / 1000`
- source arrays:
  - `aG = {10, 11, 12, 13, 15}`
  - `aH = {10, 12, 13, 14, 15, 16}`
  - `aI = {105, 100, 80, 60, 40, 20, 5}`

`game.b`:

- `g(int)` adds EXP into `S`, clamped at zero and no-op at level 50.
- `u()` returns next threshold `A(T + 1)` or `A(50)` when maxed.
- `A(level) = level * 15 * level - 200`.
- `v()` increments level, subtracts `A(newLevel)`, refreshes stats/PP, and recalculates battle data.

`game.h`:

- state `8` calls `am()` to animate EXP bar by `J += 8`.
- if EXP reaches threshold, source switches battle owner to state `22`.
- state `22` calls `ao()`, which holds the level-up UI for 40 ticks or until confirm, then returns to state `8` if residual EXP remains.
- `an()` opens `/data/ui/levelUp.ui`, writes old stats into ids `19..22`, new stats into ids `31..34`, name id `38`, level id `40`, sprite widget `10`, and learn-skill message id `51`.

## Rebuild Notes

The rebuild currently maps only the active player pet to source `game.d.x`. This is intentional for the small verified slice and is traced as `participants=1 share/passive-exp=PENDING`.

Focused smoke:

- `battle_exp_levelup_ui`: active pet starts near level 6 threshold, enemy is defeated by existing active-queue death path, P8 opens `levelUp.ui`, pet level increases, and PNG captures the overlay.

Follow-up implemented in `101_battle_levelup_learn_skill_evolution_audit.md`:

- `battle_exp_levelup_choiceskill_ui`
- `battle_exp_levelup_learn_skill_done`
