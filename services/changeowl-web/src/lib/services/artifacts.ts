import 'server-only';
import logger from '@/lib/logger';
import {
  BehavioralImpact,
  ChangeSurface,
  ChangeType,
  Confidence,
  RiskLevel,
} from '@/types/artifact-intelligence';
import { Artifact } from '@/types/artifact';

export interface ArtifactFilters {
  q?: string | null;
  limit?: number;
  offset?: number;

  changeType?: ChangeType | null;
  surface?: ChangeSurface | null;
  risk?: RiskLevel | null;
  confidence?: Confidence | null;
  impact?: BehavioralImpact | null;
  impactRadius?: string | null;

  relatedTo?: number | null;
  repoId?: number | null;
}

export interface ArtifactPageResponse {
  artifacts: Artifact[];
  limit: number;
  offset: number;
  total: number;
}

export async function getArtifacts(params: ArtifactFilters) {
  try {
    const baseUrl = process.env.ARTIFACT_GATEWAY_URL;

    if (!baseUrl) {
      throw new Error('ARTIFACT_GATEWAY_URL is not defined');
    }

    const query = new URLSearchParams();

    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        query.append(key, String(value));
      }
    });

    const response = await fetch(`${baseUrl}/api/artifacts?${query.toString()}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      cache: 'no-store',
    });

    if (!response.ok) {
      throw new Error(`Artifacts fetch failed: ${response.status} ${response.statusText}`);
    }

    const data = (await response.json()) as ArtifactPageResponse;
    return data.artifacts;
  } catch (e) {
    logger.error({
      endpoint: 'GET /ticker',
      msg: 'Ticker data fetch failed.',
      error: e instanceof Error ? e.message : 'Unknown error',
    });
    throw new Error(e instanceof Error ? e.message : 'Unknown error fetching ticker data');
  }
}

export async function getSimilarArtifacts(id: number) {
  try {
    const baseUrl = process.env.ARTIFACT_GATEWAY_URL;

    if (!baseUrl) {
      throw new Error('ARTIFACT_GATEWAY_URL is not defined');
    }

    const response = await fetch(`${baseUrl}/api/artifacts/${id}/similar}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      cache: 'no-store',
    });

    if (!response.ok) {
      throw new Error(`Artifacts fetch failed: ${response.status} ${response.statusText}`);
    }

    const data = (await response.json()) as ArtifactPageResponse;
    return data.artifacts;
  } catch (e) {
    logger.error({
      endpoint: 'GET /ticker',
      msg: 'Ticker data fetch failed.',
      error: e instanceof Error ? e.message : 'Unknown error',
    });
    throw new Error(e instanceof Error ? e.message : 'Unknown error fetching ticker data');
  }
}

export async function getArtifact(id: number) {
  try {
    const baseUrl = process.env.ARTIFACT_GATEWAY_URL;

    if (!baseUrl) {
      throw new Error('ARTIFACT_GATEWAY_URL is not defined');
    }

    const response = await fetch(`${baseUrl}/api/artifacts/${id}`, {
      method: 'GET',
      headers: {
        'Content-Type': 'application/json',
      },
      cache: 'no-store',
    });

    if (!response.ok) {
      throw new Error(`Artifact fetch failed: ${response.status} ${response.statusText}`);
    }

    const data = (await response.json()) as Artifact;
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
