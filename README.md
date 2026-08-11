# Quest

App Android + API FastAPI — un lieu par jour, même ville, check-in sur place.

**APK** : [GitHub Releases](https://github.com/CloudDown/quest/releases/latest)

## Utilisation

1. **Backend** — sur le PC :
   ```bash
   ./server.sh
   ```
   → `http://localhost:8001/docs`

2. **Publier l'APK** :
   ```bash
   ./release-github.sh
   ```

3. **Téléphone** — APK depuis GitHub Releases, même Wi-Fi que le PC.

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
