param(
    [string]$OutputRoot = "dist"
)

$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$distRoot = Join-Path $repoRoot $OutputRoot
$packageRoot = Join-Path $distRoot "server\WizardMusicServer"
$zipFile = Join-Path $distRoot "server\WizardMusicServer.zip"

Push-Location $repoRoot
try {
    mvn -q -pl player-server -am package -DskipTests

    $jarFile = Get-ChildItem "player-server\target\player-server-*.jar" |
        Where-Object { $_.Name -notlike "original-*" } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
    if ($null -eq $jarFile) {
        throw "Unable to locate the packaged player-server jar."
    }

    if (Test-Path $packageRoot) {
        Remove-Item -Recurse -Force $packageRoot
    }
    if (Test-Path $zipFile) {
        Remove-Item -Force $zipFile
    }

    New-Item -ItemType Directory -Force -Path `
        (Join-Path $packageRoot "bin"), `
        (Join-Path $packageRoot "config"), `
        (Join-Path $packageRoot "logs"), `
        (Join-Path $packageRoot "runtime\online\music"), `
        (Join-Path $packageRoot "runtime\online\lyrics"), `
        (Join-Path $packageRoot "runtime\online\covers"), `
        (Join-Path $packageRoot "runtime\online\cache") | Out-Null

    Copy-Item $jarFile.FullName (Join-Path $packageRoot "wizard-music-server.jar")
    Copy-Item "player-server\src\main\resources\application.yml" (Join-Path $packageRoot "config\application.yml")

    @'
$ErrorActionPreference = "Stop"
$appRoot = Split-Path -Parent $PSScriptRoot
Push-Location $appRoot
try {
    java -jar ".\wizard-music-server.jar" --spring.config.additional-location="file:.\config\"
} finally {
    Pop-Location
}
'@ | Set-Content -LiteralPath (Join-Path $packageRoot "bin\start-server.ps1") -Encoding UTF8

    @'
#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

cd "${APP_ROOT}"
java -jar "./wizard-music-server.jar" --spring.config.additional-location="file:./config/"
'@ | Set-Content -LiteralPath (Join-Path $packageRoot "bin\start-server.sh") -Encoding UTF8

    Compress-Archive -Path (Join-Path $packageRoot "*") -DestinationPath $zipFile -Force
    Write-Host "Server package created at $packageRoot"
    Write-Host "Zip archive created at $zipFile"
} finally {
    Pop-Location
}
