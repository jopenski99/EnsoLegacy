-- Local-only seed data for development. Never run against production.
insert into waitlist (email) values
  ('davao.collector@example.com'),
  ('legacy.tester@example.com')
on conflict (email) do nothing;
