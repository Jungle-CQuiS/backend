package meowKai.CQuiS_backend.global;

import meowKai.CQuiS_backend.exception.CustomErrorResponse;
import meowKai.CQuiS_backend.exception.RateLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.TOO_MANY_REQUESTS)
    @ExceptionHandler(RateLimitExceededException.class)
    public CustomErrorResponse handleRateLimitExceeded(RateLimitExceededException ex) {
        return new CustomErrorResponse(
                ex.getMessage(),
                HttpStatus.TOO_MANY_REQUESTS
        );
    }
}
