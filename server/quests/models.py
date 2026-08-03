from datetime import date, datetime
from enum import Enum
from typing import Optional

from pydantic import BaseModel, Field
from sqlmodel import Field as SQLField, SQLModel

from timeutil import utcnow


class Rarity(str, Enum):
    COMMON = "COMMON"
    RARE = "RARE"
    LEGENDARY = "LEGENDARY"


class CheckInStatus(str, Enum):
    PENDING = "PENDING"
    VALIDATED = "VALIDATED"
    REJECTED = "REJECTED"


class DailyQuest(SQLModel, table=True):
    """Lieu du jour figé pour une ville (source de vérité partagée)."""
    id: Optional[int] = SQLField(default=None, primary_key=True)
    date: str = SQLField(index=True)  # YYYY-MM-DD
    city_name: str = SQLField(index=True)
    city_key: str = SQLField(index=True)  # lowercase slug
    poi_page_id: str
    poi_name: str
    poi_description: str
    latitude: float
    longitude: float
    rarity: str = Rarity.COMMON.value
    photo_url: Optional[str] = None
    created_at: datetime = SQLField(default_factory=utcnow)


class CheckIn(SQLModel, table=True):
    id: Optional[int] = SQLField(default=None, primary_key=True)
    quest_id: int = SQLField(index=True, foreign_key="dailyquest.id")
    user_id: int = SQLField(index=True, foreign_key="user.id")
    photo_path: Optional[str] = None
    status: str = CheckInStatus.PENDING.value
    validated_by: Optional[int] = None
    submitted_at: datetime = SQLField(default_factory=utcnow)


class TodayRequest(BaseModel):
    city_name: str = Field(min_length=1)
    center_lat: float
    center_lon: float
    user_lat: float | None = None
    user_lon: float | None = None


class QuestPublic(BaseModel):
    id: int
    date: str
    city_name: str
    poi_name: str
    poi_description: str
    latitude: float
    longitude: float
    rarity: str
    photo_url: str | None
    distance_meters: float | None = None


class CheckInPublic(BaseModel):
    id: int
    quest_id: int
    user_id: int
    status: str
    validated_by: int | None
    submitted_at: datetime


class SubmitCheckInRequest(BaseModel):
    """URL photo optionnelle (ex. Wikipedia du lieu en attendant la caméra)."""
    photo_url: str | None = None


class SubmitCheckInResponse(BaseModel):
    check_in: CheckInPublic
