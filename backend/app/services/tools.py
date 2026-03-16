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

# ── Notion Tool Definitions ────────────────────────────────────────────────

NOTION_TOOL_DEFINITIONS: list[dict[str, Any]] = [
    {
        "name": "notion_read_page",
        "description": "Fetch the content of a Notion page by its page ID.",
        "input_schema": {
            "type": "object",
            "properties": {
                "page_id": {"type": "string", "description": "The Notion page ID (UUID)."},
            },
            "required": ["page_id"],
        },
    },
    {
        "name": "notion_create_page",
        "description": "Create a new Notion page under the specified parent.",
        "input_schema": {
            "type": "object",
            "properties": {
                "parent_id": {"type": "string", "description": "Parent page or database ID."},
                "title": {"type": "string", "description": "Page title."},
                "content": {"type": "string", "description": "Page content in plain text or Markdown."},
            },
            "required": ["parent_id", "title"],
        },
    },
    {
        "name": "notion_search",
        "description": "Full-text search across the user's Notion workspace.",
        "input_schema": {
            "type": "object",
            "properties": {
                "query": {"type": "string", "description": "Search query."},
            },
            "required": ["query"],
        },
    },
    {
        "name": "notion_update_database",
        "description": "Write or update a row in a Notion database.",
        "input_schema": {
            "type": "object",
            "properties": {
                "database_id": {"type": "string", "description": "Notion database ID."},
                "properties": {
                    "type": "object",
                    "description": "Database property values to set (Notion API format).",
                },
            },
            "required": ["database_id", "properties"],
        },
    },
]

# ── Gmail + Calendar Tool Definitions ──────────────────────────────────────

GMAIL_TOOL_DEFINITIONS: list[dict[str, Any]] = [
    {
        "name": "gmail_search",
        "description": "Search the user's Gmail inbox.",
        "input_schema": {
            "type": "object",
            "properties": {
                "query": {"type": "string", "description": "Gmail search query (same syntax as the Gmail search box)."},
                "max_results": {"type": "integer", "description": "Max messages to return.", "default": 10},
            },
            "required": ["query"],
        },
    },
    {
        "name": "gmail_send",
        "description": "Send an email via the user's Gmail account.",
        "input_schema": {
            "type": "object",
            "properties": {
                "to": {"type": "string", "description": "Recipient email address."},
                "subject": {"type": "string", "description": "Email subject."},
                "body": {"type": "string", "description": "Email body (plain text)."},
            },
            "required": ["to", "subject", "body"],
        },
    },
    {
        "name": "gmail_read",
        "description": "Read a Gmail message thread by message ID.",
        "input_schema": {
            "type": "object",
            "properties": {
                "message_id": {"type": "string", "description": "Gmail message ID."},
            },
            "required": ["message_id"],
        },
    },
    {
        "name": "calendar_list_events",
        "description": "List upcoming Google Calendar events within a date range.",
        "input_schema": {
            "type": "object",
            "properties": {
                "time_min": {"type": "string", "description": "Start of range (ISO 8601)."},
                "time_max": {"type": "string", "description": "End of range (ISO 8601)."},
                "max_results": {"type": "integer", "description": "Max events to return.", "default": 10},
            },
            "required": ["time_min"],
        },
    },
    {
        "name": "calendar_create_event",
        "description": "Create a new Google Calendar event.",
        "input_schema": {
            "type": "object",
            "properties": {
                "title": {"type": "string", "description": "Event title."},
                "start_time": {"type": "string", "description": "Start time (ISO 8601)."},
                "end_time": {"type": "string", "description": "End time (ISO 8601)."},
                "attendees": {
                    "type": "array",
                    "items": {"type": "string"},
                    "description": "List of attendee email addresses.",
                },
                "description": {"type": "string", "description": "Event description."},
            },
            "required": ["title", "start_time", "end_time"],
        },
    },
]

# ── GitHub Tool Definitions ────────────────────────────────────────────────

