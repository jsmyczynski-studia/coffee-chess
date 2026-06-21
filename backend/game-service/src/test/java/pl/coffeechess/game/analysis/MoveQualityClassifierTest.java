package pl.coffeechess.game.analysis;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MoveQualityClassifierTest {

    @Test
    void classify_brilliantWhenPlayedMoveIsMuchBetterThanBestLine() {
        assertThat(MoveQualityClassifier.classify(2.0, 0.5)).isEqualTo(MoveQuality.BRILLIANT);
        assertThat(MoveQualityClassifier.classify(0.3, 0.0)).isEqualTo(MoveQuality.BRILLIANT);
    }

    @Test
    void classify_goodWhenLossIsSmall() {
        assertThat(MoveQualityClassifier.classify(0.4, 0.5)).isEqualTo(MoveQuality.GOOD);
        assertThat(MoveQualityClassifier.classify(-0.05, 0.0)).isEqualTo(MoveQuality.GOOD);
    }

    @Test
    void classify_inaccuracyMistakeAndBlunderByCentipawnLoss() {
        assertThat(MoveQualityClassifier.classify(0.0, 0.6)).isEqualTo(MoveQuality.INACCURACY);
        assertThat(MoveQualityClassifier.classify(0.0, 1.6)).isEqualTo(MoveQuality.MISTAKE);
        assertThat(MoveQualityClassifier.classify(0.0, 3.2)).isEqualTo(MoveQuality.BLUNDER);
    }

    @Test
    void classify_usesThresholdBoundaries() {
        assertThat(MoveQualityClassifier.classify(0.0, 0.5)).isEqualTo(MoveQuality.INACCURACY);
        assertThat(MoveQualityClassifier.classify(0.0, 1.5)).isEqualTo(MoveQuality.MISTAKE);
        assertThat(MoveQualityClassifier.classify(0.0, 3.0)).isEqualTo(MoveQuality.BLUNDER);
        assertThat(MoveQualityClassifier.classify(0.1, 0.0)).isEqualTo(MoveQuality.BRILLIANT);
    }
}
