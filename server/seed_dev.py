"""Comptes de démo locaux."""
from sqlmodel import Session, select

from auth.models import User
from auth.service import hash_password
from db import engine


def seed():
    accounts = [
        ("rain@quest.local", "rain", "rain"),
        ("alex@quest.local", "alex", "alex"),
        ("juju@quest.local", "juju", "juju"),
        ("marion@quest.local", "marion", "marion"),
    ]
    with Session(engine) as session:
        for email, username, password in accounts:
            existing = session.exec(select(User).where(User.username == username)).first()
            if existing:
                continue
            session.add(
                User(
                    email=email,
                    username=username,
                    hashed_password=hash_password(password),
                    streak=4 if username == "rain" else 2,
                    total_check_ins=12 if username == "rain" else 5,
                )
            )
        session.commit()
    print("🌱 Seed users: rain/rain, alex/alex, juju/juju, marion/marion")


if __name__ == "__main__":
    seed()
