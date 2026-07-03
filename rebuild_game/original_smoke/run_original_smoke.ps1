param(
    [string]$JarPath = "C:\Users\Dell\Downloads\S40-VuongQuoc-SungVat-LietHoa-240x320.jar",
    [string]$EmulatorExe = "C:\Users\Dell\Downloads\KEmulator-JavaEmulator\KEmulator_JavaEmulator\KEmulator.exe",
    [string]$RouteFile = ".\original_smoke\routes\boot_menu_probe.route",
    [string]$OutRoot = ".\original_smoke\captures",
    [int]$WindowWaitMs = 15000,
    [int]$GameX = 2,
    [int]$GameY = 2,
    [int]$GameWidth = 240,
    [int]$GameHeight = 320,
    [switch]$RawClient,
    [switch]$KeepOpen
)

$ErrorActionPreference = "Stop"

function Resolve-FullPath([string]$path) {
    $ExecutionContext.SessionState.Path.GetUnresolvedProviderPathFromPSPath($path)
}

function Ensure-NativeMethods {
    if ("NativeMethods" -as [type]) {
        return
    }
    Add-Type -TypeDefinition @"
using System;
using System.Runtime.InteropServices;

public static class NativeMethods {
    [StructLayout(LayoutKind.Sequential)]
    public struct RECT {
        public int Left;
        public int Top;
        public int Right;
        public int Bottom;
    }

    [StructLayout(LayoutKind.Sequential)]
    public struct POINT {
        public int X;
        public int Y;
    }

    [DllImport("user32.dll")]
    public static extern bool GetClientRect(IntPtr hWnd, out RECT rect);

    [DllImport("user32.dll")]
    public static extern bool ClientToScreen(IntPtr hWnd, ref POINT point);

    [DllImport("user32.dll")]
    public static extern bool SetForegroundWindow(IntPtr hWnd);

    [DllImport("user32.dll")]
    public static extern bool ShowWindow(IntPtr hWnd, int nCmdShow);
}
"@
}

function Get-EmulatorWindowHandle([System.Diagnostics.Process]$process, [int]$timeoutMs) {
    $deadline = [DateTime]::UtcNow.AddMilliseconds($timeoutMs)
    while ([DateTime]::UtcNow -lt $deadline) {
        try {
            $process.Refresh()
            if (-not $process.HasExited -and ([int64]$process.MainWindowHandle) -ne 0) {
                return $process.MainWindowHandle
            }
        } catch {
            # KEmulator.exe often spawns a javaw.exe child and exits quickly.
        }
        $candidate = Get-Process | Where-Object {
            ([int64]$_.MainWindowHandle) -ne 0 -and
            ($_.MainWindowTitle -match "KEmulator|Vuong|Sung|Vat|Liet" -or $_.ProcessName -match "KEmulator|javaw")
        } | Sort-Object StartTime -Descending | Select-Object -First 1
        if ($candidate) {
            return $candidate.MainWindowHandle
        }
        Start-Sleep -Milliseconds 250
    }
    throw "Timed out waiting for emulator window."
}

function Focus-Window([IntPtr]$hwnd) {
    [NativeMethods]::ShowWindow($hwnd, 5) | Out-Null
    [NativeMethods]::SetForegroundWindow($hwnd) | Out-Null
    Start-Sleep -Milliseconds 150
}

function Get-ClientInfo([IntPtr]$hwnd) {
    $rect = New-Object NativeMethods+RECT
    if (-not [NativeMethods]::GetClientRect($hwnd, [ref]$rect)) {
        throw "GetClientRect failed."
    }
    $pt = New-Object NativeMethods+POINT
    $pt.X = 0
    $pt.Y = 0
    [NativeMethods]::ClientToScreen($hwnd, [ref]$pt) | Out-Null
    [pscustomobject]@{
        X = $pt.X
        Y = $pt.Y
        Width = $rect.Right - $rect.Left
        Height = $rect.Bottom - $rect.Top
    }
}

function Capture-Client([IntPtr]$hwnd, [string]$path) {
    Add-Type -AssemblyName System.Drawing
    $info = Get-ClientInfo $hwnd
    if ($info.Width -le 0 -or $info.Height -le 0) {
        throw "Invalid client size: $($info.Width)x$($info.Height)"
    }
    $cropX = 0
    $cropY = 0
    $cropW = $info.Width
    $cropH = $info.Height
    if (-not $RawClient -and $info.Width -ge ($GameX + $GameWidth) -and $info.Height -ge ($GameY + $GameHeight)) {
        $cropX = $GameX
        $cropY = $GameY
        $cropW = $GameWidth
        $cropH = $GameHeight
    }
    $bmp = New-Object System.Drawing.Bitmap($cropW, $cropH)
    $gfx = [System.Drawing.Graphics]::FromImage($bmp)
    try {
        $gfx.CopyFromScreen(($info.X + $cropX), ($info.Y + $cropY), 0, 0, $bmp.Size)
        $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    } finally {
        $gfx.Dispose()
        $bmp.Dispose()
    }
}

