"use client";

import { useEffect, useRef, useState, memo } from "react";
import { useParams } from "next/navigation";
import Link from "next/link";
import { useChatStore } from "@/lib/store";

const TOOL_OUTPUT_TRUNCATE_CHARS = 400;

const EXAMPLE_PROMPTS = [
  { icon: "🐍", text: "Write a Python script to analyze a CSV file" },
  { icon: "🔍", text: "Search the web for the latest AI news" },
  { icon: "📊", text: "Create a weekly schedule spreadsheet" },
  { icon: "✉️", text: "Help me draft a professional email" },
];

function renderMarkdown(text: string): React.ReactNode {
  const parts = text.split(/(`[^`]+`)/g);
  const nodes: React.ReactNode[] = [];

  parts.forEach((part, partIdx) => {
    if (part.startsWith("`") && part.endsWith("`") && part.length > 2) {
      nodes.push(
        <code
          key={partIdx}
          className="px-1.5 py-0.5 rounded text-[13px] font-mono"
          style={{ background: "var(--color-surface-3)", color: "#a5f3fc" }}
        >
          {part.slice(1, -1)}
        </code>
      );
      return;
    }

    const boldParts = part.split(/(\*\*[^*]+\*\*)/g);
    boldParts.forEach((bp, bpIdx) => {
      if (bp.startsWith("**") && bp.endsWith("**") && bp.length > 4) {
        nodes.push(<strong key={`${partIdx}-${bpIdx}`} className="font-semibold text-white">{bp.slice(2, -2)}</strong>);
        return;
      }
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

const MessageBubble = memo(function MessageBubble({
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
      <div className="mx-4 my-2 rounded-xl overflow-hidden text-sm"
        style={{ background: "var(--color-surface-2)", border: "1px solid var(--color-border)" }}>
        <div className="flex items-center gap-2 px-3 py-2"
          style={{ borderBottom: "1px solid var(--color-border)", background: "var(--color-surface-3)" }}>
          <div className="w-5 h-5 rounded-md flex items-center justify-center"
            style={{ background: "rgba(245, 158, 11, 0.15)" }}>
            <svg className="w-3 h-3 text-amber-400" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M11.3 1.046A1 1 0 0112 2v5h4a1 1 0 01.82 1.573l-7 10A1 1 0 018 18v-5H4a1 1 0 01-.82-1.573l7-10a1 1 0 011.12-.38z" clipRule="evenodd" />
            </svg>
          </div>
          <span className="font-medium text-amber-400 text-[13px]">{msg.toolName || "Tool"}</span>
        </div>
        <div className="px-3 py-2">
          <pre className="text-xs whitespace-pre-wrap overflow-x-auto mb-1 leading-relaxed"
            style={{ color: "var(--color-text-secondary)" }}>
            {msg.content}
          </pre>
          {output.length > 0 && (
            <>
              <div className="my-2 h-px" style={{ background: "var(--color-border)" }} />
              <pre className="text-xs whitespace-pre-wrap overflow-x-auto leading-relaxed"
                style={{ color: "var(--color-text-primary)" }}>
                {displayOutput}
              </pre>
              {isTruncated && (
                <button
                  onClick={() => setToolExpanded((v) => !v)}
                  className="mt-2 text-xs font-medium hover:underline"
                  style={{ color: "var(--color-accent)" }}
                >
                  {toolExpanded ? "Show less ↑" : "Show more ↓"}
                </button>
              )}
            </>
          )}
        </div>
      </div>
    );
  }

  if (msg.role === "file") {
    const sizeLabel = formatFileSize(msg.size);
    return (
      <div className="mx-4 my-2 px-4 py-3 rounded-xl flex items-center gap-3 text-sm"
        style={{ background: "var(--color-surface-2)", border: "1px solid var(--color-border)" }}>
        <div className="w-9 h-9 rounded-xl flex items-center justify-center flex-shrink-0"
          style={{ background: "var(--color-accent-subtle)" }}>
          <svg className="w-4 h-4" style={{ color: "var(--color-accent)" }} fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15.172 7l-6.586 6.586a2 2 0 102.828 2.828l6.414-6.586a4 4 0 00-5.656-5.656l-6.415 6.585a6 6 0 108.486 8.486L20.5 13" />
          </svg>
        </div>
        <div className="flex-1 min-w-0">
          <div className="font-medium text-white truncate">{msg.filename || msg.content}</div>
          {sizeLabel && <div className="text-[11px] mt-0.5" style={{ color: "var(--color-text-muted)" }}>{sizeLabel}</div>}
        </div>
        {msg.url && (
          <a
            href={msg.url}
            target="_blank"
            rel="noopener noreferrer"
            className="shrink-0 flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold text-white transition-colors"
            style={{ background: "var(--color-accent)" }}
          >
            <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" />
            </svg>
            Download
          </a>
        )}
      </div>
    );
  }

  const isUser = msg.role === "user";
  return (
    <div className={`flex ${isUser ? "justify-end" : "justify-start"} px-4 py-1`}>
      {!isUser && (
        <div className="w-7 h-7 rounded-full flex items-center justify-center text-xs font-bold text-white mr-2 mt-1 flex-shrink-0"
          style={{ background: "linear-gradient(135deg, #6366f1, #8b5cf6)" }}>
          AI
        </div>
      )}
      <div className={`max-w-[80%] ${isUser ? "max-w-[70%]" : ""}`}>
        <div
          className={`px-4 py-3 rounded-2xl text-[14px] leading-relaxed ${isUser ? "rounded-tr-sm" : "rounded-tl-sm"}`}
          style={
            isUser
              ? { background: "var(--color-accent)", color: "#fff" }
              : { background: "var(--color-surface-2)", color: "var(--color-text-primary)", border: "1px solid var(--color-border)" }
          }
        >
          <div className="whitespace-pre-wrap break-words">
            {isUser ? msg.content : renderMarkdown(msg.content)}
          </div>
          {msg.isStreaming && (
            <span className="inline-flex items-center gap-0.5 ml-1">
              {[0, 150, 300].map((delay) => (
                <span
                  key={delay}
                  className="w-1 h-1 rounded-full animate-bounce"
                  style={{ background: "var(--color-accent)", animationDelay: `${delay}ms` }}
                />
              ))}
            </span>
          )}
        </div>
        {msg.model && !isUser && (
          <div className="flex items-center gap-1 mt-1 px-1">
            <div className="w-1.5 h-1.5 rounded-full" style={{ background: "var(--color-success)" }} />
            <span className="text-[11px]" style={{ color: "var(--color-text-muted)" }}>
              {msg.model}
            </span>
          </div>
        )}
      </div>
    </div>
  );
});

export default function ChatPage() {
  const params = useParams();
  const conversationId = params.id as string;
  const [input, setInput] = useState("");
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLTextAreaElement>(null);
  const [inputRows, setInputRows] = useState(1);

  const { messages, isStreaming, error, selectConversation, sendMessage } = useChatStore();

  useEffect(() => {
    if (conversationId) selectConversation(conversationId);
  }, [conversationId, selectConversation]);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  useEffect(() => {
    inputRef.current?.focus();
  }, [conversationId]);

  // Auto-resize textarea
  function handleInputChange(e: React.ChangeEvent<HTMLTextAreaElement>) {
    setInput(e.target.value);
    const lines = e.target.value.split("\n").length;
    setInputRows(Math.min(Math.max(lines, 1), 6));
  }

  function handleSend(text?: string) {
    const toSend = (text ?? input).trim();
    if (!toSend || isStreaming) return;
    setInput("");
    setInputRows(1);
    sendMessage(toSend);
  }

  function handleKeyDown(e: React.KeyboardEvent) {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      handleSend();
    }
  }

  function handlePromptClick(prompt: string) {
    handleSend(prompt);
  }

  const isOutOfCredits =
    error != null &&
    (error.toLowerCase().includes("out of credits") || error.toLowerCase().includes("insufficient credits"));

  return (
    <div className="flex flex-col h-full min-h-0">
      {/* Messages area */}
      <div className="flex-1 overflow-y-auto">
        {messages.length === 0 ? (
          <div className="flex flex-col items-center justify-center h-full gap-8 px-6 py-12">
            <div className="text-center">
              <div
                className="w-16 h-16 rounded-2xl flex items-center justify-center text-2xl font-bold text-white mx-auto mb-4"
                style={{ background: "linear-gradient(135deg, #6366f1, #8b5cf6)", boxShadow: "0 0 40px rgba(99,102,241,0.3)" }}
              >
                C
              </div>
              <h1 className="text-2xl font-bold text-white mb-2">How can I help you?</h1>
              <p className="text-sm" style={{ color: "var(--color-text-secondary)" }}>
                I can write code, search the web, analyze files, and more.
              </p>
            </div>

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5 w-full max-w-xl">
              {EXAMPLE_PROMPTS.map(({ icon, text }) => (
                <button
                  key={text}
                  onClick={() => handlePromptClick(text)}
                  disabled={isStreaming}
                  className="group flex items-start gap-3 text-left px-4 py-3 rounded-xl transition-all active:scale-[0.98] disabled:opacity-40 disabled:cursor-not-allowed"
                  style={{
                    background: "var(--color-surface-2)",
                    border: "1px solid var(--color-border)",
                  }}
                >
                  <span className="text-lg mt-0.5 flex-shrink-0">{icon}</span>
                  <span className="text-sm leading-snug" style={{ color: "var(--color-text-secondary)" }}>
                    {text}
                  </span>
                </button>
              ))}
            </div>
          </div>
        ) : (
          <div className="py-4 space-y-1">
            {messages.map((msg, i) => (
              <MessageBubble key={msg.id || i} msg={msg} />
            ))}
          </div>
        )}
        <div ref={messagesEndRef} />
      </div>

      {/* Error banner */}
      {error && (
        <div className="mx-4 mb-2 px-4 py-2.5 rounded-xl flex items-center gap-2.5 text-sm"
          style={{ background: "rgba(239,68,68,0.1)", border: "1px solid rgba(239,68,68,0.25)", color: "#fca5a5" }}>
          <svg className="w-4 h-4 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
          </svg>
          {isOutOfCredits ? (
            <span>
              Out of credits —{" "}
              <Link href="/settings" className="font-semibold underline hover:text-red-200">
                Buy more credits
              </Link>{" "}
              to continue
            </span>
          ) : (
            error
          )}
        </div>
      )}

      {/* Input area */}
      <div className="px-4 pb-4">
        <div
          className="flex items-end gap-2 p-2 rounded-2xl"
          style={{
            background: "var(--color-surface-2)",
            border: "1px solid var(--color-border)",
            boxShadow: "0 0 0 1px transparent",
          }}
        >
          <textarea
            ref={inputRef}
            autoFocus
            value={input}
            onChange={handleInputChange}
            onKeyDown={handleKeyDown}
            placeholder="Message myOpenClaw…"
            rows={inputRows}
            className="flex-1 bg-transparent text-white placeholder-gray-600 resize-none focus:outline-none text-sm leading-relaxed py-1.5 px-2 overflow-y-auto"
            style={{ maxHeight: "180px" }}
          />
          <button
            onClick={() => handleSend()}
            disabled={isStreaming || !input.trim()}
            className="flex-shrink-0 w-8 h-8 rounded-xl flex items-center justify-center transition-all active:scale-95 disabled:opacity-30"
            style={{
              background: input.trim() && !isStreaming ? "var(--color-accent)" : "var(--color-surface-3)",
            }}
            aria-label="Send message"
          >
            {isStreaming ? (
              <svg className="w-3.5 h-3.5 text-white animate-spin" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
              </svg>
            ) : (
              <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M5 12h14M12 5l7 7-7 7" />
              </svg>
            )}
          </button>
        </div>
        <p className="text-center text-[11px] mt-1.5" style={{ color: "var(--color-text-muted)" }}>
          Shift+Enter for new line · Enter to send
        </p>
      </div>
    </div>
  );
}
