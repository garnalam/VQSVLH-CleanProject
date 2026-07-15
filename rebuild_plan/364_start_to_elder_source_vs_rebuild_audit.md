# 364 - Start To Elder Source Vs Rebuild Audit

Date: 2026-07-15

## Scope

Route audited here:

1. boot/new game or rebuild skip prompt
2. full intro handoff to scene 1 room 0
3. scene 1 room 0 group 0: ten-years-later village task
4. scene 1 room 1 group 1: save prompt before Bunny field
5. scene 1 room 1 group 0: Bunny battle/catch return
6. scene 1 room 0 group 2: report to Elder
7. scene 1 room 0 group 3: starter pet choice
8. scene 1 room 0 group 6: Elder battle, reward, free-world unlock

This audit is source-first. Source evidence comes from:

- `modules/event/decoded/data__event__scene_1.mid.json`
- `modules/source_code/decoded/decompiled_source_cfr/game/e.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/k.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
- current rebuild route scripts in `rebuild_game/src/main/java`

No pixel-perfect claim is made here.

## Source Route Timeline

### Scene 1 Room 0 Group 0

Source event records:

| Record | Opcode | Args | Meaning |
| --- | --- | --- | --- |
| 0 | `op15` | `[1,3,0]` | gate against prior story event |
| 1 | `op1` | `[10,60,90]` | full-screen "ten years later" text |
| 2 | `op2` | actor/state lists | actor visibility setup |
| 3 | `op8` | `[199,218]` | set player position |
| 4/6 | `op12` | `[30]`, `[60]` | waits |
| 5 | `op51` | `[10,10,260,220,50]` | source text box for noisy village line |
| 7/9/11 | `op5` | actor effects on 36/50/36 | dialogue actor effects |
| 8..16/21 | `op4` | dialogue rows | Ali/Titan/Elder/Neil dialogue |
| 17..19 | `op17` | `[0,0,1]`, `[0,1,2]`, `[0,4,5]` | item rewards before Bunny task |
| 20 | `op39` | none | refresh/recover pets |
| 22 | `op45` | `[0]` | task notice |
| 23..25 | `op10` | action rows | scripted player movement |
| 26 | `op22` | `[1,1,55,279,240,320]` | transition to room 1 |
| 27 | `op25` | `[1]` | game flag |
| 28 | `op6` | `[1,1,37,1]` | place player/actor transition |
| 29 | `op14` | none | complete group |

Rebuild status:

- `Scene1Room0Group0Script` is `PORTED_MANUAL_WITH_APPROX`.
- Item rewards, pet refresh, movement, transition, and task notice exist.
- `op15 [1,3,0]` is not fully VM-driven in the rebuild route; skip/new-game
  uses a rebuild policy to jump to this event block.
- `op4` dialog UI now uses source-backed `/data/ui/dialog.ui` layout for this
  route. Source `game.e case 4` passes `short_args[1]` as dialog side and
  `short_args[0]` as portrait index into `game.k.a(...)`; rebuild route scripts
  now call `dialogOp4(...)` with those exact side/portrait pairs. Neil rows use
  sprite `323` portrait state `0`; Elder/Ali/Ti-Tan rows in this route have
  portrait `-1`, so source shows name tab only.
- `op45` now uses source-backed `/data/ui/taskTip.ui` layout through
  `TextBox.taskTip` plus opcode-shaped route trace (`taskFlag=0`).
  The rebuild still does not run the generic event VM field `game.e.G`; the
  flag is traced/asserted for this route.

### Scene 1 Room 1 Group 1 Save Prompt

Source records:

| Record | Opcode | Args | Meaning |
| --- | --- | --- | --- |
| 0 | `op15` | `[1,0,0]` | gate |
| 1 | `op56` | `[0,1]` | actor visibility |
| 2 | `op46` | none | save prompt |
| 3 | `op14` | none | complete group |

Source `game.e case 46`:

- Opens save UI via `game.h.K()`.
- Shows prompt text through `game.h.a(...)`.
- Confirm key sets `d.f = 1`, text becomes "Dang luu...", calls save.
- Save success sets event state complete, shows "Luu thanh cong", then closes.
- Cancel closes without saving.

Rebuild status:

- `VqsvRoom1Group1SavePromptWrapper` and `VqsvSavePromptBlocking` are
  source-shaped for the audited route.
- Follow-up fix: pointer/click input now maps source `msgtip.ui` softkeys
  correctly:
  - widget `3` at the lower right is the tick/confirm key and routes to
    source key `196640`;
  - widget `4` at the lower left is the X/cancel key and routes to source key
    `262144`;
  - pointer clicks outside these two softkeys are consumed and do not confirm
    the prompt.
- Follow-up fix: room1 group1 event completion now occurs after the save
  success state, matching `game.e case 46` where the event cell is set complete
  in `f == 1` before displaying `"Lưu thành công"`, instead of completing
  before the save call.
- Save/load and movement unstuck have smoke coverage.
- Visual uses `/data/ui/msgtip.ui` frame/text/widgets. Exact original RMS save
  backend remains `PORTED/PARTIAL` because PC rebuild uses
  `VqsvSaveRuntime`.

### Scene 1 Room 1 Group 0 Bunny

Source records:

| Record | Opcode | Args | Meaning |
| --- | --- | --- | --- |
| 0 | `op13` | `[370,176,80,32]` | source rect trigger |
| 1 | `op37` | `[34,5,1]` | Bunny encounter |
| 2 | `op52` | `[0,1]` | battle flag |
| 3 | `op66` | `[0]` | source side flag |
| 4 | `op32` | `[0,0]` | enter battle state |
| 5 | `op47` | `[12,0,0]` | result branch |
| 6 | `op4` | dialogue | Neil report |
| 7 | `op56` | `[1,1]` | Bunny actor visibility |
| 8 | `op23` | `[1,0,1]` | mark room0 group1 state |
| 9 | `op40` | task text | return-to-Elder task |
| 10 | `op14` | none | complete Bunny group |
| 11..13 | `op4/op10/op42` | return/free movement sequence |

Rebuild status:

- Bunny battle/catch is source-shaped and smoke-covered for current route.
- Catch tutorial and P17 visuals are `PORTED/PARTIAL`, not pixel-perfect.
- `op47` bridge is descriptor-backed.

### Scene 1 Room 0 Group 2 Report To Elder

Source records:

| Record | Opcode | Args | Meaning |
| --- | --- | --- | --- |
| 0 | `op86` | `[1,1,0]` | gate after Bunny group |
| 1 | `op16` | `[52]` | actor interaction with Elder |
| 2..13 | `op4/op5/op12` | dialogue/effects | report and starter-pet offer |
| 14 | `op45` | `[1]` | task notice: choose pet and fight Elder |
| 15 | `op14` | none | complete group |

Rebuild status:

- Actor interaction trigger exists.
- Dialogue/effects exist.
- `op4` dialog rows now use source-backed `dialog.ui`; Neil rows include
  sprite `323` portrait state `0`, Elder rows are name-tab only because source
  portrait index is `-1`.
- `op45` now uses source-backed `/data/ui/taskTip.ui` layout through
  `TextBox.taskTip` plus opcode-shaped route trace (`taskFlag=1`).

### Scene 1 Room 0 Group 3 Starter Pet Choice

Source records:

| Record | Opcode | Args | Meaning |
| --- | --- | --- | --- |
| 0 | `op15` | `[1,0,2]` | requires group2 complete |
| 1 | `op2` | `[3]` + actor list `53,54,55` | show three starter actors |
| 2 | `op38` | actors `53,54,55`, branch targets `4,8,12` | wait actor choice |
| 3/7/11 | `op4` | Elder descriptions | starter descriptions |
| 4/8/12 | `op35` | `[2,0]` + yes/no + branches | option.ui confirm |
| 5 | `op87` | `[0,51,7,3,2,30,45,0]` | add Penguin starter |
| 9 | `op87` | `[0,16,7,3,2,10,45,0]` | add Frog/Wood starter |
| 13 | `op87` | `[0,6,7,3,2,0,45,0]` | add Dragon/Fire starter |
| 15 | `op3` | hide starter actors | hide choice actors |
| 16 | `op9` | `[2,0,0,0,0,0]` | source visual effect |
| 17 | `op14` | none | complete group |

Source `game.e case 87`:

```text
K.a(args[7], args[1], args[2], -1, args[4], args[3], -1,
    new int[]{1, args[5], args[6]})
