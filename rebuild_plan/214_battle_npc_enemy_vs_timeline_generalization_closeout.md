# 214 - battle npcEnemy.ui timeline/generalization closeout

## Scope
- Request: audit/polish exact `npcEnemy.ui` pre-battle timeline and apply it to all battle-with-enemy cases.
- Source anchor:
  - `modules/source_code/decoded/decompiled_source_cfr/b.java` state/case `8`.
  - `modules/source_code/decoded/decompiled_source_cfr/game/h.java` `at()` and `b(int step, int frame)`.

## Source facts
- `b.case 8` initializes:
  - `i = 0`
  - `game.h.at()`
  - `a(0)` where `Z = A[i].length`
- Update calls:
  - `game.h.b(i, A[i][aa])`
  - then advances `aa`, `i`.
- Source timeline:
  - `{0}, {1}, {2}, {3}, {4,5,6,7,8}, {9}, {10}, {11,12}, {13}, {14,15,16,17,18,19,20}, {21}, {22}`
- `game.h.at()` loads `/data/ui/npcEnemy.ui` using sprite base `296`, initializes widget `1`, and hides widget `36`.
- `game.h.b(step, frame)` mutates widget `1`, portrait widgets `2/3/34/35/4/5`, party slot widgets `6..29`, label widgets `30..33`, and overlay widget `36`.

## Rebuild changes
- `SourceBattleRuntime` now has a separate `npcEnemyEntry` flag instead of coupling this UI only to `sourceBattleSlice`.
- `VqsvBattleEventDescriptor` marks:
  - `SCENE1_ROOM0_GROUP6_ELDER`: `npcEnemyEntry=true`
  - `SCENE1_ROOM3_GROUP0_SOPHIE`: `npcEnemyEntry=true`
  - `SCENE1_ROOM1_GROUP0_BUNNY`: `npcEnemyEntry=false`
- `VqsvIntroDemo.Scene` tracks `battleNpcEnemyEntryStep` and `battleNpcEnemyEntryFrame`.
- `VqsvBattleRenderer` replays the source timeline and renders source-backed `npcEnemy.ui` widget visibility/cell state:
  - sprite 296 cell `0/4/5/6/7/8`
  - enemy/player counts
  - labels/VS widgets
  - widget 36 overlay approximation

## Status
- `npcEnemy.ui` timeline: PORTED/PARTIAL.
- `game.h.b(step, frame)` widget sequencing: PORTED/PARTIAL.
- Elder/Sophie NPC battle gate: PORTED.
- Bunny wild/tutorial battle not using `npcEnemy.ui`: PORTED for current source-backed descriptor.
- Actor portrait mapping: PORTED/PARTIAL because source uses global `game.k.u/v` actor table and rebuild maps to current world actor sprite ids.
- Pixel-perfect claim: PENDING. No original-vs-rebuild frame compare was performed.

## Required smoke
- `battle_entry_vs_elder_ui`
- `battle_entry_vs_sophie_ui`
- `battle_entry_vs_bunny_no_npc_ui`
- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`

## Next
- Next battle integration slice should audit result/KO all-party-faint behavior from source before patching generic battle end conditions.
- Do not return to P7 visuals unless there is a route mismatch or original-client frame capture.
