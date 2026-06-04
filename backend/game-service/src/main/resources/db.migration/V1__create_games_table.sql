CREATE TABLE games
(
    id                  UUID         NOT NULL,
    white_player_id     UUID,
    black_player_id     UUID,
    fen                 VARCHAR(255) NOT NULL,
    pgn                 TEXT,
    status              VARCHAR(255) NOT NULL,
    white_time_left_ms  BIGINT,
    black_time_left_ms  BIGINT,
    last_move_timestamp TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_games PRIMARY KEY (id)
);