# 75 Battle `game.d.q()` Post-Skill Matrix

Status: PORTED/PARTIAL.

Scope: this document covers the first half of `game.d.q()`: post-skill
heal/buff/leech/reflect side effects after P7 damage/effect playback. It does
not claim the later turn-advance, forced replacement, win/lose, P12/P13 active
queue lifecycle, P15, P22, or P23 flows.

## Source Anchors

| Source | Meaning |
| --- | --- |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java:1971` | `private boolean q()` post-P7 resolver. |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java:1980` | `aF` stores pre-effect HP for the unit whose HP bar will refresh. |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java:1982..2021` | Hardcoded skill post-effects. |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java:2023..2046` | Status 8 heal, buff 2 reflect, buff 5 stored reflect, floating HP text. |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java:2048..2063` | `effectMode == 1` buff name floating text. |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java:2064..2073` | HP bar refresh target based on `skill[9]`. |
| `modules/source_code/decoded/decompiled_source_cfr/game/b.java:482` | `game.b.a(byte,int,int)` source buff application. |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java:832..845` | Skill `52/58` set battle flag `aa` with a 30 percent source-shaped chance. |

## Rebuild Anchors

| Rebuild | Responsibility |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java:2035` | `applyP7PostSkillEffects(...)`, the current `q()` post-skill equivalent. |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java:2116` | `applyP7SourcePostDamageModifiers(...)`. |
| `rebuild_game/src/main/java/VqsvBattleUnit.java:308` | `applySourceBuff(...)`, port of `game.b.a(byte,int,int)` for buff ids 0..15. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Smoke checkpoints for heal, buff, leech and route regressions. |

## Skill Post-Effect Matrix

| Source branch | Skill ids / condition | Source behavior | Rebuild equivalent | Status |
| --- | --- | --- | --- | --- |
| `case 11,17` | `11,17` | Heal attacker by `h.B() * skill[8] / 100`, minimum 1. | Heals attacker using rebuild damage-model base attack helper and `skill[8]`; smoke `battle_elder_p7_q_heal_skill11`. | PORTED/PARTIAL: source formula shape is represented, but exact `h.B()` parity remains tied to full formula work. |
| `case 21,27,42,48,62,68` | hardcoded self buff | Apply buff `skill[7]` to attacker via `h.a(...)`. | Applies `row.effectId` to attacker via `BattleUnit.applySourceBuff(...)`; smoke covered by skill 45/default and route checks where available. | PORTED/PARTIAL: buff storage/effect ids are ported; active queue lifetime is P12/P13 work. |
| `case 52,58` | leech skills | If `aa` is true, heal attacker by `Z[0] * skill[8] / 100`; source falls through to `case 64`. `aa` is set source-side by a 30 percent chance for these skills. | Rebuild now checks `p7DamageApplied && p7Damage > 0` plus `rollSourceChance(30)`, heals attacker, then preserves fall-through effect-call semantics with `buffId = row.effectId`. Smoke uses `battle_elder_p7_q_leech_skill58` because current skill 52 elder-smoke damage rounds heal to 0 and correctly shows no plus text. | PORTED/PARTIAL: post-skill leech chance/side effect is source-shaped. Exact timing of `aa` assignment remains inside the simplified P7 runtime. |
| `case 64` | copy/selected-index buff | Apply buff `skill[7]` using `h.I` selected index/target. | Copies target buffs through `copySourceBuffsFrom(...)` when target is available; stores selected target slot. | PORTED/PARTIAL: target slot path exists; full multi-pet target parity waits for P5/P12/P13 integration. |
| `default effectMode == 1` | other `skill[6] == 1` | Apply buff/effect id `skill[7]` to `h.p`. | Applies `row.effectId` to current P7 target. | PORTED/PARTIAL: matches current P6/P7 target model; exact source target vector parity still belongs to full state-machine work. |

## Post-Damage Modifier Matrix

| Source branch | Source behavior | Rebuild equivalent | Status |
| --- | --- | --- | --- |
| `h.f(8)` | If attacker has form/status 8 and chance passes, heal attacker by `Z[0] * aq.c[3][8][6] / 100`. | `applyP7SourcePostDamageModifiers()` checks `hasSourceFormStatus(8)`, rolls source chance, heals attacker. | PORTED/PARTIAL: data columns are used; status lifecycle waits for P12/P13. |
| `target.m(2)` | Target buff 2 reflects `Z[0] * target.v[2][2] / 100` to attacker. | Checks target buff 2 and damages attacker by the same percent. | PORTED. |
| `target.m(5)` | Target buff 5 reflects stored attacker `K[5]`, then clears it. | Checks target buff 5, consumes attacker stored reflect damage, damages attacker, clears scratch. | PORTED/PARTIAL: storage hook exists in damage formula; full active queue still P12/P13. |
| HP delta text | If attacker HP fell below `aF`, show negative; otherwise if `n3 > 0`, show plus heal. | Shows one post-effect text: reflect damage, heal, or buff text depending on visible result. | PARTIAL: original can spawn multiple floating text objects; rebuild currently exposes one representative P7 post-effect text. |
| `effectMode == 1` text | Shows `aq.c[6][effectId][0]`; hardcoded self-buff skills draw over attacker, default draws over target. | Displays buff name in `battleP7PostEffectText` when no higher-priority HP delta text is selected. | PORTED/PARTIAL: text/source side is represented, but multi-text concurrency is still simplified. |
| HP bar refresh | Refreshes attacker or target HP bar based on `skill[9]`. | Render state reads current `BattleUnit` HP after post-effect changes. | PORTED/PARTIAL: visual HP bar update is integrated with current renderer, not the original `game.h` HP animation object. |

## Smoke Coverage

| Checkpoint | Covers |
| --- | --- |
| `battle_elder_p7_damage_result_debuff` | P7 preserves damage result/debuff text before `q()`. |
| `battle_elder_p7_q_heal_skill11` | Hardcoded `11/17` heal branch. |
| `battle_elder_p7_q_buff_skill45` | Default `effectMode == 1` buff-name branch. |
| `battle_elder_p7_q_leech_skill58` | Hardcoded `52/58` leech branch with deterministic source-shaped 30 percent roll and visible `+` heal text. |
| `route_sophie_after_battle_branch` | P7/q completion still returns to Sophie branch. |
| `route_bunny_after_battle_task` | P7/q completion still returns to Bunny task route. |
| `route_elder_after_battle_reward_state` | P7/q completion still reaches elder reward/state route. |

## Remaining Before P12/P13

No additional `game.d.q()` post-skill branch is intentionally left unrepresented
in the current rebuild layer. The remaining gaps are boundaries of later work:

- P12/P13 active buff/debuff queue duration and tick behavior.
- P15 forced replacement after death.
- P22/P23 EXP, level-up and learn-skill UI.
- Full original `game.h` HP-bar animation object.
- Multiple simultaneous floating text objects from source `d.a(...)`.
- Full validation of damaged `SOURCE_SWITCH_GAP` direct-damage paths.
