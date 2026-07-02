# 17. Battle State Machine

Pham vi:

- `source_code/decoded/decompiled_source_cfr/game/d.java`
- `source_code/decoded/decompiled_source_cfr/game/b.java`
- `source_code/decoded/decompiled_source_cfr/game/h.java`
- `source_code/decoded/decompiled_source_cfr/game/i.java`
- `source_code/decoded/decompiled_source_cfr/an.java`

Trang thai: VERIFIED for battle logic/state skeleton; PIXEL/TIMING validation still separate.

Ghi chu audit lai: `game.d.b()` va `game.b.b(game.b)` bi CFR `Unable to fully structure code`, nhung pass nay da doi chieu them `source_code/decoded/bytecode_javap/game__d.javap.txt` va `game__b.javap.txt`. Cac state phu `18/19/24/101/102/104`, skill/effect table semantics, va damage formula khong con de o muc "chua ro logic". Viec con lai la validation runtime/pixel/timing, khong phai thieu doc hieu source.

## 1. Ket Luan Nhanh

`game.d` la battle engine chinh. No khong chi render battle, ma quan ly:

- setup tran dau, active unit slots, enemy wave;
- turn order theo speed;
- UI battle command;
- skill target, AI enemy, damage, buff/debuff, status;
- bat pet, doi pet, dung item, chay tron;
- win/lose, EXP, level up, learn skill, return world.

`game.b` la battle unit/pet model:

- stats, HP/EXP/level;
- skill list va PP;
- buff/debuff arrays;
- damage formula;
- item effect validation/use;
- save payload for pet.

`game.h` la UI controller cho battle:

- `battle.ui`, `choiceskill.ui`, `choice.ui`, `msgwarm.ui`, `levelUp.ui`;
- doc input, chuyen `game.d.P` bang `o.a(byte)`;
- update HP bar, status icon, skill list, catch ball list, item list.

`game.i` la module switcher. Battle duoc load qua `game.i` state `12`, chay trong `13`, va thoat ve world bang `game.i.a((byte)10)`.

## 2. Core Fields In `game.d`

| Field | Meaning | Status |
| --- | --- | --- |
| `P` | State hien tai, ke thua tu `an`. | VERIFIED |
| `Q` | State truoc do, set trong `a(byte)`. | VERIFIED |
| `a` | Battle layout/type: `0` -> 1 player active + enemies; `1` -> 2 player active + enemies. | VERIFIED/PARTIAL |
| `b` | Battle mode/rule: observed `0/1/2`; affects layout and catch/run rules. | PARTIAL |
| `s` | Enemy spawn table, rows like `[monsterId, level, extra]`. | VERIFIED/PARTIAL |
| `d[]` | Active battle slots. Player side has `r()==0`, enemy side has `r()==1`. | VERIFIED |
| `f[]` | Player party index order into `game.g.z`. | VERIFIED |
| `t[]`, `e[]` | Turn order mapping and reverse slot mapping. Built in `T()`. | VERIFIED/PARTIAL |
| `v` | Vector turn order, filled from `d[e[i]]`. | VERIFIED |
| `i` | Current turn index in `v`. | VERIFIED |
| `h` | Current acting battle unit. | VERIFIED |
| `h.p` | Current target unit for acting unit. | VERIFIED |
| `h.D` | Selected skill id. | VERIFIED |
| `x` | Static vector of alive player-side EXP participants. | VERIFIED/PARTIAL |
| `j` | Static vector of pets receiving EXP/level result. | VERIFIED/PARTIAL |
| `u[0]` | Enemy spawn cursor. | VERIFIED/PARTIAL |
| `u[1]` | Enemy defeated count. | VERIFIED/PARTIAL |
| `H` | Current special effect animation (`ah`). | VERIFIED |
| `al[]` | Battle actor/slot marker sprites (`f`). | VERIFIED/PARTIAL |
| `am/an/ao/m/aA/ap/aq/ar` | Battle scripts: positions, action positions, effect, special effect, blood, buff/debuff scripts. | VERIFIED/PARTIAL |

## 3. Battle Resource Load

`game.d.d()` loads battle runtime data:

| Resource | Loaded into | Role | Status |
| --- | --- | --- | --- |
| `/data/script/pos.mid` | `am` | Slot/cursor positions. | VERIFIED |
| `/data/script/cpos.mid` | `an` | Battle entry/current positions per layout variant. | VERIFIED/PARTIAL |
| `/data/script/effect.mid` | `ao` | Skill effect sequence. | VERIFIED/PARTIAL |
| `/data/script/speffect.mid` | `m` | Special effect script for `ah`. | VERIFIED/PARTIAL |
| `/data/script/blood.mid` | `aA` | Floating damage/heal number animation timing. | VERIFIED/PARTIAL |
| `/data/script/bufDebuf.mid` | `ap`, `aq`, `ar` | Buff/debuff animation/effect sequence mapping. | VERIFIED/PARTIAL |
| `/data/tex/blood_0..2` | `az[]` | Damage/heal number images. | VERIFIED/PARTIAL |

