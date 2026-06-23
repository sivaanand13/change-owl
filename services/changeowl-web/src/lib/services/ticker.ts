import logger from '../logger';

export type TickerData = {
  repositories: number;
  artifacts: number;
  insights: number;
};

export async function fetchTickerData(): Promise<TickerData> {
  try {
    const baseUrl = process.env.ARTIFACT_GATEWAY_URL;

    if (!baseUrl) {
      throw new Error('ARTIFACT_GATEWAY_URL is not defined');
    }

    const response = await fetch(`${baseUrl}/api/ticker`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      cache: 'no-store',
    });

    if (!response.ok) {
      throw new Error(`Ticker API failed: ${response.status} ${response.statusText}`);
    }

    const data = (await response.json()) as TickerData;

    return data;
  } catch (e) {
    console.error('fetchTickerData failed:', e);
    logger.error({
      endpoint: 'GET /ticker',
      msg: 'Ticker data fetch failed.',
      error: e instanceof Error ? e.message : 'Unknown error',
    });
    throw new Error(e instanceof Error ? e.message : 'Unknown error fetching ticker data');
  }
}
