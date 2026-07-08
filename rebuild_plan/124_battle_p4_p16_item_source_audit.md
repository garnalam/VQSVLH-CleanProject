# 124 Battle P4/P16 Item Source Audit

Status: SOURCE AUDIT ONLY / NO CODE.

Purpose:

- Audit battle item flow before changing code.
- Scope is only `game.d` P4/P16, `game.h` item UI facade, and `game.b.x/w`.
- Do not touch intro/world/panel/catch/pet-switch in this audit.

Source files:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
- `modules/script/decoded/data__script__db.mid.json` group `4`

Rebuild files audited:

- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleUnit.java`
- `rebuild_game/src/main/java/VqsvBattleRenderer.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

## Source Entry Chain

| Step | Source | Fact |
| --- | --- | --- |
| P4 enter | `game.d case 4` | Calls `this.S.aj()`. |
| P4 update | `game.d case 4` update | Calls `this.S.ak()`. |
| P16 enter | `game.d case 16` | Sets `S.c = 0`, `S.l = false`, then calls `S.W()`. |
| P16 update | `game.d case 16` update | Calls `this.S.al()`. |
| P4/P16 draw | `game.d` draw switch | P4/P16 rely on `game.h` UI overlays rather than custom battle draw. |

## P4 Source UI / Input

Source `game.h.aj()` / item list:

| Source behavior | Details | Rebuild status |
| --- | --- | --- |
| Open `/data/ui/choice.ui` | Header widget 8 = `Đạo cụ`; widget 9 = `Số lượng`; action widget 5 = `Sử dụng`. | PORTED/PARTIAL |
| Populate list from `q.J` | Row icon uses `aq.c[4][item][1]`; row name uses `aq.c[4][item][0]`; row value is count. | PORTED/PARTIAL |
| Description widget | Widget 53 uses `aq.c[4][selectedItem][2]`. | PORTED/PARTIAL |
| Hide widgets 59/60 | Source disables extra widgets for this list. | PARTIAL/manual renderer |
| Up/down | `ak()` handles key 4100/8448 through choice list. | PORTED through shared menu |
| Confirm | Source stores selected item id in `S.s`. | PORTED as `selectedItemId` |
| Block behaviors 7..10 | Opens `msgwarm.ui`: `Trong chiến đấu không thể sử dụng`. | PORTED/PARTIAL |
| Valid item | Switches owner to state 16 and closes `choice.ui`. | PORTED |
| Back | Closes `choice.ui`, returns owner state 20. | PORTED |

## P16 Source UI / Input

Source `game.h.W()` / `e(c)`:

| Source behavior | Details | Rebuild status |
| --- | --- | --- |
| `W()` resets list cursor | `this.b = 0`, then `this.e(this.c)`. | PORTED/PARTIAL |
| Opens `/data/ui/petstate.ui` | P16 is petstate target selection, not choice list. | PORTED/PARTIAL |
| Battle rows use `((d)o).f[i]` | Pet rows are displayed by battle party order, not arbitrary world order. | PARTIAL: rebuild uses `sourcePetOrder` proxy |
| Hide widgets 63/64 in battle | Source hides non-battle controls. | PARTIAL/manual renderer |
| Action label | If `o.Q == 4`, widget 75 text is `Sử dụng`. | PORTED/PARTIAL |
| Confirm | `al()` calls `bo()`. | PORTED |
| Back before success | Returns to P4 and closes petstate. | PORTED |
| Back after success (`l == true`) | If active pet was used, source may refresh active display, increment turn, enter P1; otherwise returns P4. | PARTIAL |

## `game.h.bo()` Validation / Apply Flow

Source `bo()` behavior:

1. If `f == 0`, set `f = 1`.
2. Resolve target:
   - world: `q.z[c]`
   - battle: `q.z[((d)o).f[c]]`
3. Call target `x(S.s)`.
4. If validation returns a warning code, open `/data/ui/msgwarm.ui` and return.
5. If valid and bag has item via `q.b(S.s,1,0)`:
   - world: `q.z[c].w(S.s)`
   - battle: set `((d)o).h.J = true`, then `q.z[((d)o).f[c]].w(S.s)`
   - refresh petstate via `e(c)`
   - set `f = 1`, `l = true`
   - open `msgwarm.ui` success text: `Thành công sử dụng đạo cụ`
6. If item missing:
   - set `f = 2`
   - open missing item warning.
7. Confirming warning/success:
   - `f == 1`: closes `msgwarm.ui`, keeps current UI flow.
   - `f == 2`: closes warning and petstate; routes back according to active turn.

Important source detail:

- A successful item use does **not** immediately jump straight to P1 visually.
  It refreshes petstate and shows a success `msgwarm.ui` first.
- Rebuild currently applies the item and enters P1 dispatch immediately. This
  is the biggest P16 flow parity gap.

## `game.b.x(item)` Validation Matrix

Source: `game.b.x(int itemId)`.

| Item behavior `aq.c[4][item][5]` | Source validation | Return code | Rebuild status |
| ---: | --- | ---: | --- |
| any except revive | If pet dead and behavior != 4 | `8` | PORTED |
| 0 | Ball/catch item used as normal item | `6` | PORTED/PARTIAL warning |
| 1 | Heal HP when HP already full | `2` | PORTED |
| 2 | Restore PP when all PP full | `3` | PORTED |
| 3 | HP+PP item; if PP not full valid, if HP+PP full return combined warning | `7` | PORTED/PARTIAL; needs direct checkpoint |
| 4 | Revive item used on alive pet | `1` | PORTED |
| 5 | Clear debuff with no debuffs | `4` | PORTED |
| 6 | Excitement/state flag already >= 2 | `5` | PORTED/PARTIAL; no current source item smoke |
| valid | No warning | `-1` | PORTED |

