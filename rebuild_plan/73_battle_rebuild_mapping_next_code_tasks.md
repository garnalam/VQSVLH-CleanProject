# 73 Battle Rebuild Mapping / Next Code Tasks

Status: SOURCE/CODE MAPPING AUDIT ONLY.

Scope: phase 1, part 3. This document maps the source audits in `70`, `71`,
and `72` onto the current rebuild code and closes the immediate code-task list
for the next battle-engine slice.

No rebuild code was changed in this step.

Primary rebuild files:

- `rebuild_game/src/main/java/VqsvBattleTables.java`
- `rebuild_game/src/main/java/VqsvBattleUnit.java`
- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvSourceModels.java`

Primary source docs:

- `rebuild_plan/70_battle_unit_full_field_matrix.md`
- `rebuild_plan/71_battle_skill_status_table_matrix.md`
- `rebuild_plan/72_battle_full_skill_status_behavior_classification.md`

## High-Level Result

The current rebuild has the correct battle data backbone, but it is not yet a
full source-equivalent battle engine.

| Area | Current state | Reason |
| --- | --- | --- |
| `db.mid` table loading | PORTED/PARTIAL | All 9 groups are loaded. Dedicated wrappers exist for species, skill, status, item, buff, debuff. Passive `aq.c[2]`, misc `aq.c[5]`, and learn `aq.c[8]` mostly use generic `row(...)`. |
| `game.b` core fields | PORTED/PARTIAL | Main arrays and fields exist in `BattleUnit`: `c/d/v/w/x/N/y/z/P/K`, species, level, exp, side, selected skill/target. Some are present but not fully used. |
| Direct damage formula | PORTED/PARTIAL | `BattleUnit.computeDamage()` implements the main source switch families, relation, crit, partial passive/status/debuff hooks. |
| Battle state shell | PORTED/PARTIAL | `SourceBattleRuntime` has P0/P20/P3/P6/P21/P17/P4/P16/P5/P11/P10/P2/P7/P1/P8/P9. Several flows are still source-shaped, not full parity. |
| P7 animation/effect backbone | PORTED/PARTIAL | Effect rows, actor `u`, speffect AH type 1/9, L effects, state triggers are partially ported. Full skill/status side effects are not wired. |
| Buff/debuff/status runtime | PARTIAL/MISSING | Slots exist and some formula hooks exist. Full apply/tick/clear/display lifecycle is missing. |
| Passive/global hooks | PARTIAL/MISSING | Boolean placeholders exist, but they are not wired from real `game.g/game.k` save/global state. |
| Items/catch | PARTIAL/APPROX | Catch and item menu flows exist. Catch and item behavior are not full `game.d/game.b.w/x` parity. |

## Rebuild Mapping: Data Tables

| Source table | Rebuild equivalent | Status | Gap |
| --- | --- | --- | --- |
| `aq.c[0]` species | `BattleSpeciesRow` | PORTED/PARTIAL | Evolution fields `[19..21]` are not modeled for battle; okay for current battle core. |
| `aq.c[1]` skills | `BattleSkillRow` | PORTED/PARTIAL | All columns are parsed. Behavior for `SOURCE_SWITCH_GAP` skills is not validated. |
| `aq.c[2]` passive hooks | generic `VqsvBattleTables.row(2,id)` only | MISSING/PARTIAL | No `BattlePassiveRow`; current `BattleUnit` booleans are not loaded from `game.g.o().c(...)`. |
| `aq.c[3]` status/form | `BattleStatusRow` | PORTED/PARTIAL | Important ids are used; ids `6,7,9,12..17` still not source-closed. |
| `aq.c[4]` items | `BattleItemRow` | PORTED/PARTIAL | Catch/heal shell exists; full `game.b.w/x` behavior missing. |
| `aq.c[5]` misc | generic row only | UNKNOWN | Not proven as battle-core dependency. Do not port until source path is proven. |
| `aq.c[6]` buffs | `BattleBuffRow` | PORTED/PARTIAL | Rows parsed, but full apply method equivalent to `game.b.a(byte,int,int)` is missing. |
| `aq.c[7]` debuffs | `BattleDebuffRow` | PORTED/PARTIAL | Rows parsed; apply is partial, per-turn tick is mostly missing. |
| `aq.c[8]` learn threshold | generic row in `BattleUnit.loadDefaultSkillsFromSpecies()` | PORTED/PARTIAL | Learn/replace UI and save update are not complete. |

## Rebuild Mapping: `game.b` Fields

| Source field/area | Rebuild field | Status | Notes |
| --- | --- | --- | --- |
| `c[23]` base stats | `BattleUnit.baseStats` | PORTED/PARTIAL | Main indices `0..6` are set; metadata indices remain table-backed. |
| `d[23]` current stats | `BattleUnit.currentStats` | PORTED/PARTIAL | HP/atk/def/speed work. `d[6]` item/revive state not fully wired. |
| `v[16][5]` self buffs | `BattleUnit.buffSlots` | PARTIAL | Slots exist; apply/tick/clear behavior incomplete. |
| `w[11][5]` target debuffs | `BattleUnit.debuffSlots` | PARTIAL | Slot writes exist for ids `0,3,4,5,6,7`; tick behavior missing for many ids. |
| `x[2][3]`, `N[2]` active queues | `activeEffectQueue`, `activeEffectCount` | PARTIAL | Insert/reset exists; UI rendering and duration removal not fully source-equivalent. |
| `y[5]`, `z[5]`, `O` skills/PP/count | `skillPp`, `skillIds`, `skillCount` | PORTED/PARTIAL | PP consume exists; PP restore/item/learn replacement partial. |
| `D`, `I` selected skill/target | `selectedSkillId`, `selectedTargetSlot` | PORTED/PARTIAL | P3/P6 set selected skill/target; selectedTargetSlot is not strongly used after selection. |
| `K[16]` scratch | `effectScratch` | PARTIAL | Used for buff 5/12, but not all source scratch cases are wired. |
| `P[4]` preview stats | `previewStats` | MISSING/PARTIAL | Field exists but source preview methods `w()/x()/i()` are not used by runtime. |
| `Q/R` HP/EXP UI latches | no direct field | MISSING/UI | HP/EXP display uses rebuild UI fields, not source latch semantics. |
| `F` stat-growth counter | no direct field | MISSING | Needed for passive hook `aq.c[2][5]` / `game.b.y()`. |
| `E`, `Y`, evolution/world fields | mostly absent | PENDING/NON_CORE | Not required for immediate battle formula, but needed for full save/evolution parity. |
| `U/u/L/Z` animation fields | P7 runtime fields + `P7ActorAnimation` | PORTED/PARTIAL | Good backbone, but not all AH/speffect types or exact source timing are done. |

## Formula / Skill Coverage

| Behavior family | Source skill ids | Rebuild status | Gap |
| --- | --- | --- | --- |
| Direct simple | See `72` direct list | PORTED/PARTIAL | Damage value runs through `BattleUnit.computeDamage()`. Post-effect display/result handling incomplete. |
| Direct plus divisor | `1,7` | PORTED/PARTIAL | Damage branch exists; debuff tick/display partial. |
| Explicit debuff chance | `2,8,22,28,41,47` | PORTED/PARTIAL | Chance application exists, but debuff ids `8,9,10` behaviors are not closed. |
| Conditional target debuff | `3,9,23,29` | PORTED/PARTIAL | Formula exists; source test coverage needed. |
| Clear target buffs | `43,49` | PORTED/PARTIAL | `target.clearBuffs()` exists; active queue/UI and timing partial. |
| HP percent scaling | `53,59` | PORTED/PARTIAL | Formula exists; minimum damage/source clamp needs deterministic check. |
| Hardcoded post-hit heal | `11,17,52,58` | MISSING/PARTIAL | `game.d.q()` heal paths are not wired into P7 result lifecycle. |
| Hardcoded self buff | `21,27,42,48,62,68` | MISSING/PARTIAL | Needs `applySourceBuff(...)` and correct damage/control-flow validation for gap skills. |
| Buff with selected index | `64` | MISSING | Requires selected-index semantics for buff `11`. |
| SOURCE_SWITCH_GAP | `21,27,42,48,62,67` | UNKNOWN/PENDING | Do not port by guessing from row. Needs bytecode/control-flow validation. |

## Runtime Coverage

| Runtime slice | Current status | Gap |
| --- | --- | --- |
| P20 command UI | PORTED/PARTIAL | Commands work, but exact disabled bitmask/status UI is not full `game.h`. |
| P3 skill list | PORTED/PARTIAL | Uses `choiceskill.ui`-shaped UI and source skill rows. Needs disabled state and per-skill warnings parity. |
| P6 target select | PORTED/PARTIAL | Basic target vector from `skill[9]` exists. Multi-unit/formation parity is still limited. |
| P7 resolve | PORTED/PARTIAL | Animation backbone exists. Damage result currently collapses to amount; crit/debuff/post-effect lifecycle incomplete. |
| P21/P17 catch | PORTED/PARTIAL/APPROX | Ball list and chance shape exist; success can be forced for tutorial; storage parity incomplete. |
| P4/P16 item | PARTIAL/APPROX | Heal behavior exists but ignores flat param in some cases, PP restore is approximate, revive/status clear incomplete. |
| P5 pet switch | PARTIAL | Menu exists; full switch validation/status/turn effects pending. |
| P11 shop | APPROX | Minimal buy flow, not full battle shop parity. |
| P10 run | PARTIAL | Flow exists; source chance/restrictions need audit. |
| P1 turn dispatch | PARTIAL | Speed order exists, but buff/debuff ticking and passive turn hooks are not source-complete. |
| P8/P9 result | PARTIAL | Branching works for current story smoke. EXP/level-up/reward parity incomplete. |

## APPROX Items To Avoid Treating As Complete

| Item | Why APPROX |
| --- | --- |
| Battle random | Rebuild uses deterministic `Random`; source uses `ae.a(...)`. Good for checks, not source RNG parity. |
| Tutorial catch item seed | Bunny path can seed item `0` if missing. This is smoke-friendly but not save/inventory parity. |
| P16 item heal | Current heal uses max HP percent; source also uses flat params and behavior-specific PP/revive/status logic. |
| P7 timing | Source-shaped phases exist, but not every `H.i()/H.e()/u.a(frame)` condition is closed for all skills. |
| Passive booleans | `sourcePassive*` fields exist, but not loaded from real `game.g.o().c(id,slot)` lifecycle. |
| Battle UI widgets | Command/choice/skill UIs are source-shaped. Full `game.h` widget/runtime parity is still pending. |

## UNKNOWN Items And Why

| Unknown | Reason |
| --- | --- |
| Skill ids `21,27,42,48,62,67` damage/control-flow | Decompiled switch does not clearly include them despite table rows implying behavior. Needs bytecode/control-flow validation before code. |
| Status ids `6,7,9,12..17` | No complete source consumption path proven in current audit. |
| `aq.c[5]` | Loaded from `db.mid`, but not proven as a `game.b` battle-core dependency. |
| Passive hook `aq.c[2][3]` row value | Source references `[5]`, decoded row has `-1` at `[5]`. Needs validation before enabling. |
| Buff id `15` | `game.b.a(byte,int,int)` has `case 15`, but `aq.c[6]` only has rows `0..14`. Treat as inaccessible/artifact until proven. |

## Next Code Tasks

These are ordered to reduce risk. Do not jump to UI polish before these logic
tasks are wired.

### Task 1: Preserve Full Damage Result Through P7

Goal: make P7 consume the complete `BattleDamageResult`, not only the damage
integer.

Required changes:

- Add a runtime field like `BattleDamageResult p7DamageResult`.
- Change `SourceBattleUnit.basicDamageTo(...)` or add a new method so
  `applyP7Damage()` can get `damage`, `critFlag`, and `appliedDebuffId`.
- Use `appliedDebuffId` to show debuff text from `aq.c[7][id][0]` when source
  would show it.
- Use `critFlag` to select the correct damage text style.

Why first: this is the bridge between the already-ported formula and the visible
P7 turn result.

### Task 2: Add `BattleUnit.applySourceBuff(...)`

Goal: port `game.b.a(byte buffId, int value, int sourceSkill)` into one rebuild
method.

Required first coverage:

- Buff ids `0..14` according to `72`.
- Preserve known source quirk for buff `6`.
- Add active queue insertion for bank `0`.
- Update mutable stats immediately for defense/speed/attack buffs.
- Return heal amount when source returns `n4`.

Wire points:

- `game.d.q()` equivalents in P7 after effect animation.
- Hardcoded skills `11,17,21,27,42,48,52,58,62,64,68`.

### Task 3: Add Debuff Tick / Duration Lifecycle

Goal: port `game.b.q(int)`, `c(int,int)`, `d(int,int)`, `C(int)`, `n(int)`
runtime behavior enough for turn dispatch.

Required first coverage:

- Debuff `0`, `3`, `5`, `6`, `7`.
- Buff duration clear for ids already applied.
- Active queue removal.

Wire point:

- P1/round transition, before selecting the next actor, matching source turn
  order as closely as current P1 allows.

### Task 4: Wire Passive Hook Context

Goal: replace ad-hoc passive booleans with source-backed hook state.

Required changes:

- Add `BattlePassiveRow` or a small helper around `aq.c[2]`.
- Add a `BattlePassiveContext` loaded from current `game.g`-equivalent source
  model, not hardcoded booleans.
- Wire hooks `0,1,2,4,6,7` first.
- Keep hook `3` and `5` gated until row/source ambiguity is resolved.

### Task 5: Complete Item Behavior `game.b.w/x`

Goal: move P16 item handling from approximate to source-shaped.

Required changes:

- Implement usability result equivalent to `game.b.x(itemId)`.
- Implement behavior ids `1..6` with percent + flat heal, PP restore, revive,
  debuff clear, `d[6]` flag.
- Use selected pet target, not always current player unit.

### Task 6: Validate SOURCE_SWITCH_GAP Skills Before Porting

Goal: do not guess skills `21,27,42,48,62,67`.

Required audit:

- Bytecode/control-flow check around `game.b.b(target)` and `game.d.q()`.
- Produce `74_battle_source_switch_gap_skill_validation.md`.
- Only after validation should these skills be wired into damage or no-damage
  routes.

### Task 7: Deterministic Smoke Matrix

Goal: one smoke PNG/check per source behavior family.

Required smoke families:

- Direct simple.
- Plus raw divisor.
- Explicit debuff chance.
- Conditional on debuff `0`.
- Conditional on debuff `1`.
- Clear target buffs.
- HP percent scaling.
- No-damage buff.
- Hardcoded heal.
- Catch success/fail.
- Item heal/PP/revive/status clear.

## Immediate Recommendation

Next code slice should be Task 1 only:

`Preserve Full Damage Result Through P7`

It is small, source-backed, and unlocks visible validation for crit/debuff
result text without touching the risky unknown skill group. After that, Task 2
can wire self-buffs and hardcoded post-skill effects into the same P7 lifecycle.
