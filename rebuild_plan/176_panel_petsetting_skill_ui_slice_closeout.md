# 176 - Panel petsetting c=4 -> skill.ui source-backed slice

Date: 2026-07-10

## Scope

Small read-only panel slice:

- `gamemenu b=1 -> P=7 -> petstate.ui`
- confirm pet -> `petsetting.ui`
- `petsetting c=4` -> `/data/ui/skill.ui`
- render/navigate/back only

No skill use, skill replacement, item/equip/release/evolve mutation is included here.

## Source Chain

Primary source:

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`

Confirmed in `game.h.X()`:

- In `petsetting.ui` state `f=1`, `c=4`:
  - `f = 2`
  - `r = 0`
  - open `/data/ui/skill.ui`
  - close `/data/ui/petsetting.ui`
  - close `/data/ui/petstate.ui`
  - widget `12` = selected pet name
  - widget `14` = selected pet level
  - widget `16` = selected pet sprite `C`
  - widgets `18..` = skill names from `aq.c[1][skill][1]`
  - call `bf()`
- In `skill.ui` branch:
  - up/down/left/right call `p.a.b(0..3)` and `bf()`
  - back calls `e(b)` then closes `/data/ui/skill.ui`
- `bf()`:
  - if selected skill exists, widget `9` = `an.a(aq.c[1][skill][2], {"Nhất định", "Nhất định"})`
  - otherwise widget `9` = empty

## UI Mapping

Decoded UI:

- `modules/ui/decoded/data__ui__skill.ui.json`

Widgets used:

- frame/static: `1`, `2`, `3`, `4`, `8`, `10`, `11`, `15`
- title/softkeys: `5`, `6`, `7`
- description: `9`
- pet name/level/sprite: `12`, `13`, `14`, `16`
- skill rows: `18`, `19`, `20`, `21`, `22`

## Rebuild Status

Implemented:

- `petsetting c=4` opens `skill.ui` and closes `petsetting.ui` plus `petstate.ui`, matching the source call sequence.
- Skill rows render from `SourcePetState.skillIds` through `BattleSkillRow.name()`.
- Pet name/level/sprite render from the selected source pet and species table.
- Direction keys navigate the selected skill row and refresh description.
- Back closes `skill.ui` and refreshes world `petstate.ui`.
- Confirm logs a pending trace and does not mutate state.

Status:

- `PORTED/PARTIAL`: source route, widget mapping, navigation/back.
- `PORTED/PARTIAL`: description uses `BattleSkillRow.description()` for `aq.c[1][skill][2]`; full `an.a(...)` runtime substitution is not fully ported.
- `PORTED/PARTIAL`: source UI has 5 rows, current `SourcePetState` stores 4 skill slots, so row 5 renders empty until the pet model is widened from source proof.
- `PENDING`: original-vs-rebuild pixel compare.

## Verification

Focused PNG smoke:

- `panel_petstate_petsetting_skill_open`
- `panel_petstate_petsetting_skill_navigation`
- `panel_petstate_petsetting_skill_back_returns_petstate`
- `panel_petstate_petsetting_skill_confirm_pending`

Regression to keep:

- petstate open/navigation/back
- petsetting open/navigation/back
- bag/task/petmap/save/system option
- Sophie/Bunny/Elder route smoke

## Next

Next recommended panel slice:

1. Audit `petsetting c=2` equipment `choice.ui` path or `petsetting c=0` item-use path from source.
2. Only after the remaining important source panel branches are either ported or explicitly deferred, audit the modded speed toggle.
