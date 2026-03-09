"use client";

import { useEffect, useRef, useState } from "react";
import { useParams } from "next/navigation";
import { useChatStore } from "@/lib/store";

function MessageBubble({
  msg,
}: {
  msg: {
    role: string;
    content: string;
    model?: string;
    isStreaming?: boolean;
    toolName?: string;
    toolOutput?: string;
  };
}) {
  if (msg.role === "tool") {
    return (
      <div className="mx-4 my-2 p-3 bg-gray-800/50 border border-gray-700 rounded-lg text-sm">
        <div className="flex items-center gap-2 text-yellow-400 font-medium mb-1">
          <span>⚡</span>
          <span>{msg.toolName || "Tool"}</span>
        </div>
        <pre className="text-gray-300 whitespace-pre-wrap text-xs overflow-x-auto">
          {msg.content}
        </pre>
      </div>
    );
  }

  const isUser = msg.role === "user";
  return (
    <div className={`flex ${isUser ? "justify-end" : "justify-start"} px-4 py-1`}>
      <div
        className={`max-w-[80%] px-4 py-2 rounded-2xl ${
          isUser
            ? "bg-blue-600 text-white"
            : "bg-gray-800 text-gray-100 border border-gray-700"
        }`}
      >
        <div className="whitespace-pre-wrap break-words">{msg.content}</div>
        {msg.isStreaming && (
          <span className="inline-block w-2 h-4 bg-blue-400 animate-pulse ml-1" />
        )}
        {msg.model && !isUser && (
          <div className="text-xs text-gray-500 mt-1">{msg.model}</div>
        )}
      </div>
    </div>
  );
}

export default function ChatPage() {
  const params = useParams();
  const conversationId = params.id as string;
  const [input, setInput] = useState("");
  const messagesEndRef = useRef<HTMLDivElement>(null);

  const { messages, isStreaming, error, selectConversation, sendMessage } =
    useChatStore();

  useEffect(() => {
    if (conversationId) {
      selectConversation(conversationId);
    }
  }, [conversationId, selectConversation]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  function handleSend() {
    const text = input.trim();
    if (!text || isStreaming) return;
    setInput("");
    sendMessage(text);
  }

  function handleKeyDown(e: React.KeyboardEvent) {
    if (e.key === "Enter" && (e.metaKey || e.ctrlKey)) {
      e.preventDefault();
      handleSend();
    }
  }

  return (
    <div className="flex flex-col h-full">
      {/* Messages */}
      <div className="flex-1 overflow-y-auto py-4">
        {messages.length === 0 && (
          <div className="flex items-center justify-center h-full text-gray-500">
            Send a message to start chatting
          </div>
        )}
        {messages.map((msg, i) => (
          <MessageBubble key={msg.id || i} msg={msg} />
        ))}
        <div ref={messagesEndRef} />
      </div>

      {/* Error banner */}
      {error && (
        <div className="mx-4 mb-2 px-3 py-2 bg-red-900/50 border border-red-800 rounded-lg text-red-300 text-sm">
          {error}
        </div>
      )}

      {/* Input */}
      <div className="border-t border-gray-800 p-4">
        <div className="flex gap-2">
          <textarea
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Type a message... (Cmd+Enter to send)"
            rows={2}
            className="flex-1 px-3 py-2 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 resize-none focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <button
            onClick={handleSend}
            disabled={isStreaming || !input.trim()}
            className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium disabled:opacity-50 transition-colors self-end"
          >
            {isStreaming ? "..." : "Send"}
          </button>
        </div>
      </div>
    </div>
  );
}
