CREATE TABLE game_history (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_id          UUID UNIQUE NOT NULL,
    white_player_id  UUID NOT NULL REFERENCES users(id),
    black_player_id  UUID NOT NULL REFERENCES users(id),
    outcome          VARCHAR(20) NOT NULL CHECK (outcome IN ('WHITE_WINS', 'BLACK_WINS', 'DRAW')),
    white_elo_change INT,
    black_elo_change INT,
    pgn              TEXT,
    time_control     VARCHAR(50),
    played_at        TIMESTAMP NOT NULL
);

CREATE INDEX idx_game_history_white ON game_history (white_player_id, played_at DESC);
CREATE INDEX idx_game_history_black ON game_history (black_player_id, played_at DESC);