Port rule: battle cannot be pixel-close without these scripts. They drive slot motion, hit animation, special effects, and number float timing.

## 4. External Entry / Exit

| Caller | Flow | Evidence | Status |
| --- | --- | --- | --- |
| `game.k` world | Prepares `game.d.a().a(int[][] enemySpawn)` then switches `game.i` into battle load state. | `game.k` calls around battle trigger. | PARTIAL |
| `game.i` state `12` | Creates/loads `game.d`, calls `d.d()`, waits loading/logo, then `((d)m).g()`. | `game.i.b()` state `12`. | VERIFIED |
| `game.d.g()` | Binds UI manager and opens battle HUD via `game.h.a(player/enemy)`. | `game.d.g()` calls `S.a(...)`. | VERIFIED |
| `game.i` state `13` | Runs `game.d.b()` every tick. | `game.i.b()` states `8/11/13/20` delegate `m.b()`. | VERIFIED |
| Battle end | Calls `game.i.a((byte)10)` to return world. | win/catch/run/lose branches. | VERIFIED/PARTIAL |

## 5. High-Level State Graph

```text
load game.i:12
  -> game.d.d()
  -> game.d.g()
  -> P0 battle entry animation
  -> P20 player command
       -> P3 skill list -> P2/P6/P7 skill execution
       -> P21 ball list -> P17 catch animation
       -> P4 item list -> P16 item target/use
       -> P5 switch pet
       -> P11 info/points
       -> P10 run
  -> P1 turn dispatcher
       -> P12 enemy pre-turn buff/debuff tick -> P2/P15/P8/P9
       -> P13 player pre-turn buff/debuff tick -> P20/P5/P9
       -> P2 AI/player skill execute -> P7 hit/effect resolve
  -> P8 win/EXP -> P22 level up -> P23 learn skill -> return world
  -> P9 lose -> P24/return world
```

## 6. State Matrix `game.d.P`

