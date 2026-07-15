# 342 - Battle P7 Actor Sprite Timing + Draw Order Matrix

Date: 2026-07-14

Status: AUDIT COMPLETE / SHARED DRAW-ORDER PATCH APPLIED / VISUAL PARTIAL.

This audit follows `340_new_dev_chat_handoff_skill_animation_reopened.md` and
`341_battle_skill_animation_current_roadmap.md`. It supersedes any older claim
that Fire skill visuals are closed. Skill logic can be PORTED while visual
animation remains PARTIAL.

No client was opened for this audit.

## Entry Exercise

### 1. Fire skills 0/1/3/6/7/9 first actor chunk

`modules/script/decoded/data__script__effect.mid.json` proves the first chunk:

| Skill | effect.mid first chunk | Status |
|---:|---|---|
| 0 | `[0,0,20,0,-1,-1,0]` | PORTED source row |
| 1 | `[0,0,20,0,-1,-1,0]` | PORTED source row |
| 3 | `[0,0,20,0,-1,-1,0]` | PORTED source row |
| 6 | `[0,0,20,0,-1,-1,0]` | PORTED source row |
| 7 | `[0,0,20,0,-1,-1,0]` | PORTED source row |
| 9 | `[0,0,20,0,-1,-1,0]` | PORTED source row |

Conclusion: these Fire skills really share the same first actor action in
source data: owner side `0`, actor effect branch `0`, source actor id `20`,
state `0`, no chunk frame trigger.

### 2. Source actor id 20 mapping

Source `modules/source_code/decoded/decompiled_source_cfr/ah.java` maps actor
ids through:

```java
private int[] x = new int[]{262, 263, 264, 265, 266, 267, 268, 299, 300, 301, 304, 306, 307, 308, 309};
...
this.a.a(this.x[this.u - 20], false);
```

So source actor id `20` maps to sprite `262`.

Rebuild maps the same id in
`rebuild_game/src/main/java/VqsvBattleRuntime.java`:

```java
private static final int[] SOURCE_AH_ACTOR_SPRITES = {
        262, 263, 264, 265, 266, 267, 268, 299, 300, 301, 304, 306, 307, 308, 309
};
```

Status: PORTED for id-to-sprite mapping.

### 3. Why same u20/state0 does not prove the whole skill visual is complete

Same source chunk only proves the first actor action. The live presentation also
depends on:

- P7 chunk scheduling in `game.d.n()`.
- frame trigger columns `[4]`, `[5]`, `[6]`;
- attacker base state `1`, target hit state `2`, recover/idle reset;
- `game.b.a(Graphics)` draw order around actor `u`;
- special `H`/AH effects for multi-chunk skills;
- body status effects in P12/P13 after the skill;
- damage text and HP tween timing;
- Java ME sprite delay/reset behavior in source `d.java`.

Therefore Fire skills 0/1/3/6/7/9 can share `u20/state0` and still be VISUAL
PARTIAL if the shared actor pipeline is wrong.

### 4. Source draw order vs rebuild draw order

Source P7 draw in `game.d.b(Graphics)`:

1. `this.a(graphics, false)` draws battle background/layers.
2. If `H != null`, draw special AH effect `H`.
3. `this.a(graphics)` loops battle actors and calls `game.b.a(Graphics)`.
4. `this.c(graphics)` draws floating text.

Source actor draw in `game.b.a(Graphics)`:

1. optional species/effect `L` before actor when `Z == 0`;
2. base pet sprite if visible;
3. optional `L` after actor when `Z == 1`;
4. actor action `u.a(graphics)`.

Current rebuild `VqsvBattleRenderer.renderSourceLikeBattleUi()`:

1. battle background and ground markers;
2. global `L` before;
3. enemy base sprite;
4. player base sprite;
5. global `L` after;
6. `drawP7ActorEffect()`;
7. death effect;
8. `drawP7SpecialEffect()`;
9. UI and text.

