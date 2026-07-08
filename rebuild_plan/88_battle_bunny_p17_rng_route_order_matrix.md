# 88 Battle Bunny P17 RNG Route Order Matrix

## Scope

Route-specific audit for Bunny tutorial from battle entry / weak Bunny prompt to
P21/P17 catch.

Goal:
- Prove the source sequence around `U/V`, P20, P21, and P17.
- Identify which steps consume source RNG `ae.f`.
- Compare current rebuild behavior and trace coverage.

This is audit-only. No code changes in this step.

## Source Files Read

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvText.java`
- Related audits: `82_battle_p21_p17_catch_edge_case_matrix.md`,
  `86_battle_p17_pixel_and_rng_parity_audit.md`,
  `87_battle_rng_trace_harness_matrix.md`.

## Source Route Summary

The relevant source path is:

```text
Battle action lowers Bunny HP
-> game.d.l() sees U=0,V=0 and enemy HP <= 50%
-> tutorial prompts player to use Phong an cau
-> P20 command Catch
-> P21 choice.ui ball list via game.h.ah()
-> P21 confirm via game.h.ai()
   -> game.d.l = selected item id
   -> game.d.m() advances tutorial V at V=2/4/7
   -> consumes one ball
   -> enters P17
-> P17 entry creates sprite 269, computes chance, rolls ae.a(100)
   -> if U==0 && V==5, force catch fail
