# 287 - Battle Debuff4 Muc Audit

Scope: source audit for target-side debuff id `4` / `Muc` before dedicated
closeout smoke.

## Plain Gameplay Explanation

`Muc` is not poison and not delayed damage.

It is a temporary accuracy/evasion pressure status. When a pet has debuff4 and
later attacks, the miss/evasion calculation treats that attacker as a little
slower. Skill `31` stores value `1`; skill `37` stores value `2`. Higher stored
value means slightly higher chance that the target dodges.

## Source Facts

```text
debuff4 = [315,326,3]
skill31 = [3,148,560,60,0,45,2,4,1,0]
skill37 = [3,154,566,100,2,30,2,4,2,0]
effect.mid[31] = [0,0,23,0,-1,-1,0]
effect.mid[37] = [0,0,23,0,-1,-1,0, 0,1,7,0,-1,0,0, 0,1,6,0,-1,-1,0]
bufDebuf ar[1][4] -> [1,1,0,0, 1,11,0,-1]
game.d.ai[1] = [0,1,2,3,8,9,10]
```

## Source Chain

Producer:

- `game.d.q()` applies normal P7 skill flow.
- For target-side debuff skills, source calls `((b)h.p).a(effectId, -1, skillId)`.
- `game.b` debuff apply case `4` stores `w[4][1] = aq.c[1][skill][8]`.
- Therefore skill `31` stores `1`; skill `37` stores `2`.
- Duration comes from `aq.c[7][4][2] = 3`.

P12/P13 active queue:

- `game.d` builds debuff queue from `x[1]`.
- `bufDebuf` has a visual row for debuff4, but source helper `a(b2)` skips
  visuals unless the debuff id is listed in `ai[1]`.
- `ai[1] = [0,1,2,3,8,9,10]`, so debuff4 is not visualized in P12/P13.
- Source immediately calls `game.b.q(4)` then `game.b.c(4,slot)`.
- `game.b.q(4)` returns immediately; no HP/stat body effect.
- `game.b.c(4,slot)` decrements duration and clears the icon when duration
  reaches `0`.

Miss/evasion consumer:

In source P7 damage application, the miss chance path computes a speed
difference. If the attacker has debuff4, source subtracts `w[4][1]` from the
attacker speed before comparing against target speed:

```text
if attacker.p(4):
    missChance = (targetSpeed - (attackerSpeed - attacker.w[4][1])) << 1
else:
    missChance = (targetSpeed - attackerSpeed) << 1

clamp missChance to 0..20
held item 9 forces missChance = 0
```

## Existing Rebuild Status Before Closeout

Existing Phase 9-H smoke already covered:

```text
battle_phase9h_debuff4_success_skill_31
battle_phase9h_debuff4_success_skill_37
battle_phase9h_debuff4_buff14_block_skill_31
battle_phase9h_debuff4_status3_block_skill_31
battle_phase9h_debuff4_miss_queue_skill_31
battle_phase9h_debuff4_miss_chance_skill_31
```

Gap: those were family/trace smokes, not a dedicated table-order closeout named
for debuff4 with before/producer/active-queue/expiry/miss-value PNGs.

## Required Closeout Checkpoints

```text
battle_status_debuff4_before_no_effect
battle_status_debuff4_skill31_producer_apply
battle_status_debuff4_skill37_producer_apply
battle_status_debuff4_p12_no_body_visual_skip
battle_status_debuff4_tick_noop_duration2
battle_status_debuff4_expiry_clears_icon
battle_status_debuff4_miss_chance_value1
battle_status_debuff4_miss_chance_value2
battle_status_debuff4_buff14_blocks_skill31
```

Expected proof:

| Checkpoint | Must Prove |
| --- | --- |
| before | no debuff4, no enemy status icon |
| skill31 producer | icon `5/137`, source skill `31`, stored value `1` |
| skill37 producer | icon `5/137`, source skill `37`, stored value `2` |
| P12 no body visual | no `active queue visual start`; no actor/speffect; duration `3->2` |
| tick no-op | HP unchanged; duration decremented |
| expiry | duration `3->2->1->0`; icon cleared; HP unchanged |
| miss value1 | speed `55` vs `60`, value `1`, miss chance `12` |
| miss value2 | speed `55` vs `60`, value `2`, miss chance `14` |
| buff14 block | damage can hit, debuff4 not applied |

## Classification Before Smoke

```text
Debuff4 producer skills 31/37: PORTED/PARTIAL until dedicated smoke passes
Stored flat value 1/2: PORTED/PARTIAL until dedicated smoke passes
P12/P13 no-body-visual skip: NEEDS DEDICATED SMOKE
Tick/expiry no-op: NEEDS DEDICATED SMOKE
Miss chance consumer: PORTED/PARTIAL, existing trace smoke only
Buff14 block: PORTED/PARTIAL, existing family smoke only
Pixel-perfect original comparison: PENDING
```

