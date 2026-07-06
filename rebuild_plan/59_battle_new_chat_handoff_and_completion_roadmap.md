# 59 Battle New Chat Handoff And Completion Roadmap

Status: HANDOFF / TRAINING DOC for the next dev chat.

Scope: onboard a new chat to continue the VQSV/Liet Hoa battle engine without
guessing, without touching confirmed intro/world work, and with a roadmap to
finish battle parity step by step.

## Supreme Rules

- Source first, visuals second. Do not draw UI/effects unless source logic calls them.
- No guessing. Every battle behavior must trace to source or assets:
  `game.d`, `game.b`, `game.h`, `ah`, `l`, `aq.c/db.mid`, `effect.mid`,
  `speffect.mid`, `blood.mid`, `bufDebuf.mid`, `.ui` files, sprite/assets.
- Always mark status: `PORTED`, `PORTED/PARTIAL`, `APPROX`, `STUB`,
  `PENDING`, or `UNKNOWN`.
- Do not touch intro/world/panel/scene scripts unless the current battle task
  explicitly requires it.
- Do not open the playable client unless asked. Use smoke PNG checkpoints.
- Never hide partial work. If something is source-shaped but not pixel-compared
  against MIDP original, say so.
- Do not claim pixel-perfect until there is a repeatable original-vs-rebuild
  frame comparison.

## Read First

Read in this order before coding:

1. `rebuild_plan/50_battle_full_engine_port_plan.md`
2. `rebuild_plan/52_battle_logic_asset_full_audit.md`
3. `rebuild_plan/53_battle_current_status_and_next_plan.md`
4. `rebuild_plan/54_battle_entry_snapshot_matrix.md`
5. `rebuild_plan/55_battle_p6_target_select_matrix.md`
6. `rebuild_plan/56_battle_p7_animation_effect_matrix.md`
7. `rebuild_plan/57_battle_speffect_ah_matrix.md`
8. `rebuild_plan/58_battle_speffect_type9_port.md`
9. `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
10. `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
11. `modules/source_code/decoded/decompiled_source_cfr/game/h.java`
12. `modules/source_code/decoded/decompiled_source_cfr/ah.java`
13. `modules/source_code/decoded/decompiled_source_cfr/l.java`
14. `rebuild_game/src/main/java/VqsvBattleRuntime.java`
15. `rebuild_game/src/main/java/VqsvBattleRenderer.java`
16. `rebuild_game/src/main/java/VqsvBattleAnimationTables.java`
17. `rebuild_game/src/main/java/VqsvBattleUnit.java`
18. `rebuild_game/src/main/java/VqsvBattleTables.java`
19. `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Important: `57_battle_speffect_ah_matrix.md` is an audit snapshot from before
the latest type `1` / type `9` implementation. Use
`58_battle_speffect_type9_port.md` for the current implementation truth.

## Current Truth

Current battle runtime files:

- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleRenderer.java`
- `rebuild_game/src/main/java/VqsvBattleAnimationTables.java`
- `rebuild_game/src/main/java/VqsvBattleTables.java`
- `rebuild_game/src/main/java/VqsvBattleUnit.java`
- `rebuild_game/src/main/java/VqsvBattleScripts.java`
- `rebuild_game/src/main/java/VqsvSmokeHarness.java`

Current status matrix:

| Area | Status | Truth |
| --- | --- | --- |
| Story battle entry | PORTED/PARTIAL | Sophie, Bunny, elder use source encounter ids and branch targets. |
| Command `P20` | PORTED/PARTIAL | Routes to fight/catch/item/pet/shop/run; full disabled/status rules remain partial. |
| Skill list `P3` | PORTED/PARTIAL | `choiceskill.ui`-shaped list, PP labels, desc, confirm/back/no-PP warning. Generic `game.h` widget runtime is not full. |
| Target select `P6` | PORTED/PARTIAL | Uses `aq.c[1][skill][9]` target side and source-shaped target list. Multi-target/full formations remain partial. |
| Catch `P21/P17` | PORTED/PARTIAL | Ball list, consume, chance, basic result route, storage/bank/full behavior. Pixel-perfect animation still partial. |
| Damage formula | PORTED/PARTIAL | `VqsvBattleUnit.computeDamage` ports much of `game.b.b(target)`. Full buff/debuff tick/runtime is not complete. |
| P7 direct animation | PORTED/PARTIAL | Normal `effect.mid` actor animation rows drive basic animation state for direct damage skills. |
| P7 AH type `9` | PORTED/PARTIAL | Skill `45` chunk0 / `speffect 19` blinking color overlay. Source-shaped, not pixel-compared. |
| P7 AH type `1` | PORTED/PARTIAL | Skill `45` chunk1 / `speffect 15` texture composite via `tex_3`. Source-shaped, not pixel-compared. |
| P7 multi-chunk sequence | PORTED/PARTIAL | Current row advances through chunks before damage/no-damage exit. Exact `H.i()` and `chunk[4]/[5]/[6]` hooks remain partial. |
| Skill `45` no-damage | PORTED | `aq.c[1][45][3] == 0`; skill `45` runs effects and does not apply fake damage/text. |
| EXP/level-up/learn skill | PENDING | `P8/P22/P23` still need source-backed implementation. |
| Item/pet switch/shop/run | PORTED/PARTIAL | Shell logic/UI exists; full item behaviors and restrictions remain partial. |
| Enemy AI/pre-turn status | PORTED/PARTIAL | Basic dispatch exists; full `P1/P12/P13/P2` parity remains pending. |

