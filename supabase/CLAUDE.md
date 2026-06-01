# CLAUDE.md — supabase

Postgres database, auth, storage, migrations, and edge functions for Ensō Legacy. See the [root CLAUDE.md](../../CLAUDE.md) for product context.

## Layout
- [migrations/](migrations/) — ordered, immutable SQL migrations (`NNNN_name.sql`). The schema is whatever the migrations produce.
- [seed/seed.sql](seed/seed.sql) — local-only development data. **Never** run against production.
- [functions/](functions/) — Deno edge functions (e.g. `weather-rain-check`).
- [config.toml](config.toml) — local Supabase stack config.

## Rules
- **Migrations are append-only and immutable.** To change schema, add a new numbered migration — never edit a migration that has run. Keep them idempotent where practical (`create table if not exists`).
- **Row Level Security is mandatory.** Every table with user data must `enable row level security` and ship explicit policies in the same migration. Default-deny: the client uses the anon/auth key, so anything without a policy is inaccessible (which is correct, not a bug).
- Public QR tree-history pages read `bonsai` rows **only** where `public_slug is not null` — preserve that policy boundary; never expose private trees to `anon`.
- **Service-role key is server-only** (edge functions / web route handlers). It bypasses RLS — never ship it to a client.
- Edge functions run on **Deno** (`Deno.serve`, web `fetch`, URL imports) — not Node. Keep them small and stateless.

## Commands (Supabase CLI)
| Command | Does |
|---|---|
| `supabase start` | Boot the local stack (Postgres, Studio, Auth) |
| `supabase db reset` | Recreate local DB from migrations + seed |
| `supabase migration new <name>` | Scaffold a new migration file |
| `supabase functions serve <name>` | Run an edge function locally |
| `supabase functions deploy <name>` | Deploy an edge function |
