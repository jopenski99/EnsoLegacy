# 🌿 Ensō Legacy

> *Your collection. Its history. Forever.*

A bonsai collection legacy platform — a mobile app for tracking trees, milestones, photos, care, and provenance, plus a public web presence and a future hardware sensor line.

## Monorepo

```
enso-legacy/
├── apps/
│   ├── web/          Next.js — landing page + public QR tree-history pages
│   └── mobile/       Kotlin / Jetpack Compose — the core app (Gradle)
├── packages/
│   └── shared/       @enso/shared — shared types & product constants
└── supabase/         Postgres schema, migrations, edge functions
```

## Getting started

```bash
# JS/TS workspaces (web + shared)
pnpm install
pnpm dev          # runs apps/web at http://localhost:3000

# Web env: copy apps/web/.env.example -> apps/web/.env.local and fill in Supabase keys

# Mobile: open apps/mobile in Android Studio (generates the Gradle wrapper),
# or run `gradle wrapper` then `./gradlew assembleDebug`.

# Supabase (requires the Supabase CLI):
supabase start
```

## Working in this repo

Each workspace has a `CLAUDE.md` with its own conventions. Start with the root [CLAUDE.md](CLAUDE.md), then the relevant workspace file.

Built slowly and deliberately — see the roadmap in [CLAUDE.md](CLAUDE.md).
