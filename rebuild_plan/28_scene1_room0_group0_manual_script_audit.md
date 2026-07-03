# Scene 1 Room 0 Group 0 Manual Script Audit

Scope: `modules/event/decoded/data__event__scene_1.mid.json`,
`room_index=0`, `event_group[0]`, records `0..29`.

Implementation audited: `rebuild_game/src/main/java/VqsvIntroDemo.java`,
manual script appended after the existing `scene_1 room3 group0` transition
into `loadScene1Room0()`.

Source context:

- Previous handoff point: `scene_1 room3 group0` records `101..103` transition
  to `scene_1 room0`, map id / `unknown_ab = 2`, camera center `199,218`.
- Current group starts in `scene_1 room0`, room name `Thuy Moc Thon`, and ends
  with transition target `[scene=1, room=1, actor=37]`.

Status legend:

- `PORTED`: source args/text/target are represented in the manual script.
- `APPROX`: source intent is represented, but original engine subsystem is not
  fully ported.
- `STUB`: visible/controlled placeholder.
- `MISSING`: source record is not represented.

## Important Honesty Notes

- This is still manual scripted porting, not the full `game.c` event VM.
- `opcode 15` activation is implicit because the manual event list reaches this
  group after the previous transition; source trigger state is not modeled.
- `opcode 8` moves the player model `game.g` to `[199,218]`; current demo has
  no real `game.g`, so it centers the camera only.
- `opcode 17`, `25`, `39` mutate inventory/pet/event flags in source. Current
  demo now has source-backed minimal side effects for this room0 group:
  inventory stack checks plus `openbox.ui`-positioned popups for `op17`, a
  represented pet refresh loop for `op39`, and exact `game.c.f` flag semantics
  for `op25`. The `op17` popup now uses source sprite `257`, animation state
  `9` (`sprite.mid` row `257 -> spr_257 + images 800..803`). Text uses the
  source `openbox.ui` visual id `2` rect `(47,154,146,*)`, align `b=4`: center
  if it fits, otherwise clip horizontally rather than wrapping. The frame uses
  source visual id `1` rect `(45,147,150,*)`, sprite `257`, anim state `9`, and
  the same `m.a(..., align=0)` placement rule. Sprite cell transforms now use
  the MIDP transform constants from source `d.java` (`c={0,5,3,6,2,4,1,7}`),
  which fixes the rotated openbox corners and keeps the lower line below text.
  The full
  `game.g/game.h/game.b` runtime is still not ported, so inventory UI timing
  and pet internals remain `APPROX` where noted.
- `opcode 45` now uses the source `taskTip.ui` path, sprite `257` animation
  state `10`, visual id `1/2` layout, and horizontal scroll for over-width text.
  The close animation/state `13` is still approximate.
- `opcode 10` for actor id `-1` moves the player in source through
  `game.c case 10`: set direction, enter walk state, set speed, decrement the
  per-target duration, then return to idle/default speed. Current demo now
  performs the room0 group0 player timed motion for records `23..25`
  (`right 36`, `down 12`, `right 8`) and keeps camera centered on Neil. Full
  `game.g.q()` collision/free-world scan is still not ported.
- `opcode 6` target `[1,1,37,1]` is represented with `loadScene1Room1()` using
  source room1 map id `5` and source actor table. The current code now records
  an approximate player placement from transition actor `37`, matching
  `game.k.d()`'s actor-target branch at a high level. Full `game.i` state `22`
  loader/persistence is still not modeled.
- Room1 group0 is not automatic after this transition. Its first record is
  `opcode 13 [370,176,80,32]`, so source requires the player to reach that
  area trigger after free/world movement.

## Record Matrix

