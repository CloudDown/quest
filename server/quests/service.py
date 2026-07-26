"""Logique quest du jour + check-in."""
from __future__ import annotations

import math
from datetime import date

from fastapi import HTTPException
from sqlmodel import Session, select

from auth.models import User
from places.service import pick_daily_place
from places.wikipedia import places_around
from quests.models import CheckIn, CheckInStatus, DailyQuest, QuestPublic


def _city_key(name: str) -> str:
    return name.strip().lower()


def _haversine_m(lat1: float, lon1: float, lat2: float, lon2: float) -> float:
    r = 6_371_000.0
    p1, p2 = math.radians(lat1), math.radians(lat2)
    dp = math.radians(lat2 - lat1)
    dl = math.radians(lon2 - lon1)
    a = math.sin(dp / 2) ** 2 + math.cos(p1) * math.cos(p2) * math.sin(dl / 2) ** 2
    return 2 * r * math.asin(math.sqrt(a))


async def get_or_create_today_quest(
    session: Session,
    *,
    city_name: str,
    center_lat: float,
    center_lon: float,
    user_lat: float | None = None,
    user_lon: float | None = None,
    day: date | None = None,
) -> QuestPublic:
    day = day or date.today()
    key = _city_key(city_name)
    day_s = day.isoformat()

    existing = session.exec(
        select(DailyQuest).where(DailyQuest.city_key == key, DailyQuest.date == day_s)
    ).first()

    if not existing:
        places = await places_around(center_lat, center_lon)
        if not places:
            raise HTTPException(404, "Aucun lieu Wikipedia trouvé pour cette ville")
        place, rarity = pick_daily_place(city_name, places, day)
        existing = DailyQuest(
            date=day_s,
            city_name=city_name.strip(),
            city_key=key,
            poi_page_id=str(place.page_id),
            poi_name=place.title,
            poi_description=place.description,
            latitude=place.latitude,
            longitude=place.longitude,
            rarity=rarity.value,
            photo_url=place.photo_url,
        )
        session.add(existing)
        session.commit()
        session.refresh(existing)

    distance = None
    if user_lat is not None and user_lon is not None:
        distance = _haversine_m(user_lat, user_lon, existing.latitude, existing.longitude)

    return QuestPublic(
        id=existing.id,
        date=existing.date,
        city_name=existing.city_name,
        poi_name=existing.poi_name,
        poi_description=existing.poi_description,
        latitude=existing.latitude,
        longitude=existing.longitude,
        rarity=existing.rarity,
        photo_url=existing.photo_url,
        distance_meters=distance,
    )


def submit_check_in(session: Session, user: User, quest_id: int, photo_path: str | None) -> CheckIn:
    quest = session.get(DailyQuest, quest_id)
    if not quest:
        raise HTTPException(404, "Quest introuvable")
    if quest.date != date.today().isoformat():
        raise HTTPException(400, "Check-in uniquement pour le quest du jour")

    existing = session.exec(
        select(CheckIn).where(CheckIn.quest_id == quest_id, CheckIn.user_id == user.id)
    ).first()
    if existing:
        return existing

    check_in = CheckIn(
        quest_id=quest_id,
        user_id=user.id,
        photo_path=photo_path,
        status=CheckInStatus.PENDING.value,
    )
    session.add(check_in)
    if user.city_name != quest.city_name:
        user.city_name = quest.city_name
        session.add(user)
    session.commit()
    session.refresh(check_in)
    return check_in


def validate_check_in(session: Session, validator: User, check_in_id: int) -> CheckIn:
    check_in = session.get(CheckIn, check_in_id)
    if not check_in:
        raise HTTPException(404, "Check-in introuvable")
    if check_in.user_id == validator.id:
        raise HTTPException(400, "Tu ne peux pas valider ton propre check-in")
    if check_in.status != CheckInStatus.PENDING.value:
        raise HTTPException(400, "Ce check-in n'est plus en attente")

    # Le validateur doit avoir lui-même checké ce quest
    mine = session.exec(
        select(CheckIn).where(
            CheckIn.quest_id == check_in.quest_id,
            CheckIn.user_id == validator.id,
        )
    ).first()
    if not mine:
        raise HTTPException(403, "Tu dois avoir checké aujourd'hui pour valider")

    check_in.status = CheckInStatus.VALIDATED.value
    check_in.validated_by = validator.id
    owner = session.get(User, check_in.user_id)
    if owner:
        owner.total_check_ins += 1
        owner.streak += 1
        session.add(owner)
    session.add(check_in)
    session.commit()
    session.refresh(check_in)
    return check_in
