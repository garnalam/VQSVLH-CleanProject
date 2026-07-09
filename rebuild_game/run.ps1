param(
    [string]$ModulesRoot = "",
    [switch]$NoBuild
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$JarPath = Join-Path $ProjectRoot "build\libs\vqsv-liet-hoa-rebuild.jar"

if (!$NoBuild -or !(Test-Path $JarPath)) {
    & (Join-Path $ProjectRoot "build.ps1")
}

if ($ModulesRoot -eq "") {
    $ModulesRoot = (Resolve-Path (Join-Path $ProjectRoot "..\modules")).Path
}

java "-Dvqsv.modules=$ModulesRoot" -jar $JarPath
