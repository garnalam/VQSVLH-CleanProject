# 106 UI Single-Line Marquee Overflow Policy

Status: IMPLEMENTED FOR CURRENT REBUILD UI HOTSPOTS.

Rule from current visual QA:

- UI widgets with narrow text slots must not auto-wrap long text into stacked
  lines.
- If text is wider than its widget, keep it on one clipped line and scroll it
  from right to left.
- Dialog/cutscene paging is not part of this policy; those source paths
  intentionally paginate/wrap text.

## Implemented Areas

| Area | File | Behavior |
| --- | --- | --- |
| `openbox` / `tasktip` source UI text | `VqsvTextRenderer.java` | Long text now uses one-line marquee; old split helper removed. |
| evolution world notice | `VqsvTextRenderer.java`, `VqsvIntroDemo.java` | Uses same source UI marquee behavior; no stacked text. |
| battle choice/item description panel | `VqsvBattleRenderer.java` | Description text now uses `drawMarqueeTinyBattleText`. |
| battle skill description panel | `VqsvBattleRenderer.java` | Description text now uses `drawMarqueeTinyBattleText`. |
| battle warning title/prompt | `VqsvBattleRenderer.java` | Warning text now uses `drawMarqueeTinyBattleText`. |

## Verification PNGs

- `rebuild_game/build/smoke/world_evolution_notice_after_levelup.png`
- `rebuild_game/build/smoke/battle_elder_p3_skill_list_marquee_regression.png`
- `rebuild_game/build/smoke/battle_elder_item_p4_marquee_regression.png`
- `rebuild_game/build/smoke/battle_p16_item_hp_full_warning_marquee_regression.png`
- `rebuild_game/build/smoke/battle_catch_missing_count_warning_marquee_regression.png`

## Pending

- Exact MIDP marquee timing is still `PARTIAL`; current rebuild uses
  source-shaped clipped one-line scrolling, not a pixel/tick compare.

