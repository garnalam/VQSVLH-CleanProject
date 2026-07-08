# 122 Battle P8/P22/P23 EXP UI Closeout Matrix

Status: PORTED/PARTIAL + CHECKPOINTS PASS.

Purpose:

- Close the post-battle EXP/level-up consumer UI path after Slice A/B/C.
- Scope is only P8/P22/P23:
  - P8 EXP bar/display queue.
  - P22 level-up overlay.
  - P23 learn-skill choiceskill flow.
- No live client/game. PNG/headless only.

Source files:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/ui/decoded/data__ui__levelUp.ui.json`
- `modules/ui/decoded/data__ui__choiceskill.ui.json`

Rebuild files:

- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleRenderer.java`
- `rebuild_game/src/main/java/VqsvBattleLevelUpView.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

## Source State Matrix

| State | Source entry/update/draw | Source behavior | Rebuild status |
| --- | --- | --- | --- |
| P8 | `game.d case 8 -> S.am()` | Animate EXP with `J += 8`; draw `al[0]`; draw current `game.d.j[i]` pet. | PORTED/PARTIAL |
| P8 confirm | `game.h.am()` + `o.k(196640)` | Fast-forward current EXP display to current EXP or threshold. | PORTED in this closeout |
| P8 next queue | `++i`, skip level-50 pets, exit if `i >= j.size()` | Iterate every `game.d.j` entry. | PORTED by Slice A |
| P22 entry | `game.d case 22 -> S.an()` | Capture old stats, call `b.v()`, open `levelUp.ui`. | PORTED/PARTIAL |
| P22 update | `game.d case 22 -> S.ao()` | Wait 40 ticks or confirm, then P23/P8/exit. | PORTED/PARTIAL |
| P23 entry | `game.d case 23 -> S.ap()` | Open `choiceskill.ui`, close `levelUp.ui`, populate candidates. | PORTED/PARTIAL |
| P23 update | `game.d case 23 -> S.aq()` | Up/down candidate list, confirm opens `msgwarm.ui`, confirm learns skill and returns P8/exit. | PORTED/PARTIAL |

## Closeout Fix

Source `game.h.am()` handles confirm while EXP is still animating:

```java
if (this.o.k(196640)) {
    if (i4 >= i3) { ... state 22 ... }
    if (i2 >= i4) { ... next j ... }
    this.J = 0;
    i2 = i4;
    v1.j(i2);
    ...
    return;
}
```

Rebuild now consumes `s.key0` during P8 EXP animation and fast-forwards
`expDisplayValue` to the current source target for that pet, preserving later
branching to P22 or next `game.d.j`.

## Checkpoints

| Checkpoint | Purpose | Status |
| --- | --- | --- |
| `battle_exp_p8_confirm_fast_forward` | Confirm during P8 EXP fill jumps to target, source-shaped `game.h.am()`. | PENDING |
| `battle_exp_levelup_ui` | P8 -> P22 `levelUp.ui` overlay. | EXISTING |
| `battle_exp_levelup_choiceskill_ui` | P22 confirm -> P23 `choiceskill.ui`. | EXISTING |
| `battle_exp_levelup_learn_skill_done` | P23 confirm learns skill and persists payload. | EXISTING |
| `battle_exp_vector_j_iterates_second_pet` | Multi-`game.d.j` queue still advances after first pet. | EXISTING |

Checkpoint result:

| Checkpoint | Status | PNG |
| --- | --- | --- |
| `battle_exp_p8_confirm_fast_forward` | PASS | `rebuild_game/build_intro_demo/closeout_battle_exp_p8_confirm_fast_forward.png` |
| `battle_exp_levelup_ui` | PASS | `rebuild_game/build_intro_demo/closeout_battle_exp_levelup_ui.png` |
| `battle_exp_levelup_choiceskill_ui` | PASS | `rebuild_game/build_intro_demo/closeout_battle_exp_levelup_choiceskill_ui.png` |
| `battle_exp_levelup_learn_skill_done` | PASS | `rebuild_game/build_intro_demo/closeout_battle_exp_levelup_learn_skill_done.png` |
| `battle_exp_vector_j_iterates_second_pet` | PASS | `rebuild_game/build_intro_demo/sliceA_battle_exp_vector_j_iterates_second_pet.png` |

Regression run:

- `build.ps1`: PASS.
- Java mojibake scan: PASS.
- `com.vqsv.rebuild.Main --check`: PASS.
- `VqsvBattleDamageFormulaCheck`: PASS.
- Slice A multi-`game.d.j`: PASS.
- Slice B global/share EXP: PASS.
- Slice C `game.d.X()` passive heal: PASS.

## Remaining Partial

| Item | Status | Reason |
| --- | --- | --- |
| Full generic `levelUp.ui` runtime | PARTIAL | Renderer is source-shaped/manual, not full widget VM. |
| Full generic `choiceskill.ui` runtime | PARTIAL | Shared choiceskill renderer is source-shaped/manual. |
| `game.b.u(...)` pre-heal `Q` display after passive heal | PENDING | Not consumed by current P8/P22/P23 UI. |
| MIDP pixel compare | USER-COMPARE | User will manually compare when needed. |
