# Battle P16 Source Re-Audit Matrix

Date: 2026-07-08

Scope: battle item target/use flow only. This audit covers source `game.d` state
16, `game.h` item target UI/input, and `game.b.x/w` validation/apply behavior.
No intro/world/panel behavior is in scope.

## Summary

P16 is not a standalone choice-list target screen in the original game. The source
routes from P4 `/data/ui/choice.ui` item list into P16, then P16 opens
`/data/ui/petstate.ui` through `game.h.W()` so the player selects a pet target
from the same pet status screen family used by P5.

Current rebuild item validation/apply logic is already source-shaped for item
behaviors `1..6`, but the target UI remains PARTIAL because
`VqsvBattleRuntime.prepareItemTargetMenu()` still builds a generic menu instead
of the petstate screen.

Implementation update:

- 2026-07-08: P16 target screen now opens the shared petstate renderer, uses
  source-order pet rows, and shows action label `Sử dụng`.
- 2026-07-08 later: shared petstate renderer now reads visual widget ids,
  bounds, text widths, and `alt_image_ref` cells from
  `modules/ui/decoded/data__ui__petstate.ui.json` through `VqsvUiLayout`.
- Still PARTIAL: this is decoded-resource widget runtime for visual widgets,
  not a full binary `/data/ui/petstate.ui` parser with container navigation and
  all source style-state behavior.

## Source Entry Chain

| Step | Source | Fact | Status |
|---|---|---|---|
| P4 enter | `game.d.java:857..859` | `case 4` calls `S.aj()` to open item list. | VERIFIED |
| P4 UI | `game.h.java:3992..4003` | `aj()` opens `/data/ui/choice.ui`, sets headers `Đạo cụ`, `Số lượng`, action `Sử dụng`, then populates bag list. | VERIFIED |
| P4 confirm | `game.h.java:4014..4033` | `ak()` stores selected item id in `S.s`; item behaviors `7..10` are blocked in battle; otherwise calls `o.a((byte)16)` and closes `/data/ui/choice.ui`. | VERIFIED |
| P16 enter | `game.d.java:869..873` | `case 16`: `S.c = 0`, `S.l = false`, then `S.W()`. | VERIFIED |
| P16 update | `game.d.java:1649..1651` | battle update delegates to `S.al()`. | VERIFIED |

## P16 UI / Input Source

| Source | Fact | Rebuild Equivalent | Status |
|---|---|---|---|
| `game.h.java:1780..1783` | `W()` resets `b = 0`, then calls `e(c)`. | `prepareItemTargetMenu()` resets via `setMenu`, not petstate. | PARTIAL |
| `game.h.java:1785..1829` | `e(int)` opens `/data/ui/petstate.ui`, fills six rows, and in battle mode reads pets by `((d)o).f[i]`. | P5 uses petstate rows; P16 still generic target list. | PARTIAL |
| `game.h.java:1817..1823` | In battle facade, widget 63/64 hidden; if `o.Q == 4`, widget 75 text is `Sử dụng`; if `o.P == 5`, widget 75 text is `Xuất chiến`. | P5 has deploy/back labels; P16 needs action label `Sử dụng`. | PENDING |
| `game.h.java:4136..4147` | `al()` handles up/down and confirm; confirm calls `bo()`. | `tickItemTarget()` uses shared menu input. | PARTIAL |
| `game.h.java:4149..4165` | Back from P16 returns to P4 if no item has been successfully used; if `l == true`, it can return to P1/P4 depending active turn state. | Rebuild back returns to P4; success currently jumps P1 directly. | PARTIAL |

## game.h.bo() Target Validation / Apply

| Source | Fact | Status |
|---|---|---|
| `game.h.java:4046..4050` | On confirm, target is `q.z[c]` outside battle, or `q.z[((d)o).f[c]]` inside battle; validation is `pet.x(S.s)`. | VERIFIED |
| `game.h.java:4051..4089` | Validation warnings map codes `0,1,2,3,4,5,7,8` to `msgwarm.ui` messages. | VERIFIED |
| `game.h.java:4092..4098` | On valid item, source removes one item with `q.b(S.s, 1, 0)`; in battle it sets active pet `h.J = true` and calls target `w(S.s)`. | VERIFIED |
| `game.h.java:4099..4103` | After apply, source refreshes petstate via `e(c)`, sets `f=1`, `l=true`, opens `/data/ui/msgwarm.ui`, and shows `Thành công sử dụng đạo cụ`. | VERIFIED |
| `game.h.java:4111..4133` | Confirming warning/success closes msgwarm. If item missing (`f=2`), it reopens petstate and routes back to state 4/P1 depending active turn. | VERIFIED |

## game.b.x(item) Validation Matrix

Source: `game.b.java:1162..1211`.

| Item behavior `aq.c[4][item][5]` | Source validation | Warning code | Rebuild status |
|---|---|---:|---|
| any except revive when pet dead | `!S() && behavior != 4` | `8` | PORTED |
| `0` catch/ball style item | not a normal battle item | `6` | PORTED/PARTIAL: warning maps to generic cannot-use text. |
| `1` heal HP | HP already full | `2` | PORTED |
| `2` restore PP | all skill PP full | `3` | PORTED |
| `3` heal HP + restore PP | if PP not full, valid; if both HP/full-or-dead and PP full, code `7` | `7` | PORTED/PARTIAL: behavior wired, dedicated warning smoke still useful. |
| `4` revive | alive pet invalid | `1` | PORTED |
| `5` clear debuff | no active debuff | `4` | PORTED |
| `6` excitement/side flag | `d[6] >= 2` invalid | `5` | PORTED/PARTIAL: flag exists, full lifecycle still broader battle-status work. |
| default valid | returns `-1` | `-1` | PORTED |

