package meowKai.CQuiS_backend.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CustomErrorResponse {

    private final String message;
    private final LocalDateTime timestamp;
    private final HttpStatus status;

    public CustomErrorResponse(String message, HttpStatus status) {
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.status = status;
    }
}
