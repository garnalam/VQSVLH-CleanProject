# 118 Battle EXP Participant / Share Vector Matrix

Status: SOURCE AUDIT / NO CODE CHANGE.

Scope:

- Audit source `game.d` states `P8`, `P22`, `P23`.
- Audit `game.d.x` participant vector and `game.d.j` EXP/level-up display vector.
- Audit source `game.b` EXP, level-up, learn-skill, evolution queue producer.
- Compare with current rebuild `VqsvBattleRuntime` / `BattleUnit`.

This doc intentionally moves away from P21/P17 catch. Do not use catch/UI
pending items as blockers for this phase.

## Source Files Read

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleUnit.java`
- `rebuild_game/src/main/java/VqsvSourceModels.java`

Related existing docs:

- `rebuild_plan/100_battle_exp_levelup_source_audit.md`
- `rebuild_plan/101_battle_levelup_learn_skill_evolution_audit.md`
- `rebuild_plan/103_battle_levelup_evolution_queue_matrix.md`
- `rebuild_plan/battle_engine_master_roadmap_progress.md`

## Source State Chain

```text
enemy defeated
-> game.d.h(defeatedEnemy) computes pending EXP into game.b.B
-> game.d.X() commits pending EXP into game.b.S and prepares game.d.j
-> state P8
   enter: game.k.a().M.l = 0; partial HP restore; setup marker/UI
   tick: game.h.am() animates EXP bar for each game.d.j pet
-> if EXP reaches threshold: state P22
   enter: game.h.an()
      old stats -> levelUp.ui
      game.b.v()
         level++
         subtract threshold
         game.b.J() evolution queue producer
         restore skill PP
         refresh stats
      new stats -> levelUp.ui
      possible learn-skill message
   tick: game.h.ao()
      wait 40 ticks or confirm
      if learn candidates: state P23
      else return P8 for residual EXP / next pet, or exit battle
-> state P23
   enter: game.h.ap() choiceskill.ui
   tick: game.h.aq() confirm learn skill or return P8/exit
```

## `game.d.x` Participant Vector

Source fields:

| Field | Meaning | Source evidence | Status |
| --- | --- | --- | --- |
| `private static Vector x` | Participants that directly took part and share defeated enemy EXP. | Constructed in `game.d` constructor; cleared in `f()`; used in `h(b)` as divisor `x.size()`. | SOURCE CONFIRMED |
| `public static Vector j` | Pets that have pending/displayed EXP/level-up UI after battle. | Cleared in `f()`; filled in `h(b)`; used by `game.h.am/an/ao/ap/aq`. | SOURCE CONFIRMED |
| `game.b.B` | Pending EXP delta for a pet before `X()` commits it to `S`. | `h(b)` adds to `B`; `X()` calls `g(B)` then zeros `B`. | SOURCE CONFIRMED |

Participant creation/removal source facts:

| Source path | Behavior | Classification |
| --- | --- | --- |
| Battle setup | Extra active player-side combatants are added to `x` when `this.c(this.f[n]).S()` is selected into battle slots. | SOURCE CONFIRMED / NEEDS FULL CONTEXT |
| P5 switch `game.d.a(int)` | When player switches a healthy pet to front, if not already in `x`, add it to `x`; mark pet `J=true`, `d(true)`. | SOURCE CONFIRMED |
| Player pet defeated | Source removes defeated player pet from `x` and `j`, zeros pending EXP `B`, clears active flags. | SOURCE CONFIRMED |
| Enemy defeated | Source calls `h(defeatedEnemy)`, which uses current `x.size()` and participants in `x`. | SOURCE CONFIRMED |
| After battle win | `game.d.X()` commits `B` to real EXP `S` for living `j` entries. Dead `j` entries are removed. | SOURCE CONFIRMED |

Important implication:

- EXP sharing is not just active slot `f[0]`.
- Any switched-in participant in `x` should divide the direct EXP share.
- `j` can also contain non-participants added by passive/share paths.

## Source EXP Formula: `game.d.h(b defeated)`

Source constants in `game.d` constructor:

```text
aG = {10, 11, 12, 13, 15}
aH = {10, 12, 13, 14, 15, 16}
aI = {105, 100, 80, 60, 40, 20, 5}
```

For defeated enemy `b2`:

```text
enemyLevel = b2.s()
base = (((enemyLevel << 1) * enemyLevel + 50)
        * aG[b2.c[0] - 1] / 10) + 400
