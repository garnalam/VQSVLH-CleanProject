# 84 UI Runtime Completion Audit

Scope: `/modules/ui/original/*.ui` and the rebuild renderers currently used by
scene 1 and the battle roadmap.

Rule: source-first. A UI is not COMPLETE unless its source call path, decoded
widget layout, sprite assets, text behavior, and PNG smoke are all checked.

## Active UI Status

| UI | Source path | Rebuild path | Status | Notes |
| --- | --- | --- | --- | --- |
| `taskTip.ui` | `game.h.c(String)` + `game.h.g()` | `TextBox.taskTip` | PORTED/PARTIAL | Widget 1 uses sprite 257 state 10 and text appears only after open frame reaches cursor 4. Horizontal text scroll is source-shaped. No original-vs-rebuild pixel compare yet. |
| `openbox.ui` | `game.h.b/e(String)` + `game.h.f()` | `TextBox.openBox` | PORTED/PARTIAL | Uses decoded rects `45,147,150` and text `47,154,146`. Shares source UI text scrolling helper. Needs dedicated original pixel compare. |
| `msgwarm.ui` | `game.h.a(title,prompt)` warning path | `VqsvBattleRenderer.drawWarningOverlay` | PORTED/PARTIAL | Updated to decoded widget positions and fills: background ids 1/2/3/5, frame id 8 at `76,106`, title id 7 at `85,119`, prompt id 6 near `89,170`. Prompt uses tighter line step because rebuild font is wider than source J2ME font. Generic UI manager timing remains pending. |
| `choice.ui` | `game.h.ah/ai`, `aj/ak`, petsetting calls | `drawChoiceOverlay` | PORTED/PARTIAL | P21 catch list now source-shaped for icons, rows, chance, selected count widget 52/53. Other choice.ui modes still share simplified renderer. |
| `choiceskill.ui` | `game.h.e/f(b)` | `drawChoiceSkillOverlay` | PORTED/PARTIAL | Skill rows and description are source-shaped. Full widget runtime/scroll parity pending. |
| `battle.ui` | `game.h.Z()/aa()` battle command UI | `VqsvBattleRenderer.renderSourceLikeBattleUi` | PORTED/PARTIAL | Battle HUD and command bar are source-shaped, but not pixel-perfect certified. |
| `petstate.ui` | `game.h.X/Y` pet switch/state | `drawPetStateOverlay` | PORTED/PARTIAL | Enough for P5 smokes. Full petstate widget details and petsetting integration pending. |
| `world.ui` | `game.h.c/e()` world HUD | `WorldUi` | PORTED/PARTIAL | Bottom widgets and minimap/help icon are source-backed enough for current world smokes. Full `ao/af/k/m` runtime pending. |
| `dialog.ui` | `game.h.a/b(dialog)` | `TextBox.dialog` | PORTED/PARTIAL | Dialog frame/text pages are custom source-shaped. Full decoded widget renderer not implemented. |

## Deferred UI Files

These files exist and are decoded, but are not currently part of the active
scene 1/battle smoke surface. Do not fake-complete them without source call
path and smoke checkpoints:

`answer.ui`, `badge.ui`, `bag.ui`, `bodyShop.ui`, `evolve.ui`,
`gamemenu.ui`, `gamesystem.ui`, `help.ui`, `help1.ui`, `levelUp.ui`,
`msgconfirm.ui`, `msgRecover.ui`, `msgtip.ui`, `msgyn.ui`, `npcEnemy.ui`,
`option.ui`, `petmap.ui`, `petsetting.ui`, `record.ui`, `ride.ui`,
`shop.ui`, `shopbuy.ui`, `shopsale.ui`, `skill.ui`, `smsInfo.ui`,
`smsTip.ui`, `task.ui`, `taskOption.ui`, `transmit.ui`, `wharf1.ui`,
`wharf2.ui`.

## Current Fixes In This Slice

- `taskTip.ui`: fixed source timing so text does not render over frame 0
  small opening cell.
- `choice.ui`: P21 catch list gained source-shaped row height, icons, chance
  column, and selected count widget.
- `msgwarm.ui`: warning overlay now uses decoded widget positions and
  background fill widgets instead of a hand-made battle panel.

## Remaining Work

1. Build a generic decoded `.ui` runtime for `ao/af/al/ac/k/m` instead of
   one-off renderers.
2. Add smoke checkpoints for every UI before marking it PORTED.
3. Add original-vs-rebuild pixel comparison before any pixel-perfect claim.
4. Keep SMS UI `OUT_OF_SCOPE_FOR_PC / DEFERRED` unless the PC rebuild needs a
   non-SMS replacement flow.
