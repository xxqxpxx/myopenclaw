"""Conversation and chat API endpoints."""

from __future__ import annotations

from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.responses import StreamingResponse

from app.auth.jwt import AuthenticatedUser, get_current_user
from app.db import supabase as db
from app.models.schemas import (
    ConversationResponse,
    CreateConversationRequest,
    MessageResponse,
    MessageRole,
    SendMessageRequest,
)
from app.services.llm import route_model, estimate_credits, calculate_cost
from app.services.bridge import stream_agent_response
from app.services.byok import resolve_byok_key
from app.services.tiers import is_model_allowed
from app.services.memory import build_memory_context

router = APIRouter(prefix="/conversations", tags=["conversations"])


# ── Conversations CRUD ─────────────────────────────────────────────────────

@router.post("", response_model=ConversationResponse, status_code=status.HTTP_201_CREATED)
async def create_conversation(
    body: CreateConversationRequest,
    user: AuthenticatedUser = Depends(get_current_user),
):
    """Create a new conversation."""
    row = await db.create_conversation(user.user_id, body.title)
    return ConversationResponse(**row)


@router.get("", response_model=list[ConversationResponse])
async def list_conversations(
    limit: int = 50,
    offset: int = 0,
    user: AuthenticatedUser = Depends(get_current_user),
):
    """List all conversations for the authenticated user."""
    rows = await db.get_conversations(user.user_id, limit=limit, offset=offset)
    return [ConversationResponse(**r) for r in rows]


@router.get("/{conversation_id}", response_model=ConversationResponse)
async def get_conversation(
    conversation_id: str,
    user: AuthenticatedUser = Depends(get_current_user),
):
    """Get a single conversation."""
    row = await db.get_conversation(conversation_id, user.user_id)
    if not row:
        raise HTTPException(status_code=404, detail="Conversation not found")
    return ConversationResponse(**row)


@router.delete("/{conversation_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_conversation(
    conversation_id: str,
    user: AuthenticatedUser = Depends(get_current_user),
):
    """Delete a conversation and its messages."""
    deleted = await db.delete_conversation(conversation_id, user.user_id)
    if not deleted:
        raise HTTPException(status_code=404, detail="Conversation not found")


# ── Messages ───────────────────────────────────────────────────────────────

@router.get("/{conversation_id}/messages", response_model=list[MessageResponse])
async def list_messages(
    conversation_id: str,
    limit: int = 100,
    offset: int = 0,
    user: AuthenticatedUser = Depends(get_current_user),
):
    """Get all messages in a conversation."""
    # Verify ownership
    conv = await db.get_conversation(conversation_id, user.user_id)
    if not conv:
        raise HTTPException(status_code=404, detail="Conversation not found")
    rows = await db.get_messages(conversation_id, limit=limit, offset=offset)
    return [MessageResponse(**r) for r in rows]


# ── Chat Streaming ─────────────────────────────────────────────────────────

@router.post("/{conversation_id}/chat/stream")
async def chat_stream(
    conversation_id: str,
    body: SendMessageRequest,
    user: AuthenticatedUser = Depends(get_current_user),
):
    """Send a message and stream the AI response via SSE.

    1. Verify conversation ownership
    2. Check credits
    3. Save user message
    4. Build message history
    5. Route to appropriate model
    6. Stream response tokens via SSE
    7. Save assistant response + log usage (after stream completes)
    """
    # 1. Verify conversation exists and belongs to user
    conv = await db.get_conversation(conversation_id, user.user_id)
    if not conv:
        raise HTTPException(status_code=404, detail="Conversation not found")

    # 2. Check credits
    profile = await db.get_user_profile(user.user_id)
    if not profile:
        profile = await db.ensure_user_profile(user.user_id, user.email)
    if profile["credits_balance"] <= 0:
        raise HTTPException(status_code=402, detail="Insufficient credits")

    # 3. Save the user's message
    await db.insert_message(
        conversation_id=conversation_id,
        role=MessageRole.user.value,
        content=body.content,
    )

    # 4. Build conversation history (last 20 messages for context)
    history_rows = await db.get_messages(conversation_id, limit=20)
    messages = [{"role": r["role"], "content": r["content"]} for r in history_rows]

    # 5. Route model
    model = route_model(body.content, body.model)

    # 5a. Check tier allows this model
    sub = await db.get_subscription(user.user_id)
    tier_name = sub["tier"] if sub else "free"
    if not is_model_allowed(tier_name, model):
        raise HTTPException(
            status_code=403,
            detail=f"Model {model} is not available on your {tier_name} plan. Upgrade to access more models.",
        )

    # 5b. Resolve BYOK key if user has one stored
    byok_api_key = await resolve_byok_key(user.user_id, provider="anthropic")

    # 5c. Build system prompt with user memory
    memory_ctx = await build_memory_context(user.user_id)
    system_prompt = "You are a helpful AI assistant powered by myOpenClaw."
    if memory_ctx:
        system_prompt += memory_ctx

    # 6. Stream response
    async def event_generator():
        full_response = ""
        total_input = 0
        total_output = 0
        credits_used = 0

        async for sse_line in stream_agent_response(
                user_id=user.user_id,
                conversation_id=conversation_id,
                messages=messages,
                model=model,
                system_prompt=system_prompt,
                api_key=byok_api_key,
            ):
            yield sse_line

            # Parse the done event to extract totals for post-stream bookkeeping
            if '"type":"done"' in sse_line or '"type": "done"' in sse_line:
                import json
                try:
                    data_str = sse_line.replace("data: ", "").strip()
                    data = json.loads(data_str)
                    total_input = data.get("total_tokens", 0)
                    credits_used = data.get("credits_used", 0)
                except (json.JSONDecodeError, ValueError):
                    pass

            elif '"type":"token"' in sse_line or '"type": "token"' in sse_line:
                import json
                try:
                    data_str = sse_line.replace("data: ", "").strip()
                    data = json.loads(data_str)
                    full_response += data.get("content", "")
                except (json.JSONDecodeError, ValueError):
                    pass

        # 7. Post-stream: save assistant message and log usage
        if full_response:
            await db.insert_message(
                conversation_id=conversation_id,
                role=MessageRole.assistant.value,
                content=full_response,
                model=model,
                tokens_used=total_input,
            )

        if credits_used > 0:
            try:
                await db.deduct_credits(user.user_id, credits_used)
            except ValueError:
                pass  # Don't fail the stream for credit issues

            cost = calculate_cost(model, total_input, total_input)
            await db.log_usage(
                user_id=user.user_id,
                conversation_id=conversation_id,
                model=model,
                input_tokens=total_input,
                output_tokens=total_output,
                cost_usd=cost,
            )

        # Auto-title the conversation from first message
        if len(history_rows) <= 1 and full_response:
            short_title = full_response[:60].split("\n")[0]
            if len(short_title) > 50:
                short_title = short_title[:50] + "…"
            await db.update_conversation(conversation_id, user.user_id, title=short_title)

    return StreamingResponse(
        event_generator(),
        media_type="text/event-stream",
        headers={
            "Cache-Control": "no-cache",
            "Connection": "keep-alive",
            "X-Accel-Buffering": "no",
        },
    )