participantCount = x.size()
levelFactor:
  participant.level - enemyLevel >= 6 -> aI[6]
  participant.level - enemyLevel > 0  -> aI[diff]
  equal                            -> aI[1]
  participant lower                 -> aI[0]
directAward = base / participantCount
              * aH[participantCount - 1]
              * levelFactor / 1000
if participant has buff/status 5:
  directAward *= (aq.c[3][5][5] + 100) / 100
participant.B += directAward
if not in j: j.addElement(participant)
```

Classification:

- Formula shape: `SOURCE CONFIRMED`.
- Current rebuild active-only formula: `PORTED/PARTIAL`.
- Multi-participant `x.size()` division and `aH[count-1]`: `PENDING` in rebuild runtime.
- Buff/status `5` EXP multiplier: `PENDING` unless already represented in
  `BattleUnit` active status for EXP.

## Passive / Share EXP Paths

After direct participants, source loops all player pets:

```java
for (n3 = 0; n3 < game.d.p.A; ++n3) {
    if (!this.c(n3).S() || x.contains(this.c(n3))) continue;
    if (p.c((byte)7, (byte)0) == 2) {
        award = base / x.size() * aH[x.size() - 1] * levelFactor / 3000;
        pet.B += award;
        pet.B += award;
        pet.c();
        if (!j.contains(pet)) j.addElement(pet);
        continue;
    }
    if (!this.c(n3).f((byte)6)) continue;
    award = base / x.size() * aH[x.size() - 1] * levelFactor / 1000;
    pet.B += award;
    pet.c();
    if (!j.contains(pet)) j.addElement(pet);
}
```

Matrix:

| Source condition | Target pets | Award | Current rebuild |
| --- | --- | --- | --- |
| `p.c(7,0) == 2` | Living non-participants not in `x` | Formula uses `/3000`, then adds twice to `B` | PENDING |
| `pet.f(6)` | Living non-participants with form/status `6` | Formula uses `/1000`, adds once | PENDING |
| Direct participant has `f(5)` | Participant in `x` | Direct award multiplied by status table `aq.c[3][5][5]` | PENDING/PARTIAL |

Open question:

- The `p.c(7,0)==2` path adds the computed award twice. This appears in the
  decompiled source. Do not "fix" it unless original bytecode proves otherwise.

## `game.d.X()` Commit / Cleanup

Source:

```java
for each pet in j:
    if pet.S():
        pet.g(pet.B)   // add pending EXP into S
        pet.B = 0
        pet.d(false)
    else:
        j.removeElementAt(i)

if p.c(0,0) == 2 && p.c(0,1) == 1:
    for each living bag pet:
        heal HP by aq.c[0][species][5] * aq.c[2][0][6] / 100
```

Matrix:

| Behavior | Meaning | Current rebuild |
| --- | --- | --- |
| `B -> S` commit | Pending EXP is committed after win, before P8 UI animates from old to new EXP. | APPROX/PORTED for active pet only |
| dead `j` removal | Dead pets do not receive post-battle EXP UI commit. | PENDING for multi-pet |
| `d(false)` | Clears battle-active flag after EXP commit. | PENDING/PARTIAL |
| global heal from `p.c(0,0/1)` | Post-win passive/team heal. | PENDING |

## P8 Source UI / Flow: `game.h.a(int,int)` and `game.h.am()`

Source P8 entry:

| Source | Behavior |
| --- | --- |
| `game.d.a(8)` | Sets `game.k.a().M.l = 0`; partial HP restore for participants; positions marker; calls `S.a(am[0][4], am[0][5])`. |
| `game.h.a(int,int)` | Chooses current `game.d.j[this.i]`, skips max-level pets, starts sprite/marker and resets animation counters. |
| `game.h.am()` | Increments EXP display by `J += 8`; updates widget `40` and progress `#P`; if threshold reached, enters P22. |