Latest P7 smoke images:

- `rebuild_game/build_intro_demo/battle_elder_p7_speffect45_start.png`
- `rebuild_game/build_intro_demo/battle_elder_p7_speffect45_overlay.png`
- `rebuild_game/build_intro_demo/battle_elder_p7_speffect45_type1.png`
- `rebuild_game/build_intro_demo/battle_elder_p7_speffect45_after.png`

Required regression checks:

```text
build.ps1
com.vqsv.rebuild.Main --check
VqsvBattleDamageFormulaCheck
Java source mojibake scan
route_sophie_after_battle_branch
route_bunny_after_battle_task
route_elder_after_battle_reward_state
```

## Compulsory Entry Exercise

The next chat must answer this in chat before coding.

### Exercise 1: Prove skill 45 P7 chain

Expected answer:

- `aq.c[1][45] = [4,162,574,0,1,10,1,9,-1,1]`.
- `aq.c[1][45][3] == 0`, so skill `45` is no-damage after effects.
- `effect.mid[45] = [0,1,19,0,-1,-1,0, 0,1,15,0,-1,-1,0]`.
- Chunk0: `chunk[0] == 0` target side, `chunk[1] == 1`, `chunk[2] == 19`.
- `speffect.mid[19] = [9,120,255,255,255,0,6,2]`, AH type `9`.
- Chunk1: `chunk[0] == 0` target side, `chunk[1] == 1`, `chunk[2] == 15`.
- `speffect.mid[15] = [1,0,5,3,0,0]`, AH type `1`.
- `game.d.n()` prepends runtime actor data:
  `[type, actorX, actorY, actorSpriteId, actorAnimState, actorOrientation, ...tail]`.
- Current rebuild should render both chunks, then exit without damage.

### Exercise 2: Explain remaining PARTIALs honestly

Expected answer must include:

- No pixel-compare against MIDP original frames yet.
- `effect.mid chunk[4]/[5]/[6]` trigger hooks are not fully ported.
- Exact `H.i()` start/visibility conditions are not fully ported.
- AH types `7,8,11,12,13,14,15` are not generally ported for P7.
- P17 catch type `8`-shaped behavior exists but remains not pixel-perfect.
- Full buff/debuff ticking, item behavior, pet switch restrictions, EXP/level-up,
  learn-skill, and enemy AI are still partial/pending.

### Exercise 3: Candidate matrix for next P7 slice

Pick one candidate and justify it from source rows.

Good candidates:

- Skill `11`: normal actor animation chunk plus attacker-side AH type `9`.
- Skill `17`: same effect shape as skill `11`, but requires route/seed support.
- Skill `68`: multi-side/multi-chunk target-side `speffect 0` and attacker-side
  `speffect 15`.
- Skill `55`: AH type `12`, useful after type `9`/`1` are stable.

Required matrix fields:

```text
skill id
aq.c[1] row
effect.mid row
speffect row(s)
AH type(s)
attacker/target side from chunk[0]
expected damage/no-damage from aq.c[1][skill][3]
smoke checkpoint names
```

### Exercise 4: Smoke plan

Expected answer must be PNG-only:

```text
new focused P7 start frame
new focused P7 special/effect frame
new focused P7 damage/no-damage after frame
route_sophie_after_battle_branch
route_bunny_after_battle_task
route_elder_after_battle_reward_state
```

### Exercise 5: Safety statement

Expected answer:

- Files to edit.
- Files not to touch.
- What remains `PARTIAL/PENDING` before implementation.

## Recommended Roadmap To Finish Battle Engine

### Step A: P7 chunk trigger hooks

Create `rebuild_plan/60_battle_p7_chunk_trigger_hooks_matrix.md`.

Goal:

- Audit and port `effect.mid chunk[4]/[5]/[6]` trigger hooks.
- Audit source conditions around `H.i()`, `H.e()`, `M`, `N`, `L`, `I`, `J`, `K`.

Required source:

- `game.d case 7`
- `game.d.n()`
- `ah.e()`
- actor animation helpers called through `u.a(frame)`, `u.i()`, `u.d()`,
  `u.a()`, `u.b()`

Exit criteria:

- Matrix explains each hook:
  - `chunk[4]`: trigger next effect chunk.
  - `chunk[5]`: trigger actor state change.
  - `chunk[6]`: actor state applied by `d(...)`.
