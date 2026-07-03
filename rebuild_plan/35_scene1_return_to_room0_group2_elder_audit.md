# Scene 1 Return To Room0 Group2 Elder Audit

Date: 2026-07-03

Scope: source-backed audit for the next slice after `scene_1 room1 group0`
Bunny capture success. This file does not implement the behavior. It defines
the chain that the rebuild must follow before porting `scene_1 room0 group2`.

Status legend:

- `VERIFIED`: directly backed by decoded event JSON and/or decompiled source.
- `PORTED`: implemented in rebuild with source-backed behavior.
- `APPROX`: represented, but original subsystem is not fully ported.
- `STUB`: deliberate placeholder.
- `PENDING`: source-known, not implemented in this slice yet.
- `UNKNOWN`: not enough evidence yet.

## 1. Source Files Read

- `modules/event/decoded/data__event__scene_1.mid.json`
  - room1 group0 lines around `5000..5180`
  - room0 group2 lines around `1848..2076`
  - room0 actor index `52`
- `modules/source_code/decoded/decompiled_source_cfr/game/c.java`
  - `game.c.b()` trigger scan
  - trigger cases `16` and `86`
  - executor cases `23`, `45`, and `5`
- `rebuild_game/src/main/java/VqsvIntroDemo.java`
  - `loadScene1Room0()`
  - `loadScene1Room1()`
  - room1 Bunny capture stub and source trace helpers

## 2. Proven Event Chain

The correct chain is:

```text
scene_1 room1 group0 success path
  rec8:  op23 [1,0,1]
    -> marks event state scene=1, room=0, event=1 as complete/state 3
  rec10: op14 []
    -> completes current event scene=1, room=1, group=0 as state 3

return to scene_1 room0
  room0 group2 rec0: op86 [1,1,0]
    -> event group is eligible only if scene=1, room=1, event=0 is complete/state 3

player interacts with elder
  room0 group2 rec1: op16 [52]
    -> requires selected/interacted actor game.k.u == 52 and confirm flag game.c.h

then room0 group2 records 2..15 execute
```

Important: room0 group2 must not auto-run just because Bunny was captured.
`op86` can make the group eligible, but `op16 [52]` still requires the player
to interact with actor `52`.

Important correction: `op23 [1,0,1]` and `op86 [1,1,0]` are different event
state keys. The `op23` record marks room0 group1 complete/progress. The
`op86` gate for room0 group2 depends on room1 group0 being complete, which is
produced by normal current-event completion at `op14`.

## 3. Evidence: Room1 Bunny Success Marks Progress

Decoded event JSON:

```text
scene_1 room1 group0
rec6  op4  Neil: "ChÃ­nh lÃ  con thá» cá»§a ngÆ°Æ¡i, mau giÃºp ta bÃ¡o cÃ¡o káº¿t quáº£ Ä‘á»ƒ vÆ°á»£t qua"
rec7  op56 [1,1] strings "50", "0"
rec8  op23 [1,0,1]
rec9  op40 "Trá»Ÿ vá» tÃ¬m trÆ°á»Ÿng thÃ´n!"
rec10 op14 []
```

Source behavior in `game.c case 23`:

```text
this.b[game.k.a(args[0], args[1])][args[2]] = 3
```

For `op23 [1,0,1]`, the event-state table entry
`state[scene=1, room=0, event=1]` is set to `3`.

This is required story progress, but it is not the direct gate consumed by
room0 group2 `op86 [1,1,0]`.

Source behavior for `op14` and active event completion:

```text
case 14:
    currentEvent.state = 3

tail:
    if currentEvent.state == 3 or 4:
        state[currentSceneRoom][currentEventId] = currentEvent.state
```

For room1 group0, this is the source path that should persist
`state[scene=1, room=1, event=0] = 3`.

Current rebuild status:

- `op23MarkEventComplete(1,0,1)` now writes a minimal source-shaped event-state
  table entry `state[1,0,1] = 3`: `PORTED` for this slice.
- `op14CompleteEvent(1,1,0)` now writes `state[1,1,0] = 3`: `PORTED` for the
  room0 group2 gate.
- This is still not a generic `game.c` event VM; it is a minimal table for the
  currently ported manual flow.

## 4. Evidence: Room0 Group2 Is Gated By op86

Decoded event JSON:

```text
scene_1 room0 group2
rec0 op86 [1,1,0]
rec1 op16 [52]
```

Source behavior in `game.c.b()` trigger scan:

