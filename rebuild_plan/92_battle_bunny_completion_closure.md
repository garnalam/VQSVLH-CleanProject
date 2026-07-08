# 92 Battle Bunny Completion Closure

## Scope

Close the Bunny tutorial/catch slice enough to move on in the battle roadmap.

This does not claim full battle-engine completion. It classifies exactly what is
done for Bunny and what remains global/P17 pixel parity work.

## Source Files Rechecked

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
  - P17 entry/tick/render
  - tutorial `l()` and `m()`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
  - `ah()` P21 `/data/ui/choice.ui`
  - `ai()` confirm/missing-count/P17 transition
  - `c()` and `ay()` `/data/ui/taskTip.ui`
- `modules/source_code/decoded/decompiled_source_cfr/f.java`
  - sprite wrapper used by P17 `aj`
- `modules/source_code/decoded/decompiled_source_cfr/ah.java`
  - type 8 effect scale/offset behavior
- `modules/ui/decoded/data__ui__choice.ui.json`
- `modules/ui/decoded/data__ui__taskTip.ui.json`
- `modules/ui/decoded/data__ui__msgwarm.ui.json`
- `modules/ui/decoded/data__ui__openbox.ui.json`

## Source Bunny U/V Contract

Source `game.d.l()` / `game.d.m()` for `U == 0`:

| Source state | Behavior | Rebuild status |
|---|---|---|
| `U=0,V=0` | Wait until Bunny HP <= 50%, then `V++`, taskTip wounded/use Phong an cau | PORTED/PARTIAL |
| `U=0,V=1` | Wait for `taskTip.ui` close, then set guide flags and prompt button 5 | PORTED/PARTIAL |
| `U=0,V=3` | Prompt select Phong an cau, then `V++` | PORTED/PARTIAL |
| `U=0,V=4` | P21 confirm calls `m()`, `V=4->5` | PORTED |
| `U=0,V=5` | P17 entry force-fails catch after still consuming `ae.a(100)` | PORTED |
| after first fail | Bunny returns to normal turn flow and gets its counterattack before the retry prompt is shown | PORTED/PARTIAL, smoke-asserted |
| `U=0,V=6` | After retry taskTip closes, re-enter P21 | PORTED |
| `U=0,V=7` | Retry P21 confirm calls `m()`, `V=7->8` | PORTED |
| `U=0,V=8` | Cleanup `U=-1,V=0` | PORTED |

Code now exposes and smoke-asserts:

- `Scene.battleTutorialU`
- `Scene.battleTutorialV`

The route no longer depends only on opaque `bunnyTutorial...` booleans.

## P21 `choice.ui`

Source `game.h.ah()`:

- Opens `/data/ui/choice.ui`.
- Sets title id `8` to `"Pokemon ball"`.
- Sets header id `9` to `"Tỉ lệ bắt"`.
- Sets action id `5` to `"Sử dụng"`.
- Populates rows from `q.K`.
- Uses item icon from `aq.c[4][item][1]`.
- Uses item name from `aq.c[4][item][0]`.
- Uses chance text from `game.d.b(item) + "%"`.
- Uses count line id `53`: `"Số lượng: X cái "`.

Rebuild status:

| Concern | Status |
|---|---|
| Source-backed catch item ids `[0,1]` for Bunny reward route | VERIFIED |
| First tutorial cursor item id `1` Phong an cau | VERIFIED |
| Retry cursor item id `0` Tat Trung Cau | VERIFIED |
| Count line from selected bag item | PORTED/PARTIAL |
| Decoded `choice.ui` core coords/colors | PORTED/PARTIAL |
| Full generic `game.h/ao/af` UI runtime | PENDING, not required to close Bunny route |

Decoded coordinate check:

- frame/core visuals match current renderer anchors:
  - id `1`: `41,68,w=158`
  - id `2`: `44,78,w=151`
  - id `3`: `44,238,w=151`
  - id `4`: `44,70,w=151`
  - count id `53`: `57,180,w=125`
  - row icons ids `54..58`: `54,y=95/110/125/140/155`

## `taskTip.ui` / `msgwarm.ui`

Source:

