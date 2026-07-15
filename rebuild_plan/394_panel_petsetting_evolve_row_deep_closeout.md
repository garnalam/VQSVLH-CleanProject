# 394 - Petsetting Tien Hoa Row Deep Closeout

Ngay: 2026-07-15

Pham vi: tab **Sung vat** -> `petsetting.ui` -> row `5 Tien hoa / Di hoa`.

Luat lam viec: audit source truoc, chi smoke/headless PNG. Khong mo live client.

## Source Path

| Source | Vai tro | Ket luan |
| --- | --- | --- |
| `game.k.bl()` | Mo `/data/ui/evolve.ui`, dong `petsetting.ui` + `petstate.ui`, bind pet/material/stat widgets | PORTED/PARTIAL |
| `game.k.bp()` | Confirm/back trong `evolve.ui`, validate target/level/material, start evolution effect, mutate pet, warning flow | PORTED/PARTIAL |
| `aq.c[0][species][19]` | Target species evolution/mutation | PORTED |
| `aq.c[0][species][20] + 12` | Material id can dung | PORTED |
| `aq.c[0][species][21]` | Material count can dung | PORTED |
| `aq.c[0][target][2]` + `game.i.u` | Required level: `[12, 30, 5]` theo target kind | PORTED |
| `aq.c[0][species][17]` | Pet visual id cho current/target sprite | PORTED/PARTIAL |
| `/data/ui/evolve.ui` | Widget positions/styles cho man tien hoa | PORTED/PARTIAL source-shaped renderer |

## Source Behavior Matrix

| Case | Source behavior | Rebuild behavior | Status |
| --- | --- | --- | --- |
| Open row 5 | `f=2`, `r=0`, open `evolve.ui`, close `petsetting.ui` + `petstate.ui` | `sourceEvolveVisible=true`, parent overlays closed | PORTED |
| Header/current pet | widget `10` sprite, `38` name, `40` level | Uses selected `SourcePetState` and species row | PORTED/PARTIAL |
| Material | widget `45` material name, `46` count/need from q inventory family | Uses `sourceSpecialRewards` count and `BattleItemRow` name | PORTED/PARTIAL |
| Stats | widgets `19..22` current stats, `31..34` target stats | Uses `BattleUnit.sourceVisibleStats()` for current and target clone | PORTED/PARTIAL |
| No target | `s8 == -1` -> warning `Khong the lai tien hoa hoac di hoa` | Warning shown, no mutate, continue returns `evolve.ui` | PORTED |
| Level low | pet level < required level -> warning | Warning shown, no material consume, no mutate, continue returns `evolve.ui` | PORTED |
| Missing material | material count < need -> warning evolution/mutation specific text | Warning shown, no material consume, no mutate, continue returns `evolve.ui` | PORTED |
| Success start | hide widget 10, create `game.l.x = new a.a.b()` with type10 row, consume material | Starts source-shaped phase 1 and consumes material once | PORTED/PARTIAL |
| Success commit | after effect complete, rebuild target pet payload/stat/name and show success `msgwarm.ui` | Mutates species, preserves EXP, refreshes panel, shows success text | PORTED/PARTIAL |
| Continue after success | source closes only `msgwarm.ui`, returns to `evolve.ui` | Success message confirm keeps evolved pet visible in `evolve.ui` | PORTED |
| Back | `f < 3` + back closes `evolve.ui`, refreshes `petstate.ui` state | Closes evolve overlay and resets evolution tutorial markers | PORTED |

## UI Widget Binding

Decoded from `modules/ui/decoded/data__ui__evolve.ui.json`.

| Widget | Source meaning | Rebuild mapping |
| --- | --- | --- |
| `5` | Title `Tien hoa` | Center title from evolution/mutation kind |
| `6` | Right softkey `Phan hoi` | Back/close text |
| `7` | Left softkey `Xac dinh` | Confirm text |
| `10` | Current pet sprite slot | `sourceEvolveOldVisualId` sprite |
| `38` | Current pet name | Current species name |
| `40` | Current pet level | Selected pet level |
| `45` | Material name | `VqsvSourceEvolutionRuntime.materialName()` |
| `46` | Material count/need | `materialCount/materialNeed` |
| `19..22` | Current visible stats | `visibleStats(current pet)` |
| `31..34` | Target visible stats | `targetVisibleStats(target species)` |

## Evolution Data Rule

For a selected pet species:

```text
targetSpecies = aq.c[0][species][19]
materialId    = aq.c[0][species][20] + 12
materialNeed  = aq.c[0][species][21]
targetKind    = aq.c[0][targetSpecies][2]
requiredLevel = game.i.u[targetKind - 1]
game.i.u      = [12, 30, 5]
```

