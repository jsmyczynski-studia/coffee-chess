package pl.coffeechess.game_service.domain;

import org.junit.jupiter.api.Test;
import pl.coffeechess.game_service.model.domain.Board;
import pl.coffeechess.game_service.model.domain.pieces.*;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class BoardTest {

    private final String startingFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w";

    @Test
    void shouldCreateBoardFromFen() {
        Board board = new Board(startingFen);
        Piece[][] startingGrid = new Piece[8][8];

        startingGrid[0][0] = new Rook(false);   // a8
        startingGrid[1][0] = new Knight(false); // b8
        startingGrid[2][0] = new Bishop(false); // c8
        startingGrid[3][0] = new Queen(false);  // d8
        startingGrid[4][0] = new King(false);   // e8
        startingGrid[5][0] = new Bishop(false); // f8
        startingGrid[6][0] = new Knight(false); // g8
        startingGrid[7][0] = new Rook(false);   // h8

        for (int x = 0; x < 8; x++) {
            startingGrid[x][1] = new Pawn(false);
        }
        for (int x = 0; x < 8; x++) {
            startingGrid[x][6] = new Pawn(true);
        }

        startingGrid[0][7] = new Rook(true);    // a1
        startingGrid[1][7] = new Knight(true);  // b1
        startingGrid[2][7] = new Bishop(true);  // c1
        startingGrid[3][7] = new Queen(true);   // d1
        startingGrid[4][7] = new King(true);    // e1
        startingGrid[5][7] = new Bishop(true);  // f1
        startingGrid[6][7] = new Knight(true);  // g1
        startingGrid[7][7] = new Rook(true);    // h1

        assertTrue(Arrays.deepEquals(startingGrid, board.getGrid()),
                "Wygenerowana plansza powinna odpowiadać ręcznie ułożonej tablicy");
    }

    @Test
    void shouldGenerateStartingFenFromBoard() {
        Board board = new Board(startingFen);

        String generatedFen = board.toFen();

        assertEquals(startingFen, generatedFen,
                "Wygenerowany FEN dla planszy startowej musi być identyczny z wejściowym");
    }

    @Test
    void shouldGenerateFenAndGroupEmptySquares() {
        String customFen = "8/8/4p3/8/8/7K/8/8 b";
        Board board = new Board(customFen);

        String generatedFen = board.toFen();

        assertEquals(customFen, generatedFen,
                "Algorytm musi poprawnie zamieniać puste obiekty w zliczone cyfry");
    }

    @Test
    void shouldReflectBoardChangesInFen() {
        Board board = new Board(startingFen);

        board.getGrid()[4][6] = null;
        board.getGrid()[4][4] = new Pawn(true);
        board.setWhiteToMove(false);

        String expectedFenAfterMove = "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b";

        String generatedFen = board.toFen();

        assertEquals(expectedFenAfterMove, generatedFen,
                "FEN musi odzwierciedlać zmiany stanu po ręcznym przesunięciu figur na gridzie");
    }
}