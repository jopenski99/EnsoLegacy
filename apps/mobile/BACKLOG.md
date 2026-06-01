# Ensō Legacy — Mobile App Backlog

Working backlog for the Kotlin/Compose app (`apps/mobile`). Mirrored in Notion.
See [CLAUDE.md](CLAUDE.md) for architecture and the root [CLAUDE.md](../../CLAUDE.md) for phases/tone.

## ✅ Done so far
- [x] MVVM reference slice wired end-to-end (Room → repository → ViewModel → Collection screen)
- [x] Manual ServiceLocator DI (`EnsoApp`)
- [x] Add a tree (name, species dropdown, stage, health, acquired year)
- [x] Delete a tree
- [x] Species starter catalog (bundled asset, shared with web)
- [x] Life stages: Juvenile / In Training / Mature
- [x] Health status: Healthy / Needs Care / Critical (+ Room v2 migration)
- [x] Light theme (primary) + dummy dark theme + system switching
- [x] CLI build/run tooling (`scripts/run-app.ps1`)

## 🎨 Design & theming
- [ ] Design the real **dark theme** palette
- [ ] Bundle a **brand serif font** (replace system serif placeholder)
- [ ] Real **tree photo thumbnails** (replace initial-letter placeholder)
- [ ] App icon + branded splash screen
- [ ] Empty-state illustration (replace the ◯ placeholder)

## 🧭 Navigation & shell
- [ ] **Bottom navigation**: Dashboard / Collection / Schedule / Settings
- [ ] Navigation library/graph setup (Navigation-Compose)
- [ ] Top bar with user/collection name + avatar

## 📊 Dashboard
- [ ] **Collection Overview** cards (total, counts per stage)
- [ ] **Filter/sort tabs**: Age / Stage / Health
- [ ] "Your Collection" list with rich cards

## 🌳 Tree detail & editing
- [ ] Tree detail screen (full profile + timeline)
- [ ] Edit tree
- [ ] **Acquisition source** field + badges (Yamadori, Cutting, Nursery Stock, Seedling…)
- [ ] Multiple photos per tree

## 📸 Milestones
- [ ] Milestone data model (entity/DAO/repo) + migration
- [ ] Log a milestone (title, notes, date)
- [ ] **10-photo flow** (`MAX_MILESTONE_PHOTOS`)
- [ ] Milestone timeline view on tree detail

## 🔁 Stage transitions
- [ ] Record a stage change (tracked event)
- [ ] Stage history on the tree timeline

## ⏰ Care & scheduling
- [ ] Care reminder model (repot, prune, fertilize, water, wire removal…)
- [ ] **WorkManager** scheduled reminders + notifications
- [ ] "X days since…" / "due in…" indicators on cards
- [ ] Schedule screen (upcoming tasks)
- [ ] **Weather/rain check** via Open-Meteo (WorkManager)

## 🔎 Species
- [ ] **Species browser** ("Browse Catalog" — currently a stub)
- [ ] Species detail (scientific name, family, care notes)
- [ ] Expand the starter catalog

## 👋 Onboarding
- [ ] Onboarding flow (3 screens from the design)
- [ ] First-run vs returning logic

## ⚙️ Settings & account
- [ ] Settings screen
- [ ] Theme toggle (light/dark/system)
- [ ] Auth (Supabase) — later

## 🗄️ Data, sync & export (later phases)
- [ ] Supabase sync (repository remote source)
- [ ] **Tree Passport** export (basic PDF free)
- [ ] Public **QR tree-history** linkage
- [ ] Gratitude Leaf profile mark

## 🛠️ Technical / infra
- [ ] First git commit + branch strategy
- [ ] Unit tests (repository, migrations) + UI tests
- [ ] CI build (GitHub Actions: assembleDebug + lint)
- [ ] Reconsider DI (Hilt) if wiring grows
- [ ] Accessibility pass (content descriptions, contrast, touch targets)
