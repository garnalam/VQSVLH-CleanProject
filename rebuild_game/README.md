# VQSV Rebuild Game

Skeleton Java project for rebuilding `Vuong Quoc Sung Vat - Liet Hoa` from decoded source and resources.

This project lives next to `modules` by default:

```text
<ResourcesVQSV>\
  modules\
  rebuild_game\
```

Runtime code resolves the default asset/source root as `..\modules` relative to `rebuild_game`. You can override it with `-Dvqsv.modules=...` or `run.ps1 -ModulesRoot ...`.

## Layout

- `src/main/java/com/vqsv/rebuild`: new rebuild source.
- `src/main/java/com/vqsv/rebuild/resource`: asset locator and binary reader foundation.
- `build.ps1`: compile Java sources and create a runnable JAR.
- `run.ps1`: build if needed and run the skeleton.
- `build/libs/vqsv-rebuild-skeleton.jar`: generated output.

## Asset Root

Default asset/source root from this project:

```text
..\modules
```

Override when needed:

```powershell
powershell -ExecutionPolicy Bypass -File .\run.ps1 -ModulesRoot "C:\path\to\modules"
```

Or with Java directly:

```powershell
java "-Dvqsv.modules=C:\path\to\ResourcesVQSV\modules" -jar .\build\libs\vqsv-rebuild-skeleton.jar
```

## Resource Foundation

Current low-level resource layer:

- `AssetPaths`: normalized paths for `img`, `spr`, `map`, `mod`, `font`, `ui`, and `event`.
- `ResourceLocator`: existence checks, required file lookup, byte/text/open stream helpers.
- `BinaryReader`: Java `DataInputStream`-style big-endian reads for signed/unsigned byte, short, and int.
- `ImageLoader`: decoded PNG/raw PNG image loading via `ImageIO`, with cache by path and decoded image id.
- `ImageAssetInventory`: scans `modules/img/original` and `modules/img/decoded` and reports missing pairs.
- `BitmapFont`: parser/renderer for `root_misc/original/font.bin`, ported from `s.java`.
- `SpriteTable` / `SpriteMetadata` / `SpriteAnimator`: initial sprite table, `spr_*_all(r)` metadata, animation, and cell renderer.
- `GameMap` / `TileSet` / `MapRenderer`: initial map/mod/layer parser and full-redraw renderer.
- `ResourceSmokeCheck`: non-GUI check that probes representative files in `modules`.

## Build

```powershell
cd <ResourcesVQSV>\rebuild_game
powershell -ExecutionPolicy Bypass -File .\build.ps1
```

## Run

```powershell
cd <ResourcesVQSV>\rebuild_game
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

The first screen only proves that the runtime loop and asset locator work. Real modules should be ported in this order:

1. MIDP-like runtime/input/state layer.
2. Resource loader and renderer primitives.
3. Text/cutscene intro path.
4. World/event opcode runtime.
5. UI, battle, save, and remaining systems.

## Non-GUI Check

```powershell
java "-Dvqsv.modules=..\modules" -jar .\build\libs\vqsv-rebuild-skeleton.jar --check
```
