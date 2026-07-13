param(
    [ValidateSet("npc", "catch")]
    [string]$Lane = "npc",
    [string]$Scenario = "command",
    [string]$ModulesRoot = "",
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
    throw "Unknown battle lab scenario: lane=$Lane scenario=$Scenario"
}

if (!$NoBuild) {
    & (Join-Path $ProjectRoot "build.ps1") -ModulesRoot $ModulesRoot
}

if ($ModulesRoot -eq "") {
    $ModulesRoot = (Resolve-Path (Join-Path $ProjectRoot "..\modules")).Path
}

$ClassesDir = Join-Path $ProjectRoot "build\classes"
$Checkpoint = $Scenarios[$Lane][$Scenario]

Write-Host "Battle lab manual"
Write-Host "  lane:       $Lane"
Write-Host "  scenario:   $Scenario"
Write-Host "  checkpoint: $Checkpoint"

java "-Dvqsv.modules=$ModulesRoot" -cp $ClassesDir VqsvIntroDemo --play-checkpoint $Checkpoint
if ($LASTEXITCODE -ne 0) {
    throw "Battle lab manual failed: lane=$Lane scenario=$Scenario checkpoint=$Checkpoint"
}
