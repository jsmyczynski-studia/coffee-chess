package pl.coffeechess.game.model.board;

import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.piece.Bishop;
import pl.coffeechess.game.model.piece.King;
import pl.coffeechess.game.model.piece.Knight;
import pl.coffeechess.game.model.piece.Pawn;
import pl.coffeechess.game.model.piece.Piece;
import pl.coffeechess.game.model.piece.Queen;
import pl.coffeechess.game.model.piece.Rook;

import java.util.Objects;

public class GameBoard {

    public static final int BOARD_SIZE = 8;

    private final Piece[][] squares;

    private GameBoard() {
        this.squares = new Piece[BOARD_SIZE][BOARD_SIZE];
    }

    public static GameBoard empty() {
        return new GameBoard();
    }

    public static GameBoard standardSetup() {
        GameBoard board = new GameBoard();
        board.setupBackRank(Color.BLACK, 0);
        board.setupPawns(Color.BLACK, 1);
        board.setupPawns(Color.WHITE, 6);
        board.setupBackRank(Color.WHITE, 7);
        return board;
    }

    public Piece getPieceAt(String square) {
        int[] position = parseSquare(square);
        return squares[position[0]][position[1]];
    }

    public boolean isEmpty(String square) {
        return getPieceAt(square) == null;
    }

    public boolean hasPiece(String square) {
        return getPieceAt(square) != null;
    }

    public void placePiece(String square, Piece piece) {
        Objects.requireNonNull(piece, "piece cannot be null");
        int[] position = parseSquare(square);
        squares[position[0]][position[1]] = piece;
    }

    public Piece removePiece(String square) {
        int[] position = parseSquare(square);
        Piece removedPiece = squares[position[0]][position[1]];
        squares[position[0]][position[1]] = null;
        return removedPiece;
    }

    public Piece movePiece(String from, String to) {
        int[] source = parseSquare(from);
        int[] target = parseSquare(to);

        MoveValidator.validateMove(this, from, to);

        Piece piece = squares[source[0]][source[1]];
        Piece targetPiece = squares[target[0]][target[1]];

        squares[target[0]][target[1]] = piece;
        squares[source[0]][source[1]] = null;
        return targetPiece;
    }

    public boolean isPathClear(String from, String to) {
        int[] source = parseSquare(from);
        int[] target = parseSquare(to);
        int sourceRow = source[0];
        int sourceColumn = source[1];
        int targetRow = target[0];
        int targetColumn = target[1];
        int rowStep = Integer.compare(targetRow, sourceRow);
        int columnStep = Integer.compare(targetColumn, sourceColumn);
        int currentRow = sourceRow + rowStep;
        int currentColumn = sourceColumn + columnStep;

        while (currentRow != targetRow || currentColumn != targetColumn) {
            if (squares[currentRow][currentColumn] != null) {
                return false;
            }
            currentRow += rowStep;
            currentColumn += columnStep;
        }

        return true;
    }

    public int getRow(String square) {
        return parseSquare(square)[0];
    }

    public int getColumn(String square) {
        return parseSquare(square)[1];
    }

    private void setupBackRank(Color color, int row) {
        squares[row][0] = new Rook(color);
        squares[row][1] = new Knight(color);
        squares[row][2] = new Bishop(color);
        squares[row][3] = new Queen(color);
        squares[row][4] = new King(color);
        squares[row][5] = new Bishop(color);
        squares[row][6] = new Knight(color);
        squares[row][7] = new Rook(color);
    }

    private void setupPawns(Color color, int row) {
        for (int column = 0; column < BOARD_SIZE; column++) {
            squares[row][column] = new Pawn(color);
        }
    }

    private int[] parseSquare(String square) {
        if (square == null || square.length() != 2) {
            throw new IllegalArgumentException("Square must use algebraic notation, for example e4");
        }

        char file = square.charAt(0);
        char rank = square.charAt(1);
        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') {
            throw new IllegalArgumentException("Square is outside the board: " + square);
        }

        int row = BOARD_SIZE - Character.getNumericValue(rank);
        int column = file - 'a';
        return new int[]{row, column};
    }
}
