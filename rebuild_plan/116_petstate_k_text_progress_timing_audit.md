# Petstate k.java Text/Progress Timing Audit

Date: 2026-07-08

Scope: close the focused `petstate.ui` text/progress timing slice before
returning to Battle Roadmap Phase 5.

## Source Facts

| Source | Fact | Status |
|---|---|---|
| `game.h.W()` | Opens `/data/ui/petstate.ui` through `e(c)` and resets selected row. | VERIFIED |
| `game.h.e(int)` | Fills row progress widgets `16+i*6` and `17+i*6` with `#P + percent`. | VERIFIED |
| `game.h.a(b[],int)` | Fills selected pet detail widgets `48`, `51`, `52`, `59..62`, `65..68`, `69..74`. | VERIFIED |
| `k.a(Graphics,...)` | Selected visual state draws fill/border/image using `e/f/i/g`; normal state uses `j/k/m/l`. | VERIFIED |
| `k.a(...String...)` | `#P` text draws only `fillRect(x+1,y+1,percent*w/100-1,h-1)` in the widget. | VERIFIED |
| `k.a(...String...)` | Non-wrap long text starts with `n[0] = -widgetWidth/2`, moves by `+2` each draw, then resets to `-widgetWidth`. | VERIFIED |
| `k.a(...String...)` | When text changes, both scroll offsets are reset before drawing. | VERIFIED |

## Rebuild Mapping

| Area | Rebuild change | Status |
|---|---|---|
| UI open timing | Added `Scene.battleUiModeStartTick` and set it when P5/P16/world petstate opens. | PORTED/PARTIAL |
| Text scroll | Petstate text now computes elapsed ticks from UI-open time and uses source-shaped `n[0]` horizontal timing: start `-w/2`, step `+2`, reset `-w`. | PORTED/PARTIAL |
| Text alignment | Petstate text now reads widget `b` alignment for left/center/right and vertical positioning. | PORTED/PARTIAL |
| Progress draw | Petstate HP/EXP bars now follow the `#P` inner fill shape: `x+1,y+1,width-1,height-1`. | PORTED/PARTIAL |
| Binary layout guard | Existing smoke still asserts `petstate.ui` loads from binary and has required widgets. | PORTED |

## Remaining Limits

| Limit | Why |
|---|---|
| Full `ae.a` wrap mode is not generic. | This slice only ports non-wrap one-line widget text used by petstate detail/softkey fields. |
| Exact MIDP font metrics still depend on rebuild `FontBitmap`. | Source uses Java ME `Font.getFont(0,0,8)` or custom `y` draw path. Rebuild font is close but not byte-identical. |
| Per-widget persistent text-change state is approximated by UI-open tick. | Source resets offsets per widget when text changes. Rebuild resets on petstate open; selection-change reset should be audited if we later add multi-row detail selection smoke. |
| Full `ao/al/ac/z` style runtime remains pending. | This slice improves visual/text/progress behavior but does not replace battle/world input with the original UI tree runtime. |

## Smoke Evidence

Generated PNGs:

- `rebuild_game/build_intro_demo/battle_p16_target_petstate_ui_k_timing.png`
- `rebuild_game/build_intro_demo/battle_p5_petstate_source_rows_k_timing.png`
- `rebuild_game/build_intro_demo/battle_p5_petstate_text_start.png`
- `rebuild_game/build_intro_demo/battle_p5_petstate_text_active.png`
- `rebuild_game/build_intro_demo/battle_p16_item_revive_k_timing.png`
- `rebuild_game/build_intro_demo/route_sophie_after_battle_branch_k_timing_regression.png`
- `rebuild_game/build_intro_demo/route_bunny_after_battle_task_k_timing_regression.png`
- `rebuild_game/build_intro_demo/route_elder_after_battle_reward_state_k_timing_regression.png`

Checks:

- `build.ps1`: pass.
- `com.vqsv.rebuild.Main --check`: pass.
- `VqsvBattleDamageFormulaCheck`: pass.
- Java source mojibake scan: pass.

## Roadmap Return

This closes the focused petstate visual timing slice at `PORTED/PARTIAL`.
Return to Battle Roadmap Phase 5. The next source-backed work should not be
broad UI polish; pick one remaining Phase 5 consumer:

1. P21/P17 catch remaining parity: storage full branches, warning/result flow,
   and P17 animation details.
2. P4/P16/P5 exact widget-runtime cleanup only if a specific UI mismatch is
   blocking testing.
3. SMS/free purchase behavior for shop/item flow if source P11/P4 calls it.

Recommended next slice: **P21/P17 catch edge-case audit + code**, because P5/P16
now have enough petstate coverage to stop blocking Phase 5.