If `targetSpecies == -1`, the UI must hide target/material junk and warn on confirm instead of mutating.

## Mutation Payload Rule

Source `game.k.bp()` does not add a temporary bonus. It builds a new `game.i`
pet object from the target species and the same level, then copies important
runtime state:

```text
newPet.a(targetSpecies, oldLevel, oldHeld, oldSideByte, oldQualityOrRandom, -1)
newPet.a(newPet.d[1], oldExp, oldF)
newPet.b(oldPet.R())
oldPet.a(newPet.Q())
```

Meaning for rebuild:

| Field | Expected after evolution |
| --- | --- |
| species/name/sprite | target species |
| visible stats | recalculated from target species + same level/quality/nature |
| HP | full target max HP, because source passes `newPet.d[1]` into `a(hp, exp, F)` |
| EXP | preserved |
| skills/PP | preserved through `R()` / `b(...)` copy |
| held/form slot | preserved |
| material | consumed once before success animation completes |

The latest smoke hardens this: success now seeds held/form payload, non-zero EXP,
and a second skill with custom PP, then asserts all of them after mutation.

## Smoke Coverage

Focused panel suite:

```powershell
cd C:\Users\Dell\Downloads\ResourcesVQSV\rebuild_game
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-suite panel_pet_tab .\build_intro_demo\panel_pet_tab_394
```

Result: PASS `39/39`.

Evolution row PNGs:

| Checkpoint | Chung minh |
| --- | --- |
| `panel_petstate_petsetting_evolve_open` | row 5 direct route opens `evolve.ui`, closes parent overlays |
| `panel_petstate_petsetting_evolve_no_material_warning` | missing material warning, no mutation |
| `panel_petstate_petsetting_evolve_success_mutate` | material consumed once, species `6 -> 7`, target stats applied, HP becomes new max, EXP/skills/PP/held payload preserved, success msgwarm |

World/evolution edge checkpoints:

Output directory: `rebuild_game/build_intro_demo/evolve_row_394_world`

| Checkpoint | Status |
| --- | --- |
| `world_evolution_evolve_ui_open` | PASS |
| `world_evolution_confirm_success_mutate` | PASS |
| `world_evolution_after_success_continue` | PASS |
| `world_evolution_confirm_no_material` | PASS |
| `world_evolution_confirm_no_material_after_warning_continue` | PASS |
| `world_evolution_confirm_level_low` | PASS |
| `world_evolution_confirm_level_low_after_warning_continue` | PASS |
| `world_evolution_no_next_target_warning` | PASS |
| `world_evolution_no_next_target_after_warning_continue` | PASS |
| `world_evolution_back_from_evolve_ui` | PASS |

Build/check:

| Command | Result |
| --- | --- |
| `.\build.ps1` | PASS |
| `java "-Dvqsv.modules=..\modules" -jar .\build\libs\vqsv-liet-hoa-rebuild.jar --check` | PASS |

Extra logic recheck after payload hardening:

Output directory: `rebuild_game/build_intro_demo/evolve_logic_recheck_394`

| Checkpoint group | Result |
| --- | --- |
| panel open/no-material/success mutate | PASS |
| world success/continue | PASS |
| world warning continue: no material, level low, no target | PASS |
| world back from evolve UI | PASS |

## Current Status

Row `5 Tien hoa / Di hoa` hien dat **PORTED/PARTIAL**:

- Source route from `petsetting.ui` is represented.
- Target/material/level validation is source-backed.
- Material consume and pet mutation payload are source-backed for species/stat/HP/EXP/skill/PP/held preservation.
- Warning flow and continue/back lifecycle are covered by smoke.
- UI is rendered from decoded `evolve.ui` coordinates in a source-shaped renderer.

## Remaining Debt

| Debt | Ghi chu |
| --- | --- |
| Exact `a.a.b` / `ah type10` evolution animation | Current renderer intentionally avoids fake overlay. Source row uses type10 with alpha/overlay behavior that still needs exact Java ME `drawRGB` parity before claiming pixel-perfect effect. |
| Full Java ME widget VM | Renderer is source-shaped, not full `game.h/game.k` widget object runtime. |
| Original-vs-rebuild pixel compare | Not claimed. Current proof is source-backed lifecycle + smoke PNG asserts. |
| Exact source `Q()` payload index naming | Rebuild keeps its current payload convention for compatibility. Evolution now preserves the source-important semantics, but a full save-object/index audit is wider save-system work. |

## Next Roadmap Step

Create compact all-row closeout for `petsetting.ui` rows `0..5`, then leave tab **Sung vat** unless a visible bug appears in manual play.
