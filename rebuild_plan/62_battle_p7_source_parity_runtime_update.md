# 62 Battle P7 Source Parity Runtime Update

Status: IMPLEMENTED / SOURCE-SHAPED PARITY.

Scope: replace the old duration-only P7 effect chain with a source-shaped
runtime for:

- exact sprite-table frame cursor trigger for `effect.mid` chunk `[4]`;
- chunk `[5]/[6]` actor-state trigger;
- `H.i()` / `H.a()` / `H.e()` active lifecycle;
- actor base hide/restore around special effect `H`;
- non-special actor-effect sprite mapping through original `ah` id `20..34`.

## Source Facts Used

Source files:

- `modules/source_code/decoded/decompiled_source_cfr/game/d.java`
  - `game.d.n()`
  - P7 case actor branches
  - P7 `H` branch
- `modules/source_code/decoded/decompiled_source_cfr/ah.java`
- `modules/source_code/decoded/decompiled_source_cfr/n.java`
- `modules/source_code/decoded/decompiled_source_cfr/game/b.java`
- `modules/source_code/decoded/decompiled_source_cfr/d.java`

Important source semantics:

| Source behavior | Rebuild equivalent | Status |
| --- | --- | --- |
| `game.d.n()` sets `J = I`, loads current `effect.mid` chunk, then increments `I` | `enterP7SourceChunk()` tracks `p7SourceJ/p7SourceI` | PORTED |
| Non-special chunk calls `actor.a(animId,param)` and creates actor `u = new ah()` | `P7ActorAnimation` maps `ah` ids `20..34` to sprites `262..309` | PORTED/PARTIAL |
| Actor `u.a()` starts active flag inherited from `n.e` | `P7ActorAnimation.start()` | PORTED |
| Actor `u.a(frame)` checks actual animation cursor | `SpriteAnim` duration/cursor from `spr_*_all(r)` | PORTED |
| Chunk `[4]` advances to next chunk on actor frame trigger | `tickP7ActorAnimation()` checks cursor and enters next chunk | PORTED |
| Chunk `[5]/[6]` changes actor state on frame trigger | `setP7BaseState()` on matching cursor | PORTED/PARTIAL |
| Target-side `[4]` keeps target actor `u` alive while entering next chunk | `enterP7SourceChunk(..., preservedActorAnimation)` | PORTED |
| `H.a()` starts special effect and hides owner actor with `actor.b(false)` | `p7SpecialActive=true` and `battleP7BaseHidden*` | PORTED |
| `H.e()` ticks until complete and restores owner with `actor.b(true)` | `p7SpecialTicks` duration and hide restore | PORTED/PARTIAL |
| AH type `9` and type `1` render overlays | Existing renderer paths retained | PORTED/PARTIAL |

## Files Changed

- `rebuild_game/src/main/java/VqsvBattleRuntime.java`
- `rebuild_game/src/main/java/VqsvBattleRenderer.java`
- `rebuild_game/src/main/java/VqsvIntroDemo.java`

## Validation

Smoke PNGs generated:

- `build_intro_demo/fullparity3_battle_elder_p7_skill15_start.png`
- `build_intro_demo/fullparity3_battle_elder_p7_skill15_chunk4_trigger.png`
- `build_intro_demo/fullparity3_battle_elder_p7_skill15_after.png`
- `build_intro_demo/fullparity3_battle_elder_p7_speffect45_start.png`
- `build_intro_demo/fullparity3_battle_elder_p7_speffect45_overlay.png`
- `build_intro_demo/fullparity3_battle_elder_p7_speffect45_type1.png`
- `build_intro_demo/fullparity3_battle_elder_p7_speffect45_after.png`
- `build_intro_demo/fullparity3_route_sophie_after_battle_branch.png`
- `build_intro_demo/fullparity3_route_bunny_after_battle_task.png`
- `build_intro_demo/fullparity3_route_elder_after_battle_reward_state.png`

Regression commands passed:

- `build.ps1`
- `com.vqsv.rebuild.Main --check`
- `VqsvBattleDamageFormulaCheck`
- Java source mojibake scan

## Honest Limits

This closes the runtime gap for the requested source mechanisms, but it is not
yet a proven pixel-perfect MIDP match.

Remaining reasons:

- No side-by-side pixel compare against the original MIDP emulator frame dump
  has been performed.
- `game.d.W()` depends on battle side-effect flags `Z/aa`; rebuild still keeps
  that as a pending status/buff-engine hook.
- AH renderers beyond the currently supported type `9` and type `1` remain
  pending globally.
- Multi-unit/multi-target cases still need broader battle smoke once those
  systems are ported.

Practical conclusion:

- Requested P7 lifecycle mechanisms are now source-shaped and smoke backed.
- Do not call the whole battle effect renderer 100% pixel-perfect until MIDP
  pixel comparison is available.
