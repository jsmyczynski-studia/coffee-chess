package pl.coffeechess.game_service.model.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pl.coffeechess.game_service.model.domain.pieces.*;

@Data
@EqualsAndHashCode
public class Board{
    //Pierwsza lista to kolumny, a druga to wiersze
    private final Piece[][] grid = new Piece[8][8];
    private boolean whiteToMove;

    // Budowa obiektu z FEN
    public Board(String fen) {
        loadFromFen(fen);
    }

    private void loadFromFen(String fen) {
        String[] parts = fen.split(" ");
        String piecesPart = parts[0];
        String[] piecesRows = piecesPart.split("/");

        for (int y = 0; y < piecesRows.length; y++) {
            int x = 0;

            for (char letter : piecesRows[y].toCharArray()) {
                if (Character.isDigit(letter)) {
                    x += Character.getNumericValue(letter);
                } else {
                    switch (letter) {
                        case 'p' -> grid[x][y] = new Pawn(false);
                        case 'P' -> grid[x][y] = new Pawn(true);
                        case 'r' -> grid[x][y] = new Rook(false);
                        case 'R' -> grid[x][y] = new Rook(true);
                        case 'n' -> grid[x][y] = new Knight(false);
                        case 'N' -> grid[x][y] = new Knight(true);
                        case 'b' -> grid[x][y] = new Bishop(false);
                        case 'B' -> grid[x][y] = new Bishop(true);
                        case 'q' -> grid[x][y] = new Queen(false);
                        case 'Q' -> grid[x][y] = new Queen(true);
                        case 'k' -> grid[x][y] = new King(false);
                        case 'K' -> grid[x][y] = new King(true);
                    }
                    x++;
                }
            }
        }

        this.whiteToMove = parts[1].equals("w");
        // TODO parts[2-4] - en passant, mozliwe remisy
    }

    public String toFen() {
        StringBuilder sb = new StringBuilder();

        for (int y = 0; y < 8; y++) {
            int emptySquares = 0;

            for (int x = 0; x < 8; x++) {
                Piece piece = grid[x][y];

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

            if (emptySquares > 0) {
                sb.append(emptySquares);
            }

            if (y < 7) {
                sb.append("/");
            }
        }

        sb.append(whiteToMove ? " w" : " b");

        // TODO: (Roszady, bicie w przelocie, licznik półruchów, numer tury).

        return sb.toString();
    }

    public Piece getPieceAt(Position pos) {
        return grid[pos.x()][pos.y()];
    }

    public Piece doMove(Move move) {
        Piece capturedPiece = grid[move.to().x()][move.to().y()];
        grid[move.to().x()][move.to().y()] = grid[move.from().x()][move.from().y()];
        grid[move.from().x()][move.from().y()] = null;
        return capturedPiece;
    }

    public void undoMove(Move move, Piece capturedPiece) {
        grid[move.from().x()][move.from().y()] = grid[move.to().x()][move.to().y()];
        grid[move.to().x()][move.to().y()] = capturedPiece;
    }
}