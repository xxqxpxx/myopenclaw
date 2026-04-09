from functools import lru_cache

from pydantic import validator
from pydantic_settings import BaseSettings


class Settings(BaseSettings):
    # App
    app_env: str = "development"
    app_debug: bool = False

    # CORS — comma-separated list of allowed origins; defaults to wildcard (dev)
    cors_origins: str = ""

    def get_cors_origins(self) -> list[str]:
        if not self.cors_origins.strip():
            return ["*"]
        return [o.strip() for o in self.cors_origins.split(",") if o.strip()]

    # Supabase
    supabase_url: str
    supabase_anon_key: str
    supabase_service_role_key: str
    supabase_jwt_secret: str

    # Anthropic
    anthropic_api_key: str

    # OpenAI (Whisper transcription for voice messages)
    openai_api_key: str = ""

    # E2B (Phase 4)
    e2b_api_key: str = ""
    e2b_sandbox_template_id: str = ""
    e2b_webhook_secret: str = ""

    # Telegram (Phase B)
    telegram_bot_token: str = ""

    # WhatsApp / Meta Cloud API (Phase B)
    whatsapp_phone_number_id: str = ""
    whatsapp_token: str = ""

    # Google OAuth (Phase B)
    google_client_id: str = ""
    google_client_secret: str = ""
    google_redirect_uri: str = ""

    # BYOK encryption (Phase 7) — 32-byte hex key for AES-256-GCM
    byok_encryption_key: str = ""

    # Stripe (Phase 8)
    stripe_secret_key: str = ""
    stripe_webhook_secret: str = ""
    stripe_price_starter: str = ""
    stripe_price_pro: str = ""
    stripe_price_power: str = ""
    stripe_price_byok: str = ""
    stripe_price_topup: str = ""

    # Sentry
    sentry_dsn: str = ""

    # Model routing defaults
    haiku_model: str = "claude-3-5-haiku-20241022"
    sonnet_model: str = "claude-sonnet-4-20250514"
    opus_model: str = "claude-opus-4-20250514"

    # Rate limits
    max_messages_per_minute: int = 20
    max_conversations_per_user: int = 100

    # Credits
    default_free_credits: int = 50

    model_config = {"env_file": ".env", "env_file_encoding": "utf-8"}


@lru_cache
def get_settings() -> Settings:
    return Settings()
