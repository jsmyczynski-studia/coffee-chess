package pl.coffeechess.game_service.model.domain;

public record Move(Position from, Position to, Character promotionPiece) {
    public static Move fromUciNotation(String uci) {
        if (uci == null || (uci.length() != 4 && uci.length() != 5)) {
            throw new IllegalArgumentException("Bad data format: " + uci);
        }

        int fromX = uci.charAt(0) - 'a';
        int fromY = 8 - Character.getNumericValue(uci.charAt(1));

        int toX = uci.charAt(2) - 'a';
        int toY = 8 - Character.getNumericValue(uci.charAt(3));

        Character promotion = uci.length() == 5 ? uci.charAt(4) : null;
        return new Move(new Position(fromX, fromY), new Position(toX, toY), promotion);
    }
}
