# 188 Battle Phase 10-A Status Icon / Effect Overlay Audit And Port Plan

Status: AUDIT COMPLETE, 10-A1/10-A2 CODE PORTED, DEDICATED SMOKE CHECKPOINTS PORTED

## Scope

Phase 10-A is about battle status icon/effect overlay parity after Phase 9 skill coverage.

This document focuses on the source-backed HUD status icon layer:

- active buff/debuff icons in `battle.ui`
- duration overlay cells
- how P7/P12/P13 refresh those HUD widgets
- how rebuild should expose `BattleUnit` effect queues to the renderer

This is separate from body-attached battle effects:

- `game.b.L`
- `game.b.u`
- `game.d.H`
- `ah.java`
- `effect.mid`
- `speffect.mid`

Those remain P7/effect animation work, not sprite 325 HUD icon work.

## Source Facts

### Status Icon Asset Chain

| Source asset | Source fact | Meaning | Status |
|---|---|---|---|
| `modules/script/decoded/data__script__sprite.mid.json` | row around sprite id `325` is `[325, 804]` | sprite `325` uses image `804` | VERIFIED |
| `modules/img/decoded/data__img__img_804.mid.png` | decoded image exists | bitmap sheet for status/help icons | VERIFIED |
| `modules/spr/original/spr_325_all(r)` | original sprite metadata exists | cell definitions for sprite `325` | VERIFIED |
| `modules/ui/original/battle.ui` | original battle UI exists | contains widgets used by `game.h` | VERIFIED |
| `modules/ui/decoded/data__ui__battle.ui.json` | decoded battle UI exists | rebuild can read widget ids/positions | VERIFIED |

### Source HUD Refresh Calls

| Source | Lines / method | Behavior | Status |
|---|---|---|---|
| `game.d.java` | `case 12/13` entry around `d.java:769..806` | Before active queue processing, calls `S.a(h,false); S.a(h)` for one side or `S.b(h,false); S.b(h)` for the other side. | VERIFIED |
| `game.d.java` | `case 7` entry around `d.java:806..826` | Refreshes both attacker and target battle HUDs before P7 resolve. | VERIFIED |
| `game.d.java` | draw switch around `d.java:1841..1873` | P7/P12/P13 draw battlefield, optional `H`, actors, and floating text. Sprite `325` is not drawn there directly. | VERIFIED |

### Source Widget Mapping

Source methods:

- `game.h.a(b v1)` around `h.java:3564..3592`
- `game.h.b(b v1)` around `h.java:3657..3683`

They reset and populate six visible status slots.

| Side method | Icon widget ids | Duration widget ids | Sprite id | Empty icon cell | Empty duration cell | Status |
|---|---:|---:|---:|---:|---:|---|
| `game.h.a(b)` | `26..31` | `43..48` | `325` | `0` | `145` via sprite/UI `257` | VERIFIED |
| `game.h.b(b)` | `32..37` | `49..54` | `325` | `0` | `145` via sprite/UI `257` | VERIFIED |

Important note: exact semantic side naming depends on which unit `game.d` passes in that battle state. Rebuild should not rename these as "player/enemy" without checking current unit side; it can expose both player/enemy arrays but must populate by actual runtime unit.

### Source Icon Cell Formula

Source loops over three queue slots. For each queue index `i`, it tries buff first, then debuff. A shared `k` is incremented for every visible icon, so the visible order is:

`buff slot 0`, `debuff slot 0`, `buff slot 1`, `debuff slot 1`, `buff slot 2`, `debuff slot 2`

| Source condition | Icon cell | Duration cell | Status |
|---|---:|---:|---|
| empty/reset | `0` | `145` | VERIFIED |
| active buff id from `v1.x[0][i]` and `v1.v[id][0] > 0` | `id + 12` | `134 + v1.v[id][0]` | VERIFIED |
| active debuff id from `v1.x[1][i]` and `v1.w[id][0] > 0` | `id + 1` | `134 + v1.w[id][0]` | VERIFIED |

### Source Data Model

