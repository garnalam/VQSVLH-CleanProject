# 97 Battle P5 Petstate UI Logic Full Audit

## Scope

Current complaint: battle pet switch UI (`petstate.ui`) is not complete in logic or visuals.

This audit is source-first and intentionally does not code a fix yet. It records:

- which source state opens the UI
- which UI file/sprite are loaded
- which widget ids are filled
- which pet data fields drive those widgets
- what input does to battle state
- what rebuild currently ports, approximates, or misses

Status key: PORTED / PORTED-PARTIAL / APPROX / STUB / PENDING / UNKNOWN.

## Source Call Chain

### Battle enters P5

Source file: `modules/source_code/decoded/decompiled_source_cfr/game/d.java`

`game.d.a(byte)` state entry:

- `case 5`: `this.S.c = 0; this.S.W();`
- `S` is `game.h`, the UI/controller helper.

Status in rebuild: PORTED-PARTIAL.

Rebuild equivalent:

- `VqsvBattleRuntime.preparePetMenu(...)`
- `enterState(... P5_PET_SWITCH ...)`
- sets `s.battleUiMode = "petstate"`

Gap: rebuild opens a custom renderer backed by generic `battleMenu*` arrays, not a real `game.h` + `petstate.ui` widget model.

### P5 update

Source file: `modules/source_code/decoded/decompiled_source_cfr/game/d.java`

Battle update:

- `case 5`: `this.S.X();`

Status in rebuild: PORTED-PARTIAL.

Rebuild equivalent:

- `VqsvBattleRuntime.tickPetSwitch(...)`
- handles confirm/back/warnings/reorder.

Gap: core validation/reorder is mostly present, but list navigation/detail refresh is not modeled as `game.h.X()` + `ao/al` widget selection.

### P5 draw

Source file: `modules/source_code/decoded/decompiled_source_cfr/game/d.java`

Battle draw:

- `case 5`: set white and draw active pet source name at screen center y=200:
  `graphics.drawString(game.d.f(aq.c[0][this.c((int)this.f[0]).q()][0]), game.d.w() >> 1, 200, 17);`

Important: the source battle draw code only adds the active pet name. The actual petstate panel is drawn by the loaded UI runtime.

Status in rebuild: PENDING/APPROX.

Gap: rebuild currently draws a custom full panel and does not separately preserve the source's extra active-pet-name draw at y=200 as an audited layer.

## Source UI Open And Fill

Source file: `modules/source_code/decoded/decompiled_source_cfr/game/h.java`

### `game.h.W()`

- resets selected list index: `this.b = 0`
- calls `this.e(this.c)`

Status in rebuild: PORTED-PARTIAL.

Gap: rebuild uses `battleMenuIndex`, but does not model `this.b`, `this.c`, `this.f` modes of `game.h`.

### `game.h.e(int i1)`

For battle owner `game.d`:

- loads `/data/ui/petstate.ui` with sprite `257`
- calls `f(i1)` to fill selected pet details
- loops six rows
- row order uses `((d)o).f[i4]`, not raw party order
- row HP widget id: `16 + i4 * 6`
- row second bar widget id: `17 + i4 * 6`
- HP text: `"#P" + pet.L()`
- second bar text: `"#P" + pet.O()`
- hides widgets `63` and `64`
- if `o.Q == 4`, widget `75` text = `"Su dung"`
- else if `o.P == 5`, widget `75` text = `"Xuat chien"`
- sets root list metadata:
  - total count `a = q.A`
  - visible count `d = q.A`
  - selected index `f = i1`
- `g = true`

Status in rebuild: PORTED-PARTIAL.

Gaps:

- Rebuild uses `sourcePets` order as if it were source `f[]`. That is acceptable only after we prove `sourcePets` has been kept in exact source `f[]` order.
- Rebuild row data only carries names/values/ids, not a source-like widget fill model.
- Rebuild does not expose six fixed UI rows with hidden/empty rows driven by `#P0`.
- Rebuild does not model root list scroll fields `a/d/e/f`.
- Rebuild has no true `petstate.ui` loader/runtime.

## Source Detail Fill

Source file: `modules/source_code/decoded/decompiled_source_cfr/game/h.java`

