"""Pydantic models for the myOpenClaw API — shared across all layers."""

from __future__ import annotations

import uuid
from datetime import datetime
from enum import Enum

from pydantic import BaseModel, Field


# ── Enums ──────────────────────────────────────────────────────────────────

class MessageRole(str, Enum):
    user = "user"
    assistant = "assistant"
    tool = "tool"
    system = "system"


class SubscriptionTier(str, Enum):
    free = "free"
    starter = "starter"
    pro = "pro"
    power = "power"
    byok = "byok"


class SSEEventType(str, Enum):
    token = "token"
    tool_start = "tool_start"
    tool_result = "tool_result"
    file = "file"
    error = "error"
    done = "done"


class AIProvider(str, Enum):
    anthropic = "anthropic"
    openai = "openai"
    gemini = "gemini"


# ── Database row models ────────────────────────────────────────────────────

class UserProfile(BaseModel):
    id: str
    email: str
    display_name: str | None = None
    subscription_tier: SubscriptionTier = SubscriptionTier.free
    credits_balance: int = 0
    created_at: datetime | None = None


class Conversation(BaseModel):
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    user_id: str
    title: str = "New Chat"
    created_at: datetime | None = None
    updated_at: datetime | None = None
    sandbox_id: str | None = None


class Message(BaseModel):
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    conversation_id: str
    role: MessageRole
    content: str
    tokens_used: int = 0
    model: str | None = None
    created_at: datetime | None = None


class UsageLog(BaseModel):
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    user_id: str
    conversation_id: str | None = None
    model: str
    input_tokens: int = 0
    output_tokens: int = 0
    cost_usd: float = 0.0
    created_at: datetime | None = None


class UserApiKey(BaseModel):
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    user_id: str
    provider: AIProvider
    encrypted_key: str
    is_active: bool = True
    created_at: datetime | None = None


class FileRecord(BaseModel):
    id: str = Field(default_factory=lambda: str(uuid.uuid4()))
    user_id: str
    conversation_id: str | None = None
    filename: str
    size: int = 0
    storage_path: str
    created_at: datetime | None = None


# ── API request/response schemas ──────────────────────────────────────────

class CreateConversationRequest(BaseModel):
    title: str = "New Chat"


class SendMessageRequest(BaseModel):
    content: str
    model: str | None = None  # None = auto-route


class ConversationResponse(BaseModel):
    id: str
    title: str
    created_at: datetime | None = None
    updated_at: datetime | None = None
    last_message: str | None = None


class MessageResponse(BaseModel):
    id: str
    role: MessageRole
    content: str
    model: str | None = None
    tokens_used: int = 0
    created_at: datetime | None = None


class UserProfileResponse(BaseModel):
    id: str
    email: str
    display_name: str | None = None
    subscription_tier: SubscriptionTier
    credits_balance: int


class CreditBalanceResponse(BaseModel):
    credits_balance: int
    subscription_tier: SubscriptionTier


class SSEEvent(BaseModel):
    """Server-Sent Event payload — serialized as JSON in the `data:` field."""
    type: SSEEventType
    content: str | None = None
    model: str | None = None
    tool: str | None = None
    input: dict | None = None
    output: str | None = None
    filename: str | None = None
    url: str | None = None
    size: int | None = None
    total_tokens: int | None = None
    credits_used: int | None = None
    error: str | None = None


class HealthResponse(BaseModel):
    status: str = "ok"
    version: str = "0.1.0"
    environment: str = "development"
