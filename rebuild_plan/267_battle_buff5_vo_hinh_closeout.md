# 267 - Battle Buff5 Vo Hinh Closeout

Date: 2026-07-13

Scope: close out `aq.c[6][5]` temporary self buff, producer skill `34`.

Status: PORTED.

## Source Audit

| Source | Proven fact |
| --- | --- |
| `modules/script/decoded/data__script__db.mid.json` | Buff row `aq.c[6][5] = [338,353,3,30,-1]`. |
| `modules/script/decoded/data__script__db.mid.json` | Skill `34 = [3,151,563,0,1,10,1,5,-1,1]`. |
| `modules/source_code/decoded/decompiled_source_cfr/game/b.java` | `game.b.a(byte,int,int)` case `5` stores `v[5][1] = aq.c[6][5][3]`, so chance is `30`. |
| `modules/source_code/decoded/decompiled_source_cfr/game/b.java` | Damage formula checks `target.m(5)` and `ae.a(100) <= target.v[5][1]`; on pass it stores the incoming damage in attacker `K[5]`. |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java` | `game.d.q()` consumes attacker `K[5]` and applies that damage back to the attacker, then clears `K[5]`. |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java` | Buff body visual gate `ai[0] = {3,5,13}` includes buff5. |
| `modules/script/decoded/data__script__effect.mid.json` | `effect.mid[34] = [0,1,4,0,-1,-1,0]`; producer visual is skill34's own speffect `4`. |
| `modules/script/original/bufDebuf.mid` through `VqsvBattleAnimationTables` | Active queue map for buff5 is `ar[0][5] -> ap[6] = [0,23,0,-1]`, a type0 actor action. |

## Formula / Runtime Shape

Apply:

```text
v[5][0] = 3
v[5][1] = 30
v[5][4] = 1
```

Reflect hook:

```text
if target has buff5 and ae.a(100) <= 30:
    attacker.K[5] = incomingDamage

later in game.d.q():
    if target has buff5 and attacker.K[5] > 0:
        attacker takes attacker.K[5]
        attacker.K[5] = 0
```

HUD:

```text
iconCell = buffId + 12 = 17
durationCell(3) = 134 + 3 = 137
durationCell(2) = 136
durationCell(1) = 135
duration 0 -> clear icon
```

## Implemented Runtime Fix

`VqsvBattleRuntime.applyP7SourcePostDamageModifiers()` already consumed stored buff5 reflect damage, but it did not sync the rendered attacker HP or trace the result.

Fixed:

- sync `p7Attacker.hp` after reflected damage;
- add trace `PORTED battle P7 buff5 Vo hinh reflect ...`;
- add smoke-only forced roll for `damage.buff5` to test success/fail deterministically.

No release UI/debug overlay was added.

## Implemented Smoke Checkpoints

| Checkpoint | What it locks |
| --- | --- |
| `battle_status_buff5_before_no_effect` | Baseline: no buff5 value/duration/icon before skill34 is used. |
| `battle_status_buff5_producer_visual` | Skill34 uses `effect.mid[34]`, speffect `4`, applies buff5 chance `30`, no damage/hitroll. |
| `battle_status_buff5_forced_reflect_success` | Forced roll `0 <= 30`: incoming damage `80`, reflected damage `80`, attacker HP `134 -> 54`. |
| `battle_status_buff5_forced_reflect_fail` | Forced roll `99 > 30`: incoming damage `80`, reflected damage `0`, attacker HP stays `134`. |
| `battle_status_buff5_p12_body_visual_start` | P13 active queue starts buff5 body visual using actor row `[0,23,0,-1]`, sprite `265`, state `0`. |
| `battle_status_buff5_expiry_clears_icon` | Duration/icon sequence `17/137 -> 17/136 -> 17/135 -> clear`. |

## Focused Smoke Results

| Stage | PNG | Numeric result |
| --- | --- | --- |
| Before | `rebuild_game/build_intro_demo/battle_status_buff5_before_no_effect.png` | buff5 inactive, value `0`, duration `0`, status icon count `0`. |
| Producer | `rebuild_game/build_intro_demo/battle_status_buff5_producer_visual.png` | value `30`, duration `3`, icon/duration `17/137`. |
| Reflect success | `rebuild_game/build_intro_demo/battle_status_buff5_forced_reflect_success.png` | incoming damage `80`, reflected `80`, attacker HP `134 -> 54`. |
| Reflect fail | `rebuild_game/build_intro_demo/battle_status_buff5_forced_reflect_fail.png` | incoming damage `80`, reflected `0`, attacker HP `134 -> 134`. |
| Body visual | `rebuild_game/build_intro_demo/battle_status_buff5_p12_body_visual_start.png` | active visual row `[0,23,0,-1]`, actor sprite `265`, duration `3`. |
| After expiry | `rebuild_game/build_intro_demo/battle_status_buff5_expiry_clears_icon.png` | duration reaches `0`, status icon count `0`. |

## Re-Audit

| Question | Result |
| --- | --- |
| Did we copy buff0/1/4 producer visual? | No. Skill34 asserts `effect.mid[34] = [0,1,4,0,-1,-1,0]`. |
| Did we invent a damage path for skill34? | No. Producer smoke asserts no damage frame and no hitroll. |
| Is reflect chance deterministic in smoke but normal in release? | Yes. Only smoke calls `debugSetNextDamageBuff5RollForSmoke`; normal runtime uses `VqsvSourceRandom`. |
| Does reflect happen on miss? | Not in these smokes. P7 side effects are committed only on hit; existing result-flow miss regression remains the guard. |
| Is P12/P13 body visual source-backed? | Yes. Source gate includes buff5, and runtime uses original `bufDebuf.mid` mapping `ar[0][5] -> ap[6] = [0,23,0,-1]`. |
| Did we confuse decoded row grouping with source row map? | Fixed. The closeout records the actual mapped row `[0,23,0,-1]`; no speffect17 claim remains. |

Remaining status:

- `PORTED`: producer visual, apply value/duration/icon, chance success/fail reflect, P12/P13 actor body visual, expiry.
- `PORTED/PARTIAL`: source-backed visual timeline is audited in `rebuild_plan/268_battle_buff5_visual_timeline_audit.md`, but there is still no original-client pixel compare for exact actor-effect frame placement.
- `PENDING`: broader multi-target/party vector reflect parity beyond current single-target battle runtime.

Next roadmap step: buff6 `Kien nhan`, which is already marked source oddity/partial and must be audited before code.
