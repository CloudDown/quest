# Quest

App Android + API FastAPI — un lieu par jour, check-in sur place.

**APK** : [GitHub Releases](https://github.com/CloudDown/quest/releases/latest)

## Utilisation

```bash
./server.sh              # API + ngrok (port 8001)
./release-github.sh      # APK → URL ngrok
```

Sans tunnel : `./server.sh --local` puis `./release-github.sh lan`.

Comptes démo : `rain` / `rain`, `alex` / `alex`.

> Refaire `./release-github.sh` après chaque nouveau tunnel ngrok.

Conventions Cursor : [AGENTS.md](AGENTS.md)
