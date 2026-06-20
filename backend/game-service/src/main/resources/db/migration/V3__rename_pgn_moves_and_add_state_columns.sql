ALTER TABLE games RENAME COLUMN pgn_moves TO move_list_uci;

ALTER TABLE games ALTER COLUMN white_player_id DROP NOT NULL;
ALTER TABLE games ALTER COLUMN black_player_id DROP NOT NULL;

ALTER TABLE games ADD COLUMN draw_offered_by VARCHAR(8)
    CHECK (draw_offered_by IS NULL OR draw_offered_by IN ('WHITE', 'BLACK'));

ALTER TABLE games ADD COLUMN halfmove_clock BIGINT NOT NULL DEFAULT 0;

ALTER TABLE games ADD COLUMN position_history TEXT NOT NULL DEFAULT '';
