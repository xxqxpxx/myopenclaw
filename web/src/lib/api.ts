import { API_BASE_URL } from "./config";

/** Typed API client for the myOpenClaw backend */

async function getAuthHeaders(): Promise<Record<string, string>> {
  const { createClient } = await import("./supabase");
  const supabase = createClient();
  const {
    data: { session },
  } = await supabase.auth.getSession();
  if (!session?.access_token) return {};
  return { Authorization: `Bearer ${session.access_token}` };
}

async function apiFetch<T>(
  path: string,
  options: RequestInit = {}
): Promise<T> {
  const headers = {
    "Content-Type": "application/json",
    ...(await getAuthHeaders()),
    ...(options.headers as Record<string, string>),
  };
  const res = await fetch(`${API_BASE_URL}/api/v1${path}`, {
    ...options,
    headers,
  });
  if (!res.ok) {
    const body = await res.json().catch(() => ({ detail: res.statusText }));
    throw new Error(body.detail || res.statusText);
  }
  return res.json();
}

// ── Types ──────────────────────────────────────────────────────────────

export interface Conversation {
  id: string;
  title: string;
  created_at: string | null;
  updated_at: string | null;
  last_message: string | null;
}

export interface Message {
  id: string;
  role: "user" | "assistant" | "tool" | "system";
  content: string;
  model: string | null;
  tokens_used: number;
  created_at: string | null;
}

export interface UserProfile {
  id: string;
  email: string;
  display_name: string | null;
  subscription_tier: string;
  credits_balance: number;
  preferred_model?: string;
}

export interface SSEEvent {
  type: "token" | "tool_start" | "tool_result" | "file" | "error" | "done";
  content?: string;
  model?: string;
  tool?: string;
  input?: Record<string, unknown>;
  output?: string;
  filename?: string;
  url?: string;
  size?: number;
  total_tokens?: number;
  credits_used?: number;
  error?: string;
}

// ── Conversations ──────────────────────────────────────────────────────

export async function listConversations(): Promise<Conversation[]> {
  return apiFetch("/conversations");
}

export async function createConversation(
  title = "New Chat"
): Promise<Conversation> {
  return apiFetch("/conversations", {
    method: "POST",
    body: JSON.stringify({ title }),
  });
}

export async function deleteConversation(id: string): Promise<void> {
  await apiFetch(`/conversations/${id}`, { method: "DELETE" });
}

// ── Messages ───────────────────────────────────────────────────────────

export async function listMessages(conversationId: string): Promise<Message[]> {
  return apiFetch(`/conversations/${conversationId}/messages`);
}

// ── Chat Streaming ─────────────────────────────────────────────────────

export async function streamChat(
  conversationId: string,
  content: string,
  onEvent: (event: SSEEvent) => void,
  model?: string
): Promise<void> {
  const headers = await getAuthHeaders();

  const res = await fetch(
    `${API_BASE_URL}/api/v1/conversations/${conversationId}/chat/stream`,
    {
      method: "POST",
      headers: { "Content-Type": "application/json", ...headers },
      body: JSON.stringify({ content, model }),
    }
  );

  if (!res.ok) {
    const body = await res.json().catch(() => ({ detail: res.statusText }));
    throw new Error(body.detail || res.statusText);
  }

  const reader = res.body?.getReader();
  if (!reader) throw new Error("No response body");

  const decoder = new TextDecoder();
  let buffer = "";

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    buffer += decoder.decode(value, { stream: true });
    const lines = buffer.split("\n");
    buffer = lines.pop() || "";

    for (const line of lines) {
      if (line.startsWith("data: ")) {
        try {
          const event: SSEEvent = JSON.parse(line.slice(6));
          onEvent(event);
        } catch {
          // Skip malformed events
        }
      }
    }
  }
}

// ── User Profile ───────────────────────────────────────────────────────

export async function getProfile(): Promise<UserProfile> {
  return apiFetch("/users/me");
}

export async function getCredits(): Promise<{
  credits_balance: number;
  subscription_tier: string;
}> {
  return apiFetch("/users/me/credits");
}
