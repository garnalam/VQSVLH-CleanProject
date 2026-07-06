# Battle Logic + Asset Full Audit

Date: 2026-07-06

Purpose: chốt lại toàn bộ hiểu biết battle trước khi port tiếp. Battle không
được làm theo kiểu nhìn UI rồi vẽ lại. Mạch đúng phải là:

```text
event opcode -> game.d state -> game.b unit/stat/skill/status -> game.h UI
-> sprite/effect/script asset -> result/branch/save/world return
```

Nếu một phần chưa chứng minh được bằng source hoặc asset, trạng thái phải là
`PARTIAL`, `PENDING`, hoặc `UNKNOWN`; không được gọi là xong.

## 1. Source Files And Assets

Primary source files:

| Source | Vai trò thật | Current audit |
|---|---|---|
| `modules/source_code/decoded/decompiled_source_cfr/game/d.java` | Battle state machine, turn dispatch, command state, skill execute, catch, win/lose/EXP/level-up return world. | VERIFIED source, PARTIAL port |
| `modules/source_code/decoded/decompiled_source_cfr/game/b.java` | Battle unit/pet/monster model: `c[]`, `d[]`, skill ids/PP, buff/debuff, damage formula, item effect, payload `P()`. | VERIFIED source, PORTED/PARTIAL |
| `modules/source_code/decoded/decompiled_source_cfr/game/h.java` | Battle UI facade: `battle.ui`, `choiceskill.ui`, `choice.ui`, `msgwarm.ui`, `levelUp.ui`, HP/name/status widgets, command input. | VERIFIED source, PARTIAL port |
| `modules/source_code/decoded/decompiled_source_cfr/game/g.java` | Player save/inventory/pet bag/bank, battle passive checks, item operations. | VERIFIED source, PARTIAL port |
| `modules/source_code/decoded/decompiled_source_cfr/f.java`, root `d.java`, `ah.java`, `l.java`, `e.java` | Sprite entity, animator, RGB effect pipeline. | VERIFIED/PARTIAL pixel |
| `modules/source_code/decoded/decompiled_source_cfr/game/c.java` | Event opcode `37/32/47/52/67` starts battle and consumes result branch. | VERIFIED/PARTIAL route |

Primary data/assets:

| Asset | Ý nghĩa | Current audit |
|---|---|---|
| `modules/script/original/db.mid` / decoded `data__script__db.mid.json` | `aq.c[0..8]`: species, skills, passive, status, items, rewards, buffs, debuffs, learn thresholds. | PORTED table wrappers |
| `modules/script/original/sprite.mid` | Sprite index -> `sprId,imgIds`; battle sprite ids come from here and `aq.c[0][species][17]`. | PORTED through source sprite table loader |
| `modules/spr/original/spr_*_all(r)` + `modules/img/decoded/*.png` | Sprite frame/cell/anim/image resources. | PORTED renderer, PARTIAL pixel parity |
| `modules/ui/original/battle.ui` / decoded json | Main battle HUD and six command slots. | Source-shaped renderer only |
| `modules/ui/original/choiceskill.ui` | Skill list UI, PP display, description. | PENDING |
| `modules/ui/original/choice.ui` | Generic list UI used by capture balls and battle items. | PARTIAL |
| `modules/ui/original/msgwarm.ui` | Warning popup. | PARTIAL |
| `modules/ui/original/levelUp.ui` | EXP/level-up/new skill UI. | PENDING |
| `modules/script/original/pos.mid`, `cpos.mid`, `effect.mid`, `speffect.mid`, `blood.mid`, `bufDebuf.mid` | Battle animation/effect timing tables. | PENDING |

## 2. Source Entry Chain Into Battle

Known story battle entry points currently used in rebuild:

