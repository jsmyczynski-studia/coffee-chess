CREATE TABLE games (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    white_player_id  UUID         NOT NULL,
    black_player_id  UUID         NOT NULL,
    status           VARCHAR(32)  NOT NULL DEFAULT 'IN_PROGRESS'
                                 CHECK (status IN ('WAITING_FOR_OPPONENT', 'IN_PROGRESS',
                                                   'WHITE_WINS', 'BLACK_WINS', 'DRAW', 'ABORTED')),
    end_reason       VARCHAR(32)
                                 CHECK (end_reason IS NULL OR end_reason IN (
                                     'CHECKMATE', 'STALEMATE', 'RESIGNATION', 'INSUFFICIENT_MATERIAL',
                                     'FIFTY_MOVE_RULE', 'THREEFOLD_REPETITION', 'TIME_OUT', 'AGREEMENT')),
    current_fen      TEXT         NOT NULL,
    pgn_moves        TEXT,
    time_control     VARCHAR(32),
    white_time_ms    BIGINT       NOT NULL,
    black_time_ms    BIGINT       NOT NULL,
    started_at       TIMESTAMP    NOT NULL,
    ended_at         TIMESTAMP,
    created_at       TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMP
);

CREATE INDEX idx_games_white_player ON games (white_player_id);
CREATE INDEX idx_games_black_player ON games (black_player_id);
CREATE INDEX idx_games_status       ON games (status);
