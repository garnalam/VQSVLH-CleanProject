# 91 Battle RNG Formula/Stream Slice Result

## Scope

Post-audit implementation result for the small RNG formula/stream slice after:

- `90_battle_bunny_pre_p17_trace_order_compare.md`

Goal:
- Change battle damage RNG from rebuild-local `Random.nextInt(100)` to source-shaped
  `ae.a(100)`.
- Use the same `VqsvSourceRandom` stream for P7 damage RNG and P17 catch RNG
  inside `SourceBattleRuntime`.
- Preserve trace labels so route order remains auditable.

## Code Changed

| File | Change | Status |
|---|---|---|
| `rebuild_game/src/main/java/VqsvBattleUnit.java` | Replaced `BATTLE_RANDOM.nextInt(100)` damage rolls with `VqsvSourceRandom.a(label, 100, trace)` via `randomPercent(label)`. | PORTED/PARTIAL |
| `rebuild_game/src/main/java/VqsvBattleUnit.java` | Added `setSourceRandomTrace(VqsvSourceRandom, trace, context)` so runtime can provide the source-global battle stream. | PORTED/PARTIAL |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | P7 damage and post-effect chance hooks now pass the runtime `SOURCE_RANDOM`, the same instance used by P17 catch. | PORTED/PARTIAL |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Updated Bunny pre-P17 trace assertion from old helper `BattleUnit.randomPercent` to source helper `ae.a(int)`. | VERIFIED |

## New Trace Shape

Focused smoke:

```text
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-checkpoint battle_bunny_pre_p17_rng_trace
```

Observed:

```text
RNG TRACE battle.P7.skill10.damage.crit seed=1783443931682 source=System.currentTimeMillis
RNG TRACE battle.P7.skill10.damage.crit helper=ae.a(int) bound=100 raw=-1032744464 return=16 seed=runtime
RNG TRACE battle.P7.skill10.damage.jitter helper=ae.a(int) bound=100 raw=207552541 return=70 seed=runtime
RNG TRACE battle.P17.catch helper=ae.a(int) bound=100 raw=150539624 return=12 seed=runtime
```

Interpretation:
- First lazy source seed now appears at the first active source RNG consumer:
  `damage.crit`.
- `damage.jitter` consumes the next raw value.
- P17 catch consumes the next raw value from the same runtime source stream.

Route-specific order remains:

```text
damage.crit -> damage.jitter -> P17.catch
```

## Classification After Slice

| Area | Status | Notes |
|---|---|---|
| Battle damage RNG formula | PORTED/PARTIAL | Uses source-shaped `ae.a(100)` formula. |
| P7 damage and P17 catch shared stream in `SourceBattleRuntime` | PORTED/PARTIAL | Same `SOURCE_RANDOM` instance for current runtime slice. |
| Trace labels | VERIFIED | Labels such as `battle.P7.skill10.damage.crit` are preserved. |
| Full game-global `ae.f` stream | PENDING | Rebuild still scopes this to battle runtime/harness, not every source caller from boot/world. |
| Exact seed parity | PENDING | Source seeds from `System.currentTimeMillis()`; deterministic seed injection is smoke-only. |
| P17 pixel/UI parity | PENDING | This slice is RNG only. |

## Verification

Passed:

- `powershell -ExecutionPolicy Bypass -File .\build.ps1`
- `java "-Dvqsv.modules=..\modules" -jar .\build\libs\vqsv-rebuild-skeleton.jar --check`
- `java -cp .\build\classes VqsvBattleDamageFormulaCheck`
- Mojibake scan Java source: no matches
- `git diff --check`
- PNG smoke `battle_bunny_pre_p17_rng_trace`
- PNG smoke `battle_rng_trace_p17_catch`
- PNG smoke `battle_catch_fail_or_warning`
- PNG smoke `battle_catch_generic_roll_success`
- PNG smoke `battle_bunny_first_catch_forced_fail`
- PNG smoke `battle_bunny_retry_p21_item0`
- PNG smoke `route_bunny_after_battle_task`
- PNG smoke `route_sophie_after_battle_branch`
- PNG smoke `route_elder_after_battle_reward_state`

## Remaining Work

Next safe choices:

1. Audit whether other battle RNG consumers outside `BattleUnit.randomPercent()`
   must join `VqsvSourceRandom`.
2. Build deterministic trace compare for seeded route: expected raw sequence for
   `crit -> jitter -> P17.catch`.
3. Continue P17 animation/UI parity separately; do not mix it into RNG stream
   work.