| Story slice | Event source | Source setup | Rebuild status |
|---|---|---|---|
| Sophie/kidnapping battle | scene_1 room3 group0 branch before record 78 | battle actor around `56`, encounter `[5,20,4]`, branch target `78` on result `0`. | PORTED/PARTIAL route smoke |
| Bunny tutorial capture | scene_1 room1 group0 | op37 encounter `[34,5,1]`, op52 flags `[0,1]`, op32 mode `[0,0]`, op47 branch `[12,0,0]`; success capture route returns result `-1`. | PORTED/PARTIAL |
| Elder battle | scene_1 room0 group6 | op67 actor `52`, op37 encounter `[68,5,1]`, op32 mode `[0,2]`, op47 branch `[10,10,0]`, then rewards and free-world unlock. | PORTED/PARTIAL |

Important rule:

- Do not start battle by guessing. The event must prove actor id, encounter row,
  battle flags/mode, and op47 branch target.
- Current rebuild uses `SourceBattleRuntime` manually from these proven values.
  That preserves branch smoke, but it is not yet a bytecode-equivalent `game.d`
  runtime.

## 3. Source Battle State Machine

Source state field: `game.d.P`.

Observed source state graph:

```text
P0 battle entry / first actor
  -> P1 turn dispatch
     -> P20 player command if active player unit needs input
        -> P3 skill list
        -> P21 catch ball list
        -> P4 item list
        -> P5 pet switch
        -> P11 shop/points/info branch
        -> P10 run
     -> P2 select/AI choose skill/target
     -> P7 execute skill animation/damage/effects
     -> P12/P13 pre-turn buff/debuff effect queues
  -> P8 win / EXP flow
     -> P22 level-up UI
     -> P23 learn skill UI
  -> P9 lose
     -> P24 revive/lose return variant
  -> P17 catch animation/result
  -> P16 item target/use
  -> P101/P102/P104 special/tutorial/body-shop/SMS-ish branches
```

Current rebuild state enum:

`P0_ENTRY, P20_COMMAND, P3_SKILL_LIST, P21_CATCH_LIST, P17_CATCH_RESULT,
P4_ITEM_LIST, P16_ITEM_TARGET, P5_PET_SWITCH, P11_SHOP, P10_RUN, WARNING,
P2_SELECT_EXECUTE, P7_RESOLVE, P1_DISPATCH, P8_WIN, P9_LOSE, EXIT_FADE, DONE`.

Status:

| Source state/group | Rebuild status | Truth |
|---|---|---|
| P0/P1/P20 basic route | PORTED/PARTIAL | Has explicit phases and command routing, but turn order is simplified. |
| P3 skill list | STUB/PENDING | Current code immediately selects/executes; no `choiceskill.ui`, PP, skill desc, target side. |
| P2/P7 damage | PORTED/PARTIAL | Uses `BattleUnit.computeDamage`, but animation/timing/effect scripts are not driving visuals. |
| P21/P17 catch | PORTED/PARTIAL | Best-covered flow: ball list, consume, formula shape, sprite 269, type 8 effect, storage. |
| P4/P16 item | PARTIAL | Opens source-shaped list and applies a few item behavior paths; not full `game.b.w/x` validation. |
| P5 pet switch | PARTIAL | Can list/switch non-active pet; missing source restrictions and UI parity. |
| P10 run | PARTIAL | Has story-block warning and speed-shaped chance; world integration for free battles incomplete. |
| P11 shop/info | PARTIAL | Has shell buy flow; not full source `shopbuy.ui` quantity/category runtime. |
| P8/P22/P23 EXP/level-up | PENDING | Not ported. |
| P9/P24 lose/revive | PARTIAL/PENDING | Basic lose branch exists; source revive/return variants not complete. |
| P12/P13 buff/debuff queues | PENDING | Tables understood, runtime queue visuals/ticks not complete. |
| P101/P102/P104 | PENDING | Not part of current battle path except noted tutorial/special branches. |

## 4. Unit/Stat/Skill Model

Source `game.b` fields:

