# 357 Battle P7 Attacker Action Before Target Effect Audit

## Target

Fix the shared P7 skill timeline after the S60 asset merge:

1. player/enemy attacker enters source attack/action state first;
2. target-side skill effect starts after that action gate;
3. damage text / HP tween still happens after the source effect sequence.

This is a runtime sequencing fix, not a per-skill animation patch.

## Source Chain

Source class for the current merged dump is `game.a`.

`game.a` P7 entry does:

| Source point | Meaning | Status |
| --- | --- | --- |
| `case 7` entry calls `I()` | Loads `effect.mid` row `ap[this.r.E]`, sets current chunk `U`, and creates target/attacker effect object from chunk fields. | PORTED/PARTIAL |
| after `I()`, if `Z[U*7] == 0` | Source calls attacker `r.a((byte)1, true)` and returns. This is the attacker action state before target-side playback. | PORTED/PARTIAL |
| `game.i.a(byte 1, true)` | Sets base sprite state 1 and creates species-specific action overlay `M` for supported species. | PORTED/PARTIAL |
| `game.i.a(Graphics)` | Starts species action overlay `M.a()` only when base action sprite reaches a species-specific cursor. | PORTED/PARTIAL |
| later P7 update | Target `v` / battle `S` effect progresses and then P7 can enter hit/damage/result handling. | PORTED/PARTIAL |

Important correction: old handoff references to `game.d` were historical. In the current S60-merged source tree, this battle runtime is `modules/source_code/decoded/decompiled_source_cfr/game/a.java`.

## Rebuild Problem

Before this slice, `SourceBattleRuntime.prepareP7SourceState()` called `enterP7SourceChunk(... "initial")` immediately. That prepared the target-side actor/special effect and base attacker state in the same setup path, and `tickP7ActorAnimation()` could start the target effect on the first P7 phase-1 tick.

Visible result reported by manual Battle Lab: target skill effect appeared before the attack/action animation of the user's pet.

## Patch

Added a source-shaped initial target-effect gate:

| Rebuild field/path | Behavior | Status |
| --- | --- | --- |
| `p7InitialTargetEffectGatePending` | Set only for initial `effect.mid` chunk whose side is target (`chunk[0] == 0`). | PORTED/PARTIAL |
| `tickP7SourceEffectSequence()` | Holds target actor/special playback until attacker base state 1 reaches cursor >= 1. | PORTED/PARTIAL |
| gate release | Emits one standalone frame where attacker action is visible and target effect is still hidden; next tick starts target effect. | PORTED/PARTIAL |

No damage formula, PP, RNG, skill row, `effect.mid`, or asset mapping changed.

## Smoke Coverage

New checkpoints:

| Checkpoint | Expected |
| --- | --- |
| `battle_skill10_direct_attacker_action_gate` | P7 phase 1, attacker is player, player base state 1 cursor >= 1, target actor/special/damage not visible. |
| `battle_skill0_direct_attacker_action_gate` | Same order guard for fire representative. |
| `battle_skill20_direct_attacker_action_gate` | Same order guard for earth representative. |
| `battle_skill30_direct_attacker_action_gate` | Same order guard for water representative. |
| `battle_skill40_direct_attacker_action_gate` | Same order guard for metal representative. |
| `battle_skill60_direct_attacker_action_gate` | Same order guard for special representative. |

Existing actor/damage checkpoints still assert target effect before damage and damage after effect.

## Classification

| Area | Status | Note |
| --- | --- | --- |
| P7 attacker state 1 before target effect | PORTED/PARTIAL | Source ordering is now represented by a gate, but not pixel-compared with original client. |
| Species action overlay `M` | PORTED/PARTIAL | Existing `battle state1 L` support is reused. Full source overlay parity remains partial. |
| Target actor/special effect rows | PORTED/PARTIAL | Still sourced from `effect.mid` / `speffect.mid`; existing renderer coverage applies. |
| P7 pixel-perfect timing | PENDING | Needs original-vs-rebuild frame capture compare. |

## Next

Run focused P7 order smokes, then Battle Lab fire/wood representative suites. If pass, continue skill group roadmap from the next element group; do not reopen P7 unless a source-route mismatch or original capture proves another gap.
