#!/usr/bin/env bash
# Lance l'API Quest en local (port 8001).
set -euo pipefail
ROOT="$(cd "$(dirname "$0")" && pwd)"
PORT="${QUEST_PORT:-8001}"
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
[[ -n "$LAN_IP" ]] && echo "Téléphone (même Wi-Fi) : http://${LAN_IP}:${PORT}/"
echo

exec .venv/bin/uvicorn main:app --reload --host 0.0.0.0 --port "$PORT"