`game.h.f(i1)` selects the pet:

- battle owner `game.d`: calls `a(this.q.z, ((d)this.o).f[i1])`
- non-battle owner `game.k`: calls `a(this.q.z, i1)`

So battle P5 selected row index is not the pet slot directly. It is a row in `game.d.f[]`.

### `game.h.a(b[] pets, int selectedPetSlot)`

Widget fills when selected pet exists:

| Widget | Source meaning | Data source | Rebuild status |
|---:|---|---|---|
| 48 | selected pet sprite | `pet.C` loaded into sprite object mode 3 | PORTED-PARTIAL, uses `visualId` and generic draw |
| 51 | pet name | `an.f(pet.j((byte)0))` | PORTED-PARTIAL |
| 52 | pet element/type text | `an.f(365 + pet.j((byte)1))` | PENDING, rebuild mostly shows no type text |
| 62 | evolve/mutate text | `pet.j(19)` then `aq.c[0][...][2]` | PENDING |
| 61 | element relation/type string | `pet.T()` | PENDING/APPROX |
| 64 | detail action text | battle owner sets `"Xuat chien"` | Hidden later for battle by `e()`, so do not draw in battle |
| 59 | held/equip icon | sprite 258, cell `aq.c[3][pet.c[5]][1]` when `c[5] != -1` | PENDING |
| 60 | held/equip name | `an.f(aq.c[3][pet.c[5]][0])` | PENDING |
| 65 | level value | `pet.s()` | PORTED-PARTIAL |
| 66 | attack value | `pet.e((byte)2)` | PORTED-PARTIAL |
| 67 | defense value | `pet.e((byte)3)` | PORTED-PARTIAL |
| 68 | speed value | `pet.e((byte)4)` | PORTED-PARTIAL |
| 69..74 | rarity/quality stars | species rarity `aq.a(0, pet.q(), 4) - 1`, filled count from `pet.d[0]`, sprite 257 cells 14/16 | PORTED-PARTIAL, likely wrong count/source |

Important source formulas from `game.b.java`:

- `q()` = species id `V`
- `s()` = level `T`
- `j(byte)` = `aq.c[0][V][byte]`
- `L()` = current HP percent = `d[1] * 100 / c[1]`
- `O()` = EXP/next-level percent = `S * 100 / u()`
- `S()` = alive = `d[1] > 0`
- `K()` = active flag `Y`
- `T()` = element string from `aq.c[0][V][1]`
- `e(2/3/4)` = live stat values with item/status modifiers, not just base table values

Status in rebuild: PORTED-PARTIAL.

Largest gap: current P5 UI is reading `SourceBattleUnit` render fields, not a full `game.b`-like pet state with `c[]`, `d[]`, `S`, `E`, held item, current form, active flag, and source `f[]` indirection.

## Decoded `petstate.ui` Widget Map

Source layout file: `modules/ui/decoded/data__ui__petstate.ui.json`

UI file loaded by source: `/data/ui/petstate.ui`

Sprite bank: `257`

### Root/panel widgets

| Widget | Coord | Asset/cell | Meaning | Status |
|---:|---|---|---|---|
| 1 | x43 y55 w158 | alt cell 1 mode 2 | main panel frame | PORTED-PARTIAL |
| 2 | x70 y58 w100 | text | title: pet in bag | APPROX |
| 3 | x46 y79 w151 | color strip | header strip | APPROX |
| 4 | x46 y87 w151 | color fill | main body | APPROX |
| 5 | x46 y247 w151 | color fill | footer strip | APPROX |
| 7 | x107 y92 w88 | alt cell 8 mode 2 | pet detail visual backing | APPROX |
| 8 | x50 y184 w80 | alt cell 17 mode 2 | left stat/detail backing | APPROX |
| 9 | x144 y184 w47 | alt cell 15 mode 2 | level bar/backing | APPROX |
| 10 | x144 y197 w47 | alt cell 16 mode 2 | attack bar/backing | APPROX |
| 11 | x144 y210 w47 | alt cell 16 mode 2 | defense bar/backing | APPROX |
| 12 | x144 y223 w47 | alt cell 16 mode 2 | speed bar/backing | APPROX |
| 49 | x78 y78 w9 | alt cell 0 mode 3 | up arrow | APPROX |
| 50 | x76 y174 w9 | alt cell 1 mode 3 | down arrow | APPROX |
| 75 | x50 y240 w24 | text | battle action, `"Xuat chien"` in P5 | PORTED-PARTIAL |
| 76 | x164 y240 w24 | text | back | PORTED-PARTIAL |