| State | Proposed name | Set/setup in `a(byte)` | Tick behavior in `b()` | Transitions | Status |
| --- | --- | --- | --- | --- | --- |
| `0` | `BATTLE_ENTRY` | Resets `i` to first player-side unit. | Animates all active slots from entry positions; after last slot moves -> `20`. | `0 -> 20` | VERIFIED |
| `15` | `ENEMY_REPLACEMENT_ENTRY` | Inserts/spawns next enemy into slot `g`, sets marker state. | Animates new enemy to position, then returns to turn flow. | `15 -> 1/13/20/2` depending queue and previous state. | VERIFIED |
| `20` | `PLAYER_COMMAND` | `h = current player`; shows command cursor/target; calls `S.c(h)` and target focus. | Updates cursor animation; waits UI input handled by `game.h.d(b)`. | UI can set `3/4/5/11/21/10`; back from submenus returns here. | VERIFIED |
| `1` | `TURN_DISPATCH` | Selects next alive non-`J` unit from speed order `v`; skips dead/used units. | After text/UI idle, branches based on unit side and pre-turn effects. | enemy -> `12` or `2`; player -> `13` or `20`; round end resets order. | VERIFIED |
| `12` | `ENEMY_PRE_TURN_EFFECTS` | Builds buff/debuff effect queue from current enemy unit `x[0]/x[1]`. | Plays DOT/HOT/buff/debuff effects, applies ticks, may kill enemy. | dead enemy -> `15` or `8`; otherwise `2`/next. | VERIFIED |
| `13` | `PLAYER_PRE_TURN_EFFECTS` | Builds buff/debuff effect queue from current player unit. | Plays status ticks; may kill/remove player unit. | dead player -> `5` or `9`; otherwise `20`. | VERIFIED |
| `2` | `SELECT_OR_EXECUTE_SKILL` | No direct setup; uses `h` and selected skill/target. | Enemy AI picks skill/target; player selected skill is executed; calls `h.a(skill,target)`, then `7`. | `2 -> 7` | VERIFIED |
| `7` | `SKILL_ANIMATION_AND_RESOLVE` | Focuses attacker/target, starts skill effect script `ao[h.D]`, computes damage metadata `Z`. | Plays skill animation, special effects, applies damage/heal/buff/debuff, floating numbers, death checks. | next turn `1`; enemy replace `15`; switch `5`; win `8`; lose `9`; target select may route `6`. | VERIFIED |
| `3` | `OPEN_SKILL_LIST` | Calls `S.e(currentUnit)`. | `game.h.f(b)` handles `choiceskill.ui`; confirm sets selected skill and either `6` target select or direct execute. | `3 -> 6/2/20` | VERIFIED |
| `6` | `TARGET_SELECT` | Sets `C=0`, highlights target list `h.G/H`. | Handles nav masks and confirm; `i()` assigns `h.p`, `h.I`, then `h()`. | `6 -> 7/3` | VERIFIED |
| `4` | `OPEN_BATTLE_ITEM_LIST` | Calls `S.aj()`. | `game.h.ak()` handles `choice.ui` item list. | item chosen -> `16`; back -> `20`. | VERIFIED |
| `16` | `BATTLE_ITEM_TARGET` | Calls `S.W()` with item target mode. | `game.h.al()` target/use flow. | success/dead/current target -> `1/4/8`; back -> `4/20`. | VERIFIED |
| `5` | `SWITCH_PET` | Calls `S.W()` for player pet switch; `S.c=0`. | `game.h.X()/battle petstate branch` chooses replacement pet. | confirm replacement -> `15/1`; no pet alive -> `9`. | PARTIAL |
| `21` | `OPEN_CATCH_BALL_LIST` | Calls `S.ah()`. | `game.h.ai()` handles ball choice. | ball confirm -> `17`; back -> `20`; no ball can route `101`. | VERIFIED |
| `17` | `CATCH_ANIMATION` | Sets target enemy `d[0]`, creates ball sprite `aj`, computes catch chance via `b(l)`. | Plays ball animation; on success adds pet to bag/bank; on fail marks turn used and returns to dispatch. | success -> world `game.i:10`; fail -> `1`; no space message -> `1`. | VERIFIED |
| `10` | `RUN_ESCAPE` | No setup. | Moves unit out/prints escape text; when done exits battle. | `10 -> game.i:10` | VERIFIED |
| `11` | `BATTLE_INFO_OR_POINTS` | Calls `S.a(4,0)`. | Delegates a UI/info helper; no damage/turn mutation seen in `game.d`. | Back/close returns through UI flow. | VERIFIED |
| `8` | `WIN_EXP` | Marks map battle result, heals/normalizes EXP recipients, sets UI positions. | `S.am()` distributes EXP to `game.d.j`; can advance level-up. | `8 -> 22` or world `game.i:10`. | VERIFIED |
| `22` | `LEVEL_UP_SCREEN` | Calls `S.an()`. | `S.ao()` shows level-up/stat gains; after timer/action either learn skill or next EXP target. | `22 -> 23/8/game.i:10` | VERIFIED |
| `23` | `LEARN_SKILL_AFTER_LEVEL` | Calls `S.ap()`. | `S.aq()` skill learn choice. | `23 -> 8/game.i:10` | VERIFIED |
| `9` | `LOSE` | If map flag `M.i` true -> `24`, else revives party to 1 HP and exits world. | Mostly no-op after setup. | `9 -> 24` or `game.i:10` | VERIFIED |
| `24` | `DEFEAT_REVIVE_PROMPT` | Calls `S.aE()`: opens `smsInfo.ui` and asks whether to spend `10000` money to restore all party pets. | `S.aF()`: `ACTION` pays 10000, calls `q.z[i].I()`, restores HP, calls `game.d.a().c()`, returns `P=0`; insufficient money opens warning then can route `102`; `SOFT_BACK_B` exits defeat flow through `bv()`. | `24 -> 0/102/world` | VERIFIED |
| `14` | `NOOP_OR_RESERVED` | No setup. | No tick behavior. | Unknown. | UNKNOWN |
| `18` | `DORMANT_MODAL_RETURN` | No setup. | No tick behavior; render path has a passive branch but no input/effect. | Terminal/dormant substate reached from `19`. | VERIFIED |
| `19` | `WAIT_SOFT_BACK_TO_DORMANT` | No setup. | On `SOFT_BACK_B(786432)` -> `18`; otherwise waits. | `19 -> 18` | VERIFIED |
| `101` | `SMS_BUY_MASTER_BALL` | Calls `S.aH()`: configures SMS product id `1`, opens `smsInfo.ui`, title "Mua sam tat trung cau". | `S.aM()` handles SMS confirm/send/cancel state machine from `an.N()`. Triggered from catch UI when ball purchase path is selected. | SMS success applies product through `an.b(true)`, then returns previous `Q`. | VERIFIED |
| `102` | `SMS_BUY_MONEY` | Calls `S.aJ()`: configures SMS product id `2`, opens `smsInfo.ui`, title "Mua sam kim tien". | `S.aM()` handles SMS confirm/send/cancel; used by defeat revive when player lacks money. | SMS success gives money through `an.b(true)`, then returns previous `Q`. | VERIFIED |
| `104` | `SMS_BUY_BADGE` | Calls `S.aI()`: configures SMS product id `4`, opens `smsInfo.ui`, title "Mua sam huy hieu". | `S.aM()` handles SMS confirm/send/cancel. | SMS success grants badges through `an.b(true)`, then returns previous `Q`. | VERIFIED |

