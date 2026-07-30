#!/usr/bin/env bash
# Installation API Quest sur Raspberry Pi (systemd, redémarrage auto)
set -euo pipefail

QUEST_DIR="${QUEST_DIR:-/home/pi/quest}"
DATA_DIR="${QUEST_DATA:-/var/lib/quest}"
SERVICE_USER="${SERVICE_USER:-pi}"
SUDO_PASS="${SUDO_PASS:?SUDO_PASS/PI_PASS manquant — export PI_PASS ou oeuil/secrets.env}"
QUEST_PORT="${QUEST_PORT:-8001}"

sudo_cmd() {
  echo "$SUDO_PASS" | sudo -S "$@"
}

echo "==> Quest API — installation Pi"
echo "    code  : $QUEST_DIR/server"
echo "    data  : $DATA_DIR"
echo "    port  : $QUEST_PORT"
echo

sudo_cmd mkdir -p "$DATA_DIR/uploads"
sudo_cmd chown -R "$SERVICE_USER:$SERVICE_USER" "$DATA_DIR"

cd "$QUEST_DIR/server"

PYTHON=""
if command -v python3.13 >/dev/null 2>&1; then
  PYTHON="$(command -v python3.13)"
elif command -v python3.12 >/dev/null 2>&1; then
  PYTHON="$(command -v python3.12)"
elif command -v python3.11 >/dev/null 2>&1; then
  PYTHON="$(command -v python3.11)"
else
  PYTHON="$(command -v python3)"
fi

echo "==> Python : $($PYTHON --version 2>&1)"

if [[ -d .venv ]] && ! .venv/bin/python -m pip --version &>/dev/null; then
  echo "==> Suppression venv incompatible…"
  rm -rf .venv
fi

if [[ ! -x .venv/bin/python ]]; then
  rm -rf .venv
  "$PYTHON" -m venv .venv
fi

.venv/bin/python -m pip install -q --upgrade pip
.venv/bin/python -m pip install -q -r requirements.txt

echo "==> Service systemd"
UNIT="/tmp/quest-api.service"
cat > "$UNIT" <<EOF
[Unit]
Description=Quest API (FastAPI)
After=network-online.target
Wants=network-online.target

[Service]
Type=simple
User=$SERVICE_USER
Group=$SERVICE_USER
WorkingDirectory=$QUEST_DIR/server
Environment=QUEST_DATABASE_URL=sqlite:////$DATA_DIR/quest.db
Environment=QUEST_UPLOAD_DIR=$DATA_DIR/uploads
Environment=QUEST_DEMO_MODE=1
Environment=QUEST_CORS_ORIGINS=*
Environment=PATH=$QUEST_DIR/server/.venv/bin:/usr/local/bin:/usr/bin:/bin
ExecStart=$QUEST_DIR/server/.venv/bin/uvicorn main:app --host 0.0.0.0 --port $QUEST_PORT
Restart=always
RestartSec=5

[Install]
WantedBy=multi-user.target
EOF

sudo_cmd cp "$UNIT" /etc/systemd/system/quest-api.service
rm -f "$UNIT"

sudo_cmd systemctl daemon-reload
sudo_cmd systemctl enable quest-api
sudo_cmd systemctl restart quest-api

echo
echo "==> Statut"
sleep 2
sudo_cmd systemctl status quest-api --no-pager -l || true
echo
LAN_IP="$(hostname -I | awk '{print $1}')"
echo "LAN  : http://${LAN_IP}:${QUEST_PORT}/docs"
echo "Logs : journalctl -u quest-api -f"
