param(
    [ValidateSet("npc", "catch", "all")]
    [string]$Lane = "all",
    [string]$Suite = "core",
    [string]$ModulesRoot = "",
    [string]$OutDir = "",
    [switch]$NoBuild,
    [switch]$List
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
. (Join-Path $ProjectRoot "BattleLabScenarios.ps1")

$Scenarios = Get-VqsvBattleLabScenarios
$Suites = Get-VqsvBattleLabSuites
if ($List) {
    Show-VqsvBattleLabSuites -Suites $Suites
    exit 0
}

if (!$NoBuild) {
    & (Join-Path $ProjectRoot "build.ps1") -ModulesRoot $ModulesRoot
}

if ($ModulesRoot -eq "") {
    $ModulesRoot = (Resolve-Path (Join-Path $ProjectRoot "..\modules")).Path
}

if ($OutDir -eq "") {
    $OutDir = Join-Path $ProjectRoot "build_intro_demo\battle_lab_suites"
}

$ClassesDir = Join-Path $ProjectRoot "build\classes"
$LanesToRun = if ($Lane -eq "all") { @("npc", "catch") } else { @($Lane) }
$Failures = New-Object System.Collections.Generic.List[string]
$Total = 0

foreach ($CurrentLane in $LanesToRun) {
    if (!$Suites[$CurrentLane].Contains($Suite)) {
        Show-VqsvBattleLabSuites -Suites $Suites
        throw "Unknown battle lab suite: lane=$CurrentLane suite=$Suite"
    }

    $SuiteOutDir = Join-Path $OutDir (Join-Path $CurrentLane $Suite)
    New-Item -ItemType Directory -Force -Path $SuiteOutDir | Out-Null

    foreach ($Scenario in $Suites[$CurrentLane][$Suite]) {
        if (!$Scenarios[$CurrentLane].Contains($Scenario)) {
            $Failures.Add("missing-scenario lane=$CurrentLane suite=$Suite scenario=$Scenario")
            continue
        }

        $Checkpoint = $Scenarios[$CurrentLane][$Scenario]
        $OutPng = Join-Path $SuiteOutDir "$Scenario.png"
        $Total += 1

        Write-Host "battle-lab-suite-step lane=$CurrentLane suite=$Suite scenario=$Scenario checkpoint=$Checkpoint"
        java "-Dvqsv.modules=$ModulesRoot" -cp $ClassesDir VqsvIntroDemo --smoke-checkpoint $Checkpoint $OutPng
        if ($LASTEXITCODE -ne 0) {
            $Failures.Add("failed lane=$CurrentLane suite=$Suite scenario=$Scenario checkpoint=$Checkpoint")
        }
    }
}

if ($Failures.Count -gt 0) {
    foreach ($Failure in $Failures) {
        Write-Host "battle-lab-suite-fail $Failure"
    }
    throw "Battle lab suite failed: failures=$($Failures.Count) total=$Total"
}

Write-Host "battle-lab-suite-ok lane=$Lane suite=$Suite total=$Total outDir=$OutDir"
