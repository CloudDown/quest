"""Tirage déterministe du lieu du jour pour une ville."""
from __future__ import annotations

import random
from datetime import date
from enum import Enum

from places.wikipedia import WikiPlace


class Rarity(str, Enum):
    COMMON = "COMMON"
    RARE = "RARE"
    LEGENDARY = "LEGENDARY"


def rarity_for(city_name: str, day: date) -> Rarity:
    city = city_name.lower()
    legendary_day = random.Random(f"{city}|{day.year}-{day.month}").randint(1, _days_in_month(day))
    if day.day == legendary_day:
        return Rarity.LEGENDARY

    week = day.isocalendar().week
    rare_day = random.Random(f"{city}|{day.year}-w{week}").randint(1, 7)
    if day.isoweekday() == rare_day:
        return Rarity.RARE
    return Rarity.COMMON


def _days_in_month(day: date) -> int:
    if day.month == 12:
        nxt = date(day.year + 1, 1, 1)
    else:
        nxt = date(day.year, day.month + 1, 1)
    return (nxt - date(day.year, day.month, 1)).days


def pick_daily_place(city_name: str, places: list[WikiPlace], day: date) -> tuple[WikiPlace, Rarity]:
    rarity = rarity_for(city_name, day)
    sorted_places = sorted(places, key=lambda p: p.page_id)
    candidates = sorted_places
    if rarity != Rarity.COMMON:
        with_photo = [p for p in sorted_places if p.photo_url]
        if with_photo:
            candidates = with_photo
    seed = f"{city_name.lower()}|{day.isoformat()}"
    place = candidates[random.Random(seed).randint(0, len(candidates) - 1)]
    return place, rarity
