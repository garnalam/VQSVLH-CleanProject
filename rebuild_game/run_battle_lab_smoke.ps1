param(
    [ValidateSet("npc", "catch")]
    [string]$Lane = "npc",
    [string]$Scenario = "command",
    [string]$ModulesRoot = "",
    [string]$OutDir = "",
    [string]$OutPng = "",
    [switch]$NoBuild,
    [switch]$List
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
. (Join-Path $ProjectRoot "BattleLabScenarios.ps1")

$Scenarios = Get-VqsvBattleLabScenarios
if ($List) {
    Show-VqsvBattleLabScenarios -Scenarios $Scenarios
    exit 0
}

if (!$Scenarios.Contains($Lane) -or !$Scenarios[$Lane].Contains($Scenario)) {
    Show-VqsvBattleLabScenarios -Scenarios $Scenarios
    throw "Unknown battle lab smoke scenario: lane=$Lane scenario=$Scenario"
}

if (!$NoBuild) {
    & (Join-Path $ProjectRoot "build.ps1") -ModulesRoot $ModulesRoot
}

if ($ModulesRoot -eq "") {
    $ModulesRoot = (Resolve-Path (Join-Path $ProjectRoot "..\modules")).Path
}

if ($OutDir -eq "") {
    $OutDir = Join-Path $ProjectRoot (Join-Path "build_intro_demo\battle_lab" $Lane)
}
New-Item -ItemType Directory -Force -Path $OutDir | Out-Null

if ($OutPng -eq "") {
    $SafeScenario = $Scenario -replace '[^A-Za-z0-9_.-]', '_'
    $OutPng = Join-Path $OutDir "$SafeScenario.png"
}
$OutPngParent = Split-Path -Parent $OutPng
if ($OutPngParent -ne "") {
    New-Item -ItemType Directory -Force -Path $OutPngParent | Out-Null
}

$ClassesDir = Join-Path $ProjectRoot "build\classes"
$Checkpoint = $Scenarios[$Lane][$Scenario]

Write-Host "Battle lab PNG smoke"
Write-Host "  lane:       $Lane"
Write-Host "  scenario:   $Scenario"
Write-Host "  checkpoint: $Checkpoint"
Write-Host "  out:        $OutPng"

java "-Dvqsv.modules=$ModulesRoot" -cp $ClassesDir VqsvIntroDemo --smoke-checkpoint $Checkpoint $OutPng
if ($LASTEXITCODE -ne 0) {
    throw "Battle lab smoke failed: lane=$Lane scenario=$Scenario checkpoint=$Checkpoint"
}
