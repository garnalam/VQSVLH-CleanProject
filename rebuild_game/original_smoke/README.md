# Original JAR Smoke Harness

Purpose: run the original MIDP JAR in a local emulator, send scripted input,
and capture PNG screenshots for visual comparison with the rebuild smoke
images.

This is emulator-driven visual smoke. It is not tick-perfect like
`VqsvIntroDemo --smoke-drive`, because the original game runs inside the MIDP
emulator on real time.

## Default Inputs

- Original JAR:
  `C:\Users\Dell\Downloads\S40-VuongQuoc-SungVat-LietHoa-240x320.jar`
- Emulator:
  `C:\Users\Dell\Downloads\KEmulator-JavaEmulator\KEmulator_JavaEmulator\KEmulator.exe`

## Run

From `C:\Users\Dell\Downloads\ResourcesVQSV\rebuild_game`:

```powershell
powershell -ExecutionPolicy Bypass -File .\original_smoke\run_original_smoke.ps1
```

Run a specific route:

```powershell
powershell -ExecutionPolicy Bypass -File .\original_smoke\run_original_smoke.ps1 `
  -RouteFile .\original_smoke\routes\boot_menu_probe.route
```

Output images go under:

```text
original_smoke\captures\<timestamp>\
```

By default the script crops KEmulator's client area to the inner `240x320`
game screen. Use `-RawClient` if you need the full emulator client including
status/footer.

## Route Commands

One command per line:

```text
wait <milliseconds>
key <token> [afterMs]
click <x> <y> [afterMs]
capture <name>
note <free text>
```

Key tokens:

```text
0 1 2 3 4 5 6 7 8 9
UP DOWN LEFT RIGHT
FIRE ENTER
LSOFT RSOFT
STAR POUND
```

Notes:

- `click x y` uses emulator client coordinates, so for this game the intended
  screen is roughly 240x320.
- Keep every original smoke result labeled as emulator-driven. Do not present
  it as deterministic engine timing.