```text
case 86:
    if state[game.k.a(args[0], args[1])][args[2]] != 3:
        break
    // fall through to trigger event
```

For `op86 [1,1,0]`, room0 group2 is eligible only when
`state[scene=1, room=1, event=0] == 3`.

This matches the current story progression: room1 group0 must complete first.

Current rebuild status:

- Room1 group0 success path ends with `op14CompleteEvent(1,1,0)`, and the
  current rebuild now writes `state[1,1,0] = 3`.
- A trace entry `op86 gate preview [1,1,0]=3 complete=true` is emitted after
  room1 group0 completion.
- Do not bypass this by directly appending room0 group2.

## 5. Evidence: op16 Requires Actor 52 Interaction

Decoded event JSON:

```text
scene_1 room0 group2 rec1: op16 [52]
```

Source behavior in `game.c.b()` trigger scan:

```text
case 16:
    if args[0] != game.k.u:
        break
    game.c.g = true
    if !game.c.h:
        break
    stop player
    game.c.h = false
    trigger event
```

Meaning:

- `game.k.u` must equal actor id `52`.
- `game.c.h` must be true, which is the confirm/interact flag.
- Therefore the event is interaction-gated. It is not an automatic map entry
  event and must not run merely because the player returned to room0.

Current rebuild status:

- The demo has click/key confirm for text and menu flow.
- It still needs a minimal source-shaped world interaction model for actor
  selection:
  - determine nearby/selectable actor,
  - set `game.k.u` equivalent to `52`,
  - on `0`/click set confirm,
  - then activate room0 group2.

## 6. Evidence: Actor 52 Is The Elder Target In Room0

Decoded room0 actor table at index `52`:

```text
52: kind=0 values=[0,51,0,200,190,1,1,1,0,0,0,4,8]
```

Relevant fields from the source room actor format:

```text
record[0] = actor type
record[1] = sprite table index
record[2] = direction/state
record[3] = world x
record[4] = world y
record[5] = visible flag
record[6] = behavior/subtype
```

So actor `52` is:

- type `0`
- sprite table index `51`
- direction/state `0`
- position `[200,190]`
- visible flag `1`
- subtype/behavior `1`

Current rebuild row in `loadScene1Room0()`:

```text
{52, 51, 0, 200, 190, 1, 1, 1}
```

This matches the source fields needed for the next interaction.

Status:

- Actor id/position/sprite-index are `VERIFIED`.
- Sprite index `51` is now `READY` in
  `24_scene1_room0_sprite_mapping_audit.md`: source row `[51,136]`, runtime
  `spr_51_all(r)`, runtime `img/136.png`, and fallback mapping `{51,51,136}`
  have been ported.

## 7. Evidence: Room1 Return Transition To Room0

Decoded room1 actor table:

```text
37: kind=0 values=[1,223,2,19,273,1,1,1,1,0,30]
```

Source actor parse for type `1` in `game.a.a(short[], int)` maps:

```text
record[7]  -> C  transition direction slot
record[8]  -> N  target scene
record[9]  -> O  target room
record[10] -> P  target actor
```

For room1 actor `37`:

- type `1` transition actor
- source position `[19,273]`
- `C=1`
- target `[scene=1, room=0, actor=30]`

Source `game.a` uses direction table `R = {2,3,0,1}`. Therefore `C=1`
requires player direction `3` (left). When the player overlaps the transition
actor and faces that direction, source sets target `f/g/j` and switches runtime
state.

Decoded room0 paired actor:

```text
30: kind=0 values=[1,223,3,408,273,1,1,3,1,1,37]
```

Rebuild status:

- `PORTED`: `ActorTransitionFreeWorldTrigger` waits after room1 Bunny success
  and task notice completion.
- It requires source overlap with actor `37` and player direction `3`.
- It loads `scene_1 room0` and places player using target actor `30`.
- The target/actor/direction are source-backed.
- Collision now uses source sprite masks from `spr_*_all(r)`:
  - `aa.a()` reads first post-animation table into `o.d` and second into
    `o.c`.
  - `d.k()` returns `o.d`, used as player movement/collision mask.
  - `d.j()` returns `o.c`, used here as actor transition hit/trigger mask,
    matching `game.a` type-1 transition checks.
  - Overlap follows source `ae.a(x1,y1,x2,y2, mask1, mask2)` using the first
    4 mask values. Rectangle fallback remains only if a sprite mask/resource is
    missing.

## 8. Room0 Group2 Record Matrix

