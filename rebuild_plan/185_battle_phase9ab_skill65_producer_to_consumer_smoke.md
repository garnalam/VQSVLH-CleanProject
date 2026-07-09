# 185 Battle Phase 9-AB Skill65 Producer To Consumer Smoke

Status date: 2026-07-09

Status: PHASE 9-AB / IMPLEMENTED-PARTIAL / SMOKE-COVERED.

Purpose:

- Close the missing producer-to-consumer proof for skill `65`.
- Prove skill `65` applies buff12 by normal `game.d.q()` post-skill logic.
- Prove P13 active queue promotes `K12` from `1` to `2`.
- Prove the next real player skill uses the promoted buff12 to route back to P2 follow-up.
- Do not use the old smoke helper `debugSetPlayerBuff12KForSmoke()`.

## Source Facts

Sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/script/decoded/data__script__db.mid.json`
- `rebuild_plan/181_battle_phase9x_no_damage_effectmode1_audit.md`

Rows:

```text
skill65 = [6,182,594,0,1,10,1,12,-1,1]
buff12  = [345,360,2,-1,-1]
effect.mid[65] = [1,1,16,0,-1,-1,0, 1,1,15,0,-1,-1,0]
```

Source lifecycle:

```text
game.d.q() after skill65:
  ((b)h.p).a((byte)12, -1, 65)

game.b.a(buff12, value, sourceSkill):
  K[12] = 1
  add active buff queue

game.b.o(12) during P13:
  K[12] = 2

game.d.q() after a later skill:
  if h.m(12) && h.K[12] == 2:
      h.K[12]--
      if target alive:
          state = P2
```

Important timing:

- Skill65 does not immediately create follow-up.
- The battle can continue to the enemy action first.
- The next player-side P13 active queue tick is what promotes `K12` to `2`.
- Only a later skill can consume `K12 == 2` and route P2.

## Implementation

Touched code:

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Added smoke-only `debugPlayerK12ForSmoke()` accessor. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added checkpoint `battle_phase9ab_skill65_producer_to_followup`. |
| `rebuild_plan/156_battle_phase9_skill_coverage_matrix.md` | Updated skill `65`. |

Checkpoint route:

1. Enter elder battle with player skills `[65, 10]`.
2. Use skill `65`.
3. Assert producer:
   - P7 post-effect visible on player side.
   - player has buff12.
   - `K12 == 1`.
   - no P7 damage frame for skill65.
   - no P7 hitroll for skill65.
   - target vector is same-side `targetSide=1`, `targetSlot=1`.
4. Let the state machine continue normally.
5. Assert P13 active queue applies `bank=0 id=12`.
6. Assert `K12 == 2`.
7. Assert the old forced helper trace is absent.
8. Return to P20, choose skill `10`.
9. Assert skill10 reaches P7 and then buff12 routes back to P2 with trace `K12=2->1`.
10. Assert final `K12 == 1`.

## Smoke

Output:

```text
rebuild_game/build/smoke/phase9ab/battle_phase9ab_skill65_producer_to_followup.png
```

| Checkpoint | Result |
| --- | --- |
| `battle_phase9ab_skill65_producer_to_followup` | PASS |

## Status Decision

| Skill | Status | Notes |
| ---: | --- | --- |
| `65` | PORTED/PARTIAL, smoke-covered | Producer buff12, P13 `K12=2`, and later q() P2 follow-up are covered. |

Still not claimed:

- Pixel-perfect AH type9/type1 visual parity for skill65.
- Full RNG stream parity.
- Multi-target or alternate formation parity.

## Next Roadmap Step

Recommended:

```text
Phase 9-AC: remaining conditional damage smoke for skills 7, 9, 23, 29 if not already separately covered.
```

Reason:

- Phase 9 no-damage/default producer group `4/5/14/24/25/34/35/44/65` now has dedicated producer smoke.
- The next visible gaps in `156_battle_phase9_skill_coverage_matrix.md` are conditional/variant sibling rows still marked `MISSING` despite formula support.
