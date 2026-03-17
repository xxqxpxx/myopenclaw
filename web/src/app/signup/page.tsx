"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";
import { createClient } from "@/lib/supabase";
import { provisionUser } from "@/lib/api";

export default function SignupPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const router = useRouter();

  async function handleSignup(e: React.FormEvent) {
    e.preventDefault();
    setLoading(true);
    setError("");

    const supabase = createClient();
    const { error } = await supabase.auth.signUp({ email, password });

    if (error) {
      setError(error.message);
      setLoading(false);
    } else {
      // Provision user: create profile, first conversation, and warm sandbox
      try {
        const result = await provisionUser();
        router.push(`/chat/${result.conversation_id}`);
      } catch {
        // Provisioning failed but signup succeeded — go to dashboard
        router.push("/");
      }
    }
  }

  return (
    <div className="min-h-screen flex items-center justify-center px-4" style={{ background: "var(--color-surface-0)" }}>
      <div
        className="fixed inset-0 pointer-events-none"
        style={{
          background: "radial-gradient(ellipse 60% 40% at 50% 0%, rgba(99,102,241,0.12), transparent)",
        }}
      />

      <div className="w-full max-w-md relative">
        <div className="text-center mb-8">
          <div
            className="w-12 h-12 rounded-2xl flex items-center justify-center text-lg font-bold text-white mx-auto mb-4"
            style={{
              background: "linear-gradient(135deg, #6366f1, #8b5cf6)",
              boxShadow: "0 0 30px rgba(99,102,241,0.4)",
            }}
          >
            C
          </div>
          <h1 className="text-2xl font-bold text-white">Create your account</h1>
          <p className="text-sm mt-1" style={{ color: "var(--color-text-secondary)" }}>
            Start with 50 free credits — no card required
          </p>
        </div>

        <div
          className="p-8 rounded-2xl"
          style={{
            background: "var(--color-surface-2)",
            border: "1px solid var(--color-border)",
            boxShadow: "0 25px 50px rgba(0,0,0,0.4)",
          }}
        >
          <form onSubmit={handleSignup} className="space-y-4">
            <div>
              <label className="block text-sm font-medium mb-1.5" style={{ color: "var(--color-text-secondary)" }}>
                Email
              </label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full px-3.5 py-2.5 text-sm text-white placeholder-gray-600 rounded-xl focus:outline-none focus:ring-2"
                style={{
                  background: "var(--color-surface-3)",
                  border: "1px solid var(--color-border)",
                  "--tw-ring-color": "var(--color-accent)",
                } as React.CSSProperties}
                placeholder="you@example.com"
                required
              />
            </div>

            <div>
              <label className="block text-sm font-medium mb-1.5" style={{ color: "var(--color-text-secondary)" }}>
                Password
              </label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full px-3.5 py-2.5 text-sm text-white placeholder-gray-600 rounded-xl focus:outline-none focus:ring-2"
                style={{
                  background: "var(--color-surface-3)",
                  border: "1px solid var(--color-border)",
                  "--tw-ring-color": "var(--color-accent)",
                } as React.CSSProperties}
                placeholder="Min. 6 characters"
                minLength={6}
                required
              />
            </div>

            {error && (
              <div
                className="flex items-center gap-2 px-3.5 py-2.5 rounded-xl text-sm"
                style={{ background: "rgba(239,68,68,0.1)", border: "1px solid rgba(239,68,68,0.2)", color: "#fca5a5" }}
              >
                <svg className="w-4 h-4 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
                </svg>
                {error}
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full py-2.5 rounded-xl text-sm font-semibold text-white transition-all active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed mt-2"
              style={{ background: "linear-gradient(135deg, #6366f1, #8b5cf6)" }}
            >
              {loading ? (
                <span className="flex items-center justify-center gap-2">
                  <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                  </svg>
                  Creating account…
                </span>
              ) : "Create Account"}
            </button>
          </form>
        </div>

        <p className="text-center text-sm mt-6" style={{ color: "var(--color-text-muted)" }}>
          Already have an account?{" "}
          <a href="/login" className="font-medium hover:underline" style={{ color: "#a5b4fc" }}>
            Sign in
          </a>
        </p>
      </div>
    </div>
  );
}
