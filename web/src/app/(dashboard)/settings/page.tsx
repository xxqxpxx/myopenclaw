"use client";

import { useEffect, useState } from "react";
import { createClient } from "@/lib/supabase";
import { getProfile } from "@/lib/api";
import { useRouter } from "next/navigation";

export default function SettingsPage() {
  const router = useRouter();
  const supabase = createClient();
  const [email, setEmail] = useState("");
  const [model, setModel] = useState("auto");
  const [apiKey, setApiKey] = useState("");
  const [saved, setSaved] = useState(false);

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
    })();
  }, [supabase.auth]);

  async function handleSignOut() {
    await supabase.auth.signOut();
    router.push("/login");
  }

  function handleSave() {
    // TODO: call backend to save preferences
    setSaved(true);
    setTimeout(() => setSaved(false), 2000);
  }

  return (
    <div className="flex-1 overflow-y-auto p-8 max-w-2xl mx-auto w-full">
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
        <div className="bg-gray-900 border border-gray-800 rounded-lg p-4">
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
            Use your own API key to skip credit limits. Stored encrypted.
          </p>
        </div>
      </section>

      {/* Save */}
      <div className="flex items-center gap-4 mb-8">
        <button
          onClick={handleSave}
          className="px-6 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg font-medium transition-colors"
        >
          Save Changes
        </button>
        {saved && <span className="text-green-400 text-sm">Saved!</span>}
      </div>

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
