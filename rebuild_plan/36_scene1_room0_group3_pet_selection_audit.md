# Scene 1 Room 0 Group 3 Pet Selection Audit

Scope: `scene_1` room0 group3, records 0..17, manual rebuild path in
`rebuild_game/src/main/java/VqsvIntroDemo.java`.

## Source Evidence

- Event JSON: `modules/event/decoded/data__event__scene_1.mid.json`
- Source VM: `modules/source_code/decoded/decompiled_source_cfr/game/c.java`
- Manual rebuild: `rebuild_game/src/main/java/VqsvIntroDemo.java`

## Record Matrix

| Rec | Opcode | Args | Rebuild status |
|---:|---|---|---|
| 0 | 15 | `[1,0,2]` | PORTED: waits for group2 complete. |
| 1 | 2 | ids `53,54,55`, dirs `0,0,0`, states `1,1,1` | PORTED: shows three pet actors. |
| 2 | 38 | actors `53,54,55`, targets `4,8,12` | PORTED: waits for player interaction by source collision mask. |
| 3 | 4 | Penguin description | PORTED/APPROX: text content/layout via current dialog renderer. |
| 4 | 35 | choices `Co, Khong`, branches `6,2` | PORTED/APPROX: branch logic implemented; source `game.c` passes `game.h.a(aa,Z,...)`, so `[2,0]` means `aa=0` -> `/data/ui/option.ui`, `Z=2`; rebuild uses option.ui cells/coords, not full `ao/al`. |
| 5 | 87 | `[0,51,7,3,2,30,45,0]` | PORTED/APPROX: stores `SourcePetState`; full `game.g` pet inventory pending. |
| 6 | 41 | `[16]` | PORTED: jumps to record 15. |
| 7 | 4 | Frog description | PORTED/APPROX. |
| 8 | 35 | choices `Co, Khong`, branches `10,2` | PORTED/APPROX: same `option.ui` choice overlay. |
| 9 | 87 | `[0,17,7,3,2,10,45,0]` | PORTED/APPROX. |
| 10 | 41 | `[16]` | PORTED. |
| 11 | 4 | Dragon description | PORTED/APPROX. |
| 12 | 35 | choices `Co, Khong`, branches `14,2` | PORTED/APPROX: same `option.ui` choice overlay. |
| 13 | 87 | `[0,6,7,3,2,0,45,0]` | PORTED/APPROX. |
| 14 | 41 | `[16]` | PORTED. |
| 15 | 3 | hide `53,54,55` | PORTED. |
| 16 | 9 | `[2,0,0,0,0,0]` | PORTED/APPROX: routed through shared source `op9` helper; id 2 maps to rebuild fade-out. |
| 17 | 14 | complete group3 | PORTED: sets state `[1,0,3]=3`. |

## Important Caveat

After record 17 the original event flow can continue into room0 group6,
which starts with gate `[1,0,3]` and later uses `op9 [1,0,0,0,0,0]`.
Group6 is now present as a manual source-backed slice, but the full `game.d`
battle turn engine is still not ported. Smoke output must still be checked
for `state103=3` and `sourcePets=1`.

## Smoke Results

Commands were run from `rebuild_game` with `-Dvqsv.modules=..\modules`.

- `room0_after_return_no_dialog.png`: OK, `state103=0`, `sourcePets=0`.
- `room0_group2_first_dialog.png`: OK, reaches group2 first dialog.
- `room0_group3_pet_offer.png`: OK, pets 53/54/55 visible, `state103=0`.
- `room0_pet_choice_ui.png`: OK, choice visible, `state103=0`, `sourcePets=0`.
- `room0_after_pet_choice.png`: OK for Frog branch, `state103=3`, `sourcePets=1`; visual is black because group6 is still pending.
- `room0_after_pet_choice_penguin.png`: OK for Penguin branch, `state103=3`, `sourcePets=1`.
- `room0_after_pet_choice_dragon.png`: OK for Dragon branch, `state103=3`, `sourcePets=1`.
- `room0_pet_choice_ui_option_fixed.png`: OK, source-shaped `option.ui` choice visible; `state103=0`, `sourcePets=0`.
- `room0_after_pet_choice_option_fixed.png`: OK for Frog branch; `state103=3`, `sourcePets=1`.
- `room0_after_pet_choice_after_patch2.png`: OK for Frog branch; `state103=3`, `sourcePets=1`.

## Remaining Work

- PARTIAL: full `game.h`/`ao` widget renderer for opcode 35. Current rebuild uses source `option.ui` layout/cells and correct input/branches, not the original UI object stack.
- PENDING: full `game.g` pet inventory behavior for opcode 87.
- PARTIAL: exact source effect renderer for opcode 9. Current helper covers ids used here and documents unsupported ids.
- PARTIAL: room0 group6 elder battle chain exists as source-backed slice; full `game.d` turn engine remains pending.
