# 366 - New Dev Chat Handoff: World Panel UI Routes

Date: 2026-07-15

Purpose: hand off the current world softkey and panel UI work to a new dev
chat. This handoff overrides any older generic panel direction. It does not
override the battle engine roadmap; it only says where the panel/world UI
polish stands and what the next small source-backed slice should be.

## Prime Rules

- Source first. Read source and UI data before changing behavior.
- Do not guess UI route behavior from screenshots alone. Screenshots can point
  to a mismatch, but source decides the route.
- Do not open the live client unless the user explicitly asks. Use PNG smoke
  checkpoints by default.
- Keep every claim classified: `PORTED`, `FIXED`, `PORTED/PARTIAL`, `APPROX`,
  `STUB`, `PENDING`, `UNKNOWN`, or `PC_QOL`.
- Do not claim pixel-perfect parity without original-vs-rebuild pixel compare.
- End every slice with build/check/smoke/regression status and a concrete next
  recommended step.
- Keep changes small. For panel work, prefer one route branch per slice.
- Do not hardcode absolute paths in scripts or docs. Use repo-relative commands
  and the existing `-Dvqsv.modules=..\modules` pattern from `rebuild_game`.
- Respect the dirty worktree. Do not revert unrelated asset/S60/battle changes.

## Must Read First

Read these before coding:

1. `rebuild_plan/365_world_softkey_panel_source_audit.md`
2. `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
3. `modules/ui/decoded/data__ui__world.ui.json`
4. `modules/ui/decoded/data__ui__gamemenu.ui.json`
5. `modules/ui/decoded/data__ui__gamesystem.ui.json`
6. `modules/ui/decoded/data__ui__record.ui.json`
7. `modules/ui/decoded/data__ui__petmap.ui.json`
8. `rebuild_game/src/main/java/VqsvWorldActors.java`
9. `rebuild_game/src/main/java/VqsvPanelRuntime.java`
10. `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Load extra files only when the next slice needs them. For example, if working
on badge UI, then read:

- `modules/ui/decoded/data__ui__badge.ui.json`
- the relevant `game.k.java` `W()` / `X()` badge branch methods

## Current Source Mapping

`game.k.c()` opens `/data/ui/world.ui`.

`world.ui`:

| Widget | Cell | Role |
| --- | --- | --- |
| `7` | `175` | bottom-left system softkey |
| `5` | `68` | bottom-right game menu softkey |

Source route:

- Left softkey -> `game.k.m()` -> `/data/ui/gamesystem.ui`.
- Right softkey -> `game.k.k()` -> `/data/ui/gamemenu.ui`.

Important: the two map corner buttons are not both inventory buttons.

- Left button is system menu: continue/help/settings/main-menu confirm.
- Right button is game menu: pet/bag/record-task-save style gameplay menu.

## Current Implementation Status

Files touched by this panel/world work:

- `rebuild_game/src/main/java/VqsvWorldActors.java`
- `rebuild_game/src/main/java/VqsvPanelRuntime.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`
- `rebuild_plan/365_world_softkey_panel_source_audit.md`

Status matrix:

| Area | Status | Notes |
| --- | --- | --- |
| `world.ui` corner icons | `PORTED` | Source widgets `7/5`, cells `175/68`. |
| Left softkey click -> `gamesystem.ui` | `FIXED/PORTED/PARTIAL` | Includes source hitbox and PC-friendly bottom-corner hitbox. |
| Right softkey click -> `gamemenu.ui` | `FIXED/PORTED/PARTIAL` | Includes source hitbox and PC-friendly bottom-corner hitbox. |
| `gamesystem.ui` continue/help/settings/option-no | `PORTED/PARTIAL` | Open/navigate/back verified. Confirm-yes reset is not the next target unless asked. |
| `gamemenu.ui` pet -> `petstate.ui` | `PORTED/PARTIAL` | Opens current source pet state. Deeper petsetting work exists elsewhere. |
| `gamemenu.ui` bag -> `bag.ui` | `PORTED/PARTIAL` | Opens list, hover preview, wheel scroll, back. Item mutation coverage exists in earlier slices. |
| `gamemenu.ui` task -> `task.ui` | `PORTED/PARTIAL` | Fixed title/status overlap. Exact source marquee/crop is still not full. |
| `gamemenu.ui` record -> `record.ui` | `FIXED/PORTED/PARTIAL` | Fixed double-cursor visual issue in lower options. |
| `record.ui` c=0 -> `petmap.ui` | `PORTED/PARTIAL` | Open/navigate/tab/wheel/back verified. Entry details pending. |
| `record.ui` c=1 -> `badge.ui` | `PORTED/PARTIAL` | Open/render/navigation/back is done; per-badge `q.C` state is still proxied from `sourceBadges`. |
| Save row -> panel save prompt | `PORTED/PARTIAL` | Opens prompt and completes save flow through rebuild save runtime. |

