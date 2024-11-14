package meowKai.CQuiS_backend.exception;

public class RateLimitExceededException extends RuntimeException{
    public RateLimitExceededException(String message) {
        super(message);
    }
}
