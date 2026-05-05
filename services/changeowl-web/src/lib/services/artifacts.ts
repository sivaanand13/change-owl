import "server-only";

import { db } from "@/lib/db";
import { eq, desc, and, sql, ne, SQL } from "drizzle-orm";
import logger from "@/lib/logger";
import {
  artifacts,
  artifactIntelligence,
  technologies,
  trackedRepositories,
} from "@/lib/db/schema";
import {
  BehavioralImpact,
  ChangeSurface,
  ChangeType,
  Confidence,
  RiskLevel,
} from "@/types/artifact-intelligence";

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

export async function getArtifacts(params: ArtifactFilters) {
  const {
    q,
    limit: reqLimit,
    offset = 0,
    changeType,
    surface,
    risk,
    confidence,
    impact,
    impactRadius,
    relatedTo,
    repoId,
  } = params;
  const limit = Math.min(reqLimit ?? 10, 50);

  try {
    let similaritySQL: SQL | undefined;
    let refEmbedding: number[] | null = null;
    if (relatedTo) {
      const source = await db
        .select({ embedding: artifactIntelligence.embedding })
        .from(artifactIntelligence)
        .where(eq(artifactIntelligence.artifactId, relatedTo))
        .limit(1);
      if (source.length > 0 && source[0].embedding) {
        refEmbedding =
          typeof source[0].embedding === "string"
            ? JSON.parse(source[0].embedding)
            : source[0].embedding;
        similaritySQL = sql`${artifactIntelligence.embedding} <=> ${JSON.stringify(refEmbedding)}::vector`;
      }
    } else if (q) {
      const url = `${process.env.SEMANTIC_SERVICE_API_URL}/embed`;
      const payload = JSON.stringify({
        query: q,
      });
      const embedRes = await fetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "X-API-Key": process.env.SEMANTIC_SERVICE_API_KEY as string,
        },
        body: payload,
      });

      if (!embedRes.ok) throw new Error("Embedding service unreachable");

      const { embedding } = await embedRes.json();
      refEmbedding = embedding;
      similaritySQL = sql`${artifactIntelligence.embedding} <=> ${JSON.stringify(embedding)}::vector`;
    }

    let query = db
      .select({
        id: artifacts.id,
        title: artifacts.title,
        body: artifacts.body,
        url: artifacts.url,
        author: artifacts.author,
        createdAt: artifacts.sourceCreatedAt,
        state: artifacts.state,

        repoName: trackedRepositories.name,
        repoId: artifacts.repoId,
        techName: technologies.name,
        techId: artifacts.techId,
        intelligence: artifactIntelligence.aiSummary,
        similarityScore: refEmbedding
          ? sql<number>`1 - (${artifactIntelligence.embedding} <=> ${JSON.stringify(refEmbedding)}::vector)`
          : sql<number>`NULL`,
      })
      .from(artifacts)
      .innerJoin(
        artifactIntelligence,
        eq(artifacts.id, artifactIntelligence.artifactId),
      )
      .innerJoin(
        trackedRepositories,
        eq(artifacts.repoId, trackedRepositories.id),
      )
      .innerJoin(technologies, eq(artifacts.techId, technologies.id))
      .$dynamic();

    const filters = [];
    if (repoId) filters.push(eq(artifacts.repoId, repoId));
    if (changeType)
      filters.push(
        sql`${artifactIntelligence.aiSummary}->>'change_type' = ${changeType}`,
      );
    if (surface)
      filters.push(
        sql`${artifactIntelligence.aiSummary}->>'change_surface' = ${surface}`,
      );
    if (risk)
      filters.push(
        sql`${artifactIntelligence.aiSummary}->>'risk_level' = ${risk}`,
      );
    if (confidence)
      filters.push(
        sql`${artifactIntelligence.aiSummary}->>'confidence' = ${confidence}`,
      );
    if (impact)
      filters.push(
        sql`${artifactIntelligence.aiSummary}->>'behavioral_impact' = ${impact}`,
      );
    if (impactRadius)
      filters.push(
        sql`${artifactIntelligence.aiSummary}->'impact_radius' ? ${impactRadius}`,
      );
    if (relatedTo) filters.push(ne(artifacts.id, relatedTo));

    if (filters.length > 0) {
      query = query.where(and(...filters));
    }

    if (similaritySQL) {
      query = query.orderBy(similaritySQL);
    } else {
      query = query.orderBy(desc(artifacts.sourceCreatedAt));
    }

    return await query.limit(limit).offset(offset);
  } catch (error) {
    logger.error({
      msg: "Get artifacts failed",
      error: error instanceof Error ? error.message : "Unknown error",
      stack: error instanceof Error ? error.stack : undefined,
      context: { limit, offset, semanticQuery: !!q },
    });
    throw new Error("Get artifacts failed.");
  }
}

export async function getSimilarArtifacts(id: number) {
  return await getArtifacts({
    relatedTo: id,
    limit: 1,
  });
}

export async function getArtifact(id: number) {
  try {
    const result = await db
      .select({
        id: artifacts.id,
        title: artifacts.title,
        body: artifacts.body,
        url: artifacts.url,
        author: artifacts.author,
        createdAt: artifacts.sourceCreatedAt,
        state: artifacts.state,

        repoName: trackedRepositories.name,
        repoId: artifacts.repoId,
        techName: technologies.name,
        techId: artifacts.techId,
        intelligence: artifactIntelligence.aiSummary,
      })
      .from(artifacts)
      .innerJoin(
        artifactIntelligence,
        eq(artifacts.id, artifactIntelligence.artifactId),
      )
      .innerJoin(
        trackedRepositories,
        eq(artifacts.repoId, trackedRepositories.id),
      )
      .innerJoin(technologies, eq(artifacts.techId, technologies.id))
      .where(eq(artifacts.id, id))
      .limit(1);

    return result;
  } catch (error) {
    logger.error({
      msg: "Get artifact failed",
      error: error instanceof Error ? error.message : "Unknown error",
      stack: error instanceof Error ? error.stack : undefined,
      context: { id },
    });
    throw new Error("Get artifact failed.");
  }
}
