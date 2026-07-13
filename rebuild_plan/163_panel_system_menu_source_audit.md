# 163 - Panel/System Menu Source Audit

Date: 2026-07-09

Scope: audit original world panel/menu system before adding rebuild-only speed controls.

Rule: original panel first. Any speed multiplier is `REBUILD_POLICY` and must be added only after the original `gamemenu.ui` / `gamesystem.ui` flow is source-backed enough to host it without hiding missing source behavior.

## Source Files Read

- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
  - Main world runtime/state machine.
  - `P == 0` is free world.
  - `P == 6` is game menu.
  - `P == 7` pet carried state.
  - `P == 8` bag.
  - `P == 9` record/pokedex.
  - `P == 10` task.
  - `P == 14` game/system menu.
  - `P == 22` save.
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
  - UI facade/runtime for world, menu, panel, task, bag, pet, save, help.
- Source UI files:
  - `/data/ui/world.ui`
  - `/data/ui/gamemenu.ui`
  - `/data/ui/gamesystem.ui`
  - `/data/ui/petstate.ui`
  - `/data/ui/bag.ui`
  - `/data/ui/record.ui`
  - `/data/ui/task.ui`
  - `/data/ui/help.ui`
  - `/data/ui/help1.ui`
  - plus modal UIs: `msgtip.ui`, `msgwarm.ui`, `msgconfirm.ui`, `choice.ui`, `skill.ui`, `option.ui`.

## Source Entry Flow

Status: `PORTED_TARGET / NOT_PORTED_IN_REBUILD`

In `game.k.b()` free-world case `P == 0`, when no blocking UI/dialog is active, source opens panel on key `262144`:

- calls `this.m()`
- sets `this.S.b = 0`
- switches to `this.a((byte)6)`

State `P == 6` calls `this.S.l()`, and `game.h.l()` handles `gamemenu.ui`.

Important: the panel is not just a painted overlay. It is a world runtime state transition from free world into `P=6`, and subsequent menu pages are further `P` states.

## `world.ui`

Status in source: `PORTED_SOURCE_KNOWN`

Source `game.h.c()` loads `/data/ui/world.ui` with sprite bank `257`.

Source `game.h.e()` updates world UI widget text/effects while free-world state runs:

- Widget `6` receives `((k)this.o).k` while the world UI animation is active.
- Widgets `1..7` are hidden by `aS()` except `2/3/4` when entering menus.

Rebuild status: `PORTED/PARTIAL`

- Rebuild has a minimal `WorldUi` and corner click shortcut.
- It does not yet route through original `game.k.P == 6 -> game.h.l()` panel state.

## `gamemenu.ui` Hub

Status in source: `PORTED_SOURCE_KNOWN`

Source `game.h.k()`:

- hides world widgets via `aS()`
- loads `/data/ui/gamemenu.ui`, sprite bank `257`
- sets list count widget `0` to `6`
- writes title/icon text:
  - widget `14` = `an.f(605 + this.b)`
  - widget `15` = first menu label
  - widgets `5..9` = remaining menu labels
  - widget `18` = `this.q.G()`
  - widget `19` = `this.q.E()`
- sets `f = 0`

Source labels from code:

1. `Tuy than cua hang` (`Tuy than cua hang` without accents; source string has Vietnamese accents)
2. `Sung vat`
3. `Lung bao`
4. `Do giam`
5. `Nhiem vu`
6. `Luu du lieu`

Decoded UI geometry summary:

- `gamemenu.ui`: 19 top-level children, 1 style.
- Text widgets include:
  - `10` title at about `99,76`
  - `15` first row at about `93,105`
  - `5..9` next rows at about `93,122..190`
  - `11` bottom-right back label
  - `12` bottom-left confirm label

Source `game.h.l()` input:

- `4100`: up
- `8448`: down
- `196640`: confirm
- `262144`: back

Menu branch mapping:

| `b` | Label | Source action |
| --- | --- | --- |
| 0 | Tuy than cua hang | close `gamemenu.ui`, set world state `P=14` |
| 1 | Sung vat | reset `c=0`, call `o.m()`, close menu, set `P=7` |
| 2 | Lung bao | call `o.m()`, close menu, set `P=8` |
| 3 | Do giam | reset `c=0`, close menu, set `P=9` |
| 4 | Nhiem vu | reset `b=0`, close menu, set `P=10` |
| 5 | Luu du lieu | hide widgets `11/12`, set `P=22` |

