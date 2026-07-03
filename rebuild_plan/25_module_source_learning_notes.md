# Module Source Learning Notes

Purpose: ghi lai vong doc sau bo `modules/rebuild_plan` va doi chieu voi
`modules/source_code`. File nay la ban do hoc du an cho cac turn sau, khong
thay the cac audit chi tiet.

## Source Of Truth Order

1. `modules/rebuild_plan/*.md`: canonical audit/module notes.
2. `modules/source_code/decoded/decompiled_source_cfr`: source decompiled de doi chieu.
3. `modules/source_code/decoded/bytecode_javap`: dung khi CFR damaged.
4. `modules/script/decoded`, `modules/event/decoded`, `modules/*/original`: data/resource truth.
5. `rebuild_plan/*.md` at root: handoff va progress gan day cua rebuild demo.
6. `rebuild_game/src/main/java/VqsvIntroDemo.java`: current implementation, co the manual/approx.

Rule: neu implementation khac source/data, tin source/data truoc, roi cap nhat
audit/progress ro rang.

## Runtime Core

Main docs read:

- `modules/rebuild_plan/06_runtime_core_notes.md`
- source: `game/GameMIDLet.java`, `game/e.java`, `game/i.java`, `an.java`, `ap.java`

Key model:

- `game.e` is the MIDP Canvas/game loop: update -> repaint -> serviceRepaints -> sleep around 66ms.
- `ap` input is a bitmask system. Do not drive gameplay from raw desktop key events.
- `game.i` is the top-level state manager. World runs under state `11`, battle under state `13`; loading/reload states include `9/12/22/23`.
- `an.s/t/u/v` are not cosmetic: they are loading repaint pulse plus loading-complete flag.
- SMS/payment side effects live partly in `an`, even though real provider behavior can be stubbed.

Port implication:

- Keep state ids or a direct mapping during early port to avoid transition mistakes.
- Preserve child input delegation from state manager to active module.
- Music/SMS can be adapters, but call sites and side effects must exist.

## Resource And Renderer Foundation

Main docs read:

- `modules/rebuild_plan/05_resource_format_specs.md`
- `modules/rebuild_plan/07_resource_renderer_notes.md`
- `modules/rebuild_plan/15_renderer_primitive_deep_audit.md`
- source: `aq.java`, `ae.java`, `am.java`, `aa.java`, `o.java`, root `d.java`, root `j.java`, `s.java`

Key model:

```text
actor/ui sprite index
  -> aq.a[spriteIndex] from /data/script/sprite.mid
     [0]   = sprId
     [1..] = imgIds
  -> aa.a(sprId) loads /data/spr/spr_<sprId>_all(r)
  -> am.a(imgId) loads /data/img/img_<imgId>.mid
  -> root d.java draws frame/cell/animation through MIDP drawRegion
```

Important correction:

- Actor `record[1]` is sprite table index, not image id.
- Manual `SPRITE_TO_IMGS` in current rebuild is a temporary shortcut. The real source is `sprite.mid`.

Port implication:

- Replace/augment manual sprite table with a real `sprite.mid` loader as soon as practical.
- Preserve `aa` special branch for `sprId 86..185`.
- Preserve image cache release difference: `am.b()` keeps image; `am.c()` can unload.
- Map renderer can redraw full frame first, but final parity needs root `j.java` transform/copyArea behavior.

## World, Actor, Event VM

Main docs read:

- `modules/rebuild_plan/09_world_event_notes.md`
- `modules/rebuild_plan/10_world_event_opcode_matrix.md`
- `modules/rebuild_plan/11_world_event_opcode_deep_audit.md`
- `modules/rebuild_plan/19_world_tick_actor_matrix.md`
- source: `game/k.java`, `game/c.java`, `game/a.java`, `f.java`, `n.java`, `ai.java`, `ah.java`, `p.java`, `ad.java`

Key model:

- `game.k` owns room/map/player/actors/event VM/UI bridge.
- Room linear index is `game.k.l[worldId] + roomId`; actor/event persistence depends on this.
- `p` is event timeline; `ad` is one command.
- Event states:
  - `0` idle
  - `1` active/running
  - `2` conditional/interact
  - `3` complete/persist
  - `4` alternate complete/persist
  - `5` command waiting/running
  - `6` blocked/paused

Event VM rules:

- If state is not `5` or `6`, VM auto-advances command pointer with `p.e()`.
- Branch opcodes commonly set pointer to `target - 2` because auto-advance happens after switch.
- Completed state `3/4` must be persisted into `game.c.b[roomLinearIndex][eventId]`.

Port implication:

- Manual scripts are acceptable for current demo only if they mark APPROX/STUB honestly.
- Full event port should mirror `p/ad` parser and VM wait/advance rules.
- Actor movement/collision should eventually come from `n/f/game.a`, not only speed-duration stepping.
- `ai` camera modes `0/1` are usable; mode `2` is unsupported/dead unless new evidence appears.

## Text And Cutscene Renderer

Main docs read:

- `modules/rebuild_plan/20_text_cutscene_renderer_matrix.md`
- source: `game/j.java`, `game/c.java`, `s.java`, bytecode for `game.j`

Key model:

- `game.j` is text/cutscene renderer, not the map renderer root `j.java`.
- Event opcodes `1/48/51` use `game.j`.
- Opcode `84` is UI dialog through `game.h`, not direct `game.j`.
- Current event data uses mode `0` only for `game.j` (`short_args[0] = 10`).
- Mode 0 depends on text starting with color tag `#RRGGBB`; audited data has 22/22 such strings.
- Mode 3 is broken/dead in this JAR and should not be "fixed" unless another data/JAR proves it is used.

