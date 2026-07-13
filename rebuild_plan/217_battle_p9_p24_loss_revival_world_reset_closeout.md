# 217 - battle P9/P24 loss revival/world reset closeout

## Scope
- Request: audit/polish `P9` lose, revive state `24`, and post-loss world reset.
- Source-first targets:
  - `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
  - `modules/source_code/decoded/decompiled_source_cfr/game/h.java`

## Source facts
- `game.d.a(byte 9)` is the battle lose entry.
- If `game.k.a().M.i` is true, source enters battle state `24`.
- If `game.k.a().M.i` is false, source loops all party pets:
  - `game.d.p.z[i].l(1)`
  - `game.d.p.z[i].u(1)`
  - `game.d.p.z[i].c()`
  - then calls `game.i.a().a((byte)10)`.
- After either lose branch, source sets:
  - `game.k.a().M.l = 1`
  - `game.k.a().M.i = true`
- `game.d.a(byte 24)` calls `game.h.aE()`.
- `game.h.aE()` opens `/data/ui/smsInfo.ui` and shows:
  - "Có dùng 10000 kim tiền để khôi phục trạng thái của tất cả sủng vật trong ba lô không?"
  - "Tại chỗ sống lại"
- `game.h.aF()` confirm with enough money:
  - checks `q.t(10000)`
  - deducts `q.s(-10000)`
  - loops all pets with `I()` and `u(maxHp)`
  - returns battle state `0`
- Source not-enough-money is two-step: first confirm opens `/data/ui/msgwarm.ui`
  and sets `game.h.f = 1`; next confirm resets all party pets to 1 HP/PP
  and routes toward SMS purchase state `102`.
- `game.h.aF()` not-enough-money path shows "Kim tiền chưa đủ"; original then routes toward SMS purchase state `102`.

## Rebuild changes
- Added source-shaped Scene fields:
  - `sourceBattleLoseReviveArmed`
  - `sourceBattleLoseWorldMode`
- Added battle state:
  - `P24_LOSE_REVIVE(24, "P24")`
- `tickLose()` now:
  - routes to P24 if `sourceBattleLoseReviveArmed` is already true;
  - otherwise persists current active pet, applies post-loss reset to every source pet, sets `sourceBattleLoseWorldMode=1`, arms `sourceBattleLoseReviveArmed=true`, and exits through the existing battle result bridge.
- Added `SourcePetState.sourceLossResetOneHpOnePp()`:
  - HP payload becomes `1`;
  - known skill PP becomes `1`;
  - active/turn flags clear.
- Added `SourcePetState.sourceReviveFull()`:
  - HP payload restores to source max HP;
  - known skill PP restores to source max PP;
  - active/turn flags clear.
- Added P24 paid revive path:
  - if `sourceMoney >= 10000`, deduct 10000, revive all pets, and return battle state `P0`.
- P24 UI:
  - uses a source-backed minimal `/data/ui/smsInfo.ui` renderer;
  - widget `8` shows the revive message;
  - widget `5` shows "Tại chỗ sống lại";
  - widgets `10/11` show confirm/back labels.
  - prompt no longer instantiates `TextBox.msgWarm`, so the old `msgwarm.ui` box no longer overdraws `smsInfo.ui`.
- PC rebuild policy:
  - SMS purchase is removed.
  - Not-enough-money P24 path is `PORTED/PARTIAL`: it shows the source warning text, traces that source would enter P102, then falls back to post-loss world reset instead of opening SMS.
  - Current PC behavior preserves the source `f=1` warning phase first; the fallback world reset happens on the next confirm.
- Smoke harness was split slightly:
  - route-end battle checkpoints moved to `runBattleRouteCheckpointInExistingScene(...)` to keep `runSmokeCheckpoint(...)` under Java method-size limits.

## Smoke evidence
- Focused:
  - `battle_p9_first_loss_world_reset_one_hp`
  - `battle_elder_all_player_pets_ko_p9_no_exp`
  - `battle_p24_revive_prompt`
  - `battle_p24_insufficient_money_warning`
  - `battle_p24_revive_pay_full_restore`
- Regression:
  - `--smoke-suite battle_quick` passed 14/14.

## Verification commands
```powershell
cd rebuild_game
.\build.ps1
java "-Dvqsv.modules=..\modules" -cp .\build\classes com.vqsv.rebuild.Main --check
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvBattleDamageFormulaCheck
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-checkpoint battle_p9_first_loss_world_reset_one_hp .\build_intro_demo\battle_p9_first_loss_world_reset_one_hp.png
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-checkpoint battle_p24_revive_prompt .\build_intro_demo\battle_p24_revive_prompt.png
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-checkpoint battle_p24_insufficient_money_warning .\build_intro_demo\battle_p24_insufficient_money_warning.png
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-checkpoint battle_p24_revive_pay_full_restore .\build_intro_demo\battle_p24_revive_pay_full_restore.png
java "-Dvqsv.modules=..\modules" -cp .\build\classes VqsvIntroDemo --smoke-suite battle_quick .\build_intro_demo\battle_quick_p9_p24_regression
```

## Classification
- P9 all-player-loss no EXP: PORTED for current routes.
- P9 first-loss world reset to 1 HP/PP: PORTED/PARTIAL.
- P9 `M.l=1`, `M.i=true` source-state equivalent: PORTED/PARTIAL.
- P24 paid revive all pets and deduct 10000: PORTED/PARTIAL.
- P24 not-enough-money warning and `f=1` phase: PORTED/PARTIAL; SMS/P102 is intentionally removed, so second confirm falls back to post-loss world reset.
- P24 `/data/ui/smsInfo.ui` renderer: PORTED/PARTIAL. Current rebuild reads the source UI layout and draws the proven message/action/softkey widgets, but full `ao/af/k/m` runtime is not claimed.
- P24 SMS/P102 purchase path: intentionally not ported in PC rebuild; trace marks `PC_SMS_REMOVED`.
- Exact `game.h.bv()` map/coordinate restoration variants: PENDING/PARTIAL. Current rebuild preserves existing route exit/branch bridge and records source reset flags.
- Pixel-perfect claim: PENDING.

## Next
- Recommended next battle slice: audit the remaining P9/P24 UI visual debt only if original-client screenshots are available for `/data/ui/smsInfo.ui`.
- Otherwise move forward to Phase 9 broad skill coverage matrix/current selected skill slice, keeping `battle_p9_first_loss_world_reset_one_hp`, `battle_p24_insufficient_money_warning`, `battle_p24_revive_pay_full_restore`, and `battle_quick` in regression.