| Source | Meaning | Rebuild |
|---|---|---|
| `c[23]` | base stats; `c[1]` max HP, `c[2]` atk, `c[3]` def, `c[4]` speed, `c[5]` form/status, `c[6]` side flag. | PORTED as `baseStats` |
| `d[23]` | current/mutable stats; `d[1]` current HP. | PORTED as `currentStats` |
| `z[5]` | skill ids. | PORTED |
| `y[5]` | current PP/use count. | PORTED/PARTIAL |
| `v[16][5]` | self/buff slots. | PORTED structure, PARTIAL runtime |
| `w[11][5]` | target/debuff slots. | PORTED structure, PARTIAL runtime |
| `x[2][3]`, `N[2]` | active effect queue/count. | PORTED structure, PENDING runtime |
| `P()` | serialized pet payload after catch. | PORTED/PARTIAL shape in `SourcePetState.sourcePayload` |

Stat source:

- Species table: `aq.c[0]`.
- HP formula: `aq.c[0][species][5] + [6] * level + [7]`, multiplied by quality/nature multiplier.
- Attack formula: `[8] + [9] * level + [10]`, multiplied by quality/nature.
- Defense formula: `[11] + [12] * level / 10 + [13]`, multiplied by quality/nature.
- Speed formula: `[14] + [15] * level / 10 + [16]`, multiplied by quality/nature.
- Visual sprite index: `aq.c[0][species][17]`.
- Learn group: `aq.c[0][species][18]`.
- Relation/catch class: `aq.c[0][species][22]`.

Current truth:

- `VqsvBattleTables` reads `db.mid` groups and wraps species/skill/status/item/buff/debuff rows.
- `VqsvBattleUnit` mirrors source arrays and has a bytecode-shaped damage formula port.
- Full global passive wiring from `game.g/game.k` is not complete.
- Full caught pet parity cannot be called done until the real `game.g` pet bag/bank/save model is ported.

## 5. UI Matrix: Source Calls vs Rebuild

| Flow | Source method | UI file | Key widgets/data | Rebuild status |
|---|---|---|---|---|
| Main battle HUD | `game.h.a(b,b)` and update helpers | `battle.ui` | ids `1/2/57` panels, commands `3..8`, cursor overlay `20..25`, HP/name/status ids around `9..19`, `38..42`, `55..59`. | PARTIAL source-shaped renderer |
| Command input | `game.h.d(b)` | `battle.ui` | left/right changes selected command; action routes to states. | PORTED/PARTIAL, click/key works |
| Skill list | `game.h.e(b)`, `h(b)`, `f(b)` | `choiceskill.ui` | rows `13+5*i`, `14+5*i`, desc `53`, PP count. | PENDING |
| Catch ball list | `game.h.ah()`, `ai()` | `choice.ui` | title id `8`, column id `9`, action id `5`, icon ids `54+i`, row ids `13+5*i`, `14+5*i`, desc/count id `53`. | PORTED/PARTIAL |
| Battle item list | `game.h.aj()`, `ak()`, `bo()` | `choice.ui`, `msgwarm.ui` | item rows from bag vector, behavior `aq.c[4][id][5]`. | PARTIAL |
| Pet switch | `game.h.W/X` related battle branches | `petstate.ui`/battle UI path | active pet list/status. | PARTIAL |
| Warning | many `game.h` branches | `msgwarm.ui` | prompt text and close. | PARTIAL |
| Level up/learn skill | `game.h.an/ao/ap/aq` | `levelUp.ui`, `choiceskill.ui` | stat changes, new skill choice. | PENDING |
| Shop/points | `game.h.a(byte,byte)` style branches | `shopbuy.ui` | category, price, quantity/money. | PARTIAL shell |

Important UI truth:

- Current battle UI is not a generic `game.h` widget runtime. It is a
  battle-specific renderer using source cell ids and decoded coordinates.
- This is acceptable for smoke progress, but not enough for “100% original UI”.
- Next UI work must follow the source call (`game.h` method) first, then render
  the UI file it opens. Do not port a UI file just because it exists.

