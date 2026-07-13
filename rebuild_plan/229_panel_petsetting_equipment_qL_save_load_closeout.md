# 229 - Panel Petsetting Equipment q.L Save/Load Closeout

Status: PORTED/PARTIAL / SOURCE-SHAPED SAVE COVERAGE.

Purpose:

- Close the item-roadmap gap called out in `227`: equipment inventory `q.L`
  existed at runtime but was not persisted.
- Keep this slice limited to equipment save/load state. Do not mix in q.N
  special rows, battle passive/stat effects, or generic RMS parity.

## Source Anchors

- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `rebuild_plan/180_panel_petsetting_equipment_choice_audit.md`
- `rebuild_plan/181_panel_petsetting_equipment_choice_render_slice_closeout.md`
- `rebuild_plan/182_panel_petsetting_equipment_confirm_slice_closeout.md`
- `rebuild_plan/227_battle_item_full_completion_matrix.md`

## Source Facts

Source `q.L` equipment rows are:

```text
[equipmentId, equippedFlag, 0]
```

Relevant source behavior:

- `game.g.c(id, qty, 2)` adds equipment rows to `q.L` for `id < 12`.
- `game.g.l(id)` clears the row equipped flag.
- `game.g.f(id, petIndex)` clears any previous holder, marks the selected row
  equipped, and sets selected pet `c[5] = id`.
- Pet `c[5]` is mirrored in rebuild payload index `2`.

## Implemented

Files:

- `rebuild_game/src/main/java/VqsvSaveRuntime.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Runtime:

- Added `equipment.count` and `equipment.N` properties to save.
- Restores `sourceEquipmentItems` as source-shaped `q.L` rows.
- Existing pet payload save/load continues to persist selected pet `c[5]`
  mirror at payload index `2`.

Smoke checkpoint:

```text
panel_petstate_petsetting_equipment_save_load_qL
```

The checkpoint:

1. Opens petsetting equipment choice.
2. Equips row `2` to selected pet.
3. Saves.
4. Clears in-memory equipment/pet state.
5. Loads.
6. Asserts:
   - `q.L` rows restored;
   - row `0` no longer equipped;
   - row `1` still equipped by pet 1;
   - row `2` equipped by pet 0;
   - pet payload `[2]` restored for both pets.

## Status

- `PORTED/PARTIAL`: q.L equipment list save/load.
- `PORTED/PARTIAL`: pet equipment slot payload `[2]` survives save/load.
- `PENDING`: bank/storage-side equipment ownership parity.
- `PENDING`: equipment stat/passive effects in battle.
- `PENDING`: full original RMS byte/vector parity.
- `PENDING`: original-client pixel compare for related UI.

## Verification

Focused PNG:

```text
panel_petstate_petsetting_equipment_save_load_qL PASS
```

Output:

```text
rebuild_game/build_intro_demo/229_equipment_qL_save_load.png
```

Regression:

```text
build.ps1                                      PASS
com.vqsv.rebuild.Main --check                 PASS
VqsvBattleDamageFormulaCheck                  PASS
Java/doc mojibake scan                        PASS, no matches
equipment choice focused checkpoints          PASS 7/7
VqsvIntroDemo --smoke-suite panel_wheel       PASS 8/8
VqsvIntroDemo --smoke-suite battle_quick      PASS 20/20
```

## Next

Recommended next item-roadmap choices:

1. Audit remaining q.N special rows from `196_panel_bag_qN_remaining_rows_audit.md`.
2. Or add save/load smoke for existing q.N slices: egg/hatch and ride.

Do not jump to skill completion until item persistence coverage is stable.
