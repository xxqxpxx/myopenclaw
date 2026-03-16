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
    // Optimistic update
    setEntries((prev) => prev.filter((e) => e.id !== id));
    try {
      await deleteMemory(id);
    } catch (err) {
      // Revert on failure
      setError(err instanceof Error ? err.message : "Failed to delete memory");
      await loadMemories();
    } finally {
      setDeleting(null);
    }
  }

  async function handleClearAll() {
    if (!confirm("Delete all memories? This cannot be undone.")) return;
    setClearing(true);
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

  // Group entries by category
  const grouped = entries.reduce<Record<string, MemoryEntry[]>>((acc, entry) => {
    const key = entry.category || "general";
    if (!acc[key]) acc[key] = [];
    acc[key].push(entry);
    return acc;
  }, {});

  const categories = Object.keys(grouped).sort();

  return (
    <div className="flex-1 overflow-y-auto p-8 max-w-2xl mx-auto w-full">
      {/* Header */}
      <div className="flex items-start justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-white">Memory</h1>
          <p className="text-gray-400 text-sm mt-1">What the AI remembers about you</p>
        </div>
        {entries.length > 0 && (
          <button
            onClick={handleClearAll}
            disabled={clearing}
            className="px-4 py-2 bg-red-600/20 text-red-400 hover:bg-red-600/30 disabled:opacity-50 disabled:cursor-not-allowed text-sm rounded-lg font-medium transition-colors"
          >
            {clearing ? "Clearing..." : "Clear All"}
          </button>
        )}
      </div>

      {/* Error banner */}
      {error && (
        <div className="mb-6 px-4 py-3 bg-red-600/20 border border-red-600/40 text-red-300 rounded-lg text-sm">
          {error}
        </div>
      )}

      {/* Loading state */}
      {loading && (
        <div className="bg-gray-900 border border-gray-800 rounded-lg p-8 flex items-center justify-center">
          <span className="text-gray-500 text-sm">Loading memories...</span>
        </div>
      )}

      {/* Empty state */}
      {!loading && entries.length === 0 && (
        <div className="bg-gray-900 border border-gray-800 rounded-lg p-8 text-center">
          <div className="text-3xl mb-3">🧠</div>
          <p className="text-gray-400 text-sm">
            No memories yet. The AI will remember things from your conversations here.
          </p>
        </div>
      )}

      {/* Grouped entries */}
      {!loading && categories.map((category) => (
        <section key={category} className="mb-6">
          <h2 className="text-xs font-semibold text-gray-500 uppercase tracking-wider mb-2 px-1">
            {category}
          </h2>
          <div className="bg-gray-900 border border-gray-800 rounded-lg overflow-hidden">
            {grouped[category].map((entry, idx) => (
              <div
                key={entry.id}
                className={`flex items-start gap-3 p-4 ${
                  idx < grouped[category].length - 1 ? "border-b border-gray-800" : ""
                }`}
              >
                <div className="flex-1 min-w-0">
                  <p className="text-gray-200 text-sm break-words leading-relaxed">
                    {entry.content}
                  </p>
                  <div className="flex items-center gap-2 mt-2">
                    <span className="bg-blue-600/30 text-blue-300 text-xs px-2 py-0.5 rounded-full">
                      {entry.category}
                    </span>
                    <span className="text-gray-600 text-xs">
                      {formatDate(entry.created_at)}
                    </span>
                  </div>
                </div>
                <button
                  onClick={() => handleDelete(entry.id)}
                  disabled={deleting === entry.id}
                  aria-label="Delete memory"
                  className="shrink-0 w-7 h-7 flex items-center justify-center text-gray-500 hover:text-red-400 hover:bg-red-600/10 rounded transition-colors disabled:opacity-50"
                >
                  {deleting === entry.id ? (
                    <span className="text-xs">...</span>
                  ) : (
                    <span className="text-base leading-none">×</span>
                  )}
                </button>
              </div>
            ))}
          </div>
        </section>
      ))}
    </div>
  );
}
