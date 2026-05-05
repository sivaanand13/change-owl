import {
  ChangeType,
  ChangeSurface,
  BehavioralImpact,
  RiskLevel,
  Confidence,
} from "@/types/artifact-intelligence";

export const UI_CONFIG = {
  change_type: {
    bugfix: {
      label: "Bug Fix",
      color: "bg-emerald-500/10 text-emerald-600 border-emerald-200",
    },
    feature: {
      label: "Feature",
      color: "bg-blue-500/10 text-blue-600 border-blue-200",
    },
    refactor: {
      label: "Refactor",
      color: "bg-indigo-500/10 text-indigo-600 border-indigo-200",
    },
    arch_change: {
      label: "Architecture",
      color: "bg-amber-500/10 text-amber-700 border-amber-200",
    },
    chore: {
      label: "System / Chore",
      color: "bg-slate-500/10 text-slate-600 border-slate-200",
    },
  } as Record<ChangeType, { label: string; color: string }>,

  change_surface: {
    api: { label: "API", icon: "🌐" },
    runtime: { label: "Execution / Logic", icon: "⚙️" },
    dependency: { label: "Dependency", icon: "📦" },
    internal: { label: "Core / Internal", icon: "🧩" },
    infra: { label: "Infrastructure", icon: "🏗️" },
  } as Record<ChangeSurface, { label: string; icon: string }>,

  behavioral_impact: {
    none: { label: "Stable Behavior", color: "text-slate-400" },
    bugfix: { label: "Resolves issue", color: "text-emerald-500" },
    performance: { label: "Performance boost", color: "text-cyan-500" },
    functional_change: { label: "Functional change", color: "text-blue-500" },
    breaking_change: {
      label: "Breaking Change",
      color: "text-red-600 font-bold",
    },
  } as Record<BehavioralImpact, { label: string; color: string }>,

  risk_level: {
    low: { label: "Low Risk", color: "bg-green-100 text-green-700" },
    medium: { label: "Medium Risk", color: "bg-amber-100 text-amber-700" },
    high: {
      label: "High Risk",
      color: "bg-red-100 text-red-700 border-red-200",
    },
  } as Record<RiskLevel, { label: string; color: string }>,

  confidence: {
    high: {
      label: "High Confidence",
      color:
        "bg-status-success/10 text-status-success border-status-success/20",
    },
    medium: {
      label: "Medium Confidence",
      color: "bg-brand-primary/10 text-brand-primary border-brand-primary/20",
    },
    low: {
      label: "Uncertain",
      color: "bg-status-error/10 text-status-error border-status-error/20",
    },
  } as Record<Confidence, { label: string; color: string }>,
};
