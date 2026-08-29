# VQSV portable feature patch

This directory contains the active `vqsv.*` source and resource files changed for the test build on branch `vqsv-portable-fix`. It is intentionally isolated from the older `com.vqsv.rebuild` tree so the legacy reconstruction is not overwritten before review.

## Included changes

- LH-064 Lôi Quang Hầu -> Hỏa Diễm Hầu Vương evolution and cleaned idle/action sprites.
- LH-004 Hỏa Diễm Hồ -> Thiên Viêm Hồ evolution.
- Selectable animated world pet companion with save/load support.
- Tàn Nguyệt Long Ma boss, world placement, shadow/nameplate, exact battle stats, interaction fix, and left-facing battle presentation.
- Headless smoke tests for evolution, companion persistence, boss presence, and battle start.

## Test build

The matching Windows build is attached to GitHub Release `vqsv-portable-fix-v1`:

`VQSV-PORTABLE-Java26-Boss-Quay-Trai-Windows.zip`

Extract it and run `Chay-VQSV-PORTABLE.bat`. The archive includes its own Java 26 runtime.

SHA-256:

`9D6A361C0F01A01672091B5311C1F732E6BD27C9351FB8FF230A6B0D92F7C494`

## Review and merge policy

Do not merge this branch into `main` until gameplay testing is complete. The files in this directory preserve their paths relative to the active `vqsv` source root so they can be reviewed or applied selectively.
