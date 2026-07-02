# Scene 1 Room 3 Group 0 Manual Script Audit

Scope: `event/decoded/data__event__scene_1.mid.json`, `room_index=3`, `event_group[0]`, records `0..103`.

Implementation audited: `rebuild_game/src/main/java/VqsvIntroDemo.java`, current manual intro script around `makeEvents()`.

Status legend:

- `PORTED`: manual script has the corresponding record and preserves the main source arguments.
- `APPROX`: record exists, but helper behavior is still an approximation of the original engine.
- `STUB`: intentionally replaced by a visible/controlled stub.
- `MISSING`: source record is not implemented, or only implied by a nearby unrelated action.

Important honesty notes:

- Dialog opcode 4 is now ported at workflow level: speaker tab, paging, confirm key/click. It is not yet pixel-perfect against `game.h`/`ae` text layout.
- Actor movement opcode 10 is source-directed but still approximate: it now starts on the next tick like `game.c` state 5 and moves by source speed/duration, but it does not yet fully model original `actor.h()` movement-state/collision/path completion.
- Effects opcode 5 are anchored to sprite 259 and cycle-complete, but exact sprite offset/layer lifetime can still differ.
- Battle opcodes 67/37/52/32/47 are a visible stub, not `game.d` battle engine.

## Record Matrix

