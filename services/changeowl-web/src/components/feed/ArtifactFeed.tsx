"use client";

import { ArtifactFilters, useArtifacts } from "@/hooks/use-artifacts";
import { ArtifactCard } from "./ArtifactCard";
import { Loader2 } from "lucide-react";
import { ErrorState } from "../ui/ErrorState";
import { Virtuoso } from "react-virtuoso";
import { useQueryStates } from "nuqs";
import { artifactFilterParser } from "./filters";
import { FilterGroup } from "./FilterGroup";
import { useCallback } from "react";

export function ArtifactFeed() {
  const [filters, setFilters] = useQueryStates(artifactFilterParser, {
    shallow: false,
    history: "push",
  });
  const { artifacts, setSize, isLoadingMore, error, isReachingEnd } =
    useArtifacts(filters);

  const setFiltersCallback = useCallback(
    (filters: ArtifactFilters) => {
      setFilters({
        ...filters,
      });
    },
    [setFilters],
  );

  const loadMore = () => {
    if (!isReachingEnd && !isLoadingMore) {
      setSize((prev) => prev + 1);
    }
  };

  if (error) {
    return (
      <ErrorState
        title="Something went wrong"
        message="We're having trouble loading the feed right now. Please try refreshing the page or check back in a few minutes."
        error={error}
        onRetry={() => setSize(1)}
      />
    );
  }

  if (!artifacts) {
    return (
      <div className="flex justify-center p-20">
        <Loader2 className="animate-spin text-brand-primary" />
      </div>
    );
  }

  return (
    <div className="max-h-[80vh] h-screen flex justify-center w-full">
      <div className="w-full flex flex-col gap-5 max-w-4xl justify-center">
        <div className="relative z-50 w-full">
          <FilterGroup
            className="w-[80vw] max-w-4xl"
            filters={filters}
            setFilters={setFiltersCallback}
          />
        </div>
        <Virtuoso
          className=" no-scrollbar"
          data={artifacts}
          endReached={loadMore}
          increaseViewportBy={300}
          itemContent={(index, artifact) => (
            <div className="my-5">
              <ArtifactCard artifact={artifact} />
            </div>
          )}
          components={{
            Footer: () => {
              if (isLoadingMore) {
                return (
                  <div className="flex justify-center p-20">
                    <Loader2
                      className="animate-spin text-slate-400"
                      size={20}
                    />
                  </div>
                );
              }

              if (isReachingEnd) {
                return (
                  <div className="flex flex-col items-center justify-center p-20 gap-2 border-t border-slate-100 mt-10">
                    <p className="text-card-body font-bold text-slate-800 uppercase tracking-widest">
                      {artifacts.length === 0
                        ? "No matching artifacts found"
                        : "End of Intelligence Stream"}
                    </p>
                    <p className="text-small text-slate-600">
                      {artifacts.length === 0
                        ? "Try adjusting your filters or search query"
                        : `Indexed ${artifacts.length} patterns across distributed systems`}
                    </p>
                  </div>
                );
              }
              return null;
            },
          }}
        />
      </div>
    </div>
  );
}
