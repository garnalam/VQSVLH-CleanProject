# 277 - Battle Buff12 Gia Toc Audit And Closeout

Scope: buff12 `Gia Toc` / skill65 source audit plus focused runtime smoke.

## Source Rows

```text
skill65 = [6,182,594,0,1,10,1,12,-1,1]
buff12  = [345,360,2,-1,-1]
effect.mid[65] = [1,1,16,0,-1,-1,0, 1,1,15,0,-1,-1,0]
```

## Source Chain

| Source | Fact |
| --- | --- |
| `game.d.q()` | For skill65/default effect mode, applies buff id `12` via `((b)h.p).a((byte)12, -1, skillId)`. |
| `game.b.a(12, value, sourceSkill)` | Sets `K[12] = 1`, queues buff, duration from row `2`, active flag `v[12][4] = 1`. |
| `game.b.o(12)` | Active tick sets `K[12] = 2`, then duration lifecycle ticks. |
| `game.b.a(byte skill,b target)` | Consumes one PP, but if `m(12) && K[12] == 1`, refunds one PP. |
| `game.d.q()` follow-up | If attacker has buff12 and `K[12] == 2`, source decrements `K12`; if target is alive, routes back to P2 for another attack. |

## Visual/UI

| Surface | Source-backed result |
| --- | --- |
| Producer skill65 | P7 effect row uses speffect `16`, then speffect `15`. No normal damage frame/hitroll because power is `0`. |
| Status icon | Buff icon cell `24`; duration cells `136` for duration `2`, then `135` for duration `1`. |
| P12/P13 body visual | None. Source active visual gate includes buff ids `3,5,13`; buff12 is excluded. |

## Implementation Status

Existing runtime already had partial support:

- `BattleUnit.applySourceBuff(12)` sets `effectScratch[12] = 1`.
- `BattleUnit.tickSourceBuff(12)` sets `effectScratch[12] = 2`.
- `BattleUnit.consumeSkillPp()` refunds PP when buff12 is active and `K12 == 1`.
- `SourceBattleRuntime.tryEnterP7FollowUpAction()` routes `K12 == 2` to P2 and sets `K12 = 1`.

This closeout adds dedicated smoke coverage and battle_quick regression entries instead of changing the core scheduler.

## Focused Smoke Checkpoints

```text
battle_status_buff12_before_no_effect
battle_status_buff12_skill65_speffect16
battle_status_buff12_skill65_speffect15
battle_status_buff12_after_apply_k1
battle_status_buff12_p13_promotes_k2_no_body_visual
battle_status_buff12_pp_refund_followup
battle_status_buff12_expiry_clears_icon
```

## Current Classification

```text
PORTED/PARTIAL
```

Reason: skill65 producer, K12 apply/tick/follow-up, PP refund, icon/duration, and no-body-visual gate are source-backed and smoke-covered. Full global turn-vector parity, multi-actor edge cases, and original-client pixel comparison remain partial.

## Next

Next table-order slice after regression: buff13 `Thach Hoa`.
