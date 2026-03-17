"""Billing & subscription endpoints — Stripe Checkout, webhooks, credit management."""

from __future__ import annotations

import logging
from datetime import datetime

import stripe
from fastapi import APIRouter, Depends, HTTPException, Request
from pydantic import BaseModel

from app.auth.jwt import AuthenticatedUser, get_current_user
from app.config import get_settings
from app.db import supabase as db
from app.services.tiers import get_tier

router = APIRouter(prefix="/billing", tags=["billing"])
logger = logging.getLogger(__name__)

# Tier → Stripe price ID mapping (set via env vars)
TIER_PRICES: dict[str, str] = {}


def _init_stripe():
    settings = get_settings()
    stripe.api_key = settings.stripe_secret_key
    global TIER_PRICES
    TIER_PRICES = {
        "starter": settings.stripe_price_starter,
        "pro": settings.stripe_price_pro,
        "power": settings.stripe_price_power,
        "byok": settings.stripe_price_byok,
    }


# ── Request / Response ─────────────────────────────────────────────────────

class CheckoutRequest(BaseModel):
    tier: str
    success_url: str = "https://myopenclaw.vercel.app/settings?billing=success"
    cancel_url: str = "https://myopenclaw.vercel.app/settings?billing=cancel"


class SubscriptionResponse(BaseModel):
    tier: str
    status: str
    credits_balance: int
    monthly_credits: int
    features: dict


class TopUpRequest(BaseModel):
    success_url: str = "https://myopenclaw.vercel.app/settings?topup=success"
    cancel_url: str = "https://myopenclaw.vercel.app/settings?topup=cancel"


# ── Endpoints ──────────────────────────────────────────────────────────────

@router.get("/subscription", response_model=SubscriptionResponse)
async def get_subscription(user: AuthenticatedUser = Depends(get_current_user)):
    """Get current subscription status and credit balance."""
    sub = await db.get_subscription(user.user_id)
    profile = await db.get_user_profile(user.user_id)
    tier_name = sub["tier"] if sub else "free"
    tier = get_tier(tier_name)

    return SubscriptionResponse(
        tier=tier_name,
        status=sub["status"] if sub else "active",
        credits_balance=profile["credits_balance"] if profile else 0,
        monthly_credits=tier.monthly_credits,
        features={
            "can_execute_code": tier.can_execute_code,
            "can_create_files": tier.can_create_files,
            "can_web_browse": tier.can_web_browse,
            "max_storage_gb": tier.max_storage_gb,
            "allowed_models": tier.allowed_models,
        },
    )


@router.post("/checkout")
async def create_checkout(
    body: CheckoutRequest,
    user: AuthenticatedUser = Depends(get_current_user),
):
    """Create a Stripe Checkout session for subscription purchase."""
    _init_stripe()
    settings = get_settings()

    if not settings.stripe_secret_key:
        raise HTTPException(status_code=503, detail="Stripe not configured")

    if body.tier not in TIER_PRICES or not TIER_PRICES.get(body.tier):
        raise HTTPException(status_code=400, detail=f"Invalid tier: {body.tier}")

    # Get or create Stripe customer
    sub = await db.get_subscription(user.user_id)
    customer_id = sub.get("stripe_customer_id") if sub else None

    if not customer_id:
        customer = stripe.Customer.create(
            email=user.email,
            metadata={"user_id": user.user_id},
        )
        customer_id = customer.id
        await db.upsert_subscription(
            user.user_id,
            stripe_customer_id=customer_id,
        )

    session = stripe.checkout.Session.create(
        customer=customer_id,
        mode="subscription",
        line_items=[{"price": TIER_PRICES[body.tier], "quantity": 1}],
        success_url=body.success_url,
        cancel_url=body.cancel_url,
        metadata={"user_id": user.user_id, "tier": body.tier},
    )

    return {"checkout_url": session.url}


@router.post("/topup")
async def create_topup(
    body: TopUpRequest,
    user: AuthenticatedUser = Depends(get_current_user),
):
    """Create a Stripe Checkout session for a one-time credit top-up."""
    _init_stripe()
    settings = get_settings()

    if not settings.stripe_secret_key or not settings.stripe_price_topup:
        raise HTTPException(status_code=503, detail="Stripe not configured")

    sub = await db.get_subscription(user.user_id)
    customer_id = sub.get("stripe_customer_id") if sub else None

    if not customer_id:
        customer = stripe.Customer.create(
            email=user.email,
            metadata={"user_id": user.user_id},
        )
        customer_id = customer.id
        await db.upsert_subscription(user.user_id, stripe_customer_id=customer_id)

    session = stripe.checkout.Session.create(
        customer=customer_id,
        mode="payment",
        line_items=[{"price": settings.stripe_price_topup, "quantity": 1}],
        success_url=body.success_url,
        cancel_url=body.cancel_url,
        metadata={"user_id": user.user_id, "type": "topup"},
    )

    return {"checkout_url": session.url}


