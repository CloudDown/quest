from fastapi import APIRouter, Depends
from sqlmodel import Session, select

from auth.models import User
from auth.service import get_current_user
from db import get_session
from quests.models import (
    CheckIn,
    CheckInPublic,
    SubmitCheckInResponse,
    TodayRequest,
    QuestPublic,
)
from quests import service as quest_service

router = APIRouter(prefix="/quests", tags=["quests"])


@router.post("/today", response_model=QuestPublic)
async def today(
    req: TodayRequest,
    session: Session = Depends(get_session),
    user: User = Depends(get_current_user),
):
    """Lieu du jour pour la ville (créé une fois, partagé par tous)."""
    return await quest_service.get_or_create_today_quest(
        session,
        city_name=req.city_name,
        center_lat=req.center_lat,
        center_lon=req.center_lon,
        user_lat=req.user_lat,
        user_lon=req.user_lon,
    )


@router.get("/today/mine", response_model=CheckInPublic | None)
def my_check_in(
    session: Session = Depends(get_session),
    user: User = Depends(get_current_user),
):
    from datetime import date
    from quests.models import DailyQuest

    day = date.today().isoformat()
    quest = session.exec(
        select(DailyQuest).where(
            DailyQuest.date == day,
            DailyQuest.city_name == (user.city_name or ""),
        )
    ).first()
    if not quest:
        # Cherche n'importe quel check-in du jour pour cet user
        check_ins = session.exec(select(CheckIn).where(CheckIn.user_id == user.id)).all()
        for c in reversed(check_ins):
            q = session.get(DailyQuest, c.quest_id)
            if q and q.date == day:
                return CheckInPublic(
                    id=c.id,
                    quest_id=c.quest_id,
                    user_id=c.user_id,
                    status=c.status,
                    validated_by=c.validated_by,
                    submitted_at=c.submitted_at,
                )
        return None
    c = session.exec(
        select(CheckIn).where(CheckIn.quest_id == quest.id, CheckIn.user_id == user.id)
    ).first()
    if not c:
        return None
    return CheckInPublic(
        id=c.id,
        quest_id=c.quest_id,
        user_id=c.user_id,
        status=c.status,
        validated_by=c.validated_by,
        submitted_at=c.submitted_at,
    )


@router.post("/{quest_id}/check-in", response_model=SubmitCheckInResponse)
def submit_check_in(
    quest_id: int,
    session: Session = Depends(get_session),
    user: User = Depends(get_current_user),
):
    c = quest_service.submit_check_in(session, user, quest_id, photo_path=None)
    return SubmitCheckInResponse(
        check_in=CheckInPublic(
            id=c.id,
            quest_id=c.quest_id,
            user_id=c.user_id,
            status=c.status,
            validated_by=c.validated_by,
            submitted_at=c.submitted_at,
        )
    )


@router.post("/check-ins/{check_in_id}/validate", response_model=CheckInPublic)
def validate(
    check_in_id: int,
    session: Session = Depends(get_session),
    user: User = Depends(get_current_user),
):
    c = quest_service.validate_check_in(session, user, check_in_id)
    return CheckInPublic(
        id=c.id,
        quest_id=c.quest_id,
        user_id=c.user_id,
        status=c.status,
        validated_by=c.validated_by,
        submitted_at=c.submitted_at,
    )
