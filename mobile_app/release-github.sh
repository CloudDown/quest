#!/usr/bin/env bash
# Wrapper → scripts hub. Usage: ./release-github.sh [version]
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SCRIPTS="$(cd "$ROOT/../.." && pwd)/scripts"
export MOBILE_ROOT="$ROOT"
export API_KEY="quest.api.base.url"
export API_PORT="${QUEST_API_PORT:-8001}"
export TRAILING_SLASH=no
exec "$SCRIPTS/release-github.sh" CloudDown/quest Quest pi "${1:-}"
