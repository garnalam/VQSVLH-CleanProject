# Scene 1 Post Intro Original Init Trace

Purpose: source-backed checklist for what the original game initializes after
`scene_1 room3 group0` ends and the game enters the "Muoi nam sau..." village
sequence. This file exists to prevent missing player, UI, direction, map, or
event-trigger details during the rebuild.

Status terms:

- `PORTED`: backed by source/event/assets and represented in rebuild.
- `APPROX`: source intent represented, but original subsystem is not fully ported.
- `STUB`: deliberate placeholder.
- `MISSING`: source-backed behavior not represented yet.

## 1. Transition Out Of Room3 Group0

Decoded event source:

- `modules/event/decoded/data__event__scene_1.mid.json`
- `scene_1 room3 group0 rec101`: `op22 [1,2,199,218,240,320]`
- `scene_1 room3 group0 rec102`: `op6 [1,0,0,0]`
- `scene_1 room3 group0 rec103`: `op14 []`

Source behavior:

- `game.c op22` sets `game.k.x = true`, `game.k.w = 2`,
  `game.k.h = 199`, `game.k.i = 218`, `game.k.B = 240`,
  `game.k.C = 320`, and clears transition actor `j = -1`.
- `game.c op6` marks current event state complete as `3`, sets target
  scene/room to `scene=1, room=0`, leaves transition actor as `-1`, then
  calls `game.i.a().a((byte)22)`.
- `game.i state 22` calls `game.k.a().d()` and returns to world state.

Rebuild status:

- Transition target room is `APPROX/PORTED`.
- Full `game.i` state 22 and event persistence table are still `APPROX`.

## 2. Room0 Loader Work In game.k.d()

Source owner:

- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- Main loader block: `game.k.d()`.

Original loader sequence:

1. Reset/setup world display state with `s()`.
2. Ensure shared `ar` resource array exists.
3. Bind event controller `M = game.c.a()` and attach current world.
4. Bind player controller `c = game.g.o()`.
5. Open `/data/event/scene_1.mid`.
6. Skip to room0 block.
7. Read room string pool.
8. Read room name.
9. Read map id / room background id.
10. Read actor count and actor table.
11. Read room event groups into `game.c`.
12. Ensure temp effect sprite `p` exists with sprite `259`.
13. Load pet-area/world helper tables through `T()`, `U()`, `V()`.
14. Set room display name from text table.
15. Load/render map through `game.j`/map display object.
16. Add all room actors to display list.
17. Initialize or reposition player depending on transition flag and actor target.
18. Attach camera/follow object to player.
19. Clear or apply special screen effects.
20. Bind UI controller `S = game.h.a()` and attach current world.
21. Initialize event scanning by calling `M.i()` and `M.b()`.
22. Set `game.k.J = true`, set world state, and finish loading.

Room0 decoded data:

- Room name: `Thuy Moc Thon` (decoded mojibake in current JSON display).
- Map id / `unknown_ab`: `2`.
- Actor count: `58`.
- Event group count: `12`.

Rebuild status:

- Map2 render is `APPROX/PORTED`.
- Actor table loading is manually represented and sprite resolution now uses
  source `sprite.mid`: `APPROX/PORTED`.
- Full loader/state/display-list behavior remains `APPROX`.
- Real `game.h` world UI binding is `MISSING`.

## 3. Why Room0 Group0 Starts

Decoded event source:

- `scene_1 room0 group0 rec0`: `op15 [1,3,0]`.

Source behavior:

- `game.c.b()` scans event groups whose state is idle.
- `op15` checks whether the referenced event state is complete.
- Because room3 group0 was persisted as state `3`, room0 group0 is triggered.

Rebuild status:

- Running room0 group0 after room3 group0 is source-valid.
- It is currently a manual/approx trigger, not the real `game.c.b()` scan.

## 4. Player / Neil Initialization And Direction

Source facts:

- `game.k.d()` receives transition metadata from room3 rec101:
  `h=199`, `i=218`, `w=2`.
- If the player is not initialized, `game.k.d()` creates player init data:
  `[199,218,2,4,4,8,40,100,0]`.
