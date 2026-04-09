"""myOpenClaw Backend — FastAPI Application Entry Point."""

from __future__ import annotations

import logging
from contextlib import asynccontextmanager

import sentry_sdk
from fastapi import FastAPI, Request
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse

from app.config import get_settings
from app.api.conversations import router as conversations_router
from app.api.users import router as users_router
from app.api.sandboxes import router as sandboxes_router
from app.api.files import router as files_router
from app.api.api_keys import router as api_keys_router
from app.api.billing import router as billing_router
from app.api.memory import router as memory_router
from app.api.telegram import router as telegram_router
from app.api.whatsapp import router as whatsapp_router
from app.api.google_oauth import router as google_oauth_router
from app.services.sandbox import get_sandbox_manager

logger = logging.getLogger(__name__)

settings = get_settings()


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Startup/shutdown hooks."""
    settings = get_settings()

    if settings.app_env == "production" and not settings.stripe_webhook_secret:
        logger.warning(
            "STRIPE_WEBHOOK_SECRET is empty in production — Stripe webhooks will not be verified"
        )

    # Initialize Sentry if DSN provided
    if settings.sentry_dsn:
        sentry_sdk.init(dsn=settings.sentry_dsn, traces_sample_rate=0.1)

    # Start sandbox manager reaper (Phase 4)
    sandbox_mgr = None
    try:
        sandbox_mgr = get_sandbox_manager()
        await sandbox_mgr.start()
    except Exception as e:
        logger.warning("Sandbox manager failed to start (E2B not configured?): %s", e)

    yield  # App runs here

    # Shutdown: destroy all active sandboxes
    if sandbox_mgr is not None:
        try:
            await sandbox_mgr.stop()
        except Exception as e:
            logger.warning("Sandbox manager failed to stop cleanly: %s", e)


app = FastAPI(
    title="myOpenClaw API",
    description="AI Agent Backend — chat streaming, conversations, credit management",
    version="0.1.0",
    lifespan=lifespan,
)

# CORS — allow mobile apps and web dashboard
# Set CORS_ORIGINS env var (comma-separated) to restrict in production
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.get_cors_origins(),
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Mount routers
app.include_router(conversations_router, prefix="/api/v1")
app.include_router(users_router, prefix="/api/v1")
app.include_router(sandboxes_router, prefix="/api/v1")
app.include_router(files_router, prefix="/api/v1")
app.include_router(api_keys_router, prefix="/api/v1")
app.include_router(billing_router, prefix="/api/v1")
# Stripe webhook URL: POST /api/v1/billing/webhooks/stripe — configure this in the Stripe dashboard
app.include_router(memory_router, prefix="/api/v1")
app.include_router(telegram_router)          # /webhooks/telegram (no prefix — matches Telegram dashboard URL)
app.include_router(whatsapp_router)          # /webhooks/whatsapp (no prefix — matches Meta dashboard URL)
app.include_router(google_oauth_router, prefix="/api/v1")  # /api/v1/auth/google*


@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    """Catch-all for unhandled errors — return clean JSON, log details."""
    logger.exception("Unhandled error on %s %s", request.method, request.url.path)
    origin = request.headers.get("origin", "")
    allowed_origins = settings.get_cors_origins()
    headers: dict[str, str] = {}
    if origin and ("*" in allowed_origins or origin in allowed_origins):
        headers["access-control-allow-origin"] = origin if "*" not in allowed_origins else "*"
        headers["access-control-allow-credentials"] = "true"
    return JSONResponse(
        status_code=500,
        content={"detail": "Internal server error"},
        headers=headers,
    )


@app.get("/health")
async def health():
    return {
        "status": "ok",
        "version": "0.1.0",
        "environment": settings.app_env,
        "services": {
            "anthropic": bool(settings.anthropic_api_key),
            "supabase": bool(settings.supabase_url),
            "e2b": bool(settings.e2b_api_key),
            "stripe": bool(settings.stripe_secret_key),
        },
    }