P8 tick behavior:

| Condition | Source next |
| --- | --- |
| `this.i >= game.d.j.size()` | exit battle (`game.i.a().a(10)`) |
| displayed EXP reaches next threshold | state P22 |
| displayed EXP reaches post-award target but no level-up | hold for 10 ticks, then next pet or exit |
| confirm key | fast-forwards to threshold/target depending current values |

Current rebuild:

- `tickWinExpLevelUp()` animates one active pet's EXP by `+8`, matching the
  source step shape.
- It does not iterate through full `game.d.j`.
- It does not skip max-level pets in a source vector.

Status: `PORTED/PARTIAL`.

## P22 Source Level-Up: `game.h.an()` / `game.b.v()` / `game.h.ao()`

Source `game.h.an()`:

| Step | Behavior | Current rebuild |
| --- | --- | --- |
| Select pet | `v1 = game.d.j[this.i]` | active pet only |
| Old stats | read `v1.c[1..4]` into levelUp UI ids `19..22` | PORTED/PARTIAL |
| Level up | calls `v1.v()` | PORTED/PARTIAL |
| Fill UI | opens `/data/ui/levelUp.ui`, writes new stats ids `31..34`, name id `38`, level id `40`, sprite widget `10` | PORTED/PARTIAL |
| Learn prompt | if `E()<5 && E()<level/10+1`, sets `this.y=v1.F()` and widget `51` text | PORTED/PARTIAL |

Source `game.b.v()`:

```text
++T
g(-A(T))
J()                 // evolution queue producer
restore every skill PP to aq.c[1][skill][5]
V()                 // recompute stats/clamp current HP
```

Current rebuild:

- `BattleUnit.sourceLevelUpOnce()` increments level, subtracts threshold, restores PP, refreshes stats.
- `VqsvBattleRuntime.produceSourceEvolutionQueue()` is called immediately after level-up to mirror `game.b.v() -> J()`.

Status: `PORTED/PARTIAL`.

Source `game.h.ao()`:

| Condition | Next |
| --- | --- |
| after 40 ticks or confirm, `this.y != null` | P23 learn-skill |
| no learn skill and current pet still has residual EXP `z()>0` | P8 again |
| no residual and more pets in `j` | P8 next pet |
| no residual and last pet | exit battle |

Current rebuild:

- Handles repeated level-up for active pet if residual EXP remains.
- Does not iterate multiple `j` pets.

Status: `PORTED/PARTIAL`.

## P23 Learn Skill: `game.h.ap()` / `game.h.aq()`

Source `game.b.F()`:

```text
learnGroup = aq.c[0][species][18]
element = aq.c[0][species][1]
tier = W() from level thresholds
candidate skills = element*10 .. element*10+9
include if aq.c[1][skill][4] <= aq.c[8][learnGroup][tier]
exclude already-known skills
```

Source P23:

| Step | Behavior | Current rebuild |
| --- | --- | --- |
| Enter | `game.h.ap()` opens `/data/ui/choiceskill.ui`, closes `levelUp.ui`, fills rows via `bp()` | PORTED/PARTIAL |
| Confirm first time | opens `msgwarm.ui` with `"Học tập" + skillName` | PORTED/PARTIAL |
| Confirm warning | calls `v1.g(skill)`, clears `y`, closes msgwarm/choiceskill, returns P8 or exits | PORTED/PARTIAL |
| Back/skip | Source has UI/input paths around `aq()`; exact skip semantics need care | APPROX in rebuild |

Current rebuild:

- Candidate skill list and skill add are implemented.
- Back/skip is traced as approximate.
- Full `choiceskill.ui` widget runtime remains partial.

