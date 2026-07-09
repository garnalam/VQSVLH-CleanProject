# 129 Battle P21/P17 Remaining Source Audit

Status date: 2026-07-08

Status: SOURCE-BACKED AUDIT ONLY / NO CODE CHANGE.

Purpose:

- Re-audit the remaining P21/P17 catch parity surface after the Phase 5 UI
  runtime slice in `128_new_dev_chat_handoff_battle_phase5_ui_runtime.md`.
- Focus only on source paths requested by the user:
  `game.h.ai()` catch list/warning, `game.d case 17`, `f/ah` catch
  sprite/effect, `game.g.y()` storage, and `openbox.ui` / `msgwarm.ui`.
- Do not touch intro/world/panel/scene scripts.
- Do not open the live client/JAR. Verification for later code slices must be
  PNG/headless only.

## Prior Docs To Treat As Current Context

| Doc | Meaning for this audit |
| --- | --- |
| `82_battle_p21_p17_catch_edge_case_matrix.md` | Older full matrix for P21/P17 state edges. Keep as source background. |
| `110_battle_p21_p17_catch_edge_closeout.md` | Closed the first catch edge slice: back, missing count, P101 policy, storage smokes. |
| `111_battle_p17_ui_widget_runtime_parity_matrix.md` | Audited P17 UI widgets/effects/openbox/msgwarm parity. |
| `123_battle_p21_p17_catch_edge_final_closeout.md` | Latest catch edge closeout before item/UI runtime work. Logic edge checkpoints passed at `PORTED/PARTIAL`. |
| `127_battle_choice_petstate_msgwarm_ui_runtime_closeout.md` | Latest UI runtime slice. `choice.ui`, `petstate.ui`, `msgwarm.ui` battle call sites are fuller, but still not full `game.h`. |
| `128_new_dev_chat_handoff_battle_phase5_ui_runtime.md` | Current controlling handoff. Phase 5 can exit with documented `PORTED/PARTIAL` gaps. |

This audit does not invalidate `123`. It narrows what is still worth touching if
P21/P17 is selected again.

## Source Chain Summary

### P21 Catch List: `game.h.ah()` / `game.h.ai()`

Source files:

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`

Source facts:

| Step | Source method | Source behavior | Current rebuild status |
| --- | --- | --- | --- |
| P21 enter | `game.d case 21` | Sets catch target then opens catch UI through `S.ah()`. | `PORTED/PARTIAL` in `VqsvBattleRuntime.prepareCatchMenu()`. |
| Open list | `game.h.ah()` | Loads `/data/ui/choice.ui` with sprite `257`; sets title `Pokemon ball`, subtitle catch-rate column, action use label. | `PORTED/PARTIAL`; renderer uses `choice.ui` widget map, not full `game.h`. |
| Populate rows | `game.h.ah()` | Iterates `q.K`, uses item icon from `aq.c[4][item][1]`, name from `[0]`, chance from `game.d.b(item)`, count text widget `53`. | `PORTED/PARTIAL`; rows come from `sourceBagItems` behavior `0`, chance via `catchChance()`. |
| Move cursor | `game.h.ai()` | Up/down calls UI list move and refreshes count with `bn()`. | `PORTED/PARTIAL`; `handleMenuInput()` and selected row values drive renderer. |
| Confirm with count | `game.h.ai()` | Checks `q.b(item,1,0)`, sets `game.d.l`, calls `o.m()`, consumes `q.d(item,1,0)`, enters P17, closes `choice.ui`. | `PORTED/PARTIAL`; `tickCatchList()` consumes once then calls `initCatchResult()`. Tutorial hooks are route-local. |
| Missing count | `game.h.ai()` | Opens `/data/ui/msgwarm.ui`, writes no-ball warning, sets local `f=1`, no consume. | `PORTED/PARTIAL`; `enterWarning(... P21)` returns to P21. |
| Missing item 0 | `game.h.ai()` | On confirm after warning, item id `0` closes `choice.ui` and enters state `101` purchase/SMS. | `REBUILD_POLICY`; source SMS/network is bypassed by free grant per user policy. |
| Back | `game.h.ai()` | Closes `choice.ui` and returns command state. | `PORTED`. |

Remaining risk:

- `choice.ui` is source-widget driven, but not a full `game.h/ab/af/al`
  runtime. Status stays `PORTED/PARTIAL`.
- P101 SMS is intentionally `REBUILD_POLICY`, not source parity.
- Bunny tutorial `U/V` is route-local approximation around P21/P17, not full
  global source tutorial state.

## P17 Catch Result: `game.d case 17`

Source files:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/f.java`
- `modules/source_code/decoded/decompiled_source_cfr/ah.java`

