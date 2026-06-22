"use client";

import { useState } from "react";
import type { GalleryImage } from "@/lib/tree-data";

export default function Gallery({
  images,
  heroImage,
  heroAlt,
  exhibitionLabel,
}: {
  images: GalleryImage[];
  heroImage: string;
  heroAlt: string;
  exhibitionLabel: string;
}) {
  const [activeSrc, setActiveSrc] = useState(heroImage);

  return (
    <aside className="float-gallery space-y-md lg:mb-lg">
      <div className="rounded-xl overflow-hidden bg-surface-container shadow-2xl relative aspect-[3/4] group">
        <img
          alt={heroAlt}
          className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-105"
          src={activeSrc}
        />
        <div className="absolute bottom-0 left-0 right-0 p-lg bg-gradient-to-t from-black/60 to-transparent">
          <p className="text-white font-label-md text-label-md">{exhibitionLabel}</p>
        </div>
      </div>

      <div className="grid grid-cols-4 gap-sm">
        {images.map((img) => (
          <button
            key={img.src}
            onClick={() => setActiveSrc(img.src)}
            className={
              img.src === activeSrc
                ? "aspect-square rounded-lg overflow-hidden border-2 border-primary shadow-sm"
                : "aspect-square rounded-lg overflow-hidden border border-outline-variant opacity-60 hover:opacity-100 transition-opacity"
            }
          >
            <img
              className="w-full h-full object-cover"
              alt={img.alt}
              src={img.src}
            />
          </button>
        ))}
        <button className="aspect-square rounded-lg overflow-hidden bg-surface-container-high flex flex-col items-center justify-center gap-xs border border-dashed border-outline-variant text-outline hover:text-primary hover:border-primary transition-all">
          <span className="material-symbols-outlined">add_a_photo</span>
          <span className="text-[10px] font-bold uppercase tracking-tighter">Add</span>
        </button>
      </div>
    </aside>
  );
}
