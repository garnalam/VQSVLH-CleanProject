# 192 - Panel bag itemId 13 avoid-monster pill closeout

## Scope

This slice audits and ports the smallest source-backed top-level `bag.ui` item-use branch after the generic item `0..3` warning:

- `game.h.Y()` opens `/data/ui/bag.ui`, sets `b=0`, refreshes list via `bi()`, and writes widget `14 = "Vat pham"`.
- `game.h.ac()` handles `bag.ui` input.
- In tab `b == 0`, source selects rows from `q.K + q.J`.
- `case 13` is the "tranh quai hoan" / avoid-monster pill branch.

No item 14 egg hatch and no default `P=17` target-item flow were ported in this slice.

## Source facts

Source file:

- `modules/source_code/decoded/decompiled_source_cfr/game/h.java`

Relevant branch:

```java
case 13: {
    if (this.f != 0) break;
    if (this.q.x <= 0) {
        if (game.k.a().f == 3 && game.k.a().g == 7) {
            this.E();
            this.a("Noi nay khong cach nao su dung tranh quai hoan", "Nhan nut 5 de tiep tuc");
            this.f = 1;
            break;
        }
        if (!this.q.b(v1[0], 1, (byte)0)) break;
        this.q.d(v1[0], 1, (byte)0);
        this.q.x = aq.c[4][v1[0]][6];
        this.q.w = 0;
        this.bk();
        this.E();
        this.q.c(1);
        this.a("Thanh cong su dung dao cu, cung co thoi gian ngan tranh quai hieu qua", "Nhan nut 5 de tiep tuc");
        this.f = 1;
        break;
    }
    this.E();
    this.a("Da co duoc thoi gian ngan tranh quai hieu qua", "Nhan nut 5 de tiep tuc");
    this.f = 1;
    break;
}
```

Source state mapping:

| Source | Rebuild | Status |
| --- | --- | --- |
| `q.x` active avoid-monster duration | `Scene.sourceAvoidMonsterTicks` | PORTED/PARTIAL |
| `q.w` elapsed/counter reset | `Scene.sourceAvoidMonsterElapsed` | PORTED/PARTIAL |
| `aq.c[4][13][6]` duration | `BattleItemRow.paramA` | PORTED |
| `q.b(item,1,0)` enough item | `VqsvSourceOps.sourceCanRemoveItem(s,13,1)` | PORTED/PARTIAL |
| `q.d(item,1,0)` consume item | `VqsvSourceOps.sourceRemoveItem(s,13,1)` | PORTED/PARTIAL |
| `game.k.a().f == 3 && game.k.a().g == 7` forbidden location | `currentSceneId == 3 && currentRoomIndex == 7` | PORTED/PARTIAL |
| `this.E(); this.a(...); this.f = 1` | `TextBox.msgWarm(...); bagMessageMode != 0` | PORTED |
| `this.bk()` list refresh / cursor clamp | `bagRows(s)` refresh + selected clamp | PORTED/PARTIAL |
| `q.c(1)` side-effect | trace note only | PENDING |
| Runtime decrement/expiry of `q.x/q.w` during world encounter logic | not ported | PENDING |

## Rebuild changes

Files touched:

- `rebuild_game/src/main/java/VqsvPanelRuntime.java`
  - Added top-level `bag.ui` `itemId == 13` branch.
  - Handles success, already-active, and forbidden-room warning loops.
  - Keeps `bag.ui` open after `msgwarm.ui` closes, matching source `f=1 -> f=0`.

- `rebuild_game/src/main/java/VqsvText.java`
  - Added exact source warning/success strings for item 13.

- `rebuild_game/src/main/java/VqsvSourceOps.java`
  - `sourceItem(13)` now reads metadata from `VqsvBattleTables.instance().item(13)` instead of falling back to `"Item 13"`.

- `rebuild_game/src/main/java/VqsvIntroDemo.java`
  - Added `sourceAvoidMonsterTicks` and `sourceAvoidMonsterElapsed`.

- `rebuild_game/src/main/java/VqsvSaveRuntime.java`
  - Persists and restores the avoid-monster state.

- `rebuild_game/src/main/java/VqsvSmokeHarness.java`
  - Added focused PNG checkpoints for all source-proven item 13 branches.

## Smoke checkpoints

Focused item 13 PNG smoke:

- `panel_bag_item13_success_msg`
- `panel_bag_item13_success_returns_bag`
- `panel_bag_item13_already_warning`
- `panel_bag_item13_forbidden_warning`

Regression smoke run:

- `panel_bag_open_from_gamemenu`
- `panel_bag_item_cannot_use_warning`
- `panel_bag_item_cannot_use_returns_bag`
- `panel_petstate_petsetting_active_switch_success`
- `panel_petstate_petsetting_active_dead_warning`
- `panel_petstate_petsetting_active_already_warning`
- `panel_petstate_petsetting_evolve_open`
- `panel_petstate_petsetting_release_success_removes_pet`
- `panel_petstate_petsetting_item_choice_success_msg`
- `panel_petstate_petsetting_equipment_choice_equip_success_msg`
- `panel_petstate_petsetting_skill_open`
- `panel_save_prompt_from_gamemenu`
- `route_sophie_after_battle_branch`
- `route_bunny_after_battle_task`
- `route_elder_after_battle_reward_state`

PNG output:

- `rebuild_game/build/smoke/panel_bag_item13/`
- `rebuild_game/build/smoke/panel_bag_item13_regression/`

## Verification

Passed:

- `rebuild_game/build.ps1`
- `java "-Dvqsv.modules=..\modules" -jar build\libs\vqsv-liet-hoa-rebuild.jar --check`
- `java "-Dvqsv.modules=..\modules" -cp build\classes VqsvBattleDamageFormulaCheck`
- `rg -n "Ã|Â|�" rebuild_game/src/main/java`
- `git diff --check`
- focused PNG smoke listed above
- regression PNG smoke listed above

Note: `git diff --check` only reported existing CRLF conversion warnings from Git, no whitespace error.

## Current status

`bag.ui itemId 13`: PORTED/PARTIAL

Honest remaining gaps:

- `q.c(1)` is still trace-only; exact downstream side effect is not yet audited.
- `q.x/q.w` active duration is stored and saved, but global encounter/timer decrement parity is still pending.
- Forbidden-room mapping uses rebuild `currentSceneId/currentRoomIndex`; exact `game.k.a().f/g` parity across every source map is not globally proven.

## Recommended next

Next smallest source-backed panel branch:

1. Audit `bag.ui` itemId 14 egg-hatch branch from `game.h.ac()`.
2. Only port if source state for `game.k.q`, `q.I`, egg availability, and warning/success messages can be proven.
3. If item 14 is too broad, audit default `bag.ui` branch that sets `s = itemId`, `o.a((byte)17)`, and closes `bag.ui`, but do not mutate until `P=17` source target flow is clear.
