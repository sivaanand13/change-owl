interface ErrorStateProps {
  title: string;
  message: string;
  error?: Error;
  onRetry?: () => void;
  className?: string;
}
import { cn } from "@/lib/utils";
import { AlertTriangle, RefreshCcw, GitCommit } from "lucide-react";

export function ErrorState({
  title,
  message,
  error,
  onRetry,
  className,
}: ErrorStateProps) {
  return (
    <div
      className={cn(
        "flex flex-col items-center justify-center min-h-100 p-8 text-center animate-in fade-in duration-500",
        className,
      )}
    >
      <div className="relative mb-6">
        <div className="absolute inset-0 bg-red-500/10 blur-3xl rounded-full" />
        <div className="relative flex h-20 w-20 items-center justify-center rounded-full border border-red-500/20 bg-background/50 text-status-error shadow-2xl backdrop-blur-xl">
          <AlertTriangle className="h-10 w-10" />
        </div>
      </div>

      <h3 className="text-2xl font-bold text-foreground tracking-tight mb-3">
        {title}
      </h3>
      <p className="max-w-sm text-sm text-muted-foreground leading-relaxed mb-8">
        {message}
      </p>

      <div className="flex flex-wrap items-center justify-center gap-4">
        {onRetry && (
          <button
            onClick={onRetry}
            className="flex items-center gap-2 px-6 py-2.5 rounded-full bg-red-600 text-white text-small font-bold hover:bg-red-500 transition-all shadow-lg shadow-red-600/20 active:scale-95"
          >
            <RefreshCcw size={16} />
            Retry Connection
          </button>
        )}

        <a
          href="https://github.com/sivaanand13/change-owl/issues"
          target="_blank"
          rel="noreferrer"
          className="flex items-center gap-2 px-6 py-2.5 rounded-full border border-border bg-secondary/50 text-muted-foreground text-sm font-semibold hover:bg-secondary hover:text-foreground transition-all"
        >
          <GitCommit size={16} />
          Report Issue
        </a>
      </div>

      {process.env.NODE_ENV === "development" && error && (
        <div className="mt-12 w-full max-w-2xl">
          <div className="flex items-center gap-2 mb-2 px-1">
            <div className="h-1.5 w-1.5 rounded-full bg-red-500 animate-pulse" />
            <span className="text-[10px] font-mono uppercase tracking-widest text-muted-foreground/70">
              System Trace
            </span>
          </div>
          <pre className="p-4 rounded-lg bg-zinc-950 text-red-400/90 text-xs font-mono overflow-auto max-h-48 border border-white/5 no-scrollbar text-left leading-relaxed">
            {error.stack || error.message || JSON.stringify(error, null, 2)}
          </pre>
        </div>
      )}
    </div>
  );
}