| Rec | Opcode | Source intent / args | Current implementation | Status | Notes |
|---:|---:|---|---|---|---|
| 0 | 15 | Gate/start condition `[1,3,0]` | Manual flow enters group after room3 transition | APPROX | Event activation/persist state not modeled. |
| 1 | 1 | Full text `#FFFFFFMười năm sau...`, pos `[10,60,90]` | `TextBox.full(60,90,...)` | APPROX | Text and position kept; `game.j` mode arg remains simplified. |
| 2 | 2 | Show actors `36,38..51`, dirs `1,1,1,1,1,1,0,1,0,3,1,0,0,0,3` | `setActive(...)` | APPROX | Visibility/direction set; event actor persistence not modeled. |
| 3 | 8 | Set player position `[199,218]` | `setPlayerPositionApprox(199,218)` | APPROX | Camera-centered approximation; no real `game.g`. |
| 4 | 12 | Delay `30` | `Delay(30)` | PORTED | Direct. |
| 5 | 51 | Special text `#1c6c91Tiếng huyên náo...`, box `[10,260,220,50]` | `TextBox.box(10,260,220,50,...)` | APPROX | Source text/box kept; `game.j` mode/layout not exact. |
| 6 | 12 | Delay `60` | `Delay(60)` | PORTED | Direct. |
| 7 | 5 | Effect sprite 259 anim `13` on actor `36` | `spawnActorEffect(36,13)` | APPROX | Same temp effect shortcut as earlier scene_1 group. |
| 8 | 4 | Ali dialog | `dialog("Ali", ...)` | PORTED | Text/speaker kept; UI layout not pixel-perfect. |
| 9 | 5 | Effect anim `13` on actor `50` | `spawnActorEffect(50,13)` | APPROX | Offset/layer/lifetime approximate. |
| 10 | 4 | Ti-Tan dialog | `dialog("Ti-Tan", ...)` | PORTED | Text/speaker kept. |
| 11 | 5 | Effect anim `13` on actor `36` | `spawnActorEffect(36,13)` | APPROX | Offset/layer/lifetime approximate. |
| 12 | 4 | Ali dialog | `dialog("Ali", ...)` | PORTED | Text/speaker kept. |
| 13 | 4 | Trưởng thôn `Ho!` | `dialog("Trưởng thôn","Ho!")` | PORTED | Text/speaker kept. |
| 14 | 4 | Trưởng thôn exam dialog | `dialog("Trưởng thôn", ...)` | PORTED | Text/speaker kept. |
| 15 | 4 | Neil reply, source mode `[0,0]` | `dialog("Neil", ...)` | PORTED | Text/speaker kept. |
| 16 | 4 | Trưởng thôn task direction | `dialog("Trưởng thôn", ...)` | PORTED | Text/speaker kept. |
| 17 | 17 | Add item/state `[0,0,1]` | `op17Item(0,0,1)` | APPROX | Source-backed stack check/add and `openbox.ui` popup `Đạt được: Tất Trung Cầu x 1`; uses source visual id `1/2`, sprite `257` state `9`, and text slot `(47,154,146,*)`. Full `game.g.K/game.h.S` runtime not ported. |
| 18 | 17 | Add item/state `[0,1,2]` | `op17Item(0,1,2)` | APPROX | Source-backed item name `Phong ấn cầu`, count `2`, stack limit `99`; popup uses source visual id `1/2`, sprite `257` state `9`, and text slot `(47,154,146,*)`. |
| 19 | 17 | Add item/state `[0,4,5]` | `op17Item(0,4,5)` | APPROX | Source-backed item name `Bánh Sandwich`, count `5`, bag channel from `aq.c[4][4][5]=1`; popup uses source visual id `1/2`, sprite `257` state `9`, and text slot `(47,154,146,*)`. |
| 20 | 39 | Pet/player refresh | `op39RefreshPets()` | APPROX | Source loop represented. At this point source player pet count `A=0`, so no pet `I()` calls; full pet stats are not ported. |
| 21 | 4 | Neil `Rất đơn giản...` | `dialog("Neil", ...)` | PORTED | Text/speaker kept. |
| 22 | 45 | Task notice text | `taskNotice(...)` | APPROX | Source UI path identified: `game.h.c(text)` loads `/data/ui/taskTip.ui`, sprite `257`, anim state `10`; rebuild now uses taskTip visual id `1/2` positions, text slot `(16,154,208,*)`, and horizontal scroll for over-width text following source `ae.a(... nArray[0] += 2 ...)`. Full close animation state `13` remains approximate. |
| 23 | 10 | Player `-1`, dir `1`, speed `4`, duration `36` | `op10PlayerTimedAction(1,4,36)` | APPROX | Source-backed timed player motion: walk anim, `x += 4` for 36 ticks, camera follows, then idle. Collision/free-world `game.g.q()` is not full yet. |
| 24 | 10 | Player `-1`, dir `0`, speed `4`, duration `12` | `op10PlayerTimedAction(0,4,12)` | APPROX | Source-backed timed player motion: `y += 4` for 12 ticks, camera follows, then idle. |
| 25 | 10 | Player `-1`, dir `1`, speed `4`, duration `8` | `op10PlayerTimedAction(1,4,8)` | APPROX | Source-backed timed player motion: `x += 4` for 8 ticks, camera follows, then idle before transition prep. |
| 26 | 22 | Transition prep `[1,1,55,279,240,320]` | `prepareTransition(55,279,240,320)` | APPROX | Source target/camera kept; full state 22 not modeled. |
| 27 | 25 | Flag `[1]` | `op25SetGameFlag(1)` | PORTED | Exact source semantic: `game.c.f = args[0] == 0`, so this record sets it to `false`. |
| 28 | 6 | Transition target `[1,1,37,1]` | `markWorldTransition(1,1,37)` + `loadScene1Room1(55,279)` + actor37 player placement approx | APPROX | Room1 map id `5` and actor table loaded; player approximation is tile-aligned to actor37 `(16,272)` for map5 tile size 16. Event persistence and full `game.k.d()` are not modeled. |
| 29 | 14 | End event | End of manual list for this group | APPROX | Completion/persist state not modeled. |