Port implication:

- Do not promise pixel-perfect text until bitmap font baseline, MIDP prompt font, timing, and screenshots are validated.
- Opcode `48` uses `x=b[1], y=b[2], w=b[3], h=b[4]` from `game.c`.

## Effect And UI Runtime

Main docs read:

- `modules/rebuild_plan/08_effect_ui_renderer_notes.md`
- `modules/rebuild_plan/12_ui_system_notes.md`
- `modules/rebuild_plan/16_ui_workflow_matrix.md`
- source: `b.java`, `ab.java`, `ao.java`, `al.java`, `af.java`, `ac.java`, `k.java`, `m.java`, `z.java`, `game/h.java`

Key model:

- `b.java` is a screen/effect compositor with multiple channels: main, secondary, special UI overlay.
- UI is a binary `.ui` widget runtime, not hardcoded screen coordinates.
- `ab` manages UI cache/stack; `ao` parses one UI and handles focus/input.
- `game.h` is the real UI workflow/controller and mutates gameplay.
- `package a/*` is a Lua-like/payment VM, not the normal UI layout system.

Port implication:

- Build UI primitives before trying to port bag/shop/battle workflows.
- `msgwarm.ui` is common and not optional for real UI flows.
- Dynamic list binding in `z.java` remains CFR-risky and must be validated through callers.

## Gameplay, Battle, Save

Main docs read:

- `modules/rebuild_plan/13_gameplay_battle_save_notes.md`
- `modules/rebuild_plan/17_battle_state_machine.md`
- `modules/rebuild_plan/18_battle_formula_status_matrix.md`
- source: `game/g.java`, `game/b.java`, `game/d.java`, `game/h.java`, `game/k.java`, `ar.java`

Key model:

- `game.g` is player/world gameplay model.
- `game.b` is pet/battle unit model.
- `game.d` is battle engine and state machine.
- `game.h` supplies battle/menu/bag/shop UI command flows.
- `ar` is low-level RMS wrapper; `game.k` coordinates save slots.

Battle status:

- State machine and formula are source/bytecode-audited enough to port logic.
- Remaining battle risk is animation/pixel timing and end-to-end UI edge cases, especially pet switch.

Save slots:

```text
0 PK6_RMS_ACTOR
1 PK6_RMS_WORLD
2 PK6_RMS_EVENT
3 PK6_RMS_RMS
4 PK6_RMS_SMS
5 PK6_RMS_CNTSMS
6 PK6_RMS_GOLD
7 PK6_RMS_POKPET
8 PK6_RMS_CONITEM
9 PK6_RMS_PETBALL
```

Important:

- Slot `5 PK6_RMS_CNTSMS` exists in the name list, but no read/write evidence was found in current decoded source/bytecode search.
- Do not assign behavior to slot `5` without new evidence.

## SMS / Payment

Main docs read:

- `modules/rebuild_plan/21_sms_payment_side_effect_matrix.md`
- source: `an.java`, `game/h.java`, `q.java`, `r.java`, `u.java`, `v.java`, `package a/*`

Key model:

- Game-side rewards are applied in `an.b(true)` while state `l == 4`.
- Current source path calls success directly enough that offline rebuild can implement deterministic payment adapter.
- `q` Canvas and `/data/event/scene_13.mib` VM are coherent but no visible `new q(...)` caller was found in current source.

Port implication:

- Port `an` product side effects first.
- Treat carrier/provider/legacy VM as optional later work.
- Save rewards through normal gameplay save paths, not a made-up SMS-only path.

## Current Rebuild Implications

- Root `rebuild_plan/24_scene1_room0_sprite_mapping_audit.md` should use `sprite.mid` rows as evidence.
- Scene/manual code should keep `PORTED/APPROX/STUB/MISSING` labels honestly.
- Do not touch user-approved early intro/scene_0 unless task requires it.
- Next safe technical direction after audit: port source-backed room0 sprite mappings/resources, then smoke room0.
- Larger structural direction: replace manual `SpriteAnim.SPRITE_TO_IMGS` with a real `aq.a`/`sprite.mid` equivalent.

## Handoff Re-Read Update

After re-reading `rebuild_plan/23_new_chat_handoff_training.md` with the
source map in mind:

- The handoff is the near-term operating rule for the current manual demo path.
- `modules/rebuild_plan` is the deeper canonical map for source/module behavior.
- The handoff task about room0 sprite mapping should be interpreted through
  `aq.java` and `/data/script/sprite.mid`: "sprite id" in current demo actor
  rows is really a sprite table index, and the true row is
  `[sprId, imgId...]`.
- Its warning about blank room0 actors is now sharper: many are not unknown;
  they are source-proven but not yet ported into rebuild code/resources.
- The suggested next step "fill sprite mappings/resources" must mean
  source-backed rows from `modules/script/decoded/data__script__sprite.mid.json`,
  not hand-written guesses.
- The long-term warning remains unchanged: manual scripted porting is acceptable
  only while it is honest about APPROX/STUB and does not replace the source VM.

## Residual Risk Register

- CFR damaged methods still need caution: `game.c.n/b`, `game.k.b`, `game.d.b`, `game.j` render, `ao.a(Graphics)`, `b.a(Graphics)`.
- Some damaged battle formulas are closed by bytecode audit, but implementation still needs regression tests.
- Pixel parity remains unclaimed for effects, map copyArea scrolling, UI dynamic lists, battle animations, and text prompt font.
- Domain names for some vectors/tables are PARTIAL; keep numeric/source-backed names until evidence is strong.
