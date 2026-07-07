# 74 Battle `game.d` State Full Matrix

Status: SOURCE/CODE MAPPING AUDIT ONLY.

Scope: Phase 2 battle state-machine audit. This file maps the source battle
state machine in `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
against the current rebuild runtime in
`rebuild_game/src/main/java/VqsvBattleRuntime.java`.

No rebuild code was changed in this step.

## Source Files Read

Primary source:

- `game.d.a(byte)` state enter/init: source around lines `697..973`.
- `game.d.b()` update switch: source around lines `976..1763`.
- `game.d.b(Graphics)` draw switch: source around lines `1825..1923`.
- `game.d.h()`, `game.d.p()`, `game.d.q()`: turn advance and post-P7 effect
  handling around lines `1929..2175`.
- `game.d.l()/m()`: Bunny/tutorial catch guidance around lines `2570..2675`.

UI facade source:

- `game.h.f(b)`: skill list update / confirm.
- `game.h.ah()/ai()`: P21 catch list.
- `game.h.aj()/ak()`: P4 item list.
- `game.h.al()`: P16 item target/use.
- `game.h.X()`: P5 pet switch.
- `game.h.am()/an()/ao()/ap()/aq()`: win, exp, level-up, learn skill.
- `game.h.aE()/aF()/aH()/aI()/aJ()/aM()`: revive/SMS purchase states.

Current rebuild:

- `BattleRuntimeState` enum covers P0/P20/P3/P6/P21/P17/P4/P16/P5/P11/P10,
  WARNING, P2/P7/P1/P8/P9, EXIT/DONE.
- `SourceBattleRuntime.tick()` dispatches those rebuild states.

## High-Level Result

The rebuild already has the visible battle backbone, but it is not a full
`game.d` state machine yet.

| Area | Current rebuild status | Reason |
| --- | --- | --- |
| Entry/dispatch/result path | PARTIAL | P0/P1/P2/P7/P8/P9 exist and story routes pass, but source replacement, EXP, level-up, death and round lifecycle are incomplete. |
| Command path | PORTED/PARTIAL | P20/P3/P6/P21/P17/P4/P16/P5/P10/P11 exist, but many source validation/warning branches are simplified. |
| P7 resolve | PORTED/PARTIAL | Effect/animation backbone exists. Full `BattleDamageResult` and `game.d.q()` post-skill heal/buff/leech/reflect slice are now represented; death/replacement, P12/P13 active queues and some effect triggers are not complete. |
| UI facade | PARTIAL | Rebuild has source-shaped command/choice/skill panels, but not full `game.h` widget/runtime parity. |
| Extra source states | MISSING/PENDING | P12/P13/P15/P18/P19/P22/P23/P24/P101/P102/P104 are source states with no direct rebuild equivalent. |

## State Matrix

| Source state | Source enter/init | Source update | Source draw | UI/method | Input | Side effects | Next state(s) | Rebuild equivalent | Status |
| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |
| P0 entry | `a(byte 0)` finds first player-side live unit and resets `i`. | Plays entry positions via `an[r][G]`, updates actor and `al` positions, then enters P20. | Draws battle scene and all actors. | Battle scene setup, no menu. | None. | Initializes visible battle actors and entry animation. | P20. | `P0_ENTRY` with fade/short wait. | PORTED/PARTIAL: visible entry exists, source position table animation not fully equivalent. |
| P1 dispatch | `a(byte 1)` selects next actor in sorted vector `v`, skips dead/J actors, handles no-PP warning for player. | Waits for `S.ay()`, then chooses player command, AI execute, or starts new round. Handles `w/y/T()` round flags. | Draws battle scene. | `S.ay()`, `S.g()`, actor marker `al`. | Mostly text/UI completion. | Core turn order, skips dead units, starts new round, checks AI/player. | P20, P2, P12, P13, P1. | `P1_DISPATCH`. | PARTIAL: speed order exists; source buff/debuff tick, J flag, round reset, forced state transitions incomplete. |
| P2 select/execute | `a(byte 2)` has no init. | AI or selected player skill resolves target. Enemy chooses skill via `e(h)` and random target. Player uses selected skill/target. Calls `h.a(...)`, `S.b(attacker,target)`, then P7. | Draws battle scene. | Source target builders `b(byte)`, `f(b)`, selected fields `D/I/p/G/H`. | None in state itself. | Finalizes skill and target, consumes selected action path. | P7. | `P2_SELECT_EXECUTE`. | PARTIAL: basic player/enemy select exists; enemy skill RNG, statuses and multi-target parity incomplete. |
| P3 skill list | `a(byte 3)` calls `S.e(activeUnit)`. | `S.f(activeUnit)` handles choiceskill UI, PP check, confirm/back. Confirm can call target builder then P6 or direct `i()`. | Source draw only sets white color; UI drawn by facade. | `/data/ui/choiceskill.ui`, `game.h.f(b)`. | Up/down, confirm, back. | Checks PP via `v1.s(e)`, sets selected skill index, may show warning. | P6, P20, P7 via direct `i()`. | `P3_SKILL_LIST`. | PORTED/PARTIAL: choiceskill-shaped panel and PP labels exist; warning/disabled/widget parity partial. |
| P4 item list | `a(byte 4)` calls `S.aj()`. | `S.ak()` opens choice UI, blocks item behavior 7..10, confirms to P16, or backs to P20. | Source draw only sets white color; UI drawn by facade. | `/data/ui/choice.ui`, `/data/ui/msgwarm.ui`, `game.h.aj()/ak()`. | Up/down, confirm, back. | Selects item id from bag list, validates battle-usability. | P16, P20, P4 warning loop. | `P4_ITEM_LIST`. | PARTIAL/APPROX: menu exists; item source list and validation only partly equivalent. |
| P5 pet switch | `a(byte 5)` sets `S.c=0`, calls `S.W()`. | `S.X()` handles pet-state UI and switch. | Draws selected pet name at y=200. | Pet-state UI through `game.h.W()/X()`. | Direction/confirm/back through UI. | Selects replacement pet, rearranges party/active slot. | P1/P20 or menu return depending source path. | `P5_PET_SWITCH`. | PARTIAL: menu shell and simple switch exist; source restrictions, dead/current checks and switch turn cost not complete. |
| P6 target select | `a(byte 6)` initializes `C=0`, highlights first target, positions marker. | Direction keys cycle `h.G/H`; confirm calls `i()`, back goes P3. | Draws scene, with `a(graphics,true)` only in battle mode 1. | Target highlight via `S.b(attacker,target)`, marker `al`. | Up/down/left/right, confirm, back. | Selects `h.p`, `h.I`, selected skill, then advances turn. | P3, P7 through `i()/h()`. | `P6_TARGET_SELECT`. | PORTED/PARTIAL: target vector from skill target column exists; source marker/formation/multi-target parity limited. |
| P7 resolve | `a(byte 7)` resets effect flags, calls `n()` to load effect row, computes damage array `Z`, sets attacker action state. | Large effect state machine: actor `u`, special `H`, chunk triggers `[4]/[5]/[6]`, damage/miss text, HP bar update, death/replacement, then `q()` for post-skill effects. | Draws scene, special effect `H`, actors, floating text. | Effect tables `effect.mid`, `speffect.mid`, `blood.mid`, actor `u`, `S.a/b` HP bar. | None direct. | Damage, crit/debuff text, HP update, actor states, death, forced replacement, win/lose, post-effect heal/buff/leech via `q()`. | P1, P2, P5, P8, P9, P15, P20, P12/P13 in some chains. | `P7_RESOLVE`. | PORTED/PARTIAL: animation/effect backbone, full `BattleDamageResult`, and `q()` post-skill heal/buff/leech/reflect slice exist. Forced replacement, P12/P13 active queues, multi-floating-text parity and remaining broad result hooks are still pending. |
| P8 win/EXP | `a(byte 8)` resets battle map state, applies some after-win HP/EXP setup, positions EXP UI. | `S.am()` increments EXP display, may enter P22 level-up or exit world. | Draws EXP/level result actor panel. | `game.h.am()`, result UI. | Confirm can fast-forward. | Award EXP to `game.d.j`, update EXP bars, maybe level-up. | P22, world state 10. | `P8_WIN`. | PARTIAL: story branch win works; EXP/level-up/reward parity incomplete. |
| P9 lose | `a(byte 9)` either P24 revive flow or revives/sets world state 10. | Empty in update. | Not drawn separately in switch. | World transition / revive. | None. | Loss handling, pet HP reset, world state. | P24 or world state 10. | `P9_LOSE`. | PARTIAL: current route can lose/branch, but source revive/world side effects incomplete. |
| P10 run | `a(byte 10)` no init. | Shows run text if allowed, waits for UI complete then exits to world. | Draws battle scene. | `S.c(...)`, `S.g()`. | Mostly UI completion. | Attempts escape from battle. | World state 10. | `P10_RUN`. | PARTIAL: shell exists; exact source restrictions/chance need closer audit. |
| P11 shop | `a(byte 11)` calls `S.a(4,0)`. | `S.a(byte 4, byte 0)` shop/buy flow. | No explicit draw case. | Battle shop UI in `game.h`. | Shop UI input. | Buy item, money checks. | Usually returns menu/world depending UI. | `P11_SHOP`. | APPROX/PARTIAL: minimal shop buy exists, not full source shop UI/runtime. |
| P12 enemy/effect sequence | `a(byte 12)` shares init with P13: loads active buff/debuff/effect arrays `af/ag/ah`, starts effect sequence or calls `o()`. | Runs `u`/`H` effect sequence, applies queued buffs/debuffs, updates HP text/bar. | Draws scene plus `H` and floating text. | Actor/effect runtime, `S.a/b` HP bar. | None direct. | Applies active queued effect bank 0/1 entries from `x`, damage/heal float text, HP update. | P1/P20/P13 depending active actor. | No rebuild state. | MISSING/PENDING: active queue effect resolution not represented as its own state. |
| P13 player/effect sequence | Same as P12. | Same as P12, entered when a player actor has active queues via `game.d.d(b)`. | Same as P12. | Same as P12. | None direct. | Same as P12 but for player-side queued effects. | P1/P20/P13. | No rebuild state. | MISSING/PENDING. |
| P14 shop/body state | `a(byte 14)` no init. | Empty. | No explicit draw. | Related `game.h.as()` body shop path elsewhere. | Unknown in battle slice. | Unknown/transition. | Unknown. | No rebuild state. | UNKNOWN/PENDING: source case exists but not closed in current battle path. |
| P15 forced replacement | `a(byte 15)` sets forced replacement `g`, swaps active player pet, marks J, positions replacement. | Plays replacement entry animation, then chooses next actor/P20/P13/P1 and clears flags. | Draws battle scene. | Entry animation table `an`, active actor `al`. | None. | Replaces defeated/forced player pet and resumes turn order. | P1, P13, P20. | No direct rebuild state. | MISSING: current pet/death replacement is simplified. |
| P16 item target/use | `a(byte 16)` sets selection fields and `S.W()`. | `S.al()` target selection and `bo()` item use result handling. | No explicit draw. | Pet target UI, `/data/ui/msgwarm.ui`, `/data/ui/petstate.ui`. | Up/down, confirm, back. | Uses item on selected pet through `game.b.x/w`, consumes item, may set active unit J. | P4, P1, P20 depending success/back. | `P16_ITEM_TARGET`. | PARTIAL/APPROX: HP/PP shell exists; revive/status/full `game.b.w/x` behavior missing. |
| P17 catch result | `a(byte 17)` sets target to enemy slot 0, loads sprite 269, starts phase `q=0`, computes catch chance via `b(itemId)`. | Runs catch animation phases q=0..4, success/fail messages, storage via `game.g.y()/a()/b()`, returns to world or P1. | Draws battle scene, catch sprite and special `H` ordering. | Sprite `269`, `H/ah`, `S.b(...)` messages. | Confirm after result messages. | Consumes ball before entry, hides/restores enemy, add pet to bag/bank/release full, or continue battle. | P1, P8/world state 10. | `P17_CATCH_RESULT`. | PORTED/PARTIAL: chance/list/storage shell exists; exact animation/storage parity still incomplete. |
| P18 | `a(byte 18)` no init. | Empty. | Empty draw. | Unknown. | Unknown. | Unknown. | Unknown. | No rebuild state. | UNKNOWN/PENDING. |
| P19 | No enter case, update waits for key mask `786432` then P18. | Key-gated transition to P18. | No draw case. | Unknown. | Key mask `786432`. | Unknown. | P18. | No rebuild state. | UNKNOWN/PENDING. |
| P20 command | `a(byte 20)` sets active unit `h`, refreshes alive player HP bars, positions command marker and command UI target info. | Calls `S.d(h)` every tick. | Draws battle scene with command overlay. | `game.h.d(b)` command bar. | Direction/click/confirm through UI facade. | Opens command choice: fight/catch/item/pet/shop/run. | P3, P21, P4, P5, P10, P11. | `P20_COMMAND`. | PORTED/PARTIAL: command bar works; exact disabled flags/status prompts still partial. |
| P21 catch list | `a(byte 21)` sets target enemy and calls `S.ah()`. | `S.ai()` handles ball list, consumes ball, calls tutorial `m()`, enters P17, or back/P101 warning. | No explicit draw; UI facade draws choice. | `/data/ui/choice.ui`, `game.h.ah()/ai()`. | Up/down, confirm, back. | Lists bag balls where item behavior is catch; removes selected ball. | P17, P20, P101. | `P21_CATCH_LIST`. | PORTED/PARTIAL: source-shaped list exists; exact bag/source tutorial edge cases partial. |
| P22 level-up wait | `a(byte 22)` calls `S.an()` to open levelUp UI. | `S.ao()` auto/confirm advances, may enter P23 skill learn or P8 next EXP. | No draw case in `d.b(Graphics)`, UI facade draws. | `/data/ui/levelUp.ui`, `game.h.an()/ao()`. | Confirm/timeout. | Applies stat growth, may offer new skill. | P23, P8, world state 10. | No rebuild state. | MISSING/PENDING: current win result lacks full level-up flow. |
| P23 learn skill | `a(byte 23)` calls `S.ap()`. | `S.aq()` choiceskill UI for new skill; confirm learns/returns. | No draw case. | `/data/ui/choiceskill.ui`, `/data/ui/levelUp.ui`, `game.h.ap()/aq()`. | Up/down, confirm/back. | Learns/replaces skill after level-up. | P8 or world state 10. | No rebuild state. | MISSING/PENDING. |
| P24 revive/pay after loss | `a(byte 24)` calls `S.aE()`. | `S.aF()` handles pay 10000 revive, SMS-like restore, or exit. | No draw case. | `/data/ui/smsInfo.ui`, `game.h.aE()/aF()`. | Confirm/back. | Money check, revive all pets, world transition. | P0/world state or P102. | No rebuild state. | MISSING/PENDING. |
| P101 ball purchase/SMS | `a(byte 101)` calls `S.aH()`. | Shared `S.aM()` SMS/purchase flow. | No draw case. | `/data/ui/smsInfo.ui`, `game.h.aH()/aM()`. | SMS/purchase UI input. | Purchase all-hit ball / SMS side effects. | UI-specific states/world. | No rebuild state. | MISSING/PENDING. |
| P102 money purchase/SMS | `a(byte 102)` calls `S.aJ()`. | Shared `S.aM()`. | No draw case. | `/data/ui/smsInfo.ui`, `game.h.aJ()/aM()`. | SMS/purchase UI input. | Purchase money / SMS side effects. | UI-specific states/world. | No rebuild state. | MISSING/PENDING. |
| P104 badge purchase/SMS | `a(byte 104)` calls `S.aI()`. | Shared `S.aM()`. | No draw case. | `/data/ui/smsInfo.ui`, `game.h.aI()/aM()`. | SMS/purchase UI input. | Purchase badge / SMS side effects. | UI-specific states/world. | No rebuild state. | MISSING/PENDING. |
| Warning states | Not a separate `game.d.P`; source warnings are usually `msgwarm.ui` with local `game.h.f` flags. | UI methods close warning and return to previous source state. | UI facade. | `/data/ui/msgwarm.ui`. | Confirm. | Does not always consume turn; depends on caller. | Return to P3/P4/P16/P20/etc. | `WARNING`. | APPROX/PARTIAL: rebuild has explicit warning state; source uses per-UI `f` flags. |

## Important Source Transition Facts

| Fact | Source evidence | Rebuild implication |
| --- | --- | --- |
| P20 is not the first state after entry animation. | P0 update advances through `an` entry frames then calls `a(byte 20)`. | Entry timing should remain separate from command UI. |
| P1 is the real turn dispatcher. | P1 update chooses P20 for player, P2 for enemy/player execution, or P12/P13 when active queues exist. | Buff/debuff lifecycle belongs around P1/P12/P13, not only P7. |
| P7 does not end immediately after damage. | P7 update waits actor/special effects, applies damage text/bar, then calls `q()`, then resolves death/replacement/win/lose. | P7 must preserve full damage result and post-effect context until `q()` equivalent runs. |
| Catch is two states. | P21 opens ball choice and consumes item; P17 performs animation/result/storage. | P21 and P17 should stay distinct; do not collapse catch into command confirm. |
| Item is two states. | P4 chooses item; P16 chooses pet target and calls `game.b.x/w` behavior. | Full item behavior requires `game.b.w/x`, not just item table rows. |
| Level-up/learn skill are battle states. | P8 can enter P22 and P23 before returning world. | Story smoke can pass while full result parity is still missing. |
| SMS/purchase states are reachable from battle UI. | P21 can go P101, P24 uses revive/pay, P102/P104 exist. | Full parity eventually needs payment/SMS state handling, though not core battle damage. |

## Current Rebuild State Coverage

| Rebuild state | Source equivalent | Current status | Gap |
| --- | --- | --- | --- |
| `P0_ENTRY` | P0 | PORTED/PARTIAL | Source entry position table/timing not fully copied. |
| `P1_DISPATCH` | P1 | PARTIAL | Missing full turn tick, active queue P12/P13, forced replacement integration. |
| `P2_SELECT_EXECUTE` | P2 | PARTIAL | Enemy AI/status target selection simplified. |
| `P3_SKILL_LIST` | P3 | PORTED/PARTIAL | UI close enough for smoke; not full `game.h` widget/warning parity. |
| `P4_ITEM_LIST` | P4 | PARTIAL/APPROX | Item list exists, source behavior matrix incomplete. |
| `P5_PET_SWITCH` | P5 | PARTIAL | Switch rules and active slot side effects incomplete. |
| `P6_TARGET_SELECT` | P6 | PORTED/PARTIAL | Basic target select exists; formation/multi-target incomplete. |
| `P7_RESOLVE` | P7 | PORTED/PARTIAL | Highest-impact gap: damage result and post-effect lifecycle. |
| `P8_WIN` | P8 | PARTIAL | Branch works; EXP/level-up/learn missing. |
| `P9_LOSE` | P9 | PARTIAL | Lose branch exists; revive/payment/world side effects incomplete. |
| `P10_RUN` | P10 | PARTIAL | Chance/restrictions need source validation. |
| `P11_SHOP` | P11 | APPROX/PARTIAL | Minimal buy flow only. |
| `P16_ITEM_TARGET` | P16 | PARTIAL/APPROX | Needs `game.b.x/w`. |
| `P17_CATCH_RESULT` | P17 | PORTED/PARTIAL | Catch animation/storage not full parity. |
| `P20_COMMAND` | P20 | PORTED/PARTIAL | Command UI source-shaped, disabled checks partial. |
| `P21_CATCH_LIST` | P21 | PORTED/PARTIAL | Ball list/chance exists; tutorial/SMS edge cases partial. |
| `WARNING` | Source `msgwarm.ui` local flags | APPROX/PARTIAL | Explicit rebuild state differs from source per-UI warning flags. |
| `EXIT_FADE` / `DONE` | world transition through `game.i.a(...)` | APPROX | Rebuild wrapper state, not source battle state. |

## Missing Source States To Port Later

| Missing source state | Priority | Why |
| --- | --- | --- |
| P12/P13 | High | Required for active buff/debuff/effect queue lifecycle from `game.b.x/N`. |
| P15 | High | Required for real forced replacement after player pet death. |
| P22/P23 | Medium | Required for full EXP, level-up and learn-skill parity. |
| P24 | Medium | Required for full lose/revive behavior. |
| P101/P102/P104 | Low/Medium | Required for SMS/payment purchase parity, but not core damage. |
| P18/P19/P14 | Unknown | Source cases exist but current path meaning is not closed. Need targeted trace before port. |

## Next Code Slice Recommendation

The first code slice after this audit should be:

`Preserve Full BattleDamageResult Through P7`

Reason:

- Phase 1 already has `BattleUnit.computeDamage()` returning
  `BattleDamageResult`.
- Current P7 collapses that result to damage amount through
  `SourceBattleUnit.basicDamageTo(...)`.
- Source P7 visibly consumes more than damage: crit style (`Z[1]`), debuff text
  (`Z[2]`), HP bar update, miss text, and post-effect handling through `q()`.
- This slice is smaller and safer than implementing full P12/P13 or item/catch
  parity, and it directly improves visible battle correctness.

Minimum next implementation criteria:

1. Add a P7 runtime field for full `BattleDamageResult`.
2. Preserve `damage`, `critFlag`, and `appliedDebuffId` until damage frame.
3. Show crit/debuff text according to the existing source table wrappers.
4. Do not port `SOURCE_SWITCH_GAP` skills by guessing.
5. Keep P12/P13/P15/P22/P23/P24 documented as pending, not silently skipped.

## Done Criteria For This Audit

- Every source battle state visible in `game.d.a(byte)`, `game.d.b()`, or
  `game.d.b(Graphics)` has a matrix row.
- Every current rebuild `BattleRuntimeState` has a status row.
- Shell/approx states are explicitly marked.
- Next code task is selected from the state-machine gaps, not from visual polish.