```

Closeout/fix in this audit:

- Fixed rebuild `OP87_ARGS` second starter from species `17` to source species
  `16`.
- Updated `route_elder_after_battle_reward_state` to use source-route starter
  species `16` instead of the generic Battle Lab fixture species `17`.

Closeout/fix after follow-up audit:

- Source `game.e case 87` calls `game.j.a(slot, species, level, ...)`, not the
  append-only overload.
- Source `game.j.a(int n2, int n3, int n4, ...)` inserts at `A[n2]` by shifting
  existing party entries right with `System.arraycopy(this.A, n2, this.A, n2 + 1, this.B - n2)`.
- Rebuild `Room0Group3PetOffer.applySourceOp87` now inserts at `args[7]`, then
  renumbers source party slots to match list order.
- Route smoke `route_starter_insert_party_order_before_elder` verifies the route
  state after initial Dien Mieu + caught Bunny + starter choice:
  party species order `[16,68,34]`, slots `[0,1,2]`, caught Bunny low HP retained,
  and Elder battle enters with species `16` as the active pet.

Remaining notes:

- `Room0Group3PetOffer` still uses simplified `ChoiceBox.optionUi`; full
  `game.h` `option.ui` runtime parity is not claimed.
- `op9 [2,0,0,0,0,0]` is handled by `VqsvSourceEffects` but remains
  source-shaped partial.
- Rebuild add-pet ordering for `op87 mode=0` is now `PORTED` for the audited
  route. Other pet add paths remain outside this route audit unless a source
  event hits them.

### Scene 1 Room 0 Group 6 Elder Battle

Source records:

| Record | Opcode | Args | Meaning |
| --- | --- | --- | --- |
| 0 | `op15` | `[1,0,3]` | requires starter choice complete |
| 1 | `op8` | `[199,218]` | set player position |
| 2 | `op7` | `[1]` + action strings | player action |
| 3 | `op9` | `[1,0,0,0,0,0]` | pre-battle visual effect |
| 4 | `op4` | Elder "take this attack" |
| 5 | `op67` | `[52]` | source NPC actor id |
| 6 | `op37` | `[68,5,1]` | Elder encounter |
| 7 | `op32` | `[0,2]` | NPC battle entry |
| 8 | `op47` | `[10,10,0]` | result branch |
| 9 | `op4` | Elder reward dialogue |
| 10 | `op31` | `[0,0,500]` | money reward |
| 11 | `op17` | `[0,4,10]` | item reward |
| 12 | `op17` | `[0,11,2]` | item reward |
| 13 | `op19` | `[5,1]` | special reward |
| 14..16 | `op4` | book/Abra/Neil dialogue |
| 17..18 | `op23` | `[1,0,4]`, `[1,0,5]` | mark events |
| 19 | `op45` | `[2]` | task notice |
| 20 | `op40` | free-world notice |
| 21 | `op14` | complete group6 |

Source NPC entry:

- `game.h.aw()` loads `/data/ui/npcEnemy.ui`.
- It uses sprite `296` and widget `1`.
- `game.h.c(mode, frame)` changes sprite cells and attaches pet sprites based
  on `game.l.E/F` / active party state.

Rebuild status:

- `BattleEventDescriptor.SCENE1_ROOM0_GROUP6_ELDER` matches op67/op37/op32/op47.
- `npcEnemy.ui` entry exists and is smoke-covered, but exact timeline/pixel
  parity is still `PORTED/PARTIAL`.
- Rewards and downstream descriptor assertions exist.
- P8 EXP/result is source-shaped for active participant, not full original
  every-party route parity.
- `op4` dialog rows now use source-backed `dialog.ui`; Neil final memory row
  includes sprite `323` portrait state `0`, Elder rows are name-tab only.
- `op45` now uses source-backed `/data/ui/taskTip.ui` layout through
  `TextBox.taskTip` plus opcode-shaped route trace (`taskFlag=2`).

## Current Difference Matrix

| Area | Status | Difference |
| --- | --- | --- |
| New-game skip prompt | `REBUILD_POLICY` | Source game does not have this PC skip prompt. It is intentional for testing/release convenience. |
| Scene0 full intro | `PORTED_MANUAL` | Not re-audited frame-by-frame in this pass. |
| Group0 gate `op15 [1,3,0]` | `PORTED/PARTIAL` | Rebuild route can jump directly to ten-years-later through policy. |
| Group0/2/3/6 dialog UI `op4` | `FIXED/PORTED/PARTIAL` | Uses source-backed `dialog.ui` frame/tab/text widgets and sprite `323` portrait state for audited route rows. Generic event VM pagination/portrait lifecycle is not fully ported globally. |
| Group0/2/6 task UI `op45` | `FIXED/PORTED/PARTIAL` | Uses source-backed `taskTip.ui` layout, widget 1 animation state 10, text widget 2, widget 3 icon cell 18, and route taskFlag traces/smokes. Generic event VM `game.e.G` mutation remains trace-only. |
| Room1 save prompt `op46` | `FIXED/PORTED/PARTIAL` | `msgtip.ui` prompt, X/tick click mapping, and save-success event completion are fixed/ported for this route. PC save backend remains rebuild partial. |
| Bunny catch tutorial | `PORTED/PARTIAL` | Current route is smoke-covered; exact P17 pixel parity pending. |
| Group3 second starter species | `FIXED` | Source is species `16`; rebuild was species `17`. |
| Group3 add-pet ordering/slot | `FIXED/PORTED` | Source inserts at slot `0`; rebuild now inserts and smoke verifies `[16,68,34]` before Elder. |
| Group3 `option.ui` | `PORTED/PARTIAL` | Yes/no logic exists; exact source widget runtime pending. |
| Group3/6 `op9` effects | `PORTED/PARTIAL` | Source effect dispatcher is broader than rebuild helper. |
| Elder `npcEnemy.ui` | `PORTED/PARTIAL` | Source UI/sprite path mapped; exact timeline/pixels not claimed. |
| Elder result/reward | `PORTED/PARTIAL` | Current route reward and op47 assertions pass; full generic event VM not present. |

## Verification Targets

After route fixes touching this path, run at minimum:

```powershell
cd rebuild_game
.\build.ps1
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint room0_pet_choice_ui build_intro_demo\start_to_elder\room0_pet_choice_ui.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint room0_group2_first_dialog build_intro_demo\start_to_elder\room0_group2_first_dialog.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint font_long_dialog build_intro_demo\start_to_elder\font_long_dialog.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint room0_group0_op45_tasktip_bunny build_intro_demo\start_to_elder\room0_group0_op45_tasktip_bunny.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint room0_group2_op45_tasktip_pet_choice build_intro_demo\start_to_elder\room0_group2_op45_tasktip_pet_choice.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint room0_group6_op45_tasktip_bich_thuy build_intro_demo\start_to_elder\room0_group6_op45_tasktip_bich_thuy.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint room1_bunny_save_prompt build_intro_demo\start_to_elder\room1_bunny_save_prompt.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint room1_bunny_save_prompt_click_tick_success build_intro_demo\start_to_elder\room1_bunny_save_prompt_click_tick_success.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint room1_bunny_save_prompt_click_x_cancel build_intro_demo\start_to_elder\room1_bunny_save_prompt_click_x_cancel.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint battle_entry_vs_elder_ui build_intro_demo\start_to_elder\battle_entry_vs_elder_ui.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint route_starter_insert_party_order_before_elder build_intro_demo\start_to_elder\route_starter_insert_party_order_before_elder.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint route_elder_after_battle_reward_state build_intro_demo\start_to_elder\route_elder_after_battle_reward_state.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-suite battle_quick build_intro_demo\battle_quick_after_start_to_elder_audit
```

Latest verification after dialog/taskTip source-backed UI patch:

- `.\build.ps1`: PASS.
- `com.vqsv.rebuild.Main --check`: PASS.
- `VqsvBattleDamageFormulaCheck`: PASS.
- Focused PNG checkpoints:
  - `room0_group2_first_dialog`: PASS, Neil uses sprite `323` portrait state `0`.
  - `font_long_dialog`: PASS, Elder source portrait arg `-1` remains name-tab only.
- `battle_quick`: PASS, `235/235`, output
  `rebuild_game/build_intro_demo/battle_quick_after_dialog_ui_rebuild`.
- Mojibake scan: no new hits from this patch; existing intentional/legacy `??`
  speaker rows remain in Scene1 room3 and text renderer handling.

## Next Recommended Step

Party-order correctness after starter choice is now settled for this route.

Next concrete slice should be UI parity polish in source order, keeping logic
small:

1. `op35 option.ui` starter confirm: tighten exact yes/no footer/layout before
   touching broader option runtime.
2. `npcEnemy.ui` Elder entry timeline: only after source route UI above is stable.
