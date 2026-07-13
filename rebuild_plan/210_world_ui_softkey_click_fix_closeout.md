# 210 - World UI Softkey Click Fix Closeout

Date: 2026-07-10

Scope: make the two bottom world UI icons clickable in the PC rebuild.

## Source facts

Sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- `/data/ui/world.ui`

Confirmed source flow:

- `game.h.c()` loads `/data/ui/world.ui`.
- `game.h.d()` shows widgets `5` and `7`.
- `world.ui` widget `7` is the bottom-left icon, source-backed cell `167`.
- `world.ui` widget `5` is the bottom-right icon, source-backed cell `68`.
- In `game.k` state `P=0`:
  - key `131072` enters `P=13`, which calls `game.h.m()` and opens `gamesystem.ui`;
  - key `262144` enters `P=6`, which calls `game.h.k()` and opens `gamemenu.ui`.

## Rebuild changes

Files:

- `rebuild_game/src/main/java/VqsvWorldActors.java`
- `rebuild_game/src/main/java/VqsvIntroDemo.java`
- `rebuild_game/src/main/java/VqsvPanelRuntime.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Implemented:

- `WorldUi` now renders widget `7` and widget `5` via `/data/ui/world.ui` coordinates.
- `WorldUi.buttonAt(x,y)` uses source-backed widget hitboxes.
- Bottom-left icon opens `gamesystem.ui` directly through a new panel runtime entry:
  - status: `PORTED/PARTIAL`
  - trace: `world.ui left softkey source game.k P=0 key=131072 -> P=13 game.h.m gamesystem.ui open`
- Bottom-right icon opens `gamemenu.ui`:
  - status: `PORTED/PARTIAL`
  - trace: `world.ui right softkey source game.k P=0 key=262144 -> P=6 game.h.k gamemenu.ui open`

## Verified PNG smoke

Output:

- `rebuild_game/build/smoke/world_softkey_fix/`

Focused:

- `panel_gamesystem_click_softkey_open`
- `panel_gamemenu_click_softkey_open`

Regression:

- `panel_gamemenu_open_from_world`
- `panel_petstate_open_from_gamemenu`
- `panel_bag_open_from_gamemenu`
- `route_bunny_after_battle_task`

Core:

- `build.ps1`
- `com.vqsv.rebuild.Main --check`
- `VqsvBattleDamageFormulaCheck`
- Java mojibake/SMS scan

## Next recommended step

Rebuild the official jar again and let the user test directly from the saved room/world state. If more pointer UI issues appear, continue with source-backed widget hitboxes rather than hardcoded screen rectangles.
