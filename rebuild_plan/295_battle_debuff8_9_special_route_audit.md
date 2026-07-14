# 295 - Battle Debuff8/9 Special Route Audit

Scope: source-backed audit before coding debuff id `8` / `Quy Mi` and debuff id
`9` / `Hon Loan`.

## Plain Gameplay Explanation

These two rows are not normal direct debuffs.

Skills `54` and `55` have power `0`, so the normal P7 damage/debuff producer
does not behave like skills such as debuff6/debuff7. The source still plays the
skill animation, but it does not simply deal damage and then write debuff id
`8/9` through the usual direct-hit path.

The important part is downstream behavior if a unit already has debuff8 or
debuff9:

- debuff8 affects the attack target route and can redirect/build a target list
  from living pets except the attacker;
- debuff9 affects target/result routing and switch/selection behavior;
- both need route-specific implementation, not a generic "apply debuff on hit"
  patch.

Update after closeout `296`: debuff8 `Quy Mi` no longer follows the source
target-route consumer in the current rebuild. The user approved an intentional
gameplay fix instead: active debuff8 gives `+10%` outgoing damage and routes
1v1 attacks by `55%` self-hit / `45%` opponent-hit. Keep the source audit below
as historical proof for why skill54 is not an ordinary producer.

## Source Rows

```text
skill54 = [5,171,583,0,1,10,2,8,40,0]
skill55 = [5,172,584,0,1,10,2,9,-1,0]

debuff8 = [319,330,4]
debuff9 = [320,331,1]

effect.mid[54] = [0,1,0,0,-1,-1,0]
effect.mid[55] = [0,1,12,0,-1,-1,0]
```

Known visual status rows from the previous Phase 9-M audit:

```text
bufDebuf debuff8 visual row = [1,0,0,-1,0,25,0,-1]
bufDebuf debuff9 visual row = [1,12,0,-1]
```

## Producer Audit: Skills 54/55

Historical audit `170_battle_phase9m_zero_power_debuff8_9_coverage.md`
already proved the first critical rule:

```text
skill54/55 reach P7 zero-power guard
no HP damage
no hitroll/miss path
no normal debuff write
no buff14/status3 gate relevance
no P12/P13 visual row starts from these skills
```

That remains the correct classification for the producer side:

```text
skill54 P7 animation: PORTED/PARTIAL
skill54 normal debuff8 producer: NOT_REACHED
skill55 P7 animation: PORTED/PARTIAL
skill55 normal debuff9 producer: NOT_REACHED
```

Do not implement debuff8/9 by simply enabling ordinary pending debuff application
for skills `54/55`.

## Downstream Source Consumers

### Debuff8

Source snippets:

```text
game.d case 7:
if (attacker.side != target.side || attacker.hasDebuff8) {
    Z = attacker.computeDamage(target)
}
```

This means debuff8 allows a same-side/self-route damage calculation that would
normally be skipped.

In `game.d` state/dispatch around the target-selection branch:

```text
if skill table param[9] == 0 && attacker.hasDebuff8
   && ae.a(100) > aq.a(skill, 8):
    f(attacker)
```

`f(attacker)` clears attacker target vectors and fills them with every living
unit except the attacker:

```text
attacker.G.clear()
attacker.H.clear()
for every battle unit:
    if unit.alive && unit != attacker:
        attacker.G.add(unit)
        attacker.H.add(unitIndex)
```

So debuff8 is a target-routing status, not a stat debuff and not the same as
buff5 reflect storage.

Open question before code:

- Which route in current PC rebuild should be allowed to put debuff8 on a unit,
  since skill54 itself does not write it through normal P7?
- Should the first patch be smoke-only seeding of debuff8 to prove target routing,
  or should we first find another source caller that applies debuff8?

### Debuff9

Source snippets:

```text
game.d target/dispatch:
if attacker.hasDebuff9:
    f(attacker)
    choose random target from attacker.G
    enter P7
```

and later:

```text
if target.side != attacker.side || attacker.hasDebuff9
    use one result target route
else
    use the normal selected target route
```

Debuff9 therefore affects who the attacker can/does target and how the P7/result
transition evaluates the chosen target. It also has a status text/table meaning
around not being able to switch own pet, but the exact P5 command lock still
needs a focused source route smoke before claiming it as fully ported.

