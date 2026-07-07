# 61 Battle Skill 15 Chunk4 Audit

Status: SOURCE AUDIT + IMPLEMENTATION TARGET.

Scope: skill `15` only, to port the smallest proven `effect.mid` chunk `[4]`
advance trigger in P7. This document intentionally does not cover chunk
`[5]/[6]`.

## Source Facts

| Data | Row | Meaning | Status |
| --- | --- | --- | --- |
| `aq.c[1][15]` | `[1,132,544,0,1,10,1,3,-1,1]` | Skill table row. The power field currently used by rebuild damage formula is `0`, so this is a no-damage skill in the current P7 damage path. | PORTED/PARTIAL |
| `effect.mid[15]` | `[0,0,33,0,0,-1,0, 0,1,7,0,-1,-1,0]` | Two chunks. Chunk0 is target-side actor animation `33` with `[4] == 0`; chunk1 is target-side `speffect 7`. | PORTED/PARTIAL |
| `speffect.mid[7]` | `[9,120,218,217,169,0,9,9]` | AH type `9`. Current rebuild renderer already supports AH type 9 as a source-shaped overlay. | PORTED/PARTIAL |

## Expected Source Shape

From `game.d.n()` and P7 actor-animation branches:

- Chunk0 enters through `actor.a(33, 0)`.
- Because `effect.mid[15]` chunk0 has `[4] == 0`, source can advance to the
  next chunk when `actor.u.a(0)` reports that frame trigger.
- Chunk1 then creates `H = new ah()` from `speffect.mid[7]`.
- Exact MIDP actor frame timing is still not fully modeled in rebuild, so the
  chunk4 trigger is implemented as source-shaped, not pixel-perfect.

## Smoke Checkpoints

| Checkpoint | Purpose |
| --- | --- |
| `battle_elder_p7_skill15_start.png` | P7 has entered skill 15 and is still on chunk0 actor animation. |
| `battle_elder_p7_skill15_chunk4_trigger.png` | Chunk0 `[4] == 0` advanced into chunk1 and AH type 9 overlay is visible. |
| `battle_elder_p7_skill15_after.png` | Skill 15 exits P7 without fake damage. |

## Implementation Boundary

Allowed:

- `VqsvBattleRuntime`: add source-shaped chunk `[4]` advance for non-special
  actor animation chunks only.
- `VqsvSmokeHarness`: add skill 15 smoke checkpoints and a skill-seed helper.

Not allowed in this slice:

- Port chunk `[5]/[6]`.
- Change intro/world/panel/scene scripts.
- Claim pixel-perfect P7 animation.

## Honest Status

- Skill 15 row mapping and speffect type are source-backed.
- The rebuild trigger is PORTED/PARTIAL because it uses simplified animation
  cursor timing instead of the original `actor.u.a(frame)` runtime.
- `[5]/[6]`, exact `H.i()` start condition, actor hide/restore, and
  multi-flag sequencing remain PENDING globally.