| Rec | Opcode | Source intent / args | Current implementation | Status | Notes |
|---:|---:|---|---|---|---|
| 0 | 15 | Gate/start condition `[0,1,0]` | Manual route enters this group after scene_0/scene_5 transition (`loadScene1Room3Entry`) | APPROX | Original event activation state is not modeled as opcode 15; manual flow assumes this group is active. |
| 1 | 1 | Full text `#FFFFFF Sau nam sau ...`, pos `[10,60,90]` | `TextBox.full(60,90,...)` | APPROX | Text exists; first arg/type handling from `game.j` is simplified. |
| 2 | 2 | Show actors `48,49,50`, dirs `1,2,2`, active flags `1,1,1` | `setActive(48,49,50)` | APPROX | Visibility/direction set; original actor state persistence not fully modeled. |
| 3 | 11 | Camera snap to actor 49 | `new CameraPan(49,0)` | PORTED | Main behavior preserved. |
| 4 | 12 | Delay `15` | `new Delay(15)` | PORTED | Direct. |
| 5 | 11 | Camera pan to actor 48 speed 10 | `new CameraPan(48,10)` | APPROX | Pan exists; exact `ai` camera easing/clamp may differ. |
| 6 | 4 | Neil dialog: `Den day di!...` mode 0 | `dialog("Neil", ...)` | PORTED | Dialog workflow present; layout still not pixel-perfect. |
| 7 | 7 | Actor action 49,50 mode `0,0`, dir `0,0` | `ActionSet(49,50)` | APPROX | Now waits one sprite cycle; exact `actor.b()` timing may differ. |
| 8 | 10 | Move 49,50 dir 0 speed 4 duration 13 | `TimedAction(49,50, dir 0, speed 4, dur 13)` | APPROX | Movement simplified. |
| 9 | 10 | Move 49,50 dir 3 speed 4 duration 13 | `TimedAction(...)` | APPROX | Movement simplified. |
| 10 | 10 | Move 49,50 dir 0 speed 4 duration 23 | `TimedAction(...)` | APPROX | Movement simplified. |
| 11 | 10 | Move 49,50 dir 3 speed 4 duration 20 | `TimedAction(...)` | APPROX | Movement simplified. |
| 12 | 10 | Move 49,50 dir 2 speed 4 duration 18 | `TimedAction(...)` | APPROX | Movement simplified. |
| 13 | 7 | Actor action 49,50 mode 1 dir 3 | `ActionSet(49,50, mode 1, dir 3)` | APPROX | Animation-cycle wait approximate. |
| 14 | 7 | Actor action 49,50 mode 1 dir 1 | `ActionSet(...)` | APPROX | Animation-cycle wait approximate. |
| 15 | 7 | Actor action 49,50 mode 2 dir 2 | `ActionSet(...)` | APPROX | Animation-cycle wait approximate. |
| 16 | 5 | Actor effect sprite 259 anim 14 on actor 49 | `spawnActorEffect(49,14)` | APPROX | Anchored effect exists; exact offset/layer may differ. |
| 17 | 12 | Delay `15` | `Delay(15)` | PORTED | Direct. |
| 18 | 4 | Sophie dialog | `dialog("Sophie", ...)` | PORTED | Dialog workflow present. |
| 19 | 10 | Move 48 dir 0 speed 4 duration 6 | `TimedAction(48,...)` | APPROX | Movement simplified. |
| 20 | 10 | Move 48 dir 1 speed 4 duration 13 | `TimedAction(48,...)` | APPROX | Movement simplified. |
| 21 | 10 | Move 48 dir 2 speed 4 duration 8 | `TimedAction(48,...)` | APPROX | Movement simplified. |
| 22 | 4 | Neil dialog | `dialog("Neil", ...)` | PORTED | Direct text. |
| 23 | 5 | Effect anim 7 on actor 49 | `spawnActorEffect(49,7)` | APPROX | Effect exists; exact offset/layer may differ. |
| 24 | 7 | Actor action 49,50 mode 0 dir 0 | `ActionSet(49,50,0,0)` | APPROX | Animation-cycle wait approximate. |
| 25 | 4 | Sophie dialog | `dialog("Sophie", ...)` | PORTED | Direct text. |
| 26 | 4 | Neil dialog | `dialog("Neil", ...)` | PORTED | Direct text. |
| 27 | 4 | Sophie dialog | `dialog("Sophie", ...)` | PORTED | Direct text. |
| 28 | 10 | Move 48,49,50 dir 0 speed 4 duration 13 | `TimedAction(48,49,50,...)` | APPROX | Movement simplified. |
| 29 | 10 | Move 48,49,50 dir 1 speed 4 duration 20 | `TimedAction(...)` | APPROX | Movement simplified. |
| 30 | 7 | Actor action 48 mode 2 dir 2 | `ActionSet(48,2,2)` | APPROX | Animation-cycle wait approximate. |
| 31 | 4 | Neil dialog | `dialog("Neil", ...)` | PORTED | Direct text. |
| 32 | 7 | Actor action 49 mode 0 dir 0 | `ActionSet(49,0,0)` | APPROX | Animation-cycle wait approximate. |
| 33 | 4 | Sophie dialog | `dialog("Sophie", ...)` | PORTED | Direct text. |
| 34 | 5 | Effect anim 8 on actor 48 | `spawnActorEffect(48,8)` | APPROX | Effect exists; exact offset/layer may differ. |
| 35 | 4 | Neil dialog | `dialog("Neil", ...)` | PORTED | Direct text. |
| 36 | 10 | Move 49,50 dir 2 speed 4 duration 16 | `TimedAction(49,50,...)` | APPROX | Movement simplified. |
| 37 | 7 | Actor action 49,50 mode 0 dir 0 | `ActionSet(49,50,0,0)` | APPROX | Animation-cycle wait approximate. |
| 38 | 4 | Sophie long dialog | `dialog("Sophie", ...)` | APPROX | Text is present and pages; line/page split may differ from `ae`. |
| 39 | 4 | Neil `...` dialog | `dialog("Neil","...")` | PORTED | Direct text. |
| 40 | 4 | Sophie dialog | `dialog("Sophie", ...)` | PORTED | Direct text. |
| 41 | 4 | Neil dialog | `dialog("Neil", ...)` | PORTED | Direct text. |
| 42 | 4 | Sophie long dialog | `dialog("Sophie", ...)` | APPROX | Text is present and pages; line/page split may differ from `ae`. |
| 43 | 12 | Delay `15` | `Delay(15)` | PORTED | Direct. |
| 44 | 10 | Move 48 dir 2 speed 4 duration 10 | `TimedAction(48,...)` | APPROX | Movement simplified. |
| 45 | 4 | Neil dialog | `dialog("Neil", ...)` | PORTED | Direct text. |
| 46 | 5 | Effect anim 5 on actor 49 | `spawnActorEffect(49,5)` | APPROX | Effect exists; exact offset/layer may differ. |
| 47 | 4 | Sophie dialog | `dialog("Sophie", ...)` | PORTED | Direct text. |
| 48 | 4 | Neil `...` dialog | `dialog("Neil","...")` | PORTED | Direct text. |
| 49 | 4 | Sophie dialog | `dialog("Sophie", ...)` | PORTED | Direct text. |
| 50 | 4 | Neil long dialog | `dialog("Neil", ...)` | APPROX | Text is present and pages; line/page split may differ from `ae`. |
| 51 | 5 | Effect anim 14 on actor 49 | `spawnActorEffect(49,14)` | APPROX | Effect exists; exact offset/layer may differ. |
| 52 | 4 | Sophie dialog | `dialog("Sophie", ...)` | PORTED | Direct text. |
| 53 | 4 | Neil dialog | `dialog("Neil", ...)` | PORTED | Direct text. |
| 54 | 2 | Show actors 53,54,55,56 dirs 0 | `setActive(53,54,55,56)` | APPROX | Visibility/direction set; original state persistence not fully modeled. |
| 55 | 10 | Move 53..56 dir 0 speed 4 duration 23 | `TimedAction(53..56,...)` | APPROX | Movement simplified. |
| 56 | 10 | Move 53..56 dir 3 speed 4 duration 15 | `TimedAction(53..56,...)` | APPROX | Movement simplified. |
| 57 | 7 | Actor action 53..56 mode 0 dir 0 | `ActionSet(53..56,0,0)` | APPROX | Animation-cycle wait approximate. |
| 58 | 4 | `??` dialog | `dialog("??", ...)` | PORTED | Direct text. |
| 59 | 5 | Effect anim 7 on actor 48 | `spawnActorEffect(48,7)` | APPROX | Effect exists; exact offset/layer may differ. |
| 60 | 5 | Effect anim 7 on actor 49 | `spawnActorEffect(49,7)` | APPROX | Effect exists; exact offset/layer may differ. |
| 61 | 7 | Actor action 49 mode 2 dir 2 | `ActionSet(49,2,2)` | APPROX | Animation-cycle wait approximate. |
| 62 | 4 | Neil dialog | `dialog("Neil", ...)` | PORTED | Direct text. |
| 63 | 7 | Actor action 49,50 mode 0 dir 0 | `ActionSet(49,50,0,0)` | APPROX | Animation-cycle wait approximate. |
| 64 | 10 | Move 53,49,56,50 dir 0 speeds 6/4/6/4 duration 4 | `TimedAction(...)` | APPROX | Movement simplified. |
| 65 | 10 | Move 53,49,56,50 dir 2 speed 4 duration 6 | `TimedAction(...)` | APPROX | Movement simplified. |
| 66 | 4 | Sophie dialog | `dialog("Sophie", ...)` | PORTED | Direct text. |
| 67 | 10 | Move 48 dir 2 speed 4 duration 6 | `TimedAction(48,...)` | APPROX | Movement simplified. |
| 68 | 4 | Neil dialog | `dialog("Neil", ...)` | PORTED | Direct text. |
| 69 | 7 | Actor action 53,56,49 mode 0 dir 0 | `ActionSet(53,56,49,0,0)` | APPROX | Animation-cycle wait approximate. |
| 70 | 4 | `??` dialog | `dialog("??", ...)` | PORTED | Direct text. |
| 71 | 4 | `??` dialog | `dialog("??", ...)` | PORTED | Direct text. |
| 72 | 67 | Set battle/event actor `56` | `ScriptedBattleStub(actor=56,...)` | STUB | State captured inside visible battle stub. |
| 73 | 37 | Battle encounter `[5,20,4]` | `ScriptedBattleStub(encounter=[5,20,4])` | STUB | Visible placeholder, not `game.d`. |
| 74 | 52 | Battle flags `[1,1]` | `ScriptedBattleStub(flags=[1,1])` | STUB | Captured; no full side effects. |
| 75 | 32 | Enter battle mode `[0,2]` | `ScriptedBattleStub(mode=[0,2])` | STUB | Shows visible placeholder and fade. |
| 76 | 47 | Branch `[78,78,0]` | `ScriptedBattleStub(result=0 -> branch 78)` | STUB | Correct branch for this data; not a generic branch VM. |
| 77 | 3 | Hide actor `50`, count/flag `1` | `hide(50)` | APPROX | Actor hidden; event-state save flag not modeled. |
| 78 | 4 | `??` dialog after battle | `dialog("??", ...)` | PORTED | Direct text. |
| 79 | 4 | Sophie dialog | `dialog("Sophie", ...)` | PORTED | Direct text. |
| 80 | 4 | Neil dialog | `dialog("Neil", ...)` | PORTED | Direct text. |
| 81 | 5 | Effect anim 6 on actor 49 | `spawnActorEffect(49,6)` | APPROX | Effect exists; exact offset/layer may differ. |
| 82 | 12 | Delay `15` | `Delay(15)` | PORTED | Direct. |
| 83 | 4 | Sophie dialog | `dialog("Sophie", ...)` | PORTED | Direct text. |
| 84 | 4 | `??` dialog | `dialog("??", ...)` | PORTED | Direct text. |
| 85 | 10 | Move 49,53,54,55,56 dir 1 speed 4 duration 15 | `TimedAction(...)` | APPROX | Movement simplified. |
| 86 | 10 | Move 48 dir 2 speed 4 duration 4 | `TimedAction(48,...)` | APPROX | Movement simplified. |
| 87 | 10 | Move 49,53,54,55,56 dir 2 speed 4 duration 23 | `TimedAction(...)` | APPROX | Movement simplified. |
| 88 | 3 | Hide actors 49,53,54,55,56 | `hide(49,53,54,55,56)` | APPROX | Actors hidden; event-state save flag not modeled. |
| 89 | 9 | Effect/fade type 2 | `effect.startFade(2,0)` | APPROX | Basic fade exists; original `b.a()` opcode 9 behavior not fully matched. |
| 90 | 34 | Timed text/effect counter `[1,0,70,0,0]` | `Opcode34Counter(70,0,0)` | PORTED | Source sets `N=70,O=0,B=0`, then completes immediately/near-immediately; no invented visual effect was added. |
| 91 | 48 | Special text box args `[10,20,220,200,40,1]` | `TextBox.box(20,220,200,40,...)` | APPROX | Position now follows source `x=20,y=220,w=200,h=40`; `game.j` mode/layout behavior is still not exact. |
| 92 | 12 | Delay `30` | `Delay(30)` | PORTED | Direct. |
| 93 | 1 | Full text `Dam xac xuoc nay!...` | `TextBox.full(60,90,...)` | APPROX | Text exists; first arg/type handling simplified. |
| 94 | 5 | Effect anim 1 on actor 48 | `spawnActorEffect(48,1)` | APPROX | Effect exists; exact offset/layer may differ. |
| 95 | 4 | Neil dialog | `dialog("Neil", ...)` | PORTED | Direct text. |
| 96 | 9 | Circle/special effect `[15,0,120,100,10,0]` | `effect.startCircle(0,0,120,100,10)` | APPROX | Visual intent present; opcode 9 type 15 is not pixel-perfect. |
| 97 | 4 | Neil dialog | `dialog("Neil", ...)` | PORTED | Direct text. |
| 98 | 5 | Effect anim 13 on actor 48 | `spawnActorEffect(48,13)` | APPROX | Effect exists; exact offset/layer may differ. |
| 99 | 12 | Delay `15` | `Delay(15)` | PORTED | Direct. |
| 100 | 4 | Neil dialog | `dialog("Neil", ...)` | PORTED | Direct text. |
| 101 | 22 | World transition prep `[1,2,199,218,240,320]` | `prepareTransition(199,218,240,320)` + room0 map id 2 load path | APPROX | Camera/viewport prep is ported; full `game.k.x/w/B/C` transition state is not modeled. |
| 102 | 6 | Transition target `[1,0,0,0]` | `markWorldTransition(1,0,-1)` + `loadScene1Room0(199,218)` | APPROX | Scene 1 room 0 map/actor table now loads and fades in; event completion/save-state and full state 22 loader are not modeled. |
| 103 | 14 | End event | End of manual list (`return e`) | APPROX | Event completion/save-state not modeled. |

## Current Totals

- `PORTED`: 40 records
- `APPROX`: 59 records
- `STUB`: 5 records
- `MISSING`: 0 records

## Priority Fixes From This Audit

1. Port the real `game.k` state 22 loader/free-movement handoff after room0 loads; current manual transition is visible and source-directed, but not the full world controller.
2. Fill sprite image mappings/resources for scene 1 room 0 actors that currently render blank instead of guessing their image tables.
3. Reduce `APPROX` in actor movement (`op10`) by modeling original `actor.h()`/collision/path completion instead of only speed-duration stepping.
4. Reduce `APPROX` in opcode `9` visual effects only after the event flow itself is complete.
5. Replace battle stub with real `game.d` when the next flow needs battle behavior instead of just continuing cutscene.
