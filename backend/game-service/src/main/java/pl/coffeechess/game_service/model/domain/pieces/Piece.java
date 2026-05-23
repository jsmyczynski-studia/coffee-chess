package pl.coffeechess.game_service.model.domain.pieces;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.coffeechess.game_service.model.domain.Board;
import pl.coffeechess.game_service.model.domain.Move;
import pl.coffeechess.game_service.model.domain.Position;

import java.util.List;

@Data
@EqualsAndHashCode
public abstract class Piece {
    protected boolean isWhite;

    public Piece(boolean isWhite) {
        this.isWhite = isWhite;
    }

    public abstract List<Move> getPseudoLegalMoves(Board board, Position currentPosition);

    public abstract char getFenChar();
}
