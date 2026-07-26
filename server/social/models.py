from datetime import datetime
from typing import Optional

from pydantic import BaseModel
from sqlmodel import Field as SQLField, SQLModel

from timeutil import utcnow


class Reaction(SQLModel, table=True):
    id: Optional[int] = SQLField(default=None, primary_key=True)
    check_in_id: int = SQLField(index=True, foreign_key="checkin.id")
    user_id: int = SQLField(index=True, foreign_key="user.id")
    created_at: datetime = SQLField(default_factory=utcnow)


class WallPhoto(BaseModel):
    check_in_id: int
    username: str
    photo_url: str | None
    reactions: int
    status: str


class LeaderboardEntry(BaseModel):
    user_id: int
    username: str
    streak: int
    total_check_ins: int
    rank: int
    checked_today: bool
