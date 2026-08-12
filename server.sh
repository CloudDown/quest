#!/usr/bin/env bash
# Lance l'API Quest (port 8001) + ngrok par défaut. Option : --local (sans tunnel).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SCRIPTS="$(cd "$ROOT/.." && pwd)/scripts"
PORT="${QUEST_PORT:-8001}"
USE_NGROK=1
for arg in "$@"; do
  case "$arg" in
    --local|--no-ngrok|local) USE_NGROK=0 ;;
    --ngrok|ngrok) USE_NGROK=1 ;;
  esac
done
[[ "${NGROK:-}" == "0" ]] && USE_NGROK=0

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

LAN_IP="$(ip -4 route get 1.1.1.1 2>/dev/null | awk '{for(i=1;i<=NF;i++) if($i=="src") print $(i+1)}' | head -1 || true)"
echo "Quest API — http://localhost:${PORT}/docs"
[[ -n "$LAN_IP" ]] && echo "LAN (optionnel) : http://${LAN_IP}:${PORT}/"

if [[ "$USE_NGROK" == "1" ]]; then
  # shellcheck disable=SC1091
  source "$SCRIPTS/with-ngrok.sh"
  start_ngrok "$PORT"
  printf '%s\n' "$NGROK_URL" > "$ROOT/.ngrok-url"
  echo "Téléphone (ngrok / 4G) : ${NGROK_URL}/"
  echo "  APK : ./release-github.sh   (cible ngrok par défaut)"
fi
echo

if [[ "$USE_NGROK" == "1" ]]; then
  .venv/bin/uvicorn main:app --reload --host 0.0.0.0 --port "$PORT"
else
  exec .venv/bin/uvicorn main:app --reload --host 0.0.0.0 --port "$PORT"
fi
