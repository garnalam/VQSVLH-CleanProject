# 185 - Panel petsetting release confirm softkey visual closeout

Date: 2026-07-10

## Scope

Fixed the visual debt in the `petsetting c=3` release confirm prompt:

- `/data/ui/msgconfirm.ui`
- bottom softkeys `2` and `3`
- centered prompt message text

No release/remove-pet gameplay logic was changed in this slice.

## Source Notes

Source files checked:

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/k.java`
- `modules/source_code/decoded/decompiled_source_cfr/m.java`
- `modules/ui/decoded/data__ui__msgconfirm.ui.json`

Important finding:

- `msgconfirm.ui` softkey widgets `2/3` use `alt_image_ref id=15 mode=3`.
- Source `m.java` treats mode `3` as sprite animation/state rendering, not a direct cell draw.
- Previous rebuild renderer drew `id=15` as a cell, so the softkey backing was effectively wrong and the text looked unwrapped/unframed.

## Implemented

File:

- `rebuild_game/src/main/java/VqsvSceneView.java`

Changes:

- `renderSourceReleaseConfirm()` now draws the prompt message through a dedicated centered prompt text helper.
- `drawPromptSoftkeyBackground()` now handles `altMode == 3` by calling `SpriteAnim.setState(widget.altId)` and `drawAligned(...)`.
- `world.ui` is hidden while `sourceReleaseConfirmVisible` so world softkeys do not bleed under the modal prompt.
- Text still uses source widget text color and is scaled into the widget width when needed.

## Status

- `PORTED/PARTIAL`: `msgconfirm.ui` frame/message/softkey render path.
- `PORTED/PARTIAL`: source animation-mode softkey backing for this prompt.
- `PENDING`: full generic `ao/af/k/m` UI runtime.
- `PENDING`: release success mutation.
- `PENDING`: last-alive/protected-pet warning branches.
- `PENDING`: original-vs-rebuild pixel-perfect comparison.

## Verification

Build/check:

- `build.ps1` pass
- `git diff --check` pass with existing CRLF warnings only
- `com.vqsv.rebuild.Main --check` pass
- `VqsvBattleDamageFormulaCheck` pass
- Java mojibake scan: no output

Focused smoke PNG:

- `panel_petstate_petsetting_release_confirm_open`
- `panel_petstate_petsetting_release_cancel_returns_petstate`

Regression smoke PNG:

- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`

## Next

Recommended next slice:

1. Implement `petsetting c=3` release confirm success mutation from `game.h.X()`.
2. Keep the first pass narrow:
   - confirm removes selected pet
   - clears equipment owner for released pet
   - refreshes `petstate.ui`
   - clamps selected index
3. Then add warning branches separately:
   - last-alive pet cannot release
   - protected/mythic pet cannot release
