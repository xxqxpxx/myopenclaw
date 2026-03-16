"use client";

import { useEffect, useRef, useState } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import { useChatStore } from "@/lib/store";

const TOOL_OUTPUT_TRUNCATE_CHARS = 400;

const EXAMPLE_PROMPTS = [
  "Write a Python script to analyze a CSV file",
  "Search the web for the latest AI news",
  "Create a weekly schedule spreadsheet",
  "Help me draft a professional email",
];

function renderMarkdown(text: string): React.ReactNode {
  // Split on code spans first so we don't mangle them
  const parts = text.split(/(`[^`]+`)/g);
  const nodes: React.ReactNode[] = [];

  parts.forEach((part, partIdx) => {
    if (part.startsWith("`") && part.endsWith("`") && part.length > 2) {
      // Inline code
      nodes.push(
        <code
          key={partIdx}
          className="bg-gray-700 px-1 rounded text-sm font-mono"
        >
          {part.slice(1, -1)}
        </code>
      );
      return;
    }

    // Bold + line-break pass on plain segments
    const boldParts = part.split(/(\*\*[^*]+\*\*)/g);
    boldParts.forEach((bp, bpIdx) => {
      if (bp.startsWith("**") && bp.endsWith("**") && bp.length > 4) {
        nodes.push(<strong key={`${partIdx}-${bpIdx}`}>{bp.slice(2, -2)}</strong>);
        return;
      }
      // Newlines → <br />
      const lines = bp.split("\n");
      lines.forEach((line, lineIdx) => {
        if (lineIdx > 0) nodes.push(<br key={`${partIdx}-${bpIdx}-br-${lineIdx}`} />);
        if (line) nodes.push(<span key={`${partIdx}-${bpIdx}-${lineIdx}`}>{line}</span>);
      });
    });
  });

  return <>{nodes}</>;
}

function formatFileSize(bytes?: number): string {
  if (bytes == null) return "";
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
  return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

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
    filename?: string;
    url?: string;
    size?: number;
  };
}) {
  const [toolExpanded, setToolExpanded] = useState(false);

  if (msg.role === "tool") {
    const output = msg.toolOutput ?? "";
    const isTruncated = output.length > TOOL_OUTPUT_TRUNCATE_CHARS;
    const displayOutput = isTruncated && !toolExpanded
      ? output.slice(0, TOOL_OUTPUT_TRUNCATE_CHARS) + "…"
      : output;

    return (
      <div className="mx-4 my-2 p-3 bg-gray-800/50 border border-gray-700 rounded-lg text-sm">
        <div className="flex items-center gap-2 text-yellow-400 font-medium mb-1">
          <span>⚡</span>
          <span>{msg.toolName || "Tool"}</span>
        </div>
        {/* Tool call line */}
        <pre className="text-gray-400 whitespace-pre-wrap text-xs overflow-x-auto mb-1">
          {msg.content}
        </pre>
        {/* Tool output, shown only when it exists */}
        {output.length > 0 && (
          <>
            <pre className="text-gray-300 whitespace-pre-wrap text-xs overflow-x-auto mt-1 border-t border-gray-700 pt-2">
              {displayOutput}
            </pre>
            {isTruncated && (
              <button
                onClick={() => setToolExpanded((v) => !v)}
                className="mt-1 text-xs text-blue-400 hover:text-blue-300 underline"
              >
                {toolExpanded ? "Show less" : "Show more"}
              </button>
            )}
          </>
        )}
      </div>
    );
  }

  if (msg.role === "file") {
    const sizeLabel = formatFileSize(msg.size);
    return (
      <div className="mx-4 my-2 px-4 py-3 bg-gray-800/50 border border-gray-700 rounded-lg text-sm flex items-center gap-3">
        <span className="text-2xl">📎</span>
        <div className="flex-1 min-w-0">
          <div className="text-gray-200 font-medium truncate">
            {msg.filename || msg.content}
          </div>
          {sizeLabel && (
            <div className="text-gray-500 text-xs">{sizeLabel}</div>
          )}
        </div>
        {msg.url && (
          <a
            href={msg.url}
            target="_blank"
            rel="noopener noreferrer"
            className="shrink-0 px-3 py-1 bg-blue-600 hover:bg-blue-700 text-white text-xs font-medium rounded-lg transition-colors"
          >
            Download
          </a>
        )}
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
        <div className="whitespace-pre-wrap break-words">
          {isUser ? msg.content : renderMarkdown(msg.content)}
        </div>
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
  const inputRef = useRef<HTMLTextAreaElement>(null);

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

  // Focus input whenever the conversation changes
  useEffect(() => {
    inputRef.current?.focus();
  }, [conversationId]);

  function handleSend(text?: string) {
    const toSend = (text ?? input).trim();
    if (!toSend || isStreaming) return;
    setInput("");
    sendMessage(toSend);
  }

  function handleKeyDown(e: React.KeyboardEvent) {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  }

  function handlePromptClick(prompt: string) {
    setInput(prompt);
    // Send after state flush so sendMessage picks up the value
    setTimeout(() => {
      handleSend(prompt);
    }, 0);
  }

  const isOutOfCredits =
    error != null &&
    (error.toLowerCase().includes("out of credits") ||
      error.toLowerCase().includes("insufficient credits"));

  return (
    <div className="flex flex-col h-full">
      {/* Messages */}
      <div className="flex-1 overflow-y-auto py-4">
        {messages.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full gap-6 px-4">
            <div className="text-center">
              <div className="text-4xl mb-3">QuickClaw</div>
              <p className="text-gray-400 text-sm">
                Your AI agent, no setup required.
              </p>
            </div>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 w-full max-w-xl">
              {EXAMPLE_PROMPTS.map((prompt) => (
                <button
                  key={prompt}
                  onClick={() => handlePromptClick(prompt)}
                  className="text-left px-4 py-3 bg-gray-800 border border-gray-700 rounded-xl text-sm text-gray-300 hover:bg-gray-700 hover:border-gray-600 transition-colors"
                >
                  {prompt}
                </button>
              ))}
            </div>
          </div>
        ) : (
          messages.map((msg, i) => (
            <MessageBubble key={msg.id || i} msg={msg} />
          ))
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Error banner */}
      {error && (
        <div className="mx-4 mb-2 px-3 py-2 bg-red-900/50 border border-red-800 rounded-lg text-red-300 text-sm">
          {isOutOfCredits ? (
            <>
              Out of credits &mdash;{" "}
              <Link
                href="/settings"
                className="underline hover:text-red-200 transition-colors"
              >
                Buy more credits &rarr;
              </Link>{" "}
              to continue
            </>
          ) : (
            error
          )}
        </div>
      )}

      {/* Input */}
      <div className="border-t border-gray-800 p-4">
        <div className="flex gap-2">
          <textarea
            ref={inputRef}
            autoFocus
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={handleKeyDown}
            placeholder="Message QuickClaw... (Shift+Enter for new line)"
            rows={2}
            className="flex-1 px-3 py-2 bg-gray-800 border border-gray-700 rounded-lg text-white placeholder-gray-500 resize-none focus:outline-none focus:ring-2 focus:ring-blue-500"
          />
          <button
            onClick={() => handleSend()}
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
