# 354 - Battle Buff/Debuff New Source Reaudit

Date: 2026-07-14

Status: REAUDIT AFTER S60 SOURCE/ASSET MERGE / NO RUNTIME PATCH REQUIRED.

Scope:

- Temporary battle buffs from `aq.c[6]`, rebuild `BattleUnit.buffSlots[id][0..4]`.
- Temporary battle debuffs from `aq.c[7]`, rebuild `BattleUnit.debuffSlots[id][0..4]`.
- This audit excludes held items/passives from `aq.c[3]`; those were reaudited separately in `353_battle_held_item_new_source_reaudit.md`.

## Why This Audit Exists

After the S60 source/resource merge, several source roles shifted in the
decompiled tree. Older docs often say `game.b` / `game.d`. In the current
source tree, the clearest buff/debuff runtime anchors are:

| Current source file | What it proves |
| --- | --- |
| `modules/source_code/decoded/decompiled_source_cfr/game/i.java` | Pet/battle unit state, buff apply `a(byte,int,int)`, buff tick `o(int)`, clear `n/C/D/E`, debuff apply/damage hooks, debuff tick `q(int)`, duration tick `c/d`. |
| `modules/source_code/decoded/decompiled_source_cfr/game/a.java` | Battle state machine, active queue gate `aj = {{3,5,13},{0,1,2,3,8,9,10}}`, catch status formulas, post-hit/recoil/result flow. |
| `modules/script/decoded/data__script__db.mid.json` | Current raw `aq.c[6]` and `aq.c[7]` rows. |
| `modules/script/decoded/data__script__chs.mid.json` | Current text ids/names. |
| `rebuild_game/src/main/java/VqsvBattleEffectLogic.java` | Current rebuild apply/tick/clear/formula implementation. |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Current target routing, P12/P13 active queue, switch lock, catch multiplier integration. |

This means older documents are still useful, but the current source proof in
new audits should cite `game.i` / `game.a` when those are the files being read.

## Source Table Snapshot

Current `aq.c[6]` rows:

| Id | Name | Raw row | Reaudit status |
| ---: | --- | --- | --- |
| 0 | Suc Luc | `[333,348,2,30,190]` | PORTED |
| 1 | Pha Phu | `[334,349,3,50,50]` | PORTED |
| 2 | Kinh Cuc | `[335,350,3,30,10]` | PORTED |
| 3 | Khoi phuc | `[336,351,3,5,-1]` | PORTED |
| 4 | Phong ngu | `[337,352,2,-1,-1]` | PORTED |
| 5 | Vo hinh | `[338,353,3,30,-1]` | PORTED |
| 6 | Kien nhan | `[339,354,3,50,-1]` | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED |
| 7 | Linh Xao | `[340,355,2,-1,-1]` | PORTED |
| 8 | Dien ap | `[341,356,4,30,-1]` | PORTED |
| 9 | Hoa Thach | `[342,357,3,50,50]` | PORTED |
| 10 | Man Luc | `[343,358,2,-1,-1]` | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED |
| 11 | Thau Thu | `[344,359,3,-1,-1]` | PORTED/PARTIAL |
| 12 | Gia Toc | `[345,360,2,-1,-1]` | PORTED/PARTIAL |
| 13 | Thach Hoa | `[346,361,3,20,-1]` | PORTED |
| 14 | Thach Phu | `[347,362,3,-1,-1]` | PORTED |

Current `aq.c[7]` rows:

| Id | Name | Raw row | Reaudit status |
| ---: | --- | --- | --- |
| 0 | Gieo Hat | `[311,322,3]` | PORTED |
| 1 | Me Muoi | `[312,323,2]` | PORTED |
| 2 | Quan Quanh | `[313,324,3]` | PORTED |
| 3 | Thuc Loai | `[314,325,3]` | PORTED |
| 4 | Muc | `[315,326,3]` | PORTED |
| 5 | Cham Chap | `[316,327,3]` | PORTED |
| 6 | Nhut Chi | `[317,328,3]` | PORTED |
| 7 | Phong Ngu | `[318,329,3]` | PORTED |
| 8 | Quy Mi | `[319,330,4]` | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED |
| 9 | Hon Loan | `[320,331,1]` | PORTED/PARTIAL |
| 10 | Te Liet | `[321,332,4]` | PORTED/PARTIAL |

