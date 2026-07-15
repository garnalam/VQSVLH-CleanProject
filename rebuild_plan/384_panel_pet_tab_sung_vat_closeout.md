# 384 - Panel gamemenu.ui Tab Sung Vat Closeout

Date: 2026-07-15

Scope: close out the right-softkey `gamemenu.ui` row `Sung vat` as a current
roadmap slice. This covers:

- `gamemenu.ui` row `Sung vat`;
- carried pet list `petstate.ui`;
- pet action menu `petsetting.ui`;
- source-backed subroutes already represented from `petsetting.ui`.

This does not claim generic `game.h` widget VM parity or original-vs-rebuild
pixel compare.

## Source Entry

Source files/data read:

- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- `modules/ui/decoded/data__ui__gamemenu.ui.json`
- `modules/ui/decoded/data__ui__petstate.ui.json`
- `modules/ui/decoded/data__ui__petsetting.ui.json`
- `modules/ui/decoded/data__ui__choice.ui.json`
- `modules/ui/decoded/data__ui__skill.ui.json`
- `modules/ui/decoded/data__ui__evolve.ui.json`

Source route with PC policy premium shop enabled:

```text
world.ui right softkey
  -> game.k.k()
  -> gamemenu.ui row b=1 "Sung vat"
  -> game.k.l()
  -> c=0, o.r(), o.a((byte)7)
  -> close gamemenu.ui
  -> game.k.Z()/f(c) style petstate entry
  -> petstate.ui
```

Important source fact: when the portable shop row exists, `Sung vat` is row
`b=1`, not row `0`.

## Source UI Logic

`petstate.ui` source duties:

- list carried pets from source party;
- show HP/PP progress for up to 6 visible rows;
- show selected pet sprite, name, type, level, stats, star/rank cells;
- show held item/equipment text through `aq.c[3]` when present;
- back returns to `gamemenu.ui` selected row `Sung vat`;
- confirm opens `petsetting.ui` when owner is `game.k` and state is the normal
  world/menu pet branch.

`petsetting.ui` source rows:

| Row index | Source label | Source behavior | Rebuild status |
| --- | --- | --- | --- |
| `0` | `Dao cu` | opens `choice.ui` item-use list for selected pet | `PORTED/PARTIAL` |
| `1` | `Chien dau` | validate battle eligibility, move selected pet to front, or warning | `PORTED` |
| `2` | `Vat pham trang suc` | opens `choice.ui` equipment/held item list | `PORTED/PARTIAL` |
| `3` | `Phong sinh` | warning/confirm, release pet, protected/last-pet guards | `PORTED` |
| `4` | `Ky nang` | opens `skill.ui`, navigate/back | `PORTED/PARTIAL` |
| `5` | `Tien hoa` / `Di hoa` | only visible when source pet supports it; opens `evolve.ui` | `PORTED/PARTIAL` |

## Rebuild Mapping

| Source concept | Rebuild mapping | Status |
| --- | --- | --- |
| `game.k.k()` gamemenu route | `VqsvPanelRuntime.open(...)` / row selected `1` | `PORTED/PARTIAL` |
| `o.a((byte)7)` petstate route | `Scene.openWorldPetstate()` | `PORTED/PARTIAL` |
| carried party `game.g.z` / `q.A` | `Scene.sourcePets` | `PORTED/PARTIAL` |
| pet HP/PP/stat/detail | `VqsvBattlePetStateView` rows from `SourcePetState` | `PORTED/PARTIAL` |
| held item widget `59/60` | `VqsvBattleTables.heldItem(id)` / `aq.c[3]` | `PORTED/PARTIAL` |
| `petsetting.ui` shell | `Scene.sourcePetSettingVisible` + source row count | `PORTED/PARTIAL` |
| item-use choice | source item validation/apply helpers | `PORTED/PARTIAL` |
| battle-position switch | source-shaped move-to-front and warnings | `PORTED` |
| equipment choice | `Scene.sourceEquipmentItems`, q.L save/load smoke | `PORTED/PARTIAL` |
| release pet | confirmation, mutation, guards | `PORTED` |
| skill view | `skill.ui` route, navigation/back | `PORTED/PARTIAL` |
| evolve view | `VqsvSourceEvolutionRuntime` via `evolve.ui` | `PORTED/PARTIAL` |

