import { create } from 'zustand';
import logger from '@/lib/logger';
import { fetchEventSource } from '@microsoft/fetch-event-source';
import { AgentEvent, Message } from '@/types/assistant';
import { newMessageId, newSessionId } from '@/lib/utils';

const ASSISTANT_ENDPOINT = '/api/assistant';

interface ChatState {
  messages: Message[];
  loading: boolean;
  sessionId: string;
  sendMessage: (question: string) => Promise<void>;
  clearChat: () => void;
}

export const useAssistantStore = create<ChatState>((set, get) => {
  function handleAgentEvent(messageId: string, event: AgentEvent) {
    set((state) => ({
      messages: state.messages.map((msg) => {
        if (msg.id !== messageId) return msg;

        switch (event.type) {
          case 'start':
          case 'tool_start':
            return {
              ...msg,
              statusMessage: event.message,
            };

          case 'tool_end':
            return {
              ...msg,
              statusMessage: event.message,
            };

          case 'token':
            return {
              ...msg,
              content: msg.content + (event.content ?? ''),
              statusMessage: undefined,
            };

          case 'complete':
            return {
              ...msg,
              isStreaming: false,
              statusMessage: undefined,
            };

          case 'error':
            return {
              ...msg,
              content: event.message ?? 'An unexpected error occurred.',
              statusMessage: undefined,
              isStreaming: false,
            };

          default:
            return msg;
        }
      }),
    }));
  }

  function finalizeMessage(messageId: string) {
    set((state) => ({
      messages: state.messages.map((msg) =>
        msg.id === messageId
          ? {
              ...msg,
              isStreaming: false,
            }
          : msg
      ),
    }));
  }

  function failMessage(messageId: string, error: unknown) {
    logger.error(`Assistant stream failure: ${error}`);

    set((state) => ({
      messages: state.messages.map((msg) =>
        msg.id === messageId
          ? {
              ...msg,
              content: 'Unable to reach the assistant service.',
              statusMessage: undefined,
              isStreaming: false,
            }
          : msg
      ),
    }));
  }

  return {
    messages: [
      {
        id: newMessageId(),
        sender: 'assistant',
        content: 'Hello! I am your ChangeOwl engineering intelligence assistant.',
      },
    ],

    loading: false,

    sessionId: newSessionId(),

    clearChat: () =>
      set({
        sessionId: newSessionId(),
        messages: [
          {
            id: newMessageId(),
            sender: 'assistant',
            content: 'Chat history cleared.',
          },
        ],
      }),

    sendMessage: async (question: string) => {
      const { loading, sessionId } = get();

      if (!question.trim() || loading) {
        return;
      }

      set({ loading: true });

      const assistantMessageId = newMessageId();

      const userMessage: Message = {
        id: newMessageId(),
        sender: 'user',
        content: question,
      };

      const assistantMessage: Message = {
        id: assistantMessageId,
        sender: 'assistant',
        content: '',
        statusMessage: 'Formulating strategy...',
        isStreaming: true,
      };

      set((state) => ({
        messages: [...state.messages, userMessage, assistantMessage],
      }));

      try {
        await fetchEventSource(ASSISTANT_ENDPOINT, {
          method: 'POST',

          headers: {
            'Content-Type': 'application/json',
            'X-Session-ID': sessionId,
          },

          body: JSON.stringify({
            question,
          }),

          onmessage(event) {
            logger.info(`New event: ${event.data}`);
            try {
              const parsed = JSON.parse(event.data) as AgentEvent;
              handleAgentEvent(assistantMessageId, parsed);
            } catch (error) {
              logger.error(`Failed to parse SSE event data: ${error}`);
            }
          },

          onclose() {
            finalizeMessage(assistantMessageId);
          },

          onerror(error) {
            failMessage(assistantMessageId, error);
            throw error;
          },
        });
      } catch {
        // handled in onerror
      } finally {
        set({ loading: false });
      }
    },
  };
});
