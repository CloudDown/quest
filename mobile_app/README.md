# Quest — Android (`mobile_app/`)

App native Kotlin + Jetpack Compose. Backend : `../server/` (FastAPI).

Voir le [README racine](../README.md) et [AGENTS.md](../AGENTS.md).

## Concept v0.2

- **Lieu du jour réel** via Wikipedia autour du centre-ville (seed déterministe `ville + date`)
- **Carte OpenStreetMap** (osmdroid), photo Coil, distance GPS
- **Rareté calendaire** : 1 Rare / semaine, 1 Légendaire / mois
- Cache DataStore (stable toute la journée, offline après 1er load)
- Social / mur encore en démo locale (API serveur prête dans `../server/`)

## Stack

- Kotlin + Jetpack Compose (Material 3)
- osmdroid 6.1.20, Coil 3.2.0, Play Services Location 21.4.0, DataStore
- API URL via `local.properties` → `quest.api.base.url`

## Commandes

```bash
./configure-device-api.sh usb
./gradlew assembleDebug

# depuis la racine du repo :
adb install app/build/outputs/apk/debug/app-debug.apk
```
