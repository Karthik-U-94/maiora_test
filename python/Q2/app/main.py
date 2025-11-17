import asyncio
from typing import Dict, List

import httpx
from fastapi import Depends, FastAPI, HTTPException
from sqlalchemy.orm import Session

try:  # pragma: no cover - runtime environment dependent
    from .database import Base, engine, get_db
    from .models import Joke
    from .schemas import JokeResponse
except ImportError:  # pragma: no cover - fallback when executed as script
    from database import Base, engine, get_db
    from models import Joke
    from schemas import JokeResponse

Base.metadata.create_all(bind=engine)

app = FastAPI(title="Joke Importer API")

JOKE_API_BASE_URL = "https://v2.jokeapi.dev/joke"
MAX_JOKES_PER_REQUEST = 10
DEFAULT_TARGET_JOKES = 100

async def fetch_jokes_from_api(target: int) -> List[Dict]:
    jokes: List[Dict] = []
    async with httpx.AsyncClient(timeout=30.0) as client:
        while len(jokes) < target:
            amount = min(MAX_JOKES_PER_REQUEST, target - len(jokes))
            try:
                response = await client.get(
                    f"{JOKE_API_BASE_URL}/Any",
                    params={"amount": amount, "safe-mode": ""},
                )
                response.raise_for_status()
            except httpx.HTTPError as err:
                raise HTTPException(
                    status_code=502,
                    detail=f"Failed to fetch jokes from JokeAPI: {err}",
                ) from err

            payload = response.json()
            batch = []
            if payload.get("error"):
                raise HTTPException(status_code=502, detail="JokeAPI returned an error response")

            if "jokes" in payload:
                batch = payload["jokes"]
            else:
                batch = [payload]

            jokes.extend(batch)

            if not batch:
                break

    return jokes

def transform_joke(raw: Dict) -> Dict:
    flags = raw.get("flags", {})
    transformed = {
        "joke_id": raw.get("id"),
        "category": raw.get("category"),
        "type": raw.get("type"),
        "flag_nsfw": flags.get("nsfw", False),
        "flag_political": flags.get("political", False),
        "flag_sexist": flags.get("sexist", False),
        "safe": raw.get("safe", True),
        "lang": raw.get("lang"),
        "joke": None,
        "setup": None,
        "delivery": None,
    }

    if transformed["type"] == "single":
        transformed["joke"] = raw.get("joke")
    elif transformed["type"] == "twopart":
        transformed["setup"] = raw.get("setup")
        transformed["delivery"] = raw.get("delivery")

    return transformed

@app.post("/jokes/import", response_model=JokeResponse)
async def import_jokes(target: int = DEFAULT_TARGET_JOKES, db: Session = Depends(get_db)):
    if target < 1:
        raise HTTPException(status_code=400, detail="Target must be at least 1")

    raw_jokes = await fetch_jokes_from_api(target)

    unique_raw_jokes = []
    seen_ids = set()
    for raw in raw_jokes:
        joke_id = raw.get("id")
        if joke_id is None or joke_id in seen_ids:
            continue
        seen_ids.add(joke_id)
        unique_raw_jokes.append(raw)

    processed = [transform_joke(raw) for raw in unique_raw_jokes]

    inserted = 0
    updated = 0
    for joke_data in processed:
        existing = db.query(Joke).filter(Joke.joke_id == joke_data["joke_id"]).first()
        if existing:
            for key, value in joke_data.items():
                setattr(existing, key, value)
            updated += 1
        else:
            db.add(Joke(**joke_data))
            inserted += 1
    db.commit()

    return JokeResponse(
        requested=target,
        fetched=len(processed),
        inserted=inserted,
        updated=updated,
    )
