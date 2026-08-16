#!/usr/bin/env bash
# Lance l'API Quest (port 8001). Cloudflare : https://quest.instree.org
# Option : --local
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SCRIPTS="$(cd "$ROOT/.." && pwd)/scripts"
PORT="${QUEST_PORT:-8001}"
USE_CF=1
for arg in "$@"; do
  case "$arg" in
    --local|local) USE_CF=0 ;;
  esac
done
[[ "${CLOUDFLARE:-}" == "0" ]] && USE_CF=0

cd "$ROOT/server"

PYTHON=""
if command -v python3.13 >/dev/null 2>&1; then
  PYTHON="$(command -v python3.13)"
elif command -v python3.12 >/dev/null 2>&1; then
  PYTHON="$(command -v python3.12)"
else
  PYTHON="$(command -v python3)"
fi

if [ ! -d .venv ]; then
  "$PYTHON" -m venv .venv
  .venv/bin/pip install -U pip
  .venv/bin/pip install -r requirements.txt
fi

export QUEST_DEMO_MODE="${QUEST_DEMO_MODE:-1}"

# shellcheck disable=SC1091
source "$SCRIPTS/cloudflare-urls.sh"
PUBLIC_URL="$(cloudflare_public_url quest)"

LAN_IP="$(ip -4 route get 1.1.1.1 2>/dev/null | awk '{for(i=1;i<=NF;i++) if($i=="src") print $(i+1)}' | head -1 || true)"
echo "Quest API — http://localhost:${PORT}/docs"
[[ -n "$LAN_IP" ]] && echo "LAN : http://${LAN_IP}:${PORT}/"

if [[ "$USE_CF" == "1" ]]; then
  "$SCRIPTS/cloudflare-tunnel.sh" ensure
  echo "Public (4G) : ${PUBLIC_URL}/"
fi
echo

exec .venv/bin/uvicorn main:app --reload --host 0.0.0.0 --port "$PORT"