Status: PARTIAL. The source draws actor `u` inside the actor's own draw call,
immediately after that actor's base sprite. Rebuild draws the actor `u` after
both base actors. For target-side enemy effects, source order is effectively
`enemy base -> enemy u -> player base`, while rebuild is
`enemy base -> player base -> enemy u`. That is a proven draw-order mismatch.

### 5. Latest fixed bug

The previous fix in `drawP7ActorEffect()` removed a double-applied battle motion
offset. The old anchor effectively did:

```text
sourceBattleActorX/Y + sideOffset + playerOffset/enemyOffset
```

Base actor drawing already includes motion offsets separately. Actor effects
should be anchored to the same source actor origin as the base actor:

```text
sourceBattleActorX/Y + sideOffset
```

Status: PORTED/PARTIAL. Anchor bug is fixed, but draw order and exact timing
remain open.

### 6. Remaining PARTIALs

| Area | Status | Reason |
|---|---|---|
| Actor `u` id mapping | PORTED | `20 -> 262` and table `20..34` match source. |
| Actor `u` draw order | PARTIAL | Rebuild draws `u` after both base actors, source draws `u` inside the owning actor draw. |
| Special `H` draw order | PARTIAL | Source draws `H` before actor list; rebuild currently draws P7 special after actor/death layers. |
| Sprite delay/reset parity | PARTIAL | Rebuild `tickHoldLast()` holds last frame; source `d.d()` can reset, hold, or loop depending `p`. Actor lifecycle may clear before visible divergence, but this is not pixel-proven. |
| Target hit/recover timing | PARTIAL | Source has P7 branches that set target state `2` around actor/special completion; rebuild also sets state `2` on damage apply, but the source-equivalent timing needs a focused patch/check. |
| AH type 1/7/8/9/12 pixel parity | PARTIAL | Source-shaped, not original-vs-rebuild pixel-perfect. |
| Fire visual closeout | REOPENED/PARTIAL | First chunks are source-backed; full live visual is not closed. |

### 7. Smoke PNGs to run after a patch

