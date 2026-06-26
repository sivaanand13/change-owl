export type AgentEventType =
  | 'start'
  | 'status'
  | 'tool_start'
  | 'tool_end'
  | 'token'
  | 'source'
  | 'complete'
  | 'error';

export interface AgentEvent {
  type: AgentEventType;

  message?: string;

  content?: string;

  tool_name?: string;

  data?: Record<string, unknown>;
}

export interface Message {
  id: string;

  sender: 'user' | 'assistant';

  content: string;

  statusMessage?: string;

  isStreaming?: boolean;
}
