# 187 - msgwarm.ui runtime polish closeout

Date: 2026-07-10

## Scope

Polished the generic `/data/ui/msgwarm.ui` runtime used by panel and battle
warnings. This slice only changes warning text rendering; it does not change
pet release, item, equipment, battle, or route logic.

## Source/UI Audit

Files checked:

- `modules/ui/decoded/data__ui__msgwarm.ui.json`
- `modules/source_code/decoded/decompiled_source_cfr/ae.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `rebuild_game/src/main/java/VqsvTextRenderer.java`

Decoded widget map:

- widget `8`: frame, `alt_image_ref id=128 mode=2`
- widgets `1/2/3/5`: colored fill bands
- widget `7`: message text, `x=85 y=119 w=70`, wraps enabled
- widget `6`: prompt text, `x=89 y=170 w=60`, align center

Important source behavior:

- `k.a(...)` calls `ae.a(...)`.
- When widget wrap is true, `ae.a(...)` lays out multiple wrapped lines and
  scrolls vertically only when needed.
- Rebuild previously treated `msgwarm.ui` message as one marquee line, causing
  long messages like `Ba lo phai luu it nhat 1 sung vat` to be clipped.

## Implemented

File:

- `rebuild_game/src/main/java/VqsvTextRenderer.java`

Changes:

- `SOURCE_MSGWARM` tick now scrolls only the prompt when the prompt is wider
  than widget `6`.
- widget `7` message now wraps inside the source-shaped message area.
- widget `6` prompt remains a one-line centered/marquee prompt.
- Wrapped message rendering now preserves the source widget text color instead
  of falling back to white.

## Status

- `PORTED/PARTIAL`: `msgwarm.ui` frame/fill/message/prompt widget map.
- `PORTED/PARTIAL`: message wrap behavior for current warning use cases.
- `PORTED/PARTIAL`: prompt one-line center/marquee behavior.
- `PENDING`: full `ao/af/k/m` runtime, including exact vertical scrolling
  lifecycle and original-vs-rebuild pixel compare.
- `PENDING`: exact source font width parity; rebuild bitmap font still differs
  slightly from J2ME `Font`.

## Verification

Build/check:

- `build.ps1` pass
- `git diff --check` pass with existing CRLF warnings only
- `com.vqsv.rebuild.Main --check` pass
- `VqsvBattleDamageFormulaCheck` pass
- Java mojibake scan: no output

Focused PNG smoke:

- `panel_petstate_petsetting_release_last_alive_warning`
- `panel_petstate_petsetting_release_protected_warning`
- `panel_petstate_petsetting_item_choice_warning_hp_full`
- `battle_msgwarm_source_widget_warning`

Regression PNG smoke:

- `panel_petstate_petsetting_release_warning_returns_petstate`
- `panel_petstate_petsetting_item_choice_success_msg`
- `panel_petstate_petsetting_equipment_choice_equip_success_msg`
- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`

## Next

Recommended next slice:

Return to panel roadmap and audit the remaining source panel branches after
petsetting c=3:

1. confirm whether petsetting c=1 / c=5 have source behavior still pending in
   this route,
2. or move to bag item-use from `bag.ui` if petsetting is considered closed.

Do not broaden `msgwarm.ui` further unless a concrete original-vs-rebuild
visual mismatch is captured.