Run headless only:

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-suite battle_fire_live_frame_strip build\smoke\fire_live_frame_strip_after_p7_actor_draw_order
java -cp build\classes VqsvIntroDemo --smoke-suite battle_fire_source_stage_animation build\smoke\fire_source_stage_after_p7_actor_draw_order
java -cp build\classes VqsvIntroDemo --smoke-suite battle_quick build\smoke\battle_quick_after_p7_actor_draw_order
java -cp build\classes com.vqsv.rebuild.Main --check
java -cp build\classes VqsvBattleDamageFormulaCheck
rg -n "Ãƒ|Ã‚|HÃ¡Â»|Ã„" rebuild_game\src\main\java
git diff --check
```

Why these:

- `battle_fire_live_frame_strip`: visually checks repeated Fire rows and side
  crops after the shared renderer change.
- `battle_fire_source_stage_animation`: checks actor start/mid/damage/settle
  state frames.
- `battle_quick`: protects broader battle route smoke.
- `--check`, formula, mojibake, diff check: regression guardrails.

## Source/Rebuild Matrix

| Source method/file | Meaning | Rebuild equivalent | Current status | Evidence | Gap |
|---|---|---|---|---|---|
| `game.d.n()` | Reads `effect.mid` in 7-byte chunks; `chunk[1] == 1` creates special `H`, otherwise calls actor `u` on target or attacker. | `SourceBattleRuntime.enterP7SourceChunk()` and `prepareP7SpecialEffect()`. | PORTED/PARTIAL | Source `game.d.java:429..466`; rebuild `VqsvBattleRuntime.java:4752..4779`. | Needs draw-order patch and exact trigger parity checks. |
| `game.d` P7 entry case | Calls `n()`, computes hit flags, sets attacker base state `1` if first chunk owner side is target (`chunk[0] == 0`), else idle state `0`. | `prepareP7SourceState()` and initial `setP7BaseState()`. | PORTED/PARTIAL | Source `game.d.java:812..851`; rebuild `VqsvBattleRuntime.java:4720..4762`. | Initial state is source-shaped; later target state timing still partial. |
| `game.d` P7 update case actor branch | Starts actor `u`, checks last frame, frame trigger `[4]`, state trigger `[5]/[6]`, then advances chunks. Target-side branch may set target state `2` after actor completion / next H chunk. | `tickP7SourceEffectSequence()` and `tickP7ActorAnimation()`. | PORTED/PARTIAL | Source `game.d.java:1355..1442`; rebuild `VqsvBattleRuntime.java:4821..4897`. | Rebuild has triggers but target hit state timing needs closer source parity. |
| `game.d` P7 update case special `H` | Starts H after gates, hides affected actor, restores after H completes, then continues or exits. | `tickP7SpecialEffect()` with `setP7BaseHidden()`. | PORTED/PARTIAL | Source `game.d.java:1443..1482`; rebuild `VqsvBattleRuntime.java:4900..4947`. | H draw layer is mismatched: source draws H before actors; rebuild draws special after actor/death layers. |
| `game.d.b(Graphics)` case 7 | P7 draw order: background, H, actors, floating text. | `VqsvBattleRenderer.renderSourceLikeBattleUi()`. | PARTIAL | Source `game.d.java:1867..1874`; rebuild `VqsvBattleRenderer.java:50..81`. | Proven order mismatch for H and actor `u`. |
| `game.b.a(short, byte)` | Creates actor `u = new ah()`, passes `{effectId,state,dir}`, anchors at actor `i,j`; only `20/state3` and `22/state4` get vertical offset. | `P7ActorAnimation` plus `drawP7ActorEffect()`. | PORTED/PARTIAL | Source `game.b.java:119..130`; rebuild `VqsvBattleRuntime.java:50..123`, renderer `1597..1613`. | Anchor fix is done; exact sprite frame transform still partial. |
| `game.b.a(Graphics)` | Actor draw order: L before, base actor, L after, actor `u`. | Renderer currently draws bases first, then one global actor effect. | PARTIAL | Source `game.b.java:224..282`; rebuild `VqsvBattleRenderer.java:56..81`. | Patch should draw P7 actor effect immediately after its owner actor's base/L layer. |
| `ah.java` actor ids 20..34 | Maps source actor ids to sprite ids `262,263,264,265,266,267,268,299,300,301,304,306,307,308,309`. | `SOURCE_AH_ACTOR_SPRITES`. | PORTED | Source `ah.java:16,261..264`; rebuild `VqsvBattleRuntime.java:50..122`. | None for mapping. |
| `ah.java` actor fallback update | For ids not handled by special cases, actor AH calls inner sprite `d.d()`. | `P7ActorAnimation.tick()` -> `SpriteAnim.tickHoldLast()`. | PARTIAL | Source `ah.java:466..468`; rebuild `VqsvBattleRuntime.java:102..105`. | Source sprite reset mode `p` is not fully represented by `tickHoldLast()`. |
| Source `d.java a(byte,byte,boolean)` | Sets actor sprite state/cursor and initializes delay from source anim data. | `SpriteAnim.setState()`. | PORTED/PARTIAL | Source `d.java:118..136`; rebuild `VqsvSpriteRenderer.java:102..107,144..150`. | Delay initialization shape is close; exact reset mode is still partial. |
| Source `d.java d()/e()/b(frame)` | `d()` advances with delays and completion/reset policy; `e()` means cursor is last frame; `b(frame)` tests cursor. | `tickHoldLast()`, `lastFrame()`, `frame()`. | PARTIAL | Source `d.java:138..170`; rebuild `VqsvSpriteRenderer.java:128..150`, `VqsvBattleRuntime.java:90..104`. | Source `d.d()` completion behavior can reset/hold/loop; rebuild always holds last until runtime clears. |
| Source `d.java a(Graphics,x,y,dir)` | Draws current source cell with direction transforms and cell offsets. | `drawBattleSpriteAtSource()` via sprite renderer. | PORTED/PARTIAL | Source `d.java:327..380`; renderer uses decoded sprite/cell data. | Java ME transform/crop parity is source-shaped, not pixel-perfect. |
| `VqsvBattleRenderer.drawP7ActorEffect()` | Draws current actor `u` at source actor origin. | Same method. | PORTED/PARTIAL | Rebuild `VqsvBattleRenderer.java:1597..1613`. | Anchor is fixed; call site order is still wrong. |

## Patch Applied

Implemented shared renderer-only patch in
`rebuild_game/src/main/java/VqsvBattleRenderer.java`:

1. Moved actor `u` drawing into the owner side's actor draw order.
2. Target-side enemy `u` now draws immediately after enemy base/L layer, before the
   player base layer.
3. Player-side `u` now draws immediately after player base/L layer.
4. P7 special `H` now draws before actor list to match source P7 case, while
   preserving current hide/restore state.

Classification:

- Actor draw-order mismatch: PROVEN.
- H draw-order mismatch: PROVEN.
- Shared draw-order patch: PORTED/PARTIAL.
- Exact sprite timing mismatch: PARTIAL/PENDING; do not patch timing until a
  focused frame strip proves the concrete delta.

## Verification

Ran:

```text
powershell -ExecutionPolicy Bypass -File .\build.ps1
java -cp build\classes VqsvIntroDemo --smoke-suite battle_fire_live_frame_strip build\smoke\fire_live_frame_strip_after_p7_actor_draw_order
java -cp build\classes VqsvIntroDemo --smoke-suite battle_fire_source_stage_animation build\smoke\fire_source_stage_after_p7_actor_draw_order
java -cp build\classes VqsvIntroDemo --smoke-suite battle_quick build\smoke\battle_quick_after_p7_actor_draw_order
java -cp build\classes com.vqsv.rebuild.Main --check
java -cp build\classes VqsvBattleDamageFormulaCheck
git diff --check
```

Results:

- Build: PASS.
- `battle_fire_live_frame_strip`: PASS.
- `battle_fire_source_stage_animation`: PASS.
- `battle_quick`: PASS, 227 checkpoints.
- `com.vqsv.rebuild.Main --check`: PASS.
- `VqsvBattleDamageFormulaCheck`: PASS.
- `git diff --check`: PASS. Git reported only the normal CRLF warning for
  `VqsvBattleRenderer.java`.
- Targeted mojibake scan was run through code-point patterns for the old bad
  sequences and returned no matches. A broad `Ã`/`Â` scan intentionally matches
  valid Vietnamese text and is not a valid failure signal.

Smoke PNGs:

```text
rebuild_game/build/smoke/fire_live_frame_strip_after_p7_actor_draw_order/battle_fire_live_frame_strip.png
rebuild_game/build/smoke/fire_source_stage_after_p7_actor_draw_order/battle_fire_source_stage_animation.png
rebuild_game/build/smoke/fire_source_stage_after_p7_actor_draw_order/battle_fire_source_stage_animation_zoom.png
rebuild_game/build/smoke/battle_quick_after_p7_actor_draw_order/
```

Visual note:

- The PNGs are nonblank and show source-stage strips for Fire skills 0..9.
- This is not an original-vs-rebuild pixel comparison, so the status remains
  VISUAL PARTIAL, not pixel-perfect.

## Next Step

Next audit should focus on exact sprite timing/state transition:

1. Compare source `d.d()/d.e()/b(frame)` to `SpriteAnim.tickHoldLast()` for
   actor sprite `262` state `0`.
2. Decide whether the next patch should change actor `u` completion timing or
   target hit/recover state timing.
3. Only then reopen Fire skill-by-skill visual closeout.
