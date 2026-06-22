import Gallery from "./gallery";
import Timeline from "./timeline";
import { sentinelData } from "@/lib/tree-data";

export default function LegacyPage() {
  const tree = sentinelData;

  return (
    <>
      <header className="fixed top-0 w-full z-50 bg-surface/70 backdrop-blur-xl shadow-sm">
        <nav className="flex justify-between items-center h-20 px-gutter max-w-container-max mx-auto">
          <div className="font-display-lg text-headline-md tracking-tight text-primary">
            Enso Legacy
          </div>
          <div className="hidden md:flex gap-lg items-center">
            <a
              className="text-primary font-semibold border-b-2 border-primary pb-1 transition-all duration-300"
              href="#"
            >
              Heritage
            </a>
            <a
              className="text-on-surface-variant hover:text-primary transition-colors duration-300"
              href="#"
            >
              Library
            </a>
            <a
              className="text-on-surface-variant hover:text-primary transition-colors duration-300"
              href="#"
            >
              Atelier
            </a>
            <a
              className="text-on-surface-variant hover:text-primary transition-colors duration-300"
              href="#"
            >
              Legacy
            </a>
          </div>
          <div className="flex items-center gap-sm">
            <span className="material-symbols-outlined text-primary cursor-pointer">
              menu
            </span>
          </div>
        </nav>
      </header>

      <main className="pt-32 pb-xl px-gutter max-w-container-max mx-auto">
        <div>
          <header className="mb-lg lg:w-1/2">
            <h1 className="font-display-lg text-display-lg text-primary mb-xs">
              {tree.name}
            </h1>
            <p className="font-body-lg text-body-lg italic text-secondary">
              {tree.species}
            </p>
          </header>

          <Gallery
            images={tree.gallery}
            heroImage={tree.heroImage}
            heroAlt={tree.heroAlt}
            exhibitionLabel={tree.currentExhibition}
          />

          <Timeline events={tree.timeline} />

          <div className="clear-both pt-xl">
            <div className="bg-surface-container-low p-md rounded-xl border border-outline-variant/30 mb-lg flex flex-col md:flex-row justify-between items-center gap-md">
              <div className="flex flex-col items-center md:items-start">
                <span className="font-label-md text-label-md text-outline">
                  Ancestral Value
                </span>
                <span className="font-headline-md text-headline-md text-primary">
                  {tree.ancestralValue}
                </span>
              </div>
              <button className="w-full md:w-auto px-lg py-base bg-primary text-on-primary font-label-md text-label-md rounded-lg flex items-center justify-center gap-sm hover:brightness-110 active:scale-95 transition-all">
                <span className="material-symbols-outlined text-[18px]">
                  history_edu
                </span>
                View Full Pedigree
              </button>
            </div>

            <div className="pt-lg border-t border-secondary-container">
              <h4 className="font-label-md text-label-md text-outline uppercase tracking-widest mb-md">
                Stewardship Gallery
              </h4>
              <div className="flex gap-lg flex-wrap">
                {tree.stewards.map((steward) => (
                  <div
                    key={steward.name}
                    className="group flex flex-col items-center"
                  >
                    <div
                      className={
                        steward.isCurrent
                          ? "w-20 h-20 rounded-full border-2 border-primary p-1 mb-base transition-transform group-hover:scale-105"
                          : "w-20 h-20 rounded-full border border-outline-variant p-1 mb-base transition-transform group-hover:scale-105"
                      }
                    >
                      <div
                        className={
                          steward.isCurrent
                            ? "w-full h-full rounded-full bg-primary-container/20 flex items-center justify-center stewardship-stamp overflow-hidden"
                            : "w-full h-full rounded-full bg-surface-container flex items-center justify-center stewardship-stamp overflow-hidden opacity-80"
                        }
                      >
                        <span
                          className={
                            steward.isCurrent
                              ? "material-symbols-outlined text-primary"
                              : "material-symbols-outlined text-outline"
                          }
                        >
                          qr_code_2
                        </span>
                      </div>
                    </div>
                    <span
                      className={
                        steward.isCurrent
                          ? "font-label-sm text-label-sm text-primary font-bold"
                          : "font-label-sm text-label-sm text-on-surface-variant"
                      }
                    >
                      {steward.name}
                    </span>
                  </div>
                ))}
              </div>
            </div>
          </div>
        </div>
      </main>
    </>
  );
}