Source entry:

| Source step | Detail | Current rebuild status |
| --- | --- | --- |
| Target setup | `b3 = d[0]`, `h.p = b3`. | `PORTED/PARTIAL`; enemy battle unit is target. |
| Catch sprite | If `aj == null`, create `new f()`, load sprite `269`, place at `h.i/h.j`, call `aj.c()`. | `PORTED/PARTIAL`; `SpriteAnim.load(269)`, positioned in enemy battle rect. |
| Phase init | Calls `e((byte)0)`, so ball anim state q0 starts. | `PORTED/PARTIAL`; `catchPhase = 0`, `setCatchAnimState(0, false)`. |
| Chance | `n4 = b(l)`, `ak = ae.a(100) < n4`. | `PORTED/PARTIAL`; `catchChance()` + `VqsvSourceRandom` trace. Full global RNG parity is still partial. |
| Bunny forced fail | If tutorial `U == 0 && V == 5`, force `ak = false`. | `PORTED/PARTIAL`; route-local Bunny first-catch force fail. |
| UI flag | `S.f = 0`. | `PORTED/PARTIAL`; rebuild uses `catchOpenBoxState` and warning/openbox states. |

Source update chain:

| Source q | Source behavior | Current rebuild status |
| --- | --- | --- |
| q0 | When `aj.b()` ends, call `e(1)`. | `PORTED/PARTIAL`. |
| q1 | Enemy is hidden; starts `H = new ah()` type 8 with target sprite row and scale/offset steps; ball anim state q1. Waits for `H.e()`. | `PORTED/PARTIAL`; visible source-shaped effect exists, exact `ah/l/drawRGB` parity pending. |
| q2 | On `aj.b()`, branch to q3 if `ak`, q4 if fail. | `PORTED/PARTIAL`. |
| q3 | On success, call `game.g.y()` and open catch success/full messages. | `PORTED` for current storage branches; payload is `PORTED/PARTIAL`. |
| q4 | On fail and `H.e()` done: clear `H`, show enemy, disable `aj`, set `h.J = true`, then either full-storage message or return to P1. | `PORTED/PARTIAL`; fail restore smoke exists. |

Draw order:

| Source draw | Current rebuild status |
| --- | --- |
| P17 draws battle scene, actors, then if `H != null && H.c()` draw `H` and then `aj`; otherwise draw `aj`. | `PORTED/PARTIAL`; smoke verifies H-before-ball order, but no original MIDP pixel compare. |

Remaining risk:

- The q0..q4 state shape is present, but exact frame cursor lengths, sprite
  state `f/n/d`, `ah.e()` timing, and Java ME `drawRGB` pixels are still
  `PENDING` for pixel-perfect parity.
- Rebuild renderer approximates `ah` type 8 with scaled/brightened enemy sprite.
  It is source-shaped, not exact `l.a/l.b/e` bitmap pipeline parity.

## `f` Catch Sprite Wrapper

Source file:

- `modules/source_code/decoded/decompiled_source_cfr/f.java`

Source facts:

