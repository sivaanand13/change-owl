import { NextRequest, NextResponse } from "next/server";
import logger from "@/lib/logger";
import {
  BehavioralImpact,
  ChangeSurface,
  ChangeType,
  Confidence,
  RiskLevel,
} from "@/types/artifact-intelligence";
import { getArtifacts } from "@/lib/services/artifacts";

export async function GET(req: NextRequest) {
  const { searchParams } = new URL(req.url);

  // pagination
  const limit = Math.min(Number(searchParams.get("limit")) || 10, 50);
  const offset = Number(searchParams.get("offset")) || 0;

  // filters
  const changeType = searchParams.get("type") as ChangeType | null;
  const surface = searchParams.get("surface") as ChangeSurface | null;
  const risk = searchParams.get("risk") as RiskLevel | null;
  const confidence = searchParams.get("confidence") as Confidence | null;
  const impact = searchParams.get("behavior") as BehavioralImpact | null;
  const impactRadius = searchParams.get("scope") as string | null;

  // semantic filters
  const q = searchParams.get("q");
  const relatedTo = searchParams.get("related-to")
    ? Number(searchParams.get("related-to"))
    : null;
  const repoId = searchParams.get("repoId")
    ? Number(searchParams.get("repoId"))
    : null;

  const params = {
    limit,
    offset,
    changeType,
    surface,
    risk,
    confidence,
    impact,
    impactRadius,
    q,
    relatedTo,
    repoId,
  };

  try {
    const results = await getArtifacts(params);
    return NextResponse.json(results);
  } catch (error) {
    logger.error({
      endpoint: "GET /artifacts",
      msg: "Artifacts retrieval failed.",
      error: error instanceof Error ? error.message : "Unknown error",
      stack: error instanceof Error ? error.stack : undefined,
      context: params,
    });
    return NextResponse.json(
      { error: "Failed to fetch artifacts." },
      { status: 500 },
    );
  }
}
