# 184 Battle Phase 9-AA Defensive Hook Producer Smoke

Status date: 2026-07-09

Status: PHASE 9-AA / IMPLEMENTED-PARTIAL / SMOKE-COVERED.

Purpose:

- Smoke producer behavior for no-damage/default `effectMode == 1` skills `34` and `35`.
- Prove skill `34` applies buff5 through same-side no-damage `game.d.q()` route.
- Prove skill `35` applies buff6 through same-side no-damage `game.d.q()` route.
- Prove the defensive hooks currently wired in `BattleUnit.computeDamage()`:
  - buff5 stores returned damage into attacker `K[5]`, consumed later by `game.d.q()` post-damage.
  - buff6 keeps the source oddity: source checks `target.m(6)` but reads attacker `v[6][1]/v[6][2]`.

## Source Facts

Sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/script/decoded/data__script__db.mid.json`
- `rebuild_plan/181_battle_phase9x_no_damage_effectmode1_audit.md`

Rows:

```text
skill34 = [3,151,563,0,1,10,1,5,-1,1]
skill35 = [3,152,564,0,1,10,1,6,-1,1]
buff5  = [338,353,3,30,-1]
buff6  = [339,354,3,50,-1]
```

No-damage producer route:

```text
skill[3] == 0
skill[6] == 1
skill[9] == 1
game.d.q() default -> ((b)h.p).a((byte)skill[7], -1, skillId)
```

Defensive hook source route:

```text
game.b.b(target):
  if target.m(6) && ae.a(100) <= this.v[6][1]:
      damage = damage * this.v[6][2] / 100

  if target.m(5) && ae.a(100) <= target.v[5][1]:
      this.K[5] = damage

game.d.q() post-damage:
  if target.m(5) && attacker.K[5] > 0:
      attacker takes attacker.K[5]
      attacker.K[5] = 0
```

Important oddity:

- buff6 is not a normal "read target buff params" hook.
- Source checks whether the target has buff6, but it reads chance/percent from the attacker `this.v[6]`.
- Rebuild keeps that odd shape in `BattleUnit.computeDamage()`.

## Implementation

Touched code:

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Added smoke-only helpers `debugEnemyAttackPlayerReflectHookForSmoke()` and `debugEnemyAttackPlayerBuff6ReductionForSmoke()`. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added checkpoint prefix `battle_phase9aa_defensive_hook_skill_`. |
| `rebuild_plan/156_battle_phase9_skill_coverage_matrix.md` | Updated rows `34/35`. |

Checkpoint setup:

- Enter elder battle P7 with player skill `34` or `35`.
- Advance to P7 phase 3.
- Assert the producer route:
  - post-effect visible on player side.
  - player has expected buff.
  - no P7 damage frame.
  - no P7 hitroll.
  - trace contains `battle P7 no-damage skill=...`.
  - trace contains `game.d.q postEffect skill=...`.
  - target vector is same-side `targetSide=1`, `targetSlot=1`.

Controlled hook checks:

| Skill | Hook smoke | Notes |
| ---: | --- | --- |
| `34` | After buff5 is active on player, enemy skill10 attacks player with deterministic seed; attacker stores and consumes reflected damage. | Proves `K[5]` path is wired. |
| `35` | After buff6 is active on player, enemy skill10 is computed twice with deterministic seed: baseline, then with attacker buff6 params loaded. Reduced damage must be lower. | This intentionally proves the source oddity, not a normal target-param read. |

## Smoke

Outputs:

```text
rebuild_game/build/smoke/phase9aa/battle_phase9aa_defensive_hook_skill_34.png
rebuild_game/build/smoke/phase9aa/battle_phase9aa_defensive_hook_skill_35.png
```

| Checkpoint | Result |
| --- | --- |
| `battle_phase9aa_defensive_hook_skill_34` | PASS |
| `battle_phase9aa_defensive_hook_skill_35` | PASS |

## Status Decision

| Skill | Status | Notes |
| ---: | --- | --- |
| `34` | PORTED/PARTIAL, smoke-covered | Producer no-damage buff5 and `K[5]` reflect hook are covered. Visual AH type7 remains outside this slice. |
| `35` | PORTED/PARTIAL, smoke-covered | Producer no-damage buff6 and source-odd formula hook are covered. Visual AH type7/type1 remains outside this slice. |

Still not claimed:

- Pixel-perfect AH type7 visual parity.
- Full game.h widget runtime parity for all post-effect text.
- Broad random parity across all seeds.

## Next Roadmap Step

Recommended:

```text
Phase 9-AB: skill65 producer-to-consumer.
```

Scope:

- Skill `65` is still in the no-damage/default `effectMode == 1` group from audit 181.
- Prove skill65 applies buff12.
- Prove active queue consumer changes `K12`.
- Prove the next q()/P2 follow-up route uses buff12 instead of creating fake damage.