## Evolution Queue Producer: `game.b.J()`

Source facts already audited in `103`, reconfirmed here because P22 calls it:

| Source | Behavior | Current rebuild |
| --- | --- | --- |
| `game.b.v()` | calls `J()` immediately after level increment/subtract EXP | PORTED/PARTIAL |
| `aq.c[0][species][19]` | target species; `-1` means no target | PORTED/PARTIAL |
| `R()` | target kind gate: kind `1/2 -> 1`, kind `3 -> 2`, otherwise `0` | PORTED/PARTIAL |
| `game.k.H` | queue notice `{currentSpecies, currentNameTextId}` | PORTED/PARTIAL |
| `game.k.L[0/1]` | stores level/species for later world/tutorial bridge | PORTED/PARTIAL |
| `game.k.I=0` | marks pending queue consumer | PORTED/PARTIAL |
| material columns `20/21` | used later in evolve confirm UI | PORTED/PARTIAL metadata |

Status:

- Producer exists in rebuild.
- World notice/evolve UI path has later docs/smokes, but is outside this
  participant/share vector audit.

## Current Rebuild Gap Matrix

| Concern | Current rebuild status | Needed next code slice |
| --- | --- | --- |
| Direct active-pet EXP formula | PORTED/PARTIAL | Keep. |
| `game.d.x` participant list | MISSING/PENDING | Add source participant vector/order at runtime. |
| Multi-participant divisor/share | MISSING/PENDING | Use `participantCount = x.size()` and apply `aH[count-1]`. |
| P5 switch adds pet to `x` | PENDING | Hook existing P5 switch success into participant vector. |
| Player defeated removes from `x/j` | PENDING | Wire when P5 forced replacement/death cleanup runs. |
| `game.d.j` multi-pet EXP UI iteration | MISSING/PENDING | P8 should iterate through all `j`, skipping max-level pets. |
| Pending EXP field `B` equivalent | MISSING/PENDING | Add per-pet pending EXP delta before commit. |
| `game.d.X()` commit | PARTIAL | Active pet only currently; generalize to all `j`. |
| Passive/share `p.c(7,0)==2` | MISSING/PENDING | Needs source state mapping to rebuild event/global state. |
| Passive/status `f(6)` share | MISSING/PENDING | Needs field/status source mapping on reserve pets. |
| Direct EXP multiplier `f(5)` | MISSING/PENDING | Needs status/form mapping in `BattleUnit` EXP path. |
| Post-win passive heal `p.c(0,0)==2 && p.c(0,1)==1` | MISSING/PENDING | Audit global source state before coding. |
| P22/P23 active pet level/learn | PORTED/PARTIAL | Keep; do not rewrite unless vector work requires it. |
| P23 back/skip exact source | APPROX | Separate UI/input slice later. |

## Recommended Next Code Slice

Do not jump to UI polish. The clean next slice is:

### Slice A: Add EXP Participant Vector Backbone

Goal:

- Add runtime equivalents for:
  - `game.d.x`: direct participants.
  - `game.d.j`: pets with pending/displayed post-battle EXP.
  - `game.b.B`: pending EXP delta per source pet.
- Initially support current single active pet plus P5-switched pet.
- Do not implement passive/share yet.

Required source-backed behavior:

1. At battle start, active player pet is in `x`.
2. On successful P5 switch, switched-in pet is added to `x` if alive and not already present.
3. On enemy defeated, compute direct EXP for every `x` participant using `x.size()`.
4. Add each participant to `j` if not present.
5. On P8, commit pending EXP from `B` into each living `j` pet and iterate UI one pet at a time.

Focused smoke plan:

- `battle_exp_single_participant_regression.png`
- `battle_exp_two_participants_share_vector.png`
- `battle_exp_p5_switched_pet_gets_share.png`
- `battle_exp_j_iterates_second_pet_levelup.png`

Regression after code:

