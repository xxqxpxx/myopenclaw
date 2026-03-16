"use client";

import { useEffect, useState } from "react";
import { listMemories, deleteMemory, clearAllMemories, MemoryEntry } from "@/lib/api";

function formatDate(iso: string): string {
  const d = new Date(iso);
  return d.toLocaleDateString(undefined, { month: "short", day: "numeric", year: "numeric" });
}

export default function MemoryPage() {
  const [entries, setEntries] = useState<MemoryEntry[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [deleting, setDeleting] = useState<string | null>(null);
  const [clearing, setClearing] = useState(false);
  const [confirmClear, setConfirmClear] = useState(false);

  useEffect(() => {
    loadMemories();
  }, []);

  async function loadMemories() {
    setLoading(true);
    setError(null);
    try {
      const data = await listMemories();
      setEntries(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to load memories");
    } finally {
      setLoading(false);
    }
  }

  async function handleDelete(id: string) {
    setDeleting(id);
    setEntries((prev) => prev.filter((e) => e.id !== id));
    try {
      await deleteMemory(id);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to delete memory");
      await loadMemories();
    } finally {
      setDeleting(null);
    }
  }

  async function handleClearAll() {
    setClearing(true);
    setConfirmClear(false);
    setError(null);
    try {
      await clearAllMemories();
      setEntries([]);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to clear memories");
    } finally {
      setClearing(false);
    }
  }

  const grouped = entries.reduce<Record<string, MemoryEntry[]>>((acc, entry) => {
    const key = entry.category || "general";
    if (!acc[key]) acc[key] = [];
    acc[key].push(entry);
    return acc;
  }, {});

  const categories = Object.keys(grouped).sort();

  return (
    <div className="flex-1 overflow-y-auto">
      <div className="max-w-2xl mx-auto px-6 py-8 w-full">
        {/* Header */}
        <div className="flex items-start justify-between mb-7">
          <div>
            <h1 className="text-2xl font-bold text-white">Memory</h1>
            <p className="text-sm mt-1" style={{ color: "var(--color-text-secondary)" }}>
              What the AI remembers about you
            </p>
          </div>
          {entries.length > 0 && (
            confirmClear ? (
              <div className="flex items-center gap-2">
                <span className="text-xs" style={{ color: "var(--color-text-secondary)" }}>Delete all?</span>
                <button
                  onClick={handleClearAll}
                  disabled={clearing}
                  className="px-3 py-1.5 rounded-xl text-xs font-semibold text-white transition-all disabled:opacity-50"
                  style={{ background: "var(--color-error)" }}
                >
                  {clearing ? "Clearing…" : "Yes, delete"}
                </button>
                <button
                  onClick={() => setConfirmClear(false)}
                  className="px-3 py-1.5 rounded-xl text-xs font-medium transition-all"
                  style={{
                    background: "var(--color-surface-3)",
                    color: "var(--color-text-secondary)",
                  }}
                >
                  Cancel
                </button>
              </div>
            ) : (
              <button
                onClick={() => setConfirmClear(true)}
                disabled={clearing}
                className="px-4 py-2 rounded-xl text-sm font-medium transition-all disabled:opacity-50"
                style={{
                  background: "rgba(239,68,68,0.1)",
                  border: "1px solid rgba(239,68,68,0.2)",
                  color: "#fca5a5",
                }}
              >
                Clear All
              </button>
            )
          )}
        </div>

        {/* Error banner */}
        {error && (
          <div
            className="mb-5 flex items-center gap-2.5 px-4 py-3 rounded-xl text-sm"
            style={{ background: "rgba(239,68,68,0.1)", border: "1px solid rgba(239,68,68,0.25)", color: "#fca5a5" }}
          >
            <svg className="w-4 h-4 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
            </svg>
            {error}
          </div>
        )}

        {/* Loading skeleton */}
        {loading && (
          <div className="space-y-3">
            {[...Array(3)].map((_, i) => (
              <div key={i} className="p-4 rounded-2xl" style={{ background: "var(--color-surface-2)", border: "1px solid var(--color-border)" }}>
                <div className="h-3 w-16 rounded animate-pulse mb-3" style={{ background: "var(--color-surface-3)" }} />
                <div className="h-4 w-3/4 rounded animate-pulse mb-2" style={{ background: "var(--color-surface-3)" }} />
                <div className="h-4 w-1/2 rounded animate-pulse" style={{ background: "var(--color-surface-3)" }} />
              </div>
            ))}
          </div>
        )}

        {/* Empty state */}
        {!loading && entries.length === 0 && (
          <div
            className="flex flex-col items-center justify-center py-16 rounded-2xl text-center"
            style={{ background: "var(--color-surface-2)", border: "1px dashed var(--color-border)" }}
          >
            <div className="text-4xl mb-3">🧠</div>
            <p className="text-sm" style={{ color: "var(--color-text-secondary)" }}>
              No memories yet.
            </p>
            <p className="text-xs mt-1" style={{ color: "var(--color-text-muted)" }}>
              The AI will remember things from your conversations here.
            </p>
          </div>
        )}

        {/* Grouped entries */}
        {!loading && categories.map((category) => (
          <section key={category} className="mb-5">
            <h2
              className="text-[10px] font-semibold uppercase tracking-widest mb-2 px-1"
              style={{ color: "var(--color-accent)" }}
            >
              {category}
            </h2>
            <div className="rounded-2xl overflow-hidden" style={{ background: "var(--color-surface-2)", border: "1px solid var(--color-border)" }}>
              {grouped[category].map((entry, idx) => (
                <div
                  key={entry.id}
                  className="flex items-start gap-3 px-4 py-3"
                  style={idx < grouped[category].length - 1 ? { borderBottom: "1px solid var(--color-border-subtle)" } : undefined}
                >
                  <div className="flex-1 min-w-0">
                    <p className="text-sm break-words leading-relaxed" style={{ color: "var(--color-text-primary)" }}>
                      {entry.content}
                    </p>
                    <span className="text-[11px] mt-1.5 inline-block" style={{ color: "var(--color-text-muted)" }}>
                      {formatDate(entry.created_at)}
                    </span>
                  </div>
                  <button
                    onClick={() => handleDelete(entry.id)}
                    disabled={deleting === entry.id}
                    aria-label="Delete memory"
                    className="shrink-0 w-7 h-7 flex items-center justify-center rounded-lg transition-all disabled:opacity-50 hover:bg-white/5"
                    style={{ color: "var(--color-text-muted)" }}
                  >
                    {deleting === entry.id ? (
                      <svg className="w-3.5 h-3.5 animate-spin" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                      </svg>
                    ) : (
                      <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    )}
                  </button>
                </div>
              ))}
            </div>
          </section>
        ))}
      </div>
    </div>
  );
}
