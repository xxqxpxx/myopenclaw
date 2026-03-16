---
name: api-endpoint
description: Scaffold a new FastAPI endpoint following the myopenclaw pattern: router in api/, Pydantic schema in models/schemas.py, DB function in db/supabase.py, wired with JWT auth. Usage: /api-endpoint <METHOD> <path> [description]
disable-model-invocation: true
---

# API Endpoint Scaffolder

Generate a complete FastAPI endpoint following the myopenclaw conventions.

## Pattern to follow

The project structure is:
- `backend/app/api/<router>.py` — FastAPI routers mounted at `/api/v1`
- `backend/app/models/schemas.py` — Pydantic request/response models
- `backend/app/db/supabase.py` — All DB operations using Supabase service role
- Auth: `AuthenticatedUser` dependency from `app/auth/jwt.py` injected into every protected route

## Steps

Given `<METHOD> <path>` (e.g., `POST /conversations/{id}/messages`):

1. **Add Pydantic schemas** to `backend/app/models/schemas.py`:
   - Request model: `<ResourceName>Request` with relevant fields
   - Response model: `<ResourceName>Response` matching expected DB columns

2. **Add DB function** to `backend/app/db/supabase.py`:
   - Async function using the supabase client
   - Follow existing pattern: `supabase.table(...).insert/select/update(...).execute()`
   - Return typed dict or raise HTTPException on error

3. **Add route** to the appropriate `backend/app/api/<router>.py`:
   - Import `AuthenticatedUser` and `get_current_user` from `app.auth.jwt`
   - Add `current_user: AuthenticatedUser = Depends(get_current_user)`
   - Validate ownership if accessing user-specific resources
   - Return response model

4. **Show a summary** of all files modified and the new endpoint path.

## Example output structure

```python
# models/schemas.py addition
class MessageRequest(BaseModel):
    content: str
    role: str = "user"

class MessageResponse(BaseModel):
    id: str
    content: str
    role: str
    created_at: str

# db/supabase.py addition
async def create_message(conversation_id: str, user_id: str, content: str) -> dict:
    ...

# api/conversations.py addition
@router.post("/{conversation_id}/messages", response_model=MessageResponse)
async def create_message(
    conversation_id: str,
    body: MessageRequest,
    current_user: AuthenticatedUser = Depends(get_current_user),
):
    ...
```
