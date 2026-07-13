# 224 Battle P8 EXP pos.mid Marker Placement Audit

Status: SOURCE AUDIT + HUD CORRECTION.

Scope:

- Decode `game.d.am[0]` from `/data/script/pos.mid`.
- Map `game.h.a(am[0][4], am[0][5])` and `al[0].b(am[0][6], am[0][7])`.
- Correct rebuild normal P8 EXP display so it updates the existing
  `battle.ui` player HUD EXP bar, matching `game.h.am()`, instead of drawing a
  separate floating panel.

Out of scope:

- Original-client pixel-perfect compare for P8.
- Full generic `game.h` UI widget runtime.

## Source Facts

Decoded file:

- `modules/script/decoded/data__script__pos.mid.json`
- `game.d.d()` loads it into static `game.d.am`.

`am[0]` is:

```text
[177,103,144,85,70,223,36,206]
```

Battle state 8 entry in `game.d`:

```text
this.al[0].b(am[0][6], am[0][7]);
this.S.a((int)am[0][4], (int)am[0][5]);
```

Therefore for source group 0 P8:

| Field | Meaning | Value |
| --- | --- | --- |
| `am[0][0..1]` | enemy actor anchor | `177,103` |
| `am[0][2..3]` | enemy marker anchor | `144,85` |
| `am[0][4..5]` | player/P8 EXP pet anchor passed to `game.h.a` | `70,223` |
| `am[0][6..7]` | player/P8 marker anchor passed to `al[0].b` | `36,206` |

`game.h.a(int,int)`:

- selects current `game.d.j[this.i]` EXP recipient;
- skips max-level pets;
- stores `H/I`;
- calls `pet.c()` and `pet.b(i1, i2)`;
- resets hold counter `x = 0`.

`game.h.am()`:

- increments visual EXP by `J += 8`;
- updates battle UI widgets `40`, `9`, `12`, `13`, `17`;
- enters state 22 only if the displayed EXP reaches the next-level threshold.

Source state 8 draw path draws the marker and current EXP pet, then UI manager
draws the existing battle UI. It does not open `/data/ui/levelUp.ui`; that
belongs to state 22.

## Rebuild Mapping

Existing rebuild already reads original `script/original/pos.mid` through
`VqsvBattleAnimationTables.posRow(group)`.

This slice removes the incorrect compact normal-EXP overlay. When
`battleUiMode == "levelup"` and `VqsvBattleLevelUpView.leveled == false`,
`drawBattleHudWidgets()` now overrides the player HUD fields with the P8 EXP
view:

| Source widget | Source update in `game.h.am()` | Rebuild display |
| --- | --- | --- |
| `12` | pet name | `view.name` |
| `13` | `"lv" + level` | `view.level` |
| `17` | `94 + element` | `view.elementId` |
| `9` | EXP percent/sprite bar | `view.expPercent` |
| `40` | `displayExp + "/" + threshold` | `view.expValue + "/" + view.expMax` |

The `pos.mid` row is still audited and smoke-asserted for the P8 pet/marker
anchor. It is not used as a panel top-left because source does not open a
separate normal-EXP panel.

Why still not pixel-perfect:

- Rebuild updates the source-shaped `battle.ui` HUD widgets directly, but it is
  still not a full generic `game.h` widget runtime.
- No original-client P8 frame capture has been compared pixel-by-pixel.

## Smoke

`battle_exp_normal_gain_no_levelup_anim` now also asserts:

```text
sourceCposGroup == 0
game.h.a(am[0][4],am[0][5]) == 70,223
al[0].b(am[0][6],am[0][7]) == 36,206
```

This locks the source row mapping for the Elder-style normal EXP route.

## Status

| Item | Status |
| --- | --- |
| Decode `am[0]` | PORTED |
| P8 actor anchor mapping | PORTED |
| P8 marker anchor mapping | PORTED |
| Normal EXP HUD bar/text on `battle.ui` | PORTED/PARTIAL |
| Exact original-client pixel parity | PENDING |

## Next

If P8 visual polish continues, capture original-client P8 frames for a normal
non-level-up win and compare against rebuild PNGs. Only after that should this
be called pixel-perfect.
