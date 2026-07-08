# 113 Battle P5 Source Re-Audit Matrix

## Scope

Re-audit P5 from original source before the next code slice.

Read sources:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/ui/decoded/data__ui__petstate.ui.json`
- current rebuild files:
  - `rebuild_game/src/main/java/VqsvBattleRuntime.java`
  - `rebuild_game/src/main/java/VqsvBattleRenderer.java`
  - `rebuild_game/src/main/java/VqsvBattlePetStateView.java`

Status key: PORTED / PARTIAL / APPROX / PENDING / N/A.

## Source Chain

### Entry: `game.d.a(byte)` case 5

Source fact:

```java
case 5: {
    this.S.c = 0;
    this.S.W();
    return;
}
```

Meaning:

- Battle state P5 delegates UI open to `game.h`.
- `S.c = 0` resets the UI helper selection/mode field before opening.
- P5 is not `/data/ui/choice.ui`.

Rebuild:

- `VqsvBattleRuntime.preparePetMenu(...)`
- sets `s.battleUiMode = "petstate"`

Status: PARTIAL.

Gap:

- Rebuild does not model `game.h.c`, `game.h.b`, `game.h.f` as source UI state. It uses `battleMenuIndex`/`battleMenuIds`.

### Update: `game.d` case 5

Source fact:

```java
case 5: {
    this.S.X();
    break;
}
```

Meaning:

- All P5 input is handled by `game.h.X()`.

Rebuild:

- `VqsvBattleRuntime.tickPetSwitch(...)`

Status: PARTIAL.

### Draw: `game.d` case 5

Source fact:

```java
case 5: {
    graphics.setColor(0xFFFFFF);
    graphics.drawString(game.d.f(aq.c[0][this.c((int)this.f[0]).q()][0]),
            game.d.w() >> 1, 200, 17);
    break;
}
```

Meaning:

- Battle draw itself only adds the active pet/source species name around center y=200.
- The pet list/detail panel is drawn by the loaded UI runtime from `/data/ui/petstate.ui`.

Rebuild:

- `VqsvBattleRenderer.renderPetStateOverlay(...)` draws the whole petstate-like panel.

Status: APPROX/PARTIAL.

Gap:

- Rebuild does not explicitly layer the source drawString active species name at y=200.
- Current full panel is source-shaped, not a real `game.h` widget runtime.

## `game.h.W()` And `game.h.e(int)`

### `W()`

Source fact:

```java
public final void W() {
    this.b = 0;
    this.e(this.c);
}
```

Meaning:

- UI list cursor `b` resets to 0.
- It opens/fills petstate using current selected row `c`.

Rebuild:

- `preparePetMenu(...)` preserves `battleMenuIndex` unless out of range.

Status: PARTIAL.

Gap:

- Source resets `b`; rebuild may preserve old selection. Next code slice should reset P5 cursor on entry unless a proven source state needs otherwise.

### `e(int i1)` for battle owner

Source facts:

- Opens `/data/ui/petstate.ui` with sprite bank `257`.
- Calls `f(i1)` to fill selected-pet detail.
- If owner is `game.d`, loops six fixed rows.
- Row pet order is `((d)o).f[i4]`, not raw pet array order.
- Valid row:
  - HP widget `16 + i4 * 6` = `"#P" + q.z[f[i4]].L()`
  - second bar widget `17 + i4 * 6` = `"#P" + q.z[f[i4]].O()`
- Empty row:
  - both bars = `#P0`
- Hides widgets `63` and `64`.
- If `o.Q == 4`, widget 75 text = `Sử dụng`.
- Else if `o.P == 5`, widget 75 text = `Xuất chiến`.
- Root list metadata:
  - total count `a = q.A`
  - visible count `d = q.A`
  - selected index `f = i1`
- Sets `g = true`.

Rebuild:

- `preparePetMenu(...)` builds `battleMenuIds` from `s.sourcePets` order.
- `buildPetStateRows(...)` maps row directly to pet index.
- `renderPetStateOverlay(...)` draws six rows.

Status: PARTIAL.

Gaps:

- Rebuild does not have an explicit source `game.d.f[]` array. It currently treats `sourcePets` order as if it were `f[]`.
- Row order is acceptable only if all switches keep `sourcePets[0]` equivalent to source `f[0]`, but this needs to be documented in traces and smoke.
- Empty rows are rendered, but not through actual `#P0` widget semantics.
- Widget 75 text is shown through rebuild constants, not source `o.P/o.Q`.