## Buff Reaudit Matrix

| Id | Source behavior in current `game.i` | Rebuild behavior | Difference |
| ---: | --- | --- | --- |
| 0 | Defense `+30%`; store extra damage `190% * C()`; damage hook when duration counter reaches source edge `0`. | Same in `applySourceBuff` and `applyDamageFormulaHooks`. | None found. |
| 1 | Defense `-50%`; outgoing damage `+50%`. | Same. | None found. |
| 2 | Defense `+30%`; reflect `10%` of final incoming damage through battle post-hit path. | Same through buff2 reflect hook smoke. | None found. |
| 3 | Heal `5% maxHP` on apply and active tick. | Same. | None found. |
| 4 | Uses producer skill param, not row `-1`; known producers use defense `+10%`. | Same; stores source skill in scratch and uses `skill.chanceOrParam`. | None found. |
| 5 | Store reflect chance `30`; later hit can reflect final damage. | Same. | None found. |
| 6 | Source branch is odd: target has buff6, but source bytecode reads attacker slot fields in the damage hook. | User-approved gameplay: target-side `50%` proc, incoming damage reduced by `50%`. | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED. Do not "fix" back to source oddity without user decision. |
| 7 | Uses producer skill param; known producers use speed `+5%`. | Same. | None found. |
| 8 | Store `30`; outgoing damage `+30%`; PP hook costs one extra PP. | Same. | None found. |
| 9 | Speed `+50%`, defense `-50%`. | Same. | None found. |
| 10 | Source row sentinel `-1` would lower attack by 1% style oddity. | User-approved gameplay: 3 turns, attack boost `+15% -> +10% -> +5% -> clear`. | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED. |
| 11 | Copy selected donor's active buffs and clear donor; source also repeats on active tick and has switch/KO cleanup paths. | Cast-time copy/clear, active re-steal, nonzero donor slot, donor switch cleanup are implemented. | PORTED/PARTIAL: multi-enemy/full source slot vector and broader KO/replacement cleanup remain partial. |
| 12 | Follow-up/extra-action state through `L[12]`/battle scheduler. | Local follow-up/PP conservation path implemented and smoke-covered. | PORTED/PARTIAL: full global multi-actor turn-vector parity remains partial. |
| 13 | Clear debuffs, heal `20% maxHP` on apply/tick. | Same. | None found. |
| 14 | Clear debuffs and block incoming debuff apply. | Same in `planTargetDebuff`. | None found. |

## Debuff Reaudit Matrix

| Id | Source behavior in current `game.i` / `game.a` | Rebuild behavior | Difference |
| ---: | --- | --- | --- |
| 0 | Store pre-skill raw damage; tick damage `raw / skill[8]`. | Same. | None found. |
| 1 | Flag status; no HP/stat tick; conditional skills and catch multiplier consume it. | Same. | None found. |
| 2 | Bind/command lock; no HP/stat tick; catch multiplier and defense formula consume it. | Same. Only this debuff blocks pet/item/run. | None found. |
| 3 | Store pre-skill raw damage; final tick damage `raw * skill[8] / 100`. | Same. | None found. |
| 4 | Store skill param; later miss/evasion path uses it as speed penalty. | Same. | None found. |
| 5 | Store `baseSpeed * skill[8] / 100`; lower speed, reassert on active tick, restore on expiry. | Same. | None found. |
| 6 | Store skill param; affected unit outgoing damage decreases by that percent. | Same. | None found. |
| 7 | Store `baseDefense * skill[8] / 100`; lower defense, reassert on active tick, restore on expiry. | Same. | None found. |
| 8 | Table skill 54 is zero-power/special and ordinary producer is not reached from normal P7 damage/debuff path. Source consumer remains special/unclear. | User-approved gameplay: active attacker gets `+10%` outgoing damage and target route rolls `55%` self / `45%` opponent in 1v1. | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED. |
| 9 | Table skill 55 is zero-power/special; active consumer randomizes target vector. User confirmed switch remains allowed. | Random target route implemented; switch remains allowed; body visual/expiry covered. | PORTED/PARTIAL: full multi-active target divergence remains partial. |
| 10 | Explicit chance family and catch multiplier; no proven PC source consumer found for action-delay scheduling. | Producer/icon/P12 body visual/catch multiplier/expiry implemented. | PORTED/PARTIAL: action-delay remains `NOT_FOUND_IN_PC_SOURCE / PENDING_SOURCE_PROOF`. |

