package pl.coffeechess.game.analysis;

/**
 * Classifies how good a played move was relative to the engine's best line (eval loss in pawns).
 */
public final class MoveQualityClassifier {

    private MoveQualityClassifier() {
    }

    public static MoveQuality classify(double playedEval, double bestEval) {
        double loss = bestEval - playedEval;
        if (loss >= 3.0) {
            return MoveQuality.BLUNDER;
        }
        if (loss >= 1.5) {
            return MoveQuality.MISTAKE;
        }
        if (loss >= 0.5) {
            return MoveQuality.INACCURACY;
        }
        if (loss <= -0.1) {
            return MoveQuality.BRILLIANT;
        }
        return MoveQuality.GOOD;
    }
}