## Current Totals

- `PORTED`: 11 records
- `APPROX`: 19 records
- `STUB`: 0 records
- `MISSING`: 0 records

## Verification

Smoke/build/check run after implementation:

- Compile/build script completed:
  `powershell -ExecutionPolicy Bypass -File .\build.ps1`
- Smoke baseline after room0 transition:
  `java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke ".\build_intro_demo\scene1_room0_after_6200.png" 6200`
- Smoke group0 dialog:
  `java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke ".\build_intro_demo\scene1_room0_group0_text_5400.png" 5400`
- Smoke end/transition path:
  `java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke ".\build_intro_demo\scene1_room0_group0_after_16000.png" 16000`
- Smoke op17 inventory popup:
  `java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke ".\build_intro_demo\scene1_openbox_text_lift_5700.png" 5700`
- Smoke op17 third reward popup:
  `java "-Dvqsv.modules=..\modules" -cp ".\build\classes" VqsvIntroDemo --smoke ".\build_intro_demo\scene1_openbox_text_lift_5810.png" 5810`
- Reflection trace confirmed event list reached end after this group:
  `eventIndex=248`, `nextWorldG=1`.
- Final jar check passed:
  `java "-Dvqsv.modules=..\modules" -jar ".\build\libs\vqsv-rebuild-skeleton.jar" --check`
- Final room0 group0 smoke set:
  - `build_intro_demo/room0_group0_final_5200_year_text.png`
  - `build_intro_demo/room0_group0_final_5400_crowd.png`
  - `build_intro_demo/room0_group0_final_5700_openbox.png`
  - `build_intro_demo/room0_group0_final_5800_tasktip_start.png`
  - `build_intro_demo/room0_group0_final_5830_tasktip_mid.png`
  - `build_intro_demo/room0_group0_final_5860_tasktip_late.png`
  - `build_intro_demo/room0_group0_final_6200_after_group.png`
  - `build_intro_demo/room0_group0_final_sheet.png`
- Opcode 10 player movement smoke set after source-backed motion port:
  - `build_intro_demo/room0_op10_sheet.png`
  - `build_intro_demo/room0_op10_5865_right_start.png`
  - `build_intro_demo/room0_op10_5885_right_late.png`
  - `build_intro_demo/room0_op10_5905_down_mid.png`
  - `build_intro_demo/room0_op10_5915_right_final.png`
  - `build_intro_demo/room0_op10_5920_room1_loaded.png`
- Reflection probe for the same auto-advance path confirmed source args:
  - tick `5865`: record 23 active, player `[211,218]`, dir `1`
  - tick `5885`: record 23 active, player `[291,218]`, dir `1`
  - tick `5905`: record 24 active, player `[343,246]`, dir `0`
  - tick `5915`: record 25 active, player `[363,266]`, dir `1`
  - tick `5920`: group complete, room1 loaded, player `[16,272]`

Visual proof:

- `build_intro_demo/scene1_room0_group0_text_5400.png` shows room0 group0
  dialog `Trưởng thôn / Ho!` with actors visible.

## Safe Next Steps

1. Minimal source-backed world/free-movement and trigger scan is now represented
   in [32_scene1_room1_freeworld_op13_audit.md](32_scene1_room1_freeworld_op13_audit.md).
   Room1 group0 still should not auto-run; it starts only after player reaches
   `op13 [370,176,80,32]`.
2. Port `scene_1 room1 group0` records `1..13` after auditing the battle/task
   cluster that follows the op13 trigger.
3. Continue room0 sprite resource work so newly shown actors do not stay blank.
4. Replace the remaining `APPROX` inventory/pet popup/runtime with real
   `game.g/game.h/game.b` equivalents when those modules are ported.
