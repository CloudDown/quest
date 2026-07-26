from fastapi import APIRouter, Depends, HTTPException
from sqlmodel import Session, select

from auth.models import User
from auth.service import get_current_user
from db import get_session
from quests.models import CheckIn, CheckInStatus
from social.models import LeaderboardEntry, Reaction, WallPhoto
from social import service as social_service

router = APIRouter(prefix="/social", tags=["social"])


@router.get("/wall/{quest_id}", response_model=list[WallPhoto])
def wall(
    quest_id: int,
    session: Session = Depends(get_session),
    user: User = Depends(get_current_user),
):
    my = session.exec(
        select(CheckIn).where(CheckIn.quest_id == quest_id, CheckIn.user_id == user.id)
    ).first()
    unlocked = my is not None
    return social_service.wall_for_quest(session, quest_id, unlocked)


@router.get("/leaderboard", response_model=list[LeaderboardEntry])
def leaderboard(
    city: str | None = None,
    session: Session = Depends(get_session),
    user: User = Depends(get_current_user),
):
    return social_service.leaderboard(session, city or user.city_name)


@router.post("/check-ins/{check_in_id}/react")
def react(
    check_in_id: int,
    session: Session = Depends(get_session),
    user: User = Depends(get_current_user),
):
    check_in = session.get(CheckIn, check_in_id)
    if not check_in or check_in.status != CheckInStatus.VALIDATED.value:
        raise HTTPException(404, "Photo introuvable")
    existing = session.exec(
        select(Reaction).where(
            Reaction.check_in_id == check_in_id,
            Reaction.user_id == user.id,
        )
    ).first()
    if existing:
        return {"ok": True, "already": True}
    session.add(Reaction(check_in_id=check_in_id, user_id=user.id))
    session.commit()
    return {"ok": True, "already": False}
