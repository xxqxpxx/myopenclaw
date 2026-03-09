from pydantic_settings import BaseSettings
from functools import lru_cache


class Settings(BaseSettings):
    # App
    app_env: str = "development"
    app_debug: bool = False

    # Supabase
    supabase_url: str
    supabase_anon_key: str
    supabase_service_role_key: str
    supabase_jwt_secret: str

    # Anthropic
    anthropic_api_key: str

    # E2B (Phase 4)
    e2b_api_key: str = ""

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
    haiku_model: str = "claude-haiku-4-5-20250315"
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
