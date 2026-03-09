"""Tool definitions and execution for the OpenClaw agent.

Defines Anthropic-compatible tool schemas and handles execution of tool calls
either inside E2B sandboxes (code_execute) or server-side (web_search, file ops).
"""

from __future__ import annotations

import json
import logging
from typing import Any

from app.config import get_settings

logger = logging.getLogger(__name__)


# ── Anthropic Tool Schemas ─────────────────────────────────────────────────
# These are passed to Claude's `tools` parameter so it can call them.

TOOL_DEFINITIONS: list[dict[str, Any]] = [
    {
        "name": "code_execute",
        "description": (
            "Execute code in a secure sandbox. Supports Python, JavaScript (Node.js), "
            "and Bash. Use for calculations, data processing, generating files, or any "
            "task that requires running code. Output files are saved in /workspace/output/."
        ),
        "input_schema": {
            "type": "object",
            "properties": {
                "code": {
                    "type": "string",
                    "description": "The code to execute.",
                },
                "language": {
                    "type": "string",
                    "enum": ["python", "javascript", "bash"],
                    "description": "Programming language. Defaults to python.",
                    "default": "python",
                },
            },
            "required": ["code"],
        },
    },
    {
        "name": "web_search",
        "description": (
            "Search the web and return relevant results. Use when the user asks about "
            "current events, needs factual information, or wants to research a topic."
        ),
        "input_schema": {
            "type": "object",
            "properties": {
                "query": {
                    "type": "string",
                    "description": "The search query.",
                },
                "num_results": {
                    "type": "integer",
                    "description": "Number of results to return (1-10).",
                    "default": 5,
                },
            },
            "required": ["query"],
        },
    },
    {
        "name": "file_read",
        "description": (
            "Read the contents of a file that the user has uploaded or that was "
            "previously created. Returns the file text or a summary for binary files."
        ),
        "input_schema": {
            "type": "object",
            "properties": {
                "filename": {
                    "type": "string",
                    "description": "Name or path of the file to read.",
                },
            },
            "required": ["filename"],
        },
    },
    {
        "name": "file_create",
        "description": (
            "Create a file with the given content. Useful for generating reports, "
            "saving code, or producing documents. The file will be available for "
            "the user to download."
        ),
        "input_schema": {
            "type": "object",
            "properties": {
                "filename": {
                    "type": "string",
                    "description": "Name for the output file (e.g., 'report.md', 'data.csv').",
                },
                "content": {
                    "type": "string",
                    "description": "The content to write to the file.",
                },
            },
            "required": ["filename", "content"],
        },
    },
    {
        "name": "http_request",
        "description": (
            "Make an HTTP request to an external API. Use for fetching data from "
            "APIs, checking endpoints, or retrieving structured data."
        ),
        "input_schema": {
            "type": "object",
            "properties": {
                "url": {
                    "type": "string",
                    "description": "The URL to request.",
                },
                "method": {
                    "type": "string",
                    "enum": ["GET", "POST", "PUT", "DELETE"],
                    "description": "HTTP method.",
                    "default": "GET",
                },
                "headers": {
                    "type": "object",
                    "description": "Optional request headers.",
                },
                "body": {
                    "type": "string",
                    "description": "Optional request body (for POST/PUT).",
                },
            },
            "required": ["url"],
        },
    },
]


def get_tool_schemas() -> list[dict[str, Any]]:
    """Return the tool definitions to pass to the Anthropic API."""
    return TOOL_DEFINITIONS


# ── Tool Executors ─────────────────────────────────────────────────────────

async def execute_tool(
    tool_name: str,
    tool_input: dict[str, Any],
    conversation_id: str,
    user_id: str,
) -> str:
    """Execute a tool call and return the result as a string.

    Dispatches to the appropriate handler based on tool_name.
    """
    handlers = {
        "code_execute": _execute_code,
        "web_search": _execute_web_search,
        "file_read": _execute_file_read,
        "file_create": _execute_file_create,
        "http_request": _execute_http_request,
    }

    handler = handlers.get(tool_name)
    if not handler:
        return f"Unknown tool: {tool_name}"

    try:
        return await handler(tool_input, conversation_id, user_id)
    except Exception as e:
        logger.exception("Tool %s execution failed", tool_name)
        return f"Tool execution error: {e}"


async def _execute_code(
    tool_input: dict, conversation_id: str, user_id: str
) -> str:
    """Execute code in the E2B sandbox."""
    from app.services.sandbox import get_sandbox_manager
    from app.services.bridge import is_sandbox_enabled

    code = tool_input.get("code", "")
    language = tool_input.get("language", "python")

    if not is_sandbox_enabled():
        return (
            "Code execution is not available (E2B sandbox not configured). "
            "Please set E2B_API_KEY in the environment."
        )

    manager = get_sandbox_manager()
    result = await manager.execute_code(
        conversation_id=conversation_id,
        code=code,
        language=language,
    )

    parts = []
    if result["stdout"]:
        parts.append(f"stdout:\n{result['stdout']}")
    if result["stderr"]:
        parts.append(f"stderr:\n{result['stderr']}")
    if result["error"]:
        parts.append(f"error:\n{result['error']}")
    if result["results"]:
        parts.append("results:\n" + "\n".join(result["results"]))

    return "\n".join(parts) if parts else "(no output)"


