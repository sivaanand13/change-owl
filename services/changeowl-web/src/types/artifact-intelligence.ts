export type ChangeType =
  | "bugfix"
  | "feature"
  | "refactor"
  | "arch_change"
  | "chore";
export type ChangeSurface =
  | "api"
  | "runtime"
  | "dependency"
  | "internal"
  | "infra";
export type BehavioralImpact =
  | "none"
  | "bugfix"
  | "performance"
  | "functional_change"
  | "breaking_change";
export type RiskLevel = "low" | "medium" | "high";
export type Confidence = "high" | "medium" | "low";

export interface ArtifactIntelligence {
  rationale: string;
  intent: string;
  change_type: ChangeType;
  change_surface: ChangeSurface;
  behavioral_impact: BehavioralImpact;
  impact_radius: string[];
  key_points: string[];
  risk_level: RiskLevel;
  confidence: Confidence;
}
