"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import { useChatStore } from "@/lib/store";

export default function DashboardHome() {
  const router = useRouter();
  const { conversations, loadConversations, newConversation, creditsBalance } =
    useChatStore();

  useEffect(() => {
    loadConversations();
  }, [loadConversations]);

  async function handleNew() {
    await newConversation();
    const id = useChatStore.getState().activeConversationId;
    if (id) router.push(`/chat/${id}`);
  }

  return (
    <div className="flex-1 flex flex-col items-center justify-center p-8">
      <h1 className="text-4xl font-bold mb-2">myOpenClaw</h1>
      <p className="text-gray-400 mb-8">Your open-source AI coding assistant</p>

      <button
        onClick={handleNew}
        className="px-6 py-3 bg-blue-600 hover:bg-blue-700 text-white rounded-xl font-medium text-lg transition-colors mb-8"
      >
        Start a new chat
      </button>

      {/* Stats */}
      <div className="grid grid-cols-2 gap-4 mb-8 text-center">
        <div className="bg-gray-900 border border-gray-800 rounded-lg px-6 py-4">
          <div className="text-2xl font-bold">{conversations.length}</div>
          <div className="text-gray-400 text-sm">Conversations</div>
        </div>
        <div className="bg-gray-900 border border-gray-800 rounded-lg px-6 py-4">
          <div className="text-2xl font-bold">{creditsBalance}</div>
          <div className="text-gray-400 text-sm">Credits</div>
        </div>
      </div>

      {/* Recent chats */}
      {conversations.length > 0 && (
        <div className="w-full max-w-md">
          <h2 className="text-sm font-medium text-gray-400 mb-2">Recent chats</h2>
          <div className="space-y-1">
            {conversations.slice(0, 5).map((c) => (
              <div
                key={c.id}
                onClick={() => router.push(`/chat/${c.id}`)}
                className="px-4 py-3 bg-gray-900 border border-gray-800 rounded-lg cursor-pointer hover:bg-gray-800 transition-colors"
              >
                <div className="text-sm font-medium truncate">
                  {c.title || "Untitled chat"}
                </div>
                <div className="text-xs text-gray-500 mt-0.5">
                  {c.created_at
                    ? new Date(c.created_at).toLocaleDateString()
                    : ""}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
