# 391 - Petsetting Kỹ Năng Row Deep Closeout

Ngày: 2026-07-15

Phạm vi: tab **Sủng vật** -> `petsetting.ui` -> row `4 Kỹ năng`.

Luật làm việc: audit source trước, chỉ smoke/headless PNG. Không mở live client.

## Source Path

| Source | Vai trò | Kết luận |
| --- | --- | --- |
| `game.k.bj()` | Open `/data/ui/skill.ui`, bind pet name/level/sprite/skill rows | PORTED/PARTIAL |
| `game.k.bk()` | Refresh description widget 9 for selected skill | PORTED/PARTIAL |
| `game.k.bo()` | Navigate up/down/left/right and back | PORTED |
| `game.i.F()` | Source skill count `P` | PORTED/PARTIAL |
| `game.i.t(slot)` | Source skill id lookup | PORTED |
| `aq.c[1]` | Skill name/description/PP/source params | PORTED for displayed name/description |
| `/data/ui/skill.ui` | Widget positions/styles for skill screen | PORTED/PARTIAL source-shaped renderer |

## Source Behavior Matrix

| Case | Source behavior | Rebuild behavior | Status |
| --- | --- | --- | --- |
| Open row 4 | `f=2`, `r=0`, open `skill.ui`, close `petsetting.ui` + `petstate.ui` | `sourceSkillVisible=true`, overlays closed | PORTED |
| Pet header | widget 12 pet name, widget 14 level, widget 16 sprite | Uses selected `SourcePetState` + species row | PORTED/PARTIAL |
| Skill rows | widgets 18..22 from `aq.c[1][skill][1]` | 5 UI rows; names from `BattleSkillRow.name()` | PORTED |
| Description | widget 9 from `aq.c[1][skill][2]` through source text runtime | Uses `BattleSkillRow.description()` | PORTED/PARTIAL |
| Direction navigation | `4100/8448/16400/32832` call list controller then `bk()` | up/down/left/right change selection and refresh description | PORTED |
| Back | `262144` refreshes `petstate.ui`, closes `skill.ui` | PORTED |
| Confirm | Source `bo()` has no confirm mutation path for this screen | Rebuild logs source no-op and stays visible | PORTED |
| Mouse hover | Source has no mouse; PC QoL preview only | Hover changes selected row, no confirm | PC_QOL PORTED |
| Mouse wheel | Source has no mouse; PC QoL preview only | Wheel changes selected row in non-scrollable 5-row list, no confirm | PC_QOL PORTED |

## PP / Value Clarification

`skill.ui` in `petsetting.ui` does **not** draw current PP/value labels in source.

Source `game.k.bj()` binds:

- pet name;
- pet level;
- pet sprite;
- skill names;
- selected skill description.

PP/value exists in pet runtime payload and battle `choiceskill.ui`, but not as visible text in this screen. Rebuild keeps PP data in `SourcePetState.skillCooldowns` and source payload, and smoke verifies payload PP for 5 slots. We do not add a fake PP column to `skill.ui`.

## Model Fix

Rebuild `SourcePetState.skillIds` was widened from 4 slots to 5 slots to match:

- source `skill.ui` widgets 18..22;
- source battle unit skill arrays;
- rebuild `BattleUnit` 5-slot model.

This removes the old gap where row 5 existed in UI but source pet model could never populate it.

## Smoke Coverage

Suite mới: `panel_petsetting_skill`.

| Checkpoint | Chứng minh |
| --- | --- |
| `panel_petstate_petsetting_skill_open` | opens `skill.ui`, closes parent overlays |
| `panel_petstate_petsetting_skill_data_binding_five_slots` | pet name/level/sprite, 5 skill slots, description, PP payload |
| `panel_petstate_petsetting_skill_navigation` | keyboard navigation refreshes selected row |
| `panel_petstate_petsetting_skill_mouse_wheel_non_scrollable_no_confirm` | wheel previews next row, no confirm/mutation |
| `panel_petstate_petsetting_skill_hover_preview_no_confirm` | hover previews row, no confirm/mutation |
| `panel_petstate_petsetting_skill_confirm_noop` | confirm does nothing because source has no confirm branch |
| `panel_petstate_petsetting_skill_back_returns_petstate` | back closes `skill.ui`, refreshes `petstate.ui` |

Smoke output:

`rebuild_game/build_intro_demo/panel_petsetting_skill_391_pre2`

## Current Status

Row `4 Kỹ năng` hiện đạt **PORTED/PARTIAL**:

- Source route, screen lifecycle, navigation/back, no-op confirm are source-backed.
- UI renderer uses decoded `skill.ui` widget positions and sprite cells in a source-shaped way.
- Data binding now supports 5 skill rows.
- Mouse hover/wheel behavior follows current PC QoL rule and never auto-confirms.

## Remaining Debt

| Debt | Ghi chú |
| --- | --- |
| Full Java ME widget VM | Renderer is source-shaped, not full `game.h/game.k` widget VM 100%. |
| Exact `an.a(...)` text substitution | Current text lookup is correct for normal rows, but full formatter runtime is not fully ported. |
| Pixel-perfect baseline/clip | Smoke image looks correct; exact original-vs-rebuild pixel compare remains UI-engine debt. |
| Skill replacement/learn UI | Not this screen. Learn/replace belongs to battle EXP/level-up `choiceskill.ui`, not `petsetting.ui` row 4. |

## Next Roadmap Step

Finish `petsetting.ui` row order:

1. Row `5 Tiến hóa` when evolution notice exists.
2. Then create a compact `petsetting.ui` all-row closeout matrix for rows 0..5.

