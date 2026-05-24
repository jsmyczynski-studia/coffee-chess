package pl.coffeechess.game.model.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ColorTest {

    @Test
    void oppositeOfWhiteIsBlack() {
        assertEquals(Color.BLACK, Color.WHITE.opposite());
    }

    @Test
    void oppositeOfBlackIsWhite() {
        assertEquals(Color.WHITE, Color.BLACK.opposite());
    }

    @Test
    void oppositeIsInvolution() {
        for (Color c : Color.values()) {
            assertEquals(c, c.opposite().opposite());
        }
    }
}