## 7. UI Command Flow From `battle.ui`

`battle.ui` command ids:

| Command index `game.h.a` | Label id | Meaning | Input branch | State set | Status |
| --- | --- | --- | --- | --- | --- |
| `0` | `3` | Fight / skill. | `ACTION` in `game.h.d(b)`. | `P=3` | VERIFIED |
| `1` | `4` | Catch. | Checks `d.b != 2`, bag/bank space, then opens ball list. | `P=21` | VERIFIED/PARTIAL |
| `2` | `5` | Item. | Blocked if status `p(2)` ("bi quan"). | `P=4` | VERIFIED |
| `3` | `6` | Pet switch. | Blocked if status `p(2)`; sets `game.d.k=true`. | `P=5` | VERIFIED/PARTIAL |
| `4` | `7` | Points/info. | Direct. | `P=11` | PARTIAL |
| `5` | `8` | Run. | Checks battle mode and `game.c.j`; chance by level difference. | success `game.i:10`, fail marks turn and `P=1`. | VERIFIED/PARTIAL |

Navigation:

- `NAV_LEFT(16400)` and `NAV_RIGHT(32832)` move command highlight.
- `ACTION(196640)` confirms.
- Warnings use `msgwarm.ui`; close returns command state.

## 8. Skill Flow

| Step | Code path | Behavior | Status |
| --- | --- | --- | --- |
| Open skill list | `game.h.e(b)` | Opens `choiceskill.ui`, fills rows `13+5*i` skill name, `14+5*i` PP/max, desc id `53`. | VERIFIED |
| Navigate | `game.h.f(b)` | `NAV_UP/DOWN` changes selected skill. | VERIFIED |
| Confirm | `game.h.f(b)` | If PP available `v1.s(e)`, closes UI and calls `game.d.b(skillId)`. | VERIFIED |
| Target setup | `game.d.b(byte skill)` | Builds target list `h.G/H` from `aq.c[1][skill][9]`: `0` means opponent-side targets, `1` means same-side targets. | VERIFIED |
| Auto/direct target | `game.h.f(b)` | If battle type `a==0`, calls `game.d.i()` directly; else state `6` target select. | VERIFIED |
| Execute | `game.d.i()` then `h()`/`P=7` | Assigns `h.p`, `h.I`, consumes PP via `b.a(skill,target)`, starts execution. | VERIFIED |
| Resolve | `P=7`, `q()` | Skill animation, damage/effect, death/replacement/win/lose routing. | VERIFIED |

## 9. Enemy AI

Observed in state `2`:

- Enemy side `h.r()==1` chooses skill by `game.d.e(h)`.
- `e(b)` starts from first skill then rolls against weights `{50,20,15,10,5,5,5,5,5,5}` over available skills with PP.
- If skill target type allows allies/enemies, target list is built by `b(skill)` or `f(h)`.
- Target selected randomly from `h.G`.
- If current target is dead, code searches another alive target in `h.G`.

Status: VERIFIED. Skill weighting and target side semantics are both traceable from `game.d.b(byte)` and `game.d.b()`.

## 10. Unit Model `game.b`

Important fields:

| Field | Meaning | Status |
| --- | --- | --- |
| `V` | Monster/species id. | VERIFIED |
| `T` | Level. | VERIFIED |
| `c[]` | Base/max stats. `c[1]` HP, `c[2]` attack, `c[3]` defense, `c[4]` speed. | VERIFIED |
| `d[]` | Current battle stats/HP/status. `d[1]` current HP; `d[2..4]` mutable attack/defense/speed. | VERIFIED |
| `z[]` | Skill ids. | VERIFIED |
| `y[]` | Skill PP/current energy. | VERIFIED |
| `O` | Skill count. | VERIFIED |
| `v[16][5]` | Buff/self effect slots: `[0] duration, [1] param1, [2] param2, [3] sourceSkill/effectParam, [4] active`. | VERIFIED |
| `w[11][5]` | Debuff/target effect slots: `[0] duration, [1] param1, [2] param2, [3] sourceSkill, [4] active`. | VERIFIED |
| `x[2][3]` | Active effect id queues: `x[0]` buff/self effect ids, `x[1]` debuff/target effect ids. | VERIFIED |
| `N[2]` | Count of active ids in `x[0]` and `x[1]`. | VERIFIED |
| `D` | Selected skill id for current action. | VERIFIED |
| `p` | Target unit. | VERIFIED |
| `I` | Target slot index. | VERIFIED |
| `B` | Pending EXP gained in battle. | VERIFIED |
| `J` | Used/skip flag during turn order. | VERIFIED |

