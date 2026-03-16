"use client";

import { Suspense, useEffect, useState } from "react";
import { createClient } from "@/lib/supabase";
import { getProfile, savePreferences, saveApiKey, getCredits, createCheckout, getGoogleStatus, disconnectGoogle, listMemories, deleteMemory, clearAllMemories } from "@/lib/api";
import { useRouter, useSearchParams } from "next/navigation";
import { API_BASE_URL } from "@/lib/config";

const PLANS = [
  { id: "starter", label: "Starter", price: "$5.99", credits: 500 },
  { id: "pro", label: "Pro", price: "$17.99", credits: 2000 },
  { id: "power", label: "Power", price: "$49.99", credits: 6000 },
  { id: "byok", label: "BYOK", price: "$9.99", credits: 0 },
] as const;

function SettingsContent() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const supabase = createClient();
  const [email, setEmail] = useState("");
  const [model, setModel] = useState("auto");
  const [apiKey, setApiKey] = useState("");
  const [githubKey, setGithubKey] = useState("");
  const [notionKey, setNotionKey] = useState("");
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);

  const [creditsBalance, setCreditsBalance] = useState<number | null>(null);
  const [subscriptionTier, setSubscriptionTier] = useState<string | null>(null);
  const [checkoutLoading, setCheckoutLoading] = useState<string | null>(null);
  const [checkoutError, setCheckoutError] = useState<string | null>(null);
  const [billingBanner, setBillingBanner] = useState<"success" | "cancel" | null>(null);

  const [googleConnected, setGoogleConnected] = useState<boolean | null>(null);
  const [googleDisconnecting, setGoogleDisconnecting] = useState(false);
  const [googleError, setGoogleError] = useState<string | null>(null);
  const [googleSuccessBanner, setGoogleSuccessBanner] = useState(false);

  const [memories, setMemories] = useState<Array<{ id: string; category: string; content: string; created_at: string }>>([]);
  const [memoriesLoading, setMemoriesLoading] = useState(false);
  const [memoryDeleting, setMemoryDeleting] = useState<string | null>(null);
  const [memoryError, setMemoryError] = useState<string | null>(null);
  const [confirmClearMemory, setConfirmClearMemory] = useState(false);

  async function refreshCredits() {
    try {
      const credits = await getCredits();
      setCreditsBalance(credits.credits_balance);
      setSubscriptionTier(credits.subscription_tier);
    } catch {
      // ignore
    }
  }

  useEffect(() => {
    (async () => {
      const {
        data: { user },
      } = await supabase.auth.getUser();
      if (user?.email) setEmail(user.email);

      try {
        const profile = await getProfile();
        if (profile.preferred_model) setModel(profile.preferred_model);
      } catch {
        // ignore — profile not loaded
      }

      await refreshCredits();

      try {
        const status = await getGoogleStatus();
        setGoogleConnected(status.connected);
      } catch {
        // ignore — google status not loaded
      }

      try {
        const entries = await listMemories();
        setMemories(entries);
      } catch {
        // ignore — memories not loaded
      }
    })();
  }, [supabase.auth]);

  // Handle ?billing=success / ?billing=cancel after Stripe redirect
  useEffect(() => {
    const billing = searchParams.get("billing");
    if (billing === "success" || billing === "cancel") {
      setBillingBanner(billing);
      // Clean the query param from the URL without a full navigation
      const url = new URL(window.location.href);
      url.searchParams.delete("billing");
      window.history.replaceState({}, "", url.toString());
      if (billing === "success") {
        refreshCredits();
      }
      const timer = setTimeout(() => setBillingBanner(null), 5000);
      return () => clearTimeout(timer);
    }
  }, [searchParams]);

  // Detect ?connected=google redirect from OAuth callback
  useEffect(() => {
    if (searchParams.get("connected") === "google") {
      setGoogleConnected(true);
      setGoogleSuccessBanner(true);
      // Clean the query param from the URL without a full navigation
      const url = new URL(window.location.href);
      url.searchParams.delete("connected");
      window.history.replaceState({}, "", url.toString());
      const timer = setTimeout(() => setGoogleSuccessBanner(false), 4000);
      return () => clearTimeout(timer);
    }
  }, [searchParams]);

  async function handleSignOut() {
    await supabase.auth.signOut();
    router.push("/login");
  }

  async function handleBuyCredits(plan: string) {
    setCheckoutLoading(plan);
    setCheckoutError(null);
    try {
      const { checkout_url } = await createCheckout(plan);
      window.location.href = checkout_url;
    } catch (err) {
      setCheckoutError(err instanceof Error ? err.message : "Checkout failed");
      setCheckoutLoading(null);
    }
  }

  async function loadMemories() {
    setMemoriesLoading(true);
    setMemoryError(null);
    try {
      const entries = await listMemories();
      setMemories(entries);
    } catch (err) {
      setMemoryError(err instanceof Error ? err.message : "Failed to load memories");
    } finally {
      setMemoriesLoading(false);
    }
  }

  async function handleDeleteMemory(id: string) {
    setMemoryDeleting(id);
    setMemoryError(null);
    try {
      await deleteMemory(id);
      setMemories((prev) => prev.filter((m) => m.id !== id));
    } catch (err) {
      setMemoryError(err instanceof Error ? err.message : "Failed to delete");
    } finally {
      setMemoryDeleting(null);
    }
  }

  async function handleClearAllMemories() {
    setConfirmClearMemory(false);
    setMemoriesLoading(true);
    setMemoryError(null);
    try {
      await clearAllMemories();
      setMemories([]);
    } catch (err) {
      setMemoryError(err instanceof Error ? err.message : "Failed to clear");
    } finally {
      setMemoriesLoading(false);
    }
  }

  async function handleGoogleDisconnect() {
    setGoogleDisconnecting(true);
    setGoogleError(null);
    try {
      await disconnectGoogle();
      setGoogleConnected(false);
    } catch (err) {
      setGoogleError(err instanceof Error ? err.message : "Failed to disconnect Google.");
    } finally {
      setGoogleDisconnecting(false);
    }
  }

  async function handleSave() {
    setSaving(true);
    setSaved(false);
    setSaveError(null);

    try {
      await savePreferences(model);

      const keyOps: Array<Promise<void>> = [];
      if (apiKey.trim()) keyOps.push(saveApiKey("anthropic", apiKey.trim()));
      if (githubKey.trim()) keyOps.push(saveApiKey("github", githubKey.trim()));
      if (notionKey.trim()) keyOps.push(saveApiKey("notion", notionKey.trim()));
      await Promise.all(keyOps);

      // Only clear key fields after confirmed successful save
      if (apiKey.trim()) setApiKey("");
      if (githubKey.trim()) setGithubKey("");
      if (notionKey.trim()) setNotionKey("");

      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    } catch (err) {
      setSaveError(err instanceof Error ? err.message : "Failed to save settings.");
    } finally {
      setSaving(false);
    }
  }

  const inputCls = "w-full px-3.5 py-2.5 text-sm text-white placeholder-gray-600 rounded-xl focus:outline-none focus:ring-1 focus:ring-indigo-500/50";
  const inputStyle = { background: "var(--color-surface-3)", border: "1px solid var(--color-border)" };
  const cardStyle = { background: "var(--color-surface-2)", border: "1px solid var(--color-border)" };

  return (
    <div className="flex-1 overflow-y-auto">
      <div className="max-w-2xl mx-auto px-6 py-8 w-full">

        {/* Banners */}
        {googleSuccessBanner && (
          <div className="mb-5 flex items-center gap-2.5 px-4 py-3 rounded-xl text-sm"
            style={{ background: "rgba(34,197,94,0.1)", border: "1px solid rgba(34,197,94,0.25)", color: "#86efac" }}>
            <svg className="w-4 h-4 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
            </svg>
            Google connected successfully!
          </div>
        )}
        {billingBanner === "success" && (
          <div className="mb-5 flex items-center gap-2.5 px-4 py-3 rounded-xl text-sm"
            style={{ background: "rgba(34,197,94,0.1)", border: "1px solid rgba(34,197,94,0.25)", color: "#86efac" }}>
            <svg className="w-4 h-4 flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
            </svg>
            Payment successful! Credits added to your account.
          </div>
        )}
        {billingBanner === "cancel" && (
          <div className="mb-5 flex items-center gap-2.5 px-4 py-3 rounded-xl text-sm"
            style={{ background: "rgba(245,158,11,0.1)", border: "1px solid rgba(245,158,11,0.25)", color: "#fcd34d" }}>
            Checkout cancelled.
          </div>
        )}

        <h1 className="text-2xl font-bold text-white mb-7">Settings</h1>

        {/* Profile */}
        <section className="mb-6">
          <h2 className="text-xs font-semibold uppercase tracking-widest mb-3" style={{ color: "var(--color-text-muted)" }}>Profile</h2>
          <div className="p-4 rounded-2xl" style={cardStyle}>
            <label className="block text-xs font-medium mb-1.5" style={{ color: "var(--color-text-secondary)" }}>Email</label>
            <div className="text-sm text-white">{email || "—"}</div>
          </div>
        </section>

        {/* Model preference */}
        <section className="mb-6">
          <h2 className="text-xs font-semibold uppercase tracking-widest mb-3" style={{ color: "var(--color-text-muted)" }}>Model Preference</h2>
          <div className="p-4 rounded-2xl" style={cardStyle}>
            <select
              value={model}
              onChange={(e) => setModel(e.target.value)}
              aria-label="Model preference"
              className="w-full text-sm text-white rounded-xl px-3.5 py-2.5 focus:outline-none focus:ring-1 focus:ring-indigo-500/50"
              style={inputStyle}
            >
              <option value="auto">Auto — route by complexity</option>
              <option value="haiku">Claude Haiku — fast & cheap</option>
              <option value="sonnet">Claude Sonnet — balanced</option>
              <option value="opus">Claude Opus — best quality</option>
            </select>
          </div>
        </section>

        {/* BYOK */}
        <section className="mb-6">
          <h2 className="text-xs font-semibold uppercase tracking-widest mb-3" style={{ color: "var(--color-text-muted)" }}>Bring Your Own Key</h2>
          <div className="p-4 rounded-2xl space-y-4" style={cardStyle}>
            {[
              { label: "Anthropic API Key", value: apiKey, onChange: setApiKey, placeholder: "sk-ant-...", hint: "Use your own Anthropic key to skip credit limits. Stored encrypted." },
              { label: "GitHub Personal Access Token", value: githubKey, onChange: setGithubKey, placeholder: "ghp_...", hint: "Allows the agent to read and write GitHub repositories on your behalf." },
              { label: "Notion Integration Token", value: notionKey, onChange: setNotionKey, placeholder: "secret_...", hint: "Allows the agent to read and update Notion pages and databases." },
            ].map(({ label, value, onChange, placeholder, hint }) => (
              <div key={label}>
                <label className="block text-xs font-medium mb-1.5" style={{ color: "var(--color-text-secondary)" }}>{label}</label>
                <input type="password" value={value} onChange={(e) => onChange(e.target.value)} placeholder={placeholder} className={inputCls} style={inputStyle} />
                <p className="text-[11px] mt-1.5" style={{ color: "var(--color-text-muted)" }}>{hint}</p>
              </div>
            ))}
          </div>
        </section>

        {/* Save */}
        <div className="flex items-center gap-3 mb-8">
          <button
            onClick={handleSave}
            disabled={saving}
            className="px-5 py-2.5 rounded-xl text-sm font-semibold text-white transition-all active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed"
            style={{ background: "var(--color-accent)" }}
          >
            {saving ? (
              <span className="flex items-center gap-2">
                <svg className="w-3.5 h-3.5 animate-spin" fill="none" viewBox="0 0 24 24">
                  <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                  <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                </svg>
                Saving…
              </span>
            ) : "Save Changes"}
          </button>
          {saved && <span className="text-sm flex items-center gap-1.5" style={{ color: "var(--color-success)" }}>
            <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20"><path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" /></svg>
            Saved
          </span>}
          {saveError && <span className="text-sm text-red-400">{saveError}</span>}
        </div>

        {/* Integrations */}
        <section className="mb-6">
          <h2 className="text-xs font-semibold uppercase tracking-widest mb-3" style={{ color: "var(--color-text-muted)" }}>Integrations</h2>
          <div className="p-4 rounded-2xl" style={cardStyle}>
            <div className="flex items-center justify-between gap-4">
              <div className="flex items-center gap-3">
                <div className="w-8 h-8 rounded-xl flex items-center justify-center text-base"
                  style={{ background: "var(--color-surface-3)" }}>
                  G
                </div>
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-sm font-medium text-white">Google</span>
                    <span className="text-xs px-1.5 py-0.5 rounded-md" style={{ background: "var(--color-surface-3)", color: "var(--color-text-muted)" }}>
                      Gmail + Calendar
                    </span>
                  </div>
                  <div className="flex items-center gap-1.5 mt-0.5">
                    <div className="w-1.5 h-1.5 rounded-full"
                      style={{ background: googleConnected ? "var(--color-success)" : "var(--color-text-muted)" }} />
                    <span className="text-xs" style={{ color: "var(--color-text-muted)" }}>
                      {googleConnected === true ? "Connected" : googleConnected === false ? "Not connected" : "Checking…"}
                    </span>
                  </div>
                </div>
              </div>
              <div className="shrink-0">
                {googleConnected === true ? (
                  <button
                    onClick={handleGoogleDisconnect}
                    disabled={googleDisconnecting}
                    className="px-3 py-1.5 rounded-xl text-xs font-medium transition-all disabled:opacity-50"
                    style={{ background: "rgba(239,68,68,0.1)", border: "1px solid rgba(239,68,68,0.2)", color: "#fca5a5" }}
                  >
                    {googleDisconnecting ? "Disconnecting…" : "Disconnect"}
                  </button>
                ) : (
                  <button
                    onClick={() => { window.location.href = `${API_BASE_URL}/api/v1/auth/google`; }}
                    className="px-3 py-1.5 rounded-xl text-xs font-semibold text-white transition-all"
                    style={{ background: "var(--color-accent)" }}
                  >
                    Connect
                  </button>
                )}
              </div>
            </div>
            {googleError && <p className="text-xs mt-3 text-red-400">{googleError}</p>}
          </div>
        </section>

        {/* Memory */}
        <section className="mb-6" data-testid="memory-section">
          <div className="flex items-center justify-between mb-3">
            <h2 className="text-xs font-semibold uppercase tracking-widest" style={{ color: "var(--color-text-muted)" }}>Memory</h2>
            {memories.length > 0 && (
              confirmClearMemory ? (
                <div className="flex items-center gap-2">
                  <span className="text-xs" style={{ color: "var(--color-text-secondary)" }}>Delete all?</span>
                  <button
                    onClick={handleClearAllMemories}
                    disabled={memoriesLoading}
                    className="px-2.5 py-1 rounded-lg text-xs font-semibold text-white transition-all disabled:opacity-50"
                    style={{ background: "var(--color-error)" }}
                  >
                    Yes
                  </button>
                  <button
                    onClick={() => setConfirmClearMemory(false)}
                    className="px-2.5 py-1 rounded-lg text-xs font-medium transition-all"
                    style={{ background: "var(--color-surface-3)", color: "var(--color-text-secondary)" }}
                  >
                    Cancel
                  </button>
                </div>
              ) : (
                <button
                  onClick={() => setConfirmClearMemory(true)}
                  disabled={memoriesLoading}
                  className="text-xs font-medium transition-colors disabled:opacity-50"
                  style={{ color: "#fca5a5" }}
                >
                  Clear all
                </button>
              )
            )}
          </div>
          <div className="p-4 rounded-2xl" style={cardStyle}>
            <p className="text-xs mb-3" style={{ color: "var(--color-text-secondary)" }}>
              {memoriesLoading ? "Loading…" : `${memories.length} fact${memories.length === 1 ? "" : "s"} stored`}
            </p>
            {memoryError && <p className="text-xs text-red-400 mb-3">{memoryError}</p>}
            {memories.length > 0 && (
              <div className="space-y-1.5">
                {memories.map((m) => (
                  <div key={m.id} className="flex items-start gap-3 px-3 py-2.5 rounded-xl"
                    style={{ background: "var(--color-surface-3)" }}>
                    <div className="flex-1 min-w-0">
                      <span className="text-[10px] uppercase tracking-wider font-medium" style={{ color: "var(--color-accent)" }}>
                        {m.category}
                      </span>
                      <p className="text-xs mt-0.5 break-words leading-relaxed" style={{ color: "var(--color-text-primary)" }}>{m.content}</p>
                    </div>
                    <button
                      onClick={() => handleDeleteMemory(m.id)}
                      disabled={memoryDeleting === m.id}
                      className="shrink-0 p-1 rounded-lg transition-colors disabled:opacity-50 hover:bg-white/5"
                      style={{ color: "var(--color-text-muted)" }}
                      aria-label="Delete memory"
                    >
                      <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    </button>
                  </div>
                ))}
              </div>
            )}
          </div>
        </section>

        {/* Billing */}
        <section className="mb-8">
          <h2 className="text-xs font-semibold uppercase tracking-widest mb-3" style={{ color: "var(--color-text-muted)" }}>Billing</h2>
          <div className="p-4 rounded-2xl" style={cardStyle}>
            {/* Balance header */}
            <div className="flex items-center justify-between mb-5 pb-4"
              style={{ borderBottom: "1px solid var(--color-border)" }}>
              <div>
                <div className="text-xs mb-1" style={{ color: "var(--color-text-muted)" }}>Credits balance</div>
                <div className="text-xl font-bold text-white">
                  {creditsBalance !== null ? creditsBalance.toLocaleString() : "—"}
                  {creditsBalance !== null && <span className="text-sm font-normal ml-1.5" style={{ color: "var(--color-text-muted)" }}>credits</span>}
                </div>
              </div>
              {subscriptionTier && (
                <span className="text-xs px-2.5 py-1 rounded-full font-semibold capitalize"
                  style={{ background: "var(--color-accent-subtle)", color: "#a5b4fc" }}>
                  {subscriptionTier}
                </span>
              )}
            </div>

            <div className="space-y-2">
              {PLANS.map((plan) => (
                <div key={plan.id} className="flex items-center justify-between py-2 px-3 rounded-xl transition-colors"
                  style={{ background: "var(--color-surface-3)" }}>
                  <div>
                    <span className="text-sm font-medium text-white">{plan.label}</span>
                    {plan.credits > 0 && (
                      <span className="text-xs ml-2" style={{ color: "var(--color-text-muted)" }}>
                        {plan.credits.toLocaleString()} credits
                      </span>
                    )}
                  </div>
                  <button
                    onClick={() => handleBuyCredits(plan.id)}
                    disabled={checkoutLoading === plan.id}
                    className="px-4 py-1.5 rounded-xl text-xs font-semibold text-white transition-all active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed min-w-[72px] text-center"
                    style={{ background: "var(--color-accent)" }}
                  >
                    {checkoutLoading === plan.id ? (
                      <span className="flex items-center justify-center gap-1">
                        <svg className="w-3 h-3 animate-spin" fill="none" viewBox="0 0 24 24">
                          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                        </svg>
                      </span>
                    ) : plan.price}
                  </button>
                </div>
              ))}
            </div>

            {checkoutError && <p className="text-xs text-red-400 mt-3">{checkoutError}</p>}
          </div>
        </section>

        {/* Sign out */}
        <section style={{ borderTop: "1px solid var(--color-border-subtle)", paddingTop: "1.5rem" }}>
          <button
            onClick={handleSignOut}
            className="flex items-center gap-2 px-5 py-2.5 rounded-xl text-sm font-medium transition-all active:scale-[0.98]"
            style={{ background: "rgba(239,68,68,0.08)", border: "1px solid rgba(239,68,68,0.15)", color: "#fca5a5" }}
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
            </svg>
            Sign Out
          </button>
        </section>
      </div>
    </div>
  );
}

export default function SettingsPage() {
  return (
    <Suspense fallback={<div className="p-8 text-gray-400">Loading…</div>}>
      <SettingsContent />
    </Suspense>
  );
}
