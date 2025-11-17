# Joke Importer API

## Setup
1. **Create virtual environment (optional)**
   ```bash
   python -m venv .venv
   .venv\Scripts\activate
   ```
2. **Install dependencies**
   ```bash
   pip install -r requirements.txt
   ```

## Running the API
```bash
uvicorn app.main:app --reload
```

The server starts at `http://127.0.0.1:8000`.

## Usage
Trigger the joke import:
```bash
curl -X POST "http://127.0.0.1:8000/jokes/import?target=150"
```

- **requested**: target provided.
- **fetched**: jokes fetched and processed.
- **inserted**: new jokes persisted.
- **updated**: existing jokes updated with latest data.

Data is stored in `jokes.db` (SQLite).
