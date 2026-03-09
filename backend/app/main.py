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
from app.models.schemas import HealthResponse

logger = logging.getLogger(__name__)


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Startup/shutdown hooks."""
    settings = get_settings()

    # Initialize Sentry if DSN provided
    if settings.sentry_dsn:
        sentry_sdk.init(dsn=settings.sentry_dsn, traces_sample_rate=0.1)

    yield  # App runs here


app = FastAPI(
    title="myOpenClaw API",
    description="AI Agent Backend — chat streaming, conversations, credit management",
    version="0.1.0",
    lifespan=lifespan,
)

# CORS — allow mobile apps and web dashboard
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # Lock down in production
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Mount routers
app.include_router(conversations_router, prefix="/api/v1")
app.include_router(users_router, prefix="/api/v1")


@app.exception_handler(Exception)
async def global_exception_handler(request: Request, exc: Exception):
    """Catch-all for unhandled errors — return clean JSON, log details."""
    logger.exception("Unhandled error on %s %s", request.method, request.url.path)
    return JSONResponse(
        status_code=500,
        content={"detail": "Internal server error"},
    )


@app.get("/health", response_model=HealthResponse)
async def health():
    settings = get_settings()
    return HealthResponse(
        status="ok",
        version="0.1.0",
        environment=settings.app_env,
    )
