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
  if (diffDays < 7) return date.toLocaleDateString("en-US", { weekday: "short" });
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
      // ignore
    }
  }, []);

  useEffect(() => {
    fetchCredits();
    window.addEventListener("focus", fetchCredits);
    return () => window.removeEventListener("focus", fetchCredits);
  }, [fetchCredits]);

  if (balance === null) return null;

  return (
    <div className="px-3 py-2.5 rounded-xl border border-indigo-500/20 bg-indigo-500/10 mb-2">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-1.5">
          <svg className="w-3.5 h-3.5 text-indigo-400" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M11.3 1.046A1 1 0 0112 2v5h4a1 1 0 01.82 1.573l-7 10A1 1 0 018 18v-5H4a1 1 0 01-.82-1.573l7-10a1 1 0 011.12-.38z" clipRule="evenodd" />
          </svg>
          <span className="text-sm font-semibold text-white">{balance.toLocaleString()}</span>
          <span className="text-xs text-indigo-300/70">credits</span>
        </div>
        {tier && (
          <span className="text-xs px-2 py-0.5 rounded-full bg-indigo-500/30 text-indigo-300 font-medium capitalize">
            {tier}
          </span>
        )}
      </div>
    </div>
  );
}

function ConversationSkeleton() {
  return (
    <div className="space-y-1 px-2">
      {[...Array(4)].map((_, i) => (
        <div key={i} className="flex items-center gap-2 px-3 py-2 rounded-lg">
          <div className="flex-1 space-y-1.5">
            <div
              className="h-3 rounded animate-pulse bg-white/5"
              style={{ width: `${60 + (i * 13) % 30}%` }}
            />
            <div className="h-2.5 w-12 rounded animate-pulse bg-white/5" />
          </div>
        </div>
      ))}
    </div>
  );
}

