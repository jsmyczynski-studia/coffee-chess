package pl.coffeechess.game.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// mapuje wyjątki silnika na odpowiednie kody http
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        String msg = ex.getMessage() == null ? "" : ex.getMessage();

        HttpStatus status;
        String title;

        if (msg.equals("It's not your turn!")) {
            status = HttpStatus.CONFLICT;
            title = "Not your turn";
        } else if (msg.equals("Illegal move!") || msg.startsWith("Illegal move from")) {
            status = HttpStatus.UNPROCESSABLE_ENTITY;
            title = "Illegal move";
        } else if (msg.equals("Game has already ended.")) {
            status = HttpStatus.CONFLICT;
            title = "Game already ended";
        } else if (msg.equals("Invalid move format")
                || msg.startsWith("Niepoprawny format ruchu")
                || msg.startsWith("Square must use algebraic notation")
                || msg.startsWith("Square is outside the board")) {
            status = HttpStatus.BAD_REQUEST;
            title = "Invalid move format";
        } else if (msg.equals("Game doesn't exist")) {
            status = HttpStatus.NOT_FOUND;
            title = "Game not found";
        } else if (msg.equals("Player is not a participant of this game.")) {
            status = HttpStatus.FORBIDDEN;
            title = "Not a participant";
        } else if (msg.equals("No draw offer pending.")
                || msg.equals("Cannot accept your own draw offer.")
                || msg.equals("Cannot decline your own draw offer.")
                || msg.equals("You already have a pending draw offer.")) {
            status = HttpStatus.CONFLICT;
            title = "Draw offer conflict";
        } else if (msg.equals("Creator must be one of the participants.")) {
            status = HttpStatus.BAD_REQUEST;
            title = "Invalid participants";
        } else if (msg.equals("JWT subject is not a valid player UUID.")) {
            status = HttpStatus.UNAUTHORIZED;
            title = "Invalid principal";
        } else {
            status = HttpStatus.BAD_REQUEST;
            title = "Bad request";
        }

        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, msg);
        pd.setTitle(title);
        return pd;
    }

    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
        pd.setTitle("Conflict");
        return pd;
    }
}