### Detail text/widgets

| Widget | Coord | Default/source text | Source setter | Status |
|---:|---|---|---|---|
| 48 | x105 y85 w90 | pet sprite | `pet.C` | PORTED-PARTIAL |
| 51 | x53 y178 w72 | selected pet name | `an.f(pet.j(0))` | PORTED-PARTIAL |
| 52 | x53 y194 w36 | selected pet type | `an.f(365 + pet.j(1))` | PENDING |
| 53 | x53 y208 w36 | label `Tuong khac` | static | PENDING |
| 54 | x53 y224 w24 | label `Mang theo` | static | PENDING |
| 55 | x150 y178 w12 | label `lv` | static | PORTED-PARTIAL |
| 56 | x150 y190 w12 | label `Cong` | static | PENDING |
| 57 | x150 y203 w12 | label `Phong` | static | PENDING |
| 58 | x150 y216 w12 | label `Min` | static | PENDING |
| 59 | x80 y222 w14 | held item icon | sprite 258 + item icon | PENDING |
| 60 | x100 y224 w48 | held item name | item table text | PENDING |
| 61 | x100 y208 w24 | relation/type | `pet.T()` | PENDING |
| 62 | x95 y194 w36 | evolve/mutate | species evolution metadata | PENDING |
| 65 | x165 y178 w24 | level value | `pet.s()` | PORTED-PARTIAL |
| 66 | x165 y190 w24 | attack value | `pet.e(2)` | PORTED-PARTIAL |
| 67 | x165 y203 w24 | defense value | `pet.e(3)` | PORTED-PARTIAL |
| 68 | x165 y216 w24 | speed value | `pet.e(4)` | PORTED-PARTIAL |
| 69..74 | x111..167 y83/84 | star cells | source rarity/quality loop | PORTED-PARTIAL |

### Six row widgets

The UI has six fixed row containers in the root layout. They are not arbitrary text rows.

| Row | Container id | Row background id/cell | Number id | Icon id/cell | HP id | EXP id | Absolute y |
|---:|---:|---|---:|---|---:|---:|---:|
| 0 | 13 | 6, cell 9/10 | 14 | 15, cell 18 | 16 | 17 | 86 |
| 1 | 18 | 19, cell 9/10 | 21 | 20, cell 18 | 22 | 23 | 101 |
| 2 | 24 | 25, cell 9/10 | 27 | 26, cell 18 | 28 | 29 | 116 |
| 3 | 30 | 31, cell 9/10 | 33 | 32, cell 18 | 34 | 35 | 131 |
| 4 | 36 | 37, cell 9/10 | 39 | 38, cell 18 | 40 | 41 | 146 |
| 5 | 42 | 43, cell 9/10 | 45 | 44, cell 18 | 46 | 47 | 161 |

Row fill source:

- if row maps to a valid `game.d.f[row]` and `q.z[f[row]] != null`:
  - HP id text = `#P` + `pet.L()`
  - EXP id text = `#P` + `pet.O()`
- otherwise:
  - HP id text = `#P0`
  - EXP id text = `#P0`

Status in rebuild: PORTED-PARTIAL/APPROX.

Gaps:

- rebuild draws a row icon based on element color, but source default row icon is sprite 257 cell 18 unless the UI runtime changes it through selection/skin.
- rebuild draws HP and a hardcoded empty second bar; source fills both HP and EXP percent.
- rebuild uses visible count = `battleMenuNames.length`; source has six row slots and fills missing rows with `#P0`.
- selected row background/source cell behavior is not proven. Current use of cell 10 vs 9 is an approximation.

## Source Input And Side Effects

Source file: `modules/source_code/decoded/decompiled_source_cfr/game/h.java`

`game.h.X()` when `f == 0`:

