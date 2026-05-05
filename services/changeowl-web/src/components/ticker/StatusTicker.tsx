"use client";
import { useSpring, motion, useTransform } from "framer-motion";
import { useTranslations } from "next-intl";
import { useEffect, useState } from "react";

type TickerData = {
  repositories: number;
  artifacts: number;
  insights: number;
};

function AnimatedNumber({ value }: { value: number }) {
  const spring = useSpring(0, { mass: 0.8, stiffness: 75, damping: 15 });
  const display = useTransform(spring, (current) =>
    Math.round(current).toLocaleString(),
  );

  useEffect(() => {
    spring.set(value);
  }, [value, spring]);

  return <motion.span className="text-white font-bold">{display}</motion.span>;
}

export default function StatusTicker() {
  const t = useTranslations("Status");
  const [data, setData] = useState<TickerData>({
    repositories: 0,
    artifacts: 0,
    insights: 0,
  });

  useEffect(() => {
    const es = new EventSource("/api/ticker");

    es.onmessage = (event) => {
      try {
        const parsed = JSON.parse(event.data);
        setData(parsed);
      } catch (err) {
        console.error("SSE parse error", err);
      }
    };

    es.onerror = (err) => {
      console.error("SSE error", err);
      es.close();
    };

    return () => {
      es.close();
    };
  }, []);

  return (
    <div className="w-full bg-brand-secondary py-1.5 text-white/90 border-white/10">
      <div className="container mx-auto px-4 flex flex-wrap justify-between items-center text-ticker font-mono tracking-widest uppercase">
        <div className="flex items-center gap-4">
          <div className="flex items-center gap-2">
            <div className="h-1.5 w-1.5 rounded-full bg-brand-primary animate-pulse" />
            <AnimatedNumber value={data?.repositories ?? 0} />
            <span>{t("reposTracked")}</span>
          </div>

          <span className="opacity-30">|</span>

          <div className="flex items-center gap-2">
            <AnimatedNumber value={data.artifacts ?? 0} />
            <span>{t("artifactsIngested")}</span>
          </div>
        </div>

        <div className="flex mx-0.5 items-center gap-3">
          <span className="bg-brand-accent/20 text-brand-accent px-2 py-0.5 rounded border border-brand-accent/30 flex items-center gap-2">
            <AnimatedNumber value={data.insights ?? 0} />
            {t("intelligentInsights")}
          </span>
        </div>
      </div>
    </div>
  );
}