function Convert-KeyToken([string]$token) {
    switch ($token.ToUpperInvariant()) {
        "0" { "0"; break }
        "1" { "1"; break }
        "2" { "2"; break }
        "3" { "3"; break }
        "4" { "4"; break }
        "5" { "5"; break }
        "6" { "6"; break }
        "7" { "7"; break }
        "8" { "8"; break }
        "9" { "9"; break }
        "UP" { "{UP}"; break }
        "DOWN" { "{DOWN}"; break }
        "LEFT" { "{LEFT}"; break }
        "RIGHT" { "{RIGHT}"; break }
        "FIRE" { "{ENTER}"; break }
        "ENTER" { "{ENTER}"; break }
        "LSOFT" { "{F1}"; break }
        "RSOFT" { "{F2}"; break }
        "STAR" { "*"; break }
        "POUND" { "/"; break }
        default { throw "Unknown key token: $token" }
    }
}

function Invoke-Route([IntPtr]$hwnd, [string]$routePath, [string]$outDir) {
    $shell = New-Object -ComObject WScript.Shell
    $captureIndex = 0
    foreach ($raw in Get-Content -LiteralPath $routePath) {
        $line = $raw.Trim()
        if ($line.Length -eq 0 -or $line.StartsWith("#")) {
            continue
        }
        $parts = $line -split "\s+"
        $cmd = $parts[0].ToLowerInvariant()
        switch ($cmd) {
            "wait" {
                if ($parts.Count -lt 2) { throw "wait requires milliseconds: $line" }
                Start-Sleep -Milliseconds ([int]$parts[1])
            }
            "key" {
                if ($parts.Count -lt 2) { throw "key requires token: $line" }
                Focus-Window $hwnd
                $send = Convert-KeyToken $parts[1]
                $shell.SendKeys($send)
                $after = if ($parts.Count -ge 3) { [int]$parts[2] } else { 250 }
                Start-Sleep -Milliseconds $after
            }
            "click" {
                if ($parts.Count -lt 3) { throw "click requires x y: $line" }
                Focus-Window $hwnd
                $info = Get-ClientInfo $hwnd
                Add-Type -AssemblyName System.Windows.Forms
                [System.Windows.Forms.Cursor]::Position = New-Object System.Drawing.Point(($info.X + [int]$parts[1]), ($info.Y + [int]$parts[2]))
                Add-Type -TypeDefinition @"
using System.Runtime.InteropServices;
public static class MouseNative {
    [DllImport("user32.dll")]
    public static extern void mouse_event(int flags, int dx, int dy, int data, int extraInfo);
}
"@ -ErrorAction SilentlyContinue
                [MouseNative]::mouse_event(0x0002, 0, 0, 0, 0)
                Start-Sleep -Milliseconds 50
                [MouseNative]::mouse_event(0x0004, 0, 0, 0, 0)
                $after = if ($parts.Count -ge 4) { [int]$parts[3] } else { 250 }
                Start-Sleep -Milliseconds $after
            }
            "capture" {
                if ($parts.Count -lt 2) { throw "capture requires name: $line" }
                $captureIndex++
                $safe = ($parts[1] -replace '[^A-Za-z0-9_.-]', '_')
                $path = Join-Path $outDir ("{0:000}_{1}.png" -f $captureIndex, $safe)
                Capture-Client $hwnd $path
                Write-Host "capture $path"
            }
            "note" {
                Write-Host ("note " + ($line.Substring(4).Trim()))
            }
            default {
                throw "Unknown route command: $cmd"
            }
        }
    }
}

$jarFull = Resolve-FullPath $JarPath
$emuFull = Resolve-FullPath $EmulatorExe
$routeFull = Resolve-FullPath $RouteFile
$outRootFull = Resolve-FullPath $OutRoot
$stamp = Get-Date -Format "yyyyMMdd_HHmmss"
$outDir = Join-Path $outRootFull ("original_" + $stamp)
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

Ensure-NativeMethods

Write-Host "original-smoke"
Write-Host "jar=$jarFull"
Write-Host "emulator=$emuFull"
Write-Host "route=$routeFull"
Write-Host "out=$outDir"

$proc = Start-Process -FilePath $emuFull -ArgumentList @($jarFull) -WorkingDirectory (Split-Path -Parent $emuFull) -PassThru
try {
    $hwnd = Get-EmulatorWindowHandle $proc $WindowWaitMs
    Focus-Window $hwnd
    Invoke-Route $hwnd $routeFull $outDir
    Write-Host "original-smoke-ok $outDir"
} finally {
    if (-not $KeepOpen) {
        try {
            $windowProcesses = Get-Process | Where-Object {
                ([int64]$_.MainWindowHandle) -ne 0 -and
                ($_.MainWindowTitle -match "KEmulator|Vuong|Sung|Vat|Liet" -or $_.ProcessName -match "KEmulator|javaw")
            }
            foreach ($windowProc in $windowProcesses) {
                if (-not $windowProc.HasExited) {
                    $windowProc.CloseMainWindow() | Out-Null
                }
            }
            Start-Sleep -Milliseconds 1000
            foreach ($windowProc in $windowProcesses) {
                Start-Sleep -Milliseconds 1000
                if (-not $windowProc.HasExited) {
                    $windowProc.Kill()
                }
            }
            if ($proc -and -not $proc.HasExited) {
                $proc.Kill()
            }
        } catch {
            Write-Warning $_
        }
    }
}