- Skill `45` smoke still passes.
- Any unported hook is explicitly marked `PARTIAL/PENDING`.

### Step B: Attacker-side AH type 9

Recommended first skill: `11`.

Expected rows:

```text
aq.c[1][11] = [1,128,540,90,0,45,0,-1,10,0]
effect.mid[11] = [0,0,21,1,-1,-1,0, 1,1,10,0,-1,-1,0]
speffect.mid[10] = [9,120,218,217,169,0,4,2]
```

Why:

- It tests `chunk[0] == 1` attacker-side special effects.
- It keeps the next slice small and visible.

Smoke:

- `battle_elder_p7_skill11_start.png`
- `battle_elder_p7_skill11_attacker_type9.png`
- `battle_elder_p7_skill11_damage.png`

### Step C: More AH types

Port one renderer family at a time:

- AH type `12`: candidate skill `55`, `speffect 12`.
- AH type `15`: candidate rows `28..33`, after sequencing is safer.
- AH types `7,8,11,13,14`: audit first, then one focused smoke each.

Exit criteria:

- Matrix doc for each type.
- Focused PNG smoke.
- Route regression still passes.

### Step D: Turn/state parity

Finish `P1/P12/P13/P2`:

- pre-turn status/effects
- status skip
- enemy skill selection
- player/enemy order
- dead pet handling
- action result dispatch

Add smoke for:

- player-first turn
- enemy-first turn
- status-skip turn

### Step E: Buff/debuff runtime

Finish lifecycle:

- apply
- duration decrement
- stat restore
- resist/block
- floating status text

Source:

- `game.b`
- `game.d P12/P13/P7`
- `bufDebuf.mid`
- `aq.c[6]` buff rows
- `aq.c[7]` debuff rows

### Step F: Items, pet switch, run, shop

Finish `P4/P16/P5/P10/P11`:

- item behavior table from `aq.c[4]`
- bag count mutations
- disabled states
- pet switch constraints
- run chance and no-run warnings
- source UI screens where proven

### Step G: Win/lose, EXP, level-up, learn skill

Finish `P8/P9/P22/P23/P24`:

- EXP formula
- level-up stat changes
- `levelUp.ui`
- learn skill `choiceskill.ui`
- reward/save/event side effects
- branch return to event/world

### Step H: Pixel compare workflow

Create repeatable original-vs-rebuild capture and comparison.

Compare at least:

- battle HUD idle
- P3 skill UI
- P6 target UI
- P7 skill `45` type9/type1 frames
- one direct-damage skill
- one catch result

Until this exists, do not claim pixel-perfect.

## Suggested Prompt For The New Chat

```text
Ban dang tiep quan battle engine rebuild VQSV/Liet Hoa.

Luat toi cao:
- Source first, khong doan.
- Khong dung intro/world/panel neu khong lien quan battle.
- Khong mo client, chi smoke PNG.
- Moi thu phai phan loai PORTED / PORTED/PARTIAL / APPROX / STUB / PENDING / UNKNOWN.
- Lam xong phai build/check/formula/mojibake/smoke regression.

Truoc khi code, doc:
1. rebuild_plan/59_battle_new_chat_handoff_and_completion_roadmap.md
2. rebuild_plan/58_battle_speffect_type9_port.md
3. rebuild_plan/57_battle_speffect_ah_matrix.md
4. rebuild_plan/56_battle_p7_animation_effect_matrix.md
5. rebuild_plan/55_battle_p6_target_select_matrix.md
6. rebuild_plan/54_battle_entry_snapshot_matrix.md
7. rebuild_plan/53_battle_current_status_and_next_plan.md
8. rebuild_plan/50_battle_full_engine_port_plan.md
9. modules/source_code/decoded/decompiled_source_cfr/game/d.java
10. modules/source_code/decoded/decompiled_source_cfr/game/b.java
11. modules/source_code/decoded/decompiled_source_cfr/ah.java
12. modules/source_code/decoded/decompiled_source_cfr/l.java
13. rebuild_game/src/main/java/VqsvBattleRuntime.java
14. rebuild_game/src/main/java/VqsvBattleRenderer.java
15. rebuild_game/src/main/java/VqsvBattleUnit.java
16. rebuild_game/src/main/java/VqsvSmokeHarness.java

Sau khi doc xong, chua duoc code. Hay tra loi Compulsory Entry Exercise trong
rebuild_plan/59_battle_new_chat_handoff_and_completion_roadmap.md.

Current target sau khi bai tap dat:
Step A: tao audit `60_battle_p7_chunk_trigger_hooks_matrix.md`, roi port nho
`effect.mid chunk[4]/[5]/[6]` trigger hooks neu chung minh du tu source.
```

## Current Best Next Target

Do Step A first: `60_battle_p7_chunk_trigger_hooks_matrix.md`.

Reason:

- It directly addresses the current remaining PARTIAL from skill `45`.
- It is safer than jumping into every AH type or EXP/level-up.
- It reduces risk before porting skills with more complex chunk timing.
