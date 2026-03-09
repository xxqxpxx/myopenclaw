"""Tests for model routing and credit estimation."""

import pytest
from app.services.llm import route_model, calculate_cost, estimate_credits
from app.services.tiers import get_tier, is_model_allowed, TIERS


class TestModelRouting:
    def test_short_simple_query_routes_to_haiku(self):
        model = route_model("hello there")
        assert "haiku" in model

    def test_code_query_routes_to_sonnet(self):
        model = route_model("write code to sort a list in python")
        assert "sonnet" in model

    def test_user_override_respected(self):
        model = route_model("hello", user_model="claude-opus-4-20250514")
        assert model == "claude-opus-4-20250514"

    def test_complex_query_routes_to_sonnet(self):
        model = route_model("analyze this architecture diagram and explain the data flow patterns")
        assert "sonnet" in model


class TestCreditCalculation:
    def test_haiku_cost(self):
        cost = calculate_cost("claude-haiku-4-5-20250315", 1000, 500)
        assert cost > 0
        assert cost < 0.01

    def test_opus_costs_more(self):
        haiku = calculate_cost("claude-haiku-4-5-20250315", 1000, 500)
        opus = calculate_cost("claude-opus-4-20250514", 1000, 500)
        assert opus > haiku

    def test_estimate_credits_minimum_one(self):
        credits = estimate_credits("claude-haiku-4-5-20250315", 1, 1)
        assert credits >= 1


class TestTiers:
    def test_all_tiers_exist(self):
        assert set(TIERS.keys()) == {"free", "starter", "pro", "power", "byok"}

    def test_free_tier_haiku_only(self):
        assert is_model_allowed("free", "claude-haiku-4-5-20250315")
        assert not is_model_allowed("free", "claude-sonnet-4-20250514")
        assert not is_model_allowed("free", "claude-opus-4-20250514")

    def test_power_tier_all_models(self):
        assert is_model_allowed("power", "claude-haiku-4-5-20250315")
        assert is_model_allowed("power", "claude-sonnet-4-20250514")
        assert is_model_allowed("power", "claude-opus-4-20250514")

    def test_free_tier_no_code_execution(self):
        tier = get_tier("free")
        assert not tier.can_execute_code
        assert not tier.can_create_files

    def test_starter_tier_has_features(self):
        tier = get_tier("starter")
        assert tier.can_execute_code
        assert tier.can_web_browse
        assert tier.monthly_credits == 2000

    def test_unknown_tier_defaults_to_free(self):
        tier = get_tier("nonexistent")
        assert tier.name == "Free"
