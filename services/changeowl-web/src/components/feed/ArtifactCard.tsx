"use client";

import React from "react";
import { Artifact } from "@/types/artifact";
import { UI_CONFIG } from "@/lib/constants/ui-mappings";
import { cn } from "@/lib/utils";
import {
  ExternalLink,
  Clock,
  Terminal,
  Search,
  Zap,
  Target,
  Info,
  ArrowRight,
} from "lucide-react";
import Link from "next/link";

interface Props {
  artifact: Artifact;
  type?: "feed" | "similar";
}

export function ArtifactCard({ artifact, type }: Props) {
  const intel = artifact.intelligence;

  const typeStyle = intel ? UI_CONFIG.change_type[intel.change_type] : null;
  const surfaceStyle = intel
    ? UI_CONFIG.change_surface[intel.change_surface]
    : null;
  const impactStyle = intel
    ? UI_CONFIG.behavioral_impact[intel.behavioral_impact]
    : null;
  const riskStyle = intel ? UI_CONFIG.risk_level[intel.risk_level] : null;
  const confStyle = intel ? UI_CONFIG.confidence[intel.confidence] : null;

  if (type && type == "similar") {
    return (
      <div className="group relative rounded-lg bg-white border border-slate-200 p-4 transition-all hover:shadow-md hover:border-brand-primary/20">
        <div className="flex items-center justify-between mb-2">
          {artifact.similarityScore && (
            <div className="flex items-center gap-1 text-[10px] font-black text-emerald-600 bg-emerald-50 px-1.5 py-0.5 rounded border border-emerald-100">
              <Search size={8} />
              {(artifact.similarityScore * 100).toFixed(0)}% MATCH
            </div>
          )}
          <div className="flex items-center gap-1 text-[10px] font-medium text-slate-400">
            <Clock size={10} />
            {new Date(artifact.createdAt).toLocaleString()}
          </div>
        </div>

        <Link href={`/artifacts/${artifact.id}`} className="block">
          <h4 className="text-sm font-bold text-slate-900 leading-snug mb-2 group-hover:text-brand-primary transition-colors line-clamp-2">
            {artifact.title}
          </h4>

          {intel && (
            <div className="text-card-body space-y-2">
              <div className="flex flex-wrap gap-1">
                <span
                  className={cn(
                    "text-[9px] font-bold px-1.5 py-0.5 rounded uppercase tracking-wider",
                    riskStyle?.color,
                  )}
                >
                  {riskStyle?.label}
                </span>
                <span className="text-[9px] font-bold px-1.5 py-0.5 rounded uppercase tracking-wider bg-slate-100 text-slate-600">
                  {typeStyle?.label}
                </span>
              </div>

              <p className="text-xs text-slate-500 line-clamp-2 italic leading-relaxed">
                &quot;{intel.intent}&quot;
              </p>
            </div>
          )}
        </Link>

        <div className="mt-3 pt-3 border-t border-slate-50 flex items-center justify-between">
          <span className="text-[10px] font-semibold text-slate-400 uppercase tracking-tight">
            {artifact.repoName}
          </span>
          <Link
            href={`/artifacts/${artifact.id}`}
            className="text-[10px] font-bold text-brand-primary flex items-center gap-0.5 opacity-0 group-hover:opacity-100 transition-opacity"
          >
            VIEW <ArrowRight size={10} />
          </Link>
        </div>
      </div>
    );
  }

  return (
    <div className="group rounded-card bg-brand-surface shadow-card border border-border-subtle">
      <div className="p-5">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-2">
            <div className="flex items-center gap-1.5 px-2 py-1 bg-secondary/50 rounded text-small font-bold uppercase tracking-tight text-text-muted">
              <Terminal size={12} className="text-brand-primary" />
              {artifact.repoName}
            </div>
            {surfaceStyle && (
              <span className="text-small font-bold uppercase text-text-muted/70">
                {surfaceStyle.icon} {surfaceStyle.label}
              </span>
            )}
          </div>
          {artifact.similarityScore && (
            <div className="flex items-center gap-1 text-small font-black text-status-success bg-status-success/10 px-2 py-1 rounded-full border border-status-success/20">
              <Search size={10} />
              {(artifact.similarityScore * 100).toFixed(0)}% MATCH
            </div>
          )}
        </div>

        <div className="flex justify-between items-start gap-4 mb-5 overflow-auto">
          <h3 className="text-card-title font-bold text-foreground leading-tight">
            {artifact.title}
          </h3>
        </div>

        {intel && (
          <div
            className={
              "glow-border relative isolate text-card-body bg-brand-surface"
            }
          >
            <div className="flex flex-wrap gap-1.5 mb-5">
              <div className="flex items-center gap-1 badge-base text-brand-primary">
                <Zap size={10} fill="currentColor" /> AI INTEL
              </div>
              <span className={cn("badge-base", typeStyle?.color)}>
                {typeStyle?.label}
              </span>
              <span className={cn("badge-base", riskStyle?.color)}>
                {riskStyle?.label}
              </span>
              <span className={cn("badge-base", impactStyle?.color)}>
                {impactStyle?.label}
              </span>
              <span className={cn("badge-base", confStyle?.color)}>
                {confStyle?.label}
              </span>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-4">
                <IntelField
                  icon={<Target size={12} />}
                  label="Intent"
                  value={intel.intent}
                />
                <IntelField
                  icon={<Info size={12} />}
                  label="Rationale"
                  value={intel.rationale}
                  variant="italic"
                />
              </div>

              <div className="space-y-4">
                <IntelList label="Key Insights" items={intel.key_points} />
                <IntelList
                  label="Impact Radius"
                  items={intel.impact_radius}
                  horizontal
                />
              </div>
            </div>
          </div>
        )}

        <div className="mt-5 pt-4 border-t border-border-subtle flex items-center justify-between">
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2 group/author">
              <div className="h-6 w-6 rounded-full bg-brand-secondary text-white flex items-center justify-center text-small font-bold shadow-sm">
                {artifact.author?.[0]?.toUpperCase() ?? "?"}
              </div>
              <span className="text-card-body font-semibold text-text-muted group-hover/author:text-foreground transition-colors">
                {artifact.author}
              </span>
            </div>
            <div className="flex items-center gap-1 text-small font-medium text-text-muted/60">
              <Clock size={11} />
              {new Date(artifact.createdAt).toLocaleString()}
            </div>
          </div>

          <div className="flex flex-row gap-5">
            <a href={artifact.url} target="_blank" className="link-action">
              SOURCE
              <ExternalLink size={14} />
            </a>
            <Link href={`/artifacts/${artifact.id}`} className="link-action">
              DETAILS
              <ExternalLink size={14} />
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
}

function IntelField({
  label,
  value,
  icon,
  variant,
}: {
  label: string;
  value: string;
  icon: React.ReactNode;
  variant?: "italic";
}) {
  return (
    <div className="space-y-1">
      <span className="flex items-center gap-1.5 text-label-muted">
        {icon} {label}
      </span>
      <p
        className={cn(
          "leading-relaxed text-foreground font-medium",
          variant === "italic" && "text-text-muted italic",
        )}
      >
        {value}
      </p>
    </div>
  );
}

function IntelList({
  label,
  items,
  horizontal,
}: {
  label: string;
  items: string[];
  horizontal?: boolean;
}) {
  if (!items?.length) return null;
  return (
    <div className="space-y-2">
      <span className="text-label-muted">{label}</span>
      <ul className={cn("flex gap-1.5", horizontal ? "flex-wrap" : "flex-col")}>
        {items.map((item, i) => (
          <li
            key={i}
            className={cn(
              "leading-tight flex items-start gap-2",
              horizontal
                ? "px-2 py-1 rounded bg-secondary/30 border border-border-subtle text-text-muted"
                : "text-text-muted",
            )}
          >
            {!horizontal && (
              <span className="mt-1 h-1 w-1 rounded-full bg-brand-secondary shrink-0" />
            )}
            {item}
          </li>
        ))}
      </ul>
    </div>
  );
}