Rebuild status: `PENDING`

- No source-shaped `gamemenu.ui` runtime exists yet.
- No `P=6` equivalent exists yet.

## `gamesystem.ui`

Status in source: `PORTED_SOURCE_KNOWN`

Source `game.h.m()`:

- hides world widgets via `aS()`
- loads `/data/ui/gamesystem.ui`, sprite bank `257`
- sets list widget `0` selected row from `b`
- sets `f = 0`

Decoded UI summary:

- `gamesystem.ui`: 11 top-level children, 1 style.
- Text labels in source UI:
  - `Tiep tuc tro choi`
  - `Tro giup choi`
  - `Thiet lap tro choi`
  - `Tro lai menu chinh`
  - bottom confirm/back labels

Source `game.h.n()` branch mapping:

| `b` | Label | Source action |
| --- | --- | --- |
| 0 | Continue game | close `gamesystem.ui`, return `P=0` |
| 1 | Help | close `gamesystem.ui`, set `P=20` |
| 2 | Settings/help page | close `gamesystem.ui`, set `P=21` |
| 3 | Return main menu | opens `option.ui`; if confirmed, clears timers/flags and sends game app to main menu state |

Original speed control: `UNKNOWN / NOT_FOUND_IN_THIS_AUDIT`

- No source-backed speed toggle was found in the read `gamesystem.ui` branch.
- A future speed multiplier should be marked `REBUILD_POLICY`, not `PORTED`.

Rebuild status: `PENDING`

- Boot title menu exists, but in-world `gamesystem.ui` does not.
- New speed UI must not be presented as original behavior.

## Save Flow

Status in source: `PORTED_SOURCE_KNOWN / REBUILD_PARTIAL`

Source branch:

- `gamemenu b=5` enters `P=22`.
- `game.h.K()` drives save.
- `f == 0` and confirm:
  - sets `f = 1`
  - writes `Dang luu...`
  - hides widgets `3/4` via `J()`
- `f == 1`:
  - waits for `((k)this.o).k()`
  - writes `Luu thanh cong`
  - sets `f = 2`
- `f == 2`:
  - closes `msgtip.ui`
  - closes `gamemenu.ui`
  - returns `P=0`

Rebuild status: `PORTED/PARTIAL`

- `VqsvSaveRuntime` can save/load route snapshot.
- Current world save prompt is route-specific and not the original menu save flow.
- Needs connection from future panel `gamemenu b=5`.

## Pet Panel

Status in source: `PORTED_SOURCE_KNOWN / REBUILD_PARTIAL`

There are two relevant pet paths:

1. Carried pet panel from `gamemenu b=1 -> P=7 -> game.h.X()`.
2. Pet bank from `game.h.y()/z()` using `petstate.ui` title `Ngan hang Sung vat`.

Source carried-pet `X()`:

- handles row navigation in `petstate.ui`
- confirm opens `petsetting.ui` for actions when owner is `game.k`
- action options include item use, battle position, equipment, release, skill, evolve/mutate when available
- battle owner `game.d` has different validation messages for dead/current pet.

Rebuild status: `PORTED/PARTIAL`

- Battle P5/P16 and world petstate renderer exist.
- World petstate is currently a direct shortcut, not the source `gamemenu b=1 -> P=7` path.
- `petsetting.ui`, full skill/equipment/release/evolve menu flow are still partial/pending.

## Bag Panel

Status in source: `PORTED_SOURCE_KNOWN / MOSTLY_PENDING`

Source `game.h.Y()` loads `/data/ui/bag.ui`, sets `b=0`, initializes tab list, sets widget `14` to `Vat pham`.

`game.h.ac()` handles bag runtime:

- left/right tab switches through list widgets `8`, `47`, `86`, `125`
- up/down moves rows
- confirm uses item or opens subflows
- back returns to `P=6`

Source bag tabs from code:

| `b` | Source data vector | Meaning |
| --- | --- | --- |
| 0 | `q.K + q.J` | normal items/items |
| 1 | `q.L` | equipment/accessories |
| 2 | `q.M` | key/other item group |
| 3 | `q.N` | special items/features |

