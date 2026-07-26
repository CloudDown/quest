from datetime import datetime
from typing import Optional

from pydantic import BaseModel, Field
from sqlmodel import Field as SQLField, SQLModel

from timeutil import utcnow


class User(SQLModel, table=True):
    id: Optional[int] = SQLField(default=None, primary_key=True)
    email: str = SQLField(index=True, unique=True)
    username: str = SQLField(index=True, unique=True)
    hashed_password: str
    city_name: Optional[str] = None
    streak: int = 0
    total_check_ins: int = 0
    created_at: datetime = SQLField(default_factory=utcnow)


class RegisterRequest(BaseModel):
    email: str
    username: str = Field(min_length=2, max_length=32)
    password: str = Field(min_length=4)


class LoginRequest(BaseModel):
    email: str  # email ou username
    password: str


class TokenResponse(BaseModel):
    access_token: str
    token_type: str = "bearer"
    user_id: int
    username: str


class UserPublic(BaseModel):
    id: int
    username: str
    city_name: Optional[str] = None
    streak: int
    total_check_ins: int
