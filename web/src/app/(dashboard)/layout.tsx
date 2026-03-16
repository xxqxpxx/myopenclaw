"use client";

import { useEffect, useState, useCallback } from "react";
import { useRouter, usePathname } from "next/navigation";
import { useChatStore } from "@/lib/store";
import { getCredits } from "@/lib/api";

function formatDate(dateStr: string | null): string {
  if (!dateStr) return "";
  const date = new Date(dateStr);
  const now = new Date();
  const diffMs = now.getTime() - date.getTime();
  const diffDays = Math.floor(diffMs / (1000 * 60 * 60 * 24));

  if (diffDays === 0) return "Today";
  if (diffDays === 1) return "Yesterday";
  if (diffDays < 7) {
    return date.toLocaleDateString("en-US", { weekday: "short" });
  }
  return date.toLocaleDateString("en-US", { month: "short", day: "numeric" });
}

function CreditsDisplay() {
  const [balance, setBalance] = useState<number | null>(null);
  const [tier, setTier] = useState<string | null>(null);

  const fetchCredits = useCallback(async () => {
    try {
      const data = await getCredits();
      setBalance(data.credits_balance);
      setTier(data.subscription_tier);
    } catch {
      // Silently ignore — user may not be authenticated yet
    }
  }, []);

  useEffect(() => {
    fetchCredits();

    function handleFocus() {
      fetchCredits();
    }
    window.addEventListener("focus", handleFocus);
    return () => window.removeEventListener("focus", handleFocus);
  }, [fetchCredits]);

  if (balance === null) return null;

  return (
    <div className="px-3 py-2 rounded-lg bg-gray-800/60 mb-2">
      <div className="text-white text-sm font-medium">
        ⚡ {balance.toLocaleString()} credits
      </div>
      {tier && (
        <div className="mt-1">
          <span className="text-xs px-2 py-0.5 rounded-full bg-blue-600/30 text-blue-300 font-medium capitalize">
            {tier}
          </span>
        </div>
      )}
    </div>
  );
}

function Sidebar() {
  const router = useRouter();
  const pathname = usePathname();
  const [search, setSearch] = useState("");

  const {
    conversations,
    activeConversationId,
    loading,
    loadConversations,
    newConversation,
    removeConversation,
  } = useChatStore();

  useEffect(() => {
    loadConversations();
  }, [loadConversations]);

  async function handleNew() {
    await newConversation();
    const id = useChatStore.getState().activeConversationId;
    if (id) router.push(`/chat/${id}`);
  }

  const filtered = search
    ? conversations.filter((c) =>
        (c.title ?? "Untitled chat").toLowerCase().includes(search.toLowerCase())
      )
    : conversations;

  return (
    <aside className="w-64 bg-gray-900 border-r border-gray-800 flex flex-col h-full">
      <div className="p-4 border-b border-gray-800 space-y-2">
        <button
          onClick={handleNew}
          className="w-full px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors"
        >
          + New Chat
        </button>
        <div className="relative">
          <span className="absolute left-2.5 top-1/2 -translate-y-1/2 text-gray-500 text-xs pointer-events-none">
            🔍
          </span>
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search conversations..."
            className="w-full pl-7 pr-3 py-1.5 bg-gray-800 border border-gray-700 rounded-lg text-sm text-gray-200 placeholder-gray-500 focus:outline-none focus:border-blue-500 transition-colors"
          />
        </div>
      </div>

      <nav className="flex-1 overflow-y-auto p-2">
        {loading && conversations.length === 0 && (
          <div className="text-gray-500 text-sm p-2">Loading…</div>
        )}
        {!loading && search && filtered.length === 0 && (
          <div className="text-gray-600 text-xs px-3 py-2">No matches</div>
        )}
        {filtered.map((c) => {
          const isActive = pathname === `/chat/${c.id}`;
          return (
            <div
              key={c.id}
              className={`group flex items-start gap-2 px-3 py-2 rounded-lg cursor-pointer text-sm mb-0.5 ${
                isActive
                  ? "bg-gray-800 text-white"
                  : "text-gray-400 hover:bg-gray-800/50 hover:text-gray-200"
              }`}
              onClick={() => {
                setSearch("");
                router.push(`/chat/${c.id}`);
              }}
            >
              <div className="flex-1 min-w-0">
                <div className="truncate">{c.title || "Untitled chat"}</div>
                <div className="text-xs text-gray-600 mt-0.5">
                  {formatDate(c.updated_at)}
                </div>
              </div>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  removeConversation(c.id);
                }}
                className="opacity-0 group-hover:opacity-100 text-gray-500 hover:text-red-400 transition-opacity mt-0.5 flex-shrink-0"
                aria-label="Delete conversation"
              >
                ×
              </button>
            </div>
          );
        })}
      </nav>

      <div className="p-4 border-t border-gray-800">
        <CreditsDisplay />
        <button
          onClick={() => router.push("/memory")}
          className={`w-full px-4 py-2 text-sm rounded-lg transition-colors text-left mb-1 ${
            pathname === "/memory"
              ? "bg-gray-800 text-white"
              : "text-gray-400 hover:text-white hover:bg-gray-800"
          }`}
          data-testid="nav-memory"
        >
          🧠 Memory
        </button>
        <button
          onClick={() => router.push("/settings")}
          className={`w-full px-4 py-2 text-sm rounded-lg transition-colors text-left ${
            pathname === "/settings"
              ? "bg-gray-800 text-white"
              : "text-gray-400 hover:text-white hover:bg-gray-800"
          }`}
          data-testid="nav-settings"
        >
          ⚙ Settings
        </button>
      </div>
    </aside>
  );
}

export default function DashboardLayout({
  children,
}: {
  children: React.ReactNode;
}) {
  return (
    <div className="flex h-screen bg-gray-950 text-white">
      <Sidebar />
      <main className="flex-1 flex flex-col overflow-hidden">{children}</main>
    </div>
  );
}
