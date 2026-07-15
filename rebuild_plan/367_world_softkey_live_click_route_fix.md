# 367 - World Softkey Live Click Route Fix

Date: 2026-07-15

Scope: fix the two bottom free-world `world.ui` corner buttons that rendered
but did not respond to mouse clicks in a live route state.

## Source Facts

Source files/data read:

- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- `modules/ui/decoded/data__ui__world.ui.json`
- `rebuild_game/src/main/java/VqsvIntroDemo.java`
- `rebuild_game/src/main/java/VqsvWorldActors.java`
- `rebuild_game/src/main/java/VqsvPanelRuntime.java`
- `rebuild_game/src/main/java/VqsvWorldResumeDescriptor.java`

`game.k.c()` opens `/data/ui/world.ui`.

`world.ui` bottom softkeys:

| Widget | Source position | Cell | Meaning |
| --- | --- | --- | --- |
| `7` | `x=0 y=303 w=18` | `175` | left system softkey |
| `5` | `x=222 y=303 w=16` | `68` | right game menu softkey |

Source route:

- Left softkey route opens `/data/ui/gamesystem.ui` through `game.k.m()`.
- Right softkey route opens `/data/ui/gamemenu.ui` through `game.k.k()`.
- `game.k.k()` / `game.k.m()` call `aU()` before opening the panel, hiding the
  active `world.ui` widgets while the panel is active.

## Rebuild Bug

The rebuild already rendered the correct icons and had simple smoke coverage,
but the click gate used a concrete class whitelist:

- `Op13FreeWorldTrigger`
- `ActorTransitionFreeWorldTrigger`
- `ActorInteractionFreeWorldTrigger`
- `Room0PostGroup6FreeWorld`

Live route resume can wrap those free-world blockers in
`VqsvWorldResumeDescriptor.WorldResumeTraceBlocking`. The visible world icons
therefore stayed on screen, but `canOpenSourcePanel()` returned false because it
only saw the wrapper class.

## Fix

Implemented a package-local marker:

`SourceWorldPanelOpen`

Applied it to source-shaped free-world blockers:

- `Op13FreeWorldTrigger`
- `ActorTransitionFreeWorldTrigger`
- `ActorInteractionFreeWorldTrigger`
- `Room0PostGroup6FreeWorld`
- `WorldResumeTraceBlocking`

`VqsvIntroDemo.Scene.canOpenSourcePanel()` now accepts
`current instanceof SourceWorldPanelOpen`.

`Scene.click()` now treats a click on the rendered `world.ui` softkey as a
world-softkey click first. If a modal is not blocking and the current blocker is
free-world-panel-open-capable, it opens the matching panel. If a future state is
blocked, the click is consumed and a trace says `world.ui softkey click blocked`
with a debug snapshot instead of silently falling through to `key0`.

Follow-up softkey visual parity fix:

- `gamemenu.ui` widget `12` (`Xác định`, `x=1 y=296`) and widget `11`
  (`Quay lại`, `x=197 y=296`) both have `alt_image_ref` cell `28`.
- `gamesystem.ui` widget `10` (`Xác định`, `x=1 y=296`) and widget `11`
  (`Quay lại`, `x=197 y=296`) both have `alt_image_ref` cell `28`.
- Rebuild now draws that cell before drawing the text, instead of rendering the
  softkey text directly on top of the map.
- `gamesystem.ui` uses `l_color=-1` on softkey widgets. Rebuild treats that as
  source white for these softkeys instead of falling back to red.
- Softkey labels are centered against the rendered source cell bounds, not
  pinned to the widget x coordinate only.
- Follow-up visual polish: the bitmap glyph ink was still sitting too high
  inside the blue source cell. Rebuild now lowers only the softkey text baseline
  by 2 px after source-cell centering, matching the live screenshot crop more
  closely without changing panel input or route logic.

## Status

| Area | Status | Note |
| --- | --- | --- |
| `world.ui` widget/cell mapping | `PORTED` | widgets `7/5`, cells `175/68`. |
| Left click -> `gamesystem.ui` | `FIXED/PORTED/PARTIAL` | route is source-backed; generic UI VM still not full. |
| Right click -> `gamemenu.ui` | `FIXED/PORTED/PARTIAL` | route is source-backed; deeper panel rows remain per existing slices. |
| `gamemenu.ui` confirm/back softkey frame | `FIXED/PORTED/PARTIAL` | widgets `12/11`, cell `28`, text from source layout. |
| `gamesystem.ui` confirm/back softkey frame | `FIXED/PORTED/PARTIAL` | widgets `10/11`, cell `28`, text from source layout; label color white and baseline visually centered. |
| Wrapped resume click | `FIXED` | wrapper now preserves free-world panel-open eligibility. |
| Full Java ME input VM | `PENDING` | this slice fixes concrete live route behavior only. |

## Smoke

New checkpoints:

- `world_softkey_resume_wrapper_left_opens_gamesystem`
- `world_softkey_resume_wrapper_right_opens_gamemenu`

Follow-up smoke for the softkey visual center:

- `world_softkeys`: PASS 8/8 at
  `rebuild_game/build_intro_demo/world_softkeys_softkey_text_center_final`
- `world_panel_full`: PASS 44/44 at
  `rebuild_game/build_intro_demo/world_panel_full_softkey_text_center_final`

These load room1, mark `[1,1,1]` complete, wrap an `Op13FreeWorldTrigger` in
`VqsvWorldResumeDescriptor.SCENE1_ROOM1_AFTER_SAVE_TO_OP13`, then click the
bottom corners. They assert:

- the descriptor trace exists,
- the correct panel opens,
- no `softkey click blocked` trace appears.

## Next

After this live-click fix, return to the panel route roadmap:

1. If user still sees a blocked click, inspect the new debug trace snapshot and
   classify the blocking overlay/current state.
2. Otherwise continue with `record.ui` c=1 -> `badge.ui` open/render/back.
