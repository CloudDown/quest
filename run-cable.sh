#!/usr/bin/env bash
# Installe + lance Quest sur le téléphone USB.
# Usage: ./run-cable.sh [--build]
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SCRIPTS="$(cd "$ROOT/.." && pwd)/scripts"
export MOBILE_ROOT="$ROOT/mobile_app"
export APP_ID="com.quest.app"
export APP_ACTIVITY=".MainActivity"
export API_KEY="quest.api.base.url"
export API_PORT="${QUEST_API_PORT:-8001}"
export API_APP="quest"
export TRAILING_SLASH=no
exec "$SCRIPTS/run-cable.sh" "$@"
