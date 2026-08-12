# Quest

App Android + API FastAPI — un lieu par jour, même ville, check-in sur place.

**APK** : [GitHub Releases](https://github.com/CloudDown/quest/releases/latest)

## Utilisation

1. **Backend** — sur le PC :
   ```bash
   ./server.sh            # Wi-Fi local (port 8001)
   ./server.sh --ngrok    # + tunnel public (4G)
   ```

2. **Publier l'APK** :
   ```bash
   ./release-github.sh
   ./release-github.sh ngrok
   ```

3. **Téléphone** — APK depuis GitHub Releases (LAN = même Wi-Fi ; ngrok = 4G OK).

Comptes démo : `rain` / `rain`, `alex` / `alex`.

## Structure

```
quest/
├── server.sh
├── release-github.sh
├── server/          # FastAPI (port 8001)
└── mobile_app/      # Android
```

Conventions Cursor : [AGENTS.md](AGENTS.md)
