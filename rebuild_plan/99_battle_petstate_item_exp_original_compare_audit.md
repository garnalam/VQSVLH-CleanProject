# 99 Battle Petstate / Item / EXP Original Compare Audit

## Scope

User supplied 9 original-game screenshots for battle/world UI parity:

1. World/non-battle `petstate.ui`.
2. World/non-battle `petstate.ui` with Dien Mieu.
3. Battle entry/HUD/base markers.
4. NPC battle catch-forbidden warning.
5. Battle item list `choice.ui`.
6. Battle pet switch/state `petstate.ui`.
7. Forced pet switch after active pet death.
8. Dead pet selected warning.
9. Win EXP gain after defeating enemy.

This audit is source-first and records current rebuild gaps before code fixes.

Status key: PORTED / PORTED-PARTIAL / APPROX / STUB / PENDING / UNKNOWN.

## Source Anchors

### World petstate

Source: `modules/source_code/decoded/decompiled_source_cfr/game/h.java`

- `game.h.W()` resets selection and calls `e(this.c)`.
- `game.h.e(int)` loads `/data/ui/petstate.ui` with sprite `257`.
- For world owner `game.k`, rows read `game.g.q.z[i]` directly.
- `game.h.y()/aV()` handles pet bank state, also loading `/data/ui/petstate.ui`.
- Detail fill uses widget ids `48/51/52/59/60/61/62/65..74`.

Source data meaning:

- `game.b.L()` = current HP percent from `d[1] / c[1]`.
- `game.b.O()` = EXP percent from `S / u()`.
- `game.b.P()` serializes species, level, held/form field, status field, quality, nature, current HP, EXP, visual/evolution field, skills, current PP.
- `game.b.I()` recovers HP and PP through opcode 39.

Rebuild status: PENDING for world/non-battle `petstate.ui`.

Current rebuild has a battle-only P5 renderer. It does not expose the same UI outside battle/panel flow.

### Battle entry/HUD

Source: `game.d.a(byte)` state P0/P20 plus draw `game.d.b(Graphics)`.

- Entry states move actor sprites by source `an/cpos` tables.
- Draw includes battle background, actors, marker sprites `al[]`, pet sprites, HUD, command bar.
- `game.d` P20 draws battle scene + command UI.

Rebuild status: PORTED-PARTIAL / APPROX.

Current smoke `audit99_battle_entry_both_landed.png` shows both pets and HUD, but background is abstract bands, not source battle/world backing. Ground markers exist but placement/scale still not pixel-perfect.

### P21 catch list and catch-forbidden warning

Source:

- `game.d.a(21)` targets `d[0]`, sets `h.p`, calls `S.ah()`.
- `game.h.ah()` loads `/data/ui/choice.ui`, title `"Pokemon ball"`, subtitle `"Ti le bat"`, action `"Su dung"`, fills icons from sprite `258`, item names, catch percent, and selected count text widget `53`.
- `game.h.ai()` confirms: checks count through `q.b(item,1,0)`, sets `game.d.l`, removes item, enters P17. Missing-count warning is `"So luong Pokemon ball khong du"`.

Rebuild:

- `VqsvBattleRuntime.tickCommand()` blocks catch when `battleMode[0] == 2` and shows `VqsvText.Battle.CATCH_NOT_ALLOWED`.
- `prepareCatchMenu()` uses current bag ball items and `choice.ui`-like renderer.

Status:

- Catch list: PORTED-PARTIAL.
- Missing count warning: PORTED-PARTIAL, smoke exists.
- NPC catch-forbidden branch: PORTED-PARTIAL in rebuild code, but source condition/setup for the exact NPC-battle flag still needs a route-specific audit/smoke. Text exists in rebuild; direct source string search did not locate it in the decompiled `game.h/game.d` chunks inspected.

Gap from screenshot 4:

- Need focused smoke for NPC battle catch forbidden, not just missing ball.
- Warning box currently source-shaped but small/position may differ from original `msgwarm.ui`.

### P4 item list

Source:

- `game.d.a(4)` calls `S.aj()`.
- `game.h.aj()` loads `/data/ui/choice.ui`, title `"Dao cu"`, subtitle `"So luong"`, action `"Su dung"`, hides widgets `59/60`, then `be()` fills item rows.
- `game.h.ak()` blocks item behaviors `7..10` in battle and routes usable items to P16.

Rebuild:

