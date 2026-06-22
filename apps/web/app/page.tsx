import type { Metadata } from "next";

export const metadata: Metadata = {
  title: "Ensō Legacy — Honoring the Passage of Time",
};

const images = {
  hero: "https://lh3.googleusercontent.com/aida-public/AB6AXuAjAYyyvt7pLGFHzaL2Rd4w-ZY_oakYfGYg_IlBYjvl2lOs3QKIAHjLbnmNj1RaRv1JUipuKcZ_PSI7Cb0YnhfdIaquXkc4lDCqSL4c8Xh7PwM_Sgeo1mtTAdbCaq-U3TDcLyhztcJp8e0zBAidfln8FX7ifMmCQ4sZT0j5dAbZuY19prDl0kCxh93GvMRrp3aXjbyZUjUR-PwLVemh2uF9dT_0IDZbZV330NrrLZkz43mybnUM9jKbZ6tLSRZj-Lfs2FY3EytKQ1M",
  curator: "https://lh3.googleusercontent.com/aida-public/AB6AXuC896NcTkJVJ-AQwP_mv6Y_adJBCmxRBRG1Rj9huEVRmdNioABGE0oA4GjSDE1pW8XR4BwmTCByqC-YkuHX7QiPvBMVhYXB3kDz57h12OyNBvMdE4X48hwK6qRXzKwI2KnIsnaMm-di931eaoAKQCHhq_tUT33w3m7jHzwvqFNomLxbtIu2FI3VWbwol942wkgLd5lvyUee9QrL1HzNCzJsydaLwAnPZFwLBJnQVpr9qrreK6jYU976MNR3Gn60EMN5Gd9zckOr-6E",
  pruning: "https://lh3.googleusercontent.com/aida-public/AB6AXuC6ImLb4F45xvNArWq0TbKr_zGsjSUezOUQUx0pihYPQPQNymCaBtjxGelbDnDonXmXrgjmM-E_rNBBgolzw35XbtMJe44okO5QRNp6IF-rxBdzCbNtod2rvhcfkGMvIQ2pq9xjnF0nycITR5oZP4MAtea3Yi8LadRy2mByVtqadygdRv_n0_CQ31dfHtKbXt03uPq6GTajQOnkepQGBblZ6v9e4TXySnq6PwbEFtF7SJ6H0IuwDIdpI_g1EauvvpxzsMtF7U64OwQ",
  wire: "https://lh3.googleusercontent.com/aida-public/AB6AXuCtjNqO-DEV4OmRGQUqYd1xsRvRCLFrK2fMZ_4f2_sevtxxMKT88fxEjfYSu0aEMbwY5Z8n4Nx0Pp-yxq1qzSntMIs1NVk1-_oF5LEpCT-NctFxZndE7mjq5OLQQT-97O_9EBhA46Z2kCPmh91F68WEeTe28IPI3XzgTDS86eTO8xqUCl5RFyOOvLwvdXGsU5og6eeb8LgwejRUcIJBbU0QFRkRAw4w8E-jJlzWY-SA4t2w_-DSHVPp1vF4IAg9gkYodUqF743-ANE",
  passport: "https://lh3.googleusercontent.com/aida-public/AB6AXuDvcI9ZHMQ1WDMM1yOHRLpxwSqTjY08SmBis4dzpqPQMUHd6jmTV6N5gRuXIbYJa62Lp8IbCnCFNI2LX_3_Y5pIWI3zdsgLkbgT7fTbP4dLK3y13cPu8uaW2J68TrpSg_N9G5hJH5NCcfIcXR0-UH48U2983EN3fgd_7kPQIgqUiWABvUVmBDKL0Hkz0zJFHphugSwIv0Asi5eQJ4bvp-BJQ4f6ii3Hfk8KUTeKW1-aohux242ukF5eCFFj_yqfH8DrKyr7pfPDztw",
};

