# 76 Battle P12/P13 Active Queue Lifecycle Matrix

Status: SOURCE AUDIT plus CODE SLICES THROUGH P15 / TYPE0 ACTOR ACTION.

Scope: source audit for `game.d` states `12` and `13`, and the active
buff/debuff queue helpers in `game.b`. This is the next source layer after
`75_battle_game_d_q_post_skill_matrix.md`.

## Source Anchors

| Source | What it proves |
| --- | --- |
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java:220..223` | `/data/script/bufDebuf.mid` is loaded into three byte matrices: `ap`, `aq`, `ar`. |
| `game/d.java:469..491` | Helper `a(b)` skips or immediately applies queue entries whose visual row is blocked by `ai`, otherwise starts visual sequence. |
| `game/d.java:494..517` | Helper `o()` starts the next visual segment from the current `af[ad]` row. |
| `game/d.java:591..608` | Helper `b(b)` detects queue completion and dispatches by current state: P12 uses `a(b,true)`, P13 uses `b(b,true)`. |
| `game/d.java:610..638` | Helper `c(b)` advances to the next queue entry, possibly clears queue arrays if the unit died and dispatches through P12/P13 helper. |
| `game/d.java:769..807` | P12/P13 enter/init: build active queue arrays from `b.x[0]` buffs and `b.x[1]` debuffs, then start first sequence. |
| `game/d.java:1198..1299` | P12/P13 update: tick actor action `u`, special effect `H`, apply queue effect once, float HP text, update HP bar, then advance. |
| `game/d.java:1841..1849` | P12/P13 draw: battle scene, optional `H`, actors, floating texts. |
| `game/d.java:494..517` + `game/b.java:119..129` | `bufDebuf` segment kind `0` calls `b.a(short,byte)`, which creates `game.b.u = new ah()` from `{effectId,state,oldDirection}`. |
| `game/d.java:708..716` | P15 entry replaces the active enemy: resets controller state, marks new enemy active, puts `d[g]` into turn vector, hides replacement marker, increments turn cursor. |
| `game/d.java:520..543` | If enemy-side unit dies and source still has reserve enemy slots, it sets `g=e[i]` and enters P15 instead of P8. |
| `game/b.java:482..582` | `a(byte,int,int)`: apply self-side buff into `v[][]`, insert into active queue bank `x[0]`. |
| `game/b.java:585..665` | `o(int)`: per-turn buff effect body. |
| `game/b.java:672..710` | `C()/D()/C(int)/e(int,int)`: clear all/one debuffs or buffs and remove queue entries. |
| `game/b.java:712..779` | `q(int)`, `c(int,int)`, `d(int,int)`: per-turn debuff effect and duration decrement/expiry. |
| `game/b.java:1368..1425` | Debuff application writes `w[][]`, inserts into active queue bank `x[1]`, stores duration/source skill. |
| `modules/script/decoded/data__script__bufDebuf.mid.json` | Decoded P12/P13 visual sequences and effect-id-to-row maps. |

## Source Data Model

`game.b` has two active effect banks:

| Bank | Source fields | Meaning | Inserted by | Processed by P12/P13 |
| --- | --- | --- | --- | --- |
| `0` | `x[0][0..2]`, `N[0]`, `v[16][5]` | Self-side buffs from `aq.c[6]`. | `game.b.a(byte,int,int)` calls private `a(0,buffId)`. | P12/P13 maps `x[0][slot]` through `ar[0]`, then `ap`. |
| `1` | `x[1][0..2]`, `N[1]`, `w[11][5]` | Target-side debuffs from `aq.c[7]`. | `game.b.b(target)` debuff apply calls private `a(1,debuffId)`. | P12/P13 maps `x[1][slot]` through `ar[1]`, then `aq`. |

Slot layout:

| Table | `[0]` | `[1]` | `[2]` | `[3]` | `[4]` |
| --- | --- | --- | --- | --- | --- |
| `v[buffId]` | remaining duration/counter | primary computed value | secondary computed value | source skill/index | active flag |
| `w[debuffId]` | remaining duration/counter | primary computed value | secondary value | source skill id | active flag |

Queue insertion details from `game.b.a(int,byte)`:

- Each bank holds at most 3 effect ids.
- Duplicate effect id is ignored.
- If all 3 slots are full, source overwrites `x[bank][0]` but does not clearly increment `N` further.
- `e(bank,slot)` removes a slot by setting it to `-1` and decrementing `N[bank]`.

## `bufDebuf.mid` Visual Data

Decoded file shape: three byte matrices:

| Matrix | Source name | Meaning |
| --- | --- | --- |
| `groups[0]` | `ap` | Visual rows for buff bank `0`. |
| `groups[1]` | `aq` | Visual rows for debuff bank `1`. |
| `groups[2]` | `ar` | Maps buff/debuff id to row index in `ap`/`aq`. |

Each visual row is a sequence of 4-byte segments:

| Segment field | Source usage |
| --- | --- |
| `[0] kind` | `1` means create `ah H` from `speffect` row; otherwise call actor action `b.a(state,param)`. |
| `[1] id` | For kind `1`, speffect id; for kind `0`, actor action/state id. |
| `[2] param` | Actor action parameter for kind `0`; copied to `b.a(short,param)`. |
| `[3] trigger` | If not `-1`, P12/P13 can start the next segment when `u.a(trigger)` is true. |

### Buff Visual Map

`ar[0] = [0,1,2,3,1,6,5,1,8,8,1,7,1,4,5]`.

| Buff id | `ar[0][id]` | `ap[row]` raw segments | Visual meaning |
| --- | --- | --- | --- |
| `0` | `0` | `[1,16,0,-1] [1,15,0,-1]` | Two `ah` effects, speffect 16 then 15. |
| `1` | `1` | `[1,16,0,-1] [1,15,0,-1]` | Same as buff 0. |
| `2` | `2` | `[0,21,0,-1]` | Actor action/state 21. |
| `3` | `3` | `[0,33,0,0] [1,7,0,-1]` | Actor action 33 with trigger 0, then speffect 7. |
| `4` | `1` | `[1,16,0,-1] [1,15,0,-1]` | Same as buff 0/1. |
| `5` | `6` | `[0,23,0,-1]` | Actor action/state 23. |
| `6` | `5` | `[1,4,0,-1] [1,17,0,-1]` | Two `ah` effects, speffect 4 then 17. |
| `7` | `1` | `[1,16,0,-1] [1,15,0,-1]` | Same as buff 0/1/4. |
| `8` | `8` | `[1,19,0,-1] [1,15,0,-1]` | Speffect 19 then 15. |
| `9` | `8` | `[1,19,0,-1] [1,15,0,-1]` | Same as buff 8. |
| `10` | `1` | `[1,16,0,-1] [1,15,0,-1]` | Same as buff 0/1/4/7. |
| `11` | `7` | `[1,18,0,-1] [1,15,0,-1]` | Speffect 18 then 15. |
| `12` | `1` | `[1,16,0,-1] [1,15,0,-1]` | Same as buff 0/1/4/7/10. |
| `13` | `4` | `[1,17,0,-1]` | Speffect 17. |
| `14` | `5` | `[1,4,0,-1] [1,17,0,-1]` | Same as buff 6. |
| `15` | no decoded `ar[0]` entry | Unknown in current decoded table. | UNKNOWN/PENDING. |

### Debuff Visual Map

`ar[1] = [0,1,2,3,4,4,4,4,5,6,7]`.

| Debuff id | `ar[1][id]` | `aq[row]` raw segments | Visual meaning |
| --- | --- | --- | --- |
| `0` | `0` | `[1,18,0,-1]` | Speffect 18. |
| `1` | `1` | `[1,14,0,-1]` | Speffect 14. |
| `2` | `2` | `[0,21,0,0] [1,6,0,0]` | Actor action 21 and speffect 6, both trigger-aware. |
| `3` | `3` | `[0,21,0,-1]` | Actor action 21. |
| `4` | `4` | `[1,1,0,0] [1,11,0,-1]` | Speffect 1 then 11. |
| `5` | `4` | `[1,1,0,0] [1,11,0,-1]` | Same as debuff 4. |
| `6` | `4` | `[1,1,0,0] [1,11,0,-1]` | Same as debuff 4/5. |
| `7` | `4` | `[1,1,0,0] [1,11,0,-1]` | Same as debuff 4/5/6. |
| `8` | `5` | `[1,0,0,-1] [0,25,0,-1]` | Speffect 0, then actor action 25. |
| `9` | `6` | `[1,12,0,-1]` | Speffect 12. |
| `10` | `7` | `[1,19,0,-1] [1,6,0,-1]` | Speffect 19 then 6. |

## P12/P13 Entry Matrix

| Step | Source lines | Behavior | Rebuild status |
| --- | --- | --- | --- |
| HP bar refresh for current attacker `h` | `game.d:771..778` | Calls `S.a/b(h,false)` and `S.a/b(h)` by side. This refreshes source HP bar before active queue playback. | MISSING in dedicated P12/P13 state. |
| Select active unit | `game.d:779` | `b2 = (b)v.elementAt(i)`. P12/P13 operate on current turn-order unit, not necessarily `h`. | PARTIAL in rebuild dispatch. |
| Buff 13/14 pre-clear | `game.d:780..782` | If `b2.m(13) || b2.m(14)`, call `b2.C()` to clear all debuffs before building the queue. | MISSING/PENDING for P12/P13 timing. |
| Allocate queue playback arrays | `game.d:783..785` | `af` length = `b2.r(0)+b2.r(1)`, `ag` has bank/id pairs, `ah` stores queue slot index. | MISSING/PENDING as state arrays. |
| Add buff entries | `game.d:787..794` | For each queue slot `x[0][n] != -1`: `af=row ap[ar[0][buffId]]`, `ag=(0,buffId)`, `ah=slot`. | MISSING/PENDING. |
| Add debuff entries | `game.d:795..801` | For each queue slot `x[1][n] != -1`: `af=row aq[ar[1][debuffId]]`, `ag=(1,debuffId)`, `ah=slot`. Buff entries always play before debuffs. | MISSING/PENDING. |
| Initialize cursors | `game.d:803..804` | `ab=0`, `ac=0`; `ad` is current entry index. | MISSING/PENDING. |
| Skip/apply blocked entries or start first visual | `game.d:805..806` | `a(b2)` can immediately apply entries if their effect id is listed in `ai[bank]`; otherwise `o()` starts visual. | UNKNOWN/PENDING: `ai` table semantics need a separate small audit before coding. |

## P12/P13 Update Matrix

| Runtime state | Source lines | Behavior | Side effect |
| --- | --- | --- | --- |
| Actor action `u` running | `game.d:1201..1229` | If `b2.u` exists, tick it with `u.a()`. If animation object completes and the segment row has more chunks, call `o()` to start next segment. If trigger `[3] != -1` and `u.a(trigger)` is true, start next segment early. | Drives actor action rows from `bufDebuf.mid`. |
| Special effect `H` running | `game.d:1232..1243` | If `H` exists and not started/done, tick `H.a()` and hide actor via `b2.b(false)`. When `H` completes, null it, restore actor with `b2.b(true)`, and either mark `A=true` or start next segment. | Drives `ah` speffect rows from `bufDebuf.mid`. |
| Queue entry ready to apply | `game.d:1245..1272` | When visual sequence is done (`A` true) and `aE` is false, apply the effect once: bank 0 calls `b2.o(id)` then `b2.d(id,slot)`; bank 1 calls `b2.q(id)` then `b2.c(id,slot)`. | Applies per-turn HP/stat effect, decrements duration, removes expired queue slot. |
| Floating HP text | `game.d:1259..1264` | Compare pre-apply HP `var5_20 = b2.N()` to current HP. If HP decreased, float negative delta. If returned heal `var6_21 > 0`, float `+heal`. | Text source is HP delta, not buff/debuff name text. |
| HP bar update | `game.d:1265..1271` | Reset `S.k`, then call side-specific `S.a/b(b2)`. | Updates HP bar after queue effect. |
| Wait for floating text/bar completion | `game.d:1273..1296` | Calls `V()` for floating text completion and `S.a/b(..., true/false)` for HP bar completion. When both complete, clears `aE`, calls `c(b2)`. | Advances to next queued effect or exits P12/P13. |
| Draw | `game.d:1841..1849` | Draw battle scene, optional `H`, actors and floating text. | UI is visual-only; input is not involved. |

## Rebuild Implementation Update

| Area | Source-backed behavior | Rebuild status | Evidence |
| --- | --- | --- | --- |
| Active queue bank scan | Buff bank `x[0]` slots then debuff bank `x[1]` slots. | PORTED for current runtime. | Smoke covers buff9, debuff0, debuff3, debuff5 and route regressions. |
| Visual gate `ai` | Source skips visuals for effect ids listed in `ai[bank]`. | PORTED/PARTIAL. Current rebuild gates to the source-observed visible ids: buffs `{3,5,13}`, debuffs `{0,1,2,3,8,9,10}`. | `battle_p12_debuff5_stat_skip_visual.png`. |
| Segment kind `1` | Creates `ah H` from `speffect.mid`; actor hidden while `H` plays. | PORTED/PARTIAL for existing speffect type 1/9 paths; not a full `ah` runtime. | Existing skill45/P12 smokes. |
| Segment kind `0` | Calls `game.b.a(short,byte)` and creates actor-action `game.b.u`. | PORTED/PARTIAL. Rebuild now creates `P7ActorAnimation` from the same source AH effect id and state, ticks cursor, supports trigger check. It no longer fakes this as base actor state. | `battle_p12_debuff3_type0_actor_start.png`, `battle_p12_debuff3_type0_actor_mid.png`, `battle_p12_debuff3_type0_actor_after.png`. |
| P15 enemy replacement | Dead enemy with reserve enemy enters case 15 and swaps active enemy instead of P8. | PORTED/PARTIAL. Rebuild now has an enemy party array, active enemy index, pending replacement, and P15 state. Source arrays `d/v/e/t/s/u` are still simplified. | `battle_p12_queue_death_to_p15_start.png`, `battle_p15_enemy_replaced.png`, plus `battle_p12_queue_death_to_p8_regression.png` for no-reserve path. |

Honest limitation: type0 still uses the rebuild `P7ActorAnimation` renderer for
the AH default actor-action path. That is source-shaped by effect id/state/cursor,
but it is not yet a complete generic `ah` renderer for every transform/drawRGB
variant.

## Buff Tick Matrix: `game.b.o(int)` + `d(int,int)`

P12/P13 processes bank `0` by calling:

```text
heal = b2.o(buffId)
b2.d(buffId, queueSlot)
```

| Buff id | `o(buffId)` per-turn/stat effect | `d(buffId,slot)` duration behavior | HP/stat delta | Text |
| --- | --- | --- | --- | --- |
| `0` | No per-turn body. | Decrement `v[0][0]`; when `<=0`, `n(0)` clears active flag and restores stats 2..4, then `e(0,slot)`. | No HP delta in `o()`. Defense reset on expiry through `n()`. | None unless HP changed elsewhere. |
| `1` | Sets defense to `baseDefense - v[1][1]`. | Same duration/expiry clear. | Defense lowered while active. | None. |
| `2` | Sets defense to `baseDefense + v[2][1]`. | Same. | Defense raised while active. | None. |
| `3` | Heals by `v[3][1]`; returns heal amount. | Same. | HP increases by stored value each tick. | `+heal` if > 0. |
| `4` | Adds `v[4][1]` to current defense (`d[3] + v[4][1]`). | Same. | Defense boost is cumulative relative to current defense in `o()`, not base. | None. |
| `5` | No per-turn body. | Same. | No direct HP/stat in `o()`. Used elsewhere for reflect/check path. | None. |
| `6` | No per-turn body. | Same. | No direct HP/stat in `o()`. Damage formula uses slot values. | None. |
| `7` | Sets speed to `baseSpeed + v[7][1]`. | Same. | Speed raised while active. | None. |
| `8` | No per-turn body. | Same. | No direct HP/stat in `o()`. PP/damage behavior occurs elsewhere. | None. |
| `9` | Sets speed to `baseSpeed + v[9][1]` and defense to `baseDefense - v[9][2]`. | Same. | Speed up, defense down. | None. |
| `10` | Sets attack to `baseAttack + v[10][1]`. | Same. | Attack raised. | None. |
| `11` | Copies active buffs from another unit `d[v[11][1]]`, then clears donor buffs through `b2.D()`. | Same. | Re-applies copied buff effects; exact multi-unit timing is source-specific. | Any heal from copied buffs can surface later, not directly returned here. |
| `12` | Sets `K[12]=2`. | Same. | Turn/PP conservation state, not HP/stat. | None. |
| `13` | Heals by `v[13][1]`; returns heal amount. | Same. | HP increases by stored value each tick. | `+heal` if > 0. |
| `14` | No `o()` case; only apply-time clear debuffs from `a(byte,...)`. | Same if active. | No per-turn HP/stat. | None. |
| `15` | No `o()` case in visible source. | Same if active. | UNKNOWN. | UNKNOWN. |

Important clear behavior:

- `n(buffId)` only marks `v[buffId][4]=0` and restores mutable stats `d[2..4]` to base `c[2..4]`.
- This means expiry of one buff resets all mutable attack/defense/speed to base, then surviving buffs are expected to reassert through their own future `o()` ticks.
- `D()` clears every active buff via `n(id)` and removes every buff queue slot.

## Debuff Tick Matrix: `game.b.q(int)` + `c(int,int)`

P12/P13 processes bank `1` by calling:

```text
b2.q(debuffId)
b2.c(debuffId, queueSlot)
```

| Debuff id | `q(debuffId)` per-turn/stat effect | `c(debuffId,slot)` duration behavior | HP/stat delta | Text |
| --- | --- | --- | --- | --- |
| `0` | Damage by `w[0][1] / aq.c[1][w[0][3]][8]`; if unit dies, set actor state `3`. | Decrement `w[0][0]`; when `<=0`, clear and restore stats, remove queue slot. | HP decreases by computed damage. | Negative HP delta. |
| `1` | No per-turn body. | Same. | No direct HP/stat. Presence affects skills 23/29. | None. |
| `2` | No per-turn body. | Same. | No direct HP/stat here. Used by source damage/defense helper paths. | None. |
| `3` | If `w[3][0] <= 1`, damage by `w[3][1] * aq.c[1][w[3][3]][8] / 100`; if unit dies, set actor state `3`. | Same. | Delayed/late-duration HP damage. | Negative HP delta. |
| `4` | No per-turn body. | Same. | No direct HP/stat in visible `q()`. | None. |
| `5` | Sets speed to `baseSpeed - w[5][1]`. | Same. | Speed lowered. | None. |
| `6` | No per-turn body. | Same. | No direct HP/stat here. Damage formula uses `w[6][1]`. | None. |
| `7` | Sets defense to `baseDefense - w[7][1]`. | Same. | Defense lowered. | None. |
| `8` | No visible `q()` case. | Same if active. | UNKNOWN/PENDING. | UNKNOWN. |
| `9` | No visible `q()` case. | Same if active. | UNKNOWN/PENDING. | UNKNOWN. |
| `10` | No visible `q()` case. | Same if active. | UNKNOWN/PENDING. | UNKNOWN. |

Important clear behavior:

- `C(debuffId)` marks `w[debuffId][4]=0` and restores mutable stats `d[2..4]` to base `c[2..4]`.
- `C()` clears all active debuffs and removes every debuff queue slot.
- Buff ids `13` and `14` call `C()` at apply time; P12/P13 also calls `C()` before queue build if `b2.m(13) || b2.m(14)`.

## Text Matrix

| Text source | Source lines | Content | Status |
| --- | --- | --- | --- |
| HP decrease | `game.d:1259..1261` | `"" + (currentHp - previousHp)`; negative number. | SOURCE-BACKED. |
| HP heal | `game.d:1262..1264` | `"+" + healReturnedByBuffO`. | SOURCE-BACKED. |
| Buff/debuff name | Not in P12/P13 tick body. Names are shown during P7 `game.d.q()` or debuff landing, not active queue tick. | P12/P13 active tick does not show `aq.c[6/7]` names directly in audited source. | SOURCE-BACKED. |
| No HP change | P12/P13 still plays visual sequence and HP bar update; no floating HP text is spawned by this branch. | None. | SOURCE-BACKED. |

## Next State Matrix

P12 and P13 have different exit helpers:

| Current state | Completion path | Source helper | Next state possibilities |
| --- | --- | --- | --- |
| P12 | All queue entries complete through `b(b)` | `a(b2,true)` | If the processed unit died and all opposing units are defeated: P8 win. If it died and player-side replacement exists: P15 forced replacement. If alive and `true` flag: P2 select/execute. Otherwise increments turn and calls `p()` to continue dispatch. |
| P12 | One entry completes through `c(b)` | `a(b2,false)` after optional `g(b2)==2` queue clear | If death condition says replacement/win path, arrays may be cleared first; otherwise continues dispatch without forcing P2. |
| P13 | All queue entries complete through `b(b)` | `b(b2,true)` | If all player-side units are dead: P9 lose. If current player unit died and replacement is possible: P5 pet switch. If alive and `true` flag: P20 command, unless source status `p(9)` sends P2. Otherwise advances turn through `p()`. |
| P13 | One entry completes through `c(b)` | `b(b2,false)` after optional `g(b2)==1` queue clear | If death condition says pet-switch/lose path, arrays may be cleared first; otherwise continues dispatch. |

Honest note: helper names `a(b,boolean)` and `b(b,boolean)` are source facts, but
CFR damage plus obfuscated side fields make exact human labels such as
"enemy-side" and "player-side" risky without a live state trace. The transition
conditions above are grounded in visible source branches rather than renamed
semantics.

## Current Rebuild Gap

| Area | Current rebuild | Gap |
| --- | --- | --- |
| Unit storage | `BattleUnit.buffSlots`, `debuffSlots`, `activeEffectQueue`, `activeEffectCount` exist. | Structures exist. Runtime now scans all 3 queue slots for bank `0` then bank `1`, matching source loop order. |
| Apply buff | `BattleUnit.applySourceBuff(...)` covers buff ids 0..15 source-shaped. | Runtime tick now has `o(id)` equivalents for the source-visible buff cases; complex buff `11` copy semantics remain PARTIAL because rebuild lacks full battle party index parity. |
| Apply debuff | `BattleUnit.computeDamage(...)` can write debuff slots for several ids. | Runtime tick now has `q(id)` equivalents for source-visible debuffs `0`, `3`, `5`, `7`; no-op ids remain no-op. Debuff ids `8..10` still need broader behavior validation. |
| Visual sequence | P7 has AH/speffect renderer slices. | `bufDebuf.mid` is loaded from original binary and mapped via `ar[bank][id] -> ap/aq`. Source visual gate `ai` is honored: buff visual ids `{3,5,13}`, debuff visual ids `{0,1,2,3,8,9,10}`. |
| HP/stat per-turn tick | Some effects influence damage formula immediately. | Generic active queue calls bank `0`: `tickSourceBuff(id)+d(id,slot)` and bank `1`: `tickSourceDebuff(id)+c(id,slot)`. HP delta text is emitted only after visual entries, matching source update path. |
| Next-state dispatch | P1/P20/P7/P8/P9 route smoke passes. | P12 enemy death routes P8; P13 player death routes P5 if reserve pet exists, otherwise P9; living P12 routes P2 and living P13 routes P20/P2 depending debuff9. Full P15 enemy replacement remains PENDING because current rebuild battle runtime has one enemy, not source enemy party arrays. |

## Implemented Slice: Buff Id 9 From Skill 45

Implemented in:

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | Added `P12_ACTIVE_QUEUE` and `P13_ACTIVE_QUEUE`; dispatch can enter P13/P12 for active buff id `9`; plays speffect `19` then `15`; applies buff tick and returns to P1. |
| `rebuild_game/src/main/java/VqsvBattleUnit.java` | Added source-shaped `activeBuffSlot(9)`, `tickSourceBuff9(slot)`, duration decrement and queue removal on expiry. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added smoke checkpoints for queue start, segment 15, and after-apply. |

Source mapping:

| Source behavior | Rebuild slice |
| --- | --- |
| P12/P13 builds bank `0` queue from `x[0]`. | Rebuild detects active buff id `9` in bank `0`. |
| `ar[0][9] = 8`, `ap[8] = [1,19,0,-1] [1,15,0,-1]`. | Rebuild plays speffect `19`, then speffect `15`. |
| `game.b.o(9)` sets speed to `baseSpeed + v[9][1]` and defense to `baseDefense - v[9][2]`. | `BattleUnit.tickSourceBuff9()` applies the same stat assignment. |
| `game.b.d(9,slot)` decrements duration and clears/removes queue when expired. | Rebuild decrements `buffSlots[9][0]`, clears buff and removes queue slot when duration reaches zero. |

Smoke PNGs:

| Checkpoint | PNG |
| --- | --- |
| `battle_p13_buff9_queue_start` | `rebuild_game/build_intro_demo/battle_p13_buff9_queue_start.png` |
| `battle_p13_buff9_visual_speffect15` | `rebuild_game/build_intro_demo/battle_p13_buff9_visual_speffect15.png` |
| `battle_p13_buff9_after_apply` | `rebuild_game/build_intro_demo/battle_p13_buff9_after_apply.png` |

## Implemented Slice: Debuff Id 0 HP-Delta Tick

Implemented in:

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | P12/P13 active queue now detects bank `1` debuff id `0`, plays `aq[0]` visual speffect `18`, applies the debuff tick once, emits negative HP-delta text, then returns through P1. |
| `rebuild_game/src/main/java/VqsvBattleUnit.java` | Added source-shaped `activeDebuffSlot(0)`, `tickSourceDebuff0(slot)`, duration decrement and queue removal on expiry. |
| `rebuild_game/src/main/java/VqsvIntroDemo.java` | Added active-queue bank/effect metadata fields so smoke can distinguish buff and debuff queue entries. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added smoke checkpoints for debuff0 queue start, HP-delta text, and after-apply. |

Source mapping:

| Source behavior | Rebuild slice |
| --- | --- |
| P12/P13 builds bank `1` queue from `x[1]`. | Rebuild detects active debuff id `0` in bank `1`. |
| `ar[1][0] = 0`, `aq[0] = [1,18,0,-1]`. | Rebuild plays speffect `18` as the single visual segment. |
| `game.b.q(0)` computes damage as `w[0][1] / aq.c[1][w[0][3]][8]`, then applies HP damage and dead state if needed. | `BattleUnit.tickSourceDebuff0()` reads the stored pre-skill raw value and source skill id, divides by that skill row parameter, then damages the unit. |
| `game.b.c(0,slot)` decrements duration and clears/removes queue when expired. | Rebuild decrements `debuffSlots[0][0]`, clears debuff and removes queue slot when duration reaches zero. |
| `game.d` P12/P13 compares HP before/after and floats negative text if HP decreased. | Rebuild stores `battleP7PostEffectText` as a negative HP delta during the active queue apply step. |

Smoke PNGs:

| Checkpoint | PNG |
| --- | --- |
| `battle_p12_debuff0_queue_start` | `rebuild_game/build_intro_demo/battle_p12_debuff0_queue_start.png` |
| `battle_p12_debuff0_damage_text` | `rebuild_game/build_intro_demo/battle_p12_debuff0_damage_text.png` |
| `battle_p12_debuff0_after_apply` | `rebuild_game/build_intro_demo/battle_p12_debuff0_after_apply.png` |

## Generalized Active Queue Update

Implemented after the first buff9/debuff0 slices:

| Source behavior | Rebuild behavior | Status |
| --- | --- | --- |
| P12/P13 builds an ordered queue by scanning buff slots `x[0][0..2]`, then debuff slots `x[1][0..2]`. | Runtime builds `activeQueueOrderBank/id/slot` in the same order. | PORTED |
| Before queue build, if buff `13` or `14` is active, source clears all debuffs with `C()`. | Runtime calls `clearDebuffs()` before building the queue if buff `13` or `14` is active. | PORTED |
| Source helper `a(b2)` skips visual for effects not listed in `ai = {{3,5,13},{0,1,2,3,8,9,10}}`, applying them immediately. | Runtime applies non-visual queue entries immediately and does not show HP text for those immediate ticks. This corrected the earlier buff9 visual approximation. | PORTED |
| Source visual rows come from `bufDebuf.mid`: `ar[bank][id] -> ap/aq row`. | `VqsvBattleAnimationTables` now loads all three byte matrices from original `bufDebuf.mid`. | PORTED |
| Visual chunk type `1` creates `ah`/speffect. | Runtime reuses the existing P7 speffect renderer for type `1`. | PORTED/PARTIAL |
| Visual chunk type `0` calls `b2.a(state, direction)`. | Runtime sets the visible base actor state for the active unit. This is source-shaped but not the full original `u` actor-action object. | PARTIAL |
| P12 dead enemy branch. | Enemy death during active queue routes to P8. | PORTED for single-enemy rebuild runtime |
| P13 dead player branch. | Player death during active queue routes to P5 when a reserve pet exists, otherwise P9. | PORTED/PARTIAL; pet HP persistence is still not full source parity |
| P15 enemy replacement branch. | Not fully represented because current rebuild has one enemy unit and no source enemy party/replacement array. | PENDING |

Smoke PNGs:

| Checkpoint | PNG |
| --- | --- |
| `battle_p12_debuff3_queue_start` | `rebuild_game/build_intro_demo/battle_p12_debuff3_queue_start.png` |
| `battle_p12_debuff3_after_apply` | `rebuild_game/build_intro_demo/battle_p12_debuff3_after_apply.png` |
| `battle_p12_debuff5_stat_skip_visual` | `rebuild_game/build_intro_demo/battle_p12_debuff5_stat_skip_visual.png` |
| `battle_p12_queue_death_to_p8` | `rebuild_game/build_intro_demo/battle_p12_queue_death_to_p8.png` |
| `battle_p13_queue_death_to_p5` | `rebuild_game/build_intro_demo/battle_p13_queue_death_to_p5.png` |
| `battle_p13_queue_death_to_p9` | `rebuild_game/build_intro_demo/battle_p13_queue_death_to_p9.png` |

## Implementation Recommendation

Completed ordering:

1. First active queue slice: buff9/debuff0.
2. Corrected source visual gate so buff9 is immediate/no-visual in P12/P13.
3. Generalized queue scan/order and `bufDebuf.mid` loader.
4. Added debuff3 visual queue and stat debuff5 no-visual tick smoke.
5. Added P12/P13 death branch smoke for P8/P5/P9.

## Audit Status

| Topic | Status |
| --- | --- |
| P12/P13 queue source shape | PORTED-READY audit. |
| `bufDebuf.mid` visual row mapping | PORTED-READY audit for ids in `ar[0]` and `ar[1]`. |
| Buff tick behavior `o/d/n/D` | SOURCE-MAPPED. |
| Debuff tick behavior `q/c/C` | SOURCE-MAPPED, with ids 8..10 still PENDING in visible per-turn body. |
| P12/P13 exact side naming | PARTIAL due obfuscation/CFR; transitions are documented by source helper/condition. |
| Rebuild implementation | PORTED/PARTIAL for generic active queue, source visual gate, buff/debuff tick switch, and P8/P5/P9 transitions. P15 enemy replacement and full actor-action type0 parity remain PENDING/PARTIAL. |
