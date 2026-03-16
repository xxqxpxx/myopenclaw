"""E2B Sandbox Manager — lifecycle management for OpenClaw agent sandboxes.

Each conversation gets a dedicated E2B Firecracker microVM running OpenClaw.
The manager handles: create → active → idle → pause → resume → destroy.
"""

from __future__ import annotations

import asyncio
import logging
import time
from dataclasses import dataclass, field
from enum import Enum

from e2b_code_interpreter import Sandbox

from app.config import get_settings

logger = logging.getLogger(__name__)

# How long (seconds) before an idle sandbox gets paused
IDLE_TIMEOUT_SECONDS = 600  # 10 minutes
# Max sandbox lifetime before forced destroy (7 days in seconds)
MAX_LIFETIME_SECONDS = 7 * 24 * 3600


class SandboxState(str, Enum):
    creating = "creating"
    active = "active"
    idle = "idle"
    paused = "paused"
    resuming = "resuming"
    destroyed = "destroyed"


@dataclass
class SandboxInfo:
    """In-memory tracking for an active sandbox."""
    sandbox_id: str
    conversation_id: str
    user_id: str
    state: SandboxState = SandboxState.active
    last_active_at: float = field(default_factory=time.time)
    created_at: float = field(default_factory=time.time)
    sandbox: Sandbox | None = None


