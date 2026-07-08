# 108 Battle P5/P15 Pet Switch Source Audit

Status: SOURCE AUDIT ONLY, NO CODE CHANGES.

Scope:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/script/decoded/data__script__cpos.mid.json`
- `modules/ui/decoded/data__ui__petstate.ui.json`

## Executive Summary

Battle pet switch in the original game is not a simple menu selection.

The source flow is:

```text
P20 command "Sủng vật"
-> P5 opens /data/ui/petstate.ui
-> game.h.X() handles pet list input
-> game.d.a(selectedRow) validates and reorders party f[]
-> valid switch sets state P15
-> P15 plays cpos/an transition for the incoming pet
-> battle returns to P20/P13/P1 depending on previous state and active queue context
```

Important: P5 is the selection UI state. P15 is the deployment/replacement
animation/state transition. A rebuild must not treat P5 confirm as an
instant active-pet swap with no transition.

## Source Matrix

| Piece | Source | Behavior | Rebuild Status |
| --- | --- | --- | --- |
| P5 entry | `game.d.a(byte)` case `5` | `S.c = 0; S.W();` opens petstate selection UI. | `PARTIAL`; current rebuild has petstate overlay, but needs source validation parity. |
| P5 update | `game.d.b()` case `5` | Calls `S.X()` every tick. | `PARTIAL`; current rebuild should be checked against `game.h.X()` confirm/back flow. |
| P5 draw | `game.d.a(Graphics)` case `5` | Draws selected outgoing/incoming pet name at center y=200. | `PENDING/PARTIAL`; rebuild UI is richer but may not match this state draw exactly. |
| petstate open | `game.h.W()` | Sets `b=0`, calls `e(c)`. | `PARTIAL`; current rebuild opens petstate, but source fields `b/c/f` need mapping. |
| petstate setup | `game.h.e(int)` | Loads `/data/ui/petstate.ui`, fills 6 party rows. In battle branch, rows use `d.f[]` order, not raw bag order. | `PENDING`; critical to avoid Neil/player fallback or wrong pet order. |
| HP bar row | `game.h.e(int)` | Widget ids `16+i*6` get `#P + pet.L()`; ids `17+i*6` get `#P + pet.O()`. | `PARTIAL`; current rebuild must render source party HP/energy/percent, not generic text only. |
| battle command label | `game.h.e(int)` | If owner is `game.d` and state P5, widget `75` text is `Xuất chiến`; widgets `63/64` hidden. | `PENDING`; current rebuild pet switch UI needs this softkey parity. |
| P5 confirm | `game.h.X()` | If owner is `game.d`, calls `((d)o).a(this.b)`. Return `0` warns cannot battle; return `1` warns already deployed; return `-1` performs valid switch setup. | `PENDING`; this is the main logic gap. |
| cannot battle warning | `game.h.X()` | `Sủng vật này không thể tham chiến`, opens `/data/ui/msgwarm.ui`, hides petsetting. | `PENDING`. |
| already deployed warning | `game.h.X()` | `Sủng vật này đã đặt ở vị trí chiến đấu`, opens `/data/ui/msgwarm.ui`, hides petsetting. | `PENDING`. |
| valid switch | `game.h.X()` | Calls `d.a(d.g, 0)`, sets `S.a=0`, `battle.a((byte)15)`, closes petsetting/petstate. | `PENDING`; current rebuild must not skip P15. |
| P5 back | `game.h.X()` | If owner is `game.d`, and previous state is not P7/P13, closes petstate, sets `game.d.k=false`, `S.a=0`, battle state `20`. | `PENDING/PARTIAL`. |
| validate selected pet | `game.d.a(int selectedRow)` | Checks selected pet via `c(f[selectedRow])`. If dead/not alive returns `0`; if already active `K()` returns `1`; else valid returns `-1`. | `PENDING`; current rebuild must use this exact tri-state. |
| party reorder | `game.d.a(int selectedRow)` | Moves selected `f[selectedRow]` to `f[0]`, shifts earlier entries right. | `PENDING`; critical for subsequent battle order and UI row order. |
| active vector update | `game.d.a(int selectedRow)` | Adds selected unit to vector `x` if absent, sets selected `J=true`, `d(true)`, disables old active `h.d(false)`, resets old `h.F=0`. | `PENDING`. |
| status cleanup | `game.d.a(int selectedRow)` | Clears status `11` from units linked to old active `h`. | `PENDING`; affects full status parity. |
| P15 entry | `game.d.a(byte)` case `15` | Sets `S.a=0`, `y=true`, `G=g`, `E[G]=0`, writes `d[g]` into active vector slot, marks `d[g].J=true`, hides marker `b(g,false)`, increments `i`. | `PENDING`; this is replacement/deployment setup. |
| P15 update | `game.d.b()` case `15` | Advances `E[G]` through `game.d.an[r][G]` cpos frames; moves `d[G]` by cpos coordinates. | `PENDING`; source-backed cpos transition missing/approx in rebuild. |
| P15 draw | `game.d.a(Graphics)` case `15` | Draws battle actors without command UI. | `PENDING/PARTIAL`. |
| P15 completion | `game.d.b()` case `15` | Returns to P20/P13/P1 depending on previous state `Q`, active queue, `k`, and remaining units. | `PENDING`; do not hardcode always P20. |