## 6. Animation/Effect Assets

Current source-backed assets already used:

| Asset | Used by | Rebuild status |
|---|---|---|
| `sprite.mid` source table | All sprite indexes. | PORTED loader |
| `spr_269_all(r)` + image `309` | P17 capture ball animation. | PORTED/PARTIAL pixel |
| sprite `257` cells | battle/ui panels/buttons/cursor. | PORTED/PARTIAL |
| sprite `258` cells | item/ball icons in `choice.ui`. | PORTED/PARTIAL |
| sprite `325` and UI cell `145` | status icon slots. | PARTIAL |

Pending battle animation tables:

| Asset | Expected role | Status |
|---|---|---|
| `pos.mid` | Unit battle positions / movement positions. | PENDING |
| `cpos.mid` | Camera/combat position variants. | PENDING |
| `effect.mid` | Skill animation/effect table. | PENDING |
| `speffect.mid` | Special skill/effect table. | PENDING |
| `blood.mid` | Damage/blood number/effect table. | PENDING |
| `bufDebuf.mid` | Buff/debuff visual effect table. | PENDING |

P17 current detail:

- Source `game.d.e(byte)` q1/q4 arrays are ported:
  - q1: scale/offset `(10,0,0) -> (7,0,-10) -> (4,0,-20)`, duration `9`.
  - q4: scale/offset `(4,0,-20) -> (6,0,-12) -> (8,0,-4) -> (10,0,0)`, duration `8`.
- Rebuild now models `ah` type 8 counters and draws a cloned target sprite cell
  with nearest scaling and RGB brighten.
- Still not byte-for-byte MIDP `drawRGB` parity because root `l/e` image buffer
  pipeline is not fully recreated.

## 7. What Has Been Done

PORTED:

- Battle table loader/wrappers for `db.mid`.
- `BattleUnit` source-shaped arrays and factories.
- Direct damage formula regression through `VqsvBattleDamageFormulaCheck`.
- Three current story battle entries and branch smoke:
  Sophie -> branch 78, Bunny -> return task, elder -> reward/free-world.
- Main battle state shell with explicit labels and route checkpoints.
- Command bar selection/click/key for six commands.
- P21/P17 catch flow:
  ball list, icon/name/chance, item consume, source chance formula shape,
  sprite 269 animation, type 8 effect shape, bag/bank/full storage.
- Source-shaped HP/name/level/element relation HUD.
- Smoke checkpoints for command/catch/item/pet/shop/run/warnings and routes.

PARTIAL:

- Battle UI is source-shaped, not generic `game.h`.
- Skill command currently lacks real `choiceskill.ui` and PP/target semantics.
- Item behavior table only has a small subset.
- Pet switch lacks full source restrictions/status/death behavior.
- Run has story-block and rough chance, not full free-world return parity.
- Status/buff/debuff structures exist but queue/tick/visual runtime incomplete.
- Catch payload shape exists, but full `game.g` save/global parity pending.
- P17 effect closer than before, but not pixel-perfect MIDP.

PENDING:

- Generic UI widget runtime or exact battle-specific equivalent for:
  `battle.ui`, `choiceskill.ui`, `choice.ui`, `msgwarm.ui`, `levelUp.ui`,
  `shopbuy.ui`.
- Full skill select -> target select -> animation -> damage -> status apply.
- Battle animation table mapping (`pos/cpos/effect/speffect/blood/bufDebuf`).
- EXP, level-up, skill learn, post-battle passive heal.
- Full `game.g` inventory/pet bag/bank/save behavior.
- Full AI target/skill selection and turn order parity.
- Pixel/timing compare against original battle screenshots/video.

UNKNOWN / needs audit before coding:

- Exact semantics of every row in `pos/cpos/effect/speffect/blood/bufDebuf`.
- Exact generic `game.h` widget focus/scroll behavior across every battle UI.
- Exact source values for global passive hooks in current rebuild save state.
- Whether story battle mode flags in op32 alter catch/run/item availability in
  more ways than currently ported.

