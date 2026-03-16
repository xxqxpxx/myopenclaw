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
  if (diffDays < 7) {
    return date.toLocaleDateString("en-US", { weekday: "short" });
  }
  return date.toLocaleDateString("en-US", { month: "short", day: "numeric" });
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
      .catch(() => {
        // Not authenticated yet or endpoint unavailable — silently ignore
      });
  }, [loadConversations]);

  async function handleNew() {
    await newConversation();
    const id = useChatStore.getState().activeConversationId;
    if (id) router.push(`/chat/${id}`);
  }

  const recentConversations = conversations.slice(0, 5);

  return (
    <div className="flex-1 flex flex-col items-center justify-center p-8 max-w-2xl mx-auto w-full">
      <h1 className="text-4xl font-bold mb-1">myOpenClaw</h1>
      <p className="text-gray-400 mb-10">Your open-source AI coding assistant</p>

      {/* Stats row */}
      <div className="grid grid-cols-3 gap-4 w-full mb-10">
        <div className="bg-gray-900 border border-gray-800 rounded-xl px-5 py-4 text-center">
          <div className="text-2xl font-bold">{conversations.length}</div>
          <div className="text-gray-400 text-sm mt-0.5">Conversations</div>
        </div>
        <div className="bg-gray-900 border border-gray-800 rounded-xl px-5 py-4 text-center">
          <div className="text-2xl font-bold">
            {creditsBalance !== null ? creditsBalance.toLocaleString() : "—"}
          </div>
          <div className="text-gray-400 text-sm mt-0.5">Credits</div>
        </div>
        <div className="bg-gray-900 border border-gray-800 rounded-xl px-5 py-4 text-center">
          {subscriptionTier ? (
            <>
              <div className="inline-block text-sm px-2.5 py-0.5 rounded-full bg-blue-600/30 text-blue-300 font-semibold capitalize mb-0.5">
                {subscriptionTier}
              </div>
              <div className="text-gray-400 text-sm mt-0.5">Plan</div>
            </>
          ) : (
            <>
              <div className="text-2xl font-bold text-gray-600">—</div>
              <div className="text-gray-400 text-sm mt-0.5">Plan</div>
            </>
          )}
        </div>
      </div>

      {/* Recent conversations */}
      <div className="w-full mb-10">
        <h2 className="text-sm font-medium text-gray-400 uppercase tracking-wide mb-3">
          Recent conversations
        </h2>

        {recentConversations.length > 0 ? (
          <div className="space-y-2">
            {recentConversations.map((c) => (
              <div
                key={c.id}
                className="flex items-center justify-between px-4 py-3 bg-gray-900 border border-gray-800 rounded-xl hover:bg-gray-800 hover:border-gray-700 transition-colors group"
              >
                <div className="min-w-0 flex-1">
                  <div className="text-sm font-medium truncate">
                    {c.title || "Untitled chat"}
                  </div>
                  <div className="text-xs text-gray-500 mt-0.5">
                    {formatDate(c.updated_at)}
                  </div>
                </div>
                <button
                  onClick={() => router.push(`/chat/${c.id}`)}
                  className="ml-4 text-sm text-blue-400 hover:text-blue-300 font-medium whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity"
                >
                  Continue →
                </button>
              </div>
            ))}
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center py-12 bg-gray-900 border border-gray-800 border-dashed rounded-xl text-center">
            <div className="text-gray-500 text-sm mb-4">
              No conversations yet. Start one to get going.
            </div>
            <button
              onClick={handleNew}
              className="px-5 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium text-sm transition-colors"
            >
              Start your first chat
            </button>
          </div>
        )}
      </div>

      {/* Quick actions */}
      <div className="w-full">
        <h2 className="text-sm font-medium text-gray-400 uppercase tracking-wide mb-3">
          Quick actions
        </h2>
        <div className="grid grid-cols-3 gap-3">
          <button
            onClick={handleNew}
            className="px-4 py-3 bg-blue-600 hover:bg-blue-700 text-white rounded-xl font-medium text-sm transition-colors"
          >
            + New Chat
          </button>
          <Link
            href="/memory"
            className="px-4 py-3 bg-gray-900 border border-gray-800 hover:bg-gray-800 text-gray-300 hover:text-white rounded-xl font-medium text-sm transition-colors text-center"
          >
            Manage Memory →
          </Link>
          <Link
            href="/settings"
            className="px-4 py-3 bg-gray-900 border border-gray-800 hover:bg-gray-800 text-gray-300 hover:text-white rounded-xl font-medium text-sm transition-colors text-center"
          >
            Settings →
          </Link>
        </div>
      </div>
    </div>
  );
}