## game.b Field/Method Facts Needed By P5

| Method/Field | Meaning In This Slice |
| --- | --- |
| `b.S()` | Alive check. Source P5 rejects selected pet when `!S()`. |
| `b.K()` | Already active/deployed check. Source P5 returns `1` when true. |
| `b.L()` | HP percent for petstate row widget `16+i*6`. |
| `b.O()` | Secondary percent for petstate row widget `17+i*6` (source uses `S * 100 / u()`). |
| `b.q()` | Species id, used for display/name. |
| `b.C` | Visual id/sprite cell source for pet display widget. |
| `b.J` | Turn/deployment flag; set true on selected pet. |
| `b.d(true/false)` | Active/deployed visual/runtime flag. |
| `b.F` | Runtime counter reset on old active during switch. |
| `b.m(11)/n(11)/v[11][1]` | Status link cleanup targeting old active pet. |

## cpos.mid / P15 Transition

`data__script__cpos.mid.json` has three groups. `game.d` loads it into
`game.d.an` and indexes it as:

```text
game.d.an[battleBackgroundOrMode][actorSlot][frame * 4 + xy]
```

P15 uses:

```text
this.d[this.G].b(an[this.r][this.G][E[G]<<2],
                 an[this.r][this.G][(E[G]<<2)+1])
```

So P15 incoming-pet movement should be driven by cpos rows, not by a hand-made
teleport. Current rebuild cpos usage exists for battle entry, but pet-switch
P15 transition remains `PENDING`.

## UI Resource Notes

`/data/ui/petstate.ui` decoded facts:

- Main frame widget id `1`, x43 y55 w158.
- Title widget id `2`, text is `Sủng vật trong hành trang`.
- Row style has six visible rows.
- In battle mode, `game.h.e(int)` hides widgets `63/64`.
- In battle P5, widget `75` text is `Xuất chiến`.
- Row widgets `16+i*6` and `17+i*6` are source percent bars/text markers.

## Current Rebuild Risks To Check Before Coding

- Pet switch UI may display Neil/player fallback instead of active source pet.
- Party order may be raw `sourcePets` order instead of source `d.f[]` order.
- Confirm may instant-swap active pet without P15 cpos transition.
- Dead/current pet disabled checks may be missing or only cosmetic.
- Back from P5 may not restore P20/source state cleanly.
- Forced replacement after death may be conflated with manual P5.

## Recommended Code Slice

Do not implement all P5/P15 at once. Recommended next slice:

1. Add battle party order mapping equivalent to `game.d.f[]`.
2. Port `game.d.a(selectedRow)` tri-state validation:
   - `0`: dead/cannot battle warning.
   - `1`: already deployed warning.
   - `-1`: valid switch.
3. Keep current P15 visual minimal but source-shaped:
   - selected pet becomes active only through a P15 phase.
   - record trace that cpos transition is still `PARTIAL` if exact frames are not yet rendered.
4. Smoke:
   - P5 list shows source party, no Neil fallback.
   - current pet confirm shows already-deployed warning.
   - dead pet confirm shows cannot-battle warning.
   - valid pet confirm enters P15 then returns to P20 with active pet changed.
   - back from P5 returns to P20.

## Verification Needed After Code

- `build.ps1`
- Java mojibake scan.
- `VqsvBattleDamageFormulaCheck`.
- Smoke P5 list/current/dead/valid/back.
- Existing route smoke: Sophie, Bunny, Elder.