- `VqsvBattleRuntime.prepareItemMenu()` lists `sourceBagItems` where item count > 0 and item behavior != 0.
- `VqsvBattleRenderer.drawChoiceOverlay()` approximates `choice.ui`.

Status: PORTED-PARTIAL / APPROX.

Current smoke `audit99_battle_elder_item_p4.png` proves the list opens, but compared with original screenshot 5 it is missing/weak:

- source-like lower description panel text for selected item is absent;
- exact row frame/cell placement is off;
- item icon/name/count are present but not full `choice.ui` runtime;
- background/HUD layering is still rebuild battle approximation.

### P16 item target

Source:

- `game.d.a(16)` sets `S.c=0`, `S.l=false`, calls `S.W()`.
- Therefore P16 target selection uses `/data/ui/petstate.ui`, not `choice.ui`.
- `game.h.bo()` validates item on selected pet via `game.b.x(itemId)`, then applies `game.b.w(itemId)` and consumes item.

Rebuild:

- Item behavior `1..6` is mostly wired.
- Target menu currently uses generic menu setup before P16, not a full petstate widget runtime.

Status: PORTED-PARTIAL for behavior, APPROX/PENDING for exact UI.

### P5 battle petstate / switch

Source:

- `game.d.a(5)` sets `S.c=0`, then `S.W()`.
- `game.h.e(int)` loads `/data/ui/petstate.ui`.
- For battle owner `game.d`, row order uses `game.d.f[i]`, not raw bag order.
- Row HP widget: `16 + i * 6`, filled with `"#P" + pet.L()`.
- Row EXP widget: `17 + i * 6`, filled with `"#P" + pet.O()`.
- Detail widgets:
  - `48` sprite from `pet.C`
  - `51` pet name
  - `52` element/type text
  - `61` relation/type string from `pet.T()`
  - `62` evolution/mutation text
  - `59/60` held/equip icon/name
  - `65..68` level/attack/defense/speed
  - `69..74` quality/rarity stars
- Battle P5 hides widgets `63/64`; widget `75` becomes `"Xuat chien"` when `P == 5`.
- `game.h.X()` confirms through `game.d.a(slot)`.

Source confirm results:

- `0`: selected pet cannot battle, opens `msgwarm.ui` with `"Sung vat nay khong the tham chien"`.
- `1`: selected pet already active, opens active-position warning.
- `-1`: valid, reorders `f[slot]` to front, marks active, clears old battle target/effects, enters P15.

Rebuild:

- `VqsvBattleRuntime.preparePetMenu()` builds a P5 view model and `VqsvBattleRenderer.drawPetStateOverlay()` draws source-backed widget coordinates.
- Persistence of low-HP caught Bunny into P5 is PORTED.

Status: PORTED-PARTIAL / APPROX.

Current smoke `audit99_battle_elder_pet_p5.png` and `audit99_battle_bunny_caught_pet_p5_low_hp.png` show major visual gaps:

- panel title/header is missing or not rendered like source;
- row list is too dense/visually wrong and text overlaps/truncates;
- source row icon/cell behavior is approximate;
- stat labels/detail text can overflow and collide;
- stars/quality are source-shaped but not proven pixel-perfect;
- `ao/al` UI runtime is still PENDING;
- world/non-battle `petstate.ui` is not implemented.

Logic gap found in current rebuild:

- `preparePetMenu()` uses `start = forcedPetSwitch ? 1 : 0` and filters dead pets during forced switch.
- Original screenshot 7/8 and source `game.h.X()` indicate the UI can show an invalid/dead pet and then warn on confirm.
- Therefore forced-switch list filtering is likely wrong for parity. Keep rows visible, validate on confirm.

### Forced switch after death

Source:

- Death routing can enter P5/P15 paths depending battle queue state.
- P5 itself still uses `petstate.ui`; invalid/current/dead choices are handled by confirm warnings, not by hiding the row in the audited `game.h.X()` path.

Rebuild:

- Forced replacement exists and smoke `battle_p5_forced_replacement_success` reaches P1 after selecting reserve.
- But current forced list filtering can hide dead/current rows, so screenshot 7/8 behavior is not faithfully represented.

Status: PORTED-PARTIAL, with specific NEED FIX for forced list visibility/validation.

### Dead pet selected warning

Source:

- `game.h.X()` opens `/data/ui/msgwarm.ui`, text `"Sung vat nay khong the tham chien"`, hint `"Nhan nut 5 de tiep tuc"`.

Rebuild:

- Warning text exists and smoke `audit99_battle_p5_dead_warning.png` reaches WARN.