Open question before code:

- Is debuff9 currently only meaningful as an active seeded state in rebuild, or
  is there a reachable source route that applies it outside skill55's zero-power
  no-damage path?
- P5 switch-lock should not be patched from text alone; it needs a source-backed
  command-state proof.

## Current Rebuild State

Current rebuild code already has historical Phase 9-M checkpoints proving:

```text
battle_phase9m_zero_power_success_skill_54
battle_phase9m_zero_power_success_skill_55
battle_phase9m_zero_power_buff14_block_skill_54
battle_phase9m_zero_power_status3_block_skill_54
battle_phase9m_zero_power_miss_queue_skill_54
battle_phase9m_debuff8_visual_consumer_skill54
battle_phase9m_debuff9_visual_consumer_skill55
```

Current rebuild does not yet model the downstream source consumers as a complete
gameplay route:

```text
debuff8 target reroute / same-side damage permission: PENDING
debuff8 source caller beyond skill54 zero-power route: UNKNOWN
debuff9 random target route: PENDING
debuff9 P5 switch-lock/command parity: PENDING
debuff9 source caller beyond skill55 zero-power route: UNKNOWN
```

## Recommended Small Next Slice

Do not patch both effects broadly.

Completed smoke-only slice:

```text
debuff8 seeded-consumer smoke only
```

Purpose:

- seed attacker with debuff8 in a smoke-only helper;
- force a route where same-side/self target would normally skip normal target
  damage;
- assert the source-shaped target routing trace:
  - attacker has debuff8;
  - target vector is rebuilt from living units except attacker;
  - no ordinary skill54 debuff producer is involved;
  - no HP mutation is added unless source route proof is sufficient.

Runtime patch decision:

- `SourceBattleRuntime.prepareTargetList()` now ports the guarded debuff8
  consumer for the active attacker.
- It still does not treat skill `54` as an ordinary debuff producer.

Superseded by dedicated debuff8 closeout:

```text
296_battle_debuff8_quy_mi_closeout.md
```

The closeout adds these focused checkpoints:

```text
battle_status_debuff8_before_no_effect
battle_status_debuff8_skill54_zero_power_no_apply
battle_status_debuff8_skill54_zero_power_buff14_no_apply
battle_status_debuff8_gameplay_fixed_self_hit_damage_up
battle_status_debuff8_gameplay_fixed_enemy_hit_damage_up
battle_status_debuff8_p12_body_visual_type1_actor25
battle_status_debuff8_expiry_clears_icon
```

Important update:

- The source-route limitation above is now historical. Current runtime uses the
  user-approved gameplay fix, so the smoke locks self-hit and opponent-hit
  routes directly in 1v1.

Second slice after that:

```text
debuff9 seeded-consumer smoke for random target route / P5 lock audit
```

## Classification

```text
skill54 visual: PORTED/PARTIAL
skill54 normal debuff8 producer: NOT_REACHED
skill55 visual: PORTED/PARTIAL
skill55 normal debuff9 producer: NOT_REACHED
debuff8 seeded source consumer smoke: SUPERSEDED_BY_GAMEPLAY_FIX
debuff8 gameplay consumer: INTENTIONAL_DEVIATION / GAMEPLAY_FIXED
debuff9 active consumer: PENDING
P5 switch-lock for debuff9: PENDING
pixel-perfect original comparison: PENDING
```

`debuff8 gameplay consumer` is now intentionally different from the source
route. Closeout `296` locks the approved behavior: outgoing damage `+10%`,
roll `<55` self-hit, roll `>=55` opponent-hit. Do not reintroduce the old
`game.d.f(attacker)` route unless the user explicitly reverses this gameplay
decision.

## Next Step

Debuff9 active consumer is now closed by:

```text
297_battle_debuff9_hon_loan_closeout.md
```

It keeps skill55's ordinary producer as `NOT_REACHED`, ports the active
`game.d.f(attacker)` + `ae.a(G.size())` random target consumer, ports the
`bufDebuf` body visual and expiry, and leaves direct P5 confirm-lock as
`PENDING` because no `game.h.X()` debuff9 branch was proven.
