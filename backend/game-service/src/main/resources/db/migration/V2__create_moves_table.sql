CREATE TABLE moves (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_id      UUID         NOT NULL REFERENCES games(id) ON DELETE CASCADE,
    move_number  INT          NOT NULL,
    color        VARCHAR(8)   NOT NULL CHECK (color IN ('WHITE', 'BLACK')),
    san          VARCHAR(16)  NOT NULL,
    uci          VARCHAR(8)   NOT NULL,
    fen_after    TEXT         NOT NULL,
    played_at    TIMESTAMP    NOT NULL
);

CREATE INDEX idx_moves_game_order ON moves (game_id, move_number, color);
