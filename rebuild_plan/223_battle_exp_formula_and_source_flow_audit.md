# 223 Battle EXP Formula And Source Flow Audit

Status: SOURCE AUDIT / FORMULA LOCK.

Scope:

- Prove how battle EXP is produced, committed, animated, and converted into
  level-up in the original source.
- Separate real formula EXP from forced smoke/lab setup.
- Classify direct EXP assignment paths.

Out of scope:

- Pixel-perfect P8 EXP marker placement from `pos.mid`.
- Full original-client frame compare.

## Source Files Read

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvSourceModels.java`
- `rebuild_game/src/main/java/VqsvBattleRenderer.java`

## High-Level Flow

```text
enemy-side pet is defeated
-> game.d.h(defeatedEnemy)
   -> calculates EXP into each recipient pet's pending field game.b.B
   -> adds recipients to display vector game.d.j
-> all enemies defeated
   -> game.d.X()
      -> commits B into real EXP field game.b.S
      -> clears B
      -> clears active battle marker d(false)
-> game.d state 8
   -> game.h.a(posX,posY) selects a game.d.j pet and starts EXP display
   -> game.h.am() animates display by J += 8
   -> if displayed EXP reaches next-level threshold, switch to state 22
-> game.d state 22
   -> game.h.an() opens /data/ui/levelUp.ui and calls game.b.v()
-> game.b.v()
   -> level++
   -> subtract threshold from S
   -> restore skill PP
   -> refresh stats
   -> call game.b.J() evolution queue producer
```

## EXP Storage Fields

| Source field | Meaning | Notes |
| --- | --- | --- |
| `game.b.S` | Real current EXP within current level | Serialized in pet payload index `7`. |
| `game.b.B` | Pending post-battle EXP delta | Filled by `game.d.h()`, committed by `game.d.X()`. |
| `game.d.x` | Direct battle participant vector | Controls direct EXP division. P5 switch adds the switched-in pet. |
| `game.d.j` | Post-battle EXP display vector | P8/P22/P23 iterate this vector. |
| `game.h.J` | P8 visual EXP increment accumulator | `am()` adds `8` per tick when not holding. |
| `game.b.R` | Displayed EXP cursor during P8 | Set by `j(int)`, read by `A()`. |

## Direct EXP Formula

Source: `game.d.h(b defeatedEnemy)`.

Constants from `game.d` constructor:

```text
aG = {10, 11, 12, 13, 15}
aH = {10, 12, 13, 14, 15, 16}
aI = {105, 100, 80, 60, 40, 20, 5}
```

For each direct participant `p` in `game.d.x`:

```text
enemyLevel = defeated.s()
enemyQuality = defeated.c[0]
participantCount = x.size()

base = (((enemyLevel << 1) * enemyLevel + 50)
        * aG[enemyQuality - 1] / 10) + 400

diff = participant.level - enemyLevel

if diff >= 6:
    levelFactor = aI[6]   # 5
else if diff > 0:
    levelFactor = aI[diff]
else if diff == 0:
    levelFactor = aI[1]   # 100
else:
    levelFactor = aI[0]   # 105

award = base / participantCount
        * aH[participantCount - 1]
        * levelFactor / 1000

if participant has form/status f(5):
    award = award * (aq.c[3][5][5] + 100) / 100

participant.B += award
if participant not in game.d.j:
    game.d.j.addElement(participant)
```

Important: this is Java integer arithmetic. Division truncates at each `/`.
Do not rewrite it as floating-point math.

## Reserve / Share EXP Formula

After direct participants, source loops all living party pets not already in
`x`.

If `game.g.B[7][0] == 2`:

```text
award = base / participantCount
        * aH[participantCount - 1]
        * levelFactorFromLastDirectParticipant / 3000
```

If the reserve pet has form/status `f(6)`:

```text
award = base / participantCount
        * aH[participantCount - 1]
        * levelFactorFromLastDirectParticipant / 1000
```

The source uses the last direct participant's level for these reserve factors,
not the reserve pet's own level. This is odd but source-confirmed.

## Commit: `game.d.X()`

Source:

```text
for each pet in game.d.j:
    if pet.S() alive:
        pet.g(pet.B)    # B -> S
        pet.B = 0
        pet.d(false)
    else:
        remove from j

if game.g.B[0][0] == 2 && game.g.B[0][1] == 1:
    heal all living party pets by speciesBaseHp * aq.c[2][0][6] / 100
```

`game.b.g(int)`:

```text
if level >= 50: return
S += amount
if S < 0: S = 0
```

## Level Threshold

Source: `game.b.u()` and private `game.b.A(level)`.

```text
nextThreshold = level >= 50 ? A(50) : A(level + 1)
A(level) = level * 15 * level - 200
```

On level-up, source calls `game.b.v()`:

```text
level++
S -= A(newLevel)
game.b.J()      # evolution queue producer
restore all skill PP to aq.c[1][skill][5]
refresh stats and clamp HP
```

## P8 EXP Animation

Source `game.h.a(int,int)`:

- Selects current `game.d.j[this.i]`.
- Skips max-level pets.
- Stores marker position from `game.d.am[0][4..5]`.
- Calls `pet.c()` and `pet.b(posX,posY)`.
- Resets hold counter `x = 0`.

Source `game.h.am()`:

```text
if holdCounter <= 0:
    visualIncrement J += 8

