import { fetchTickerData } from '@/lib/services/ticker';

const clients: Set<ReadableStreamDefaultController> = new Set();
const encoder = new TextEncoder();

type TickerData = {
  repositories: number;
  artifacts: number;
  insights: number;
};

async function broadcast(data: TickerData | undefined) {
  if (!data || clients.size === 0) return;

  const payload = encoder.encode(`data: ${JSON.stringify(data)}\n\n`);

  clients.forEach((client) => {
    try {
      client.enqueue(payload);
    } catch {
      clients.delete(client);
    }
  });
}

setInterval(async () => {
  const data = await fetchTickerData();
  await broadcast(data);
}, 5000);

export async function GET() {
  let controllerRef: ReadableStreamDefaultController | null = null;

  const stream = new ReadableStream({
    start(controller) {
      controllerRef = controller;
      clients.add(controller);

      fetchTickerData().then((data) => {
        if (data) {
          const initialPayload = encoder.encode(`data: ${JSON.stringify(data)}\n\n`);
          controller.enqueue(initialPayload);
        }
      });
    },
    cancel() {
      if (controllerRef) {
        clients.delete(controllerRef);
      }
    },
  });

  return new Response(stream, {
    headers: {
      'Content-Type': 'text/event-stream',
      'Cache-Control': 'no-cache, no-transform',
      Connection: 'keep-alive',
    },
  });
}
