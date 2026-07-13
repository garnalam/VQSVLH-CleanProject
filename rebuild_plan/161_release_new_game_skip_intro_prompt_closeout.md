# 161 Release New Game Skip Intro Prompt Closeout

Status date: 2026-07-09

Status: RELEASE UX FEATURE COMPLETE / REBUILD_POLICY / PNG SMOKE COVERED.

Purpose:

- Add a release-menu convenience prompt when the player chooses `Chơi mới`.
- If the player chooses `Có`, skip the long intro and begin at the source-backed
  `scene1_room0_group0` ten-years-later event: `Mười năm sau...`.
- If the player chooses `Không`, start the original rebuild intro path as before.

## Classification

| Area | Status | Note |
| --- | --- | --- |
| Prompt after `Chơi mới` | `REBUILD_POLICY` | New convenience behavior requested for release testing. |
| Prompt visual surface | `REBUILD_POLICY` | Uses the same black-screen prompt style as the music prompt, not the title-menu background. |
| Skip target | `PORTED/PARTIAL` | Replays the source transition target `scene1 room0 center=[199,218]`, then uses registered `VqsvIntroDemo.Scene.tenYearsEventIndex`, not a hardcoded index. |
| Original intro path | `UNCHANGED` | Choosing `Không` constructs the normal `LegacyIntroDemoState(false, false)`. |
| `Chơi tiếp` save path | `UNCHANGED` | Continue still uses `LegacyIntroDemoState(true)`. |
| Full original boot/menu parity | `PENDING` | This prompt is not claimed as original-game behavior. |

## Files Changed

- `rebuild_game/src/main/java/com/vqsv/rebuild/state/BootFlowState.java`
- `rebuild_game/src/main/java/com/vqsv/rebuild/state/LegacyIntroDemoState.java`
- `rebuild_game/src/main/java/com/vqsv/rebuild/state/GameStateMachine.java`
- `rebuild_game/src/main/java/com/vqsv/rebuild/state/BootFlowSmokeCheck.java`
- `rebuild_game/src/main/java/VqsvIntroDemo.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

## Smoke PNG

- `rebuild_game/build/smoke_skip_intro/boot_new_game_skip_intro_prompt.png`
- `rebuild_game/build/smoke_skip_intro/boot_new_game_skip_intro_yes.png`
- `rebuild_game/build/smoke_skip_intro/boot_new_game_skip_intro_noise.png`
- `rebuild_game/build/smoke_skip_intro/boot_title_continue_with_save.png`

## Verification

Required checks:

```powershell
cd rebuild_game
powershell -ExecutionPolicy Bypass -File .\build.ps1
java "-Dvqsv.modules=..\modules" -jar build\libs\vqsv-liet-hoa-rebuild.jar --check
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvBattleDamageFormulaCheck
```

Focused smoke:

```powershell
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint boot_new_game_skip_intro_prompt build\smoke_skip_intro\boot_new_game_skip_intro_prompt.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint boot_new_game_skip_intro_yes build\smoke_skip_intro\boot_new_game_skip_intro_yes.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint boot_new_game_skip_intro_noise build\smoke_skip_intro\boot_new_game_skip_intro_noise.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint boot_title_continue_with_save build\smoke_skip_intro\boot_title_continue_with_save.png
```

## Next Step

Recommended next:

```text
Let the user test the release menu path. If accepted, return to the battle
engine roadmap; do not expand boot/menu work unless a concrete mismatch appears.
```
