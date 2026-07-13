# 221 Battle Choice UI Wheel/Hover/Click Mapping Audit

Status: VERIFIED / CODED SMALL FIX.

Scope:

- Battle `/data/ui/choice.ui` list input mapping for P4, P16, and P21.
- Mouse wheel should move the visible viewport/scrollbar only.
- Hover/click after wheel must map visible row back to the correct source item id.
- No client launch; PNG smoke only.

Out of scope:

- Pixel-perfect `choice.ui` widget runtime.
- Full generic `game.h` UI VM.
- P17 catch animation parity.

## Source-backed classification

| State | Source UI | Rebuild verification | Status |
| --- | --- | --- | --- |
| P4 item list | `game.h.aj()/ak()` opens `/data/ui/choice.ui` | `battle_choice_ui_p4_wheel_hover_click_viewport` scrolls a real item list and asserts hover/click choose the viewport row item ids | PORTED/PARTIAL, VERIFIED |
| P16 item target | Source enters P16 then opens `/data/ui/petstate.ui`, not `choice.ui` | `battle_choice_ui_p16_audit_not_choice_ui` proves current route opens petstate and records trace | NOT APPLICABLE TO CHOICE.UI, VERIFIED |
| P21 catch list | `game.h.ah()/ai()` opens `/data/ui/choice.ui` | Real route remains covered by `battle_mouse_wheel_p21_list`; long-list mapping uses synthetic P21 because current source route exposes only the available ball rows | PORTED/PARTIAL, VERIFIED |

## Bug found

The previous `VqsvChoiceUiView` construction and render sync reused source
cursor clamping for PC mouse-wheel input. That meant wheel movement could be
pulled back to keep the old selected index visible, which breaks the PC QoL
rule already used by panel lists: wheel moves the viewport, hover/click changes
selection.

## Code change

- `VqsvChoiceUiView` now has viewport-only scroll clamping.
- Battle wheel/render sync uses viewport scroll for `choice.ui`.
- Pointer hover/click in battle menu input keeps the wheel viewport and maps
  visible row to `scroll + row`.
- Keyboard/source cursor behavior remains separate.

## Smoke evidence

PNG checkpoints added to `battle_quick`:

- `battle_choice_ui_p4_wheel_hover_click_viewport`
- `battle_choice_ui_p21_synthetic_wheel_hover_click_viewport`
- `battle_choice_ui_p16_audit_not_choice_ui`

Regression checks run:

- `--smoke-suite battle_quick` => 19/19 pass.
- `--smoke-suite panel_wheel` => 8/8 pass.
- `battle_p11_shop_mouse_wheel_no_confirm` pass.
- `battle_mouse_wheel_p21_list` pass.

## Remaining honesty notes

- P21 long-list scroll mapping is synthetic. This is intentional because the
  current source route only has the real ball rows available from inventory,
  typically one or two rows, so it cannot prove scroll math by itself.
- P16 is not a `choice.ui` target in source. Future work should not add P16 to
  choice-ui parity tasks except as a "not applicable" regression guard.
- Full `choice.ui` binary widget runtime is still PARTIAL; this audit only
  locks the input mapping and source state classification.

## Next

If list parity work continues, audit the remaining battle list overlays that
are not `choice.ui`, especially `petstate.ui` row mapping under forced switch
and item target flows. If visual UI work continues, stay in Phase 11 and choose
a concrete widget-runtime gap instead of reopening generic list input.
