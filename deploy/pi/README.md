# Quest API sur Raspberry Pi

Serveur FastAPI en **systemd** (`quest-api.service`) — port **8001** (Dispo utilise 8000).

## Prérequis Pi

- Raspberry Pi OS **64-bit**, Python 3.11+
- Port **8001** libre
- SSH activé

## Déploiement automatique (depuis ton PC)

```bash
cd /chemin/vers/quest
python3 deploy/pi/deploy_paramiko.py
```

Variables : `PI_PASS` **obligatoire** (`oeuil/secrets.env` ou `export`). Optionnelles : `PI_HOST`, `PI_USER`, `PI_DIR`, `PI_DATA`.

## Déploiement manuel

```bash
./deploy/pi/deploy-from-dev.sh
```

## Résultat

| Élément | Chemin / URL |
|---------|----------------|
| Code | `/home/pi/quest/server` |
| SQLite + uploads | `/var/lib/quest/` |
| Service | `quest-api.service` |
| Docs API | `http://192.168.2.170:8001/docs` |
| Health | `http://192.168.2.170:8001/health` |

```bash
sudo systemctl status quest-api
journalctl -u quest-api -f
```

## Mobile (LAN / Waydroid)

```bash
cd mobile_app
./configure-device-api.sh pi
./run-phone.sh pi
# ou
./run-waydroid.sh pi
```
