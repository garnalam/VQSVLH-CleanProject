# 69 Battle Skill 15 Actor U33 Frame Trigger Port

Status: PORTED/PARTIAL.

Scope: one source-backed actor `u` action with a real frame trigger. This closes
the smallest proven gap after skill `10`, which had an actor action but no
`effect.mid` trigger.

## Source Facts

| Source | Fact | Rebuild status |
| --- | --- | --- |
| `script/decoded/data__script__effect.mid.json` | `effect[15] = [0,0,33,0,0,-1,0, 0,1,7,0,-1,-1,0]` | PORTED |
| `effect[15] chunk0` | `[0,0,33,0,0,-1,0]`: target-side non-special actor `u`, effect id `33`, state `0`, chunk `[4] == 0` | PORTED/PARTIAL |
| `effect[15] chunk1` | `[0,1,7,0,-1,-1,0]`: target-side special effect `H`, speffect id `7` | PORTED/PARTIAL |
| `speffect.mid[7]` | `[9,120,218,217,169,0,9,9]`: AH type `9` overlay | PORTED/PARTIAL |
| `game.d.n()` | non-special chunk calls target/attacker `a(effectId,state)`; special chunk creates `H = new ah()` | PORTED/PARTIAL |
| `game.b.a(short, byte)` | creates actor `u = new ah(new short[]{effectId,state,direction})` and starts it | PORTED/PARTIAL |
| `ah.java` default actor mapping | effect id `33` maps through `x[33 - 20]` to sprite `308` | PORTED |
| P7 case in `game.d` | checks `u.a(chunk[4])` and advances to next chunk on that animation-frame trigger | PORTED/PARTIAL |

## Candidate Scan

Scanning all `effect.mid` rows for non-special actor chunks where `[4]` or
`[5]` is not `-1` found only this candidate:

| Skill | Chunk | Row | Meaning |
| --- | --- | --- | --- |
| `15` | `0` | `[0,0,33,0,0,-1,0]` | target actor `u33`, state `0`, frame `0` advances to chunk1 |

No other non-special actor `u` trigger candidate is currently known from
`effect.mid`.

## Rebuild Checkpoints

| Checkpoint | Asserted behavior | Status |
| --- | --- | --- |
| `battle_elder_p7_actor_u33_start.png` | skill 15 reaches P7, starts target-side actor `u33`, sprite `308`, state `0`, cursor `0`. In this elder smoke, elder acts first, so the target side is the player side. | PORTED |
| `battle_elder_p7_actor_u33_to_h7.png` | same target-side actor at cursor `0` advances through chunk `[4]` into speffect `7`, AH type `9` | PORTED/PARTIAL |
| `battle_elder_p7_skill15_after.png` | skill 15 exits P7 without fake damage because skill power is zero | PORTED |

## Honest Limits

- The trigger is source-shaped through sprite cursor equality, but still has not
  been pixel-compared against the original MIDP runtime.
- This only proves `chunk[4]` frame-trigger advance for skill `15`.
- `chunk[5]/[6]` state triggers are implemented in the runtime but no separate
  source-backed effect-row smoke candidate was found in this scan.
- AH type `9` rendering remains PORTED/PARTIAL until MIDP pixel comparison.