| Source field | Meaning | Rebuild equivalent | Status |
|---|---|---|---|
| `game.b.v[16][5]` | buff slots | `BattleUnit.buffSlots` | PORTED/PARTIAL |
| `game.b.w[11][5]` | debuff slots | `BattleUnit.debuffSlots` | PORTED/PARTIAL |
| `game.b.x[2][3]` | active effect icon/order queue | `BattleUnit.activeEffectQueue` | PORTED/PARTIAL |
| `game.b.N[2]` | active queue counts | `BattleUnit.activeEffectCount` | PORTED/PARTIAL |
| `game.b.a(int, byte)` | enqueue active effect id, max three per bank | `BattleUnit.addActiveEffect` | PORTED/PARTIAL |
| `game.b.d(int, slot)` | buff tick/expire and queue removal | `BattleUnit.tickBuff` / queue removal helpers | PORTED/PARTIAL |
| `game.b.c(int, slot)` | debuff tick/expire and queue removal | `BattleUnit.tickDebuff` / queue removal helpers | PORTED/PARTIAL |

## Rebuild Current State

| Rebuild file | Current behavior | Gap | Status |
|---|---|---|---|
| `rebuild_game/src/main/java/VqsvBattleUnit.java` | stores source-shaped `buffSlots`, `debuffSlots`, `activeEffectQueue`, `activeEffectCount` | model exists | PORTED/PARTIAL |
| `rebuild_game/src/main/java/VqsvBattleRuntime.java` | `syncRenderState(...)` copies HP/name/element/log/marker/status cells into `Scene` | dedicated smoke checkpoints/pixel asserts not added yet | PORTED/PARTIAL |
| `rebuild_game/src/main/java/VqsvIntroDemo.java` | `Scene` has player/enemy status icon arrays, duration arrays, and counts | arrays are render DTO only | PORTED |
| `rebuild_game/src/main/java/VqsvBattleRenderer.java` | `drawBattleHudWidgets(...)` calls `drawStatusSlots(...)` for both sides with real cell arrays | exact MIDP widget mode parity still pending | PORTED/PARTIAL |
| `rebuild_game/src/main/java/VqsvBattleRenderer.java` | `drawStatusSlots(...)` draws sprite `325` cells and duration cells from scene arrays | source cell formula is runtime-backed | PORTED/PARTIAL |

Update 2026-07-09: `Scene` now has player/enemy status icon and duration arrays, `SourceBattleRuntime.syncRenderState(...)` populates them from `BattleUnit.activeEffectQueue + buffSlots/debuffSlots`, and `VqsvBattleRenderer.drawStatusSlots(...)` draws those cells. Dedicated Phase 10-A checkpoints now assert scene cells and rendered visible pixels for enemy debuff, enemy buff, player debuff, and mixed source order.

## Phase 10-A Port Plan

### Slice 10-A1: Runtime-To-Scene Status Slot Bridge

Code change target:

- `rebuild_game/src/main/java/VqsvIntroDemo.java`
- `rebuild_game/src/main/java/VqsvBattleRuntime.java`

Add scene arrays, length six:

- player side icon cells
- player side duration cells
- player side visible count
- enemy side icon cells
- enemy side duration cells
- enemy side visible count

Populate from `BattleUnit` using the exact source formula:

1. initialize all six icon cells to `0`
2. initialize all six duration cells to `145`
3. loop `i = 0..2`
4. if buff queue slot valid and duration > 0:
   - icon = `buffId + 12`
   - duration = `134 + buffSlots[buffId][0]`
5. if debuff queue slot valid and duration > 0:
   - icon = `debuffId + 1`
   - duration = `134 + debuffSlots[debuffId][0]`
6. stop at six visible slots

Classification target after slice:

- active status cell mapping: PORTED
- exact widget mode-2 behavior: PARTIAL until pixel comparison

### Slice 10-A2: Renderer Uses Real Cells

Code change target:

- `rebuild_game/src/main/java/VqsvBattleRenderer.java`

Change `drawStatusSlots(...)` to accept icon cell and duration cell arrays.

