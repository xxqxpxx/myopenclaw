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
    if (!confirm("Delete all memories? This cannot be undone.")) return;
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

      // Clear key fields after successful save — avoid re-sending on next save
      setApiKey("");
      setGithubKey("");
      setNotionKey("");

      setSaved(true);
      setTimeout(() => setSaved(false), 3000);
    } catch (err) {
      setSaveError(err instanceof Error ? err.message : "Failed to save settings.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <div className="flex-1 overflow-y-auto p-8 max-w-2xl mx-auto w-full">
      {googleSuccessBanner && (
        <div className="mb-6 flex items-center gap-2 px-4 py-3 bg-green-600/20 border border-green-600/40 text-green-400 rounded-lg text-sm">
          <span className="w-2 h-2 rounded-full bg-green-400 shrink-0" />
          Google connected successfully!
        </div>
      )}

      {/* Billing success/cancel banners */}
      {billingBanner === "success" && (
        <div className="mb-6 px-4 py-3 rounded-lg bg-green-600/20 border border-green-600/40 text-green-300 text-sm">
          Payment successful! Credits added to your account.
        </div>
      )}
      {billingBanner === "cancel" && (
        <div className="mb-6 px-4 py-3 rounded-lg bg-yellow-600/20 border border-yellow-600/40 text-yellow-300 text-sm">
          Checkout cancelled.
        </div>
      )}

      <h1 className="text-2xl font-bold mb-6">Settings</h1>

      {/* Profile */}
      <section className="mb-8">
        <h2 className="text-lg font-medium mb-3 text-gray-200">Profile</h2>
        <div className="bg-gray-900 border border-gray-800 rounded-lg p-4 space-y-3">
          <div>
            <label className="block text-sm text-gray-400 mb-1">Email</label>
            <div className="text-white">{email || "—"}</div>
          </div>
        </div>
      </section>

      {/* Model preference */}
      <section className="mb-8">
        <h2 className="text-lg font-medium mb-3 text-gray-200">
          Model Preference
        </h2>
        <div className="bg-gray-900 border border-gray-800 rounded-lg p-4">
          <select
            value={model}
            onChange={(e) => setModel(e.target.value)}
            aria-label="Model preference"
            className="w-full bg-gray-800 text-white border border-gray-700 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            <option value="auto">Auto (route by complexity)</option>
            <option value="haiku">Claude Haiku (fast, cheap)</option>
            <option value="sonnet">Claude Sonnet (balanced)</option>
            <option value="opus">Claude Opus (best quality)</option>
          </select>
        </div>
      </section>

      {/* BYOK */}
      <section className="mb-8">
        <h2 className="text-lg font-medium mb-3 text-gray-200">
          Bring Your Own Key (BYOK)
        </h2>
        <div className="bg-gray-900 border border-gray-800 rounded-lg p-4 space-y-4">
          <div>
            <label className="block text-sm text-gray-400 mb-1">
              Anthropic API Key
            </label>
            <input
              type="password"
              value={apiKey}
              onChange={(e) => setApiKey(e.target.value)}
              placeholder="sk-ant-..."
              className="w-full bg-gray-800 text-white border border-gray-700 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder-gray-600"
            />
            <p className="text-xs text-gray-500 mt-1">
              Use your own Anthropic key to skip credit limits. Stored encrypted.
            </p>
          </div>

          <div>
            <label className="block text-sm text-gray-400 mb-1">
              GitHub Personal Access Token
            </label>
            <input
              type="password"
              value={githubKey}
              onChange={(e) => setGithubKey(e.target.value)}
              placeholder="ghp_..."
              className="w-full bg-gray-800 text-white border border-gray-700 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder-gray-600"
            />
            <p className="text-xs text-gray-500 mt-1">
              Allows the agent to read and write GitHub repositories on your behalf.
            </p>
          </div>

          <div>
            <label className="block text-sm text-gray-400 mb-1">
              Notion Integration Token
            </label>
            <input
              type="password"
              value={notionKey}
              onChange={(e) => setNotionKey(e.target.value)}
              placeholder="secret_..."
              className="w-full bg-gray-800 text-white border border-gray-700 rounded-lg px-3 py-2 focus:outline-none focus:ring-2 focus:ring-blue-500 placeholder-gray-600"
            />
            <p className="text-xs text-gray-500 mt-1">
              Allows the agent to read and update Notion pages and databases.
            </p>
          </div>
        </div>
      </section>

      {/* Save */}
      <div className="flex items-center gap-4 mb-8">
        <button
          onClick={handleSave}
          disabled={saving}
          className="px-6 py-2 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed text-white rounded-lg font-medium transition-colors"
        >
          {saving ? "Saving..." : "Save Changes"}
        </button>
        {saved && <span className="text-green-400 text-sm">Saved!</span>}
        {saveError && <span className="text-red-400 text-sm">{saveError}</span>}
      </div>

      {/* Integrations */}
      <section className="mb-8">
        <h2 className="text-lg font-medium mb-3 text-gray-200">Integrations</h2>
        <div className="bg-gray-900 border border-gray-800 rounded-lg p-4">
          <div className="flex items-center justify-between gap-4">
            <div>
              <div className="flex items-center gap-2 mb-0.5">
                {googleConnected === true ? (
                  <span className="w-2 h-2 rounded-full bg-green-400 shrink-0" />
                ) : (
                  <span className="w-2 h-2 rounded-full bg-gray-500 shrink-0" />
                )}
                <span className="text-white font-medium">Google (Gmail + Calendar)</span>
              </div>
              <p className="text-xs text-gray-500 ml-4">
                {googleConnected === true
                  ? "Connected"
                  : googleConnected === false
                  ? "Not connected"
                  : "Checking..."}
              </p>
              <p className="text-xs text-gray-600 mt-1 ml-4">
                Allows the AI to read/send Gmail and manage your Calendar
              </p>
            </div>

            <div className="shrink-0">
              {googleConnected === true ? (
                <button
                  onClick={handleGoogleDisconnect}
                  disabled={googleDisconnecting}
                  className="px-4 py-1.5 bg-red-600/20 text-red-400 hover:bg-red-600/30 disabled:opacity-50 disabled:cursor-not-allowed text-sm rounded-lg font-medium transition-colors"
                >
                  {googleDisconnecting ? "Disconnecting..." : "Disconnect"}
                </button>
              ) : (
                <button
                  onClick={() => { window.location.href = `${API_BASE_URL}/api/v1/auth/google`; }}
                  className="px-4 py-1.5 bg-blue-600 hover:bg-blue-700 text-white text-sm rounded-lg font-medium transition-colors"
                >
                  Connect Google
                </button>
              )}
            </div>
          </div>

          {googleError && (
            <p className="text-red-400 text-sm mt-3">{googleError}</p>
          )}
        </div>
      </section>

      {/* Memory */}
      <section className="mb-8" data-testid="memory-section">
        <h2 className="text-lg font-medium mb-3 text-gray-200">Memory</h2>
        <div className="bg-gray-900 border border-gray-800 rounded-lg p-4">
          <p className="text-sm text-gray-400 mb-3">
            {memoriesLoading
              ? "Loading..."
              : `You have ${memories.length} fact${memories.length === 1 ? "" : "s"} stored`}
          </p>
          {memoryError && (
            <p className="text-red-400 text-sm mb-3">{memoryError}</p>
          )}
          {memories.length > 0 && (
            <div className="space-y-2 mb-4">
              {memories.map((m) => (
                <div
                  key={m.id}
                  className="flex items-start gap-3 py-2 px-3 bg-gray-800/50 rounded-lg"
                >
                  <div className="flex-1 min-w-0">
                    <span className="text-xs text-gray-500 uppercase">{m.category}</span>
                    <p className="text-gray-200 text-sm mt-0.5 break-words">{m.content}</p>
                  </div>
                  <button
                    onClick={() => handleDeleteMemory(m.id)}
                    disabled={memoryDeleting === m.id}
                    className="shrink-0 px-2 py-1 text-xs bg-red-600/20 text-red-400 hover:bg-red-600/30 rounded disabled:opacity-50"
                  >
                    {memoryDeleting === m.id ? "…" : "Delete"}
                  </button>
                </div>
              ))}
            </div>
          )}
          {memories.length > 0 && (
            <button
              onClick={handleClearAllMemories}
              disabled={memoriesLoading}
              className="text-sm text-red-400 hover:text-red-300 disabled:opacity-50"
            >
              Clear all memory
            </button>
          )}
        </div>
      </section>

      {/* Billing */}
      <section className="mb-8">
        <h2 className="text-lg font-medium mb-3 text-gray-200">Billing</h2>
        <div className="bg-gray-900 border border-gray-800 rounded-lg p-4">
          {/* Current balance */}
          <div className="flex items-center gap-3 mb-4 pb-4 border-b border-gray-800">
            <div>
              <div className="text-sm text-gray-400 mb-0.5">Credits balance</div>
              <div className="text-white font-medium">
                {creditsBalance !== null
                  ? `${creditsBalance.toLocaleString()} credits`
                  : "—"}
              </div>
            </div>
            {subscriptionTier && (
              <span className="ml-auto text-xs px-2 py-0.5 rounded-full bg-blue-600/30 text-blue-300 font-medium capitalize">
                {subscriptionTier}
              </span>
            )}
          </div>

          {/* Plan options */}
          <div className="space-y-2">
            {PLANS.map((plan) => (
              <div
                key={plan.id}
                className="flex items-center justify-between py-2"
              >
                <div>
                  <span className="text-white font-medium">{plan.label}</span>
                  {plan.credits > 0 && (
                    <span className="text-gray-400 text-sm ml-2">
                      {plan.credits.toLocaleString()} credits
                    </span>
                  )}
                </div>
                <button
                  onClick={() => handleBuyCredits(plan.id)}
                  disabled={checkoutLoading !== null}
                  className="px-4 py-1.5 bg-blue-600 hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed text-white text-sm rounded-lg font-medium transition-colors min-w-[90px] text-center"
                >
                  {checkoutLoading === plan.id ? "Loading..." : plan.price}
                </button>
              </div>
            ))}
          </div>

          {checkoutError && (
            <p className="text-red-400 text-sm mt-3">{checkoutError}</p>
          )}
        </div>
      </section>

      {/* Sign out */}
      <section className="border-t border-gray-800 pt-6">
        <button
          onClick={handleSignOut}
          className="px-6 py-2 bg-red-600/20 text-red-400 hover:bg-red-600/30 rounded-lg font-medium transition-colors"
        >
          Sign Out
        </button>
      </section>
    </div>
  );
}