## game.b.w(item) Apply Matrix

Source: `game.b.java:1121..1159`.

| Behavior | Source effect | Rebuild status |
|---|---|---|
| `1` | Heal `maxHP * param6 / 100 + param7`, call heal text/effect `l(s2)`. | PORTED/PARTIAL: HP changes; exact source visual text/effect is not full parity. |
| `2` | Restore PP by `param6` via `B(s3)`. | PORTED |
| `3` | Heal using `param6/7` and restore PP by `param8`. | PORTED |
| `4` | Revive/reset via `c()`, set HP, heal text/effect, restore PP. | PORTED/PARTIAL: revive stats are source-shaped. |
| `5` | Clear debuffs via `C()`. | PORTED |
| `6` | Set `d[6] = 2`. | PORTED/PARTIAL: flag set, full turn lifecycle belongs to active effect roadmap. |
| all valid behaviors | Source removes item again via `game.g.o().d(item,1,0)` inside `w()`. | PARTIAL: rebuild removes item once in runtime before/around apply to avoid double consume; source call ownership should be kept documented. |

## Current Rebuild Mapping

| Rebuild file | Current behavior | Status |
|---|---|---|
| `VqsvBattleRuntime.prepareItemMenu()` | P4 item list uses choice-style menu with item count and blocks behavior `7..10`. | PORTED/PARTIAL |
| `VqsvBattleRuntime.prepareItemTargetMenu()` | Builds petstate rows from source pet order, sets `battleUiMode = "petstate"`, and action label `Sử dụng`. | PORTED/PARTIAL. |
| `VqsvUiLayout` + `VqsvBattleRenderer.renderPetStateOverlay()` | Loads decoded `petstate.ui` visual widgets and renders frame/cells/text/progress by widget id. | PORTED/PARTIAL: visual widget runtime; binary parser/container/style states remain pending. |
| `VqsvBattleRuntime.tickItemTarget()` | Handles back/confirm, calls `validateBattleItem`, applies behavior `1..6`, consumes item, persists target, and returns to dispatch. | PORTED/PARTIAL |
| `VqsvBattleUnit.validateBattleItem()` | Mirrors `game.b.x(item)` behavior codes. | PORTED |
| `VqsvBattleUnit.applyBattleItem()` | Mirrors `game.b.w(item)` behavior `1..6`. | PORTED/PARTIAL |
| `VqsvBattleRenderer.renderPetStateOverlay()` | Existing source-shaped petstate renderer used by P5. | PORTED/PARTIAL, reusable for P16 visual slice. |

## Gaps / Risks

| Gap | Why it matters | Recommendation |
|---|---|---|
| Full binary P16 petstate widget runtime is not ported. | Source explicitly enters `W()` and opens `/data/ui/petstate.ui`; rebuild currently reads decoded visual widget data and keeps some source-shaped fallbacks. | Keep as PARTIAL until binary parser, container navigation, and style-state rendering exist. |
| P16 action label must be `Sử dụng`. | Source sets widget 75 by `o.Q == 4`. | PORTED in source-shaped renderer. |
| Warning confirm should preserve selected target and return to P16. | Source `bo()` closes msgwarm and returns to petstate flow. | Ensure warning return calls item target petstate prepare without resetting cursor unless source does. |
| Success route is simplified. | Source sets `l=true`, refreshes petstate, shows success msgwarm, then routes depending `h.J` and active pet. | Keep current logic for now if smokes depend on it; audit before changing turn consumption. |
| Item consume ownership differs. | Source removes in `bo()` then `w()` also calls global inventory remove; decompiled ownership may reflect shared bag path. | Do not change without a focused inventory parity audit. |

## Next Code Slice

Recommended next slice: **P16 petstate UI parity only**.

1. Change `prepareItemTargetMenu()` to prepare battle petstate rows using `sourcePetOrder`/`sourcePets`, set `battleUiMode = "petstate"`, reset cursor to `0`, and set action semantics to item-use.
2. Keep `tickItemTarget()` validation/apply code unchanged except for reading target from the same petstate row order.
3. Render action label as `Sử dụng` for P16 and `Xuất chiến` for P5.
4. Back from P16 should return to P4 item list.
5. Warning confirm should return to P16 petstate target UI and preserve the selected row where appropriate.

Smoke PNG after code:

- `battle_p16_target_petstate_ui.png`
- `battle_p16_item_heal_hp.png`
- `battle_p16_item_pp_restore.png`
- `battle_p16_item_hp_pp.png`
- `battle_p16_item_revive.png`
- `battle_p16_item_clear_debuff.png`
- `battle_p16_item_hp_full_warning.png`
- `battle_p16_item_pp_full_warning.png`
- `battle_p16_item_no_debuff_warning.png`

Regression:

- build
- `--check`
- `VqsvBattleDamageFormulaCheck`
- mojibake source scan
- route Sophie/Bunny/Elder smoke PNG only