Status: PORTED-PARTIAL.

Gaps:

- Warning box is visibly smaller and lower-quality than original screenshot 8.
- Underlying P5 panel/background layering differs.
- Need ensure forced-switch route can produce the same warning without hiding the dead row.

### Win EXP gain

Source:

- `game.d.h(b defeatedEnemy)` computes EXP:
  - base `n5 = ((level * 2) * level + 50) * aG[quality - 1] / 10 + 400`;
  - distributes across participating pets in static/vector `x`;
  - applies level-difference multiplier `aI[]`;
  - applies share multiplier `aH[]`;
  - held item/status bonus can modify EXP;
  - party EXP-share cases can add EXP to non-participants.
- `game.d.X()` filters alive participants and transfers pending `B` EXP into each pet.
- `game.d.a(22)` calls `game.h.an()`.
- `game.h.an()` loads `/data/ui/levelUp.ui`, snapshots old stats, calls `v1.v()`, fills old/new stats and selected pet sprite/name/level, and can route to skill learning.
- `game.h.ao()` advances/auto-closes level-up UI and returns to P8/world exit or skill learn flow.

Rebuild:

- `VqsvBattleRuntime.tickWin()` now has a source-shaped P8/P22 EXP and level-up slice for the active participant.
- `VqsvBattleUnit.exp`, source thresholds, stat refresh, and payload persistence are wired for the active pet.
- `VqsvBattleRenderer` has a `/data/ui/levelUp.ui` renderer using source widget positions.
- `VqsvBattleRuntime` can continue into P23 learn-skill when source `game.b.F()` returns candidate skills.

Status: PORTED-PARTIAL for active-pet EXP/levelUp/learn-skill. PENDING for full `game.d.x` participant vector, passive EXP share, evolution queue, exact `levelUp.ui`/`choiceskill.ui`/`msgwarm.ui` pixels.

## Original Screenshot Compare Matrix

| Shot | Original behavior | Current rebuild | Status | Notes |
| --- | --- | --- | --- | --- |
| 1 | World `petstate.ui`, Bunny selected, full detail/stats over map | No confirmed world petstate runtime; only battle P5 renderer exists | PENDING | Need world/panel route using source `game.h.W/e` owner `game.k`. |
| 2 | World `petstate.ui`, Dien Mieu, title `"Sung vat trong hanh trang"` | Same gap | PENDING | Must use real party `sourcePets`, no mock. |
| 3 | Battle entry with map/dim backdrop, HUD, bases under both pets, command bar | Pets/HUD/bases exist, but background/ground/base placement is approximate | PORTED-PARTIAL / APPROX | Do not claim pixel-perfect. |
| 4 | NPC catch command warns `"Tran chien nay khong cho bat sung vat"` | Rebuild has code branch/text, but no focused PNG route proof yet | PORTED-PARTIAL | Add smoke for battleMode catch-forbidden. |
| 5 | P4 item `choice.ui` with icon/name/count and selected description | List exists; description panel missing/weak | PORTED-PARTIAL / APPROX | Need source `be()`/description widget audit. |
| 6 | Battle P5 `petstate.ui`, 3 pets, low-HP Bunny preserved | Low HP/persistence works; UI still visibly off | PORTED-PARTIAL / APPROX | Needs renderer/layout pass and source field audit for all text. |
| 7 | Active pet dead -> P5 opens to choose replacement | Rebuild has forced replacement, but may hide invalid rows | PORTED-PARTIAL | Need fix forced-switch visibility, validate on confirm. |
| 8 | Selecting dead pet shows `msgwarm.ui` over P5 | Warning exists | PORTED-PARTIAL / APPROX | Box size/layout and forced-route parity need work. |
| 9 | Victory grants EXP and updates EXP bar/level-up flow | Active-pet EXP, levelUp UI, and learn-skill branch exist | PORTED-PARTIAL | Full participant/share/evolution/pixel parity remains pending. |

## Recommended Fix Order

1. Forced P5 parity first.
   - Keep source-shaped six-row list visible during forced switch.
   - Do not filter dead rows out.
   - Confirm validates dead/current and opens existing warning.
   - Smoke: forced P5 open, dead warning, reserve switch success.

2. P4/P21 `choice.ui` renderer cleanup.
   - Add selected-item/count/description area from source widget ids.
   - Add focused smoke for NPC catch forbidden.
   - Keep missing-count and Bunny P21 regression.