## Latest Visual Fixes

Follow-up slice after visual review:

- star/rank widgets above the selected pet now render from source
  `petstate.ui` widgets `69..74`; filled stars use state/cell `14`, empty
  slots use the widget alt state;
- added a PC-only header back arrow beside the `Sung vat trong hanh trang`
  header so mouse users can return to `gamemenu.ui` without relying only on
  the softkey/back key;
- the header arrow is marked `PC_QOL`, not original MIDP pixel parity, because
  the source phone UI expects a softkey/back action rather than a mouse hitbox.

## Smoke Suite

Added dedicated suite:

```powershell
cd rebuild_game
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-suite panel_pet_tab build_intro_demo\panel_pet_tab_stars_back_385
```

Latest result: `PASS`, `39/39`.

Output directory:

```text
rebuild_game/build_intro_demo/panel_pet_tab_stars_back_385
```

Representative PNGs:

- `panel_petstate_open_from_gamemenu.png`
- `panel_petstate_quality_stars_header_back_arrow.png`
- `panel_petstate_header_back_arrow_returns_gamemenu.png`
- `panel_petstate_petsetting_open.png`
- `panel_petstate_petsetting_skill_open.png`
- `panel_petstate_petsetting_evolve_open.png`

## Coverage Matrix

| Checkpoint group | Coverage | Status |
| --- | --- | --- |
| `panel_petstate_quality_stars_header_back_arrow` | source star widgets visible above selected pet, PC header back arrow visible | `PASS` |
| `panel_petstate_header_back_arrow_returns_gamemenu` | mouse click on header arrow returns to `gamemenu.ui` row `Sung vat` | `PASS` |
| `panel_petstate_open/navigation/hover/back` | open from gamemenu, select row, hover preview, return to gamemenu | `PASS` |
| `panel_petstate_held_item0_widget_59_60` | held item name/icon uses `aq.c[3]` | `PASS` |
| `panel_petstate_petsetting_open/navigation/back` | action menu shell over petstate | `PASS` |
| `active_switch_*` | success, dead warning, already-active warning | `PASS` |
| `release_*` | confirm, cancel, success, last-pet warning, protected warning | `PASS` |
| `item_choice_*` | open, navigate, hp-full warning, warning return, success message, close back | `PASS` |
| `equipment_choice_*` | open, navigate, status labels, unequip/equip/transfer, return, q.L save/load | `PASS` |
| `skill_*` | skill UI open, navigate, wheel no-op, back | `PASS` |
| `evolve_*` | evolve UI open, missing material warning, success mutate | `PASS` |

## Remaining Debt

Still not claimed:

- full generic Java ME `game.h` widget VM;
- exact text baseline/clip/marquee behavior for every `petstate.ui`,
  `petsetting.ui`, `choice.ui`, `skill.ui`, and `evolve.ui` widget;
- original-vs-rebuild pixel compare;
- full pet bank/storage screen from `game.k.B()/C()` beyond the carried-pet tab;
- complete skill-management mutation if the original supports deeper skill
  rearrange/learn behavior from `skill.ui`;
- exact save object parity for every pet/equipment field, beyond focused q.L
  smoke coverage.

## Closeout Status

The `Sung vat` gamemenu tab is closed for current route-functionality purposes
at `PORTED/PARTIAL`.

All main visible branches from the tab are represented and have dedicated smoke
coverage. Remaining work is deeper pixel/runtime parity or special storage/skill
edge behavior, not a missing top-level tab route.

## Next Roadmap Step

Recommended next step after this closeout:

1. continue route-by-route in `gamemenu.ui`, choosing the next visible tab that
   still needs deeper parity;
2. or, if staying inside `Sung vat`, audit the exact `skill.ui` source behavior
   beyond read-only view before implementing any skill rearrange/learn mutation.
