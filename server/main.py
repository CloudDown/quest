"""
Quest — API Backend
===================
Lancer : ./server.sh  (port 8001)
Doc     : http://localhost:8001/docs
"""
from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

import auth.models  # noqa: F401
import quests.models  # noqa: F401
import social.models  # noqa: F401

from config import CORS_ORIGINS, DEMO_AUTO_SEED, DEMO_MODE, UPLOAD_DIR
from db import create_tables

from auth.router import router as auth_router
from places.router import router as places_router
from quests.router import router as quests_router
from social.router import router as social_router


@asynccontextmanager
async def lifespan(app: FastAPI):
    UPLOAD_DIR.mkdir(parents=True, exist_ok=True)
    create_tables()
    if DEMO_MODE and DEMO_AUTO_SEED:
        from seed_dev import seed
        seed()
        print("🎬 Mode démo actif — comptes seed chargés")
    print("✅ Quest API démarrée — http://localhost:8001/docs")
    yield


app = FastAPI(title="Quest API", version="0.2.0", lifespan=lifespan)

app.add_middleware(
    CORSMiddleware,
    allow_origins=CORS_ORIGINS if CORS_ORIGINS != ["*"] else ["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth_router)
app.include_router(places_router)
app.include_router(quests_router)
app.include_router(social_router)


@app.get("/health")
def health():
    return {"ok": True, "service": "quest", "demo": DEMO_MODE}


@app.get("/config")
def public_config():
    return {
        "demo_mode": DEMO_MODE,
        "features": {
            "peer_validation": True,
            "wall": True,
            "leaderboard": True,
            "wikipedia_places": True,
        },
    }
