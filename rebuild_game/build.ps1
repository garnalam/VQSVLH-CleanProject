param(
    [string]$ModulesRoot = ""
)

$ErrorActionPreference = "Stop"

$ProjectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$BuildDir = Join-Path $ProjectRoot "build"
$ClassesDir = Join-Path $BuildDir "classes"
$LibDir = Join-Path $BuildDir "libs"
$ManifestPath = Join-Path $BuildDir "MANIFEST.MF"
$ReleaseJarName = "vqsv-liet-hoa-rebuild.jar"
$JarPath = Join-Path $LibDir $ReleaseJarName

$JarTool = "jar"
if ($env:JAVA_HOME) {
    $CandidateJar = Join-Path $env:JAVA_HOME "bin\jar.exe"
    if (Test-Path $CandidateJar) {
        $JarTool = $CandidateJar
    }
}

if ($ModulesRoot -eq "") {
    $ModulesRoot = (Resolve-Path (Join-Path $ProjectRoot "..\modules")).Path
}

if (!(Test-Path $ModulesRoot)) {
    throw "Modules root not found: $ModulesRoot"
}

Remove-Item -Recurse -Force $ClassesDir -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Force -Path $ClassesDir, $LibDir | Out-Null
Remove-Item -Force (Join-Path $LibDir "vqsv-rebuild-skeleton.jar") -ErrorAction SilentlyContinue
Remove-Item -Force $JarPath -ErrorAction SilentlyContinue

$Sources = Get-ChildItem -Path (Join-Path $ProjectRoot "src\main\java") -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
if ($Sources.Count -eq 0) {
    throw "No Java source files found."
}

javac -encoding UTF-8 -d $ClassesDir $Sources

$ResourcesDir = Join-Path $ProjectRoot "src\main\resources"
if (Test-Path $ResourcesDir) {
    Copy-Item -Path (Join-Path $ResourcesDir "*") -Destination $ClassesDir -Recurse -Force
}

@"
Manifest-Version: 1.0
Main-Class: com.vqsv.rebuild.Main
Implementation-Title: VQSV Liet Hoa Rebuild
Implementation-Version: 0.1.0

"@ | Set-Content -Path $ManifestPath -Encoding ASCII

& $JarTool --create --file $JarPath --manifest $ManifestPath -C $ClassesDir .

Write-Host "Built: $JarPath"
Write-Host "Modules root: $ModulesRoot"
Write-Host "Run: powershell -ExecutionPolicy Bypass -File `"$ProjectRoot\run.ps1`""
