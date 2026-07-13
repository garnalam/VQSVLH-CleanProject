# 188 - Panel petsetting remaining c=1/c=5 audit

## Scope

Audit the remaining `petsetting.ui` branches after the completed `c=3` release-pet slice.

Focus:

- `petsetting c=1`
- `petsetting c=5`
- whether panel can move on to `bag.ui` item-use from the top-level panel menu.

Do not code in this audit. This is a source-backed route/status checkpoint.

## Source files read

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
  - `game.h.X()` petstate/petsetting input loop.
  - `game.h.bg()` evolve UI open.
  - `game.h.bh()` evolve/mutate confirm runtime.
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
  - `game.g.p(int)` party reorder helper.
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
  - `game.b.S()` alive check.
  - `game.b.R()` evolution/mutation availability kind.
- Rebuild:
  - `rebuild_game/src/main/java/VqsvIntroDemo.java`
  - `rebuild_game/src/main/java/VqsvSourceEvolutionRuntime.java`
  - `rebuild_game/src/main/java/VqsvSourceModels.java`
  - `rebuild_game/src/main/java/VqsvSmokeHarness.java`

## Current rebuild state

Already ported or ported/partial in panel `petsetting.ui`:

| Row | Source meaning | Rebuild status |
| --- | --- | --- |
| `c=0` | `Dao cu` -> `choice.ui` item-use list and confirm loop | `PORTED/PARTIAL` |
| `c=2` | `Vat pham trang suc` -> `choice.ui` equipment equip/unequip/transfer | `PORTED/PARTIAL` |
| `c=3` | `Phong sinh` -> `msgconfirm.ui`, success mutation, warnings | `PORTED` for current modeled state |
| `c=4` | `Ky nang` -> `skill.ui` render/navigate/back | `PORTED/PARTIAL` |

Still pending:

| Row | Source meaning | Rebuild status |
| --- | --- | --- |
| `c=1` | `Chien dau` / set selected pet as first active party pet | `PENDING` |
| `c=5` | `Tien hoa` or `Di hoa`, only visible when source `R()` returns `1` or `2` | `PORTED/PARTIAL entry assets exist, petsetting direct route PENDING` |

## Source route: opening petsetting.ui

In `game.h.X()`, when the owner is `game.k` and `o.Q == 6 || o.Q == 0`, confirm from `petstate.ui` opens `/data/ui/petsetting.ui`.

Source initialization:

- `this.c = 0`
- `this.o.m()`
- `this.f = 1`
- load `/data/ui/petsetting.ui`
- widget/list selection `0.f = c`
- if selected pet `R() == 2`:
  - widget `9` text = `Di hoa`
  - row count = `6`
- else if selected pet `R() == 1`:
  - widget `9` text = `Tien hoa`
  - row count = `6`
- else:
  - widget `9` text = empty
  - row count = `5`

Rebuild already mirrors this with:

- `openSourcePetSettingFromPetstate()`
- `VqsvSourceEvolutionRuntime.noticeForPet(...)`
- `sourcePetSettingCount = 5 or 6`
- `sourcePetSettingActionLabel(5)` returning `Tien hoa` / `Di hoa`

Status: `PORTED/PARTIAL`. The row visibility is source-shaped, but row `c=5` confirm is not wired from petsetting.

## Branch c=1: set active/lead pet

### Source behavior

Inside `game.h.X()`, `f == 1`, `o.Q == 6 || o.Q == 0`, confirm row `c == 1`:

1. If selected pet is dead:
   - condition: `!this.q.z[this.b].S()`
   - `game.b.S()` returns `this.d[1] > 0`
   - set `f = 2`
   - load `/data/ui/msgwarm.ui`
   - message source text: `Sung vat nay khong the tham chien`
   - prompt source text: `Nhan nut 5 de tiep tuc`
   - close `/data/ui/petsetting.ui`
   - set `b = 0`

2. Else if selected pet is already row `0`:
   - condition: `this.b == 0`
   - set `f = 2`
   - set `b = 0`
   - load `/data/ui/msgwarm.ui`
   - message source text: `Sung vat nay da xuat chien`
   - prompt source text: `Nhan nut 5 de tiep tuc`
   - close `/data/ui/petsetting.ui`

3. Else valid switch:
   - call `this.q.p(this.b)`
   - `game.g.p(int)` moves selected `z[n]` to `z[0]` and shifts earlier pets down one slot.
   - set `f = 0`
   - set `b = 0`
   - refresh petstate with `e(b)`
   - close `/data/ui/petsetting.ui`
   - reset list cursor widget fields `0.f = 0`, `0.e = 0`

### Rebuild gap

Current `tickSourcePetSetting()` handles rows `0`, `2`, `3`, `4`.

Row `1` falls through to:

`PENDING panel game.h.X petsetting confirm c=1 action=Chien dau subflow not mutated in petsetting shell slice`