## 11. Damage And Effect Formula

Status: VERIFIED from bytecode. CFR damaged `game.b.b(game.b)`, but `game__b.javap.txt` preserves exact control flow and constants. Method returns `int[]{damage, critFlag, appliedDebuffId}`.

Damage pipeline:

1. Base attack starts from `this.B()`:
   - attack stat minus target defense;
   - status/passive modifiers from `f(0)`, `f(1)`, target `m(2)`, player passives.
2. Critical:
   - base crit chance `5 + speed/2`;
   - special monster/form condition can raise to `30`;
   - buff `f(4)` adds crit chance;
   - crit damage `* 3 / 2`.
3. Skill power by skill id family:
   - ids `{0,6,10,11,12,13,16,17,18,19,20,26,30,31,32,33,36,37,38,39,40,46,50,51,52,54,55,56,57,58,60,61,63,66,68,69}`: `damage = base * skill[3] / 100`;
   - ids `{1,7}`: `damage = base * skill[3] / 100 + base / skill[8]`;
   - ids `{2,8,22,28,41,47}`: `damage = base * skill[3] / 100`, secondary chance `skill[8]`;
   - ids `{3,9}`: if target has debuff `0`, `damage = base * skill[8] / 100`, else `base * skill[3] / 100`;
   - ids `{23,29}`: if target has debuff `1`, `damage = base * skill[8] / 100`, else `base * skill[3] / 100`;
   - ids `{43,49}`: `damage = base * skill[3] / 100`, then target clears debuffs via `D()`;
   - ids `{53,59}`: `damage = base * (skill[8] - attackerHpPercent) / 100`;
   - all other ids: no direct damage/effect in this method, `appliedDebuffId = -1`.
4. Skill secondary/debuff effect:
   - target debuff id is `skill[7]`;
   - if `skill[7] == -1`, no debuff is applied;
   - if target has immunity/status `f(3)`, chance is reduced by `aq.c[3][3][5]`;
   - if target has buff `m(14)`, debuff is blocked;
   - if `skill[8] != -1`, it is used as proc chance;
   - debuff duration is `aq.c[7][effectId][2]`, halved by player passive `game.g.c(6,1)==1`;
   - debuff id returned in result slot `2`.
5. Status/passive modifiers:
   - attacker buffs `m(0)`, `m(1)`, `m(8)`;
   - target debuffs `p(6)`;
   - global player passive/badge tables `aq.c[2]`, `aq.c[3]`.
6. Element relation:
   - `a(target)==0` -> damage `*3`;
   - `a(target)==1` -> damage `*60/100`;
   - else neutral.
7. Clamp/random:
   - if damage <= 0, set to `1`;
   - then there is a very small jitter branch: `rand=ae.a(100)`, `delta=(damage*2)/100`; if `delta <= 0`, damage may +1 or -1, then clamped again to minimum `1`.

Element relation in `game.b.a(game.b)`:

```text
0 beats 1
1 beats 2
2 beats 3
3 beats 0
5 beats 6
6 beats 4
4 beats 5
```

Species field `aq.c[0][species][22]` is a relation/catch class, verified from bytecode:

- `0/1/3`: normal relation class. Both strong and weak direction checks are enabled.
- attacker class `2`, target class not `2`: only attacker strong-direction check is enabled.
- attacker class not `2`, target class `2`: only target strong-direction check is enabled, so attacker can be resisted but cannot be super-effective through the normal pair table.
- both class `2`: both directions are enabled.

The same `[22]` is also used in catch chance with multiplier `{1000, 500, 1, 1000}` for classes `{0,1,2,3}`. This is now VERIFIED, not an open formula gap.

## 12. Buff / Debuff Runtime

`game.b` has two effect banks:

| Bank | Field | Meaning | Tick state | Status |
| --- | --- | --- | --- | --- |
| self/buff | `v[16][5]` | Positive or self-applied effects. `[0]` remaining duration, `[1]` primary value, `[2]` secondary value, `[3]` source/skill param when used, `[4]` active flag. | `P=12/13` via `b2.x[0]`; tick by `o(effect)` and expire by `d(effect, queueSlot)`. | VERIFIED |
| target/debuff | `w[11][5]` | Negative target effects. `[0]` remaining duration, `[1]` computed param, `[2]` mostly unused/reserved, `[3]` source skill id, `[4]` active flag. | `P=12/13` via `b2.x[1]`; tick by `q(effect)` and expire by `c(effect, queueSlot)`. | VERIFIED |