GITHUB_TOOL_DEFINITIONS: list[dict[str, Any]] = [
    {
        "name": "github_list_repos",
        "description": "List the user's GitHub repositories.",
        "input_schema": {
            "type": "object",
            "properties": {
                "type": {
                    "type": "string",
                    "enum": ["all", "owner", "member"],
                    "description": "Filter by ownership type.",
                    "default": "owner",
                },
            },
        },
    },
    {
        "name": "github_create_issue",
        "description": "Open a new GitHub issue in a repository.",
        "input_schema": {
            "type": "object",
            "properties": {
                "repo": {"type": "string", "description": "Full repo name (owner/repo)."},
                "title": {"type": "string", "description": "Issue title."},
                "body": {"type": "string", "description": "Issue body (Markdown)."},
            },
            "required": ["repo", "title"],
        },
    },
    {
        "name": "github_read_pr",
        "description": "Read details of a GitHub pull request.",
        "input_schema": {
            "type": "object",
            "properties": {
                "repo": {"type": "string", "description": "Full repo name (owner/repo)."},
                "pr_number": {"type": "integer", "description": "Pull request number."},
            },
            "required": ["repo", "pr_number"],
        },
    },
    {
        "name": "github_search_code",
        "description": "Search for code across GitHub repositories.",
        "input_schema": {
            "type": "object",
            "properties": {
                "query": {"type": "string", "description": "Code search query."},
                "repo": {"type": "string", "description": "Limit search to this repo (owner/repo). Optional."},
            },
            "required": ["query"],
        },
    },
]


def get_tool_schemas() -> list[dict[str, Any]]:
    """Return the tool definitions to pass to the Anthropic API."""
    return (
        TOOL_DEFINITIONS
        + NOTION_TOOL_DEFINITIONS
        + GMAIL_TOOL_DEFINITIONS
        + GITHUB_TOOL_DEFINITIONS
    )


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
        # Notion
        "notion_read_page": _notion_read_page,
        "notion_create_page": _notion_create_page,
        "notion_search": _notion_search,
        "notion_update_database": _notion_update_database,
        # Gmail / Calendar
        "gmail_search": _gmail_search,
        "gmail_send": _gmail_send,
        "gmail_read": _gmail_read,
        "calendar_list_events": _calendar_list_events,
        "calendar_create_event": _calendar_create_event,
        # GitHub
        "github_list_repos": _github_list_repos,
        "github_create_issue": _github_create_issue,
        "github_read_pr": _github_read_pr,
        "github_search_code": _github_search_code,
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


# ── Notion Executors ───────────────────────────────────────────────────────

async def _get_notion_client(user_id: str):
    """Build a Notion client using the user's stored API key."""
    from notion_client import AsyncClient
    from app.services.byok import resolve_byok_key
    token = await resolve_byok_key(user_id, "notion")
    if not token:
        raise ValueError("Notion API key not configured. Add it via Settings → API Keys.")
    return AsyncClient(auth=token)


async def _notion_read_page(tool_input: dict, conversation_id: str, user_id: str) -> str:
    client = await _get_notion_client(user_id)
    page_id = tool_input["page_id"]
    page = await client.pages.retrieve(page_id=page_id)
    blocks = await client.blocks.children.list(block_id=page_id)
    lines = [f"# {_notion_title(page)}"]
    for block in blocks.get("results", []):
        lines.append(_notion_block_text(block))
    return "\n".join(lines)[:8000]


async def _notion_create_page(tool_input: dict, conversation_id: str, user_id: str) -> str:
    client = await _get_notion_client(user_id)
    parent_id = tool_input["parent_id"]
    title = tool_input["title"]
    content = tool_input.get("content", "")
    page = await client.pages.create(
        parent={"page_id": parent_id},
        properties={"title": {"title": [{"text": {"content": title}}]}},
        children=[{
            "object": "block",
            "type": "paragraph",
            "paragraph": {"rich_text": [{"text": {"content": content[:2000]}}]},
        }] if content else [],
    )
    return f"Page created: {page['id']} — {title}"


async def _notion_search(tool_input: dict, conversation_id: str, user_id: str) -> str:
    client = await _get_notion_client(user_id)
    results = await client.search(query=tool_input["query"])
    lines = []
    for item in results.get("results", [])[:10]:
        lines.append(f"- [{item['object']}] {_notion_title(item)} (id: {item['id']})")
    return "\n".join(lines) if lines else "No results found."


