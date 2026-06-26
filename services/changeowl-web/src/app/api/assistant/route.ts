import logger from '@/lib/logger';
import { newSessionId } from '@/lib/utils';
import { NextRequest, NextResponse } from 'next/server';

export async function POST(req: NextRequest) {
  try {
    const body = await req.json();
    const gatewayUrl = process.env.ARTIFACT_GATEWAY_URL;

    const response = await fetch(`${gatewayUrl}/api/agents/chat/stream`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Session-ID': req.headers.get('X-Session-ID') || newSessionId(),
      },
      body: JSON.stringify(body),
    });

    return new NextResponse(response.body, {
      headers: {
        'Content-Type': 'text/event-stream',
        'Cache-Control': 'no-cache, no-transform',
        Connection: 'keep-alive',
      },
    });
  } catch (error) {
    logger.error({
      endpoint: 'POST /api/assistant',
      msg: 'Search assistant chat stream failed',
      error: error instanceof Error ? error.message : 'Unknown error',
      stack: error instanceof Error ? error.stack : undefined,
    });
    return new NextResponse('Internal Proxy Error', { status: 500 });
  }
}
