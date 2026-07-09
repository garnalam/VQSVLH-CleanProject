# 141 Battle P7 `game.d.q()` Follow-Up Branches Audit

## Scope

Audit the remaining `game.d.q()` branches after P7:

- buff/status `12` with `K[12] == 2`;
- skills `63` and `69`;
- decide whether rebuild needs a small patch.

This follows `140_battle_p7_death_original_compare_and_queue_transition.md`.

## Source Facts

Source `game.d.q()` applies post-P7 side effects, waits HP/floating text, then resolves follow-up/turn transitions.

Relevant branch:

```text
if h.m(12) && h.K[12] == 2:
    h.K[12]--
    if target is dead:
        h.K[12]--
        i++
        p()
    else:
        a((byte)2)
        break

n = ae.a(100)
if (h.D == 63 || h.D == 69) && n <= aq.c[1][h.D][8]:
    if target is dead:
        h.K[12]--
    else:
        a((byte)2)
        break

i++
p()
```

`game.b.a(byte skill,b target)` already consumes skill PP, but if buff `12`
has `K[12] == 1`, source refunds one PP. Rebuild already had that PP refund
shape via `BattleUnit.consumeSkillPp()`.

`game.b.o(12)` sets `K[12] = 2` during active-queue buff tick. That is the
state consumed by the P7 `q()` follow-up branch.

Skill rows from source `db.mid`:

| Skill | Row | Meaning for this slice |
| --- | --- | --- |
| `63` | `[6,180,592,100,1,30,0,-1,5,0]` | Direct damage; after `q()`, 5% roll can re-enter P2 if target lives. |
| `69` | `[6,186,598,150,3,15,0,-1,8,0]` | Direct damage; after `q()`, 8% roll can re-enter P2 if target lives. |
| `65` | `[6,182,594,0,1,10,1,12,-1,1]` | Source row that can apply buff `12`; broader target semantics remain partial. |

## Rebuild Gap Found

Before this slice, rebuild had:

- PP refund when buff12 `effectScratch[12] == 1`;
- buff12 apply/tick state storage;
- P7 `game.d.q()` post-effect text and P12/P13 queue order.

But rebuild did not consume `K[12] == 2` or skill `63/69` roll after P7 to re-enter P2.
It always fell through to P1 dispatch for living target after P7.

## Patch Decision

Smallest source-backed patch:

- After P7 and after KO/result checks, if attacker buff12 is active with `K12 == 2`,
  set `K12 = 1` and enter P2 for the same actor.
- If skill is `63` or `69`, roll source `ae.a(100)` via `VqsvSourceRandom`; if roll
  is within `aq.c[1][skill][8]`, enter P2 for the same actor.
- Keep dead-target cases on the existing P8/P9/P15 route, but consume the dead-target
  K12 marker for skill `63/69` when present.

This remains `PORTED/PARTIAL` because source turn vector `i++/p()` is still simplified
in the rebuild runtime.

## Smoke Checkpoints

| Checkpoint | Purpose |
| --- | --- |
| `battle_p7_q_buff12_followup_p2` | Forces player buff12 `K12=2`, lets P7 finish with target alive, expects P2 follow-up. |
| `battle_p7_q_skill63_followup_p2` | Seeds source RNG so skill63 follow-up roll passes, expects P2 follow-up. |

## Status

| Area | Status | Note |
| --- | --- | --- |
| Buff12 PP refund `K12=1` | PORTED/PARTIAL | Existing rebuild path. |
| Buff12 follow-up `K12=2 -> P2` | PORTED/PARTIAL | Added after P7 living-target branch. |
| Skill63 follow-up roll | PORTED/PARTIAL | Added source RNG trace and P2 transition. |
| Skill69 follow-up roll | PORTED/PARTIAL | Same code path as skill63; smoke currently covers skill63. |
| Full `i++/p()` turn cursor parity | PARTIAL | Rebuild dispatch is still simplified compared with source vectors. |

## Next

If smoke passes, P7 logic can be closed at `PORTED/PARTIAL` for current routes.
The remaining follow-up is documentation/closeout unless a later route exposes
a source turn-vector edge case.
