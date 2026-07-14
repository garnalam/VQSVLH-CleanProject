# 276 - Battle Buff11 Thau Thu Closeout

Scope: buff11 `Thau Thu` / skill64 donor-vector copy-clear runtime slice after audit `275`.

## Source Basis

Source-backed chain from audit `275`:

- `skill64 = [6,181,593,0,1,10,1,11,-1,0]`.
- `buff11 = [344,359,3,-1,-1]`.
- `game.d.i()` stores the selected target vector in `h.I` from real `game.d.d[]` slots.
- `game.d.q()` case64 calls `h.a((byte)11, h.I, skillId)`.
- `game.b.a(11, selectedSlot, skill64)` stores `v[11][1] = selectedSlot`, copies active buffs from donor `game.d.a().d[selectedSlot]`, then clears donor buffs with `D()`.
- `game.b.o(11)` repeats the same donor copy/clear while buff11 is active.
- Source cleanup scans remove buff11 when the referenced donor leaves/dies/switches.

## Implemented

| Area | Status | Notes |
| --- | --- | --- |
| Cast-time skill64 steal | PORTED/PARTIAL | Existing selected target copy/clear path retained and now has `battle_status_buff11_skill64_selected_buff_copy`. |
| Runtime active tick re-steal | PORTED/PARTIAL | Active queue and smoke tick wrappers call runtime-level donor resolve before `tickSourceBuff(11)`, matching `game.b.o(11)` shape. |
| Donor queue copy safety | PORTED | `copySourceBuffsOnlyFrom()` snapshots donor active buffs before clearing donor, avoiding mutation while iterating. |
| Nonzero donor slot smoke | PORTED/PARTIAL | `battle_status_buff11_selected_slot_nonzero` proves donor slot `1` maps to active player and copies/clears a real buff. |
| Donor switch cleanup | PORTED/PARTIAL | `battle_status_buff11_donor_switch_cleanup` proves stale buff11 reference is cleared when active player switches. |
| Status icon stale clear | PORTED | Status slot sync now checks `hasBuff/hasDebuff`, so duration-only stale queue entries no longer render. |

## Smoke Results

Focused PNG checkpoints:

```text
rebuild_game/build_intro_demo/buff11_thau_thu/battle_status_buff11_skill64_selected_buff_copy.png
rebuild_game/build_intro_demo/buff11_thau_thu/battle_status_buff11_active_tick_resteal.png
rebuild_game/build_intro_demo/buff11_thau_thu/battle_status_buff11_selected_slot_nonzero.png
rebuild_game/build_intro_demo/buff11_thau_thu/battle_status_buff11_donor_switch_cleanup.png
```

These checkpoints are now included in `battle_quick`.

## Current Classification

```text
PORTED/PARTIAL
```

Reason: the source-backed one-enemy/active-player donor flow is now covered, including active tick and switch cleanup. Remaining parity gaps are broader source `game.d.d[]` slot coverage in multi-enemy/multi-party cases, KO/replacement cleanup beyond the currently focused switch smoke, and original-client pixel comparison.

## Next

Move to table-order buff12 `Gia Toc` only after regression passes. Do not revisit buff11 unless a source-route mismatch appears.
