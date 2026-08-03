"""
Configuration Quest — variables d'environnement avec défauts locaux.

Préfixe : QUEST_*
"""
import os
from pathlib import Path

_DEFAULT_SECRET_KEY = "quest-dev-secret-CHANGE-ME-in-prod"
SECRET_KEY = os.getenv("QUEST_SECRET_KEY", _DEFAULT_SECRET_KEY)
ALGORITHM = "HS256"
ACCESS_TOKEN_EXPIRE_MINUTES = int(os.getenv("QUEST_TOKEN_EXPIRE_MINUTES", str(60 * 24 * 30)))

CORS_ORIGINS = [o.strip() for o in os.getenv("QUEST_CORS_ORIGINS", "*").split(",") if o.strip()]

DATABASE_URL = os.getenv("QUEST_DATABASE_URL", "sqlite:///./quest.db")

UPLOAD_DIR = Path(os.getenv("QUEST_UPLOAD_DIR", "./uploads"))

DEMO_MODE = os.getenv("QUEST_DEMO_MODE", "1").strip().lower() in {"1", "true", "yes", "on"}
# Pas de comptes fictifs par défaut — l’app crée un compte appareil.
DEMO_AUTO_SEED = os.getenv("QUEST_DEMO_AUTO_SEED", "0").strip().lower() in {"1", "true", "yes", "on"}

# Rayon Wikipedia autour du centre-ville (mètres, max API = 10_000)
WIKI_RADIUS_M = int(os.getenv("QUEST_WIKI_RADIUS_M", "10000"))
WIKI_LIMIT = int(os.getenv("QUEST_WIKI_LIMIT", "50"))
WIKI_USER_AGENT = os.getenv("QUEST_WIKI_USER_AGENT", "QuestApp/0.2 (server; contact@quest.local)")

if not DEMO_MODE and SECRET_KEY == _DEFAULT_SECRET_KEY:
    raise RuntimeError(
        "QUEST_SECRET_KEY doit être défini quand QUEST_DEMO_MODE=0 "
        "(la clé de développement par défaut est interdite hors démo)."
    )
