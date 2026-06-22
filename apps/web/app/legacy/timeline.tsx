"use client";

import { useEffect, useRef } from "react";
import type { TimelineEvent } from "@/lib/tree-data";

function TimelineDot({ event }: { event: TimelineEvent }) {
  if (event.isPremium) {
    return (
      <div className="absolute -left-[36px] top-0 w-6 h-6 flex items-center justify-center rounded-full bg-primary-container text-on-primary-container ring-4 ring-surface-bright shadow-sm">
        <span className="material-symbols-outlined text-[14px]">workspace_premium</span>
      </div>
    );
  }

  const isFirst = event.year === "1924";

  return (
    <div
      className={
        isFirst
          ? "absolute -left-[30px] top-1.5 w-3 h-3 rounded-full bg-primary border-2 border-surface-bright ring-4 ring-secondary-container"
          : "absolute -left-[30px] top-1.5 w-3 h-3 rounded-full bg-secondary-fixed border-2 border-surface-bright"
      }
    />
  );
}

function TimelineEntry({ event }: { event: TimelineEvent }) {
  const ref = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const el = ref.current;
    if (!el) return;

    const observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            entry.target.classList.add("opacity-100", "translate-y-0");
            entry.target.classList.remove("opacity-0", "translate-y-4");
          }
        }
      },
      { threshold: 0.1 },
    );

    observer.observe(el);
    return () => observer.disconnect();
  }, []);

  return (
    <div
      ref={ref}
      className="relative transition-all duration-700 ease-out opacity-0 translate-y-4"
    >
      <TimelineDot event={event} />
      <div>
        <span
          className={
            event.isPresent
              ? "font-label-md text-label-md text-primary font-bold"
              : event.isPremium
                ? "font-label-md text-label-md text-primary-fixed-dim bg-primary px-2 py-0.5 rounded"
                : "font-label-md text-label-md text-outline"
          }
        >
          {event.year}
        </span>
        <h3 className="font-headline-md text-headline-md mt-sm">{event.title}</h3>
        <p className="mt-base text-on-surface-variant max-w-2xl leading-relaxed">
          {event.description}
        </p>
      </div>
    </div>
  );
}

export default function Timeline({ events }: { events: TimelineEvent[] }) {
  return (
    <div className="timeline-container relative pl-gutter py-base space-y-xl">
      <div className="timeline-line" />
      {events.map((event) => (
        <TimelineEntry key={event.year} event={event} />
      ))}
    </div>
  );
}
