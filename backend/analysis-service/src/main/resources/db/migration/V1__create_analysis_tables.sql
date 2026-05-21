CREATE TABLE game_analysis (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_id         UUID UNIQUE NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                        CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED')),
    blunders        INT NOT NULL DEFAULT 0,
    mistakes        INT NOT NULL DEFAULT 0,
    inaccuracies    INT NOT NULL DEFAULT 0,
    white_accuracy  DOUBLE PRECISION,
    black_accuracy  DOUBLE PRECISION,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    completed_at    TIMESTAMP
);

CREATE TABLE move_analysis (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_analysis_id  UUID NOT NULL REFERENCES game_analysis(id) ON DELETE CASCADE,
    move_number       INT NOT NULL,
    move_san          VARCHAR(20) NOT NULL,
    fen_after         TEXT,
    best_move         VARCHAR(20),
    evaluation        DOUBLE PRECISION,
    move_quality      VARCHAR(20) CHECK (move_quality IN ('BRILLIANT','BEST','GOOD','INACCURACY','MISTAKE','BLUNDER')),
    comment           TEXT
);

CREATE TABLE analysis_report (
    id                  UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_analysis_id    UUID UNIQUE NOT NULL REFERENCES game_analysis(id) ON DELETE CASCADE,
    summary             TEXT,
    white_summary       TEXT,
    black_summary       TEXT,
    key_moment_move     INT,
    key_moment_comment  TEXT
);

CREATE INDEX idx_move_analysis_game ON move_analysis (game_analysis_id, move_number);