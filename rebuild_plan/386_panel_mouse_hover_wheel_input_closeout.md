# 386 - Panel Mouse Hover / Wheel Input Closeout

Date: 2026-07-15

Scope: apply the battle-style PC input model to panel/menu UIs with selection.

Rule locked by this slice:

- hover only previews selection/detail;
- click/confirm is the only path that activates a row;
- mouse wheel scrolls long visible lists without changing/confirming selection;
- mouse wheel moves selection for short non-scrollable menus/lists without
  confirming.

## Runtime Changes

Touched:

- `rebuild_game/src/main/java/VqsvPanelRuntime.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Implemented/changed:

- generic `VqsvPanelRuntime.mouseWheel(...)` now falls back to selection movement
  for non-scrollable panel modes;
- fixed `bodyShop.ui` and `shopbuy.ui` hover so it no longer sets `key0` or
  `keyBack`;
- added smoke coverage for:
  - `gamemenu.ui` hover and wheel selection;
  - `gamesystem.ui` hover and wheel selection;
  - `taskOption.ui` hover preview;
  - `bodyShop.ui` hover and wheel selection;
  - `shopbuy.ui` hover preview;
  - existing bag/task/petmap/world petstate wheel and hover behavior.

## Smoke

Command:

```powershell
cd rebuild_game
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-suite panel_wheel build_intro_demo\panel_wheel_input_386
```

Result: `PASS`, `16/16`.

Additional regression:

```powershell
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-suite panel_portable_shop build_intro_demo\panel_portable_shop_after_input_386
```

Result: `PASS`, `22/22`.

Release check:

```powershell
java "-Dvqsv.modules=..\modules" -cp build\classes com.vqsv.rebuild.Main --check
```

Result: `PASS`.

## Status

Status: `PORTED/PARTIAL` plus `PC_QOL`.

This is source-shaped for PC usability. It does not claim exact Java ME pointer
runtime, because the original phone UI primarily uses key events and softkeys.

## Next

Continue route-by-route on the remaining panel/game-system features. The next
best slice is to choose the next visible `gamesystem.ui`/menu route and audit
source UI + logic before porting.