## 8. Recommended Next Work

Do not jump to “all battle 100%” at once. The safest sequence:

### Step A: Battle Entry Snapshot

Goal: when entering each of the three known battles, dump/verify:

- event source values: actor, encounter, flags, mode, branch targets;
- player unit source pet payload;
- enemy `BattleUnit` stats from `aq.c[0]`;
- sprite indexes and image rows;
- battle UI file opened by source.

Output:

- `53_battle_entry_snapshot_matrix.md`
- smoke PNG: command UI for Sophie, Bunny, elder.

### Step B: Real Skill List P3

Goal: port only `P3` first.

Source to follow:

- `game.d case 3`
- `game.h.e(b)`, `game.h.h(b)`, `game.h.f(b)`
- `choiceskill.ui`
- `aq.c[1]` skill name/desc/PP/target side.

Must prove:

- skill rows are from active player unit `z[]/y[]`;
- disabled/no PP warning uses `msgwarm.ui`;
- back returns to P20;
- confirm sets selected skill and moves to the correct next state.

### Step C: Target Select P6/P2

Goal: after choosing a skill, source target list must be built correctly.

Need:

- target side from `aq.c[1][skill][9]`;
- target vectors `h.G/H`;
- cursor/target display from battle UI helpers;
- AI equivalent for enemies can remain partial until player path is correct.

### Step D: Skill Resolve P7 Animation Slice

Goal: one simple direct-damage skill should run with source animation tables.

Need:

- map one skill id to `effect/speffect/pos/cpos/blood/bufDebuf`;
- drive `f/ah` effects from source tables;
- HP bar animation through `game.h.a/b(b,boolean)` shape.

### Step E: Status/Buff Tick

Goal: close P12/P13, effect queues `x/N`, and visual status icons.

Need:

- apply/tick `v/w`;
- source status icon rendering with sprite `325` and overlay cells;
- verify damage/turn changes.

### Step F: EXP/Level-Up

Goal: after win, port P8/P22/P23.

Need:

- EXP formula and participant list `j`;
- `levelUp.ui`;
- learn skill choice via `choiceskill.ui`;
- save pet payload updates.

## 9. Smoke Plan For Every Battle Change

Minimum commands after touching battle:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" com.vqsv.rebuild.Main --check
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvBattleDamageFormulaCheck
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke-checkpoint route_sophie_after_battle_branch ".\build_intro_demo\route_sophie_after_battle.png"
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke-checkpoint route_bunny_after_battle_task ".\build_intro_demo\route_bunny_after_battle.png"
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state ".\build_intro_demo\route_elder_after_battle.png"
```

For UI slices:

```powershell
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke-checkpoint battle_bunny_command_ui ".\build_intro_demo\battle_bunny_command_ui.png"
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke-checkpoint battle_bunny_catch_p21 ".\build_intro_demo\battle_bunny_catch_p21.png"
java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke-checkpoint battle_bunny_catch_p17_anim_or_result ".\build_intro_demo\battle_bunny_p17.png"
```

Rule: if a smoke text line in terminal is mojibake but PNG/source strings are
correct, classify as console encoding issue. If PNG has mojibake, fix text
source before touching battle logic.

## 10. Final Current Truth

Battle is no longer just an empty visible stub. It has a real source-backed
foundation: tables, unit model, damage formula, command shell, and catch flow.

But it is not a full original battle engine yet.

The largest missing pieces are:

1. real skill list/target select from `choiceskill.ui`;
2. source animation/effect table runtime for P7;
3. full buff/debuff/status tick and icons;
4. EXP/level-up/learn skill;
5. generic or equivalent `game.h` widget parity;
6. full `game.g` save/inventory/pet parity.

Next safe coding target: Step A battle entry snapshot, then Step B real P3
skill list. Do not start P7 animations before P3/P6 are source-backed.
