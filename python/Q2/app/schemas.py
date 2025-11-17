from typing import Optional
from pydantic import BaseModel, ConfigDict

class JokeBase(BaseModel):
    joke_id: int
    category: str
    type: str
    flag_nsfw: bool
    flag_political: bool
    flag_sexist: bool
    safe: bool
    lang: str
    joke: Optional[str] = None
    setup: Optional[str] = None
    delivery: Optional[str] = None

    model_config = ConfigDict(from_attributes=True)

class JokeResponse(BaseModel):
    requested: int
    fetched: int
    inserted: int
    updated: int
