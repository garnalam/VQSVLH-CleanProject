# 81 New Chat Hand Off - Battle Engine Current

> SUPERSEDED 2026-07-08: this handoff captured the pre-catch/levelUp state.
> New dev chats must start from
> `rebuild_plan/102_new_dev_chat_handoff_battle_engine_after_levelup.md`.
> Keep this file as historical context only. Its "next target" and
> "EXP/level-up pending" statements are outdated.

Status: NEW DEV CHAT HANDOFF / TRAINING / ENTRY EXERCISE.

This document is for a new dev chat that will continue the VQSV/Liet Hoa
rebuild battle engine. Read this before coding. Do not skip the entry exercise.

Do not use machine-specific absolute paths in new docs or code. Use project
relative paths such as `modules/...`, `rebuild_game/...`, and `rebuild_plan/...`.

## Copy-Paste Prompt For The New Chat

You are taking over the VQSV/Liet Hoa rebuild battle engine.

Rules:

- Source first, then logic, then UI/effect.
- Do not guess behavior, sprites, UI, effects, timing, or event links.
- Prove every claim from `modules/source_code/...`, decoded resources under
  `modules/...`, rebuild code under `rebuild_game/...`, or docs under
  `rebuild_plan/...`.
- Mark every piece honestly as `PORTED`, `PARTIAL`, `APPROX`, `STUB`,
  `PENDING`, `UNKNOWN`, or `DAMAGED`.
- Do not touch intro/world/panel/scene scripts unless the battle task requires
  it.
- Do not open the live client unless explicitly asked. Use smoke PNG.
- Keep Vietnamese text in `VqsvText`/Unicode-safe form; do not introduce
  mojibake.
- After code: run build, `--check`, formula check if battle runtime/formula was
  touched, mojibake scan, and focused smoke PNG.

Historical note: this prompt is superseded. New chats must read
`rebuild_plan/102_new_dev_chat_handoff_battle_engine_after_levelup.md`, not this
file, before coding.

## Prime Directive

- Source first, then logic, then UI/effect.
- Do not guess behavior, sprites, UI, effects, timing, or event links.
- Every claim must point back to one of:
  - original/decompiled source under `modules/source_code/...`
  - decoded event/table/resource under `modules/...`
  - current rebuild code under `rebuild_game/src/main/java/...`
  - current audit docs under `rebuild_plan/...`
- Classify honestly: `PORTED`, `PARTIAL`, `APPROX`, `STUB`, `PENDING`,
  `UNKNOWN`, or `DAMAGED`.
- Do not hide stubs/approximations.
- Do not touch intro/world/panel/scene scripts unless the current task explicitly
  requires it.
- Do not open the live Swing client unless the user asks. Prefer smoke PNG.
- After code changes: build, check, mojibake scan, and focused smoke.
- If a piece was user-confirmed as good, do not refactor or "clean it up" unless
  the task directly targets it.

## How To Work In This Project

The desired working style is strict and incremental:

1. Audit source/resource first.
2. Write or update the matrix doc.
3. Choose one small slice with clear source proof.
4. Implement only that slice.
5. Build/check/smoke.
6. Report what is truly done and what remains partial.

Useful habits:

- Use `rg` for source and table search.
- Use `apply_patch` for manual edits.
- Use smoke PNG checkpoints instead of launching the live client.
- If a visual result looks wrong, create a named regression smoke so the same bug
  does not return.
- UI is not decoration: port UI only when `game.d/game.h` source logic calls it.
- When source is damaged or ambiguous, say `DAMAGED` or `UNKNOWN`; do not fill
  gaps with invented behavior.

Automatic fail conditions for a new chat:

- It claims pixel-perfect parity without original MIDP comparison.
- It opens the live client when only smoke PNG was requested.
- It edits intro/world/panel while working on battle without a source-backed
  reason.
- It adds guessed sprite/effect/UI behavior.
- It hides a stub behind wording like "basically done".
- It introduces mojibake in Java source.

## Required Reading Order

Read these in order before making any code change:

1. `rebuild_plan/battle_engine_master_roadmap_progress.md`
2. `rebuild_plan/50_battle_full_engine_port_plan.md`
3. `rebuild_plan/52_battle_logic_asset_full_audit.md`
4. `rebuild_plan/53_battle_current_status_and_next_plan.md`
5. `rebuild_plan/70_battle_unit_full_field_matrix.md`
6. `rebuild_plan/71_battle_skill_status_table_matrix.md`
7. `rebuild_plan/72_battle_full_skill_status_behavior_classification.md`
8. `rebuild_plan/73_battle_rebuild_mapping_next_code_tasks.md`
9. `rebuild_plan/74_battle_game_d_state_full_matrix.md`
10. `rebuild_plan/75_battle_game_d_q_post_skill_matrix.md`
11. `rebuild_plan/76_battle_p12_p13_active_queue_lifecycle_matrix.md`
12. `rebuild_plan/77_battle_p12_p13_h_speffect_matrix.md`
13. `rebuild_plan/78_battle_active_effect_lifecycle_full_matrix.md`
14. `rebuild_plan/79_battle_item_pet_catch_state_matrix.md`
15. `rebuild_plan/80_battle_p15_cpos_transition_matrix.md`
16. `rebuild_game/src/main/java/VqsvBattleRuntime.java`
17. `rebuild_game/src/main/java/VqsvBattleUnit.java`
18. `rebuild_game/src/main/java/VqsvBattleRenderer.java`
19. `rebuild_game/src/main/java/VqsvBattleTables.java`
20. `rebuild_game/src/main/java/VqsvBattleAnimationTables.java`
21. `rebuild_game/src/main/java/VqsvSmokeHarness.java`
22. `rebuild_game/src/main/java/VqsvText.java`