- `game.g.a(short[])` binds player sprite index `0` when `t == -1`, sets
  `i=x`, `j=y`, and calls `b((byte)0, (byte)sArray[2])`.
- Therefore Neil/player should start with source direction `2` from transition
  metadata before room0 group0 runs.
- `scene_1 room0 group0 rec3`: `op8 [199,218]`.
- `game.c op8` does:
  - `this.x.c()`
  - `game.k.u = -1`
  - `this.x.b(199,218)`
  - `this.x.b.b(199,218)`
  - `this.x.b((byte)0, this.x.n)`
- This means `op8` moves the actual player model and marker to `[199,218]`;
  it does not merely center the camera.
- `op8` keeps current player direction `this.x.n`.

Rebuild status:

- Player sprite index `0` render exists: `APPROX/PORTED`.
- Player position `[199,218]` after `op8` exists: `APPROX/PORTED`.
- Player direction from source transition `w=2` is currently not proven in code:
  `APPROX / NEED FIX`.
- Full `game.g` player state, marker sprite `337`, collision, and movement
  controller are not ported: `APPROX/MISSING`.

## 5. Room0 Actor Initialization Relevant To Crowd

Room0 group0 rec2:

- `op2 [15]`
- Actor ids: `36,38,39,40,41,42,43,44,45,46,47,48,49,50,51`
- Directions: `1,1,1,1,1,1,0,1,0,3,1,0,0,0,3`
- Active flags: all `1`.

Selected room0 actor table rows:

| Actor | Sprite index | Initial dir | X | Y | Notes |
|---:|---:|---:|---:|---:|---|
| 36 | 81 | 1 | 152 | 212 | visible by rec2 |
| 38 | 66 | 1 | 153 | 233 | visible by rec2 |
| 39 | 52 | 1 | 148 | 179 | visible by rec2 |
| 40 | 81 | 1 | 142 | 249 | visible by rec2 |
| 41 | 23 | 1 | 124 | 186 | visible by rec2 |
| 42 | 50 | 1 | 134 | 202 | visible by rec2 |
| 43 | 53 | 0 | 158 | 143 | visible by rec2 |
| 44 | 69 | 1 | 128 | 231 | visible by rec2 |
| 45 | 54 | 0 | 242 | 152 | visible by rec2 |
| 46 | 23 | 1 | 263 | 170 | rec2 overrides dir to 3 |
| 47 | 69 | 0 | 280 | 139 | rec2 overrides dir to 1 |
| 48 | 25 | 0 | 180 | 153 | visible by rec2 |
| 49 | 23 | 0 | 208 | 131 | visible by rec2 |
| 50 | 17 | 0 | 223 | 160 | visible by rec2 |
| 51 | 66 | 0 | 386 | 190 | rec2 overrides dir to 3 |

Rebuild status:

- Actor rows are manually represented: `APPROX/PORTED`.
- Source sprite-index resolution through `sprite.mid` is now used:
  `APPROX/PORTED`.
- Current `Actor` direction handling must be checked/fixed because constructor
  does not visibly preserve source row direction in all cases: `NEED FIX`.

## 6. Bottom / World UI Initialization

Source facts:

- `game.k.d()` binds UI controller with `S = game.h.a()` and `S.a(this)`.
- `game.h.c()` loads `/data/ui/world.ui` with resource id `257`.
- `game.h.d()` makes widgets `5` and `7` visible.
- `game.h.e()` may write room name into widget `6` when world UI is ready and
  `game.k.J` is true.
- `game.h.aS()` hides world UI widgets except `2/3/4` in certain UI modes.

Decoded `world.ui` relevant widgets:

| Widget | X | Y | W | H | Payload | Mode | Meaning from screenshot |
|---:|---:|---:|---:|---:|---:|---:|---|
| 5 | 222 | 303 | 16 | -1 | cell/frame `68` in resource `257` | 2 | lower-right four-square icon |
| 7 | 1 | 303 | 18 | -1 | cell/frame `167` in resource `257` | 2 | lower-left blue gear icon |
| 6 | 11 | 11 | 72 | -1 | text field | n/a | room name/status text |
| 2 | 42 | 277 | 44 | -2 | anim `6` in resource `257` | 3 | hidden unless UI mode shows it |
| 3 | 95 | 278 | 40 | -2 | anim `7` in resource `257` | 3 | hidden unless UI mode shows it |
| 4 | 143 | 277 | 45 | -2 | anim `8` in resource `257` | 3 | hidden unless UI mode shows it |

