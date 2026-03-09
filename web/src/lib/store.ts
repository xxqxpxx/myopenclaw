import { create } from "zustand";
import type { Conversation, Message, SSEEvent } from "@/lib/api";
import {
  listConversations,
  createConversation,
  deleteConversation,
  listMessages,
  streamChat,
} from "@/lib/api";

interface ChatMessage {
  id: string;
  role: "user" | "assistant" | "tool";
  content: string;
  model?: string;
  isStreaming?: boolean;
  toolName?: string;
  toolOutput?: string;
}

interface ChatStore {
  // Conversations
  conversations: Conversation[];
  activeConversationId: string | null;
  loading: boolean;

  // Messages
  messages: ChatMessage[];
  isStreaming: boolean;
  error: string | null;

  // Credits
  creditsBalance: number;

  // Actions
  loadConversations: () => Promise<void>;
  selectConversation: (id: string) => Promise<void>;
  newConversation: () => Promise<void>;
  removeConversation: (id: string) => Promise<void>;
  sendMessage: (content: string) => Promise<void>;
}

export const useChatStore = create<ChatStore>((set, get) => ({
  conversations: [],
  activeConversationId: null,
  loading: false,
  messages: [],
  isStreaming: false,
  error: null,
  creditsBalance: 0,

  loadConversations: async () => {
    set({ loading: true });
    try {
      const conversations = await listConversations();
      set({ conversations, loading: false });
    } catch (e) {
      set({ error: (e as Error).message, loading: false });
    }
  },

  selectConversation: async (id: string) => {
    set({ activeConversationId: id, messages: [], loading: true });
    try {
      const msgs = await listMessages(id);
      set({
        messages: msgs.map((m) => ({
          id: m.id,
          role: m.role as "user" | "assistant" | "tool",
          content: m.content,
          model: m.model ?? undefined,
        })),
        loading: false,
      });
    } catch (e) {
      set({ error: (e as Error).message, loading: false });
    }
  },

  newConversation: async () => {
    try {
      const conv = await createConversation();
      set((state) => ({
        conversations: [conv, ...state.conversations],
        activeConversationId: conv.id,
        messages: [],
      }));
    } catch (e) {
      set({ error: (e as Error).message });
    }
  },

  removeConversation: async (id: string) => {
    try {
      await deleteConversation(id);
      set((state) => ({
        conversations: state.conversations.filter((c) => c.id !== id),
        activeConversationId:
          state.activeConversationId === id
            ? null
            : state.activeConversationId,
        messages: state.activeConversationId === id ? [] : state.messages,
      }));
    } catch (e) {
      set({ error: (e as Error).message });
    }
  },

  sendMessage: async (content: string) => {
    const { activeConversationId } = get();
    if (!activeConversationId || get().isStreaming) return;

    // Add user message
    const userMsg: ChatMessage = {
      id: `user-${Date.now()}`,
      role: "user",
      content,
    };

    // Add placeholder for assistant
    const assistantMsg: ChatMessage = {
      id: `assistant-${Date.now()}`,
      role: "assistant",
      content: "",
      isStreaming: true,
    };

    set((state) => ({
      messages: [...state.messages, userMsg, assistantMsg],
      isStreaming: true,
      error: null,
    }));

    try {
      await streamChat(activeConversationId, content, (event: SSEEvent) => {
        const state = get();

        switch (event.type) {
          case "token":
            set({
              messages: state.messages.map((m) =>
                m.id === assistantMsg.id
                  ? {
                      ...m,
                      content: m.content + (event.content || ""),
                      model: event.model,
                    }
                  : m
              ),
            });
            break;

          case "tool_start": {
            const toolMsg: ChatMessage = {
              id: `tool-${Date.now()}`,
              role: "tool",
              content: `Running ${event.tool}...`,
              toolName: event.tool,
            };
            set({ messages: [...state.messages, toolMsg] });
            break;
          }

          case "tool_result":
            set({
              messages: state.messages.map((m) =>
                m.role === "tool" && m.toolName === event.tool && !m.toolOutput
                  ? { ...m, content: event.output || "", toolOutput: event.output }
                  : m
              ),
            });
            break;

          case "error":
            set({ error: event.error || "Unknown error" });
            break;

          case "done":
            set({
              isStreaming: false,
              creditsBalance:
                state.creditsBalance - (event.credits_used || 0),
              messages: state.messages.map((m) =>
                m.id === assistantMsg.id
                  ? { ...m, isStreaming: false }
                  : m
              ),
            });
            break;
        }
      });
    } catch (e) {
      set({ isStreaming: false, error: (e as Error).message });
    }

    // Refresh conversation list to get updated title
    get().loadConversations();
  },
}));
