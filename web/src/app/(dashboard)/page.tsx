"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";
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

function StatCard({ value, label, accent }: { value: React.ReactNode; label: string; accent?: boolean }) {
  return (
    <div
      className="px-5 py-4 rounded-2xl"
      style={{
        background: "var(--color-surface-2)",
        border: `1px solid ${accent ? "rgba(99,102,241,0.3)" : "var(--color-border)"}`,
        boxShadow: accent ? "0 0 20px rgba(99,102,241,0.08)" : undefined,
      }}
    >
      <div className="text-xl font-bold text-white mb-0.5">{value}</div>
      <div className="text-xs" style={{ color: "var(--color-text-muted)" }}>{label}</div>
    </div>
  );
}

export default function DashboardHome() {
  const router = useRouter();
  const { conversations, loadConversations, newConversation } = useChatStore();
  const [creditsBalance, setCreditsBalance] = useState<number | null>(null);
  const [subscriptionTier, setSubscriptionTier] = useState<string | null>(null);

  useEffect(() => {
    loadConversations();
    getCredits()
      .then((data) => {
        setCreditsBalance(data.credits_balance);
        setSubscriptionTier(data.subscription_tier);
      })
      .catch(() => {});
  }, [loadConversations]);

  async function handleNew() {
    await newConversation();
    const id = useChatStore.getState().activeConversationId;
    if (id) router.push(`/chat/${id}`);
  }

  const recentConversations = conversations.slice(0, 5);

  return (
    <div className="flex-1 overflow-y-auto">
      <div className="max-w-2xl mx-auto px-6 py-12 w-full">
        {/* Hero */}
        <div className="text-center mb-10">
          <div
            className="w-14 h-14 rounded-2xl flex items-center justify-center text-xl font-bold text-white mx-auto mb-5"
            style={{
              background: "linear-gradient(135deg, #6366f1, #8b5cf6)",
              boxShadow: "0 0 40px rgba(99,102,241,0.35)",
            }}
          >
            C
          </div>
          <h1 className="text-3xl font-bold text-white mb-2">myOpenClaw</h1>
          <p className="text-sm" style={{ color: "var(--color-text-secondary)" }}>
            Your AI coding and productivity agent
          </p>
        </div>

        {/* Stats */}
        <div className="grid grid-cols-3 gap-3 mb-10">
          <StatCard value={conversations.length} label="Conversations" />
          <StatCard
            value={creditsBalance !== null ? creditsBalance.toLocaleString() : "—"}
            label="Credits"
            accent
          />
          <StatCard
            value={
              subscriptionTier ? (
                <span
                  className="text-sm px-2.5 py-0.5 rounded-full font-semibold capitalize"
                  style={{ background: "var(--color-accent-subtle)", color: "#a5b4fc" }}
                >
                  {subscriptionTier}
                </span>
              ) : "—"
            }
            label="Plan"
          />
        </div>

        {/* Recent Conversations */}
        <div className="mb-10">
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-xs font-semibold uppercase tracking-widest" style={{ color: "var(--color-text-muted)" }}>
              Recent chats
            </h2>
            {recentConversations.length > 0 && (
              <span className="text-xs" style={{ color: "var(--color-text-muted)" }}>
                {conversations.length} total
              </span>
            )}
          </div>

          {recentConversations.length > 0 ? (
            <div className="space-y-1.5">
              {recentConversations.map((c) => (
                <button
                  key={c.id}
                  onClick={() => router.push(`/chat/${c.id}`)}
                  className="w-full flex items-center justify-between px-4 py-3 rounded-xl text-left transition-all group active:scale-[0.99]"
                  style={{
                    background: "var(--color-surface-2)",
                    border: "1px solid var(--color-border)",
                  }}
                >
                  <div className="flex items-center gap-3 min-w-0">
                    <div className="w-1.5 h-1.5 rounded-full flex-shrink-0" style={{ background: "var(--color-accent)" }} />
                    <div className="min-w-0">
                      <div className="text-sm font-medium text-white truncate">
                        {c.title || "Untitled chat"}
                      </div>
                      <div className="text-[11px] mt-0.5" style={{ color: "var(--color-text-muted)" }}>
                        {formatDate(c.updated_at)}
                      </div>
                    </div>
                  </div>
                  <svg
                    className="w-4 h-4 flex-shrink-0 ml-3 opacity-0 group-hover:opacity-100 transition-opacity"
                    style={{ color: "var(--color-accent)" }}
                    fill="none" stroke="currentColor" viewBox="0 0 24 24"
                  >
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                  </svg>
                </button>
              ))}
            </div>
          ) : (
            <div
              className="flex flex-col items-center justify-center py-12 rounded-2xl text-center"
              style={{
                background: "var(--color-surface-2)",
                border: "1px dashed var(--color-border)",
              }}
            >
              <div className="text-3xl mb-3">💬</div>
              <p className="text-sm mb-5" style={{ color: "var(--color-text-secondary)" }}>
                No conversations yet. Start one to get going.
              </p>
              <button
                onClick={handleNew}
                className="px-5 py-2 rounded-xl text-sm font-semibold text-white transition-all active:scale-95"
                style={{ background: "var(--color-accent)" }}
              >
                Start your first chat
              </button>
            </div>
          )}
        </div>

        {/* Quick Actions */}
        <div>
          <h2 className="text-xs font-semibold uppercase tracking-widest mb-3" style={{ color: "var(--color-text-muted)" }}>
            Quick actions
          </h2>
          <div className="grid grid-cols-3 gap-2.5">
            <button
              onClick={handleNew}
              className="flex items-center justify-center gap-2 px-4 py-3 rounded-xl text-sm font-semibold text-white transition-all active:scale-[0.98]"
              style={{ background: "linear-gradient(135deg, #6366f1, #8b5cf6)" }}
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M12 4v16m8-8H4" />
              </svg>
              New Chat
            </button>
            <Link
              href="/memory"
              className="flex items-center justify-center gap-2 px-4 py-3 rounded-xl text-sm font-medium transition-all active:scale-[0.98]"
              style={{
                background: "var(--color-surface-2)",
                border: "1px solid var(--color-border)",
                color: "var(--color-text-secondary)",
              }}
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z" />
              </svg>
              Memory
            </Link>
            <Link
              href="/settings"
              className="flex items-center justify-center gap-2 px-4 py-3 rounded-xl text-sm font-medium transition-all active:scale-[0.98]"
              style={{
                background: "var(--color-surface-2)",
                border: "1px solid var(--color-border)",
                color: "var(--color-text-secondary)",
              }}
            >
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" /><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              Settings
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}