| Source method | Meaning for P17 | Current rebuild status |
| --- | --- | --- |
| `a(int, boolean)` | Loads sprite resource through internal `d`. P17 uses sprite `269`. | `PORTED/PARTIAL` via `SpriteAnim.load(269)`. |
| `a(byte, byte, boolean)` | Sets animation state and mode. P17 uses states q0..q4. | `PORTED/PARTIAL`; state cursor is mirrored enough for smoke. |
| `b()` | Returns animation ended (`a.e()`). P17 phase transitions depend on this. | `PORTED/PARTIAL`; `catchAnimAtLastFrame()` approximates end check. |
| `a(Graphics, int, int)` | Draws sprite with orientation/mode. | `PORTED/PARTIAL`; renderer draws aligned sprite in enemy rect. |
| `c()` / `d()` | Show/hide catch sprite. | `PORTED/PARTIAL`. |

Remaining risk:

- Exact internal `d` animation semantics, hold-last behavior, and orientation
  mode are not proven pixel-perfect.

## `ah` Catch Effect

Source file:

- `modules/source_code/decoded/decompiled_source_cfr/ah.java`

Source facts:

| Source area | Meaning for P17 | Current rebuild status |
| --- | --- | --- |
| `ah.a(short[])` type 8 | Used by P17 q1: target sprite is copied and scaled through `l.a`, using steps `[10,0,0]`, `[7,0,-10]`, `[4,0,-20]`. | `PORTED/PARTIAL`; rebuild uses scale10/dx/dy steps and brightened enemy sprite. |
| `ah.a(short[])` type 8 in q4 path | Used by P17 fail escape: steps `[4,0,-20]`, `[6,0,-12]`, `[8,0,-4]`, `[10,0,0]`. | `PORTED/PARTIAL`. |
| `ah.e()` | Advances effect timing and returns finished state. | `PORTED/PARTIAL`; rebuild has source-like tick counters. |
| `ah.c()` | P17 draw gate: only draw H effect when source says visible/current type. | `PORTED/PARTIAL`. |
| `ah.a(Graphics)` | Uses `drawRGB` from precomputed `e` bitmap cells. | `PENDING` for exact MIDP pixel parity. |

Remaining risk:

- Current effect is good enough for source-shaped visual smoke, but exact
  `drawRGB` transparency key, bitmap generation, and timing are not
  pixel-compared against original client.

## Storage Result: `game.g.y()`

Source file:

- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`

Source facts:

| `game.g.y()` result | Source meaning | P17 source side effect | Current rebuild status |
| --- | --- | --- | --- |
| `0` | Party/bag has fewer than 6 pets. | `S.f = 1`; open success openbox; `game.d.p.a(caught.P())`. | `PORTED`; adds to `sourcePets`; payload `PORTED/PARTIAL`. |
| `1` | Party full, bank has fewer than 100 pets. | `S.f = 2`; open first success openbox; `game.d.p.b(caught.P())`; after close opens second bank notice. | `PORTED`; bank branch and second openbox smoke-covered. |
| `2` | Party and bank full. | `S.f = 1`; open full/released message; no add. | `PORTED`; full-release smoke-covered. |

Remaining risk:

- `game.b.P()` full payload parity remains `PORTED/PARTIAL`, especially for
  fields not consumed by current battle/petstate/save paths.
- Save/global persistence of caught pets is outside this audit and remains
  broader save-runtime work.

## `openbox.ui` and `msgwarm.ui`

Source files/resources:

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/ui/original/openbox.ui`
- `modules/ui/decoded/data__ui__openbox.ui.json`
- `modules/ui/original/msgwarm.ui`
- `modules/ui/decoded/data__ui__msgwarm.ui.json`

Source facts:

