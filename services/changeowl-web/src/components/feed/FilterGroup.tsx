"use client";

import { useEffect, useState } from "react";
import { UI_CONFIG } from "@/lib/constants/ui-mappings";
import { X, XIcon } from "lucide-react";
import { cn } from "@/lib/utils";
import { useDebounce } from "@/hooks/common/use-debounce";

interface FilterBarProps {
  filters: Record<string, string | null | undefined>;
  setFilters: (updates: Record<string, string | null | undefined>) => void;
  className: string;
}

export function FilterGroup({
  filters,
  setFilters,
  className,
}: FilterBarProps) {
  const [searchQuery, setSearchQuery] = useState(filters.q || "");
  const debouncedQuery = useDebounce(searchQuery);

  const clearFilters = () => {
    setSearchQuery("");
    setFilters({
      q: undefined,
      type: undefined,
      risk: undefined,
      surface: undefined,
      behavior: undefined,
    });
  };

  const clearQuery = () => {
    setSearchQuery("");
    setFilters({
      ...filters,
      q: undefined,
    });
  };

  useEffect(() => {
    const normalizedQuery = debouncedQuery || null;
    const normalizedFilter = filters.q || null;
    if (normalizedQuery !== normalizedFilter) {
      setFilters({ ...filters, q: normalizedQuery });
    }
  }, [debouncedQuery, setFilters, filters.q, filters]);

  const handleSelect = (key: string, value: string) => {
    setFilters({
      ...filters,
      [key]: filters[key] === value ? null : value,
    });
  };

  const hasFilters = Object.values(filters).some((v) => v !== "" && v !== null);

  return (
    <div
      className={cn(
        "w-1.5 flex flex-wrap items-center gap-2 p-2 bg-surface-interactive border border-slate-200 rounded-lg",
        className,
      )}
    >
      <div className="relative flex-1 w-full">
        <input
          type="text"
          placeholder="Search artifacts..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="w-full pl-3 pr-8 py-1.5 text-small bg-white border border-slate-200 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500/20"
        />
        {filters.q && (
          <X
            size={14}
            className="absolute right-2 top-2.5 text-slate-400 cursor-pointer hover:text-slate-600"
            onClick={clearQuery}
          />
        )}
      </div>

      <div className="flex items-center gap-1.5">
        <FilterDropdown
          label="Type"
          activeValue={filters.type ?? null}
          options={UI_CONFIG.change_type}
          onSelect={(val: string) => handleSelect("type", val)}
        />

        <FilterDropdown
          label="Risk"
          activeValue={filters.risk ?? null}
          options={UI_CONFIG.risk_level}
          onSelect={(val: string) => handleSelect("risk", val)}
        />

        <FilterDropdown
          label="Surface"
          activeValue={filters.surface ?? null}
          options={UI_CONFIG.change_surface}
          onSelect={(val: string) => handleSelect("surface", val)}
        />

        <FilterDropdown
          label="Behavior"
          activeValue={filters.behavior ?? null}
          options={UI_CONFIG.behavioral_impact}
          onSelect={(val: string) => handleSelect("behavior", val)}
        />
      </div>

      <button
        onClick={clearFilters}
        disabled={!hasFilters}
        title="Clear all filters"
        className={`
        flex items-center justify-center w-8 h-8 rounded-md transition-all duration-200
        ${
          hasFilters
            ? "text-slate-900 hover:bg-slate-200 hover:text-slate-600 cursor-pointer opacity-100"
            : "text-slate-200 cursor-not-allowed opacity-80"
        }
      `}
      >
        <XIcon
          size={14}
          className={hasFilters ? "animate-in spin-in-180 duration-500" : ""}
        />
      </button>
    </div>
  );
}

interface FilterDropdownProps {
  label: string;
  activeValue: string | null;
  options: Record<string, { label: string; color?: string }>;
  onSelect: (value: string) => void;
}

export function FilterDropdown({
  label,
  activeValue,
  options,
  onSelect,
}: FilterDropdownProps) {
  return (
    <select
      value={activeValue || ""}
      onChange={(e) => onSelect(e.target.value)}
      className="mx-1 text-small"
      aria-label={`Filter by ${label}`}
    >
      <option value="">{label}</option>
      {Object.entries(options).map(([key, config]) => (
        <option key={key} value={key}>
          {config.label}
        </option>
      ))}
    </select>
  );
}
