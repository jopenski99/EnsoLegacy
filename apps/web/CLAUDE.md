# CLAUDE.md — apps/web

Next.js (App Router) site for Ensō Legacy. See the [root CLAUDE.md](../../CLAUDE.md) for product context, phases, tone, and the monorepo overview.

## What lives here
- **Landing page** with email waitlist capture (Phase 1).
- **Public QR tree-history pages** — read-only pages reached by scanning a tree's QR code (Phase 3).

This app is **public and mostly read-only**. The authenticated collection management experience lives in the mobile app, not here.

## Stack & layout
- Next.js 15 (App Router) + React 19, TypeScript.
- Supabase JS client in [lib/supabase.ts](lib/supabase.ts) — anon key only.
- Routes/pages under [app/](app/) (`layout.tsx`, `page.tsx`, `globals.css`).
- Hosted on Vercel (free tier).

## Conventions
- **App Router only** — use Server Components by default; add `"use client"` only when you need interactivity. Don't introduce the legacy `pages/` directory.
- **Secrets:** only `NEXT_PUBLIC_*` vars reach the browser. The Supabase **service-role key is server-only** — use it exclusively in route handlers / server actions, never import it into a client component. Copy [.env.example](.env.example) → `.env.local`.
- **Shared code:** import domain types and product constants from `@enso/shared` (e.g. `MAX_MILESTONE_PHOTOS`, `AD_PLACEMENTS_BY_TIER`) instead of redefining them. It's transpiled via `transpilePackages` in [next.config.mjs](next.config.mjs).
- Keep the landing experience calm and fast — no heavy client JS, no tracking-heavy ad scripts. Ads (Phase 3) are Carbon/Ethical Ads only and never on the public tree-history pages.

## Commands (from this dir or via root Turborepo)
| Command | Does |
|---|---|
| `pnpm dev` | Local dev server (http://localhost:3000) |
| `pnpm build` | Production build |
| `pnpm lint` / `pnpm typecheck` | Lint / typecheck |