Source files to keep open for battle work:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/g.java`
- `modules/source_code/decoded/decompiled_source_cfr/ae.java`
- `modules/source_code/decoded/decompiled_source_cfr/ah.java`
- `modules/source_code/decoded/decompiled_source_cfr/f.java`
- `modules/source_code/decoded/decompiled_source_cfr/l.java`

Resource/table files commonly needed:

- `modules/script/original/db.mid`
- `modules/script/original/effect.mid`
- `modules/script/original/speffect.mid`
- `modules/script/original/blood.mid`
- `modules/script/original/bufDebuf.mid`
- `modules/script/original/pos.mid`
- `modules/script/original/cpos.mid`
- `modules/script/decoded/data__script__db.mid.json`
- `modules/script/decoded/data__script__effect.mid.json`
- `modules/script/decoded/data__script__speffect.mid.json`
- `modules/script/decoded/data__script__blood.mid.json`
- `modules/script/decoded/data__script__bufDebuf.mid.json`
- `modules/script/decoded/data__script__cpos.mid.json`
- `modules/ui/decoded/data__ui__battle.ui.json`
- `modules/ui/decoded/data__ui__choice.ui.json`
- `modules/ui/decoded/data__ui__choiceskill.ui.json`
- `modules/ui/decoded/data__ui__petstate.ui.json`
- `modules/ui/decoded/data__ui__msgwarm.ui.json`

## Current Battle Status

The battle engine is no longer a pure stub, but it is not complete.

### PORTED / PORTED-PARTIAL

- `BattleUnit` data model exists and mirrors many `game.b` arrays:
  `c[]/d[]`, skills/PP, buff/debuff slots, active effect queues.
- Direct damage formula is source-shaped and covered by
  `VqsvBattleDamageFormulaCheck`.
- Core state runtime exists:
  - P0 entry
  - P20 command
  - P3 skill list
  - P6 target select
  - P2 select/execute
  - P7 resolve
  - P12/P13 active effect queue
  - P15 enemy replacement
  - P8/P9 result
  - P4/P16 item
  - P5 pet switch
  - P21/P17 catch
  - P10 run
  - P11 shop shell
- P3 `choiceskill.ui` is source-shaped.
- P6 target select is source-shaped.
- P7 preserves full `BattleDamageResult` through damage frame.
- P7 post-skill `game.d.q()` behavior has source-shaped slices.
- P12/P13 active queue supports current known buff/debuff paths, including:
  - buff id 9
  - debuff id 0
  - debuff id 3
  - stat debuff id 5
  - kind 0 actor-action visual slice
  - kind 1 H/speffect slices for source-reachable AH types 1/8/9/12
- P16 item target/use ports `game.b.x(int)` validation and
  `game.b.w(int)` behavior 1..6 at current rebuild granularity.
- P5 pet switch now has:
  - dead/current/invalid validation
  - voluntary and forced replacement list behavior
  - source pet reorder `f[slot] -> f[0]`
  - HP/PP persistence for source pet payload
  - battle-only `petstate` renderer
  - selected pet detail panel
  - source P5 button label `Xuat chien`
  - petstate-specific click hitbox
  - source state 15 transition before P1
  - `cpos.mid` row `an[0][1]` player-switch motion for current elder P5 route
- Live checkpoint `battle_elder_command_ui` seeds a reserve pet so P5 can be
  tested. Live checkpoint `battle_elder_pet_p5` opens directly into P5.

### PARTIAL / APPROX / PENDING

- Full generic `game.h` widget runtime is not ported.
- P16 item target UI still uses a generic source-shaped menu, not full
  `petstate.ui`.
- P5 `petstate` renderer lacks full equipment/relation/evolution data because
  the current rebuild pet payload does not carry all original fields.
- Full `game.d.an` parity is not complete. Current P5 player switch uses
  `cpos.mid`; P0 entry, enemy replacement, side-marker `al[]`, group 1
  multi-unit layout, and exact MIDP sprite anchor remain partial.
- P21/P17 catch is still the next major Phase 5 target:
  - tutorial gating is not fully source-exact
  - no-ball/purchase/SMS path is partial
  - catch chance/storage payload parity is partial
  - P17 animation lifecycle is source-shaped but not pixel-perfect
- P11 shop is a shell/minimal flow.
- Historical status only: EXP/level-up/learn-skill P22/P23 were pending when
  this file was written. They are now PORTED/PARTIAL in `100` and `101`.
- Full passive/global context from `game.g/game.k` is partial.
- Broad skill coverage is partial even though many representative skills have
  smoke slices.
- Pixel-perfect MIDP compare is not done unless the user explicitly provides
  original screenshots/observations.

## Recent Fixes That Must Not Be Regressed

These were recent user-visible issues:

- Pet switch could not be tested from `battle_elder_command_ui` because the
  checkpoint had only one pet. Fixed by seeding a reserve pet.
- P5 click used `choice.ui` hitbox. Fixed with `petstate` row hitbox.
- P5 UI title and `Quay lai` label were clipped or low contrast. Fixed.
- P5 valid switch now animates via `cpos.mid` for the current elder path.

Required P5 regression smokes:

- `battle_elder_pet_p5`
- `battle_p5_click_reserve_success`
- `battle_p5_current_warning`
- `battle_p5_dead_warning`
- `battle_p5_forced_replacement_success`
- `battle_p5_switch_transition`
- `battle_p5_switch_transition_mid`

## Commands

Run from `rebuild_game`.

Build:

```powershell
powershell -ExecutionPolicy Bypass -File .\build.ps1
```

Core check:

```powershell
java "-Dvqsv.modules=..\modules" -jar .\build\libs\vqsv-rebuild-skeleton.jar --check
```

Formula check:

```powershell
java -cp .\build\classes VqsvBattleDamageFormulaCheck
```

Mojibake scan:

```powershell
$bad = Select-String -Path .\src\main\java\*.java -Pattern '�|Ã|Â|Ă|Ä|Æ' -AllMatches
if ($bad) { $bad | Select-Object Path,LineNumber,Line; exit 1 } else { 'mojibake-scan-ok' }
```

Smoke PNG:

```powershell
java -cp .\build\classes VqsvIntroDemo --smoke-checkpoint battle_elder_pet_p5 build_intro_demo\battle_elder_pet_p5.png
```

Live test only when user asks:

```powershell
java -cp .\build\classes VqsvIntroDemo --play-checkpoint battle_elder_command_ui
java -cp .\build\classes VqsvIntroDemo --play-checkpoint battle_elder_pet_p5
java -cp .\build\classes VqsvIntroDemo --play-checkpoint battle_bunny_command_ui
java -cp .\build\classes VqsvIntroDemo --play-checkpoint battle_elder_skill15_p7
java -cp .\build\classes VqsvIntroDemo --play-checkpoint battle_elder_skill45_p7
java -cp .\build\classes VqsvIntroDemo --play-checkpoint battle_sophie_command_ui
```

## Verification Standard

For a code slice, do at least:

1. `build.ps1`
2. `--check`
3. `VqsvBattleDamageFormulaCheck` if battle formula/runtime touched
4. mojibake scan
5. focused smoke PNG for the touched state
6. route regression if battle result/branch can be affected:
   - `route_sophie_after_battle_branch`
   - `route_bunny_after_battle_task`
   - `route_elder_after_battle_reward_state`

Report PNG paths and, when possible, show the images in the chat.

## Current Recommended Next Work

Follow the roadmap. The next roadmap-consistent target is:

### P21/P17 Catch Edge Cases

Scope:

- `game.d` state 21 and 17
- `game.h.ah()` and `game.h.ai()`
- `game.d.b(int itemId)` catch chance
- `game.d.l()/m()` tutorial hooks
- `game.g.y()` catch storage
- `choice.ui`, `msgwarm.ui`, sprite 269, H/ah catch effect

Do not start by drawing prettier effects. Start by proving source logic.

Recommended slice order:

1. Audit and document exact P21/P17 current delta:
   - create `82_battle_p21_p17_catch_edge_case_matrix.md`
   - matrix: state -> source method -> UI file -> input -> side effect -> next state
   - matrix: item/ball id -> source table row -> count behavior -> chance params
2. Fix tutorial gating:
   - prove when source auto-guides Bunny catch
   - remove or gate any rebuild auto-seed behavior that is too broad
3. Fix no-ball / missing count path:
   - warning text
   - return state
   - purchase/SMS path should remain `PARTIAL/PENDING` unless source side effect
     is fully audited
4. Tighten catch chance/storage:
   - compare `game.d.b(itemId)` with rebuild formula
   - compare `game.g.y()` bag/bank/full/release behavior
   - do not invent storage payload fields
5. Tighten P17 lifecycle:
   - q phase transitions
   - hide/restore enemy
   - sprite 269 phase timing
   - H/ah effect ordering
   - success/fail message and branch

Required catch smokes:

- `battle_bunny_catch_p21`
- `battle_bunny_catch_p17_anim_or_result`
- `battle_bunny_after_catch_route`
- `battle_catch_fail_or_warning`
- `battle_catch_storage_bank`
- `battle_catch_storage_full_release`

Regression:

- P5 smokes listed above, because command menu adjacent code is easy to break.
- P16 item smoke if touching shared menu handling.
- Route Sophie/Bunny/Elder.

## Compulsory Entry Exercise

Before coding, answer these in chat.

### Exercise 1: P5 Source Chain Proof

Prove the current P5 flow from source and rebuild:

- Which source method opens P5 UI?
- Which source method validates selected pet?
- What does `game.d.a(slot)` return for dead/current/valid pet?
- Which source state runs after a valid switch?
- Which `cpos.mid` group/row is used by current elder P5 route?
- Which rebuild methods implement this?

Expected facts:

- `game.d.a(5)` -> `S.W()`.
- `game.h.X()` calls `game.d.a(this.b)`.
- `game.d.a(slot)` returns `0`, `1`, or `-1`.
- valid switch enters source state `15`.
- current elder P5 route uses `an[0][1]` from `cpos.mid`.
- rebuild implementation is in `VqsvBattleRuntime`,
  `VqsvBattleAnimationTables`, `VqsvBattleRenderer`, and `VqsvSmokeHarness`.

### Exercise 2: P21/P17 Current Gap Matrix

Create a short matrix in chat before editing:

| Source state/method | Current rebuild method | Status | Gap |
| --- | --- | --- | --- |

Must include:

- `game.d.a(21)`
- `game.h.ah()`
- `game.h.ai()`
- `game.d.a(17)`
- `game.d.b(int itemId)`
- P17 q phase update
- `game.g.y()`

### Exercise 3: Unknowns Before Coding

List unknowns. At minimum:

- exact no-ball purchase/SMS route
- exact tutorial `U/V` gating
- exact storage payload parity
- exact H/ah catch effect timing/pixel parity

If the new chat claims any of these are done without source proof, stop it.

### Exercise 4: Smoke Plan

Write the smoke plan before code:

- focused smoke PNGs
- route regression
- build/check/formula/mojibake commands

### Exercise 5: No-Guess Rule

State explicitly:

- "I will not modify P5/P16/P7 unless P21/P17 code requires shared helpers."
- "I will not replace missing source behavior with guessed visual behavior."
- "I will mark purchase/SMS as PENDING unless side effects are fully audited."

Only after the exercise is answered satisfactorily should coding begin.

## Style / Work Skills

- Use `rg` first for file/source search.
- Prefer small source-backed slices over broad rewrites.
- Use `apply_patch` for manual edits.
- Keep UI changes paired with source logic that calls that UI.
- Add new smoke checkpoints for user-visible bugs.
- When a smoke catches a bug, keep that smoke as regression.
- When adding Vietnamese text in Java, prefer existing `VqsvText` constants and
  Unicode escapes to avoid mojibake.
- If PowerShell stdout displays mojibake but Java source scan is clean, say so;
  do not confuse console codepage with source corruption.

## Current Answer To "What Next?"

Next: P21/P17 catch edge cases.

Do not move to broad effects, EXP/level-up, shop, or generic UI runtime before
catch edge cases unless the user redirects.

The first concrete deliverable should be:

- `rebuild_plan/82_battle_p21_p17_catch_edge_case_matrix.md`
- then a small code slice chosen from that matrix
- then smoke PNG and regression
