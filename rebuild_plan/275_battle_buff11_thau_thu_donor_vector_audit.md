# 275 - Battle Buff11 Thau Thu Donor Vector Audit

Scope: buff11 `Thau Thu` / skill64 selected donor copy-clear behavior.

Status: `AUDIT-ONLY / NO CODE CHANGE`.

Current classification after audit:

`PORTED/PARTIAL`

Reason:

- cast-time selected target slot and one-donor copy/clear are already implemented and smoke-covered;
- active queue tick `game.b.o(11)` is not implemented in rebuild yet;
- multi-target/vector parity is still only partially proven.

## Source Rows

Rows already decoded in previous skill/status matrices:

```text
skill64 = [6,181,593,0,1,10,1,11,-1,0]
buff11  = [344,359,3,-1,-1]
```

Meaning for this audit:

| Field | Source meaning |
| --- | --- |
| `skill64[3] = 0` | no normal damage power |
| `skill64[6] = 1` | q()/post-effect branch applies a buff/effect |
| `skill64[7] = 11` | applies buff id `11` |
| `skill64[9] = 0` | target mode is opposing side |
| `buff11[2] = 3` | duration 3 |
| `buff11[3..4] = -1,-1` | no numeric stat params; selected donor slot is stored at runtime |

## Source Target Vector Proof

Source: `modules/source_code/decoded/decompiled_source_cfr/game/d.java`

The selected donor slot is a real `game.d.d[]` battle-unit slot, not merely a UI row.

| Source line area | Behavior |
| --- | --- |
| `game.d` target-vector build | For each valid target, source pushes the `b` unit object into `h.G` and the real unit slot as string into `h.H`. |
| `game.d.i()` | `h.p = h.G[C]`; `h.I = Byte.parseByte(h.H[C])`. |
| auto-target branches | Also assign `h.I = Byte.parseByte(h.H[randomIndex])`. |
| KO/replacement logic | Uses `h.I` when the selected unit has to be replaced or when status11 references must be cleared. |

Important conclusion:

```text
h.I == selected real battle-unit slot in game.d.d[].
```

Rebuild equivalent:

```text
VqsvBattleRuntime.commitSelectedTarget()
  targetSlots[selectedTargetIndex] -> player.battleUnit.selectedTargetSlot
```

Status: `PORTED/PARTIAL`.

It is source-shaped for the current one-enemy smoke path, but still needs a dedicated multi-slot smoke where `targetSlot != 0`.

## Source q() / Skill64 Proof

Source: `modules/source_code/decoded/decompiled_source_cfr/game/d.java`

Relevant source shape:

```text
case 64:
    h.a((byte)aq.c[1][skillId][7], h.I, skillId);
```

For skill64:

```text
aq.c[1][64][7] == 11
```

So the source call is:

```text
attacker.a((byte)11, selectedRealSlot, 64)
```

The same q() block draws the buff text on the attacker side for skills:

```text
21, 27, 42, 48, 62, 64, 68
```

That means skill64 is an attacker-side buff text/effect, not a target damage text.

Visual row from the current skill matrix:

```text
effect.mid[64] = [1,1,18,0,-1,-1,0, 1,1,15,0,-1,-1,0]
```

Interpretation:

- producer visual chunk 0: `speffect 18`, AH type 9;
- producer visual chunk 1: `speffect 15`, AH type 1;
- no normal damage frame should be committed.

Status: `PORTED/PARTIAL`.

Existing smoke `battle_phase9w_skill64_selected_buff_copy` checks no fake damage and the q() post-effect trace.

## Source Buff11 Apply Proof

Source: `modules/source_code/decoded/decompiled_source_cfr/game/b.java`

`game.b.a(byte by, int n2, int n3)` case `11`:

```text
v[11][1] = (short)n2
b2 = game.d.a().d[n2]
for i in 0 .. b2.N[0]:
    this.a(b2.x[0][i], b2.v[b2.x[0][i]][1], game.d.a().d[n2].K[i])
b2.D()
```

Meaning:

| Step | Source behavior |
| --- | --- |
| store donor | `v[11][1]` stores selected real donor slot |
| read donor | donor unit is `game.d.a().d[v[11][1]]` |
| iterate buffs | source iterates donor active buff queue `x[0][]` / count `N[0]` |
| copy each buff | caster calls generic buff apply with donor buff id and donor stored value |
| clear donor | donor `D()` clears all active buffs and resets mutable stats/queue |
| activate buff11 | after switch, generic apply sets `v[11][0] = 3`, queues icon `23`, active flag `1` |

Important nuance:

The copied value is passed into the helper, but many buff cases recompute their stored value from the caster's base stats or source skill row. Therefore exact copied numeric value is buff-id dependent; do not assume raw donor `v[id][1]` remains unchanged after copy.

Status: `PORTED/PARTIAL`.

Rebuild equivalent:

```text
BattleUnit.copySourceBuffsFrom(source, selectedIndex, sourceSkill)
```

Current rebuild copies active donor buffs, clears donor buffs, then applies buff11 storing `selectedIndex`.

## Source Buff11 Active Tick Proof

Source: `modules/source_code/decoded/decompiled_source_cfr/game/b.java`

`game.b.o(int n2)` case `11` repeats the donor copy/clear:

