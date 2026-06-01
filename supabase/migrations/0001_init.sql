-- Ensō Legacy — initial schema.
-- Phase 1 keeps the web side to the waitlist; tree data lands in Phase 2.

create table if not exists waitlist (
  id          uuid primary key default gen_random_uuid(),
  email       text not null unique,
  created_at  timestamptz not null default now()
);

alter table waitlist enable row level security;

-- Anyone may add themselves to the waitlist; nobody may read it from the client.
create policy "Anyone can join the waitlist"
  on waitlist for insert
  to anon, authenticated
  with check (true);

-- Public, read-only tree-history pages are reached by slug (Phase 3).
create table if not exists bonsai (
  id            uuid primary key default gen_random_uuid(),
  owner_id      uuid references auth.users(id) on delete cascade,
  name          text not null,
  species       text not null,
  stage         text not null default 'development',
  acquired_year int,
  public_slug   text unique,
  created_at    timestamptz not null default now(),
  updated_at    timestamptz not null default now()
);

alter table bonsai enable row level security;

create policy "Owners manage their own trees"
  on bonsai for all
  to authenticated
  using (auth.uid() = owner_id)
  with check (auth.uid() = owner_id);

create policy "Public can read shared trees"
  on bonsai for select
  to anon
  using (public_slug is not null);