## Selected Pet Detail: `game.h.f(int)` And `a(b[] pets, int slot)`

### `f(int i1)`

Source fact:

```java
if (this.o instanceof d) {
    this.a(this.q.z, (int)((d)this.o).f[i1]);
}
```

Meaning:

- UI cursor row is not directly the pet slot.
- Battle P5 detail panel selects pet by `game.d.f[row]`.

Rebuild:

- selected row -> `battlePetStateRows[row]` -> `sourcePets[petIndex]`.

Status: PARTIAL.

### Detail widget fills

Source method `a(b[] pets, int slot)` fills:

| Widget | Source value | Rebuild status |
|---:|---|---|
| 48 | sprite object from `pet.C`, mode 3 | PARTIAL |
| 51 | name `an.f(pet.j(0))` | PORTED/PARTIAL |
| 52 | element/type `an.f(365 + pet.j(1))` | PARTIAL |
| 62 | evolution/mutation text from `pet.j(19)` and `aq.c[0][target][2]` | PARTIAL |
| 61 | relation/type string `pet.T()` | APPROX |
| 64 | action text, `Xuất chiến` for battle owner | hidden later in battle owner |
| 59 | held item icon, sprite 258 cell `aq.c[3][pet.c[5]][1]` | PARTIAL |
| 60 | held item name `an.f(aq.c[3][pet.c[5]][0])` | PARTIAL |
| 65 | level `pet.s()` | PORTED/PARTIAL |
| 66 | attack `pet.e(2)` | PORTED/PARTIAL |
| 67 | defense `pet.e(3)` | PORTED/PARTIAL |
| 68 | speed `pet.e(4)` | PORTED/PARTIAL |
| 69..74 | star cells from rarity and `pet.d[0]`, using sprite 257 cell 14/16 | PORTED/PARTIAL |

Important source meanings:

- `pet.C` is the sprite/visual id for the selected pet.
- `pet.j(byte)` pulls species table fields.
- `pet.T()` is source element/relation text, not just element name.
- `pet.e(2/3/4)` returns current live stat, not raw base stat.
- Star count uses `pet.d[0]` quality/fill and species rarity `aq.a(0, pet.q(), 4) - 1`.

Current rebuild:

- `VqsvBattlePetStateView.fromPet(...)` already carries many of these fields.
- Relation text currently approximates as element name.
- Some source payload indices are inferred, not fully proven against `game.b` field lifecycle.

Status: PARTIAL.

## Input: `game.h.X()`

### Navigation

Source facts when `this.f == 0`:

- Up input `4100`: `this.p.a.b(0)`
- Down input `8448`: `this.p.a.b(1)`
- Guarded by `!an.b(this.b, 0)` and `!this.j()`.

Rebuild:

- `handleMenuInput(...)` handles up/down/click and updates `battleMenuIndex`.

Status: PARTIAL.

Gap:

- Source list widget moves internal cursor and detail refresh is tied to `f(row)`. Rebuild refreshes through render-time selected row; works visually but is not a widget runtime.

### Confirm

Source facts:

If owner is `game.d`, confirm calls:

```java
int i1 = ((d)this.o).a(this.b);
```

Then:

- `i1 == 0`
  - opens `msgwarm.ui`
  - text: `Sủng vật này không thể tham chiến`
  - unloads `/data/ui/petsetting.ui`
- `i1 == 1`
  - opens `msgwarm.ui`
  - text: `Sủng vật này đã đặt ở vị trí chiến đấu`
  - unloads `/data/ui/petsetting.ui`
- `i1 == -1`
  - calls `((d)this.o).a((int)((d)this.o).g, 0)`
  - sets `this.a = 0`
  - battle state -> `15`
  - unloads `/data/ui/petsetting.ui`
  - unloads `/data/ui/petstate.ui`

Rebuild:

- Dead pet warning.
- Already-active warning.
- Valid switch reorders `sourcePets`, sets new player, enters `P15_PLAYER_SWITCH`.

Status: PARTIAL.

Gap:

- Warning text currently uses rebuild `VqsvText` equivalents; should verify exact Vietnamese strings and UI source kind.
- Rebuild immediately mutates `sourcePets` before P15. Source mutates `f[]` in `game.d.a(row)` before entering state 15, so this is source-shaped.
- Source call `a(g,0)` before P15 is not fully audited in rebuild.

### Back

