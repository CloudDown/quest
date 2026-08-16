#!/usr/bin/env bash
# Build APK + publie sur GitHub Releases (défaut : Cloudflare).
# Usage: ./release-github.sh [cloudflare|lan|usb|emulator] [version]
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SCRIPTS="$(cd "$ROOT/.." && pwd)/scripts"
MODE="cloudflare"
VER=""
if [[ "${1:-}" =~ ^(lan|cloudflare|usb|emulator|ngrok)$ ]]; then
  MODE="$1"
  VER="${2:-}"
else
  VER="${1:-}"
fi
[[ "$MODE" == "ngrok" ]] && MODE="cloudflare"
export MOBILE_ROOT="$ROOT/mobile_app"
export API_KEY="quest.api.base.url"
export API_PORT="${QUEST_API_PORT:-8001}"
export API_APP="quest"
export TRAILING_SLASH=no
exec "$SCRIPTS/release-github.sh" CloudDown/quest Quest "$MODE" "$VER"
