package pl.coffeechess.analysis.validation;

import pl.coffeechess.analysis.exception.InvalidFenException;

public final class FenValidator {

    private FenValidator() {
    }

    public static String normalizeAndValidate(String fen) {
        if (fen == null || fen.isBlank()) {
            throw new InvalidFenException("FEN is required.");
        }

        String trimmed = fen.trim();
        String[] parts = trimmed.split("\\s+");
        if (parts.length < 2) {
            throw new InvalidFenException("FEN must include the board and side to move.");
        }

        validateBoard(parts[0]);

        if (!parts[1].equals("w") && !parts[1].equals("b")) {
            throw new InvalidFenException("Side to move must be 'w' or 'b'.");
        }

        if (parts.length >= 3 && !parts[2].equals("-") && !parts[2].matches("^[KQkq]+$")) {
            throw new InvalidFenException("Invalid castling availability in FEN.");
        }

        if (parts.length >= 4 && !parts[3].equals("-") && !parts[3].matches("^[a-h][36]$")) {
            throw new InvalidFenException("Invalid en passant square in FEN.");
        }

        if (parts.length >= 5 && !parts[4].matches("^\\d+$")) {
            throw new InvalidFenException("Invalid halfmove clock in FEN.");
        }

        if (parts.length >= 6 && !parts[5].matches("^\\d+$")) {
            throw new InvalidFenException("Invalid fullmove number in FEN.");
        }

        return trimmed;
    }

    private static void validateBoard(String board) {
        String[] ranks = board.split("/");
        if (ranks.length != 8) {
            throw new InvalidFenException("Board must contain exactly 8 ranks.");
        }

        int kings = 0;
        for (String rank : ranks) {
            if (rank.isEmpty()) {
                throw new InvalidFenException("Rank cannot be empty.");
            }

            int squares = 0;
            for (int i = 0; i < rank.length(); i++) {
                char ch = rank.charAt(i);
                if (Character.isDigit(ch)) {
                    if (ch == '0') {
                        throw new InvalidFenException("Rank cannot contain zero-length empty runs.");
                    }
                    squares += Character.getNumericValue(ch);
                } else if (isPiece(ch)) {
                    squares++;
                    if (ch == 'K' || ch == 'k') {
                        kings++;
                    }
                } else {
                    throw new InvalidFenException("Board contains invalid piece character: " + ch);
                }
            }

            if (squares != 8) {
                throw new InvalidFenException("Each rank must describe exactly 8 squares.");
            }
        }

        if (kings != 2) {
            throw new InvalidFenException("Board must contain exactly one white king and one black king.");
        }
    }

    private static boolean isPiece(char ch) {
        return switch (ch) {
            case 'p', 'r', 'n', 'b', 'q', 'k', 'P', 'R', 'N', 'B', 'Q', 'K' -> true;
            default -> false;
        };
    }
}
