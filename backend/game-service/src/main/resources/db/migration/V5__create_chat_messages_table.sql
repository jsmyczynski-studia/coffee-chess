CREATE TABLE chat_messages (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    game_id      UUID         NOT NULL REFERENCES games(id) ON DELETE CASCADE,
    author_id    UUID,
    type         VARCHAR(16)  NOT NULL CHECK (type IN ('USER', 'SYSTEM', 'BOT_LLM')),
    text         TEXT         NOT NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_chat_messages_game ON chat_messages (game_id, created_at);
