import useSWRInfinite from "swr/infinite";
import { Artifact } from "@/types/artifact";
import {
  BehavioralImpact,
  ChangeSurface,
  ChangeType,
  Confidence,
  RiskLevel,
} from "@/types/artifact-intelligence";

export interface ArtifactFilters {
  q?: string | null;
  type?: ChangeType | null;
  risk?: RiskLevel | null;
  surface?: ChangeSurface | null;
  confidence?: Confidence | null;
  behavior?: BehavioralImpact | null;
  scope?: string | null;
  relatedTo?: string | null;
}

const fetcher = async (url: string) => {
  const res = await fetch(url);

  if (!res.ok) {
    const info = await res.json();
    const error = new Error(
      info.error || "An error occurred while fetching data.",
    );
    throw error;
  }
  return await res.json();
};

export function useArtifacts(filters: ArtifactFilters = {}) {
  const { data, error, size, setSize, mutate } = useSWRInfinite<Artifact[]>(
    (index, previousPageData) => {
      if (previousPageData && !previousPageData.length) return null;

      const params = new URLSearchParams({
        limit: "10",
        offset: (index * 10).toString(),
      });

      if (filters.q) params.append("q", filters.q);
      if (filters.type) params.append("type", filters.type);
      if (filters.risk) params.append("risk", filters.risk);
      if (filters.surface) params.append("surface", filters.surface);
      if (filters.confidence) params.append("confidence", filters.confidence);
      if (filters.behavior) params.append("impact", filters.behavior);
      if (filters.scope) params.append("radius", filters.scope);
      if (filters.relatedTo) params.append("relatedTo", filters.relatedTo);

      return `/api/artifacts?${params.toString()}`;
    },
    fetcher,
    {
      revalidateFirstPage: true,
      revalidateOnFocus: false,
      persistSize: true,
    },
  );

  const artifacts = data ? data.flat() : [];

  const isLoadingMore: boolean =
    (!data && !error) ||
    (size > 0 && data && typeof data[size - 1] === "undefined") ||
    false;
  const isReachingEnd: boolean =
    data?.[0]?.length === 0 ||
    (data && data[data.length - 1]?.length < 10) ||
    false;

  return {
    artifacts,
    error,
    isLoadingMore,
    isReachingEnd,
    size,
    setSize,
    mutate,
  };
}