Source: `modules/event/decoded/data__event__scene_1.mid.json`,
`room_index=0`, `group_index=2`, records `0..15`.

| Rec | Opcode | Args/Text | Source meaning | Required rebuild plan | Status |
|---:|---:|---|---|---|---|
| 0 | 86 | `[1,1,0]` | Gate on room1 group0 complete/state 3 | Minimal event-state table now supports this check; group activation still pending | PORTED/PENDING |
| 1 | 16 | `[52]` | Actor interaction trigger with actor 52 | Require player near/selected actor 52 and confirm/click | PENDING |
| 2 | 4 | Neil: `Bá»‹ báº¯t` | Dialog | Source text/speaker dialog | PENDING |
| 3 | 4 | TrÆ°á»Ÿng thÃ´n: `Nhá»¯ng con thá» trÃ´ng dá»… thÆ°Æ¡ng lÃ m sao.` | Dialog | Source text/speaker dialog | PENDING |
| 4 | 5 | `[0,0,9,0,0]` | Spawn effect sprite 259 anim 9 attached to player | Use existing op5 effect path, mark approximate | PENDING/APPROX |
| 5 | 12 | `[15]` | Delay | Delay 15 ticks | PENDING |
| 6 | 4 | Neil: `TrÆ°á»Ÿng thÃ´n ... cÃ³ váº» má»¥c tiÃªu sai ...` | Dialog | Source text/speaker dialog | PENDING |
| 7 | 4 | TrÆ°á»Ÿng thÃ´n long explanation | Dialog | Source text/speaker dialog | PENDING |
| 8 | 5 | `[0,0,14,0,0]` | Spawn effect sprite 259 anim 14 attached to player | Use existing op5 effect path, mark approximate | PENDING/APPROX |
| 9 | 12 | `[15]` | Delay | Delay 15 ticks | PENDING |
| 10 | 4 | Neil: `Tá»‘t quÃ¡! Ta Ä‘i xem!` | Dialog | Source text/speaker dialog | PENDING |
| 11 | 4 | TrÆ°á»Ÿng thÃ´n: choose only one pet | Dialog | Source text/speaker dialog | PENDING |
| 12 | 4 | Neil: `Tháº¿ nÃ y cháº£ báº±ng cho Ã ?` | Dialog | Source text/speaker dialog | PENDING |
| 13 | 5 | `[1,52,3,0,0]` | Spawn effect sprite 259 anim 3 attached to actor 52 | Attach effect to actor 52, mark approximate | PENDING/APPROX |
| 14 | 45 | `[1]` text `Lá»±a chá»n sá»§ng váº­t cÃ¹ng trÆ°á»Ÿng thÃ´n tá»· thÃ­.` | Task notice through `game.h.c(text)` / taskTip UI | Use existing taskTip approximation; source text preserved | PENDING/APPROX |
| 15 | 14 | `[]` | Complete active group | Persist room0 group2 complete/state 3 | PENDING |

Implementation update 2026-07-03:

- Records `0..15` are now manually ported in `VqsvIntroDemo.java`.
- Rec0 `op86 [1,1,0]`: `PORTED`; checks source event state
  `[1,1,0] == 3`.
- Rec1 `op16 [52]`: `PORTED/APPROX`; requires player confirm/click on actor
  `52`, emulating `game.k.u == 52` with source sprite-mask interaction.
- Rec2,3,6,7,10,11,12 `op4`: `PORTED`; source dialog ordering and speaker
  layout are preserved through existing dialog renderer.
- Rec4,8,13 `op5`: `PORTED/APPROX`; uses source sprite `259` and source anim
  ids `9`, `14`, `3`, but full original `f` effect lifecycle/layer timing is
  still approximate.
- Rec5,9 `op12`: `PORTED`; delay 15 ticks.
- Rec14 `op45`: `PORTED/APPROX`; taskTip frame/text appears, but generic
  `game.h` task state remains approximate.
- Rec15 `op14`: `PORTED`; writes `state[1,0,2] = 3`.
- Supersedes the older `PENDING` statuses in the table above; the table is
  kept for source record reference.

Map/actor re-audit 2026-07-03:

- `scene_1 room0` source has `unknown_ab=2`, actor count `58`, and declared
  room block size `16303`.
- `loadScene1Room0()` uses map id `2`.
- `loadScene1Room0()` rows match all `58/58` source actor rows for
  `[actorIndex,sprite,state,x,y,visible,variant,layer]`; no actor-table
  mismatch was found.