Source facts:

If owner is `game.d`:

- if `Q == 7 || Q == 13`, return and do not exit.
- otherwise:
  - unload `/data/ui/petstate.ui`
  - `game.d.a().k = false`
  - `this.a = 0`
  - battle state -> `20`

Rebuild:

- Back returns to P20.

Status: PARTIAL.

Gap:

- Rebuild does not model `Q == 7/13` back lock.
- Rebuild does not model `game.d.k = false` field explicitly.

## `game.d.a(int row)` Validation And Reorder

Source facts:

```java
if (!this.c((int)this.f[row]).S()) return 0;
if (this.c((int)this.f[row]).K()) return 1;
byte by = this.f[row];
--row;
while (row >= 0) {
    this.f[row + 1] = this.f[row];
    --row;
}
this.f[0] = by;
...
this.c((int)this.f[0]).J = true;
this.c((int)this.f[0]).d(true);
this.h.d(false);
this.h.F = 0;
...
return -1;
```

Meaning:

- Return `0`: selected pet cannot fight because `S()` false.
- Return `1`: selected pet is already active because `K()` true.
- Return `-1`: valid.
- Valid switch moves selected `f[row]` to `f[0]` and shifts earlier entries right.
- Active pet is marked active:
  - `J = true`
  - `d(true)`
- Previous active `h` is marked inactive:
  - `h.d(false)`
  - `h.F = 0`
- If enemies have debuff/status 11 pointing at old active pet, it clears that effect.

Rebuild:

- Uses `sourcePets.remove(selectedIndex); add(0, next)` as source-shaped equivalent of moving `f[row]` to `f[0]`.
- Sets `player` from new source pet.
- Runs P15 source-shaped cpos transition.

Status: PARTIAL.

Gaps:

- Does not explicitly model `J`, `K()`, `d(true/false)`, `F=0`.
- Does not clear enemy status/debuff 11 references to old active pet.
- Does not maintain a separate `f[]` array; sourcePets order is the proxy.

## Decoded `petstate.ui` Required Geometry

Source file:

`modules/ui/decoded/data__ui__petstate.ui.json`

Important widgets:

| Widget | Meaning | Geometry / asset |
|---:|---|---|
| 1 | main frame | x=43,y=55,w=158, sprite 257 cell 1 |
| 2 | title | x=70,y=58,w=100 |
| 3 | top strip | x=46,y=79,w=151 |
| 4 | body fill | x=46,y=87,w=151 |
| 5 | footer strip | x=46,y=247,w=151 |
| 7 | selected pet sprite backing | x=107,y=92,w=88, cell 8 |
| 8 | left detail backing | x=50,y=184,w=80, cell 17 |
| 9..12 | stat backing bars | x=144,y=184/197/210/223, cells 15/16 |
| 48 | selected pet sprite | x=105,y=85,w=90 |
| 49/50 | arrows | x=78,y=78 and x=76,y=174 |
| 51/52/61/62 | detail text | name/type/relation/evolution |
| 59/60 | held item icon/name | x=80,y=222 and x=100,y=224 |
| 63/64 | bottom softkeys | hidden for battle owner |
| 75/76 | in-panel action/back | x=50,y=240 and x=164,y=240 |

Rows:

| Row | BG | Number | Icon | HP | EXP | y |
|---:|---:|---:|---:|---:|---:|---:|
| 0 | 6 | 14 | 15 | 16 | 17 | 86 |
| 1 | 19 | 21 | 20 | 22 | 23 | 101 |
| 2 | 25 | 27 | 26 | 28 | 29 | 116 |
| 3 | 31 | 33 | 32 | 34 | 35 | 131 |
| 4 | 37 | 39 | 38 | 40 | 41 | 146 |
| 5 | 43 | 45 | 44 | 46 | 47 | 161 |

## Current Rebuild Gap Summary

KEEP:

- `P5` correctly routes to `petstate` mode, not `choice`.
- Valid switch currently uses source-shaped reorder-to-front.
- P15 transition exists and uses cpos rows.

FIX NEXT:

1. Entry cursor:
   - Source `W()` resets `b = 0`.
   - Rebuild should reset `battleMenuIndex = 0` on P5 entry unless a source exception is proven.

2. Source `f[]` proxy:
   - Document and trace that `sourcePets` order is the rebuild proxy for `game.d.f[]`.
   - Make row ids and selected pet index reflect row order consistently.

