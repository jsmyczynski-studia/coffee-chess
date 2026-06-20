package pl.coffeechess.analysis.validation;

import org.junit.jupiter.api.Test;
import pl.coffeechess.analysis.exception.InvalidFenException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FenValidatorTest {

    private static final String START_FEN =
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    @Test
    void acceptsStandardStartingPosition() {
        assertThat(FenValidator.normalizeAndValidate(START_FEN)).isEqualTo(START_FEN);
    }

    @Test
    void rejectsBlankFen() {
        assertThatThrownBy(() -> FenValidator.normalizeAndValidate("  "))
                .isInstanceOf(InvalidFenException.class)
                .hasMessage("FEN is required.");
    }

    @Test
    void rejectsMissingSideToMove() {
        assertThatThrownBy(() -> FenValidator.normalizeAndValidate("8/8/8/8/8/8/8/8"))
                .isInstanceOf(InvalidFenException.class)
                .hasMessage("FEN must include the board and side to move.");
    }

    @Test
    void rejectsInvalidRankLength() {
        assertThatThrownBy(() -> FenValidator.normalizeAndValidate("8/8/8/8/8/8/8 w - - 0 1"))
                .isInstanceOf(InvalidFenException.class)
                .hasMessage("Board must contain exactly 8 ranks.");
    }

    @Test
    void rejectsInvalidSideToMove() {
        assertThatThrownBy(() -> FenValidator.normalizeAndValidate("7k/8/8/8/8/8/8/K7 x - - 0 1"))
                .isInstanceOf(InvalidFenException.class)
                .hasMessage("Side to move must be 'w' or 'b'.");
    }
}
