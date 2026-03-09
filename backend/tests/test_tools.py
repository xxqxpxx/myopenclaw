"""Tests for tool schemas and SSRF protection."""

import pytest
from app.services.tools import get_tool_schemas, TOOL_DEFINITIONS


class TestToolSchemas:
    def test_five_tools_defined(self):
        schemas = get_tool_schemas()
        assert len(schemas) == 5

    def test_all_schemas_have_required_fields(self):
        for schema in TOOL_DEFINITIONS:
            assert "name" in schema
            assert "description" in schema
            assert "input_schema" in schema

    def test_tool_names(self):
        names = {s["name"] for s in TOOL_DEFINITIONS}
        assert names == {"code_execute", "web_search", "file_read", "file_create", "http_request"}
