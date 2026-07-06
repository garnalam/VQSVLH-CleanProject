# Battle Full Engine Port Plan

Date: 2026-07-06

Scope: replace the remaining `PORTED/APPROX` battle slice with a source-backed
`game.d/game.b/game.h` battle runtime in small, testable steps.

This document is created after the user observed the visible fallback battle
overlay:

```text
auto result ... -> branch ...
Scripted stub
```

Immediate fix in this slice: runtime rendering now calls the source-like battle
HUD path instead of the old stub overlay. This only fixes the visible fallback
UI. It does not complete the battle engine.

## Current Truth

Current rebuild files:

- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleRenderer.java`
- `rebuild_game/src/main/java/VqsvBattleScripts.java`
- `rebuild_game/src/main/java/VqsvBattleTables.java`
- `rebuild_game/src/main/java/VqsvBattleUnit.java`
- `rebuild_game/src/main/java/VqsvSourceModels.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Current battle status:

| Area | Status | Truth |
|---|---|---|
| Visible stub overlay | FIXED | `VqsvBattleRenderer.render(...)` now delegates to source-like HUD. |
| Battle table wrappers | PORTED | `VqsvBattleTables.java` reads `db.mid` groups `0..8` and exposes named row wrappers. |
| Battle unit structure | PORTED/PARTIAL | `VqsvBattleUnit.java` mirrors `game.b` arrays/slots and current battles now instantiate it. Damage formula is ported on `BattleUnit`; full buff/debuff tick/runtime is still partial. |
| Three story battle entry records | PORTED/APPROX | Sophie, Bunny, elder use source encounter ids and branch targets. |
| Source species/stat table | PORTED/APPROX | `aq.c[0]` is read from `db.mid`. |
| Basic damage loop | PORTED/PARTIAL | `SourceBattleUnit.basicDamageTo(...)` now delegates to `BattleUnit.computeDamage(...)`, a bytecode-shaped port of `game.b.b(target)`. Global passive state from `game.g/game.k` has hooks but is not fully wired to real save/global state yet. |
| Command UI | PENDING | No real command state `20 -> 3/21/4/5/11/10`. |
| Skill list and target select | PENDING | No `choiceskill.ui`, PP, target side, target cursor. |
| Catch flow | PORTED/PARTIAL | `P21 -> P17` now has source-backed ball list/icons, consume, chance formula, source-timed q-like result phases with sprite 269, `ah` type 8 offset effect, and bag/bank/full storage behavior. Exact bitmap transforms and full pet payload remain partial. |
| Item/pet switch | PENDING | No battle item list or pet switch flow. |
| Buff/debuff/status | PENDING | Tables understood, not ported into runtime. |
| EXP/level-up/learn skill | PENDING | Not ported. |
| Battle animation scripts | PENDING | `pos/cpos/effect/speffect/blood/bufDebuf` not driving visuals yet. |

## Source Facts To Use

Primary current-project docs:

- `rebuild_plan/41_battle_engine_three_stub_replacement_audit.md`
- `rebuild_plan/37_scene1_room0_group6_elder_battle_audit.md`
- `rebuild_plan/40_intro_to_elder_battle_closeout_audit.md`

Legacy module battle docs that still matter:

- `modules/rebuild_plan/17_battle_state_machine.md`
- `modules/rebuild_plan/18_battle_formula_status_matrix.md`

Original source:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/c.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/i.java`

Original resources:

- `modules/script/original/db.mid`
- `modules/script/original/pos.mid`
- `modules/script/original/cpos.mid`
- `modules/script/original/effect.mid`
- `modules/script/original/speffect.mid`
- `modules/script/original/blood.mid`
- `modules/script/original/bufDebuf.mid`
- `modules/ui/original/battle.ui`
- `modules/ui/original/choiceskill.ui`
- `modules/ui/original/choice.ui`
- `modules/ui/original/msgwarm.ui`
- `modules/ui/original/levelUp.ui`

## Original Battle State Target

The rebuild should converge on the source state graph from `game.d.P`:

```text
P0 entry
  -> P20 player command
       -> P3 skill list -> P6 target select -> P2/P7 execute
       -> P21 catch ball list -> P17 catch animation/result
       -> P4 item list -> P16 item target/use
       -> P5 switch pet
       -> P11 info/points
       -> P10 run
  -> P1 turn dispatch
       -> P12 enemy pre-turn effects
       -> P13 player pre-turn effects
       -> P2 enemy/player skill select
       -> P7 skill animation/resolve
  -> P8 win/EXP
  -> P22 level up
  -> P23 learn skill
  -> P9/P24 lose/revive
  -> return world
