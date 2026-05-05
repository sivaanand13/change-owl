import { notFound } from "next/navigation";
import { Artifact } from "@/types/artifact";
import { UI_CONFIG } from "@/lib/constants/ui-mappings";
import { cn } from "@/lib/utils";
import Markdown from "react-markdown";
import {
  ShieldAlert,
  Target,
  Zap,
  Activity,
  Info,
  ExternalLink,
} from "lucide-react";

interface ArtifactPageProps {
  params: Promise<{ id: string }>;
}

export default async function ArtifactPage({ params }: ArtifactPageProps) {
  const { id } = await params;

  const res = await fetch(`${process.env.PUBLIC_URL}/api/artifacts/${id}`, {
    next: { revalidate: 3600 },
  });

  if (!res.ok) return notFound();
  const artifact: Artifact = await res.json();
  const intel = artifact.intelligence;

  const riskStyle = intel ? UI_CONFIG.risk_level[intel.risk_level] : null;

  return (
    <article className="p-8 w-full">
      <header className="mb-10 border-b border-border-subtle pb-8">
        <div className="flex items-center gap-3 mb-4">
          <span className="badge-base text-small font-bold text-text-muted uppercase tracking-widest">
            {artifact.repoName}
          </span>
        </div>

        <h1 className="text-4xl font-extrabold text-foreground tracking-tight leading-none mb-6">
          {artifact.title}
        </h1>

        <div className="flex items-center gap-6 text-info text-text-muted">
          <div className="flex items-center gap-2">
            <div className="h-8 w-8 rounded-full bg-brand-secondary text-white flex items-center justify-center font-bold">
              {artifact.author?.[0].toUpperCase()}
            </div>
            <span className="font-semibold text-foreground">
              {artifact.author}
            </span>
          </div>
          <span>•</span>
          <time>
            {new Date(artifact.createdAt).toLocaleDateString(undefined, {
              dateStyle: "long",
            })}
          </time>
        </div>
      </header>

      <div className="grid grid-cols-1 lg:grid-cols-12 gap-12">
        <div className="lg:col-span-12 space-y-10">
          {intel && (
            <div className="grid lg:grid-cols-6 gap-6 items-stretch">
              <section className="lg:col-span-4 glow-border p-6 flex flex-col justify-between">
                <div className="grid md:grid-cols-2 gap-8">
                  <div className="flex flex-col gap-2">
                    <h3 className="text-label-muted flex items-center gap-2">
                      <Target size={14} /> Strategic Intent
                    </h3>
                    <p className="text-card-body font-medium italic">
                      &quot;{intel.intent}&quot;
                    </p>
                  </div>

                  <div className="flex flex-col gap-2">
                    <h3 className="text-label-muted flex items-center gap-2">
                      <Info size={14} /> AI Rationale
                    </h3>
                    <p className="text-card-body text-text-muted">
                      {intel.rationale}
                    </p>
                  </div>
                </div>

                <div className="grid md:grid-cols-2 gap-8 pt-6 border-t border-border-subtle">
                  <div className="flex flex-col gap-4">
                    <h3 className="text-label-muted uppercase tracking-tighter">
                      Key Intelligence
                    </h3>
                    <ul className="space-y-2">
                      {intel.key_points.map((point, i) => (
                        <li
                          key={i}
                          className="flex items-start gap-3 text-card-body text-text-muted"
                        >
                          <span className="mt-1.5 h-1.5 w-1.5 rounded-full bg-brand-primary shrink-0" />
                          {point}
                        </li>
                      ))}
                    </ul>
                  </div>

                  <div className="flex flex-col gap-4">
                    <h3 className="text-label-muted uppercase tracking-tighter">
                      Impact Radius
                    </h3>
                    <div className="flex flex-wrap gap-2">
                      {intel.impact_radius.map((area, i) => (
                        <span
                          key={i}
                          className="px-2 py-1 rounded border border-border-subtle bg-surface-interactive text-small font-bold uppercase text-text-muted"
                        >
                          {area}
                        </span>
                      ))}
                    </div>
                  </div>
                </div>
              </section>

              <section className="lg:col-span-2 glow-border p-6 flex flex-col justify-between">
                <h2 className="text-label-muted flex items-center gap-2">
                  <Zap
                    size={14}
                    className="text-brand-accent fill-brand-accent"
                  />
                  Classification
                </h2>

                <div className="flex flex-col gap-6">
                  <div className="flex flex-col gap-2">
                    <label className="text-label-muted">Risk Assessment</label>
                    <div
                      className={cn(
                        "badge-base w-full justify-center py-2 text-small",
                        riskStyle?.color,
                      )}
                    >
                      <ShieldAlert size={16} className="mr-2" />
                      {riskStyle?.label}
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="text-label-muted">Surface</label>
                      <p className="text-info font-bold uppercase">
                        {intel?.change_surface
                          ? UI_CONFIG.change_surface[intel.change_surface]
                              ?.label
                          : "Unknown"}
                      </p>
                    </div>

                    <div>
                      <label className="text-label-muted">Confidence</label>
                      <p className="text-info font-bold uppercase">
                        {
                          UI_CONFIG.confidence[intel?.confidence ?? "low"]
                            ?.label
                        }
                      </p>
                    </div>
                  </div>

                  {intel?.behavioral_impact && (
                    <div className="pt-6 border-t border-border-subtle">
                      <label className="text-label-muted block mb-2">
                        Behavioral Impact
                      </label>
                      <div className="flex items-start gap-2 text-card-body text-text-muted italic">
                        <Activity
                          size={14}
                          className="mt-0.5 shrink-0 text-brand-secondary"
                        />
                        {
                          UI_CONFIG.behavioral_impact[intel.behavioral_impact]
                            .label
                        }
                      </div>
                    </div>
                  )}
                </div>
              </section>
            </div>
          )}

          <section className="prose prose-slate max-w-none">
            <h2 className="text-card-title font-bold text-foreground mb-4 border-l-4 border-brand-secondary pl-4 overflow-scroll">
              Raw Document
            </h2>
            <div className="bg-brand-surface p-6 rounded-card border border-border-subtle">
              <Markdown>{artifact.body}</Markdown>
            </div>
          </section>

          <div>
            <a
              href={artifact.url}
              target="_blank"
              className="link-action group"
            >
              INVESTIGATE ORIGINAL SOURCE
              <ExternalLink
                size={14}
                className="group-hover:translate-x-1 transition-transform"
              />
            </a>
          </div>
        </div>
      </div>
    </article>
  );
}
