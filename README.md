# Quest

> Un rendez-vous quotidien dans ta ville. Un lieu. Tout le monde. Aujourd'hui.

Spec produit : [PRODUCT.md](PRODUCT.md)

## Structure (inspirée de Vif)

```
quest/
├── server/          # API FastAPI — lieu du jour, social, auth
├── mobile_app/      # Android Kotlin + Compose
├── apps/mobile/     # ancien prototype Expo (legacy)
├── packages/        # partagé éventuel
├── PRODUCT.md
├── AGENTS.md
└── bin/adb          # install + launch en une commande
```

## Backend

```bash
cd server
python3 -m venv .venv
.venv/bin/pip install -r requirements.txt
.venv/bin/uvicorn main:app --reload --host 0.0.0.0 --port 8000
```

- Docs : http://localhost:8000/docs
- Health : http://localhost:8000/health
- Mode démo par défaut (`QUEST_DEMO_MODE=1`) avec comptes seed : `rain/rain`, `alex/alex`…

### Modules serveur

| Module | Rôle |
|--------|------|
| `auth/` | Inscription, login JWT, profil |
| `places/` | Wikipedia geosearch + tirage déterministe ville+date |
| `quests/` | Quest du jour figé en DB, check-in, validation par les pairs |
| `social/` | Mur de photos, leaderboard, réactions |

Routers fins + logique dans `*/service.py` (comme Vif).

## Mobile

```bash
cd mobile_app
./configure-device-api.sh usb   # ou lan
./gradlew assembleDebug
# depuis la racine du repo :
adb install app/build/outputs/apk/debug/app-debug.apk
```

L’URL API est lue depuis `mobile_app/local.properties` → `quest.api.base.url`.

## Qui fait quoi (MVP actuel)

| Responsabilité | Où |
|----------------|-----|
| Choix du lieu (Wikipedia, seed ville+date) | **Serveur** (`places/` + `quests/`) — aussi encore en local côté app le temps de brancher l’API |
| Social (mur, classement, validations) | **Serveur** (`social/`, `quests/`) |
| GPS → nom de ville | **Mobile** (Geocoder) puis envoi centre-ville au serveur |
| UI, carte OSM, thème | **Mobile** |
