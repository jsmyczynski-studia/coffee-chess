package pl.coffeechess.analysis.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidFenException.class)
    public ProblemDetail handleInvalidFen(InvalidFenException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        detail.setTitle("Invalid FEN");
        return detail;
    }

    @ExceptionHandler(EngineAnalysisException.class)
    public ProblemDetail handleEngineAnalysis(EngineAnalysisException ex) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY, ex.getMessage());
        detail.setTitle("Engine analysis failed");
        return detail;
    }
}
