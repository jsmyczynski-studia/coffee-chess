-- Seed three hardcoded test accounts for local/dev testing.
-- The ids MUST match the Keycloak user ids in infra/keycloak/coffee-chess-realm.json
-- so the JWT subject lines up with the local row (post-V5 the id IS the Keycloak subject).
-- Logins/passwords are intentionally very short: a/a, b/b, c/c (password lives in Keycloak).
INSERT INTO users (id, nickname, email, elo_rating, games_played, games_won, games_lost, games_drawn, created_at)
VALUES
    ('11111111-1111-1111-1111-111111111111', 'a', 'a@test.cc', 1200, 0, 0, 0, 0, NOW()),
    ('22222222-2222-2222-2222-222222222222', 'b', 'b@test.cc', 1200, 0, 0, 0, 0, NOW()),
    ('33333333-3333-3333-3333-333333333333', 'c', 'c@test.cc', 1200, 0, 0, 0, 0, NOW())
ON CONFLICT (id) DO NOTHING;
