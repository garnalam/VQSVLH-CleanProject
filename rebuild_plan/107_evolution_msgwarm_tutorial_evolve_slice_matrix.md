# 107 Evolution Msgwarm Tutorial Evolve Slice Matrix

Status: IMPLEMENTED SOURCE-SHAPED, WITH PIXEL-PARITY CAVEATS.

Scope:

- Finish the path after battle level-up evolution queue notice.
- Source files:
  - `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
  - `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- UI resources:
  - `modules/ui/decoded/data__ui__msgwarm.ui.json`
  - `modules/ui/decoded/data__ui__evolve.ui.json`

## Source Chain

```text
game.b.J()
-> game.k.H/L/I pending queue
-> game.k world tick shows S.b(...) or S.E(); S.a(text,prompt)
-> after msgwarm closes, key 32 gate:
   !M.h() && !S.G() && !K && L[0] != -1 && k(32)
   => U = 4, K = true, S.c = 0, state = 7, S.F()
-> state 7 opens petstate via S.W()
-> tutorial U == 4 locates pet by L[0]/L[1]
-> prompt user to select that pet and press 5
-> h.bg() opens /data/ui/evolve.ui
-> h.bh() confirm:
   if no target: msgwarm cannot evolve/mutate
   if level/material OK: hide widget 10, create ah row type 10, consume material
   after ah completes: mutate pet payload to target species, update widget 10/name/material, show success msgwarm
```

## msgwarm.ui

| Widget | Source meaning |
| ---: | --- |
| `8` | frame cell `257:128`, x76 y106 w89 |
| `7` | message text, x85 y119 w70 |
| `6` | prompt text, x89 y170 w60 |

Rebuild target:

- `TextBox.msgWarm(text,prompt)`.
- Use source UI frame coordinates/cell where practical.
- Long message and prompt use one-line marquee per current UI policy.

## evolve.ui

| Widget | Source meaning |
| ---: | --- |
| `4` | main panel, x43 y55 w158, sprite 257 cell 1 |
| `5` | title |
| `6` | right/back softkey |
| `7` | left/confirm softkey |
| `10` | pet sprite |
| `38` | current/target pet name |
| `40` | level |
| `19..22` | old HP/attack/defense/speed |
| `31..34` | target HP/attack/defense/speed |
| `45` | material name |
| `46` | material current/required |

## Current Classification

| Piece | Status |
| --- | --- |
| Queue producer | `PORTED/PARTIAL`; battle P22 fills `Scene.sourceEvolutionQueue`, `sourceEvolutionL`, and `sourceEvolutionI`. |
| World notice consumer | `PORTED/PARTIAL`; `game.k.H/L/I/ac` flow is represented, including detailed `msgwarm` notice when `L[0] != -1`. |
| msgwarm frame/prompt | `PORTED/PARTIAL`; `/data/ui/msgwarm.ui` decoded coordinates are used for frame/message/prompt, with one-line marquee. Not a generic `ao/al` widget runtime. |
| Tutorial bridge `K/L/key32/U=4` | `PORTED`; after detailed notice, confirm opens world petstate, sets `K=true`, `U=4`, and selects pet matching `L[0]/L[1]`. |
| evolve.ui static panel | `PORTED/PARTIAL`; decoded widget coordinates for frame, sprite, name, level, stats, material, and softkeys are represented. Not full `ao/al` runtime. |
| Confirm validation level/material | `PORTED`; no-target, level-low, and material-missing branches use `msgwarm` warnings and do not mutate pet. |
| Warning confirm | `PORTED`; pressing continue on no-target, level-low, or material-missing `msgwarm` closes only `msgwarm.ui` and returns to `evolve.ui` (`f=3 -> f=2`). |
| Back/soft-right from evolve.ui | `PORTED`; source-shaped `f<3` back closes `/data/ui/evolve.ui` and resets tutorial/evolution selection state. |
| Material consume | `PORTED/PARTIAL`; material count is consumed through current rebuild special/material inventory slot. Full save/global inventory parity remains future work. |
| Pet payload mutate | `PORTED/PARTIAL`; species/visual/max HP are updated and EXP is preserved. Full original payload carryover still needs deeper save parity audit. |
| `ah` type 10 effect | `PORTED/PARTIAL`; source row `[0,0,10,0,0,oldVisual,0,0,newVisual,0,0]` controls wait/mutate ordering. Exact `l.b(...,0)` alpha transform and MIDP pixel parity are still not claimed. |
| Success confirm after msgwarm | `PORTED`; pressing continue after `Tiến hóa thành...` closes only `msgwarm.ui` and returns to `evolve.ui` with the evolved pet/name/stats visible, matching `game.h.bh()` `f=3 -> f=2` behavior. |
| Panel refresh after mutate | `PORTED`; after payload mutation, rebuild refreshes the current pet side and next-evolution side from the mutated species. For species `6 -> 7`, the panel becomes current `7` and next target `8`, so material changes to the next requirement instead of keeping species `6 -> 7` stale data. |
| No next target panel | `PORTED/PARTIAL`; when target is `-1`, rebuild hides target stat values and material text/count to match the source clearing material widgets `42/45/46`. Exact source behavior for every stale stat widget is still source-shaped rather than full widget-runtime parity. |

## Smoke Coverage

- `world_evolution_notice_after_levelup_current.png`
- `world_evolution_notice_queue_exhausted_current.png`
- `world_evolution_tutorial_petstate_bridge_current.png`
- `world_evolution_evolve_ui_open_current.png`
- `world_evolution_confirm_success_mutate_current.png`
- `world_evolution_after_success_continue.png`
- `world_evolution_confirm_no_material_current.png`
- `world_evolution_confirm_level_low_current.png`
- `world_evolution_no_next_target_warning.png`
- `world_evolution_no_next_target_after_warning_continue.png`
- `world_evolution_confirm_no_material_after_warning_continue.png`
- `world_evolution_confirm_level_low_after_warning_continue.png`
- `world_evolution_back_from_evolve_ui.png`

Regression route smoke:

- `route_sophie_after_battle_branch_current.png`
- `route_bunny_after_battle_task_current.png`
- `route_elder_after_battle_reward_state_current.png`

Verification:

- `build.ps1`: pass.
- `com.vqsv.rebuild.Main --check`: pass.
- `VqsvBattleDamageFormulaCheck`: pass.
- Java mojibake scan: pass.

## Remaining Caveats

- `msgwarm.ui` and `evolve.ui` are source-shaped renderers, not a full reusable widget runtime.
- Evolution `ah` type 10 is not pixel-compared with MIDP and does not yet reproduce the exact Java ME `drawRGB`/alpha transform.
- Material consume uses the current rebuild's special/material inventory representation; full save inventory parity is still a later phase.
