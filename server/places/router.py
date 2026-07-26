from fastapi import APIRouter, HTTPException, Query
from pydantic import BaseModel, Field

from places.service import Rarity, pick_daily_place
from places.wikipedia import places_around
from datetime import date

router = APIRouter(prefix="/places", tags=["places"])


class PlacePreview(BaseModel):
    page_id: int
    title: str
    description: str
    latitude: float
    longitude: float
    photo_url: str | None
    rarity: Rarity


class DailyPickRequest(BaseModel):
    city_name: str = Field(min_length=1)
    center_lat: float
    center_lon: float
    day: str | None = None  # YYYY-MM-DD, défaut = aujourd'hui


@router.post("/daily", response_model=PlacePreview)
async def daily_place(req: DailyPickRequest):
    """Choisit le lieu du jour pour une ville (déterministe, partagé)."""
    day = date.fromisoformat(req.day) if req.day else date.today()
    places = await places_around(req.center_lat, req.center_lon)
    if not places:
        raise HTTPException(404, "Aucun lieu Wikipedia autour de ce centre-ville")
    place, rarity = pick_daily_place(req.city_name, places, day)
    return PlacePreview(
        page_id=place.page_id,
        title=place.title,
        description=place.description,
        latitude=place.latitude,
        longitude=place.longitude,
        photo_url=place.photo_url,
        rarity=rarity,
    )


@router.get("/around")
async def around(
    lat: float = Query(...),
    lon: float = Query(...),
):
    places = await places_around(lat, lon)
    return [
        {
            "page_id": p.page_id,
            "title": p.title,
            "latitude": p.latitude,
            "longitude": p.longitude,
            "photo_url": p.photo_url,
        }
        for p in places
    ]
