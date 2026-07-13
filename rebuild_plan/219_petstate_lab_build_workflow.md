# 219 - Petstate Lab Build Workflow

Status: WORKFLOW / PRE-CODE PLAN.

Purpose:

- Before touching a large feature cluster such as `petstate.ui`, prepare a
  separate test/lab path that contains all currently ported systems and lets us
  jump directly into important battle/panel states.
- Avoid wasting time replaying from intro/world every time.
- Avoid mixing experimental test shortcuts into the official player route.

## Rule

Large feature work must have two lanes:

| Lane | Purpose | Can contain shortcuts? | Used for final release? |
| --- | --- | --- | --- |
| Main/release lane | Normal game flow, source-backed route progression. | No. | Yes. |
| Lab/test lane | Fast manual testing of state combinations. | Yes, but clearly gated behind lab entrypoints. | No direct release unless changes are promoted after smoke/regression. |

No lab shortcut should silently alter the normal player route.

## Branch Policy

Recommended branch for this cluster:

```text
codex/petstate-runtime-lab
```

This branch can add:

- lab launcher scripts;
- `--play-checkpoint` or future `--play-lab petstate` entrypoints;
- curated smoke outputs;
- temporary state setup helpers used only by smoke/lab.

When a slice is proven:

1. Keep runtime/source-backed code.
2. Remove or keep lab helpers only if they are gated behind explicit debug/test
   entrypoints.
3. Run full verification.
4. Promote the runtime change into the official build path.

## Petstate Lab Scenarios

The first lab build should cover all current behavior we need to inspect before
and after `petstate.ui` runtime work.

| Scenario | What it proves | Suggested checkpoint / route |
| --- | --- | --- |
| P5 normal switch open | Command -> pet switch opens `petstate.ui`. | `battle_p5_petstate_switch_open` or existing P5 equivalent. |
| P5 active already warning | Selecting active pet warns and returns correctly. | `battle_p5_already_active_warning`. |
| P5 dead warning | Selecting dead reserve pet warns correctly. | `battle_p5_dead_warning`. |
| P5 forced switch after active KO | Active pet dies; battle opens P5 instead of ending if reserves live. | `battle_elder_switched_bunny_ko_forced_p5_no_exp`. |
| P9 all dead | All player pets dead -> P9 lose, no EXP. | `battle_elder_all_player_pets_ko_p9_no_exp`. |
| P16 item target open | P4 item -> P16 target petstate. | `battle_p16_item_target_open` or item checkpoint. |
| P16 item success | Item mutates selected pet HP/PP/debuff state. | `battle_p16_item_heal`, `battle_p16_item_clear_debuff`, shop buy/use checkpoints. |
| P8 win EXP | Killing enemy grants EXP only to valid participants. | `battle_exp_levelup_ui`, route Elder reward checks. |
| P22/P23 level/learn | Level-up UI and learn-skill flow. | existing EXP/learn-skill smoke checkpoints. |
| Panel petstate | World/panel petstate navigation and petsetting submenus. | `panel_petstate_*` checkpoints. |

If a suggested checkpoint does not exist yet, create it as a smoke/lab-only
checkpoint before touching renderer/runtime code.

## Lab Build Entrypoint Recommendation

Start with the existing proven mechanism:

```text
java -Dvqsv.modules=..\modules -cp .\build\classes VqsvIntroDemo --play-checkpoint <checkpoint>
```

Add a convenience script later:

```text
rebuild_game/run_petstate_lab.ps1 -Scenario p5_forced_switch
```

The script should only map scenario names to existing `--play-checkpoint`
values. It should not implement gameplay logic.

Future optional improvement:

```text
VqsvIntroDemo --play-lab petstate
```

This can show a debug-only selector screen, but only after smoke checkpoints are
stable. Do not build a lab menu before the state checkpoints are trustworthy.

## Required Verification Before Promoting To Main Build

For every petstate runtime slice:

```powershell
cd rebuild_game
.\build.ps1
java "-Dvqsv.modules=..\modules" -cp .\build\classes com.vqsv.rebuild.Main --check
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvBattleDamageFormulaCheck
rg -n "�|Ã|á»|áº|Ä|Æ" src/main/java
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-suite battle_quick .\build_intro_demo\battle_quick_petstate_lab
```

Focused petstate smoke must include:

- P5 normal switch.
- P5 forced switch after KO.
- P5 active/dead warnings.
- P16 item target open/back/success.
- P8 EXP/level-up regression.
- Sophie/Bunny/Elder route regressions through `battle_quick`.

## What Not To Do

- Do not add permanent item/pet/EXP shortcuts into normal new-game or continue
  flow.
- Do not use fake pet stats in the renderer. Lab setup may seed states, but the
  UI must read the same source-shaped pet model as the runtime.
- Do not mark `petstate.ui` pixel-perfect without original-vs-rebuild capture.
- Do not promote lab-only helper behavior as source parity.

## Immediate Next Step

Before code:

1. Audit existing petstate-related checkpoints in `VqsvSmokeHarness`.
2. Identify which lab scenarios already exist.
3. Add only the missing smoke/lab checkpoints.
4. Then start `petstate.ui` runtime parity from the source widget/layout matrix.