- `game.h.c(text)` opens `/data/ui/taskTip.ui`.
- `game.h.ay()` returns true when `/data/ui/taskTip.ui` is closed.
- P21 missing ball opens `/data/ui/msgwarm.ui`.

Rebuild status:

| UI | Decoded coords | Rebuild status |
|---|---|---|
| `taskTip.ui` | frame `14,147,w=212`, text `16,154,w=208` | PORTED/PARTIAL |
| `msgwarm.ui` | frame ids `1/2/3/5/6/7/8` at decoded positions | PORTED/PARTIAL |
| `openbox.ui` | frame `45,147,w=150`, text `47,154,w=146` | PORTED/PARTIAL |

Full generic UI runtime remains PENDING, but Bunny-specific UI coordinates are
source-backed enough for the current route.

## P17 Catch

Source P17 entry:

- `this.aj = new f()`
- `this.aj.a(269, false)`
- `this.aj.b(this.h.i, this.h.j)`
- `this.aj.c()`
- `this.e((byte)0)`
- `this.ak = ae.a(100) < game.d.b(l)`
- if `U == 0 && V == 5`, force `ak=false`

Source P17 q flow:

```text
q0 -> q1 -> q2 -> q3 success
               -> q4 fail
```

Rebuild status:

| Concern | Status |
|---|---|
| Sprite id 269 | PORTED |
| q0..q4 phase flow | PORTED/PARTIAL |
| `ae.a(100) < chance` | PORTED |
| First Bunny forced fail at `U=0,V=5` | PORTED |
| Success storage bag/bank/full release | PORTED/PARTIAL |
| Type8-style enemy scale/offset effect | PORTED/PARTIAL |
| Pixel-perfect animation/UI | PENDING until original-vs-rebuild capture compare |

Important: do not claim pixel-perfect P17 yet.

## RNG Status

Current Bunny focused order:

```text
damage.crit -> damage.jitter -> P17.catch
```

After slice 91:

- P7 damage RNG uses source-shaped `ae.a(100)`.
- P17 catch uses the same `SourceBattleRuntime.SOURCE_RANDOM` stream.
- Trace labels are preserved.

Remaining:

- full boot/world/game-global `ae.f` parity is PENDING.
- exact source seed parity is PENDING.

These are not Bunny-route blockers anymore because the route-local battle stream
is now source-shaped and smoke-verified.

## Smoke Closure

New/strengthened assertions:

- first Bunny P17 force fail requires `U=0,V=5`;
- first Bunny P17 force fail now queues a normal enemy P7 counterattack before
  the retry taskTip;
- retry P21 item0 requires `U=0,V=7`;
- route completion requires cleanup `U=-1,V=0`;
- Bunny route still completes to task return with pet added.

Passed PNG checkpoints:

- `battle_bunny_first_catch_forced_fail`
- `battle_bunny_weak_prompt_tasktip`
- `battle_bunny_first_fail_enemy_counterattack`
- `battle_bunny_retry_p21_item0`
- `battle_bunny_after_catch_route`
- `route_bunny_after_battle_task`
- `battle_bunny_pre_p17_rng_trace`
- `battle_rng_trace_p17_catch`
- `battle_catch_fail_or_warning`
- `battle_catch_generic_roll_success`

## Final Bunny Classification

| Area | Status |
|---|---|
| Bunny tutorial route state `U=0` | PORTED/PARTIAL, smoke-asserted |
| P20/P21/P17 route order | PORTED/PARTIAL, smoke-asserted |
| First Phong an cau forced fail | PORTED |
| Retry Tat Trung Cau success path | PORTED/PARTIAL |
| Item count/consume/menu ids | PORTED/PARTIAL |
| Catch chance and RNG decision | PORTED/PARTIAL |
| Storage after catch | PORTED/PARTIAL |
| Bunny route completion to next task | VERIFIED |
| Full generic `game.h` UI runtime | PENDING |
| Full global boot/world RNG stream | PENDING |
| P17 pixel-perfect animation/UI | PENDING |

Decision:

The Bunny route can be treated as closed for the current battle-engine roadmap.
Future work should not reopen Bunny unless:

- an original-vs-rebuild capture proves a P17/UI mismatch;
- generic `game.h` runtime becomes the active roadmap target;
- global RNG parity becomes the active roadmap target.
