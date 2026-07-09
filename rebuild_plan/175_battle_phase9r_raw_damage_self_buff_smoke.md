# 175 Battle Phase 9-R Raw Damage Self-Buff Smoke

Status date: 2026-07-09

Status: PHASE 9-R / IMPLEMENTED-PARTIAL / SMOKE-COVERED.

Purpose:

- Smoke the bytecode-default raw-damage skills with source-backed
  `game.d.q()` self-buff behavior.
- Cover only `21/27/42/48/62`.
- Keep skill `67` out of this slice because effectMode `2` still needs its own
  audit.

## Source Basis

Primary audit:

- `rebuild_plan/174_battle_phase9q_source_switch_gap_audit.md`

Source facts used:

| Skill | Formula producer | Post-skill q() |
| --- | --- | --- |
| `21` | `game.b.b(target)` bytecode switch target `706`, default raw damage. | `game.d.q()` applies self buff `4`. |
| `27` | `game.b.b(target)` bytecode switch target `706`, default raw damage. | `game.d.q()` applies self buff `4`. |
| `42` | `game.b.b(target)` bytecode switch target `706`, default raw damage. | `game.d.q()` applies self buff `7`. |
| `48` | `game.b.b(target)` bytecode switch target `706`, default raw damage. | `game.d.q()` applies self buff `7`. |
| `62` | `game.b.b(target)` bytecode switch target `706`, default raw damage. | `game.d.q()` applies self buff `10`. |

Important exclusion:

```text
Skill 67 is not part of Phase 9-R.
```

It also uses bytecode-default raw damage, but `skill[6] == 2` and effect id `5`
do not have a proven `game.d.q()` consumer in the Phase 9-Q audit.

## Implementation

Touched code:

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleUnit.java` | Added trace-only marker when skill `21/27/42/48/62/67` uses bytecode-default raw damage. No damage formula change. |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Added read-only smoke hooks for player buff presence/active slot. No runtime logic change. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added `battle_phase9r_raw_self_buff_skill_<id>` checkpoints for `21/27/42/48/62`. |

Smoke setup:

- Enters Elder P7 with the selected skill.
- Sets player attack to `100`.
- Sets enemy defense to `40`.
- Forces P7 hit roll to `99`.
- Uses deterministic damage seed.

Asserted behavior:

- P7 damage frame occurs.
- Trace contains `BYTECODE_DEFAULT_RAW_DAMAGE skill=<id>`.
- Trace marks `powerPercentIgnored=...`.
- P7 damage result has `appliedDebuffId=-1`.
- `game.d.q()` post-effect trace appears.
- Post-effect is on player side.
- Player has expected buff active.

## Smoke Matrix

Output folder:

```text
rebuild_game/build/smoke/phase9r/
```

| Skill | Expected self buff | Checkpoint | Result |
| --- | --- | --- | --- |
| `21` | `4` | `battle_phase9r_raw_self_buff_skill_21` | PASS |
| `27` | `4` | `battle_phase9r_raw_self_buff_skill_27` | PASS |
| `42` | `7` | `battle_phase9r_raw_self_buff_skill_42` | PASS |
| `48` | `7` | `battle_phase9r_raw_self_buff_skill_48` | PASS |
| `62` | `10` | `battle_phase9r_raw_self_buff_skill_62` | PASS |

## Regression

| Check | Result |
| --- | --- |
| `build.ps1` | PASS |
| `com.vqsv.rebuild.Main --check` | PASS |
| `VqsvBattleDamageFormulaCheck` | PASS |
| Java mojibake scan | PASS, no matches |
| `route_sophie_after_battle_branch` | PASS |
| `route_bunny_after_battle_task` | PASS |
| `route_elder_after_battle_reward_state` | PASS |

Regression PNG folder:

```text
rebuild_game/build/smoke/phase9r_regression/
```

## Status Decision

| Area | Status |
| --- | --- |
| `21/27/42/48/62` formula branch | PORTED/PARTIAL, smoke-covered as bytecode-default raw damage. |
| `21/27/42/48/62` q() self-buff | PORTED/PARTIAL, smoke-covered. |
| Target debuff from damage result | PORTED/PARTIAL: smoke asserts `appliedDebuffId=-1`. |
| Exact RNG stream parity | PARTIAL: deterministic hooks are used. |
| Skill `67` | Phase 9-S audit done; classified as raw damage + P7 visual only, smoke pending. |

## Next Roadmap Step

Completed follow-up:

```text
Phase 9-S: audit skill 67 effectMode 2 / effect id 5 consumer.
```

Result:

```text
Skill 67 is not a self-buff. Smoke it next as raw damage with no debuff/no q() post-effect.
```
