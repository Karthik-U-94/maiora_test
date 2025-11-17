from sqlalchemy import Column, String, Integer, Boolean

try:  # pragma: no cover - runtime environment dependent
    from .database import Base
except ImportError:  # pragma: no cover - fallback when executed as script
    from database import Base

class Joke(Base):
    __tablename__ = "jokes"

    id = Column(Integer, primary_key=True, index=True, autoincrement=True)
    joke_id = Column(Integer, unique=True, index=True, nullable=False)
    category = Column(String, nullable=False)
    type = Column(String, nullable=False)
    joke = Column(String, nullable=True)
    setup = Column(String, nullable=True)
    delivery = Column(String, nullable=True)
    flag_nsfw = Column(Boolean, default=False)
    flag_political = Column(Boolean, default=False)
    flag_sexist = Column(Boolean, default=False)
    safe = Column(Boolean, default=True)
    lang = Column(String, nullable=False)