- build/check/mojibake scan.
- Existing:
  - `battle_exp_levelup_ui`
  - `battle_exp_levelup_choiceskill_ui`
  - `battle_exp_levelup_learn_skill_done`
  - Sophie/Bunny/Elder route smoke.

### Slice B: Passive / Share EXP

Only after Slice A:

- Port `p.c(7,0)==2` share path after mapping the rebuild global/event state.
- Port reserve-pet `f(6)` share after mapping source form/status 6 on non-active pets.
- Port participant `f(5)` EXP multiplier.

### Slice C: P8 Multi-Pet UI Polish

Only after Slice A proves data:

- Make `levelUp.ui`/EXP animation iterate all `j` pets like `game.h.am()`.
- Keep existing one-line marquee/text fixes intact.

## Honest Current Conclusion

The current rebuild has a working visible active-pet post-battle path:

- P8 EXP fill: `PORTED/PARTIAL`.
- P22 level-up UI: `PORTED/PARTIAL`.
- P23 learn-skill: `PORTED/PARTIAL`.
- Evolution queue producer/consumer work: handled by docs `103+`.

But it is not source-complete because source EXP is built around `game.d.x`,
`game.d.j`, and per-pet pending EXP `B`. The next serious battle-engine step is
not another catch/UI loop; it is the participant vector backbone.

## Implementation Update: Slice A

Status: PORTED/PARTIAL.

Files changed:

- `rebuild_game/src/main/java/VqsvSourceModels.java`
- `rebuild_game/src/main/java/VqsvBattleRuntime.java`

Implemented source-shaped equivalents:

| Source concept | Rebuild field/runtime | Status |
| --- | --- | --- |
| `game.d.x` direct participant vector | `sourceExpParticipants` in `SourceBattleRuntime`; per-pet `sourceExpParticipant` flag | PORTED/PARTIAL |
| Initial active pet in `x` | battle entry adds `sourcePets[0]` | PORTED/PARTIAL |
| P5 switched-in pet added to `x` | successful `tickPetSwitch()` adds selected `SourcePetState` object after reorder | PORTED/PARTIAL |
| Remove dead participant/display pet | `pruneSourceExpVectors(...)` clears dead pets and pending EXP | PORTED/PARTIAL |
| `game.b.B` pending EXP | `SourcePetState.sourcePendingExp` | PORTED/PARTIAL |
| EXP display start / `A()` equivalent | `SourcePetState.sourceExpStart` | PORTED/PARTIAL |
| `game.d.j` EXP display vector | `sourceExpDisplay` in `SourceBattleRuntime`; per-pet `sourceExpDisplay` flag | PORTED/PARTIAL |
| Direct EXP division by `x.size()` | `sourceExpAward(enemy, unit, participantCount)` now uses actual participant count | PORTED/PARTIAL |
| `game.d.X()` commit `B -> S` | `prepareSourceExpAwards()` commits pending EXP into each display pet payload before P8 UI | PORTED/PARTIAL |
| P8 multi-pet iteration | `selectCurrentExpDisplayPet()` / `finishCurrentExpPet()` walk `sourceExpDisplay` | PORTED/PARTIAL |

Deliberately still pending:

- Passive/share `p.c(7,0)==2`.
- Reserve pet status/form `f(6)` share.
- Direct participant `f(5)` EXP multiplier.
- Post-win passive heal `p.c(0,0)==2 && p.c(0,1)==1`.
- Exact `game.h.am()` sprite marker/position timing for multi-pet UI.
- Dedicated smoke PNG for two-participant share was not run in this slice
  because the user explicitly asked not to run game jar/client.

Verification performed:

- `powershell -ExecutionPolicy Bypass -File .\build.ps1`: pass.
- Java source mojibake scan: pass.

No live client/game was opened.

Next roadmap-consistent slice:

1. Add a non-jar/unit-style check or user-approved smoke for:
   - active-only regression,
   - P5 switched pet share,
   - `game.d.j` second-pet iteration.
2. Then implement Slice B passive/share EXP only after mapping the source global
   state for `p.c(7,0)` and pet status/form `6`.
