# 213 Battle NPC Enemy VS Entry UI Closeout

Date: 2026-07-10

## Source Audit

- `modules/source_code/decoded/decompiled_source_cfr/b.java`
  - Case `8` draws the captured battle/world snapshot, runs UI animation, then reloads `/data/ui/npcEnemy.ui`.
  - Init path calls `game.h.a().at()`.
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
  - `at()` loads `/data/ui/npcEnemy.ui` with sprite base `296`, initializes widget `1`, and hides widget `36`.
  - `b(int step, int frame)` mutates widgets `1..36`, including the two sides and the 6 slot/count rows.
- `modules/ui/decoded/data__ui__npcEnemy.ui.json`
  - Widget `1`: main VS panel band.
  - Widgets `6..17`: top/right side slot row.
  - Widgets `18..29`: bottom/left side slot row.
  - Widgets `30..33`: text/label areas.
  - Widgets `34/35`: side portrait/image areas.

## Rebuild Slice

- `PORTED/PARTIAL`: Elder/sourceBattleSlice battles now enter a new `NPCVS` runtime state before `P0`.
- `PORTED`: renderer uses source layout `npcEnemy.ui` and sprite base `296` for the panel and slot icons.
- `PORTED`: player/enemy counts come from source-shaped battle state (`sourcePets.size()`, `enemyParty.length`).
- `PORTED/PARTIAL`: side portraits prefer source world actor sprites (`Scene.player`, actor 52 Elder) and fall back to battle pet sprites when no world actor exists.
- `PENDING`: full top-level `b.java` entry animation timeline and original-vs-rebuild pixel compare.

## Smoke

- Added `battle_entry_vs_elder_ui`.
- Regression pass:
  - `battle_entry_enemy_cpos`
  - `battle_entry_player_cpos`
  - `battle_entry_both_landed`
  - `route_sophie_after_battle_branch`
  - `route_bunny_after_battle_task`
  - `route_elder_after_battle_reward_state`

## Next

Audit/polish the remaining `npcEnemy.ui` timeline from source `b.java`/`game.h.b(step,frame)`:

1. Prove exact step order and duration from original entry controller.
2. Map sprite 296 cells `0/4/5/6/7/8` to the visible frame states.
3. Decide whether Sophie NPC battle should also use this pre-entry path.
4. Only then tighten pixel placement/animation beyond the current `PORTED/PARTIAL` render.
