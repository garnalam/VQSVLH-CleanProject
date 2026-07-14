# 272 - Battle Buff9 Hoa Thach Closeout

Date: 2026-07-13

Status: PORTED

Scope: close out self buff `aq.c[6][9]` / skill `45` with source-backed logic, producer visual trace, P12/P13 active queue behavior, expiry, focused PNG smoke, and regression hooks.

## Source Facts

- Buff row: `aq.c[6][9] = [342,357,3,50,50]`.
- Producer skill: `45`.
- Skill45 effect row: `effect.mid[45] = [0,1,19,0,-1,-1,0, 0,1,15,0,-1,-1,0]`.
- Source apply method: `modules/source_code/decoded/decompiled_source_cfr/game/b.java`, `game.b.a(byte by, int n2, int n3)`, case `9`.
- Source formula:
  - `v[9][1] = c[4] * aq.c[6][9][3] / 100`, so speed delta is `baseSpeed * 50 / 100`.
  - `v[9][2] = c[3] * aq.c[6][9][4] / 100`, so defense delta is `baseDefense * 50 / 100`.
  - `d[4] = c[4] + v[9][1]`.
  - `d[3] = c[3] - v[9][2]`.
- Source active tick reapplies the same stat shape before duration decrement.
- Source P12/P13 body visual gate excludes buff id `9`, so no body visual is expected after the producer.
- Status icon cell: `21`.
- Duration cells: `137 -> 136 -> 135 -> clear`.

## Rebuild Mapping

- `VqsvBattleUnit.applySourceBuff(9, value, sourceSkill)` stores:
  - `buffSlots[9][1] = baseSpeed * row.paramA / 100`.
  - `buffSlots[9][2] = baseDefense * row.paramB / 100`.
  - `currentStats[SPEED] = baseSpeed + buffSlots[9][1]`.
  - `currentStats[DEFENSE] = baseDefense - buffSlots[9][2]`.
- `VqsvBattleUnit.tickSourceBuff(9, queueSlot)` reapplies speed and defense while the buff is active, then decrements duration.
- `VqsvBattleUnit.restoreMutableStats()` plus `reapplyActiveStatEffects()` restores speed/defense after expiry.
- Smoke uses base speed and defense `100` to avoid truncation:
  - before: `speed 100`, `defense 100`.
  - active: `speed 150`, `defense 50`.
  - expired: `speed 100`, `defense 100`.

## Focused Smoke Checkpoints

Added to `BATTLE_QUICK_SUITE`:

- `battle_status_buff9_before_no_effect`
- `battle_status_buff9_producer_visual_stats`
- `battle_elder_p7_speffect45_start`
- `battle_elder_p7_speffect45_overlay`
- `battle_elder_p7_speffect45_type1`
- `battle_elder_p7_speffect45_after`
- `battle_status_buff9_p12_no_body_visual`
- `battle_status_buff9_expiry_clears_stats`

Focused PNG paths:

- `rebuild_game/build_intro_demo/buff9_closeout/battle_status_buff9_before_no_effect.png`
- `rebuild_game/build_intro_demo/buff9_closeout/battle_status_buff9_producer_visual_stats.png`
- `rebuild_game/build_intro_demo/buff9_closeout/battle_status_buff9_p12_no_body_visual.png`
- `rebuild_game/build_intro_demo/buff9_closeout/battle_status_buff9_expiry_clears_stats.png`

Focused animation timeline PNG paths:

- `rebuild_game/build_intro_demo/buff9_animation_timeline/battle_elder_p7_speffect45_start.png`
- `rebuild_game/build_intro_demo/buff9_animation_timeline/battle_elder_p7_speffect45_overlay.png`
- `rebuild_game/build_intro_demo/buff9_animation_timeline/battle_elder_p7_speffect45_type1.png`
- `rebuild_game/build_intro_demo/buff9_animation_timeline/battle_elder_p7_speffect45_after.png`

## Verified Results

Before checkpoint:

- No buff9 active.
- No status icon.
- Base/current speed: `100 -> 100`.
- Base/current defense: `100 -> 100`.

Producer checkpoint:

- Skill45 does not run damage/hitroll.
- P7 producer visual traces `effect.mid[45]` speffects `19 -> 15`.
- `game.d.q postEffect skill=45` applies buff id `9` to same side.
- Buff9 active values: `value=50`, `secondary=50`, `duration=3`.
- Stats: speed `100 -> 150`, defense `100 -> 50`.
- HUD status icon: `21/137`.

Animation timeline checkpoint:

- Start frame: P7 has entered skill45, before the visible special overlay.
- Chunk0: `speffect=19` renders AH type `9` on the player-side pet.
- Chunk1: `speffect=15` renders AH type `1` on the player-side pet.
- After frame: P7 resolves, post-effect applied, buff9 icon is visible.

P12/P13 active queue checkpoint:

- Active queue applies bank `0`, id `9`.
- No `active queue visual start bank=0 id=9`.
- No actor/body special visual.
- Duration changes `3 -> 2`.
- Stats remain speed `150`, defense `50`.
- HUD status icon: `21/136`.

Expiry checkpoint:

- Tick 1: duration `3 -> 2`, icon `21/136`, stats stay `150/50`.
- Tick 2: duration `2 -> 1`, icon `21/135`, stats stay `150/50`.
- Tick 3: duration `1 -> 0`, buff clears, icon clears, speed/defense restore to `100/100`.

## Commands Run

```cmd
cd /d E:\VQSVLH-CleanProject\rebuild_game
.\build.ps1
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint battle_status_buff9_before_no_effect build_intro_demo\buff9_closeout\battle_status_buff9_before_no_effect.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint battle_status_buff9_producer_visual_stats build_intro_demo\buff9_closeout\battle_status_buff9_producer_visual_stats.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint battle_status_buff9_p12_no_body_visual build_intro_demo\buff9_closeout\battle_status_buff9_p12_no_body_visual.png
java "-Dvqsv.modules=..\modules" -cp build\classes VqsvIntroDemo --smoke-checkpoint battle_status_buff9_expiry_clears_stats build_intro_demo\buff9_closeout\battle_status_buff9_expiry_clears_stats.png
```

## Remaining Notes

- This is not a pixel-perfect original-vs-rebuild visual claim. It is source-backed rebuild smoke coverage.
- No runtime gameplay behavior change was required; this closeout primarily added proof coverage and documentation.
- Next recommended slice: audit buff10 `Man Luc` because it is currently `PORTED-AS-SOURCE / SOURCE_ODDITY`, then decide whether to add dedicated closeout smoke or move to buff11 donor-vector work.