3. P5 renderer cleanup.
   - Fix title/header, row spacing/cells, text clipping, detail text overflow.
   - Preserve low-HP Bunny payload.
   - Smoke Bunny low HP P5, Elder P5, current/dead warnings.

4. World `petstate.ui`.
   - Implement non-battle owner `game.k` path using same view model but world source data.
   - Must not use mock values.
   - Smoke from saved/resume point with actual party.

5. EXP/levelUp.
   - DONE/PARTIAL in `100_battle_exp_levelup_source_audit.md`.
   - Learn-skill follow-up DONE/PARTIAL in `101_battle_levelup_learn_skill_evolution_audit.md`.
   - Remaining: full `game.d.x` participant vector, passive EXP share, evolution queue, exact UI pixel parity.

## Current PNG Evidence

Generated current-rebuild smoke PNGs:

- `rebuild_game/build/smoke/audit99_battle_entry_both_landed.png`
- `rebuild_game/build/smoke/audit99_battle_elder_item_p4.png`
- `rebuild_game/build/smoke/audit99_battle_catch_missing_count_warning.png`
- `rebuild_game/build/smoke/audit99_battle_elder_pet_p5.png`
- `rebuild_game/build/smoke/audit99_battle_p5_forced_replacement_success.png`
- `rebuild_game/build/smoke/audit99_battle_p5_dead_warning.png`
- `rebuild_game/build/smoke/audit99_battle_bunny_caught_pet_p5_low_hp.png`
- `rebuild_game/build/smoke/audit99_battle_p5_current_warning.png`

Verification run for audit:

- `powershell -ExecutionPolicy Bypass -File .\build.ps1`
- PNG-only smoke checkpoints above.

No runtime code was changed in this audit.

## Implementation Slice 2026-07-08

Implemented after the audit:

- Forced P5 no longer hides active/dead rows. `preparePetMenu()` now keeps the source-visible party rows and validates on confirm, matching `game.d.a(slot)` more closely.
- P5 now persists the active battle pet payload before building `petstate.ui` rows, so a killed active pet is visible/validated as dead instead of being treated as still alive from stale source payload.
- `choice.ui` item list now carries widget-53-style selected item description from `aq.c[4][item][2]`.
- Added focused smoke for NPC catch-forbidden warning (`battle_catch_not_allowed_warning`), source path anchored to `game.h.d(b)` command branch where `((d)o).b == 2` warns `"Tran chien nay khong cho bat sung vat"`.

Updated status:

- Forced P5 visibility/validation: PORTED-PARTIAL, improved. Dead/current/valid confirm paths are smoke-covered; full `game.d.f[]` vector parity remains PORTED-PARTIAL.
- P4 item `choice.ui` description: PORTED-PARTIAL, improved. It is still not a full `ao/al` UI runtime.
- Catch-forbidden warning: PORTED-PARTIAL, focused smoke-covered.

Smoke PNGs from this implementation slice:

- `rebuild_game/build/smoke/forced_p5_dead_warning_visibility.png`
- `rebuild_game/build/smoke/forced_p5_menu_visibility.png`
- `rebuild_game/build/smoke/forced_p5_replacement_success_visibility.png`
- `rebuild_game/build/smoke/choice_item_p4_description.png`
- `rebuild_game/build/smoke/choice_catch_not_allowed_warning.png`
- `rebuild_game/build/smoke/choice_catch_missing_count_warning_regression.png`
- `rebuild_game/build/smoke/choice_item_target_p16_regression.png`
- `rebuild_game/build/smoke/forced_slice_bunny_caught_pet_p5_low_hp_regression.png`

Verification after implementation:

- `powershell -ExecutionPolicy Bypass -File .\build.ps1`
- `java "-Dvqsv.modules=..\modules" -jar build\libs\vqsv-rebuild-skeleton.jar --check`
- `java "-Dvqsv.modules=..\modules" -cp build\classes VqsvBattleDamageFormulaCheck`
- `rg -n "Ãƒ|Ã‚|ï¿½" rebuild_game/src/main/java -g "*.java"`: no hits
- `git diff --check`: only existing LF/CRLF worktree warnings
- route PNG regressions:
  - `route_sophie_after_battle_branch`
  - `route_bunny_after_battle_task`
  - `route_elder_after_battle_reward_state`

## Implementation Slice 2026-07-08B

Implemented:

