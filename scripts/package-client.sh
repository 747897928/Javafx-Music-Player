#!/usr/bin/env bash
set -euo pipefail

OUTPUT_ROOT="${1:-dist}"
SERVER_BASE_URL="${2:-http://127.0.0.1:18080}"
REPO_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
DIST_ROOT="${REPO_ROOT}/${OUTPUT_ROOT}"
WORK_ROOT="${REPO_ROOT}/.package-work/client"
INPUT_DIR="${WORK_ROOT}/input"
PACKAGE_ROOT="${DIST_ROOT}/client"
ARCHIVE_FILE="${DIST_ROOT}/client/WizardMusicBox.tar.gz"

cd "${REPO_ROOT}"

if ! command -v jpackage >/dev/null 2>&1; then
  echo "jpackage is not available in the current JDK." >&2
  exit 1
fi

mvn -q install -DskipTests
mvn -q -pl player-fx dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/dist-libs

MAIN_JAR="$(find player-fx/target -maxdepth 1 -type f -name 'player-fx-*.jar' ! -name 'original-*' | sort | tail -n 1)"
if [[ -z "${MAIN_JAR}" ]]; then
  echo "Unable to locate the packaged player-fx jar." >&2
  exit 1
fi

rm -rf "${WORK_ROOT}" "${PACKAGE_ROOT}"
rm -f "${ARCHIVE_FILE}"
mkdir -p "${INPUT_DIR}"

cp "${MAIN_JAR}" "${INPUT_DIR}/"
find "player-fx/target/dist-libs" -maxdepth 1 -type f -name '*.jar' -exec cp {} "${INPUT_DIR}/" \;

jpackage \
  --type app-image \
  --dest "${PACKAGE_ROOT}" \
  --name "WizardMusicBox" \
  --input "${INPUT_DIR}" \
  --main-jar "$(basename "${MAIN_JAR}")" \
  --main-class "com.aquarius.wizard.player.fx.PlayerFxLauncher"

APP_ROOT="${PACKAGE_ROOT}/WizardMusicBox"
if [[ ! -d "${APP_ROOT}" ]]; then
  echo "jpackage did not produce the expected WizardMusicBox app-image." >&2
  exit 1
fi

mkdir -p "${APP_ROOT}/config" "${APP_ROOT}/logs"
cat > "${APP_ROOT}/config/player-fx.ini" <<EOF
# WizardMusicBox desktop runtime configuration
# Change the URL below to the address of your deployed online music service.

server.base-url=${SERVER_BASE_URL}
EOF

mkdir -p "$(dirname "${ARCHIVE_FILE}")"
tar -czf "${ARCHIVE_FILE}" -C "${PACKAGE_ROOT}" "WizardMusicBox"

echo "Client package created at ${APP_ROOT}"
echo "Archive created at ${ARCHIVE_FILE}"