- up/down input calls `this.p.a.b(0/1)`, moving selected widget/list row
- confirm calls `((d)this.o).a(this.b)` for battle owner
- back in battle owner:
  - if `Q == 7 || Q == 13`, return
  - unloads `/data/ui/petstate.ui`
  - sets `game.d.a().k = false`
  - `this.a = 0`
  - `this.o.a((byte)20)`

### Confirm results from `game.d.a(int slot)`

Source file: `modules/source_code/decoded/decompiled_source_cfr/game/d.java`

| Result | Condition | UI/result |
|---:|---|---|
| 0 | `!this.c(this.f[slot]).S()` | warning: pet cannot battle |
| 1 | `this.c(this.f[slot]).K()` | warning: pet already active |
| -1 | valid | reorder `f[slot]` to `f[0]`, mark active, clear old battle target/effects, then state P15 |

Valid switch side effects:

- move selected `f[slot]` to front
- add selected pet to battle vector `x` if missing
- `this.c(f[0]).J = true`
- `this.c(f[0]).d(true)` so `K()` active flag becomes true
- `this.h.d(false)`
- `this.h.F = 0`
- clears effects targeting previous `h`
- `game.h.X()` then calls:
  - `((d)o).a((int)((d)o).g, 0)`
  - `this.a = 0`
  - `this.o.a((byte)15)`
  - unloads/reloads `petsetting.ui` and `petstate.ui`

Status in rebuild: PORTED-PARTIAL.

Good:

- dead/current validation exists.
- valid switch reorders `s.sourcePets` and enters P15.
- active battle pet is persisted before reorder.

Gaps:

- no explicit source `K()/Y` active flag model per pet.
- no battle vector `x` equivalent beyond active `player`.
- no exact `game.h` UI reload/unload sequencing.
- forced-switch P5 list filtering may differ from source if source still shows dead/current rows and validates on confirm in some paths. This needs route-specific proof before changing.

## Current Rebuild Status

Files:

- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleRenderer.java`
- `rebuild_game/src/main/java/VqsvSourceModels.java`
- `rebuild_game/src/main/java/VqsvBattleUnit.java`

### Runtime

`preparePetMenu(...)`:

- builds generic `ids/names/values`
- values are `"lvX hp/max"` or `KO`
- assigns via `setMenu(...)`
- then sets `battleUiMode = "petstate"`

Status: PORTED-PARTIAL.

Problem: this cannot reproduce source `petstate.ui` because P5 needs source widget fields, not generic text rows.

### Renderer

`drawPetStateOverlay(...)` currently:

- draws custom fills approximating panel
- draws six-ish rows only from available menu entries
- draws custom element circles
- draws selected pet sprite
- draws custom HP/stat bars
- draws only name/level clearly

Status: APPROX/PORTED-PARTIAL.

Problems:

- not a full `/data/ui/petstate.ui` renderer.
- several widget ids and asset cells are guessed.
- missing source detail fields: type text, relation text, evolution/mutation, held item icon/name, exact labels/values.
- second row bar is hardcoded 0 instead of source `pet.O()`.
- star fill uses `unit.nature` as quality-like count, but source uses `pet.d[0]` plus species rarity.
- row order and row count need to be modeled from `game.d.f[]`.

## Required Fix Direction

Do not patch by eye. Recommended next code work should be:

1. Add a P5-specific view model, not more generic `battleMenuValues`.
   - fields: row source index, pet slot/f index, species, name, level, current hp/max hp, hpPercent, expPercent, attack, defense, speed, element id/text, relation text, evolution text, held item id/icon/name, visual sprite id, quality, rarity, alive, active.
   - status target: PORTED-PARTIAL.

2. Fill that view model in `VqsvBattleRuntime.preparePetMenu(...)` from `SourcePetState`/`BattleUnit`.
   - keep current behavior unless source proves otherwise.
   - record source limitations explicitly where `game.b` fields are missing.

3. Render from decoded widget coordinates.
   - draw root/panel cells from sprite 257 where available.
   - draw six fixed rows.
   - fill HP and EXP bars from source-like percentages.
   - draw detail labels and values at widget coords.
   - draw buttons `75/76`; do not draw hidden `63/64` in battle P5.

4. Keep runtime side effects separate.
   - switch validation/reorder remains in P5 runtime.
   - UI renderer must not mutate battle state.

5. Add focused PNG smoke.
   - `battle_elder_pet_p5_petstate_ui`
   - `battle_bunny_caught_pet_p5_low_hp`
   - `battle_p5_click_reserve_success`
   - `battle_p5_current_warning`
   - `battle_p5_dead_warning`

## Verification Required After Code

After the actual fix, run:

- `powershell -ExecutionPolicy Bypass -File .\build.ps1`
- `java "-Dvqsv.modules=..\modules" -jar .\build\libs\vqsv-rebuild-skeleton.jar --check`
- `java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvBattleDamageFormulaCheck`
- `git diff --check`
- `rg -n "Ã|Â|�" rebuild_game/src/main/java -g "*.java"`
- focused smoke PNG for P5
- route regression PNG:
  - `route_bunny_after_battle_task`
  - `route_elder_after_battle_reward_state`
  - any Bunny caught low-HP P5 checkpoint

## Honest Current Conclusion

Current rebuild P5 petstate is not complete.

Classification:

- UI open/state: PORTED-PARTIAL
- switch validation/reorder: PORTED-PARTIAL
- source pet HP persistence into P5: PORTED
- `petstate.ui` widget runtime: PENDING
- current visual renderer: APPROX/PORTED-PARTIAL
- selected pet detail fields: PORTED-PARTIAL with several PENDING fields
- row HP percent: PORTED-PARTIAL
- row EXP percent: PENDING
- stars/rarity: PORTED-PARTIAL but not fully source-proven
- held item/equip display: PENDING
- pixel-perfect parity: UNKNOWN/PENDING

Next safe slice: implement the P5-specific source-shaped view model and redraw the overlay from this widget map, then smoke PNG. Do not claim pixel-perfect until original-vs-rebuild pixel compare exists.

## Implementation Slice 2026-07-08

Implemented:

- added `VqsvBattlePetStateView` as P5-specific source-shaped view model
- added `Scene.battlePetStateRows`
- `VqsvBattleRuntime.preparePetMenu()` now fills six P5 row views from current `battleMenuIds`
- `VqsvBattleRenderer.drawPetStateOverlay()` now renders from decoded `petstate.ui` coordinates instead of generic menu values
- row HP uses source-like `L()` percent from current HP/max HP
- row second bar uses source-like `O()` EXP percent from payload exp/next-level formula
- detail panel now shows name, element text, evolution text, relation text, held item icon/name, level, attack, defense, speed, and quality stars where available
- P5 render now suppresses normal battle HUD/command bar because source `game.d` case 5 does not call the battle HUD renderer

Current classification after this slice:

- P5 view model: PORTED-PARTIAL
- P5 switch validation/reorder: PORTED-PARTIAL, unchanged
- P5 HP persistence/low-HP Bunny row: PORTED
- `petstate.ui` renderer: PORTED-PARTIAL, source-backed by decoded widget coords
- full `ao/al` UI runtime: PENDING
- exact selection sprite/cell behavior: APPROX
- forced-switch source row filtering parity: UNKNOWN/PENDING route-specific audit
- pixel-perfect parity: UNKNOWN/PENDING

Smoke PNG outputs:

- `rebuild_game/build/smoke/battle_elder_pet_p5_petstate_viewmodel_v2.png`
- `rebuild_game/build/smoke/battle_bunny_caught_pet_p5_low_hp_viewmodel_v2.png`
- `rebuild_game/build/smoke/battle_p5_click_reserve_success_viewmodel_v2.png`
- `rebuild_game/build/smoke/battle_p5_current_warning_viewmodel_v2.png`
- `rebuild_game/build/smoke/battle_p5_dead_warning_viewmodel_v2.png`
- `rebuild_game/build/smoke/route_bunny_after_battle_task_p5_viewmodel_v2_regression.png`
- `rebuild_game/build/smoke/route_elder_after_battle_reward_state_p5_viewmodel_v2_regression.png`
- `rebuild_game/build/smoke/route_sophie_after_battle_branch_p5_viewmodel_v2_regression.png`