-> P17 q0..q4 animation/result
-> after first forced fail, game.d.l() at U=0,V=5 shows retry prompt
-> game.d.l() at U=0,V=6 returns to P21
-> second P21 confirm Tat Trung Cau advances V to 8
-> second P17 uses item0 chance 100
-> tutorial cleanup at U=0,V=8
```

## Source Evidence

### P20 Command Entry

`game.d.a(byte)` case `20`:
- selects active unit `this.h = (b)this.v.elementAt(this.i)`;
- refreshes battle UI around player/enemy;
- no `ae.a`/`ae.b` call in this case body.

Status: PROVED, no direct RNG in P20 entry.

### P21 Entry And UI Build

`game.d.a(byte)` case `21`:
- `b b4 = this.d[0]`;
- `this.h.p = b4`;
- `this.S.ah()`;
- no direct RNG in the case body.

`game.h.ah()`:
- opens `/data/ui/choice.ui`;
- sets title `"Pokemon ball"`, header catch-rate text, action use text;
- fills rows from `this.q.K`;
- icon from `aq.c[4][item][1]`;
- name from `aq.c[4][item][0]`;
- chance text from `((d)this.o).b(item) + "%"`;
- count text via `bn()`.

No `ae.a`/`ae.b` call appears inside `game.h.ah()` itself. The chance text calls
`game.d.b(item)`, whose catch formula is deterministic for current mapped
inputs and does not call `ae.f`.

Status: PROVED, no source RNG in P21 list build.

### P21 Confirm

`game.h.ai()`:
- handles up/down cursor movement and count text refresh;
- on confirm, checks `this.q.b(v1[0], 1, (byte)0)`;
- if count is missing, opens `/data/ui/msgwarm.ui`;
- if count exists:
  - sets `game.d.l = (byte)v1[0]`;
  - calls `this.o.m()` tutorial advance hook;
  - consumes one ball via `this.q.d(v1[0], 1, (byte)0)`;
  - enters state `17` via `this.o.a((byte)17)`;
  - closes `/data/ui/choice.ui`.

No `ae.a`/`ae.b` call appears inside this P21 confirm path.

Status: PROVED, no source RNG in P21 confirm itself.

### Tutorial Gate `game.d.l()`

For `U == 0`:

| Source condition | Source behavior | Direct RNG |
|---|---|---|
| `V == 0` | if enemy HP <= 50%, set prompt flags, `V++`, show Bunny wounded / use Phong an cau text | No |
| `V == 1` | waits for taskTip UI close via `S.ay()`, prompts pressing key 5 | No |
| `V == 3` | prompts selecting Phong an cau | No |
| `V == 5` | `V++`, shows caught-failed / try Tat Trung Cau prompt | No |
| `V == 6` | after UI close, enters P21 again | No |
| `V == 8` | clears tutorial flags, sets `U=-1,V=0` | No |

`game.h.ay()` only checks that `/data/ui/taskTip.ui` is closed.

Status: PROVED, no direct RNG in these `U==0` tutorial gate steps.

### Tutorial Advance `game.d.m()`

For `U == 0`:
- if `V == 2 || V == 4 || V == 7`, increment `V`.

This is called by P21 confirm (`game.h.ai()`) before entering P17.

Implications:
- First guided Phong an cau confirm advances `V == 4 -> 5`.
- P17 entry then sees `U == 0 && V == 5` and forces failure.
- Retry Tat Trung Cau confirm advances `V == 7 -> 8`.

Status: PROVED, no RNG.

### P17 Entry

`game.d.a(byte)` case `17`:
- `this.h.p = this.d[0]`;
- lazily creates `this.aj = new f()`;
- loads sprite `269`;
- positions `aj` at enemy coordinates;
- starts `aj`;
- enters q0 via `this.e((byte)0)`;
- computes catch chance `int n4 = this.b((int)l)`;
- source RNG call: `this.ak = ae.a(100) < n4`;
- tutorial edge: `if (U == 0 && V == 5) this.ak = false`;
- clears `this.S.f = 0`.

Status:
- P17 RNG call: PROVED.
- Tutorial force-fail edge: PROVED.

## Route RNG Order Matrix

This table is for the tutorial steps themselves, not every possible random call
that may happen earlier in boot/world/battle damage.

| Order | Source step | Source method | `ae.f` consumption | Rebuild current | Status |
|---:|---|---|---|---|---|
| 1 | Bunny HP reaches tutorial threshold | `game.d.l()` at `U=0,V=0` | None | `finishP7()` checks Bunny HP <= 50%, sets catch command prompt | PORTED/PARTIAL |
| 2 | Command UI asks catch | `game.d.a(20)` / `S.c(...)` | None in P20 entry | `enterCommandState`, `battleCommandIndex=1` | PORTED/PARTIAL |
| 3 | Player confirms Catch | command handler -> state 21 | None in command confirm itself | `tickCommand()` -> `prepareCatchMenu()` -> P21 | PORTED/PARTIAL |
| 4 | P21 opens ball list | `game.d.a(21)` -> `game.h.ah()` | None, except deterministic chance formula display | `prepareCatchMenu()` rows from `sourceBagItems` | PORTED/PARTIAL |
| 5 | Tutorial guides Phong an cau | `game.d.l()` `V=3`; P21 list cursor | None | select item id `1` when `bunnyTutorialFirstCatchPending` | PORTED/PARTIAL |
| 6 | First P21 confirm | `game.h.ai()` | None | `tickCatchList()` item id `1` | PORTED/PARTIAL |
| 7 | Tutorial advance first catch | `game.d.m()` `V=4->5` | None | sets `bunnyTutorialForceFailActive=true` | PORTED/PARTIAL |
| 8 | First P17 entry | `game.d.a(17)` | `ae.a(100)` exactly once for catch decision | `sourceCatchRollPercent()` traced as `battle.P17.catch` | PORTED/PARTIAL |
| 9 | First P17 forced fail | `if U==0 && V==5` | No extra RNG; overrides result to false | `bunnyTutorialForceFailActive` overrides caught false | PORTED/PARTIAL |
| 10 | P17 fail animation/result | `game.d` tick case 17 q4 | No RNG found in q4 tick body | phase 4 fail path | PORTED/PARTIAL |
| 11 | Retry prompt | `game.d.l()` `V=5->6` | None | taskTip retry text | PORTED/PARTIAL |
| 12 | Return to P21 | `game.d.l()` `V=6`, `a((byte)21)` | None | prompt close -> `prepareCatchMenu()` -> P21 | PORTED/PARTIAL |
| 13 | Retry P21 selects Tat Trung Cau | P21 list + guide | None | select item id `0` | PORTED/PARTIAL |
| 14 | Second P21 confirm | `game.h.ai()` | None | `tickCatchList()` item id `0` | PORTED/PARTIAL |
| 15 | Tutorial advance retry | `game.d.m()` `V=7->8` | None | clears retry pending, logs retry item 0 | PORTED/PARTIAL |
| 16 | Second P17 entry | `game.d.a(17)` | `ae.a(100)` once, but chance is 100 for item0 | `sourceCatchRollPercent()` traced as `battle.P17.catch`; item0 chance 100 | PORTED/PARTIAL |
| 17 | Tutorial cleanup | `game.d.l()` `V=8` | None | route completes after catch; full global `U/V` storage is approximate | PORTED/PARTIAL |

## Important Boundary

For this Bunny tutorial route segment, the source steps P20, P21, tutorial
`l()/m()`, and P17 q4 fail path do not introduce additional `ae.f` consumers
besides the P17 entry roll itself.

However, exact RNG parity for the entire playthrough is still PENDING because
earlier route phases can consume `ae.f`:
- boot/logo/menu;
- world actor behavior;
- encounter setup;
- battle unit generation;
- P7 damage/status/passive logic before Bunny reaches low HP.

This audit only proves the local route section from the tutorial catch command
through P17.

## Current Rebuild Delta

| Area | Rebuild status | Notes |
|---|---|---|
| Bunny weak HP gate | PORTED/PARTIAL | Implemented after P7 when enemy HP <= 50%; not full global `U/V` implementation. |
| P21 ball list | PORTED/PARTIAL | Uses source-backed item rows and chance formula; not full `game.h` UI runtime. |
| First Phong an cau selection | PORTED/PARTIAL | Cursor selects item id `1` when first tutorial catch is pending. |
| First forced fail | PORTED/PARTIAL | Rebuild force-fails first item1 tutorial catch, matching `U==0,V==5` behavior. |
| Retry prompt | PORTED/PARTIAL | Text and taskTip path exist; exact source widget state flags are partial. |
| Retry P21 Tat Trung Cau | PORTED/PARTIAL | Cursor selects item id `0`; menu ids smoke verifies `[0,1]`. |
| P17 RNG trace | PORTED/PARTIAL | `battle.P17.catch` traces helper/bound/raw/return/seed. Exact prior stream order not solved. |
| Global `U/V` state | APPROX/PARTIAL | Rebuild has route-local booleans instead of full source static/tutorial state. |
| P101/SMS missing item0 path | PENDING | PC client makes SMS path nonessential for current route, but source state remains documented. |

## Focused Smoke Coverage

Current route-specific PNG checkpoints:
- `battle_bunny_first_catch_forced_fail`
- `battle_bunny_retry_p21_item0`
- `battle_bunny_retry_prompt_tasktip`
- `route_bunny_after_battle_task`
- `battle_rng_trace_p17_catch`

Expected assertions:
- first P17 uses item id `1` and `caught=false`;
- retry P21 has menu ids `[0,1]` and cursor item id `0`;
- retry prompt is taskTip text;
- route completes with Bunny caught and task return text;
- P17 RNG trace records `helper=ae.a(int)`, `bound=100`, `raw`, `return`, and
  injected seed in the focused trace smoke.

## Classification

| Concern | Status |
|---|---|
| Source local P20/P21/tutorial/P17 order | PROVED |
| Source local RNG consumers in P20/P21/tutorial gate | PROVED none |
| Source P17 catch roll | PROVED one `ae.a(100)` per P17 entry |
| Rebuild P17 trace hook | PORTED/PARTIAL |
| Bunny forced fail/retry route | PORTED/PARTIAL |
| Exact full route RNG stream from game boot to catch | PENDING |
| P17 pixel-perfect animation/UI | PENDING |

## Recommended Next Step

The local Bunny P21/P17 tutorial route does not need a new RNG behavior change
yet. The next useful slice should be one of:

1. Add audit-only trace labels to `BattleUnit.randomPercent()` callsites before
   Bunny reaches HP <= 50%, without replacing RNG behavior.
2. Or start P17 frame/pixel audit by collecting original-vs-rebuild capture pairs
   for the q0/q1/q2/q4 first forced-fail route.

Do not replace `BattleUnit.BATTLE_RANDOM` with `VqsvSourceRandom` until the
source call order before Bunny weak gate is proven.