- Polished shared `petstate.ui` renderer:
  - title now renders centered and unclipped in the source header;
  - selected pet sprite is clipped to widget 48 bounds so large pet sprites do not cover the stat/detail text;
  - battle mode still renders widget 75/76 action/back labels;
  - world mode hides battle action/footer labels, matching the `game.k` path where `game.h.e()` hides widgets 75/76.
- Added world/non-battle petstate overlay:
  - `Scene.openWorldPetstate()` builds rows from real `sourcePets`;
  - click bottom-left world UI opens the overlay when world UI is visible;
  - up/down changes selected row, confirm/back closes;
  - renderer reuses the same source-shaped `petstate.ui` widget map as battle.
- Added smoke for world petstate with actual route payload:
  - catches Bunny through the rebuild battle route first;
  - preserves low-HP Bunny payload;
  - opens world `petstate.ui` over scene 1 room 1.

Updated status:

- Battle P5 renderer: PORTED-PARTIAL, improved. Still not full `ao/al` runtime and not pixel-perfect.
- World petstate: PORTED-PARTIAL. It renders real source party data and supports minimal open/select/close, but full game.k menu modes, bank/send/release/equip/skill submenus remain PENDING.

Smoke PNGs:

- `rebuild_game/build/smoke/petstate_polish_bunny_low_hp_p5_titlefit.png`
- `rebuild_game/build/smoke/world_petstate_dien_mieu_selected.png`
- `rebuild_game/build/smoke/world_petstate_bunny_selected.png`
- `rebuild_game/build/smoke/petstate_polish_forced_dead_warning_regression.png`
- `rebuild_game/build/smoke/petstate_polish_forced_replacement_success_regression.png`

Verification:

- `powershell -ExecutionPolicy Bypass -File .\build.ps1`
- `java "-Dvqsv.modules=..\modules" -jar build\libs\vqsv-rebuild-skeleton.jar --check`
- `java "-Dvqsv.modules=..\modules" -cp build\classes VqsvBattleDamageFormulaCheck`
- `rg -n "Ãƒ|Ã‚|ï¿½" rebuild_game/src/main/java -g "*.java"`: no hits
- `git diff --check`: only existing LF/CRLF worktree warnings
- route PNG regressions:
  - `route_sophie_after_battle_branch`
  - `route_bunny_after_battle_task`
  - `route_elder_after_battle_reward_state`

## Implementation Slice 2026-07-08C

Implemented:

- Active-participant battle EXP and `levelUp.ui`:
  - source-shaped `game.d.h(b)` EXP award formula for one active participant;
  - `game.b.A()/g()/u()/v()` threshold/subtract/stat refresh behavior;
  - P8 EXP fill and P22 level-up overlay;
  - `/data/ui/levelUp.ui` renderer for title, sprite, EXP, old stats, new stats, and message.
- Learn-skill branch after level-up:
  - source-shaped candidate list from `game.b.F()`;
  - P23 `choiceskill.ui` open after confirm/timeout on level-up;
  - confirm prompt and `game.b.g(byte)`-style skill add into source pet payload.

Updated status:

- Active-pet EXP/levelUp: PORTED-PARTIAL.
- Learn-skill after levelUp: PORTED-PARTIAL.
- Full participant EXP vector/passive share: PENDING.
- Evolution queue from `game.b.J()` / `game.k.H` / `game.k.L` / `game.k.I`: PENDING.
- Exact `levelUp.ui`, `choiceskill.ui`, and `msgwarm.ui` pixel parity: PENDING.

Smoke PNGs:

- `rebuild_game/build/smoke/battle_exp_levelup_ui_v2.png`
- `rebuild_game/build/smoke/battle_exp_levelup_choiceskill_ui.png`
- `rebuild_game/build/smoke/battle_exp_levelup_learn_skill_done.png`
- `rebuild_game/build/smoke/route_sophie_after_battle_branch_levelup_learn_regression.png`
- `rebuild_game/build/smoke/route_bunny_after_battle_task_levelup_learn_regression.png`
- `rebuild_game/build/smoke/route_elder_after_battle_reward_state_levelup_learn_regression.png`

Verification:

- `powershell -ExecutionPolicy Bypass -File .\build.ps1`
- `java "-Dvqsv.modules=..\modules" -jar build\libs\vqsv-rebuild-skeleton.jar --check`
- `java "-Dvqsv.modules=..\modules" -cp build\classes VqsvBattleDamageFormulaCheck`
- `rg -n "Ãƒ|Ã‚|ï¿½" rebuild_game/src/main/java -g "*.java"`: no hits
- `git diff --check`: only existing LF/CRLF worktree warnings