displayExp = pet.A() + J
threshold = pet.u()
targetExp = pet.z()

displayExp = min(displayExp, threshold, targetExp)

widget 40 text = displayExp + "/" + threshold
widget 9 text = "#P" + pet.v(displayExp)
widget 12 text = pet name
widget 13 text = "lv" + level
widget 17 sprite = 94 + element

if displayExp reaches threshold:
    pet.j(0)
    enter battle state 22
else if displayExp reaches targetExp:
    hold 10 ticks
    move to next game.d.j pet or exit battle
```

Therefore normal P8 EXP must not render full `/data/ui/levelUp.ui`; that UI
belongs to state 22 only.

## Example Calculations

### Elder Route Normal EXP

Current test setup:

```text
enemy level = 5
enemy quality = 1
participant level = 7
participant count = 1
```

Calculation:

```text
base = (((5 << 1) * 5 + 50) * 10 / 10) + 400
     = ((10 * 5 + 50) * 10 / 10) + 400
     = (100 * 10 / 10) + 400
     = 500

diff = 7 - 5 = 2
levelFactor = aI[2] = 80

award = 500 / 1 * aH[0] * 80 / 1000
      = 500 * 10 * 80 / 1000
      = 400
```

Next threshold for level 7 -> 8:

```text
A(8) = 8 * 15 * 8 - 200 = 760
```

If the pet starts at `EXP = 0`, then `0 + 400 < 760`, so it must not level up.

### Forced Level-Up Smoke

Some smoke/lab checks intentionally set:

```text
pet.S = A(nextLevel) - 10
```

Then any valid award larger than `10` should trigger state 22 and open
`levelUp.ui`. This is not normal gameplay; it is a focused level-up test.

## Direct EXP Assignment Paths

Search result classification:

| Path | Meaning | Classification |
| --- | --- | --- |
| `game.b` constructor `this.S = n2` | Load/create pet with stored EXP | SOURCE LOAD/INIT, not battle reward |
| `game.b.P()` payload index `7` | Save/export real EXP | SOURCE SAVE |
| `game.d.h()` | Calculates battle reward into `B` | SOURCE FORMULA |
| `game.d.X()` | Commits `B -> S` | SOURCE CONSUMER |
| `game.b.v()` | Subtracts level threshold via `g(-A(level))` | SOURCE LEVEL-UP |
| `game.b.h(int)` | Adds levels directly, not EXP | SOURCE LEVEL MUTATION, separate from battle EXP |
| Event/opcode direct fixed EXP reward | Not found in current battle EXP source path | UNKNOWN outside searched battle/source scope; do not assume without opcode proof |

## Rebuild Mapping

| Source concept | Rebuild mapping | Status |
| --- | --- | --- |
| `game.d.x` | `SourceBattleRuntime.sourceExpParticipants` | PORTED/PARTIAL |
| `game.d.j` | `SourceBattleRuntime.sourceExpDisplay` | PORTED/PARTIAL |
| `game.b.B` | `SourcePetState.sourcePendingExp` | PORTED/PARTIAL |
| `game.b.S` | `SourcePetState.sourcePayload[7]` / `BattleUnit.exp` | PORTED/PARTIAL |
| Direct formula constants | `sourceExpAward(...)` | PORTED |
| `f(5)` multiplier | `hasSourceFormStatus(5)` + status table param | PORTED/PARTIAL |
| reserve global `B[7][0]` share | `Scene.sourceGlobalState[7][0]` | PORTED/PARTIAL |
| reserve `f(6)` share | `hasSourceFormStatus(6)` | PORTED/PARTIAL |
| `game.d.X()` commit and passive heal | `consumeSourceExpAwards(...)` | PORTED/PARTIAL |
| P8 normal EXP visual | compact source-shaped EXP panel | PORTED/PARTIAL |
| P22 `levelUp.ui` | `VqsvBattleLevelUpView.leveled == true` | PORTED/PARTIAL |
| exact `pos.mid` marker placement | not yet ported/pixel-compared | PENDING |

## Current Smoke Evidence

- `battle_exp_normal_gain_no_levelup_anim`
  - proves Elder-style normal EXP = 400, no level-up.
- `battle_exp_levelup_ui`
  - proves forced-threshold P22 level-up UI still appears.
- `battle_exp_vector_active_only_regression`
  - proves one direct participant formula.
- `battle_exp_vector_p5_switch_two_participants`
  - proves P5 switched participant vector sharing.
- `battle_exp_vector_j_iterates_second_pet`
  - proves multi-pet `game.d.j` iteration.
- `battle_exp_levelup_learn_skill_done`
  - proves P23 learn-skill path after level-up.

## Remaining

- Exact P8 marker/pet sprite animation placement from `pos.mid` and
  `game.h.a(int,int)` remains pending.
- P8 compact EXP panel is source-shaped, not pixel-perfect.
- If a real manual route levels up unexpectedly, first inspect saved
  `sourcePayload[7]` before battle; the formula may be correct while the saved
  EXP state is already near threshold.

## Next

Audit and port exact P8 EXP marker placement:

1. Decode `pos.mid` row `game.d.am[0]`.
2. Map `game.h.a(am[0][4], am[0][5])` to rebuild coordinates.
3. Compare normal P8 EXP PNG before/after.
4. Keep `battle_exp_normal_gain_no_levelup_anim` and `battle_exp_levelup_ui`
   as the focused regression pair.
