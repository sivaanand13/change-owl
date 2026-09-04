import { type ClassValue, clsx } from 'clsx';
import { twMerge } from 'tailwind-merge';

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs));
}

export type SessionId = `sess-${string}`;
export type MessageId = `msg-${string}`;

export function newSessionId(): SessionId {
  return `sess-${crypto.randomUUID()}`;
}

export function newMessageId(): MessageId {
  return `msg-${crypto.randomUUID()}`;
}
