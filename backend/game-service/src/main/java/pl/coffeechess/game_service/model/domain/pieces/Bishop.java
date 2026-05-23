package pl.coffeechess.game_service.model.domain.pieces;

import pl.coffeechess.game_service.model.domain.Board;
import pl.coffeechess.game_service.model.domain.Move;
import pl.coffeechess.game_service.model.domain.Position;

import java.util.List;

public class Bishop extends Piece{
    public Bishop(boolean isWhite) {
        super(isWhite);
    }

    @Override
    public List<Move> getPseudoLegalMoves(Board board, Position currentPosition) {
        return List.of();
    }

    @Override
    public char getFenChar() {
        if (isWhite)
            return 'B';
        else
            return 'b';
    }
}
