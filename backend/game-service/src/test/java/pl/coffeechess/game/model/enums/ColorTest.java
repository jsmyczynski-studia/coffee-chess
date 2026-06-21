package pl.coffeechess.game.model.enums;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ColorTest {

    @Test
    void oppositeOfWhiteIsBlack() {
        assertThat(Color.WHITE.opposite()).isEqualTo(Color.BLACK);
    }

    @Test
    void oppositeOfBlackIsWhite() {
        assertThat(Color.BLACK.opposite()).isEqualTo(Color.WHITE);
    }

    @Test
    void oppositeIsInvolution() {
        for (Color color : Color.values()) {
            assertThat(color.opposite().opposite()).isEqualTo(color);
        }
    }
}
