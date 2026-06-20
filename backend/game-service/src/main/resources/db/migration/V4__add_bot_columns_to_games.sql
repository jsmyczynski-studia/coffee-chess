ALTER TABLE games ADD COLUMN vs_bot BOOLEAN NOT NULL DEFAULT FALSE;

ALTER TABLE games ADD COLUMN bot_color VARCHAR(8)
    CHECK (bot_color IS NULL OR bot_color IN ('WHITE', 'BLACK'));

ALTER TABLE games ADD COLUMN bot_difficulty VARCHAR(16)
    CHECK (bot_difficulty IS NULL OR bot_difficulty IN ('EASY', 'MEDIUM', 'HARD'));
