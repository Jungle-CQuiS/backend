package meowKai.CQuiS_backend.dto.request;

import lombok.*;
import meowKai.CQuiS_backend.domain.ResponseStatus;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RequestSelectAnswerDto {
    private ResponseStatus responseStatus;
    private Long number;
    private Long roomId;
}
