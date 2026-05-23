package pl.coffeechess.game_service.domain.pieces;

import org.junit.jupiter.api.Test;
import pl.coffeechess.game_service.model.domain.pieces.Pawn;
import pl.coffeechess.game_service.model.domain.pieces.Piece;

import static org.junit.jupiter.api.Assertions.*;

class PieceTest {
    @Test
    public void equalsAndHashCodeTestForPieces() {
        Piece pawnW1 = new Pawn(true);
        Piece pawnB = new Pawn(false);
        Piece pawnW2 = new Pawn(true);
        assertEquals(pawnW1, pawnW2);
        assertNotEquals(pawnW1, pawnB);
    }
}