- The real missing-image/logic issue after the first group2 port was not map
  id or actor row data. It was the next source chain:
  - room0 group3 rec0 `op15 [1,0,2]`;
  - rec1 `op2` shows pet actors `53,54,55`;
  - rec2 `op38` waits for interaction with one of those pets.
- Rebuild now starts a `Room0Group3PetOffer` after group2 `op14`: it shows
  actors `53,54,55` and waits for pet interaction.
- `op35/op87` pet choice/grant and subsequent battle chain remain
  `PENDING/APPROX`; this audit does not claim full pet-selection completion.

## 9. Implementation Requirements Before Coding

The next implementation should not be considered done unless:

1. DONE: `op23 [1,0,1]` writes a queryable `state[1,0,1] = 3` entry. This is
   required story progress, but it is not the direct `op86` gate for group2.
2. DONE: completing room1 group0 through `op14` writes `state[1,1,0] = 3`, so
   room0 group2 `op86` can pass by source logic.
3. DONE: the player can return to room0 through room1 transition actor `37`,
   target `[1,0,30]`, using source direction slot `C=1 -> dir=3` and source
   sprite masks for overlap.
4. Room0 group2 does not start immediately on room load.
5. Interaction with actor 52 is required:
   - near/select actor 52,
   - press `0` or click/tap confirm,
   - then run records `2..15`.
6. Every record `0..15` is represented or explicitly marked as `PENDING`,
   `APPROX`, or `STUB`.
7. DONE: actor 52 remains source row `{52,51,0,200,190,1,1,1}` and sprite
   index `51` resource is ported. Do not swap sprite.
8. Build/check pass.
9. Smoke images are generated and visually inspected.

## 10. Current Smoke Reality

At the time this audit is written, smoke can verify the current implemented
state up to room1 group0 completion and return-to-room0:

- player can reach room1 `op13 [370,176,80,32]`;
- Bunny battle/capture path runs through a visible `STUB`;
- actor 50 is hidden by `op56`;
- source trace records `op23 [1,0,1]`, `op40`, and `op14`.
- player can return through room1 actor `37` to room0 actor `30`.

Smoke cannot yet honestly verify room0 group2 execution, because:

- minimal event-state table exists, but generic `game.c` VM activation is not
  complete;
- `op16 [52]` actor interaction runner is not implemented;
- group2 records `0..15` are still `PENDING`.

Verification run on 2026-07-03:

```powershell
$env:PROJECT_ROOT = "<path-to-project-root>"
$env:MODULES_ROOT = Join-Path $env:PROJECT_ROOT "modules"
$env:REBUILD_GAME = Join-Path $env:PROJECT_ROOT "rebuild_game"
cd $env:REBUILD_GAME
powershell -ExecutionPolicy Bypass -File .\build.ps1
java "-Dvqsv.modules=$env:MODULES_ROOT" -jar ".\build\libs\vqsv-rebuild-skeleton.jar" --check
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp ".\build\classes" VqsvIntroDemo --smoke-drive ".\build_intro_demo\audit35_room1_start.png" 5920 "" 0
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp ".\build\classes" VqsvIntroDemo --smoke-drive ".\build_intro_demo\audit35_room1_op13_trigger.png" 5920 "R90,U20" 0
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp ".\build\classes" VqsvIntroDemo --smoke-drive ".\build_intro_demo\audit35_room1_battle_stub.png" 5920 "R90,U20" 20
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp ".\build\classes" VqsvIntroDemo --smoke-drive ".\build_intro_demo\audit35_room1_complete.png" 5920 "R90,U20" 210
```

Additional event-state verification run:

```powershell
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp ".\build\classes" VqsvIntroDemo --smoke-drive ".\build_intro_demo\event_state_room1_complete.png" 5920 "R90,U20" 210
```

Build and `--check` passed after adding minimal event-state storage to
`VqsvIntroDemo.java`.

Additional return transition verification run:

```powershell
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp ".\build\classes" VqsvIntroDemo --smoke-drive ".\build_intro_demo\return_room1_to_room0_attempt3.png" 5920 "R90,U20,L330,D15,L20" 20
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp ".\build\classes" VqsvIntroDemo --smoke-drive ".\build_intro_demo\return_room1_to_room0_mask.png" 5920 "R90,U20,L330,D15,L20" 20
```

Observed images:

- `audit35_room1_start.png`: room1 loaded after room0 group0 transition; Neil
  starts near the source transition/actor placement.
- `audit35_room1_op13_trigger.png`: route `R90,U20` reaches the Bunny trigger
  area and begins the battle/capture transition overlay.