Warning text source mapping in `game.h.bo()`:

| Code | Source warning meaning |
| ---: | --- |
| `0` | Pet is dead, cannot use. |
| `1` | Pet does not exist / cannot use. |
| `2` | HP full. |
| `3` | Skill/PP value full. |
| `4` | No negative effects. |
| `5` | Excited, cannot use. |
| `7` | HP and skill are both full. |
| `8` | Pet is dead, cannot use. |

## `game.b.w(item)` Apply Matrix

Source: `game.b.w(int itemId)`.

| Behavior | Source effect | Rebuild status |
| ---: | --- | --- |
| 1 | Heal `maxHP * param6 / 100 + param7`; calls `u(oldHp + heal)` then `l(heal)`. | PORTED/PARTIAL; HP changes, `Q` pre-display field not modeled |
| 2 | Restore PP by `param6` via `B(param6)`. | PORTED |
| 3 | Heal by `param6/7` and restore PP by `param8`. | PORTED |
| 4 | Revive/reset via `c()`, set HP, heal text/effect, restore PP. | PORTED/PARTIAL |
| 5 | Clear debuffs with `C()`. | PORTED |
| 6 | Set `d[6] = 2`. | PORTED/PARTIAL |
| all valid | Source calls `game.g.o().d(item,1,0)` after apply. | PARTIAL: rebuild consumes once in runtime |

## Current Rebuild Mapping

| Rebuild method | Source equivalent | Status |
| --- | --- | --- |
| `prepareItemMenu()` | `game.h.aj()` | PORTED/PARTIAL |
| `tickItemList()` | `game.h.ak()` | PORTED/PARTIAL |
| `prepareItemTargetMenu()` | `game.d case 16` + `game.h.W()/e(c)` | PORTED/PARTIAL |
| `tickItemTarget()` | `game.h.al()/bo()` | PORTED/PARTIAL |
| `BattleUnit.validateBattleItem()` | `game.b.x(item)` | PORTED |
| `BattleUnit.applyBattleItem()` | `game.b.w(item)` | PORTED/PARTIAL |
| `renderPetStateOverlay()` | `/data/ui/petstate.ui` visual path | PORTED/PARTIAL |

## Current Smoke Coverage Found

Existing checkpoints in `VqsvSmokeHarness`:

- `battle_elder_item_p4`
- `battle_elder_item_target_p16`
- `battle_p16_target_petstate_ui`
- `battle_p16_item_heal_hp`
- `battle_p16_item_pp_restore`
- `battle_p16_item_hp_pp`
- `battle_p16_item_revive`
- `battle_p16_item_clear_debuff`
- `battle_p16_item_hp_full_warning`
- `battle_p16_item_pp_full_warning`
- `battle_p16_item_no_debuff_warning`

Coverage gaps:

- P4 block behavior `7..10` warning checkpoint.
- P16 success `msgwarm.ui` before turn advances.
- P16 warning confirm returns to petstate and preserves cursor.
- P16 back before success returns to P4.
- Validation code `7` HP+PP full warning direct checkpoint.
- Behavior `6` item if a real source item row exists in current db.

## Gaps / Risk Ranking

| Priority | Gap | Why |
| --- | --- | --- |
| P1 | P16 success flow jumps directly to P1 in rebuild | Source refreshes petstate and shows success `msgwarm.ui` first. |
| P1 | Warning confirm/cursor preservation | Source keeps target flow after warning; rebuild likely returns via warning state but needs checkpoint. |
| P2 | P4 behavior 7..10 blocked warning | Logic exists but needs source-backed checkpoint. |
| P2 | Validation code `7` direct smoke | Behavior 3 combined HP+PP full edge is easy to regress. |
| P3 | Full binary `choice.ui/petstate.ui/msgwarm.ui` runtime | Larger UI engine work, not necessary before logic parity. |
| P3 | `game.g.o().d()` inventory ownership | Rebuild consumes once; source/decompile appears to remove in global path. Needs focused inventory audit before changing. |

## Recommended Next Code Slice

Slice P4/P16-A: close the P16 success/warning/back flow, not item formulas.

Tasks:

1. Keep `game.b.x/w` logic unchanged.
2. After valid item apply:
   - consume item;
   - persist target;
   - refresh P16 petstate rows;
   - show success `msgwarm.ui`;
   - defer turn transition until success message confirm.
3. Warning confirm:
   - return to P16 petstate target UI;
   - preserve selected row.
4. Back before success:
   - return to P4 item list.
5. Add/check smoke:
   - `battle_p16_item_success_msgwarm`
   - `battle_p16_success_confirm_to_p1`
   - `battle_p16_warning_return_petstate_preserve_cursor`
   - `battle_p16_back_returns_p4`
   - `battle_p4_blocked_item_warning`
   - `battle_p16_item_hp_pp_full_warning`

Regression:

- build
- Java mojibake scan
- `com.vqsv.rebuild.Main --check`
- `VqsvBattleDamageFormulaCheck`
- Catch closeout route
- EXP closeout route

