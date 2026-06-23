import { NextRequest, NextResponse } from 'next/server';
import { getArtifact } from '@/lib/services/artifacts';
import logger from '@/lib/logger';

export async function GET(req: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  const { id } = await params;

  const numberId = parseInt(id);
  if (isNaN(numberId)) {
    return NextResponse.json({ error: 'Invalid artifact ID' }, { status: 400 });
  }

  try {
    const result = await getArtifact(numberId);

    return NextResponse.json(result);
  } catch (error) {
    logger.error({
      endpoint: 'GET /artifacts/:id',
      msg: 'Artifacts retrieval failed.',
      error: error instanceof Error ? error.message : 'Unknown error',
      stack: error instanceof Error ? error.stack : undefined,
      context: { id },
    });
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