- `audit35_room1_battle_stub.png`: visible `Scripted stub`, confirming this
  is not the real `game.d` battle engine.
- `audit35_room1_complete.png`: task notice `Trá»Ÿ vá» tÃ¬m trÆ°á»Ÿng thÃ´n!` is shown
  after the success path. Actor 50 is no longer visible in the frame.
- `return_room1_to_room0_attempt3.png`: final image is room0 after taking the
  source-backed transition actor `37 -> [1,0,30]`; no elder/group2 dialog is
  visible yet.
- `return_room1_to_room0_mask.png`: same route still reaches room0 after
  replacing rectangle actor overlap with source sprite-mask overlap.

Additional room0 group2 verification run:

```powershell
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp ".\build\classes" VqsvIntroDemo --smoke-drive ".\build_intro_demo\room0_group2_actor52_first_dialog.png" 5920 "R90,U20,L330,D15,L20,L30,U20,0" 5
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp ".\build\classes" VqsvIntroDemo --smoke-drive ".\build_intro_demo\room0_group2_post600.png" 5920 "R90,U20,L330,D15,L20,L30,U20,0" 600
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp ".\build\classes" VqsvIntroDemo --smoke-drive ".\build_intro_demo\room0_group2_actor52_task_notice.png" 5920 "R90,U20,L330,D15,L20,L30,U20,0" 900
java "-Dvqsv.modules=$env:MODULES_ROOT" -cp ".\build\classes" VqsvIntroDemo --smoke-drive ".\build_intro_demo\room0_group3_pet_offer.png" 5920 "R90,U20,L330,D15,L20,L30,U20,0" 650
```

Observed:

- `room0_group2_actor52_first_dialog.png`: after return to room0, route
  `L30,U20,0` reaches actor `52` and triggers the first group2 dialog.
- `room0_group2_post600.png`: `op45` task notice is visible.
- `room0_group2_actor52_task_notice.png`: after auto-advancing the chain,
  group2 completes and the script reaches room0 group3 pet-offer wait.
- `room0_group3_pet_offer.png`: after group2 completion, room0 group3 starts,
  pet actors `53,54,55` are visible, and the script waits for pet interaction.

Honesty note: these smoke images verify the manual room0 group2 port. They do
not prove the generic `game.c` VM runner, full `game.h` task system, or full
`op35/op87` pet-selection branch yet.

## 11. Smoke Plan For Next Implementation

Required smoke images after implementation:

1. `scene1_room1_after_bunny_complete.png`
   - room1 shown after Bunny capture success;
   - actor 50 hidden;
   - task notice `Trá»Ÿ vá» tÃ¬m trÆ°á»Ÿng thÃ´n!` either visible or recently completed.
2. `scene1_return_room0_loaded.png`
   - room0 loaded;
   - group2 has not started automatically;
   - no elder dialog visible yet.
3. `scene1_room0_near_actor52_before_interact.png`
   - player near actor 52 at/around `[200,190]`;
   - no group2 dialog yet.
4. `scene1_room0_group2_first_dialog.png`
   - after pressing `0`/click on actor 52;
   - first dialog is Neil: `Bá»‹ báº¯t`.
5. `scene1_room0_group2_task_notice.png`
   - task notice text `Lá»±a chá»n sá»§ng váº­t cÃ¹ng trÆ°á»Ÿng thÃ´n tá»· thÃ­.`.
6. `scene1_room0_group2_complete.png`
   - group2 finished;
   - event state for room0 group2 persisted/trace-visible.

If any image needs a debug route or smoke-drive shortcut, the filename and trace
must label that shortcut. Do not present a shortcut as original gameplay.

## 12. Next Safe Coding Slice

Recommended minimal code slice:

1. DONE: add tiny event-state storage keyed by `(scene, room, group)`.
2. DONE: make `op23MarkEventComplete(...)` write the explicit target state from
   opcode args, for this slice `state[1,0,1] = 3`.
3. DONE: make `op14CompleteEvent(...)` write the current active event state, for this
   slice `state[1,1,0] = 3`.
4. DONE: add room1-to-room0 return transition through actor `37`, target
   `[1,0,30]`, using source sprite-mask overlap.
5. DONE: add a blocking free-world interaction state in room0 for actor `52`,
   using source sprite-mask interaction and confirm/click.
6. DONE: port room0 group2 records `0..15`.
7. DONE: build, check, smoke, and update this audit with actual implementation
   statuses.


