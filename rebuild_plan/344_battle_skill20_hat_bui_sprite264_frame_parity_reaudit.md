# 344 - Battle Skill 20 Hất Bụi Sprite264 Frame Parity Re-audit

Date: 2026-07-14

Status: HẤT BỤI VISUAL BUG CONFIRMED / SOURCE MAPPING PORTED / FRAME PRESENTATION WRONG-PARTIAL.

This document corrects the previous conclusion in
`343_battle_skill_animation_source_mapping_reaudit.md`.

## User Visual Ground Truth

User correction:

```text
Hất bụi animation is a dust wave, not falling rock chunks.
```

Treat this as the current visual target until an original-client frame capture is
available. Do not claim Hất bụi visual parity from rebuild smoke.

## Source Mapping Facts

Skill row:

```text
aq.c[1][20] = [2,137,549,100,0,45,0,-1,-1,0]
```

Meaning:

- skill id `20`;
- name row `137`: Hất bụi;
- element/class `2`: Earth;
- power `100`;
- PP `45`;
- direct damage;
- no buff/debuff/post-effect.

Animation row:

```text
effect.mid[20] = [0,0,22,0,-1,-1,0]
```

Meaning:

- target-side actor action;
- actor id `22`;
- actor state `0`;
- no chunk trigger;
- no special `H` chunk.

Actor mapping in source `ah.java`:

```text
actor id 22 -> sprite index 264
```

Sprite table:

```text
sprite.mid[264] = [264,305]
```

So source-backed mapping is:

```text
Hất bụi -> u22/state0 -> sprite264 -> img305
```

Status: PORTED for data route only.

## Sprite264 Frame Evidence

Generated contact sheets:

```text
rebuild_game/build/smoke/sprite264_audit/sprite264_states.png
rebuild_game/build/smoke/asset_audit/img260_310_contact.png
```

`sprite264` decoded runtime metadata:

```text
states=1 cells=4 frames=2
state 0 row=[2,0, 2,1, 1,2, 1,3]
```

Cells:

| Cursor | Cell | Visual in rebuild contact sheet | Current problem |
|---:|---:|---|---|
| 0 | 0 | large rock/debris high above target | visually wrong for Hất bụi if original starts as dust wave |
| 1 | 1 | smaller falling debris | visually wrong/too prominent |
| 2 | 2 | beige dust-wave burst plus small debris | closest to user-described original |
| 3 | 3 | debris spread left/right | trailing debris |

Important: `img305` contains both rock chunks and a dust-wave burst. The bug is
not simply "wrong image loaded"; it is that rebuild currently presents the rock
cells too prominently/too early for Hất bụi.

## Current Rebuild Failure

Current smoke:

```text
java -cp build\classes VqsvIntroDemo --smoke-suite battle_skill20_hat_bui_source_stage_animation build\smoke\skill20_hat_bui_reaudit_source_stage
```

It proves:

```text
actorEffect=22 sprite=264 state=0 side=enemy
```

But the visible frame strip shows falling rock chunks before the dust-wave cell.

Classification:

- `effect.mid` route: PORTED.
- `ah.java` actor id mapping: PORTED.
- sprite/image load: PORTED/PARTIAL.
- visual result: WRONG/PARTIAL.
- pixel-perfect: PENDING.

## Suspected Source-Equivalent Gaps

These are not yet patches; they are the next audit targets.

| Suspect | Why it could cause the bug | Status |
|---|---|---|
| First visible actor cursor | If original calls `d.d()` before first draw or gates `u.a()` differently, cursor 0/1 may be skipped or shortened. | PENDING |
| Actor tick speed | Rebuild may make cursor 0/1 visible too long compared with MIDP. | PENDING |
| Actor placement/anchor | Rocks are high above target because cell 0 has y offsets around `-123`; source placement may differ in battle actor context. | PENDING |
| Direction/orientation | Transform maps match source arrays for basic orientation, but exact side placement still needs frame compare. | PARTIAL |
| Missing H/special layer | `effect.mid[20]` has no H chunk, so not likely for Hất bụi itself. | UNLIKELY |
| Wrong skill id | Source row confirms Hất bụi is skill 20. | CLOSED |

## Required Next Patch Plan

Do **not** patch all skills. Do **not** fake per-skill visuals.

Next safe slice:

1. Create a focused smoke frame strip for skill20 that captures every visible
   actor cursor with labels:
   `cursor0`, `cursor1`, `cursor2 dust-wave`, `cursor3`.
2. Add trace for P7 actor `u`:
   tick number, cursor before tick, cursor after tick, visible flag, draw cursor.
3. Compare source `game.d`/`game.b.o()`/`ah.e()`/`d.d()` order:
   update-before-draw vs draw-before-update.
4. If source proves first visible cursor should be later than 0, patch shared
   actor timing.
5. If source does not prove it but original capture/user evidence remains clear,
   mark a small `GAMEPLAY_VISUAL_FIX` for skill20 only after explicit approval.

## Current User-Facing Truth

Hất bụi is not done. The current rebuild has the correct source row and actor
sprite id, but it is showing the wrong visible presentation: falling rocks are
too prominent and the dust-wave look is not matching the original.

