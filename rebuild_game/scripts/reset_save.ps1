param(
    [ValidateSet("Delete", "Room1Bunny")]
    [string]$Mode = "Room1Bunny"
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$SavePath = Join-Path $ProjectRoot "build\save\vqsv_autosave.properties"

if (!(Test-Path $SavePath)) {
    Write-Host "No rebuild save found: $SavePath"
    exit 0
}

if ($Mode -eq "Delete") {
    Remove-Item -LiteralPath $SavePath -Force
    Write-Host "Deleted rebuild save: $SavePath"
    exit 0
}

$lines = Get-Content -LiteralPath $SavePath
$set = [ordered]@{
    "scene" = "1"
    "room" = "1"
    "camera" = "254,20"
    "player" = "374,180,0,1"
}

foreach ($key in @($set.Keys)) {
    $found = $false
    for ($i = 0; $i -lt $lines.Count; $i++) {
        if ($lines[$i] -match "^$([regex]::Escape($key))=") {
            $lines[$i] = "$key=$($set[$key])"
            $found = $true
            break
        }
    }
    if (!$found) {
        $lines += "$key=$($set[$key])"
    }
}

Set-Content -LiteralPath $SavePath -Value $lines -Encoding ISO-8859-1
Write-Host "Reset rebuild save to room1 Bunny trigger checkpoint: $SavePath"
