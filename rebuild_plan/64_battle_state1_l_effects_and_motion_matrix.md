# 64 - Battle State 1 L Effects And P7 Motion Matrix

Status: PORTED/PARTIAL for P7 lunge/recoil feel, PENDING for species-specific `L` renderer parity.

## Source Facts

- `game.b.d(byte)` state `1` calls `this.a.a(1, 0, true)` and creates `this.L = new ah(...)` only for species ids `0, 10, 91, 92, 97, 98, 62, 75, 87`.
- Source `L` speffects by species:
  - species `0` -> speffect `27`
  - species `10` -> speffect `28`, with `Z = 1`
  - species `91` -> speffect `26`
  - species `92` -> speffect `25`
  - species `97/98` -> speffect `23`
  - species `62` -> speffect `24`
  - species `75` -> speffect `20`
  - species `87` -> speffect `21`
- `game.b.a(Graphics)` starts `L.a()` only when `U == 1` and the base sprite animation reports frame `1` via `this.a.b(1)`. It draws `L` before/after the base actor depending on `Z`.
- Current Elder smoke battle does not use one of those species ids, so blindly adding `L` would not improve the visible Elder battle and would be fake.

## Rebuild Matrix

| Feature | Source | Rebuild Equivalent | Status |
| --- | --- | --- | --- |
| State 1 base animation | `game.b.d(1)` | `p7BaseState* = 1` on P7 attacker action | PORTED/PARTIAL |
| State 2 hurt pose | `game.b.d(2)` | target base state `2` during damage frame | PORTED/PARTIAL |
| State 3 down pose | `game.b.d(3)` | target base state `3` if HP reaches zero | PORTED/PARTIAL |
| Species `L` speffect creation | `game.b.z(int)` | not yet implemented as generic `L` overlay | PENDING |
| `L` start condition | `this.a.b(1)` frame trigger | known, not wired to generic battle actors | PENDING |
| P7 lunge/recoil feel | source actor states imply visible action/hurt phase; exact movement helper still needs deeper source trace | offsets on attacker during P7 phase 1 and target during damage phase | PORTED/PARTIAL |

## Implementation Notes

- Added render-state offsets to `VqsvIntroDemo.Scene`.
- `VqsvBattleRuntime.syncP7MotionOffsets()` now sets:
  - attacker lunge toward the target during P7 action phase;
  - target recoil/shake during P7 damage phase.
- `VqsvBattleRenderer` applies those offsets to base sprites, P7 actor effects, AH type overlays, and damage number placement.

## Remaining Work

- Audit a battle involving species `0/10/62/75/87/91/92/97/98` before porting the generic `L` overlay.
- Port exact `game.b.z(int)` AH payload drawing order, including `Z == 1` for species `10`.
- Pixel-compare P7 movement/recoil against MIDP original; current offset curve is source-shaped and visual, not bytecode/pixel-perfect.