```text
b2 = game.d.a().d[this.v[11][1]]
for i in 0 .. b2.N[0]:
    this.a(b2.x[0][i], b2.v[b2.x[0][i]][1], game.d.a().d[this.v[11][1]].K[i])
b2.D()
```

Meaning:

- buff11 is not only a cast-time steal;
- each active queue application can steal the current donor's active buffs again;
- after each steal, donor buffs are cleared again;
- then normal active duration decrement runs through source active queue lifecycle.

Rebuild gap:

```text
BattleUnit.tickSourceBuff(11, queueSlot)
```

currently falls through default and only decrements duration. It does not repeat donor copy/clear because it lacks access to the battle-unit vector.

Status: `MISSING / PENDING`.

This is the main next code slice.

## Source Cleanup When Donor Leaves

Source: `modules/source_code/decoded/decompiled_source_cfr/game/d.java`

Multiple source branches remove buff11 when its referenced donor is no longer valid:

```text
if d[n].m(11) && d[d[n].v[11][1]].equals(leavingUnit):
    d[n].n(11)
```

Seen in:

- enemy defeat / replacement path;
- player pet switch path;
- post-damage KO/result transition path.

Meaning:

If the donor pet leaves, dies, or is replaced, any unit whose buff11 points to that donor must clear buff11.

Rebuild status:

- `battle_p5_status11_cleanup` exists and smoke-covers one switch cleanup path;
- broader KO/replacement cleanup needs coverage against P15/P9/P8 flow.

Status: `PORTED/PARTIAL`.

## Current Rebuild Coverage

| Area | Existing checkpoint | Current status |
| --- | --- | --- |
| skill64 no-damage path | `battle_phase9w_skill64_selected_buff_copy` | PASS historically; not currently part of `battle_quick` |
| selected slot store `0` | `battle_phase9w_skill64_selected_buff_copy` | PORTED/PARTIAL; one target only |
| copy donor buff2 to caster | `battle_phase9w_skill64_selected_buff_copy` | PORTED/PARTIAL |
| clear donor buff2 | `battle_phase9w_skill64_selected_buff_copy` | PORTED/PARTIAL |
| caster receives buff11 icon/state | `battle_phase9w_skill64_selected_buff_copy` | PORTED/PARTIAL |
| P5 switch cleanup | `battle_p5_status11_cleanup` | PORTED/PARTIAL |
| active tick repeats donor copy/clear | none | MISSING |
| donor slot not zero | none | PENDING |
| KO/replacement cleanup | none focused | PENDING |
| exact visual pixel parity | none | PENDING |

## Risks / Edge Cases

| Risk | Why it matters |
| --- | --- |
| Donor slot stale | Source stores a real `d[]` slot. If the donor leaves, source clears buff11 references. Rebuild must avoid stealing from stale render-only objects. |
| Active tick needs vector access | `BattleUnit.tickSourceBuff()` alone cannot find `game.d.a().d[v[11][1]]`; runtime-level wrapper likely needs to handle case11. |
| Copy while iterating donor queue | Source iterates `b2.N[0]` then clears donor at the end. Rebuild should snapshot donor active queue before clearing to avoid mutation during copy. |
| Copy value semantics | Some buff apply cases recompute from caster stats; tests should assert effect shape, not raw donor value for every buff. |
| Buff11 copying itself | Source copies whatever active buffs are in donor queue. If donor has buff11, recursive/reentrant behavior is possible. Avoid broad generic behavior until a source route requires it. |
| Multi-target parity | Current smoke selected slot `0` can pass accidentally in one-enemy battles. Need at least one smoke with donor slot `1` or higher. |

## Recommended Next Code Slice

Do not genericize the whole event/battle VM.

Smallest source-backed slice:

1. Add runtime-level support for active tick case11:
   - when active queue applies buff11, resolve donor by stored `buffSlots[11][1]`;
   - copy donor active buffs to owner;
   - clear donor buffs;
   - then decrement buff11 duration through existing duration lifecycle.
2. Keep the existing cast-time `copySourceBuffsFrom()` path.
3. Add smoke checkpoints:
   - `battle_status_buff11_skill64_selected_buff_copy`;
   - `battle_status_buff11_active_tick_resteal`;
   - `battle_status_buff11_donor_switch_cleanup`;
   - `battle_status_buff11_selected_slot_nonzero`.
4. Add these focused checkpoints to `battle_quick` only after they are stable.

Expected verification after code:

```text
build.ps1
com.vqsv.rebuild.Main --check
VqsvBattleDamageFormulaCheck
VqsvIntroDemo --smoke-suite battle_quick build_intro_demo/suite_battle_quick
git diff --check
mojibake scan for touched Java/docs
```

## Decision

Buff11 should remain:

```text
PORTED/PARTIAL
```

until active-tick re-steal, nonzero donor slot, and cleanup-on-KO/replacement are smoke-covered.

## Closeout Update

Implemented in `276_battle_buff11_thau_thu_closeout.md`:

- `battle_status_buff11_skill64_selected_buff_copy`
- `battle_status_buff11_active_tick_resteal`
- `battle_status_buff11_selected_slot_nonzero`
- `battle_status_buff11_donor_switch_cleanup`

Current status remains `PORTED/PARTIAL`: focused source-shaped donor copy/clear is covered, but multi-enemy/full `game.d.d[]` slot parity, broader KO/replacement cleanup, and original-client pixel comparison are still not fully closed.