@router.post("/portal")
async def create_portal(user: AuthenticatedUser = Depends(get_current_user)):
    """Create a Stripe Customer Portal session for managing subscriptions."""
    _init_stripe()
    sub = await db.get_subscription(user.user_id)
    if not sub or not sub.get("stripe_customer_id"):
        raise HTTPException(status_code=400, detail="No active subscription")

    session = stripe.billing_portal.Session.create(
        customer=sub["stripe_customer_id"],
        return_url="https://myopenclaw.vercel.app/settings",
    )
    return {"portal_url": session.url}


# ── Stripe Webhook ─────────────────────────────────────────────────────────

@router.post("/webhooks/stripe")
async def stripe_webhook(request: Request):
    """Handle Stripe webhook events for subscription lifecycle."""
    _init_stripe()
    settings = get_settings()

    payload = await request.body()
    sig_header = request.headers.get("stripe-signature", "")

    try:
        event = stripe.Webhook.construct_event(
            payload, sig_header, settings.stripe_webhook_secret
        )
    except (ValueError, stripe.error.SignatureVerificationError):
        raise HTTPException(status_code=400, detail="Invalid webhook signature")

    event_type = event["type"]
    data = event["data"]["object"]
    logger.info("Stripe webhook: %s", event_type)

    if event_type == "checkout.session.completed":
        await _handle_checkout_completed(data)
    elif event_type == "customer.subscription.updated":
        await _handle_subscription_updated(data)
    elif event_type == "customer.subscription.deleted":
        await _handle_subscription_deleted(data)
    elif event_type == "invoice.payment_succeeded":
        await _handle_invoice_paid(data)

    return {"status": "ok"}


async def _handle_checkout_completed(session: dict):
    metadata = session.get("metadata", {})
    user_id = metadata.get("user_id")
    if not user_id:
        return

    # One-time top-up
    if metadata.get("type") == "topup":
        await db.add_credits(user_id, 500)
        await db.log_credit_purchase(
            user_id=user_id,
            amount=500,
            price_usd=4.99,
            source="stripe",
            payment_id=session.get("payment_intent"),
        )
        return

    # Subscription checkout
    tier = metadata.get("tier", "starter")
    tier_config = get_tier(tier)
    stripe_sub_id = session.get("subscription")

    await db.upsert_subscription(
        user_id,
        tier=tier,
        status="active",
        stripe_subscription_id=stripe_sub_id,
        monthly_credits=tier_config.monthly_credits,
    )
    await db.add_credits(user_id, tier_config.monthly_credits)


async def _handle_subscription_updated(subscription: dict):
    customer_id = subscription.get("customer")
    sub = await db.get_subscription_by_stripe_customer(customer_id)
    if not sub:
        return

    sub_status = subscription.get("status", "active")
    status_map = {
        "active": "active",
        "past_due": "past_due",
        "canceled": "cancelled",
        "unpaid": "past_due",
    }
    await db.upsert_subscription(
        sub["user_id"],
        status=status_map.get(sub_status, "active"),
        stripe_subscription_id=subscription.get("id"),
    )


async def _handle_subscription_deleted(subscription: dict):
    customer_id = subscription.get("customer")
    sub = await db.get_subscription_by_stripe_customer(customer_id)
    if not sub:
        return

    await db.upsert_subscription(
        sub["user_id"],
        tier="free",
        status="cancelled",
        monthly_credits=100,
    )


async def _handle_invoice_paid(invoice: dict):
    """On recurring invoice payment, reset monthly credits."""
    customer_id = invoice.get("customer")
    sub = await db.get_subscription_by_stripe_customer(customer_id)
    if not sub:
        return

    tier_config = get_tier(sub["tier"])
    await db.add_credits(sub["user_id"], tier_config.monthly_credits)
    await db.upsert_subscription(
        sub["user_id"],
        credits_reset_at=datetime.utcnow().isoformat(),
    )