Decoded UI summary:

- `bag.ui`: 11 top-level children, 1 style.
- It uses nested list containers, so top-level child count is misleading; source code references widgets up to at least `165`.

Rebuild status: `PORTED/PARTIAL_FOR_BATTLE_ONLY`

- Battle `choice.ui` item/catch menu exists.
- Full world `bag.ui` is pending.

## Record / Task / Help

Status in source: `PORTED_SOURCE_KNOWN / PENDING`

Record:

- `gamemenu b=3 -> P=9 -> game.h.N()/O()`
- loads `record.ui`
- writes pet count, rare/god pet counters, badge count, total play time.
- can branch to pet map/badge pages.

Task:

- `gamemenu b=4 -> P=10 -> game.h.R()/S()`
- loads `task.ui`
- two tabs: main and side task.
- data comes from `game.c.r`, `game.c.q`, `game.c.s`, `game.c.t`, `game.c.u`.

Help:

- reachable from `gamesystem.ui`.
- uses `help.ui` and `help1.ui`; scrolls text/pages with left/right/back.

Rebuild status:

- Task flags and task-tip messages exist for scripted route.
- Full panel pages `record.ui`, `task.ui`, `help.ui`, `help1.ui` are pending.

## Current Rebuild Gap List

| Area | Status | Notes |
| --- | --- | --- |
| `world.ui` corner widgets | `PORTED/PARTIAL` | Enough for current route, not full source runtime. |
| `gamemenu.ui` hub | `PENDING` | No `P=6` equivalent yet. |
| `gamesystem.ui` | `PENDING` | Required before clean speed mod entry. |
| save through panel | `PENDING/PARTIAL` | Save runtime exists; original UI flow not wired. |
| carried pet panel from menu | `PORTED/PARTIAL` | Renderer exists, but source menu path and petsetting flow missing. |
| bag panel | `PENDING` | Battle item UI is not world bag. |
| task panel | `PENDING` | Script task-tip exists; full task list missing. |
| record/pokedex panel | `PENDING` | No source-backed runtime page yet. |
| help panel | `PENDING` | No source-backed runtime page yet. |
| speed multiplier | `REBUILD_POLICY/PENDING` | Add only after original system menu is stable. |

## Recommended Implementation Order

1. `PanelRuntime` skeleton: implement `P=6` equivalent for `gamemenu.ui`, source input mapping, close/confirm/back, no subpages yet.
2. Source-backed `gamemenu.ui` renderer using `VqsvUiLayout.load("gamemenu.ui")`, sprite bank `257`, labels from `game.h.k()`, widgets `5..15`, money/badge widgets `18/19`.
3. Wire `ESC/BACKSPACE` and bottom-left world UI click to open panel, replacing direct petstate shortcut only after smoke proves no regression.
4. Implement `gamesystem.ui` page with original four options and back/continue flow.
5. Wire panel save option to existing `VqsvSaveRuntime` through a `msgtip.ui` shaped prompt/status, matching `game.h.K()`.
6. Add `REBUILD_POLICY` speed option under system/settings only after steps 1-5 pass. It should be visually marked in trace/docs as rebuild-only.
7. Then port pages in gameplay value order: bag, carried pet/petsetting, task, record, help.

## Smoke Plan For First Slice

PNG-only, no client needed:

- `panel_gamemenu_open_from_world`
  - start at 10-years-later/free world
  - press back/menu
  - assert panel visible and `gamemenu.ui` widget geometry/colors are rendered
  - assert labels match source list
- `panel_gamemenu_navigation`
  - up/down changes selected row
  - no event progression while panel is open
- `panel_gamemenu_back_returns_world`
  - back closes menu and free movement still works
- `panel_gamemenu_save_entry`
  - select row 5
  - assert `msgtip.ui` shaped save status appears
  - save succeeds and returns world
- Regression:
  - `boot_new_game_skip_intro_yes`
  - `room1_bunny_after_save_immediate_free_move`
  - `route_bunny_after_battle_task`

## Next Concrete Step

Implement only the first slice:

`PanelRuntime` + `gamemenu.ui` open/navigation/back smoke.

Do not implement speed toggle in the same slice. Speed belongs after original `gamesystem.ui` and save page are working, otherwise it will hide panel regressions behind a modded feature.