function Sidebar({ onClose }: { onClose?: () => void }) {
  const router = useRouter();
  const pathname = usePathname();
  const [search, setSearch] = useState("");

  const [confirmDeleteId, setConfirmDeleteId] = useState<string | null>(null);

  const { conversations, activeConversationId, loading, loadConversations, newConversation, removeConversation } =
    useChatStore();

  useEffect(() => {
    loadConversations();
  }, [loadConversations]);

  async function handleNew() {
    await newConversation();
    const id = useChatStore.getState().activeConversationId;
    if (id) {
      router.push(`/chat/${id}`);
      onClose?.();
    }
  }

  function handleNav(path: string) {
    router.push(path);
    onClose?.();
    setSearch("");
    setConfirmDeleteId(null);
  }

  const filtered = search
    ? conversations.filter((c) =>
        (c.title ?? "Untitled chat").toLowerCase().includes(search.toLowerCase())
      )
    : conversations;

  return (
    <aside
      className="w-64 flex flex-col h-full"
      style={{ background: "var(--color-surface-1)", borderRight: "1px solid var(--color-border-subtle)" }}
    >
      {/* Header */}
      <div className="px-4 pt-4 pb-3" style={{ borderBottom: "1px solid var(--color-border-subtle)" }}>
        {/* Logo */}
        <div className="flex items-center gap-2.5 mb-4">
          <div className="w-8 h-8 rounded-xl flex items-center justify-center text-sm font-bold text-white"
            style={{ background: "linear-gradient(135deg, #6366f1, #8b5cf6)" }}>
            C
          </div>
          <div>
            <div className="text-sm font-semibold text-white leading-none">myOpenClaw</div>
            <div className="text-xs mt-0.5" style={{ color: "var(--color-text-muted)" }}>AI Agent</div>
          </div>
          {onClose && (
            <button
              onClick={onClose}
              className="ml-auto p-1 rounded-lg hover:bg-white/5 text-gray-500 hover:text-gray-300"
              aria-label="Close sidebar"
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          )}
        </div>

        {/* New Chat */}
        <button
          onClick={handleNew}
          className="w-full flex items-center justify-center gap-2 px-4 py-2 rounded-xl text-sm font-semibold text-white transition-all active:scale-[0.98]"
          style={{ background: "linear-gradient(135deg, #6366f1, #8b5cf6)" }}
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M12 4v16m8-8H4" />
          </svg>
          New Chat
        </button>

        {/* Search */}
        <div className="relative mt-2.5">
          <svg className="absolute left-2.5 top-1/2 -translate-y-1/2 w-3.5 h-3.5 pointer-events-none" style={{ color: "var(--color-text-muted)" }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input
            type="text"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search chats..."
            className="w-full pl-8 pr-3 py-1.5 text-sm text-gray-200 placeholder-gray-600 rounded-lg focus:outline-none focus:ring-1"
            style={{
              background: "var(--color-surface-2)",
              border: "1px solid var(--color-border)",
              "--tw-ring-color": "var(--color-accent)",
            } as React.CSSProperties}
          />
        </div>
      </div>

      {/* Conversation list */}
      <nav className="flex-1 overflow-y-auto py-2">
        {loading && conversations.length === 0 && <ConversationSkeleton />}
        {!loading && search && filtered.length === 0 && (
          <div className="px-4 py-3 text-xs" style={{ color: "var(--color-text-muted)" }}>
            No chats match &quot;{search}&quot;
          </div>
        )}
        {!loading && !search && conversations.length === 0 && (
          <div className="px-4 py-6 text-center">
            <div className="text-2xl mb-2">💬</div>
            <p className="text-xs" style={{ color: "var(--color-text-muted)" }}>No conversations yet</p>
          </div>
        )}
        <div className="px-2">
          {filtered.map((c) => {
            const isActive = pathname === `/chat/${c.id}` || activeConversationId === c.id;
            return (
              <div
                key={c.id}
                className="group flex items-start gap-2 px-3 py-2 rounded-xl cursor-pointer text-sm mb-0.5 relative"
                style={{
                  background: isActive ? "var(--color-surface-3)" : "transparent",
                  color: isActive ? "var(--color-text-primary)" : "var(--color-text-secondary)",
                }}
                onClick={() => handleNav(`/chat/${c.id}`)}
              >
                {isActive && (
                  <div className="absolute left-0 top-1/2 -translate-y-1/2 w-0.5 h-5 rounded-r" style={{ background: "var(--color-accent)" }} />
                )}
                <div className="flex-1 min-w-0">
                  <div className="truncate text-[13px] font-medium">{c.title || "Untitled chat"}</div>
                  <div className="text-[11px] mt-0.5" style={{ color: "var(--color-text-muted)" }}>
                    {formatDate(c.updated_at)}
                  </div>
                </div>
                {confirmDeleteId === c.id ? (
                  <div className="flex items-center gap-1 flex-shrink-0" onClick={(e) => e.stopPropagation()}>
                    <button
                      onClick={() => { removeConversation(c.id); setConfirmDeleteId(null); }}
                      className="px-1.5 py-0.5 rounded text-[10px] font-semibold text-white"
                      style={{ background: "var(--color-error)" }}
                    >
                      Del
                    </button>
                    <button
                      onClick={() => setConfirmDeleteId(null)}
                      className="px-1.5 py-0.5 rounded text-[10px] font-medium"
                      style={{ background: "var(--color-surface-3)", color: "var(--color-text-muted)" }}
                    >
                      No
                    </button>
                  </div>
                ) : (
                  <button
                    onClick={(e) => { e.stopPropagation(); setConfirmDeleteId(c.id); }}
                    className="opacity-0 group-hover:opacity-100 p-1 rounded-md hover:bg-white/10 flex-shrink-0 mt-0.5"
                    style={{ color: "var(--color-text-muted)" }}
                    aria-label="Delete conversation"
                  >
                    <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                )}
              </div>
            );
          })}
        </div>
      </nav>

      {/* Footer */}
      <div className="p-3" style={{ borderTop: "1px solid var(--color-border-subtle)" }}>
        <CreditsDisplay />

        <div className="space-y-0.5">
          <NavButton
            icon={
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
              </svg>
            }
            label="Memory"
            active={pathname === "/memory"}
            onClick={() => handleNav("/memory")}
          />
          <NavButton
            icon={
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
            }
            label="Settings"
            active={pathname === "/settings"}
            onClick={() => handleNav("/settings")}
          />
        </div>
      </div>
    </aside>
  );
}

function NavButton({
  icon,
  label,
  active,
  onClick,
}: {
  icon: React.ReactNode;
  label: string;
  active: boolean;
  onClick: () => void;
}) {
  return (
    <button
      onClick={onClick}
      className="w-full flex items-center gap-2.5 px-3 py-2 rounded-xl text-sm transition-all text-left"
      style={{
        background: active ? "var(--color-surface-3)" : "transparent",
        color: active ? "var(--color-text-primary)" : "var(--color-text-secondary)",
      }}
    >
      {icon}
      {label}
    </button>
  );
}

export default function DashboardLayout({ children }: { children: React.ReactNode }) {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="flex h-screen overflow-hidden" style={{ background: "var(--color-surface-0)" }}>
      {/* Desktop sidebar */}
      <div className="hidden md:flex flex-shrink-0">
        <Sidebar />
      </div>

      {/* Mobile sidebar overlay */}
      {sidebarOpen && (
        <div className="fixed inset-0 z-50 md:hidden">
          <div
            className="absolute inset-0 bg-black/60 backdrop-blur-sm"
            onClick={() => setSidebarOpen(false)}
          />
          <div className="absolute left-0 top-0 bottom-0 w-64 z-10">
            <Sidebar onClose={() => setSidebarOpen(false)} />
          </div>
        </div>
      )}

      {/* Main */}
      <main className="flex-1 flex flex-col overflow-hidden min-w-0">
        {/* Mobile top bar */}
        <div
          className="md:hidden flex items-center gap-3 px-4 py-3 flex-shrink-0"
          style={{ borderBottom: "1px solid var(--color-border-subtle)" }}
        >
          <button
            onClick={() => setSidebarOpen(true)}
            className="p-2 rounded-lg hover:bg-white/5"
            style={{ color: "var(--color-text-secondary)" }}
            aria-label="Open sidebar"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
          <span className="text-sm font-semibold text-white">myOpenClaw</span>
        </div>

        {children}
      </main>
    </div>
  );
}