async def _notion_update_database(tool_input: dict, conversation_id: str, user_id: str) -> str:
    client = await _get_notion_client(user_id)
    db_id = tool_input["database_id"]
    props = tool_input["properties"]
    page = await client.pages.create(
        parent={"database_id": db_id},
        properties=props,
    )
    return f"Database row created: {page['id']}"


def _notion_title(obj: dict) -> str:
    """Extract plain-text title from a Notion page/database object."""
    props = obj.get("properties", {})
    for key in ("title", "Name", "Title"):
        title_prop = props.get(key, {})
        texts = title_prop.get("title", [])
        if texts:
            return texts[0].get("plain_text", "")
    return obj.get("id", "Untitled")


def _notion_block_text(block: dict) -> str:
    """Extract plain text from a Notion block."""
    btype = block.get("type", "")
    content = block.get(btype, {})
    rich_texts = content.get("rich_text", [])
    return "".join(rt.get("plain_text", "") for rt in rich_texts)


# ── Gmail / Calendar Executors ─────────────────────────────────────────────

async def _get_google_service(user_id: str, service_name: str, version: str):
    """Build a Google API service client using the user's stored OAuth tokens."""
    import json as _json
    from googleapiclient.discovery import build
    from google.oauth2.credentials import Credentials
    from app.db.supabase import get_supabase

    client = get_supabase()
    result = (
        client.table("oauth_tokens")
        .select("token_data")
        .eq("user_id", user_id)
        .eq("provider", "google")
        .maybe_single()
        .execute()
    )
    if not result.data:
        raise ValueError("Google account not connected. Visit Settings → Integrations to connect Google.")

    token_data = result.data["token_data"]
    if isinstance(token_data, str):
        token_data = _json.loads(token_data)

    creds = Credentials(
        token=token_data.get("access_token"),
        refresh_token=token_data.get("refresh_token"),
        token_uri="https://oauth2.googleapis.com/token",
        client_id=token_data.get("client_id"),
        client_secret=token_data.get("client_secret"),
    )
    return build(service_name, version, credentials=creds)


async def _gmail_search(tool_input: dict, conversation_id: str, user_id: str) -> str:
    import asyncio
    service = await _get_google_service(user_id, "gmail", "v1")
    query = tool_input["query"]
    max_results = min(tool_input.get("max_results", 10), 20)
    result = await asyncio.to_thread(
        lambda: service.users().messages().list(userId="me", q=query, maxResults=max_results).execute()
    )
    messages = result.get("messages", [])
    if not messages:
        return "No messages found."
    lines = [f"Found {len(messages)} message(s):"]
    for msg in messages[:10]:
        lines.append(f"  - id: {msg['id']}")
    return "\n".join(lines)


async def _gmail_send(tool_input: dict, conversation_id: str, user_id: str) -> str:
    import asyncio
    import base64
    from email.message import EmailMessage
    service = await _get_google_service(user_id, "gmail", "v1")
    msg = EmailMessage()
    msg["To"] = tool_input["to"]
    msg["Subject"] = tool_input["subject"]
    msg.set_content(tool_input["body"])
    raw = base64.urlsafe_b64encode(msg.as_bytes()).decode()
    await asyncio.to_thread(
        lambda: service.users().messages().send(userId="me", body={"raw": raw}).execute()
    )
    return f"Email sent to {tool_input['to']}"


async def _gmail_read(tool_input: dict, conversation_id: str, user_id: str) -> str:
    import asyncio
    import base64
    service = await _get_google_service(user_id, "gmail", "v1")
    msg = await asyncio.to_thread(
        lambda: service.users().messages().get(
            userId="me", id=tool_input["message_id"], format="full"
        ).execute()
    )
    headers = {h["name"]: h["value"] for h in msg.get("payload", {}).get("headers", [])}
    subject = headers.get("Subject", "")
    sender = headers.get("From", "")
    parts = msg.get("payload", {}).get("parts", [])
    body = ""
    for part in parts:
        if part.get("mimeType") == "text/plain":
            data = part.get("body", {}).get("data", "")
            body = base64.urlsafe_b64decode(data + "==").decode("utf-8", errors="replace")
            break
    return f"From: {sender}\nSubject: {subject}\n\n{body[:3000]}"


