# CLAUDE.md — packages/shared (`@enso/shared`)

The single source of truth for cross-platform domain types and product rules. See the [root CLAUDE.md](../../CLAUDE.md) for the bigger picture.

## What belongs here
- **Domain types** ([src/types.ts](src/types.ts)) — `Bonsai`, `Milestone`, `StageTransition`, `TreePassport`, `SensorReading`, `GratitudeLeaf`, etc.
- **Product constants / rules** ([src/constants.ts](src/constants.ts)) — values that MUST stay identical everywhere: `MAX_MILESTONE_PHOTOS`, `FREE_TIER_AD_PLACEMENTS`, `AD_PLACEMENTS_BY_TIER`, tree-planting tiers, Open-Meteo endpoints.

## Rules
- **Framework-agnostic.** No React, no Android, no DB-driver or Node-only imports. Plain TypeScript only — this is consumed by `apps/web` directly and *mirrored* by `apps/mobile` (Kotlin).
- **Consumed as raw TS source** (`main`/`types` point at `src/index.ts`; web transpiles it). Re-export everything through [src/index.ts](src/index.ts).
- Use `.js` extensions in relative imports (ESM/`NodeNext` resolution), e.g. `export * from "./types.js"`.
- **Changing a type or rule here is a cross-cutting change.** Update the consuming code in `apps/web` AND the hand-written Kotlin mirror in `apps/mobile` (e.g. `BonsaiEntity`) in the same change.

## Commands
- `pnpm typecheck` — type-check without emitting
- `pnpm build` — emit `dist/` (only needed if a consumer ever wants compiled output)
