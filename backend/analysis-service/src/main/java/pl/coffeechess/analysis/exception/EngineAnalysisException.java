package pl.coffeechess.analysis.exception;

public class EngineAnalysisException extends RuntimeException {

    public EngineAnalysisException(String message) {
        super(message);
    }

    public EngineAnalysisException(String message, Throwable cause) {
        super(message, cause);
    }
}
