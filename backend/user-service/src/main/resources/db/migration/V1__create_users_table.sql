CREATE TABLE users (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    keycloak_id VARCHAR(255) UNIQUE NOT NULL,
    nickname    VARCHAR(32)  UNIQUE NOT NULL,
    email       VARCHAR(255) UNIQUE NOT NULL,
    elo_rating  INT          NOT NULL DEFAULT 1200,
    games_played INT         NOT NULL DEFAULT 0,
    games_won   INT          NOT NULL DEFAULT 0,
    games_lost  INT          NOT NULL DEFAULT 0,
    games_drawn INT          NOT NULL DEFAULT 0,
    avatar_url  VARCHAR(512),
    created_at  TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMP
);

CREATE INDEX idx_users_elo_rating ON users (elo_rating DESC);
CREATE INDEX idx_users_nickname   ON users (nickname);