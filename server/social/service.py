from datetime import date

from sqlmodel import Session, func, select

from auth.models import User
from quests.models import CheckIn, CheckInStatus, DailyQuest
from social.models import LeaderboardEntry, Reaction, WallPhoto


def wall_for_quest(session: Session, quest_id: int, unlocked: bool) -> list[WallPhoto]:
    if not unlocked:
        return []
    check_ins = session.exec(
        select(CheckIn).where(
            CheckIn.quest_id == quest_id,
            CheckIn.status == CheckInStatus.VALIDATED.value,
        )
    ).all()
    photos: list[WallPhoto] = []
    for c in check_ins:
        user = session.get(User, c.user_id)
        if not user:
            continue
        reactions = session.exec(
            select(func.count()).select_from(Reaction).where(Reaction.check_in_id == c.id)
        ).one()
        photos.append(
            WallPhoto(
                check_in_id=c.id,
                username=user.username,
                photo_url=c.photo_path,
                reactions=int(reactions or 0),
                status=c.status,
            )
        )
    return photos


def leaderboard(session: Session, city_name: str | None) -> list[LeaderboardEntry]:
    # Pas de ville = pas de liste (évite d'afficher des comptes seed / hors contexte)
    if not city_name:
        return []
    users = session.exec(select(User).where(User.city_name == city_name)).all()
    users = sorted(users, key=lambda u: (-u.streak, -u.total_check_ins, u.username))
    day = date.today().isoformat()
    entries: list[LeaderboardEntry] = []
    for i, user in enumerate(users, start=1):
        checked = False
        if user.city_name:
            quest = session.exec(
                select(DailyQuest).where(
                    DailyQuest.date == day,
                    DailyQuest.city_name == user.city_name,
                )
            ).first()
            if quest:
                checked = session.exec(
                    select(CheckIn).where(
                        CheckIn.quest_id == quest.id,
                        CheckIn.user_id == user.id,
                    )
                ).first() is not None
        entries.append(
            LeaderboardEntry(
                user_id=user.id,
                username=user.username,
                streak=user.streak,
                total_check_ins=user.total_check_ins,
                rank=i,
                checked_today=checked,
            )
        )
    return entries
