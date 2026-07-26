from fastapi import APIRouter, Depends, HTTPException, status
from sqlmodel import Session, select

from auth.models import LoginRequest, RegisterRequest, TokenResponse, User, UserPublic
from auth.service import create_token, get_current_user, hash_password, verify_password
from db import get_session

router = APIRouter(prefix="/auth", tags=["auth"])


@router.post("/register", response_model=TokenResponse, status_code=status.HTTP_201_CREATED)
def register(req: RegisterRequest, session: Session = Depends(get_session)):
    if session.exec(select(User).where(User.email == req.email)).first():
        raise HTTPException(400, "Email déjà utilisé")
    if session.exec(select(User).where(User.username == req.username)).first():
        raise HTTPException(400, "Username déjà utilisé")

    user = User(
        email=req.email.strip().lower(),
        username=req.username.strip(),
        hashed_password=hash_password(req.password),
    )
    session.add(user)
    session.commit()
    session.refresh(user)
    return TokenResponse(
        access_token=create_token(user.id),
        user_id=user.id,
        username=user.username,
    )


@router.post("/login", response_model=TokenResponse)
def login(req: LoginRequest, session: Session = Depends(get_session)):
    identifiant = req.email.strip()
    user = session.exec(select(User).where(User.email == identifiant.lower())).first()
    if not user:
        user = session.exec(select(User).where(User.username == identifiant)).first()
    if not user or not verify_password(req.password, user.hashed_password):
        raise HTTPException(401, "Identifiants incorrects")
    return TokenResponse(
        access_token=create_token(user.id),
        user_id=user.id,
        username=user.username,
    )


@router.get("/me", response_model=UserPublic)
def me(user: User = Depends(get_current_user)):
    return UserPublic(
        id=user.id,
        username=user.username,
        city_name=user.city_name,
        streak=user.streak,
        total_check_ins=user.total_check_ins,
    )
