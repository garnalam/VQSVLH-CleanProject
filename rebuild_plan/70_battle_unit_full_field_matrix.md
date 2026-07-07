# 70 Battle Unit Full Field Matrix

Status: SOURCE AUDIT ONLY.

Scope: phase 1, part 1. This document audits `game.b` field-by-field and maps
the battle-unit data it owns or directly mutates. No rebuild code was changed in
this step.

Primary sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/n.java`
- `rebuild_game/src/main/java/VqsvBattleUnit.java`

Important CFR note: `game.b` is decompiled as `extends f`, while the stat and
actor-base fields it uses (`c`, `d`, `i`, `j`, `n`, `p`, flags) are visible in
`n.java`. The source is damaged/inconsistent around the inheritance chain, so
this audit treats those fields as inherited actor-base fields and only claims
the behavior that `game.b` directly reads or writes.

## Core Stat Arrays

| Source field | Owner | Meaning from `game.b` use | Evidence | Rebuild equivalent | Status |
| --- | --- | --- | --- | --- | --- |
| `c` | inherited `n`, reallocated in `game.b()` as `short[23]` | Base stat table for the pet/unit. `c[0]` quality, `c[1]` max HP, `c[2]` attack, `c[3]` defense, `c[4]` speed, `c[5]` form/status nature, `c[6]` side/init flag. Other indices mirror species table access and are not directly assigned by `game.b` init. | `game.b.a(int,int,short,byte,short,byte)`, `V()`, `P()` | `BattleUnit.baseStats` | PORTED/PARTIAL |
| `d` | inherited `n`, reallocated in `game.b()` as `short[23]` | Current mutable stat table. `d[1]` current HP, `d[2]` current attack, `d[3]` current defense, `d[4]` current speed, `d[6]` item/revive/state flag saved in `P()`. Reset from `c` by inherited `n.g()`. | `k(int)`, `l(int)`, `S()`, `g()`, `P()` | `BattleUnit.currentStats` | PORTED/PARTIAL |
| `M` | static `short[]` | Quality multiplier table `[90,95,100,110,125]`, indexed by `c[0]-1` for stats. | stat formulas in init, `V()`, static `a(...)` | `BattleSpeciesRow` stat helpers / `BattleUnit.fromSpecies` | PORTED |
| `t` | static `byte[]` | Evolution level thresholds for evolution class from `aq.c[0][evolveSpecies][2]`: `[12,30,5]`. | `J()` | not in battle runtime | NON_BATTLE / PENDING EVOLUTION |

## Base/Current Stat Index Matrix

| Index | Source meaning | Evidence | Rebuild constant | Status |
| --- | --- | --- | --- | --- |
| `0` | Quality/star value. Randomized from `aq.c[0][species][3]` when input is `-1`, saved by `P()`. | init lines assigning `c[0]`; `P()[4]` | `STAT_QUALITY` | PORTED |
| `1` | HP/max HP in `c[1]`, current HP in `d[1]`. | stat formula, `k/l/S/L/M/N/u` | `STAT_HP` | PORTED |
| `2` | Attack/current attack. | stat formula, `B()`, `e(byte)`, buff/debuff logic | `STAT_ATTACK` | PORTED/PARTIAL |
| `3` | Defense/current defense. | stat formula, `B()`, buffs `v`, debuffs `w` | `STAT_DEFENSE` | PORTED/PARTIAL |
| `4` | Speed/current speed and crit contribution. | stat formula, damage formula, debuff `w[5]`, buff `v[7]/v[9]` | `STAT_SPEED` | PORTED/PARTIAL |
| `5` | Form/status/nature marker passed as `s2`; checked by `f(byte)`. | init `c[5]=s2`; `f(byte)`; damage form statuses 0..4 | `STAT_FORM` | PORTED/PARTIAL |
| `6` | Input `by` saved in base and `d[6]` saved; item case 6 can set `d[6]=2`. Exact user-facing meaning belongs to item/status audit. | init `c[6]=by`; `P()[3]=d[6]`; item `w(int)` case 6 | `STAT_SIDE_FLAG` plus `currentStats[6]` | PARTIAL |
| `7..22` | Not initialized as battle stats in `game.b`; species metadata read directly from `aq.c[0]` via `j(byte)`. | `j(byte)` returns `aq.c[0][V][by]` | species table rows | NOT_UNIT_STATE |

## Identity, Save, Level, EXP

| Source field | Meaning | Evidence | Rebuild equivalent | Status |
| --- | --- | --- | --- | --- |
| `V` | Species id. | `q()`, init, species table lookups, save `P()[0]` | `BattleUnit.speciesId` | PORTED |
| `T` | Level. Max level check is `T == 50`. | `s()`, `t()`, `v()`, `h(int)`, save `P()[1]` | `BattleUnit.level` | PORTED |
| `S` | EXP/progress toward next level. | `g(int)`, `z()`, `O()`, save `P()[7]` | `BattleUnit.exp` | PORTED/PARTIAL |
| `E` | Save metadata byte from `a(short hp,int exp,int n3)`, saved as `P()[8]`. Exact semantic is outside battle turn logic. | `a(short,int,int)`, `P()` | source pet metadata not fully modeled | PENDING/NON_CORE |
| `W` | Nature/personality type. Values 7/8/9 modify attack/speed/HP. Saved as `P()[5]`. | `i(byte)`, `P()` | `BattleUnit.natureType` | PORTED |
| `C` | Battle sprite/visual id from `aq.c[0][species][17]`. | init, `c()` loads sprite `C`, `z()` passes to AH, `P()` indirectly through species | `BattleUnit.visualSpriteId` | PORTED |
| `X` | Battle side/owner flag. Source uses `0` for player side and `1` for enemy side in current battle paths. | `f(int)`, `r()`, battle entry sets 0/1, formula checks `X==0` for player passives | `BattleUnit.ownerSide` | PORTED |
| `Q` | Previous/latched HP for UI percent animation. Set before HP changes by `u(int)`. | `M()`, `N()`, `u(int)`, `k/l` | render HP latch not fully mirrored | PARTIAL/UI |
| `R` | Scratch int set to current EXP at battle setup with `j(z())`; getter `A()`. Likely previous EXP latch for result UI. | `j(int)`, `A()`, battle setup in `game.d` | not modeled | PENDING/UI_RESULT |

## Skills and PP

| Source field | Meaning | Evidence | Rebuild equivalent | Status |
| --- | --- | --- | --- | --- |
| `z` | Skill id slots, length 5, initialized to `-1`. | constructor, `b(int[])`, `g(byte)`, `t(int)`, `a(byte,b)` | `BattleUnit.skillIds` | PORTED |
| `y` | Current PP per skill slot, length 5. Reset from `aq.c[1][skill][5]`. | constructor, `v()`, `B(int)`, `b(int[])`, `Q()` | `BattleUnit.skillPp` | PORTED/PARTIAL |
| `O` | Skill count. | `b(int[])`, `g(byte)`, `E()`, `P/Q` | `BattleUnit.skillCount` | PORTED |
| `D` | Currently selected skill id for a turn. | `a(byte,b)` sets `D`; `H()` returns `D`; damage uses `aq.c[1][D]` | `BattleUnit.selectedSkillId` | PORTED |
| `I` | Selected target slot/index for some target-selection paths. | `game.d` assigns from UI vector `H`; source field public | `BattleUnit.selectedTargetSlot` | PARTIAL |
| `G` | Vector used by battle selection/UI code; exact content requires `game.d/game.h` state audit. | field exists; not directly manipulated in `game.b` body | not modeled | PENDING/UI_SELECTION |
| `H` | Vector used by battle selection/UI code; `game.d` stores selected-target string values into it. | `game.d` accesses `this.h.H` and writes `h.I` from selected element | not modeled directly | PENDING/UI_SELECTION |

Skill methods:

| Method | Meaning | Status |
| --- | --- | --- |
| `b(int[])` | Load skill count, skill ids, PP from serialized array. | PORTED/PARTIAL |
| `Q()` | Save only skill count, ids, and PP. | PORTED/PARTIAL |
| `F()` | Compute learnable skill candidates by element, learn group, level tier. | PARTIAL |
| `G()` | Auto-learn default/eligible skills. | PARTIAL |
| `a(byte,b)` | Select skill, set target, consume PP, apply PP modifiers from buffs 8 and 12. | PORTED/PARTIAL |
| `s(int)` | Check skill slot has PP. | PORTED |

## Buff Slots `v[16][5]`

`v` is the source positive/self-side buff table. `m(id)` checks active state
with `v[id][4] == 1`.

Common slot layout from source usage:

| Sub-index | Meaning |
| --- | --- |
| `[0]` | remaining duration/counter |
| `[1]` | primary value |
| `[2]` | secondary value |
| `[3]` | related skill id or source id for selected buff types |
| `[4]` | active flag, `1` active |

| Buff id | Source behavior in `game.b.a(byte,int,int)` / formula | Rebuild status |
| --- | --- | --- |
| `0` | Defense increase plus stored extra damage value; damage formula adds `v[0][2]` if duration reaches 0. | PARTIAL |
| `1` | Defense decrease style self modifier; damage formula boosts damage by `v[1][2]%`. | PARTIAL |
| `2` | Defense increase. | PARTIAL |
| `3` | Heal by percent of max HP. | PARTIAL |
| `4` | Skill-derived defense increase, stores skill id in `K[4]`. | PARTIAL |
| `5` | Chance/value holder used by target reflection/absorb check in damage formula. | PARTIAL |
| `6` | Chance/value damage reduction or conversion. Notable source quirk: target checks `target.m(6)` but reads attacker `v[6][1]/v[6][2]`. | PORTED/PARTIAL |
| `7` | Skill-derived speed increase, stores skill id in `K[7]`. | PARTIAL |
| `8` | PP cost/damage modifier: selected skill loses extra PP; damage formula also boosts. | PARTIAL |
| `9` | Speed increase and defense decrease. | PARTIAL |
| `10` | Attack increase. | PARTIAL |
| `11` | Copies buffs from another battle unit indexed by `v[11][1]`, uses `x/N/K`. | PENDING |
| `12` | PP conservation / extra-turn state using `K[12]`. | PARTIAL |
| `13` | Heal and clears debuffs. | PARTIAL |
| `14` | Clears debuffs. Also blocks target debuff in damage formula when active. | PARTIAL |
| `15` | Stores `n2 * aq.c[6][15][3]`; exact gameplay meaning needs table audit. | PENDING |

Methods:

| Method | Meaning | Status |
| --- | --- | --- |
| `a(byte,int,int)` | Apply buff by id. | PARTIAL |
| `n(int)` | Clear one buff and restore mutable stats. | PORTED/PARTIAL |
| `D()` | Clear all buffs and active buff queue. | PORTED/PARTIAL |
| `d(int,int)` | Tick one buff duration and clear when expired. | PARTIAL |

## Debuff Slots `w[11][5]`

`w` is the source negative/target-side debuff table. `p(id)` checks active state
with `w[id][4] == 1`.

Common slot layout mirrors `v`:

| Sub-index | Meaning |
| --- | --- |
| `[0]` | remaining duration/counter |
| `[1]` | primary value |
| `[2]` | secondary value, rarely used |
| `[3]` | source skill id |
| `[4]` | active flag |

| Debuff id | Source behavior in damage/apply/tick | Rebuild status |
| --- | --- | --- |
| `0` | Damage-over-time style value; `q(0)` damages by `w[0][1] / aq.c[1][sourceSkill][8]`. | PARTIAL |
| `1` | Presence modifies skill ids 23/29 damage. | PARTIAL |
| `2` | Target defense modifier used by attacker `B()` when target has `f(2)` form/status. | PARTIAL |
| `3` | Damage-over-time percent of stored value when duration low. | PARTIAL |
| `4` | Presence/skill-status effect; exact behavior belongs to status table audit. | PENDING |
| `5` | Speed decrease: stores `base speed * skill param / 100`, lowers `d[4]`. | PORTED/PARTIAL |
| `6` | Damage reduction percent applied to incoming computed damage. | PARTIAL |
| `7` | Defense decrease: stores `base defense * skill param / 100`, lowers `d[3]`. | PORTED/PARTIAL |
| `8..10` | Allocated but not directly switched in visible `game.b` debuff apply/tick body. | PENDING |

Methods:

| Method | Meaning | Status |
| --- | --- | --- |
| `b(b target)` | Computes damage and may apply target debuff from `aq.c[1][D][7]`. | PORTED/PARTIAL |
| `q(int)` | Apply per-turn debuff damage/stat effects. | PARTIAL |
| `c(int,int)` | Tick one debuff duration and clear when expired. | PARTIAL |
| `C()` | Clear all debuffs and active debuff queue. | PORTED/PARTIAL |
| `C(int)` | Clear one debuff and restore mutable stats. | PORTED/PARTIAL |

## Active Effect Queue and Scratch

| Source field | Meaning | Evidence | Rebuild equivalent | Status |
| --- | --- | --- | --- | --- |
| `x[2][3]` | Active visual/status id queue. Bank `0` for buffs, bank `1` for debuffs. Holds up to 3 ids. | initialized to `-1`; private `a(int,byte)` inserts; `e(int,int)` removes | `BattleUnit.activeEffectQueue` | PORTED/PARTIAL |
| `N[2]` | Active count per `x` bank. | private `a(int,byte)`, `e(int,int)`, `r(int)` | `BattleUnit.activeEffectCount` | PORTED/PARTIAL |
| `K[16]` | Scratch per buff/status. Known uses: `K[4]` and `K[7]` store source skill id, `K[5]` stores reflected damage, `K[12]` tracks PP/turn behavior. | buff apply, damage formula, `game.d` turn dispatch | `BattleUnit.effectScratch` | PORTED/PARTIAL |
| `F` | Incrementing stat-growth counter up to 20; `y()` raises `d[2..4]` by `F%`. Reset in `game.d` battle setup. | `y()`, `game.d` sets `F=0` | not explicitly modeled | PENDING |
| `J` | Turn-used/skip flag in `game.d` dispatch. | `game.d` sets/checks `h.J` during turn selection | `BattleUnit.turnUsed` | PORTED/PARTIAL |

## Render and Animation Fields

These are battle-unit fields but should not be mixed into the stat model.

| Source field | Meaning | Evidence | Rebuild equivalent | Status |
| --- | --- | --- | --- | --- |
| `U` | Actor animation state. `0` idle, `1` action/attack, `2` hit, `3` dead, `4` alternate idle/recover. | `d(byte)`, `p()`, `game.d` P7 checks `p()` | P7 base state fields | PORTED/PARTIAL |
| `u` | Temporary actor action `ah` spawned by P7 non-special effect chunks. | `a(short,byte)`, `game.d.n()`, P7 branch | `P7ActorAnimation` | PORTED/PARTIAL |
| `L` | Species-specific special effect `ah` shown during state `1` and death effect. | `z(int)`, `d(byte)`, `a(Graphics)` | battle L effect renderer | PORTED/PARTIAL |
| `Z` | Draw ordering flag for `L`; species 10 sets `Z=1` to draw L after base actor. | `d(byte)` case species 10; `a(Graphics)` | `battleLDrawAfter` | PORTED |
| inherited `a` | Sprite animator object for base actor. | `c()`, `d(byte)`, `a(Graphics)`, frame triggers | `SpriteAnim` / battle renderer | PARTIAL |
| inherited `i/j` | Actor screen/world position for battle draw/effects. | `a(short,byte)`, `z(int)`, `game.d` P7 special setup | battle renderer positions | PARTIAL |
| inherited `n` | Direction. Passed into AH effect and draw. | `a(short,byte)`, `z(int)`, `a(Graphics)` | battle sprite direction | PARTIAL |
| inherited `f` | Visibility flag checked before drawing base actor. | `a(Graphics)` | `battleP7BaseHidden*` inverse | PARTIAL |

## Item / Inventory Use Hooks

| Source method/field | Meaning | Rebuild status |
| --- | --- | --- |
| `w(int itemId)` | Applies item behavior from `aq.c[4][item][5]`: heal, PP restore, heal+PP, revive, clear debuffs, set `d[6]=2`; then decrements inventory through `game.g.o().d`. | PARTIAL |
| `x(int itemId)` | Returns item usability/error code based on alive state, HP full, PP full, debuffs present, `d[6]`. | PENDING/PARTIAL |
| `B(int)` | Restore PP across learned skills by amount, clamped to skill max PP. | PARTIAL |

## Evolution / World Hooks

| Source field/method | Meaning | Rebuild status |
| --- | --- | --- |
| `J()` | Checks species evolution data in `aq.c[0]`, level threshold `t[]`, item/material in `game.g`, then pushes evolution candidate to `game.k.H` and `game.k.L`. | NON_BATTLE / PENDING |
| `R()` | Returns evolution category from species evolution target table. | NON_BATTLE / PENDING |
| `Y` | Boolean flag with getter/setter `K()`/`d(boolean)`. No direct battle-core behavior found in `game.b`; likely world/UI state. | NON_CORE_UNRESOLVED |
| `A`, `B` | Declared in `game.b`; no direct source use found in `game.b` body. May be accessed externally or CFR artifact. | NON_CORE_UNRESOLVED |

## Save Layout

`P()` serializes the full pet/unit state:

| Save index | Source value | Meaning |
| --- | --- | --- |
| `0` | `V` | species id |
| `1` | `T` | level |
| `2` | `c[5]` | form/status/nature marker |
| `3` | `d[6]` | item/revive/state flag |
| `4` | `c[0]` | quality |
| `5` | `W` | nature/personality type |
| `6` | `d[1]` | current HP |
| `7` | `S` | EXP |
| `8` | `E` | metadata byte |
| `9` | `O` | skill count |
| `10..` | `z[]`, then `y[]` | skill ids and PP |

`Q()` serializes only skill count, ids, and PP.

## Current Rebuild Coverage Summary

| Area | Status | Notes |
| --- | --- | --- |
| Species id, level, quality, max/current HP, attack, defense, speed | PORTED/PARTIAL | Main formula fields exist in `BattleUnit`. |
| Skill ids, PP, selected skill, PP consume | PORTED/PARTIAL | Basic behavior exists; full UI/target selection edge cases still need phase 2/3. |
| Buff/debuff arrays and active queues | PORTED/PARTIAL | Structures exist; not every id behavior is complete. |
| Damage formula field access | PORTED/PARTIAL | Direct damage is close, but phase 2 must validate every table semantic. |
| Item use flags and item behavior | PARTIAL/PENDING | Needs later item phase. |
| Evolution/world hooks | NON_BATTLE/PENDING | Do not block battle engine core. |
| Render/effect actor fields `U/u/L/Z` | PORTED/PARTIAL | Covered by battle animation work, not by stat model. |
| UI selection vectors `G/H/I` | PENDING/UI_SELECTION | Needs `game.d/game.h` state audit, not stat model. |

## Phase 1 Part 1 Conclusion

All important battle-unit runtime fields in `game.b` are now identified and have
a rebuild target or explicit non-core classification. The remaining unresolved
items in this file are not HP/stat/skill/status backbone fields; they are UI
selection, item/evolution, or render/scratch concerns to be closed in later
phase-1 and battle-state audits.
