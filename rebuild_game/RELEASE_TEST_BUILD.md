# VQSV Liet Hoa Rebuild - Release Test Build

This is the canonical local play-test build for the rebuild project.

## Official Launcher

```powershell
powershell -ExecutionPolicy Bypass -File .\run.ps1
```

`run.ps1` rebuilds first, then launches:

```text
build/libs/vqsv-liet-hoa-rebuild.jar
```

## Included Runtime Path

The release-test jar runs `com.vqsv.rebuild.Main`, which starts:

```text
GameApp -> BootFlowState -> LegacyIntroDemoState -> VqsvIntroDemo.Scene
```

That path includes the current rebuilt intro/world/event/battle/save/UI work in `src/main/java`, including the scene scripts and battle runtime we have been porting.

## Dev-Only Paths

`VqsvIntroDemo --smoke-*` and `VqsvIntroDemo --play-*` are developer checkpoint tools. They are not the official player-facing test build.

## Current Truth

This build is still a partial rebuild, not a completed original-game replacement. Anything marked `PARTIAL`, `APPROX`, `STUB`, `PENDING`, or `UNKNOWN` in `rebuild_plan` remains honest status, but the release-test launcher must always include the latest implemented code.