Smoke currently has `panel_petstate_petsetting_confirm_pending` expecting this pending behavior.

### Required rebuild behavior

Implement as a small panel-only slice:

- Do not involve battle P5.
- Do not invoke `SourceBattleRuntime`.
- Use `sourcePayload[6] > 0` / existing `sourcePetLiving(...)` for `game.b.S()`.
- If selected index is `0`, show `msgwarm.ui` with exact source text and close `petsetting.ui`.
- If selected pet is dead, show `msgwarm.ui` with exact source text, close `petsetting.ui`, and set `battleMenuIndex = 0`.
- Else reorder `sourcePets` with source-equivalent move-selected-to-front.
- Reassign `SourcePetState.slot = index` after reorder.
- Set `battleMenuIndex = 0`.
- Hide `sourcePetSettingVisible`.
- Refresh `worldPetstateVisible` rows via the existing petstate renderer.

Status: `PENDING`, important. Do not skip to bag.ui before this unless explicitly choosing to defer active-pet panel behavior.

## Branch c=5: direct evolve/mutate row

### Source behavior

Row `c == 5` exists only when selected pet `R()` returns `1` or `2`.

Source `game.b.R()`:

- if no next species: `0`
- if next species kind is `1` or `2`: `1` (`Tien hoa`)
- if next species kind is `3`: `2` (`Di hoa`)
- else `0`

Confirm row `c == 5`:

- `this.o.m()`
- `this.bg()`

Source `bg()`:

- set `f = 2`
- set `r = 0`
- load `/data/ui/evolve.ui`
- close `/data/ui/petsetting.ui`
- close `/data/ui/petstate.ui`
- widget `10`: current pet sprite/model
- widget `38`: current species name
- widget `40`: current level
- widget `45`: required material name
- widget `46`: owned material count / required material count
- widgets `19..22` and `31..34`: old/new visible stat comparison
- set `g = true`

Then `bh()` handles confirm in `evolve.ui`:

- validates material/evolve state through source pet/table data
- runs AH type 10 visual effect
- mutates pet species and payload
- refreshes widgets
- opens `msgwarm.ui` success text:
  - `Tien hoa thanh #2...` or `Di hoa thanh #2...`

### Rebuild state

Rebuild already has a `VqsvSourceEvolutionRuntime` and `openSourceEvolveUi(int petIndex)`:

- `evolve.ui` visibility and panel fields exist.
- `noticeForPet(...)` maps current species -> target species and material requirements from source tables.
- `tickSourceEvolve()` has warning/success/material consume/mutation behavior.
- Tutorial/level-up evolution flow already uses the runtime.

But `tickSourcePetSetting()` does not route `sourcePetSettingIndex == 5` into `openSourceEvolveUi(...)`.

### Required rebuild behavior

Implement after c=1, as a separate slice:

- In `petsetting c=5`, call the existing `openSourceEvolveUi(battleMenuIndex)`.
- Ensure it also closes `sourcePetSettingVisible` and `worldPetstateVisible`, matching `bg()`.
- Confirm `o.m()` side effect is either already represented by refresh fields or explicitly traced.
- Add direct-panel smokes separate from level-up tutorial smokes:
  - open evolve row from petsetting
  - back returns/clears correctly
  - insufficient material warning
  - success mutation if source-backed fixture has material

Status: `PENDING/PARTIAL`. Runtime exists, but direct source route from `petsetting.ui` is still not wired.

## Decision: can we move to bag.ui item-use now?

No, not cleanly.

There are still two source-backed `petsetting.ui` branches after `c=3`:

1. `c=1` active/lead pet switch: important gameplay state, small and low-risk.
2. `c=5` direct evolve/mutate entry: important when row count is 6, but larger and should follow c=1.

Bag UI item-use from top-level panel should come after at least `c=1`, unless we intentionally defer c=5 with an explicit `PENDING/PARTIAL` note.

## Recommended next implementation slice

Implement `petsetting c=1` first.

Why:

- Source chain is small and fully proven.
- It only mutates party order and current selection.
- It uses existing `msgwarm.ui` runtime.
- It closes a visible pending row in current `petsetting.ui`.
- It is safer than direct evolution because it does not touch material consume, AH type 10, or species mutation.

Smoke PNG checkpoints to add:

- `panel_petstate_petsetting_active_switch_success`
- `panel_petstate_petsetting_active_dead_warning`
- `panel_petstate_petsetting_active_already_warning`
- regression:
  - `panel_petstate_petsetting_release_success_removes_pet`
  - `panel_petstate_petsetting_item_choice_success_msg`
  - `panel_petstate_petsetting_equipment_choice_equip_success_msg`
  - `panel_petstate_petsetting_skill_open`
  - panel save smoke
  - Sophie/Bunny/Elder route smoke

After that:

1. Audit/port `petsetting c=5` direct evolve entry.
2. Then move to top-level `bag.ui` item-use from panel if no higher-priority panel branch remains.