Draw every slot exactly like source reset behavior:

- sprite `325`, cell from array, default `0`
- duration overlay cell from array, default `145`

Use `battle.ui` widget positions already loaded through `VqsvUiLayout`:

- current enemy/HUD call uses widgets `32` and `49`
- current player/HUD call uses widgets `26` and `43`

Do not move coordinates in this slice unless decoded `battle.ui` proves renderer is using the wrong widget ids.

### Slice 10-A3: Smoke PNG And Pixel Assertions

Add checkpoints in `VqsvSmokeHarness`, using existing debug hooks:

| Checkpoint | Setup | Expected visible cell | Expected duration | Purpose |
|---|---|---:|---:|---|
| `battle_phase10a_status_icons_enemy_debuff1` | enemy debuff id `1`, duration `3` | `2` | `137` | PORTED |
| `battle_phase10a_status_icons_enemy_buff9` | enemy buff id `9`, duration `3` | `21` | `137` | PORTED |
| `battle_phase10a_status_icons_player_debuff5` | player debuff id `5`, duration `3` | `6` | `137` | PORTED |
| `battle_phase10a_status_icons_mixed_order` | buff slot0 + debuff slot0 + buff slot1 | `21, 2, 22` order | `137, 137, 137` | PORTED |

Smoke output PNGs:

- `battle_phase10a_status_icons_enemy_debuff1.png`
- `battle_phase10a_status_icons_enemy_buff9.png`
- `battle_phase10a_status_icons_player_debuff5.png`
- `battle_phase10a_status_icons_mixed_order.png`

Pixel/cell assertions should check that status icon region is not the empty-cell baseline. Prefer asserting scene arrays directly plus sampling a small rendered region around the widget position.

Smoke command must be PNG-only:

```powershell
java -cp build\classes VqsvIntroDemo --smoke-checkpoint battle_phase10a_status_icons_enemy_debuff1 build\smoke\phase10a\battle_phase10a_status_icons_enemy_debuff1.png
```

Do not use commands that open the client.

## Regression Plan

Run after code slices:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes com.vqsv.rebuild.Main --check
java -cp build\classes VqsvBattleDamageFormulaCheck
rg -n "Ã|Â|Æ|Ð|ð|□|�|mojibake" src\main\java
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_sophie_after_battle_branch build\smoke\phase10a\route_sophie_after_battle_branch.png
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_bunny_after_battle_task build\smoke\phase10a\route_bunny_after_battle_task.png
java -cp build\classes VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state build\smoke\phase10a\route_elder_after_battle_reward_state.png
```

## Classification

| Item | Status | Reason |
|---|---|---|
| sprite `325` / image `804` asset chain | VERIFIED | decoded and original assets exist |
| source HUD icon cell formulas | VERIFIED | direct `game.h.a(b)` / `game.h.b(b)` source calls |
| source duration cell formula | VERIFIED | direct `134 + duration` source calls |
| source empty cell behavior | VERIFIED | direct reset to icon `0`, duration `145` |
| source status display order | VERIFIED | direct loop over `x[0][i]` then `x[1][i]`, shared `k` |
| rebuild battle unit status storage | PORTED/PARTIAL | fields exist, some effect lifecycle still partial |
| rebuild HUD active icon rendering | PORTED/PARTIAL | scene arrays, runtime bridge, renderer cells, and dedicated smoke exist; exact MIDP widget mode still pending |
| exact Java ME `m` sprite mode-2 draw parity | PENDING | renderer uses Java2D helper, not full MIDP widget runtime |
| pixel comparison with MIDP original | PENDING | user will compare manually later |
| body-attached status/effect visuals | OUT OF SCOPE HERE | belongs to `game.b.L`, `game.b.u`, `game.d.H`, `ah/effect/speffect` |

## Next Roadmap Step

Phase 10-A status HUD icons now pass dedicated smoke. Continue Phase 10-B:

- audit body-attached status overlays from `game.b.u` / `game.d.H`
- decide which status effect visual is source-called by actual battle state
- port only that effect slice, with PNG smoke