## Current Visual Caveats

- `record.ui` lower option buttons are source-positioned and logic-shaped, but
  not pixel-perfect source-client cell playback. The source selected cells
  embed cursor art in a way that made both cursors visible in rebuild, so the
  current renderer draws clean source-positioned buttons and only one cursor.
- `task.ui` status text is clipped instead of using full source marquee/crop
  behavior.
- `option.ui` may show source-shaped cursor behavior that looks odd. Do not
  change it without reading source and proving the source hides or shows the
  relevant widget.
- Generic source UI VM/input engine is not fully ported. This route work is
  concrete branch-by-branch, not a full generic VM.

## Verified Outputs

Latest verified commands from `rebuild_game`:

```powershell
.\build.ps1
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-suite world_panel_full build_intro_demo\world_panel_full_final
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-suite panel_wheel build_intro_demo\panel_wheel_final
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-suite battle_quick build_intro_demo\battle_quick_after_world_panel_final
java "-Dvqsv.modules=..\modules" -cp build\classes com.vqsv.rebuild.Main --check
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvBattleDamageFormulaCheck
```

Latest result:

- build: PASS.
- `world_panel_full`: PASS, `42/42`.
- `panel_wheel`: PASS, `8/8`.
- `battle_quick`: PASS, `235/235`.
- release check: PASS.
- battle damage formula check: PASS.
- mojibake scan for common Java-source mojibake sequences: PASS.

Key PNG outputs to inspect:

- `rebuild_game/build_intro_demo/world_panel_full_final/world_softkey_icons_render.png`
- `rebuild_game/build_intro_demo/world_panel_full_final/panel_gamemenu_open_from_world.png`
- `rebuild_game/build_intro_demo/world_panel_full_final/panel_gamesystem_click_softkey_open.png`
- `rebuild_game/build_intro_demo/world_panel_full_final/panel_petmap_record_open_from_gamemenu.png`
- `rebuild_game/build_intro_demo/world_panel_full_final/panel_petmap_open_from_record.png`
- `rebuild_game/build_intro_demo/world_panel_full_final/panel_save_prompt_from_gamemenu.png`

## Next Concrete Slice

Recommended next slice: `task.ui` confirm -> `taskOption.ui` open/render/back.

Why this slice:

- Source branch exists in `game.k.java`: task page confirm opens task detail /
  option-style UI.
- Rebuild currently opens task list/tab/back but task row confirm still traces
  pending.
- It is a small UI route branch and does not require battle logic.

Expected work:

1. Audit source methods around `game.k.U()`, `V()`, and task option/detail
   helpers.
2. Read `taskOption.ui` layout.
3. Create a small audit doc.
4. Implement only open/render/navigate/back first.
5. Add PNG smoke checkpoints:
   - `panel_task_option_open_from_task`
   - `panel_task_option_navigation`
   - `panel_task_option_back_returns_task`
   - `panel_task_option_task_back_returns_gamemenu`
6. Run verification:
   - build
   - `world_panel_full` or a new focused `world_badge` suite
   - `panel_wheel` if any list/scroll input changes
   - `battle_quick`
   - `--check`
   - `VqsvBattleDamageFormulaCheck`
   - mojibake scan

Do not implement badge mutation/reward logic unless source proves it belongs to
this route slice.

## Alternative Next Slice

If the user asks to stay in story route order instead of panel completion:

1. `op35 option.ui` starter confirm polish.
2. `npcEnemy.ui` Elder battle entry timeline.

Do not return to P7/skill animation work unless the user asks or a source-route
mismatch requires it.

## Compulsory Entry Exercise

Before coding, answer these in chat:

1. Prove the world softkey source chain:
   - `world.ui` widgets and cells.
   - left route method and UI.
   - right route method and UI.
2. Explain why `record.ui` c=1 is the next smallest panel route.
3. List the exact files you will read for badge UI and why.
4. Write a PNG-only smoke plan for badge open/render/back.
5. State what you will not touch:
   - battle P7/skills
   - save mutation beyond existing flow
   - generic full UI VM
   - unrelated S60 assets

If the exercise cannot be answered from source, stop and audit more before
coding.

## Reporting Format For The Next Dev Chat

When finished with a slice, report:

- `Changed:` concise file/function summary.
- `Source proof:` exact source methods/widgets used.
- `Status:` `PORTED`, `FIXED`, `PORTED/PARTIAL`, or `PENDING`.
- `Smoke PNG:` output folder and checkpoints.
- `Regression:` exact pass/fail list.
- `Remaining debt:` honest caveats.
- `Next:` one concrete recommended slice.
