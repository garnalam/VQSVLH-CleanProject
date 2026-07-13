# 175 - Panel petstate -> petsetting source path slice

Date: 2026-07-10

## Scope

Source-backed small slice for:

- `gamemenu b=1 -> P=7 -> petstate.ui`
- confirm carried pet in `game.h.X()`
- open `/data/ui/petsetting.ui`
- navigate/back/trace confirm only

No pet action mutation is included in this slice.

## Source Chain

Primary source:

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`

Confirmed path:

- `game.h.l()` on gamemenu `b=1`
- `o.a((byte)7)` enters carried pet state.
- `game.h.W()` opens `/data/ui/petstate.ui`.
- `game.h.X()` when owner is `game.k`, state `Q == 6 || Q == 0`, `f == 0`, confirm:
  - sets `c = 0`
  - calls `o.m()`
  - sets `f = 1`
  - opens `/data/ui/petsetting.ui`
  - sets list cursor to `c`
  - if selected pet `R() == 2`, widget `9 = "Di hoa"`, list length 6
  - if selected pet `R() == 1`, widget `9 = "Tien hoa"`, list length 6
  - otherwise widget `9 = ""`, list length 5

Petsetting action source from `game.h.X()` f=1 is larger and intentionally deferred:

- `c=0`: opens `choice.ui` item-use list.
- `c=1`: battle-position/switch validation.
- `c=2`: opens `choice.ui` equipment list.
- `c=3`: release confirm/warnings.
- `c=4`: opens `skill.ui`.
- `c=5`: opens `evolve.ui` through `bg()/bh()`.

## UI Mapping

Decoded UI:

- `modules/ui/decoded/data__ui__petsetting.ui.json`

Widgets used:

- frame/static: `1`, `2`, `3`, `4`
- action rows: `5`, `6`, `7`, `8`, `10`, `9`
- row text/order:
  - `5`: Dao cu
  - `6`: Chien dau
  - `7`: Vat pham trang suc
  - `8`: Phong sinh
  - `10`: Ky nang
  - `9`: Tien hoa / Di hoa / hidden-empty

## Rebuild Status

Implemented:

- Open `petsetting.ui` from world/panel `petstate.ui` confirm.
- Keep `petstate.ui` rendered underneath, matching source where both UI files remain loaded.
- Up/down navigation over 5 or 6 source rows.
- Back closes only `petsetting.ui` and returns to `petstate.ui`.
- Confirm logs `PENDING` with selected source action, no mutation.

Status:

- `PORTED/PARTIAL`: source route and UI shell.
- `PENDING`: item-use, battle-position, equipment, release, skill, evolve action subflows.
- `PENDING`: full original-vs-rebuild pixel compare.

## Verification

Focused PNG smoke:

- `panel_petstate_petsetting_open`
- `panel_petstate_petsetting_navigation`
- `panel_petstate_petsetting_back_returns_petstate`
- `panel_petstate_petsetting_confirm_pending`

Regression:

- existing panel petstate open/navigation/back
- bag/task/petmap/save/system option
- Sophie/Bunny/Elder route smoke

## Next

Next recommended original panel slice:

1. Audit and port `petsetting c=4 -> skill.ui` render/navigate/back, because it is read-only and low mutation risk.
2. After read-only petsetting subviews are stable, choose between bag item-use or speed toggle.

Do not implement speed toggle until we either finish or explicitly defer the remaining source panel subflows.
