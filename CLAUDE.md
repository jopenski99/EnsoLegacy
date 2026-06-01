# CLAUDE.md — Ensō Legacy (root)

> *Your collection. Its history. Forever.*
> Ensō Legacy preserves the story of a bonsai collection so it survives as long as the trees do. A bonsai can outlive its owner by centuries; this project exists to make sure its history does too.

This is the **root** instruction file — shared context for the whole monorepo. Each workspace has its own `CLAUDE.md` with details specific to that codebase; read the relevant one before working there:

- [apps/web/CLAUDE.md](apps/web/CLAUDE.md) — Next.js landing page + public QR tree-history pages
- [apps/mobile/CLAUDE.md](apps/mobile/CLAUDE.md) — Kotlin / Jetpack Compose app (MVVM, Room)
- [packages/shared/CLAUDE.md](packages/shared/CLAUDE.md) — shared TypeScript types & constants
- [supabase/CLAUDE.md](supabase/CLAUDE.md) — Postgres schema, migrations, edge functions

---

## Project Overview

Ensō Legacy is a bonsai collection legacy platform: a mobile app for tracking trees, milestones, photos, care reminders, and provenance, plus a web presence (landing page + public QR "tree history" pages) and a future hardware sensor line.

The product is built around emotional, long-term value — a tree's documented history (its "Tree Passport") is the core asset. Monetization is intentionally gentle: free tier with light ethical ads, optional donations, paid export editions, and later subscriptions and hardware.

---

## Monorepo Layout

```
enso-legacy/
├── apps/
│   ├── web/          → Next.js (landing page + QR tree history page)
│   └── mobile/       → Kotlin / Jetpack Compose (Gradle, not in pnpm workspace)
├── packages/
│   └── shared/       → @enso/shared — shared types, constants, utilities
└── supabase/         → migrations, seed data, edge functions
```

- **Package manager:** pnpm workspaces + Turborepo. JS/TS workspaces are `apps/web` and `packages/shared`.
- **`apps/mobile` is a Gradle/Android project** and is *not* part of the pnpm workspace — it's built with Gradle/Android Studio. It mirrors the shared types by hand (see its CLAUDE.md).
- Shared types, constants, and product rules belong in `packages/shared` so web (directly) and mobile (by mirroring) share one source of truth. When you change a rule there, update the mobile mirror too.

### Common commands (run from repo root)
| Command | Does |
|---|---|
| `pnpm install` | Install JS/TS deps for all workspaces |
| `pnpm dev` | Run all dev servers via Turborepo (`apps/web`) |
| `pnpm build` | Build all JS/TS workspaces |
| `pnpm lint` / `pnpm typecheck` | Lint / typecheck across workspaces |

Mobile is built separately in Android Studio or via `./gradlew` inside `apps/mobile`.

---

## Roadmap & Priorities

Ship in phases. Respect the current phase — don't pull future-phase work forward unless asked.

1. **Phase 1 — Foundation (Month 1–3):** domain, monorepo, landing page, email waitlist. **← current focus**
2. **Phase 2 — Core App (Month 3–6):** MVP mobile app, 50–100 real users from the Davao bonsai community, feedback loop.
3. **Phase 3 — First Revenue (Month 6–12):** Tree Passport export, Ko-fi donations, light ads, public QR scan page.
4. **Phase 4 — Paid Plans (Year 2):** mid-tier & Legacy subscriptions, community events tab, BLE sensor integration.
5. **Phase 5 — Hardware + Scale (Year 3+):** white-label then custom sensor hardware, physical Legacy Book, expansion.

---

## Domain Concepts

- **Bonsai** — a tree in a user's collection.
- **Milestone** — a documented event in a tree's life; supports up to a 10-photo flow (`MAX_MILESTONE_PHOTOS`).
- **Stage transition** — a tree moving between life stages (`seedling → development → refinement → mature → legacy`); a tracked event.
- **Tree Passport** — exportable history of a single tree. Basic PDF free; Legacy Edition (full timeline, QR, provenance) paid.
- **QR tree history page** — public web page reachable by scanning a tree's QR code.
- **Gratitude Leaf** — a quiet, private profile mark earned by supporting the project. No labels, no leaderboards.
- **Ensō Sensor** — future BLE hardware (soil moisture/temp, ambient temp, light, advanced pH/salinity).

---

## Business Model (context for product decisions)

- Free tier with **3 non-intrusive ad placements** (Carbon/Ethical Ads). Donors see ~50% fewer; subscribers see none. Encoded in `AD_PLACEMENTS_BY_TIER`.
- Revenue order over time: Tree Passport Legacy Edition ($4–8) → donations → ads → subscriptions (mid ~$35/yr, Legacy ~$55/yr) → hardware ($39–49) → printed Legacy Book.
- App is free for the first 6–12 months before paid plans launch.
- **Tree planting:** 1 tree planted per account, +1 per leaf earned (partner: One Tree Planted).

---

## Tech Stack

| Layer | Choice |
|---|---|
| Mobile | Kotlin · Jetpack Compose · MVVM · Room · WorkManager |
| Weather | Open-Meteo (free, no API key) |
| Web | Next.js (App Router) · React |
| Backend / DB | Supabase (Postgres, Auth, Storage) |
| Web hosting | Vercel (free tier) |
| Payments / Donations | Stripe · Ko-fi (early) |
| Email | Resend or Postmark |
| Monorepo | pnpm + Turborepo |
| Domain | Cloudflare Registrar |

---

## Mission & Tone

- **Community-first, Davao-first.** The events surface is a curated bulletin board (events, exhibits, workshops, cutting trades, club directory) — *not* a social feed.
- The product voice is calm, reverent, and patient. **Avoid hype, growth-hacking, dark patterns, and engagement-maximizing mechanics.** Donation prompts trigger at meaningful moments, never naggy.

---

## Working Conventions

- **Match the surrounding code** — naming, idioms, comment density, structure.
- Keep changes **small, self-contained, and reviewable.** This is a solo project built by a founder with a full-time job and a baby — slow and consistent beats fast and burnt out. One task per session.
- Prefer the **free / no-key services** the stack already chose (e.g. Open-Meteo).
- When a task is ambiguous about phase or scope, ask rather than expanding scope.

---

*Source: Ensō Legacy Notion workspace config (v1.0). Update these files as the project evolves.*