```

## Port Order

### Step 0: Remove Visible Stub Fallback

Status: DONE in this slice.

Change:

- `VqsvBattleRenderer.render(...)` now calls
  `renderSourceLikeBattleUi(...)`.

Smoke:

- `battle_fix_kidnapping_ui.png`
- `battle_fix_bunny_ui.png`
- `battle_fix_elder_ui.png`

### Step 1: Battle Data Wrappers

Goal: stop scattering magic `aq.c` indexes through runtime.

Create or extend:

- `VqsvBattleTables.java`
- `BattleSpeciesRow`
- `BattleSkillRow`
- `BattleStatusRow`
- `BattleItemRow`
- `BattleBuffRow`
- `BattleDebuffRow`

Must cover:

- `aq.c[0]` species;
- `aq.c[1]` skills;
- `aq.c[3]` status/form;
- `aq.c[4]` items/capture balls;
- `aq.c[6]` buffs;
- `aq.c[7]` debuffs;
- `aq.c[8]` learn thresholds.

Status:

- DONE: `VqsvBattleTables.java` added.
- DONE: loader reads all 9 `db.mid` groups through the same binary table parser
  used by the resource skeleton.
- DONE: wrappers added for species, skill, status, item, buff, and debuff rows.
- DONE: `SourceBattleUnit` now consumes `BattleSpeciesRow` instead of raw
  `short[]`.
- DONE: relation helper now uses species relation/catch class `[22]`, not
  sprite/visual id.
- PARTIAL: dedicated table regression command is not wired into `--check` yet.

Smoke/check:

- existing `--check`;
- a new battle-table check printing row counts and key rows:
  Bunny `34`, elder enemy `68`, Sophie enemy `5`, player pet ids.

### Step 2: Port `game.b` Unit Model

Goal: replace `SourceBattleUnit` with a real `BattleUnit` equivalent.

Must include:

- `speciesId`, `level`, `nature`;
- `baseStats/currentStats` matching `c[]/d[]`;
- `skillIds` and `skillPP`;
- `buffSlots v[16][5]`;
- `debuffSlots w[11][5]`;
- `activeEffectQueue x[2][3]`;
- status helpers matching `f/m/p`;
- exact integer order from source formulas.

Status:

- DONE: `VqsvBattleUnit.java` added.
- DONE: arrays match `game.b` constructor shape:
  - `baseStats c[23]`;
  - `currentStats d[23]`;
  - `skillPp y[5]`;
  - `skillIds z[5]`, initialized to `-1`;
  - `previewStats P[4]`;
  - `effectScratch K[16]`;
  - `buffSlots v[16][5]`;
  - `debuffSlots w[11][5]`;
  - `activeEffectQueue x={{-1,-1,-1},{-1,-1,-1}}`;
  - `activeEffectCount N[2]`.
- DONE: enemy/player factories create `BattleUnit` first, then expose current
  render/runtime fields through the old `SourceBattleUnit` bridge.
- DONE: current HP mutation through `SourceBattleUnit.damage(...)` also updates
  `BattleUnit.currentStats[1]`.
- DONE: basic helpers added for HP, PP, selected skill, buff/debuff active
  checks, effect queue insert, and clear buff/debuff reset.
- DONE: `game.b.b(target)` damage formula is ported into
  `BattleUnit.computeDamage(...)` for skill family damage, crit, relation,
  target debuff application, clear-buff skills, low-HP skills, clamp/jitter, and
  the verified odd `m(6)` behavior where the target buff check reads attacker
  `v[6][1]/v[6][2]`.
- DONE: `VqsvBattleDamageFormulaCheck.java` locks the current formula with
  focused regression cases.
- PARTIAL: full buff/debuff apply/tick outside direct attack resolution, item
  validation/use, catch, EXP, level-up, and save payload are not complete yet.
- PARTIAL: `game.g/game.k` passive branches are represented as `BattleUnit`
  hooks, but the rebuild has not wired those hooks to the real global/save state.

Keep `SourceBattleUnit` only as a temporary render/runtime bridge until the
new `BattleUnit` state machine and damage formula are wired fully.

### Step 3: Damage Formula Regression

Goal: implement `game.b.b(target)` equivalent before building command UI.

Required tests:

- neutral, strong, weak relation;
- species relation/catch class `[22]`;
- crit branch;
- low-HP attack boost;
- skill families `1/7`, `3/9`, `23/29`, `43/49`, `53/59`;
- debuff blocked by status `f(3)` and buff `m(14)`;
- odd verified buff `m(6)` behavior exactly as source bytecode;
- minimum damage clamp.

Status:

- DONE: `BattleUnit.computeDamage(...)` is now source-shaped against
  `game.b.b(target)` bytecode for the direct damage path.
- DONE: regression command:

```powershell
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvBattleDamageFormulaCheck
```

- VERIFIED in this slice:
  - skill family damage: base power, `1/7`, `3/9`, `23/29`, `43/49`, `53/59`;
  - target debuff application and `m(14)` block;
  - relation multiplier strong/weak;
  - status attack/defense modifiers from `aq.c[3]`;
  - minimum damage clamp;
  - odd source behavior for `m(6)`.
- PARTIAL: global passive data from `game.g.W/X`, `game.g.c(...)`, and
  `game.k.O` has no full rebuild owner yet. `BattleUnit` has hooks for these
  branches, defaulted off, so later global-state work can wire them without
  changing the formula shape.
- PENDING: animation remains pending.

### Step 4: Battle State Runtime Shell

Goal: replace auto-advance battle loop with a `BattleState` enum mirroring
`game.d.P`.

Initial states to port first:

- `P0` entry;
- `P20` player command;
- `P3` skill list;
- `P2` select/execute;
- `P7` resolve;
- `P1` dispatch;
- `P8` win;
- `P9` lose.

Do not port item/catch/pet switch in the same slice. Keep slices small.

Status:

- DONE/PARTIAL: `VqsvBattleRuntime.java` now uses `BattleRuntimeState` with
  source labels `P0`, `P20`, `P3`, `P2`, `P7`, `P1`, `P8`, `P9`.
- DONE: the old anonymous `phase 0..4` loop has been replaced by explicit
  battle entry, dispatch, command, skill-list, select/execute, resolve,
  win/lose, and exit states.
- DONE: route smoke still preserves current event results:
  - Sophie battle -> result `0`, branch `78`;
  - Bunny battle -> result `-1`, branch `-1`;
  - Elder battle -> result `0`, branch `10`.
- DONE: smoke checkpoints added for command-state inspection:
  - `battle_bunny_command_ui`;
  - `battle_elder_command_ui`.
- PARTIAL: `P20` and `P3` are state-shells that auto-select the first available
  battle action/skill for route smoke. They do not yet implement real
  input-driven `game.h` command/skill widgets.
- PARTIAL: Bunny capture still uses the existing scripted tutorial/capture path
  from the rebuild route. Full `P21 -> P17` catch flow is not part of this
  slice.
- PENDING: item, pet switch, catch list, run, command warnings, PP display,
  target cursor, and battle animations.

### Step 5: Battle Command UI

Goal: input-driven command menu instead of auto result.

Source-backed UI:

- `battle.ui` command ids `3..8`;
- left/right command navigation;
- action key/click confirm;
- disabled command warnings through `msgwarm.ui` where source proves it.

Current implementation status:

- PORTED/PARTIAL: `battle.ui` command text ids `3..8` are rendered at source
  x positions `7,48,88,128,168,208`, with the six source command labels:
  `Chiến đấu`, `Bắt được`, `Đạo cụ`, `Sủng vật`, `Thương điếm`, `Chạy trốn`.
- PORTED/PARTIAL: bottom command icon slots use the source battle UI cell layer
  and focus can move left/right.
- PORTED/PARTIAL: action key and mouse click are routed to battle command
  input. Input during the state-enter wait is buffered so route smoke does not
  lose confirm at `P20`.
- PORTED: confirming command `0` enters `P3` fight/skill shell.
- PORTED/PARTIAL: command `1` enters `P21` catch list using
  `/data/ui/choice.ui` source layout semantics:
  title `Pokemon ball`, subtitle `Tỉ lệ bắt`, action `Sử dụng`.
  The list is built from source bag items whose `aq.c[4][item][5] == 0`.
  Confirm consumes one ball and enters `P17` catch result. Catch chance follows
  the source `game.d.b(itemId)` formula shape: HP threshold, ball parameter,
  species quality, relation class, and cap. P17 now advances through q-like
  phases `0..4` and draws source sprite `269` from `spr_269_all(r)`.
  P17 timing now follows the sprite cursor/end shape, and q1/q4 port the
  source `ah` type 8 effect offsets/durations. Exact bitmap transforms from
  root `l/e` helpers are still PARTIAL.
- PORTED/PARTIAL: command `2` enters `P4` item list using `choice.ui`:
  title `Đạo cụ`, subtitle `Số lượng`, action `Sử dụng`.
  Confirm enters `P16` target selection for usable battle items, consumes the
  item, and applies current HP/PP-style side effects for known source rows.
  Full `game.b.x(item)` validation and every item behavior are PARTIAL.
- PORTED/PARTIAL: command `3` enters `P5` pet switch list. It lists non-active
  source pets and swaps the selected pet to active, then consumes the player
  action. Full death/status/switch restrictions are PARTIAL.
- PORTED/PARTIAL: command `4` enters `P11` shop flow. It renders source item
  rows from `aq.c[4]`, uses the source battle-shop price rule (`item 0` normal
  price, others double), subtracts money and adds bag item on confirm.
  Full `shopbuy.ui` widgets and quantity picker are PARTIAL.
- PORTED/PARTIAL: command `5` enters `P10` run logic. Story battles where
  source disallows running show `msgwarm` warning
  `Trận chiến này không thể trốn chạy`; free battles use the source speed-based
  chance shape. Full world return integration for non-story run is PARTIAL.
- PORTED/PARTIAL: `msgwarm.ui` warning layer is rendered for blocked catch,
  bind-status item/pet/run, no item count, no money, and run-not-allowed.
- PENDING: exact generic `game.h` widget runtime, exact `choice.ui/shopbuy.ui`
  scroll bar and icons, full item behavior table, full P17 secondary effects,
  full disabled-state bitmask UI, and save/global inventory parity.
- PENDING: this is not the full `game.h` widget runtime. It is a source-backed
  battle command bar inside the current battle renderer.

Update 2026-07-06:

- PORTED/PARTIAL: P17 secondary `H/ah` type 8 now uses the exact scale/offset
  arrays and duration/group counters from `game.d.e(byte)` for q1 and q4.
- PORTED/PARTIAL: P17 effect rendering now draws a cloned target sprite cell
  with nearest scale and `+50` RGB brighten, matching the proven `ah` case 8
  shape more closely than the previous full-size enemy-copy approximation.
- PORTED/PARTIAL: captured pet storage now records a `game.b.P()`-shaped
  `sourcePayload` in `SourcePetState`; bag/bank/full routing still follows
  `game.g.y()`.
- STILL PENDING: byte-for-byte MIDP `drawRGB` parity, full generic `game.h`
  widget runtime, and complete `game.g` save/global inventory parity.

Smoke generated for this slice:

- `rebuild_game/build_intro_demo/battle_command_ui_bunny_p20.png`
- `rebuild_game/build_intro_demo/battle_command_ui_elder_p20.png`
- `rebuild_game/build_intro_demo/battle_command_ui_elder_right.png`
- `rebuild_game/build_intro_demo/battle_command_ui_elder_click_pet.png`
- `rebuild_game/build_intro_demo/battle_flow_catch_p21.png`
- `rebuild_game/build_intro_demo/battle_bunny_catch_p17_anim_or_result.png`
- `rebuild_game/build_intro_demo/battle_bunny_after_catch_route.png`
- `rebuild_game/build_intro_demo/battle_catch_fail_or_warning.png`
- `rebuild_game/build_intro_demo/battle_flow_item_p4.png`
- `rebuild_game/build_intro_demo/battle_flow_item_target_p16.png`
- `rebuild_game/build_intro_demo/battle_flow_pet_p5.png`
- `rebuild_game/build_intro_demo/battle_flow_shop_p11.png`
- `rebuild_game/build_intro_demo/battle_flow_run_warning.png`
- `rebuild_game/build_intro_demo/battle_route_sophie_after_command_ui.png`
- `rebuild_game/build_intro_demo/battle_route_bunny_after_command_ui.png`
- `rebuild_game/build_intro_demo/battle_route_elder_after_command_ui.png`

Smoke must allow user to stop at battle command and press/click:

- Fight;
- Catch;
- Item;
- Pet;
- Points;
- Run.

### Step 6: Skill UI And Target Select

Goal: port `choiceskill.ui` and skill execution path.

Required:

- skill name/PP list from `aq.c[1]`;
- PP check;
- skill description id;
- target side from skill `[9]`;
- same-side/opponent-side target masks;
- `P6` target select when layout requires it.

### Step 7: Bunny Catch Flow

Goal: replace scripted Bunny capture.

Required:

- `P21` ball list;
- item consumption;
- `game.d.b(itemId)` catch chance;
- tutorial force/guide behavior from source;
- `P17` catch animation/result;
- success path must preserve current event result:
  Bunny route continues and marks `state[1,0,1]` / `state[1,1,0]`.

### Step 8: Elder Battle And Sophie Battle Through Real Runtime

Goal: current story route uses the new state runtime, not the old auto loop.

Must preserve branches:

- Sophie battle result `0 -> branch 78`;
- Bunny capture success continues with result `-1`;
- elder battle result `0 -> branch 10`, then reward/state path.

### Step 9: EXP / Level-Up / Learn Skill

Goal: port source result states.

Required:

- `P8` EXP distribution;
- `P22` level up UI;
- `P23` learn skill UI;
- post-win HP/passive side effects.

### Step 10: Animation And Pixel Work

Goal: move from logic-correct to visually close.

Port script drivers:

- `pos.mid`;
- `cpos.mid`;
- `effect.mid`;
- `speffect.mid`;
- `blood.mid`;
- `bufDebuf.mid`;
- `blood_0..2` textures.

This comes after logic, because animation scripts depend on correct state,
actor slot, attacker/target, damage, buff, and result data.

## Smoke Policy

For every battle slice:

```powershell
$env:PROJECT_ROOT = "<path-to-project-root>"
$env:MODULES_ROOT = Join-Path $env:PROJECT_ROOT "modules"
$env:REBUILD_GAME = Join-Path $env:PROJECT_ROOT "rebuild_game"
cd $env:REBUILD_GAME
powershell -ExecutionPolicy Bypass -File .\build.ps1
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" com.vqsv.rebuild.Main --check
```

Required current battle smoke:

```powershell
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint battle_kidnapping "$env:REBUILD_GAME\build_intro_demo\battle_dev_kidnapping.png"
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint battle_bunny_capture "$env:REBUILD_GAME\build_intro_demo\battle_dev_bunny.png"
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint battle_elder "$env:REBUILD_GAME\build_intro_demo\battle_dev_elder.png"
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint route_sophie_after_battle_branch "$env:REBUILD_GAME\build_intro_demo\battle_dev_route_sophie.png"
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint route_bunny_after_battle_task "$env:REBUILD_GAME\build_intro_demo\battle_dev_route_bunny.png"
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp "$env:REBUILD_GAME\build\classes" VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state "$env:REBUILD_GAME\build_intro_demo\battle_dev_route_elder.png"
```

## Immediate Next Slice

Recommended next implementation slice:

```text
Step 1: create `VqsvBattleTables.java` and table regression checks.
```

Do not begin by porting the whole `game.d` state machine at once. First make
the data rows named, testable, and source-backed. Then port `BattleUnit` and
damage formula with tests before changing player-facing battle input.
