# 289 - Battle Debuff5 Cham Chap Audit

Scope: source audit for target-side debuff id `5` / `Cham Chap` before
dedicated closeout smoke.

## Plain Gameplay Explanation

`Cham Chap` is a speed-down status.

It does not damage HP each turn. When it lands, it immediately lowers the
target's current speed. During active queue ticks, source reasserts the lowered
speed and decrements duration. When it expires, speed returns to the base value.

Because battle miss chance uses current speed, this status can also affect
whether later attacks hit or miss.

## Source Facts

```text
debuff5 = [316,327,3]
skill32 = [3,149,561,60,0,45,2,5,10,0]
skill38 = [3,155,567,150,3,15,2,5,10,0]
skill61 = [6,178,590,80,0,45,2,5,5,0]
skill67 = [6,184,596,110,2,30,2,5,5,0]
effect.mid[32] = [0,0,23,0,-1,-1,0, 0,1,1,0,-1,-1,0]
effect.mid[38] = [0,0,23,0,-1,-1,0, 0,1,7,0,-1,-1,0]
effect.mid[61] = [0,0,26,0,-1,-1,0, 0,1,11,0,-1,-1,0]
effect.mid[67] = [0,0,26,0,-1,-1,0, 0,1,11,0,-1,-1,0]
bufDebuf ar[1][5] -> [1,0,0,-1, 0,25,0,-1]
game.d.ai[1] = [0,1,2,3,8,9,10]
```

## Source Chain

Producer:

- `game.b.b(target)` applies debuff id `5` for skills that source routes to the
  debuff5 family.
- Apply case `5` stores:

```text
w[5][1] = target.baseSpeed * aq.c[1][sourceSkill][8] / 100
target.currentSpeed = target.baseSpeed - w[5][1]
```

- Skill `32` and `38` use `10%`.
- Skill `61` uses `5%`.
- Duration comes from `aq.c[7][5][2] = 3`.

Skill67 exception:

- Skill `67` row also has `skill[7] == 5`, but previous bytecode audit `176`
  proved `game.b.b(target)` routes skill67 to default raw damage and clears the
  applied effect id to `-1`.
- `game.d.q()` also does not consume skill67 because it has no explicit case
  and `skill[6] != 1`.
- Therefore skill67 must remain `NOT_REACHED` for debuff5.

P12/P13 active queue:

- `bufDebuf` has a row for debuff5, but source helper `a(b2)` skips visuals
  unless the debuff id is in `ai[1]`.
- `ai[1]` excludes id `5`.
- Source therefore calls `game.b.q(5)` and `game.b.c(5,slot)` immediately.
- `game.b.q(5)` reasserts:

```text
currentSpeed = baseSpeed - w[5][1]
```

- `game.b.c(5,slot)` decrements duration and clears the icon when duration
  reaches `0`.

## Required Closeout Checkpoints

```text
battle_status_debuff5_before_no_effect
battle_status_debuff5_skill32_producer_speed10
battle_status_debuff5_skill38_producer_speed10
battle_status_debuff5_skill61_producer_speed5
battle_status_debuff5_skill67_raw_no_debuff
battle_status_debuff5_p12_no_body_visual_reassert_speed
battle_status_debuff5_expiry_restores_speed
battle_status_debuff5_miss_chance_attacker_speed_down
battle_status_debuff5_buff14_blocks_skill32
```

Expected proof:

| Checkpoint | Must Prove |
| --- | --- |
| before | no debuff5, speed `100 -> 100`, no enemy status icon |
| skill32 | stores `10`, speed `100 -> 90`, icon `6/137` |
| skill38 | stores `10`, speed `100 -> 90`, icon `6/137` |
| skill61 | stores `5`, speed `100 -> 95`, icon `6/137` |
| skill67 | raw damage/visual only, no debuff5, speed unchanged |
| P12 no visual | no `active queue visual start`, no actor/speffect, speed reasserted |
| expiry | duration `3->2->1->0`, speed `100->90->90->90->100`, icon cleared |
| miss chance | player speed `55` with value `10` becomes `45`, miss chance uses `45` |
| buff14 block | damage can hit, debuff5 not applied, speed unchanged |

## Classification Before Smoke

```text
Debuff5 producer skills 32/38/61: PORTED/PARTIAL until dedicated smoke passes
Skill67 as debuff5 producer: NOT_REACHED, already audited in 176/177
Stored speed-down value: PORTED/PARTIAL until dedicated smoke passes
P12/P13 no-body-visual skip: NEEDS DEDICATED SMOKE
Expiry speed restore: NEEDS DEDICATED SMOKE
Miss chance consumer through lowered current speed: NEEDS DEDICATED SMOKE
Pixel-perfect original comparison: PENDING
```