export default function HomePage() {
  return (
    <>
      <nav className="fixed top-0 w-full z-50 bg-background/70 backdrop-blur-xl border-b border-surface-container-highest/20">
        <div className="flex justify-between items-center px-gutter md:px-xl py-sm max-w-container-max mx-auto">
          <div className="font-headline-md text-headline-md text-primary tracking-tight">
            Ensō Legacy
          </div>
          <div className="hidden md:flex items-center space-x-lg">
            <a className="font-label-md text-label-md text-primary border-b-2 border-primary pb-1" href="#">
              Heritage
            </a>
            <a className="font-label-md text-label-md text-on-surface-variant hover:text-primary transition-colors" href="#">
              Library
            </a>
            <a className="font-label-md text-label-md text-on-surface-variant hover:text-primary transition-colors" href="#">
              Atelier
            </a>
            <a className="font-label-md text-label-md text-on-surface-variant hover:text-primary transition-colors" href="#">
              Legacy
            </a>
          </div>
          <div className="flex items-center space-x-md">
            <button className="hidden sm:block font-label-md text-label-md text-on-surface-variant hover:opacity-80 transition-opacity">
              Log In
            </button>
            <button className="bg-primary text-on-primary px-lg py-sm rounded-lg font-label-md text-label-md hover:opacity-90 transition-opacity transform active:scale-[0.98]">
              Get Started
            </button>
          </div>
        </div>
      </nav>

      <header className="pt-xl pb-lg px-gutter md:px-xl max-w-container-max mx-auto min-h-screen flex flex-col justify-center">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-lg items-center">
          <div className="order-2 lg:order-1 reveal-on-scroll">
            <h1 className="font-display-lg text-display-lg text-on-background mb-md max-w-lg">
              Honoring the <span className="italic text-primary">Passage</span> of Time
            </h1>
            <p className="font-body-lg text-body-lg text-on-surface-variant mb-lg max-w-md">
              The digital steward for your living masterpieces. Document every wire, every clip, and every seasonal change for the next generation.
            </p>
            <div className="flex flex-col sm:flex-row gap-sm">
              <button className="bg-primary text-on-primary px-lg py-md rounded-lg font-label-md text-label-md hover:opacity-90 transition-all flex items-center justify-center gap-base">
                Start Your Legacy
                <span className="material-symbols-outlined text-[18px]">arrow_forward</span>
              </button>
              <button className="bg-secondary-container text-on-secondary-container px-lg py-md rounded-lg font-label-md text-label-md hover:bg-opacity-80 transition-all text-center">
                Explore Philosophy
              </button>
            </div>
          </div>
          <div className="order-1 lg:order-2 relative reveal-on-scroll" style={{ transitionDelay: "200ms" }}>
            <div className="aspect-[4/5] rounded-xl overflow-hidden shadow-lg border border-surface-container-highest/40">
              <img
                className="w-full h-full object-cover"
                alt="A cinematic, high-fidelity studio photograph of an ancient, gnarled Juniper bonsai tree with dramatic deadwood (jin) and vibrant green foliage. The tree is set against a soft, textured cream-colored background with warm, diffused gallery lighting that emphasizes its sculptural form and heritage. The overall aesthetic is serene, minimalist, and premium, evoking a sense of timelessness and botanical art."
                src={images.hero}
              />
            </div>
            <div className="absolute -bottom-base -left-base bg-surface-container-low/90 backdrop-blur-md p-md rounded-lg shadow-xl border border-surface-container-highest/20 hidden md:block">
              <div className="flex items-center gap-sm">
                <div className="w-sm h-sm rounded-full bg-primary animate-pulse"></div>
                <span className="font-label-sm text-label-sm text-on-surface-variant uppercase tracking-widest">Master Record: Shimpaku #842</span>
              </div>
              <p className="font-headline-md text-headline-md text-primary mt-xs italic">84 Years in Training</p>
            </div>
          </div>
        </div>
      </header>

      <section className="py-xl bg-surface-container-low">
        <div className="max-w-container-max mx-auto px-gutter md:px-xl">
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-xl items-center">
            <div className="reveal-on-scroll">
              <span className="font-label-md text-label-md text-primary mb-sm block">HERITAGE &amp; CARE</span>
              <h2 className="font-display-lg text-headline-lg text-on-background mb-md">Tree Lineage &amp; Digital Provenance</h2>
              <p className="font-body-md text-body-md text-on-surface-variant mb-lg">
                Track every tree in your collection—from its first cutting to its final form. Document origin, species details, and the decade-long journey of each specimen with archival-grade precision.
              </p>
              <div className="space-y-md">
                <div className="flex items-start gap-md">
                  <div className="w-lg h-lg rounded-full bg-primary-container flex items-center justify-center text-on-primary-container flex-shrink-0">
                    <span className="material-symbols-outlined">history_edu</span>
                  </div>
                  <div>
                    <h4 className="font-label-md text-label-md text-on-background">Archival Timeline</h4>
                    <p className="font-body-md text-label-sm text-on-surface-variant">Continuous logs of soil mixes, pot history, and pruning styles.</p>
                  </div>
                </div>
                <div className="flex items-start gap-md">
                  <div className="w-lg h-lg rounded-full bg-primary-container flex items-center justify-center text-on-primary-container flex-shrink-0">
                    <span className="material-symbols-outlined">verified_user</span>
                  </div>
                  <div>
                    <h4 className="font-label-md text-label-md text-on-background">Provenance Verification</h4>
                    <p className="font-body-md text-label-sm text-on-surface-variant">Immutable ownership records for high-value heritage trees.</p>
                  </div>
                </div>
              </div>
            </div>
            <div className="reveal-on-scroll bg-surface border border-surface-container-highest/40 rounded-xl p-lg shadow-sm relative overflow-hidden" style={{ transitionDelay: "200ms" }}>
              <div className="absolute top-0 right-0 p-md">
                <span className="material-symbols-outlined text-outline">more_vert</span>
              </div>
              <div className="flex items-center gap-sm mb-lg">
                <div className="w-12 h-12 rounded-full overflow-hidden border border-primary/20">
                  <img
                    className="w-full h-full object-cover"
                    alt="A professional headshot of a mature Japanese bonsai master in traditional attire, looking serenely into the camera. The background is a blurred bonsai nursery at dusk with soft golden light. High-end, editorial portrait photography style."
                    src={images.curator}
                  />
                </div>
                <div>
                  <p className="font-label-md text-label-md text-on-background">Yugen Bonsai</p>
                  <p className="font-label-sm text-label-sm text-on-surface-variant">Curator Since 1994</p>
                </div>
              </div>
              <div className="relative timeline-line ml-base space-y-lg">
                <div className="relative pl-lg">
                  <div className="absolute left-0 top-1 w-6 h-6 rounded-full bg-primary border-4 border-background z-10"></div>
                  <span className="font-label-sm text-label-sm text-primary uppercase tracking-widest">2024 — PRESENT</span>
                  <h5 className="font-headline-md text-headline-md text-on-background mt-xs">Structural Pruning</h5>
                  <p className="font-body-md text-label-sm text-on-surface-variant mt-xs">Refined apex and thinned secondary branches for winter silhouettes.</p>
                  <div className="mt-md flex gap-sm">
                    <div className="w-20 h-20 rounded-lg overflow-hidden border border-surface-container-highest">
                      <img
                        className="w-full h-full object-cover"
                        alt="Macro close-up photograph of bonsai pruning tools—shears and wire cutters—resting on a textured wood surface next to a trimmed pine branch. Soft natural light, deep shadows, archival mood."
                        src={images.pruning}
                      />
                    </div>
                    <div className="w-20 h-20 rounded-lg overflow-hidden border border-surface-container-highest">
                      <img
                        className="w-full h-full object-cover"
                        alt="High-detail shot of a bonsai branch with copper wire perfectly spiraled around it. The bark of the tree is textured and old. Focused, macro photography with warm tones."
                        src={images.wire}
                      />
                    </div>
                  </div>
                </div>
                <div className="relative pl-lg">
                  <div className="absolute left-1 top-1 w-4 h-4 rounded-full bg-secondary-fixed border-2 border-background z-10"></div>
                  <span className="font-label-sm text-label-sm text-on-surface-variant uppercase tracking-widest">2021</span>
                  <h5 className="font-label-md text-label-md text-on-background mt-xs">Repotted in Tokoname Clay</h5>
                  <p className="font-body-md text-label-sm text-on-surface-variant">Transferred to a handmade shallow rectangle pot by master cermaicist.</p>
                </div>
                <div className="relative pl-lg">
                  <div className="absolute left-0 top-1 w-6 h-6 rounded-full bg-surface-container flex items-center justify-center border-4 border-background z-10 shadow-sm">
                    <span className="material-symbols-outlined text-[12px] text-primary" style={{ fontVariationSettings: "'FILL' 1" }}>stars</span>
                  </div>
                  <span className="font-label-sm text-label-sm text-primary uppercase tracking-widest">2018 — MILESTONE</span>
                  <h5 className="font-label-md text-label-md text-on-background mt-xs">Yamadori Collection</h5>
                  <p className="font-body-md text-label-sm text-on-surface-variant">Sourced from the Nagano foothills. Initial recovery and stabilization.</p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section className="py-xl">
        <div className="max-w-container-max mx-auto px-gutter md:px-xl">
          <div className="text-center mb-xl reveal-on-scroll">
            <span className="font-label-md text-label-md text-primary mb-sm block">CARE ORCHESTRATION</span>
            <h2 className="font-display-lg text-headline-lg text-on-background mb-md">Seasonal Rhythms</h2>
            <p className="font-body-md text-body-md text-on-surface-variant max-w-2xl mx-auto">
              Intelligent reminders for repotting, wiring, and seasonal care, synchronized with local weather patterns to ensure your trees thrive through every solstice.
            </p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-md">
            <div className="reveal-on-scroll bg-[#fcebe1] p-lg rounded-xl shadow-sm hover:shadow-md transition-shadow group">
              <div className="w-12 h-12 rounded-lg bg-primary-container/20 flex items-center justify-center text-primary-container mb-md group-hover:scale-110 transition-transform">
                <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>ac_unit</span>
              </div>
              <h4 className="font-label-md text-label-md text-on-background mb-sm">Winter Dormancy</h4>
              <p className="font-label-sm text-label-sm text-on-surface-variant">Monitor frost levels and adjust hydration for deciduous varieties.</p>
            </div>
            <div className="reveal-on-scroll bg-[#fcebe1] p-lg rounded-xl shadow-sm hover:shadow-md transition-shadow group" style={{ transitionDelay: "100ms" }}>
              <div className="w-12 h-12 rounded-lg bg-primary-container/20 flex items-center justify-center text-primary-container mb-md group-hover:scale-110 transition-transform">
                <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>local_florist</span>
              </div>
              <h4 className="font-label-md text-label-md text-on-background mb-sm">Spring Awakening</h4>
              <p className="font-label-sm text-label-sm text-on-surface-variant">The window for repotting opens as buds begin to swell. Prepare your substrate.</p>
            </div>
            <div className="reveal-on-scroll bg-[#fcebe1] p-lg rounded-xl shadow-sm hover:shadow-md transition-shadow group" style={{ transitionDelay: "200ms" }}>
              <div className="w-12 h-12 rounded-lg bg-primary-container/20 flex items-center justify-center text-primary-container mb-md group-hover:scale-110 transition-transform">
                <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>wb_sunny</span>
              </div>
              <h4 className="font-label-md text-label-md text-on-background mb-sm">Summer Vigor</h4>
              <p className="font-label-sm text-label-sm text-on-surface-variant">Pinching growth and intensive watering schedules during peak heat.</p>
            </div>
            <div className="reveal-on-scroll bg-[#fcebe1] p-lg rounded-xl shadow-sm hover:shadow-md transition-shadow group" style={{ transitionDelay: "300ms" }}>
              <div className="w-12 h-12 rounded-lg bg-primary-container/20 flex items-center justify-center text-primary-container mb-md group-hover:scale-110 transition-transform">
                <span className="material-symbols-outlined" style={{ fontVariationSettings: "'FILL' 1" }}>eco</span>
              </div>
              <h4 className="font-label-md text-label-md text-on-background mb-sm">Autumn Hardening</h4>
              <p className="font-label-sm text-label-sm text-on-surface-variant">Final structural wiring and preparations for the coming winter quiet.</p>
            </div>
          </div>
        </div>
      </section>

      <section className="py-xl bg-primary text-on-primary relative overflow-hidden">
        <div className="absolute top-0 right-0 w-1/2 h-full opacity-10 pointer-events-none">
          <svg viewBox="0 0 400 400" xmlns="http://www.w3.org/2000/svg">
            <path d="M37.5,-64.2C48.6,-57.4,57.7,-47.5,65.6,-36.3C73.5,-25.1,80.2,-12.5,79.5,-0.4C78.8,11.7,70.7,23.3,62,33.1C53.3,42.8,44,50.7,33.5,58.7C23,66.6,11.5,74.7,-1.4,77.1C-14.3,79.5,-28.5,76.2,-39.8,68.7C-51.1,61.2,-59.4,49.5,-66.6,37.3C-73.8,25.1,-79.8,12.5,-81,0.2C-82.2,-12.1,-78.6,-24.1,-71.4,-34.8C-64.2,-45.5,-53.4,-54.9,-41.8,-61.5C-30.2,-68.1,-15.1,-71.9,-1,-70.2C13.1,-68.5,26.3,-71,37.5,-64.2Z" fill="#ffffff" transform="translate(200 200)" />
          </svg>
        </div>
        <div className="max-w-container-max mx-auto px-gutter md:px-xl relative z-10">
          <div className="grid grid-cols-1 lg:grid-cols-12 gap-lg items-center">
            <div className="lg:col-span-7 reveal-on-scroll">
              <h2 className="font-display-lg text-display-lg mb-md leading-tight">
                When the time comes to pass your bonsai on, <span className="italic font-normal">its entire history goes with it.</span>
              </h2>
              <p className="font-body-lg text-body-lg text-primary-fixed mb-lg opacity-80">
                The Ensō Tree Passport™ is a secure, verifiable record of stewardship. Transfer documentation, photos, and maintenance logs to the next owner with a single click, ensuring the legacy continues uninterrupted.
              </p>
              <div className="flex flex-wrap gap-md">
                <div className="flex items-center gap-sm">
                  <span className="material-symbols-outlined text-primary-fixed">qr_code_2</span>
                  <span className="font-label-sm text-label-sm tracking-widest uppercase">Encrypted History</span>
                </div>
                <div className="flex items-center gap-sm">
                  <span className="material-symbols-outlined text-primary-fixed">share_reviews</span>
                  <span className="font-label-sm text-label-sm tracking-widest uppercase">Ownership Transfer</span>
                </div>
                <div className="flex items-center gap-sm">
                  <span className="material-symbols-outlined text-primary-fixed">print</span>
                  <span className="font-label-sm text-label-sm tracking-widest uppercase">Printable Certificates</span>
                </div>
              </div>
            </div>
            <div className="lg:col-span-5 reveal-on-scroll" style={{ transitionDelay: "200ms" }}>
              <div className="bg-background/10 backdrop-blur-md p-lg rounded-xl border border-white/10 shadow-2xl">
                <div className="flex justify-between items-start mb-lg">
                  <div className="font-headline-md text-headline-md italic">The Passport</div>
                  <div className="px-sm py-xs bg-primary-fixed text-on-primary-fixed font-label-sm text-[10px] rounded-full uppercase tracking-tighter">Verified</div>
                </div>
                <div className="aspect-square mb-md rounded-lg overflow-hidden border border-white/20">
                  <img
                    className="w-full h-full object-cover"
                    alt="A moody, high-contrast black and white photograph of an ancient bonsai root base, showing the deep texture and age of the nebari. The lighting is focused and artistic, reminiscent of a historical archive photo. Sophisticated and sculptural."
                    src={images.passport}
                  />
                </div>
                <div className="space-y-sm">
                  <div className="flex justify-between border-b border-white/10 pb-xs">
                    <span className="font-label-sm text-label-sm opacity-60">Common Name</span>
                    <span className="font-label-md text-label-md">Japanese Black Pine</span>
                  </div>
                  <div className="flex justify-between border-b border-white/10 pb-xs">
                    <span className="font-label-sm text-label-sm opacity-60">Estimated Age</span>
                    <span className="font-label-md text-label-md">112 Years</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="font-label-sm text-label-sm opacity-60">Original Collector</span>
                    <span className="font-label-md text-label-md">K. Watanabe, 1912</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <footer className="bg-surface-container-low text-primary relative w-full mt-xl">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-gutter px-gutter md:px-xl py-lg max-w-container-max mx-auto">
          <div className="space-y-md">
            <div className="font-headline-md text-headline-md text-primary">Ensō Legacy</div>
            <p className="font-body-md text-label-sm text-on-surface-variant max-w-[200px]">Honoring the passage of time through digital stewardship.</p>
          </div>
          <div>
            <h5 className="font-label-md text-label-md text-on-background mb-md">Foundation</h5>
            <ul className="space-y-sm">
              <li><a className="font-label-sm text-label-sm text-on-surface-variant hover:text-primary hover:underline underline-offset-4 transition-colors" href="#">Philosophy</a></li>
              <li><a className="font-label-sm text-label-sm text-on-surface-variant hover:text-primary hover:underline underline-offset-4 transition-colors" href="#">Provenance</a></li>
              <li><a className="font-label-sm text-label-sm text-on-surface-variant hover:text-primary hover:underline underline-offset-4 transition-colors" href="#">Care Guides</a></li>
            </ul>
          </div>
          <div>
            <h5 className="font-label-md text-label-md text-on-background mb-md">Resource</h5>
            <ul className="space-y-sm">
              <li><a className="font-label-sm text-label-sm text-on-surface-variant hover:text-primary hover:underline underline-offset-4 transition-colors" href="#">Archives</a></li>
              <li><a className="font-label-sm text-label-sm text-on-surface-variant hover:text-primary hover:underline underline-offset-4 transition-colors" href="#">Newsletter</a></li>
              <li><a className="font-label-sm text-label-sm text-on-surface-variant hover:text-primary hover:underline underline-offset-4 transition-colors" href="#">Privacy</a></li>
            </ul>
          </div>
          <div className="space-y-md">
            <h5 className="font-label-md text-label-md text-on-background">Stay Informed</h5>
            <p className="font-label-sm text-label-sm text-on-surface-variant">Occasional letters on history, care, and updates.</p>
            <div className="relative">
              <input
                className="w-full bg-transparent border-0 border-b border-outline-variant focus:border-primary focus:ring-0 px-0 py-xs text-on-background placeholder:opacity-50 transition-all"
                placeholder="Your email address"
                type="email"
              />
              <button className="absolute right-0 bottom-1">
                <span className="material-symbols-outlined text-[20px] text-primary">arrow_forward</span>
              </button>
            </div>
          </div>
        </div>
        <div className="border-t border-surface-container-highest/30 py-lg px-gutter md:px-xl max-w-container-max mx-auto flex flex-col md:flex-row justify-between items-center gap-md">
          <div className="font-label-sm text-label-sm text-on-surface-variant opacity-60">© 2024 Ensō Legacy — Honoring the passage of time.</div>
          <div className="flex items-center gap-lg">
            <a className="text-on-surface-variant hover:text-primary transition-colors" href="#"><span className="material-symbols-outlined">share</span></a>
            <a className="text-on-surface-variant hover:text-primary transition-colors" href="#"><span className="material-symbols-outlined">language</span></a>
          </div>
        </div>
      </footer>

      <script
        dangerouslySetInnerHTML={{
          __html: `
            const observerOptions = { threshold: 0.1 };
            const observer = new IntersectionObserver((entries) => {
              entries.forEach(entry => {
                if (entry.isIntersecting) {
                  entry.target.classList.add('visible');
                }
              });
            }, observerOptions);
            document.querySelectorAll('.reveal-on-scroll').forEach(el => observer.observe(el));
            window.addEventListener('scroll', () => {
              const nav = document.querySelector('nav');
              if (window.scrollY > 50) {
                nav.classList.add('py-xs');
                nav.classList.remove('py-sm');
                nav.classList.add('shadow-sm');
              } else {
                nav.classList.add('py-sm');
                nav.classList.remove('py-xs');
                nav.classList.remove('shadow-sm');
              }
            });
            document.querySelectorAll('button').forEach(btn => {
              btn.addEventListener('mousedown', () => btn.style.transform = 'scale(0.98)');
              btn.addEventListener('mouseup', () => btn.style.transform = 'scale(1)');
              btn.addEventListener('mouseleave', () => btn.style.transform = 'scale(1)');
            });
          `,
        }}
      />
    </>
  );
}
