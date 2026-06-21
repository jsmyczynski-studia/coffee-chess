-- Move existing rows from the old local UUID to the Keycloak subject UUID before
-- dropping keycloak_id. Foreign keys must cascade this primary-key update.
ALTER TABLE elo_history DROP CONSTRAINT IF EXISTS elo_history_user_id_fkey;
ALTER TABLE elo_history
    ADD CONSTRAINT elo_history_user_id_fkey
    FOREIGN KEY (user_id) REFERENCES users(id) ON UPDATE CASCADE;

ALTER TABLE game_history DROP CONSTRAINT IF EXISTS game_history_white_player_id_fkey;
ALTER TABLE game_history
    ADD CONSTRAINT game_history_white_player_id_fkey
    FOREIGN KEY (white_player_id) REFERENCES users(id) ON UPDATE CASCADE;

ALTER TABLE game_history DROP CONSTRAINT IF EXISTS game_history_black_player_id_fkey;
ALTER TABLE game_history
    ADD CONSTRAINT game_history_black_player_id_fkey
    FOREIGN KEY (black_player_id) REFERENCES users(id) ON UPDATE CASCADE;

ALTER TABLE friendships DROP CONSTRAINT IF EXISTS friendships_requester_id_fkey;
ALTER TABLE friendships
    ADD CONSTRAINT friendships_requester_id_fkey
    FOREIGN KEY (requester_id) REFERENCES users(id) ON UPDATE CASCADE;

ALTER TABLE friendships DROP CONSTRAINT IF EXISTS friendships_addressee_id_fkey;
ALTER TABLE friendships
    ADD CONSTRAINT friendships_addressee_id_fkey
    FOREIGN KEY (addressee_id) REFERENCES users(id) ON UPDATE CASCADE;

UPDATE users
SET id = keycloak_id::uuid
WHERE keycloak_id IS NOT NULL
  AND keycloak_id ~* '^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$'
  AND id <> keycloak_id::uuid;

-- Drop keycloak_id column (no longer needed, we use Keycloak subject UUID directly as user id).
ALTER TABLE users DROP COLUMN IF EXISTS keycloak_id;

-- Remove the default gen_random_uuid() from id since we set it explicitly from JWT subject.
ALTER TABLE users ALTER COLUMN id DROP DEFAULT;
