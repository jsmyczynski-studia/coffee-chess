package pl.coffeechess.game.model.board;

import lombok.Data;
import pl.coffeechess.game.model.enums.Color;
import pl.coffeechess.game.model.enums.PieceType;
import pl.coffeechess.game.model.piece.Bishop;
import pl.coffeechess.game.model.piece.King;
import pl.coffeechess.game.model.piece.Knight;
import pl.coffeechess.game.model.piece.Pawn;
import pl.coffeechess.game.model.piece.Piece;
import pl.coffeechess.game.model.piece.Queen;
import pl.coffeechess.game.model.piece.Rook;

import java.util.Objects;

@Data
public class GameBoard {

    public static final int BOARD_SIZE = 8;

    private final Piece[][] squares;

    private Color activeColor = Color.WHITE;

    public GameBoard(String fen) {
        this.squares = new Piece[BOARD_SIZE][BOARD_SIZE];
        loadFromFen(fen);
    }

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

    public void undoMove(String from, String to, Piece capturedPiece) {
        int[] source = parseSquare(from);
        int[] target = parseSquare(to);

        squares[source[0]][source[1]] = squares[target[0]][target[1]];
        squares[target[0]][target[1]] = capturedPiece;
    }

    private void loadFromFen(String fen) {
        String[] parts = fen.split(" ");
        String[] piecesRows = parts[0].split("/");

        for (int row = 0; row < piecesRows.length; row++) {
            int col = 0;
            for (char letter : piecesRows[row].toCharArray()) {
                if (Character.isDigit(letter)) {
                    col += Character.getNumericValue(letter);
                } else {
                    switch (letter) {
                        case 'p' -> squares[row][col] = new Pawn(Color.BLACK);
                        case 'P' -> squares[row][col] = new Pawn(Color.WHITE);
                        case 'r' -> squares[row][col] = new Rook(Color.BLACK);
                        case 'R' -> squares[row][col] = new Rook(Color.WHITE);
                        case 'n' -> squares[row][col] = new Knight(Color.BLACK);
                        case 'N' -> squares[row][col] = new Knight(Color.WHITE);
                        case 'b' -> squares[row][col] = new Bishop(Color.BLACK);
                        case 'B' -> squares[row][col] = new Bishop(Color.WHITE);
                        case 'q' -> squares[row][col] = new Queen(Color.BLACK);
                        case 'Q' -> squares[row][col] = new Queen(Color.WHITE);
                        case 'k' -> squares[row][col] = new King(Color.BLACK);
                        case 'K' -> squares[row][col] = new King(Color.WHITE);
                    }
                    col++;
                }
            }
        }
        this.activeColor = parts[1].equals("w") ? Color.WHITE : Color.BLACK;
        //TODO en passant, roszady itp
    }

    public String toFen() {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < BOARD_SIZE; row++) {
            int emptySquares = 0;
            for (int col = 0; col < BOARD_SIZE; col++) {
                Piece piece = squares[row][col];
                if (piece == null) {
                    emptySquares++;
                } else {
                    if (emptySquares > 0) {
                        sb.append(emptySquares);
                        emptySquares = 0;
                    }
                    sb.append(piece.getFenChar());
                }
            }
            if (emptySquares > 0) sb.append(emptySquares);
            if (row < 7) sb.append("/");
        }

        sb.append(activeColor == Color.WHITE ? " w" : " b");
        sb.append(" - - 0 1");
        //TODO en passant, roszady itp
        return sb.toString();
    }

    public void promotePiece(String square, PieceType promotionType, Color color) {
        Piece promotedPiece = switch (promotionType) {
            case QUEEN -> new Queen(color);
            case ROOK -> new Rook(color);
            case BISHOP -> new Bishop(color);
            case KNIGHT -> new Knight(color);
            default -> throw new IllegalArgumentException("unknown promotion type");
        };
        placePiece(square, promotedPiece);
    }
}
