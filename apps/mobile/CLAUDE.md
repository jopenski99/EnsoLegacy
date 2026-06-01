# CLAUDE.md — apps/mobile

Kotlin / Jetpack Compose Android app for Ensō Legacy. See the [root CLAUDE.md](../../CLAUDE.md) for product context, phases, tone, and domain concepts.

This is where the **core product** lives (Phase 2): collection, add/edit bonsai, species browser, milestones (10-photo flow), stage transitions, care reminders, Tree Passport export, settings.

## Architecture — MVVM (strict)
Keep the layers separated; never skip a layer.

```
Compose screen  →  ViewModel  →  Repository  →  Room DAO / Entity
   (ui/*)          (ui/*)        (data/repository)   (data/local)
```

- **UI** ([ui/](app/src/main/java/com/ensolegacy/mobile/ui/)) — Composables + theme. Stateless where possible; observe state from the ViewModel. No DB or network calls here.
- **ViewModel** — exposes immutable UI state (e.g. `StateFlow`) and handles user intents. Talks to repositories only, **never to DAOs directly.**
- **Repository** ([data/repository/](app/src/main/java/com/ensolegacy/mobile/data/repository/)) — single source of truth; mediates Room (and later remote/Supabase sync).
- **Room** ([data/local/](app/src/main/java/com/ensolegacy/mobile/data/local/)) — `EnsoDatabase`, entities, DAOs. Schema export is on; bump `version` and add a migration for any schema change.

The existing Bonsai slice (entity → DAO → database → repository → ViewModel → `CollectionScreen`) is the **reference pattern** — follow it for new features.

## Conventions
- **Background work → WorkManager** (care reminders, the weather/rain check). Don't use raw threads or `AlarmManager` for these.
- **Weather → Open-Meteo** (free, no API key). Endpoint constants live in `@enso/shared` conceptually — mirror them, don't hardcode new ones.
- **Shared types are mirrored, not imported.** This module can't consume the TS `@enso/shared` package, so Kotlin data classes (e.g. `BonsaiEntity`) intentionally mirror those types. If a shared type or product rule changes in `packages/shared`, update the Kotlin mirror to match.
- Package root: `com.ensolegacy.mobile`. Feature UI under `ui/<feature>`, cross-cutting jobs under `feature/`.
- minSdk 26, targetSdk/compileSdk 35, JDK 17, Compose BOM. Versions are pinned in [build.gradle.kts](build.gradle.kts) and [app/build.gradle.kts](app/build.gradle.kts).

## Build
Built with Gradle / Android Studio — **not** part of the pnpm workspace.
- `./gradlew assembleDebug` — debug APK
- `./gradlew lint` — Android lint
- Note: the Gradle wrapper jar and `local.properties` (Android SDK path) are not committed; open the folder in Android Studio once to generate them, or run `gradle wrapper`.
