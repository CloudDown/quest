# AGENTS.md

## Repo Shape
- `server/` — backend FastAPI live ; entrypoint `server/main.py`, config `server/config.py`, SQLite `server/quest.db` par défaut.
- `mobile_app/` — app Android : Kotlin + Compose, Gradle wrapper, module unique `:app`.
- `PRODUCT.md` — spec produit.

## Backend Commands
- API locale :
  ```bash
  cd server && python3 -m venv .venv && .venv/bin/pip install -r requirements.txt
  .venv/bin/uvicorn main:app --reload --host 0.0.0.0 --port 8000
  ```
- Seed comptes démo : `.venv/bin/python seed_dev.py` (aussi au démarrage si `QUEST_DEMO_AUTO_SEED=1`).
- Smoke import : `.venv/bin/python -c "import main; print('ok')"`.

## Backend Structure
- Routers fins ; logique métier dans `*/service.py`.
- `places/` : client Wikipedia + tirage déterministe `ville + date` + rareté calendaire.
- `quests/` : persistance du quest du jour par ville, check-in, validation pair-à-pair.
- `social/` : mur (déverrouillé après check-in), leaderboard, réactions.
- `auth/` : register / login JWT / me.
- Timestamps via `timeutil.utcnow()` (UTC naïf).
- Config via env `QUEST_*` (pas de python-dotenv requis).

## Backend Gotchas
- Hors démo (`QUEST_DEMO_MODE=0`), `QUEST_SECRET_KEY` doit être défini.
- Wikipedia exige un User-Agent (`QUEST_WIKI_USER_AGENT`).
- Le lieu du jour est créé au premier `POST /quests/today` pour une ville+date, puis figé.

## Mobile Commands
- Configurer l’URL API : `cd mobile_app && ./configure-device-api.sh lan` (ou `usb`, `emulator`, `url`).
- Release APK : `cd mobile_app && ./release-github.sh`
- Build seul : `cd mobile_app && ./gradlew assembleDebug`
- Install + launch depuis la racine : `adb install …` (wrapper `bin/adb`)

## Mobile Structure
- Mono-module Dispo-like : `core/` (models, repository, location, wiki client local) + `ui/`.
- `BuildConfig.API_BASE_URL` / `QuestApi` pointent vers le serveur local.
- Thème clair/sombre persisté (DataStore) ; page Paramètres.

## Mobile Gotchas
- Coil figé en **3.2.0** (Kotlin 2.4 incompatible avec le compilateur AGP 9 / KGP 2.2.10).
- `compileSdk` AGP 9 : `compileSdk { version = release(36) { minorApiLevel = 1 } }`.
- Ouvrir **`mobile_app/`** dans Android Studio (pas la racine du repo).
