param(
    [string]$OutputRoot = "dist",
    [string]$ServerBaseUrl = "http://127.0.0.1:18080"
)

$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$distRoot = Join-Path $repoRoot $OutputRoot
$workRoot = Join-Path $repoRoot ".package-work\client"
$inputDir = Join-Path $workRoot "input"
$jpackageDir = Join-Path $workRoot "jpackage"
$packageRoot = Join-Path $distRoot "client"
$zipFile = Join-Path $distRoot "client\WizardMusicBox.zip"

Push-Location $repoRoot
try {
    if (-not (Get-Command jpackage -ErrorAction SilentlyContinue)) {
        throw "jpackage is not available in the current JDK."
    }

    mvn -q install -DskipTests
    mvn -q -pl player-fx dependency:copy-dependencies "-DincludeScope=runtime" "-DoutputDirectory=target\dist-libs"

    $mainJar = Get-ChildItem "player-fx\target\player-fx-*.jar" |
        Where-Object { $_.Name -notlike "original-*" } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
    if ($null -eq $mainJar) {
        throw "Unable to locate the packaged player-fx jar."
    }

    if (Test-Path $workRoot) {
        Remove-Item -Recurse -Force $workRoot
    }
    if (Test-Path $packageRoot) {
        Remove-Item -Recurse -Force $packageRoot
    }
    if (Test-Path $zipFile) {
        Remove-Item -Force $zipFile
    }

    New-Item -ItemType Directory -Force -Path $inputDir, $jpackageDir | Out-Null

    Copy-Item $mainJar.FullName $inputDir
    Copy-Item "player-fx\target\dist-libs\*.jar" $inputDir

    & jpackage `
        --type app-image `
        --dest $packageRoot `
        --name "WizardMusicBox" `
        --input $inputDir `
        --main-jar $mainJar.Name `
        --main-class "com.aquarius.wizard.player.fx.PlayerFxLauncher"

    $appRoot = Join-Path $packageRoot "WizardMusicBox"
    if (-not (Test-Path $appRoot)) {
        throw "jpackage did not produce the expected WizardMusicBox app-image."
    }

    New-Item -ItemType Directory -Force -Path (Join-Path $appRoot "config"), (Join-Path $appRoot "logs") | Out-Null

    @"
# WizardMusicBox desktop runtime configuration
# Change the URL below to the address of your deployed online music service.

server.base-url=$ServerBaseUrl
"@ | Set-Content -LiteralPath (Join-Path $appRoot "config\player-fx.ini") -Encoding UTF8

    Compress-Archive -Path (Join-Path $appRoot "*") -DestinationPath $zipFile -Force
    Write-Host "Client package created at $appRoot"
    Write-Host "Zip archive created at $zipFile"
} finally {
    Pop-Location
}
