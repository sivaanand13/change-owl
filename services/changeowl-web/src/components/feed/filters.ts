import { UI_CONFIG } from "@/lib/constants/ui-mappings";
import {
  BehavioralImpact,
  ChangeSurface,
  ChangeType,
  RiskLevel,
} from "@/types/artifact-intelligence";
import { parseAsString, parseAsStringLiteral, createSerializer } from "nuqs";

export const artifactFilterParser = {
  q: parseAsString,
  type: parseAsStringLiteral(
    Object.keys(UI_CONFIG.change_type) as ChangeType[],
  ),
  risk: parseAsStringLiteral(Object.keys(UI_CONFIG.risk_level) as RiskLevel[]),
  surface: parseAsStringLiteral(
    Object.keys(UI_CONFIG.change_surface) as ChangeSurface[],
  ),
  behavior: parseAsStringLiteral(
    Object.keys(UI_CONFIG.behavioral_impact) as BehavioralImpact[],
  ),
  scope: parseAsString,
  relatedTo: parseAsString,
};

export const serializeFilters = createSerializer(artifactFilterParser);
