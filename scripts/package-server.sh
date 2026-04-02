#!/usr/bin/env bash
set -euo pipefail

OUTPUT_ROOT="${1:-dist}"
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIST_ROOT="${REPO_ROOT}/${OUTPUT_ROOT}"
PACKAGE_ROOT="${DIST_ROOT}/server/WizardMusicServer"
ARCHIVE_FILE="${DIST_ROOT}/server/WizardMusicServer.tar.gz"

cd "${REPO_ROOT}"

mvn -q -pl player-server -am package -DskipTests

JAR_FILE="$(find player-server/target -maxdepth 1 -type f -name 'player-server-*.jar' ! -name 'original-*' | sort | tail -n 1)"
if [[ -z "${JAR_FILE}" ]]; then
  echo "Unable to locate the packaged player-server jar." >&2
  exit 1
fi

rm -rf "${PACKAGE_ROOT}"
rm -f "${ARCHIVE_FILE}"

mkdir -p \
  "${PACKAGE_ROOT}/bin" \
  "${PACKAGE_ROOT}/config" \
  "${PACKAGE_ROOT}/logs" \
  "${PACKAGE_ROOT}/runtime/online/music" \
  "${PACKAGE_ROOT}/runtime/online/lyrics" \
  "${PACKAGE_ROOT}/runtime/online/covers" \
  "${PACKAGE_ROOT}/runtime/online/cache"

cp "${JAR_FILE}" "${PACKAGE_ROOT}/wizard-music-server.jar"
cp "player-server/src/main/resources/application.yml" "${PACKAGE_ROOT}/config/application.yml"

cat > "${PACKAGE_ROOT}/bin/start-server.sh" <<'EOF'
#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

cd "${APP_ROOT}"
java -jar "./wizard-music-server.jar" --spring.config.additional-location="file:./config/"
EOF
chmod +x "${PACKAGE_ROOT}/bin/start-server.sh"

cat > "${PACKAGE_ROOT}/bin/start-server.ps1" <<'EOF'
$ErrorActionPreference = "Stop"
$appRoot = Split-Path -Parent $PSScriptRoot
Push-Location $appRoot
try {
    java -jar ".\wizard-music-server.jar" --spring.config.additional-location="file:.\config\"
} finally {
    Pop-Location
}
EOF

mkdir -p "$(dirname "${ARCHIVE_FILE}")"
tar -czf "${ARCHIVE_FILE}" -C "${DIST_ROOT}/server" "WizardMusicServer"

echo "Server package created at ${PACKAGE_ROOT}"
echo "Archive created at ${ARCHIVE_FILE}"