| UI | Source caller | Source behavior | Current rebuild status |
| --- | --- | --- | --- |
| `msgwarm.ui` | `game.h.ai()` missing count | `p.a("/data/ui/msgwarm.ui",257,this)`, writes title/prompt through widgets `7/6`. | `PORTED/PARTIAL`; `TextBox.msgWarm` uses `VqsvUiLayout.load("msgwarm.ui")` and source widget positions. |
| `openbox.ui` | `game.h.b(String)` | Loads `/data/ui/openbox.ui`, sprite `257`, then writes text through source helper. | `PORTED/PARTIAL`; `TextBox.openBox` uses decoded source rects and sprite state. |
| `openbox.ui` close | `game.h.ax()` | P17 waits until UI is closed before continuing branch. | `PORTED/PARTIAL`; `tickCatchOpenBox()` waits for key/close state. |

Decoded widget anchors:

| UI file | Important widgets |
| --- | --- |
| `openbox.ui` | visual `1` frame at `45,147,w=150`, visual `2` text at `47,154,w=146`. |
| `msgwarm.ui` | fills `1/2/3/5`, prompt widget `6`, message widget `7`, frame widget `8` with sprite cell `128`. |
| `choice.ui` | title `8`, subtitle `9`, action/back `5/6`, rows `13/14...`, icon widgets `54..58`, count/desc `53`. |

Remaining risk:

- These are battle call-site renderers, not a complete source `game.h` UI VM.
- Exact text scroll timing, sprite state, Java ME font metrics, and all widget
  modes remain `PORTED/PARTIAL` or `PENDING` for pixel-perfect compare.

## Current Rebuild Evidence

Rebuild files:

- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleRenderer.java`
- `rebuild_game/src/main/java/VqsvTextRenderer.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Existing smoke coverage from prior closeouts:

| Checkpoint | Coverage |
| --- | --- |
| `battle_catch_missing_count_warning` | P21 non-item0 missing count opens `msgwarm.ui`. |
| `battle_catch_missing_count_warning_return_p21` | Warning confirm returns to P21. |
| `battle_catch_p21_back_to_command` | Back from P21 returns command state. |
| `battle_catch_sms_free_item0_p17` | Item0 P101 SMS is rebuild free-grant policy. |
| `battle_catch_storage_bag` | `game.g.y()==0` party storage. |
| `battle_catch_storage_bank` | `game.g.y()==1` bank storage and second openbox. |
| `battle_catch_storage_full_release` | `game.g.y()==2` release/no-add path. |
| `battle_p17_q1_h_effect_order` | q1 hides target and draws H/ball order. |
| `battle_p17_q4_fail_restore_enemy` | q4 fail clears visuals and restores target. |
| `battle_bunny_first_catch_forced_fail` | Bunny first Phong An Cau forced fail. |
| `battle_bunny_first_catch_fail_escape_effect` | Bunny fail escape effect smoke. |
| `battle_bunny_first_catch_q2_rumble` | Bunny q2 rumble checkpoint. |
| `battle_catch_success_q3_flash_mid` | Success flash/mid checkpoint. |
| `battle_openbox_source_widget_catch_success` | Catch success uses `openbox.ui` shaped TextBox. |
| `battle_msgwarm_source_widget_warning` | Battle warning uses `msgwarm.ui` shaped TextBox. |

## Remaining Gap Matrix

| Area | Status | Why it remains |
| --- | --- | --- |
| P21 list/count/warning/back | `PORTED/PARTIAL` | Logic is covered; full `choice.ui` VM and P101 SMS UI are not. |
| P101 SMS purchase | `REBUILD_POLICY` | User policy: PC client does not need SMS; item0 free grant is intentional. |
| P17 q0..q4 state chain | `PORTED/PARTIAL` | State shape covered; exact frame timing/pixel parity not proven. |
| P17 RNG | `PORTED/PARTIAL` | Catch roll traces `ae.a(100)` equivalent, but whole-game RNG stream parity is broader. |
| `f` sprite 269 | `PORTED/PARTIAL` | Sprite state is used; exact wrapper/mode semantics need original compare. |
| `ah` type 8 catch effect | `PORTED/PARTIAL` | Source-shaped scaling/offset exists; exact `l/e/drawRGB` bitmap pipeline pending. |
| Storage `game.g.y()` | `PORTED` for current routes | Payload/save-wide parity remains `PORTED/PARTIAL`. |
| `openbox.ui` | `PORTED/PARTIAL` | Source rect/sprite/text ready behavior exists; full UI runtime/pixel parity pending. |
| `msgwarm.ui` | `PORTED/PARTIAL` | Source widget map is used; full UI runtime/pixel parity pending. |
| Bunny tutorial U/V | `PORTED/PARTIAL` | Current route works; full global tutorial state is not ported. |