## Active Queue / Visual Gate Check

Current source `game.a` has:

```text
aj = {{3, 5, 13}, {0, 1, 2, 3, 8, 9, 10}}
```

Rebuild `VqsvBattleRuntime.activeQueueNeedsVisual()` uses the same gate:

```text
buff body visual ids: 3, 5, 13
debuff body visual ids: 0, 1, 2, 3, 8, 9, 10
```

No mismatch found here after the S60 merge.

## Important Intentional Deviations To Preserve

These are not source parity claims. They are user-approved gameplay fixes:

| Effect | Current rule |
| --- | --- |
| buff6 Kien nhan | When holder is hit: `50%` chance to reduce incoming damage by `50%`. |
| buff10 Man Luc | Attack boost over 3 turns: `+15%`, then `+10%`, then `+5%`, then clears. |
| debuff8 Quy Mi | Affected pet gets `+10%` outgoing damage, but target route is `55%` self-hit / `45%` opponent-hit in 1v1. |

Do not reopen these just because they differ from source. Reopen only if the
user asks to restore exact source oddity.

## Remaining Partials / Pending

| Area | Status | Why it remains |
| --- | --- | --- |
| buff11 Thau Thu | PORTED/PARTIAL | Multi-enemy/full source donor slot vector and broader KO/replacement cleanup are not fully proven. |
| buff12 Gia Toc | PORTED/PARTIAL | Local follow-up and PP conservation are covered; full global multi-actor turn-vector parity remains partial. |
| debuff9 Hon Loan | PORTED/PARTIAL | 1v1 random target route is covered; full multi-active target divergence remains partial. Switch is intentionally allowed. |
| debuff10 Te Liet | PORTED/PARTIAL | Action-delay scheduling has no proven PC source callsite. Catch multiplier and visuals are covered. |
| Pixel-perfect body visuals | PENDING | Smoke PNG verifies presence/route, not original-vs-rebuild pixel comparison. |

## Verification

Commands run from `rebuild_game/`:

```text
build.ps1
java -cp build/classes com.vqsv.rebuild.Main --check
java -cp build/classes VqsvBattleDamageFormulaCheck
java -cp build/classes VqsvIntroDemo --smoke-suite battle_quick build/smoke/buff_debuff_reaudit_battle_quick
```

Results:

| Check | Result |
| --- | --- |
| Build | PASS |
| Release check | PASS |
| Damage formula check | PASS |
| `battle_quick` smoke | PASS, `227/227` |

Smoke output directory:

```text
rebuild_game/build/smoke/buff_debuff_reaudit_battle_quick/
```

Representative checkpoints inside the suite:

- `battle_status_buff6_damage_reduction_success.png`
- `battle_status_buff10_gameplay_decay_attack_up.png`
- `battle_status_debuff8_gameplay_fixed_self_hit_damage_up.png`
- `battle_status_debuff8_gameplay_fixed_enemy_hit_damage_up.png`
- `battle_status_debuff9_random_target_seeded_active.png`
- `battle_status_debuff10_catch_multiplier.png`

## Decision

No runtime patch is required for this reaudit.

Temporary buff/debuff table work remains closed as:

```text
PORTED / PORTED-PARTIAL / INTENTIONAL_DEVIATION where explicitly noted
```

## Next Step

Return to the skill/effect roadmap:

1. Continue grouped skill animation/effect coverage from `301_battle_skill_grouped_logic_animation_roadmap.md` and `303_battle_all_skill_source_logic_animation_audit.md`.
2. Pick the next concrete skill family slice with source-proven producer row, P7 visual path, numbers, and PNG before/during/after.
3. Do not reopen buff/debuff tables unless a new source-route mismatch, failed regression, or original-vs-rebuild capture proves a real issue.
