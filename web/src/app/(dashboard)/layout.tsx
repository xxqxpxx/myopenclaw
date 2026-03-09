"use client";

import { useEffect } from "react";
import { useRouter, usePathname } from "next/navigation";
import { useChatStore } from "@/lib/store";

function Sidebar() {
  const router = useRouter();
  const pathname = usePathname();

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

  return (
    <aside className="w-64 bg-gray-900 border-r border-gray-800 flex flex-col h-full">
      <div className="p-4 border-b border-gray-800">
        <button
          onClick={handleNew}
          className="w-full px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors"
        >
          + New Chat
        </button>
      </div>

      <nav className="flex-1 overflow-y-auto p-2">
        {loading && conversations.length === 0 && (
          <div className="text-gray-500 text-sm p-2">Loading…</div>
        )}
        {conversations.map((c) => {
          const isActive = pathname === `/chat/${c.id}`;
          return (
            <div
              key={c.id}
              className={`group flex items-center gap-2 px-3 py-2 rounded-lg cursor-pointer text-sm mb-0.5 ${
                isActive
                  ? "bg-gray-800 text-white"
                  : "text-gray-400 hover:bg-gray-800/50 hover:text-gray-200"
              }`}
              onClick={() => router.push(`/chat/${c.id}`)}
            >
              <span className="flex-1 truncate">
                {c.title || "Untitled chat"}
              </span>
              <button
                onClick={(e) => {
                  e.stopPropagation();
                  removeConversation(c.id);
                }}
                className="opacity-0 group-hover:opacity-100 text-gray-500 hover:text-red-400 transition-opacity"
                aria-label="Delete conversation"
              >
                ×
              </button>
            </div>
          );
        })}
      </nav>

      <div className="p-4 border-t border-gray-800">
        <button
          onClick={() => router.push("/settings")}
          className="w-full px-4 py-2 text-gray-400 hover:text-white text-sm rounded-lg hover:bg-gray-800 transition-colors text-left"
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