3. Visual parity:
   - Draw row HP and EXP bars strictly from `L()`/`O()` equivalent.
   - Keep six fixed rows with `#P0` empty rows.
   - Improve detail fields:
     - type text
     - `pet.T()` relation approximation status
     - held item icon/name
     - star cells 14/16 parity
   - Add active pet name draw layer near y=200 if source layering requires it behind panel.

4. Validation parity:
   - Confirm warning strings/source UI kind for return 0/1.
   - Add/verify back lock for `Q == 7 || Q == 13` if those states are reachable in rebuild.
   - Track old active deactivation and enemy status 11 cleanup as PENDING unless proven unnecessary for current route.

5. Smoke PNG:
   - `battle_p5_petstate_source_rows.png`
   - `battle_p5_current_pet_warning.png`
   - `battle_p5_dead_pet_warning.png`
   - `battle_p5_valid_switch_transition.png`
   - `battle_p5_after_switch_active_pet.png`

## Recommendation

Next code slice should be small:

**P5 visual/data parity only**

- reset P5 cursor on entry,
- make row rendering and `VqsvBattlePetStateView` explicitly source-shaped around `game.d.f[]` proxy,
- add pixel smoke for six row panel/body/detail regions,
- do not change P15 logic yet.

After that:

**P5 validation/P15 cleanup slice**

- verify warning text/opening through `msgwarm.ui`,
- add back-lock if needed,
- audit/port old-active deactivation and status 11 cleanup.

## Implemented Slice: P5 Visual/Data Parity Only

Code files changed:

- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleRenderer.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Implemented:

- `preparePetMenu(...)` now has a source-shaped `resetCursor` path for P5 entry, matching `game.h.W()` setting `b = 0`.
- Warning return to P5 keeps cursor via `preparePetMenu(s, false)` to avoid re-entering `W()` behavior when merely closing `msgwarm.ui`.
- P5 trace explicitly records `sourceProxy=sourcePets-as-game.d.f`.
- P5 row rendering no longer draws rebuild-only circular element icons over source row icon cell 18.
- Detail panel no longer draws unproven custom HP/stat fill bars over widgets 8..12.
- Footer action/back text uses source widget bounds:
  - widget 75: `x=50,y=240,w=24`
  - widget 76: `x=164,y=240,w=24`
- Smoke aliases were added:
  - `battle_p5_petstate_source_rows`
  - `battle_p5_current_pet_warning`
  - `battle_p5_dead_pet_warning`
  - `battle_p5_valid_switch_transition`
  - `battle_p5_after_switch_active_pet`
- `battle_p5_petstate_source_rows` now pixel-checks panel body, footer, row HP bar, and detail text.

Follow-up cleanup implemented:

- Arrow widgets 49/50 now use sprite 257 animation states 0/1. Direct cell 0/1 was wrong because cell 1 is the main frame; inspected bounds prove state 0/1 are the 9x9 arrow widgets.
- `SourcePetState` now carries source-shaped active fields:
  - `sourceActive` for `game.b.K()` / `game.b.d(boolean)`.
  - `sourceTurnUsed` for `game.b.J` bookkeeping.
  - `sourceF` placeholder for `game.b.F`.
- `VqsvBattleRuntime` now has explicit `sourcePetOrder` as the battle-side `game.d.f[]` mirror. It is still mirrored with `sourcePets` order after switch to preserve existing world/save assumptions that active pet is `sourcePets[0]`.
- P5 validation now checks `sourceK()` for already-active instead of only `selectedIndex == 0`.
- Valid switch now sets active flags via `setActiveSourcePetFlags(...)`.
- Valid switch clears enemy buff/status 11 using `BattleUnit.clearSourceBuffForSwitch(11)`, with smoke coverage.
- `battle_p5_status11_cleanup` smoke verifies:
  - enemy buff 11 was prepared,
  - valid P5 switch enters P15,
  - `clearedEnemyBuff11=1`,
  - active flags are updated.

Still PARTIAL:

- `sourcePetOrder` is explicit but still mirrored with `sourcePets` order. A fully independent `game.d.f[]` array would require auditing every world/save/menu assumption that reads `sourcePets[0]` as active.
- `pet.T()` is effectively source-equivalent for current tables because source returns the species element name array; code still routes through rebuild table data rather than a dedicated decompiled `game.b.T()` helper.
- `sourceF` is stored/reset, but full `game.b.F` lifecycle beyond P5 switch is not fully modeled.
