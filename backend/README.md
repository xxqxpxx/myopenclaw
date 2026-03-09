# myOpenClaw Backend

AI agent backend with chat streaming, credit management, and user authentication.

## Quick Start

```bash
# Install dependencies
pip install -e ".[dev]"

# Copy env and fill in your keys
cp .env.example .env

# Run the database migration in Supabase SQL Editor
# → Copy migrations/001_initial_schema.sql into your Supabase project

# Start the server
uvicorn app.main:app --reload --port 8000
```

## API Endpoints

| Method | Endpoint                                 | Description          |
| ------ | ---------------------------------------- | -------------------- |
| GET    | `/health`                                | Health check         |
| GET    | `/api/v1/users/me`                       | Get user profile     |
| GET    | `/api/v1/users/me/credits`               | Get credit balance   |
| POST   | `/api/v1/conversations`                  | Create conversation  |
| GET    | `/api/v1/conversations`                  | List conversations   |
| GET    | `/api/v1/conversations/{id}`             | Get conversation     |
| DELETE | `/api/v1/conversations/{id}`             | Delete conversation  |
| GET    | `/api/v1/conversations/{id}/messages`    | List messages        |
| POST   | `/api/v1/conversations/{id}/chat/stream` | Stream AI chat (SSE) |

## Architecture

```
app/
├── main.py           # FastAPI app entry point
├── config.py         # Settings (env vars)
├── auth/jwt.py       # Supabase JWT validation
├── models/schemas.py # Pydantic models
├── db/supabase.py    # Database operations
├── services/llm.py   # Anthropic streaming + model routing
└── api/
    ├── conversations.py  # Chat & conversation endpoints
    └── users.py          # User profile & credits
```
