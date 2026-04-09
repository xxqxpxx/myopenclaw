"""Subscription tier definitions and feature gating."""

from __future__ import annotations

from dataclasses import dataclass


@dataclass
class TierConfig:
    name: str
    monthly_credits: int
    allowed_models: list[str]
    max_storage_gb: int
    can_execute_code: bool
    can_create_files: bool
    can_web_browse: bool


TIERS: dict[str, TierConfig] = {
    "free": TierConfig(
        name="Free",
        monthly_credits=100,
        allowed_models=["claude-3-5-haiku-20241022"],
        max_storage_gb=1,
        can_execute_code=False,
        can_create_files=False,
        can_web_browse=False,
    ),
    "starter": TierConfig(
        name="Starter",
        monthly_credits=2000,
        allowed_models=["claude-3-5-haiku-20241022", "claude-sonnet-4-20250514"],
        max_storage_gb=5,
        can_execute_code=True,
        can_create_files=True,
        can_web_browse=True,
    ),
    "pro": TierConfig(
        name="Pro",
        monthly_credits=8000,
        allowed_models=["claude-3-5-haiku-20241022", "claude-sonnet-4-20250514"],
        max_storage_gb=20,
        can_execute_code=True,
        can_create_files=True,
        can_web_browse=True,
    ),
    "power": TierConfig(
        name="Power",
        monthly_credits=30000,
        allowed_models=[
            "claude-3-5-haiku-20241022",
            "claude-sonnet-4-20250514",
            "claude-opus-4-20250514",
        ],
        max_storage_gb=100,
        can_execute_code=True,
        can_create_files=True,
        can_web_browse=True,
    ),
    "byok": TierConfig(
        name="BYOK",
        monthly_credits=999999,  # effectively unlimited
        allowed_models=[
            "claude-3-5-haiku-20241022",
            "claude-sonnet-4-20250514",
            "claude-opus-4-20250514",
        ],
        max_storage_gb=50,
        can_execute_code=True,
        can_create_files=True,
        can_web_browse=True,
    ),
}


def get_tier(tier_name: str) -> TierConfig:
    return TIERS.get(tier_name, TIERS["free"])


def is_model_allowed(tier_name: str, model: str) -> bool:
    return model in get_tier(tier_name).allowed_models
