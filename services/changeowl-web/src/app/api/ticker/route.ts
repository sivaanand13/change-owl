import { db } from "@/lib/db";
import {
  trackedRepositories,
  artifacts,
  artifactIntelligence,
} from "@/lib/db/schema";
import { count } from "drizzle-orm";
import logger from "@/lib/logger";

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

const fetchTickerData = async () => {
  try {
    const [repoCount, artifactCount, insightCount] = await Promise.all([
      db.select({ value: count() }).from(trackedRepositories),
      db.select({ value: count() }).from(artifacts),
      db.select({ value: count() }).from(artifactIntelligence),
    ]);

    return {
      repositories: repoCount[0].value,
      artifacts: artifactCount[0].value,
      insights: insightCount[0].value,
    };
  } catch (error) {
    logger.error({
      endpoint: "GET /ticker",
      msg: "Ticker data fetch failed.",
      error: error instanceof Error ? error.message : "Unknown error",
    });
  }
};

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
          const initialPayload = encoder.encode(
            `data: ${JSON.stringify(data)}\n\n`,
          );
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
      "Content-Type": "text/event-stream",
      "Cache-Control": "no-cache, no-transform",
      Connection: "keep-alive",
    },
  });
}
