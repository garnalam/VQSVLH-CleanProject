# 85 Battle Catch Success Random Parity Audit

## Scope

Current slice: P21/P17 catch success decision only.

Out of scope for this slice:
- P101/SMS purchase path.
- Full status multiplier parity in `game.d.b(int itemId)`.
- Pixel-perfect P17 animation compare against original client.

## Source chain

### P17 entry

Source: `modules/source_code/decoded/decompiled_source_cfr/game/d.java`, `case 17`.

Observed logic:
- Sets enemy target holder: `this.h.p = this.d[0]`.
- Lazily creates `this.aj = new f()` and loads sprite `269`.
- Positions `aj` at enemy coordinates: `this.aj.b(this.h.i, this.h.j)`.
- Starts animation: `this.aj.c()`.
- Enters catch phase q0: `this.e((byte)0)`.
- Computes chance: `int n4 = this.b((int)l)`.
- Decides success: `this.ak = ae.a(100) < n4`.
- Tutorial edge: if `U == 0 && V == 5`, force `this.ak = false`.

Status: PORTED/PARTIAL in rebuild after this slice.

### Source random

Source: `modules/source_code/decoded/decompiled_source_cfr/ae.java`.

`ae.a(100)` initializes a `Random(System.currentTimeMillis())` and returns
`(f.nextInt() >>> 1) % 100`, so the source catch roll is `0..99`.

Rebuild parity:
- Runtime path uses `(Random.nextInt() >>> 1) % 100`.
- Smoke path can inject the next roll once, so PNG checkpoints are deterministic.

Status: PORTED for decision shape, PORTED/PARTIAL for RNG seed parity because exact
source time seed is not reproduced.

### Chance formula

Source: `game.d.b(int itemId)`.

Confirmed source factors:
- `itemId == 0` returns `100`.
- HP threshold: `<=15% => 85`, `<=50% => 45`, otherwise `<=100% => 20`.
- Ball multiplier: `aq.c[4][itemId][6] / 100`.
- Target nature/quality multiplier: `[110,100,95,80,70]`.
- Status multiplier from target statuses `1`, `2`, `10`, and attacker status `11`.
- Extra attacker status `11` bonus from `aq.c[3][11][5]`.
- Relation/catch class multiplier from `aq.c[0][targetSpecies][22]`.
- Level cap for targets `>=20`: `[0,15,35,65][itemId]`.
- Clamp to `1..100`.

Rebuild status after status multiplier slice:
- PORTED: item0, HP threshold, ball param, nature/quality, target status
  multipliers `1/2/10`, attacker form/status 11 multiplier + status param `[5]`,
  relation class, level cap, clamp.
- VERIFIED: `battle_catch_chance_status_multipliers` asserts P21 item1 chance
  increases for target status and attacker form 11.

## Rebuild change

Previous rebuild decision was APPROX:

`itemId == 0 || isBunnyCaptureBattle() || catchChance >= 50`

This was wrong for generic catch because it bypassed source random and made Bunny
route auto-success too broadly.

Current rebuild decision:

`roll = ae-like 0..99; caught = roll < catchChance; if bunny tutorial force-fail active, caught=false`

Item 0 still succeeds because `catchChance(0) == 100` and roll is `0..99`.

## Smoke plan

Focused deterministic PNG checkpoints:
- `battle_catch_fail_or_warning`: item1, injected roll 99, assert `caught=false`.
- `battle_catch_generic_roll_success`: item1, injected roll 0, assert `caught=true`.
- `battle_catch_chance_status_multipliers`: item1 P21 chance, target debuff
  `1/2/10` and attacker form 11 variants.

Regression PNG checkpoints:
- `battle_bunny_catch_p21`
- `battle_bunny_first_catch_forced_fail`
- `battle_bunny_retry_prompt_tasktip`
- `battle_bunny_retry_p21_item0`
- `battle_bunny_after_catch_route`
- `battle_catch_missing_count_warning`
- `battle_catch_storage_bank`
- `battle_catch_storage_full_release`
- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`

## Remaining status

- Catch success decision: PORTED/PARTIAL.
- Generic random comparison: PORTED.
- Exact RNG seed stream parity: PORTED/PARTIAL.
- Chance formula status multipliers: PORTED.
- P17 animation/UI pixel parity: PORTED/PARTIAL until original-vs-rebuild compare exists.
