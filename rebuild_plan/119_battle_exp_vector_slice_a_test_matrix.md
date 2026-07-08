# 119 Battle EXP Vector Slice A Test Matrix

Status: TEST MATRIX / CHECKPOINTS PASS.

Purpose:

- Close out Slice A by defining exact tests for the new EXP participant vector
  backbone.
- Do not move to passive/share EXP until these cases are covered.
- Do not open live client/game. Any future smoke must be PNG/headless only and
  only if explicitly approved.

Related source/audit:

- `rebuild_plan/118_battle_exp_participant_share_vector_matrix.md`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`

Related rebuild code:

- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvSourceModels.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

## Slice A Behavior Under Test

Implemented Slice A behaviors:

| Source concept | Rebuild trace/source | Must test |
| --- | --- | --- |
| `game.d.x` reset | `battle EXP reset source vectors game.d.x/game.d.j/game.b.B` | yes |
| Active pet added to `x` at battle start | `battle EXP game.d.x add ... reason=battle entry active f[0]` | yes |
| P5 switched pet added to `x` | `battle EXP game.d.x add ... reason=P5 game.d.a(slot) switched-in pet` | yes |
| EXP computed per participant | `battle P8 game.d.h direct EXP ... participants=<n>` | yes |
| Pending EXP `B` committed | `battle P8 game.d.X commit B->S ...` | yes |
| P8 selects entries from `game.d.j` | `battle P8 game.h.a select game.d.j index=<i>/<n>` | yes |
| P8 finishes one `j` entry then goes next | `battle P8 finish game.d.j index=<i>/<n>` | yes |

## Existing Coverage Before New Tests

| Existing checkpoint | Covers | Missing for Slice A |
| --- | --- | --- |
| `battle_exp_levelup_ui` | Active pet gets EXP, P8/P22 levelUp UI opens. | Does not prove multi-participant `x.size()`. |
| `battle_exp_levelup_choiceskill_ui` | P22 -> P23 learn-skill branch. | Does not prove multi-pet `j` iteration. |
| `battle_exp_levelup_learn_skill_done` | Learn skill persists into payload. | Does not prove second participant receives EXP. |
| `battle_p5_voluntary_switch_success` | P5 switch reorders party and reaches P1. | Does not continue to win/EXP after switch. |
| `battle_p5_switch_transition` | P15 transition after switch. | Does not prove switched pet was added to EXP `x`. |

Conclusion:

- Existing checks are useful regressions but not enough to close Slice A.
- Need dedicated EXP vector checks before Slice B.

## Required Test Case Matrix

### Case A1: Active-Only Regression

Proposed checkpoint:

```text
battle_exp_vector_active_only_regression
```

Setup:

- One source pet.
- Enemy defeated by existing deterministic debug active-queue/debuff path.
- No P5 switch.

Expected source-shaped facts:

| Assertion | Expected |
| --- | --- |
| `game.d.x` add count | exactly one participant, active pet |
| direct EXP trace | `participants=1` |
| `game.d.j` size | `1` |
| commit trace | one `game.d.X commit B->S` |
| UI flow | existing P8/P22 behavior still works |
| regression | no change to active-only level-up smoke |

Required trace fragments:

```text
battle EXP game.d.x add
reason=battle entry active f[0]
battle P8 game.d.h direct EXP
participants=1
battle P8 game.d.X commit B->S
battle P8 game.h.a select game.d.j index=0/1
```

Status: `PASS`.

### Case A2: P5 Switched Pet Share

Proposed checkpoint:

```text
battle_exp_vector_p5_switch_two_participants
```

Setup:

- Two source pets:
  - pet A starts active.
  - pet B is reserve.
- Enter battle.
- Open P5 and switch from A to B.
- Defeat enemy after switch.

Expected source-shaped facts:

| Assertion | Expected |
| --- | --- |
| active pet add | pet A added to `x` at battle entry |
| switched pet add | pet B added to `x` after P5 success |
| participant count | `participants=2` in EXP formula |
| direct EXP rows | two `game.d.h direct EXP` traces |
| pending/commit rows | two `game.d.X commit B->S` traces |
| both payloads | both source pet payload EXP values increase |
| old active retained | pet A still receives share after being switched out |

Important:

- Because current rebuild reorders `sourcePets` on P5, assertions should track
  the `SourcePetState` object/species, not only slot index.
- This is exactly why Slice A stores participants by `SourcePetState`.

Required trace fragments:

```text
reason=battle entry active f[0]
reason=P5 game.d.a(slot) switched-in pet
participants=2
game.d.X commit B->S
game.h.a select game.d.j index=0/2
game.h.a select game.d.j index=1/2
```

Status: `PASS`.

### Case A3: `game.d.j` Iterates Second Pet After First Finishes

Proposed checkpoint:

```text
battle_exp_vector_j_iterates_second_pet
```

Setup:

- Two participants in `x`.
- Arrange EXP so:
  - pet A has visible EXP gain and possibly level-up.
  - pet B also has visible EXP gain.
- Confirm/advance through pet A's P8/P22/P23 flow.

Expected source-shaped facts:

| Assertion | Expected |
| --- | --- |
| first select | `game.h.a select game.d.j index=0/2` |
| first finish | `game.d.j index=0/2` finish trace |
| second select | `game.h.a select game.d.j index=1/2` |
| no early exit | battle does not exit immediately after first pet |
| second payload | second pet EXP has been committed and rendered/selected |

Required trace fragments:

```text
battle P8 game.h.a select game.d.j index=0/2
battle P8 finish game.d.j index=0/2
battle P8 game.h.a select game.d.j index=1/2
```

Status: `PASS`.

## Optional Case A4: Max-Level Skip

Proposed checkpoint:

```text
battle_exp_vector_j_skips_max_level_pet
```

Source:

- `game.h.a(int,int)` skips `game.d.j` pets where `t()` is true.

Expected:

- If first `j` pet is level 50, trace skip and select next non-max pet.

Status: `OPTIONAL / AFTER A1-A3`.

## Expected Formula Check

For each direct participant:

```text
base = (((enemyLevel << 1) * enemyLevel + 50) * aG[enemyQuality - 1] / 10) + 400
award = base / x.size() * aH[x.size() - 1] * levelFactor / 1000
```

Where:

```text
aG = {10,11,12,13,15}
aH = {10,12,13,14,15,16}
aI = {105,100,80,60,40,20,5}
```

Test assertions should compare final payload EXP deltas against this formula,
not just check that EXP is greater than zero.

Do not include these in Slice A tests yet:

- `f(5)` EXP multiplier.
- `p.c(7,0)==2` passive share.
- reserve `f(6)` share.
- post-win passive heal.

Those belong to Slice B.

## Implementation Options For Tests

Preferred option if allowed later:

- Add `VqsvSmokeHarness` checkpoints named above.
- Run only `--smoke-checkpoint` PNG/headless with explicit user permission.

Alternative if jar execution remains disallowed:

- Add a small non-UI Java check class that constructs `SourceBattleRuntime`
  state and asserts traces without opening any window.
- This still runs Java code, so ask before executing.

Implementation update:

- Added checkpoint `battle_exp_vector_active_only_regression`.
- Added checkpoint `battle_exp_vector_p5_switch_two_participants`.
- Added checkpoint `battle_exp_vector_j_iterates_second_pet`.
- Added helper assertions in `VqsvSmokeHarness` for:
  - source formula expected EXP,
  - trace count,
  - trace wait,
  - payload EXP assertion.
- Build compile passed.
- Java source mojibake scan passed.
- Checkpoints run:
  - `battle_exp_vector_active_only_regression`: PASS.
  - `battle_exp_vector_p5_switch_two_participants`: PASS.
  - `battle_exp_vector_j_iterates_second_pet`: PASS after fixing the test assert
    to account for source `game.b.v()` subtracting `A(newLevel)` from EXP on
    level-up.
- No live client/game was opened.

## Gate Before Slice B

Do not port passive/share EXP until:

| Gate | Required result |
| --- | --- |
| A1 active-only regression | PASS |
| A2 P5 switched pet share | PASS |
| A3 `game.d.j` second-pet iteration | PASS |
| Build | PASS |
| Java mojibake scan | PASS |

Once these pass, continue to Slice B:

1. Audit exact rebuild mapping for source global state `p.c(7,0)==2`.
2. Audit reserve pet `f(6)` representation in current payload/runtime.
3. Audit participant `f(5)` EXP multiplier.
4. Port one passive/share branch at a time.
