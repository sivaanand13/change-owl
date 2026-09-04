'use client';

import { useEffect, useRef, useState } from 'react';
import Image from 'next/image';
import { MessageRenderer } from '../ui/MessageRenderer';
import { useAssistantStore } from '@/store/useAssistantStore';

type Props = {
  onClose: () => void;
};

export function AssistantWindow({ onClose }: Props) {
  const { messages, loading, sendMessage } = useAssistantStore();

  const [input, setInput] = useState('');

  const scrollRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    if (!scrollRef.current) {
      return;
    }

    scrollRef.current.scrollTo({
      top: scrollRef.current.scrollHeight,
      behavior: 'smooth',
    });
  }, [messages]);

  async function handleSend(e: React.FormEvent) {
    e.preventDefault();

    const question = input.trim();

    if (!question || loading) {
      return;
    }

    setInput('');

    await sendMessage(question);
  }

  return (
    <div
      className="
        fixed
        bottom-24
        right-6
        z-50
        h-175
        w-105
        rounded-xl
        border
        border-border-subtle
        bg-background
        text-foreground
        shadow-xl
        flex
        flex-col
        overflow-hidden
      "
    >
      <div className="flex items-center justify-between p-4 border-b border-border-subtle bg-brand-surface">
        <div className="flex items-center gap-2.5">
          <div className="relative w-6 h-6 rounded-full overflow-hidden border border-border-subtle bg-background">
            <Image src="/change-owl.png" alt="ChangeOwl Logo" fill priority />
          </div>

          <span className="font-semibold text-info tracking-tight">ChangeOwl Assistant</span>
        </div>

        <button
          onClick={onClose}
          className="
            text-text-muted
            hover:text-foreground
            transition-colors
            p-1
            rounded
            hover:bg-surface-interactive
          "
        >
          ✕
        </button>
      </div>

      <div
        ref={scrollRef}
        className="
          flex-1
          overflow-y-auto
          p-4
          space-y-4
          no-scrollbar
          bg-background
        "
      >
        {messages.map((msg) => (
          <div
            key={msg.id}
            className={`flex items-start gap-2.5 ${
              msg.sender === 'user' ? 'justify-end' : 'justify-start'
            }`}
          >
            {msg.sender === 'assistant' && (
              <div className="relative w-7 h-7 rounded-full overflow-hidden bg-brand-surface shrink-0 border border-border-subtle mt-0.5">
                <Image
                  src="/search-assistant.png"
                  alt="Assistant Avatar"
                  fill
                  sizes="28px"
                  className="object-contain"
                />
              </div>
            )}

            <div
              className={`max-w-[82%] rounded-[0.75rem] px-3.5 py-2 text-card-body ${
                msg.sender === 'user'
                  ? 'bg-brand-primary text-white rounded-tr-none'
                  : 'bg-brand-surface border border-border-subtle rounded-tl-none'
              }`}
            >
              {msg.statusMessage ? (
                <div className="flex items-center gap-2 text-xs text-text-muted italic animate-pulse">
                  <span className="w-1.5 h-1.5 rounded-full bg-brand-accent" />
                  <span>{msg.statusMessage}</span>
                </div>
              ) : (
                <MessageRenderer className="max-w-full overflow-hidden" content={msg.content} />
              )}
            </div>
          </div>
        ))}
      </div>

      <form
        onSubmit={handleSend}
        className="
          p-3
          border-t
          border-border-subtle
          bg-brand-surface
          flex
          gap-2
        "
      >
        <input
          value={input}
          onChange={(e) => setInput(e.target.value)}
          placeholder="Ask ChangeOwl..."
          disabled={loading}
          className="
            flex-1
            px-3
            py-2
            border
            rounded-lg
            bg-background
          "
        />

        <button
          type="submit"
          disabled={loading || !input.trim()}
          className="
            p-2
            rounded-lg
            bg-brand-primary
            text-white
          "
        >
          Send
        </button>
      </form>
    </div>
  );
}