class SandboxManager:
    """Manages E2B sandbox lifecycle for all active conversations.

    Maps (user_id, conversation_id) → SandboxInfo.
    Runs a background reaper task to pause idle sandboxes.
    """

    def __init__(self):
        self._sandboxes: dict[str, SandboxInfo] = {}  # key = conversation_id
        self._lock = asyncio.Lock()
        self._reaper_task: asyncio.Task | None = None

    def _key(self, conversation_id: str) -> str:
        return conversation_id

    async def start(self):
        """Start the background reaper that pauses idle sandboxes."""
        if self._reaper_task is None:
            self._reaper_task = asyncio.create_task(self._reaper_loop())
            logger.info("Sandbox reaper started")

    async def stop(self):
        """Stop the reaper and destroy all active sandboxes."""
        if self._reaper_task:
            self._reaper_task.cancel()
            self._reaper_task = None

        async with self._lock:
            for info in list(self._sandboxes.values()):
                await self._destroy_sandbox(info)
            self._sandboxes.clear()
        logger.info("Sandbox manager stopped, all sandboxes destroyed")

    async def get_or_create(
        self,
        user_id: str,
        conversation_id: str,
        anthropic_api_key: str | None = None,
        model: str | None = None,
    ) -> SandboxInfo:
        """Get an active sandbox for a conversation, or create/resume one.

        Returns a SandboxInfo with a live sandbox connection.
        """
        key = self._key(conversation_id)

        async with self._lock:
            info = self._sandboxes.get(key)

            if info and info.state == SandboxState.active:
                info.last_active_at = time.time()
                return info

            if info and info.state == SandboxState.paused:
                return await self._resume_sandbox(info)

            # Create new sandbox
            return await self._create_sandbox(user_id, conversation_id, anthropic_api_key, model)

    async def _create_sandbox(
        self,
        user_id: str,
        conversation_id: str,
        anthropic_api_key: str | None = None,
        model: str | None = None,
    ) -> SandboxInfo:
        """Spin up a new E2B sandbox with OpenClaw configured."""
        settings = get_settings()
        api_key = anthropic_api_key or settings.anthropic_api_key
        agent_model = model or settings.sonnet_model

        env_vars = {
            "ANTHROPIC_API_KEY": api_key,
            "OPENCLAW_MODEL": agent_model,
            "OPENCLAW_USER_ID": user_id,
            "OPENCLAW_CONVERSATION_ID": conversation_id,
        }

        logger.info("Creating sandbox for conversation %s (user %s)", conversation_id, user_id)

        template = settings.e2b_sandbox_template_id or "base"
        sandbox = await asyncio.to_thread(
            Sandbox,
            template=template,
            api_key=settings.e2b_api_key,
            env_vars=env_vars,
            timeout=300,  # 5 min keep-alive by default
        )

        info = SandboxInfo(
            sandbox_id=sandbox.sandbox_id,
            conversation_id=conversation_id,
            user_id=user_id,
            state=SandboxState.active,
            sandbox=sandbox,
        )

        key = self._key(conversation_id)
        self._sandboxes[key] = info

        logger.info("Sandbox %s created for conversation %s", sandbox.sandbox_id, conversation_id)
        return info

    async def _resume_sandbox(self, info: SandboxInfo) -> SandboxInfo:
        """Resume a paused sandbox from its snapshot."""
        logger.info("Resuming sandbox %s for conversation %s", info.sandbox_id, info.conversation_id)
        info.state = SandboxState.resuming

        settings = get_settings()
        try:
            sandbox = await asyncio.to_thread(
                Sandbox.reconnect,
                info.sandbox_id,
                api_key=settings.e2b_api_key,
            )
            info.sandbox = sandbox
            info.state = SandboxState.active
            info.last_active_at = time.time()
            logger.info("Sandbox %s resumed", info.sandbox_id)
        except Exception:
            logger.warning("Failed to resume sandbox %s, creating new one", info.sandbox_id)
            # Sandbox snapshot expired or corrupted — create fresh
            key = self._key(info.conversation_id)
            self._sandboxes.pop(key, None)
            return await self._create_sandbox(
                info.user_id, info.conversation_id
            )

        return info

    async def pause_sandbox(self, conversation_id: str) -> bool:
        """Pause (snapshot) a sandbox to save costs."""
        key = self._key(conversation_id)
        async with self._lock:
            info = self._sandboxes.get(key)
            if not info or info.state != SandboxState.active:
                return False

            try:
                if info.sandbox:
                    await asyncio.to_thread(info.sandbox.pause)
                info.state = SandboxState.paused
                info.sandbox = None
                logger.info("Sandbox %s paused", info.sandbox_id)
                return True
            except Exception:
                logger.exception("Failed to pause sandbox %s", info.sandbox_id)
                return False

    async def destroy_sandbox(self, conversation_id: str) -> bool:
        """Destroy a sandbox and remove it from tracking."""
        key = self._key(conversation_id)
        async with self._lock:
            info = self._sandboxes.pop(key, None)
            if not info:
                return False
            await self._destroy_sandbox(info)
            return True

    async def _destroy_sandbox(self, info: SandboxInfo):
        """Internal: kill the sandbox process."""
        try:
            if info.sandbox:
                await asyncio.to_thread(info.sandbox.kill)
            info.state = SandboxState.destroyed
            logger.info("Sandbox %s destroyed", info.sandbox_id)
        except Exception:
            logger.exception("Failed to destroy sandbox %s", info.sandbox_id)

    async def execute_code(
        self,
        conversation_id: str,
        code: str,
        language: str = "python",
    ) -> dict:
        """Execute code inside a conversation's sandbox.

        Returns {"stdout": str, "stderr": str, "error": str | None, "results": list}.
        """
        key = self._key(conversation_id)
        info = self._sandboxes.get(key)
        if not info or not info.sandbox or info.state != SandboxState.active:
            raise RuntimeError(f"No active sandbox for conversation {conversation_id}")

        info.last_active_at = time.time()

        execution = await asyncio.to_thread(
            info.sandbox.run_code,
            code,
            language=language,
        )

        return {
            "stdout": execution.logs.stdout,
            "stderr": execution.logs.stderr,
            "error": str(execution.error) if execution.error else None,
            "results": [r.text for r in execution.results if hasattr(r, "text")],
        }

    def get_sandbox_info(self, conversation_id: str) -> SandboxInfo | None:
        """Get sandbox info without modifying state."""
        return self._sandboxes.get(self._key(conversation_id))

    def get_active_count(self) -> int:
        """Number of currently active (not paused/destroyed) sandboxes."""
        return sum(
            1 for info in self._sandboxes.values()
            if info.state == SandboxState.active
        )

    async def _reaper_loop(self):
        """Background task: pause idle sandboxes, destroy expired ones."""
        while True:
            try:
                await asyncio.sleep(60)  # Check every minute
                now = time.time()

                async with self._lock:
                    for key, info in list(self._sandboxes.items()):
                        # Destroy sandboxes past max lifetime
                        if now - info.created_at > MAX_LIFETIME_SECONDS:
                            logger.info("Sandbox %s exceeded max lifetime, destroying", info.sandbox_id)
                            await self._destroy_sandbox(info)
                            self._sandboxes.pop(key, None)
                            continue

                        # Pause idle active sandboxes
                        if (
                            info.state == SandboxState.active
                            and now - info.last_active_at > IDLE_TIMEOUT_SECONDS
                        ):
                            logger.info("Sandbox %s idle for %ds, pausing", info.sandbox_id, IDLE_TIMEOUT_SECONDS)
                            try:
                                if info.sandbox:
                                    await asyncio.to_thread(info.sandbox.pause)
                                info.state = SandboxState.paused
                                info.sandbox = None
                            except Exception:
                                logger.exception("Failed to pause idle sandbox %s", info.sandbox_id)

            except asyncio.CancelledError:
                break
            except Exception:
                logger.exception("Error in sandbox reaper loop")


# ── Module-level singleton ─────────────────────────────────────────────────

_manager: SandboxManager | None = None


def get_sandbox_manager() -> SandboxManager:
    """Get the global sandbox manager singleton."""
    global _manager
    if _manager is None:
        _manager = SandboxManager()
    return _manager