Important methods:

- `a(byte effect, int param, int skill)` applies self/buff effect.
- `q(int effect)` applies debuff tick.
- `o(int effect)` applies buff tick / heal / stat restore.
- `a(int bank, byte effect)` inserts an active effect id into `x[bank][0..2]`, avoiding duplicates until the queue is full.
- `C()` clears all target/debuff effects and resets mutable battle stats `d[2..4]`.
- `D()` clears all self/buff effects and removes their active queue entries.

Effect ids closed from `game.b`:

| Bank | Ids | Runtime meaning |
| --- | --- | --- |
| `v` buff/self | `0` | Defense up plus delayed extra damage value based on `B()`. |
| `v` buff/self | `1` | Defense down self-modifier, later contributes percent damage boost while active. |
| `v` buff/self | `2` | Defense up; also checked by attacker target branch for reflected/extra damage. |
| `v` buff/self | `3`, `13` | Heal by percent of max HP; `13` also clears debuffs via `C()`. |
| `v` buff/self | `4` | Defense up amount derived from source skill `[8]`; source skill saved in `K[4]`. |
| `v` buff/self | `5` | Revenge/counter storage: target can store incoming damage in attacker `K[5]`. |
| `v` buff/self | `6` | Verified odd bytecode behavior: in `game.b.b(target)`, condition checks `target.m(6)`, but chance/value are read from attacker `v[6][1]/v[6][2]`. Do not "fix" this to target values unless intentionally changing gameplay. |
| `v` buff/self | `7` | Speed up amount derived from source skill `[8]`; source skill saved in `K[7]`. |
| `v` buff/self | `8` | Percent damage boost. |
| `v` buff/self | `9` | Speed up plus defense down. |
| `v` buff/self | `10` | Attack up. |
| `v` buff/self | `11` | Copies another slot's current self buffs, then clears the source slot. |
| `v` buff/self | `12` | Two-step marker via `K[12]` (`1` on apply, `2` on tick). |
| `v` buff/self | `14` | Clears debuffs via `C()` and blocks later target debuff apply while active. |
| `v` buff/self | `15` | Reserved/dead code path in `game.b`: switch has case `15`, but decoded `aq.c[6]` has rows `0..14` only and current skill table does not reference effect `15`. Do not generate this effect from data. |
| `w` debuff/target | `0` | Damage-over-time by stored damage divided by source skill `[8]`. |
| `w` debuff/target | `1`, `2`, `4`, `6`, `8`, `9`, `10` | Persistent status/flag effects; `q(effect)` does no direct HP/stat mutation for these ids, but duration/active semantics are verified. |
| `w` debuff/target | `3` | End-of-duration damage based on stored damage and source skill `[8]`. |
| `w` debuff/target | `5` | Speed down. |
| `w` debuff/target | `7` | Defense down. |

- `d(int effect, int slot)` decrements/removes buff.
- `c(int effect, int slot)` decrements/removes debuff.
- `C()` clears all debuffs.
- `D()` clears all buffs.

`bufDebuf.mid` provides animation sequences:

- `ap` for buff bank;
- `aq` for debuff bank;
- `ar` maps effect id to animation row.

## 13. Catch Flow

| Step | Code path | Behavior | Status |
| --- | --- | --- | --- |
| Choose command | `battle.ui` command `1` | Disallowed if `d.b == 2`; also checks storage space. | VERIFIED/PARTIAL |
| Ball list | `P=21`, `game.h.ah()/ai()` | Opens `choice.ui`, lists player `q.K` ball items, shows catch rate via `game.d.b(itemId)`. | VERIFIED |
| Confirm ball | `game.h.ai()` | Consumes ball from inventory, stores ball id in static `game.d.l`, calls `P=17`. | VERIFIED |
| Chance | `game.d.a(17)` | `ak = ae.a(100) < game.d.b(l)`, except tutorial branch can force fail. | VERIFIED/PARTIAL |
| Success | `P=17` tick | Adds target pet payload `P()` to bag or bank; if full, shows release/no-space message. | VERIFIED/PARTIAL |
| End | `P=17` tick | On success exits to world; on fail sets acting enemy used and dispatches next turn. | VERIFIED/PARTIAL |

Catch rate `game.d.b(int itemId)`:

- returns `100` for item id `0`;
- adjusts by target status (`m(1)`, `m(2)`, `m(10)`, attacker/ball status `f(11)`);
- adjusts by target HP ratio;
- multiplies item catch modifier `aq.c[4][itemId][6]`;
- adjusts by target quality/star and species field `[22]`;
- clamps `1..100`.

