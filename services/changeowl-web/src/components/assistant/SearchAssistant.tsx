'use client';

import { useState } from 'react';

import { AssistantLauncher } from './AssistantLauncher';
import { AssistantWindow } from './AssistantWindow';

export function SearchAssistant() {
  const [open, setOpen] = useState(false);

  return (
    <>
      <AssistantLauncher open={open} onToggle={() => setOpen((prev) => !prev)} />

      {open && <AssistantWindow onClose={() => setOpen(false)} />}
    </>
  );
}
