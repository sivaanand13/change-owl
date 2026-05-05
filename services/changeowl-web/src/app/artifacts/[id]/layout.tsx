import { ArtifactCard } from "@/components/feed/ArtifactCard";
import { Artifact } from "@/types/artifact";

export default async function ArtifactLayout({
  children,
  params,
}: {
  children: React.ReactNode;
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;

  const query = new URLSearchParams({
    "related-to": id,
    limit: "10",
  });

  const res = await fetch(
    `${process.env.PUBLIC_URL}/api/artifacts?${query.toString()}`,
    {
      next: { revalidate: 3600 },
    },
  );

  const similarArtifacts: Artifact[] = res.ok ? await res.json() : [];

  return (
    <div className="flex h-screen w-full bg-background overflow-hidden">
      <main className="flex-1 overflow-y-auto bg-brand-surface no-scrollbar">
        {children}
      </main>

      <aside className="w-80 flex flex-col bg-brand-surface border-l border-border-subtle shrink-0">
        <div className="p-6 flex-1 overflow-y-auto no-scrollbar">
          <header className="mb-6">
            <h3 className="text-label-muted flex items-center gap-2">
              <span className="h-1.5 w-1.5 rounded-full bg-brand-primary" />
              Similar Intelligence
            </h3>
          </header>

          <div className="space-y-4">
            {similarArtifacts.map((similar) => (
              <ArtifactCard
                type="similar"
                key={similar.id}
                artifact={similar}
              />
            ))}

            {similarArtifacts.length === 0 && (
              <div className="p-4 rounded-lg border border-dashed border-border-subtle text-center">
                <p className="text-small text-text-muted italic">
                  No matching intelligence found.
                </p>
              </div>
            )}
          </div>
        </div>
      </aside>
    </div>
  );
}