async def _calendar_list_events(tool_input: dict, conversation_id: str, user_id: str) -> str:
    import asyncio
    service = await _get_google_service(user_id, "calendar", "v3")
    kwargs = {
        "calendarId": "primary",
        "timeMin": tool_input["time_min"],
        "maxResults": min(tool_input.get("max_results", 10), 20),
        "singleEvents": True,
        "orderBy": "startTime",
    }
    if tool_input.get("time_max"):
        kwargs["timeMax"] = tool_input["time_max"]
    result = await asyncio.to_thread(
        lambda: service.events().list(**kwargs).execute()
    )
    events = result.get("items", [])
    if not events:
        return "No upcoming events found."
    lines = []
    for e in events:
        start = e.get("start", {}).get("dateTime") or e.get("start", {}).get("date", "")
        lines.append(f"- {start}: {e.get('summary', 'Untitled')} (id: {e['id']})")
    return "\n".join(lines)


async def _calendar_create_event(tool_input: dict, conversation_id: str, user_id: str) -> str:
    import asyncio
    service = await _get_google_service(user_id, "calendar", "v3")
    body = {
        "summary": tool_input["title"],
        "start": {"dateTime": tool_input["start_time"]},
        "end": {"dateTime": tool_input["end_time"]},
    }
    if tool_input.get("attendees"):
        body["attendees"] = [{"email": e} for e in tool_input["attendees"]]
    if tool_input.get("description"):
        body["description"] = tool_input["description"]
    event = await asyncio.to_thread(
        lambda: service.events().insert(calendarId="primary", body=body).execute()
    )
    return f"Event created: {event.get('summary')} at {tool_input['start_time']} (id: {event['id']})"


# ── GitHub Executors ───────────────────────────────────────────────────────

async def _get_github_client(user_id: str):
    """Build a PyGithub client using the user's stored personal access token."""
    from github import Github
    from app.services.byok import resolve_byok_key
    token = await resolve_byok_key(user_id, "github")
    if not token:
        raise ValueError("GitHub token not configured. Add it via Settings → API Keys.")
    return Github(token)


async def _github_list_repos(tool_input: dict, conversation_id: str, user_id: str) -> str:
    import asyncio
    g = await _get_github_client(user_id)
    repo_type = tool_input.get("type", "owner")
    repos = await asyncio.to_thread(
        lambda: list(g.get_user().get_repos(type=repo_type))[:20]
    )
    return "\n".join(f"- {r.full_name} ({'private' if r.private else 'public'})" for r in repos)


async def _github_create_issue(tool_input: dict, conversation_id: str, user_id: str) -> str:
    import asyncio
    g = await _get_github_client(user_id)
    repo = await asyncio.to_thread(lambda: g.get_repo(tool_input["repo"]))
    issue = await asyncio.to_thread(
        lambda: repo.create_issue(title=tool_input["title"], body=tool_input.get("body", ""))
    )
    return f"Issue created: #{issue.number} — {issue.title}\n{issue.html_url}"


async def _github_read_pr(tool_input: dict, conversation_id: str, user_id: str) -> str:
    import asyncio
    g = await _get_github_client(user_id)
    repo = await asyncio.to_thread(lambda: g.get_repo(tool_input["repo"]))
    pr = await asyncio.to_thread(lambda: repo.get_pull(tool_input["pr_number"]))
    return (
        f"PR #{pr.number}: {pr.title}\n"
        f"State: {pr.state} | Author: {pr.user.login}\n"
        f"Branch: {pr.head.ref} → {pr.base.ref}\n\n"
        f"{pr.body[:2000] if pr.body else '(no description)'}"
    )


async def _github_search_code(tool_input: dict, conversation_id: str, user_id: str) -> str:
    import asyncio
    g = await _get_github_client(user_id)
    query = tool_input["query"]
    if tool_input.get("repo"):
        query = f"{query} repo:{tool_input['repo']}"
    results = await asyncio.to_thread(lambda: list(g.search_code(query)[:10]))
    if not results:
        return "No code results found."
    lines = [f"- {r.repository.full_name}/{r.path}" for r in results]
    return "\n".join(lines)
