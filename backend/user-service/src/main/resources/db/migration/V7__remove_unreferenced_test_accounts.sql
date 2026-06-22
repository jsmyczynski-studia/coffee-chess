-- V6 created development-only application profiles for the hardcoded Keycloak users.
-- Credentials never belonged in user-service; Keycloak is the identity store.
-- Keep historical rows that are already referenced, but remove unused seed profiles.
DELETE FROM users u
WHERE u.id IN (
    '11111111-1111-1111-1111-111111111111',
    '22222222-2222-2222-2222-222222222222',
    '33333333-3333-3333-3333-333333333333'
)
AND NOT EXISTS (SELECT 1 FROM elo_history e WHERE e.user_id = u.id)
AND NOT EXISTS (
    SELECT 1 FROM game_history g
    WHERE g.white_player_id = u.id OR g.black_player_id = u.id
)
AND NOT EXISTS (
    SELECT 1 FROM friendships f
    WHERE f.requester_id = u.id OR f.addressee_id = u.id
);
