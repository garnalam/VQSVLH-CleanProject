# 133 Battle Background game.d.c Snapshot

## Scope

Tighten the battle background visual path before moving to P7 hit/recoil/blood
timing.

This slice replaces the rebuild-only colored battle bands with the source-shaped
`game.d.c` background snapshot/fallback path, then applies the dark battle
presentation seen in the original capture. It does not change P7 damage, catch,
items, EXP, pet-switch, battle entry state, or actor motion.

## Source Chain

| Source | Finding | Status |
| --- | --- | --- |
| `game.d` field `public Image c` | Battle renderer owns an optional background image. | PORTED |
| `game.d.b(Graphics)` | If `this.c != null`, draws `this.c` at `(0,0)` before battle UI/actors. If null, fills the full screen black. | PORTED |
| `game.k` battle entry path | Before switching to battle state `game.i.a().a((byte)12)`, source creates `game.d.a().c = Image.createImage(an.w(), an.x())`, gets its graphics, and calls the current world/map renderer into that image. | PORTED/PARTIAL |
| `game.k` map overlay path | Uses the same captured `game.d.c` as a frozen world/map image for overlay state `P == 4`; clears it on exit. | AUDITED |
| `game.c` battle-like entry path | Also captures a current-screen image into `game.d.a().c` before entering the next UI/battle flow. | AUDITED |

## Rebuild Change

`SourceBattleRuntime.enterBattle` now sets:

- `s.battleBackgroundSnapshot = VqsvSceneView.captureBattleBackground(s)` only
  when the scene has a real map renderer (`s.useMap && s.mapRenderer != null`).
- Otherwise the snapshot remains `null`, so `VqsvBattleRenderer` follows the
  source fallback and fills the screen black.

`VqsvSceneView.captureBattleBackground` renders the current map plus rebuild
scenery actors into a 240x320 offscreen image before battle UI is drawn. It
intentionally excludes the player, event actors, battle HUD, text boxes, choice
UI, save prompt, world UI, and battle overlays. This follows the traced
`game.k.b.b(Graphics)` / `game.c.b` path, where the captured image comes from
the map/background renderer path rather than the full live scene composite.

`VqsvBattleRenderer` now draws that snapshot first, or black if missing. The old
hardcoded horizontal blue/green bands are no longer the battle background path.

Battle actor and marker placement now reads `/data/script/pos.mid` through
`VqsvBattleAnimationTables`: enemy slot `(177,103)/(144,85)` and player slot
`(70,223)/(36,206)` for group 0, with group 1 using the source player-side
quad offset. Follow-up source review found the actor coordinates were already
correct, but the rebuild drew both sides with direction `0`. Source battle setup
sets enemy actor `n = 1` and player actor `n = 0`, so the main battle sprites
now render enemy-facing and player-facing separately. This fixes the apparent
sprite/base drift without hand-tuning coordinates.

Follow-up correction after original visual review: when a snapshot exists, the
renderer darkens the captured map before drawing battle UI/actors. This matches
the original capture visually, where the battle background is the current map in
a dimmed battle presentation. The decompiled `game.d.b(Graphics)` only proves
the `game.d.c` draw/fallback order; the exact source of the dimming still needs
deeper tracing through `game.c.j` / map capture render state, so this darken
step is intentionally not claimed pixel-perfect.

## Smoke

Focused checkpoint:

```powershell
java -cp .\build\classes VqsvIntroDemo --smoke-checkpoint battle_background_game_d_c_snapshot build_intro_demo\battle_background_game_d_c_snapshot.png
```

Smoke setup:

- Load source-backed Scene 1 Room 1 map renderer.
- Place player near the Bunny route area.
- Enter the Bunny battle runtime.
- Assert `battleBackgroundSnapshot != null`.
- Render a PNG and assert visible non-black, non-old-band pixels exist behind
  battle actors/HUD.
- Assert the rendered background region is darker than the raw captured
  snapshot.

## Status

| Area | Status | Note |
| --- | --- | --- |
| `game.d.c` optional snapshot concept | PORTED/PARTIAL | Rebuild captures map-backed battle entry to a BufferedImage. |
| Black fallback when no snapshot exists | PORTED | Renderer fills full battle screen black when snapshot is null. |
| Removal of rebuild-only colored bands | PORTED | Old band colors are no longer used as battle background. |
| Dimmed map battle presentation | PORTED/PARTIAL | Rebuild darkens captured map before UI/actors; visual matches source capture better, exact decompiled source hook still under audit. |
| Battle actor/marker final coordinates | PORTED/PARTIAL | Final actor and marker anchors come from `pos.mid`; entry/switch offsets still have separate partial animation coverage. |
| Battle actor facing | PORTED | Enemy uses source direction `n=1`; player uses source direction `n=0`. |
| Snapshot object filtering | PORTED/PARTIAL | Keeps map/scenery objects, excludes player/event actors in rebuild's mixed actor model. Exact source map-object split is not fully reconstructed. |
| Exact world renderer capture parity | PORTED/PARTIAL | Rebuild uses current `VqsvSceneView` world render path, not a MIDP pixel compare of `game.k.b.b(Graphics)`. |
| Capture timing | PORTED/PARTIAL | Snapshot is taken at `enterBattle`, before battle overlay/HUD. Exact ordering against every source entry path is not fully proven. |
| Temp sprite/effect inclusion | PENDING | Current capture excludes temp sprites and screen effects; source entry snippets show world renderer capture but need deeper per-entry audit for transient effects. |
| Pixel-perfect background | PENDING | No original-vs-rebuild pixel comparison. |

## Next

After build/check/mojibake and PNG regression pass, move to the next visual debt:
P7 hit/recoil/blood timing.
