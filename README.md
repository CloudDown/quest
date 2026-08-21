# Quest

App Android + API FastAPI — un lieu par jour, check-in sur place.

**APK** : [GitHub Releases](https://github.com/CloudDown/quest/releases/latest)  
**API publique** : https://quest.instree.org

## Utilisation

```bash
./server.sh
./release-github.sh   # GitHub + popup de mise à jour in-app
./run-cable.sh        # téléphone USB (adb)
./run-cable.sh --build
```

Laisse `./server.sh` tourner après une release : l'app propose d'installer toute seule.

LAN : `./server.sh --local` puis `./release-github.sh lan`.

Comptes démo : `rain` / `rain`, `alex` / `alex`.

Conventions Cursor : [AGENTS.md](AGENTS.md)
