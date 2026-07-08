# Quest — Android

App native Kotlin + Jetpack Compose pour [Quest](../../PRODUCT.md).

> **Version 0.2** — Lieu du jour réel via Wikipedia + géolocalisation. Social encore en démo.

## Concept

- Un **quest du jour** : un lieu, toute la ville, aujourd'hui.
- **Check-in photo** sur place, validé par les pairs.
- **Mur de photos** déverrouillé après avoir checké.
- **Leaderboard** et **journal** de ses explorations passées.

## Comment le lieu du jour est choisi

```
Position GPS → Geocoder (nom de ville) → centre de la ville
            → Wikipedia geosearch (rayon 10 km, ~50 POIs)
            → tirage déterministe seed = ville + date
            → cache DataStore (stable toute la journée, offline)
```

- **Même lieu pour tous** : le tirage se fait autour du centre-ville avec un
  seed `ville + date` — tous les habitants d'une ville ont le même lieu le même jour.
- **Rareté calendaire** : 1 jour Légendaire par mois, 1 jour Rare par semaine,
  choisis par hash `ville+mois` / `ville+semaine` (déterministe aussi).
- **Wikipedia** (fr ou en selon la langue du téléphone), sans clé API :
  geosearch + extraits + photos, en `HttpURLConnection` + `JSONObject`.
- **Distance affichée** entre ma position et le lieu.

## Stack technique

- **Kotlin** + **Jetpack Compose** (Material 3)
- **osmdroid** — carte OpenStreetMap (aperçu + plein écran)
- **Coil 3** — photos Wikipedia
- **Play Services Location** — position (FusedLocationProviderClient)
- **DataStore Preferences** — cache du quest du jour + check-in
- **Coroutines / Flow** — état réactif via singleton repository
- minSdk 26 · targetSdk 36 · JDK 17

> Note : Coil est figé en **3.2.0** — les versions 3.3+ sont compilées avec
> Kotlin 2.4, incompatible avec le compilateur Kotlin 2.2.10 embarqué par AGP 9.

## Structure du projet

```
app/src/main/java/com/quest/app/
├── MainActivity.kt           # Header streak + pager 4 pages + overlays carte/check-in
├── core/
│   ├── Models.kt             # Quest, Poi, CheckIn, LoadState, QuestUiState…
│   ├── QuestRepository.kt    # Tirage déterministe, rareté, cache DataStore
│   ├── WikipediaApi.kt       # geosearch + extraits + photos
│   └── LocationService.kt    # Position + Geocoder (ville, centre-ville)
└── ui/
    ├── TodayPanel.kt         # Photo héro, chips, carte, états permission/erreur
    ├── QuestMap.kt           # Carte osmdroid, pin nature, plein écran
    ├── Celebration.kt        # Particules feuilles/soleil au check-in validé
    ├── WallPanel.kt          # Mur de photos (Coil)
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
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

L'APK debug se trouve dans `app/build/outputs/apk/debug/app-debug.apk`.

## Limites connues (v0.2)

- Mur, classement et validations : données de démo (pas de backend)
- Check-in photo simulé (pas encore de CameraX) — validation auto après 5 s
- Pas de notifications push

## Roadmap

- [ ] Backend API (mur, classement, validation par les pairs)
- [ ] CameraX + vérification présence au POI
- [ ] Notifications quest du jour
- [ ] Release Play Store