## 14. Item Flow In Battle

| Step | Code path | Behavior | Status |
| --- | --- | --- | --- |
| Open item list | `P=4`, `game.h.aj()/ak()` | Opens `choice.ui`, lists bag usable items. | VERIFIED/PARTIAL |
| Reject item types | `game.h.ak()` | Item table `[5]` values `7..10` are rejected in battle. | VERIFIED/PARTIAL |
| Target item | `P=16`, `game.h.al()/bo()` | Selects target pet; validates with `game.b.x(itemId)`. | VERIFIED/PARTIAL |
| Apply item | `game.b.w(itemId)` | Heal HP, restore PP, clear status, revive, set status, decrement inventory. | VERIFIED/PARTIAL |
| Return battle | `game.h.bo()/al()` | If current acting pet used item, may mark `h.J=true` and return dispatch. | PARTIAL |

Item validation return codes in `game.b.x(itemId)` observed:

| Code | Meaning from UI message | Status |
| --- | --- | --- |
| `0` | Pet dead, cannot use. | VERIFIED |
| `1` | Pet not available / does not exist. | VERIFIED |
| `2` | HP full, no need. | VERIFIED |
| `3` | Skill value/PP full, no need. | VERIFIED |
| `4` | Already has beneficial effect. | VERIFIED |
| `5` | Excited/buff state blocks item. | VERIFIED |
| `6` | Item type cannot use. | VERIFIED/PARTIAL |
| `7` | HP and PP both full. | VERIFIED |
| `8` | Dead, cannot use this item. | VERIFIED |
| `-1` | Valid. | VERIFIED |

## 15. Switch Pet Flow

Observed:

- battle command `3` sets `game.d.k = true`, stores `g = e[i]`, then `P=5`;
- `P=5` calls `S.W()` and UI pet selection;
- chosen pet can call `game.d.a(int partyIndex)` to move selected party pet to front/order;
- `game.d.a(int)`:
  - returns `0` if target pet dead;
  - returns `1` if pet has forbidden state `K()`;
  - returns `-1` on success and reorders `f[]`, `x`, flags current `h`.

Status: PARTIAL. The UI branch is split across `game.h` petstate logic, so exact all warning paths need another pass when porting pet switch screen.

## 16. Win / Lose / EXP / Level Up

Win condition:

- enemy defeated increments `u[1]`;
- if `u[1] >= s.length`, calls `X()` then `P=8`;
- `X()` finalizes EXP recipients in static `j`, heals passive if player has specific global passive.

Lose condition:

- if no alive player-side pet remains, `P=9`;
- `P=9` either routes to `P=24` if world flag `game.k.a().M.i` is true, or sets all pets to 1 HP and returns world state `game.i:10`.

EXP:

- defeated enemy EXP is computed in `h(b defeatedEnemy)`;
- EXP formula uses enemy level, quality/star, number of participants, participant level difference, passive boosts;
- EXP is accumulated into `B` on pets in `j`;
- `P=8` uses `game.h.am()` to animate EXP bar and advance each pet;
- level up goes `P=22`, learn-skill choice goes `P=23`.

Status: VERIFIED. Regression tests are still useful for port correctness, but not because the formula is unknown.

## 17. Data Tables Used By Battle

`db.mid` is loaded into `aq.c[0..8]`. Row counts and widths from decoded JSON:

```text
group 0 rows=100 width=23
group 1 rows=70  width=10
group 2 rows=8   width=7
group 3 rows=18  width=5..7
group 4 rows=15  width=6..9
group 5 rows=11  width=3
group 6 rows=15  width=5
group 7 rows=11  width=3
group 8 rows=4   width=5
```

