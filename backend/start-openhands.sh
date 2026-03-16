#!/bin/bash
# Boot the OpenHands runtime inside the E2B sandbox.
# Environment variables are injected by the sandbox manager at creation time:
#   ANTHROPIC_API_KEY, OPENCLAW_MODEL, OPENCLAW_USER_ID, OPENCLAW_CONVERSATION_ID

set -e

exec python -m openhands.runtime.server \
    --host 0.0.0.0 \
    --port 18789 \
    --llm-model "${OPENCLAW_MODEL:-claude-sonnet-4-6}" \
    --llm-api-key "${ANTHROPIC_API_KEY}"
