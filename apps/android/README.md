# Quest — Android

App native Kotlin + Jetpack Compose pour [Quest](../../PRODUCT.md).

> **Version 0.1** — MVP local avec données de démo.

## Concept

- Un **quest du jour** : un lieu, toute la ville, aujourd'hui.
- **Check-in photo** sur place, validé par les pairs.
- **Mur de photos** déverrouillé après avoir checké.
- **Leaderboard** et **journal** de ses explorations passées.

## Stack technique

- **Kotlin** + **Jetpack Compose** (Material 3)
- **DataStore Preferences** — persistance locale (à venir)
- **Coroutines / Flow** — état réactif via singleton repository
- minSdk 26 · targetSdk 36 · JDK 17

## Structure du projet

```
app/src/main/java/com/quest/app/
├── MainActivity.kt           # Pager 4 pages + onglets + overlay check-in
├── core/
│   ├── Models.kt             # Quest, CheckIn, QuestUiState…
│   └── QuestRepository.kt    # État local, données de démo
└── ui/
    ├── TodayPanel.kt         # Quest du jour + carte
    ├── WallPanel.kt          # Mur de photos
    ├── SocialPanel.kt        # Leaderboard
    ├── HistoryPanel.kt       # Journal visuel
    ├── CheckInScreen.kt      # Overlay check-in photo
    └── theme/Theme.kt        # Palette nature : lime, soleil, blanc chaud
```

## Build & installation

**Prérequis** : JDK 17+, Android SDK (chemin dans `local.properties`).

```bash
# Compiler l'APK debug
./gradlew assembleDebug

# Installer sur un appareil connecté
adb install app/build/outputs/apk/debug/app-debug.apk
```

L'APK debug se trouve dans `app/build/outputs/apk/debug/app-debug.apk`.

## Limites connues (v0.1)

- Pas de backend — données simulées en mémoire
- Pas de CameraX ni GPS — check-in en démo
- Pas de carte réelle — placeholder coordonnées
- Pas de notifications push

## Roadmap

- [ ] Backend API (Retrofit)
- [ ] CameraX + vérification présence au POI
- [ ] Carte (osmdroid ou Maps)
- [ ] Notifications quest du jour
- [ ] Release Play Store