Important correction:

- The decoded `alt_image_ref.id` values are not actor sprite indexes.
- `ao.java` calls `m.a(257, ...)`; therefore widget ids `68` and `167` are
  cells/frames inside sprite resource `257`, backed by
  `modules/spr/original/spr_257_all(r)`.
- Rendering them as actor sprites `68` or `167` is wrong.

Rebuild status:

- Lower-left and lower-right world UI are source-backed and now render in
  rebuild from sprite resource `257`, mode `2`, cells `167` and `68`.
- Status is `PORTED/APPROX`: positions and source cells are preserved, but the
  full `ao/af/k/m` UI runtime for all widgets and modes is not ported.

## 7. Room0 Group0 Script After Init

Decoded records:

| Rec | Opcode | Args / source intent | Required status |
|---:|---:|---|---|
| 0 | 15 | `[1,3,0]` trigger from completed room3 group0 | trigger scan `APPROX` |
| 1 | 1 | full text "Muoi nam sau..." at `[10,60,90]` | text `APPROX` |
| 2 | 2 | show actors `36,38..51` with listed directions | actor show/dir `APPROX` |
| 3 | 8 | player position `[199,218]` | player pos `APPROX/PORTED`, dir needs fix |
| 4 | 12 | delay `30` | `PORTED` |
| 5 | 51 | special box `[10,10,260,220,50]` | text UI `APPROX` |
| 6 | 12 | delay `60` | `PORTED` |
| 7 | 5 | effect sprite `259`, anim `13`, actor `36` | effect `APPROX` |
| 8 | 4 | Ali dialog | dialog `APPROX/PORTED text` |
| 9 | 5 | effect sprite `259`, anim `13`, actor `50` | effect `APPROX` |
| 10 | 4 | Ti-Tan dialog | dialog `APPROX/PORTED text` |
| 11 | 5 | effect sprite `259`, anim `13`, actor `36` | effect `APPROX` |
| 12-16 | 4 | Ali / village chief / Neil dialogs | dialog `APPROX/PORTED text` |
| 17-19 | 17 | add state/items `[0,0,1]`, `[0,1,2]`, `[0,4,5]` | `APPROX`: source-backed inventory stack checks/add + `openbox.ui` reward popup using visual id `1/2`, sprite `257` anim `9`, text slot `(47,154,146,*)`; full `game.g/game.h` runtime absent |
| 20 | 39 | refresh player pets/state | `APPROX`: source loop represented; current source pet count `A=0` so no pet `I()` call occurs |
| 21 | 4 | Neil dialog | dialog `APPROX/PORTED text` |
| 22 | 45 | task tip text | task UI `APPROX` |
| 23-25 | 10 | player `-1` movement dirs `1,0,1` | movement `APPROX` |
| 26 | 22 | transition prep `[1,1,55,279,240,320]` | transition `APPROX` |
| 27 | 25 | event/battle flag `[1]` | `PORTED`: sets rebuild `sourceGameCF=false`, matching `game.c.f = args[0] == 0` |
| 28 | 6 | target `[1,1,37,1]` | room1 transition `APPROX` |
| 29 | 14 | end event | event completion `APPROX` |

## 8. What Must Be Fixed Before Claiming This Segment Is Correct

1. DONE: render world UI widgets `7` and `5` from sprite resource `257`, not
   actor sprites, at the source positions from `world.ui`.
2. DONE: preserve source direction for player after transition: initial
   direction from room3 rec101 `w=2`, then room0 `op8` keeps `this.x.n`.
3. DONE: preserve source direction for NPC actors from room table and `op2`
   overrides.
4. Keep Neil/player visible as `game.g` sprite index `0` at `[199,218]`.
5. Keep trigger chain source-backed: room0 group0 starts because room3 group0
   persisted state `3`, not because Java manually appends it.
6. Do not append room1 group0 automatically after room0 group0; room1 group0
   starts with an `op13` area trigger and requires free movement.
