#!/usr/bin/env bash
# Configure l'URL API Quest dans mobile_app/local.properties
# Usage:
#   ./configure-device-api.sh usb   # adb reverse tcp:8000 (recommandé)
#   ./configure-device-api.sh lan   # IP Wi-Fi de la machine
set -euo pipefail

ROOT="$(cd "$(dirname "$0")" && pwd)"
PROPS="$ROOT/local.properties"
MODE="${1:-usb}"

if [[ ! -f "$PROPS" ]]; then
  if [[ -f "$ROOT/local.properties.example" ]]; then
    cp "$ROOT/local.properties.example" "$PROPS"
  else
    echo "sdk.dir=" > "$PROPS"
  fi
fi

# Preserve sdk.dir
SDK_LINE="$(grep '^sdk.dir=' "$PROPS" || true)"

case "$MODE" in
  usb)
    URL="http://127.0.0.1:8000"
    if command -v adb >/dev/null 2>&1; then
      adb reverse tcp:8000 tcp:8000 || true
      echo "adb reverse tcp:8000 → téléphone"
    fi
    ;;
  lan)
    IP="$(ip -4 route get 1.1.1.1 2>/dev/null | awk '{for(i=1;i<=NF;i++) if($i=="src"){print $(i+1); exit}}')"
    if [[ -z "${IP:-}" ]]; then
      IP="$(hostname -I 2>/dev/null | awk '{print $1}')"
    fi
    if [[ -z "${IP:-}" ]]; then
      echo "Impossible de détecter l'IP LAN" >&2
      exit 1
    fi
    URL="http://${IP}:8000"
    ;;
  *)
    echo "Usage: $0 [usb|lan]" >&2
    exit 1
    ;;
esac

{
  [[ -n "$SDK_LINE" ]] && echo "$SDK_LINE"
  echo "quest.api.base.url=$URL"
} > "$PROPS.tmp"
mv "$PROPS.tmp" "$PROPS"

echo "quest.api.base.url=$URL écrit dans local.properties"
