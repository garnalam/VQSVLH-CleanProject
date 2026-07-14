# 280 - Battle Buff Table 0..14 Closeout

Date: 2026-07-13

Scope: closeout checkpoint for self-side temporary buffs from `aq.c[6]`, rows
`0..14`.

This document is intentionally concise. Detailed source proofs remain in
`263_battle_buff_debuff_effect_current_matrix.md` and per-buff closeout docs
`266..279`.

## Source Anchors

| Source | What it proves |
| --- | --- |
| `modules/script/decoded/data__script__db.mid.json` | Raw buff rows in group `6` and producer skills in group `1`. |
| `modules/script/decoded/data__script__chs.mid.json` | Source names/descriptions. |
| `modules/source_code/decoded/decompiled_source_cfr/game/b.java` | `game.b.a(byte,int,int)` apply, `o(int)` tick, clear helpers, damage formula hooks. |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java` | P7 producer flow and P12/P13 active queue visual gate. |
| `modules/script/original/effect.mid` | Producer animation chunks. |
| `modules/script/original/bufDebuf.mid` | P12/P13 active queue body visual rows. |
| `rebuild_game/src/main/java/VqsvBattleUnit.java` | Rebuild buff apply/tick/formula hooks. |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Rebuild P7/P12/P13 runtime integration. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Focused PNG smoke checkpoints. |

## Runtime Rules Now Locked

| Rule | Status |
| --- | --- |
| Buff state lives in source-shaped slots `v[id][0..4]` / rebuild `buffSlots[id][0..4]`. | PORTED |
| HUD icon cell for buff `id` is `id + 12`. | PORTED |
| HUD duration cell is `134 + remainingDuration`. | PORTED |
| P12/P13 body visual gate for buffs is exactly ids `3`, `5`, `13`. | PORTED |
| Buffs `13` and `14` clear debuffs through source `C()` behavior. | PORTED |
| Buff id `15` is not a decoded `aq.c[6]` gameplay row; it remains source residue / UNKNOWN. | PENDING/UNKNOWN |

## Buff Closeout Matrix

| Id | Name | Raw row | Producer skill(s) | Runtime effect | Visual/body rule | Closeout status |
| ---: | --- | --- | --- | --- | --- | --- |
| 0 | Suc Luc | `[333,348,2,30,190]` | `4` | Defense `+30%`; stores extra damage from `190% * B()` for source duration-edge hook. | Producer `effect.mid[4]`; icon `12`; no body visual. | PORTED |
| 1 | Pha Phu | `[334,349,3,50,50]` | `5` | Defense `-50%`; outgoing damage `+50%`. | Producer `effect.mid[5]`; icon `13`; no body visual. | PORTED |
| 2 | Kinh Cuc | `[335,350,3,30,10]` | `14` | Defense `+30%`; reflects `10%` of final incoming hit damage. | Producer actor action `effect.mid[14]`; icon `14`; no body visual. | PORTED |
| 3 | Khoi phuc | `[336,351,3,5,-1]` | `15` | Heal `5% maxHP` on apply and active tick. | Producer visual; icon `15`; P12/P13 body visual required. | PORTED |
| 4 | Phong ngu | `[337,352,2,-1,-1]` | `21`, `27` | Uses producer `skill[8]`; known producers give defense `+10%`. | Producer differs by skill; icon `16`; no body visual. | PORTED |
| 5 | Vo hinh | `[338,353,3,30,-1]` | `34` | `30%` chance to reflect/store incoming damage back to attacker. | Producer speffect `4`; icon `17`; P12/P13 body visual required. | PORTED |
| 6 | Kien nhan | `[339,354,3,50,-1]` | `35` | User-approved gameplay fix: `50%` proc, incoming damage reduced by `50%`. | Producer speffect `4 -> 17`; icon `18`; no body visual. | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED |
| 7 | Linh Xao | `[340,355,2,-1,-1]` | `42`, `48` | Uses producer `skill[8]=5`; speed `+5%`. | Producer differs by skill; icon `19`; no body visual. | PORTED |
| 8 | Dien ap | `[341,356,4,30,-1]` | `44` | Outgoing damage `+30%`; skill PP cost consumes one extra PP. | Producer speffect `19 -> 15`; icon `20`; no body visual. | PORTED |
| 9 | Hoa Thach | `[342,357,3,50,50]` | `45` | Speed `+50%`; defense `-50%`. | Producer speffect `19 -> 15`; icon `21`; no body visual. | PORTED |
| 10 | Man Luc | `[343,358,2,-1,-1]` | `62`, `68` | User-approved gameplay fix: attack `+15% -> +10% -> +5% -> clear`. Source oddity was `100 -> 99`. | Producer actor `26`, speffect `0 -> 15`; icon `22`; no body visual. | INTENTIONAL_DEVIATION / GAMEPLAY_FIXED |
| 11 | Thau Thu | `[344,359,3,-1,-1]` | `64` | Copies selected donor buffs, clears donor, repeats on active tick. | Icon `23`; no body visual. | PORTED/PARTIAL |
| 12 | Gia Toc | `[345,360,2,-1,-1]` | `65` | Follow-up attack state `K[12]`; next repeated skill conserves PP. | Producer speffect `16 -> 15`; icon `24`; no body visual. | PORTED/PARTIAL |
| 13 | Thach Hoa | `[346,361,3,20,-1]` | `24` | Clears debuffs, heals `20% maxHP` on apply and tick. | Producer actor/speffect; icon `25`; P12/P13 body visual required. | PORTED |
| 14 | Thach Phu | `[347,362,3,-1,-1]` | `25` | Clears debuffs and blocks incoming debuff families while active. | Producer speffect `4 -> 17`; icon `26`; no body visual. | PORTED |

## Intentional Deviations

| Buff | Reason |
| --- | --- |
| `6` Kien nhan | Source branch is odd. User approved simpler gameplay: when holder is hit, `50%` chance to halve incoming damage. |
| `10` Man Luc | Source row uses sentinel `-1` literally and lowers attack. User approved a three-turn attack boost decay: `+15%`, `+10%`, `+5%`. |

These are not claimed as exact source parity. They are gameplay-fixed by user
decision and must stay labeled as `INTENTIONAL_DEVIATION / GAMEPLAY_FIXED`.

## Remaining Partials Inside Buff Table

| Buff | Remaining partial |
| --- | --- |
| `11` Thau Thu | Multi-enemy/full source `game.d.d[]` donor slot parity and broader KO/replacement cleanup are not fully proven. |
| `12` Gia Toc | Full global turn-vector/multi-actor parity remains partial, though the local follow-up/PP behavior is smoke-covered. |
| Visual exactness | No buff row is claimed pixel-perfect unless an original-vs-rebuild pixel compare exists. |

## Smoke Coverage Summary

The current `battle_quick` suite includes the dedicated buff checkpoints for
buffs `0..14`, including producer visuals, active logic, icon/duration, and
expiry where applicable.

Latest verified buff14 closeout command set:

```text
build.ps1
com.vqsv.rebuild.Main --check
VqsvBattleDamageFormulaCheck
focused buff14 PNG smoke 7/7
battle_quick suite 134/134
git diff --check
mojibake Java/doc scan
```

## Phase Decision

Buff table `aq.c[6]` rows `0..14` is closed for the current battle-engine
roadmap.

Next lane:

1. Start target-side debuff table `aq.c[7]` in source table order.
2. First target is debuff0 `Gieo Hat`.
3. Even if parts of debuff0 already exist, create a dedicated source audit and
   closeout/checkpoint plan before any new code.
