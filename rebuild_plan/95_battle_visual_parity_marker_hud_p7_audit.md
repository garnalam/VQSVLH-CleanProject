# 95 Battle Visual Parity: Marker, HUD, P7

## Trigger

User compared rebuild battle visuals with original capture and pointed out:

- HP/battle HUD is not original-like enough.
- Ground/platform under pets is missing.
- Green active-turn circle under the acting pet is missing.
- Skill use should animate the pet/skill into the enemy, then show damage/HP loss.

## Source Findings

### Ground / Active Marker

Source `game.d` constructs `al[]` marker sprites:

- every battle actor gets `new f()` with sprite `294`;
- actor marker uses state `0`;
- active marker uses `al[d.length]`, sprite `294`, state `1`;
- extra marker uses `al[d.length + 1]`, sprite `294`, state `2` for some battle modes.

Draw order:

```text
game.d.a(Graphics, boolean) draws al[] markers
game.d.a(Graphics) draws battle actors
```

Position data:

- `/data/script/pos.mid` -> `am`
- `/data/script/cpos.mid` -> `an`

Current group `0` positions:

| Source | Actor pos | Marker pos |
| --- | --- | --- |
| enemy | `(177,103)` | `(144,85)` |
| player | `(70,223)` | `(36,206)` |

### HUD / HP

Source uses `/data/ui/battle.ui` through `game.h`.
Current rebuild `VqsvBattleRenderer.renderSourceLikeBattleUi()` still has hardcoded HP/text drawing and broad colored bands.

Status: `PARTIAL`. Need a focused `game.h` + `battle.ui` widget mapping pass before claiming parity.

### Background Under Battle

Source `game.d.b(Graphics)` draws `this.c` if it exists, otherwise black. Current rebuild fills stylized horizontal color bands.

Status: `UNKNOWN/PENDING`. Need to trace where `this.c` is assigned for current story battles and whether original PC-like capture is world/map-backed or battle background-backed.

### P7 Skill Animation / Damage

Existing audits already classify P7 as `PORTED/PARTIAL`:

- `56_battle_p7_animation_effect_matrix.md`
- `67_battle_p7_actor_motion_camera_matrix.md`

Rebuild has:

- source-shaped actor motion/effect chunks;
- damage formula and HP mutation;
- damage text smoke.

Still partial:

- exact actor `u` object timing;
- target state `2` hit/recoil timing;
- `blood.mid` flyout parity;
- original-vs-rebuild frame compare.

## Code Slice Done

- `VqsvBattleAnimationTables` now loads `/data/script/pos.mid`.
- Scene exposes battle marker positions and active marker flags.
- Runtime syncs marker positions from `pos.mid` and active side from battle state/current actor.
- Renderer draws sprite `294`:
  - state `0` under both battle actors;
  - state `1` under the active actor.
- Follow-up fix: renderer no longer centers marker from fixed actor rectangles. It applies the source `pos.mid` marker-vs-actor delta to the current rebuild sprite anchor, so different pet sprite bounds keep the ground/platform under the pet feet.

## Status

| Area | Status | Note |
| --- | --- | --- |
| Ground/platform under actors | PORTED/PARTIAL | Uses source sprite `294` state `0` and source `pos.mid` marker positions. |
| Active green circle | PORTED/PARTIAL | Uses source sprite `294` state `1`; active-side lifecycle is source-shaped, not full `game.h/game.d` parity. |
| Extra marker state `2` | PENDING | Source has `al[d.length + 1]`; not rendered yet. |
| HP HUD | PARTIAL | Still hardcoded renderer, not full battle.ui widget runtime. |
| Battle background | UNKNOWN/PENDING | Need trace for `game.d.c` background image / world capture. |
| P7 hit/recoil/damage visual | PARTIAL | Damage exists; exact actor/recoil/blood timing not complete. |

## Verification

- `build.ps1`: pass.
- `--check`: pass.
- `VqsvBattleDamageFormulaCheck`: pass.
- `rg -n "Ã|Â|�" rebuild_game/src/main/java`: no Java source hits.
- `git diff --check`: pass.
- PNG smoke:
  - `battle_elder_command_ui_marker294`
  - `battle_bunny_retry_p21_item0_marker294`
  - `battle_elder_p7_damage_frame_marker294`

## Recommended Next Order

1. Audit/fix battle HUD HP from `game.h` + `/data/ui/battle.ui`.
2. Audit/fix battle background source `game.d.c`.
3. Tighten P7 visual timing: actor state `2` hit/recoil, `blood.mid` damage flyout, HP bar update frame.