## Recommended Next Slice

If the user wants to continue P21/P17 now, choose one small slice only:

1. P17 animation parity checkpoint tightening.
   - Audit exact sprite 269 state frame counts and `f.b()` end timing.
   - Compare current q0/q1/q2/q3/q4 frame cursor progression in smoke trace.
   - Do not claim pixel-perfect without original frame/pixel capture.

2. `ah` catch effect parity tightening.
   - Audit type 8 bitmap path through `ah.a(short[])`, `l.a`, `l.b`, `e`.
   - Add trace/smoke for q1 and q4 scale/offset steps.
   - Keep renderer source-shaped if full `drawRGB` cannot be proven.

3. `openbox.ui` / `msgwarm.ui` close/readiness parity.
   - Audit `game.h.e(String)`, `game.h.f()`, `game.h.ax()` text readiness
     and close timing.
   - Add PNG assertions for openbox/message frame and text readiness.

Do not redo storage/back/no-ball unless an existing smoke fails. Those are
already closed at `PORTED/PARTIAL` by `123`.

## PNG-Only Smoke Plan For A Future Code Slice

Focused catch smokes:

```powershell
java -cp .\build\classes VqsvIntroDemo --smoke-checkpoint battle_bunny_first_catch_fail_escape_effect build_intro_demo\p17_fail_escape.png
java -cp .\build\classes VqsvIntroDemo --smoke-checkpoint battle_bunny_first_catch_q2_rumble build_intro_demo\p17_q2_rumble.png
java -cp .\build\classes VqsvIntroDemo --smoke-checkpoint battle_catch_success_q3_flash_mid build_intro_demo\p17_q3_flash.png
java -cp .\build\classes VqsvIntroDemo --smoke-checkpoint battle_openbox_source_widget_catch_success build_intro_demo\p17_openbox.png
java -cp .\build\classes VqsvIntroDemo --smoke-checkpoint battle_msgwarm_source_widget_warning build_intro_demo\p21_msgwarm.png
```

Route regressions if runtime/branch/storage changes:

```powershell
java -cp .\build\classes VqsvIntroDemo --smoke-checkpoint route_sophie_after_battle_branch build_intro_demo\route_sophie.png
java -cp .\build\classes VqsvIntroDemo --smoke-checkpoint route_bunny_after_battle_task build_intro_demo\route_bunny.png
java -cp .\build\classes VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state build_intro_demo\route_elder.png
```

Always also run build/check/mojibake and `VqsvBattleDamageFormulaCheck` if
battle runtime/formula files are touched.

## Safety

May edit for a later code slice:

- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleRenderer.java`
- `rebuild_game/src/main/java/VqsvTextRenderer.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`
- A new closeout/audit doc under `rebuild_plan/`

Must not touch for this catch slice unless source proves a direct dependency:

- Intro/world/panel/scene scripts.
- Save runtime.
- P4/P16/P5 item/pet switch logic.
- Live client/JAR.

## Audit Conclusion

P21/P17 logic edge behavior is already closed enough for Phase 5 current routes
at `PORTED/PARTIAL` by `123`. The remaining source-backed work is not broad
catch logic; it is specifically visual/timing/UI parity around:

- sprite `269` q0..q4 timing,
- `ah` type 8 effect bitmap/timing,
- `openbox.ui` / `msgwarm.ui` readiness and exact rendering,
- full original-vs-rebuild pixel comparison.

Next action should be a tiny visual/timing parity slice, not another storage or
no-ball rewrite, unless a smoke regression proves those paths broke.
