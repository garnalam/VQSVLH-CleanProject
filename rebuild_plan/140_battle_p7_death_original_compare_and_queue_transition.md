# 140 Battle P7 Death Original Compare And Queue Transition

## Scope

Follow-up after `139_battle_p7_death_state3_transition_audit.md`.

Requested path:

1. Try original-client frame compare for AH type16 death effect.
2. If no original capture exists, move to the next P7 queue/result transition gap.

## Original-Client Frame Compare Check

Search result:

- `rebuild_game/original_smoke/captures/...` currently contains boot/music/fire startup captures only.
- No original-client/MIDP battle frame capture for P7 KO / AH type16 death effect was found.
- Existing `137_battle_p7_original_vs_rebuild_frame_compare.md` already states that P7 compare is source-asset-vs-rebuild, not original-client runtime parity.

Decision:

- Do not claim AH type16 pixel-perfect.
- Keep AH type16 death visual as `PORTED/PARTIAL`.
- Exact Java ME `l.a` / `drawRGB(..., true)` parity remains `PENDING` until a same-route original capture exists.

## Source Facts For Next Queue/Result Gap

After P7 damage/effect settles, source `game.d` case `7` calls `q()` when:

```text
B && A && q()
```

`q()` then:

- applies skill post-damage side effects such as heal/buff/debuff text;
- refreshes the HP bar through `S.a(...)` or `S.b(...)`;
- waits for floating text / HP bar completion via `V()` and helper `a(...)`;
- resolves death/replacement/result branches:
  - dead enemy with reserve can route P15;
  - all enemies defeated routes P8;
  - dead player routes P5/P9 depending reserve;
  - living unit advances turn/dispatch.

P12/P13 source entry builds active queue from the processed unit:

- scan buff queue `x[0][0..2]` first;
- scan debuff queue `x[1][0..2]` second;
- use `ap[ar[0][buffId]]` or `aq[ar[1][debuffId]]` visual rows;
- non-visual queue ids are applied immediately according to the source `ai` gate.

## Rebuild Checkpoints Added

No runtime behavior change in this slice. Added focused smoke assertions only:

| Checkpoint | Purpose |
| --- | --- |
| `battle_p7_to_p13_queue_order_skill45` | Proves P7 skill45 post-effect trace happens before P13 player active queue, and queue apply follows. |
| `battle_p7_to_p12_queue_order_debuff0` | Proves P7 skill1 damage/debuff trace happens before P12 enemy active queue, and queue apply follows. |

These checkpoints are stricter names around existing behavior already partially covered by `battle_p13_buff9_*` and `battle_p12_debuff0_*`.

## Status

| Area | Status | Note |
| --- | --- | --- |
| Original AH type16 frame compare | PENDING | No original battle KO capture exists in repo. |
| AH type16 death visual | PORTED/PARTIAL | Source-shaped sprite/anchor/lifetime; not Java ME pixel-certified. |
| P7 -> P13 queue order after player buff | PORTED/PARTIAL | Smoke trace-order checkpoint added. |
| P7 -> P12 queue order after enemy debuff | PORTED/PARTIAL | Smoke trace-order checkpoint added. |
| Full `game.d.q()` result parity | PARTIAL | Source has extra branches for follow-up action/status/result edge cases. |

## Next

If these checkpoints pass, the next roadmap-consistent slice is to audit `game.d.q()` remaining branches that are not yet proven by smoke:

- skill/status follow-up action branches around status `12` and skills `63/69`;
- exact post-`q()` turn cursor advance `i++/p()` parity;
- any missing P5/P8/P9/P15 edge path not already covered by route smoke.
