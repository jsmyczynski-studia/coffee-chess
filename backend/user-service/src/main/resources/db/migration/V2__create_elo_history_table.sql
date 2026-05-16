CREATE TABLE elo_history (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES users(id),
    game_id      UUID NOT NULL,
    elo_before   INT  NOT NULL,
    elo_change   INT  NOT NULL,
    elo_after    INT  NOT NULL,
    opponent_elo INT  NOT NULL,
    result       VARCHAR(10) NOT NULL CHECK (result IN ('WIN', 'LOSS', 'DRAW')),
    recorded_at  TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_elo_history_user_id ON elo_history (user_id, recorded_at DESC);