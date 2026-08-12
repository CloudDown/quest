#!/usr/bin/env bash
# Lance l'API Quest en local (port 8001). Option : --ngrok (tunnel public 4G).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
SCRIPTS="$(cd "$ROOT/.." && pwd)/scripts"
PORT="${QUEST_PORT:-8001}"
USE_NGROK=0
for arg in "$@"; do
  case "$arg" in --ngrok|ngrok) USE_NGROK=1 ;; esac
done
[[ "${NGROK:-0}" == "1" ]] && USE_NGROK=1

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
[[ -n "$LAN_IP" ]] && echo "Téléphone (Wi-Fi) : http://${LAN_IP}:${PORT}/"

if [[ "$USE_NGROK" == "1" ]]; then
  # shellcheck disable=SC1091
  source "$SCRIPTS/with-ngrok.sh"
  start_ngrok "$PORT"
  echo "Téléphone (4G / hors Wi-Fi) : ${NGROK_URL}/"
  echo "  → rebuild APK : ./release-github.sh ngrok"
fi
echo

if [[ "$USE_NGROK" == "1" ]]; then
  .venv/bin/uvicorn main:app --reload --host 0.0.0.0 --port "$PORT"
else
  exec .venv/bin/uvicorn main:app --reload --host 0.0.0.0 --port "$PORT"
fi
