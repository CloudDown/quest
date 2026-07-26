"""
Client Wikipedia : geosearch autour d'un centre-ville + extraits/photos.
Le tirage déterministe (ville + date) vit dans places/service.py.
"""
from __future__ import annotations

from dataclasses import dataclass
from typing import Optional

import httpx

from config import WIKI_LIMIT, WIKI_RADIUS_M, WIKI_USER_AGENT


@dataclass
class WikiPlace:
    page_id: int
    title: str
    description: str
    latitude: float
    longitude: float
    photo_url: Optional[str]


def _lang(prefer_fr: bool = True) -> str:
    return "fr" if prefer_fr else "en"


async def places_around(
    latitude: float,
    longitude: float,
    *,
    radius_m: int = WIKI_RADIUS_M,
    limit: int = WIKI_LIMIT,
    prefer_fr: bool = True,
) -> list[WikiPlace]:
    lang = _lang(prefer_fr)
    radius = min(radius_m, 10_000)
    headers = {"User-Agent": WIKI_USER_AGENT}

    async with httpx.AsyncClient(timeout=15.0, headers=headers) as client:
        geo_url = (
            f"https://{lang}.wikipedia.org/w/api.php"
            f"?action=query&list=geosearch"
            f"&gscoord={latitude}|{longitude}"
            f"&gsradius={radius}&gslimit={limit}&format=json"
        )
        geo = (await client.get(geo_url)).json()
        results = geo.get("query", {}).get("geosearch", [])
        if not results:
            # Fallback EN si FR vide
            if prefer_fr:
                return await places_around(
                    latitude, longitude, radius_m=radius_m, limit=limit, prefer_fr=False,
                )
            return []

        coords = {item["pageid"]: (item["lat"], item["lon"]) for item in results}
        page_ids = "|".join(str(pid) for pid in coords)
        detail_url = (
            f"https://{lang}.wikipedia.org/w/api.php"
            f"?action=query&pageids={page_ids}"
            f"&prop=extracts|pageimages"
            f"&exintro=1&explaintext=1&exsentences=3&exlimit=max"
            f"&piprop=thumbnail&pithumbsize=1000&pilimit=max&format=json"
        )
        detail = (await client.get(detail_url)).json()
        pages = detail.get("query", {}).get("pages", {})

        places: list[WikiPlace] = []
        for page_id, (lat, lon) in coords.items():
            page = pages.get(str(page_id)) or pages.get(page_id)
            if not page:
                continue
            extract = (page.get("extract") or "").strip()
            if not extract:
                continue
            thumb = (page.get("thumbnail") or {}).get("source")
            places.append(
                WikiPlace(
                    page_id=int(page_id),
                    title=page.get("title") or f"page-{page_id}",
                    description=extract,
                    latitude=float(lat),
                    longitude=float(lon),
                    photo_url=thumb,
                )
            )
        return places
