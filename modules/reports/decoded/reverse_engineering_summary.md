# Reverse Engineering Summary

## What Was Decoded
- event_scene_binary: 12
- java_class: 68
- map_binary: 94
- midi_audio: 1
- module_tileset_binary: 8
- png_chunk_container_or_raw_png: 22
- png_image: 246
- script_table_binary: 17
- sprite_binary: 299
- ui_layout_binary: 40
- unknown_or_misc: 6
- vm_bytecode_script: 1

## Runtime Entry Points
- `game.GameMIDLet` creates `game.e`, a full-screen MIDP Canvas.
- `game.e.run()` is the main loop: update `game.i.b()`, repaint, then sleep for `an.B() - frame_time` milliseconds.
- `game.i` is the high-level state/screen/audio controller. It loads only `/data/sound/0.mid` as real `audio/midi`.
- `game.k` is the main world/scene controller. It loads `/data/event/scene_<scene>.mid` and selects a room block by `g`.
- `j` is the map renderer/loader for `/data/map/map_<id>.mid` and `/data/mod/mod_<tileset>.mid`.
- `aq`, `game.c`, `game.d`, and `game.k` load script tables under `/data/script/*.mid`.
- `ao`/`ab` load binary UI layout files under `/data/ui/*.ui`.
- package `a.*` is a compact VM used by `/data/event/scene_13.mib` for SMS/payment scripting.

## Resource Formats
- `data/sound/0.mid`: real MIDI audio (`MThd`).
- `data/img/img_*.mid`: PNG bytes stored with `.mid` extension.
- `data/tex/menu.mid`: PNG bytes stored with `.mid`; other `data/tex/*.mid` are compact PNG chunk containers reconstructed by `ae.f()`.
- `data/event/scene_*.mid`: scene files containing a block table, per-room string pool, room name, actor records, and action/event records.
- `data/map/map_*.mid`: map files containing compact flag, tileset/mod id, dimensions, tile size, layer count, and tile/object entries.
- `data/mod/mod_*.mid`: tileset rectangle metadata for rendering map tiles.
- `data/script/*.mid`: typed tables: string matrices, short matrices, byte matrices, or multiple such matrices in sequence.
- `data/ui/*.ui`: binary UI layouts parsed from `ao.a(...)`: root container, styles, visual controls, and list/grid controls.
- `data/event/scene_13.mib`: compact VM bytecode tree parsed from `a.f`.

## Output Files
- `file_inventory.csv`: every file, size, SHA-256, magic bytes, inferred type.
- `decoded_images.csv` and `decoded_assets/png/`: extracted/reconstructed PNG assets.
- `decoded_tables_index.csv` and `decoded_assets/json/`: decoded map/script/event/ui JSON files.
- `code_resource_references.csv`: resource path strings and source locations in decompiled code.
- `decompiled_source_cfr/`: CFR Java source for all classes.
- `bytecode_javap/`: verbose bytecode disassembly for all classes.