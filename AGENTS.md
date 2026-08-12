# Quest — guide agents

## Layout

- `server/` — FastAPI, port **8001**, env `QUEST_*`
- `mobile_app/` — Android Kotlin + Compose

## Commandes

```bash
./server.sh
./server.sh --ngrok
./release-github.sh
./release-github.sh ngrok
cd mobile_app && ./gradlew assembleDebug
```

## Conventions

- Modules : `auth/`, `places/`, `quests/`, `social/`
- Tirage déterministe lieu du jour : `ville + date` (Wikipedia)
- Ouvrir **`mobile_app/`** dans Android Studio (pas la racine)
