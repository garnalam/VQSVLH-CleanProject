# 195 - Panel bag.ui b==3 q.N case 0 hatch closeout

Date: 2026-07-10

## Scope

Target slice: audit/port `bag.ui` tab `b == 3`, `q.N case 0` hatch action.

This is separate from item 14 accelerator:
- item 14 lives in item tab logic and only sets `game.k.q` progress.
- hatch action lives in special tab `q.N[0]`.

## Source anchors

Source files:
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
  - `bl()` renders `q.N` rows into `bag.ui` widgets `137/138/139...`, description `163`, scrollbar `162`.
  - `b == 3`, `q.N case 0`:
    - if `!q.k(0)`: no action.
    - if `game.k.a().r()` false: `msgwarm.ui` text `Vẫn chưa thể ấp trứng`, `f = 1`.
    - if storage full `q.y() == 2`: `msgwarm.ui` text `Không gian không đủ...`, `f = 1`.
    - success: `game.k.q = 0`, `q.j(0)`, refresh `bl()`, `msgwarm.ui` text `Ấp trứng thành công`, `f = 2`.
    - after `f == 2`: add species via `game.h.g(int)`, then `openbox.ui` result, `f = 3`.
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
  - `r()`: ready when `q.I == 0 && game.k.q >= 10` or `q.I > 0 && game.k.q >= 30`.
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
  - `y()`: party `< 6` -> 0, bank `< 100` -> 1, else 2.
  - `j(0)`: closes active egg row `[0,1,*] -> [0,0,*]`.
  - `a(short species)`: records hatched species in `R/I`.

## Rebuild changes

Files:
- `rebuild_game/src/main/java/VqsvPanelRuntime.java`
  - Added `bagTab` navigation.
  - Added source-shaped special tab row for `q.N case 0` from `aq.c[5][0]`.
  - Added hatch action:
    - not-ready warning.
    - full-space warning.
    - success mutation.
    - `msgwarm.ui -> openbox.ui` two-step result.
  - Uses `VqsvSourceRandom` trace for later random egg species.
- `rebuild_game/src/main/java/VqsvIntroDemo.java`
  - Added `sourceEggKnownSpecies` as small rebuild mirror of `q.R`.
- `rebuild_game/src/main/java/VqsvSaveRuntime.java`
  - Saves/loads `sourceEggKnownSpecies`.
- `rebuild_game/src/main/java/VqsvText.java`
  - Added source hatch warning/success/result strings.
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`
  - Added focused hatch smoke checkpoints.
- `rebuild_game/src/main/java/VqsvTextRenderer.java`
  - Tightened `SOURCE_OPENBOX` long-line rendering so hatch result text is visible instead of starting as an empty marquee frame.

## Status

PORTED/PARTIAL:
- `bag.ui b==3 q.N case0` egg row render.
- `game.k.r()` readiness threshold.
- `q.y()` party/bank/full decision.
- first egg species 58 path.
- later egg weighted species path via traceable RNG.
- `msgwarm.ui` success/warning and `openbox.ui` result sequence.
- `q.j(0)` behavior approximated by `sourceEggActive=false`.
- `q.R/q.I` approximated by `sourceEggKnownSpecies/sourceEggType`.

PENDING:
- Full `q.N` runtime for special rows other than case 0.
- Full `game.h.g(int)` nature/quality parity for special species forms.
- Full `bag.ui` scrollbars per tab widget `125/162`; current hatch smoke validates runtime behavior and visible UI but not pixel-perfect.
- Full global RNG seed stream parity.

## Verified

Build/check:
- `.\build.ps1`
- `java "-Dvqsv.modules=..\modules" -jar build\libs\vqsv-liet-hoa-rebuild.jar --check`
- `java "-Dvqsv.modules=..\modules" -cp build\classes VqsvBattleDamageFormulaCheck`
- `rg -n "Ã|Â|�" rebuild_game/src/main/java`
- `git diff --check`

Focused PNG smoke:
- `panel_bag_egg_tab_ready_render`
- `panel_bag_egg_hatch_not_ready_warning`
- `panel_bag_egg_hatch_success_msg`
- `panel_bag_egg_hatch_result_to_bag`
- `panel_bag_egg_hatch_result_to_bank`
- `panel_bag_egg_hatch_space_warning`

Regression PNG smoke:
- bag open/navigation/back/cannot-use.
- item13 success/already/forbidden.
- item14 no-egg/type0/type1/already-ready/return.
- panel save.
- petsetting active/evolve/release/item/equipment/skill.
- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`

## Recommended next

Next best slice: audit/port remaining `bag.ui b==3 q.N` special rows 5/6/10/7/8/9 only if those rows become reachable in current route. Otherwise move to top-level `bag.ui` default item-use state 17 branches after source audit.
