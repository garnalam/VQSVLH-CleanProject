# Battle Engine Three Stub Replacement Audit

Date: 2026-07-03

Scope: replace visible battle smoke/stub for the three current story battles in
`rebuild_game/src/main/java/VqsvIntroDemo.java`.

This is not a claim that full original `game.d/game.h` battle has been ported.
The old `ScriptedBattleStub` class has been replaced with a source-backed
runtime slice that uses original encounter records, `game.b` stat formulas, and
`op47` branch results.

## Source Facts Used

- `game.c case 37`: `game.d.a().a(new int[][]{{species, level, nature}})`.
- `game.c case 32`: sets `game.d.a().a`, `game.d.a().b`, captures world screen,
  and switches `game.i` to state 12.
- `game.c case 47`: if `game.c.l != -1`, jumps by battle result index.
- `game.c case 52`: sets capture/tutorial flags.
- `game.d.d()`: initializes player/enemy units and loads `pos.mid`,
  `cpos.mid`, `effect.mid`, `speffect.mid`, `bufDebuf.mid`, `blood_*.png`.
- `game.d.m(int)`: creates enemy `game.b` from encounter table.
- `game.b.a(species, level, hp, side, nature, extra)`: computes stats from
  `aq.c[0]` with nature multiplier `{90,95,100,110,125}`.
- `game.d.l()`: Bunny capture tutorial prompts after Bunny HP reaches <=50%.

## Battle Matrix

| Story point | Source records | Runtime result |
|---|---|---|
| Sophie kidnapping fight | room3 group0: `op67 [56]`, `op37 [5,20,4]`, `op52 [1,1]`, `op32 [0,2]`, `op47 [78,78,0]` | PORTED/APPROX: species 5 level 20 enemy uses `aq.c[0]` stats; Neil loses; result `0 -> branch 78`. |
| Bunny capture | room1 group0: `op37 [34,5,1]`, `op52 [0,1]`, `op32 [0,0]`, `op47 [12,0,0]` | PORTED/APPROX: species 34 Bunny uses `aq.c[0]`; HP is reduced to <=50%; capture tutorial/capture resolves; result `-1`, manual script continues success path. |
| Elder battle | room0 group6: `op67 [52]`, `op37 [68,5,1]`, `op32 [0,2]`, `op47 [10,10,0]` | PORTED/APPROX: selected pet from group3 fights species 68 level 5 using source stat table; result `0 -> branch 10`. |

## Code Changes

- Replaced `ScriptedBattleStub` with `SourceBattleRuntime`.
- Added `SourceBattleDb` loader:
  - reads `/data/script/db.mid` from `modules/script/original/db.mid`;
  - uses group 0 as `aq.c[0]`;
  - reads `data__script__chs.mid.json` for display names using row `[0]`.
- `SourceBattleUnit` now stores source-backed:
  - species id, level, nature;
  - HP, attack, defense, speed;
  - element and visual id used by the original relation check.
- Added battle smoke checkpoints:
  - `battle_kidnapping`
  - `battle_bunny_capture`
  - `battle_elder`
  - `battle_kidnapping_result`
  - `battle_bunny_capture_result`
  - `battle_elder_result`
- Added state/event checkpoints that do not depend on old fixed-tick route
  timing:
  - `route_sophie_after_battle_branch`
  - `route_bunny_after_battle_task`
  - `route_elder_after_battle_reward_state`
- Replaced the debug battle overlay with a fuller source-like battle HUD:
  - uses `battle.ui` coordinates for enemy/player name, level, HP text, HP bar,
    command area, and log area;
  - draws source sprite-257 UI cells used by `battle.ui`: top enemy panel,
    bottom command panel, middle relation arrow panel, element icons, and command
    icon strip;
  - uses `aq.c[0][species][17]` as the visual/sprite id for enemy/player
    battle sprites;
  - renders all six source command slots: battle, capture, item, pet, shop, run;
  - renders element icons, relation power percent widgets `58/59`, and empty
    buff/debuff slot positions `26..37` / `43..54`.

## Smoke Results

Build/check:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
java "-Dvqsv.modules=..\modules" -jar ".\build\libs\vqsv-rebuild-skeleton.jar" --check
```

Result: PASS.

Visual checkpoints:

- `rebuild_game/build_intro_demo/battle_kidnapping_fuller_ui.png`
- `rebuild_game/build_intro_demo/battle_bunny_fuller_ui.png`
- `rebuild_game/build_intro_demo/battle_elder_fuller_ui.png`

Result checkpoints:

- kidnapping: `battleResult=0`, `battleBranch=78`, HP `0/120 : 473/473`.
- Bunny: `battleResult=-1`, `battleBranch=-1`, HP `120/120 : 0/104`.
- elder: `battleResult=0`, `battleBranch=10`, HP `102/134 : 0/109`.

State/event checkpoints:

- Sophie after battle branch:
  - checkpoint: `route_sophie_after_battle_branch`
  - proves `battleResult=0`, `battleBranch=78`
  - then shows the post-branch dialog after the battle record.
- Bunny after battle task:
  - checkpoint: `route_bunny_after_battle_task`
  - proves `battleResult=-1`, `battleBranch=-1`
  - applies source side effects `op23 [1,0,1]` and `op14 [1,1,0]`
  - observed `state101=3`, `state110=3`
  - then shows task text `Trở về tìm trưởng thôn!`.
- Elder after battle reward/state:
  - checkpoint: `route_elder_after_battle_reward_state`
  - proves `battleResult=0`, `battleBranch=10`
  - applies reward slice after branch: money `+500`, items `4 x10`,
    `11 x2`, special reward `5`, `op23 [1,0,4]`, `op23 [1,0,5]`,
    `op14 [1,0,6]`
  - observed `state106=3`, `money=500`, `pets=1`.

Note: Windows console output still mojibakes Vietnamese text in some smoke lines;
the images render through the current Java2D text path and are the visual proof.

## Current Truth

Status: `PORTED/APPROX`, not full `game.d`.

No longer true:

- The three story battles are no longer plain smoke/stub overlays.
- Enemy names and stats are no longer hardcoded per battle.
- `op47` branch result is now produced by battle runtime state.

Still not complete:

- Full `game.d` state machine is PENDING.
- Full `game.h` command UI/menu is PENDING.
- Skill table `aq.c[1]`, buffs/debuffs, status, cooldowns, EXP, rewards, and
  animation/effect scripts are PENDING.
- Capture probability/item inventory semantics are APPROX; Bunny tutorial path
  follows source shape but not full command menu.
- Battle visual layout is improved to PORTED/APPROX. It now follows audited
  `battle.ui` coordinates, source sprite-257 UI cells, source sprite ids,
  element icons, command slots, relation percent, and empty status-slot
  positions. It is still not pixel-perfect because the full `ao/al` UI renderer,
  MIDP font width, exact text clipping/wrapping, battle animation positions from
  `pos.mid/cpos.mid`, and `blood.mid` flyout rendering are still pending.
- Old fixed-tick full-route smoke remains unreliable after text/battle timing
  changes; use source-state checkpoints or recalibrate route timing before
  treating a route as gameplay proof.
