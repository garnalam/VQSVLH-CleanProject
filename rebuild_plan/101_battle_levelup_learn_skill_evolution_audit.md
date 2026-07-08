# 101 Battle LevelUp Learn-Skill / Evolution Audit

## Scope

This audit covers what source updates after a battle level-up and the next UI branch for learning a new skill.

Implemented in this slice:

- `PORTED/PARTIAL`: after `levelUp.ui`, if the leveled pet can learn a new skill, rebuild opens `choiceskill.ui`.
- `PORTED/PARTIAL`: candidate skill list mirrors `game.b.F()`.
- `PORTED/PARTIAL`: confirm prompt mirrors `game.h.aq()` message flow and then adds the skill with `game.b.g(byte)`.
- `PENDING`: exact `msgwarm.ui` renderer for the learn confirmation; rebuild uses existing warning overlay.
- `PENDING`: exact `choiceskill.ui` pixel compare; current renderer is source-backed partial.
- `PENDING`: evolution queue triggered by `game.b.J()` / `game.k.H` / `game.k.L` / `game.k.I`.

## Source Chain

### Level Up Stat Update

`game.h.an()`:

- Takes old visible stats `v1.c[1..4]` and writes them to `levelUp.ui` ids `19..22`.
- Calls `v1.v()`.

`game.b.v()`:

- Increments `T` level.
- Subtracts `A(T)` from EXP `S`.
- Calls `J()` to enqueue possible evolution.
- Restores existing skill PP from source skill max.
- Calls `V()` to recompute stats and clamp current HP.

`game.h.an()` then:

- Calls `this.g(v1)` to fill current pet info.
- Writes new stats to `levelUp.ui` ids `31..34`.
- Writes pet name id `38`, level id `40`, sprite widget `10`.

### Learn-Skill Trigger

`game.h.an()` checks:

```java
if (v1.E() < 5 && v1.E() < v1.s() / 10 + 1) {
    this.y = v1.F();
    widget51 = "Co the hoc tap ky nang moi";
}
```

Meaning:

- `E()` is current skill count `O`.
- A pet can learn if it has fewer than 5 skills and fewer than `level / 10 + 1` skills.
- `F()` builds learnable skills from species element and learn group.

`game.b.F()`:

- `s2 = aq.c[0][species][18]` learn group.
- `s3 = aq.c[0][species][1]` element.
- `n3 = W()` tier from level thresholds `{5,10,20,30,40}`.
- Candidate ids are `element * 10 .. element * 10 + 9`.
- Include only skills whose `aq.c[1][skill][4] <= aq.c[8][learnGroup][tier]`.
- Exclude already-known skills.

### Learn-Skill UI

`game.h.ao()`:

- After 40 ticks or confirm on `levelUp.ui`, if `this.y != null`, switches battle owner to state `23`.

`game.d` state `23`:

- enter: `this.S.ap()`.
- tick: `this.S.aq()`.

`game.h.ap()`:

- Opens `/data/ui/choiceskill.ui`, closes `/data/ui/levelUp.ui`.
- Sets list length to `this.y.length`.
- Populates visible rows via `bp()`.

`game.h.bp()`:

- Row widgets `13 + i*5` show skill name from `aq.c[1][skill][1]`.
- Row widgets `14 + i*5` show PP max from `aq.c[1][skill][5]`.
- Widget `53` shows description from `aq.c[1][skill][2]`.

`game.h.aq()`:

- Up/down moves selection.
- Confirm opens `msgwarm.ui` with `"Hoc tap" + skillName`.
- Confirm again calls `v1.g((byte)this.y[this.h])`.
- Clears `this.y`, closes `msgwarm.ui` and `choiceskill.ui`, then returns to state `8` or exits battle if done.

## Evolution Queue

`game.b.v()` calls `J()` before refreshing stats. `J()` may enqueue evolution:

- If species has evolution target at `aq.c[0][species][19]`.
- Checks required level from `t[...]`.
- May check required event/item state through `game.g.o().a(...)`.
- Pushes `{currentSpecies, speciesNameTextId}` to `game.k.H`.
- Stores `game.k.L[0] = level`, `game.k.L[1] = species`, and `game.k.I = 0`.

This is not the same UI as `levelUp.ui` / `choiceskill.ui`; it belongs to later `game.k` evolution handling. Rebuild keeps it `PENDING` until that source path is audited with its UI and route.

## Smoke Coverage

- `battle_exp_levelup_ui`: P8 levelUp overlay.
- `battle_exp_levelup_choiceskill_ui`: confirm levelUp, then `choiceskill.ui` opens with learn candidates.
- `battle_exp_levelup_learn_skill_done`: confirm first candidate, then pet payload contains learned skill.

