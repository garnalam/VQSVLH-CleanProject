# Scene 1 Post Group6 Flow Audit

Scope: what can happen after `scene_1 room0 group6` completes with
`state[1,0,6]=3`.

This audit is source-reading only. No runtime code was changed.

## Source Files Read

- `modules/event/decoded/data__event__scene_1.mid.json`
- `modules/source_code/decoded/decompiled_source_cfr/game/c.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/a.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`

## Key Source Rules

- `op15 [scene,room,group]` passes only when event state is `3` or `4`.
- `op42` sets the current event group state to `4`.
- `op14` completes the current group, state `3`.
- `op23 [scene,room,group]` directly marks another event group complete, state `3`.
- `op13 [x,y,w,h]` is a free-world zone trigger.
- Type-1 actor records are map/zone transitions:
  `[kind,sprite,state,x,y,visible,v,C,targetScene,targetRoom,targetActor]`.

## Direct Result Of Group6

`room0 group6` ends with:

- `op23 [1,0,4]`
- `op23 [1,0,5]`
- `op45 [2]` task text: go to Bich Thuy Thanh
- `op40` free-world notice
- `op14`, so `state[1,0,6]=3`

There is no remaining `scene_1 room0` event group gated directly by
`op15 [1,0,6]`.

## Group4 And Group5 Meaning

Group4:

- Gate: `op15 [1,0,1]`
- Zone: `op13 [160,288,80,32]`
- Text: "Choose one pet to battle the elder"
- Ends with `op42`, not `op14`

Group5:

- Gate: `op15 [1,0,2]`
- Zone: `op13 [385,240,32,48]`
- Text: "Choose one pet to battle the elder"
- Ends with `op42`, not `op14`

Conclusion: group4/group5 are repeatable blocker/reminder zones while the
player has not yet completed the pet/battle tutorial. Group6 marks both as
complete with `op23`, so after group6 they should no longer block exits.

## Available Transitions After Group6

Relevant room0 type-1 actors:

| Actor | Source row | Meaning |
|---:|---|---|
| 30 | `[1,223,3,408,273,1,1,3,1,1,37]` | Transition to `scene_1 room1`, target actor 37. This is the east/Bunny side path. |
| 31 | `[1,223,1,200,318,1,1,2,1,2,2]` | Transition to `scene_1 room2`, target actor 2. This is the south path toward the next route/Bich Thuy direction. |
| 3 | `[1,213,0,192,81,1,0,0,11,5,0]` | Building/door transition to `scene_11 room5`, target actor 0. |
| 4 | `[1,213,0,45,220,1,0,0,11,6,0]` | Building/door transition to `scene_11 room6`, target actor 0. |
| 5 | `[1,213,0,346,145,1,0,0,11,4,0]` | Building/door transition to `scene_11 room4`, target actor 0. |

Most likely main progression immediately after tutorial is actor 31:
`scene_1 room0 -> scene_1 room2`, because group6 task text says go to
Bich Thuy Thanh and room2 is named "Theo chi dan dia do tiep theo la Thuy Moc
Thon" in the decoded data.

## Side Quest Groups Open In Room0

Group7:

- Starts with `op43 [0,1,1,0,35,0,0,-1,-1,0,0]`
- Actor: 35 (`Dodo`)
- No prior event-state requirement because arg7 is `-1`.
- Offers side quest via `op49`; accept path completes group7, refuse path ends with `op42`.

Group8:

- Starts with `op44 [0,1,1,0,35,1,0,7,0,1,23,0]`
- Actor: 35 (`Dodo`)
- Requires `state[1,0,7]=3` before completion branch can run.
- Appears to be the turn-in for Dodo's first side quest.

Group9:

- Starts with `op43 [1,1,1,0,35,1,0,8,1,0,0]`
- Actor: 35 (`Dodo`)
- Requires `state[1,0,8]=3`.
- Starts Dodo's next side quest.

These groups are optional side-quest interactions, not automatic continuation
after group6.

## Not Opened By Group6

Group10:

- Gate: `op15 [1,3,6]`
- Actor 52 elder interaction.
- This belongs to a later story return after `scene_1 room3 group6`, not the
immediate post-tutorial flow.

Group11:

- Gate: `op15 [7,2,5]`
- Later story flag from scene 7, not immediate post-tutorial flow.

## Chot Flow

After "Gio co the tu do di chuyen":

1. Player is in free-world in room0.
2. Group4/group5 blocker zones must be disabled because group6 completed them
   via `op23`.
3. Player can freely use transitions:
   - east back toward room1,
   - south actor31 toward room2/main route,
   - building doors to scene11 rooms.
4. Player can optionally interact with Dodo actor35 for side quests.
5. No source-backed auto event should start solely from `state[1,0,6]=3`.

## Recommended Next Port Slice

Port room0 post-group6 free-world transitions, starting with actor31:

`room0 actor31 -> scene_1 room2 target actor2`

Then audit `scene_1 room2` group/transition flow before implementing more.