| Table | Verified battle semantics | Status |
| --- | --- | --- |
| `aq.c[0]` | Monster/species data. `[0]` name text id, `[1]` element, `[3]` quality/random stat range, `[5..7]` HP formula, `[8..10]` attack formula, `[11..13]` defense formula, `[14..16]` speed formula, `[17]` sprite id, `[18]` learn-table group, `[19..21]` evolution metadata, `[22]` relation/catch class. | VERIFIED |
| `aq.c[1]` | Skill table. `[0]` element/family, `[1]` name text id, `[2]` description text id, `[3]` power percent, `[4]` learn threshold/tier, `[5]` PP max, `[6]` effect display/apply mode, `[7]` secondary buff/debuff id or `-1`, `[8]` chance/param, `[9]` target side: `0` opponent side, `1` same side. | VERIFIED |
| `aq.c[2]` | Passive/badge battle bonuses. Used by `game.g.c(type,slot)` checks: HP bonus `[1][6]`, defense bonus `[2][6]`, damage bonus `[3][5]`, defense adjustment `[4][5/6]`, post-win heal `[0][6]`, damage bonus `[6][5]`. | VERIFIED |
| `aq.c[3]` | Status/form config. Used for status id names/icons, formula modifiers (`[0]`, `[1]`, `[2]`, `[3]`, `[4]`, `[5]`, `[8]`, `[10]`, `[11]` observed). Variable row width is expected. | VERIFIED |
| `aq.c[4]` | Item table. `[0]` name, `[1]` icon, `[2]` desc, `[3]` price/value, `[4]` currency/type, `[5]` item behavior, `[6..8]` behavior params. In battle `[5]` drives `game.b.w/x`; catch uses `[6]` as ball modifier. | VERIFIED |
| `aq.c[5]` | Special/task/egg item group. Used outside core damage, but can appear in post-battle reward/UI. | VERIFIED |
| `aq.c[6]` | Buff/self effect config. `[0]` name text id, `[1]` description text id, `[2]` duration, `[3]` param1, `[4]` param2. Applied into `game.b.v`; row ids `0..14` map to the self/buff ids above. | VERIFIED |
| `aq.c[7]` | Debuff/target effect config. `[0]` name text id, `[1]` description text id, `[2]` duration. Applied into `game.b.w`; row ids `0..10` map to the target/debuff ids above. | VERIFIED |
| `aq.c[8]` | Skill learn thresholds by species learn group and tier. `game.b.F/G` checks whether a skill can be learned by comparing `aq.c[1][skill][4] <= aq.c[8][learnGroup][tier]`. | VERIFIED |

`aq.c[1][skill][6/7/8/9]` closure:

| Column | Meaning | Evidence | Status |
| --- | --- | --- | --- |
| `[6]` | Skill extra-effect display/apply mode. When value is `1`, `game.d.q()` displays effect name from `aq.c[6][effectId][0]`; other values are used by hardcoded skill-id branches. | `game.d.q()`, `game.b.b(target)`. | VERIFIED |
| `[7]` | Buff/debuff/effect id. `-1` means no secondary effect. If applied to target, it indexes `aq.c[7]`; if applied to self/same-side branch, it indexes `aq.c[6]`. | `game.b.b(target)`, `game.d.q()`. | VERIFIED |
| `[8]` | Multipurpose chance/parameter: proc chance for secondary effects, damage percent for conditional skills, heal percent for some skills. | `game.b.b(target)`, `game.d.q()`. | VERIFIED |
| `[9]` | Target side and HP-display basis: `0` opponent side, `1` same side. | `game.d.b(byte)` builds `h.G/H` by this value. | VERIFIED |

## 18. Port Plan For Battle

Recommended rebuild order:

1. Port `game.b` as `BattleUnit` with named fields: speciesId, level, maxStats, currentStats, skillIds, skillPP, buffs, debuffs.
2. Port table access wrappers around `aq.c[0..8]`; do not scatter magic indexes across code.
3. Port `game.d` state enum using the state matrix above.
4. Port battle UI facade from `game.h`: command, skill list, ball list, item target, level-up.
5. Port deterministic turn order `T()`: sort by speed `c[4]`, special priority status `f(7)`, `J` used flag.
6. Port skill execution state `7` and formula from `game.b.b(target)`; write tests for neutral, advantage, disadvantage, crit, miss/dodge, status apply.
7. Port catch and EXP after core attack loop.
8. Only after gameplay works, tune animation scripts `pos/cpos/effect/speffect/blood/bufDebuf` for pixel/timing.

## 19. Remaining Battle Audit

- State semantics for `18/19/24/101/102/104`: CLOSED in this pass.
- Skill/effect table columns `aq.c[1][6/7/8/9]`: CLOSED in this pass.
- Buff/debuff runtime tables `aq.c[6]`, `aq.c[7]`, `game.b.v/w/x/N`: CLOSED in this pass.
- Damage formula `game.b.b(target)` and relation helper `game.b.a(target)`: CLOSED from `game__b.javap.txt`; port should implement the bytecode-equivalent algorithm above.
- Still useful during implementation: run sample battle regression tests for damage/catch/EXP to catch port mistakes.
- Still separate renderer work: map battle animation scripts field-by-field (`effect.mid`, `speffect.mid`, `bufDebuf.mid`) for pixel/timing parity.
- Still gameplay/UI integration work: state `5` pet switch UI should be tested end-to-end because the UI path is split between `battle.ui`, `petstate.ui`, and `game.h.X()`.

Ket luan: battle logic/state/formula da du de port that, khong con treo o muc "chua ro". Nhung thu con lai la validation khi implement va renderer timing, khong phai thieu hieu biet ve state machine hay cong thuc.
