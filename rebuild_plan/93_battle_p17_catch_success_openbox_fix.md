# 93 Battle P17 Catch Success Openbox Fix

## Scope

Fix the missing runtime success notification after P17 catch success.

## Source Chain

- `game.d` P17 tick, `q == 3 && aj.b()`:
  - calls `game.d.p.y()` for storage destination;
  - bag path sets `S.f = 1`, calls `S.b("Bắt thành công #2" + petName)`, then stores with `game.g.a(...)`;
  - bank path sets `S.f = 2`, calls the same success openbox, then stores with `game.g.b(...)`;
  - full path sets `S.f = 1`, calls `S.b("Không còn không gian, sủng vật này đã phóng sinh")`.
- `game.h.b(String)` loads `/data/ui/openbox.ui` with sprite `257` and pushes text into source UI runtime.
- While `S.f != 0`, P17 waits for `S.ax()` before exiting. Bank path opens a second message:
  - `S.f == 2` -> `S.b("Sủng vật ba lô đã đủ, đã để vào ngân hàng")`, then `S.f = 4`;
  - `S.f == 4 || S.f == 1` exits battle via `game.i.a().a((byte)10)`.

## Rebuild Change

- `SourceBattleRuntime.tickCatchResult()` now keeps P17 active after success and opens `TextBox.openBox(...)`.
- Catch storage now records storage result:
  - `0`: bag, one success openbox.
  - `1`: bank, success openbox then bank notice openbox.
  - `2`: full, release openbox.
- Runtime confirms the openbox with `key0` before advancing to P8/exit.
- Smoke harness storage checkpoints now wait for the runtime openbox instead of manually creating `TextBox.openBox(...)` after battle completion.

## Status

| Area | Status | Note |
| --- | --- | --- |
| P17 success openbox source call | PORTED/PARTIAL | Source call order and text are ported; full `game.h` UI runtime is still partial. |
| Bag storage notice | PORTED/PARTIAL | Runtime opens `/data/ui/openbox.ui` style `TextBox.openBox`. |
| Bank two-step notice | PORTED/PARTIAL | Success openbox then bank notice openbox. |
| Full storage release notice | PORTED/PARTIAL | Runtime opens full/release notice. |
| Pixel-perfect UI | PENDING | No original-vs-rebuild pixel compare yet. |

## Verification

- `build.ps1`: pass.
- `--check`: pass.
- `VqsvBattleDamageFormulaCheck`: pass.
- `rg -n "Ã|Â|�" rebuild_game/src/main/java`: no Java source hits.
- `git diff --check`: pass.
- PNG smoke:
  - `battle_catch_storage_bag`
  - `battle_catch_storage_bank`
  - `battle_catch_storage_full_release`
  - `battle_bunny_retry_p21_item0`
  - `battle_bunny_catch_p17_anim_or_result`
  - `route_bunny_after_battle_task`
  - `route_sophie_after_battle_branch`
  - `route_elder_after_battle_reward_state`
