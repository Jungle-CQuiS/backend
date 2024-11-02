package meowKai.CQuiS_backend.dto.request;

import lombok.*;
import meowKai.CQuiS_backend.domain.ResponseStatus;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RequestSelectQuizDto {
    private ResponseStatus responseStatus;
    private Long number;
    private Long roomId;
}
