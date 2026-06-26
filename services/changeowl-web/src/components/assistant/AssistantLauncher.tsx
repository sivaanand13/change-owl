'use client';

import Image from 'next/image';

type Props = {
  open: boolean;
  onToggle: () => void;
};

export function AssistantLauncher({ open, onToggle }: Props) {
  return (
    <button onClick={onToggle} className="fixed bottom-5 right-5 rounded-full glow-border">
      {<Image src="/search-assistant.png" alt="ChangeOwl Logo" width={50} height={50} priority />}
    </button>
  );
}