async def _execute_web_search(
    tool_input: dict, conversation_id: str, user_id: str
) -> str:
    """Perform a web search. Uses a simple httpx request to a search API."""
    import httpx

    query = tool_input.get("query", "")
    num_results = min(tool_input.get("num_results", 5), 10)

    # Use DuckDuckGo instant answer API (no API key needed)
    async with httpx.AsyncClient(timeout=10) as client:
        resp = await client.get(
            "https://api.duckduckgo.com/",
            params={"q": query, "format": "json", "no_html": 1},
        )

    if resp.status_code != 200:
        return f"Search failed with status {resp.status_code}"

    data = resp.json()
    results = []

    # Abstract/answer
    if data.get("Abstract"):
        results.append(f"**{data.get('Heading', 'Result')}**: {data['Abstract']}")
        if data.get("AbstractURL"):
            results.append(f"Source: {data['AbstractURL']}")

    # Related topics
    for topic in data.get("RelatedTopics", [])[:num_results]:
        if isinstance(topic, dict) and topic.get("Text"):
            text = topic["Text"][:200]
            url = topic.get("FirstURL", "")
            results.append(f"- {text}" + (f" ({url})" if url else ""))

    if not results:
        return f"No results found for: {query}"

    return "\n".join(results)


async def _execute_file_read(
    tool_input: dict, conversation_id: str, user_id: str
) -> str:
    """Read a file from the sandbox or uploaded files."""
    from app.services.sandbox import get_sandbox_manager
    from app.services.bridge import is_sandbox_enabled

    filename = tool_input.get("filename", "")

    if is_sandbox_enabled():
        manager = get_sandbox_manager()
        info = manager.get_sandbox_info(conversation_id)
        if info and info.sandbox:
            try:
                import asyncio
                content = await asyncio.to_thread(
                    info.sandbox.files.read, filename
                )
                if isinstance(content, bytes):
                    return f"(binary file: {filename}, {len(content)} bytes)"
                return str(content)[:10000]  # Cap at 10K chars
            except Exception as e:
                return f"Failed to read file: {e}"

    return f"File not found or sandbox not available: {filename}"


async def _execute_file_create(
    tool_input: dict, conversation_id: str, user_id: str
) -> str:
    """Create a file in the sandbox."""
    from app.services.sandbox import get_sandbox_manager
    from app.services.bridge import is_sandbox_enabled

    filename = tool_input.get("filename", "output.txt")
    content = tool_input.get("content", "")

    if is_sandbox_enabled():
        manager = get_sandbox_manager()
        info = manager.get_sandbox_info(conversation_id)
        if info and info.sandbox:
            try:
                import asyncio
                path = f"/workspace/output/{filename}"
                await asyncio.to_thread(
                    info.sandbox.files.write, path, content
                )
                return f"File created: {path} ({len(content)} bytes)"
            except Exception as e:
                return f"Failed to create file: {e}"

    return f"Sandbox not available. File content ({len(content)} chars) for '{filename}' would be created when sandboxes are enabled."


async def _execute_http_request(
    tool_input: dict, conversation_id: str, user_id: str
) -> str:
    """Make an HTTP request to an external API."""
    import httpx

    url = tool_input.get("url", "")
    method = tool_input.get("method", "GET").upper()
    headers = tool_input.get("headers", {})
    body = tool_input.get("body")

    # Basic URL validation - block private/internal IPs (SSRF protection)
    from urllib.parse import urlparse
    parsed = urlparse(url)
    hostname = parsed.hostname or ""
    blocked_prefixes = ("10.", "172.16.", "172.17.", "172.18.", "172.19.",
                        "172.20.", "172.21.", "172.22.", "172.23.", "172.24.",
                        "172.25.", "172.26.", "172.27.", "172.28.", "172.29.",
                        "172.30.", "172.31.", "192.168.", "127.", "0.")
    if hostname.startswith(blocked_prefixes) or hostname in ("localhost", ""):
        return "Error: requests to private/internal addresses are not allowed."

    async with httpx.AsyncClient(timeout=15) as client:
        resp = await client.request(
            method=method,
            url=url,
            headers=headers,
            content=body if body else None,
        )

    # Cap response body to prevent huge outputs
    body_text = resp.text[:5000]
    return f"HTTP {resp.status_code}\n{body_text}"
