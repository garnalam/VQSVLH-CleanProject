# 299 - Battle Debuff10 Te Liet Closeout

Date: 2026-07-14

Status: AUDITED / PORTED-PARTIAL / SMOKE-COVERED.

Scope: debuff id `10` from `aq.c[7]`, the `Te Liet` status.

## Simple Mechanism

`Te Liet` is the source battle debuff whose text says each action loses a
noticeable amount of wait time.

What the PC source proves for the current rebuild slice:

- skills `41` and `47` can apply debuff10 to the target;
- debuff10 lasts `4` turns;
- the HUD status icon is debuff icon cell `11`;
- active queue P12/P13 can play the debuff10 body visual;
- catch chance treats debuff10 as status index `3`, multiplier `12 / 10`;
- normal tick/expiry only decrements duration and clears the icon.

What is not proven yet:

- an action-delay scheduling consumer for debuff slot `w[10]`.

Important correction:

```text
game.d h.f((byte)10) is held/passive item id 10, not debuff10.
```

That held item is documented separately in
`261_battle_held_item10_cam_lam_chi_diep_hp_floor_audit.md`.

## Source Rows

From `modules/script/decoded/data__script__db.mid.json`:

| Source row | Value | Meaning |
| --- | --- | --- |
| skill `41` | `[4,158,570,90,0,45,2,10,10,0]` | direct damage, explicit debuff family, debuff id `10`, chance param `10` |
| skill `47` | `[4,164,576,130,2,30,2,10,10,0]` | direct damage, explicit debuff family, debuff id `10`, chance param `10` |
| skill `68` | `[6,185,597,110,3,15,1,10,5,0]` | direct damage plus target debuff10 plus self buff10; covered by older Phase 9-U smoke, not the primary closeout producer here |
| debuff `10` | `[321,332,4]` | debuff text/name ids and duration `4` |
| held item `10` | `[223,11,247,5,1,10]` | different system; HP floor passive, not this debuff |

## Source Code Evidence

### Apply

Source: `modules/source_code/decoded/decompiled_source_cfr/game/b.java`.

`game.b.b(target)` handles explicit debuff families:

- reads target debuff id from skill row column `7`;
- reads explicit chance from skill row column `8`;
- blocks the debuff if target has buff14;
- applies chance reduction if target has held/passive status id `3`;
- writes `w[debuffId][0] = aq.c[7][debuffId][2]`;
- writes `w[debuffId][3] = sourceSkill`;
- writes `w[debuffId][4] = 1`;
- queues active effect bank `1`, id `10`.

For debuff10 specifically, the apply switch has no case that stores a stat
delta, so `w[10][1]` remains `0`.

### Tick / Expiry

Source: `game.b.q(int)` only has special logic for debuff ids:

```text
0, 1, 2, 3, 4, 5, 6, 7
```

There is no `case 10`. Therefore debuff10 uses the default no-op tick plus
generic duration decrement/clear in `game.b.c(int,int)`.

### Body Visual

Source: `modules/script/original/bufDebuf.mid`.

Known row:

```text
bank 1, id 10 -> [1,19,0,-1,1,6,0,-1]
```

`game.d.ai[1]` includes id `10`, so P12/P13 active queue is allowed to start a
body visual for debuff10.

### Catch Consumer

Source: `modules/source_code/decoded/decompiled_source_cfr/game/d.java`,
catch chance path:

```text
target.m(1)  -> status index 1
target.m(2)  -> status index 2
target.m(10) -> status index 3
attacker.f(11) -> status index 4
```

The status multiplier array uses index `3 = 12`, so debuff10 shares the same
`12 / 10` catch multiplier as debuff2.

### Action Delay Search

Searched source callsites that looked suspicious:

```text
game.d h.f((byte)10)
game.b p(10)
game.b w[10]
game.b q(10)
game.d target.m(10)
```

Result:

- `p(10)` is the true debuff-active check helper, but no action scheduling
  consumer has been found using it.
- `target.m(10)` is catch chance.
- `h.f((byte)10)` is held/passive item id 10, not debuff10.

Classification:

```text
action-delay scheduling = NOT_FOUND_IN_PC_SOURCE / PENDING_SOURCE_PROOF
```

## Implemented Smoke Closeout

Added dedicated PNG checkpoints:

| Checkpoint | What it proves |
| --- | --- |
| `battle_status_debuff10_before_no_effect` | no icon, no active debuff10, duration/value `0` |
| `battle_status_debuff10_skill41_producer_apply` | skill41 forced hit + forced debuff roll applies debuff10 duration `4`, source skill `41`, icon `11/138` |
| `battle_status_debuff10_skill47_producer_apply` | same apply path for skill47 |
| `battle_status_debuff10_p12_body_visual_type9` | P12/P13 starts source row `[1,19,0,-1,1,6,0,-1]`, first visual type `9` |
| `battle_status_debuff10_catch_multiplier` | catch chance is higher than base through status multiplier path |
| `battle_status_debuff10_expiry_clears_icon` | duration `4 -> 3 -> 2 -> 1 -> 0`, HP/defense/speed unchanged, icon clears |

These checkpoints are in the fixed `battle_quick` suite.

## Current Status

| Area | Status |
| --- | --- |
| Skill41/47 producer | PORTED |
| Explicit chance `10` | PORTED |
| Buff14 block | PORTED by existing Phase 9-L smoke |
| Status3 chance reduction | PORTED by existing Phase 9-L smoke |
| Miss interaction | PORTED by existing Phase 9-L smoke |
| Icon `11`, duration cell `134 + duration` | PORTED |
| P12/P13 body visual row | PORTED/PARTIAL; source row and visible type are smoke-covered, original pixel compare is not claimed |
| Catch multiplier `12 / 10` | PORTED |
| Generic expiry | PORTED |
| Action-delay scheduling | NOT_FOUND_IN_PC_SOURCE / PENDING_SOURCE_PROOF |

## Next Roadmap Step

Debuff table `aq.c[7]` rows `0..10` now has dedicated closeout coverage.

Next recommended step: create a small table closeout doc for all debuffs
`0..10`, then move to the next battle skill/effect roadmap phase: broad skill
coverage cleanup for remaining non-status skill special cases, or item/battle
interaction gaps if the user redirects there.
