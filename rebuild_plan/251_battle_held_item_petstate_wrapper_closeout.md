# 251 - Held Item Wrapper + Petstate Widget 59/60 Closeout

Date: 2026-07-13

Scope: code slice after `250_battle_held_item_source_pipeline_audit.md`.

## Goal

Make rebuild petstate read held item/accessory rows from source table `aq.c[3]`, not
normal item table `aq.c[4]`.

This is required before porting more held item/passive battle effects, because every
future smoke must show the correct icon/name in petstate widget `59/60`.

## Code Changes

| File | Change |
| --- | --- |
| `rebuild_game/src/main/java/VqsvBattleTables.java` | Added `BattleHeldItemRow` and `VqsvBattleTables.heldItem(id)` wrapper for `aq.c[3]`. |
| `rebuild_game/src/main/java/VqsvBattlePetStateView.java` | Petstate held item view now uses `heldItem(id)`, with icon cell from `aq.c[3][id][1]` and name from `aq.c[3][id][0]`. |
| `rebuild_game/src/main/java/VqsvBattleRenderer.java` | Petstate detail row now renders held item text only; widget `59` icon data stays in the view model for source/list parity but is not drawn in the detail panel. |
| `rebuild_game/src/main/java/VqsvSourceOps.java` | Source equipment helpers now use `BattleHeldItemRow` instead of direct raw row reads. |
| `rebuild_game/src/main/java/VqsvSmokeHarness.java` | Added smoke checkpoint `panel_petstate_held_item0_widget_59_60`; updated held item 0 audit overlay to use wrapper. |

## Smoke

Checkpoint:

```text
panel_petstate_held_item0_widget_59_60
```

PNG:

```text
rebuild_game/build_intro_demo/panel_petstate_held_item0_widget_59_60.png
```

What it verifies:

- selected pet payload index `2` / source `c[5]` is set to held item id `0`;
- petstate view model has `heldItemId = 0`;
- widget `59` icon cell comes from `aq.c[3][0][1]`;
- widget `60` text comes from `aq.c[3][0][0]`;
- rendered petstate frame visually shows `Mạn Đà La Thạch` as text only in the `Mang` row, without drawing the held item icon.

## Verification

Passed:

- `build.ps1`
- `java -cp build/classes VqsvIntroDemo --smoke-checkpoint panel_petstate_held_item0_widget_59_60 build_intro_demo/panel_petstate_held_item0_widget_59_60.png`
- `java -cp build/classes com.vqsv.rebuild.Main --check`
- `java -cp build/classes VqsvBattleDamageFormulaCheck`
- Java mojibake scan. Hits were existing valid Vietnamese intro text only.

Additional UI preference update:

- Removed the held item icon from the petstate detail `Mang` row.
- Re-ran `panel_petstate_held_item0_widget_59_60`, `--check`, and `VqsvBattleDamageFormulaCheck`.

## Status

`PORTED` for shared held item table wrapper and petstate widget `59/60` data source.

Remaining naming cleanup:

- `BattleUnit.STAT_FORM` still mirrors source `game.b.c[5]`, but should eventually be renamed or wrapped as held item/passive id.
- `BattleUnit.hasFormStatus(byte)` still works behaviorally but should eventually become `hasHeldItem(byte)` or similar.

## Next Roadmap Step

Proceed to held item/passive id `4` - `Viễn Cổ Long Cốt`:

```text
crit chance += 10 percentage points
```

Required first step: source audit and deterministic smoke proving the +10 percentage-point crit window.
